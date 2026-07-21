package org.opentrafficsim.editor.extensions.map.edit;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.curve.BezierCubic2d;
import org.djutils.draw.curve.Flattener2d;
import org.djutils.draw.curve.Flattener2d.NumSegments;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.opentrafficsim.base.geometry.OtsGeometryUtil;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.MapData;
import org.opentrafficsim.editor.extensions.map.MapLaneBasedObjectData;
import org.opentrafficsim.editor.extensions.map.MapLinkData;
import org.opentrafficsim.editor.extensions.map.MapNodeData;
import org.opentrafficsim.editor.extensions.map.MapVisualizationPanel;
import org.opentrafficsim.editor.extensions.map.edit.DraggableAnnotation.Draggable;
import org.opentrafficsim.editor.extensions.map.edit.DraggableAnnotation.Show;
import org.opentrafficsim.editor.extensions.map.edit.DraggableAnnotation.UpdateMode;
import org.opentrafficsim.editor.extensions.map.edit.HelperAnnotation.Helper;
import org.opentrafficsim.editor.extensions.map.edit.HelperAnnotation.Scaling;

import com.google.common.util.concurrent.AtomicDouble;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.dsol.swing.animation.d2.InputListener;

/**
 * Listener for mouse events on the map. This overrides certain methods of {@link InputListener} to disable standard inspection
 * and add functionalities for visual editing.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class MapInputListener extends InputListener implements EventListener
{

    /** Key for snap property. */
    private static final String SNAP_KEY = "map.snap";

    /** Key for grid size property. */
    private static final String SNAP_DISTANCE_KEY = "map.snapDistance";

    /** Key for snap angle property. */
    private static final String SNAP_ANGLE_KEY = "map.snapAngle";

    /** Grid size options. */
    private static final List<String> SNAP_DISTANCE_OPTIONS = List.of("1 cm", "5 cm", "10 cm", "50 cm", "1 m", "5 m", "10 m");

    /** Snap angle options. */
    private static final List<String> SNAP_ANGLE_OPTIONS = List.of("0.5 deg", "1 deg", "3 deg", "5 deg", "10 deg", "15 deg");

    /** Format for grid sizes. */
    private static final DecimalFormat GRID_FORMAT = new DecimalFormat("0.0#", DecimalFormatSymbols.getInstance(Locale.US));

    /** Format for snapped angles. */
    private static final DecimalFormat ANGLE_FORMAT = new DecimalFormat("0.0 deg", DecimalFormatSymbols.getInstance(Locale.US));

    /** Format for arbitrary double values. */
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.US));

    /** Offset from object location for any draggable that indicates some angle for the point. */
    private static final double ANGLE_POINT_OFFSET_PX = 30.0;

    /** Minimum Bezier shape value when dragging. */
    private static final double BEZIES_SHAPE_MIN = 0.01;

    /** Minimum Bezier shape value when dragging. */
    private static final double BEZIES_SHAPE_MAX = 2.0;

    /** Minimum distance between point on polyline. */
    private static final double POLYLINE_DIST_MIN_M = 0.05;

    /** Size of small node annotations. */
    private static final double ANNOTATION_PX = 3.5;

    /** Circle annotation. */
    private static final PolyLine2d CIRCLE = circle(ANNOTATION_PX, 32);

    /** Square annotation. */
    private static final PolyLine2d SQUARE =
            new PolyLine2d(new double[] {-ANNOTATION_PX, ANNOTATION_PX, ANNOTATION_PX, -ANNOTATION_PX, -ANNOTATION_PX},
                    new double[] {-ANNOTATION_PX, -ANNOTATION_PX, ANNOTATION_PX, ANNOTATION_PX, -ANNOTATION_PX});

    /** Editor. */
    private final OtsEditor editor;

    /** Editor map. */
    private final EditorMap editorMap;

    /** Mouse click screen coordinates. */
    private Point2D mousePressed = null;

    /** Whether currently dragging. */
    private boolean dragging = false;

    /** Selection visualization. */
    private Set<SelectionAnnotation> selectionIndicators = new LinkedHashSet<>();

    /** Currently selected draggable. */
    private DraggableAnnotation selectedDraggable;

    /** Set of draggables of current selected element. */
    private Set<DraggableAnnotation> draggables = new LinkedHashSet<>();

    /** Snap setting. */
    private boolean snapSetting;

    /** Snapping due to setting and not holding CTRL down. */
    private boolean snap;

    /** Grid size. */
    private Length snapDistance;

    /** Snap angle. */
    private Angle snapAngle;

    /**
     * Constructor.
     * @param editor editor
     * @param editorMap editor map
     */
    public MapInputListener(final OtsEditor editor, final EditorMap editorMap)
    {
        super(editorMap.getPanel());
        this.editor = editor;
        this.editorMap = editorMap;
        this.snapSetting = OtsEditor.PROPERTIES_STORE.getOptionalBoolean(SNAP_KEY).orElse(false);
        this.snapDistance = Length.valueOf(OtsEditor.PROPERTIES_STORE.getPropertyOrDefault(SNAP_DISTANCE_KEY, "10 cm"));
        this.snapAngle = Angle.valueOf(OtsEditor.PROPERTIES_STORE.getPropertyOrDefault(SNAP_ANGLE_KEY, "3 deg"));
    }

    /**
     * Returns an n-segment circle.
     * @param radius radius
     * @param n number of segments
     * @return circle
     */
    private static Polygon2d circle(final double radius, final int n)
    {
        double[] x = new double[n + 1];
        double[] y = new double[n + 1];
        for (int i = 0; i <= n; i++)
        {
            double ang = (Math.PI * 2.0 * i) / n;
            x[i] = radius * Math.cos(ang);
            y[i] = radius * Math.sin(ang);
        }
        return new Polygon2d(x, y);
    }

    @Override
    public void mousePressed(final MouseEvent e)
    {
        this.editorMap.getPanel().requestFocus();
        if (SwingUtilities.isLeftMouseButton(e))
        {
            this.mousePressed = e.getPoint();
            List<Locatable> locatables = this.editorMap.getPanel().getSelectedObjects(e.getPoint());
            if (!locatables.isEmpty())
            {
                // dragging?
                List<Draggable<?>> draggablesDatas = locatables.stream().filter(Draggable.class::isInstance)
                        .<Draggable<?>>map(Draggable.class::cast).toList();
                if (!draggablesDatas.isEmpty())
                {
                    Point2d world = this.editorMap.getPanel().getRenderableScale().getWorldCoordinates(e.getPoint(),
                            this.editorMap.getPanel().getExtent(), this.editorMap.getPanel().getSize());
                    Draggable<?> data = draggablesDatas.stream()
                            .min(Comparator.comparingDouble((d) -> d.getLocation().distance(world))).get();
                    if (this.selectedDraggable != null)
                    {
                        this.selectedDraggable.setSelected(false);
                    }
                    this.selectedDraggable =
                            this.draggables.stream().filter((d) -> d.getSource().equals(data)).findFirst().get();
                    this.selectedDraggable.setSelected(true);
                    return;
                }
            }
            if (this.selectedDraggable != null)
            {
                this.selectedDraggable.setSelected(false);
            }
            this.selectedDraggable = null;
        }
    }

    @Override
    public void mouseDragged(final MouseEvent e)
    {
        this.dragging = true;
        if (this.selectedDraggable != null)
        {
            this.snap = this.snapSetting ^ e.isControlDown();
            Point2d world = this.editorMap.getPanel().getRenderableScale().getWorldCoordinates(e.getPoint(),
                    this.editorMap.getPanel().getExtent(), this.editorMap.getPanel().getSize());
            this.selectedDraggable.mouseDragged(world);
            this.editorMap.update();
            e.consume();
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e)
    {
        if (this.dragging && this.selectedDraggable != null)
        {
            Point2d world = this.editorMap.getPanel().getRenderableScale().getWorldCoordinates(e.getPoint(),
                    this.editorMap.getPanel().getExtent(), this.editorMap.getPanel().getSize());
            this.selectedDraggable.mouseReleased(world);
            this.editor.repaint(); // not just panel to also update attribute or node value in (tree) tables
        }
        // Pan if either shift is down or the left mouse button is used.
        else if (SwingUtilities.isLeftMouseButton(e))
        {
            this.editorMap.getPanel().pan(this.mousePressed, e.getPoint());
            this.editorMap.update();
        }
        this.dragging = false;
    }

    @Override
    public void mouseClicked(final MouseEvent e)
    {

        this.editorMap.getPanel().requestFocus();

        if (this.selectedDraggable != null)
        {
            if (e.getClickCount() > 1)
            {
                this.selectedDraggable.getSource().writeDefaultValue();
                this.editor.repaint();
            }
            else if (e.isShiftDown())
            {
                this.selectedDraggable.showValue(this.editor);
            }
            return;
        }

        List<MapData> mapDatas = this.editorMap.getPanel().getSelectedObjects(e.getPoint()).stream()
                .filter(MapData.class::isInstance).map(MapData.class::cast).toList();
        if (mapDatas.size() == 1 && SwingUtilities.isLeftMouseButton(e))
        {
            select(mapDatas.get(0), e.isControlDown(), e.isShiftDown());
        }
        else if (!mapDatas.isEmpty() && SwingUtilities.isLeftMouseButton(e))
        {
            Optional<MapData> highest = mapDatas.stream().max(Comparator.comparingDouble(MapData::getZ));
            if (highest.isPresent())
            {
                select(highest.get(), e.isControlDown(), e.isShiftDown());
            }
        }
        else if (!mapDatas.isEmpty() && SwingUtilities.isRightMouseButton(e))
        {
            showSelectLocatableMenu(mapDatas, e);
        }
        else if (mapDatas.isEmpty() && SwingUtilities.isRightMouseButton(e))
        {
            showSettingsMenu(e);
        }
        else if (!e.isControlDown())
        {
            deselect();
        }
    }

    @Override
    public void keyPressed(final KeyEvent e)
    {
        super.keyPressed(e);
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_DELETE:
                if (this.selectedDraggable != null)
                {
                    this.selectedDraggable.delete();
                }
                else if (!this.selectionIndicators.isEmpty())
                {
                    if (this.selectionIndicators.size() >= 5
                            && !this.editor.dialogs().confirmGeneral("Delete " + this.selectionIndicators.size() + " objects?"))
                    {
                        return;
                    }
                    XsdTreeNode first = null;
                    for (SelectionAnnotation selectionAnnotation : this.selectionIndicators)
                    {
                        XsdTreeNode parent = selectionAnnotation.getSource().mapData().getNode().getParent();
                        if (first == null)
                        {
                            first = selectionAnnotation.getSource().mapData().getNode();
                            this.editor.getUndo().startAction(ActionType.REMOVE, first, null);
                        }
                        selectionAnnotation.getSource().mapData().getNode().remove();
                        this.editor.show(parent, null);
                    }
                }
                break;
            default:
        }
    }

    /**
     * Show menu to select locatable.
     * @param locatables list of locatables under mouse event
     * @param e mouse event
     */
    private void showSelectLocatableMenu(final List<MapData> locatables, final MouseEvent e)
    {
        JPopupMenu popup = new JPopupMenu();
        for (MapData mapData : locatables)
        {
            JMenuItem item = new JMenuItem(mapData.getNode().toString());
            item.addActionListener((ev) ->
            {
                select(mapData, e.isControlDown(), e.isShiftDown());
                this.editorMap.update();
            });
            popup.add(item);
        }
        popup.show(this.editorMap.getPanel(), (int) e.getPoint().getX(), (int) e.getPoint().getY());
    }

    /**
     * Show menu with map settings (snapping).
     * @param event mouse event
     */
    private void showSettingsMenu(final MouseEvent event)
    {
        JPopupMenu popup = new JPopupMenu();

        JCheckBoxMenuItem snapItem = new JCheckBoxMenuItem("Snap", this.snapSetting);
        snapItem.addActionListener((ev) ->
        {
            this.snapSetting = snapItem.isSelected();
            OtsEditor.PROPERTIES_STORE.setBoolean(SNAP_KEY, this.snapSetting);
        });
        popup.add(snapItem);

        JMenu gridMenu = new JMenu("Snap distance");
        ButtonGroup gridGroup = new ButtonGroup();
        for (String gridSizeStringValue : SNAP_DISTANCE_OPTIONS)
        {
            Length value = Length.valueOf(gridSizeStringValue);
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(gridSizeStringValue, this.snapDistance.eq(value));
            item.addActionListener((ev) ->
            {
                this.snapDistance = value;
                OtsEditor.PROPERTIES_STORE.setProperty(SNAP_DISTANCE_KEY, gridSizeStringValue);
            });
            gridGroup.add(item);
            gridMenu.add(item);
        }
        popup.add(gridMenu);

        JMenu angleMenu = new JMenu("Snap angle");
        ButtonGroup angleGroup = new ButtonGroup();
        for (String snapAngleStringValue : SNAP_ANGLE_OPTIONS)
        {
            Angle value = Angle.valueOf(snapAngleStringValue);
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(snapAngleStringValue, this.snapAngle.eq(value));
            item.addActionListener((ev) ->
            {
                this.snapAngle = value;
                OtsEditor.PROPERTIES_STORE.setProperty(SNAP_ANGLE_KEY, snapAngleStringValue);
            });
            angleGroup.add(item);
            angleMenu.add(item);
        }
        popup.add(angleMenu);

        popup.show(this.editorMap.getPanel(), (int) event.getPoint().getX(), (int) event.getPoint().getY());
    }

    /**
     * Select map data object.
     * @param mapData map data
     * @param add whether to add to selection
     * @param show show the accompanying {@link XsdTreeNode} in the tree
     */
    private void select(final MapData mapData, final boolean add, final boolean show)
    {
        Optional<SelectionAnnotation> currentlySelected =
                this.selectionIndicators.stream().filter((r) -> r.getSource().mapData().equals(mapData)).findFirst();
        if (add && currentlySelected.isPresent())
        {
            // ctrl-click on selected item, remove it from selection
            removeDraggables();
            this.selectionIndicators.remove(currentlySelected.get());
            this.editorMap.removeAnimation(currentlySelected.get());
            return;
        }
        else if (add && this.selectionIndicators.size() == 1)
        {
            removeDraggables();
        }
        else if (!add)
        {
            deselect();
        }
        this.selectionIndicators.add(new SelectionAnnotation(mapData, MapInputListener.this.editorMap));
        if (!add && this.selectionIndicators.size() == 1)
        {
            // in java 21 this can be a pretty switch-case pattern
            if (mapData instanceof MapNodeData nodeData)
            {
                addNodeDraggables(nodeData);
                mapData.addListener(this, MapData.MAP_DATA_CHANGED, ReferenceType.WEAK);
            }
            else if (mapData instanceof MapLinkData linkData)
            {
                if (linkData.isBezier())
                {
                    addBezierDraggables(linkData);
                    mapData.addListener(this, MapData.MAP_DATA_CHANGED, ReferenceType.WEAK);
                }
                else if (linkData.isPolyline())
                {
                    addPolylineDraggables(linkData);
                    mapData.addListener(this, MapData.MAP_DATA_CHANGED, ReferenceType.WEAK);
                }
            }
            else if (mapData instanceof MapLaneBasedObjectData laneBasedData)
            {
                addLaneBasedObjectDraggables(laneBasedData);
                mapData.addListener(this, MapData.MAP_DATA_CHANGED, ReferenceType.WEAK);
            }
        }
        this.editorMap.update();
        if (show)
        {
            this.editor.show(mapData.getNode(), null);
        }
    }

    @Override
    public void notify(final Event event)
    {
        if (this.selectionIndicators.size() == 1)
        {
            select(this.selectionIndicators.stream().findFirst().get().getSource().mapData(), false, false);
        }
    }

    /**
     * Add draggables to edit a node.
     * @param nodeData node data
     */
    private void addNodeDraggables(final MapNodeData nodeData)
    {
        // rotation draggable
        UnaryOperator<Point2d> rotationSnapper = (p) ->
        {
            double a = Math.atan2(p.y - nodeData.getLocation().y, p.x - nodeData.getLocation().x);
            if (this.snap)
            {
                a = Math.round(a / this.snapAngle.si) * this.snapAngle.si;
            }
            return getRotationPoint(nodeData, a);
        };
        Function<Point2d, Direction> rotationFunction =
                (p) -> Direction.ofSI(Math.atan2(p.y - nodeData.getLocation().y, p.x - nodeData.getLocation().x));
        Consumer<Direction> rotationWriter = (v) ->
        {
            String value = this.snap ? ANGLE_FORMAT.format(Math.toDegrees(v.si)) : Math.toDegrees(v.si) + " deg";
            MapInputListener.this.editor.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, nodeData.getNode(), "Direction");
            nodeData.getNode().setAttributeValue("Direction", value);
        };
        DraggableAnnotation rotationDraggable =
                new DraggableAnnotation(
                        new Draggable<>(getRotationPoint(nodeData, nodeData.getDirZ()), CIRCLE, rotationSnapper,
                                rotationFunction, rotationWriter).setShow(new Show(nodeData.getNode(), "Direction")),
                        MapInputListener.this.editorMap);
        this.draggables.add(rotationDraggable);

        // line from node to rotation draggable
        Function<Point2d, PolyLine2d> lineFunction = (p) ->
        {
            return new PolyLine2d(new Point2d(nodeData.getLocation().x - p.x, nodeData.getLocation().y - p.y),
                    new Point2d(0.0, 0.0));
        };
        HelperAnnotation rotationLine =
                new HelperAnnotation(new Helper(getRotationPoint(nodeData, nodeData.getDirZ()), lineFunction),
                        MapInputListener.this.editorMap, Scaling.SCALE);
        rotationDraggable.addHelperAnnotation(rotationLine, UpdateMode.DYNAMIC);

        // listen to panel extent to update rotation annotations as location is based on pixels
        this.editorMap.getPanel().addListener(new EventListener()
        {
            @Override
            public void notify(final Event event)
            {
                if (MapInputListener.this.draggables.contains(rotationDraggable))
                {
                    Point2d p = getRotationPoint(nodeData, nodeData.getDirZ());
                    rotationDraggable.getSource().setPointSnapped(p);
                    rotationLine.getSource().setLocation(p);
                }
                else
                {
                    MapInputListener.this.editorMap.getPanel().removeListener(this, MapVisualizationPanel.EXTENT_CHANGED);
                }
            }
        }, MapVisualizationPanel.EXTENT_CHANGED);

        // position draggable
        Consumer<Point2d> positionWriter = (p) ->
        {
            String value = pointToCoordinateString(p);
            MapInputListener.this.editor.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, nodeData.getNode(), "Coordinate");
            nodeData.getNode().setAttributeValue("Coordinate", value);
        };
        DraggableAnnotation positionDraggable = new DraggableAnnotation(
                new Draggable<>(nodeData.getLocation(), SQUARE, this::snapPointToGrid, (p) -> p, positionWriter)
                        .setShow(new Show(nodeData.getNode(), "Coordinate")),
                MapInputListener.this.editorMap);
        this.draggables.add(positionDraggable);

        // snap circle
        Polygon2d circle = circle(ANGLE_POINT_OFFSET_PX, 32);
        positionDraggable.addHelperAnnotation(new HelperAnnotation(new Helper(nodeData.getLocation(), (p) -> circle),
                MapInputListener.this.editorMap, Scaling.NO_SCALE), UpdateMode.STATIC);
    }

    /**
     * Creates a string representation of a point, suitable for XML (attribute) values. It will be of the form (1.23, 4.56).
     * @param point point
     * @return string representation of a point
     */
    private String pointToCoordinateString(final Point2d point)
    {
        return this.snap ? "(" + GRID_FORMAT.format(point.x) + "," + GRID_FORMAT.format(point.y) + ")"
                : "(" + point.x + "," + point.y + ")";
    }

    /**
     * Snap point to grid, if snapping is enabled.
     * @param point point to snap
     * @return point snapped to grid
     */
    private Point2d snapPointToGrid(final Point2d point)
    {
        Point2d snapped = point;
        if (this.snap)
        {
            double x = Math.round(point.x / this.snapDistance.si) * this.snapDistance.si;
            double y = Math.round(point.y / this.snapDistance.si) * this.snapDistance.si;
            snapped = new Point2d(x, y);
        }
        return snapped;
    }

    /**
     * Get point at some offset from object location in the direction of the angle.
     * @param data data of object at some location
     * @param angle angle at which to offset returned point
     * @return point offset from object of data in the direction of the angel
     */
    private Point2d getRotationPoint(final MapData data, final double angle)
    {
        double rX = this.editorMap.getPanel().getRenderableScale().getXScale(this.editorMap.getPanel().getExtent(),
                this.editorMap.getPanel().getSize());
        double rY = this.editorMap.getPanel().getRenderableScale().getYScale(this.editorMap.getPanel().getExtent(),
                this.editorMap.getPanel().getSize());
        double x = data.getLocation().x + ANGLE_POINT_OFFSET_PX * rX * Math.cos(angle);
        double y = data.getLocation().y + ANGLE_POINT_OFFSET_PX * rY * Math.sin(angle);
        return new Point2d(x, y);
    }

    /**
     * Add bezier link draggables. The link is checked to be a Bezier.
     * @param linkData link data
     */
    private void addBezierDraggables(final MapLinkData linkData)
    {
        // define geometry
        Function<Boolean, Double> computeWeight = (startSide) ->
        {
            if (!linkData.isWeightedBezier())
            {
                return 0.5;
            }
            DirectedPoint2d start = linkData.getDesignLine().getStartPoint();
            DirectedPoint2d end = linkData.getDesignLine().getEndPoint();
            double dStart = start.distance(new Ray2d(end).projectOrthogonalExtended(start));
            double dEnd = end.distance(new Ray2d(start).projectOrthogonalExtended(end));
            return (startSide ? dStart : dEnd) / (dStart + dEnd);
        };
        Supplier<PolyLine2d> lineSupplier1 = () ->
        {
            DirectedPoint2d start = linkData.getDesignLine().getStartPoint();
            DirectedPoint2d end = linkData.getDesignLine().getEndPoint();
            double distance = start.distance(end);
            double weight = computeWeight.apply(true);
            return new PolyLine2d(OtsGeometryUtil.translatePoint(start, distance * weight * BEZIES_SHAPE_MIN),
                    OtsGeometryUtil.translatePoint(start, distance * weight * BEZIES_SHAPE_MAX));
        };
        Supplier<PolyLine2d> lineSupplier2 = () ->
        {
            DirectedPoint2d start = linkData.getDesignLine().getStartPoint();
            DirectedPoint2d end = linkData.getDesignLine().getEndPoint();
            double distance = start.distance(end);
            double weight = computeWeight.apply(false);
            return new PolyLine2d(OtsGeometryUtil.translatePoint(end, -distance * weight * BEZIES_SHAPE_MIN),
                    OtsGeometryUtil.translatePoint(end, -distance * weight * BEZIES_SHAPE_MAX));
        };

        // value conversions
        AtomicDouble shapeValueHolder = new AtomicDouble();
        shapeValueHolder.set(linkData.getBezierShape());
        Function<Double, Double> fractionToShape = (f) -> BEZIES_SHAPE_MIN + f * (BEZIES_SHAPE_MAX - BEZIES_SHAPE_MIN);
        Function<Double, Double> shapeToFraction = (s) -> (s - BEZIES_SHAPE_MIN) / (BEZIES_SHAPE_MAX - BEZIES_SHAPE_MIN);
        Function<Supplier<PolyLine2d>, Function<Point2d, Double>> valueFunctionFactory = (lineSupplier) -> (p) ->
        {
            double v = fractionToShape.apply(lineSupplier.get().projectOrthogonalFractionalExtended(p));
            shapeValueHolder.set(v);
            return v;
        };
        Function<Point2d, Double> valueFunction1 = valueFunctionFactory.apply(lineSupplier1);
        Function<Point2d, Double> valueFunction2 = valueFunctionFactory.apply(lineSupplier2);

        // create draggables
        Consumer<Double> valueWriter = (v) ->
        {
            String value = v == null ? null : DOUBLE_FORMAT.format(v);
            MapInputListener.this.editor.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, linkData.getNode().getChild(0),
                    "Shape");
            linkData.getNode().getChild(0).setAttributeValue("Shape", value);
        };
        DraggableAnnotation draggable1 =
                createBezierDraggable(linkData, lineSupplier1, valueFunction1, valueWriter, shapeToFraction);
        DraggableAnnotation draggable2 =
                createBezierDraggable(linkData, lineSupplier2, valueFunction2, valueWriter, shapeToFraction);

        // shape prediction line
        Flattener2d flattener = new NumSegments(32);
        Function<Point2d, PolyLine2d> predictionFunction = (p) ->
        {
            double f = shapeToFraction.apply(shapeValueHolder.get());
            // this updates the location of the small circular shape draggables live
            draggable1.getSource().setPointSnapped(lineSupplier1.get().getLocationFractionExtended(f));
            draggable2.getSource().setPointSnapped(lineSupplier2.get().getLocationFractionExtended(f));
            PolyLine2d line = new BezierCubic2d(linkData.getDesignLine().getStartPoint(), draggable1.getSource().getLocation(),
                    draggable2.getSource().getLocation(), linkData.getDesignLine().getEndPoint()).toPolyLine(flattener);
            return OtsShape.transformLine(line, p);
        };
        HelperAnnotation predictionLine = new HelperAnnotation(new Helper(linkData.getLocation(), predictionFunction),
                MapInputListener.this.editorMap, Scaling.SCALE).setPredicate(() -> this.dragging);
        draggable1.addHelperAnnotation(predictionLine, UpdateMode.DYNAMIC);
        draggable2.addHelperAnnotation(predictionLine, UpdateMode.DYNAMIC);
    }

    /**
     * Create Bezier draggable.
     * @param linkData link data
     * @param lineSupplier line supplier
     * @param valueFunction value supplier
     * @param valueWriter value writer
     * @param shapeToFraction shape to fraction function
     * @return draggable
     */
    private DraggableAnnotation createBezierDraggable(final MapLinkData linkData, final Supplier<PolyLine2d> lineSupplier,
            final Function<Point2d, Double> valueFunction, final Consumer<Double> valueWriter,
            final Function<Double, Double> shapeToFraction)
    {
        Point2d point = lineSupplier.get().getLocationFractionExtended(shapeToFraction.apply(linkData.getBezierShape()));
        DraggableAnnotation draggable = new DraggableAnnotation(
                new Draggable<Double>(point, CIRCLE, p -> lineSupplier.get().closestPointOnPolyLine(p), valueFunction,
                        valueWriter).setDefaultValue(null).setShow(new Show(linkData.getNode().getChild(0), "Shape")),
                MapInputListener.this.editorMap);
        this.draggables.add(draggable);
        draggable.addHelperAnnotation(
                new HelperAnnotation(new Helper(linkData.getLocation(), p -> OtsShape.transformLine(lineSupplier.get(), p)),
                        MapInputListener.this.editorMap, Scaling.SCALE),
                UpdateMode.STATIC);
        return draggable;
    }

    /**
     * Add polyline link draggables. The link is checked to be a polyline.
     * @param linkData link data
     */
    private void addPolylineDraggables(final MapLinkData linkData)
    {

        // First segment is shown as: -----o-----[]
        // Intermediary segments are shown as: []-----o-----[]
        // Last segment is shown as: []-----o-----
        // [] = coordinate
        // o = intermediary, used to create new coordinates

        NavigableMap<XsdTreeNode, Point2d> coordinates = linkData.getPolylineCoordinates();
        if (coordinates.values().contains(null))
        {
            if (coordinates.size() > 1)
            {
                // at least one coordinate is invalid, show no editing draggables
                return;
            }
            else
            {
                // one coordinate tree node with empty or invalid coordinate value, allow map to show 1 draggable
                coordinates.clear();
            }
        }

        PolyLine2d line = linkData.getCenterLine();
        Point2d prev = line.getFirst();
        DraggableAnnotation intermediary = null;
        // location for intermediary node on first segment
        Point2d intermediaryLocation =
                line.getFirst().interpolate(linkData.getPolylineCoordinates().firstEntry().getValue(), 0.5);
        for (Entry<XsdTreeNode, Point2d> entry : coordinates.entrySet())
        {
            // intermediary point
            if (prev.distance(entry.getValue()) > POLYLINE_DIST_MIN_M * 2)
            {
                intermediary = addPolylineDraggable(linkData, line, intermediaryLocation, CIRCLE, (p) ->
                {
                    String value = pointToCoordinateString(p);
                    MapInputListener.this.editor.getUndo().startAction(ActionType.ADD, entry.getKey(), null);
                    XsdTreeNode newNode = entry.getKey().add();
                    newNode.move(-1);
                    MapInputListener.this.editor.getUndo().setPostActionShowNode(newNode);
                    newNode.setValue(value);
                    this.editor.updateTree();
                });
                addPolylinePrediction(linkData, entry.getKey(), intermediary, true);
            }

            // coordinate
            DraggableAnnotation coordinate =
                    addPolylineDraggable(linkData, line, linkData.getPolylineCoordinates().get(entry.getKey()), SQUARE, (p) ->
                    {
                        String value = pointToCoordinateString(p);
                        MapInputListener.this.editor.getUndo().startAction(ActionType.VALUE_CHANGE, entry.getKey(), null);
                        entry.getKey().setValue(value);
                    });
            coordinate.getSource().setShow(new Show(entry.getKey(), null)).setDeleter(() ->
            {
                if (entry.getKey().isRemovable())
                {
                    entry.getKey().remove();
                }
            });
            addPolylinePrediction(linkData, entry.getKey(), coordinate, false);

            // location for intermediary node on intermediary segment
            NavigableMap<XsdTreeNode, Point2d> ps = linkData.getPolylineCoordinates();
            // higher node as this is used in the next loop, and this higher node should be the next segment's lower node
            // higherEntry will be null on last segment, but then this location supplier is not used
            Entry<XsdTreeNode, Point2d> priorNode = ps.higherEntry(entry.getKey());
            intermediaryLocation = priorNode == null ? null : priorNode.getValue().interpolate(ps.get(entry.getKey()), 0.5);
            prev = entry.getValue();
        }
        if (prev.distance(line.getLast()) > POLYLINE_DIST_MIN_M * 2)
        {
            // location for intermediary node on last segment
            NavigableMap<XsdTreeNode, Point2d> coords = linkData.getPolylineCoordinates();
            Point2d from = coords.isEmpty() || coords.size() == 1 && coords.values().contains(null) ? line.getFirst()
                    : coords.lastEntry().getValue();
            intermediaryLocation = from.interpolate(line.getLast(), 0.5);
            intermediary = addPolylineDraggable(linkData, line, intermediaryLocation, CIRCLE, (p) ->
            {
                String value = pointToCoordinateString(p);
                XsdTreeNode newNode;
                if (coordinates.isEmpty())
                {
                    // special case of a single node with wrong coordinate that can be set through the map
                    newNode = linkData.getPolylineCoordinates().firstKey();
                    MapInputListener.this.editor.getUndo().startAction(ActionType.VALUE_CHANGE, newNode, null);
                }
                else
                {
                    MapInputListener.this.editor.getUndo().startAction(ActionType.ADD, coordinates.lastKey(), null);
                    newNode = coordinates.lastKey().add();
                }
                MapInputListener.this.editor.getUndo().setPostActionShowNode(newNode);
                newNode.setValue(value);
                this.editor.updateTree();
            });
            addPolylinePrediction(linkData, null, intermediary, true);
        }
    }

    /**
     * Add polyline draggable, either on an existing point, or intermediary.
     * @param linkData link data
     * @param line current line, to stay away from current points
     * @param initialLocation initial location
     * @param annotation annotation (square or circle)
     * @param valueWriter value writer upon mouse release
     * @return draggable
     */
    private DraggableAnnotation addPolylineDraggable(final MapLinkData linkData, final PolyLine2d line,
            final Point2d initialLocation, final PolyLine2d annotation, final Consumer<Point2d> valueWriter)
    {
        AtomicReference<Point2d> lastPoint = new AtomicReference<>(initialLocation);
        DraggableAnnotation draggable = new DraggableAnnotation(new Draggable<>(initialLocation, annotation, (p) ->
        {
            Point2d point = snapPointToGrid(p);
            // only update point if it is not too close to any existing point on the line (except itself)
            if (line.getPointList().stream()
                    .noneMatch((c) -> c.distance(point) < POLYLINE_DIST_MIN_M && c.distance(initialLocation) > 1e-9))
            {
                lastPoint.set(point);
            }
            return lastPoint.get();
        }, (p) -> p, valueWriter), MapInputListener.this.editorMap);
        draggable.setDynamic(true);
        this.draggables.add(draggable);
        return draggable;
    }

    /**
     * Shows a prediction line on any draggable of a polyline when it is being dragged.
     * @param linkData link data
     * @param node XSD node of the latter coordinate, can be {@code null} for last intermediary node
     * @param draggable draggable for which to show prediction line
     * @param intermediate whether this concerns a draggable of an intermediary node
     */
    private void addPolylinePrediction(final MapLinkData linkData, final XsdTreeNode node, final DraggableAnnotation draggable,
            final boolean intermediate)
    {
        Function<Point2d, PolyLine2d> predictionFunction = (p) ->
        {
            NavigableMap<XsdTreeNode, Point2d> ps = linkData.getPolylineCoordinates();
            Point2d prior, posterior;
            if (ps.isEmpty() || ps.size() == 1 && ps.values().contains(null))
            {
                prior = linkData.getCenterLine().getFirst();
                posterior = linkData.getCenterLine().getLast();
            }
            else
            {
                Entry<XsdTreeNode, Point2d> priorEntry = node == null ? ps.lastEntry() : ps.lowerEntry(node);
                prior = priorEntry == null ? linkData.getCenterLine().getFirst() : priorEntry.getValue();
                Entry<XsdTreeNode, Point2d> posteriorEntry =
                        node == null ? null : (intermediate ? ps.ceilingEntry(node) : ps.higherEntry(node));
                posterior = posteriorEntry == null ? linkData.getCenterLine().getLast() : posteriorEntry.getValue();
            }
            PolyLine2d line = new PolyLine2d(prior, p, posterior);
            return OtsShape.transformLine(line, p);
        };
        HelperAnnotation predictionLine =
                new HelperAnnotation(new Helper(linkData.getLocation(), predictionFunction), MapInputListener.this.editorMap,
                        Scaling.SCALE).setPredicate(() -> this.dragging && draggable.equals(this.selectedDraggable));
        draggable.addHelperAnnotation(predictionLine, UpdateMode.DYNAMIC);
    }

    /**
     * Add lane based object draggables.
     * @param laneBasedObjectData lane-based object data
     */
    private void addLaneBasedObjectDraggables(final MapLaneBasedObjectData laneBasedObjectData)
    {
        // position draggable
        UnaryOperator<Point2d> snapper = (p) ->
        {
            PolyLine2d centerline = laneBasedObjectData.getLaneCenterLine().get();
            Point2d pointOnLine = centerline.closestPointOnPolyLine(p);
            double f = centerline.projectOrthogonalFractionalExtended(pointOnLine);
            double x = f * centerline.getLength();
            if (this.snap)
            {
                x = Math.round(x / this.snapDistance.si) * this.snapDistance.si;
            }
            return centerline.getLocation(x);
        };
        Function<Point2d, Length> valueFunction = (p) ->
        {
            PolyLine2d centerline = laneBasedObjectData.getLaneCenterLine().get();
            Point2d pointOnLine = centerline.closestPointOnPolyLine(p);
            return Length.ofSI(centerline.projectOrthogonalFractionalExtended(pointOnLine) * centerline.getLength());
        };
        Consumer<Length> positionWriter = (p) ->
        {
            String value = this.snap ? GRID_FORMAT.format(p.si) + "m" : p.si + "m";
            MapInputListener.this.editor.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, laneBasedObjectData.getNode(),
                    "Position");
            laneBasedObjectData.getNode().setAttributeValue("Position", value);
            MapInputListener.this.editor.repaint();
        };
        DraggableAnnotation positionDraggable = new DraggableAnnotation(
                new Draggable<>(laneBasedObjectData.getLocation(), SQUARE, snapper, valueFunction, positionWriter)
                        .setDefaultValue(null).setShow(new Show(laneBasedObjectData.getNode(), "Position")),
                MapInputListener.this.editorMap);
        this.draggables.add(positionDraggable);

        // lane center line as snapping helper line
        Optional<PolyLine2d> centerLine = laneBasedObjectData.getLaneCenterLine();
        if (centerLine.isPresent())
        {
            Function<Point2d, PolyLine2d> lineFunction = (p) -> OtsShape.transformLine(centerLine.get(), p);
            positionDraggable
                    .addHelperAnnotation(new HelperAnnotation(new Helper(laneBasedObjectData.getLocation(), lineFunction),
                            MapInputListener.this.editorMap, Scaling.SCALE), UpdateMode.STATIC);
        }
    }

    /**
     * Deselect current selection, if any.
     */
    private void deselect()
    {
        this.selectedDraggable = null;
        for (Renderable2d<?> selectionRenderable : this.selectionIndicators)
        {
            this.editorMap.removeAnimation(selectionRenderable);
        }
        this.selectionIndicators.clear();
        removeDraggables();
        this.editorMap.update();
    }

    /**
     * Removes draggables and all linked annotations.
     */
    private void removeDraggables()
    {
        for (Renderable2d<?> draggable : this.draggables)
        {
            this.editorMap.removeAnimation(draggable);
        }
        this.draggables.clear();
    }

    /**
     * General purpose renderable removal. This method should be called by outside objects that destroy renderables for reasons
     * outside of this input listener.
     * @param renderable renderable
     */
    public void removeRenderable(final Renderable2d<?> renderable)
    {
        this.draggables.remove(renderable);
        this.selectionIndicators.remove(renderable);
        if (renderable != null && renderable.equals(this.selectedDraggable))
        {
            this.selectedDraggable = null;
        }
    }

}

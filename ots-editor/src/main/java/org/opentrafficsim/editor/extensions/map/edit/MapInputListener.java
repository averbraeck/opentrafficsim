package org.opentrafficsim.editor.extensions.map.edit;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.curve.Flattener2d;
import org.djutils.draw.curve.Flattener2d.NumSegments;
import org.djutils.draw.curve.OffsetCurve2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.opentrafficsim.animation.data.util.IconUtil;
import org.opentrafficsim.base.geometry.OtsGeometryUtil;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.MapData;
import org.opentrafficsim.editor.extensions.map.MapLaneBasedObjectData;
import org.opentrafficsim.editor.extensions.map.MapLinkData;
import org.opentrafficsim.editor.extensions.map.MapNodeData;
import org.opentrafficsim.editor.extensions.map.MapVisualizationPanel;
import org.opentrafficsim.editor.extensions.map.edit.DraggableAnnotation.Draggable;
import org.opentrafficsim.editor.extensions.map.edit.DraggableAnnotation.UpdateMode;
import org.opentrafficsim.editor.extensions.map.edit.HelperAnnotation.Helper;
import org.opentrafficsim.editor.extensions.map.edit.HelperAnnotation.Scaling;

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

    /** Minimum distance between points, for example on polyline or where a link can be split. */
    static final double DIST_MIN_M = 0.05;

    /** Format for grid sizes. */
    private static final DecimalFormat GRID_FORMAT = new DecimalFormat("0.0#", DecimalFormatSymbols.getInstance(Locale.US));

    /** Format for snapped angles. */
    private static final DecimalFormat ANGLE_FORMAT = new DecimalFormat("0.0 deg", DecimalFormatSymbols.getInstance(Locale.US));

    /** Offset from object location for any draggable that indicates some angle for the point. */
    private static final double ANGLE_POINT_OFFSET_PX = 30.0;

    /** Size of small node annotations. */
    private static final double ANNOTATION_PX = 3.5;

    /** Circle annotation. */
    static final PolyLine2d CIRCLE = circle(ANNOTATION_PX, 32);

    /** Square annotation. */
    static final PolyLine2d SQUARE =
            new PolyLine2d(new double[] {-ANNOTATION_PX, ANNOTATION_PX, ANNOTATION_PX, -ANNOTATION_PX, -ANNOTATION_PX},
                    new double[] {-ANNOTATION_PX, -ANNOTATION_PX, ANNOTATION_PX, ANNOTATION_PX, -ANNOTATION_PX});

    /** Editor. */
    private final OtsEditor editor;

    /** Editor map. */
    private final EditorMap editorMap;

    /** Current tree root observed for removals. */
    private XsdTreeNodeRoot treeRoot;

    /** Number of active annotation/gesture references per tree node. */
    private final Map<XsdTreeNode, Integer> activationListenerCounts = new LinkedHashMap<>();

    /** Mouse click screen coordinates. */
    private Point2D mousePressed = null;

    /** Whether currently dragging. */
    private boolean dragging = false;

    /** Selection visualization. */
    private Set<SelectionAnnotation> selectionIndicators = new LinkedHashSet<>();

    /** Currently selected draggable. */
    private DraggableAnnotation selectedDraggable;

    /** Start node for a link or connector being added. */
    private MapNodeData addingStart;

    /** Highlight of the start node while adding a link or connector. */
    private HighlightAnnotation addingStartHighlight;

    /** Current end-node candidate and its highlight. */
    private MapNodeData addingEnd;

    /** Highlight of the current end-node candidate. */
    private HighlightAnnotation addingEndHighlight;

    /** Helper line shown while adding a link or connector. */
    private HelperAnnotation addingHelper;

    /** Helper managing Ctrl-drag box selection state and behavior. */
    private final MapBoxSelectionHandler boxSelectionHandler;

    /** Helper managing polyline draggable creation/edit behavior. */
    private final MapPolylineEditHelper polylineEditHelper;

    /** Whether the click event following an add drag should be ignored. */
    private boolean ignoreClickAfterAddDrag;

    /** Whether the mouse release following Esc should be ignored. */
    private boolean ignoreMouseReleaseAfterEscape;

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

    /** Last tree highlight when tab was released. */
    private XsdTreeNode highlightOnRelease;

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
        this.boxSelectionHandler =
                new MapBoxSelectionHandler(this.editorMap, this.editor, () -> this.mousePressed, () -> this.addingStart,
                        () -> this.selectedDraggable, this::isSelected, (mapData) -> select(mapData, true, false, false));
        this.polylineEditHelper = new MapPolylineEditHelper(this.editorMap, this.editor, this::snapPointToGrid,
                (draggable) -> this.dragging && draggable.equals(this.selectedDraggable), this::setSelectedDraggable,
                this.draggables::add, this::pointToCoordinateString);
        this.snapSetting = OtsEditor.PROPERTIES_STORE.getOptionalBoolean(SNAP_KEY).orElse(false);
        this.snapDistance = Length.valueOf(OtsEditor.PROPERTIES_STORE.getPropertyOrDefault(SNAP_DISTANCE_KEY, "10 cm"));
        this.snapAngle = Angle.valueOf(OtsEditor.PROPERTIES_STORE.getPropertyOrDefault(SNAP_ANGLE_KEY, "3 deg"));
        this.editor.addListener(this, OtsEditor.NEW_FILE);
        this.editor.addListener((e) ->
        {
            if (!this.editor.isFocusedTab(EditorMap.MAP_TAB))
            {
                return;
            }
            Object[] payload = (Object[]) e.getContent();
            XsdTreeNode node = (XsdTreeNode) payload[0];
            MouseEvent me = (MouseEvent) payload[1];
            MapData data = editorMap.getData(node).orElse(null);
            boolean pan = (me.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
            if (data != null && data.isValid() && (pan || this.selectionIndicators.size() < 2))
            {
                if (pan)
                {
                    panToData(data);
                }
                select(data, false, false, false);
            }
        }, OtsEditor.MOUSE_EVENT);
        this.editor.addListener((e) ->
        {
            if (!this.editor.isFocusedTab(EditorMap.MAP_TAB))
            {
                return;
            }
            XsdTreeNode node = (XsdTreeNode) e.getContent();
            MapData data = editorMap.getData(node).orElse(null);
            if (data != null && data.isValid() && this.selectionIndicators.size() < 2)
            {
                select(data, false, false, false);
            }
        }, OtsEditor.SELECTION_CHANGED);
        // OtsEditor invokes release actions before any other tab change handling, regardless of tab construction order.
        editor.addTabSelectAction(EditorMap.MAP_TAB, () ->
        {
            if (this.highlightOnRelease != null)
            {
                this.editor.setHighlight(this.highlightOnRelease);
            }
        });
        this.editor.addTabReleaseAction(EditorMap.MAP_TAB, () ->
        {
            this.highlightOnRelease = this.editor.getHighlight();
            this.editor.setHighlight(null);
        });
    }

    /**
     * Sets the tree table highlight, only if the map tab is focused.
     * @param node node to highlight, may be {@code null}.
     */
    private void setHighlight(final XsdTreeNode node)
    {
        if (this.editor.isFocusedTab(EditorMap.MAP_TAB))
        {
            this.editor.setHighlight(node);
        }
    }

    /**
     * Shows a node in the tree table, only if the map tab is focused.
     * @param node node to show.
     * @param attribute attribute to show, may be {@code null}.
     */
    private void show(final XsdTreeNode node, final String attribute)
    {
        if (this.editor.isFocusedTab(EditorMap.MAP_TAB))
        {
            this.editor.show(node, attribute);
        }
    }

    /**
     * Selects a node in the tree table, only if the map tab is focused.
     * @param node node to select.
     * @param attribute attribute to select, may be {@code null}.
     */
    private void select(final XsdTreeNode node, final String attribute)
    {
        if (this.editor.isFocusedTab(EditorMap.MAP_TAB))
        {
            this.editor.select(node, attribute);
        }
    }

    /**
     * Clears the tree table selection, only if the map tab is focused.
     */
    private void clearSelection()
    {
        if (this.editor.isFocusedTab(EditorMap.MAP_TAB))
        {
            this.editor.clearSelection();
        }
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
            removeAddingHelper();
            this.boxSelectionHandler.clear();
            this.addingStart = null;
            this.dragging = false;
            this.ignoreClickAfterAddDrag = false;
            this.ignoreMouseReleaseAfterEscape = false;
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
                        this.selectedDraggable.setHighlightIfDeletable(false);
                    }
                    this.selectedDraggable =
                            this.draggables.stream().filter((d) -> d.getSource().equals(data)).findFirst().get();
                    this.selectedDraggable.setHighlightIfDeletable(true);
                    this.polylineEditHelper.setSelectedPolyLineCoordinateNode(
                            data.getNode() != null && data.getNode().getPathString().equals(XsdPaths.POLYLINE_COORDINATE)
                                    ? data.getNode() : null);
                    return;
                }
            }
            if (this.selectedDraggable != null)
            {
                this.selectedDraggable.setHighlightIfDeletable(false);
            }
            this.polylineEditHelper.setSelectedPolyLineCoordinateNode(null);
            this.selectedDraggable = null;
            if (isAddMode(e))
            {
                this.addingStart = getNodeAt(e.getPoint(), null);
                if (this.addingStart != null)
                {
                    addActivationListener(this.addingStart.getNode());
                }
            }
        }
    }

    @Override
    public void mouseDragged(final MouseEvent e)
    {
        this.dragging = true;
        if (this.addingStart != null && !isAddMode(e))
        {
            removeAddingHelper();
            this.addingStart = null;
            this.ignoreMouseReleaseAfterEscape = true;
            this.ignoreClickAfterAddDrag = true;
        }
        if (this.selectedDraggable != null)
        {
            this.boxSelectionHandler.clear();
            this.snap = this.snapSetting ^ e.isControlDown();
            Point2d world = this.editorMap.getPanel().getRenderableScale().getWorldCoordinates(e.getPoint(),
                    this.editorMap.getPanel().getExtent(), this.editorMap.getPanel().getSize());
            this.selectedDraggable.mouseDragged(world);
            this.editorMap.update();
            e.consume();
        }
        else if (this.addingStart != null)
        {
            this.boxSelectionHandler.clear();
            updateAddingHelper(e.getPoint());
            e.consume();
        }
        else if (this.boxSelectionHandler.isGesture(e))
        {
            this.boxSelectionHandler.update(e.getPoint());
            e.consume();
        }
        else
        {
            this.boxSelectionHandler.clear();
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e)
    {
        if (this.ignoreMouseReleaseAfterEscape)
        {
            this.ignoreMouseReleaseAfterEscape = false;
            this.dragging = false;
            this.mousePressed = null;
            return;
        }
        boolean addDrag = this.addingStart != null && this.dragging && SwingUtilities.isLeftMouseButton(e) && isAddMode(e);
        if (this.dragging && this.selectedDraggable != null)
        {
            Point2d world = this.editorMap.getPanel().getRenderableScale().getWorldCoordinates(e.getPoint(),
                    this.editorMap.getPanel().getExtent(), this.editorMap.getPanel().getSize());
            Draggable<?> draggable = this.selectedDraggable.getSource();
            this.selectedDraggable.mouseReleased(world);
            XsdTreeNode draggableNode = draggable.getNode();
            String draggableAttribute = draggable.getAttribute();
            if (draggableNode != null && "Coordinate".equals(draggableAttribute)
                    && XsdPaths.NODE.equals(draggableNode.getPathString()))
            {
                MapContextActions.autoFixNodeRepositioned(this.editorMap, draggableNode);
            }
            else if (draggableNode != null && "Direction".equals(draggableAttribute)
                    && XsdPaths.NODE.equals(draggableNode.getPathString()))
            {
                Set<XsdTreeNode> visited = new HashSet<>();
                visited.add(draggableNode);
                MapContextActions.autoFixNodeDirectionChanged(this.editorMap, draggableNode, visited);
            }
        }
        else if (this.addingStart != null)
        {
            if (addDrag)
            {
                MapNodeData end = getNodeAt(e.getPoint(), this.addingStart);
                if (end != null && isAddMode(e))
                {
                    addConnection(this.addingStart, end);
                }
                this.ignoreClickAfterAddDrag = true;
            }
            else if (this.dragging)
            {
                this.ignoreClickAfterAddDrag = true;
            }
            removeAddingHelper();
            this.addingStart = null;
        }
        else if (this.boxSelectionHandler.isActive())
        {
            this.boxSelectionHandler.apply(e);
        }
        else if (SwingUtilities.isLeftMouseButton(e))
        {
            this.editorMap.getPanel().pan(this.mousePressed, e.getPoint());
            this.editorMap.update();
        }
        this.dragging = false;
        this.mousePressed = null;
    }

    @Override
    public void mouseClicked(final MouseEvent e)
    {

        this.editorMap.getPanel().requestFocus();

        if (this.ignoreClickAfterAddDrag)
        {
            this.ignoreClickAfterAddDrag = false;
            return;
        }

        if (this.selectedDraggable != null && SwingUtilities.isLeftMouseButton(e))
        {
            if (e.getClickCount() > 1)
            {
                this.selectedDraggable.getSource().writeDefaultValue();
            }
            else if (e.isShiftDown())
            {
                this.selectedDraggable.showValue(this.editor);
            }
            else
            {
                this.selectedDraggable.selectValue(this.editor);
            }
            return;
        }

        List<MapData> mapDatas = this.editorMap.getPanel().getSelectedObjects(e.getPoint()).stream()
                .filter(MapData.class::isInstance).map(MapData.class::cast).toList();
        if (SwingUtilities.isLeftMouseButton(e))
        {
            if (this.editorMap.getPanel().consumeContextMenuJustClosed() && this.selectionIndicators.size() > 1)
            {
                // click to remove context menu
                e.consume();
            }
            else if (mapDatas.size() == 1)
            {
                selectSingleObject(mapDatas.get(0), e);
            }
            else if (!mapDatas.isEmpty())
            {
                Optional<MapData> highest = mapDatas.stream().max(Comparator.comparingDouble(MapData::getZ));
                if (highest.isPresent())
                {
                    selectSingleObject(highest.get(), e);
                }
            }
            else if (e.getClickCount() == 1)
            {
                if (!e.isControlDown() && !this.selectionIndicators.isEmpty())
                {
                    // normal click but there is a selection -> deselect even in add mode
                    setHighlight(null);
                    deselect();
                }
                else if (isAddMode(e) && (!e.isControlDown() || this.selectionIndicators.size() < 2))
                {
                    // skip ctrl click if there is a multi-selection (losing your selection is annoying)
                    this.snap = !this.editorMap.isAddMode() || this.snapSetting;
                    Point2d world = this.editorMap.getPanel().getRenderableScale().getWorldCoordinates(e.getPoint(),
                            this.editorMap.getPanel().getExtent(), this.editorMap.getPanel().getSize());
                    addPoint(snapPointToGrid(world), this.editorMap.isCentroidMode() ^ e.isShiftDown());
                }
            }
        }
        else if (SwingUtilities.isRightMouseButton(e))
        {
            if (!mapDatas.isEmpty())
            {
                if (mapDatas.size() == 1)
                {
                    if (!e.isControlDown())
                    {
                        // no-op when there is a multi-selection and we are right-clicking on some other object
                        if (this.selectionIndicators.size() > 1 && !isSelected(mapDatas.get(0)))
                        {
                            return;
                        }
                        XsdTreeNode highlight = this.editor.getHighlight();
                        if (highlight != null && !highlight.equals(mapDatas.get(0).getNode()))
                        {
                            // highlighted some other node; do not keep this highlight
                            setHighlight(null);
                        }
                        // TreeTableModelAdapter.delayedFireTableDataChanged() uses invokeLater, need to be after that
                        SwingUtilities.invokeLater(() ->
                        {
                            if (this.selectionIndicators.size() <= 1)
                            {
                                // select this one if there is no multi-selection, so a relevant operations menu can be shown
                                select(mapDatas.get(0).getNode(), null);
                                select(mapDatas.get(0), false, true, false);
                            }
                            showContextMenu(mapDatas, e);
                        });
                    }
                    else
                    {
                        // treat ctrl right-click on single object as ctrl left-click (no point in showSelectLocatableMenu)
                        selectSingleObject(mapDatas.get(0), e);
                    }
                }
                else
                {
                    showContextMenu(mapDatas, e);
                }
            }
            else
            {
                showSettingsMenu(e);
            }
        }
        else if (!e.isControlDown() || !isAddMode(e))
        {
            setHighlight(null);
            deselect();
        }
    }

    /**
     * Select an object that was the only object clicked.
     * @param data data of clicked object
     * @param e mouse event
     */
    private void selectSingleObject(final MapData data, final MouseEvent e)
    {
        if (!e.isControlDown())
        {
            setHighlight(!e.isShiftDown() ? data.getNode() : null);
            // TreeTableModelAdapter.delayedFireTableDataChanged() uses invokeLater, need to be after that
            SwingUtilities.invokeLater(() -> select(data, false, true, e.isShiftDown()));
        }
        else
        {
            setHighlight(null);
            select(data, true, false, e.isShiftDown());
        }
    }

    /**
     * Show context menu based on map data's under the mouse. If one of the map data's is already selected and ctrl is not held
     * down, then operations based on the selection are shown, if any.
     * @param mapDatas map data's
     * @param e mouse event
     */
    private void showContextMenu(final List<MapData> mapDatas, final MouseEvent e)
    {
        boolean atLeastOneUnderMouseSelected = mapDatas.stream().anyMatch(this::isSelected);
        boolean adding = e.isControlDown();
        boolean contextMenuHandled = atLeastOneUnderMouseSelected && !adding // only allow context actions on existing selection
                && MapContextActions.showContextMenu(
                        this.selectionIndicators.stream().map(a -> a.getSource().mapData()).toList(), this.editorMap,
                        this.editorMap.getPanel(), e);
        if (contextMenuHandled)
        {
            return;
        }
        boolean multipleUnderMouse = mapDatas.size() > 1;
        boolean multiSelection = this.selectionIndicators.size() > 1;
        if (multipleUnderMouse && (!multiSelection || adding))
        {
            showSelectLocatableMenu(mapDatas, e);
        }
    }

    /**
     * Returns the effective add mode for a mouse event, with Ctrl temporarily toggling the toolbar state.
     * @param event mouse event
     * @return effective add mode
     */
    private boolean isAddMode(final MouseEvent event)
    {
        return this.editorMap.isAddMode() ^ event.isControlDown();
    }

    /**
     * Returns the compatible map node under a screen location.
     * @param point screen location
     * @param exclude node to ignore
     * @return compatible map node, or {@code null}
     */
    private MapNodeData getNodeAt(final Point2D point, final MapNodeData exclude)
    {
        return this.editorMap.getPanel().getSelectedObjects(point).stream().filter(MapNodeData.class::isInstance)
                .map(MapNodeData.class::cast).filter(MapNodeData::isValid).filter((node) -> !node.equals(exclude))
                .filter((node) -> this.addingStart == null || !this.addingStart.isCentroid() || !node.isCentroid())
                .filter((node) -> this.addingStart == null || !hasDirectedConnection(this.addingStart, node))
                .max(Comparator.comparingDouble(MapNodeData::getZ)).orElse(null);
    }

    /**
     * Returns whether a link or connector already joins the nodes in the requested direction.
     * @param start starting map node
     * @param end ending map node
     * @return whether the directed connection already exists
     */
    private boolean hasDirectedConnection(final MapNodeData start, final MapNodeData end)
    {
        XsdTreeNode network = this.editorMap.getNetworkNode();
        if (network == null)
        {
            return false;
        }
        for (XsdTreeNode element : network.getChildren())
        {
            if (!element.isActive())
            {
                continue;
            }
            XsdTreeNode from;
            XsdTreeNode to;
            if (element.getPathString().equals(XsdPaths.LINK))
            {
                from = element.getCoupledNodeAttribute("NodeStart").orElse(null);
                to = element.getCoupledNodeAttribute("NodeEnd").orElse(null);
            }
            else if (element.getPathString().equals(XsdPaths.CONNECTOR))
            {
                XsdTreeNode centroid = element.getCoupledNodeAttribute("Centroid").orElse(null);
                XsdTreeNode node = element.getCoupledNodeAttribute("Node").orElse(null);
                String outbound = element.getAttributeValueOrDefault("Outbound");
                if (centroid == null || node == null
                        || !"true".equalsIgnoreCase(outbound) && !"false".equalsIgnoreCase(outbound))
                {
                    continue;
                }
                from = "true".equalsIgnoreCase(outbound) ? centroid : node;
                to = "true".equalsIgnoreCase(outbound) ? node : centroid;
            }
            else
            {
                continue;
            }
            if (start.getNode().equals(from) && end.getNode().equals(to))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Update the temporary line to the pointer, snapping it to a compatible end point when present.
     * @param point pointer location
     */
    private void updateAddingHelper(final Point2D point)
    {
        Point2d world = this.editorMap.getPanel().getRenderableScale().getWorldCoordinates(point,
                this.editorMap.getPanel().getExtent(), this.editorMap.getPanel().getSize());
        MapNodeData end = getNodeAt(point, this.addingStart);
        if (this.addingStartHighlight == null)
        {
            this.addingStartHighlight = new HighlightAnnotation(this.addingStart, this.editorMap);
            this.editorMap.addAnimation(this.addingStartHighlight);
        }
        updateAddingEndHighlight(end);
        Point2d location = end == null ? world : end.getLocation();
        if (this.addingHelper == null)
        {
            Point2d start = this.addingStart.getLocation();
            Function<Point2d, PolyLine2d> line = (p) ->
            {
                double x = start.x - p.x;
                double y = start.y - p.y;
                if (x == 0.0 && y == 0.0)
                {
                    x = 1e-6;
                    y = 1e-6;
                }
                return new PolyLine2d(new Point2d(x, y), new Point2d(0.0, 0.0));
            };
            this.addingHelper = new HelperAnnotation(new Helper(location, line), this.editorMap, Scaling.SCALE);
            this.editorMap.addAnimation(this.addingHelper);
        }
        else
        {
            this.addingHelper.getSource().setLocation(location);
        }
        this.editorMap.update();
    }

    /**
     * Highlight the current compatible endpoint candidate.
     * @param end candidate endpoint, or {@code null}
     */
    private void updateAddingEndHighlight(final MapNodeData end)
    {
        if (end == this.addingEnd)
        {
            return;
        }
        if (this.addingEndHighlight != null)
        {
            this.editorMap.removeAnimation(this.addingEndHighlight);
            this.addingEndHighlight = null;
        }
        if (this.addingEnd != null)
        {
            removeActivationListener(this.addingEnd.getNode());
        }
        this.addingEnd = end;
        if (end != null)
        {
            addActivationListener(end.getNode());
            this.addingEndHighlight = new HighlightAnnotation(end, this.editorMap);
            this.editorMap.addAnimation(this.addingEndHighlight);
        }
    }

    /**
     * Remove endpoint highlights shown during creation of a link or connector.
     */
    private void removeAddingHighlights()
    {
        updateAddingEndHighlight(null);
        if (this.addingStartHighlight != null)
        {
            this.editorMap.removeAnimation(this.addingStartHighlight);
            this.addingStartHighlight = null;
        }
        if (this.addingStart != null)
        {
            removeActivationListener(this.addingStart.getNode());
        }
    }

    /**
     * Remove the temporary line shown while creating a link or connector.
     */
    private void removeAddingHelper()
    {
        if (this.addingHelper != null)
        {
            this.editorMap.removeAnimation(this.addingHelper);
            this.addingHelper = null;
        }
        removeAddingHighlights();
        this.editorMap.update();
        this.editorMap.getPanel().repaint();
    }

    /**
     * Add a node or centroid at a snapped coordinate.
     * @param coordinate world coordinate
     * @param centroid whether to add a centroid
     */
    private void addPoint(final Point2d coordinate, final boolean centroid)
    {
        XsdTreeNode added = MapAdder.addPoint(this.editor, this.editorMap, pointToCoordinateString(coordinate), centroid, null);
        if (added != null)
        {
            select(added);
        }
    }

    /**
     * Add a link between two nodes or a connector between a node and centroid.
     * @param start starting map node
     * @param end ending map node
     */
    private void addConnection(final MapNodeData start, final MapNodeData end)
    {
        XsdTreeNode added = MapAdder.addConnection(this.editor, this.editorMap, start, end, this.editorMap.getDefaultShape(),
                this.editorMap.getDefaultRoadLayout(), this.editorMap.getDefaultLinkType());
        if (added != null)
        {
            select(added);
        }
    }

    /**
     * Show and select a newly created tree/map element.
     * @param node created element
     */
    private void select(final XsdTreeNode node)
    {
        setHighlight(node);
        this.editorMap.getData(node).ifPresentOrElse((data) -> select(data, false, true, false), () -> select(node, null));
    }

    /**
     * Pan towards data if not (partially) visible.
     * @param data data
     */
    private void panToData(final MapData data)
    {
        Bounds2d extent = this.editorMap.getPanel().getExtent();
        if (!data.getAbsoluteContour().intersects(new Polygon2d(extent.getPointList())))
        {
            Point2d mid = data.getLocation();
            double dx = 0.5 * extent.getDeltaX();
            double dy = 0.5 * extent.getDeltaY();
            Bounds2d newBounds = new Bounds2d(mid.x - dx, mid.x + dx, mid.y - dy, mid.y + dy);
            this.editorMap.getPanel().setExtent(newBounds);
        }
    }

    @Override
    public void keyPressed(final KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && cancelActiveMapEdit())
        {
            e.consume();
            return;
        }
        super.keyPressed(e);
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_DELETE:
                if (this.selectedDraggable != null && this.selectedDraggable.isDeletable())
                {
                    this.selectedDraggable.delete();
                }
                else if (!this.selectionIndicators.isEmpty())
                {
                    if (this.selectionIndicators.size() == 1)
                    {
                        MapData selectedData = this.selectionIndicators.iterator().next().getSource().mapData();
                        if (selectedData instanceof MapNodeData nodeData)
                        {
                            XsdTreeNode network = nodeData.getNode().getParent();
                            Optional<Boolean> ans = MapLinkEditActions.tryMergeNodeOnDelete(this.editor, nodeData.getNode());
                            if (ans.isEmpty())
                            {
                                return; // Explicit cancel
                            }
                            if (ans.get())
                            {
                                deselect();
                                if (network != null)
                                {
                                    show(network, null);
                                }
                                break; // exits switch
                            }
                        }
                    }
                    if (this.selectionIndicators.size() >= 5
                            && !this.editor.dialogs().confirmGeneral("Delete " + this.selectionIndicators.size() + " objects?"))
                    {
                        return;
                    }
                    List<XsdTreeNode> nodes =
                            this.selectionIndicators.stream().map((s) -> s.getSource().mapData().getNode()).toList();
                    boolean multiple = nodes.size() > 1;
                    XsdTreeNode network = nodes.get(0).getParent();
                    this.editor.getUndo().startAction(ActionType.REMOVE, network, null);
                    if (multiple)
                    {
                        this.editor.getUndo().setPostActionClearSelection();
                    }
                    deselect(); // remove selection annotations
                    nodes.forEach((n) -> n.remove());
                    if (multiple)
                    {
                        clearSelection();
                    }
                    else
                    {
                        show(network, null);
                    }
                }
                break;
            default:
        }
    }

    /**
     * Cancel an active draggable edit or Link/Connector creation gesture.
     * @return whether an active edit was cancelled
     */
    private boolean cancelActiveMapEdit()
    {
        boolean cancelAdding = this.addingStart != null;
        boolean cancelDraggable = this.selectedDraggable != null;
        boolean cancelBoxSelection = this.boxSelectionHandler.isActive();
        if (!cancelAdding && !cancelDraggable && !cancelBoxSelection)
        {
            return false;
        }

        boolean mouseGestureActive = this.dragging || this.mousePressed != null;
        if (cancelAdding)
        {
            removeAddingHelper();
            this.addingStart = null;
        }
        if (cancelBoxSelection)
        {
            this.boxSelectionHandler.clear();
        }
        this.selectedDraggable = null;
        this.dragging = false;
        this.mousePressed = null;
        if (mouseGestureActive)
        {
            this.ignoreMouseReleaseAfterEscape = true;
            this.ignoreClickAfterAddDrag = true;
        }

        MapData selectedData =
                this.selectionIndicators.size() == 1 ? this.selectionIndicators.iterator().next().getSource().mapData() : null;
        if (cancelDraggable && selectedData != null)
        {
            select(selectedData, false, false, false);
        }
        else if (cancelDraggable)
        {
            removeDraggables();
        }
        this.editorMap.update();
        this.editorMap.getPanel().repaint();
        return true;
    }

    /**
     * Returns whether the given map data is part of the current selection.
     * @param mapData map data
     * @return whether the given map data is part of the current selection
     */
    private boolean isSelected(final MapData mapData)
    {
        return this.selectionIndicators.stream().anyMatch((annotation) -> annotation.getSource().mapData().equals(mapData));
    }

    /**
     * Sets the currently selected draggable.
     * @param draggable selected draggable
     */
    private void setSelectedDraggable(final DraggableAnnotation draggable)
    {
        this.selectedDraggable = draggable;
    }

    /**
     * Show menu to select locatable.
     * @param mapDatas list of map datas under mouse event
     * @param e mouse event
     */
    private void showSelectLocatableMenu(final List<MapData> mapDatas, final MouseEvent e)
    {
        AtomicReference<HighlightAnnotation> highlight = new AtomicReference<>();
        Runnable remover = () ->
        {
            HighlightAnnotation high = highlight.get();
            if (high != null)
            {
                this.editorMap.removeAnimation(high);
                this.editorMap.update();
            }
        };
        JPopupMenu popup = new JPopupMenu();
        List<MapData> list = mapDatas;
        if (mapDatas.size() <= 1000)
        {
            // skip multi-object selection menu sorting if there are many objects in the network
            List<XsdTreeNode> children = mapDatas.get(0).getNode().getParent().getChildren();
            Comparator<MapData> comp = new Comparator<>()
            {
                @Override
                public int compare(final MapData o1, final MapData o2)
                {
                    return Integer.compare(children.indexOf(o1.getNode()), children.indexOf(o2.getNode()));
                }
            };
            list = mapDatas.stream().sorted(comp).toList();
        }
        Map<String, Icon> iconMap = new HashMap<>();
        int size = OtsEditor.ICON_SIZE;
        for (MapData mapData : list)
        {
            String path = mapData.getNode().getPathString();
            Icon icon = iconMap.computeIfAbsent(path, (p) ->
            {
                return switch (p)
                {
                    case XsdPaths.CENTROID -> IconUtil.of("Centroid24.png").imageSize(size, size).get();
                    case XsdPaths.CONNECTOR -> IconUtil.of("Connector24.png").imageSize(size, size).get();
                    case XsdPaths.NODE -> IconUtil.of("Node24.png").imageSize(size, size).get();
                    case XsdPaths.LINK -> IconUtil.of("Link24.png").imageSize(size, size).get();
                    case XsdPaths.TRAFFIC_LIGHT -> IconUtil.of("TrafficLight24.png").imageSize(size, size).get();
                    default -> null;
                };
            });
            JMenuItem item = new JMenuItem(mapData.getNode().toString(), icon);
            item.addActionListener((ev) ->
            {
                if (!e.isControlDown() && !e.isShiftDown())
                {
                    setHighlight(mapData.getNode());
                }
                select(mapData, e.isControlDown(), true, e.isShiftDown());
                this.editorMap.update();
                remover.run();
            });
            popup.add(item);
            item.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseEntered(final MouseEvent e)
                {
                    HighlightAnnotation high = new HighlightAnnotation(mapData, MapInputListener.this.editorMap);
                    highlight.set(high);
                    MapInputListener.this.editorMap.update();
                }

                @Override
                public void mouseExited(final MouseEvent e)
                {
                    remover.run();
                }
            });
        }
        this.editorMap.getPanel().showContextMenu(popup, e);
    }

    /**
     * Show menu with map settings (snapping).
     * @param event mouse event
     */
    private void showSettingsMenu(final MouseEvent event)
    {
        JPopupMenu popup = new JPopupMenu();
        IconUtil util = IconUtil.of("Check24.png").imageSize(OtsEditor.ICON_SIZE, OtsEditor.ICON_SIZE);
        Icon check = util.get();
        if (!this.snapSetting)
        {
            util = util.gray();
        }
        JMenuItem snapItem = new JMenuItem("Snap", util.get());
        snapItem.addActionListener((ev) ->
        {
            this.snapSetting = !this.snapSetting;
            OtsEditor.PROPERTIES_STORE.setBoolean(SNAP_KEY, this.snapSetting);
        });
        popup.add(snapItem);

        JMenu gridMenu = new JMenu("Snap distance");
        ButtonGroup gridGroup = new ButtonGroup();
        gridMenu.setIcon(IconUtil.of("Grid24.png").imageSize(OtsEditor.ICON_SIZE, OtsEditor.ICON_SIZE).get());
        for (String gridSizeStringValue : SNAP_DISTANCE_OPTIONS)
        {
            Length value = Length.valueOf(gridSizeStringValue);
            JMenuItem item = new JMenuItem(gridSizeStringValue);
            if (this.snapDistance.eq(value))
            {
                item.setIcon(check);
            }
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
        angleMenu.setIcon(IconUtil.of("Arc24.png").imageSize(OtsEditor.ICON_SIZE, OtsEditor.ICON_SIZE).get());
        for (String snapAngleStringValue : SNAP_ANGLE_OPTIONS)
        {
            Angle value = Angle.valueOf(snapAngleStringValue);
            JMenuItem item = new JMenuItem(snapAngleStringValue);
            if (this.snapAngle.eq(value))
            {
                item.setIcon(check);
            }
            item.addActionListener((ev) ->
            {
                this.snapAngle = value;
                OtsEditor.PROPERTIES_STORE.setProperty(SNAP_ANGLE_KEY, snapAngleStringValue);
            });
            angleGroup.add(item);
            angleMenu.add(item);
        }
        popup.add(angleMenu);

        this.editorMap.getPanel().showContextMenu(popup, event);
    }

    /**
     * Set selection in map to elements regarding given nodes.
     * @param nodes nodes
     */
    public void setSelection(final Collection<XsdTreeNode> nodes)
    {
        int i = 0;
        XsdTreeNode lastSelected = null;
        for (XsdTreeNode node : nodes)
        {
            Optional<MapData> data = this.editorMap.getData(node);
            if (data.isPresent() && data.get().isValid())
            {
                if (i == 0)
                {
                    deselect();
                }
                lastSelected = data.get().getNode();
                select(data.get(), i++ > 0, false, false);
            }
        }
        if (i == 1)
        {
            setHighlight(lastSelected);
        }
        else if (this.editor.getHighlight() != null && i > 1)
        {
            setHighlight(null);
        }
    }

    /**
     * Select map data object.
     * @param mapData map data
     * @param add whether to add to selection
     * @param selectInTree show the accompanying {@link XsdTreeNode} in the tree
     * @param showInTree show the accompanying {@link XsdTreeNode} in the tree
     */
    private void select(final MapData mapData, final boolean add, final boolean selectInTree, final boolean showInTree)
    {
        Optional<SelectionAnnotation> currentlySelected =
                this.selectionIndicators.stream().filter((r) -> r.getSource().mapData().equals(mapData)).findFirst();
        if (add && currentlySelected.isPresent())
        {
            // ctrl-click on selected item, remove it from selection
            removeDraggables();
            SelectionAnnotation annotation = currentlySelected.get();
            MapData selectedData = annotation.getSource().mapData();
            selectedData.removeListener(this, MapData.MAP_DATA_CHANGED);
            removeActivationListener(selectedData.getNode());
            this.selectionIndicators.remove(annotation);
            this.editorMap.removeAnimation(annotation);
            this.editorMap.update();
            return;
        }
        else if (add && this.selectionIndicators.size() == 1)
        {
            // multi-select, show no draggables on any
            removeDraggables();
        }
        else if (!add)
        {
            // normal click, deselect any current selection before selecting mapData
            deselect();
        }

        if (!add && this.selectionIndicators.isEmpty() && mapData.isValid())
        {
            // in java 21 this can be a pretty switch-case pattern
            if (mapData instanceof MapNodeData nodeData)
            {
                addNodeDraggables(nodeData);
                mapData.addListener(this, MapData.MAP_DATA_CHANGED, ReferenceType.WEAK);
            }
            else if (mapData instanceof MapLinkData linkData)
            {
                boolean needToListen = false;
                if (linkData.isBezier())
                {
                    MapBezierDraggablesHelper.addBezierDraggables(this.editorMap, this.editor, linkData,
                            (d) -> this.dragging && d.equals(this.selectedDraggable), this.draggables::add);
                    needToListen = true;
                }
                else if (linkData.isPolyLine())
                {
                    this.polylineEditHelper.addPolylineDraggables(linkData);
                    needToListen = true;
                }
                if (!linkData.isConnector())
                {
                    addLinkOffsetDraggables(linkData);
                    needToListen = true;
                }
                if (needToListen)
                {
                    mapData.addListener(this, MapData.MAP_DATA_CHANGED, ReferenceType.WEAK);
                }
            }
            else if (mapData instanceof MapLaneBasedObjectData laneBasedData)
            {
                addLaneBasedObjectDraggables(laneBasedData);
                mapData.addListener(this, MapData.MAP_DATA_CHANGED, ReferenceType.WEAK);
            }
        }
        this.selectionIndicators.add(new SelectionAnnotation(mapData, MapInputListener.this.editorMap));
        addActivationListener(mapData.getNode());
        this.editorMap.update();
        if (!add)
        {
            if (showInTree)
            {
                show(mapData.getNode(), null);
            }
            else if (selectInTree)
            {
                select(mapData.getNode(), null);
            }
        }
    }

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
            if (this.treeRoot != null)
            {
                this.treeRoot.removeListener(this, XsdTreeNodeRoot.NODE_REMOVED);
            }
            removeAddingHelper();
            this.addingStart = null;
            this.ignoreClickAfterAddDrag = false;
            deselect();
            this.treeRoot = (XsdTreeNodeRoot) event.getContent();
            this.treeRoot.addListener(this, XsdTreeNodeRoot.NODE_REMOVED, ReferenceType.WEAK);
            return;
        }
        if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
        {
            clearAnnotationsForNode((XsdTreeNode) ((Object[]) event.getContent())[0]);
            return;
        }
        if (event.getType().equals(XsdTreeNode.ACTIVATION_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            if (!(boolean) content[1])
            {
                clearAnnotationsForNode((XsdTreeNode) content[0]);
            }
            return;
        }
        // MapData.MAP_DATA_CHANGED
        if (this.selectionIndicators.size() == 1)
        {
            select(this.selectionIndicators.stream().findFirst().get().getSource().mapData(), false, false, false);
        }
    }

    /**
     * Add draggables to edit a node.
     * @param nodeData node data
     */
    private void addNodeDraggables(final MapNodeData nodeData)
    {
        if (!nodeData.isCentroid())
        {
            addNodeRotationDraggable(nodeData);
        }

        // position draggable
        Consumer<Point2d> positionWriter = (p) ->
        {
            String value = pointToCoordinateString(p);
            MapInputListener.this.editor.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, nodeData.getNode(), "Coordinate");
            nodeData.getNode().setAttributeValue("Coordinate", value);
        };
        DraggableAnnotation positionDraggable = new DraggableAnnotation(
                new Draggable<>(nodeData.getLocation(), SQUARE, this::snapPointToGrid, (p) -> p, positionWriter)
                        .setNodeAttribute(nodeData.getNode(), "Coordinate"),
                MapInputListener.this.editorMap);
        this.draggables.add(positionDraggable);

        // snap circle
        Polygon2d circle = circle(ANGLE_POINT_OFFSET_PX, 32);
        positionDraggable.addHelperAnnotation(new HelperAnnotation(new Helper(nodeData.getLocation(), (p) -> circle),
                MapInputListener.this.editorMap, Scaling.NO_SCALE), UpdateMode.STATIC);
    }

    /**
     * Add draggables to edit the start and end offset of a link. The link is checked not to be a connector.
     * @param linkData link data
     */
    private void addLinkOffsetDraggables(final MapLinkData linkData)
    {
        OffsetCurve2d designLine = linkData.getDesignLine();
        if (designLine == null || linkData.getStartPoint() == null || linkData.getEndPoint() == null)
        {
            return;
        }
        DraggableAnnotation draggable1 = addLinkOffsetDraggable(linkData, true, designLine.getStartPoint());
        DraggableAnnotation draggable2 = addLinkOffsetDraggable(linkData, false, designLine.getEndPoint());

        // shape prediction line
        Flattener2d flattener = new NumSegments(32);
        Function<Point2d, PolyLine2d> predictionFunction = (p) ->
        {
            OffsetCurve2d virtualLine = linkData.getVirtualDesignLine(
                    draggable1.equals(this.selectedDraggable) ? (Length) draggable1.getSource().getValue()
                            : linkData.getStartOffset(),
                    draggable2.equals(this.selectedDraggable) ? (Length) draggable2.getSource().getValue()
                            : linkData.getEndOffset());
            if (virtualLine == null)
            {
                return new PolyLine2d(new Point2d(0.0, 0.0), new Point2d(1e-6, 0.0));
            }
            return OtsShape.transformLine(virtualLine.toPolyLine(flattener), p);
        };
        HelperAnnotation predictionLine =
                new HelperAnnotation(new Helper(linkData.getLocation(), predictionFunction), this.editorMap, Scaling.SCALE)
                        .setPredicate(() -> this.dragging
                                && (draggable1.equals(this.selectedDraggable) || draggable2.equals(this.selectedDraggable)));
        draggable1.addHelperAnnotation(predictionLine, UpdateMode.DYNAMIC);
        draggable2.addHelperAnnotation(predictionLine, UpdateMode.DYNAMIC);
    }

    /**
     * Add a draggable to edit one of the link offsets, including a perpendicular snapping line and a live preview of the
     * resulting design line.
     * @param linkData link data
     * @param start whether this draggable edits the start offset
     * @param initialLocation current design line end-point corresponding to the offset
     * @return draggable annotation
     */
    private DraggableAnnotation addLinkOffsetDraggable(final MapLinkData linkData, final boolean start,
            final Point2d initialLocation)
    {
        String attribute = start ? "OffsetStart" : "OffsetEnd";
        Point2d origin = start ? linkData.getStartPoint() : linkData.getEndPoint();
        Direction direction = start ? linkData.getStartDirection() : linkData.getEndDirection();
        DirectedPoint2d nodePoint = new DirectedPoint2d(origin, direction == null ? 0.0 : direction.si);
        UnaryOperator<Point2d> snapper = (p) ->
        {
            double offset = lateralOffset(p, nodePoint);
            if (this.snap)
            {
                offset = Math.round(offset / this.snapDistance.si) * this.snapDistance.si;
            }
            return OtsGeometryUtil.offsetPoint(nodePoint, offset);
        };
        Function<Point2d, Length> valueFunction = (p) -> Length.ofSI(lateralOffset(p, nodePoint));
        Consumer<Length> valueWriter = (v) ->
        {
            String value = v == null ? null : this.snap ? GRID_FORMAT.format(v.si) + "m" : v.si + "m";
            this.editor.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, linkData.getNode(), attribute);
            linkData.getNode().setAttributeValue(attribute, value);
        };
        Draggable<Length> source = new Draggable<>(initialLocation, CIRCLE, snapper, valueFunction, valueWriter)
                .setDefaultValue(null).setNodeAttribute(linkData.getNode(), attribute);
        DraggableAnnotation draggable = new DraggableAnnotation(source, this.editorMap);
        this.draggables.add(draggable);
        return draggable;
    }

    /**
     * Returns the signed lateral offset of a point from a directed node point, left of the direction being positive.
     * @param point point
     * @param nodePoint directed node point
     * @return signed lateral offset
     */
    private static double lateralOffset(final Point2d point, final DirectedPoint2d nodePoint)
    {
        return -(point.x - nodePoint.x) * Math.sin(nodePoint.dirZ) + (point.y - nodePoint.y) * Math.cos(nodePoint.dirZ);
    }

    /**
     * Add the Direction draggable and its helper line to a Node.
     * @param nodeData node data
     */
    private void addNodeRotationDraggable(final MapNodeData nodeData)
    {
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
            this.editor.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, nodeData.getNode(), "Direction");
            nodeData.getNode().setAttributeValue("Direction", value);
        };
        DraggableAnnotation rotationDraggable =
                new DraggableAnnotation(
                        new Draggable<>(getRotationPoint(nodeData, nodeData.getDirZ()), CIRCLE, rotationSnapper,
                                rotationFunction, rotationWriter).setNodeAttribute(nodeData.getNode(), "Direction"),
                        this.editorMap);
        this.draggables.add(rotationDraggable);

        Function<Point2d, PolyLine2d> lineFunction =
                (p) -> new PolyLine2d(new Point2d(nodeData.getLocation().x - p.x, nodeData.getLocation().y - p.y),
                        new Point2d(0.0, 0.0));
        HelperAnnotation rotationLine = new HelperAnnotation(
                new Helper(getRotationPoint(nodeData, nodeData.getDirZ()), lineFunction), this.editorMap, Scaling.SCALE);
        rotationDraggable.addHelperAnnotation(rotationLine, UpdateMode.DYNAMIC);

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
        if (!this.snap)
        {
            return point;
        }
        double x = Math.round(point.x / this.snapDistance.si) * this.snapDistance.si;
        double y = Math.round(point.y / this.snapDistance.si) * this.snapDistance.si;
        return new Point2d(x, y);
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
        };
        DraggableAnnotation positionDraggable = new DraggableAnnotation(
                new Draggable<>(laneBasedObjectData.getLocation(), SQUARE, snapper, valueFunction, positionWriter)
                        .setDefaultValue(null).setNodeAttribute(laneBasedObjectData.getNode(), "Position"),
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
        for (SelectionAnnotation selectionRenderable : this.selectionIndicators)
        {
            MapData mapData = selectionRenderable.getSource().mapData();
            mapData.removeListener(this, MapData.MAP_DATA_CHANGED);
            removeActivationListener(mapData.getNode());
            this.editorMap.removeAnimation(selectionRenderable);
        }
        this.selectionIndicators.clear();
        removeDraggables();
        this.editorMap.update();
    }

    /**
     * Remove annotations that refer to a tree node that is removed or deactivated.
     * @param node removed or deactivated node
     */
    private void clearAnnotationsForNode(final XsdTreeNode node)
    {
        if (this.addingStart != null && this.addingStart.getNode().equals(node))
        {
            removeAddingHelper();
            this.addingStart = null;
        }
        else if (this.addingEnd != null && this.addingEnd.getNode().equals(node))
        {
            updateAddingEndHighlight(null);
        }

        boolean selected = this.selectionIndicators.stream()
                .anyMatch((annotation) -> annotation.getSource().mapData().getNode().equals(node));
        if (!selected)
        {
            return;
        }
        if (this.selectionIndicators.size() == 1)
        {
            deselect();
            this.editorMap.getPanel().repaint();
            return;
        }
        for (SelectionAnnotation annotation : new LinkedHashSet<>(this.selectionIndicators))
        {
            MapData mapData = annotation.getSource().mapData();
            if (mapData.getNode().equals(node))
            {
                mapData.removeListener(this, MapData.MAP_DATA_CHANGED);
                removeActivationListener(mapData.getNode());
                this.selectionIndicators.remove(annotation);
                this.editorMap.removeAnimation(annotation);
            }
        }
        this.editorMap.update();
        this.editorMap.getPanel().repaint();
    }

    /**
     * Keep one activation listener per node while any selection or drag annotation references it.
     * @param node node to observe
     */
    private void addActivationListener(final XsdTreeNode node)
    {
        int count = this.activationListenerCounts.getOrDefault(node, 0);
        if (count == 0)
        {
            node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED, ReferenceType.WEAK);
        }
        this.activationListenerCounts.put(node, count + 1);
    }

    /**
     * Release one selection or drag annotation reference to a node.
     * @param node node to stop observing when unused
     */
    private void removeActivationListener(final XsdTreeNode node)
    {
        Integer count = this.activationListenerCounts.get(node);
        if (count == null)
        {
            return;
        }
        if (count <= 1)
        {
            node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            this.activationListenerCounts.remove(node);
        }
        else
        {
            this.activationListenerCounts.put(node, count - 1);
        }
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
        if (this.selectionIndicators.remove(renderable) && renderable instanceof SelectionAnnotation selection)
        {
            removeActivationListener(selection.getSource().mapData().getNode());
        }
        if (renderable != null && renderable.equals(this.selectedDraggable))
        {
            this.selectedDraggable = null;
        }
    }

    /**
     * Apply road layout to selection.
     * @param roadLayout road layout
     */
    public void applyRoadLayoutToSelection(final String roadLayout)
    {
        if (this.selectionIndicators.isEmpty())
        {
            return;
        }
        // Link.Defined(1).DefinedLayout(0)
        XsdTreeNode network = this.selectionIndicators.iterator().next().getSource().mapData().getNode().getParent();
        this.editor.getUndo().startAction(ActionType.VALUE_CHANGE, network, null);
        for (SelectionAnnotation i : this.selectionIndicators)
        {
            XsdTreeNode layout = i.getSource().mapData().getNode().getChild(1);
            if (layout.getNodeName().equals("RoadLayout"))
            {
                XsdTreeNode defined = layout.getOptions().stream().map((o) -> o.optionNode())
                        .filter((n) -> n.getNodeName().equals("Defined")).findFirst().get();
                layout.setOption(defined);
                layout = defined;
            }
            layout.getChild(0).setValue(roadLayout);
        }
    }

    /**
     * Apply link type to selection.
     * @param linkType link type
     */
    public void applyLinkTypeToSelection(final String linkType)
    {
        if (this.selectionIndicators.isEmpty())
        {
            return;
        }
        XsdTreeNode network = this.selectionIndicators.iterator().next().getSource().mapData().getNode().getParent();
        this.editor.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, network, null);
        for (SelectionAnnotation i : this.selectionIndicators)
        {
            XsdTreeNode link = i.getSource().mapData().getNode();
            link.setAttributeValue("Type", linkType);
        }
    }

}

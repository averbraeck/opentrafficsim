package org.opentrafficsim.editor.extensions.map;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djutils.draw.curve.Arc2d;
import org.djutils.draw.curve.Clothoid2d;
import org.djutils.draw.curve.OffsetCurve2d;
import org.djutils.draw.curve.Straight2d;
import org.djutils.draw.function.ContinuousPiecewiseLinearFunction;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventListenerMap;
import org.djutils.event.EventProducer;
import org.djutils.event.EventType;
import org.djutils.event.reference.ReferenceType;
import org.djutils.math.AngleUtil;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.animation.network.LinkAnimation.LinkData;
import org.opentrafficsim.animation.road.CrossSectionElementAnimation;
import org.opentrafficsim.animation.road.LaneAnimation;
import org.opentrafficsim.animation.road.PriorityAnimation;
import org.opentrafficsim.animation.road.StripeAnimation;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.StripeElement.StripeLateralSync;
import org.opentrafficsim.base.geometry.OtsGeometryUtil;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.geometry.BezierCubic2d;
import org.opentrafficsim.core.geometry.CurveFlattener;
import org.opentrafficsim.core.geometry.PolyLineCurve2d;
import org.opentrafficsim.editor.ChildNodeFinder;
import org.opentrafficsim.editor.Coalesced;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeUtil;
import org.opentrafficsim.editor.extensions.Adapters;
import org.opentrafficsim.road.network.CrossSectionGeometry;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.StripeData.StripePhaseSync;
import org.opentrafficsim.road.network.factory.xml.utils.RoadLayoutOffsets.CseData;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType.ArcDirection;
import org.opentrafficsim.xml.generated.Link.Arc;
import org.opentrafficsim.xml.generated.Link.Bezier;
import org.opentrafficsim.xml.generated.Link.Clothoid;
import org.opentrafficsim.xml.generated.Link.PolyLine;

import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;

/**
 * LinkData for the editor Map. This class will also listen to any changes that may affect the link shape, maintain the drawn
 * layout, and maintain the priority animation.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class MapLinkData extends MapData implements LinkData, EventListener, EventProducer
{

    /** Event when layout is rebuilt. */
    public static final EventType LAYOUT_REBUILT = new EventType("LAYOUTREBUILT", new MetaData("LAYOUT", "Layout is rebuilt.",
            new ObjectDescriptor("LinkData", "Map link data object.", MapLinkData.class)));

    /** Event listeners. */
    private final EventListenerMap eventListenerMap = new EventListenerMap();

    /** Listener to changes in shape. */
    private final ShapeListener shapeListener = new ShapeListener();

    /** String attribute. */
    private String id = "";

    /** Tree node of start. */
    private XsdTreeNode nodeStart;

    /** Tree node of end. */
    private XsdTreeNode nodeEnd;

    /** Start direction. */
    private Direction directionStart = Direction.ZERO;

    /** End direction. */
    private Direction directionEnd = Direction.ZERO;

    /** Start offset. */
    private Length offsetStart;

    /** End offset. */
    private Length offsetEnd;

    /** Start point. */
    private Point2d pointStart;

    /** End point. */
    private Point2d pointEnd;

    /** Continuous design line. */
    private OffsetCurve2d designLine = null;

    /** Flattened design line. */
    private PolyLine2d flattenedDesignLine = null;

    /** Location. */
    private DirectedPoint2d location;

    /** Absolute contour. */
    private Polygon2d absoluteContour;

    /** Relative contour. */
    private Polygon2d relativeContour;

    /** Node describing the road layout. */
    private XsdTreeNode roadLayoutNode;

    /** Node linking defined road layout id. */
    private XsdTreeNode definedRoadLayoutNode;

    /** Listener to road layout, if locally defined. */
    private RoadLayoutListener roadLayoutListener;

    /** Listener to flattener, if locally defined. */
    private FlattenerListener flattenerListener;

    /** Set of drawable cross-section elements. */
    private Set<Renderable2d<?>> crossSectionElements = new LinkedHashSet<>();

    /** Lane data. */
    private java.util.Map<String, MapLaneData> laneData = new LinkedHashMap<>();

    /** Stripe data. */
    private java.util.Map<String, MapStripeData> stripeData = new LinkedHashMap<>();

    /** Cached downstream lane connection state per lane. */
    private final Map<MapLaneData, Boolean> hasNextLaneCache = new LinkedHashMap<>();

    /** Cached upstream lane connection state per lane. */
    private final Map<MapLaneData, Boolean> hasPrevLaneCache = new LinkedHashMap<>();

    /** Priority animation. */
    private PriorityAnimation priorityAnimation;

    /** Whether this is a connector. */
    private final boolean isConnector;

    /** Centroid node of this connector, if it is a connector. */
    private XsdTreeNode centroid;

    /** Regular node of this connector, if it is a connector. */
    private XsdTreeNode connectorNode;

    /** Whether link geometry is dirty. */
    private boolean dirtyGeometry = true;

    /** Whether layout is dirty. */
    private boolean dirtyLayout = true;

    /** Escape delayed execution when object was destroyed. */
    private volatile boolean destroyed = false;

    /** Schedules a rebuild of dirty geometry and layout. */
    private final Coalesced rebuild = new Coalesced(() ->
    {
        if (!this.destroyed)
        {
            checkDirtyGeometry();
        }
    });

    /**
     * Constructor.
     * @param map map.
     * @param linkNode node Ots.Network.Link.
     * @param editor editor.
     */
    public MapLinkData(final EditorMap map, final XsdTreeNode linkNode, final OtsEditor editor)
    {
        super(map, linkNode, editor);
        // linkNode.getRoot().addListener(this, XsdTreeNodeRoot.NODE_CREATED, ReferenceType.WEAK);
        linkNode.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
        this.isConnector = "Connector".equals(linkNode.getNodeName());
        if (this.isConnector)
        {
            laterIfNotDestroyed(() ->
            {
                // this is for when the centroid or node is later in a loaded xml
                notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Id", null}));
                this.centroid = replaceNode(this.centroid, linkNode.getCoupledNodeAttribute("Centroid").orElse(null));
                this.connectorNode = replaceNode(this.connectorNode, linkNode.getCoupledNodeAttribute("Node").orElse(null));
                setDirtyAndCheckValidity();
            });
            return;
        }
        if (linkNode.getChildCount() > 0 && EditorMap.LINK_SHAPES.contains(linkNode.getChild(0).getNodeName()))
        {
            linkNode.getChild(0).addListener(this.shapeListener, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);
            this.shapeListener.shapeNode = linkNode.getChild(0);
        }

        // as RoadLayout is the default, a setOption() never triggers this (for Defined this is not required)
        laterIfNotDestroyed(() ->
        {
            if (linkNode.getChildCount() < 2)
            {
                return; // children not (yet) available, e.g. during replay/undo-redo
            }
            XsdTreeNode layout = linkNode.getChild(1);
            layout.removeListener(this, XsdTreeNode.OPTION_CHANGED);
            layout.addListener(this, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);
            if (layout.equals(layout.getOption()) || layout.getOption() != null)
            {
                XsdTreeNode selected = layout.getOption() == null ? layout : layout.getOption();
                notify(new Event(XsdTreeNode.OPTION_CHANGED, new Object[] {selected, selected, layout}));
            }
            setDirtyAndCheckValidity();
        });

        // for when node is duplicated, set data immediately if (getNode().isActive())
        if (getNode().isActive())
        {
            laterIfNotDestroyed(() ->
            {
                if (linkNode.getChildCount() < 1)
                {
                    return; // children not (yet) available, e.g. during replay/undo-redo
                }
                // this is for 1) when delete is undone, as some children are recovered later, including the shape node, and 2)
                // when the start or end node are later in a loaded xml
                XsdTreeNode shape = linkNode.getChild(0);
                this.shapeListener.shapeNode = shape;
                shape.removeListener(this.shapeListener, XsdTreeNode.OPTION_CHANGED);
                shape.addListener(this.shapeListener, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);

                notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Id", null}));
                this.nodeStart = replaceNode(this.nodeStart, linkNode.getCoupledNodeAttribute("NodeStart").orElse(null));
                this.nodeEnd = replaceNode(this.nodeEnd, linkNode.getCoupledNodeAttribute("NodeEnd").orElse(null));
                notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "OffsetStart", null}));
                notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "OffsetEnd", null}));
                // Use the shape node itself as previous so listeners treat this as a real selection/state sync (not a no-op).
                this.shapeListener
                        .notify(new Event(XsdTreeNode.OPTION_CHANGED, new Object[] {shape, shape, shape.getOption()}));

                setDirtyAndCheckValidity();
            });
        }
    }

    /**
     * Executes the runnable later on the Swing EDT, but only if at that time the object is not destroyed.
     * @param r runnable
     */
    private void laterIfNotDestroyed(final Runnable r)
    {
        SwingUtilities.invokeLater(() ->
        {
            if (!this.destroyed)
            {
                r.run();
            }
        });
    }

    @Override
    public void destroy()
    {
        this.destroyed = true;
        super.destroy();
        removeAllListeners();
        for (Renderable2d<?> renderable : this.crossSectionElements)
        {
            getMap().removeAnimation(renderable);
        }
        this.crossSectionElements.clear();
        if (this.roadLayoutListener != null)
        {
            this.roadLayoutListener.destroy();
            this.roadLayoutListener = null;
        }
        if (this.definedRoadLayoutNode != null)
        {
            this.definedRoadLayoutNode.removeListener(this, XsdTreeNode.VALUE_CHANGED);
            this.definedRoadLayoutNode = null;
        }
        if (this.flattenerListener != null)
        {
            this.flattenerListener.destroy();
            this.flattenerListener = null;
        }
        if (this.priorityAnimation != null)
        {
            getMap().removeAnimation(this.priorityAnimation);
        }
        if (this.nodeStart != null)
        {
            this.nodeStart.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
        if (this.nodeEnd != null)
        {
            this.nodeEnd.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
        if (this.centroid != null)
        {
            this.centroid.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
        if (this.connectorNode != null)
        {
            this.connectorNode.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
        this.roadLayoutNode = null;
        getMap().scheduleStripeSynchronization();
    }

    /**
     * Check for dirty geometry, and restore it if it is dirty.
     */
    private void checkDirtyGeometry()
    {
        if (this.dirtyGeometry)
        {
            buildDesignLine();
        }
        else if (this.dirtyLayout)
        {
            buildLayout();
        }
    }

    @Override
    public DirectedPoint2d getLocation()
    {
        checkDirtyGeometry();
        return this.location;
    }

    @Override
    public Polygon2d getAbsoluteContour()
    {
        checkDirtyGeometry();
        return this.absoluteContour;
    }

    @Override
    public Polygon2d getRelativeContour()
    {
        checkDirtyGeometry();
        return this.relativeContour;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public boolean isConnector()
    {
        return this.isConnector;
    }

    @Override
    public PolyLine2d getCenterLine()
    {
        checkDirtyGeometry();
        return this.flattenedDesignLine;
    }

    @Override
    public PolyLine2d getLine()
    {
        return OtsShape.transformLine(getCenterLine(), getLocation());
    }

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(XsdTreeNode.OPTION_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode selected = (XsdTreeNode) content[1];
            if (selected.getNodeName().equals("RoadLayout") || (selected.getNodeName().equals("xsd:sequence")
                    && selected.getChildCount() > 0 && selected.getChild(0).getNodeName().equals("DefinedLayout")))
            {
                // road layout
                if (this.roadLayoutListener != null)
                {
                    this.roadLayoutListener.destroy();
                }
                else if (this.roadLayoutNode != null)
                {
                    getMap().getRoadLayoutListener(this.roadLayoutNode).removeListener(this, ChangeListener.CHANGE_EVENT);
                }
                if (this.definedRoadLayoutNode != null)
                {
                    this.definedRoadLayoutNode.removeListener(this, XsdTreeNode.VALUE_CHANGED);
                }
                if (selected.getNodeName().equals("RoadLayout"))
                {
                    this.roadLayoutNode = selected;
                    this.definedRoadLayoutNode = null;
                    this.roadLayoutListener = new RoadLayoutListener(selected, this::getEval);
                    this.roadLayoutListener.addListener(this, ChangeListener.CHANGE_EVENT, ReferenceType.WEAK);
                }
                else
                {
                    this.definedRoadLayoutNode = selected.getChild(0);
                    this.definedRoadLayoutNode.addListener(this, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
                    this.roadLayoutNode = this.definedRoadLayoutNode.getCoupledNodeValue().orElse(null);
                    if (this.roadLayoutNode != null)
                    {
                        getMap().getRoadLayoutListener(this.roadLayoutNode).addListener(this, ChangeListener.CHANGE_EVENT,
                                ReferenceType.WEAK);
                    }
                    this.roadLayoutListener = null;
                }
            }
            else
            {
                // flattener
                if (this.flattenerListener != null)
                {
                    this.flattenerListener.destroy();
                }
                this.flattenerListener = new FlattenerListener(selected, this::getEval);
            }
            setDirtyLayout();
            return;
        }
        else if (event.getType().equals(XsdTreeNode.VALUE_CHANGED))
        {
            // defined road layout value changed
            if (this.roadLayoutNode != null)
            {
                getMap().getRoadLayoutListener(this.roadLayoutNode).removeListener(this, ChangeListener.CHANGE_EVENT);
            }
            this.roadLayoutNode = this.definedRoadLayoutNode.getCoupledNodeValue().orElse(null);
            if (this.roadLayoutNode != null)
            {
                getMap().getRoadLayoutListener(this.roadLayoutNode).addListener(this, ChangeListener.CHANGE_EVENT,
                        ReferenceType.WEAK);
            }
            setDirtyLayout();
            return;
        }
        else if (event.getType().equals(ChangeListener.CHANGE_EVENT))
        {
            XsdTreeNode node = (XsdTreeNode) event.getContent();
            if (node.getNodeName().equals("RoadLayout") || node.getNodeName().equals("DefinedLayout"))
            {
                if (node.isIdentifiable() && this.definedRoadLayoutNode != null)
                {
                    // for if the id in the road layout has changed
                    this.roadLayoutNode = this.definedRoadLayoutNode.getCoupledNodeValue().orElse(null);
                }
                if (node.equals(this.roadLayoutNode) && node.reportInvalidId().isEmpty())
                {
                    setDirtyLayout();
                }
            }
            else
            {
                // change in flattener
                setDirtyAndCheckValidity();
            }
            return;
        }

        // any attribute of the link node, or of either of the connected nodes
        Object[] content = (Object[]) event.getContent();
        XsdTreeNode node = (XsdTreeNode) content[0];
        String attribute = (String) content[1];
        String value = node.getAttributeValueOrDefault(attribute);

        if ("Id".equals(attribute) && node.getNodeName().equals("Link"))
        {
            this.id = value == null ? "" : value;
            return;
        }
        else if ("NodeStart".equals(attribute))
        {
            this.nodeStart = replaceNode(this.nodeStart, getNode().getCoupledNodeAttribute("NodeStart").orElse(null));
        }
        else if ("NodeEnd".equals(attribute))
        {
            this.nodeEnd = replaceNode(this.nodeEnd, getNode().getCoupledNodeAttribute("NodeEnd").orElse(null));
        }
        else if ("Centroid".equals(attribute))
        {
            this.centroid = replaceNode(this.centroid, getNode().getCoupledNodeAttribute("Centroid").orElse(null));
        }
        else if ("Node".equals(attribute))
        {
            this.connectorNode = replaceNode(this.connectorNode, getNode().getCoupledNodeAttribute("Node").orElse(null));
        }
        else if ("OffsetStart".equals(attribute))
        {
            executeForAttribute((v) -> this.offsetStart = v, getNode(), attribute, Adapters.get(Length.class));
        }
        else if ("OffsetEnd".equals(attribute))
        {
            executeForAttribute((v) -> this.offsetEnd = v, getNode(), attribute, Adapters.get(Length.class));
        }
        else if (!"Coordinate".equals(attribute) && !"Direction".equals(attribute))
        {
            // Coordinate and Direction pertain to either of the nodes, to which this class also listens
            // Otherwise: other attribute, not important, do not build design line
            return;
        }
        setDirtyAndCheckValidity();
    }

    /**
     * Replaces the old node with the new node, adding and removing this as a listener as required. If a node refers to a
     * default input parameter node, the node that the input parameter id refers to is found instead.
     * @param oldNode former node.
     * @param newNode new node.
     * @return the actual new node (Ots.Network.Node).
     */
    private XsdTreeNode replaceNode(final XsdTreeNode oldNode, final XsdTreeNode newNode)
    {
        if (oldNode != null)
        {
            oldNode.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            if (oldNode.getPathString().equals(XsdPaths.DEFAULT_INPUT_PARAMETER_STRING))
            {
                XsdTreeNode node = findInputNode(newNode);
                if (node != null)
                {
                    node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
                }
            }
        }
        if (newNode != null)
        {
            newNode.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
            if (newNode.getPathString().equals(XsdPaths.DEFAULT_INPUT_PARAMETER_STRING))
            {
                XsdTreeNode node = findInputNode(newNode);
                if (node != null)
                {
                    node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
                }
                return node;
            }
        }
        return newNode;
    }

    /**
     * Finds the network node that the value of an input parameter node refers to.
     * @param inputParameter input parameter node (default).
     * @return the actual node (Ots.Network.Node).
     */
    private XsdTreeNode findInputNode(final XsdTreeNode inputParameter)
    {
        String inputId = inputParameter.getId();
        String nodeId;
        try
        {
            nodeId = (String) getEval().evaluate(inputId.substring(1, inputId.length() - 1));
        }
        catch (IllegalArgumentException ex)
        {
            // expression could not be evaluated, possibly a missing parameter
            return null;
        }
        XsdTreeNode ots = inputParameter.getRoot();
        for (XsdTreeNode child : ots.getChildren())
        {
            if (child.getPathString().equals(XsdPaths.NETWORK))
            {
                for (XsdTreeNode networkElement : child.getChildren())
                {
                    if (networkElement.isType("Node") && nodeId != null && nodeId.equals(networkElement.getId()))
                    {
                        return networkElement;
                    }
                }
            }
        }
        return null;
    }

    /**
     * The map was notified a new coordinate node was added. The node may or may not be part of this link.
     * @param node added coordinate node.
     */
    public void addCoordinate(final XsdTreeNode node)
    {
        if (this.shapeListener.shapeNode != null && this.shapeListener.shapeNode.equals(node.getParent()))
        {
            this.shapeListener.coordinates.put(node, Adapters.parseOrNull(getEval(), node.getValueOrDefault(), Point2d.class));
            setDirtyAndCheckValidity();
            node.addListener(this.shapeListener, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
            node.addListener(this.shapeListener, XsdTreeNode.MOVED, ReferenceType.WEAK);
        }
    }

    /**
     * The map was notified a coordinate node was removed. The node may or may not be part of this link.
     * @param node removed coordinate node.
     */
    public void removeCoordinate(final XsdTreeNode node)
    {
        // this.shapeListener.shapeNode.equals(node.getParent()) does not work as the parent is null after being removed
        // this.shapeListener.coordinates.containsKey() id. as the ordering is incomplete as the node is removed from the parent
        Iterator<XsdTreeNode> it = this.shapeListener.coordinates.keySet().iterator();
        while (it.hasNext())
        {
            XsdTreeNode key = it.next();
            if (node.equals(key))
            {
                it.remove();
                setDirtyAndCheckValidity();
                node.removeListener(this.shapeListener, XsdTreeNode.VALUE_CHANGED);
                node.removeListener(this.shapeListener, XsdTreeNode.MOVED);
                return;
            }
        }
    }

    /**
     * Sets the link as dirty and checks validity. A rebuild is scheduled so that geometry and layout are also updated when the
     * link itself is not drawn, for example to update lanes and stripes after a change in the link shape.
     */
    private void setDirtyAndCheckValidity()
    {
        this.dirtyGeometry = true;
        this.rebuild.request();

        // Connector
        if (this.isConnector)
        {
            if (this.centroid == null || this.connectorNode == null)
            {
                setInvalid();
                return;
            }
            executeForAttribute((v) -> this.pointStart = v, this.centroid, "Coordinate", Adapters.get(Point2d.class));
            executeForAttribute((v) -> this.pointEnd = v, this.connectorNode, "Coordinate", Adapters.get(Point2d.class));
            if (this.pointStart == null || this.pointEnd == null)
            {
                setInvalid();
                return;
            }
            setValid();
            return;
        }

        // Node
        if (this.nodeStart == null || this.nodeEnd == null || this.nodeStart.equals(this.nodeEnd)
                || this.shapeListener.shapeNode == null)
        {
            setInvalid();
            return;
        }
        executeForAttribute((v) -> this.pointStart = v, this.nodeStart, "Coordinate", Adapters.get(Point2d.class));
        executeForAttribute((v) -> this.directionStart = v, this.nodeStart, "Direction", Adapters.get(Direction.class));
        executeForAttribute((v) -> this.pointEnd = v, this.nodeEnd, "Coordinate", Adapters.get(Point2d.class));
        executeForAttribute((v) -> this.directionEnd = v, this.nodeEnd, "Direction", Adapters.get(Direction.class));
        if (this.pointStart == null || this.pointEnd == null)
        {
            setInvalid();
            return;
        }

        setValid();
    }

    /**
     * Set only the layout as dirty.
     */
    private void setDirtyLayout()
    {
        this.dirtyLayout = true;
        this.rebuild.request();
        fireEvent(new Event(MAP_DATA_CHANGED, null));
    }

    /**
     * Builds the design line.
     */
    private void buildDesignLine()
    {
        // TODO: move this to a parallel process?

        XsdTreeNode linkNode = getNode();
        if (linkNode.getParent() == null || !linkNode.isActive()
                || (this.isConnector && (this.centroid == null || this.connectorNode == null)) || (!this.isConnector
                        && (this.nodeStart == null || this.nodeEnd == null || this.shapeListener.shapeNode == null)))
        {
            this.dirtyGeometry = false;
            this.dirtyLayout = false;
            return;
        }

        if (this.pointStart == null || this.pointEnd == null)
        {
            return;
        }

        // Connector
        if (this.isConnector)
        {
            DirectedPoint2d fromPoint = new DirectedPoint2d(this.pointStart, this.pointStart.directionTo(this.pointEnd));
            this.designLine = new Straight2d(fromPoint, this.pointStart.distance(this.pointEnd));
            setGeometry();
            return;
        }

        // Node
        DirectedPoint2d fromPoint = getOffsetStartPoint();
        DirectedPoint2d toPoint = getOffsetEndPoint();

        // Geometry
        this.designLine = this.shapeListener.getContiuousLine(fromPoint, toPoint);
        if (this.designLine == null)
        {
            this.designLine = new Straight2d(fromPoint, fromPoint.distance(toPoint));
        }
        setGeometry();
        if (this.priorityAnimation != null)
        {
            getMap().removeAnimation(this.priorityAnimation);
        }
        this.priorityAnimation = new PriorityAnimation(new MapPriorityData(this), getMap().getContextualized());
        buildLayout();
        updateNode(this.nodeStart);
        updateNode(this.nodeEnd);
    }

    /**
     * Returns the offset start point.
     * @return offset start point
     */
    public DirectedPoint2d getOffsetStartPoint()
    {
        return getOffsetPoint(this.pointStart, this.directionStart, this.offsetStart);
    }

    /**
     * Returns the offset end point.
     * @return offset end point
     */
    public DirectedPoint2d getOffsetEndPoint()
    {
        return getOffsetPoint(this.pointEnd, this.directionEnd, this.offsetEnd);
    }

    /**
     * Returns the offset end point.
     * @param point original point
     * @param dir point direction
     * @param offset offset
     * @return offset poitn
     */
    private DirectedPoint2d getOffsetPoint(final Point2d point, final Direction dir, final Length offset)
    {
        double dirStart = dir == null ? 0.0 : dir.si;
        DirectedPoint2d fromPoint = new DirectedPoint2d(point, dirStart);
        if (offset != null)
        {
            fromPoint = OtsGeometryUtil.offsetPoint(fromPoint, offset.si);
        }
        return fromPoint;
    }

    /**
     * Invokes {@code update()} on the map data of the given node, if available. This refreshes the node color, which depends on
     * the alignment of all links connected to the node.
     * @param node node
     */
    private void updateNode(final XsdTreeNode node)
    {
        if (node == null)
        {
            return;
        }
        MapData data = getMap().getData(node).orElse(null);
        if (data instanceof MapNodeData nodeData)
        {
            nodeData.update();
        }
    }

    /**
     * Based on the design line, sets the flattened line, location, absolute contour and relative contour.
     */
    private void setGeometry()
    {
        this.flattenedDesignLine = this.designLine.toPolyLine(getFlattener());
        DirectedPoint2d point = this.flattenedDesignLine.getLocationFractionExtended(0.5);
        this.location = new DirectedPoint2d(point.x, point.y, point.dirZ);
        this.absoluteContour =
                new Polygon2d(PolyLine2d.concatenate(this.flattenedDesignLine, this.flattenedDesignLine.reverse()).iterator());
        this.relativeContour =
                new Polygon2d(0.0, OtsShape.toRelativeTransform(this.location).transform(this.absoluteContour.iterator()));
        this.dirtyGeometry = false;
    }

    /**
     * Returns the flattener to use, which is either a flattener defined at link level, or at network level.
     * @return Flattener, flattener to use.
     */
    private CurveFlattener getFlattener()
    {
        if (this.flattenerListener != null)
        {
            CurveFlattener flattener = this.flattenerListener.getData();
            if (flattener != null)
            {
                return flattener; // otherwise not valid, return network level flattener
            }
        }
        return getMap().getNetworkFlattener();
    }

    /**
     * Builds all animation objects for stripes, lanes, shoulders, and their center lines and id's.
     */
    private void buildLayout()
    {
        if (this.designLine == null)
        {
            return;
        }
        java.util.Map<XsdTreeNode, CseData> cseDataMap = null;
        if (this.roadLayoutNode != null)
        {
            RoadLayoutListener listener = this.roadLayoutListener != null ? this.roadLayoutListener
                    : getMap().getRoadLayoutListener(this.roadLayoutNode);
            if (listener == null)
            {
                // layout is not available (yet); keep the current animations and try again on the next change
                return;
            }
            cseDataMap = listener.getData();
        }
        for (Renderable2d<?> renderable : this.crossSectionElements)
        {
            getMap().removeAnimation(renderable);
        }
        for (MapStripeData value : this.stripeData.values())
        {
            getMap().getSynchronizableStripes().remove(value);
        }
        this.crossSectionElements.clear();
        this.laneData.clear();
        this.stripeData.clear();
        if (cseDataMap != null)
        {
            MiddleOffset middleOffset = new MiddleOffset();
            Map<String, XsdTreeNode> stripeOverrides = new LinkedHashMap<>();
            if (new ChildNodeFinder(getNode()).hasActiveChild("DefinedLayout"))
            {
                for (XsdTreeNode child : getNode().getChildren())
                {
                    if (child.getNodeName().equals("xsd:sequence"))
                    {
                        for (XsdTreeNode override : child.getChildren())
                        {
                            if (override.getNodeName().equals("StripeOverride") && override.isActive())
                            {
                                stripeOverrides.put(override.getAttributeValueOrDefault("Stripe"), override);
                            }
                        }
                    }
                }
            }

            for (Entry<XsdTreeNode, CseData> entry : cseDataMap.entrySet())
            {
                XsdTreeNode node = entry.getKey();
                CseData cseData = entry.getValue();
                ContinuousPiecewiseLinearFunction offsetFunc = ContinuousPiecewiseLinearFunction.of(0.0,
                        cseData.centerOffsetStart.si, 1.0, cseData.centerOffsetEnd.si);
                if (node.getNodeName().equals("Stripe"))
                {
                    StripeAnimation stripe = createStripe(node, offsetFunc, middleOffset, stripeOverrides);
                    if (stripe == null)
                    {
                        continue;
                    }
                    this.crossSectionElements.add(stripe);
                }
                else
                {
                    ContinuousPiecewiseLinearFunction widthFunc =
                            ContinuousPiecewiseLinearFunction.of(0.0, cseData.widthStart.si, 1.0, cseData.widthEnd.si);
                    CrossSectionGeometry geometry =
                            CrossSectionGeometry.of(this.designLine, getFlattener(), offsetFunc, widthFunc);
                    if (node.getNodeName().equals("Lane"))
                    {
                        MapLaneData mapLaneData = new MapLaneData(node.getId(), getNode(), geometry, this);
                        LaneAnimation lane =
                                new LaneAnimation(mapLaneData, getMap().getContextualized(), Color.GRAY.brighter());
                        this.crossSectionElements.add(lane);
                        this.laneData.put(node.getId(), mapLaneData);
                    }
                    else if (node.getNodeName().equals("Shoulder"))
                    {
                        CrossSectionElementAnimation<?> shoulder = new CrossSectionElementAnimation<>(
                                new MapShoulderData(getNode(), geometry), getMap().getContextualized(), Color.DARK_GRAY);
                        this.crossSectionElements.add(shoulder);
                    }
                    else if (node.getNodeName().equals("NoTrafficLane"))
                    {
                        CrossSectionElementAnimation<?> noTrafficLane = new CrossSectionElementAnimation<>(
                                new MapCrossSectionData(getNode(), geometry), getMap().getContextualized(), Color.DARK_GRAY);
                        this.crossSectionElements.add(noTrafficLane);
                    }
                    else
                    {
                        throw new OtsRuntimeException(
                                "Element " + node.getNodeName() + " is not a supported cross-section element.");
                    }
                }
            }
        }
        getMap().scheduleStripeSynchronization();
        getMap().scheduleLaneConnectionCacheClear();
        this.dirtyLayout = false;
        fireEvent(LAYOUT_REBUILT, this);
        getMap().update();
    }

    /**
     * Creates stripe animation from stripe node.
     * @param node node Stripe within road layout
     * @param offsetFunc offset function
     * @param middleOffset middle offset to add stripe offsets to
     * @param stripeOverrides stripe overrides
     * @return stripe animation, {@code null} if something was not valid
     */
    private StripeAnimation createStripe(final XsdTreeNode node, final ContinuousPiecewiseLinearFunction offsetFunc,
            final MiddleOffset middleOffset, final Map<String, XsdTreeNode> stripeOverrides)
    {
        StripePhaseSync phaseSync = StripePhaseSync.NONE;
        Length dashOffset = Length.ZERO;
        StripeLateralSync lateralSync = StripeLateralSync.LINK;
        List<XsdTreeNode> elementNodes;

        XsdTreeNode overrideNode = stripeOverrides.get(node.getId());
        XsdTreeNode overrideDataNode = null;
        XsdTreeNode stripeDataNode;
        XsdTreeNode definedOverride =
                overrideNode == null ? null : XsdTreeNodeUtil.findActiveChild(overrideNode, "DefinedStripe");
        if (definedOverride != null)
        {
            // override with defined stripe; take that stripe definition completely
            stripeDataNode = definedOverride.getCoupledNodeValue().orElse(null);
            if (stripeDataNode == null)
            {
                return null;
            }
        }
        else
        {
            if (overrideNode != null)
            {
                overrideDataNode = XsdTreeNodeUtil.findActiveChild(overrideNode, "Custom");
            }
            stripeDataNode = MapStripeDefinitionUtil.resolveStripeNode(node);
            if (stripeDataNode == null)
            {
                return null;
            }
        }
        StripeDefinition stripeDat = MapStripeDefinitionUtil.collectStripeData(stripeDataNode, overrideDataNode, getEval());
        phaseSync = stripeDat.phaseSync();
        dashOffset = stripeDat.dashOffset();
        lateralSync = stripeDat.lateralSync();
        elementNodes = stripeDat.elementNodes();

        List<StripeElement> elements = MapStripeDefinitionUtil.createStripeElements(elementNodes, getEval(), this::setInvalid);
        Length width = Length.ZERO;
        for (StripeElement element : elements)
        {
            width = width.plus(element.width());
        }
        ContinuousPiecewiseLinearFunction widthFunc = ContinuousPiecewiseLinearFunction.of(0.0, width.si, 1.0, width.si);
        CrossSectionGeometry geometry = CrossSectionGeometry.of(this.designLine, getFlattener(), offsetFunc, widthFunc);
        middleOffset.addStartOffset(geometry.offset().get(0.0));
        middleOffset.addEndOffset(geometry.offset().get(1.0));
        MapStripeData data = new MapStripeData(dashOffset, getNode(), geometry, elements, lateralSync, this.flattenedDesignLine,
                middleOffset, this.directionStart, this.directionEnd);
        getMap().getSynchronizableStripes().put(data, new SynchronizableMapStripe(this, data, phaseSync));
        this.stripeData.put(node.getId(), data);
        return new StripeAnimation(data, getMap().getContextualized());
    }

    /**
     * Returns the stripes.
     * @return stripes
     */
    protected Collection<MapStripeData> getStripeData()
    {
        return this.stripeData.values();
    }

    @Override
    public EventListenerMap getEventListenerMap()
    {
        return this.eventListenerMap;
    }

    @Override
    public void update()
    {
        if (getNode().isActive())
        {
            this.id = getNode().getId() == null ? "" : getNode().getId();
            this.nodeStart = replaceNode(this.nodeStart, getNode().getCoupledNodeAttribute("NodeStart").orElse(null));
            this.nodeEnd = replaceNode(this.nodeEnd, getNode().getCoupledNodeAttribute("NodeEnd").orElse(null));
            XsdTreeNode node = getNode();
            if (node != null && node.getPathString().equals(XsdPaths.LINK)) // Connectors have no offsets
            {
                var lengthAdapter = Adapters.get(Length.class);
                executeForAttribute((v) -> this.offsetStart = v, node, "OffsetStart", lengthAdapter);
                executeForAttribute((v) -> this.offsetEnd = v, node, "OffsetEnd", lengthAdapter);
            }
            this.shapeListener.updateShape();
            setDirtyAndCheckValidity();
        }
    }

    /**
     * Notification from the Map that a node (Ots.Network.Node) was created or its id was changed.
     */
    public void nodeCreatedOrIdChanged()
    {
        this.nodeStart = replaceNode(this.nodeStart, getNode().getCoupledNodeAttribute("NodeStart").orElse(null));
        this.nodeEnd = replaceNode(this.nodeEnd, getNode().getCoupledNodeAttribute("NodeEnd").orElse(null));
        this.centroid = replaceNode(this.centroid, getNode().getCoupledNodeAttribute("Centroid").orElse(null));
        this.connectorNode = replaceNode(this.connectorNode, getNode().getCoupledNodeAttribute("Node").orElse(null));
        setDirtyAndCheckValidity();
    }

    /**
     * Notification from the Map that a node (Ots.Network.Node) is removed.
     * @param node node of node
     */
    public void notifyNodeRemoved(final XsdTreeNode node)
    {
        if (node.equals(this.nodeStart))
        {
            this.nodeStart = null;
            this.directionStart = null;
            setDirtyAndCheckValidity();
        }
        else if (node.equals(this.nodeEnd))
        {
            this.nodeEnd = null;
            this.directionEnd = null;
            setDirtyAndCheckValidity();
        }
        else if (node.equals(this.centroid))
        {
            this.centroid = null;
            setDirtyAndCheckValidity();
        }
        else if (node.equals(this.connectorNode))
        {
            this.connectorNode = null;
            setDirtyAndCheckValidity();
        }
    }

    /**
     * Returns the editor lane data for the lane of given id.
     * @param laneId id.
     * @return editor lane data for the lane of given id.
     */
    public MapLaneData getLaneData(final String laneId)
    {
        return this.laneData.get(laneId);
    }

    /**
     * Returns whether the lane has a downstream lane on another link.
     * @param lane lane
     * @return whether the lane has a downstream lane on another link
     */
    public boolean hasNextLane(final MapLaneData lane)
    {
        return this.hasNextLaneCache.computeIfAbsent(lane, this::computeHasNextLane);
    }

    /**
     * Returns whether the lane has an upstream lane on another link.
     * @param lane lane
     * @return whether the lane has an upstream lane on another link
     */
    public boolean hasPrevLane(final MapLaneData lane)
    {
        return this.hasPrevLaneCache.computeIfAbsent(lane, this::computeHasPrevLane);
    }

    /**
     * Computes whether the lane has a downstream lane on another link.
     * @param lane lane
     * @return whether the lane has a downstream lane on another link
     */
    private boolean computeHasNextLane(final MapLaneData lane)
    {
        XsdTreeNode endNode = getEndNode();
        XsdTreeNode networkNode = getMap().getNetworkNode();
        if (endNode == null || networkNode == null)
        {
            return false;
        }
        PolyLine2d centerLine = lane.getCenterLine();
        if (centerLine == null || centerLine.size() < 1)
        {
            return false;
        }
        Point2d ownEnd = centerLine.getLast();
        for (XsdTreeNode child : networkNode.getChildren())
        {
            if (!XsdPaths.LINK.equals(child.getPathString()))
            {
                continue;
            }
            MapData data = getMap().getData(child).orElse(null);
            if (!(data instanceof MapLinkData other) || other == this)
            {
                continue;
            }
            if (endNode.equals(other.getStartNode()) && hasConnectingLane(other, ownEnd, true))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Computes whether the lane has an upstream lane on another link.
     * @param lane lane
     * @return whether the lane has an upstream lane on another link
     */
    private boolean computeHasPrevLane(final MapLaneData lane)
    {
        XsdTreeNode startNode = getStartNode();
        XsdTreeNode networkNode = getMap().getNetworkNode();
        if (startNode == null || networkNode == null)
        {
            return false;
        }
        PolyLine2d centerLine = lane.getCenterLine();
        if (centerLine == null || centerLine.size() < 1)
        {
            return false;
        }
        Point2d ownStart = centerLine.getFirst();
        for (XsdTreeNode child : networkNode.getChildren())
        {
            if (!XsdPaths.LINK.equals(child.getPathString()))
            {
                continue;
            }
            MapData data = getMap().getData(child).orElse(null);
            if (!(data instanceof MapLinkData other) || other == this)
            {
                continue;
            }
            if (startNode.equals(other.getEndNode()) && hasConnectingLane(other, ownStart, false))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether any lane or shoulder on the given link connects to the given point within {@link Lane#MARGIN}.
     * @param other other link data
     * @param point own endpoint
     * @param next looking for next lane (otherwise prev)
     * @return whether any lane or shoulder on the given link connects to the given point
     */
    private boolean hasConnectingLane(final MapLinkData other, final Point2d point, final boolean next)
    {
        for (Renderable2d<?> element : other.crossSectionElements)
        {
            Object source = element.getSource();
            if (!(source instanceof MapLaneData) && !(source instanceof MapShoulderData))
            {
                continue;
            }
            PolyLine2d otherCenterLine = ((MapCrossSectionData) source).getCenterLine();
            if (otherCenterLine == null || otherCenterLine.size() < 1)
            {
                continue;
            }
            double jump = next ? otherCenterLine.getFirst().distance(point) : otherCenterLine.getLast().distance(point);
            if (jump < Lane.MARGIN.si)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Clears cached lane connection information.
     */
    public void clearLaneConnectionCache()
    {
        this.hasNextLaneCache.clear();
        this.hasPrevLaneCache.clear();
    }

    /**
     * Returns the design line.
     * @return design line
     */
    public OffsetCurve2d getDesignLine()
    {
        checkDirtyGeometry();
        return this.designLine;
    }

    /**
     * Returns the design line for a temporarily overridden offset at one of the link ends. A {@code null} offset is treated as
     * zero, consistent with the regular design line construction.
     * @param startOffset offset to use at start
     * @param endOffset offset to use at end
     * @return virtual design line, or {@code null} if it cannot be determined
     */
    public OffsetCurve2d getVirtualDesignLine(final Length startOffset, final Length endOffset)
    {
        DirectedPoint2d fromPoint = getOffsetPoint(this.pointStart, this.directionStart, startOffset);
        DirectedPoint2d toPoint = getOffsetPoint(this.pointEnd, this.directionEnd, endOffset);
        return this.shapeListener.getContiuousLine(fromPoint, toPoint);
    }

    /**
     * Returns whether the link has a Straight shape. Connectors have no shape child and return {@code false}.
     * @return whether the link has a Straight shape
     */
    public boolean isStraight()
    {
        return hasShapeNode() && this.shapeListener.shapeNode.getNodeName().equals("Straight");
    }

    /**
     * Returns whether the link has a Arc shape. Connectors have no shape child and return {@code false}.
     * @return whether the link has a Arc shape
     */
    public boolean isArc()
    {
        return hasShapeNode() && this.shapeListener.shapeNode.getNodeName().equals("Arc");
    }

    /**
     * Returns whether the link has the given shape.
     * @param clazz class of generated class of XML tag
     * @return whether the link has the given shape
     */
    private boolean isShape(final Class<?> clazz)
    {
        return hasShapeNode() && this.shapeListener.shapeNode.getNodeName().equals(clazz.getSimpleName());
    }

    // Bezier methods

    /**
     * Returns whether the link has a Bezier shape. Connectors have no shape child and return {@code false}.
     * @return whether the link has a Bezier shape
     */
    public boolean isBezier()
    {
        return isShape(Bezier.class);
    }

    /**
     * Returns the shape value of the Bezier. Connectors have no shape child and return the default value.
     * @return the shape value of the Bezier, or 1.0 when no Bezier shape is present
     */
    public double getBezierShape()
    {
        return !hasShapeNode() || this.shapeListener.shape == null ? 1.0 : this.shapeListener.shape;
    }

    /**
     * Returns whether the Bezier is weighted. Connectors have no Bezier shape and return {@code false}.
     * @return whether the Bezier is weighted
     */
    public boolean isWeightedBezier()
    {
        return hasShapeNode() && Boolean.TRUE.equals(this.shapeListener.weighted);
    }

    // Polyline methods

    /**
     * Returns whether the link has a polyline shape. Connectors have no shape child and return {@code false}.
     * @return whether the link has a polyline shape
     */
    public boolean isPolyLine()
    {
        return isShape(PolyLine.class);
    }

    /**
     * Returns a safe copy of the polyline coordinates, mapped from XSD node to point. Connectors have no shape child and return
     * an empty map.
     * @return a safe copy of the polyline coordinates, or an empty map when no shape child is present
     */
    public NavigableMap<XsdTreeNode, Point2d> getPolyLineCoordinates()
    {
        return hasShapeNode() ? new TreeMap<>(this.shapeListener.coordinates) : new TreeMap<>();
    }

    // Clothoid methods

    /**
     * Returns whether the link has a clothoid shape. Connectors have no shape child and return {@code false}.
     * @return whether the link has a clothoid shape
     */
    public boolean isClothoid()
    {
        return isShape(Clothoid.class);
    }

    /**
     * Returns the start curvature from the clothoid. Connectors have no clothoid shape and return {@code null}.
     * @return start curvature from the clothoid, or {@code null} when no clothoid is present
     */
    public LinearDensity getClothoidStartCurvature()
    {
        if (hasShapeNode() && this.designLine instanceof Clothoid2d)
        {
            return LinearDensity.ofSI(((Clothoid2d) this.designLine).getStartCurvature());
        }
        return null;
    }

    /**
     * Returns the end curvature from the clothoid. Connectors have no clothoid shape and return {@code null}.
     * @return end curvature from the clothoid, or {@code null} when no clothoid is present
     */
    public LinearDensity getClothoidEndCurvature()
    {
        if (hasShapeNode() && this.designLine instanceof Clothoid2d)
        {
            return LinearDensity.ofSI(((Clothoid2d) this.designLine).getEndCurvature());
        }
        return null;
    }

    /**
     * Returns the length from the clothoid. Connectors have no clothoid shape and return {@code null}.
     * @return length from the clothoid, or {@code null} when no clothoid is present
     */
    public Length getClothoidLength()
    {
        if (hasShapeNode() && this.designLine instanceof Clothoid2d)
        {
            return Length.ofSI(((Clothoid2d) this.designLine).getLength());
        }
        return null;
    }

    /**
     * Returns the A value from the clothoid. Connectors have no clothoid shape and return {@code null}.
     * @return A value from the clothoid, or {@code null} when no clothoid is present
     */
    public Length getClothoidA()
    {
        if (hasShapeNode() && this.designLine instanceof Clothoid2d)
        {
            return Length.ofSI(((Clothoid2d) this.designLine).getA());
        }
        return null;
    }

    /**
     * Returns whether the shape was applied as a Clothoid, an Arc, or as a Straight, depending on start and end position and
     * direction. Connectors have no Clothoid shape and return {@code null}.
     * @return "Clothoid", "Arc" or "Straight", or {@code null} when no Clothoid shape is present
     */
    public String getClothoidAppliedShape()
    {
        if (hasShapeNode() && this.designLine instanceof Clothoid2d)
        {
            return ((Clothoid2d) this.designLine).getAppliedShape();
        }
        return null;
    }

    // MapAligner interaction

    /**
     * Returns whether this LinkData has a Link shape node. Connectors do not have shape child nodes.
     * @return whether a Link shape node is available
     */
    public boolean hasShapeNode()
    {
        return !this.isConnector && this.shapeListener.shapeNode != null;
    }

    /**
     * Returns shape node.
     * @return shape node
     */
    public XsdTreeNode getShapeNode()
    {
        return this.shapeListener.shapeNode;
    }

    /**
     * Returns start node.
     * @return start node
     */
    public XsdTreeNode getStartNode()
    {
        return this.nodeStart;
    }

    /**
     * Returns end node.
     * @return end node
     */
    public XsdTreeNode getEndNode()
    {
        return this.nodeEnd;
    }

    /**
     * Returns start point.
     * @return start point
     */
    public Point2d getStartPoint()
    {
        return this.pointStart;
    }

    /**
     * Returns end point.
     * @return end point
     */
    public Point2d getEndPoint()
    {
        return this.pointEnd;
    }

    /**
     * Returns start direction.
     * @return start direction
     */
    public Direction getStartDirection()
    {
        return this.directionStart;
    }

    /**
     * Returns end direction.
     * @return end direction
     */
    public Direction getEndDirection()
    {
        return this.directionEnd;
    }

    /**
     * Returns start offset.
     * @return start offset
     */
    public Length getStartOffset()
    {
        return this.offsetStart;
    }

    /**
     * Returns end offset.
     * @return end offset
     */
    public Length getEndOffset()
    {
        return this.offsetEnd;
    }

    @Override
    public String toString()
    {
        return (this.isConnector ? "Connector " : "Link ") + this.id;
    }

    /**
     * Listener to events that affect the shape. This class can also deliver the resulting line.
     * @author Wouter Schakel
     */
    private final class ShapeListener implements EventListener
    {
        /** Threshold to consider input to be a trivial straight or circle arc. The value is 1/10th of a degree. */
        private static final double ANGLE_TOLERANCE = 2.0 * Math.PI / 3600.0;

        /** Node of the shape. */
        private XsdTreeNode shapeNode;

        /** Bezier shape. */
        private Double shape;

        /** Bezier weighted or not. */
        private Boolean weighted;

        /** Clothoid start curvature. */
        private LinearDensity startCurvature;

        /** Clothoid end curvature. */
        private LinearDensity endCurvature;

        /** Clothoid length. */
        private Length length;

        /** Clothoid a-value. */
        private Length a;

        /** Arc radius. */
        private Length radius;

        /** Arc direction. */
        private ArcDirection direction;

        /** Polyline coordinates. */
        private NavigableMap<XsdTreeNode, Point2d> coordinates = new TreeMap<>(new Comparator<>()
        {
            @Override
            public int compare(final XsdTreeNode o1, final XsdTreeNode o2)
            {
                List<XsdTreeNode> list = ShapeListener.this.shapeNode.getChildren();
                return Integer.compare(list.indexOf(o1), list.indexOf(o2));
            }
        });

        @Override
        public void notify(final Event event)
        {
            if (event.getType().equals(XsdTreeNode.OPTION_CHANGED))
            {
                XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[1];
                if (node.getParent().equals(this.shapeNode))
                {
                    // clothoid option changed
                    for (XsdTreeNode option : node.getChildren())
                    {
                        option.addListener(this, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
                    }
                    // later as values may not be loaded yet during loading
                    laterIfNotDestroyed(() -> updateShape());
                }
                else if (!this.shapeNode.getNodeName().equals("xsd:choice"))
                {
                    // shape node changed
                    if (this.shapeNode != null)
                    {
                        if (this.shapeNode.getChildCount() > 0)
                        {
                            this.shapeNode.getChild(0).removeListener(this, XsdTreeNode.OPTION_CHANGED);
                        }
                        this.shapeNode.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
                    }
                    this.shapeNode = node;
                    this.shapeNode.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
                    if (this.shapeNode.getNodeName().equals(PolyLine.class.getSimpleName()))
                    {
                        for (XsdTreeNode option : this.shapeNode.getChildren())
                        {
                            option.addListener(this, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
                            option.addListener(this, XsdTreeNode.MOVED, ReferenceType.WEAK);
                        }
                    }
                    else if (this.shapeNode.getNodeName().equals(Clothoid.class.getSimpleName()))
                    {
                        // later as flattener may not be loaded yet during loading
                        laterIfNotDestroyed(() ->
                        {
                            if (!this.shapeNode.isActive() || this.shapeNode.getChildCount() < 1)
                            {
                                Logger.ots().info("Not listening to Clothoid choice");
                                return;
                            }
                            this.shapeNode.getChild(0).addListener(this, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);
                            for (XsdTreeNode option : this.shapeNode.getChild(0).getChildren())
                            {
                                option.addListener(this, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
                            }
                        });
                    }
                    if (this.shapeNode.getNodeName().equals(Clothoid.class.getSimpleName())
                            || this.shapeNode.getNodeName().equals(Arc.class.getSimpleName())
                            || this.shapeNode.getNodeName().equals(Bezier.class.getSimpleName()))
                    {
                        // later as flattener may not be loaded yet during loading
                        laterIfNotDestroyed(() -> setFlattenerListener());
                    }
                    // later as values/coordinates may not be loaded yet during loading
                    laterIfNotDestroyed(() -> updateShape());
                }
                setDirtyAndCheckValidity();
            }
            else if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
            {
                setAttribute((String) ((Object[]) event.getContent())[1]);
                setDirtyAndCheckValidity();
            }
            else if (event.getType().equals(XsdTreeNode.VALUE_CHANGED))
            {
                // clothoid option specification or polyline coordinate
                Object[] content = (Object[]) event.getContent();
                XsdTreeNode node = (XsdTreeNode) content[0];
                try
                {
                    switch (node.getNodeName())
                    {
                        case "Coordinate":
                            this.coordinates.put(node,
                                    Adapters.parseOrNull(getEval(), node.getValueOrDefault(), Point2d.class));
                            break;
                        case "StartCurvature":
                            this.startCurvature =
                                    Adapters.parseOrNull(getEval(), node.getValueOrDefault(), LinearDensity.class);
                            break;
                        case "EndCurvature":
                            this.endCurvature = Adapters.parseOrNull(getEval(), node.getValueOrDefault(), LinearDensity.class);
                            break;
                        case "Length":
                            this.length = Adapters.parseOrNull(getEval(), node.getValueOrDefault(), Length.class);
                            break;
                        case "A":
                            this.a = Adapters.parseOrNull(getEval(), node.getValueOrDefault(), Length.class);
                            break;
                        default:
                            Logger.ots().warn(node.getNodeName()
                                    + " is unknown node name of which the value is assumed to affect link center line.");
                    }
                }
                catch (Exception ex)
                {
                    // leave line as is, new value is not a valid value
                    return;
                }
                setDirtyAndCheckValidity();
            }
            else if (event.getType().equals(XsdTreeNode.MOVED))
            {
                // order of coordinates changed
                updateShape();
                setDirtyAndCheckValidity();
            }
            else if (event.getType().equals(XsdTreeNode.ACTIVATION_CHANGED))
            {
                // flattener node
                boolean activated = (boolean) ((Object[]) event.getContent())[1];
                if (activated)
                {
                    setFlattenerListener();
                }
                else
                {
                    if (MapLinkData.this.flattenerListener != null)
                    {
                        MapLinkData.this.flattenerListener.destroy();
                    }
                    MapLinkData.this.flattenerListener = null;
                }
                setDirtyAndCheckValidity();
            }
        }

        /**
         * Sets the flattener listener in the link.
         */
        private void setFlattenerListener()
        {
            if (MapLinkData.this.flattenerListener != null)
            {
                MapLinkData.this.flattenerListener.destroy();
            }
            // Bezier (0), Clothoid (1), Arc (0), i.e. last child
            MapLinkData.this.flattenerListener =
                    new FlattenerListener(this.shapeNode.getChild(this.shapeNode.getChildCount() - 1), () -> getEval());
            MapLinkData.this.flattenerListener.addListener(MapLinkData.this, ChangeListener.CHANGE_EVENT, ReferenceType.WEAK);
            this.shapeNode.getChild(0).addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        }

        /**
         * Update the line, clearing all fields, and setting any already available attributes (as the shape node was previously
         * selected and edited).
         */
        private void updateShape()
        {
            this.shape = null;
            this.weighted = null;
            this.startCurvature = null;
            this.endCurvature = null;
            this.length = null;
            this.a = null;
            this.radius = null;
            this.direction = null;
            this.coordinates.clear();
            if (this.shapeNode == null)
            {
                return;
            }
            switch (this.shapeNode.getNodeName())
            {
                case "Straight":
                    setDirtyAndCheckValidity();
                    break;
                case "PolyLine":
                    for (XsdTreeNode child : this.shapeNode.getChildren())
                    {
                        try
                        {
                            this.coordinates.put(child,
                                    Adapters.parseOrNull(getEval(), child.getValueOrDefault(), Point2d.class));
                        }
                        catch (Exception ex)
                        {
                            Adapters.parseOrNull(getEval(), child.getValueOrDefault(), Point2d.class);
                            throw new OtsRuntimeException(
                                    "Expression adapter could not unmarshal value for polyline coordinate.");
                        }
                    }
                    setDirtyAndCheckValidity();
                    break;
                case "Bezier":
                    setAttribute("Shape");
                    setAttribute("Weighted");
                    setDirtyAndCheckValidity();
                    setFlattenerListener();
                    break;
                case "Clothoid":
                    if (this.shapeNode.getChildCount() > 0 && this.shapeNode.getChild(0).getChildCount() > 0)
                    {
                        // child is an xsd:sequence, take children from that
                        for (int childIndex = 0; childIndex < this.shapeNode.getChild(0).getChildCount(); childIndex++)
                        {
                            XsdTreeNode child = this.shapeNode.getChild(0).getChild(childIndex);
                            try
                            {
                                switch (child.getNodeName())
                                {
                                    case "StartCurvature":
                                        this.startCurvature =
                                                Adapters.parseOrNull(getEval(), child.getValueOrDefault(), LinearDensity.class);
                                        break;
                                    case "EndCurvature":
                                        this.endCurvature =
                                                Adapters.parseOrNull(getEval(), child.getValueOrDefault(), LinearDensity.class);
                                        break;
                                    case "Length":
                                        this.length = Adapters.parseOrNull(getEval(), child.getValueOrDefault(), Length.class);
                                        break;
                                    case "A":
                                        this.a = Adapters.parseOrNull(getEval(), child.getValueOrDefault(), Length.class);
                                        break;
                                    default:
                                        throw new OtsRuntimeException(
                                                "Clothoid child " + child.getNodeName() + " not supported.");
                                }
                            }
                            catch (Exception ex)
                            {
                                throw new OtsRuntimeException("Expression adapter could not unmarshal value for Clothoid child "
                                        + child.getNodeName());
                            }
                        }
                    }
                    setDirtyAndCheckValidity();
                    setFlattenerListener();
                    break;
                case "Arc":
                    setAttribute("Radius");
                    setAttribute("Direction");
                    setDirtyAndCheckValidity();
                    setFlattenerListener();
                    break;
                case "xsd:choice":
                    // inactive node, will be invalid
                    setDirtyAndCheckValidity();
                    break;
                default:
                    throw new OtsRuntimeException(
                            "Drawing of shape node " + this.shapeNode.getNodeName() + " is not supported.");
            }
        }

        /**
         * Set the given attribute from the shape node.
         * @param attribute attribute name.
         */
        private void setAttribute(final String attribute)
        {
            if (!this.shapeNode.reportInvalidAttributeValue(this.shapeNode.getAttributeIndexByName(attribute)).isEmpty())
            {
                // invalid value, do nothing
                return;
            }
            switch (attribute)
            {
                case "Shape":
                    this.shape = Adapters.parseOrNull(getEval(), this.shapeNode.getAttributeValueOrDefault(attribute),
                            Adapters.DOUBLE_POSITIVE_ADAPTER);
                    break;
                case "Weighted":
                    this.weighted = Adapters.parseOrNull(getEval(), this.shapeNode.getAttributeValueOrDefault(attribute),
                            Boolean.class);
                    break;
                case "Length":
                    this.length =
                            Adapters.parseOrNull(getEval(), this.shapeNode.getAttributeValueOrDefault(attribute), Length.class);
                    break;
                case "Radius":
                    this.radius =
                            Adapters.parseOrNull(getEval(), this.shapeNode.getAttributeValueOrDefault(attribute), Length.class);
                    break;
                case "Direction":
                    this.direction = Adapters.parseOrNull(getEval(), this.shapeNode.getAttributeValueOrDefault(attribute),
                            ArcDirection.class);
                    break;
                default:
                    // an attribute was changed that does not change the shape
            }
        }

        /**
         * Returns the continuous line.
         * @param fromPoint possibly offset start point.
         * @param toPoint possibly offset end point.
         * @return line from the shape and attributes.
         */
        public OffsetCurve2d getContiuousLine(final DirectedPoint2d fromPoint, final DirectedPoint2d toPoint)
        {
            switch (this.shapeNode.getNodeName())
            {
                case "Straight":
                    return new Straight2d(fromPoint, fromPoint.distance(toPoint));
                case "PolyLine":
                    List<Point2d> list = new ArrayList<>();
                    list.add(fromPoint);
                    for (Point2d value : this.coordinates.values())
                    {
                        list.add(value);
                    }
                    list.add(toPoint);
                    if (list.contains(null))
                    {
                        if (list.size() == 3)
                        {
                            // one coordinate tree node with empty or invalid coordinate value, allow map to show 1 draggable
                            list.remove(1);
                        }
                        else
                        {
                            return null;
                        }
                    }
                    PolyLine2d line = new PolyLine2d(0.0, list);
                    return new PolyLineCurve2d(line, line.getLocationFraction(0.0).dirZ, line.getLocationFraction(1.0).dirZ);
                case "Bezier":
                    // similar to how Clothoid fixes this
                    double[] phiBezier = phi(fromPoint, toPoint);
                    if (Math.abs(phiBezier[0]) < ANGLE_TOLERANCE && Math.abs(phiBezier[1]) < ANGLE_TOLERANCE)
                    {
                        return new Straight2d(fromPoint, fromPoint.distance(toPoint));
                    }
                    return new BezierCubic2d(new Ray2d(fromPoint), new Ray2d(toPoint), this.shape == null ? 1.0 : this.shape,
                            this.weighted == null ? false : this.weighted);
                case "Clothoid":
                    if (this.shapeNode.getChildCount() == 0 || this.shapeNode.getChild(0).getChildCount() == 0
                            || this.shapeNode.getChild(0).getChild(0).getNodeName().equals("Interpolated"))
                    {
                        return new Clothoid2d(fromPoint, toPoint);
                    }
                    else if (this.shapeNode.getChild(0).getChild(0).getNodeName().equals("Length"))
                    {
                        if (this.length == null || this.startCurvature == null || this.endCurvature == null
                                || this.startCurvature.si == this.endCurvature.si)
                        {
                            return null;
                        }
                        return Clothoid2d.withLength(fromPoint, this.length.si, this.startCurvature.si, this.endCurvature.si);
                    }
                    else
                    {
                        if (this.a == null || this.startCurvature == null || this.endCurvature == null
                                || this.startCurvature.si == this.endCurvature.si)
                        {
                            return null;
                        }
                        return new Clothoid2d(fromPoint, this.a.si, this.startCurvature.si, this.endCurvature.si);
                    }
                case "Arc":
                    if (this.direction == null || this.radius == null)
                    {
                        return null;
                    }
                    boolean left = this.direction.equals(ArcDirection.LEFT);
                    double endHeading = toPoint.dirZ;
                    while (left && endHeading < fromPoint.dirZ)
                    {
                        endHeading += 2.0 * Math.PI;
                    }
                    while (!left && endHeading > fromPoint.dirZ)
                    {
                        endHeading -= 2.0 * Math.PI;
                    }
                    Angle angle = Angle.ofSI(
                            AngleUtil.normalizeAroundPi(left ? endHeading - fromPoint.dirZ : fromPoint.dirZ - endHeading));
                    if (Math.abs(angle.si) < ANGLE_TOLERANCE)
                    {
                        return new Straight2d(fromPoint, fromPoint.distance(toPoint));
                    }
                    return new Arc2d(fromPoint, this.radius.si, left, angle.si);
                default:
                    throw new OtsRuntimeException(
                            "Drawing of shape node " + this.shapeNode.getNodeName() + " is not supported.");
            }
        }

        /**
         * Compute angles between end point directions and the line connecting them.
         * @param fromPoint from point
         * @param toPoint to point
         * @return angles between end point directions and the line connecting them
         */
        private static double[] phi(final DirectedPoint2d fromPoint, final DirectedPoint2d toPoint)
        {
            // similar to how Clothoid fixes this
            double dx = toPoint.x - fromPoint.x;
            double dy = toPoint.y - fromPoint.y;
            double d = Math.atan2(dy, dx); // angle of line through start and end points
            double phi1 = AngleUtil.normalizeAroundZero(d - fromPoint.dirZ);
            double phi2 = AngleUtil.normalizeAroundZero(toPoint.dirZ - d);
            return new double[] {phi1, phi2};
        }
    }

}

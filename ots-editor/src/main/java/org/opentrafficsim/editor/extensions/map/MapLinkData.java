package org.opentrafficsim.editor.extensions.map;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djutils.draw.DrawRuntimeException;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventListenerMap;
import org.djutils.event.EventProducer;
import org.djutils.event.EventType;
import org.djutils.event.reference.ReferenceType;
import org.djutils.exceptions.Try;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.ContinuousArc;
import org.opentrafficsim.core.geometry.ContinuousBezierCubic;
import org.opentrafficsim.core.geometry.ContinuousClothoid;
import org.opentrafficsim.core.geometry.ContinuousLine;
import org.opentrafficsim.core.geometry.ContinuousPolyLine;
import org.opentrafficsim.core.geometry.ContinuousStraight;
import org.opentrafficsim.core.geometry.Flattener;
import org.opentrafficsim.core.geometry.OtsGeometryUtil;
import org.opentrafficsim.draw.ClickableBounds;
import org.opentrafficsim.draw.network.LinkAnimation.LinkData;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.PriorityAnimation;
import org.opentrafficsim.draw.road.StripeAnimation;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.Adapters;
import org.opentrafficsim.road.network.factory.xml.utils.RoadLayoutOffsets.CseData;
import org.opentrafficsim.road.network.lane.CrossSectionSlice;
import org.opentrafficsim.road.network.lane.LaneGeometryUtil;
import org.opentrafficsim.road.network.lane.SliceInfo;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Type;
import org.opentrafficsim.xml.bindings.ExpressionAdapter;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType.ArcDirection;

import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;

/**
 * LinkData for the editor Map. This class will also listen to any changes that may affect the link shape, maintain the drawn
 * layout, and maintain the priority animation.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MapLinkData extends MapData implements LinkData, EventListener, EventProducer
{

    /** */
    private static final long serialVersionUID = 20231003L;

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

    /** From point. */
    private Point2d from;

    /** To point. */
    private Point2d to;

    /** Continuous design line. */
    private ContinuousLine designLine = null;

    /** Flattened design line. */
    private PolyLine2d flattenedDesignLine = null;

    /** Bounds around the flattened design line. */
    private Bounds2d bounds;

    /** Location. */
    private OrientedPoint2d location;

    /** Contour. */
    private Polygon2d contour;

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

    /** Priority animation. */
    private PriorityAnimation priorityAnimation;

    /**
     * Constructor.
     * @param map map.
     * @param linkNode node Ots.Network.Link.
     * @param editor editor.
     */
    public MapLinkData(final EditorMap map, final XsdTreeNode linkNode, final OtsEditor editor)
    {
        super(map, linkNode, editor);
        linkNode.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
        linkNode.getChild(0).addListener(this.shapeListener, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);
        linkNode.getChild(1).addListener(this, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);
        this.shapeListener.shapeNode = linkNode.getChild(0);

        // as RoadLayout is the default, a setOption() never triggers this (for DefinedRoadLayout this is not required)
        SwingUtilities.invokeLater(() ->
        {
            XsdTreeNode layout = linkNode.getChild(1);
            if (layout.getOption().equals(layout))
            {
                try
                {
                    notify(new Event(XsdTreeNode.OPTION_CHANGED, new Object[] {layout, layout, layout}));
                }
                catch (RemoteException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        // for when node is duplicated, set data immediately if (getNode().isActive())
        if (getNode().isActive())
        {
            SwingUtilities.invokeLater(() ->
            {
                try
                {
                    // this is for when delete is undone, as some children are recovered later, including the shape node
                    this.shapeListener.shapeNode = linkNode.getChild(0);
                    linkNode.getChild(0).addListener(this.shapeListener, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);

                    notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Id", null}));
                    this.nodeStart = replaceNode(this.nodeStart, linkNode.getCoupledKeyrefNodeAttribute("NodeStart"));
                    this.nodeEnd = replaceNode(this.nodeEnd, linkNode.getCoupledKeyrefNodeAttribute("NodeEnd"));
                    notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "OffsetStart", null}));
                    notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "OffsetEnd", null}));
                    XsdTreeNode shape = linkNode.getChild(0);
                    this.shapeListener.notify(new Event(XsdTreeNode.OPTION_CHANGED, new Object[] {shape, shape, shape}));
                }
                catch (RemoteException e)
                {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void destroy()
    {
        super.destroy();
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
            this.priorityAnimation.destroy(getMap().getContextualized());
        }
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds2d getBounds()
    {
        return this.bounds;
    }

    /** {@inheritDoc} */
    @Override
    public Polygon2d getContour()
    {
        return this.contour;
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConnector()
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getDesignLine()
    {
        return this.flattenedDesignLine;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
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
                if (this.definedRoadLayoutNode != null)
                {
                    this.definedRoadLayoutNode.removeListener(this, XsdTreeNode.VALUE_CHANGED);
                }
                if (selected.getNodeName().equals("RoadLayout"))
                {
                    this.roadLayoutNode = selected;
                    this.definedRoadLayoutNode = null;
                    this.roadLayoutListener = new RoadLayoutListener(selected, () -> getEval());
                    this.roadLayoutListener.addListener(this, ChangeListener.CHANGE_EVENT, ReferenceType.WEAK);
                }
                else
                {
                    this.definedRoadLayoutNode = selected.getChild(0);
                    this.definedRoadLayoutNode.addListener(this, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
                    this.roadLayoutNode = this.definedRoadLayoutNode.getCoupledKeyrefNodeValue();
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
                this.flattenerListener = new FlattenerListener(selected, () -> getEval());
            }
            buildLayout();
            return;
        }
        else if (event.getType().equals(XsdTreeNode.VALUE_CHANGED))
        {
            // defined road layout value changed
            this.roadLayoutNode = this.definedRoadLayoutNode.getCoupledKeyrefNodeValue();
            buildLayout();
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
                    this.roadLayoutNode = this.definedRoadLayoutNode.getCoupledKeyrefNodeValue();
                }
                if (node.equals(this.roadLayoutNode) && node.reportInvalidId() == null)
                {
                    buildLayout();
                }
            }
            else
            {
                // change in flattener
                buildDesignLine();
            }
            return;
        }

        // any attribute of the link node, or of either of the connected nodes
        Object[] content = (Object[]) event.getContent();
        XsdTreeNode node = (XsdTreeNode) content[0];
        String attribute = (String) content[1];
        String value = node.getAttributeValue(attribute);

        if ("Id".equals(attribute))
        {
            this.id = value == null ? "" : value;
            return;
        }
        else if ("NodeStart".equals(attribute))
        {
            this.nodeStart = replaceNode(this.nodeStart, getNode().getCoupledKeyrefNodeAttribute("NodeStart"));
        }
        else if ("NodeEnd".equals(attribute))
        {
            this.nodeEnd = replaceNode(this.nodeEnd, getNode().getCoupledKeyrefNodeAttribute("NodeEnd"));
        }
        else if ("OffsetStart".equals(attribute))
        {
            setValue((v) -> this.offsetStart = v, Adapters.get(Length.class), getNode(), attribute);
        }
        else if ("OffsetEnd".equals(attribute))
        {
            setValue((v) -> this.offsetEnd = v, Adapters.get(Length.class), getNode(), attribute);
        }
        else if ("Coordinate".equals(attribute))
        {
            // this pertains to either of the nodes, to which this class also listens
        }
        else if ("Direction".equals(attribute))
        {
            // this pertains to either of the nodes, to which this class also listens
        }
        else
        {
            // other attribute, not important
            return;
        }
        buildDesignLine();
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
                XsdTreeNode node = getInputNode(newNode);
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
                XsdTreeNode node = getInputNode(newNode);
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
    private XsdTreeNode getInputNode(final XsdTreeNode inputParameter)
    {
        String inputId = inputParameter.getId();
        String nodeId = (String) getEval().evaluate(inputId.substring(1, inputId.length() - 1));
        XsdTreeNode ots = inputParameter.getRoot();
        for (XsdTreeNode child : ots.getChildren())
        {
            if (child.getPathString().equals(XsdPaths.NETWORK))
            {
                for (XsdTreeNode networkElement : child.getChildren())
                {
                    if (networkElement.isType("Node") && networkElement.getId().equals(nodeId))
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
        if (this.shapeListener.shapeNode.equals(node.getParent()))
        {
            this.shapeListener.coordinates.put(node, orNull(node.getValue(), Adapters.get(Point2d.class)));
            buildDesignLine();
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
                buildDesignLine();
                node.removeListener(this.shapeListener, XsdTreeNode.VALUE_CHANGED);
                node.removeListener(this.shapeListener, XsdTreeNode.MOVED);
                return;
            }
        }
    }

    /**
     * Builds the design line.
     */
    private void buildDesignLine()
    {
        if (this.nodeStart == null || this.nodeEnd == null || this.nodeStart.equals(this.nodeEnd))
        {
            setInvalid();
            return;
        }
        setValue((v) -> this.from = v, Adapters.get(Point2d.class), this.nodeStart, "Coordinate");
        setValue((v) -> this.to = v, Adapters.get(Point2d.class), this.nodeEnd, "Coordinate");
        if (this.from == null || this.to == null)
        {
            setInvalid();
            return;
        }
        setValue((v) -> this.directionStart = v, Adapters.get(Direction.class), this.nodeStart, "Direction");
        double dirStart = this.directionStart == null ? 0.0 : this.directionStart.si;
        OrientedPoint2d fromPoint = new OrientedPoint2d(this.from, dirStart);
        setValue((v) -> this.directionEnd = v, Adapters.get(Direction.class), this.nodeEnd, "Direction");
        double dirEnd = this.directionEnd == null ? 0.0 : this.directionEnd.si;
        OrientedPoint2d toPoint = new OrientedPoint2d(this.to, dirEnd);
        if (this.offsetStart != null)
        {
            fromPoint = OtsGeometryUtil.offsetPoint(fromPoint, this.offsetStart.si);
        }
        if (this.offsetEnd != null)
        {
            toPoint = OtsGeometryUtil.offsetPoint(toPoint, this.offsetEnd.si);
        }
        this.designLine = this.shapeListener.getContiuousLine(fromPoint, toPoint);
        if (this.designLine == null)
        {
            return;
        }
        this.flattenedDesignLine = this.designLine.flatten(getFlattener());
        Ray2d ray = this.flattenedDesignLine.getLocationFractionExtended(0.5);
        this.location = new OrientedPoint2d(ray.x, ray.y, ray.phi);
        this.contour =
                new Polygon2d(PolyLine2d.concatenate(this.flattenedDesignLine, this.flattenedDesignLine.reverse()).getPoints());
        this.bounds = ClickableBounds.get(OtsLocatable.asBounds(this));
        if (this.priorityAnimation != null)
        {
            getMap().removeAnimation(this.priorityAnimation);
        }
        this.priorityAnimation = new PriorityAnimation(new MapPriorityData(this), getMap().getContextualized());
        buildLayout();
        setValid();
    }

    /**
     * Returns the flattener to use, which is either a flattener defined at link level, or at network level.
     * @return Flattener, flattener to use.
     */
    private Flattener getFlattener()
    {
        if (this.flattenerListener != null)
        {
            Flattener flattener = this.flattenerListener.getData();
            if (flattener != null)
            {
                return flattener; // otherwise not valid, return network level flattener
            }
        }
        return getMap().getNetworkFlattener();
    }

    /**
     * Builds all animation objects for stripes, lanes, shoulders, no-traffic lanes, and their center lines and id's.
     */
    private void buildLayout()
    {
        if (this.designLine == null)
        {
            return;
        }
        for (Renderable2d<?> renderable : this.crossSectionElements)
        {
            getMap().removeAnimation(renderable);
        }
        this.crossSectionElements.clear();
        this.laneData.clear();
        if (this.roadLayoutNode != null)
        {
            java.util.Map<XsdTreeNode, CseData> cseDataMap = this.roadLayoutListener != null ? this.roadLayoutListener.getData()
                    : getMap().getRoadLayoutListener(this.roadLayoutNode).getData();
            for (Entry<XsdTreeNode, CseData> entry : cseDataMap.entrySet())
            {
                XsdTreeNode node = entry.getKey();
                CseData cseData = entry.getValue();
                try
                {
                    List<CrossSectionSlice> slices;
                    Type type = null;
                    Length width = null;
                    if (node.getNodeName().equals("Stripe"))
                    {
                        type = Adapters.get(Stripe.Type.class).unmarshal(node.getAttributeValue("Type")).get(getEval());
                        width = node.getChild(1).isActive() // child 1 is DrawingWidth
                                ? Adapters.get(Length.class).unmarshal(node.getChild(1).getValue()).get(getEval())
                                : type.defaultWidth();
                        slices = LaneGeometryUtil.getSlices(this.designLine, cseData.centerOffsetStart, cseData.centerOffsetEnd,
                                width, width);
                    }
                    else
                    {
                        slices = LaneGeometryUtil.getSlices(this.designLine, cseData.centerOffsetStart, cseData.centerOffsetEnd,
                                cseData.widthStart, cseData.widthEnd);
                    }
                    SliceInfo sliceInfo = new SliceInfo(slices, Length.instantiateSI(this.designLine.getLength()));

                    Flattener flattener = getFlattener();
                    PolyLine2d centerLine = this.designLine
                            .flattenOffset(LaneGeometryUtil.getCenterOffsets(this.designLine, slices), flattener);
                    PolyLine2d leftEdge = this.designLine
                            .flattenOffset(LaneGeometryUtil.getLeftEdgeOffsets(this.designLine, slices), flattener);
                    PolyLine2d rightEdge = this.designLine
                            .flattenOffset(LaneGeometryUtil.getRightEdgeOffsets(this.designLine, slices), flattener);
                    Polygon2d contour = LaneGeometryUtil.getContour(leftEdge, rightEdge);

                    if (node.getNodeName().equals("Stripe"))
                    {
                        StripeAnimation stripe =
                                new StripeAnimation(
                                        new MapStripeData(StripeData.Type.valueOf(type.name()), width,
                                                slices.get(0).getOffset(), getNode(), centerLine, contour, sliceInfo),
                                        getMap().getContextualized());
                        this.crossSectionElements.add(stripe);
                    }
                    else
                    {
                        if (node.getNodeName().equals("Lane"))
                        {
                            MapLaneData laneData = new MapLaneData(node.getId(), getNode(), centerLine, contour, sliceInfo);
                            LaneAnimation lane =
                                    new LaneAnimation(laneData, getMap().getContextualized(), Color.GRAY.brighter());
                            this.crossSectionElements.add(lane);
                            this.laneData.put(node.getId(), laneData);
                        }
                        else if (node.getNodeName().equals("Shoulder"))
                        {
                            CrossSectionElementAnimation<?> shoulder = new CrossSectionElementAnimation<>(
                                    new MapShoulderData(slices.get(0).getOffset(), getNode(), centerLine, contour, sliceInfo),
                                    getMap().getContextualized(), Color.DARK_GRAY);
                            this.crossSectionElements.add(shoulder);
                        }
                        else if (node.getNodeName().equals("NoTrafficLane"))
                        {
                            CrossSectionElementAnimation<?> noTrafficLane = new CrossSectionElementAnimation<>(
                                    new MapCrossSectionData(getNode(), centerLine, contour, sliceInfo),
                                    getMap().getContextualized(), Color.DARK_GRAY);
                            this.crossSectionElements.add(noTrafficLane);
                        }
                        else
                        {
                            throw new RuntimeException(
                                    "Element " + node.getNodeName() + " is not a supported cross-section element.");
                        }
                    }
                }
                catch (RemoteException | NamingException exception)
                {
                    throw new RuntimeException("Exception while building layout.");
                }
            }
        }
        Try.execute(() -> this.fireEvent(LAYOUT_REBUILT, this), "Unable to fire LAYOUT event.");
    }

    /** {@inheritDoc} */
    @Override
    public EventListenerMap getEventListenerMap() throws RemoteException
    {
        return this.eventListenerMap;
    }

    /** {@inheritDoc} */
    @Override
    public void evalChanged()
    {
        if (getNode().isActive())
        {
            this.id = getNode().getId() == null ? "" : getNode().getId();
            this.nodeStart = replaceNode(this.nodeStart, getNode().getCoupledKeyrefNodeAttribute("NodeStart"));
            this.nodeEnd = replaceNode(this.nodeEnd, getNode().getCoupledKeyrefNodeAttribute("NodeEnd"));
            setValue((v) -> this.offsetStart = v, Adapters.get(Length.class), getNode(), "OffsetStart");
            setValue((v) -> this.offsetEnd = v, Adapters.get(Length.class), getNode(), "OffsetEnd");
            this.shapeListener.update();
            buildDesignLine();
        }
    }

    /**
     * Notification from the Map that a node (Ots.Network.Node) id was changed.
     * @param node node.
     */
    public void notifyNodeIdChanged(final XsdTreeNode node)
    {
        this.nodeStart = replaceNode(this.nodeStart, getNode().getCoupledKeyrefNodeAttribute("NodeStart"));
        this.nodeEnd = replaceNode(this.nodeEnd, getNode().getCoupledKeyrefNodeAttribute("NodeEnd"));
        buildDesignLine();
    }

    /**
     * Returns the value with appropriate adapter, or {@code null} if the value is {@code null}.
     * @param <T> type of the value after unmarshaling.
     * @param value value.
     * @param adapter adapter for values of type T.
     * @return unmarshaled value.
     */
    private <T> T orNull(final String value, final ExpressionAdapter<T, ?> adapter)
    {
        try
        {
            return value == null ? null : adapter.unmarshal(value).get(getEval());
        }
        catch (IllegalArgumentException ex)
        {
            // illegal value for adapter
            return null;
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
     * Listener to events that affect the shape. This class can also deliver the resulting line.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private class ShapeListener implements EventListener
    {
        /** */
        private static final long serialVersionUID = 20231020L;

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
        public SortedMap<XsdTreeNode, Point2d> coordinates = new TreeMap<>(new Comparator<>()
        {
            /** {@inheritDoc} */
            @Override
            public int compare(final XsdTreeNode o1, final XsdTreeNode o2)
            {
                List<XsdTreeNode> list = ShapeListener.this.shapeNode.getChildren();
                return Integer.compare(list.indexOf(o1), list.indexOf(o2));
            }
        });

        /** {@inheritDoc} */
        @Override
        public void notify(final Event event) throws RemoteException
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
                    SwingUtilities.invokeLater(() -> update());
                }
                else
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
                    if (this.shapeNode.getNodeName().equals("Polyline"))
                    {
                        for (XsdTreeNode option : this.shapeNode.getChildren())
                        {
                            option.addListener(this, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
                            option.addListener(this, XsdTreeNode.MOVED, ReferenceType.WEAK);
                        }
                    }
                    else if (this.shapeNode.getNodeName().equals("Clothoid"))
                    {
                        this.shapeNode.getChild(0).addListener(this, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);
                        for (XsdTreeNode option : this.shapeNode.getChild(0).getChildren())
                        {
                            option.addListener(this, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
                        }
                    }
                    if (this.shapeNode.getNodeName().equals("Clothoid") || this.shapeNode.getNodeName().equals("Arc")
                            || this.shapeNode.getNodeName().equals("Bezier"))
                    {
                        setFlattenerListener();
                    }
                    // later as values/coordinates may not be loaded yet during loading
                    SwingUtilities.invokeLater(() -> update());
                }
                buildDesignLine();
            }
            else if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
            {
                setAttribute((String) ((Object[]) event.getContent())[1]);
                buildDesignLine();
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
                            this.coordinates.put(node, orNull(node.getValue(), Adapters.get(Point2d.class)));
                            break;
                        case "StartCurvature":
                            this.startCurvature = orNull(node.getValue(), Adapters.get(LinearDensity.class));
                            break;
                        case "EndCurvature":
                            this.endCurvature = orNull(node.getValue(), Adapters.get(LinearDensity.class));
                            break;
                        case "Length":
                            this.length = orNull(node.getValue(), Adapters.get(Length.class));
                            break;
                        case "A":
                            this.a = orNull(node.getValue(), Adapters.get(Length.class));
                            break;
                    }
                }
                catch (Exception ex)
                {
                    // leave line as is, new value is not a valid value
                    return;
                }
                buildDesignLine();
            }
            else if (event.getType().equals(XsdTreeNode.MOVED))
            {
                // order of coordinates changed
                update();
                buildDesignLine();
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
                buildDesignLine();
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
            MapLinkData.this.flattenerListener = new FlattenerListener(this.shapeNode.getChild(0), () -> getEval());
            MapLinkData.this.flattenerListener.addListener(MapLinkData.this, ChangeListener.CHANGE_EVENT, ReferenceType.WEAK);
            this.shapeNode.getChild(0).addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        }

        /**
         * Update the line, clearing all fields, and setting any already available attributes (as the shape node was previously
         * selected and edited).
         */
        private void update()
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
            switch (this.shapeNode.getNodeName())
            {
                case "Straight":
                    buildDesignLine();
                    break;
                case "Polyline":
                    for (XsdTreeNode child : this.shapeNode.getChildren())
                    {
                        try
                        {
                            this.coordinates.put(child, orNull(child.getValue(), Adapters.get(Point2d.class)));
                        }
                        catch (Exception ex)
                        {
                            throw new RuntimeException("Expression adapter could not unmarshal value for polyline coordinate.");
                        }
                    }
                    buildDesignLine();
                    break;
                case "Bezier":
                    setAttribute("Shape");
                    setAttribute("Weighted");
                    buildDesignLine();
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
                                        this.startCurvature = orNull(child.getValue(), Adapters.get(LinearDensity.class));
                                        break;
                                    case "EndCurvature":
                                        this.endCurvature = orNull(child.getValue(), Adapters.get(LinearDensity.class));
                                        break;
                                    case "Length":
                                        this.length = orNull(child.getValue(), Adapters.get(Length.class));
                                        break;
                                    case "A":
                                        this.a = orNull(child.getValue(), Adapters.get(Length.class));
                                        break;
                                    default:
                                        throw new RuntimeException("Clothoid child " + child.getNodeName() + " not supported.");
                                }
                            }
                            catch (Exception ex)
                            {
                                throw new RuntimeException("Expression adapter could not unmarshal value for Clothoid child "
                                        + child.getNodeName());
                            }
                        }
                    }
                    buildDesignLine();
                    setFlattenerListener();
                    break;
                case "Arc":
                    setAttribute("Radius");
                    setAttribute("Direction");
                    buildDesignLine();
                    setFlattenerListener();
                    break;
                default:
                    throw new RuntimeException("Drawing of shape node " + this.shapeNode.getNodeName() + " is not supported.");
            }
        }

        /**
         * Set the given attribute from the shape node.
         * @param attribute attribute name.
         */
        private void setAttribute(final String attribute)
        {
            if (this.shapeNode.reportInvalidAttributeValue(this.shapeNode.getAttributeIndexByName(attribute)) != null)
            {
                // invalid value, do nothing
                return;
            }
            switch (attribute)
            {
                case "Shape":
                    this.shape = getOrNull(attribute, Adapters.get(Double.class));
                    break;
                case "Weighted":
                    this.weighted = getOrNull(attribute, Adapters.get(Boolean.class));
                    break;
                case "Length":
                    this.length = getOrNull(attribute, Adapters.get(Length.class));
                    break;
                case "Radius":
                    this.radius = getOrNull(attribute, Adapters.get(Length.class));
                    break;
                case "Direction":
                    this.direction = getOrNull(attribute, Adapters.get(ArcDirection.class));
                    break;
                default:
                    // an attribute was changed that does not change the shape
            }
        }

        /**
         * Returns the attribute value with appropriate adapter, or {@code null} if the attribute is not given.
         * @param <T> type of the attribute value after unmarshaling.
         * @param attribute attribute.
         * @param adapter adapter for values of type T.
         * @return unmarshaled value.
         */
        private <T> T getOrNull(final String attribute, final ExpressionAdapter<T, ?> adapter)
        {
            String value = this.shapeNode.getAttributeValue(attribute);
            return orNull(value, adapter);
        }

        /**
         * Returns the continuous line.
         * @param from possibly offset start point.
         * @param to possibly offset end point.
         * @return line from the shape and attributes.
         */
        public ContinuousLine getContiuousLine(final OrientedPoint2d from, final OrientedPoint2d to)
        {
            try
            {
                switch (this.shapeNode.getNodeName())
                {
                    case "Straight":
                        double length = from.distance(to);
                        return new ContinuousStraight(from, length);
                    case "Polyline":
                        List<Point2d> list = new ArrayList<>();
                        list.add(from);
                        for (Entry<XsdTreeNode, Point2d> entry : this.coordinates.entrySet())
                        {
                            list.add(entry.getValue());
                        }
                        list.add(to);
                        if (list.contains(null))
                        {
                            return null;
                        }
                        return new ContinuousPolyLine(new PolyLine2d(list), from, to);
                    case "Bezier":
                        double shape = this.shape == null ? 1.0 : this.shape;
                        boolean weighted = this.weighted == null ? false : this.weighted;
                        Point2d[] points = Bezier.cubicControlPoints(from, to, shape, weighted);
                        return new ContinuousBezierCubic(points[0], points[1], points[2], points[3]);
                    case "Clothoid":
                        if (this.shapeNode.getChildCount() == 0 || this.shapeNode.getChild(0).getChildCount() == 0
                                || this.shapeNode.getChild(0).getChild(0).getNodeName().equals("Interpolated"))
                        {
                            return new ContinuousClothoid(from, to);
                        }
                        else if (this.shapeNode.getChild(0).getChild(0).getNodeName().equals("Length"))
                        {
                            if (this.length == null || this.startCurvature == null || this.endCurvature == null)
                            {
                                return null;
                            }
                            return ContinuousClothoid.withLength(from, this.length.si, this.startCurvature.si,
                                    this.endCurvature.si);
                        }
                        else
                        {
                            if (this.a == null || this.startCurvature == null || this.endCurvature == null)
                            {
                                return null;
                            }
                            return new ContinuousClothoid(from, this.a.si, this.startCurvature.si, this.endCurvature.si);
                        }
                    case "Arc":
                        if (this.direction == null || this.radius == null)
                        {
                            return null;
                        }
                        boolean left = this.direction.equals(ArcDirection.LEFT);
                        double endHeading = to.dirZ;
                        while (left && endHeading < from.dirZ)
                        {
                            endHeading += 2.0 * Math.PI;
                        }
                        while (!left && endHeading > from.dirZ)
                        {
                            endHeading -= 2.0 * Math.PI;
                        }
                        Angle angle = Angle.instantiateSI(left ? endHeading - from.dirZ : from.dirZ - endHeading);
                        return new ContinuousArc(from, this.radius.si, left, angle);
                    default:
                        throw new RuntimeException(
                                "Drawing of shape node " + this.shapeNode.getNodeName() + " is not supported.");
                }
            }
            catch (DrawRuntimeException exception)
            {
                // Probably a degenerate line as nodes are at the same location
                return null;
            }
        }
    }

    /**
     * Returns the start curvature from the clothoid.
     * @return start curvature from the clothoid.
     */
    public LinearDensity getClothoidStartCurvature()
    {
        if (this.designLine != null && this.designLine instanceof ContinuousClothoid)
        {
            return LinearDensity.instantiateSI(((ContinuousClothoid) this.designLine).getStartCurvature());
        }
        return null;
    }

    /**
     * Returns the end curvature from the clothoid.
     * @return end curvature from the clothoid.
     */
    public LinearDensity getClothoidEndCurvature()
    {
        if (this.designLine != null && this.designLine instanceof ContinuousClothoid)
        {
            return LinearDensity.instantiateSI(((ContinuousClothoid) this.designLine).getEndCurvature());
        }
        return null;
    }

    /**
     * Returns the length from the clothoid.
     * @return length from the clothoid.
     */
    public Length getClothoidLength()
    {
        if (this.designLine != null && this.designLine instanceof ContinuousClothoid)
        {
            return Length.instantiateSI(((ContinuousClothoid) this.designLine).getLength());
        }
        return null;
    }

    /**
     * Returns the A value from the clothoid.
     * @return A value from the clothoid.
     */
    public Length getClothoidA()
    {
        if (this.designLine != null && this.designLine instanceof ContinuousClothoid)
        {
            return Length.instantiateSI(((ContinuousClothoid) this.designLine).getA());
        }
        return null;
    }

    /**
     * Returns whether the shape was applied as a Clothoid, an Arc, or as a Straight, depending on start and end position and
     * direction.
     * @return "Clothoid", "Arc" or "Straight".
     */
    public String getClothoidAppliedShape()
    {
        if (this.designLine != null && this.designLine instanceof ContinuousClothoid)
        {
            return ((ContinuousClothoid) this.designLine).getAppliedShape();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Link " + this.id;
    }

}

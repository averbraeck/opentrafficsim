package org.opentrafficsim.editor.extensions.map;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djutils.draw.DrawRuntimeException;
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.ContinuousArc;
import org.opentrafficsim.core.geometry.ContinuousBezierCubic;
import org.opentrafficsim.core.geometry.ContinuousClothoid;
import org.opentrafficsim.core.geometry.Flattener;
import org.opentrafficsim.core.geometry.OtsGeometryUtil;
import org.opentrafficsim.draw.ClickableBounds;
import org.opentrafficsim.draw.network.LinkAnimation.LinkData;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.factory.xml.parser.ScenarioParser;
import org.opentrafficsim.xml.bindings.ArcDirectionAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.DirectionAdapter;
import org.opentrafficsim.xml.bindings.DoubleAdapter;
import org.opentrafficsim.xml.bindings.ExpressionAdapter;
import org.opentrafficsim.xml.bindings.IntegerAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.LinearDensityAdapter;
import org.opentrafficsim.xml.bindings.Point2dAdapter;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType.ArcDirection;

/**
 * LinkData for the editor Map. This class will also listen to any changes that may affect the link shape.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class MapLinkData extends MapData implements LinkData, EventListener
{

    /** */
    private static final long serialVersionUID = 20231003L;

    /** Point adapter. */
    private final static Point2dAdapter POINT_ADAPTER = new Point2dAdapter();

    /** Direction adapter. */
    private final static DirectionAdapter DIRECTION_ADAPTER = new DirectionAdapter();

    /** Length adapter. */
    private final static LengthAdapter LENGTH_ADAPTER = new LengthAdapter();

    /** Linear density adapter. */
    private final static LinearDensityAdapter LINEAR_DENSITY_ADAPTER = new LinearDensityAdapter();

    /** Arc direction adapter. */
    private final static ArcDirectionAdapter ARC_DIRECTION_ADAPTER = new ArcDirectionAdapter();

    /** Integer adapter. */
    private final static IntegerAdapter INTEGER_ADAPTER = new IntegerAdapter();

    /** Double adapter. */
    private final static DoubleAdapter DOUBLE_ADAPTER = new DoubleAdapter();

    /** Boolean adapter. */
    private final static BooleanAdapter BOOLEAN_ADAPTER = new BooleanAdapter();

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

    /** Design line. */
    private PolyLine2d designLine = null;

    /** Location. */
    private OrientedPoint2d location = new OrientedPoint2d(0.0, 0.0);

    /**
     * Constructor.
     * @param map Map; map.
     * @param linkNode XsdTreeNode; node Ots.Network.Link.
     * @param editor OtsEditor; editor.
     */
    public MapLinkData(final Map map, final XsdTreeNode linkNode, final OtsEditor editor)
    {
        super(map, linkNode, editor);
        linkNode.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
        linkNode.getChild(0).addListener(this.shapeListener, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);
        this.shapeListener.shapeNode = linkNode.getChild(0);
        // for when node is duplicated, set data immediately if (getNode().isActive())
        if (getNode().isActive())
        {
            SwingUtilities.invokeLater(() ->
            {
                try
                {
                    notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Id", null}));
                    this.nodeStart = replaceNode(this.nodeStart, linkNode.getCoupledKeyrefNode("NodeStart"));
                    this.nodeEnd = replaceNode(this.nodeEnd, linkNode.getCoupledKeyrefNode("NodeEnd"));
                    notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "OffsetStart", null}));
                    notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "OffsetEnd", null}));
                    buildDesignLine();
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
    public Bounds<?, ?, ?> getBounds() throws RemoteException
    {
        return ClickableBounds.get(this.designLine.getBounds());
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        return this.location;
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
        return this.designLine;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
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
            this.nodeStart = replaceNode(this.nodeStart, getNode().getCoupledKeyrefNode("NodeStart"));
        }
        else if ("NodeEnd".equals(attribute))
        {
            this.nodeEnd = replaceNode(this.nodeEnd, getNode().getCoupledKeyrefNode("NodeEnd"));
        }
        else if ("OffsetStart".equals(attribute))
        {
            setValue((v) -> this.offsetStart = v, LENGTH_ADAPTER, getNode(), attribute);
        }
        else if ("OffsetEnd".equals(attribute))
        {
            setValue((v) -> this.offsetEnd = v, LENGTH_ADAPTER, getNode(), attribute);
        }
        else if ("Coordinate".equals(attribute))
        {
            // this pertains to either of the nodes, to which this class also listens
            // buildDesignLine();
        }
        else if ("Direction".equals(attribute))
        {
            // this pertains to either of the nodes, to which this class also listens
            // buildDesignLine();
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
     * @param oldNode XsdTreeNode; former node.
     * @param newNode XsdTreeNode; new node.
     * @return XsdTreeNode; the actual new node (Ots.Network.Node).
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
     * @param inputParameter XsdTreeNode; input parameter node (default).
     * @return XsdTreeNode; the actual node (Ots.Network.Node).
     */
    private XsdTreeNode getInputNode(final XsdTreeNode inputParameter)
    {
        String inputId = inputParameter.getId();
        String nodeId;
        try
        {
            nodeId = (String) getEval().evaluate(inputId.substring(1, inputId.length() - 1));
        }
        catch (RuntimeException ex)
        {
            // TODO: dirty trick to obtain a value that was given to Eval, which not yet supports non Boolean/DoubleScalar.
            nodeId = (String) ScenarioParser.lastLookedUp;
        }
        XsdTreeNode ots = inputParameter.getPath().get(0);
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
     * @param node XsdTreeNode; added coordinate node.
     */
    public void addCoordinate(final XsdTreeNode node)
    {
        if (this.shapeListener.shapeNode.equals(node.getParent()))
        {
            this.shapeListener.coordinates.put(node,
                    Try.assign(() -> orNull(node.getValue(), POINT_ADAPTER), "Exception while interpreting point."));
            buildDesignLine();
            node.addListener(this.shapeListener, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
            node.addListener(this.shapeListener, XsdTreeNode.MOVED, ReferenceType.WEAK);
        }
    }

    /**
     * The map was notified a coordinate node was removed. The node may or may not be part of this link.
     * @param node XsdTreeNode; removed coordinate node.
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
        setValue((v) -> this.from = v, POINT_ADAPTER, this.nodeStart, "Coordinate");
        setValue((v) -> this.to = v, POINT_ADAPTER, this.nodeEnd, "Coordinate");
        if (this.from == null || this.to == null)
        {
            setInvalid();
            return;
        }
        setValue((v) -> this.directionStart = v, DIRECTION_ADAPTER, this.nodeStart, "Direction");
        double dirStart = this.directionStart == null ? 0.0 : this.directionStart.si;
        OrientedPoint2d from = new OrientedPoint2d(this.from, dirStart);
        setValue((v) -> this.directionEnd = v, DIRECTION_ADAPTER, this.nodeEnd, "Direction");
        double dirEnd = this.directionEnd == null ? 0.0 : this.directionEnd.si;
        OrientedPoint2d to = new OrientedPoint2d(this.to, dirEnd);
        if (this.offsetStart != null)
        {
            from = OtsGeometryUtil.offsetPoint(from, this.offsetStart.si);
        }
        if (this.offsetEnd != null)
        {
            to = OtsGeometryUtil.offsetPoint(to, this.offsetEnd.si);
        }
        PolyLine2d line = this.shapeListener.getLine(from, to);
        if (line == null)
        {
            return;
        }
        this.designLine = line;
        setValid();
    }

    /** {@inheritDoc} */
    @Override
    public void evalChanged()
    {
        this.id = getNode().getId() == null ? "" : getNode().getId();
        this.nodeStart = replaceNode(this.nodeStart, getNode().getCoupledKeyrefNode("NodeStart"));
        this.nodeEnd = replaceNode(this.nodeEnd, getNode().getCoupledKeyrefNode("NodeEnd"));
        this.shapeListener.update();
        buildDesignLine();
    }

    /**
     * Notification from the Map that a node (Ots.Network.Node) id was changed.
     * @param node XsdTreeNode; node.
     */
    public void notifyNodeIdChanged(final XsdTreeNode node)
    {
        this.nodeStart = replaceNode(this.nodeStart, getNode().getCoupledKeyrefNode("NodeStart"));
        this.nodeEnd = replaceNode(this.nodeEnd, getNode().getCoupledKeyrefNode("NodeEnd"));
        buildDesignLine();
    }

    /**
     * Returns the value with appropriate adapter, or {@code null} if the value is {@code null}.
     * @param <T> type of the value after unmarshaling.
     * @param value String; value.
     * @param adapter ExpressionAdapter&lt;T, ?&gt;; adapter for values of type T.
     * @return T; unmarshaled value.
     * @throws Exception; from the adapter at unmarshaling.
     */
    private <T> T orNull(final String value, final ExpressionAdapter<T, ?> adapter) throws Exception
    {
        return value == null ? null : adapter.unmarshal(value).get(getEval());
    }

    /**
     * Listener to events that affect the shape. This class can also deliver the resulting line.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private class ShapeListener implements EventListener
    {
        /** */
        private static final long serialVersionUID = 20231020L;

        /** Node of the shape. */
        private XsdTreeNode shapeNode;

        /** Number of segments of Bezier, Clothoid or Arc. */
        private Integer numSegments;

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
                            this.coordinates.put(node, orNull(node.getValue(), POINT_ADAPTER));
                            break;
                        case "StartCurvature":
                            this.startCurvature = orNull(node.getValue(), LINEAR_DENSITY_ADAPTER);
                            break;
                        case "EndCurvature":
                            this.endCurvature = orNull(node.getValue(), LINEAR_DENSITY_ADAPTER);
                            break;
                        case "Length":
                            this.length = orNull(node.getValue(), LENGTH_ADAPTER);
                            break;
                        case "A":
                            this.a = orNull(node.getValue(), LENGTH_ADAPTER);
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
        }

        /**
         * Update the line, clearing all fields, and setting any already available attributes (as the shape node was previously
         * selected and edited).
         */
        private void update()
        {
            this.numSegments = null;
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
                            this.coordinates.put(child, orNull(child.getValue(), POINT_ADAPTER));
                        }
                        catch (Exception ex)
                        {
                            throw new RuntimeException("Expression adapter could not unmarshal value for polyline coordinate.");
                        }
                    }
                    buildDesignLine();
                    break;
                case "Bezier":
                    setAttribute("NumSegments");
                    setAttribute("Shape");
                    setAttribute("Weighted");
                    buildDesignLine();
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
                                        this.startCurvature = orNull(child.getValue(), LINEAR_DENSITY_ADAPTER);
                                        break;
                                    case "EndCurvature":
                                        this.endCurvature = orNull(child.getValue(), LINEAR_DENSITY_ADAPTER);
                                        break;
                                    case "Length":
                                        this.length = orNull(child.getValue(), LENGTH_ADAPTER);
                                        break;
                                    case "A":
                                        this.a = orNull(child.getValue(), LENGTH_ADAPTER);
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
                    setAttribute("NumSegments");
                    buildDesignLine();
                    break;
                case "Arc":
                    setAttribute("Radius");
                    setAttribute("Direction");
                    setAttribute("NumSegments");
                    buildDesignLine();
                    break;
                default:
                    throw new RuntimeException("Drawing of shape node " + this.shapeNode.getNodeName() + " is not supported.");
            }
        }

        /**
         * Set the given attribute from the shape node.
         * @param attribute String; attribute name.
         */
        private void setAttribute(final String attribute)
        {
            if (this.shapeNode.reportInvalidAttributeValue(this.shapeNode.getAttributeIndexByName(attribute)) != null)
            {
                // invalid value, do nothing
                return;
            }
            try
            {
                switch (attribute)
                {
                    case "NumSegments":
                        this.numSegments = getOrNull(attribute, INTEGER_ADAPTER);
                        break;
                    case "Shape":
                        this.shape = getOrNull(attribute, DOUBLE_ADAPTER);
                        break;
                    case "Weighted":
                        this.weighted = getOrNull(attribute, BOOLEAN_ADAPTER);
                        break;
                    case "Length":
                        this.length = getOrNull(attribute, LENGTH_ADAPTER);
                        break;
                    case "Radius":
                        this.radius = getOrNull(attribute, LENGTH_ADAPTER);
                        break;
                    case "Direction":
                        this.direction = getOrNull(attribute, ARC_DIRECTION_ADAPTER);
                        break;
                    default:
                        // an attribute was changed that does not change the shape
                }
            }
            catch (Exception ex)
            {
                throw new RuntimeException("Expression adapter could not unmarshal value for attribute " + attribute);
            }
        }

        /**
         * Returns the attribute value with appropriate adapter, or {@code null} if the attribute is not given.
         * @param <T> type of the attribute value after unmarshaling.
         * @param attribute String; attribute.
         * @param adapter ExpressionAdapter&lt;T, ?&gt;; adapter for values of type T.
         * @return T; unmarshaled value.
         * @throws Exception; from the adapter at unmarshaling.
         */
        private <T> T getOrNull(final String attribute, final ExpressionAdapter<T, ?> adapter) throws Exception
        {
            String value = this.shapeNode.getAttributeValue(attribute);
            return orNull(value, adapter);
        }

        /**
         * Returns the line.
         * @param from OrientedPoint2d; possibly offset start point.
         * @param to OrientedPoint2d; possibly offset end point.
         * @return PolyLine2d; line from the shape and attributes.
         */
        public PolyLine2d getLine(final OrientedPoint2d from, final OrientedPoint2d to)
        {
            try
            {
                switch (this.shapeNode.getNodeName())
                {
                    case "Straight":
                        double length = from.distance(to);
                        double dx = Math.cos(from.dirZ) * length;
                        double dy = Math.sin(from.dirZ) * length;
                        return new PolyLine2d(from, from.translate(dx, dy));
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
                        return new PolyLine2d(list);
                    case "Bezier":
                        double shape = this.shape == null ? 1.0 : this.shape;
                        boolean weighted = this.weighted == null ? false : this.weighted;
                        Point2d[] points = Bezier.cubicControlPoints(from, to, shape, weighted);
                        ContinuousBezierCubic bezier = new ContinuousBezierCubic(points[0], points[1], points[2], points[3]);
                        int numSegments = this.numSegments == null ? 64 : this.numSegments;
                        return bezier.flatten(new Flattener.NumSegments(numSegments));
                    case "Clothoid":
                        ContinuousClothoid clothoid;
                        if (this.shapeNode.getChildCount() == 0 || this.shapeNode.getChild(0).getChildCount() == 0
                                || this.shapeNode.getChild(0).getChild(0).getNodeName().equals("Interpolated"))
                        {
                            clothoid = new ContinuousClothoid(from, to);
                        }
                        else if (this.shapeNode.getChild(0).getChild(0).getNodeName().equals("Length"))
                        {
                            if (this.length == null || this.startCurvature == null || this.endCurvature == null)
                            {
                                return null;
                            }
                            clothoid = ContinuousClothoid.withLength(from, this.length.si, this.startCurvature.si,
                                    this.endCurvature.si);
                        }
                        else
                        {
                            if (this.a == null || this.startCurvature == null || this.endCurvature == null)
                            {
                                return null;
                            }
                            clothoid = new ContinuousClothoid(from, this.a.si, this.startCurvature.si, this.endCurvature.si);
                        }
                        numSegments = this.numSegments == null ? 64 : this.numSegments;
                        return clothoid.flatten(new Flattener.NumSegments(numSegments));
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
                        Angle angle = Angle.instantiateSI(Math.abs(endHeading) - from.dirZ);
                        ContinuousArc arc = new ContinuousArc(from, this.radius.si, left, angle);
                        numSegments = this.numSegments == null ? 64 : this.numSegments;
                        return arc.flatten(new Flattener.NumSegments(numSegments));
                    default:
                        throw new RuntimeException("Drawing of shape node " + this.shapeNode.getNodeName() + " is not supported.");
                }
            }
            catch (DrawRuntimeException exception)
            {
                // Probably a degenerate line as nodes are at the same location
                return null;
            }
        }
    }

}

package org.opentrafficsim.editor.extensions.map;

import java.util.Locale;
import java.util.Optional;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.draw.road.AbstractLineAnimation.LaneBasedObjectData;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.Adapters;
import org.opentrafficsim.road.network.factory.xml.utils.ParseUtil;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType.LengthBeginEnd;

/**
 * Data class for objects that are drawn at a lane position. Implementations must call setLinkNode() in their constructor or by
 * some other dynamic means, or the XSD node must have a Link attribute that points to the XSD node of a link by a keyref. This
 * class will listen to attributes Id, Link, Lane and Position, and update visualization as needed. Attributes Id and Link are
 * optional.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class MapLaneBasedObjectData extends MapData implements LaneBasedObjectData, EventListener
{

    /** Id. */
    private String id = "";

    /** Lane. */
    private String lane;

    /** Position as entered, e.g. END-20m. */
    private LengthBeginEnd position;

    /** Position from start. */
    private Length positionFromStart;

    /** Lane width. */
    private Length laneWidth;

    /** Location. */
    private DirectedPoint2d location;

    /** Bounds. */
    private Bounds2d bounds;

    /** Absolute contour. */
    private Polygon2d absoluteContour;

    /** Relative contour. */
    private Polygon2d relativeContour;

    /** Line on lane. */
    private PolyLine2d line;

    /** Node of link. */
    private XsdTreeNode lastLinkNode;

    /**
     * Constructor.
     * @param map map.
     * @param node node.
     * @param editor editor.
     */
    public MapLaneBasedObjectData(final EditorMap map, final XsdTreeNode node, final OtsEditor editor)
    {
        super(map, node, editor);
        getNode().addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
        if (getNode().isActive())
        {
            if (getNode().isIdentifiable())
            {
                notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Id", null}));
            }
            if (getNode().hasAttribute("Link"))
            {
                notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Link", null}));
            }
            notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Lane", null}));
            notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Position", null}));
        }
    }

    /**
     * Sets a node as link. Sub-classes may call this in their constructor if it is a fixed node. This class will listen to
     * changes in the Link attribute, and set a coupled node as link node if it exists.
     * @param linkNode link node.
     */
    protected void setLinkNode(final XsdTreeNode linkNode)
    {
        if (this.lastLinkNode != null)
        {
            Optional<MapData> data = getMap().getData(linkNode);
            if (data.isPresent())
            {
                ((MapLinkData) data.get()).removeListener(this, MapLinkData.LAYOUT_REBUILT);
            }
        }
        this.lastLinkNode = linkNode;
        Optional<MapData> data = getMap().getData(linkNode);
        if (data.isPresent())
        {
            ((MapLinkData) data.get()).addListener(this, MapLinkData.LAYOUT_REBUILT, ReferenceType.WEAK);
        }
    }

    @Override
    public void destroy()
    {
        super.destroy();
        getNode().removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        if (this.lastLinkNode != null)
        {
            this.lastLinkNode.removeListener(this, MapLinkData.LAYOUT_REBUILT);
        }
    }

    @Override
    public Length getLaneWidth()
    {
        return this.laneWidth;
    }

    @Override
    public DirectedPoint2d getLocation()
    {
        return this.location;
    }

    @Override
    public Bounds2d getRelativeBounds()
    {
        return this.bounds;
    }

    @Override
    public Polygon2d getAbsoluteContour()
    {
        return this.absoluteContour;
    }

    @Override
    public Polygon2d getRelativeContour()
    {
        return this.relativeContour;
    }

    @Override
    public PolyLine2d getLine()
    {
        return this.line;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * Returns an id in the form {linkId}.{laneId}.{id} or {linkId}.{laneId}@{position} if the id is empty.
     * @return id in link/lane/position form.
     */
    protected String getLinkLanePositionId()
    {
        StringBuilder str = new StringBuilder();
        String sep = "";
        if (this.lastLinkNode != null)
        {
            str.append(this.lastLinkNode.getId());
            sep = ".";
        }
        if (this.lane != null)
        {
            str.append(sep).append(this.lane);
        }
        if (this.positionFromStart != null)
        {
            str.append(String.format(Locale.US, "@%.3fm", this.positionFromStart.si));
        }
        return str.toString();
    }

    @Override
    public void evalChanged()
    {
        if (getNode().isIdentifiable())
        {
            this.id = getNode().getId() == null ? "" : getNode().getId();
        }
        if (getNode().hasAttribute("Link"))
        {
            XsdTreeNode linkNode = getNode().getCoupledNodeAttribute("Link").orElse(null);
            setLinkNode(linkNode);
        }
        setValue((v) -> this.lane = v, Adapters.get(String.class), getNode(), "Lane");
        setValue((v) -> this.position = v, Adapters.get(LengthBeginEnd.class), getNode(), "Position");
        setLocation();
    }

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            String attribute = (String) ((Object[]) event.getContent())[1];
            String value = getNode().getAttributeValue(attribute);
            if ("Id".equals(attribute))
            {
                this.id = value == null ? "" : value;
                return;
            }
            else if ("Link".equals(attribute))
            {
                XsdTreeNode linkNode = getNode().getCoupledNodeAttribute("Link").orElse(null);
                setLinkNode(linkNode);
            }
            else if ("Lane".equals(attribute))
            {
                setValue((v) -> this.lane = v, Adapters.get(String.class), getNode(), "Lane");
            }
            else if ("Position".equals(attribute))
            {
                setValue((v) -> this.position = v, Adapters.get(LengthBeginEnd.class), getNode(), "Position");
            }
        }
        // else: MapLinkData.LAYOUT_REBUILT
        setLocation();
    }

    /**
     * Set the location from the coordinate and direction. Notify when invalid or valid.
     */
    private void setLocation()
    {
        if (this.lane == null || this.position == null || this.lastLinkNode == null)
        {
            setInvalid();
            return;
        }
        Optional<MapData> linkDataOptional = getMap().getData(this.lastLinkNode);
        if (linkDataOptional.isEmpty())
        {
            setInvalid();
            return;
        }
        MapLinkData linkData = (MapLinkData) linkDataOptional.get();
        MapLaneData laneData = linkData.getLaneData(this.lane);
        if (laneData == null)
        {
            setInvalid();
            return;
        }
        this.positionFromStart =
                ParseUtil.parseLengthBeginEnd(this.position, Length.ofSI(linkData.getCenterLine().getLength()));

        Length w = laneData.getWidth(this.positionFromStart);
        if (this.laneWidth != null && !this.laneWidth.equals(w))
        {
            // animation (see EditorMap.setValid) stores static data that depends on the lane width
            getMap().reinitialize(getNode());
            return;
        }
        this.laneWidth = w;
        double w45 = 0.45 * getLaneWidth().si;
        DirectedPoint2d point = laneData.getCenterLine().getLocationExtended(this.positionFromStart.si);
        this.location = new DirectedPoint2d(point.x, point.y, point.dirZ);
        this.line = new PolyLine2d(new double[] {0.0, 0.0}, new double[] {-w45, w45});
        this.relativeContour = new Polygon2d(PolyLine2d.concatenate(this.line, this.line.reverse()).getPointList());
        this.bounds = LaneBasedObjectData.super.getRelativeBounds();
        this.absoluteContour =
                new Polygon2d(OtsShape.toAbsoluteTransform(this.location).transform(this.relativeContour.iterator()));
        setValid();
    }

}

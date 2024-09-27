package org.opentrafficsim.editor.extensions.map;

import java.rmi.RemoteException;
import java.util.Locale;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.opentrafficsim.base.geometry.BoundingBox;
import org.opentrafficsim.base.geometry.ClickableBounds;
import org.opentrafficsim.base.geometry.OtsBounds2d;
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

    /** */
    private static final long serialVersionUID = 20240310L;

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
    private OrientedPoint2d location;

    /** Bounds. */
    private OtsBounds2d bounds = new BoundingBox(1.0, 0.25);

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
        try
        {
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
        catch (RemoteException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets a node as link. Sub-classes may call this in their constructor if it is a fixed node. This class will listen to
     * changes in the Link attribute, and set a coupled node as link node if it exists.
     * @param linkNode link node.
     */
    protected void setLinkNode(final XsdTreeNode linkNode)
    {
        try
        {
            if (this.lastLinkNode != null)
            {
                MapLinkData data = (MapLinkData) getMap().getData(linkNode);
                if (data != null)
                {
                    data.removeListener(this, MapLinkData.LAYOUT_REBUILT);
                }
            }
            this.lastLinkNode = linkNode;
            MapLinkData data = (MapLinkData) getMap().getData(linkNode);
            if (data != null)
            {
                data.addListener(this, MapLinkData.LAYOUT_REBUILT, ReferenceType.WEAK);
            }
        }
        catch (RemoteException e)
        {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public Length getLaneWidth()
    {
        return this.laneWidth;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public OtsBounds2d getBounds()
    {
        return this.bounds;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public void evalChanged()
    {
        if (getNode().isIdentifiable())
        {
            this.id = getNode().getId() == null ? "" : getNode().getId();
        }
        if (getNode().hasAttribute("Link"))
        {
            XsdTreeNode linkNode = getNode().getCoupledKeyrefNodeAttribute("Link");
            setLinkNode(linkNode);
        }
        setValue((v) -> this.lane = v, Adapters.get(String.class), getNode(), "Lane");
        setValue((v) -> this.position = v, Adapters.get(LengthBeginEnd.class), getNode(), "Position");
        setLocation();
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
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
                XsdTreeNode linkNode = getNode().getCoupledKeyrefNodeAttribute("Link");
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
        MapLinkData linkData = (MapLinkData) getMap().getData(this.lastLinkNode);
        if (linkData == null)
        {
            setInvalid();
            return;
        }
        MapLaneData laneData = linkData.getLaneData(this.lane);
        if (laneData == null)
        {
            setInvalid();
            return;
        }
        this.positionFromStart =
                ParseUtil.parseLengthBeginEnd(this.position, Length.instantiateSI(linkData.getDesignLine().getLength()));

        Length w = laneData.getWidth(this.positionFromStart);
        if (this.laneWidth != null && !this.laneWidth.equals(w))
        {
            // animation (see EditorMap.setValid) stores static data that depends on the lane width
            getMap().reinitialize(getNode());
            return;
        }
        this.laneWidth = w;
        this.bounds = calculateBounds();
        Ray2d ray = laneData.getCenterLine().getLocationExtended(this.positionFromStart.si);
        this.location = new OrientedPoint2d(ray.x, ray.y, ray.phi);
        setValid();
    }

    /**
     * Calculates the bounds. Can be overridden for objects with non-line shapes.
     * @return bounds of the object.
     */
    protected OtsBounds2d calculateBounds()
    {
        double w45 = 0.45 * getLaneWidth().si;
        return ClickableBounds.get(new Bounds2d(0.0, 0.0, -w45, w45));
    }

}

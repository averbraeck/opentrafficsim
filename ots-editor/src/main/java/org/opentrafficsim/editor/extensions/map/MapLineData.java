package org.opentrafficsim.editor.extensions.map;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.opentrafficsim.base.geometry.BoundingBox;
import org.opentrafficsim.base.geometry.OtsBounds2d;
import org.opentrafficsim.draw.ClickableBounds;
import org.opentrafficsim.draw.road.AbstractLineAnimation.LaneBasedObjectData;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.Adapters;

/**
 * Data classes for objects that are drawn as a lateral line on the lane.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public abstract class MapLineData extends MapData implements LaneBasedObjectData, EventListener
{

    /** */
    private static final long serialVersionUID = 20240220L;

    /** Id. */
    private String id = "";

    /** Lane. */
    private String lane = null;

    /** Position. */
    private Length position = null;

    /** Lane width. */
    private Length laneWidth = null;

    /** Location. */
    private OrientedPoint2d location = null;

    /** Bounds. */
    private OtsBounds2d bounds = new BoundingBox(1.0, 0.25);

    /**
     * Constructor.
     * @param map Map; map.
     * @param node XsdTreeNode; node Ots.Network.Link.TrafficLight.
     * @param editor OtsEditor; editor.
     */
    public MapLineData(final EditorMap map, final XsdTreeNode node, final OtsEditor editor)
    {
        super(map, node, editor);
        getNode().addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
        try
        {
            ((MapLinkData) map.getData(getLinkNode())).addListener(this, MapLinkData.LAYOUT_REBUILT, ReferenceType.WEAK);
            if (getNode().isActive())
            {
                notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Id", null}));
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
     * Returns the {@code XsdTreeNode} of the link that contains this object.
     * @return XsdTreeNode; node of the link that contains this object.
     */
    abstract protected XsdTreeNode getLinkNode();

    /** {@inheritDoc} */
    @Override
    public void destroy()
    {
        super.destroy();
        getNode().removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        getLinkNode().removeListener(this, MapLinkData.LAYOUT_REBUILT);
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

    /** {@inheritDoc} */
    @Override
    public void evalChanged()
    {
        this.id = getNode().getId() == null ? "" : getNode().getId();
        setValue((v) -> this.lane = v, Adapters.get(String.class), getNode(), "Lane");
        setValue((v) -> this.position = v, Adapters.get(Length.class), getNode(), "Position");
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
            else if ("Lane".equals(attribute))
            {
                setValue((v) -> this.lane = v, Adapters.get(String.class), getNode(), "Lane");
            }
            else if ("Position".equals(attribute))
            {
                setValue((v) -> this.position = v, Adapters.get(Length.class), getNode(), "Position");
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
        if (this.lane == null || this.position == null)
        {
            setInvalid();
            return;
        }
        MapLinkData linkData = (MapLinkData) getMap().getData(getLinkNode());
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
        Ray2d ray = laneData.getCenterLine().getLocationExtended(this.position.si);
        Length w = laneData.getWidth(this.position);

        // bounds
        double w45 = 0.45 * w.si;
        double a = ray.phi + Math.PI / 2.0;
        Point2d p1 = new Point2d(ray.x + w45 * Math.cos(a), ray.y - w45 * Math.sin(a));
        Point2d p2 = new Point2d(ray.x - w45 * Math.cos(a), ray.y + w45 * Math.sin(a));
        PolyLine2d geometry = new PolyLine2d(p1, p2);
        AffineTransform transform = AffineTransform.getRotateInstance(-ray.phi, 0.0, 0.0);
        transform.concatenate(AffineTransform.getTranslateInstance(-ray.x, -ray.y));
        Shape path = transform.createTransformedShape(geometry.toPath2D());
        Rectangle2D rect = path.getBounds2D();
        this.bounds = ClickableBounds.get(new Bounds2d(rect.getMinX(), rect.getMaxX(), rect.getMinY(), rect.getMaxY()));
        if (this.laneWidth != null && !this.laneWidth.equals(w))
        {
            // animation (see EditorMap.setValid) stores static data that depends on the lane width
            getMap().reinitialize(getNode());
            return;
        }
        this.location = new OrientedPoint2d(ray.x, ray.y, ray.phi);
        this.laneWidth = w;

        setValid();
    }

}

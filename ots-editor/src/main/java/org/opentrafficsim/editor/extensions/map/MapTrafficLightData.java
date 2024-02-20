package org.opentrafficsim.editor.extensions.map;

import java.awt.Color;
import java.rmi.RemoteException;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.opentrafficsim.draw.road.TrafficLightAnimation.TrafficLightData;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * TrafficLightData for the editor Map.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class MapTrafficLightData extends MapData implements TrafficLightData, EventListener
{

    /** */
    private static final long serialVersionUID = 20240212L;

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
    private Bounds<?, ?, ?> bounds = new Bounds2d(-1.0, 1.0, -.25, 0.25);

    /**
     * Constructor.
     * @param map Map; map.
     * @param node XsdTreeNode; node Ots.Network.Link.TrafficLight.
     * @param editor OtsEditor; editor.
     */
    public MapTrafficLightData(final EditorMap map, final XsdTreeNode node, final OtsEditor editor)
    {
        super(map, node, editor);
        getNode().addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
        try
        {
            ((MapLinkData) map.getData(getNode().getParent())).addListener(this, MapLinkData.LAYOUT_REBUILT,
                    ReferenceType.WEAK);
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

    /** {@inheritDoc} */
    @Override
    public void destroy()
    {
        super.destroy();
        getNode().removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        getNode().getParent().removeListener(this, MapLinkData.LAYOUT_REBUILT);
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
    public Bounds<?, ?, ?> getBounds() throws RemoteException
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
        setValue((v) -> this.lane = v, getAdapter(String.class), getNode(), "Lane");
        setValue((v) -> this.position = v, getAdapter(Length.class), getNode(), "Position");
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
                setValue((v) -> this.lane = v, getAdapter(String.class), getNode(), "Lane");
            }
            else if ("Position".equals(attribute))
            {
                setValue((v) -> this.position = v, getAdapter(Length.class), getNode(), "Position");
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
        MapLinkData linkData = (MapLinkData) getMap().getData(getNode().getParent());
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
        if (this.laneWidth != null && !this.laneWidth.equals(w))
        {
            // animation stores static data that depends on the lane width
            System.out.println("Reinitializing traffic light");
            getMap().reinitialize(getNode());
            return;
        }
        this.location = new OrientedPoint2d(ray.x, ray.y, ray.phi);
        this.laneWidth = w;
        setValid();
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor()
    {
        return Color.RED;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TrafficLight " + getId();
    }

}

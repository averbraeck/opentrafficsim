package org.opentrafficsim.animation.data;

import java.rmi.RemoteException;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.draw.ClickableBounds;
import org.opentrafficsim.draw.road.AbstractLineAnimation.LaneBasedObjectData;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Animation data of a LaneBasedObject.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> lane based object type
 */
public abstract class AnimationLaneBasedObjectData<T extends LaneBasedObject> implements LaneBasedObjectData
{

    /** Lane based object. */
    private final T laneBasedObject;

    /**
     * Constructor.
     * @param laneBasedObject T; laneBasedObject.
     */
    public AnimationLaneBasedObjectData(final T laneBasedObject)
    {
        this.laneBasedObject = laneBasedObject;
    }

    /** {@inheritDoc} */
    @Override
    public Length getLaneWidth()
    {
        return this.laneBasedObject.getLane().getWidth(this.laneBasedObject.getLongitudinalPosition());
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        return this.laneBasedObject.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public Bounds<?, ?, ?> getBounds() throws RemoteException
    {
        return ClickableBounds.get(this.laneBasedObject.getBounds());
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.laneBasedObject.getId();
    }

    /**
     * Returns the wrapped object.
     * @return T; wrapped object.
     */
    public T getObject()
    {
        return this.laneBasedObject;
    }

}

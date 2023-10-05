package org.opentrafficsim.animation.data;

import java.rmi.RemoteException;

import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.point.Point;
import org.opentrafficsim.core.gtu.GtuGenerator.GtuGeneratorPosition;
import org.opentrafficsim.draw.ClickableBounds;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation.GtuGeneratorPositionData;

/**
 * Animation data of a GtuGeneratorPosition.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AnimationGtuGeneratorPositionData implements GtuGeneratorPositionData
{

    /** Position. */
    private final GtuGeneratorPosition position;

    /**
     * Constructor.
     * @param position GtuGeneratorPosition; position within a generator.
     */
    public AnimationGtuGeneratorPositionData(final GtuGeneratorPosition position)
    {
        this.position = position;
    }

    /** {@inheritDoc} */
    @Override
    public Point<?> getLocation() throws RemoteException
    {
        return this.position.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public Bounds<?, ?, ?> getBounds() throws RemoteException
    {
        return ClickableBounds.get(this.position.getBounds());
    }

    /** {@inheritDoc} */
    @Override
    public int getQueueCount()
    {
        return this.position.getQueueCount();
    }

    /**
     * Returns the generator position.
     * @return GtuGeneratorPosition; generator position.
     */
    public GtuGeneratorPosition getGeneratorPosition()
    {
        return this.position;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Generator position";
    }

}

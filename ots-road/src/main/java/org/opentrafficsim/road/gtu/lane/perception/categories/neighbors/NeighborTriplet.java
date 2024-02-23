package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Results from anticipation.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class NeighborTriplet
{

    /** Headway. */
    private final Length headway;

    /** Speed. */
    private final Speed speed;

    /** Acceleration. */
    private final Acceleration acceleration;

    /**
     * @param headway Length; headway
     * @param speed Speed; speed
     * @param acceleration Acceleration; acceleration
     */
    public NeighborTriplet(final Length headway, final Speed speed, final Acceleration acceleration)
    {
        this.headway = headway;
        this.speed = speed;
        this.acceleration = acceleration;
    }

    /**
     * @return headway.
     */
    public Length getHeadway()
    {
        return this.headway;
    }

    /**
     * @return speed.
     */
    public Speed getSpeed()
    {
        return this.speed;
    }

    /**
     * @return acceleration.
     */
    public Acceleration getAcceleration()
    {
        return this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "NeighborTriplet [headway=" + this.headway + ", speed=" + this.speed + ", acceleration=" + this.acceleration
                + "]";
    }
}

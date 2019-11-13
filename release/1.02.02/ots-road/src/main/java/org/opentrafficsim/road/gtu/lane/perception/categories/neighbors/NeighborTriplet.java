package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Results from anticipation.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 feb. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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

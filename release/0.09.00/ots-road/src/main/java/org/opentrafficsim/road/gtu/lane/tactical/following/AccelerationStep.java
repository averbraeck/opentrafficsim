package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

/**
 * Storage for the result of a GTU following model. <br>
 * Currently the result is restricted to a constant acceleration during the period of validity (the time slot) of the result.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1378 $, $LastChangedDate: 2015-09-03 13:38:01 +0200 (Thu, 03 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 6 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AccelerationStep implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** Acceleration that will be maintained during the current time slot. */
    private final Acceleration acceleration;

    /** Time when the current time slot ends. */
    private final Time validUntil;
    
    /** Duration of the time step. */
    final Duration duration;

    /**
     * Create a new GTUFollowingModelResult.
     * @param acceleration DoubleScalarAbs&lt;AccelerationUnit&gt;; computed acceleration
     * @param validUntil Time; time when this result expires
     * @param duration Duration; duration of the time step
     */
    public AccelerationStep(final Acceleration acceleration, final Time validUntil, final Duration duration)
    {
        this.acceleration = acceleration;
        this.validUntil = validUntil;
        this.duration = duration;
    }

    /**
     * @return acceleration.
     */
    public final Acceleration getAcceleration()
    {
        return this.acceleration;
    }

    /**
     * @return validUntil.
     */
    public final Time getValidUntil()
    {
        return this.validUntil;
    }

    /**
     * @return duration.
     */
    public final Duration getDuration()
    {
        return this.duration;
    }

    /**
     * {@inheritDoc}
     */
    public final String toString()
    {
        return String.format("a=%s, valid until %s", this.acceleration, this.validUntil);
    }

}

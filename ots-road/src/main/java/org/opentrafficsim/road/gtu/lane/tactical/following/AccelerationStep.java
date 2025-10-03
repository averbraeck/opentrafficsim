package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;

/**
 * Storage for the result of a GTU following model. <br>
 * Currently the result is restricted to a constant acceleration during the period of validity (the time slot) of the result.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class AccelerationStep implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** Acceleration that will be maintained during the current time slot. */
    private final Acceleration acceleration;

    /** Time when the current time slot ends. */
    private final Duration validUntil;

    /** Duration of the time step. */
    private final Duration duration;

    /**
     * Create a new GtuFollowingModelResult.
     * @param acceleration computed acceleration
     * @param validUntil time when this result expires
     * @param duration duration of the time step
     */
    public AccelerationStep(final Acceleration acceleration, final Duration validUntil, final Duration duration)
    {
        this.acceleration = acceleration;
        this.validUntil = validUntil;
        this.duration = duration;
    }

    /**
     * Returns acceleration.
     * @return acceleration.
     */
    public final Acceleration getAcceleration()
    {
        return this.acceleration;
    }

    /**
     * Returns valid time.
     * @return validUntil.
     */
    public final Duration getValidUntil()
    {
        return this.validUntil;
    }

    /**
     * Returns duration.
     * @return duration.
     */
    public final Duration getDuration()
    {
        return this.duration;
    }

    @Override
    public final String toString()
    {
        return String.format("a=%s, valid until %s", this.acceleration, this.validUntil);
    }

}

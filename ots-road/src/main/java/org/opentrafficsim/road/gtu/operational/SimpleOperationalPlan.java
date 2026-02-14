package org.opentrafficsim.road.gtu.operational;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Simplified plan containing an acceleration value and possible lane change direction.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class SimpleOperationalPlan
{

    /** Acceleration. */
    private Acceleration acceleration;

    /** Duration of the plan. */
    private final Duration duration;

    /** Lane change direction. */
    private final LateralDirectionality laneChangeDirection;

    /**
     * Constructor.
     * @param acceleration acceleration
     * @param duration duration
     */
    public SimpleOperationalPlan(final Acceleration acceleration, final Duration duration)
    {
        this(acceleration, duration, LateralDirectionality.NONE);
    }

    /**
     * Constructor.
     * @param acceleration acceleration
     * @param duration duration
     * @param laneChangeDirection lane change direction, may be {@code null}.
     */
    public SimpleOperationalPlan(final Acceleration acceleration, final Duration duration,
            final LateralDirectionality laneChangeDirection)
    {
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        Throw.whenNull(duration, "Duration may not be null.");
        Throw.whenNull(laneChangeDirection, "Lane change direction may not be null.");
        checkAcceleration(acceleration);
        this.acceleration = Acceleration.max(Acceleration.ofSI(-100.0), acceleration);
        this.duration = duration;
        this.laneChangeDirection = laneChangeDirection;
    }

    /**
     * Return acceleration.
     * @return acceleration.
     */
    public final Acceleration getAcceleration()
    {
        return this.acceleration;
    }

    /**
     * Sets acceleration.
     * @param acceleration acceleration
     */
    public final void setAcceleration(final Acceleration acceleration)
    {
        checkAcceleration(acceleration);
        this.acceleration = acceleration;
    }

    /**
     * Return duration.
     * @return duration.
     */
    public Duration getDuration()
    {
        return this.duration;
    }

    /**
     * Return whether this plan is a lane change plan.
     * @return if lane change.
     */
    public final boolean isLaneChange()
    {
        return this.laneChangeDirection != LateralDirectionality.NONE;
    }

    /**
     * Return lane change direction.
     * @return laneChangeDirection, may be NONE if no lane change.
     */
    public final LateralDirectionality getLaneChangeDirection()
    {
        return this.laneChangeDirection;
    }

    /**
     * Set minimum of current and given acceleration.
     * @param a acceleration to set if lower than current acceleration
     */
    public final void minimizeAcceleration(final Acceleration a)
    {
        checkAcceleration(a);
        this.acceleration = Acceleration.max(Acceleration.ofSI(-100.0), Acceleration.min(this.acceleration, a));
    }

    /**
     * Check acceleration level.
     * @param a acceleration
     */
    private void checkAcceleration(final Acceleration a)
    {
        if (a.equals(Acceleration.NEGATIVE_INFINITY) || a.equals(Acceleration.NEG_MAXVALUE))
        {
            Logger.ots().error("Model has calculated a negative infinite or negative max value acceleration.");
        }
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "SimpleOperationalPlan [Acceleration=" + this.acceleration + ", change=" + this.laneChangeDirection + "]";
    }

}

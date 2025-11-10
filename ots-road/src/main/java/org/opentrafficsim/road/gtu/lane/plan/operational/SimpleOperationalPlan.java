package org.opentrafficsim.road.gtu.lane.plan.operational;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.TurnIndicatorIntent;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * Simplified plan containing an acceleration value and possible lane change direction.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** Deviation from center line. */
    private final Length deviation;

    /** Lane change direction. */
    private final LateralDirectionality laneChangeDirection;

    /** Indicator intent. */
    private TurnIndicatorIntent indicatorIntent = TurnIndicatorIntent.NONE;

    /** Distance to object causing turn indicator intent. */
    private Length indicatorObjectDistance = null;

    /**
     * Constructor.
     * @param acceleration acceleration
     * @param duration duration
     */
    public SimpleOperationalPlan(final Acceleration acceleration, final Duration duration)
    {
        this(acceleration, duration, Length.ZERO, LateralDirectionality.NONE);
    }

    /**
     * Constructor.
     * @param acceleration acceleration
     * @param duration duration
     * @param deviation deviation from center line, positive values is left
     */
    public SimpleOperationalPlan(final Acceleration acceleration, final Duration duration, final Length deviation)
    {
        this(acceleration, duration, deviation, LateralDirectionality.NONE);
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
        this(acceleration, duration, Length.ZERO, laneChangeDirection);
    }

    /**
     * Constructor.
     * @param acceleration acceleration
     * @param duration duration
     * @param deviation deviation from center line, positive values is left
     * @param laneChangeDirection lane change direction, may be {@code null}.
     */
    public SimpleOperationalPlan(final Acceleration acceleration, final Duration duration, final Length deviation,
            final LateralDirectionality laneChangeDirection)
    {
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        Throw.whenNull(duration, "Duration may not be null.");
        Throw.whenNull(deviation, "Deviation may not be null.");
        Throw.whenNull(laneChangeDirection, "Lane change direction may not be null.");
        checkAcceleration(acceleration);
        this.acceleration = Acceleration.max(Acceleration.ofSI(-100.0), acceleration);
        this.duration = duration;
        this.deviation = deviation;
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
     * Return deviation.
     * @return deviation.
     */
    public Length getDeviation()
    {
        return this.deviation;
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
            CategoryLogger.always().error("Model has calculated a negative infinite or negative max value acceleration.");
        }
    }

    /**
     * Returns indicator intent.
     * @return indicatorIntent.
     */
    public final TurnIndicatorIntent getIndicatorIntent()
    {
        return this.indicatorIntent;
    }

    /**
     * Set left indicator intent. Any intent given with distance overrules this intent.
     */
    public final void setIndicatorIntentLeft()
    {
        if (this.indicatorObjectDistance != null)
        {
            return;
        }
        if (this.indicatorIntent.isRight())
        {
            this.indicatorIntent = TurnIndicatorIntent.CONFLICTING;
        }
        else
        {
            this.indicatorIntent = TurnIndicatorIntent.LEFT;
        }
    }

    /**
     * Set right indicator intent. Any intent given with distance overrules this intent.
     */
    public final void setIndicatorIntentRight()
    {
        if (this.indicatorObjectDistance != null)
        {
            return;
        }
        if (this.indicatorIntent.isLeft())
        {
            this.indicatorIntent = TurnIndicatorIntent.CONFLICTING;
        }
        else
        {
            this.indicatorIntent = TurnIndicatorIntent.RIGHT;
        }
    }

    /**
     * Set left indicator intent. Intent with smallest provided distance has priority.
     * @param distance distance to object pertaining to the turn indicator intent
     */
    public final void setIndicatorIntentLeft(final Length distance)
    {
        if (compareAndIgnore(distance))
        {
            return;
        }
        if (this.indicatorIntent.isRight())
        {
            this.indicatorIntent = TurnIndicatorIntent.CONFLICTING;
        }
        else
        {
            this.indicatorIntent = TurnIndicatorIntent.LEFT;
        }

    }

    /**
     * Set right indicator intent. Intent with smallest provided distance has priority.
     * @param distance distance to object pertaining to the turn indicator intent
     */
    public final void setIndicatorIntentRight(final Length distance)
    {
        if (compareAndIgnore(distance))
        {
            return;
        }
        if (this.indicatorIntent.isLeft())
        {
            this.indicatorIntent = TurnIndicatorIntent.CONFLICTING;
        }
        else
        {
            this.indicatorIntent = TurnIndicatorIntent.RIGHT;
        }
    }

    /**
     * Compares distances and returns whether the given distance (and intent) can be ignored.
     * @param distance distance to object of intent
     * @return whether the given distance can be ignored
     */
    private boolean compareAndIgnore(final Length distance)
    {
        if (this.indicatorObjectDistance != null)
        {
            if (this.indicatorObjectDistance.lt(distance))
            {
                // disregard input; the intent from larger distance
                return true;
            }
            if (this.indicatorObjectDistance.gt(distance))
            {
                // disregard existing; the intent from larger distance
                this.indicatorIntent = TurnIndicatorIntent.NONE; // prevents a set to CONFLICTING
            }
        }
        else
        {
            // disregard existing; the intent without distance
            this.indicatorIntent = TurnIndicatorIntent.NONE; // prevents a set to CONFLICTING
        }
        return false;
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "SimpleOperationalPlan [Acceleration=" + this.acceleration + ", change=" + this.laneChangeDirection
                + ", indicator intent=" + this.indicatorIntent + "]";
    }

    /**
     * Set turn indicator.
     * @param gtu LaneBasedGtu to set the indicator on
     * @throws GtuException if GTU does not support the indicator
     */
    public final void setTurnIndicator(final LaneBasedGtu gtu) throws GtuException
    {
        if (this.indicatorIntent.isLeft())
        {
            gtu.setTurnIndicatorStatus(TurnIndicatorStatus.LEFT);
        }
        else if (this.indicatorIntent.isRight())
        {
            gtu.setTurnIndicatorStatus(TurnIndicatorStatus.RIGHT);
        }
        else
        {
            gtu.setTurnIndicatorStatus(TurnIndicatorStatus.NONE);
        }
    }

}

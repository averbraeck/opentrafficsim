package org.opentrafficsim.road.gtu.lane.plan.operational;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.TurnIndicatorIntent;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Simplified plan containing an acceleration value and possible lane change direction.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 26, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class SimpleOperationalPlan implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Acceleration. */
    private Acceleration acceleration;

    /** Lane change direction. */
    private final LateralDirectionality laneChangeDirection;

    /** Indicator intent. */
    private TurnIndicatorIntent indicatorIntent = TurnIndicatorIntent.NONE;

    /** Distance to object causing turn indicator intent. */
    private Length indicatorObjectDistance = null;

    /** Duration of the plan. */
    private final Duration duration;

    /**
     * @param acceleration Acceleration; acceleration
     * @param duration Duration; duration
     */
    public SimpleOperationalPlan(final Acceleration acceleration, final Duration duration)
    {
        this(acceleration, duration, LateralDirectionality.NONE);
    }

    /**
     * @param acceleration Acceleration; acceleration
     * @param duration Duration; duration
     * @param laneChangeDirection LateralDirectionality; lane change direction, may be {@code null}.
     */
    public SimpleOperationalPlan(final Acceleration acceleration, final Duration duration,
            final LateralDirectionality laneChangeDirection)
    {
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        Throw.whenNull(duration, "Duration may not be null.");
        Throw.whenNull(laneChangeDirection, "Lane change direction may not be null.");
        checkAcceleration(acceleration);
        this.acceleration = Acceleration.max(Acceleration.instantiateSI(-100.0), acceleration);
        this.duration = duration;
        this.laneChangeDirection = laneChangeDirection;
    }

    /**
     * @return acceleration.
     */
    public final Acceleration getAcceleration()
    {
        return this.acceleration;
    }

    /**
     * Sets acceleration.
     * @param acceleration Acceleration; acceleration
     */
    public final void setAcceleration(final Acceleration acceleration)
    {
        checkAcceleration(acceleration);
        this.acceleration = acceleration;
    }

    /**
     * @return duration.
     */
    public Duration getDuration()
    {
        return this.duration;
    }

    /**
     * @return if lane change.
     */
    public final boolean isLaneChange()
    {
        return this.laneChangeDirection != LateralDirectionality.NONE;
    }

    /**
     * @return laneChangeDirection, may be NONE if no lane change.
     */
    public final LateralDirectionality getLaneChangeDirection()
    {
        return this.laneChangeDirection;
    }

    /**
     * Set minimum of current and given acceleration.
     * @param a Acceleration; acceleration to set if lower than current acceleration
     */
    public final void minimizeAcceleration(final Acceleration a)
    {
        checkAcceleration(a);
        // XXX: AV
        this.acceleration = Acceleration.max(Acceleration.instantiateSI(-100.0), Acceleration.min(this.acceleration, a));
    }

    /**
     * Check acceleration level.
     * @param a Acceleration; acceleration
     */
    private void checkAcceleration(final Acceleration a)
    {
        if (a.equals(Acceleration.NEGATIVE_INFINITY) || a.equals(Acceleration.NEG_MAXVALUE))
        {
            // XXX: AV
            CategoryLogger.always().error("Model has calculated a negative infinite or negative max value acceleration.");
        }
    }

    /**
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
     * @param distance Length; distance to object pertaining to the turn indicator intent
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
     * @param distance Length; distance to object pertaining to the turn indicator intent
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
     * @param distance Length; distance to object of intent
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

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "SimpleOperationalPlan [Acceleration=" + this.acceleration + ", change=" + this.laneChangeDirection
                + ", indicator intent=" + this.indicatorIntent + "]";
    }

    /**
     * @param gtu LaneBasedGTU; LaneBasedGTU to set the indicator on
     * @throws GTUException if GTU does not support the indicator
     */
    public final void setTurnIndicator(final LaneBasedGTU gtu) throws GTUException
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

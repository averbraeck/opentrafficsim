package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSituationalAwareness;

/**
 * Estimation of neighbor headway, speed and acceleration.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface Estimation
{
    /** No estimation errors. */
    Estimation NONE = new Estimation()
    {
        @Override
        public NeighborTriplet estimate(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance, final boolean downstream, final Time when) throws ParameterException
        {
            return new NeighborTriplet(getDelayedHeadway(perceivingGtu, perceivedGtu, distance, downstream, when),
                    getEgoSpeed(perceivingGtu).plus(getDelayedSpeedDifference(perceivingGtu, perceivedGtu, when)),
                    perceivedGtu.getAcceleration(when));
        }
    };

    /** Underestimation based on situational awareness. */
    Estimation UNDERESTIMATION = new FactorEstimation()
    {
        /** {@inheritDoc} */
        @Override
        boolean overEstimation()
        {
            return false;
        }
    };

    /** OVerestimation based on situational awareness. */
    Estimation OVERESTIMATION = new FactorEstimation()
    {
        /** {@inheritDoc} */
        @Override
        boolean overEstimation()
        {
            return true;
        }
    };

    /**
     * Estimate headway, speed and acceleration.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param perceivedGtu LaneBasedGTU; perceived GTU
     * @param distance Length; actual headway at 'now' (i.e. not at 'when' if there is a reaction time)
     * @param downstream boolean; downstream (or upstream) neighbor
     * @param when Time; moment of perception, reaction time included
     * @return NeighborTriplet; perceived headway, speed and acceleration
     * @throws ParameterException on invalid parameter value or if parameter is not available
     */
    NeighborTriplet estimate(LaneBasedGTU perceivingGtu, LaneBasedGTU perceivedGtu, Length distance, boolean downstream,
            Time when) throws ParameterException;

    /**
     * Returns a delayed headway.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param perceivedGtu LaneBasedGTU; perceived GTU
     * @param distance Length; actual headway at 'now' (i.e. not at 'when' if there is a reaction time)
     * @param downstream boolean; downstream (or upstream) neighbor
     * @param when Time; moment of perception, reaction time included
     * @return Length; delayed headway
     */
    default Length getDelayedHeadway(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu, final Length distance,
            final boolean downstream, final Time when)
    {
        double delta = (perceivedGtu.getOdometer().si - perceivedGtu.getOdometer(when).si)
                - (perceivingGtu.getOdometer().si - perceivingGtu.getOdometer(when).si);
        if (downstream)
        {
            delta = -delta; // faster leader increases the headway, faster follower reduces the headway
        }
        return Length.instantiateSI(distance.si + delta);
    }

    /**
     * Returns the ego speed. This is the speed used in AbstractLaneBasedGTU.getCarFollowingAcceleration(), and hence this is
     * the reference speed for the stimulus of speed difference.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @return Speed; ego speed
     */
    default Speed getEgoSpeed(final LaneBasedGTU perceivingGtu)
    {
        try
        {
            return perceivingGtu.getTacticalPlanner().getPerception().getPerceptionCategory(EgoPerception.class).getSpeed();
        }
        catch (OperationalPlanException exception)
        {
            throw new RuntimeException("Speed difference is perceived using EgoPerception for the ego speed, but it's missing.",
                    exception);
        }
    }

    /**
     * Returns a delayed speed difference (other minus ego).
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param perceivedGtu LaneBasedGTU; perceived GTU
     * @param when Time; moment of perception, reaction time included
     * @return Speed; delayed speed difference (other minus ego)
     */
    default Speed getDelayedSpeedDifference(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu, final Time when)
    {
        return Speed.instantiateSI(perceivedGtu.getSpeed(when).si - perceivingGtu.getSpeed(when).si);
    }

    /**
     * Estimation based on a factor.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    abstract class FactorEstimation implements Estimation
    {

        /** Sign. */
        private final double sign;

        /**
         * Constructor.
         */
        FactorEstimation()
        {
            this.sign = overEstimation() ? 1.0 : -1.0;
        }

        /** {@inheritDoc} */
        @Override
        public NeighborTriplet estimate(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance, final boolean downstream, final Time when) throws ParameterException
        {
            double factor = 1.0 + this.sign * (perceivingGtu.getParameters().getParameter(AdaptationSituationalAwareness.SA_MAX)
                    - perceivingGtu.getParameters().getParameter(AdaptationSituationalAwareness.SA));
            Length headway = getDelayedHeadway(perceivingGtu, perceivedGtu, distance, downstream, when).times(factor);
            Speed speed =
                    getEgoSpeed(perceivingGtu).plus(getDelayedSpeedDifference(perceivingGtu, perceivedGtu, when).times(factor));
            Acceleration acceleration = perceivedGtu.getAcceleration(when);
            return new NeighborTriplet(headway, speed, acceleration);
        }

        /**
         * Returns whether this is over-estimation.
         * @return boolean; whether this is over-estimation
         */
        abstract boolean overEstimation();
    }

}

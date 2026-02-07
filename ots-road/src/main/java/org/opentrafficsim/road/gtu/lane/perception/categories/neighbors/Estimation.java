package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Estimation of neighbor headway, speed and acceleration.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface Estimation
{

    /** No estimation errors. */
    Estimation NONE = new Estimation()
    {
        @Override
        public NeighborTriplet estimate(final LaneBasedGtu perceivingGtu, final LaneBasedObject reference,
                final LaneBasedGtu perceivedGtu, final Length distance, final boolean downstream, final Duration when)
                throws ParameterException
        {
            return new NeighborTriplet(getDelayedDistance(perceivingGtu, reference, perceivedGtu, distance, downstream, when),
                    getDelayedReferenceSpeed(perceivingGtu, reference, when)
                            .plus(getDelayedSpeedDifference(perceivingGtu, reference, perceivedGtu, when)),
                    perceivedGtu.getAcceleration(when));
        }

        @Override
        public String toString()
        {
            return "NONE";
        }
    };

    /**
     * Estimate headway, speed and acceleration.
     * @param perceivingGtu perceiving GTU
     * @param reference reference object, e.g. the perceiving GTU, or a Conflict
     * @param perceivedGtu perceived GTU
     * @param distance actual headway at 'now' (i.e. not at 'when' if there is a reaction time)
     * @param downstream downstream (or upstream) neighbor
     * @param when moment of perception, reaction time included
     * @return perceived headway, speed and acceleration
     * @throws ParameterException on invalid parameter value or if parameter is not available
     */
    NeighborTriplet estimate(LaneBasedGtu perceivingGtu, LaneBasedObject reference, LaneBasedGtu perceivedGtu, Length distance,
            boolean downstream, Duration when) throws ParameterException;

    /**
     * Returns a delayed distance. If the reference is not the perceiving GTU (e.g. a static object) this is the current
     * distance minus the odometer difference of the perceived GTU over the delay. In case the reference is the perceiving GTU,
     * the odometer difference over the delay of the perceiving GTU is added.
     * @param perceivingGtu perceiving GTU
     * @param reference reference object, e.g. the perceiving GTU, or a Conflict
     * @param perceivedGtu perceived GTU
     * @param distance actual distance at 'now' (i.e. not at 'when' if there is a reaction time)
     * @param downstream downstream (or upstream) neighbor
     * @param when moment of perception, reaction time included
     * @return delayed headway
     */
    default Length getDelayedDistance(final LaneBasedGtu perceivingGtu, final LaneBasedObject reference,
            final LaneBasedGtu perceivedGtu, final Length distance, final boolean downstream, final Duration when)
    {
        double delta = (perceivedGtu.getOdometer().si - perceivedGtu.getOdometer(when).si);
        if (perceivingGtu.equals(reference))
        {
            delta -= (perceivingGtu.getOdometer().si - perceivingGtu.getOdometer(when).si);
        }
        if (downstream)
        {
            delta = -delta; // faster leader increases the headway, faster follower reduces the headway
        }
        return Length.ofSI(distance.si + delta);
    }

    /**
     * Returns the reference speed. If the perceiving GTU is the reference, it is the speed of the perceiving GTU. Otherwise
     * zero speed is returned, assuming a static reference.
     * @param perceivingGtu perceiving GTU
     * @param reference reference object, e.g. the perceiving GTU, or a Conflict
     * @param when moment of perception, reaction time included
     * @return ego speed
     */
    default Speed getDelayedReferenceSpeed(final LaneBasedGtu perceivingGtu, final LaneBasedObject reference,
            final Duration when)
    {
        if (!perceivingGtu.equals(reference))
        {
            return Speed.ZERO;
        }
        return perceivingGtu.getSpeed(when);
    }

    /**
     * Returns a delayed speed difference (other minus reference speed). If the perceiving GTU is the reference, this is the
     * speed difference between the two GTUs. Otherwise it is the speed of the perceived GTU (i.e. speed difference to a static
     * object).
     * @param perceivingGtu perceiving GTU
     * @param reference reference object, e.g. the perceiving GTU, or a Conflict
     * @param perceivedGtu perceived GTU
     * @param when moment of perception, reaction time included
     * @return delayed speed difference (other minus ego)
     */
    default Speed getDelayedSpeedDifference(final LaneBasedGtu perceivingGtu, final LaneBasedObject reference,
            final LaneBasedGtu perceivedGtu, final Duration when)
    {
        if (perceivingGtu.equals(reference))
        {
            return perceivedGtu.getSpeed(when).minus(perceivingGtu.getSpeed(when));
        }
        return perceivedGtu.getSpeed(when);
    }

}

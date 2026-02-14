package org.opentrafficsim.road.gtu.tactical.util.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.util.CarFollowingUtil;

/**
 * Interface for LMRS gap-acceptance models.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface GapAcceptance
{

    /** Being informed of the model and parameters of other drivers (default LMRS). */
    GapAcceptance INFORMED = new GapAcceptance()
    {
        @Override
        public boolean acceptGap(final TacticalContextEgo context, final double desire, final LateralDirectionality lat)
                throws ParameterException, OperationalPlanException
        {
            NeighborsPerception neighbors = context.getPerception().getPerceptionCategory(NeighborsPerception.class);
            if (neighbors.isGtuAlongside(lat))
            {
                // gtu alongside
                return false;
            }

            Acceleration threshold = context.getParameters().getParameter(ParameterTypes.B).times(-desire);
            if (!acceptEgoAcceleration(context, desire, lat, threshold))
            {
                return false;
            }

            // TODO
            /*-
             * Followers and are accepted if the acceleration and speed is 0, a leader is accepted if the ego speed is 0. This
             * is in place as vehicles that provide courtesy, will decelerate for us and overshoot the stand-still distance. As
             * a consequence, they will cease cooperation as they are too close. A pattern will arise where followers slow down
             * to (near) stand-still, and accelerate again, before we could ever accept the gap.
             *
             * By accepting the gap in the moment that they reach stand-still, this vehicle can at least accept the gap at some
             * point. All of this is only a problem if the own vehicle is standing still. Otherwise the stand-still distance is
             * not important and movement of our own will create an acceptable situation.
             *
             * What needs to be done, is to find a better way to deal with the cooperation and gap-acceptance, such that this
             * hack is not required.
             */
            for (PerceivedGtu follower : neighbors.getFirstFollowers(lat))
            {
                if (follower.getSpeed().gt0() || follower.getAcceleration().gt0() || follower.getDistance().si < 1.0)
                {
                    Acceleration aFollow =
                            LmrsUtil.singleAcceleration(follower, follower.getDistance(), context.getSpeed(), desire);
                    if (threshold.gt(aFollow))
                    {
                        return false;
                    }
                }
            }

            if (!acceptLaneChangers(context, lat, threshold))
            {
                return false;
            }

            return true;
        }

        @Override
        public String toString()
        {
            return "INFORMED";
        }
    };

    /** Being informed of the model and parameters of other drivers, but applying own headway value. */
    GapAcceptance EGO_HEADWAY = new GapAcceptance()
    {
        @Override
        public boolean acceptGap(final TacticalContextEgo context, final double desire, final LateralDirectionality lat)
                throws ParameterException, OperationalPlanException
        {
            NeighborsPerception neigbors = context.getPerception().getPerceptionCategory(NeighborsPerception.class);
            if (neigbors.isGtuAlongside(lat))
            {
                // gtu alongside
                return false;
            }

            Acceleration threshold = context.getParameters().getParameter(ParameterTypes.B).times(-desire);
            if (!acceptEgoAcceleration(context, desire, lat, threshold))
            {
                return false;
            }

            for (PerceivedGtu follower : neigbors.getFirstFollowers(lat))
            {
                if (follower.getSpeed().gt0() || follower.getAcceleration().gt0())
                {
                    // Change headway parameter
                    Parameters folParams = follower.getBehavior().getParameters();
                    folParams.setParameter(ParameterTypes.TMIN, context.getParameters().getParameter(ParameterTypes.TMIN));
                    folParams.setParameter(ParameterTypes.TMAX, context.getParameters().getParameter(ParameterTypes.TMAX));
                    Acceleration aFollow =
                            LmrsUtil.singleAcceleration(follower, follower.getDistance(), context.getSpeed(), desire);
                    folParams.resetParameter(ParameterTypes.TMIN);
                    folParams.resetParameter(ParameterTypes.TMAX);
                    if (threshold.gt(aFollow))
                    {
                        return false;
                    }
                }
            }

            if (!acceptLaneChangers(context, lat, threshold))
            {
                return false;
            }

            return true;
        }

        @Override
        public String toString()
        {
            return "EGO_HEADWAY";
        }
    };

    /**
     * Determine whether a gap is acceptable.
     * @param context tactical information such as parameters and car-following model
     * @param desire level of lane change desire
     * @param lat lateral direction for synchronization
     * @param threshold threshold value
     * @return whether a gap is acceptable
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    private static boolean acceptEgoAcceleration(final TacticalContextEgo context, final double desire,
            final LateralDirectionality lat, final Acceleration threshold) throws ParameterException, OperationalPlanException
    {
        if (context.getSpeed().gt0())
        {
            for (PerceivedGtu leader : context.getPerception().getPerceptionCategory(NeighborsPerception.class)
                    .getFirstLeaders(lat))
            {
                Acceleration a = LmrsUtil.singleAcceleration(context, leader.getDistance(), leader.getSpeed(), desire);
                if (threshold.gt(a))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determine whether a gap is acceptable regarding lane changers from the second adjacent lane to the first adjacent lane.
     * @param context tactical information such as parameters and car-following model
     * @param lat lateral direction for synchronization
     * @param threshold threshold value
     * @return whether a gap is acceptable
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    private static boolean acceptLaneChangers(final TacticalContextEgo context, final LateralDirectionality lat,
            final Acceleration threshold) throws ParameterException, OperationalPlanException
    {
        if (context.getSpeed().gt0())
        {
            NeighborsPerception neighbors = context.getPerception().getPerceptionCategory(NeighborsPerception.class);
            // Only potential lane changers in the gap to the leader in the target lane are relevant
            SortedSet<PerceivedGtu> firstLeaders = neighbors.getFirstLeaders(lat);
            Length range = Length.POS_MAXVALUE;
            if (!firstLeaders.isEmpty())
            {
                range = Length.ZERO;
                for (PerceivedGtu leader : firstLeaders)
                {
                    range = Length.max(range, leader.getDistance());
                }
            }
            for (PerceivedGtu leader : neighbors.getLeaders(new RelativeLane(lat, 2)))
            {
                if (leader.getDistance().gt(range))
                {
                    return true;
                }
                if (leader.getManeuver().isChangingLane(lat.flip()))
                {
                    Acceleration a = CarFollowingUtil.followSingleLeader(context, leader.getDistance(), leader.getSpeed());
                    return a.ge(threshold);
                }
            }
        }
        return true;
    }

    /**
     * Determine whether a gap is acceptable.
     * @param context tactical information such as parameters and car-following model
     * @param desire level of lane change desire
     * @param lat lateral direction for synchronization
     * @return whether a gap is acceptable
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    boolean acceptGap(TacticalContextEgo context, double desire, LateralDirectionality lat)
            throws ParameterException, OperationalPlanException;

}

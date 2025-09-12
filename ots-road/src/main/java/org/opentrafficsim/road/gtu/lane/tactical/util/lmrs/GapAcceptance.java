package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Interface for LMRS gap-acceptance models.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
        public boolean acceptGap(final LanePerception perception, final Parameters params, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final Speed ownSpeed, final Acceleration ownAcceleration,
                final LateralDirectionality lat) throws ParameterException, OperationalPlanException
        {
            NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
            if (neighbors.isGtuAlongside(lat))
            {
                // gtu alongside
                return false;
            }

            Acceleration threshold = params.getParameter(ParameterTypes.B).times(-desire);
            if (threshold.gt(ownAcceleration))
            {
                return false;
            }

            if (!acceptEgoAcceleration(perception, params, sli, cfm, desire, ownSpeed, lat, threshold))
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
                    Acceleration aFollow = LmrsUtil.singleAcceleration(follower.getDistance(), follower.getSpeed(), ownSpeed,
                            desire, follower.getBehavior().getParameters(), follower.getBehavior().getSpeedLimitInfo(),
                            follower.getBehavior().getCarFollowingModel());
                    if (threshold.gt(aFollow))
                    {
                        return false;
                    }
                }
            }

            if (!acceptLaneChangers(perception, params, sli, cfm, ownSpeed, lat, threshold))
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
        public boolean acceptGap(final LanePerception perception, final Parameters params, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final Speed ownSpeed, final Acceleration ownAcceleration,
                final LateralDirectionality lat) throws ParameterException, OperationalPlanException
        {
            NeighborsPerception neigbors = perception.getPerceptionCategory(NeighborsPerception.class);
            if (neigbors.isGtuAlongside(lat))
            {
                // gtu alongside
                return false;
            }

            Acceleration threshold = params.getParameter(ParameterTypes.B).times(-desire);
            if (!acceptEgoAcceleration(perception, params, sli, cfm, desire, ownSpeed, lat, threshold))
            {
                return false;
            }

            for (PerceivedGtu follower : neigbors.getFirstFollowers(lat))
            {
                if (follower.getSpeed().gt0() || follower.getAcceleration().gt0())
                {
                    // Change headway parameter
                    Parameters folParams = follower.getBehavior().getParameters();
                    folParams.setParameterResettable(ParameterTypes.TMIN, params.getParameter(ParameterTypes.TMIN));
                    folParams.setParameterResettable(ParameterTypes.TMAX, params.getParameter(ParameterTypes.TMAX));
                    Acceleration aFollow = LmrsUtil.singleAcceleration(follower.getDistance(), follower.getSpeed(), ownSpeed,
                            desire, folParams, follower.getBehavior().getSpeedLimitInfo(),
                            follower.getBehavior().getCarFollowingModel());
                    folParams.resetParameter(ParameterTypes.TMIN);
                    folParams.resetParameter(ParameterTypes.TMAX);
                    if (threshold.gt(aFollow))
                    {
                        return false;
                    }
                }
            }

            if (!acceptLaneChangers(perception, params, sli, cfm, ownSpeed, lat, threshold))
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
     * @param perception perception
     * @param params parameters
     * @param sli speed limit info
     * @param cfm car-following model
     * @param desire level of lane change desire
     * @param ownSpeed own speed
     * @param lat lateral direction for synchronization
     * @param threshold threshold value
     * @return whether a gap is acceptable
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    private static boolean acceptEgoAcceleration(final LanePerception perception, final Parameters params,
            final SpeedLimitInfo sli, final CarFollowingModel cfm, final double desire, final Speed ownSpeed,
            final LateralDirectionality lat, final Acceleration threshold) throws ParameterException, OperationalPlanException
    {
        if (ownSpeed.gt0())
        {
            for (PerceivedGtu leader : perception.getPerceptionCategory(NeighborsPerception.class).getFirstLeaders(lat))
            {
                Acceleration a = LmrsUtil.singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire, params,
                        sli, cfm);
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
     * @param perception perception
     * @param params parameters
     * @param sli speed limit info
     * @param cfm car-following model
     * @param ownSpeed own speed
     * @param lat lateral direction for synchronization
     * @param threshold threshold value
     * @return whether a gap is acceptable
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    private static boolean acceptLaneChangers(final LanePerception perception, final Parameters params,
            final SpeedLimitInfo sli, final CarFollowingModel cfm, final Speed ownSpeed, final LateralDirectionality lat,
            final Acceleration threshold) throws ParameterException, OperationalPlanException
    {
        if (ownSpeed.gt0())
        {
            NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
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
                    Acceleration a = CarFollowingUtil.followSingleLeader(cfm, params, ownSpeed, sli, leader.getDistance(),
                            leader.getSpeed());
                    return a.ge(threshold);
                }
            }
        }
        return true;
    }

    /**
     * Determine whether a gap is acceptable.
     * @param perception perception
     * @param params parameters
     * @param sli speed limit info
     * @param cfm car-following model
     * @param desire level of lane change desire
     * @param ownSpeed own speed
     * @param ownAcceleration current car-following acceleration
     * @param lat lateral direction for synchronization
     * @return whether a gap is acceptable
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    boolean acceptGap(LanePerception perception, Parameters params, SpeedLimitInfo sli, CarFollowingModel cfm, double desire,
            Speed ownSpeed, Acceleration ownAcceleration, LateralDirectionality lat)
            throws ParameterException, OperationalPlanException;

}

package org.opentrafficsim.road.gtu.tactical.util.lmrs;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.NamedConstants;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.util.CarFollowingUtil;

/**
 * Different forms of synchronization.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
public interface Synchronization extends NamedConstants
{

    /** Speed below which vehicles will creep rather than synchronize. */
    /*
     * 0km/h makes the vehicles merge where d = dCoop, but highly stop-and-go and inefficient. 10km/h roughly makes the vehicles
     * merge at the end of the lane. Larger values will cause the synchronization process to go beyond the end of the lane.
     * Conceptually this value should correlate to "typical vehicle length" / "desired time headway". Then vehicles will be able
     * to merge assuming full lane change urgency. For 4.0m / 1.6s we get 9km/h.
     */
    ParameterTypeSpeed CREEP_SPEED = new ParameterTypeSpeed("vCreep",
            "Speed below which vehicles will creep rather than follow.", new Speed(9, SpeedUnit.KM_PER_HOUR));

    /**
     * Synchronization where the current leader is followed with limited deceleration.
     */
    Synchronization PASSIVE = new Synchronization()
    {
        @Override
        public Acceleration synchronize(final TacticalContextEgo context, final double desire, final LateralDirectionality lat,
                final LmrsData lmrsData, final LateralDirectionality initiatedLaneChange)
                throws ParameterException, OperationalPlanException
        {
            Acceleration a = Acceleration.POS_MAXVALUE;
            RelativeLane relativeLane = new RelativeLane(lat, 1);
            PerceivedGtu leader = Synchronization.getProspectiveLeader(context, relativeLane, desire);
            if (leader != null)
            {
                // follow prospective leader
                a = LmrsUtil.relaxedAcceleration(context, leader.getDistance(), leader.getSpeed(), desire);

                // never stop before we can actually merge
                Length xMerge = Synchronization.getMergeDistance(context.getPerception(), lat).minus(context.getLength());
                if (xMerge.gt0())
                {
                    Acceleration aMerge = CarFollowingUtil.followSingleLeader(context, xMerge, Speed.ZERO);
                    a = Acceleration.max(a, aMerge);
                }

                // limit deceleration based on desire
                a = Acceleration.max(a, context.getParameters().getParameter(ParameterTypes.B).neg());
            }
            return a;
        }

        @Override
        public String name()
        {
            return "PASSIVE";
        }
    };

    /**
     * Synchronization where the current leader is followed with limited deceleration.
     * <p>
     * Synchronization is disabled for v &lt; {@link #CREEP_SPEED}.
     */
    Synchronization PASSIVE_MOVING = new Synchronization()
    {
        @Override
        public Acceleration synchronize(final TacticalContextEgo context, final double desire, final LateralDirectionality lat,
                final LmrsData lmrsData, final LateralDirectionality initiatedLaneChange)
                throws ParameterException, OperationalPlanException
        {
            if (context.getSpeed().si < context.getParameters().getParameter(CREEP_SPEED).si
                    && !Synchronization.leaderIsCreeping(context, desire, lat))
            {
                return Acceleration.POS_MAXVALUE;
            }
            return PASSIVE.synchronize(context, desire, lat, lmrsData, initiatedLaneChange);
        }

        @Override
        public String name()
        {
            return "PASSIVE_MOVING";
        }
    };

    /**
     * Synchronization by following the adjacent leader or aligning with the middle of the gap, whichever allows the largest
     * acceleration. Note that aligning with the middle of the gap then means the gap is too small, as following would cause
     * lower acceleration. Aligning with the middle of the gap will however provide a better starting point for the rest of the
     * process. Mainly, the adjacent follower can decelerate less, allowing more smooth merging.
     */
    Synchronization ALIGN_GAP = new Synchronization()
    {
        @Override
        public Acceleration synchronize(final TacticalContextEgo context, final double desire, final LateralDirectionality lat,
                final LmrsData lmrsData, final LateralDirectionality initiatedLaneChange)
                throws ParameterException, OperationalPlanException
        {
            Acceleration a = Acceleration.POS_MAXVALUE;
            RelativeLane relativeLane = new RelativeLane(lat, 1);
            PerceivedGtu leader = Synchronization.getProspectiveLeader(context, relativeLane, desire);
            if (leader != null)
            {
                // follow prospective leader
                a = LmrsUtil.relaxedAcceleration(context, leader.getDistance(), leader.getSpeed(), desire);

                // follow middle of gap, allow acceleration to be higher
                PerceivedGtu follower = Synchronization.getProspectiveFollower(context, relativeLane, leader);
                if (follower != null)
                {
                    double netGap = leader.getDistance().si + follower.getDistance().si;
                    Length desired = context.getCarFollowingModel().desiredHeadway(context.getParameters(), context.getSpeed());
                    Length gap =
                            Length.ofSI(leader.getDistance().si + 0.5 * Math.min(-netGap, leader.getLength().si) + desired.si);
                    Acceleration aGap = CarFollowingUtil.followSingleLeader(context, gap, leader.getSpeed());
                    a = a.si > aGap.si ? a : aGap;
                }

                // never stop before we can actually merge
                Length xMerge = Synchronization.getMergeDistance(context.getPerception(), lat);
                if (xMerge.gt0())
                {
                    Acceleration aMerge = CarFollowingUtil.followSingleLeader(context, xMerge, Speed.ZERO);
                    a = a.si > aMerge.si ? a : aMerge;
                }

                // limit deceleration based on desire
                a = Acceleration.max(a, context.getParameters().getParameter(ParameterTypes.B).neg());
            }
            return a;
        }

        @Override
        public String name()
        {
            return "ALIGN_GAP";
        }
    };

    /**
     * Synchronization by following the adjacent leader or aligning with the middle of the gap, whichever allows the largest
     * acceleration. Note that aligning with the middle of the gap then means the gap is too small, as following would cause
     * lower acceleration. Aligning with the middle of the gap will however provide a better starting point for the rest of the
     * process. Mainly, the adjacent follower can decelerate less, allowing more smooth merging.
     * <p>
     * Synchronization is disabled for v &lt; {@link #CREEP_SPEED}.
     */
    Synchronization ALIGN_GAP_MOVING = new Synchronization()
    {
        @Override
        public Acceleration synchronize(final TacticalContextEgo context, final double desire, final LateralDirectionality lat,
                final LmrsData lmrsData, final LateralDirectionality initiatedLaneChange)
                throws ParameterException, OperationalPlanException
        {
            if (context.getSpeed().si < context.getParameters().getParameter(CREEP_SPEED).si
                    && !Synchronization.leaderIsCreeping(context, desire, lat))
            {
                return Acceleration.POS_MAXVALUE;
            }
            return ALIGN_GAP.synchronize(context, desire, lat, lmrsData, initiatedLaneChange);
        }

        @Override
        public String name()
        {
            return "ALIGN_GAP_MOVING";
        }
    };

    /**
     * Determine acceleration for synchronization.
     * @param context tactical information such as parameters and car-following model
     * @param desire level of lane change desire
     * @param lat lateral direction for synchronization
     * @param lmrsData LMRS data
     * @param initiatedLaneChange lateral direction of initiated lane change
     * @return acceleration for synchronization
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    Acceleration synchronize(TacticalContextEgo context, double desire, LateralDirectionality lat, LmrsData lmrsData,
            LateralDirectionality initiatedLaneChange) throws ParameterException, OperationalPlanException;

    /**
     * Returns the leader to consider for synchronization based on lane change desire and whether the leader is moving. For
     * {@code d > dCoop} the first leader is taken. Otherwise it is the first moving leader.
     * @param context tactical context
     * @param relativeLane relative lane
     * @param desire lane change desire
     * @return leader to consider for synchronization
     * @throws OperationalPlanException when the neighbors perception category is missing
     * @throws ParameterException when the dCoop parameter is missing
     */
    static PerceivedGtu getProspectiveLeader(final TacticalContextEgo context, final RelativeLane relativeLane,
            final double desire) throws OperationalPlanException, ParameterException
    {
        double dCoop = context.getParameters().getParameter(LmrsParameters.DCOOP);
        PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders =
                context.getPerception().getPerceptionCategory(NeighborsPerception.class).getLeaders(relativeLane);

        if (leaders != null)
        {
            if (desire >= dCoop && !leaders.isEmpty())
            {
                // for dCoop < d take first leader
                return leaders.first();
            }
            else
            {
                // for dSync < d < dCoop take first leader with non-zero speed
                for (PerceivedGtu leader : leaders)
                {
                    if (leader.getSpeed().gt0())
                    {
                        return leader;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the leader upstream of the given leader, which is either a leading vehicle before the leader, or the first
     * following vehicle.
     * @param context tactical context
     * @param relativeLane relative lane
     * @param leader leader
     * @return leader upstream of the given leader
     * @throws OperationalPlanException when the neighbors perception category is missing
     */
    static PerceivedGtu getProspectiveFollower(final TacticalContextEgo context, final RelativeLane relativeLane,
            final PerceivedGtu leader) throws OperationalPlanException
    {
        NeighborsPerception neighbors = context.getPerception().getPerceptionCategory(NeighborsPerception.class);
        if (leader != null)
        {
            PerceivedGtu prevLeader = null;
            for (PerceivedGtu nextLeader : neighbors.getLeaders(relativeLane))
            {
                if (nextLeader.equals(leader))
                {
                    if (prevLeader != null)
                    {
                        return prevLeader;
                    }
                    else
                    {
                        break;
                    }
                }
                prevLeader = nextLeader;
            }
        }
        PerceptionCollectable<PerceivedGtu, LaneBasedGtu> followers = neighbors.getFollowers(relativeLane);
        return followers.isEmpty() ? null : followers.first();
    }

    /**
     * Returns the distance to the next merge, stopping within this distance is futile for a lane change.
     * @param perception perception
     * @param lat lateral direction
     * @return distance to the next merge
     * @throws OperationalPlanException if there is no infrastructure perception
     */
    static Length getMergeDistance(final LanePerception perception, final LateralDirectionality lat)
            throws OperationalPlanException
    {
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        Length dx = perception.getGtu().getFront().dx();
        Length xMergeRef = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, lat);
        if (xMergeRef.gt0() && xMergeRef.lt(dx))
        {
            return Length.ZERO;
        }
        Length xMerge = xMergeRef.minus(dx);
        return xMerge.lt0() ? xMerge.neg() : Length.ZERO; // positive value where lane change is not possible
    }

    /**
     * Returns whether the leader in the target lane is considered to be creeping.
     * @param context tactical context
     * @param desire lane change desire
     * @param lat lateral directionality of lane change desire
     * @return whether the leader in the target lane is considered to be creeping
     * @throws OperationalPlanException when the neighbors perception category is missing
     * @throws ParameterException when the dCoop or vCreep parameter is missing
     */
    static boolean leaderIsCreeping(final TacticalContextEgo context, final double desire, final LateralDirectionality lat)
            throws OperationalPlanException, ParameterException
    {
        PerceivedGtu leader = getProspectiveLeader(context, new RelativeLane(lat, 1), desire);
        return leader != null && leader.getSpeed().si < context.getParameters().getParameter(CREEP_SPEED).si;
    }

}

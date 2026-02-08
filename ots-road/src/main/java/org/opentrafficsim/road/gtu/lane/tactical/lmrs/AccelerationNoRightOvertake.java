package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.FilteredIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Makes a GTU follow leaders in the left lane, with limited deceleration.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class AccelerationNoRightOvertake implements AccelerationIncentive, Stateless<AccelerationNoRightOvertake>
{

    /** Speed threshold below which traffic is considered congested. */
    public static final ParameterTypeSpeed VCONG = ParameterTypes.VCONG;

    /** Maximum adjustment deceleration, e.g. when speed limit drops. */
    public static final ParameterTypeAcceleration B0 = ParameterTypes.B0;

    /** Singleton instance. */
    public static final AccelerationNoRightOvertake SINGLETON = new AccelerationNoRightOvertake();

    @Override
    public AccelerationNoRightOvertake get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private AccelerationNoRightOvertake()
    {
        //
    }

    @Override
    public Acceleration accelerate(final RelativeLane lane, final Length mergeDistance, final LaneBasedGtu gtu,
            final LanePerception perception, final CarFollowingModel carFollowingModel, final Speed speed,
            final Parameters params, final SpeedLimitInfo speedLimitInfo) throws ParameterException, GtuException
    {
        // Ignore incentive if we need to change lane for the route
        if (!lane.isCurrent() || !perception.getLaneStructure().exists(lane.getLeft())
                || (gtu.getTacticalPlanner() instanceof AbstractIncentivesTacticalPlanner planner
                        && planner.getLatestMandatoryDesire().right() > 0.0))
        {
            return NO_REASON;
        }

        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        Length legal = infra.getLegalLaneChangePossibility(RelativeLane.LEFT, LateralDirectionality.RIGHT);
        Length ignore = legal.lt0() ? legal.neg() : Length.ZERO;

        if (lane.isCurrent() && perception.getLaneStructure().exists(RelativeLane.LEFT))
        {
            Speed vCong = params.getParameter(VCONG);
            if (perception.getPerceptionCategory(TrafficPerception.class).getSpeed(RelativeLane.CURRENT).si > vCong.si)
            {
                // TODO depends on left/right traffic
                PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders =
                        perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.LEFT);
                if (!leaders.isEmpty())
                {
                    for (PerceivedGtu leader : ignore.le0() ? leaders
                            : new FilteredIterable<>(leaders, (leader) -> leader.getDistance().gt(ignore)))
                    {
                        if (speed.si > leader.getSpeed().si && leader.getDistance().gt0())
                        {
                            Acceleration b0 = params.getParameter(B0);
                            Acceleration a = CarFollowingUtil.followSingleLeader(carFollowingModel, params, speed,
                                    speedLimitInfo, leader);
                            return a.si < -b0.si ? b0.neg() : a;
                        }
                        else
                        {
                            // Stop when leader is faster than desired speed
                            return NO_REASON;
                        }
                    }
                }
            }
        }
        return NO_REASON;
    }

}

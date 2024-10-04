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
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Makes a GTU follow leaders in the left lane, with limited deceleration.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AccelerationNoRightOvertake implements AccelerationIncentive
{

    /** Speed threshold below which traffic is considered congested. */
    public static final ParameterTypeSpeed VCONG = ParameterTypes.VCONG;

    /** Maximum adjustment deceleration, e.g. when speed limit drops. */
    public static final ParameterTypeAcceleration B0 = ParameterTypes.B0;

    /** {@inheritDoc} */
    @Override
    public void accelerate(final SimpleOperationalPlan simplePlan, final RelativeLane lane, final Length mergeDistance,
            final LaneBasedGtu gtu, final LanePerception perception, final CarFollowingModel carFollowingModel,
            final Speed speed, final Parameters params, final SpeedLimitInfo speedLimitInfo)
            throws OperationalPlanException, ParameterException, GtuException
    {
        // TODO ignore incentive if we need to change lane for the route
        if (lane.isCurrent() && perception.getLaneStructure().exists(RelativeLane.LEFT))
        {
            Speed vCong = params.getParameter(VCONG);
            if (perception.getPerceptionCategory(TrafficPerception.class).getSpeed(RelativeLane.CURRENT).si > vCong.si)
            {
                PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders =
                        perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.LEFT);
                if (!leaders.isEmpty())
                {
                    HeadwayGtu leader = leaders.first();
                    Speed desiredSpeed = perception.getGtu().getDesiredSpeed();
                    if (desiredSpeed.si > leader.getSpeed().si)
                    {
                        Acceleration b0 = params.getParameter(B0);
                        // TODO only sensible if the left leader can change right; add this info to HeadwayGtu?
                        Acceleration a =
                                CarFollowingUtil.followSingleLeader(carFollowingModel, params, speed, speedLimitInfo, leader);
                        simplePlan.minimizeAcceleration(a.si < -b0.si ? b0.neg() : a);
                    }
                }
            }
        }
    }

}

package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 mrt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class AccelerationNoRightOvertake implements AccelerationIncentive
{

    /** Speed threshold below which traffic is considered congested. */
    public final static ParameterTypeSpeed VCONG = ParameterTypes.VCONG;

    /** Maximum adjustment deceleration, e.g. when speed limit drops. */
    public final static ParameterTypeAcceleration B0 = ParameterTypes.B0;

    /** {@inheritDoc} */
    @Override
    public void accelerate(final SimpleOperationalPlan simplePlan, final RelativeLane lane, final LaneBasedGTU gtu,
            final LanePerception perception, final CarFollowingModel carFollowingModel, final Speed speed,
            final Parameters params, final SpeedLimitInfo speedLimitInfo)
            throws OperationalPlanException, ParameterException, GTUException
    {
        // TODO ignore incentive if we need to change lane for the route
        if (lane.isCurrent() && perception.getLaneStructure().getCrossSection().contains(RelativeLane.LEFT))
        {
            Speed vCong = params.getParameter(VCONG);
            if (perception.getPerceptionCategory(TrafficPerception.class).getSpeed(RelativeLane.CURRENT).si > vCong.si)
            {
                PerceptionCollectable<HeadwayGTU, LaneBasedGTU> leaders =
                        perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.LEFT);
                if (!leaders.isEmpty())
                {
                    HeadwayGTU leader = leaders.first();
                    Speed desiredSpeed = perception.getGtu().getDesiredSpeed();
                    if (desiredSpeed.si > leader.getSpeed().si)
                    {
                        Acceleration b0 = params.getParameter(B0);
                        // TODO only sensible if the left leader can change right; add this info to HeadwayGTU?
                        Acceleration a =
                                CarFollowingUtil.followSingleLeader(carFollowingModel, params, speed, speedLimitInfo, leader);
                        simplePlan.minimizeAcceleration(a.si < -b0.si ? b0.neg() : a);
                    }
                }
            }
        }
    }

}

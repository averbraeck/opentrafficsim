package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectIntersectionPerception;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.TrafficLightUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class AccelerationTrafficLights implements AccelerationIncentive
{

    /** {@inheritDoc} */
    @Override
    public void accelerate(final SimpleOperationalPlan simplePlan, final RelativeLane lane, final LaneBasedGTU gtu,
            final LanePerception perception, final CarFollowingModel carFollowingModel, final Speed speed,
            final BehavioralCharacteristics bc, final SpeedLimitInfo speedLimitInfo)
            throws ParameterException, OperationalPlanException
    {
        simplePlan.minimizeAcceleration(TrafficLightUtil.respondToTrafficLights(bc,
                perception.getPerceptionCategory(DirectIntersectionPerception.class).getTrafficLights(lane), carFollowingModel, speed,
                speedLimitInfo));
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "AccelerationTrafficLights";
    }

}

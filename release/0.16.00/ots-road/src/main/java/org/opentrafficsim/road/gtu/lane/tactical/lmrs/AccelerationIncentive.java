package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface AccelerationIncentive
{

    /**
     * Determine acceleration.
     * @param simplePlan simple plan to set the acceleration
     * @param lane lane on which to consider the acceleration
     * @param gtu gtu
     * @param perception perception
     * @param carFollowingModel car-following model
     * @param speed current speed
     * @param params parameters
     * @param speedLimitInfo speed limit info
     * @throws OperationalPlanException in case of an error
     * @throws ParameterException on missing parameter
     * @throws GTUException when there is a problem with the state of the GTU when planning a path
     */
    void accelerate(SimpleOperationalPlan simplePlan, RelativeLane lane, LaneBasedGTU gtu, LanePerception perception,
            CarFollowingModel carFollowingModel, Speed speed, Parameters params, SpeedLimitInfo speedLimitInfo)
            throws OperationalPlanException, ParameterException, GTUException;

}

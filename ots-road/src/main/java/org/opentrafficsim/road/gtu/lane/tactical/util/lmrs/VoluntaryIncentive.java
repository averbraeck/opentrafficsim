package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;

/**
 * Interface for voluntary incentives.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface VoluntaryIncentive extends Incentive
{

    /**
     * Determines level of lane change desire for a lane change incentive.
     * @param parameters parameters
     * @param perception perception
     * @param carFollowingModel car-following model
     * @param mandatoryDesire level of mandatory desire at current time
     * @param voluntaryDesire level of voluntary desire at current time, of voluntary incentives calculated before
     * @return level of lane change desire for this incentive
     * @throws ParameterException if a parameter is not given or out of bounds
     * @throws OperationalPlanException in case of a perception exception
     */
    Desire determineDesire(Parameters parameters, LanePerception perception, CarFollowingModel carFollowingModel,
            Desire mandatoryDesire, Desire voluntaryDesire) throws ParameterException, OperationalPlanException;

}

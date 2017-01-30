package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
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
public interface AccelerationIncentive
{

    /**
     * Determine acceleration.
     * @param gtu gtu
     * @param perception perception
     * @param carFollowingModel car-following model
     * @param speed current speed
     * @param bc behavioral characteristics
     * @param speedLimitInfo speed limit info
     * @return acceleration
     * @throws OperationalPlanException in case of an error
     * @throws ParameterException on missing parameter
     * @throws GTUException when there is a problem with the state of the GTU when planning a path
     */
    Acceleration acceleration(LaneBasedGTU gtu, LanePerception perception, CarFollowingModel carFollowingModel, Speed speed,
            BehavioralCharacteristics bc, SpeedLimitInfo speedLimitInfo)
            throws OperationalPlanException, ParameterException, GTUException;

}

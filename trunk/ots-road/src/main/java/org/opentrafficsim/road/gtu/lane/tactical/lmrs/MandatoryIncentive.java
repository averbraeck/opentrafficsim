package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Interface for mandatory incentives.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface MandatoryIncentive
{

    /**
     * Determines level of lane change desire for a lane change incentive.
     * @param gtu GTU to determine the lane change desire for.
     * @param mandatoryDesire level of mandatory desire at current time
     * @return level of lane change desire for this incentive
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    Desire determineDesire(LaneBasedGTU gtu, Desire mandatoryDesire) throws ParameterException;

}

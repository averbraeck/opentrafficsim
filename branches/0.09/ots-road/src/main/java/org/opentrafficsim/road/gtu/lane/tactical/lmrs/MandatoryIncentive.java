package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Interface for lane change incentives that determine a level of lane change desire. Different incentives may determine lane
 * change desire, which the lane change model combines in a total lane change desire.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface MandatoryIncentive
{

    /**
     * Determines level of lane change desire for a lane change incentive.
     * @param gtu GTU to determine the lane change desire for.
     * @param perception Perception which supplies the situation.
     * @return Level of lane change desire for this incentive.
     */
    Desire determineDesire(LaneBasedGTU gtu, LanePerception perception);

}

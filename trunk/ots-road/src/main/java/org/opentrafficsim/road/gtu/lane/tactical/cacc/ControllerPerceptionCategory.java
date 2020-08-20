package org.opentrafficsim.road.gtu.lane.tactical.cacc;

import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.DownstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.LaneBasedPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * Interface defining what controller perception can deliver. Implementation determine how, i.e. with what noise.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 sep. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface ControllerPerceptionCategory extends LaneBasedPerceptionCategory
{

    /**
     * Get the leaders.
     * @param lane RelativeLane; lane
     * @return DownstreamNeighborsIterable; leaders
     */
    DownstreamNeighborsIterable getLeaders(RelativeLane lane);

    /**
     * Get the leader in an adjacent lane.
     * @param lat LateralDirectionality; lateral direction
     * @return leader in adjacent lane
     */
    HeadwayGTU getLeader(LateralDirectionality lat);

    /**
     * Get the follower in an adjacent lane.
     * @param lat LateralDirectionality; lateral direction
     * @return follower in adjacent lane
     */
    HeadwayGTU getFollower(LateralDirectionality lat);

}

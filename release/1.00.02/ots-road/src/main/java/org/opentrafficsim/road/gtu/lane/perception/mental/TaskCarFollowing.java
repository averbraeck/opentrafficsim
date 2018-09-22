package org.opentrafficsim.road.gtu.lane.perception.mental;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;

/**
 * Task demand for car-following.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TaskCarFollowing extends TaskHeadwayBased
{

    /** {@inheritDoc} */
    @Override
    protected Duration getHeadway(final LanePerception perception, final LaneBasedGTU gtu, final Parameters parameters)
    {
        NeighborsPerception neighbors = Try.assign(() -> perception.getPerceptionCategory(NeighborsPerception.class),
                "NeighborsPerception not available.");
        Try.execute(() -> neighbors.updateLeaders(RelativeLane.CURRENT), "Exception while updating adjacent leaders.");
        return neighbors.getLeaders(RelativeLane.CURRENT).collect(new TaskHeadwayCollector(getSpeed()));
    }

}

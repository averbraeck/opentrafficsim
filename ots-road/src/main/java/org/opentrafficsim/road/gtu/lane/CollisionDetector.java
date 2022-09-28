package org.opentrafficsim.road.gtu.lane;

import java.util.Iterator;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.UnderlyingDistance;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * Checks for collisions.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 6, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CollisionDetector extends AbstractLaneBasedMoveChecker
{

    /**
     * Constructor.
     * @param network OTSNetwork; network
     */
    public CollisionDetector(final OTSNetwork network)
    {
        super(network);
    }

    /** {@inheritDoc} */
    @Override
    public void checkMove(final LaneBasedGTU gtu) throws Exception
    {
        try
        {
            NeighborsPerception neighbors =
                    gtu.getTacticalPlanner().getPerception().getPerceptionCategory(NeighborsPerception.class);
            PerceptionCollectable<HeadwayGTU, LaneBasedGTU> leaders = neighbors.getLeaders(RelativeLane.CURRENT);
            Iterator<UnderlyingDistance<LaneBasedGTU>> gtus = leaders.underlyingWithDistance();
            if (!gtus.hasNext())
            {
                return;
            }
            UnderlyingDistance<LaneBasedGTU> leader = gtus.next();
            if (leader.getDistance().lt0())
            {
                throw new CollisionException("GTU " + gtu.getId() + " collided with GTU " + leader.getObject().getId());
            }
        }
        catch (OperationalPlanException exception)
        {
            throw new GTUException(exception);
        }
    }

}

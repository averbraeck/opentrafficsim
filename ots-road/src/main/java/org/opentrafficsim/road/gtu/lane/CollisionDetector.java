package org.opentrafficsim.road.gtu.lane;

import java.util.Iterator;

import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.OtsNetwork;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.UnderlyingDistance;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;

/**
 * Checks for collisions.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class CollisionDetector extends AbstractLaneBasedMoveChecker
{

    /**
     * Constructor.
     * @param network OtsNetwork; network
     */
    public CollisionDetector(final OtsNetwork network)
    {
        super(network);
    }

    /** {@inheritDoc} */
    @Override
    public void checkMove(final LaneBasedGtu gtu) throws Exception
    {
        try
        {
            NeighborsPerception neighbors =
                    gtu.getTacticalPlanner().getPerception().getPerceptionCategory(NeighborsPerception.class);
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders = neighbors.getLeaders(RelativeLane.CURRENT);
            Iterator<UnderlyingDistance<LaneBasedGtu>> gtus = leaders.underlyingWithDistance();
            if (!gtus.hasNext())
            {
                return;
            }
            UnderlyingDistance<LaneBasedGtu> leader = gtus.next();
            if (leader.getDistance().lt0())
            {
                throw new CollisionException("GTU " + gtu.getId() + " collided with GTU " + leader.getObject().getId());
            }
        }
        catch (OperationalPlanException exception)
        {
            throw new GtuException(exception);
        }
    }

}

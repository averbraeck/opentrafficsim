package org.opentrafficsim.road.gtu.lane;

import java.util.Iterator;

import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.UnderlyingDistance;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;

/**
 * Checks for collisions.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CollisionDetector extends AbstractLaneBasedMoveChecker
{

    /**
     * Constructor.
     * @param network network
     */
    public CollisionDetector(final Network network)
    {
        super(network);
    }

    @Override
    public void checkMove(final LaneBasedGtu gtu) throws Exception
    {
        try
        {
            NeighborsPerception neighbors =
                    gtu.getTacticalPlanner().getPerception().getPerceptionCategory(NeighborsPerception.class);
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neighbors.getLeaders(RelativeLane.CURRENT);
            Iterator<UnderlyingDistance<LaneBasedGtu>> gtus = leaders.underlyingWithDistance();
            if (!gtus.hasNext())
            {
                return;
            }
            UnderlyingDistance<LaneBasedGtu> leader = gtus.next();
            if (leader.distance().lt0())
            {
                throw new CollisionException("GTU " + gtu.getId() + " collided with GTU " + leader.object().getId());
            }
        }
        catch (OperationalPlanException exception)
        {
            throw new GtuException(exception);
        }
    }

}

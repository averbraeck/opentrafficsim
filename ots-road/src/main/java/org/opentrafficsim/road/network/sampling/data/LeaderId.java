package org.opentrafficsim.road.network.sampling.data;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Leader id in trajectory information.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LeaderId extends ExtendedDataString<GtuDataRoad>
{

    /**
     * Constructor.
     */
    public LeaderId()
    {
        super("leaderId", "Id of the leading GTU");
    }

    @Override
    public Optional<String> getValue(final GtuDataRoad gtu)
    {
        Optional<NeighborsPerception> neigbors =
                gtu.getGtu().getTacticalPlanner().getPerception().getPerceptionCategoryOptional(NeighborsPerception.class);
        if (neigbors.isEmpty())
        {
            throw new NoSuchElementException(
                    "Leader id can only be stored in trajectories if the GTU's have NeighborsPerception.");
        }
        Iterator<LaneBasedGtu> it = neigbors.get().getLeaders(RelativeLane.CURRENT).underlying();
        return Optional.ofNullable(it.hasNext() ? it.next().getId() : "");
    }

}

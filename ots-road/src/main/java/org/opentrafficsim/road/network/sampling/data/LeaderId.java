package org.opentrafficsim.road.network.sampling.data;

import java.util.Iterator;

import org.opentrafficsim.kpi.sampling.data.ExtendedDataTypeString;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.network.sampling.GtuData;

/**
 * Leader id in trajectory information.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LeaderId extends ExtendedDataTypeString<GtuData>
{

    /**
     * Constructor.
     */
    public LeaderId()
    {
        super("leaderId");
    }

    /** {@inheritDoc} */
    @Override
    public String getValue(final GtuData gtu)
    {
        NeighborsPerception neigbors =
                gtu.getGtu().getTacticalPlanner().getPerception().getPerceptionCategoryOrNull(NeighborsPerception.class);
        if (neigbors == null)
        {
            throw new RuntimeException("Leader id can only be stored in trajectories if the GTU's have NeighborsPerception.");
        }
        Iterator<LaneBasedGtu> it = neigbors.getLeaders(RelativeLane.CURRENT).underlying();
        return it.hasNext() ? it.next().getId() : "";
    }

}

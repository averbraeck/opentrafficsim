package org.opentrafficsim.road.gtu.lane.tactical;

import java.io.Serializable;
import java.util.Set;

import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * This class provides information for an operational plan about the next location where the network splits. if the networks
 * splits, the node where it splits, and the current lanes that lead to the right node are calculated.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param nextSplitNode the first subsequent node at which the route splits.
 * @param correctCurrentLanes the lane(s) and/or adjacent lane(s) on which the reference point of the GTU is registered that
 *            lead us in the direction of the route provided by the strategical planner.
 * @param requiredDirection required direction for lane changes for this split, beyond lane on current link
 */
public record NextSplitInfo(Node nextSplitNode, Set<Lane> correctCurrentLanes, LateralDirectionality requiredDirection)
        implements Serializable
{

    /** */
    private static final long serialVersionUID = 20151231L;

    /**
     * Constructor.
     * @param nextSplitNode the first subsequent node at which the route splits.
     * @param correctCurrentLanes the lane(s) and/or adjacent lane(s) on which the reference point of the GTU is registered that
     *            lead us in the direction of the route provided by the strategical planner.
     */
    public NextSplitInfo(final Node nextSplitNode, final Set<Lane> correctCurrentLanes)
    {
        this(nextSplitNode, correctCurrentLanes, null);
    }

    /**
     * Return whether this is a split.
     * @return split indicates whether the route splits within the given distance.
     */
    public final boolean isSplit()
    {
        return this.nextSplitNode != null;
    }

}

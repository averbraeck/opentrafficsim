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
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class NextSplitInfo implements Serializable
{
    /** */
    private static final long serialVersionUID = 20151231L;

    /** If the route splits, at what node does it split? */
    private final Node nextSplitNode;

    /** Required direction. */
    private final LateralDirectionality requiredDirection;

    /**
     * If the route splits, what are the lane(s) and/or adjacent lane(s) on which the reference point of the GTU is registered
     * that lead us in the direction of the route provided by the strategical planner.
     */
    private final Set<Lane> correctCurrentLanes;

    /**
     * @param nextSplitNode Node; the first subsequent node at which the route splits.
     * @param correctCurrentLanes Set&lt;Lane&gt;; the lane(s) and/or adjacent lane(s) on which the reference point of the GTU
     *            is registered that lead us in the direction of the route provided by the strategical planner.
     */
    public NextSplitInfo(final Node nextSplitNode, final Set<Lane> correctCurrentLanes)
    {
        this(nextSplitNode, correctCurrentLanes, null);
    }

    /**
     * @param nextSplitNode Node; the first subsequent node at which the route splits.
     * @param correctCurrentLanes Set&lt;Lane&gt;; the lane(s) and/or adjacent lane(s) on which the reference point of the GTU
     *            is registered that lead us in the direction of the route provided by the strategical planner.
     * @param requiredDirection LateralDirectionality; required direction for lane changes for this split, beyond lane on
     *            current link
     */
    public NextSplitInfo(final Node nextSplitNode, final Set<Lane> correctCurrentLanes,
            final LateralDirectionality requiredDirection)
    {
        this.nextSplitNode = nextSplitNode;
        this.correctCurrentLanes = correctCurrentLanes;
        this.requiredDirection = requiredDirection;
    }

    /**
     * @return split indicates whether the route splits within the given distance.
     */
    public final boolean isSplit()
    {
        return this.nextSplitNode != null;
    }

    /**
     * @return nextSplitNode the first subsequent node at which the route splits.
     */
    public final Node getNextSplitNode()
    {
        return this.nextSplitNode;
    }

    /**
     * @return correctCurrentLanes the lane(s) and/or adjacent lane(s) on which the reference point of the GTU is registered
     *         that lead us in the direction of the route provided by the strategical planner.
     */
    public final Set<Lane> getCorrectCurrentLanes()
    {
        return this.correctCurrentLanes;
    }

    /**
     * @return requiredDirection.
     */
    public final LateralDirectionality getRequiredDirection()
    {
        return this.requiredDirection;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "NextSplitInfo [nextSplitNode=" + this.nextSplitNode + ", correctCurrentLanes=" + this.correctCurrentLanes
                + ", requiredDirection=" + this.requiredDirection + "]";
    }
}

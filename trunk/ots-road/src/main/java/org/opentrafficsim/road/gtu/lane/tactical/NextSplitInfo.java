package org.opentrafficsim.road.gtu.lane.tactical;

import java.io.Serializable;
import java.util.Set;

import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * This class provides information for an operational plan about the next location where the network splits. if the networks
 * splits, the node where it splits, and the current lanes that lead to the right node are calculated.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 31, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class NextSplitInfo implements Serializable
{
    /** */
    private static final long serialVersionUID = 20151231L;

    /** If the route splits, at what node does it split? */
    private final Node nextSplitNode;
    
    /**
     * If the route splits, what are the lane(s) and/or adjacent lane(s) on which the reference point of the GTU is registered
     * that lead us in the direction of the route provided by the strategical planner.
     */
    private final Set<Lane> correctCurrentLanes;

    /**
     * @param nextSplitNode the first subsequent node at which the route splits.
     * @param correctCurrentLanes the lane(s) and/or adjacent lane(s) on which the reference point of the GTU is registered that
     *            lead us in the direction of the route provided by the strategical planner.
     */
    public NextSplitInfo(final Node nextSplitNode, final Set<Lane> correctCurrentLanes)
    {
        super();
        this.nextSplitNode = nextSplitNode;
        this.correctCurrentLanes = correctCurrentLanes;
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "NextSplitInfo [nextSplitNode=" + this.nextSplitNode + ", correctCurrentLanes=" + this.correctCurrentLanes + "]";
    }
}

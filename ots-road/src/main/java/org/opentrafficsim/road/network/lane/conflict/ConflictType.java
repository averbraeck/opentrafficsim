package org.opentrafficsim.road.network.lane.conflict;

/**
 * Type of conflict. This regards how the conflict is spatially correlated to the other traffic.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public enum ConflictType
{

    /** Crossing conflict. */
    CROSSING,

    /** Merge conflict. */
    MERGE,

    /** Split conflict. */
    SPLIT;

    /**
     * Returns whether this is a crossing.
     * @return whether this is a crossing
     */
    public boolean isCrossing()
    {
        return this.equals(CROSSING);
    }

    /**
     * Returns whether this is a merge.
     * @return whether this is a merge
     */
    public boolean isMerge()
    {
        return this.equals(MERGE);
    }

    /**
     * Returns whether this is a split.
     * @return whether this is a split
     */
    public boolean isSplit()
    {
        return this.equals(SPLIT);
    }
}

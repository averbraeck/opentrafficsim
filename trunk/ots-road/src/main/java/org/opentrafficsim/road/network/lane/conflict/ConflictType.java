package org.opentrafficsim.road.network.lane.conflict;

/**
 * Type of conflict. This regards how the conflict is spatially correlated to the other traffic.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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

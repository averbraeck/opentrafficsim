package org.opentrafficsim.road.network.lane.conflict;

/**
 * Priority of conflict. This tells a GTU how to respond to the conflict. Whether a GTU has priority or not may come from any
 * conflict rule. This only represents the resulting priority.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public enum ConflictPriority
{
    /** Have priority. */
    PRIORITY,

    /** Turn on red. */
    TURN_ON_RED,

    /** Yield. */
    YIELD,

    /** Stop and give priority. */
    STOP,

    /** All-way stop. */
    ALL_STOP,

    /** Split. */
    SPLIT;

    /**
     * Returns whether this is a priority conflict.
     * @return whether this is a priority conflict
     */
    public final boolean isPriority()
    {
        return this.equals(PRIORITY);
    }

    /**
     * Returns whether this is a turn on red conflict.
     * @return whether this is a turn on red conflict
     */
    public final boolean isTurnOnRed()
    {
        return this.equals(TURN_ON_RED);
    }

    /**
     * Returns whether this is a give-way conflict.
     * @return whether this is a give-way conflict
     */
    public final boolean isGiveWay()
    {
        return this.equals(YIELD);
    }

    /**
     * Returns whether this is a stop conflict.
     * @return whether this is a stop conflict
     */
    public final boolean isStop()
    {
        return this.equals(STOP);
    }

    /**
     * Returns whether this is an all-stop conflict.
     * @return whether this is an all-stop conflict
     */
    public final boolean isAllStop()
    {
        return this.equals(ALL_STOP);
    }

    /**
     * Returns whether this is a stop conflict.
     * @return whether this is a stop conflict
     */
    public final boolean isSplit()
    {
        return this.equals(SPLIT);
    }

}

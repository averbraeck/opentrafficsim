package org.opentrafficsim.road.network.lane.conflict;

/**
 * Rule of conflict. This tells a GTU how to respond to the conflict. Whether a GTU has priority or not may come from any 
 * traffic rule. This only represents the resulting priority.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public enum ConflictRule
{
    /** Have priority. */
    PRIORITY,

    /** Give priority. */
    GIVE_WAY,

    /** Stop and give priority. */
    STOP,

    /** All-way stop. */
    ALL_STOP,
    
    /** Split. */
    SPLIT;
}

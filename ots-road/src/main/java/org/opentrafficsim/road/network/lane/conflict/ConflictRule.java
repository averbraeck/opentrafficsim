package org.opentrafficsim.road.network.lane.conflict;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

/**
 * A conflict rule provides the conflict priority.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface ConflictRule
{

    /**
     * Determines the priority for the given conflict.
     * @param conflict Conflict; conflict
     * @return priority for the given conflict
     */
    ConflictPriority determinePriority(Conflict conflict);

    /**
     * Creates a clone of the conflict rule.
     * @param newSimulator OTSSimulatorInterface; new simulator
     * @return clone of the conflict rule
     */
    ConflictRule clone(OTSSimulatorInterface newSimulator);

}

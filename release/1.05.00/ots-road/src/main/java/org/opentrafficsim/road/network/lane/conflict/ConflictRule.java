package org.opentrafficsim.road.network.lane.conflict;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * A conflict rule provides the conflict priority.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 26 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
     * @param newSimulator SimulatorInterface.TimeDoubleUnit; new simulator
     * @return clone of the conflict rule
     */
    ConflictRule clone(SimulatorInterface.TimeDoubleUnit newSimulator);

}

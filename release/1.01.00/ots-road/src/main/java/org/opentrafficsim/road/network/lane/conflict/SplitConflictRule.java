package org.opentrafficsim.road.network.lane.conflict;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Conflict rule for split conflicts.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SplitConflictRule implements ConflictRule
{

    /** {@inheritDoc} */
    @Override
    public final ConflictPriority determinePriority(final Conflict conflict)
    {
        return ConflictPriority.SPLIT;
    }

    /** {@inheritDoc} */
    @Override
    public ConflictRule clone(final SimulatorInterface.TimeDoubleUnit newSimulator)
    {
        // stateless so no copy required
        return this;
    }

}

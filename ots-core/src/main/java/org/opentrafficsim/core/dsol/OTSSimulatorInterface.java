package org.opentrafficsim.core.dsol;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Typed extension of the SimulatorInterface without remote exceptions.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface OTSSimulatorInterface extends
    SimulatorInterface<Time, Duration, OTSSimTimeDouble>
{
    /** {@inheritDoc} */
    @Override
    OTSSimTimeDouble getSimulatorTime();

    /** {@inheritDoc} */
    @Override
    void initialize(Replication<Time, Duration, OTSSimTimeDouble> replication,
        ReplicationMode replicationMode) throws SimRuntimeException;

    /** {@inheritDoc} */
    @Override
    boolean isRunning();

    /** {@inheritDoc} */
    @Override
    void start() throws SimRuntimeException;

    /** {@inheritDoc} */
    @Override
    void step() throws SimRuntimeException;

    /** {@inheritDoc} */
    @Override
    void stop() throws SimRuntimeException;

}

package org.opentrafficsim.simulationengine;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;

import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;

/**
 * Construct a DSOL DEVSSimulator or DEVSAnimator the easy way.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 12 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SimpleSimulator extends OTSDEVSSimulator implements SimpleSimulatorInterface
{
    /** */
    private static final long serialVersionUID = 20150510L;

    /** Counter for replication. */
    private int lastReplication = 0;

    /**
     * Create a simulation engine without animation; the easy way. PauseOnError is set to true;
     * @param startTime OTSSimTimeDouble; the start time of the simulation
     * @param warmupPeriod DoubleScalar.Rel&lt;TimeUnit&gt;; the warm up period of the simulation (use new
     *            DoubleScalar.Rel&lt;TimeUnit&gt;(0, SECOND) if you don't know what this is)
     * @param runLength DoubleScalar.Rel&lt;TimeUnit&gt;; the duration of the simulation
     * @param model OTSModelInterface; the simulation to execute
     * @throws RemoteException on communications failure
     * @throws SimRuntimeException on ???
     * @throws NamingException when the context for the replication cannot be created
     */
    public SimpleSimulator(final Time.Abs startTime, final Time.Rel warmupPeriod, final Time.Rel runLength,
        final OTSModelInterface model) throws RemoteException, SimRuntimeException, NamingException
    {
        setPauseOnError(true);
        initialize(new OTSReplication("rep" + ++this.lastReplication, new OTSSimTimeDouble(startTime), warmupPeriod,
            runLength, model), ReplicationMode.TERMINATING);
    }

    /**
     * {@inheritDoc}
     */
    public final SimEvent<OTSSimTimeDouble> scheduleEvent(final Time.Abs executionTime, final short priority,
        final Object source, final Object target, final String method, final Object[] args) throws SimRuntimeException
    {
        SimEvent<OTSSimTimeDouble> result =
            new SimEvent<OTSSimTimeDouble>(new OTSSimTimeDouble(new Time.Abs(executionTime.getSI(), SECOND)), priority,
                source, target, method, args);
        scheduleEvent(result);
        return result;
    }

}

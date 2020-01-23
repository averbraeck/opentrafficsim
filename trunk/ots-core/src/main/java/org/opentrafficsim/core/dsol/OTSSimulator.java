package org.opentrafficsim.core.dsol;

import java.io.Serializable;

import javax.naming.NamingException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;

/**
 * Construct a DSOL DEVSSimulator the easy way.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-10-30 14:03:57 +0100 (Tue, 30 Oct 2018) $, @version $Revision: 4727 $, by $Author: pknoppers $,
 * initial version 12 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSSimulator extends DEVSSimulator.TimeDoubleUnit implements OTSSimulatorInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150510L;

    /** Counter for replication. */
    private int lastReplication = 0;

    /**
     * Construct an OTSSimulator.
     */
    public OTSSimulator()
    {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model) throws SimRuntimeException, NamingException
    {
        setPauseOnError(true);
        OTSReplication newReplication =
                OTSReplication.create("rep" + ++this.lastReplication, startTime, warmupPeriod, runLength, model);
        super.initialize(newReplication, ReplicationMode.TERMINATING);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model, final int replicationNr) throws SimRuntimeException, NamingException
    {
        setPauseOnError(true);
        OTSReplication newReplication = OTSReplication.create("rep" + replicationNr, startTime, warmupPeriod, runLength, model);
        super.initialize(newReplication, ReplicationMode.TERMINATING);
    }

    /** {@inheritDoc} */
    @Override
    public final SimEvent<SimTimeDoubleUnit> scheduleEvent(final Time executionTime, final short priority, final Object source,
            final Object target, final String method, final Object[] args) throws SimRuntimeException
    {
        SimEvent<SimTimeDoubleUnit> result = new SimEvent<>(
                new SimTimeDoubleUnit(new Time(executionTime.getSI(), TimeUnit.DEFAULT)), priority, source, target, method, args);
        scheduleEvent(result);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public OTSReplication getReplication()
    {
        return (OTSReplication) super.getReplication();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "OTSSimulator [lastReplication=" + this.lastReplication + "]";
    }

}

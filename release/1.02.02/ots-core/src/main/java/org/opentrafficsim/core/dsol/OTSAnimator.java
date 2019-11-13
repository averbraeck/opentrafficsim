package org.opentrafficsim.core.dsol;

import java.io.Serializable;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeClock;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Construct a DSOL DEVSRealTimeClock the easy way.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-10-30 14:03:57 +0100 (Tue, 30 Oct 2018) $, @version $Revision: 4727 $, by $Author: pknoppers $,
 * initial version 11 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSAnimator extends DEVSRealTimeClock.TimeDoubleUnit implements OTSAnimatorInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150511L;

    /** Counter for replication. */
    private int lastReplication = 0;

    /**
     * Construct an OTSAnimator.
     */
    public OTSAnimator()
    {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model) throws SimRuntimeException, NamingException
    {
        setPauseOnError(true);
        setAnimationDelay(20); // 50 Hz animation update
        OTSReplication newReplication =
                OTSReplication.create("rep" + ++this.lastReplication, startTime, warmupPeriod, runLength, model);
        super.initialize(newReplication, ReplicationMode.TERMINATING);
    }
    
    /**
     * Initialize a simulation engine without animation; the easy way. PauseOnError is set to true;
     * @param startTime Time; the start time of the simulation
     * @param warmupPeriod Duration; the warm up period of the simulation (use new Duration(0, SECOND) if you don't know what
     *            this is)
     * @param runLength Duration; the duration of the simulation
     * @param model OTSModelInterface; the simulation to execute
     * @param streams Map&lt;String, StreamInterface&gt;; streams
     * @throws SimRuntimeException when e.g., warmupPeriod is larger than runLength
     * @throws NamingException when the context for the replication cannot be created
     */
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model, final Map<String, StreamInterface> streams) throws SimRuntimeException, NamingException
    {
        setPauseOnError(true);
        setAnimationDelay(20); // 50 Hz animation update
        OTSReplication newReplication =
                OTSReplication.create("rep" + ++this.lastReplication, startTime, warmupPeriod, runLength, model);
        newReplication.getStreams().putAll(streams);
        super.initialize(newReplication, ReplicationMode.TERMINATING);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model, final int replicationnr) throws SimRuntimeException, NamingException
    {
        setPauseOnError(true);
        setAnimationDelay(20); // 50 Hz animation update
        OTSReplication newReplication = OTSReplication.create("rep" + replicationnr, startTime, warmupPeriod, runLength, model);
        super.initialize(newReplication, ReplicationMode.TERMINATING);
    }

    /** {@inheritDoc} */
    @Override
    public final SimEvent<SimTimeDoubleUnit> scheduleEvent(final Time executionTime, final short priority, final Object source,
            final Object target, final String method, final Object[] args) throws SimRuntimeException
    {
        SimEvent<SimTimeDoubleUnit> result = new SimEvent<>(
                new SimTimeDoubleUnit(new Time(executionTime.getSI(), TimeUnit.BASE)), priority, source, target, method, args);
        scheduleEvent(result);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSReplication getReplication()
    {
        return (OTSReplication) super.getReplication();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "OTSAnimator [lastReplication=" + this.lastReplication + "]";
    }
}

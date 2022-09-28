package org.opentrafficsim.core.dsol;

import java.io.Serializable;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeAnimator;
import nl.tudelft.simulation.dsol.simulators.ErrorStrategy;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Construct a DSOL DEVSRealTimeAnimator the easy way.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-10-30 14:03:57 +0100 (Tue, 30 Oct 2018) $, @version $Revision: 4727 $, by $Author: pknoppers $,
 * initial version 11 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSAnimator extends DEVSRealTimeAnimator<Duration> implements OTSAnimatorInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150511L;

    /** Counter for replication. */
    private int lastReplication = 0;

    /**
     * Construct an OTSAnimator.
     * @param simulatorId the id of the simulator to use in remote communication
     */
    public OTSAnimator(final Serializable simulatorId)
    {
        super(simulatorId);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final OTSModelInterface model, final OTSReplication replication) throws SimRuntimeException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        setAnimationDelay(20); // 50 Hz animation update
        super.initialize(model, replication);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model) throws SimRuntimeException, NamingException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        setAnimationDelay(20); // 50 Hz animation update
        OTSReplication newReplication = new OTSReplication("rep" + ++this.lastReplication, startTime, warmupPeriod, runLength);
        super.initialize(model, newReplication);
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
            final OTSModelInterface model, final Map<String, StreamInterface> streams)
            throws SimRuntimeException, NamingException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        setAnimationDelay(20); // 50 Hz animation update
        OTSReplication newReplication = new OTSReplication("rep" + ++this.lastReplication, startTime, warmupPeriod, runLength);
        model.getStreams().putAll(streams);
        super.initialize(model, newReplication);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model, final int replicationnr) throws SimRuntimeException, NamingException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        setAnimationDelay(20); // 50 Hz animation update
        OTSReplication newReplication = new OTSReplication("rep" + replicationnr, startTime, warmupPeriod, runLength);
        super.initialize(model, newReplication);
    }

    /** {@inheritDoc} */
    @Override
    protected Duration simulatorTimeForWallClockMillis(final double wallMilliseconds)
    {
        return new Duration(wallMilliseconds, DurationUnit.MILLISECOND);
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

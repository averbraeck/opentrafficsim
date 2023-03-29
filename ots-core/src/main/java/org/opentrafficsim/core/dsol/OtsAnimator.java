package org.opentrafficsim.core.dsol;

import java.io.Serializable;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DevsRealTimeAnimator;
import nl.tudelft.simulation.dsol.simulators.ErrorStrategy;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Construct a DSOL DevsRealTimeAnimator the easy way.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class OtsAnimator extends DevsRealTimeAnimator<Duration> implements OtsAnimatorInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150511L;

    /** Counter for replication. */
    private int lastReplication = 0;

    /**
     * Construct an OTSAnimator.
     * @param simulatorId the id of the simulator to use in remote communication
     */
    public OtsAnimator(final Serializable simulatorId)
    {
        super(simulatorId);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final OtsModelInterface model, final OtsReplication replication) throws SimRuntimeException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        setAnimationDelay(20); // 50 Hz animation update
        super.initialize(model, replication);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OtsModelInterface model) throws SimRuntimeException, NamingException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        setAnimationDelay(20); // 50 Hz animation update
        OtsReplication newReplication = new OtsReplication("rep" + ++this.lastReplication, startTime, warmupPeriod, runLength);
        super.initialize(model, newReplication);
    }

    /**
     * Initialize a simulation engine without animation; the easy way. PauseOnError is set to true;
     * @param startTime Time; the start time of the simulation
     * @param warmupPeriod Duration; the warm up period of the simulation (use new Duration(0, SECOND) if you don't know what
     *            this is)
     * @param runLength Duration; the duration of the simulation
     * @param model OtsModelInterface; the simulation to execute
     * @param streams Map&lt;String, StreamInterface&gt;; streams
     * @throws SimRuntimeException when e.g., warmupPeriod is larger than runLength
     * @throws NamingException when the context for the replication cannot be created
     */
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OtsModelInterface model, final Map<String, StreamInterface> streams)
            throws SimRuntimeException, NamingException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        setAnimationDelay(20); // 50 Hz animation update
        OtsReplication newReplication = new OtsReplication("rep" + ++this.lastReplication, startTime, warmupPeriod, runLength);
        model.getStreams().putAll(streams);
        super.initialize(model, newReplication);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OtsModelInterface model, final int replicationnr) throws SimRuntimeException, NamingException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        setAnimationDelay(20); // 50 Hz animation update
        OtsReplication newReplication = new OtsReplication("rep" + replicationnr, startTime, warmupPeriod, runLength);
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
    public final OtsReplication getReplication()
    {
        return (OtsReplication) super.getReplication();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "OTSAnimator [lastReplication=" + this.lastReplication + "]";
    }
}

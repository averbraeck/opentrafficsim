package org.opentrafficsim.core.dsol;

import java.io.Serializable;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DevsRealTimeAnimator;
import nl.tudelft.simulation.dsol.simulators.ErrorStrategy;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Construct a DSOL DevsRealTimeAnimator the easy way.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
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

    @Override
    public void initialize(final OtsModelInterface model, final OtsReplication replication) throws SimRuntimeException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        setAnimationDelay(20); // 50 Hz animation update
        super.initialize(model, replication);
    }

    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OtsModelInterface model, final HistoryManager historyManager) throws SimRuntimeException, NamingException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        setAnimationDelay(20); // 50 Hz animation update
        OtsReplication newReplication =
                new OtsReplication("rep" + ++this.lastReplication, startTime, warmupPeriod, runLength, historyManager);
        super.initialize(model, newReplication);
    }

    /**
     * Initialize a simulation engine without animation; the easy way. PauseOnError is set to true;
     * @param startTime the start time of the simulation
     * @param warmupPeriod the warm up period of the simulation (use new Duration(0, SECOND) if you don't know what this is)
     * @param runLength the duration of the simulation
     * @param model the simulation to execute
     * @param streams streams
     * @param historyManager history manager
     * @throws SimRuntimeException when e.g., warmupPeriod is larger than runLength
     * @throws NamingException when the context for the replication cannot be created
     */
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OtsModelInterface model, final Map<String, StreamInterface> streams, final HistoryManager historyManager)
            throws SimRuntimeException, NamingException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        setAnimationDelay(20); // 50 Hz animation update
        OtsReplication newReplication =
                new OtsReplication("rep" + ++this.lastReplication, startTime, warmupPeriod, runLength, historyManager);
        model.getStreams().putAll(streams);
        super.initialize(model, newReplication);
    }

    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OtsModelInterface model, final HistoryManager historyManager, final int replicationnr)
            throws SimRuntimeException, NamingException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        setAnimationDelay(20); // 50 Hz animation update
        OtsReplication newReplication =
                new OtsReplication("rep" + replicationnr, startTime, warmupPeriod, runLength, historyManager);
        super.initialize(model, newReplication);
    }

    @Override
    protected Duration simulatorTimeForWallClockMillis(final double wallMilliseconds)
    {
        return new Duration(wallMilliseconds, DurationUnit.MILLISECOND);
    }

    @Override
    public final OtsReplication getReplication()
    {
        return (OtsReplication) super.getReplication();
    }

    @Override
    public String toString()
    {
        return "OTSAnimator [lastReplication=" + this.lastReplication + "]";
    }
}

package org.opentrafficsim.core.dsol;

import java.io.Serializable;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.perception.HistoryManager;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.simulators.DevsRealTimeAnimator;
import nl.tudelft.simulation.dsol.simulators.ErrorStrategy;

/**
 * Construct a DSOL DevsRealTimeAnimator the easy way.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class OtsAnimator extends DevsRealTimeAnimator<Duration> implements OtsSimulatorInterface
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
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OtsModelInterface model, final HistoryManager historyManager) throws SimRuntimeException, NamingException
    {
        initialize(startTime, warmupPeriod, runLength, model, historyManager, ++this.lastReplication);
    }

    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OtsModelInterface model, final HistoryManager historyManager, final int replicationnr)
            throws SimRuntimeException, NamingException
    {
        initialize(model, new OtsReplication("rep" + replicationnr, startTime, warmupPeriod, runLength, historyManager));
    }

    @Override
    public void initialize(final OtsModelInterface model, final OtsReplication replication) throws SimRuntimeException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        setAnimationDelay(20); // 50 Hz animation update
        Supplier<Duration> simTimeSupplier = this::getSimulatorTime;
        Logger.setSimTimeSupplier(simTimeSupplier);
        addListener((e) -> Logger.removeSimTimeSupplier(simTimeSupplier), Replication.END_REPLICATION_EVENT);
        super.initialize(model, replication);
    }

    @Override
    public Duration simulatorTimeForWallClockMillis(final double wallMilliseconds)
    {
        return new Duration(wallMilliseconds, DurationUnit.MILLISECOND);
    }

    @Override
    public OtsReplication getReplication()
    {
        return (OtsReplication) super.getReplication();
    }

    @Override
    public String toString()
    {
        return "OtsAnimator [lastReplication=" + this.lastReplication + "]";
    }
}

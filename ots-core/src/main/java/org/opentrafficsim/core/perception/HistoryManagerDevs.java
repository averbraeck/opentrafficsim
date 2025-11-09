package org.opentrafficsim.core.perception;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;

/**
 * History manager that uses an {@code OtsSimulatorInterface}.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class HistoryManagerDevs extends HistoryManager implements EventListener
{
    /** */
    private static final long serialVersionUID = 1L;

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** Time over which history is guaranteed. */
    private final Duration history;

    /** Clean-up interval. */
    private final Duration cleanUpInterval;

    /**
     * Constructor.
     * @param simulator simulator
     * @param history time over which history is guaranteed
     * @param cleanUpInterval clean-up interval
     */
    public HistoryManagerDevs(final OtsSimulatorInterface simulator, final Duration history, final Duration cleanUpInterval)
    {
        this.simulator = simulator;
        this.history = history;
        this.cleanUpInterval = cleanUpInterval;
        Try.execute(() -> this.simulator.addListener(this, Replication.START_REPLICATION_EVENT), "Unable to add listener.");
        Try.execute(() -> this.simulator.addListener(this, Replication.END_REPLICATION_EVENT), "Unable to add listener.");
    }

    /**
     * Returns a history manager with no guaranteed history.
     * @param simulator simulator
     * @return history manager with no guaranteed history
     */
    public static HistoryManagerDevs noHistory(final OtsSimulatorInterface simulator)
    {
        return new HistoryManagerDevs(simulator, Duration.ZERO, Duration.ofSI(10.0));
    }

    @Override
    public Duration now()
    {
        return this.simulator.getSimulatorTime();
    }

    /**
     * Cleans up the history of all registered {@code Historicals}.
     */
    protected final void cleanUpHistory()
    {
        for (HistoricalElement historical : getHistoricals())
        {
            historical.cleanUpHistory(this.history);
        }
        try
        {
            this.simulator.scheduleEventRel(this.cleanUpInterval, () -> cleanUpHistory());
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(Replication.START_REPLICATION_EVENT))
        {
            cleanUpHistory(); // start clean-up event chain
        }
        else if (event.getType().equals(Replication.END_REPLICATION_EVENT))
        {
            endOfSimulation();
        }
    }

    @Override
    public String toString()
    {
        return "HistoryManagerDevs [history=" + this.history + ", cleanUpInterval=" + this.cleanUpInterval + "]";
    }

}

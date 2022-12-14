package org.opentrafficsim.core.perception;

import java.rmi.RemoteException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationInterface;

/**
 * History manager that uses an {@code OTSSimulatorInterface}.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class HistoryManagerDevs extends HistoryManager implements EventListenerInterface
{
    /** */
    private static final long serialVersionUID = 1L;

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** Time over which history is guaranteed. */
    private final Duration history;

    /** Clean-up interval. */
    private final Duration cleanUpInterval;

    /** Event input, can be the same as it's nothing. */
    private final Object[] none = new Object[0];

    /**
     * Constructor.
     * @param simulator OTSSimulatorInterface; simulator
     * @param history Duration; time over which history is guaranteed
     * @param cleanUpInterval Duration; clean-up interval
     */
    public HistoryManagerDevs(final OtsSimulatorInterface simulator, final Duration history, final Duration cleanUpInterval)
    {
        this.simulator = simulator;
        this.history = history;
        this.cleanUpInterval = cleanUpInterval;
        cleanUpHistory(); // start clean-up event chain
        Try.execute(() -> this.simulator.addListener(this, ReplicationInterface.END_REPLICATION_EVENT),
                "Unable to add listener.");
    }

    /** {@inheritDoc} */
    @Override
    public Time now()
    {
        return this.simulator.getSimulatorAbsTime();
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
            this.simulator.scheduleEventRel(this.cleanUpInterval, this, this, "cleanUpHistory", this.none);
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(ReplicationInterface.END_REPLICATION_EVENT))
        {
            endOfSimulation();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoryManagerDEVS [history=" + this.history + ", cleanUpInterval=" + this.cleanUpInterval + "]";
    }

}

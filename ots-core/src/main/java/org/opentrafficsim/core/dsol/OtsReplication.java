package org.opentrafficsim.core.dsol;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.perception.HistoryManager;

import nl.tudelft.simulation.dsol.experiment.SingleReplication;

/**
 * Simulation replication with history manager.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public class OtsReplication extends SingleReplication<Duration>
{
    /** History manager. */
    private final HistoryManager historyManager;

    /** Start time of day. */
    private final Duration startTimeOfDay;

    /**
     * Create a new OtsReplication.
     * @param id id of the new OtsReplication
     * @param startTimeOfDay the start time of the new OtsReplication
     * @param warmupPeriod the warm-up period of the new OtsReplication
     * @param runLength the run length of the new OtsReplication
     * @param historyManager history manager
     * @throws NamingException when the context for the replication cannot be created
     */
    public OtsReplication(final String id, final Duration startTimeOfDay, final Duration warmupPeriod, final Duration runLength,
            final HistoryManager historyManager) throws NamingException
    {
        super(id, Duration.ZERO, warmupPeriod, runLength);
        Throw.whenNull(historyManager, "historyManager");
        Throw.whenNull(startTimeOfDay, "startTimeOfDay");
        this.historyManager = historyManager;
        this.startTimeOfDay = startTimeOfDay;
    }

    /**
     * Returns the history manager.
     * @param simulator simulator
     * @return history manager
     */
    public HistoryManager getHistoryManager(final OtsSimulatorInterface simulator)
    {
        return this.historyManager;
    }

    /**
     * Returns the start time-of-day.
     * @return start time-of-day
     */
    public Duration getStartTimeOfDay()
    {
        return this.startTimeOfDay;
    }

    @Override
    public final String toString()
    {
        return "OtsReplication []";
    }

}

package org.opentrafficsim.core.dsol;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.perception.HistoryManager;

import nl.tudelft.simulation.dsol.experiment.SingleReplication;

/**
 * Simulation replication with history manager.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public class OtsReplication extends SingleReplication<Duration>
{
    /** History manager. */
    private final HistoryManager historyManager;

    /** The (absolute) start time of the replication. */
    private final Time startTimeAbs;

    /**
     * Create a new OtsReplication.
     * @param id id of the new OtsReplication
     * @param startTime the start time of the new OtsReplication
     * @param warmupPeriod the warmup period of the new OtsReplication
     * @param runLength the run length of the new OtsReplication
     * @param historyManager history manager
     * @throws NamingException when the context for the replication cannot be created
     */
    public OtsReplication(final String id, final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final HistoryManager historyManager) throws NamingException
    {
        super(id, Duration.ZERO, warmupPeriod, runLength);
        Throw.whenNull(historyManager, "historyManager");
        this.startTimeAbs = startTime;
        this.historyManager = historyManager;
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
     * Return the absolute start time of the simulation.
     * @return the absolute start time of the simulation
     */
    public Time getStartTimeAbs()
    {
        return this.startTimeAbs;
    }

    /** */
    private static final long serialVersionUID = 20140815L;

    @Override
    public final String toString()
    {
        return "OtsReplication []";
    }
}

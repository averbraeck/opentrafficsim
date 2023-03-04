package org.opentrafficsim.core.dsol;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.core.perception.HistoryManagerDevs;

import nl.tudelft.simulation.dsol.experiment.SingleReplication;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public class OtsReplication extends SingleReplication<Duration>
{
    /** History manager. */
    private HistoryManager historyManager;

    /** the (absolute) start time of the replication. */
    private final Time startTimeAbs;

    /**
     * Create a new OTSReplication.
     * @param id String; id of the new OTSReplication
     * @param startTime Time; the start time of the new OTSReplication
     * @param warmupPeriod Duration; the warmup period of the new OTSReplication
     * @param runLength Duration; the run length of the new OTSReplication
     * @throws NamingException when the context for the replication cannot be created
     */
    public OtsReplication(final String id, final Time startTime, final Duration warmupPeriod, final Duration runLength)
            throws NamingException
    {
        super(id, Duration.ZERO, warmupPeriod, runLength);
        this.startTimeAbs = startTime;
    }

    /**
     * Returns the history manager. If none was set, one is created coupled to the simulator using 0s of history and 10s
     * clean-up time.
     * @param simulator OtsSimulatorInterface; simulator
     * @return HistoryManager; history manager
     */
    public HistoryManager getHistoryManager(final OtsSimulatorInterface simulator)
    {
        if (this.historyManager == null)
        {
            this.historyManager = new HistoryManagerDevs(simulator, Duration.ZERO, Duration.instantiateSI(10.0));
        }
        return this.historyManager;
    }

    /**
     * Set history manager.
     * @param historyManager HistoryManager; history manager to set
     */
    public void setHistoryManager(final HistoryManager historyManager)
    {
        this.historyManager = historyManager;
    }

    /**
     * Return the absolute start time of the simulation.
     * @return Time; the absolute start time of the simulation
     */
    public Time getStartTimeAbs()
    {
        return this.startTimeAbs;
    }

    /** */
    private static final long serialVersionUID = 20140815L;

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OTSReplication []";
    }
}

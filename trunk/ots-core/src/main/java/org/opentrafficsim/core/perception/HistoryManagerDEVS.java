package org.opentrafficsim.core.perception;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * History manager that uses an {@code OTSDEVSSimulatorInterface}.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 5 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class HistoryManagerDEVS extends HistoryManager
{

    /** Simulator. */
    private final OTSDEVSSimulatorInterface simulator;
    
    /** Time over which history is guaranteed. */
    private final Duration history;
    
    /** Clean-up interval. */
    private final Duration cleanUpInterval;
    
    /** Event input, can be the same as it's nothing. */
    private final Object[] none = new Object[0];

    /**
     * Constructor.
     * @param simulator OTSDEVSSimulatorInterface; simulator
     * @param history Duration; time over which history is guaranteed
     * @param cleanUpInterval Duration; clean-up interval
     */
    public HistoryManagerDEVS(final OTSDEVSSimulatorInterface simulator, final Duration history, final Duration cleanUpInterval)
    {
        this.simulator = simulator;
        this.history = history;
        this.cleanUpInterval = cleanUpInterval;
        cleanUpHistory(); // start clean-up event chain
    }

    /** {@inheritDoc} */
    @Override
    public Time now()
    {
        return this.simulator.getSimulatorTime().getTime();
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
    public String toString()
    {
        return "HistoryManagerDEVS [history=" + this.history + ", cleanUpInterval=" + this.cleanUpInterval + "]";
    }
    
}

package org.opentrafficsim.core.perception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;

import nl.tudelft.simulation.dsol.experiment.Replication;

/**
 * History manager with automatic garbage collection by the java garbage collector using weak references to the
 * {@code Historical}s.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class HistoryManager
{

    // HACK ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TODO remove this hack and obtain manager from somewhere else, OTSReplication?
    /** Centrally stored manager. */
    private static Map<Replication<Time, Duration, OTSSimTimeDouble>, HistoryManagerDEVS> MANAGERS = new HashMap<>();

    /**
     * Get central manager.
     * @param simulator
     * @return HistoryManagerDEVS; central manager
     */
    public static HistoryManagerDEVS get(final OTSDEVSSimulatorInterface simulator)
    {
        HistoryManagerDEVS manager = MANAGERS.get(simulator.getReplication());
        if (manager == null)
        {
            manager = new HistoryManagerDEVS(simulator, Duration.createSI(0.0), Duration.createSI(10.0));
            MANAGERS.put(simulator.getReplication(), manager);
        }
        return manager;
    }
    
    /**
     * Set central manager.
     * @param manager
     * @param simulator
     */
    public static void set(final HistoryManagerDEVS manager, final OTSDEVSSimulatorInterface simulator)
    {
        MANAGERS.put(simulator.getReplication(), manager);
    }
    // END OF HACK /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Set of all {@code Historical}s. */
    // There's no WeakSet, but this is effectively the same. Iterating over this is safe, only alive objects are returned.
    private final Set<AbstractHistorical<?, ?>> historicals =
            Collections.newSetFromMap(new WeakHashMap<AbstractHistorical<?, ?>, Boolean>());

    /**
     * Registers a historical.
     * @param historical Historical; historical to register.
     */
    public void registerHistorical(AbstractHistorical<?, ?> historical)
    {
        if (historical != null)
        {
            this.historicals.add(historical);
        }
    }

    /**
     * Returns the historicals.
     * @return the historicals
     */
    protected final Set<AbstractHistorical<?, ?>> getHistoricals()
    {
        return this.historicals;
    }

    /**
     * Returns the current simulation time. This is used by historicals to time-stamp state changes.
     * @return Time; current simulation time.
     */
    abstract Time now();

}

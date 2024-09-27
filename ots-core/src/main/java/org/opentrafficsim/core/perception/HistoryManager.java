package org.opentrafficsim.core.perception;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

/**
 * History manager with automatic garbage collection by the java garbage collector using weak references to the
 * {@code Historical}s.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class HistoryManager
{

    /** Set of all {@code Historical}s. */
    // There's no WeakSet, but this is effectively the same. Iterating over this is safe, only alive objects are returned.
    private final Set<HistoricalElement> historicals = Collections.newSetFromMap(new WeakHashMap<HistoricalElement, Boolean>());

    /**
     * Registers a historical.
     * @param historical historical to register.
     */
    public void registerHistorical(final HistoricalElement historical)
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
    protected final Set<HistoricalElement> getHistoricals()
    {
        return this.historicals;
    }

    /**
     * Returns the current simulation time. This is used by historicals to time-stamp state changes.
     * @return current simulation time.
     */
    abstract Time now();

    /**
     * Historical view for the history manager.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface HistoricalElement
    {
        /**
         * Removes events that are no longer needed to guarantee the history time. This is invoked by the history manager.
         * @param history history time to keep
         */
        void cleanUpHistory(Duration history);
    }

    /**
     * Method that clears the entire memory at simulation end.
     */
    protected final void endOfSimulation()
    {
        for (HistoricalElement historical : this.historicals)
        {
            historical.cleanUpHistory(Duration.ZERO);
        }
        this.historicals.clear();
    }

}

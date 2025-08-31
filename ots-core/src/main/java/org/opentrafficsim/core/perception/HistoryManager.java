package org.opentrafficsim.core.perception;

import java.util.Collections;
import java.util.Iterator;
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
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class HistoryManager
{

    /** Set of all {@code Historical}s. */
    // There's no WeakSet, but this is effectively the same. Iterating over this is safe, only alive objects are returned.
    private final Set<HistoricalElement> historicals = Collections.newSetFromMap(new WeakHashMap<HistoricalElement, Boolean>());

    /**
     * Constructor.
     */
    public HistoryManager()
    {
        //
    }

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
    protected abstract Time now();

    /**
     * Historical view for the history manager.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface HistoricalElement
    {
        /**
         * Removes events that are no longer needed to guarantee the history time. Events of before this time may remain in
         * memory if they are required to determine the earliest state within the history. For example in a single-valued
         * element, to maintain history from 60s through 65s, events at 58s, 61s and 64s may be kept in memory. In this way the
         * value at 60s can be gathered from the event at 58s. This method is invoked by the history manager.
         * @param history history time to keep
         */
        void cleanUpHistory(Duration history);

        /**
         * Returns the object that owns the historical value so that a {@code HistoryManager} can apply logic that depends on
         * the type of object.
         * @return object that owns the historical value
         */
        Object getOwner();

        /**
         * Returns event times from most recent to oldest.
         * @return iterator over event times
         */
        Iterator<Time> timeIterator();
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

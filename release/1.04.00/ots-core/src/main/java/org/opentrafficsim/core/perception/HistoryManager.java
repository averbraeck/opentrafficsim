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
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class HistoryManager
{

    /** Set of all {@code Historical}s. */
    // There's no WeakSet, but this is effectively the same. Iterating over this is safe, only alive objects are returned.
    private final Set<HistoricalElement> historicals = Collections.newSetFromMap(new WeakHashMap<HistoricalElement, Boolean>());

    /**
     * Registers a historical.
     * @param historical HistoricalElement; historical to register.
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
     * @return Time; current simulation time.
     */
    abstract Time now();

    /**
     * Historical view for the history manager.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public interface HistoricalElement
    {
        /**
         * Removes events that are no longer needed to guarantee the history time. This is invoked by the history manager.
         * @param history Duration; history time to keep
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

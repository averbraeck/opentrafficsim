package org.opentrafficsim.core.perception.collections;

import java.util.HashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * HashMap-valued historical state. The current hash map is always maintained, and past states of the hash map are obtained by
 * applying the events between now and the requested time in reverse.<br>
 * <br>
 * The set views returned by this class are unmodifiable.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 */
public class HistoricalHashMap<K, V> extends AbstractHistoricalMap<K, V, HashMap<K, V>>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     */
    public HistoricalHashMap(final HistoryManager historyManager)
    {
        super(historyManager, new HashMap<>());
    }

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param m Map&lt;? extends K, ? extends V&gt;; initial map
     */
    public HistoricalHashMap(final HistoryManager historyManager, final Map<? extends K, ? extends V> m)
    {
        super(historyManager, new HashMap<>(m));
    }

    /** {@inheritDoc} */
    @Override
    public HashMap<K, V> get()
    {
        return getMap();
    }

    /** {@inheritDoc} */
    @Override
    public HashMap<K, V> get(final Time time)
    {
        if (isLastState(time))
        {
            return getMap();
        }
        return fill(time, new HashMap<>());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalHashMap [current=" + getMap() + "]";
    }

}

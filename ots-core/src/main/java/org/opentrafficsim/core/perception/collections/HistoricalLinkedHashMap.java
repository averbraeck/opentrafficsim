package org.opentrafficsim.core.perception.collections;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * LinkedHashMap-valued historical state. The current linked hash map is always maintained, and past states of the linked hash
 * map are obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * The set views returned by this class are unmodifiable.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 */
public class HistoricalLinkedHashMap<K, V> extends AbstractHistoricalMap<K, V, LinkedHashMap<K, V>>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     */
    public HistoricalLinkedHashMap(final HistoryManager historyManager)
    {
        super(historyManager, new LinkedHashMap<>());
    }

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param m Map&lt;? extends K, ? extends V&gt;; initial map
     */
    public HistoricalLinkedHashMap(final HistoryManager historyManager, final Map<? extends K, ? extends V> m)
    {
        super(historyManager, new LinkedHashMap<>(m));
    }

    /** {@inheritDoc} */
    @Override
    public LinkedHashMap<K, V> get()
    {
        return getMap();
    }

    /** {@inheritDoc} */
    @Override
    public LinkedHashMap<K, V> get(final Time time)
    {
        if (isLastState(time))
        {
            return getMap();
        }
        return fill(time, new LinkedHashMap<>());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalLinkedHashMap [current=" + getMap() + "]";
    }

}

package org.opentrafficsim.core.perception.collections;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * LinkedHashMap-valued historical state. The current linked hash map is always maintained, and past states of the linked hash
 * map are obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * The set views returned by this class are unmodifiable.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 */
public class HistoricalLinkedHashMap<K, V> extends AbstractHistoricalMap<K, V, LinkedHashMap<K, V>>
{

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     */
    public HistoricalLinkedHashMap(final HistoryManager historyManager, final Object owner)
    {
        super(historyManager, owner, new LinkedHashMap<>());
    }

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param m initial map
     */
    public HistoricalLinkedHashMap(final HistoryManager historyManager, final Object owner,
            final Map<? extends K, ? extends V> m)
    {
        super(historyManager, owner, new LinkedHashMap<>(m));
    }

    @Override
    public LinkedHashMap<K, V> get()
    {
        return getMap();
    }

    @Override
    public LinkedHashMap<K, V> get(final Duration time)
    {
        if (isLastState(time))
        {
            return getMap();
        }
        return fill(time, new LinkedHashMap<>());
    }

    @Override
    public String toString()
    {
        return "HistoricalLinkedHashMap [current=" + getMap() + "]";
    }

}

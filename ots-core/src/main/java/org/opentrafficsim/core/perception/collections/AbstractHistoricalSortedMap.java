package org.opentrafficsim.core.perception.collections;

import java.util.Collections;
import java.util.Comparator;
import java.util.SortedMap;

import org.opentrafficsim.core.perception.HistoryManager;

/**
 * SortedMap-valued historical state. The current sorted map is always maintained, and past states of the sorted map are
 * obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * The set views and sub-maps returned by this class are unmodifiable.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 * @param <M> sorted map type
 */
public abstract class AbstractHistoricalSortedMap<K, V, M extends SortedMap<K, V>> extends AbstractHistoricalMap<K, V, M>
        implements HistoricalSortedMap<K, V>
{

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param map initial map
     */
    protected AbstractHistoricalSortedMap(final HistoryManager historyManager, final Object owner, final M map)
    {
        super(historyManager, owner, map);
    }

    // Non-altering SortedMap methods

    @Override
    public Comparator<? super K> comparator()
    {
        return getMap().comparator();
    }

    @Override
    public SortedMap<K, V> subMap(final K fromKey, final K toKey)
    {
        return Collections.unmodifiableSortedMap(getMap().subMap(fromKey, toKey));
    }

    @Override
    public SortedMap<K, V> headMap(final K toKey)
    {
        return Collections.unmodifiableSortedMap(getMap().headMap(toKey));
    }

    @Override
    public SortedMap<K, V> tailMap(final K fromKey)
    {
        return Collections.unmodifiableSortedMap(getMap().tailMap(fromKey));
    }

    @Override
    public K firstKey()
    {
        return getMap().firstKey();
    }

    @Override
    public K lastKey()
    {
        return getMap().lastKey();
    }

}

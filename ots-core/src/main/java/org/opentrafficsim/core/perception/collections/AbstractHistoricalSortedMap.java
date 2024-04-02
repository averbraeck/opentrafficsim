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
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
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
     * @param historyManager HistoryManager; history manager
     * @param map M; initial map
     */
    protected AbstractHistoricalSortedMap(final HistoryManager historyManager, final M map)
    {
        super(historyManager, map);
    }

    // Non-altering SortedMap methods

    /** {@inheritDoc} */
    @Override
    public Comparator<? super K> comparator()
    {
        return getMap().comparator();
    }

    /** {@inheritDoc} */
    @Override
    public SortedMap<K, V> subMap(final K fromKey, final K toKey)
    {
        return Collections.unmodifiableSortedMap(getMap().subMap(fromKey, toKey));
    }

    /** {@inheritDoc} */
    @Override
    public SortedMap<K, V> headMap(final K toKey)
    {
        return Collections.unmodifiableSortedMap(getMap().headMap(toKey));
    }

    /** {@inheritDoc} */
    @Override
    public SortedMap<K, V> tailMap(final K fromKey)
    {
        return Collections.unmodifiableSortedMap(getMap().tailMap(fromKey));
    }

    /** {@inheritDoc} */
    @Override
    public K firstKey()
    {
        return getMap().firstKey();
    }

    /** {@inheritDoc} */
    @Override
    public K lastKey()
    {
        return getMap().lastKey();
    }

}

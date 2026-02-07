package org.opentrafficsim.core.perception.collections;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.NavigableSet;

import org.opentrafficsim.core.perception.HistoryManager;

/**
 * NavigableMap-valued historical state. The current navigable map is always maintained, and past states of the navigable map
 * are obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * The set views and sub-maps returned by this class are unmodifiable.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 * @param <M> navigable map type
 */
public abstract class AbstractHistoricalNavigableMap<K, V, M extends NavigableMap<K, V>>
        extends AbstractHistoricalSortedMap<K, V, M> implements HistoricalNavigableMap<K, V>
{

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param map initial map
     */
    protected AbstractHistoricalNavigableMap(final HistoryManager historyManager, final Object owner, final M map)
    {
        super(historyManager, owner, map);
    }

    // Altering NavigableMap methods

    @Override
    public Entry<K, V> pollFirstEntry()
    {
        if (getMap().isEmpty())
        {
            return null;
        }
        Entry<K, V> entry = Collections.unmodifiableNavigableMap(getMap()).firstEntry();
        remove(entry.getKey());
        return entry;
    }

    @Override
    public Entry<K, V> pollLastEntry()
    {
        if (getMap().isEmpty())
        {
            return null;
        }
        Entry<K, V> entry = Collections.unmodifiableNavigableMap(getMap()).lastEntry();
        remove(entry.getKey());
        return entry;
    }

    // Non-altering NavigableMap methods

    @Override
    public Entry<K, V> lowerEntry(final K key)
    {
        return Collections.unmodifiableNavigableMap(getMap()).lowerEntry(key);
    }

    @Override
    public K lowerKey(final K key)
    {
        return getMap().lowerKey(key);
    }

    @Override
    public Entry<K, V> floorEntry(final K key)
    {
        return Collections.unmodifiableNavigableMap(getMap()).floorEntry(key);
    }

    @Override
    public K floorKey(final K key)
    {
        return getMap().floorKey(key);
    }

    @Override
    public Entry<K, V> ceilingEntry(final K key)
    {
        return Collections.unmodifiableNavigableMap(getMap()).ceilingEntry(key);
    }

    @Override
    public K ceilingKey(final K key)
    {
        return getMap().ceilingKey(key);
    }

    @Override
    public Entry<K, V> higherEntry(final K key)
    {
        return Collections.unmodifiableNavigableMap(getMap()).higherEntry(key);
    }

    @Override
    public K higherKey(final K key)
    {
        return getMap().higherKey(key);
    }

    @Override
    public Entry<K, V> firstEntry()
    {
        return Collections.unmodifiableNavigableMap(getMap()).firstEntry();
    }

    @Override
    public Entry<K, V> lastEntry()
    {
        return Collections.unmodifiableNavigableMap(getMap()).lastEntry();
    }

    @Override
    public NavigableMap<K, V> descendingMap()
    {
        return Collections.unmodifiableNavigableMap(getMap().descendingMap());
    }

    @Override
    public NavigableSet<K> navigableKeySet()
    {
        return Collections.unmodifiableNavigableSet(getMap().navigableKeySet());
    }

    @Override
    public NavigableSet<K> descendingKeySet()
    {
        return Collections.unmodifiableNavigableSet(getMap().descendingKeySet());
    }

    @Override
    public NavigableMap<K, V> subMap(final K fromKey, final boolean fromInclusive, final K toKey, final boolean toInclusive)
    {
        return Collections.unmodifiableNavigableMap(getMap().subMap(fromKey, fromInclusive, toKey, toInclusive));
    }

    @Override
    public NavigableMap<K, V> headMap(final K toKey, final boolean inclusive)
    {
        return Collections.unmodifiableNavigableMap(getMap().headMap(toKey, inclusive));
    }

    @Override
    public NavigableMap<K, V> tailMap(final K fromKey, final boolean inclusive)
    {
        return Collections.unmodifiableNavigableMap(getMap().tailMap(fromKey, inclusive));
    }

}

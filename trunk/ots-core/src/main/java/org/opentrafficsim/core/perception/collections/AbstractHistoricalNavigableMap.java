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
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 2 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 * @param <M> navigable map type
 */
public abstract class AbstractHistoricalNavigableMap<K, V, M extends NavigableMap<K, V>>
        extends AbstractHistoricalSortedMap<K, V, M> implements HistoricalNavigableMap<K, V>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param map M; initial map
     */
    protected AbstractHistoricalNavigableMap(final HistoryManager historyManager, final M map)
    {
        super(historyManager, map);
    }

    // Altering NavigableMap methods

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public Entry<K, V> lowerEntry(final K key)
    {
        return Collections.unmodifiableNavigableMap(getMap()).lowerEntry(key);
    }

    /** {@inheritDoc} */
    @Override
    public K lowerKey(final K key)
    {
        return getMap().lowerKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public Entry<K, V> floorEntry(final K key)
    {
        return Collections.unmodifiableNavigableMap(getMap()).floorEntry(key);
    }

    /** {@inheritDoc} */
    @Override
    public K floorKey(final K key)
    {
        return getMap().floorKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public Entry<K, V> ceilingEntry(final K key)
    {
        return Collections.unmodifiableNavigableMap(getMap()).ceilingEntry(key);
    }

    /** {@inheritDoc} */
    @Override
    public K ceilingKey(final K key)
    {
        return getMap().ceilingKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public Entry<K, V> higherEntry(final K key)
    {
        return Collections.unmodifiableNavigableMap(getMap()).higherEntry(key);
    }

    /** {@inheritDoc} */
    @Override
    public K higherKey(final K key)
    {
        return getMap().higherKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public Entry<K, V> firstEntry()
    {
        return Collections.unmodifiableNavigableMap(getMap()).firstEntry();
    }

    /** {@inheritDoc} */
    @Override
    public Entry<K, V> lastEntry()
    {
        return Collections.unmodifiableNavigableMap(getMap()).lastEntry();
    }

    /** {@inheritDoc} */
    @Override
    public NavigableMap<K, V> descendingMap()
    {
        return Collections.unmodifiableNavigableMap(getMap().descendingMap());
    }

    /** {@inheritDoc} */
    @Override
    public NavigableSet<K> navigableKeySet()
    {
        return Collections.unmodifiableNavigableSet(getMap().navigableKeySet());
    }

    /** {@inheritDoc} */
    @Override
    public NavigableSet<K> descendingKeySet()
    {
        return Collections.unmodifiableNavigableSet(getMap().descendingKeySet());
    }

    /** {@inheritDoc} */
    @Override
    public NavigableMap<K, V> subMap(final K fromKey, final boolean fromInclusive, final K toKey, final boolean toInclusive)
    {
        return Collections.unmodifiableNavigableMap(getMap().subMap(fromKey, fromInclusive, toKey, toInclusive));
    }

    /** {@inheritDoc} */
    @Override
    public NavigableMap<K, V> headMap(final K toKey, final boolean inclusive)
    {
        return Collections.unmodifiableNavigableMap(getMap().headMap(toKey, inclusive));
    }

    /** {@inheritDoc} */
    @Override
    public NavigableMap<K, V> tailMap(final K fromKey, final boolean inclusive)
    {
        return Collections.unmodifiableNavigableMap(getMap().tailMap(fromKey, inclusive));
    }

}

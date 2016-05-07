package org.opentrafficsim.core.immutablecollections;

import java.util.Comparator;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * An immutable wrapper for a TreeMap.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <K> the key type of content of this Map
 * @param <V> the value type of content of this Map
 */
public class ImmutableTreeMap<K, V> extends ImmutableAbstractMap<K, V> implements ImmutableNavigableMap<K, V>
{
    /** */
    private static final long serialVersionUID = 20160507L;

    /**
     * @param sortedMap the map to use as the immutable map.
     */
    public ImmutableTreeMap(final SortedMap<K, V> sortedMap)
    {
        super(new TreeMap<K, V>(sortedMap));
    }

    /**
     * @param sortedMap the map to use as the immutable map.
     */
    public ImmutableTreeMap(final ImmutableSortedMap<K, V> sortedMap)
    {
        this(sortedMap.toMap());
    }

    /** {@inheritDoc} */
    @Override
    protected final NavigableMap<K, V> getMap()
    {
        return (NavigableMap<K, V>) super.getMap();
    }

    /** {@inheritDoc} */
    @Override
    public final NavigableMap<K, V> toMap()
    {
        return new TreeMap<K, V>(super.getMap());
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableSortedSet<K> keySet()
    {
        return new ImmutableTreeSet<K>((SortedSet<K>) getMap().keySet());
    }
    
    /** {@inheritDoc} */
    @Override
    public final Comparator<? super K> comparator()
    {
        return getMap().comparator();
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableSortedMap<K, V> subMap(final K fromKey, final K toKey)
    {
        return new ImmutableTreeMap<K, V>(getMap().subMap(fromKey, toKey));
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableSortedMap<K, V> headMap(final K toKey)
    {
        return new ImmutableTreeMap<K, V>(getMap().headMap(toKey));
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableSortedMap<K, V> tailMap(final K fromKey)
    {
        return new ImmutableTreeMap<K, V>(getMap().tailMap(fromKey));
    }

    /** {@inheritDoc} */
    @Override
    public final K firstKey()
    {
        return getMap().firstKey();
    }

    /** {@inheritDoc} */
    @Override
    public final K lastKey()
    {
        return getMap().lastKey();
    }

    /** {@inheritDoc} */
    @Override
    public final K lowerKey(final K key)
    {
        return getMap().lowerKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public final K floorKey(final K key)
    {
        return getMap().floorKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public final K ceilingKey(final K key)
    {
        return getMap().ceilingKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public final K higherKey(final K key)
    {
        return getMap().higherKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableNavigableMap<K, V> descendingMap()
    {
        return new ImmutableTreeMap<K, V>(getMap().descendingMap());
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableNavigableMap<K, V> subMap(final K fromKey, final boolean fromInclusive, final K toKey,
            final boolean toInclusive)
    {
        return new ImmutableTreeMap<K, V>(getMap().subMap(fromKey, fromInclusive, toKey, toInclusive));
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableNavigableMap<K, V> headMap(final K toKey, final boolean inclusive)
    {
        return new ImmutableTreeMap<K, V>(getMap().headMap(toKey, inclusive));
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableNavigableMap<K, V> tailMap(final K fromKey, final boolean inclusive)
    {
        return new ImmutableTreeMap<K, V>(getMap().tailMap(fromKey, inclusive));
    }

}

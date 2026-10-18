package org.opentrafficsim.base.collections;

import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Defines a map that returns a sorted key set, also through its partial map views.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface SortedKeySetMap<K, V> extends SortedMap<K, V>
{

    @Override
    SortedSet<K> keySet();

    @Override
    SortedKeySetMap<K, V> subMap(K fromKey, K toKey);

    @Override
    SortedKeySetMap<K, V> headMap(K toKey);

    @Override
    SortedKeySetMap<K, V> tailMap(K fromKey);

}

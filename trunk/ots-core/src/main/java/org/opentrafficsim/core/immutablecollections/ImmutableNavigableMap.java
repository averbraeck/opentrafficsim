package org.opentrafficsim.core.immutablecollections;

import java.util.Collections;
import java.util.Comparator;
import java.util.NavigableMap;

/**
 * A {@link ImmutableSortedMap} extended with navigation methods reporting closest matches for given search targets. Methods
 * {@code lowerKey}, {@code floorKey}, {@code ceilingKey}, and {@code higherKey} return keys respectively less than, less than
 * or equal, greater than or equal, and greater than a given key, returning {@code null} if there is no such key. All methods
 * from java.util.NavigableMap that can change the map have been left out.
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
public interface ImmutableNavigableMap<K, V> extends ImmutableSortedMap<K, V>
{
    /**
     * Returns a modifiable copy of this immutable map.
     * @return a modifiable copy of this immutable map.
     */
    NavigableMap<K, V> toMap();

    /**
     * Returns a {@link ImmutableSortedSet} view of the keys contained in this map.
     * @return an immutable sorted set of the keys contained in this map
     */
    ImmutableSortedSet<K> keySet();

    /**
     * Returns the greatest key in this immutable map strictly less than the given key, or {@code null} if there is no such key.
     * @param e the value to match
     * @return the greatest key less than {@code e}, or {@code null} if there is no such key
     * @throws ClassCastException if the specified key cannot be compared with the keys currently in the immutable map
     * @throws NullPointerException if the specified key is null and this immutable map does not permit null keys
     */
    K lowerKey(K e);

    /**
     * Returns the greatest key in this immutable map less than or equal to the given key, or {@code null} if there is no such
     * key.
     * @param e the value to match
     * @return the greatest key less than or equal to {@code e}, or {@code null} if there is no such key
     * @throws ClassCastException if the specified key cannot be compared with the keys currently in the immutable map
     * @throws NullPointerException if the specified key is null and this immutable map does not permit null keys
     */
    K floorKey(K e);

    /**
     * Returns the least key in this immutable map greater than or equal to the given key, or {@code null} if there is no such
     * key.
     * @param e the value to match
     * @return the least key greater than or equal to {@code e}, or {@code null} if there is no such key
     * @throws ClassCastException if the specified key cannot be compared with the keys currently in the immutable map
     * @throws NullPointerException if the specified key is null and this immutable map does not permit null keys
     */
    K ceilingKey(K e);

    /**
     * Returns the least key in this immutable map strictly greater than the given key, or {@code null} if there is no such key.
     * @param e the value to match
     * @return the least key greater than {@code e}, or {@code null} if there is no such key
     * @throws ClassCastException if the specified key cannot be compared with the keys currently in the immutable map
     * @throws NullPointerException if the specified key is null and this immutable map does not permit null keys
     */
    K higherKey(K e);

    /**
     * Returns a reverse order view of the keys contained in this immutable map.
     * <p>
     * The returned immutable map has an ordering equivalent to
     * <tt>{@link Collections#reverseOrder(Comparator) Collections.reverseOrder}(comparator())</tt>. The expression
     * {@code s.descendingMap().descendingMap()} returns a view of {@code s} essentially equivalent to {@code s}.
     * @return a reverse order view of this immutable map
     */
    ImmutableNavigableMap<K, V> descendingMap();

    /**
     * Returns a view of the portion of this immutable map whose keys range from {@code fromKey} to {@code toKey}. If
     * {@code fromKey} and {@code toKey} are equal, the returned immutable map is empty unless {@code fromInclusive} and
     * {@code toInclusive} are both true.
     * @param fromKey low endpoint of the returned immutable map
     * @param fromInclusive {@code true} if the low endpoint is to be included in the returned view
     * @param toKey high endpoint of the returned immutable map
     * @param toInclusive {@code true} if the high endpoint is to be included in the returned view
     * @return a view of the portion of this immutable map whose keys range from {@code fromKey}, inclusive, to {@code toKey},
     *         exclusive
     * @throws ClassCastException if {@code fromKey} and {@code toKey} cannot be compared to one another using this immutable
     *             map's comparator (or, if the immutable map has no comparator, using natural ordering). Implementations may,
     *             but are not required to, throw this exception if {@code fromKey} or {@code toKey} cannot be compared to keys
     *             currently in the immutable map.
     * @throws NullPointerException if {@code fromKey} or {@code toKey} is null and this immutable map does not permit null keys
     * @throws IllegalArgumentException if {@code fromKey} is greater than {@code toKey}; or if this immutable map itself has a
     *             restricted range, and {@code fromKey} or {@code toKey} lies outside the bounds of the range.
     */
    ImmutableNavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

    /**
     * Returns a view of the portion of this immutable map whose keys are less than (or equal to, if {@code inclusive} is true)
     * {@code toKey}.
     * @param toKey high endpoint of the returned immutable map
     * @param inclusive {@code true} if the high endpoint is to be included in the returned view
     * @return a view of the portion of this immutable map whose keys are less than (or equal to, if {@code inclusive} is true)
     *         {@code toKey}
     * @throws ClassCastException if {@code toKey} is not compatible with this immutable map's comparator (or, if the immutable
     *             map has no comparator, if {@code toKey} does not implement {@link Comparable}). Implementations may, but are
     *             not required to, throw this exception if {@code toKey} cannot be compared to keys currently in the immutable
     *             map.
     * @throws NullPointerException if {@code toKey} is null and this immutable map does not permit null keys
     * @throws IllegalArgumentException if this immutable map itself has a restricted range, and {@code toKey} lies outside the
     *             bounds of the range
     */
    ImmutableNavigableMap<K, V> headMap(K toKey, boolean inclusive);

    /**
     * Returns a view of the portion of this immutable map whose keys are greater than (or equal to, if {@code inclusive} is
     * true) {@code fromKey}.
     * @param fromKey low endpoint of the returned immutable map
     * @param inclusive {@code true} if the low endpoint is to be included in the returned view
     * @return a view of the portion of this immutable map whose keys are greater than or equal to {@code fromKey}
     * @throws ClassCastException if {@code fromKey} is not compatible with this immutable map's comparator (or, if the
     *             immutable map has no comparator, if {@code fromKey} does not implement {@link Comparable}). Implementations
     *             may, but are not required to, throw this exception if {@code fromKey} cannot be compared to keys currently in
     *             the immutable map.
     * @throws NullPointerException if {@code fromKey} is null and this immutable map does not permit null keys
     * @throws IllegalArgumentException if this immutable map itself has a restricted range, and {@code fromKey} lies outside
     *             the bounds of the range
     */
    ImmutableNavigableMap<K, V> tailMap(K fromKey, boolean inclusive);

}

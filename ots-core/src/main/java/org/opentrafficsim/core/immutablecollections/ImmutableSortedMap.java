package org.opentrafficsim.core.immutablecollections;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.SortedMap;

/**
 * A SortedMap interface without the methods that can change it. The return values of subMap, tailMap and headMap are all
 * ImmutableSortedMaps.
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
public interface ImmutableSortedMap<K, V> extends ImmutableMap<K, V>
{
    /**
     * Returns a modifiable copy of this immutable map.
     * @return a modifiable copy of this immutable map.
     */
    SortedMap<K, V> toMap();

    /**
     * Returns the comparator used to order the keys in this immutable map, or <tt>null</tt> if this immutable map uses the
     * {@linkplain Comparable natural ordering} of its keys.
     * @return the comparator used to order the keys in this immutable map, or <tt>null</tt> if this immutable map uses the
     *         natural ordering of its keys
     */
    Comparator<? super K> comparator();

    /**
     * Returns a view of the portion of this immutable map whose keys range from <tt>fromKey</tt>, inclusive, to
     * <tt>toKey</tt>, exclusive. (If <tt>fromKey</tt> and <tt>toKey</tt> are equal, the returned immutable map is
     * empty.)
     * <p>
     * The result of this method is a new, immutable sorted map.
     * @param fromKey low endpoint (inclusive) of the returned immutable map
     * @param toKey high endpoint (exclusive) of the returned immutable map
     * @return a new, immutable sorted map of the portion of this immutable map whose keys range from <tt>fromKey</tt>,
     *         inclusive, to <tt>toKey</tt>, exclusive
     * @throws ClassCastException if <tt>fromKey</tt> and <tt>toKey</tt> cannot be compared to one another using this
     *             immutable map's comparator (or, if the immutable map has no comparator, using natural ordering).
     *             Implementations may, but are not required to, throw this exception if <tt>fromKey</tt> or
     *             <tt>toKey</tt> cannot be compared to keys currently in the immutable map.
     * @throws NullPointerException if <tt>fromKey</tt> or <tt>toKey</tt> is null and this immutable map does not permit
     *             null keys
     * @throws IllegalArgumentException if <tt>fromKey</tt> is greater than <tt>toKey</tt>; or if this immutable map
     *             itself has a restricted range, and <tt>fromKey</tt> or <tt>toKey</tt> lies outside the bounds of the
     *             range
     */
    ImmutableSortedMap<K, V> subMap(K fromKey, K toKey);

    /**
     * Returns a view of the portion of this immutable map whose keys are strictly less than <tt>toKey</tt>. The
     * returned immutable map is backed by this immutable map, so changes in the returned immutable map are reflected in this
     * immutable map, and vice-versa. The returned immutable map supports all optional immutable map operations that this
     * immutable map supports.
     * <p>
     * The result of this method is a new, immutable sorted map.
     * @param toKey high endpoint (exclusive) of the returned immutable map
     * @return a view of the portion of this immutable map whose keys are strictly less than <tt>toKey</tt>
     * @throws ClassCastException if <tt>toKey</tt> is not compatible with this immutable map's comparator (or, if the
     *             immutable map has no comparator, if <tt>toKey</tt> does not implement {@link Comparable}).
     *             Implementations may, but are not required to, throw this exception if <tt>toKey</tt> cannot be compared
     *             to keys currently in the immutable map.
     * @throws NullPointerException if <tt>toKey</tt> is null and this immutable map does not permit null keys
     * @throws IllegalArgumentException if this immutable map itself has a restricted range, and <tt>toKey</tt> lies outside
     *             the bounds of the range
     */
    ImmutableSortedMap<K, V> headMap(K toKey);

    /**
     * Returns a view of the portion of this immutable map whose keys are greater than or equal to <tt>fromKey</tt>. The
     * returned immutable map is backed by this immutable map, so changes in the returned immutable map are reflected in this
     * immutable map, and vice-versa. The returned immutable map supports all optional immutable map operations that this
     * immutable map supports.
     * <p>
     * The result of this method is a new, immutable sorted map.
     * @param fromKey low endpoint (inclusive) of the returned immutable map
     * @return a view of the portion of this immutable map whose keys are greater than or equal to <tt>fromKey</tt>
     * @throws ClassCastException if <tt>fromKey</tt> is not compatible with this immutable map's comparator (or, if the
     *             immutable map has no comparator, if <tt>fromKey</tt> does not implement {@link Comparable}).
     *             Implementations may, but are not required to, throw this exception if <tt>fromKey</tt> cannot be compared
     *             to keys currently in the immutable map.
     * @throws NullPointerException if <tt>fromKey</tt> is null and this immutable map does not permit null keys
     * @throws IllegalArgumentException if this immutable map itself has a restricted range, and <tt>fromKey</tt> lies
     *             outside the bounds of the range
     */
    ImmutableSortedMap<K, V> tailMap(K fromKey);

    /**
     * Returns the first (lowest) key currently in this immutable map.
     * @return the first (lowest) key currently in this immutable map
     * @throws NoSuchElementException if this immutable map is empty
     */
    K firstKey();

    /**
     * Returns the last (highest) key currently in this immutable map.
     * @return the last (highest) key currently in this immutable map
     * @throws NoSuchElementException if this immutable map is empty
     */
    K lastKey();
    
    /**
     * Return an ImmutableSortedSet view of the keys contained in this immutable map.
     * @return an ImmutableSortedSet view of the keys contained in this immutable map
     */
    ImmutableSortedSet<K> keySet();
}

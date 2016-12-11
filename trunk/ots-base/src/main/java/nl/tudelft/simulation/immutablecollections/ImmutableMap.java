package nl.tudelft.simulation.immutablecollections;

import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * A Map interface without the methods that can change it. The constructor of the ImmutableMap needs to be given an initial Map.
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
public interface ImmutableMap<K, V> extends Serializable
{
    /**
     * Returns the number of elements in this immutable collection. If this immutable collection contains more than
     * <tt>Integer.MAX_VALUE</tt> elements, returns <tt>Integer.MAX_VALUE</tt>.
     * @return the number of elements in this immutable collection
     */
    int size();

    /**
     * Returns <tt>true</tt> if this immutable collection contains no elements.
     * @return <tt>true</tt> if this immutable collection contains no elements
     */
    boolean isEmpty();

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified key. More formally, returns <tt>true</tt> if and
     * only if this map contains a mapping for a key <tt>k</tt> such that <tt>(key==null ? k==null : key.equals(k))</tt>. (There
     * can be at most one such mapping.)
     * @param key key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified key
     * @throws ClassCastException if the key is of an inappropriate type for this map
     * @throws NullPointerException if the specified key is null and this map does not permit null keys
     */
    boolean containsKey(Object key);

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value. More formally, returns <tt>true</tt> if
     * and only if this map contains at least one mapping to a value <tt>v</tt> such that
     * <tt>(value==null ? v==null : value.equals(v))</tt>. This operation will probably require time linear in the map size for
     * most implementations of the <tt>Map</tt> interface.
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the specified value
     * @throws ClassCastException if the value is of an inappropriate type for this map
     * @throws NullPointerException if the specified value is null and this map does not permit null values
     */
    boolean containsValue(Object value);

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the key.
     * <p>
     * More formally, if this map contains a mapping from a key {@code k} to a value {@code v} such that
     * {@code (key==null ? k==null : key.equals(k))}, then this method returns {@code v}; otherwise it returns {@code null}.
     * (There can be at most one such mapping.)
     * <p>
     * If this map permits null values, then a return value of {@code null} does not <i>necessarily</i> indicate that the map
     * contains no mapping for the key; it's also possible that the map explicitly maps the key to {@code null}. The
     * {@link #containsKey containsKey} operation may be used to distinguish these two cases.
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the key
     * @throws ClassCastException if the key is of an inappropriate type for this map
     * @throws NullPointerException if the specified key is null and this map does not permit null keys
     */
    V get(Object key);

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * @return an immutable set of the keys contained in this map
     */
    ImmutableSet<K> keySet();

    /**
     * Returns a {@link ImmutableCollection} view of the values contained in this map.
     * @return an immutable collection view of the values contained in this map
     */
    ImmutableCollection<V> values();

    /**
     * Returns the value to which the specified key is mapped, or {@code defaultValue} if this map contains no mapping for the
     * key. The default implementation makes no guarantees about synchronization or atomicity properties of this method. Any
     * implementation providing atomicity guarantees must override this method and document its concurrency properties.
     * @param key the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or {@code defaultValue} if this map contains no mapping for the
     *         key
     * @throws ClassCastException if the key is of an inappropriate type for this map
     * @throws NullPointerException if the specified key is null and this map does not permit null keys
     */
    default V getOrDefault(Object key, V defaultValue)
    {
        V v = get(key);
        return ((v != null) || containsKey(key)) ? v : defaultValue;
    }

    /**
     * Performs the given action for each entry in this map until all entries have been processed or the action throws an
     * exception. Unless otherwise specified by the implementing class, actions are performed in the order of entry set
     * iteration (if an iteration order is specified.) Exceptions thrown by the action are relayed to the caller. The default
     * implementation makes no guarantees about synchronization or atomicity properties of this method. Any implementation
     * providing atomicity guarantees must override this method and document its concurrency properties.
     * @param action The action to be performed for each entry
     * @throws NullPointerException if the specified action is null
     * @throws ConcurrentModificationException if an entry is found to be removed during iteration
     */
    default void forEach(BiConsumer<? super K, ? super V> action)
    {
        Objects.requireNonNull(action);
        for (Map.Entry<K, V> entry : toMap().entrySet())
        {
            K k;
            V v;
            try
            {
                k = entry.getKey();
                v = entry.getValue();
            }
            catch (IllegalStateException ise)
            {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
            action.accept(k, v);
        }
    }

    /**
     * Returns a modifiable copy of this immutable list.
     * @return a modifiable copy of this immutable list.
     */
    Map<K, V> toMap();

    /**
     * Force to redefine equals for the implementations of immutable collection classes.
     * @param obj the object to compare this collection with
     * @return whether the objects are equal
     */
    boolean equals(final Object obj);

    /**
     * Force to redefine hashCode for the implementations of immutable collection classes.
     * @return the calculated hashCode
     */
    int hashCode();

    /**
     * Return whether the internal storage is a wrapped pointer to the original map. If true, this means that anyone holding a
     * pointer to this data structure can still change it. The users of the ImmutableMap itself can, however, not make any
     * changes.
     * @return boolean; whether the internal storage is a wrapped pointer to the original map
     */
    boolean isWrap();

    /**
     * Return whether the internal storage is a (shallow) copy of the original map. If true, this means that anyone holding a
     * pointer to the original of the data structure can not change it anymore. Nor can the users of the ImmutableMap itself
     * make any changes.
     * @return boolean; whether the internal storage is a safe copy of the original map
     */
    default boolean isCopy()
    {
        return !isWrap();
    }

}

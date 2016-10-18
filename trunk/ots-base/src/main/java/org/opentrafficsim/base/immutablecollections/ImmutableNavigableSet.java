package org.opentrafficsim.base.immutablecollections;

import java.util.Collections;
import java.util.Comparator;
import java.util.NavigableSet;

/**
 * A {@link ImmutableSortedSet} extended with navigation methods reporting closest matches for given search targets. Methods
 * {@code lower}, {@code floor}, {@code ceiling}, and {@code higher} return elements respectively less than, less than or equal,
 * greater than or equal, and greater than a given element, returning {@code null} if there is no such element. All methods from
 * java.util.NavigableSet that can change the set have been left out.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> the type of content of this Set
 */
public interface ImmutableNavigableSet<E> extends ImmutableSortedSet<E>
{
    /**
     * Returns a modifiable copy of this immutable set.
     * @return a modifiable copy of this immutable set.
     */
    NavigableSet<E> toSet();

    /**
     * Returns the greatest element in this immutable set strictly less than the given element, or {@code null} if there is no
     * such element.
     * @param e the value to match
     * @return the greatest element less than {@code e}, or {@code null} if there is no such element
     * @throws ClassCastException if the specified element cannot be compared with the elements currently in the immutable set
     * @throws NullPointerException if the specified element is null and this immutable set does not permit null elements
     */
    E lower(E e);

    /**
     * Returns the greatest element in this immutable set less than or equal to the given element, or {@code null} if there is
     * no such element.
     * @param e the value to match
     * @return the greatest element less than or equal to {@code e}, or {@code null} if there is no such element
     * @throws ClassCastException if the specified element cannot be compared with the elements currently in the immutable set
     * @throws NullPointerException if the specified element is null and this immutable set does not permit null elements
     */
    E floor(E e);

    /**
     * Returns the least element in this immutable set greater than or equal to the given element, or {@code null} if there is
     * no such element.
     * @param e the value to match
     * @return the least element greater than or equal to {@code e}, or {@code null} if there is no such element
     * @throws ClassCastException if the specified element cannot be compared with the elements currently in the immutable set
     * @throws NullPointerException if the specified element is null and this immutable set does not permit null elements
     */
    E ceiling(E e);

    /**
     * Returns the least element in this immutable set strictly greater than the given element, or {@code null} if there is no
     * such element.
     * @param e the value to match
     * @return the least element greater than {@code e}, or {@code null} if there is no such element
     * @throws ClassCastException if the specified element cannot be compared with the elements currently in the immutable set
     * @throws NullPointerException if the specified element is null and this immutable set does not permit null elements
     */
    E higher(E e);

    /**
     * Returns a reverse order view of the elements contained in this immutable set.
     * <p>
     * The returned immutable set has an ordering equivalent to
     * <tt>{@link Collections#reverseOrder(Comparator) Collections.reverseOrder}(comparator())</tt>. The expression
     * {@code s.descendingSet().descendingSet()} returns a view of {@code s} essentially equivalent to {@code s}.
     * @return a reverse order view of this immutable set
     */
    ImmutableNavigableSet<E> descendingSet();

    /**
     * Returns an immutable iterator over the elements in this immutable set, in descending order. Equivalent in effect to
     * {@code descendingSet().iterator()}.
     * @return an immutable iterator over the elements in this immutable set, in descending order
     */
    ImmutableIterator<E> descendingIterator();

    /**
     * Returns a view of the portion of this immutable set whose elements range from {@code fromElement} to {@code toElement}.
     * If {@code fromElement} and {@code toElement} are equal, the returned immutable set is empty unless {@code fromInclusive}
     * and {@code toInclusive} are both true.
     * @param fromElement low endpoint of the returned immutable set
     * @param fromInclusive {@code true} if the low endpoint is to be included in the returned view
     * @param toElement high endpoint of the returned immutable set
     * @param toInclusive {@code true} if the high endpoint is to be included in the returned view
     * @return a view of the portion of this immutable set whose elements range from {@code fromElement}, inclusive, to
     *         {@code toElement}, exclusive
     * @throws ClassCastException if {@code fromElement} and {@code toElement} cannot be compared to one another using this
     *             immutable set's comparator (or, if the immutable set has no comparator, using natural ordering).
     *             Implementations may, but are not required to, throw this exception if {@code fromElement} or
     *             {@code toElement} cannot be compared to elements currently in the immutable set.
     * @throws NullPointerException if {@code fromElement} or {@code toElement} is null and this immutable set does not permit
     *             null elements
     * @throws IllegalArgumentException if {@code fromElement} is greater than {@code toElement}; or if this immutable set
     *             itself has a restricted range, and {@code fromElement} or {@code toElement} lies outside the bounds of the
     *             range.
     */
    ImmutableNavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive);

    /**
     * Returns a view of the portion of this immutable set whose elements are less than (or equal to, if {@code inclusive} is
     * true) {@code toElement}.
     * @param toElement high endpoint of the returned immutable set
     * @param inclusive {@code true} if the high endpoint is to be included in the returned view
     * @return a view of the portion of this immutable set whose elements are less than (or equal to, if {@code inclusive} is
     *         true) {@code toElement}
     * @throws ClassCastException if {@code toElement} is not compatible with this immutable set's comparator (or, if the
     *             immutable set has no comparator, if {@code toElement} does not implement {@link Comparable}). Implementations
     *             may, but are not required to, throw this exception if {@code toElement} cannot be compared to elements
     *             currently in the immutable set.
     * @throws NullPointerException if {@code toElement} is null and this immutable set does not permit null elements
     * @throws IllegalArgumentException if this immutable set itself has a restricted range, and {@code toElement} lies outside
     *             the bounds of the range
     */
    ImmutableNavigableSet<E> headSet(E toElement, boolean inclusive);

    /**
     * Returns a view of the portion of this immutable set whose elements are greater than (or equal to, if {@code inclusive} is
     * true) {@code fromElement}.
     * @param fromElement low endpoint of the returned immutable set
     * @param inclusive {@code true} if the low endpoint is to be included in the returned view
     * @return a view of the portion of this immutable set whose elements are greater than or equal to {@code fromElement}
     * @throws ClassCastException if {@code fromElement} is not compatible with this immutable set's comparator (or, if the
     *             immutable set has no comparator, if {@code fromElement} does not implement {@link Comparable}).
     *             Implementations may, but are not required to, throw this exception if {@code fromElement} cannot be compared
     *             to elements currently in the immutable set.
     * @throws NullPointerException if {@code fromElement} is null and this immutable set does not permit null elements
     * @throws IllegalArgumentException if this immutable set itself has a restricted range, and {@code fromElement} lies
     *             outside the bounds of the range
     */
    ImmutableNavigableSet<E> tailSet(E fromElement, boolean inclusive);

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

}

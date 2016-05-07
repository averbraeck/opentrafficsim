package org.opentrafficsim.core.immutablecollections;

import java.util.List;

/**
 * A List interface without the methods that can change it. The constructor of the ImmutableList needs to be given an initial
 * List.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> the type of content of this List
 */
public interface ImmutableList<E> extends ImmutableCollection<E>
{
    /**
     * Returns the element at the specified position in this immutable list.
     * @param index index of the element to return
     * @return the element at the specified position in this immutable list
     * @throws IndexOutOfBoundsException if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    E get(int index);

    /**
     * Returns the index of the first occurrence of the specified element in this immutable list, or -1 if this immutable list
     * does not contain the element. More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>, or -1 if there is no such index.
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in this immutable list, or -1 if this immutable list
     *         does not contain the element
     * @throws ClassCastException if the type of the specified element is incompatible with this immutable list
     * @throws NullPointerException if the specified element is null and this immutable list does not permit null elements
     */
    int indexOf(Object o);

    /**
     * Returns the index of the last occurrence of the specified element in this immutable list, or -1 if this immutable list
     * does not contain the element. More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>, or -1 if there is no such index.
     * @param o element to search for
     * @return the index of the last occurrence of the specified element in this immutable list, or -1 if this immutable list
     *         does not contain the element
     * @throws ClassCastException if the type of the specified element is incompatible with this immutable list
     * @throws NullPointerException if the specified element is null and this immutable list does not permit null elements
     */
    int lastIndexOf(Object o);

    /**
     * Returns a safe, immutable copy of the portion of this immutable list between the specified <tt>fromIndex</tt>, inclusive,
     * and <tt>toIndex</tt>, exclusive. (If <tt>fromIndex</tt> and <tt>toIndex</tt> are equal, the returned immutable list is
     * empty).
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * @return a view of the specified range within this immutable list
     * @throws IndexOutOfBoundsException for an illegal endpoint index value (<tt>fromIndex &lt; 0 || toIndex &gt; size ||
     *         fromIndex &gt; toIndex</tt>)
     */
    ImmutableList<E> subList(int fromIndex, int toIndex);

    /**
     * Returns a modifiable copy of this immutable list.
     * @return a modifiable copy of this immutable list.
     */
    List<E> toList();

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

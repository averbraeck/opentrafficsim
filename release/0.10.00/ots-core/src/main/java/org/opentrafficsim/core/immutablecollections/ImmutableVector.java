package org.opentrafficsim.core.immutablecollections;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * An immutable wrapper for a Vector.
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
public class ImmutableVector<E> extends ImmutableAbstractList<E>
{
    /** */
    private static final long serialVersionUID = 20160507L;

    /**
     * @param list the list to use as the content of the immutable vector.
     */
    public ImmutableVector(final List<E> list)
    {
        super(new Vector<E>(list));
    }

    /**
     * @param list the list to use as the content of the immutable vector.
     */
    public ImmutableVector(final ImmutableList<E> list)
    {
        this(list.toList());
    }

    /** {@inheritDoc} */
    @Override
    protected final Vector<E> getList()
    {
        return (Vector<E>) super.getList();
    }

    /** {@inheritDoc} */
    @Override
    public final List<E> toList()
    {
        return new Vector<E>(getList());
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableList<E> subList(final int fromIndex, final int toIndex)
    {
        return new ImmutableVector<E>(getList().subList(fromIndex, toIndex));
    }

    /**
     * Copies the components of this immutable vector into the specified array. The item at index {@code k} in this immutable
     * vector is copied into component {@code k} of {@code anArray}.
     * @param anArray the array into which the components get copied
     * @throws NullPointerException if the given array is null
     * @throws IndexOutOfBoundsException if the specified array is not large enough to hold all the components of this immutable
     *             vector
     * @throws ArrayStoreException if a component of this immutable vector is not of a runtime type that can be stored in the
     *             specified array
     * @see #toArray(Object[])
     */
    public final void copyInto(final Object[] anArray)
    {
        getList().copyInto(anArray);
    }

    /**
     * Returns the current capacity of this immutable vector.
     * @return the current capacity of this immutable vector.
     */
    public final int capacity()
    {
        return getList().capacity();
    }

    /**
     * Returns an enumeration of the components of this vector. The returned {@code Enumeration} object will generate all items
     * in this vector. The first item generated is the item at index {@code 0}, then the item at index {@code 1}, and so on.
     * @return an enumeration of the components of this vector
     * @see Iterator
     */
    public final Enumeration<E> elements()
    {
        return getList().elements();
    }

    /**
     * Returns the index of the first occurrence of the specified element in this immutable vector, searching forwards from
     * {@code index}, or returns -1 if the element is not found. More formally, returns the lowest index {@code i} such that
     * <tt>(i&nbsp;&gt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i))))</tt>,
     * or -1 if there is no such index.
     * @param o element to search for
     * @param index index to start searching from
     * @return the index of the first occurrence of the element in this immutable vector at position {@code index} or later in
     *         the vector; {@code -1} if the element is not found.
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @see Object#equals(Object)
     */
    public final int indexOf(final Object o, final int index)
    {
        return getList().indexOf(o, index);
    }

    /**
     * Returns the index of the last occurrence of the specified element in this immutable vector, searching backwards from
     * {@code index}, or returns -1 if the element is not found. More formally, returns the highest index {@code i} such that
     * <tt>(i&nbsp;&lt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i))))</tt>,
     * or -1 if there is no such index.
     * @param o element to search for
     * @param index index to start searching backwards from
     * @return the index of the last occurrence of the element at position less than or equal to {@code index} in this immutable
     *         vector; -1 if the element is not found.
     * @throws IndexOutOfBoundsException if the specified index is greater than or equal to the current size of this immutable
     *             vector
     */
    public final int lastIndexOf(final Object o, final int index)
    {
        return getList().lastIndexOf(o, index);
    }

    /**
     * Returns the component at the specified index.
     * <p>
     * This method is identical in functionality to the {@link #get(int)} method (which is part of the {@link List} interface).
     * @param index an index into this immutable vector
     * @return the component at the specified index
     * @throws ArrayIndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >= size()})
     */
    public final E elementAt(final int index)
    {
        return getList().elementAt(index);
    }

    /**
     * Returns the first component (the item at index {@code 0}) of this immutable vector.
     * @return the first component of this immutable vector
     * @throws NoSuchElementException if this immutable vector has no components
     */
    public final E firstElement()
    {
        return getList().firstElement();
    }

    /**
     * Returns the last component of the immutable vector.
     * @return the last component of the immutable vector, i.e., the component at index <code>size()&nbsp;-&nbsp;1</code>.
     * @throws NoSuchElementException if this immutable vector is empty
     */
    public final E lastElement()
    {
        return getList().lastElement();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        List<E> list = getList();
        if (null == list)
        {
            return "ImmutableVector []";
        }
        return "ImmutableVector [" + list.toString() + "]";
    }

}

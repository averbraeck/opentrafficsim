package org.opentrafficsim.core.immutablecollections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A Collection interface without the methods that can change it.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> the type of content of this Collection
 */
public interface ImmutableCollection<E> extends Iterable<E>, Serializable
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
     * Returns <tt>true</tt> if this immutable collection contains the specified element. More formally, returns <tt>true</tt>
     * if and only if this immutable collection contains at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     * @param o element whose presence in this immutable collection is to be tested
     * @return <tt>true</tt> if this immutable collection contains the specified element
     * @throws ClassCastException if the type of the specified element is incompatible with this immutable collection
     * @throws NullPointerException if the specified element is null and this immutable collection does not permit null elements
     */
    boolean contains(Object o);

    /**
     * Returns an immutable iterator over the elements in this immutable collection. There are no guarantees concerning the
     * order in which the elements are returned (unless this immutable collection is an instance of some class that provides a
     * guarantee). The ImmutableIterator is an Iterator where the remove() operation will throw an exception.
     * @return an <tt>ImmutableIterator</tt> over the elements in this immutable collection
     */
    ImmutableIterator<E> iterator();

    /**
     * Returns an array containing all of the elements in this immutable collection. If this immutable collection makes any
     * guarantees as to what order its elements are returned by its iterator, this method must return the elements in the same
     * order.
     * <p>
     * See java.util.Collection.toArray() for more details.
     * @return an array containing all of the elements in this immutable collection
     */
    Object[] toArray();

    /**
     * Returns an array containing all of the elements in this immutable collection; the runtime type of the returned array is
     * that of the specified array. If the immutable collection fits in the specified array, it is returned therein. Otherwise,
     * a new array is allocated with the runtime type of the specified array and the size of this immutable collection.
     * <p>
     * See java.util.Collection.toArray(T[]) for more details.
     * @param <T> the runtime type of the array to contain the immutable collection
     * @param a the array into which the elements of this immutable collection are to be stored, if it is big enough; otherwise,
     *            a new array of the same runtime type is allocated for this purpose.
     * @return an array containing all of the elements in this immutable collection
     * @throws ArrayStoreException if the runtime type of the specified array is not a supertype of the runtime type of every
     *             element in this immutable collection
     * @throws NullPointerException if the specified array is null
     */
    <T> T[] toArray(T[] a);

    /**
     * Returns <tt>true</tt> if this immutable collection contains all of the elements in the specified collection.
     * @param c collection to be checked for containment in this immutable collection
     * @return <tt>true</tt> if this immutable collection contains all of the elements in the specified collection
     * @throws ClassCastException if the types of one or more elements in the specified collection are incompatible with this
     *             immutable collection
     * @throws NullPointerException if the specified collection contains one or more null elements and this immutable collection
     *             does not permit null elements, or if the specified collection is null.
     * @see #contains(Object)
     */
    boolean containsAll(Collection<?> c);

    /**
     * Returns <tt>true</tt> if this immutable collection contains all of the elements in the specified immutable collection.
     * @param c immutable collection to be checked for containment in this immutable collection
     * @return <tt>true</tt> if this immutable collection contains all of the elements in the specified immutable collection
     * @throws ClassCastException if the types of one or more elements in the specified immutable collection are incompatible
     *             with this immutable collection
     * @throws NullPointerException if the specified immutable collection contains one or more null elements and this immutable
     *             collection does not permit null elements, or if the specified immutable collection is null.
     * @see #contains(Object)
     */
    boolean containsAll(ImmutableCollection<?> c);

    /**
     * Creates a Spliterator over the elements in this collection. Implementations should document characteristic values
     * reported by the spliterator. See java.util.Collection for more information.
     * @return a {@code Spliterator} over the elements in this collection
     */
    @Override
    default Spliterator<E> spliterator()
    {
        return Spliterators.spliterator(toCollection(), 0);
    }

    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     * <p>
     * This method should be overridden when the {@link #spliterator()} method cannot return a spliterator that is
     * {@code IMMUTABLE}, {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()} for details.)
     * @implSpec The default implementation creates a sequential {@code Stream} from the collection's {@code Spliterator}.
     * @return a sequential {@code Stream} over the elements in this collection
     */
    default Stream<E> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns a possibly parallel {@code Stream} with this collection as its source. It is allowable for this method to return
     * a sequential stream.
     * <p>
     * This method should be overridden when the {@link #spliterator()} method cannot return a spliterator that is
     * {@code IMMUTABLE}, {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()} for details.)
     * @implSpec The default implementation creates a parallel {@code Stream} from the collection's {@code Spliterator}.
     * @return a possibly parallel {@code Stream} over the elements in this collection
     */
    default Stream<E> parallelStream()
    {
        return StreamSupport.stream(spliterator(), true);
    }

    /**
     * Returns a modifiable copy of this immutable list.
     * @return a modifiable copy of this immutable list.
     */
    Collection<E> toCollection();
}

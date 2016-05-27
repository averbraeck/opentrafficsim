package org.opentrafficsim.core.immutablecollections;

import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * An abstract base class for an immutable wrapper for a List.
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
public abstract class ImmutableAbstractList<E> implements ImmutableList<E>, RandomAccess
{
    /** */
    private static final long serialVersionUID = 20160507L;

    /** the list that is wrapped, without giving access to methods that can change it. */
    private final List<E> list;

    /**
     * Construct an abstract immutable list. Make sure that the argument is a safe copy of the list of the right type!
     * @param list a safe copy of the list to use as the immutable list
     */
    protected ImmutableAbstractList(final List<E> list)
    {
        this.list = list;
    }

    /**
     * Prepare the list of the right type for use a subclass. Implement e.g. as follows:
     * 
     * <pre>
     * {@literal @}Override
     * protected ArrayList&lt;E&gt; getList()
     * {
     *     return (ArrayList&lt;E&gt;) super.getList();
     * }
     * </pre>
     * @return the list of the right type for use a subclass
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected List<E> getList()
    {
        return this.list;
    }

    /** {@inheritDoc} */
    @Override
    public final Collection<E> toCollection()
    {
        return toList();
    }

    /** {@inheritDoc} */
    @Override
    public final int size()
    {
        return this.list.size();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isEmpty()
    {
        return this.list.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean contains(final Object o)
    {
        return this.list.contains(o);
    }

    /** {@inheritDoc} */
    @Override
    public final int indexOf(final Object o)
    {
        return this.list.indexOf(o);
    }

    /** {@inheritDoc} */
    @Override
    public final int lastIndexOf(final Object o)
    {
        return this.list.lastIndexOf(o);
    }

    /** {@inheritDoc} */
    @Override
    public final Object[] toArray()
    {
        return this.list.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public final <T> T[] toArray(final T[] a)
    {
        return this.list.toArray(a);
    }

    /** {@inheritDoc} */
    @Override
    public final E get(final int index)
    {
        return this.list.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableIterator<E> iterator()
    {
        return new ImmutableIterator<E>(this.list.iterator());
    }

    /** {@inheritDoc} */
    @Override
    public final void forEach(final Consumer<? super E> action)
    {
        this.list.forEach(action);
    }

    /** {@inheritDoc} */
    @Override
    public final Spliterator<E> spliterator()
    {
        return this.list.spliterator();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsAll(final Collection<?> c)
    {
        return this.list.containsAll(c);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsAll(final ImmutableCollection<?> c)
    {
        return this.list.containsAll(c.toCollection());
    }

    /** {@inheritDoc} */
    @Override
    public final Stream<E> stream()
    {
        return this.list.stream();
    }

    /** {@inheritDoc} */
    @Override
    public final Stream<E> parallelStream()
    {
        return this.list.parallelStream();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.list == null) ? 0 : this.list.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({ "checkstyle:designforextension", "checkstyle:needbraces" })
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImmutableAbstractList<?> other = (ImmutableAbstractList<?>) obj;
        if (this.list == null)
        {
            if (other.list != null)
                return false;
        }
        else if (!this.list.equals(other.list))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "Immutable[" + this.list.toString() + "]";
    }
}

package org.opentrafficsim.core.immutablecollections;

import java.util.Collection;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.opentrafficsim.core.Throw;

/**
 * An abstract base class for an immutable wrapper for a Set.
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
public abstract class ImmutableAbstractSet<E> implements ImmutableSet<E>
{
    /** */
    private static final long serialVersionUID = 20160507L;

    /** the set that is wrapped, without giving access to methods that can change it. */
    private final Collection<E> collection;

    /** COPY stores a safe, internal copy of the collection; WRAP stores a pointer to the original collection. */
    private final Immutable copyOrWrap;

    /**
     * Construct an abstract immutable set. Make sure that the argument is a safe copy of the set of the right type!
     * @param collection a safe copy of the collection to use as the immutable set
     * @param copy indicate whether the immutable is a copy or a wrap
     */
    protected ImmutableAbstractSet(final Collection<E> collection, final boolean copy)
    {
        Throw.whenNull(collection, "the collection argument cannot be null");
        this.collection = collection;
        this.copyOrWrap = copy ? Immutable.COPY : Immutable.WRAP;
    }

    /**
     * Prepare the set of the right type for use a subclass. Implement e.g. as follows:
     * 
     * <pre>
     * {@literal @}Override
     * protected ArraySet&lt;E&gt; getSet()
     * {
     *     return (ArraySet&lt;E&gt;) super.getSet();
     * }
     * </pre>
     * @return the set of the right type for use a subclass
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected Collection<E> getSet()
    {
        return this.collection;
    }

    /** {@inheritDoc} */
    @Override
    public final Collection<E> toCollection()
    {
        return toSet();
    }

    /** {@inheritDoc} */
    @Override
    public final int size()
    {
        return this.collection.size();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isEmpty()
    {
        return this.collection.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean contains(final Object o)
    {
        return this.collection.contains(o);
    }

    /** {@inheritDoc} */
    @Override
    public final Object[] toArray()
    {
        return this.collection.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public final <T> T[] toArray(final T[] a)
    {
        return this.collection.toArray(a);
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableIterator<E> iterator()
    {
        return new ImmutableIterator<E>(this.collection.iterator());
    }

    /** {@inheritDoc} */
    @Override
    public final void forEach(final Consumer<? super E> action)
    {
        this.collection.forEach(action);
    }

    /** {@inheritDoc} */
    @Override
    public final Spliterator<E> spliterator()
    {
        return this.collection.spliterator();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsAll(final Collection<?> c)
    {
        return this.collection.containsAll(c);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsAll(final ImmutableCollection<?> c)
    {
        return this.collection.containsAll(c.toCollection());
    }

    /** {@inheritDoc} */
    @Override
    public final Stream<E> stream()
    {
        return this.collection.stream();
    }

    /** {@inheritDoc} */
    @Override
    public final Stream<E> parallelStream()
    {
        return this.collection.parallelStream();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isWrap()
    {
        return this.copyOrWrap == Immutable.WRAP;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.collection == null) ? 0 : this.collection.hashCode());
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
        ImmutableAbstractSet<?> other = (ImmutableAbstractSet<?>) obj;
        if (this.collection == null)
        {
            if (other.collection != null)
                return false;
        }
        else if (!this.collection.equals(other.collection))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "Immutable[" + this.collection.toString() + "]";
    }
}

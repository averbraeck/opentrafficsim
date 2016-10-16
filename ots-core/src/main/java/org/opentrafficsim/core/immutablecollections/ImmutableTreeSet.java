package org.opentrafficsim.core.immutablecollections;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import nl.tudelft.simulation.language.Throw;

/**
 * An immutable wrapper for a TreeSet.
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
public class ImmutableTreeSet<E> extends ImmutableAbstractSet<E> implements ImmutableNavigableSet<E>
{
    /** */
    private static final long serialVersionUID = 20160507L;

    /**
     * @param sortedSet the collection to use as the immutable set.
     */
    public ImmutableTreeSet(final SortedSet<E> sortedSet)
    {
        this(sortedSet, Immutable.COPY);
    }

    /**
     * @param sortedSet the collection to use as the immutable set.
     * @param copyOrWrap COPY stores a safe, internal copy of the collection; WRAP stores a pointer to the original collection
     */
    public ImmutableTreeSet(final SortedSet<E> sortedSet, final Immutable copyOrWrap)
    {
        super(copyOrWrap == Immutable.COPY ? new TreeSet<E>(sortedSet) : sortedSet, copyOrWrap == Immutable.COPY);
        Throw.whenNull(copyOrWrap, "the copyOrWrap argument should be Immutable.COPY or Immutable.WRAP");
    }

    /**
     * @param immutableSortedSet the collection to use as the immutable set.
     */
    public ImmutableTreeSet(final ImmutableSortedSet<E> immutableSortedSet)
    {
        this(immutableSortedSet, Immutable.COPY);
    }

    /**
     * @param immutableSortedSet the collection to use as the immutable set.
     * @param copyOrWrap COPY stores a safe, internal copy of the collection; WRAP stores a pointer to the original collection
     */
    public ImmutableTreeSet(final ImmutableSortedSet<E> immutableSortedSet, final Immutable copyOrWrap)
    {
        this(immutableSortedSet.toSet(), copyOrWrap);
    }

    /** {@inheritDoc} */
    @Override
    protected final NavigableSet<E> getSet()
    {
        return (NavigableSet<E>) super.getSet();
    }

    /** {@inheritDoc} */
    @Override
    public final NavigableSet<E> toSet()
    {
        return new TreeSet<E>(super.getSet());
    }

    /** {@inheritDoc} */
    @Override
    public final Comparator<? super E> comparator()
    {
        return getSet().comparator();
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableSortedSet<E> subSet(final E fromElement, final E toElement)
    {
        return new ImmutableTreeSet<E>(getSet().subSet(fromElement, toElement));
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableSortedSet<E> headSet(final E toElement)
    {
        return new ImmutableTreeSet<E>(getSet().headSet(toElement));
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableSortedSet<E> tailSet(final E fromElement)
    {
        return new ImmutableTreeSet<E>(getSet().tailSet(fromElement));
    }

    /** {@inheritDoc} */
    @Override
    public final E first()
    {
        return getSet().first();
    }

    /** {@inheritDoc} */
    @Override
    public final E last()
    {
        return getSet().last();
    }

    /** {@inheritDoc} */
    @Override
    public final E lower(final E e)
    {
        return getSet().lower(e);
    }

    /** {@inheritDoc} */
    @Override
    public final E floor(final E e)
    {
        return getSet().floor(e);
    }

    /** {@inheritDoc} */
    @Override
    public final E ceiling(final E e)
    {
        return getSet().ceiling(e);
    }

    /** {@inheritDoc} */
    @Override
    public final E higher(final E e)
    {
        return getSet().higher(e);
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableNavigableSet<E> descendingSet()
    {
        return new ImmutableTreeSet<E>(getSet().descendingSet());
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableIterator<E> descendingIterator()
    {
        return new ImmutableIterator<E>(getSet().descendingIterator());
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableNavigableSet<E> subSet(final E fromElement, final boolean fromInclusive, final E toElement,
            final boolean toInclusive)
    {
        return new ImmutableTreeSet<E>(getSet().subSet(fromElement, fromInclusive, toElement, toInclusive));
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableNavigableSet<E> headSet(final E toElement, final boolean inclusive)
    {
        return new ImmutableTreeSet<E>(getSet().headSet(toElement, inclusive));
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableNavigableSet<E> tailSet(final E fromElement, final boolean inclusive)
    {
        return new ImmutableTreeSet<E>(getSet().tailSet(fromElement, inclusive));
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        NavigableSet<E> set = getSet();
        if (null == set)
        {
            return "ImmutableTreeSet []";
        }
        return "ImmutableTreeSet [" + set.toString() + "]";
    }

}

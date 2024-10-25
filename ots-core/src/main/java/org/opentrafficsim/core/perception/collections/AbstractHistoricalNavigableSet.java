package org.opentrafficsim.core.perception.collections;

import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableSet;

import org.opentrafficsim.core.perception.HistoryManager;

/**
 * NavigableSet-valued historical state. The current navigable set is always maintained, and past states of the navigable set
 * are obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()} method. Any returned subset is
 * unmodifiable.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <E> element type
 * @param <S> navigable set type
 */
public abstract class AbstractHistoricalNavigableSet<E, S extends NavigableSet<E>> extends AbstractHistoricalSortedSet<E, S>
        implements HistoricalNavigableSet<E>
{

    /**
     * Constructor.
     * @param historyManager history manager
     * @param set initial set
     */
    protected AbstractHistoricalNavigableSet(final HistoryManager historyManager, final S set)
    {
        super(historyManager, set);
    }

    @Override
    public String toString()
    {
        return "HistoricalNavigableSet [current=" + getCollection() + "]";
    }

    // Altering NavigableSet methods

    @Override
    public E pollFirst()
    {
        if (getCollection().isEmpty())
        {
            return null;
        }
        E element = getCollection().first();
        remove(element);
        return element;
    }

    @Override
    public E pollLast()
    {
        if (getCollection().isEmpty())
        {
            return null;
        }
        E element = getCollection().last();
        remove(element);
        return element;
    }

    // Non-altering NavigableSet methods

    @Override
    public E lower(final E e)
    {
        return getCollection().lower(e);
    }

    @Override
    public E floor(final E e)
    {
        return getCollection().floor(e);
    }

    @Override
    public E ceiling(final E e)
    {
        return getCollection().ceiling(e);
    }

    @Override
    public E higher(final E e)
    {
        return getCollection().higher(e);
    }

    @Override
    public NavigableSet<E> descendingSet()
    {
        return Collections.unmodifiableNavigableSet(getCollection().descendingSet());
    }

    @Override
    public Iterator<E> descendingIterator()
    {
        return Collections.unmodifiableNavigableSet(getCollection()).descendingIterator();
    }

    @Override
    public NavigableSet<E> subSet(final E fromElement, final boolean fromInclusive, final E toElement,
            final boolean toInclusive)
    {
        return Collections.unmodifiableNavigableSet(getCollection().subSet(fromElement, fromInclusive, toElement, toInclusive));
    }

    @Override
    public NavigableSet<E> headSet(final E toElement, final boolean inclusive)
    {
        return Collections.unmodifiableNavigableSet(getCollection().headSet(toElement, inclusive));
    }

    @Override
    public NavigableSet<E> tailSet(final E fromElement, final boolean inclusive)
    {
        return Collections.unmodifiableNavigableSet(getCollection().tailSet(fromElement, inclusive));
    }

}

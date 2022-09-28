package org.opentrafficsim.core.perception.collections;

import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;

import org.opentrafficsim.core.perception.HistoryManager;

/**
 * SortedSet-valued historical state. The current sorted set is always maintained, and past states of the sorted set are
 * obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()} method. Any returned subset is
 * unmodifiable.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> element type
 * @param <S> sorted set type
 */
public abstract class AbstractHistoricalSortedSet<E, S extends SortedSet<E>> extends AbstractHistoricalCollection<E, S>
        implements HistoricalSortedSet<E>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param set S; initial set
     */
    protected AbstractHistoricalSortedSet(final HistoryManager historyManager, final S set)
    {
        super(historyManager, set);
    }

    // Non-altering SortedSet methods

    /** {@inheritDoc} */
    @Override
    public Comparator<? super E> comparator()
    {
        return getCollection().comparator();
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<E> subSet(final E fromElement, final E toElement)
    {
        return Collections.unmodifiableSortedSet(getCollection().subSet(fromElement, toElement));
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<E> headSet(final E toElement)
    {
        return Collections.unmodifiableSortedSet(getCollection().headSet(toElement));
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<E> tailSet(final E fromElement)
    {
        return Collections.unmodifiableSortedSet(getCollection().tailSet(fromElement));
    }

    /** {@inheritDoc} */
    @Override
    public E first()
    {
        return getCollection().first();
    }

    /** {@inheritDoc} */
    @Override
    public E last()
    {
        return getCollection().last();
    }

}

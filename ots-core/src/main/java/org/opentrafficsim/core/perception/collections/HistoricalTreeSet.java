package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * TreeSet-valued historical state. The current tree set is always maintained, and past states of the tree set are obtained by
 * applying the events between now and the requested time in reverse.<br>
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
 */
public class HistoricalTreeSet<E> extends AbstractHistoricalNavigableSet<E, TreeSet<E>>
{

    /**
     * Constructor.
     * @param historyManager history manager
     */
    public HistoricalTreeSet(final HistoryManager historyManager)
    {
        super(historyManager, new TreeSet<>());
    }

    /**
     * Constructor.
     * @param historyManager history manager
     * @param c initial collection
     */
    public HistoricalTreeSet(final HistoryManager historyManager, final Collection<? extends E> c)
    {
        super(historyManager, new TreeSet<>(c));
    }

    /** {@inheritDoc} */
    @Override
    public TreeSet<E> get()
    {
        return getCollection();
    }

    /** {@inheritDoc} */
    @Override
    public TreeSet<E> get(final Time time)
    {
        if (isLastState(time))
        {
            return getCollection();
        }
        return fill(time, new TreeSet<>());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalTreeSet [current=" + getCollection() + "]";
    }

}

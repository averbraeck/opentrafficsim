package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Duration;
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
     * @param owner object that owns the historical value
     */
    public HistoricalTreeSet(final HistoryManager historyManager, final Object owner)
    {
        super(historyManager, owner, new TreeSet<>());
    }

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param c initial collection
     */
    public HistoricalTreeSet(final HistoryManager historyManager, final Object owner, final Collection<? extends E> c)
    {
        super(historyManager, owner, new TreeSet<>(c));
    }

    @Override
    public TreeSet<E> get()
    {
        return getCollection();
    }

    @Override
    public TreeSet<E> get(final Duration time)
    {
        if (isLastState(time))
        {
            return getCollection();
        }
        return fill(time, new TreeSet<>());
    }

    @Override
    public String toString()
    {
        return "HistoricalTreeSet [current=" + getCollection() + "]";
    }

}

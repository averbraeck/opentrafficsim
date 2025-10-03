package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.PriorityQueue;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * PriorityQueue-valued historical state. The current priority queue is always maintained, and past states of the priority queue
 * are obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()}, {@code add()} and {@code set()} methods.
 * Any returned sublist is unmodifiable.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <E> element type
 */
public class HistoricalPriorityQueue<E> extends AbstractHistoricalQueue<E, PriorityQueue<E>>
{

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     */
    public HistoricalPriorityQueue(final HistoryManager historyManager, final Object owner)
    {
        super(historyManager, owner, new PriorityQueue<>());
    }

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param c initial collection
     */
    public HistoricalPriorityQueue(final HistoryManager historyManager, final Object owner, final Collection<? extends E> c)
    {
        super(historyManager, owner, new PriorityQueue<>(c));
    }

    @Override
    public PriorityQueue<E> get()
    {
        return getCollection();
    }

    @Override
    public PriorityQueue<E> get(final Duration time)
    {
        if (isLastState(time))
        {
            return getCollection();
        }
        return fill(time, new PriorityQueue<E>());
    }

    @Override
    public String toString()
    {
        return "HistoricalPriorityQueue [current=" + getCollection() + "]";
    }

}

package org.opentrafficsim.core.perception.collections;

import java.util.Queue;

import org.opentrafficsim.core.perception.HistoryManager;

/**
 * Queue-valued historical state. The current queue is always maintained, and past states of the queue are obtained by applying
 * the events between now and the requested time in reverse.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()} method.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <E> element type
 * @param <Q> queue type
 */
public abstract class AbstractHistoricalQueue<E, Q extends Queue<E>> extends AbstractHistoricalCollection<E, Q>
        implements HistoricalQueue<E>
{

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param queue initial queue
     */
    protected AbstractHistoricalQueue(final HistoryManager historyManager, final Object owner, final Q queue)
    {
        super(historyManager, owner, queue);
    }

    // Altering PriorityQueue methods

    @Override
    public boolean offer(final E e)
    {
        boolean added = getCollection().offer(e);
        if (added)
        {
            addEvent(new AddEvent<>(now().si, e));
        }
        return added;
    }

    @Override
    public E remove()
    {
        E e = getCollection().remove();
        addEvent(new RemoveEvent<>(now().si, e));
        return e;
    }

    @Override
    public E poll()
    {
        if (isEmpty())
        {
            return null;
        }
        return remove();
    }

    // Non-altering PriorityQueue methods

    @Override
    public E peek()
    {
        return this.getCollection().peek();
    }

    @Override
    public E element()
    {
        return this.getCollection().element();
    }

}

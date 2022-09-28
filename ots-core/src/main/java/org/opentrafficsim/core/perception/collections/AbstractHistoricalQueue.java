package org.opentrafficsim.core.perception.collections;

import java.util.Queue;

import org.opentrafficsim.core.perception.HistoryManager;

/**
 * Queue-valued historical state. The current queue is always maintained, and past states of the queue are obtained by applying
 * the events between now and the requested time in reverse.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()} method.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <E> element type
 * @param <Q> queue type
 */
public abstract class AbstractHistoricalQueue<E, Q extends Queue<E>> extends AbstractHistoricalCollection<E, Q>
        implements HistoricalQueue<E>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param queue Q; initial queue
     */
    protected AbstractHistoricalQueue(final HistoryManager historyManager, final Q queue)
    {
        super(historyManager, queue);
    }

    // Altering PriorityQueue methods

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public E remove()
    {
        E e = getCollection().remove();
        addEvent(new RemoveEvent<>(now().si, e));
        return e;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public E peek()
    {
        return this.getCollection().peek();
    }

    /** {@inheritDoc} */
    @Override
    public E element()
    {
        return this.getCollection().element();
    }

}

package org.opentrafficsim.core.perception.collections;

import java.util.Queue;

import org.opentrafficsim.core.perception.HistoryManager;

/**
 * Queue-valued historical state. The current queue is always maintained, and past states of the queue are obtained by applying
 * the events between now and the requested time in reverse.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()} method.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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

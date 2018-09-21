package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.PriorityQueue;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * PriorityQueue-valued historical state. The current priority queue is always maintained, and past states of the priority queue
 * are obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()}, {@code add()} and {@code set()} methods.
 * Any returned sublist is unmodifiable.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> element type
 */
public class HistoricalPriorityQueue<E> extends AbstractHistoricalQueue<E, PriorityQueue<E>>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     */
    public HistoricalPriorityQueue(final HistoryManager historyManager)
    {
        super(historyManager, new PriorityQueue<>());
    }

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param c Collection; initial collection
     */
    public HistoricalPriorityQueue(final HistoryManager historyManager, final Collection<? extends E> c)
    {
        super(historyManager, new PriorityQueue<>(c));
    }

    /** {@inheritDoc} */
    @Override
    public PriorityQueue<E> get()
    {
        return getCollection();
    }

    /** {@inheritDoc} */
    @Override
    public PriorityQueue<E> get(final Time time)
    {
        if (isLastState(time))
        {
            return getCollection();
        }
        return fill(time, new PriorityQueue<E>());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalPriorityQueue [current=" + getCollection() + "]";
    }

}

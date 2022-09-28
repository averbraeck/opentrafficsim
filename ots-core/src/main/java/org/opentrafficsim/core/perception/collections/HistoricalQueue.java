package org.opentrafficsim.core.perception.collections;

import java.util.Queue;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for historical queues.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <E> element type
 */
public interface HistoricalQueue<E> extends HistoricalCollection<E>, Queue<E>
{

    /**
     * Returns the current queue.
     * @return Queue; current queue
     */
    @Override
    Queue<E> get();

    /**
     * Returns a past queue.
     * @param time Time; time to obtain the queue at
     * @return Queue; past queue
     */
    @Override
    Queue<E> get(Time time);

}

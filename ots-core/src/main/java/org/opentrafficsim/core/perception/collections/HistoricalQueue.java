package org.opentrafficsim.core.perception.collections;

import java.util.Queue;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Interface for historical queues.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 * @param <E> element type
 */
public interface HistoricalQueue<E> extends HistoricalCollection<E>, Queue<E>
{

    /**
     * Returns the current queue.
     * @return current queue
     */
    @Override
    Queue<E> get();

    /**
     * Returns a past queue.
     * @param time simulation time to obtain the queue at
     * @return past queue
     */
    @Override
    Queue<E> get(Duration time);

}

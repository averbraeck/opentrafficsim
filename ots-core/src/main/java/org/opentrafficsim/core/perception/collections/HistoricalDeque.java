package org.opentrafficsim.core.perception.collections;

import java.util.Deque;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Interface for historical deques.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 * @param <E> element type
 */
public interface HistoricalDeque<E> extends HistoricalQueue<E>, Deque<E>
{

    /**
     * Returns the current deque.
     * @return current deque
     */
    @Override
    Deque<E> get();

    /**
     * Returns a past deque.
     * @param time simulation time to obtain the deque at
     * @return past deque
     */
    @Override
    Deque<E> get(Duration time);

}

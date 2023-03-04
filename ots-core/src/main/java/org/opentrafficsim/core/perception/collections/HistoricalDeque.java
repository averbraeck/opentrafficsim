package org.opentrafficsim.core.perception.collections;

import java.util.Deque;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for historical deques.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <E> element type
 */
public interface HistoricalDeque<E> extends HistoricalQueue<E>, Deque<E>
{

    /**
     * Returns the current deque.
     * @return Deque; current deque
     */
    @Override
    Deque<E> get();

    /**
     * Returns a past deque.
     * @param time Time; time to obtain the deque at
     * @return Deque; past deque
     */
    @Override
    Deque<E> get(Time time);

}

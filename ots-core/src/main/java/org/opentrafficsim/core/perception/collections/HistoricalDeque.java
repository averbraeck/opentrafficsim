package org.opentrafficsim.core.perception.collections;

import java.util.Deque;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for historical deques.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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

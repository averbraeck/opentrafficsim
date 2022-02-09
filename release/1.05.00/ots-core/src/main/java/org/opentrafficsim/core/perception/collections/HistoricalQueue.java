package org.opentrafficsim.core.perception.collections;

import java.util.Queue;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for historical queues.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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

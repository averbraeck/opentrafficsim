package org.opentrafficsim.core.perception.collections;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for historical sorted sets.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> element type
 */
public interface HistoricalSortedSet<E> extends HistoricalSet<E>, SortedSet<E>
{

    /**
     * Returns the current sorted set.
     * @return SortedSet; current sorted set
     */
    @Override
    SortedSet<E> get();

    /**
     * Returns a past sorted set.
     * @param time Time; time to obtain the sorted set at
     * @return SortedSet; past sorted set
     */
    @Override
    SortedSet<E> get(Time time);

}

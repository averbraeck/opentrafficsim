package org.opentrafficsim.core.perception.collections;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Interface for historical sorted sets.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <E> element type
 */
public interface HistoricalSortedSet<E> extends HistoricalSet<E>, SortedSet<E>
{

    /**
     * Returns the current sorted set.
     * @return current sorted set
     */
    @Override
    SortedSet<E> get();

    /**
     * Returns a past sorted set.
     * @param time simulation time to obtain the sorted set at
     * @return past sorted set
     */
    @Override
    SortedSet<E> get(Duration time);

}

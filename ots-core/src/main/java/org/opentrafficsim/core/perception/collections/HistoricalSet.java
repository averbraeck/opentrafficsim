package org.opentrafficsim.core.perception.collections;

import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Interface for historical sets.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <E> element type
 */
public interface HistoricalSet<E> extends HistoricalCollection<E>, Set<E>
{

    /**
     * Returns the current set.
     * @return current set
     */
    @Override
    Set<E> get();

    /**
     * Returns a past set.
     * @param time simulation time to obtain the set at
     * @return past set
     */
    @Override
    Set<E> get(Duration time);

}

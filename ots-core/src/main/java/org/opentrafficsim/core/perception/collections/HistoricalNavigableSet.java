package org.opentrafficsim.core.perception.collections;

import java.util.NavigableSet;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Interface for historical navigable sets.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 * @param <E> element type
 */
public interface HistoricalNavigableSet<E> extends HistoricalSortedSet<E>, NavigableSet<E>
{

    /**
     * Returns the current navigable set.
     * @return current navigable set
     */
    @Override
    NavigableSet<E> get();

    /**
     * Returns a past navigable set.
     * @param time simulation time to obtain the navigable set at
     * @return past navigable set
     */
    @Override
    NavigableSet<E> get(Duration time);

}

package org.opentrafficsim.core.perception.collections;

import java.util.NavigableSet;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for historical navigable sets.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> element type
 */
public interface HistoricalNavigableSet<E> extends HistoricalSortedSet<E>, NavigableSet<E>
{

    /**
     * Returns the current navigable set.
     * @return NavigableSet; current navigable set
     */
    @Override
    NavigableSet<E> get();

    /**
     * Returns a past navigable set.
     * @param time Time; time to obtain the navigable set at
     * @return NavigableSet; past navigable set
     */
    @Override
    NavigableSet<E> get(Time time);

}

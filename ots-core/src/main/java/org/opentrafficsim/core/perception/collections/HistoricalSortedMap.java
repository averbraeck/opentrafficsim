package org.opentrafficsim.core.perception.collections;

import java.util.SortedMap;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Interface for historical sorted maps.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 */
public interface HistoricalSortedMap<K, V> extends HistoricalMap<K, V>, SortedMap<K, V>
{

    /**
     * Returns the current sorted map.
     * @return current sorted map
     */
    @Override
    SortedMap<K, V> get();

    /**
     * Returns a past sorted map.
     * @param time simulation time to obtain the sorted map at
     * @return past sorted map
     */
    @Override
    SortedMap<K, V> get(Duration time);

}

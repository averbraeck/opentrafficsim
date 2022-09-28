package org.opentrafficsim.core.perception.collections;

import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for historical maps.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 */
public interface HistoricalMap<K, V> extends Map<K, V>
{

    /**
     * Returns the current map.
     * @return Map; current map
     */
    Map<K, V> get();

    /**
     * Returns a past map.
     * @param time Time; time to obtain the map at
     * @return Map; past map
     */
    Map<K, V> get(Time time);

    /** {@inheritDoc} */
    @Override
    default void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function)
    {
        Objects.requireNonNull(function);
        Map<K, V> puts = new LinkedHashMap<>();
        for (Entry<K, V> entry : entrySet())
        {
            K k;
            V v;
            try
            {
                k = entry.getKey();
                v = entry.getValue();
            }
            catch (IllegalStateException ise)
            {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }

            // ise thrown from function is not a cme.
            v = function.apply(k, v);

            try
            {
                // super uses entry.setValue(v) which is not supported, can't use put() due to concurrency
                puts.put(k, v);
            }
            catch (IllegalStateException ise)
            {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
        }
        // second loop to perform the puts without concurrency
        for (Entry<K, V> entry : puts.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

}

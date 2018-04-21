package org.opentrafficsim.core.gtu;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import nl.tudelft.simulation.language.Throw;

/**
 * Utility class to cache data based on a variable (between cache instances) number of keys of any type.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 20 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type
 */
public class NestedCache<T>
{
    
    /** Key types. */
    private final Class<?>[] types;

    /** Map with cache. */
    private final Map<Object, Object> map = new HashMap<>();

    /**
     * Constructor.
     * @param types Class...; types
     */
    public NestedCache(final Class<?>... types)
    {
        this.types = types;
    }

    /**
     * Returns a value.
     * @param supplier Supplier; supplier of {@code T} for if it wasn't cached yet
     * @param keys List; list of key objects
     * @return T; value
     */
    public T getValue(final Supplier<T> supplier, final Object... keys)
    {
        return getValue(supplier, Arrays.asList(keys));
    }

    /**
     * Returns a value. This uses a {@code List} rather than an array to allow flexible inner workings.
     * @param supplier Supplier; supplier of {@code T} for if it wasn't cached yet
     * @param keys List; list of key objects
     * @return T; value
     */
    @SuppressWarnings("unchecked")
    private T getValue(final Supplier<T> supplier, final List<Object> keys)
    {
        Throw.when(keys.size() != this.types.length, IllegalArgumentException.class, "Incorrect number of keys.");
        Throw.when(!this.types[0].isAssignableFrom(keys.get(0).getClass()), IllegalArgumentException.class,
                "Key %s is not of %s.", keys.get(0), this.types[0]);
        Object sub = this.map.get(keys.get(0));
        if (this.types.length == 1)
        {
            if (sub == null)
            {
                sub = supplier.get();
                this.map.put(keys.get(0), sub);
            }
            return (T) sub;
        }
        if (sub == null)
        {
            // create sub-NestedCache with 1 less key
            Class<Object>[] subTypes = new Class[this.types.length - 1];
            System.arraycopy(this.types, 1, subTypes, 0, this.types.length - 1);
            sub = new NestedCache<T>(subTypes);
            this.map.put(keys.get(0), sub);
        }
        // return from sub-NestedCache with 1 less key
        return ((NestedCache<T>) sub).getValue(supplier, keys.subList(1, keys.size()));
    }

}

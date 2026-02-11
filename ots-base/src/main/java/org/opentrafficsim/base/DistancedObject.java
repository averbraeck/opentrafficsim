package org.opentrafficsim.base;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;

/**
 * Wrapper for object and its distance.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param object underlying object
 * @param distance distance to object
 * @param <T> underlying object type
 */
public record DistancedObject<T>(T object, Length distance) implements Comparable<DistancedObject<T>>
{

    /**
     * Constructor.
     */
    public DistancedObject
    {
        Throw.whenNull(object, "object");
        Throw.whenNull(distance, "distance");
    }

    @Override
    public int compareTo(final DistancedObject<T> o)
    {
        int out = this.distance.compareTo(o.distance);
        if (out != 0)
        {
            return out;
        }
        return Integer.compare(this.object.hashCode(), o.object.hashCode());
    }

}

package org.opentrafficsim.base;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;

/**
 * An object with a time stamp, where the object is of a specific class.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 * @param <T> the time stamped object class.
 * @param object the object
 * @param timestamp the simulation time stamp
 */
public record TimeStampedObject<T>(T object, Duration timestamp) implements Comparable<TimeStampedObject<?>>
{

    /**
     * Constructor.
     * @param object the object
     * @param timestamp the simulation time stamp
     */
    public TimeStampedObject
    {
        Throw.whenNull(object, "object");
        Throw.whenNull(timestamp, "timestamp");
    }

    @Override
    public int compareTo(final TimeStampedObject<?> o)
    {
        return this.timestamp.compareTo(o.timestamp);
    }

}

package org.opentrafficsim.base;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Time;

/**
 * An object with a time stamp, where the object is of a specific class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <C> the time stamped object class.
 * @param object the object.
 * @param timestamp the time stamp.
 */
public record TimeStampedObject<C>(C object, Time timestamp) implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160129L;
}

package org.opentrafficsim.base;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Time;

/**
 * An object with a time stamp, where the object is of a specific class.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jan 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <C> the time stamped object class.
 */
public class TimeStampedObject<C> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160129L;

    /** The object. */
    private final C object;

    /** The time stamp. */
    private final Time timestamp;

    /**
     * Construct a new TimeStampedObject.
     * @param object C; the object.
     * @param timestamp Time; the time stamp.
     */
    public TimeStampedObject(final C object, final Time timestamp)
    {
        this.object = object;
        this.timestamp = timestamp;
    }

    /**
     * Retrieve the object.
     * @return C; the object
     */
    public final C getObject()
    {
        return this.object;
    }

    /**
     * Retrieve the time stamp.
     * @return Time; the time stamp
     */
    public final Time getTimestamp()
    {
        return this.timestamp;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TimeStampedObject [object=" + this.object + ", timestamp=" + this.timestamp + "]";
    }

}

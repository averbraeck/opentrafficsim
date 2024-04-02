package org.opentrafficsim.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.jupiter.api.Test;

/**
 * Test the TimeStampedObject class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TimeStampedObjectTest
{

    /**
     * Test the TimeStampedObject class.
     */
    @Test
    public final void testTimeStampedObject()
    {
        Time timeStamp1 = new Time(123, TimeUnit.DEFAULT);
        Time timeStamp2 = new Time(456, TimeUnit.DEFAULT);
        TimeStampedObject<String> tso1 = new TimeStampedObject<String>("tso1", timeStamp1);
        assertEquals(timeStamp1, tso1.getTimestamp(), "time stamp matches");
        assertEquals("tso1", tso1.getObject(), "string matches");
        assertFalse("String".equals(tso1.getObject()), "some other string does not match payload");
        TimeStampedObject<Double> tso2 = new TimeStampedObject<Double>(12.34, timeStamp2);
        assertEquals(tso2.getObject(), new Double(12.34), "payload matches");
        TimeStampedObject<String> tso3 = new TimeStampedObject<String>("tso1", timeStamp2);
        assertFalse(tso1.equals(tso3), "tso's with different time stamp are not equal");
        tso2 = new TimeStampedObject<Double>(12.34, timeStamp1);
        assertFalse(tso1.equals(tso2), "tso's of different generic type but with same time stamp are not equal");
        assertTrue(tso1.toString().contains("TimeStampedObject"),
                "the toString method returns something with the class name in it");
        assertTrue(tso1.toString().contains("tso1"), "toString method returns something with the value of the object in it");
    }
}

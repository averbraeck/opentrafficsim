package org.opentrafficsim.core.gtu.perception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.TimeStampedObject;

/**
 * Test the TimeStampedObject class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class TimeStampedObjectTest
{

    /**
     * Test the entire TimeStampedObject class.
     */
    @Test
    public final void testAll()
    {
        String string1 = "string 1";
        Time time1 = new Time(1234, TimeUnit.DEFAULT);
        TimeStampedObject<String> tso1 = new TimeStampedObject<String>(string1, time1);
        String string2 = "string 2";
        Time time2 = new Time(2345, TimeUnit.DEFAULT);
        TimeStampedObject<String> tso2 = new TimeStampedObject<String>(string2, time2);
        verifyFields(tso1, string1, time1);
        verifyFields(tso2, string2, time2);
        assertTrue(tso1.toString().length() > 10, "the toString method returns something");
    }

    /**
     * Verify all fields in a TimeStampedObject.
     * @param tso TimeStampedObject&lt;String&gt;; the TimeStampedObject
     * @param string String; the object that should be returned by the getObject method of the TimeStampedObject
     * @param time Time; the time that should be returned by the getTimeStamp method of the TimeStampedObject
     */
    private void verifyFields(final TimeStampedObject<String> tso, final String string, final Time time)
    {
        assertEquals(string, tso.getObject(), "object must be " + string);
        assertEquals(time.si, tso.getTimestamp().si, time.si / 99999, "time must be " + time);
    }
}

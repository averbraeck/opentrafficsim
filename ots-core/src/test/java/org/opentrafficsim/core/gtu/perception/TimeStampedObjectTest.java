package org.opentrafficsim.core.gtu.perception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.TimeStampedObject;

/**
 * Test the TimeStampedObject class.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class TimeStampedObjectTest
{

    /** */
    private TimeStampedObjectTest()
    {
        // do not instantiate test class
    }

    /**
     * Test the entire TimeStampedObject class.
     */
    @Test
    public void testAll()
    {
        String string1 = "string 1";
        Duration time1 = new Duration(1234, DurationUnit.SI);
        TimeStampedObject<String> tso1 = new TimeStampedObject<String>(string1, time1);
        String string2 = "string 2";
        Duration time2 = new Duration(2345, DurationUnit.SI);
        TimeStampedObject<String> tso2 = new TimeStampedObject<String>(string2, time2);
        verifyFields(tso1, string1, time1);
        verifyFields(tso2, string2, time2);
        assertTrue(tso1.toString().length() > 10, "the toString method returns something");
    }

    /**
     * Verify all fields in a TimeStampedObject.
     * @param tso the TimeStampedObject
     * @param string the object that should be returned by the getObject method of the TimeStampedObject
     * @param time the time that should be returned by the getTimeStamp method of the TimeStampedObject
     */
    private void verifyFields(final TimeStampedObject<String> tso, final String string, final Duration time)
    {
        assertEquals(string, tso.object(), "object must be " + string);
        assertEquals(time.si, tso.timestamp().si, time.si / 99999, "time must be " + time);
    }
}

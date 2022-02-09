package org.opentrafficsim.core.gtu.perception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.base.TimeStampedObject;

/**
 * Test the TimeStampedObject class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 19, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
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
        assertTrue("the toString method returns something", tso1.toString().length() > 10);
    }

    /**
     * Verify all fields in a TimeStampedObject.
     * @param tso TimeStampedObject&lt;String&gt;; the TimeStampedObject
     * @param string String; the object that should be returned by the getObject method of the TimeStampedObject
     * @param time Time; the time that should be returned by the getTimeStamp method of the TimeStampedObject
     */
    private void verifyFields(final TimeStampedObject<String> tso, final String string, final Time time)
    {
        assertEquals("object must be " + string, string, tso.getObject());
        assertEquals("time must be " + time, time.si, tso.getTimestamp().si, time.si / 99999);
    }
}

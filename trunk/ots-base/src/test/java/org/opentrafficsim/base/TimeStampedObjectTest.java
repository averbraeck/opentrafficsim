package org.opentrafficsim.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;

/**
 * Test the TimeStampedObject class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 17, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
        assertEquals("time stamp matches", timeStamp1, tso1.getTimestamp());
        assertEquals("string matches", "tso1", tso1.getObject());
        assertFalse("some other string does not match payload", "String".equals(tso1.getObject()));
        TimeStampedObject<Double> tso2 = new TimeStampedObject<Double>(12.34, timeStamp2);
        assertEquals("payload matches", tso2.getObject(), new Double(12.34));
        TimeStampedObject<String> tso3 = new TimeStampedObject<String>("tso1", timeStamp2);
        assertFalse("tso's with different time stamp are not equal", tso1.equals(tso3));
        tso2 = new TimeStampedObject<Double>(12.34, timeStamp1);
        assertFalse("tso's of different generic type but with same time stamp are not equal", tso1.equals(tso2));
        assertTrue("the toString method returns something with the class name in it",
                tso1.toString().contains("TimeStampedObject"));
        assertTrue("toString method returns something with the value of the object in it", tso1.toString().contains("tso1"));
    }
}

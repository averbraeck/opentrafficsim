package trafficcontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opentrafficsim.trafficcontrol.TrafficControlException;

/**
 * Test the TrafficControlException class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TrafficControlExceptionTest
{

    /**
     * Test the TrafficControlException class.
     */
    @Test
    public final void trafficControlExceptionTest()
    {
        TrafficControlException e = new TrafficControlException();
        assertNotNull("result should not be null", e);
        String message = "test message";
        e = new TrafficControlException(message);
        assertEquals("message should be " + message, message, e.getMessage());
        String causeMessage = "cause message";
        Throwable cause = new Throwable(causeMessage);
        e = new TrafficControlException(cause);
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        e = new TrafficControlException(message, cause);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        e = new TrafficControlException(message, cause, false, false);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        // TODO check that the values of enableSuppresion and writableStackTrace are correctly reflected in e
        e = new TrafficControlException(message, cause, false, true);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        e = new TrafficControlException(message, cause, true, false);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        e = new TrafficControlException(message, cause, true, true);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
    }

}

package trafficcontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.trafficcontrol.TrafficControlException;

/**
 * Test the TrafficControlException class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
        assertNotNull(e, "result should not be null");
        String message = "test message";
        e = new TrafficControlException(message);
        assertEquals(message, e.getMessage(), "message should be " + message);
        String causeMessage = "cause message";
        Throwable cause = new Throwable(causeMessage);
        e = new TrafficControlException(cause);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        e = new TrafficControlException(message, cause);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        e = new TrafficControlException(message, cause, false, false);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        // TODO check that the values of enableSuppresion and writableStackTrace are correctly reflected in e
        e = new TrafficControlException(message, cause, false, true);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        e = new TrafficControlException(message, cause, true, false);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        e = new TrafficControlException(message, cause, true, true);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
    }

}

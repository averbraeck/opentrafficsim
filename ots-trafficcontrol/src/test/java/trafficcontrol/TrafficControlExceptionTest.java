package trafficcontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.trafficcontrol.TrafficControlException;

/**
 * Test the TrafficControlException class.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class TrafficControlExceptionTest
{

    /** */
    private TrafficControlExceptionTest()
    {
        // do not instantiate test class
    }

    /**
     * Test the TrafficControlException class.
     */
    @Test
    public void trafficControlExceptionTest()
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
    }

}

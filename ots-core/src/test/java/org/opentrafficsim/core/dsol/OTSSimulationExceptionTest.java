package org.opentrafficsim.core.dsol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.djutils.exceptions.Throw;
import org.junit.Test;

/**
 * Test the GTUException class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSSimulationExceptionTest
{

    /**
     * Test the GTUException class.
     */
    @Test
    public final void otsSimulationExceptionTest()
    {
        OTSSimulationException e = new OTSSimulationException();
        assertNotNull("result should not be null", e);
        String message = "test message";
        e = new OTSSimulationException(message);
        assertEquals("message should be " + message, message, e.getMessage());
        String causeMessage = "cause message";
        Throwable cause = new Throwable(causeMessage);
        e = new OTSSimulationException(cause);
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        e = new OTSSimulationException(message, cause);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        e = new OTSSimulationException(message, cause, false, false);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        // TODO check that the values of enableSuppresion and writableStackTrace are correctly reflected in e
        e = new OTSSimulationException(message, cause, false, true);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        e = new OTSSimulationException(message, cause, true, false);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        e = new OTSSimulationException(message, cause, true, true);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
    }

    /**
     * Test the throwIf method.
     * @throws OTSSimulationException should not happen (this test has failed if it does)
     */
    @Test
    public final void throwIfTest() throws OTSSimulationException
    {
        String message = "message";
        try
        {
            Throw.when(true, OTSSimulationException.class, message);
            fail("Previous statement should have thrown a GTUException");
        }
        catch (OTSSimulationException e)
        {
            assertTrue(e.getMessage().endsWith(message));
        }

        Throw.when(false, OTSSimulationException.class, message);
    }
}

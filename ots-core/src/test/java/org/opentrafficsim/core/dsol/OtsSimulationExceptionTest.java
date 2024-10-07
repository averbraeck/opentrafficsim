package org.opentrafficsim.core.dsol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.djutils.exceptions.Throw;
import org.junit.jupiter.api.Test;

/**
 * Test the GTUException class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class OtsSimulationExceptionTest
{

    /**
     * Test the GTUException class.
     */
    @Test
    public final void otsSimulationExceptionTest()
    {
        OtsSimulationException e = new OtsSimulationException();
        assertNotNull(e, "result should not be null");
        String message = "test message";
        e = new OtsSimulationException(message);
        assertEquals(message, e.getMessage(), "message should be " + message);
        String causeMessage = "cause message";
        Throwable cause = new Throwable(causeMessage);
        e = new OtsSimulationException(cause);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        e = new OtsSimulationException(message, cause);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        e = new OtsSimulationException(message, cause, false, false);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        // TODO check that the values of enableSuppresion and writableStackTrace are correctly reflected in e
        e = new OtsSimulationException(message, cause, false, true);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        e = new OtsSimulationException(message, cause, true, false);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        e = new OtsSimulationException(message, cause, true, true);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
    }

    /**
     * Test the throwIf method.
     * @throws OtsSimulationException should not happen (this test has failed if it does)
     */
    @Test
    public final void throwIfTest() throws OtsSimulationException
    {
        String message = "message";
        try
        {
            Throw.when(true, OtsSimulationException.class, message);
            fail("Previous statement should have thrown a GTUException");
        }
        catch (OtsSimulationException e)
        {
            assertTrue(e.getMessage().endsWith(message));
        }

        Throw.when(false, OtsSimulationException.class, message);
    }
}

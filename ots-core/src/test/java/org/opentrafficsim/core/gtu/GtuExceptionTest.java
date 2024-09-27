package org.opentrafficsim.core.gtu;

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
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class GtuExceptionTest
{

    /**
     * Test the GTUException class.
     */
    @Test
    public final void gtuExceptionTest()
    {
        GtuException e = new GtuException();
        assertNotNull(e, "result should not be null");
        String message = "test message";
        e = new GtuException(message);
        assertEquals(message, e.getMessage(), "message should be " + message);
        String causeMessage = "cause message";
        Throwable cause = new Throwable(causeMessage);
        e = new GtuException(cause);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        e = new GtuException(message, cause);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        e = new GtuException(message, cause, false, false);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        // TODO check that the values of enableSuppresion and writableStackTrace are correctly reflected in e
        e = new GtuException(message, cause, false, true);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        e = new GtuException(message, cause, true, false);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
        e = new GtuException(message, cause, true, true);
        assertEquals(message, e.getMessage(), "message should be " + message);
        assertEquals(causeMessage, e.getCause().getMessage(), "cause message should be" + causeMessage);
    }

    /**
     * Test the throwIf method.
     * @throws GtuException should not happen (this test has failed if it does)
     */
    @Test
    public final void throwIfTest() throws GtuException
    {
        String message = "message";
        try
        {
            Throw.when(true, GtuException.class, message);
            fail("Previous statement should have thrown a GTUException");
        }
        catch (GtuException e)
        {
            assertTrue(e.getMessage().endsWith(message));
        }

        Throw.when(false, GtuException.class, message);
    }
}

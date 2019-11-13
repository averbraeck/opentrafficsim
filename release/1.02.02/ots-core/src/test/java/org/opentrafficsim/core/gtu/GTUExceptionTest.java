package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.djutils.exceptions.Throw;
import org.junit.Test;

/**
 * Test the GTUException class.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 11, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUExceptionTest
{

    /**
     * Test the GTUException class.
     */
    @Test
    public final void gtuExceptionTest()
    {
        GTUException e = new GTUException();
        assertNotNull("result should not be null", e);
        String message = "test message";
        e = new GTUException(message);
        assertEquals("message should be " + message, message, e.getMessage());
        String causeMessage = "cause message";
        Throwable cause = new Throwable(causeMessage);
        e = new GTUException(cause);
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        e = new GTUException(message, cause);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        e = new GTUException(message, cause, false, false);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        // TODO check that the values of enableSuppresion and writableStackTrace are correctly reflected in e
        e = new GTUException(message, cause, false, true);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        e = new GTUException(message, cause, true, false);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
        e = new GTUException(message, cause, true, true);
        assertEquals("message should be " + message, message, e.getMessage());
        assertEquals("cause message should be" + causeMessage, causeMessage, e.getCause().getMessage());
    }

    /**
     * Test the throwIf method.
     * @throws GTUException should not happen (this test has failed if it does)
     */
    @Test
    public final void throwIfTest() throws GTUException
    {
        String message = "message";
        try
        {
            Throw.when(true, GTUException.class, message);
            fail("Previous statement should have thrown a GTUException");
        }
        catch (GTUException e)
        {
            assertTrue(e.getMessage().endsWith(message));
        }

        Throw.when(false, GTUException.class, message);
    }
}

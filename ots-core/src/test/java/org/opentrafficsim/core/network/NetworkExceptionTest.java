package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/** Test the NetworkException class. */
public class NetworkExceptionTest
{

    /** Test the NetworkException class. */
    @Test
    public void testException()
    {
        try
        {
            throw new NetworkException();
        }
        catch (NetworkException e)
        {
            assertNull(e.getMessage());
        }
        catch (Exception exception)
        {
            fail("Right exception not thrown");
        }

        try
        {
            throw new NetworkException("abc");
        }
        catch (NetworkException e)
        {
            assertEquals("abc", e.getMessage());
        }
        catch (Exception exception)
        {
            fail("Right exception not thrown");
        }

        try
        {
            throw new NetworkException(new IllegalArgumentException());
        }
        catch (NetworkException e)
        {
            assertTrue(e.getMessage().contains("IllegalArgumentException"));
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
        catch (Exception exception)
        {
            fail("Right exception not thrown");
        }

        try
        {
            throw new NetworkException("abc", new IllegalArgumentException("def"));
        }
        catch (NetworkException e)
        {
            assertEquals("abc", e.getMessage());
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertEquals("def", e.getCause().getMessage());
        }
        catch (Exception exception)
        {
            fail("Right exception not thrown");
        }

        for (boolean suppression : new boolean[] {false, true})
        {
            for (boolean writableStackTrace : new boolean[] {false, true})
            {
                try
                {
                    throw new NetworkException("abc", new IllegalArgumentException("def"), suppression, writableStackTrace);
                }
                catch (NetworkException e)
                {
                    assertEquals("abc", e.getMessage());
                    assertTrue(e.getCause() instanceof IllegalArgumentException);
                    assertEquals("def", e.getCause().getMessage());
                    // Rest of this test code stolen from old version of djunits.
                    StackTraceElement[] stackTrace = new StackTraceElement[1];
                    stackTrace[0] = new StackTraceElement("a", "b", "c", 1234);
                    try
                    {
                        e.setStackTrace(stackTrace);
                    }
                    catch (Exception e1)
                    {
                        assertTrue("Stack trace should be writable", writableStackTrace);
                        continue;
                    }
                    // You wouldn't believe it, but a call to setStackTrace if non-writable is silently ignored
                    StackTraceElement[] retrievedStackTrace = e.getStackTrace();
                    if (retrievedStackTrace.length > 0)
                    {
                        assertTrue("stack trace should be writable", writableStackTrace);
                    }
                }
                catch (Exception exception)
                {
                    fail("Right exception not thrown");
                }
            }
        }
    }

}

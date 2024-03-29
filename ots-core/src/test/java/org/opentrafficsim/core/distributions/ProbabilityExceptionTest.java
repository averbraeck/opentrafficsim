package org.opentrafficsim.core.distributions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/** Test the ProbabilityException class. */
public class ProbabilityExceptionTest
{

    /** Test the ProbabilityException class. */
    @Test
    public void testException()
    {
        try
        {
            throw new ProbabilityException();
        }
        catch (ProbabilityException e)
        {
            assertNull(e.getMessage());
        }
        catch (Exception exception)
        {
            fail("Right exception not thrown");
        }

        try
        {
            throw new ProbabilityException("abc");
        }
        catch (ProbabilityException e)
        {
            assertEquals("abc", e.getMessage());
        }
        catch (Exception exception)
        {
            fail("Right exception not thrown");
        }

        try
        {
            throw new ProbabilityException(new IllegalArgumentException());
        }
        catch (ProbabilityException e)
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
            throw new ProbabilityException("abc", new IllegalArgumentException("def"));
        }
        catch (ProbabilityException e)
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
                    throw new ProbabilityException("abc", new IllegalArgumentException("def"), suppression, writableStackTrace);
                }
                catch (ProbabilityException e)
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
                        assertTrue(writableStackTrace, "Stack trace should be writable");
                        continue;
                    }
                    // You wouldn't believe it, but a call to setStackTrace if non-writable is silently ignored
                    StackTraceElement[] retrievedStackTrace = e.getStackTrace();
                    if (retrievedStackTrace.length > 0)
                    {
                        assertTrue(writableStackTrace, "stack trace should be writable");
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

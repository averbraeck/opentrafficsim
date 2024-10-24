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

    }

}

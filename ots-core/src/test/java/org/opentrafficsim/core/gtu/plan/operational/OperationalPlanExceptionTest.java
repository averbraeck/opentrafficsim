package org.opentrafficsim.core.gtu.plan.operational;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/** Test the OperationalPlanException class. */
public final class OperationalPlanExceptionTest
{

    /** */
    private OperationalPlanExceptionTest()
    {
        // do not instantiate test class
    }

    /** Test the OperationalPlanException class. */
    @Test
    public void testException()
    {
        try
        {
            throw new OperationalPlanException();
        }
        catch (OperationalPlanException e)
        {
            assertNull(e.getMessage());
        }
        catch (Exception exception)
        {
            fail("Right exception not thrown");
        }

        try
        {
            throw new OperationalPlanException("abc");
        }
        catch (OperationalPlanException e)
        {
            assertEquals("abc", e.getMessage());
        }
        catch (Exception exception)
        {
            fail("Right exception not thrown");
        }

        try
        {
            throw new OperationalPlanException(new IllegalArgumentException());
        }
        catch (OperationalPlanException e)
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
            throw new OperationalPlanException("abc", new IllegalArgumentException("def"));
        }
        catch (OperationalPlanException e)
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

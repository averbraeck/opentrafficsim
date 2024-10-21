package org.opentrafficsim.base.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/** Test the OtsGeometryException class. */
public class OtsGeometryExceptionTest
{

    /** Test the OtsGeometryException class. */
    @Test
    public void testException()
    {
        try
        {
            throw new OtsGeometryException();
        }
        catch (OtsGeometryException e)
        {
            assertNull(e.getMessage());
        }
        catch (Exception exception)
        {
            fail("Right exception not thrown");
        }

        try
        {
            throw new OtsGeometryException("abc");
        }
        catch (OtsGeometryException e)
        {
            assertEquals("abc", e.getMessage());
        }
        catch (Exception exception)
        {
            fail("Right exception not thrown");
        }

        try
        {
            throw new OtsGeometryException(new IllegalArgumentException());
        }
        catch (OtsGeometryException e)
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
            throw new OtsGeometryException("abc", new IllegalArgumentException("def"));
        }
        catch (OtsGeometryException e)
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

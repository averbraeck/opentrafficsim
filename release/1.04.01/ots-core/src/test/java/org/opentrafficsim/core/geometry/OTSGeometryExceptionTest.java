package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/** Test the OTSGeometryException class. */
public class OTSGeometryExceptionTest
{

    /** Test the OTSGeometryException class. */
    @Test
    public void testException()
    {
        try
        {
            throw new OTSGeometryException();
        }
        catch (OTSGeometryException e)
        {
            assertNull(e.getMessage());
        }
        catch (Exception exception)
        {
            fail("Right exception not thrown");
        }

        try
        {
            throw new OTSGeometryException("abc");
        }
        catch (OTSGeometryException e)
        {
            assertEquals("abc", e.getMessage());
        }
        catch (Exception exception)
        {
            fail("Right exception not thrown");
        }

        try
        {
            throw new OTSGeometryException(new IllegalArgumentException());
        }
        catch (OTSGeometryException e)
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
            throw new OTSGeometryException("abc", new IllegalArgumentException("def"));
        }
        catch (OTSGeometryException e)
        {
            assertEquals("abc", e.getMessage());
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertEquals("def", e.getCause().getMessage());
        }
        catch (Exception exception)
        {
            fail("Right exception not thrown");
        }

        for (boolean suppression : new boolean[] { false, true })
        {
            for (boolean writableStackTrace : new boolean[] { false, true })
            {
                try
                {
                    throw new OTSGeometryException("abc", new IllegalArgumentException("def"), suppression, writableStackTrace);
                }
                catch (OTSGeometryException e)
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

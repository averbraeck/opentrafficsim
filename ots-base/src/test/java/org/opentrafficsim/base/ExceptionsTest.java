package org.opentrafficsim.base;

import org.djutils.test.ExceptionTest;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.geometry.OtsGeometryException;
import org.opentrafficsim.base.parameters.ParameterException;

/**
 * Tests exceptions in base.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ExceptionsTest
{

    /**
     * Constructor.
     */
    private ExceptionsTest()
    {
        //
    }

    /**
     * Test exception in base.
     */
    @Test
    public void testExceptions()
    {
        ExceptionTest.testExceptionClass(OtsException.class);
        ExceptionTest.testExceptionClass(OtsRuntimeException.class);
        ExceptionTest.testExceptionClass(OtsGeometryException.class);
        ExceptionTest.testExceptionClass(ParameterException.class);
    }

}

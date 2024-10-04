package org.opentrafficsim.core.distributions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test the ConstantGenerator class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class ConstantGeneratorTest
{

    /** Test the ConstantGenerator class. */
    @Test
    public void constantGeneratorTest()
    {
        for (double testValue : new double[] {-999, -1, 0, Math.PI, 12345.678})
        {
            ConstantGenerator<Double> cg = new ConstantGenerator<>(testValue);
            for (int attempt = 0; attempt < 10; attempt++)
            {
                assertEquals(testValue, cg.draw(), 0, "Constant generator returns constant");
            }
            assertEquals(testValue, cg.getValue(), 0, "getValue returns the value");
            assertTrue(cg.toString().startsWith("ConstantGenerator"),
                    "The toString method of the ConstantGenerator returns something descriptive");
        }
    }

}

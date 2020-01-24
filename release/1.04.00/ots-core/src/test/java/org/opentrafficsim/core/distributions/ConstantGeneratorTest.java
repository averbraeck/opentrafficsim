package org.opentrafficsim.core.distributions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test the ConstantGenerator class.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ConstantGeneratorTest
{

    /** Test the ConstantGenerator class. */
    @Test
    public void constantGeneratorTest()
    {
        for (double testValue : new double[] { -999, -1, 0, Math.PI, 12345.678 })
        {
            ConstantGenerator<Double> cg = new ConstantGenerator<>(testValue);
            for (int attempt = 0; attempt < 10; attempt++)
            {
                assertEquals("Constant generator returns constant", testValue, cg.draw(), 0);
            }
            assertEquals("getValue returns the value", testValue, cg.getValue(), 0);
            assertTrue("The toString method of the ConstantGenerator returns something descriptive",
                    cg.toString().startsWith("ConstantGenerator"));
        }
    }

}

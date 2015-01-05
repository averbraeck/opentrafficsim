package org.opentrafficsim.core.value;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FormatTest
{
    /**
     * Test that size, precision and accuracy are OK.
     */
    @SuppressWarnings("static-method")
    @Test
    public final void format()
    {
        double[] baseValues = {1, (float) (1 / 3d)};
        for (int width = 8; width <= 20; width++)
        {
            for (int precision = 0; precision <= 10; precision++)
            {
                for (int power = -20; power <= 20; power++)
                {
                    if (width - precision <= 6)
                    {
                        continue; // can't be done
                    }
                    for (double baseValue : baseValues)
                    {
                        float value = (float) (baseValue * Math.pow(10, power));
                        // System.out.print("Trying " + width + ", " + precision + ",  " + value);
                        String result = Format.format(value, width, precision);
                        // System.out.println(": \"" + result + "\"");
                        assertEquals("Length of result should equal specified width", width, result.length());
                        double reverseValue = Double.parseDouble(result);
                        int expectedPrecision = precision - 2;
                        if (expectedPrecision > 6)
                        {
                            expectedPrecision = 6;
                        }
                        double tolerance = Math.abs(value / Math.pow(10, expectedPrecision));
                        assertEquals("Parsed result should equal original value within tolerance " + tolerance, value,
                            reverseValue, tolerance);
                    }
                    for (double baseValue : baseValues)
                    {
                        double value = baseValue * Math.pow(10, power);
                        // System.out.print("Trying " + width + ", " + precision + ",  " + value);
                        String result = Format.format(value, width, precision);
                        // System.out.println(": \"" + result + "\"");
                        assertEquals("Length of result should equal specified width", width, result.length());
                        double reverseValue = Double.parseDouble(result);
                        int expectedPrecision = precision - 2;
                        if (expectedPrecision > 15)
                        {
                            expectedPrecision = 15;
                        }
                        double tolerance = Math.abs(value / Math.pow(10, expectedPrecision));
                        assertEquals("Parsed result should equal original value within tolerance " + tolerance, value,
                            reverseValue, tolerance);
                    }
                }
            }
        }
    }
}

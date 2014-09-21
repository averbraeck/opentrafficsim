package org.opentrafficsim.core.value;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties, including,
 * but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no
 * event shall the copyright holder or contributors be liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or
 * profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or
 * tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
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
        double[] baseValues = { 1, (float) (1 / 3d) };
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

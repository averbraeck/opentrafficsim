package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The DSOL project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Jun 18, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SICoefficientsTest
{
    /**
     * Currently the parser throws an Error if something is not right. This behavior may be changed in the future. To
     * prevent the need to rewrite all parser tests, this wrapper will catch those error and assume null when they
     * occur.
     * @param inputString String; the coefficientString to parse
     */
    private static void parseString(String inputString, final String expectedResult)
    {
        SICoefficients result = null;
        try
        {
            result = SICoefficients.create(inputString);
        }
        catch (Error e)
        {
            assertTrue("input \"" + inputString + "\" should have been parseable", null == expectedResult);
            return;
        }
        assertTrue("input \"" + inputString + "\" should not have been parseable", null != expectedResult);
        assertEquals("input \"" + inputString + "\" did not yield the expected result", expectedResult, result.toString());
    }

    /**
     * Test the create method.
     */
    @SuppressWarnings("static-method")
    @Test
    public void parser()
    {
        parseString("", "1");
        parseString("1", "1");
        parseString("kgm/s2", "kg.m/s2");
        parseString("kg-2m^3/s2/A", "m3/kg2/s2/A");
        parseString("s-2", "1/s2");
        parseString("cd/mol^2", "cd/mol2");
        parseString("Kmmol3/AsKcd4", "m.s.K2.cd4.mol3/A");
        parseString("m", "m");
        parseString("mol", "mol");
        parseString("OIhFUSAIFAHsufasfs", null);
        parseString("/s", "1/s");
        parseString("/s/s", "1/s2");
        parseString("s-2345", "1/s2345");
        parseString("s3/s3", "1");
        parseString("s3/s4", "1/s");
        parseString("s-3/s-4", "s");
        parseString("sssss", "s5");
        parseString("/s/s/s/s/s", "1/s5");
        parseString("s^3", "s3");
        parseString("s^-3", "1/s3");
        parseString("/s^4", "1/s4");
        parseString("/s^-5", "s5");
    }
    
    /**
     * Execute one multiplication test
     * @param leftString String; coefficientString of first operand
     * @param rightString String; coefficientString of second operand
     * @param expectedResult String; coefficientString of the expected result
     */
    private static void multiplyTest(String leftString, String rightString, String expectedResult) {
        SICoefficients left = SICoefficients.create(leftString);
        SICoefficients right = SICoefficients.create(rightString);
        SICoefficients product = SICoefficients.multiply(left, right);
        assertEquals("input \"" + leftString + "\" times " + rightString + " did not yield the expected result", expectedResult, product.toString());        
    }
    
    /**
     * Test the multiply method
     */
    @SuppressWarnings("static-method")
    @Test
    public void multiply()
    {
        multiplyTest("1", "1", "1");
        multiplyTest("A", "K", "A.K");
        multiplyTest("A-1", "A", "1");
        multiplyTest("A-2", "/A^2", "1/A4");
        multiplyTest("Kmmol3/AsKcd4", "1", "m.s.K2.cd4.mol3/A");
        multiplyTest("1", "Kmmol3/AsKcd4", "m.s.K2.cd4.mol3/A");
        multiplyTest("Kmmol3", "/AsKcd4", "m.s.K2.cd4.mol3/A");
    }

    /**
     * Execute one division test
     * @param leftString String; coefficientString of dividend
     * @param rightString String; coefficientString of divisor
     * @param expectedResult String; coefficientString of the expected result 
     */
    private static void divideTest(String leftString, String rightString, String expectedResult) {
        SICoefficients left = SICoefficients.create(leftString);
        SICoefficients right = SICoefficients.create(rightString);
        SICoefficients quotient = SICoefficients.divide(left, right);
        assertEquals("input \"" + leftString + "\" times " + rightString + " did not yield the expected result", expectedResult, quotient.toString());        
    }
    
    /**
     * Test the divide method
     */
    @SuppressWarnings("static-method")
    @Test
    public void divide()
    {
        divideTest("1", "1", "1");
        divideTest("A", "1", "A");
        divideTest("1", "A", "1/A");
        divideTest("A-1K2", "As2", "K2/s2/A2");
        divideTest("s10", "/s10", "s20");
        divideTest("s-10", "/s10", "1");
    }

}

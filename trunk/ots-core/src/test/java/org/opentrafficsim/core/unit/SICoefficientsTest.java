package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionJun 18, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SICoefficientsTest
{
    /**
     * Currently the parser throws an Error if something is not right. This behavior may be changed in the future. To
     * prevent the need to rewrite all parser tests, this wrapper will catch those error and assume null when they
     * occur.
     * @param inputString String; the coefficientString to parse
     * @param expectedResult String; the expected output
     */
    private static void parseString(final String inputString, final String expectedResult)
    {
        SICoefficients result = null;
        try
        {
            result = new SICoefficients(SICoefficients.parse(inputString));
        }
        catch (UnitException e)
        {
            assertTrue("input \"" + inputString + "\" should have been parseable", null == expectedResult);
            return;
        }
        assertTrue("input \"" + inputString + "\" should not have been parseable", null != expectedResult);
        assertEquals("input \"" + inputString + "\" did not yield the expected result", expectedResult,
                result.toString());
    }

    /**
     * Test the create method.
     */
    @SuppressWarnings("static-method")
    @Test
    public final void parser()
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
        parseString("/", null);
        parseString("/s/", null);
    }

    /**
     * Execute one multiplication test.
     * @param leftString String; coefficientString of first operand
     * @param rightString String; coefficientString of second operand
     * @param expectedResult String; coefficientString of the expected result
     */
    private static void multiplyTest(final String leftString, final String rightString, final String expectedResult)
    {
        try
        {
            SICoefficients left = new SICoefficients(SICoefficients.parse(leftString));
            SICoefficients right = new SICoefficients(SICoefficients.parse(rightString));
            SICoefficients product = SICoefficients.multiply(left, right);
            assertEquals("input \"" + leftString + "\" times " + rightString + " did not yield the expected result",
                    expectedResult, product.toString());
        }
        catch (UnitException exception)
        {
            fail("Caught unexpected UnitException");
        }
    }

    /**
     * Test the multiply method.
     */
    @SuppressWarnings("static-method")
    @Test
    public final void multiply()
    {
        multiplyTest("1", "1", "1");
        multiplyTest("A", "K", "A.K");
        multiplyTest("A-1", "A", "1");
        multiplyTest("A-2", "/A^2", "1/A4");
        multiplyTest("Kmmol3/AsKcd4", "1", "m.s.K2.cd4.mol3/A");
        multiplyTest("1", "Kmmol3/AsKcd4", "m.s.K2.cd4.mol3/A");
        multiplyTest("Kmmol3", "/AsKcd4", "m.s.K2.cd4.mol3/A");
        multiplyTest("kgm/s2", "m", "kg.m2/s2");
    }

    /**
     * Execute one division test.
     * @param leftString String; coefficientString of dividend
     * @param rightString String; coefficientString of divisor
     * @param expectedResult String; coefficientString of the expected result
     */
    private static void divideTest(final String leftString, final String rightString, final String expectedResult)
    {
        try
        {
            SICoefficients left = new SICoefficients(SICoefficients.parse(leftString));
            SICoefficients right = new SICoefficients(SICoefficients.parse(rightString));
            SICoefficients quotient = SICoefficients.divide(left, right);
            assertEquals("input \"" + leftString + "\" times " + rightString + " did not yield the expected result",
                    expectedResult, quotient.toString());
        }
        catch (UnitException exception)
        {
            fail("Caught unexpected UnitException");
        }
    }

    /**
     * Test the divide method.
     */
    @SuppressWarnings("static-method")
    @Test
    public final void divide()
    {
        divideTest("1", "1", "1");
        divideTest("A", "1", "A");
        divideTest("1", "A", "1/A");
        divideTest("A-1K2", "As2", "K2/s2/A2");
        divideTest("s10", "/s10", "s20");
        divideTest("s-10", "/s10", "1");
    }

}

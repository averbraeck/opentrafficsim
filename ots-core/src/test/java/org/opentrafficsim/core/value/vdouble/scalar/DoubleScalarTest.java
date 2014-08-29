package org.opentrafficsim.core.value.vdouble.scalar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentrafficsim.core.unit.AreaUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TemperatureUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.Relative;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
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
 * @version Jun 26, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DoubleScalarTest
{
    /**
     * Test creator, verify the various fields in the created objects, test conversions to other units
     */
    @SuppressWarnings("static-method")
    @Test
    public void basics()
    {
        TemperatureUnit tempUnit = TemperatureUnit.DEGREE_CELSIUS;
        double value = 38.0;
        DoubleScalarAbs<TemperatureUnit> temperatureFS = new DoubleScalarAbs<TemperatureUnit>(value, tempUnit);
        assertEquals("Unit should be Celsius", tempUnit, temperatureFS.getUnit());
        assertEquals("Value is what we put in", value, temperatureFS.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Kelvin", 311.15, temperatureFS.getValueSI(), 0.05);
        assertEquals("Value in Fahrenheit", 100.4, temperatureFS.getValueInUnit(TemperatureUnit.DEGREE_FAHRENHEIT), 0.1);
        DoubleScalarAbs<TemperatureUnit> u2 = new DoubleScalarAbs<TemperatureUnit>(temperatureFS);
        //temperatureFS.setDisplayUnit(TemperatureUnit.DEGREE_FAHRENHEIT);
        //assertEquals("Unit should now be Fahrenheit", TemperatureUnit.DEGREE_FAHRENHEIT, temperatureFS.getUnit());
        //assertEquals("Value in unit is now the equivalent in Fahrenheit", 100.4, temperatureFS.getValueInUnit(), 0.05);
        assertEquals("Value in SI is equivalent in Kelvin", 311.15, temperatureFS.getValueSI(), 0.1);
        assertEquals("Value in Fahrenheit", 100.4, temperatureFS.getValueInUnit(TemperatureUnit.DEGREE_FAHRENHEIT), 0.1);
        assertTrue("Value is absolute", temperatureFS.isAbsolute());
        assertFalse("Value is absolute", temperatureFS.isRelative());
        assertEquals("Unit of copy made before calling setUnit should be unchanged", tempUnit, u2.getUnit());

        LengthUnit lengthUnit = LengthUnit.INCH;
        value = 12;
        DoubleScalarRel<LengthUnit> lengthFS = new DoubleScalarRel<LengthUnit>(value, lengthUnit);
        // System.out.println("lengthFS is " + lengthFS);
        assertEquals("Unit should be Inch", lengthUnit, lengthFS.getUnit());
        assertEquals("Value is what we put in", value, lengthFS.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Meter", 0.3048, lengthFS.getValueSI(), 0.0005);
        assertEquals("Value in Foot", 1, lengthFS.getValueInUnit(LengthUnit.FOOT), 0.0001);
        DoubleScalarRel<LengthUnit> copy = new DoubleScalarRel<LengthUnit>(lengthFS);
        //lengthFS.setDisplayUnit(LengthUnit.MILLIMETER);
        //assertEquals("Unit should not be Millimeter", LengthUnit.MILLIMETER, lengthFS.getUnit());
        assertEquals("Unit of copy should also be in Inch", LengthUnit.INCH, copy.getUnit());
    }
    
    /**
     * Check that copy really performs a deep copy.
     */
    @SuppressWarnings("static-method")
    @Test
    public void testCopyAbs()
    {
        DoubleScalarAbs<TemperatureUnit> value = new DoubleScalarAbs<TemperatureUnit>(10, TemperatureUnit.DEGREE_CELSIUS);
        DoubleScalarAbs<?> copy = (DoubleScalarAbs<?>) value.copy();
        assertEquals("Copy should have same value", value.getValueSI(), copy.getValueSI(), 0.0001);
        assertTrue("Copy should be equal to value", value.equals(copy));
        assertTrue("Value should be equal to copy", copy.equals(value));
        value.set(new DoubleScalarAbs<TemperatureUnit>(20, TemperatureUnit.DEGREE_CELSIUS));
        assertFalse("Copy should have same value", value.getValueSI() == copy.getValueSI());
        assertFalse("Copy should be equal to value", value.equals(copy));
        assertFalse("Value should be equal to copy", copy.equals(value));        
    }

    /**
     * Check that copy really performs a deep copy.
     */
    @SuppressWarnings("static-method")
    @Test
    public void testCopyRel()
    {
        DoubleScalarRel<TemperatureUnit> value = new DoubleScalarRel<TemperatureUnit>(10, TemperatureUnit.DEGREE_CELSIUS);
        DoubleScalarRel<?> copy = (DoubleScalarRel<?>) value.copy();
        assertEquals("Copy should have same value", value.getValueSI(), copy.getValueSI(), 0.0001);
        assertTrue("Copy should be equal to value", value.equals(copy));
        assertTrue("Value should be equal to copy", copy.equals(value));
        value.set(new DoubleScalarRel<TemperatureUnit>(20, TemperatureUnit.DEGREE_CELSIUS));
        assertFalse("Copy should have same value", value.getValueSI() == copy.getValueSI());
        assertFalse("Copy should be equal to value", value.equals(copy));
        assertFalse("Value should be equal to copy", copy.equals(value));        
    }

    /**
     * Test plus, minus, etc.
     */
    @SuppressWarnings("static-method")
    @Test
    public void simpleArithmetic()
    {
        double leftValue = 123.456;
        double rightValue = 21.098;
        double rightValue2 = 1.098;
        DoubleScalarAbs<LengthUnit> leftAbs = new DoubleScalarAbs<LengthUnit>(leftValue, LengthUnit.METER);
        DoubleScalarRel<LengthUnit> right = new DoubleScalarRel<LengthUnit>(rightValue, LengthUnit.INCH);
        DoubleScalarRel<LengthUnit> right2 = new DoubleScalarRel<LengthUnit>(rightValue2, LengthUnit.MILLIMETER);
        DoubleScalarAbs<LengthUnit> sum = DoubleScalar.plus(leftAbs, right);
        assertEquals("result should be in METER", LengthUnit.METER, sum.getUnit());
        assertEquals("value of result should be sum of meter equivalent of values", leftValue + rightValue * 0.0254,
                sum.getValueSI(), 0.0001);
        DoubleScalarRel<LengthUnit> leftRel = new DoubleScalarRel<LengthUnit>(leftValue, LengthUnit.METER);
        DoubleScalarRel<LengthUnit> sum2 = DoubleScalar.plus(LengthUnit.MILLIMETER, leftRel, right, right2);
        assertEquals("result should be in MILLIMETER", LengthUnit.MILLIMETER, sum2.getUnit());
        assertEquals("value in SI should be sum of meter equivalent of values", leftValue + rightValue * 0.0254
                + rightValue2 / 1000, sum2.getValueSI(), 0.0001);
        assertEquals("value in \"own\" unit should be equivalent in MILLIMETER", 1000
                * (leftValue + rightValue * 0.0254) + rightValue2, sum2.getValueInUnit(), 0.1);
        DoubleScalarAbs<LengthUnit> difference = DoubleScalar.minus(leftAbs, right, right2);
        assertEquals("result should be in METER", LengthUnit.METER, difference.getUnit());
        assertEquals("value in SI should be sum of meter equivalent of values", leftValue - rightValue * 0.0254
                - rightValue2 / 1000, difference.getValueSI(), 0.0001);
        assertEquals("value in \"own\" unit should be equivalent in METER", leftValue - rightValue * 0.0254
                - rightValue2 / 1000, difference.getValueInUnit(), 0.1);
        DoubleScalarRel<LengthUnit> differenceRel = DoubleScalar.minus(leftRel, right, right2);
        assertEquals("result should be in METER", LengthUnit.METER, differenceRel.getUnit());
        assertEquals("value in SI should be sum of meter equivalent of values", leftValue - rightValue * 0.0254
                - rightValue2 / 1000, differenceRel.getValueSI(), 0.0001);
        assertEquals("value in \"own\" unit should be equivalent in METER", leftValue - rightValue * 0.0254
                - rightValue2 / 1000, differenceRel.getValueInUnit(), 0.001);
        differenceRel = DoubleScalar.minus(sum, leftAbs);
        assertEquals("result should be in METER", LengthUnit.METER, difference.getUnit());
        assertEquals("value of result should be minus leftValue", rightValue * 0.0254, differenceRel.getValueSI(),
                0.0001);
        DoubleScalarAbs<?> surface = DoubleScalar.multiply(leftAbs, difference);
        // System.out.println("surface is " + surface);
         assertEquals("Surface should be in square meter", AreaUnit.SQUARE_METER.getSICoefficientsString(), surface
                .getUnit().getSICoefficientsString());
        assertEquals("Surface should be equal to the product of contributing values",
                leftAbs.getValueSI() * difference.getValueSI(), surface.getValueSI(), 0.05);
        DoubleScalarRel<?> relSurface = DoubleScalar.multiply(right, right2);
        assertEquals("Surface should be in square meter", AreaUnit.SQUARE_METER.getSICoefficientsString(), relSurface
                .getUnit().getSICoefficientsString());
        assertEquals("Surface should be equal to the product of contributing values",
                right.getValueSI() * right2.getValueSI(), relSurface.getValueSI(), 0.00000005);
        assertTrue("DoubleScalar should be equal to itself", leftAbs.equals(leftAbs));
        DoubleScalar<?> copy = new DoubleScalarAbs<LengthUnit>(leftAbs);
        assertTrue("Copy of DoubleScalar should be equal to itself", leftAbs.equals(copy));
        copy = new DoubleScalarRel<LengthUnit>(leftAbs.getValueInUnit(), leftAbs.getUnit());
        assertTrue("this copy should be relative", copy instanceof Relative);
        assertFalse("Relative can not be equal to (otherwise equal) absolute", leftAbs.equals(copy));
        assertFalse("DoubleScalar is not a String", copy.equals("String"));
        DoubleScalarAbs<TimeUnit> timeScalar = new DoubleScalarAbs<TimeUnit>(leftValue, TimeUnit.SECOND);
        assertEquals("Value should match", leftAbs.getValueSI(), timeScalar.getValueSI(), 0.0001);
        assertTrue("Both are absolute", leftAbs.isAbsolute() && timeScalar.isAbsolute());
        assertFalse("Absolute length is not equal to absolute time with same value", leftAbs.equals(timeScalar));
        leftAbs = new DoubleScalarAbs<LengthUnit>(leftValue, LengthUnit.METER);
        leftAbs.add(right);
        assertEquals("after add-and-becomes the type should not be changed", LengthUnit.METER, leftAbs.getUnit());
        assertEquals("after add-and-becomes the value should be changed", leftValue + rightValue * 0.0254,
                leftAbs.getValueSI(), 0.0001);
        leftAbs = new DoubleScalarAbs<LengthUnit>(leftValue, LengthUnit.METER);
        leftAbs.subtract(right);
        assertEquals("after subtract-and-becomes the type should not be changed", LengthUnit.METER, leftAbs.getUnit());
        assertEquals("after subtract-and-becomes the value should be changed", leftValue - rightValue * 0.0254,
                leftAbs.getValueSI(), 0.0001);
    }

    /**
     * Test the math operations
     */
    @SuppressWarnings("static-method")
    @Test
    public void mathMethods()
    {
        double[] inputValues = {-10, -2, -1, -0.5, -0.1, 0, 0.1, 0.5, 1, 2, 10};
        for (double inputValue : inputValues)
        {
            DoubleScalarRel<LengthUnit> fs;
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.abs();
            MathTester.tester(inputValue, "abs", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.abs(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.acos();
            MathTester.tester(inputValue, "acos", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.acos(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.asin();
            MathTester.tester(inputValue, "asin", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.asin(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.atan();
            MathTester.tester(inputValue, "atan", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.atan(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.cbrt();
            MathTester.tester(inputValue, "cbrt", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.cbrt(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.ceil();
            MathTester.tester(inputValue, "ceil", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.ceil(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.cos();
            MathTester.tester(inputValue, "cos", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.cos(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.cosh();
            MathTester.tester(inputValue, "cosh", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.cosh(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.exp();
            MathTester.tester(inputValue, "exp", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.exp(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.expm1();
            MathTester.tester(inputValue, "expm1", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.expm1(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.floor();
            MathTester.tester(inputValue, "floor", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.floor(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.log();
            MathTester.tester(inputValue, "log", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.log(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.log10();
            MathTester.tester(inputValue, "log10", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.log10(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.log1p();
            MathTester.tester(inputValue, "log10", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.log1p(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.rint();
            MathTester.tester(inputValue, "rint", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.rint(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.round();
            MathTester.tester(inputValue, "round", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.round(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.signum();
            MathTester.tester(inputValue, "signum", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.signum(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.sin();
            MathTester.tester(inputValue, "sin", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.sin(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.sinh();
            MathTester.tester(inputValue, "sinh", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.sinh(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.sqrt();
            MathTester.tester(inputValue, "sqrt", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.sqrt(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.tan();
            MathTester.tester(inputValue, "tan", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.tan(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.tanh();
            MathTester.tester(inputValue, "tanh", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.tanh(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.toDegrees();
            MathTester.tester(inputValue, "toDegrees", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.toDegrees(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.toRadians();
            MathTester.tester(inputValue, "toRadians", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return Math.toRadians(f);
                }
            });
            fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.inv();
            MathTester.tester(inputValue, "inv", fs, 0.001, new DoubleToDouble()
            {
                public double function(double f)
                {
                    return 1 / f;
                }
            });
            for (int i = -10; i <= 10; i++)
            {
                final double exponent = i * 0.5;
                fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
                fs.pow(exponent);
                MathTester.tester(inputValue, "pow(" + exponent + ")", fs, 0.001, new DoubleToDouble()
                {
                    public double function(double f)
                    {
                        return Math.pow(f, exponent);
                    }
                });
            }
            double[] constants = {-1000, -100, -10, 0, 10, 100, 1000};
            for (final double constant : constants)
            {
                fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
                fs.multiply(constant);
                MathTester.tester(inputValue, "multiply by " + constant, fs, 0.001, new DoubleToDouble()
                {
                    public double function(double f)
                    {
                        return f * constant;
                    }
                });
                fs = new DoubleScalarRel<LengthUnit>(inputValue, LengthUnit.METER);
                fs.divide(constant);
                MathTester.tester(inputValue, "divide by " + constant, fs, 0.001, new DoubleToDouble()
                {
                    public double function(double f)
                    {
                        return f / constant;
                    }
                });
            }
        }
    }
    
    /**
     * Interface encapsulating a function that takes a double and returns a double.
     * <p>
     * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
     * reserved.
     * <p>
     * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
     * <p>
     * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
     * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
     * following conditions are met:
     * <ul>
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the
     * following disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
     * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse
     * or promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied
     * warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular
     * purpose are disclaimed. In no event shall the copyright holder or contributors be liable for any direct,
     * indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of
     * substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any
     * theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising
     * in any way out of the use of this software, even if advised of the possibility of such damage.
     * @version Jun 23, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    interface DoubleToDouble
    {
        /**
         * @param f double value
         * @return double value
         */
        double function(double f);
    }

    /**
     * <p>
     * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
     * reserved.
     * <p>
     * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
     * <p>
     * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
     * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
     * following conditions are met:
     * <ul>
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the
     * following disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
     * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse
     * or promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied
     * warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular
     * purpose are disclaimed. In no event shall the copyright holder or contributors be liable for any direct,
     * indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of
     * substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any
     * theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising
     * in any way out of the use of this software, even if advised of the possibility of such damage.
     * @version Jun 23, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    abstract static class MathTester
    {

        /**
         * @param inputValue Double with unprocessed value
         * @param operation String; description of method that is being tested
         * @param actualResult DoubleScalar; the actual result of the operation
         * @param precision double expected accuracy
         * @param function DoubleToDouble encapsulating function that converts one value in inputValues to the
         *            corresponding value in resultValues
         */
        public static void tester(final double inputValue, String operation, final DoubleScalar<?> actualResult,
                final double precision, final DoubleToDouble function)
        {
            double expectedResult = function.function(inputValue);
            String description =
                    String.format("%s(%f)->%f should be equal to %f with precision %f", operation, inputValue,
                            expectedResult, actualResult.getValueSI(), precision);
            // System.out.println(description);
            assertEquals(description, expectedResult, actualResult.getValueSI(), precision);

        }

        /**
         * Function that takes a double value and returns a double value
         * @param in double value
         * @return double value
         */
        public abstract double function(double in);
    }

}

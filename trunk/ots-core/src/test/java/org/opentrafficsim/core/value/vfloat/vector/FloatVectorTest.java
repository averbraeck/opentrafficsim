package org.opentrafficsim.core.value.vfloat.vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Test;
import org.opentrafficsim.core.unit.EnergyUnit;
import org.opentrafficsim.core.unit.ForceUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.MassUnit;
import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.unit.UnitException;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalarAbs;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalarRel;

import cern.colt.matrix.tfloat.FloatMatrix1D;

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
 * @version Jun 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class FloatVectorTest
{
    /**
     * Test FloatVectorAbs and FloatVectorRel creators that take a float[] and a Unit as arguments
     */
    @Test
    public void floatVectorTwoArgs()
    {
        floatVectorTwoArgs(true); // test absolute version
        floatVectorTwoArgs(false); // rest relative version
    }

    /**
     * Test the FloatVectorAbs that takes a float[] and a Unit as arguments and some methods.
     */
    private void floatVectorTwoArgs(Boolean absolute)
    {
        float[] in = new float[10];
        for (int i = 0; i < in.length; i++)
            in[i] = i / 3.0f;
        LengthUnit u = LengthUnit.FOOT;
        FloatVector<LengthUnit> fv = createFloatVector(in, u, absolute);
        assertEquals("FloatVector should have 10 elements", 10, fv.size());
        float[] out = fv.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
        {
            assertEquals("Values in floatVector in unit should be equal to input values", in[i], out[i], 0.0001);
            try
            {
                assertEquals("Values in floatVector in unit should be equal to input values", in[i], fv.getInUnit(i),
                        0.0001);
                assertEquals("Values in floatVector in unit should be equal to input values", in[i], fv.getSI(i)
                        / (12 * 0.0254), 0.0001);
                assertEquals("Values in floatVector in unit should be equal to input values", in[i],
                        fv.getInUnit(i, LengthUnit.MILE) * 1609 / (12 * 0.0254), 0.001);
            }
            catch (ValueException exception)
            {
                fail("Get should not throw exceptions for legal values of the index");
            }
        }
        System.out.println(fv.toString());
        System.out.println(fv.toString(LengthUnit.NAUTICAL_MILE));
        FloatMatrix1D fm1d;
        if (fv instanceof FloatVectorAbsDense)
            fm1d = ((FloatVectorAbsDense<LengthUnit>) fv).getColtDenseFloatMatrix1D();
        else if (fv instanceof FloatVectorRelDense)
            fm1d = ((FloatVectorRelDense<LengthUnit>) fv).getColtDenseFloatMatrix1D();
        else if (fv instanceof FloatVectorAbsSparse)
            fm1d = ((FloatVectorAbsSparse<LengthUnit>) fv).getColtSparseFloatMatrix1D();
        else if (fv instanceof FloatVectorRelSparse)
            fm1d = ((FloatVectorRelSparse<LengthUnit>) fv).getColtSparseFloatMatrix1D();
        else
            throw new Error("Vector neither Dense nor Sparse");
        assertTrue("ColtMatrix1D should not be null", null != fm1d);
        assertEquals("Size of Colt matrix should be size of input array", in.length, fm1d.size());
        for (int i = 0; i < in.length; i++)
            assertEquals("Contents of Colt matrix should be SI equivalent of input", in[i] * (12 * 0.0254),
                    fm1d.getQuick(i), 0.0001);
        fm1d = fv.getVectorSI();
        assertTrue("VectorSI should not be null", null != fm1d);
        assertEquals("Size of VectorSI should be size of input array", in.length, fm1d.size());
        for (int i = 0; i < in.length; i++)
            assertEquals("Contents of VectorSI should be SI equivalent of input", in[i] * (12 * 0.0254),
                    fm1d.getQuick(i), 0.0001);
        float[] valuesInUnit = fv.getValuesInUnit();
        assertTrue("valuesInUnit should not be null", null != valuesInUnit);
        assertEquals("Size of valuesInUnit should be size of input array", in.length, valuesInUnit.length);
        for (int i = 0; i < in.length; i++)
            assertEquals("Contents of valuesInUnit should be equal to input", in[i], valuesInUnit[i], 0.0001);
        LengthUnit outputUnit = LengthUnit.DEKAMETER;
        float[] valuesInOtherUnit = fv.getValuesInUnit(outputUnit);
        assertTrue("valuesInUnit should not be null", null != valuesInOtherUnit);
        assertEquals("Size of valuesInUnit should be size of input array", in.length, valuesInOtherUnit.length);
        for (int i = 0; i < in.length; i++)
            assertEquals("Contents of valuesInUnit should be equal to input", in[i] * (12 * 0.0254) / 10,
                    valuesInOtherUnit[i], 0.0001);
        try
        {
            fv.getInUnit(-1);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getInUnit(in.length);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getSI(-1);
            fail("Using a bad index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getSI(in.length);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getInUnit(-1, LengthUnit.MILE);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getInUnit(in.length, LengthUnit.MILE);
            fail("Using an index that is too big should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.setSI(-1, 12345f);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        try
        {
            fv.setSI(in.length, 12345f);
            fail("Using an index that is too big should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        out = fv.getValuesSI();
        assertTrue("getValuesSI does not return null", null != out);
        assertEquals("Length of getValuesSI should match size", in.length, out.length);
        for (int i = 0; i < in.length; i++)
            assertEquals("Values in floatVector should be equivalent values in meters", in[i], out[i] / (12 * 0.0254),
                    0.0001);
        LengthUnit uOut = fv.getUnit();
        assertEquals("Stored unit should be provided unit", u, uOut);
        FloatVector<LengthUnit> copy = null;
        if (fv instanceof FloatVectorAbs<?>)
        {
            copy = ((FloatVectorAbs<LengthUnit>) fv).copy();
        }
        else if (fv instanceof FloatVectorRel<?>)
        {
            copy = ((FloatVectorRel<LengthUnit>) fv).copy();
        }
        else
            fail("Vector neither Absolute nor Relative");

        assertEquals("copy should have 10 elements", 10, copy.size());
        float[] copyOut = copy.getValuesSI();
        for (int i = 0; i < in.length; i++)
            assertEquals("Values in copy of floatVector should be equivalent values in meters", in[i], copyOut[i]
                    / (12 * 0.0254), 0.0001);
        copyOut = fv.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
            assertEquals("Values in copy of floatVector in unit should be equal to input values", in[i], copyOut[i],
                    0.0001);
        try
        {
            copy.setSI(0, 12345f);
            assertEquals("value should be altered", 12345f, copy.getSI(0), 0.01);
            assertEquals("original value should not be altered", out[0], fv.getSI(0), 0.001);
            FloatScalar<LengthUnit> value = copy.get(1);
            assertTrue("value cannot be null", null != value);
            assertEquals("value should be same as SI value", copy.getSI(1), value.getValueSI(), 0.0001);
            copy.set(2, value);
            assertEquals("value should be same as SI value", copy.getSI(2), value.getValueSI(), 0.0001);
        }
        catch (ValueException exception)
        {
            fail("set*/get* should not throw ValueException for valid index and correctly typed value");
        }
        try
        {
            copy.setInUnit(1, 321, LengthUnit.HECTOMETER);
            assertEquals("321 hectometer is 32100m", copy.getSI(1), 32100, 0.001);
        }
        catch (ValueException exception)
        {
            fail("Legal index should not throw exception");
        }
        float sum = 0;
        for (int i = 0; i < in.length; i++)
            sum += in[i];
        sum *= (12 * 0.0254); // convert to meters
        assertEquals("zsum should be sum of the values", sum, fv.zSum(), 0.001);
        try
        {
            fv.normalize();
            for (int i = 0; i < in.length; i++)
                assertEquals("Unexpected normalized value", in[i] * (12 * 0.0254) / sum, fv.getSI(i), 0.0001);
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertEquals("Cardinality should be 9", 9, fv.cardinality());
        float[] in2 = {1f, -1f, 0f};
        fv = createFloatVector(in2, u, absolute);
        assertEquals("zSum should be 0", 0, fv.zSum(), 0.00001);
        try
        {
            fv.normalize();
            fail("Should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        assertEquals("Cardinality should be 2", 2, fv.cardinality());
        try
        {
            fv.setSI(0, 0);
            assertEquals("Cardinality should be 1", 1, fv.cardinality());
            fv.setSI(1, 0);
            assertEquals("Cardinality should be 0", 0, fv.cardinality());
            fv.setSI(2, 999);
            assertEquals("Cardinality should be 1", 1, fv.cardinality());
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertTrue("FloatVector should be equal to itself", fv.equals(fv));
        assertFalse("FloatVector should not be equal to null", fv.equals(null));
        if (fv instanceof FloatVectorAbs<?>)
        {
            copy = ((FloatVectorAbs<LengthUnit>) fv).copy();
        }
        else if (fv instanceof FloatVectorRel<?>)
        {
            copy = ((FloatVectorRel<LengthUnit>) fv).copy();
        }
        else
            fail("Vector neither Absolute nor Relative");
        assertTrue("FloatVector should be equal to copy of itself", fv.equals(copy));
        try
        {
            copy.setSI(1, copy.getSI(1) + 0.001f);
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertFalse("FloatVector should different from slightly altered to copy of itself", fv.equals(copy));
        try
        {
            copy.setSI(1, fv.getSI(1));
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertTrue("FloatVector should equals to repaired copy of itself", fv.equals(copy));
        if (absolute)
        {
            float[] values = fv.getValuesInUnit();
            FloatVector<LengthUnit> fvr = createFloatVector(values, fv.getUnit(), false);

            try
            {
                for (int i = 0; i < in2.length; i++)
                    assertEquals("Values should be equal", fv.getSI(i), fvr.getSI(i), 0.00001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertEquals("fv and fvr should have same unit", fv.getUnit(), fvr.getUnit());
            assertFalse("fv and fvr should not be equal", fv.equals(fvr));
            assertFalse("fvr and fv should not be equal", fvr.equals(fv));
        }
        float[] in3 = {-100, -10, -1, -0.1f, 1, 0.1f, 1, 10, 100};
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.abs();
        MathTester.tester(in3, "abs", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return Math.abs(f);
            }
        });
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.acos();
        MathTester.tester(in3, "acos", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.acos(f);
            }
        });
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.asin();
        MathTester.tester(in3, "asin", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.asin(f);
            }
        });
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.atan();
        MathTester.tester(in3, "atan", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.atan(f);
            }
        });
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.cbrt();
        MathTester.tester(in3, "cbrt", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.cbrt(f);
            }
        });
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.ceil();
        MathTester.tester(in3, "ceil", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.ceil(f);
            }
        });
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.cos();
        MathTester.tester(in3, "cos", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.cos(f);
            }
        });
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.cosh();
        MathTester.tester(in3, "cosh", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.cosh(f);
            }
        });
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.exp();
        MathTester.tester(in3, "exp", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.exp(f);
            }
        });
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.expm1();
        MathTester.tester(in3, "expm1", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.expm1(f);
            }
        });
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.floor();
        MathTester.tester(in3, "floor", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.floor(f);
            }
        });
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.log();
        MathTester.tester(in3, "log", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.log(f);
            }
        });
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.log10();
        MathTester.tester(in3, "log10", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.log10(f);
            }
        });
        fv = createFloatVector(in3, LengthUnit.METER, absolute);
        fv.log1p();
        MathTester.tester(in3, "log1p", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.log1p(f);
            }
        });

    }

    /**
     * Interface encapsulating a function that takes a float and returns a float.
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
    interface FloatToFloat
    {
        /**
         * @param f float value
         * @return float value
         */
        float function(float f);
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
         * @param inputValues array of float with unprocessed values
         * @param operation TODO
         * @param resultValues array of float with processed values
         * @param precision double expected accuracy
         * @param function FloatToFloat encapsulating function that converts one value in inputValues to the
         *            corresponding value in resultValues
         */
        public static void tester(final float[] inputValues, String operation, final float[] resultValues,
                final double precision, final FloatToFloat function)
        {
            for (int i = 0; i < inputValues.length; i++)
            {
                float result = function.function(inputValues[i]);
                String description =
                        String.format("index=%d: %s(%f)->%f should be equal to %f with precision %f", i, operation,
                                inputValues[i], result, resultValues[i], precision);
                System.out.println(description);
                assertEquals(description, result, resultValues[i], precision);
            }
        }

        /**
         * Function that takes a float value and returns a float value
         * @param in float value
         * @return float value
         */
        public abstract float function(float in);
    }

    /**
     * Test FloatVectorAbs and FloatVectorRel creators that take an array of FloatScalar as argument
     */
    @Test
    public void floatVectorOneArg()
    {
        floatVectorOneArg(true); // test absolute version
        floatVectorOneArg(false); // test relative version
    }

    /**
     * Test the FloatVectorAbs and FloatVectorRel that takes a FloatScalar*<U>[] as argument
     */
    @SuppressWarnings("unchecked")
    private void floatVectorOneArg(Boolean absolute)
    {
        FloatVector<LengthUnit> fv = null;
        FloatScalarAbs<LengthUnit>[] inAbs = new FloatScalarAbs[0];
        FloatScalarRel<LengthUnit>[] inRel = new FloatScalarRel[0];
        try
        {
            if (absolute)
                fv = createFloatVectorAbs(inAbs);
            else
                fv = createFloatVectorRel(inRel);
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        inAbs = new FloatScalarAbs[1];
        inAbs[0] = new FloatScalarAbs<LengthUnit>(123.456f, LengthUnit.FOOT);
        inRel = new FloatScalarRel[1];
        inRel[0] = new FloatScalarRel<LengthUnit>(123.456f, LengthUnit.FOOT);
        try
        {
            if (absolute)
                fv = createFloatVectorAbs(inAbs);
            else
                fv = createFloatVectorRel(inRel);
        }
        catch (ValueException exception)
        {
            fail("Should NOT have thrown an exception");
        }
        float[] out = fv.getValuesInUnit();
        assertTrue("Result of getValuesInUnit should not be null", null != out);
        assertEquals("Array of values should have length 1", 1, out.length);
        assertEquals("Element in array should have the expected value", 123.456f, out[0], 0.001);
    }

    /**
     * Test adding and subtracting FloatVectorRel.
     */
    @Test
    public void relRel()
    {
        float[] in1 = {10f, 20f, 30f, 40f};
        float[] in2 = {110f, 120f, 130f, 140f};
        MassUnit u = MassUnit.POUND;
        FloatVectorRel<MassUnit> fv1 = createFloatVectorRel(in1, u);
        FloatVectorRel<MassUnit> fv2 = createFloatVectorRel(in2, u);
        FloatVectorRel<MassUnit> sum = null;
        try
        {
            sum = FloatVector.plus(fv1, fv2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add FloatVectorRel to FloatVectorRel of same size");
        }
        assertTrue("Result should not be null", null != sum);
        assertEquals("Size of result should be size of inputs", 4, sum.size());
        assertEquals("Type of result should be type of inputs", u, sum.getUnit());
        float[] sumValues = sum.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            assertEquals("Each element should equal the sum of the contributing elements", in1[i] + in2[i],
                    sumValues[i], 0.0001);
        FloatVectorRel<MassUnit> difference = null;
        try
        {
            difference = FloatVector.minus(fv1, fv2);
        }
        catch (ValueException exception1)
        {
            fail("Should be able to subtract FloatFectorRel from FloatFectorRel of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 4, difference.size());
        assertEquals("Type of result should be type of inputs", u, difference.getUnit());
        float[] differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            assertEquals("Each element should equal the difference of the contributing elements", in1[i] - in2[i],
                    differenceValues[i], 0.0001);
        float[] in3 = {110f, 120f, 130f};
        FloatVectorRel<MassUnit> fv3 = createFloatVectorRel(in3, u);
        try
        {
            sum = FloatVector.plus(fv1, fv3);
            fail("Adding FloatVectors of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            difference = FloatVector.minus(fv1, fv3);
            fail("Subtracting FloatVectors of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        MassUnit u2 = MassUnit.OUNCE;
        fv2 = createFloatVectorRel(in2, u2);
        try
        {
            sum = FloatVector.plus(fv1, fv2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add FloatVectorRel to FloatVectorRel of same size");
        }
        assertTrue("Result should not be null", null != sum);
        assertEquals("Size of result should be size of inputs", 4, sum.size());
        assertEquals("Type of result should be type of first input", u, sum.getUnit());
        assertFalse("Type of result should be different of type of second input", u2 == sum.getUnit());
        sumValues = sum.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
        {
            assertEquals("Each element should equal the weighted sum of the contributing elements", in1[i] * 0.45359
                    + in2[i] * 0.028350, sumValues[i] * 0.45359, 0.0001);
        }
        fv2 = createFloatVectorRel(in2, u2);
        try
        {
            difference = FloatVector.minus(fv1, fv2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add FloatVectorRel to FloatVectorRel of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 4, difference.size());
        assertEquals("Type of result should be type of first input", u, difference.getUnit());
        assertFalse("Type of result should be different of type of second input", u2 == difference.getUnit());
        differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
        {
            assertEquals("Each element should equal the weighted difference of the contributing elements", in1[i]
                    * 0.45359 - in2[i] * 0.028350, differenceValues[i] * 0.45359, 0.0001);
        }
    }

    /**
     * Test adding and subtracting FloatVectorAbs.
     */
    @Test
    public void absAbs()
    {
        float[] in1 = {10f, 20f, 30f, 40f};
        float[] in2 = {110f, 120f, 130f, 140f};
        MassUnit u = MassUnit.POUND;
        FloatVectorAbs<MassUnit> fv1 = createFloatVectorAbs(in1, u);
        FloatVectorAbs<MassUnit> fv2 = createFloatVectorAbs(in2, u);
        FloatVectorRel<MassUnit> difference = null;
        try
        {
            difference = FloatVector.minus(fv1, fv2);
        }
        catch (ValueException exception1)
        {
            fail("Should be able to subtract FloatFectorAbs from FloatFectorAbs of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 4, difference.size());
        assertEquals("Type of result should be type of inputs", u, difference.getUnit());
        float[] differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            assertEquals("Each element should equal the difference of the contributing elements", in1[i] - in2[i],
                    differenceValues[i], 0.0001);
        float[] in3 = {110f, 120f, 130f};
        FloatVectorAbs<MassUnit> fv3 = createFloatVectorAbs(in3, u);
        try
        {
            difference = FloatVector.minus(fv1, fv3);
            fail("Subtracting FloatVectors of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        MassUnit u2 = MassUnit.OUNCE;
        fv2 = createFloatVectorAbs(in2, u2);
        fv2 = createFloatVectorAbs(in2, u2);
        try
        {
            difference = FloatVector.minus(fv1, fv2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add FloatVectorAbs to FloatVectorAbs of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 4, difference.size());
        assertEquals("Type of result should be type of first input", u, difference.getUnit());
        assertFalse("Type of result should be different of type of second input", u2 == difference.getUnit());
        differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
        {
            assertEquals("Each element should equal the weighted difference of the contributing elements", in1[i]
                    * 0.45359 - in2[i] * 0.028350, differenceValues[i] * 0.45359, 0.0002);
        }
        try
        {
            difference = FloatVector.minus(fv2, fv1);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add FloatVectorAbs to FloatVectorAbs of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 4, difference.size());
        assertEquals("Type of result should be type of first input", u2, difference.getUnit());
        assertFalse("Type of result should be different of type of second input", u == difference.getUnit());
        differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
        {
            assertEquals("Each element should equal the weighted difference of the contributing elements", in2[i]
                    * 0.028350 - in1[i] * 0.45359, differenceValues[i] * 0.028350, 0.001);
        }
        LengthUnit u4 = LengthUnit.INCH;
        FloatVectorAbs<LengthUnit> fv4 = createFloatVectorAbs(in1, u4);
        ForceUnit u5 = ForceUnit.POUND_FORCE;
        FloatVectorAbs<ForceUnit> fv5 = createFloatVectorAbs(in2, u5);
        Unit<EnergyUnit> resultUnit = EnergyUnit.CALORIE_IT;
        FloatVectorAbs<?> product = null;
        try
        {
            product = FloatVector.multiply(fv4, fv5);
        }
        catch (ValueException exception)
        {
            fail("Should be able to multiply FloatVectorAbs with FloatVectorAbs of same size");
        }
        assertTrue("Result should not be null", null != product);
        assertEquals("Size of result should be size of inputs", 4, product.size());
        // System.out.println("unit of product is " + product.getUnit().getSICoefficientsString());
        // System.out.println("expected result unit is " + resultUnit);
        Set<Unit<?>> matches = null;
        try
        {
            matches =
                    Unit.lookupUnitWithSICoefficients(SICoefficients.normalize(product.getUnit().getStandardUnit()
                            .getSICoefficientsString()));
        }
        catch (UnitException exception)
        {
            exception.printStackTrace();
        }
        // System.out.println("matches: " + matches);
        assertTrue("Result is an EnergyUnit", matches.contains(resultUnit.getStandardUnit()));
        for (int i = 0; i < in1.length; i++)
        {
            try
            {
                assertEquals("Value in product should be product of contributing values", fv4.getSI(i) * fv5.getSI(i),
                        product.getSI(i), 0.00001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected ValueException");
            }
        }

    }

    /**
     * Test the FloatVectorRelDense that takes a float[] as argument.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void floatVectorRel2()
    {
        FloatVector<LengthUnit> fsa = null;
        FloatScalarRel<LengthUnit>[] in = new FloatScalarRel[0];
        try
        {
            fsa = createFloatVectorRel(in);
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        in = new FloatScalarRel[1];
        in[0] = new FloatScalarRel<LengthUnit>(123.456f, LengthUnit.FOOT);
        try
        {
            fsa = new FloatVectorRelDense<LengthUnit>(in);
        }
        catch (ValueException exception)
        {
            fail("Should NOT have thrown an exception");
        }
        float[] out = fsa.getValuesInUnit();
        assertTrue("Result of getValuesInUnit should not be null", null != out);
        assertEquals("Array of values should have length 1", 1, out.length);
        assertEquals("Element in array should have the expected value", 123.456f, out[0], 0.001);
    }

    /**
     * Create a FloatVectorAbs or a FloatVectorRel from an array of float values and Unit
     * @param in float[] with values
     * @param u Unit; type for the new FloatVector
     * @param absolute Boolean; true to create a FloatVectorAbs; false to create a FloatVectorRel
     * @return FloatVector
     */
    private <U extends Unit<U>> FloatVector<U> createFloatVector(float[] in, U u, boolean absolute)
    {
        if (absolute)
            return createFloatVectorAbs(in, u);
        else
            return createFloatVectorRel(in, u);
    }

    /**
     * Create a new FloatVectorAbs from an array of float values and Unit.
     * @param in float[] with values
     * @param u Unit; type for the new FloatVectorAbs
     * @return
     */
    protected abstract <U extends Unit<U>> FloatVectorAbs<U> createFloatVectorAbs(float[] in, U u);

    /**
     * Create a new FloatVectorAbs from an array of FloatScalarAbs values.
     * @param in FloatScalarAbs[]; the values
     * @return
     * @throws ValueException when the array is empty
     */
    protected abstract <U extends Unit<U>> FloatVectorAbs<U> createFloatVectorAbs(FloatScalarAbs<U>[] in)
            throws ValueException;

    /**
     * Create a new FloatVectorRel from an array of float values and Unit.
     * @param in float[] with values
     * @param u Unit; type for the new FloatVectorAbs
     * @return
     */
    protected abstract <U extends Unit<U>> FloatVectorRel<U> createFloatVectorRel(float[] in, U u);

    /**
     * Create a new FloatVectorRel from an array of FloatScalarAbs values.
     * @param in FloatScalarAbs[]; the values
     * @return
     * @throws ValueException when the array is empty
     */
    protected abstract <U extends Unit<U>> FloatVectorRel<U> createFloatVectorRel(FloatScalarRel<U>[] in)
            throws ValueException;

}

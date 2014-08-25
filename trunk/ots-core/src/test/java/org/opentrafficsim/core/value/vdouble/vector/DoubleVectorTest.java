package org.opentrafficsim.core.value.vdouble.vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;
import java.util.Set;

import org.junit.Test;
import org.opentrafficsim.core.unit.EnergyUnit;
import org.opentrafficsim.core.unit.ForceUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.MassUnit;
import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.unit.UnitException;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.Dense;
import org.opentrafficsim.core.value.Format;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.Sparse;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrix;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

import cern.colt.matrix.tdouble.DoubleMatrix1D;

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
 * @version Jun 24, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class DoubleVectorTest
{
    /**
     * Test DoubleVectorAbs and DoubleVectorRel creators that take a double[] and a Unit as arguments
     */
    @Test
    public void doubleVectorTwoArgs()
    {
        doubleVectorTwoArgs(true); // test absolute version
        doubleVectorTwoArgs(false); // rest relative version
    }

    /**
     * Test the doubleVectorAbs that takes a double[] and a Unit as arguments and some methods.
     */
    private void doubleVectorTwoArgs(Boolean absolute)
    {
        double[] in = new double[12];
        for (int i = 0; i < in.length; i++)
            in[i] = i / 3f;
        LengthUnit u = LengthUnit.FOOT;
        DoubleVector<LengthUnit> fv = createDoubleVector(in, u, absolute);
        assertEquals("DoubleVector should have 12 elements", 12, fv.size());
        double[] out = fv.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
        {
            assertEquals("Values in DoubleVector in unit should be equal to input values", in[i], out[i], 0.0001);
            try
            {
                assertEquals("Values in DoubleVector in unit should be equal to input values", in[i], fv.getInUnit(i),
                        0.0001);
                assertEquals("Values in DoubleVector in unit should be equal to input values", in[i], fv.getSI(i)
                        / (12 * 0.0254), 0.0001);
                assertEquals("Values in DoubleVector in unit should be equal to input values", in[i],
                        fv.getInUnit(i, LengthUnit.MILE) * 1609 / (12 * 0.0254), 0.001);
            }
            catch (ValueException exception)
            {
                fail("Get should not throw exceptions for legal values of the index");
            }
        }
        // System.out.println(fv.toString());
        String output = fv.toString(LengthUnit.MILLIMETER);
        // System.out.println("output is \"" + output + "\"");
        String[] fields = output.split("[ ]+");
        assertEquals("Number of fields should be number of entries + one", fv.size() + 1, fields.length);
        assertEquals("first field should contain unit in brackers", "[mm]", fields[0]);
        for (int i = 1; i < fields.length; i++)
        {
            double expectedValue = in[i - 1] * (12 * 0.0254) * 1000;
            String expected;
            expected = Format.format(expectedValue).trim();
            // System.out.println("expected: \"" + expected + "\", got \"" + fields[i] + "\", in is "+ in[i-1] +
            // " expectedValue is " + expectedValue);
            assertEquals("Field " + i + " should contain \"" + expected + "\"", expected, fields[i]);
        }
        output = fv.toString();
        fields = output.split("[ ]+");
        assertEquals("Number of fields should be number of entries + one", fv.size() + 1, fields.length);
        assertEquals("first field should contain unit in brackers", "[ft]", fields[0]);
        for (int i = 1; i < fields.length; i++)
        {
            double expectedValue = in[i - 1];
            String expected;
            if (0 == expectedValue)
                expected = String.format(Locale.US, "%8.3f", 0f);
            else if (Math.abs(expectedValue) > 0.01 && Math.abs(expectedValue) < 999.0)
                expected = String.format(Locale.US, "%8.3f", expectedValue);
            else
                expected = String.format(Locale.US, "%8.3e", expectedValue);
            expected = expected.trim();
            // System.out.println("expected: \"" + expected + "\", got \"" + fields[i] + "\", in is "+ in[i-1] +
            // " expectedValue is " + expectedValue);
            assertEquals("Field " + i + " should contain \"" + expected + "\"", expected, fields[i]);
        }

        DoubleMatrix1D dm1d;
        if (fv instanceof DoubleVectorAbsDense)
            dm1d = ((DoubleVectorAbsDense<LengthUnit>) fv).getColtDenseDoubleMatrix1D();
        else if (fv instanceof DoubleVectorRelDense)
            dm1d = ((DoubleVectorRelDense<LengthUnit>) fv).getColtDenseDoubleMatrix1D();
        else if (fv instanceof DoubleVectorAbsSparse)
            dm1d = ((DoubleVectorAbsSparse<LengthUnit>) fv).getColtSparseDoubleMatrix1D();
        else if (fv instanceof DoubleVectorRelSparse)
            dm1d = ((DoubleVectorRelSparse<LengthUnit>) fv).getColtSparseDoubleMatrix1D();
        else
            throw new Error("Vector neither Dense nor Sparse");
        assertTrue("ColtMatrix1D should not be null", null != dm1d);
        assertEquals("Size of Colt matrix should be size of input array", in.length, dm1d.size());
        for (int i = 0; i < in.length; i++)
            assertEquals("Contents of Colt matrix should be SI equivalent of input", in[i] * (12 * 0.0254),
                    dm1d.getQuick(i), 0.0001);
        dm1d = fv.getVectorSI();
        assertTrue("VectorSI should not be null", null != dm1d);
        assertEquals("Size of VectorSI should be size of input array", in.length, dm1d.size());
        for (int i = 0; i < in.length; i++)
            assertEquals("Contents of VectorSI should be SI equivalent of input", in[i] * (12 * 0.0254),
                    dm1d.getQuick(i), 0.0001);
        double[] valuesInUnit = fv.getValuesInUnit();
        assertTrue("valuesInUnit should not be null", null != valuesInUnit);
        assertEquals("Size of valuesInUnit should be size of input array", in.length, valuesInUnit.length);
        for (int i = 0; i < in.length; i++)
            assertEquals("Contents of valuesInUnit should be equal to input", in[i], valuesInUnit[i], 0.0001);
        LengthUnit outputUnit = LengthUnit.DEKAMETER;
        double[] valuesInOtherUnit = fv.getValuesInUnit(outputUnit);
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
            assertEquals("Values in DoubleVector should be equivalent values in meters", in[i], out[i] / (12 * 0.0254),
                    0.0001);
        LengthUnit uOut = fv.getUnit();
        assertEquals("Stored unit should be provided unit", u, uOut);
        DoubleVector<LengthUnit> copy = null;
        if (fv instanceof DoubleVectorAbs<?>)
        {
            copy = ((DoubleVectorAbs<LengthUnit>) fv).copy();
        }
        else if (fv instanceof DoubleVectorRel<?>)
        {
            copy = ((DoubleVectorRel<LengthUnit>) fv).copy();
        }
        else
            fail("Vector neither Absolute nor Relative");

        assertEquals("copy should have 10 elements", 12, copy.size());
        double[] copyOut = copy.getValuesSI();
        for (int i = 0; i < in.length; i++)
            assertEquals("Values in copy of DoubleVector should be equivalent values in meters", in[i], copyOut[i]
                    / (12 * 0.0254), 0.0001);
        copyOut = fv.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
            assertEquals("Values in copy of DoubleVector in unit should be equal to input values", in[i], copyOut[i],
                    0.0001);
        try
        {
            copy.setSI(0, 12345f);
            assertEquals("value should be altered", 12345f, copy.getSI(0), 0.01);
            assertEquals("original value should not be altered", out[0], fv.getSI(0), 0.001);
            DoubleScalar<LengthUnit> value = copy.get(1);
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
        double sum = 0;
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
        assertEquals("Cardinality should be 11", 11, fv.cardinality());
        double[] in2 = {1f, -1f, 0f};
        fv = createDoubleVector(in2, u, absolute);
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
        assertTrue("DoubleVector should be equal to itself", fv.equals(fv));
        assertFalse("DoubleVector should not be equal to null", fv.equals(null));
        if (fv instanceof DoubleVectorAbs<?>)
        {
            copy = ((DoubleVectorAbs<LengthUnit>) fv).copy();
        }
        else if (fv instanceof DoubleVectorRel<?>)
        {
            copy = ((DoubleVectorRel<LengthUnit>) fv).copy();
        }
        else
            fail("Vector neither Absolute nor Relative");
        assertTrue("DoubleVector should be equal to copy of itself", fv.equals(copy));
        try
        {
            copy.setSI(1, copy.getSI(1) + 0.001f);
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertFalse("DoubleVector should different from slightly altered to copy of itself", fv.equals(copy));
        try
        {
            copy.setSI(1, fv.getSI(1));
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertTrue("DoubleVector should equals to repaired copy of itself", fv.equals(copy));
        if (absolute)
        {
            double[] values = fv.getValuesInUnit();
            DoubleVector<LengthUnit> fvr = createDoubleVector(values, fv.getUnit(), false);

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
        double[] in3 = {-100, -10, -1, -0.1f, 1, 0.1f, 1, 10, 100};
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.abs();
        MathTester.tester(in3, "abs", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.abs(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.acos();
        MathTester.tester(in3, "acos", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.acos(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.asin();
        MathTester.tester(in3, "asin", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.asin(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.atan();
        MathTester.tester(in3, "atan", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.atan(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.cbrt();
        MathTester.tester(in3, "cbrt", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.cbrt(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.ceil();
        MathTester.tester(in3, "ceil", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.ceil(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.cos();
        MathTester.tester(in3, "cos", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.cos(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.cosh();
        MathTester.tester(in3, "cosh", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.cosh(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.exp();
        MathTester.tester(in3, "exp", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.exp(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.expm1();
        MathTester.tester(in3, "expm1", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.expm1(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.floor();
        MathTester.tester(in3, "floor", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.floor(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.log();
        MathTester.tester(in3, "log", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.log(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.log10();
        MathTester.tester(in3, "log10", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.log10(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.log1p();
        MathTester.tester(in3, "log1p", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.log1p(f);
            }
        });
        for (double power = -5; power <= 5; power += 0.5)
        {
            fv = createDoubleVector(in3, LengthUnit.METER, absolute);
            fv.pow(power);
            final double myPower = power;
            MathTester.tester(in3, "pow(" + power + ")", fv.getValuesSI(), 0.001, new DoubleToDouble()
            {
                public double function(final double f)
                {
                    return Math.pow(f, myPower);
                }
            });
        }
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.rint();
        MathTester.tester(in3, "rint", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.rint(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.round();
        MathTester.tester(in3, "round", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.round(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.signum();
        MathTester.tester(in3, "signum", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.signum(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.sin();
        MathTester.tester(in3, "sin", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.sin(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.sinh();
        MathTester.tester(in3, "sinh", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.sinh(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.sqrt();
        MathTester.tester(in3, "sqrt", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.sqrt(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.tan();
        MathTester.tester(in3, "tan", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.tan(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.tanh();
        MathTester.tester(in3, "tanh", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.tanh(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.toDegrees();
        MathTester.tester(in3, "toDegrees", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.toDegrees(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.toRadians();
        MathTester.tester(in3, "toRadians", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.toRadians(f);
            }
        });
        fv = createDoubleVector(in3, LengthUnit.METER, absolute);
        fv.inv();
        MathTester.tester(in3, "inv", fv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return 1.0 / f;
            }
        });
        for (double factor = -5; factor <= 5; factor += 0.5)
        {
            fv = createDoubleVector(in3, LengthUnit.METER, absolute);
            fv.multiply(factor);
            final double myFactor = factor;
            MathTester.tester(in3, "multiply(" + factor + ")", fv.getValuesSI(), 0.001, new DoubleToDouble()
            {
                public double function(final double f)
                {
                    return myFactor * f;
                }
            });
        }
        if (absolute)
        {
            DoubleVectorAbs<LengthUnit> fv1 =
                    (DoubleVectorAbs<LengthUnit>) createDoubleVector(in3, LengthUnit.METER, true);
            double[] in4 = {1, 2, 3, 4};
            DoubleVectorRel<LengthUnit> fv2 =
                    (DoubleVectorRel<LengthUnit>) createDoubleVector(in4, LengthUnit.METER, false);
            DoubleVector<LengthUnit> plus = null;
            DoubleVector<LengthUnit> minus = null;
            try
            {
                plus = DoubleVector.plus(fv1, fv2);
                fail("Adding DoubleVectors of unequal length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            try
            {
                minus = DoubleVector.minus(fv1, fv2);
                fail("Subtracting DoubleVectors of unequal length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            double[] in5 = {1, 2, 3, 4, 5, 6, 7, 8, 9};
            fv2 = (DoubleVectorRel<LengthUnit>) createDoubleVector(in5, LengthUnit.METER, false);
            try
            {
                plus = DoubleVector.plus(fv1, fv2);
                minus = DoubleVector.minus(fv1, fv2);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("plus should not return null", null != plus);
            assertEquals("size of result should be " + in5.length, in5.length, plus.size());
            assertTrue("minus should not return null", null != minus);
            assertEquals("size of result should be " + in5.length, in5.length, minus.size());
            try
            {
                for (int i = 0; i < in3.length; i++)
                    assertEquals("value of element should be sum of contributing elements", in3[i] + in5[i],
                            plus.getSI(i), 0.00001);
                for (int i = 0; i < in3.length; i++)
                    assertEquals("value of element should be sum of contributing elements", in3[i] - in5[i],
                            minus.getSI(i), 0.00001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("result should be Absolute", plus instanceof Absolute);
            assertTrue("result should be Absolute", minus instanceof Absolute);
            if (fv1 instanceof Dense)
            {
                assertTrue("result should be Dense", plus instanceof Dense);
                assertTrue("result should be Dense", minus instanceof Dense);
            }
            else if (fv1 instanceof Sparse)
            {
                assertTrue("result should be Sparse", plus instanceof Sparse);
                assertTrue("result should be Sparse", minus instanceof Sparse);
            }
            else
                fail("fv1 neither Dense nor Sparse");

            DoubleVector<LengthUnit> plusReverse = null;
            try
            {
                plusReverse = DoubleVector.plus(fv2, fv1);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("result should be Absolute", plusReverse instanceof Absolute);
            // System.out.println("plus is       " + plus);
            // System.out.println("plusReverse is" + plusReverse);
            assertTrue("result of a + b should be equal to result of b + a", plus.equals(plusReverse));
            try
            {
                double in6[]= {1, 2, 3};
                DoubleVector<LengthUnit> original = createDoubleVector(in6, LengthUnit.METER, absolute);
                DoubleVector<LengthUnit> duplicate = (DoubleVector<LengthUnit>) original.copy();
                assertTrue("Original should be equal to duplicate", original.equals(duplicate));
                assertTrue("Duplicate should be equal to original", duplicate.equals(original));
                original.setSI(0, 123.456);
                assertFalse("Original should now differ from duplicate", original.equals(duplicate));
                assertFalse("Duplicate should now differ from original", duplicate.equals(original));
                
            }
            catch (ValueException exception)
            {
                fail("Unexpected ValueException");
            }
        }
        else
        {
            DoubleVectorRel<LengthUnit> fv1 =
                    (DoubleVectorRel<LengthUnit>) createDoubleVector(in3, LengthUnit.METER, false);
            double[] in4 = {1, 2, 3, 4};
            DoubleVectorRel<LengthUnit> fv2 =
                    (DoubleVectorRel<LengthUnit>) createDoubleVector(in4, LengthUnit.METER, false);
            DoubleVector<SIUnit> multiply = null;
            try
            {
                multiply = DoubleVector.multiply(fv1, fv2);
                fail("Adding DoubleVectors of unequal length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            double[] in5 = {1, 2, 3, 4, 5, 6, 7, 8, 9};
            fv2 = (DoubleVectorRel<LengthUnit>) createDoubleVector(in5, LengthUnit.METER, false);
            try
            {
                multiply = DoubleVector.multiply(fv1, fv2);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("multiply should not return null", null != multiply);
            assertEquals("size of result should be " + in5.length, in5.length, multiply.size());
            try
            {
                for (int i = 0; i < in3.length; i++)
                    assertEquals("value of element should be sum of contributing elements", in3[i] * in5[i],
                            multiply.getSI(i), 0.00001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("result should be Relative", multiply instanceof Relative);
            if (fv1 instanceof Dense)
            {
                assertTrue("result should be Dense", multiply instanceof Dense);
            }
            else if (fv1 instanceof Sparse)
            {
                assertTrue("result should be Sparse", multiply instanceof Sparse);
            }
            else
                fail("fv1 neither Dense nor Sparse");
            // System.out.println("Result of multiply has unit " + multiply);
            assertEquals("Result of multiplication should be in square meters", "m2", multiply.getUnit()
                    .getSICoefficientsString());

            DoubleVector<SIUnit> multiplyReverse = null;
            try
            {
                multiplyReverse = DoubleVector.multiply(fv2, fv1);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("result should be Absolute", multiplyReverse instanceof Relative);
            // System.out.println("plus is       " + multiply);
            // System.out.println("plusReverse is" + multiplyReverse);
            assertTrue("result of a * b should be equal to result of b * a", multiply.equals(multiplyReverse));
            try
            {
                double in6[]= {1, 2, 3};
                DoubleVector<LengthUnit> original = createDoubleVector(in6, LengthUnit.METER, absolute);
                DoubleVector<LengthUnit> duplicate = (DoubleVector<LengthUnit>) original.copy();
                assertTrue("Original should be equal to duplicate", original.equals(duplicate));
                assertTrue("Duplicate should be equal to original", duplicate.equals(original));
                original.setSI(0, 123.456);
                assertFalse("Original should now differ from duplicate", original.equals(duplicate));
                assertFalse("Duplicate should now differ from original", duplicate.equals(original));
                
            }
            catch (ValueException exception)
            {
                fail("Unexpected ValueException");
            }
        }
        fv = createDoubleVector(in, u, absolute);
        double[] factorsTooShort = {10, 20, 30, 40, 50, 60};
        double[] factorsCorrectLength = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120};
        if (absolute)
        {
            DoubleVectorAbs<LengthUnit> fv2 = null;
            try
            {
                fv2 = DoubleVector.multiply((DoubleVectorAbs<LengthUnit>) fv, factorsTooShort);
                fail("Multiplication array of wrong length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            try
            {
                fv2 = DoubleVector.multiply((DoubleVectorAbs<LengthUnit>) fv, factorsCorrectLength);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            try
            {
                for (int i = 0; i < in.length; i++)
                    assertEquals("values in fv2 should be product of contributing values", in[i] * (12 * 0.0254)
                            * factorsCorrectLength[i], fv2.getSI(i), 0.0001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertEquals("type of result should be input type", fv.getUnit(), fv2.getUnit());
        }
        else
        {
            DoubleVectorRel<LengthUnit> fv2 = null;
            try
            {
                fv2 = DoubleVector.multiply((DoubleVectorRel<LengthUnit>) fv, factorsTooShort);
                fail("Multiplication array of wrong length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            try
            {
                fv2 = DoubleVector.multiply((DoubleVectorRel<LengthUnit>) fv, factorsCorrectLength);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            try
            {
                for (int i = 0; i < in.length; i++)
                    assertEquals("values in fv2 should be product of contributing values", in[i] * (12 * 0.0254)
                            * factorsCorrectLength[i], fv2.getSI(i), 0.0001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertEquals("type of result should be input type", fv.getUnit(), fv2.getUnit());
        }
        if (fv instanceof Dense)
        {
            DoubleVector<LengthUnit> fv2 = null;
            if (fv instanceof Absolute)
                fv2 = DoubleVector.denseToSparse((DoubleVectorAbsDense<LengthUnit>) fv);
            else
                fv2 = DoubleVector.denseToSparse((DoubleVectorRelDense<LengthUnit>) fv);
            // FIXME this one fails: assertFalse("dense version is not equal to sparse version", fv.equals(fv2));
            assertEquals("unit should be same", fv.getUnit(), fv2.getUnit());
            try
            {
                for (int i = 0; i < fv.size(); i++)
                    assertEquals("Values should be equal", fv.getSI(i), fv2.getSI(i), 0.0001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
        }
        else
        {
            DoubleVector<LengthUnit> fv2 = null;
            if (fv instanceof Absolute)
                fv2 = DoubleVector.sparseToDense((DoubleVectorAbsSparse<LengthUnit>) fv);
            else
                fv2 = DoubleVector.sparseToDense((DoubleVectorRelSparse<LengthUnit>) fv);
            // FIXME this one fails: assertFalse("dense version is not equal to sparse version", fv.equals(fv2));
            assertEquals("unit should be same", fv.getUnit(), fv2.getUnit());
            try
            {
                for (int i = 0; i < fv.size(); i++)
                    assertEquals("Values should be equal", fv.getSI(i), fv2.getSI(i), 0.0001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
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
         * @param inputValues array of double with unprocessed values
         * @param operation String; description of method that is being tested
         * @param resultValues array of double with processed values
         * @param precision double expected accuracy
         * @param function DoubleToDouble encapsulating function that converts one value in inputValues to the
         *            corresponding value in resultValues
         */
        public static void tester(final double[] inputValues, String operation, final double[] resultValues,
                final double precision, final DoubleToDouble function)
        {
            for (int i = 0; i < inputValues.length; i++)
            {
                double result = function.function(inputValues[i]);
                String description =
                        String.format("index=%d: %s(%f)->%f should be equal to %f with precision %f", i, operation,
                                inputValues[i], result, resultValues[i], precision);
                // System.out.println(description);
                assertEquals(description, result, resultValues[i], precision);
            }
        }

        /**
         * Function that takes a double value and returns a double value
         * @param in double value
         * @return double value
         */
        public abstract double function(double in);
    }

    /**
     * Test DoubleVectorAbs and DoubleVectorRel creators that take an array of DoubleScalar as argument
     */
    @Test
    public void doubleVectorOneArg()
    {
        doubleVectorOneArg(true); // test absolute version
        doubleVectorOneArg(false); // test relative version
    }

    /**
     * Test the DoubleVectorAbs and DoubleVectorRel that takes a DoubleScalar*<U>[] as argument
     */
    @SuppressWarnings("unchecked")
    private void doubleVectorOneArg(Boolean absolute)
    {
        DoubleVector<LengthUnit> fv = null;
        DoubleScalarAbs<LengthUnit>[] inAbs = new DoubleScalarAbs[0];
        DoubleScalarRel<LengthUnit>[] inRel = new DoubleScalarRel[0];
        try
        {
            if (absolute)
                fv = createDoubleVectorAbs(inAbs);
            else
                fv = createDoubleVectorRel(inRel);
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        inAbs = new DoubleScalarAbs[1];
        inAbs[0] = new DoubleScalarAbs<LengthUnit>(123.456, LengthUnit.FOOT);
        inRel = new DoubleScalarRel[1];
        inRel[0] = new DoubleScalarRel<LengthUnit>(123.456, LengthUnit.FOOT);
        try
        {
            if (absolute)
                fv = createDoubleVectorAbs(inAbs);
            else
                fv = createDoubleVectorRel(inRel);
        }
        catch (ValueException exception)
        {
            fail("Should NOT have thrown an exception");
        }
        double[] out = fv.getValuesInUnit();
        assertTrue("Result of getValuesInUnit should not be null", null != out);
        assertEquals("Array of values should have length 1", 1, out.length);
        assertEquals("Element in array should have the expected value", 123.456, out[0], 0.001);
    }

    /**
     * Test adding and subtracting DoubleVectorRel.
     */
    @Test
    public void relRel()
    {
        double[] in1 = {10, 20, 30, 40};
        double[] in2 = {110, 120, 130, 140};
        MassUnit u = MassUnit.POUND;
        DoubleVectorRel<MassUnit> fv1 = createDoubleVectorRel(in1, u);
        DoubleVectorRel<MassUnit> fv2 = createDoubleVectorRel(in2, u);
        DoubleVectorRel<MassUnit> sum = null;
        try
        {
            sum = DoubleVector.plus(fv1, fv2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add DoubleVectorRel to DoubleVectorRel of same size");
        }
        assertTrue("Result should not be null", null != sum);
        assertEquals("Size of result should be size of inputs", 4, sum.size());
        assertEquals("Type of result should be type of inputs", u, sum.getUnit());
        double[] sumValues = sum.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            assertEquals("Each element should equal the sum of the contributing elements", in1[i] + in2[i],
                    sumValues[i], 0.0001);
        DoubleVectorRel<MassUnit> difference = null;
        try
        {
            difference = DoubleVector.minus(fv1, fv2);
        }
        catch (ValueException exception1)
        {
            fail("Should be able to subtract DoubleVectorRel from DoubleVectorRel of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 4, difference.size());
        assertEquals("Type of result should be type of inputs", u, difference.getUnit());
        double[] differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            assertEquals("Each element should equal the difference of the contributing elements", in1[i] - in2[i],
                    differenceValues[i], 0.0001);
        double[] in3 = {110f, 120f, 130f};
        DoubleVectorRel<MassUnit> fv3 = createDoubleVectorRel(in3, u);
        try
        {
            sum = DoubleVector.plus(fv1, fv3);
            fail("Adding DoubleVectors of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            difference = DoubleVector.minus(fv1, fv3);
            fail("Subtracting DoubleVectors of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        MassUnit u2 = MassUnit.OUNCE;
        fv2 = createDoubleVectorRel(in2, u2);
        try
        {
            sum = DoubleVector.plus(fv1, fv2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add DoubleVectorRel to DoubleVectorRel of same size");
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
        fv2 = createDoubleVectorRel(in2, u2);
        try
        {
            difference = DoubleVector.minus(fv1, fv2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add DoubleVectorRel to DoubleVectorRel of same size");
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
     * Test adding and subtracting DoubleVectorAbs.
     */
    @Test
    public void absAbs()
    {
        double[] in1 = {10, 20, 30, 40};
        double[] in2 = {110, 120, 130, 140};
        MassUnit u = MassUnit.POUND;
        DoubleVectorAbs<MassUnit> fv1 = createDoubleVectorAbs(in1, u);
        DoubleVectorAbs<MassUnit> fv2 = createDoubleVectorAbs(in2, u);
        DoubleVectorRel<MassUnit> difference = null;
        try
        {
            difference = DoubleVector.minus(fv1, fv2);
        }
        catch (ValueException exception1)
        {
            fail("Should be able to subtract DoubleVectorAbs from DoubleVectorAbs of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 4, difference.size());
        assertEquals("Type of result should be type of inputs", u, difference.getUnit());
        double[] differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            assertEquals("Each element should equal the difference of the contributing elements", in1[i] - in2[i],
                    differenceValues[i], 0.0001);
        double[] in3 = {110, 120, 130};
        DoubleVectorAbs<MassUnit> fv3 = createDoubleVectorAbs(in3, u);
        try
        {
            difference = DoubleVector.minus(fv1, fv3);
            fail("Subtracting DoubleVectors of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        MassUnit u2 = MassUnit.OUNCE;
        fv2 = createDoubleVectorAbs(in2, u2);
        fv2 = createDoubleVectorAbs(in2, u2);
        try
        {
            difference = DoubleVector.minus(fv1, fv2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add DoubleVectorAbs to DoubleVectorAbs of same size");
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
            difference = DoubleVector.minus(fv2, fv1);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add DoubleVectorAbs to DoubleVectorAbs of same size");
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
        DoubleVectorAbs<LengthUnit> fv4 = createDoubleVectorAbs(in1, u4);
        ForceUnit u5 = ForceUnit.POUND_FORCE;
        DoubleVectorAbs<ForceUnit> fv5 = createDoubleVectorAbs(in2, u5);
        Unit<EnergyUnit> resultUnit = EnergyUnit.CALORIE_IT;
        DoubleVectorAbs<?> product = null;
        try
        {
            product = DoubleVector.multiply(fv4, fv5);
        }
        catch (ValueException exception)
        {
            fail("Should be able to multiply DoubleVectorAbs with DoubleVectorAbs of same size");
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
     * Test the DoubleVectorRelDense that takes a double[] as argument.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void doubleVectorRel2()
    {
        DoubleVector<LengthUnit> fsa = null;
        DoubleScalarRel<LengthUnit>[] in = new DoubleScalarRel[0];
        try
        {
            fsa = createDoubleVectorRel(in);
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        in = new DoubleScalarRel[1];
        in[0] = new DoubleScalarRel<LengthUnit>(123.456, LengthUnit.FOOT);
        try
        {
            fsa = new DoubleVectorRelDense<LengthUnit>(in);
        }
        catch (ValueException exception)
        {
            fail("Should NOT have thrown an exception");
        }
        double[] out = fsa.getValuesInUnit();
        assertTrue("Result of getValuesInUnit should not be null", null != out);
        assertEquals("Array of values should have length 1", 1, out.length);
        assertEquals("Element in array should have the expected value", 123.456, out[0], 0.001);
    }

    /**
     * Create a DoubleVectorAbs or a DoubleVectorRel from an array of double values and Unit
     * @param in double[] with values
     * @param u Unit; type for the new DoubleVector
     * @param absolute Boolean; true to create a DoubleVectorAbs; false to create a DoubleVectorRel
     * @return DoubleVector
     */
    private <U extends Unit<U>> DoubleVector<U> createDoubleVector(double[] in, U u, boolean absolute)
    {
        if (absolute)
            return createDoubleVectorAbs(in, u);
        else
            return createDoubleVectorRel(in, u);
    }

    /**
     * Create a new DoubleVectorAbs from an array of double values and Unit.
     * @param in double[] with values
     * @param u Unit; type for the new DoubleVectorAbs
     * @return
     */
    protected abstract <U extends Unit<U>> DoubleVectorAbs<U> createDoubleVectorAbs(double[] in, U u);

    /**
     * Create a new DoubleVectorAbs from an array of DoubleScalarAbs values.
     * @param in DoubleScalarAbs[]; the values
     * @return
     * @throws ValueException when the array is empty
     */
    protected abstract <U extends Unit<U>> DoubleVectorAbs<U> createDoubleVectorAbs(DoubleScalarAbs<U>[] in)
            throws ValueException;

    /**
     * Create a new DoubleVectorRel from an array of double values and Unit.
     * @param in double[] with values
     * @param u Unit; type for the new DoubleVectorAbs
     * @return
     */
    protected abstract <U extends Unit<U>> DoubleVectorRel<U> createDoubleVectorRel(double[] in, U u);

    /**
     * Create a new DoubleVectorRel from an array of DoubleScalarAbs values.
     * @param in DoubleScalarAbs[]; the values
     * @return
     * @throws ValueException when the array is empty
     */
    protected abstract <U extends Unit<U>> DoubleVectorRel<U> createDoubleVectorRel(DoubleScalarRel<U>[] in)
            throws ValueException;

}

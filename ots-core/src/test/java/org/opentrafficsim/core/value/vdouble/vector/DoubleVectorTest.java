package org.opentrafficsim.core.value.vdouble.vector;

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
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.unit.UnitException;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.Format;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class DoubleVectorTest
{
    /**
     * Test DoubleVectorAbs and DoubleVectorRel creators that take a double[] and a Unit as arguments.
     */
    @Test
    public final void doubleVectorTwoArgs()
    {
        doubleVectorTwoArgs(true); // test absolute version
        doubleVectorTwoArgs(false); // rest relative version
    }

    /**
     * Test the DoubleVectorAbs that takes a double[] and a Unit as arguments and some methods.
     * @param absolute Boolean;
     */
    private void doubleVectorTwoArgs(final Boolean absolute)
    {
        double[] in = new double[12];
        for (int i = 0; i < in.length; i++)
        {
            in[i] = i / 3f;
        }
        LengthUnit u = LengthUnit.FOOT;
        DoubleVector<LengthUnit> fv = createDoubleVector(in, u, absolute);
        // System.out.println("fv: " + fv);
        assertEquals("Constructed DoubleVector should have 12 elements", 12, fv.size());
        double[] out = fv.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
        {
            assertEquals("Values in DoubleVector in unit should be equal to input values", in[i], out[i], 0.0001);
            try
            {
                assertEquals("Values in DoubleVector in unit should be equal to input values", in[i], fv.getInUnit(i), 0.0001);
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
        assertEquals("Number of fields should be number of entries + four", fv.size() + 4, fields.length);
        assertEquals("field 3 should contain unit in brackets", "[mm]", fields[3]);
        for (int i = 4; i < fields.length; i++)
        {
            double expectedValue = (in[i - 4] * (12 * 0.0254) * 1000);
            String expected = Format.format(expectedValue).trim();
            // System.out.println("expected: \"" + expected + "\", got \"" + fields[i] + "\", in is " + in[i - 1]
            // + " expectedValue is " + expectedValue);
            assertEquals("Field " + i + " should contain \"" + expected + "\"", expected, fields[i]);
        }
        output = fv.toString();
        fields = output.split("[ ]+");
        assertEquals("Number of fields should be number of entries + four", fv.size() + 4, fields.length);
        assertEquals("field 3 should contain unit in brackers", "[ft]", fields[3]);
        for (int i = 4; i < fields.length; i++)
        {
            double expectedValue = in[i - 4];
            String expected;
            expected = Format.format(expectedValue);
            expected = expected.trim();
            // System.out.println("expected: \"" + expected + "\", got \"" + fields[i] + "\", in is "+ in[i-1] +
            // " expectedValue is " + expectedValue);
            assertEquals("Field " + i + " should contain \"" + expected + "\"", expected, fields[i]);
        }

        double[] valuesInUnit = fv.getValuesInUnit();
        assertTrue("valuesInUnit should not be null", null != valuesInUnit);
        assertEquals("Size of valuesInUnit should be size of input array", in.length, valuesInUnit.length);
        for (int i = 0; i < in.length; i++)
        {
            assertEquals("Contents of valuesInUnit should be equal to input", in[i], valuesInUnit[i], 0.0001);
        }
        LengthUnit outputUnit = LengthUnit.DEKAMETER;
        double[] valuesInOtherUnit = fv.getValuesInUnit(outputUnit);
        assertTrue("valuesInUnit should not be null", null != valuesInOtherUnit);
        assertEquals("Size of valuesInUnit should be size of input array", in.length, valuesInOtherUnit.length);
        for (int i = 0; i < in.length; i++)
        {
            assertEquals("Contents of valuesInUnit should be equal to input", in[i] * (12 * 0.0254) / 10, valuesInOtherUnit[i],
                    0.0001);
        }
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
        MutableDoubleVector<LengthUnit> mfv = fv.mutable();
        assertEquals("fv and mfv should have same size", mfv.size(), fv.size());
        try
        {
            mfv.setSI(-1, 12345f);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        try
        {
            mfv.setSI(in.length, 12345f);
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
        {
            assertEquals("Values in DoubleVector should be equivalent values in meters", in[i], out[i] / (12 * 0.0254), 0.0001);
        }
        out = mfv.getValuesSI();
        assertTrue("getValuesSI does not return null", null != out);
        assertEquals("Length of getValuesSI should match size", in.length, out.length);
        for (int i = 0; i < in.length; i++)
        {
            assertEquals("Values in DoubleVector should be equivalent values in meters", in[i], out[i] / (12 * 0.0254), 0.0001);
        }
        LengthUnit uOut = fv.getUnit();
        assertEquals("Stored unit should be provided unit", u, uOut);
        try
        {
            mfv.setSI(0, 12345f);
            assertEquals("value should be altered", 12345f, mfv.getSI(0), 0.01);
            assertEquals("original value should not be altered", out[0], fv.getSI(0), 0.001);
            DoubleScalar<LengthUnit> value = mfv.get(1);
            assertTrue("value cannot be null", null != value);
            assertEquals("value should be same as SI value", mfv.getSI(1), value.getSI(), 0.0001);
            mfv.set(2, value);
            assertEquals("value should be same as SI value", mfv.getSI(2), value.getSI(), 0.0001);
        }
        catch (ValueException exception)
        {
            fail("set*/get* should not throw ValueException for valid index and correctly typed value");
        }
        try
        {
            mfv.setInUnit(1, 321, LengthUnit.HECTOMETER);
            assertEquals("321 hectometer is 32100m", mfv.getSI(1), 32100, 0.001);
        }
        catch (ValueException exception)
        {
            fail("Legal index should not throw exception");
        }
        double sum = 0;
        for (int i = 0; i < in.length; i++)
        {
            sum += in[i];
        }
        sum *= (12 * 0.0254); // convert to meters
        assertEquals("zsum should be sum of the values", sum, fv.zSum(), 0.001);
        mfv = fv.mutable(); // undo edits to mfv
        try
        {
            mfv.normalize();
            for (int i = 0; i < in.length; i++)
            {
                assertEquals("Expected normalized value", in[i] * (12 * 0.0254) / sum, mfv.getSI(i), 0.0001);
            }
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertEquals("Cardinality should be 11", 11, fv.cardinality());
        double[] in2 = {1f, -1f, 0f };
        mfv = createDoubleVector(in2, u, absolute).mutable();
        assertEquals("zSum should be 0", 0, mfv.zSum(), 0.00001);
        try
        {
            mfv.normalize();
            fail("Should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        assertEquals("Cardinality should be 2", 2, mfv.cardinality());
        try
        {
            mfv.setSI(0, 0);
            assertEquals("Cardinality should be 1", 1, mfv.cardinality());
            mfv.setSI(1, 0);
            assertEquals("Cardinality should be 0", 0, mfv.cardinality());
            mfv.setSI(2, 999);
            assertEquals("Cardinality should be 1", 1, mfv.cardinality());
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertTrue("DoubleVector should be equal to itself", mfv.equals(mfv));
        assertFalse("DoubleVector should not be equal to null", mfv.equals(null));
        MutableDoubleVector<LengthUnit> copy = mfv.copy();
        assertTrue("DoubleVector should be equal to copy of itself", mfv.equals(copy));
        try
        {
            copy.setSI(1, copy.getSI(1) + 0.001f);
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertFalse("DoubleVector should be different from slightly altered to copy of itself", mfv.equals(copy));
        try
        {
            copy.setSI(1, mfv.getSI(1));
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertTrue("DoubleVector should be equal to repaired copy of itself", mfv.equals(copy));
        if (absolute)
        {
            double[] values = fv.getValuesInUnit();
            DoubleVector<LengthUnit> fvr = createDoubleVector(values, fv.getUnit(), false);
            try
            {
                for (int i = 0; i < in2.length; i++)
                {
                    assertEquals("Values should be equal", fv.getSI(i), fvr.getSI(i), 0.00001);
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertEquals("fv and fvr should have same unit", fv.getUnit(), fvr.getUnit());
            assertFalse("fv and fvr should not be equal", fv.equals(fvr));
            assertFalse("fvr and fv should not be equal", fvr.equals(fv));
        }
        double[] in3 = {-100, -10, -1, -0.1f, 1, 0.1f, 1, 10, 100 };
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.abs();
        MathTester.tester(in3, "abs", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.abs(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.acos();
        MathTester.tester(in3, "acos", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.acos(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.asin();
        MathTester.tester(in3, "asin", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.asin(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.atan();
        MathTester.tester(in3, "atan", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.atan(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.cbrt();
        MathTester.tester(in3, "cbrt", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.cbrt(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.ceil();
        MathTester.tester(in3, "ceil", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.ceil(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.cos();
        MathTester.tester(in3, "cos", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.cos(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.cosh();
        MathTester.tester(in3, "cosh", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.cosh(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.exp();
        MathTester.tester(in3, "exp", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.exp(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.expm1();
        MathTester.tester(in3, "expm1", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.expm1(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.floor();
        MathTester.tester(in3, "floor", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.floor(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.log();
        MathTester.tester(in3, "log", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.log(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.log10();
        MathTester.tester(in3, "log10", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.log10(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.log1p();
        MathTester.tester(in3, "log1p", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.log1p(f);
            }
        });
        for (double power = -5; power <= 5; power += 0.5)
        {
            mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
            mfv.pow(power);
            final double myPower = power;
            MathTester.tester(in3, "pow(" + power + ")", mfv.getValuesSI(), 0.001, new DoubleToDouble()
            {
                @Override
                public double function(final double f)
                {
                    return Math.pow(f, myPower);
                }
            });
        }
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.rint();
        MathTester.tester(in3, "rint", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.rint(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.round();
        MathTester.tester(in3, "round", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.round(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.signum();
        MathTester.tester(in3, "signum", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.signum(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.sin();
        MathTester.tester(in3, "sin", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.sin(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.sinh();
        MathTester.tester(in3, "sinh", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.sinh(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.sqrt();
        MathTester.tester(in3, "sqrt", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.sqrt(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.tan();
        MathTester.tester(in3, "tan", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.tan(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.tanh();
        MathTester.tester(in3, "tanh", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.tanh(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.toDegrees();
        MathTester.tester(in3, "toDegrees", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.toDegrees(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.toRadians();
        MathTester.tester(in3, "toRadians", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.toRadians(f);
            }
        });
        mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
        mfv.inv();
        MathTester.tester(in3, "inv", mfv.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return 1.0 / f;
            }
        });
        for (double factor = -5; factor <= 5; factor += 0.5)
        {
            mfv = createDoubleVector(in3, LengthUnit.METER, absolute).mutable();
            mfv.multiply(factor);
            final double myFactor = factor;
            MathTester.tester(in3, "multiply(" + factor + ")", mfv.getValuesSI(), 0.001, new DoubleToDouble()
            {
                @Override
                public double function(final double f)
                {
                    return myFactor * f;
                }
            });
        }
        if (absolute)
        {
            DoubleVector<LengthUnit> fvAbsolute = createDoubleVector(in3, LengthUnit.METER, true);
            double[] in4 = {1, 2, 3, 4 };
            DoubleVector<LengthUnit> fvRelative = createDoubleVector(in4, LengthUnit.METER, false);
            MutableDoubleVector<LengthUnit> plus = null;
            MutableDoubleVector<LengthUnit> minus = null;
            try
            {
                if (fvAbsolute instanceof DoubleVector.Abs.Dense)
                {
                    plus =
                            DoubleVector.plus((DoubleVector.Abs.Dense<LengthUnit>) fvAbsolute,
                                    (DoubleVector.Rel.Dense<LengthUnit>) fvRelative);
                }
                else if (fvAbsolute instanceof DoubleVector.Abs.Sparse)
                {
                    plus =
                            DoubleVector.plus((DoubleVector.Abs.Sparse<LengthUnit>) fvAbsolute,
                                    (DoubleVector.Rel.Sparse<LengthUnit>) fvRelative);
                }
                else
                {
                    fail("Error in test: cannot figure out type of fvAbsolute");
                }
                fail("Adding DoubleVectors of unequal length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            try
            {
                if (fvAbsolute instanceof DoubleVector.Abs.Dense)
                {
                    minus =
                            DoubleVector.minus((DoubleVector.Abs.Dense<LengthUnit>) fvAbsolute,
                                    (DoubleVector.Rel.Dense<LengthUnit>) fvRelative);
                }
                else if (fvAbsolute instanceof DoubleVector.Abs.Sparse)
                {
                    minus =
                            DoubleVector.minus((DoubleVector.Abs.Sparse<LengthUnit>) fvAbsolute,
                                    (DoubleVector.Rel.Sparse<LengthUnit>) fvRelative);
                }
                else
                {
                    fail("Error in test: cannot figure out type of fvAbsolute");
                }
                fail("Subtracting DoubleVectors of unequal length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            double[] in5 = {1, 2, 3, 4, 5, 6, 7, 8, 9 };
            fvRelative = createDoubleVector(in5, LengthUnit.METER, false);
            try
            {
                if (fvAbsolute instanceof DoubleVector.Abs.Dense)
                {
                    plus =
                            DoubleVector.plus((DoubleVector.Abs.Dense<LengthUnit>) fvAbsolute,
                                    (DoubleVector.Rel.Dense<LengthUnit>) fvRelative);
                }
                else if (fvAbsolute instanceof DoubleVector.Abs.Sparse)
                {
                    plus =
                            DoubleVector.plus((DoubleVector.Abs.Sparse<LengthUnit>) fvAbsolute,
                                    (DoubleVector.Rel.Sparse<LengthUnit>) fvRelative);
                }
                else
                {
                    fail("Error in test: cannot figure out type of fvAbsolute");
                }
                if (fvAbsolute instanceof DoubleVector.Abs.Dense)
                {
                    minus =
                            DoubleVector.minus((DoubleVector.Abs.Dense<LengthUnit>) fvAbsolute,
                                    (DoubleVector.Rel.Dense<LengthUnit>) fvRelative);
                }
                else if (fvAbsolute instanceof DoubleVector.Abs.Sparse)
                {
                    minus =
                            DoubleVector.minus((DoubleVector.Abs.Sparse<LengthUnit>) fvAbsolute,
                                    (DoubleVector.Rel.Sparse<LengthUnit>) fvRelative);
                }
                else
                {
                    fail("Error in test: cannot figure out type of fvAbsolute");
                }
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
                {
                    assertEquals("value of element should be sum of contributing elements", in3[i] + in5[i], plus.getSI(i),
                            0.00001);
                }
                for (int i = 0; i < in3.length; i++)
                {
                    assertEquals("value of element should be sum of contributing elements", in3[i] - in5[i], minus.getSI(i),
                            0.00001);
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("result should be Absolute", plus instanceof Absolute);
            assertTrue("result should be Absolute", minus instanceof Absolute);
            if (fvAbsolute instanceof DoubleVector.Abs.Dense)
            {
                assertTrue("result should be Dense", plus instanceof MutableDoubleVector.Abs.Dense);
                assertTrue("result should be Dense", minus instanceof MutableDoubleVector.Abs.Dense);
            }
            else if (fvAbsolute instanceof DoubleVector.Abs.Sparse)
            {
                assertTrue("result should be Sparse", plus instanceof MutableDoubleVector.Abs.Sparse);
                assertTrue("result should be Sparse", minus instanceof MutableDoubleVector.Abs.Sparse);
            }
            else
            {
                fail("fv1 neither Dense nor Sparse");
            }

            try
            {
                double[] in6 = {1, 2, 3 };
                MutableDoubleVector<LengthUnit> original = createDoubleVector(in6, LengthUnit.METER, absolute).mutable();
                MutableDoubleVector<LengthUnit> duplicate = original.copy();
                assertTrue("Original should be equal to duplicate", original.equals(duplicate));
                assertTrue("Duplicate should be equal to original", duplicate.equals(original));
                original.setSI(0, 123.456f);
                assertFalse("Original should now differ from duplicate", original.equals(duplicate));
                assertFalse("Duplicate should now differ from original", duplicate.equals(original));

            }
            catch (ValueException exception)
            {
                fail("Unexpected ValueException");
            }
        }
        else
        // Relative
        {
            DoubleVector<LengthUnit> fv1 = createDoubleVector(in3, LengthUnit.METER, false);
            double[] in4 = {1, 2, 3, 4 };
            DoubleVector<LengthUnit> fv2 = createDoubleVector(in4, LengthUnit.METER, false);
            MutableDoubleVector<SIUnit> product = null;
            try
            {
                if (fv1 instanceof DoubleVector.Rel.Dense)
                {
                    product = DoubleVector.times((DoubleVector.Rel.Dense<?>) fv1, (DoubleVector.Rel.Dense<?>) fv2);
                }
                else if (fv1 instanceof DoubleVector.Rel.Sparse)
                {
                    product = DoubleVector.times((DoubleVector.Rel.Sparse<?>) fv1, (DoubleVector.Rel.Sparse<?>) fv2);
                }
                fail("Multiplying DoubleVectors of unequal length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            double[] in5 = {1, 2, 3, 4, 5, 6, 7, 8, 9 };
            fv2 = createDoubleVector(in5, LengthUnit.METER, false);
            try
            {
                if (fv1 instanceof DoubleVector.Rel.Dense)
                {
                    product = DoubleVector.times((DoubleVector.Rel.Dense<?>) fv1, (DoubleVector.Rel.Dense<?>) fv2);
                }
                else if (fv1 instanceof DoubleVector.Rel.Sparse)
                {
                    product = DoubleVector.times((DoubleVector.Rel.Sparse<?>) fv1, (DoubleVector.Rel.Sparse<?>) fv2);
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("multiply should not return null", null != product);
            assertEquals("size of result should be " + in5.length, in5.length, product.size());
            try
            {
                for (int i = 0; i < in3.length; i++)
                {
                    assertEquals("value of element should be sum of contributing elements", in3[i] * in5[i], product.getSI(i),
                            0.00001);
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("result should be Relative", product instanceof Relative);
            if (fv1 instanceof DoubleVector.Abs.Dense || fv1 instanceof DoubleVector.Rel.Dense)
            {
                assertTrue("result should be Dense", product instanceof MutableDoubleVector.Abs.Dense
                        || product instanceof MutableDoubleVector.Rel.Dense);
            }
            else if (fv1 instanceof DoubleVector.Abs.Sparse || fv1 instanceof DoubleVector.Rel.Sparse)
            {
                assertTrue("result should be Sparse", product instanceof MutableDoubleVector.Abs.Sparse
                        || product instanceof MutableDoubleVector.Rel.Sparse);
            }
            else
            {
                fail("fv1 neither Dense nor Sparse");
            }
            // System.out.println("Result of multiply has unit " + multiply);
            assertEquals("Result of multiplication should be in square meters", "m2", product.getUnit()
                    .getSICoefficientsString());

            MutableDoubleVector<SIUnit> multiplyReverse = null;
            try
            {
                if (fv1 instanceof DoubleVector.Rel.Dense)
                {
                    multiplyReverse =
                            DoubleVector.times((DoubleVector.Rel.Dense<?>) fv2, (DoubleVector.Rel.Dense<?>) fv1);
                }
                else if (fv1 instanceof DoubleVector.Rel.Sparse)
                {
                    multiplyReverse =
                            DoubleVector.times((DoubleVector.Rel.Sparse<?>) fv2, (DoubleVector.Rel.Sparse<?>) fv1);
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("result should be Absolute", multiplyReverse instanceof Relative);
            // System.out.println("plus is       " + multiply);
            // System.out.println("plusReverse is" + multiplyReverse);
            assertTrue("result of a * b should be equal to result of b * a", product.equals(multiplyReverse));
            try
            {
                double[] in6 = {1, 2, 3 };
                DoubleVector<LengthUnit> original = createDoubleVector(in6, LengthUnit.METER, absolute);
                MutableDoubleVector<LengthUnit> duplicate = original.mutable();
                assertTrue("Original should be equal to duplicate", original.equals(duplicate));
                assertTrue("Duplicate should be equal to original", duplicate.equals(original));
                duplicate.setSI(0, 123.456f);
                assertFalse("Original should now differ from duplicate", original.equals(duplicate));
                assertFalse("Duplicate should now differ from original", duplicate.equals(original));

            }
            catch (ValueException exception)
            {
                fail("Unexpected ValueException");
            }
        }
        fv = createDoubleVector(in, u, absolute);
        double[] factorsTooShort = {10, 20, 30, 40, 50, 60 };
        double[] factorsCorrectLength = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120 };
        if (absolute)
        {
            MutableDoubleVector<LengthUnit> fv2 = null;
            try
            {
                if (fv instanceof DoubleVector.Abs.Dense)
                {
                    DoubleVector.times((DoubleVector.Abs.Dense<LengthUnit>) fv, factorsTooShort);
                }
                else if (fv instanceof DoubleVector.Abs.Sparse)
                {
                    DoubleVector.times((DoubleVector.Abs.Sparse<LengthUnit>) fv, factorsTooShort);
                }
                fail("Multiplication array of wrong length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            try
            {
                if (fv instanceof DoubleVector.Abs.Dense)
                {
                    fv2 = DoubleVector.times((DoubleVector.Abs.Dense<LengthUnit>) fv, factorsCorrectLength);
                }
                else if (fv instanceof DoubleVector.Abs.Sparse)
                {
                    fv2 = DoubleVector.times((DoubleVector.Abs.Sparse<LengthUnit>) fv, factorsCorrectLength);
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            try
            {
                for (int i = 0; i < in.length; i++)
                {
                    assertEquals("values in fv2 should be product of contributing values", in[i] * (12 * 0.0254)
                            * factorsCorrectLength[i], fv2.getSI(i), 0.0001);
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertEquals("type of result should be input type", fv.getUnit(), fv2.getUnit());
        }
        else
        // Relative
        {
            MutableDoubleVector<LengthUnit> fv2 = null;
            try
            {
                if (fv instanceof DoubleVector.Rel.Dense)
                {
                    DoubleVector.times((DoubleVector.Rel.Dense<LengthUnit>) fv, factorsTooShort);
                }
                else if (fv instanceof DoubleVector.Rel.Sparse)
                {
                    DoubleVector.times((DoubleVector.Rel.Sparse<LengthUnit>) fv, factorsTooShort);
                }
                fail("Multiplication array of wrong length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            try
            {
                if (fv instanceof DoubleVector.Rel.Dense)
                {
                    fv2 = DoubleVector.times((DoubleVector.Rel.Dense<LengthUnit>) fv, factorsCorrectLength);
                }
                else if (fv instanceof DoubleVector.Rel.Sparse)
                {
                    fv2 = DoubleVector.times((DoubleVector.Rel.Sparse<LengthUnit>) fv, factorsCorrectLength);
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            try
            {
                for (int i = 0; i < in.length; i++)
                {
                    assertEquals("values in fv2 should be product of contributing values", in[i] * (12 * 0.0254)
                            * factorsCorrectLength[i], fv2.getSI(i), 0.0001);
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertEquals("type of result should be input type", fv.getUnit(), fv2.getUnit());
        }
        if (fv instanceof DoubleVector.Abs.Dense || fv instanceof DoubleVector.Rel.Dense)
        {
            MutableDoubleVector<LengthUnit> fv2 = null;
            if (fv instanceof Absolute)
            {
                fv2 = DoubleVector.denseToSparse((DoubleVector.Abs.Dense<LengthUnit>) fv);
            }
            else
            {
                fv2 = DoubleVector.denseToSparse((DoubleVector.Rel.Dense<LengthUnit>) fv);
            }
            // System.out.println("fv:  " + fv);
            // System.out.println("fv2: " + fv2);
            assertTrue("dense version is equal to sparse version", fv.equals(fv2));
            assertEquals("unit should be same", fv.getUnit(), fv2.getUnit());
            try
            {
                for (int i = 0; i < fv.size(); i++)
                {
                    assertEquals("Values should be equal", fv.getSI(i), fv2.getSI(i), 0.0001);
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
        }
        else
        // SparseData
        {
            MutableDoubleVector<LengthUnit> fv2 = null;
            if (fv instanceof Absolute)
            {
                fv2 = DoubleVector.sparseToDense((DoubleVector.Abs.Sparse<LengthUnit>) fv);
            }
            else
            {
                fv2 = DoubleVector.sparseToDense((DoubleVector.Rel.Sparse<LengthUnit>) fv);
            }
            assertTrue("dense version is  equal to sparse version", fv.equals(fv2));
            assertEquals("unit should be same", fv.getUnit(), fv2.getUnit());
            try
            {
                for (int i = 0; i < fv.size(); i++)
                {
                    assertEquals("Values should be equal", fv.getSI(i), fv2.getSI(i), 0.0001);
                }
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
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <p>
     * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
     * <p>
     * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
     * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
     * following conditions are met:
     * <ul>
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
     * disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
     * disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
     * promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
     * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
     * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
     * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or services;
     * loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in
     * contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this
     * software, even if advised of the possibility of such damage.
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
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <p>
     * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
     * <p>
     * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
     * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
     * following conditions are met:
     * <ul>
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
     * disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
     * disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
     * promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
     * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
     * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
     * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or services;
     * loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in
     * contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this
     * software, even if advised of the possibility of such damage.
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
         * @param function DoubleToDouble encapsulating function that converts one value in inputValues to the corresponding
         *            value in resultValues
         */
        public static void tester(final double[] inputValues, final String operation, final double[] resultValues,
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
         * Function that takes a double value and returns a double value.
         * @param in double value
         * @return double value
         */
        public abstract double function(double in);
    }

    /**
     * Test DoubleVectorAbs and DoubleVectorRel creators that take an array of DoubleScalar as argument.
     */
    @Test
    public final void doubleVectorOneArg()
    {
        doubleVectorOneArg(true); // test absolute version
        doubleVectorOneArg(false); // test relative version
    }

    /**
     * Test the DoubleVectorAbs and DoubleVectorRel that takes a DoubleScalar*<U>[] as argument.
     * @param absolute Boolean;
     */
    @SuppressWarnings("unchecked")
    private void doubleVectorOneArg(final Boolean absolute)
    {
        DoubleVector<LengthUnit> fv = null;
        DoubleScalar.Abs<LengthUnit>[] inAbs = new DoubleScalar.Abs[0];
        DoubleScalar.Rel<LengthUnit>[] inRel = new DoubleScalar.Rel[0];
        try
        {
            if (absolute)
            {
                fv = createDoubleVectorAbs(inAbs);
            }
            else
            {
                fv = createDoubleVectorRel(inRel);
            }
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        inAbs = new DoubleScalar.Abs[1];
        inAbs[0] = new DoubleScalar.Abs<LengthUnit>(123.456f, LengthUnit.FOOT);
        inRel = new DoubleScalar.Rel[1];
        inRel[0] = new DoubleScalar.Rel<LengthUnit>(123.456f, LengthUnit.FOOT);
        try
        {
            if (absolute)
            {
                fv = createDoubleVectorAbs(inAbs);
            }
            else
            {
                fv = createDoubleVectorRel(inRel);
            }
        }
        catch (ValueException exception)
        {
            fail("Should NOT have thrown an exception");
        }
        double[] out = fv.getValuesInUnit();
        assertTrue("Result of getValuesInUnit should not be null", null != out);
        assertEquals("Array of values should have length 1", 1, out.length);
        assertEquals("Element in array should have the expected value", 123.456f, out[0], 0.001);
    }

    /**
     * Test adding and subtracting DoubleVectorRel.
     */
    @Test
    public final void relRel()
    {
        double[] in1 = {10f, 20f, 30f, 40f };
        double[] in2 = {110f, 120f, 130f, 140f };
        MassUnit u = MassUnit.POUND;
        DoubleVector<MassUnit> fv1 = createDoubleVectorRel(in1, u);
        DoubleVector<MassUnit> fv2 = createDoubleVectorRel(in2, u);
        MutableDoubleVector<MassUnit> sum = null;
        try
        {
            if (fv1 instanceof DoubleVector.Rel.Dense)
            {
                sum = DoubleVector.plus((DoubleVector.Rel.Dense<MassUnit>) fv1, (DoubleVector.Rel.Dense<MassUnit>) fv2);
            }
            else if (fv1 instanceof DoubleVector.Rel.Sparse)
            {
                sum =
                        DoubleVector.plus((DoubleVector.Rel.Sparse<MassUnit>) fv1,
                                (DoubleVector.Rel.Sparse<MassUnit>) fv2);
            }
            else
            {
                fail("Error in test: unhandled type of DoubleVector");
            }
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
        {
            assertEquals("Each element should equal the sum of the contributing elements", in1[i] + in2[i], sumValues[i],
                    0.0001);
        }
        MutableDoubleVector<MassUnit> difference = null;
        try
        {
            if (fv1 instanceof DoubleVector.Rel.Dense)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Rel.Dense<MassUnit>) fv1,
                                (DoubleVector.Rel.Dense<MassUnit>) fv2);
            }
            else if (fv1 instanceof DoubleVector.Rel.Sparse)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Rel.Sparse<MassUnit>) fv1,
                                (DoubleVector.Rel.Sparse<MassUnit>) fv2);
            }
            else
            {
                fail("Error in test: unhandled type of DoubleVector");
            }
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
        {
            assertEquals("Each element should equal the difference of the contributing elements", in1[i] - in2[i],
                    differenceValues[i], 0.0001);
        }
        double[] in3 = {110f, 120f, 130f };
        DoubleVector<MassUnit> fv3 = createDoubleVectorRel(in3, u);
        try
        {
            if (fv1 instanceof DoubleVector.Rel.Dense)
            {
                sum = DoubleVector.plus((DoubleVector.Rel.Dense<MassUnit>) fv1, (DoubleVector.Rel.Dense<MassUnit>) fv3);
            }
            else if (fv1 instanceof DoubleVector.Rel.Sparse)
            {
                sum =
                        DoubleVector.plus((DoubleVector.Rel.Sparse<MassUnit>) fv1,
                                (DoubleVector.Rel.Sparse<MassUnit>) fv3);
            }
            else
            {
                fail("Error in test: unhandled type of DoubleVector");
            }
            fail("Adding DoubleVectors of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            if (fv1 instanceof DoubleVector.Rel.Dense)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Rel.Dense<MassUnit>) fv1,
                                (DoubleVector.Rel.Dense<MassUnit>) fv3);
            }
            else if (fv1 instanceof DoubleVector.Rel.Sparse)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Rel.Sparse<MassUnit>) fv1,
                                (DoubleVector.Rel.Sparse<MassUnit>) fv3);
            }
            else
            {
                fail("Error in test: unhandled type of DoubleVector");
            }
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
            if (fv1 instanceof DoubleVector.Rel.Dense)
            {
                sum = DoubleVector.plus((DoubleVector.Rel.Dense<MassUnit>) fv1, (DoubleVector.Rel.Dense<MassUnit>) fv2);
            }
            else if (fv1 instanceof DoubleVector.Rel.Sparse)
            {
                sum =
                        DoubleVector.plus((DoubleVector.Rel.Sparse<MassUnit>) fv1,
                                (DoubleVector.Rel.Sparse<MassUnit>) fv2);
            }
            else
            {
                fail("Error in test: unhandled type of DoubleVector");
            }
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
            assertEquals("Each element should equal the weighted sum of the contributing elements", in1[i] * 0.45359 + in2[i]
                    * 0.028350, sumValues[i] * 0.45359, 0.0001);
        }
        fv2 = createDoubleVectorRel(in2, u2);
        try
        {
            if (fv1 instanceof DoubleVector.Rel.Dense)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Rel.Dense<MassUnit>) fv1,
                                (DoubleVector.Rel.Dense<MassUnit>) fv2);
            }
            else if (fv1 instanceof DoubleVector.Rel.Sparse)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Rel.Sparse<MassUnit>) fv1,
                                (DoubleVector.Rel.Sparse<MassUnit>) fv2);
            }
            else
            {
                fail("Error in test: unhandled type of DoubleVector");
            }
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
            assertEquals("Each element should equal the weighted difference of the contributing elements", in1[i] * 0.45359
                    - in2[i] * 0.028350, differenceValues[i] * 0.45359, 0.0001);
        }
    }

    /**
     * Test adding and subtracting DoubleVectorAbs.
     */
    @Test
    public final void absAbs()
    {
        double[] in1 = {10f, 20f, 30f, 40f };
        double[] in2 = {110f, 220f, 330f, 440f };
        MassUnit u = MassUnit.POUND;
        DoubleVector<MassUnit> fv1 = createDoubleVectorAbs(in1, u);
        // System.out.println("fv1: " + fv1);
        DoubleVector<MassUnit> fv2 = createDoubleVectorAbs(in2, u);
        // System.out.println("fv2: " + fv2);
        MutableDoubleVector<MassUnit> difference = null;
        try
        {
            if (fv1 instanceof DoubleVector.Abs.Dense)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Abs.Dense<MassUnit>) fv1,
                                (DoubleVector.Abs.Dense<MassUnit>) fv2);
            }
            else if (fv1 instanceof DoubleVector.Abs.Sparse)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Abs.Sparse<MassUnit>) fv1,
                                (DoubleVector.Abs.Sparse<MassUnit>) fv2);
            }
            else
            {
                fail("Error in test: unhandled type of DoubleVector");
            }
        }
        catch (ValueException exception1)
        {
            fail("Should be able to subtract DoubleVectorAbs from DoubleVectorAbs of same size");
        }
        // System.out.println("difference: " + difference);
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 4, difference.size());
        assertEquals("Type of result should be type of inputs", u, difference.getUnit());
        double[] differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
        {
            assertEquals("Each element should equal the difference of the contributing elements", in1[i] - in2[i],
                    differenceValues[i], 0.0001);
        }
        double[] in3 = {110f, 120f, 130f };
        DoubleVector<MassUnit> fv3 = createDoubleVectorAbs(in3, u);
        try
        {
            if (fv1 instanceof DoubleVector.Abs.Dense)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Abs.Dense<MassUnit>) fv1,
                                (DoubleVector.Abs.Dense<MassUnit>) fv3);
            }
            else if (fv1 instanceof DoubleVector.Abs.Sparse)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Abs.Sparse<MassUnit>) fv1,
                                (DoubleVector.Abs.Sparse<MassUnit>) fv3);
            }
            else
            {
                fail("Error in test: unhandled type of DoubleVector");
            }
            fail("Subtracting DoubleVectors of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        MassUnit u2 = MassUnit.OUNCE;
        fv2 = createDoubleVectorAbs(in2, u2);
        try
        {
            if (fv1 instanceof DoubleVector.Abs.Dense)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Abs.Dense<MassUnit>) fv1,
                                (DoubleVector.Abs.Dense<MassUnit>) fv2);
            }
            else if (fv1 instanceof DoubleVector.Abs.Sparse)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Abs.Sparse<MassUnit>) fv1,
                                (DoubleVector.Abs.Sparse<MassUnit>) fv2);
            }
            else
            {
                fail("Error in test: unhandled type of DoubleVector");
            }
        }
        catch (ValueException exception)
        {
            fail("Should be able to subtract DoubleVectorAbs from DoubleVectorAbs of same size and compatible type");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 4, difference.size());
        assertEquals("Type of result should be type of first input", u, difference.getUnit());
        assertFalse("Type of result should be different of type of second input", u2 == difference.getUnit());
        differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
        {
            assertEquals("Each element should equal the weighted difference of the contributing elements", in1[i] * 0.45359
                    - in2[i] * 0.028350, differenceValues[i] * 0.45359, 0.001);
        }
        try
        {
            if (fv1 instanceof DoubleVector.Abs.Dense)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Abs.Dense<MassUnit>) fv2,
                                (DoubleVector.Abs.Dense<MassUnit>) fv1);
            }
            else if (fv1 instanceof DoubleVector.Abs.Sparse)
            {
                difference =
                        DoubleVector.minus((DoubleVector.Abs.Sparse<MassUnit>) fv2,
                                (DoubleVector.Abs.Sparse<MassUnit>) fv1);
            }
            else
            {
                fail("Error in test: unhandled type of DoubleVector");
            }
        }
        catch (ValueException exception)
        {
            fail("Should be able to add DoubleVectorAbs to DoubleVectorAbs of same size and compatible type");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 4, difference.size());
        assertEquals("Type of result should be type of first input", u2, difference.getUnit());
        assertFalse("Type of result should be different of type of second input", u == difference.getUnit());
        differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
        {
            assertEquals("Each element should equal the weighted difference of the contributing elements", in2[i] * 0.028350
                    - in1[i] * 0.45359, differenceValues[i] * 0.028350, 0.001);
        }
        LengthUnit u4 = LengthUnit.INCH;
        DoubleVector<LengthUnit> fv4 = createDoubleVectorAbs(in1, u4);
        ForceUnit u5 = ForceUnit.POUND_FORCE;
        DoubleVector<ForceUnit> fv5 = createDoubleVectorAbs(in2, u5);
        Unit<EnergyUnit> resultUnit = EnergyUnit.CALORIE_IT;
        // System.out.println("fv4:             " + fv4);
        // System.out.println("fv4:             " + fv4.toString(LengthUnit.METER));
        // System.out.println("fv5:            " + fv5);
        // System.out.println("fv5:            " + fv5.toString(ForceUnit.NEWTON));
        MutableDoubleVector<SIUnit> product = null;
        try
        {
            if (fv4 instanceof DoubleVector.Abs.Dense)
            {
                product = DoubleVector.times((DoubleVector.Abs.Dense<?>) fv4, (DoubleVector.Abs.Dense<?>) fv5);
            }
            else if (fv4 instanceof DoubleVector.Abs.Sparse)
            {
                product = DoubleVector.times((DoubleVector.Abs.Sparse<?>) fv4, (DoubleVector.Abs.Sparse<?>) fv5);
            }
            else
            {
                fail("Error in test: unhandled type");
            }
        }
        catch (ValueException exception)
        {
            fail("Should be able to multiply DoubleVectorAbs with DoubleVectorAbs of same size");
        }
        assertTrue("Result should not be null", null != product);
        assertEquals("Size of result should be size of inputs", 4, product.size());
        // System.out.println("product: " + product);
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
     * Test the DoubleVector.newDenseRelInstance method that takes a DoubleScalarRel[] as argument.
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void doubleVectorRel2()
    {
        DoubleVector<LengthUnit> fsa = null;
        DoubleScalar.Rel<LengthUnit>[] in = new DoubleScalar.Rel[0];
        try
        {
            fsa = createDoubleVectorRel(in);
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        in = new DoubleScalar.Rel[1];
        in[0] = new DoubleScalar.Rel<LengthUnit>(123.456f, LengthUnit.FOOT);
        try
        {
            fsa = createDoubleVectorRel(in);
        }
        catch (ValueException exception)
        {
            fail("Should NOT have thrown an exception");
        }
        double[] out = fsa.getValuesInUnit();
        assertTrue("Result of getValuesInUnit should not be null", null != out);
        assertEquals("Array of values should have length 1", 1, out.length);
        assertEquals("Element in array should have the expected value", 123.456f, out[0], 0.001);
    }

    /**
     * Create a DoubleVectorAbs or a DoubleVectorRel from an array of double values and Unit.
     * @param in double[] with values
     * @param u Unit; type for the new DoubleVector
     * @param absolute Boolean; true to create a DoubleVectorAbs; false to create a DoubleVectorRel
     * @return DoubleVector
     * @param <U> Unit; the unit
     */
    private <U extends Unit<U>> DoubleVector<U> createDoubleVector(final double[] in, final U u, final boolean absolute)
    {
        if (absolute)
        {
            return createDoubleVectorAbs(in, u);
        }
        else
        {
            return createDoubleVectorRel(in, u);
        }
    }

    /**
     * Create a new DoubleVectorAbs from an array of double values and Unit.
     * @param in double[] with values
     * @param u Unit; type for the new DoubleVectorAbs
     * @return DoubleVector.Abs
     * @param <U> Unit; the unit
     */
    protected abstract <U extends Unit<U>> DoubleVector.Abs<U> createDoubleVectorAbs(double[] in, U u);

    /**
     * Create a new DoubleVectorAbs from an array of DoubleScalarAbs values.
     * @param in DoubleScalar.Abs[]; the values
     * @return DoubleVector.Abs
     * @throws ValueException when the array is empty
     * @param <U> Unit; the unit
     */
    protected abstract <U extends Unit<U>> DoubleVector.Abs<U> createDoubleVectorAbs(DoubleScalar.Abs<U>[] in)
            throws ValueException;

    /**
     * Create a new DoubleVector.*.Rel from an array of double values and Unit.
     * @param in double[] with values
     * @param u Unit; type for the new DoubleVectorRel
     * @return DoubleVector.Rel
     * @param <U> Unit; the unit
     */
    protected abstract <U extends Unit<U>> DoubleVector.Rel<U> createDoubleVectorRel(double[] in, U u);

    /**
     * Create a new DoubleVector.*.Rel from an array of DoubleScalarRel values.
     * @param in DoubleScalar.Abs[]; the values
     * @return DoubleVector.Rel
     * @throws ValueException when the array is empty
     * @param <U> Unit; the unit
     */
    protected abstract <U extends Unit<U>> DoubleVector.Rel<U> createDoubleVectorRel(DoubleScalar.Rel<U>[] in)
            throws ValueException;

}

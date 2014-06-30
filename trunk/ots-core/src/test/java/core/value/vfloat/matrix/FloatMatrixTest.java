package core.value.vfloat.matrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.junit.Test;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.Dense;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.Sparse;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.matrix.FloatMatrix;
import org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixAbs;
import org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixAbsDense;
import org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixAbsSparse;
import org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixRel;
import org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixRelDense;
import org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixRelSparse;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalarAbs;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalarRel;

import cern.colt.matrix.tfloat.FloatMatrix2D;

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
public abstract class FloatMatrixTest
{
    /**
     * Test FloatMatrixAbs and FloatMatrixRel creators that take a float[][] and a Unit as arguments
     */
    @Test
    public void floatMatrixTwoArgs()
    {
        floatMatrixTwoArgs(true); // test absolute version
        floatMatrixTwoArgs(false); // rest relative version
    }

    /**
     * Test the FloatMatrixAbs that takes a float[][] and a Unit as arguments and some methods.
     */
    private void floatMatrixTwoArgs(Boolean absolute)
    {
        float[][] in = new float[12][3];
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                in[i][j] = i / 3f + j / 5f;
        LengthUnit u = LengthUnit.FOOT;
        FloatMatrix<LengthUnit> fv = safeCreateFloatMatrix(in, u, absolute);
        assertEquals("FloatMatrix should have 12 rows", 12, fv.rows());
        assertEquals("FloatMatrix should have 3 columns", 3, fv.columns());
        float[][] out = fv.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
            {
                assertEquals("Values in FloatMatrix in unit should be equal to input values", in[i][j], out[i][j],
                        0.0001);
                try
                {
                    assertEquals("Values in FloatMatrix in unit should be equal to input values", in[i][j],
                            fv.getInUnit(i, j), 0.0001);
                    assertEquals("Values in FloatMatrix in unit should be equal to input values", in[i][j],
                            fv.getSI(i, j) / (12 * 0.0254), 0.0001);
                    assertEquals("Values in FloatMatrix in unit should be equal to input values", in[i][j],
                            fv.getInUnit(i, j, LengthUnit.MILE) * 1609 / (12 * 0.0254), 0.001);
                }
                catch (ValueException exception)
                {
                    fail("Get should not throw exceptions for legal values of the index");
                }
            }
        String output = fv.toString(LengthUnit.MILLIMETER);
        String[] lines = output.split("[\n]");
        assertEquals("Number of lines should be number of rows + 1", in.length + 1, lines.length);
        assertEquals("first line should contain unit in brackers", "[mm]", lines[0]);
        for (int i = 1; i < lines.length; i++)
        {
            String[] fields = lines[i].trim().split("[ ]+");
            assertEquals("Number of fields should be number of columns", fv.columns(), fields.length);
            for (int j = 0; j < fields.length; j++)
            {
                float expectedValue = (float) (in[i - 1][j] * (12 * 0.0254) * 1000);
                String expected;
                    expected = String.format(Locale.US, "%9.3g", expectedValue);
                expected = expected.trim();
                assertEquals("Field " + j + " should contain \"" + expected + "\"", expected, fields[j]);
            }
        }
        output = fv.toString();
        lines = output.split("[\n]");
        assertEquals("Number of lines should be number of rows + 1", in.length + 1, lines.length);
        assertEquals("first line should contain unit in brackers", "[ft]", lines[0]);
        for (int i = 1; i < lines.length; i++)
        {
            String[] fields = lines[i].trim().split("[ ]+");
            assertEquals("Number of fields should be number of columns", fv.columns(), fields.length);
            for (int j = 0; j < fields.length; j++)
            {
                float expectedValue = in[i - 1][j];
                String expected = String.format(Locale.US, "%9.3g", expectedValue);
                expected = expected.trim();
                assertEquals("Field " + i + " should contain \"" + expected + "\"", expected, fields[j]);
            }
        }

        FloatMatrix2D fm2d;
        if (fv instanceof FloatMatrixAbsDense)
            fm2d = ((FloatMatrixAbsDense<LengthUnit>) fv).getColtDenseFloatMatrix2D();
        else if (fv instanceof FloatMatrixRelDense)
            fm2d = ((FloatMatrixRelDense<LengthUnit>) fv).getColtDenseFloatMatrix2D();
        else if (fv instanceof FloatMatrixAbsSparse)
            fm2d = ((FloatMatrixAbsSparse<LengthUnit>) fv).getColtSparseFloatMatrix2D();
        else if (fv instanceof FloatMatrixRelSparse)
            fm2d = ((FloatMatrixRelSparse<LengthUnit>) fv).getColtSparseFloatMatrix2D();
        else
            throw new Error("Matrix neither Dense nor Sparse");
        assertTrue("ColtMatrix1D should not be null", null != fm2d);
        assertEquals("Size of Colt matrix should be number of cells", in.length * in[0].length, fm2d.size());
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Contents of Colt matrix should be SI equivalent of input", in[i][j] * (12 * 0.0254),
                        fm2d.getQuick(i, j), 0.0001);
        fm2d = fv.getMatrixSI();
        assertTrue("MatrixSI should not be null", null != fm2d);
        assertEquals("Size of MatrixSI should be number of cells", in.length * in[0].length, fm2d.size());
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Contents of MatrixSI should be SI equivalent of input", in[i][j] * (12 * 0.0254),
                        fm2d.getQuick(i, j), 0.0001);
        float[][] valuesInUnit = fv.getValuesInUnit();
        assertTrue("valuesInUnit should not be null", null != valuesInUnit);
        assertEquals("Size of valuesInUnit should be size of input array", in.length, valuesInUnit.length);
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Contents of valuesInUnit should be equal to input", in[i][j], valuesInUnit[i][j], 0.0001);
        LengthUnit outputUnit = LengthUnit.DEKAMETER;
        float[][] valuesInOtherUnit = fv.getValuesInUnit(outputUnit);
        assertTrue("valuesInUnit should not be null", null != valuesInOtherUnit);
        assertEquals("Size of valuesInUnit should be size of input array", in.length, valuesInOtherUnit.length);
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Contents of valuesInUnit should be equal to input", in[i][j] * (12 * 0.0254) / 10,
                        valuesInOtherUnit[i][j], 0.0001);
        try
        {
            fv.getInUnit(-1, 0);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getInUnit(in.length, 0);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getInUnit(0, -1);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getInUnit(0, in[0].length);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getSI(-1, 0);
            fail("Using a bad index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getSI(in.length, 0);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getSI(0, -1);
            fail("Using a bad index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getSI(0, in[0].length);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getInUnit(-1, 0, LengthUnit.MILE);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getInUnit(in.length, 0, LengthUnit.MILE);
            fail("Using an index that is too big should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getInUnit(0, -1, LengthUnit.MILE);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.getInUnit(0, in[0].length, LengthUnit.MILE);
            fail("Using an index that is too big should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fv.setSI(-1, 0, 12345f);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        try
        {
            fv.setSI(in.length, 0, 12345f);
            fail("Using an index that is too big should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        try
        {
            fv.setSI(0, -1, 12345f);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        try
        {
            fv.setSI(0, in[0].length, 12345f);
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
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Values in FloatMatrix should be equivalent values in meters", in[i][j], out[i][j]
                        / (12 * 0.0254), 0.0001);
        LengthUnit uOut = fv.getUnit();
        assertEquals("Stored unit should be provided unit", u, uOut);
        FloatMatrix<LengthUnit> copy = null;
        if (fv instanceof FloatMatrixAbs<?>)
        {
            copy = ((FloatMatrixAbs<LengthUnit>) fv).copy();
        }
        else if (fv instanceof FloatMatrixRel<?>)
        {
            copy = ((FloatMatrixRel<LengthUnit>) fv).copy();
        }
        else
            fail("Matrix neither Absolute nor Relative");

        assertEquals("copy should have 12 rows", 12, copy.rows());
        assertEquals("copy should have 3 columns", 3, copy.columns());
        float[][] copyOut = copy.getValuesSI();
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Values in copy of FloatMatrix should be equivalent values in meters", in[i][j],
                        copyOut[i][j] / (12 * 0.0254), 0.0001);
        copyOut = fv.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Values in copy of FloatMatrix in unit should be equal to input values", in[i][j],
                        copyOut[i][j], 0.0001);
        try
        {
            copy.setSI(0, 0, 12345f);
            assertEquals("value should be altered", 12345f, copy.getSI(0, 0), 0.01);
            assertEquals("original value should not be altered", out[0][0], fv.getSI(0, 0), 0.001);
            FloatScalar<LengthUnit> value = copy.get(1, 0);
            assertTrue("value cannot be null", null != value);
            assertEquals("value should be same as SI value", copy.getSI(1, 0), value.getValueSI(), 0.0001);
            copy.set(2, 0, value);
            assertEquals("value should be same as SI value", copy.getSI(2, 0), value.getValueSI(), 0.0001);
        }
        catch (ValueException exception)
        {
            fail("set*/get* should not throw ValueException for valid index and correctly typed value");
        }
        try
        {
            copy.setInUnit(1, 0, 321, LengthUnit.HECTOMETER);
            assertEquals("321 hectometer is 32100m", copy.getSI(1, 0), 32100, 0.001);
        }
        catch (ValueException exception)
        {
            fail("Legal index should not throw exception");
        }
        float sum = 0;
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                sum += in[i][j];
        sum *= (12 * 0.0254); // convert to meters
        assertEquals("zsum should be sum of the values", sum, fv.zSum(), 0.001);
        try
        {
            fv.normalize();
            for (int i = 0; i < in.length; i++)
                for (int j = 0; j < in[i].length; j++)
                    assertEquals("Unexpected normalized value", in[i][j] * (12 * 0.0254) / sum, fv.getSI(i, j), 0.0001);
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertEquals("Cardinality should be 35", 35, fv.cardinality());
        float[][] in2 = {{1f, -1f, 0f}};
        fv = safeCreateFloatMatrix(in2, u, absolute);
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
            fv.setSI(0, 0, 0);
            assertEquals("Cardinality should be 1", 1, fv.cardinality());
            fv.setSI(0, 1, 0);
            assertEquals("Cardinality should be 0", 0, fv.cardinality());
            fv.setSI(0, 2, 999);
            assertEquals("Cardinality should be 1", 1, fv.cardinality());
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertTrue("FloatMatrix should be equal to itself", fv.equals(fv));
        assertFalse("FloatMatrix should not be equal to null", fv.equals(null));
        if (fv instanceof FloatMatrixAbs<?>)
        {
            copy = ((FloatMatrixAbs<LengthUnit>) fv).copy();
        }
        else if (fv instanceof FloatMatrixRel<?>)
        {
            copy = ((FloatMatrixRel<LengthUnit>) fv).copy();
        }
        else
            fail("Matrix neither Absolute nor Relative");
        assertTrue("FloatMatrix should be equal to copy of itself", fv.equals(copy));
        try
        {
            copy.setSI(0, 1, copy.getSI(0, 1) + 0.001f);
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertFalse("FloatMatrix should be different from slightly altered to copy of itself", fv.equals(copy));
        try
        {
            copy.setSI(0, 1, fv.getSI(0, 1));
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertTrue("FloatMatrix should be equal to repaired copy of itself", fv.equals(copy));
        if (absolute)
        {
            float[][] values = fv.getValuesInUnit();
            FloatMatrix<LengthUnit> fvr = null;
            try
            {
                fvr = createFloatMatrix(values, fv.getUnit(), false);
                for (int i = 0; i < in2.length; i++)
                    for (int j = 0; j < in2[i].length; j++)
                        assertEquals("Values should be equal", fv.getSI(i, j), fvr.getSI(i, j), 0.00001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertEquals("fv and fvr should have same unit", fv.getUnit(), fvr.getUnit());
            assertFalse("fv and fvr should not be equal", fv.equals(fvr));
            assertFalse("fvr and fv should not be equal", fvr.equals(fv));
        }
        float[][] inNonRect = {{1, 2, 3, 4, 5}, {1, 2, 3, 4}};
        try
        {
            fv = createFloatMatrix(inNonRect, LengthUnit.METER, absolute);
            fail("Non rectangular input data should have thrown a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        float[][] in3 = {{-100, -10, -1, -0.1f, 1, 0.1f, 1, 10, 100}, {-1000000, -1000, -100, -0.001f, 1, 0.001f, 100, 1000, 1000000}};
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.abs();
        MathTester.tester(in3, "abs", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return Math.abs(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.acos();
        MathTester.tester(in3, "acos", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.acos(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.asin();
        MathTester.tester(in3, "asin", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.asin(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.atan();
        MathTester.tester(in3, "atan", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.atan(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.cbrt();
        MathTester.tester(in3, "cbrt", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.cbrt(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.ceil();
        MathTester.tester(in3, "ceil", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.ceil(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.cos();
        MathTester.tester(in3, "cos", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.cos(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.cosh();
        MathTester.tester(in3, "cosh", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.cosh(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.exp();
        MathTester.tester(in3, "exp", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.exp(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.expm1();
        MathTester.tester(in3, "expm1", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.expm1(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.floor();
        MathTester.tester(in3, "floor", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.floor(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.log();
        MathTester.tester(in3, "log", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.log(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.log10();
        MathTester.tester(in3, "log10", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.log10(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.log1p();
        MathTester.tester(in3, "log1p", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.log1p(f);
            }
        });
        for (float power = -5; power <= 5; power += 0.5)
        {
            fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
            fv.pow(power);
            final float myPower = power;
            MathTester.tester(in3, "pow(" + power + ")", fv.getValuesSI(), 0.001, new FloatToFloat()
            {
                public float function(final float f)
                {
                    return (float) Math.pow(f, myPower);
                }
            });
        }
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.rint();
        MathTester.tester(in3, "rint", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.rint(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.round();
        MathTester.tester(in3, "round", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return Math.round(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.signum();
        MathTester.tester(in3, "signum", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return Math.signum(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.sin();
        MathTester.tester(in3, "sin", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.sin(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.sinh();
        MathTester.tester(in3, "sinh", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.sinh(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.sqrt();
        MathTester.tester(in3, "sqrt", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.sqrt(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.tan();
        MathTester.tester(in3, "tan", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.tan(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.tanh();
        MathTester.tester(in3, "tanh", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.tanh(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.toDegrees();
        MathTester.tester(in3, "toDegrees", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.toDegrees(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.toRadians();
        MathTester.tester(in3, "toRadians", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) Math.toRadians(f);
            }
        });
        fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
        fv.inv();
        MathTester.tester(in3, "inv", fv.getValuesSI(), 0.001, new FloatToFloat()
        {
            public float function(float f)
            {
                return (float) (1.0 / f);
            }
        });
        for (float factor = -5; factor <= 5; factor += 0.5)
        {
            fv = safeCreateFloatMatrix(in3, LengthUnit.METER, absolute);
            fv.multiply(factor);
            final float myFactor = factor;
            MathTester.tester(in3, "multiply(" + factor + ")", fv.getValuesSI(), 0.001, new FloatToFloat()
            {
                public float function(final float f)
                {
                    return myFactor * f;
                }
            });
        }
        if (absolute)
        {
            FloatMatrixAbs<LengthUnit> fv1 =
                    (FloatMatrixAbs<LengthUnit>) safeCreateFloatMatrix(in3, LengthUnit.METER, true);
            float[][] inRowCountMismatch = {{-100, -10, -1, -0.1f, 1, 0.1f, 1, 10}, {-1000000, -1000, -100, -0.001f, 1, 0.001f, 100, 1000}};
            float[][] inColCountMismatch = {{-100, -10, -1, -0.1f, 1, 0.1f, 1, 10, 100}};
            FloatMatrixRel<LengthUnit> fv2 =
                    (FloatMatrixRel<LengthUnit>) safeCreateFloatMatrix(inRowCountMismatch, LengthUnit.METER, false);
            FloatMatrix<LengthUnit> plus = null;
            FloatMatrix<LengthUnit> minus = null;
            try
            {
                plus = FloatMatrix.plus(fv1, fv2);
                fail("Adding FloatMatrixs of unequal size should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            try
            {
                minus = FloatMatrix.minus(fv1, fv2);
                fail("Subtracting FloatMatrices of unequal size should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            fv2 = (FloatMatrixRel<LengthUnit>) safeCreateFloatMatrix(inColCountMismatch, LengthUnit.METER, false);
            try
            {
                plus = FloatMatrix.plus(fv1, fv2);
                fail("Adding FloatMatrixs of unequal size should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            try
            {
                minus = FloatMatrix.minus(fv1, fv2);
                fail("Subtracting FloatMatrixs of unequal length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            float[][] in5 = {{1, 2, 3, 4, 5, 6, 7, 8, 9}, {11, 12, 13, 14, 15, 16, 17, 18, 19}};
            fv2 = (FloatMatrixRel<LengthUnit>) safeCreateFloatMatrix(in5, LengthUnit.METER, false);
            try
            {
                plus = FloatMatrix.plus(fv1, fv2);
                minus = FloatMatrix.minus(fv1, fv2);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("plus should not return null", null != plus);
            assertEquals("rowcount of result should be " + in5.length, in5.length, plus.rows());
            assertEquals("columncount of result should be " + in5[0].length, in5[0].length, plus.columns());
            assertTrue("minus should not return null", null != minus);
            assertEquals("rowcount of result should be " + in5.length, in5.length, minus.rows());
            assertEquals("columncount of result should be " + in5[0].length, in5[0].length, minus.columns());
            try
            {
                for (int i = 0; i < in3.length; i++)
                    for (int j = 0; j < in[i].length; j++)
                        assertEquals("value of element should be sum of contributing elements", in3[i][j] + in5[i][j],
                                plus.getSI(i, j), 0.00001);
                for (int i = 0; i < in3.length; i++)
                    for (int j = 0; j < in[i].length; j++)
                        assertEquals("value of element should be sum of contributing elements", in3[i][j] - in5[i][j],
                                minus.getSI(i, j), 0.00001);
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

            FloatMatrix<LengthUnit> plusReverse = null;
            try
            {
                plusReverse = FloatMatrix.plus(fv2, fv1);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("result should be Absolute", plusReverse instanceof Absolute);
            // System.out.println("plus is       " + plus);
            // System.out.println("plusReverse is" + plusReverse);
            assertTrue("result of a + b should be equal to result of b + a", plus.equals(plusReverse));
        }
        else
        {
            FloatMatrixRel<LengthUnit> fv1 =
                    (FloatMatrixRel<LengthUnit>) safeCreateFloatMatrix(in3, LengthUnit.METER, false);
            float[][] in4 = {{1, 2, 3, 4}};
            FloatMatrixRel<LengthUnit> fv2 =
                    (FloatMatrixRel<LengthUnit>) safeCreateFloatMatrix(in4, LengthUnit.METER, false);
            FloatMatrix<SIUnit> multiply = null;
            try
            {
                multiply = FloatMatrix.multiply(fv1, fv2);
                fail("Adding FloatMatrices of unequal length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            float[][] in5 = {{1, 2, 3, 4, 5, 6, 7, 8, 9}, {11, 12, 13, 14, 15, 16, 17, 18, 19}};
            fv2 = (FloatMatrixRel<LengthUnit>) safeCreateFloatMatrix(in5, LengthUnit.METER, false);
            try
            {
                multiply = FloatMatrix.multiply(fv1, fv2);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("multiply should not return null", null != multiply);
            assertEquals("size of result should be " + in5.length, in5.length, multiply.rows());
            assertEquals("size of result should be " + in5[0].length, in5[0].length, multiply.columns());
            try
            {
                for (int i = 0; i < in3.length; i++)
                    for (int j = 0; j < in3[i].length; j++)
                        assertEquals("value of element should be sum of contributing elements", in3[i][j] * in5[i][j],
                                multiply.getSI(i, j), 0.00001);
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

            FloatMatrix<SIUnit> multiplyReverse = null;
            try
            {
                multiplyReverse = FloatMatrix.multiply(fv2, fv1);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("result should be Absolute", multiplyReverse instanceof Relative);
            // System.out.println("plus is       " + multiply);
            // System.out.println("plusReverse is" + multiplyReverse);
            assertTrue("result of a * b should be equal to result of b * a", multiply.equals(multiplyReverse));
        }
        if (fv instanceof Dense)
        {
            FloatMatrix<LengthUnit> fv2 = null;
            if (fv instanceof Absolute)
                fv2 = FloatMatrix.denseToSparse((FloatMatrixAbsDense<LengthUnit>) fv);
            else
                fv2 = FloatMatrix.denseToSparse((FloatMatrixRelDense<LengthUnit>) fv);
            // FIXME this one fails: assertFalse("dense version is not equal to sparse version", fv.equals(fv2));
            assertEquals("unit should be same", fv.getUnit(), fv2.getUnit());
            try
            {
                for (int i = 0; i < fv.rows(); i++)
                    for (int j = 0; j < fv.columns(); j++)
                        assertEquals("Values should be equal", fv.getSI(i, j), fv2.getSI(i, j), 0.0001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
        }
        else
        {
            FloatMatrix<LengthUnit> fv2 = null;
            if (fv instanceof Absolute)
                fv2 = FloatMatrix.sparseToDense((FloatMatrixAbsSparse<LengthUnit>) fv);
            else
                fv2 = FloatMatrix.sparseToDense((FloatMatrixRelSparse<LengthUnit>) fv);
            // FIXME this one fails: assertFalse("dense version is not equal to sparse version", fv.equals(fv2));
            assertEquals("unit should be same", fv.getUnit(), fv2.getUnit());
            try
            {
                for (int i = 0; i < fv.rows(); i++)
                    for (int j = 0; j < fv.columns(); j++)
                        assertEquals("Values should be equal", fv.getSI(i, j), fv2.getSI(i, j), 0.0001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
        }
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
         * @param inputValues array of array of float with unprocessed values
         * @param operation String; description of method that is being tested
         * @param resultValues array of array of float with processed values
         * @param precision double expected accuracy
         * @param function FloatToFloat encapsulating function that converts one value in inputValues to the
         *            corresponding value in resultValues
         */
        public static void tester(final float[][] inputValues, String operation, final float[][] resultValues,
                final double precision, final FloatToFloat function)
        {
            for (int i = 0; i < inputValues.length; i++)
                for (int j = 0; j < inputValues[i].length; j++)
                {
                    float result = function.function(inputValues[i][j]);
                    String description =
                            String.format("index=%d: %s(%f)->%f should be equal to %f with precision %f", i, operation,
                                    inputValues[i][j], result, resultValues[i][j], precision);
                    // System.out.println(description);
                    assertEquals(description, result, resultValues[i][j], precision);
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
     * Test FloatMatrixAbs and FloatMatrixRel creators that take an array of FloatScalar as argument
     */
    @Test
    public void floatMatrixOneArg()
    {
        floatMatrixOneArg(true); // test absolute version
        floatMatrixOneArg(false); // test relative version
    }

    /**
     * Test the FloatMatrixAbs and FloatMatrixRel that takes a FloatScalar*<U>[] as argument
     */
    @SuppressWarnings("unchecked")
    private void floatMatrixOneArg(Boolean absolute)
    {
        FloatMatrix<LengthUnit> fv = null;
        FloatScalarAbs<LengthUnit>[][] inAbs = new FloatScalarAbs[0][0];
        FloatScalarRel<LengthUnit>[][] inRel = new FloatScalarRel[0][0];
        try
        {
            if (absolute)
                fv = createFloatMatrixAbs(inAbs);
            else
                fv = createFloatMatrixRel(inRel);
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        inAbs = new FloatScalarAbs[1][1];
        inAbs[0][0] = new FloatScalarAbs<LengthUnit>(123.456f, LengthUnit.FOOT);
        inRel = new FloatScalarRel[1][1];
        inRel[0][0] = new FloatScalarRel<LengthUnit>(123.456f, LengthUnit.FOOT);
        try
        {
            if (absolute)
                fv = createFloatMatrixAbs(inAbs);
            else
                fv = createFloatMatrixRel(inRel);
        }
        catch (ValueException exception)
        {
            fail("Should NOT have thrown an exception");
        }
        float[][] out = fv.getValuesInUnit();
        assertTrue("Result of getValuesInUnit should not be null", null != out);
        assertEquals("Array of values should have length 1", 1, out.length);
        assertEquals("Element in array should have the expected value", 123.456f, out[0][0], 0.001);
    }

    /**
     * Test adding and subtracting FloatMatrixRel.
     */
    /*
    @Test
    public void relRel()
    {
        float[] in1 = {10f, 20f, 30f, 40f};
        float[] in2 = {110f, 120f, 130f, 140f};
        MassUnit u = MassUnit.POUND;
        FloatMatrixRel<MassUnit> fv1 = createFloatMatrixRel(in1, u);
        FloatMatrixRel<MassUnit> fv2 = createFloatMatrixRel(in2, u);
        FloatMatrixRel<MassUnit> sum = null;
        try
        {
            sum = FloatMatrix.plus(fv1, fv2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add FloatMatrixRel to FloatMatrixRel of same size");
        }
        assertTrue("Result should not be null", null != sum);
        assertEquals("Size of result should be size of inputs", 4, sum.size());
        assertEquals("Type of result should be type of inputs", u, sum.getUnit());
        float[] sumValues = sum.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            assertEquals("Each element should equal the sum of the contributing elements", in1[i] + in2[i],
                    sumValues[i], 0.0001);
        FloatMatrixRel<MassUnit> difference = null;
        try
        {
            difference = FloatMatrix.minus(fv1, fv2);
        }
        catch (ValueException exception1)
        {
            fail("Should be able to subtract FloatMatrixRel from FloatMatrixRel of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 4, difference.size());
        assertEquals("Type of result should be type of inputs", u, difference.getUnit());
        float[] differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            assertEquals("Each element should equal the difference of the contributing elements", in1[i] - in2[i],
                    differenceValues[i], 0.0001);
        float[] in3 = {110f, 120f, 130f};
        FloatMatrixRel<MassUnit> fv3 = createFloatMatrixRel(in3, u);
        try
        {
            sum = FloatMatrix.plus(fv1, fv3);
            fail("Adding FloatMatrixs of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            difference = FloatMatrix.minus(fv1, fv3);
            fail("Subtracting FloatMatrixs of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        MassUnit u2 = MassUnit.OUNCE;
        fv2 = createFloatMatrixRel(in2, u2);
        try
        {
            sum = FloatMatrix.plus(fv1, fv2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add FloatMatrixRel to FloatMatrixRel of same size");
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
        fv2 = createFloatMatrixRel(in2, u2);
        try
        {
            difference = FloatMatrix.minus(fv1, fv2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add FloatMatrixRel to FloatMatrixRel of same size");
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
    */

    /**
     * Test adding and subtracting FloatMatrixAbs.
     */
    /*
    @Test
    public void absAbs()
    {
        float[] in1 = {10f, 20f, 30f, 40f};
        float[] in2 = {110f, 120f, 130f, 140f};
        MassUnit u = MassUnit.POUND;
        FloatMatrixAbs<MassUnit> fv1 = createFloatMatrixAbs(in1, u);
        FloatMatrixAbs<MassUnit> fv2 = createFloatMatrixAbs(in2, u);
        FloatMatrixRel<MassUnit> difference = null;
        try
        {
            difference = FloatMatrix.minus(fv1, fv2);
        }
        catch (ValueException exception1)
        {
            fail("Should be able to subtract FloatMatrixAbs from FloatMatrixAbs of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 4, difference.size());
        assertEquals("Type of result should be type of inputs", u, difference.getUnit());
        float[] differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            assertEquals("Each element should equal the difference of the contributing elements", in1[i] - in2[i],
                    differenceValues[i], 0.0001);
        float[] in3 = {110f, 120f, 130f};
        FloatMatrixAbs<MassUnit> fv3 = createFloatMatrixAbs(in3, u);
        try
        {
            difference = FloatMatrix.minus(fv1, fv3);
            fail("Subtracting FloatMatrixs of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        MassUnit u2 = MassUnit.OUNCE;
        fv2 = createFloatMatrixAbs(in2, u2);
        fv2 = createFloatMatrixAbs(in2, u2);
        try
        {
            difference = FloatMatrix.minus(fv1, fv2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add FloatMatrixAbs to FloatMatrixAbs of same size");
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
            difference = FloatMatrix.minus(fv2, fv1);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add FloatMatrixAbs to FloatMatrixAbs of same size");
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
        FloatMatrixAbs<LengthUnit> fv4 = createFloatMatrixAbs(in1, u4);
        ForceUnit u5 = ForceUnit.POUND_FORCE;
        FloatMatrixAbs<ForceUnit> fv5 = createFloatMatrixAbs(in2, u5);
        Unit<EnergyUnit> resultUnit = EnergyUnit.CALORIE_IT;
        FloatMatrixAbs<?> product = null;
        try
        {
            product = FloatMatrix.multiply(fv4, fv5);
        }
        catch (ValueException exception)
        {
            fail("Should be able to multiply FloatMatrixAbs with FloatMatrixAbs of same size");
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
    */

    /**
     * Test the FloatMatrixRelDense that takes a float[] as argument.
     */
    /*
    @SuppressWarnings("unchecked")
    @Test
    public void floatMatrixRel2()
    {
        FloatMatrix<LengthUnit> fsa = null;
        FloatScalarRel<LengthUnit>[] in = new FloatScalarRel[0];
        try
        {
            fsa = createFloatMatrixRel(in);
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
            fsa = new FloatMatrixRelDense<LengthUnit>(in);
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
    */

    /**
     * Version of createFloatMatrix for use with guaranteed rectangular data
     * @param in float[][] with values MUST BE RECTANGULAR
     * @param u Unit; type for the new FloatMatrix
     * @param absolute Boolean; true to create a FloatMatrixAbs; false to create a FloatMatrixRel
     * @return FloatMatrix
     */
    private <U extends Unit<U>> FloatMatrix<U> safeCreateFloatMatrix(float[][] in, U u, boolean absolute)
    {
        FloatMatrix<U> result = null;
        try
        {
            result = createFloatMatrix(in, u, absolute);
        }
        catch (ValueException exception)
        {
            fail("Caught unexpected ValueException in safeCreateFloatMatrix");
        }
        return result;
    }
    /**
     * Create a FloatMatrixAbs or a FloatMatrixRel from an array of float values and Unit
     * @param in float[][] with values
     * @param u Unit; type for the new FloatMatrix
     * @param absolute Boolean; true to create a FloatMatrixAbs; false to create a FloatMatrixRel
     * @return FloatMatrix
     * @throws ValueException 
     */
    private <U extends Unit<U>> FloatMatrix<U> createFloatMatrix(float[][] in, U u, boolean absolute) throws ValueException
    {
        if (absolute)
            return createFloatMatrixAbs(in, u);
        else
            return createFloatMatrixRel(in, u);
    }

    /**
     * Create a new FloatMatrixAbs from an array of float values and Unit.
     * @param in float[][] with values
     * @param u Unit; type for the new FloatMatrixAbs
     * @return
     * @throws ValueException when the array is not rectangular
     */
    protected abstract <U extends Unit<U>> FloatMatrixAbs<U> createFloatMatrixAbs(float[][] in, U u) throws ValueException;

    /**
     * Create a new FloatMatrixAbs from an array of FloatScalarAbs values.
     * @param in FloatScalarAbs[][]; the values
     * @return
     * @throws ValueException when the array is empty or not rectangular
     */
    protected abstract <U extends Unit<U>> FloatMatrixAbs<U> createFloatMatrixAbs(FloatScalarAbs<U>[][] in)
            throws ValueException;

    /**
     * Create a new FloatMatrixRel from an array of float values and Unit.
     * @param in float[][] with values
     * @param u Unit; type for the new FloatMatrixAbs
     * @return
     * @throws ValueException when the array is not rectangular
     */
    protected abstract <U extends Unit<U>> FloatMatrixRel<U> createFloatMatrixRel(float[][] in, U u) throws ValueException;

    /**
     * Create a new FloatMatrixRel from an array of FloatScalarAbs values.
     * @param in FloatScalarAbs[][]; the values
     * @return
     * @throws ValueException when the array is empty or not rectangular
     */
    protected abstract <U extends Unit<U>> FloatMatrixRel<U> createFloatMatrixRel(FloatScalarRel<U>[][] in)
            throws ValueException;

}

package org.opentrafficsim.core.value.vdouble.matrix;

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
import org.opentrafficsim.core.value.Dense;
import org.opentrafficsim.core.value.Format;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.Sparse;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

import cern.colt.matrix.tdouble.DoubleMatrix2D;

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
 * @version Jul 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class DoubleMatrixTest
{
    /**
     * Test DoubleMatrixAbs and DoubleMatrixRel creators that take a double[][] and a Unit as arguments
     */
    @Test
    public void doubleMatrixTwoArgs()
    {
        doubleMatrixTwoArgs(true); // test absolute version
        doubleMatrixTwoArgs(false); // rest relative version
    }

    /**
     * Create a 2D array of double for testing. Entry 0,0 is zero, all others will be non-zero unless a non-zero offset
     * is specified.
     * @param rows Integer; number of rows in the 2D array
     * @param cols Integer; number of columns in the 2D array
     * @param badRow Boolean; if true; make the last row one entry longer than the rest
     * @param offset Double; number to add to each entry
     * @return double[][]; 2D array of double
     */
    private static double[][] buildArray(int rows, int cols, boolean badRow, double offset)
    {
        double[][] result = new double[rows][];
        int badRowIndex = badRow ? rows - 1 : -1;
        for (int row = 0; row < rows; row++)
        {
            result[row] = new double[row == badRowIndex ? cols + 1 : cols];
            for (int col = 0; col < result[row].length; col++)
                result[row][col] = row * 1000 + col + offset;
        }
        return result;
    }

    /**
     * Test the DoubleMatrixAbs that takes a double[][] and a Unit as arguments and some methods.
     */
    private void doubleMatrixTwoArgs(Boolean absolute)
    {
        double[][] in = buildArray(12, 3, false, 0);
        LengthUnit u = LengthUnit.FOOT;
        DoubleMatrix<LengthUnit> fm = safeCreateDoubleMatrix(in, u, absolute);
        assertEquals("DoubleMatrix should have 12 rows", 12, fm.rows());
        assertEquals("DoubleMatrix should have 3 columns", 3, fm.columns());
        double[][] out = fm.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
            {
                assertEquals("Values in DoubleMatrix in unit should be equal to input values", in[i][j], out[i][j],
                        0.0001);
                try
                {
                    assertEquals("Values in DoubleMatrix in unit should be equal to input values", in[i][j],
                            fm.getInUnit(i, j), 0.0001);
                    assertEquals("Values in DoubleMatrix in unit should be equal to input values", in[i][j],
                            fm.getSI(i, j) / (12 * 0.0254), 0.0001);
                    assertEquals("Values in DoubleMatrix in unit should be equal to input values", in[i][j],
                            fm.getInUnit(i, j, LengthUnit.MILE) * 1609.34 / (12 * 0.0254), 0.1);
                }
                catch (ValueException exception)
                {
                    fail("Get should not throw exceptions for legal values of the index");
                }
            }
        String output = fm.toString(LengthUnit.MILLIMETER);
        String[] lines = output.split("[\n]");
        assertEquals("Number of lines should be number of rows + 1", in.length + 1, lines.length);
        assertEquals("first line should contain unit in brackers", "[mm]", lines[0]);
        for (int i = 1; i < lines.length; i++)
        {
            String[] fields = lines[i].trim().split("[ ]+");
            assertEquals("Number of fields should be number of columns", fm.columns(), fields.length);
            for (int j = 0; j < fields.length; j++)
            {
                double expectedValue = in[i - 1][j] * (12 * 0.0254) * 1000;
                String expected = Format.format(expectedValue).trim();
                assertEquals("Field " + j + " should contain \"" + expected + "\"", expected, fields[j]);
            }
        }
        output = fm.toString();
        lines = output.split("[\n]");
        assertEquals("Number of lines should be number of rows + 1", in.length + 1, lines.length);
        assertEquals("first line should contain unit in brackers", "[ft]", lines[0]);
        for (int i = 1; i < lines.length; i++)
        {
            String[] fields = lines[i].trim().split("[ ]+");
            assertEquals("Number of fields should be number of columns", fm.columns(), fields.length);
            for (int j = 0; j < fields.length; j++)
            {
                double expectedValue = in[i - 1][j];
                String expected = Format.format(expectedValue).trim();
                assertEquals("Field " + i + " should contain \"" + expected + "\"", expected, fields[j]);
            }
        }

        DoubleMatrix2D fm2d;
        if (fm instanceof DoubleMatrixAbsDense)
            fm2d = ((DoubleMatrixAbsDense<LengthUnit>) fm).getColtDenseDoubleMatrix2D();
        else if (fm instanceof DoubleMatrixRelDense)
            fm2d = ((DoubleMatrixRelDense<LengthUnit>) fm).getColtDenseDoubleMatrix2D();
        else if (fm instanceof DoubleMatrixAbsSparse)
            fm2d = ((DoubleMatrixAbsSparse<LengthUnit>) fm).getColtSparseDoubleMatrix2D();
        else if (fm instanceof DoubleMatrixRelSparse)
            fm2d = ((DoubleMatrixRelSparse<LengthUnit>) fm).getColtSparseDoubleMatrix2D();
        else
            throw new Error("Matrix neither Dense nor Sparse");
        assertTrue("ColtMatrix1D should not be null", null != fm2d);
        assertEquals("Size of Colt matrix should be number of cells", in.length * in[0].length, fm2d.size());
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Contents of Colt matrix should be SI equivalent of input", in[i][j] * (12 * 0.0254),
                        fm2d.getQuick(i, j), 0.0001);
        fm2d = fm.getMatrixSI();
        assertTrue("MatrixSI should not be null", null != fm2d);
        assertEquals("Size of MatrixSI should be number of cells", in.length * in[0].length, fm2d.size());
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Contents of MatrixSI should be SI equivalent of input", in[i][j] * (12 * 0.0254),
                        fm2d.getQuick(i, j), 0.0001);
        double[][] valuesInUnit = fm.getValuesInUnit();
        assertTrue("valuesInUnit should not be null", null != valuesInUnit);
        assertEquals("Size of valuesInUnit should be size of input array", in.length, valuesInUnit.length);
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Contents of valuesInUnit should be equal to input", in[i][j], valuesInUnit[i][j], 0.0001);
        LengthUnit outputUnit = LengthUnit.DEKAMETER;
        double[][] valuesInOtherUnit = fm.getValuesInUnit(outputUnit);
        assertTrue("valuesInUnit should not be null", null != valuesInOtherUnit);
        assertEquals("Size of valuesInUnit should be size of input array", in.length, valuesInOtherUnit.length);
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Contents of valuesInUnit should be equal to input", in[i][j] * (12 * 0.0254) / 10,
                        valuesInOtherUnit[i][j], 0.0001);
        try
        {
            fm.getInUnit(-1, 0);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fm.getInUnit(in.length, 0);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fm.getInUnit(0, -1);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fm.getInUnit(0, in[0].length);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fm.getSI(-1, 0);
            fail("Using a bad index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fm.getSI(in.length, 0);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fm.getSI(0, -1);
            fail("Using a bad index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fm.getSI(0, in[0].length);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fm.getInUnit(-1, 0, LengthUnit.MILE);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fm.getInUnit(in.length, 0, LengthUnit.MILE);
            fail("Using an index that is too big should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fm.getInUnit(0, -1, LengthUnit.MILE);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fm.getInUnit(0, in[0].length, LengthUnit.MILE);
            fail("Using an index that is too big should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            fm.setSI(-1, 0, 12345f);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        try
        {
            fm.setSI(in.length, 0, 12345f);
            fail("Using an index that is too big should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        try
        {
            fm.setSI(0, -1, 12345f);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        try
        {
            fm.setSI(0, in[0].length, 12345f);
            fail("Using an index that is too big should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        out = fm.getValuesSI();
        assertTrue("getValuesSI does not return null", null != out);
        assertEquals("Length of getValuesSI should match size", in.length, out.length);
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Values in DoubleMatrix should be equivalent values in meters", in[i][j], out[i][j]
                        / (12 * 0.0254), 0.0001);
        LengthUnit uOut = fm.getUnit();
        assertEquals("Stored unit should be provided unit", u, uOut);
        DoubleMatrix<LengthUnit> copy = null;
        if (fm instanceof DoubleMatrixAbs<?>)
        {
            copy = ((DoubleMatrixAbs<LengthUnit>) fm).copy();
        }
        else if (fm instanceof DoubleMatrixRel<?>)
        {
            copy = ((DoubleMatrixRel<LengthUnit>) fm).copy();
        }
        else
            fail("Matrix neither Absolute nor Relative");

        assertEquals("copy should have 12 rows", 12, copy.rows());
        assertEquals("copy should have 3 columns", 3, copy.columns());
        double[][] copyOut = copy.getValuesSI();
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Values in copy of DoubleMatrix should be equivalent values in meters", in[i][j],
                        copyOut[i][j] / (12 * 0.0254), 0.0001);
        copyOut = fm.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                assertEquals("Values in copy of DoubleMatrix in unit should be equal to input values", in[i][j],
                        copyOut[i][j], 0.0001);
        try
        {
            copy.setSI(0, 0, 12345f);
            assertEquals("value should be altered", 12345f, copy.getSI(0, 0), 0.01);
            assertEquals("original value should not be altered", out[0][0], fm.getSI(0, 0), 0.001);
            DoubleScalar<LengthUnit> value = copy.get(1, 0);
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
        double sum = 0;
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < in[i].length; j++)
                sum += in[i][j];
        sum *= (12 * 0.0254); // convert to meters
        assertEquals("zsum should be sum of the values", sum, fm.zSum(), 0.001);
        try
        {
            fm.normalize();
            for (int i = 0; i < in.length; i++)
                for (int j = 0; j < in[i].length; j++)
                    assertEquals("Unexpected normalized value", in[i][j] * (12 * 0.0254) / sum, fm.getSI(i, j), 0.0001);
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertEquals("Cardinality should be 35", 35, fm.cardinality());
        double[][] in2 = {{1f, -1f, 0f}};
        fm = safeCreateDoubleMatrix(in2, u, absolute);
        assertEquals("zSum should be 0", 0, fm.zSum(), 0.00001);
        try
        {
            fm.normalize();
            fail("Should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        assertEquals("Cardinality should be 2", 2, fm.cardinality());
        try
        {
            fm.setSI(0, 0, 0);
            assertEquals("Cardinality should be 1", 1, fm.cardinality());
            fm.setSI(0, 1, 0);
            assertEquals("Cardinality should be 0", 0, fm.cardinality());
            fm.setSI(0, 2, 999);
            assertEquals("Cardinality should be 1", 1, fm.cardinality());
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertTrue("DoubleMatrix should be equal to itself", fm.equals(fm));
        assertFalse("DoubleMatrix should not be equal to null", fm.equals(null));
        if (fm instanceof DoubleMatrixAbs<?>)
        {
            copy = ((DoubleMatrixAbs<LengthUnit>) fm).copy();
        }
        else if (fm instanceof DoubleMatrixRel<?>)
        {
            copy = ((DoubleMatrixRel<LengthUnit>) fm).copy();
        }
        else
            fail("Matrix neither Absolute nor Relative");
        assertTrue("DoubleMatrix should be equal to copy of itself", fm.equals(copy));
        try
        {
            copy.setSI(0, 1, copy.getSI(0, 1) + 0.001f);
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertFalse("DoubleMatrix should be different from slightly altered to copy of itself", fm.equals(copy));
        try
        {
            copy.setSI(0, 1, fm.getSI(0, 1));
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertTrue("DoubleMatrix should be equal to repaired copy of itself", fm.equals(copy));
        if (absolute)
        {
            double[][] values = fm.getValuesInUnit();
            DoubleMatrix<LengthUnit> fmr = null;
            try
            {
                fmr = createDoubleMatrix(values, fm.getUnit(), false);
                for (int i = 0; i < in2.length; i++)
                    for (int j = 0; j < in2[i].length; j++)
                        assertEquals("Values should be equal", fm.getSI(i, j), fmr.getSI(i, j), 0.00001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertEquals("fm and fmr should have same unit", fm.getUnit(), fmr.getUnit());
            assertFalse("fm and fmr should not be equal", fm.equals(fmr));
            assertFalse("fmr and fm should not be equal", fmr.equals(fm));
        }
        double[][] inNonRect = buildArray(4, 5, true, 0);
        try
        {
            fm = createDoubleMatrix(inNonRect, LengthUnit.METER, absolute);
            fail("Non rectangular input data should have thrown a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        double[][] in3 =
                {{-100, -10, -1, -0.1f, 1, 0.1f, 1, 10, 100},
                        {-1000000, -1000, -100, -0.001f, 1, 0.001f, 100, 1000, 1000000}};
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.abs();
        MathTester.tester(in3, "abs", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.abs(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.acos();
        MathTester.tester(in3, "acos", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.acos(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.asin();
        MathTester.tester(in3, "asin", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.asin(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.atan();
        MathTester.tester(in3, "atan", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.atan(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.cbrt();
        MathTester.tester(in3, "cbrt", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.cbrt(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.ceil();
        MathTester.tester(in3, "ceil", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.ceil(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.cos();
        MathTester.tester(in3, "cos", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.cos(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.cosh();
        MathTester.tester(in3, "cosh", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.cosh(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.exp();
        MathTester.tester(in3, "exp", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.exp(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.expm1();
        MathTester.tester(in3, "expm1", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.expm1(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.floor();
        MathTester.tester(in3, "floor", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.floor(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.log();
        MathTester.tester(in3, "log", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.log(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.log10();
        MathTester.tester(in3, "log10", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.log10(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.log1p();
        MathTester.tester(in3, "log1p", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.log1p(f);
            }
        });
        for (double power = -5; power <= 5; power += 0.5)
        {
            fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
            fm.pow(power);
            final double myPower = power;
            MathTester.tester(in3, "pow(" + power + ")", fm.getValuesSI(), 0.001, new DoubleToDouble()
            {
                public double function(final double f)
                {
                    return Math.pow(f, myPower);
                }
            });
        }
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.rint();
        MathTester.tester(in3, "rint", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.rint(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.round();
        MathTester.tester(in3, "round", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.round(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.signum();
        MathTester.tester(in3, "signum", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.signum(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.sin();
        MathTester.tester(in3, "sin", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.sin(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.sinh();
        MathTester.tester(in3, "sinh", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.sinh(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.sqrt();
        MathTester.tester(in3, "sqrt", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.sqrt(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.tan();
        MathTester.tester(in3, "tan", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.tan(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.tanh();
        MathTester.tester(in3, "tanh", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.tanh(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.toDegrees();
        MathTester.tester(in3, "toDegrees", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.toDegrees(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.toRadians();
        MathTester.tester(in3, "toRadians", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return Math.toRadians(f);
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        fm.inv();
        MathTester.tester(in3, "inv", fm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            public double function(double f)
            {
                return 1.0 / f;
            }
        });
        fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
        try
        {
            fm.det();
            fail("det of non-square matrix should have thrown a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        double[][] singular = {{1, 2, 3}, {3, 5, 7}, {5, 10, 0}};
        fm = safeCreateDoubleMatrix(singular, LengthUnit.METER, absolute);
        System.out.println("matrix is " + fm.toString());
        if (fm instanceof Sparse)
            System.out.println("(sparse)");
        if (fm instanceof Dense)
            System.out.println("(dense)");
        if (fm instanceof Absolute)
            System.out.println("(absolute)");
        if (fm instanceof Relative)
            System.out.println("(relative)");
        try
        {
            assertEquals("Determinant should be 15", 15, fm.det(), 0.0001);
        }
        catch (ValueException exception1)
        {
            // TODO this one fails for sparse matrices
            // admittedly, this matrix is not very sparse
            exception1.printStackTrace();
            // fail("Unexpected exception");
        }
        for (double factor = -5; factor <= 5; factor += 0.5)
        {
            fm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute);
            fm.multiply(factor);
            final double myFactor = factor;
            MathTester.tester(in3, "multiply(" + factor + ")", fm.getValuesSI(), 0.001, new DoubleToDouble()
            {
                public double function(final double f)
                {
                    return myFactor * f;
                }
            });
        }
        if (absolute)
        {
            DoubleMatrixAbs<LengthUnit> fm1 =
                    (DoubleMatrixAbs<LengthUnit>) safeCreateDoubleMatrix(in3, LengthUnit.METER, true);
            double[][] inRowCountMismatch =
                    {{-100, -10, -1, -0.1f, 1, 0.1f, 1, 10}, {-1000000, -1000, -100, -0.001f, 1, 0.001f, 100, 1000}};
            double[][] inColCountMismatch = {{-100, -10, -1, -0.1f, 1, 0.1f, 1, 10, 100}};
            DoubleMatrixRel<LengthUnit> fm2 =
                    (DoubleMatrixRel<LengthUnit>) safeCreateDoubleMatrix(inRowCountMismatch, LengthUnit.METER, false);
            DoubleMatrix<LengthUnit> plus = null;
            DoubleMatrix<LengthUnit> minus = null;
            try
            {
                plus = DoubleMatrix.plus(fm1, fm2);
                fail("Adding DoubleMatrices of unequal size should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            try
            {
                minus = DoubleMatrix.minus(fm1, fm2);
                fail("Subtracting DoubleMatrices of unequal size should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            fm2 = (DoubleMatrixRel<LengthUnit>) safeCreateDoubleMatrix(inColCountMismatch, LengthUnit.METER, false);
            try
            {
                plus = DoubleMatrix.plus(fm1, fm2);
                fail("Adding DoubleMatrices of unequal size should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            try
            {
                minus = DoubleMatrix.minus(fm1, fm2);
                fail("Subtracting DoubleMatrices of unequal length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            double[][] in5 = buildArray(2, 9, false, 0);
            fm2 = (DoubleMatrixRel<LengthUnit>) safeCreateDoubleMatrix(in5, LengthUnit.METER, false);
            try
            {
                plus = DoubleMatrix.plus(fm1, fm2);
                minus = DoubleMatrix.minus(fm1, fm2);
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
            if (fm1 instanceof Dense)
            {
                assertTrue("result should be Dense", plus instanceof Dense);
                assertTrue("result should be Dense", minus instanceof Dense);
            }
            else if (fm1 instanceof Sparse)
            {
                assertTrue("result should be Sparse", plus instanceof Sparse);
                assertTrue("result should be Sparse", minus instanceof Sparse);
            }
            else
                fail("fm1 neither Dense nor Sparse");

            DoubleMatrix<LengthUnit> plusReverse = null;
            try
            {
                plusReverse = DoubleMatrix.plus(fm2, fm1);
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
                double in4[][] = {{1, 2, 3}, {4, 5, 6}};
                DoubleMatrix<LengthUnit> original = safeCreateDoubleMatrix(in4, LengthUnit.METER, absolute);
                DoubleMatrix<LengthUnit> duplicate = (DoubleMatrix<LengthUnit>) original.copy();
                assertTrue("Original should be equal to duplicate", original.equals(duplicate));
                assertTrue("Duplicate should be equal to original", duplicate.equals(original));
                original.setSI(0, 0, 123.456);
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
            DoubleMatrixRel<LengthUnit> fm1 =
                    (DoubleMatrixRel<LengthUnit>) safeCreateDoubleMatrix(in3, LengthUnit.METER, false);
            double[][] in4 = buildArray(1, 4, false, 0);
            DoubleMatrixRel<LengthUnit> fm2 =
                    (DoubleMatrixRel<LengthUnit>) safeCreateDoubleMatrix(in4, LengthUnit.METER, false);
            DoubleMatrix<SIUnit> multiply = null;
            try
            {
                multiply = DoubleMatrix.multiply(fm1, fm2);
                fail("Adding DoubleMatrices of unequal length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            double[][] in5 = buildArray(2, 9, false, 0);
            fm2 = (DoubleMatrixRel<LengthUnit>) safeCreateDoubleMatrix(in5, LengthUnit.METER, false);
            try
            {
                multiply = DoubleMatrix.multiply(fm1, fm2);
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
            if (fm1 instanceof Dense)
            {
                assertTrue("result should be Dense", multiply instanceof Dense);
            }
            else if (fm1 instanceof Sparse)
            {
                assertTrue("result should be Sparse", multiply instanceof Sparse);
            }
            else
                fail("fm1 neither Dense nor Sparse");
            // System.out.println("Result of multiply has unit " + multiply);
            assertEquals("Result of multiplication should be in square meters", "m2", multiply.getUnit()
                    .getSICoefficientsString());

            DoubleMatrix<SIUnit> multiplyReverse = null;
            try
            {
                multiplyReverse = DoubleMatrix.multiply(fm2, fm1);
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
                double in6[][] = {{1, 2, 3}, {4, 5, 6}};
                DoubleMatrix<LengthUnit> original = safeCreateDoubleMatrix(in6, LengthUnit.METER, absolute);
                DoubleMatrix<LengthUnit> duplicate = (DoubleMatrix<LengthUnit>) original.copy();
                assertTrue("Original should be equal to duplicate", original.equals(duplicate));
                assertTrue("Duplicate should be equal to original", duplicate.equals(original));
                original.setSI(0, 0, 123.456);
                assertFalse("Original should now differ from duplicate", original.equals(duplicate));
                assertFalse("Duplicate should now differ from original", duplicate.equals(original));
                
            }
            catch (ValueException exception)
            {
                fail("Unexpected ValueException");
            }
        }
        if (fm instanceof Dense)
        {
            DoubleMatrix<LengthUnit> fm2 = null;
            if (fm instanceof Absolute)
                fm2 = DoubleMatrix.denseToSparse((DoubleMatrixAbsDense<LengthUnit>) fm);
            else
                fm2 = DoubleMatrix.denseToSparse((DoubleMatrixRelDense<LengthUnit>) fm);
            assertTrue("dense version is  equal to sparse version", fm.equals(fm2));
            assertEquals("unit should be same", fm.getUnit(), fm2.getUnit());
            try
            {
                for (int i = 0; i < fm.rows(); i++)
                    for (int j = 0; j < fm.columns(); j++)
                        assertEquals("Values should be equal", fm.getSI(i, j), fm2.getSI(i, j), 0.0001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
        }
        else
        {
            DoubleMatrix<LengthUnit> fm2 = null;
            if (fm instanceof Absolute)
                fm2 = DoubleMatrix.sparseToDense((DoubleMatrixAbsSparse<LengthUnit>) fm);
            else
                fm2 = DoubleMatrix.sparseToDense((DoubleMatrixRelSparse<LengthUnit>) fm);
            assertTrue("dense version is equal to sparse version", fm.equals(fm2));
            assertEquals("unit should be same", fm.getUnit(), fm2.getUnit());
            try
            {
                for (int i = 0; i < fm.rows(); i++)
                    for (int j = 0; j < fm.columns(); j++)
                        assertEquals("Values should be equal", fm.getSI(i, j), fm2.getSI(i, j), 0.0001);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
        }
        double[][] left = buildArray(4, 5, false, 0);
        DoubleMatrix<LengthUnit> leftMatrix = safeCreateDoubleMatrix(left, LengthUnit.METER, absolute);
        double[][] right = buildArray(4, 6, false, 0.3f);
        DoubleMatrixRel<LengthUnit> rightMatrix =
                (DoubleMatrixRel<LengthUnit>) safeCreateDoubleMatrix(right, LengthUnit.METER, false);
        try
        {
            leftMatrix.add(rightMatrix);
            fail("Adding matrices of different sizes should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // Ignore
        }
        right = buildArray(3, 5, false, 0.3f);
        rightMatrix = (DoubleMatrixRel<LengthUnit>) safeCreateDoubleMatrix(right, LengthUnit.METER, false);
        try
        {
            leftMatrix.add(rightMatrix);
            fail("Adding matrices of different sizes should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // Ignore
        }
        right = buildArray(4, 5, false, 0.3f);
        rightMatrix = (DoubleMatrixRel<LengthUnit>) safeCreateDoubleMatrix(right, LengthUnit.METER, false);
        try
        {
            leftMatrix.add(rightMatrix);
        }
        catch (ValueException exception)
        {
            fail("Adding matrices of equal sizes should not have thrown an exception");
        }
        try
        {
            for (int i = 0; i < left.length; i++)
                for (int j = 0; j < left[0].length; j++)
                    assertEquals("Values should now be sum of input values", left[i][j] + right[i][j],
                            leftMatrix.getSI(i, j), 0.001);
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        leftMatrix = safeCreateDoubleMatrix(left, LengthUnit.METER, absolute);
        right = buildArray(4, 6, false, 0.3f);
        rightMatrix = (DoubleMatrixRel<LengthUnit>) safeCreateDoubleMatrix(right, LengthUnit.METER, false);
        try
        {
            leftMatrix.subtract(rightMatrix);
            fail("Subtracting matrices of different sizes should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // Ignore
        }
        right = buildArray(3, 5, false, 0.3f);
        rightMatrix = (DoubleMatrixRel<LengthUnit>) safeCreateDoubleMatrix(right, LengthUnit.METER, false);
        try
        {
            leftMatrix.subtract(rightMatrix);
            fail("Subtracting matrices of different sizes should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // Ignore
        }
        right = buildArray(4, 5, false, 0.3f);
        rightMatrix = (DoubleMatrixRel<LengthUnit>) safeCreateDoubleMatrix(right, LengthUnit.METER, false);
        try
        {
            leftMatrix.subtract(rightMatrix);
        }
        catch (ValueException exception)
        {
            fail("Subtracting matrices of equal sizes should not have thrown an exception");
        }
        try
        {
            for (int i = 0; i < left.length; i++)
                for (int j = 0; j < left[0].length; j++)
                    assertEquals("Values should now be difference of input values", left[i][j] - right[i][j],
                            leftMatrix.getSI(i, j), 0.001);
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
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
         * @param inputValues array of array of double with unprocessed values
         * @param operation String; description of method that is being tested
         * @param resultValues array of array of double with processed values
         * @param precision double expected accuracy
         * @param function DoubleToDouble encapsulating function that converts one value in inputValues to the
         *            corresponding value in resultValues
         */
        public static void tester(final double[][] inputValues, String operation, final double[][] resultValues,
                final double precision, final DoubleToDouble function)
        {
            for (int i = 0; i < inputValues.length; i++)
                for (int j = 0; j < inputValues[i].length; j++)
                {
                    double result = function.function(inputValues[i][j]);
                    String description =
                            String.format("index=%d: %s(%f)->%f should be equal to %f with precision %f", i, operation,
                                    inputValues[i][j], result, resultValues[i][j], precision);
                    // System.out.println(description);
                    assertEquals(description, result, resultValues[i][j], precision);
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
     * Test DoubleMatrixAbs and DoubleMatrixRel creators that take an array of DoubleScalar as argument
     */
    @Test
    public void doubleMatrixOneArg()
    {
        doubleMatrixOneArg(true); // test absolute version
        doubleMatrixOneArg(false); // test relative version
    }

    /**
     * Test the DoubleMatrixAbs and DoubleMatrixRel that takes a DoubleScalar*<U>[] as argument
     */
    @SuppressWarnings("unchecked")
    private void doubleMatrixOneArg(Boolean absolute)
    {
        DoubleMatrix<LengthUnit> fm = null;
        DoubleScalarAbs<LengthUnit>[][] inAbs = new DoubleScalarAbs[0][0];
        DoubleScalarRel<LengthUnit>[][] inRel = new DoubleScalarRel[0][0];
        try
        {
            if (absolute)
                fm = createDoubleMatrixAbs(inAbs);
            else
                fm = createDoubleMatrixRel(inRel);
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        inAbs = new DoubleScalarAbs[1][0];
        inRel = new DoubleScalarRel[1][0];
        try
        {
            if (absolute)
                fm = createDoubleMatrixAbs(inAbs);
            else
                fm = createDoubleMatrixRel(inRel);
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        inAbs = new DoubleScalarAbs[0][1];
        inRel = new DoubleScalarRel[0][1];
        try
        {
            if (absolute)
                fm = createDoubleMatrixAbs(inAbs);
            else
                fm = createDoubleMatrixRel(inRel);
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        inAbs = new DoubleScalarAbs[1][1];
        inAbs[0][0] = new DoubleScalarAbs<LengthUnit>(123.456f, LengthUnit.FOOT);
        inRel = new DoubleScalarRel[1][1];
        inRel[0][0] = new DoubleScalarRel<LengthUnit>(123.456f, LengthUnit.FOOT);
        try
        {
            if (absolute)
                fm = createDoubleMatrixAbs(inAbs);
            else
                fm = createDoubleMatrixRel(inRel);
        }
        catch (ValueException exception)
        {
            fail("Should NOT have thrown an exception");
        }
        double[][] out = fm.getValuesInUnit();
        assertTrue("Result of getValuesInUnit should not be null", null != out);
        assertEquals("Array of values should have length 1", 1, out.length);
        assertEquals("Element in array should have the expected value", 123.456f, out[0][0], 0.001);
        double[][] in4 = buildArray(2, 3, true, 0);
        try
        {
            if (absolute)
            {
                DoubleScalarAbs<LengthUnit>[][] inv4 = new DoubleScalarAbs[in4.length][];
                for (int i = 0; i < in4.length; i++)
                {
                    inv4[i] = new DoubleScalarAbs[in4[i].length];
                    for (int j = 0; j < in4[i].length; j++)
                        inv4[i][j] = new DoubleScalarAbs<LengthUnit>(in4[i][j], LengthUnit.FOOT);
                }
                fm = createDoubleMatrixAbs(inv4);
            }
            else
            {
                DoubleScalarRel<LengthUnit>[][] inv4 = new DoubleScalarRel[in4.length][];
                for (int i = 0; i < in4.length; i++)
                {
                    inv4[i] = new DoubleScalarRel[in4[i].length];
                    for (int j = 0; j < in4[i].length; j++)
                        inv4[i][j] = new DoubleScalarRel<LengthUnit>(in4[i][j], LengthUnit.FOOT);
                }
                fm = createDoubleMatrixRel(inv4);
            }
            fail("Attempt to create doubleMatrix from 2D array with rows of different sizes should have failed");
        }
        catch (ValueException exception)
        {
            // Ignore
        }
    }

    /**
     * Test adding and subtracting DoubleMatrixRel.
     */
    @Test
    public void relRel()
    {
        double[][] in1 = buildArray(2, 4, false, 0);
        double[][] in2 = buildArray(2, 4, false, 0);
        MassUnit u = MassUnit.POUND;
        DoubleMatrixRel<MassUnit> fm1 = null;
        DoubleMatrixRel<MassUnit> fm2 = null;
        try
        {
            fm1 = createDoubleMatrixRel(in1, u);
            fm2 = createDoubleMatrixRel(in2, u);
        }
        catch (ValueException exception2)
        {
            fail("Unexpected exception");
        }
        DoubleMatrixRel<MassUnit> sum = null;
        try
        {
            sum = DoubleMatrix.plus(fm1, fm2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add DoubleMatrixRel to DoubleMatrixRel of same size");
        }
        assertTrue("Result should not be null", null != sum);
        assertEquals("Size of result should be size of inputs", 2, sum.rows());
        assertEquals("Size of result should be size of inputs", 4, sum.columns());
        assertEquals("Type of result should be type of inputs", u, sum.getUnit());
        double[][] sumValues = sum.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            for (int j = 0; j < in1[0].length; j++)
                assertEquals("Each element should equal the sum of the contributing elements", in1[i][j] + in2[i][j],
                        sumValues[i][j], 0.0001);
        DoubleMatrixRel<MassUnit> difference = null;
        try
        {
            difference = DoubleMatrix.minus(fm1, fm2);
        }
        catch (ValueException exception1)
        {
            fail("Should be able to subtract DoubleMatrixRel from DoubleMatrixRel of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 2, difference.rows());
        assertEquals("Size of result should be size of inputs", 4, difference.columns());
        assertEquals("Type of result should be type of inputs", u, difference.getUnit());
        double[][] differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            for (int j = 0; j < in1[0].length; j++)
                assertEquals("Each element should equal the difference of the contributing elements", in1[i][j]
                        - in2[i][j], differenceValues[i][j], 0.0001);
        double[][] in3 = buildArray(2, 3, false, 0);
        DoubleMatrixRel<MassUnit> fm3 = null;
        try
        {
            fm3 = createDoubleMatrixRel(in3, u);
        }
        catch (ValueException exception1)
        {
            fail("Unexpected exception");
        }
        try
        {
            sum = DoubleMatrix.plus(fm1, fm3);
            fail("Adding DoubleMatrices of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            difference = DoubleMatrix.minus(fm1, fm3);
            fail("Subtracting DoubleMatrices of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        MassUnit u2 = MassUnit.OUNCE;
        try
        {
            fm2 = createDoubleMatrixRel(in2, u2);
        }
        catch (ValueException exception2)
        {
            fail("Unexpected exceptin");
        }
        try
        {
            sum = DoubleMatrix.plus(fm1, fm2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add DoubleMatrixRel to DoubleMatrixRel of same size");
        }
        assertTrue("Result should not be null", null != sum);
        assertEquals("Size of result should be size of inputs", 2, sum.rows());
        assertEquals("Size of result should be size of inputs", 4, sum.columns());
        assertEquals("Type of result should be type of first input", u, sum.getUnit());
        assertFalse("Type of result should be different of type of second input", u2 == sum.getUnit());
        sumValues = sum.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            for (int j = 0; j < in1[0].length; j++)
            {
                assertEquals("Each element should equal the weighted sum of the contributing elements", in1[i][j]
                        * 0.45359 + in2[i][j] * 0.028350, sumValues[i][j] * 0.45359, 0.001);
            }
        try
        {
            fm2 = createDoubleMatrixRel(in2, u2);
        }
        catch (ValueException exception1)
        {
            fail("Unexpected exception");
        }
        try
        {
            difference = DoubleMatrix.minus(fm1, fm2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add DoubleMatrixRel to DoubleMatrixRel of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 2, difference.rows());
        assertEquals("Size of result should be size of inputs", 4, difference.columns());
        assertEquals("Type of result should be type of first input", u, difference.getUnit());
        assertFalse("Type of result should be different of type of second input", u2 == difference.getUnit());
        differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            for (int j = 0; j < in1[0].length; j++)
                assertEquals("Each element should equal the weighted difference of the contributing elements",
                        in1[i][j] * 0.45359 - in2[i][j] * 0.028350, differenceValues[i][j] * 0.45359, 0.001);
    }

    /**
     * Test adding, subtracting and multiplication of DoubleMatrixAbs.
     */

    @Test
    public void absAbs()
    {
        double[][] in1 = buildArray(2, 4, false, 0);
        double[][] in2 = buildArray(2, 4, false, 0);
        MassUnit u = MassUnit.POUND;
        DoubleMatrixAbs<MassUnit> fm1 = null;
        DoubleMatrixAbs<MassUnit> fm2 = null;
        try
        {
            fm1 = createDoubleMatrixAbs(in1, u);
            fm2 = createDoubleMatrixAbs(in2, u);
        }
        catch (ValueException exception2)
        {
            fail("Unexpected exception");
        }
        DoubleMatrixRel<MassUnit> difference = null;
        try
        {
            difference = DoubleMatrix.minus(fm1, fm2);
        }
        catch (ValueException exception1)
        {
            fail("Should be able to subtract DoubleMatrixAbs from DoubleMatrixAbs of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 2, difference.rows());
        assertEquals("Size of result should be size of inputs", 4, difference.columns());
        assertEquals("Type of result should be type of inputs", u, difference.getUnit());
        double[][] differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            for (int j = 0; j < in1[0].length; j++)
                assertEquals("Each element should equal the difference of the contributing elements", in1[i][j]
                        - in2[i][j], differenceValues[i][j], 0.0001);
        double[][] in3 = buildArray(2, 3, false, 0);
        DoubleMatrixAbs<MassUnit> fm3 = null;
        try
        {
            fm3 = createDoubleMatrixAbs(in3, u);
        }
        catch (ValueException exception2)
        {
            fail("Unexpected exception");
        }
        try
        {
            difference = DoubleMatrix.minus(fm1, fm3);
            fail("Subtracting DoubleMatrices of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        MassUnit u2 = MassUnit.OUNCE;
        try
        {
            fm2 = createDoubleMatrixAbs(in2, u2);
        }
        catch (ValueException exception2)
        {
            fail("Unexpected exception");
        }
        try
        {
            difference = DoubleMatrix.minus(fm1, fm2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to subtract DoubleMatrixAbs to DoubleMatrixAbs of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 2, difference.rows());
        assertEquals("Size of result should be size of inputs", 4, difference.columns());
        assertEquals("Type of result should be type of first input", u, difference.getUnit());
        assertFalse("Type of result should be different of type of second input", u2 == difference.getUnit());
        differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            for (int j = 0; j < in1[0].length; j++)
                assertEquals("Each element should equal the weighted difference of the contributing elements",
                        in1[i][j] * 0.45359 - in2[i][j] * 0.028350, differenceValues[i][j] * 0.45359, 0.002);
        try
        {
            difference = DoubleMatrix.minus(fm2, fm1);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add DoubleMatrixAbs to DoubleMatrixAbs of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 2, difference.rows());
        assertEquals("Size of result should be size of inputs", 4, difference.columns());
        assertEquals("Type of result should be type of first input", u2, difference.getUnit());
        assertFalse("Type of result should be different of type of second input", u == difference.getUnit());
        differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            for (int j = 0; j < in1[i].length; j++)
                assertEquals("Each element should equal the weighted difference of the contributing elements",
                        in2[i][j] * 0.028350 - in1[i][j] * 0.45359, differenceValues[i][j] * 0.028350, 0.02);
        LengthUnit u4 = LengthUnit.INCH;
        ForceUnit u5 = ForceUnit.POUND_FORCE;
        DoubleMatrixAbs<LengthUnit> fm4 = null;
        DoubleMatrixAbs<ForceUnit> fm5 = null;
        try
        {
            fm4 = createDoubleMatrixAbs(in1, u4);
            fm5 = createDoubleMatrixAbs(in2, u5);
        }
        catch (ValueException exception1)
        {
            fail("Unexpected exception");
        }
        Unit<EnergyUnit> resultUnit = EnergyUnit.CALORIE_IT;
        DoubleMatrixAbs<?> product = null;
        try
        {
            product = DoubleMatrix.multiply(fm4, fm5);
        }
        catch (ValueException exception)
        {
            fail("Should be able to multiply DoubleMatrixAbs with DoubleMatrixAbs of same size");
        }
        assertTrue("Result should not be null", null != product);
        assertEquals("Size of result should be size of inputs", 2, product.rows());
        assertEquals("Size of result should be size of inputs", 4, product.columns());
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
            for (int j = 0; j < in1[i].length; j++)
                try
                {
                    assertEquals("Value in product should be product of contributing values",
                            fm4.getSI(i, j) * fm5.getSI(i, j), product.getSI(i, j), 0.00001);
                }
                catch (ValueException exception)
                {
                    fail("Unexpected ValueException");
                }
        try
        {
            DoubleMatrixAbs<LengthUnit> lhs = createDoubleMatrixAbs(buildArray(2, 3, false, 0.5), LengthUnit.METER);
            double rhs[][] = {{1, 2, 3}, {4, 5, 6}};
            DoubleMatrix<LengthUnit> result = DoubleMatrix.multiply(lhs, rhs);
            assertTrue("Result should not be null", null != result);
            assertEquals("Result should have 2 rows", 2, result.rows());
            assertEquals("Result should have 3 columns", 3, result.columns());
            for (int row = 0; row < 2; row++)
                for (int column = 0; column < 3; column++)
                    assertEquals("Cell should contain product of contributing cell values", lhs.get(row, column)
                            .getValueSI() * rhs[row][column], result.get(row, column).getValueSI(), 0.0001);
        }
        catch (ValueException exception)
        {
            fail("Unexpected ValueException");
        }
        try
        {
            DoubleMatrixRel<LengthUnit> lhs = createDoubleMatrixRel(buildArray(2, 3, false, 0.5), LengthUnit.METER);
            double rhs[][] = {{1, 2, 3}, {4, 5, 6}};
            DoubleMatrix<LengthUnit> result = DoubleMatrix.multiply(lhs, rhs);
            assertTrue("Result should not be null", null != result);
            assertEquals("Result should have 2 rows", 2, result.rows());
            assertEquals("Result should have 3 columns", 3, result.columns());
            for (int row = 0; row < 2; row++)
                for (int column = 0; column < 3; column++)
                    assertEquals("Cell should contain product of contributing cell values", lhs.get(row, column)
                            .getValueSI() * rhs[row][column], result.get(row, column).getValueSI(), 0.0001);
        }
        catch (ValueException exception)
        {
            fail("Unexpected ValueException");
        }
        try
        {
            DoubleMatrixAbs<LengthUnit> lhs = createDoubleMatrixAbs(buildArray(2, 3, false, 0.5), LengthUnit.METER);
            double rhs[][] = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
            DoubleMatrix.multiply(lhs, rhs);
            fail("Should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // Ignore
        }
        try
        {
            DoubleMatrixRel<LengthUnit> lhs = createDoubleMatrixRel(buildArray(2, 3, false, 0.5), LengthUnit.METER);
            double rhs[][] = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
            DoubleMatrix.multiply(lhs, rhs);
            fail("Should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // Ignore
        }
    }

    /**
     * Test adding, subtracting of absolute and relative matrix
     */
    @Test
    public void absRel()
    {
        double[][] in1 = buildArray(2, 4, false, 0);
        double[][] in2 = buildArray(2, 4, false, 10);
        MassUnit u = MassUnit.POUND;
        DoubleMatrixAbs<MassUnit> fm1 = null;
        DoubleMatrixRel<MassUnit> fm2 = null;
        try
        {
            fm1 = createDoubleMatrixAbs(in1, u);
            fm2 = createDoubleMatrixRel(in2, u);
        }
        catch (ValueException exception2)
        {
            fail("Unexpected exception");
        }
        DoubleMatrixAbs<MassUnit> difference = null;
        try
        {
            difference = DoubleMatrix.minus(fm1, fm2);
        }
        catch (ValueException exception1)
        {
            fail("Should be able to subtract FloatMatrixAbs from FloatMatrixAbs of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 2, difference.rows());
        assertEquals("Size of result should be size of inputs", 4, difference.columns());
        assertEquals("Type of result should be type of inputs", u, difference.getUnit());
        double[][] differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            for (int j = 0; j < in1[0].length; j++)
                assertEquals("Each element should equal the difference of the contributing elements", in1[i][j]
                        - in2[i][j], differenceValues[i][j], 0.0001);
        double[][] in3 = buildArray(2, 3, false, 0);
        DoubleMatrixRel<MassUnit> fm3 = null;
        try
        {
            fm3 = createDoubleMatrixRel(in3, u);
        }
        catch (ValueException exception2)
        {
            fail("Unexpected exception");
        }
        try
        {
            difference = DoubleMatrix.minus(fm1, fm3);
            fail("Subtracting FloatMatrices of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        MassUnit u2 = MassUnit.OUNCE;
        try
        {
            fm2 = createDoubleMatrixRel(in2, u2);
        }
        catch (ValueException exception2)
        {
            fail("Unexpected exception");
        }
        try
        {
            difference = DoubleMatrix.minus(fm1, fm2);
        }
        catch (ValueException exception)
        {
            fail("Should be able to add FloatMatrixAbs to FloatMatrixAbs of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 2, difference.rows());
        assertEquals("Size of result should be size of inputs", 4, difference.columns());
        assertEquals("Type of result should be type of first input", u, difference.getUnit());
        assertFalse("Type of result should be different of type of second input", u2 == difference.getUnit());
        differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
            for (int j = 0; j < in1[0].length; j++)
                assertEquals("Each element should equal the weighted difference of the contributing elements",
                        in1[i][j] * 0.45359 - in2[i][j] * 0.028350, differenceValues[i][j] * 0.45359, 0.002);
    }

    /**
     * Test the DoubleMatrixRelDense that takes a double[] as argument.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void doubleMatrixRel2()
    {
        DoubleMatrix<LengthUnit> fsa = null;
        DoubleScalarRel<LengthUnit>[][] in = new DoubleScalarRel[0][0];
        try
        {
            fsa = createDoubleMatrixRel(in);
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        in = new DoubleScalarRel[1][1];
        in[0][0] = new DoubleScalarRel<LengthUnit>(123.456f, LengthUnit.FOOT);
        try
        {
            fsa = new DoubleMatrixRelDense<LengthUnit>(in);
        }
        catch (ValueException exception)
        {
            fail("Should NOT have thrown an exception");
        }
        double[][] out = fsa.getValuesInUnit();
        assertTrue("Result of getValuesInUnit should not be null", null != out);
        assertEquals("Array of values should have length 1", 1, out.length);
        assertEquals("Element in array should have the expected value", 123.456f, out[0][0], 0.001);
    }

    /**
     * Version of createDoubleMatrix for use with guaranteed rectangular data
     * @param in double[][] with values MUST BE RECTANGULAR
     * @param u Unit; type for the new DoubleMatrix
     * @param absolute Boolean; true to create a DoubleMatrixAbs; false to create a DoubleMatrixRel
     * @return DoubleMatrix
     */
    private <U extends Unit<U>> DoubleMatrix<U> safeCreateDoubleMatrix(double[][] in, U u, boolean absolute)
    {
        DoubleMatrix<U> result = null;
        try
        {
            result = createDoubleMatrix(in, u, absolute);
        }
        catch (ValueException exception)
        {
            fail("Caught unexpected ValueException in safeCreateDoubleMatrix");
        }
        return result;
    }

    /**
     * Create a DoubleMatrixAbs or a DoubleMatrixRel from an array of double values and Unit
     * @param in double[][] with values
     * @param u Unit; type for the new DoubleMatrix
     * @param absolute Boolean; true to create a DoubleMatrixAbs; false to create a DoubleMatrixRel
     * @return DoubleMatrix
     * @throws ValueException
     */
    private <U extends Unit<U>> DoubleMatrix<U> createDoubleMatrix(double[][] in, U u, boolean absolute)
            throws ValueException
    {
        if (absolute)
            return createDoubleMatrixAbs(in, u);
        else
            return createDoubleMatrixRel(in, u);
    }

    /**
     * Create a new DoubleMatrixAbs from an array of double values and Unit.
     * @param in double[][] with values
     * @param u Unit; type for the new DoubleMatrixAbs
     * @return
     * @throws ValueException when the array is not rectangular
     */
    protected abstract <U extends Unit<U>> DoubleMatrixAbs<U> createDoubleMatrixAbs(double[][] in, U u)
            throws ValueException;

    /**
     * Create a new DoubleMatrixAbs from an array of DoubleScalarAbs values.
     * @param in DoubleScalarAbs[][]; the values
     * @return
     * @throws ValueException when the array is empty or not rectangular
     */
    protected abstract <U extends Unit<U>> DoubleMatrixAbs<U> createDoubleMatrixAbs(DoubleScalarAbs<U>[][] in)
            throws ValueException;

    /**
     * Create a new DoubleMatrixRel from an array of double values and Unit.
     * @param in double[][] with values
     * @param u Unit; type for the new DoubleMatrixRel
     * @return
     * @throws ValueException when the array is not rectangular
     */
    protected abstract <U extends Unit<U>> DoubleMatrixRel<U> createDoubleMatrixRel(double[][] in, U u)
            throws ValueException;

    /**
     * Create a new DoubleMatrixRel from an array of DoubleScalarRel values.
     * @param in DoubleScalarAbs[][]; the values
     * @return
     * @throws ValueException when the array is empty or not rectangular
     */
    protected abstract <U extends Unit<U>> DoubleMatrixRel<U> createDoubleMatrixRel(DoubleScalarRel<U>[][] in)
            throws ValueException;

}

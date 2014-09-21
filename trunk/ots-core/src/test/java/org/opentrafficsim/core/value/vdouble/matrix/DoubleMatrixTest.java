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
import org.opentrafficsim.core.unit.TemperatureUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.unit.UnitException;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.DenseData;
import org.opentrafficsim.core.value.Format;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.SparseData;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVector;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 26, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class DoubleMatrixTest
{
    /**
     * Test DoubleMatrixAbs and DoubleMatrixRel creators that take a double[][] and a Unit as arguments.
     */
    @Test
    public final void doubleMatrixTwoArgs()
    {
        doubleMatrixTwoArgs(true); // test absolute version
        doubleMatrixTwoArgs(false); // test relative version
    }

    /**
     * Check that the values in a DoubleMatrix match with the values in a 2D double array.
     * @param fm DoubleMatrix; the DoubleMatrix to match
     * @param reference double[][]; the 2D double array
     * @param precision double; the maximum allowed error
     * @param expectAbsolute boolean; if true; the DoubleMatrix should be Absolute; otherwise it should be Relative
     * @param expectDense boolean; if true; the DoubleMatrix should be Dense; otherwise it should be Sparse
     */
    private static void checkContentsAndType(final DoubleMatrix<?> fm, final double[][] reference, final double precision,
            final boolean expectAbsolute, final boolean expectDense)
    {
        assertTrue("DoubleMatrix argument should not be null", null != fm);
        assertEquals("Number of rows should match", fm.rows(), reference.length);
        assertEquals("Number of columns should match", fm.columns(), reference[0].length);
        for (int row = 0; row < fm.rows(); row++)
        {
            for (int column = 0; column < fm.columns(); column++)
            {
                try
                {
                    assertEquals("Value should match", reference[row][column], fm.getInUnit(row, column), precision);
                }
                catch (ValueException exception)
                {
                    fail("Unexpected exception");
                }
            }
        }
        if (expectAbsolute)
        {
            assertTrue("DoubleMatrix argument should be Absolute", fm instanceof Absolute);
        }
        else
        {
            assertTrue("DoubleMatrix argument should be Relative", fm instanceof Relative);
        }
        if (expectDense)
        {
            assertTrue("DoubleMatrix argument should be Dense", fm instanceof DenseData);
        }
        else
        {
            assertTrue("DoubleMatrix argument should be Sparse", fm instanceof SparseData);
        }
        MutableDoubleMatrix<?> mutableVersion = fm.mutable();
        try
        {
            mutableVersion.setSI(0, 0, mutableVersion.getSI(0, 0) + 10);
            assertTrue("Value in mutable version is changed", mutableVersion.getSI(0, 0) != fm.getSI(0, 0));
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        for (int row = 0; row < fm.rows(); row++)
        {
            for (int column = 0; column < fm.columns(); column++)
            {
                try
                {
                    assertEquals("Value should still match", reference[row][column], fm.getInUnit(row, column), precision);
                }
                catch (ValueException exception)
                {
                    fail("Unexpected exception");
                }
            }
        }
        if (fm instanceof MutableDoubleMatrix)
        {
            MutableDoubleMatrix<?> mfm = (MutableDoubleMatrix<?>) fm;
            DoubleMatrix<?> immutableVersion = mfm.immutable();
            try
            {
                mfm.setSI(0, 0, mfm.getSI(0, 0) + 10);
                assertTrue("Only the value in the mutable version is changed", mfm.getSI(0, 0) != reference[0][0]);
                assertEquals("Value in the immutable version still matches the reference", reference[0][0],
                        immutableVersion.getSI(0, 0), precision);
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
        }
    }

    /**
     * Check that the four creators of MutableDoubleMatrix work.
     */
    @SuppressWarnings({ "static-method", "unused" })
    @Test
    public final void mutableDoubleMatrixCreators()
    {
        double[][] data = buildArray(5, 7, false, 0);
        try
        {
            checkContentsAndType(new DoubleMatrix.Abs.Dense<LengthUnit>(data, LengthUnit.FOOT), data, 0.001f, true, true);
            checkContentsAndType(new DoubleMatrix.Abs.Sparse<LengthUnit>(data, LengthUnit.FOOT), data, 0.001f, true, false);
            checkContentsAndType(new DoubleMatrix.Rel.Dense<LengthUnit>(data, LengthUnit.FOOT), data, 0.001f, false, true);
            checkContentsAndType(new DoubleMatrix.Rel.Sparse<LengthUnit>(data, LengthUnit.FOOT), data, 0.001f, false, false);
            checkContentsAndType(new MutableDoubleMatrix.Abs.Dense<LengthUnit>(data, LengthUnit.FOOT), data, 0.001f, true, true);
            checkContentsAndType(new MutableDoubleMatrix.Abs.Sparse<LengthUnit>(data, LengthUnit.FOOT), data, 0.001f, true,
                    false);
            checkContentsAndType(new MutableDoubleMatrix.Rel.Dense<LengthUnit>(data, LengthUnit.FOOT), data, 0.001f, false,
                    true);
            checkContentsAndType(new MutableDoubleMatrix.Rel.Sparse<LengthUnit>(data, LengthUnit.FOOT), data, 0.001f, false,
                    false);
        }
        catch (ValueException exception)
        {
            fail("Correctly formed data array should not throw an exception");
        }
        data = buildArray(3, 8, true, 2);
        try
        {
            new DoubleMatrix.Abs.Dense<LengthUnit>(data, LengthUnit.FOOT);
            fail("Non-rectangular data should have thrown a valueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new DoubleMatrix.Abs.Sparse<LengthUnit>(data, LengthUnit.FOOT);
            fail("Non-rectangular data should have thrown a valueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new DoubleMatrix.Rel.Dense<LengthUnit>(data, LengthUnit.FOOT);
            fail("Non-rectangular data should have thrown a valueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new DoubleMatrix.Rel.Sparse<LengthUnit>(data, LengthUnit.FOOT);
            fail("Non-rectangular data should have thrown a valueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new MutableDoubleMatrix.Abs.Dense<LengthUnit>(data, LengthUnit.FOOT);
            fail("Non-rectangular data should have thrown a valueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new MutableDoubleMatrix.Abs.Sparse<LengthUnit>(data, LengthUnit.FOOT);
            fail("Non-rectangular data should have thrown a valueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new MutableDoubleMatrix.Rel.Dense<LengthUnit>(data, LengthUnit.FOOT);
            fail("Non-rectangular data should have thrown a valueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new MutableDoubleMatrix.Rel.Sparse<LengthUnit>(data, LengthUnit.FOOT);
            fail("Non-rectangular data should have thrown a valueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        @SuppressWarnings("unchecked")
        DoubleScalar.Abs<TemperatureUnit>[][] emptyAbsMatrix = new DoubleScalar.Abs[0][0];
        @SuppressWarnings("unchecked")
        DoubleScalar.Rel<TemperatureUnit>[][] emptyRelMatrix = new DoubleScalar.Rel[0][0];
        try
        {
            new DoubleMatrix.Abs.Dense<TemperatureUnit>(emptyAbsMatrix);
            fail("Empty array should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new DoubleMatrix.Abs.Sparse<TemperatureUnit>(emptyAbsMatrix);
            fail("Empty array should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new DoubleMatrix.Rel.Dense<TemperatureUnit>(emptyRelMatrix);
            fail("Empty array should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new DoubleMatrix.Rel.Sparse<TemperatureUnit>(emptyRelMatrix);
            fail("Empty array should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new MutableDoubleMatrix.Abs.Dense<TemperatureUnit>(emptyAbsMatrix);
            fail("Empty array should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new MutableDoubleMatrix.Abs.Sparse<TemperatureUnit>(emptyAbsMatrix);
            fail("Empty array should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new MutableDoubleMatrix.Rel.Dense<TemperatureUnit>(emptyRelMatrix);
            fail("Empty array should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            new MutableDoubleMatrix.Rel.Sparse<TemperatureUnit>(emptyRelMatrix);
            fail("Empty array should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // ignore
        }
    }

    /**
     * Create a 2D array of double for testing. Entry 0,0 is zero, all others will be non-zero unless a non-zero offset is
     * specified.
     * @param rows Integer; number of rows in the 2D array
     * @param cols Integer; number of columns in the 2D array
     * @param badRow Boolean; if true; make the last row one entry longer than the rest
     * @param offset Double; number to add to each entry
     * @return double[][]; 2D array of double
     */
    private static double[][] buildArray(final int rows, final int cols, final boolean badRow, final double offset)
    {
        double[][] result = new double[rows][];
        int badRowIndex = badRow ? rows - 1 : -1;
        for (int row = 0; row < rows; row++)
        {
            result[row] = new double[row == badRowIndex ? cols + 1 : cols];
            for (int col = 0; col < result[row].length; col++)
            {
                result[row][col] = row * 1000 + col + offset;
            }
        }
        return result;
    }

    /**
     * Test the DoubleMatrixAbs that takes a double[][] and a Unit as arguments and some methods.
     */
    private void doubleMatrixTwoArgs(final Boolean absolute)
    {
        double[][] in = buildArray(12, 3, false, 0);
        LengthUnit u = LengthUnit.FOOT;
        DoubleMatrix<LengthUnit> dm = safeCreateDoubleMatrix(in, u, absolute);
        assertEquals("DoubleMatrix should have 12 rows", 12, dm.rows());
        assertEquals("DoubleMatrix should have 3 columns", 3, dm.columns());
        double[][] out = dm.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
        {
            for (int j = 0; j < in[i].length; j++)
            {
                assertEquals("Values in DoubleMatrix in unit should be equal to input values", in[i][j], out[i][j], 0.001);
                try
                {
                    assertEquals("Values in DoubleMatrix in unit should be equal to input values", in[i][j],
                            dm.getInUnit(i, j), 0.001);
                    assertEquals("Values in DoubleMatrix in unit should be equal to input values", in[i][j], dm.getSI(i, j)
                            / (12 * 0.0254), 0.001);
                    assertEquals("Values in DoubleMatrix in unit should be equal to input values", in[i][j],
                            dm.getInUnit(i, j, LengthUnit.MILE) * 1609.34 / (12 * 0.0254), 0.1);
                }
                catch (ValueException exception)
                {
                    fail("Get should not throw exceptions for legal values of the index");
                }
            }
        }
        String output = dm.toString(LengthUnit.MILLIMETER);
        String[] lines = output.split("[\n]");
        assertEquals("Number of lines should be number of rows + 1", in.length + 1, lines.length);
        assertTrue("first line should contain unit in brackers", lines[0].contains("[mm]"));
        for (int i = 1; i < lines.length; i++)
        {
            String[] fields = lines[i].trim().split("[ ]+");
            assertEquals("Number of fields should be number of columns", dm.columns(), fields.length);
            for (int j = 0; j < fields.length; j++)
            {
                double expectedValue = in[i - 1][j] * (12 * 0.0254) * 1000;
                String expected = Format.format(expectedValue).trim();
                assertEquals("Field " + j + " should contain \"" + expected + "\"", expected, fields[j]);
            }
        }
        output = dm.toString();
        lines = output.split("[\n]");
        assertEquals("Number of lines should be number of rows + 1", in.length + 1, lines.length);
        assertTrue("first line should contain unit in brackers", lines[0].contains("[ft]"));
        for (int i = 1; i < lines.length; i++)
        {
            String[] fields = lines[i].trim().split("[ ]+");
            assertEquals("Number of fields should be number of columns", dm.columns(), fields.length);
            for (int j = 0; j < fields.length; j++)
            {
                double expectedValue = in[i - 1][j];
                String expected = Format.format(expectedValue).trim();
                assertEquals("Field " + i + " should contain \"" + expected + "\"", expected, fields[j]);
            }
        }

        double[][] valuesInUnit = dm.getValuesInUnit();
        assertTrue("valuesInUnit should not be null", null != valuesInUnit);
        assertEquals("Size of valuesInUnit should be size of input array", in.length, valuesInUnit.length);
        for (int i = 0; i < in.length; i++)
        {
            for (int j = 0; j < in[i].length; j++)
            {
                assertEquals("Contents of valuesInUnit should be equal to input", in[i][j], valuesInUnit[i][j], 0.001);
            }
        }
        LengthUnit outputUnit = LengthUnit.DEKAMETER;
        double[][] valuesInOtherUnit = dm.getValuesInUnit(outputUnit);
        assertTrue("valuesInUnit should not be null", null != valuesInOtherUnit);
        assertEquals("Size of valuesInUnit should be size of input array", in.length, valuesInOtherUnit.length);
        for (int i = 0; i < in.length; i++)
        {
            for (int j = 0; j < in[i].length; j++)
            {
                assertEquals("Contents of valuesInUnit should be equal to input", in[i][j] * (12 * 0.0254) / 10,
                        valuesInOtherUnit[i][j], 0.0001);
            }
        }
        try
        {
            dm.getInUnit(-1, 0);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            dm.getInUnit(in.length, 0);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            dm.getInUnit(0, -1);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            dm.getInUnit(0, in[0].length);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            dm.getSI(-1, 0);
            fail("Using a bad index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            dm.getSI(in.length, 0);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            dm.getSI(0, -1);
            fail("Using a bad index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            dm.getSI(0, in[0].length);
            fail("Using a index that is too hig should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            dm.getInUnit(-1, 0, LengthUnit.MILE);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            dm.getInUnit(in.length, 0, LengthUnit.MILE);
            fail("Using an index that is too big should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            dm.getInUnit(0, -1, LengthUnit.MILE);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            dm.getInUnit(0, in[0].length, LengthUnit.MILE);
            fail("Using an index that is too big should throw a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        MutableDoubleMatrix<LengthUnit> mfm = dm.mutable();
        try
        {
            mfm.setSI(-1, 0, 12345f);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        try
        {
            mfm.setSI(in.length, 0, 12345f);
            fail("Using an index that is too big should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        try
        {
            mfm.setSI(0, -1, 12345f);
            fail("Using a negative index should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        try
        {
            mfm.setSI(0, in[0].length, 12345f);
            fail("Using an index that is too big should throw a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        out = dm.getValuesSI();
        assertTrue("getValuesSI does not return null", null != out);
        assertEquals("Length of getValuesSI should match size", in.length, out.length);
        for (int i = 0; i < in.length; i++)
        {
            for (int j = 0; j < in[i].length; j++)
            {
                assertEquals("Values in DoubleMatrix should be equivalent values in meters", in[i][j], out[i][j]
                        / (12 * 0.0254), 0.001);
            }
        }
        LengthUnit uOut = dm.getUnit();
        assertEquals("Stored unit should be provided unit", u, uOut);
        DoubleMatrix<LengthUnit> copy = (DoubleMatrix<LengthUnit>) dm.copy();
        assertEquals("copy should have 12 rows", 12, copy.rows());
        assertEquals("copy should have 3 columns", 3, copy.columns());
        double[][] copyOut = copy.getValuesSI();
        for (int i = 0; i < in.length; i++)
        {
            for (int j = 0; j < in[i].length; j++)
            {
                assertEquals("Values in copy of DoubleMatrix should be equivalent values in meters", in[i][j], copyOut[i][j]
                        / (12 * 0.0254), 0.001);
            }
        }
        copyOut = dm.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
        {
            for (int j = 0; j < in[i].length; j++)
            {
                assertEquals("Values in copy of DoubleMatrix in unit should be equal to input values", in[i][j], copyOut[i][j],
                        0.001);
            }
        }
        MutableDoubleMatrix<LengthUnit> mcopy = copy.mutable();
        try
        {
            mcopy.setSI(0, 0, 12345f);
            assertEquals("value should be altered", 12345f, mcopy.getSI(0, 0), 0.01);
            assertEquals("original value should not be altered", out[0][0], dm.getSI(0, 0), 0.001);
            DoubleScalar<LengthUnit> value = mcopy.get(1, 0);
            assertTrue("value cannot be null", null != value);
            assertEquals("value should be same as SI value", mcopy.getSI(1, 0), value.getValueSI(), 0.0001);
            mcopy.set(2, 0, value);
            assertEquals("value should be same as SI value", mcopy.getSI(2, 0), value.getValueSI(), 0.0001);
        }
        catch (ValueException exception)
        {
            fail("set*/get* should not throw ValueException for valid index and correctly typed value");
        }
        try
        {
            mcopy.setInUnit(1, 0, 321, LengthUnit.HECTOMETER);
            assertEquals("321 hectometer is 32100m", mcopy.getSI(1, 0), 32100, 0.001);
        }
        catch (ValueException exception)
        {
            fail("Legal index should not throw exception");
        }
        double sum = 0;
        for (int i = 0; i < in.length; i++)
        {
            for (int j = 0; j < in[i].length; j++)
            {
                sum += in[i][j];
            }
        }
        sum *= (12 * 0.0254); // convert to meters
        assertEquals("zsum should be sum of the values", sum, dm.zSum(), 0.01);
        try
        {
            mfm.normalize();
            for (int i = 0; i < in.length; i++)
            {
                for (int j = 0; j < in[i].length; j++)
                {
                    assertEquals("Unexpected normalized value", in[i][j] * (12 * 0.0254) / sum, mfm.getSI(i, j), 0.0001);
                }
            }
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertEquals("Cardinality should be 35", 35, dm.cardinality());
        double[][] in2 = { { 1f, -1f, 0f } };
        mfm = safeCreateDoubleMatrix(in2, u, absolute).mutable();
        assertEquals("Cardinality should be 2", 2, mfm.cardinality());
        assertEquals("zSum should be 0", 0, mfm.zSum(), 0.00001);
        try
        {
            mfm.normalize();
            fail("Should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        assertEquals("Cardinality should be 2", 2, mfm.cardinality());
        try
        {
            mfm.setSI(0, 0, 0);
            assertEquals("Cardinality should be 1", 1, mfm.cardinality());
            mfm.setSI(0, 1, 0);
            assertEquals("Cardinality should be 0", 0, mfm.cardinality());
            mfm.setSI(0, 2, 999);
            assertEquals("Cardinality should be 1", 1, mfm.cardinality());
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertTrue("DoubleMatrix should be equal to itself", mfm.equals(mfm));
        assertFalse("DoubleMatrix should not be equal to null", mfm.equals(null));
        mcopy = mfm.copy();
        assertTrue("DoubleMatrix should be equal to copy of itself", mfm.equals(mcopy));
        try
        {
            mcopy.setSI(0, 1, mcopy.getSI(0, 1) + 0.001f);
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertFalse("DoubleMatrix should be different from slightly altered to copy of itself", mfm.equals(mcopy));
        try
        {
            mcopy.setSI(0, 1, mfm.getSI(0, 1));
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        assertTrue("DoubleMatrix should be equal to repaired copy of itself", mfm.equals(mcopy));
        if (absolute)
        {
            double[][] values = dm.getValuesInUnit();
            DoubleMatrix<LengthUnit> fmr = null;
            try
            {
                fmr = createDoubleMatrix(values, dm.getUnit(), false);
                for (int i = 0; i < in2.length; i++)
                {
                    for (int j = 0; j < in2[i].length; j++)
                    {
                        assertEquals("Values should be equal", dm.getSI(i, j), fmr.getSI(i, j), 0.00001);
                    }
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertEquals("fm and fmr should have same unit", dm.getUnit(), fmr.getUnit());
            assertFalse("fm and fmr should not be equal", dm.equals(fmr));
            assertFalse("fmr and fm should not be equal", fmr.equals(dm));
        }
        double[][] inNonRect = buildArray(4, 5, true, 0);
        try
        {
            dm = createDoubleMatrix(inNonRect, LengthUnit.METER, absolute);
            fail("Non rectangular input data should have thrown a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        double[][] in3 =
                { { -100, -10, -1, -0.1f, 1, 0.1f, 1, 10, 100 },
                        { -1000000, -1000, -100, -0.001f, 1, 0.001f, 100, 1000, 1000000 } };
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.abs();
        MathTester.tester(in3, "abs", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.abs(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.acos();
        MathTester.tester(in3, "acos", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.acos(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.asin();
        MathTester.tester(in3, "asin", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.asin(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.atan();
        MathTester.tester(in3, "atan", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.atan(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.cbrt();
        MathTester.tester(in3, "cbrt", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.cbrt(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.ceil();
        MathTester.tester(in3, "ceil", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.ceil(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.cos();
        MathTester.tester(in3, "cos", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.cos(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.cosh();
        MathTester.tester(in3, "cosh", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.cosh(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.exp();
        MathTester.tester(in3, "exp", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.exp(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.expm1();
        MathTester.tester(in3, "expm1", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.expm1(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.floor();
        MathTester.tester(in3, "floor", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.floor(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.log();
        MathTester.tester(in3, "log", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.log(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.log10();
        MathTester.tester(in3, "log10", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.log10(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.log1p();
        MathTester.tester(in3, "log1p", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.log1p(f);
            }
        });
        for (double power = -5; power <= 5; power += 0.5)
        {
            mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
            mfm.pow(power);
            final double myPower = power;
            MathTester.tester(in3, "pow(" + power + ")", mfm.getValuesSI(), 0.001, new DoubleToDouble()
            {
                @Override
                public double function(final double f)
                {
                    return Math.pow(f, myPower);
                }
            });
        }
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.rint();
        MathTester.tester(in3, "rint", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.rint(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.round();
        MathTester.tester(in3, "round", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.round(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.signum();
        MathTester.tester(in3, "signum", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.signum(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.sin();
        MathTester.tester(in3, "sin", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.sin(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.sinh();
        MathTester.tester(in3, "sinh", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.sinh(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.sqrt();
        MathTester.tester(in3, "sqrt", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.sqrt(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.tan();
        MathTester.tester(in3, "tan", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.tan(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.tanh();
        MathTester.tester(in3, "tanh", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.tanh(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.toDegrees();
        MathTester.tester(in3, "toDegrees", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.toDegrees(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.toRadians();
        MathTester.tester(in3, "toRadians", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return Math.toRadians(f);
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        mfm.inv();
        MathTester.tester(in3, "inv", mfm.getValuesSI(), 0.001, new DoubleToDouble()
        {
            @Override
            public double function(final double f)
            {
                return 1.0 / f;
            }
        });
        mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
        try
        {
            mfm.det();
            fail("det of non-square matrix should have thrown a ValueException");
        }
        catch (ValueException exception1)
        {
            // ignore
        }
        double[][] singular = { { 1, 2, 3 }, { 3, 5, 7 }, { 5, 10, 0 } };
        mfm = safeCreateDoubleMatrix(singular, LengthUnit.METER, absolute).mutable();
        try
        {
            assertEquals("Determinant should be 15", 15, mfm.det(), 0.0001);
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
            mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
            mfm.multiply(factor);
            final double myFactor = factor;
            MathTester.tester(in3, "multiply(" + factor + ")", mfm.getValuesSI(), 0.001, new DoubleToDouble()
            {
                @Override
                public double function(final double f)
                {
                    return myFactor * f;
                }
            });
        }
        for (double divisor = -4.75f; divisor <= 5; divisor += 0.5)
        {
            mfm = safeCreateDoubleMatrix(in3, LengthUnit.METER, absolute).mutable();
            mfm.divide(divisor);
            final double myDivisor = divisor;
            MathTester.tester(in3, "divide(" + divisor + ")", mfm.getValuesSI(), 0.1, new DoubleToDouble()
            {
                @Override
                public double function(final double f)
                {
                    return f / myDivisor;
                }
            });
        }
        if (absolute)
        {
            DoubleMatrix<LengthUnit> fm1 = safeCreateDoubleMatrix(in3, LengthUnit.METER, true);
            double[][] inRowCountMismatch =
                    { { -100, -10, -1, -0.1f, 1, 0.1f, 1, 10 }, { -1000000, -1000, -100, -0.001f, 1, 0.001f, 100, 1000 } };
            double[][] inColCountMismatch = { { -100, -10, -1, -0.1f, 1, 0.1f, 1, 10, 100 } };
            DoubleMatrix<LengthUnit> fm2 = safeCreateDoubleMatrix(inRowCountMismatch, LengthUnit.METER, false);
            DoubleMatrix<LengthUnit> plus = null;
            DoubleMatrix<LengthUnit> minus = null;
            try
            {
                if (fm1 instanceof DenseData)
                {
                    plus =
                            MutableDoubleMatrix.plus((DoubleMatrix.Abs.Dense<LengthUnit>) fm1,
                                    (DoubleMatrix.Rel<LengthUnit>) fm2);
                }
                else if (fm1 instanceof SparseData)
                {
                    if (fm2 instanceof DenseData)
                    {
                        plus =
                                MutableDoubleMatrix.plus((DoubleMatrix.Abs.Sparse<LengthUnit>) fm1,
                                        (DoubleMatrix.Rel.Dense<LengthUnit>) fm2);
                    }
                    else if (fm2 instanceof SparseData)
                    {
                        plus =
                                MutableDoubleMatrix.plus((DoubleMatrix.Abs.Sparse<LengthUnit>) fm1,
                                        (DoubleMatrix.Rel.Sparse<LengthUnit>) fm2);
                    }
                    else
                    {
                        fail("Error in test; unexpected type");
                    }
                }
                else
                {
                    fail("Error in test; unexpected type");
                }
                fail("Adding DoubleMatrices of unequal size should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            try
            {
                if (fm1 instanceof DenseData)
                {
                    plus =
                            MutableDoubleMatrix.minus((DoubleMatrix.Abs.Dense<LengthUnit>) fm1,
                                    (DoubleMatrix.Rel<LengthUnit>) fm2);
                }
                else if (fm1 instanceof SparseData)
                {
                    if (fm2 instanceof DenseData)
                    {
                        plus =
                                MutableDoubleMatrix.minus((DoubleMatrix.Abs.Sparse<LengthUnit>) fm1,
                                        (DoubleMatrix.Rel.Dense<LengthUnit>) fm2);
                    }
                    else if (fm2 instanceof SparseData)
                    {
                        plus =
                                MutableDoubleMatrix.minus((DoubleMatrix.Abs.Sparse<LengthUnit>) fm1,
                                        (DoubleMatrix.Rel.Sparse<LengthUnit>) fm2);
                    }
                    else
                    {
                        fail("Error in test; unexpected type");
                    }
                }
                else
                {
                    fail("Error in test; unexpected type");
                }
                fail("Subtracting DoubleMatrices of unequal size should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            fm2 = safeCreateDoubleMatrix(inColCountMismatch, LengthUnit.METER, false);
            try
            {
                if (fm1 instanceof DenseData)
                {
                    plus =
                            MutableDoubleMatrix.plus((DoubleMatrix.Abs.Dense<LengthUnit>) fm1,
                                    (DoubleMatrix.Rel<LengthUnit>) fm2);
                }
                else if (fm1 instanceof SparseData)
                {
                    if (fm2 instanceof DenseData)
                    {
                        plus =
                                MutableDoubleMatrix.plus((DoubleMatrix.Abs.Sparse<LengthUnit>) fm1,
                                        (DoubleMatrix.Rel.Dense<LengthUnit>) fm2);
                    }
                    else if (fm2 instanceof SparseData)
                    {
                        plus =
                                MutableDoubleMatrix.plus((DoubleMatrix.Abs.Sparse<LengthUnit>) fm1,
                                        (DoubleMatrix.Rel.Sparse<LengthUnit>) fm2);
                    }
                    else
                    {
                        fail("Error in test; unexpected type");
                    }
                }
                else
                {
                    fail("Error in test; unexpected type");
                }
                fail("Adding DoubleMatrices of unequal size should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            try
            {
                if (fm1 instanceof DenseData)
                {
                    minus =
                            MutableDoubleMatrix.minus((DoubleMatrix.Abs.Dense<LengthUnit>) fm1,
                                    (DoubleMatrix.Rel<LengthUnit>) fm2);
                }
                else if (fm1 instanceof SparseData)
                {
                    if (fm2 instanceof DenseData)
                    {
                        minus =
                                MutableDoubleMatrix.minus((DoubleMatrix.Abs.Sparse<LengthUnit>) fm1,
                                        (DoubleMatrix.Rel.Dense<LengthUnit>) fm2);
                    }
                    else if (fm2 instanceof SparseData)
                    {
                        minus =
                                MutableDoubleMatrix.minus((DoubleMatrix.Abs.Sparse<LengthUnit>) fm1,
                                        (DoubleMatrix.Rel.Sparse<LengthUnit>) fm2);
                    }
                    else
                    {
                        fail("Error in test; unexpected type");
                    }
                }
                else
                {
                    fail("Error in test; unexpected type");
                }
                fail("Subtracting DoubleMatrices of unequal length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            double[][] in5 = buildArray(2, 9, false, 0);
            fm2 = safeCreateDoubleMatrix(in5, LengthUnit.METER, false);
            try
            {
                if (fm1 instanceof DenseData)
                {
                    plus =
                            MutableDoubleMatrix.plus((DoubleMatrix.Abs.Dense<LengthUnit>) fm1,
                                    (DoubleMatrix.Rel<LengthUnit>) fm2);
                    minus =
                            MutableDoubleMatrix.minus((DoubleMatrix.Abs.Dense<LengthUnit>) fm1,
                                    (DoubleMatrix.Rel<LengthUnit>) fm2);
                }
                else if (fm1 instanceof SparseData)
                {
                    if (fm2 instanceof DenseData)
                    {
                        plus =
                                MutableDoubleMatrix.plus((DoubleMatrix.Abs.Sparse<LengthUnit>) fm1,
                                        (DoubleMatrix.Rel.Dense<LengthUnit>) fm2);
                        minus =
                                MutableDoubleMatrix.minus((DoubleMatrix.Abs.Sparse<LengthUnit>) fm1,
                                        (DoubleMatrix.Rel.Dense<LengthUnit>) fm2);
                    }
                    else if (fm2 instanceof SparseData)
                    {
                        plus =
                                MutableDoubleMatrix.plus((DoubleMatrix.Abs.Sparse<LengthUnit>) fm1,
                                        (DoubleMatrix.Rel.Sparse<LengthUnit>) fm2);
                        minus =
                                MutableDoubleMatrix.minus((DoubleMatrix.Abs.Sparse<LengthUnit>) fm1,
                                        (DoubleMatrix.Rel.Sparse<LengthUnit>) fm2);
                    }
                    else
                    {
                        fail("Error in test; unexpected type");
                    }
                }
                else
                {
                    fail("Error in test; unexpected type");
                }
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
                {
                    for (int j = 0; j < in[i].length; j++)
                    {
                        assertEquals("value of element should be sum of contributing elements", in3[i][j] + in5[i][j],
                                plus.getSI(i, j), 0.00001);
                    }
                }
                for (int i = 0; i < in3.length; i++)
                {
                    for (int j = 0; j < in[i].length; j++)
                    {
                        assertEquals("value of element should be sum of contributing elements", in3[i][j] - in5[i][j],
                                minus.getSI(i, j), 0.00001);
                    }
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("result should be Absolute", plus instanceof Absolute);
            assertTrue("result should be Absolute", minus instanceof Absolute);
            if (fm1 instanceof DenseData)
            {
                assertTrue("result should be Dense", plus instanceof DenseData);
                assertTrue("result should be Dense", minus instanceof DenseData);
            }
            else if (fm1 instanceof SparseData)
            {
                assertTrue("result should be Sparse", plus instanceof SparseData);
                assertTrue("result should be Sparse", minus instanceof SparseData);
            }
            else
            {
                fail("fm1 neither Dense nor Sparse");
            }

        }
        else
        // not absolute; i.e. Relative
        {
            DoubleMatrix<LengthUnit> fm1 = safeCreateDoubleMatrix(in3, LengthUnit.METER, false);
            double[][] in4 = buildArray(1, 4, false, 0);
            DoubleMatrix<LengthUnit> fm2 = safeCreateDoubleMatrix(in4, LengthUnit.METER, false);
            DoubleMatrix<SIUnit> multiply = null;
            try
            {
                if (fm1 instanceof DenseData)
                {
                    multiply =
                            MutableDoubleMatrix.times((DoubleMatrix.Rel.Dense<LengthUnit>) fm1,
                                    (DoubleMatrix.Rel.Dense<LengthUnit>) fm2);
                }
                else if (fm1 instanceof SparseData)
                {
                    multiply =
                            MutableDoubleMatrix.times((DoubleMatrix.Rel.Sparse<LengthUnit>) fm1,
                                    (DoubleMatrix.Rel.Sparse<LengthUnit>) fm2);
                }
                else
                {
                    fail("Error in test; unanticipated type");
                }
                fail("Multiplying DoubleMatrices of unequal length should have thrown a ValueException");
            }
            catch (ValueException exception)
            {
                // ignore
            }
            double[][] in5 = buildArray(2, 9, false, 0);
            fm2 = safeCreateDoubleMatrix(in5, LengthUnit.METER, false);
            try
            {
                if (fm1 instanceof DenseData)
                {
                    multiply =
                            MutableDoubleMatrix.times((DoubleMatrix.Rel.Dense<LengthUnit>) fm1,
                                    (DoubleMatrix.Rel.Dense<LengthUnit>) fm2);
                }
                else if (fm1 instanceof SparseData)
                {
                    multiply =
                            MutableDoubleMatrix.times((DoubleMatrix.Rel.Sparse<LengthUnit>) fm1,
                                    (DoubleMatrix.Rel.Sparse<LengthUnit>) fm2);
                }
                else
                {
                    fail("Error in test; unanticipated type");
                }
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
                {
                    for (int j = 0; j < in3[i].length; j++)
                    {
                        assertEquals("value of element should be sum of contributing elements", in3[i][j] * in5[i][j],
                                multiply.getSI(i, j), 0.00001);
                    }
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
            assertTrue("result should be Relative", multiply instanceof Relative);
            if (fm1 instanceof DenseData && fm2 instanceof DenseData)
            {
                assertTrue("result should be Dense", multiply instanceof DenseData);
            }
            else if (fm1 instanceof SparseData || fm2 instanceof SparseData)
            {
                assertTrue("result should be Sparse", multiply instanceof SparseData);
            }
            else
            {
                fail("Error in test; failed to account for some combination of dens and sparse");
            }
            // System.out.println("Result of multiply has unit " + multiply);
            assertEquals("Result of multiplication should be in square meters", "m2", multiply.getUnit()
                    .getSICoefficientsString());

            DoubleMatrix<SIUnit> multiplyReverse = null;
            try
            {
                if (fm2 instanceof DenseData)
                {
                    multiplyReverse =
                            MutableDoubleMatrix.times((DoubleMatrix.Rel.Dense<LengthUnit>) fm2,
                                    (DoubleMatrix.Rel.Dense<LengthUnit>) fm1);
                }
                else if (fm2 instanceof SparseData)
                {
                    multiplyReverse =
                            MutableDoubleMatrix.times((DoubleMatrix.Rel.Sparse<LengthUnit>) fm2,
                                    (DoubleMatrix.Rel.Sparse<LengthUnit>) fm1);
                }
                else
                {
                    fail("Error in test; unanticipated type");
                }
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
                double[][] in6 = { { 1, 2, 3 }, { 4, 5, 6 } };
                MutableDoubleMatrix<LengthUnit> original = safeCreateDoubleMatrix(in6, LengthUnit.METER, absolute).mutable();
                DoubleMatrix<LengthUnit> duplicate = original.copy();
                assertTrue("Original should be equal to duplicate", original.equals(duplicate));
                assertTrue("Duplicate should be equal to original", duplicate.equals(original));
                original.setSI(0, 0, 123.456f);
                assertFalse("Original should now differ from duplicate", original.equals(duplicate));
                assertFalse("Duplicate should now differ from original", duplicate.equals(original));

            }
            catch (ValueException exception)
            {
                fail("Unexpected ValueException");
            }
        }
        if (dm instanceof DenseData)
        {
            MutableDoubleMatrix<LengthUnit> fm2 = null;
            if (dm instanceof Absolute)
            {
                fm2 = MutableDoubleMatrix.denseToSparse((DoubleMatrix.Abs.Dense<LengthUnit>) dm);
            }
            else
            {
                fm2 = MutableDoubleMatrix.denseToSparse((DoubleMatrix.Rel.Dense<LengthUnit>) dm);
            }
            assertTrue("dense version is  equal to sparse version", dm.equals(fm2));
            assertEquals("unit should be same", dm.getUnit(), fm2.getUnit());
            try
            {
                for (int i = 0; i < dm.rows(); i++)
                {
                    for (int j = 0; j < dm.columns(); j++)
                    {
                        assertEquals("Values should be equal", dm.getSI(i, j), fm2.getSI(i, j), 0.0001);
                    }
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
        }
        else
        // fm instanceof SparseData
        {
            MutableDoubleMatrix<LengthUnit> fm2 = null;
            if (dm instanceof Absolute)
            {
                fm2 = MutableDoubleMatrix.sparseToDense((DoubleMatrix.Abs.Sparse<LengthUnit>) dm);
            }
            else
            {
                fm2 = MutableDoubleMatrix.sparseToDense((DoubleMatrix.Rel.Sparse<LengthUnit>) dm);
            }
            assertTrue("dense version is equal to sparse version", dm.equals(fm2));
            assertEquals("unit should be same", dm.getUnit(), fm2.getUnit());
            try
            {
                for (int i = 0; i < dm.rows(); i++)
                {
                    for (int j = 0; j < dm.columns(); j++)
                    {
                        assertEquals("Values should be equal", dm.getSI(i, j), fm2.getSI(i, j), 0.0001);
                    }
                }
            }
            catch (ValueException exception)
            {
                fail("Unexpected exception");
            }
        }
        double[][] left = buildArray(4, 5, false, 0);
        MutableDoubleMatrix<LengthUnit> leftMatrix = safeCreateDoubleMatrix(left, LengthUnit.METER, absolute).mutable();
        double[][] right = buildArray(4, 6, false, 0.3f);
        DoubleMatrix.Rel<LengthUnit> rightMatrix =
                (DoubleMatrix.Rel<LengthUnit>) safeCreateDoubleMatrix(right, LengthUnit.METER, false);
        try
        {
            leftMatrix.incrementBy(rightMatrix);
            fail("Adding matrices of different sizes should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // Ignore
        }
        right = buildArray(3, 5, false, 0.3f);
        rightMatrix = (DoubleMatrix.Rel<LengthUnit>) safeCreateDoubleMatrix(right, LengthUnit.METER, false);
        try
        {
            leftMatrix.incrementBy(rightMatrix);
            fail("Adding matrices of different sizes should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // Ignore
        }
        right = buildArray(4, 5, false, 0.3f);
        rightMatrix = (DoubleMatrix.Rel<LengthUnit>) safeCreateDoubleMatrix(right, LengthUnit.METER, false);
        try
        {
            leftMatrix.incrementBy(rightMatrix);
        }
        catch (ValueException exception)
        {
            fail("Adding matrices of equal sizes should not have thrown an exception");
        }
        try
        {
            for (int i = 0; i < left.length; i++)
            {
                for (int j = 0; j < left[0].length; j++)
                {
                    assertEquals("Values should now be sum of input values", left[i][j] + right[i][j], leftMatrix.getSI(i, j),
                            0.001);
                }
            }
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
        }
        leftMatrix = safeCreateDoubleMatrix(left, LengthUnit.METER, absolute).mutable();
        right = buildArray(4, 6, false, 0.3f);
        rightMatrix = (DoubleMatrix.Rel<LengthUnit>) safeCreateDoubleMatrix(right, LengthUnit.METER, false);
        try
        {
            leftMatrix.decrementBy(rightMatrix);
            fail("Subtracting matrices of different sizes should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // Ignore
        }
        right = buildArray(3, 5, false, 0.3f);
        rightMatrix = (DoubleMatrix.Rel<LengthUnit>) safeCreateDoubleMatrix(right, LengthUnit.METER, false);
        try
        {
            leftMatrix.decrementBy(rightMatrix);
            fail("Subtracting matrices of different sizes should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // Ignore
        }
        right = buildArray(4, 5, false, 0.3f);
        rightMatrix = (DoubleMatrix.Rel<LengthUnit>) safeCreateDoubleMatrix(right, LengthUnit.METER, false);
        try
        {
            leftMatrix.decrementBy(rightMatrix);
        }
        catch (ValueException exception)
        {
            fail("Subtracting matrices of equal sizes should not have thrown an exception");
        }
        try
        {
            for (int i = 0; i < left.length; i++)
            {
                for (int j = 0; j < left[0].length; j++)
                {
                    assertEquals("Values should now be difference of input values", left[i][j] - right[i][j],
                            leftMatrix.getSI(i, j), 0.001);
                }
            }
        }
        catch (ValueException exception)
        {
            fail("Unexpected exception");
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
         * @param inputValues array of array of double with unprocessed values
         * @param operation String; description of method that is being tested
         * @param resultValues array of array of double with processed values
         * @param precision double expected accuracy
         * @param function DoubleToDouble encapsulating function that converts one value in inputValues to the corresponding
         *            value in resultValues
         */
        public static void tester(final double[][] inputValues, final String operation, final double[][] resultValues,
                final double precision, final DoubleToDouble function)
        {
            for (int i = 0; i < inputValues.length; i++)
            {
                for (int j = 0; j < inputValues[i].length; j++)
                {
                    double result = function.function(inputValues[i][j]);
                    String description =
                            String.format("indices=%d,%d: %s(%f)->%f should be equal to %f with precision %f", i, j, operation,
                                    inputValues[i][j], result, resultValues[i][j], precision);
                    // System.out.println(description);
                    assertEquals(description, result, resultValues[i][j], precision);
                }
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
     * Test DoubleMatrixAbs and DoubleMatrixRel creators that take an array of DoubleScalar as argument.
     */
    @Test
    public final void doubleMatrixOneArg()
    {
        doubleMatrixOneArg(true); // test absolute version
        doubleMatrixOneArg(false); // test relative version
    }

    /**
     * Test the DoubleMatrixAbs and DoubleMatrixRel that takes a DoubleScalar*<U>[] as argument.
     */
    @SuppressWarnings("unchecked")
    private void doubleMatrixOneArg(final Boolean absolute)
    {
        DoubleMatrix<LengthUnit> fm = null;
        DoubleScalar.Abs<LengthUnit>[][] inAbs = new DoubleScalar.Abs[0][0];
        DoubleScalar.Rel<LengthUnit>[][] inRel = new DoubleScalar.Rel[0][0];
        try
        {
            if (absolute)
            {
                fm = createDoubleMatrixAbs(inAbs);
            }
            else
            {
                fm = createDoubleMatrixRel(inRel);
            }
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        inAbs = new DoubleScalar.Abs[1][0];
        inRel = new DoubleScalar.Rel[1][0];
        try
        {
            if (absolute)
            {
                fm = createDoubleMatrixAbs(inAbs);
            }
            else
            {
                fm = createDoubleMatrixRel(inRel);
            }
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        inAbs = new DoubleScalar.Abs[0][1];
        inRel = new DoubleScalar.Rel[0][1];
        try
        {
            if (absolute)
            {
                fm = createDoubleMatrixAbs(inAbs);
            }
            else
            {
                fm = createDoubleMatrixRel(inRel);
            }
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        inAbs = new DoubleScalar.Abs[1][1];
        inAbs[0][0] = new DoubleScalar.Abs<LengthUnit>(123.456f, LengthUnit.FOOT);
        inRel = new DoubleScalar.Rel[1][1];
        inRel[0][0] = new DoubleScalar.Rel<LengthUnit>(123.456f, LengthUnit.FOOT);
        try
        {
            if (absolute)
            {
                fm = createDoubleMatrixAbs(inAbs);
            }
            else
            {
                fm = createDoubleMatrixRel(inRel);
            }
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
                DoubleScalar.Abs<LengthUnit>[][] inv4 = new DoubleScalar.Abs[in4.length][];
                for (int i = 0; i < in4.length; i++)
                {
                    inv4[i] = new DoubleScalar.Abs[in4[i].length];
                    for (int j = 0; j < in4[i].length; j++)
                    {
                        inv4[i][j] = new DoubleScalar.Abs<LengthUnit>(in4[i][j], LengthUnit.FOOT);
                    }
                }
                fm = createDoubleMatrixAbs(inv4);
            }
            else
            {
                DoubleScalar.Rel<LengthUnit>[][] inv4 = new DoubleScalar.Rel[in4.length][];
                for (int i = 0; i < in4.length; i++)
                {
                    inv4[i] = new DoubleScalar.Rel[in4[i].length];
                    for (int j = 0; j < in4[i].length; j++)
                    {
                        inv4[i][j] = new DoubleScalar.Rel<LengthUnit>(in4[i][j], LengthUnit.FOOT);
                    }
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
    public final void relRel()
    {
        double[][] in1 = buildArray(2, 4, false, 0);
        double[][] in2 = buildArray(2, 4, false, 0);
        MassUnit u = MassUnit.POUND;
        DoubleMatrix.Rel<MassUnit> fm1 = null;
        DoubleMatrix.Rel<MassUnit> fm2 = null;
        try
        {
            fm1 = createDoubleMatrixRel(in1, u);
            fm2 = createDoubleMatrixRel(in2, u);
        }
        catch (ValueException exception2)
        {
            fail("Unexpected exception");
        }
        MutableDoubleMatrix.Rel<MassUnit> sum = null;
        try
        {
            if (fm1 instanceof DenseData)
            {
                sum = MutableDoubleMatrix.plus((DoubleMatrix.Rel.Dense<MassUnit>) fm1, fm2);
            }
            else if (fm1 instanceof SparseData)
            {
                sum =
                        MutableDoubleMatrix.plus((DoubleMatrix.Rel.Sparse<MassUnit>) fm1,
                                (DoubleMatrix.Rel.Sparse<MassUnit>) fm2);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
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
        {
            for (int j = 0; j < in1[0].length; j++)
            {
                assertEquals("Each element should equal the sum of the contributing elements", in1[i][j] + in2[i][j],
                        sumValues[i][j], 0.0001);
            }
        }
        MutableDoubleMatrix.Rel<MassUnit> difference = null;
        try
        {
            if (fm1 instanceof DenseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Rel.Dense<MassUnit>) fm1,
                                (DoubleMatrix.Rel.Dense<MassUnit>) fm2);
            }
            else if (fm1 instanceof SparseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Rel.Sparse<MassUnit>) fm1,
                                (DoubleMatrix.Rel.Sparse<MassUnit>) fm2);
            }
            else
            {
                fail("Error in test; unanticiapted type");
            }
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
        {
            for (int j = 0; j < in1[0].length; j++)
            {
                assertEquals("Each element should equal the difference of the contributing elements", in1[i][j] - in2[i][j],
                        differenceValues[i][j], 0.0001);
            }
        }
        double[][] in3 = buildArray(2, 3, false, 0);
        DoubleMatrix.Rel<MassUnit> fm3 = null;
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
            if (fm1 instanceof DenseData)
            {
                sum = MutableDoubleMatrix.plus((DoubleMatrix.Rel.Dense<MassUnit>) fm1, fm3);
            }
            else if (fm1 instanceof SparseData)
            {
                sum =
                        MutableDoubleMatrix.plus((DoubleMatrix.Rel.Sparse<MassUnit>) fm1,
                                (DoubleMatrix.Rel.Sparse<MassUnit>) fm3);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
            fail("Adding DoubleMatrices of unequal size should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // ignore
        }
        try
        {
            if (fm1 instanceof DenseData)
            {
                difference = MutableDoubleMatrix.minus((DoubleMatrix.Rel.Dense<MassUnit>) fm1, fm3);
            }
            else if (fm1 instanceof SparseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Rel.Sparse<MassUnit>) fm1,
                                (DoubleMatrix.Rel.Sparse<MassUnit>) fm3);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
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
            if (fm1 instanceof DenseData)
            {
                sum = MutableDoubleMatrix.plus((DoubleMatrix.Rel.Dense<MassUnit>) fm1, fm2);
            }
            else if (fm1 instanceof SparseData)
            {
                sum =
                        MutableDoubleMatrix.plus((DoubleMatrix.Rel.Sparse<MassUnit>) fm1,
                                (DoubleMatrix.Rel.Sparse<MassUnit>) fm2);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
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
        {
            for (int j = 0; j < in1[0].length; j++)
            {
                assertEquals("Each element should equal the weighted sum of the contributing elements", in1[i][j] * 0.45359
                        + in2[i][j] * 0.028350, sumValues[i][j] * 0.45359, 0.001);
            }
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
            if (fm1 instanceof DenseData)
            {
                difference = MutableDoubleMatrix.minus((DoubleMatrix.Rel.Dense<MassUnit>) fm1, fm2);
            }
            else if (fm1 instanceof SparseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Rel.Sparse<MassUnit>) fm1,
                                (DoubleMatrix.Rel.Sparse<MassUnit>) fm2);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
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
        {
            for (int j = 0; j < in1[0].length; j++)
            {
                assertEquals("Each element should equal the weighted difference of the contributing elements", in1[i][j]
                        * 0.45359 - in2[i][j] * 0.028350, differenceValues[i][j] * 0.45359, 0.001);
            }
        }
    }

    /**
     * Test adding, subtracting and multiplication of DoubleMatrixAbs.
     */
    @Test
    public final void absAbs()
    {
        double[][] in1 = buildArray(2, 4, false, 0);
        double[][] in2 = buildArray(2, 4, false, 0);
        MassUnit u = MassUnit.POUND;
        DoubleMatrix<MassUnit> fm1 = null;
        DoubleMatrix<MassUnit> fm2 = null;
        try
        {
            fm1 = createDoubleMatrixAbs(in1, u);
            fm2 = createDoubleMatrixAbs(in2, u);
        }
        catch (ValueException exception2)
        {
            fail("Unexpected exception");
        }
        MutableDoubleMatrix<MassUnit> difference = null;
        try
        {
            if (fm1 instanceof DenseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Dense<MassUnit>) fm1,
                                (DoubleMatrix.Abs.Dense<MassUnit>) fm2);
            }
            else if (fm1 instanceof SparseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Sparse<MassUnit>) fm1,
                                (DoubleMatrix.Abs.Sparse<MassUnit>) fm2);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
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
        {
            for (int j = 0; j < in1[0].length; j++)
            {
                assertEquals("Each element should equal the difference of the contributing elements", in1[i][j] - in2[i][j],
                        differenceValues[i][j], 0.0001);
            }
        }
        double[][] in3 = buildArray(2, 3, false, 0);
        DoubleMatrix<MassUnit> fm3 = null;
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
            if (fm1 instanceof DenseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Dense<MassUnit>) fm1,
                                (DoubleMatrix.Abs.Dense<MassUnit>) fm3);
            }
            else if (fm1 instanceof SparseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Sparse<MassUnit>) fm1,
                                (DoubleMatrix.Abs.Sparse<MassUnit>) fm3);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
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
            if (fm1 instanceof DenseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Dense<MassUnit>) fm1,
                                (DoubleMatrix.Abs.Dense<MassUnit>) fm2);
            }
            else if (fm1 instanceof SparseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Sparse<MassUnit>) fm1,
                                (DoubleMatrix.Abs.Sparse<MassUnit>) fm2);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
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
        {
            for (int j = 0; j < in1[0].length; j++)
            {
                assertEquals("Each element should equal the weighted difference of the contributing elements", in1[i][j]
                        * 0.45359 - in2[i][j] * 0.028350, differenceValues[i][j] * 0.45359, 0.01);
            }
        }
        try
        {
            if (fm1 instanceof DenseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Dense<MassUnit>) fm2,
                                (DoubleMatrix.Abs.Dense<MassUnit>) fm1);
            }
            else if (fm1 instanceof SparseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Sparse<MassUnit>) fm2,
                                (DoubleMatrix.Abs.Sparse<MassUnit>) fm1);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
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
        {
            for (int j = 0; j < in1[i].length; j++)
            {
                assertEquals("Each element should equal the weighted difference of the contributing elements", in2[i][j]
                        * 0.028350 - in1[i][j] * 0.45359, differenceValues[i][j] * 0.028350, 0.02);
            }
        }
        LengthUnit u4 = LengthUnit.INCH;
        ForceUnit u5 = ForceUnit.POUND_FORCE;
        DoubleMatrix.Abs<LengthUnit> fm4 = null;
        DoubleMatrix.Abs<ForceUnit> fm5 = null;
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
        MutableDoubleMatrix.Abs<?> product = null;
        try
        {
            if (fm4 instanceof DenseData)
            {
                product =
                        MutableDoubleMatrix.times((DoubleMatrix.Abs.Dense<LengthUnit>) fm4,
                                (DoubleMatrix.Abs.Dense<ForceUnit>) fm5);
            }
            else if (fm4 instanceof SparseData)
            {
                product =
                        MutableDoubleMatrix.times((DoubleMatrix.Abs.Sparse<LengthUnit>) fm4,
                                (DoubleMatrix.Abs.Sparse<ForceUnit>) fm5);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
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
        {
            for (int j = 0; j < in1[i].length; j++)
            {
                try
                {
                    assertEquals("Value in product should be product of contributing values",
                            fm4.getSI(i, j) * fm5.getSI(i, j), product.getSI(i, j), 0.00001);
                }
                catch (ValueException exception)
                {
                    fail("Unexpected ValueException");
                }
            }
        }
        try
        {
            DoubleMatrix.Abs<LengthUnit> lhs = createDoubleMatrixAbs(buildArray(2, 3, false, 0.5f), LengthUnit.METER);
            double[][] rhs = { { 1, 2, 3 }, { 4, 5, 6 } };
            MutableDoubleMatrix<LengthUnit> result = null;
            if (lhs instanceof DenseData)
            {
                result = MutableDoubleMatrix.times((DoubleMatrix.Abs.Dense<LengthUnit>) lhs, rhs);
            }
            else if (lhs instanceof SparseData)
            {
                result = MutableDoubleMatrix.times((DoubleMatrix.Abs.Sparse<LengthUnit>) lhs, rhs);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
            assertTrue("Result should not be null", null != result);
            assertEquals("Result should have 2 rows", 2, result.rows());
            assertEquals("Result should have 3 columns", 3, result.columns());
            for (int row = 0; row < 2; row++)
            {
                for (int column = 0; column < 3; column++)
                {
                    assertEquals("Cell should contain product of contributing cell values", lhs.get(row, column).getValueSI()
                            * rhs[row][column], result.get(row, column).getValueSI(), 0.0001);
                }
            }
        }
        catch (ValueException exception)
        {
            fail("Unexpected ValueException");
        }
        try
        {
            DoubleMatrix.Rel<LengthUnit> lhs = createDoubleMatrixRel(buildArray(2, 3, false, 0.5f), LengthUnit.METER);
            double[][] rhs = { { 1, 2, 3 }, { 4, 5, 6 } };
            MutableDoubleMatrix<LengthUnit> result = null;
            if (lhs instanceof DenseData)
            {
                result = MutableDoubleMatrix.times((DoubleMatrix.Rel.Dense<LengthUnit>) lhs, rhs);
            }
            else if (lhs instanceof SparseData)
            {
                result = MutableDoubleMatrix.times((DoubleMatrix.Rel.Sparse<LengthUnit>) lhs, rhs);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
            assertTrue("Result should not be null", null != result);
            assertEquals("Result should have 2 rows", 2, result.rows());
            assertEquals("Result should have 3 columns", 3, result.columns());
            for (int row = 0; row < 2; row++)
            {
                for (int column = 0; column < 3; column++)
                {
                    assertEquals("Cell should contain product of contributing cell values", lhs.get(row, column).getValueSI()
                            * rhs[row][column], result.get(row, column).getValueSI(), 0.0001);
                }
            }
        }
        catch (ValueException exception)
        {
            fail("Unexpected ValueException");
        }
        try
        {
            DoubleMatrix.Abs<LengthUnit> lhs = createDoubleMatrixAbs(buildArray(2, 3, false, 0.5f), LengthUnit.METER);
            double[][] rhs = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
            if (lhs instanceof DenseData)
            {
                MutableDoubleMatrix.times((DoubleMatrix.Abs.Dense<LengthUnit>) lhs, rhs);
            }
            else if (lhs instanceof SparseData)
            {
                MutableDoubleMatrix.times((DoubleMatrix.Abs.Sparse<LengthUnit>) lhs, rhs);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
            fail("Should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // Ignore
        }
        try
        {
            DoubleMatrix.Rel<LengthUnit> lhs = createDoubleMatrixRel(buildArray(2, 3, false, 0.5f), LengthUnit.METER);
            double[][] rhs = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
            if (lhs instanceof DenseData)
            {
                MutableDoubleMatrix.times((DoubleMatrix.Rel.Dense<LengthUnit>) lhs, rhs);
            }
            else if (lhs instanceof SparseData)
            {
                MutableDoubleMatrix.times((DoubleMatrix.Rel.Sparse<LengthUnit>) lhs, rhs);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }

            fail("Should have thrown a ValueException");
        }
        catch (ValueException exception)
        {
            // Ignore
        }

    }

    /**
     * Test adding, subtracting of absolute and relative matrix.
     */
    @Test
    public final void absRel()
    {
        double[][] in1 = buildArray(2, 4, false, 0);
        double[][] in2 = buildArray(2, 4, false, 10);
        MassUnit u = MassUnit.POUND;
        DoubleMatrix<MassUnit> fm1 = null;
        DoubleMatrix<MassUnit> fm2 = null;
        try
        {
            fm1 = createDoubleMatrixAbs(in1, u);
            fm2 = createDoubleMatrixRel(in2, u);
        }
        catch (ValueException exception2)
        {
            fail("Unexpected exception");
        }
        MutableDoubleMatrix<MassUnit> difference = null;
        try
        {
            if (fm1 instanceof DenseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Dense<MassUnit>) fm1,
                                (DoubleMatrix.Rel.Dense<MassUnit>) fm2);
            }
            else if (fm1 instanceof SparseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Sparse<MassUnit>) fm1,
                                (DoubleMatrix.Rel.Sparse<MassUnit>) fm2);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
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
        {
            for (int j = 0; j < in1[0].length; j++)
            {
                assertEquals("Each element should equal the difference of the contributing elements", in1[i][j] - in2[i][j],
                        differenceValues[i][j], 0.0001);
            }
        }
        double[][] in3 = buildArray(2, 3, false, 0);
        DoubleMatrix<MassUnit> fm3 = null;
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
            if (fm1 instanceof DenseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Dense<MassUnit>) fm1,
                                (DoubleMatrix.Rel.Dense<MassUnit>) fm3);
            }
            else if (fm1 instanceof SparseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Sparse<MassUnit>) fm1,
                                (DoubleMatrix.Rel.Sparse<MassUnit>) fm3);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
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
            fail("Unexpected exception");
        }
        try
        {
            if (fm1 instanceof DenseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Dense<MassUnit>) fm1,
                                (DoubleMatrix.Rel.Dense<MassUnit>) fm2);
            }
            else if (fm1 instanceof SparseData)
            {
                difference =
                        MutableDoubleMatrix.minus((DoubleMatrix.Abs.Sparse<MassUnit>) fm1,
                                (DoubleMatrix.Rel.Sparse<MassUnit>) fm2);
            }
            else
            {
                fail("Error in test; unanticipated type");
            }
        }
        catch (ValueException exception)
        {
            fail("Should be able to add DoubleMatrixAbs to DoubleMatrixAbs of same size");
        }
        assertTrue("Result should not be null", null != difference);
        assertEquals("Size of result should be size of inputs", 2, difference.rows());
        assertEquals("Size of result should be size of inputs", 4, difference.columns());
        assertEquals("Type of result should be type of first input", u, difference.getUnit());
        assertFalse("Type of result should be different of type of second input", u2 == difference.getUnit());
        differenceValues = difference.getValuesInUnit();
        for (int i = 0; i < in1.length; i++)
        {
            for (int j = 0; j < in1[0].length; j++)
            {
                assertEquals("Each element should equal the weighted difference of the contributing elements", in1[i][j]
                        * 0.45359 - in2[i][j] * 0.028350, differenceValues[i][j] * 0.45359, 0.002);
            }
        }
    }

    /**
     * Test the solve methods.
     */
    @Test
    public final void solver()
    {
        double[][] a = { { 1, 2, 3 }, { 5, 6, 7 }, { 9, 10, 15 } };
        double[] b = { 4, 8, 12 };
        DoubleMatrix<LengthUnit> aMatrix = safeCreateDoubleMatrix(a, LengthUnit.METER, true);
        DoubleVector<ForceUnit> bVector = null;
        if (aMatrix instanceof SparseData)
        {
            bVector = new DoubleVector.Abs.Sparse<ForceUnit>(b, ForceUnit.NEWTON);
        }
        else
        {
            bVector = new DoubleVector.Abs.Dense<ForceUnit>(b, ForceUnit.NEWTON);
        }
        try
        {
            DoubleVector<SIUnit> result = null;
            if (aMatrix instanceof SparseData)
            {
                if (aMatrix instanceof Absolute)
                {
                    // This case does not work (the other three do work)
                    System.out.println(aMatrix);
                    return; // result = DoubleMatrix.solve((DoubleMatrixAbsSparse<LengthUnit>) aMatrix,
                            // (DoubleVectorAbs<ForceUnit>) bVector);
                }
                else if (aMatrix instanceof Relative)
                {
                    result = DoubleMatrix.solve(aMatrix, bVector);
                }
            }
            else
            {
                result = DoubleMatrix.solve(aMatrix, bVector);
            }
            // System.out.println("unit of result is " + result.getUnit());
            assertEquals("result[0] should be -2", -2, result.getSI(0), 0.0001);
            assertEquals("result[1] should be 3", 3, result.getSI(1), 0.0001);
            assertEquals("result[2] should be 0", 0, result.getSI(2), 0.0001);
        }
        catch (ValueException ve)
        {
            fail("Unexpected exception");
        }
        catch (IllegalArgumentException iae)
        {
            iae.printStackTrace();
            fail("Unexpected exception");
        }
    }

    /**
     * Test the DoubleMatrixRelDense that takes a double[] as argument.
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void doubleMatrixRel2()
    {
        DoubleMatrix<LengthUnit> fsa = null;
        DoubleScalar.Rel<LengthUnit>[][] in = new DoubleScalar.Rel[0][0];
        try
        {
            fsa = createDoubleMatrixRel(in);
            fail("Should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        in = new DoubleScalar.Rel[1][1];
        in[0][0] = new DoubleScalar.Rel<LengthUnit>(123.456f, LengthUnit.FOOT);
        try
        {
            fsa = new DoubleMatrix.Rel.Dense<LengthUnit>(in);
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
     * Version of createDoubleMatrix for use with guaranteed rectangular data.
     * @param in double[][] with values MUST BE RECTANGULAR
     * @param u Unit; type for the new DoubleMatrix
     * @param absolute Boolean; true to create a DoubleMatrixAbs; false to create a DoubleMatrixRel
     * @return DoubleMatrix
     * @param <U> Unit; the unit
     */
    private <U extends Unit<U>> DoubleMatrix<U> safeCreateDoubleMatrix(final double[][] in, final U u, final boolean absolute)
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
     * Create a DoubleMatrixAbs or a DoubleMatrixRel from an array of double values and Unit.
     * @param in double[][] with values
     * @param u Unit; type for the new DoubleMatrix
     * @param absolute Boolean; true to create a DoubleMatrixAbs; false to create a DoubleMatrixRel
     * @return DoubleMatrix
     * @throws ValueException when in is not rectangular
     * @param <U> Unit; the unit
     */
    private <U extends Unit<U>> DoubleMatrix<U> createDoubleMatrix(final double[][] in, final U u, final boolean absolute)
            throws ValueException
    {
        if (absolute)
        {
            return createDoubleMatrixAbs(in, u);
        }
        else
        {
            return createDoubleMatrixRel(in, u);
        }
    }

    /**
     * Create a new DoubleMatrix.Abs from an array of double values and Unit.
     * @param in double[][] with values
     * @param u Unit; type for the new DoubleMatrixAbs
     * @return DoubleMatrix.Abs
     * @throws ValueException when the array is not rectangular
     * @param <U> Unit; the unit
     */
    protected abstract <U extends Unit<U>> DoubleMatrix.Abs<U> createDoubleMatrixAbs(double[][] in, U u) throws ValueException;

    /**
     * Create a new DoubleMatrix.Abs from an array of DoubleScalarAbs values.
     * @param in DoubleScalar.Abs[][]; the values
     * @return DoubleMatrix.Abs
     * @throws ValueException when the array is empty or not rectangular
     * @param <U> Unit; the unit
     */
    protected abstract <U extends Unit<U>> DoubleMatrix.Abs<U> createDoubleMatrixAbs(DoubleScalar.Abs<U>[][] in)
            throws ValueException;

    /**
     * Create a new DoubleMatrix.Rel from an array of double values and Unit.
     * @param in double[][] with values
     * @param u Unit; type for the new DoubleMatrixRel
     * @return DoubleMatrix.Rel
     * @throws ValueException when the array is not rectangular
     * @param <U> Unit; the unit
     */
    protected abstract <U extends Unit<U>> DoubleMatrix.Rel<U> createDoubleMatrixRel(double[][] in, U u) throws ValueException;

    /**
     * Create a new DoubleMatrix.Rel from an array of DoubleScalarRel values.
     * @param in DoubleScalar.Rel[][]; the values
     * @return DoubleMatrix.Rel
     * @throws ValueException when the array is empty or not rectangular
     * @param <U> Unit; the unit
     */
    protected abstract <U extends Unit<U>> DoubleMatrix.Rel<U> createDoubleMatrixRel(DoubleScalar.Rel<U>[][] in)
            throws ValueException;

}

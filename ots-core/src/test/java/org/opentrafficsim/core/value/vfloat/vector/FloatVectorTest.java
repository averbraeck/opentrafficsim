package org.opentrafficsim.core.value.vfloat.vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.MassUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalarAbs;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalarRel;

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
            fail("Using a negative index should throw a ValueException");
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
        out = fv.getValuesSI();
        for (int i = 0; i < in.length; i++)
            assertEquals("Values in floatVector should be equivalent values in meters", in[i], out[i] / (12 * 0.0254),
                    0.0001);
        LengthUnit uOut = fv.getUnit();
        assertEquals("Stored unit should be provided unit", u, uOut);
        FloatVector<LengthUnit> copy = fv.copy();
        assertEquals("copy should have 10 elements", 10, copy.size());
        float[] copyOut = copy.getValuesSI();
        for (int i = 0; i < in.length; i++)
            assertEquals("Values in copy of floatVector should be equivalent values in meters", in[i], copyOut[i]
                    / (12 * 0.0254), 0.0001);
        copyOut = fv.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
            assertEquals("Values in copy of floatVector in unit should be equal to input values", in[i], copyOut[i],
                    0.0001);
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
                    * 0.45359 - in2[i] * 0.028350, differenceValues[i] * 0.45359, 0.0001);
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

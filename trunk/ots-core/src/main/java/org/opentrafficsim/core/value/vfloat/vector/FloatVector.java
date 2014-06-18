package org.opentrafficsim.core.value.vfloat.vector;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.Vector;
import org.opentrafficsim.core.value.vfloat.FloatMathFunctions;
import org.opentrafficsim.core.value.vfloat.FloatMathFunctionsImpl;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.jet.math.tfloat.FloatFunctions;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.opentrafficsim.org/"> www.opentrafficsim.org</a>.
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
 * @version Jun 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> The unit for this value type
 */
public abstract class FloatVector<U extends Unit<U>> extends Vector<U> implements FloatMathFunctions
{
    /** */
    private static final long serialVersionUID = 20140618L;

    /** the internal storage for the vector; internally they are stored in SI units; can be dense or sparse */
    protected FloatMatrix1D vectorSI;

    /**
     * Construct the vector and store the values in SI units.
     * @param values an array of values for the constructor
     * @param unit the unit of the values
     */
    public FloatVector(float[] values, final U unit)
    {
        super(unit);
        if (unit.equals(unit.getStandardUnit()))
        {
            this.vectorSI = createMatrix1D(values.length);
            this.vectorSI.assign(values);
        }
        else
        {
            this.vectorSI = createMatrix1D(values.length);
            for (int index = 0; index < values.length; index++)
            {
                this.vectorSI.set(index, (float) convertToSIUnit(values[index]));
            }
        }
    }

    /**
     * Construct the vector and store the values in SI units.
     * @param values an array of values for the constructor
     * @throws ValueException exception thrown when array with zero elements is offered
     */
    public FloatVector(FloatScalar<U>[] values) throws ValueException
    {
        super(values.length > 0 ? values[0].getUnit() : null);
        if (values.length == 0)
        {
            throw new ValueException("FloatVector constructor called with an empty array of FloatScalar elements");
        }

        this.vectorSI = createMatrix1D(values.length);
        for (int index = 0; index < values.length; index++)
        {
            this.vectorSI.set(index, values[index].getValueSI());
        }
    }

    /**
     * This method has to be implemented by each leaf class.
     * @param size the number of cells in the vector
     * @return an instance of the right type of matrix (absolute /relative, dense / sparse, etc.).
     */
    protected abstract FloatMatrix1D createMatrix1D(int size);

    /**
     * @return values in SI units
     */
    public float[] getValuesSI()
    {
        return this.vectorSI.toArray();
    }

    /**
     * @return values in original units
     */
    public float[] getValuesInUnit()
    {
        float[] values = this.vectorSI.toArray();
        for (int i = 0; i < values.length; i++)
            values[i] = (float) convertToSpecifiedUnit(values[i]);
        return values;
    }

    /**
     * @param targetUnit the unit to convert the values to
     * @return values in specific target unit
     */
    public float[] getValuesInUnit(final U targetUnit)
    {
        float[] values = this.vectorSI.toArray();
        for (int i = 0; i < values.length; i++)
            values[i] = (float) convertToUnit(values[i], targetUnit);
        return values;
    }

    /**
     * @return the unit
     */
    public U getUnit()
    {
        return this.unit;
    }

    /**
     * @return the size of the vector as an int
     */
    public int size()
    {
        return (int) this.vectorSI.size();
    }

    /**
     * Create a deep copy of the vector, independent of the original vector.
     * @return a deep copy of the absolute / relative, dense / sparse vector
     */
    public abstract FloatVector<U> copy();

    /**********************************************************************************/
    /********************************** MATH METHODS **********************************/
    /**********************************************************************************/

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#abs()
     */
    @Override
    public void abs()
    {
        this.vectorSI.assign(FloatFunctions.abs);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#acos()
     */
    @Override
    public void acos()
    {
        this.vectorSI.assign(FloatFunctions.acos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#asin()
     */
    @Override
    public void asin()
    {
        this.vectorSI.assign(FloatFunctions.asin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#atan()
     */
    @Override
    public void atan()
    {
        this.vectorSI.assign(FloatFunctions.atan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cbrt()
     */
    @Override
    public void cbrt()
    {
        this.vectorSI.assign(FloatMathFunctionsImpl.cbrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#ceil()
     */
    @Override
    public void ceil()
    {
        this.vectorSI.assign(FloatFunctions.ceil);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cos()
     */
    @Override
    public void cos()
    {
        this.vectorSI.assign(FloatFunctions.cos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cosh()
     */
    @Override
    public void cosh()
    {
        this.vectorSI.assign(FloatMathFunctionsImpl.cosh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#exp()
     */
    @Override
    public void exp()
    {
        this.vectorSI.assign(FloatFunctions.exp);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#expm1()
     */
    @Override
    public void expm1()
    {
        this.vectorSI.assign(FloatMathFunctionsImpl.expm1);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#floor()
     */
    @Override
    public void floor()
    {
        this.vectorSI.assign(FloatFunctions.floor);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log()
     */
    @Override
    public void log()
    {
        this.vectorSI.assign(FloatFunctions.log);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log10()
     */
    @Override
    public void log10()
    {
        this.vectorSI.assign(FloatMathFunctionsImpl.log10);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log1p()
     */
    @Override
    public void log1p()
    {
        this.vectorSI.assign(FloatMathFunctionsImpl.log1p);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#pow(double)
     */
    @Override
    public void pow(double x)
    {
        this.vectorSI.assign(FloatFunctions.pow((float) x));
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#rint()
     */
    @Override
    public void rint()
    {
        this.vectorSI.assign(FloatFunctions.rint);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#round()
     */
    @Override
    public void round()
    {
        this.vectorSI.assign(FloatMathFunctionsImpl.round);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#signum()
     */
    @Override
    public void signum()
    {
        this.vectorSI.assign(FloatMathFunctionsImpl.signum);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sin()
     */
    @Override
    public void sin()
    {
        this.vectorSI.assign(FloatFunctions.sin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sinh()
     */
    @Override
    public void sinh()
    {
        this.vectorSI.assign(FloatMathFunctionsImpl.sinh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sqrt()
     */
    @Override
    public void sqrt()
    {
        this.vectorSI.assign(FloatFunctions.sqrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tan()
     */
    @Override
    public void tan()
    {
        this.vectorSI.assign(FloatFunctions.tan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tanh()
     */
    @Override
    public void tanh()
    {
        this.vectorSI.assign(FloatMathFunctionsImpl.tanh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toDegrees()
     */
    @Override
    public void toDegrees()
    {
        this.vectorSI.assign(FloatMathFunctionsImpl.toDegrees);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toRadians()
     */
    @Override
    public void toRadians()
    {
        this.vectorSI.assign(FloatMathFunctionsImpl.toRadians);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.FloatMathFunctions#multiply(float)
     */
    @Override
    public void multiply(float constant)
    {
        this.vectorSI.assign(FloatFunctions.mult(constant));
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.FloatMathFunctions#divide(float)
     */
    @Override
    public void divide(float constant)
    {
        this.vectorSI.assign(FloatFunctions.div(constant));
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return toString(this.unit);
    }

    /**
     * @param displayUnit the unit to display the vector in.
     * @return a printable String with the vector contents
     */
    public String toString(final U displayUnit)
    {
        // TODO: check how to always format numbers corresponding to the Locale used.
        String s = "[" + displayUnit.getAbbreviation() + "]";
        for (int i = 0; i < this.vectorSI.size(); i++)
        {
            float f = (float) convertToUnit(this.vectorSI.get(i), displayUnit);
            if (Math.abs(f) > 0.01 && Math.abs(f) < 999.0)
                s += " " + String.format("%8.3f", f);
            else
                s += " " + String.format("%8.3e", f);
        }
        return s;
    }

    /**********************************************************************************/
    /******************************* NON-STATIC METHODS *******************************/
    /**********************************************************************************/

    /**
     * Add another value to this value. Only relative values are allowed; adding an absolute value to an absolute value
     * is not allowed. Adding an absolute value to an existing relative value would require the result to become
     * absolute, which is a type change that is impossible. For that operation, use a static method.
     * @param vector the vector to add
     * @throws ValueException when vectors have unequal size
     */
    public void add(final FloatVectorRel<U> vector) throws ValueException
    {
        if (size() != vector.size())
            throw new ValueException("FloatVector.add - two vectors have unequal size: " + size() + " != "
                    + vector.size());
        this.vectorSI.assign(vector.vectorSI, FloatFunctions.plus);
    }

    /**
     * Subtract another value from this value. Only relative values are allowed; subtracting an absolute value from a
     * relative value is not allowed. Subtracting an absolute value from an existing absolute value would require the
     * result to become relative, which is a type change that is impossible. For that operation, use a static method.
     * @param vector the value to subtract
     * @throws ValueException when vectors have unequal size
     */
    public void subtract(FloatVectorRel<U> vector) throws ValueException
    {
        if (size() != vector.size())
            throw new ValueException("FloatVector.subtract - two vectors have unequal size: " + size() + " != "
                    + vector.size());
        this.vectorSI.assign(vector.vectorSI, FloatFunctions.minus);
    }

    /**********************************************************************************/
    /********************************* STATIC METHODS *********************************/
    /**********************************************************************************/

    /**
     * Add a vector with absolute values x and a vector with relative values y. The target unit will be the unit of
     * absolute value x.
     * @param x absolute vector 1
     * @param y relative vector 2
     * @return new Vector with absolute elements sum of x[i] and y[i]
     * @throws ValueException when vectors have unequal size
     */
    public static <U extends Unit<U>> FloatVectorAbs<U> plus(FloatVectorAbs<U> x, FloatVectorRel<U> y)
            throws ValueException
    {
        if (x.size() != y.size())
            throw new ValueException("FloatVector.plus - two vectors have unequal size: " + x.size() + " != "
                    + y.size());

        FloatVectorAbs<U> c = (FloatVectorAbs<U>) x.copy();
        c.add(y);
        return c;
    }

    /**
     * Add a vector with relative values x and a vector with absolute values y. The target unit will be the unit of
     * absolute value y.
     * @param x relative vector 1
     * @param y absolute vector 2
     * @param targetUnit unit in which the results will be displayed
     * @return new Vector with absolute elements sum of x[i] and y[i]
     * @throws ValueException when vectors have unequal size
     */
    public static <U extends Unit<U>> FloatVectorAbs<U> plus(FloatVectorRel<U> x, FloatVectorAbs<U> y)
            throws ValueException
    {
        return plus(y, x);
    }

    /**
     * Add a vector with relative values x and a vector with relative values y. The target unit will be the unit of
     * relative value x.
     * @param x relative vector 1
     * @param y relative vector 2
     * @return new Vector with absolute elements sum of x[i] and y[i]
     * @throws ValueException when vectors have unequal size
     */
    public static <U extends Unit<U>> FloatVectorRel<U> plus(FloatVectorRel<U> x, FloatVectorRel<U> y)
            throws ValueException
    {
        if (x.size() != y.size())
            throw new ValueException("FloatVector.plus - two vectors have unequal size: " + x.size() + " != "
                    + y.size());

        FloatVectorRel<U> c = (FloatVectorRel<U>) x.copy();
        c.add(y);
        return c;
    }

    // TODO: subtract

    // TODO: normalize

    // TODO: zproduct

    // TODO: inner product

}

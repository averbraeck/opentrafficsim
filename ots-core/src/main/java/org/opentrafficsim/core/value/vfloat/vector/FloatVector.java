package org.opentrafficsim.core.value.vfloat.vector;

import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Dense;
import org.opentrafficsim.core.value.Sparse;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.Vector;
import org.opentrafficsim.core.value.vfloat.FloatMathFunctions;
import org.opentrafficsim.core.value.vfloat.FloatMathFunctionsImpl;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix1D;
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
public abstract class FloatVector<U extends Unit<U>> extends Vector<U> implements FloatMathFunctions,
        FloatVectorFunctions<U>
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
    public FloatVector(final float[] values, final U unit)
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
    public FloatVector(final FloatScalar<U>[] values) throws ValueException
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
    protected abstract FloatMatrix1D createMatrix1D(final int size);

    /**
     * @return the Colt vector.
     */
    public FloatMatrix1D getVectorSI()
    {
        return this.vectorSI;
    }

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
     * @see org.opentrafficsim.core.value.VectorFunctions#size()
     */
    public int size()
    {
        return (int) this.vectorSI.size();
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.vector.FloatVectorFunctions#getSI(int)
     */
    public float getSI(final int index) throws ValueException
    {
        if (index < 0 || index >= this.vectorSI.size())
            throw new ValueException("FloatVector.get: index<0 || index>=size. index=" + index + ", size=" + size());
        return this.vectorSI.get(index);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.vector.FloatVectorFunctions#getInUnit(int)
     */
    public float getInUnit(final int index) throws ValueException
    {
        return (float) convertToSpecifiedUnit(getSI(index));
    }

    /**
     * @param index position to get the value for in the SI unit in which it has been stored.
     * @param targetUnit the unit for the result.
     * @return value at position i.
     * @throws ValueException if i < 0 or i >= vector.size().
     */
    public float getInUnit(final int index, final U targetUnit) throws ValueException
    {
        return (float) convertToUnit(getSI(index), targetUnit);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.vector.FloatVectorFunctions#zSum()
     */
    public float zSum()
    {
        return this.vectorSI.zSum();
    }

    /**
     * @see org.opentrafficsim.core.value.VectorFunctions#normalize()
     */
    public void normalize() throws ValueException
    {
        float sum = this.zSum();
        if (sum == 0)
            throw new ValueException("FloatVector.normalize: zSum of the vector values == 0, cannot normalize");
        this.divide(sum);
    }

    /**
     * @see org.opentrafficsim.core.value.VectorFunctions#cardinality()
     */
    @Override
    public int cardinality()
    {
        return this.vectorSI.cardinality();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj)
    {
        // unequal if object is of a different type.
        if (!(obj instanceof FloatVector<?>))
            return false;
        FloatVector<?> fv = (FloatVector<?>) obj;

        // unequal if the SI unit type differs (km/h and m/s could have the same content, so that is allowed)
        if (!this.getUnit().getStandardUnit().equals(fv.getUnit().getStandardUnit()))
            return false;

        // unequal if one is absolute and the other is relative
        if (this.isAbsolute() != fv.isAbsolute() || this.isRelative() != fv.isRelative())
            return false;

        // Colt's equals also tests the size of the vector
        return this.vectorSI.equals(fv.vectorSI);
    }

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
     * @see org.opentrafficsim.core.value.MathFunctions#inv()
     */
    @Override
    public void inv()
    {
        this.vectorSI.assign(FloatFunctions.inv);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.FloatMathFunctions#multiply(float)
     */
    @Override
    public void multiply(final float constant)
    {
        this.vectorSI.assign(FloatFunctions.mult(constant));
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.FloatMathFunctions#divide(float)
     */
    @Override
    public void divide(final float constant)
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
    public void subtract(final FloatVectorRel<U> vector) throws ValueException
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
    public static <U extends Unit<U>> FloatVectorAbs<U> plus(final FloatVectorAbs<U> x, final FloatVectorRel<U> y)
            throws ValueException
    {
        if (x.size() != y.size())
            throw new ValueException("FloatVector.plus - two vectors have unequal size: " + x.size() + " != "
                    + y.size());

        FloatVectorAbs<U> c = x.copy();
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
    public static <U extends Unit<U>> FloatVectorAbs<U> plus(final FloatVectorRel<U> x, final FloatVectorAbs<U> y)
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
    public static <U extends Unit<U>> FloatVectorRel<U> plus(final FloatVectorRel<U> x, final FloatVectorRel<U> y)
            throws ValueException
    {
        if (x.size() != y.size())
            throw new ValueException("FloatVector.plus - two vectors have unequal size: " + x.size() + " != "
                    + y.size());

        FloatVectorRel<U> c = x.copy();
        c.add(y);
        return c;
    }

    /**
     * Subtract a vector with relative values y from a vector with relative values x. The result is a vector with
     * relative values. The target unit will be the unit of relative value x.
     * @param x relative vector 1
     * @param y relative vector 2
     * @return new Vector with absolute elements values x[i] minus y[i]
     * @throws ValueException when vectors have unequal size
     */
    public static <U extends Unit<U>> FloatVectorRel<U> minus(final FloatVectorRel<U> x, final FloatVectorRel<U> y)
            throws ValueException
    {
        if (x.size() != y.size())
            throw new ValueException("FloatVector.minus - two vectors have unequal size: " + x.size() + " != "
                    + y.size());

        FloatVectorRel<U> c = x.copy();
        c.subtract(y);
        return c;
    }

    /**
     * Subtract a vector with relative values y from a vector with absolute values x. The result is a vector with
     * absolute values. The target unit will be the unit of vector x.
     * @param x absolute vector 1
     * @param y relative vector 2
     * @return new Vector with absolute elements: values x[i] minus y[i]
     * @throws ValueException when vectors have unequal size
     */
    public static <U extends Unit<U>> FloatVectorAbs<U> minus(final FloatVectorAbs<U> x, final FloatVectorRel<U> y)
            throws ValueException
    {
        if (x.size() != y.size())
            throw new ValueException("FloatVector.minus - two vectors have unequal size: " + x.size() + " != "
                    + y.size());

        FloatVectorAbs<U> c = x.copy();
        c.subtract(y);
        return c;
    }

    /**
     * Subtract a vector with absolute values y from a vector with absolute values x. The result is a vector with
     * relative values. The target unit will be the unit of vector x.
     * @param x absolute vector 1
     * @param y absolute vector 2
     * @return new Vector with relative elements: values x[i] minus y[i]
     * @throws ValueException when vectors have unequal size
     */
    public static <U extends Unit<U>> FloatVectorRel<U> minus(final FloatVectorAbs<U> x, final FloatVectorAbs<U> y)
            throws ValueException
    {
        if (x.size() != y.size())
            throw new ValueException("FloatVector.minus - two vectors have unequal size: " + x.size() + " != "
                    + y.size());

        FloatVectorRel<U> c = null;
        if (x instanceof Dense)
            c = new FloatVectorRelDense<U>(x.getValuesSI(), x.unit.getStandardUnit());
        else if (x instanceof Sparse)
            c = new FloatVectorRelSparse<U>(x.getValuesSI(), x.unit.getStandardUnit());
        else
            throw new ValueException("FloatVector.minus - vector neither sparse nor dense");

        c.vectorSI.assign(y.vectorSI, FloatFunctions.minus);
        c.unit = x.unit;
        return c;
    }

    /**
     * Multiply two absolute vectors on a cell-by-cell basis, e.g. x[i] * y[i]. The result will have a new SI unit.
     * @param x the first vector to do the multiplication with
     * @param y the second vector to do the multiplication with
     * @return the multiplication of this vector and another vector of the same size.
     * @throws ValueException if the two vectors have unequal size
     */
    public static FloatVectorAbs<SIUnit> multiply(final FloatVectorAbs<?> x, final FloatVectorAbs<?> y)
            throws ValueException
    {
        if (x.size() != y.size())
            throw new ValueException("FloatVector.zProduct - two vectors have unequal size: " + x.size() + " != "
                    + y.size());

        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(x.getUnit().getSICoefficients(),
                        y.getUnit().getSICoefficients()).toString());

        @SuppressWarnings("unchecked")
        FloatVectorAbs<SIUnit> c = (FloatVectorAbs<SIUnit>) x.copy();
        c.vectorSI.assign(y.vectorSI, FloatFunctions.mult);
        c.unit = targetUnit;
        return c;
    }

    /**
     * Multiply two relative vectors on a cell-by-cell basis, e.g. x[i] * y[i]. The result will have a new SI unit.
     * @param x the first vector to do the multiplication with
     * @param y the second vector to do the multiplication with
     * @return the multiplication of this vector and another vector of the same size.
     * @throws ValueException if the two vectors have unequal size
     */
    public static FloatVectorRel<SIUnit> multiply(final FloatVectorRel<?> x, final FloatVectorRel<?> y)
            throws ValueException
    {
        if (x.size() != y.size())
            throw new ValueException("FloatVector.zProduct - two vectors have unequal size: " + x.size() + " != "
                    + y.size());

        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(x.getUnit().getSICoefficients(),
                        y.getUnit().getSICoefficients()).toString());

        @SuppressWarnings("unchecked")
        FloatVectorRel<SIUnit> c = (FloatVectorRel<SIUnit>) x.copy();
        c.vectorSI.assign(y.vectorSI, FloatFunctions.mult);
        c.unit = targetUnit;
        return c;
    }

    /**
     * Multiply an absolute vector with units on a cell-by-cell basis with a dimensionless vector, e.g. x[i] * c[i]. The
     * result will have the same unit as vector x.
     * @param x the first vector to do the multiplication with
     * @param c the dimensionless vector with constants to do the multiplication with
     * @return the multiplication of this vector and another vector of the same size.
     * @throws ValueException if the two vectors have unequal size
     */
    public static <U extends Unit<U>> FloatVectorAbs<U> multiply(final FloatVectorAbs<U> x, final float[] c)
            throws ValueException
    {
        if (x.size() != c.length)
            throw new ValueException("FloatVector.zProduct with dimensionless vector - two vectors have unequal size: "
                    + x.size() + " != " + c.length);

        // TODO: more elegant implementation that does not copy the entire vector?
        FloatVectorAbs<U> result = x.copy();
        // TODO: only dense?
        DenseFloatMatrix1D cMatrix = new DenseFloatMatrix1D(c);
        result.vectorSI.assign(cMatrix, FloatFunctions.mult);
        return result;
    }

    /**
     * Multiply a relative vector with units on a cell-by-cell basis with a dimensionless vector, e.g. x[i] * c[i]. The
     * result will have the same unit as vector x.
     * @param x the first vector to do the multiplication with
     * @param c the dimensionless vector with constants to do the multiplication with
     * @return the multiplication of this vector and another vector of the same size.
     * @throws ValueException if the two vectors have unequal size
     */
    public static <U extends Unit<U>> FloatVectorRel<U> multiply(final FloatVectorRel<U> x, final float[] c)
            throws ValueException
    {
        if (x.size() != c.length)
            throw new ValueException("FloatVector.zProduct with dimensionless vector - two vectors have unequal size: "
                    + x.size() + " != " + c.length);

        // TODO: more elegant implementation that does not copy the entire vector?
        FloatVectorRel<U> result = x.copy();
        // TODO: only dense?
        DenseFloatMatrix1D cMatrix = new DenseFloatMatrix1D(c);
        result.vectorSI.assign(cMatrix, FloatFunctions.mult);
        return result;
    }

    /**
     * Convert sparse vector to dense vector.
     * @param x the vector to convert
     * @return the converted vector
     */
    public static <U extends Unit<U>> FloatVectorAbsDense<U> sparseToDense(final FloatVectorAbsSparse<U> x)
    {
        FloatVectorAbsDense<U> v = new FloatVectorAbsDense<U>(x.getValuesSI(), x.getUnit().getStandardUnit());
        v.unit = x.unit;
        return v;
    }

    /**
     * Convert sparse vector to dense vector.
     * @param x the vector to convert
     * @return the converted vector
     */
    public static <U extends Unit<U>> FloatVectorRelDense<U> sparseToDense(final FloatVectorRelSparse<U> x)
    {
        FloatVectorRelDense<U> v = new FloatVectorRelDense<U>(x.getValuesSI(), x.getUnit().getStandardUnit());
        v.unit = x.unit;
        return v;
    }

    /**
     * Convert dense vector to sparse vector.
     * @param x the vector to convert
     * @return the converted vector
     */
    public static <U extends Unit<U>> FloatVectorAbsSparse<U> denseToSparse(final FloatVectorAbsDense<U> x)
    {
        FloatVectorAbsSparse<U> v = new FloatVectorAbsSparse<U>(x.getValuesSI(), x.getUnit().getStandardUnit());
        v.unit = x.unit;
        return v;
    }

    /**
     * Convert dense vector to sparse vector.
     * @param x the vector to convert
     * @return the converted vector
     */
    public static <U extends Unit<U>> FloatVectorRelSparse<U> denseToSparse(final FloatVectorRelDense<U> x)
    {
        FloatVectorRelSparse<U> v = new FloatVectorRelSparse<U>(x.getValuesSI(), x.getUnit().getStandardUnit());
        v.unit = x.unit;
        return v;
    }
}

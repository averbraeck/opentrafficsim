package org.opentrafficsim.core.value.vfloat.matrix;

import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Dense;
import org.opentrafficsim.core.value.Format;
import org.opentrafficsim.core.value.Matrix;
import org.opentrafficsim.core.value.Sparse;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.FloatMathFunctions;
import org.opentrafficsim.core.value.vfloat.FloatMathFunctionsImpl;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;
import org.opentrafficsim.core.value.vfloat.vector.FloatVectorAbs;
import org.opentrafficsim.core.value.vfloat.vector.FloatVectorAbsDense;
import org.opentrafficsim.core.value.vfloat.vector.FloatVectorAbsSparse;
import org.opentrafficsim.core.value.vfloat.vector.FloatVectorRel;
import org.opentrafficsim.core.value.vfloat.vector.FloatVectorRelDense;
import org.opentrafficsim.core.value.vfloat.vector.FloatVectorRelSparse;

import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.FloatMatrix2D;
import cern.colt.matrix.tfloat.algo.DenseFloatAlgebra;
import cern.colt.matrix.tfloat.algo.SparseFloatAlgebra;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
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
 * @param <U> The unit for this value type
 */
public abstract class FloatMatrix<U extends Unit<U>> extends Matrix<U> implements FloatMathFunctions,
        FloatMatrixFunctions<U>
{
    /** */
    private static final long serialVersionUID = 20140618L;

    /** the internal storage for the matrix; internally they are stored in SI units; can be dense or sparse */
    protected FloatMatrix2D matrixSI;

    /**
     * Construct the matrix and store the values in SI units.
     * @param values a 2D array of values for the constructor
     * @param unit the unit of the values
     * @throws ValueException when the array is not rectangular
     */
    public FloatMatrix(final float[][] values, final U unit) throws ValueException
    {
        super(unit);
        for (int row = 1; row < values.length; row++)
            if (values[0].length != values[row].length)
                throw new ValueException("lengths of rows are not all the same");
        this.matrixSI = createMatrix2D(values.length, (values.length > 0 ? values[0].length : 0));
        if (unit.equals(unit.getStandardUnit()))
        {
            this.matrixSI.assign(values);
        }
        else
        {
            for (int row = 0; row < values.length; row++)
            {
                for (int column = 0; column < values[row].length; column++)
                {
                    this.matrixSI.set(row, column, (float) expressAsSIUnit(values[row][column]));
                }
            }
        }
    }

    /**
     * Construct the matrix and store the values in SI units.
     * @param values an array of values for the constructor
     * @throws ValueException exception thrown when array with zero elements is offered or the array is not rectangular
     */
    public FloatMatrix(final FloatScalar<U>[][] values) throws ValueException
    {
        super(values.length > 0 && values[0].length > 0 ? values[0][0].getUnit() : null);
        for (int row = 1; row < values.length; row++)
            if (values[0].length != values[row].length)
                throw new ValueException("lengths of rows are not all the same");
        if (values.length == 0 || values[0].length == 0)
        {
            throw new ValueException(
                    "FloatMatrix constructor called with an empty row or column of FloatScalar elements");
        }

        this.matrixSI = createMatrix2D(values.length, values[0].length);
        for (int row = 0; row < values.length; row++)
        {
            for (int column = 0; column < values[row].length; column++)
            {
                this.matrixSI.set(row, column, values[row][column].getValueSI());
            }
        }
    }

    /**
     * This method has to be implemented by each leaf class.
     * @param rows the number of rows in the matrix
     * @param columns the number of columns in the matrix
     * @return an instance of the right type of matrix (absolute /relative, dense / sparse, etc.).
     */
    protected abstract FloatMatrix2D createMatrix2D(final int rows, final int columns);

    /**
     * @return the Colt matrix.
     */
    public FloatMatrix2D getMatrixSI()
    {
        return this.matrixSI;
    }

    /**
     * @return values in SI units
     */
    public float[][] getValuesSI()
    {
        return this.matrixSI.toArray();
    }

    /**
     * @return values in original units
     */
    public float[][] getValuesInUnit()
    {
        float[][] values = this.matrixSI.toArray();
        for (int i = 0; i < values.length; i++)
            for (int j = 0; j < values[i].length; j++)
                values[i][j] = (float) expressAsSpecifiedUnit(values[i][j]);
        return values;
    }

    /**
     * @param targetUnit the unit to convert the values to
     * @return values in specific target unit
     */
    public float[][] getValuesInUnit(final U targetUnit)
    {
        float[][] values = this.matrixSI.toArray();
        for (int i = 0; i < values.length; i++)
            for (int j = 0; j < values[i].length; j++)
                values[i][j] = (float) expressAsUnit(values[i][j], targetUnit);
        return values;
    }

    /**
     * @see org.opentrafficsim.core.value.MatrixFunctions#rows()
     */
    @Override
    public int rows()
    {
        return this.matrixSI.rows();
    }

    /**
     * @see org.opentrafficsim.core.value.MatrixFunctions#columns()
     */
    @Override
    public int columns()
    {
        return this.matrixSI.columns();
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixFunctions#getSI(int, int)
     */
    public float getSI(final int row, final int column) throws ValueException
    {
        if (row < 0 || row >= this.matrixSI.rows() || column < 0 || column >= this.matrixSI.columns())
            throw new ValueException("FloatMatrix.get: row<0 || row>=size || column<0 || column>=size. row=" + row
                    + ", size=" + rows() + ", column=" + column + ", size=" + columns());
        return this.matrixSI.get(row, column);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixFunctions#getInUnit(int, int)
     */
    public float getInUnit(final int row, final int column) throws ValueException
    {
        return (float) expressAsSpecifiedUnit(getSI(row, column));
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixFunctions#getInUnit(int, int,
     *      org.opentrafficsim.core.unit.Unit)
     */
    @Override
    public float getInUnit(final int row, final int column, final U targetUnit) throws ValueException
    {
        return (float) expressAsUnit(getSI(row, column), targetUnit);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixFunctions#setSI(int, int, float)
     */
    @Override
    public void setSI(final int row, final int column, float valueSI) throws ValueException
    {
        if (row < 0 || row >= this.matrixSI.rows() || column < 0 || column >= this.matrixSI.columns())
            throw new ValueException("FloatMatrix.get: row<0 || row>=size || column<0 || column>=size. row=" + row
                    + ", size=" + rows() + ", column=" + column + ", size=" + columns());
        this.matrixSI.set(row, column, valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixFunctions#set(int, int,
     *      org.opentrafficsim.core.value.vfloat.scalar.FloatScalar)
     */
    @Override
    public void set(final int row, final int column, FloatScalar<U> value) throws ValueException
    {
        setSI(row, column, value.getValueSI());
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixFunctions#setInUnit(int, int, float,
     *      org.opentrafficsim.core.unit.Unit)
     */
    @Override
    public void setInUnit(final int row, final int column, float value, U valueUnit) throws ValueException
    {
        setSI(row, column, (float) expressAsSIUnit(value, valueUnit));
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixFunctions#zSum()
     */
    public float zSum()
    {
        return this.matrixSI.zSum();
    }

    /**
     * @see org.opentrafficsim.core.value.MatrixFunctions#normalize()
     */
    public void normalize() throws ValueException
    {
        float sum = this.zSum();
        if (sum == 0)
            throw new ValueException("FloatMatrix.normalize: zSum of the matrix values == 0, cannot normalize");
        this.divide(sum);
    }

    /**
     * @see org.opentrafficsim.core.value.MatrixFunctions#cardinality()
     */
    @Override
    public int cardinality()
    {
        return this.matrixSI.cardinality();
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrixFunctions#det()
     */
    @Override
    public float det() throws ValueException
    {
        try
        {
            if (this instanceof Sparse)
            {
                //System.out.println("calling SparseFloatAlgebra().det(this.matrixSI)");
                return new SparseFloatAlgebra().det(this.matrixSI);
            }
            if (this instanceof Dense)
            {
                //System.out.println("calling DenseFloatAlgebra().det(this.matrixSI)");
                return new DenseFloatAlgebra().det(this.matrixSI);
            }
            throw new ValueException("FloatMatrix.det -- matrix implements neither Sparse nor Dense");
        }
        catch (IllegalArgumentException exception)
        {
            if (! exception.getMessage().startsWith("Matrix must be square"))
            exception.printStackTrace();
            throw new ValueException(exception.getMessage());    // probably Matrix must be square
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        // unequal if object is of a different type.
        if (!(obj instanceof FloatMatrix<?>))
            return false;
        FloatMatrix<?> fm = (FloatMatrix<?>) obj;

        // unequal if the SI unit type differs (km/h and m/s could have the same content, so that is allowed)
        if (!this.getUnit().getStandardUnit().equals(fm.getUnit().getStandardUnit()))
            return false;

        // unequal if one is absolute and the other is relative
        if (this.isAbsolute() != fm.isAbsolute() || this.isRelative() != fm.isRelative())
            return false;

        // Colt's equals also tests the number of rows and columns
        return this.matrixSI.equals(fm.matrixSI);
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
        this.matrixSI.assign(FloatFunctions.abs);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#acos()
     */
    @Override
    public void acos()
    {
        this.matrixSI.assign(FloatFunctions.acos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#asin()
     */
    @Override
    public void asin()
    {
        this.matrixSI.assign(FloatFunctions.asin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#atan()
     */
    @Override
    public void atan()
    {
        this.matrixSI.assign(FloatFunctions.atan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cbrt()
     */
    @Override
    public void cbrt()
    {
        this.matrixSI.assign(FloatMathFunctionsImpl.cbrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#ceil()
     */
    @Override
    public void ceil()
    {
        this.matrixSI.assign(FloatFunctions.ceil);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cos()
     */
    @Override
    public void cos()
    {
        this.matrixSI.assign(FloatFunctions.cos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cosh()
     */
    @Override
    public void cosh()
    {
        this.matrixSI.assign(FloatMathFunctionsImpl.cosh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#exp()
     */
    @Override
    public void exp()
    {
        this.matrixSI.assign(FloatFunctions.exp);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#expm1()
     */
    @Override
    public void expm1()
    {
        this.matrixSI.assign(FloatMathFunctionsImpl.expm1);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#floor()
     */
    @Override
    public void floor()
    {
        this.matrixSI.assign(FloatFunctions.floor);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log()
     */
    @Override
    public void log()
    {
        this.matrixSI.assign(FloatFunctions.log);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log10()
     */
    @Override
    public void log10()
    {
        this.matrixSI.assign(FloatMathFunctionsImpl.log10);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log1p()
     */
    @Override
    public void log1p()
    {
        this.matrixSI.assign(FloatMathFunctionsImpl.log1p);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#pow(double)
     */
    @Override
    public void pow(double x)
    {
        this.matrixSI.assign(FloatFunctions.pow((float) x));
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#rint()
     */
    @Override
    public void rint()
    {
        this.matrixSI.assign(FloatFunctions.rint);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#round()
     */
    @Override
    public void round()
    {
        this.matrixSI.assign(FloatMathFunctionsImpl.round);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#signum()
     */
    @Override
    public void signum()
    {
        this.matrixSI.assign(FloatMathFunctionsImpl.signum);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sin()
     */
    @Override
    public void sin()
    {
        this.matrixSI.assign(FloatFunctions.sin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sinh()
     */
    @Override
    public void sinh()
    {
        this.matrixSI.assign(FloatMathFunctionsImpl.sinh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sqrt()
     */
    @Override
    public void sqrt()
    {
        this.matrixSI.assign(FloatFunctions.sqrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tan()
     */
    @Override
    public void tan()
    {
        this.matrixSI.assign(FloatFunctions.tan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tanh()
     */
    @Override
    public void tanh()
    {
        this.matrixSI.assign(FloatMathFunctionsImpl.tanh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toDegrees()
     */
    @Override
    public void toDegrees()
    {
        this.matrixSI.assign(FloatMathFunctionsImpl.toDegrees);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toRadians()
     */
    @Override
    public void toRadians()
    {
        this.matrixSI.assign(FloatMathFunctionsImpl.toRadians);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#inv()
     */
    @Override
    public void inv()
    {
        this.matrixSI.assign(FloatFunctions.inv);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.FloatMathFunctions#multiply(float)
     */
    @Override
    public void multiply(float constant)
    {
        this.matrixSI.assign(FloatFunctions.mult(constant));
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.FloatMathFunctions#divide(float)
     */
    @Override
    public void divide(float constant)
    {
        this.matrixSI.assign(FloatFunctions.div(constant));
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
     * @param displayUnit the unit to display the matrix in.
     * @return a printable String with the matrix contents
     */
    public String toString(final U displayUnit)
    {
        // TODO: check how to always format numbers corresponding to the Locale used.
        String s = "[" + displayUnit.getAbbreviation() + "]";
        for (int i = 0; i < this.matrixSI.rows(); i++)
        {
            s += "\n";
            for (int j = 0; j < this.matrixSI.columns(); j++)
            {
                float f = (float) expressAsUnit(this.matrixSI.get(i, j), displayUnit);
                    s += " " + Format.format(f);
            }
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
     * @param matrix the matrix to add
     * @throws ValueException when matrices have unequal size
     */
    public void add(final FloatMatrixRel<U> matrix) throws ValueException
    {
        if (rows() != matrix.rows() || columns() != matrix.columns())
            throw new ValueException("FloatMatrix.add - two matrices have unequal size: " + rows() + "x" + columns()
                    + " != " + matrix.rows() + "x" + matrix.columns());
        this.matrixSI.assign(matrix.matrixSI, FloatFunctions.plus);
    }

    /**
     * Subtract another value from this value. Only relative values are allowed; subtracting an absolute value from a
     * relative value is not allowed. Subtracting an absolute value from an existing absolute value would require the
     * result to become relative, which is a type change that is impossible. For that operation, use a static method.
     * @param matrix the value to subtract
     * @throws ValueException when matrices have unequal size
     */
    public void subtract(final FloatMatrixRel<U> matrix) throws ValueException
    {
        if (rows() != matrix.rows() || columns() != matrix.columns())
            throw new ValueException("FloatMatrix.subtract - two matrices have unequal size: " + rows() + "x"
                    + columns() + " != " + matrix.rows() + "x" + matrix.columns());
        this.matrixSI.assign(matrix.matrixSI, FloatFunctions.minus);
    }

    /**********************************************************************************/
    /********************************* STATIC METHODS *********************************/
    /**********************************************************************************/

    /**
     * Add a matrix with absolute values x and a matrix with relative values y. The target unit will be the unit of
     * absolute value x.
     * @param x absolute matrix 1
     * @param y relative matrix 2
     * @return new Matrix with absolute elements sum of x[i] and y[i]
     * @throws ValueException when matrices have unequal size
     */
    public static <U extends Unit<U>> FloatMatrixAbs<U> plus(final FloatMatrixAbs<U> x, final FloatMatrixRel<U> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("FloatMatrix.plus - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        FloatMatrixAbs<U> c = x.copy();
        c.add(y);
        return c;
    }

    /**
     * Add a matrix with relative values x and a matrix with absolute values y. The target unit will be the unit of
     * absolute value y.
     * @param x relative matrix 1
     * @param y absolute matrix 2
     * @param targetUnit unit in which the results will be displayed
     * @return new Matrix with absolute elements sum of x[i] and y[i]
     * @throws ValueException when matrices have unequal size
     */
    public static <U extends Unit<U>> FloatMatrixAbs<U> plus(final FloatMatrixRel<U> x, final FloatMatrixAbs<U> y)
            throws ValueException
    {
        return plus(y, x);
    }

    /**
     * Add a matrix with relative values x and a matrix with relative values y. The target unit will be the unit of
     * relative value x.
     * @param x relative matrix 1
     * @param y relative matrix 2
     * @return new Matrix with absolute elements sum of x[i] and y[i]
     * @throws ValueException when matrices have unequal size
     */
    public static <U extends Unit<U>> FloatMatrixRel<U> plus(final FloatMatrixRel<U> x, final FloatMatrixRel<U> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("FloatMatrix.plus - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        FloatMatrixRel<U> c = x.copy();
        c.add(y);
        return c;
    }

    /**
     * Subtract a matrix with relative values y from a matrix with relative values x. The result is a matrix with
     * relative values. The target unit will be the unit of relative value x.
     * @param x relative matrix 1
     * @param y relative matrix 2
     * @return new Matrix with absolute elements values x[i,j] minus y[i,j]
     * @throws ValueException when matrices have unequal size
     */
    public static <U extends Unit<U>> FloatMatrixRel<U> minus(final FloatMatrixRel<U> x, final FloatMatrixRel<U> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("FloatMatrix.minus - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        FloatMatrixRel<U> c = x.copy();
        c.subtract(y);
        return c;
    }

    /**
     * Subtract a matrix with relative values y from a matrix with absolute values x. The result is a matrix with
     * absolute values. The target unit will be the unit of matrix x.
     * @param x absolute matrix 1
     * @param y relative matrix 2
     * @return new Matrix with absolute elements: values x[i,j] minus y[i,j]
     * @throws ValueException when matrices have unequal size
     */
    public static <U extends Unit<U>> FloatMatrixAbs<U> minus(final FloatMatrixAbs<U> x, final FloatMatrixRel<U> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("FloatMatrix.minus - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        FloatMatrixAbs<U> c = x.copy();
        c.subtract(y);
        return c;
    }

    /**
     * Subtract a matrix with absolute values y from a matrix with absolute values x. The result is a matrix with
     * relative values. The target unit will be the unit of matrix x.
     * @param x absolute matrix 1
     * @param y absolute matrix 2
     * @return new Matrix with relative elements: values x[i,j] minus y[i,j]
     * @throws ValueException when matrices have unequal size
     */
    public static <U extends Unit<U>> FloatMatrixRel<U> minus(final FloatMatrixAbs<U> x, final FloatMatrixAbs<U> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("FloatMatrix.minus - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        FloatMatrixRel<U> c = null;
        if (x instanceof Dense)
            c = new FloatMatrixRelDense<U>(x.getValuesSI(), x.unit.getStandardUnit());
        else if (x instanceof Sparse)
            c = new FloatMatrixRelSparse<U>(x.getValuesSI(), x.unit.getStandardUnit());
        else
            throw new ValueException("FloatMatrix.minus - matrix neither sparse nor dense");

        c.matrixSI.assign(y.matrixSI, FloatFunctions.minus);
        c.unit = x.unit;

        return c;
    }

    /**
     * Multiply two absolute matrices on a cell-by-cell basis, e.g. x[i,j] * y[i,j]. The result will have a new SI unit.
     * @param x the first matrix to do the multiplication with
     * @param y the second matrix to do the multiplication with
     * @return the multiplication of this matrix and another matrix of the same size.
     * @throws ValueException if the two matrices have unequal size
     */
    public static FloatMatrixAbs<SIUnit> multiply(final FloatMatrixAbs<?> x, final FloatMatrixAbs<?> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("FloatMatrix.multiply - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(x.getUnit().getSICoefficients(),
                        y.getUnit().getSICoefficients()).toString());

        @SuppressWarnings("unchecked")
        FloatMatrixAbs<SIUnit> c = (FloatMatrixAbs<SIUnit>) x.copy();
        c.matrixSI.assign(y.matrixSI, FloatFunctions.mult);
        c.unit = targetUnit;
        return c;
    }

    /**
     * Multiply two relative matrices on a cell-by-cell basis, e.g. x[i,j] * y[i,j]. The result will have a new SI unit.
     * @param x the first matrix to do the multiplication with
     * @param y the second matrix to do the multiplication with
     * @return the multiplication of this matrix and another matrix of the same size.
     * @throws ValueException if the two matrices have unequal size
     */
    public static FloatMatrixRel<SIUnit> multiply(final FloatMatrixRel<?> x, final FloatMatrixRel<?> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("FloatMatrix.multiply - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(x.getUnit().getSICoefficients(),
                        y.getUnit().getSICoefficients()).toString());

        @SuppressWarnings("unchecked")
        FloatMatrixRel<SIUnit> c = (FloatMatrixRel<SIUnit>) x.copy();
        c.matrixSI.assign(y.matrixSI, FloatFunctions.mult);
        c.unit = targetUnit;
        return c;
    }

    /**
     * Multiply an absolute matrix with units on a cell-by-cell basis with a dimensionless matrix, e.g. x[i,j] * c[i,j].
     * The result will have the same unit as matrix x.
     * @param x the first matrix to do the multiplication with
     * @param c the dimensionless matrix with constants to do the multiplication with
     * @return the multiplication of this matrix and another matrix of the same size.
     * @throws ValueException if the two matrices have unequal size
     */
    public static <U extends Unit<U>> FloatMatrixAbs<U> multiply(final FloatMatrixAbs<U> x, final float[][] c)
            throws ValueException
    {
        for (int row = 1; row < c.length; row++)
            if (c[0].length != c[row].length)
                throw new ValueException("FloatMatrix.multiply lengths of rows of c are not all the same");
        if (x.rows() != c.length || x.columns() != (c.length > 0 ? c[0].length : 0))
            throw new ValueException("FloatMatrix.multiply with dimensionless matrix- two matrices have unequal size: "
                    + x.rows() + "x" + x.columns() + " != " + c.length + "x" + (c.length > 0 ? c[0].length : 0));

        // TODO: more elegant implementation that does not copy the entire matrix?
        FloatMatrixAbs<U> result = x.copy();
        DenseFloatMatrix2D cMatrix = new DenseFloatMatrix2D(c);
        result.matrixSI.assign(cMatrix, FloatFunctions.mult);
        return result;
    }

    /**
     * Multiply a relative matrix with units on a cell-by-cell basis with a dimensionless matrix, e.g. x[i,j] * c[i,j].
     * The result will have the same unit as matrix x.
     * @param x the first matrix to do the multiplication with
     * @param c the dimensionless matrix with constants to do the multiplication with
     * @return the multiplication of this matrix and another matrix of the same size.
     * @throws ValueException if the two matrices have unequal size
     */
    public static <U extends Unit<U>> FloatMatrixRel<U> multiply(final FloatMatrixRel<U> x, final float[][] c)
            throws ValueException
    {
        for (int row = 1; row < c.length; row++)
            if (c[0].length != c[row].length)
                throw new ValueException("FloatMatrix.multiply lengths of rows of c are not all the same");
        if (x.rows() != c.length || x.columns() != (c.length > 0 ? c[0].length : 0))
            throw new ValueException("FloatMatrix.multiply with dimensionless matrix- two matrices have unequal size: "
                    + x.rows() + "x" + x.columns() + " != " + c.length + "x" + (c.length > 0 ? c[0].length : 0));

        // TODO: more elegant implementation that does not copy the entire matrix?
        FloatMatrixRel<U> result = x.copy();
        DenseFloatMatrix2D cMatrix = new DenseFloatMatrix2D(c);
        result.matrixSI.assign(cMatrix, FloatFunctions.mult);
        return result;
    }

    /**
     * Convert sparse matrix to dense matrix.
     * @param x the matrix to convert
     * @return the converted matrix
     */
    public static <U extends Unit<U>> FloatMatrixAbsDense<U> sparseToDense(final FloatMatrixAbsSparse<U> x)
    {
        FloatMatrixAbsDense<U> v = null;
        try
        {
            v = new FloatMatrixAbsDense<U>(x.getValuesSI(), x.getUnit().getStandardUnit());
        }
        catch (ValueException exception)
        {
            System.err.println("CANNOT HAPPEN");
            // TODO fix error logging
        }
        v.unit = x.unit;
        return v;
    }

    /**
     * Convert sparse matrix to dense matrix.
     * @param x the matrix to convert
     * @return the converted matrix
     */
    public static <U extends Unit<U>> FloatMatrixRelDense<U> sparseToDense(final FloatMatrixRelSparse<U> x)
    {
        FloatMatrixRelDense<U> v = null;
        try
        {
            v = new FloatMatrixRelDense<U>(x.getValuesSI(), x.getUnit().getStandardUnit());
        }
        catch (ValueException exception)
        {
            System.err.println("CANNOT HAPPEN");
            // TODO fix error logging
        }
        v.unit = x.unit;
        return v;
    }

    /**
     * Convert dense matrix to sparse matrix.
     * @param x the matrix to convert
     * @return the converted matrix
     */
    public static <U extends Unit<U>> FloatMatrixAbsSparse<U> denseToSparse(final FloatMatrixAbsDense<U> x)
    {
        FloatMatrixAbsSparse<U> v = null;
        try
        {
            v = new FloatMatrixAbsSparse<U>(x.getValuesSI(), x.getUnit().getStandardUnit());
        }
        catch (ValueException exception)
        {
            System.err.println("CANNOT HAPPEN");
            // TODO fix error logging
        }
        v.unit = x.unit;
        return v;
    }

    /**
     * Convert dense matrix to sparse matrix.
     * @param x the matrix to convert
     * @return the converted matrix
     */
    public static <U extends Unit<U>> FloatMatrixRelSparse<U> denseToSparse(final FloatMatrixRelDense<U> x)
    {
        FloatMatrixRelSparse<U> v = null;
        try
        {
            v = new FloatMatrixRelSparse<U>(x.getValuesSI(), x.getUnit().getStandardUnit());
        }
        catch (ValueException exception)
        {
            System.err.println("CANNOT HAPPEN");
            // TODO fix error logging
        }
        v.unit = x.unit;
        return v;
    }

    /**
     * Solve x for A*x = b. According to Colt: x; a new independent matrix; solution if A is square, least squares
     * solution if A.rows() > A.columns(), underdetermined system solution if A.rows() < A.columns().
     * @param A matrix A in A*x = b
     * @param b vector b in A*x = b
     * @return vector x in A*x = b
     * @throws ValueException when Matrix A is neither Sparse nor Dense.
     */
    public static FloatVectorAbs<SIUnit> solve(final FloatMatrixAbs<?> A, final FloatVectorAbs<?> b)
            throws ValueException
    {
        // TODO: is this correct? Should lookup matrix algebra to find out unit for x when solving A*x = b ?
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(b.getUnit().getSICoefficients(),
                        A.getUnit().getSICoefficients()).toString());

        // TODO: should the algorithm throw an exception when rows/columns do not match when solving A*x = b ?
        FloatMatrix2D A2D = A.getMatrixSI();
        FloatMatrix1D b1D = b.getVectorSI();
        if (A instanceof Sparse)
        {
            FloatMatrix1D x1D = new SparseFloatAlgebra().solve(A2D, b1D);
            FloatVectorAbsSparse<SIUnit> x = new FloatVectorAbsSparse<SIUnit>(x1D.toArray(), targetUnit);
            return x;
        }
        if (A instanceof Dense)
        {
            FloatMatrix1D x1D = new DenseFloatAlgebra().solve(A2D, b1D);
            FloatVectorAbsDense<SIUnit> x = new FloatVectorAbsDense<SIUnit>(x1D.toArray(), targetUnit);
            return x;
        }
        throw new ValueException("FloatMatrix.det -- matrix implements neither Sparse nor Dense");
    }

    /**
     * Solve x for A*x = b. According to Colt: x; a new independent matrix; solution if A is square, least squares
     * solution if A.rows() > A.columns(), underdetermined system solution if A.rows() < A.columns().
     * @param A matrix A in A*x = b
     * @param b vector b in A*x = b
     * @return vector x in A*x = b
     * @throws ValueException when Matrix A is neither Sparse nor Dense.
     */
    public static FloatVectorRel<SIUnit> solve(final FloatMatrixRel<?> A, final FloatVectorRel<?> b)
            throws ValueException
    {
        // TODO: is this correct? Should lookup matrix algebra to find out unit for x when solving A*x = b ?
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(b.getUnit().getSICoefficients(),
                        A.getUnit().getSICoefficients()).toString());

        // TODO: should the algorithm throw an exception when rows/columns do not match when solving A*x = b ?
        FloatMatrix2D A2D = A.getMatrixSI();
        FloatMatrix1D b1D = b.getVectorSI();
        if (A instanceof Sparse)
        {
            FloatMatrix1D x1D = new SparseFloatAlgebra().solve(A2D, b1D);
            FloatVectorRelSparse<SIUnit> x = new FloatVectorRelSparse<SIUnit>(x1D.toArray(), targetUnit);
            return x;
        }
        if (A instanceof Dense)
        {
            FloatMatrix1D x1D = new DenseFloatAlgebra().solve(A2D, b1D);
            FloatVectorRelDense<SIUnit> x = new FloatVectorRelDense<SIUnit>(x1D.toArray(), targetUnit);
            return x;
        }
        throw new ValueException("FloatMatrix.det -- matrix implements neither Sparse nor Dense");
    }

}

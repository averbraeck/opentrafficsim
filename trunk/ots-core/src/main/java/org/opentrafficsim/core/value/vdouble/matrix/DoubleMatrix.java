package org.opentrafficsim.core.value.vdouble.matrix;

import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Dense;
import org.opentrafficsim.core.value.Matrix;
import org.opentrafficsim.core.value.Sparse;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.DoubleMathFunctions;
import org.opentrafficsim.core.value.vdouble.DoubleMathFunctionsImpl;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVectorAbs;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVectorAbsDense;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVectorAbsSparse;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVectorRel;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVectorRelDense;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVectorRelSparse;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.SparseDoubleAlgebra;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;

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
public abstract class DoubleMatrix<U extends Unit<U>> extends Matrix<U> implements DoubleMathFunctions,
        DoubleMatrixFunctions<U>
{
    /** */
    private static final long serialVersionUID = 20140618L;

    /** the internal storage for the matrix; internally they are stored in SI units; can be dense or sparse */
    protected DoubleMatrix2D matrixSI;

    /**
     * Construct the matrix and store the values in SI units.
     * @param values a 2D array of values for the constructor
     * @param unit the unit of the values
     */
    public DoubleMatrix(final double[][] values, final U unit)
    {
        super(unit);
        if (unit.equals(unit.getStandardUnit()))
        {
            this.matrixSI = createMatrix2D(values.length, (values.length > 0 ? values[0].length : 0));
            this.matrixSI.assign(values);
        }
        else
        {
            this.matrixSI = createMatrix2D(values.length, (values.length > 0 ? values[0].length : 0));
            for (int row = 0; row < values.length; row++)
            {
                for (int column = 0; column < (values.length > 0 ? values[0].length : 0); column++)
                {
                    this.matrixSI.set(row, column, expressAsSIUnit(values[row][column]));
                }
            }
        }
    }

    /**
     * Construct the matrix and store the values in SI units.
     * @param values an array of values for the constructor
     * @throws ValueException exception thrown when array with zero elements is offered
     */
    public DoubleMatrix(final DoubleScalar<U>[][] values) throws ValueException
    {
        super(values.length > 0 && (values.length > 0 ? values[0].length : 0) > 0 ? values[0][0].getUnit() : null);
        if (values.length == 0 || (values.length > 0 ? values[0].length : 0) == 0)
        {
            throw new ValueException(
                    "DoubleMatrix constructor called with an empty row or column of DoubleScalar elements");
        }

        this.matrixSI = createMatrix2D(values.length, (values.length > 0 ? values[0].length : 0));
        for (int row = 0; row < values.length; row++)
        {
            for (int column = 0; column < (values.length > 0 ? values[0].length : 0); column++)
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
    protected abstract DoubleMatrix2D createMatrix2D(final int rows, final int columns);

    /**
     * @return the Colt matrix.
     */
    public DoubleMatrix2D getMatrixSI()
    {
        return this.matrixSI;
    }

    /**
     * @return values in SI units
     */
    public double[][] getValuesSI()
    {
        return this.matrixSI.toArray();
    }

    /**
     * @return values in original units
     */
    public double[][] getValuesInUnit()
    {
        double[][] values = this.matrixSI.toArray();
        for (int i = 0; i < values.length; i++)
            for (int j = 0; j < (values.length > 0 ? values[0].length : 0); j++)
                values[i][j] = expressAsSpecifiedUnit(values[i][j]);
        return values;
    }

    /**
     * @param targetUnit the unit to convert the values to
     * @return values in specific target unit
     */
    public double[][] getValuesInUnit(final U targetUnit)
    {
        double[][] values = this.matrixSI.toArray();
        for (int i = 0; i < values.length; i++)
            for (int j = 0; j < (values.length > 0 ? values[0].length : 0); j++)
                values[i][j] = expressAsUnit(values[i][j], targetUnit);
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
     * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrixFunctions#getSI(int, int)
     */
    public double getSI(final int row, final int column) throws ValueException
    {
        if (row < 0 || row >= this.matrixSI.rows() || column < 0 || column >= this.matrixSI.columns())
            throw new ValueException("DoubleMatrix.get: row<0 || row>=size || column<0 || column>=size. row=" + row
                    + ", size=" + rows() + ", column=" + column + ", size=" + columns());
        return this.matrixSI.get(row, column);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrixFunctions#getInUnit(int, int)
     */
    public double getInUnit(final int row, final int column) throws ValueException
    {
        return expressAsSpecifiedUnit(getSI(row, column));
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrixFunctions#getInUnit(int, int,
     *      org.opentrafficsim.core.unit.Unit)
     */
    @Override
    public double getInUnit(final int row, final int column, final U targetUnit) throws ValueException
    {
        return expressAsUnit(getSI(row, column), targetUnit);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrixFunctions#setSI(int, int, double)
     */
    @Override
    public void setSI(final int row, final int column, double valueSI) throws ValueException
    {
        if (row < 0 || row >= this.matrixSI.rows() || column < 0 || column >= this.matrixSI.columns())
            throw new ValueException("DoubleMatrix.get: row<0 || row>=size || column<0 || column>=size. row=" + row
                    + ", size=" + rows() + ", column=" + column + ", size=" + columns());
        this.matrixSI.set(row, column, valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrixFunctions#set(int, int,
     *      org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar)
     */
    @Override
    public void set(final int row, final int column, DoubleScalar<U> value) throws ValueException
    {
        setSI(row, column, value.getValueSI());
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrixFunctions#setInUnit(int, int, double,
     *      org.opentrafficsim.core.unit.Unit)
     */
    @Override
    public void setInUnit(final int row, final int column, double value, U valueUnit) throws ValueException
    {
        setSI(row, column, expressAsSIUnit(value, valueUnit));
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrixFunctions#zSum()
     */
    public double zSum()
    {
        return this.matrixSI.zSum();
    }

    /**
     * @see org.opentrafficsim.core.value.MatrixFunctions#normalize()
     */
    public void normalize() throws ValueException
    {
        double sum = this.zSum();
        if (sum == 0)
            throw new ValueException("DoubleMatrix.normalize: zSum of the vector values == 0, cannot normalize");
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
     * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrixFunctions#det()
     */
    @Override
    public double det() throws ValueException
    {
        if (this instanceof Sparse)
            return new SparseDoubleAlgebra().det(this.matrixSI);
        if (this instanceof Dense)
            return new DenseDoubleAlgebra().det(this.matrixSI);
        throw new ValueException("DoubleMatrix.det -- matrix implements neither Sparse nor Dense");
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        // unequal if object is of a different type.
        if (!(obj instanceof DoubleMatrix<?>))
            return false;
        DoubleMatrix<?> fm = (DoubleMatrix<?>) obj;

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
        this.matrixSI.assign(DoubleFunctions.abs);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#acos()
     */
    @Override
    public void acos()
    {
        this.matrixSI.assign(DoubleFunctions.acos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#asin()
     */
    @Override
    public void asin()
    {
        this.matrixSI.assign(DoubleFunctions.asin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#atan()
     */
    @Override
    public void atan()
    {
        this.matrixSI.assign(DoubleFunctions.atan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cbrt()
     */
    @Override
    public void cbrt()
    {
        this.matrixSI.assign(DoubleMathFunctionsImpl.cbrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#ceil()
     */
    @Override
    public void ceil()
    {
        this.matrixSI.assign(DoubleFunctions.ceil);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cos()
     */
    @Override
    public void cos()
    {
        this.matrixSI.assign(DoubleFunctions.cos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cosh()
     */
    @Override
    public void cosh()
    {
        this.matrixSI.assign(DoubleMathFunctionsImpl.cosh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#exp()
     */
    @Override
    public void exp()
    {
        this.matrixSI.assign(DoubleFunctions.exp);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#expm1()
     */
    @Override
    public void expm1()
    {
        this.matrixSI.assign(DoubleMathFunctionsImpl.expm1);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#floor()
     */
    @Override
    public void floor()
    {
        this.matrixSI.assign(DoubleFunctions.floor);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log()
     */
    @Override
    public void log()
    {
        this.matrixSI.assign(DoubleFunctions.log);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log10()
     */
    @Override
    public void log10()
    {
        this.matrixSI.assign(DoubleMathFunctionsImpl.log10);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log1p()
     */
    @Override
    public void log1p()
    {
        this.matrixSI.assign(DoubleMathFunctionsImpl.log1p);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#pow(double)
     */
    @Override
    public void pow(double x)
    {
        this.matrixSI.assign(DoubleFunctions.pow(x));
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#rint()
     */
    @Override
    public void rint()
    {
        this.matrixSI.assign(DoubleFunctions.rint);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#round()
     */
    @Override
    public void round()
    {
        this.matrixSI.assign(DoubleMathFunctionsImpl.round);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#signum()
     */
    @Override
    public void signum()
    {
        this.matrixSI.assign(DoubleMathFunctionsImpl.signum);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sin()
     */
    @Override
    public void sin()
    {
        this.matrixSI.assign(DoubleFunctions.sin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sinh()
     */
    @Override
    public void sinh()
    {
        this.matrixSI.assign(DoubleMathFunctionsImpl.sinh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sqrt()
     */
    @Override
    public void sqrt()
    {
        this.matrixSI.assign(DoubleFunctions.sqrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tan()
     */
    @Override
    public void tan()
    {
        this.matrixSI.assign(DoubleFunctions.tan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tanh()
     */
    @Override
    public void tanh()
    {
        this.matrixSI.assign(DoubleMathFunctionsImpl.tanh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toDegrees()
     */
    @Override
    public void toDegrees()
    {
        this.matrixSI.assign(DoubleMathFunctionsImpl.toDegrees);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toRadians()
     */
    @Override
    public void toRadians()
    {
        this.matrixSI.assign(DoubleMathFunctionsImpl.toRadians);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#inv()
     */
    @Override
    public void inv()
    {
        this.matrixSI.assign(DoubleFunctions.inv);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.DoubleMathFunctions#multiply(double)
     */
    @Override
    public void multiply(double constant)
    {
        this.matrixSI.assign(DoubleFunctions.mult(constant));
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.DoubleMathFunctions#divide(double)
     */
    @Override
    public void divide(double constant)
    {
        this.matrixSI.assign(DoubleFunctions.div(constant));
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
                double f = expressAsUnit(this.matrixSI.get(i, j), displayUnit);
                if (Math.abs(f) > 0.01 && Math.abs(f) < 999.0)
                    s += " " + String.format("%8.3f", f);
                else
                    s += " " + String.format("%8.3e", f);
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
    public void add(final DoubleMatrixRel<U> matrix) throws ValueException
    {
        if (rows() != matrix.rows() || columns() != matrix.columns())
            throw new ValueException("DoubleMatrix.add - two matrices have unequal size: " + rows() + "x" + columns()
                    + " != " + matrix.rows() + "x" + matrix.columns());
        this.matrixSI.assign(matrix.matrixSI, DoubleFunctions.plus);
    }

    /**
     * Subtract another value from this value. Only relative values are allowed; subtracting an absolute value from a
     * relative value is not allowed. Subtracting an absolute value from an existing absolute value would require the
     * result to become relative, which is a type change that is impossible. For that operation, use a static method.
     * @param matrix the value to subtract
     * @throws ValueException when matrices have unequal size
     */
    public void subtract(final DoubleMatrixRel<U> matrix) throws ValueException
    {
        if (rows() != matrix.rows() || columns() != matrix.columns())
            throw new ValueException("DoubleMatrix.subtract - two matrices have unequal size: " + rows() + "x"
                    + columns() + " != " + matrix.rows() + "x" + matrix.columns());
        this.matrixSI.assign(matrix.matrixSI, DoubleFunctions.minus);
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
    public static <U extends Unit<U>> DoubleMatrixAbs<U> plus(final DoubleMatrixAbs<U> x, final DoubleMatrixRel<U> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("DoubleMatrix.plus - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        DoubleMatrixAbs<U> c = x.copy();
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
    public static <U extends Unit<U>> DoubleMatrixAbs<U> plus(final DoubleMatrixRel<U> x, final DoubleMatrixAbs<U> y)
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
    public static <U extends Unit<U>> DoubleMatrixRel<U> plus(final DoubleMatrixRel<U> x, final DoubleMatrixRel<U> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("DoubleMatrix.plus - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        DoubleMatrixRel<U> c = x.copy();
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
    public static <U extends Unit<U>> DoubleMatrixRel<U> minus(final DoubleMatrixRel<U> x, final DoubleMatrixRel<U> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("DoubleMatrix.minus - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        DoubleMatrixRel<U> c = x.copy();
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
    public static <U extends Unit<U>> DoubleMatrixAbs<U> minus(final DoubleMatrixAbs<U> x, final DoubleMatrixRel<U> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("DoubleMatrix.minus - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        DoubleMatrixAbs<U> c = x.copy();
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
    public static <U extends Unit<U>> DoubleMatrixRel<U> minus(final DoubleMatrixAbs<U> x, final DoubleMatrixAbs<U> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("DoubleMatrix.minus - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        DoubleMatrixRel<U> c = null;
        if (x instanceof Dense)
            c = new DoubleMatrixRelDense<U>(x.getValuesSI(), x.unit.getStandardUnit());
        else if (x instanceof Sparse)
            c = new DoubleMatrixRelSparse<U>(x.getValuesSI(), x.unit.getStandardUnit());
        else
            throw new ValueException("DoubleVector.minus - vector neither sparse nor dense");

        c.matrixSI.assign(y.matrixSI, DoubleFunctions.minus);
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
    public static DoubleMatrixAbs<SIUnit> multiply(final DoubleMatrixAbs<?> x, final DoubleMatrixAbs<?> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("DoubleMatrix.multiply - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(x.getUnit().getSICoefficients(),
                        y.getUnit().getSICoefficients()).toString());

        @SuppressWarnings("unchecked")
        DoubleMatrixAbs<SIUnit> c = (DoubleMatrixAbs<SIUnit>) x.copy();
        c.matrixSI.assign(y.matrixSI, DoubleFunctions.mult);
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
    public static DoubleMatrixRel<SIUnit> multiply(final DoubleMatrixRel<?> x, final DoubleMatrixRel<?> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("DoubleMatrix.multiply - two matrices have unequal size: " + x.rows() + "x"
                    + x.columns() + " != " + y.rows() + "x" + y.columns());

        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(x.getUnit().getSICoefficients(),
                        y.getUnit().getSICoefficients()).toString());

        @SuppressWarnings("unchecked")
        DoubleMatrixRel<SIUnit> c = (DoubleMatrixRel<SIUnit>) x.copy();
        c.matrixSI.assign(y.matrixSI, DoubleFunctions.mult);
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
    public static <U extends Unit<U>> DoubleMatrixAbs<U> multiply(final DoubleMatrixAbs<U> x, final double[][] c)
            throws ValueException
    {
        if (x.rows() != c.length || x.columns() != (c.length > 0 ? c[0].length : 0))
            throw new ValueException(
                    "DoubleMatrix.multiply with dimensionless matrix- two matrices have unequal size: " + x.rows()
                            + "x" + x.columns() + " != " + c.length + "x" + (c.length > 0 ? c[0].length : 0));

        // TODO: more elegant implementation that does not copy the entire matrix?
        DoubleMatrixAbs<U> result = x.copy();
        DenseDoubleMatrix2D cMatrix = new DenseDoubleMatrix2D(c);
        result.matrixSI.assign(cMatrix, DoubleFunctions.mult);
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
    public static <U extends Unit<U>> DoubleMatrixRel<U> multiply(final DoubleMatrixRel<U> x, final double[][] c)
            throws ValueException
    {
        if (x.rows() != c.length || x.columns() != (c.length > 0 ? c[0].length : 0))
            throw new ValueException(
                    "DoubleMatrix.multiply with dimensionless matrix- two matrices have unequal size: " + x.rows()
                            + "x" + x.columns() + " != " + c.length + "x" + (c.length > 0 ? c[0].length : 0));

        // TODO: more elegant implementation that does not copy the entire matrix?
        DoubleMatrixRel<U> result = x.copy();
        DenseDoubleMatrix2D cMatrix = new DenseDoubleMatrix2D(c);
        result.matrixSI.assign(cMatrix, DoubleFunctions.mult);
        return result;
    }

    /**
     * Convert sparse matrix to dense matrix.
     * @param x the matrix to convert
     * @return the converted matrix
     */
    public static <U extends Unit<U>> DoubleMatrixAbsDense<U> sparseToDense(final DoubleMatrixAbsSparse<U> x)
    {
        DoubleMatrixAbsDense<U> v = new DoubleMatrixAbsDense<U>(x.getValuesSI(), x.getUnit().getStandardUnit());
        v.unit = x.unit;
        return v;
    }

    /**
     * Convert sparse matrix to dense matrix.
     * @param x the matrix to convert
     * @return the converted matrix
     */
    public static <U extends Unit<U>> DoubleMatrixRelDense<U> sparseToDense(final DoubleMatrixRelSparse<U> x)
    {
        DoubleMatrixRelDense<U> v = new DoubleMatrixRelDense<U>(x.getValuesSI(), x.getUnit().getStandardUnit());
        v.unit = x.unit;
        return v;
    }

    /**
     * Convert dense matrix to sparse matrix.
     * @param x the matrix to convert
     * @return the converted matrix
     */
    public static <U extends Unit<U>> DoubleMatrixAbsSparse<U> denseToSparse(final DoubleMatrixAbsDense<U> x)
    {
        DoubleMatrixAbsSparse<U> v = new DoubleMatrixAbsSparse<U>(x.getValuesSI(), x.getUnit().getStandardUnit());
        v.unit = x.unit;
        return v;
    }

    /**
     * Convert dense matrix to sparse matrix.
     * @param x the matrix to convert
     * @return the converted matrix
     */
    public static <U extends Unit<U>> DoubleMatrixRelSparse<U> denseToSparse(final DoubleMatrixRelDense<U> x)
    {
        DoubleMatrixRelSparse<U> v = new DoubleMatrixRelSparse<U>(x.getValuesSI(), x.getUnit().getStandardUnit());
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
    public static DoubleVectorAbs<SIUnit> solve(final DoubleMatrixAbs<?> A, final DoubleVectorAbs<?> b)
            throws ValueException
    {
        // TODO: is this correct? Should lookup matrix algebra to find out unit for x when solving A*x = b ?
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(b.getUnit().getSICoefficients(),
                        A.getUnit().getSICoefficients()).toString());

        // TODO: should the algorithm throw an exception when rows/columns do not match when solving A*x = b ?
        DoubleMatrix2D A2D = A.getMatrixSI();
        DoubleMatrix1D b1D = b.getVectorSI();
        if (A instanceof Sparse)
        {
            DoubleMatrix1D x1D = new SparseDoubleAlgebra().solve(A2D, b1D);
            DoubleVectorAbsSparse<SIUnit> x = new DoubleVectorAbsSparse<SIUnit>(x1D.toArray(), targetUnit);
            return x;
        }
        if (A instanceof Dense)
        {
            DoubleMatrix1D x1D = new DenseDoubleAlgebra().solve(A2D, b1D);
            DoubleVectorAbsDense<SIUnit> x = new DoubleVectorAbsDense<SIUnit>(x1D.toArray(), targetUnit);
            return x;
        }
        throw new ValueException("DoubleMatrix.det -- matrix implements neither Sparse nor Dense");
    }

    /**
     * Solve x for A*x = b. According to Colt: x; a new independent matrix; solution if A is square, least squares
     * solution if A.rows() > A.columns(), underdetermined system solution if A.rows() < A.columns().
     * @param A matrix A in A*x = b
     * @param b vector b in A*x = b
     * @return vector x in A*x = b
     * @throws ValueException when Matrix A is neither Sparse nor Dense.
     */
    public static DoubleVectorRel<SIUnit> solve(final DoubleMatrixRel<?> A, final DoubleVectorRel<?> b)
            throws ValueException
    {
        // TODO: is this correct? Should lookup matrix algebra to find out unit for x when solving A*x = b ?
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(b.getUnit().getSICoefficients(),
                        A.getUnit().getSICoefficients()).toString());

        // TODO: should the algorithm throw an exception when rows/columns do not match when solving A*x = b ?
        DoubleMatrix2D A2D = A.getMatrixSI();
        DoubleMatrix1D b1D = b.getVectorSI();
        if (A instanceof Sparse)
        {
            DoubleMatrix1D x1D = new SparseDoubleAlgebra().solve(A2D, b1D);
            DoubleVectorRelSparse<SIUnit> x = new DoubleVectorRelSparse<SIUnit>(x1D.toArray(), targetUnit);
            return x;
        }
        if (A instanceof Dense)
        {
            DoubleMatrix1D x1D = new DenseDoubleAlgebra().solve(A2D, b1D);
            DoubleVectorRelDense<SIUnit> x = new DoubleVectorRelDense<SIUnit>(x1D.toArray(), targetUnit);
            return x;
        }
        throw new ValueException("DoubleMatrix.det -- matrix implements neither Sparse nor Dense");
    }

}

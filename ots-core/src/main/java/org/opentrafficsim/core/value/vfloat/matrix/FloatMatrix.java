package org.opentrafficsim.core.value.vfloat.matrix;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Matrix;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.FloatMathFunctions;
import org.opentrafficsim.core.value.vfloat.FloatMathFunctionsImpl;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

import cern.colt.matrix.tfloat.FloatMatrix2D;
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
public abstract class FloatMatrix<U extends Unit<U>> extends Matrix<U> implements FloatMathFunctions
{
    /** */
    private static final long serialVersionUID = 20140618L;

    /** the internal storage for the matrix; internally they are stored in SI units; can be dense or sparse */
    protected FloatMatrix2D matrixSI;

    /**
     * Construct the matrix and store the values in SI units.
     * @param values a 2D array of values for the constructor
     * @param unit the unit of the values
     */
    public FloatMatrix(float[][] values, final U unit)
    {
        super(unit);
        if (unit.equals(unit.getStandardUnit()))
        {
            this.matrixSI = createMatrix2D(values.length, values[0].length);
            this.matrixSI.assign(values);
        }
        else
        {
            this.matrixSI = createMatrix2D(values.length, values[0].length);
            for (int row = 0; row < values.length; row++)
            {
                for (int column = 0; column < values[0].length; column++)
                {
                    this.matrixSI.set(row, column, (float) convertToSIUnit(values[row][column]));
                }
            }
        }
    }

    /**
     * Construct the matrix and store the values in SI units.
     * @param values an array of values for the constructor
     * @throws ValueException exception thrown when array with zero elements is offered
     */
    public FloatMatrix(FloatScalar<U>[][] values) throws ValueException
    {
        super(values.length > 0 && values[0].length > 0 ? values[0][0].getUnit() : null);
        if (values.length == 0 || values[0].length == 0)
        {
            throw new ValueException(
                    "FloatMatrix constructor called with an empty row or column of FloatScalar elements");
        }

        this.matrixSI = createMatrix2D(values.length, values[0].length);
        for (int row = 0; row < values.length; row++)
        {
            for (int column = 0; column < values[0].length; column++)
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
    protected abstract FloatMatrix2D createMatrix2D(int rows, int columns);

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
            for (int j = 0; j < values[0].length; j++)
                values[i][j] = (float) convertToSpecifiedUnit(values[i][j]);
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
            for (int j = 0; j < values[0].length; j++)
                values[i][j] = (float) convertToUnit(values[i][j], targetUnit);
        return values;
    }

    /**
     * @return the number of rows of the matrix as an int
     */
    public int rows()
    {
        return this.matrixSI.rows();
    }

    /**
     * @return the number of columns of the matrix as an int
     */
    public int columns()
    {
        return this.matrixSI.columns();
    }

    /**
     * Create a deep copy of the matrix, independent of the original matrix.
     * @return a deep copy of the absolute / relative, dense / sparse matrix
     */
    public abstract FloatMatrix<U> copy();

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
                float f = (float) convertToUnit(this.matrixSI.get(i, j), displayUnit);
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
    public void add(final FloatMatrixRel<U> matrix) throws ValueException
    {
        if (rows() != matrix.rows() || columns() != matrix.columns())
            throw new ValueException("FloatMatrix.add - two matrices have unequal size: rows " + rows() + " != "
                    + matrix.rows() + " or columns " + columns() + " != " + matrix.columns());
        this.matrixSI.assign(matrix.matrixSI, FloatFunctions.plus);
    }

    /**
     * Subtract another value from this value. Only relative values are allowed; subtracting an absolute value from a
     * relative value is not allowed. Subtracting an absolute value from an existing absolute value would require the
     * result to become relative, which is a type change that is impossible. For that operation, use a static method.
     * @param matrix the value to subtract
     * @throws ValueException when matrices have unequal size
     */
    public void subtract(FloatMatrixRel<U> matrix) throws ValueException
    {
        if (rows() != matrix.rows() || columns() != matrix.columns())
            throw new ValueException("FloatMatrix.add - two matrices have unequal size: rows " + rows() + " != "
                    + matrix.rows() + " or columns " + columns() + " != " + matrix.columns());
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
     * @return new Vector with absolute elements sum of x[i] and y[i]
     * @throws ValueException when matrices have unequal size
     */
    public static <U extends Unit<U>> FloatMatrixAbs<U> plus(FloatMatrixAbs<U> x, FloatMatrixRel<U> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("FloatMatrix.add - two matrices have unequal size: rows " + x.rows() + " != "
                    + y.rows() + " or columns " + x.columns() + " != " + y.columns());

        FloatMatrixAbs<U> c = (FloatMatrixAbs<U>) x.copy();
        c.add(y);
        return c;
    }

    /**
     * Add a matrix with relative values x and a matrix with absolute values y. The target unit will be the unit of
     * absolute value y.
     * @param x relative matrix 1
     * @param y absolute matrix 2
     * @param targetUnit unit in which the results will be displayed
     * @return new Vector with absolute elements sum of x[i] and y[i]
     * @throws ValueException when matrices have unequal size
     */
    public static <U extends Unit<U>> FloatMatrixAbs<U> plus(FloatMatrixRel<U> x, FloatMatrixAbs<U> y)
            throws ValueException
    {
        return plus(y, x);
    }

    /**
     * Add a matrix with relative values x and a matrix with relative values y. The target unit will be the unit of
     * relative value x.
     * @param x relative matrix 1
     * @param y relative matrix 2
     * @return new Vector with absolute elements sum of x[i] and y[i]
     * @throws ValueException when matrices have unequal size
     */
    public static <U extends Unit<U>> FloatMatrixRel<U> plus(FloatMatrixRel<U> x, FloatMatrixRel<U> y)
            throws ValueException
    {
        if (x.rows() != y.rows() || x.columns() != y.columns())
            throw new ValueException("FloatMatrix.add - two matrices have unequal size: rows " + x.rows() + " != "
                    + y.rows() + " or columns " + x.columns() + " != " + y.columns());

        FloatMatrixRel<U> c = (FloatMatrixRel<U>) x.copy();
        c.add(y);
        return c;
    }

    // TODO: subtract

    // TODO: normalize

    // TODO: zdotproduct

    // TODO: zsum

}

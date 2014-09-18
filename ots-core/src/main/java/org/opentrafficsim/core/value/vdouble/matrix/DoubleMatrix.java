package org.opentrafficsim.core.value.vdouble.matrix;

import java.io.Serializable;

import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.AbstractValue;
import org.opentrafficsim.core.value.DenseData;
import org.opentrafficsim.core.value.Format;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.SparseData;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.ValueUtil;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVector;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.SparseDoubleAlgebra;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

/**
 * Immutable double matrix.
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
 * @version Sep 9, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit
 */
public abstract class DoubleMatrix<U extends Unit<U>> extends AbstractValue<U> implements Serializable,
        ReadOnlyDoubleMatrixFunctions<U>
{

    /** */
    private static final long serialVersionUID = 20140909L;

    /** the internal storage for the vector; internally they are stored in SI units; can be dense or sparse. */
    protected DoubleMatrix2D matrixSI;

    /**
     * @param unit
     */
    protected DoubleMatrix(final U unit)
    {
        super(unit);
    }

    /**
     * @param <U> Unit
     */
    public abstract static class Abs<U extends Unit<U>> extends DoubleMatrix<U> implements Absolute
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a Absolute Immutable DoubleMatrix.
         * @param unit Unit; the unit of the new DoubleMatrix
         */
        protected Abs(final U unit)
        {
            super(unit);
        }

        /**
         * @param <U> Unit
         */
        public static class Dense<U extends Unit<U>> extends Abs<U> implements DenseData
        {
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * For package internal use only.
             * @param values DoubleMatrix2D; the values to assign to the entries in the new DoubleMatrix
             * @param unit Unit; the unit of the values in the new DoubleMatrix
             */
            protected Dense(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values); // shallow copy
            }

            /**
             * Create a new Absolute Dense Immutable DoubleMatrix.
             * @param values float[][]; the values to assign to the entries in the new DoubleMatrix
             * @param unit Unit; the unit of the values in the new DoubleMatrix
             * @throws ValueException when values is not rectangular
             */
            public Dense(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Create a new Absolute Dense Immutable DoubleMatrix.
             * @param values DoubleScalar.Abs[][]; the values to assign to the entries in the new DoubleMatrix
             * @throws ValueException when values is not rectangular or contains no values
             */
            public Dense(final DoubleScalar.Abs<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrix#mutable()
             */
            @Override
            public final MutableDoubleMatrix.Abs.Dense<U> mutable()
            {
                return new MutableDoubleMatrix.Abs.Dense<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.AbstractDoubleMatrix#createMatrix2D(int, int)
             */
            @Override
            protected final DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new DenseDoubleMatrix2D(rows, columns);
            }

            /**
             * @see org.opentrafficsim.core.value.Value#copy()
             */
            @Override
            public final DoubleMatrix.Abs.Dense<U> copy()
            {
                return this;
            }

        }

        /**
         * @param <U> Unit
         */
        public static class Sparse<U extends Unit<U>> extends Abs<U> implements SparseData
        {
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * For package internal use only.
             * @param values DoubleMatrix2D; the values to assign to the entries of the new DoubleMatrix
             * @param unit Unit; unit of the values in the new DoubleMatrix
             */
            protected Sparse(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values); // shallow copy
            }

            /**
             * Create a Relative Sparse Immutable DoubleMatrix.
             * @param values double[][]; values to assign to the entries of the new DoubleMatrix
             * @param unit Unit; unit of the values in the new DoubleMatrix
             * @throws ValueException when values is not rectangular, or contains no entries
             */
            public Sparse(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Create a new Absolute Sparse Immutable DoubleMatrix.
             * @param values DoubleScalar.Abs[][]; values to assign to the entries in the new DoubleMatrix
             * @throws ValueException when values is not rectangular or contains no entries
             */
            public Sparse(final DoubleScalar.Abs<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrix#mutable()
             */
            @Override
            public MutableDoubleMatrix.Abs.Sparse<U> mutable()
            {
                return new MutableDoubleMatrix.Abs.Sparse<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.AbstractDoubleMatrix#createMatrix2D(int, int)
             */
            @Override
            protected final DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new DenseDoubleMatrix2D(rows, columns);
            }

            /**
             * @see org.opentrafficsim.core.value.Value#copy()
             */
            @Override
            public final DoubleMatrix.Abs.Sparse<U> copy()
            {
                return this;
            }
        }

        /**
         * @see org.opentrafficsim.core.value.vdouble.matrix.ReadOnlyDoubleMatrixFunctions#get(int, int)
         */
        @Override
        public final DoubleScalar<U> get(final int row, final int column) throws ValueException
        {
            return new DoubleScalar.Abs<U>(getInUnit(row, column, this.unit), this.unit);
        }

    }

    /**
     * @param <U> Unit
     */
    public abstract static class Rel<U extends Unit<U>> extends DoubleMatrix<U> implements Relative
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a Relative Immutable DoubleMatrix.
         * @param unit Unit; the unit of the new DoubleMatrix
         */
        protected Rel(final U unit)
        {
            super(unit);
        }

        /**
         * @param <U>
         */
        public static class Dense<U extends Unit<U>> extends Rel<U> implements DenseData
        {
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * For package internal use only.
             * @param values DoubleMatrix2D; the values to assign to the entries in the new DoubleMatrix
             * @param unit Unit; the unit of the values in the new DoubleMatrix
             */
            protected Dense(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values); // shallow copy
            }

            /**
             * Create a new Relative Dense Immutable DoubleMatrix.
             * @param values double[][]; the values to assign to the entries in the new DoubleMatrix
             * @param unit Unit; the unit of the values in the new DoubleMatrix
             * @throws ValueException when values is not rectangular
             */
            public Dense(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Create a new Relative Dense Immutable DoubleMatrix.
             * @param values DoubleScalar.Rel; the values to assign to the entries in the new DoubleMatrix
             * @throws ValueException when values is not rectangular or contains no entries
             */
            public Dense(final DoubleScalar.Rel<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrix#mutable()
             */
            @Override
            public final MutableDoubleMatrix.Rel.Dense<U> mutable()
            {
                return new MutableDoubleMatrix.Rel.Dense<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.AbstractDoubleMatrix#createMatrix2D(int, int)
             */
            @Override
            protected final DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new SparseDoubleMatrix2D(rows, columns);
            }

            /**
             * @see org.opentrafficsim.core.value.Value#copy()
             */
            @Override
            public final DoubleMatrix.Rel.Dense<U> copy()
            {
                return this;
            }

        }

        /**
         * @param <U> Unit
         */
        public static class Sparse<U extends Unit<U>> extends Rel<U> implements SparseData
        {
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * For package internal use only.
             * @param values DoubleMatrix2D; the values to assign to the entries in the new DoubleMatrix
             * @param unit Unit; the unit of the values in the new DoubleMatrix
             */
            protected Sparse(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values); // shallow copy
            }

            /**
             * Create a new Sparse Relative Immutable DoubleMatrix.
             * @param values DoubleMatrix2D; the values to assign to the entries in the new DoubleMatrix
             * @param unit Unit; the unit of the values in the new DoubleMatrix
             * @throws ValueException when values is not rectangular
             */
            public Sparse(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Create a Relative Sparse Immutable DoubleMatrix.
             * @param values DoubleScalar.Rel[][]; 2D array of values to assign to the entries in the new DoubleMatrix
             * @throws ValueException when values contains zero entries
             */
            public Sparse(final DoubleScalar.Rel<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrix#mutable()
             */
            @Override
            public final MutableDoubleMatrix.Rel.Sparse<U> mutable()
            {
                return new MutableDoubleMatrix.Rel.Sparse<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.AbstractDoubleMatrix#createMatrix2D(int, int)
             */
            @Override
            protected final DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new SparseDoubleMatrix2D(rows, columns);
            }

            /**
             * @see org.opentrafficsim.core.value.Value#copy()
             */
            @Override
            public final DoubleMatrix.Rel.Sparse<U> copy()
            {
                return this;
            }

        }

        /**
         * @see org.opentrafficsim.core.value.vdouble.matrix.ReadOnlyDoubleMatrixFunctions#get(int, int)
         */
        @Override
        public final DoubleScalar<U> get(final int row, final int column) throws ValueException
        {
            return new DoubleScalar.Rel<U>(getInUnit(row, column, this.unit), this.unit);
        }

    }

    /**
     * Create a mutable version of this DoubleMatrix. <br />
     * The mutable version is created with a shallow copy of the data and the internal copyOnWrite flag set. The first
     * operation in the mutable version that modifies the data shall trigger a deep copy of the data.
     * @return MutableDoubleMatrix; mutable version of this DoubleMatrix
     */
    public abstract MutableDoubleMatrix<U> mutable();

    /**
     * Import the values and convert them into SI units.
     * @param values an array of values
     * @throws ValueException
     */
    protected final void initialize(final double[][] values) throws ValueException
    {
        ensureRectangular(values);
        this.matrixSI = createMatrix2D(values.length, 0 == values.length ? 0 : values[0].length);
        if (this.unit.equals(this.unit.getStandardUnit()))
        {
            this.matrixSI.assign(values);
        }
        else
        {
            for (int row = 0; row < values.length; row++)
            {
                for (int column = 0; column < values[row].length; column++)
                {
                    safeSet(row, column, expressAsSIUnit(values[row][column]));
                }
            }
        }
    }

    /**
     * @param values
     */
    protected final void initialize(final DoubleMatrix2D values)
    {
        this.matrixSI = values;
    }

    /**
     * Construct the vector and store the values in SI units.
     * @param values double[][] a 2D array of values for the constructor
     * @throws ValueException exception thrown when array with zero elements is offered
     */
    protected final void initialize(final DoubleScalar<U>[][] values) throws ValueException
    {
        ensureRectangularAndNonEmpty(values);
        this.matrixSI = createMatrix2D(values.length, values[0].length);
        for (int row = 0; row < values.length; row++)
        {
            for (int column = 0; column < values[0].length; column++)
            {
                safeSet(row, column, values[row][column].getValueSI());
            }
        }
    }

    /**
     * This method has to be implemented by each leaf class.
     * @param rows the number of rows in the matrix
     * @param columns the number of columns in the matrix
     * @return an instance of the right type of matrix (absolute / relative, dense / sparse, etc.).
     */
    protected abstract DoubleMatrix2D createMatrix2D(final int rows, final int columns);

    /**
     * Create a double[][] array filled with the values in SI unit.
     * @return double[][]; array of values in SI unit
     */
    public final double[][] getValuesSI()
    {
        return this.matrixSI.toArray(); // this makes a deep copy
    }

    /**
     * Create a double[][] array filled with the values in the original unit.
     * @return values in original unit
     */
    public final double[][] getValuesInUnit()
    {
        return getValuesInUnit(this.unit);
    }

    /**
     * Create a double[][] array filled with the values in the specified unit.
     * @param targetUnit the unit to convert the values to
     * @return values in specific target unit
     */
    public final double[][] getValuesInUnit(final U targetUnit)
    {
        double[][] values = this.matrixSI.toArray();
        for (int row = 0; row < values.length; row++)
        {
            for (int column = 0; column < values[0].length; column++)
            {
                values[row][column] = ValueUtil.expressAsUnit(values[row][column], targetUnit);
            }
        }
        return values;
    }

    /** {@inheritDoc} */
    @Override
    public final int rows()
    {
        return this.matrixSI.rows();
    }

    /** {@inheritDoc} */
    @Override
    public final int columns()
    {
        return this.matrixSI.columns();
    }

    /** {@inheritDoc} */
    @Override
    public final double getSI(final int row, final int column) throws ValueException
    {
        checkIndex(row, column);
        return safeGet(row, column);
    }

    /** {@inheritDoc} */
    @Override
    public final double getInUnit(final int row, final int column) throws ValueException
    {
        return expressAsSpecifiedUnit(getSI(row, column));
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.ReadOnlyDoubleVectorFunctions#getInUnit(int,
     *      org.opentrafficsim.core.unit.Unit)
     */
    @Override
    public final double getInUnit(final int row, final int column, final U targetUnit) throws ValueException
    {
        return ValueUtil.expressAsUnit(getSI(row, column), targetUnit);
    }

    /** {@inheritDoc} */
    @Override
    public final double zSum()
    {
        return this.matrixSI.zSum();
    }

    /** {@inheritDoc} */
    @Override
    public final int cardinality()
    {
        return this.matrixSI.cardinality();
    }

    /** {@inheritDoc} */
    @Override
    public final double det() throws ValueException
    {
        try
        {
            if (this instanceof SparseData)
            {
                // System.out.println("calling SparseDoubleAlgebra().det(this.matrixSI)");
                return new SparseDoubleAlgebra().det(this.matrixSI);
            }
            if (this instanceof DenseData)
            {
                // System.out.println("calling DenseDoubleAlgebra().det(this.matrixSI)");
                return new DenseDoubleAlgebra().det(this.matrixSI);
            }
            throw new ValueException("DoubleMatrix.det -- matrix implements neither Sparse nor Dense");
        }
        catch (IllegalArgumentException exception)
        {
            if (!exception.getMessage().startsWith("Matrix must be square"))
            {
                exception.printStackTrace();
            }
            throw new ValueException(exception.getMessage()); // probably Matrix must be square
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return toString(this.unit);
    }

    /**
     * Print this AbstractDoubleVector with the values expressed in the specified unit.
     * @param displayUnit the unit to display the vector in.
     * @return a printable String with the vector contents
     */
    public final String toString(final U displayUnit)
    {
        StringBuffer buf = new StringBuffer();
        if (this instanceof MutableDoubleMatrix)
        {
            buf.append("Mutable   ");
            if (this instanceof MutableDoubleMatrix.Abs.Dense)
            {
                buf.append("Abs Dense  ");
            }
            else if (this instanceof MutableDoubleMatrix.Rel.Dense)
            {
                buf.append("Rel Dense  ");
            }
            else if (this instanceof MutableDoubleMatrix.Abs.Sparse)
            {
                buf.append("Abs Sparse ");
            }
            else if (this instanceof MutableDoubleMatrix.Rel.Sparse)
            {
                buf.append("Rel Sparse ");
            }
            else
            {
                buf.append("??? ");
            }
        }
        else
        {
            buf.append("Immutable ");
            if (this instanceof DoubleMatrix.Abs.Dense)
            {
                buf.append("Abs Dense  ");
            }
            else if (this instanceof DoubleMatrix.Rel.Dense)
            {
                buf.append("Rel Dense  ");
            }
            else if (this instanceof DoubleMatrix.Abs.Sparse)
            {
                buf.append("Abs Sparse ");
            }
            else if (this instanceof DoubleMatrix.Rel.Sparse)
            {
                buf.append("Rel Sparse ");
            }
            else
            {
                buf.append("??? ");
            }
        }
        buf.append("[" + displayUnit.getAbbreviation() + "]");
        for (int row = 0; row < this.matrixSI.rows(); row++)
        {
            buf.append("\r\n\t");
            for (int column = 0; column < this.matrixSI.columns(); column++)
            {
                double f = ValueUtil.expressAsUnit(safeGet(row, column), displayUnit);
                buf.append(" " + Format.format(f));
            }
        }
        return buf.toString();
    }

    /**
     * Centralized size equality check.
     * @param other DoubleMatrix<U>; other DoubleVector
     * @throws ValueException when vectors have unequal size
     */
    protected final void checkSize(final DoubleMatrix<?> other) throws ValueException
    {
        if (rows() != other.rows() || columns() != other.columns())
        {
            throw new ValueException("The matrices have different sizes: " + rows() + "x" + columns() + " != "
                    + other.rows() + "x" + other.columns());
        }
    }

    /**
     * Centralized size equality check.
     * @param other double[][]; array of double
     * @throws ValueException when vectors have unequal size
     */
    protected final void checkSize(final double[][] other) throws ValueException
    {
        if (rows() != other.length || columns() != other[0].length)
        {
            throw new ValueException("The vector and the array have different sizes: " + rows() + " != " + other.length);
        }
        ensureRectangular(other);
    }

    /**
     * Check that a 2D array of double is rectangular; i.e. all rows have the same length.
     * @param values double[][]; the 2D array to check
     * @throws ValueException when values is not rectangular
     */
    private static void ensureRectangular(final double[][] values) throws ValueException
    {
        for (int row = 1; row < values.length; row++)
        {
            if (values[0].length != values[row].length)
            {
                throw new ValueException("Lengths of rows are not all the same");
            }
        }
    }

    /**
     * Check that a 2D array of double is rectangular; i.e. all rows have the same length.
     * @param values DoubleScalar[][]; the 2D array to check
     * @throws ValueException when values is not rectangular
     */
    private static void ensureRectangularAndNonEmpty(final DoubleScalar<?>[][] values) throws ValueException
    {
        if (0 == values.length || 0 == values[0].length)
        {
            throw new ValueException("Cannot determine unit for DoubleMatrix from an empty array of DoubleScalar");
        }
        for (int row = 1; row < values.length; row++)
        {
            if (values[0].length != values[row].length)
            {
                throw new ValueException("Lengths of rows are not all the same");
            }
        }
    }

    /**
     * Check that provided row and column indices are valid.
     * @param row integer; the row value to check
     * @param column integer; the column value to check
     * @throws ValueException when row or column has an invalid value
     */
    protected final void checkIndex(final int row, final int column) throws ValueException
    {
        if (row < 0 || row >= this.matrixSI.rows() || column < 0 || column >= this.matrixSI.columns())
        {
            throw new ValueException("index out of range (valid range is 0.." + (this.matrixSI.rows() - 1) + ", 0.."
                    + this.matrixSI.columns() + ", got " + row + ", " + column + ")");
        }
    }

    /**
     * Retrieve a value in vectorSI without checking validity of the index.
     * @param row integer; the row where the value must be retrieved
     * @param column integer; the column where the value must be retrieved
     * @return double; the value stored at the indicated row and column
     */
    protected final double safeGet(final int row, final int column)
    {
        return this.matrixSI.getQuick(row, column);
    }

    /**
     * Modify a value in vectorSI without checking validity of the indices.
     * @param row integer; the row where the value must be stored
     * @param column integer; the column where the value must be stored
     * @param valueSI double; the new value for the entry in vectorSI
     */
    protected final void safeSet(final int row, final int column, final double valueSI)
    {
        this.matrixSI.setQuick(row, column, valueSI);
    }

    /**
     * Create a deep copy of the data.
     * @return DoubleMatrix2D; deep copy of the data
     */
    protected final DoubleMatrix2D deepCopyOfData()
    {
        return this.matrixSI.copy();
    }

    /**
     * Check that a provided array can be used to create some descendant of an AbstractDoubleMatrix.
     * @param fsArray DoubleScalar[][]; the provided array
     * @return DoubleScalar[][]; the provided array
     * @throws ValueException when the provided array has zero entries
     * @param <U> Unit; the unit
     */
    protected static <U extends Unit<U>> DoubleScalar<U>[][] checkNonEmpty(final DoubleScalar<U>[][] fsArray)
            throws ValueException
    {
        if (0 == fsArray.length || 0 == fsArray[0].length)
        {
            throw new ValueException(
                    "Cannot create a DoubleValue or MutableDoubleValue from an empty array of DoubleScalar");
        }
        return fsArray;
    }

    /**
     * Solve x for A*x = b. According to Colt: x; a new independent matrix; solution if A is square, least squares
     * solution if A.rows() > A.columns(), underdetermined system solution if A.rows() < A.columns().
     * @param A matrix A in A*x = b
     * @param b vector b in A*x = b
     * @return vector x in A*x = b
     * @throws ValueException when Matrix A is neither Sparse nor Dense.
     */
    public static DoubleVector<SIUnit> solve(final DoubleMatrix<?> A, final DoubleVector<?> b) throws ValueException
    {
        // TODO: is this correct? Should lookup matrix algebra to find out unit for x when solving A*x = b ?
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(b.getUnit().getSICoefficients(),
                        A.getUnit().getSICoefficients()).toString());

        // TODO: should the algorithm throw an exception when rows/columns do not match when solving A*x = b ?
        DoubleMatrix2D A2D = A.matrixSI;
        if (A instanceof SparseData)
        {
            SparseDoubleMatrix1D b1D = new SparseDoubleMatrix1D(b.getValuesSI());
            DoubleMatrix1D x1D = new SparseDoubleAlgebra().solve(A2D, b1D);
            DoubleVector.Abs.Sparse<SIUnit> x = new DoubleVector.Abs.Sparse<SIUnit>(x1D.toArray(), targetUnit);
            return x;
        }
        if (A instanceof DenseData)
        {
            DenseDoubleMatrix1D b1D = new DenseDoubleMatrix1D(b.getValuesSI());
            DoubleMatrix1D x1D = new DenseDoubleAlgebra().solve(A2D, b1D);
            DoubleVector.Abs.Dense<SIUnit> x = new DoubleVector.Abs.Dense<SIUnit>(x1D.toArray(), targetUnit);
            return x;
        }
        throw new ValueException("DoubleMatrix.det -- matrix implements neither Sparse nor Dense");
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.matrixSI.hashCode();
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof DoubleMatrix))
        {
            return false;
        }
        DoubleMatrix<?> other = (DoubleMatrix<?>) obj;
        // unequal if one is absolute and the other is relative
        if (this.isAbsolute() != other.isAbsolute() || this.isRelative() != other.isRelative())
        {
            return false;
        }
        // unequal if the SI unit type differs (km/h and m/s could have the same content, so that is allowed)
        if (!this.getUnit().getStandardUnit().equals(other.getUnit().getStandardUnit()))
        {
            return false;
        }
        // Colt's equals also tests the size of the vector
        if (!this.matrixSI.equals(other.matrixSI))
        {
            return false;
        }
        return true;
    }

}

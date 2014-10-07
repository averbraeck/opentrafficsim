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
 * Immutable DoubleMatrix.
 * <p>
 * This file was generated by the OpenTrafficSim value classes generator, 07 okt, 2014
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 07 okt, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit; the unit of this DoubleMatrix
 */
public abstract class DoubleMatrix <U extends Unit<U>> extends AbstractValue<U> implements Serializable,
    ReadOnlyDoubleMatrixFunctions<U>
{
    /**  */
    private static final long serialVersionUID = 20141007L;

    /** 
     * The internal storage for the matrix; internally the values are stored in standard SI unit; storage can be dense
     * or sparse.
     */
    private DoubleMatrix2D matrixSI;

    /**
     * Construct a new Immutable DoubleMatrix.
     * @param unit U; the unit of the new DoubleMatrix
     */
    protected  DoubleMatrix(final U unit)
    {
        super(unit);
        // System.out.println("Created DoubleMatrix");
    }

    /**
     * @param <U> Unit
     */
    public abstract static class Abs<U extends Unit<U>> extends DoubleMatrix<U> implements Absolute
    {
        /**  */
        private static final long serialVersionUID = 20141007L;

        /**
         * Construct a new Absolute Immutable DoubleMatrix.
         * @param unit U; the unit of the new Absolute Immutable DoubleMatrix
         */
        protected Abs(final U unit)
        {
            super(unit);
            // System.out.println("Created Abs");
        }

        /**
         * @param <U> Unit
         */
        public static class Dense<U extends Unit<U>> extends Abs<U> implements DenseData
        {
            /**  */
            private static final long serialVersionUID = 20141007L;

            /**
             * Construct a new Absolute Dense Immutable DoubleMatrix.
             * @param values double[][]; the values of the entries in the new Absolute Dense Immutable DoubleMatrix
             * @param unit U; the unit of the new Absolute Dense Immutable DoubleMatrix
             * @throws ValueException when values is not rectangular
             */
            public Dense(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Construct a new Absolute Dense Immutable DoubleMatrix.
             * @param values DoubleScalar.Abs&lt;U&gt;[][]; the values of the entries in the new Absolute Dense
             *            Immutable DoubleMatrix
             * @throws ValueException when values has zero entries, or is not rectangular
             */
            public Dense(final DoubleScalar.Abs<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values DoubleMatrix2D; the values of the entries in the new Absolute Dense Immutable DoubleMatrix
             * @param unit U; the unit of the new Absolute Dense Immutable DoubleMatrix
             */
            protected Dense(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleMatrix.Abs.Dense<U> mutable()
            {
                return new MutableDoubleMatrix.Abs.Dense<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new DenseDoubleMatrix2D(rows, columns);
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleMatrix.Abs.Dense<U> copy()
            {
                return this; // That was easy...
            }

        }

        /**
         * @param <U> Unit
         */
        public static class Sparse<U extends Unit<U>> extends Abs<U> implements SparseData
        {
            /**  */
            private static final long serialVersionUID = 20141007L;

            /**
             * Construct a new Absolute Sparse Immutable DoubleMatrix.
             * @param values double[][]; the values of the entries in the new Absolute Sparse Immutable DoubleMatrix
             * @param unit U; the unit of the new Absolute Sparse Immutable DoubleMatrix
             * @throws ValueException when values is not rectangular
             */
            public Sparse(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Construct a new Absolute Sparse Immutable DoubleMatrix.
             * @param values DoubleScalar.Abs&lt;U&gt;[][]; the values of the entries in the new Absolute Sparse
             *            Immutable DoubleMatrix
             * @throws ValueException when values has zero entries, or is not rectangular
             */
            public Sparse(final DoubleScalar.Abs<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values DoubleMatrix2D; the values of the entries in the new Absolute Sparse Immutable DoubleMatrix
             * @param unit U; the unit of the new Absolute Sparse Immutable DoubleMatrix
             */
            protected Sparse(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleMatrix.Abs.Sparse<U> mutable()
            {
                return new MutableDoubleMatrix.Abs.Sparse<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new SparseDoubleMatrix2D(rows, columns);
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleMatrix.Abs.Sparse<U> copy()
            {
                return this; // That was easy...
            }

        }

        /** {@inheritDoc} */
        @Override
        public final DoubleScalar.Abs<U> get(final int row, final int column) throws ValueException
        {
            return new DoubleScalar.Abs<U>(getInUnit(row, column, getUnit()), getUnit());
        }

    }

    /**
     * @param <U> Unit
     */
    public abstract static class Rel<U extends Unit<U>> extends DoubleMatrix<U> implements Relative
    {
        /**  */
        private static final long serialVersionUID = 20141007L;

        /**
         * Construct a new Relative Immutable DoubleMatrix.
         * @param unit U; the unit of the new Relative Immutable DoubleMatrix
         */
        protected Rel(final U unit)
        {
            super(unit);
            // System.out.println("Created Rel");
        }

        /**
         * @param <U> Unit
         */
        public static class Dense<U extends Unit<U>> extends Rel<U> implements DenseData
        {
            /**  */
            private static final long serialVersionUID = 20141007L;

            /**
             * Construct a new Relative Dense Immutable DoubleMatrix.
             * @param values double[][]; the values of the entries in the new Relative Dense Immutable DoubleMatrix
             * @param unit U; the unit of the new Relative Dense Immutable DoubleMatrix
             * @throws ValueException when values is not rectangular
             */
            public Dense(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Construct a new Relative Dense Immutable DoubleMatrix.
             * @param values DoubleScalar.Rel&lt;U&gt;[][]; the values of the entries in the new Relative Dense
             *            Immutable DoubleMatrix
             * @throws ValueException when values has zero entries, or is not rectangular
             */
            public Dense(final DoubleScalar.Rel<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values DoubleMatrix2D; the values of the entries in the new Relative Dense Immutable DoubleMatrix
             * @param unit U; the unit of the new Relative Dense Immutable DoubleMatrix
             */
            protected Dense(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleMatrix.Rel.Dense<U> mutable()
            {
                return new MutableDoubleMatrix.Rel.Dense<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new DenseDoubleMatrix2D(rows, columns);
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleMatrix.Rel.Dense<U> copy()
            {
                return this; // That was easy...
            }

        }

        /**
         * @param <U> Unit
         */
        public static class Sparse<U extends Unit<U>> extends Rel<U> implements SparseData
        {
            /**  */
            private static final long serialVersionUID = 20141007L;

            /**
             * Construct a new Relative Sparse Immutable DoubleMatrix.
             * @param values double[][]; the values of the entries in the new Relative Sparse Immutable DoubleMatrix
             * @param unit U; the unit of the new Relative Sparse Immutable DoubleMatrix
             * @throws ValueException when values is not rectangular
             */
            public Sparse(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Construct a new Relative Sparse Immutable DoubleMatrix.
             * @param values DoubleScalar.Rel&lt;U&gt;[][]; the values of the entries in the new Relative Sparse
             *            Immutable DoubleMatrix
             * @throws ValueException when values has zero entries, or is not rectangular
             */
            public Sparse(final DoubleScalar.Rel<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values DoubleMatrix2D; the values of the entries in the new Relative Sparse Immutable DoubleMatrix
             * @param unit U; the unit of the new Relative Sparse Immutable DoubleMatrix
             */
            protected Sparse(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleMatrix.Rel.Sparse<U> mutable()
            {
                return new MutableDoubleMatrix.Rel.Sparse<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new SparseDoubleMatrix2D(rows, columns);
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleMatrix.Rel.Sparse<U> copy()
            {
                return this; // That was easy...
            }

        }

        /** {@inheritDoc} */
        @Override
        public final DoubleScalar.Rel<U> get(final int row, final int column) throws ValueException
        {
            return new DoubleScalar.Rel<U>(getInUnit(row, column, getUnit()), getUnit());
        }

    }

    /**
     * Retrieve the internal data.
     * @return DoubleMatrix2D; the data in the internal format
     */
    protected final DoubleMatrix2D getMatrixSI()
    {
        return this.matrixSI;
    }

    /**
     * Make a deep copy of the data (used ONLY in the MutableDoubleMatrix sub class).
     */
    protected final void deepCopyData()
    {
        this.matrixSI = getMatrixSI().copy(); // makes a deep copy, using multithreading
    }

    /**
     * Create a mutable version of this DoubleMatrix. <br>
     * The mutable version is created with a shallow copy of the data and the internal copyOnWrite flag set. The first
     * operation in the mutable version that modifies the data shall trigger a deep copy of the data.
     * @return MutableDoubleMatrix&lt;U&gt;; mutable version of this DoubleMatrix
     */
    public abstract MutableDoubleMatrix<U> mutable();

    /**
     * Import the values and convert them into the SI standard unit.
     * @param values double[][]; an array of values
     * @throws ValueException when values is not rectangular
     */
    protected final void initialize(final double[][] values) throws ValueException
    {
        ensureRectangular(values);
        this.matrixSI = createMatrix2D(values.length, 0 == values.length ? 0 : values[0].length);
        if (getUnit().equals(getUnit().getStandardUnit()))
        {
            this.matrixSI.assign(values);
        }
        else
        {
            for (int row = values.length; --row >= 0;)
            {
                for (int column = values[row].length; --column >= 0;)
                {
                    safeSet(row, column, expressAsSIUnit(values[row][column]));
                }
            }
        }
    }

    /**
     * Import the values from an existing DoubleMatrix2D. This makes a shallow copy.
     * @param values DoubleMatrix2D; the values
     */
    protected final void initialize(final DoubleMatrix2D values)
    {
        this.matrixSI = values;
    }

    /**
     * Construct the matrix and store the values in the standard SI unit.
     * @param values DoubleScalar&lt;U&gt;[][]; a 2D array of values
     * @throws ValueException when values has zero entries, or is not rectangular
     */
    protected final void initialize(final DoubleScalar<U>[][] values) throws ValueException
    {
        ensureRectangularAndNonEmpty(values);
        this.matrixSI = createMatrix2D(values.length, values[0].length);
        for (int row = values.length; --row >= 0;)
        {
            for (int column = values[row].length; --column >= 0;)
            {
                safeSet(row, column, values[row][column].getValueSI());
            }
        }
    }

    /**
     * Create storage for the data. <br/>
     * This method must be implemented by each leaf class.
     * @param rows int; the number of rows in the matrix
     * @param columns int; the number of columns in the matrix
     * @return DoubleMatrix2D; an instance of the right type of DoubleMatrix2D (absolute/relative, dense/sparse, etc.)
     */
    protected abstract DoubleMatrix2D createMatrix2D(final int rows, final int columns);

    /**
     * Create a double[][] array filled with the values in the standard SI unit.
     * @return double[][]; array of values in the standard SI unit
     */
    public final double[][] getValuesSI()
    {
        return this.matrixSI.toArray(); // this makes a deep copy
    }

    /**
     * Create a double[][] array filled with the values in the original unit.
     * @return double[][]; the values in the original unit
     */
    public final double[][] getValuesInUnit()
    {
        return getValuesInUnit(getUnit());
    }

    /**
     * Create a double[][] array filled with the values converted into a specified unit.
     * @param targetUnit U; the unit into which the values are converted for use
     * @return double[][]; the values converted into the specified unit
     */
    public final double[][] getValuesInUnit(final U targetUnit)
    {
        double[][] values = this.matrixSI.toArray();
        for (int row = rows(); --row >= 0;)
        {
            for (int column = columns(); --column >= 0;)
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

    /** {@inheritDoc} */
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
                return new SparseDoubleAlgebra().det(getMatrixSI());
            }
            if (this instanceof DenseData)
            {
                return new DenseDoubleAlgebra().det(getMatrixSI());
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
        return toString(getUnit());
    }

    /**
     * Print this DoubleMatrix with the values expressed in the specified unit.
     * @param displayUnit U; the unit into which the values are converted for display
     * @return String; printable string with the matrix contents
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
        for (int row = 0; row < rows(); row++)
        {
        buf.append("\r\n\t");
        for (int column = 0; column < columns(); column++)
        {
                double d = ValueUtil.expressAsUnit(safeGet(row, column), displayUnit);
                buf.append(" " + Format.format(d));
            }
        }
        return buf.toString();
    }

    /**
     * Centralized size equality check.
     * @param other DoubleMatrix&lt;?&gt;; other DoubleMatrix
     * @throws ValueException when matrices have unequal size
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
     * @throws ValueException when matrices have unequal size
     */
    protected final void checkSize(final double[][] other) throws ValueException
    {
        final int otherColumns = 0 == other.length ? 0 : other[0].length;
        if (rows() != other.length || columns() != otherColumns)
        {
        throw new ValueException("The matrix and the array have different sizes: " + rows() + "x" + columns()
                    + " != " + other.length + "x" + otherColumns);
        }
        ensureRectangular(other);
    }

    /**
     * Check that a 2D array of double is rectangular; i.e. all rows have the same length.
     * @param values double[][]; the 2D array to check
     * @throws ValueException when not all rows have the same length
     */
    private static void ensureRectangular(final double[][] values) throws ValueException
    {
        for (int row = values.length; --row >= 1;)
        {
            if (values[0].length != values[row].length)
            {
                throw new ValueException("Lengths of rows are not all the same");
            }
        }
    }

    /**
     * Check that a 2D array of DoubleScalar&lt;?&gt; is rectangular; i.e. all rows have the same length and is non
     * empty.
     * @param values DoubleScalar&lt;?&gt;[][]; the 2D array to check
     * @throws ValueException when values is not rectangular, or contains no data
     */
    private static void ensureRectangularAndNonEmpty(final DoubleScalar<?>[][] values) throws ValueException
    {
        if (0 == values.length || 0 == values[0].length)
        {
            throw new ValueException("Cannot determine unit for DoubleMatrix from an empty array of DoubleScalar");
        }
        for (int row = values.length; --row >= 1;)
        {
            if (values[0].length != values[row].length)
            {
                throw new ValueException("Lengths of rows are not all the same");
            }
        }
    }

    /**
     * Check that provided row and column indices are valid.
     * @param row int; the row value to check
     * @param column int; the column value to check
     * @throws ValueException when row or column is invalid
     */
    protected final void checkIndex(final int row, final int column) throws ValueException
    {
        if (row < 0 || row >= rows() || column < 0 || column >= columns())
        {
            throw new ValueException("index out of range (valid range is 0.." + (rows() - 1) + ", 0.."
                    + (columns() - 1) + ", got " + row + ", " + column + ")");
        }
    }

    /**
     * Retrieve a value in matrixSI without checking validity of the indices.
     * @param row int; the row where the value must be retrieved
     * @param column int; the column where the value must be retrieved
     * @return double; the value stored at the indicated row and column
     */
    protected final double safeGet(final int row, final int column)
    {
        return this.matrixSI.getQuick(row, column);
    }

    /**
     * Modify a value in matrixSI without checking validity of the indices.
     * @param row int; the row where the value must be stored
     * @param column int; the column where the value must be stored
     * @param valueSI double; the new value for the entry in matrixSI
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
     * Check that a provided array can be used to create some descendant of a DoubleMatrix.
     * @param dsArray DoubleScalar&lt;U&gt;[][]; the provided array
     * @param <U> Unit; the unit of the DoubleScalar array
     * @return DoubleScalar&lt;U&gt;[][]; the provided array
     * @throws ValueException when the array has zero entries
     */
    protected static <U extends Unit<U>> DoubleScalar<U>[][] checkNonEmpty(final DoubleScalar<U>[][] dsArray)
             throws ValueException
    {
        if (0 == dsArray.length || 0 == dsArray[0].length)
        {
            throw new ValueException(
                    "Cannot create a DoubleMatrix or MutableDoubleMatrix from an empty array of DoubleScalar");
        }
        return dsArray;
    }

    /**
     * Solve x for A*x = b. According to Colt: x; a new independent matrix; solution if A is square, least squares
     * solution if A.rows() &gt; A.columns(), underdetermined system solution if A.rows() &lt; A.columns().
     * @param A DoubleMatrix&lt;?&gt;; matrix A in A*x = b
     * @param b DoubleVector&lt;?&gt;; vector b in A*x = b
     * @return DoubleVector&lt;SIUnit&gt;; vector x in A*x = b
     * @throws ValueException when matrix A is neither Sparse nor Dense
     */
    public static DoubleVector<SIUnit> solve(final DoubleMatrix<?> A, final DoubleVector<?> b) throws ValueException
    {
        // TODO: is this correct? Should lookup matrix algebra to find out unit for x when solving A*x = b ?
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(b.getUnit().getSICoefficients(),
                        A.getUnit().getSICoefficients()).toString());
        
        // TODO: should the algorithm throw an exception when rows/columns do not match when solving A*x = b ?
        DoubleMatrix2D A2D = A.getMatrixSI();
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
        // unequal if not both absolute or both relative
        if (this.isAbsolute() != other.isAbsolute() || this.isRelative() != other.isRelative())
        {
            return false;
        }
        // unequal if the standard SI units differ
        if (!this.getUnit().getStandardUnit().equals(other.getUnit().getStandardUnit()))
        {
            return false;
        }
        // Colt's equals also tests the size of the matrix
        if (!getMatrixSI().equals(other.getMatrixSI()))
        {
            return false;
        }
        return true;
    }

}

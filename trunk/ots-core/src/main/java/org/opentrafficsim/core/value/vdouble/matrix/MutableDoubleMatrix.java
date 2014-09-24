package org.opentrafficsim.core.value.vdouble.matrix;

import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.DenseData;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.SparseData;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.DoubleMathFunctions;
import org.opentrafficsim.core.value.vdouble.DoubleMathFunctionsImpl;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 9, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit
 */
public abstract class MutableDoubleMatrix<U extends Unit<U>> extends DoubleMatrix<U> implements WriteDoubleMatrixFunctions<U>,
        DoubleMathFunctions
{

    /** */
    private static final long serialVersionUID = 20140909L;

    /**
     * @param unit Unit
     */
    protected MutableDoubleMatrix(final U unit)
    {
        super(unit);
    }

    /** If set, any modification of the data must be preceded by replacing the data with a local copy. */
    private boolean copyOnWrite = false;

    /**
     * @return copyOnWrite
     */
    public final boolean isCopyOnWrite()
    {
        return this.copyOnWrite;
    }

    /**
     * @param copyOnWrite set copyOnWrite
     */
    public final void setCopyOnWrite(final boolean copyOnWrite)
    {
        this.copyOnWrite = copyOnWrite;
    }

    /** {@inheritDoc} */
    @Override
    public final void normalize() throws ValueException
    {
        double sum = zSum();
        if (0 == sum)
        {
            throw new ValueException("zSum is 0; cannot normalize");
        }
        checkCopyOnWrite();
        for (int row = 0; row < rows(); row++)
        {
            for (int column = 0; column < columns(); column++)
            {
                safeSet(row, column, safeGet(row, column) / sum);
            }
        }
    }

    /**
     * @param <U> Unit
     */
    public abstract static class Abs<U extends Unit<U>> extends MutableDoubleMatrix<U> implements Absolute
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a new Absolute MutableDoubleMatrix.
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
             * @param values DoubleMatrix2D; the initial values for the new MutableDoubleMatrix
             * @param unit Unit; the unit for the new MutableDoubleMatrix
             */
            protected Dense(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                this.setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /**
             * Create a new Absolute Dense Mutable DoubleMatrix.
             * @param values double[][]; the initial values for the new MutableDoubleMatrix
             * @param unit Unit; the unit of the values for the new MutableDoubleMatrix
             * @throws ValueException when values is not rectangular, or has zero entries
             */
            public Dense(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Create a new Absolute Dense Mutable DoubleMatrix.
             * @param values DoubleScalar.Abs][][]; the initial values for the new MutableDoubleMatrix
             * @throws ValueException when values it not rectangular
             */
            public Dense(final DoubleScalar.Abs<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleMatrix.Abs.Dense<U> immutable()
            {
                this.setCopyOnWrite(true);
                return new DoubleMatrix.Abs.Dense<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleMatrix.Abs.Dense<U> mutable()
            {
                this.setCopyOnWrite(true);
                return new MutableDoubleMatrix.Abs.Dense<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new DenseDoubleMatrix2D(rows, columns);
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
             * @param values DoubleMatrix2D; the initial values for the entries in the new MutableDoubleMatrix
             * @param unit Unit; the unit of the new MutableDoubleMatrix
             */
            protected Sparse(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                this.setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /**
             * Create a new Sparse Relative Mutable DoubleMatrix.
             * @param values double[][]; the initial values for the entries in the new MutableDoubleMatrix
             * @param unit Unit; the unit of the values
             * @throws ValueException when values it not rectangular
             */
            public Sparse(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Create a new Absolute Sparse MutableDoubleMatrix.
             * @param values DoubleScalar.Abs[][]; the initial values for the entries in the new MutableDoubleMatrix
             * @throws ValueException when values is not rectangular, or has zero entries
             */
            public Sparse(final DoubleScalar.Abs<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleMatrix.Abs.Sparse<U> immutable()
            {
                this.setCopyOnWrite(true);
                return new DoubleMatrix.Abs.Sparse<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleMatrix.Abs.Sparse<U> mutable()
            {
                this.setCopyOnWrite(true);
                return new MutableDoubleMatrix.Abs.Sparse<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new DenseDoubleMatrix2D(rows, columns);
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
    public abstract static class Rel<U extends Unit<U>> extends MutableDoubleMatrix<U> implements Relative
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a new Relative MutableDoubleMatrix.
         * @param unit Unit; the unit of the new DoubleMatrix
         */
        protected Rel(final U unit)
        {
            super(unit);
        }

        /**
         * @param <U> Unit
         */
        public static class Dense<U extends Unit<U>> extends Rel<U> implements DenseData
        {
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * For package internal use only.
             * @param values DoubleMatrix2D; initial values for the new MutableDoubleMatrix
             * @param unit Unit; the unit of the new DoubleMatrix
             */
            protected Dense(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                this.setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /**
             * Create a new Relative Dense Mutable DoubleMatrix.
             * @param values double[][]; initial values for the new MutableDoubleMatrix
             * @param unit Unit; the unit of the values
             * @throws ValueException when values it not rectangular
             */
            public Dense(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Create a new Relative Dense Mutable DoubleMatrix.
             * @param values DoubleScalar.Rel[][]; initial values for the new MutableDoubleMatrix
             * @throws ValueException when values it not rectangular or has zero entries
             */
            public Dense(final DoubleScalar.Rel<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleMatrix.Rel.Dense<U> immutable()
            {
                this.setCopyOnWrite(true);
                return new DoubleMatrix.Rel.Dense<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleMatrix.Rel.Dense<U> mutable()
            {
                this.setCopyOnWrite(true);
                return new MutableDoubleMatrix.Rel.Dense<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new SparseDoubleMatrix2D(rows, columns);
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
             * @param values DoubleMatrix2D; the initial values for the new DoubleMatrix
             * @param unit Unit; the unit for the new DoubleMatrix
             */
            protected Sparse(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                this.setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /**
             * Create a new Relative Sparse Mutable DoubleMatrix.
             * @param values double[][]; initial values for the entries in the new DoubleMatrix
             * @param unit Unit; unit of the values
             * @throws ValueException when values is not rectangular
             */
            public Sparse(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Create a new Relative Sparse Mutable DoubleMatrix.
             * @param values DoubleScalar.Rel[][]; initial values for the entries in the new DoubleMatrix
             * @throws ValueException when values is not rectangular or has zero entries
             */
            public Sparse(final DoubleScalar.Rel<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleMatrix.Rel.Sparse<U> immutable()
            {
                this.setCopyOnWrite(true);
                return new DoubleMatrix.Rel.Sparse<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleMatrix.Rel.Sparse<U> mutable()
            {
                this.setCopyOnWrite(true);
                return new MutableDoubleMatrix.Rel.Sparse<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new SparseDoubleMatrix2D(rows, columns);
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
     * Make (immutable) DoubleMatrix equivalent for any type of MutableDoubleMatrix.
     * @return DoubleMatrix
     */
    public abstract DoubleMatrix<U> immutable();

    /** {@inheritDoc} */
    @Override
    public final MutableDoubleMatrix<U> copy()
    {
        return immutable().mutable();
        // FIXME: This may cause both the original and the copy to be deep copied later
        // Maybe it is better to make a deep copy now?
    }

    /**
     * Check the copyOnWrite flag and, if it is set make a deep copy of the data and clear the flag.
     */
    protected final void checkCopyOnWrite()
    {
        if (this.isCopyOnWrite())
        {
            // System.out.println("copyOnWrite is set: Copying data");
            deepCopyData();
            this.setCopyOnWrite(false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void setSI(final int row, final int column, final double valueSI) throws ValueException
    {
        checkIndex(row, column);
        checkCopyOnWrite();
        safeSet(row, column, valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void set(final int row, final int column, final DoubleScalar<U> value) throws ValueException
    {
        setSI(row, column, value.getValueSI());
    }

    /** {@inheritDoc} */
    @Override
    public final void setInUnit(final int row, final int column, final double value, final U valueUnit) throws ValueException
    {
        // TODO: creating a DoubleScalarAbs along the way may not be the most efficient way to do this...
        setSI(row, column, new DoubleScalar.Abs<U>(value, valueUnit).getValueSI());
    }

    /**
     * Execute a function on a cell by cell basis.
     * @param f cern.colt.function.tdouble.DoubleFunction; the function to apply
     */
    public final void assign(final cern.colt.function.tdouble.DoubleFunction f)
    {
        checkCopyOnWrite();
        getMatrixSI().assign(f);
    }

    /** {@inheritDoc} */
    @Override
    public final void abs()
    {
        assign(DoubleFunctions.abs);
    }

    /** {@inheritDoc} */
    @Override
    public final void acos()
    {
        assign(DoubleFunctions.acos);
    }

    /** {@inheritDoc} */
    @Override
    public final void asin()
    {
        assign(DoubleFunctions.asin);
    }

    /** {@inheritDoc} */
    @Override
    public final void atan()
    {
        assign(DoubleFunctions.atan);
    }

    /** {@inheritDoc} */
    @Override
    public final void cbrt()
    {
        assign(DoubleMathFunctionsImpl.cbrt);
    }

    /** {@inheritDoc} */
    @Override
    public final void ceil()
    {
        assign(DoubleFunctions.ceil);
    }

    /** {@inheritDoc} */
    @Override
    public final void cos()
    {
        assign(DoubleFunctions.cos);
    }

    /** {@inheritDoc} */
    @Override
    public final void cosh()
    {
        assign(DoubleMathFunctionsImpl.cosh);
    }

    /** {@inheritDoc} */
    @Override
    public final void exp()
    {
        assign(DoubleFunctions.exp);
    }

    /** {@inheritDoc} */
    @Override
    public final void expm1()
    {
        assign(DoubleMathFunctionsImpl.expm1);
    }

    /** {@inheritDoc} */
    @Override
    public final void floor()
    {
        assign(DoubleFunctions.floor);
    }

    /** {@inheritDoc} */
    @Override
    public final void log()
    {
        assign(DoubleFunctions.log);
    }

    /** {@inheritDoc} */
    @Override
    public final void log10()
    {
        assign(DoubleMathFunctionsImpl.log10);
    }

    /** {@inheritDoc} */
    @Override
    public final void log1p()
    {
        assign(DoubleMathFunctionsImpl.log1p);
    }

    /** {@inheritDoc} */
    @Override
    public final void pow(final double x)
    {
        assign(DoubleFunctions.pow(x));
    }

    /** {@inheritDoc} */
    @Override
    public final void rint()
    {
        assign(DoubleFunctions.rint);
    }

    /** {@inheritDoc} */
    @Override
    public final void round()
    {
        assign(DoubleMathFunctionsImpl.round);
    }

    /** {@inheritDoc} */
    @Override
    public final void signum()
    {
        assign(DoubleMathFunctionsImpl.signum);
    }

    /** {@inheritDoc} */
    @Override
    public final void sin()
    {
        assign(DoubleFunctions.sin);
    }

    /** {@inheritDoc} */
    @Override
    public final void sinh()
    {
        assign(DoubleMathFunctionsImpl.sinh);
    }

    /** {@inheritDoc} */
    @Override
    public final void sqrt()
    {
        assign(DoubleFunctions.sqrt);
    }

    /** {@inheritDoc} */
    @Override
    public final void tan()
    {
        assign(DoubleFunctions.tan);
    }

    /** {@inheritDoc} */
    @Override
    public final void tanh()
    {
        assign(DoubleMathFunctionsImpl.tanh);
    }

    /** {@inheritDoc} */
    @Override
    public final void toDegrees()
    {
        assign(DoubleMathFunctionsImpl.toDegrees);
    }

    /** {@inheritDoc} */
    @Override
    public final void toRadians()
    {
        assign(DoubleMathFunctionsImpl.toRadians);
    }

    /** {@inheritDoc} */
    @Override
    public final void inv()
    {
        assign(DoubleFunctions.inv);
    }

    /** {@inheritDoc} */
    @Override
    public final void multiply(final double constant)
    {
        assign(DoubleFunctions.mult(constant));
    }

    /** {@inheritDoc} */
    @Override
    public final void divide(final double constant)
    {
        assign(DoubleFunctions.div(constant));
    }

    /**
     * Increment the values in this MutableDoubleMatrix by the corresponding values in an DoubleMatrix.
     * @param increment AbstractDoubleMatrix; contains the amounts by which to increment the corresponding entries in this
     *            MutableDoubleMatrix
     * @return this
     * @throws ValueException when the matrices do not have the same size
     */
    private MutableDoubleMatrix<U> incrementValueByValue(final DoubleMatrix<U> increment) throws ValueException
    {
        checkSizeAndCopyOnWrite(increment);
        for (int row = this.rows(); --row >= 0;)
        {
            for (int column = this.columns(); --column >= 0;)
            {
                safeSet(row, column, safeGet(row, column) + increment.safeGet(row, column));
            }
        }
        return this;
    }

    /**
     * Increment the entries in this MutableDoubleMatrix by the corresponding values in a Relative DoubleMatrix.
     * @param rel DoubleMatrix.Rel; the Relative DoubleMatrix
     * @return this
     * @throws ValueException when the matrices do not have the same size
     */
    public final MutableDoubleMatrix<U> incrementBy(final DoubleMatrix.Rel<U> rel) throws ValueException
    {
        return incrementValueByValue(rel);
    }

    /**
     * Decrement the values in this MutableDoubleMatrix by the corresponding values in an DoubleMatrix.
     * @param decrement AbstractDoubleMatrix; contains the amounts by which to decrement the corresponding entries in this
     *            MutableDoubleMatrix
     * @return this
     * @throws ValueException when the matrices do not have the same size
     */
    private MutableDoubleMatrix<U> decrementValueByValue(final DoubleMatrix<U> decrement) throws ValueException
    {
        checkSizeAndCopyOnWrite(decrement);
        for (int row = rows(); --row >= 0;)
        {
            for (int column = columns(); --column >= 0;)
            {
                safeSet(row, column, safeGet(row, column) - decrement.safeGet(row, column));
            }
        }
        return this;
    }

    /**
     * Decrement the entries in this MutableDoubleMatrix by the corresponding values in a Relative DoubleMatrix.
     * @param rel DoubleMatrix.Rel; the Relative DoubleMatrix
     * @return this
     * @throws ValueException when the matrices do not have the same size
     */
    public final MutableDoubleMatrix<U> decrementBy(final DoubleMatrix.Rel<U> rel) throws ValueException
    {
        return decrementValueByValue(rel);
    }

    /**
     * Decrement the entries in this MutableDoubleMatrix by the corresponding values in a Absolute DoubleMatrix.
     * @param abs DoubleMatrix.Abs; the Absolute DoubleMatrix
     * @return this
     * @throws ValueException when the matrices do not have the same size
     */
    public final MutableDoubleMatrix<U> decrementBy(final DoubleMatrix.Abs<U> abs) throws ValueException
    {
        return decrementValueByValue(abs);
    }

    /**
     * Scale the values in this MutableDoubleMatrix by the corresponding values in an DoubleMatrix.
     * @param factor AbstractDoubleMatrix; contains the values by which to scale the corresponding entries in this
     *            MutableDoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    public final void scaleValueByValue(final DoubleMatrix<?> factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int row = this.rows(); --row >= 0;)
        {
            for (int column = this.columns(); --column >= 0;)
            {
                safeSet(row, column, safeGet(row, column) * factor.safeGet(row, column));
            }
        }
    }

    /**
     * Scale the values in this MutableDoubleMatrix by the corresponding values in a double array.
     * @param factor double[][]; contains the values by which to scale the corresponding entries in this MutableDoubleMatrix
     * @return this
     * @throws ValueException when the matrix and the 2D array do not have the same size
     */
    public final MutableDoubleMatrix<U> scaleValueByValue(final double[][] factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int row = this.rows(); --row >= 0;)
        {
            for (int column = this.columns(); --column >= 0;)
            {
                safeSet(row, column, safeGet(row, column) * factor[row][column]);
            }
        }
        return this;
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other AbstractDoubleMatrix; partner for the size check
     * @throws ValueException when the matrices do not have the same size
     */
    private void checkSizeAndCopyOnWrite(final DoubleMatrix<?> other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other double[][]; partner for the size check
     * @throws ValueException when the matrices do not have the same size
     */
    private void checkSizeAndCopyOnWrite(final double[][] other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Add two DoubleMatrices entry by entry.
     * @param left Absolute Dense DoubleMatrix
     * @param right Relative DoubleMatrix
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> plus(final DoubleMatrix.Abs.Dense<U> left,
            final DoubleMatrix.Rel<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleMatrices entry by entry.
     * @param left Absolute Sparse DoubleMatrix
     * @param right Relative Sparse DoubleMatrix
     * @return new Absolute Sparse Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> plus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleMatrices entry by entry.
     * @param left Absolute Sparse DoubleMatrix
     * @param right Relative Dense DoubleMatrix
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> plus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Add two DoubleMatrices entry by entry.
     * @param left Relative Dense DoubleMatrix
     * @param right Relative DoubleMatrix
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> plus(final DoubleMatrix.Rel.Dense<U> left,
            final DoubleMatrix.Rel<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleMatrices entry by entry.
     * @param left Relative Sparse DoubleMatrix
     * @param right Relative Sparse DoubleMatrix
     * @return new Relative Sparse Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> plus(final DoubleMatrix.Rel.Sparse<U> left,
            final DoubleMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleMatrices entry by entry.
     * @param left Relative Sparse DoubleMatrix
     * @param right Relative Dense DoubleMatrix
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> plus(final DoubleMatrix.Rel.Sparse<U> left,
            final DoubleMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry.
     * @param left Absolute Dense DoubleMatrix
     * @param right Absolute DoubleMatrix
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> minus(final DoubleMatrix.Abs.Dense<U> left,
            final DoubleMatrix.Abs<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) new MutableDoubleMatrix.Rel.Dense<U>(left.deepCopyOfData(), left.getUnit())
                .decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry.
     * @param left Absolute Sparse DoubleMatrix
     * @param right Absolute Sparse DoubleMatrix
     * @return new Relative Sparse Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> minus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Abs.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Sparse<U>) new MutableDoubleMatrix.Rel.Sparse<U>(left.deepCopyOfData(), left.getUnit())
                .decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry.
     * @param left Absolute Sparse DoubleMatrix
     * @param right Absolute Dense DoubleMatrix
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> minus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Abs.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) new MutableDoubleMatrix.Rel.Dense<U>(left.deepCopyOfData(), left.getUnit())
                .decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry.
     * @param left Absolute Dense DoubleMatrix
     * @param right Relative DoubleMatrix
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> minus(final DoubleMatrix.Abs.Dense<U> left,
            final DoubleMatrix.Rel<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry.
     * @param left Absolute Sparse DoubleMatrix
     * @param right Relative Sparse DoubleMatrix
     * @return new Absolute Sparse Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> minus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry.
     * @param left Absolute Sparse DoubleMatrix
     * @param right Relative Dense DoubleMatrix
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> minus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Sparse<U>) sparseToDense(left).decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry.
     * @param left Relative Dense DoubleMatrix
     * @param right Relative DoubleMatrix
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> minus(final DoubleMatrix.Rel.Dense<U> left,
            final DoubleMatrix.Rel<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry.
     * @param left Relative Sparse DoubleMatrix
     * @param right Relative Sparse DoubleMatrix
     * @return new Relative Sparse Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> minus(final DoubleMatrix.Rel.Sparse<U> left,
            final DoubleMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry.
     * @param left Relative Sparse DoubleMatrix
     * @param right Relative Dense DoubleMatrix
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> minus(final DoubleMatrix.Rel.Sparse<U> left,
            final DoubleMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) sparseToDense(left).decrementBy(right);
    }

    /**
     * Multiply two DoubleMatricess entry by entry.
     * @param left Absolute Dense DoubleMatrix
     * @param right Absolute Dense DoubleMatrix
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    public static MutableDoubleMatrix.Abs.Dense<SIUnit> times(final DoubleMatrix.Abs.Dense<?> left,
            final DoubleMatrix.Abs.Dense<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableDoubleMatrix.Abs.Dense<SIUnit> work =
                new MutableDoubleMatrix.Abs.Dense<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two DoubleMatrices entry by entry.
     * @param left Relative Dense DoubleMatrix
     * @param right Relative Dense DoubleMatrix
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    public static MutableDoubleMatrix.Rel.Dense<SIUnit> times(final DoubleMatrix.Rel.Dense<?> left,
            final DoubleMatrix.Rel.Dense<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableDoubleMatrix.Rel.Dense<SIUnit> work =
                new MutableDoubleMatrix.Rel.Dense<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two DoubleMatrices entry by entry.
     * @param left Absolute Sparse DoubleMatrix
     * @param right Absolute DoubleMatrix
     * @return new Absolute Sparse Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    public static MutableDoubleMatrix.Abs.Sparse<SIUnit> times(final DoubleMatrix.Abs.Sparse<?> left,
            final DoubleMatrix.Abs<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableDoubleMatrix.Abs.Sparse<SIUnit> work =
                new MutableDoubleMatrix.Abs.Sparse<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two DoubleMatrices entry by entry.
     * @param left Relative Sparse DoubleMatrix
     * @param right Relative DoubleMatrix
     * @return new Relative Sparse Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    public static MutableDoubleMatrix.Rel.Sparse<SIUnit> times(final DoubleMatrix.Rel.Sparse<?> left,
            final DoubleMatrix.Rel<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableDoubleMatrix.Rel.Sparse<SIUnit> work =
                new MutableDoubleMatrix.Rel.Sparse<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply the values in a DoubleMatrix by the corresponding values in a 2D double array.
     * @param left Absolute Dense DoubleMatrix
     * @param right double[][]; the array
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException when the matrix and the 2D array do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> times(final DoubleMatrix.Abs.Dense<U> left,
            final double[][] right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleMatrix by the corresponding values in a 2D double array.
     * @param left Relative Dense DoubleMatrix
     * @param right double[][]; the array
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> times(final DoubleMatrix.Rel.Dense<U> left,
            final double[][] right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleMatrix by the corresponding values in a 2D double array.
     * @param left Absolute Sparse DoubleMatrix
     * @param right double[][]; the values to multiply the entries by
     * @return new Sparse Absolute Mutable DoubleMatrix
     * @throws ValueException when the array does not have the same size as the DoubleMatrix
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> times(final DoubleMatrix.Abs.Sparse<U> left,
            final double[][] right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleMatrix by the corresponding values in a double array.
     * @param left Relative Sparse DoubleMatrix
     * @param right double[][]; the values to multiply the entries by
     * @return new Relative Sparse Mutable DoubleMatrix
     * @throws ValueException when the array does not have the same size as the DoubleMatrix
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> times(final DoubleMatrix.Rel.Sparse<U> left,
            final double[][] right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Make the Sparse equivalent of a DenseDoubleMatrix2D.
     * @param dense DenseDoubleMatrix2D
     * @return SparseDoubleMatrix2D
     */
    private static DoubleMatrix2D makeSparse(final DoubleMatrix2D dense)
    {
        DoubleMatrix2D result = new SparseDoubleMatrix2D(dense.rows(), dense.columns());
        result.assign(dense);
        return result;
    }

    /**
     * Create a Sparse version of this Dense DoubleMatrix.
     * @param in DoubleMatrix.Abs.Dense the Dense DoubleMatrix
     * @return MutableDoubleMatrix.Sparse.Abs
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> denseToSparse(final DoubleMatrix.Abs.Dense<U> in)
    {
        return new MutableDoubleMatrix.Abs.Sparse<U>(makeSparse(in.getMatrixSI()), in.getUnit());
    }

    /**
     * Create a Sparse version of this Dense DoubleMatrix.
     * @param in DoubleMatrix.Rel.Dense; the Dense DoubleMatrix
     * @return MutableDoubleMatrix.Abs.Sparse
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> denseToSparse(final DoubleMatrix.Rel.Dense<U> in)
    {
        return new MutableDoubleMatrix.Rel.Sparse<U>(makeSparse(in.getMatrixSI()), in.getUnit());
    }

    /**
     * Make the Dense equivalent of a SparseDoubleMatrix2D.
     * @param sparse SparseDoubleMatrix2D; the Sparse DoubleMatrix
     * @return DenseMatrix2D
     */
    private static DoubleMatrix2D makeDense(final DoubleMatrix2D sparse)
    {
        DoubleMatrix2D result = new SparseDoubleMatrix2D(sparse.rows(), sparse.columns());
        result.assign(sparse);
        return result;
    }

    /**
     * Create a Dense version of this Sparse DoubleMatrix.
     * @param in DoubleMatrix.Abs.Dense the Dense DoubleMatrix
     * @return MutableDoubleMatrix.Abs.Sparse
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> sparseToDense(final DoubleMatrix.Abs.Sparse<U> in)
    {
        return new MutableDoubleMatrix.Abs.Dense<U>(makeDense(in.getMatrixSI()), in.getUnit());
    }

    /**
     * Create a Dense version of this Sparse DoubleMatrix.
     * @param in DoubleMatrix.Abs.Dense the Dense DoubleMatrix
     * @return MutableDoubleMatrix.Abs.Dense
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> sparseToDense(final DoubleMatrix.Rel.Sparse<U> in)
    {
        return new MutableDoubleMatrix.Rel.Dense<U>(makeDense(in.getMatrixSI()), in.getUnit());
    }

}

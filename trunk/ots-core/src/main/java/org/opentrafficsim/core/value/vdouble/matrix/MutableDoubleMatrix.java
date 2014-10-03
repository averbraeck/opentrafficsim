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
 * MutableDoubleMatrix.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 9, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit
 */
public abstract class MutableDoubleMatrix<U extends Unit<U>> extends DoubleMatrix<U> implements
        WriteDoubleMatrixFunctions<U>, DoubleMathFunctions
{
    /** */
    private static final long serialVersionUID = 20140909L;

    /**
     * Construct a new MutableDoubleMatrix.
     * @param unit U; the unit of the new MutableDoubleMatrix
     */
    protected MutableDoubleMatrix(final U unit)
    {
        super(unit);
        // System.out.println("Created MutableDoubleMatrix");
    }

    /** If set, any modification of the data must be preceded by replacing the data with a local copy. */
    private boolean copyOnWrite = false;

    /**
     * Retrieve the value of the copyOnWrite flag.
     * @return boolean
     */
    private boolean isCopyOnWrite()
    {
        return copyOnWrite;
    }

    /**
     * Change the copyOnWrite flag.
     * @param copyOnWrite boolean; the new value for the copyOnWrite flag
     */
    final void setCopyOnWrite(final boolean copyOnWrite)
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
        for (int row = rows(); --row >= 0;)
        {
            for (int column = columns(); --column >= 0;)
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
         * Construct a new Absolute MutableDoubleMatrix.
         * @param unit U; the unit of the new Absolute MutableDoubleMatrix
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
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * Construct a new Absolute Dense MutableDoubleMatrix.
             * @param values double[][]; the initial values of the entries in the new Absolute Dense MutableDoubleMatrix
             * @param unit U; the unit of the new Absolute Dense MutableDoubleMatrix
             * @throws ValueException when values is not rectangular
             */
            public Dense(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Construct a new Absolute Dense MutableDoubleMatrix.
             * @param values DoubleScalar.Abs&lt;U&gt;[][]; the initial values of the entries in the new Absolute Dense
             *            MutableDoubleMatrix
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
             * @param values DoubleMatrix2D; the initial values of the entries in the new Absolute Dense
             *            MutableDoubleMatrix
             * @param unit U; the unit of the new Absolute Dense MutableDoubleMatrix
             */
            protected Dense(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleMatrix.Abs.Dense<U> immutable()
            {
                setCopyOnWrite(true);
                return new DoubleMatrix.Abs.Dense<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleMatrix.Abs.Dense<U> mutable()
            {
                setCopyOnWrite(true);
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
             * Construct a new Absolute Sparse MutableDoubleMatrix.
             * @param values double[][]; the initial values of the entries in the new Absolute Sparse
             *            MutableDoubleMatrix
             * @param unit U; the unit of the new Absolute Sparse MutableDoubleMatrix
             * @throws ValueException when values is not rectangular
             */
            public Sparse(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Construct a new Absolute Sparse MutableDoubleMatrix.
             * @param values DoubleScalar.Abs&lt;U&gt;[][]; the initial values of the entries in the new Absolute Sparse
             *            MutableDoubleMatrix
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
             * @param values DoubleMatrix2D; the initial values of the entries in the new Absolute Sparse
             *            MutableDoubleMatrix
             * @param unit U; the unit of the new Absolute Sparse MutableDoubleMatrix
             */
            protected Sparse(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleMatrix.Abs.Sparse<U> immutable()
            {
                setCopyOnWrite(true);
                return new DoubleMatrix.Abs.Sparse<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleMatrix.Abs.Sparse<U> mutable()
            {
                setCopyOnWrite(true);
                return new MutableDoubleMatrix.Abs.Sparse<U>(getMatrixSI(), getUnit());
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
         * Construct a new Relative MutableDoubleMatrix.
         * @param unit U; the unit of the new Relative MutableDoubleMatrix
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
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * Construct a new Relative Dense MutableDoubleMatrix.
             * @param values double[][]; the initial values of the entries in the new Relative Dense MutableDoubleMatrix
             * @param unit U; the unit of the new Relative Dense MutableDoubleMatrix
             * @throws ValueException when values is not rectangular
             */
            public Dense(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Construct a new Relative Dense MutableDoubleMatrix.
             * @param values DoubleScalar.Rel&lt;U&gt;[][]; the initial values of the entries in the new Relative Dense
             *            MutableDoubleMatrix
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
             * @param values DoubleMatrix2D; the initial values of the entries in the new Relative Dense
             *            MutableDoubleMatrix
             * @param unit U; the unit of the new Relative Dense MutableDoubleMatrix
             */
            protected Dense(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleMatrix.Rel.Dense<U> immutable()
            {
                setCopyOnWrite(true);
                return new DoubleMatrix.Rel.Dense<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleMatrix.Rel.Dense<U> mutable()
            {
                setCopyOnWrite(true);
                return new MutableDoubleMatrix.Rel.Dense<U>(getMatrixSI(), getUnit());
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
        public static class Sparse<U extends Unit<U>> extends Rel<U> implements SparseData
        {
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * Construct a new Relative Sparse MutableDoubleMatrix.
             * @param values double[][]; the initial values of the entries in the new Relative Sparse
             *            MutableDoubleMatrix
             * @param unit U; the unit of the new Relative Sparse MutableDoubleMatrix
             * @throws ValueException when values is not rectangular
             */
            public Sparse(final double[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Construct a new Relative Sparse MutableDoubleMatrix.
             * @param values DoubleScalar.Rel&lt;U&gt;[][]; the initial values of the entries in the new Relative Sparse
             *            MutableDoubleMatrix
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
             * @param values DoubleMatrix2D; the initial values of the entries in the new Relative Sparse
             *            MutableDoubleMatrix
             * @param unit U; the unit of the new Relative Sparse MutableDoubleMatrix
             */
            protected Sparse(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleMatrix.Rel.Sparse<U> immutable()
            {
                setCopyOnWrite(true);
                return new DoubleMatrix.Rel.Sparse<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleMatrix.Rel.Sparse<U> mutable()
            {
                setCopyOnWrite(true);
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
     * @return DoubleMatrix&lt;U&gt;
     */
    public abstract DoubleMatrix<U> immutable();

    /** {@inheritDoc} */
    @Override
    public final MutableDoubleMatrix<U> copy()
    {
        return immutable().mutable();
        // FIXME: This may cause both the original and the copy to be deep copied later
        // Maybe it is better to make one deep copy now...
    }

    /**
     * Check the copyOnWrite flag and, if it is set, make a deep copy of the data and clear the flag.
     */
    protected final void checkCopyOnWrite()
    {
        if (isCopyOnWrite())
        {
            // System.out.println("copyOnWrite is set: Copying data");
            deepCopyData();
            setCopyOnWrite(false);
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
    public final void setInUnit(final int row, final int column, final double value, final U valueUnit)
            throws ValueException
    {
        // TODO: creating a DoubleScalar.Abs along the way may not be the most efficient way to do this...
        setSI(row, column, new DoubleScalar.Abs<U>(value, valueUnit).getValueSI());
    }

    /**
     * Execute a function on a cell by cell basis.
     * @param d cern.colt.function.tdouble.DoubleFunction; the function to apply
     */
    public final void assign(final cern.colt.function.tdouble.DoubleFunction d)
    {
        checkCopyOnWrite();
        getMatrixSI().assign(d);
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
     * Increment the values in this MutableDoubleMatrix by the corresponding values in a DoubleMatrix.
     * @param increment DoubleMatrix&lt;U&gt;; the values by which to increment the corresponding values in this
     *            MutableDoubleMatrix
     * @return MutableDoubleMatrix&lt;U&gt;; this modified MutableDoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    private MutableDoubleMatrix<U> incrementValueByValue(final DoubleMatrix<U> increment) throws ValueException
    {
        checkSizeAndCopyOnWrite(increment);
        for (int row = rows(); --row >= 0;)
        {
            for (int column = columns(); --column >= 0;)
            {
                safeSet(row, column, safeGet(row, column) + increment.safeGet(row, column));
            }
        }
        return this;
    }

    /**
     * Increment the values in this MutableDoubleMatrix by the corresponding values in a Relative DoubleMatrix.
     * @param rel DoubleMatrix.Rel&lt;U&gt;; the Relative DoubleMatrix
     * @return MutableDoubleMatrix&lt;U&gt;; this modified MutableDoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    public final MutableDoubleMatrix<U> incrementBy(final DoubleMatrix.Rel<U> rel) throws ValueException
    {
        return incrementValueByValue(rel);
    }

    /**
     * Decrement the values in this MutableDoubleMatrix by the corresponding values in a DoubleMatrix.
     * @param decrement DoubleMatrix&lt;U&gt;; the values by which to decrement the corresponding values in this
     *            MutableDoubleMatrix
     * @return MutableDoubleMatrix&lt;U&gt;; this modified MutableDoubleMatrix
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
     * Decrement the values in this MutableDoubleMatrix by the corresponding values in a Relative DoubleMatrix.
     * @param rel DoubleMatrix.Rel&lt;U&gt;; the Relative DoubleMatrix
     * @return MutableDoubleMatrix&lt;U&gt;; this modified MutableDoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    public final MutableDoubleMatrix<U> decrementBy(final DoubleMatrix.Rel<U> rel) throws ValueException
    {
        return decrementValueByValue(rel);
    }

    /**
     * Decrement the values in this Relative MutableDoubleMatrix by the corresponding values in an Absolute
     * DoubleMatrix.
     * @param abs DoubleMatrix.Abs&lt;U&gt;; the Absolute DoubleMatrix
     * @return MutableDoubleMatrix.Rel&lt;U&gt;; this modified Relative MutableDoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    protected final MutableDoubleMatrix.Rel<U> decrementBy(final DoubleMatrix.Abs<U> abs) throws ValueException
    {
        return (MutableDoubleMatrix.Rel<U>) decrementValueByValue(abs);
    }

    /**
     * Scale the values in this MutableDoubleMatrix by the corresponding values in a DoubleMatrix.
     * @param factor DoubleMatrix&lt;?&gt;; contains the values by which to scale the corresponding values in this
     *            MutableDoubleMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    public final void scaleValueByValue(final DoubleMatrix<?> factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int row = rows(); --row >= 0;)
        {
            for (int column = columns(); --column >= 0;)
            {
                safeSet(row, column, safeGet(row, column) * factor.safeGet(row, column));
            }
        }
    }

    /**
     * Scale the values in this MutableDoubleMatrix by the corresponding values in a double array.
     * @param factor double[][]; contains the values by which to scale the corresponding values in this
     *            MutableDoubleMatrix
     * @return MutableDoubleMatrix&lt;U&gt;; this modified MutableDoubleMatrix
     * @throws ValueException when the matrix and the array do not have the same size
     */
    public final MutableDoubleMatrix<U> scaleValueByValue(final double[][] factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int row = rows(); --row >= 0;)
        {
            for (int column = columns(); --column >= 0;)
            {
                safeSet(row, column, safeGet(row, column) * factor[row][column]);
            }
        }
        return this;
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other DoubleMatrix&lt;?&gt;; partner for the size check
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
     * Add two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Abs.Dense&lt;U&gt;.
     * @param left DoubleMatrix.Abs.Dense&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Rel&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Abs.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> plus(final DoubleMatrix.Abs.Dense<U> left,
            final DoubleMatrix.Rel<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Abs.Dense&lt;U&gt;.
     * @param left DoubleMatrix.Abs.Sparse&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Rel.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Abs.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> plus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Add two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Abs.Sparse&lt;U&gt;.
     * @param left DoubleMatrix.Abs.Sparse&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Rel.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Abs.Sparse&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> plus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Rel.Dense&lt;U&gt;.
     * @param left DoubleMatrix.Rel.Dense&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Rel&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> plus(final DoubleMatrix.Rel.Dense<U> left,
            final DoubleMatrix.Rel<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Rel.Dense&lt;U&gt;.
     * @param left DoubleMatrix.Rel.Sparse&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Rel.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> plus(final DoubleMatrix.Rel.Sparse<U> left,
            final DoubleMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Add two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Rel.Sparse&lt;U&gt;.
     * @param left DoubleMatrix.Rel.Sparse&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Rel.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Rel.Sparse&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> plus(final DoubleMatrix.Rel.Sparse<U> left,
            final DoubleMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Rel.Dense&lt;U&gt;.
     * @param left DoubleMatrix.Abs.Dense&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Abs&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> minus(final DoubleMatrix.Abs.Dense<U> left,
            final DoubleMatrix.Abs<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) new MutableDoubleMatrix.Rel.Dense<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Rel.Sparse&lt;U&gt;.
     * @param left DoubleMatrix.Abs.Sparse&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Abs.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Rel.Sparse&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> minus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Abs.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Sparse<U>) new MutableDoubleMatrix.Rel.Sparse<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Rel.Dense&lt;U&gt;.
     * @param left DoubleMatrix.Abs.Sparse&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Abs.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> minus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Abs.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) new MutableDoubleMatrix.Rel.Dense<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Abs.Dense&lt;U&gt;.
     * @param left DoubleMatrix.Abs.Dense&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Rel&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Abs.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> minus(final DoubleMatrix.Abs.Dense<U> left,
            final DoubleMatrix.Rel<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Abs.Dense&lt;U&gt;.
     * @param left DoubleMatrix.Abs.Sparse&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Rel.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Abs.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> minus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Dense<U>) sparseToDense(left).decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Abs.Sparse&lt;U&gt;.
     * @param left DoubleMatrix.Abs.Sparse&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Rel.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Abs.Sparse&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> minus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Rel.Dense&lt;U&gt;.
     * @param left DoubleMatrix.Rel.Dense&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Rel&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> minus(final DoubleMatrix.Rel.Dense<U> left,
            final DoubleMatrix.Rel<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Rel.Dense&lt;U&gt;.
     * @param left DoubleMatrix.Rel.Sparse&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Rel.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> minus(final DoubleMatrix.Rel.Sparse<U> left,
            final DoubleMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) sparseToDense(left).decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices value by value and store the result in a new MutableDoubleMatrix.Rel.Sparse&lt;U&gt;.
     * @param left DoubleMatrix.Rel.Sparse&lt;U&gt;; the left operand
     * @param right DoubleMatrix.Rel.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleMatrix.Rel.Sparse&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> minus(final DoubleMatrix.Rel.Sparse<U> left,
            final DoubleMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Multiply two DoubleMatrices value by value and store the result in a new
     * MutableDoubleMatrix.Abs.Dense&lt;SIUnit&gt;.
     * @param left DoubleMatrix.Abs.Dense&lt;?&gt;; the left operand
     * @param right DoubleMatrix.Abs.Dense&lt;?&gt;; the right operand
     * @return MutableDoubleMatrix.Abs.Dense&lt;SIUnit&gt;
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
     * Multiply two DoubleMatrices value by value and store the result in a new
     * MutableDoubleMatrix.Abs.Sparse&lt;SIUnit&gt;.
     * @param left DoubleMatrix.Abs.Dense&lt;?&gt;; the left operand
     * @param right DoubleMatrix.Abs.Sparse&lt;?&gt;; the right operand
     * @return MutableDoubleMatrix.Abs.Sparse&lt;SIUnit&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static MutableDoubleMatrix.Abs.Sparse<SIUnit> times(final DoubleMatrix.Abs.Dense<?> left,
            final DoubleMatrix.Abs.Sparse<?> right) throws ValueException
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
     * Multiply two DoubleMatrices value by value and store the result in a new
     * MutableDoubleMatrix.Abs.Sparse&lt;SIUnit&gt;.
     * @param left DoubleMatrix.Abs.Sparse&lt;?&gt;; the left operand
     * @param right DoubleMatrix.Abs&lt;?&gt;; the right operand
     * @return MutableDoubleMatrix.Abs.Sparse&lt;SIUnit&gt;
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
     * Multiply two DoubleMatrices value by value and store the result in a new
     * MutableDoubleMatrix.Rel.Dense&lt;SIUnit&gt;.
     * @param left DoubleMatrix.Rel.Dense&lt;?&gt;; the left operand
     * @param right DoubleMatrix.Rel.Dense&lt;?&gt;; the right operand
     * @return MutableDoubleMatrix.Rel.Dense&lt;SIUnit&gt;
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
     * Multiply two DoubleMatrices value by value and store the result in a new
     * MutableDoubleMatrix.Rel.Sparse&lt;SIUnit&gt;.
     * @param left DoubleMatrix.Rel.Dense&lt;?&gt;; the left operand
     * @param right DoubleMatrix.Rel.Sparse&lt;?&gt;; the right operand
     * @return MutableDoubleMatrix.Rel.Sparse&lt;SIUnit&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static MutableDoubleMatrix.Rel.Sparse<SIUnit> times(final DoubleMatrix.Rel.Dense<?> left,
            final DoubleMatrix.Rel.Sparse<?> right) throws ValueException
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
     * Multiply two DoubleMatrices value by value and store the result in a new
     * MutableDoubleMatrix.Rel.Sparse&lt;SIUnit&gt;.
     * @param left DoubleMatrix.Rel.Sparse&lt;?&gt;; the left operand
     * @param right DoubleMatrix.Rel&lt;?&gt;; the right operand
     * @return MutableDoubleMatrix.Rel.Sparse&lt;SIUnit&gt;
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
     * Multiply the values in a DoubleMatrix and a double array value by value and store the result in a new
     * MutableDoubleMatrix.Abs.Dense&lt;U&gt;.
     * @param left DoubleMatrix.Abs.Dense&lt;U&gt;; the DoubleMatrix
     * @param right double[][]; the double array
     * @param <U> Unit; the unit of the left parameter and the result
     * @return MutableDoubleMatrix.Abs.Dense&lt;U&gt;
     * @throws ValueException when the DoubleMatrix and the array do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> times(final DoubleMatrix.Abs.Dense<U> left,
            final double[][] right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleMatrix and a double array value by value and store the result in a new
     * MutableDoubleMatrix.Abs.Sparse&lt;U&gt;.
     * @param left DoubleMatrix.Abs.Sparse&lt;U&gt;; the DoubleMatrix
     * @param right double[][]; the double array
     * @param <U> Unit; the unit of the left parameter and the result
     * @return MutableDoubleMatrix.Abs.Sparse&lt;U&gt;
     * @throws ValueException when the DoubleMatrix and the array do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> times(final DoubleMatrix.Abs.Sparse<U> left,
            final double[][] right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleMatrix and a double array value by value and store the result in a new
     * MutableDoubleMatrix.Rel.Dense&lt;U&gt;.
     * @param left DoubleMatrix.Rel.Dense&lt;U&gt;; the DoubleMatrix
     * @param right double[][]; the double array
     * @param <U> Unit; the unit of the left parameter and the result
     * @return MutableDoubleMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the DoubleMatrix and the array do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> times(final DoubleMatrix.Rel.Dense<U> left,
            final double[][] right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleMatrix and a double array value by value and store the result in a new
     * MutableDoubleMatrix.Rel.Sparse&lt;U&gt;.
     * @param left DoubleMatrix.Rel.Sparse&lt;U&gt;; the DoubleMatrix
     * @param right double[][]; the double array
     * @param <U> Unit; the unit of the left parameter and the result
     * @return MutableDoubleMatrix.Rel.Sparse&lt;U&gt;
     * @throws ValueException when the DoubleMatrix and the array do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> times(final DoubleMatrix.Rel.Sparse<U> left,
            final double[][] right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Make the Sparse equivalent of a DenseDoubleMatrix2D.
     * @param dense DoubleMatrix2D; the Dense DoubleMatrix2D
     * @return SparseDoubleMatrix2D
     */
    private static SparseDoubleMatrix2D makeSparse(final DoubleMatrix2D dense)
    {
        SparseDoubleMatrix2D result = new SparseDoubleMatrix2D(dense.rows(), dense.columns());
        result.assign(dense);
        return result;
    }

    /**
     * Create a Sparse version of a Dense DoubleMatrix.
     * @param in DoubleMatrix.Abs.Dense&lt;U&gt;; the Dense DoubleMatrix
     * @param <U> Unit; the unit of the parameter and the result
     * @return MutableDoubleMatrix.Abs.Sparse&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> denseToSparse(final DoubleMatrix.Abs.Dense<U> in)
    {
        return new MutableDoubleMatrix.Abs.Sparse<U>(makeSparse(in.getMatrixSI()), in.getUnit());
    }

    /**
     * Create a Sparse version of a Dense DoubleMatrix.
     * @param in DoubleMatrix.Rel.Dense&lt;U&gt;; the Dense DoubleMatrix
     * @param <U> Unit; the unit of the parameter and the result
     * @return MutableDoubleMatrix.Rel.Sparse&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> denseToSparse(final DoubleMatrix.Rel.Dense<U> in)
    {
        return new MutableDoubleMatrix.Rel.Sparse<U>(makeSparse(in.getMatrixSI()), in.getUnit());
    }

    /**
     * Make the Dense equivalent of a SparseDoubleMatrix2D.
     * @param sparse DoubleMatrix2D; the Sparse DoubleMatrix2D
     * @return DenseDoubleMatrix2D
     */
    private static DenseDoubleMatrix2D makeDense(final DoubleMatrix2D sparse)
    {
        DenseDoubleMatrix2D result = new DenseDoubleMatrix2D(sparse.rows(), sparse.columns());
        result.assign(sparse);
        return result;
    }

    /**
     * Create a Dense version of a Sparse DoubleMatrix.
     * @param in DoubleMatrix.Abs.Sparse&lt;U&gt;; the Sparse DoubleMatrix
     * @param <U> Unit; the unit of the parameter and the result
     * @return MutableDoubleMatrix.Abs.Dense&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> sparseToDense(final DoubleMatrix.Abs.Sparse<U> in)
    {
        return new MutableDoubleMatrix.Abs.Dense<U>(makeDense(in.getMatrixSI()), in.getUnit());
    }

    /**
     * Create a Dense version of a Sparse DoubleMatrix.
     * @param in DoubleMatrix.Rel.Sparse&lt;U&gt;; the Sparse DoubleMatrix
     * @param <U> Unit; the unit of the parameter and the result
     * @return MutableDoubleMatrix.Rel.Dense&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> sparseToDense(final DoubleMatrix.Rel.Sparse<U> in)
    {
        return new MutableDoubleMatrix.Rel.Dense<U>(makeDense(in.getMatrixSI()), in.getUnit());
    }

}

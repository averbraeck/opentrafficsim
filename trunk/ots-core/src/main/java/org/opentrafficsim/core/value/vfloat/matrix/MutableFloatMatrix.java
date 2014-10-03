package org.opentrafficsim.core.value.vfloat.matrix;

import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.DenseData;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.SparseData;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.FloatMathFunctions;
import org.opentrafficsim.core.value.vfloat.FloatMathFunctionsImpl;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

import cern.colt.matrix.tfloat.FloatMatrix2D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import cern.jet.math.tfloat.FloatFunctions;

/**
 * MutableFloatMatrix.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 9, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> the Unit for this MutableFloatMatrix
 */
public abstract class MutableFloatMatrix<U extends Unit<U>> extends FloatMatrix<U> implements
        WriteFloatMatrixFunctions<U>, FloatMathFunctions
{
    /** */
    private static final long serialVersionUID = 20140909L;

    /**
     * Construct a new MutableFloatMatrix.
     * @param unit U; the unit of the new MutableFloatMatrix
     */
    protected MutableFloatMatrix(final U unit)
    {
        super(unit);
        // System.out.println("Created MutableFloatMatrix");
    }

    /** If set, any modification of the data must be preceded by replacing the data with a local copy. */
    private boolean copyOnWrite = false;

    /**
     * Retrieve the value of the copyOnWrite flag.
     * @return boolean
     */
    private boolean isCopyOnWrite()
    {
        return this.copyOnWrite;
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
        float sum = zSum();
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
    public abstract static class Abs<U extends Unit<U>> extends MutableFloatMatrix<U> implements Absolute
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Construct a new Absolute MutableFloatMatrix.
         * @param unit U; the unit of the new Absolute MutableFloatMatrix
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
             * Construct a new Absolute Dense MutableFloatMatrix.
             * @param values float[][]; the initial values of the entries in the new Absolute Dense MutableFloatMatrix
             * @param unit U; the unit of the new Absolute Dense MutableFloatMatrix
             * @throws ValueException when values is not rectangular
             */
            public Dense(final float[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Construct a new Absolute Dense MutableFloatMatrix.
             * @param values FloatScalar.Abs&lt;U&gt;[][]; the initial values of the entries in the new Absolute Dense
             *            MutableFloatMatrix
             * @throws ValueException when values has zero entries, or is not rectangular
             */
            public Dense(final FloatScalar.Abs<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values FloatMatrix2D; the initial values of the entries in the new Absolute Dense
             *            MutableFloatMatrix
             * @param unit U; the unit of the new Absolute Dense MutableFloatMatrix
             */
            protected Dense(final FloatMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final FloatMatrix.Abs.Dense<U> immutable()
            {
                setCopyOnWrite(true);
                return new FloatMatrix.Abs.Dense<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatMatrix.Abs.Dense<U> mutable()
            {
                setCopyOnWrite(true);
                return new MutableFloatMatrix.Abs.Dense<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final FloatMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new DenseFloatMatrix2D(rows, columns);
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
             * Construct a new Absolute Sparse MutableFloatMatrix.
             * @param values float[][]; the initial values of the entries in the new Absolute Sparse MutableFloatMatrix
             * @param unit U; the unit of the new Absolute Sparse MutableFloatMatrix
             * @throws ValueException when values is not rectangular
             */
            public Sparse(final float[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Construct a new Absolute Sparse MutableFloatMatrix.
             * @param values FloatScalar.Abs&lt;U&gt;[][]; the initial values of the entries in the new Absolute Sparse
             *            MutableFloatMatrix
             * @throws ValueException when values has zero entries, or is not rectangular
             */
            public Sparse(final FloatScalar.Abs<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values FloatMatrix2D; the initial values of the entries in the new Absolute Sparse
             *            MutableFloatMatrix
             * @param unit U; the unit of the new Absolute Sparse MutableFloatMatrix
             */
            protected Sparse(final FloatMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final FloatMatrix.Abs.Sparse<U> immutable()
            {
                setCopyOnWrite(true);
                return new FloatMatrix.Abs.Sparse<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatMatrix.Abs.Sparse<U> mutable()
            {
                setCopyOnWrite(true);
                return new MutableFloatMatrix.Abs.Sparse<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final FloatMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new SparseFloatMatrix2D(rows, columns);
            }

        }

        /** {@inheritDoc} */
        @Override
        public final FloatScalar.Abs<U> get(final int row, final int column) throws ValueException
        {
            return new FloatScalar.Abs<U>(getInUnit(row, column, getUnit()), getUnit());
        }

    }

    /**
     * @param <U> Unit
     */
    public abstract static class Rel<U extends Unit<U>> extends MutableFloatMatrix<U> implements Relative
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Construct a new Relative MutableFloatMatrix.
         * @param unit U; the unit of the new Relative MutableFloatMatrix
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
             * Construct a new Relative Dense MutableFloatMatrix.
             * @param values float[][]; the initial values of the entries in the new Relative Dense MutableFloatMatrix
             * @param unit U; the unit of the new Relative Dense MutableFloatMatrix
             * @throws ValueException when values is not rectangular
             */
            public Dense(final float[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Construct a new Relative Dense MutableFloatMatrix.
             * @param values FloatScalar.Rel&lt;U&gt;[][]; the initial values of the entries in the new Relative Dense
             *            MutableFloatMatrix
             * @throws ValueException when values has zero entries, or is not rectangular
             */
            public Dense(final FloatScalar.Rel<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values FloatMatrix2D; the initial values of the entries in the new Relative Dense
             *            MutableFloatMatrix
             * @param unit U; the unit of the new Relative Dense MutableFloatMatrix
             */
            protected Dense(final FloatMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final FloatMatrix.Rel.Dense<U> immutable()
            {
                setCopyOnWrite(true);
                return new FloatMatrix.Rel.Dense<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatMatrix.Rel.Dense<U> mutable()
            {
                setCopyOnWrite(true);
                return new MutableFloatMatrix.Rel.Dense<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final FloatMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new DenseFloatMatrix2D(rows, columns);
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
             * Construct a new Relative Sparse MutableFloatMatrix.
             * @param values float[][]; the initial values of the entries in the new Relative Sparse MutableFloatMatrix
             * @param unit U; the unit of the new Relative Sparse MutableFloatMatrix
             * @throws ValueException when values is not rectangular
             */
            public Sparse(final float[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Construct a new Relative Sparse MutableFloatMatrix.
             * @param values FloatScalar.Rel&lt;U&gt;[][]; the initial values of the entries in the new Relative Sparse
             *            MutableFloatMatrix
             * @throws ValueException when values has zero entries, or is not rectangular
             */
            public Sparse(final FloatScalar.Rel<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values FloatMatrix2D; the initial values of the entries in the new Relative Sparse
             *            MutableFloatMatrix
             * @param unit U; the unit of the new Relative Sparse MutableFloatMatrix
             */
            protected Sparse(final FloatMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final FloatMatrix.Rel.Sparse<U> immutable()
            {
                setCopyOnWrite(true);
                return new FloatMatrix.Rel.Sparse<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatMatrix.Rel.Sparse<U> mutable()
            {
                setCopyOnWrite(true);
                return new MutableFloatMatrix.Rel.Sparse<U>(getMatrixSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final FloatMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new SparseFloatMatrix2D(rows, columns);
            }

        }

        /** {@inheritDoc} */
        @Override
        public final FloatScalar.Rel<U> get(final int row, final int column) throws ValueException
        {
            return new FloatScalar.Rel<U>(getInUnit(row, column, getUnit()), getUnit());
        }

    }

    /**
     * Make (immutable) FloatMatrix equivalent for any type of MutableFloatMatrix.
     * @return FloatMatrix&lt;U&gt;
     */
    public abstract FloatMatrix<U> immutable();

    /** {@inheritDoc} */
    @Override
    public final MutableFloatMatrix<U> copy()
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
        if (this.isCopyOnWrite())
        {
            // System.out.println("copyOnWrite is set: Copying data");
            deepCopyData();
            setCopyOnWrite(false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void setSI(final int row, final int column, final float valueSI) throws ValueException
    {
        checkIndex(row, column);
        checkCopyOnWrite();
        safeSet(row, column, valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void set(final int row, final int column, final FloatScalar<U> value) throws ValueException
    {
        setSI(row, column, value.getValueSI());
    }

    /** {@inheritDoc} */
    @Override
    public final void setInUnit(final int row, final int column, final float value, final U valueUnit)
            throws ValueException
    {
        // TODO: creating a FloatScalar.Abs along the way may not be the most efficient way to do this...
        setSI(row, column, new FloatScalar.Abs<U>(value, valueUnit).getValueSI());
    }

    /**
     * Execute a function on a cell by cell basis.
     * @param f cern.colt.function.tfloat.FloatFunction; the function to apply
     */
    public final void assign(final cern.colt.function.tfloat.FloatFunction f)
    {
        checkCopyOnWrite();
        getMatrixSI().assign(f);
    }

    /** {@inheritDoc} */
    @Override
    public final void abs()
    {
        assign(FloatFunctions.abs);
    }

    /** {@inheritDoc} */
    @Override
    public final void acos()
    {
        assign(FloatFunctions.acos);
    }

    /** {@inheritDoc} */
    @Override
    public final void asin()
    {
        assign(FloatFunctions.asin);
    }

    /** {@inheritDoc} */
    @Override
    public final void atan()
    {
        assign(FloatFunctions.atan);
    }

    /** {@inheritDoc} */
    @Override
    public final void cbrt()
    {
        assign(FloatMathFunctionsImpl.cbrt);
    }

    /** {@inheritDoc} */
    @Override
    public final void ceil()
    {
        assign(FloatFunctions.ceil);
    }

    /** {@inheritDoc} */
    @Override
    public final void cos()
    {
        assign(FloatFunctions.cos);
    }

    /** {@inheritDoc} */
    @Override
    public final void cosh()
    {
        assign(FloatMathFunctionsImpl.cosh);
    }

    /** {@inheritDoc} */
    @Override
    public final void exp()
    {
        assign(FloatFunctions.exp);
    }

    /** {@inheritDoc} */
    @Override
    public final void expm1()
    {
        assign(FloatMathFunctionsImpl.expm1);
    }

    /** {@inheritDoc} */
    @Override
    public final void floor()
    {
        assign(FloatFunctions.floor);
    }

    /** {@inheritDoc} */
    @Override
    public final void log()
    {
        assign(FloatFunctions.log);
    }

    /** {@inheritDoc} */
    @Override
    public final void log10()
    {
        assign(FloatMathFunctionsImpl.log10);
    }

    /** {@inheritDoc} */
    @Override
    public final void log1p()
    {
        assign(FloatMathFunctionsImpl.log1p);
    }

    /** {@inheritDoc} */
    @Override
    public final void pow(final double x)
    {
        assign(FloatFunctions.pow((float) x));
    }

    /** {@inheritDoc} */
    @Override
    public final void rint()
    {
        assign(FloatFunctions.rint);
    }

    /** {@inheritDoc} */
    @Override
    public final void round()
    {
        assign(FloatMathFunctionsImpl.round);
    }

    /** {@inheritDoc} */
    @Override
    public final void signum()
    {
        assign(FloatMathFunctionsImpl.signum);
    }

    /** {@inheritDoc} */
    @Override
    public final void sin()
    {
        assign(FloatFunctions.sin);
    }

    /** {@inheritDoc} */
    @Override
    public final void sinh()
    {
        assign(FloatMathFunctionsImpl.sinh);
    }

    /** {@inheritDoc} */
    @Override
    public final void sqrt()
    {
        assign(FloatFunctions.sqrt);
    }

    /** {@inheritDoc} */
    @Override
    public final void tan()
    {
        assign(FloatFunctions.tan);
    }

    /** {@inheritDoc} */
    @Override
    public final void tanh()
    {
        assign(FloatMathFunctionsImpl.tanh);
    }

    /** {@inheritDoc} */
    @Override
    public final void toDegrees()
    {
        assign(FloatMathFunctionsImpl.toDegrees);
    }

    /** {@inheritDoc} */
    @Override
    public final void toRadians()
    {
        assign(FloatMathFunctionsImpl.toRadians);
    }

    /** {@inheritDoc} */
    @Override
    public final void inv()
    {
        assign(FloatFunctions.inv);
    }

    /** {@inheritDoc} */
    @Override
    public final void multiply(final float constant)
    {
        assign(FloatFunctions.mult(constant));
    }

    /** {@inheritDoc} */
    @Override
    public final void divide(final float constant)
    {
        assign(FloatFunctions.div(constant));
    }

    /**
     * Increment the values in this MutableFloatMatrix by the corresponding values in a FloatMatrix.
     * @param increment FloatMatrix&lt;U&gt;; the values by which to increment the corresponding values in this
     *            MutableFloatMatrix
     * @return MutableFloatMatrix&lt;U&gt;; this modified MutableFloatMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    private MutableFloatMatrix<U> incrementValueByValue(final FloatMatrix<U> increment) throws ValueException
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
     * Increment the values in this MutableFloatMatrix by the corresponding values in a Relative FloatMatrix.
     * @param rel FloatMatrix.Rel&lt;U&gt;; the Relative FloatMatrix
     * @return MutableFloatMatrix&lt;U&gt;; this modified MutableFloatMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    public final MutableFloatMatrix<U> incrementBy(final FloatMatrix.Rel<U> rel) throws ValueException
    {
        return incrementValueByValue(rel);
    }

    /**
     * Decrement the values in this MutableFloatMatrix by the corresponding values in a FloatMatrix.
     * @param decrement FloatMatrix&lt;U&gt;; the values by which to decrement the corresponding values in this
     *            MutableFloatMatrix
     * @return MutableFloatMatrix&lt;U&gt;; this modified MutableFloatMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    private MutableFloatMatrix<U> decrementValueByValue(final FloatMatrix<U> decrement) throws ValueException
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
     * Decrement the values in this MutableFloatMatrix by the corresponding values in a Relative FloatMatrix.
     * @param rel FloatMatrix.Rel&lt;U&gt;; the Relative FloatMatrix
     * @return MutableFloatMatrix&lt;U&gt;; this modified MutableFloatMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    public final MutableFloatMatrix<U> decrementBy(final FloatMatrix.Rel<U> rel) throws ValueException
    {
        return decrementValueByValue(rel);
    }

    /**
     * Decrement the values in this Relative MutableFloatMatrix by the corresponding values in an Absolute FloatMatrix.
     * @param abs FloatMatrix.Abs&lt;U&gt;; the Absolute FloatMatrix
     * @return MutableFloatMatrix.Rel&lt;U&gt;; this modified Relative MutableFloatMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    protected final MutableFloatMatrix.Rel<U> decrementBy(final FloatMatrix.Abs<U> abs) throws ValueException
    {
        return (MutableFloatMatrix.Rel<U>) decrementValueByValue(abs);
    }

    /**
     * Scale the values in this MutableFloatMatrix by the corresponding values in a FloatMatrix.
     * @param factor FloatMatrix&lt;?&gt;; contains the values by which to scale the corresponding values in this
     *            MutableFloatMatrix
     * @throws ValueException when the matrices do not have the same size
     */
    public final void scaleValueByValue(final FloatMatrix<?> factor) throws ValueException
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
     * Scale the values in this MutableFloatMatrix by the corresponding values in a float array.
     * @param factor float[][]; contains the values by which to scale the corresponding values in this
     *            MutableFloatMatrix
     * @return MutableFloatMatrix&lt;U&gt;; this modified MutableFloatMatrix
     * @throws ValueException when the matrix and the array do not have the same size
     */
    public final MutableFloatMatrix<U> scaleValueByValue(final float[][] factor) throws ValueException
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
     * @param other FloatMatrix&lt;?&gt;; partner for the size check
     * @throws ValueException when the matrices do not have the same size
     */
    private void checkSizeAndCopyOnWrite(final FloatMatrix<?> other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other float[][]; partner for the size check
     * @throws ValueException when the matrices do not have the same size
     */
    private void checkSizeAndCopyOnWrite(final float[][] other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Add two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Abs.Dense&lt;U&gt;.
     * @param left FloatMatrix.Abs.Dense&lt;U&gt;; the left operand
     * @param right FloatMatrix.Rel&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Abs.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Dense<U> plus(final FloatMatrix.Abs.Dense<U> left,
            final FloatMatrix.Rel<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Abs.Dense&lt;U&gt;.
     * @param left FloatMatrix.Abs.Sparse&lt;U&gt;; the left operand
     * @param right FloatMatrix.Rel.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Abs.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Dense<U> plus(final FloatMatrix.Abs.Sparse<U> left,
            final FloatMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Add two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Abs.Sparse&lt;U&gt;.
     * @param left FloatMatrix.Abs.Sparse&lt;U&gt;; the left operand
     * @param right FloatMatrix.Rel.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Abs.Sparse&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Sparse<U> plus(final FloatMatrix.Abs.Sparse<U> left,
            final FloatMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Rel.Dense&lt;U&gt;.
     * @param left FloatMatrix.Rel.Dense&lt;U&gt;; the left operand
     * @param right FloatMatrix.Rel&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> plus(final FloatMatrix.Rel.Dense<U> left,
            final FloatMatrix.Rel<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Rel.Dense&lt;U&gt;.
     * @param left FloatMatrix.Rel.Sparse&lt;U&gt;; the left operand
     * @param right FloatMatrix.Rel.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> plus(final FloatMatrix.Rel.Sparse<U> left,
            final FloatMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Add two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Rel.Sparse&lt;U&gt;.
     * @param left FloatMatrix.Rel.Sparse&lt;U&gt;; the left operand
     * @param right FloatMatrix.Rel.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Rel.Sparse&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Sparse<U> plus(final FloatMatrix.Rel.Sparse<U> left,
            final FloatMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Subtract two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Rel.Dense&lt;U&gt;.
     * @param left FloatMatrix.Abs.Dense&lt;U&gt;; the left operand
     * @param right FloatMatrix.Abs&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> minus(final FloatMatrix.Abs.Dense<U> left,
            final FloatMatrix.Abs<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) new MutableFloatMatrix.Rel.Dense<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Rel.Sparse&lt;U&gt;.
     * @param left FloatMatrix.Abs.Sparse&lt;U&gt;; the left operand
     * @param right FloatMatrix.Abs.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Rel.Sparse&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Sparse<U> minus(final FloatMatrix.Abs.Sparse<U> left,
            final FloatMatrix.Abs.Sparse<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Sparse<U>) new MutableFloatMatrix.Rel.Sparse<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Rel.Dense&lt;U&gt;.
     * @param left FloatMatrix.Abs.Sparse&lt;U&gt;; the left operand
     * @param right FloatMatrix.Abs.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> minus(final FloatMatrix.Abs.Sparse<U> left,
            final FloatMatrix.Abs.Dense<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) new MutableFloatMatrix.Rel.Dense<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Abs.Dense&lt;U&gt;.
     * @param left FloatMatrix.Abs.Dense&lt;U&gt;; the left operand
     * @param right FloatMatrix.Rel&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Abs.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Dense<U> minus(final FloatMatrix.Abs.Dense<U> left,
            final FloatMatrix.Rel<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Abs.Dense&lt;U&gt;.
     * @param left FloatMatrix.Abs.Sparse&lt;U&gt;; the left operand
     * @param right FloatMatrix.Rel.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Abs.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Dense<U> minus(final FloatMatrix.Abs.Sparse<U> left,
            final FloatMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Dense<U>) sparseToDense(left).decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Abs.Sparse&lt;U&gt;.
     * @param left FloatMatrix.Abs.Sparse&lt;U&gt;; the left operand
     * @param right FloatMatrix.Rel.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Abs.Sparse&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Sparse<U> minus(final FloatMatrix.Abs.Sparse<U> left,
            final FloatMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Rel.Dense&lt;U&gt;.
     * @param left FloatMatrix.Rel.Dense&lt;U&gt;; the left operand
     * @param right FloatMatrix.Rel&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> minus(final FloatMatrix.Rel.Dense<U> left,
            final FloatMatrix.Rel<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Rel.Dense&lt;U&gt;.
     * @param left FloatMatrix.Rel.Sparse&lt;U&gt;; the left operand
     * @param right FloatMatrix.Rel.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> minus(final FloatMatrix.Rel.Sparse<U> left,
            final FloatMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) sparseToDense(left).decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices value by value and store the result in a new MutableFloatMatrix.Rel.Sparse&lt;U&gt;.
     * @param left FloatMatrix.Rel.Sparse&lt;U&gt;; the left operand
     * @param right FloatMatrix.Rel.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableFloatMatrix.Rel.Sparse&lt;U&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Sparse<U> minus(final FloatMatrix.Rel.Sparse<U> left,
            final FloatMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Multiply two FloatMatrices value by value and store the result in a new
     * MutableFloatMatrix.Abs.Dense&lt;SIUnit&gt;.
     * @param left FloatMatrix.Abs.Dense&lt;?&gt;; the left operand
     * @param right FloatMatrix.Abs.Dense&lt;?&gt;; the right operand
     * @return MutableFloatMatrix.Abs.Dense&lt;SIUnit&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static MutableFloatMatrix.Abs.Dense<SIUnit> times(final FloatMatrix.Abs.Dense<?> left,
            final FloatMatrix.Abs.Dense<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatMatrix.Abs.Dense<SIUnit> work =
                new MutableFloatMatrix.Abs.Dense<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two FloatMatrices value by value and store the result in a new
     * MutableFloatMatrix.Abs.Sparse&lt;SIUnit&gt;.
     * @param left FloatMatrix.Abs.Dense&lt;?&gt;; the left operand
     * @param right FloatMatrix.Abs.Sparse&lt;?&gt;; the right operand
     * @return MutableFloatMatrix.Abs.Sparse&lt;SIUnit&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static MutableFloatMatrix.Abs.Sparse<SIUnit> times(final FloatMatrix.Abs.Dense<?> left,
            final FloatMatrix.Abs.Sparse<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatMatrix.Abs.Sparse<SIUnit> work =
                new MutableFloatMatrix.Abs.Sparse<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two FloatMatrices value by value and store the result in a new
     * MutableFloatMatrix.Abs.Sparse&lt;SIUnit&gt;.
     * @param left FloatMatrix.Abs.Sparse&lt;?&gt;; the left operand
     * @param right FloatMatrix.Abs&lt;?&gt;; the right operand
     * @return MutableFloatMatrix.Abs.Sparse&lt;SIUnit&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static MutableFloatMatrix.Abs.Sparse<SIUnit> times(final FloatMatrix.Abs.Sparse<?> left,
            final FloatMatrix.Abs<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatMatrix.Abs.Sparse<SIUnit> work =
                new MutableFloatMatrix.Abs.Sparse<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two FloatMatrices value by value and store the result in a new
     * MutableFloatMatrix.Rel.Dense&lt;SIUnit&gt;.
     * @param left FloatMatrix.Rel.Dense&lt;?&gt;; the left operand
     * @param right FloatMatrix.Rel.Dense&lt;?&gt;; the right operand
     * @return MutableFloatMatrix.Rel.Dense&lt;SIUnit&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static MutableFloatMatrix.Rel.Dense<SIUnit> times(final FloatMatrix.Rel.Dense<?> left,
            final FloatMatrix.Rel.Dense<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatMatrix.Rel.Dense<SIUnit> work =
                new MutableFloatMatrix.Rel.Dense<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two FloatMatrices value by value and store the result in a new
     * MutableFloatMatrix.Rel.Sparse&lt;SIUnit&gt;.
     * @param left FloatMatrix.Rel.Dense&lt;?&gt;; the left operand
     * @param right FloatMatrix.Rel.Sparse&lt;?&gt;; the right operand
     * @return MutableFloatMatrix.Rel.Sparse&lt;SIUnit&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static MutableFloatMatrix.Rel.Sparse<SIUnit> times(final FloatMatrix.Rel.Dense<?> left,
            final FloatMatrix.Rel.Sparse<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatMatrix.Rel.Sparse<SIUnit> work =
                new MutableFloatMatrix.Rel.Sparse<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two FloatMatrices value by value and store the result in a new
     * MutableFloatMatrix.Rel.Sparse&lt;SIUnit&gt;.
     * @param left FloatMatrix.Rel.Sparse&lt;?&gt;; the left operand
     * @param right FloatMatrix.Rel&lt;?&gt;; the right operand
     * @return MutableFloatMatrix.Rel.Sparse&lt;SIUnit&gt;
     * @throws ValueException when the matrices do not have the same size
     */
    public static MutableFloatMatrix.Rel.Sparse<SIUnit> times(final FloatMatrix.Rel.Sparse<?> left,
            final FloatMatrix.Rel<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatMatrix.Rel.Sparse<SIUnit> work =
                new MutableFloatMatrix.Rel.Sparse<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply the values in a FloatMatrix and a float array value by value and store the result in a new
     * MutableFloatMatrix.Abs.Dense&lt;U&gt;.
     * @param left FloatMatrix.Abs.Dense&lt;U&gt;; the FloatMatrix
     * @param right float[][]; the float array
     * @param <U> Unit; the unit of the left parameter and the result
     * @return MutableFloatMatrix.Abs.Dense&lt;U&gt;
     * @throws ValueException when the FloatMatrix and the array do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Dense<U> times(final FloatMatrix.Abs.Dense<U> left,
            final float[][] right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a FloatMatrix and a float array value by value and store the result in a new
     * MutableFloatMatrix.Abs.Sparse&lt;U&gt;.
     * @param left FloatMatrix.Abs.Sparse&lt;U&gt;; the FloatMatrix
     * @param right float[][]; the float array
     * @param <U> Unit; the unit of the left parameter and the result
     * @return MutableFloatMatrix.Abs.Sparse&lt;U&gt;
     * @throws ValueException when the FloatMatrix and the array do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Sparse<U> times(final FloatMatrix.Abs.Sparse<U> left,
            final float[][] right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a FloatMatrix and a float array value by value and store the result in a new
     * MutableFloatMatrix.Rel.Dense&lt;U&gt;.
     * @param left FloatMatrix.Rel.Dense&lt;U&gt;; the FloatMatrix
     * @param right float[][]; the float array
     * @param <U> Unit; the unit of the left parameter and the result
     * @return MutableFloatMatrix.Rel.Dense&lt;U&gt;
     * @throws ValueException when the FloatMatrix and the array do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> times(final FloatMatrix.Rel.Dense<U> left,
            final float[][] right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a FloatMatrix and a float array value by value and store the result in a new
     * MutableFloatMatrix.Rel.Sparse&lt;U&gt;.
     * @param left FloatMatrix.Rel.Sparse&lt;U&gt;; the FloatMatrix
     * @param right float[][]; the float array
     * @param <U> Unit; the unit of the left parameter and the result
     * @return MutableFloatMatrix.Rel.Sparse&lt;U&gt;
     * @throws ValueException when the FloatMatrix and the array do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Sparse<U> times(final FloatMatrix.Rel.Sparse<U> left,
            final float[][] right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Make the Sparse equivalent of a DenseFloatMatrix2D.
     * @param dense FloatMatrix2D; the Dense FloatMatrix2D
     * @return SparseFloatMatrix2D
     */
    private static SparseFloatMatrix2D makeSparse(final FloatMatrix2D dense)
    {
        SparseFloatMatrix2D result = new SparseFloatMatrix2D(dense.rows(), dense.columns());
        result.assign(dense);
        return result;
    }

    /**
     * Create a Sparse version of a Dense FloatMatrix.
     * @param in FloatMatrix.Abs.Dense&lt;U&gt;; the Dense FloatMatrix
     * @param <U> Unit; the unit of the parameter and the result
     * @return MutableFloatMatrix.Abs.Sparse&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Sparse<U> denseToSparse(final FloatMatrix.Abs.Dense<U> in)
    {
        return new MutableFloatMatrix.Abs.Sparse<U>(makeSparse(in.getMatrixSI()), in.getUnit());
    }

    /**
     * Create a Sparse version of a Dense FloatMatrix.
     * @param in FloatMatrix.Rel.Dense&lt;U&gt;; the Dense FloatMatrix
     * @param <U> Unit; the unit of the parameter and the result
     * @return MutableFloatMatrix.Rel.Sparse&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Sparse<U> denseToSparse(final FloatMatrix.Rel.Dense<U> in)
    {
        return new MutableFloatMatrix.Rel.Sparse<U>(makeSparse(in.getMatrixSI()), in.getUnit());
    }

    /**
     * Make the Dense equivalent of a SparseFloatMatrix2D.
     * @param sparse FloatMatrix2D; the Sparse FloatMatrix2D
     * @return DenseFloatMatrix2D
     */
    private static DenseFloatMatrix2D makeDense(final FloatMatrix2D sparse)
    {
        DenseFloatMatrix2D result = new DenseFloatMatrix2D(sparse.rows(), sparse.columns());
        result.assign(sparse);
        return result;
    }

    /**
     * Create a Dense version of a Sparse FloatMatrix.
     * @param in FloatMatrix.Abs.Sparse&lt;U&gt;; the Sparse FloatMatrix
     * @param <U> Unit; the unit of the parameter and the result
     * @return MutableFloatMatrix.Abs.Dense&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Dense<U> sparseToDense(final FloatMatrix.Abs.Sparse<U> in)
    {
        return new MutableFloatMatrix.Abs.Dense<U>(makeDense(in.getMatrixSI()), in.getUnit());
    }

    /**
     * Create a Dense version of a Sparse FloatMatrix.
     * @param in FloatMatrix.Rel.Sparse&lt;U&gt;; the Sparse FloatMatrix
     * @param <U> Unit; the unit of the parameter and the result
     * @return MutableFloatMatrix.Rel.Dense&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> sparseToDense(final FloatMatrix.Rel.Sparse<U> in)
    {
        return new MutableFloatMatrix.Rel.Dense<U>(makeDense(in.getMatrixSI()), in.getUnit());
    }

}

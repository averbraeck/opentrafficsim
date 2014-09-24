package org.opentrafficsim.core.value.vdouble.vector;

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

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.jet.math.tdouble.DoubleFunctions;

/**
 * Mutable double vector.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 28, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit of this MutableDoubleVector
 */
public abstract class MutableDoubleVector<U extends Unit<U>> extends DoubleVector<U> implements WriteDoubleVectorFunctions<U>,
        DoubleMathFunctions
{
    /** */
    private static final long serialVersionUID = 20130903L;

    /**
     * Create a new MutableDoubleVector.
     * @param unit Unit; the unit of the new Mutable DoubleVector
     */
    protected MutableDoubleVector(final U unit)
    {
        super(unit);
        // System.out.println("Created MutableDoubleVector");
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
        for (int i = 0; i < size(); i++)
        {
            safeSet(i, safeGet(i) / sum);
        }
    }

    /**
     * @param <U> Unit
     */
    public abstract static class Abs<U extends Unit<U>> extends MutableDoubleVector<U> implements Absolute
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a new Absolute MutableDoubleVector.
         * @param unit Unit; the unit of the new MutableDoubleVector
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
             * @param values DoubleMatrix1D; the initial values for the entries of the new MutableDoubleMatrix
             * @param unit Unit; the unit of the new MutableDoubleMatrix
             */
            protected Dense(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /**
             * Create a new Absolute Dense MutableDoubleVector.
             * @param values double[]; the initial values for the entries of the new MutableDoubleVector
             * @param unit Unit; the unit of the values for the new MutableDoubleVector
             */
            public Dense(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Create a new Absolute Dense MutableDoubleVector.
             * @param values DoubleScalar.Abs[]; the initial values for the entries of the new MutableDoubleVector
             * @throws ValueException when values is not rectangular or contains zero entries
             */
            public Dense(final DoubleScalar.Abs<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleVector.Abs.Dense<U> immutable()
            {
                setCopyOnWrite(true);
                return new DoubleVector.Abs.Dense<U>(getVectorSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleVector.Abs.Dense<U> mutable()
            {
                setCopyOnWrite(true);
                return new MutableDoubleVector.Abs.Dense<U>(getVectorSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final DoubleMatrix1D createMatrix1D(final int size)
            {
                return new DenseDoubleMatrix1D(size);
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
             * @param values DoubleMatrix1D; the values for the new MutableDoubleVector
             * @param unit Unit; the unit of the new MutableDoubleVector
             */
            protected Sparse(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Rel");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /**
             * Create a new Relative Sparse MutableDoubleVector.
             * @param values double[][]; initial values for the new MutableDoubleVector
             * @param unit Unit; the unit of the values for the new MutableDoubleVector
             */
            public Sparse(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Rel");
                initialize(values);
            }

            /**
             * Create a new Absolute Sparse MutableDoubleVector.
             * @param values DoubleScalar.Rel[]; the initial values for the new MutableDoubleVector
             * @throws ValueException when values contains zero entries
             */
            public Sparse(final DoubleScalar.Rel<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Rel");
                initialize(values);
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleVector.Abs.Sparse<U> immutable()
            {
                setCopyOnWrite(true);
                return new DoubleVector.Abs.Sparse<U>(getVectorSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleVector.Abs.Sparse<U> mutable()
            {
                setCopyOnWrite(true);
                return new MutableDoubleVector.Abs.Sparse<U>(getVectorSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final DoubleMatrix1D createMatrix1D(final int size)
            {
                return new DenseDoubleMatrix1D(size);
            }

        }

        /** {@inheritDoc} */
        @Override
        public final DoubleScalar.Abs<U> get(final int index) throws ValueException
        {
            return new DoubleScalar.Abs<U>(getInUnit(index, getUnit()), getUnit());
        }

    }

    /**
     * @param <U> Unit
     */
    public abstract static class Rel<U extends Unit<U>> extends MutableDoubleVector<U> implements Relative
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a Relative MutableDoubleVector.
         * @param unit Unit; the unit of the new MutableDoubleVector
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
             * @param values DoubleMatrix1D; the initial values for the new MutableDoubleVector
             * @param unit Unit; the unit of the new MutableDoubleVector
             */
            protected Dense(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /**
             * Create a new Relative Dense MutableDoubleVector.
             * @param values double[]; the initial values for the new MutableDoubleVector
             * @param unit Unit; the unit of the new MutableDoubleVector
             */
            public Dense(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Create a new Relative Dense MutableDoubleVector.
             * @param values DoubleScalar.Abs[]; the initial values for the new MutableDoubleVector
             * @throws ValueException when values has zero entries
             */
            public Dense(final DoubleScalar.Abs<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleVector.Rel.Dense<U> immutable()
            {
                setCopyOnWrite(true);
                return new DoubleVector.Rel.Dense<U>(getVectorSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleVector.Rel.Dense<U> mutable()
            {
                setCopyOnWrite(true);
                return new MutableDoubleVector.Rel.Dense<U>(getVectorSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final DoubleMatrix1D createMatrix1D(final int size)
            {
                return new SparseDoubleMatrix1D(size);
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
             * @param values DoubleMatrix1D; the initial values for the new MutableDoubleVector
             * @param unit Unit; the unit of the new MutableDoubleVector
             */
            protected Sparse(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /**
             * Create a new Relative Sparse MutableDoubleVector.
             * @param values double[]; the initial values for the new MutableDoubleVector
             * @param unit Unit; the unit of the new MutableDoubleVector
             */
            public Sparse(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Create a new Relative Sparse MutableDoubleVector.
             * @param values DoubleScalar.Rel[]; the initial values for the new MutableDoubleVector
             * @throws ValueException when values has zero entries
             */
            public Sparse(final DoubleScalar.Rel<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /** {@inheritDoc} */
            @Override
            public final DoubleVector.Rel.Sparse<U> immutable()
            {
                setCopyOnWrite(true);
                return new DoubleVector.Rel.Sparse<U>(getVectorSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableDoubleVector.Rel.Sparse<U> mutable()
            {
                setCopyOnWrite(true);
                return new MutableDoubleVector.Rel.Sparse<U>(getVectorSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            protected final DoubleMatrix1D createMatrix1D(final int size)
            {
                return new SparseDoubleMatrix1D(size);
            }

        }

        /** {@inheritDoc} */
        @Override
        public final DoubleScalar.Rel<U> get(final int index) throws ValueException
        {
            return new DoubleScalar.Rel<U>(getInUnit(index, getUnit()), getUnit());
        }

    }

    /**
     * Make (immutable) DoubleVector equivalent for any type of MutableDoubleVector.
     * @return DoubleVector
     */
    public abstract DoubleVector<U> immutable();

    /** {@inheritDoc} */
    @Override
    public final MutableDoubleVector<U> copy()
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
            setCopyOnWrite(false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void setSI(final int index, final double valueSI) throws ValueException
    {
        checkIndex(index);
        checkCopyOnWrite();
        safeSet(index, valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void set(final int index, final DoubleScalar<U> value) throws ValueException
    {
        setSI(index, value.getValueSI());
    }

    /** {@inheritDoc} */
    @Override
    public final void setInUnit(final int index, final double value, final U valueUnit) throws ValueException
    {
        // TODO: creating a DoubleScalarAbs along the way may not be the most efficient way to do this...
        setSI(index, new DoubleScalar.Abs<U>(value, valueUnit).getValueSI());
    }

    /**
     * Execute a function on a cell by cell basis.
     * @param f cern.colt.function.tdouble.DoubleFunction; the function to apply
     */
    public final void assign(final cern.colt.function.tdouble.DoubleFunction f)
    {
        checkCopyOnWrite();
        getVectorSI().assign(f);
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
     * Increment the values in this MutableDoubleVector by the corresponding values in a DoubleVector.
     * @param increment DoubleVector; contains the amounts by which to increment the corresponding entries in this
     *            MutableDoubleVector
     * @return this
     * @throws ValueException when the vectors do not have the same size
     */
    private MutableDoubleVector<U> incrementValueByValue(final DoubleVector<U> increment) throws ValueException
    {
        checkSizeAndCopyOnWrite(increment);
        for (int index = this.size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) + increment.safeGet(index));
        }
        return this;
    }

    /**
     * Increment the entries in this MutableDoubleVector by the corresponding values in a Relative DoubleVector.
     * @param rel DoubleVector.Rel; the other DoubleVector
     * @return this
     * @throws ValueException when the vectors do not have the same size
     */
    public final MutableDoubleVector<U> incrementBy(final DoubleVector.Rel<U> rel) throws ValueException
    {
        return incrementValueByValue(rel);
    }

    /**
     * Decrement the values in this MutableDoubleVector by the corresponding values in a DoubleVector.
     * @param decrement DoubleVector; contains the amounts by which to decrement the corresponding entries in this
     *            MutableDoubleVector
     * @return this
     * @throws ValueException when the vectors do not have the same size
     */
    private MutableDoubleVector<U> decrementValueByValue(final DoubleVector<U> decrement) throws ValueException
    {
        checkSizeAndCopyOnWrite(decrement);
        for (int index = this.size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) - decrement.safeGet(index));
        }
        return this;
    }

    /**
     * Decrement the entries in this MutableDoubleVector by the corresponding values in a Relative DoubleVector.
     * @param rel DoubleVector.Rel; the Relative DoubleVector
     * @return this
     * @throws ValueException when the vectors do not have the same size
     */
    public final MutableDoubleVector<U> decrementBy(final DoubleVector.Rel<U> rel) throws ValueException
    {
        return decrementValueByValue(rel);
    }

    /**
     * Decrement the entries in this MutableDoubleVector by the corresponding values in a Absolute DoubleVector.
     * @param abs DoubleVector.Abs; the Absolute DoubleVector
     * @return this
     * @throws ValueException when the vectors do not have the same size
     */
    public final MutableDoubleVector<U> decrementBy(final DoubleVector.Abs<U> abs) throws ValueException
    {
        return decrementValueByValue(abs);
    }

    /**
     * Scale the values in this MutableDoubleVector by the corresponding values in a DoubleVector.
     * @param factor DoubleVector; contains the values by which to scale the corresponding entries in this MutableDoubleVector
     * @throws ValueException when the vectors do not have the same size
     */
    public final void scaleValueByValue(final DoubleVector<?> factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int index = this.size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) * factor.safeGet(index));
        }
    }

    /**
     * Scale the values in this MutableDoubleVector by the corresponding values in a double array.
     * @param factor double[]; contains the values by which to scale the corresponding entries in this MutableDoubleVector
     * @return this
     * @throws ValueException when the vectors do not have the same size
     */
    public final MutableDoubleVector<U> scaleValueByValue(final double[] factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int index = this.size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) * factor[index]);
        }
        return this;
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other AbstractDoubleVector; partner for the size check
     * @throws ValueException when the vectors do not have the same size
     */
    private void checkSizeAndCopyOnWrite(final DoubleVector<?> other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other double[]; partner for the size check
     * @throws ValueException when the vectors do not have the same size
     */
    private void checkSizeAndCopyOnWrite(final double[] other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Add two DoubleVectors entry by entry.
     * @param left Absolute Dense DoubleVector
     * @param right Relative DoubleVector
     * @return new Absolute Dense Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> plus(final DoubleVector.Abs.Dense<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleVectors entry by entry.
     * @param left Absolute Sparse DoubleVector
     * @param right Relative Dense DoubleVector
     * @return new Absolute Dense Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> plus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Add two DoubleVectors entry by entry.
     * @param left Absolute Sparse DoubleVector
     * @param right Relative DoubleVector
     * @return new Absolute Sparse Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Sparse<U> plus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleVectors entry by entry.
     * @param left Relative Dense DoubleVector
     * @param right Relative DoubleVector
     * @return new Absolute Dense Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> plus(final DoubleVector.Rel.Dense<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleVectors entry by entry.
     * @param left Relative Sparse DoubleVector
     * @param right Relative DoubleVector
     * @return new Relative Sparse Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> plus(final DoubleVector.Rel.Sparse<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Subtract two DoubleVectors entry by entry.
     * @param left Absolute Dense DoubleVector
     * @param right Absolute DoubleVector
     * @return new Relative Dense Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> minus(final DoubleVector.Abs.Dense<U> left,
            final DoubleVector.Abs<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) new MutableDoubleVector.Rel.Dense<U>(left.deepCopyOfData(), left.getUnit())
                .decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors entry by entry.
     * @param left Absolute Sparse DoubleVector
     * @param right Absolute DoubleVector
     * @return new Relative Sparse Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> minus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Abs<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Sparse<U>) new MutableDoubleVector.Rel.Sparse<U>(left.deepCopyOfData(), left.getUnit())
                .decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors entry by entry.
     * @param left Absolute Dense DoubleVector
     * @param right Relative DoubleVector
     * @return new Relative Dense Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> minus(final DoubleVector.Abs.Dense<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors entry by entry.
     * @param left Absolute Sparse DoubleVector
     * @param right Relative DoubleVector
     * @return new Absolute Sparse Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Sparse<U> minus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors entry by entry.
     * @param left Relative Dense DoubleVector
     * @param right Relative DoubleVector
     * @return new Relative Dense Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> minus(final DoubleVector.Rel.Dense<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors entry by entry.
     * @param left Relative Sparse DoubleVector
     * @param right Relative DoubleVector
     * @return new Relative Sparse Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> minus(final DoubleVector.Rel.Sparse<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Multiply two DoubleVectors entry by entry.
     * @param left Absolute Dense DoubleVector
     * @param right Absolute DoubleVector
     * @return new Absolute Dense Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     */
    public static MutableDoubleVector.Abs.Dense<SIUnit> times(final DoubleVector.Abs.Dense<?> left,
            final DoubleVector.Abs<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableDoubleVector.Abs.Dense<SIUnit> work =
                new MutableDoubleVector.Abs.Dense<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two DoubleVectors entry by entry.
     * @param left Relative Dense DoubleVector
     * @param right Relative DoubleVector
     * @return new Relative Dense Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     */
    public static MutableDoubleVector.Rel.Dense<SIUnit> times(final DoubleVector.Rel.Dense<?> left,
            final DoubleVector.Rel<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableDoubleVector.Rel.Dense<SIUnit> work =
                new MutableDoubleVector.Rel.Dense<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two DoubleVectors entry by entry.
     * @param left Absolute Sparse DoubleVector
     * @param right Absolute DoubleVector
     * @return new XAbsolute Sparse Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     */
    public static MutableDoubleVector.Abs.Sparse<SIUnit> times(final DoubleVector.Abs.Sparse<?> left,
            final DoubleVector.Abs<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableDoubleVector.Abs.Sparse<SIUnit> work =
                new MutableDoubleVector.Abs.Sparse<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two DoubleVectors entry by entry.
     * @param left Relative Sparse DoubleVector
     * @param right Relative DoubleVector
     * @return new Relative Sparse Mutable DoubleVector
     * @throws ValueException when the vectors do not have the same size
     */
    public static MutableDoubleVector.Rel.Sparse<SIUnit> times(final DoubleVector.Rel.Sparse<?> left,
            final DoubleVector.Rel<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableDoubleVector.Rel.Sparse<SIUnit> work =
                new MutableDoubleVector.Rel.Sparse<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply the values in a DoubleVector by the corresponding values in a double array.
     * @param left Absolute Dense DoubleVector
     * @param right double[]; the double array
     * @return new Dense Absolute Mutable DoubleVector
     * @throws ValueException when the DoubleVector and the array do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> times(final DoubleVector.Abs.Dense<U> left,
            final double[] right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleVector by the corresponding values in a double array.
     * @param left Relative Dense DoubleVector
     * @param right double[]; the double array
     * @return new Relative Dense Mutable DoubleVector
     * @throws ValueException when the DoubleVector and the array do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> times(final DoubleVector.Rel.Dense<U> left,
            final double[] right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleVector by the corresponding values in a double array.
     * @param left Absolute Sparse DoubleVector
     * @param right double[]; the double array
     * @return new Absolute Sparse Mutable DoubleVector
     * @throws ValueException when the DoubleVector and the array do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Sparse<U> times(final DoubleVector.Abs.Sparse<U> left,
            final double[] right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleVector by the corresponding values in a double array.
     * @param left Relative Sparse DoubleVector
     * @param right double[]; the double array
     * @return new Relative Sparse Mutable DoubleVector
     * @throws ValueException when the DoubleVector and the array do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> times(final DoubleVector.Rel.Sparse<U> left,
            final double[] right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Make the Sparse equivalent of a DenseDoubleMatrix1D.
     * @param dense DenseDoubleMatrix1D
     * @return SparseDoubleMatrix1D
     */
    private static DoubleMatrix1D makeSparse(final DoubleMatrix1D dense)
    {
        DoubleMatrix1D result = new SparseDoubleMatrix1D((int) dense.size());
        result.assign(dense);
        return result;
    }

    /**
     * Create a Sparse version of this Dense DoubleVector. <br>
     * @param in DoubleVector.Abs.Dense the Dense DoubleVector
     * @return MutableDoubleVector.Abs.Sparse
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Sparse<U> denseToSparse(final DoubleVector.Abs.Dense<U> in)
    {
        return new MutableDoubleVector.Abs.Sparse<U>(makeSparse(in.getVectorSI()), in.getUnit());
    }

    /**
     * Create a Sparse version of this Dense DoubleVector. <br>
     * @param in DoubleVector.Rel.Dense the Dense DoubleVector
     * @return MutableDoubleVector.Rel.Sparse
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> denseToSparse(final DoubleVector.Rel.Dense<U> in)
    {
        return new MutableDoubleVector.Rel.Sparse<U>(makeSparse(in.getVectorSI()), in.getUnit());
    }

    /**
     * Make the Dense equivalent of a SparseDoubleMatrix1D.
     * @param sparse SparseDoubleMatrix1D
     * @return DenseDoubleMatrix1D
     */
    private static DoubleMatrix1D makeDense(final DoubleMatrix1D sparse)
    {
        DoubleMatrix1D result = new SparseDoubleMatrix1D((int) sparse.size());
        result.assign(sparse);
        return result;
    }

    /**
     * Create a Dense version of this Sparse DoubleVector. <br>
     * @param in DoubleVector.Abs.Dense the Dense DoubleVector
     * @return MutableDoubleVector.Abs.Sparse
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> sparseToDense(final DoubleVector.Abs.Sparse<U> in)
    {
        return new MutableDoubleVector.Abs.Dense<U>(makeDense(in.getVectorSI()), in.getUnit());
    }

    /**
     * Create a Dense version of this Sparse DoubleVector. <br>
     * @param in DoubleVector.Rel.Dense the Dense DoubleVector
     * @return MutableDoubleVector.Rel.Sparse
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> sparseToDense(final DoubleVector.Rel.Sparse<U> in)
    {
        return new MutableDoubleVector.Rel.Dense<U>(makeDense(in.getVectorSI()), in.getUnit());
    }

}

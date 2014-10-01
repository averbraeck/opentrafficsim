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
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 28, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit of this MutableDoubleVector
 */
public abstract class MutableDoubleVector<U extends Unit<U>> extends DoubleVector<U> implements
        WriteDoubleVectorFunctions<U>, DoubleMathFunctions
{
    /** */
    private static final long serialVersionUID = 20130903L;

    /**
     * Construct a new MutableDoubleVector.
     * @param unit U; the unit of the new MutableDoubleVector
     */
    protected MutableDoubleVector(final U unit)
    {
        super(unit);
        // System.out.println("Created MutableDoubleVector");
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
         * Construct a new Absolute MutableDoubleVector.
         * @param unit U; the unit of the new Absolute MutableDoubleVector
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
             * Construct a new Absolute Dense MutableDoubleVector.
             * @param values double[]; the initial values of the entries in the new Absolute Dense MutableDoubleVector
             * @param unit U; the unit of the new Absolute Dense MutableDoubleVector
             */
            public Dense(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Construct a new Absolute Dense MutableDoubleVector.
             * @param values DoubleScalar.Abs&lt;U&gt;[]; the initial values of the entries in the new Absolute Dense
             *            MutableDoubleVector
             * @throws ValueException when values has zero entries
             */
            public Dense(final DoubleScalar.Abs<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values DoubleMatrix1D; the initial values of the entries in the new Absolute Dense
             *            MutableDoubleVector
             * @param unit U; the unit of the new Absolute Dense MutableDoubleVector
             */
            protected Dense(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
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
             * Construct a new Absolute Sparse MutableDoubleVector.
             * @param values double[]; the initial values of the entries in the new Absolute Sparse MutableDoubleVector
             * @param unit U; the unit of the new Absolute Sparse MutableDoubleVector
             */
            public Sparse(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Construct a new Absolute Sparse MutableDoubleVector.
             * @param values DoubleScalar.Abs&lt;U&gt;[]; the initial values of the entries in the new Absolute Sparse
             *            MutableDoubleVector
             * @throws ValueException when values has zero entries
             */
            public Sparse(final DoubleScalar.Abs<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values DoubleMatrix1D; the initial values of the entries in the new Absolute Sparse
             *            MutableDoubleVector
             * @param unit U; the unit of the new Absolute Sparse MutableDoubleVector
             */
            protected Sparse(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
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
                return new SparseDoubleMatrix1D(size);
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
         * Construct a new Relative MutableDoubleVector.
         * @param unit U; the unit of the new Relative MutableDoubleVector
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
             * Construct a new Relative Dense MutableDoubleVector.
             * @param values double[]; the initial values of the entries in the new Relative Dense MutableDoubleVector
             * @param unit U; the unit of the new Relative Dense MutableDoubleVector
             */
            public Dense(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Construct a new Relative Dense MutableDoubleVector.
             * @param values DoubleScalar.Rel&lt;U&gt;[]; the initial values of the entries in the new Relative Dense
             *            MutableDoubleVector
             * @throws ValueException when values has zero entries
             */
            public Dense(final DoubleScalar.Rel<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values DoubleMatrix1D; the initial values of the entries in the new Relative Dense
             *            MutableDoubleVector
             * @param unit U; the unit of the new Relative Dense MutableDoubleVector
             */
            protected Dense(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
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
                return new DenseDoubleMatrix1D(size);
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
             * Construct a new Relative Sparse MutableDoubleVector.
             * @param values double[]; the initial values of the entries in the new Relative Sparse MutableDoubleVector
             * @param unit U; the unit of the new Relative Sparse MutableDoubleVector
             */
            public Sparse(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Construct a new Relative Sparse MutableDoubleVector.
             * @param values DoubleScalar.Rel&lt;U&gt;[]; the initial values of the entries in the new Relative Sparse
             *            MutableDoubleVector
             * @throws ValueException when values has zero entries
             */
            public Sparse(final DoubleScalar.Rel<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values DoubleMatrix1D; the initial values of the entries in the new Relative Sparse
             *            MutableDoubleVector
             * @param unit U; the unit of the new Relative Sparse MutableDoubleVector
             */
            protected Sparse(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
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
     * @return DoubleVector&lt;U&gt;
     */
    public abstract DoubleVector<U> immutable();

    /** {@inheritDoc} */
    @Override
    public final MutableDoubleVector<U> copy()
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
        // TODO: creating a DoubleScalar.Abs along the way may not be the most efficient way to do this...
        setSI(index, new DoubleScalar.Abs<U>(value, valueUnit).getValueSI());
    }

    /**
     * Execute a function on a cell by cell basis.
     * @param d cern.colt.function.tdouble.DoubleFunction; the function to apply
     */
    public final void assign(final cern.colt.function.tdouble.DoubleFunction d)
    {
        checkCopyOnWrite();
        getVectorSI().assign(d);
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
     * @param increment DoubleVector&lt;U&gt;; the values by which to increment the corresponding values in this
     *            MutableDoubleVector
     * @return MutableDoubleVEctor&lt;U&gt;; this modified MutableDoubleVector
     * @throws ValueException when the vectors do not have the same size
     */
    private MutableDoubleVector<U> incrementValueByValue(final DoubleVector<U> increment) throws ValueException
    {
        checkSizeAndCopyOnWrite(increment);
        for (int index = size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) + increment.safeGet(index));
        }
        return this;
    }

    /**
     * Increment the values in this MutableDoubleVector by the corresponding values in a Relative DoubleVector.
     * @param rel DoubleVector.Rel&lt;U&gt;; the Relative DoubleVector
     * @return MutableDoubleVector&lt;U&gt;; this modified MutableDoubleVector
     * @throws ValueException when the vectors do not have the same size
     */
    public final MutableDoubleVector<U> incrementBy(final DoubleVector.Rel<U> rel) throws ValueException
    {
        return incrementValueByValue(rel);
    }

    /**
     * Decrement the values in this MutableDoubleVector by the corresponding values in a DoubleVector.
     * @param decrement DoubleVector&lt;U&gt;; contains the amounts by which to decrement the corresponding values in
     *            this MutableDoubleVector
     * @return MutableDoubleVector&lt;U&gt;; this modified MutableDoubleVector
     * @throws ValueException when the vectors do not have the same size
     */
    private MutableDoubleVector<U> decrementValueByValue(final DoubleVector<U> decrement) throws ValueException
    {
        checkSizeAndCopyOnWrite(decrement);
        for (int index = size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) - decrement.safeGet(index));
        }
        return this;
    }

    /**
     * Decrement the values in this MutableDoubleVector by the corresponding values in a Relative DoubleVector.
     * @param rel DoubleVector.Rel&lt;U&gt;; the Relative DoubleVector
     * @return MutableDoubleVector&lt;U&gt;; this modified MutableDoubleVector
     * @throws ValueException when the vectors do not have the same size
     */
    public final MutableDoubleVector<U> decrementBy(final DoubleVector.Rel<U> rel) throws ValueException
    {
        return decrementValueByValue(rel);
    }

    /**
     * Decrement the values in this Relative MutableDoubleVector by the corresponding values in an Absolute
     * DoubleVector.
     * @param abs DoubleVector.Abs&lt;U&gt;; the Absolute DoubleVector
     * @return MutableDoubleVector.Rel&lt;U&gt;; this modified Relative MutableDoubleVector
     * @throws ValueException when the vectors do not have the same size
     */
    protected final MutableDoubleVector.Rel<U> decrementBy(final DoubleVector.Abs<U> abs) throws ValueException
    {
        return (MutableDoubleVector.Rel<U>) decrementValueByValue(abs);
    }

    /**
     * Scale the values in this MutableDoubleVector by the corresponding values in a DoubleVector.
     * @param factor DoubleVector&lt;?&gt;; contains the values by which to scale the corresponding values in this
     *            MutableDoubleVector
     * @throws ValueException when the vectors do not have the same size
     */
    public final void scaleValueByValue(final DoubleVector<?> factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int index = size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) * factor.safeGet(index));
        }
    }

    /**
     * Scale the values in this MutableDoubleVector by the corresponding values in a double array.
     * @param factor double[]; contains the values by which to scale the corresponding values in this
     *            MutableDoubleVector
     * @return MutableDoubleVector&lt;U&gt;; this modified MutableDoubleVector
     * @throws ValueException when the vector and the array do not have the same size
     */
    public final MutableDoubleVector<U> scaleValueByValue(final double[] factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int index = size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) * factor[index]);
        }
        return this;
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other DoubleVector&lt;?&gt;; partner for the size check
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
     * Add two DoubleVectors value by value and store the result in a new MutableDoubleVector.Abs.Dense&lt;U&gt;.
     * @param left DoubleVector.Abs.Dense&lt;U&gt;; the left operand
     * @param right DoubleVector.Rel&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Abs.Dense&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> plus(final DoubleVector.Abs.Dense<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleVectors value by value and store the result in a new MutableDoubleVector.Abs.Dense&lt;U&gt;.
     * @param left DoubleVector.Abs.Sparse&lt;U&gt;; the left operand
     * @param right DoubleVector.Rel.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Abs.Dense&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> plus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Add two DoubleVectors value by value and store the result in a new MutableDoubleVector.Abs.Sparse&lt;U&gt;.
     * @param left DoubleVector.Abs.Sparse&lt;U&gt;; the left operand
     * @param right DoubleVector.Rel.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Abs.Sparse&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Sparse<U> plus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleVectors value by value and store the result in a new MutableDoubleVector.Rel.Dense&lt;U&gt;.
     * @param left DoubleVector.Rel.Dense&lt;U&gt;; the left operand
     * @param right DoubleVector.Rel&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Rel.Dense&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> plus(final DoubleVector.Rel.Dense<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleVectors value by value and store the result in a new MutableDoubleVector.Rel.Dense&lt;U&gt;.
     * @param left DoubleVector.Rel.Sparse&lt;U&gt;; the left operand
     * @param right DoubleVector.Rel.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Rel.Dense&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> plus(final DoubleVector.Rel.Sparse<U> left,
            final DoubleVector.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Add two DoubleVectors value by value and store the result in a new MutableDoubleVector.Rel.Sparse&lt;U&gt;.
     * @param left DoubleVector.Rel.Sparse&lt;U&gt;; the left operand
     * @param right DoubleVector.Rel.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Rel.Sparse&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> plus(final DoubleVector.Rel.Sparse<U> left,
            final DoubleVector.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Subtract two DoubleVectors value by value and store the result in a new MutableDoubleVector.Rel.Dense&lt;U&gt;.
     * @param left DoubleVector.Abs.Dense&lt;U&gt;; the left operand
     * @param right DoubleVector.Abs&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Rel.Dense&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> minus(final DoubleVector.Abs.Dense<U> left,
            final DoubleVector.Abs<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) new MutableDoubleVector.Rel.Dense<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors value by value and store the result in a new MutableDoubleVector.Rel.Sparse&lt;U&gt;.
     * @param left DoubleVector.Abs.Sparse&lt;U&gt;; the left operand
     * @param right DoubleVector.Abs.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Rel.Sparse&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> minus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Abs.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Sparse<U>) new MutableDoubleVector.Rel.Sparse<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors value by value and store the result in a new MutableDoubleVector.Rel.Dense&lt;U&gt;.
     * @param left DoubleVector.Abs.Sparse&lt;U&gt;; the left operand
     * @param right DoubleVector.Abs.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Rel.Dense&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> minus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Abs.Dense<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) new MutableDoubleVector.Rel.Dense<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors value by value and store the result in a new MutableDoubleVector.Abs.Dense&lt;U&gt;.
     * @param left DoubleVector.Abs.Dense&lt;U&gt;; the left operand
     * @param right DoubleVector.Rel&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Abs.Dense&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> minus(final DoubleVector.Abs.Dense<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors value by value and store the result in a new MutableDoubleVector.Abs.Dense&lt;U&gt;.
     * @param left DoubleVector.Abs.Sparse&lt;U&gt;; the left operand
     * @param right DoubleVector.Rel.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Abs.Dense&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> minus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Dense<U>) sparseToDense(left).decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors value by value and store the result in a new MutableDoubleVector.Abs.Sparse&lt;U&gt;.
     * @param left DoubleVector.Abs.Sparse&lt;U&gt;; the left operand
     * @param right DoubleVector.Rel.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Abs.Sparse&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Sparse<U> minus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors value by value and store the result in a new MutableDoubleVector.Rel.Dense&lt;U&gt;.
     * @param left DoubleVector.Rel.Dense&lt;U&gt;; the left operand
     * @param right DoubleVector.Rel&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Rel.Dense&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> minus(final DoubleVector.Rel.Dense<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors value by value and store the result in a new MutableDoubleVector.Rel.Dense&lt;U&gt;.
     * @param left DoubleVector.Rel.Sparse&lt;U&gt;; the left operand
     * @param right DoubleVector.Rel.Dense&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Rel.Dense&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> minus(final DoubleVector.Rel.Sparse<U> left,
            final DoubleVector.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) sparseToDense(left).decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors value by value and store the result in a new MutableDoubleVector.Rel.Sparse&lt;U&gt;.
     * @param left DoubleVector.Rel.Sparse&lt;U&gt;; the left operand
     * @param right DoubleVector.Rel.Sparse&lt;U&gt;; the right operand
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleVector.Rel.Sparse&lt;U&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> minus(final DoubleVector.Rel.Sparse<U> left,
            final DoubleVector.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Multiply two DoubleVectors value by value and store the result in a new
     * MutableDoubleVector.Abs.Dense&lt;SIUnit&gt;.
     * @param left DoubleVector.Abs.Dense&lt;?&gt;; the left operand
     * @param right DoubleVector.Abs.Dense&lt;?&gt;; the right operand
     * @return MutableDoubleVector.Abs.Dense&lt;SIUnit&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static MutableDoubleVector.Abs.Dense<SIUnit> times(final DoubleVector.Abs.Dense<?> left,
            final DoubleVector.Abs.Dense<?> right) throws ValueException
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
     * Multiply two DoubleVectors value by value and store the result in a new
     * MutableDoubleVector.Abs.Sparse&lt;SIUnit&gt;.
     * @param left DoubleVector.Abs.Dense&lt;?&gt;; the left operand
     * @param right DoubleVector.Abs.Sparse&lt;?&gt;; the right operand
     * @return MutableDoubleVector.Abs.Sparse&lt;SIUnit&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static MutableDoubleVector.Abs.Sparse<SIUnit> times(final DoubleVector.Abs.Dense<?> left,
            final DoubleVector.Abs.Sparse<?> right) throws ValueException
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
     * Multiply two DoubleVectors value by value and store the result in a new
     * MutableDoubleVector.Abs.Sparse&lt;SIUnit&gt;.
     * @param left DoubleVector.Abs.Sparse&lt;?&gt;; the left operand
     * @param right DoubleVector.Abs&lt;?&gt;; the right operand
     * @return MutableDoubleVector.Abs.Sparse&lt;SIUnit&gt;
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
     * Multiply two DoubleVectors value by value and store the result in a new
     * MutableDoubleVector.Rel.Dense&lt;SIUnit&gt;.
     * @param left DoubleVector.Rel.Dense&lt;?&gt;; the left operand
     * @param right DoubleVector.Rel.Dense&lt;?&gt;; the right operand
     * @return MutableDoubleVector.Rel.Dense&lt;SIUnit&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static MutableDoubleVector.Rel.Dense<SIUnit> times(final DoubleVector.Rel.Dense<?> left,
            final DoubleVector.Rel.Dense<?> right) throws ValueException
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
     * Multiply two DoubleVectors value by value and store the result in a new
     * MutableDoubleVector.Rel.Sparse&lt;SIUnit&gt;.
     * @param left DoubleVector.Rel.Dense&lt;?&gt;; the left operand
     * @param right DoubleVector.Rel.Sparse&lt;?&gt;; the right operand
     * @return MutableDoubleVector.Rel.Sparse&lt;SIUnit&gt;
     * @throws ValueException when the vectors do not have the same size
     */
    public static MutableDoubleVector.Rel.Sparse<SIUnit> times(final DoubleVector.Rel.Dense<?> left,
            final DoubleVector.Rel.Sparse<?> right) throws ValueException
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
     * Multiply two DoubleVectors value by value and store the result in a new
     * MutableDoubleVector.Rel.Sparse&lt;SIUnit&gt;.
     * @param left DoubleVector.Rel.Sparse&lt;?&gt;; the left operand
     * @param right DoubleVector.Rel&lt;?&gt;; the right operand
     * @return MutableDoubleVector.Rel.Sparse&lt;SIUnit&gt;
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
     * Multiply the values in a DoubleVector and a double array value by value and store the result in a new
     * MutableDoubleVector.Abs.Dense&lt;U&gt;.
     * @param left DoubleVector.Abs.Dense&lt;U&gt;; the DoubleVector
     * @param right double[]; the double array
     * @param <U> Unit; the unit of the left parameter and the result
     * @return MutableDoubleVector.Abs.Dense&lt;U&gt;
     * @throws ValueException when the DoubleVector and the array do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> times(final DoubleVector.Abs.Dense<U> left,
            final double[] right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleVector and a double array value by value and store the result in a new
     * MutableDoubleVector.Abs.Sparse&lt;U&gt;.
     * @param left DoubleVector.Abs.Sparse&lt;U&gt;; the DoubleVector
     * @param right double[]; the double array
     * @param <U> Unit; the unit of the left parameter and the result
     * @return MutableDoubleVector.Abs.Sparse&lt;U&gt;
     * @throws ValueException when the DoubleVector and the array do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Sparse<U> times(final DoubleVector.Abs.Sparse<U> left,
            final double[] right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleVector and a double array value by value and store the result in a new
     * MutableDoubleVector.Rel.Dense&lt;U&gt;.
     * @param left DoubleVector.Rel.Dense&lt;U&gt;; the DoubleVector
     * @param right double[]; the double array
     * @param <U> Unit; the unit of the left parameter and the result
     * @return MutableDoubleVector.Rel.Dense&lt;U&gt;
     * @throws ValueException when the DoubleVector and the array do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> times(final DoubleVector.Rel.Dense<U> left,
            final double[] right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleVector and a double array value by value and store the result in a new
     * MutableDoubleVector.Rel.Sparse&lt;U&gt;.
     * @param left DoubleVector.Rel.Sparse&lt;U&gt;; the DoubleVector
     * @param right double[]; the double array
     * @param <U> Unit; the unit of the left parameter and the result
     * @return MutableDoubleVector.Rel.Sparse&lt;U&gt;
     * @throws ValueException when the DoubleVector and the array do not have the same size
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> times(final DoubleVector.Rel.Sparse<U> left,
            final double[] right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Make the Sparse equivalent of a DenseDoubleMatrix1D.
     * @param dense DoubleMatrix1D; the Dense DoubleMatrix1D
     * @return SparseDoubleMatrix1D
     */
    private static SparseDoubleMatrix1D makeSparse(final DoubleMatrix1D dense)
    {
        SparseDoubleMatrix1D result = new SparseDoubleMatrix1D((int) dense.size());
        result.assign(dense);
        return result;
    }

    /**
     * Create a Sparse version of this Dense DoubleVector.
     * @param in DoubleVector.Abs.Dense&lt;U&gt;; the Dense DoubleVector
     * @param <U> Unit; the unit of the parameter and the result
     * @return MutableDoubleVector.Abs.Sparse&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Sparse<U> denseToSparse(final DoubleVector.Abs.Dense<U> in)
    {
        return new MutableDoubleVector.Abs.Sparse<U>(makeSparse(in.getVectorSI()), in.getUnit());
    }

    /**
     * Create a Sparse version of this Dense DoubleVector.
     * @param in DoubleVector.Rel.Dense&lt;U&gt;; the Dense DoubleVector
     * @param <U> Unit; the unit of the parameter and the result
     * @return MutableDoubleVector.Rel.Sparse&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> denseToSparse(final DoubleVector.Rel.Dense<U> in)
    {
        return new MutableDoubleVector.Rel.Sparse<U>(makeSparse(in.getVectorSI()), in.getUnit());
    }

    /**
     * Make the Dense equivalent of a SparseDoubleMatrix1D.
     * @param sparse DoubleMatrix1D; the Sparse DoubleMatrix1D
     * @return DenseDoubleMatrix1D
     */
    private static DenseDoubleMatrix1D makeDense(final DoubleMatrix1D sparse)
    {
        DenseDoubleMatrix1D result = new DenseDoubleMatrix1D((int) sparse.size());
        result.assign(sparse);
        return result;
    }

    /**
     * Create a Dense version of this Sparse DoubleVector.
     * @param in DoubleVector.Abs.Sparse&lt;U&gt;; the Sparse DoubleVector
     * @param <U> Unit; the unit of the parameter and the result
     * @return MutableDoubleVector.Abs.Dense&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> sparseToDense(final DoubleVector.Abs.Sparse<U> in)
    {
        return new MutableDoubleVector.Abs.Dense<U>(makeDense(in.getVectorSI()), in.getUnit());
    }

    /**
     * Create a Dense version of this Sparse DoubleVector.
     * @param in DoubleVector.Rel.Sparse&lt;U&gt;; the Sparse DoubleVector
     * @param <U> Unit; the unit of the parameter and the result
     * @return MutableDoubleVector.Rel.Dense&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> sparseToDense(final DoubleVector.Rel.Sparse<U> in)
    {
        return new MutableDoubleVector.Rel.Dense<U>(makeDense(in.getVectorSI()), in.getUnit());
    }

}

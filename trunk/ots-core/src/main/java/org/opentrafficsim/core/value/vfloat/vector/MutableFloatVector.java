package org.opentrafficsim.core.value.vfloat.vector;

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

import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix1D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix1D;
import cern.jet.math.tfloat.FloatFunctions;

/**
 * Mutable float vector.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 28, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit of this MutableFloatVector
 */
public abstract class MutableFloatVector<U extends Unit<U>> extends FloatVector<U> implements WriteFloatVectorFunctions<U>,
        FloatMathFunctions
{
    /** */
    private static final long serialVersionUID = 20130903L;

    /**
     * Create a new MutableFloatVector.
     * @param unit Unit; the unit of the new MutableFloatVector
     */
    protected MutableFloatVector(final U unit)
    {
        super(unit);
        // System.out.println("Created MutableFloatVector");
    }

    /** If set, any modification of the data must be preceded by replacing the data with a local copy. */
    boolean copyOnWrite = false;

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
        for (int i = 0; i < this.vectorSI.size(); i++)
        {
            safeSet(i, safeGet(i) / sum);
        }
    }

    /**
     * @param <U> Unit
     */
    public abstract static class Abs<U extends Unit<U>> extends MutableFloatVector<U> implements Absolute
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a new Absolute MutableFloatVector.
         * @param unit Unit; the unit of the new MutableFloatVector
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
             * @param values FloatMatrix1D; the initial values of the entries of the new MutableFloatVector
             * @param unit Unit; the unit of the new MutableFloatVector
             */
            protected Dense(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Absolute Dense MutableFloatVector.
             * @param values float[]; the initial values of the entries of the new MutableFloatVector
             * @param unit Unit; the unit of the values of the new MutableFloatVector
             */
            public Dense(final float[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Create a new Absolute Dense MutableFloatVector.
             * @param values FloatScalar.Abs[]; the initial values of the entries of the new MutableFloatVector
             * @throws ValueException when values has zero entries
             */
            public Dense(final FloatScalar.Abs<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /** {@inheritDoc} */
            @Override
            public final FloatVector.Abs.Dense<U> immutable()
            {
                this.copyOnWrite = true;
                return new FloatVector.Abs.Dense<U>(this.vectorSI, this.unit);
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatVector.Abs.Dense<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableFloatVector.Abs.Dense<U>(this.vectorSI, this.unit);
            }

            /** {@inheritDoc} */
            @Override
            protected final FloatMatrix1D createMatrix1D(final int size)
            {
                return new DenseFloatMatrix1D(size);
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
             * @param values FloatMatrix1D; the initial values for the entries of the new MutableFloatVector
             * @param unit Unit; the unit of the new MutableFloatVector
             */
            protected Sparse(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Relative Sparse MutableFloatVector.
             * @param values float[]; the initial values for the entries of the new MutableFloatVector
             * @param unit Unit; the unit of the new MutableFloatVector
             */
            public Sparse(final float[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Create a new Relative Sparse MutableFloatVector.
             * @param values FloatScalar.Rel[]; the initial values for the
             * @throws ValueException when values contains zero entries
             */
            public Sparse(final FloatScalar.Rel<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /** {@inheritDoc} */
            @Override
            public final FloatVector.Abs.Sparse<U> immutable()
            {
                this.copyOnWrite = true;
                return new FloatVector.Abs.Sparse<U>(this.vectorSI, this.unit);
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatVector.Abs.Sparse<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableFloatVector.Abs.Sparse<U>(this.vectorSI, this.unit);
            }

            /** {@inheritDoc} */
            @Override
            protected final FloatMatrix1D createMatrix1D(final int size)
            {
                return new DenseFloatMatrix1D(size);
            }

        }

        /** {@inheritDoc} */
        @Override
        public final FloatScalar.Abs<U> get(final int index) throws ValueException
        {
            return new FloatScalar.Abs<U>(getInUnit(index, this.unit), this.unit);
        }

    }

    /**
     * @param <U> Unit
     */
    public abstract static class Rel<U extends Unit<U>> extends MutableFloatVector<U> implements Relative
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a Relative MutableFloatVector.
         * @param unit Unit; the unit of the new Relative MutableFloatVector
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
             * @param values FloatMatrix1D; initial values for the entries of the new MutableFloatVector
             * @param unit Unit; the unit of the new MutableFloatVector
             */
            protected Dense(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Relative Dense MutableFloatVector.
             * @param values float[]; the initial values for the entries of the new MutableFloatVector
             * @param unit Unit; the unit of the new MutableFloatVector
             */
            public Dense(final float[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Create a new Relative Dense MutableFloatVector.
             * @param values FloatScalar.Abs[]; the initial values for the entries of the new MutableFloatVector
             * @throws ValueException when values has zero entries
             */
            public Dense(final FloatScalar.Abs<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /** {@inheritDoc} */
            @Override
            public final FloatVector.Rel.Dense<U> immutable()
            {
                this.copyOnWrite = true;
                return new FloatVector.Rel.Dense<U>(this.vectorSI, this.unit);
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatVector.Rel.Dense<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableFloatVector.Rel.Dense<U>(this.vectorSI, this.unit);
            }

            /** {@inheritDoc} */
            @Override
            protected final FloatMatrix1D createMatrix1D(final int size)
            {
                return new SparseFloatMatrix1D(size);
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
             * @param values FloatMatrix1D; the initial values for the entries of the new MutableFloatVector
             * @param unit Unit; the unit of the new MutableFloatVector
             */
            protected Sparse(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Relative Sparse MutableFloatVector.
             * @param values float[]; the initial values for the entries of the new MutableFloatVector
             * @param unit Unit; the unit of the new MutableFloatVector
             */
            public Sparse(final float[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Create a new Relative Sparse MutableFloatVector.
             * @param values FloatScalar.Rel[]; initial values for the entries of the new MutableFloatVector
             * @throws ValueException when values has zero entries
             */
            public Sparse(final FloatScalar.Rel<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /** {@inheritDoc} */
            @Override
            public final FloatVector.Rel.Sparse<U> immutable()
            {
                this.copyOnWrite = true;
                return new FloatVector.Rel.Sparse<U>(this.vectorSI, this.unit);
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatVector.Rel.Sparse<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableFloatVector.Rel.Sparse<U>(this.vectorSI, this.unit);
            }

            /** {@inheritDoc} */
            @Override
            protected final FloatMatrix1D createMatrix1D(final int size)
            {
                return new SparseFloatMatrix1D(size);
            }

        }

        /** {@inheritDoc} */
        @Override
        public final FloatScalar.Rel<U> get(final int index) throws ValueException
        {
            return new FloatScalar.Rel<U>(getInUnit(index, this.unit), this.unit);
        }

    }

    /**
     * Make (immutable) FloatVector equivalent for any type of MutableFloatVector.
     * @return FloatVector
     */
    public abstract FloatVector<U> immutable();

    /** {@inheritDoc} */
    @Override
    public final MutableFloatVector<U> copy()
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
        if (this.copyOnWrite)
        {
            // System.out.println("copyOnWrite is set: Copying data");
            this.vectorSI = this.vectorSI.copy(); // makes a deep copy, using multithreading
            this.copyOnWrite = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void setSI(final int index, final float valueSI) throws ValueException
    {
        checkIndex(index);
        checkCopyOnWrite();
        safeSet(index, valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void set(final int index, final FloatScalar<U> value) throws ValueException
    {
        setSI(index, value.getValueSI());
    }

    /** {@inheritDoc} */
    @Override
    public final void setInUnit(final int index, final float value, final U valueUnit) throws ValueException
    {
        // TODO: creating a FloatScalarAbs along the way may not be the most efficient way to do this...
        setSI(index, new FloatScalar.Abs<U>(value, valueUnit).getValueSI());
    }

    /**
     * Execute a function on a cell by cell basis.
     * @param f cern.colt.function.tfloat.FloatFunction; the function to apply
     */
    public final void assign(final cern.colt.function.tfloat.FloatFunction f)
    {
        checkCopyOnWrite();
        this.vectorSI.assign(f);
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
     * Increment the values in this MutableFloatVector by the corresponding values in a FloatVector.
     * @param increment FloatVector; contains the amounts by which to increment the corresponding entries in this
     *            MutableFloatVector
     * @return this
     * @throws ValueException when the vectors do not have the same size
     */
    private MutableFloatVector<U> incrementValueByValue(final FloatVector<U> increment) throws ValueException
    {
        checkSizeAndCopyOnWrite(increment);
        for (int index = this.size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) + increment.safeGet(index));
        }
        return this;
    }

    /**
     * Increment the entries in this MutableFloatVector by the corresponding values in a Relative FloatVector.
     * @param rel FloatVector.Rel; the Relative FloatVector
     * @return this
     * @throws ValueException when the vectors do not have the same size
     */
    public final MutableFloatVector<U> incrementBy(final FloatVector.Rel<U> rel) throws ValueException
    {
        return incrementValueByValue(rel);
    }

    /**
     * Decrement the values in this MutableFloatVector by the corresponding values in a FloatVector.
     * @param decrement FloatVector; contains the amounts by which to decrement the corresponding entries in this
     *            MutableFloatVector
     * @return this
     * @throws ValueException when the vectors do not have the same size
     */
    private MutableFloatVector<U> decrementValueByValue(final FloatVector<U> decrement) throws ValueException
    {
        checkSizeAndCopyOnWrite(decrement);
        for (int index = this.size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) - decrement.safeGet(index));
        }
        return this;
    }

    /**
     * Decrement the entries in this MutableFloatVector by the corresponding values in a Relative FloatVector.
     * @param rel FloatVector.Rel; the Relative FloatVector
     * @return this
     * @throws ValueException when the vectors do not have the same size
     */
    public final MutableFloatVector<U> decrementBy(final FloatVector.Rel<U> rel) throws ValueException
    {
        return decrementValueByValue(rel);
    }

    /**
     * Decrement the entries in this MutableFloatVector by the corresponding values in a Absolute FloatVector.
     * @param abs FloatVector.Abs; the Absolute FloatVector
     * @return this
     * @throws ValueException when the vectors do not have the same size
     */
    public final MutableFloatVector<U> decrementBy(final FloatVector.Abs<U> abs) throws ValueException
    {
        return decrementValueByValue(abs);
    }

    /**
     * Scale the values in this MutableFloatVector by the corresponding values in a FloatVector.
     * @param factor FloatVector; contains the values by which to scale the corresponding entries in this MutableFloatVector
     * @throws ValueException when the vectors do not have the same size
     */
    public final void scaleValueByValue(final FloatVector<?> factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int index = this.size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) * factor.safeGet(index));
        }
    }

    /**
     * Scale the values in this MutableFloatVector by the corresponding values in a float array.
     * @param factor float[]; contains the values by which to scale the corresponding entries in this MutableFloatVector
     * @return this
     * @throws ValueException when the vector and the array do not have the same size
     */
    public final MutableFloatVector<U> scaleValueByValue(final float[] factor) throws ValueException
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
     * @param other FloatVector; partner for the size check
     * @throws ValueException when the vectors do not have the same size
     */
    private void checkSizeAndCopyOnWrite(final FloatVector<?> other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other float[]; partner for the size check
     * @throws ValueException when the vectors do not have the same size
     */
    private void checkSizeAndCopyOnWrite(final float[] other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Add two FloatVectors entry by entry.
     * @param left Absolute Dense FloatVector
     * @param right Relative FloatVector
     * @return new Absolute Dense Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Abs.Dense<U> plus(final FloatVector.Abs.Dense<U> left,
            final FloatVector.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Abs.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatVectors entry by entry.
     * @param left Absolute Sparse FloatVector
     * @param right Relative Dense FloatVector
     * @return new Absolute Dense Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Abs.Dense<U> plus(final FloatVector.Abs.Sparse<U> left,
            final FloatVector.Rel.Dense<U> right) throws ValueException
    {
        return (MutableFloatVector.Abs.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Add two FloatVectors entry by entry.
     * @param left Absolute Sparse FloatVector
     * @param right Relative FloatVector
     * @return new Absolute Sparse Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Abs.Sparse<U> plus(final FloatVector.Abs.Sparse<U> left,
            final FloatVector.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableFloatVector.Abs.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatVectors entry by entry.
     * @param left Relative Dense FloatVector
     * @param right Relative FloatVector
     * @return new Absolute Dense Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Rel.Dense<U> plus(final FloatVector.Rel.Dense<U> left,
            final FloatVector.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Rel.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatVectors entry by entry.
     * @param left Relative Sparse FloatVector
     * @param right Relative FloatVector
     * @return new Relative Sparse Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Rel.Sparse<U> plus(final FloatVector.Rel.Sparse<U> left,
            final FloatVector.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Rel.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry.
     * @param left Absolute Dense FloatVector
     * @param right Absolute FloatVector
     * @return new Relative Dense Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Rel.Dense<U> minus(final FloatVector.Abs.Dense<U> left,
            final FloatVector.Abs<U> right) throws ValueException
    {
        return (MutableFloatVector.Rel.Dense<U>) new MutableFloatVector.Rel.Dense<U>(left.deepCopyOfData(), left.getUnit())
                .decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry.
     * @param left Absolute Sparse FloatVector
     * @param right Absolute FloatVector
     * @return new Relative Sparse Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Rel.Sparse<U> minus(final FloatVector.Abs.Sparse<U> left,
            final FloatVector.Abs<U> right) throws ValueException
    {
        return (MutableFloatVector.Rel.Sparse<U>) new MutableFloatVector.Rel.Sparse<U>(left.deepCopyOfData(), left.getUnit())
                .decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry.
     * @param left Absolute Dense FloatVector
     * @param right Relative FloatVector
     * @return new Relative Dense Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Abs.Dense<U> minus(final FloatVector.Abs.Dense<U> left,
            final FloatVector.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Abs.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry.
     * @param left Absolute Sparse FloatVector
     * @param right Relative FloatVector
     * @return new Absolute Sparse Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Abs.Sparse<U> minus(final FloatVector.Abs.Sparse<U> left,
            final FloatVector.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Abs.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry.
     * @param left Relative Dense FloatVector
     * @param right Relative FloatVector
     * @return new Relative Dense Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Rel.Dense<U> minus(final FloatVector.Rel.Dense<U> left,
            final FloatVector.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Rel.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry.
     * @param left Relative Sparse FloatVector
     * @param right Relative FloatVector
     * @return new Relative Sparse Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Rel.Sparse<U> minus(final FloatVector.Rel.Sparse<U> left,
            final FloatVector.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Rel.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Multiply two FloatVectors entry by entry.
     * @param left Absolute Dense FloatVector
     * @param right Absolute FloatVector
     * @return new Absolute Dense Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     */
    public static MutableFloatVector.Abs.Dense<SIUnit> times(final FloatVector.Abs.Dense<?> left, final FloatVector.Abs<?> right)
            throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatVector.Abs.Dense<SIUnit> work = new MutableFloatVector.Abs.Dense<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two FloatVectors entry by entry.
     * @param left Relative Dense FloatVector
     * @param right Relative FloatVector
     * @return new Relative Dense Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     */
    public static MutableFloatVector.Rel.Dense<SIUnit> times(final FloatVector.Rel.Dense<?> left, final FloatVector.Rel<?> right)
            throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatVector.Rel.Dense<SIUnit> work = new MutableFloatVector.Rel.Dense<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two FloatVectors entry by entry.
     * @param left Absolute Sparse FloatVector
     * @param right Absolute FloatVector
     * @return new XAbsolute Sparse Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     */
    public static MutableFloatVector.Abs.Sparse<SIUnit> times(final FloatVector.Abs.Sparse<?> left,
            final FloatVector.Abs<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatVector.Abs.Sparse<SIUnit> work =
                new MutableFloatVector.Abs.Sparse<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two FloatVectors entry by entry.
     * @param left Relative Sparse FloatVector
     * @param right Relative FloatVector
     * @return new Relative Sparse Mutable FloatVector
     * @throws ValueException when the vectors do not have the same size
     */
    public static MutableFloatVector.Rel.Sparse<SIUnit> times(final FloatVector.Rel.Sparse<?> left,
            final FloatVector.Rel<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatVector.Rel.Sparse<SIUnit> work =
                new MutableFloatVector.Rel.Sparse<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply the values in a FloatVector by the corresponding values in a float array.
     * @param left Absolute Dense FloatVector
     * @param right float[]; the float array
     * @return new Dense Absolute Mutable FloatVector
     * @throws ValueException when the vector and the array do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Abs.Dense<U> times(final FloatVector.Abs.Dense<U> left,
            final float[] right) throws ValueException
    {
        return (MutableFloatVector.Abs.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a FloatVector by the corresponding values in a float array.
     * @param left Relative Dense FloatVector
     * @param right float[]; the float array
     * @return new Relative Dense Mutable FloatVector
     * @throws ValueException when the vector and the array do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Rel.Dense<U> times(final FloatVector.Rel.Dense<U> left,
            final float[] right) throws ValueException
    {
        return (MutableFloatVector.Rel.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a FloatVector by the corresponding values in a float array.
     * @param left Absolute Sparse FloatVector
     * @param right float[]; the float array
     * @return new Absolute Sparse Mutable FloatVector
     * @throws ValueException when the vector and the array do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Abs.Sparse<U> times(final FloatVector.Abs.Sparse<U> left,
            final float[] right) throws ValueException
    {
        return (MutableFloatVector.Abs.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a FloatVector by the corresponding values in a float array.
     * @param left Relative Sparse FloatVector
     * @param right float[]; the float array
     * @return new Relative Sparse Mutable FloatVector
     * @throws ValueException when the vector and the array do not have the same size
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Rel.Sparse<U> times(final FloatVector.Rel.Sparse<U> left,
            final float[] right) throws ValueException
    {
        return (MutableFloatVector.Rel.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Make the Sparse equivalent of a DenseFloatMatrix1D.
     * @param dense DenseFloatMatrix1D
     * @return SparseFloatMatrix1D
     */
    private static FloatMatrix1D makeSparse(final FloatMatrix1D dense)
    {
        FloatMatrix1D result = new SparseFloatMatrix1D((int) dense.size());
        result.assign(dense);
        return result;
    }

    /**
     * Create a Sparse version of this Dense FloatVector.
     * @param in FloatVector.Abs.Dense the Dense FloatVector
     * @return MutableFloatVector.Abs.Sparse
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Abs.Sparse<U> denseToSparse(final FloatVector.Abs.Dense<U> in)
    {
        return new MutableFloatVector.Abs.Sparse<U>(makeSparse(in.vectorSI), in.getUnit());
    }

    /**
     * Create a Sparse version of this Dense FloatVector.
     * @param in FloatVector.Rel.Dense the Dense FloatVector
     * @return MutableFloatVector.Rel.Sparse
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Rel.Sparse<U> denseToSparse(final FloatVector.Rel.Dense<U> in)
    {
        return new MutableFloatVector.Rel.Sparse<U>(makeSparse(in.vectorSI), in.getUnit());
    }

    /**
     * Make the Dense equivalent of a SparseFloatMatrix1D.
     * @param sparse SparseFloatMatrix1D
     * @return DenseFloatMatrix1D
     */
    private static FloatMatrix1D makeDense(final FloatMatrix1D sparse)
    {
        FloatMatrix1D result = new SparseFloatMatrix1D((int) sparse.size());
        result.assign(sparse);
        return result;
    }

    /**
     * Create a Dense version of this Sparse FloatVector.
     * @param in FloatVector.Abs.Dense the Dense FloatVector
     * @return MutableFloatVector.Abs.Sparse
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Abs.Dense<U> sparseToDense(final FloatVector.Abs.Sparse<U> in)
    {
        return new MutableFloatVector.Abs.Dense<U>(makeDense(in.vectorSI), in.getUnit());
    }

    /**
     * Create a Dense version of this Sparse FloatVector.
     * @param in FloatVector.Rel.Dense the Dense FloatVector
     * @return MutableFloatVector.Rel.Sparse
     * @param <U> Unit; the unit of the parameters
     */
    public static <U extends Unit<U>> MutableFloatVector.Rel.Dense<U> sparseToDense(final FloatVector.Rel.Sparse<U> in)
    {
        return new MutableFloatVector.Rel.Dense<U>(makeDense(in.vectorSI), in.getUnit());
    }

}

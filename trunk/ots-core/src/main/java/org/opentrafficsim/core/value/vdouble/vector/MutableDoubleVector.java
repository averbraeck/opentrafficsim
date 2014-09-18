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
     * Create a new MutableDoubleVector.
     * @param unit Unit; the unit of the new Mutable DoubleVector
     */
    protected MutableDoubleVector(final U unit)
    {
        super(unit);
        // System.out.println("Created MutableDoubleVector");
    }

    /** If set, any modification of the data must be preceded by replacing the data with a local copy. */
    boolean copyOnWrite = false;

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.WriteDoubleVectorFunctions#normalize()
     */
    @Override
    public final void normalize() throws ValueException
    {
        double sum = zSum();
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
                this.copyOnWrite = true;
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
             * @param unit
             * @throws ValueException
             */
            public Dense(final DoubleScalar.Abs<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.MutableDoubleVector#immutable()
             */
            @Override
            public DoubleVector.Abs.Dense<U> immutable()
            {
                this.copyOnWrite = true;
                return new DoubleVector.Abs.Dense<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#mutable()
             */
            @Override
            public MutableDoubleVector.Abs.Dense<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableDoubleVector.Abs.Dense<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#createMatrix1D(int)
             */
            @Override
            protected DoubleMatrix1D createMatrix1D(final int size)
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
                this.copyOnWrite = true;
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

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.MutableDoubleVector#immutable()
             */
            @Override
            public final DoubleVector.Abs.Sparse<U> immutable()
            {
                this.copyOnWrite = true;
                return new DoubleVector.Abs.Sparse<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#mutable()
             */
            @Override
            public final MutableDoubleVector.Abs.Sparse<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableDoubleVector.Abs.Sparse<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#createMatrix1D(int)
             */
            @Override
            protected final DoubleMatrix1D createMatrix1D(final int size)
            {
                return new DenseDoubleMatrix1D(size);
            }

        }

        /**
         * @see org.opentrafficsim.core.value.vdouble.vector.ReadOnlyDoubleVectorFunctions#get(int)
         */
        @Override
        public final DoubleScalar.Abs<U> get(final int index) throws ValueException
        {
            return new DoubleScalar.Abs<U>(getInUnit(index, this.unit), this.unit);
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
                this.copyOnWrite = true;
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

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.MutableDoubleVector#immutable()
             */
            @Override
            public final DoubleVector.Rel.Dense<U> immutable()
            {
                this.copyOnWrite = true;
                return new DoubleVector.Rel.Dense<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#mutable()
             */
            @Override
            public final MutableDoubleVector.Rel.Dense<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableDoubleVector.Rel.Dense<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#createMatrix1D(int)
             */
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
                this.copyOnWrite = true;
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

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.MutableDoubleVector#immutable()
             */
            @Override
            public DoubleVector.Rel.Sparse<U> immutable()
            {
                this.copyOnWrite = true;
                return new DoubleVector.Rel.Sparse<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#mutable()
             */
            @Override
            public MutableDoubleVector.Rel.Sparse<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableDoubleVector.Rel.Sparse<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#createMatrix1D(int)
             */
            @Override
            protected DoubleMatrix1D createMatrix1D(final int size)
            {
                return new SparseDoubleMatrix1D(size);
            }

        }

        /**
         * @see org.opentrafficsim.core.value.vdouble.vector.ReadOnlyDoubleVectorFunctions#get(int)
         */
        @Override
        public DoubleScalar.Rel<U> get(final int index) throws ValueException
        {
            return new DoubleScalar.Rel<U>(getInUnit(index, this.unit), this.unit);
        }

    }

    /**
     * Make (immutable) DoubleVector equivalent for any type of MutableDoubleVector.
     * @return DoubleVector
     */
    public abstract DoubleVector<U> immutable();

    /**
     * @see org.opentrafficsim.core.value.Value#copy()
     */
    @Override
    public MutableDoubleVector<U> copy()
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

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.WriteDoubleVectorFunctions#setSI(int, double)
     */
    @Override
    public final void setSI(final int index, final double valueSI) throws ValueException
    {
        checkIndex(index);
        checkCopyOnWrite();
        safeSet(index, valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.WriteDoubleVectorFunctions#set(int,
     *      org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar)
     */
    @Override
    public final void set(final int index, final DoubleScalar<U> value) throws ValueException
    {
        setSI(index, value.getValueSI());
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.WriteDoubleVectorFunctions#setInUnit(int, double,
     *      org.opentrafficsim.core.unit.Unit)
     */
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
        this.vectorSI.assign(f);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#abs()
     */
    @Override
    public final void abs()
    {
        assign(DoubleFunctions.abs);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#acos()
     */
    @Override
    public final void acos()
    {
        assign(DoubleFunctions.acos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#asin()
     */
    @Override
    public final void asin()
    {
        assign(DoubleFunctions.asin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#atan()
     */
    @Override
    public final void atan()
    {
        assign(DoubleFunctions.atan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cbrt()
     */
    @Override
    public final void cbrt()
    {
        assign(DoubleMathFunctionsImpl.cbrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#ceil()
     */
    @Override
    public final void ceil()
    {
        assign(DoubleFunctions.ceil);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cos()
     */
    @Override
    public final void cos()
    {
        assign(DoubleFunctions.cos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cosh()
     */
    @Override
    public final void cosh()
    {
        assign(DoubleMathFunctionsImpl.cosh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#exp()
     */
    @Override
    public final void exp()
    {
        assign(DoubleFunctions.exp);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#expm1()
     */
    @Override
    public final void expm1()
    {
        assign(DoubleMathFunctionsImpl.expm1);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#floor()
     */
    @Override
    public final void floor()
    {
        assign(DoubleFunctions.floor);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log()
     */
    @Override
    public final void log()
    {
        assign(DoubleFunctions.log);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log10()
     */
    @Override
    public final void log10()
    {
        assign(DoubleMathFunctionsImpl.log10);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log1p()
     */
    @Override
    public final void log1p()
    {
        assign(DoubleMathFunctionsImpl.log1p);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#pow(double)
     */
    @Override
    public final void pow(final double x)
    {
        assign(DoubleFunctions.pow(x));
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#rint()
     */
    @Override
    public final void rint()
    {
        assign(DoubleFunctions.rint);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#round()
     */
    @Override
    public final void round()
    {
        assign(DoubleMathFunctionsImpl.round);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#signum()
     */
    @Override
    public final void signum()
    {
        assign(DoubleMathFunctionsImpl.signum);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sin()
     */
    @Override
    public final void sin()
    {
        assign(DoubleFunctions.sin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sinh()
     */
    @Override
    public final void sinh()
    {
        assign(DoubleMathFunctionsImpl.sinh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sqrt()
     */
    @Override
    public final void sqrt()
    {
        assign(DoubleFunctions.sqrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tan()
     */
    @Override
    public final void tan()
    {
        assign(DoubleFunctions.tan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tanh()
     */
    @Override
    public final void tanh()
    {
        assign(DoubleMathFunctionsImpl.tanh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toDegrees()
     */
    @Override
    public final void toDegrees()
    {
        assign(DoubleMathFunctionsImpl.toDegrees);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toRadians()
     */
    @Override
    public final void toRadians()
    {
        assign(DoubleMathFunctionsImpl.toRadians);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#inv()
     */
    @Override
    public final void inv()
    {
        assign(DoubleFunctions.inv);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.DoubleMathFunctions#multiply(double)
     */
    @Override
    public final void multiply(final double constant)
    {
        assign(DoubleFunctions.mult(constant));
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.DoubleMathFunctions#divide(double)
     */
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
     * @param factor DoubleVector; contains the values by which to scale the corresponding entries in this
     *            MutableDoubleVector
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
     * @param factor double[]; contains the values by which to scale the corresponding entries in this
     *            MutableDoubleVector
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
        return (MutableDoubleVector.Rel.Dense<U>) new MutableDoubleVector.Rel.Dense<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
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
        return (MutableDoubleVector.Rel.Sparse<U>) new MutableDoubleVector.Rel.Sparse<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
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
     * Create a Sparse version of this Dense DoubleVector. <br />
     * @param in DoubleVector.Abs.Dense the Dense DoubleVector
     * @return MutableDoubleVector.Abs.Sparse
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Sparse<U> denseToSparse(final DoubleVector.Abs.Dense<U> in)
    {
        return new MutableDoubleVector.Abs.Sparse<U>(makeSparse(in.vectorSI), in.getUnit());
    }

    /**
     * Create a Sparse version of this Dense DoubleVector. <br />
     * @param in DoubleVector.Rel.Dense the Dense DoubleVector
     * @return MutableDoubleVector.Rel.Sparse
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> denseToSparse(final DoubleVector.Rel.Dense<U> in)
    {
        return new MutableDoubleVector.Rel.Sparse<U>(makeSparse(in.vectorSI), in.getUnit());
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
     * Create a Dense version of this Sparse DoubleVector. <br />
     * @param in DoubleVector.Abs.Dense the Dense DoubleVector
     * @return MutableDoubleVector.Abs.Sparse
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> sparseToDense(final DoubleVector.Abs.Sparse<U> in)
    {
        return new MutableDoubleVector.Abs.Dense<U>(makeDense(in.vectorSI), in.getUnit());
    }

    /**
     * Create a Dense version of this Sparse DoubleVector. <br />
     * @param in DoubleVector.Rel.Dense the Dense DoubleVector
     * @return MutableDoubleVector.Rel.Sparse
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> sparseToDense(final DoubleVector.Rel.Sparse<U> in)
    {
        return new MutableDoubleVector.Rel.Dense<U>(makeDense(in.vectorSI), in.getUnit());
    }

}

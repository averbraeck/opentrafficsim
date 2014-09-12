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
     * @param unit
     */
    protected MutableDoubleVector(U unit)
    {
        super(unit);
        // System.out.println("Created MutableDoubleVector");
    }

    /** If set, any modification of the data must be preceded by replacing the data with a local copy */
    boolean copyOnWrite = false;

    public void normalize() throws ValueException
    {
        double sum = zSum();
        if (0 == sum)
            throw new ValueException("zSum is 0; cannot normalize");
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
         * Create an Abs.
         * @param unit
         */
        protected Abs(U unit)
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
             * For package internal use only
             * @param values
             * @param unit
             */
            protected Dense(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Absolute Dense Mutable DoubleVector
             * @param values
             * @param unit
             */
            public Dense(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * @param values
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
             * Create an immutable version
             * @return Dense Absolute Immutable DoubleVector
             */
            public DoubleVector.Abs.Dense<U> immutable()
            {
                this.copyOnWrite = true;
                return new DoubleVector.Abs.Dense<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#mutable()
             */
            public MutableDoubleVector.Abs.Dense<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableDoubleVector.Abs.Dense<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.AbstractDoubleVector#createMatrix1D(int)
             */
            @Override
            protected DoubleMatrix1D createMatrix1D(int size)
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
             * @param values
             * @param unit
             */
            protected Sparse(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Rel");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Dense Relative Mutable DoubleVector.
             * @param values
             * @param unit
             */
            public Sparse(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Rel");
                initialize(values);
            }

            /**
             * @param values
             * @param unit
             * @throws ValueException
             */
            public Sparse(final DoubleScalar.Rel<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Rel");
                initialize(values);
            }

            /**
             * Create an immutable version.
             * @return Absolute Dense Immutable DoubleVector
             */
            public DoubleVector.Abs.Sparse<U> immutable()
            {
                this.copyOnWrite = true;
                return new DoubleVector.Abs.Sparse<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#mutable()
             */
            public MutableDoubleVector.Abs.Sparse<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableDoubleVector.Abs.Sparse<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.AbstractDoubleVector#createMatrix1D(int)
             */
            @Override
            protected DoubleMatrix1D createMatrix1D(int size)
            {
                return new DenseDoubleMatrix1D(size);
            }

        }

        /**
         * @see org.opentrafficsim.core.value.vdouble.vector.ReadOnlyDoubleVectorFunctions#get(int)
         */
        @Override
        public DoubleScalar.Abs<U> get(int index) throws ValueException
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
         * Create a Relative.
         * @param unit
         */
        protected Rel(U unit)
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
             * @param values
             * @param unit
             */
            protected Dense(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a Sparse Absolute Mutable DoubleVector.
             * @param values
             * @param unit
             */
            public Dense(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * @param values
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
             * Create an immutable version.
             * @return Sparse Absolute Immutable DoubleVector
             */
            public DoubleVector.Rel.Dense<U> immutable()
            {
                this.copyOnWrite = true;
                return new DoubleVector.Rel.Dense<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#mutable()
             */
            public MutableDoubleVector.Rel.Dense<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableDoubleVector.Rel.Dense<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.AbstractDoubleVector#createMatrix1D(int)
             */
            @Override
            protected DoubleMatrix1D createMatrix1D(int size)
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
             * @param values
             * @param unit
             */
            protected Sparse(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Relative Sparse Mutable DoubleVector.
             * @param values
             * @param unit
             */
            public Sparse(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * @param values
             * @param unit
             * @throws ValueException
             */
            public Sparse(final DoubleScalar.Rel<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Create an immutable version.
             * @return Sparse Relative Immutable DoubleVector
             */
            public DoubleVector.Rel.Sparse<U> immutable()
            {
                this.copyOnWrite = true;
                return new DoubleVector.Rel.Sparse<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#mutable()
             */
            public MutableDoubleVector.Rel.Sparse<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableDoubleVector.Rel.Sparse<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.AbstractDoubleVector#createMatrix1D(int)
             */
            @Override
            protected DoubleMatrix1D createMatrix1D(int size)
            {
                return new SparseDoubleMatrix1D(size);
            }

        }

        /**
         * @see org.opentrafficsim.core.value.vdouble.vector.ReadOnlyDoubleVectorFunctions#get(int)
         */
        @Override
        public DoubleScalar.Rel<U> get(int index) throws ValueException
        {
            return new DoubleScalar.Rel<U>(getInUnit(index, this.unit), this.unit);
        }

    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.ReadOnlyDoubleVectorFunctions#get(int)
     */
    @Override
    public DoubleScalar<U> get(final int index) throws ValueException
    {
        if (this instanceof MutableDoubleVector.Abs)
            return new DoubleScalar.Abs<U>(getInUnit(index), this.unit);
        else if (this instanceof MutableDoubleVector.Rel)
            return new DoubleScalar.Rel<U>(getInUnit(index), this.unit);
        throw new Error("Cannot figure out subtype of this");
    }

    /**
     * Make (immutable) DoubleVector equivalent for any type of MutableDoubleVector.
     * @return DoubleVector
     */
    public abstract DoubleVector<U> immutable();

    /**
     * @see org.opentrafficsim.core.value.Value#copy()
     */
    public MutableDoubleVector<U> copy()
    {
        return immutable().mutable(); // Almost as simple as the copy in DoubleVector
    }

    /**
     * Check the copyOnWrite flag and, if it is set make a deep copy of the data and clear the flag.
     */
    protected void checkCopyOnWrite()
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
    public void setSI(final int index, final double valueSI) throws ValueException
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
    public void set(final int index, final DoubleScalar<U> value) throws ValueException
    {
        setSI(index, value.getValueSI());
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.WriteDoubleVectorFunctions#setInUnit(int, double,
     *      org.opentrafficsim.core.unit.Unit)
     */
    @Override
    public void setInUnit(final int index, final double value, final U valueUnit) throws ValueException
    {
        // TODO: creating a DoubleScalarAbs along the way may not be the most efficient way to do this...
        setSI(index, new DoubleScalar.Abs<U>(value, valueUnit).getValueSI());
    }

    /**
     * Execute a function on a cell by cell basis.
     * @param f cern.colt.function.tdouble.DoubleFunction; the function to apply
     */
    public void assign(final cern.colt.function.tdouble.DoubleFunction f)
    {
        checkCopyOnWrite();
        this.vectorSI.assign(f);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#abs()
     */
    @Override
    public void abs()
    {
        assign(DoubleFunctions.abs);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#acos()
     */
    @Override
    public void acos()
    {
        assign(DoubleFunctions.acos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#asin()
     */
    @Override
    public void asin()
    {
        assign(DoubleFunctions.asin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#atan()
     */
    @Override
    public void atan()
    {
        assign(DoubleFunctions.atan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cbrt()
     */
    @Override
    public void cbrt()
    {
        assign(DoubleMathFunctionsImpl.cbrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#ceil()
     */
    @Override
    public void ceil()
    {
        assign(DoubleFunctions.ceil);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cos()
     */
    @Override
    public void cos()
    {
        assign(DoubleFunctions.cos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cosh()
     */
    @Override
    public void cosh()
    {
        assign(DoubleMathFunctionsImpl.cosh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#exp()
     */
    @Override
    public void exp()
    {
        assign(DoubleFunctions.exp);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#expm1()
     */
    @Override
    public void expm1()
    {
        assign(DoubleMathFunctionsImpl.expm1);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#floor()
     */
    @Override
    public void floor()
    {
        assign(DoubleFunctions.floor);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log()
     */
    @Override
    public void log()
    {
        assign(DoubleFunctions.log);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log10()
     */
    @Override
    public void log10()
    {
        assign(DoubleMathFunctionsImpl.log10);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log1p()
     */
    @Override
    public void log1p()
    {
        assign(DoubleMathFunctionsImpl.log1p);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#pow(double)
     */
    @Override
    public void pow(final double x)
    {
        assign(DoubleFunctions.pow(x));
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#rint()
     */
    @Override
    public void rint()
    {
        assign(DoubleFunctions.rint);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#round()
     */
    @Override
    public void round()
    {
        assign(DoubleMathFunctionsImpl.round);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#signum()
     */
    @Override
    public void signum()
    {
        assign(DoubleMathFunctionsImpl.signum);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sin()
     */
    @Override
    public void sin()
    {
        assign(DoubleFunctions.sin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sinh()
     */
    @Override
    public void sinh()
    {
        assign(DoubleMathFunctionsImpl.sinh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sqrt()
     */
    @Override
    public void sqrt()
    {
        assign(DoubleFunctions.sqrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tan()
     */
    @Override
    public void tan()
    {
        assign(DoubleFunctions.tan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tanh()
     */
    @Override
    public void tanh()
    {
        assign(DoubleMathFunctionsImpl.tanh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toDegrees()
     */
    @Override
    public void toDegrees()
    {
        assign(DoubleMathFunctionsImpl.toDegrees);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toRadians()
     */
    @Override
    public void toRadians()
    {
        assign(DoubleMathFunctionsImpl.toRadians);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#inv()
     */
    @Override
    public void inv()
    {
        assign(DoubleFunctions.inv);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.DoubleMathFunctions#multiply(double)
     */
    @Override
    public void multiply(final double constant)
    {
        assign(DoubleFunctions.mult(constant));
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.DoubleMathFunctions#divide(double)
     */
    @Override
    public void divide(final double constant)
    {
        assign(DoubleFunctions.div(constant));
    }

    /**
     * Increment the values in this MutableDoubleVector by the corresponding values in a DoubleVector.
     * @param increment DoubleVector; contains the amounts by which to increment the corresponding entries in this
     *            MutableDoubleVector
     * @return this
     * @throws ValueException
     */
    private MutableDoubleVector<U> incrementValueByValue(DoubleVector<U> increment) throws ValueException
    {
        checkSizeAndCopyOnWrite(increment);
        for (int index = this.size(); --index >= 0;)
            safeSet(index, safeGet(index) + increment.safeGet(index));
        return this;
    }

    /**
     * Increment the entries in this MutableDoubleVector by the corresponding values in a Relative DoubleVector
     * @param rel
     * @return this
     * @throws ValueException
     */
    public MutableDoubleVector<U> incrementBy(DoubleVector.Rel<U> rel) throws ValueException
    {
        return incrementValueByValue(rel);
    }

    /**
     * Decrement the values in this MutableDoubleVector by the corresponding values in a DoubleVector.
     * @param decrement DoubleVector; contains the amounts by which to decrement the corresponding entries in this
     *            MutableDoubleVector
     * @return this
     * @throws ValueException
     */
    private MutableDoubleVector<U> decrementValueByValue(DoubleVector<U> decrement) throws ValueException
    {
        checkSizeAndCopyOnWrite(decrement);
        for (int index = this.size(); --index >= 0;)
            safeSet(index, safeGet(index) - decrement.safeGet(index));
        return this;
    }

    /**
     * Decrement the entries in this MutableDoubleVector by the corresponding values in a Relative DoubleVector
     * @param rel
     * @return this
     * @throws ValueException
     */
    public MutableDoubleVector<U> decrementBy(DoubleVector.Rel<U> rel) throws ValueException
    {
        return decrementValueByValue(rel);
    }

    /**
     * Decrement the entries in this MutableDoubleVector by the corresponding values in a Absolute DoubleVector
     * @param abs
     * @return this
     * @throws ValueException
     */
    public MutableDoubleVector<U> decrementBy(DoubleVector.Abs<U> abs) throws ValueException
    {
        return decrementValueByValue(abs);
    }

    /**
     * Scale the values in this MutableDoubleVector by the corresponding values in a DoubleVector.
     * @param factor DoubleVector; contains the values by which to scale the corresponding entries in this
     *            MutableDoubleVector
     * @throws ValueException
     */
    public void scaleValueByValue(DoubleVector<?> factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int index = this.size(); --index >= 0;)
            safeSet(index, safeGet(index) * factor.safeGet(index));
    }

    /**
     * Scale the values in this MutableDoubleVector by the corresponding values in a double array.
     * @param factor double[]; contains the values by which to scale the corresponding entries in this MutableDoubleVector
     * @return this
     * @throws ValueException
     */
    public MutableDoubleVector<U> scaleValueByValue(double[] factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int index = this.size(); --index >= 0;)
            safeSet(index, safeGet(index) * factor[index]);
        return this;
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other AbstractDoubleVector; partner for the size check
     * @throws ValueException
     */
    private void checkSizeAndCopyOnWrite(DoubleVector<?> other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other double[]; partner for the size check
     * @throws ValueException
     */
    private void checkSizeAndCopyOnWrite(double[] other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Add two DoubleVectors entry by entry
     * @param left Absolute Dense DoubleVector
     * @param right Relative DoubleVector
     * @return new Absolute Dense Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> plus(final DoubleVector.Abs.Dense<U> left,// tweede
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleVectors entry by entry
     * @param left Absolute Sparse DoubleVector
     * @param right Relative Dense DoubleVector
     * @return new Absolute Dense Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> plus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Add two DoubleVectors entry by entry
     * @param left Absolute Sparse DoubleVector
     * @param right Relative DoubleVector
     * @return new Absolute Sparse Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Sparse<U> plus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleVectors entry by entry
     * @param left Relative Dense DoubleVector
     * @param right Relative DoubleVector
     * @return new Absolute Dense Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> plus(final DoubleVector.Rel.Dense<U> left,// eerste
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleVectors entry by entry
     * @param left Relative Sparse DoubleVector
     * @param right Relative DoubleVector
     * @return new Relative Sparse Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> plus(final DoubleVector.Rel.Sparse<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Subtract two DoubleVectors entry by entry
     * @param left Absolute Dense DoubleVector
     * @param right Absolute DoubleVector
     * @return new Relative Dense Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> minus(final DoubleVector.Abs.Dense<U> left,
            final DoubleVector.Abs<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) new MutableDoubleVector.Rel.Dense<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors entry by entry
     * @param left Absolute Sparse DoubleVector
     * @param right Absolute DoubleVector
     * @return new Relative Sparse Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> minus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Abs<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Sparse<U>) new MutableDoubleVector.Rel.Sparse<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors entry by entry
     * @param left Absolute Dense DoubleVector
     * @param right Relative DoubleVector
     * @return new Relative Dense Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> minus(final DoubleVector.Abs.Dense<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors entry by entry
     * @param left Absolute Sparse DoubleVector
     * @param right Relative DoubleVector
     * @return new Absolute Sparse Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Sparse<U> minus(final DoubleVector.Abs.Sparse<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors entry by entry
     * @param left Relative Dense DoubleVector
     * @param right Relative DoubleVector
     * @return new Relative Dense Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> minus(final DoubleVector.Rel.Dense<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleVectors entry by entry
     * @param left Relative Sparse DoubleVector
     * @param right Relative DoubleVector
     * @return new Relative Sparse Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> minus(final DoubleVector.Rel.Sparse<U> left,
            final DoubleVector.Rel<U> right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Multiply two DoubleVectors entry by entry
     * @param left Absolute Dense DoubleVector
     * @param right Absolute DoubleVector
     * @return new Absolute Dense Mutable DoubleVector
     * @throws ValueException
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
     * Multiply two DoubleVectors entry by entry
     * @param left Relative Dense DoubleVector
     * @param right Relative DoubleVector
     * @return new Relative Dense Mutable DoubleVector
     * @throws ValueException
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
     * Multiply two DoubleVectors entry by entry
     * @param left Absolute Sparse DoubleVector
     * @param right Absolute DoubleVector
     * @return new XAbsolute Sparse Mutable DoubleVector
     * @throws ValueException
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
     * Multiply two DoubleVectors entry by entry
     * @param left Relative Sparse DoubleVector
     * @param right Relative DoubleVector
     * @return new Relative Sparse Mutable DoubleVector
     * @throws ValueException
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
     * @param right double[]
     * @return new Dense Absolute Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> times(final DoubleVector.Abs.Dense<U> left,
            final double[] right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleVector by the corresponding values in a double array.
     * @param left Relative Dense DoubleVector
     * @param right double[]
     * @return new Relative Dense Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> times(final DoubleVector.Rel.Dense<U> left,
            final double[] right) throws ValueException
    {
        return (MutableDoubleVector.Rel.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleVector by the corresponding values in a double array.
     * @param left Absolute Sparse DoubleVector
     * @param right double[]
     * @return new Absolute Sparse Mutable DoubleVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Sparse<U> times(final DoubleVector.Abs.Sparse<U> left,
            final double[] right) throws ValueException
    {
        return (MutableDoubleVector.Abs.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleVector by the corresponding values in a double array.
     * @param left Relative Sparse DoubleVector
     * @param right double[]
     * @return new Relative Sparse Mutable DoubleVector
     * @throws ValueException
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
    private static DoubleMatrix1D makeSparse(DoubleMatrix1D dense)
    {
        DoubleMatrix1D result = new SparseDoubleMatrix1D((int) dense.size());
        result.assign(dense);
        return result;
    }

    /**
     * Create a Sparse version of this Dense DoubleVector. <br />
     * @param in DoubleVector.Abs.Dense the Dense DoubleVector
     * @return MutableDoubleVector.Abs.Sparse
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Sparse<U> denseToSparse(final DoubleVector.Abs.Dense<U> in)
    {
        return new MutableDoubleVector.Abs.Sparse<U>(makeSparse(in.vectorSI), in.getUnit());
    }

    /**
     * Create a Sparse version of this Dense DoubleVector. <br />
     * @param in DoubleVector.Rel.Dense the Dense DoubleVector
     * @return MutableDoubleVector.Rel.Sparse
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Sparse<U> denseToSparse(final DoubleVector.Rel.Dense<U> in)
    {
        return new MutableDoubleVector.Rel.Sparse<U>(makeSparse(in.vectorSI), in.getUnit());
    }

    /**
     * Make the Dense equivalent of a SparseDoubleMatrix1D.
     * @param dense DenseDoubleMatrix1D
     * @return DenseDoubleMatrix1D
     */
    private static DoubleMatrix1D makeDense(DoubleMatrix1D sparse)
    {
        DoubleMatrix1D result = new SparseDoubleMatrix1D((int) sparse.size());
        result.assign(sparse);
        return result;
    }

    /**
     * Create a Dense version of this Sparse DoubleVector. <br />
     * @param in DoubleVector.Abs.Dense the Dense DoubleVector
     * @return MutableDoubleVector.Abs.Sparse
     */
    public static <U extends Unit<U>> MutableDoubleVector.Abs.Dense<U> sparseToDense(final DoubleVector.Abs.Sparse<U> in)
    {
        return new MutableDoubleVector.Abs.Dense<U>(makeDense(in.vectorSI), in.getUnit());
    }

    /**
     * Create a Dense version of this Sparse DoubleVector. <br />
     * @param in DoubleVector.Rel.Dense the Dense DoubleVector
     * @return MutableDoubleVector.Rel.Sparse
     */
    public static <U extends Unit<U>> MutableDoubleVector.Rel.Dense<U> sparseToDense(final DoubleVector.Rel.Sparse<U> in)
    {
        return new MutableDoubleVector.Rel.Dense<U>(makeDense(in.vectorSI), in.getUnit());
    }

}

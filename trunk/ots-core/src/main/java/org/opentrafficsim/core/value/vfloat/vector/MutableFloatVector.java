package org.opentrafficsim.core.value.vfloat.vector;

import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.DenseData;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.SparseData;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.WriteFloatVectorFunctions;
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
 * @param <U> Unit of this MutableFloatVector
 */
public abstract class MutableFloatVector<U extends Unit<U>> extends AbstractFloatVector<U> implements
        WriteFloatVectorFunctions<U>, FloatMathFunctions
{
    /** */
    private static final long serialVersionUID = 20130903L;

    /**
     * @param unit
     */
    protected MutableFloatVector(U unit)
    {
        super(unit);
        // System.out.println("Created MutableFloatVector");
    }

    /** If set, any modification of the data must be preceded by replacing the data with a local copy */
    boolean copyOnWrite = false;

    public void normalize() throws ValueException
    {
        float sum = zSum();
        if (0 == sum)
            throw new ValueException("zSum is 0; cannot normalize");
        checkCopyOnWrite();
        for (int i = 0; i < this.vectorSI.size(); i++)
        {
            safeSet(i, this.vectorSI.get(i) / sum);
        }
    }

    /**
     * @param <U> Unit
     */
    public abstract static class Dense<U extends Unit<U>> extends MutableFloatVector<U> implements DenseData
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a Dense.
         * @param unit
         */
        private Dense(U unit)
        {
            super(unit);
            throw new Error("There is never a need to create a Dense");
        }

        /**
         * @param <U> Unit
         */
        public static class Abs<U extends Unit<U>> extends MutableFloatVector<U> implements Absolute
        {
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * For package internal use only
             * @param values
             * @param unit
             */
            protected Abs(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Abs");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Dense Absolute Mutable FloatVector
             * @param values
             * @param unit
             */
            public Abs(final float[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Abs");
                initialize(values);
            }

            /**
             * @param values
             * @param unit
             * @throws ValueException
             */
            public Abs(final FloatScalar.Abs<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Abs");
                initialize(values);
            }

            /**
             * Create an immutable version
             * @return Dense Absolute Immutable FloatVector
             */
            public FloatVector.Dense.Abs<U> immutable()
            {
                this.copyOnWrite = true;
                return new FloatVector.Dense.Abs<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.ReadOnlyFloatVectorFunctions#get(int)
             */
            @Override
            public FloatScalar<U> get(int index) throws ValueException
            {
                return new FloatScalar.Abs<U>(getInUnit(index, this.unit), this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.vector.AbstractFloatVector#createMatrix1D(int)
             */
            @Override
            protected FloatMatrix1D createMatrix1D(int size)
            {
                return new DenseFloatMatrix1D(size);
            }

        }

        /**
         * @param <U> Unit
         */
        public static class Rel<U extends Unit<U>> extends MutableFloatVector<U> implements Relative
        {
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * For package internal use only.
             * @param values
             * @param unit
             */
            protected Rel(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Rel");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Dense Relative Mutable FloatVector.
             * @param values
             * @param unit
             */
            public Rel(final float[] values, final U unit)
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
            public Rel(final FloatScalar.Rel<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Rel");
                initialize(values);
            }

            /**
             * Create an immutable version.
             * @return Dense Relative Immutable FloatVector
             */
            public FloatVector.Dense.Rel<U> immutable()
            {
                this.copyOnWrite = true;
                return new FloatVector.Dense.Rel<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.ReadOnlyFloatVectorFunctions#get(int)
             */
            @Override
            public FloatScalar<U> get(int index) throws ValueException
            {
                return new FloatScalar.Rel<U>(getInUnit(index, this.unit), this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.vector.AbstractFloatVector#createMatrix1D(int)
             */
            @Override
            protected FloatMatrix1D createMatrix1D(int size)
            {
                return new DenseFloatMatrix1D(size);
            }

        }

    }

    /**
     * @param <U> Unit
     */
    public abstract static class Sparse<U extends Unit<U>> extends MutableFloatVector<U> implements SparseData
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a Sparse.
         * @param unit
         */
        private Sparse(U unit)
        {
            super(unit);
            throw new Error("There is never a need to create a Sparse");
        }

        /**
         * @param <U> Unit
         */
        public static class Abs<U extends Unit<U>> extends MutableFloatVector<U> implements Absolute
        {
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * For package internal use only.
             * @param values
             * @param unit
             */
            protected Abs(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Abs");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a Sparse Absolute Mutable FloatVector.
             * @param values
             * @param unit
             */
            public Abs(final float[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Abs");
                initialize(values);
            }

            /**
             * @param values
             * @param unit
             * @throws ValueException
             */
            public Abs(final FloatScalar.Abs<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Abs");
                initialize(values);
            }

            /**
             * Create an immutable version.
             * @return Sparse Absolute Immutable FloatVector
             */
            public FloatVector.Sparse.Abs<U> immutable()
            {
                this.copyOnWrite = true;
                return new FloatVector.Sparse.Abs<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.ReadOnlyFloatVectorFunctions#get(int)
             */
            @Override
            public FloatScalar<U> get(int index) throws ValueException
            {
                return new FloatScalar.Abs<U>(getInUnit(index, this.unit), this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.vector.AbstractFloatVector#createMatrix1D(int)
             */
            @Override
            protected FloatMatrix1D createMatrix1D(int size)
            {
                return new SparseFloatMatrix1D(size);
            }

        }

        /**
         * @param <U> Unit
         */
        public static class Rel<U extends Unit<U>> extends MutableFloatVector<U> implements Relative
        {
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * For package internal use only.
             * @param values
             * @param unit
             */
            protected Rel(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Rel");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Sparse Relative Mutable FloatVector.
             * @param values
             * @param unit
             */
            public Rel(final float[] values, final U unit)
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
            public Rel(final FloatScalar.Rel<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Rel");
                initialize(values);
            }

            /**
             * Create an immutable version.
             * @return Sparse Relative Immutable FloatVector
             */
            public FloatVector.Sparse.Rel<U> immutable()
            {
                this.copyOnWrite = true;
                return new FloatVector.Sparse.Rel<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.ReadOnlyFloatVectorFunctions#get(int)
             */
            @Override
            public FloatScalar<U> get(int index) throws ValueException
            {
                return new FloatScalar.Abs<U>(getInUnit(index, this.unit), this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.vector.AbstractFloatVector#createMatrix1D(int)
             */
            @Override
            protected FloatMatrix1D createMatrix1D(int size)
            {
                return new SparseFloatMatrix1D(size);
            }

        }

    }

    /**
     * @see org.opentrafficsim.core.value.ReadOnlyFloatVectorFunctions#get(int)
     */
    @Override
    public FloatScalar<U> get(int index) throws ValueException
    {
        if (this instanceof MutableFloatVector.Dense.Abs || this instanceof MutableFloatVector.Sparse.Abs)
            return new FloatScalar.Abs<U>(getInUnit(index), this.unit);
        else if (this instanceof MutableFloatVector.Dense.Rel || this instanceof MutableFloatVector.Sparse.Rel)
            return new FloatScalar.Rel<U>(getInUnit(index), this.unit);
        throw new Error("Cannot figure out subtype of this");
    }

    /**
     * Make (immutable) FloatVector equivalent for any type of MutableFloatVector.
     * @return FloatVector
     */
    public abstract FloatVector<U> immutable();

    /**
     * @see org.opentrafficsim.core.value.Value#copy()
     */
    public MutableFloatVector<U> copy()
    {
        return immutable().mutable(); // Almost as simple as the copy in FloatVector
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
     * @see org.opentrafficsim.core.value.WriteFloatVectorFunctions#setSI(int, float)
     */
    @Override
    public void setSI(final int index, final float valueSI) throws ValueException
    {
        checkIndex(index);
        checkCopyOnWrite();
        safeSet(index, valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.WriteFloatVectorFunctions#set(int,
     *      org.opentrafficsim.core.value.vfloat.scalar.FloatScalar)
     */
    @Override
    public void set(final int index, final FloatScalar<U> value) throws ValueException
    {
        setSI(index, value.getValueSI());
    }

    /**
     * @see org.opentrafficsim.core.value.WriteFloatVectorFunctions#setInUnit(int, float,
     *      org.opentrafficsim.core.unit.Unit)
     */
    @Override
    public void setInUnit(final int index, final float value, final U valueUnit) throws ValueException
    {
        // TODO: creating a FloatScalarAbs along the way may not be the most efficient way to do this...
        setSI(index, new FloatScalar.Abs<U>(value, valueUnit).getValueSI());
    }

    /**
     * Execute a function on a cell by cell basis.
     * @param f cern.colt.function.tfloat.FloatFunction; the function to apply
     */
    public void assign(final cern.colt.function.tfloat.FloatFunction f)
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
        assign(FloatFunctions.abs);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#acos()
     */
    @Override
    public void acos()
    {
        assign(FloatFunctions.acos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#asin()
     */
    @Override
    public void asin()
    {
        assign(FloatFunctions.asin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#atan()
     */
    @Override
    public void atan()
    {
        assign(FloatFunctions.atan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cbrt()
     */
    @Override
    public void cbrt()
    {
        assign(FloatMathFunctionsImpl.cbrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#ceil()
     */
    @Override
    public void ceil()
    {
        assign(FloatFunctions.ceil);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cos()
     */
    @Override
    public void cos()
    {
        assign(FloatFunctions.cos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cosh()
     */
    @Override
    public void cosh()
    {
        assign(FloatMathFunctionsImpl.cosh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#exp()
     */
    @Override
    public void exp()
    {
        assign(FloatFunctions.exp);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#expm1()
     */
    @Override
    public void expm1()
    {
        assign(FloatMathFunctionsImpl.expm1);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#floor()
     */
    @Override
    public void floor()
    {
        assign(FloatFunctions.floor);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log()
     */
    @Override
    public void log()
    {
        assign(FloatFunctions.log);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log10()
     */
    @Override
    public void log10()
    {
        assign(FloatMathFunctionsImpl.log10);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log1p()
     */
    @Override
    public void log1p()
    {
        assign(FloatMathFunctionsImpl.log1p);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#pow(double)
     */
    @Override
    public void pow(final double x)
    {
        assign(FloatFunctions.pow((float) x));
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#rint()
     */
    @Override
    public void rint()
    {
        assign(FloatFunctions.rint);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#round()
     */
    @Override
    public void round()
    {
        assign(FloatMathFunctionsImpl.round);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#signum()
     */
    @Override
    public void signum()
    {
        assign(FloatMathFunctionsImpl.signum);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sin()
     */
    @Override
    public void sin()
    {
        assign(FloatFunctions.sin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sinh()
     */
    @Override
    public void sinh()
    {
        assign(FloatMathFunctionsImpl.sinh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sqrt()
     */
    @Override
    public void sqrt()
    {
        assign(FloatFunctions.sqrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tan()
     */
    @Override
    public void tan()
    {
        assign(FloatFunctions.tan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tanh()
     */
    @Override
    public void tanh()
    {
        assign(FloatMathFunctionsImpl.tanh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toDegrees()
     */
    @Override
    public void toDegrees()
    {
        assign(FloatMathFunctionsImpl.toDegrees);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toRadians()
     */
    @Override
    public void toRadians()
    {
        assign(FloatMathFunctionsImpl.toRadians);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#inv()
     */
    @Override
    public void inv()
    {
        assign(FloatFunctions.inv);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.FloatMathFunctions#multiply(float)
     */
    @Override
    public void multiply(final float constant)
    {
        assign(FloatFunctions.mult(constant));
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.FloatMathFunctions#divide(float)
     */
    @Override
    public void divide(final float constant)
    {
        assign(FloatFunctions.div(constant));
    }

    /**
     * Increment the values in this MutableFloatVector by the corresponding values in an AbstractFloatVector.
     * @param increment AbstractFloatVector; contains the amounts by which to increment the corresponding entries in
     *            this MutableFloatVector
     * @return this
     * @throws ValueException
     */
    private MutableFloatVector<U> incrementValueByValue(AbstractFloatVector<U> increment) throws ValueException
    {
        checkSizeAndCopyOnWrite(increment);
        for (int index = this.size(); --index >= 0;)
            safeSet(index, safeGet(index) + increment.safeGet(index));
        return this;
    }

    /**
     * Increment the entries in this MutableFloatVector by the corresponding values in a Dense Relative FloatVector
     * @param rel
     * @return this
     * @throws ValueException
     */
    public MutableFloatVector<U> incrementBy(FloatVector.Dense.Rel<U> rel) throws ValueException
    {
        return incrementValueByValue(rel);
    }

    /**
     * Increment the entries in this MutableFloatVector by the corresponding values in a Sparse Relative FloatVector
     * @param rel
     * @return this
     * @throws ValueException
     */
    public MutableFloatVector<U> incrementBy(FloatVector.Sparse.Rel<U> rel) throws ValueException
    {
        return incrementValueByValue(rel);
    }

    /**
     * Decrement the values in this MutableFloatVector by the corresponding values in an AbstractFloatVector.
     * @param decrement AbstractFloatVector; contains the amounts by which to decrement the corresponding entries in
     *            this MutableFloatVector
     * @return this
     * @throws ValueException
     */
    private MutableFloatVector<U> decrementValueByValue(AbstractFloatVector<U> decrement) throws ValueException
    {
        checkSizeAndCopyOnWrite(decrement);
        for (int index = this.size(); --index >= 0;)
            safeSet(index, safeGet(index) - decrement.safeGet(index));
        return this;
    }

    /**
     * Decrement the entries in this MutableFloatVector by the corresponding values in a Dense Relative FloatVector
     * @param rel
     * @return this
     * @throws ValueException
     */
    public MutableFloatVector<U> decrementBy(FloatVector.Dense.Rel<U> rel) throws ValueException
    {
        return decrementValueByValue(rel);
    }

    /**
     * Decrement the entries in this MutableFloatVector by the corresponding values in a Sparse Relative FloatVector
     * @param rel
     * @return this
     * @throws ValueException
     */
    public MutableFloatVector<U> decrementBy(FloatVector.Sparse.Rel<U> rel) throws ValueException
    {
        return decrementValueByValue(rel);
    }

    /**
     * Decrement the entries in this MutableFloatVector by the corresponding values in a Dense Relative FloatVector
     * @param abs
     * @return this
     * @throws ValueException
     */
    public MutableFloatVector<U> decrementBy(FloatVector.Dense.Abs<U> abs) throws ValueException
    {
        return decrementValueByValue(abs);
    }

    /**
     * Decrement the entries in this MutableFloatVector by the corresponding values in a Sparse Relative FloatVector
     * @param abs
     * @return this
     * @throws ValueException
     */
    public MutableFloatVector<U> decrementBy(FloatVector.Sparse.Abs<U> abs) throws ValueException
    {
        return decrementValueByValue(abs);
    }

    /**
     * Scale the values in this MutableFloatVector by the corresponding values in an AbstractFloatVector.
     * @param factor AbstractFloatVector; contains the values by which to scale the corresponding entries in this
     *            MutableFloatVector
     * @throws ValueException
     */
    public void scaleValueByValue(AbstractFloatVector<?> factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int index = this.size(); --index >= 0;)
            safeSet(index, safeGet(index) * factor.safeGet(index));
    }

    /**
     * Scale the values in this MutableFloatVector by the corresponding values in a float array.
     * @param factor float[]; contains the values by which to scale the corresponding entries in this MutableFloatVector
     * @return this
     * @throws ValueException
     */
    public MutableFloatVector<U> scaleValueByValue(float[] factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int index = this.size(); --index >= 0;)
            safeSet(index, safeGet(index) * factor[index]);
        return this;
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other AbstractFloatVector; partner for the size check
     * @throws ValueException
     */
    private void checkSizeAndCopyOnWrite(AbstractFloatVector<?> other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other float[]; partner for the size check
     * @throws ValueException
     */
    private void checkSizeAndCopyOnWrite(float[] other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Add two FloatVectors entry by entry
     * @param left Dense Absolute FloatVector
     * @param right Dense Relative FloatVector
     * @return new Dense Absolute Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Abs<U> plus(final FloatVector.Dense.Abs<U> left,
            final FloatVector.Dense.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Dense.Abs<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatVectors entry by entry
     * @param left Sparse Absolute FloatVector
     * @param right Sparse Relative FloatVector
     * @return new Sparse Absolute Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Abs<U> plus(final FloatVector.Sparse.Abs<U> left,
            final FloatVector.Sparse.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Sparse.Abs<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatVectors entry by entry
     * @param left Dense Absolute FloatVector
     * @param right Sparse Relative FloatVector
     * @return new Dense Absolute Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Abs<U> plus(final FloatVector.Dense.Abs<U> left,
            final FloatVector.Sparse.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Dense.Abs<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatVectors entry by entry
     * @param left Sparse Absolute FloatVector
     * @param right Dense Relative FloatVector
     * @return new Sparse Absolute Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Abs<U> plus(final FloatVector.Sparse.Abs<U> left,
            final FloatVector.Dense.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Sparse.Abs<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatVectors entry by entry
     * @param left Dense Relative FloatVector
     * @param right Dense Relative FloatVector
     * @return new Dense Absolute Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Rel<U> plus(final FloatVector.Dense.Rel<U> left,
            final FloatVector.Dense.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Dense.Rel<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatVectors entry by entry
     * @param left Sparse Relative FloatVector
     * @param right Sparse Relative FloatVector
     * @return new Sparse Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Rel<U> plus(final FloatVector.Sparse.Rel<U> left,
            final FloatVector.Sparse.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Sparse.Rel<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatVectors entry by entry
     * @param left Dense Relative FloatVector
     * @param right Sparse Relative FloatVector
     * @return new Dense Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Rel<U> plus(final FloatVector.Dense.Rel<U> left,
            final FloatVector.Sparse.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Dense.Rel<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatVectors entry by entry
     * @param left Sparse Relative FloatVector
     * @param right Dense Relative FloatVector
     * @return new Sparse Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Rel<U> plus(final FloatVector.Sparse.Rel<U> left,
            final FloatVector.Dense.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Sparse.Rel<U>) left.mutable().incrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry
     * @param left Dense Absolute FloatVector
     * @param right Dense Absolute FloatVector
     * @return new Dense Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Rel<U> minus(final FloatVector.Dense.Abs<U> left,
            final FloatVector.Dense.Abs<U> right) throws ValueException
    {
        return (MutableFloatVector.Dense.Rel<U>) new MutableFloatVector.Dense.Rel<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry
     * @param left Sparse Absolute FloatVector
     * @param right Sparse Absolute FloatVector
     * @return new Sparse Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Rel<U> minus(final FloatVector.Sparse.Abs<U> left,
            final FloatVector.Sparse.Abs<U> right) throws ValueException
    {
        return (MutableFloatVector.Sparse.Rel<U>) new MutableFloatVector.Sparse.Rel<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry
     * @param left Dense Absolute FloatVector
     * @param right Sparse Absolute FloatVector
     * @return new Dense Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Rel<U> minus(final FloatVector.Dense.Abs<U> left,
            final FloatVector.Sparse.Abs<U> right) throws ValueException
    {
        return (MutableFloatVector.Dense.Rel<U>) new MutableFloatVector.Dense.Rel<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry
     * @param left Sparse Absolute FloatVector
     * @param right Dense Absolute FloatVector
     * @return new Sparse Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Rel<U> minus(final FloatVector.Sparse.Abs<U> left,
            final FloatVector.Dense.Abs<U> right) throws ValueException
    {
        return (MutableFloatVector.Sparse.Rel<U>) new MutableFloatVector.Dense.Rel<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry
     * @param left Dense Absolute FloatVector
     * @param right Dense Relative FloatVector
     * @return new Dense Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Abs<U> minus(final FloatVector.Dense.Abs<U> left,
            final FloatVector.Dense.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Dense.Abs<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry
     * @param left Sparse Absolute FloatVector
     * @param right Sparse Relative FloatVector
     * @return new Sparse Absolute Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Abs<U> minus(final FloatVector.Sparse.Abs<U> left,
            final FloatVector.Sparse.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Sparse.Abs<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry
     * @param left Dense Absolute FloatVector
     * @param right Sparse Relative FloatVector
     * @return new Dense Absolute Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Abs<U> minus(final FloatVector.Dense.Abs<U> left,
            final FloatVector.Sparse.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Dense.Abs<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry
     * @param left Sparse Absolute FloatVector
     * @param right Dense Relative FloatVector
     * @return new Sparse Absolute Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Abs<U> minus(final FloatVector.Sparse.Abs<U> left,
            final FloatVector.Dense.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Sparse.Abs<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry
     * @param left Dense Relative FloatVector
     * @param right Dense Relative FloatVector
     * @return new Dense Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Rel<U> minus(final FloatVector.Dense.Rel<U> left,
            final FloatVector.Dense.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Dense.Rel<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry
     * @param left Sparse Relative FloatVector
     * @param right Sparse Relative FloatVector
     * @return new Sparse Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Rel<U> minus(final FloatVector.Sparse.Rel<U> left,
            final FloatVector.Sparse.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Sparse.Rel<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry
     * @param left Dense Relative FloatVector
     * @param right Sparse Relative FloatVector
     * @return new Dense Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Rel<U> minus(final FloatVector.Dense.Rel<U> left,
            final FloatVector.Sparse.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Dense.Rel<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatVectors entry by entry
     * @param left Sparse Relative FloatVector
     * @param right Dense Relative FloatVector
     * @return new Sparse Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Rel<U> minus(final FloatVector.Sparse.Rel<U> left,
            final FloatVector.Dense.Rel<U> right) throws ValueException
    {
        return (MutableFloatVector.Sparse.Rel<U>) left.mutable().decrementBy(right);
    }

    /**
     * Multiply two FloatVectors entry by entry
     * @param left Dense Absolute FloatVector
     * @param right Dense Absolute FloatVector
     * @return new Dense Absolute Mutable FloatVector
     * @throws ValueException
     */
    public static MutableFloatVector.Dense.Abs<SIUnit> times(final FloatVector.Dense.Abs<?> left,
            final FloatVector.Dense.Abs<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatVector.Dense.Abs<SIUnit> work =
                new MutableFloatVector.Dense.Abs<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two FloatVectors entry by entry
     * @param left Dense Relative FloatVector
     * @param right Dense Relative FloatVector
     * @return new Dense Relative Mutable FloatVector
     * @throws ValueException
     */
    public static MutableFloatVector.Dense.Rel<SIUnit> times(final FloatVector.Dense.Rel<?> left,
            final FloatVector.Dense.Rel<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatVector.Dense.Rel<SIUnit> work =
                new MutableFloatVector.Dense.Rel<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two FloatVectors entry by entry
     * @param left Sparse Absolute FloatVector
     * @param right Sparse Absolute FloatVector
     * @return new Sparse Absolute Mutable FloatVector
     * @throws ValueException
     */
    public static MutableFloatVector.Sparse.Abs<SIUnit> times(final FloatVector.Sparse.Abs<?> left,
            final FloatVector.Sparse.Abs<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatVector.Sparse.Abs<SIUnit> work =
                new MutableFloatVector.Sparse.Abs<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply two FloatVectors entry by entry
     * @param left Sparse Relative FloatVector
     * @param right Sparse Relative FloatVector
     * @return new Sparse Relative Mutable FloatVector
     * @throws ValueException
     */
    public static MutableFloatVector.Sparse.Rel<SIUnit> times(final FloatVector.Sparse.Rel<?> left,
            final FloatVector.Sparse.Rel<?> right) throws ValueException
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        MutableFloatVector.Sparse.Rel<SIUnit> work =
                new MutableFloatVector.Sparse.Rel<SIUnit>(left.deepCopyOfData(), targetUnit);
        work.scaleValueByValue(right);
        return work;
    }

    /**
     * Multiply the values in a FloatVector by the corresponding values in a float array.
     * @param left Dense Absolute FloatVector
     * @param right float[]
     * @return new Dense Absolute Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Abs<U> times(final FloatVector.Dense.Abs<U> left,
            final float[] right) throws ValueException
    {
        return (MutableFloatVector.Dense.Abs<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a FloatVector by the corresponding values in a float array.
     * @param left Dense Relative FloatVector
     * @param right float[]
     * @return new Dense Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Rel<U> times(final FloatVector.Dense.Rel<U> left,
            final float[] right) throws ValueException
    {
        return (MutableFloatVector.Dense.Rel<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a FloatVector by the corresponding values in a float array.
     * @param left Sparse Absolute FloatVector
     * @param right float[]
     * @return new Sparse Absolute Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Abs<U> times(final FloatVector.Sparse.Abs<U> left,
            final float[] right) throws ValueException
    {
        return (MutableFloatVector.Sparse.Abs<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a FloatVector by the corresponding values in a float array.
     * @param left Sparse Relative FloatVector
     * @param right float[]
     * @return new Sparse Relative Mutable FloatVector
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Rel<U> times(final FloatVector.Sparse.Rel<U> left,
            final float[] right) throws ValueException
    {
        return (MutableFloatVector.Sparse.Rel<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Make the Sparse equivalent of a DenseFloatMatrix1D.
     * @param dense DenseFloatMatrix1D
     * @return SparseFloatMatrix1D
     */
    private static FloatMatrix1D makeSparse(FloatMatrix1D dense)
    {
        FloatMatrix1D result = new SparseFloatMatrix1D((int) dense.size());
        result.assign(dense);
        return result;
    }

    /**
     * Create a Sparse version of this Dense FloatVector. <br />
     * @param in FloatVector.Dense.Abs the Dense FloatVector
     * @return MutableFloatVector.Sparse.Abs
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Abs<U> denseToSparse(final FloatVector.Dense.Abs<U> in)
    {
        return new MutableFloatVector.Sparse.Abs<U>(makeSparse(in.vectorSI), in.getUnit());
    }

    /**
     * Create a Sparse version of this Dense FloatVector. <br />
     * @param in FloatVector.Dense.Abs the Dense FloatVector
     * @return MutableFloatVector.Sparse.Abs
     */
    public static <U extends Unit<U>> MutableFloatVector.Sparse.Rel<U> denseToSparse(final FloatVector.Dense.Rel<U> in)
    {
        return new MutableFloatVector.Sparse.Rel<U>(makeSparse(in.vectorSI), in.getUnit());
    }

    /**
     * Make the Dense equivalent of a SparseFloatMatrix1D.
     * @param dense DenseFloatMatrix1D
     * @return DenseFloatMatrix1D
     */
    private static FloatMatrix1D makeDense(FloatMatrix1D sparse)
    {
        FloatMatrix1D result = new SparseFloatMatrix1D((int) sparse.size());
        result.assign(sparse);
        return result;
    }

    /**
     * Create a Dense version of this Sparse FloatVector. <br />
     * @param in FloatVector.Dense.Abs the Dense FloatVector
     * @return MutableFloatVector.Sparse.Abs
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Abs<U> sparseToDense(final FloatVector.Sparse.Abs<U> in)
    {
        return new MutableFloatVector.Dense.Abs<U>(makeDense(in.vectorSI), in.getUnit());
    }

    /**
     * Create a Dense version of this Sparse FloatVector. <br />
     * @param in FloatVector.Dense.Abs the Dense FloatVector
     * @return MutableFloatVector.Sparse.Abs
     */
    public static <U extends Unit<U>> MutableFloatVector.Dense.Rel<U> sparseToDense(final FloatVector.Sparse.Rel<U> in)
    {
        return new MutableFloatVector.Dense.Rel<U>(makeDense(in.vectorSI), in.getUnit());
    }

}

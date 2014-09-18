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
 * @version Sep 9, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit
 */
public abstract class MutableFloatMatrix<U extends Unit<U>> extends FloatMatrix<U> implements
        WriteFloatMatrixFunctions<U>, FloatMathFunctions
{

    /** */
    private static final long serialVersionUID = 20140909L;

    /**
     * Create a new MutableFloatMatrix.
     * @param unit Unit; the unit of the new MutableFloatMatrix
     */
    protected MutableFloatMatrix(final U unit)
    {
        super(unit);
    }

    /** If set, any modification of the data must be preceded by replacing the data with a local copy. */
    boolean copyOnWrite = false;

    /**
     * @see org.opentrafficsim.core.value.vfloat.matrix.WriteFloatMatrixFunctions#normalize()
     */
    @Override
    public final void normalize() throws ValueException
    {
        float sum = zSum();
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
    public abstract static class Abs<U extends Unit<U>> extends MutableFloatMatrix<U> implements Absolute
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a new Absolute MutableFloatMatrix.
         * @param unit Unit; the unit of the new MutableFloatMatrix
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
             * @param values FloatMatrix2D; the initial values of the new MutableFloatMatrix
             * @param unit Unit; the unit of the new MutableFloatMatrix
             */
            protected Dense(final FloatMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Absolute Dense MutableFloatMatrix.
             * @param values float[][]; the initial values of the new MutableFloatMatrix
             * @param unit Unit; the unit of the new MutableFloatMatrix
             * @throws ValueException when values is not rectangular
             */
            public Dense(final float[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Create a new Absolute Dense MutableFloatMatrix.
             * @param values FloatScalar.Abs[][]; the initial values of the entries of the new MutableFloatMatrix
             * @throws ValueException when values is not rectangular
             */
            public Dense(final FloatScalar.Abs<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.matrix.MutableFloatMatrix#immutable()
             */
            @Override
            public FloatMatrix.Abs.Dense<U> immutable()
            {
                this.copyOnWrite = true;
                return new FloatMatrix.Abs.Dense<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrix#mutable()
             */
            @Override
            public MutableFloatMatrix.Abs.Dense<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableFloatMatrix.Abs.Dense<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrix#createMatrix2D(int, int)
             */
            @Override
            protected FloatMatrix2D createMatrix2D(final int rows, final int columns)
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
             * For package internal use only.
             * @param values FloatMatrix2D; the initial values of the entries of the new MutableFloatMatrix
             * @param unit Unit; the unit of the new MutableFloatMatrix
             */
            protected Sparse(final FloatMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Absolute Sparse MutableFloatMatrix.
             * @param values float[][]; the initial values of the entries of the new MutableFloatMatrix
             * @param unit Unit; the unit of the new MutableFloatMatrix
             * @throws ValueException when values is not rectangular
             */
            public Sparse(final float[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Create a new Absolute Sparse MutableFloatMatrix.
             * @param values FloatScalar.Abs[][]; the initial values of the entries of the new MutableFloatMatrix
             * @throws ValueException when values is not rectangular, or contains zero entries
             */
            public Sparse(final FloatScalar.Abs<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.matrix.MutableFloatMatrix#immutable()
             */
            @Override
            public final FloatMatrix.Abs.Sparse<U> immutable()
            {
                this.copyOnWrite = true;
                return new FloatMatrix.Abs.Sparse<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrix#mutable()
             */
            @Override
            public final MutableFloatMatrix.Abs.Sparse<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableFloatMatrix.Abs.Sparse<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrix#createMatrix2D(int, int)
             */
            @Override
            protected final FloatMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new DenseFloatMatrix2D(rows, columns);
            }

        }

        /**
         * @see org.opentrafficsim.core.value.vfloat.matrix.ReadOnlyFloatMatrixFunctions#get(int, int)
         */
        @Override
        public final FloatScalar.Abs<U> get(final int row, final int column) throws ValueException
        {
            return new FloatScalar.Abs<U>(getInUnit(row, column, this.unit), this.unit);
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
         * Create a new Relative MutableFloatMatrix.
         * @param unit Unit; the unit of the new MutableFloatMatrix
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
             * @param values FloatMatrix2D; the initial values of the entries of the new MutableFloatMatrix
             * @param unit Unit; the unit of the new MutableFloatMatrix
             */
            protected Dense(final FloatMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Relative Dense MutableFloatMatrix.
             * @param values float[][]; the initial values of the entries of the new MutableFloatMatrix
             * @param unit Unit; the unit of the value of the new MutableFloatMatrix
             * @throws ValueException when values is not rectangular
             */
            public Dense(final float[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Create a new Relative Dense MutableFloatMatrix.
             * @param values FloatScalar.Rel[][]; the initial values of the entries of the new MutableFloatMatrix
             * @throws ValueException when values is not rectangular, or contains zero entries
             */
            public Dense(final FloatScalar.Rel<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.matrix.MutableFloatMatrix#immutable()
             */
            @Override
            public FloatMatrix.Rel.Dense<U> immutable()
            {
                this.copyOnWrite = true;
                return new FloatMatrix.Rel.Dense<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrix#mutable()
             */
            @Override
            public MutableFloatMatrix.Rel.Dense<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableFloatMatrix.Rel.Dense<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrix#createMatrix2D(int, int)
             */
            @Override
            protected FloatMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new SparseFloatMatrix2D(rows, columns);
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
             * @param values FloatMatrix2D; the initial values of the entries of the new MutableFloatMatrix
             * @param unit Unit; the unit of the new MutableFloatMatrix
             */
            protected Sparse(final FloatMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Relative Sparse MutableFloatMatrix.
             * @param values float[][]; the initial values of the entries of the new MutableFloatMatrix
             * @param unit Unit; the unit of the values of the new MutableFloatMatrix
             * @throws ValueException when values is not rectangular
             */
            public Sparse(final float[][] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Create a new Relative Sparse MutableFloatMatrix.
             * @param values FloatScalar.Rel[][]; the initial values of the entries of the new MutableFloatMatrix
             * @throws ValueException when values is not rectangular, or has zero entries
             */
            public Sparse(final FloatScalar.Rel<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.matrix.MutableFloatMatrix#immutable()
             */
            @Override
            public FloatMatrix.Rel.Sparse<U> immutable()
            {
                this.copyOnWrite = true;
                return new FloatMatrix.Rel.Sparse<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrix#mutable()
             */
            @Override
            public MutableFloatMatrix.Rel.Sparse<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableFloatMatrix.Rel.Sparse<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.matrix.FloatMatrix#createMatrix2D(int, int)
             */
            @Override
            protected FloatMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new SparseFloatMatrix2D(rows, columns);
            }

        }

        /**
         * @see org.opentrafficsim.core.value.vfloat.matrix.ReadOnlyFloatMatrixFunctions#get(int, int)
         */
        @Override
        public FloatScalar.Rel<U> get(final int row, final int column) throws ValueException
        {
            return new FloatScalar.Rel<U>(getInUnit(row, column, this.unit), this.unit);
        }

    }

    /**
     * Make (immutable) FloatMatrix equivalent for any type of MutableFloatMatrix.
     * @return FloatMatrix
     */
    public abstract FloatMatrix<U> immutable();

    /**
     * @see org.opentrafficsim.core.value.Value#copy()
     */
    @Override
    public MutableFloatMatrix<U> copy()
    {
        return immutable().mutable(); // Almost as simple as the copy in FloatMatrix
    }

    /**
     * Check the copyOnWrite flag and, if it is set make a deep copy of the data and clear the flag.
     */
    protected final void checkCopyOnWrite()
    {
        if (this.copyOnWrite)
        {
            // System.out.println("copyOnWrite is set: Copying data");
            this.matrixSI = this.matrixSI.copy(); // makes a deep copy, using multithreading
            this.copyOnWrite = false;
        }
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.matrix.WriteFloatMatrixFunctions#setSI(int, int, float)
     */
    @Override
    public final void setSI(final int row, final int column, final float valueSI) throws ValueException
    {
        checkIndex(row, column);
        checkCopyOnWrite();
        safeSet(row, column, valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.matrix.WriteFloatMatrixFunctions#set(int, int,
     *      org.opentrafficsim.core.value.vfloat.scalar.FloatScalar)
     */
    @Override
    public final void set(final int row, final int column, final FloatScalar<U> value) throws ValueException
    {
        setSI(row, column, value.getValueSI());
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.matrix.WriteFloatMatrixFunctions#setInUnit(int, int, float,
     *      org.opentrafficsim.core.unit.Unit)
     */
    @Override
    public final void setInUnit(final int row, final int column, final float value, final U valueUnit) throws ValueException
    {
        // TODO: creating a FloatScalarAbs along the way may not be the most efficient way to do this...
        setSI(row, column, new FloatScalar.Abs<U>(value, valueUnit).getValueSI());
    }

    /**
     * Execute a function on a cell by cell basis.
     * @param f cern.colt.function.tfloat.FloatFunction; the function to apply
     */
    public final void assign(final cern.colt.function.tfloat.FloatFunction f)
    {
        checkCopyOnWrite();
        this.matrixSI.assign(f);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#abs()
     */
    @Override
    public final void abs()
    {
        assign(FloatFunctions.abs);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#acos()
     */
    @Override
    public final void acos()
    {
        assign(FloatFunctions.acos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#asin()
     */
    @Override
    public final void asin()
    {
        assign(FloatFunctions.asin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#atan()
     */
    @Override
    public final void atan()
    {
        assign(FloatFunctions.atan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cbrt()
     */
    @Override
    public final void cbrt()
    {
        assign(FloatMathFunctionsImpl.cbrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#ceil()
     */
    @Override
    public final void ceil()
    {
        assign(FloatFunctions.ceil);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cos()
     */
    @Override
    public final void cos()
    {
        assign(FloatFunctions.cos);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cosh()
     */
    @Override
    public final void cosh()
    {
        assign(FloatMathFunctionsImpl.cosh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#exp()
     */
    @Override
    public final void exp()
    {
        assign(FloatFunctions.exp);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#expm1()
     */
    @Override
    public final void expm1()
    {
        assign(FloatMathFunctionsImpl.expm1);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#floor()
     */
    @Override
    public final void floor()
    {
        assign(FloatFunctions.floor);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log()
     */
    @Override
    public final void log()
    {
        assign(FloatFunctions.log);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log10()
     */
    @Override
    public final void log10()
    {
        assign(FloatMathFunctionsImpl.log10);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log1p()
     */
    @Override
    public final void log1p()
    {
        assign(FloatMathFunctionsImpl.log1p);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#pow(double)
     */
    @Override
    public final void pow(final double x)
    {
        assign(FloatFunctions.pow((float) x));
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#rint()
     */
    @Override
    public final void rint()
    {
        assign(FloatFunctions.rint);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#round()
     */
    @Override
    public final void round()
    {
        assign(FloatMathFunctionsImpl.round);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#signum()
     */
    @Override
    public final void signum()
    {
        assign(FloatMathFunctionsImpl.signum);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sin()
     */
    @Override
    public final void sin()
    {
        assign(FloatFunctions.sin);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sinh()
     */
    @Override
    public final void sinh()
    {
        assign(FloatMathFunctionsImpl.sinh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sqrt()
     */
    @Override
    public final void sqrt()
    {
        assign(FloatFunctions.sqrt);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tan()
     */
    @Override
    public final void tan()
    {
        assign(FloatFunctions.tan);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tanh()
     */
    @Override
    public final void tanh()
    {
        assign(FloatMathFunctionsImpl.tanh);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toDegrees()
     */
    @Override
    public final void toDegrees()
    {
        assign(FloatMathFunctionsImpl.toDegrees);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toRadians()
     */
    @Override
    public final void toRadians()
    {
        assign(FloatMathFunctionsImpl.toRadians);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#inv()
     */
    @Override
    public final void inv()
    {
        assign(FloatFunctions.inv);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.FloatMathFunctions#multiply(float)
     */
    @Override
    public final void multiply(final float constant)
    {
        assign(FloatFunctions.mult(constant));
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.FloatMathFunctions#divide(float)
     */
    @Override
    public final void divide(final float constant)
    {
        assign(FloatFunctions.div(constant));
    }

    /**
     * Increment the values in this MutableFloatMatrix by the corresponding values in a FloatMatrix.
     * @param increment FloatMatrix; contains the amounts by which to increment the corresponding entries in this
     *            MutableFloatMatrix
     * @return this
     * @throws ValueException if the matrices do not have the same size
     */
    private MutableFloatMatrix<U> incrementValueByValue(final FloatMatrix<U> increment) throws ValueException
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
     * Increment the entries in this MutableFloatMatrix by the corresponding values in a Relative FloatMatrix.
     * @param rel FloatMatrix.Rel; the Relative FloatMatrix
     * @return this
     * @throws ValueException if the matrices do not have the same size
     */
    public final MutableFloatMatrix<U> incrementBy(final FloatMatrix.Rel<U> rel) throws ValueException
    {
        return incrementValueByValue(rel);
    }

    /**
     * Decrement the values in this MutableFloatMatrix by the corresponding values in a FloatMatrix.
     * @param decrement FloatMatrix; contains the amounts by which to decrement the corresponding entries in this
     *            MutableFloatMatrix
     * @return this
     * @throws ValueException if the matrices do not have the same size
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
     * Decrement the entries in this MutableFloatMatrix by the corresponding values in a Relative FloatMatrix.
     * @param rel FloatMatrix.Rel; the Relative FloatMatrix
     * @return this
     * @throws ValueException if the matrices do not have the same size
     */
    public final MutableFloatMatrix<U> decrementBy(final FloatMatrix.Rel<U> rel) throws ValueException
    {
        return decrementValueByValue(rel);
    }

    /**
     * Decrement the entries in this MutableFloatMatrix by the corresponding values in a Absolute FloatMatrix.
     * @param abs FloatMatrix.Abs; the Absolute FloatMatrix
     * @return this
     * @throws ValueException if the matrices do not have the same size
     */
    public final MutableFloatMatrix<U> decrementBy(final FloatMatrix.Abs<U> abs) throws ValueException
    {
        return decrementValueByValue(abs);
    }

    /**
     * Scale the values in this MutableFloatMatrix by the corresponding values in a FloatMatrix.
     * @param factor FloatMatrix; contains the values by which to scale the corresponding entries in this
     *            MutableFloatMatrix
     * @throws ValueException if the matrices do not have the same size
     */
    public final void scaleValueByValue(final FloatMatrix<?> factor) throws ValueException
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
     * Scale the values in this MutableFloatMatrix by the corresponding values in a float array.
     * @param factor float[][]; contains the values by which to scale the corresponding entries in this
     *            MutableFloatMatrix
     * @return this
     * @throws ValueException if the matrix and the array do not have the same size
     */
    public final MutableFloatMatrix<U> scaleValueByValue(final float[][] factor) throws ValueException
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
     * @param other FloatMatrix; partner for the size check
     * @throws ValueException if the matrices do not have the same size
     */
    private void checkSizeAndCopyOnWrite(final FloatMatrix<?> other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other float[][]; partner for the size check
     * @throws ValueException when the size of other does not match the size of this
     */
    private void checkSizeAndCopyOnWrite(final float[][] other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Add two FloatMatrices entry by entry.
     * @param left Absolute Dense FloatMatrix
     * @param right Relative FloatMatrix
     * @return new Absolute Dense Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Dense<U> plus(final FloatMatrix.Abs.Dense<U> left,
            final FloatMatrix.Rel<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatMatrices entry by entry.
     * @param left Absolute Sparse FloatMatrix
     * @param right Relative Sparse FloatMatrix
     * @return new Absolute Sparse Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Sparse<U> plus(final FloatMatrix.Abs.Sparse<U> left,
            final FloatMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatMatrices entry by entry.
     * @param left Absolute Sparse FloatMatrix
     * @param right Relative Dense FloatMatrix
     * @return new Absolute Dense Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Dense<U> plus(final FloatMatrix.Abs.Sparse<U> left,
            final FloatMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Add two FloatMatrices entry by entry.
     * @param left Relative Dense FloatMatrix
     * @param right Relative FloatMatrix
     * @return new Absolute Dense Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> plus(final FloatMatrix.Rel.Dense<U> left,
            final FloatMatrix.Rel<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatMatrices entry by entry.
     * @param left Relative Sparse FloatMatrix
     * @param right Relative Sparse FloatMatrix
     * @return new Relative Sparse Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Sparse<U> plus(final FloatMatrix.Rel.Sparse<U> left,
            final FloatMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two FloatMatrices entry by entry.
     * @param left Relative Sparse FloatMatrix
     * @param right Relative Dense FloatMatrix
     * @return new Relative Dense Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> plus(final FloatMatrix.Rel.Sparse<U> left,
            final FloatMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Subtract two FloatMatrices entry by entry.
     * @param left Absolute Dense FloatMatrix
     * @param right Absolute FloatMatrix
     * @return new Relative Dense Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> minus(final FloatMatrix.Abs.Dense<U> left,
            final FloatMatrix.Abs<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) new MutableFloatMatrix.Rel.Dense<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices entry by entry.
     * @param left Absolute Sparse FloatMatrix
     * @param right Absolute Sparse FloatMatrix
     * @return new Relative Sparse Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Sparse<U> minus(final FloatMatrix.Abs.Sparse<U> left,
            final FloatMatrix.Abs.Sparse<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Sparse<U>) new MutableFloatMatrix.Rel.Sparse<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices entry by entry.
     * @param left Absolute Sparse FloatMatrix
     * @param right Absolute Dense FloatMatrix
     * @return new Relative Dense Mutable FloatMatrix
     * @param <U> Unit; the unit
     * @throws ValueException if the matrices do not have the same size
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> minus(final FloatMatrix.Abs.Sparse<U> left,
            final FloatMatrix.Abs.Dense<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) new MutableFloatMatrix.Rel.Dense<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices entry by entry.
     * @param left Absolute Dense FloatMatrix
     * @param right Relative FloatMatrix
     * @return new Absolute Dense Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Dense<U> minus(final FloatMatrix.Abs.Dense<U> left,
            final FloatMatrix.Rel<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices entry by entry.
     * @param left Absolute Sparse FloatMatrix
     * @param right Relative Sparse FloatMatrix
     * @return new Absolute Sparse Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Sparse<U> minus(final FloatMatrix.Abs.Sparse<U> left,
            final FloatMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices entry by entry.
     * @param left Absolute Sparse FloatMatrix
     * @param right Relative Dense FloatMatrix
     * @return new Absolute Dense Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Sparse<U> minus(final FloatMatrix.Abs.Sparse<U> left,
            final FloatMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Sparse<U>) sparseToDense(left).decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices entry by entry.
     * @param left Relative Dense FloatMatrix
     * @param right Relative FloatMatrix
     * @return new Relative Dense Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> minus(final FloatMatrix.Rel.Dense<U> left,
            final FloatMatrix.Rel<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices entry by entry.
     * @param left Relative Sparse FloatMatrix
     * @param right Relative Sparse FloatMatrix
     * @return new Relative Sparse Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Sparse<U> minus(final FloatMatrix.Rel.Sparse<U> left,
            final FloatMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two FloatMatrices entry by entry.
     * @param left Relative Sparse FloatMatrix
     * @param right Relative Dense FloatMatrix
     * @return new Relative Dense Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> minus(final FloatMatrix.Rel.Sparse<U> left,
            final FloatMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) sparseToDense(left).decrementBy(right);
    }

    /**
     * Multiply two FloatMatricess entry by entry.
     * @param left Absolute Dense FloatMatrix
     * @param right Absolute Dense FloatMatrix
     * @return new Absolute Dense Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
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
     * Multiply two FloatMatrices entry by entry.
     * @param left Relative Dense FloatMatrix
     * @param right Relative Dense FloatMatrix
     * @return new Relative Dense Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
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
     * Multiply two FloatMatrices entry by entry.
     * @param left Absolute Sparse FloatMatrix
     * @param right Absolute FloatMatrix
     * @return new Absolute Sparse Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
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
     * Multiply two FloatMatrices entry by entry.
     * @param left Relative Sparse FloatMatrix
     * @param right Relative FloatMatrix
     * @return new Relative Sparse Mutable FloatMatrix
     * @throws ValueException if the matrices do not have the same size
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
     * Multiply the values in a FloatMatrix by the corresponding values in a float array.
     * @param left Absolute Dense FloatMatrix
     * @param right float[][]; the float array
     * @return new Absolute Dense Mutable FloatMatrix
     * @throws ValueException if the matrix and the float array do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Dense<U> times(final FloatMatrix.Abs.Dense<U> left,
            final float[][] right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a FloatMatrix by the corresponding values in a float array.
     * @param left Relative Dense FloatMatrix
     * @param right float[][]; the float array
     * @return new Relative Dense Mutable FloatMatrix
     * @throws ValueException if the matrix and the float array do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> times(final FloatMatrix.Rel.Dense<U> left,
            final float[][] right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a FloatMatrix by the corresponding values in a float array.
     * @param left Absolute Sparse FloatMatrix
     * @param right float[][]; the float array
     * @return new Sparse Absolute Mutable FloatMatrix
     * @throws ValueException if the matrix and the float array do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Sparse<U> times(final FloatMatrix.Abs.Sparse<U> left,
            final float[][] right) throws ValueException
    {
        return (MutableFloatMatrix.Abs.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a FloatMatrix by the corresponding values in a float array.
     * @param left Relative Sparse FloatMatrix
     * @param right float[][]; the float array
     * @return new Relative Sparse Mutable FloatMatrix
     * @throws ValueException if the matrix and the float array do not have the same size
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Sparse<U> times(final FloatMatrix.Rel.Sparse<U> left,
            final float[][] right) throws ValueException
    {
        return (MutableFloatMatrix.Rel.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Make the Sparse equivalent of a DenseFloatMatrix2D.
     * @param dense DenseFloatMatrix2D
     * @return SparseFloatMatrix2D
     */
    private static FloatMatrix2D makeSparse(final FloatMatrix2D dense)
    {
        FloatMatrix2D result = new SparseFloatMatrix2D(dense.rows(), dense.columns());
        result.assign(dense);
        return result;
    }

    /**
     * Create a Sparse version of this Dense FloatMatrix.
     * @param in FloatMatrix.Abs.Dense the Dense FloatMatrix
     * @return MutableFloatMatrix.Sparse.Abs
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Sparse<U> denseToSparse(final FloatMatrix.Abs.Dense<U> in)
    {
        return new MutableFloatMatrix.Abs.Sparse<U>(makeSparse(in.matrixSI), in.getUnit());
    }

    /**
     * Create a Sparse version of this Dense FloatMatrix.
     * @param in FloatMatrix.Rel.Dense the Dense FloatMatrix
     * @return MutableFloatMatrix.Abs.Sparse
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Sparse<U> denseToSparse(final FloatMatrix.Rel.Dense<U> in)
    {
        return new MutableFloatMatrix.Rel.Sparse<U>(makeSparse(in.matrixSI), in.getUnit());
    }

    /**
     * Make the Dense equivalent of a SparseFloatMatrix2D.
     * @param sparse SpaseFloatMatrix2D
     * @return DenseFloatMatrix2D
     */
    private static FloatMatrix2D makeDense(final FloatMatrix2D sparse)
    {
        FloatMatrix2D result = new SparseFloatMatrix2D(sparse.rows(), sparse.columns());
        result.assign(sparse);
        return result;
    }

    /**
     * Create a Dense version of this Sparse FloatMatrix.
     * @param in FloatMatrix.Abs.Dense the Dense FloatMatrix
     * @return MutableFloatMatrix.Abs.Sparse
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Abs.Dense<U> sparseToDense(final FloatMatrix.Abs.Sparse<U> in)
    {
        return new MutableFloatMatrix.Abs.Dense<U>(makeDense(in.matrixSI), in.getUnit());
    }

    /**
     * Create a Dense version of this Sparse FloatMatrix.
     * @param in FloatMatrix.Abs.Dense the Dense FloatMatrix
     * @return MutableFloatMatrix.Abs.Dense
     * @param <U> Unit; the unit
     */
    public static <U extends Unit<U>> MutableFloatMatrix.Rel.Dense<U> sparseToDense(final FloatMatrix.Rel.Sparse<U> in)
    {
        return new MutableFloatMatrix.Rel.Dense<U>(makeDense(in.matrixSI), in.getUnit());
    }

}

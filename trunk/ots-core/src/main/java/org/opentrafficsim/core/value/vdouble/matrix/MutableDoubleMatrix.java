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
public abstract class MutableDoubleMatrix<U extends Unit<U>> extends DoubleMatrix<U> implements
        WriteDoubleMatrixFunctions<U>, DoubleMathFunctions
{

    /** */
    private static final long serialVersionUID = 20140909L;

    /**
     * @param unit
     */
    protected MutableDoubleMatrix(U unit)
    {
        super(unit);
    }

    /** If set, any modification of the data must be preceded by replacing the data with a local copy */
    boolean copyOnWrite = false;

    public void normalize() throws ValueException
    {
        double sum = zSum();
        if (0 == sum)
            throw new ValueException("zSum is 0; cannot normalize");
        checkCopyOnWrite();
        for (int row = 0; row < rows(); row++)
        {
            for (int column = 0; column < columns(); column++)
                safeSet(row, column, safeGet(row, column) / sum);
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
         * Create a Dense.
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
            protected Dense(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Dense Absolute Mutable DoubleMatrix
             * @param values
             * @param unit
             * @throws ValueException
             */
            public Dense(final double[][] values, final U unit) throws ValueException
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
            public Dense(final DoubleScalar.Abs<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.MutableDoubleMatrix#immutable()
             */
            public DoubleMatrix.Abs.Dense<U> immutable()
            {
                this.copyOnWrite = true;
                return new DoubleMatrix.Abs.Dense<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrix#mutable()
             */
            public MutableDoubleMatrix.Abs.Dense<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableDoubleMatrix.Abs.Dense<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.AbstractDoubleMatrix#createMatrix2D(int, int)
             */
            @Override
            protected DoubleMatrix2D createMatrix2D(final int rows, final int columns)
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
             * @param values
             * @param unit
             */
            protected Sparse(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Dense Relative Mutable DoubleMatrix.
             * @param values
             * @param unit
             * @throws ValueException
             */
            public Sparse(final double[][] values, final U unit) throws ValueException
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
            public Sparse(final DoubleScalar.Abs<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.MutableDoubleMatrix#immutable()
             */
            public DoubleMatrix.Abs.Sparse<U> immutable()
            {
                this.copyOnWrite = true;
                return new DoubleMatrix.Abs.Sparse<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrix#mutable()
             */
            public MutableDoubleMatrix.Abs.Sparse<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableDoubleMatrix.Abs.Sparse<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.AbstractDoubleMatrix#createMatrix2D(int, int)
             */
            @Override
            protected DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new DenseDoubleMatrix2D(rows, columns);
            }

        }

        /**
         * @see org.opentrafficsim.core.value.vdouble.matrix.MutableDoubleMatrix#get(int, int)
         */
        @Override
        public DoubleScalar.Abs<U> get(final int row, final int column) throws ValueException
        {
            return new DoubleScalar.Abs<U>(getInUnit(row, column, this.unit), this.unit);
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
         * Create a Rel.
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
            protected Dense(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a Sparse Absolute Mutable DoubleMatrix.
             * @param values
             * @param unit
             * @throws ValueException
             */
            public Dense(final double[][] values, final U unit) throws ValueException
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
            public Dense(final DoubleScalar.Rel<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.MutableDoubleMatrix#immutable()
             */
            public DoubleMatrix.Rel.Dense<U> immutable()
            {
                this.copyOnWrite = true;
                return new DoubleMatrix.Rel.Dense<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrix#mutable()
             */
            public MutableDoubleMatrix.Rel.Dense<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableDoubleMatrix.Rel.Dense<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.AbstractDoubleMatrix#createMatrix2D(int, int)
             */
            @Override
            protected DoubleMatrix2D createMatrix2D(final int rows, final int columns)
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
             * @param values
             * @param unit
             */
            protected Sparse(final DoubleMatrix2D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                this.copyOnWrite = true;
                initialize(values); // shallow copy
            }

            /**
             * Create a new Sparse Relative Mutable DoubleMatrix.
             * @param values
             * @param unit
             * @throws ValueException
             */
            public Sparse(final double[][] values, final U unit) throws ValueException
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
            public Sparse(final DoubleScalar.Rel<U>[][] values) throws ValueException
            {
                super(checkNonEmpty(values)[0][0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Create an immutable version.
             * @return Sparse Relative Immutable DoubleMatrix
             */
            public DoubleMatrix.Rel.Sparse<U> immutable()
            {
                this.copyOnWrite = true;
                return new DoubleMatrix.Rel.Sparse<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrix#mutable()
             */
            public MutableDoubleMatrix.Rel.Sparse<U> mutable()
            {
                this.copyOnWrite = true;
                return new MutableDoubleMatrix.Rel.Sparse<U>(this.matrixSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.matrix.AbstractDoubleMatrix#createMatrix2D(int, int)
             */
            @Override
            protected DoubleMatrix2D createMatrix2D(final int rows, final int columns)
            {
                return new SparseDoubleMatrix2D(rows, columns);
            }

        }

        /**
         * @see org.opentrafficsim.core.value.vdouble.matrix.MutableDoubleMatrix#get(int, int)
         */
        @Override
        public DoubleScalar.Rel<U> get(final int row, final int column) throws ValueException
        {
            return new DoubleScalar.Rel<U>(getInUnit(row, column, this.unit), this.unit);
        }

    }

    /**
     * Make (immutable) DoubleMatrix equivalent for any type of MutableDoubleMatrix.
     * @return DoubleMatrix
     */
    public abstract DoubleMatrix<U> immutable();

    /**
     * @see org.opentrafficsim.core.value.Value#copy()
     */
    public MutableDoubleMatrix<U> copy()
    {
        return immutable().mutable(); // Almost as simple as the copy in DoubleMatrix
    }

    /**
     * Check the copyOnWrite flag and, if it is set make a deep copy of the data and clear the flag.
     */
    protected void checkCopyOnWrite()
    {
        if (this.copyOnWrite)
        {
            // System.out.println("copyOnWrite is set: Copying data");
            this.matrixSI = this.matrixSI.copy(); // makes a deep copy, using multithreading
            this.copyOnWrite = false;
        }
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.Matrix.WriteDoubleMatrixFunctions#setSI(int, double)
     */
    @Override
    public void setSI(final int row, final int column, final double valueSI) throws ValueException
    {
        checkIndex(row, column);
        checkCopyOnWrite();
        safeSet(row, column, valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.Matrix.WriteDoubleMatrixFunctions#set(int,
     *      org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar)
     */
    @Override
    public void set(final int row, final int column, final DoubleScalar<U> value) throws ValueException
    {
        setSI(row, column, value.getValueSI());
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.Matrix.WriteDoubleMatrixFunctions#setInUnit(int, double,
     *      org.opentrafficsim.core.unit.Unit)
     */
    @Override
    public void setInUnit(final int row, final int column, final double value, final U valueUnit) throws ValueException
    {
        // TODO: creating a DoubleScalarAbs along the way may not be the most efficient way to do this...
        setSI(row, column, new DoubleScalar.Abs<U>(value, valueUnit).getValueSI());
    }

    /**
     * Execute a function on a cell by cell basis.
     * @param f cern.colt.function.tdouble.DoubleFunction; the function to apply
     */
    public void assign(final cern.colt.function.tdouble.DoubleFunction f)
    {
        checkCopyOnWrite();
        this.matrixSI.assign(f);
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
     * Increment the values in this MutableDoubleMatrix by the corresponding values in an DoubleMatrix.
     * @param increment AbstractDoubleMatrix; contains the amounts by which to increment the corresponding entries in
     *            this MutableDoubleMatrix
     * @return this
     * @throws ValueException
     */
    private MutableDoubleMatrix<U> incrementValueByValue(DoubleMatrix<U> increment) throws ValueException
    {
        checkSizeAndCopyOnWrite(increment);
        for (int row = this.rows(); --row >= 0;)
            for (int column = this.columns(); --column >= 0;)
                safeSet(row, column, safeGet(row, column) + increment.safeGet(row, column));
        return this;
    }

    /**
     * Increment the entries in this MutableDoubleMatrix by the corresponding values in a Relative DoubleMatrix
     * @param rel
     * @return this
     * @throws ValueException
     */
    public MutableDoubleMatrix<U> incrementBy(DoubleMatrix.Rel<U> rel) throws ValueException
    {
        return incrementValueByValue(rel);
    }

    /**
     * Decrement the values in this MutableDoubleMatrix by the corresponding values in an DoubleMatrix.
     * @param decrement AbstractDoubleMatrix; contains the amounts by which to decrement the corresponding entries in
     *            this MutableDoubleMatrix
     * @return this
     * @throws ValueException
     */
    private MutableDoubleMatrix<U> decrementValueByValue(DoubleMatrix<U> decrement) throws ValueException
    {
        checkSizeAndCopyOnWrite(decrement);
        for (int row = rows(); --row >= 0;)
            for (int column = columns(); --column >= 0;)
                safeSet(row, column, safeGet(row, column) - decrement.safeGet(row, column));
        return this;
    }

    /**
     * Decrement the entries in this MutableDoubleMatrix by the corresponding values in a Dense Relative DoubleMatrix
     * @param rel
     * @return this
     * @throws ValueException
     */
    public MutableDoubleMatrix<U> decrementBy(DoubleMatrix.Rel<U> rel) throws ValueException
    {
        return decrementValueByValue(rel);
    }

    /**
     * Decrement the entries in this MutableDoubleMatrix by the corresponding values in a Dense Absolute DoubleMatrix
     * @param abs
     * @return this
     * @throws ValueException
     */
    public MutableDoubleMatrix<U> decrementBy(DoubleMatrix.Abs<U> abs) throws ValueException
    {
        return decrementValueByValue(abs);
    }

    /**
     * Scale the values in this MutableDoubleMatrix by the corresponding values in an DoubleMatrix.
     * @param factor AbstractDoubleMatrix; contains the values by which to scale the corresponding entries in this
     *            MutableDoubleMatrix
     * @throws ValueException
     */
    public void scaleValueByValue(DoubleMatrix<?> factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int row = this.rows(); --row >= 0;)
            for (int column = this.columns(); --column >= 0;)
                safeSet(row, column, safeGet(row, column) * factor.safeGet(row, column));
    }

    /**
     * Scale the values in this MutableDoubleMatrix by the corresponding values in a double array.
     * @param factor double[][]; contains the values by which to scale the corresponding entries in this
     *            MutableDoubleMatrix
     * @return this
     * @throws ValueException
     */
    public MutableDoubleMatrix<U> scaleValueByValue(double[][] factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int row = this.rows(); --row >= 0;)
            for (int column = this.columns(); --column >= 0;)
                safeSet(row, column, safeGet(row, column) * factor[row][column]);
        return this;
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other AbstractDoubleMatrix; partner for the size check
     * @throws ValueException
     */
    private void checkSizeAndCopyOnWrite(DoubleMatrix<?> other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other double[][]; partner for the size check
     * @throws ValueException
     */
    private void checkSizeAndCopyOnWrite(double[][] other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Add two DoubleMatrices entry by entry
     * @param left Absolute Dense DoubleMatrix
     * @param right Relative DoubleMatrix
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> plus(final DoubleMatrix.Abs.Dense<U> left,
            final DoubleMatrix.Rel<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleMatrices entry by entry
     * @param left Absolute Sparse DoubleMatrix
     * @param right Relative Sparse DoubleMatrix
     * @return new Absolute Sparse Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> plus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleMatrices entry by entry
     * @param left Absolute Sparse DoubleMatrix
     * @param right Relative Dense DoubleMatrix
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> plus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Add two DoubleMatrices entry by entry
     * @param left Relative Dense DoubleMatrix
     * @param right Relative DoubleMatrix
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> plus(final DoubleMatrix.Rel.Dense<U> left,
            final DoubleMatrix.Rel<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleMatrices entry by entry
     * @param left Relative Sparse DoubleMatrix
     * @param right Relative Sparse DoubleMatrix
     * @return new Relative Sparse Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> plus(final DoubleMatrix.Rel.Sparse<U> left,
            final DoubleMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Sparse<U>) left.mutable().incrementBy(right);
    }

    /**
     * Add two DoubleMatrices entry by entry
     * @param left Relative Sparse DoubleMatrix
     * @param right Relative Dense DoubleMatrix
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> plus(final DoubleMatrix.Rel.Sparse<U> left,
            final DoubleMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) sparseToDense(left).incrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry
     * @param left Absolute Dense DoubleMatrix
     * @param right Absolute DoubleMatrix
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> minus(final DoubleMatrix.Abs.Dense<U> left,
            final DoubleMatrix.Abs<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) new MutableDoubleMatrix.Rel.Dense<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry
     * @param left Absolute Sparse DoubleMatrix
     * @param right Absolute Sparse DoubleMatrix
     * @return new Relative Sparse Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> minus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Abs.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Sparse<U>) new MutableDoubleMatrix.Rel.Sparse<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry
     * @param left Absolute Sparse DoubleMatrix
     * @param right Absolute Dense DoubleMatrix
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> minus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Abs.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) new MutableDoubleMatrix.Rel.Dense<U>(left.deepCopyOfData(),
                left.getUnit()).decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry
     * @param left Absolute Dense DoubleMatrix
     * @param right Relative DoubleMatrix
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> minus(final DoubleMatrix.Abs.Dense<U> left,
            final DoubleMatrix.Rel<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry
     * @param left Absolute Sparse DoubleMatrix
     * @param right Relative Sparse DoubleMatrix
     * @return new Absolute Sparse Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> minus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry
     * @param left Absolute Sparse DoubleMatrix
     * @param right Relative Dense DoubleMatrix
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> minus(final DoubleMatrix.Abs.Sparse<U> left,
            final DoubleMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Sparse<U>) sparseToDense(left).decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry
     * @param left Relative Dense DoubleMatrix
     * @param right Relative DoubleMatrix
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> minus(final DoubleMatrix.Rel.Dense<U> left,
            final DoubleMatrix.Rel<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry
     * @param left Relative Sparse DoubleMatrix
     * @param right Relative Sparse DoubleMatrix
     * @return new Relative Sparse Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> minus(final DoubleMatrix.Rel.Sparse<U> left,
            final DoubleMatrix.Rel.Sparse<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Sparse<U>) left.mutable().decrementBy(right);
    }

    /**
     * Subtract two DoubleMatrices entry by entry
     * @param left Relative Sparse DoubleMatrix
     * @param right Relative Dense DoubleMatrix
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> minus(final DoubleMatrix.Rel.Sparse<U> left,
            final DoubleMatrix.Rel.Dense<U> right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) sparseToDense(left).decrementBy(right);
    }

    /**
     * Multiply two DoubleMatricess entry by entry
     * @param left Absolute Dense DoubleMatrix
     * @param right Absolute Dense DoubleMatrix
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException
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
     * Multiply two DoubleMatrices entry by entry
     * @param left Relative Dense DoubleMatrix
     * @param right Relative Dense DoubleMatrix
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException
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
     * Multiply two DoubleMatrices entry by entry
     * @param left Absolute Sparse DoubleMatrix
     * @param right Absolute DoubleMatrix
     * @return new Absolute Sparse Mutable DoubleMatrix
     * @throws ValueException
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
     * Multiply two DoubleMatrices entry by entry
     * @param left Relative Sparse DoubleMatrix
     * @param right Relative DoubleMatrix
     * @return new Relative Sparse Mutable DoubleMatrix
     * @throws ValueException
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
     * Multiply the values in a DoubleMatrix by the corresponding values in a double array.
     * @param left Absolute Dense DoubleMatrix
     * @param right double[][]
     * @return new Absolute Dense Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> times(final DoubleMatrix.Abs.Dense<U> left,
            final double[][] right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleMatrix by the corresponding values in a double array.
     * @param left Relative Dense DoubleMatrix
     * @param right double[][]
     * @return new Relative Dense Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> times(final DoubleMatrix.Rel.Dense<U> left,
            final double[][] right) throws ValueException
    {
        return (MutableDoubleMatrix.Rel.Dense<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleMatrix by the corresponding values in a double array.
     * @param left Absolute Sparse DoubleMatrix
     * @param right double[][]
     * @return new Sparse Absolute Mutable DoubleMatrix
     * @throws ValueException
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> times(final DoubleMatrix.Abs.Sparse<U> left,
            final double[][] right) throws ValueException
    {
        return (MutableDoubleMatrix.Abs.Sparse<U>) left.mutable().scaleValueByValue(right);
    }

    /**
     * Multiply the values in a DoubleMatrix by the corresponding values in a double array.
     * @param left Relative Sparse DoubleMatrix
     * @param right double[][]
     * @return new Relative Sparse Mutable DoubleMatrix
     * @throws ValueException
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
    private static DoubleMatrix2D makeSparse(DoubleMatrix2D dense)
    {
        DoubleMatrix2D result = new SparseDoubleMatrix2D(dense.rows(), dense.columns());
        result.assign(dense);
        return result;
    }

    /**
     * Create a Sparse version of this Dense DoubleMatrix. <br />
     * @param in DoubleMatrix.Abs.Dense the Dense DoubleMatrix
     * @return MutableDoubleMatrix.Sparse.Abs
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Sparse<U> denseToSparse(final DoubleMatrix.Abs.Dense<U> in)
    {
        return new MutableDoubleMatrix.Abs.Sparse<U>(makeSparse(in.matrixSI), in.getUnit());
    }

    /**
     * Create a Sparse version of this Dense DoubleMatrix. <br />
     * @param in DoubleMatrix.Rel.Dense the Dense DoubleMatrix
     * @return MutableDoubleMatrix.Abs.Sparse
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Sparse<U> denseToSparse(final DoubleMatrix.Rel.Dense<U> in)
    {
        return new MutableDoubleMatrix.Rel.Sparse<U>(makeSparse(in.matrixSI), in.getUnit());
    }

    /**
     * Make the Dense equivalent of a SparseDoubleMatrix2D.
     * @param dense DenseDoubleMatrix2D
     * @return DenseDoubleMatrix2D
     */
    private static DoubleMatrix2D makeDense(DoubleMatrix2D sparse)
    {
        DoubleMatrix2D result = new SparseDoubleMatrix2D(sparse.rows(), sparse.columns());
        result.assign(sparse);
        return result;
    }

    /**
     * Create a Dense version of this Sparse DoubleMatrix. <br />
     * @param in DoubleMatrix.Abs.Dense the Dense DoubleMatrix
     * @return MutableDoubleMatrix.Abs.Sparse
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Abs.Dense<U> sparseToDense(final DoubleMatrix.Abs.Sparse<U> in)
    {
        return new MutableDoubleMatrix.Abs.Dense<U>(makeDense(in.matrixSI), in.getUnit());
    }

    /**
     * Create a Dense version of this Sparse DoubleMatrix. <br />
     * @param in DoubleMatrix.Abs.Dense the Dense DoubleMatrix
     * @return MutableDoubleMatrix.Abs.Dense
     */
    public static <U extends Unit<U>> MutableDoubleMatrix.Rel.Dense<U> sparseToDense(final DoubleMatrix.Rel.Sparse<U> in)
    {
        return new MutableDoubleMatrix.Rel.Dense<U>(makeDense(in.matrixSI), in.getUnit());
    }

}

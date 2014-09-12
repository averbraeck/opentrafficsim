package org.opentrafficsim.core.value.vdouble.vector;

import java.io.Serializable;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.AbstractValue;
import org.opentrafficsim.core.value.DenseData;
import org.opentrafficsim.core.value.Format;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.SparseData;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.ValueUtil;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;

/**
 * Immutable double vector.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.opentrafficsim.org/"> www.opentrafficsim.org</a>.
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
 * @version Jun 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> The unit for this DoubleVector
 */
public abstract class DoubleVector<U extends Unit<U>> extends AbstractValue<U> implements Serializable,
        ReadOnlyDoubleVectorFunctions<U>
{
    /** the internal storage for the vector; internally they are stored in SI units; can be dense or sparse. */
    protected DoubleMatrix1D vectorSI;

    /**
     * @param unit
     */
    protected DoubleVector(U unit)
    {
        super(unit);
        // System.out.println("Created DoubleVector");
    }

    /** */
    private static final long serialVersionUID = 20140618L;

    /**
     * @param <U> Unit
     */
    public abstract static class Abs<U extends Unit<U>> extends DoubleVector<U> implements Absolute
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a Dense.
         * @param unit
         */
        Abs(U unit)
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
             * @param values
             * @param unit
             */
            protected Dense(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values); // shallow copy
            }

            /**
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
             * Make a mutable version.
             * @return Absolute Dense MutableDoubleVector
             */
            public MutableDoubleVector.Abs.Dense<U> mutable()
            {
                return new MutableDoubleVector.Abs.Dense<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.AbstractDoubleVector#createMatrix1D(int)
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
             * For package internal use only
             * @param values
             * @param unit
             */
            protected Sparse(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values); // shallow copy
            }

            /**
             * Create a Dense Relative Immutable DoubleVector
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
            public Sparse(final DoubleScalar.Abs<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Create a mutable version.
             * @return Dense Relative Mutable DoubleVector
             */
            public MutableDoubleVector.Abs.Sparse<U> mutable()
            {
                return new MutableDoubleVector.Abs.Sparse<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.AbstractDoubleVector#createMatrix1D(int)
             */
            @Override
            protected DoubleMatrix1D createMatrix1D(final int size)
            {
                return new DenseDoubleMatrix1D(size);
            }

        }

        /**
         * @see org.opentrafficsim.core.value.vdouble.vector.ReadOnlyDoubleVectorFunctions#get(int)
         */
        @Override
        public DoubleScalar.Abs<U> get(final int index) throws ValueException
        {
            return new DoubleScalar.Abs<U>(getInUnit(index, this.unit), this.unit);
        }

    }

    /**
     * @param <U> Unit
     */
    public abstract static class Rel<U extends Unit<U>> extends DoubleVector<U> implements Relative
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a Rel
         * @param unit
         */
        Rel(U unit)
        {
            super(unit);
        }

        /**
         * @param <U>
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
                initialize(values); // shallow copy
            }

            /**
             * @param values
             * @param unit
             */
            public Dense(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values); // shallow copy
            }

            /**
             * @param values
             * @param unit
             * @throws ValueException
             */
            public Dense(final DoubleScalar.Rel<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Construct a mutable version containing the same data.
             * @return Dense Absolute MutableDoubleVector
             */
            public MutableDoubleVector.Rel.Dense<U> mutable()
            {
                return new MutableDoubleVector.Rel.Dense<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.AbstractDoubleVector#createMatrix1D(int)
             */
            @Override
            protected DoubleMatrix1D createMatrix1D(final int size)
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
             * For package internal use only
             * @param values
             * @param unit
             */
            protected Sparse(final DoubleMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values); // shallow copy
            }

            /**
             * Create a new Sparse Relative Immutable DoubleVector
             * @param values
             * @param unit
             */
            public Sparse(final double[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values); // shallow copy
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
             * Construct a mutable version.
             * @return Sparse Relative Mutable DoubleVector
             */
            public MutableDoubleVector.Rel.Sparse<U> mutable()
            {
                return new MutableDoubleVector.Rel.Sparse<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vdouble.vector.AbstractDoubleVector#createMatrix1D(int)
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
     * Create a mutable version of this DoubleVector. <br />
     * The mutable version is created with a shallow copy of the data and the internal copyOnWrite flag set. The first
     * operation in the mutable version that modifies the data shall trigger a deep copy of the data.
     * @return MutableDoubleVector; mutable version of this DoubleVector
     */
    public abstract MutableDoubleVector<U> mutable();

    /**
     * @see org.opentrafficsim.core.value.Value#copy()
     */
    public DoubleVector<U> copy()
    {
        return this; // That was easy!
    }

    /**
     * Import the values and convert them into SI units.
     * @param values an array of values
     */
    protected void initialize(final double[] values)
    {
        this.vectorSI = createMatrix1D(values.length);
        if (this.unit.equals(this.unit.getStandardUnit()))
        {
            this.vectorSI.assign(values);
        }
        else
        {
            for (int index = 0; index < values.length; index++)
            {
                safeSet(index, expressAsSIUnit(values[index]));
            }
        }
    }

    /**
     * @param values
     */
    protected void initialize(final DoubleMatrix1D values)
    {
        this.vectorSI = values;
    }

    /**
     * Construct the vector and store the values in SI units.
     * @param values an array of values for the constructor
     * @throws ValueException exception thrown when array with zero elements is offered
     */
    protected void initialize(final DoubleScalar<U>[] values) throws ValueException
    {
        this.vectorSI = createMatrix1D(values.length);
        for (int index = 0; index < values.length; index++)
        {
            safeSet(index, values[index].getValueSI());
        }
    }

    /**
     * This method has to be implemented by each leaf class.
     * @param size the number of cells in the vector
     * @return an instance of the right type of matrix (absolute / relative, dense / sparse, etc.).
     */
    protected abstract DoubleMatrix1D createMatrix1D(final int size);

    /**
     * Create a double[] array filled with the values in SI unit.
     * @return double[]; array of values in SI unit
     */
    public double[] getValuesSI()
    {
        return this.vectorSI.toArray(); // this makes a deep copy
    }

    /**
     * Create a double[] array filled with the values in the original unit.
     * @return values in original unit
     */
    public double[] getValuesInUnit()
    {
        return getValuesInUnit(this.unit);
    }

    /**
     * Create a double[] array filled with the values in the specified unit.
     * @param targetUnit the unit to convert the values to
     * @return values in specific target unit
     */
    public double[] getValuesInUnit(final U targetUnit)
    {
        double[] values = this.vectorSI.toArray();
        for (int i = 0; i < values.length; i++)
            values[i] = ValueUtil.expressAsUnit(values[i], targetUnit);
        return values;
    }

    public int size()
    {
        return (int) this.vectorSI.size();
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.ReadOnlyDoubleVectorFunctions#getSI(int)
     */
    public double getSI(final int index) throws ValueException
    {
        checkIndex(index);
        return safeGet(index);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.ReadOnlyDoubleVectorFunctions#getInUnit(int)
     */
    public double getInUnit(final int index) throws ValueException
    {
        return expressAsSpecifiedUnit(getSI(index));
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.ReadOnlyDoubleVectorFunctions#getInUnit(int,
     *      org.opentrafficsim.core.unit.Unit)
     */
    public double getInUnit(final int index, final U targetUnit) throws ValueException
    {
        return ValueUtil.expressAsUnit(getSI(index), targetUnit);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.ReadOnlyDoubleVectorFunctions#zSum()
     */
    public double zSum()
    {
        return this.vectorSI.zSum();
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vectormut.Vectormut#cardinality()
     */
    @Override
    public int cardinality()
    {
        return this.vectorSI.cardinality();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj)
    {
        // unequal if object is of a different type.
        if (!(obj instanceof DoubleVector<?>))
            return false;
        DoubleVector<?> fv = (DoubleVector<?>) obj;

        // unequal if the SI unit type differs (km/h and m/s could have the same content, so that is allowed)
        if (!this.getUnit().getStandardUnit().equals(fv.getUnit().getStandardUnit()))
            return false;

        // unequal if one is absolute and the other is relative
        if (this.isAbsolute() != fv.isAbsolute() || this.isRelative() != fv.isRelative())
            return false;

        // Colt's equals also tests the size of the vector
        return this.vectorSI.equals(fv.vectorSI);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return toString(this.unit);
    }

    /**
     * Print this AbstractDoubleVector with the values expressed in the specified unit.
     * @param displayUnit the unit to display the vector in.
     * @return a printable String with the vector contents
     */
    public String toString(final U displayUnit)
    {
        StringBuffer buf = new StringBuffer();
        if (this instanceof MutableDoubleVector)
        {
            buf.append("Mutable   ");
            if (this instanceof MutableDoubleVector.Abs.Dense)
                buf.append("Abs Dense  ");
            else if (this instanceof MutableDoubleVector.Rel.Dense)
                buf.append("Rel Dense  ");
            else if (this instanceof MutableDoubleVector.Abs.Sparse)
                buf.append("Abs Sparse ");
            else if (this instanceof MutableDoubleVector.Rel.Sparse)
                buf.append("Rel Sparse ");
            else
                buf.append("??? ");
        }
        else
        {
            buf.append("Immutable ");
            if (this instanceof DoubleVector.Abs.Dense)
                buf.append("Abs Dense  ");
            else if (this instanceof DoubleVector.Rel.Dense)
                buf.append("Rel Dense  ");
            else if (this instanceof DoubleVector.Abs.Sparse)
                buf.append("Abs Sparse ");
            else if (this instanceof DoubleVector.Rel.Sparse)
                buf.append("Rel Sparse ");
            else
                buf.append("??? ");
        }
        buf.append("[" + displayUnit.getAbbreviation() + "]");
        for (int i = 0; i < this.vectorSI.size(); i++)
        {
            double f = ValueUtil.expressAsUnit(safeGet(i), displayUnit);
            buf.append(" " + Format.format(f));
        }
        return buf.toString();
    }

    /**
     * Centralized size equality check.
     * @param other DoubleVector<U>; other DoubleVector
     * @throws ValueException when vectors have unequal size
     */
    protected void checkSize(final DoubleVector<?> other) throws ValueException
    {
        if (size() != other.size())
            throw new ValueException("The vectors have different sizes: " + size() + " != " + other.size());
    }

    /**
     * Centralized size equality check.
     * @param other double[]; array of double
     * @throws ValueException when vectors have unequal size
     */
    protected void checkSize(final double[] other) throws ValueException
    {
        if (size() != other.length)
            throw new ValueException("The vector and the array have different sizes: " + size() + " != " + other.length);
    }

    /**
     * Check that a provided index is valid.
     * @param index integer; the value to check
     * @throws ValueException
     */
    protected void checkIndex(final int index) throws ValueException
    {
        if (index < 0 || index >= this.vectorSI.size())
            throw new ValueException("index out of range (valid range is 0.." + (this.vectorSI.size() - 1) + ", got "
                    + index + ")");
    }

    /**
     * Retrieve a value in vectorSI without checking validity of the index.
     * @param index integer; the index
     * @return double; the value stored at that index
     */
    protected double safeGet(final int index)
    {
        return this.vectorSI.getQuick(index);
    }

    /**
     * Modify a value in vectorSI without checking validity of the index.
     * @param index integer; the index
     * @param valueSI double; the new value for the entry in vectorSI
     */
    protected void safeSet(final int index, final double valueSI)
    {
        this.vectorSI.setQuick(index, valueSI);
    }

    /**
     * Create a deep copy of the data.
     * @return DoubleMatrix1D; deep copy of the data
     */
    protected DoubleMatrix1D deepCopyOfData()
    {
        return this.vectorSI.copy();
    }

    /**
     * Check that a provided array can be used to create some descendant of an AbstractDoubleVector.
     * @param fsArray DoubleScalar[]; the provided array
     * @return DoubleScalar[]; the provided array
     * @throws ValueException
     */
    protected static <U extends Unit<U>> DoubleScalar<U>[] checkNonEmpty(DoubleScalar<U>[] fsArray) throws ValueException
    {
        if (0 == fsArray.length)
            throw new ValueException(
                    "Cannot create a DoubleValue or MutableDoubleValue from an empty array of DoubleScalar");
        return fsArray;
    }

}

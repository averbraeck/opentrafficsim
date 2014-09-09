package org.opentrafficsim.core.value.vfloat.vector;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.DenseData;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.SparseData;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix1D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix1D;

/**
 * Immutable float vector.
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
 * @param <U> The unit for this FloatVector
 */
public abstract class FloatVector<U extends Unit<U>> extends AbstractFloatVector<U>
{
    /**
     * @param unit
     */
    protected FloatVector(U unit)
    {
        super(unit);
        // System.out.println("Created FloatVector");
    }

    /** */
    private static final long serialVersionUID = 20140618L;

    /**
     * @param <U> Unit
     */
    public abstract static class Dense<U extends Unit<U>> extends FloatVector<U> implements DenseData
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
        public static class Abs<U extends Unit<U>> extends FloatVector<U> implements Absolute
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
                initialize(values); // shallow copy
            }

            /**
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
             * Make a mutable version.
             * @return Dense Absolute MutableFloatVector
             */
            public MutableFloatVector.Dense.Abs<U> mutable()
            {
                return new MutableFloatVector.Dense.Abs<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.vector.ReadOnlyFloatVectorFunctions#get(int)
             */
            @Override
            public FloatScalar<U> get(final int index) throws ValueException
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
        public static class Rel<U extends Unit<U>> extends FloatVector<U> implements Relative
        {
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * For package internal use only
             * @param values
             * @param unit
             */
            protected Rel(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Rel");
                initialize(values); // shallow copy
            }

            /**
             * Create a Dense Relative Immutable FloatVector
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
             * Create a mutable version.
             * @return Dense Relative Mutable FloatVector
             */
            public MutableFloatVector.Dense.Rel<U> mutable()
            {
                return new MutableFloatVector.Dense.Rel<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.vector.ReadOnlyFloatVectorFunctions#get(int)
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
    public abstract static class Sparse<U extends Unit<U>> extends FloatVector<U> implements SparseData
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a Sparse
         * @param unit
         */
        private Sparse(U unit)
        {
            super(unit);
            throw new Error("There is never a need to create a Sparse");
        }

        /**
         * @param <U>
         */
        public static class Abs<U extends Unit<U>> extends FloatVector<U> implements Absolute
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
                initialize(values); // shallow copy
            }

            /**
             * @param values
             * @param unit
             */
            public Abs(final float[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Abs");
                initialize(values); // shallow copy
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
             * Construct a mutable version containing the same data.
             * @return Dense Absolute MutableFloatVector
             */
            public MutableFloatVector.Sparse.Abs<U> mutable()
            {
                return new MutableFloatVector.Sparse.Abs<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.vector.ReadOnlyFloatVectorFunctions#get(int)
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
        public static class Rel<U extends Unit<U>> extends FloatVector<U> implements Relative
        {
            /** */
            private static final long serialVersionUID = 20140905L;

            /**
             * For package internal use only
             * @param values
             * @param unit
             */
            protected Rel(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Rel");
                initialize(values); // shallow copy
            }

            /**
             * Create a new Sparse Relative Immutable FloatVector
             * @param values
             * @param unit
             */
            public Rel(final float[] values, final U unit)
            {
                super(unit);
                // System.out.println("Created Rel");
                initialize(values); // shallow copy
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
             * Construct a mutable version.
             * @return Sparse Relative Mutable FloatVector
             */
            public MutableFloatVector.Sparse.Rel<U> mutable()
            {
                return new MutableFloatVector.Sparse.Rel<U>(this.vectorSI, this.unit);
            }

            /**
             * @see org.opentrafficsim.core.value.vfloat.vector.ReadOnlyFloatVectorFunctions#get(int)
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
                return new SparseFloatMatrix1D(size);
            }

        }
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.vector.ReadOnlyFloatVectorFunctions#get(int)
     */
    @Override
    public FloatScalar<U> get(int index) throws ValueException
    {
        return null;
    }

    /**
     * Create a mutable version of this FloatVector. <br />
     * The mutable version is created with a shallow copy of the data and the internal copyOnWrite flag set. The first
     * operation in the mutable version that modifies the data shall trigger a deep copy of the data.
     * @return MutableFloatVector; mutable version of this FloatVector
     */
    public abstract MutableFloatVector<U> mutable();

    /**
     * @see org.opentrafficsim.core.value.Value#copy()
     */
    public FloatVector<U> copy()
    {
        return this; // That was easy!
    }

}

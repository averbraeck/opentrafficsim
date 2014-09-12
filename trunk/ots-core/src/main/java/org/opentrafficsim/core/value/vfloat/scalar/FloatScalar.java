package org.opentrafficsim.core.value.vfloat.scalar;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.Scalar;
import org.opentrafficsim.core.value.ValueUtil;

/**
 * All calculations are according to IEEE 754. This means that division by zero results in Float.INFINITY, and some
 * calculations could result in NaN. No changes have been made to avoid this, as it is the standard behavior of Java for
 * floating point numbers.
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
 * @param <U> the unit of the values in the constructor and for display
 */
public abstract class FloatScalar<U extends Unit<U>> extends Scalar<U>
{
    /** */
    private static final long serialVersionUID = 20140618L;

    /**
     * @param unit
     */
    public FloatScalar(U unit)
    {
        super(unit);
    }

    /** the value, stored in SI units. */
    protected float valueSI;

    /**
     * @param <U> Unit
     */
    public static class Abs<U extends Unit<U>> extends FloatScalar<U> implements Absolute
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a new Absolute FloatScalar.
         * @param value float; the value of the new Absolute FloatScalar
         * @param unit Unit; the unit of the new Absolute FloatScalar
         */
        public Abs(final float value, final U unit)
        {
            super(unit);
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Create a new Absolute FloatScalar from an existing one.
         * @param value Absolute FloatScalar; the reference
         */
        public Abs(final FloatScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Create a new Absolute FloatScalar from an existing Absolute MutableFloatScalar.
         * @param value Absolute MutableFloatScalar; the reference
         */
        public Abs(final MutableFloatScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Create an mutable version of this FloatScalar
         * @return Absolute MutableFloatScalar
         */
        @Override
        public MutableFloatScalar.Abs<U> mutable()
        {
            return new MutableFloatScalar.Abs<U>(this);
        }

        /**
         * @see org.opentrafficsim.core.value.vfloat.scalar.FloatScalar#copy()
         */
        @Override
        public FloatScalar.Abs<U> copy()
        {
            return this; // that was easy!
        }

    }

    /**
     * @param <U> Unit
     */
    public static class Rel<U extends Unit<U>> extends FloatScalar<U> implements Relative
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a new Relative FloatScalar.
         * @param value float; the value of the new Relative FloatScalar
         * @param unit Unit; the unit of the new Relative FloatScalar
         */
        public Rel(final float value, final U unit)
        {
            super(unit);
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Create a new Relative FloatScalar from an existing one.
         * @param value Relative FloatScalar; the reference
         */
        public Rel(final FloatScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Create a new Relative FloatScalar from an existing Relative MutableFloatScalar.
         * @param value Relative MutableFloatScalar; the reference
         */
        public Rel(final MutableFloatScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Create a mutable version.
         * @return Relative MutableFloatScalar
         */
        @Override
        public MutableFloatScalar.Rel<U> mutable()
        {
            return new MutableFloatScalar.Rel<U>(this);
        }

        /**
         * @see org.opentrafficsim.core.value.vfloat.scalar.FloatScalar#copy()
         */
        @Override
        public FloatScalar.Rel<U> copy()
        {
            return this; // that was easy!
        }

    }

    /**
     * Initialize the valueSI field (performing conversion to the SI standard unit if needed).
     * @param value float; the value in the unit of this FloatScalar
     */
    protected void initialize(float value)
    {
        if (this.unit.equals(this.unit.getStandardUnit()))
        {
            this.valueSI = value;
        }
        else
        {
            this.valueSI = (float) expressAsSIUnit(value);
        }
    }

    /**
     * Initialize the valueSI field. As the provided value is already in the SI standard unit, conversion is never
     * necessary.
     * @param value FloatScalar; the value to use for initialization
     */
    protected void initialize(FloatScalar<U> value)
    {
        this.valueSI = value.valueSI;
    }

    /**
     * @return value in SI units
     */
    public float getValueSI()
    {
        return this.valueSI;
    }

    /**
     * @return value in original units
     */
    public float getValueInUnit()
    {
        return (float) expressAsSpecifiedUnit(this.valueSI);
    }

    /**
     * @param targetUnit the unit to convert the value to
     * @return value in specific target unit
     */
    public float getValueInUnit(final U targetUnit)
    {
        return (float) ValueUtil.expressAsUnit(this.valueSI, targetUnit);
    }

    /**********************************************************************************/
    /******************************** NUMBER METHODS **********************************/
    /**********************************************************************************/

    /**
     * @see java.lang.Number#intValue()
     */
    @Override
    public int intValue()
    {
        return Math.round(this.valueSI);
    }

    /**
     * @see java.lang.Number#longValue()
     */
    @Override
    public long longValue()
    {
        return Math.round(this.valueSI);
    }

    /**
     * @see java.lang.Number#floatValue()
     */
    @Override
    public float floatValue()
    {
        return this.valueSI;
    }

    /**
     * @see java.lang.Number#doubleValue()
     */
    @Override
    public double doubleValue()
    {
        return this.valueSI;
    }

    /**
     * Create a mutable version of this FloatScalar. <br />
     * The mutable version is created as a deep copy of this. Delayed copying is not worthwhile for a Scalar.
     * @return MutableFloatScalar; mutable version of this FloatScalar
     */
    public abstract MutableFloatScalar<U> mutable();

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj)
    {
        // unequal if object is of a different type.
        if (!(obj instanceof FloatScalar<?>))
            return false;
        FloatScalar<?> fs = (FloatScalar<?>) obj;

        // unequal if the SI unit type differs (km/h and m/s could have the same content, so that is allowed)
        if (!this.getUnit().getStandardUnit().equals(fs.getUnit().getStandardUnit()))
            return false;

        // unequal if one is absolute and the other is relative
        if (this.isAbsolute() != fs.isAbsolute() || this.isRelative() != fs.isRelative())
            return false;

        return this.valueSI == fs.valueSI;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return this.getValueInUnit() + " " + this.unit.getAbbreviationKey();
    }

    /**
     * @see org.opentrafficsim.core.value.Value#copy()
     */
    public abstract FloatScalar<U> copy();

}
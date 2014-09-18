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
     * Create a new FloatScalar.
     * @param unit Unit; the unit of the new FloatScalar
     */
    public FloatScalar(final U unit)
    {
        super(unit);
    }

    /** the value, stored in SI units. */
    protected float valueSI;

    /**
     * @param <U> Unit
     */
    public static class Abs<U extends Unit<U>> extends FloatScalar<U> implements Absolute, Comparable<Abs<U>>
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a new Absolute Immutable FloatScalar.
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
         * Create a new Absolute Immutable FloatScalar from an existing one.
         * @param value Absolute FloatScalar; the reference
         */
        public Abs(final FloatScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Create a new Absolute Immutable FloatScalar from an existing Absolute MutableFloatScalar.
         * @param value Absolute MutableFloatScalar; the reference
         */
        public Abs(final MutableFloatScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * @see org.opentrafficsim.core.value.vfloat.scalar.FloatScalar#mutable()
         */
        @Override
        public MutableFloatScalar.Abs<U> mutable()
        {
            return new MutableFloatScalar.Abs<U>(this);
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(final Abs<U> o)
        {
            return new Float(this.valueSI).compareTo(o.valueSI);
        }

        /**
         * @see org.opentrafficsim.core.value.Value#copy()
         */
        @Override
        public FloatScalar.Abs<U> copy()
        {
            return this;
        }

    }

    /**
     * @param <U> Unit
     */
    public static class Rel<U extends Unit<U>> extends FloatScalar<U> implements Relative, Comparable<Rel<U>>
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a new Relative Immutable FloatScalar.
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
         * Create a new Relative Immutable FloatScalar from an existing one.
         * @param value Relative FloatScalar; the reference
         */
        public Rel(final FloatScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Create a new Relative Immutable FloatScalar from an existing Relative MutableFloatScalar.
         * @param value Relative MutableFloatScalar; the reference
         */
        public Rel(final MutableFloatScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * @see org.opentrafficsim.core.value.vfloat.scalar.FloatScalar#mutable()
         */
        @Override
        public final MutableFloatScalar.Rel<U> mutable()
        {
            return new MutableFloatScalar.Rel<U>(this);
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public final int compareTo(final Rel<U> o)
        {
            return new Float(this.valueSI).compareTo(o.valueSI);
        }

        /**
         * @see org.opentrafficsim.core.value.Value#copy()
         */
        @Override
        public final FloatScalar.Rel<U> copy()
        {
            return this;
        }

    }

    /**
     * Create a mutable version of this FloatScalar. <br />
     * The mutable version is created as a deep copy of this. Delayed copying is not worthwhile for a Scalar.
     * @return MutableFloatScalar; mutable version of this FloatScalar
     */
    public abstract MutableFloatScalar<U> mutable();

    /**
     * Initialize the valueSI field (performing conversion to the SI standard unit if needed).
     * @param value float; the value in the unit of this FloatScalar
     */
    protected final void initialize(final float value)
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
    protected final void initialize(final FloatScalar<U> value)
    {
        this.valueSI = value.valueSI;
    }

    /**
     * Retrieve the value in the underlying SI unit.
     * @return value in SI units
     */
    public final float getValueSI()
    {
        return this.valueSI;
    }

    /**
     * Retrieve the value in the original unit.
     * @return value in original units
     */
    public final float getValueInUnit()
    {
        return (float) expressAsSpecifiedUnit(this.valueSI);
    }

    /**
     * @param targetUnit the unit to convert the value to
     * @return value in specific target unit
     */
    public final float getValueInUnit(final U targetUnit)
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
    public final long longValue()
    {
        return Math.round(this.valueSI);
    }

    /**
     * @see java.lang.Number#floatValue()
     */
    @Override
    public final float floatValue()
    {
        return this.valueSI;
    }

    /**
     * @see java.lang.Number#doubleValue()
     */
    @Override
    public final double doubleValue()
    {
        return this.valueSI;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString()
    {
        return this.getValueInUnit() + " " + this.unit.getAbbreviationKey();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(this.valueSI);
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof FloatScalar))
        {
            return false;
        }
        FloatScalar<?> other = (FloatScalar<?>) obj;
        // unequal if one is absolute and the other is relative
        if (this.isAbsolute() != other.isAbsolute() || this.isRelative() != other.isRelative())
        {
            return false;
        }
        // unequal if the SI unit type differs (km/h and m/s could have the same content, so that is allowed)
        if (!this.getUnit().getStandardUnit().equals(other.getUnit().getStandardUnit()))
        {
            return false;
        }
        if (Float.floatToIntBits(this.valueSI) != Float.floatToIntBits(other.valueSI))
        {
            return false;
        }
        return true;
    }

}

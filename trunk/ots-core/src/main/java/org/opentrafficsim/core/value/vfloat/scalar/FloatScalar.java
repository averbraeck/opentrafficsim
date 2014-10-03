package org.opentrafficsim.core.value.vfloat.scalar;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.Scalar;
import org.opentrafficsim.core.value.ValueUtil;

/**
 * Immutable FloatScalar.
 * <p>
 * All calculations are according to IEEE 754. This means that division by zero results in Float.INFINITY, and some calculations
 * could result in NaN. No changes have been made to avoid this, as it is the standard behavior of Java for floating point
 * numbers.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> the unit of the values in the constructor and for display
 */
public abstract class FloatScalar<U extends Unit<U>> extends Scalar<U>
{
    /** */
    private static final long serialVersionUID = 20140618L;

    /** The value, stored in the standard SI unit. */
    private float valueSI;

    /**
     * Construct a new Immutable FloatScalar.
     * @param unit U; the unit of the new FloatScalar
     */
    protected FloatScalar(final U unit)
    {
        super(unit);
    }

    /**
     * @param <U> Unit
     */
    public static class Abs<U extends Unit<U>> extends FloatScalar<U> implements Absolute, Comparable<Abs<U>>
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Construct a new Absolute Immutable FloatScalar.
         * @param value float; the value of the new Absolute Immutable FloatScalar
         * @param unit U; the unit of the new Absolute Immutable FloatScalar
         */
        public Abs(final float value, final U unit)
        {
            super(unit);
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Construct a new Absolute Immutable FloatScalar from an existing Absolute Immutable FloatScalar.
         * @param value FloatScalar.Abs&lt;U&gt;; the reference
         */
        public Abs(final FloatScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Construct a new Absolute Immutable FloatScalar from an existing Absolute MutableFloatScalar.
         * @param value MutableFloatScalar.Abs&lt;U&gt;; the reference
         */
        public Abs(final MutableFloatScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatScalar.Abs<U> mutable()
        {
            return new MutableFloatScalar.Abs<U>(this);
        }

        /** {@inheritDoc} */
        @Override
        public final int compareTo(final Abs<U> o)
        {
            return new Float(getValueSI()).compareTo(o.getValueSI());
        }

        /** {@inheritDoc} */
        @Override
        public final FloatScalar.Abs<U> copy()
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
         * Construct a new Relative Immutable FloatScalar.
         * @param value float; the value of the new Relative Immutable FloatScalar
         * @param unit U; the unit of the new Relative Immutable FloatScalar
         */
        public Rel(final float value, final U unit)
        {
            super(unit);
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Construct a new Relative Immutable FloatScalar from an existing Relative Immutable FloatScalar.
         * @param value FloatScalar.Rel&lt;U&gt;; the reference
         */
        public Rel(final FloatScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Construct a new Relative Immutable FloatScalar from an existing Relative MutableFloatScalar.
         * @param value MutableFloatScalar.Rel&lt;U&gt;; the reference
         */
        public Rel(final MutableFloatScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Rel");
            initialize(value);
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatScalar.Rel<U> mutable()
        {
            return new MutableFloatScalar.Rel<U>(this);
        }

        /** {@inheritDoc} */
        @Override
        public final int compareTo(final Rel<U> o)
        {
            return new Float(getValueSI()).compareTo(o.getValueSI());
        }

        /** {@inheritDoc} */
        @Override
        public final FloatScalar.Rel<U> copy()
        {
            return this;
        }

    }

    /**
     * Create a mutable version of this FloatScalar. <br>
     * The mutable version is created as a deep copy of this. Delayed copying is not worthwhile for a Scalar.
     * @return MutableFloatScalar&lt;U&gt;
     */
    public abstract MutableFloatScalar<U> mutable();

    /**
     * Initialize the valueSI field (performing conversion to the SI standard unit if needed).
     * @param value float; the value in the unit of this FloatScalar
     */
    protected final void initialize(final float value)
    {
        if (this.getUnit().equals(this.getUnit().getStandardUnit()))
        {
            this.valueSI = value;
        }
        else
        {
            this.valueSI = (float) expressAsSIUnit(value);
        }
    }

    /**
     * Initialize the valueSI field. As the provided value is already in the SI standard unit, conversion is never necessary.
     * @param value FloatScalar&lt;U&gt;; the value to use for initialization
     */
    protected final void initialize(final FloatScalar<U> value)
    {
        setValueSI(value.getValueSI());
    }

    /**
     * Retrieve the value in the underlying SI unit.
     * @return float
     */
    public final float getValueSI()
    {
        return this.valueSI;
    }

    /**
     * Set the value in the underlying SI unit.
     * @param value float; the new value in the underlying SI unit
     */
    protected final void setValueSI(final float value)
    {
        this.valueSI = value;
    }

    /**
     * Retrieve the value in the original unit.
     * @return float
     */
    public final float getValueInUnit()
    {
        return (float) expressAsSpecifiedUnit(this.valueSI);
    }

    /**
     * Retrieve the value converted into some specified unit.
     * @param targetUnit U; the unit to convert the value into
     * @return float
     */
    public final float getValueInUnit(final U targetUnit)
    {
        return (float) ValueUtil.expressAsUnit(this.valueSI, targetUnit);
    }

    /**********************************************************************************/
    /********************************* NUMBER METHODS *********************************/
    /**********************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final int intValue()
    {
        return Math.round(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final long longValue()
    {
        return Math.round(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final float floatValue()
    {
        return this.valueSI;
    }

    /** {@inheritDoc} */
    @Override
    public final double doubleValue()
    {
        return this.valueSI;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return this.getValueInUnit() + " " + this.getUnit().getAbbreviationKey();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(this.valueSI);
        return result;
    }

    /** {@inheritDoc} */
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
        // unequal if not both Absolute or both Relative
        if (this.isAbsolute() != other.isAbsolute() || this.isRelative() != other.isRelative())
        {
            return false;
        }
        // unequal if the underlying standard SI unit is different
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

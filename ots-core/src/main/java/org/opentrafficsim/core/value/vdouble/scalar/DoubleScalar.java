package org.opentrafficsim.core.value.vdouble.scalar;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.Scalar;
import org.opentrafficsim.core.value.ValueUtil;

/**
 * All calculations are according to IEEE 754. This means that division by zero results in Double.INFINITY, and some
 * calculations could result in NaN. No changes have been made to avoid this, as it is the standard behavior of Java for
 * floating point numbers.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> the unit of the values in the constructor and for display
 */
public abstract class DoubleScalar<U extends Unit<U>> extends Scalar<U>
{
    /** */
    private static final long serialVersionUID = 20140618L;

    /** The value, stored in the standard SI unit. */
    private double valueSI;

    /**
     * Construct a new Immutable DoubleScalar.
     * @param unit U; the unit of the new DoubleScalar
     */
    protected DoubleScalar(final U unit)
    {
        super(unit);
    }

    /**
     * @param <U> Unit
     */
    public static class Abs<U extends Unit<U>> extends DoubleScalar<U> implements Absolute, Comparable<Abs<U>>
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Construct a new Absolute Immutable DoubleScalar.
         * @param value double; the value of the new Absolute Immutable DoubleScalar
         * @param unit U; the unit of the new Absolute Immutable DoubleScalar
         */
        public Abs(final double value, final U unit)
        {
            super(unit);
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Construct a new Absolute Immutable DoubleScalar from an existing Absolute Immutable DoubleScalar.
         * @param value DoubleScalar.Abs<U>; the reference
         */
        public Abs(final DoubleScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Construct a new Absolute Immutable DoubleScalar from an existing Absolute MutableDoubleScalar.
         * @param value MutableDoubleScalar.Abs<U>; the reference
         */
        public Abs(final MutableDoubleScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /** {@inheritDoc} */
        @Override
        public final MutableDoubleScalar.Abs<U> mutable()
        {
            return new MutableDoubleScalar.Abs<U>(this);
        }

        /** {@inheritDoc} */
        @Override
        public final int compareTo(final Abs<U> o)
        {
            return new Double(getValueSI()).compareTo(o.getValueSI());
        }

        /** {@inheritDoc} */
        @Override
        public final DoubleScalar.Abs<U> copy()
        {
            return this;
        }

    }

    /**
     * @param <U> Unit
     */
    public static class Rel<U extends Unit<U>> extends DoubleScalar<U> implements Relative, Comparable<Rel<U>>
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Construct a new Relative Immutable DoubleScalar.
         * @param value double; the value of the new Relative Immutable DoubleScalar
         * @param unit U; the unit of the new Relative Immutable DoubleScalar
         */
        public Rel(final double value, final U unit)
        {
            super(unit);
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Construct a new Relative Immutable DoubleScalar from an existing Relative Immutable DoubleScalar.
         * @param value DoubleScalar.Rel<U>; the reference
         */
        public Rel(final DoubleScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Construct a new Relative Immutable DoubleScalar from an existing Relative MutableDoubleScalar.
         * @param value MutableDoubleScalar.Rel<U>; the reference
         */
        public Rel(final MutableDoubleScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Rel");
            initialize(value);
        }

        /** {@inheritDoc} */
        @Override
        public final MutableDoubleScalar.Rel<U> mutable()
        {
            return new MutableDoubleScalar.Rel<U>(this);
        }

        /** {@inheritDoc} */
        @Override
        public final int compareTo(final Rel<U> o)
        {
            return new Double(getValueSI()).compareTo(o.getValueSI());
        }

        /** {@inheritDoc} */
        @Override
        public final DoubleScalar.Rel<U> copy()
        {
            return this;
        }

    }

    /**
     * Create a mutable version of this DoubleScalar. <br>
     * The mutable version is created as a deep copy of this. Delayed copying is not worthwhile for a Scalar.
     * @return MutableDoubleScalar<U>
     */
    public abstract MutableDoubleScalar<U> mutable();

    /**
     * Initialize the valueSI field (performing conversion to the SI standard unit if needed).
     * @param value double; the value in the unit of this DoubleScalar
     */
    protected final void initialize(final double value)
    {
        if (this.getUnit().equals(this.getUnit().getStandardUnit()))
        {
            this.valueSI = value;
        }
        else
        {
            this.valueSI = expressAsSIUnit(value);
        }
    }

    /**
     * Initialize the valueSI field. As the provided value is already in the SI standard unit, conversion is never necessary.
     * @param value DoubleScalar<U>; the value to use for initialization
     */
    protected final void initialize(final DoubleScalar<U> value)
    {
        setValueSI(value.getValueSI());
    }

    /**
     * Retrieve the value in the underlying SI unit.
     * @return double
     */
    public final double getValueSI()
    {
        return this.valueSI;
    }

    /**
     * Set the value in the underlying SI unit.
     * @param value double; the new value in the underlying SI unit
     */
    protected final void setValueSI(final double value)
    {
        this.valueSI = value;
    }
    
    /**
     * Retrieve the value in the original unit.
     * @return double
     */
    public final double getValueInUnit()
    {
        return expressAsSpecifiedUnit(this.valueSI);
    }

    /**
     * Retrieve the value converted into some specified unit.
     * @param targetUnit U; the unit to convert the value into
     * @return double
     */
    public final double getValueInUnit(final U targetUnit)
    {
        return ValueUtil.expressAsUnit(this.valueSI, targetUnit);
    }

    /**********************************************************************************/
    /********************************* NUMBER METHODS *********************************/
    /**********************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final int intValue()
    {
        return (int) Math.round(this.valueSI);
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
        return (float) this.valueSI;
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
        long temp;
        temp = Double.doubleToLongBits(this.valueSI);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        if (!(obj instanceof DoubleScalar))
        {
            return false;
        }
        DoubleScalar<?> other = (DoubleScalar<?>) obj;
        // unequal if not both absolute or both relative
        if (this.isAbsolute() != other.isAbsolute() || this.isRelative() != other.isRelative())
        {
            return false;
        }
        // unequal if the underlying standard SI unit is different
        if (!this.getUnit().getStandardUnit().equals(other.getUnit().getStandardUnit()))
        {
            return false;
        }
        if (Double.doubleToLongBits(this.valueSI) != Double.doubleToLongBits(other.valueSI))
        {
            return false;
        }
        return true;
    }

}

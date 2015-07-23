package org.opentrafficsim.core.value.vdouble.scalar;

import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.Format;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.Scalar;
import org.opentrafficsim.core.value.ValueUtil;

/**
 * Immutable DoubleScalar.
 * <p>
 * This file was generated by the OpenTrafficSim value classes generator, 26 jun, 2015
 * <p>
 * Copyright (c) 2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 26 jun, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit; the unit of this DoubleScalar
 */
public abstract class DoubleScalar<U extends Unit<U>> extends Scalar<U>
{
    /**  */
    private static final long serialVersionUID = 20150626L;

    /** The value, stored in the standard SI unit. */
    private double valueSI;

    /**
     * Construct a new Immutable DoubleScalar.
     * @param unit U; the unit of the new DoubleScalar
     */
    protected DoubleScalar(final U unit)
    {
        super(unit);
        // System.out.println("Created DoubleScalar");
    }

    /**
     * @param <U> Unit
     */
    public static class Abs<U extends Unit<U>> extends DoubleScalar<U> implements Absolute, Comparable<Abs<U>>
    {
        /**  */
        private static final long serialVersionUID = 20150626L;

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
         * @param value DoubleScalar.Abs&lt;U&gt;; the reference
         */
        public Abs(final DoubleScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Construct a new Absolute Immutable DoubleScalar from an existing Absolute MutableDoubleScalar.
         * @param value MutableDoubleScalar.Abs&lt;U&gt;; the reference
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
            return new Double(getSI()).compareTo(o.getSI());
        }

        /** {@inheritDoc} */
        @Override
        public final DoubleScalar.Abs<U> copy()
        {
            return this;
        }

        /**
         * Test if this DoubleScalar.Abs&lt;U&gt; is less than a DoubleScalar.Abs&lt;U&gt;.
         * @param o DoubleScalar.Abs&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean lt(final DoubleScalar.Abs<U> o)
        {
            return this.getSI() < o.getSI();
        }

        /**
         * Test if this DoubleScalar.Abs&lt;U&gt; is less than or equal to a DoubleScalar.Abs&lt;U&gt;.
         * @param o DoubleScalar.Abs&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean le(final DoubleScalar.Abs<U> o)
        {
            return this.getSI() <= o.getSI();
        }

        /**
         * Test if this DoubleScalar.Abs&lt;U&gt; is greater than or equal to a DoubleScalar.Abs&lt;U&gt;.
         * @param o DoubleScalar.Abs&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean gt(final DoubleScalar.Abs<U> o)
        {
            return this.getSI() > o.getSI();
        }

        /**
         * Test if this DoubleScalar.Abs&lt;U&gt; is greater than a DoubleScalar.Abs&lt;U&gt;.
         * @param o DoubleScalar.Abs&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean ge(final DoubleScalar.Abs<U> o)
        {
            return this.getSI() >= o.getSI();
        }

        /**
         * Test if this DoubleScalar.Abs&lt;U&gt; is equal to a DoubleScalar.Abs&lt;U&gt;.
         * @param o DoubleScalar.Abs&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean eq(final DoubleScalar.Abs<U> o)
        {
            return this.getSI() == o.getSI();
        }

        /**
         * Test if this DoubleScalar.Abs&lt;U&gt; is not equal to a DoubleScalar.Abs&lt;U&gt;.
         * @param o DoubleScalar.Abs&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean ne(final DoubleScalar.Abs<U> o)
        {
            return this.getSI() != o.getSI();
        }

        /**
         * Test if this DoubleScalar.Abs&lt;U&gt; is less than a MutableDoubleScalar.Abs&lt;U&gt;.
         * @param o MutableDoubleScalar.Abs&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean lt(final MutableDoubleScalar.Abs<U> o)
        {
            return this.getSI() < o.getSI();
        }

        /**
         * Test if this DoubleScalar.Abs&lt;U&gt; is less than or equal to a MutableDoubleScalar.Abs&lt;U&gt;.
         * @param o MutableDoubleScalar.Abs&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean le(final MutableDoubleScalar.Abs<U> o)
        {
            return this.getSI() <= o.getSI();
        }

        /**
         * Test if this DoubleScalar.Abs&lt;U&gt; is greater than or equal to a MutableDoubleScalar.Abs&lt;U&gt;.
         * @param o MutableDoubleScalar.Abs&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean gt(final MutableDoubleScalar.Abs<U> o)
        {
            return this.getSI() > o.getSI();
        }

        /**
         * Test if this DoubleScalar.Abs&lt;U&gt; is greater than a MutableDoubleScalar.Abs&lt;U&gt;.
         * @param o MutableDoubleScalar.Abs&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean ge(final MutableDoubleScalar.Abs<U> o)
        {
            return this.getSI() >= o.getSI();
        }

        /**
         * Test if this DoubleScalar.Abs&lt;U&gt; is equal to a MutableDoubleScalar.Abs&lt;U&gt;.
         * @param o MutableDoubleScalar.Abs&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean eq(final MutableDoubleScalar.Abs<U> o)
        {
            return this.getSI() == o.getSI();
        }

        /**
         * Test if this DoubleScalar.Abs&lt;U&gt; is not equal to a MutableDoubleScalar.Abs&lt;U&gt;.
         * @param o MutableDoubleScalar.Abs&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean ne(final MutableDoubleScalar.Abs<U> o)
        {
            return this.getSI() != o.getSI();
        }

    }

    /**
     * @param <U> Unit
     */
    public static class Rel<U extends Unit<U>> extends DoubleScalar<U> implements Relative, Comparable<Rel<U>>
    {
        /**  */
        private static final long serialVersionUID = 20150626L;

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
         * @param value DoubleScalar.Rel&lt;U&gt;; the reference
         */
        public Rel(final DoubleScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Construct a new Relative Immutable DoubleScalar from an existing Relative MutableDoubleScalar.
         * @param value MutableDoubleScalar.Rel&lt;U&gt;; the reference
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
            return new Double(getSI()).compareTo(o.getSI());
        }

        /** {@inheritDoc} */
        @Override
        public final DoubleScalar.Rel<U> copy()
        {
            return this;
        }

        /**
         * Test if this DoubleScalar.Rel&lt;U&gt; is less than a DoubleScalar.Rel&lt;U&gt;.
         * @param o DoubleScalar.Rel&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean lt(final DoubleScalar.Rel<U> o)
        {
            return this.getSI() < o.getSI();
        }

        /**
         * Test if this DoubleScalar.Rel&lt;U&gt; is less than or equal to a DoubleScalar.Rel&lt;U&gt;.
         * @param o DoubleScalar.Rel&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean le(final DoubleScalar.Rel<U> o)
        {
            return this.getSI() <= o.getSI();
        }

        /**
         * Test if this DoubleScalar.Rel&lt;U&gt; is greater than or equal to a DoubleScalar.Rel&lt;U&gt;.
         * @param o DoubleScalar.Rel&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean gt(final DoubleScalar.Rel<U> o)
        {
            return this.getSI() > o.getSI();
        }

        /**
         * Test if this DoubleScalar.Rel&lt;U&gt; is greater than a DoubleScalar.Rel&lt;U&gt;.
         * @param o DoubleScalar.Rel&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean ge(final DoubleScalar.Rel<U> o)
        {
            return this.getSI() >= o.getSI();
        }

        /**
         * Test if this DoubleScalar.Rel&lt;U&gt; is equal to a DoubleScalar.Rel&lt;U&gt;.
         * @param o DoubleScalar.Rel&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean eq(final DoubleScalar.Rel<U> o)
        {
            return this.getSI() == o.getSI();
        }

        /**
         * Test if this DoubleScalar.Rel&lt;U&gt; is not equal to a DoubleScalar.Rel&lt;U&gt;.
         * @param o DoubleScalar.Rel&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean ne(final DoubleScalar.Rel<U> o)
        {
            return this.getSI() != o.getSI();
        }

        /**
         * Test if this DoubleScalar.Rel&lt;U&gt; is less than a MutableDoubleScalar.Rel&lt;U&gt;.
         * @param o MutableDoubleScalar.Rel&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean lt(final MutableDoubleScalar.Rel<U> o)
        {
            return this.getSI() < o.getSI();
        }

        /**
         * Test if this DoubleScalar.Rel&lt;U&gt; is less than or equal to a MutableDoubleScalar.Rel&lt;U&gt;.
         * @param o MutableDoubleScalar.Rel&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean le(final MutableDoubleScalar.Rel<U> o)
        {
            return this.getSI() <= o.getSI();
        }

        /**
         * Test if this DoubleScalar.Rel&lt;U&gt; is greater than or equal to a MutableDoubleScalar.Rel&lt;U&gt;.
         * @param o MutableDoubleScalar.Rel&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean gt(final MutableDoubleScalar.Rel<U> o)
        {
            return this.getSI() > o.getSI();
        }

        /**
         * Test if this DoubleScalar.Rel&lt;U&gt; is greater than a MutableDoubleScalar.Rel&lt;U&gt;.
         * @param o MutableDoubleScalar.Rel&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean ge(final MutableDoubleScalar.Rel<U> o)
        {
            return this.getSI() >= o.getSI();
        }

        /**
         * Test if this DoubleScalar.Rel&lt;U&gt; is equal to a MutableDoubleScalar.Rel&lt;U&gt;.
         * @param o MutableDoubleScalar.Rel&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean eq(final MutableDoubleScalar.Rel<U> o)
        {
            return this.getSI() == o.getSI();
        }

        /**
         * Test if this DoubleScalar.Rel&lt;U&gt; is not equal to a MutableDoubleScalar.Rel&lt;U&gt;.
         * @param o MutableDoubleScalar.Rel&lt;U&gt;; the right hand side operand of the comparison
         * @return boolean
         */
        public final boolean ne(final MutableDoubleScalar.Rel<U> o)
        {
            return this.getSI() != o.getSI();
        }

    }

    /**
     * Create a mutable version of this DoubleScalar. <br>
     * The mutable version is created as a deep copy of this. Delayed copying is not worthwhile for a Scalar.
     * @return MutableDoubleScalar&lt;U&gt;
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
            setValueSI(value);
        }
        else
        {
            setValueSI(expressAsSIUnit(value));
        }
    }

    /**
     * Initialize the valueSI field. As the provided value is already in the SI standard unit, conversion is never
     * necessary.
     * @param value DoubleScalar&lt;U&gt;; the value to use for initialization
     */
    protected final void initialize(final DoubleScalar<U> value)
    {
        setValueSI(value.getSI());
    }

    /**
     * Retrieve the value in the underlying SI unit.
     * @return double
     */
    public final double getSI()
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
    public final double getInUnit()
    {
        return expressAsSpecifiedUnit(getSI());
    }

    /**
     * Retrieve the value converted into some specified unit.
     * @param targetUnit U; the unit to convert the value into
     * @return double
     */
    public final double getInUnit(final U targetUnit)
    {
        return ValueUtil.expressAsUnit(getSI(), targetUnit);
    }

    /**********************************************************************************/
    /********************************* NUMBER METHODS *********************************/
    /**********************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final int intValue()
    {
        return (int) Math.round(getSI());
    }

    /** {@inheritDoc} */
    @Override
    public final long longValue()
    {
        return Math.round(getSI());
    }

    /** {@inheritDoc} */
    @Override
    public final float floatValue()
    {
        return (float) getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final double doubleValue()
    {
        return getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return toString(getUnit(), false, true);
    }

    /**
     * Print this DoubleScalar with the value expressed in the specified unit.
     * @param displayUnit U; the unit into which the value is converted for display
     * @return String; printable string with the scalar contents expressed in the specified unit
     */
    public final String toString(final U displayUnit)
    {
        return toString(displayUnit, false, true);
    }

    /**
     * Print this DoubleScalar with optional type and unit information.
     * @param verbose boolean; if true; include type info; if false; exclude type info
     * @param withUnit boolean; if true; include the unit; of false; exclude the unit
     * @return String; printable string with the scalar contents
     */
    public final String toString(final boolean verbose, final boolean withUnit)
    {
        return toString(getUnit(), verbose, withUnit);
    }

    /**
     * Print this DoubleScalar with the value expressed in the specified unit.
     * @param displayUnit U; the unit into which the value is converted for display
     * @param verbose boolean; if true; include type info; if false; exclude type info
     * @param withUnit boolean; if true; include the unit; of false; exclude the unit
     * @return String; printable string with the scalar contents
     */
    public final String toString(final U displayUnit, final boolean verbose, final boolean withUnit)
    {
        StringBuffer buf = new StringBuffer();
        if (verbose)
        {
            if (this instanceof MutableDoubleScalar)
            {
                buf.append("Mutable   ");
                if (this instanceof MutableDoubleScalar.Abs)
                {
                    buf.append("Abs ");
                }
                else if (this instanceof MutableDoubleScalar.Rel)
                {
                    buf.append("Rel ");
                }
                else
                {
                    buf.append("??? ");
                }
            }
            else
            {
                buf.append("Immutable ");
                if (this instanceof DoubleScalar.Abs)
                {
                    buf.append("Abs ");
                }
                else if (this instanceof DoubleScalar.Rel)
                {
                    buf.append("Rel ");
                }
                else
                {
                    buf.append("??? ");
                }
            }
        }
        double d = ValueUtil.expressAsUnit(getSI(), displayUnit);
        buf.append(Format.format(d));
        if (withUnit)
        {
            buf.append(displayUnit.getAbbreviation());
        }
        return buf.toString();
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
        // unequal if not both Absolute or both Relative
        if (this.isAbsolute() != other.isAbsolute() || this.isRelative() != other.isRelative())
        {
            return false;
        }
        // unequal if the standard SI units differ
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

    /**********************************************************************************/
    /********************************* STATIC METHODS *********************************/
    /**********************************************************************************/

    /**
     * Add a Relative value to an Absolute value. Return a new instance of the value. The unit of the return value will
     * be the unit of the left argument.
     * @param left DoubleScalar.Abs&lt;U&gt;; the left argument
     * @param right DoubleScalar.Rel&lt;U&gt;; the right argument
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleScalar.Abs&lt;U&gt;; the sum of the values as an Absolute value
     */
    public static <U extends Unit<U>> MutableDoubleScalar.Abs<U> plus(final DoubleScalar.Abs<U> left,
            final DoubleScalar.Rel<U> right)
    {
        MutableDoubleScalar.Abs<U> result = new MutableDoubleScalar.Abs<U>(left);
        result.incrementByImpl(right);
        return result;
    }

    /**
     * Add a Relative value to a Relative value. Return a new instance of the value. The unit of the return value will
     * be the unit of the left argument.
     * @param left DoubleScalar.Rel&lt;U&gt;; the left argument
     * @param right DoubleScalar.Rel&lt;U&gt;; the right argument
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleScalar.Rel&lt;U&gt;; the sum of the values as a Relative value
     */
    public static <U extends Unit<U>> MutableDoubleScalar.Rel<U> plus(final DoubleScalar.Rel<U> left,
            final DoubleScalar.Rel<U> right)
    {
        MutableDoubleScalar.Rel<U> result = new MutableDoubleScalar.Rel<U>(left);
        result.incrementByImpl(right);
        return result;
    }

    /**
     * Subtract a Relative value from an absolute value. Return a new instance of the value. The unit of the return
     * value will be the unit of the left argument.
     * @param left DoubleScalar.Abs&lt;U&gt;; the left value
     * @param right DoubleScalar.Rel&lt;U&gt;; the right value
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleScalar.Abs&lt;U&gt;; the resulting value as an absolute value
     */
    public static <U extends Unit<U>> MutableDoubleScalar.Abs<U> minus(final DoubleScalar.Abs<U> left,
            final DoubleScalar.Rel<U> right)
    {
        MutableDoubleScalar.Abs<U> result = new MutableDoubleScalar.Abs<U>(left);
        result.decrementByImpl(right);
        return result;
    }

    /**
     * Subtract a relative value from a relative value. Return a new instance of the value. The unit of the value will
     * be the unit of the first argument.
     * @param left DoubleScalar.Rel&lt;U&gt;; the left value
     * @param right DoubleScalar.Rel&lt;U&gt;; the right value
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleScalar.Rel&lt;U&gt;; the resulting value as a relative value
     */
    public static <U extends Unit<U>> MutableDoubleScalar.Rel<U> minus(final DoubleScalar.Rel<U> left,
            final DoubleScalar.Rel<U> right)
    {
        MutableDoubleScalar.Rel<U> result = new MutableDoubleScalar.Rel<U>(left);
        result.decrementByImpl(right);
        return result;
    }

    /**
     * Subtract two absolute values. Return a new instance of a relative value of the difference. The unit of the value
     * will be the unit of the first argument.
     * @param valueAbs1 DoubleScalar.Abs&lt;U&gt;; value 1
     * @param valueAbs2 DoubleScalar.Abs&lt;U&gt;; value 2
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleScalar.Rel&lt;U&gt;; the difference of the two absolute values as a relative value
     */
    public static <U extends Unit<U>> MutableDoubleScalar.Rel<U> minus(final DoubleScalar.Abs<U> valueAbs1,
            final DoubleScalar.Abs<U> valueAbs2)
    {
        MutableDoubleScalar.Rel<U> result = new MutableDoubleScalar.Rel<U>(valueAbs1.getInUnit(), valueAbs1.getUnit());
        result.decrementBy(valueAbs2);
        return result;
    }

    /**
     * Multiply two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param left DoubleScalar.Abs&lt;?&gt;; the left operand
     * @param right DoubleScalar.Abs&lt;?&gt;; the right operand
     * @return MutableDoubleScalar.Abs&lt;SIUnit&gt;; the product of the two values
     */
    public static MutableDoubleScalar.Abs<SIUnit> multiply(final DoubleScalar.Abs<?> left,
            final DoubleScalar.Abs<?> right)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        return new MutableDoubleScalar.Abs<SIUnit>(left.getSI() * right.getSI(), targetUnit);
    }

    /**
     * Multiply two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param left DoubleScalar.Rel&lt;?&gt;; the left operand
     * @param right DoubleScalar.Rel&lt;?&gt;; the right operand
     * @return MutableDoubleScalar.Rel&lt;SIUnit&gt;; the product of the two values
     */
    public static MutableDoubleScalar.Rel<SIUnit> multiply(final DoubleScalar.Rel<?> left,
            final DoubleScalar.Rel<?> right)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        return new MutableDoubleScalar.Rel<SIUnit>(left.getSI() * right.getSI(), targetUnit);
    }

    /**
     * Divide two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param left DoubleScalar.Abs&lt;?&gt;; the left operand
     * @param right DoubleScalar.Abs&lt;?&gt;; the right operand
     * @return MutableDoubleScalar.Abs&lt;SIUnit&gt;; the ratio of the two values
     */
    public static MutableDoubleScalar.Abs<SIUnit> divide(final DoubleScalar.Abs<?> left, final DoubleScalar.Abs<?> right)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        return new MutableDoubleScalar.Abs<SIUnit>(left.getSI() / right.getSI(), targetUnit);
    }

    /**
     * Divide two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param left DoubleScalar.Rel&lt;?&gt;; the left operand
     * @param right DoubleScalar.Rel&lt;?&gt;; the right operand
     * @return MutableDoubleScalar.Rel&lt;SIUnit&gt;; the ratio of the two values
     */
    public static MutableDoubleScalar.Rel<SIUnit> divide(final DoubleScalar.Rel<?> left, final DoubleScalar.Rel<?> right)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        return new MutableDoubleScalar.Rel<SIUnit>(left.getSI() / right.getSI(), targetUnit);
    }

    /**
     * Interpolate between or extrapolate over two values.
     * @param zero DoubleScalar.Abs&lt;U&gt;; zero reference (returned when ratio == 0)
     * @param one DoubleScalar.Abs&lt;U&gt;; one reference (returned when ratio == 1)
     * @param ratio double; the ratio that determines where between (or outside) zero and one the result lies
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleScalar.Abs&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableDoubleScalar.Abs<U> interpolate(final DoubleScalar.Abs<U> zero,
            final DoubleScalar.Abs<U> one, final double ratio)
    {
        MutableDoubleScalar.Abs<U> result = zero.mutable();
        result.setSI(result.getSI() * (1 - ratio) + one.getSI() * ratio);
        return result;
    }

    /**
     * Interpolate between or extrapolate over two values.
     * @param zero DoubleScalar.Rel&lt;U&gt;; zero reference (returned when ratio == 0)
     * @param one DoubleScalar.Rel&lt;U&gt;; one reference (returned when ratio == 1)
     * @param ratio double; the ratio that determines where between (or outside) zero and one the result lies
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleScalar.Rel&lt;U&gt;
     */
    public static <U extends Unit<U>> MutableDoubleScalar.Rel<U> interpolate(final DoubleScalar.Rel<U> zero,
            final DoubleScalar.Rel<U> one, final double ratio)
    {
        MutableDoubleScalar.Rel<U> result = zero.mutable();
        result.setSI(result.getSI() * (1 - ratio) + one.getSI() * ratio);
        return result;
    }

}

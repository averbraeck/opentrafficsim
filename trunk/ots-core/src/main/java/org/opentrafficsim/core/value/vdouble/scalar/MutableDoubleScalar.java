package org.opentrafficsim.core.value.vdouble.scalar;

import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.ValueUtil;
import org.opentrafficsim.core.value.vdouble.DoubleMathFunctions;

// FIXME The compareTo methods look wrong because they don't check for object, nor for Absolute vs. Relative.

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 5, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit
 */
public abstract class MutableDoubleScalar<U extends Unit<U>> extends DoubleScalar<U> implements DoubleMathFunctions
{
    /** */
    private static final long serialVersionUID = 20140905L;

    /**
     * Construct a new MutableDoubleScalar.
     * @param unit U; the unit of the new MutableDoubleScalar
     */
    protected MutableDoubleScalar(final U unit)
    {
        super(unit);
    }

    /**
     * @param <U> Unit
     */
    public static class Abs<U extends Unit<U>> extends MutableDoubleScalar<U> implements Absolute, Comparable<Abs<U>>
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Construct a new Absolute MutableDoubleScalar.
         * @param value double; the value of the new Absolute MutableDoubleScalar
         * @param unit U; the unit of the new Absolute MutableDoubleScalar
         */
        public Abs(final double value, final U unit)
        {
            super(unit);
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Construct a new Absolute MutableDoubleScalar from an existing Absolute Immutable DoubleScalar.
         * @param value DoubleScalar.Abs<U>; the reference
         */
        public Abs(final DoubleScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Construct a new Absolute MutableDoubleScalar from an existing Absolute MutableDoubleScalar.
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
        public final DoubleScalar.Abs<U> immutable()
        {
            return new DoubleScalar.Abs<U>(this);
        }

        /** {@inheritDoc} */
        @Override
        public final int compareTo(final Abs<U> o)
        {
            return new Double(getValueSI()).compareTo(o.getValueSI());
        }

        /** {@inheritDoc} */
        @Override
        public final MutableDoubleScalar.Abs<U> copy()
        {
            return new MutableDoubleScalar.Abs<U>(this);
        }

    }

    /**
     * @param <U> Unit
     */
    public static class Rel<U extends Unit<U>> extends MutableDoubleScalar<U> implements Relative, Comparable<Rel<U>>
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Construct a new Relative MutableDoubleScalar.
         * @param value double; the value of the new Relative MutableDoubleScalar
         * @param unit U; the unit of the new Relative MutableDoubleScalar
         */
        public Rel(final double value, final U unit)
        {
            super(unit);
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Construct a new Relative MutableDoubleScalar from an existing Relative Immutable DoubleScalar.
         * @param value DoubleScalar.Rel<U>; the reference
         */
        public Rel(final DoubleScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Construct a new Relative MutableDoubleScalar from an existing Relative MutableDoubleScalar.
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
        public final DoubleScalar.Rel<U> immutable()
        {
            return new DoubleScalar.Rel<U>(this);
        }

        /** {@inheritDoc} */
        @Override
        public final int compareTo(final Rel<U> o)
        {
            return new Double(getValueSI()).compareTo(o.getValueSI());
        }

        /** {@inheritDoc} */
        @Override
        public final MutableDoubleScalar.Rel<U> copy()
        {
            return new MutableDoubleScalar.Rel<U>(this);
        }

    }

    /**
     * Construct an immutable version of this MutableDoubleScalar. <br>
     * The immutable version is created as a deep copy of this. Delayed copying is not worthwhile for a Scalar.
     * @return DoubleScalar<U>; immutable version of this DoubleScalar
     */
    public abstract DoubleScalar<U> immutable();

    /**
     * Replace the stored value by the supplied value which is expressed in the standard SI unit.
     * @param valueSI double; the value to store (value must already be in the standard SI unit)
     */
    final void setSI(final double valueSI)
    {
        setValueSI(valueSI);
    }

    /**
     * Replace the stored value by the supplied value.
     * @param value DoubleScalar<U>; the strongly typed value to store
     */
    final void set(final DoubleScalar<U> value)
    {
        setValueSI(value.getValueSI());
    }

    /**
     * Replace the stored value by the supplied value which can be expressed in any compatible unit.
     * @param value double; the value to store
     * @param valueUnit U; the unit of the supplied value
     */
    final void setInUnit(final double value, final U valueUnit)
    {
        setValueSI(ValueUtil.expressAsSIUnit(value, valueUnit));
    }

    /**********************************************************************************/
    /******************************* NON-STATIC METHODS *******************************/
    /**********************************************************************************/

    /**
     * Add another value to this value. Only relative values are allowed; adding an absolute value to an absolute value
     * is not allowed. Adding an absolute value to an existing relative value would require the result to become
     * absolute, which is a type change that is impossible. For that operation, use a static method.
     * @param value DoubleScalar.Rel<U>; the value to add
     */
    public final void add(final DoubleScalar.Rel<U> value)
    {
        setValueSI(getValueSI() + value.getValueSI());
    }

    /**
     * Subtract another value from this value. Only relative values are allowed; subtracting an absolute value from a
     * relative value is not allowed. Subtracting an absolute value from an existing absolute value would require the
     * result to become relative, which is a type change that is impossible. For that operation, use a static method.
     * @param value DoubleScalar.Rel<U>; the value to subtract
     */
    public final void subtract(final DoubleScalar.Rel<U> value)
    {
        setValueSI(getValueSI() - value.getValueSI());
    }

    /**********************************************************************************/
    /********************************* STATIC METHODS *********************************/
    /**********************************************************************************/

    /**
     * Increment the stored value by a specified amount.
     * @param increment DoubleScalar<?>; the amount by which to increment the stored value
     * @return DoubleScalar<?>; the modified MutableDoubleScalar
     */
    protected final DoubleScalar<?> incrementBy(final DoubleScalar<?> increment)
    {
        setValueSI(getValueSI() + increment.getValueSI());
        return this;
    }

    /**
     * Add a number of relative values to an absolute value. Return a new instance of the value. The unit of the return
     * value will be the unit of the first argument. Due to type erasure of generics, the method cannot check whether an
     * array of arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param valueAbs DoubleScalar.Abs<U>; the absolute base value
     * @param valuesRel DoubleScalar.Rel<U>...; zero or more relative values to add to the absolute value
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleScalar.Abs<U>; the sum of the values as an absolute value
     */
    @SafeVarargs
    public static <U extends Unit<U>> MutableDoubleScalar.Abs<U> plus(final DoubleScalar.Abs<U> valueAbs,
            final DoubleScalar.Rel<U>... valuesRel)
    {
        MutableDoubleScalar.Abs<U> result = new MutableDoubleScalar.Abs<U>(valueAbs);
        for (DoubleScalar.Rel<U> v : valuesRel)
        {
            result.incrementBy(v);
        }
        return result;
    }

    /**
     * Add a number of relative values. Return a new instance of the value. Due to type erasure of generics, the method
     * cannot check whether an array of arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param targetUnit U; the unit of the sum
     * @param valuesRel DoubleScalar.Rel<U>...; zero or more relative values to add together
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleScalar.Rel<U>; the sum of the values as a relative value
     */
    @SafeVarargs
    public static <U extends Unit<U>> MutableDoubleScalar.Rel<U> plus(final U targetUnit,
            final DoubleScalar.Rel<U>... valuesRel)
    {
        MutableDoubleScalar.Rel<U> result = new MutableDoubleScalar.Rel<U>(0.0, targetUnit);
        for (DoubleScalar.Rel<U> v : valuesRel)
        {
            result.incrementBy(v);
        }
        return result;
    }

    /**
     * Decrement the stored value by a specified amount.
     * @param decrement DoubleScalar<?>; the amount by which to decrement the stored value
     * @return DoubleScalar<?>; the modified MutableDoubleScalar
     */
    protected final DoubleScalar<?> decrementBy(final DoubleScalar<?> decrement)
    {
        setValueSI(getValueSI() - decrement.getValueSI());
        return this;
    }

    /**
     * Subtract a number of relative values from an absolute value. Return a new instance of the value. The unit of the
     * return value will be the unit of the first argument. Due to type erasure of generics, the method cannot check
     * whether an array of arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param valueAbs DoubleScalar.Abs<U>; the absolute base value
     * @param valuesRel DoubleScalar.Rel<U>...; zero or more relative values to subtract from the absolute value
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleScalar.Abs<U>; the resulting value as an absolute value
     */
    @SafeVarargs
    public static <U extends Unit<U>> MutableDoubleScalar.Abs<U> minus(final DoubleScalar.Abs<U> valueAbs,
            final DoubleScalar.Rel<U>... valuesRel)
    {
        MutableDoubleScalar.Abs<U> result = new MutableDoubleScalar.Abs<U>(valueAbs);
        for (DoubleScalar.Rel<U> v : valuesRel)
        {
            result.decrementBy(v);
        }
        return result;
    }

    /**
     * Subtract a number of relative values from a relative value. Return a new instance of the value. The unit of the
     * value will be the unit of the first argument. Due to type erasure of generics, the method cannot check whether an
     * array of arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param valueRel DoubleScalar.Rel<U>; the relative base value
     * @param valuesRel DoubleScalar.Rel<U>...; zero or more relative values to subtract from the first value
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleScalar.Rel<U>; the resulting value as a relative value
     */
    @SafeVarargs
    public static <U extends Unit<U>> MutableDoubleScalar.Rel<U> minus(final DoubleScalar.Rel<U> valueRel,
            final DoubleScalar.Rel<U>... valuesRel)
    {
        MutableDoubleScalar.Rel<U> result = new MutableDoubleScalar.Rel<U>(valueRel);
        for (DoubleScalar.Rel<U> v : valuesRel)
        {
            result.decrementBy(v);
        }
        return result;
    }

    /**
     * Subtract two absolute values. Return a new instance of a relative value of the difference. The unit of the value
     * will be the unit of the first argument.
     * @param valueAbs1 DoubleScalar.Abs<U>; value 1
     * @param valueAbs2 DoubleScalar.Abs<U>; value 2
     * @param <U> Unit; the unit of the parameters and the result
     * @return MutableDoubleScalar.Rel<U>; the difference of the two absolute values as a relative value
     */
    public static <U extends Unit<U>> MutableDoubleScalar.Rel<U> minus(final DoubleScalar.Abs<U> valueAbs1,
            final DoubleScalar.Abs<U> valueAbs2)
    {
        MutableDoubleScalar.Rel<U> result =
                new MutableDoubleScalar.Rel<U>(valueAbs1.getValueInUnit(), valueAbs1.getUnit());
        result.decrementBy(valueAbs2);
        return result;
    }

    /**
     * Multiply two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param left DoubleScalar.Abs<?>; the left operand
     * @param right DoubleScalar.Abs<?>; the right operand
     * @return MutableDoubleScalar.Abs<SIUnit>; the product of the two values
     */
    public static MutableDoubleScalar.Abs<SIUnit> multiply(final DoubleScalar.Abs<?> left,
            final DoubleScalar.Abs<?> right)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        return new MutableDoubleScalar.Abs<SIUnit>(left.getValueSI() * right.getValueSI(), targetUnit);
    }

    /**
     * Multiply two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param left DoubleScalar.Rel<?>; the left operand
     * @param right DoubleScalar.Rel<?>; the right operand
     * @return MutableDoubleScalar.Rel<SIUnit>; the product of the two values
     */
    public static MutableDoubleScalar.Rel<SIUnit> multiply(final DoubleScalar.Rel<?> left,
            final DoubleScalar.Rel<?> right)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        return new MutableDoubleScalar.Rel<SIUnit>(left.getValueSI() * right.getValueSI(), targetUnit);
    }

    /**
     * Divide two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param left DoubleScalar.Abs<?>; the left operand
     * @param right DoubleScalar.Abs<?>; the right operand
     * @return MutableDoubleScalar.Abs<SIUnit>; the ratio of the two values
     */
    public static MutableDoubleScalar.Abs<SIUnit> divide(final DoubleScalar.Abs<?> left, final DoubleScalar.Abs<?> right)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        return new MutableDoubleScalar.Abs<SIUnit>(left.getValueSI() / right.getValueSI(), targetUnit);
    }

    /**
     * Divide two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param left DoubleScalar.Rel<?>; the left operand
     * @param right DoubleScalar.Rel<?>; the right operand
     * @return MutableDoubleScalar.Rel<SIUnit>; the ratio of the two values
     */
    public static MutableDoubleScalar.Rel<SIUnit> divide(final DoubleScalar.Rel<?> left, final DoubleScalar.Rel<?> right)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(left.getUnit().getSICoefficients(),
                        right.getUnit().getSICoefficients()).toString());
        return new MutableDoubleScalar.Rel<SIUnit>(left.getValueSI() / right.getValueSI(), targetUnit);
    }

    /**********************************************************************************/
    /********************************** MATH METHODS **********************************/
    /**********************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final void abs()
    {
        setValueSI(Math.abs(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void acos()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.acos(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void asin()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.asin(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void atan()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.atan(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void cbrt()
    {
        // TODO: dimension for all SI coefficients / 3.
        setValueSI(Math.cbrt(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void ceil()
    {
        setValueSI(Math.ceil(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void cos()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.cos(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void cosh()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.cosh(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void exp()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.exp(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void expm1()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.expm1(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void floor()
    {
        setValueSI(Math.floor(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void log()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.log(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void log10()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.log10(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void log1p()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.log1p(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void pow(final double x)
    {
        // TODO: SI unit with coefficients * x.
        setValueSI(Math.pow(getValueSI(), x));
    }

    /** {@inheritDoc} */
    @Override
    public final void rint()
    {
        setValueSI(Math.rint(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void round()
    {
        setValueSI(Math.round(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void signum()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.signum(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void sin()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.sin(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void sinh()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.sinh(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void sqrt()
    {
        // TODO: unit coefficients / 2.
        setValueSI(Math.sqrt(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void tan()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.tan(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void tanh()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        setValueSI(Math.tanh(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void toDegrees()
    {
        setValueSI(Math.toDegrees(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void toRadians()
    {
        setValueSI(Math.toRadians(getValueSI()));
    }

    /** {@inheritDoc} */
    @Override
    public final void inv()
    {
        // TODO: negate all coefficients in the Unit.
        setValueSI(1.0 / getValueSI());
    }

    /** {@inheritDoc} */
    @Override
    public final void multiply(final double constant)
    {
        setValueSI(getValueSI() * constant);
    }

    /** {@inheritDoc} */
    @Override
    public final void divide(final double constant)
    {
        setValueSI(getValueSI() / constant);
    }
    
}

package org.opentrafficsim.core.value.vdouble.scalar;

import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.ValueUtil;
import org.opentrafficsim.core.value.vdouble.DoubleMathFunctions;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 5, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit
 */
public abstract class MutableDoubleScalar<U extends Unit<U>> extends DoubleScalar<U> implements DoubleMathFunctions
{

    // FIXME The compareTo methods look wrong because they don't check for object, nor for Absolute vs. Relative.

    /** */
    private static final long serialVersionUID = 20140905L;

    /**
     * Create a new MutableDoubleScalar.
     * @param unit Unit; the unit of the new MutableDoubleScalar
     */
    public MutableDoubleScalar(final U unit)
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
         * Create a new Absolute MutableDoubleScalar.
         * @param value double; the value of the new Absolute MutableDoubleScalar
         * @param unit Unit; the unit of the new Absolute MutableDoubleScalar
         */
        protected Abs(final double value, final U unit)
        {
            super(unit);
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Create a new Absolute MutableDoubleScalar from an existing immutable one.
         * @param value DoubleScalar.Abs; the reference
         */
        public Abs(final DoubleScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Create a new Absolute MutableDoubleScalar from an existing one.
         * @param value MutableDoubleScalar.Abs; the existing Absolute MutableDoubleScalar
         */
        public Abs(final MutableDoubleScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /** {@inheritDoc} */
        @Override
        public final DoubleScalar.Abs<U> immutable()
        {
            return new DoubleScalar.Abs<U>(this);
        }

        /** {@inheritDoc} */
        @Override
        public final MutableDoubleScalar.Abs<U> mutable()
        {
            return new MutableDoubleScalar.Abs<U>(this);
        }

        /** {@inheritDoc} */
        @Override
        public final MutableDoubleScalar.Abs<U> copy()
        {
            return new MutableDoubleScalar.Abs<U>(this);
        }

        /** {@inheritDoc} */
        @Override
        public final int compareTo(final Abs<U> o)
        {
            return new Double(this.valueSI).compareTo(o.valueSI);
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
         * Create a new Relative MutableDoubleScalar.
         * @param value double; the value of the new Relative MutableDoubleScalar
         * @param unit Unit of the new Relative MutableDoubleScalar
         */
        public Rel(final double value, final U unit)
        {
            super(unit);
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Create a new Relative MutableDoubleScalar.
         * @param value DoubleScalar.Rel; the value of the new Relative MutableDoubleScalar
         */
        public Rel(final DoubleScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Create a new Absolute MutableDoubleScalar from an existing one.
         * @param value DoubleScalar.Rel; the reference
         */
        public Rel(final MutableDoubleScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Rel");
            initialize(value);
        }

        /** {@inheritDoc} */
        @Override
        public final DoubleScalar.Rel<U> immutable()
        {
            return new DoubleScalar.Rel<U>(this);
        }

        /** {@inheritDoc} */
        @Override
        public final MutableDoubleScalar.Rel<U> mutable()
        {
            return new MutableDoubleScalar.Rel<U>(this);
        }

        /** {@inheritDoc} */
        @Override
        public final MutableDoubleScalar.Rel<U> copy()
        {
            return new MutableDoubleScalar.Rel<U>(this);
        }

        /** {@inheritDoc} */
        @Override
        public final int compareTo(final Rel<U> o)
        {
            return new Double(this.valueSI).compareTo(o.valueSI);
        }

    }

    /**
     * Create a mutable version of this DoubleScalar. <br>
     * The mutable version is created as a deep copy of this. Delayed copying is not worthwhile for a Scalar.
     * @return MutableDoubleScalar; mutable version of this DoubleScalar
     */
    public abstract DoubleScalar<U> immutable();

    /**
     * @param valueSI the value to store in the cell
     */
    final void setSI(final double valueSI)
    {
        this.valueSI = valueSI;
    }

    /**
     * @param value the strongly typed value to store in the cell
     */
    final void set(final DoubleScalar<U> value)
    {
        this.valueSI = value.valueSI;
    }

    /**
     * @param value the strongly typed value to store in the cell
     */
    final void set(final MutableDoubleScalar<U> value)
    {
        this.valueSI = value.valueSI;
    }

    /**
     * @param value the value to store in the cell
     * @param valueUnit the unit of the value.
     */
    final void setInUnit(final double value, final U valueUnit)
    {
        this.valueSI = ValueUtil.expressAsSIUnit(value, valueUnit);
    }

    /**********************************************************************************/
    /******************************* NON-STATIC METHODS *******************************/
    /**********************************************************************************/

    /**
     * Add another value to this value. Only relative values are allowed; adding an absolute value to an absolute value is not
     * allowed. Adding an absolute value to an existing relative value would require the result to become absolute, which is a
     * type change that is impossible. For that operation, use a static method.
     * @param value the value to add
     */
    public final void add(final DoubleScalar.Rel<U> value)
    {
        this.valueSI += value.getValueSI();
    }

    /**
     * Subtract another value from this value. Only relative values are allowed; subtracting an absolute value from a relative
     * value is not allowed. Subtracting an absolute value from an existing absolute value would require the result to become
     * relative, which is a type change that is impossible. For that operation, use a static method.
     * @param value the value to subtract
     */
    public final void subtract(final DoubleScalar.Rel<U> value)
    {
        this.valueSI -= value.getValueSI();
    }

    /**********************************************************************************/
    /********************************* STATIC METHODS *********************************/
    /**********************************************************************************/

    /**
     * Add a number of relative values to an absolute value. Return a new instance of the value. The unit of the return value
     * will be the unit of the first argument. Because of type erasure of generics, the method cannot check whether an array of
     * arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param valueAbs the absolute base value
     * @param valuesRel zero or more values to add to the absolute value
     * @return the sum of the values as an absolute value
     * @param <U> Unit; the unit
     */
    @SafeVarargs
    public static <U extends Unit<U>> MutableDoubleScalar.Abs<U> plus(final DoubleScalar.Abs<U> valueAbs,
            final DoubleScalar.Rel<U>... valuesRel)
    {
        MutableDoubleScalar.Abs<U> result = new MutableDoubleScalar.Abs<U>(valueAbs);
        for (DoubleScalar.Rel<U> v : valuesRel)
        {
            result.valueSI += v.valueSI;
        }
        return result;
    }

    /**
     * Add a number of relative values. Return a new instance of the value. Because of type erasure of generics, the method
     * cannot check whether an array of arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param targetUnit the unit of the sum
     * @param valuesRel zero or more values to add
     * @return the sum of the values as a relative value
     * @param <U> Unit; the unit
     */
    @SafeVarargs
    public static <U extends Unit<U>> MutableDoubleScalar.Rel<U> plus(final U targetUnit,
            final DoubleScalar.Rel<U>... valuesRel)
    {
        MutableDoubleScalar.Rel<U> result = new MutableDoubleScalar.Rel<U>(0.0f, targetUnit);
        for (DoubleScalar.Rel<U> v : valuesRel)
        {
            result.valueSI += v.valueSI;
        }
        return result;
    }

    /**
     * Subtract a number of relative values from an absolute value. Return a new instance of the value. The unit of the return
     * value will be the unit of the first argument. Because of type erasure of generics, the method cannot check whether an
     * array of arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param valueAbs the absolute base value
     * @param valuesRel zero or more values to subtract from the absolute value
     * @return the resulting value as an absolute value
     * @param <U> Unit; the unit
     */
    @SafeVarargs
    public static <U extends Unit<U>> MutableDoubleScalar.Abs<U> minus(final DoubleScalar.Abs<U> valueAbs,
            final DoubleScalar.Rel<U>... valuesRel)
    {
        MutableDoubleScalar.Abs<U> value = new MutableDoubleScalar.Abs<U>(valueAbs);
        for (DoubleScalar.Rel<U> v : valuesRel)
        {
            value.valueSI -= v.valueSI;
        }
        return value;
    }

    /**
     * Subtract a number of relative values from a relative value. Return a new instance of the value. The unit of the value
     * will be the unit of the first argument. Because of type erasure of generics, the method cannot check whether an array of
     * arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param valueRel the relative base value
     * @param valuesRel zero or more values to subtract from the first value
     * @return the resulting value as a relative value
     * @param <U> Unit; the unit
     */
    @SafeVarargs
    public static <U extends Unit<U>> MutableDoubleScalar.Rel<U> minus(final DoubleScalar.Rel<U> valueRel,
            final DoubleScalar.Rel<U>... valuesRel)
    {
        MutableDoubleScalar.Rel<U> value = new MutableDoubleScalar.Rel<U>(valueRel);
        for (DoubleScalar.Rel<U> v : valuesRel)
        {
            value.valueSI -= v.valueSI;
        }
        return value;
    }

    /**
     * Subtract two absolute values. Return a new instance of a relative value of the difference. The unit of the value will be
     * the unit of the first argument.
     * @param valueAbs1 value 1
     * @param valueAbs2 value 2
     * @param <U> Unit; the unit
     * @return the difference of the two absolute values as a relative value
     */
    public static <U extends Unit<U>> MutableDoubleScalar.Rel<U> minus(final DoubleScalar.Abs<U> valueAbs1,
            final DoubleScalar.Abs<U> valueAbs2)
    {
        return new MutableDoubleScalar.Rel<U>(valueAbs1.valueSI - valueAbs2.valueSI, valueAbs1.getUnit());
    }

    /**
     * Multiply two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueAbs1 value 1
     * @param valueAbs2 value 2
     * @return the product of the two absolute values as an absolute value
     */
    public static MutableDoubleScalar.Abs<SIUnit> multiply(final DoubleScalar.Abs<?> valueAbs1,
            final DoubleScalar.Abs<?> valueAbs2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(valueAbs1.getUnit().getSICoefficients(),
                        valueAbs2.getUnit().getSICoefficients()).toString());
        return new MutableDoubleScalar.Abs<SIUnit>(valueAbs1.valueSI * valueAbs2.valueSI, targetUnit);
    }

    /**
     * Multiply two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueRel1 value 1
     * @param valueRel2 value 2
     * @return the product of the two relative values as a relative value
     */
    public static MutableDoubleScalar.Rel<SIUnit> multiply(final DoubleScalar.Rel<?> valueRel1,
            final DoubleScalar.Rel<?> valueRel2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(valueRel1.getUnit().getSICoefficients(),
                        valueRel2.getUnit().getSICoefficients()).toString());
        return new MutableDoubleScalar.Rel<SIUnit>(valueRel1.valueSI * valueRel2.valueSI, targetUnit);
    }

    /**
     * Divide two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueAbs1 value 1
     * @param valueAbs2 value 2
     * @return the division of the two absolute values as an absolute value
     */
    public static DoubleScalar.Abs<SIUnit> divide(final DoubleScalar.Abs<?> valueAbs1, final DoubleScalar.Abs<?> valueAbs2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(valueAbs1.getUnit().getSICoefficients(),
                        valueAbs2.getUnit().getSICoefficients()).toString());
        return new DoubleScalar.Abs<SIUnit>(valueAbs1.valueSI / valueAbs2.valueSI, targetUnit);
    }

    /**
     * Divide two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueRel1 value 1
     * @param valueRel2 value 2
     * @return the division of the two two relative values as a relative value
     */
    public static DoubleScalar.Rel<SIUnit> divide(final DoubleScalar.Rel<?> valueRel1, final DoubleScalar.Rel<?> valueRel2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(valueRel1.getUnit().getSICoefficients(),
                        valueRel2.getUnit().getSICoefficients()).toString());
        return new DoubleScalar.Rel<SIUnit>(valueRel1.valueSI / valueRel2.valueSI, targetUnit);
    }

    /**********************************************************************************/
    /********************************** MATH METHODS **********************************/
    /**********************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final void abs()
    {
        this.valueSI = Math.abs(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void acos()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        this.valueSI = Math.acos(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void asin()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        this.valueSI = Math.asin(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void atan()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        this.valueSI = Math.atan(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void cbrt()
    {
        // TODO: dimension for all SI coefficients / 3.
        this.valueSI = Math.cbrt(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void ceil()
    {
        this.valueSI = Math.ceil(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void cos()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        this.valueSI = Math.cos(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void cosh()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        this.valueSI = Math.cosh(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void exp()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        this.valueSI = Math.exp(this.valueSI);
    }

    /** {@inheritDoc} */

    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void expm1()
    {
        this.valueSI = Math.expm1(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void floor()
    {
        this.valueSI = Math.floor(this.valueSI);
    }

    /** {@inheritDoc} */

    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void log()
    {
        this.valueSI = Math.log(this.valueSI);
    }

    /** {@inheritDoc} */

    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void log10()
    {
        this.valueSI = Math.log10(this.valueSI);
    }

    /** {@inheritDoc} */

    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void log1p()
    {
        this.valueSI = Math.log1p(this.valueSI);
    }

    /** {@inheritDoc} */

    // TODO: SI unit with coefficients * x.
    @Override
    public final void pow(final double x)
    {
        this.valueSI = Math.pow(this.valueSI, x);
    }

    /** {@inheritDoc} */
    @Override
    public final void rint()
    {
        this.valueSI = Math.rint(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void round()
    {
        this.valueSI = Math.round(this.valueSI);
    }

    /** {@inheritDoc} */

    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void signum()
    {
        this.valueSI = Math.signum(this.valueSI);
    }

    /** {@inheritDoc} */

    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void sin()
    {
        this.valueSI = Math.sin(this.valueSI);
    }

    /** {@inheritDoc} */

    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void sinh()
    {
        this.valueSI = Math.sinh(this.valueSI);
    }

    /** {@inheritDoc} */

    // TODO: unit coefficients / 2.
    @Override
    public final void sqrt()
    {
        this.valueSI = Math.sqrt(this.valueSI);
    }

    /** {@inheritDoc} */

    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void tan()
    {
        this.valueSI = Math.tan(this.valueSI);
    }

    /** {@inheritDoc} */

    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void tanh()
    {
        this.valueSI = Math.tanh(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void toDegrees()
    {
        this.valueSI = Math.toDegrees(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void toRadians()
    {
        this.valueSI = Math.toRadians(this.valueSI);
    }

    /** {@inheritDoc} */

    // TODO: negate all coefficients in the Unit.
    @Override
    public final void inv()
    {
        this.valueSI = 1.0f / this.valueSI;
    }

    /** {@inheritDoc} */
    @Override
    public final void multiply(final double constant)
    {
        this.valueSI *= constant;
    }

    /** {@inheritDoc} */
    @Override
    public final void divide(final double constant)
    {
        this.valueSI /= constant;
    }
}

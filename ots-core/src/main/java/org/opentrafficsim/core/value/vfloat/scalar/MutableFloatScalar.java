package org.opentrafficsim.core.value.vfloat.scalar;

import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.ValueUtil;
import org.opentrafficsim.core.value.vfloat.FloatMathFunctions;

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
 * @version Sep 5, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit
 */
public abstract class MutableFloatScalar<U extends Unit<U>> extends FloatScalar<U> implements FloatMathFunctions
{
    /** */
    private static final long serialVersionUID = 20140905L;

    /**
     * Create a new MutableFloatScalar.
     * @param unit Unit; the unit of the new MutableFloatScalar
     */
    public MutableFloatScalar(final U unit)
    {
        super(unit);
    }

    /**
     * @param <U> Unit
     */
    public static class Abs<U extends Unit<U>> extends MutableFloatScalar<U> implements Absolute, Comparable<Abs<U>>
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a new Absolute MutableFloatScalar.
         * @param value float; the value of the new Absolute MutableFloatScalar
         * @param unit Unit; the unit of the new Absolute MutableFloatScalar
         */
        protected Abs(final float value, final U unit)
        {
            super(unit);
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Create a new Absolute MutableFloatScalar from an existing immutable one.
         * @param value FloatScalar.Abs; the reference
         */
        public Abs(final FloatScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * Create a new Absolute MutableFloatScalar from an existing one.
         * @param value MutableFloatScalar.Abs; the reference
         */
        public Abs(final MutableFloatScalar.Abs<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Abs");
            initialize(value);
        }

        /**
         * @see org.opentrafficsim.core.value.vfloat.scalar.MutableFloatScalar#immutable()
         */
        @Override
        public FloatScalar.Abs<U> immutable()
        {
            return new FloatScalar.Abs<U>(this);
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
         * @see org.opentrafficsim.core.value.Value#copy()
         */
        @Override
        public MutableFloatScalar.Abs<U> copy()
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
        
    }

    /**
     * @param <U> Unit
     */
    public static class Rel<U extends Unit<U>> extends MutableFloatScalar<U> implements Relative, Comparable<Rel<U>>
    {
        /** */
        private static final long serialVersionUID = 20140905L;

        /**
         * Create a new Relative MutableFloatScalar.
         * @param value float; the value of the new Relative MutableFloatScalar
         * @param unit Unit; the unit of the new MutableFloatScalar
         */
        public Rel(final float value, final U unit)
        {
            super(unit);
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Create a new Relative MutableFloatScalar from an existing immutable one.
         * @param value FloatScalar.Rel; the reference
         */
        public Rel(final FloatScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * Create a new Relative MutableFloatScalar from an existing one.
         * @param value MutableFloatScalar.Rel; the reference
         */
        public Rel(final MutableFloatScalar.Rel<U> value)
        {
            super(value.getUnit());
            // System.out.println("Created Rel");
            initialize(value);
        }

        /**
         * @see org.opentrafficsim.core.value.vfloat.scalar.MutableFloatScalar#immutable()
         */
        @Override
        public FloatScalar.Rel<U> immutable()
        {
            return new FloatScalar.Rel<U>(this);
        }

        /**
         * @see org.opentrafficsim.core.value.vfloat.scalar.FloatScalar#mutable()
         */
        @Override
        public MutableFloatScalar.Rel<U> mutable()
        {
            return new MutableFloatScalar.Rel<U>(this);
        }

        /**
         * @see org.opentrafficsim.core.value.Value#copy()
         */
        @Override
        public final MutableFloatScalar.Rel<U> copy()
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

    }

    /**
     * Create a mutable version of this FloatScalar. <br />
     * The mutable version is created as a deep copy of this. Delayed copying is not worthwhile for a Scalar.
     * @return MutableFloatScalar; mutable version of this FloatScalar
     */
    public abstract FloatScalar<U> immutable();

    /**
     * @param valueSI the value to store in the cell
     */
    final void setSI(final float valueSI)
    {
        this.valueSI = valueSI;
    }

    /**
     * @param value the strongly typed value to store in the cell
     */
    final void set(final FloatScalar<U> value)
    {
        this.valueSI = value.valueSI;
    }

    /**
     * @param value the strongly typed value to store in the cell
     */
    final void set(final MutableFloatScalar<U> value)
    {
        this.valueSI = value.valueSI;
    }

    /**
     * @param value the value to store in the cell
     * @param valueUnit the unit of the value.
     */
    final void setInUnit(final float value, final U valueUnit)
    {
        this.valueSI = (float) ValueUtil.expressAsSIUnit(value, valueUnit);
    }

    /**********************************************************************************/
    /******************************* NON-STATIC METHODS *******************************/
    /**********************************************************************************/

    /**
     * Add another value to this value. Only relative values are allowed; adding an absolute value to an absolute value
     * is not allowed. Adding an absolute value to an existing relative value would require the result to become
     * absolute, which is a type change that is impossible. For that operation, use a static method.
     * @param value the value to add
     */
    public final void add(final FloatScalar.Rel<U> value)
    {
        this.valueSI += value.getValueSI();
    }

    /**
     * Subtract another value from this value. Only relative values are allowed; subtracting an absolute value from a
     * relative value is not allowed. Subtracting an absolute value from an existing absolute value would require the
     * result to become relative, which is a type change that is impossible. For that operation, use a static method.
     * @param value the value to subtract
     */
    public final void subtract(final FloatScalar.Rel<U> value)
    {
        this.valueSI -= value.getValueSI();
    }

    /**********************************************************************************/
    /********************************* STATIC METHODS *********************************/
    /**********************************************************************************/

    /**
     * Add a number of relative values to an absolute value. Return a new instance of the value. The unit of the return
     * value will be the unit of the first argument. Because of type erasure of generics, the method cannot check
     * whether an array of arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param valueAbs the absolute base value
     * @param valuesRel zero or more values to add to the absolute value
     * @return the sum of the values as an absolute value
     * @param <U> Unit; the unit of the parameters and the result
     */
    @SafeVarargs
    public static <U extends Unit<U>> MutableFloatScalar.Abs<U> plus(final FloatScalar.Abs<U> valueAbs,
            final FloatScalar.Rel<U>... valuesRel)
    {
        MutableFloatScalar.Abs<U> result = new MutableFloatScalar.Abs<U>(valueAbs);
        for (FloatScalar.Rel<U> v : valuesRel)
        {
            result.valueSI += v.valueSI;
        }
        return result;
    }

    /**
     * Add a number of relative values. Return a new instance of the value. Because of type erasure of generics, the
     * method cannot check whether an array of arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param targetUnit the unit of the sum
     * @param valuesRel zero or more values to add
     * @return the sum of the values as a relative value
     * @param <U> Unit; the unitof the parameters and the result
     */
    @SafeVarargs
    public static <U extends Unit<U>> MutableFloatScalar.Rel<U> plus(final U targetUnit,
            final FloatScalar.Rel<U>... valuesRel)
    {
        MutableFloatScalar.Rel<U> result = new MutableFloatScalar.Rel<U>(0.0f, targetUnit);
        for (FloatScalar.Rel<U> v : valuesRel)
        {
            result.valueSI += v.valueSI;
        }
        return result;
    }

    /**
     * Subtract a number of relative values from an absolute value. Return a new instance of the value. The unit of the
     * return value will be the unit of the first argument. Because of type erasure of generics, the method cannot check
     * whether an array of arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param valueAbs the absolute base value
     * @param valuesRel zero or more values to subtract from the absolute value
     * @return the resulting value as an absolute value
     * @param <U> Unit; the unitof the parameters and the result
     */
    @SafeVarargs
    public static <U extends Unit<U>> MutableFloatScalar.Abs<U> minus(final FloatScalar.Abs<U> valueAbs,
            final FloatScalar.Rel<U>... valuesRel)
    {
        MutableFloatScalar.Abs<U> value = new MutableFloatScalar.Abs<U>(valueAbs);
        for (FloatScalar.Rel<U> v : valuesRel)
        {
            value.valueSI -= v.valueSI;
        }
        return value;
    }

    /**
     * Subtract a number of relative values from a relative value. Return a new instance of the value. The unit of the
     * value will be the unit of the first argument. Because of type erasure of generics, the method cannot check
     * whether an array of arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param valueRel the relative base value
     * @param valuesRel zero or more values to subtract from the first value
     * @return the resulting value as a relative value
     * @param <U> Unit; the unitof the parameters and the result
     */
    @SafeVarargs
    public static <U extends Unit<U>> MutableFloatScalar.Rel<U> minus(final FloatScalar.Rel<U> valueRel,
            final FloatScalar.Rel<U>... valuesRel)
    {
        MutableFloatScalar.Rel<U> value = new MutableFloatScalar.Rel<U>(valueRel);
        for (FloatScalar.Rel<U> v : valuesRel)
        {
            value.valueSI -= v.valueSI;
        }
        return value;
    }

    /**
     * Subtract two absolute values. Return a new instance of a relative value of the difference. The unit of the value
     * will be the unit of the first argument.
     * @param valueAbs1 value 1
     * @param valueAbs2 value 2
     * @return the difference of the two absolute values as a relative value
     * @param <U> Unit; the unitof the parameters and the result
     */
    public static <U extends Unit<U>> MutableFloatScalar.Rel<U> minus(final FloatScalar.Abs<U> valueAbs1,
            final FloatScalar.Abs<U> valueAbs2)
    {
        return new MutableFloatScalar.Rel<U>(valueAbs1.valueSI - valueAbs2.valueSI, valueAbs1.getUnit());
    }

    /**
     * Multiply two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueAbs1 value 1
     * @param valueAbs2 value 2
     * @return the product of the two absolute values as an absolute value
     */
    public static MutableFloatScalar.Abs<SIUnit> multiply(final FloatScalar.Abs<?> valueAbs1,
            final FloatScalar.Abs<?> valueAbs2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(
                        valueAbs1.getUnit().getSICoefficients(), valueAbs2.getUnit().getSICoefficients()).toString());
        return new MutableFloatScalar.Abs<SIUnit>(valueAbs1.valueSI * valueAbs2.valueSI, targetUnit);
    }

    /**
     * Multiply two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueRel1 value 1
     * @param valueRel2 value 2
     * @return the product of the two relative values as a relative value
     */
    public static MutableFloatScalar.Rel<SIUnit> multiply(final FloatScalar.Rel<?> valueRel1,
            final FloatScalar.Rel<?> valueRel2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(
                        valueRel1.getUnit().getSICoefficients(), valueRel2.getUnit().getSICoefficients()).toString());
        return new MutableFloatScalar.Rel<SIUnit>(valueRel1.valueSI * valueRel2.valueSI, targetUnit);
    }

    /**
     * Divide two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueAbs1 value 1
     * @param valueAbs2 value 2
     * @return the division of the two absolute values as an absolute value
     */
    public static FloatScalar.Abs<SIUnit> divide(final FloatScalar.Abs<?> valueAbs1, final FloatScalar.Abs<?> valueAbs2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(
                        valueAbs1.getUnit().getSICoefficients(), valueAbs2.getUnit().getSICoefficients()).toString());
        return new FloatScalar.Abs<SIUnit>(valueAbs1.valueSI / valueAbs2.valueSI, targetUnit);
    }

    /**
     * Divide two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueRel1 value 1
     * @param valueRel2 value 2
     * @return the division of the two two relative values as a relative value
     */
    public static FloatScalar.Rel<SIUnit> divide(final FloatScalar.Rel<?> valueRel1, final FloatScalar.Rel<?> valueRel2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(
                        valueRel1.getUnit().getSICoefficients(), valueRel2.getUnit().getSICoefficients()).toString());
        return new FloatScalar.Rel<SIUnit>(valueRel1.valueSI / valueRel2.valueSI, targetUnit);
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
        this.valueSI = (float) Math.acos(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void asin()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        this.valueSI = (float) Math.asin(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void atan()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        this.valueSI = (float) Math.atan(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void cbrt()
    {
        // TODO: dimension for all SI coefficients / 3.
        this.valueSI = (float) Math.cbrt(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void ceil()
    {
        this.valueSI = (float) Math.ceil(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void cos()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        this.valueSI = (float) Math.cos(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void cosh()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        this.valueSI = (float) Math.cosh(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void exp()
    {
        // TODO: dimensionless result (SIUnit.ONE).
        this.valueSI = (float) Math.exp(this.valueSI);
    }

    /** {@inheritDoc} */
    
    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void expm1()
    {
        this.valueSI = (float) Math.expm1(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void floor()
    {
        this.valueSI = (float) Math.floor(this.valueSI);
    }

    /** {@inheritDoc} */
    
    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void log()
    {
        this.valueSI = (float) Math.log(this.valueSI);
    }

    /** {@inheritDoc} */
    
    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void log10()
    {
        this.valueSI = (float) Math.log10(this.valueSI);
    }

    /** {@inheritDoc} */
    
    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void log1p()
    {
        this.valueSI = (float) Math.log1p(this.valueSI);
    }

    /** {@inheritDoc} */
    
    // TODO: SI unit with coefficients * x.
    @Override
    public final void pow(final double x)
    {
        this.valueSI = (float) Math.pow(this.valueSI, x);
    }

    /** {@inheritDoc} */
    @Override
    public final void rint()
    {
        this.valueSI = (float) Math.rint(this.valueSI);
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
        this.valueSI = (float) Math.sin(this.valueSI);
    }

    /** {@inheritDoc} */
    
    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void sinh()
    {
        this.valueSI = (float) Math.sinh(this.valueSI);
    }

    /** {@inheritDoc} */
    
    // TODO: unit coefficients / 2.
    @Override
    public final void sqrt()
    {
        this.valueSI = (float) Math.sqrt(this.valueSI);
    }

    /** {@inheritDoc} */
    
    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void tan()
    {
        this.valueSI = (float) Math.tan(this.valueSI);
    }

    /** {@inheritDoc} */
    
    // TODO: dimensionless result (SIUnit.ONE).
    @Override
    public final void tanh()
    {
        this.valueSI = (float) Math.tanh(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void toDegrees()
    {
        this.valueSI = (float) Math.toDegrees(this.valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void toRadians()
    {
        this.valueSI = (float) Math.toRadians(this.valueSI);
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
    public final void multiply(final float constant)
    {
        this.valueSI *= constant;
    }

    /** {@inheritDoc} */
    @Override
    public final void divide(final float constant)
    {
        this.valueSI /= constant;
    }

}

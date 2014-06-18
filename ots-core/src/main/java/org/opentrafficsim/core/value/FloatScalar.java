package org.opentrafficsim.core.value;

import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;

/**
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
public abstract class FloatScalar<U extends Unit<U>> extends Scalar<U> implements FloatFunctions
{
    /** */
    private static final long serialVersionUID = 20140615L;

    /** the value, stored in SI units */
    protected float valueSI;

    /**
     * Construct a value and store it in SI units for calculation.
     * @param value the value in the given units
     * @param unit the unit of the value
     */
    public FloatScalar(final float value, final U unit)
    {
        super(unit);
        // TODO: method convert to standard, e.g. degree C to K does not work correct this way.
        this.valueSI = (float) (value * unit.getConversionFactorToStandardUnit());
    }

    /**
     * Construct a value from another value. The value is already in SI units.
     * @param value the value to duplicate
     */
    public FloatScalar(final FloatScalar<U> value)
    {
        super(value.getUnit());
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
        // TODO: method convert to specific unit, e.g. K to degree C does not work correct this way.
        return (float) (this.valueSI / this.unit.getConversionFactorToStandardUnit());
    }

    /**
     * @return unit
     */
    public U getUnit()
    {
        return this.unit;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return this.getValueInUnit() + " " + this.unit.getAbbreviationKey();
    }


    /**********************************************************************************/
    /******************************* NON-STATIC METHODS *******************************/
    /**********************************************************************************/

    // TODO: for all functions: argument is NaN, INFINITY, etc. Handle or specify in javadoc.

    /**
     * Add another value to this value. Only relative values are allowed; adding an absolute value to an absolute value
     * is not allowed. Adding an absolute value to an existing relative value would require the result to become
     * absolute, which is a type change that is impossible. For this operation, use a static method.
     * @param value the value to add
     */
    public void add(final FloatScalarRel<U> value)
    {
        this.valueSI += value.getValueSI();
    }

    /**
     * Subtract another value from this value. Only relative values are allowed; subtracting an absolute value from a
     * relative value is not allowed. Subtracting an absolute value from an existing absolute value would require the
     * result to become relative, which is a type change that is impossible. For this operation, use a static method.
     * @param value the value to subtract
     */
    public void subtract(FloatScalarRel<U> value)
    {
        this.valueSI -= value.getValueSI();
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#abs()
     */
    @Override
    public void abs()
    {
        this.valueSI = Math.abs(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#acos()
     */
    @Override
    public void acos()
    {
        this.valueSI = (float) Math.acos(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#asin()
     */
    @Override
    public void asin()
    {
        this.valueSI = (float) Math.asin(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#atan()
     */
    @Override
    public void atan()
    {
        this.valueSI = (float) Math.atan(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#cbrt()
     */
    @Override
    public void cbrt()
    {
        this.valueSI = (float) Math.cbrt(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#ceil()
     */
    @Override
    public void ceil()
    {
        this.valueSI = (float) Math.ceil(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#cos()
     */
    @Override
    public void cos()
    {
        this.valueSI = (float) Math.cos(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#cosh()
     */
    @Override
    public void cosh()
    {
        this.valueSI = (float) Math.cosh(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#exp()
     */
    @Override
    public void exp()
    {
        this.valueSI = (float) Math.exp(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#expm1()
     */
    @Override
    public void expm1()
    {
        this.valueSI = (float) Math.expm1(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#floor()
     */
    @Override
    public void floor()
    {
        this.valueSI = (float) Math.floor(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#log()
     */
    @Override
    public void log()
    {
        this.valueSI = (float) Math.log(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#log10()
     */
    @Override
    public void log10()
    {
        this.valueSI = (float) Math.log10(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#log1p()
     */
    @Override
    public void log1p()
    {
        this.valueSI = (float) Math.log1p(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#pow(double)
     */
    @Override
    public void pow(double x)
    {
        this.valueSI = (float) Math.pow(this.valueSI, x);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#rint()
     */
    @Override
    public void rint()
    {
        this.valueSI = (float) Math.rint(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#round()
     */
    @Override
    public void round()
    {
        this.valueSI = Math.round(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#signum()
     */
    @Override
    public void signum()
    {
        this.valueSI = Math.signum(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#sin()
     */
    @Override
    public void sin()
    {
        this.valueSI = (float) Math.sin(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#sinh()
     */
    @Override
    public void sinh()
    {
        this.valueSI = (float) Math.sinh(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#sqrt()
     */
    @Override
    public void sqrt()
    {
        this.valueSI = (float) Math.sqrt(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#tan()
     */
    @Override
    public void tan()
    {
        this.valueSI = (float) Math.tan(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#tanh()
     */
    @Override
    public void tanh()
    {
        this.valueSI = (float) Math.tanh(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#toDegrees()
     */
    @Override
    public void toDegrees()
    {
        this.valueSI = (float) Math.toDegrees(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.Functions#toRadians()
     */
    @Override
    public void toRadians()
    {
        this.valueSI = (float) Math.toRadians(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.FloatFunctions#multiply(float)
     */
    @Override
    public void multiply(float constant)
    {
        this.valueSI *= constant;
    }

    /**
     * @see org.opentrafficsim.core.value.FloatFunctions#divide(float)
     */
    @Override
    public void divide(float constant)
    {
        this.valueSI /= constant;
    }

    /**********************************************************************************/
    /********************************* STATIC METHODS *********************************/
    /**********************************************************************************/

    /**
     * Add a number of relative values to an absolute value. Return a new instance of the value. The unit of the return
     * value will be the unit of the first argument.
     * TODO: value 1 is NaN, value2 is NaN, value1 is INFINITY, etc. Handle or specify in javadoc.
     * @param valueAbs the absolute base value
     * @param valuesRel zero or more values to add to the absolute value
     * @return the sum of the values as an absolute value
     */
    @SafeVarargs
    public static <U extends Unit<U>> FloatScalarAbs<U> plus(FloatScalarAbs<U> valueAbs,
            final FloatScalarRel<U>... valuesRel)
    {
        FloatScalarAbs<U> value = new FloatScalarAbs<U>(valueAbs);
        for (FloatScalarRel<U> v : valuesRel)
        {
            value.valueSI += v.valueSI;
        }
        return value;
    }

    /**
     * Add a number of relative values. Return a new instance of the value.
     * TODO: add comment about heap pollution with varargs arrays of a generic type
     * @param targetUnit the unit of the sum
     * @param valuesRel zero or more values to add
     * @return the sum of the values as a relative value
     */
    @SafeVarargs
    public static <U extends Unit<U>> FloatScalarRel<U> plus(U targetUnit, final FloatScalarRel<U>... valuesRel)
    {
        FloatScalarRel<U> value = new FloatScalarRel<U>(0.0f, targetUnit);
        for (FloatScalarRel<U> v : valuesRel)
        {
            value.valueSI += v.valueSI;
        }
        return value;
    }

    /**
     * Subtract a number of relative values from an absolute value. Return a new instance of the value. The unit of the
     * return value will be the unit of the first argument.
     * TODO: value 1 is NaN, value2 is NaN, value1 is INFINITY, etc. Handle or specify in javadoc.
     * @param valueAbs the absolute base value
     * @param valuesRel zero or more values to subtract from the absolute value
     * @return the resulting value as an absolute value
     */
    @SafeVarargs
    public static <U extends Unit<U>> FloatScalarAbs<U> minus(FloatScalarAbs<U> valueAbs,
            final FloatScalarRel<U>... valuesRel)
    {
        FloatScalarAbs<U> value = new FloatScalarAbs<U>(valueAbs);
        for (FloatScalarRel<U> v : valuesRel)
        {
            value.valueSI -= v.valueSI;
        }
        return value;
    }

    /**
     * Subtract a number of relative values from a relative value. Return a new instance of the value. The unit of the
     * value will be the unit of the first argument.
     * @param valueRel the relative base value
     * @param valuesRel zero or more values to subtract from the first value
     * @return the resulting value as a relative value
     */
    @SafeVarargs
    public static <U extends Unit<U>> FloatScalarRel<U> minus(FloatScalarRel<U> valueRel,
            final FloatScalarRel<U>... valuesRel)
    {
        FloatScalarRel<U> value = new FloatScalarRel<U>(valueRel);
        for (FloatScalarRel<U> v : valuesRel)
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
     */
    public static <U extends Unit<U>> FloatScalarRel<U> minus(final FloatScalarAbs<U> valueAbs1,
            final FloatScalarAbs<U> valueAbs2)
    {
        return new FloatScalarRel<U>(valueAbs1.valueSI - valueAbs2.valueSI, valueAbs1.getUnit());
    }

    /**
     * Multiply two values; the result is a new instance with a different (existing or generated) SI unit.
     * TODO: value 1 is NaN, value2 is NaN, value1 is INFINITY, etc. Handle or specify in javadoc.
     * @param valueAbs1 value 1
     * @param valueAbs2 value 2
     * @return the product of the two absolute values as an absolute value
     */
    public static FloatScalarAbs<SIUnit> multiply(final FloatScalarAbs<?> valueAbs1, final FloatScalarAbs<?> valueAbs2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(
                        valueAbs1.getUnit().getSICoefficients(), valueAbs2.getUnit().getSICoefficients()).toString());
        return new FloatScalarAbs<SIUnit>(valueAbs1.valueSI * valueAbs2.valueSI, targetUnit);
    }

    /**
     * Multiply two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueRel1 value 1
     * @param valueRel2 value 2
     * @return the product of the two relative values as a relative value
     */
    public static FloatScalarRel<SIUnit> multiply(final FloatScalarRel<?> valueRel1, final FloatScalarRel<?> valueRel2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(
                        valueRel1.getUnit().getSICoefficients(), valueRel2.getUnit().getSICoefficients()).toString());
        return new FloatScalarRel<SIUnit>(valueRel1.valueSI * valueRel2.valueSI, targetUnit);
    }

    /**
     * Divide two values; the result is a new instance with a different (existing or generated) SI unit.
     * TODO: divide by zero, value 1 is NaN, value2 is NaN, value1 is INFINITY, etc. Handle or specify in javadoc.
     * @param valueAbs1 value 1
     * @param valueAbs2 value 2
     * @return the division of the two absolute values as an absolute value
     */
    public static FloatScalarAbs<SIUnit> divide(final FloatScalarAbs<?> valueAbs1, final FloatScalarAbs<?> valueAbs2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(
                        valueAbs1.getUnit().getSICoefficients(), valueAbs2.getUnit().getSICoefficients()).toString());
        return new FloatScalarAbs<SIUnit>(valueAbs1.valueSI / valueAbs2.valueSI, targetUnit);
    }

    /**
     * Divide two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueRel1 value 1
     * @param valueRel2 value 2
     * @return the division of the two two relative values as a relative value
     */
    public static FloatScalarRel<SIUnit> divide(final FloatScalarRel<?> valueRel1, final FloatScalarRel<?> valueRel2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(
                        valueRel1.getUnit().getSICoefficients(), valueRel2.getUnit().getSICoefficients()).toString());
        return new FloatScalarRel<SIUnit>(valueRel1.valueSI / valueRel2.valueSI, targetUnit);
    }
}

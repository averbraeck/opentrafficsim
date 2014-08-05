package org.opentrafficsim.core.value.vdouble.scalar;

import org.opentrafficsim.core.unit.SICoefficients;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Scalar;
import org.opentrafficsim.core.value.vdouble.DoubleMathFunctions;

/**
 * All calculations are according to IEEE 754. This means that division by zero results in Double.INFINITY, and some
 * calculations could result in NaN. No changes have been made to avoid this, as it is the standard behavior of Java for
 * doubleing point numbers.
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
 * @param <U> the unit of the values in the constructor and for display
 */
public abstract class DoubleScalar<U extends Unit<U>> extends Scalar<U> implements DoubleMathFunctions,
        Comparable<DoubleScalar<U>>
{
    /** */
    private static final long serialVersionUID = 20140618L;

    /** the value, stored in SI units. */
    protected double valueSI;

    /**
     * Construct a value and store it in SI units for calculation.
     * @param value the value in the given units
     * @param unit the unit of the value
     */
    public DoubleScalar(final double value, final U unit)
    {
        super(unit);
        this.valueSI = expressAsSIUnit(value);
    }

    /**
     * Construct a value from another value. The value is already in SI units.
     * @param value the value to duplicate
     */
    public DoubleScalar(final DoubleScalar<U> value)
    {
        super(value.getUnit());
        this.valueSI = value.valueSI;
    }

    /**
     * @return value in SI units
     */
    public double getValueSI()
    {
        return this.valueSI;
    }

    /**
     * @return value in original units
     */
    public double getValueInUnit()
    {
        return expressAsSpecifiedUnit(this.valueSI);
    }

    /**
     * @param targetUnit the unit to convert the value to
     * @return value in specific target unit
     */
    public double getValueInUnit(final U targetUnit)
    {
        return expressAsUnit(this.valueSI, targetUnit);
    }

    /**
     * @param valueSI the value to store in the scalar
     */
    void setSI(final double valueSI)
    {
        this.valueSI = valueSI;
    }

    /**
     * @param value the strongly typed value to store in the scalar
     */
    void set(final DoubleScalar<U> value)
    {
        setDisplayUnit(value.unit);
        this.valueSI = value.valueSI;
    }

    /**
     * @param value the value to store in the scalar
     * @param valueUnit the unit of the value.
     */
    void setInUnit(final double value, final U valueUnit)
    {
        setDisplayUnit(valueUnit);
        this.valueSI = expressAsSIUnit(value);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj)
    {
        // unequal if object is of a different type.
        if (!(obj instanceof DoubleScalar<?>))
            return false;
        DoubleScalar<?> ds = (DoubleScalar<?>) obj;

        // unequal if the SI unit type differs (km/h and m/s could have the same content, so that is allowed)
        if (!this.getUnit().getStandardUnit().equals(ds.getUnit().getStandardUnit()))
            return false;

        // unequal if one is absolute and the other is relative
        if (this.isAbsolute() != ds.isAbsolute() || this.isRelative() != ds.isRelative())
            return false;

        return this.valueSI == ds.valueSI;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @throws a ClassCastException if one value is absolute and the other relative, or if the SI unit differs.
     */
    @Override
    public int compareTo(final DoubleScalar<U> fs)
    {
        // exception if the SI unit type differs (km/h and m/s could have the same content, so that is allowed)
        if (!this.getUnit().getStandardUnit().equals(fs.getUnit().getStandardUnit()))
            throw new ClassCastException("DoubleScalar.CompareTo compares two scalars with units: "
                    + this.getUnit().toString() + fs.getUnit().toString()
                    + ", mwhich translate to different SI base units");

        // exception if one is absolute and the other is relative
        if (this.isAbsolute() != fs.isAbsolute() || this.isRelative() != fs.isRelative())
            throw new ClassCastException("DoubleScalar.CompareTo compares two scalars with of which one is absolute "
                    + "and the other is relative.");

        if (this.valueSI < fs.valueSI)
            return -1;
        if (this.valueSI > fs.valueSI)
            return 1;
        return 0;

    }

    /**********************************************************************************/
    /********************************** MATH METHODS **********************************/
    /**********************************************************************************/

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#abs()
     */
    @Override
    public void abs()
    {
        this.valueSI = Math.abs(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#acos()
     */
    @Override
    public void acos()
    {
        this.valueSI = Math.acos(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#asin()
     */
    @Override
    public void asin()
    {
        this.valueSI = Math.asin(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#atan()
     */
    @Override
    public void atan()
    {
        this.valueSI = Math.atan(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cbrt()
     */
    @Override
    public void cbrt()
    {
        this.valueSI = Math.cbrt(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#ceil()
     */
    @Override
    public void ceil()
    {
        this.valueSI = Math.ceil(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cos()
     */
    @Override
    public void cos()
    {
        this.valueSI = Math.cos(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#cosh()
     */
    @Override
    public void cosh()
    {
        this.valueSI = Math.cosh(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#exp()
     */
    @Override
    public void exp()
    {
        this.valueSI = Math.exp(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#expm1()
     */
    @Override
    public void expm1()
    {
        this.valueSI = Math.expm1(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#floor()
     */
    @Override
    public void floor()
    {
        this.valueSI = Math.floor(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log()
     */
    @Override
    public void log()
    {
        this.valueSI = Math.log(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log10()
     */
    @Override
    public void log10()
    {
        this.valueSI = Math.log10(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#log1p()
     */
    @Override
    public void log1p()
    {
        this.valueSI = Math.log1p(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#pow(double)
     */
    @Override
    public void pow(final double x)
    {
        this.valueSI = Math.pow(this.valueSI, x);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#rint()
     */
    @Override
    public void rint()
    {
        this.valueSI = Math.rint(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#round()
     */
    @Override
    public void round()
    {
        this.valueSI = Math.round(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#signum()
     */
    @Override
    public void signum()
    {
        this.valueSI = Math.signum(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sin()
     */
    @Override
    public void sin()
    {
        this.valueSI = Math.sin(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sinh()
     */
    @Override
    public void sinh()
    {
        this.valueSI = Math.sinh(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#sqrt()
     */
    @Override
    public void sqrt()
    {
        this.valueSI = Math.sqrt(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tan()
     */
    @Override
    public void tan()
    {
        this.valueSI = Math.tan(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#tanh()
     */
    @Override
    public void tanh()
    {
        this.valueSI = Math.tanh(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toDegrees()
     */
    @Override
    public void toDegrees()
    {
        this.valueSI = Math.toDegrees(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#toRadians()
     */
    @Override
    public void toRadians()
    {
        this.valueSI = Math.toRadians(this.valueSI);
    }

    /**
     * @see org.opentrafficsim.core.value.MathFunctions#inv()
     */
    @Override
    public void inv()
    {
        this.valueSI = 1.0f / this.valueSI;
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.DoubleMathFunctions#multiply(double)
     */
    @Override
    public void multiply(final double constant)
    {
        this.valueSI *= constant;
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.DoubleMathFunctions#divide(double)
     */
    @Override
    public void divide(final double constant)
    {
        this.valueSI /= constant;
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

    /**
     * Add another value to this value. Only relative values are allowed; adding an absolute value to an absolute value
     * is not allowed. Adding an absolute value to an existing relative value would require the result to become
     * absolute, which is a type change that is impossible. For that operation, use a static method.
     * @param value the value to add
     */
    public void add(final DoubleScalarRel<U> value)
    {
        this.valueSI += value.getValueSI();
    }

    /**
     * Subtract another value from this value. Only relative values are allowed; subtracting an absolute value from a
     * relative value is not allowed. Subtracting an absolute value from an existing absolute value would require the
     * result to become relative, which is a type change that is impossible. For that operation, use a static method.
     * @param value the value to subtract
     */
    public void subtract(final DoubleScalarRel<U> value)
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
     */
    @SafeVarargs
    public static <U extends Unit<U>> DoubleScalarAbs<U> plus(final DoubleScalarAbs<U> valueAbs,
            final DoubleScalarRel<U>... valuesRel)
    {
        DoubleScalarAbs<U> value = new DoubleScalarAbs<U>(valueAbs);
        for (DoubleScalarRel<U> v : valuesRel)
        {
            value.valueSI += v.valueSI;
        }
        return value;
    }

    /**
     * Add a number of relative values. Return a new instance of the value. Because of type erasure of generics, the
     * method cannot check whether an array of arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param targetUnit the unit of the sum
     * @param valuesRel zero or more values to add
     * @return the sum of the values as a relative value
     */
    @SafeVarargs
    public static <U extends Unit<U>> DoubleScalarRel<U> plus(final U targetUnit, final DoubleScalarRel<U>... valuesRel)
    {
        DoubleScalarRel<U> value = new DoubleScalarRel<U>(0.0f, targetUnit);
        for (DoubleScalarRel<U> v : valuesRel)
        {
            value.valueSI += v.valueSI;
        }
        return value;
    }

    /**
     * Subtract a number of relative values from an absolute value. Return a new instance of the value. The unit of the
     * return value will be the unit of the first argument. Because of type erasure of generics, the method cannot check
     * whether an array of arguments submitted to the varargs has a mixed-unit content at runtime.
     * @param valueAbs the absolute base value
     * @param valuesRel zero or more values to subtract from the absolute value
     * @return the resulting value as an absolute value
     */
    @SafeVarargs
    public static <U extends Unit<U>> DoubleScalarAbs<U> minus(final DoubleScalarAbs<U> valueAbs,
            final DoubleScalarRel<U>... valuesRel)
    {
        DoubleScalarAbs<U> value = new DoubleScalarAbs<U>(valueAbs);
        for (DoubleScalarRel<U> v : valuesRel)
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
     */
    @SafeVarargs
    public static <U extends Unit<U>> DoubleScalarRel<U> minus(final DoubleScalarRel<U> valueRel,
            final DoubleScalarRel<U>... valuesRel)
    {
        DoubleScalarRel<U> value = new DoubleScalarRel<U>(valueRel);
        for (DoubleScalarRel<U> v : valuesRel)
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
    public static <U extends Unit<U>> DoubleScalarRel<U> minus(final DoubleScalarAbs<U> valueAbs1,
            final DoubleScalarAbs<U> valueAbs2)
    {
        return new DoubleScalarRel<U>(valueAbs1.valueSI - valueAbs2.valueSI, valueAbs1.getUnit());
    }

    /**
     * Multiply two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueAbs1 value 1
     * @param valueAbs2 value 2
     * @return the product of the two absolute values as an absolute value
     */
    public static DoubleScalarAbs<SIUnit> multiply(final DoubleScalarAbs<?> valueAbs1,
            final DoubleScalarAbs<?> valueAbs2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(
                        valueAbs1.getUnit().getSICoefficients(), valueAbs2.getUnit().getSICoefficients()).toString());
        return new DoubleScalarAbs<SIUnit>(valueAbs1.valueSI * valueAbs2.valueSI, targetUnit);
    }

    /**
     * Multiply two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueRel1 value 1
     * @param valueRel2 value 2
     * @return the product of the two relative values as a relative value
     */
    public static DoubleScalarRel<SIUnit> multiply(final DoubleScalarRel<?> valueRel1,
            final DoubleScalarRel<?> valueRel2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(
                        valueRel1.getUnit().getSICoefficients(), valueRel2.getUnit().getSICoefficients()).toString());
        return new DoubleScalarRel<SIUnit>(valueRel1.valueSI * valueRel2.valueSI, targetUnit);
    }

    /**
     * Divide two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueAbs1 value 1
     * @param valueAbs2 value 2
     * @return the division of the two absolute values as an absolute value
     */
    public static DoubleScalarAbs<SIUnit> divide(final DoubleScalarAbs<?> valueAbs1, final DoubleScalarAbs<?> valueAbs2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(
                        valueAbs1.getUnit().getSICoefficients(), valueAbs2.getUnit().getSICoefficients()).toString());
        return new DoubleScalarAbs<SIUnit>(valueAbs1.valueSI / valueAbs2.valueSI, targetUnit);
    }

    /**
     * Divide two values; the result is a new instance with a different (existing or generated) SI unit.
     * @param valueRel1 value 1
     * @param valueRel2 value 2
     * @return the division of the two two relative values as a relative value
     */
    public static DoubleScalarRel<SIUnit> divide(final DoubleScalarRel<?> valueRel1, final DoubleScalarRel<?> valueRel2)
    {
        SIUnit targetUnit =
                Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.divide(
                        valueRel1.getUnit().getSICoefficients(), valueRel2.getUnit().getSICoefficients()).toString());
        return new DoubleScalarRel<SIUnit>(valueRel1.valueSI / valueRel2.valueSI, targetUnit);
    }
}

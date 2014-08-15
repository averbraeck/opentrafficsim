package org.opentrafficsim.core.value;

import java.io.Serializable;

import org.opentrafficsim.core.unit.OffsetUnit;
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
public abstract class Scalar<U extends Unit<U>> implements Serializable, MathFunctions
{
    /** */
    private static final long serialVersionUID = 20140615L;

    /** the unit of the value. */
    protected U unit;

    /**
     * @param unit the unit of the value
     */
    public Scalar(final U unit)
    {
        this.unit = unit;
    }

    /**
     * @return unit
     */
    public U getUnit()
    {
        return this.unit;
    }

    /**
     * @param value the value to convert in SI units
     * @return the value in SI units
     */
    protected double expressAsSIUnit(final double value)
    {
        if (this.unit instanceof OffsetUnit<?>)
            return value - ((OffsetUnit<?>) this.unit).getOffsetToStandardUnit()
                    * this.unit.getConversionFactorToStandardUnit();
        return value * this.unit.getConversionFactorToStandardUnit();
    }

    /**
     * @param value the value to convert in SI units
     * @param valueUnit the unit of the offered value
     * @return the value in SI units
     */
    // TODO: maybe as static method?
    protected double expressAsSIUnit(final double value, final Unit<U> valueUnit)
    {
        if (this.unit instanceof OffsetUnit<?>)
            return value + ((OffsetUnit<?>) valueUnit).getOffsetToStandardUnit()
                    * valueUnit.getConversionFactorToStandardUnit();
        return value * valueUnit.getConversionFactorToStandardUnit();
    }

    /**
     * @param value the value to convert in the specified unit for this scalar
     * @return the value in the unit as specified for this scalar
     */
    protected double expressAsSpecifiedUnit(final double value)
    {
        return expressAsUnit(value, this.unit);
    }

    /**
     * @param value the value to express in target unit
     * @param targetUnit the unit to convert the value to
     * @return the value in the target unit
     */
    protected double expressAsUnit(final double value, final Unit<U> targetUnit)
    {
        if (targetUnit instanceof OffsetUnit<?>)
            return value / targetUnit.getConversionFactorToStandardUnit() + ((OffsetUnit<?>) targetUnit)
                    .getOffsetToStandardUnit();
        return value / targetUnit.getConversionFactorToStandardUnit();
    }

    /**
     * Set a new unit for displaying the results.
     * @param newUnit the new unit of the right unit type
     */
    public void setDisplayUnit(final U newUnit)
    {
        this.unit = newUnit;
    }

    /**
     * @return whether the value is absolute.
     */
    public boolean isAbsolute()
    {
        return this instanceof Absolute;
    }

    /**
     * @return whether the value is relative.
     */
    public boolean isRelative()
    {
        return this instanceof Relative;
    }

    /**
     * @return a copy of the object
     */
    public abstract Scalar<U> copy();
    
}
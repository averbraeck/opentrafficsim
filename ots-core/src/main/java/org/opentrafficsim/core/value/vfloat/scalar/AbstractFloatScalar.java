package org.opentrafficsim.core.value.vfloat.scalar;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Scalar;
import org.opentrafficsim.core.value.ValueUtil;

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
public abstract class AbstractFloatScalar<U extends Unit<U>> extends Scalar<U>
{

    /** */
    private static final long serialVersionUID = 20140905L;

    /**
     * @param unit
     */
    public AbstractFloatScalar(U unit)
    {
        super(unit);
    }

    /** the value, stored in SI units. */
    protected float valueSI;

    /**
     * Initialize the valueSI field (performing conversion to the SI standard unit if needed).
     * @param value float; the value in the unit of this AbstractFloatScalar
     */
    protected void initialize(float value)
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
     * @param value AbstractFloatScalar; the value to use for initialization
     */
    protected void initialize(AbstractFloatScalar<U> value)
    {
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
        return (float) expressAsSpecifiedUnit(this.valueSI);
    }

    /**
     * @param targetUnit the unit to convert the value to
     * @return value in specific target unit
     */
    public float getValueInUnit(final U targetUnit)
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
    public long longValue()
    {
        return Math.round(this.valueSI);
    }

    /**
     * @see java.lang.Number#floatValue()
     */
    @Override
    public float floatValue()
    {
        return this.valueSI;
    }

    /**
     * @see java.lang.Number#doubleValue()
     */
    @Override
    public double doubleValue()
    {
        return this.valueSI;
    }



}

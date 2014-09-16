package org.opentrafficsim.core.value;

import org.opentrafficsim.core.unit.OffsetUnit;
import org.opentrafficsim.core.unit.Unit;

/**
 * Value is a static interface that implements a couple of unit-related static methods.
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
 * @version Aug 18, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ValueUtil
{

    /**
     * This class should never be instantiated.
     */
    private ValueUtil()
    {
        // Prevent instantiation of this class
    }

    /**
     * @param value the value to convert into SI units
     * @param unit the unit belonging to the value
     * @return the value in SI units
     */
    public static double expressAsSIUnit(final double value, final Unit<?> unit)
    {
        if (unit instanceof OffsetUnit<?>)
        {
            return (value - ((OffsetUnit<?>) unit).getOffsetToStandardUnit())
                    * unit.getConversionFactorToStandardUnit();
        }
        return value * unit.getConversionFactorToStandardUnit();
    }

    /**
     * @param siValue the value to express in target unit
     * @param targetUnit the unit to convert the value to
     * @return the value in the target unit
     */
    public static double expressAsUnit(final double siValue, final Unit<?> targetUnit)
    {
        if (targetUnit instanceof OffsetUnit<?>)
        {
            return siValue / targetUnit.getConversionFactorToStandardUnit()
                    + ((OffsetUnit<?>) targetUnit).getOffsetToStandardUnit();
        }
        return siValue / targetUnit.getConversionFactorToStandardUnit();
    }

}

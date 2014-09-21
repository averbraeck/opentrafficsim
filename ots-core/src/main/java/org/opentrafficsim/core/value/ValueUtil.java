package org.opentrafficsim.core.value;

import org.opentrafficsim.core.unit.OffsetUnit;
import org.opentrafficsim.core.unit.Unit;

/**
 * Value is a static interface that implements a couple of unit-related static methods.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 18, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class ValueUtil
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
            return (value - ((OffsetUnit<?>) unit).getOffsetToStandardUnit()) * unit.getConversionFactorToStandardUnit();
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

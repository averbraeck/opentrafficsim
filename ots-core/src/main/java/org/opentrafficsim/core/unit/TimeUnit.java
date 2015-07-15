package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_ACCEPTED;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_BASE;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Standard time units.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial versionMay 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TimeUnit extends Unit<TimeUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** The SI unit for time is second. */
    public static final TimeUnit SI;

    /** second. */
    public static final TimeUnit SECOND;

    /** millisecond. */
    public static final TimeUnit MILLISECOND;

    /** minute. */
    public static final TimeUnit MINUTE;

    /** hour. */
    public static final TimeUnit HOUR;

    /** day. */
    public static final TimeUnit DAY;

    /** week. */
    public static final TimeUnit WEEK;

    static
    {
        SI = new TimeUnit("TimeUnit.second", "TimeUnit.s", SI_BASE);
        SECOND = SI;
        MILLISECOND = new TimeUnit("TimeUnit.millisecond", "TimeUnit.ms", SI_BASE, SECOND, 0.001);
        MINUTE = new TimeUnit("TimeUnit.minute", "TimeUnit.m", SI_ACCEPTED, SECOND, 60.0);
        HOUR = new TimeUnit("TimeUnit.hour", "TimeUnit.h", SI_ACCEPTED, MINUTE, 60.0);
        DAY = new TimeUnit("TimeUnit.day", "TimeUnit.d", SI_ACCEPTED, HOUR, 24.0);
        WEEK = new TimeUnit("TimeUnit.week", "TimeUnit.w", OTHER, DAY, 7.0);
    }

    /**
     * Build a standard unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public TimeUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, true);
    }

    /**
     * Build a unit with a conversion factor to another unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public TimeUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final TimeUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
    }

    /** {@inheritDoc} */
    @Override
    public final TimeUnit getStandardUnit()
    {
        return SECOND;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "s";
    }

}

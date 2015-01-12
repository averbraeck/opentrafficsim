package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_BASE;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Standard length units. Several conversion factors have been taken from <a
 * href="http://en.wikipedia.org/wiki/Conversion_of_units">http://en.wikipedia.org/wiki/Conversion_of_units</a>.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LengthUnit extends Unit<LengthUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** The SI unit for length is meter. */
    public static final LengthUnit SI;

    /** meter. */
    public static final LengthUnit METER;

    /** millimeter. */
    public static final LengthUnit MILLIMETER;

    /** centimeter. */
    public static final LengthUnit CENTIMETER;

    /** decimeter. */
    public static final LengthUnit DECIMETER;

    /** decameter. */
    public static final LengthUnit DEKAMETER;

    /** hectometer. */
    public static final LengthUnit HECTOMETER;

    /** kilometer. */
    public static final LengthUnit KILOMETER;

    /** foot (international) = 0.3048 m = 1/3 yd = 12 inches. */
    public static final LengthUnit FOOT;

    /** inch (international) = 2.54 cm = 1/36 yd = 1/12 ft. */
    public static final LengthUnit INCH;

    /** mile (international) = 5280 ft = 1760 yd. */
    public static final LengthUnit MILE;

    /** nautical mile (international) = 1852 m. */
    public static final LengthUnit NAUTICAL_MILE;

    /** yard (international) = 0.9144 m = 3 ft = 36 in. */
    public static final LengthUnit YARD;

    static
    {
        SI = new LengthUnit("LengthUnit.meter", "LengthUnit.m", SI_BASE);
        METER = SI;
        MILLIMETER = new LengthUnit("LengthUnit.millimeter", "LengthUnit.mm", SI_BASE, METER, 0.001);
        CENTIMETER = new LengthUnit("LengthUnit.centimeter", "LengthUnit.cm", SI_BASE, METER, 0.01);
        DECIMETER = new LengthUnit("LengthUnit.decimeter", "LengthUnit.dm", SI_BASE, METER, 0.1);
        DEKAMETER = new LengthUnit("LengthUnit.dekameter", "LengthUnit.dam", SI_BASE, METER, 10.0);
        HECTOMETER = new LengthUnit("LengthUnit.hectometer", "LengthUnit.hm", SI_BASE, METER, 100.0);
        KILOMETER = new LengthUnit("LengthUnit.kilometer", "LengthUnit.km", SI_BASE, METER, 1000.0);
        FOOT = new LengthUnit("LengthUnit.foot", "LengthUnit.ft", IMPERIAL, METER, 0.3048);
        INCH = new LengthUnit("LengthUnit.inch", "LengthUnit.in", IMPERIAL, FOOT, 1.0 / 12.0);
        MILE = new LengthUnit("LengthUnit.mile", "LengthUnit.mi", IMPERIAL, FOOT, 5280.0);
        NAUTICAL_MILE = new LengthUnit("LengthUnit.nauticalMile", "LengthUnit.NM", IMPERIAL, METER, 1852.0);
        YARD = new LengthUnit("LengthUnit.yard", "LengthUnit.yd", IMPERIAL, FOOT, 3.0);
    }

    /**
     * Build a standard unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public LengthUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
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
    public LengthUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final LengthUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
    }

    /** {@inheritDoc} */
    @Override
    public final LengthUnit getStandardUnit()
    {
        return METER;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "m";
    }

}

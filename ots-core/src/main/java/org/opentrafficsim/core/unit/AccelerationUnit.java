package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.CGS;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Standard acceleration units based on distance and time.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionMay 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class AccelerationUnit extends Unit<AccelerationUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the actual length unit, e.g. KILOMETER. */
    private final LengthUnit lengthUnit;

    /** the actual time unit, e.g. HOUR. */
    private final TimeUnit timeUnit;

    /** The SI unit for acceleration is m/s^2. */
    public static final AccelerationUnit SI;

    /** m/s^2. */
    public static final AccelerationUnit METER_PER_SECOND_2;

    /** km/h^2. */
    public static final AccelerationUnit KM_PER_HOUR_2;

    /** ft/s^2. */
    public static final AccelerationUnit FOOT_PER_SECOND_2;

    /** in/s^2. */
    public static final AccelerationUnit INCH_PER_SECOND_2;

    /** mi/h^2. */
    public static final AccelerationUnit MILE_PER_HOUR_2;

    /** mi/s^2. */
    public static final AccelerationUnit MILE_PER_SECOND_2;

    /** kt/s. */
    public static final AccelerationUnit KNOT_PER_SECOND;

    /** mi/h/s. */
    public static final AccelerationUnit MILE_PER_HOUR_PER_SECOND;

    /** standard gravity. */
    public static final AccelerationUnit STANDARD_GRAVITY;

    /** standard gravity. */
    public static final AccelerationUnit GAL;

    static
    {
        SI =
                new AccelerationUnit(LengthUnit.METER, TimeUnit.SECOND, "AccelerationUnit.meter_per_second_squared",
                        "AccelerationUnit.m/s^2", SI_DERIVED);
        METER_PER_SECOND_2 = SI;
        KM_PER_HOUR_2 =
                new AccelerationUnit(LengthUnit.KILOMETER, TimeUnit.HOUR, "AccelerationUnit.km_per_hour_squared",
                        "AccelerationUnit.km/h^2", SI_DERIVED);
        FOOT_PER_SECOND_2 =
                new AccelerationUnit(LengthUnit.FOOT, TimeUnit.SECOND, "AccelerationUnit.foot_per_second_squared",
                        "AccelerationUnit.ft/s^2", IMPERIAL);
        INCH_PER_SECOND_2 =
                new AccelerationUnit(LengthUnit.INCH, TimeUnit.SECOND, "AccelerationUnit.inch_per_second_squared",
                        "AccelerationUnit.in/s^2", IMPERIAL);
        MILE_PER_HOUR_2 =
                new AccelerationUnit(LengthUnit.MILE, TimeUnit.HOUR, "AccelerationUnit.mile_per_hour_squared",
                        "AccelerationUnit.mi/h^2", IMPERIAL);
        MILE_PER_SECOND_2 =
                new AccelerationUnit(LengthUnit.MILE, TimeUnit.SECOND, "AccelerationUnit.mile_per_second_squared",
                        "AccelerationUnit.mi/s^2", IMPERIAL);
        KNOT_PER_SECOND =
                new AccelerationUnit(SpeedUnit.KNOT, TimeUnit.SECOND, "AccelerationUnit.knot_per_second",
                        "AccelerationUnit.kt/s", IMPERIAL);
        MILE_PER_HOUR_PER_SECOND =
                new AccelerationUnit(SpeedUnit.MILE_PER_HOUR, TimeUnit.SECOND,
                        "AccelerationUnit.mile_per_hour_per_second", "AccelerationUnit.mi/h/s", IMPERIAL);
        STANDARD_GRAVITY =
                new AccelerationUnit("AccelerationUnit.standard_gravity", "AccelerationUnit.g", SI_DERIVED,
                        METER_PER_SECOND_2, 9.80665);
        GAL =
                new AccelerationUnit(LengthUnit.CENTIMETER, TimeUnit.SECOND, "AccelerationUnit.gal",
                        "AccelerationUnit.Gal", CGS);
    }

    /**
     * Define acceleration units based on length and time. You can define units like meter/second^2 here.
     * @param lengthUnit the unit of length for the acceleration unit, e.g., meter
     * @param timeUnit the unit of time for the acceleration unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public AccelerationUnit(final LengthUnit lengthUnit, final TimeUnit timeUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, METER_PER_SECOND_2, lengthUnit.getConversionFactorToStandardUnit()
                / (timeUnit.getConversionFactorToStandardUnit() * timeUnit.getConversionFactorToStandardUnit()), true);
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * Define acceleration units based on speed and time. You can define units like (mile/hour)/second here.
     * @param speedUnit the unit of speed for the acceleration unit, e.g., knot
     * @param timeUnit the unit of time for the acceleration unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public AccelerationUnit(final SpeedUnit speedUnit, final TimeUnit timeUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, METER_PER_SECOND_2, speedUnit.getConversionFactorToStandardUnit()
                / timeUnit.getConversionFactorToStandardUnit(), true);
        this.lengthUnit = speedUnit.getLengthUnit();
        this.timeUnit = timeUnit;
    }

    /**
     * Create a new AccelerationUnit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public AccelerationUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final AccelerationUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
        this.lengthUnit = referenceUnit.getLengthUnit();
        this.timeUnit = referenceUnit.getTimeUnit();
    }

    /**
     * @return lengthUnit
     */
    public final LengthUnit getLengthUnit()
    {
        return this.lengthUnit;
    }

    /**
     * @return timeUnit
     */
    public final TimeUnit getTimeUnit()
    {
        return this.timeUnit;
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationUnit getStandardUnit()
    {
        return METER_PER_SECOND_2;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "m/s2";
    }

}

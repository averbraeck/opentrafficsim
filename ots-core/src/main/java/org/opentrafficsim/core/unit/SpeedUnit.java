package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * According to <a href="http://en.wikipedia.org/wiki/Velocity">Wikipedia</a>: Speed describes only how fast an object is
 * moving, whereas velocity gives both how fast and in what direction the object is moving.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SpeedUnit extends Unit<SpeedUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the unit of length for the speed unit, e.g., meter. */
    private final LengthUnit lengthUnit;

    /** the unit of time for the speed unit, e.g., second. */
    private final TimeUnit timeUnit;

    /** m/s. */
    public static final SpeedUnit METER_PER_SECOND;

    /** km/h. */
    public static final SpeedUnit KM_PER_HOUR;

    /** mile/h. */
    public static final SpeedUnit MILE_PER_HOUR;

    /** ft/s. */
    public static final SpeedUnit FOOT_PER_SECOND;

    /** knot. */
    public static final SpeedUnit KNOT;

    static
    {
        METER_PER_SECOND =
                new SpeedUnit(LengthUnit.METER, TimeUnit.SECOND, "SpeedUnit.meter_per_second", "SpeedUnit.m/s", SI_DERIVED);
        KM_PER_HOUR =
                new SpeedUnit(LengthUnit.KILOMETER, TimeUnit.HOUR, "SpeedUnit.kilometer_per_hour", "SpeedUnit.km/h", SI_DERIVED);
        MILE_PER_HOUR = new SpeedUnit(LengthUnit.MILE, TimeUnit.HOUR, "SpeedUnit.mile_per_hour", "SpeedUnit.mph", IMPERIAL);
        FOOT_PER_SECOND =
                new SpeedUnit(LengthUnit.FOOT, TimeUnit.SECOND, "SpeedUnit.foot_per_second", "SpeedUnit.fps", IMPERIAL);
        KNOT = new SpeedUnit(LengthUnit.NAUTICAL_MILE, TimeUnit.HOUR, "SpeedUnit.knot", "SpeedUnit.kt", IMPERIAL);
    }

    /**
     * Build a speed unit from a length unit and a time unit.
     * @param lengthUnit the unit of length for the speed unit, e.g., meter
     * @param timeUnit the unit of time for the speed unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public SpeedUnit(final LengthUnit lengthUnit, final TimeUnit timeUnit, final String nameKey, final String abbreviationKey,
            final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, METER_PER_SECOND, lengthUnit.getConversionFactorToStandardUnit()
                / timeUnit.getConversionFactorToStandardUnit(), true);
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * Build a speed unit based on another speed unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given reference unit
     */
    public SpeedUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final SpeedUnit referenceUnit, final double conversionFactorToReferenceUnit)
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
    public final SpeedUnit getStandardUnit()
    {
        return METER_PER_SECOND;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "m/s";
    }

}

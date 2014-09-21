package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.CGS;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.MTS;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * The units of power.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class PowerUnit extends Unit<PowerUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the unit of mass for the power unit, e.g., kilogram. */
    private final MassUnit massUnit;

    /** the unit of length for the power unit, e.g., length. */
    private final LengthUnit lengthUnit;

    /** the unit of time for the power unit, e.g., second. */
    private final TimeUnit timeUnit;

    /** watt. */
    public static final PowerUnit WATT;

    /** kilowatt. */
    public static final PowerUnit KILOWATT;

    /** megawatt. */
    public static final PowerUnit MEGAWATT;

    /** gigawatt. */
    public static final PowerUnit GIGAWATT;

    /** foot-pound-force per hour. */
    public static final PowerUnit FOOT_POUND_FORCE_PER_HOUR;

    /** foot-pound-force per minute. */
    public static final PowerUnit FOOT_POUND_FORCE_PER_MINUTE;

    /** foot-pound-force per second. */
    public static final PowerUnit FOOT_POUND_FORCE_PER_SECOND;

    /** horsepower (metric). */
    public static final PowerUnit HORSEPOWER_METRIC;

    /** sthene-meter per second. */
    public static final PowerUnit STHENE_METER_PER_SECOND;

    /** erg per second. */
    public static final PowerUnit ERG_PER_SECOND;

    static
    {
        WATT = new PowerUnit(MassUnit.KILOGRAM, LengthUnit.METER, TimeUnit.SECOND, "PowerUnit.watt", "PowerUnit.W", SI_DERIVED);
        KILOWATT = new PowerUnit("PowerUnit.kilowatt", "PowerUnit.kW", SI_DERIVED, WATT, 1000.0);
        MEGAWATT = new PowerUnit("PowerUnit.megawatt", "PowerUnit.MW", SI_DERIVED, WATT, 1.0E6);
        GIGAWATT = new PowerUnit("PowerUnit.gigawatt", "PowerUnit.GW", SI_DERIVED, WATT, 1.0E9);
        FOOT_POUND_FORCE_PER_HOUR =
                new PowerUnit(LengthUnit.FOOT, ForceUnit.POUND_FORCE, TimeUnit.HOUR, "PowerUnit.foot_pound-force_per_hour",
                        "PowerUnit.ft.lbf/h", IMPERIAL);
        FOOT_POUND_FORCE_PER_MINUTE =
                new PowerUnit(LengthUnit.FOOT, ForceUnit.POUND_FORCE, TimeUnit.MINUTE, "PowerUnit.foot_pound-force_per_minute",
                        "PowerUnit.ft.lbf/min", IMPERIAL);
        FOOT_POUND_FORCE_PER_SECOND =
                new PowerUnit(LengthUnit.FOOT, ForceUnit.POUND_FORCE, TimeUnit.SECOND, "PowerUnit.foot_pound-force_per_second",
                        "PowerUnit.ft.lbf/s", IMPERIAL);
        HORSEPOWER_METRIC = new PowerUnit("PowerUnit.horsepower_(metric)", "PowerUnit.hp", OTHER, WATT, 735.49875);
        STHENE_METER_PER_SECOND =
                new PowerUnit(LengthUnit.METER, ForceUnit.STHENE, TimeUnit.SECOND, "PowerUnit.sthene-meter_per_second",
                        "PowerUnit.sn.m/s", MTS);
        ERG_PER_SECOND =
                new PowerUnit(LengthUnit.CENTIMETER, ForceUnit.DYNE, TimeUnit.SECOND, "PowerUnit.erg_per_second",
                        "PowerUnit.erg/s", CGS);
    }

    /**
     * @param massUnit the unit of mass for the power unit, e.g., kilogram
     * @param lengthUnit the unit of length for the power unit, e.g., meter
     * @param timeUnit the unit of time for the power unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public PowerUnit(final MassUnit massUnit, final LengthUnit lengthUnit, final TimeUnit timeUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, WATT, massUnit.getConversionFactorToStandardUnit()
                * lengthUnit.getConversionFactorToStandardUnit() * lengthUnit.getConversionFactorToStandardUnit()
                / Math.pow(timeUnit.getConversionFactorToStandardUnit(), 3.0), true);
        this.massUnit = massUnit;
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @param lengthUnit the unit of length for the power unit, e.g., meter
     * @param forceUnit the unit of force for the power unit, e.g., Newton
     * @param timeUnit the unit of time for the power unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public PowerUnit(final LengthUnit lengthUnit, final ForceUnit forceUnit, final TimeUnit timeUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, WATT, lengthUnit.getConversionFactorToStandardUnit()
                * forceUnit.getConversionFactorToStandardUnit() / timeUnit.getConversionFactorToStandardUnit(), true);
        this.massUnit = forceUnit.getMassUnit();
        this.lengthUnit = forceUnit.getLengthUnit();
        this.timeUnit = forceUnit.getTimeUnit();
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given reference unit
     */
    public PowerUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final PowerUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
        this.massUnit = referenceUnit.getMassUnit();
        this.lengthUnit = referenceUnit.getLengthUnit();
        this.timeUnit = referenceUnit.getTimeUnit();
    }

    /**
     * @return massUnit
     */
    public final MassUnit getMassUnit()
    {
        return this.massUnit;
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
    public final PowerUnit getStandardUnit()
    {
        return WATT;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "kgm2/s3";
    }

}

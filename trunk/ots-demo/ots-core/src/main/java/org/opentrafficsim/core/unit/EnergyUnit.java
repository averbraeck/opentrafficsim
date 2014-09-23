package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.CGS;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.MTS;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_ACCEPTED;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * The units of energy.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class EnergyUnit extends Unit<EnergyUnit>
{
    /** */
    private static final long serialVersionUID = 20140604L;

    /** the unit of mass for the energy unit, e.g., kilogram. */
    private final MassUnit massUnit;

    /** the unit of length for the energy unit, e.g., length. */
    private final LengthUnit lengthUnit;

    /** the unit of time for the energy unit, e.g., second. */
    private final TimeUnit timeUnit;

    /** Joule. */
    public static final EnergyUnit JOULE;

    /** foot-pound force. */
    public static final EnergyUnit FOOT_POUND_FORCE;

    /** inch-pound force. */
    public static final EnergyUnit INCH_POUND_FORCE;

    /** British thermal unit (ISO). */
    public static final EnergyUnit BTU_ISO;

    /** British thermal unit (International Table). */
    public static final EnergyUnit BTU_IT;

    /** calorie (International Table). */
    public static final EnergyUnit CALORIE_IT;

    /** kilocalorie. */
    public static final EnergyUnit KILOCALORIE;

    /** watt-hour. */
    public static final EnergyUnit WATT_HOUR;

    /** kilowatt-hour. */
    public static final EnergyUnit KILOWATT_HOUR;

    /** megawatt-hour. */
    public static final EnergyUnit MEGAWATT_HOUR;

    /** gigawatt-hour. */
    public static final EnergyUnit GIGAWATT_HOUR;

    /** terawatt-hour. */
    public static final EnergyUnit TERAWATT_HOUR;

    /** milliwatt-hour. */
    public static final EnergyUnit MILLIWATT_HOUR;

    /** microwatt-hour. */
    public static final EnergyUnit MICROWATT_HOUR;

    /** electronvolt. */
    public static final EnergyUnit ELECTRONVOLT;

    /** milli-electronvolt. */
    public static final EnergyUnit MILLI_ELECTRONVOLT;

    /** kilo-electronvolt. */
    public static final EnergyUnit KILO_ELECTRONVOLT;

    /** mega-electronvolt. */
    public static final EnergyUnit MEGA_ELECTRONVOLT;

    /** giga-electronvolt. */
    public static final EnergyUnit GIGA_ELECTRONVOLT;

    /** tera-electronvolt. */
    public static final EnergyUnit TERA_ELECTRONVOLT;

    /** peta-electronvolt. */
    public static final EnergyUnit PETA_ELECTRONVOLT;

    /** exa-electronvolt. */
    public static final EnergyUnit EXA_ELECTRONVOLT;

    /** sthene-meter (mts). */
    public static final EnergyUnit STHENE_METER;

    /** erg (cgs). */
    public static final EnergyUnit ERG;

    static
    {
        JOULE =
                new EnergyUnit(MassUnit.KILOGRAM, LengthUnit.METER, TimeUnit.SECOND, "EnergyUnit.Joule",
                        "EnergyUnit.J", SI_DERIVED);
        FOOT_POUND_FORCE =
                new EnergyUnit(LengthUnit.FOOT, ForceUnit.POUND_FORCE, "EnergyUnit.foot_pound-force",
                        "EnergyUnit.ft.lbf", IMPERIAL);
        INCH_POUND_FORCE =
                new EnergyUnit(LengthUnit.INCH, ForceUnit.POUND_FORCE, "EnergyUnit.inch_pound-force",
                        "EnergyUnit.in.lbf", IMPERIAL);
        BTU_ISO =
                new EnergyUnit("EnergyUnit.British_thermal_unit_(ISO)", "EnergyUnit.BTU(ISO)", IMPERIAL, JOULE,
                        1.0545E3);
        BTU_IT =
                new EnergyUnit("EnergyUnit.British_thermal_unit_(International_Table)", "EnergyUnit.BTU(IT)", IMPERIAL,
                        JOULE, 1.05505585262E3);
        CALORIE_IT =
                new EnergyUnit("EnergyUnit.calorie_(International_Table)", "EnergyUnit.cal(IT)", OTHER, JOULE, 4.1868);
        KILOCALORIE = new EnergyUnit("EnergyUnit.kilocalorie", "EnergyUnit.kcal", OTHER, CALORIE_IT, 1000.0);
        WATT_HOUR = new EnergyUnit("EnergyUnit.watt-hour", "EnergyUnit.Wh", SI_DERIVED, JOULE, 3600.0);
        KILOWATT_HOUR = new EnergyUnit("EnergyUnit.kilowatt-hour", "EnergyUnit.kWh", SI_DERIVED, WATT_HOUR, 1000.0);
        MEGAWATT_HOUR = new EnergyUnit("EnergyUnit.megawatt-hour", "EnergyUnit.MWh", SI_DERIVED, WATT_HOUR, 1.0E6);
        GIGAWATT_HOUR = new EnergyUnit("EnergyUnit.gigawatt-hour", "EnergyUnit.GWh", SI_DERIVED, WATT_HOUR, 1.0E9);
        TERAWATT_HOUR = new EnergyUnit("EnergyUnit.terawatt-hour", "EnergyUnit.TWh", SI_DERIVED, WATT_HOUR, 1.0E12);
        MILLIWATT_HOUR = new EnergyUnit("EnergyUnit.milliwatt-hour", "EnergyUnit.mWh", SI_DERIVED, WATT_HOUR, 1.0E-3);
        MICROWATT_HOUR = new EnergyUnit("EnergyUnit.microwatt-hour", "EnergyUnit.muWh", SI_DERIVED, WATT_HOUR, 1.0E-6);
        ELECTRONVOLT =
                new EnergyUnit("EnergyUnit.electronvolt", "EnergyUnit.eV", SI_ACCEPTED, JOULE, 1.602176565314E-19);
        MILLI_ELECTRONVOLT =
                new EnergyUnit("EnergyUnit.milli-electronvolt", "EnergyUnit.meV", SI_ACCEPTED, ELECTRONVOLT, 1.0E-3);
        KILO_ELECTRONVOLT =
                new EnergyUnit("EnergyUnit.kilo-electronvolt", "EnergyUnit.keV", SI_ACCEPTED, ELECTRONVOLT, 1.0E3);
        MEGA_ELECTRONVOLT =
                new EnergyUnit("EnergyUnit.mega-electronvolt", "EnergyUnit.MeV", SI_ACCEPTED, ELECTRONVOLT, 1.0E6);
        GIGA_ELECTRONVOLT =
                new EnergyUnit("EnergyUnit.giga-electronvolt", "EnergyUnit.GeV", SI_ACCEPTED, ELECTRONVOLT, 1.0E9);
        TERA_ELECTRONVOLT =
                new EnergyUnit("EnergyUnit.tera-electronvolt", "EnergyUnit.TeV", SI_ACCEPTED, ELECTRONVOLT, 1.0E12);
        PETA_ELECTRONVOLT =
                new EnergyUnit("EnergyUnit.peta-electronvolt", "EnergyUnit.PeV", SI_ACCEPTED, ELECTRONVOLT, 1.0E15);
        EXA_ELECTRONVOLT =
                new EnergyUnit("EnergyUnit.exa-electronvolt", "EnergyUnit.EeV", SI_ACCEPTED, ELECTRONVOLT, 1.0E18);
        STHENE_METER = new EnergyUnit("EnergyUnit.sthene_meter", "EnergyUnit.sn.m", MTS, JOULE, 1000.0);
        ERG = new EnergyUnit("EnergyUnit.erg_(full)", "EnergyUnit.erg", CGS, JOULE, 1.0E-7);
    }

    /**
     * @param massUnit the unit of mass for the energy unit, e.g., kilogram
     * @param lengthUnit the unit of length for the energy unit, e.g., meter
     * @param timeUnit the unit of time for the energy unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public EnergyUnit(final MassUnit massUnit, final LengthUnit lengthUnit, final TimeUnit timeUnit,
            final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, JOULE, massUnit.getConversionFactorToStandardUnit()
                * lengthUnit.getConversionFactorToStandardUnit() * lengthUnit.getConversionFactorToStandardUnit()
                / (timeUnit.getConversionFactorToStandardUnit() * timeUnit.getConversionFactorToStandardUnit()), true);
        this.massUnit = massUnit;
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @param lengthUnit the unit of length for the energy unit, e.g., m
     * @param forceUnit the unit of force for the energy unit, e.g., Newton
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public EnergyUnit(final LengthUnit lengthUnit, final ForceUnit forceUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, JOULE, forceUnit.getConversionFactorToStandardUnit()
                * lengthUnit.getConversionFactorToStandardUnit(), true);
        this.massUnit = forceUnit.getMassUnit();
        this.lengthUnit = forceUnit.getLengthUnit();
        this.timeUnit = forceUnit.getTimeUnit();
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public EnergyUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final EnergyUnit referenceUnit, final double conversionFactorToReferenceUnit)
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
    public final EnergyUnit getStandardUnit()
    {
        return JOULE;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "kgm2/s2";
    }

}

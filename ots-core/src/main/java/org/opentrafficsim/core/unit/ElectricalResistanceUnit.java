package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * The units of electrical resistance.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ElectricalResistanceUnit extends Unit<ElectricalResistanceUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the unit of mass for the electrical resistance unit, e.g., kilogram. */
    private final MassUnit massUnit;

    /** the unit of length for the electrical resistance unit, e.g., meters. */
    private final LengthUnit lengthUnit;

    /** the unit of electrical current for the electrical resistance unit, e.g., Ampere. */
    private final ElectricalCurrentUnit electricalCurrentUnit;

    /** the unit of time for the electrical resistance unit, e.g., second. */
    private final TimeUnit timeUnit;

    /** The SI unit for electrical resistance is Ohm. */
    public static final ElectricalResistanceUnit SI;

    /** Ohm. */
    public static final ElectricalResistanceUnit OHM;

    /** milli-ohm. */
    public static final ElectricalResistanceUnit MILLIOHM;

    /** kilo-ohm. */
    public static final ElectricalResistanceUnit KILOOHM;

    /** mega-ohm. */
    public static final ElectricalResistanceUnit MEGAOHM;

    static
    {
        SI =
                new ElectricalResistanceUnit(MassUnit.KILOGRAM, LengthUnit.METER, ElectricalCurrentUnit.AMPERE,
                        TimeUnit.SECOND, "ElectricalResistanceUnit.ohm_(name)", "ElectricalResistanceUnit.ohm",
                        SI_DERIVED);
        OHM = SI;
        MILLIOHM =
                new ElectricalResistanceUnit("ElectricalResistanceUnit.milli_ohm", "ElectricalResistanceUnit.m_ohm",
                        SI_DERIVED, OHM, 0.001);
        KILOOHM =
                new ElectricalResistanceUnit("ElectricalResistanceUnit.kilo_ohm", "ElectricalResistanceUnit.k_ohm",
                        SI_DERIVED, OHM, 1000.0);
        MEGAOHM =
                new ElectricalResistanceUnit("ElectricalResistanceUnit.mega_ohm", "ElectricalResistanceUnit.M_ohm",
                        SI_DERIVED, OHM, 1.06);
    }

    /**
     * @param massUnit the unit of mass for the electrical resistance unit, e.g., kilogram
     * @param lengthUnit the unit of length for the electrical resistance unit, e.g., meter
     * @param electricalCurrentUnit the unit of electrical current for the electrical resistance unit, e.g., Ampere
     * @param timeUnit the unit of time for the electrical resistance unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public ElectricalResistanceUnit(final MassUnit massUnit, final LengthUnit lengthUnit,
            final ElectricalCurrentUnit electricalCurrentUnit, final TimeUnit timeUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, OHM, massUnit.getConversionFactorToStandardUnit()
                * lengthUnit.getConversionFactorToStandardUnit()
                * lengthUnit.getConversionFactorToStandardUnit()
                / (electricalCurrentUnit.getConversionFactorToStandardUnit()
                        * electricalCurrentUnit.getConversionFactorToStandardUnit() * Math.pow(
                        timeUnit.getConversionFactorToStandardUnit(), 3.0)), true);
        this.massUnit = massUnit;
        this.lengthUnit = lengthUnit;
        this.electricalCurrentUnit = electricalCurrentUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @param electricalPotentialUnit the unit of electrical potential difference for the electrical resistance unit,
     *            e.g., Volt
     * @param electricalCurrentUnit the unit of electrical current for the electrical resistance unit, e.g., Ampere
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public ElectricalResistanceUnit(final ElectricalPotentialUnit electricalPotentialUnit,
            final ElectricalCurrentUnit electricalCurrentUnit, final String nameKey, final String abbreviationKey,
            final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, OHM, electricalPotentialUnit.getConversionFactorToStandardUnit()
                / electricalCurrentUnit.getConversionFactorToStandardUnit(), true);
        this.massUnit = electricalPotentialUnit.getMassUnit();
        this.lengthUnit = electricalPotentialUnit.getLengthUnit();
        this.electricalCurrentUnit = electricalCurrentUnit;
        this.timeUnit = electricalPotentialUnit.getTimeUnit();
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public ElectricalResistanceUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final ElectricalResistanceUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
        this.massUnit = referenceUnit.getMassUnit();
        this.lengthUnit = referenceUnit.getLengthUnit();
        this.electricalCurrentUnit = referenceUnit.getElectricalCurrentUnit();
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
     * @return electricalCurrentUnit
     */
    public final ElectricalCurrentUnit getElectricalCurrentUnit()
    {
        return this.electricalCurrentUnit;
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
    public final ElectricalResistanceUnit getStandardUnit()
    {
        return OHM;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "kg.m2.s-3.A-2";
    }

}

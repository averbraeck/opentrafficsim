package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.CGS_EMU;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.CGS_ESU;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * The units of electrical potential (voltage).
 * <p>
 * Copyright (c) 2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ElectricalPotentialUnit extends Unit<ElectricalPotentialUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the unit of mass for the electrical potential difference (voltage) unit, e.g., kilogram. */
    private final MassUnit massUnit;

    /** the unit of length for the electrical potential difference (voltage) unit, e.g., meters. */
    private final LengthUnit lengthUnit;

    /** the unit of electrical current for the electrical potential difference (voltage) unit, e.g., Ampere. */
    private final ElectricalCurrentUnit electricalCurrentUnit;

    /** the unit of time for the electrical potential difference (voltage) unit, e.g., second. */
    private final TimeUnit timeUnit;

    /** The SI unit for electrical potential is Volt. */
    public static final ElectricalPotentialUnit SI;

    /** Volt. */
    public static final ElectricalPotentialUnit VOLT;

    /** microvolt. */
    public static final ElectricalPotentialUnit MICROVOLT;

    /** millivolt. */
    public static final ElectricalPotentialUnit MILLIVOLT;

    /** kilovolt. */
    public static final ElectricalPotentialUnit KILOVOLT;

    /** megavolt. */
    public static final ElectricalPotentialUnit MEGAVOLT;

    /** statvolt. */
    public static final ElectricalPotentialUnit STATVOLT;

    /** abvolt. */
    public static final ElectricalPotentialUnit ABVOLT;

    static
    {
        SI =
            new ElectricalPotentialUnit(MassUnit.KILOGRAM, LengthUnit.METER, ElectricalCurrentUnit.AMPERE, TimeUnit.SECOND,
                "ElectricalPotentialUnit.volt", "ElectricalPotentialUnit.V", SI_DERIVED);
        VOLT = SI;
        MICROVOLT =
            new ElectricalPotentialUnit("ElectricalPotentialUnit.microvolt", "ElectricalPotentialUnit.muV", SI_DERIVED,
                VOLT, 1.0E-6);
        MILLIVOLT =
            new ElectricalPotentialUnit("ElectricalPotentialUnit.millivolt", "ElectricalPotentialUnit.mV", SI_DERIVED, VOLT,
                0.001);
        KILOVOLT =
            new ElectricalPotentialUnit("ElectricalPotentialUnit.kilovolt", "ElectricalPotentialUnit.kV", SI_DERIVED, VOLT,
                1000.0);
        MEGAVOLT =
            new ElectricalPotentialUnit("ElectricalPotentialUnit.megavolt", "ElectricalPotentialUnit.MV", SI_DERIVED, VOLT,
                1.0E6);
        STATVOLT =
            new ElectricalPotentialUnit("ElectricalPotentialUnit.statvolt", "ElectricalPotentialUnit.statV", CGS_ESU, VOLT,
                299.792458);
        ABVOLT =
            new ElectricalPotentialUnit("ElectricalPotentialUnit.abvolt", "ElectricalPotentialUnit.abV", CGS_EMU, VOLT,
                1.0E-8);
    }

    /**
     * @param massUnit the unit of mass for the electrical potential difference (voltage) unit, e.g., kilogram
     * @param lengthUnit the unit of length for the electrical potential difference (voltage) unit, e.g., meter
     * @param electricalCurrentUnit the unit of electrical current for the electrical potential difference (voltage) unit, e.g.,
     *            Ampere
     * @param timeUnit the unit of time for the electrical potential difference (voltage) unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public ElectricalPotentialUnit(final MassUnit massUnit, final LengthUnit lengthUnit,
        final ElectricalCurrentUnit electricalCurrentUnit, final TimeUnit timeUnit, final String nameKey,
        final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, VOLT, massUnit.getConversionFactorToStandardUnit()
            * lengthUnit.getConversionFactorToStandardUnit()
            * lengthUnit.getConversionFactorToStandardUnit()
            / (electricalCurrentUnit.getConversionFactorToStandardUnit() * Math.pow(timeUnit
                .getConversionFactorToStandardUnit(), 3.0)), true);
        this.massUnit = massUnit;
        this.lengthUnit = lengthUnit;
        this.electricalCurrentUnit = electricalCurrentUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @param powerUnit the unit of power for the electrical potential difference (voltage) unit, e.g., Watt
     * @param electricalCurrentUnit the unit of electrical current for the electrical potential difference (voltage) unit, e.g.,
     *            Ampere
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public ElectricalPotentialUnit(final PowerUnit powerUnit, final ElectricalCurrentUnit electricalCurrentUnit,
        final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, VOLT, powerUnit.getConversionFactorToStandardUnit()
            / electricalCurrentUnit.getConversionFactorToStandardUnit(), true);
        this.massUnit = powerUnit.getMassUnit();
        this.lengthUnit = powerUnit.getLengthUnit();
        this.electricalCurrentUnit = electricalCurrentUnit;
        this.timeUnit = powerUnit.getTimeUnit();
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given reference unit
     */
    public ElectricalPotentialUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
        final ElectricalPotentialUnit referenceUnit, final double conversionFactorToReferenceUnit)
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
    public final ElectricalPotentialUnit getStandardUnit()
    {
        return VOLT;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "kg.m2.s-3.A-1";
    }

}

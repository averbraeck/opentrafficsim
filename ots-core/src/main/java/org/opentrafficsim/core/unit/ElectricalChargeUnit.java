package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.CGS_EMU;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.CGS_ESU;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_ACCEPTED;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Units for electrical charge.
 * <p>
 * Copyright (c) 2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version ay 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ElectricalChargeUnit extends Unit<ElectricalChargeUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the unit of electrical current, e.g., Ampere. */
    private final ElectricalCurrentUnit electricalCurrentUnit;

    /** the unit of time, e.g., second. */
    private final TimeUnit timeUnit;

    /** The SI unit for electrical charge is Coulomb = A.s. */
    public static final ElectricalChargeUnit SI;

    /** Coulomb = A.s. */
    public static final ElectricalChargeUnit COULOMB;

    /** milliampere hour. */
    public static final ElectricalChargeUnit MILLIAMPERE_HOUR;

    /** Faraday. */
    public static final ElectricalChargeUnit FARADAY;

    /** atomic unit of charge. */
    public static final ElectricalChargeUnit ATOMIC_UNIT;

    /** statcoulomb (CGS ESU). */
    public static final ElectricalChargeUnit STATCOULOMB;

    /** franklin (CGS ESU). */
    public static final ElectricalChargeUnit FRANKLIN;

    /** esu (CGS ESU). */
    public static final ElectricalChargeUnit ESU;

    /** abcoulomb (CGS EMU). */
    public static final ElectricalChargeUnit ABCOULOMB;

    /** emu (CGS EMU). */
    public static final ElectricalChargeUnit EMU;

    static
    {
        SI =
                new ElectricalChargeUnit(ElectricalCurrentUnit.AMPERE, TimeUnit.SECOND, "ElectricalChargeUnit.coulomb",
                        "ElectricalChargeUnit.C", SI_DERIVED);
        COULOMB = SI;
        MILLIAMPERE_HOUR =
                new ElectricalChargeUnit(ElectricalCurrentUnit.MILLIAMPERE, TimeUnit.HOUR,
                        "ElectricalChargeUnit.milliampere_hour", "ElectricalChargeUnit.mAh", SI_DERIVED);
        FARADAY =
                new ElectricalChargeUnit("ElectricalChargeUnit.faraday", "ElectricalChargeUnit.F", OTHER, COULOMB,
                        96485.3383);
        ATOMIC_UNIT =
                new ElectricalChargeUnit("ElectricalChargeUnit.atomic_unit_of_charge", "ElectricalChargeUnit.e",
                        SI_ACCEPTED, COULOMB, 1.6021765314E-19);
        STATCOULOMB =
                new ElectricalChargeUnit("ElectricalChargeUnit.statcoulomb", "ElectricalChargeUnit.statC", CGS_ESU,
                        COULOMB, 3.335641E-10);
        FRANKLIN =
                new ElectricalChargeUnit("ElectricalChargeUnit.franklin", "ElectricalChargeUnit.Fr", CGS_ESU,
                        STATCOULOMB, 1.0);
        ESU =
                new ElectricalChargeUnit("ElectricalChargeUnit.electrostatic_unit", "ElectricalChargeUnit.esu",
                        CGS_ESU, STATCOULOMB, 1.0);
        ABCOULOMB =
                new ElectricalChargeUnit("ElectricalChargeUnit.abcoulomb", "ElectricalChargeUnit.abC", CGS_EMU,
                        COULOMB, 10.0);
        EMU =
                new ElectricalChargeUnit("ElectricalChargeUnit.electromagnetic_unit", "ElectricalChargeUnit.emu",
                        CGS_EMU, ABCOULOMB, 1.0);
    }

    /**
     * @param electricalCurrentUnit the unit of electrical current for the electrical charge unit, e.g., meter
     * @param timeUnit the unit of time for the electrical charge unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public ElectricalChargeUnit(final ElectricalCurrentUnit electricalCurrentUnit, final TimeUnit timeUnit,
            final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, COULOMB, electricalCurrentUnit.getConversionFactorToStandardUnit()
                * timeUnit.getConversionFactorToStandardUnit(), true);
        this.electricalCurrentUnit = electricalCurrentUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public ElectricalChargeUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final ElectricalChargeUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
        this.electricalCurrentUnit = referenceUnit.getElectricalCurrentUnit();
        this.timeUnit = referenceUnit.getTimeUnit();
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
    public final ElectricalChargeUnit getStandardUnit()
    {
        return COULOMB;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "sA";
    }

}

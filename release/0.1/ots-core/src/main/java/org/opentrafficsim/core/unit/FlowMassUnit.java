package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * The mass flow rate is the mass of a substance which passes through a given surface per unit of time (wikipedia).
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class FlowMassUnit extends Unit<FlowMassUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the unit of mass for the flow unit, e.g., kilogram. */
    private final MassUnit massUnit;

    /** the unit of time for the flow unit, e.g., second. */
    private final TimeUnit timeUnit;

    /** The SI unit for mass flow rate is kg/s. */
    public static final FlowMassUnit SI;

    /** kg/s. */
    public static final FlowMassUnit KILOGRAM_PER_SECOND;

    /** lb/s. */
    public static final FlowMassUnit POUND_PER_SECOND;

    static
    {
        SI =
                new FlowMassUnit(MassUnit.KILOGRAM, TimeUnit.SECOND, "FlowMassUnit.kilogram_per_second",
                        "FlowMassUnit.kg/s", SI_DERIVED);
        KILOGRAM_PER_SECOND = SI;
        POUND_PER_SECOND =
                new FlowMassUnit(MassUnit.POUND, TimeUnit.SECOND, "FlowMassUnit.pound_per_second", "FlowMassUnit.lb/s",
                        IMPERIAL);
    }

    /**
     * Create a flow-massunit based on mass and time.
     * @param massUnit the unit of mass for the flow unit, e.g., kilogram
     * @param timeUnit the unit of time for the flow unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public FlowMassUnit(final MassUnit massUnit, final TimeUnit timeUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, KILOGRAM_PER_SECOND, massUnit.getConversionFactorToStandardUnit()
                / timeUnit.getConversionFactorToStandardUnit(), true);
        this.massUnit = massUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * Create a flow-massunit based on another flow-massunit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public FlowMassUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final FlowMassUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
        this.massUnit = referenceUnit.getMassUnit();
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
     * @return timeUnit
     */
    public final TimeUnit getTimeUnit()
    {
        return this.timeUnit;
    }

    /** {@inheritDoc} */
    @Override
    public final FlowMassUnit getStandardUnit()
    {
        return KILOGRAM_PER_SECOND;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "kg/s";
    }

}

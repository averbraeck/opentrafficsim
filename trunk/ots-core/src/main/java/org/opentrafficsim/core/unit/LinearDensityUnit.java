package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Objects per unit of distance.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 11 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LinearDensityUnit extends Unit<LinearDensityUnit>
{
    /** */
    private static final long serialVersionUID = 20141111L;

    /** the actual length unit, e.g. meter. */
    private final LengthUnit lengthUnit;

    /** 1/meter. */
    public static final LinearDensityUnit PER_METER;

    /** 1/kilometer. */
    public static final LinearDensityUnit PER_KILOMETER;

    /** 1/millimeter. */
    public static final LinearDensityUnit PER_MILLIMETER;

    static
    {
        PER_METER =
            new LinearDensityUnit(LengthUnit.METER, "LinearDensityUnit.per_meter", "LinearDensityUnit./m", SI_DERIVED);
        PER_KILOMETER =
            new LinearDensityUnit("LinearDensityUnit.per_kilometer", "LinearDensityUnit./km", SI_DERIVED, PER_METER, 0.001);
        PER_MILLIMETER =
            new LinearDensityUnit("LinearDensityUnit.per_millimeter", "LinearDensityUnit./mm", SI_DERIVED, PER_METER, 1000);
    }

    /**
     * Define frequency unit based on time. You can define unit like "per second" (Hertz) here.
     * @param lengthUnit the unit of length for the linear density unit, e.g., meter
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public LinearDensityUnit(final LengthUnit lengthUnit, final String nameKey, final String abbreviationKey,
        final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, PER_METER, 1.0 / lengthUnit.getConversionFactorToStandardUnit(), true);
        this.lengthUnit = lengthUnit;
    }

    /**
     * Build a unit with a conversion factor to another unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given reference unit
     */
    public LinearDensityUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
        final LinearDensityUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
        this.lengthUnit = referenceUnit.getLengthUnit();
    }

    /**
     * @return timeUnit
     */
    public final LengthUnit getLengthUnit()
    {
        return this.lengthUnit;
    }

    /** {@inheritDoc} */
    @Override
    public final LinearDensityUnit getStandardUnit()
    {
        return PER_METER;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "s-1";
    }

}

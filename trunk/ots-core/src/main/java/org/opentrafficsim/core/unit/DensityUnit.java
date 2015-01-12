package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Standard density units based on mass and length.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class DensityUnit extends Unit<DensityUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the actual mass unit, e.g. kg. */
    private final MassUnit massUnit;

    /** the actual length unit, e.g. meter. */
    private final LengthUnit lengthUnit;

    /** The SI unit for standard density is kg/m^3. */
    public static final DensityUnit SI;

    /** kg/m^3. */
    public static final DensityUnit KG_PER_METER_3;

    /** g/cm^3. */
    public static final DensityUnit GRAM_PER_CENTIMETER_3;

    static
    {
        SI = KG_PER_METER_3 =
            new DensityUnit(MassUnit.KILOGRAM, LengthUnit.METER, "DensityUnit.kilogram_per_cubic_meter",
                "DensityUnit.kg/m^3", SI_DERIVED);
        GRAM_PER_CENTIMETER_3 =
            new DensityUnit(MassUnit.GRAM, LengthUnit.CENTIMETER, "DensityUnit.gram_per_cubic_centimeter",
                "DensityUnit.g/cm^3", SI_DERIVED);
    }

    /**
     * Define density units based on mass and length. You can define units like kg/m^3 here.
     * @param massUnit the unit of mass for the density unit, e.g., kg
     * @param lengthUnit the unit of length for the density unit, e.g., meter
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public DensityUnit(final MassUnit massUnit, final LengthUnit lengthUnit, final String nameKey,
        final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, KG_PER_METER_3, massUnit.getConversionFactorToStandardUnit()
            / Math.pow(lengthUnit.getConversionFactorToStandardUnit(), 3.0), true);
        this.massUnit = massUnit;
        this.lengthUnit = lengthUnit;
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given reference unit
     */
    public DensityUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
        final DensityUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
        this.massUnit = referenceUnit.getMassUnit();
        this.lengthUnit = referenceUnit.getLengthUnit();
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

    /** {@inheritDoc} */
    @Override
    public final DensityUnit getStandardUnit()
    {
        return KG_PER_METER_3;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "kg/m3";
    }

}

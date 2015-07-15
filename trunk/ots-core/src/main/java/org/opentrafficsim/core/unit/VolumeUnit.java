package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_ACCEPTED;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.US_CUSTOMARY;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * VolumeUnit defines a number of common units for volumes.
 * <p>
 * Copyright (c) 2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionMay 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class VolumeUnit extends Unit<VolumeUnit>
{
    /** */
    private static final long serialVersionUID = 20140604L;

    /** The unit of length for the volume unit, e.g., meter. */
    private final LengthUnit lengthUnit;

    /** The SI unit for volume is m^3. */
    public static final VolumeUnit SI;

    /** m^3. */
    public static final VolumeUnit CUBIC_METER;

    /** dm^3. */
    public static final VolumeUnit CUBIC_DECIMETER;

    /** liter. */
    public static final VolumeUnit LITER;

    /** cm^3. */
    public static final VolumeUnit CUBIC_CENTIMETER;

    /** km^3. */
    public static final VolumeUnit CUBIC_KM;

    /** mile^3. */
    public static final VolumeUnit CUBIC_MILE;

    /** ft^3. */
    public static final VolumeUnit CUBIC_FOOT;

    /** in^3. */
    public static final VolumeUnit CUBIC_INCH;

    /** yd^3. */
    public static final VolumeUnit CUBIC_YARD;

    /** gallon (US), fluids. */
    public static final VolumeUnit GALLON_US_FLUID;

    /** gallon (imperial). */
    public static final VolumeUnit GALLON_IMP;

    /** ounce (fluid US). */
    public static final VolumeUnit OUNCE_US_FLUID;

    /** ounce (fluid imperial). */
    public static final VolumeUnit OUNCE_IMP_FLUID;

    /** pint (fluid US). */
    public static final VolumeUnit PINT_US_FLUID;

    /** pint (imperial). */
    public static final VolumeUnit PINT_IMP;

    /** quart (fluid US). */
    public static final VolumeUnit QUART_US_FLUID;

    /** quart (imperial). */
    public static final VolumeUnit QUART_IMP;

    static
    {
        SI = new VolumeUnit(LengthUnit.METER, "VolumeUnit.cubic_meter", "VolumeUnit.m^3", SI_DERIVED);
        CUBIC_METER = SI;
        CUBIC_DECIMETER =
                new VolumeUnit(LengthUnit.DECIMETER, "VolumeUnit.cubic_decimeter", "VolumeUnit.dm^3", SI_DERIVED);
        LITER = new VolumeUnit("VolumeUnit.liter", "VolumeUnit.L", SI_ACCEPTED, CUBIC_DECIMETER, 1.0);
        CUBIC_CENTIMETER =
                new VolumeUnit(LengthUnit.CENTIMETER, "VolumeUnit.cubic_centimeter", "VolumeUnit.cm^3", SI_DERIVED);
        CUBIC_KM = new VolumeUnit(LengthUnit.KILOMETER, "VolumeUnit.cubic_kilometer", "VolumeUnit.km^3", SI_DERIVED);
        CUBIC_MILE = new VolumeUnit(LengthUnit.MILE, "VolumeUnit.cubic_mile", "VolumeUnit.mi^3", IMPERIAL);
        CUBIC_FOOT = new VolumeUnit(LengthUnit.FOOT, "VolumeUnit.cubic_foot", "VolumeUnit.ft^3", IMPERIAL);
        CUBIC_INCH = new VolumeUnit(LengthUnit.INCH, "VolumeUnit.cubic_inch", "VolumeUnit.in^3", IMPERIAL);
        CUBIC_YARD = new VolumeUnit(LengthUnit.YARD, "VolumeUnit.cubic_yard", "VolumeUnit.yd^3", IMPERIAL);
        GALLON_US_FLUID =
                new VolumeUnit("VolumeUnit.gallon_(US)", "VolumeUnit.gal(US)", US_CUSTOMARY, CUBIC_INCH, 231.0);
        GALLON_IMP = new VolumeUnit("VolumeUnit.gallon_(imp)", "VolumeUnit.gal(imp)", IMPERIAL, LITER, 4.5409);
        OUNCE_US_FLUID =
                new VolumeUnit("VolumeUnit.ounce_(fluid_US)", "VolumeUnit.US_fl_oz", US_CUSTOMARY, GALLON_US_FLUID,
                        1.0 / 128.0);
        OUNCE_IMP_FLUID =
                new VolumeUnit("VolumeUnit.ounce_(fluid_imperial)", "VolumeUnit.fl_oz_(imp)", IMPERIAL, GALLON_IMP,
                        1.0 / 160.0);
        PINT_US_FLUID =
                new VolumeUnit("VolumeUnit.pint_(US_fluid)", "VolumeUnit.pt(US_fl)", US_CUSTOMARY, GALLON_US_FLUID,
                        1.0 / 8.0);
        PINT_IMP = new VolumeUnit("VolumeUnit.pint_(imperial)", "VolumeUnit.pt_(imp)", IMPERIAL, GALLON_IMP, 1.0 / 8.0);
        QUART_US_FLUID =
                new VolumeUnit("VolumeUnit.quart_(US_fluid)", "VolumeUnit.qt(US_fl)", US_CUSTOMARY, GALLON_US_FLUID,
                        1.0 / 4.0);
        QUART_IMP =
                new VolumeUnit("VolumeUnit.quart_(imperial)", "VolumeUnit.qt_(imp)", IMPERIAL, GALLON_IMP, 1.0 / 4.0);
    }

    /**
     * Define volume unit based on length, e.g. a m^3 is based on meters.
     * @param lengthUnit the unit of length for the speed unit, e.g., meter
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public VolumeUnit(final LengthUnit lengthUnit, final String nameKey, final String abbreviationKey,
            final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, CUBIC_METER, Math.pow(
                lengthUnit.getConversionFactorToStandardUnit(), 3), true);
        this.lengthUnit = lengthUnit;
    }

    /**
     * This constructor constructs a unit out of another defined unit, e.g. quart is 0.25 gallon.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public VolumeUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final VolumeUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
        this.lengthUnit = referenceUnit.getLengthUnit();
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
    public final VolumeUnit getStandardUnit()
    {
        return CUBIC_METER;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "m3";
    }

}

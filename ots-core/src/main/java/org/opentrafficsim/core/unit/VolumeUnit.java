package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_ACCEPTED;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.US_CUSTOMARY;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * VolumeUnit defines a number of common units for volumes.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.opentrafficsim.org/"> www.opentrafficsim.org</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class VolumeUnit extends Unit<VolumeUnit>
{
    /** */
    private static final long serialVersionUID = 20140604L;

    /** the unit of length for the volume unit, e.g., meter */
    private final LengthUnit lengthUnit;

    /** m^3 */
    public static final VolumeUnit CUBIC_METER = new VolumeUnit(LengthUnit.METER, "VolumeUnit.cubic_meter",
            "VolumeUnit.m^3", SI_DERIVED);

    /** dm^3 */
    public static final VolumeUnit CUBIC_DECIMETER = new VolumeUnit(LengthUnit.DECIMETER, "VolumeUnit.cubic_decimeter",
            "VolumeUnit.dm^3", SI_DERIVED);

    /** liter */
    public static final VolumeUnit LITER = new VolumeUnit("VolumeUnit.liter", "VolumeUnit.L", SI_ACCEPTED,
            CUBIC_DECIMETER, 1.0);

    /** cm^3 */
    public static final VolumeUnit CUBIC_CENTIMETER = new VolumeUnit(LengthUnit.CENTIMETER,
            "VolumeUnit.cubic_centimeter", "VolumeUnit.cm^3", SI_DERIVED);

    /** km^3 */
    public static final VolumeUnit CUBIC_KM = new VolumeUnit(LengthUnit.KILOMETER, "VolumeUnit.cubic_kilometer",
            "VolumeUnit.km^3", SI_DERIVED);

    /** mile^3 */
    public static final VolumeUnit CUBIC_MILE = new VolumeUnit(LengthUnit.MILE, "VolumeUnit.cubic_mile",
            "VolumeUnit.mi^3", IMPERIAL);

    /** ft^3 */
    public static final VolumeUnit CUBIC_FOOT = new VolumeUnit(LengthUnit.FOOT, "VolumeUnit.cubic_foot",
            "VolumeUnit.ft^3", IMPERIAL);

    /** in^3 */
    public static final VolumeUnit CUBIC_INCH = new VolumeUnit(LengthUnit.INCH, "VolumeUnit.cubic_inch",
            "VolumeUnit.in^3", IMPERIAL);

    /** yd^3 */
    public static final VolumeUnit CUBIC_YARD = new VolumeUnit(LengthUnit.YARD, "VolumeUnit.cubic_yard",
            "VolumeUnit.yd^3", IMPERIAL);

    /** gallon (US), fluids */
    public static final VolumeUnit GALLON_US_FLUID = new VolumeUnit("VolumeUnit.gallon_(US)", "VolumeUnit.gal(US)",
            US_CUSTOMARY, CUBIC_INCH, 231.0);

    /** gallon (imperial) */
    public static final VolumeUnit GALLON_IMP = new VolumeUnit("VolumeUnit.gallon_(imp)", "VolumeUnit.gal(imp)",
            IMPERIAL, LITER, 4.5409);

    /** ounce (fluid US) */
    public static final VolumeUnit OUNCE_US_FLUID = new VolumeUnit("VolumeUnit.ounce_(fluid_US)",
            "VolumeUnit.US_fl_oz", US_CUSTOMARY, GALLON_US_FLUID, 1.0 / 128.0);

    /** ounce (fluid imperial) */
    public static final VolumeUnit OUNCE_IMP_FLUID = new VolumeUnit("VolumeUnit.ounce_(fluid_imperial)",
            "VolumeUnit.fl_oz_(imp)", IMPERIAL, GALLON_IMP, 1.0 / 160.0);

    /** pint (fluid US) */
    public static final VolumeUnit PINT_US_FLUID = new VolumeUnit("VolumeUnit.pint_(US_fluid)", "VolumeUnit.pt(US_fl)",
            US_CUSTOMARY, GALLON_US_FLUID, 1.0 / 8.0);

    /** pint (imperial) */
    public static final VolumeUnit PINT_IMP = new VolumeUnit("VolumeUnit.pint_(imperial)", "VolumeUnit.pt_(imp)",
            IMPERIAL, GALLON_IMP, 1.0 / 8.0);

    /** quart (fluid US) */
    public static final VolumeUnit QUART_US_FLUID = new VolumeUnit("VolumeUnit.quart_(US_fluid)",
            "VolumeUnit.qt(US_fl)", US_CUSTOMARY, GALLON_US_FLUID, 1.0 / 4.0);

    /** quart (imperial) */
    public static final VolumeUnit QUART_IMP = new VolumeUnit("VolumeUnit.quart_(imperial)", "VolumeUnit.qt_(imp)",
            IMPERIAL, GALLON_IMP, 1.0 / 4.0);

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
    public LengthUnit getLengthUnit()
    {
        return this.lengthUnit;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getStandardUnit()
     */
    @Override
    public VolumeUnit getStandardUnit()
    {
        return CUBIC_METER;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getSICoefficientsString()
     */
    @Override
    public String getSICoefficientsString()
    {
        return "m3";
    }

}

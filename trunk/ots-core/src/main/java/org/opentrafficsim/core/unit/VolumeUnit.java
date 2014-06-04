package org.opentrafficsim.core.unit;

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
 * @param <L> the length unit type
 */
public class VolumeUnit<L extends LengthUnit> extends Unit<VolumeUnit<L>>
{
    /** */
    private static final long serialVersionUID = 20140604L;

    /** the unit of length for the area unit, e.g., meter */
    private final L lengthUnit;

    /** m^3 */
    public static final VolumeUnit<LengthUnit> CUBIC_METER = new VolumeUnit<LengthUnit>(LengthUnit.METER,
            "VolumeUnit.cubic_meter", "VolumeUnit.m^3");

    /** dm^3 */
    public static final VolumeUnit<LengthUnit> CUBIC_DECIMETER = new VolumeUnit<LengthUnit>(LengthUnit.DECIMETER,
            "VolumeUnit.cubic_decimeter", "VolumeUnit.dm^3");

    /** liter */
    public static final VolumeUnit<LengthUnit> LITER = new VolumeUnit<LengthUnit>("VolumeUnit.liter", "VolumeUnit.L",
            CUBIC_DECIMETER, 1.0);

    /** cm^3 */
    public static final VolumeUnit<LengthUnit> CUBIC_CENTIMETER = new VolumeUnit<LengthUnit>(LengthUnit.CENTIMETER,
            "VolumeUnit.cubic_centimeter", "VolumeUnit.cm^3");

    /** km^3 */
    public static final VolumeUnit<LengthUnit> CUBIC_KM = new VolumeUnit<LengthUnit>(LengthUnit.KILOMETER,
            "VolumeUnit.cubic_kilometer", "VolumeUnit.km^3");

    /** mile^3 */
    public static final VolumeUnit<LengthUnit> CUBIC_MILE = new VolumeUnit<LengthUnit>(LengthUnit.MILE,
            "VolumeUnit.cubic_mile", "VolumeUnit.mi^3");

    /** ft^3 */
    public static final VolumeUnit<LengthUnit> CUBIC_FOOT = new VolumeUnit<LengthUnit>(LengthUnit.FOOT,
            "VolumeUnit.cubic_foot", "VolumeUnit.ft^3");

    /** in^3 */
    public static final VolumeUnit<LengthUnit> CUBIC_INCH = new VolumeUnit<LengthUnit>(LengthUnit.INCH,
            "VolumeUnit.cubic_inch", "VolumeUnit.in^3");

    /** yd^3 */
    public static final VolumeUnit<LengthUnit> CUBIC_YARD = new VolumeUnit<LengthUnit>(LengthUnit.YARD,
            "VolumeUnit.cubic_yard", "VolumeUnit.yd^3");

    /** gallon (US), fluids */
    public static final VolumeUnit<LengthUnit> GALLON_US_FLUID = new VolumeUnit<LengthUnit>("VolumeUnit.gallon_(US)",
            "VolumeUnit.gal(US)", CUBIC_INCH, 231.0);

    /** gallon (imperial) */
    public static final VolumeUnit<LengthUnit> GALLON_IMP = new VolumeUnit<LengthUnit>("VolumeUnit.gallon_(imp)",
            "VolumeUnit.gal(imp)", LITER, 4.5409);

    /** ounce (fluid US) */
    public static final VolumeUnit<LengthUnit> OUNCE_US_FLUID = new VolumeUnit<LengthUnit>("VolumeUnit.US_fl_oz",
            "VolumeUnit.ounce_(fluid_US)", CUBIC_INCH, 231.0);

    /** ounce (fluid imperial) */
    public static final VolumeUnit<LengthUnit> OUNCE_IMP_FLUID = new VolumeUnit<LengthUnit>("VolumeUnit.fl_oz_(imp)",
            "VolumeUnit.ounce_(fluid_imperial)", GALLON_IMP, 1.0 / 160.0);

    /** pint (fluid US) */
    public static final VolumeUnit<LengthUnit> PINT_US_FLUID = new VolumeUnit<LengthUnit>("VolumeUnit.pt(US_fl)",
            "VolumeUnit.pint_(US_fluid)", GALLON_US_FLUID, 1.0 / 8.0);

    /** pint (imperial) */
    public static final VolumeUnit<LengthUnit> PINT_IMP = new VolumeUnit<LengthUnit>("VolumeUnit.pt_(imp)",
            "VolumeUnit.pint_(imperial)", GALLON_IMP, 1.0 / 8.0);

    /** quart (fluid US) */
    public static final VolumeUnit<LengthUnit> QUART_US_FLUID = new VolumeUnit<LengthUnit>("VolumeUnit.qt(US_fl)",
            "VolumeUnit.quart_(US_fluid)", GALLON_US_FLUID, 1.0 / 4.0);

    /** quart (imperial) */
    public static final VolumeUnit<LengthUnit> QUART_IMP = new VolumeUnit<LengthUnit>("VolumeUnit.qt_(imp)",
            "VolumeUnit.quart_(imperial)", GALLON_IMP, 1.0 / 4.0);

    /**
     * @param lengthUnit the unit of length for the speed unit, e.g., meter
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public VolumeUnit(final L lengthUnit, final String nameKey, final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, Math.pow(lengthUnit.getConversionFactorToStandardUnit(), 3.0));
        this.lengthUnit = lengthUnit;
    }

    /**
     * This constructor constructs a unit out of another defined unit, e.g. an are is 100 m^3.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param standardUnit the snatdard unit from which this unit is derived with a conversion factor
     * @param conversionFactorToStandardUnit multiply by this number to convert to the standard (e.g., SI) unit
     */
    public VolumeUnit(final String nameKey, final String abbreviationKey, final VolumeUnit<L> standardUnit,
            final double conversionFactorToStandardUnit)
    {
        super(nameKey, abbreviationKey, conversionFactorToStandardUnit);
        this.lengthUnit = standardUnit.getLengthUnit();
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getMultiplicationFactorTo(org.opentrafficsim.core.unit.Unit)
     */
    @Override
    public double getMultiplicationFactorTo(VolumeUnit<L> unit)
    {
        return this.conversionFactorToStandardUnit / unit.getConversionFactorToStandardUnit();
    }

    /**
     * @return lengthUnit
     */
    public L getLengthUnit()
    {
        return this.lengthUnit;
    }

}

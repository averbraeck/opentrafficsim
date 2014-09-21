package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * AreaUnit defines a number of common units for areas.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.opentrafficsim.org/"> www.opentrafficsim.org</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties, including,
 * but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no
 * event shall the copyright holder or contributors be liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or
 * profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or
 * tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class AreaUnit extends Unit<AreaUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the unit of length for the area unit, e.g., meter. */
    private final LengthUnit lengthUnit;

    /** m^2. */
    public static final AreaUnit SQUARE_METER;

    /** km^2. */
    public static final AreaUnit SQUARE_KM;

    /** cm^2. */
    public static final AreaUnit SQUARE_CENTIMETER;

    /** cm^2. */
    public static final AreaUnit SQUARE_MILLIMETER;

    /** are. */
    public static final AreaUnit ARE;

    /** hectare. */
    public static final AreaUnit HECTARE;

    /** mile^2. */
    public static final AreaUnit SQUARE_MILE;

    /** ft^2. */
    public static final AreaUnit SQUARE_FOOT;

    /** in^2. */
    public static final AreaUnit SQUARE_INCH;

    /** yd^2. */
    public static final AreaUnit SQUARE_YARD;

    /** acre (international). */
    public static final AreaUnit ACRE;

    static
    {
        SQUARE_METER = new AreaUnit(LengthUnit.METER, "AreaUnit.square_meter", "AreaUnit.m^2", SI_DERIVED);
        SQUARE_KM = new AreaUnit(LengthUnit.KILOMETER, "AreaUnit.square_kilometer", "AreaUnit.km^2", SI_DERIVED);
        SQUARE_CENTIMETER = new AreaUnit(LengthUnit.CENTIMETER, "AreaUnit.square_centimeter", "AreaUnit.cm^2", SI_DERIVED);
        SQUARE_MILLIMETER = new AreaUnit(LengthUnit.MILLIMETER, "AreaUnit.square_millimeter", "AreaUnit.mm^2", SI_DERIVED);
        ARE = new AreaUnit("AreaUnit.are", "AreaUnit.a", OTHER, SQUARE_METER, 100.0);
        HECTARE = new AreaUnit("AreaUnit.hectare", "AreaUnit.ha", OTHER, ARE, 100.0);
        SQUARE_MILE = new AreaUnit(LengthUnit.MILE, "AreaUnit.square_mile", "AreaUnit.mi^2", IMPERIAL);
        SQUARE_FOOT = new AreaUnit(LengthUnit.FOOT, "AreaUnit.square_foot", "AreaUnit.ft^2", IMPERIAL);
        SQUARE_INCH = new AreaUnit(LengthUnit.INCH, "AreaUnit.square_inch", "AreaUnit.in^2", IMPERIAL);
        SQUARE_YARD = new AreaUnit(LengthUnit.YARD, "AreaUnit.square_yard", "AreaUnit.yd^2", IMPERIAL);
        ACRE = new AreaUnit("AreaUnit.acre", "AreaUnit.ac", IMPERIAL, SQUARE_YARD, 4840.0);
    }

    /**
     * Define area unit based on length.
     * @param lengthUnit the unit of length for the area unit, e.g., meter
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public AreaUnit(final LengthUnit lengthUnit, final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, SQUARE_METER, lengthUnit.getConversionFactorToStandardUnit()
                * lengthUnit.getConversionFactorToStandardUnit(), true);
        this.lengthUnit = lengthUnit;
    }

    /**
     * This constructor constructs a unit out of another defined unit, e.g. an are is 100 m^2.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given reference unit
     */
    public AreaUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final AreaUnit referenceUnit, final double conversionFactorToReferenceUnit)
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
    public final AreaUnit getStandardUnit()
    {
        return SQUARE_METER;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "m2";
    }

}

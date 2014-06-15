package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Standard density units based on mass and length.
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
public class DensityUnit extends Unit<DensityUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the actual mass unit, e.g. kg */
    private final MassUnit massUnit;

    /** the actual length unit, e.g. meter */
    private final LengthUnit lengthUnit;

    /** kg/m^3 */
    public static final DensityUnit KG_PER_METER_3 = new DensityUnit(MassUnit.KILOGRAM, LengthUnit.METER,
            "DensityUnit.kilogram_per_cubic_meter", "DensityUnit.kg/m^3", SI_DERIVED);

    /** g/cm^3 */
    public static final DensityUnit GRAM_PER_CENTIMETER_3 = new DensityUnit(MassUnit.GRAM, LengthUnit.CENTIMETER,
            "DensityUnit.gram_per_cubic_centimeter", "DensityUnit.g/cm^3", SI_DERIVED);

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
                / Math.pow(lengthUnit.getConversionFactorToStandardUnit(), 3.0));
        this.massUnit = massUnit;
        this.lengthUnit = lengthUnit;
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public DensityUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final DensityUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit);
        this.massUnit = referenceUnit.getMassUnit();
        this.lengthUnit = referenceUnit.getLengthUnit();
    }

    /**
     * @return massUnit
     */
    public MassUnit getMassUnit()
    {
        return this.massUnit;
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
    public DensityUnit getStandardUnit()
    {
        return KG_PER_METER_3;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getSICoefficientsString()
     */
    @Override
    public String getSICoefficientsString()
    {
        return "kg/m3";
    }

}
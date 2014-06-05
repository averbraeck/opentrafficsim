package org.opentrafficsim.core.unit;

/**
 * The OffsetUnit provides a unit where scales can have an offset, such as the temperature scale. When converting a
 * value of such a unit into another unit: first bring back the unit to the base unit by subtracting the offset, then
 * scale back to the base unit by dividing by the conversion factor, then multiply by the conversion factor of the
 * target unit, and add the offset of the target unit.
 * <p>
 * By the way, all units are internally <u>stored</u> as a standard unit with an offset and a conversion factor. This
 * means that e.g., Kelvin is stored with offset 0.0 and conversion factor 1.0, whereas degree Celcius is stored with
 * offset -273.15 and conversion factor 1.0. This means that if we have a Temperature, it is stored in Kelvins, and if
 * we want to display it in degree Celcius, we have to <u>multiply<u> by the conversion factor and <u>add</u> the
 * offset.
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
 * @version Jun 5, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <U> the unit type
 */
public abstract class OffsetUnit<U extends Unit<U>> extends Unit<U>
{
    /** */
    private static final long serialVersionUID = 20140605L;

    /** the offset that has to be taken into account for conversions */
    private final double offsetFromStandardUnit;

    /**
     * Build a unit with a conversion factor and an offset to the standard (preferably SI) unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param conversionFactorFromStandardUnit multiply by this number to convert from the standard (e.g., SI) unit
     * @param offsetFromStandardUnit the offset to add to convert from the standard (e.g., SI) unit
     */
    public OffsetUnit(final String nameKey, final String abbreviationKey,
            final double conversionFactorFromStandardUnit, final double offsetFromStandardUnit)
    {
        super(nameKey, abbreviationKey, conversionFactorFromStandardUnit);
        this.offsetFromStandardUnit = offsetFromStandardUnit;
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param referenceUnit the unit to convert from
     * @param conversionFactorFromReferenceUnit multiply by this number to convert from the reference unit
     * @param offsetFromStandardUnit the offset to add to convert from the standard (e.g., SI) unit
     */
    public OffsetUnit(final String nameKey, final String abbreviationKey, final U referenceUnit,
            final double conversionFactorFromReferenceUnit, final double offsetFromStandardUnit)
    {
        super(nameKey, abbreviationKey, referenceUnit, conversionFactorFromReferenceUnit);
        this.offsetFromStandardUnit = offsetFromStandardUnit;
    }

    /**
     * @return offset
     */
    public double getOffset()
    {
        return this.offsetFromStandardUnit;
    }

}

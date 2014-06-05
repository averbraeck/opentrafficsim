package org.opentrafficsim.core.unit;

import java.io.Serializable;

/**
 * All units are internally <u>stored</u> as a standard unit with conversion factor. This means that e.g., meter is
 * stored with offset 0.0 and conversion factor 1.0, whereas kilometer is stored with a conversion factor 1000.0. This
 * means that if we have a length or distance, it is internally stored in meters, and if we want to display it in
 * kilometers, we have to <u>multiply<u> by the conversion factor.
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
 * @param <U> the unit for transformation reasons
 */
public abstract class Unit<U extends Unit<U>> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140603;

    /** the key to the locale file for the long name of the unit */
    private final String nameKey;

    /** the key to the locale file for the abbreviation of the unit */
    private final String abbreviationKey;

    /** multiply by this number to convert to the standard (e.g., SI) unit */
    private final double conversionFactorFromStandardUnit;

    /**
     * Build a unit with a conversion factor to the standard (preferably SI) unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param conversionFactorFromStandardUnit multiply by this number to convert from the standard (e.g., SI) unit
     */
    public Unit(final String nameKey, final String abbreviationKey, final double conversionFactorFromStandardUnit)
    {
        this.conversionFactorFromStandardUnit = conversionFactorFromStandardUnit;
        this.nameKey = nameKey;
        this.abbreviationKey = abbreviationKey;
    }

    /**
     * Build a unit with a conversion factor to another unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param referenceUnit the unit to convert from
     * @param conversionFactorFromReferenceUnit multiply by this number to convert from the reference unit
     */
    public Unit(final String nameKey, final String abbreviationKey, final U referenceUnit,
            final double conversionFactorFromReferenceUnit)
    {
        this(nameKey, abbreviationKey, referenceUnit.getConversionFactorFromStandardUnit()
                * conversionFactorFromReferenceUnit);
    }

    /**
     * @return name, e.g. meters per second
     */
    public String getName()
    {
        return UnitLocale.getString(this.nameKey);
    }

    /**
     * @return name key, e.g. TimeUnit.MetersPerSecond
     */
    public String getNameKey()
    {
        return this.nameKey;
    }

    /**
     * @return abbreviation, e.g., m/s
     */
    public String getAbbreviation()
    {
        return UnitLocale.getString(this.abbreviationKey);
    }

    /**
     * @return abbreviation key, e.g. TimeUnit.m/s
     */
    public String getAbbreviationKey()
    {
        return this.abbreviationKey;
    }

    /**
     * @return conversionFactorFromStandardUnit. Multiply by this number to convert from the standard (e.g., SI) unit
     */
    public double getConversionFactorFromStandardUnit()
    {
        return this.conversionFactorFromStandardUnit;
    }

    /**
     * @param unit the unit to convert to
     * @return multiplication factor to convert value to other unit
     */
    public double getMultiplicationFactorTo(U unit)
    {
        return this.conversionFactorFromStandardUnit / unit.getConversionFactorFromStandardUnit();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getAbbreviation();
    }

}

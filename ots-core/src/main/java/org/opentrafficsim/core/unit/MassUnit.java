package org.opentrafficsim.core.unit;

/**
 * Standard mass units. Several conversion factors have been taken from <a
 * href="http://en.wikipedia.org/wiki/Conversion_of_units">http://en.wikipedia.org/wiki/Conversion_of_units</a>.
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
public class MassUnit extends Unit<MassUnit>
{
    /** */
    private static final long serialVersionUID = 20140604L;

    /** kilogram */
    public static final MassUnit KILOGRAM = new MassUnit("MassUnit.kilogram", "MassUnit.kg", 1.0);

    /** gram */
    public static final MassUnit GRAM = new MassUnit("MassUnit.gram", "MassUnit.g", 0.001);

    /** pound */
    public static final MassUnit POUND = new MassUnit("MassUnit.pound", "MassUnit.lb", 0.45359237);

    /** pound */
    public static final MassUnit OUNCE = new MassUnit("MassUnit.ounce", "MassUnit.oz", POUND, 1.0 / 16.0);

    /** long ton = 2240 lb */
    public static final MassUnit TON_LONG = new MassUnit("MassUnit.long_ton", "MassUnit.long_tn", POUND, 2240.0);

    /** short ton = 2000 lb */
    public static final MassUnit TON_SHORT = new MassUnit("MassUnit.short_ton", "MassUnit.sh_tn", POUND, 2000.0);

    /** metric ton = 1000 kg */
    public static final MassUnit TON_METRIC = new MassUnit("MassUnit.metric_ton", "MassUnit.t", KILOGRAM, 1000.0);

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param convertFromKilogram multiply by this number to convert from kilograms
     */
    public MassUnit(final String nameKey, final String abbreviationKey, final double convertFromKilogram)
    {
        super(nameKey, abbreviationKey, convertFromKilogram);
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param referenceUnit the unit to convert from
     * @param conversionFactorFromReferenceUnit multiply by this number to convert from the reference unit
     */
    public MassUnit(String nameKey, String abbreviationKey, MassUnit referenceUnit,
            double conversionFactorFromReferenceUnit)
    {
        super(nameKey, abbreviationKey, referenceUnit, conversionFactorFromReferenceUnit);
    }

}

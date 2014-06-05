package org.opentrafficsim.core.unit;

/**
 * Standard time units.
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
public class TimeUnit extends Unit<TimeUnit>
{
    /** */
    private static final long serialVersionUID = 20140603L;

    /** millisecond */
    public static final TimeUnit MILLISECOND = new TimeUnit("TimeUnit.millisecond", "TimeUnit.ms", 0.001);

    /** second */
    public static final TimeUnit SECOND = new TimeUnit("TimeUnit.second", "TimeUnit.s", 1.0);

    /** minute */
    public static final TimeUnit MINUTE = new TimeUnit("TimeUnit.minute", "TimeUnit.m", SECOND, 60.0);

    /** hour */
    public static final TimeUnit HOUR = new TimeUnit("TimeUnit.hour", "TimeUnit.h", MINUTE, 60.0);

    /** day */
    public static final TimeUnit DAY = new TimeUnit("TimeUnit.day", "TimeUnit.d", HOUR, 24.0);

    /** week */
    public static final TimeUnit WEEK = new TimeUnit("TimeUnit.week", "TimeUnit.w", DAY, 7.0);

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param convertToSecond multiply by this number to convert to seconds
     */
    public TimeUnit(final String nameKey, final String abbreviationKey, final double convertToSecond)
    {
        super(nameKey, abbreviationKey, convertToSecond);
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public TimeUnit(String nameKey, String abbreviationKey, TimeUnit referenceUnit,
            double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, referenceUnit, conversionFactorToReferenceUnit);
    }

}

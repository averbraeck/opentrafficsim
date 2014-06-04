package org.opentrafficsim.core.unit;

/**
 * Standard frequency units based on time.
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
 * @param <T> the time unit
 */
public class FrequencyUnit<T extends TimeUnit> extends Unit<FrequencyUnit<T>>
{
    /** */
    private static final long serialVersionUID = 20140604L;

    /** the actual time unit, e.g. second */
    private final T timeUnit;

    /** hertz */
    public static final FrequencyUnit<TimeUnit> HERTZ = new FrequencyUnit<TimeUnit>(TimeUnit.SECOND,
            "FrequencyUnit.Hertz", "FrequencyUnit.Hz");

    /** kilohertz */
    public static final FrequencyUnit<TimeUnit> KILOHERTZ = new FrequencyUnit<TimeUnit>("FrequencyUnit.kilohertz",
            "FrequencyUnit.kHz", HERTZ, 1000.0);

    /** megahertz */
    public static final FrequencyUnit<TimeUnit> MEGAHERTZ = new FrequencyUnit<TimeUnit>("FrequencyUnit.megahertz",
            "FrequencyUnit.MHz", HERTZ, 1.0E6);

    /** gigahertz */
    public static final FrequencyUnit<TimeUnit> GIGAHERTZ = new FrequencyUnit<TimeUnit>("FrequencyUnit.gigahertz",
            "FrequencyUnit.GHz", HERTZ, 1.0E9);

    /** terahertz */
    public static final FrequencyUnit<TimeUnit> TERAHERTZ = new FrequencyUnit<TimeUnit>("FrequencyUnit.terahertz",
            "FrequencyUnit.THz", HERTZ, 1.0E12);

    /** revolutions per minute = 1/60 Hz */
    public static final FrequencyUnit<TimeUnit> RPM = new FrequencyUnit<TimeUnit>(
            "FrequencyUnit.revolutions_per_minute", "FrequencyUnit.rpm", HERTZ, 1.0 / 60.0);

    /**
     * Define frequency units based on length and time. You can define units like "per second" (Hertz) here.
     * @param timeUnit the unit of time for the frequency unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public FrequencyUnit(final T timeUnit, final String nameKey, final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, 1.0 / timeUnit.getConversionFactorToStandardUnit());
        this.timeUnit = timeUnit;
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param referenceUnit the unit to convert from
     * @param conversionFactorToReferenceUnit multiply by this number to convert from the reference unit
     */
    public FrequencyUnit(final String nameKey, final String abbreviationKey, final FrequencyUnit<T> referenceUnit,
            final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, referenceUnit, conversionFactorToReferenceUnit);
        this.timeUnit = referenceUnit.getTimeUnit();
    }

    /**
     * @return timeUnit
     */
    public T getTimeUnit()
    {
        return this.timeUnit;
    }

}
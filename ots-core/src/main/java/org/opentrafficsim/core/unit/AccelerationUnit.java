package org.opentrafficsim.core.unit;

/**
 * Standard acceleration units based on distance and time.
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
 * @param <T> the time unit type
 */
public class AccelerationUnit<L extends LengthUnit, T extends TimeUnit> extends Unit<AccelerationUnit<L, T>>
{
    /** */
    private static final long serialVersionUID = 20140603L;

    /** the actual length unit, e.g. KILOMETER */
    private final L lengthUnit;

    /** the actual time unit, e.g. HOUR */
    private final T timeUnit;

    /** km/h^2 */
    public static final AccelerationUnit<LengthUnit, TimeUnit> KM_PER_HOUR_2 =
            new AccelerationUnit<LengthUnit, TimeUnit>(LengthUnit.KILOMETER, TimeUnit.HOUR,
                    "AccelerationUnit.km_per_hour_squared", "AccelerationUnit.km/h^2");

    /** m/s^2 */
    public static final AccelerationUnit<LengthUnit, TimeUnit> METER_PER_SECOND_2 =
            new AccelerationUnit<LengthUnit, TimeUnit>(LengthUnit.METER, TimeUnit.SECOND,
                    "AccelerationUnit.meter_per_second_squared", "AccelerationUnit.m/s^2");

    /** ft/s^2 */
    public static final AccelerationUnit<LengthUnit, TimeUnit> FOOT_PER_SECOND_2 =
            new AccelerationUnit<LengthUnit, TimeUnit>(LengthUnit.FOOT, TimeUnit.SECOND,
                    "AccelerationUnit.foot_per_second_squared", "AccelerationUnit.ft/s^2");

    /** in/s^2 */
    public static final AccelerationUnit<LengthUnit, TimeUnit> INCH_PER_SECOND_2 =
            new AccelerationUnit<LengthUnit, TimeUnit>(LengthUnit.INCH, TimeUnit.SECOND,
                    "AccelerationUnit.inch_per_second_squared", "AccelerationUnit.in/s^2");

    /** mi/h^2 */
    public static final AccelerationUnit<LengthUnit, TimeUnit> MILE_PER_HOUR_2 =
            new AccelerationUnit<LengthUnit, TimeUnit>(LengthUnit.MILE, TimeUnit.HOUR,
                    "AccelerationUnit.mile_per_hour_squared", "AccelerationUnit.mi/h^2");

    /** mi/s^2 */
    public static final AccelerationUnit<LengthUnit, TimeUnit> MILE_PER_SECOND_2 =
            new AccelerationUnit<LengthUnit, TimeUnit>(LengthUnit.MILE, TimeUnit.SECOND,
                    "AccelerationUnit.mile_per_second_squared", "AccelerationUnit.mi/s^2");

    /** kt/s */
    public static final AccelerationUnit<LengthUnit, TimeUnit> KNOT_PER_SECOND =
            new AccelerationUnit<LengthUnit, TimeUnit>(SpeedUnit.KNOT, TimeUnit.SECOND,
                    "AccelerationUnit.knot_per_second", "AccelerationUnit.kt/s");

    /** mi/h/s */
    public static final AccelerationUnit<LengthUnit, TimeUnit> MILE_PER_HOUR_PER_SECOND =
            new AccelerationUnit<LengthUnit, TimeUnit>(SpeedUnit.MILE_PER_HOUR, TimeUnit.SECOND,
                    "AccelerationUnit.mile_per_hour_per_second", "AccelerationUnit.mi/h/s");

    /** standard gravity */
    public static final AccelerationUnit<LengthUnit, TimeUnit> STANDARD_GRAVITY =
            new AccelerationUnit<LengthUnit, TimeUnit>("AccelerationUnit.standard_gravity", "AccelerationUnit.g",
                    METER_PER_SECOND_2, 9.80665);

    /**
     * Define acceleration units based on length and time. You can define units like meter/second^2 here.
     * @param lengthUnit the unit of length for the acceleration unit, e.g., meter
     * @param timeUnit the unit of time for the acceleration unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public AccelerationUnit(final L lengthUnit, final T timeUnit, final String nameKey, final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, lengthUnit.getConversionFactorToStandardUnit()
                / (timeUnit.getConversionFactorToStandardUnit() * timeUnit.getConversionFactorToStandardUnit()));
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * Define acceleration units based on speed and time. You can define units like (mile/hour)/second here.
     * @param speedUnit the unit of speed for the acceleration unit, e.g., knot
     * @param timeUnit the unit of time for the acceleration unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public <S extends SpeedUnit<L, T>> AccelerationUnit(final S speedUnit, final T timeUnit, final String nameKey,
            final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, speedUnit.getConversionFactorToStandardUnit()
                / timeUnit.getConversionFactorToStandardUnit());
        this.lengthUnit = speedUnit.getLengthUnit();
        this.timeUnit = timeUnit;
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public AccelerationUnit(final String nameKey, final String abbreviationKey,
            final AccelerationUnit<L, T> referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, referenceUnit, conversionFactorToReferenceUnit);
        this.lengthUnit = referenceUnit.getLengthUnit();
        this.timeUnit = referenceUnit.getTimeUnit();
    }

    /**
     * @return lengthUnit
     */
    public L getLengthUnit()
    {
        return this.lengthUnit;
    }

    /**
     * @return timeUnit
     */
    public T getTimeUnit()
    {
        return this.timeUnit;
    }

}
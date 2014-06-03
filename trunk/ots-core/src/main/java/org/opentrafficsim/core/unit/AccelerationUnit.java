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
 * @param <D> the distance unit type
 * @param <T> the time unit type
 */
public class AccelerationUnit<D extends DistanceUnit, T extends TimeUnit> extends Unit<AccelerationUnit<D, T>>
{
    /** */
    private static final long serialVersionUID = 20130603L;

    /** the actual space unit, e.g. KILOMETER */
    private final D distanceUnit;

    /** the actual time unit, e.g. HOUR */
    private final T timeUnit;

    /** km/h^2 */
    public static final AccelerationUnit<DistanceUnit, TimeUnit> KM_PER_HOUR_2 =
            new AccelerationUnit<DistanceUnit, TimeUnit>(DistanceUnit.KILOMETER, TimeUnit.HOUR,
                    "AccelerationUnit.km_per_hour_squared", "AccelerationUnit.km/h^2");

    /** m/s^2 */
    public static final AccelerationUnit<DistanceUnit, TimeUnit> METER_PER_SECOND_2 =
            new AccelerationUnit<DistanceUnit, TimeUnit>(DistanceUnit.METER, TimeUnit.SECOND,
                    "AccelerationUnit.meter_per_second_squared", "AccelerationUnit.m/s^2");

    /** ft/s^2 */
    public static final AccelerationUnit<DistanceUnit, TimeUnit> FOOT_PER_SECOND_2 =
            new AccelerationUnit<DistanceUnit, TimeUnit>(DistanceUnit.FOOT, TimeUnit.SECOND,
                    "AccelerationUnit.foot_per_second_squared", "AccelerationUnit.ft/s^2");

    /** in/s^2 */
    public static final AccelerationUnit<DistanceUnit, TimeUnit> INCH_PER_SECOND_2 =
            new AccelerationUnit<DistanceUnit, TimeUnit>(DistanceUnit.INCH, TimeUnit.SECOND,
                    "AccelerationUnit.inch_per_second_squared", "AccelerationUnit.in/s^2");

    /** mi/s^2 */
    public static final AccelerationUnit<DistanceUnit, TimeUnit> MILE_PER_SECOND_2 =
            new AccelerationUnit<DistanceUnit, TimeUnit>(DistanceUnit.MILE, TimeUnit.SECOND,
                    "AccelerationUnit.mile_per_second_squared", "AccelerationUnit.mi/s^2");

    /**
     * @param distanceUnit the unit of distance for the acceleration unit, e.g., meters
     * @param timeUnit the unit of time for the acceleration unit, e.g., seconds
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public AccelerationUnit(final D distanceUnit, final T timeUnit, final String nameKey, final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, distanceUnit.getConversionFactorToStandardUnit()
                / (timeUnit.getConversionFactorToStandardUnit() * timeUnit.getConversionFactorToStandardUnit()));
        this.distanceUnit = distanceUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getMultiplicationFactorTo(org.opentrafficsim.core.unit.Unit)
     */
    @Override
    public double getMultiplicationFactorTo(AccelerationUnit<D, T> unit)
    {
        return this.conversionFactorToStandardUnit
                / (unit.getConversionFactorToStandardUnit() * unit.getConversionFactorToStandardUnit());
    }

    /**
     * @return distanceUnit
     */
    public D getDistanceUnit()
    {
        return this.distanceUnit;
    }

    /**
     * @return timeUnit
     */
    public T getTimeUnit()
    {
        return this.timeUnit;
    }

}
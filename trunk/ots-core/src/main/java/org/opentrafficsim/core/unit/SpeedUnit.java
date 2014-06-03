package org.opentrafficsim.core.unit;

/**
 * According to <a href="http://en.wikipedia.org/wiki/Velocity">Wikipedia</a>: Speed describes only how fast an object
 * is moving, whereas velocity gives both how fast and in what direction the object is moving.
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
public class SpeedUnit<D extends DistanceUnit, T extends TimeUnit> extends Unit<SpeedUnit<D, T>>
{
    /** */
    private static final long serialVersionUID = 20140603L;

    /** the unit of distance for the speed unit, e.g., meter */
    private final D distanceUnit;

    /** the unit of time for the speed unit, e.g., second */
    private final T timeUnit;

    /** m/s */
    public static final SpeedUnit<DistanceUnit, TimeUnit> METERS_PER_SECOND = new SpeedUnit<DistanceUnit, TimeUnit>(
            DistanceUnit.METER, TimeUnit.SECOND, "SpeedUnit.meter_per_second", "SpeedUnit.m/s");

    /** km/h */
    public static final SpeedUnit<DistanceUnit, TimeUnit> KM_PER_HOUR = new SpeedUnit<DistanceUnit, TimeUnit>(
            DistanceUnit.KILOMETER, TimeUnit.HOUR, "SpeedUnit.kilometer_per_hour", "SpeedUnit.km/h");

    /** mile/h */
    public static final SpeedUnit<DistanceUnit, TimeUnit> MILES_PER_HOUR = new SpeedUnit<DistanceUnit, TimeUnit>(
            DistanceUnit.MILE, TimeUnit.HOUR, "SpeedUnit.mile_per_hour", "SpeedUnit.mph");

    /** ft/s */
    public static final SpeedUnit<DistanceUnit, TimeUnit> FOOT_PER_SECOND = new SpeedUnit<DistanceUnit, TimeUnit>(
            DistanceUnit.FOOT, TimeUnit.SECOND, "SpeedUnit.foot_per_second", "SpeedUnit.fps");

    /** knot */
    public static final SpeedUnit<DistanceUnit, TimeUnit> KNOT = new SpeedUnit<DistanceUnit, TimeUnit>(
            DistanceUnit.NAUTICAL_MILE, TimeUnit.HOUR, "SpeedUnit.knot", "SpeedUnit.kt");

    /**
     * @param distanceUnit the unit of distance for the speed unit, e.g., meter
     * @param timeUnit the unit of time for the speed unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public SpeedUnit(final D distanceUnit, final T timeUnit, final String nameKey, final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, distanceUnit.getConversionFactorToStandardUnit()
                / timeUnit.getConversionFactorToStandardUnit());
        this.distanceUnit = distanceUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getMultiplicationFactorTo(org.opentrafficsim.core.unit.Unit)
     */
    @Override
    public double getMultiplicationFactorTo(SpeedUnit<D, T> unit)
    {
        return this.conversionFactorToStandardUnit / unit.getConversionFactorToStandardUnit();
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

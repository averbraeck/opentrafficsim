package org.opentrafficsim.core.unit;

/**
 * Standard acceleration units based on speed and time. You can define units like (mile/hour)/second here.
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
 * @param <S> the speed unit type
 * @param <D> the distance unit type
 * @param <T> the time unit type
 */
public class AccelerationSpeedUnit<S extends SpeedUnit<DistanceUnit, TimeUnit>, T extends TimeUnit> extends
        Unit<AccelerationSpeedUnit<S, T>>
{
    /** */
    private static final long serialVersionUID = 20130604L;

    /** the actual speed unit, e.g. KNOT */
    private final S speedUnit;

    /** the actual time unit, e.g. SECOND */
    private final T timeUnit;

    /** kt/s */
    public static final AccelerationSpeedUnit<SpeedUnit<DistanceUnit, TimeUnit>, TimeUnit> KNOT_PER_SECOND =
            new AccelerationSpeedUnit<SpeedUnit<DistanceUnit, TimeUnit>, TimeUnit>(SpeedUnit.KNOT, TimeUnit.SECOND,
                    "AccelerationSpeedUnit.knot_per_second", "AccelerationSpeedUnit.kt/s");

    /**
     * @param speedUnit the unit of speed for the acceleration unit, e.g., knot
     * @param timeUnit the unit of time for the acceleration unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public AccelerationSpeedUnit(final S speedUnit, final T timeUnit, final String nameKey, final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, speedUnit.getConversionFactorToStandardUnit()
                / timeUnit.getConversionFactorToStandardUnit());
        this.speedUnit = speedUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getMultiplicationFactorTo(org.opentrafficsim.core.unit.Unit)
     */
    @Override
    public double getMultiplicationFactorTo(AccelerationSpeedUnit<S, T> unit)
    {
        return this.conversionFactorToStandardUnit / unit.getConversionFactorToStandardUnit();
    }

    /**
     * @return distanceUnit
     */
    public S getSpeedUnit()
    {
        return this.speedUnit;
    }

    /**
     * @return timeUnit
     */
    public T getTimeUnit()
    {
        return this.timeUnit;
    }

}
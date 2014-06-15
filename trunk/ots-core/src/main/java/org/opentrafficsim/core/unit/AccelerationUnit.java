package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.CGS;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

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
 */
public class AccelerationUnit extends Unit<AccelerationUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the actual length unit, e.g. KILOMETER */
    private final LengthUnit lengthUnit;

    /** the actual time unit, e.g. HOUR */
    private final TimeUnit timeUnit;

    /** m/s^2 */
    public static final AccelerationUnit METER_PER_SECOND_2 = new AccelerationUnit(LengthUnit.METER, TimeUnit.SECOND,
            "AccelerationUnit.meter_per_second_squared", "AccelerationUnit.m/s^2", SI_DERIVED);

    /** km/h^2 */
    public static final AccelerationUnit KM_PER_HOUR_2 = new AccelerationUnit(LengthUnit.KILOMETER, TimeUnit.HOUR,
            "AccelerationUnit.km_per_hour_squared", "AccelerationUnit.km/h^2", SI_DERIVED);

    /** ft/s^2 */
    public static final AccelerationUnit FOOT_PER_SECOND_2 = new AccelerationUnit(LengthUnit.FOOT, TimeUnit.SECOND,
            "AccelerationUnit.foot_per_second_squared", "AccelerationUnit.ft/s^2", IMPERIAL);

    /** in/s^2 */
    public static final AccelerationUnit INCH_PER_SECOND_2 = new AccelerationUnit(LengthUnit.INCH, TimeUnit.SECOND,
            "AccelerationUnit.inch_per_second_squared", "AccelerationUnit.in/s^2", IMPERIAL);

    /** mi/h^2 */
    public static final AccelerationUnit MILE_PER_HOUR_2 = new AccelerationUnit(LengthUnit.MILE, TimeUnit.HOUR,
            "AccelerationUnit.mile_per_hour_squared", "AccelerationUnit.mi/h^2", IMPERIAL);

    /** mi/s^2 */
    public static final AccelerationUnit MILE_PER_SECOND_2 = new AccelerationUnit(LengthUnit.MILE, TimeUnit.SECOND,
            "AccelerationUnit.mile_per_second_squared", "AccelerationUnit.mi/s^2", IMPERIAL);

    /** kt/s */
    public static final AccelerationUnit KNOT_PER_SECOND = new AccelerationUnit(SpeedUnit.KNOT, TimeUnit.SECOND,
            "AccelerationUnit.knot_per_second", "AccelerationUnit.kt/s", IMPERIAL);

    /** mi/h/s */
    public static final AccelerationUnit MILE_PER_HOUR_PER_SECOND = new AccelerationUnit(SpeedUnit.MILE_PER_HOUR,
            TimeUnit.SECOND, "AccelerationUnit.mile_per_hour_per_second", "AccelerationUnit.mi/h/s", IMPERIAL);

    /** standard gravity */
    public static final AccelerationUnit STANDARD_GRAVITY = new AccelerationUnit("AccelerationUnit.standard_gravity",
            "AccelerationUnit.g", SI_DERIVED, METER_PER_SECOND_2, 9.80665);

    /** standard gravity */
    public static final AccelerationUnit GAL = new AccelerationUnit(LengthUnit.CENTIMETER, TimeUnit.SECOND,
            "AccelerationUnit.gal", "AccelerationUnit.Gal", CGS);

    /**
     * Define acceleration units based on length and time. You can define units like meter/second^2 here.
     * @param lengthUnit the unit of length for the acceleration unit, e.g., meter
     * @param timeUnit the unit of time for the acceleration unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public AccelerationUnit(final LengthUnit lengthUnit, final TimeUnit timeUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, METER_PER_SECOND_2, lengthUnit.getConversionFactorToStandardUnit()
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
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public AccelerationUnit(final SpeedUnit speedUnit, final TimeUnit timeUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, METER_PER_SECOND_2, speedUnit.getConversionFactorToStandardUnit()
                / timeUnit.getConversionFactorToStandardUnit());
        this.lengthUnit = speedUnit.getLengthUnit();
        this.timeUnit = timeUnit;
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public AccelerationUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final AccelerationUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit);
        this.lengthUnit = referenceUnit.getLengthUnit();
        this.timeUnit = referenceUnit.getTimeUnit();
    }

    /**
     * @return lengthUnit
     */
    public LengthUnit getLengthUnit()
    {
        return this.lengthUnit;
    }

    /**
     * @return timeUnit
     */
    public TimeUnit getTimeUnit()
    {
        return this.timeUnit;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getStandardUnit()
     */
    @Override
    public AccelerationUnit getStandardUnit()
    {
        return METER_PER_SECOND_2;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getSICoefficientsString()
     */
    @Override
    public String getSICoefficientsString()
    {
        return "m/s2";
    }

}
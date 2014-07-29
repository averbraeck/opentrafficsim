package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

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
 */
public class SpeedUnit extends Unit<SpeedUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the unit of length for the speed unit, e.g., meter. */
    private final LengthUnit lengthUnit;

    /** the unit of time for the speed unit, e.g., second. */
    private final TimeUnit timeUnit;

    /** m/s. */
    public static final SpeedUnit METER_PER_SECOND = new SpeedUnit(LengthUnit.METER, TimeUnit.SECOND,
            "SpeedUnit.meter_per_second", "SpeedUnit.m/s", SI_DERIVED);

    /** km/h. */
    public static final SpeedUnit KM_PER_HOUR = new SpeedUnit(LengthUnit.KILOMETER, TimeUnit.HOUR,
            "SpeedUnit.kilometer_per_hour", "SpeedUnit.km/h", SI_DERIVED);

    /** mile/h. */
    public static final SpeedUnit MILE_PER_HOUR = new SpeedUnit(LengthUnit.MILE, TimeUnit.HOUR,
            "SpeedUnit.mile_per_hour", "SpeedUnit.mph", IMPERIAL);

    /** ft/s. */
    public static final SpeedUnit FOOT_PER_SECOND = new SpeedUnit(LengthUnit.FOOT, TimeUnit.SECOND,
            "SpeedUnit.foot_per_second", "SpeedUnit.fps", IMPERIAL);

    /** knot. */
    public static final SpeedUnit KNOT = new SpeedUnit(LengthUnit.NAUTICAL_MILE, TimeUnit.HOUR, "SpeedUnit.knot",
            "SpeedUnit.kt", IMPERIAL);

    /**
     * Build a speed unit from a length unit and a time unit.
     * @param lengthUnit the unit of length for the speed unit, e.g., meter
     * @param timeUnit the unit of time for the speed unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public SpeedUnit(final LengthUnit lengthUnit, final TimeUnit timeUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, METER_PER_SECOND, lengthUnit.getConversionFactorToStandardUnit()
                / timeUnit.getConversionFactorToStandardUnit(), true);
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * Build a speed unit based on another speed unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public SpeedUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final SpeedUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
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
    public SpeedUnit getStandardUnit()
    {
        return METER_PER_SECOND;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getSICoefficientsString()
     */
    @Override
    public String getSICoefficientsString()
    {
        return "m/s";
    }

}

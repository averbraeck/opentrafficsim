package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_ACCEPTED;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.US_CUSTOMARY;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * The volume flow rate is the volume of fluid which passes through a given surface per unit of time (wikipedia).
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
public class FlowVolumeUnit extends Unit<FlowVolumeUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the unit of length for the flow unit, e.g., meter */
    private final LengthUnit lengthUnit;

    /** the unit of time for the flow unit, e.g., second */
    private final TimeUnit timeUnit;

    /** m^3/s */
    public static final FlowVolumeUnit CUBIC_METER_PER_SECOND = new FlowVolumeUnit(LengthUnit.METER, TimeUnit.SECOND,
            "FlowVolumeUnit.cubic_meter_per_second", "FlowVolumeUnit.m^3/s", SI_DERIVED);

    /** m^3/min */
    public static final FlowVolumeUnit CUBIC_METER_PER_MINUTE = new FlowVolumeUnit(LengthUnit.METER, TimeUnit.MINUTE,
            "FlowVolumeUnit.cubic_meter_per_minute", "FlowVolumeUnit.m^3/min", SI_ACCEPTED);

    /** m^3/hour */
    public static final FlowVolumeUnit CUBIC_METER_PER_HOUR = new FlowVolumeUnit(LengthUnit.METER, TimeUnit.HOUR,
            "FlowVolumeUnit.cubic_meter_per_hour", "FlowVolumeUnit.m^3/h", SI_ACCEPTED);

    /** m^3/day */
    public static final FlowVolumeUnit CUBIC_METER_PER_DAY = new FlowVolumeUnit(LengthUnit.METER, TimeUnit.DAY,
            "FlowVolumeUnit.cubic_meter_per_day", "FlowVolumeUnit.m^3/d", SI_ACCEPTED);

    /** L/s */
    public static final FlowVolumeUnit LITER_PER_SECOND = new FlowVolumeUnit(VolumeUnit.LITER, TimeUnit.SECOND,
            "FlowVolumeUnit.liter_per_second", "FlowVolumeUnit.L/s", SI_ACCEPTED);

    /** L/min */
    public static final FlowVolumeUnit LITER_PER_MINUTE = new FlowVolumeUnit(VolumeUnit.LITER, TimeUnit.MINUTE,
            "FlowVolumeUnit.liter_per_minute", "FlowVolumeUnit.L/min", SI_ACCEPTED);

    /** L/hour */
    public static final FlowVolumeUnit LITER_PER_HOUR = new FlowVolumeUnit(VolumeUnit.LITER, TimeUnit.HOUR,
            "FlowVolumeUnit.liter_per_hour", "FlowVolumeUnit.L/h", SI_ACCEPTED);

    /** L/day */
    public static final FlowVolumeUnit LITER_PER_DAY = new FlowVolumeUnit(VolumeUnit.LITER, TimeUnit.DAY,
            "FlowVolumeUnit.liter_per_day", "FlowVolumeUnit.L/d", SI_ACCEPTED);

    /** ft^3/s */
    public static final FlowVolumeUnit CUBIC_FEET_PER_SECOND = new FlowVolumeUnit(LengthUnit.FOOT, TimeUnit.SECOND,
            "FlowVolumeUnit.cubic_feet_per_second", "FlowVolumeUnit.ft^3/s", IMPERIAL);

    /** ft^3/min */
    public static final FlowVolumeUnit CUBIC_FEET_PER_MINUTE = new FlowVolumeUnit(LengthUnit.FOOT, TimeUnit.MINUTE,
            "FlowVolumeUnit.cubic_feet_per_minute", "FlowVolumeUnit.ft^3/min", IMPERIAL);

    /** in^3/s */
    public static final FlowVolumeUnit CUBIC_INCH_PER_SECOND = new FlowVolumeUnit(LengthUnit.INCH, TimeUnit.SECOND,
            "FlowVolumeUnit.cubic_inch_per_second", "FlowVolumeUnit.in^3/s", IMPERIAL);

    /** in^3/min */
    public static final FlowVolumeUnit CUBIC_INCH_PER_MINUTE = new FlowVolumeUnit(LengthUnit.INCH, TimeUnit.MINUTE,
            "FlowVolumeUnit.cubic_inch_per_minute", "FlowVolumeUnit.in^3/min", IMPERIAL);

    /** gallon/s (US) */
    public static final FlowVolumeUnit GALLON_PER_SECOND = new FlowVolumeUnit(VolumeUnit.GALLON_US_FLUID,
            TimeUnit.SECOND, "FlowVolumeUnit.gallon_(US)_per_second", "FlowVolumeUnit.gal/s", US_CUSTOMARY);

    /** gallon/min (US) */
    public static final FlowVolumeUnit GALLON_PER_MINUTE = new FlowVolumeUnit(VolumeUnit.GALLON_US_FLUID,
            TimeUnit.MINUTE, "FlowVolumeUnit.gallon_(US)_per_minute", "FlowVolumeUnit.gal/min", US_CUSTOMARY);

    /** gallon/hour (US) */
    public static final FlowVolumeUnit GALLON_PER_HOUR = new FlowVolumeUnit(VolumeUnit.GALLON_US_FLUID, TimeUnit.HOUR,
            "FlowVolumeUnit.gallon_(US)_per_hour", "FlowVolumeUnit.gal/h", US_CUSTOMARY);

    /** gallon/day (US) */
    public static final FlowVolumeUnit GALLON_PER_DAY = new FlowVolumeUnit(VolumeUnit.GALLON_US_FLUID, TimeUnit.DAY,
            "FlowVolumeUnit.gallon_(US)_per_day", "FlowVolumeUnit.gal/d", US_CUSTOMARY);

    /**
     * Create a flow-volumeunit based on length (cubed) per time unit.
     * @param lengthUnit the unit of length for the flow unit, e.g., meter
     * @param timeUnit the unit of time for the flow unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public FlowVolumeUnit(final LengthUnit lengthUnit, final TimeUnit timeUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, CUBIC_METER_PER_SECOND, Math.pow(
                lengthUnit.getConversionFactorToStandardUnit(), 3.0)
                / timeUnit.getConversionFactorToStandardUnit());
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * Create a flow-volumeunit based as a volume unit per time unit.
     * @param volumeUnit the unit of volume for the flow unit, e.g., cubic meter
     * @param timeUnit the unit of time for the flow unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public FlowVolumeUnit(final VolumeUnit volumeUnit, final TimeUnit timeUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, CUBIC_METER_PER_SECOND, volumeUnit
                .getConversionFactorToStandardUnit() / timeUnit.getConversionFactorToStandardUnit());
        this.lengthUnit = volumeUnit.getLengthUnit();
        this.timeUnit = timeUnit;
    }

    /**
     * Create a flow-volumeunit based on another flow-volumeunit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public FlowVolumeUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final FlowVolumeUnit referenceUnit, final double conversionFactorToReferenceUnit)
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
    public FlowVolumeUnit getStandardUnit()
    {
        return CUBIC_METER_PER_SECOND;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getSICoefficientsString()
     */
    @Override
    public String getSICoefficientsString()
    {
        return "m3/s";
    }

}

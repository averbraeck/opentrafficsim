package org.opentrafficsim.core.unit;

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
 * @param <L> the length unit type
 * @param <T> the time unit type
 */
public class FlowVolumeUnit<L extends LengthUnit, T extends TimeUnit> extends Unit<FlowVolumeUnit<L, T>>
{
    /** */
    private static final long serialVersionUID = 20140604L;

    /** the unit of length for the flow unit, e.g., meter */
    private final L lengthUnit;

    /** the unit of time for the flow unit, e.g., second */
    private final T timeUnit;

    /** m^3/s */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> CUBIC_METER_PER_SECOND =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(LengthUnit.METER, TimeUnit.SECOND,
                    "FlowVolumeUnit.cubic_meter_per_second", "FlowVolumeUnit.m^3/s");

    /** m^3/min */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> CUBIC_METER_PER_MINUTE =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(LengthUnit.METER, TimeUnit.MINUTE,
                    "FlowVolumeUnit.cubic_meter_per_minute", "FlowVolumeUnit.m^3/min");

    /** m^3/hour */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> CUBIC_METER_PER_HOUR =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(LengthUnit.METER, TimeUnit.HOUR,
                    "FlowVolumeUnit.cubic_meter_per_hour", "FlowVolumeUnit.m^3/h");

    /** m^3/day */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> CUBIC_METER_PER_DAY =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(LengthUnit.METER, TimeUnit.DAY,
                    "FlowVolumeUnit.cubic_meter_per_day", "FlowVolumeUnit.m^3/d");

    /** L/s */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> LITER_PER_SECOND =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(VolumeUnit.LITER, TimeUnit.SECOND,
                    "FlowVolumeUnit.liter_per_second", "FlowVolumeUnit.L/s");

    /** L/min */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> LITER_PER_MINUTE =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(VolumeUnit.LITER, TimeUnit.MINUTE,
                    "FlowVolumeUnit.liter_per_minute", "FlowVolumeUnit.L/min");

    /** L/hour */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> LITER_PER_HOUR =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(VolumeUnit.LITER, TimeUnit.HOUR,
                    "FlowVolumeUnit.liter_per_hour", "FlowVolumeUnit.L/h");

    /** L/day */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> LITER_PER_DAY =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(VolumeUnit.LITER, TimeUnit.DAY,
                    "FlowVolumeUnit.liter_per_day", "FlowVolumeUnit.L/d");

    /** ft^3/s */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> CUBIC_FEET_PER_SECOND =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(LengthUnit.FOOT, TimeUnit.SECOND,
                    "FlowVolumeUnit.cubic_feet_per_second", "FlowVolumeUnit.ft^3/s");

    /** ft^3/min */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> CUBIC_FEET_PER_MINUTE =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(LengthUnit.FOOT, TimeUnit.MINUTE,
                    "FlowVolumeUnit.cubic_feet_per_minute", "FlowVolumeUnit.ft^3/min");

    /** in^3/s */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> CUBIC_INCH_PER_SECOND =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(LengthUnit.INCH, TimeUnit.SECOND,
                    "FlowVolumeUnit.cubic_inch_per_second", "FlowVolumeUnit.in^3/s");

    /** in^3/min */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> CUBIC_INCH_PER_MINUTE =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(LengthUnit.INCH, TimeUnit.MINUTE,
                    "FlowVolumeUnit.cubic_inch_per_minute", "FlowVolumeUnit.in^3/min");

    /** gallon/s (US) */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> GALLON_PER_SECOND =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(VolumeUnit.GALLON_US_FLUID, TimeUnit.SECOND,
                    "FlowVolumeUnit.gallon_(US)_per_second", "FlowVolumeUnit.gal/s");

    /** gallon/min (US) */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> GALLON_PER_MINUTE =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(VolumeUnit.GALLON_US_FLUID, TimeUnit.MINUTE,
                    "FlowVolumeUnit.gallon_(US)_per_minute", "FlowVolumeUnit.gal/min");

    /** gallon/hour (US) */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> GALLON_PER_HOUR =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(VolumeUnit.GALLON_US_FLUID, TimeUnit.HOUR,
                    "FlowVolumeUnit.gallon_(US)_per_hour", "FlowVolumeUnit.gal/h");

    /** gallon/day (US) */
    public static final FlowVolumeUnit<LengthUnit, TimeUnit> GALLON_PER_DAY =
            new FlowVolumeUnit<LengthUnit, TimeUnit>(VolumeUnit.GALLON_US_FLUID, TimeUnit.DAY,
                    "FlowVolumeUnit.gallon_(US)_per_day", "FlowVolumeUnit.gal/d");

    /**
     * @param lengthUnit the unit of length for the flow unit, e.g., meter
     * @param timeUnit the unit of time for the flow unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public FlowVolumeUnit(final L lengthUnit, final T timeUnit, final String nameKey, final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, Math.pow(lengthUnit.getConversionFactorToStandardUnit(), 3.0)
                / timeUnit.getConversionFactorToStandardUnit());
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @param volumeUnit the unit of volume for the flow unit, e.g., cubic meter
     * @param timeUnit the unit of time for the flow unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public <V extends VolumeUnit<L>> FlowVolumeUnit(final V volumeUnit, final T timeUnit, final String nameKey,
            final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, volumeUnit.getConversionFactorToStandardUnit()
                / timeUnit.getConversionFactorToStandardUnit());
        this.lengthUnit = volumeUnit.getLengthUnit();
        this.timeUnit = timeUnit;
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param referenceUnit the unit to convert from
     * @param conversionFactorToReferenceUnit multiply by this number to convert from the reference unit
     */
    public FlowVolumeUnit(final String nameKey, final String abbreviationKey, final FlowVolumeUnit<L, T> referenceUnit,
            final double conversionFactorToReferenceUnit)
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

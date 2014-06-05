package org.opentrafficsim.core.unit;

/**
 * The units of power.
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
 * @param <M> the mass unit type
 * @param <L> the length unit type
 * @param <T> the time unit type
 */
public class PowerUnit<M extends MassUnit, L extends LengthUnit, T extends TimeUnit> extends Unit<PowerUnit<M, L, T>>
{
    /** */
    private static final long serialVersionUID = 20140604L;

    /** the unit of mass for the power unit, e.g., kilogram */
    private final M massUnit;

    /** the unit of length for the power unit, e.g., length */
    private final L lengthUnit;

    /** the unit of time for the power unit, e.g., second */
    private final T timeUnit;

    /** watt */
    public static final PowerUnit<MassUnit, LengthUnit, TimeUnit> WATT = new PowerUnit<MassUnit, LengthUnit, TimeUnit>(
            MassUnit.KILOGRAM, LengthUnit.METER, TimeUnit.SECOND, "PowerUnit.watt", "PowerUnit.W");

    /** kilowatt */
    public static final PowerUnit<MassUnit, LengthUnit, TimeUnit> KILOWATT =
            new PowerUnit<MassUnit, LengthUnit, TimeUnit>("PowerUnit.kilowatt", "PowerUnit.kW", WATT, 1000.0);

    /** megawatt */
    public static final PowerUnit<MassUnit, LengthUnit, TimeUnit> MEGAWATT =
            new PowerUnit<MassUnit, LengthUnit, TimeUnit>("PowerUnit.megawatt", "PowerUnit.MW", WATT, 1.0E6);

    /** gigawatt */
    public static final PowerUnit<MassUnit, LengthUnit, TimeUnit> GIGAWATT =
            new PowerUnit<MassUnit, LengthUnit, TimeUnit>("PowerUnit.gigawatt", "PowerUnit.GW", WATT, 1.0E9);

    /** foot-pound-force per hour */
    public static final PowerUnit<MassUnit, LengthUnit, TimeUnit> FOOT_POUND_FORCE_PER_HOUR =
            new PowerUnit<MassUnit, LengthUnit, TimeUnit>(LengthUnit.FOOT, ForceUnit.POUND_FORCE, TimeUnit.HOUR,
                    "PowerUnit.foot_pound-force_per_hour", "PowerUnit.ft.lbf/h");

    /** foot-pound-force per minute */
    public static final PowerUnit<MassUnit, LengthUnit, TimeUnit> FOOT_POUND_FORCE_PER_MINUTE =
            new PowerUnit<MassUnit, LengthUnit, TimeUnit>(LengthUnit.FOOT, ForceUnit.POUND_FORCE, TimeUnit.MINUTE,
                    "PowerUnit.foot_pound-force_per_minute", "PowerUnit.ft.lbf/min");

    /** foot-pound-force per second */
    public static final PowerUnit<MassUnit, LengthUnit, TimeUnit> FOOT_POUND_FORCE_PER_SECOND =
            new PowerUnit<MassUnit, LengthUnit, TimeUnit>(LengthUnit.FOOT, ForceUnit.POUND_FORCE, TimeUnit.SECOND,
                    "PowerUnit.foot_pound-force_per_second", "PowerUnit.ft.lbf/s");

    /** horsepower (metric) */
    public static final PowerUnit<MassUnit, LengthUnit, TimeUnit> HORSEPOWER_METRIC =
            new PowerUnit<MassUnit, LengthUnit, TimeUnit>("PowerUnit.horsepower_(metric)", "PowerUnit.hp", WATT,
                    735.49875);

    /**
     * @param massUnit the unit of mass for the power unit, e.g., kilogram
     * @param lengthUnit the unit of length for the power unit, e.g., meter
     * @param timeUnit the unit of time for the power unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public PowerUnit(final M massUnit, final L lengthUnit, final T timeUnit, final String nameKey,
            final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, massUnit.getConversionFactorToStandardUnit()
                * lengthUnit.getConversionFactorToStandardUnit() * lengthUnit.getConversionFactorToStandardUnit()
                / Math.pow(timeUnit.getConversionFactorToStandardUnit(), 3.0));
        this.massUnit = massUnit;
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @param lengthUnit the unit of length for the power unit, e.g., meter
     * @param forceUnit the unit of force for the power unit, e.g., Newton
     * @param timeUnit the unit of time for the power unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public <F extends ForceUnit<M, L, T>> PowerUnit(final L lengthUnit, final F forceUnit, final T timeUnit,
            final String nameKey, final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, lengthUnit.getConversionFactorToStandardUnit()
                * forceUnit.getConversionFactorToStandardUnit() / timeUnit.getConversionFactorToStandardUnit());
        this.massUnit = forceUnit.getMassUnit();
        this.lengthUnit = forceUnit.getLengthUnit();
        this.timeUnit = forceUnit.getTimeUnit();
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param referenceUnit the unit to convert from
     * @param conversionFactorToReferenceUnit multiply by this number to convert from the reference unit
     */
    public PowerUnit(final String nameKey, final String abbreviationKey, final PowerUnit<M, L, T> referenceUnit,
            final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, referenceUnit, conversionFactorToReferenceUnit);
        this.massUnit = referenceUnit.getMassUnit();
        this.lengthUnit = referenceUnit.getLengthUnit();
        this.timeUnit = referenceUnit.getTimeUnit();
    }

    /**
     * @return massUnit
     */
    public M getMassUnit()
    {
        return this.massUnit;
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

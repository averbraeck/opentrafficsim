package org.opentrafficsim.core.unit;

/**
 * The units of energy.
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
public class EnergyUnit<M extends MassUnit, L extends LengthUnit, T extends TimeUnit> extends Unit<EnergyUnit<M, L, T>>
{
    /** */
    private static final long serialVersionUID = 20140604L;

    /** the unit of mass for the energy unit, e.g., kilogram */
    private final M massUnit;

    /** the unit of length for the energy unit, e.g., length */
    private final L lengthUnit;

    /** the unit of time for the energy unit, e.g., second */
    private final T timeUnit;

    /** Joule */
    public static final EnergyUnit<MassUnit, LengthUnit, TimeUnit> JOULE =
            new EnergyUnit<MassUnit, LengthUnit, TimeUnit>(MassUnit.KILOGRAM, LengthUnit.METER, TimeUnit.SECOND,
                    "EnergyUnit.Joule", "EnergyUnit.J");

    /** foot-pound force */
    public static final EnergyUnit<MassUnit, LengthUnit, TimeUnit> FOOT_POUND_FORCE =
            new EnergyUnit<MassUnit, LengthUnit, TimeUnit>(LengthUnit.FOOT, ForceUnit.POUND_FORCE,
                    "EnergyUnit.foot_pound-force", "EnergyUnit.ft.lbf");

    /** inch-pound force */
    public static final EnergyUnit<MassUnit, LengthUnit, TimeUnit> INCH_POUND_FORCE =
            new EnergyUnit<MassUnit, LengthUnit, TimeUnit>(LengthUnit.INCH, ForceUnit.POUND_FORCE,
                    "EnergyUnit.inch_pound-force", "EnergyUnit.in.lbf");

    /** British thermal unit (ISO) */
    public static final EnergyUnit<MassUnit, LengthUnit, TimeUnit> BTU_ISO =
            new EnergyUnit<MassUnit, LengthUnit, TimeUnit>("EnergyUnit.British_thermal_unit_(ISO)",
                    "EnergyUnit.BTU(ISO)", JOULE, 1.0545E3);

    /** British thermal unit (International Table) */
    public static final EnergyUnit<MassUnit, LengthUnit, TimeUnit> BTU_IT =
            new EnergyUnit<MassUnit, LengthUnit, TimeUnit>("EnergyUnit.British_thermal_unit_(International_Table)",
                    "EnergyUnit.BTU(IT)", JOULE, 1.05505585262E3);

    /** calorie (International Table) */
    public static final EnergyUnit<MassUnit, LengthUnit, TimeUnit> CALORIE_IT =
            new EnergyUnit<MassUnit, LengthUnit, TimeUnit>("EnergyUnit.calorie_(International_Table)",
                    "EnergyUnit.cal(IT)", JOULE, 4.1868);

    /** kilocalorie */
    public static final EnergyUnit<MassUnit, LengthUnit, TimeUnit> KILOCALORIE =
            new EnergyUnit<MassUnit, LengthUnit, TimeUnit>("EnergyUnit.kilocalorie",
                    "EnergyUnit.kcal", CALORIE_IT, 1000.0);

    /** kilowatt-hour */
    public static final EnergyUnit<MassUnit, LengthUnit, TimeUnit> KILOWATT_HOUR =
            new EnergyUnit<MassUnit, LengthUnit, TimeUnit>("EnergyUnit.kilowatt-hour",
                    "EnergyUnit.kW.h", JOULE, 3.6E6);


    /**
     * @param massUnit the unit of mass for the energy unit, e.g., kilogram
     * @param lengthUnit the unit of length for the energy unit, e.g., meter
     * @param timeUnit the unit of time for the energy unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public EnergyUnit(final M massUnit, final L lengthUnit, final T timeUnit, final String nameKey,
            final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, massUnit.getConversionFactorToStandardUnit()
                * lengthUnit.getConversionFactorToStandardUnit() * lengthUnit.getConversionFactorToStandardUnit()
                / (timeUnit.getConversionFactorToStandardUnit() * timeUnit.getConversionFactorToStandardUnit()));
        this.massUnit = massUnit;
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @param lengthUnit the unit of length for the energy unit, e.g., m
     * @param forceUnit the unit of force for the energy unit, e.g., Newton
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public <F extends ForceUnit<M, L, T>> EnergyUnit(final L lengthUnit, final F forceUnit, final String nameKey,
            final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, forceUnit.getConversionFactorToStandardUnit()
                * lengthUnit.getConversionFactorToStandardUnit());
        this.massUnit = forceUnit.getMassUnit();
        this.lengthUnit = forceUnit.getLengthUnit();
        this.timeUnit = forceUnit.getTimeUnit();
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public EnergyUnit(final String nameKey, final String abbreviationKey, final EnergyUnit<M, L, T> referenceUnit,
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

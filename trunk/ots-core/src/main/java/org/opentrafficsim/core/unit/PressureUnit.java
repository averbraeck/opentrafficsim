package org.opentrafficsim.core.unit;

/**
 * The units of pressure.
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
public class PressureUnit<M extends MassUnit, L extends LengthUnit, T extends TimeUnit> extends
        Unit<PressureUnit<M, L, T>>
{
    /** */
    private static final long serialVersionUID = 20140604L;

    /** the unit of mass for the pressure unit, e.g., kilogram */
    private final M massUnit;

    /** the unit of length for the pressure unit, e.g., length */
    private final L lengthUnit;

    /** the unit of time for the pressure unit, e.g., second */
    private final T timeUnit;

    /** Pascal */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> PASCAL =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>(MassUnit.KILOGRAM, LengthUnit.METER, TimeUnit.SECOND,
                    "PressureUnit.pascal", "PressureUnit.Pa");

    /** hectoPascal */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> HECTOPASCAL =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>("PressureUnit.hectopascal", "PressureUnit.hPa", PASCAL,
                    100.0);

    /** kiloPascal */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> KILOPASCAL =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>("PressureUnit.kilopascal", "PressureUnit.kPa", PASCAL,
                    1000.0);

    /** standard atmosphere */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> ATMOSPHERE_STANDARD =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>("PressureUnit.atmosphere_(standard)", "PressureUnit.atm",
                    PASCAL, 101325.0);

    /** technical atmosphere */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> ATMOSPHERE_TECHNICAL =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>(ForceUnit.KILOGRAM_FORCE, AreaUnit.SQUARE_CENTIMETER,
                    "PressureUnit.atmosphere_(technical)", "PressureUnit.at");

    /** bar */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> BAR =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>(ForceUnit.DYNE, AreaUnit.SQUARE_CENTIMETER,
                    "PressureUnit.bar_(full)", "PressureUnit.bar");

    /** millibar */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> MILLIBAR =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>("PressureUnit.millibar", "PressureUnit.mbar",
                    PressureUnit.BAR, 0.001);

    /** cm Hg */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> CENTIMETER_MERCURY =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>("PressureUnit.centimeter_mercury", "PressureUnit.cmHg",
                    PASCAL, 1333.224);

    /** mm Hg */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> MILLIMETER_MERCURY =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>("PressureUnit.millimeter_mercury", "PressureUnit.mmHg",
                    PASCAL, 133.3224);

    /** foot Hg */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> FOOT_MERCURY =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>("PressureUnit.foot_mercury", "PressureUnit.ftHg", PASCAL,
                    40.63666E3);

    /** inch Hg */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> INCH_MERCURY =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>("PressureUnit.inch_mercury", "PressureUnit.inHg", PASCAL,
                    3.386389E3);

    /** kilogram-force per square millimeter */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> KGF_PER_SQUARE_MM =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>(ForceUnit.KILOGRAM_FORCE, AreaUnit.SQUARE_MILLIMETER,
                    "PressureUnit.kilogram-force_per_square_millimeter", "PressureUnit.kgf/mm^2");

    /** pound per square foot */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> POUND_PER_SQUARE_FOOT =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>(ForceUnit.POUND_FORCE, AreaUnit.SQUARE_FOOT,
                    "PressureUnit.pound_per_square_foot", "PressureUnit.lbf/ft^2");

    /** pound per square inch */
    public static final PressureUnit<MassUnit, LengthUnit, TimeUnit> POUND_PER_SQUARE_INCH =
            new PressureUnit<MassUnit, LengthUnit, TimeUnit>(ForceUnit.POUND_FORCE, AreaUnit.SQUARE_INCH,
                    "PressureUnit.pound_per_square_inch", "PressureUnit.lbf/in^2");

    /**
     * @param massUnit the unit of mass for the pressure unit, e.g., kilogram
     * @param lengthUnit the unit of length for the pressure unit, e.g., meter
     * @param timeUnit the unit of time for the pressure unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public PressureUnit(final M massUnit, final L lengthUnit, final T timeUnit, final String nameKey,
            final String abbreviationKey)
    {
        super(nameKey, abbreviationKey,
                massUnit.getConversionFactorToStandardUnit()
                        / (lengthUnit.getConversionFactorToStandardUnit()
                                * timeUnit.getConversionFactorToStandardUnit() * timeUnit
                                    .getConversionFactorToStandardUnit()));
        this.massUnit = massUnit;
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @param forceUnit the unit of force for the pressure unit, e.g., Newton
     * @param areaUnit the unit of area for the pressure unit, e.g., m^2
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public <F extends ForceUnit<M, L, T>, A extends AreaUnit<L>> PressureUnit(final F forceUnit, final A areaUnit,
            final String nameKey, final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, forceUnit.getConversionFactorToStandardUnit()
                / areaUnit.getConversionFactorToStandardUnit());
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
    public PressureUnit(final String nameKey, final String abbreviationKey, final PressureUnit<M, L, T> referenceUnit,
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

package org.opentrafficsim.core.unit;

/**
 * The units of force.
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
public class ForceUnit<M extends MassUnit, L extends LengthUnit, T extends TimeUnit> extends Unit<ForceUnit<M, L, T>>
{
    /** */
    private static final long serialVersionUID = 20140604L;

    /** the unit of mass for the flow unit, e.g., kilogram */
    private final M massUnit;

    /** the unit of length for the flow unit, e.g., length */
    private final L lengthUnit;

    /** the unit of time for the flow unit, e.g., second */
    private final T timeUnit;

    /** Newton */
    public static final ForceUnit<MassUnit, LengthUnit, TimeUnit> NEWTON = new ForceUnit<MassUnit, LengthUnit, TimeUnit>(
            MassUnit.KILOGRAM, LengthUnit.METER, TimeUnit.SECOND, "ForceUnit.newton", "ForceUnit.N");

    /** Dyne */
    public static final ForceUnit<MassUnit, LengthUnit, TimeUnit> DYNE = new ForceUnit<MassUnit, LengthUnit, TimeUnit>(
            MassUnit.GRAM, LengthUnit.CENTIMETER, TimeUnit.SECOND, "ForceUnit.dyne", "ForceUnit.dyn");

    /** kilogram-force */
    public static final ForceUnit<MassUnit, LengthUnit, TimeUnit> KILOGRAM_FORCE = new ForceUnit<MassUnit, LengthUnit, TimeUnit>(
            MassUnit.KILOGRAM, AccelerationUnit.STANDARD_GRAVITY, "ForceUnit.kilogram-force", "ForceUnit.kgf");

    /** ounce-force */
    public static final ForceUnit<MassUnit, LengthUnit, TimeUnit> OUNCE_FORCE = new ForceUnit<MassUnit, LengthUnit, TimeUnit>(
            MassUnit.OUNCE, AccelerationUnit.STANDARD_GRAVITY, "ForceUnit.ounce-force", "ForceUnit.ozf");

    /** pound-force */
    public static final ForceUnit<MassUnit, LengthUnit, TimeUnit> POUND_FORCE = new ForceUnit<MassUnit, LengthUnit, TimeUnit>(
            MassUnit.POUND, AccelerationUnit.STANDARD_GRAVITY, "ForceUnit.pound-force", "ForceUnit.lbf");
    
    /**
     * @param massUnit the unit of mass for the force unit, e.g., kilogram
     * @param lengthUnit the unit of length for the force unit, e.g., meter
     * @param timeUnit the unit of time for the force unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public ForceUnit(final M massUnit, final L lengthUnit, final T timeUnit, final String nameKey,
            final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, massUnit.getConversionFactorToStandardUnit()
                * lengthUnit.getConversionFactorToStandardUnit()
                / (timeUnit.getConversionFactorToStandardUnit() * timeUnit.getConversionFactorToStandardUnit()));
        this.massUnit = massUnit;
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @param massUnit the unit of mass for the force unit, e.g., kilogram
     * @param accelerationUnit the unit of acceleration for the force unit, e.g., m/s^2
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public <A extends AccelerationUnit<L, T>> ForceUnit(final M massUnit, final A accelerationUnit,
            final String nameKey, final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, massUnit.getConversionFactorToStandardUnit() *
                accelerationUnit.getConversionFactorToStandardUnit());
        this.massUnit = massUnit;
        this.lengthUnit = accelerationUnit.getLengthUnit();
        this.timeUnit = accelerationUnit.getTimeUnit();
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param referenceUnit the unit to convert from
     * @param conversionFactorToReferenceUnit multiply by this number to convert from the reference unit
     */
    public ForceUnit(final String nameKey, final String abbreviationKey, final ForceUnit<M, L, T> referenceUnit,
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

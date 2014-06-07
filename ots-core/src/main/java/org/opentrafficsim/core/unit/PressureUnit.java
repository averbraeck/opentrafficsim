package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.CGS;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.MTS;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

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
 */
public class PressureUnit extends Unit<PressureUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the unit of mass for the pressure unit, e.g., kilogram */
    private final MassUnit massUnit;

    /** the unit of length for the pressure unit, e.g., meter */
    private final LengthUnit lengthUnit;

    /** the unit of time for the pressure unit, e.g., second */
    private final TimeUnit timeUnit;

    /** Pascal */
    public static final PressureUnit PASCAL = new PressureUnit(MassUnit.KILOGRAM, LengthUnit.METER, TimeUnit.SECOND,
            "PressureUnit.pascal", "PressureUnit.Pa", SI_DERIVED);

    /** hectoPascal */
    public static final PressureUnit HECTOPASCAL = new PressureUnit("PressureUnit.hectopascal", "PressureUnit.hPa",
            SI_DERIVED, PASCAL, 100.0);

    /** kiloPascal */
    public static final PressureUnit KILOPASCAL = new PressureUnit("PressureUnit.kilopascal", "PressureUnit.kPa",
            SI_DERIVED, PASCAL, 1000.0);

    /** standard atmosphere */
    public static final PressureUnit ATMOSPHERE_STANDARD = new PressureUnit("PressureUnit.atmosphere_(standard)",
            "PressureUnit.atm", OTHER, PASCAL, 101325.0);

    /** torr */
    public static final PressureUnit TORR = new PressureUnit("PressureUnit.torr", "PressureUnit.Torr", OTHER,
            ATMOSPHERE_STANDARD, 1.0 / 760.0);

    /** technical atmosphere */
    public static final PressureUnit ATMOSPHERE_TECHNICAL = new PressureUnit(ForceUnit.KILOGRAM_FORCE,
            AreaUnit.SQUARE_CENTIMETER, "PressureUnit.atmosphere_(technical)", "PressureUnit.at", OTHER);

    /** barye */
    public static final PressureUnit BARYE = new PressureUnit(ForceUnit.DYNE, AreaUnit.SQUARE_CENTIMETER,
            "PressureUnit.barye", "PressureUnit.Ba", CGS);

    /** bar */
    public static final PressureUnit BAR = new PressureUnit("PressureUnit.bar_(full)", "PressureUnit.bar", OTHER,
            PASCAL, 1E5);

    /** millibar */
    public static final PressureUnit MILLIBAR = new PressureUnit("PressureUnit.millibar", "PressureUnit.mbar", OTHER,
            PressureUnit.BAR, 0.001);

    /** cm Hg */
    public static final PressureUnit CENTIMETER_MERCURY = new PressureUnit("PressureUnit.centimeter_mercury",
            "PressureUnit.cmHg", OTHER, PASCAL, 1333.224);

    /** mm Hg */
    public static final PressureUnit MILLIMETER_MERCURY = new PressureUnit("PressureUnit.millimeter_mercury",
            "PressureUnit.mmHg", OTHER, PASCAL, 133.3224);

    /** foot Hg */
    public static final PressureUnit FOOT_MERCURY = new PressureUnit("PressureUnit.foot_mercury", "PressureUnit.ftHg",
            IMPERIAL, PASCAL, 40.63666E3);

    /** inch Hg */
    public static final PressureUnit INCH_MERCURY = new PressureUnit("PressureUnit.inch_mercury", "PressureUnit.inHg",
            IMPERIAL, PASCAL, 3.386389E3);

    /** kilogram-force per square millimeter */
    public static final PressureUnit KGF_PER_SQUARE_MM = new PressureUnit(ForceUnit.KILOGRAM_FORCE,
            AreaUnit.SQUARE_MILLIMETER, "PressureUnit.kilogram-force_per_square_millimeter", "PressureUnit.kgf/mm^2",
            OTHER);

    /** pound per square foot */
    public static final PressureUnit POUND_PER_SQUARE_FOOT = new PressureUnit(ForceUnit.POUND_FORCE,
            AreaUnit.SQUARE_FOOT, "PressureUnit.pound_per_square_foot", "PressureUnit.lbf/ft^2", IMPERIAL);

    /** pound per square inch */
    public static final PressureUnit POUND_PER_SQUARE_INCH = new PressureUnit(ForceUnit.POUND_FORCE,
            AreaUnit.SQUARE_INCH, "PressureUnit.pound_per_square_inch", "PressureUnit.lbf/in^2", IMPERIAL);

    /** pieze */
    public static final PressureUnit PIEZE = new PressureUnit(MassUnit.TONNE, LengthUnit.METER, TimeUnit.SECOND,
            "PressureUnit.pieze", "PressureUnit.pz", MTS);

    /**
     * Construct a pressure unit from mass, length and time units.
     * @param massUnit the unit of mass for the pressure unit, e.g., kilogram
     * @param lengthUnit the unit of length for the pressure unit, e.g., meter
     * @param timeUnit the unit of time for the pressure unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public PressureUnit(final MassUnit massUnit, final LengthUnit lengthUnit, final TimeUnit timeUnit,
            final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, PASCAL,
                massUnit.getConversionFactorToStandardUnit()
                        / (lengthUnit.getConversionFactorToStandardUnit()
                                * timeUnit.getConversionFactorToStandardUnit() * timeUnit
                                    .getConversionFactorToStandardUnit()));
        this.massUnit = massUnit;
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * Construct a pressure unit from force and area units.
     * @param forceUnit the unit of force for the pressure unit, e.g., Newton
     * @param areaUnit the unit of area for the pressure unit, e.g., m^2
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public PressureUnit(final ForceUnit forceUnit, final AreaUnit areaUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, PASCAL, forceUnit.getConversionFactorToStandardUnit()
                / areaUnit.getConversionFactorToStandardUnit());
        this.massUnit = forceUnit.getMassUnit();
        this.lengthUnit = forceUnit.getLengthUnit();
        this.timeUnit = forceUnit.getTimeUnit();
    }

    /**
     * Build a unit with a conversion factor to another unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public PressureUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final PressureUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit);
        this.massUnit = referenceUnit.getMassUnit();
        this.lengthUnit = referenceUnit.getLengthUnit();
        this.timeUnit = referenceUnit.getTimeUnit();
    }

    /**
     * @return massUnit
     */
    public MassUnit getMassUnit()
    {
        return this.massUnit;
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
    public PressureUnit getStandardUnit()
    {
        return PASCAL;
    }

}

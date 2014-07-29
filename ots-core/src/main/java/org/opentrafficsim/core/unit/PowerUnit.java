package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.CGS;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.MTS;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

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
 */
public class PowerUnit extends Unit<PowerUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the unit of mass for the power unit, e.g., kilogram. */
    private final MassUnit massUnit;

    /** the unit of length for the power unit, e.g., length. */
    private final LengthUnit lengthUnit;

    /** the unit of time for the power unit, e.g., second. */
    private final TimeUnit timeUnit;

    /** watt. */
    public static final PowerUnit WATT = new PowerUnit(MassUnit.KILOGRAM, LengthUnit.METER, TimeUnit.SECOND,
            "PowerUnit.watt", "PowerUnit.W", SI_DERIVED);

    /** kilowatt. */
    public static final PowerUnit KILOWATT = new PowerUnit("PowerUnit.kilowatt", "PowerUnit.kW", SI_DERIVED, WATT,
            1000.0);

    /** megawatt. */
    public static final PowerUnit MEGAWATT = new PowerUnit("PowerUnit.megawatt", "PowerUnit.MW", SI_DERIVED, WATT,
            1.0E6);

    /** gigawatt. */
    public static final PowerUnit GIGAWATT = new PowerUnit("PowerUnit.gigawatt", "PowerUnit.GW", SI_DERIVED, WATT,
            1.0E9);

    /** foot-pound-force per hour. */
    public static final PowerUnit FOOT_POUND_FORCE_PER_HOUR = new PowerUnit(LengthUnit.FOOT, ForceUnit.POUND_FORCE,
            TimeUnit.HOUR, "PowerUnit.foot_pound-force_per_hour", "PowerUnit.ft.lbf/h", IMPERIAL);

    /** foot-pound-force per minute. */
    public static final PowerUnit FOOT_POUND_FORCE_PER_MINUTE = new PowerUnit(LengthUnit.FOOT, ForceUnit.POUND_FORCE,
            TimeUnit.MINUTE, "PowerUnit.foot_pound-force_per_minute", "PowerUnit.ft.lbf/min", IMPERIAL);

    /** foot-pound-force per second. */
    public static final PowerUnit FOOT_POUND_FORCE_PER_SECOND = new PowerUnit(LengthUnit.FOOT, ForceUnit.POUND_FORCE,
            TimeUnit.SECOND, "PowerUnit.foot_pound-force_per_second", "PowerUnit.ft.lbf/s", IMPERIAL);

    /** horsepower (metric). */
    public static final PowerUnit HORSEPOWER_METRIC = new PowerUnit("PowerUnit.horsepower_(metric)", "PowerUnit.hp",
            OTHER, WATT, 735.49875);

    /** sthene-meter per second. */
    public static final PowerUnit STHENE_METER_PER_SECOND = new PowerUnit(LengthUnit.METER, ForceUnit.STHENE,
            TimeUnit.SECOND, "PowerUnit.sthene-meter_per_second", "PowerUnit.sn.m/s", MTS);

    /** erg per second. */
    public static final PowerUnit ERG_PER_SECOND = new PowerUnit(LengthUnit.CENTIMETER, ForceUnit.DYNE,
            TimeUnit.SECOND, "PowerUnit.erg_per_second", "PowerUnit.erg/s", CGS);

    /**
     * @param massUnit the unit of mass for the power unit, e.g., kilogram
     * @param lengthUnit the unit of length for the power unit, e.g., meter
     * @param timeUnit the unit of time for the power unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public PowerUnit(final MassUnit massUnit, final LengthUnit lengthUnit, final TimeUnit timeUnit,
            final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, WATT, massUnit.getConversionFactorToStandardUnit()
                * lengthUnit.getConversionFactorToStandardUnit() * lengthUnit.getConversionFactorToStandardUnit()
                / Math.pow(timeUnit.getConversionFactorToStandardUnit(), 3.0), true);
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
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public PowerUnit(final LengthUnit lengthUnit, final ForceUnit forceUnit, final TimeUnit timeUnit,
            final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, WATT, lengthUnit.getConversionFactorToStandardUnit()
                * forceUnit.getConversionFactorToStandardUnit() / timeUnit.getConversionFactorToStandardUnit(), true);
        this.massUnit = forceUnit.getMassUnit();
        this.lengthUnit = forceUnit.getLengthUnit();
        this.timeUnit = forceUnit.getTimeUnit();
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public PowerUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final PowerUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
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
    public PowerUnit getStandardUnit()
    {
        return WATT;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getSICoefficientsString()
     */
    @Override
    public String getSICoefficientsString()
    {
        return "kgm2/s3";
    }

}

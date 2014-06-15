package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * The units of torque (moment of force).
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
public class TorqueUnit extends Unit<TorqueUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the unit of mass for the torque unit, e.g., kilogram */
    private final MassUnit massUnit;

    /** the unit of length for the torque unit, e.g., length */
    private final LengthUnit lengthUnit;

    /** the unit of time for the torque unit, e.g., second */
    private final TimeUnit timeUnit;

    /** Newton meter */
    public static final TorqueUnit NEWTON_METER = new TorqueUnit(MassUnit.KILOGRAM, LengthUnit.METER, TimeUnit.SECOND,
            "TorqueUnit.Newton_meter", "TorqueUnit.N.m", SI_DERIVED);

    /** meter kilogram-force */
    public static final TorqueUnit METER_KILOGRAM_FORCE = new TorqueUnit(ForceUnit.KILOGRAM_FORCE, LengthUnit.METER,
            "TorqueUnit.meter_kilogram-force", "TorqueUnit.m.kgf", OTHER);

    /** foot pound-force */
    public static final TorqueUnit FOOT_POUND_FORCE = new TorqueUnit(ForceUnit.POUND_FORCE, LengthUnit.FOOT,
            "TorqueUnit.foot_pound-force", "TorqueUnit.ft.lbf", IMPERIAL);

    /** inch pound-force */
    public static final TorqueUnit INCH_POUND_FORCE = new TorqueUnit(ForceUnit.POUND_FORCE, LengthUnit.INCH,
            "TorqueUnit.inch_pound-force", "TorqueUnit.in.lbf", IMPERIAL);

    /**
     * Create a torque unit from mass, length and time units.
     * @param massUnit the unit of mass for the torque unit, e.g., kilogram
     * @param lengthUnit the unit of length for the torque unit, e.g., meter
     * @param timeUnit the unit of time for the torque unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public TorqueUnit(final MassUnit massUnit, final LengthUnit lengthUnit, final TimeUnit timeUnit,
            final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, NEWTON_METER, massUnit.getConversionFactorToStandardUnit()
                * lengthUnit.getConversionFactorToStandardUnit() * lengthUnit.getConversionFactorToStandardUnit()
                / (timeUnit.getConversionFactorToStandardUnit() * timeUnit.getConversionFactorToStandardUnit()));
        this.massUnit = massUnit;
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * Create a torque unit from force and length units.
     * @param forceUnit the unit of force for the torque unit, e.g., Newton
     * @param lengthUnit the unit of length for the torque unit, e.g., m
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public TorqueUnit(final ForceUnit forceUnit, final LengthUnit lengthUnit, final String nameKey,
            final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, NEWTON_METER, forceUnit.getConversionFactorToStandardUnit()
                * lengthUnit.getConversionFactorToStandardUnit());
        this.massUnit = forceUnit.getMassUnit();
        this.lengthUnit = forceUnit.getLengthUnit();
        this.timeUnit = forceUnit.getTimeUnit();
    }

    /**
     * Construct a torque unit based on another torque unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public TorqueUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final TorqueUnit referenceUnit, final double conversionFactorToReferenceUnit)
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
    public TorqueUnit getStandardUnit()
    {
        return NEWTON_METER;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getSICoefficientsString()
     */
    @Override
    public String getSICoefficientsString()
    {
        return "kgm2/s2";
    }

}

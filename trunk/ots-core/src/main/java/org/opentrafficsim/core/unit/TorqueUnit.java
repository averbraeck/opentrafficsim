package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * The units of torque (moment of force).
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionMay 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TorqueUnit extends Unit<TorqueUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** The unit of mass for the torque unit, e.g., kilogram. */
    private final MassUnit massUnit;

    /** The unit of length for the torque unit, e.g., length. */
    private final LengthUnit lengthUnit;

    /** The unit of time for the torque unit, e.g., second. */
    private final TimeUnit timeUnit;

    /** The SI unit for torque is Newton meter. */
    public static final TorqueUnit SI;

    /** Newton meter. */
    public static final TorqueUnit NEWTON_METER;

    /** meter kilogram-force. */
    public static final TorqueUnit METER_KILOGRAM_FORCE;

    /** foot pound-force. */
    public static final TorqueUnit FOOT_POUND_FORCE;

    /** inch pound-force. */
    public static final TorqueUnit INCH_POUND_FORCE;

    static
    {
        SI =
                new TorqueUnit(MassUnit.KILOGRAM, LengthUnit.METER, TimeUnit.SECOND, "TorqueUnit.Newton_meter",
                        "TorqueUnit.N.m", SI_DERIVED);
        NEWTON_METER = SI;
        METER_KILOGRAM_FORCE =
                new TorqueUnit(ForceUnit.KILOGRAM_FORCE, LengthUnit.METER, "TorqueUnit.meter_kilogram-force",
                        "TorqueUnit.m.kgf", OTHER);
        FOOT_POUND_FORCE =
                new TorqueUnit(ForceUnit.POUND_FORCE, LengthUnit.FOOT, "TorqueUnit.foot_pound-force",
                        "TorqueUnit.ft.lbf", IMPERIAL);
        INCH_POUND_FORCE =
                new TorqueUnit(ForceUnit.POUND_FORCE, LengthUnit.INCH, "TorqueUnit.inch_pound-force",
                        "TorqueUnit.in.lbf", IMPERIAL);
    }

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
                / (timeUnit.getConversionFactorToStandardUnit() * timeUnit.getConversionFactorToStandardUnit()), true);
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
                * lengthUnit.getConversionFactorToStandardUnit(), true);
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
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
        this.massUnit = referenceUnit.getMassUnit();
        this.lengthUnit = referenceUnit.getLengthUnit();
        this.timeUnit = referenceUnit.getTimeUnit();
    }

    /**
     * @return massUnit
     */
    public final MassUnit getMassUnit()
    {
        return this.massUnit;
    }

    /**
     * @return lengthUnit
     */
    public final LengthUnit getLengthUnit()
    {
        return this.lengthUnit;
    }

    /**
     * @return timeUnit
     */
    public final TimeUnit getTimeUnit()
    {
        return this.timeUnit;
    }

    /** {@inheritDoc} */
    @Override
    public final TorqueUnit getStandardUnit()
    {
        return NEWTON_METER;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "kgm2/s2";
    }

}

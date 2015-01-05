package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.CGS;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.MTS;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * The units of force.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ForceUnit extends Unit<ForceUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the unit of mass for the force unit, e.g., kilogram. */
    private final MassUnit massUnit;

    /** the unit of length for the force unit, e.g., length. */
    private final LengthUnit lengthUnit;

    /** the unit of time for the force unit, e.g., second. */
    private final TimeUnit timeUnit;

    /** Newton. */
    public static final ForceUnit NEWTON;

    /** Dyne. */
    public static final ForceUnit DYNE;

    /** kilogram-force. */
    public static final ForceUnit KILOGRAM_FORCE;

    /** ounce-force. */
    public static final ForceUnit OUNCE_FORCE;

    /** pound-force. */
    public static final ForceUnit POUND_FORCE;

    /** ton-force. */
    public static final ForceUnit TON_FORCE;

    /** sthene. */
    public static final ForceUnit STHENE;

    static
    {
        NEWTON =
            new ForceUnit(MassUnit.KILOGRAM, LengthUnit.METER, TimeUnit.SECOND, "ForceUnit.newton", "ForceUnit.N",
                SI_DERIVED);
        DYNE = new ForceUnit(MassUnit.GRAM, LengthUnit.CENTIMETER, TimeUnit.SECOND, "ForceUnit.dyne", "ForceUnit.dyn", CGS);
        KILOGRAM_FORCE =
            new ForceUnit(MassUnit.KILOGRAM, AccelerationUnit.STANDARD_GRAVITY, "ForceUnit.kilogram-force", "ForceUnit.kgf",
                OTHER);
        OUNCE_FORCE =
            new ForceUnit(MassUnit.OUNCE, AccelerationUnit.STANDARD_GRAVITY, "ForceUnit.ounce-force", "ForceUnit.ozf",
                IMPERIAL);
        POUND_FORCE =
            new ForceUnit(MassUnit.POUND, AccelerationUnit.STANDARD_GRAVITY, "ForceUnit.pound-force", "ForceUnit.lbf",
                IMPERIAL);
        TON_FORCE =
            new ForceUnit(MassUnit.TON_SHORT, AccelerationUnit.STANDARD_GRAVITY, "ForceUnit.ton-force", "ForceUnit.tnf",
                IMPERIAL);
        STHENE =
            new ForceUnit(MassUnit.TON_METRIC, AccelerationUnit.METER_PER_SECOND_2, "ForceUnit.sthene", "ForceUnit.sn", MTS);
    }

    /**
     * Build a standard unit.
     * @param massUnit the unit of mass for the force unit, e.g., kilogram
     * @param lengthUnit the unit of length for the force unit, e.g., meter
     * @param timeUnit the unit of time for the force unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public ForceUnit(final MassUnit massUnit, final LengthUnit lengthUnit, final TimeUnit timeUnit, final String nameKey,
        final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, NEWTON, massUnit.getConversionFactorToStandardUnit()
            * lengthUnit.getConversionFactorToStandardUnit()
            / (timeUnit.getConversionFactorToStandardUnit() * timeUnit.getConversionFactorToStandardUnit()), true);
        this.massUnit = massUnit;
        this.lengthUnit = lengthUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * Build a unit with a conversion factor to another unit.
     * @param massUnit the unit of mass for the force unit, e.g., kilogram
     * @param accelerationUnit the unit of acceleration for the force unit, e.g., m/s^2
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public ForceUnit(final MassUnit massUnit, final AccelerationUnit accelerationUnit, final String nameKey,
        final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, NEWTON, massUnit.getConversionFactorToStandardUnit()
            * accelerationUnit.getConversionFactorToStandardUnit(), true);
        this.massUnit = massUnit;
        this.lengthUnit = accelerationUnit.getLengthUnit();
        this.timeUnit = accelerationUnit.getTimeUnit();
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given reference unit
     */
    public ForceUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
        final ForceUnit referenceUnit, final double conversionFactorToReferenceUnit)
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
    public final ForceUnit getStandardUnit()
    {
        return NEWTON;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "kgm/s2";
    }

}

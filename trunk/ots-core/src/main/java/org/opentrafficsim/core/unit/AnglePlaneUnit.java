package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_ACCEPTED;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Standard plane angle unit. Several conversion factors have been taken from <a
 * href="http://en.wikipedia.org/wiki/Conversion_of_units">http://en.wikipedia.org/wiki/Conversion_of_units</a>.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class AnglePlaneUnit extends Unit<AnglePlaneUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** The SI unit for plane angle is radian. */
    public static final AnglePlaneUnit SI;

    /** radian. */
    public static final AnglePlaneUnit RADIAN;

    /** degree. */
    public static final AnglePlaneUnit DEGREE;

    /** arcminute. */
    public static final AnglePlaneUnit ARCMINUTE;

    /** arcsecond. */
    public static final AnglePlaneUnit ARCSECOND;

    /** grad. */
    public static final AnglePlaneUnit GRAD;

    /** centesimal arcminute. */
    public static final AnglePlaneUnit CENTESIMAL_ARCMINUTE;

    /** centesimal arcsecond. */
    public static final AnglePlaneUnit CENTESIMAL_ARCSECOND;

    static
    {
        SI = RADIAN = new AnglePlaneUnit("AnglePlaneUnit.radian", "AnglePlaneUnit.rad", SI_DERIVED);
        DEGREE = new AnglePlaneUnit("AnglePlaneUnit.degree", "AnglePlaneUnit.deg", SI_ACCEPTED, RADIAN, Math.PI / 180.0);
        ARCMINUTE = new AnglePlaneUnit("AnglePlaneUnit.arcminute", "AnglePlaneUnit.arcmin", SI_ACCEPTED, DEGREE, 1.0 / 60.0);
        ARCSECOND =
            new AnglePlaneUnit("AnglePlaneUnit.arcsecond", "AnglePlaneUnit.arcsec", SI_ACCEPTED, DEGREE, 1.0 / 3600.0);
        GRAD = new AnglePlaneUnit("AnglePlaneUnit.gradian", "AnglePlaneUnit.grad", OTHER, RADIAN, 2.0 * Math.PI / 400.0);
        CENTESIMAL_ARCMINUTE =
            new AnglePlaneUnit("AnglePlaneUnit.centesimal_arcminute", "AnglePlaneUnit.centesimal_arcmin", OTHER, GRAD,
                1.0 / 100.0);
        CENTESIMAL_ARCSECOND =
            new AnglePlaneUnit("AnglePlaneUnit.centesimal_arcsecond", "AnglePlaneUnit.centesimal_arcsec", OTHER, GRAD,
                1.0 / 10000.0);
    }

    /**
     * Build a standard unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public AnglePlaneUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, true);
    }

    /**
     * Build a unit by converting it from another unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given reference unit
     */
    public AnglePlaneUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
        final AnglePlaneUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
    }

    /** {@inheritDoc} */
    @Override
    public final AnglePlaneUnit getStandardUnit()
    {
        return RADIAN;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "";
    }

}

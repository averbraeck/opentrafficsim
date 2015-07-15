package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Standard solid angle unit.
 * <p>
 * Copyright (c) 2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionMay 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class AngleSolidUnit extends Unit<AngleSolidUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** The SI unit for solid angle is steradian. */
    public static final AngleSolidUnit SI;

    /** steradian. */
    public static final AngleSolidUnit STERADIAN;

    /** square degree. */
    public static final AngleSolidUnit SQUARE_DEGREE;

    static
    {
        SI = new AngleSolidUnit("AngleSolidUnit.steradian", "AngleSolidUnit.sr", SI_DERIVED);
        STERADIAN = SI;
        SQUARE_DEGREE =
                new AngleSolidUnit("AngleSolidUnit.square_degree", "AngleSolidUnit.sq_deg", SI_DERIVED, STERADIAN,
                        (Math.PI / 180.0) * (Math.PI / 180.0));
    }

    /**
     * Build a standard unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public AngleSolidUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, true);
    }

    /**
     * Construct a derived unit as a conversion from another unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public AngleSolidUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final AngleSolidUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
    }

    /** {@inheritDoc} */
    @Override
    public final AngleSolidUnit getStandardUnit()
    {
        return STERADIAN;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "";
    }

}

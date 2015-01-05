package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 4, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Make the test specific for this sub class of Unit
 */
public abstract class AbstractUnitTest<U extends Unit<U>>
{
    /**
     * Verify one length conversion factor to standard unit and the localization of the name and abbreviation.
     * @param u Unit to check
     * @param expectedRatio Double; expected ratio
     * @param precision Double; precision of verification
     * @param expectedName String; expected name in the resources
     * @param expectedAbbreviation String; expected abbreviation in the resources
     */
    protected final void checkUnitRatioNameAndAbbreviation(final U u, final double expectedRatio, final double precision,
        final String expectedName, final String expectedAbbreviation)
    {
        assertEquals(String.format("one %s is about %f reference unit", u.getNameKey(), expectedRatio), expectedRatio, u
            .getConversionFactorToStandardUnit(), precision);
        assertEquals(String.format("Name of %s is %s", u.getNameKey(), expectedName), expectedName, u.getName());
        assertEquals(String.format("Abbreviation of %s is %s", u.getNameKey(), expectedAbbreviation), expectedAbbreviation,
            u.getAbbreviation());
    }

    /**
     * Check the nameKey and abbreviationKey of a Unit.
     * @param u Unit to check
     * @param expectedNameKey String; expected name key
     * @param expectedAbbreviationKey String; expected abbreviation key
     */
    protected final void checkKeys(final U u, final String expectedNameKey, final String expectedAbbreviationKey)
    {
        assertEquals("unit key", expectedNameKey, u.getNameKey());
        assertEquals("abbreviation key", expectedAbbreviationKey, u.getAbbreviationKey());
    }

    /**
     * @param fromUnit U; the unit to convert from
     * @param toUnit U; the unit to convert to
     * @return multiplication factor to convert a value from fromUnit to toUnit
     */
    public final double getMultiplicationFactorTo(final U fromUnit, final U toUnit)
    {
        return fromUnit.getConversionFactorToStandardUnit() / toUnit.getConversionFactorToStandardUnit();
    }

}

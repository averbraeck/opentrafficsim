package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionJun 6, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <OU> Make the test specific for this sub class of OffsetUnit
 */
public class AbstractOffsetUnitTest<OU extends OffsetUnit<OU>> extends AbstractUnitTest<OU>
{
    /**
     * Verify one length conversion factor to standard unit and the localization of the name and abbreviation.
     * @param ou Unit to check
     * @param expectedRatio Double; expected ratio
     * @param expectedOffset Double; expected offset
     * @param precision Double; precision of verification
     * @param expectedName String; expected name in the resources
     * @param expectedAbbreviation String; expected abbreviation in the resources
     */
    protected final void checkUnitRatioOffsetNameAndAbbreviation(final OU ou, final double expectedRatio,
            final double expectedOffset, final double precision, final String expectedName,
            final String expectedAbbreviation)
    {
        assertEquals(String.format("zero %s is about %f reference unit", ou.getNameKey(), expectedOffset),
                expectedOffset, ou.getOffsetToStandardUnit(), precision);
        assertEquals(String.format("one %s is about %f reference unit", ou.getNameKey(), expectedRatio), expectedRatio,
                ou.getConversionFactorToStandardUnit(), precision);
        assertEquals(String.format("Name of %s is %s", ou.getNameKey(), expectedName), expectedName, ou.getName());
        assertEquals(String.format("Abbreviation of %s is %s", ou.getNameKey(), expectedAbbreviation),
                expectedAbbreviation, ou.getAbbreviation());
    }

    /**
     * @param fromUnit U; the unit to convert from
     * @param toUnit U; the unit to convert to
     * @return offset to convert a value from fromUnit to toUnit
     */
    public final double getOffsetTo(final OU fromUnit, final OU toUnit)
    {
        double fromOffset = fromUnit.getOffsetToStandardUnit();
        double fromFactor = fromUnit.getConversionFactorToStandardUnit();
        double inStandard = (0d - fromOffset) * fromFactor;
        double toOffset = toUnit.getOffsetToStandardUnit();
        double toFactor = toUnit.getConversionFactorToStandardUnit();
        double inToUnit = inStandard / toFactor + toOffset;
        return inToUnit;
    }

}

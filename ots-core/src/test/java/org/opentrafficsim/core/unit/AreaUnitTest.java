package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.opentrafficsim.core.locale.DefaultLocale;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionJun 4, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AreaUnitTest extends AbstractUnitTest<AreaUnit>
{
    /**
     * Set the locale to "en" so we know what texts should be retrieved from the resources.
     */
    @SuppressWarnings("static-method")
    @Before
    public final void setup()
    {
        DefaultLocale.setLocale(new Locale("en"));
    }

    /**
     * Verify the result of some get*Key methods.
     */
    @Test
    public final void keys()
    {
        checkKeys(AreaUnit.SQUARE_METER, "AreaUnit.square_meter", "AreaUnit.m^2");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(AreaUnit.SQUARE_METER, 1, 0.00000001, "square meter", "m^2");
        checkUnitRatioNameAndAbbreviation(AreaUnit.SQUARE_KM, 1000000, 0.05, "square kilometer", "km^2");
        checkUnitRatioNameAndAbbreviation(AreaUnit.SQUARE_MILE, 2589990, 2, "square mile", "mi^2");
        // Check two conversions between non-standard units
        assertEquals("one SQUARE MILE is 640 ACRE", 640,
                getMultiplicationFactorTo(AreaUnit.SQUARE_MILE, AreaUnit.ACRE), 0.1);
        // Check conversion factor to standard unit for all remaining area units
        checkUnitRatioNameAndAbbreviation(AreaUnit.ARE, 100, 0.001, "are", "a");
        checkUnitRatioNameAndAbbreviation(AreaUnit.HECTARE, 10000, 0.01, "hectare", "ha");
        checkUnitRatioNameAndAbbreviation(AreaUnit.SQUARE_FOOT, 0.092903, 0.000001, "square foot", "ft^2");
        checkUnitRatioNameAndAbbreviation(AreaUnit.SQUARE_INCH, 0.00064516, 0.00000001, "square inch", "in^2");
        checkUnitRatioNameAndAbbreviation(AreaUnit.SQUARE_YARD, 0.836127, 0.000001, "square yard", "yd^2");
        checkUnitRatioNameAndAbbreviation(AreaUnit.ACRE, 4046.9, 0.05, "acre", "ac");
    }

}

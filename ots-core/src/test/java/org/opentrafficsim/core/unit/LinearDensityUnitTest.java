package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;

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
 * $, initial version 1 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LinearDensityUnitTest extends AbstractUnitTest<LinearDensityUnit>
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
        checkKeys(LinearDensityUnit.PER_METER, "LinearDensityUnit.per_meter", "LinearDensityUnit./m");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(LinearDensityUnit.PER_METER, 1, 0.000001, "per meter", "1/m");
        checkUnitRatioNameAndAbbreviation(LinearDensityUnit.PER_KILOMETER, 0.001, 0.0000001, "per kilometer", "1/km");
        checkUnitRatioNameAndAbbreviation(LinearDensityUnit.PER_MILLIMETER, 1000, 0.01, "per millimeter", "1/mm");
        // Check two conversions between non-standard units
        assertEquals("one per millimeter is 1000000 per kilometer", 1000000,
                getMultiplicationFactorTo(LinearDensityUnit.PER_MILLIMETER, LinearDensityUnit.PER_KILOMETER), 0.1);
        assertEquals("one per kilometer is 0.000001 per millimeter", 0.000001,
                getMultiplicationFactorTo(LinearDensityUnit.PER_KILOMETER, LinearDensityUnit.PER_MILLIMETER),
                0.00000005);
    }

    /**
     * Verify that we can create our own Frequency unit.
     */
    @Test
    public final void createLinearDensityUnit()
    {
        LinearDensityUnit muLDU =
                new LinearDensityUnit(UnitLocalizationsTest.DONOTCHECKPREFIX + "LinearDensityUnit.PerInch",
                        UnitLocalizationsTest.DONOTCHECKPREFIX + "LinearDensityUnit.PI", OTHER,
                        LinearDensityUnit.PER_METER, 2.54 / 100);
        assertTrue("Can create a new LinearDensityUnit", null != muLDU);
        checkUnitRatioNameAndAbbreviation(muLDU, 0.0254, 0.000001, "!PerInch!", "!PI!");
    }

}

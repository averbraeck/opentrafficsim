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
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial versionJun 6, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FrequencyUnitTest extends AbstractUnitTest<FrequencyUnit>
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
        checkKeys(FrequencyUnit.HERTZ, "FrequencyUnit.Hertz", "FrequencyUnit.Hz");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(FrequencyUnit.HERTZ, 1, 0.000001, "Hertz", "Hz");
        checkUnitRatioNameAndAbbreviation(FrequencyUnit.KILOHERTZ, 1000, 0.0001, "kilohertz", "kHz");
        // Check two conversions between non-standard units
        assertEquals("one KILOHERTZ is 0.001 MEGAHERTZ", 0.001, getMultiplicationFactorTo(FrequencyUnit.KILOHERTZ,
            FrequencyUnit.MEGAHERTZ), 0.00000000001);
        assertEquals("one MEGAHERTZ is 1000 KILOHERTZ", 1000, getMultiplicationFactorTo(FrequencyUnit.MEGAHERTZ,
            FrequencyUnit.KILOHERTZ), 0.0005);
        checkUnitRatioNameAndAbbreviation(FrequencyUnit.GIGAHERTZ, 1e9, 1e3, "gigahertz", "GHz");
        checkUnitRatioNameAndAbbreviation(FrequencyUnit.TERAHERTZ, 1e12, 1e6, "terahertz", "THz");
    }

    /**
     * Verify that we can create our own Frequency unit.
     */
    @Test
    public final void createFrequencyUnit()
    {
        FrequencyUnit myFU =
            new FrequencyUnit(UnitLocalizationsTest.DONOTCHECKPREFIX + "FrequencyUnit.MiddleA",
                UnitLocalizationsTest.DONOTCHECKPREFIX + "FrequencyUnit.MA", OTHER, FrequencyUnit.KILOHERTZ, 0.440);
        assertTrue("Can create a new ForceUnit", null != myFU);
        checkUnitRatioNameAndAbbreviation(myFU, 440, 0.0001, "!MiddleA!", "!MA!");
    }

}

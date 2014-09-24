package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.opentrafficsim.core.locale.DefaultLocale;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 5, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ElectricalCurrentUnitTest extends AbstractUnitTest<ElectricalCurrentUnit>
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
        checkKeys(ElectricalCurrentUnit.AMPERE, "ElectricalCurrentUnit.ampere", "ElectricalCurrentUnit.A");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(ElectricalCurrentUnit.AMPERE, 1, 0.00000001, "ampere", "A");
        checkUnitRatioNameAndAbbreviation(ElectricalCurrentUnit.MILLIAMPERE, 0.001, 0.000000001, "milliampere", "mA");
        // Check two conversions between two units
        assertEquals("one AMPERE is 1000 MILLI AMPERE", 1000,
                getMultiplicationFactorTo(ElectricalCurrentUnit.AMPERE, ElectricalCurrentUnit.MILLIAMPERE), 0.01);
        assertEquals("one MILLI AMPERE is 0.001 AMPERE", 0.001,
                getMultiplicationFactorTo(ElectricalCurrentUnit.MILLIAMPERE, ElectricalCurrentUnit.AMPERE), 0.0001);
    }

}

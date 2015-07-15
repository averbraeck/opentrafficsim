package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.opentrafficsim.core.locale.DefaultLocale;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionJun 5, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ElectricalChargeUnitTest extends AbstractUnitTest<ElectricalChargeUnit>
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
        checkKeys(ElectricalChargeUnit.COULOMB, "ElectricalChargeUnit.coulomb", "ElectricalChargeUnit.C");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(ElectricalChargeUnit.COULOMB, 1, 0.00000001, "coulomb", "C");
        checkUnitRatioNameAndAbbreviation(ElectricalChargeUnit.MILLIAMPERE_HOUR, 3.6, 0.000000005, "milliampere hour",
                "mAh");
        checkUnitRatioNameAndAbbreviation(ElectricalChargeUnit.FARADAY, 96485.3365, 0.005, "faraday", "F");
        // Check two conversions between non-standard units
        assertEquals("one MILLIAMPERE_HOUR is about 0.00003731137 FARADAY", 0.00003731137,
                getMultiplicationFactorTo(ElectricalChargeUnit.MILLIAMPERE_HOUR, ElectricalChargeUnit.FARADAY),
                0.000000001);
        // Test the other units
        checkUnitRatioNameAndAbbreviation(ElectricalChargeUnit.ATOMIC_UNIT, 1.60217653e-19, 1e-25,
                "elementary unit of charge", "e");
    }

}

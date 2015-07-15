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
 * $, initial versionJun 6, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TorqueUnitTest extends AbstractUnitTest<TorqueUnit>
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
        checkKeys(TorqueUnit.NEWTON_METER, "TorqueUnit.Newton_meter", "TorqueUnit.N.m");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(TorqueUnit.NEWTON_METER, 1, 0.00000001, "Newton meter", "N.m");
        checkUnitRatioNameAndAbbreviation(TorqueUnit.METER_KILOGRAM_FORCE, 9.80665, 0.000005, "meter kilogram-force",
                "m.kgf");
        checkUnitRatioNameAndAbbreviation(TorqueUnit.FOOT_POUND_FORCE, 1.35581794833, 0.0000001, "foot pound-force",
                "ft.lbf");
        // Check two conversions between non-standard units
        assertEquals("one FOOT POUND FORCE is 12 INCH_POUND_FORCE", 12,
                getMultiplicationFactorTo(TorqueUnit.FOOT_POUND_FORCE, TorqueUnit.INCH_POUND_FORCE), 0.0001);
        // Check conversion factor to standard unit for all remaining acceleration units
        checkUnitRatioNameAndAbbreviation(TorqueUnit.INCH_POUND_FORCE, 0.112984829, 0.000000001, "inch pound-force",
                "in.lbf");
    }

}

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
 * $, initial version un 5, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class EnergyUnitTest extends AbstractUnitTest<EnergyUnit>
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
        checkKeys(EnergyUnit.JOULE, "EnergyUnit.Joule", "EnergyUnit.J");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(EnergyUnit.JOULE, 1, 0.00000001, "Joule", "J");
        checkUnitRatioNameAndAbbreviation(EnergyUnit.FOOT_POUND_FORCE, 1.35581794833, 0.0000005, "foot pound-force",
                "ft.lbf");
        checkUnitRatioNameAndAbbreviation(EnergyUnit.BTU_ISO, 1054.5, 0.001, "British thermal unit (ISO)", "BTU(ISO)");
        // Check two conversions between non-standard units
        assertEquals("one FOOT POUND FORCE is about 0.0013 BTU ISO", 0.0013,
                getMultiplicationFactorTo(EnergyUnit.FOOT_POUND_FORCE, EnergyUnit.BTU_ISO), 0.0001);
        // Check conversion factor to standard unit for all remaining acceleration units
        checkUnitRatioNameAndAbbreviation(EnergyUnit.INCH_POUND_FORCE, 0.112984829, 0.000000001, "inch pound-force",
                "in.lbf");
        checkUnitRatioNameAndAbbreviation(EnergyUnit.BTU_IT, 1055.05585262, 0.000001,
                "British thermal unit (International Table)", "BTU(IT)");
        checkUnitRatioNameAndAbbreviation(EnergyUnit.CALORIE_IT, 4.1868, 0.00005, "calorie (International Table)",
                "cal(IT)");
        checkUnitRatioNameAndAbbreviation(EnergyUnit.KILOCALORIE, 4186.8, 0.05, "kilocalorie", "kcal");
        checkUnitRatioNameAndAbbreviation(EnergyUnit.KILOWATT_HOUR, 3600000, 0.1, "kilowatt-hour", "kWh");
    }

}

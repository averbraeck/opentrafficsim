package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.opentrafficsim.core.locale.DefaultLocale;
import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The DSOL project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Jun 6, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class PowerUnitTests extends AbstractUnitTest<PowerUnit>
{
    /**
     * Set the locale to "en" so we know what texts should be retrieved from the resources
     */
    @SuppressWarnings("static-method")
    @Before
    public void setup()
    {
        DefaultLocale.setLocale(new Locale("en"));
    }

    /**
     * Verify the result of some get*Key methods
     */
    @Test
    public void powerKeys()
    {
        checkKeys(PowerUnit.WATT, "PowerUnit.watt", "PowerUnit.W");
    }

    /**
     * Verify conversion factors, English names and abbreviations
     */
    @Test
    public void conversions()
    {
        checkUnitRatioNameAndAbbreviation(PowerUnit.WATT, 1, 0.00000001, "watt", "W");
        checkUnitRatioNameAndAbbreviation(PowerUnit.FOOT_POUND_FORCE_PER_HOUR, 0.00037661608333, 0.0000000001,
                "foot pound-force per hour", "ft.lbf/h");
        checkUnitRatioNameAndAbbreviation(PowerUnit.FOOT_POUND_FORCE_PER_MINUTE, 0.022596965, 0.000001,
                "foot pound-force per minute", "ft.lbf/min");
        // Check two conversions between non-standard units
        assertEquals("one FOOT POUND FORCE PER HOUR is about 0.0166667 FOOT POUND FORCE PER MINUTE", 0.01666667,
                getMultiplicationFactorTo(PowerUnit.FOOT_POUND_FORCE_PER_HOUR, PowerUnit.FOOT_POUND_FORCE_PER_MINUTE),
                0.0000001);
        assertEquals("one FOOT POUND FORCE PER MINUTE is 60 FOOT POUND FORCE PER HOUR", 60,
                getMultiplicationFactorTo(PowerUnit.FOOT_POUND_FORCE_PER_MINUTE, PowerUnit.FOOT_POUND_FORCE_PER_HOUR),
                0.000001);
        // Check conversion factor to standard unit for all remaining time units
        checkUnitRatioNameAndAbbreviation(PowerUnit.KILOWATT, 1000, 0.001, "kilowatt", "kW");
        checkUnitRatioNameAndAbbreviation(PowerUnit.MEGAWATT, 1000000, 1, "megawatt", "MW");
        checkUnitRatioNameAndAbbreviation(PowerUnit.GIGAWATT, 1e9, 1e3, "gigawatt", "GW");
        checkUnitRatioNameAndAbbreviation(PowerUnit.FOOT_POUND_FORCE_PER_SECOND, 1.3558179, 0.000001,
                "foot pound-force per second", "ft.lbf/s");
        checkUnitRatioNameAndAbbreviation(PowerUnit.HORSEPOWER_METRIC, 735.49875, 0.00001, "horsepower (metric)",
                "hp(M)");
    }

    /**
     * Verify that we can create our own power unit
     */
    @Test
    public void createPowerUnitUnit()
    {
        PowerUnit myMU = new PowerUnit("PowerUnit.Person", "PowerUnit.pnp", UnitSystem.OTHER, PowerUnit.WATT, 250);
        assertTrue("Can create a new PowerUnit", null != myMU);
        checkUnitRatioNameAndAbbreviation(myMU, 250, 1, "!Person!", "!pnp!");
    }

}

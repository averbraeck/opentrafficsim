package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.opentrafficsim.core.locale.DefaultLocale;

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
 * @version Jun 5, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class EnergyUnitTests extends AbstractUnitTest<EnergyUnit>
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
    public void energyKeys()
    {
        checkKeys(EnergyUnit.JOULE, "EnergyUnit.Joule", "EnergyUnit.J");
    }

    /**
     * Verify conversion factors, English names and abbreviations
     */
    @Test
    public void conversions()
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

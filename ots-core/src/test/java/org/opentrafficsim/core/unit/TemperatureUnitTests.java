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
public class TemperatureUnitTests extends AbstractOffsetUnitTest<TemperatureUnit>
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
    public void keys()
    {
        checkKeys(TemperatureUnit.KELVIN, "TemperatureUnit.kelvin", "TemperatureUnit.K");
    }

    /**
     * Verify conversion factors, English names and abbreviations
     */
    @Test
    public void conversions()
    {
        checkUnitRatioOffsetNameAndAbbreviation(TemperatureUnit.KELVIN, 1, 0, 0.00000001, "kelvin", "K");
        checkUnitRatioOffsetNameAndAbbreviation(TemperatureUnit.DEGREE_CELCIUS, 1, -273.15, 0.000001, "degree Celcius",
                "\u00B0C");
        checkUnitRatioOffsetNameAndAbbreviation(TemperatureUnit.DEGREE_FAHRENHEIT, 5. / 9., -459.67, 0.00001,
                "degree Fahrenheit", "\u00B0F");
        // Check two conversions between non-standard units
        assertEquals("one DEGREE CELCIUS is 9/5 DEGREE FAHRENHEIT", 9. / 5.,
                getMultiplicationFactorTo(TemperatureUnit.DEGREE_CELCIUS, TemperatureUnit.DEGREE_FAHRENHEIT), 0.0001);
        assertEquals("zero DEGREE CELCIUS is 32 DEGREE FAHRENHEIT", 32,
                getOffsetTo(TemperatureUnit.DEGREE_CELCIUS, TemperatureUnit.DEGREE_FAHRENHEIT), 0.0001);
        assertEquals("zero DEGREE FAHRENHEIT is about -17.7778 DEGREE CELCIUS", -17.7778,
                getOffsetTo(TemperatureUnit.DEGREE_FAHRENHEIT, TemperatureUnit.DEGREE_CELCIUS), 0.0001);
        checkUnitRatioOffsetNameAndAbbreviation(TemperatureUnit.DEGREE_RANKINE, 5. / 9., 0, 0.0001, "degree Rankine",
                "\u00B0R");
        checkUnitRatioOffsetNameAndAbbreviation(TemperatureUnit.DEGREE_REAUMUR, 0.8, -273.15, 0.000001,
                "degree Reaumur", "\u00B0R\u00E9");
    }

    /**
     * Verify that we can create our own temperature unit; i.c. Newton
     */
    @Test
    public void createTemperatureUnit()
    {
        TemperatureUnit myTU =
                new TemperatureUnit(CheckLocalizations.doNotCheckPrefix + "TemperatureUnit.Newton",
                        CheckLocalizations.doNotCheckPrefix + "TemperatureUnit.N", UnitSystem.OTHER, 3.0, -273.15);
        assertTrue("Can create a new TemperatureUnit", null != myTU);
        checkUnitRatioOffsetNameAndAbbreviation(myTU, 3, -273.15, 0.0001, "!Newton!", "!N!");
    }

}

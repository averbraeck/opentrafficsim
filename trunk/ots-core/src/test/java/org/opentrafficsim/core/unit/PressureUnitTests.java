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
public class PressureUnitTests extends AbstractUnitTest<PressureUnit>
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
        checkKeys(PressureUnit.PASCAL, "PressureUnit.pascal", "PressureUnit.Pa");
    }

    /**
     * Verify conversion factors, English names and abbreviations
     */
    @Test
    public void conversions()
    {
        checkUnitRatioNameAndAbbreviation(PressureUnit.PASCAL, 1, 0.00000001, "pascal", "Pa");
        checkUnitRatioNameAndAbbreviation(PressureUnit.ATMOSPHERE_STANDARD, 101325, 0.5, "atmosphere (standard)", "atm");
        checkUnitRatioNameAndAbbreviation(PressureUnit.ATMOSPHERE_TECHNICAL, 98066.5, 0.1, "atmosphere (technical)",
                "at");
        // Check two conversions between non-standard units
        assertEquals("one ATMOSPHERE STANDARD is about 1.03327 ATMOSPHERE TECHNICAL", 1 / 0.9678,
                getMultiplicationFactorTo(PressureUnit.ATMOSPHERE_STANDARD, PressureUnit.ATMOSPHERE_TECHNICAL), 0.0001);
        assertEquals("one ATMOSPHERE TECHNICAL is 0.9678 ATMOSPHERE STANDARD", 0.9678,
                getMultiplicationFactorTo(PressureUnit.ATMOSPHERE_TECHNICAL, PressureUnit.ATMOSPHERE_STANDARD), 0.0001);
        // Check conversion factor to standard unit for all remaining time units
        checkUnitRatioNameAndAbbreviation(PressureUnit.HECTOPASCAL, 100, 0.0001, "hectopascal", "hPa");
        checkUnitRatioNameAndAbbreviation(PressureUnit.KILOPASCAL, 1000, 0.001, "kilopascal", "kPa");
        checkUnitRatioNameAndAbbreviation(PressureUnit.BAR, 100000, 0.01, "bar", "bar");
        checkUnitRatioNameAndAbbreviation(PressureUnit.MILLIBAR, 100, 0.000001, "millibar", "mbar");
        checkUnitRatioNameAndAbbreviation(PressureUnit.CENTIMETER_MERCURY, 1333.22368, 0.001, "centimeter mercury",
                "cmHg");
        checkUnitRatioNameAndAbbreviation(PressureUnit.MILLIMETER_MERCURY, 133.322368, 0.001, "millimeter mercury",
                "mmHg");
        checkUnitRatioNameAndAbbreviation(PressureUnit.FOOT_MERCURY, 40636.66, 0.01, "foot mercury", "ftHg");
        checkUnitRatioNameAndAbbreviation(PressureUnit.INCH_MERCURY, 3386, 0.5, "inch mercury", "inHg");
        checkUnitRatioNameAndAbbreviation(PressureUnit.KGF_PER_SQUARE_MM, 9806650, 0.5,
                "kilogram-force per square millimeter", "kgf/mm^2");
        checkUnitRatioNameAndAbbreviation(PressureUnit.POUND_PER_SQUARE_FOOT, 47.880259, 0.000001,
                "pound per square foot", "lbf/ft^2");
        checkUnitRatioNameAndAbbreviation(PressureUnit.POUND_PER_SQUARE_INCH, 6894.75729, 0.00001,
                "pound per square inch", "lbf/in^2");
    }

    /**
     * Verify that we can create our own pressure unit
     */
    @Test
    public void createPressureUnit()
    {
        PressureUnit myPU =
                new PressureUnit(CheckLocalizations.doNotCheckPrefix + "PressureUnit.HealthyHumanHeart",
                        CheckLocalizations.doNotCheckPrefix + "PressureUnit.hhhp", UnitSystem.OTHER,
                        PressureUnit.MILLIMETER_MERCURY, 106);
        assertTrue("Can create a new PowerUnit", null != myPU);
        checkUnitRatioNameAndAbbreviation(myPU, 14132.1711, 0.01, "!HealthyHumanHeart!", "!hhhp!");
    }

}

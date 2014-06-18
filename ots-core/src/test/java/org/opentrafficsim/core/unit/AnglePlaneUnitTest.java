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
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
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
 * @version Jun 4, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AnglePlaneUnitTest extends AbstractUnitTest<AnglePlaneUnit>
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
        checkKeys(AnglePlaneUnit.RADIAN, "AnglePlaneUnit.radian", "AnglePlaneUnit.rad");
    }

    /**
     * Verify conversion factors, English names and abbreviations
     */
    @Test
    public void conversions()
    {
        checkUnitRatioNameAndAbbreviation(AnglePlaneUnit.DEGREE, 2 * Math.PI / 360, 0.000001, "degree", "\u00b0");
        checkUnitRatioNameAndAbbreviation(AnglePlaneUnit.ARCMINUTE, 2 * Math.PI / 360 / 60, 0.0001, "arcminute", "\'");
        checkUnitRatioNameAndAbbreviation(AnglePlaneUnit.GRAD, 2 * Math.PI / 400, 0.00001, "gradian", "grad");
        // Check two conversions between non-standard units
        assertEquals("one GRAD is about 54 ARCMINUTE", 54,
                getMultiplicationFactorTo(AnglePlaneUnit.GRAD, AnglePlaneUnit.ARCMINUTE), 0.5);
        assertEquals("one ARCMINUTE is about 0.0185 GRAD", 0.0185,
                getMultiplicationFactorTo(AnglePlaneUnit.ARCMINUTE, AnglePlaneUnit.GRAD), 0.0001);
        // Check conversion factor to standard unit for all remaining time units
        checkUnitRatioNameAndAbbreviation(AnglePlaneUnit.CENTESIMAL_ARCMINUTE, 0.00015708, 0.0000001,
                "centesimal arcminute", "\'");
        checkUnitRatioNameAndAbbreviation(AnglePlaneUnit.CENTESIMAL_ARCSECOND, 1.57079e-6, 0.1, "centesimal arcsecond",
                "\"");
    }

    /**
     * Verify that we can create our own angle unit
     */
    @Test
    public void createAngleUnit()
    {
        AnglePlaneUnit myAPU =
                new AnglePlaneUnit(UnitLocalizationsTest.doNotCheckPrefix + "AnglePlaneUnit.point",
                        UnitLocalizationsTest.doNotCheckPrefix + "AnglePlaneUnit.pt", OTHER, AnglePlaneUnit.RADIAN,
                        0.19634954085);
        assertTrue("Can create a new AngleUnit", null != myAPU);
        checkUnitRatioNameAndAbbreviation(myAPU, 0.19634954085, 0.0000001, "!point!", "!pt!");
    }

}

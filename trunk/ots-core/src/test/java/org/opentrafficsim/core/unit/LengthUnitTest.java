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
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties, including,
 * but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no
 * event shall the copyright holder or contributors be liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or
 * profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or
 * tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 * @version Jun 4, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LengthUnitTest extends AbstractUnitTest<LengthUnit>
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
        checkKeys(LengthUnit.METER, "LengthUnit.meter", "LengthUnit.m");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(LengthUnit.METER, 1, 0.00000001, "meter", "m");
        checkUnitRatioNameAndAbbreviation(LengthUnit.MILE, 1609, 0.5, "mile", "mi");
        checkUnitRatioNameAndAbbreviation(LengthUnit.CENTIMETER, 0.01, 0.000000001, "centimeter", "cm");
        // Check two conversions between non-standard units
        assertEquals("one MILE is about 160900 CENTIMETER", 160900,
                getMultiplicationFactorTo(LengthUnit.MILE, LengthUnit.CENTIMETER), 50);
        assertEquals("one CENTIMETER is about 0.000006215 MILE", 0.000006215,
                getMultiplicationFactorTo(LengthUnit.CENTIMETER, LengthUnit.MILE), 0.000000002);
        // Check conversion factor to standard unit for all remaining distance units
        checkUnitRatioNameAndAbbreviation(LengthUnit.MILLIMETER, 0.001, 0.000000001, "millimeter", "mm");
        checkUnitRatioNameAndAbbreviation(LengthUnit.DECIMETER, 0.1, 0.000000001, "decimeter", "dm");
        checkUnitRatioNameAndAbbreviation(LengthUnit.DEKAMETER, 10, 0.0000001, "dekameter", "dam");
        checkUnitRatioNameAndAbbreviation(LengthUnit.HECTOMETER, 100, 0.000001, "hectometer", "hm");
        checkUnitRatioNameAndAbbreviation(LengthUnit.KILOMETER, 1000, 0.00001, "kilometer", "km");
        checkUnitRatioNameAndAbbreviation(LengthUnit.FOOT, 0.3048, 0.000001, "foot", "ft");
        checkUnitRatioNameAndAbbreviation(LengthUnit.INCH, 0.0254, 0.0000001, "inch", "in");
        checkUnitRatioNameAndAbbreviation(LengthUnit.NAUTICAL_MILE, 1852, 0.5, "nautical mile", "NM");
        checkUnitRatioNameAndAbbreviation(LengthUnit.YARD, 0.9144, 0.00005, "yard", "yd");
    }

    /**
     * Verify that we can create our own length unit.
     */
    @Test
    public final void createLengthUnit()
    {
        LengthUnit myLU =
                new LengthUnit(UnitLocalizationsTest.DONOTCHECKPREFIX + "LengthUnit.Furlong",
                        UnitLocalizationsTest.DONOTCHECKPREFIX + "LengthUnit.fl", OTHER, LengthUnit.METER, 201.16800);
        assertTrue("Can create a new LengthUnit", null != myLU);
        checkUnitRatioNameAndAbbreviation(myLU, 200, 2, "!Furlong!", "!fl!");
    }
}

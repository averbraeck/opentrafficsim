package org.opentrafficsim.core.unit;

import static org.junit.Assert.*;

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
 * @version Jun 4, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LengthUnitTests
{
    /**
     * Set the locale to "en" so we know what texts should be retrieved from the resources
     */
    @SuppressWarnings("static-method")
    @Before
    public void setup() {
        DefaultLocale.setLocale(new Locale("en"));
    }
    
    /**
     * Verify the result of the get*Key methods
     */
    @SuppressWarnings("static-method")
    @Test
    public void lengthKeys() {
        assertEquals("unit key", "LengthUnit.meter", LengthUnit.METER.getNameKey());
        assertEquals("abbreviation key", "LengthUnit.m", LengthUnit.METER.getAbbreviationKey());
    }
    
    /**
     * Verify localization of the get* methods
     */
    @SuppressWarnings("static-method")
    @Test
    public void localizationToEnglish() {
        // Cannot localize due to wrong directory
        assertEquals("unit key", "meter", LengthUnit.METER.getName());
        System.out.println("unit for METER is \"" + LengthUnit.METER.getName() + "\"");
    }
    
    /**
     * Verify conversion factors
     */
    @SuppressWarnings("static-method")
    @Test
    public void conversions () {
        checkLengthUnitRatioNameAndAbbreviation(LengthUnit.MILE, 1609, 0.5, "mile", "mi");
        //assertEquals("Mile is about 1609 m", 1609, LengthUnit.MILE.getConversionFactorToStandardUnit(), 0.5);
        checkLengthUnitRatioNameAndAbbreviation(LengthUnit.CENTIMETER, 0.01, 0.000000001, "centimeter", "cm");
        //assertEquals("CENTIMETER is 0.01 m", 0.01, LengthUnit.CENTIMETER.getConversionFactorToStandardUnit(), 0.0000001);
        // Check two conversions between non-standard units
        assertEquals("one MILE is about 160900 CENTIMETER", 160900, LengthUnit.MILE.getMultiplicationFactorTo(LengthUnit.CENTIMETER), 50);
        assertEquals("one CENTIMETER is about 0.000006215 MILE", 0.000006215, LengthUnit.CENTIMETER.getMultiplicationFactorTo(LengthUnit.MILE), 0.000000002);
        // Check conversion factor to standard unit for all remaining distance units
        checkLengthUnitRatioNameAndAbbreviation(LengthUnit.DECIMETER, 0.1, 0.000000001, "decimeter", "dm");
        checkLengthUnitRatioNameAndAbbreviation(LengthUnit.DEKAMETER, 10, 0.0000001, "dekameter", "dam");
        checkLengthUnitRatioNameAndAbbreviation(LengthUnit.HECTOMETER, 100, 0.000001, "hectometer", "hm");
        checkLengthUnitRatioNameAndAbbreviation(LengthUnit.KILOMETER, 1000, 0.00001, "kilometer", "km");
        checkLengthUnitRatioNameAndAbbreviation(LengthUnit.FOOT, 0.3048, 0.000001, "foot", "ft");
        checkLengthUnitRatioNameAndAbbreviation(LengthUnit.INCH, 0.0254, 0.0000001, "inch", "in");
        checkLengthUnitRatioNameAndAbbreviation(LengthUnit.NAUTICAL_MILE, 1852, 0.5, "nautical mile", "NM");
        checkLengthUnitRatioNameAndAbbreviation(LengthUnit.YARD, 0.9144, 0.00005, "yard", "yd");
    }
    
    /**
     * Verify one length conversion factor to standard unit
     * @param lu LengthUnit to check
     * @param expectedRatio Double; expected ratio
     * @param precision Double; precision of verification
     * @param expectedName String; expected name in the resources
     * @param expectedAbbreviation String; expected abbreviation in the resources
     */
    private static void checkLengthUnitRatioNameAndAbbreviation(LengthUnit lu, double expectedRatio, double precision, String expectedName, String expectedAbbreviation) {
        assertEquals(String.format("one %s is about %f m", lu.getNameKey(), expectedRatio), expectedRatio, lu.getConversionFactorToStandardUnit(), precision);
        assertEquals(String.format("Name of %s is %s", lu.getNameKey(), expectedName), expectedName, lu.getName());
        assertEquals(String.format("Abbreviation of %s is %s", lu.getNameKey(), expectedAbbreviation), expectedAbbreviation, lu.getAbbreviation());
    }
    
    /**
     * Verify that we can create our own length unit
     */
    @SuppressWarnings("static-method")
    @Test
    public void createLengthUnit() {
        LengthUnit myLU = new LengthUnit("LengthUnit.Furlong", "LengthUnit.fl", 201.16800);
        assertTrue("Can create a new LengthUnit", null != myLU);
        checkLengthUnitRatioNameAndAbbreviation(myLU, 200, 2, "!Furlong!", "!fl!");
    }
}

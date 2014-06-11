package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

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
 * @version Jun 6, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SpeedUnitTests extends AbstractUnitTest<SpeedUnit>
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
        checkKeys(SpeedUnit.METER_PER_SECOND, "SpeedUnit.meter_per_second", "SpeedUnit.m/s");
    }

    /**
     * Verify conversion factors, English names and abbreviations
     */
    @Test
    public void conversions()
    {
        checkUnitRatioNameAndAbbreviation(SpeedUnit.METER_PER_SECOND, 1, 0.00000001, "meter per second", "m/s");
        checkUnitRatioNameAndAbbreviation(SpeedUnit.KM_PER_HOUR, 0.277778, 0.000001, "kilometer per hour", "km/h");
        checkUnitRatioNameAndAbbreviation(SpeedUnit.MILE_PER_HOUR, 0.44704, 0.00001, "mile per hour", "mi/h");
        // Check two conversions between non-standard units
        assertEquals("one KM PER HOUR is about 0.621371 MILE PER HOUR", 0.621371,
                getMultiplicationFactorTo(SpeedUnit.KM_PER_HOUR, SpeedUnit.MILE_PER_HOUR), 0.0001);
        assertEquals("one MILE PER HOUR is about 1.60934 KM PER HOUR", 1.60934,
                getMultiplicationFactorTo(SpeedUnit.MILE_PER_HOUR, SpeedUnit.KM_PER_HOUR), 0.0001);
        // Check conversion factor to standard unit for all remaining time units
        checkUnitRatioNameAndAbbreviation(SpeedUnit.FOOT_PER_SECOND, 0.3048, 0.0001, "foot per second", "ft/s");
        checkUnitRatioNameAndAbbreviation(SpeedUnit.KNOT, 0.514444, 0.000001, "knot", "kt");
    }

    /**
     * Verify that we can create our own speed unit
     */
    @Test
    public void createSpeedUnit()
    {
        SpeedUnit mySU =
                new SpeedUnit(CheckLocalizations.doNotCheckPrefix + "SpeedUnit.Sprinter",
                        CheckLocalizations.doNotCheckPrefix + "SpeedUnit.sprtr", SI_DERIVED, SpeedUnit.KM_PER_HOUR, 48);
        assertTrue("Can create a new PowerUnit", null != mySU);
        checkUnitRatioNameAndAbbreviation(mySU, 13.3333, 0.0001, "!Sprinter!", "!sprtr!");
    }

}

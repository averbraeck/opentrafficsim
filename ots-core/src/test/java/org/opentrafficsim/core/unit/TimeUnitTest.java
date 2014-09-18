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
public class TimeUnitTest extends AbstractUnitTest<TimeUnit>
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
        checkKeys(TimeUnit.SECOND, "TimeUnit.second", "TimeUnit.s");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(TimeUnit.SECOND, 1, 0.00000001, "second", "s");
        checkUnitRatioNameAndAbbreviation(TimeUnit.HOUR, 3600, 0.0005, "hour", "h");
        checkUnitRatioNameAndAbbreviation(TimeUnit.DAY, 86400, 0.001, "day", "d");
        // Check two conversions between non-standard units
        assertEquals("one DAY is 24 HOUR", 24, getMultiplicationFactorTo(TimeUnit.DAY, TimeUnit.HOUR), 0.0001);
        assertEquals("one HOUR is about 0.0417 DAY", 0.0417, getMultiplicationFactorTo(TimeUnit.HOUR, TimeUnit.DAY),
                0.0001);
        // Check conversion factor to standard unit for all remaining time units
        checkUnitRatioNameAndAbbreviation(TimeUnit.MILLISECOND, 0.001, 0.00000001, "millisecond", "ms");
        checkUnitRatioNameAndAbbreviation(TimeUnit.MINUTE, 60, 0.000001, "minute", "m");
        checkUnitRatioNameAndAbbreviation(TimeUnit.WEEK, 7 * 86400, 0.1, "week", "w");
    }

    /**
     * Verify that we can create our own length unit.
     */
    @Test
    public final void createLengthUnit()
    {
        TimeUnit myTU =
                new TimeUnit(UnitLocalizationsTest.DONOTCHECKPREFIX + "TimeUnit.Fortnight",
                        UnitLocalizationsTest.DONOTCHECKPREFIX + "TimeUnit.fn", OTHER, TimeUnit.SECOND, 14 * 86400);
        assertTrue("Can create a new TimeUnit", null != myTU);
        checkUnitRatioNameAndAbbreviation(myTU, 14 * 86400, 1, "!Fortnight!", "!fn!");
    }

}

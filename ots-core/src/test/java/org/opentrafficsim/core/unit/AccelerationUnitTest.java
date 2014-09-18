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
public class AccelerationUnitTest extends AbstractUnitTest<AccelerationUnit>
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
        checkKeys(AccelerationUnit.INCH_PER_SECOND_2, "AccelerationUnit.inch_per_second_squared",
                "AccelerationUnit.in/s^2");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(AccelerationUnit.METER_PER_SECOND_2, 1, 0.00000001,
                "meter per second squared", "m/s^2");
        checkUnitRatioNameAndAbbreviation(AccelerationUnit.KM_PER_HOUR_2, 1 / 3.6 / 3600, 0.0005,
                "km per hour squared", "km/h^2");
        checkUnitRatioNameAndAbbreviation(AccelerationUnit.FOOT_PER_SECOND_2, 0.3048, 0.00001,
                "foot per second squared", "ft/s^2");
        // Check two conversions between non-standard units
        assertEquals("one FOOT PER SECOND PER SECOND is ??? KM PER HOUR PER HOUR", 3950.208,
                getMultiplicationFactorTo(AccelerationUnit.FOOT_PER_SECOND_2, AccelerationUnit.KM_PER_HOUR_2), 0.01);
        // Check conversion factor to standard unit for all remaining acceleration units
        checkUnitRatioNameAndAbbreviation(AccelerationUnit.INCH_PER_SECOND_2, 0.0254, 0.0000000001,
                "inch per second squared", "in/s^2");
        checkUnitRatioNameAndAbbreviation(AccelerationUnit.MILE_PER_HOUR_2, 0.000124177778, 0.0000000001,
                "mile per hour squared", "mi/h^2");
        checkUnitRatioNameAndAbbreviation(AccelerationUnit.MILE_PER_SECOND_2, 1609.344, 0.000001,
                "mile per second squared", "mi/s^2");
        checkUnitRatioNameAndAbbreviation(AccelerationUnit.KNOT_PER_SECOND, 0.514444444, 0.000001, "knot per second",
                "kt/s");
        checkUnitRatioNameAndAbbreviation(AccelerationUnit.MILE_PER_HOUR_PER_SECOND, 0.44704, 0.00000001,
                "mile per hour per second", "mi/h/s");
    }

    /**
     * Check that g is about 9.8 m/s/s.
     */
    @Test
    public final void gravityConstant()
    {
        checkUnitRatioNameAndAbbreviation(AccelerationUnit.STANDARD_GRAVITY, 9.8, 0.02, "standard gravity", "g");
    }
}

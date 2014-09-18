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
 * @version Jun 6, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FlowVolumeUnitTest extends AbstractUnitTest<FlowVolumeUnit>
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
     * Verify the result of some get*Key methods.
     */
    @Test
    public final void keys()
    {
        checkKeys(FlowVolumeUnit.CUBIC_METER_PER_SECOND, "FlowVolumeUnit.cubic_meter_per_second",
                "FlowVolumeUnit.m^3/s");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(FlowVolumeUnit.CUBIC_METER_PER_SECOND, 1, 0.000001, "cubic meter per second",
                "m^3/s");
        checkUnitRatioNameAndAbbreviation(FlowVolumeUnit.CUBIC_METER_PER_MINUTE, 0.0166667, 0.000001,
                "cubic meter per minute", "m^3/min");
        // Check two conversions between non-standard units
        assertEquals("one CUBIC METER PER HOUR is about 2.205 CUBIC_METER_PER_MINUTED", 0.01666667,
                getMultiplicationFactorTo(FlowVolumeUnit.CUBIC_METER_PER_HOUR, FlowVolumeUnit.CUBIC_METER_PER_MINUTE),
                0.00001);
        assertEquals("one CUBIC METER PER MINUTE is 60 CUBIC_METER_PER_HOUR", 60,
                getMultiplicationFactorTo(FlowVolumeUnit.CUBIC_METER_PER_MINUTE, FlowVolumeUnit.CUBIC_METER_PER_HOUR),
                0.0001);
    }

    /**
     * Verify that we can create our own FlowVolume unit.
     */
    @Test
    public final void createFLowVolumeUnit()
    {
        FlowVolumeUnit myFVU =
                new FlowVolumeUnit(UnitLocalizationsTest.DONOTCHECKPREFIX + "FlowVolumeUnit.TrucksPerHour",
                        UnitLocalizationsTest.DONOTCHECKPREFIX + "FlowVolumeUnit.tph", UnitSystem.OTHER,
                        FlowVolumeUnit.CUBIC_METER_PER_HOUR, 100);
        assertTrue("Can create a new FlowMassUnit", null != myFVU);
        checkUnitRatioNameAndAbbreviation(myFVU, 100. / 3600, 0.0001, "!TrucksPerHour!", "!tph!");
    }

}

package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
 * @param <M> Mass unit underlying this Flow Mass unit
 * @param <T> Time unit underlying this Flow Mass unit
 */
public class FlowMassUnitTests<M extends MassUnit, T extends TimeUnit> extends AbstractUnitTest<FlowMassUnit<?, ?>>
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
    public void flowMassKeys()
    {
        checkKeys(FlowMassUnit.KILOGRAM_PER_SECOND, "FlowMassUnit.kilogram_per_second", "FlowMassUnit.kg/s");
    }

    /**
     * Verify conversion factors, English names and abbreviations
     */
    @Test
    public void conversions()
    {
        checkUnitRatioNameAndAbbreviation(FlowMassUnit.KILOGRAM_PER_SECOND, 1, 0.000001, "kilogram per second", "kg/s");
        checkUnitRatioNameAndAbbreviation(FlowMassUnit.POUND_PER_SECOND, 0.453592, 0.0001, "pound per second", "lb/s");
        // Check two conversions between non-standard units
        assertEquals("one KILOGRAM PER SECOND is about 2.205 POUND PER SECOND", 2.205,
                getMultiplicationFactorTo(FlowMassUnit.KILOGRAM_PER_SECOND, FlowMassUnit.POUND_PER_SECOND), 0.0005);
        assertEquals("one POUND PER SECOND is about CCCXXXX KILOGRAM PER SECOND", 0.453592,
                getMultiplicationFactorTo(FlowMassUnit.POUND_PER_SECOND, FlowMassUnit.KILOGRAM_PER_SECOND), 0.0001);
    }

    /**
     * Verify that we can create our own angle unit
     */
    @Test
    public void createAngleUnit()
    {
        FlowMassUnit<MassUnit, TimeUnit> myFMU =
                new FlowMassUnit<MassUnit, TimeUnit>("FlowMassUnit.WaterDropsPerHour", "FlowMassUnit.wdpu",
                        FlowMassUnit.KILOGRAM_PER_SECOND, 1234);
        assertTrue("Can create a new FlowMassUnit", null != myFMU);
        checkUnitRatioNameAndAbbreviation(myFMU, 1234, 0.0001, "!WaterDropsPerHour!", "!wdpu!");
    }

}

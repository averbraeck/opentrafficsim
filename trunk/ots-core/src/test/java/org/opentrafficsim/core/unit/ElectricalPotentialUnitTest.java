package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

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
 * @version Jun 5, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ElectricalPotentialUnitTest extends AbstractUnitTest<ElectricalPotentialUnit>
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
        checkKeys(ElectricalPotentialUnit.VOLT, "ElectricalPotentialUnit.volt", "ElectricalPotentialUnit.V");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(ElectricalPotentialUnit.VOLT, 1, 0.00000001, "volt", "V");
        checkUnitRatioNameAndAbbreviation(ElectricalPotentialUnit.MILLIVOLT, 0.001, 0.00000000001, "millivolt", "mV");
        checkUnitRatioNameAndAbbreviation(ElectricalPotentialUnit.KILOVOLT, 1000, 0.005, "kilovolt", "kV");
        // Check two conversions between non-standard units
        assertEquals("one KILOVOLT is 1000000 MILLIVOLT", 1000000,
                getMultiplicationFactorTo(ElectricalPotentialUnit.KILOVOLT, ElectricalPotentialUnit.MILLIVOLT), 0.0001);
    }

    /**
     * Verify that we can create our own electrical potential unit.
     */
    @Test
    public final void createElectricalPotentialUnit()
    {
        ElectricalPotentialUnit myEPU =
                new ElectricalPotentialUnit(
                        UnitLocalizationsTest.DONOTCHECKPREFIX + "ElectricalPotentialUnit.NanoVolt",
                        UnitLocalizationsTest.DONOTCHECKPREFIX + "ElectricalPotentialUnit.NanoV", SI_DERIVED,
                        ElectricalPotentialUnit.VOLT, 1e-9);
        assertTrue("Can create a new ElectricalPotentialUnit", null != myEPU);
        checkUnitRatioNameAndAbbreviation(myEPU, 1e-9, 0.1, "!NanoVolt!", "!NanoV!");

        myEPU =
                new ElectricalPotentialUnit(PowerUnit.FOOT_POUND_FORCE_PER_HOUR, ElectricalCurrentUnit.MICROAMPERE,
                        UnitLocalizationsTest.DONOTCHECKPREFIX + "ElectricalPotentialUnit.fpfph/microA",
                        UnitLocalizationsTest.DONOTCHECKPREFIX + "ElectricalPotentialUnit.fpfph/uA",
                        UnitSystem.IMPERIAL);
        assertTrue("Can create a new ElectricalPotentialUnit", null != myEPU);
        checkUnitRatioNameAndAbbreviation(myEPU, 376.6, 0.1, "!fpfph/microA!", "!fpfph/uA!");
    }

}

package org.opentrafficsim.core.unit;

import static org.junit.Assert.*;

import org.junit.Test;
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
 * @version Jun 18, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class UnitTest
{
    /**
     * Test the lookupUnitWithSICoefficients method
     */
    @SuppressWarnings("static-method")
    @Test
    public void lookupUnitWithSICoefficients()
    {
        assertEquals("ABVOLT is expressed in Volt", "[V]",
                Unit.lookupUnitWithSICoefficients(ElectricalPotentialUnit.ABVOLT.getSICoefficients().toString())
                        .toString());
        assertEquals(
                "ABVOLT / STATAMPERE is expressed in Ohm",
                "[\u03A9]",
                Unit.lookupUnitWithSICoefficients(
                        SICoefficients.divide(ElectricalPotentialUnit.ABVOLT.getSICoefficients(),
                                ElectricalCurrentUnit.STATAMPERE.getSICoefficients()).toString()).toString());
        assertEquals(
                "ABVOLT * STATAMPERE is expressed in Watt",
                "[W]",
                Unit.lookupUnitWithSICoefficients(
                        SICoefficients.multiply(ElectricalPotentialUnit.ABVOLT.getSICoefficients(),
                                ElectricalCurrentUnit.STATAMPERE.getSICoefficients()).toString()).toString());
        assertEquals(
                "ABVOLT / Watt is expressed in Ohm",
                "[!1/A!]",
                Unit.lookupOrCreateUnitWithSICoefficients(
                        SICoefficients.divide(ElectricalPotentialUnit.ABVOLT.getSICoefficients(),
                                PowerUnit.WATT.getSICoefficients()).toString()).toString());
        assertEquals(
                "ABVOLT * KILOVOLT is expressed in kg2.m4/s6/A2",
                "[!kg2.m4/s6/A2!]",
                Unit.lookupOrCreateUnitWithSICoefficients(
                        SICoefficients.multiply(ElectricalPotentialUnit.ABVOLT.getSICoefficients(),
                                ElectricalPotentialUnit.KILOVOLT.getSICoefficients()).toString()).toString());
    }

    /**
     * Check objects returned by getAllUnitsOfType
     */
    @SuppressWarnings("static-method")
    @Test
    public void getAllUnitsOfType()
    {
        Unit<?>[] baseUnits =
                {MassUnit.KILOGRAM, LengthUnit.METER, ElectricalCurrentUnit.AMPERE, TimeUnit.SECOND,
                        TemperatureUnit.KELVIN, /* LuminousIntencity.CANDELA, ???.mol */};
        for (Unit<?> u : baseUnits)
        {
            for (Object unitObject : u.getAllUnitsOfThisType())
            {
                assertTrue("getAllUnitsOfThisType returns Units", unitObject instanceof Unit);
                Unit<?> u2 = (Unit<?>) unitObject;
                assertEquals("Standard unit of " + u2 + " should be " + u, u, u2.getStandardUnit());
            }
        }
    }
}

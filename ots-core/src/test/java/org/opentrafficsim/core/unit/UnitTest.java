package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial versionJun 18, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class UnitTest
{
    /**
     * Test the lookupUnitWithSICoefficients method.
     */
    @SuppressWarnings("static-method")
    @Test
    public final void lookupUnitWithSICoefficients()
    {
        assertEquals("ABVOLT is expressed in Volt", "[V]", Unit.lookupUnitWithSICoefficients(
            ElectricalPotentialUnit.ABVOLT.getSICoefficients().toString()).toString());
        assertEquals("ABVOLT / STATAMPERE is expressed in Ohm", "[\u03A9]", Unit.lookupUnitWithSICoefficients(
            SICoefficients.divide(ElectricalPotentialUnit.ABVOLT.getSICoefficients(),
                ElectricalCurrentUnit.STATAMPERE.getSICoefficients()).toString()).toString());
        assertEquals("ABVOLT * STATAMPERE is expressed in Watt", "[W]", Unit.lookupUnitWithSICoefficients(
            SICoefficients.multiply(ElectricalPotentialUnit.ABVOLT.getSICoefficients(),
                ElectricalCurrentUnit.STATAMPERE.getSICoefficients()).toString()).toString());
        assertEquals("ABVOLT / Watt is expressed in Ohm", "[!1/A!]", Unit.lookupOrCreateUnitWithSICoefficients(
            SICoefficients.divide(ElectricalPotentialUnit.ABVOLT.getSICoefficients(), PowerUnit.WATT.getSICoefficients())
                .toString()).toString());
        assertEquals("ABVOLT * KILOVOLT is expressed in kg2.m4/s6/A2", "[!kg2.m4/s6/A2!]", Unit
            .lookupOrCreateUnitWithSICoefficients(
                SICoefficients.multiply(ElectricalPotentialUnit.ABVOLT.getSICoefficients(),
                    ElectricalPotentialUnit.KILOVOLT.getSICoefficients()).toString()).toString());
    }

    /**
     * Check objects returned by getAllUnitsOfType.
     */
    @SuppressWarnings("static-method")
    @Test
    public final void getAllUnitsOfType()
    {
        Unit<?>[] baseUnits =
            {MassUnit.KILOGRAM, LengthUnit.METER, ElectricalCurrentUnit.AMPERE, TimeUnit.SECOND, TemperatureUnit.KELVIN, /*
                                                                                                                          * LuminousIntencity
                                                                                                                          * .
                                                                                                                          * CANDELA
                                                                                                                          * , ?
                                                                                                                          * ? ?
                                                                                                                          * .
                                                                                                                          * mol
                                                                                                                          */};
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

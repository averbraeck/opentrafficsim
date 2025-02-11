package org.opentrafficsim.core.gtu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.definitions.DefaultsNl;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Test the methods and fields in the GtuType class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version 15 jan. 2015 <br>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class GtuTypeTest
{

    /**
     * Constructor.
     */
    public GtuTypeTest()
    {
        //
    }

    /**
     * Run the constructor and verify that all fields get correctly initialized.
     */
    @Test
    public final void constructorTest()
    {
        GtuType t = new GtuType("abc", DefaultsNl.VEHICLE);
        assertTrue("abc".equals(t.getId()), "Id is stored in the newly created GtuType");
        GtuType t2 = new GtuType("pqr", DefaultsNl.VEHICLE);
        assertTrue("pqr".equals(t2.getId()), "Id is stored in the newly created GtuType");
        // prove that the two are really distinct (do not use the same storage for the type string
        assertTrue("abc".equals(t.getId()), "Id is stored in the newly created GtuType");
        assertEquals(DefaultsNl.VEHICLE, t.getParent(), "parent can be retrieved");
    }

    /**
     * Check default GTU characteristics.
     */
    @Test
    public final void defaultsTest()
    {
        StreamInterface randomStream = new MersenneTwister();
        GtuType car = DefaultsNl.CAR;
        String message = "Exception while deriving default GTU characteristics";
        GtuCharacteristics characteristicsCar1 = Defaults.NL.apply(car, randomStream).get();
        GtuCharacteristics characteristicsCar2 = Defaults.NL.apply(car, randomStream).get();
        GtuType spaceCar = new GtuType("spaceCar", car);
        GtuCharacteristics characteristicsSpaceCar1 = Defaults.NL.apply(spaceCar, randomStream).get();
        GtuCharacteristics characteristicsSpaceCar2 = Defaults.NL.apply(spaceCar, randomStream).get();
        GtuType truck = DefaultsNl.TRUCK;
        GtuCharacteristics characteristicsTruck = Defaults.NL.apply(truck, randomStream).get();

        // Note: we can only compare characteristics that we know are not distributed for the used GTU type CAR.
        message = "Default characteristics of DEFAULTS and derived GtuType should be equal.";
        assertEquals(characteristicsCar1.getLength(), characteristicsCar2.getLength(), message);
        assertEquals(characteristicsCar1.getLength(), characteristicsSpaceCar1.getLength(), message);
        assertEquals(characteristicsCar1.getLength(), characteristicsSpaceCar2.getLength(), message);
        assertEquals(characteristicsCar1.getWidth(), characteristicsCar2.getWidth(), message);
        assertEquals(characteristicsCar1.getWidth(), characteristicsSpaceCar1.getWidth(), message);
        assertEquals(characteristicsCar1.getWidth(), characteristicsSpaceCar2.getWidth(), message);

        message = "Default characteristics of distinct DEFAULTS GtuType should not be equal.";
        assertNotEquals(characteristicsCar1.getLength(), characteristicsTruck.getLength(), message);
        assertNotEquals(characteristicsCar1.getWidth(), characteristicsTruck.getWidth(), message);
    }
}

package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.djutils.exceptions.Try;
import org.junit.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.network.Network;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Test the methods and fields in the GtuType class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version 15 jan. 2015 <br>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class GtuTypeTest
{

    /**
     * Run the constructor and verify that all fields get correctly initialized.
     */
    @Test
    public final void constructorTest()
    {
        Network network = new Network("network", new OtsSimulator("Simulator for GtuTypeTest"));
        GtuType t = new GtuType("abc", DefaultsNl.VEHICLE);
        assertTrue("Id is stored in the newly created GtuType", "abc".equals(t.getId()));
        GtuType t2 = new GtuType("pqr", DefaultsNl.VEHICLE);
        assertTrue("Id is stored in the newly created GtuType", "pqr".equals(t2.getId()));
        // prove that the two are really distinct (do not use the same storage for the type string
        assertTrue("Id is stored in the newly created GtuType", "abc".equals(t.getId()));
        assertEquals("parent can be retrieved", DefaultsNl.VEHICLE, t.getParent());
    }

    /**
     * Check default GTU characteristics.
     */
    @Test
    public final void defaultsTest()
    {
        Network network = new Network("network", new OtsSimulator("Simulator for GtuTypeTest"));
        StreamInterface randomStream = new MersenneTwister();
        GtuType car = DefaultsNl.CAR;
        String message = "Exception while deriving default GTU characteristics";
        GtuCharacteristics characteristicsCar1 =
                Try.assign(() -> GtuType.defaultCharacteristics(car, network, randomStream), message);
        GtuCharacteristics characteristicsCar2 =
                Try.assign(() -> GtuType.defaultCharacteristics(car, network, randomStream), message);
        GtuType spaceCar = new GtuType("spaceCar", car);
        GtuCharacteristics characteristicsSpaceCar1 =
                Try.assign(() -> GtuType.defaultCharacteristics(spaceCar, network, randomStream), message);
        GtuCharacteristics characteristicsSpaceCar2 =
                Try.assign(() -> GtuType.defaultCharacteristics(spaceCar, network, randomStream), message);
        GtuType truck = DefaultsNl.TRUCK;
        GtuCharacteristics characteristicsTruck =
                Try.assign(() -> GtuType.defaultCharacteristics(truck, network, randomStream), message);

        // Note: we can only compare characteristics that we know are not distributed for the used GTU type CAR.
        message = "Default characteristics of DEFAULTS and derived GtuType should be equal.";
        assertEquals(message, characteristicsCar1.getLength(), characteristicsCar2.getLength());
        assertEquals(message, characteristicsCar1.getLength(), characteristicsSpaceCar1.getLength());
        assertEquals(message, characteristicsCar1.getLength(), characteristicsSpaceCar2.getLength());
        assertEquals(message, characteristicsCar1.getWidth(), characteristicsCar2.getWidth());
        assertEquals(message, characteristicsCar1.getWidth(), characteristicsSpaceCar1.getWidth());
        assertEquals(message, characteristicsCar1.getWidth(), characteristicsSpaceCar2.getWidth());

        message = "Default characteristics of distinct DEFAULTS GtuType should not be equal.";
        assertNotEquals(message, characteristicsCar1.getLength(), characteristicsTruck.getLength());
        assertNotEquals(message, characteristicsCar1.getWidth(), characteristicsTruck.getWidth());
    }
}

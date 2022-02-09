package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.djutils.exceptions.Try;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.gtu.GTUType.DEFAULTS;
import org.opentrafficsim.core.network.OTSNetwork;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Test the methods and fields in the GTUType class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 15 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUTypeTest
{

    /**
     * Run the constructor and verify that all fields get correctly initialized.
     */
    @Test
    public final void constructorTest()
    {
        OTSNetwork network = new OTSNetwork("network", true, new OTSSimulator("Simulator for GTUTypeTest"));
        GTUType t = new GTUType("abc", network.getGtuType(GTUType.DEFAULTS.VEHICLE));
        assertTrue("Id is stored in the newly created GTUType", "abc".equals(t.getId()));
        GTUType t2 = new GTUType("pqr", network.getGtuType(GTUType.DEFAULTS.VEHICLE));
        assertTrue("Id is stored in the newly created GTUType", "pqr".equals(t2.getId()));
        // prove that the two are really distinct (do not use the same storage for the type string
        assertTrue("Id is stored in the newly created GTUType", "abc".equals(t.getId()));
        assertEquals("parent can be retrieved", network.getGtuType(GTUType.DEFAULTS.VEHICLE), t.getParent());
    }

    /**
     * Check default GTU characteristics.
     */
    @Test
    public final void defaultsTest()
    {
        OTSNetwork network = new OTSNetwork("network", true, new OTSSimulator("Simulator for GTUTypeTest"));
        StreamInterface randomStream = new MersenneTwister();
        GTUType car = network.getGtuType(DEFAULTS.CAR);
        String message = "Exception while deriving default GTU characteristics";
        GTUCharacteristics characteristicsCar1 = Try.assign(() -> GTUType.defaultCharacteristics(car, network, randomStream),
            message);
        GTUCharacteristics characteristicsCar2 = Try.assign(() -> GTUType.defaultCharacteristics(car, network, randomStream),
            message);
        GTUType spaceCar = new GTUType("spaceCar", car);
        GTUCharacteristics characteristicsSpaceCar1 = Try.assign(() -> GTUType.defaultCharacteristics(spaceCar, network,
            randomStream), message);
        GTUCharacteristics characteristicsSpaceCar2 = Try.assign(() -> GTUType.defaultCharacteristics(spaceCar, network,
            randomStream), message);
        GTUType truck = network.getGtuType(DEFAULTS.TRUCK);
        GTUCharacteristics characteristicsTruck = Try.assign(() -> GTUType.defaultCharacteristics(truck, network,
            randomStream), message);

        // Note: we can only compare characteristics that we know are not distributed for the used GTU type CAR.
        message = "Default characteristics of DEFAULTS and derived GTUType should be equal.";
        assertEquals(message, characteristicsCar1.getLength(), characteristicsCar2.getLength());
        assertEquals(message, characteristicsCar1.getLength(), characteristicsSpaceCar1.getLength());
        assertEquals(message, characteristicsCar1.getLength(), characteristicsSpaceCar2.getLength());
        assertEquals(message, characteristicsCar1.getWidth(), characteristicsCar2.getWidth());
        assertEquals(message, characteristicsCar1.getWidth(), characteristicsSpaceCar1.getWidth());
        assertEquals(message, characteristicsCar1.getWidth(), characteristicsSpaceCar2.getWidth());
        
        message = "Default characteristics of distinct DEFAULTS GTUType should not be equal.";
        assertNotEquals(message, characteristicsCar1.getLength(), characteristicsTruck.getLength());
        assertNotEquals(message, characteristicsCar1.getWidth(), characteristicsTruck.getWidth());
    }
}

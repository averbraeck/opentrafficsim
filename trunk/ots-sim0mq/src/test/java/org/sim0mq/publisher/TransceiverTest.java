package org.sim0mq.publisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.event.EventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.junit.Test;
import org.mockito.Mockito;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.mock.MockDEVSSimulator;
import org.opentrafficsim.core.network.OTSNetwork;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Unit tests.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2020-02-13 11:08:16 +0100 (Thu, 13 Feb 2020) $, @version $Revision: 6383 $, by $Author: pknoppers $,
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TransceiverTest
{
    /**
     * Test the GTUIdTransceiver and the GTUTransceiver.
     * @throws RemoteException if the happens, this test has failed
     */
    @Test
    public void testGTUIdTransceiver() throws RemoteException
    {
        try
        {
            new GTUIdTransceiver(null);
            fail("null argument should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        OTSNetwork network = new OTSNetwork("test network for TransceiverTest", true);
        GTUIdTransceiver gtuIdTransceiver = new GTUIdTransceiver(network);
        assertEquals("getId returns correct id", "GTU id transceiver", gtuIdTransceiver.getId());
        assertEquals("address has 0 entries", 0, gtuIdTransceiver.getAddressFields().size());
        assertEquals("result has one field", 1, gtuIdTransceiver.getResultFields().size());
        assertEquals("type of the result field is String", String[].class,
                gtuIdTransceiver.getResultFields().getObjectClass(0));
        assertEquals("description of the result field", "String array filled with all currently valid GTU ids",
                gtuIdTransceiver.getResultFields().getObjectDescription(0));
        try
        {
            gtuIdTransceiver.getResultFields().getObjectClass(1);
            fail("Bad index should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        try
        {
            gtuIdTransceiver.getResultFields().getObjectClass(-1);
            fail("Bad index should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        try
        {
            gtuIdTransceiver.getResultFields().getObjectDescription(1);
            fail("Bad index should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        try
        {
            gtuIdTransceiver.getResultFields().getObjectDescription(-1);
            fail("Bad index should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        for (int i = -1; i <= 1; i++)
        {
            try
            {
                gtuIdTransceiver.getIdSource(i);
                fail("any address level should have thrown an IndexOutOfBoundsException");
            }
            catch (IndexOutOfBoundsException ioobe)
            {
                // Ignore expected exception
            }
        }
        Object[] result = gtuIdTransceiver.get(null);
        assertNotNull("result should not be null", result);
        assertEquals("length of result should be 0", 0, result.length);
        MyMockGTU gtu1 = new MyMockGTU("gtu 1", new GTUType("gtuType 1", network), new DirectedPoint(1, 10, 100, 1, 1, 1),
                new Speed(1, SpeedUnit.KM_PER_HOUR), new Acceleration(1, AccelerationUnit.METER_PER_SECOND_2));
        network.addGTU(gtu1.getMock());
        result = gtuIdTransceiver.get(null);
        assertEquals("length of result is now 1", 1, result.length);
        assertTrue("result contains a string", result[0] instanceof String);
        assertEquals("result[0] is name of our mocked GTU", "gtu 1", (String) (result[0]));
        MyMockGTU gtu2 = new MyMockGTU("gtu 2", new GTUType("gtuType 2", network), new DirectedPoint(2, 20, 200, 2, 2, 2),
                new Speed(2, SpeedUnit.KM_PER_HOUR), new Acceleration(2, AccelerationUnit.METER_PER_SECOND_2));
        network.addGTU(gtu2.getMock());
        result = gtuIdTransceiver.get(new Object[0]);
        assertEquals("length of result is now 2", 2, result.length);
        for (int i = 0; i < 2; i++)
        {
            assertTrue("result contains a string", result[i] instanceof String);
            // Order is not guaranteed; the network maintains the GTUs in a LinkedHashMap
            int count = 0;
            String lookingFor = String.format("gtu %d", i + 1);
            for (int j = 0; j < 2; j++)
            {
                if (lookingFor.equals(result[j]))
                {
                    count++;
                }
            }
            assertEquals("found gtu i once", 1, count);
        }
        // Make the GTUTransceiver
        GTUTransceiver gtuTransceiver = new GTUTransceiver(network, gtuIdTransceiver);
        assertEquals("GTUTransceiver returns correct id", "GTU transceiver", gtuTransceiver.getId());
        assertEquals("getIdSource returns gtuIdTransceiver", gtuIdTransceiver, gtuTransceiver.getIdSource(0));
        try
        {
            gtuTransceiver.getIdSource(1);
            fail("Invalid index should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        try
        {
            gtuTransceiver.getIdSource(-1);
            fail("Invalid index should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        assertEquals("address field 0", "GTU id", gtuTransceiver.getAddressFields().getObjectDescription(0));
        try
        {
            gtuTransceiver.getAddressFields().getObjectDescription(1);
            fail("Invalid index should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        try
        {
            gtuTransceiver.getAddressFields().getObjectDescription(-1);
            fail("Invalid index should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        assertEquals("address field class", String.class, gtuTransceiver.getAddressFields().getObjectClass(0));
        try
        {
            gtuTransceiver.getAddressFields().getObjectClass(1);
            fail("Invalid index should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        try
        {
            gtuTransceiver.getAddressFields().getObjectClass(-1);
            fail("Invalid index should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        for (int i = 0; i < 2; i++)
        {
            Object[] gtuResult = gtuTransceiver.get(new Object[] { result[i] });
            assertNotNull("result is not null", gtuResult);
            assertEquals("result has 6 fields", 6, gtuResult.length);
            assertEquals("first field is a String", String.class, gtuResult[0].getClass());
            assertEquals("gtuResult is gtu with expected id", result[i], gtuResult[0]);
        }
        assertNull("gtuTransceiver returns null for non-existend ID", gtuTransceiver.get(new Object[] { "NONEXISTENTGTU" }));
        try
        {
            gtuTransceiver.get(new Object[] { 123 });
            fail("wrong type in Object[] should have thrown a ClassCastException");
        }
        catch (ClassCastException cce)
        {
            // Ignore expected exception
        }
        assertTrue("toString returns something descriptive", gtuTransceiver.toString().contains("Transceiver"));

    }
    
    /**
     * Test the constructResultFields method for a class that it cannot handle.
     */
    @Test
    public void testNoTransceiver()
    {
        EventType noTranceiver = new EventType("NoTransceiverEventType",
                new MetaData("NoTransceiverEventType", "Event type for which the AbstractEventTransceiver will fail",
                        new ObjectDescriptor[] { new ObjectDescriptor("NoTransceiverEventType",
                                "Event type for which the AbstractEventTransceiver will fail", NoTransceiver.class) }));
        try
        {
            AbstractEventTransceiver.constructResultFields(noTranceiver);
            fail("Should have caught a ClassCastException");
        }
        catch (ClassCastException cce)
        {
            // Ignore expected exception
        }
    }
    
}

/** ... */
class MyMockGTU
{
    /** mocked GTU. */
    private GTU mockGTU;

    /** name. */
    private final java.lang.String name;

    /** gtu type. */
    private final GTUType gtuType;

    /** location. */
    private final DirectedPoint location;

    /** speed. */
    private final Speed speed;

    /** acceleration. */
    private final Acceleration acceleration;

    /** mocked simulator. */
    private final OTSSimulatorInterface simulator = MockDEVSSimulator.createMock();

    /**
     * @param name String; the name of the mocked GTU
     * @param gtuType GTUType; the GTU type
     * @param location DirectedPoint; the location of the mocked GTU
     * @param speed Speed; the speed of the mocked GTU
     * @param acceleration Acceleration; the acceleration of the mocked GTU
     * @throws RemoteException cannot happen ...
     */
    MyMockGTU(final String name, final GTUType gtuType, final DirectedPoint location, final Speed speed,
            final Acceleration acceleration) throws RemoteException
    {
        this.name = name;
        this.gtuType = gtuType;
        this.location = location;
        this.speed = speed;
        this.acceleration = acceleration;
        this.mockGTU = Mockito.mock(GTU.class);
        Mockito.when(this.mockGTU.getSimulator()).thenReturn(this.simulator);
        Mockito.when(this.mockGTU.getGTUType()).thenReturn(this.gtuType);
        Mockito.when(this.mockGTU.getLocation()).thenReturn(this.location);
        Mockito.when(this.mockGTU.getSpeed()).thenReturn(this.speed);
        Mockito.when(this.mockGTU.getAcceleration()).thenReturn(this.acceleration);
        Mockito.when(this.mockGTU.getId()).thenReturn(this.name);
    }

    /**
     * @return mocked DEVSSimulator
     */
    public GTU getMock()
    {
        return this.mockGTU;
    }

}

/** Class for testing the TransceiverInterface. */
class NoTransceiver
{
    /** The payload. */
    private final String payload;

    /**
     * Construct a NoTransceiver object.
     * @param payload String; the payload
     */
    NoTransceiver(final String payload)
    {
        this.payload = payload;
    }

    /**
     * Retrieve the payload.
     * @return String; the payload
     */
    public String getPayload()
    {
        return this.payload;
    }
}

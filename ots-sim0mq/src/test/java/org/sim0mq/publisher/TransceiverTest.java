package org.sim0mq.publisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.PositionVector;
import org.djunits.value.vdouble.vector.data.DoubleVectorData;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventProducerInterface;
import org.djutils.event.TimedEvent;
import org.djutils.event.TimedEventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.junit.Test;
import org.mockito.Mockito;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.mock.MockDEVSSimulator;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.HistoryManagerDEVS;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.sim0mq.Sim0MQException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
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
    /** Storage for the last ACK or NACK value submitted to the ReturnWrapper. */
    private Boolean lastAckNack = null;

    /** Storage for the last payload submitted to the ReturnWrapper. */
    private Object[] lastPayload = null;

    /** Storage for last content submitted to notify method in EventListenerInterface. */
    private Serializable lastContent = null;

    /** Time stamp of last notify event. */
    private Time lastTime = null;

    /**
     * Test the GTUIdTransceiver and the GTUTransceiver.
     * @throws RemoteException if the happens, this test has failed
     * @throws SerializationException
     * @throws Sim0MQException
     * @throws NetworkException
     * @throws OTSGeometryException
     * @throws NamingException
     * @throws SimRuntimeException
     * @throws GTUException
     */
    @Test
    public void testGTUIdTransceiver() throws RemoteException, Sim0MQException, SerializationException, NetworkException,
            OTSGeometryException, SimRuntimeException, NamingException, GTUException
    {
        ReturnWrapper storeLastResult = new ReturnWrapper()
        {
            @Override
            public void encodeReplyAndTransmit(final Boolean ackNack, final Object[] payload)
            {
                lastAckNack = ackNack;
                lastPayload = payload;
            }
        };
        try
        {
            new GTUIdTransceiver(null);
            fail("null argument should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        OTSSimulatorInterface simulator = MockDEVSSimulator.createMock();

        OTSRoadNetwork network = new OTSRoadNetwork("test network for TransceiverTest", true, simulator);
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
                gtuIdTransceiver.getIdSource(i, storeLastResult);
                fail("any address level should have thrown an IndexOutOfBoundsException");
            }
            catch (IndexOutOfBoundsException ioobe)
            {
                // Ignore expected exception
            }
        }
        Object[] result = gtuIdTransceiver.get(null, storeLastResult);
        assertNotNull("result should not be null", result);
        assertEquals("length of result should be 0", 0, result.length);
        result = gtuIdTransceiver.get(new Object[] { "this is a bad address" }, storeLastResult);
        assertNull("result should not be null", result);
        assertEquals("return wrapper got a nack", Boolean.FALSE, this.lastAckNack);
        assertEquals("payload has length 1", 1, this.lastPayload.length);
        assertTrue("element of payload is a String", this.lastPayload[0] instanceof String);
        assertTrue("payload contains \"wrong length\"", ((String) this.lastPayload[0]).contains("wrong length"));

        LaneBasedGTU gtu1 = new MyMockGTU("gtu 1", new GTUType("gtuType 1", network), new DirectedPoint(1, 10, 100, 1, 1, 1),
                new Speed(1, SpeedUnit.KM_PER_HOUR), new Acceleration(1, AccelerationUnit.METER_PER_SECOND_2), simulator)
                        .getMock();
        network.addGTU(gtu1);
        result = gtuIdTransceiver.get(null, storeLastResult);
        assertEquals("length of result is now 1", 1, result.length);
        assertTrue("result contains a string", result[0] instanceof String);
        assertEquals("result[0] is name of our mocked GTU", "gtu 1", (String) (result[0]));
        LaneBasedGTU gtu2 = new MyMockGTU("gtu 2", new GTUType("gtuType 2", network), new DirectedPoint(2, 20, 200, 2, 2, 2),
                new Speed(2, SpeedUnit.KM_PER_HOUR), new Acceleration(2, AccelerationUnit.METER_PER_SECOND_2), simulator)
                        .getMock();
        network.addGTU(gtu2);
        result = gtuIdTransceiver.get(new Object[0], storeLastResult);
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
        assertEquals("getIdSource returns gtuIdTransceiver", gtuIdTransceiver, gtuTransceiver.getIdSource(0, null));
        try
        {
            gtuTransceiver.getIdSource(1, storeLastResult);
            fail("Invalid index should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        try
        {
            gtuTransceiver.getIdSource(-1, storeLastResult);
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
            Object[] gtuResult = gtuTransceiver.get(new Object[] { result[i] }, storeLastResult);
            assertNotNull("result is not null", gtuResult);
            assertEquals("result has 6 fields", 6, gtuResult.length);
            assertEquals("first field is a String", String.class, gtuResult[0].getClass());
            assertEquals("gtuResult is gtu with expected id", result[i], gtuResult[0]);
        }
        assertNull("gtuTransceiver returns null for non-existend ID",
                gtuTransceiver.get(new Object[] { "NONEXISTENTGTU" }, storeLastResult));
        gtuTransceiver.get(new Object[] { 123 }, storeLastResult);
        assertTrue("toString returns something descriptive", gtuTransceiver.toString().contains("Transceiver"));

        NodeIdTransceiver nit = new NodeIdTransceiver(network);
        assertTrue("toString of node id transceiver returns something descriptive",
                nit.toString().startsWith("NodeIdTransceiver"));

        LinkIdTransceiver lit = new LinkIdTransceiver(network);
        assertTrue("toString of link id transceiver returns something descriptive",
                lit.toString().startsWith("LinkIdTransceiver"));

        // Give the network two nodes and a link with a lane - A lot of code is required to create a lane :-(
        OTSPoint3D node1Point = new OTSPoint3D(10, 20, 30);
        OTSRoadNode node1 = new OTSRoadNode(network, "node 1", node1Point, Direction.ZERO);
        OTSRoadNode node2 = new OTSRoadNode(network, "node 2", new OTSPoint3D(110, 20, 30), Direction.ZERO);
        LinkType roadLinkType = network.getLinkType(LinkType.DEFAULTS.ROAD);
        CrossSectionLink link = new CrossSectionLink(network, "1 to 2", node1, node2, roadLinkType,
                new OTSLine3D(node1.getPoint(), node2.getPoint()), LaneKeepingPolicy.KEEPRIGHT);
        LaneType laneType = network.getLaneType(LaneType.DEFAULTS.RESIDENTIAL_ROAD_LANE);
        OTSReplication replication = Mockito.mock(OTSReplication.class);
        HistoryManagerDEVS hmd = Mockito.mock(HistoryManagerDEVS.class);
        Mockito.when(hmd.now()).thenReturn(Time.ZERO);
        Mockito.when(replication.getHistoryManager(simulator)).thenReturn(hmd);
        Mockito.when(simulator.getReplication()).thenReturn(replication);
        Lane lane = new Lane(link, "lane", Length.ZERO, Length.ZERO, new Length(3, LengthUnit.METER),
                new Length(3, LengthUnit.METER), laneType, new Speed(50, SpeedUnit.KM_PER_HOUR));
        Stripe stripe = new Stripe(link, Length.ZERO, Length.ZERO, new Length(20, LengthUnit.DECIMETER));
        String stripeId = stripe.getId();

        LinkGTUIdTransceiver linkgit = new LinkGTUIdTransceiver(network);
        assertTrue("toString of LinkGTUIdTransceiver returns something descriptive",
                linkgit.toString().startsWith("LinkGTUIdTransceiver"));
        assertFalse("LinkGTUIdTransceiver does not have an id source", linkgit.hasIdSource());

        lastAckNack = null;
        result = linkgit.get(new Object[] { "bad", "address" }, storeLastResult);
        assertNull(result);
        assertEquals("Bad address have sent a NACK", Boolean.FALSE, this.lastAckNack);
        assertTrue("NACK describes the problem", ((String) this.lastPayload[0]).contains("need id of a link"));

        lastAckNack = null;
        result = linkgit.get(new Object[] { "Non existing link" }, storeLastResult);
        assertNull("Non existing link should have returned null", result);
        assertEquals("Non existing link should have sent a NACK", Boolean.FALSE, this.lastAckNack);
        assertTrue("Description for non existing link",
                ((String) this.lastPayload[0]).startsWith("Network does not contain a link with id"));

        lastAckNack = null;
        result = linkgit.get(new Object[] { "1 to 2" }, storeLastResult);
        assertNotNull(result);
        assertEquals("result is empty array", 0, result.length);
        assertNull(this.lastAckNack);

        LaneGTUIdTransceiver lanegit = new LaneGTUIdTransceiver(network);
        assertTrue("toString of LaneGTUIdTransceiver returns something descriptive",
                lanegit.toString().startsWith("LaneGTUIdTransceiver"));
        assertFalse("LaneGTUIdTransceiver does not have an Id source", lanegit.hasIdSource());

        lastAckNack = null;
        result = lanegit.get(new Object[] { "this", "is", "a", "bad", "address" }, storeLastResult);
        assertNull(result);
        assertEquals("Bad address have sent a NACK", Boolean.FALSE, this.lastAckNack);
        assertTrue("NACK describes the problem",
                ((String) this.lastPayload[0]).contains("need id of a link and id of a CrossSectionElement"));

        lastAckNack = null;
        result = lanegit.get(new Object[] { "Non existing link", "Non existing lane" }, storeLastResult);
        assertEquals("Non existing link should have sent a NACK", Boolean.FALSE, this.lastAckNack);
        assertTrue("Description for non existing link",
                ((String) this.lastPayload[0]).startsWith("Network does not contain a link with id"));

        lastAckNack = null;
        result = lanegit.get(new Object[] { "1 to 2", "Non existing lane" }, storeLastResult);
        assertEquals("Existing link but non existing lane should have sent a NACK", Boolean.FALSE, this.lastAckNack);
        assertTrue("Description for non existing link",
                ((String) this.lastPayload[0]).contains("does not contain a cross section element with id"));

        lastAckNack = null;
        result = lanegit.get(new Object[] { "1 to 2", stripeId }, storeLastResult);
        assertEquals("Existing link but non existing lane should have sent a NACK", Boolean.FALSE, this.lastAckNack);
        assertTrue("Description for non existing link", ((String) this.lastPayload[0]).contains("is not a lane"));

        lastAckNack = null;
        result = lanegit.get(new Object[] { "1 to 2", "lane" }, storeLastResult);
        assertNull("Existing link and lane should not have sent a NACK or ACK", this.lastAckNack);
        assertEquals("Existing link and lane should have sent empty array", 0, result.length);

        // Put one of the GTUs on the lane
        lane.addGTU((LaneBasedGTU) gtu1, 0.3);

        lastAckNack = null;
        result = linkgit.get(new Object[] { "1 to 2" }, storeLastResult);
        assertNotNull(result);
        assertEquals("result is array with one entry", 1, result.length);
        assertEquals("content of entry is id of gtu1", gtu1.getId(), result[0]);
        assertNull(this.lastAckNack);

        result = lanegit.get(new Object[] { "1 to 2", "lane" }, storeLastResult);
        assertNull("Existing link and lane should not have sent a NACK or ACK", this.lastAckNack);
        assertEquals("Existing link and lane should have sent empty array", 1, result.length);
        assertEquals("content of entry is id of gtu1", gtu1.getId(), result[0]);
        assertNull(this.lastAckNack);

        Mockito.when(simulator.isInitialized()).thenReturn(false);
        SimulatorStateTransceiver sst = new SimulatorStateTransceiver(simulator);
        result = sst.get(null, storeLastResult);
        assertEquals("get returned one element Object array", 1, result.length);
        assertEquals("Mock simulator pretends not to have been initialized", "Not (yet) initialized", result[0]);
        Mockito.when(simulator.isInitialized()).thenReturn(true);
        // Next statement is not really needed; just making sure
        Mockito.when(simulator.isStartingOrRunning()).thenReturn(false);
        result = sst.get(null, storeLastResult);
        assertEquals("get returned one element Object array", 1, result.length);
        assertEquals("Mock simulator pretends be in stopped state", "Stopping or stopped", result[0]);
        Mockito.when(simulator.isStartingOrRunning()).thenReturn(true);
        result = sst.get(null, storeLastResult);
        assertEquals("get returned one element Object array", 1, result.length);
        assertEquals("Mock simulator pretends be in stopped state", "Starting or running", result[0]);
        LookupEventProducerInterface lepi = sst.getLookupEventProducerInterface();
        EventProducerInterface epi = lepi.lookup(null, storeLastResult);
        TimedEvent<Time> tev =
                new TimedEvent<>(SimulatorInterface.START_EVENT, simulator, null, new Time(123, TimeUnit.BASE_SECOND));
        EventListenerInterface recordingListener = new EventListenerInterface()
        {
            /** ... */
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            public void notify(final EventInterface event) throws RemoteException
            {
                lastContent = event.getContent();
                lastTime = null;
                if (event instanceof TimedEvent)
                {
                    lastTime = (Time) ((TimedEvent<Time>) event).getTimeStamp();
                }
            }
        };
        epi.addListener(recordingListener, SimulatorStateTransceiver.SIMULATOR_STATE_CHANGED);
        this.lastContent = null;
        ((EventListenerInterface) epi).notify(tev);
        assertEquals("last time is 123", 123.0, this.lastTime.si, 0);
        tev = new TimedEvent<>(SimulatorInterface.STOP_EVENT, simulator, null, new Time(1234, TimeUnit.BASE_SECOND));
        this.lastContent = null;
        ((EventListenerInterface) epi).notify(tev);
        assertEquals("lastContent is now true", Boolean.FALSE, this.lastContent);
        assertEquals("last time is 1234", 1234.0, this.lastTime.si, 0);

        this.lastAckNack = null; // make sure we can see that is has been set
        assertNull("using a bad address returns null", lepi.lookup(new Object[] { "This is a bad address" }, storeLastResult));
        assertEquals("using a bad address sends a NACK", Boolean.FALSE, this.lastAckNack);
        assertTrue("NACK message contains \"wrong length\"", ((String) this.lastPayload[0]).contains("wrong length"));

        NodeTransceiver nt = new NodeTransceiver(network, nit);
        assertTrue("toString of NodeTransceiver returns something descriptive", nt.toString().startsWith("NodeTransceiver"));
        assertTrue("NodeTransceiver has a Id source", nt.hasIdSource());
        assertEquals("NodeTransceiver returns NodeIdTransceiver at level 0", nit, nt.getIdSource(0, storeLastResult));

        this.lastAckNack = null; // make sure we can see that is has been set
        assertNull("Bad address level returns null", nt.getIdSource(1, storeLastResult));
        assertEquals("Bad address sent a NACK", Boolean.FALSE, this.lastAckNack);
        assertEquals("Bad address NACK describes the problem", "Only empty address is valid", this.lastPayload[0]);

        this.lastAckNack = null; // make sure we can see that is has been set
        result = nt.get(null, storeLastResult);
        assertNull("bad address should have returned null", result);
        assertEquals("bad address should have returned NACK", Boolean.FALSE, this.lastAckNack);
        assertEquals("null address is correctly diagnosed as error", "Address may not be null", this.lastPayload[0]);

        this.lastAckNack = null; // make sure we can see that is has been set
        result = nt.get(new Object[] { "Non existing node" }, storeLastResult);
        assertNull("non existing node should have returned null", result);
        assertEquals("non existing node should have sent a NACK", Boolean.FALSE, this.lastAckNack);
        assertEquals("message with NACK is descriptive", "Network does not contain a node with id Non existing node",
                this.lastPayload[0]);

        this.lastAckNack = null;
        result = nt.get(new Object[] { node1.getId() }, storeLastResult);
        assertEquals("result contains 3 fields", 4, result.length);
        assertEquals("field 0 is node id", node1.getId(), result[0]);
        assertTrue("field 1 is a position vector", result[1] instanceof PositionVector);
        PositionVector pv = (PositionVector) result[1];
        assertEquals("Position vector size is 3", 3, pv.size());
        assertEquals("x matches", node1Point.x, pv.get(0).si, 0);
        assertEquals("y matches", node1Point.y, pv.get(1).si, 0);
        assertEquals("z matches", node1Point.z, pv.get(2).si, 0);
        assertEquals("direction matches", Direction.ZERO, result[2]);
        assertEquals("Number of links is 1", 1, result[3]);

        this.lastAckNack = null;
        LinkTransceiver lt = new LinkTransceiver(network, lit);
        assertTrue("toString returns something descriptive", lt.toString().startsWith("LinkTransceiver"));
        assertEquals("LinkTransceiver can return LinkIdTransceiver", lit, lt.getIdSource(0, storeLastResult));
        assertNull("No ACK or NACK received", this.lastAckNack);

        assertNull("Bad address level returns null", lt.getIdSource(1, storeLastResult));
        assertEquals("Bad address level sent a NACK", Boolean.FALSE, this.lastAckNack);
        assertEquals("Message of NACK describes the problem", "Only empty address is valid", this.lastPayload[0]);

        this.lastAckNack = null;
        assertNull("bad address returns null", lt.get(null, storeLastResult));
        assertEquals("bad address sends a NACK", Boolean.FALSE, this.lastAckNack);
        assertEquals("bad address NACK describes the problem", "Address may not be null", this.lastPayload[0]);

        this.lastAckNack = null;
        assertNull("bad address returns null", lt.get(new Object[] {}, storeLastResult));
        assertEquals("bad address sends a NACK", Boolean.FALSE, this.lastAckNack);
        assertTrue("bad address NACK describes the problem", ((String) this.lastPayload[0]).contains("has wrong length"));

        this.lastAckNack = null;
        assertNull("Non existing link name returns null", lt.get(new Object[] { "Non existing link name" }, storeLastResult));
        assertEquals("Non existing link name sends a NACK", Boolean.FALSE, this.lastAckNack);
        assertTrue("Non existing link name NACK describes the problem",
                ((String) this.lastPayload[0]).contains("Network does not contain a link with id"));

        this.lastAckNack = null;
        result = lt.get(new Object[] {"1 to 2"}, storeLastResult);
        assertNull("No ACK or NACK", this.lastAckNack);
        assertEquals("result contains 7 elements", 7, result.length);
        assertEquals("result is our link", link.getId(), result[0]);
        assertEquals("link type is ROAD", LinkType.DEFAULTS.ROAD.getId(), result[1]);
        assertEquals("from node", node1.getId(), result[2]);
        assertEquals("to node", node2.getId(), result[3]);
        assertEquals("number of points in design line is 2", link.getDesignLine().size(), result[4]);
        assertEquals("number of GTUs on link", link.getGTUCount(), result[5]);
        assertEquals("number of cross section elements", link.getCrossSectionElementList().size(), result[6]);
    }

    /**
     * Test the constructResultFields method for a class that it cannot handle.
     */
    @Test
    public void testNoTransceiver()
    {
        TimedEventType noTranceiver = new TimedEventType("NoTransceiverEventType",
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

    /**
     * Test the static verifyMetaData method in AbstractTransceiver.
     */
    @Test
    public void testVerifyMetaData()
    {
        assertNull("NO_META_DATA allows anything", AbstractTransceiver.verifyMetaData(MetaData.NO_META_DATA, null));
        assertNull("NO_META_DATA allows anything", AbstractTransceiver.verifyMetaData(MetaData.NO_META_DATA, new Object[] {}));
        assertNull("NO_META_DATA allows anything",
                AbstractTransceiver.verifyMetaData(MetaData.NO_META_DATA, new Object[] { "Anything goes" }));

        MetaData md = new MetaData("A", "a", new ObjectDescriptor[] { new ObjectDescriptor("String", "string", String.class),
                new ObjectDescriptor("Double", "double", Double.class) });
        assertEquals("empty is not ok", "Address may not be null", AbstractTransceiver.verifyMetaData(md, null));
        assertTrue("wrong length", AbstractTransceiver.verifyMetaData(md, new Object[] {}).contains("has wrong length"));
        assertTrue("wrong type",
                AbstractTransceiver.verifyMetaData(md, new Object[] { 123.456, 234.567 }).contains("cannot be used for"));
        assertNull("Good address returns null", AbstractTransceiver.verifyMetaData(md, new Object[] { "hello", 234.567 }));
    }

}

/** ... */
class MyMockGTU
{
    /** mocked GTU. */
    private LaneBasedGTU mockGTU;

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
    private final OTSSimulatorInterface simulator;

    /**
     * @param name String; the name of the mocked GTU
     * @param gtuType GTUType; the GTU type
     * @param location DirectedPoint; the location of the mocked GTU
     * @param speed Speed; the speed of the mocked GTU
     * @param acceleration Acceleration; the acceleration of the mocked GTU
     * @param simulator OTSSimulatorInterface; (mocked) simulator
     * @throws RemoteException cannot happen ...
     */
    MyMockGTU(final String name, final GTUType gtuType, final DirectedPoint location, final Speed speed,
            final Acceleration acceleration, final OTSSimulatorInterface simulator) throws RemoteException
    {
        this.name = name;
        this.gtuType = gtuType;
        this.location = location;
        this.speed = speed;
        this.acceleration = acceleration;
        this.simulator = simulator;
        this.mockGTU = Mockito.mock(LaneBasedGTU.class);
        Mockito.when(this.mockGTU.getSimulator()).thenReturn(this.simulator);
        Mockito.when(this.mockGTU.getGTUType()).thenReturn(this.gtuType);
        Mockito.when(this.mockGTU.getLocation()).thenReturn(this.location);
        Mockito.when(this.mockGTU.getSpeed()).thenReturn(this.speed);
        Mockito.when(this.mockGTU.getAcceleration()).thenReturn(this.acceleration);
        Mockito.when(this.mockGTU.getId()).thenReturn(this.name);
    }

    /**
     * @return mocked GTU
     */
    public LaneBasedGTU getMock()
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

package org.opentrafficsim.sim0mq.publisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.PositionVector;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventProducer;
import org.djutils.event.EventType;
import org.djutils.event.TimedEvent;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.junit.Test;
import org.mockito.Mockito;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsReplication;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsPoint3d;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Type;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.sim0mq.Sim0MQException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Unit tests.
 * <p>
 * Copyright (c) 2020-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class TransceiverTest
{
    /** Storage for the last ACK or NACK value submitted to the ReturnWrapper. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Boolean lastAckNack = null;

    /** Storage for the last payload submitted to the ReturnWrapper. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Object[] lastPayload = null;

    /** Storage for last content submitted to notify method in EventListener. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Serializable lastContent = null;

    /** Time stamp of last notify event. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Time lastTime = null;

    /**
     * Test the GtuIdTransceiver and the GtuTransceiver.
     * @throws RemoteException if the happens, this test has failed
     * @throws SerializationException on error
     * @throws Sim0MQException on error
     * @throws NetworkException on error
     * @throws OtsGeometryException on error
     * @throws NamingException on error
     * @throws SimRuntimeException on error
     * @throws GtuException on error
     */
    @Test
    public void testGtuIdTransceiver() throws RemoteException, Sim0MQException, SerializationException, NetworkException,
            OtsGeometryException, SimRuntimeException, NamingException, GtuException
    {
        ReturnWrapper storeLastResult = new ReturnWrapper()
        {
            @Override
            public void encodeReplyAndTransmit(final Boolean ackNack, final Object[] payload)
            {
                TransceiverTest.this.lastAckNack = ackNack;
                TransceiverTest.this.lastPayload = payload;
            }
        };
        try
        {
            new GtuIdTransceiver(null);
            fail("null argument should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        OtsSimulatorInterface simulator = MockDevsSimulator.createMock();

        OtsRoadNetwork network = new OtsRoadNetwork("test network for TransceiverTest", simulator);
        GtuIdTransceiver gtuIdTransceiver = new GtuIdTransceiver(network);
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
        assertNull("Bad address",
                checkAckNack(gtuIdTransceiver, new Object[] {"this is a bad address"}, false, "wrong length"));

        GtuType gtuType = new GtuType("gtuType 1");
        LaneBasedGtu gtu1 =
                new MyMockGTU("gtu 1", gtuType, new DirectedPoint(1, 10, 100, 1, 1, 1), new Speed(1, SpeedUnit.KM_PER_HOUR),
                        new Acceleration(1, AccelerationUnit.METER_PER_SECOND_2), simulator).getMock();
        network.addGTU(gtu1);
        result = gtuIdTransceiver.get(null, storeLastResult);
        assertEquals("length of result is now 1", 1, result.length);
        assertTrue("result contains a string", result[0] instanceof String);
        assertEquals("result[0] is name of our mocked GTU", "gtu 1", result[0]);
        LaneBasedGtu gtu2 =
                new MyMockGTU("gtu 2", gtuType, new DirectedPoint(2, 20, 200, 2, 2, 2), new Speed(2, SpeedUnit.KM_PER_HOUR),
                        new Acceleration(2, AccelerationUnit.METER_PER_SECOND_2), simulator).getMock();
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
        // Make the GtuTransceiver
        GtuTransceiver gtuTransceiver = new GtuTransceiver(network, gtuIdTransceiver);
        assertEquals("GtuTransceiver returns correct id", "GTU transceiver", gtuTransceiver.getId());
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
            Object[] gtuResult = gtuTransceiver.get(new Object[] {result[i]}, storeLastResult);
            assertNotNull("result is not null", gtuResult);
            assertEquals("result has 6 fields", 6, gtuResult.length);
            assertEquals("first field is a String", String.class, gtuResult[0].getClass());
            assertEquals("gtuResult is gtu with expected id", result[i], gtuResult[0]);
            LaneBasedGtu gtu = (LaneBasedGtu) network.getGTU(((String) gtuResult[0]));
            assertNotNull("GTU is in the network", gtu);
            assertTrue("field 1 is id of a GtuType", gtuResult[1] instanceof String);
            assertEquals("gtu type matches", gtuType.getId(), gtuResult[1]);
            assertEquals("x matches", gtu.getLocation().x, ((PositionVector) gtuResult[2]).get(0).si, 0.0000);
            assertEquals("y matches", gtu.getLocation().y, ((PositionVector) gtuResult[2]).get(1).si, 0.0000);
            assertEquals("z matches", gtu.getLocation().z, ((PositionVector) gtuResult[2]).get(2).si, 0.0000);
            assertEquals("direction matches", new Direction(gtu.getLocation().getRotZ(), DirectionUnit.EAST_DEGREE).si,
                    ((Direction) gtuResult[3]).si, 0.0001);
            assertEquals("speed", gtu.getSpeed(), gtuResult[4]);
            assertEquals("acceleration", gtu.getAcceleration(), gtuResult[5]);
        }
        assertNull("gtuTransceiver returns null for non-existend ID",
                gtuTransceiver.get(new Object[] {"NONEXISTENTGTU"}, storeLastResult));
        gtuTransceiver.get(new Object[] {123}, storeLastResult);
        assertTrue("toString returns something descriptive", gtuTransceiver.toString().contains("Transceiver"));

        NodeIdTransceiver nit = new NodeIdTransceiver(network);
        assertTrue("toString of node id transceiver returns something descriptive",
                nit.toString().startsWith("NodeIdTransceiver"));

        LinkIdTransceiver lit = new LinkIdTransceiver(network);
        assertTrue("toString of link id transceiver returns something descriptive",
                lit.toString().startsWith("LinkIdTransceiver"));

        // Give the network two nodes and a link with a lane - A lot of code is required to create a lane :-(
        OtsPoint3d node1Point = new OtsPoint3d(10, 20, 30);
        Node node1 = new Node(network, "node 1", node1Point, Direction.ZERO);
        Node node2 = new Node(network, "node 2", new OtsPoint3d(110, 20, 30), Direction.ZERO);
        LinkType roadLinkType = DefaultsNl.ROAD;
        CrossSectionLink link = new CrossSectionLink(network, "1 to 2", node1, node2, roadLinkType,
                new OtsLine3d(node1.getPoint(), node2.getPoint()), LaneKeepingPolicy.KEEPRIGHT);
        LaneType laneType = DefaultsRoadNl.RESIDENTIAL_ROAD;
        OtsReplication replication = Mockito.mock(OtsReplication.class);
        HistoryManagerDevs hmd = Mockito.mock(HistoryManagerDevs.class);
        Mockito.when(hmd.now()).thenReturn(Time.ZERO);
        Mockito.when(replication.getHistoryManager(simulator)).thenReturn(hmd);
        Mockito.when(simulator.getReplication()).thenReturn(replication);
        Lane lane = new Lane(link, "lane", Length.ZERO, new Length(3, LengthUnit.METER), laneType,
                Map.of(DefaultsNl.VEHICLE, new Speed(50, SpeedUnit.KM_PER_HOUR)));
        Length width = new Length(20, LengthUnit.DECIMETER);
        Stripe stripe = new Stripe(Type.DASHED, link, Length.ZERO, width);
        String stripeId = stripe.getId();

        LinkGtuIdTransceiver linkgit = new LinkGtuIdTransceiver(network);
        assertTrue("toString of LinkGtuIdTransceiver returns something descriptive",
                linkgit.toString().startsWith("LinkGtuIdTransceiver"));
        assertFalse("LinkGtuIdTransceiver does not have an id source", linkgit.hasIdSource());

        assertNull("Bad address", checkAckNack(linkgit, new Object[] {"bad", "address"}, false, "need id of a link"));
        assertNull("Non existing link",
                checkAckNack(linkgit, new Object[] {"Non existing link"}, false, "Network does not contain a link with id"));

        this.lastAckNack = null;
        result = linkgit.get(new Object[] {"1 to 2"}, storeLastResult);
        assertNotNull(result);
        assertEquals("result is empty array", 0, result.length);
        assertNull(this.lastAckNack);

        LaneGtuIdTransceiver lanegit = new LaneGtuIdTransceiver(network);
        assertTrue("toString of LaneGtuIdTransceiver returns something descriptive",
                lanegit.toString().startsWith("LaneGtuIdTransceiver"));
        assertFalse("LaneGtuIdTransceiver does not have an Id source", lanegit.hasIdSource());

        assertNull("Bad address", checkAckNack(lanegit, new Object[] {"this", "is", "a", "bad", "address"}, false,
                "need id of a link and id of a CrossSectionElement"));
        assertNull("Non existing link", checkAckNack(lanegit, new Object[] {"Non existing link", "Non existing lane"}, false,
                "Network does not contain a link with id"));
        assertNull("Existing link but non existing lane", checkAckNack(lanegit, new Object[] {"1 to 2", "Non existing lane"},
                false, "does not contain a cross section element with id"));
        assertNull("Existing link, but non a lane",
                checkAckNack(lanegit, new Object[] {"1 to 2", stripeId}, false, "is not a lane"));

        this.lastAckNack = null;
        result = lanegit.get(new Object[] {"1 to 2", "lane"}, storeLastResult);
        assertNull("Existing link and lane should not have sent a NACK or ACK", this.lastAckNack);
        assertEquals("Existing link and lane should have sent empty array", 0, result.length);

        // Put one of the GTUs on the lane
        lane.addGtu(gtu1, 0.3);

        this.lastAckNack = null;
        result = linkgit.get(new Object[] {"1 to 2"}, storeLastResult);
        assertNotNull(result);
        assertEquals("result is array with one entry", 1, result.length);
        assertEquals("content of entry is id of gtu1", gtu1.getId(), result[0]);
        assertNull(this.lastAckNack);

        result = lanegit.get(new Object[] {"1 to 2", "lane"}, storeLastResult);
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
        LookupEventProducer lepi = sst.getLookupEventProducer();
        EventProducer epi = lepi.lookup(null, storeLastResult);
        TimedEvent<Time> tev = new TimedEvent<>(SimulatorInterface.START_EVENT, null, new Time(123, TimeUnit.BASE_SECOND));
        EventListener recordingListener = new EventListener()
        {
            /** ... */
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            public void notify(final Event event) throws RemoteException
            {
                TransceiverTest.this.lastContent = event.getContent();
                TransceiverTest.this.lastTime = null;
                if (event instanceof TimedEvent)
                {
                    TransceiverTest.this.lastTime = ((TimedEvent<Time>) event).getTimeStamp();
                }
            }
        };
        epi.addListener(recordingListener, SimulatorStateTransceiver.SIMULATOR_STATE_CHANGED);
        this.lastContent = null;
        ((EventListener) epi).notify(tev);
        assertEquals("last time is 123", 123.0, this.lastTime.si, 0);
        tev = new TimedEvent<>(SimulatorInterface.STOP_EVENT, null, new Time(1234, TimeUnit.BASE_SECOND));
        this.lastContent = null;
        ((EventListener) epi).notify(tev);
        assertEquals("lastContent is now true", Boolean.FALSE, this.lastContent);
        assertEquals("last time is 1234", 1234.0, this.lastTime.si, 0);

        this.lastAckNack = null; // make sure we can see that is has been set
        assertNull("using a bad address returns null", lepi.lookup(new Object[] {"This is a bad address"}, storeLastResult));
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

        assertNull("Bad address", checkAckNack(nt, null, false, "Address may not be null"));
        assertNull("Bad address", checkAckNack(nt, new Object[] {"Non existing node"}, false,
                "Network does not contain a node with id Non existing node"));

        this.lastAckNack = null;
        result = nt.get(new Object[] {node1.getId()}, storeLastResult);
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

        assertNull("Bad address", checkAckNack(lt, null, false, "Address may not be null"));
        assertNull("Bad address", checkAckNack(lt, new Object[] {}, false, "has wrong length"));
        assertNull("Non existing link name",
                checkAckNack(lt, new Object[] {"Non existing link name"}, false, "Network does not contain a link with id"));

        this.lastAckNack = null;
        result = lt.get(new Object[] {"1 to 2"}, storeLastResult);
        assertNull("No ACK or NACK", this.lastAckNack);
        assertEquals("result contains 7 elements", 7, result.length);
        assertEquals("result is our link", link.getId(), result[0]);
        assertEquals("link type is ROAD", DefaultsNl.ROAD.getId(), result[1]);
        assertEquals("from node", node1.getId(), result[2]);
        assertEquals("to node", node2.getId(), result[3]);
        assertEquals("number of points in design line is 2", link.getDesignLine().size(), result[4]);
        assertEquals("number of GTUs on link", link.getGTUCount(), result[5]);
        assertEquals("number of cross section elements", link.getCrossSectionElementList().size(), result[6]);

        CrossSectionElementTransceiver cset = new CrossSectionElementTransceiver(network);
        assertTrue("toString returns something descriptive", cset.toString().startsWith("CrossSectionElementTransceiver"));

        assertNull("Bad address", checkAckNack(cset, null, false, "Address may not be null"));
        assertNull("Bad address",
                checkAckNack(cset, new Object[] {"bad", "address", "has", "too", "many", "fields"}, false, "has wrong length"));
        assertNull("Bad address", checkAckNack(cset, new Object[] {"1 to 2", -1}, false, "valid range is"));
        assertNull("Bad address",
                checkAckNack(cset, new Object[] {"NON EXISTENT LINK", 0}, false, "Network does not contain a link with id"));
        assertNull("Bad address", checkAckNack(cset, new Object[] {"1 to 2", 2}, false, "valid range is"));

        this.lastAckNack = null;
        result = cset.get(new Object[] {"1 to 2", 0}, storeLastResult);
        assertNull("No NACK (or ACK)", this.lastAckNack);
        assertEquals("result contains 7 elements", 7, result.length);
        assertEquals("id", lane.getId(), result[0]);
        assertEquals("class name", lane.getClass().getName(), result[1]);
        assertEquals("length", lane.getLength(), result[2]);
        assertEquals("width at begin", lane.getWidth(0.0), result[3]);
        assertEquals("design line offset at begin", lane.getDesignLineOffsetAtBegin(), result[4]);
        assertEquals("width at end", lane.getWidth(1.0), result[5]);
        assertEquals("design line offset at end", lane.getDesignLineOffsetAtEnd(), result[6]);

    }

    /**
     * Call the get method of a TransceiverInterface and verify most of the results.
     * @param transceiver TransceiverInterface; the transceiver to test
     * @param address Object[]; the argument of the get method of the transceiver
     * @param expectedAckNack Boolean; null if neither an ACK nor a NACK is expected, Boolean.FALSE if a NACK is expected,
     *            Boolean.TRUE if an ACK is expected
     * @param expectedInPayload String; text that should occur in the payload of the ACK or NACK, or null if no ACK or NACK is
     *            expected
     * @return Object[]; the result of the get method should be null if an ACK or a NACK was received; non-null otherwise
     * @throws SerializationException if that happens, this test has failed
     * @throws Sim0MQException if that happens, this test has failed
     * @throws RemoteException if that happens, this test has failed
     */
    public Object[] checkAckNack(final TransceiverInterface transceiver, final Object[] address, final Boolean expectedAckNack,
            final String expectedInPayload) throws RemoteException, Sim0MQException, SerializationException
    {
        ReturnWrapper storeLastResult = new ReturnWrapper()
        {
            @Override
            public void encodeReplyAndTransmit(final Boolean ackNack, final Object[] payload)
            {
                assertEquals("ACK/NACK", expectedAckNack, ackNack);
                if (null != ackNack)
                {
                    assertNotNull("ACK or NACK should have a payload", payload);
                    assertNotNull("payload with ACK or NACK should not be null", payload);
                    assertEquals("payload has one element", 1, payload.length);
                    assertTrue("the single field of the payload is a string", payload[0] instanceof String);
                    assertTrue("payload should contain expected text", ((String) payload[0]).contains(expectedInPayload));
                }
                assertNotNull("payload should not be null", payload);
            }
        };
        return transceiver.get(address, storeLastResult);
    }

    /**
     * Test the constructResultFields method for a class that it cannot handle.
     */
    @Test
    public void testNoTransceiver()
    {
        EventType noTranceiver = new EventType("NoTransceiverEventType",
                new MetaData("NoTransceiverEventType", "Event type for which the AbstractEventTransceiver will fail",
                        new ObjectDescriptor[] {new ObjectDescriptor("NoTransceiverEventType",
                                "Event type for which the AbstractEventTransceiver will fail", NoTransceiver.class)}));
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
                AbstractTransceiver.verifyMetaData(MetaData.NO_META_DATA, new Object[] {"Anything goes"}));

        MetaData md = new MetaData("A", "a", new ObjectDescriptor[] {new ObjectDescriptor("String", "string", String.class),
                new ObjectDescriptor("Double", "double", Double.class)});
        assertEquals("empty is not ok", "Address may not be null", AbstractTransceiver.verifyMetaData(md, null));
        assertTrue("wrong length", AbstractTransceiver.verifyMetaData(md, new Object[] {}).contains("has wrong length"));
        assertTrue("wrong type",
                AbstractTransceiver.verifyMetaData(md, new Object[] {123.456, 234.567}).contains("cannot be used for"));
        assertNull("Good address returns null", AbstractTransceiver.verifyMetaData(md, new Object[] {"hello", 234.567}));
    }

}

/** ... */
class MyMockGTU
{
    /** mocked GTU. */
    private LaneBasedGtu mockGTU;

    /** name. */
    private final java.lang.String name;

    /** gtu type. */
    private final GtuType gtuType;

    /** location. */
    private final DirectedPoint location;

    /** speed. */
    private final Speed speed;

    /** acceleration. */
    private final Acceleration acceleration;

    /** mocked simulator. */
    private final OtsSimulatorInterface simulator;

    /**
     * @param name String; the name of the mocked GTU
     * @param gtuType GtuType; the GTU type
     * @param location DirectedPoint; the location of the mocked GTU
     * @param speed Speed; the speed of the mocked GTU
     * @param acceleration Acceleration; the acceleration of the mocked GTU
     * @param simulator OtsSimulatorInterface; (mocked) simulator
     * @throws RemoteException cannot happen ...
     */
    MyMockGTU(final String name, final GtuType gtuType, final DirectedPoint location, final Speed speed,
            final Acceleration acceleration, final OtsSimulatorInterface simulator) throws RemoteException
    {
        this.name = name;
        this.gtuType = gtuType;
        this.location = location;
        this.speed = speed;
        this.acceleration = acceleration;
        this.simulator = simulator;
        this.mockGTU = Mockito.mock(LaneBasedGtu.class);
        Mockito.when(this.mockGTU.getSimulator()).thenReturn(this.simulator);
        Mockito.when(this.mockGTU.getType()).thenReturn(this.gtuType);
        Mockito.when(this.mockGTU.getLocation()).thenReturn(this.location);
        Mockito.when(this.mockGTU.getSpeed()).thenReturn(this.speed);
        Mockito.when(this.mockGTU.getAcceleration()).thenReturn(this.acceleration);
        Mockito.when(this.mockGTU.getId()).thenReturn(this.name);
    }

    /**
     * @return mocked GTU
     */
    public LaneBasedGtu getMock()
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

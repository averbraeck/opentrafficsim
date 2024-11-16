package org.opentrafficsim.sim0mq.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventProducer;
import org.djutils.event.EventType;
import org.djutils.event.TimedEvent;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentrafficsim.base.geometry.OtsGeometryException;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsReplication;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneGeometryUtil;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.StripeType;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.sim0mq.Sim0MQException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Unit tests.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
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

        RoadNetwork network = new RoadNetwork("test network for TransceiverTest", simulator);
        GtuIdTransceiver gtuIdTransceiver = new GtuIdTransceiver(network);
        assertEquals("GTU id transceiver", gtuIdTransceiver.getId(), "getId returns correct id");
        assertEquals(0, gtuIdTransceiver.getAddressFields().size(), "address has 0 entries");
        assertEquals(1, gtuIdTransceiver.getResultFields().size(), "result has one field");
        assertEquals(String[].class, gtuIdTransceiver.getResultFields().getObjectClass(0),
                "type of the result field is String");
        assertEquals("String array filled with all currently valid GTU ids",
                gtuIdTransceiver.getResultFields().getObjectDescription(0), "description of the result field");
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
        assertNotNull(result, "result should not be null");
        assertEquals(0, result.length, "length of result should be 0");
        assertNull(checkAckNack(gtuIdTransceiver, new Object[] {"this is a bad address"}, false, "wrong length"),
                "Bad address");

        GtuType gtuType = new GtuType("gtuType 1");
        LaneBasedGtu gtu1 = new MyMockGTU("gtu 1", gtuType, new OrientedPoint2d(1, 10, 1), new Speed(1, SpeedUnit.KM_PER_HOUR),
                new Acceleration(1, AccelerationUnit.METER_PER_SECOND_2), simulator).getMock();
        network.addGTU(gtu1);
        result = gtuIdTransceiver.get(null, storeLastResult);
        assertEquals(1, result.length, "length of result is now 1");
        assertTrue(result[0] instanceof String, "result contains a string");
        assertEquals("gtu 1", result[0], "result[0] is name of our mocked GTU");
        LaneBasedGtu gtu2 = new MyMockGTU("gtu 2", gtuType, new OrientedPoint2d(2, 20, 2), new Speed(2, SpeedUnit.KM_PER_HOUR),
                new Acceleration(2, AccelerationUnit.METER_PER_SECOND_2), simulator).getMock();
        network.addGTU(gtu2);
        result = gtuIdTransceiver.get(new Object[0], storeLastResult);
        assertEquals(2, result.length, "length of result is now 2");
        for (int i = 0; i < 2; i++)
        {
            assertTrue(result[i] instanceof String, "result contains a string");
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
            assertEquals(1, count, "found gtu i once");
        }
        // Make the GtuTransceiver
        GtuTransceiver gtuTransceiver = new GtuTransceiver(network, gtuIdTransceiver);
        assertEquals("GTU transceiver", gtuTransceiver.getId(), "GtuTransceiver returns correct id");
        assertEquals(gtuIdTransceiver, gtuTransceiver.getIdSource(0, null), "getIdSource returns gtuIdTransceiver");
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

        assertEquals("GTU id", gtuTransceiver.getAddressFields().getObjectDescription(0), "address field 0");
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

        assertEquals(String.class, gtuTransceiver.getAddressFields().getObjectClass(0), "address field class");
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
            assertNotNull(gtuResult, "result is not null");
            assertEquals(6, gtuResult.length, "result has 6 fields");
            assertEquals(String.class, gtuResult[0].getClass(), "first field is a String");
            assertEquals(result[i], gtuResult[0], "gtuResult is gtu with expected id");
            LaneBasedGtu gtu = (LaneBasedGtu) network.getGTU(((String) gtuResult[0]));
            assertNotNull(gtu, "GTU is in the network");
            assertTrue(gtuResult[1] instanceof String, "field 1 is id of a GtuType");
            assertEquals(gtuType.getId(), gtuResult[1], "gtu type matches");
            assertEquals(gtu.getLocation().x, ((PositionVector) gtuResult[2]).get(0).si, 0.0000, "x matches");
            assertEquals(gtu.getLocation().y, ((PositionVector) gtuResult[2]).get(1).si, 0.0000, "y matches");
            assertEquals(new Direction(gtu.getLocation().getDirZ(), DirectionUnit.EAST_DEGREE).si,
                    ((Direction) gtuResult[3]).si, 0.0001, "direction matches");
            assertEquals(gtu.getSpeed(), gtuResult[4], "speed");
            assertEquals(gtu.getAcceleration(), gtuResult[5], "acceleration");
        }
        assertNull(gtuTransceiver.get(new Object[] {"NONEXISTENTGTU"}, storeLastResult),
                "gtuTransceiver returns null for non-existend ID");
        gtuTransceiver.get(new Object[] {123}, storeLastResult);
        assertTrue(gtuTransceiver.toString().contains("Transceiver"), "toString returns something descriptive");

        NodeIdTransceiver nit = new NodeIdTransceiver(network);
        assertTrue(nit.toString().startsWith("NodeIdTransceiver"),
                "toString of node id transceiver returns something descriptive");

        LinkIdTransceiver lit = new LinkIdTransceiver(network);
        assertTrue(lit.toString().startsWith("LinkIdTransceiver"),
                "toString of link id transceiver returns something descriptive");

        // Give the network two nodes and a link with a lane - A lot of code is required to create a lane :-(
        Point2d node1Point = new Point2d(10, 20);
        Node node1 = new Node(network, "node 1", node1Point, Direction.ZERO);
        Node node2 = new Node(network, "node 2", new Point2d(110, 20), Direction.ZERO);
        LinkType roadLinkType = DefaultsNl.ROAD;
        CrossSectionLink link = new CrossSectionLink(network, "1 to 2", node1, node2, roadLinkType,
                new OtsLine2d(node1.getPoint(), node2.getPoint()), null, LaneKeepingPolicy.KEEPRIGHT);
        LaneType laneType = DefaultsRoadNl.RESIDENTIAL_ROAD;
        OtsReplication replication = Mockito.mock(OtsReplication.class);
        HistoryManagerDevs hmd = Mockito.mock(HistoryManagerDevs.class);
        Mockito.when(hmd.now()).thenReturn(Time.ZERO);
        Mockito.when(replication.getHistoryManager(simulator)).thenReturn(hmd);
        Mockito.when(simulator.getReplication()).thenReturn(replication);
        Lane lane = LaneGeometryUtil.createStraightLane(link, "lane", Length.ZERO, new Length(3, LengthUnit.METER), laneType,
                Map.of(DefaultsNl.VEHICLE, new Speed(50, SpeedUnit.KM_PER_HOUR)));
        Length width = new Length(20, LengthUnit.DECIMETER);
        Stripe stripe = LaneGeometryUtil.createStraightStripe(StripeType.DASHED, link, Length.ZERO, width);
        String stripeId = stripe.getId();

        LinkGtuIdTransceiver linkgit = new LinkGtuIdTransceiver(network);
        assertTrue(linkgit.toString().startsWith("LinkGtuIdTransceiver"),
                "toString of LinkGtuIdTransceiver returns something descriptive");
        assertFalse(linkgit.hasIdSource(), "LinkGtuIdTransceiver does not have an id source");

        assertNull(checkAckNack(linkgit, new Object[] {"bad", "address"}, false, "need id of a link"), "Bad address");
        assertNull(checkAckNack(linkgit, new Object[] {"Non existing link"}, false, "Network does not contain a link with id"),
                "Non existing link");

        this.lastAckNack = null;
        result = linkgit.get(new Object[] {"1 to 2"}, storeLastResult);
        assertNotNull(result);
        assertEquals(0, result.length, "result is empty array");
        assertNull(this.lastAckNack);

        LaneGtuIdTransceiver lanegit = new LaneGtuIdTransceiver(network);
        assertTrue(lanegit.toString().startsWith("LaneGtuIdTransceiver"),
                "toString of LaneGtuIdTransceiver returns something descriptive");
        assertFalse(lanegit.hasIdSource(), "LaneGtuIdTransceiver does not have an Id source");

        assertNull(checkAckNack(lanegit, new Object[] {"this", "is", "a", "bad", "address"}, false,
                "need id of a link and id of a CrossSectionElement"), "Bad address");
        assertNull(checkAckNack(lanegit, new Object[] {"Non existing link", "Non existing lane"}, false,
                "Network does not contain a link with id"), "Non existing link");
        assertNull(checkAckNack(lanegit, new Object[] {"1 to 2", "Non existing lane"}, false,
                "does not contain a cross section element with id"), "Existing link but non existing lane");
        assertNull(checkAckNack(lanegit, new Object[] {"1 to 2", stripeId}, false, "is not a lane"),
                "Existing link, but non a lane");

        this.lastAckNack = null;
        result = lanegit.get(new Object[] {"1 to 2", "lane"}, storeLastResult);
        assertNull(this.lastAckNack, "Existing link and lane should not have sent a NACK or ACK");
        assertEquals(0, result.length, "Existing link and lane should have sent empty array");

        // Put one of the GTUs on the lane
        lane.addGtu(gtu1, 0.3);

        this.lastAckNack = null;
        result = linkgit.get(new Object[] {"1 to 2"}, storeLastResult);
        assertNotNull(result);
        assertEquals(1, result.length, "result is array with one entry");
        assertEquals(gtu1.getId(), result[0], "content of entry is id of gtu1");
        assertNull(this.lastAckNack);

        result = lanegit.get(new Object[] {"1 to 2", "lane"}, storeLastResult);
        assertNull(this.lastAckNack, "Existing link and lane should not have sent a NACK or ACK");
        assertEquals(1, result.length, "Existing link and lane should have sent empty array");
        assertEquals(gtu1.getId(), result[0], "content of entry is id of gtu1");
        assertNull(this.lastAckNack);

        Mockito.when(simulator.isInitialized()).thenReturn(false);
        SimulatorStateTransceiver sst = new SimulatorStateTransceiver(simulator);
        result = sst.get(null, storeLastResult);
        assertEquals(1, result.length, "get returned one element Object array");
        assertEquals("Not (yet) initialized", result[0], "Mock simulator pretends not to have been initialized");
        Mockito.when(simulator.isInitialized()).thenReturn(true);
        // Next statement is not really needed; just making sure
        Mockito.when(simulator.isStartingOrRunning()).thenReturn(false);
        result = sst.get(null, storeLastResult);
        assertEquals(1, result.length, "get returned one element Object array");
        assertEquals("Stopping or stopped", result[0], "Mock simulator pretends be in stopped state");
        Mockito.when(simulator.isStartingOrRunning()).thenReturn(true);
        result = sst.get(null, storeLastResult);
        assertEquals(1, result.length, "get returned one element Object array");
        assertEquals("Starting or running", result[0], "Mock simulator pretends be in stopped state");
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
        assertEquals(123.0, this.lastTime.si, 0, "last time is 123");
        tev = new TimedEvent<>(SimulatorInterface.STOP_EVENT, null, new Time(1234, TimeUnit.BASE_SECOND));
        this.lastContent = null;
        ((EventListener) epi).notify(tev);
        assertEquals(Boolean.FALSE, this.lastContent, "lastContent is now true");
        assertEquals(1234.0, this.lastTime.si, 0, "last time is 1234");

        this.lastAckNack = null; // make sure we can see that is has been set
        assertNull(lepi.lookup(new Object[] {"This is a bad address"}, storeLastResult), "using a bad address returns null");
        assertEquals(Boolean.FALSE, this.lastAckNack, "using a bad address sends a NACK");
        assertTrue(((String) this.lastPayload[0]).contains("wrong length"), "NACK message contains \"wrong length\"");

        NodeTransceiver nt = new NodeTransceiver(network, nit);
        assertTrue(nt.toString().startsWith("NodeTransceiver"), "toString of NodeTransceiver returns something descriptive");
        assertTrue(nt.hasIdSource(), "NodeTransceiver has a Id source");
        assertEquals(nit, nt.getIdSource(0, storeLastResult), "NodeTransceiver returns NodeIdTransceiver at level 0");

        this.lastAckNack = null; // make sure we can see that is has been set
        assertNull(nt.getIdSource(1, storeLastResult), "Bad address level returns null");
        assertEquals(Boolean.FALSE, this.lastAckNack, "Bad address sent a NACK");
        assertEquals("Only empty address is valid", this.lastPayload[0], "Bad address NACK describes the problem");

        assertNull(checkAckNack(nt, null, false, "Address may not be null"), "Bad address");
        assertNull(checkAckNack(nt, new Object[] {"Non existing node"}, false,
                "Network does not contain a node with id Non existing node"), "Bad address");

        this.lastAckNack = null;
        result = nt.get(new Object[] {node1.getId()}, storeLastResult);
        assertEquals(4, result.length, "result contains 3 fields");
        assertEquals(node1.getId(), result[0], "field 0 is node id");
        assertTrue(result[1] instanceof PositionVector, "field 1 is a position vector");
        PositionVector pv = (PositionVector) result[1];
        assertEquals(2, pv.size(), "Position vector size is 2");
        assertEquals(node1Point.x, pv.get(0).si, 0, "x matches");
        assertEquals(node1Point.y, pv.get(1).si, 0, "y matches");
        assertEquals(Direction.ZERO, result[2], "direction matches");
        assertEquals(1, result[3], "Number of links is 1");

        this.lastAckNack = null;
        LinkTransceiver lt = new LinkTransceiver(network, lit);
        assertTrue(lt.toString().startsWith("LinkTransceiver"), "toString returns something descriptive");
        assertEquals(lit, lt.getIdSource(0, storeLastResult), "LinkTransceiver can return LinkIdTransceiver");
        assertNull(this.lastAckNack, "No ACK or NACK received");

        assertNull(lt.getIdSource(1, storeLastResult), "Bad address level returns null");
        assertEquals(Boolean.FALSE, this.lastAckNack, "Bad address level sent a NACK");
        assertEquals("Only empty address is valid", this.lastPayload[0], "Message of NACK describes the problem");

        assertNull(checkAckNack(lt, null, false, "Address may not be null"), "Bad address");
        assertNull(checkAckNack(lt, new Object[] {}, false, "has wrong length"), "Bad address");
        assertNull(checkAckNack(lt, new Object[] {"Non existing link name"}, false, "Network does not contain a link with id"),
                "Non existing link name");

        this.lastAckNack = null;
        result = lt.get(new Object[] {"1 to 2"}, storeLastResult);
        assertNull(this.lastAckNack, "No ACK or NACK");
        assertEquals(7, result.length, "result contains 7 elements");
        assertEquals(link.getId(), result[0], "result is our link");
        assertEquals(DefaultsNl.ROAD.getId(), result[1], "link type is ROAD");
        assertEquals(node1.getId(), result[2], "from node");
        assertEquals(node2.getId(), result[3], "to node");
        assertEquals(link.getDesignLine().size(), result[4], "number of points in design line is 2");
        assertEquals(link.getGTUCount(), result[5], "number of GTUs on link");
        assertEquals(link.getCrossSectionElementList().size(), result[6], "number of cross section elements");

        CrossSectionElementTransceiver cset = new CrossSectionElementTransceiver(network);
        assertTrue(cset.toString().startsWith("CrossSectionElementTransceiver"), "toString returns something descriptive");

        assertNull(checkAckNack(cset, null, false, "Address may not be null"), "Bad address");
        assertNull(
                checkAckNack(cset, new Object[] {"bad", "address", "has", "too", "many", "fields"}, false, "has wrong length"),
                "Bad address");
        assertNull(checkAckNack(cset, new Object[] {"1 to 2", -1}, false, "valid range is"), "Bad address");
        assertNull(checkAckNack(cset, new Object[] {"NON EXISTENT LINK", 0}, false, "Network does not contain a link with id"),
                "Bad address");
        assertNull(checkAckNack(cset, new Object[] {"1 to 2", 2}, false, "valid range is"), "Bad address");

        this.lastAckNack = null;
        result = cset.get(new Object[] {"1 to 2", 0}, storeLastResult);
        assertNull(this.lastAckNack, "No NACK (or ACK)");
        assertEquals(7, result.length, "result contains 7 elements");
        assertEquals(lane.getId(), result[0], "id");
        assertEquals(lane.getClass().getName(), result[1], "class name");
        assertEquals(lane.getLength(), result[2], "length");
        assertEquals(lane.getWidth(0.0), result[3], "width at begin");
        assertEquals(lane.getOffsetAtBegin(), result[4], "design line offset at begin");
        assertEquals(lane.getWidth(1.0), result[5], "width at end");
        assertEquals(lane.getOffsetAtEnd(), result[6], "design line offset at end");

    }

    /**
     * Call the get method of a TransceiverInterface and verify most of the results.
     * @param transceiver the transceiver to test
     * @param address the argument of the get method of the transceiver
     * @param expectedAckNack null if neither an ACK nor a NACK is expected, Boolean.FALSE if a NACK is expected, Boolean.TRUE
     *            if an ACK is expected
     * @param expectedInPayload text that should occur in the payload of the ACK or NACK, or null if no ACK or NACK is expected
     * @return the result of the get method should be null if an ACK or a NACK was received; non-null otherwise
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
                assertEquals(expectedAckNack, ackNack, "ACK/NACK");
                if (null != ackNack)
                {
                    assertNotNull(payload, "ACK or NACK should have a payload");
                    assertNotNull(payload, "payload with ACK or NACK should not be null");
                    assertEquals(1, payload.length, "payload has one element");
                    assertTrue(payload[0] instanceof String, "the single field of the payload is a string");
                    assertTrue(((String) payload[0]).contains(expectedInPayload), "payload should contain expected text");
                }
                assertNotNull(payload, "payload should not be null");
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
        assertNull(AbstractTransceiver.verifyMetaData(MetaData.NO_META_DATA, null), "NO_META_DATA allows anything");
        assertNull(AbstractTransceiver.verifyMetaData(MetaData.NO_META_DATA, new Object[] {}), "NO_META_DATA allows anything");
        assertNull(AbstractTransceiver.verifyMetaData(MetaData.NO_META_DATA, new Object[] {"Anything goes"}),
                "NO_META_DATA allows anything");

        MetaData md = new MetaData("A", "a", new ObjectDescriptor[] {new ObjectDescriptor("String", "string", String.class),
                new ObjectDescriptor("Double", "double", Double.class)});
        assertEquals("Address may not be null", AbstractTransceiver.verifyMetaData(md, null), "empty is not ok");
        assertTrue(AbstractTransceiver.verifyMetaData(md, new Object[] {}).contains("has wrong length"), "wrong length");
        assertTrue(AbstractTransceiver.verifyMetaData(md, new Object[] {123.456, 234.567}).contains("cannot be used for"),
                "wrong type");
        assertNull(AbstractTransceiver.verifyMetaData(md, new Object[] {"hello", 234.567}), "Good address returns null");
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
    private final OrientedPoint2d location;

    /** speed. */
    private final Speed speed;

    /** acceleration. */
    private final Acceleration acceleration;

    /** mocked simulator. */
    private final OtsSimulatorInterface simulator;

    /**
     * @param name the name of the mocked GTU
     * @param gtuType the GTU type
     * @param location the location of the mocked GTU
     * @param speed the speed of the mocked GTU
     * @param acceleration the acceleration of the mocked GTU
     * @param simulator (mocked) simulator
     * @throws RemoteException cannot happen ...
     */
    MyMockGTU(final String name, final GtuType gtuType, final OrientedPoint2d location, final Speed speed,
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
     * @param payload the payload
     */
    NoTransceiver(final String payload)
    {
        this.payload = payload;
    }

    /**
     * Retrieve the payload.
     * @return the payload
     */
    public String getPayload()
    {
        return this.payload;
    }
}

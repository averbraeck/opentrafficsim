package org.opentrafficsim.road.network.lane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.Mockito;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsPoint3d;
import org.opentrafficsim.core.geometry.OtsShape;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.mock.MockDevsSimulator;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;
import org.opentrafficsim.road.network.lane.object.detector.LaneDetector;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the Lane class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LaneTest implements UNITS
{
    /**
     * Test the constructor.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void laneConstructorTest() throws Exception
    {
        OtsSimulatorInterface simulator = new OtsSimulator("LaneTest");
        RoadNetwork network = new RoadNetwork("lane test network", simulator);
        Model model = new Model(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model);
        // First we need two Nodes
        Node nodeFrom = new Node(network, "A", new OtsPoint3d(0, 0, 0), Direction.ZERO);
        Node nodeTo = new Node(network, "B", new OtsPoint3d(1000, 0, 0), Direction.ZERO);
        // Now we can make a Link
        OtsPoint3d[] coordinates = new OtsPoint3d[2];
        coordinates[0] = new OtsPoint3d(nodeFrom.getPoint().x, nodeFrom.getPoint().y, 0);
        coordinates[1] = new OtsPoint3d(nodeTo.getPoint().x, nodeTo.getPoint().y, 0);
        CrossSectionLink link = new CrossSectionLink(network, "A to B", nodeFrom, nodeTo, DefaultsNl.FREEWAY,
                new OtsLine3d(coordinates), LaneKeepingPolicy.KEEPRIGHT);
        Length startLateralPos = new Length(2, METER);
        Length endLateralPos = new Length(5, METER);
        Length startWidth = new Length(3, METER);
        Length endWidth = new Length(4, METER);
        GtuType gtuTypeCar = DefaultsNl.CAR;

        LaneType laneType = new LaneType("One way", DefaultsRoadNl.FREEWAY);
        laneType.addCompatibleGtuType(DefaultsNl.VEHICLE);
        Map<GtuType, Speed> speedMap = new LinkedHashMap<>();
        speedMap.put(DefaultsNl.VEHICLE, new Speed(100, KM_PER_HOUR));
        // Now we can construct a Lane
        // FIXME what overtaking conditions do we want to test in this unit test?
        Lane lane = LaneGeometryUtil.createStraightLane(link, "lane", startLateralPos, endLateralPos, startWidth, endWidth,
                laneType, speedMap);
        // Verify the easy bits
        assertEquals("Link returns network", network, link.getNetwork());
        assertEquals("Lane returns network", network, lane.getNetwork());
        assertEquals("PrevLanes should be empty", 0, lane.prevLanes(gtuTypeCar).size()); // this one caught a bug!
        assertEquals("NextLanes should be empty", 0, lane.nextLanes(gtuTypeCar).size());
        double approximateLengthOfContour =
                2 * nodeFrom.getPoint().distanceSI(nodeTo.getPoint()) + startWidth.getSI() + endWidth.getSI();
        assertEquals("Length of contour is approximately " + approximateLengthOfContour, approximateLengthOfContour,
                lane.getContour().getLengthSI(), 0.1);
        assertEquals("SpeedLimit should be " + (new Speed(100, KM_PER_HOUR)), new Speed(100, KM_PER_HOUR),
                lane.getSpeedLimit(DefaultsNl.VEHICLE));
        assertEquals("There should be no GTUs on the lane", 0, lane.getGtuList().size());
        assertEquals("LaneType should be " + laneType, laneType, lane.getType());
        // TODO: This test for expectedLateralCenterOffset fails
        for (int i = 0; i < 10; i++)
        {
            double expectedLateralCenterOffset =
                    startLateralPos.getSI() + (endLateralPos.getSI() - startLateralPos.getSI()) * i / 10;
            assertEquals(String.format("Lateral offset at %d%% should be %.3fm", 10 * i, expectedLateralCenterOffset),
                    expectedLateralCenterOffset, lane.getLateralCenterPosition(i / 10.0).getSI(), 0.01);
            Length longitudinalPosition = new Length(lane.getLength().getSI() * i / 10, METER);
            assertEquals("Lateral offset at " + longitudinalPosition + " should be " + expectedLateralCenterOffset,
                    expectedLateralCenterOffset, lane.getLateralCenterPosition(longitudinalPosition).getSI(), 0.01);
            double expectedWidth = startWidth.getSI() + (endWidth.getSI() - startWidth.getSI()) * i / 10;
            assertEquals(String.format("Width at %d%% should be %.3fm", 10 * i, expectedWidth), expectedWidth,
                    lane.getWidth(i / 10.0).getSI(), 0.0001);
            assertEquals("Width at " + longitudinalPosition + " should be " + expectedWidth, expectedWidth,
                    lane.getWidth(longitudinalPosition).getSI(), 0.0001);
            double expectedLeftOffset = expectedLateralCenterOffset - expectedWidth / 2;
            // The next test caught a bug
            assertEquals(String.format("Left edge at %d%% should be %.3fm", 10 * i, expectedLeftOffset), expectedLeftOffset,
                    lane.getLateralBoundaryPosition(LateralDirectionality.LEFT, i / 10.0).getSI(), 0.001);
            assertEquals("Left edge at " + longitudinalPosition + " should be " + expectedLeftOffset, expectedLeftOffset,
                    lane.getLateralBoundaryPosition(LateralDirectionality.LEFT, longitudinalPosition).getSI(), 0.001);
            double expectedRightOffset = expectedLateralCenterOffset + expectedWidth / 2;
            assertEquals(String.format("Right edge at %d%% should be %.3fm", 10 * i, expectedRightOffset), expectedRightOffset,
                    lane.getLateralBoundaryPosition(LateralDirectionality.RIGHT, i / 10.0).getSI(), 0.001);
            assertEquals("Right edge at " + longitudinalPosition + " should be " + expectedRightOffset, expectedRightOffset,
                    lane.getLateralBoundaryPosition(LateralDirectionality.RIGHT, longitudinalPosition).getSI(), 0.001);
        }

        // Harder case; create a Link with form points along the way
        // System.out.println("Constructing Link and Lane with one form point");
        coordinates = new OtsPoint3d[3];
        coordinates[0] = new OtsPoint3d(nodeFrom.getPoint().x, nodeFrom.getPoint().y, 0);
        coordinates[1] = new OtsPoint3d(200, 100);
        coordinates[2] = new OtsPoint3d(nodeTo.getPoint().x, nodeTo.getPoint().y, 0);
        link = new CrossSectionLink(network, "A to B with Kink", nodeFrom, nodeTo, DefaultsNl.FREEWAY,
                new OtsLine3d(coordinates), LaneKeepingPolicy.KEEPRIGHT);
        lane = LaneGeometryUtil.createStraightLane(link, "lane.1", startLateralPos, endLateralPos, startWidth, endWidth,
                laneType, speedMap);
        // Verify the easy bits

        // XXX: This is not correct...
        /*-
        assertEquals("PrevLanes should contain one lane from the other link", 1, lane.prevLanes(gtuTypeCar).size());
        assertEquals("NextLanes should contain one lane from the other link", 1, lane.nextLanes(gtuTypeCar).size());
        approximateLengthOfContour = 2 * (coordinates[0].distanceSI(coordinates[1]) + coordinates[1].distanceSI(coordinates[2]))
                + startWidth.getSI() + endWidth.getSI();
        // System.out.println("contour of lane is " + lane.getContour());
        // System.out.println(lane.getContour().toPlot());
        assertEquals("Length of contour is approximately " + approximateLengthOfContour, approximateLengthOfContour,
                lane.getContour().getLengthSI(), 4); // This lane takes a path that is about 3m longer than the design line
        assertEquals("There should be no GTUs on the lane", 0, lane.getGtuList().size());
        assertEquals("LaneType should be " + laneType, laneType, lane.getType());
        // System.out.println("Add another Lane at the inside of the corner in the design line");
        Length startLateralPos2 = new Length(-8, METER);
        Length endLateralPos2 = new Length(-5, METER);
        Lane lane2 =
                new Lane(link, "lane.2", startLateralPos2, endLateralPos2, startWidth, endWidth, laneType, speedMap, false);
        // Verify the easy bits
        assertEquals("PrevLanes should be empty", 0, lane2.prevLanes(gtuTypeCar).size());
        assertEquals("NextLanes should be empty", 0, lane2.nextLanes(gtuTypeCar).size());
        approximateLengthOfContour = 2 * (coordinates[0].distanceSI(coordinates[1]) + coordinates[1].distanceSI(coordinates[2]))
                + startWidth.getSI() + endWidth.getSI();
        assertEquals("Length of contour is approximately " + approximateLengthOfContour, approximateLengthOfContour,
                lane2.getContour().getLengthSI(), 12); // This lane takes a path that is about 11 meters shorter
        assertEquals("There should be no GTUs on the lane", 0, lane2.getGtuList().size());
        assertEquals("LaneType should be " + laneType, laneType, lane2.getType());
        */

        // Construct a lane using CrossSectionSlices
        OtsLine3d centerLine = new OtsLine3d(new OtsPoint3d(0.0, 0.0, 0.0), new OtsPoint3d(100.0, 0.0, 0.0));
        OtsShape contour = new OtsShape(new OtsPoint3d(0.0, -1.75, 0.0), new OtsPoint3d(100.0, -1.75, 0.0),
                new OtsPoint3d(100.0, 1.75, 0.0), new OtsPoint3d(0.0, -1.75, 0.0));
        try
        {
            new Lane(link, "lanex", centerLine, contour, null, laneType, speedMap);
            fail("null pointer for CrossSectionSlices should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        List<CrossSectionSlice> crossSectionSlices = new ArrayList<>();
        try
        {
            new Lane(link, "lanex", centerLine, contour, crossSectionSlices, laneType, speedMap);
            fail("empty CrossSectionSlices should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        crossSectionSlices.add(new CrossSectionSlice(Length.ZERO, startLateralPos, startWidth));
        lane = new Lane(link, "lanex", centerLine, contour, crossSectionSlices, laneType, speedMap);
        sensorTest(lane);
    }

    /**
     * Add/Remove some sensor to/from a lane and see if the expected events occur.
     * @param lane Lane; the lane to manipulate
     * @throws NetworkException when this happens uncaught; this test has failed
     */
    public final void sensorTest(final Lane lane) throws NetworkException
    {
        assertEquals("List of sensor is initially empty", 0, lane.getDetectors().size());
        Listener listener = new Listener();
        double length = lane.getLength().si;
        lane.addListener(listener, Lane.DETECTOR_ADD_EVENT);
        lane.addListener(listener, Lane.DETECTOR_REMOVE_EVENT);
        assertEquals("event list is initially empty", 0, listener.events.size());
        LaneDetector sensor1 = new MockSensor("sensor1", Length.instantiateSI(length / 4)).getMock();
        lane.addDetector(sensor1);
        assertEquals("event list now contains one event", 1, listener.events.size());
        assertEquals("event indicates that a sensor got added", listener.events.get(0).getType(), Lane.DETECTOR_ADD_EVENT);
        assertEquals("lane now contains one sensor", 1, lane.getDetectors().size());
        assertEquals("sensor on lane is sensor1", sensor1, lane.getDetectors().get(0));
        LaneDetector sensor2 = new MockSensor("sensor2", Length.instantiateSI(length / 2)).getMock();
        lane.addDetector(sensor2);
        assertEquals("event list now contains two events", 2, listener.events.size());
        assertEquals("event indicates that a sensor got added", listener.events.get(1).getType(), Lane.DETECTOR_ADD_EVENT);
        List<LaneDetector> sensors = lane.getDetectors();
        assertEquals("lane now contains two sensors", 2, sensors.size());
        assertTrue("sensor list contains sensor1", sensors.contains(sensor1));
        assertTrue("sensor list contains sensor2", sensors.contains(sensor2));
        sensors = lane.getDetectors(Length.ZERO, Length.instantiateSI(length / 3), DefaultsNl.VEHICLE);
        assertEquals("first third of lane contains 1 sensor", 1, sensors.size());
        assertTrue("sensor list contains sensor1", sensors.contains(sensor1));
        sensors = lane.getDetectors(Length.instantiateSI(length / 3), Length.instantiateSI(length), DefaultsNl.VEHICLE);
        assertEquals("last two-thirds of lane contains 1 sensor", 1, sensors.size());
        assertTrue("sensor list contains sensor2", sensors.contains(sensor2));
        sensors = lane.getDetectors(DefaultsNl.VEHICLE);
        // NB. The mocked sensor is compatible with all GTU types in all directions.
        assertEquals("sensor list contains two sensors", 2, sensors.size());
        assertTrue("sensor list contains sensor1", sensors.contains(sensor1));
        assertTrue("sensor list contains sensor2", sensors.contains(sensor2));
        sensors = lane.getDetectors(DefaultsNl.VEHICLE);
        // NB. The mocked sensor is compatible with all GTU types in all directions.
        assertEquals("sensor list contains two sensors", 2, sensors.size());
        assertTrue("sensor list contains sensor1", sensors.contains(sensor1));
        assertTrue("sensor list contains sensor2", sensors.contains(sensor2));
        SortedMap<Double, List<LaneDetector>> sensorMap = lane.getDetectorMap(DefaultsNl.VEHICLE);
        assertEquals("sensor map contains two entries", 2, sensorMap.size());
        for (Double d : sensorMap.keySet())
        {
            List<LaneDetector> sensorsAtD = sensorMap.get(d);
            assertEquals("There is one sensor at position d", 1, sensorsAtD.size());
            assertEquals("Sensor map contains the correct sensor at the correct distance", d < length / 3 ? sensor1 : sensor2,
                    sensorsAtD.get(0));
        }

        lane.removeDetector(sensor1);
        assertEquals("event list now contains three events", 3, listener.events.size());
        assertEquals("event indicates that a sensor got removed", listener.events.get(2).getType(), Lane.DETECTOR_REMOVE_EVENT);
        sensors = lane.getDetectors();
        assertEquals("lane now contains one sensor", 1, sensors.size());
        assertTrue("sensor list contains sensor2", sensors.contains(sensor2));
        try
        {
            lane.removeDetector(sensor1);
            fail("Removing a sensor twice should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            lane.addDetector(sensor2);
            fail("Adding a sensor twice should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        LaneDetector badSensor = new MockSensor("sensor3", Length.instantiateSI(-0.1)).getMock();
        try
        {
            lane.addDetector(badSensor);
            fail("Adding a sensor at negative position should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        badSensor = new MockSensor("sensor4", Length.instantiateSI(length + 0.1)).getMock();
        try
        {
            lane.addDetector(badSensor);
            fail("Adding a sensor at position beyond the end of the lane should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        lane.removeDetector(sensor2);
        List<LaneBasedObject> lboList = lane.getLaneBasedObjects();
        assertEquals("lane initially contains zero lane based objects", 0, lboList.size());
        LaneBasedObject lbo1 = new MockLaneBasedObject("lbo1", Length.instantiateSI(length / 4)).getMock();
        listener.getEvents().clear();
        lane.addListener(listener, Lane.OBJECT_ADD_EVENT);
        lane.addListener(listener, Lane.OBJECT_REMOVE_EVENT);
        lane.addLaneBasedObject(lbo1);
        assertEquals("adding a lane based object cause the lane to emit an event", 1, listener.getEvents().size());
        assertEquals("The emitted event was a OBJECT_ADD_EVENT", Lane.OBJECT_ADD_EVENT, listener.getEvents().get(0).getType());
        LaneBasedObject lbo2 = new MockLaneBasedObject("lbo2", Length.instantiateSI(3 * length / 4)).getMock();
        lane.addLaneBasedObject(lbo2);
        lboList = lane.getLaneBasedObjects();
        assertEquals("lane based object list now contains two objects", 2, lboList.size());
        assertTrue("lane base object list contains lbo1", lboList.contains(lbo1));
        assertTrue("lane base object list contains lbo2", lboList.contains(lbo2));
        lboList = lane.getLaneBasedObjects(Length.ZERO, Length.instantiateSI(length / 2));
        assertEquals("first half of lane contains one object", 1, lboList.size());
        assertEquals("object in first haf of lane is lbo1", lbo1, lboList.get(0));
        lboList = lane.getLaneBasedObjects(Length.instantiateSI(length / 2), Length.instantiateSI(length));
        assertEquals("second half of lane contains one object", 1, lboList.size());
        assertEquals("object in second haf of lane is lbo2", lbo2, lboList.get(0));
        SortedMap<Double, List<LaneBasedObject>> sortedMap = lane.getLaneBasedObjectMap();
        assertEquals("sorted map contains two objects", 2, sortedMap.size());
        for (Double d : sortedMap.keySet())
        {
            List<LaneBasedObject> objectsAtD = sortedMap.get(d);
            assertEquals("There is one object at position d", 1, objectsAtD.size());
            assertEquals("Object at position d is the expected one", d < length / 2 ? lbo1 : lbo2, objectsAtD.get(0));
        }

        for (double fraction : new double[] {-0.5, 0, 0.2, 0.5, 0.9, 1.0, 2})
        {
            double positionSI = length * fraction;
            double fractionSI = lane.fractionSI(positionSI);
            assertEquals("fractionSI matches fraction", fraction, fractionSI, 0.0001);

            LaneBasedObject nextObject = positionSI < lbo1.getLongitudinalPosition().si ? lbo1
                    : positionSI < lbo2.getLongitudinalPosition().si ? lbo2 : null;
            List<LaneBasedObject> expected = null;
            if (null != nextObject)
            {
                expected = new ArrayList<>();
                expected.add(nextObject);
            }
            List<LaneBasedObject> got = lane.getObjectAhead(Length.instantiateSI(positionSI));
            assertEquals("First bunch of objects ahead of d", expected, got);

            nextObject = positionSI > lbo2.getLongitudinalPosition().si ? lbo2
                    : positionSI > lbo1.getLongitudinalPosition().si ? lbo1 : null;
            expected = null;
            if (null != nextObject)
            {
                expected = new ArrayList<>();
                expected.add(nextObject);
            }
            got = lane.getObjectBehind(Length.instantiateSI(positionSI));
            assertEquals("First bunch of objects behind d", expected, got);
        }

        lane.removeLaneBasedObject(lbo1);
        assertEquals("removing a lane based object caused the lane to emit an event", 3, listener.getEvents().size());
        assertEquals("removing a lane based object caused the lane to emit OBJECT_REMOVE_EVENT", Lane.OBJECT_REMOVE_EVENT,
                listener.getEvents().get(2).getType());
        try
        {
            lane.removeLaneBasedObject(lbo1);
            fail("Removing a lane bases object that was already removed should have caused a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            lane.addLaneBasedObject(lbo2);
            fail("Adding a lane base object that was already added should have caused a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        LaneBasedObject badLBO = new MockLaneBasedObject("badLBO", Length.instantiateSI(-0.1)).getMock();
        try
        {
            lane.addLaneBasedObject(badLBO);
            fail("Adding a lane based object at negative position should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        badLBO = new MockLaneBasedObject("badLBO", Length.instantiateSI(length + 0.1)).getMock();
        try
        {
            lane.addLaneBasedObject(badLBO);
            fail("Adding a lane based object at position beyond end of lane should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Simple event listener that collects events in a list.
     */
    class Listener implements EventListener
    {
        /** Collect the received events. */
        private List<Event> events = new ArrayList<>();

        @Override
        public void notify(final Event event) throws RemoteException
        {
            this.events.add(event);
        }

        /**
         * Retrieve the collected events.
         * @return List&lt;EventInterface&gt;; the events
         */
        public List<Event> getEvents()
        {
            return this.events;
        }

    }

    /**
     * Mock a Detector.
     */
    class MockSensor
    {
        /** The mocked sensor. */
        private final LaneDetector mockSensor;

        /** Id of the mocked sensor. */
        private final String id;

        /** The position along the lane of the sensor. */
        private final Length position;

        /** Faked simulator. */
        private final OtsSimulatorInterface simulator = MockDevsSimulator.createMock();

        /**
         * Construct a new Mocked Detector.
         * @param id String; result of the getId() method of the mocked Detector
         * @param position Length; result of the getLongitudinalPosition of the mocked Detector
         */
        MockSensor(final String id, final Length position)
        {
            this.mockSensor = Mockito.mock(LaneDetector.class);
            this.id = id;
            this.position = position;
            Mockito.when(this.mockSensor.getId()).thenReturn(this.id);
            Mockito.when(this.mockSensor.getLongitudinalPosition()).thenReturn(this.position);
            Mockito.when(this.mockSensor.getSimulator()).thenReturn(this.simulator);
            Mockito.when(this.mockSensor.getFullId()).thenReturn(this.id);
            Mockito.when(this.mockSensor.isCompatible(Mockito.any())).thenReturn(true);
        }

        /**
         * Retrieve the mocked sensor.
         * @return Detector; the mocked sensor
         */
        public LaneDetector getMock()
        {
            return this.mockSensor;
        }

        /**
         * Retrieve the position of the mocked sensor.
         * @return Length; the longitudinal position of the mocked sensor
         */
        public Length getLongitudinalPosition()
        {
            return this.position;
        }

        @Override
        public String toString()
        {
            return "MockSensor [mockSensor=" + this.mockSensor + ", id=" + this.id + ", position=" + this.position + "]";
        }

    }

    /**
     * Mock a LaneBasedObject.
     */
    class MockLaneBasedObject
    {
        /** The mocked sensor. */
        private final LaneBasedObject mockLaneBasedObject;

        /** Id of the mocked sensor. */
        private final String id;

        /** The position along the lane of the sensor. */
        private final Length position;

        /**
         * Construct a new Mocked Detector.
         * @param id String; result of the getId() method of the mocked Detector
         * @param position Length; result of the getLongitudinalPosition of the mocked Detector
         */
        MockLaneBasedObject(final String id, final Length position)
        {
            this.mockLaneBasedObject = Mockito.mock(LaneDetector.class);
            this.id = id;
            this.position = position;
            Mockito.when(this.mockLaneBasedObject.getId()).thenReturn(this.id);
            Mockito.when(this.mockLaneBasedObject.getLongitudinalPosition()).thenReturn(this.position);
            Mockito.when(this.mockLaneBasedObject.getFullId()).thenReturn(this.id);
        }

        /**
         * Retrieve the mocked LaneBasedObject.
         * @return LaneBasedObject; the mocked LaneBasedObject
         */
        public LaneBasedObject getMock()
        {
            return this.mockLaneBasedObject;
        }

        /**
         * Retrieve the position of the mocked sensor.
         * @return Length; the longitudinal position of the mocked sensor
         */
        public Length getLongitudinalPosition()
        {
            return this.position;
        }

        @Override
        public String toString()
        {
            return "MockLaneBasedObject [mockLaneBasedObject=" + this.mockLaneBasedObject + ", id=" + this.id + ", position="
                    + this.position + "]";
        }

    }

    /**
     * Test that gradually varying lateral offsets have gradually increasing angles (with respect to the design line) in the
     * first half and gradually decreasing angles in the second half.
     * @throws NetworkException when that happens uncaught; this test has failed
     * @throws NamingException when that happens uncaught; this test has failed
     * @throws SimRuntimeException when that happens uncaught; this test has failed
     * @throws OtsGeometryException when that happens uncaught; this test has failed
     */
    @Test
    public final void lateralOffsetTest() throws NetworkException, SimRuntimeException, NamingException, OtsGeometryException
    {
        OtsPoint3d from = new OtsPoint3d(10, 10, 0);
        OtsPoint3d to = new OtsPoint3d(1010, 10, 0);
        OtsSimulatorInterface simulator = new OtsSimulator("LaneTest");
        Model model = new Model(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model);
        RoadNetwork network = new RoadNetwork("contour test network", simulator);
        LaneType laneType = DefaultsRoadNl.TWO_WAY_LANE;
        laneType.addCompatibleGtuType(DefaultsNl.VEHICLE);
        Map<GtuType, Speed> speedMap = new LinkedHashMap<>();
        speedMap.put(DefaultsNl.VEHICLE, new Speed(50, KM_PER_HOUR));
        Node start = new Node(network, "start", from, Direction.ZERO);
        Node end = new Node(network, "end", to, Direction.ZERO);
        OtsPoint3d[] coordinates = new OtsPoint3d[2];
        coordinates[0] = start.getPoint();
        coordinates[1] = end.getPoint();
        OtsLine3d line = new OtsLine3d(coordinates);
        CrossSectionLink link =
                new CrossSectionLink(network, "A to B", start, end, DefaultsNl.ROAD, line, LaneKeepingPolicy.KEEPRIGHT);
        Length offsetAtStart = Length.instantiateSI(5);
        Length offsetAtEnd = Length.instantiateSI(15);
        Length width = Length.instantiateSI(4);
        Lane lane =
                LaneGeometryUtil.createStraightLane(link, "lane", offsetAtStart, offsetAtEnd, width, width, laneType, speedMap);
        OtsLine3d laneCenterLine = lane.getCenterLine();
        // System.out.println("Center line is " + laneCenterLine);
        OtsPoint3d[] points = laneCenterLine.getPoints();
        double prev = offsetAtStart.si + from.y;
        double prevRatio = 0;
        double prevDirection = 0;
        for (int i = 0; i < points.length; i++)
        {
            OtsPoint3d p = points[i];
            double relativeLength = p.x - from.x;
            double ratio = relativeLength / (to.x - from.x);
            double actualOffset = p.y;
            if (0 == i)
            {
                assertEquals("first point must have offset at start", offsetAtStart.si + from.y, actualOffset, 0.001);
            }
            if (points.length - 1 == i)
            {
                assertEquals("last point must have offset at end", offsetAtEnd.si + from.y, actualOffset, 0.001);
            }
            // Other offsets must grow smoothly
            double delta = actualOffset - prev;
            assertTrue("delta must be nonnegative", delta >= 0);
            if (i > 0)
            {
                OtsPoint3d prevPoint = points[i - 1];
                double direction = Math.atan2(p.y - prevPoint.y, p.x - prevPoint.x);
                // System.out.println(String.format("p=%30s: ratio=%7.5f, direction=%10.7f", p, ratio, direction));
                assertTrue("Direction of lane center line is > 0", direction > 0);
                if (ratio < 0.5)
                {
                    assertTrue("in first half direction is increasing", direction > prevDirection);
                }
                else if (prevRatio > 0.5)
                {
                    assertTrue("in second half direction is decreasing", direction < prevDirection);
                }
                prevDirection = direction;
                prevRatio = ratio;
            }
        }
    }

    /**
     * Test that the contour of a constructed lane covers the expected area. Tests are only performed for straight lanes, but
     * the orientation of the link and the offset of the lane from the link is varied in many ways.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public final void contourTest() throws Exception
    {
        final int[] startPositions = {0, 1, -1, 20, -20};
        final double[] angles = {0, Math.PI * 0.01, Math.PI / 3, Math.PI / 2, Math.PI * 2 / 3, Math.PI * 0.99, Math.PI,
                Math.PI * 1.01, Math.PI * 4 / 3, Math.PI * 3 / 2, Math.PI * 1.99, Math.PI * 2, Math.PI * (-0.2)};
        int laneNum = 0;
        for (int xStart : startPositions)
        {
            for (int yStart : startPositions)
            {
                for (double angle : angles)
                {
                    OtsSimulatorInterface simulator = new OtsSimulator("LaneTest");
                    Model model = new Model(simulator);
                    simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model);
                    RoadNetwork network = new RoadNetwork("contour test network", simulator);
                    LaneType laneType = DefaultsRoadNl.TWO_WAY_LANE;
                    laneType.addCompatibleGtuType(DefaultsNl.VEHICLE);
                    Map<GtuType, Speed> speedMap = new LinkedHashMap<>();
                    speedMap.put(DefaultsNl.VEHICLE, new Speed(50, KM_PER_HOUR));
                    Node start = new Node(network, "start", new OtsPoint3d(xStart, yStart), Direction.instantiateSI(angle));
                    double linkLength = 1000;
                    double xEnd = xStart + linkLength * Math.cos(angle);
                    double yEnd = yStart + linkLength * Math.sin(angle);
                    Node end = new Node(network, "end", new OtsPoint3d(xEnd, yEnd), Direction.instantiateSI(angle));
                    OtsPoint3d[] coordinates = new OtsPoint3d[2];
                    coordinates[0] = start.getPoint();
                    coordinates[1] = end.getPoint();
                    OtsLine3d line = new OtsLine3d(coordinates);
                    CrossSectionLink link = new CrossSectionLink(network, "A to B", start, end, DefaultsNl.ROAD, line,
                            LaneKeepingPolicy.KEEPRIGHT);
                    final int[] lateralOffsets = {-10, -3, -1, 0, 1, 3, 10};
                    for (int startLateralOffset : lateralOffsets)
                    {
                        for (int endLateralOffset : lateralOffsets)
                        {
                            int startWidth = 4; // This one is not varied
                            for (int endWidth : new int[] {2, 4, 6})
                            {
                                // Now we can construct a Lane
                                // FIXME what overtaking conditions do we want to test in this unit test?
                                Lane lane = LaneGeometryUtil.createStraightLane(link, "lane." + ++laneNum,
                                        new Length(startLateralOffset, METER), new Length(endLateralOffset, METER),
                                        new Length(startWidth, METER), new Length(endWidth, METER), laneType, speedMap);
                                final Geometry geometry = lane.getContour().getLineString();
                                assertNotNull("geometry of the lane should not be null", geometry);
                                // Verify a couple of points that should be inside the contour of the Lane
                                // One meter along the lane design line
                                checkInside(lane, 1, startLateralOffset, true);
                                // One meter before the end along the lane design line
                                checkInside(lane, link.getLength().getSI() - 1, endLateralOffset, true);
                                // One meter before the start of the lane along the lane design line
                                checkInside(lane, -1, startLateralOffset, false);
                                // One meter beyond the end of the lane along the lane design line
                                checkInside(lane, link.getLength().getSI() + 1, endLateralOffset, false);
                                // One meter along the lane design line, left outside the lane
                                checkInside(lane, 1, startLateralOffset - startWidth / 2 - 1, false);
                                // One meter along the lane design line, right outside the lane
                                checkInside(lane, 1, startLateralOffset + startWidth / 2 + 1, false);
                                // One meter before the end, left outside the lane
                                checkInside(lane, link.getLength().getSI() - 1, endLateralOffset - endWidth / 2 - 1, false);
                                // One meter before the end, right outside the lane
                                checkInside(lane, link.getLength().getSI() - 1, endLateralOffset + endWidth / 2 + 1, false);
                                // Check the result of getBounds.
                                DirectedPoint l = lane.getLocation();
                                Bounds bb = lane.getBounds();
                                // System.out.println("bb is " + bb);
                                // System.out.println("l is " + l.x + "," + l.y + "," + l.z);
                                // System.out.println("start is at " + start.getX() + ", " + start.getY());
                                // System.out.println(" end is at " + end.getX() + ", " + end.getY());
                                Point2D.Double[] cornerPoints = new Point2D.Double[4];
                                cornerPoints[0] =
                                        new Point2D.Double(xStart - (startLateralOffset + startWidth / 2) * Math.sin(angle),
                                                yStart + (startLateralOffset + startWidth / 2) * Math.cos(angle));
                                cornerPoints[1] =
                                        new Point2D.Double(xStart - (startLateralOffset - startWidth / 2) * Math.sin(angle),
                                                yStart + (startLateralOffset - startWidth / 2) * Math.cos(angle));
                                cornerPoints[2] = new Point2D.Double(xEnd - (endLateralOffset + endWidth / 2) * Math.sin(angle),
                                        yEnd + (endLateralOffset + endWidth / 2) * Math.cos(angle));
                                cornerPoints[3] = new Point2D.Double(xEnd - (endLateralOffset - endWidth / 2) * Math.sin(angle),
                                        yEnd + (endLateralOffset - endWidth / 2) * Math.cos(angle));
                                // for (int i = 0; i < cornerPoints.length; i++)
                                // {
                                // System.out.println("p" + i + ": " + cornerPoints[i].x + "," + cornerPoints[i].y);
                                // }
                                double minX = cornerPoints[0].getX();
                                double maxX = cornerPoints[0].getX();
                                double minY = cornerPoints[0].getY();
                                double maxY = cornerPoints[0].getY();
                                for (int i = 1; i < cornerPoints.length; i++)
                                {
                                    Point2D.Double p = cornerPoints[i];
                                    minX = Math.min(minX, p.getX());
                                    minY = Math.min(minY, p.getY());
                                    maxX = Math.max(maxX, p.getX());
                                    maxY = Math.max(maxY, p.getY());
                                }
                                // System.out.println(" my bbox is " + minX + "," + minY + " - " + maxX + "," + maxY);
                                // System.out.println("the bbox is " + (bbLow.x + l.x) + "," + (bbLow.y + l.y) + " - "
                                // + (bbHigh.x + l.x) + "," + (bbHigh.y + l.y));
                                double boundsMinX = bb.getMinX() + l.x;
                                double boundsMinY = bb.getMinY() + l.y;
                                double boundsMaxX = bb.getMaxX() + l.x;
                                double boundsMaxY = bb.getMaxY() + l.y;
                                assertEquals("low x boundary", minX, boundsMinX, 0.1);
                                assertEquals("low y boundary", minY, boundsMinY, 0.1);
                                assertEquals("high x boundary", maxX, boundsMaxX, 0.1);
                                assertEquals("high y boundary", maxY, boundsMaxY, 0.1);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Verify that a point at specified distance along and across from the design line of the parent Link of a Lane is inside
     * c.q. outside the contour of a Lane. The test uses an implementation that is as independent as possible of the Geometry
     * class methods.
     * @param lane Lane; the lane
     * @param longitudinal double; the longitudinal position along the design line of the parent Link of the Lane. This design
     *            line is expected to be straight and the longitudinal position may be negative (indicating a point before the
     *            start of the Link) and it may exceed the length of the Link (indicating a point beyond the end of the Link)
     * @param lateral double; the lateral offset from the design line of the link (positive is left, negative is right)
     * @param expectedResult boolean; true if the calling method expects the point to be within the contour of the Lane, false
     *            if the calling method expects the point to be outside the contour of the Lane
     */
    private void checkInside(final Lane lane, final double longitudinal, final double lateral, final boolean expectedResult)
    {
        CrossSectionLink parentLink = lane.getLink();
        Node start = parentLink.getStartNode();
        Node end = parentLink.getEndNode();
        double startX = start.getPoint().x;
        double startY = start.getPoint().y;
        double endX = end.getPoint().x;
        double endY = end.getPoint().y;
        double length = Math.sqrt((endX - startX) * (endX - startX) + (endY - startY) * (endY - startY));
        double ratio = longitudinal / length;
        double designLineX = startX + (endX - startX) * ratio;
        double designLineY = startY + (endY - startY) * ratio;
        double lateralAngle = Math.atan2(endY - startY, endX - startX) + Math.PI / 2;
        double px = designLineX + lateral * Math.cos(lateralAngle);
        double py = designLineY + lateral * Math.sin(lateralAngle);
        Geometry contour = lane.getContour().getLineString();
        GeometryFactory factory = new GeometryFactory();
        Geometry p = factory.createPoint(new Coordinate(px, py));
        // CrossSectionElement.printCoordinates("contour: ", contour);
        // System.out.println("p: " + p);
        boolean result = contour.contains(p);
        Coordinate[] polygon = contour.getCoordinates();
        result = pointInsidePolygon(new Coordinate(px, py), polygon);
        if (expectedResult)
        {
            assertTrue("Point at " + longitudinal + " along and " + lateral + " lateral is within lane", result);
        }
        else
        {
            assertFalse("Point at " + longitudinal + " along and " + lateral + " lateral is outside lane", result);
        }
    }

    /**
     * Algorithm of W. Randolph Franklin http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html, found via
     * stackoverflow.com: https://stackoverflow.com/questions/217578/point-in-polygon-aka-hit-test.
     * @param point Coordinate; the point
     * @param polygon OtsPoint3d[]; the polygon (last coordinate is allowed to be identical to the first, but his is not a
     *            requirement)
     * @return boolean; true if the point is inside the polygon; false if it is outside the polygon; if the point lies <b>on</b>
     *         an vertex or edge of the polygon the result is (of course) undefined
     */
    private boolean pointInsidePolygon(final Coordinate point, final Coordinate[] polygon)
    {
        boolean result = false;
        for (int i = 0, j = polygon.length - 1; i < polygon.length; j = i++)
        {
            if ((polygon[i].y > point.y) != (polygon[j].y > point.y)
                    && point.x < (polygon[j].x - polygon[i].x) * (point.y - polygon[i].y) / (polygon[j].y - polygon[i].y)
                            + polygon[i].x)
            {
                result = !result;
            }
        }
        return result;
    }

    /** The helper model. */
    protected static class Model extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20141027L;

        /**
         * @param simulator the simulator to use
         */
        public Model(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public final RoadNetwork getNetwork()
        {
            return null;
        }
    }

}

package org.opentrafficsim.road.network.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.geom.Point2D;
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
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.function.ContinuousPiecewiseLinearFunction;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.mock.MockDevsSimulator;
import org.opentrafficsim.road.network.LaneKeepingPolicy;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;
import org.opentrafficsim.road.network.lane.object.detector.LaneDetector;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the Lane class.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class LaneTest implements UNITS
{

    /** */
    private LaneTest()
    {
        // do not instantiate test class
    }

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
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model,
                HistoryManagerDevs.noHistory(simulator));
        // First we need two Nodes
        Node nodeFrom = new Node(network, "A", new Point2d(0, 0), Direction.ZERO);
        Node nodeTo = new Node(network, "B", new Point2d(1000, 0), Direction.ZERO);
        // Now we can make a Link
        Point2d[] coordinates = new Point2d[2];
        coordinates[0] = nodeFrom.getPoint();
        coordinates[1] = nodeTo.getPoint();
        CrossSectionLink link = new CrossSectionLink(network, "A to B", nodeFrom, nodeTo, DefaultsNl.FREEWAY,
                new OtsLine2d(coordinates), null, LaneKeepingPolicy.KEEPRIGHT);
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
        assertEquals(network, link.getNetwork(), "Link returns network");
        assertEquals(network, lane.getNetwork(), "Lane returns network");
        assertEquals(0, lane.prevLanes(gtuTypeCar).size(), "PrevLanes should be empty"); // this one caught a bug!
        assertEquals(0, lane.nextLanes(gtuTypeCar).size(), "NextLanes should be empty");
        double approximateLengthOfContour =
                2 * nodeFrom.getPoint().distance(nodeTo.getPoint()) + startWidth.getSI() + endWidth.getSI();
        assertEquals(approximateLengthOfContour, lane.getAbsoluteContour().getLength(), 0.1,
                "Length of contour is approximately " + approximateLengthOfContour);
        assertEquals(new Speed(100, KM_PER_HOUR), lane.getSpeedLimit(DefaultsNl.VEHICLE),
                "SpeedLimit should be " + (new Speed(100, KM_PER_HOUR)));
        assertEquals(0, lane.getGtuList().size(), "There should be no GTUs on the lane");
        assertEquals(laneType, lane.getType(), "LaneType should be " + laneType);
        // TODO: This test for expectedLateralCenterOffset fails
        for (int i = 0; i < 10; i++)
        {
            double expectedLateralCenterOffset =
                    startLateralPos.getSI() + (endLateralPos.getSI() - startLateralPos.getSI()) * i / 10;
            assertEquals(expectedLateralCenterOffset, lane.getLateralCenterPosition(i / 10.0).getSI(), 0.01,
                    String.format("Lateral offset at %d%% should be %.3fm", 10 * i, expectedLateralCenterOffset));
            Length longitudinalPosition = new Length(lane.getLength().getSI() * i / 10, METER);
            assertEquals(expectedLateralCenterOffset, lane.getLateralCenterPosition(longitudinalPosition).getSI(), 0.01,
                    "Lateral offset at " + longitudinalPosition + " should be " + expectedLateralCenterOffset);
            double expectedWidth = startWidth.getSI() + (endWidth.getSI() - startWidth.getSI()) * i / 10;
            assertEquals(expectedWidth, lane.getWidth(i / 10.0).getSI(), 0.0001,
                    String.format("Width at %d%% should be %.3fm", 10 * i, expectedWidth));
            assertEquals(expectedWidth, lane.getWidth(longitudinalPosition).getSI(), 0.0001,
                    "Width at " + longitudinalPosition + " should be " + expectedWidth);
            double expectedLeftOffset = expectedLateralCenterOffset - expectedWidth / 2;
            // The next test caught a bug
            assertEquals(expectedLeftOffset, lane.getLateralBoundaryPosition(LateralDirectionality.LEFT, i / 10.0).getSI(),
                    0.001, String.format("Left edge at %d%% should be %.3fm", 10 * i, expectedLeftOffset));
            assertEquals(expectedLeftOffset,
                    lane.getLateralBoundaryPosition(LateralDirectionality.LEFT, longitudinalPosition).getSI(), 0.001,
                    "Left edge at " + longitudinalPosition + " should be " + expectedLeftOffset);
            double expectedRightOffset = expectedLateralCenterOffset + expectedWidth / 2;
            assertEquals(expectedRightOffset, lane.getLateralBoundaryPosition(LateralDirectionality.RIGHT, i / 10.0).getSI(),
                    0.001, String.format("Right edge at %d%% should be %.3fm", 10 * i, expectedRightOffset));
            assertEquals(expectedRightOffset,
                    lane.getLateralBoundaryPosition(LateralDirectionality.RIGHT, longitudinalPosition).getSI(), 0.001,
                    "Right edge at " + longitudinalPosition + " should be " + expectedRightOffset);
        }

        // Harder case; create a Link with form points along the way
        // System.out.println("Constructing Link and Lane with one form point");
        coordinates = new Point2d[3];
        coordinates[0] = new Point2d(nodeFrom.getPoint().x, nodeFrom.getPoint().y);
        coordinates[1] = new Point2d(200, 100);
        coordinates[2] = new Point2d(nodeTo.getPoint().x, nodeTo.getPoint().y);
        link = new CrossSectionLink(network, "A to B with Kink", nodeFrom, nodeTo, DefaultsNl.FREEWAY,
                new OtsLine2d(coordinates), null, LaneKeepingPolicy.KEEPRIGHT);
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
        OtsLine2d centerLine = new OtsLine2d(new Point2d(0.0, 0.0), new Point2d(100.0, 0.0));
        Polygon2d contour = new Polygon2d(new Point2d(0.0, -1.75), new Point2d(100.0, -1.75), new Point2d(100.0, 1.75),
                new Point2d(0.0, -1.75));
        ContinuousPiecewiseLinearFunction offsetFunc = ContinuousPiecewiseLinearFunction.of(0.0, startLateralPos.si);
        ContinuousPiecewiseLinearFunction widthFunc = ContinuousPiecewiseLinearFunction.of(0.0, startWidth.si);
        lane = new Lane(link, "lanex", new CrossSectionGeometry(centerLine, contour, offsetFunc, widthFunc), laneType,
                speedMap);
        sensorTest(lane);
    }

    /**
     * Add/Remove some sensor to/from a lane and see if the expected events occur.
     * @param lane the lane to manipulate
     * @throws NetworkException when this happens uncaught; this test has failed
     */
    public void sensorTest(final Lane lane) throws NetworkException
    {
        assertEquals(0, lane.getDetectors().size(), "List of sensor is initially empty");
        Listener listener = new Listener();
        double length = lane.getLength().si;
        lane.addListener(listener, Lane.DETECTOR_ADD_EVENT);
        lane.addListener(listener, Lane.DETECTOR_REMOVE_EVENT);
        assertEquals(0, listener.events.size(), "event list is initially empty");
        LaneDetector sensor1 = new MockSensor("sensor1", Length.ofSI(length / 4)).getMock();
        lane.addDetector(sensor1);
        assertEquals(1, listener.events.size(), "event list now contains one event");
        assertEquals(listener.events.get(0).getType(), Lane.DETECTOR_ADD_EVENT, "event indicates that a sensor got added");
        assertEquals(1, lane.getDetectors().size(), "lane now contains one sensor");
        assertEquals(sensor1, lane.getDetectors().get(0), "sensor on lane is sensor1");
        LaneDetector sensor2 = new MockSensor("sensor2", Length.ofSI(length / 2)).getMock();
        lane.addDetector(sensor2);
        assertEquals(2, listener.events.size(), "event list now contains two events");
        assertEquals(listener.events.get(1).getType(), Lane.DETECTOR_ADD_EVENT, "event indicates that a sensor got added");
        List<LaneDetector> sensors = lane.getDetectors();
        assertEquals(2, sensors.size(), "lane now contains two sensors");
        assertTrue(sensors.contains(sensor1), "sensor list contains sensor1");
        assertTrue(sensors.contains(sensor2), "sensor list contains sensor2");
        sensors = lane.getDetectors(Length.ZERO, Length.ofSI(length / 3), DefaultsNl.VEHICLE);
        assertEquals(1, sensors.size(), "first third of lane contains 1 sensor");
        assertTrue(sensors.contains(sensor1), "sensor list contains sensor1");
        sensors = lane.getDetectors(Length.ofSI(length / 3), Length.ofSI(length), DefaultsNl.VEHICLE);
        assertEquals(1, sensors.size(), "last two-thirds of lane contains 1 sensor");
        assertTrue(sensors.contains(sensor2), "sensor list contains sensor2");
        sensors = lane.getDetectors(DefaultsNl.VEHICLE);
        // NB. The mocked sensor is compatible with all GTU types in all directions.
        assertEquals(2, sensors.size(), "sensor list contains two sensors");
        assertTrue(sensors.contains(sensor1), "sensor list contains sensor1");
        assertTrue(sensors.contains(sensor2), "sensor list contains sensor2");
        sensors = lane.getDetectors(DefaultsNl.VEHICLE);
        // NB. The mocked sensor is compatible with all GTU types in all directions.
        assertEquals(2, sensors.size(), "sensor list contains two sensors");
        assertTrue(sensors.contains(sensor1), "sensor list contains sensor1");
        assertTrue(sensors.contains(sensor2), "sensor list contains sensor2");
        SortedMap<Double, List<LaneDetector>> sensorMap = lane.getDetectorMap(DefaultsNl.VEHICLE);
        assertEquals(2, sensorMap.size(), "sensor map contains two entries");
        for (Double d : sensorMap.keySet())
        {
            List<LaneDetector> sensorsAtD = sensorMap.get(d);
            assertEquals(1, sensorsAtD.size(), "There is one sensor at position d");
            assertEquals(d < length / 3 ? sensor1 : sensor2, sensorsAtD.get(0),
                    "Sensor map contains the correct sensor at the correct distance");
        }

        lane.removeDetector(sensor1);
        assertEquals(3, listener.events.size(), "event list now contains three events");
        assertEquals(listener.events.get(2).getType(), Lane.DETECTOR_REMOVE_EVENT, "event indicates that a sensor got removed");
        sensors = lane.getDetectors();
        assertEquals(1, sensors.size(), "lane now contains one sensor");
        assertTrue(sensors.contains(sensor2), "sensor list contains sensor2");
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
        LaneDetector badSensor = new MockSensor("sensor3", Length.ofSI(-0.1)).getMock();
        try
        {
            lane.addDetector(badSensor);
            fail("Adding a sensor at negative position should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        badSensor = new MockSensor("sensor4", Length.ofSI(length + 0.1)).getMock();
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
        assertEquals(0, lboList.size(), "lane initially contains zero lane based objects");
        LaneBasedObject lbo1 = new MockLaneBasedObject("lbo1", Length.ofSI(length / 4)).getMock();
        listener.getEvents().clear();
        lane.addListener(listener, Lane.OBJECT_ADD_EVENT);
        lane.addListener(listener, Lane.OBJECT_REMOVE_EVENT);
        lane.addLaneBasedObject(lbo1);
        assertEquals(1, listener.getEvents().size(), "adding a lane based object cause the lane to emit an event");
        assertEquals(Lane.OBJECT_ADD_EVENT, listener.getEvents().get(0).getType(), "The emitted event was a OBJECT_ADD_EVENT");
        LaneBasedObject lbo2 = new MockLaneBasedObject("lbo2", Length.ofSI(3 * length / 4)).getMock();
        lane.addLaneBasedObject(lbo2);
        lboList = lane.getLaneBasedObjects();
        assertEquals(2, lboList.size(), "lane based object list now contains two objects");
        assertTrue(lboList.contains(lbo1), "lane base object list contains lbo1");
        assertTrue(lboList.contains(lbo2), "lane base object list contains lbo2");
        lboList = lane.getLaneBasedObjects(Length.ZERO, Length.ofSI(length / 2));
        assertEquals(1, lboList.size(), "first half of lane contains one object");
        assertEquals(lbo1, lboList.get(0), "object in first haf of lane is lbo1");
        lboList = lane.getLaneBasedObjects(Length.ofSI(length / 2), Length.ofSI(length));
        assertEquals(1, lboList.size(), "second half of lane contains one object");
        assertEquals(lbo2, lboList.get(0), "object in second haf of lane is lbo2");
        SortedMap<Double, List<LaneBasedObject>> sortedMap = lane.getLaneBasedObjectMap();
        assertEquals(2, sortedMap.size(), "sorted map contains two objects");
        for (Double d : sortedMap.keySet())
        {
            List<LaneBasedObject> objectsAtD = sortedMap.get(d);
            assertEquals(1, objectsAtD.size(), "There is one object at position d");
            assertEquals(d < length / 2 ? lbo1 : lbo2, objectsAtD.get(0), "Object at position d is the expected one");
        }

        for (double fraction : new double[] {-0.5, 0, 0.2, 0.5, 0.9, 1.0, 2})
        {
            double positionSI = length * fraction;
            double fractionSI = lane.fractionSI(positionSI);
            assertEquals(fraction, fractionSI, 0.0001, "fractionSI matches fraction");

            LaneBasedObject nextObject = positionSI < lbo1.getLongitudinalPosition().si ? lbo1
                    : positionSI < lbo2.getLongitudinalPosition().si ? lbo2 : null;
            List<LaneBasedObject> expected = new ArrayList<>();
            if (null != nextObject)
            {
                expected.add(nextObject);
            }
            List<LaneBasedObject> got = lane.getObjectAhead(Length.ofSI(positionSI));
            assertEquals(expected, got, "First bunch of objects ahead of d");

            nextObject = positionSI > lbo2.getLongitudinalPosition().si ? lbo2
                    : positionSI > lbo1.getLongitudinalPosition().si ? lbo1 : null;
            expected = new ArrayList<>();
            if (null != nextObject)
            {
                expected.add(nextObject);
            }
            got = lane.getObjectBehind(Length.ofSI(positionSI));
            assertEquals(expected, got, "First bunch of objects behind d");
        }

        lane.removeLaneBasedObject(lbo1);
        assertEquals(3, listener.getEvents().size(), "removing a lane based object caused the lane to emit an event");
        assertEquals(Lane.OBJECT_REMOVE_EVENT, listener.getEvents().get(2).getType(),
                "removing a lane based object caused the lane to emit OBJECT_REMOVE_EVENT");
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
        LaneBasedObject badLBO = new MockLaneBasedObject("badLBO", Length.ofSI(-0.1)).getMock();
        try
        {
            lane.addLaneBasedObject(badLBO);
            fail("Adding a lane based object at negative position should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        badLBO = new MockLaneBasedObject("badLBO", Length.ofSI(length + 0.1)).getMock();
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

        /**
         * Constructor.
         */
        Listener()
        {
            //
        }

        @Override
        public void notify(final Event event)
        {
            this.events.add(event);
        }

        /**
         * Retrieve the collected events.
         * @return the events
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
         * @param id result of the getId() method of the mocked Detector
         * @param position result of the getLongitudinalPosition of the mocked Detector
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
         * @return the mocked sensor
         */
        public LaneDetector getMock()
        {
            return this.mockSensor;
        }

        /**
         * Retrieve the position of the mocked sensor.
         * @return the longitudinal position of the mocked sensor
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
         * @param id result of the getId() method of the mocked Detector
         * @param position result of the getLongitudinalPosition of the mocked Detector
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
         * @return the mocked LaneBasedObject
         */
        public LaneBasedObject getMock()
        {
            return this.mockLaneBasedObject;
        }

        /**
         * Retrieve the position of the mocked sensor.
         * @return the longitudinal position of the mocked sensor
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
     */
    @Test
    public final void lateralOffsetTest() throws NetworkException, SimRuntimeException, NamingException
    {
        Point2d from = new Point2d(10, 10);
        Point2d to = new Point2d(1010, 10);
        OtsSimulatorInterface simulator = new OtsSimulator("LaneTest");
        Model model = new Model(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model,
                HistoryManagerDevs.noHistory(simulator));
        RoadNetwork network = new RoadNetwork("contour test network", simulator);
        LaneType laneType = DefaultsRoadNl.TWO_WAY_LANE;
        laneType.addCompatibleGtuType(DefaultsNl.VEHICLE);
        Map<GtuType, Speed> speedMap = new LinkedHashMap<>();
        speedMap.put(DefaultsNl.VEHICLE, new Speed(50, KM_PER_HOUR));
        Node start = new Node(network, "start", from, Direction.ZERO);
        Node end = new Node(network, "end", to, Direction.ZERO);
        Point2d[] coordinates = new Point2d[2];
        coordinates[0] = start.getPoint();
        coordinates[1] = end.getPoint();
        OtsLine2d line = new OtsLine2d(coordinates);
        CrossSectionLink link =
                new CrossSectionLink(network, "A to B", start, end, DefaultsNl.ROAD, line, null, LaneKeepingPolicy.KEEPRIGHT);
        Length offsetAtStart = Length.ofSI(5);
        Length offsetAtEnd = Length.ofSI(15);
        Length width = Length.ofSI(4);
        Lane lane =
                LaneGeometryUtil.createStraightLane(link, "lane", offsetAtStart, offsetAtEnd, width, width, laneType, speedMap);
        OtsLine2d laneCenterLine = lane.getCenterLine();
        // System.out.println("Center line is " + laneCenterLine);
        List<Point2d> points = laneCenterLine.getPointList();
        double prev = offsetAtStart.si + from.y;
        double prevRatio = 0;
        double prevDirection = 0;
        for (int i = 0; i < points.size(); i++)
        {
            Point2d p = points.get(i);
            double relativeLength = p.x - from.x;
            double ratio = relativeLength / (to.x - from.x);
            double actualOffset = p.y;
            if (0 == i)
            {
                assertEquals(offsetAtStart.si + from.y, actualOffset, 0.001, "first point must have offset at start");
            }
            if (points.size() - 1 == i)
            {
                assertEquals(offsetAtEnd.si + from.y, actualOffset, 0.001, "last point must have offset at end");
            }
            // Other offsets must grow smoothly
            double delta = actualOffset - prev;
            assertTrue(delta >= 0, "delta must be nonnegative");
            if (i > 0)
            {
                Point2d prevPoint = points.get(i - 1);
                double direction = Math.atan2(p.y - prevPoint.y, p.x - prevPoint.x);
                // System.out.println(String.format("p=%30s: ratio=%7.5f, direction=%10.7f", p, ratio, direction));
                assertTrue(direction > 0, "Direction of lane center line is > 0");
                if (ratio < 0.5)
                {
                    assertTrue(direction > prevDirection, "in first half direction is increasing");
                }
                else if (prevRatio > 0.5)
                {
                    assertTrue(direction < prevDirection, "in second half direction is decreasing");
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
                    simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model,
                            HistoryManagerDevs.noHistory(simulator));
                    RoadNetwork network = new RoadNetwork("contour test network", simulator);
                    LaneType laneType = DefaultsRoadNl.TWO_WAY_LANE;
                    laneType.addCompatibleGtuType(DefaultsNl.VEHICLE);
                    Map<GtuType, Speed> speedMap = new LinkedHashMap<>();
                    speedMap.put(DefaultsNl.VEHICLE, new Speed(50, KM_PER_HOUR));
                    Node start = new Node(network, "start", new Point2d(xStart, yStart), Direction.ofSI(angle));
                    double linkLength = 1000;
                    double xEnd = xStart + linkLength * Math.cos(angle);
                    double yEnd = yStart + linkLength * Math.sin(angle);
                    Node end = new Node(network, "end", new Point2d(xEnd, yEnd), Direction.ofSI(angle));
                    Point2d[] coordinates = new Point2d[2];
                    coordinates[0] = start.getPoint();
                    coordinates[1] = end.getPoint();
                    OtsLine2d line = new OtsLine2d(coordinates);
                    CrossSectionLink link = new CrossSectionLink(network, "A to B", start, end, DefaultsNl.ROAD, line, null,
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
                                DirectedPoint2d l = lane.getLocation();
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
                                Bounds<?, ?> bb = lane.getAbsoluteContour().getAbsoluteBounds();
                                double boundsMinX = bb.getMinX();
                                double boundsMinY = bb.getMinY();
                                double boundsMaxX = bb.getMaxX();
                                double boundsMaxY = bb.getMaxY();
                                assertEquals(minX, boundsMinX, 0.1, "low x boundary");
                                assertEquals(minY, boundsMinY, 0.1, "low y boundary");
                                assertEquals(maxX, boundsMaxX, 0.1, "high x boundary");
                                assertEquals(maxY, boundsMaxY, 0.1, "high y boundary");
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
     * @param lane the lane
     * @param longitudinal the longitudinal position along the design line of the parent Link of the Lane. This design line is
     *            expected to be straight and the longitudinal position may be negative (indicating a point before the start of
     *            the Link) and it may exceed the length of the Link (indicating a point beyond the end of the Link)
     * @param lateral the lateral offset from the design line of the link (positive is left, negative is right)
     * @param expectedResult true if the calling method expects the point to be within the contour of the Lane, false if the
     *            calling method expects the point to be outside the contour of the Lane
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
        Polygon2d contour = lane.getAbsoluteContour();
        // GeometryFactory factory = new GeometryFactory();
        // Geometry p = factory.createPoint(new Coordinate(px, py));
        Point2d p = new Point2d(px, py);
        // CrossSectionElement.printCoordinates("contour: ", contour);
        // System.out.println("p: " + p);
        boolean result = contour.contains(p);
        if (expectedResult)
        {
            assertTrue(result, "Point at " + longitudinal + " along and " + lateral + " lateral is within lane");
        }
        else
        {
            assertFalse(result, "Point at " + longitudinal + " along and " + lateral + " lateral is outside lane");
        }
    }

    /** The helper model. */
    protected static class Model extends AbstractOtsModel
    {
        /**
         * Constructor.
         * @param simulator the simulator to use
         */
        public Model(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        @Override
        public final void constructModel() throws SimRuntimeException
        {
            //
        }

        @Override
        public final RoadNetwork getNetwork()
        {
            return null;
        }
    }

}

package org.opentrafficsim.core.network.lane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.AbstractNode;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.geotools.LinearGeometry;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Test the Lane class.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version21 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneTest
{
    /**
     * Test the constructor.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void laneConstructorTest() throws Exception
    {
        // First we need two Nodes
        NodeGeotools.STR nodeFrom = new NodeGeotools.STR("A", new Coordinate(0, 0, 0));
        NodeGeotools.STR nodeTo = new NodeGeotools.STR("B", new Coordinate(1000, 0, 0));
        // Now we can make a Link
        Coordinate[] coordinates = new Coordinate[2];
        coordinates[0] = new Coordinate(nodeFrom.getPoint().x, nodeFrom.getPoint().y, 0);
        coordinates[1] = new Coordinate(nodeTo.getPoint().x, nodeTo.getPoint().y, 0);
        GeometryFactory factory = new GeometryFactory();
        LineString lineString = factory.createLineString(coordinates);
        CrossSectionLink<?, ?> link =
                new CrossSectionLink<String, String>("A to B", nodeFrom, nodeTo, new DoubleScalar.Rel<LengthUnit>(
                        lineString.getLength(), LengthUnit.METER));
        new LinearGeometry(link, lineString, null);
        DoubleScalar.Rel<LengthUnit> startLateralPos = new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> endLateralPos = new DoubleScalar.Rel<LengthUnit>(5, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> startWidth = new DoubleScalar.Rel<LengthUnit>(3, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> endWidth = new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER);
        GTUType<String> gtuTypeCar = GTUType.makeGTUType("Car");
        GTUType<String> gtuTypeTruck = GTUType.makeGTUType("Truck");
        LaneType<String> laneType = new LaneType<String>("Car");
        laneType.addCompatibility(gtuTypeCar);
        laneType.addCompatibility(gtuTypeTruck);
        DoubleScalar.Abs<FrequencyUnit> f2000 = new DoubleScalar.Abs<FrequencyUnit>(2000, FrequencyUnit.PER_HOUR);
        LongitudinalDirectionality longitudinalDirectionality = LongitudinalDirectionality.FORWARD;
        DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        // Now we can construct a Lane
        Lane lane =
                new Lane(link, startLateralPos, endLateralPos, startWidth, endWidth, laneType,
                        longitudinalDirectionality, f2000, speedLimit);
        // Verify the easy bits
        assertEquals("Capacity should be " + f2000, f2000.getSI(), lane.getCapacity().getSI(), 0.001);
        assertEquals("PrevLanes should be empty", 0, lane.prevLanes().size()); // this one caught a bug!
        assertEquals("NextLanes should be empty", 0, lane.nextLanes().size());
        double approximateLengthOfContour =
                2 * nodeFrom.getPoint().distance(nodeTo.getPoint()) + startWidth.getSI() + endWidth.getSI();
        assertEquals("Length of contour is approximately " + approximateLengthOfContour, approximateLengthOfContour,
                lane.getContour().getLength(), 0.1);
        assertEquals("Directionality should be " + longitudinalDirectionality, longitudinalDirectionality,
                lane.getDirectionality());
        assertEquals("SpeedLimit should be " + speedLimit, speedLimit, lane.getSpeedLimit());
        assertEquals("There should be no GTUs on the lane", 0, lane.getGtuList().size());
        assertEquals("LaneType should be " + laneType, laneType, lane.getLaneType());
        for (int i = 0; i < 10; i++)
        {
            double expectedLateralCenterOffset =
                    startLateralPos.getSI() + (endLateralPos.getSI() - startLateralPos.getSI()) * i / 10;
            assertEquals(String.format("Lateral offset at %d%% should be %.3fm", 10 * i, expectedLateralCenterOffset),
                    expectedLateralCenterOffset, lane.getLateralCenterPosition(i / 10.0).getSI(), 0.01);
            DoubleScalar.Rel<LengthUnit> longitudinalPosition =
                    new DoubleScalar.Rel<LengthUnit>(lane.getLength().getSI() * i / 10, LengthUnit.METER);
            assertEquals("Lateral offset at " + longitudinalPosition + " should be " + expectedLateralCenterOffset,
                    expectedLateralCenterOffset, lane.getLateralCenterPosition(longitudinalPosition).getSI(), 0.01);
            double expectedWidth = startWidth.getSI() + (endWidth.getSI() - startWidth.getSI()) * i / 10;
            assertEquals(String.format("Width at %d%% should be %.3fm", 10 * i, expectedWidth), expectedWidth, lane
                    .getWidth(i / 10.0).getSI(), 0.0001);
            assertEquals("Width at " + longitudinalPosition + " should be " + expectedWidth, expectedWidth, lane
                    .getWidth(longitudinalPosition).getSI(), 0.0001);
            double expectedLeftOffset = expectedLateralCenterOffset - expectedWidth / 2;
            // The next test caught a bug
            assertEquals(String.format("Left edge at %d%% should be %.3fm", 10 * i, expectedLeftOffset),
                    expectedLeftOffset, lane.getLateralBoundaryPosition(LateralDirectionality.LEFT, i / 10.0).getSI(),
                    0.001);
            assertEquals("Left edge at " + longitudinalPosition + " should be " + expectedLeftOffset,
                    expectedLeftOffset,
                    lane.getLateralBoundaryPosition(LateralDirectionality.LEFT, longitudinalPosition).getSI(), 0.001);
            double expectedRightOffset = expectedLateralCenterOffset + expectedWidth / 2;
            assertEquals(String.format("Right edge at %d%% should be %.3fm", 10 * i, expectedRightOffset),
                    expectedRightOffset,
                    lane.getLateralBoundaryPosition(LateralDirectionality.RIGHT, i / 10.0).getSI(), 0.001);
            assertEquals("Right edge at " + longitudinalPosition + " should be " + expectedRightOffset,
                    expectedRightOffset,
                    lane.getLateralBoundaryPosition(LateralDirectionality.RIGHT, longitudinalPosition).getSI(), 0.001);
        }
        List<Sensor> sensors =
                lane.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER));
        assertEquals("The lane should have two sensors", 2, sensors.size());
        sensors =
                lane.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(0.1, LengthUnit.METER));
        assertEquals("There should be one sensor at the start of the lane", 1, sensors.size());
        assertTrue("The sensor at the start of the lane should be a SensorLaneStart",
                sensors.get(0) instanceof SensorLaneStart);
        assertEquals("This sensor should be at 0m", 0, sensors.get(0).getLongitudinalPosition().getSI(), 0.00001);
        assertEquals("This sensor should be at 0m", 0, sensors.get(0).getLongitudinalPositionSI(), 0.00001);
        sensors =
                lane.getSensors(new DoubleScalar.Rel<LengthUnit>(lane.getLength().getSI() - 1, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER));
        assertEquals("There should be one sensor at the end of the lane", 1, sensors.size());
        assertTrue("The sensor at the start of the lane should be a SensorLaneEnd",
                sensors.get(0) instanceof SensorLaneEnd);
        assertEquals("This sensor should be at the end of the lane", lane.getLength().getSI(), sensors.get(0)
                .getLongitudinalPosition().getSI(), 0.01);
        assertEquals("This sensor should be at the end of the lane", lane.getLength().getSI(), sensors.get(0)
                .getLongitudinalPositionSI(), 0.01);

        // Harder case; create a Link with form points along the way
        // System.out.println("Constructing Link and Lane with one form point");
        coordinates = new Coordinate[3];
        coordinates[0] = new Coordinate(nodeFrom.getPoint().x, nodeFrom.getPoint().y, 0);
        coordinates[1] = new Coordinate(200, 100);
        coordinates[2] = new Coordinate(nodeTo.getPoint().x, nodeTo.getPoint().y, 0);
        lineString = factory.createLineString(coordinates);
        link =
                new CrossSectionLink<String, String>("A to B with Kink", nodeFrom, nodeTo,
                        new DoubleScalar.Rel<LengthUnit>(lineString.getLength(), LengthUnit.METER));
        new LinearGeometry(link, lineString, null);
        lane =
                new Lane(link, startLateralPos, endLateralPos, startWidth, endWidth, laneType,
                        longitudinalDirectionality, f2000, speedLimit);
        // Verify the easy bits
        assertEquals("Capacity should be " + f2000, f2000.getSI(), lane.getCapacity().getSI(), 0.001);
        assertEquals("PrevLanes should be empty", 0, lane.prevLanes().size());
        assertEquals("NextLanes should be empty", 0, lane.nextLanes().size());
        approximateLengthOfContour =
                2 * (coordinates[0].distance(coordinates[1]) + coordinates[1].distance(coordinates[2]))
                        + startWidth.getSI() + endWidth.getSI();
        assertEquals("Length of contour is approximately " + approximateLengthOfContour, approximateLengthOfContour,
                lane.getContour().getLength(), 4); // This lane takes a path that is about 3m longer
        assertEquals("Directionality should be " + longitudinalDirectionality, longitudinalDirectionality,
                lane.getDirectionality());
        assertEquals("There should be no GTUs on the lane", 0, lane.getGtuList().size());
        assertEquals("LaneType should be " + laneType, laneType, lane.getLaneType());
        sensors =
                lane.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER));
        assertEquals("The lane should have two sensors", 2, sensors.size());
        sensors =
                lane.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(0.1, LengthUnit.METER));
        assertEquals("There should be one sensor at the start of the lane", 1, sensors.size());
        assertTrue("The sensor at the start of the lane should be a SensorLaneStart",
                sensors.get(0) instanceof SensorLaneStart);
        assertEquals("This sensor should be at 0m", 0, sensors.get(0).getLongitudinalPosition().getSI(), 0.00001);
        assertEquals("This sensor should be at 0m", 0, sensors.get(0).getLongitudinalPositionSI(), 0.00001);
        sensors =
                lane.getSensors(new DoubleScalar.Rel<LengthUnit>(lane.getLength().getSI() - 1, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER));
        assertEquals("There should be one sensor at the end of the lane", 1, sensors.size());
        assertTrue("The sensor at the start of the lane should be a SensorLaneEnd",
                sensors.get(0) instanceof SensorLaneEnd);
        assertEquals("This sensor should be at the end of the lane", lane.getLength().getSI(), sensors.get(0)
                .getLongitudinalPosition().getSI(), 0.01);
        assertEquals("This sensor should be at the end of the lane", lane.getLength().getSI(), sensors.get(0)
                .getLongitudinalPositionSI(), 0.01);
        // System.out.println("Add another Lane at the inside of the corner in the design line");
        DoubleScalar.Rel<LengthUnit> startLateralPos2 = new DoubleScalar.Rel<LengthUnit>(-8, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> endLateralPos2 = new DoubleScalar.Rel<LengthUnit>(-5, LengthUnit.METER);
        Lane lane2 =
                new Lane(link, startLateralPos2, endLateralPos2, startWidth, endWidth, laneType,
                        longitudinalDirectionality, f2000, speedLimit);
        // Verify the easy bits
        assertEquals("Capacity should be " + f2000, f2000.getSI(), lane2.getCapacity().getSI(), 0.001);
        assertEquals("PrevLanes should be empty", 0, lane2.prevLanes().size());
        assertEquals("NextLanes should be empty", 0, lane2.nextLanes().size());
        approximateLengthOfContour =
                2 * (coordinates[0].distance(coordinates[1]) + coordinates[1].distance(coordinates[2]))
                        + startWidth.getSI() + endWidth.getSI();
        assertEquals("Length of contour is approximately " + approximateLengthOfContour, approximateLengthOfContour,
                lane2.getContour().getLength(), 12); // This lane takes a path that is about 11 meters shorter
        assertEquals("Directionality should be " + longitudinalDirectionality, longitudinalDirectionality,
                lane2.getDirectionality());
        assertEquals("There should be no GTUs on the lane", 0, lane2.getGtuList().size());
        assertEquals("LaneType should be " + laneType, laneType, lane2.getLaneType());
        sensors =
                lane2.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER));
        assertEquals("The lane should have two sensors", 2, sensors.size());
        sensors =
                lane2.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(0.1, LengthUnit.METER));
        assertEquals("There should be one sensor at the start of the lane", 1, sensors.size());
        assertTrue("The sensor at the start of the lane should be a SensorLaneStart",
                sensors.get(0) instanceof SensorLaneStart);
        assertEquals("This sensor should be at 0m", 0, sensors.get(0).getLongitudinalPosition().getSI(), 0.00001);
        assertEquals("This sensor should be at 0m", 0, sensors.get(0).getLongitudinalPositionSI(), 0.00001);
        sensors =
                lane2.getSensors(new DoubleScalar.Rel<LengthUnit>(lane2.getLength().getSI() - 1, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER));
        assertEquals("There should be one sensor at the end of the lane", 1, sensors.size());
        assertTrue("The sensor at the start of the lane should be a SensorLaneEnd",
                sensors.get(0) instanceof SensorLaneEnd);
        assertEquals("This sensor should be at the end of the lane", lane2.getLength().getSI(), sensors.get(0)
                .getLongitudinalPosition().getSI(), 0.01);
        assertEquals("This sensor should be at the end of the lane", lane2.getLength().getSI(), sensors.get(0)
                .getLongitudinalPositionSI(), 0.01);
    }

    /**
     * Test that the contour of a constructed lane covers the expected area. Tests are only performed for straight
     * lanes, but the orientation of the link and the offset of the lane from the link is varied in many ways.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void contourTest() throws Exception
    {
        final int[] startPositions = {0, 1, -1, 20, -20};
        final double[] angles =
                {0, Math.PI * 0.01, Math.PI / 3, Math.PI / 2, Math.PI * 2 / 3, Math.PI * 0.99, Math.PI, Math.PI * 1.01,
                        Math.PI * 4 / 3, Math.PI * 3 / 2, Math.PI * 1.99, Math.PI * 2, Math.PI * (-0.2)};
        DoubleScalar.Abs<FrequencyUnit> f2000 = new DoubleScalar.Abs<FrequencyUnit>(2000, FrequencyUnit.PER_HOUR);
        LongitudinalDirectionality longitudinalDirectionality = LongitudinalDirectionality.FORWARD;
        LaneType<String> laneType = new LaneType<String>("Car");
        DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(50, SpeedUnit.KM_PER_HOUR);
        for (int xStart : startPositions)
        {
            for (int yStart : startPositions)
            {
                for (double angle : angles)
                {
                    NodeGeotools.STR start = new NodeGeotools.STR("start", new Coordinate(xStart, yStart));
                    double linkLength = 1000;
                    double xEnd = xStart + linkLength * Math.cos(angle);
                    double yEnd = yStart + linkLength * Math.sin(angle);
                    NodeGeotools.STR end = new NodeGeotools.STR("end", new Coordinate(xEnd, yEnd));
                    Coordinate[] coordinates = new Coordinate[2];
                    coordinates[0] = start.getPoint();
                    coordinates[1] = end.getPoint();
                    GeometryFactory factory = new GeometryFactory();
                    LineString lineString = factory.createLineString(coordinates);
                    CrossSectionLink<?, ?> link =
                            new CrossSectionLink<String, String>("A to B", start, end,
                                    new DoubleScalar.Rel<LengthUnit>(lineString.getLength(), LengthUnit.METER));
                    new LinearGeometry(link, lineString, null);
                    final int[] lateralOffsets = {-10, -3, -1, 0, 1, 3, 10};
                    for (int startLateralOffset : lateralOffsets)
                    {
                        for (int endLateralOffset : lateralOffsets)
                        {
                            int startWidth = 4; // This one is not varied
                            for (int endWidth : new int[]{2, 4, 6})
                            {
                                // Now we can construct a Lane
                                Lane lane =
                                        new Lane(link, new DoubleScalar.Rel<LengthUnit>(startLateralOffset,
                                                LengthUnit.METER), new DoubleScalar.Rel<LengthUnit>(endLateralOffset,
                                                LengthUnit.METER), new DoubleScalar.Rel<LengthUnit>(startWidth,
                                                LengthUnit.METER), new DoubleScalar.Rel<LengthUnit>(endWidth,
                                                LengthUnit.METER), laneType, longitudinalDirectionality, f2000,
                                                speedLimit);
                                final Geometry geometry = lane.getContour();
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
                                checkInside(lane, link.getLength().getSI() - 1, endLateralOffset - endWidth / 2 - 1,
                                        false);
                                // One meter before the end, right outside the lane
                                checkInside(lane, link.getLength().getSI() - 1, endLateralOffset + endWidth / 2 + 1,
                                        false);
                                // TODO check the result of getBounds (will have to wait until PK knows what that result
                                // is supposed to be)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Verify that a point at specified distance along and across from the design line of the parent Link of a Lane is
     * inside c.q. outside the contour of a Lane. The test uses an implementation that is as independent as possible of
     * the Geometry class methods.
     * @param lane Lane; the lane
     * @param longitudinal double; the longitudinal position along the design line of the parent Link of the Lane. This
     *            design line is expected to be straight and the longitudinal position may be negative (indicating a
     *            point before the start of the Link) and it may exceed the length of the Link (indicating a point
     *            beyond the end of the Link)
     * @param lateral double; the lateral offset from the design line of the link (positive is left, negative is right)
     * @param expectedResult boolean; true if the calling method expects the point to be within the contour of the Lane,
     *            false if the calling method expects the point to be outside the contour of the Lane
     */
    private void checkInside(Lane lane, double longitudinal, double lateral, boolean expectedResult)
    {
        CrossSectionLink<?, ?> parentLink = lane.getParentLink();
        AbstractNode<?, ?> start = parentLink.getStartNode();
        AbstractNode<?, ?> end = parentLink.getEndNode();
        double startX = start.getX();
        double startY = start.getY();
        double endX = end.getX();
        double endY = end.getY();
        double length = Math.sqrt((endX - startX) * (endX - startX) + (endY - startY) * (endY - startY));
        double ratio = longitudinal / length;
        double designLineX = startX + (endX - startX) * ratio;
        double designLineY = startY + (endY - startY) * ratio;
        double lateralAngle = Math.atan2(endY - startY, endX - startX) + Math.PI / 2;
        double px = designLineX + lateral * Math.cos(lateralAngle);
        double py = designLineY + lateral * Math.sin(lateralAngle);
        Geometry contour = lane.getContour();
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
     * Algorithm of W. Randolph Franklin http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html, found
     * via stackoverflow.com: http://stackoverflow.com/questions/217578/point-in-polygon-aka-hit-test.
     * @param point Coordinate; the point
     * @param polygon Coordinate[]; the polygon (last coordinate is allowed to be identical to the first, but his is not
     *            a requirement)
     * @return boolean; true if the point is inside the polygon; false if it is outside the polygon; if the point lies
     *         <b>on</b> an vertex or edge of the polygon the result is (of course) undefined
     */
    private boolean pointInsidePolygon(Coordinate point, Coordinate[] polygon)
    {
        boolean result = false;
        for (int i = 0, j = polygon.length - 1; i < polygon.length; j = i++)
        {
            if ((polygon[i].y > point.y) != (polygon[j].y > point.y)
                    && point.x < (polygon[j].x - polygon[i].x) * (point.y - polygon[i].y)
                            / (polygon[j].y - polygon[i].y) + polygon[i].x)
            {
                result = !result;
            }
        }
        return result;
    }

}

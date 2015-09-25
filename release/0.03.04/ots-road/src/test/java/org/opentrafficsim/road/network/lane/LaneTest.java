package org.opentrafficsim.road.network.lane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.junit.Test;
import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Test the Lane class.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-16 19:20:07 +0200 (Wed, 16 Sep 2015) $, @version $Revision: 1405 $, by $Author: averbraeck $,
 * initial version 21 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneTest implements OTS_SCALAR
{
    /**
     * Test the constructor.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void laneConstructorTest() throws Exception
    {
        // First we need two Nodes
        OTSNode nodeFrom = new OTSNode("A", new OTSPoint3D(0, 0, 0));
        OTSNode nodeTo = new OTSNode("B", new OTSPoint3D(1000, 0, 0));
        // Now we can make a Link
        OTSPoint3D[] coordinates = new OTSPoint3D[2];
        coordinates[0] = new OTSPoint3D(nodeFrom.getPoint().x, nodeFrom.getPoint().y, 0);
        coordinates[1] = new OTSPoint3D(nodeTo.getPoint().x, nodeTo.getPoint().y, 0);
        CrossSectionLink link =
            new CrossSectionLink("A to B", nodeFrom, nodeTo, new OTSLine3D(coordinates), LaneKeepingPolicy.KEEP_RIGHT);
        Length.Rel startLateralPos = new Length.Rel(2, METER);
        Length.Rel endLateralPos = new Length.Rel(5, METER);
        Length.Rel startWidth = new Length.Rel(3, METER);
        Length.Rel endWidth = new Length.Rel(4, METER);
        GTUType gtuTypeCar = GTUType.makeGTUType("Car");
        GTUType gtuTypeTruck = GTUType.makeGTUType("Truck");
        LaneType laneType = new LaneType("Car");
        laneType.addCompatibility(gtuTypeCar);
        laneType.addCompatibility(gtuTypeTruck);
        Map<GTUType, LongitudinalDirectionality> directionalityMap = new LinkedHashMap<>();
        directionalityMap.put(GTUType.ALL, LongitudinalDirectionality.FORWARD);
        Map<GTUType, Speed.Abs> speedMap = new LinkedHashMap<>();
        speedMap.put(GTUType.ALL, new Speed.Abs(100, KM_PER_HOUR));
        // Now we can construct a Lane
        // FIXME what overtaking conditions do we ant to test in this unit test?
        Lane lane =
            new Lane(link, "lane", startLateralPos, endLateralPos, startWidth, endWidth, laneType, directionalityMap,
                speedMap, new OvertakingConditions.LeftAndRight());
        // Verify the easy bits
        assertEquals("PrevLanes should be empty", 0, lane.prevLanes(gtuTypeCar).size()); // this one caught a bug!
        assertEquals("NextLanes should be empty", 0, lane.nextLanes(gtuTypeCar).size());
        double approximateLengthOfContour =
            2 * nodeFrom.getPoint().distanceSI(nodeTo.getPoint()) + startWidth.getSI() + endWidth.getSI();
        assertEquals("Length of contour is approximately " + approximateLengthOfContour, approximateLengthOfContour,
            lane.getContour().getLengthSI(), 0.1);
        assertEquals("Directionality should be " + LongitudinalDirectionality.FORWARD,
            LongitudinalDirectionality.FORWARD, lane.getDirectionality(GTUType.ALL));
        assertEquals("SpeedLimit should be " + (new Speed.Abs(100, KM_PER_HOUR)), new Speed.Abs(100, KM_PER_HOUR), lane
            .getSpeedLimit(GTUType.ALL));
        assertEquals("There should be no GTUs on the lane", 0, lane.getGtuList().size());
        assertEquals("LaneType should be " + laneType, laneType, lane.getLaneType());
        for (int i = 0; i < 10; i++)
        {
            double expectedLateralCenterOffset =
                startLateralPos.getSI() + (endLateralPos.getSI() - startLateralPos.getSI()) * i / 10;
            assertEquals(String.format("Lateral offset at %d%% should be %.3fm", 10 * i, expectedLateralCenterOffset),
                expectedLateralCenterOffset, lane.getLateralCenterPosition(i / 10.0).getSI(), 0.01);
            Length.Rel longitudinalPosition = new Length.Rel(lane.getLength().getSI() * i / 10, METER);
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
                expectedLeftOffset, lane.getLateralBoundaryPosition(LateralDirectionality.LEFT, longitudinalPosition)
                    .getSI(), 0.001);
            double expectedRightOffset = expectedLateralCenterOffset + expectedWidth / 2;
            assertEquals(String.format("Right edge at %d%% should be %.3fm", 10 * i, expectedRightOffset),
                expectedRightOffset, lane.getLateralBoundaryPosition(LateralDirectionality.RIGHT, i / 10.0).getSI(),
                0.001);
            assertEquals("Right edge at " + longitudinalPosition + " should be " + expectedRightOffset,
                expectedRightOffset, lane.getLateralBoundaryPosition(LateralDirectionality.RIGHT, longitudinalPosition)
                    .getSI(), 0.001);
        }

        // Harder case; create a Link with form points along the way
        // System.out.println("Constructing Link and Lane with one form point");
        coordinates = new OTSPoint3D[3];
        coordinates[0] = new OTSPoint3D(nodeFrom.getPoint().x, nodeFrom.getPoint().y, 0);
        coordinates[1] = new OTSPoint3D(200, 100);
        coordinates[2] = new OTSPoint3D(nodeTo.getPoint().x, nodeTo.getPoint().y, 0);
        link =
            new CrossSectionLink("A to B with Kink", nodeFrom, nodeTo, new OTSLine3D(coordinates),
                LaneKeepingPolicy.KEEP_RIGHT);
        // FIXME what overtaking conditions do we ant to test in this unit test?
        lane =
            new Lane(link, "lane.1", startLateralPos, endLateralPos, startWidth, endWidth, laneType, directionalityMap,
                speedMap, new OvertakingConditions.LeftAndRight());
        // Verify the easy bits
        assertEquals("PrevLanes should be empty", 0, lane.prevLanes(gtuTypeCar).size());
        assertEquals("NextLanes should be empty", 0, lane.nextLanes(gtuTypeCar).size());
        approximateLengthOfContour =
            2 * (coordinates[0].distanceSI(coordinates[1]) + coordinates[1].distanceSI(coordinates[2]))
                + startWidth.getSI() + endWidth.getSI();
        assertEquals("Length of contour is approximately " + approximateLengthOfContour, approximateLengthOfContour,
            lane.getContour().getLengthSI(), 4); // This lane takes a path that is about 3m longer
        assertEquals("There should be no GTUs on the lane", 0, lane.getGtuList().size());
        assertEquals("LaneType should be " + laneType, laneType, lane.getLaneType());
        // System.out.println("Add another Lane at the inside of the corner in the design line");
        Length.Rel startLateralPos2 = new Length.Rel(-8, METER);
        Length.Rel endLateralPos2 = new Length.Rel(-5, METER);
        // FIXME what overtaking conditions do we ant to test in this unit test?
        Lane lane2 =
            new Lane(link, "lane.2", startLateralPos2, endLateralPos2, startWidth, endWidth, laneType,
                directionalityMap, speedMap, new OvertakingConditions.LeftAndRight());
        // Verify the easy bits
        assertEquals("PrevLanes should be empty", 0, lane2.prevLanes(gtuTypeCar).size());
        assertEquals("NextLanes should be empty", 0, lane2.nextLanes(gtuTypeCar).size());
        approximateLengthOfContour =
            2 * (coordinates[0].distanceSI(coordinates[1]) + coordinates[1].distanceSI(coordinates[2]))
                + startWidth.getSI() + endWidth.getSI();
        assertEquals("Length of contour is approximately " + approximateLengthOfContour, approximateLengthOfContour,
            lane2.getContour().getLengthSI(), 12); // This lane takes a path that is about 11 meters shorter
        assertEquals("There should be no GTUs on the lane", 0, lane2.getGtuList().size());
        assertEquals("LaneType should be " + laneType, laneType, lane2.getLaneType());
    }

    /**
     * Test that the contour of a constructed lane covers the expected area. Tests are only performed for straight lanes, but
     * the orientation of the link and the offset of the lane from the link is varied in many ways.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void contourTest() throws Exception
    {
        final int[] startPositions = {0, 1, -1, 20, -20};
        final double[] angles =
            {0, Math.PI * 0.01, Math.PI / 3, Math.PI / 2, Math.PI * 2 / 3, Math.PI * 0.99, Math.PI, Math.PI * 1.01,
                Math.PI * 4 / 3, Math.PI * 3 / 2, Math.PI * 1.99, Math.PI * 2, Math.PI * (-0.2)};
        LaneType laneType = new LaneType("Car");
        Map<GTUType, LongitudinalDirectionality> directionalityMap = new LinkedHashMap<>();
        directionalityMap.put(GTUType.ALL, LongitudinalDirectionality.FORWARD);
        Map<GTUType, Speed.Abs> speedMap = new LinkedHashMap<>();
        speedMap.put(GTUType.ALL, new Speed.Abs(50, KM_PER_HOUR));
        int laneNum = 0;
        for (int xStart : startPositions)
        {
            for (int yStart : startPositions)
            {
                for (double angle : angles)
                {
                    OTSNode start = new OTSNode("start", new OTSPoint3D(xStart, yStart));
                    double linkLength = 1000;
                    double xEnd = xStart + linkLength * Math.cos(angle);
                    double yEnd = yStart + linkLength * Math.sin(angle);
                    OTSNode end = new OTSNode("end", new OTSPoint3D(xEnd, yEnd));
                    OTSPoint3D[] coordinates = new OTSPoint3D[2];
                    coordinates[0] = start.getPoint();
                    coordinates[1] = end.getPoint();
                    OTSLine3D line = new OTSLine3D(coordinates);
                    CrossSectionLink link =
                        new CrossSectionLink("A to B", start, end, line, LaneKeepingPolicy.KEEP_RIGHT);
                    final int[] lateralOffsets = {-10, -3, -1, 0, 1, 3, 10};
                    for (int startLateralOffset : lateralOffsets)
                    {
                        for (int endLateralOffset : lateralOffsets)
                        {
                            int startWidth = 4; // This one is not varied
                            for (int endWidth : new int[]{2, 4, 6})
                            {
                                // Now we can construct a Lane
                                // FIXME what overtaking conditions do we ant to test in this unit test?
                                Lane lane =
                                    new Lane(link, "lane." + ++laneNum, new Length.Rel(startLateralOffset, METER),
                                        new Length.Rel(endLateralOffset, METER), new Length.Rel(startWidth, METER),
                                        new Length.Rel(endWidth, METER), laneType, directionalityMap, speedMap,
                                        new OvertakingConditions.LeftAndRight());
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
                                checkInside(lane, link.getLength().getSI() - 1, endLateralOffset - endWidth / 2 - 1,
                                    false);
                                // One meter before the end, right outside the lane
                                checkInside(lane, link.getLength().getSI() - 1, endLateralOffset + endWidth / 2 + 1,
                                    false);
                                // Check the result of getBounds.
                                DirectedPoint l = lane.getLocation();
                                Bounds bb = lane.getBounds();
                                // System.out.println("bb is " + bb);
                                // System.out.println("l is " + l.x + "," + l.y + "," + l.z);
                                // System.out.println("start is at " + start.getX() + ", " + start.getY());
                                // System.out.println("  end is at " + end.getX() + ", " + end.getY());
                                Point2D.Double[] cornerPoints = new Point2D.Double[4];
                                cornerPoints[0] =
                                    new Point2D.Double(
                                        xStart - (startLateralOffset + startWidth / 2) * Math.sin(angle), yStart
                                            + (startLateralOffset + startWidth / 2) * Math.cos(angle));
                                cornerPoints[1] =
                                    new Point2D.Double(
                                        xStart - (startLateralOffset - startWidth / 2) * Math.sin(angle), yStart
                                            + (startLateralOffset - startWidth / 2) * Math.cos(angle));
                                cornerPoints[2] =
                                    new Point2D.Double(xEnd - (endLateralOffset + endWidth / 2) * Math.sin(angle), yEnd
                                        + (endLateralOffset + endWidth / 2) * Math.cos(angle));
                                cornerPoints[3] =
                                    new Point2D.Double(xEnd - (endLateralOffset - endWidth / 2) * Math.sin(angle), yEnd
                                        + (endLateralOffset - endWidth / 2) * Math.cos(angle));
                                for (int i = 0; i < cornerPoints.length; i++)
                                {
                                    // System.out.println("p" + i + ": " + cornerPoints[i].x + "," + cornerPoints[i].y);
                                }
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
                                Point3d bbLow = new Point3d();
                                ((BoundingBox) bb).getLower(bbLow);
                                Point3d bbHigh = new Point3d();
                                ((BoundingBox) bb).getUpper(bbHigh);
                                // System.out.println(" my bbox is " + minX + "," + minY + " - " + maxX + "," + maxY);
                                // System.out.println("the bbox is " + (bbLow.x + l.x) + "," + (bbLow.y + l.y) + " - "
                                // + (bbHigh.x + l.x) + "," + (bbHigh.y + l.y));
                                double boundsMinX = bbLow.x + l.x;
                                double boundsMinY = bbLow.y + l.y;
                                double boundsMaxX = bbHigh.x + l.x;
                                double boundsMaxY = bbHigh.y + l.y;
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
    private void checkInside(final Lane lane, final double longitudinal, final double lateral,
        final boolean expectedResult)
    {
        CrossSectionLink parentLink = lane.getParentLink();
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
     * stackoverflow.com: http://stackoverflow.com/questions/217578/point-in-polygon-aka-hit-test.
     * @param point Coordinate; the point
     * @param polygon OTSPoint3D[]; the polygon (last coordinate is allowed to be identical to the first, but his is not a
     *            requirement)
     * @return boolean; true if the point is inside the polygon; false if it is outside the polygon; if the point lies <b>on</b>
     *         an vertex or edge of the polygon the result is (of course) undefined
     */
    private boolean pointInsidePolygon(Coordinate point, Coordinate[] polygon)
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

}

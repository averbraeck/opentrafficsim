package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.List;

import org.djutils.logger.CategoryLogger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.linearref.LengthIndexedLine;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class OTSBufferingJTS
{
    /**
     * Utility class.
     */
    private OTSBufferingJTS()
    {
        // cannot be instantiated.
    }

    /**
     * Compute the distance of a line segment to a point. If the the projected points lies outside the line segment, the nearest
     * end point of the line segment is returned. Otherwise the point return lies between the end points of the line segment.
     * <br>
     * Adapted from <a href="http://paulbourke.net/geometry/pointlineplane/DistancePoint.java"> example code provided by Paul
     * Bourke</a>.
     * @param lineP1 OTSPoint3D; start of line segment
     * @param lineP2 OTSPoint3D; end of line segment
     * @param point OTSPoint3D; Point to project onto the line segment
     * @return double; the distance of the projected point or one of the end points of the line segment to the point
     */
    public static double distanceLineSegmentToPoint(final OTSPoint3D lineP1, final OTSPoint3D lineP2, final OTSPoint3D point)
    {
        return closestPointOnSegmentToPoint(lineP1, lineP2, point).distanceSI(point);
    }

    /**
     * Project a point on a line (2D). If the the projected points lies outside the line segment, the nearest end point of the
     * line segment is returned. Otherwise the point return lies between the end points of the line segment. <br>
     * Adapted from <a href="http://paulbourke.net/geometry/pointlineplane/DistancePoint.java"> example code provided by Paul
     * Bourke</a>.
     * @param lineP1 OTSPoint3D; start of line segment
     * @param lineP2 OTSPoint3D; end of line segment
     * @param point OTSPoint3D; Point to project onto the line segment
     * @return Point2D.Double; either <cite>lineP1</cite>, or <cite>lineP2</cite> or a new OTSPoint3D that lies somewhere in
     *         between those two
     */
    public static OTSPoint3D closestPointOnSegmentToPoint(final OTSPoint3D lineP1, final OTSPoint3D lineP2,
            final OTSPoint3D point)
    {
        double dX = lineP2.x - lineP1.x;
        double dY = lineP2.y - lineP1.y;
        if ((0 == dX) && (0 == dY))
        {
            return lineP1;
        }
        final double u = ((point.x - lineP1.x) * dX + (point.y - lineP1.y) * dY) / (dX * dX + dY * dY);
        if (u < 0)
        {
            return lineP1;
        }
        else if (u > 1)
        {
            return lineP2;
        }
        else
        {
            return new OTSPoint3D(lineP1.x + u * dX, lineP1.y + u * dY); // could use interpolate in stead
        }
    }

    /**
     * Construct parallel line without.
     * @param referenceLine OTSLine3D; the reference line
     * @param offset double; offset distance from the reference line; positive is LEFT, negative is RIGHT
     * @return OTSLine3D; the line that has the specified offset from the reference line
     */
    public static OTSLine3D offsetLine(final OTSLine3D referenceLine, final double offset)
    {
        try
        {
            double bufferOffset = Math.abs(offset);
            final double precision = 0.00001;
            if (bufferOffset < precision)
            {
                return referenceLine; // It is immutable; so we can safely return the original
            }
            final double circlePrecision = 0.001;
            List<OTSPoint3D> points = new ArrayList<>();
            // Make good use of the fact that an OTSLine3D cannot have consecutive duplicate points and has > 1 points
            OTSPoint3D prevPoint = referenceLine.get(0);
            Double prevAngle = null;
            for (int index = 0; index < referenceLine.size() - 1; index++)
            {
                OTSPoint3D nextPoint = referenceLine.get(index + 1);
                double angle = Math.atan2(nextPoint.y - prevPoint.y, nextPoint.x - prevPoint.x);
                OTSPoint3D segmentFrom =
                        new OTSPoint3D(prevPoint.x - Math.sin(angle) * offset, prevPoint.y + Math.cos(angle) * offset);
                OTSPoint3D segmentTo =
                        new OTSPoint3D(nextPoint.x - Math.sin(angle) * offset, nextPoint.y + Math.cos(angle) * offset);
                if (index > 0)
                {
                    double deltaAngle = angle - prevAngle;
                    if (Math.abs(deltaAngle) > Math.PI)
                    {
                        deltaAngle -= Math.signum(deltaAngle) * 2 * Math.PI;
                    }
                    if (deltaAngle * offset > 0)
                    {
                        // Inside of curve of reference line.
                        // Add the intersection point of each previous segment and the next segment
                        OTSPoint3D pPoint = null;
                        for (int i = 0; i < points.size(); i++)
                        {
                            OTSPoint3D p = points.get(i);
                            if (Double.isNaN(p.z))
                            {
                                continue; // skip this one
                            }
                            if (null != pPoint)
                            {
                                double pAngle = Math.atan2(p.y - pPoint.y, p.x - pPoint.x);
                                double totalAngle = angle - pAngle;
                                if (Math.abs(totalAngle) > Math.PI)
                                {
                                    totalAngle += Math.signum(totalAngle) * 2 * Math.PI;
                                }
                                if (Math.abs(totalAngle) > 0.01)
                                {
                                    // CategoryLogger.trace(Cat.CORE, "preceding segment " + pPoint + " to " + p + ", this
                                    // segment "
                                    // + segmentFrom + " to " + segmentTo + " totalAngle " + totalAngle);
                                    OTSPoint3D intermediatePoint =
                                            intersectionOfLineSegments(pPoint, p, segmentFrom, segmentTo);
                                    if (null != intermediatePoint)
                                    {
                                        // mark it as added point at inside corner
                                        intermediatePoint =
                                                new OTSPoint3D(intermediatePoint.x, intermediatePoint.y, Double.NaN);
                                        // CategoryLogger.trace(Cat.CORE, "Inserting intersection of preceding segment and this
                                        // "
                                        // + "segment " + intermediatePoint);
                                        points.add(intermediatePoint);
                                    }
                                }
                            }
                            pPoint = p;
                        }
                    }
                    else
                    {
                        // Outside of curve of reference line
                        // Approximate an arc using straight segments.
                        // Determine how many segments are needed.
                        int numSegments = 1;
                        if (Math.abs(deltaAngle) > Math.PI / 2)
                        {
                            numSegments = 2;
                        }
                        for (; numSegments < 1000; numSegments *= 2)
                        {
                            double maxError = bufferOffset * (1 - Math.abs(Math.cos(deltaAngle / numSegments / 2)));
                            if (maxError < circlePrecision)
                            {
                                break; // required precision reached
                            }
                        }
                        // Generate the intermediate points
                        for (int additionalPoint = 1; additionalPoint < numSegments; additionalPoint++)
                        {
                            double intermediateAngle =
                                    (additionalPoint * angle + (numSegments - additionalPoint) * prevAngle) / numSegments;
                            if (prevAngle * angle < 0 && Math.abs(prevAngle) > Math.PI / 2 && Math.abs(angle) > Math.PI / 2)
                            {
                                intermediateAngle += Math.PI;
                            }
                            OTSPoint3D intermediatePoint = new OTSPoint3D(prevPoint.x - Math.sin(intermediateAngle) * offset,
                                    prevPoint.y + Math.cos(intermediateAngle) * offset);
                            // CategoryLogger.trace(Cat.CORE, "inserting intermediate point " + intermediatePoint + " for angle
                            // "
                            // + Math.toDegrees(intermediateAngle));
                            points.add(intermediatePoint);
                        }
                    }
                }
                points.add(segmentFrom);
                points.add(segmentTo);
                prevPoint = nextPoint;
                prevAngle = angle;
            }
            // CategoryLogger.trace(Cat.CORE, OTSGeometry.printCoordinates("#before cleanup: \nc0,0,0\n#", new
            // OTSLine3D(points), "\n
            // "));
            // Remove points that are closer than the specified offset
            for (int index = 1; index < points.size() - 1; index++)
            {
                OTSPoint3D checkPoint = points.get(index);
                prevPoint = null;
                boolean tooClose = false;
                boolean somewhereAtCorrectDistance = false;
                for (int i = 0; i < referenceLine.size(); i++)
                {
                    OTSPoint3D p = referenceLine.get(i);
                    if (null != prevPoint)
                    {
                        OTSPoint3D closestPoint = closestPointOnSegmentToPoint(prevPoint, p, checkPoint);
                        if (closestPoint != referenceLine.get(0) && closestPoint != referenceLine.get(referenceLine.size() - 1))
                        {
                            double distance = closestPoint.horizontalDistanceSI(checkPoint);
                            if (distance < bufferOffset - circlePrecision)
                            {
                                // CategoryLogger.trace(Cat.CORE, "point " + checkPoint + " inside buffer (distance is " +
                                // distance +
                                // ")");
                                tooClose = true;
                                break;
                            }
                            else if (distance < bufferOffset + precision)
                            {
                                somewhereAtCorrectDistance = true;
                            }
                        }
                    }
                    prevPoint = p;
                }
                if (tooClose || !somewhereAtCorrectDistance)
                {
                    // CategoryLogger.trace(Cat.CORE, "Removing " + checkPoint);
                    points.remove(index);
                    index--;
                }
            }
            // Fix the z-coordinate of all points that were added as intersections of segments.
            for (int index = 0; index < points.size(); index++)
            {
                OTSPoint3D p = points.get(index);
                if (Double.isNaN(p.z))
                {
                    points.set(index, new OTSPoint3D(p.x, p.y, 0));
                }
            }
            return OTSLine3D.createAndCleanOTSLine3D(points);
        }
        catch (OTSGeometryException exception)
        {
            CategoryLogger.always().error(exception, "Exception in offsetLine - should never happen");
            return null;
        }
    }

    /**
     * Compute the 2D intersection of two line segments. Both line segments are defined by two points (that should be distinct).
     * @param line1P1 OTSPoint3D; first point of line 1
     * @param line1P2 OTSPoint3D; second point of line 1
     * @param line2P1 OTSPoint3D; first point of line 2
     * @param line2P2 OTSPoint3D; second point of line 2
     * @return OTSPoint3D; the intersection of the two lines, or null if the lines are (almost) parallel, or do not intersect
     */
    private static OTSPoint3D intersectionOfLineSegments(final OTSPoint3D line1P1, final OTSPoint3D line1P2,
            final OTSPoint3D line2P1, final OTSPoint3D line2P2)
    {
        double denominator =
                (line2P2.y - line2P1.y) * (line1P2.x - line1P1.x) - (line2P2.x - line2P1.x) * (line1P2.y - line1P1.y);
        if (denominator == 0f)
        {
            return null; // lines are parallel (they might even be on top of each other, but we don't check that)
        }
        double uA = ((line2P2.x - line2P1.x) * (line1P1.y - line2P1.y) - (line2P2.y - line2P1.y) * (line1P1.x - line2P1.x))
                / denominator;
        if ((uA < 0f) || (uA > 1f))
        {
            return null; // intersection outside line 1
        }
        double uB = ((line1P2.x - line1P1.x) * (line1P1.y - line2P1.y) - (line1P2.y - line1P1.y) * (line1P1.x - line2P1.x))
                / denominator;
        if (uB < 0 || uB > 1)
        {
            return null; // intersection outside line 2
        }
        return new OTSPoint3D(line1P1.x + uA * (line1P2.x - line1P1.x), line1P1.y + uA * (line1P2.y - line1P1.y), 0);
    }

    /**
     * Create a line at linearly varying offset from a reference line. The offset may change linearly from its initial value at
     * the start of the reference line to its final offset value at the end of the reference line.
     * @param referenceLine OTSLine3D; the Geometry of the reference line
     * @param offsetAtStart double; offset at the start of the reference line (positive value is Left, negative value is Right)
     * @param offsetAtEnd double; offset at the end of the reference line (positive value is Left, negative value is Right)
     * @return Geometry; the Geometry of the line at linearly changing offset of the reference line
     * @throws OTSGeometryException when this method fails to create the offset line
     */
    public static OTSLine3D offsetLine(final OTSLine3D referenceLine, final double offsetAtStart, final double offsetAtEnd)
            throws OTSGeometryException
    {
        // CategoryLogger.trace(Cat.CORE, OTSGeometry.printCoordinates("#referenceLine: \nc1,0,0\n# offset at start is " +
        // offsetAtStart + " at end is " + offsetAtEnd + "\n#", referenceLine, "\n "));

        OTSLine3D offsetLineAtStart = offsetLine(referenceLine, offsetAtStart);
        if (offsetAtStart == offsetAtEnd)
        {
            return offsetLineAtStart; // offset does not change
        }
        // CategoryLogger.trace(Cat.CORE, OTSGeometry.printCoordinates("#offset line at start: \nc0,0,0\n#", offsetLineAtStart,
        // "\n"));
        OTSLine3D offsetLineAtEnd = offsetLine(referenceLine, offsetAtEnd);
        // CategoryLogger.trace(Cat.CORE, OTSGeometry.printCoordinates("#offset line at end: \nc0.7,0.7,0.7\n#",
        // offsetLineAtEnd,
        // "\n"));
        Geometry startGeometry = offsetLineAtStart.getLineString();
        Geometry endGeometry = offsetLineAtEnd.getLineString();
        LengthIndexedLine first = new LengthIndexedLine(startGeometry);
        double firstLength = startGeometry.getLength();
        LengthIndexedLine second = new LengthIndexedLine(endGeometry);
        double secondLength = endGeometry.getLength();
        ArrayList<Coordinate> out = new ArrayList<Coordinate>();
        Coordinate[] firstCoordinates = startGeometry.getCoordinates();
        Coordinate[] secondCoordinates = endGeometry.getCoordinates();
        int firstIndex = 0;
        int secondIndex = 0;
        Coordinate prevCoordinate = null;
        final double tooClose = 0.05; // 5 cm
        while (firstIndex < firstCoordinates.length && secondIndex < secondCoordinates.length)
        {
            double firstRatio = firstIndex < firstCoordinates.length ? first.indexOf(firstCoordinates[firstIndex]) / firstLength
                    : Double.MAX_VALUE;
            double secondRatio = secondIndex < secondCoordinates.length
                    ? second.indexOf(secondCoordinates[secondIndex]) / secondLength : Double.MAX_VALUE;
            double ratio;
            if (firstRatio < secondRatio)
            {
                ratio = firstRatio;
                firstIndex++;
            }
            else
            {
                ratio = secondRatio;
                secondIndex++;
            }
            Coordinate firstCoordinate = first.extractPoint(ratio * firstLength);
            Coordinate secondCoordinate = second.extractPoint(ratio * secondLength);
            Coordinate resultCoordinate = new Coordinate((1 - ratio) * firstCoordinate.x + ratio * secondCoordinate.x,
                    (1 - ratio) * firstCoordinate.y + ratio * secondCoordinate.y);
            if (null == prevCoordinate || resultCoordinate.distance(prevCoordinate) > tooClose)
            {
                out.add(resultCoordinate);
                prevCoordinate = resultCoordinate;
            }
        }
        Coordinate[] resultCoordinates = new Coordinate[out.size()];
        for (int index = 0; index < out.size(); index++)
        {
            resultCoordinates[index] = out.get(index);
        }
        return new OTSLine3D(resultCoordinates);
    }

}

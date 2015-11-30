package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentrafficsim.core.network.NetworkException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class OTSBufferingJTS
{
    /** Precision of buffer operations. */
    private static final int QUADRANTSEGMENTS = 16;

    /**
     * 
     */
    private OTSBufferingJTS()
    {
        // cannot be instantiated.
    }

    /**
     * normalize an angle between 0 and 2 * PI.
     * @param angle original angle.
     * @return angle between 0 and 2 * PI.
     */
    private static double norm(final double angle)
    {
        double normalized = angle % (2 * Math.PI);
        if (normalized < 0.0)
        {
            normalized += 2 * Math.PI;
        }
        return normalized;
    }

    /**
     * @param c1 first coordinate
     * @param c2 second coordinate
     * @return the normalized angle of the line between c1 and c2
     */
    private static double angle(final Coordinate c1, final Coordinate c2)
    {
        return norm(Math.atan2(c2.y - c1.y, c2.x - c1.x));
    }

    /**
     * Compute the distance of a line segment to a point. If the the projected points lies outside the line segment, the nearest
     * end point of the line segment is returned. Otherwise the point return lies between the end points of the line segment. <br />
     * Adapted from <a href="http://paulbourke.net/geometry/pointlineplane/DistancePoint.java"> example code provided by Paul
     * Bourke</a>.
     * @param lineP1 OTSPoint3D; start of line segment
     * @param lineP2 OTSPoint3D; end of line segment
     * @param point Point to project onto the line segment
     * @return double; the distance of the projected point or one of the end points of the line segment to the point
     */
    public static double distanceLineSegmentToPoint(OTSPoint3D lineP1, OTSPoint3D lineP2, OTSPoint3D point)
    {
        return closestPointOnSegmentToPoint(lineP1, lineP2, point).distanceSI(point);
    }

    /**
     * Project a point on a line (2D). If the the projected points lies outside the line segment, the nearest end point of the
     * line segment is returned. Otherwise the point return lies between the end points of the line segment. <br />
     * Adapted from <a href="http://paulbourke.net/geometry/pointlineplane/DistancePoint.java"> example code provided by Paul
     * Bourke</a>.
     * @param lineP1 OTSPoint3D; start of line segment
     * @param lineP2 OTSPoint3D; end of line segment
     * @param point Point to project onto the line segment
     * @return Point2D.Double; either <cite>lineP1</cite>, or <cite>lineP2</cite> or a new OTSPoint3D that lies somewhere in
     *         between those two
     */
    public static OTSPoint3D closestPointOnSegmentToPoint(OTSPoint3D lineP1, OTSPoint3D lineP2, OTSPoint3D point)
    {
        double dX = lineP2.x - lineP1.x;
        double dY = lineP2.y - lineP1.y;
        if ((0 == dX) && (0 == dY))
            return lineP1;
        final double u = ((point.x - lineP1.x) * dX + (point.y - lineP1.y) * dY) / (dX * dX + dY * dY);
        if (u < 0)
            return lineP1;
        else if (u > 1)
            return lineP2;
        else
            return new OTSPoint3D(lineP1.x + u * dX, lineP1.y + u * dY); // could use interpolate in stead
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
                                continue;// skip this one
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
                                    // System.out.println("preceding segment " + pPoint + " to " + p + ", this segment "
                                    // + segmentFrom + " to " + segmentTo + " totalAngle " + totalAngle);
                                    OTSPoint3D intermediatePoint =
                                        intersectionOfLineSegments(pPoint, p, segmentFrom, segmentTo);
                                    if (null != intermediatePoint)
                                    {
                                        // mark it as added point at inside corner
                                        intermediatePoint =
                                            new OTSPoint3D(intermediatePoint.x, intermediatePoint.y, Double.NaN);
                                        // System.out.println("Inserting intersection of preceding segment and this segment "
                                        // + intermediatePoint);
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
                            if (prevAngle * angle < 0 && Math.abs(prevAngle) > Math.PI / 2
                                && Math.abs(angle) > Math.PI / 2)
                            {
                                intermediateAngle += Math.PI;
                            }
                            OTSPoint3D intermediatePoint =
                                new OTSPoint3D(prevPoint.x - Math.sin(intermediateAngle) * offset, prevPoint.y
                                    + Math.cos(intermediateAngle) * offset);
                            // System.out.println("inserting intermediate point " + intermediatePoint + " for angle "
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
            // System.out.println(OTSGeometry.printCoordinates("#before cleanup: \nc0,0,0\n#", new OTSLine3D(points), "\n   "));
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
                        if (closestPoint != referenceLine.get(0)
                            && closestPoint != referenceLine.get(referenceLine.size() - 1))
                        {
                            double distance = closestPoint.horizontalDistanceSI(checkPoint);
                            if (distance < bufferOffset - circlePrecision)
                            {
                                // System.out.print("point " + checkPoint + " inside buffer (distance is " + distance + ")");
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
                    // System.out.println("Removing " + checkPoint);
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
        catch (OTSGeometryException | NetworkException exception)
        {
            System.err.println("Cannot happen");
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Compute the 2D intersection of two infinite lines. Both lines are defined by two points (that should be distinct).
     * @param line1P1 OTSPoint3D; first point of line 1
     * @param line1P2 OTSPoint3D; second point of line 1
     * @param line2P1 OTSPoint3D; first point of line 2
     * @param line2P2 OTSPoint3D; second point of line 2
     * @return OTSPoint3D; the intersection of the two lines, or null if the lines are (almost) parallel
     */
    private static OTSPoint3D intersectionOfLines(final OTSPoint3D line1P1, final OTSPoint3D line1P2,
        final OTSPoint3D line2P1, final OTSPoint3D line2P2)
    {
        double determinant =
            (line1P1.x - line1P2.x) * (line2P1.y - line2P2.y) - (line1P1.y - line1P2.y) * (line2P1.x - line2P2.x);
        if (Math.abs(determinant) < 0.0000001)
        {
            return null;
        }
        return new OTSPoint3D(
            ((line1P1.x * line1P2.y - line1P1.y * line1P2.x) * (line2P1.x - line2P2.x) - (line1P1.x - line1P2.x)
                * (line2P1.x * line2P2.y - line2P1.y * line2P2.x))
                / determinant,
            ((line1P1.x * line1P2.y - line1P1.y * line1P2.x) * (line2P1.y - line2P2.y) - (line1P1.y - line1P2.y)
                * (line2P1.x * line2P2.y - line2P1.y * line2P2.x))
                / determinant);
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
        double uA =
            ((line2P2.x - line2P1.x) * (line1P1.y - line2P1.y) - (line2P2.y - line2P1.y) * (line1P1.x - line2P1.x))
                / denominator;
        if ((uA < 0f) || (uA > 1f))
        {
            return null; // intersection outside line 1
        }
        double uB =
            ((line1P2.x - line1P1.x) * (line1P1.y - line2P1.y) - (line1P2.y - line1P1.y) * (line1P1.x - line2P1.x))
                / denominator;
        if (uB < 0 || uB > 1)
        {
            return null; // intersection outside line 2
        }
        return new OTSPoint3D(line1P1.x + uA * (line1P2.x - line1P1.x), line1P1.y + uA * (line1P2.y - line1P1.y), 0);
    }

    /**
     * Generate a Geometry that has a fixed offset from a reference Geometry.
     * @param referenceLine Geometry; the reference line
     * @param offset double; offset distance from the reference line; positive is LEFT, negative is RIGHT
     * @return OTSLine3D; the line that has the specified offset from the reference line
     * @throws OTSGeometryException on failure
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static OTSLine3D offsetGeometryOLD(final OTSLine3D referenceLine, final double offset)
        throws OTSGeometryException
    {
        Coordinate[] referenceCoordinates = referenceLine.getCoordinates();
        // printCoordinates("reference", referenceCoordinates);
        double bufferOffset = Math.abs(offset);
        final double precision = 0.000001;
        if (bufferOffset < precision) // if this is not added, and offset = 1E-16: CRASH
        {
            // return a copy of the reference line
            try
            {
                return new OTSLine3D(referenceCoordinates);
            }
            catch (NetworkException exception)
            {
                // System.err.println("CANNOT HAPPEN");
                throw new Error("Caught impossible exception while creating OTSLine3D: " + exception.getMessage());
            }
        }
        Geometry geometryLine = referenceLine.getLineString();
        Coordinate[] bufferCoordinates =
            geometryLine.buffer(bufferOffset, QUADRANTSEGMENTS, BufferParameters.CAP_FLAT).getCoordinates();
        
        
        
//        try
//        {
//            System.out.println(new OTSLine3D(bufferCoordinates).toExcel());
//        }
//        catch (NetworkException exception1)
//        {
//            exception1.printStackTrace();
//        }
        
        
        
        // Z coordinates may be NaN at this point

        // find the coordinate indices closest to the start point and end point, at a distance of approximately the
        // offset
        Coordinate sC0 = referenceCoordinates[0];
        Coordinate sC1 = referenceCoordinates[1];
        Coordinate eCm1 = referenceCoordinates[referenceCoordinates.length - 1];
        Coordinate eCm2 = referenceCoordinates[referenceCoordinates.length - 2];
        Set<Integer> startIndexSet = new HashSet<>();
        Set<Coordinate> startSet = new HashSet<Coordinate>();
        Set<Integer> endIndexSet = new HashSet<>();
        Set<Coordinate> endSet = new HashSet<Coordinate>();
        for (int i = 0; i < bufferCoordinates.length; i++) // Note: the last coordinate = the first coordinate
        {
            Coordinate c = bufferCoordinates[i];
            if (Math.abs(c.distance(sC0) - bufferOffset) < bufferOffset * precision && !startSet.contains(c))
            {
                startIndexSet.add(i);
                startSet.add(c);
            }
            if (Math.abs(c.distance(eCm1) - bufferOffset) < bufferOffset * precision && !endSet.contains(c))
            {
                endIndexSet.add(i);
                endSet.add(c);
            }
        }
        if (startIndexSet.size() != 2)
        {
            throw new OTSGeometryException("offsetGeometry: startIndexSet.size() = " + startIndexSet.size());
        }
        if (endIndexSet.size() != 2)
        {
            throw new OTSGeometryException("offsetGeometry: endIndexSet.size() = " + endIndexSet.size());
        }

        // which point(s) are in the right direction of the start / end?
        int startIndex = -1;
        int endIndex = -1;
        double expectedStartAngle = norm(angle(sC0, sC1) + Math.signum(offset) * Math.PI / 2.0);
        double expectedEndAngle = norm(angle(eCm2, eCm1) + Math.signum(offset) * Math.PI / 2.0);
        for (int ic : startIndexSet)
        {
            if (norm(expectedStartAngle - angle(sC0, bufferCoordinates[ic])) < Math.PI / 4.0
                || norm(angle(sC0, bufferCoordinates[ic]) - expectedStartAngle) < Math.PI / 4.0)
            {
                startIndex = ic;
            }
        }
        for (int ic : endIndexSet)
        {
            if (norm(expectedEndAngle - angle(eCm1, bufferCoordinates[ic])) < Math.PI / 4.0
                || norm(angle(eCm1, bufferCoordinates[ic]) - expectedEndAngle) < Math.PI / 4.0)
            {
                endIndex = ic;
            }
        }
        if (startIndex == -1 || endIndex == -1)
        {
            throw new OTSGeometryException("offsetGeometry: could not find startIndex or endIndex");
        }
        startIndexSet.remove(startIndex);
        endIndexSet.remove(endIndex);

        // Make two lists, one in each direction; start at "start" and end at "end".
        List<Coordinate> coordinateList1 = new ArrayList<>();
        List<Coordinate> coordinateList2 = new ArrayList<>();
        boolean use1 = true;
        boolean use2 = true;

        int i = startIndex;
        while (i != endIndex)
        {
            if (!coordinateList1.contains(bufferCoordinates[i]))
            {
                coordinateList1.add(bufferCoordinates[i]);
            }
            i = (i + 1) % bufferCoordinates.length;
            if (startIndexSet.contains(i) || endIndexSet.contains(i))
            {
                use1 = false;
            }
        }
        if (!coordinateList1.contains(bufferCoordinates[endIndex]))
        {
            coordinateList1.add(bufferCoordinates[endIndex]);
        }

        i = startIndex;
        while (i != endIndex)
        {
            if (!coordinateList2.contains(bufferCoordinates[i]))
            {
                coordinateList2.add(bufferCoordinates[i]);
            }
            i = (i == 0) ? bufferCoordinates.length - 1 : i - 1;
            if (startIndexSet.contains(i) || endIndexSet.contains(i))
            {
                use2 = false;
            }
        }
        if (!coordinateList2.contains(bufferCoordinates[endIndex]))
        {
            coordinateList2.add(bufferCoordinates[endIndex]);
        }

        if (!use1 && !use2)
        {
            throw new OTSGeometryException("offsetGeometry: could not find path from start to end for offset");
        }
        if (use1 && use2)
        {
            throw new OTSGeometryException(
                "offsetGeometry: Both paths from start to end for offset were found to be ok");
        }
        Coordinate[] coordinates;
        if (use1)
        {
            coordinates = new Coordinate[coordinateList1.size()];
            coordinateList1.toArray(coordinates);
        }
        else
        {
            coordinates = new Coordinate[coordinateList2.size()];
            coordinateList2.toArray(coordinates);
        }
        try
        {
            return new OTSLine3D(coordinates);
        }
        catch (NetworkException exception)
        {
            // System.err.println("CANNOT HAPPEN");
            throw new Error("Caught impossible exception in OTSLine3D: " + exception.getMessage());
        }
    }

    /**
     * Create a line at linearly varying offset from a reference line. The offset may change linearly from its initial value at
     * the start of the reference line to its final offset value at the end of the reference line.
     * @param referenceLine Geometry; the Geometry of the reference line
     * @param offsetAtStart double; offset at the start of the reference line (positive value is Left, negative value is Right)
     * @param offsetAtEnd double; offset at the end of the reference line (positive value is Left, negative value is Right)
     * @return Geometry; the Geometry of the line at linearly changing offset of the reference line
     * @throws OTSGeometryException when this method fails to create the offset line
     */
    public static OTSLine3D offsetLine(final OTSLine3D referenceLine, final double offsetAtStart,
        final double offsetAtEnd) throws OTSGeometryException
    {
        // System.out.println(OTSGeometry.printCoordinates("#referenceLine: \nc1,0,0\n# offset at start is " + offsetAtStart
        // + " at end is " + offsetAtEnd + "\n#", referenceLine, "\n   "));

        OTSLine3D offsetLineAtStart = offsetLine(referenceLine, offsetAtStart);
        if (offsetAtStart == offsetAtEnd)
        {
            return offsetLineAtStart; // offset does not change
        }
        // System.out.println(OTSGeometry.printCoordinates("#offset line at start: \nc0,0,0\n#", offsetLineAtStart, "\n   "));
        OTSLine3D offsetLineAtEnd = offsetLine(referenceLine, offsetAtEnd);
        // System.out.println(OTSGeometry.printCoordinates("#offset line at end: \nc0.7,0.7,0.7\n#", offsetLineAtEnd, "\n   "));
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
            double firstRatio =
                firstIndex < firstCoordinates.length ? first.indexOf(firstCoordinates[firstIndex]) / firstLength
                    : Double.MAX_VALUE;
            double secondRatio =
                secondIndex < secondCoordinates.length ? second.indexOf(secondCoordinates[secondIndex]) / secondLength
                    : Double.MAX_VALUE;
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
            Coordinate resultCoordinate =
                new Coordinate((1 - ratio) * firstCoordinate.x + ratio * secondCoordinate.x, (1 - ratio)
                    * firstCoordinate.y + ratio * secondCoordinate.y);
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
        try
        {
            // System.out.println(OTSGeometry.printCoordinates("#offset line: \nc0,1,0\n# offset at start is " + offsetAtStart
            // + " at end is " + offsetAtEnd + "\n#", new OTSLine3D(resultCoordinates), "\n   "));

            return new OTSLine3D(resultCoordinates);
        }
        catch (NetworkException exception)
        {
            // System.err.println("CANNOT HAPPEN");
            throw new Error("Caught impossible exception in OTSLine3D " + exception.getMessage());
        }
    }

    /**
     * @param args
     * @throws NetworkException
     * @throws OTSGeometryException
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException
    {
        // OTSLine3D line =
        // new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(-579.253, 60.157, 1.568),
        // new OTSPoint3D(-579.253, 60.177, 1.568)});
        // double offset = 4.83899987;
        // System.out.println(OTSBufferingOLD.offsetGeometryOLD(line, offset));
        OTSLine3D line =
            new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(-579.253, 60.157, 4.710),
                new OTSPoint3D(-579.253, 60.144, 4.712), new OTSPoint3D(-579.253, 60.144, 0.000),
                new OTSPoint3D(-579.251, 60.044, 0.000), new OTSPoint3D(-579.246, 59.944, 0.000),
                new OTSPoint3D(-579.236, 59.845, 0.000), new OTSPoint3D(-579.223, 59.746, 0.000),
                new OTSPoint3D(-579.206, 59.647, 0.000), new OTSPoint3D(-579.185, 59.549, 0.000),
                new OTSPoint3D(-579.161, 59.452, 0.000), new OTSPoint3D(-579.133, 59.356, 0.000),
                new OTSPoint3D(-579.101, 59.261, 0.000), new OTSPoint3D(-579.066, 59.168, 0.000),
                new OTSPoint3D(-579.028, 59.075, 0.000), new OTSPoint3D(-578.986, 58.985, 0.000),
                new OTSPoint3D(-578.940, 58.896, 0.000), new OTSPoint3D(-578.891, 58.809, 0.000),
                new OTSPoint3D(-578.839, 58.723, 0.000), new OTSPoint3D(-578.784, 58.640, 0.000),
                new OTSPoint3D(-578.725, 58.559, 0.000), new OTSPoint3D(-578.664, 58.480, 0.000),
                new OTSPoint3D(-578.599, 58.403, 0.000), new OTSPoint3D(-578.532, 58.329, 0.000),
                new OTSPoint3D(-578.462, 58.258, 0.000), new OTSPoint3D(-578.390, 58.189, 0.000),
                new OTSPoint3D(-578.314, 58.123, 0.000), new OTSPoint3D(-578.237, 58.060, 0.000),
                new OTSPoint3D(-578.157, 58.000, 0.000), new OTSPoint3D(-578.075, 57.943, 0.000),
                new OTSPoint3D(-577.990, 57.889, 0.000), new OTSPoint3D(-577.904, 57.839, 0.000),
                new OTSPoint3D(-577.816, 57.791, 0.000), new OTSPoint3D(-577.726, 57.747, 0.000),
                new OTSPoint3D(-577.635, 57.707, 0.000), new OTSPoint3D(-577.542, 57.670, 0.000),
                new OTSPoint3D(-577.448, 57.636, 0.000), new OTSPoint3D(-577.352, 57.606, 0.000),
                new OTSPoint3D(-577.256, 57.580, 0.000), new OTSPoint3D(-577.159, 57.557, 0.000),
                new OTSPoint3D(-577.060, 57.538, 0.000), new OTSPoint3D(-576.962, 57.523, 0.000),
                new OTSPoint3D(-576.862, 57.512, 0.000), new OTSPoint3D(-576.763, 57.504, 0.000),
                new OTSPoint3D(-576.663, 57.500, 0.000), new OTSPoint3D(-576.623, 57.500, 6.278),
                new OTSPoint3D(-576.610, 57.500, 6.280), new OTSPoint3D(-567.499, 57.473, 6.280)});
        System.out.println(line.toExcel());
        System.out.println(OTSBufferingJTS.offsetGeometryOLD(line, -1.831));
    }
}

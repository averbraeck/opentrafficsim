package org.opentrafficsim.core.geometry;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.core.network.NetworkException;

import nl.tudelft.simulation.dsol.logger.SimLogger;

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
     * @param angle double; original angle.
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
     * @param c1 Coordinate; first coordinate
     * @param c2 Coordinate; second coordinate
     * @return the normalized angle of the line between c1 and c2
     */
    private static double angle(final Coordinate c1, final Coordinate c2)
    {
        return norm(Math.atan2(c2.y - c1.y, c2.x - c1.x));
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
                                    // SimLogger.trace(Cat.CORE, "preceding segment " + pPoint + " to " + p + ", this segment "
                                    // + segmentFrom + " to " + segmentTo + " totalAngle " + totalAngle);
                                    OTSPoint3D intermediatePoint =
                                            intersectionOfLineSegments(pPoint, p, segmentFrom, segmentTo);
                                    if (null != intermediatePoint)
                                    {
                                        // mark it as added point at inside corner
                                        intermediatePoint =
                                                new OTSPoint3D(intermediatePoint.x, intermediatePoint.y, Double.NaN);
                                        // SimLogger.trace(Cat.CORE, "Inserting intersection of preceding segment and this "
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
                            // SimLogger.trace(Cat.CORE, "inserting intermediate point " + intermediatePoint + " for angle "
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
            // SimLogger.trace(Cat.CORE, OTSGeometry.printCoordinates("#before cleanup: \nc0,0,0\n#", new OTSLine3D(points), "\n
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
                                // SimLogger.trace(Cat.CORE, "point " + checkPoint + " inside buffer (distance is " + distance +
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
                    // SimLogger.trace(Cat.CORE, "Removing " + checkPoint);
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
            SimLogger.always().error(exception, "Exception in offsetLine - should never happen");
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
     * Generate a Geometry that has a fixed offset from a reference Geometry.
     * @param referenceLine OTSLine3D; the reference line
     * @param offset double; offset distance from the reference line; positive is LEFT, negative is RIGHT
     * @return OTSLine3D; the line that has the specified offset from the reference line
     * @throws OTSGeometryException on failure
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static OTSLine3D offsetGeometryOLD(final OTSLine3D referenceLine, final double offset) throws OTSGeometryException
    {
        Coordinate[] referenceCoordinates = referenceLine.getCoordinates();
        // printCoordinates("reference", referenceCoordinates);
        double bufferOffset = Math.abs(offset);
        final double precision = 0.000001;
        if (bufferOffset < precision) // if this is not added, and offset = 1E-16: CRASH
        {
            // return a copy of the reference line
            return new OTSLine3D(referenceCoordinates);
        }
        Geometry geometryLine = referenceLine.getLineString();
        Coordinate[] bufferCoordinates =
                geometryLine.buffer(bufferOffset, QUADRANTSEGMENTS, BufferParameters.CAP_FLAT).getCoordinates();

        // Z coordinates may be NaN at this point

        // find the coordinate indices closest to the start point and end point,
        // at a distance of approximately the offset
        Coordinate sC0 = referenceCoordinates[0];
        Coordinate sC1 = referenceCoordinates[1];
        Coordinate eCm1 = referenceCoordinates[referenceCoordinates.length - 1];
        Coordinate eCm2 = referenceCoordinates[referenceCoordinates.length - 2];

        double expectedStartAngle = norm(angle(sC0, sC1) + Math.signum(offset) * Math.PI / 2.0);
        double expectedEndAngle = norm(angle(eCm2, eCm1) + Math.signum(offset) * Math.PI / 2.0);
        Coordinate sExpected = new Coordinate(sC0.x + bufferOffset * Math.cos(expectedStartAngle),
                sC0.y + bufferOffset * Math.sin(expectedStartAngle));
        Coordinate eExpected = new Coordinate(eCm1.x + bufferOffset * Math.cos(expectedEndAngle),
                eCm1.y + bufferOffset * Math.sin(expectedEndAngle));

        // which coordinates are closest to sExpected and eExpected?
        double dS = Double.MAX_VALUE;
        double dE = Double.MAX_VALUE;
        int sIndex = -1;
        int eIndex = -1;
        for (int i = 0; i < bufferCoordinates.length; i++)
        {
            Coordinate c = bufferCoordinates[i];
            double dsc = c.distance(sExpected);
            double dec = c.distance(eExpected);
            if (dsc < dS)
            {
                dS = dsc;
                sIndex = i;
            }
            if (dec < dE)
            {
                dE = dec;
                eIndex = i;
            }
        }

        if (sIndex == -1)
        {
            throw new OTSGeometryException("offsetGeometry: startIndex not found for line " + referenceLine);
        }
        if (eIndex == -1)
        {
            throw new OTSGeometryException("offsetGeometry: endIndex not found for line " + referenceLine);
        }
        if (dS > 0.01)
        {
            SimLogger.filter(Cat.CORE).trace(referenceLine.toExcel() + "\n\n\n\n" + new OTSLine3D(bufferCoordinates).toExcel()
                    + "\n\n\n\n" + sExpected + "\n" + eExpected);
            throw new OTSGeometryException("offsetGeometry: startDistance too big (" + dS + ") for line " + referenceLine);
        }
        if (dE > 0.01)
        {
            throw new OTSGeometryException("offsetGeometry: endDistance too big (" + dE + ") for line " + referenceLine);
        }

        // try positive direction
        boolean ok = true;
        int i = sIndex;
        Coordinate lastC = null;
        List<OTSPoint3D> result = new ArrayList<>();
        while (ok)
        {
            Coordinate c = bufferCoordinates[i];
            if (lastC != null && close(c, lastC, sC0, eCm1))
            {
                ok = false;
                break;
            }
            result.add(new OTSPoint3D(c));
            if (i == eIndex)
            {
                return OTSLine3D.createAndCleanOTSLine3D(result);
            }
            i = (i == bufferCoordinates.length - 1) ? 0 : i + 1;
            lastC = c;
        }

        // try negative direction
        ok = true;
        i = sIndex;
        lastC = null;
        result = new ArrayList<>();
        while (ok)
        {
            Coordinate c = bufferCoordinates[i];
            if (lastC != null && close(c, lastC, sC0, eCm1))
            {
                ok = false;
                break;
            }
            result.add(new OTSPoint3D(c));
            if (i == eIndex)
            {
                return OTSLine3D.createAndCleanOTSLine3D(result);
            }
            i = (i == 0) ? bufferCoordinates.length - 1 : i - 1;
            lastC = c;
        }

        /*- SimLogger.trace(Cat.CORE, referenceLine.toExcel() + "\n\n\n\n" + new OTSLine3D(bufferCoordinates).toExcel()
            + "\n\n\n\n" + sExpected + "\n" + eExpected); */
        throw new OTSGeometryException("offsetGeometry: could not find offset in either direction for line " + referenceLine);
    }

    /**
     * Check if the points check[] are close to the line [lineC1..LineC2].
     * @param lineC1 Coordinate; first point of the line
     * @param lineC2 Coordinate; second point of the line
     * @param check Coordinate...; the coordinates to check
     * @return whether one of the points to check is close to the line.
     */
    private static boolean close(final Coordinate lineC1, final Coordinate lineC2, final Coordinate... check)
    {
        Line2D.Double line = new Line2D.Double(lineC1.x, lineC1.y, lineC2.x, lineC2.y);
        for (Coordinate c : check)
        {
            if (line.ptSegDist(c.x, c.y) < 0.01)
            {
                return true;
            }
        }
        return false;
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
        // SimLogger.trace(Cat.CORE, OTSGeometry.printCoordinates("#referenceLine: \nc1,0,0\n# offset at start is " +
        // offsetAtStart + " at end is " + offsetAtEnd + "\n#", referenceLine, "\n "));

        OTSLine3D offsetLineAtStart = offsetLine(referenceLine, offsetAtStart);
        if (offsetAtStart == offsetAtEnd)
        {
            return offsetLineAtStart; // offset does not change
        }
        // SimLogger.trace(Cat.CORE, OTSGeometry.printCoordinates("#offset line at start: \nc0,0,0\n#", offsetLineAtStart,
        // "\n"));
        OTSLine3D offsetLineAtEnd = offsetLine(referenceLine, offsetAtEnd);
        // SimLogger.trace(Cat.CORE, OTSGeometry.printCoordinates("#offset line at end: \nc0.7,0.7,0.7\n#", offsetLineAtEnd,
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

    /**
     * @param args String[]; args
     * @throws NetworkException on error
     * @throws OTSGeometryException on error
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException
    {
        // OTSLine3D line =
        // new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(-579.253, 60.157, 1.568),
        // new OTSPoint3D(-579.253, 60.177, 1.568)});
        // double offset = 4.83899987;
        // System.out.println(OTSBufferingOLD.offsetGeometryOLD(line, offset));
        OTSLine3D line = new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(-579.253, 60.157, 4.710),
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

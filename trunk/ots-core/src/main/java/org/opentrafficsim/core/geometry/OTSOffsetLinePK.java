package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Peter Knoppers' attempt to implement offsetLine.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Dec 1, 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class OTSOffsetLinePK
{
    /** This class should never be instantiated. */
    private OTSOffsetLinePK()
    {
        // Cannot be instantiated.
    }

    /** Debugging flag. */
    private static boolean debugOffsetLine = false;

    /** Precision of approximation of arcs in the offsetLine method. */
    private static double circlePrecision = 0.001;

    /** Noise in the reference line less than this value is always filtered. */
    private static double offsetMinimumFilterValue = 0.001;

    /** Noise in the reference line greater than this value is never filtered. */
    private static double offsetMaximumFilterValue = 0.1;

    /**
     * Noise in the reference line less than <cite>offset / offsetFilterRatio</cite> is filtered except when the resulting value
     * exceeds <cite>offsetMaximumFilterValue</cite>.
     */
    private static double offsetFilterRatio = 10;

    /**
     * Construct an offset line.
     * @param referenceLine OTSLine3D; the reference line
     * @param offset double; the offset; positive values indicate left of the reference line, negative values indicate right of
     *            the reference line
     * @return OTSLine3D; a line at the specified offset from the reference line
     * @throws OTSGeometryException when this method runs into major trouble and cannot produce a decent result
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static OTSLine3D offsetLine(final OTSLine3D referenceLine, final double offset) throws OTSGeometryException
    {
        // if (referenceLine.size() > 1 && referenceLine.getFirst().horizontalDistanceSI(new OTSPoint3D(-200.376, -111.999)) <
        // 0.1
        // && referenceLine.get(1).horizontalDistanceSI(new OTSPoint3D(-204.098, -100.180)) < 0.1 && Math.abs(offset) > 1)

        // if (referenceLine.size() > 1 && referenceLine.getFirst().horizontalDistanceSI(new OTSPoint3D(-177.580, -169.726)) <
        // 0.1
        // && referenceLine.get(1).horizontalDistanceSI(new OTSPoint3D(-179.028, -166.084)) < 0.1 && Math.abs(offset) > 1
        // && referenceLine.size() == 0)
        // {
        // debugOffsetLine = true;
        // for (int i = 0; i < referenceLine.size(); i++)
        // {
        // System.out.println(String.format(
        // Locale.US,
        // "point %2d: %20s,%20s%s",
        // i,
        // referenceLine.get(i).x,
        // referenceLine.get(i).y,
        // (i == 0 ? "" : " ("
        // + Math.toDegrees(Math.atan2(referenceLine.get(i).y - referenceLine.get(i - 1).y,
        // referenceLine.get(i).x - referenceLine.get(i - 1).x)) + ")")));
        // }
        // System.out.println("# offset is " + offset);
        // System.out.println(OTSGeometry.printCoordinates("#reference:\nc0,0,0\n#", referenceLine, "\n "));
        // }
        // else
        // {
        // debugOffsetLine = false;
        // }
        double bufferOffset = Math.abs(offset);
        final double precision = 0.00001;
        if (bufferOffset < precision)
        {
            // squash the Z-coordinate
            List<OTSPoint3D> coordinates = new ArrayList<>(referenceLine.size());
            for (int i = 0; i < referenceLine.size(); i++)
            {
                OTSPoint3D coordinate = referenceLine.get(i);
                coordinates.add(new OTSPoint3D(coordinate.x, coordinate.y));
            }
            return OTSLine3D.createAndCleanOTSLine3D(coordinates);
        }

        OTSLine3D filteredReferenceLine = referenceLine.noiseFilteredLine(
                Math.max(offsetMinimumFilterValue, Math.min(bufferOffset / offsetFilterRatio, offsetMaximumFilterValue)));
        if (debugOffsetLine)
        {
            System.out.println("#filtered reference line ");
            System.out.println(filteredReferenceLine.toPlot());
        }
        List<OTSPoint3D> tempPoints = new ArrayList<>();
        // Make good use of the fact that an OTSLine3D cannot have consecutive duplicate points and has > 1 points
        OTSPoint3D prevPoint = filteredReferenceLine.get(0);
        Double prevAngle = null;
        for (int index = 0; index < filteredReferenceLine.size() - 1; index++)
        {
            OTSPoint3D nextPoint = filteredReferenceLine.get(index + 1);
            double angle = Math.atan2(nextPoint.y - prevPoint.y, nextPoint.x - prevPoint.x);
            if (debugOffsetLine)
            {
                System.out.println("#reference segment " + index + " from " + prevPoint + " to " + nextPoint + " angle "
                        + Math.toDegrees(angle));
            }
            OTSPoint3D segmentFrom =
                    new OTSPoint3D(prevPoint.x - Math.sin(angle) * offset, prevPoint.y + Math.cos(angle) * offset);
            OTSPoint3D segmentTo =
                    new OTSPoint3D(nextPoint.x - Math.sin(angle) * offset, nextPoint.y + Math.cos(angle) * offset);
            boolean addSegment = true;
            if (index > 0)
            {
                double deltaAngle = angle - prevAngle;
                if (Math.abs(deltaAngle) > Math.PI)
                {
                    deltaAngle -= Math.signum(deltaAngle) * 2 * Math.PI;
                }
                if (deltaAngle * offset <= 0)
                {
                    // Outside of curve of reference line
                    // Approximate an arc using straight segments.
                    // Determine how many segments are needed.
                    int numSegments = 1;
                    if (Math.abs(deltaAngle) > Math.PI / 2)
                    {
                        numSegments = 2;
                    }
                    while (true)
                    {
                        double maxError = bufferOffset * (1 - Math.abs(Math.cos(deltaAngle / numSegments / 2)));
                        if (maxError < circlePrecision)
                        {
                            break; // required precision reached
                        }
                        numSegments *= 2;
                    }
                    OTSPoint3D prevArcPoint = tempPoints.get(tempPoints.size() - 1);
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
                        // Find any intersection points of the new segment and all previous segments
                        OTSPoint3D prevSegFrom = null;
                        int stopAt = tempPoints.size();
                        for (int i = 0; i < stopAt; i++)
                        {
                            OTSPoint3D prevSegTo = tempPoints.get(i);
                            if (null != prevSegFrom)
                            {
                                OTSPoint3D prevSegIntersection = OTSPoint3D.intersectionOfLineSegments(prevArcPoint,
                                        intermediatePoint, prevSegFrom, prevSegTo);
                                if (null != prevSegIntersection
                                        && prevSegIntersection.horizontalDistanceSI(prevArcPoint) > circlePrecision
                                        && prevSegIntersection.horizontalDistanceSI(prevSegFrom) > circlePrecision
                                        && prevSegIntersection.horizontalDistanceSI(prevSegTo) > circlePrecision)
                                {
                                    if (debugOffsetLine)
                                    {
                                        System.out.println("#inserting intersection in arc segment " + prevSegIntersection);
                                    }
                                    tempPoints.add(prevSegIntersection);
                                    // System.out.println(new OTSLine3D(tempPoints).toPlot());
                                }
                            }
                            prevSegFrom = prevSegTo;
                        }
                        OTSPoint3D nextSegmentIntersection =
                                OTSPoint3D.intersectionOfLineSegments(prevSegFrom, intermediatePoint, segmentFrom, segmentTo);
                        if (null != nextSegmentIntersection)
                        {
                            if (debugOffsetLine)
                            {
                                System.out.println(
                                        "#inserting intersection of arc segment with next segment " + nextSegmentIntersection);
                            }
                            tempPoints.add(nextSegmentIntersection);
                            // System.out.println(new OTSLine3D(tempPoints).toPlot());
                        }
                        if (debugOffsetLine)
                        {
                            System.out.println("#inserting arc point " + intermediatePoint + " for angle "
                                    + Math.toDegrees(intermediateAngle));
                        }
                        tempPoints.add(intermediatePoint);
                        // System.out.println(new OTSLine3D(tempPoints).toPlot());
                        prevArcPoint = intermediatePoint;
                    }
                }
                // Inside of curve of reference line.
                // Add the intersection point of each previous segment and the next segment
                OTSPoint3D pPoint = null;
                int currentSize = tempPoints.size(); // PK DO NOT use the "dynamic" limit
                for (int i = 0; i < currentSize /* tempPoints.size() */; i++)
                {
                    OTSPoint3D p = tempPoints.get(i);
                    if (null != pPoint)
                    {
                        double pAngle = Math.atan2(p.y - pPoint.y, p.x - pPoint.x);
                        double angleDifference = angle - pAngle;
                        if (Math.abs(angleDifference) > Math.PI)
                        {
                            angleDifference -= Math.signum(angleDifference) * 2 * Math.PI;
                        }
                        if (debugOffsetLine)
                        {
                            System.out.println("#preceding segment " + pPoint + " to " + p + " angle " + Math.toDegrees(pAngle)
                                    + ", next segment " + segmentFrom + " to " + segmentTo + " angle " + Math.toDegrees(angle)
                                    + " angleDifference " + Math.toDegrees(angleDifference));
                        }
                        if (Math.abs(angleDifference) > 0)// 0.01)
                        {
                            OTSPoint3D intersection = OTSPoint3D.intersectionOfLineSegments(pPoint, p, segmentFrom, segmentTo);
                            if (null != intersection)
                            {
                                if (tempPoints.size() - 1 == i)
                                {
                                    if (debugOffsetLine)
                                    {
                                        System.out.println("#Replacing last point of preceding segment and "
                                                + "first point of next segment by their intersection " + intersection);
                                    }
                                    tempPoints.remove(tempPoints.size() - 1);
                                    segmentFrom = intersection;
                                }
                                else
                                {
                                    if (debugOffsetLine)
                                    {
                                        if (tempPoints.size() > 17)
                                        {
                                            System.out.println("#not good");
                                            System.out.println(new OTSLine3D(tempPoints).toPlot());
                                        }
                                        System.out.println("#Adding intersection of preceding segment and " + "next segment "
                                                + intersection);
                                    }
                                    tempPoints.add(intersection);
                                }
                                // tempPoints.set(tempPoints.size() - 1, intermediatePoint);
                                if (tempPoints.size() > 1)
                                {
                                    // System.out.println(new OTSLine3D(tempPoints).toPlot());
                                }
                            }
                        }
                        else
                        {
                            // This is where things went very wrong in the TestGeometry demo.
                            if (debugOffsetLine)
                            {
                                System.out.println("#Not adding intersection of preceding segment and this segment "
                                        + "(angle too small)");
                            }
                            if (i == tempPoints.size() - 1)
                            {
                                if (debugOffsetLine)
                                {
                                    System.out.println("#Not adding segment, but replacing end of last segment");
                                    tempPoints.remove(tempPoints.size() - 1);
                                    segmentFrom = tempPoints.get(tempPoints.size() - 1);
                                    tempPoints.remove(tempPoints.size() - 1); 
                                }
                            }
                        }
                    }
                    pPoint = p;
                }
            }
            if (addSegment)
            {
                tempPoints.add(segmentFrom);
                tempPoints.add(segmentTo);
                if (debugOffsetLine)
                {
                    System.out.println("#Added segmentFrom " + segmentFrom + " and segmentTo " + segmentTo);
                    System.out.println(new OTSLine3D(tempPoints).toPlot());
                }
                prevPoint = nextPoint;
                prevAngle = angle;
            }
        }
        if (debugOffsetLine)
        {
            System.out.println("# before cleanup: \nc1,0,0\n");
            if (tempPoints.size() > 0)
            {
                OTSPoint3D p = tempPoints.get(0);
                System.out.println(String.format(Locale.US, "M %.3f,%.3f", p.x, p.y));
                for (int i = 1; i < tempPoints.size(); i++)
                {
                    p = tempPoints.get(i);
                    System.out.println(String.format(Locale.US, "L %.3f,%.3f", p.x, p.y));
                }
            }
        }
        // Remove points that are closer than the specified offset
        for (int index = 1; index < tempPoints.size() - 1; index++)
        {
            OTSPoint3D checkPoint = tempPoints.get(index);
            prevPoint = null;
            boolean tooClose = false;
            boolean somewhereAtCorrectDistance = false;
            for (int i = 0; i < filteredReferenceLine.size(); i++)
            {
                OTSPoint3D p = filteredReferenceLine.get(i);
                if (null != prevPoint)
                {
                    OTSPoint3D closestPoint = checkPoint.closestPointOnSegment(prevPoint, p);
                    double distance = closestPoint.horizontalDistanceSI(checkPoint);
                    if (distance < bufferOffset - circlePrecision)
                    {
                        if (debugOffsetLine)
                        {
                            System.out.print("#point " + checkPoint + " inside buffer (distance is " + distance + ") ");
                        }
                        tooClose = true;
                        break;
                    }
                    else if (distance < bufferOffset + precision)
                    {
                        somewhereAtCorrectDistance = true;
                    }
                }
                prevPoint = p;
            }
            if (tooClose || !somewhereAtCorrectDistance)
            {
                if (debugOffsetLine)
                {
                    System.out.println("#Removing " + checkPoint);
                }
                tempPoints.remove(index);
                index--;
            }
        }
        if (debugOffsetLine)
        {
            System.out.println("#after cleanup " + tempPoints.size() + " points left");
            System.out.println(new OTSLine3D(tempPoints).toPlot());
        }
        // Fix the z-coordinate of all points that were added as intersections of segments.
        for (int index = 0; index < tempPoints.size(); index++)
        {
            OTSPoint3D p = tempPoints.get(index);
            if (Double.isNaN(p.z))
            {
                tempPoints.set(index, new OTSPoint3D(p.x, p.y, 0));
            }
        }
        try
        {
            return OTSLine3D.createAndCleanOTSLine3D(tempPoints);
        }
        catch (OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
        return null;
    }
    
    /**
     * Set or clear the debugging flag.
     * @param newValue boolean; new value for the debugging flag
     */
    public static void setDebugOffsetLine(final boolean newValue)
    {
        debugOffsetLine = newValue;
    }
    
}

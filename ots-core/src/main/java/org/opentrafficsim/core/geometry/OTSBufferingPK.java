package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 28, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class OTSBufferingPK
{
    /** */
    private OTSBufferingPK()
    {
    }

    /** Switch debugging on or off. */
    private static boolean debugOffsetLine = false;

    /**
     * Construct parallel line.
     * @param line the line for which the offset has to be generated
     * @param offset double; offset distance from the reference line; positive is LEFT, negative is RIGHT
     * @return OTSLine3D; the line that has the specified offset from the reference line
     */
    public static OTSLine3D offsetLine(final OTSLine3D line, final double offset)
    {
        try
        {
            double bufferOffset = Math.abs(offset);
            final double precision = 0.00001;
            if (bufferOffset < precision)
            {
                return line; // It is immutable; so we can safely return the original
            }
            final double circlePrecision = 0.001;
            List<OTSPoint3D> tempPoints = new ArrayList<>();
            // Make good use of the fact that an OTSLine3D cannot have consecutive duplicate points and has > 1 points
            OTSPoint3D prevPoint = line.get(0);
            Double prevAngle = null;
            for (int index = 0; index < line.size() - 1; index++)
            {
                OTSPoint3D nextPoint = line.get(index + 1);
                double angle = Math.atan2(nextPoint.y - prevPoint.y, nextPoint.x - prevPoint.x);
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
                    if (deltaAngle * offset > 0)
                    {
                        // Inside of curve of reference line.
                        // Add the intersection point of each previous segment and the next segment
                        OTSPoint3D pPoint = null;
                        for (int i = 0; i < tempPoints.size(); i++)
                        {
                            OTSPoint3D p = tempPoints.get(i);
                            if (Double.isNaN(p.z))
                            {
                                continue;// skip this one
                            }
                            if (null != pPoint)
                            {
                                double pAngle = Math.atan2(p.y - pPoint.y, p.x - pPoint.x);
                                double angleDifference = angle - pAngle;
                                if (Math.abs(angleDifference) > Math.PI)
                                {
                                    angleDifference += Math.signum(angleDifference) * 2 * Math.PI;
                                }
                                if (debugOffsetLine)
                                {
                                    System.out.println("#preceding segment " + pPoint + " to " + p + ", this segment "
                                        + segmentFrom + " to " + segmentTo + " angleDifference " + angleDifference);
                                }
                                if (Math.abs(angleDifference) > 0)// 0.01)
                                {
                                    OTSPoint3D intersection =
                                        OTSPoint3D.intersectionOfLineSegments(pPoint, p, segmentFrom, segmentTo);
                                    if (null != intersection)
                                    {
                                        // mark it as added point at inside corner
                                        intersection = new OTSPoint3D(intersection.x, intersection.y, Double.NaN);
                                        if (tempPoints.size() - 1 == i)
                                        {
                                            if (debugOffsetLine)
                                            {
                                                System.out
                                                    .println("#Replacing last point of preceding segment and first point of next segment by their intersection "
                                                        + intersection);
                                            }
                                            tempPoints.remove(tempPoints.size() - 1);
                                            segmentFrom = intersection;
                                        }
                                        else
                                        {
                                            if (debugOffsetLine)
                                            {
                                                System.out
                                                    .println("#Adding intersection of preceding segment and next segment "
                                                        + intersection);
                                            }
                                            tempPoints.add(intersection);
                                        }
                                        // tempPoints.set(tempPoints.size() - 1, intermediatePoint);
                                    }
                                }
                                else
                                {
                                    if (debugOffsetLine)
                                    {
                                        System.out
                                            .println("#Not adding intersection of preceding segment and this segment "
                                                + "(angle too small)");
                                    }
                                    if (i == tempPoints.size() - 1)
                                    {
                                        if (debugOffsetLine)
                                        {
                                            System.out.println("#Not adding segment");
                                        }
                                        addSegment = false;
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
                            if (debugOffsetLine)
                            {
                                System.out.println("#inserting arc point " + intermediatePoint + " for angle "
                                    + Math.toDegrees(intermediateAngle));
                            }
                            tempPoints.add(intermediatePoint);
                        }
                    }
                }
                if (addSegment)
                {
                    if (debugOffsetLine)
                    {
                        System.out.println("#Adding segmentFrom " + segmentFrom);
                    }
                    tempPoints.add(segmentFrom);
                    if (debugOffsetLine)
                    {
                        System.out.println("#Adding segmentTo " + segmentTo);
                    }
                    tempPoints.add(segmentTo);
                    prevPoint = nextPoint;
                    prevAngle = angle;
                }
            }
            if (debugOffsetLine)
            {
                System.out.println(OTSGeometry.printCoordinates("#before cleanup: \nc0,0,0\n#", new OTSLine3D(
                    tempPoints), "\n   "));
            }
            // Remove points that are closer than the specified offset
            for (int index = 1; index < tempPoints.size() - 1; index++)
            {
                OTSPoint3D checkPoint = tempPoints.get(index);
                prevPoint = null;
                boolean tooClose = false;
                boolean somewhereAtCorrectDistance = false;
                for (int i = 0; i < line.size(); i++)
                {
                    OTSPoint3D p = line.get(i);
                    if (null != prevPoint)
                    {
                        OTSPoint3D closestPoint = OTSPoint3D.closestPointOnSegmentToPoint(prevPoint, p, checkPoint);
                        if (closestPoint != line.get(0) && closestPoint != line.get(line.size() - 1))
                        {
                            double distance = closestPoint.horizontalDistanceSI(checkPoint);
                            if (distance < bufferOffset - circlePrecision)
                            {
                                if (debugOffsetLine)
                                {
                                    System.out.print("#point " + checkPoint + " inside buffer (distance is " + distance
                                        + ") ");
                                }
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
                    if (debugOffsetLine)
                    {
                        System.out.println("#Removing " + checkPoint);
                    }
                    tempPoints.remove(index);
                    index--;
                }
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
            return OTSLine3D.createAndCleanOTSLine3D(tempPoints);
        }
        catch (OTSGeometryException | NetworkException exception)
        {
            System.err.println("Cannot happen");
            exception.printStackTrace();
            return null;
        }
    }

}

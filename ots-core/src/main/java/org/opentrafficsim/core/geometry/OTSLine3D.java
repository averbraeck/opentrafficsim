package org.opentrafficsim.core.geometry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

/**
 * Line with OTSPoint3D points, a cached length indexed line, a cahced length, and a cached centroid (all calculated on first
 * use).
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class OTSLine3D implements LocatableInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** the points of the line. */
    private final OTSPoint3D[] points;

    /** the cumulative length of the line at point 'i'. */
    private double[] lengthIndexedLine = null;

    /** the cached length; will be calculated when needed for the first time. */
    private double length = Double.NaN;

    /** the cached centroid; will be calculated when needed for the first time. */
    private OTSPoint3D centroid = null;

    /** the cached bounds; will be calculated when needed for the first time. */
    private Bounds bounds = null;

    /**
     * @param points the array of points to construct this OTSLine3D from.
     * @throws NetworkException when the provided points do not constitute a valid line (too few points or identical adjacent
     *             points)
     */
    public OTSLine3D(final OTSPoint3D... points) throws NetworkException
    {
        if (points.length < 2)
        {
            throw new NetworkException("Degenerate OTSLine3D; has " + points.length + " point"
                    + (points.length != 1 ? "s" : ""));
        }
        for (int i = 1; i < points.length; i++)
        {
            if (points[i - 1].x == points[i].x && points[i - 1].y == points[i].y && points[i - 1].z == points[i].z)
            {
                throw new NetworkException("Degenerate OTSLine3D; point " + (i - 1) + " has the same x, y and z as point " + i);
            }
        }
        this.points = points;
    }
    
    /**
     * Construct parallel line.
     * @param offset double; offset distance from the reference line; positive is LEFT, negative is RIGHT
     * @return OTSLine3D; the line that has the specified offset from the reference line
     */
    public OTSLine3D offsetLine(final double offset)
    {
        try
        {
            double bufferOffset = Math.abs(offset);
            final double precision = 0.00001;
            if (bufferOffset < precision)
            {
                return this; // It is immutable; so we can safely return the original
            }
            final double circlePrecision = 0.001;
            List<OTSPoint3D> tempPoints = new ArrayList<>();
            // Make good use of the fact that an OTSLine3D cannot have consecutive duplicate points and has > 1 points
            OTSPoint3D prevPoint = get(0);
            Double prevAngle = null;
            for (int index = 0; index < size() - 1; index++)
            {
                OTSPoint3D nextPoint = get(index + 1);
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
                                            OTSPoint3D.intersectionOfLineSegments(pPoint, p, segmentFrom, segmentTo);
                                    if (null != intermediatePoint)
                                    {
                                        // mark it as added point at inside corner
                                        intermediatePoint =
                                                new OTSPoint3D(intermediatePoint.x, intermediatePoint.y, Double.NaN);
//                                        System.out.println("Inserting intersection of preceding segment and this segment "
//                                                + intermediatePoint);
                                        tempPoints.add(intermediatePoint);
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
                            OTSPoint3D intermediatePoint =
                                    new OTSPoint3D(prevPoint.x - Math.sin(intermediateAngle) * offset, prevPoint.y
                                            + Math.cos(intermediateAngle) * offset);
//                            System.out.println("inserting intermediate point " + intermediatePoint + " for angle "
//                                    + Math.toDegrees(intermediateAngle));
                            tempPoints.add(intermediatePoint);
                        }
                    }
                }
                tempPoints.add(segmentFrom);
                tempPoints.add(segmentTo);
                prevPoint = nextPoint;
                prevAngle = angle;
            }
            // System.out.println(OTSGeometry.printCoordinates("#before cleanup: \nc0,0,0\n#", new OTSLine3D(points), "\n   "));
            // Remove points that are closer than the specified offset
            for (int index = 1; index < tempPoints.size() - 1; index++)
            {
                OTSPoint3D checkPoint = tempPoints.get(index);
                prevPoint = null;
                boolean tooClose = false;
                boolean somewhereAtCorrectDistance = false;
                for (int i = 0; i < size(); i++)
                {
                    OTSPoint3D p = get(i);
                    if (null != prevPoint)
                    {
                        OTSPoint3D closestPoint = OTSPoint3D.closestPointOnSegmentToPoint(prevPoint, p, checkPoint);
                        if (closestPoint != get(0) && closestPoint != get(size() - 1))
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

    /**
     * Create a line at linearly varying offset from this line. The offset may change linearly from its initial value at
     * the start of the reference line to its final offset value at the end of the reference line.
     * @param offsetAtStart double; offset at the start of the reference line (positive value is Left, negative value is Right)
     * @param offsetAtEnd double; offset at the end of the reference line (positive value is Left, negative value is Right)
     * @return Geometry; the Geometry of the line at linearly changing offset of the reference line
     * @throws OTSGeometryException when this method fails to create the offset line
     */
    public OTSLine3D offsetLine(final double offsetAtStart, final double offsetAtEnd)
            throws OTSGeometryException
    {
        // System.out.println(OTSGeometry.printCoordinates("#referenceLine: \nc1,0,0\n# offset at start is " + offsetAtStart
        // + " at end is " + offsetAtEnd + "\n#", referenceLine, "\n   "));

        OTSLine3D offsetLineAtStart = offsetLine(offsetAtStart);
        if (offsetAtStart == offsetAtEnd)
        {
            return offsetLineAtStart; // offset does not change
        }
        // System.out.println(OTSGeometry.printCoordinates("#offset line at start: \nc0,0,0\n#", offsetLineAtStart, "\n   "));
        OTSLine3D offsetLineAtEnd = offsetLine(offsetAtEnd);
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
     * Concatenate several OTSLine3D instances.
     * @param lines OTSLine3D... one or more OTSLine3D. The last point of the first must match the first of the second, etc.
     * @return OTSLine3D
     * @throws OTSGeometryException if zero lines are given, or when there is a gap between consecutive lines
     */
    public static OTSLine3D concatenate(final OTSLine3D... lines) throws OTSGeometryException
    {
        if (0 == lines.length)
        {
            throw new OTSGeometryException("Empty argument list");
        }
        else if (1 == lines.length)
        {
            return lines[0];
        }
        int size = lines[0].size();
        for (int i = 1; i < lines.length; i++)
        {
            if (lines[i - 1].getLast().distance(lines[i].getFirst()).si > 0)
            {
                throw new OTSGeometryException("Lines are not connected");
            }
            size += lines[i].size() - 1;
        }
        OTSPoint3D[] points = new OTSPoint3D[size];
        int nextIndex = 0;
        for (int i = 0; i < lines.length; i++)
        {
            OTSLine3D line = lines[i];
            for (int j = 0 == i ? 0 : 1; j < line.size(); j++)
            {
                points[nextIndex++] = line.get(j);
            }
        }
        try
        {
            return new OTSLine3D(points);
        }
        catch (NetworkException exception)
        {
            // Cannot happen
            exception.printStackTrace();
            throw new Error(exception.getCause());
        }
    }

    /**
     * Construct a new OTSLine3D with all points of this OTSLine3D in reverse order.
     * @return OTSLine3D; the new OTSLine3D
     */
    OTSLine3D reverse()
    {
        OTSPoint3D[] resultPoints = new OTSPoint3D[size()];
        int nextIndex = size();
        for (OTSPoint3D p : getPoints())
        {
            resultPoints[--nextIndex] = p;
        }
        try
        {
            return new OTSLine3D(resultPoints);
        }
        catch (NetworkException exception)
        {
            // Cannot happen
            exception.printStackTrace();
            throw new Error(exception.getCause());
        }
    }

    /**
     * Construct a new OTSLine3D covering the indicated fraction of this OTSLine3D.
     * @param start double; starting point, valid range [0..<cite>end</cite>)
     * @param end double; ending point, valid range (<cite>start</cite>..1]
     * @return OTSLine3D; the new OTSLine3D
     * @throws OTSGeometryException when start >= end, or start < 0, or end > 1
     */
    OTSLine3D extractFractional(double start, double end) throws OTSGeometryException
    {
        if (start < 0 || start >= end || end > 1)
        {
            throw new OTSGeometryException("Bad interval");
        }
        getLength(); // computes and sets the length field
        return extract(start * this.length, end * this.length);
    }

    /**
     * Create anew OTSLine3D that covers a sub-section of this OTSLine3D.
     * @param start Length.Rel; the length along this OTSLine3D where the sub-section starts, valid range [0..<cite>end</cite>)
     * @param end Length.Rel; length along this OTSLine3D where the sub-section ends, valid range
     *            (<cite>start</cite>..<cite>length<cite> (length is the length of this OTSLine3D)
     * @return OTSLine3D; the selected sub-section
     * @throws OTSGeometryException when start >= end, or start < 0, or end > length
     */
    OTSLine3D extract(final Length.Rel start, final Length.Rel end) throws OTSGeometryException
    {
        return extract(start.si, end.si);
    }

    /**
     * Create a new OTSLine3D that covers a sub-section of this OTSLine3D.
     * @param start double; length along this OTSLine3D where the sub-section starts, valid range [0..<cite>end</cite>)
     * @param end double; length along this OTSLine3D where the sub-section ends, valid range
     *            (<cite>start</cite>..<cite>length<cite> (length is the length of this OTSLine3D)
     * @return OTSLine3D; the selected sub-section
     * @throws OTSGeometryException when start >= end, or start < 0, or end > length
     */
    OTSLine3D extract(double start, double end) throws OTSGeometryException
    {
        if (Double.isNaN(start) || Double.isNaN(end) || start < 0 || start >= end || end > getLength().si)
        {
            throw new OTSGeometryException("Bad interval (" + start + ".." + end + ")");
        }
        double cumulativeLength = 0;
        double nextCumulativeLength = 0;
        double segmentLength = 0;
        int index = 0;
        List<OTSPoint3D> pointList = new ArrayList<>();
        // System.err.println("interval " + start + ".." + end);
        while (start > cumulativeLength)
        {
            OTSPoint3D fromPoint = this.points[index];
            index++;
            OTSPoint3D toPoint = this.points[index];
            segmentLength = fromPoint.distanceSI(toPoint);
            cumulativeLength = nextCumulativeLength;
            nextCumulativeLength = cumulativeLength + segmentLength;
            if (nextCumulativeLength >= start)
            {
                break;
            }
        }
        if (start == nextCumulativeLength)
        {
            pointList.add(this.points[index]);
        }
        else
        {
            pointList.add(OTSPoint3D.interpolate((start - cumulativeLength) / segmentLength, this.points[index - 1],
                    this.points[index]));
            if (end > nextCumulativeLength)
            {
                pointList.add(this.points[index]);
            }
        }
        while (end > nextCumulativeLength)
        {
            OTSPoint3D fromPoint = this.points[index];
            index++;
            if (index >= this.points.length)
            {
                break; // rounding error
            }
            OTSPoint3D toPoint = this.points[index];
            segmentLength = fromPoint.distanceSI(toPoint);
            cumulativeLength = nextCumulativeLength;
            nextCumulativeLength = cumulativeLength + segmentLength;
            if (nextCumulativeLength >= end)
            {
                break;
            }
            pointList.add(toPoint);
        }
        if (end == nextCumulativeLength)
        {
            pointList.add(this.points[index]);
        }
        else
        {
            // System.err.println("interpolating between points " + (index - 1) + " and " + index);
            pointList.add(OTSPoint3D.interpolate((end - cumulativeLength) / segmentLength, this.points[index - 1],
                    this.points[index]));
        }
        // System.err.println("point list is");
        // for (OTSPoint3D p : pointList)
        // {
        // System.err.println("\t" + p);
        // }
        try
        {
            return new OTSLine3D(pointList);
        }
        catch (NetworkException exception)
        {
            System.err.println("interval " + start + ".." + end + "too short");
            throw new OTSGeometryException("interval " + start + ".." + end + "too short");
        }
    }

    /**
     * Build an array of OTSPoint3D from an array of Coordinate.
     * @param coordinates Coordinate[]; the coordinates
     * @return OTSPoint3D[]
     */
    private static OTSPoint3D[] coordinatesToOTSPoint3D(final Coordinate[] coordinates)
    {
        OTSPoint3D[] result = new OTSPoint3D[coordinates.length];
        for (int i = 0; i < coordinates.length; i++)
        {
            result[i] = new OTSPoint3D(coordinates[i]);
        }
        return result;
    }

    /**
     * Create an OTSLine3D, while cleaning repeating successive points.
     * @param points the coordinates of the line as OTSPoint3D
     * @return the line
     * @throws NetworkException when number of points &lt; 2
     */
    public static OTSLine3D createAndCleanOTSLine3D(final OTSPoint3D[] points) throws NetworkException
    {
        if (points.length < 2)
        {
            throw new NetworkException("Degenerate OTSLine3D; has " + points.length + " point"
                    + (points.length != 1 ? "s" : ""));
        }
        return createAndCleanOTSLine3D(new ArrayList<>(Arrays.asList(points)));
    }

    /**
     * Create an OTSLine3D, while cleaning repeating successive points.
     * @param pointList List&lt;OTSPoint3D&gt;; list of the coordinates of the line as OTSPoint3D; any duplicate points in this
     *            list are removed (this method may modify the provided list)
     * @return OTSLine3D; the line
     * @throws NetworkException when number of non-equal points &lt; 2
     */
    public static OTSLine3D createAndCleanOTSLine3D(final List<OTSPoint3D> pointList) throws NetworkException
    {
        // clean successive equal points
        int i = 1;
        while (i < pointList.size())
        {
            if (pointList.get(i - 1).equals(pointList.get(i)))
            {
                pointList.remove(i);
            }
            else
            {
                i++;
            }
        }
        return new OTSLine3D(pointList);
    }

    /**
     * @param coordinates the array of coordinates to construct this OTSLine3D from.
     * @throws NetworkException when the provided points do not constitute a valid line (too few points or identical adjacent
     *             points)
     */
    public OTSLine3D(final Coordinate[] coordinates) throws NetworkException
    {
        this(coordinatesToOTSPoint3D(coordinates));
    }

    /**
     * @param lineString the lineString to construct this OTSLine3D from.
     * @throws NetworkException when the provided points do not constitute a valid line (too few points or identical adjacent
     *             points)
     */
    public OTSLine3D(final LineString lineString) throws NetworkException
    {
        this(lineString.getCoordinates());
    }

    /**
     * @param geometry the geometry to construct this OTSLine3D from.
     * @throws NetworkException when the provided points do not constitute a valid line (too few points or identical adjacent
     *             points)
     */
    public OTSLine3D(final Geometry geometry) throws NetworkException
    {
        this(geometry.getCoordinates());
    }

    /**
     * @param pointList the list of points to construct this OTSLine3D from.
     * @throws NetworkException when the provided points do not constitute a valid line (too few points or identical adjacent
     *             points)
     */
    public OTSLine3D(final List<OTSPoint3D> pointList) throws NetworkException
    {
        this(pointList.toArray(new OTSPoint3D[pointList.size()]));
    }

    /**
     * @return an array of Coordinates corresponding to this OTSLine.
     */
    public final Coordinate[] getCoordinates()
    {
        Coordinate[] result = new Coordinate[size()];
        for (int i = 0; i < size(); i++)
        {
            result[i] = this.points[i].getCoordinate();
        }
        return result;
    }

    /**
     * @return a LineString corresponding to this OTSLine.
     */
    public final LineString getLineString()
    {
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordinates = getCoordinates();
        CoordinateSequence cs = factory.getCoordinateSequenceFactory().create(coordinates);
        return new LineString(cs, factory);
    }

    /**
     * @return the number of points on the line.
     */
    public final int size()
    {
        return this.points.length;
    }

    /**
     * @return the first point on the line
     */
    public final OTSPoint3D getFirst()
    {
        return this.points[0];
    }

    /**
     * @return the last point on the line
     */
    public final OTSPoint3D getLast()
    {
        return this.points[size() - 1];
    }

    /**
     * @param i the index of the point to retrieve
     * @return the i-th point of the line.
     * @throws OTSGeometryException when i &lt; 0 or i &gt; the number of points
     */
    public final OTSPoint3D get(final int i) throws OTSGeometryException
    {
        if (i < 0 || i > size() - 1)
        {
            throw new OTSGeometryException("OTSLine3D.get(i=" + i + "); i<0 or i>=size(), which is " + size());
        }
        return this.points[i];
    }

    /**
     * @return the length of the line in SI units.
     */
    public final synchronized double getLengthSI()
    {
        if (Double.isNaN(this.length))
        {
            this.length = 0.0;
            for (int i = 0; i < size() - 1; i++)
            {
                this.length += this.points[i].distanceSI(this.points[i + 1]);
            }
        }
        return this.length;
    }

    /**
     * @return the length of the line.
     */
    public final Length.Rel getLength()
    {
        return new Length.Rel(getLengthSI(), LengthUnit.SI);
    }

    /**
     * @return the points of this line.
     */
    public final OTSPoint3D[] getPoints()
    {
        return this.points;
    }

    /**
     * make the length indexed line if it does not exist yet, and cache it.
     */
    private void makeLengthIndexedLine()
    {
        if (this.lengthIndexedLine == null)
        {
            this.lengthIndexedLine = new double[this.points.length];
            this.lengthIndexedLine[0] = 0.0;
            for (int i = 1; i < this.points.length; i++)
            {
                this.lengthIndexedLine[i] = this.lengthIndexedLine[i - 1] + this.points[i - 1].distanceSI(this.points[i]);
            }
        }
    }

    /**
     * Get the location at a position on the line, with its direction. Position can be below 0 or more than the line length. In
     * that case, the position will be extrapolated in the direction of the line at its start or end.
     * @param position the position on the line for which to calculate the point on, before, of after the line
     * @return a directed point
     * @throws NetworkException when position could not be calculated
     */
    public final DirectedPoint getLocationExtended(final Length.Rel position) throws NetworkException
    {
        return getLocationExtendedSI(position.getSI());
    }

    /**
     * Get the location at a position on the line, with its direction. Position can be below 0 or more than the line length. In
     * that case, the position will be extrapolated in the direction of the line at its start or end.
     * @param positionSI the position on the line for which to calculate the point on, before, of after the line, in SI units
     * @return a directed point
     * @throws NetworkException when position could not be calculated
     */
    public final DirectedPoint getLocationExtendedSI(final double positionSI) throws NetworkException
    {
        makeLengthIndexedLine();
        if (positionSI >= 0.0 && positionSI <= getLengthSI())
        {
            return getLocationSI(positionSI);
        }

        // position before start point -- extrapolate
        if (positionSI < 0.0)
        {
            double len = positionSI;
            double fraction = len / (this.lengthIndexedLine[1] - this.lengthIndexedLine[0]);
            OTSPoint3D p1 = this.points[0];
            OTSPoint3D p2 = this.points[1];
            return new DirectedPoint(p1.x + fraction * (p2.x - p1.x), p1.y + fraction * (p2.y - p1.y), p1.z + fraction
                    * (p2.z - p1.z), 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
        }

        // position beyond end point -- extrapolate
        int n1 = this.lengthIndexedLine.length - 1;
        int n2 = this.lengthIndexedLine.length - 2;
        double len = positionSI - getLengthSI();
        double fraction = len / (this.lengthIndexedLine[n1] - this.lengthIndexedLine[n2]);
        OTSPoint3D p1 = this.points[n2];
        OTSPoint3D p2 = this.points[n1];
        return new DirectedPoint(p2.x + fraction * (p2.x - p1.x), p2.y + fraction * (p2.y - p1.y), p2.z + fraction
                * (p2.z - p1.z), 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
    }

    /**
     * Get the location at a fraction of the line, with its direction. Fraction should be between 0.0 and 1.0.
     * @param fraction the fraction for which to calculate the point on the line
     * @return a directed point
     * @throws NetworkException when fraction less than 0.0 or more than 1.0.
     */
    public final DirectedPoint getLocationFraction(final double fraction) throws NetworkException
    {
        if (fraction < 0.0 || fraction > 1.0)
        {
            throw new NetworkException("getLocationFraction for line: fraction < 0.0 or > 1.0. fraction = " + fraction);
        }
        return getLocationSI(fraction * getLengthSI());
    }

    /**
     * Get the location at a position on the line, with its direction. Position should be between 0.0 and line length.
     * @param position the position on the line for which to calculate the point on the line
     * @return a directed point
     * @throws NetworkException when position less than 0.0 or more than line length.
     */
    public final DirectedPoint getLocation(final Length.Rel position) throws NetworkException
    {
        return getLocationSI(position.getSI());
    }

    /**
     * Binary search for a position on the line.
     * @param pos the position to look for.
     * @return the index below the position; the position is between points[index] and points[index+1]
     * @throws NetworkException when index could not be found
     */
    private int find(final double pos) throws NetworkException
    {
        if (pos == 0)
        {
            return 0;
        }

        for (int i = 0; i < this.lengthIndexedLine.length - 2; i++)
        {
            if (pos > this.lengthIndexedLine[i] && pos <= this.lengthIndexedLine[i + 1])
            {
                return i;
            }
        }

        return this.lengthIndexedLine.length - 2;

        /*- binary variant
        int lo = 0;
        int hi = this.lengthIndexedLine.length - 1;
        while (lo <= hi)
        {
            if (hi - lo <= 1)
            {
                return lo;
            }
            int mid = lo + (hi - lo) / 2;
            if (pos < this.lengthIndexedLine[mid])
            {
                hi = mid - 1;
            }
            else if (pos > this.lengthIndexedLine[mid])
            {
                lo = mid + 1;
            }
        }
        throw new NetworkException("Could not find position " + pos + " on line with length indexes: "
            + this.lengthIndexedLine);
         */
    }

    /**
     * Get the location at a position on the line, with its direction. Position should be between 0.0 and line length.
     * @param positionSI the position on the line for which to calculate the point on the line
     * @return a directed point
     * @throws NetworkException when position less than 0.0 or more than line length.
     */
    public final DirectedPoint getLocationSI(final double positionSI) throws NetworkException
    {
        makeLengthIndexedLine();
        if (positionSI < 0.0 || positionSI > getLengthSI())
        {
            throw new NetworkException("getLocationSI for line: position < 0.0 or > line length. Position = " + positionSI
                    + " m. Length = " + getLengthSI() + " m.");
        }

        // handle special cases: position == 0.0, or position == length
        if (positionSI == 0.0)
        {
            OTSPoint3D p1 = this.points[0];
            OTSPoint3D p2 = this.points[1];
            return new DirectedPoint(p1.x, p1.y, p1.z, 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
        }
        if (positionSI == getLengthSI())
        {
            OTSPoint3D p1 = this.points[this.points.length - 2];
            OTSPoint3D p2 = this.points[this.points.length - 1];
            return new DirectedPoint(p2.x, p2.y, p2.z, 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
        }

        // find the index of the line segment, use binary search
        int index = find(positionSI);
        double remainder = positionSI - this.lengthIndexedLine[index];
        double fraction = remainder / (this.lengthIndexedLine[index + 1] - this.lengthIndexedLine[index]);
        OTSPoint3D p1 = this.points[index];
        OTSPoint3D p2 = this.points[index + 1];
        return new DirectedPoint(p1.x + fraction * (p2.x - p1.x), p1.y + fraction * (p2.y - p1.y), p1.z + fraction
                * (p2.z - p1.z), 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
    }

    /**
     * Truncate a line at the given length (less than the length of the line, and larger than zero) and return a new line.
     * @param lengthSI the location where to truncate the line
     * @return a new OTSLine3D truncated at the exact position where line.getLength() == lengthSI
     * @throws NetworkException when position less than 0.0 or more than line length.
     */
    public final OTSLine3D truncate(final double lengthSI) throws NetworkException
    {
        makeLengthIndexedLine();
        if (lengthSI <= 0.0 || lengthSI > getLengthSI())
        {
            throw new NetworkException("truncate for line: position <= 0.0 or > line length. Position = " + lengthSI
                    + " m. Length = " + getLengthSI() + " m.");
        }

        // handle special case: position == length
        if (lengthSI == getLengthSI())
        {
            return new OTSLine3D(getPoints());
        }

        // find the index of the line segment
        int index = find(lengthSI);
        double remainder = lengthSI - this.lengthIndexedLine[index];
        double fraction = remainder / (this.lengthIndexedLine[index + 1] - this.lengthIndexedLine[index]);
        OTSPoint3D p1 = this.points[index];
        OTSPoint3D p2 = this.points[index + 1];
        OTSPoint3D newLastPoint =
                new OTSPoint3D(p1.x + fraction * (p2.x - p1.x), p1.y + fraction * (p2.y - p1.y), p1.z + fraction
                        * (p2.z - p1.z));
        OTSPoint3D[] coords = new OTSPoint3D[index + 2];
        for (int i = 0; i <= index; i++)
        {
            coords[i] = this.points[i];
        }
        coords[index + 1] = newLastPoint;
        return new OTSLine3D(coords);
    }

    /**
     * Calculate the centroid of this line, and the bounds, and cache for later use. Make sure the dx, dy and dz are at least
     * 0.5 m wide.
     */
    private void calcCentroidBounds()
    {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (OTSPoint3D p : this.points)
        {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            minZ = Math.min(minZ, p.z);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
            maxZ = Math.max(maxZ, p.z);
        }
        this.centroid = new OTSPoint3D((maxX + minX) / 2, (maxY + minY) / 2, (maxZ + minZ) / 2);
        double deltaX = Math.max(maxX - minX, 0.5);
        double deltaY = Math.max(maxY - minY, 0.5);
        double deltaZ = Math.max(maxZ - minZ, 0.5);
        this.bounds = new BoundingBox(deltaX, deltaY, deltaZ);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation()
    {
        if (this.centroid == null)
        {
            calcCentroidBounds();
        }
        return this.centroid.getDirectedPoint();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds getBounds()
    {
        if (this.bounds == null)
        {
            calcCentroidBounds();
        }
        return this.bounds;
    }
    
    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return Arrays.toString(this.points);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.points);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({ "checkstyle:designforextension", "checkstyle:needbraces" })
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OTSLine3D other = (OTSLine3D) obj;
        if (!Arrays.equals(this.points, other.points))
            return false;
        return true;
    }

}

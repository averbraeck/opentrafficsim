package org.opentrafficsim.core.geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import org.djunits.unit.DirectionUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LengthIndexedLine;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Line with OTSPoint3D points, a cached length indexed line, a cached length, and a cached centroid (all calculated on first
 * use).
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class OTSLine3D implements Locatable, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** The points of the line. */
    private OTSPoint3D[] points;

    /** The cumulative length of the line at point 'i'. */
    private double[] lengthIndexedLine = null;

    /** The cached length; will be calculated at time of construction. */
    private Length length;

    /** The cached centroid; will be calculated when needed for the first time. */
    private OTSPoint3D centroid = null;

    /** The cached bounds; will be calculated when needed for the first time. */
    private Bounds bounds = null;

    /** The cached helper points for fractional projection; will be calculated when needed for the first time. */
    private OTSPoint3D[] fractionalHelperCenters = null;

    /** The cached helper directions for fractional projection; will be calculated when needed for the first time. */
    private Point2D.Double[] fractionalHelperDirections = null;

    /** Intersection of unit offset lines of first two segments. */
    private OTSPoint3D firstOffsetIntersection;

    /** Intersection of unit offset lines of last two segments. */
    private OTSPoint3D lastOffsetIntersection;

    /** Precision for fractional projection algorithm. */
    private static final double FRAC_PROJ_PRECISION = 2e-5 /* PK too fine 1e-6 */;

    /** Radius at each vertex. */
    private Length[] vertexRadii;

    /** Bounding of this OTSLine3D. */
    private Envelope envelope;

    /**
     * Construct a new OTSLine3D.
     * @param points OTSPoint3D...; the array of points to construct this OTSLine3D from.
     * @throws OTSGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSLine3D(final OTSPoint3D... points) throws OTSGeometryException
    {
        init(points);
    }

    /**
     * Construct a new OTSLine3D, and immediately make the length-indexed line.
     * @param pts OTSPoint3D...; the array of points to construct this OTSLine3D from.
     * @throws OTSGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    private void init(final OTSPoint3D... pts) throws OTSGeometryException
    {
        if (pts.length < 2)
        {
            throw new OTSGeometryException("Degenerate OTSLine3D; has " + pts.length + " point" + (pts.length != 1 ? "s" : ""));
        }
        this.lengthIndexedLine = new double[pts.length];
        this.lengthIndexedLine[0] = 0.0;
        for (int i = 1; i < pts.length; i++)
        {
            if (pts[i - 1].x == pts[i].x && pts[i - 1].y == pts[i].y && pts[i - 1].z == pts[i].z)
            {
                throw new OTSGeometryException(
                        "Degenerate OTSLine3D; point " + (i - 1) + " has the same x, y and z as point " + i);
            }
            this.lengthIndexedLine[i] = this.lengthIndexedLine[i - 1] + pts[i - 1].distanceSI(pts[i]);
        }
        this.points = pts;
        this.length = Length.instantiateSI(this.lengthIndexedLine[this.lengthIndexedLine.length - 1]);
    }

    /** Which offsetLine method to use... */
    public enum OffsetMethod
    {
        /** Via JTS buffer. */
        JTS,

        /** Peter Knoppers. */
        PK;
    };

    /** Which offset line method to use... */
    public static final OffsetMethod OFFSETMETHOD = OffsetMethod.PK;

    /**
     * Construct parallel line.<br>
     * TODO Let the Z-component of the result follow the Z-values of the reference line.
     * @param offset double; offset distance from the reference line; positive is LEFT, negative is RIGHT
     * @return OTSLine3D; the line that has the specified offset from the reference line
     */
    public final OTSLine3D offsetLine(final double offset)
    {
        try
        {
            switch (OFFSETMETHOD)
            {
                case PK:
                    return OTSOffsetLinePK.offsetLine(this, offset);

                case JTS:
                    return OTSBufferingJTS.offsetGeometryOLD(this, offset);

                default:
                    return null;
            }
        }
        catch (OTSGeometryException exception)
        {
            CategoryLogger.always().error(exception);
            return null;
        }
    }

    /**
     * Construct a line that is equal to this line except for segments that are shorter than the <cite>noiseLevel</cite>. The
     * result is guaranteed to start with the first point of this line and end with the last point of this line.
     * @param noiseLevel double; the minimum segment length that is <b>not</b> removed
     * @return OTSLine3D; the filtered line
     */
    public final OTSLine3D noiseFilteredLine(final double noiseLevel)
    {
        if (this.size() <= 2)
        {
            return this; // Except for some cached fields; an OTSLine3D is immutable; so safe to return
        }
        OTSPoint3D prevPoint = null;
        List<OTSPoint3D> list = null;
        for (int index = 0; index < this.size(); index++)
        {
            OTSPoint3D currentPoint = this.points[index];
            if (null != prevPoint && prevPoint.distanceSI(currentPoint) < noiseLevel)
            {
                if (null == list)
                {
                    // Found something to filter; copy this up to (and including) prevPoint
                    list = new ArrayList<>();
                    for (int i = 0; i < index; i++)
                    {
                        list.add(this.points[i]);
                    }
                }
                if (index == this.size() - 1)
                {
                    if (list.size() > 1)
                    {
                        // Replace the last point of the result by the last point of this OTSLine3D
                        list.set(list.size() - 1, currentPoint);
                    }
                    else
                    {
                        // Append the last point of this even though it is close to the first point than the noise value to
                        // comply with the requirement that first and last point of this are ALWAYS included in the result.
                        list.add(currentPoint);
                    }
                }
                continue; // Do not replace prevPoint by currentPoint
            }
            else if (null != list)
            {
                list.add(currentPoint);
            }
            prevPoint = currentPoint;
        }
        if (null == list)
        {
            return this;
        }
        if (list.size() == 2 && list.get(0).equals(list.get(1)))
        {
            // CategoryLogger.always().debug("Fixing up degenerate noiseFilteredLine by inserting an intermediate point");
            // Find something to insert along the way
            for (int index = 1; index < this.size() - 1; index++)
            {
                if (!this.points[index].equals(list.get(0)))
                {
                    list.add(1, this.points[index]);
                    break;
                }
            }
        }
        try
        {
            return new OTSLine3D(list);
        }
        catch (OTSGeometryException exception)
        {
            CategoryLogger.always().error(exception);
            throw new Error(exception);
        }
    }

    /**
     * Clean up a list of points that describe a polyLine by removing points that lie within epsilon distance of a more
     * straightened version of the line. <br>
     * TODO Test this code (currently untested).
     * @param epsilon double; maximal deviation
     * @param useHorizontalDistance boolean; if true; the horizontal distance is used; if false; the 3D distance is used
     * @return OTSLine3D; a new OTSLine3D containing all the remaining points
     */
    public final OTSLine3D noiseFilterRamerDouglasPeuker(final double epsilon, final boolean useHorizontalDistance)
    {
        // TODO rename this filter to noiseFilterRamerDouglasPeucker (with a c in Peucker).
        try
        {
            // Apply the Ramer-Douglas-Peucker algorithm to the buffered points.
            // Adapted from https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
            double maxDeviation = 0;
            int splitIndex = -1;
            int pointCount = size();
            OTSLine3D straight = new OTSLine3D(get(0), get(pointCount - 1));
            // Find the point with largest deviation from the straight line from start point to end point
            for (int i = 1; i < pointCount - 1; i++)
            {
                OTSPoint3D point = get(i);
                OTSPoint3D closest =
                        useHorizontalDistance ? point.closestPointOnLine2D(straight) : point.closestPointOnLine(straight);
                double deviation = useHorizontalDistance ? closest.horizontalDistanceSI(point) : closest.distanceSI(point);
                if (deviation > maxDeviation)
                {
                    splitIndex = i;
                    maxDeviation = deviation;
                }
            }
            if (maxDeviation <= epsilon)
            {
                // All intermediate points can be dropped. Return a new list containing only the first and last point.
                return straight;
            }
            // The largest deviation is larger than epsilon.
            // Split the polyLine at the point with the maximum deviation. Process each sub list recursively and concatenate the
            // results
            OTSLine3D first = new OTSLine3D(Arrays.copyOfRange(this.points, 0, splitIndex + 1))
                    .noiseFilterRamerDouglasPeuker(epsilon, useHorizontalDistance);
            OTSLine3D second = new OTSLine3D(Arrays.copyOfRange(this.points, splitIndex, this.points.length))
                    .noiseFilterRamerDouglasPeuker(epsilon, useHorizontalDistance);
            return concatenate(epsilon, first, second);
        }
        catch (OTSGeometryException exception)
        {
            CategoryLogger.always().error(exception); // Peter thinks this cannot happen ...
            return null;
        }
    }

    /**
     * Create a line at linearly varying offset from this line. The offset may change linearly from its initial value at the
     * start of the reference line to its final offset value at the end of the reference line.
     * @param offsetAtStart double; offset at the start of the reference line (positive value is Left, negative value is Right)
     * @param offsetAtEnd double; offset at the end of the reference line (positive value is Left, negative value is Right)
     * @return Geometry; the Geometry of the line at linearly changing offset of the reference line
     * @throws OTSGeometryException when this method fails to create the offset line
     */
    public final OTSLine3D offsetLine(final double offsetAtStart, final double offsetAtEnd) throws OTSGeometryException
    {
        // CategoryLogger.trace(Cat.CORE, OTSGeometry.printCoordinates("#referenceLine: \nc1,0,0\n# offset at start is "
        // + offsetAtStart + " at end is " + offsetAtEnd + "\n#", referenceLine, "\n "));

        OTSLine3D offsetLineAtStart = offsetLine(offsetAtStart);
        if (offsetAtStart == offsetAtEnd)
        {
            return offsetLineAtStart; // offset does not change
        }
        // CategoryLogger.trace(Cat.CORE, OTSGeometry.printCoordinates("#offset line at start: \nc0,0,0\n#",
        // offsetLineAtStart, "\n "));
        OTSLine3D offsetLineAtEnd = offsetLine(offsetAtEnd);
        // CategoryLogger.trace(Cat.CORE, OTSGeometry.printCoordinates("#offset line at end: \nc0.7,0.7,0.7\n#",
        // offsetLineAtEnd, "\n "));
        Geometry startGeometry = offsetLineAtStart.getLineString();
        Geometry endGeometry = offsetLineAtEnd.getLineString();
        LengthIndexedLine first = new LengthIndexedLine(startGeometry);
        double firstLength = startGeometry.getLength();
        LengthIndexedLine second = new LengthIndexedLine(endGeometry);
        double secondLength = endGeometry.getLength();
        ArrayList<Coordinate> out = new ArrayList<>();
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
     * Create a line at linearly varying offset from this line. The offset may change linearly from its initial value at the
     * start of the reference line via a number of intermediate offsets at intermediate positions to its final offset value at
     * the end of the reference line.
     * @param relativeFractions double[]; positional fractions for which the offsets have to be generated
     * @param offsets double[]; offsets at the relative positions (positive value is Left, negative value is Right)
     * @return Geometry; the Geometry of the line at linearly changing offset of the reference line
     * @throws OTSGeometryException when this method fails to create the offset line
     */
    public final OTSLine3D offsetLine(final double[] relativeFractions, final double[] offsets) throws OTSGeometryException
    {
        OTSLine3D[] offsetLine = new OTSLine3D[relativeFractions.length];
        for (int i = 0; i < offsets.length; i++)
        {
            offsetLine[i] = offsetLine(offsets[i]);
            // CategoryLogger.trace(Cat.CORE, offsetLine[i].toExcel() + "\n");
        }

        ArrayList<Coordinate> out = new ArrayList<>();
        Coordinate prevCoordinate = null;
        final double tooClose = 0.05; // 5 cm
        for (int i = 0; i < offsets.length - 1; i++)
        {
            Geometry startGeometry =
                    offsetLine[i].extractFractional(relativeFractions[i], relativeFractions[i + 1]).getLineString();
            Geometry endGeometry =
                    offsetLine[i + 1].extractFractional(relativeFractions[i], relativeFractions[i + 1]).getLineString();
            LengthIndexedLine first = new LengthIndexedLine(startGeometry);
            double firstLength = startGeometry.getLength();
            LengthIndexedLine second = new LengthIndexedLine(endGeometry);
            double secondLength = endGeometry.getLength();
            Coordinate[] firstCoordinates = startGeometry.getCoordinates();
            Coordinate[] secondCoordinates = endGeometry.getCoordinates();
            int firstIndex = 0;
            int secondIndex = 0;
            while (firstIndex < firstCoordinates.length && secondIndex < secondCoordinates.length)
            {
                double firstRatio = firstIndex < firstCoordinates.length
                        ? first.indexOf(firstCoordinates[firstIndex]) / firstLength : Double.MAX_VALUE;
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
        }

        Coordinate[] resultCoordinates = new Coordinate[out.size()];
        for (int index = 0; index < out.size(); index++)
        {
            resultCoordinates[index] = out.get(index);
        }
        return new OTSLine3D(resultCoordinates);
    }

    /**
     * Concatenate several OTSLine3D instances.
     * @param lines OTSLine3D...; OTSLine3D... one or more OTSLine3D. The last point of the first
     *            &lt;strong&gt;must&lt;/strong&gt; match the first of the second, etc.
     * @return OTSLine3D
     * @throws OTSGeometryException if zero lines are given, or when there is a gap between consecutive lines
     */
    public static OTSLine3D concatenate(final OTSLine3D... lines) throws OTSGeometryException
    {
        return concatenate(0.0, lines);
    }

    /**
     * Concatenate two OTSLine3D instances. This method is separate for efficiency reasons.
     * @param toleranceSI double; the tolerance between the end point of a line and the first point of the next line
     * @param line1 OTSLine3D; first line
     * @param line2 OTSLine3D; second line
     * @return OTSLine3D
     * @throws OTSGeometryException if zero lines are given, or when there is a gap between consecutive lines
     */
    public static OTSLine3D concatenate(final double toleranceSI, final OTSLine3D line1, final OTSLine3D line2)
            throws OTSGeometryException
    {
        if (line1.getLast().distance(line2.getFirst()).si > toleranceSI)
        {
            throw new OTSGeometryException("Lines are not connected: " + line1.getLast() + " to " + line2.getFirst()
                    + " distance is " + line1.getLast().distance(line2.getFirst()).si + " > " + toleranceSI);
        }
        int size = line1.size() + line2.size() - 1;
        OTSPoint3D[] points = new OTSPoint3D[size];
        int nextIndex = 0;
        for (int j = 0; j < line1.size(); j++)
        {
            points[nextIndex++] = line1.get(j);
        }
        for (int j = 1; j < line2.size(); j++)
        {
            points[nextIndex++] = line2.get(j);
        }
        return new OTSLine3D(points);
    }

    /**
     * Concatenate several OTSLine3D instances.
     * @param toleranceSI double; the tolerance between the end point of a line and the first point of the next line
     * @param lines OTSLine3D...; OTSLine3D... one or more OTSLine3D. The last point of the first
     *            &lt;strong&gt;must&lt;/strong&gt; match the first of the second, etc.
     * @return OTSLine3D
     * @throws OTSGeometryException if zero lines are given, or when there is a gap between consecutive lines
     */
    public static OTSLine3D concatenate(final double toleranceSI, final OTSLine3D... lines) throws OTSGeometryException
    {
        // CategoryLogger.trace(Cat.CORE, "Concatenating " + lines.length + " lines.");
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
            if (lines[i - 1].getLast().distance(lines[i].getFirst()).si > toleranceSI)
            {
                throw new OTSGeometryException(
                        "Lines are not connected: " + lines[i - 1].getLast() + " to " + lines[i].getFirst() + " distance is "
                                + lines[i - 1].getLast().distance(lines[i].getFirst()).si + " > " + toleranceSI);
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
        return new OTSLine3D(points);
    }

    /**
     * Construct a new OTSLine3D with all points of this OTSLine3D in reverse order.
     * @return OTSLine3D; the new OTSLine3D
     */
    public final OTSLine3D reverse()
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
        catch (OTSGeometryException exception)
        {
            // Cannot happen
            throw new RuntimeException(exception);
        }
    }

    /**
     * Construct a new OTSLine3D covering the indicated fraction of this OTSLine3D.
     * @param start double; starting point, valid range [0..<cite>end</cite>)
     * @param end double; ending point, valid range (<cite>start</cite>..1]
     * @return OTSLine3D; the new OTSLine3D
     * @throws OTSGeometryException when start &gt;= end, or start &lt; 0, or end &gt; 1
     */
    public final OTSLine3D extractFractional(final double start, final double end) throws OTSGeometryException
    {
        if (start < 0 || start >= end || end > 1)
        {
            throw new OTSGeometryException(
                    "Bad interval (start=" + start + ", end=" + end + ", this is " + this.toString() + ")");
        }
        return extract(start * this.length.si, end * this.length.si);
    }

    /**
     * Create a new OTSLine3D that covers a sub-section of this OTSLine3D.
     * @param start Length; the length along this OTSLine3D where the sub-section starts, valid range [0..<cite>end</cite>)
     * @param end Length; length along this OTSLine3D where the sub-section ends, valid range
     *            (<cite>start</cite>..<cite>length</cite> (length is the length of this OTSLine3D)
     * @return OTSLine3D; the selected sub-section
     * @throws OTSGeometryException when start &gt;= end, or start &lt; 0, or end &gt; length
     */
    public final OTSLine3D extract(final Length start, final Length end) throws OTSGeometryException
    {
        return extract(start.si, end.si);
    }

    /**
     * Create a new OTSLine3D that covers a sub-section of this OTSLine3D.
     * @param start double; length along this OTSLine3D where the sub-section starts, valid range [0..<cite>end</cite>)
     * @param end double; length along this OTSLine3D where the sub-section ends, valid range
     *            (<cite>start</cite>..<cite>length</cite> (length is the length of this OTSLine3D)
     * @return OTSLine3D; the selected sub-section
     * @throws OTSGeometryException when start &gt;= end, or start &lt; 0, or end &gt; length
     */
    public final OTSLine3D extract(final double start, final double end) throws OTSGeometryException
    {
        if (Double.isNaN(start) || Double.isNaN(end) || start < 0 || start >= end || end > getLengthSI())
        {
            throw new OTSGeometryException(
                    "Bad interval (" + start + ".." + end + "; length of this OTSLine3D is " + this.getLengthSI() + ")");
        }
        double cumulativeLength = 0;
        double nextCumulativeLength = 0;
        double segmentLength = 0;
        int index = 0;
        List<OTSPoint3D> pointList = new ArrayList<>();
        // CategoryLogger.trace(Cat.CORE, "interval " + start + ".." + end);
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
            OTSPoint3D point = OTSPoint3D.interpolate((end - cumulativeLength) / segmentLength, this.points[index - 1],
                    this.points[index]);
            // can be the same due to rounding
            if (!point.equals(pointList.get(pointList.size() - 1)))
            {
                pointList.add(point);
            }
        }
        try
        {
            return new OTSLine3D(pointList);
        }
        catch (OTSGeometryException exception)
        {
            CategoryLogger.always().error(exception, "interval " + start + ".." + end + " too short");
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
     * @param points OTSPoint3D...; the coordinates of the line as OTSPoint3D
     * @return the line
     * @throws OTSGeometryException when number of points &lt; 2
     */
    public static OTSLine3D createAndCleanOTSLine3D(final OTSPoint3D... points) throws OTSGeometryException
    {
        if (points.length < 2)
        {
            throw new OTSGeometryException(
                    "Degenerate OTSLine3D; has " + points.length + " point" + (points.length != 1 ? "s" : ""));
        }
        return createAndCleanOTSLine3D(new ArrayList<>(Arrays.asList(points)));
    }

    /**
     * Create an OTSLine3D, while cleaning repeating successive points.
     * @param pointList List&lt;OTSPoint3D&gt;; list of the coordinates of the line as OTSPoint3D; any duplicate points in this
     *            list are removed (this method may modify the provided list)
     * @return OTSLine3D; the line
     * @throws OTSGeometryException when number of non-equal points &lt; 2
     */
    public static OTSLine3D createAndCleanOTSLine3D(final List<OTSPoint3D> pointList) throws OTSGeometryException
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
     * Construct a new OTSLine3D from an array of Coordinate.
     * @param coordinates Coordinate[]; the array of coordinates to construct this OTSLine3D from
     * @throws OTSGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSLine3D(final Coordinate[] coordinates) throws OTSGeometryException
    {
        this(coordinatesToOTSPoint3D(coordinates));
    }

    /**
     * Construct a new OTSLine3D from a LineString.
     * @param lineString LineString; the lineString to construct this OTSLine3D from.
     * @throws OTSGeometryException when the provided LineString does not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSLine3D(final LineString lineString) throws OTSGeometryException
    {
        this(lineString.getCoordinates());
    }

    /**
     * Construct a new OTSLine3D from a Geometry.
     * @param geometry Geometry; the geometry to construct this OTSLine3D from
     * @throws OTSGeometryException when the provided Geometry do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSLine3D(final Geometry geometry) throws OTSGeometryException
    {
        this(geometry.getCoordinates());
    }

    /**
     * Construct a new OTSLine3D from a List&lt;OTSPoint3D&gt;.
     * @param pointList List&lt;OTSPoint3D&gt;; the list of points to construct this OTSLine3D from.
     * @throws OTSGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSLine3D(final List<OTSPoint3D> pointList) throws OTSGeometryException
    {
        this(pointList.toArray(new OTSPoint3D[pointList.size()]));
    }

    /**
     * Construct a new OTSShape (closed shape) from a Path2D.
     * @param path Path2D; the Path2D to construct this OTSLine3D from.
     * @throws OTSGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSLine3D(final Path2D path) throws OTSGeometryException
    {
        List<OTSPoint3D> pl = new ArrayList<>();
        for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next())
        {
            double[] p = new double[6];
            int segType = pi.currentSegment(p);
            if (segType == PathIterator.SEG_MOVETO || segType == PathIterator.SEG_LINETO)
            {
                pl.add(new OTSPoint3D(p[0], p[1]));
            }
            else if (segType == PathIterator.SEG_CLOSE)
            {
                if (!pl.get(0).equals(pl.get(pl.size() - 1)))
                {
                    pl.add(new OTSPoint3D(pl.get(0).x, pl.get(0).y));
                }
                break;
            }
        }
        init(pl.toArray(new OTSPoint3D[pl.size() - 1]));
    }

    /**
     * Construct a Coordinate array and fill it with the points of this OTSLine3D.
     * @return an array of Coordinates corresponding to this OTSLine
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
     * Construct a LineString from this OTSLine3D.
     * @return a LineString corresponding to this OTSLine
     */
    public final LineString getLineString()
    {
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordinates = getCoordinates();
        CoordinateSequence cs = factory.getCoordinateSequenceFactory().create(coordinates);
        return new LineString(cs, factory);
    }

    /**
     * Return the number of points in this OTSLine3D.
     * @return the number of points on the line
     */
    public final int size()
    {
        return this.points.length;
    }

    /**
     * Return the first point of this OTSLine3D.
     * @return the first point on the line
     */
    public final OTSPoint3D getFirst()
    {
        return this.points[0];
    }

    /**
     * Return the last point of this OTSLine3D.
     * @return the last point on the line
     */
    public final OTSPoint3D getLast()
    {
        return this.points[size() - 1];
    }

    /**
     * Return one point of this OTSLine3D.
     * @param i int; the index of the point to retrieve
     * @return OTSPoint3d; the i-th point of the line
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
     * Return the length of this OTSLine3D as a double value in SI units. (Assumes that the coordinates of the points
     * constituting this line are expressed in meters.)
     * @return the length of the line in SI units
     */
    public final double getLengthSI()
    {
        return this.length.si;
    }

    /**
     * Return the length of this OTSLine3D in meters. (Assuming that the coordinates of the points constituting this line are
     * expressed in meters.)
     * @return the length of the line
     */
    public final Length getLength()
    {
        return this.length;
    }

    /**
     * Return an array of OTSPoint3D that represents this OTSLine3D. <strong>Do not modify the result.</strong>
     * @return the points of this line
     */
    public final OTSPoint3D[] getPoints()
    {
        return this.points;
    }

    /**
     * Make the length indexed line if it does not exist yet, and cache it.
     */
    private synchronized void makeLengthIndexedLine()
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
     * @param position Length; the position on the line for which to calculate the point on, before, of after the line
     * @return a directed point
     */
    public final DirectedPoint getLocationExtended(final Length position)
    {
        return getLocationExtendedSI(position.getSI());
    }

    /**
     * Get the location at a position on the line, with its direction. Position can be below 0 or more than the line length. In
     * that case, the position will be extrapolated in the direction of the line at its start or end.
     * @param positionSI double; the position on the line for which to calculate the point on, before, of after the line, in SI
     *            units
     * @return a directed point
     */
    public final synchronized DirectedPoint getLocationExtendedSI(final double positionSI)
    {
        makeLengthIndexedLine();
        if (positionSI >= 0.0 && positionSI <= getLengthSI())
        {
            try
            {
                return getLocationSI(positionSI);
            }
            catch (OTSGeometryException exception)
            {
                // cannot happen
            }
        }

        // position before start point -- extrapolate
        if (positionSI < 0.0)
        {
            double len = positionSI;
            double fraction = len / (this.lengthIndexedLine[1] - this.lengthIndexedLine[0]);
            OTSPoint3D p1 = this.points[0];
            OTSPoint3D p2 = this.points[1];
            return new DirectedPoint(p1.x + fraction * (p2.x - p1.x), p1.y + fraction * (p2.y - p1.y),
                    p1.z + fraction * (p2.z - p1.z), 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
        }

        // position beyond end point -- extrapolate
        int n1 = this.lengthIndexedLine.length - 1;
        int n2 = this.lengthIndexedLine.length - 2;
        double len = positionSI - getLengthSI();
        double fraction = len / (this.lengthIndexedLine[n1] - this.lengthIndexedLine[n2]);
        OTSPoint3D p1 = this.points[n2];
        OTSPoint3D p2 = this.points[n1];
        return new DirectedPoint(p2.x + fraction * (p2.x - p1.x), p2.y + fraction * (p2.y - p1.y),
                p2.z + fraction * (p2.z - p1.z), 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
    }

    /**
     * Get the location at a fraction of the line, with its direction. Fraction should be between 0.0 and 1.0.
     * @param fraction double; the fraction for which to calculate the point on the line
     * @return a directed point
     * @throws OTSGeometryException when fraction less than 0.0 or more than 1.0.
     */
    public final DirectedPoint getLocationFraction(final double fraction) throws OTSGeometryException
    {
        if (fraction < 0.0 || fraction > 1.0)
        {
            throw new OTSGeometryException("getLocationFraction for line: fraction < 0.0 or > 1.0. fraction = " + fraction);
        }
        return getLocationSI(fraction * getLengthSI());
    }

    /**
     * Get the location at a fraction of the line, with its direction. Fraction should be between 0.0 and 1.0.
     * @param fraction double; the fraction for which to calculate the point on the line
     * @param tolerance double; the delta from 0.0 and 1.0 that will be forgiven
     * @return a directed point
     * @throws OTSGeometryException when fraction less than 0.0 or more than 1.0.
     */
    public final DirectedPoint getLocationFraction(final double fraction, final double tolerance) throws OTSGeometryException
    {
        if (fraction < -tolerance || fraction > 1.0 + tolerance)
        {
            throw new OTSGeometryException(
                    "getLocationFraction for line: fraction < 0.0 - tolerance or > 1.0 + tolerance; fraction = " + fraction);
        }
        double f = fraction < 0 ? 0.0 : fraction > 1.0 ? 1.0 : fraction;
        return getLocationSI(f * getLengthSI());
    }

    /**
     * Get the location at a fraction of the line (or outside the line), with its direction.
     * @param fraction double; the fraction for which to calculate the point on the line
     * @return a directed point
     */
    public final DirectedPoint getLocationFractionExtended(final double fraction)
    {
        return getLocationExtendedSI(fraction * getLengthSI());
    }

    /**
     * Get the location at a position on the line, with its direction. Position should be between 0.0 and line length.
     * @param position Length; the position on the line for which to calculate the point on the line
     * @return a directed point
     * @throws OTSGeometryException when position less than 0.0 or more than line length.
     */
    public final DirectedPoint getLocation(final Length position) throws OTSGeometryException
    {
        return getLocationSI(position.getSI());
    }

    /**
     * Binary search for a position on the line.
     * @param pos double; the position to look for.
     * @return the index below the position; the position is between points[index] and points[index+1]
     * @throws OTSGeometryException when index could not be found
     */
    private int find(final double pos) throws OTSGeometryException
    {
        if (pos == 0)
        {
            return 0;
        }

        int lo = 0;
        int hi = this.lengthIndexedLine.length - 1;
        while (lo <= hi)
        {
            if (hi == lo)
            {
                return lo;
            }
            int mid = lo + (hi - lo) / 2;
            if (pos < this.lengthIndexedLine[mid])
            {
                hi = mid - 1;
            }
            else if (pos > this.lengthIndexedLine[mid + 1])
            {
                lo = mid + 1;
            }
            else
            {
                return mid;
            }
        }
        throw new OTSGeometryException(
                "Could not find position " + pos + " on line with length indexes: " + Arrays.toString(this.lengthIndexedLine));
    }

    /**
     * Get the location at a position on the line, with its direction. Position should be between 0.0 and line length.
     * @param positionSI double; the position on the line for which to calculate the point on the line
     * @return a directed point
     * @throws OTSGeometryException when position less than 0.0 or more than line length.
     */
    public final synchronized DirectedPoint getLocationSI(final double positionSI) throws OTSGeometryException
    {
        makeLengthIndexedLine();
        if (positionSI < 0.0 || positionSI > getLengthSI())
        {
            throw new OTSGeometryException("getLocationSI for line: position < 0.0 or > line length. Position = " + positionSI
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
        return new DirectedPoint(p1.x + fraction * (p2.x - p1.x), p1.y + fraction * (p2.y - p1.y),
                p1.z + fraction * (p2.z - p1.z), 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
    }

    /**
     * Truncate a line at the given length (less than the length of the line, and larger than zero) and return a new line.
     * @param lengthSI double; the location where to truncate the line
     * @return a new OTSLine3D truncated at the exact position where line.getLength() == lengthSI
     * @throws OTSGeometryException when position less than 0.0 or more than line length.
     */
    public final synchronized OTSLine3D truncate(final double lengthSI) throws OTSGeometryException
    {
        makeLengthIndexedLine();
        if (lengthSI <= 0.0 || lengthSI > getLengthSI())
        {
            throw new OTSGeometryException("truncate for line: position <= 0.0 or > line length. Position = " + lengthSI
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
        OTSPoint3D lastPoint;
        if (0.0 == fraction)
        {
            index--;
            lastPoint = p1;
        }
        else
        {
            OTSPoint3D p2 = this.points[index + 1];
            lastPoint = new OTSPoint3D(p1.x + fraction * (p2.x - p1.x), p1.y + fraction * (p2.y - p1.y),
                    p1.z + fraction * (p2.z - p1.z));

        }
        OTSPoint3D[] coords = new OTSPoint3D[index + 2];
        for (int i = 0; i <= index; i++)
        {
            coords[i] = this.points[i];
        }
        coords[index + 1] = lastPoint;
        return new OTSLine3D(coords);
    }

    /*-
     * TODO finish this method if it is needed; remove otherwise.
     * Calculate the first point on this line that intersects somewhere with the provided line, or NaN if no intersection was
     * found.
     * @param line the line to test the intersection with
     * @return the fraction of the first intersection point
     *
    public final double firstIntersectionFraction(final OTSLine3D line)
    {
        List<Line2D.Double> segs = new ArrayList<>();
        for (int j = 1; j < line.getPoints().length; j++)
        {
            Line2D.Double seg =
                new Line2D.Double(this.points[j - 1].x, this.points[j - 1].y, this.points[j].x, this.points[j].y);
            segs.add(seg);
    
        }
        for (int i = 1; i < this.points.length; i++)
        {
            Line2D.Double thisSeg =
                new Line2D.Double(this.points[i - 1].x, this.points[i - 1].y, this.points[i].x, this.points[i].y);
            for (Line2D.Double seg : segs)
            {
                if (thisSeg.intersectsLine(seg))
                {
                    // Point2D.Double intersectionPoint = thisSeg.
                    
                }
            }
        }
        return Double.NaN;
    }
     */

    /**
     * Returns the fractional position along this line of the orthogonal projection of point (x, y) on this line. If the point
     * is not orthogonal to the closest line segment, the nearest point is selected.
     * @param x double; x-coordinate of point to project
     * @param y double; y-coordinate of point to project
     * @return fractional position along this line of the orthogonal projection on this line of a point
     */
    public final synchronized double projectOrthogonal(final double x, final double y)
    {

        // prepare
        makeLengthIndexedLine();
        double minDistance = Double.POSITIVE_INFINITY;
        double minSegmentFraction = 0;
        int minSegment = -1;

        // code based on Line2D.ptSegDistSq(...)
        for (int i = 0; i < size() - 1; i++)
        {
            double dx = this.points[i + 1].x - this.points[i].x;
            double dy = this.points[i + 1].y - this.points[i].y;
            // vector relative to (x(i), y(i))
            double px = x - this.points[i].x;
            double py = y - this.points[i].y;
            // dot product
            double dot1 = px * dx + py * dy;
            double f;
            double distance;
            if (dot1 > 0)
            {
                // vector relative to (x(i+1), y(i+1))
                px = dx - px;
                py = dy - py;
                // dot product
                double dot2 = px * dx + py * dy;
                if (dot2 > 0)
                {
                    // projection on line segment
                    double len2 = dx * dx + dy * dy;
                    double proj = dot2 * dot2 / len2;
                    f = dot1 / len2;
                    distance = px * px + py * py - proj;
                }
                else
                {
                    // dot<=0 projection 'after' line segment
                    f = 1;
                    distance = px * px + py * py;
                }
            }
            else
            {
                // dot<=0 projection 'before' line segment
                f = 0;
                distance = px * px + py * py;
            }
            // check if closer than previous
            if (distance < minDistance)
            {
                minDistance = distance;
                minSegmentFraction = f;
                minSegment = i;
            }
        }

        // return
        double segLen = this.lengthIndexedLine[minSegment + 1] - this.lengthIndexedLine[minSegment];
        return (this.lengthIndexedLine[minSegment] + segLen * minSegmentFraction) / getLengthSI();

    }

    /**
     * Returns the fractional projection of a point to a line. The projection works by taking slices in space per line segment
     * as shown below. A point is always projected to the nearest segment, but not necessarily to the closest point on that
     * segment. The slices in space are analogous to a Voronoi diagram, but for the line segments instead of points. If
     * fractional projection fails, the orthogonal projection is returned.<br>
     * <br>
     * The point 'A' is projected to point 'B' on the 3rd segment of line 'C-D'. The line from 'A' to 'B' extends towards point
     * 'E', which is the intersection of lines 'E-F' and 'E-G'. Line 'E-F' cuts the first bend of the 3rd segment (at point 'H')
     * in half, while the line 'E-G' cuts the second bend of the 3rd segment (at point 'I') in half.
     * 
     * <pre>
     *            ____________________________     G                   .
     * .         |                            |    .                 .
     *   .       |  . . . .  helper lines     |    .               .
     *     .     |  _.._.._  projection line  |   I.             .
     *       .   |____________________________|  _.'._         .       L
     *        F.                              _.'  .  '-.    .
     *          ..                       B _.'     .     '-.
     *           . .                    _.\        .     .  D
     *            .  .               _.'   :       .   .
     *     J       .   .          _.'      \       . .
     *             ..    .     _.'          :      .                M
     *            .  .     ..-'             \      .
     *           .    .    /H.               A     .
     *          .      .  /    .                   .
     *        C _________/       .                 .
     *        .          .         .               .
     *   K   .            .          .             .
     *      .              .           .           .
     *     .                .            .         .           N
     *    .                  .             .       .
     *   .                    .              .     .
     *  .                      .               .   .
     * .                        .                . .
     *                           .                 .E
     *                            .                  .
     *                             .                   .
     *                              .                    .
     * </pre>
     * 
     * Fractional projection may fail in three cases.
     * <ol>
     * <li>Numerical difficulties at slight bend, orthogonal projection returns the correct point.</li>
     * <li>Fractional projection is possible only to segments that aren't the nearest segment(s).</li>
     * <li>Fractional projection is possible for no segment.</li>
     * </ol>
     * In the latter two cases the projection is undefined and a orthogonal projection is returned if
     * {@code orthoFallback = true}, or {@code NaN} if {@code orthoFallback = false}.
     * @param start Direction; direction in first point
     * @param end Direction; direction in last point
     * @param x double; x-coordinate of point to project
     * @param y double; y-coordinate of point to project
     * @param fallback FractionalFallback; fallback method for when fractional projection fails
     * @return fractional position along this line of the fractional projection on that line of a point
     */
    public final synchronized double projectFractional(final Direction start, final Direction end, final double x,
            final double y, final FractionalFallback fallback)
    {

        // prepare
        makeLengthIndexedLine();
        double minDistance = Double.POSITIVE_INFINITY;
        double minSegmentFraction = 0;
        int minSegment = -1;
        OTSPoint3D point = new OTSPoint3D(x, y);

        // determine helpers (centers and directions)
        determineFractionalHelpers(start, end);

        // get distance of point to each segment
        double[] d = new double[this.points.length - 1];
        double minD = Double.POSITIVE_INFINITY;
        for (int i = 0; i < this.points.length - 1; i++)
        {
            d[i] = Line2D.ptSegDist(this.points[i].x, this.points[i].y, this.points[i + 1].x, this.points[i + 1].y, x, y);
            minD = d[i] < minD ? d[i] : minD;
        }

        // loop over segments for projection
        double distance;
        for (int i = 0; i < this.points.length - 1; i++)
        {
            // skip if not the closest segment, note that often two segments are equally close in their shared end point
            if (d[i] > minD + FRAC_PROJ_PRECISION)
            {
                continue;
            }
            OTSPoint3D center = this.fractionalHelperCenters[i];
            OTSPoint3D p;
            if (center != null)
            {
                // get intersection of line "center - (x, y)" and the segment
                p = OTSPoint3D.intersectionOfLines(center, point, this.points[i], this.points[i + 1]);
                if (p == null || (x < center.x + FRAC_PROJ_PRECISION && center.x + FRAC_PROJ_PRECISION < p.x)
                        || (x > center.x - FRAC_PROJ_PRECISION && center.x - FRAC_PROJ_PRECISION > p.x)
                        || (y < center.y + FRAC_PROJ_PRECISION && center.y + FRAC_PROJ_PRECISION < p.y)
                        || (y > center.y - FRAC_PROJ_PRECISION && center.y - FRAC_PROJ_PRECISION > p.y))
                {
                    // projected point may not be 'beyond' segment center (i.e. center may not be between (x, y) and (p.x, p.y)
                    continue;
                }
            }
            else
            {
                // parallel helper lines, project along direction
                OTSPoint3D offsetPoint =
                        new OTSPoint3D(x + this.fractionalHelperDirections[i].x, y + this.fractionalHelperDirections[i].y);
                p = OTSPoint3D.intersectionOfLines(point, offsetPoint, this.points[i], this.points[i + 1]);
            }
            double segLength = this.points[i].distance(this.points[i + 1]).si + FRAC_PROJ_PRECISION;
            if (p == null || this.points[i].distance(p).si > segLength || this.points[i + 1].distance(p).si > segLength)
            {
                // intersection must be on the segment
                // in case of p == null, the length of the fractional helper direction falls away due to precision
                continue;
            }
            // distance from (x, y) to intersection on segment
            double dx = x - p.x;
            double dy = y - p.y;
            distance = Math.sqrt(dx * dx + dy * dy);
            // distance from start of segment to point on segment
            if (distance < minDistance)
            {
                dx = p.x - this.points[i].x;
                dy = p.y - this.points[i].y;
                double dFrac = Math.sqrt(dx * dx + dy * dy);
                // fraction to point on segment
                minDistance = distance;
                minSegmentFraction = dFrac / (this.lengthIndexedLine[i + 1] - this.lengthIndexedLine[i]);
                minSegment = i;
            }
        }

        // return
        if (minSegment == -1)

        {
            /*
             * If fractional projection fails (x, y) is either outside of the applicable area for fractional projection, or is
             * inside an area where numerical difficulties arise (i.e. far away outside of very slight bend which is considered
             * parallel).
             */
            // CategoryLogger.info(Cat.CORE, "projectFractional failed to project " + point + " on " + this
            // + "; using fallback approach");
            return fallback.getFraction(this, x, y);
        }

        double segLen = this.lengthIndexedLine[minSegment + 1] - this.lengthIndexedLine[minSegment];
        return (this.lengthIndexedLine[minSegment] + segLen * minSegmentFraction) / getLengthSI();

    }

    /**
     * Fallback method for when fractional projection fails as the point is beyond the line or from numerical limitations.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public enum FractionalFallback
    {
        /** Orthogonal projection. */
        ORTHOGONAL
        {
            @Override
            double getFraction(final OTSLine3D line, final double x, final double y)
            {
                return line.projectOrthogonal(x, y);
            }
        },

        /** Distance to nearest end point. */
        ENDPOINT
        {
            @Override
            double getFraction(final OTSLine3D line, final double x, final double y)
            {
                OTSPoint3D point = new OTSPoint3D(x, y);
                double dStart = point.distanceSI(line.getFirst());
                double dEnd = point.distanceSI(line.getLast());
                if (dStart < dEnd)
                {
                    return -dStart / line.getLengthSI();
                }
                else
                {
                    return (dEnd + line.getLengthSI()) / line.getLengthSI();
                }
            }
        },

        /** NaN value. */
        NaN
        {
            @Override
            double getFraction(final OTSLine3D line, final double x, final double y)
            {
                return Double.NaN;
            }
        };

        /**
         * Returns fraction for when fractional projection fails as the point is beyond the line or from numerical limitations.
         * @param line OTSLine3D; line
         * @param x double; x coordinate of point
         * @param y double; y coordinate of point
         * @return double; fraction for when fractional projection fails
         */
        abstract double getFraction(OTSLine3D line, double x, double y);

    }

    /**
     * Determines all helpers (points and/or directions) for fractional projection and stores fixed information in properties
     * while returning the first and last center points (.
     * @param start Direction; direction in first point
     * @param end Direction; direction in last point
     */
    private synchronized void determineFractionalHelpers(final Direction start, final Direction end)
    {

        final int n = this.points.length - 1;

        // calculate fixed helpers if not done yet
        if (this.fractionalHelperCenters == null)
        {
            this.fractionalHelperCenters = new OTSPoint3D[n];
            this.fractionalHelperDirections = new Point2D.Double[n];
            if (this.points.length > 2)
            {
                // intersection of parallel lines of first and second segment
                OTSLine3D prevOfsSeg = unitOffsetSegment(0);
                OTSLine3D nextOfsSeg = unitOffsetSegment(1);
                OTSPoint3D parStartPoint;
                try
                {
                    parStartPoint = OTSPoint3D.intersectionOfLines(prevOfsSeg.get(0), prevOfsSeg.get(1), nextOfsSeg.get(0),
                            nextOfsSeg.get(1));
                    if (parStartPoint == null || prevOfsSeg.get(1).distanceSI(nextOfsSeg.get(0)) < Math
                            .min(prevOfsSeg.get(1).distanceSI(parStartPoint), nextOfsSeg.get(0).distanceSI(parStartPoint)))
                    {
                        parStartPoint = new OTSPoint3D((prevOfsSeg.get(1).x + nextOfsSeg.get(0).x) / 2,
                                (prevOfsSeg.get(1).y + nextOfsSeg.get(0).y) / 2);
                    }
                }
                catch (OTSGeometryException oge)
                {
                    // cannot happen as only the first and second point (which are always present) are requested
                    throw new RuntimeException(oge);
                }
                // remember the intersection of the first two unit offset segments
                this.firstOffsetIntersection = parStartPoint;
                // loop segments
                for (int i = 1; i < this.points.length - 2; i++)
                {
                    prevOfsSeg = nextOfsSeg;
                    nextOfsSeg = unitOffsetSegment(i + 1);
                    OTSPoint3D parEndPoint;
                    try
                    {
                        parEndPoint = OTSPoint3D.intersectionOfLines(prevOfsSeg.get(0), prevOfsSeg.get(1), nextOfsSeg.get(0),
                                nextOfsSeg.get(1));
                        if (parEndPoint == null || prevOfsSeg.get(1).distanceSI(nextOfsSeg.get(0)) < Math
                                .min(prevOfsSeg.get(1).distanceSI(parEndPoint), nextOfsSeg.get(0).distanceSI(parEndPoint)))
                        {
                            parEndPoint = new OTSPoint3D((prevOfsSeg.get(1).x + nextOfsSeg.get(0).x) / 2,
                                    (prevOfsSeg.get(1).y + nextOfsSeg.get(0).y) / 2);
                        }
                    }
                    catch (OTSGeometryException oge)
                    {
                        // cannot happen as only the first and second point (which are always present) are requested
                        throw new RuntimeException(oge);
                    }
                    // center = intersections of helper lines
                    this.fractionalHelperCenters[i] =
                            OTSPoint3D.intersectionOfLines(this.points[i], parStartPoint, this.points[i + 1], parEndPoint);
                    if (this.fractionalHelperCenters[i] == null)
                    {
                        // parallel helper lines, parallel segments or /\/ cause parallel helper lines, use direction
                        this.fractionalHelperDirections[i] =
                                new Point2D.Double(parStartPoint.x - this.points[i].x, parStartPoint.y - this.points[i].y);
                    }
                    parStartPoint = parEndPoint;
                }
                // remember the intersection of the last two unit offset segments
                this.lastOffsetIntersection = parStartPoint;
            }
        }

        // use directions at start and end to get unit offset points to the left at a distance of 1
        double ang = (start == null ? Math.atan2(this.points[1].y - this.points[0].y, this.points[1].x - this.points[0].x)
                : start.getInUnit(DirectionUnit.DEFAULT)) + Math.PI / 2; // start.si + Math.PI / 2;
        OTSPoint3D p1 = new OTSPoint3D(this.points[0].x + Math.cos(ang), this.points[0].y + Math.sin(ang));
        ang = (end == null ? Math.atan2(this.points[n].y - this.points[n - 1].y, this.points[n].x - this.points[n - 1].x)
                : end.getInUnit(DirectionUnit.DEFAULT)) + Math.PI / 2; // end.si + Math.PI / 2;
        OTSPoint3D p2 = new OTSPoint3D(this.points[n].x + Math.cos(ang), this.points[n].y + Math.sin(ang));

        // calculate first and last center (i.e. intersection of unit offset segments), which depend on inputs 'start' and 'end'
        if (this.points.length > 2)
        {
            this.fractionalHelperCenters[0] =
                    OTSPoint3D.intersectionOfLines(this.points[0], p1, this.points[1], this.firstOffsetIntersection);
            this.fractionalHelperCenters[n - 1] =
                    OTSPoint3D.intersectionOfLines(this.points[n - 1], this.lastOffsetIntersection, this.points[n], p2);
            if (this.fractionalHelperCenters[n - 1] == null)
            {
                // parallel helper lines, use direction for projection
                this.fractionalHelperDirections[n - 1] = new Point2D.Double(p2.x - this.points[n].x, p2.y - this.points[n].y);
            }
        }
        else
        {
            // only a single segment
            this.fractionalHelperCenters[0] = OTSPoint3D.intersectionOfLines(this.points[0], p1, this.points[1], p2);
        }
        if (this.fractionalHelperCenters[0] == null)
        {
            // parallel helper lines, use direction for projection
            this.fractionalHelperDirections[0] = new Point2D.Double(p1.x - this.points[0].x, p1.y - this.points[0].y);
        }

    }

    /**
     * Helper method for fractional projection which returns an offset line to the left of a segment at a distance of 1.
     * @param segment int; segment number
     * @return parallel line to the left of a segment at a distance of 1
     */
    private synchronized OTSLine3D unitOffsetSegment(final int segment)
    {

        // double angle = Math.atan2(this.points[segment + 1].y - this.points[segment].y,
        // this.points[segment + 1].x - this.points[segment].x) + Math.PI / 2;
        // while (angle > Math.PI)
        // {
        // angle -= Math.PI;
        // }
        // while (angle < -Math.PI)
        // {
        // angle += Math.PI;
        // }
        // OTSPoint3D from = new OTSPoint3D(this.points[segment].x + Math.cos(angle), this.points[segment].y + Math.sin(angle));
        // OTSPoint3D to =
        // new OTSPoint3D(this.points[segment + 1].x + Math.cos(angle), this.points[segment + 1].y + Math.sin(angle));
        // try
        // {
        // return new OTSLine3D(from, to);
        // }
        // catch (OTSGeometryException oge)
        // {
        // // cannot happen as points are from this OTSLine3D which performed the same checks and 2 points are given
        // throw new RuntimeException(oge);
        // }
        OTSPoint3D from = new OTSPoint3D(this.points[segment].x, this.points[segment].y);
        OTSPoint3D to = new OTSPoint3D(this.points[segment + 1].x, this.points[segment + 1].y);
        try
        {
            OTSLine3D line = new OTSLine3D(from, to);
            return line.offsetLine(1.0);
        }
        catch (OTSGeometryException oge)
        {
            // Cannot happen as points are from this OTSLine3D which performed the same checks and 2 points are given
            throw new RuntimeException(oge);
        }
    }

    /**
     * Returns the directional radius of the line at a given fraction. Negative values reflect right-hand curvature in the
     * design-line direction. The radius is taken as the minimum of the radii at the vertices before and after the given
     * fraction. The radius at a vertex is calculated as the radius of a circle that is equidistant from both edges connected to
     * the vertex. The circle center is on a line perpendicular to the shortest edge, crossing through the middle of the
     * shortest edge.
     * @param fraction double; fraction along the line, between 0.0 and 1.0 (both inclusive)
     * @return Length; radius
     * @throws OTSGeometryException fraction out of bounds
     */
    public synchronized Length getRadius(final double fraction) throws OTSGeometryException
    {
        Throw.when(fraction < 0.0 || fraction > 1.0, OTSGeometryException.class, "Fraction %f is out of bounds [0.0 ... 1.0]",
                fraction);
        if (this.vertexRadii == null)
        {
            this.vertexRadii = new Length[size() - 1];
        }
        int index = find(fraction * getLength().si);
        if (index > 0 && this.vertexRadii[index] == null)
        {
            this.vertexRadii[index] = getVertexRadius(index);
        }
        if (index < size() - 2 && this.vertexRadii[index + 1] == null)
        {
            this.vertexRadii[index + 1] = getVertexRadius(index + 1);
        }
        if (index == 0)
        {
            return this.vertexRadii[1];
        }
        if (index == size() - 2)
        {
            return this.vertexRadii[size() - 2];
        }
        return Math.abs(this.vertexRadii[index].si) < Math.abs(this.vertexRadii[index + 1].si) ? this.vertexRadii[index]
                : this.vertexRadii[index + 1];
    }

    /**
     * Calculates the directional radius at a vertex. Negative values reflect right-hand curvature in the design-line direction.
     * The radius at a vertex is calculated as the radius of a circle that is equidistant from both edges connected to the
     * vertex. The circle center is on a line perpendicular to the shortest edge, crossing through the middle of the shortest
     * edge.
     * @param index int; index of the vertex in range [1 ... size() - 2]
     * @return Length; radius at the vertex
     * @throws OTSGeometryException if the index is out of bounds
     */
    public synchronized Length getVertexRadius(final int index) throws OTSGeometryException
    {
        Throw.when(index < 1 || index > size() - 2, OTSGeometryException.class, "Index %d is out of bounds [1 ... size() - 2].",
                index);
        makeLengthIndexedLine();
        determineFractionalHelpers(null, null);
        double length1 = this.lengthIndexedLine[index] - this.lengthIndexedLine[index - 1];
        double length2 = this.lengthIndexedLine[index + 1] - this.lengthIndexedLine[index];
        int shortIndex = length1 < length2 ? index : index + 1;
        // center of shortest edge
        OTSPoint3D p1 = new OTSPoint3D(.5 * (this.points[shortIndex - 1].x + this.points[shortIndex].x),
                .5 * (this.points[shortIndex - 1].y + this.points[shortIndex].y),
                .5 * (this.points[shortIndex - 1].z + this.points[shortIndex].z));
        // perpendicular to shortest edge, line crossing p1
        OTSPoint3D p2 = new OTSPoint3D(p1.x + (this.points[shortIndex].y - this.points[shortIndex - 1].y),
                p1.y - (this.points[shortIndex].x - this.points[shortIndex - 1].x), p1.z);
        // vertex
        OTSPoint3D p3 = this.points[index];
        // point on line that splits angle between edges at vertex 50-50
        OTSPoint3D p4 = this.fractionalHelperCenters[index];
        if (p4 == null)
        {
            // parallel helper lines
            p4 = new OTSPoint3D(p3.x + this.fractionalHelperDirections[index].x,
                    p3.y + this.fractionalHelperDirections[index].y);
        }
        OTSPoint3D intersection = OTSPoint3D.intersectionOfLines(p1, p2, p3, p4);
        if (null == intersection)
        {
            return Length.instantiateSI(Double.NaN);
        }
        // determine left or right
        double refLength = length1 < length2 ? length1 : length2;
        Length radius = intersection.distance(p1);
        Length i2p2 = intersection.distance(p2);
        if (radius.si < i2p2.si && i2p2.si > refLength)
        {
            // left as p1 is closer than p2 (which was placed to the right) and not on the perpendicular line
            return radius;
        }
        // right as not left
        return radius.neg();
    }

    /**
     * Returns the length fraction at the vertex.
     * @param index int; index of vertex [0 ... size() - 1]
     * @return double; length fraction at the vertex
     * @throws OTSGeometryException if the index is out of bounds
     */
    public synchronized double getVertexFraction(final int index) throws OTSGeometryException
    {
        Throw.when(index < 0 || index > size() - 1, OTSGeometryException.class, "Index %d is out of bounds [0 %d].", index,
                size() - 1);
        makeLengthIndexedLine();
        return this.lengthIndexedLine[index] / getLengthSI();
    }

    /**
     * Calculate the centroid of this line, and the bounds, and cache for later use.
     */
    private synchronized void calcCentroidBounds()
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
        double deltaX = maxX - minX;
        double deltaY = maxY - minY;
        double deltaZ = maxZ - minZ;
        this.bounds = new BoundingBox(new Point3d(-deltaX / 2.0, -deltaY / 2.0, -deltaZ / 2.0),
                new Point3d(deltaX / 2, deltaY / 2, deltaZ / 2));
        this.envelope = new Envelope(minX, maxX, minY, maxY);
    }

    /**
     * Retrieve the centroid of this OTSLine3D.
     * @return OTSPoint3D; the centroid of this OTSLine3D
     */
    public synchronized final OTSPoint3D getCentroid()
    {
        if (this.centroid == null)
        {
            calcCentroidBounds();
        }
        return this.centroid;
    }

    /**
     * Get the bounding rectangle of this OTSLine3D.
     * @return Rectangle2D; the bounding rectangle of this OTSLine3D
     */
    public final synchronized Envelope getEnvelope()
    {
        if (this.envelope == null)
        {
            calcCentroidBounds();
        }
        return this.envelope;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public synchronized DirectedPoint getLocation()
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
    public synchronized Bounds getBounds()
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
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
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

    /**
     * Convert the 2D projection of this OTSLine3D to something that MS-Excel can plot.
     * @return excel XY plottable output
     */
    public final String toExcel()
    {
        StringBuffer s = new StringBuffer();
        for (OTSPoint3D p : this.points)
        {
            s.append(p.x + "\t" + p.y + "\n");
        }
        return s.toString();
    }

    /**
     * Convert the 2D projection of this OTSLine3D to Peter's plot format.
     * @return Peter's format plot output
     */
    public final String toPlot()
    {
        StringBuffer result = new StringBuffer();
        for (OTSPoint3D p : this.points)
        {
            result.append(String.format(Locale.US, "%s%.3f,%.3f", 0 == result.length() ? "M" : " L", p.x, p.y));
        }
        result.append("\n");
        return result.toString();
    }

    /**
     * Test/development code for the fractional helper stuff.
     * @param args String[]; the command line arguments (not used)
     * @throws OTSGeometryException in case of error
     */
    public static void main(final String[] args) throws OTSGeometryException
    {

        /*
         * OTSLine3D line = new OTSLine3D(new OTSPoint3D(-263.811, -86.551, 1.180), new OTSPoint3D(-262.945, -84.450, 1.180),
         * new OTSPoint3D(-261.966, -82.074, 1.180), new OTSPoint3D(-260.890, -79.464, 1.198), new OTSPoint3D(-259.909, -76.955,
         * 1.198), new OTSPoint3D(-258.911, -74.400, 1.198), new OTSPoint3D(-257.830, -71.633, 1.234));
         * System.out.println(line.toExcel()); double[] relativeFractions = new double[] { 0.0, 0.19827228089475762,
         * 0.30549496392494213, 0.5824753163948581, 0.6815307752261827, 0.7903990449840241, 0.8942375145295614, 1.0 }; double[]
         * offsets = new double[] { 2.9779999256134, 4.6029999256134, 3.886839156071996, 2.3664845198627207, 1.7858981925396709,
         * 1.472348149010167, 2.0416709053157285, 2.798692100483229 }; System.out.println(line.offsetLine(relativeFractions,
         * offsets).toExcel());
         */

        List<OTSPoint3D> list = new ArrayList<>();
        boolean laneOn933 = true;
        if (!laneOn933)
        {
            double x = 0;
            double y = 0;
            double dx = 0.000001;
            double dy = 0.05;
            double ddx = 1.5;
            for (int i = 0; i < 32; i++)
            {
                list.add(new OTSPoint3D(x, y));
                x += dx;
                dx *= ddx;
                y += dy;
            }
        }
        else
        {
            String lineStr = "@0   426333.939, 4581372.345@" + "1   426333.92109750526, 4581372.491581111@"
                    + "2   426333.9016207722, 4581372.6364820665@" + "3   426333.8806181711, 4581372.7797264075@"
                    + "4   426333.8581377007, 4581372.921337651@" + "5   426333.8342269785, 4581373.061339286@"
                    + "6   426333.80893323367, 4581373.199754763@" + "7   426333.78230329906, 4581373.336607476@"
                    + "8   426333.75438360614, 4581373.471920755@" + "9   426333.7252201801, 4581373.605717849@"
                    + "10  426333.69485863775, 4581373.738021923@" + "11  426333.6633441839, 4581373.868856039@"
                    + "12  426333.6307216125, 4581373.998243135@" + "13  426333.5970353065, 4581374.1262060385@"
                    + "14  426333.56232923956, 4581374.252767426@" + "15  426333.54571270826, 4581374.331102062@"
                    + "16  426333.53121128445, 4581374.399777128@" + "17  426333.51761287224, 4581374.46141805@"
                    + "18  426333.5035609495, 4581374.524905452@" + "19  426333.4885681211, 4581374.590110448@"
                    + "20  426333.4750534529, 4581374.648530791@" + "21  426333.4586325006, 4581374.71720738@"
                    + "22  426333.44573716016, 4581374.770680802@" + "23  426333.4278589452, 4581374.84273674@"
                    + "24  426333.41565935884, 4581374.891382747@" + "25  426333.39629928104, 4581374.966726161@"
                    + "26  426333.3640042249, 4581375.089202983@" + "27  426333.3310233974, 4581375.210194213@"
                    + "28  426333.2974053264, 4581375.329726505@" + "29  426333.26319745823, 4581375.44782613@"
                    + "30  426333.2284461768, 4581375.564518943@" + "31  426333.1931968143, 4581375.679830365@"
                    + "32  426333.15749366966, 4581375.793785359@" + "33  426333.12138002727, 4581375.9064084105@"
                    + "34  426333.0848981781, 4581376.017723508@" + "35  426333.0526068902, 4581376.127395174@"
                    + "36  426333.0222216131, 4581376.235573194@" + "37  426333.00835773064, 4581376.284013769@"
                    + "38  426332.9916265083, 4581376.342442355@" + "39  426332.9771780217, 4581376.392075247@"
                    + "40  426332.96085931134, 4581376.448026933@" + "41  426332.9448449097, 4581376.5021694945@"
                    + "42  426332.9299564511, 4581376.552350422@" + "43  426332.9123899684, 4581376.610862428@"
                    + "44  426332.87985284685, 4581376.718179138@" + "45  426332.8472718188, 4581376.824143872@"
                    + "46  426332.81468381727, 4581376.92878003@" + "47  426332.78212446393, 4581377.032110168@"
                    + "48  426332.7496281178, 4581377.134155947@" + "49  426332.71722788643, 4581377.234938197@"
                    + "50  426332.68495568086, 4581377.3344768565@" + "51  426332.6528422234, 4581377.432791035@"
                    + "52  426332.6209170973, 4581377.529898969@" + "53  426332.59026768577, 4581377.622609458@"
                    + "54  426332.5618311538, 4581377.708242513@" + "55  426332.5292456913, 4581377.813700842@"
                    + "56  426332.5007497582, 4581377.905735847@" + "57  426332.4725916431, 4581377.996633883@"
                    + "58  426332.4447947076, 4581378.086409748@" + "59  426332.41739884845, 4581378.175020202@"
                    + "60  426332.3904224847, 4581378.262486783@" + "61  426332.37513187295, 4581378.312218361@"
                    + "62  426332.3474726438, 4581378.402429141@" + "63  426332.3203478011, 4581378.491354613@"
                    + "64  426332.2937555201, 4581378.579078223@" + "65  426332.26771504263, 4581378.665610338@"
                    + "66  426332.24224462465, 4581378.750960108@" + "67  426332.21736132156, 4581378.835136287@"
                    + "68  426332.1930813682, 4581378.918146061@" + "69  426332.1694196611, 4581378.999996922@"
                    + "70  426332.1468078785, 4581379.079234334@" + "71  426332.1253935003, 4581379.155326921@"
                    + "72  426332.10456227185, 4581379.230438552@" + "73  426332.08413377195, 4581379.301777359@"
                    + "74  426332.0575671712, 4581379.393246921@" + "75  426332.037751917, 4581379.463051603@"
                    + "76  426332.01541074895, 4581379.543672992@" + "77  426331.9954696024, 4581379.617241848@"
                    + "78  426331.9764488572, 4581379.689794578@" + "79  426331.9581173997, 4581379.761214821@"
                    + "80  426331.9407607595, 4581379.831643043@" + "81  426331.92459788476, 4581379.898797621@"
                    + "82  426331.89349001576, 4581380.036207511@" + "83  426331.8662295119, 4581380.167554456@"
                    + "84  426331.84239882755, 4581380.294825263@" + "85  426331.8220095046, 4581380.41813201@"
                    + "86  426331.80506772455, 4581380.537631294@" + "87  426331.79158302536, 4581380.653536015@"
                    + "88  426331.78158027114, 4581380.766126917@" + "89  426331.7754554946, 4581380.838605414@"
                    + "90  426331.76793314604, 4581380.909291444@" + "91  426331.7605002508, 4581381.016285149@"
                    + "92  426331.75725734304, 4581381.119549306@" + "93  426331.75814653496, 4581381.219559045@"
                    + "94  426331.76316353114, 4581381.316908372@" + "95  426331.7723867522, 4581381.412305131@"
                    + "96  426331.7860053539, 4581381.506554079@" + "97  426331.80434182915, 4581381.600527881@"
                    + "98  426331.82733581704, 4581381.692992337@" + "99  426331.8531803791, 4581381.777938947@"
                    + "100 426331.884024255, 4581381.864352291@" + "101 426331.92063241004, 4581381.953224321@"
                    + "102 426331.96390912175, 4581382.045434713@" + "103 426331.9901409878, 4581382.095566823@"
                    + "104 426332.0148562894, 4581382.141714169@" + "105 426332.05172826024, 4581382.204388889@"
                    + "106 426332.12722889386, 4581382.323121141@" + "107 426332.1628785428, 4581382.375872464@"
                    + "108 426332.22007742553, 4581382.462661629@" + "109 426332.26023980865, 4581382.523784153@"
                    + "110 426332.3033344728, 4581382.586422447@" + "111 426332.34946240357, 4581382.650580184@"
                    + "112 426332.3987196004, 4581382.716255575@" + "113 426332.4511967281, 4581382.783441929@"
                    + "114 426332.50697922776, 4581382.852128648@" + "115 426332.56614731904, 4581382.922301916@"
                    + "116 426332.628776037, 4581382.993945288@" + "117 426332.6949354622, 4581383.067040358@"
                    + "118 426332.76469110255, 4581383.141567508@" + "119 426332.8381037568, 4581383.217505949@"
                    + "120 426332.91523022414, 4581383.294834619@" + "121 426332.9961233405, 4581383.373532268@"
                    + "122 426333.0808322224, 4581383.453577724@" + "123 426333.1693585424, 4581383.534909724@"
                    + "124 426333.26164044754, 4581383.61741792@" + "125 426333.3650128907, 4581383.707446191@";
            int fromIndex = 0;
            while (true)
            {
                int at1 = lineStr.indexOf('@', fromIndex);
                fromIndex = at1 + 1;
                int at2 = lineStr.indexOf('@', fromIndex);
                if (at2 < 0)
                {
                    break;
                }
                fromIndex = at2;

                String subStr = lineStr.substring(at1 + 5, at2);
                int comma = subStr.indexOf(',');
                double x = Double.valueOf(subStr.substring(0, comma));
                double y = Double.valueOf(subStr.substring(comma + 1));

                list.add(new OTSPoint3D(x, y, 0.0));

            }
        }
        OTSLine3D line = new OTSLine3D(list);

        line.projectFractional(null, null, 1.0, 0.5, FractionalFallback.NaN); // creates fractional helper points

        // create line of fractional helper points, give NaN points for null values
        OTSPoint3D[] array = line.fractionalHelperCenters;
        for (int i = 0; i < array.length; i++)
        {
            if (array[i] == null)
            {
                array[i] = new OTSPoint3D(Double.NaN, Double.NaN);
            }
        }
        OTSLine3D helpers = new OTSLine3D(line.fractionalHelperCenters);

        // create Matlab compatible strings of lines
        StringBuilder str = new StringBuilder();
        str.append("line = [");
        String sep = "";
        for (OTSPoint3D p : line.getPoints())
        {
            str.append(String.format(Locale.US, "%s %.8f, %.8f", sep, p.x, p.y));
            sep = ",";
        }
        str.append("];\n");

        str.append("helpers = [");
        sep = "";
        for (OTSPoint3D p : helpers.getPoints())
        {
            str.append(String.format(Locale.US, "%s %.8f, %.8f", sep, p.x, p.y));
            sep = ",";
        }
        str.append("];\n");

        System.out.print(str);
    }

}

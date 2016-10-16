package org.opentrafficsim.core.geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.j3d.Bounds;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Line with OTSPoint3D points, a cached length indexed line, a cahced length, and a cached centroid (all calculated on first
 * use).
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** The cached length; will be calculated when needed for the first time. */
    private double length = Double.NaN;

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
    private static final double FRAC_PROJ_PRECISION = 1e-6;

    /** Bounding of this OTSLine3D. */
    private Envelope envelope;

    /**
     * Construct a new OTSLine3D.
     * @param points the array of points to construct this OTSLine3D from.
     * @throws OTSGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSLine3D(final OTSPoint3D... points) throws OTSGeometryException
    {
        init(points);
    }

    /**
     * Construct a new OTSLine3D.
     * @param pts the array of points to construct this OTSLine3D from.
     * @throws OTSGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    private void init(final OTSPoint3D... pts) throws OTSGeometryException
    {
        if (pts.length < 2)
        {
            throw new OTSGeometryException("Degenerate OTSLine3D; has " + pts.length + " point"
                + (pts.length != 1 ? "s" : ""));
        }
        for (int i = 1; i < pts.length; i++)
        {
            if (pts[i - 1].x == pts[i].x && pts[i - 1].y == pts[i].y && pts[i - 1].z == pts[i].z)
            {
                throw new OTSGeometryException("Degenerate OTSLine3D; point " + (i - 1)
                    + " has the same x, y and z as point " + i);
            }
        }
        this.points = pts;
    }

    /** Which offsetLine method to use... */
    public enum OffsetMethod
    {
        /** Via JTS buffer. */
        JTS,

        /** Peter Knoppers. */
        PK,

        /** Alexander Verbraeck. */
        AV;
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

                case AV:
                    return OTSBufferingAV.offsetLine(this, offset);

                case JTS:
                    return OTSBufferingJTS.offsetGeometryOLD(this, offset);

                default:
                    return null;
            }
        }
        catch (OTSGeometryException exception)
        {
            exception.printStackTrace();
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
                    list = new ArrayList<OTSPoint3D>();
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
        try
        {
            return new OTSLine3D(list);
        }
        catch (OTSGeometryException exception)
        {
            System.err.println("CANNOT HAPPEN");
            exception.printStackTrace();
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
            OTSLine3D first =
                    new OTSLine3D(Arrays.copyOfRange(this.points, 0, splitIndex + 1)).noiseFilterRamerDouglasPeuker(epsilon,
                            useHorizontalDistance);
            OTSLine3D second =
                    new OTSLine3D(Arrays.copyOfRange(this.points, splitIndex, this.points.length))
                            .noiseFilterRamerDouglasPeuker(epsilon, useHorizontalDistance);
            return concatenate(epsilon, first, second);
        }
        catch (OTSGeometryException exception)
        {
            exception.printStackTrace(); // Peter thinks this cannot happen ...
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
                new Coordinate((1 - ratio) * firstCoordinate.x + ratio * secondCoordinate.x, (1 - ratio) * firstCoordinate.y
                    + ratio * secondCoordinate.y);
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
            // System.out.println(offsetLine[i].toExcel());
            // System.out.println();
        }

        ArrayList<Coordinate> out = new ArrayList<Coordinate>();
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
     * @param lines OTSLine3D... one or more OTSLine3D. The last point of the first <strong>must</strong> match the first of the
     *            second, etc.
     * @return OTSLine3D
     * @throws OTSGeometryException if zero lines are given, or when there is a gap between consecutive lines
     */
    public static OTSLine3D concatenate(final OTSLine3D... lines) throws OTSGeometryException
    {
        return concatenate(0.0, lines);
    }

    /**
     * Concatenate several OTSLine3D instances.
     * @param toleranceSI the tolerance between the end point of a line and the first point of the next line
     * @param lines OTSLine3D... one or more OTSLine3D. The last point of the first <strong>must</strong> match the first of the
     *            second, etc.
     * @return OTSLine3D
     * @throws OTSGeometryException if zero lines are given, or when there is a gap between consecutive lines
     */
    public static OTSLine3D concatenate(final double toleranceSI, final OTSLine3D... lines) throws OTSGeometryException
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
            if (lines[i - 1].getLast().distance(lines[i].getFirst()).si > toleranceSI)
            {
                throw new OTSGeometryException("Lines are not connected: " + lines[i - 1].getLast() + " to "
                    + lines[i].getFirst() + " distance is " + lines[i - 1].getLast().distance(lines[i].getFirst()).si
                    + " > " + toleranceSI);
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
            throw new OTSGeometryException("Bad interval");
        }
        getLength(); // computes and sets the length field
        return extract(start * this.length, end * this.length);
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
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public final OTSLine3D extract(final double start, final double end) throws OTSGeometryException
    {
        if (Double.isNaN(start) || Double.isNaN(end) || start < 0 || start >= end || end > getLengthSI())
        {
            throw new OTSGeometryException("Bad interval (" + start + ".." + end + "; length of this OTSLine3D is "
                + this.getLengthSI() + ")");
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
            pointList.add(OTSPoint3D.interpolate((end - cumulativeLength) / segmentLength, this.points[index - 1],
                this.points[index]));
        }
        try
        {
            return new OTSLine3D(pointList);
        }
        catch (OTSGeometryException exception)
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
     * @throws OTSGeometryException when number of points &lt; 2
     */
    public static OTSLine3D createAndCleanOTSLine3D(final OTSPoint3D[] points) throws OTSGeometryException
    {
        if (points.length < 2)
        {
            throw new OTSGeometryException("Degenerate OTSLine3D; has " + points.length + " point"
                + (points.length != 1 ? "s" : ""));
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
     * @param coordinates the array of coordinates to construct this OTSLine3D from
     * @throws OTSGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSLine3D(final Coordinate[] coordinates) throws OTSGeometryException
    {
        this(coordinatesToOTSPoint3D(coordinates));
    }

    /**
     * Construct a new OTSLine3D from a LineString.
     * @param lineString the lineString to construct this OTSLine3D from.
     * @throws OTSGeometryException when the provided LineString does not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSLine3D(final LineString lineString) throws OTSGeometryException
    {
        this(lineString.getCoordinates());
    }

    /**
     * Construct a new OTSLine3D from a Geometry.
     * @param geometry the geometry to construct this OTSLine3D from
     * @throws OTSGeometryException when the provided Geometry do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSLine3D(final Geometry geometry) throws OTSGeometryException
    {
        this(geometry.getCoordinates());
    }

    /**
     * Construct a new OTSLine3D from a List&lt;OTSPoint3D&gt;.
     * @param pointList the list of points to construct this OTSLine3D from.
     * @throws OTSGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSLine3D(final List<OTSPoint3D> pointList) throws OTSGeometryException
    {
        this(pointList.toArray(new OTSPoint3D[pointList.size()]));
    }

    /**
     * Construct a new OTSShape (closed shape) from a Path2D.
     * @param path the Path2D to construct this OTSLine3D from.
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
     * Return the length of this OTSLine3D in meters. (Assuming that the coordinates of the points constituting this line are
     * expressed in meters.)
     * @return the length of the line
     */
    public final Length getLength()
    {
        return new Length(getLengthSI(), LengthUnit.SI);
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
     */
    public final DirectedPoint getLocationExtended(final Length position)
    {
        return getLocationExtendedSI(position.getSI());
    }

    /**
     * Get the location at a position on the line, with its direction. Position can be below 0 or more than the line length. In
     * that case, the position will be extrapolated in the direction of the line at its start or end.
     * @param positionSI the position on the line for which to calculate the point on, before, of after the line, in SI units
     * @return a directed point
     */
    public final DirectedPoint getLocationExtendedSI(final double positionSI)
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
     * @param fraction the fraction for which to calculate the point on the line
     * @param tolerance the delta from 0.0 and 1.0 that will be forgiven
     * @return a directed point
     * @throws OTSGeometryException when fraction less than 0.0 or more than 1.0.
     */
    public final DirectedPoint getLocationFraction(final double fraction, final double tolerance)
        throws OTSGeometryException
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
     * @param fraction the fraction for which to calculate the point on the line
     * @return a directed point
     */
    public final DirectedPoint getLocationFractionExtended(final double fraction)
    {
        return getLocationExtendedSI(fraction * getLengthSI());
    }

    /**
     * Get the location at a position on the line, with its direction. Position should be between 0.0 and line length.
     * @param position the position on the line for which to calculate the point on the line
     * @return a directed point
     * @throws OTSGeometryException when position less than 0.0 or more than line length.
     */
    public final DirectedPoint getLocation(final Length position) throws OTSGeometryException
    {
        return getLocationSI(position.getSI());
    }

    /**
     * Binary search for a position on the line.
     * @param pos the position to look for.
     * @return the index below the position; the position is between points[index] and points[index+1]
     * @throws OTSGeometryException when index could not be found
     */
    private int find(final double pos) throws OTSGeometryException
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
        throw new OTSGeometryException("Could not find position " + pos + " on line with length indexes: "
            + this.lengthIndexedLine);
         */
    }

    /**
     * Get the location at a position on the line, with its direction. Position should be between 0.0 and line length.
     * @param positionSI the position on the line for which to calculate the point on the line
     * @return a directed point
     * @throws OTSGeometryException when position less than 0.0 or more than line length.
     */
    public final DirectedPoint getLocationSI(final double positionSI) throws OTSGeometryException
    {
        makeLengthIndexedLine();
        if (positionSI < 0.0 || positionSI > getLengthSI())
        {
            throw new OTSGeometryException("getLocationSI for line: position < 0.0 or > line length. Position = "
                + positionSI + " m. Length = " + getLengthSI() + " m.");
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
     * @throws OTSGeometryException when position less than 0.0 or more than line length.
     */
    public final OTSLine3D truncate(final double lengthSI) throws OTSGeometryException
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
        OTSPoint3D p2 = this.points[index + 1];
        OTSPoint3D newLastPoint =
            new OTSPoint3D(p1.x + fraction * (p2.x - p1.x), p1.y + fraction * (p2.y - p1.y), p1.z + fraction * (p2.z - p1.z));
        OTSPoint3D[] coords = new OTSPoint3D[index + 2];
        for (int i = 0; i <= index; i++)
        {
            coords[i] = this.points[i];
        }
        coords[index + 1] = newLastPoint;
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
     * @param x x-coordinate of point to project
     * @param y y-coordinate of point to project
     * @return fractional position along this line of the orthogonal projection on this line of a point
     */
    public final double projectOrthogonal(final double x, final double y)
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
     * In the latter two cases the projection is undefined and a orthogonal projection is returned.
     * @param start direction in first point
     * @param end direction in last point
     * @param x x-coordinate of point to project
     * @param y y-coordinate of point to project
     * @return fractional position along this line of the fractional projection on that line of a point
     */
    public final double projectFractional(final Direction start, final Direction end, final double x, final double y)
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
            if (p == null || p.x < Math.min(this.points[i].x, this.points[i + 1].x) - FRAC_PROJ_PRECISION
                || p.x > Math.max(this.points[i].x, this.points[i + 1].x) + FRAC_PROJ_PRECISION
                || p.y < Math.min(this.points[i].y, this.points[i + 1].y) - FRAC_PROJ_PRECISION
                || p.y > Math.max(this.points[i].y, this.points[i + 1].y) + FRAC_PROJ_PRECISION)
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
            return projectOrthogonal(x, y);
        }
        double segLen = this.lengthIndexedLine[minSegment + 1] - this.lengthIndexedLine[minSegment];
        return (this.lengthIndexedLine[minSegment] + segLen * minSegmentFraction) / getLengthSI();

    }

    /**
     * Determines all helpers (points and/or directions) for fractional projection and stores fixed information in properties
     * while returning the first and last center points (.
     * @param start direction in first point
     * @param end direction in last point
     */
    private void determineFractionalHelpers(final Direction start, final Direction end)
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
                    parStartPoint =
                        OTSPoint3D.intersectionOfLines(prevOfsSeg.get(0), prevOfsSeg.get(1), nextOfsSeg.get(0), nextOfsSeg
                            .get(1));
                    if (parStartPoint == null)
                    {
                        parStartPoint =
                            new OTSPoint3D((prevOfsSeg.get(1).x + nextOfsSeg.get(0).x) / 2,
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
                        parEndPoint =
                            OTSPoint3D.intersectionOfLines(prevOfsSeg.get(0), prevOfsSeg.get(1), nextOfsSeg.get(0),
                                nextOfsSeg.get(1));
                        if (parEndPoint == null)
                        {
                            parEndPoint =
                                new OTSPoint3D((prevOfsSeg.get(1).x + nextOfsSeg.get(0).x) / 2,
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
        double ang = start.si + Math.PI / 2;
        OTSPoint3D p1 = new OTSPoint3D(this.points[0].x + Math.cos(ang), this.points[0].y + Math.sin(ang));
        ang = end.si + Math.PI / 2;
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
                this.fractionalHelperDirections[n - 1] =
                    new Point2D.Double(p2.x - this.points[n].x, p2.y - this.points[n].y);
            }
        }
        else
        {
            // only a single segment
            this.fractionalHelperCenters[0] = OTSPoint3D.intersectionOfLines(this.points[0], p1, this.points[1], p2);
            this.fractionalHelperCenters[n - 1] = null;
        }
        if (this.fractionalHelperCenters[0] == null)
        {
            // parallel helper lines, use direction for projection
            this.fractionalHelperDirections[0] = new Point2D.Double(p1.x - this.points[0].x, p1.y - this.points[0].y);
        }

    }

    /**
     * Helper method for fractional projection which returns an offset line to the left of a segment at a distance of 1.
     * @param segment segment number
     * @return parallel line to the left of a segment at a distance of 1
     */
    private OTSLine3D unitOffsetSegment(final int segment)
    {
        OTSPoint3D from = new OTSPoint3D(this.points[segment].x, this.points[segment].y);
        OTSPoint3D to = new OTSPoint3D(this.points[segment + 1].x, this.points[segment + 1].y);
        try
        {
            OTSLine3D line = new OTSLine3D(from, to);
            return line.offsetLine(1.0);
        }
        catch (OTSGeometryException oge)
        {
            // cannot happen as points are from this OTSLine3D which performed the same checks and 2 points are given
            throw new RuntimeException(oge);
        }
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
        this.envelope = new Envelope(minX, maxX, minY, maxY);
    }

    /**
     * Retrieve the centroid of this OTSLine3D.
     * @return OTSPoint3D; the centroid of this OTSLine3D
     */
    public final OTSPoint3D getCentroid()
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
    public final Envelope getEnvelope()
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
     * @param args String[]; the command line arguments (not used)
     * @throws OTSGeometryException in case of error
     */
    public static void main(final String[] args) throws OTSGeometryException
    {
        OTSLine3D line =
            new OTSLine3D(new OTSPoint3D(-263.811, -86.551, 1.180), new OTSPoint3D(-262.945, -84.450, 1.180),
                new OTSPoint3D(-261.966, -82.074, 1.180), new OTSPoint3D(-260.890, -79.464, 1.198), new OTSPoint3D(-259.909,
                    -76.955, 1.198), new OTSPoint3D(-258.911, -74.400, 1.198), new OTSPoint3D(-257.830, -71.633, 1.234));
        System.out.println(line.toExcel());
        double[] relativeFractions =
            new double[] {0.0, 0.19827228089475762, 0.30549496392494213, 0.5824753163948581, 0.6815307752261827,
                0.7903990449840241, 0.8942375145295614, 1.0};
        double[] offsets =
            new double[] {2.9779999256134, 4.6029999256134, 3.886839156071996, 2.3664845198627207, 1.7858981925396709,
                1.472348149010167, 2.0416709053157285, 2.798692100483229};
        System.out.println(line.offsetLine(relativeFractions, offsets).toExcel());
    }
}

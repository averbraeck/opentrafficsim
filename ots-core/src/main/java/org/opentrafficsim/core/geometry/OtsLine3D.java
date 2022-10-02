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
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.djunits.unit.DirectionUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.Point3d;
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

/**
 * Line with OTSPoint3D points, a cached length indexed line, a cached length, and a cached centroid (all calculated on first
 * use).
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class OtsLine3D implements Locatable, Serializable // XXX: DJ
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** The points of the line. */
    private OtsPoint3D[] points;

    /** The cumulative length of the line at point 'i'. */
    private double[] lengthIndexedLine = null;

    /** The cached length; will be calculated at time of construction. */
    private Length length; // XXX: DJ uses double

    /** The cached centroid; will be calculated when needed for the first time. */
    private OtsPoint3D centroid = null;

    /** The cached bounds; will be calculated when needed for the first time. */
    private Bounds bounds = null; // XXX: DJ

    /** The cached helper points for fractional projection; will be calculated when needed for the first time. */
    private OtsPoint3D[] fractionalHelperCenters = null;

    /** The cached helper directions for fractional projection; will be calculated when needed for the first time. */
    private Point2D.Double[] fractionalHelperDirections = null;

    /** Intersection of unit offset lines of first two segments. */
    private OtsPoint3D firstOffsetIntersection;

    /** Intersection of unit offset lines of last two segments. */
    private OtsPoint3D lastOffsetIntersection;

    /** Precision for fractional projection algorithm. */
    private static final double FRAC_PROJ_PRECISION = 2e-5 /* PK too fine 1e-6 */;

    /** Radius at each vertex. */
    private Length[] vertexRadii; // XXX: DJ

    /** Bounding of this OTSLine3D. */
    private Envelope envelope;

    /**
     * Construct a new OTSLine3D.
     * @param points OTSPoint3D...; the array of points to construct this OTSLine3D from.
     * @throws OtsGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OtsLine3D(final OtsPoint3D... points) throws OtsGeometryException
    {
        init(points);
    }

    /**
     * Construct a new OTSLine3D, and immediately make the length-indexed line.
     * @param pts OTSPoint3D...; the array of points to construct this OTSLine3D from.
     * @throws OtsGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    private void init(final OtsPoint3D... pts) throws OtsGeometryException
    {
        if (pts.length < 2)
        {
            throw new OtsGeometryException("Degenerate OTSLine3D; has " + pts.length + " point" + (pts.length != 1 ? "s" : ""));
        }
        this.lengthIndexedLine = new double[pts.length];
        this.lengthIndexedLine[0] = 0.0;
        for (int i = 1; i < pts.length; i++)
        {
            if (pts[i - 1].x == pts[i].x && pts[i - 1].y == pts[i].y && pts[i - 1].z == pts[i].z)
            {
                throw new OtsGeometryException(
                        "Degenerate OTSLine3D; point " + (i - 1) + " has the same x, y and z as point " + i);
            }
            this.lengthIndexedLine[i] = this.lengthIndexedLine[i - 1] + pts[i - 1].distanceSI(pts[i]);
        }
        this.points = pts;
        this.length = Length.instantiateSI(this.lengthIndexedLine[this.lengthIndexedLine.length - 1]); // XXX: DJ double
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
    public final OtsLine3D offsetLine(final double offset)
    {
        try
        {
            switch (OFFSETMETHOD)
            {
                case PK:
                    return OtsOffsetLinePK.offsetLine(this, offset);

                case JTS:
                    return OtsBufferingJts.offsetLine(this, offset);

                default:
                    return null;
            }
        }
        catch (OtsGeometryException exception)
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
    public final OtsLine3D noiseFilteredLine(final double noiseLevel)
    {
        if (this.size() <= 2)
        {
            return this; // Except for some cached fields; an OTSLine3D is immutable; so safe to return
        }
        OtsPoint3D prevPoint = null;
        List<OtsPoint3D> list = null;
        for (int index = 0; index < this.size(); index++)
        {
            OtsPoint3D currentPoint = this.points[index];
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
            return new OtsLine3D(list);
        }
        catch (OtsGeometryException exception)
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
    public final OtsLine3D noiseFilterRamerDouglasPeucker(final double epsilon, final boolean useHorizontalDistance) // XXX: DJ
    {
        try
        {
            // Apply the Ramer-Douglas-Peucker algorithm to the buffered points.
            // Adapted from https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
            double maxDeviation = 0;
            int splitIndex = -1;
            int pointCount = size();
            OtsLine3D straight = new OtsLine3D(get(0), get(pointCount - 1));
            // Find the point with largest deviation from the straight line from start point to end point
            for (int i = 1; i < pointCount - 1; i++)
            {
                OtsPoint3D point = get(i);
                OtsPoint3D closest =
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
            OtsLine3D first = new OtsLine3D(Arrays.copyOfRange(this.points, 0, splitIndex + 1))
                    .noiseFilterRamerDouglasPeucker(epsilon, useHorizontalDistance);
            OtsLine3D second = new OtsLine3D(Arrays.copyOfRange(this.points, splitIndex, this.points.length))
                    .noiseFilterRamerDouglasPeucker(epsilon, useHorizontalDistance);
            return concatenate(epsilon, first, second);
        }
        catch (OtsGeometryException exception)
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
     * @throws OtsGeometryException when this method fails to create the offset line
     */
    public final OtsLine3D offsetLine(final double offsetAtStart, final double offsetAtEnd) throws OtsGeometryException // XXX:
                                                                                                                        // DJ
    {
        // CategoryLogger.trace(Cat.CORE, OTSGeometry.printCoordinates("#referenceLine: \nc1,0,0\n# offset at start is "
        // + offsetAtStart + " at end is " + offsetAtEnd + "\n#", referenceLine, "\n "));

        OtsLine3D offsetLineAtStart = offsetLine(offsetAtStart);
        if (offsetAtStart == offsetAtEnd)
        {
            return offsetLineAtStart; // offset does not change
        }
        // CategoryLogger.trace(Cat.CORE, OTSGeometry.printCoordinates("#offset line at start: \nc0,0,0\n#",
        // offsetLineAtStart, "\n "));
        OtsLine3D offsetLineAtEnd = offsetLine(offsetAtEnd);
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
        return new OtsLine3D(resultCoordinates);
    }

    /**
     * Create a line at linearly varying offset from this line. The offset may change linearly from its initial value at the
     * start of the reference line via a number of intermediate offsets at intermediate positions to its final offset value at
     * the end of the reference line.
     * @param relativeFractions double[]; positional fractions for which the offsets have to be generated
     * @param offsets double[]; offsets at the relative positions (positive value is Left, negative value is Right)
     * @return Geometry; the Geometry of the line at linearly changing offset of the reference line
     * @throws OtsGeometryException when this method fails to create the offset line
     */
    public final OtsLine3D offsetLine(final double[] relativeFractions, final double[] offsets) throws OtsGeometryException // XXX:
                                                                                                                            // DJ
    {
        Throw.whenNull(relativeFractions, "relativeFraction may not be null");
        Throw.whenNull(offsets, "offsets may not be null");
        Throw.when(relativeFractions.length < 2, OtsGeometryException.class, "size of relativeFractions must be >= 2");
        Throw.when(relativeFractions.length != offsets.length, OtsGeometryException.class,
                "size of relativeFractions must be equal to size of offsets");
        Throw.when(relativeFractions[0] < 0, OtsGeometryException.class, "relativeFractions may not start before 0");
        Throw.when(relativeFractions[relativeFractions.length - 1] > 1, OtsGeometryException.class,
                "relativeFractions may not end beyond 1");
        List<Double> fractionsList = DoubleStream.of(relativeFractions).boxed().collect(Collectors.toList());
        List<Double> offsetsList = DoubleStream.of(offsets).boxed().collect(Collectors.toList());
        if (relativeFractions[0] != 0)
        {
            fractionsList.add(0, 0.0);
            offsetsList.add(0, 0.0);
        }
        if (relativeFractions[relativeFractions.length - 1] < 1.0)
        {
            fractionsList.add(1.0);
            offsetsList.add(0.0);
        }
        OtsLine3D[] offsetLine = new OtsLine3D[fractionsList.size()];
        for (int i = 0; i < fractionsList.size(); i++)
        {
            offsetLine[i] = offsetLine(offsetsList.get(i));
            System.out.println("# offset is " + offsetsList.get(i));
            System.out.println(offsetLine[i].toPlot());
        }
        List<Coordinate> out = new ArrayList<>();
        Coordinate prevCoordinate = null;
        final double tooClose = 0.05; // 5 cm
        // TODO make tooClose a parameter of this method.
        for (int i = 0; i < offsetsList.size() - 1; i++)
        {
            Throw.when(fractionsList.get(i + 1) <= fractionsList.get(i), OtsGeometryException.class,
                    "fractions must be in ascending order");
            Geometry startGeometry =
                    offsetLine[i].extractFractional(fractionsList.get(i), fractionsList.get(i + 1)).getLineString();
            Geometry endGeometry =
                    offsetLine[i + 1].extractFractional(fractionsList.get(i), fractionsList.get(i + 1)).getLineString();
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
        return new OtsLine3D(resultCoordinates);
    }

    /**
     * Concatenate several OTSLine3D instances.
     * @param lines OTSLine3D...; OTSLine3D... one or more OTSLine3D. The last point of the first
     *            &lt;strong&gt;must&lt;/strong&gt; match the first of the second, etc.
     * @return OTSLine3D
     * @throws OtsGeometryException if zero lines are given, or when there is a gap between consecutive lines
     */
    public static OtsLine3D concatenate(final OtsLine3D... lines) throws OtsGeometryException
    {
        return concatenate(0.0, lines);
    }

    /**
     * Concatenate two OTSLine3D instances. This method is separate for efficiency reasons.
     * @param toleranceSI double; the tolerance between the end point of a line and the first point of the next line
     * @param line1 OTSLine3D; first line
     * @param line2 OTSLine3D; second line
     * @return OTSLine3D
     * @throws OtsGeometryException if zero lines are given, or when there is a gap between consecutive lines
     */
    public static OtsLine3D concatenate(final double toleranceSI, final OtsLine3D line1, final OtsLine3D line2)
            throws OtsGeometryException
    {
        if (line1.getLast().distance(line2.getFirst()).si > toleranceSI)
        {
            throw new OtsGeometryException("Lines are not connected: " + line1.getLast() + " to " + line2.getFirst()
                    + " distance is " + line1.getLast().distance(line2.getFirst()).si + " > " + toleranceSI);
        }
        int size = line1.size() + line2.size() - 1;
        OtsPoint3D[] points = new OtsPoint3D[size];
        int nextIndex = 0;
        for (int j = 0; j < line1.size(); j++)
        {
            points[nextIndex++] = line1.get(j);
        }
        for (int j = 1; j < line2.size(); j++)
        {
            points[nextIndex++] = line2.get(j);
        }
        return new OtsLine3D(points);
    }

    /**
     * Concatenate several OTSLine3D instances.
     * @param toleranceSI double; the tolerance between the end point of a line and the first point of the next line
     * @param lines OTSLine3D...; OTSLine3D... one or more OTSLine3D. The last point of the first
     *            &lt;strong&gt;must&lt;/strong&gt; match the first of the second, etc.
     * @return OTSLine3D
     * @throws OtsGeometryException if zero lines are given, or when there is a gap between consecutive lines
     */
    public static OtsLine3D concatenate(final double toleranceSI, final OtsLine3D... lines) throws OtsGeometryException
    {
        // CategoryLogger.trace(Cat.CORE, "Concatenating " + lines.length + " lines.");
        if (0 == lines.length)
        {
            throw new OtsGeometryException("Empty argument list");
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
                throw new OtsGeometryException(
                        "Lines are not connected: " + lines[i - 1].getLast() + " to " + lines[i].getFirst() + " distance is "
                                + lines[i - 1].getLast().distance(lines[i].getFirst()).si + " > " + toleranceSI);
            }
            size += lines[i].size() - 1;
        }
        OtsPoint3D[] points = new OtsPoint3D[size];
        int nextIndex = 0;
        for (int i = 0; i < lines.length; i++)
        {
            OtsLine3D line = lines[i];
            for (int j = 0 == i ? 0 : 1; j < line.size(); j++)
            {
                points[nextIndex++] = line.get(j);
            }
        }
        return new OtsLine3D(points);
    }

    /**
     * Construct a new OTSLine3D with all points of this OTSLine3D in reverse order.
     * @return OTSLine3D; the new OTSLine3D
     */
    public final OtsLine3D reverse()
    {
        OtsPoint3D[] resultPoints = new OtsPoint3D[size()];
        int nextIndex = size();
        for (OtsPoint3D p : getPoints())
        {
            resultPoints[--nextIndex] = p;
        }
        try
        {
            return new OtsLine3D(resultPoints);
        }
        catch (OtsGeometryException exception)
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
     * @throws OtsGeometryException when start &gt;= end, or start &lt; 0, or end &gt; 1
     */
    public final OtsLine3D extractFractional(final double start, final double end) throws OtsGeometryException
    {
        if (start < 0 || start >= end || end > 1)
        {
            throw new OtsGeometryException(
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
     * @throws OtsGeometryException when start &gt;= end, or start &lt; 0, or end &gt; length
     */
    public final OtsLine3D extract(final Length start, final Length end) throws OtsGeometryException // XXX: DJ
    {
        return extract(start.si, end.si);
    }

    /**
     * Create a new OTSLine3D that covers a sub-section of this OTSLine3D.
     * @param start double; length along this OTSLine3D where the sub-section starts, valid range [0..<cite>end</cite>)
     * @param end double; length along this OTSLine3D where the sub-section ends, valid range
     *            (<cite>start</cite>..<cite>length</cite> (length is the length of this OTSLine3D)
     * @return OTSLine3D; the selected sub-section
     * @throws OtsGeometryException when start &gt;= end, or start &lt; 0, or end &gt; length
     */
    public final OtsLine3D extract(final double start, final double end) throws OtsGeometryException
    {
        if (Double.isNaN(start) || Double.isNaN(end) || start < 0 || start >= end || end > getLengthSI())
        {
            throw new OtsGeometryException(
                    "Bad interval (" + start + ".." + end + "; length of this OTSLine3D is " + this.getLengthSI() + ")");
        }
        double cumulativeLength = 0;
        double nextCumulativeLength = 0;
        double segmentLength = 0;
        int index = 0;
        List<OtsPoint3D> pointList = new ArrayList<>();
        // CategoryLogger.trace(Cat.CORE, "interval " + start + ".." + end);
        while (start > cumulativeLength)
        {
            OtsPoint3D fromPoint = this.points[index];
            index++;
            OtsPoint3D toPoint = this.points[index];
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
            pointList.add(OtsPoint3D.interpolate((start - cumulativeLength) / segmentLength, this.points[index - 1],
                    this.points[index]));
            if (end > nextCumulativeLength)
            {
                pointList.add(this.points[index]);
            }
        }
        while (end > nextCumulativeLength)
        {
            OtsPoint3D fromPoint = this.points[index];
            index++;
            if (index >= this.points.length)
            {
                break; // rounding error
            }
            OtsPoint3D toPoint = this.points[index];
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
            OtsPoint3D point = OtsPoint3D.interpolate((end - cumulativeLength) / segmentLength, this.points[index - 1],
                    this.points[index]);
            // can be the same due to rounding
            if (!point.equals(pointList.get(pointList.size() - 1)))
            {
                pointList.add(point);
            }
        }
        try
        {
            return new OtsLine3D(pointList);
        }
        catch (OtsGeometryException exception)
        {
            CategoryLogger.always().error(exception, "interval " + start + ".." + end + " too short");
            throw new OtsGeometryException("interval " + start + ".." + end + "too short");
        }
    }

    /**
     * Build an array of OTSPoint3D from an array of Coordinate.
     * @param coordinates Coordinate[]; the coordinates
     * @return OTSPoint3D[]
     */
    private static OtsPoint3D[] coordinatesToOTSPoint3D(final Coordinate[] coordinates) // XXX: DJ
    {
        OtsPoint3D[] result = new OtsPoint3D[coordinates.length];
        for (int i = 0; i < coordinates.length; i++)
        {
            result[i] = new OtsPoint3D(coordinates[i]);
        }
        return result;
    }

    /**
     * Create an OTSLine3D, while cleaning repeating successive points.
     * @param points OTSPoint3D...; the coordinates of the line as OTSPoint3D
     * @return the line
     * @throws OtsGeometryException when number of points &lt; 2
     */
    public static OtsLine3D createAndCleanOTSLine3D(final OtsPoint3D... points) throws OtsGeometryException
    {
        if (points.length < 2)
        {
            throw new OtsGeometryException(
                    "Degenerate OTSLine3D; has " + points.length + " point" + (points.length != 1 ? "s" : ""));
        }
        return createAndCleanOTSLine3D(new ArrayList<>(Arrays.asList(points)));
    }

    /**
     * Create an OTSLine3D, while cleaning repeating successive points.
     * @param pointList List&lt;OTSPoint3D&gt;; list of the coordinates of the line as OTSPoint3D; any duplicate points in this
     *            list are removed (this method may modify the provided list)
     * @return OTSLine3D; the line
     * @throws OtsGeometryException when number of non-equal points &lt; 2
     */
    public static OtsLine3D createAndCleanOTSLine3D(final List<OtsPoint3D> pointList) throws OtsGeometryException
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
        return new OtsLine3D(pointList);
    }

    /**
     * Construct a new OTSLine3D from an array of Coordinate.
     * @param coordinates Coordinate[]; the array of coordinates to construct this OTSLine3D from
     * @throws OtsGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OtsLine3D(final Coordinate[] coordinates) throws OtsGeometryException // XXX: DJ
    {
        this(coordinatesToOTSPoint3D(coordinates));
    }

    /**
     * Construct a new OTSLine3D from a LineString.
     * @param lineString LineString; the lineString to construct this OTSLine3D from.
     * @throws OtsGeometryException when the provided LineString does not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OtsLine3D(final LineString lineString) throws OtsGeometryException // XXX: DJ
    {
        this(lineString.getCoordinates());
    }

    /**
     * Construct a new OTSLine3D from a Geometry.
     * @param geometry Geometry; the geometry to construct this OTSLine3D from
     * @throws OtsGeometryException when the provided Geometry do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OtsLine3D(final Geometry geometry) throws OtsGeometryException // XXX: DJ
    {
        this(geometry.getCoordinates());
    }

    /**
     * Construct a new OTSLine3D from a List&lt;OTSPoint3D&gt;.
     * @param pointList List&lt;OTSPoint3D&gt;; the list of points to construct this OTSLine3D from.
     * @throws OtsGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OtsLine3D(final List<OtsPoint3D> pointList) throws OtsGeometryException
    {
        this(pointList.toArray(new OtsPoint3D[pointList.size()]));
    }

    /**
     * Construct a new OTSShape (closed shape) from a Path2D.
     * @param path Path2D; the Path2D to construct this OTSLine3D from.
     * @throws OtsGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OtsLine3D(final Path2D path) throws OtsGeometryException
    {
        List<OtsPoint3D> pl = new ArrayList<>();
        for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next())
        {
            double[] p = new double[6];
            int segType = pi.currentSegment(p);
            if (segType == PathIterator.SEG_MOVETO || segType == PathIterator.SEG_LINETO)
            {
                pl.add(new OtsPoint3D(p[0], p[1]));
            }
            else if (segType == PathIterator.SEG_CLOSE)
            {
                if (!pl.get(0).equals(pl.get(pl.size() - 1)))
                {
                    pl.add(new OtsPoint3D(pl.get(0).x, pl.get(0).y));
                }
                break;
            }
        }
        init(pl.toArray(new OtsPoint3D[pl.size() - 1]));
    }

    /**
     * Construct a Coordinate array and fill it with the points of this OTSLine3D.
     * @return an array of Coordinates corresponding to this OTSLine
     */
    public final Coordinate[] getCoordinates() // XXX: DJ
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
    public final LineString getLineString() // XXX: DJ
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
    public final OtsPoint3D getFirst()
    {
        return this.points[0];
    }

    /**
     * Return the last point of this OTSLine3D.
     * @return the last point on the line
     */
    public final OtsPoint3D getLast()
    {
        return this.points[size() - 1];
    }

    /**
     * Return one point of this OTSLine3D.
     * @param i int; the index of the point to retrieve
     * @return OTSPoint3d; the i-th point of the line
     * @throws OtsGeometryException when i &lt; 0 or i &gt; the number of points
     */
    public final OtsPoint3D get(final int i) throws OtsGeometryException
    {
        if (i < 0 || i > size() - 1)
        {
            throw new OtsGeometryException("OTSLine3D.get(i=" + i + "); i<0 or i>=size(), which is " + size());
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
    public final Length getLength() // XXX: DJ
    {
        return this.length;
    }

    /**
     * Return an array of OTSPoint3D that represents this OTSLine3D. <strong>Do not modify the result.</strong>
     * @return the points of this line
     */
    public final OtsPoint3D[] getPoints()
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
    public final DirectedPoint getLocationExtended(final Length position) // XXX: DJ
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
    public final synchronized DirectedPoint getLocationExtendedSI(final double positionSI) // XXX: DJ name + super(p)
    {
        makeLengthIndexedLine();
        if (positionSI >= 0.0 && positionSI <= getLengthSI())
        {
            try
            {
                return getLocationSI(positionSI);
            }
            catch (OtsGeometryException exception)
            {
                // cannot happen
            }
        }

        // position before start point -- extrapolate
        if (positionSI < 0.0)
        {
            double len = positionSI;
            double fraction = len / (this.lengthIndexedLine[1] - this.lengthIndexedLine[0]);
            OtsPoint3D p1 = this.points[0];
            OtsPoint3D p2 = this.points[1];
            return new DirectedPoint(p1.x + fraction * (p2.x - p1.x), p1.y + fraction * (p2.y - p1.y),
                    p1.z + fraction * (p2.z - p1.z), 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
        }

        // position beyond end point -- extrapolate
        int n1 = this.lengthIndexedLine.length - 1;
        int n2 = this.lengthIndexedLine.length - 2;
        double len = positionSI - getLengthSI();
        double fraction = len / (this.lengthIndexedLine[n1] - this.lengthIndexedLine[n2]);
        while (Double.isInfinite(fraction))
        {
            if (--n2 < 0)
            {
                CategoryLogger.always().error("lengthIndexedLine of {} is invalid", this);
                OtsPoint3D p = this.points[n1];
                return new DirectedPoint(p.x, p.y, p.z, 0.0, 0.0, 0.0); // Bogus direction
            }
            fraction = len / (this.lengthIndexedLine[n1] - this.lengthIndexedLine[n2]);
        }
        OtsPoint3D p1 = this.points[n2];
        OtsPoint3D p2 = this.points[n1];
        return new DirectedPoint(p2.x + fraction * (p2.x - p1.x), p2.y + fraction * (p2.y - p1.y),
                p2.z + fraction * (p2.z - p1.z), 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
    }

    /**
     * Get the location at a fraction of the line, with its direction. Fraction should be between 0.0 and 1.0.
     * @param fraction double; the fraction for which to calculate the point on the line
     * @return a directed point
     * @throws OtsGeometryException when fraction less than 0.0 or more than 1.0.
     */
    public final DirectedPoint getLocationFraction(final double fraction) throws OtsGeometryException
    {
        if (fraction < 0.0 || fraction > 1.0)
        {
            throw new OtsGeometryException("getLocationFraction for line: fraction < 0.0 or > 1.0. fraction = " + fraction);
        }
        return getLocationSI(fraction * getLengthSI());
    }

    /**
     * Get the location at a fraction of the line, with its direction. Fraction should be between 0.0 and 1.0.
     * @param fraction double; the fraction for which to calculate the point on the line
     * @param tolerance double; the delta from 0.0 and 1.0 that will be forgiven
     * @return a directed point
     * @throws OtsGeometryException when fraction less than 0.0 or more than 1.0.
     */
    public final DirectedPoint getLocationFraction(final double fraction, final double tolerance) throws OtsGeometryException
    {
        if (fraction < -tolerance || fraction > 1.0 + tolerance)
        {
            throw new OtsGeometryException(
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
     * @throws OtsGeometryException when position less than 0.0 or more than line length.
     */
    public final DirectedPoint getLocation(final Length position) throws OtsGeometryException // XXX: DJ
    {
        return getLocationSI(position.getSI());
    }

    /**
     * Binary search for a position on the line.
     * @param pos double; the position to look for.
     * @return the index below the position; the position is between points[index] and points[index+1]
     * @throws OtsGeometryException when index could not be found
     */
    private int find(final double pos) throws OtsGeometryException
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
        throw new OtsGeometryException(
                "Could not find position " + pos + " on line with length indexes: " + Arrays.toString(this.lengthIndexedLine));
    }

    /**
     * Get the location at a position on the line, with its direction. Position should be between 0.0 and line length.
     * @param positionSI double; the position on the line for which to calculate the point on the line
     * @return a directed point
     * @throws OtsGeometryException when position less than 0.0 or more than line length.
     */
    public final synchronized DirectedPoint getLocationSI(final double positionSI) throws OtsGeometryException // XXX: DJ
                                                                                                               // without SI
    {
        makeLengthIndexedLine();
        if (positionSI < 0.0 || positionSI > getLengthSI())
        {
            throw new OtsGeometryException("getLocationSI for line: position < 0.0 or > line length. Position = " + positionSI
                    + " m. Length = " + getLengthSI() + " m.");
        }

        // handle special cases: position == 0.0, or position == length
        if (positionSI == 0.0)
        {
            OtsPoint3D p1 = this.points[0];
            OtsPoint3D p2 = this.points[1];
            return new DirectedPoint(p1.x, p1.y, p1.z, 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
        }
        if (positionSI == getLengthSI())
        {
            OtsPoint3D p1 = this.points[this.points.length - 2];
            OtsPoint3D p2 = this.points[this.points.length - 1];
            return new DirectedPoint(p2.x, p2.y, p2.z, 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
        }

        // find the index of the line segment, use binary search
        int index = find(positionSI);
        double remainder = positionSI - this.lengthIndexedLine[index];
        double fraction = remainder / (this.lengthIndexedLine[index + 1] - this.lengthIndexedLine[index]);
        OtsPoint3D p1 = this.points[index];
        OtsPoint3D p2 = this.points[index + 1];
        return new DirectedPoint(p1.x + fraction * (p2.x - p1.x), p1.y + fraction * (p2.y - p1.y),
                p1.z + fraction * (p2.z - p1.z), 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
    }

    /**
     * Truncate a line at the given length (less than the length of the line, and larger than zero) and return a new line.
     * @param lengthSI double; the location where to truncate the line
     * @return a new OTSLine3D truncated at the exact position where line.getLength() == lengthSI
     * @throws OtsGeometryException when position less than 0.0 or more than line length.
     */
    public final synchronized OtsLine3D truncate(final double lengthSI) throws OtsGeometryException
    {
        makeLengthIndexedLine();
        if (lengthSI <= 0.0 || lengthSI > getLengthSI())
        {
            throw new OtsGeometryException("truncate for line: position <= 0.0 or > line length. Position = " + lengthSI
                    + " m. Length = " + getLengthSI() + " m.");
        }

        // handle special case: position == length
        if (lengthSI == getLengthSI())
        {
            return new OtsLine3D(getPoints());
        }

        // find the index of the line segment
        int index = find(lengthSI);
        double remainder = lengthSI - this.lengthIndexedLine[index];
        double fraction = remainder / (this.lengthIndexedLine[index + 1] - this.lengthIndexedLine[index]);
        OtsPoint3D p1 = this.points[index];
        OtsPoint3D lastPoint;
        if (0.0 == fraction)
        {
            index--;
            lastPoint = p1;
        }
        else
        {
            OtsPoint3D p2 = this.points[index + 1];
            lastPoint = new OtsPoint3D(p1.x + fraction * (p2.x - p1.x), p1.y + fraction * (p2.y - p1.y),
                    p1.z + fraction * (p2.z - p1.z));

        }
        OtsPoint3D[] coords = new OtsPoint3D[index + 2];
        for (int i = 0; i <= index; i++)
        {
            coords[i] = this.points[i];
        }
        coords[index + 1] = lastPoint;
        return new OtsLine3D(coords);
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
            final double y, final FractionalFallback fallback) // XXX: DJ
    {

        // prepare
        makeLengthIndexedLine();
        double minDistance = Double.POSITIVE_INFINITY;
        double minSegmentFraction = 0;
        int minSegment = -1;
        OtsPoint3D point = new OtsPoint3D(x, y);

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
            OtsPoint3D center = this.fractionalHelperCenters[i];
            OtsPoint3D p;
            if (center != null)
            {
                // get intersection of line "center - (x, y)" and the segment
                p = OtsPoint3D.intersectionOfLines(center, point, this.points[i], this.points[i + 1]);
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
                OtsPoint3D offsetPoint =
                        new OtsPoint3D(x + this.fractionalHelperDirections[i].x, y + this.fractionalHelperDirections[i].y);
                p = OtsPoint3D.intersectionOfLines(point, offsetPoint, this.points[i], this.points[i + 1]);
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
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public enum FractionalFallback // XXX: DJ
    {
        /** Orthogonal projection. */
        ORTHOGONAL
        {
            @Override
            double getFraction(final OtsLine3D line, final double x, final double y)
            {
                return line.projectOrthogonal(x, y);
            }
        },

        /** Distance to nearest end point. */
        ENDPOINT
        {
            @Override
            double getFraction(final OtsLine3D line, final double x, final double y)
            {
                OtsPoint3D point = new OtsPoint3D(x, y);
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
            double getFraction(final OtsLine3D line, final double x, final double y)
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
        abstract double getFraction(OtsLine3D line, double x, double y);

    }

    /**
     * Determines all helpers (points and/or directions) for fractional projection and stores fixed information in properties
     * while returning the first and last center points (.
     * @param start Direction; direction in first point
     * @param end Direction; direction in last point
     */
    private synchronized void determineFractionalHelpers(final Direction start, final Direction end) // XXX: DJ
    {

        final int n = this.points.length - 1;

        // calculate fixed helpers if not done yet
        if (this.fractionalHelperCenters == null)
        {
            this.fractionalHelperCenters = new OtsPoint3D[n];
            this.fractionalHelperDirections = new Point2D.Double[n];
            if (this.points.length > 2)
            {
                // intersection of parallel lines of first and second segment
                OtsLine3D prevOfsSeg = unitOffsetSegment(0);
                OtsLine3D nextOfsSeg = unitOffsetSegment(1);
                OtsPoint3D parStartPoint;
                try
                {
                    parStartPoint = OtsPoint3D.intersectionOfLines(prevOfsSeg.get(0), prevOfsSeg.get(1), nextOfsSeg.get(0),
                            nextOfsSeg.get(1));
                    if (parStartPoint == null || prevOfsSeg.get(1).distanceSI(nextOfsSeg.get(0)) < Math
                            .min(prevOfsSeg.get(1).distanceSI(parStartPoint), nextOfsSeg.get(0).distanceSI(parStartPoint)))
                    {
                        parStartPoint = new OtsPoint3D((prevOfsSeg.get(1).x + nextOfsSeg.get(0).x) / 2,
                                (prevOfsSeg.get(1).y + nextOfsSeg.get(0).y) / 2);
                    }
                }
                catch (OtsGeometryException oge)
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
                    OtsPoint3D parEndPoint;
                    try
                    {
                        parEndPoint = OtsPoint3D.intersectionOfLines(prevOfsSeg.get(0), prevOfsSeg.get(1), nextOfsSeg.get(0),
                                nextOfsSeg.get(1));
                        if (parEndPoint == null || prevOfsSeg.get(1).distanceSI(nextOfsSeg.get(0)) < Math
                                .min(prevOfsSeg.get(1).distanceSI(parEndPoint), nextOfsSeg.get(0).distanceSI(parEndPoint)))
                        {
                            parEndPoint = new OtsPoint3D((prevOfsSeg.get(1).x + nextOfsSeg.get(0).x) / 2,
                                    (prevOfsSeg.get(1).y + nextOfsSeg.get(0).y) / 2);
                        }
                    }
                    catch (OtsGeometryException oge)
                    {
                        // cannot happen as only the first and second point (which are always present) are requested
                        throw new RuntimeException(oge);
                    }
                    // center = intersections of helper lines
                    this.fractionalHelperCenters[i] =
                            OtsPoint3D.intersectionOfLines(this.points[i], parStartPoint, this.points[i + 1], parEndPoint);
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
        OtsPoint3D p1 = new OtsPoint3D(this.points[0].x + Math.cos(ang), this.points[0].y + Math.sin(ang));
        ang = (end == null ? Math.atan2(this.points[n].y - this.points[n - 1].y, this.points[n].x - this.points[n - 1].x)
                : end.getInUnit(DirectionUnit.DEFAULT)) + Math.PI / 2; // end.si + Math.PI / 2;
        OtsPoint3D p2 = new OtsPoint3D(this.points[n].x + Math.cos(ang), this.points[n].y + Math.sin(ang));

        // calculate first and last center (i.e. intersection of unit offset segments), which depend on inputs 'start' and 'end'
        if (this.points.length > 2)
        {
            this.fractionalHelperCenters[0] =
                    OtsPoint3D.intersectionOfLines(this.points[0], p1, this.points[1], this.firstOffsetIntersection);
            this.fractionalHelperCenters[n - 1] =
                    OtsPoint3D.intersectionOfLines(this.points[n - 1], this.lastOffsetIntersection, this.points[n], p2);
            if (this.fractionalHelperCenters[n - 1] == null)
            {
                // parallel helper lines, use direction for projection
                this.fractionalHelperDirections[n - 1] = new Point2D.Double(p2.x - this.points[n].x, p2.y - this.points[n].y);
            }
        }
        else
        {
            // only a single segment
            this.fractionalHelperCenters[0] = OtsPoint3D.intersectionOfLines(this.points[0], p1, this.points[1], p2);
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
    private synchronized OtsLine3D unitOffsetSegment(final int segment) // XXX: DJ
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
        OtsPoint3D from = new OtsPoint3D(this.points[segment].x, this.points[segment].y);
        OtsPoint3D to = new OtsPoint3D(this.points[segment + 1].x, this.points[segment + 1].y);
        try
        {
            OtsLine3D line = new OtsLine3D(from, to);
            return line.offsetLine(1.0);
        }
        catch (OtsGeometryException oge)
        {
            // Cannot happen as points are from this OTSLine3D which performed the same checks and 2 points are given
            throw new RuntimeException(oge);
        }
    }

    /**
     * Returns the projected directional radius of the line at a given fraction. Negative values reflect right-hand curvature in
     * the design-line direction. The radius is taken as the minimum of the radii at the vertices before and after the given
     * fraction. The radius at a vertex is calculated as the radius of a circle that is equidistant from both edges connected to
     * the vertex. The circle center is on a line perpendicular to the shortest edge, crossing through the middle of the
     * shortest edge. This method ignores Z components.
     * @param fraction double; fraction along the line, between 0.0 and 1.0 (both inclusive)
     * @return Length; radius; the local radius; or si field set to Double.NaN if line is totally straight
     * @throws OtsGeometryException fraction out of bounds
     */
    public synchronized Length getProjectedRadius(final double fraction) throws OtsGeometryException // XXX: DJ
    {
        Throw.when(fraction < 0.0 || fraction > 1.0, OtsGeometryException.class, "Fraction %f is out of bounds [0.0 ... 1.0]",
                fraction);
        if (this.vertexRadii == null)
        {
            this.vertexRadii = new Length[size() - 1];
        }
        int index = find(fraction * getLength().si);
        if (index > 0 && this.vertexRadii[index] == null)
        {
            this.vertexRadii[index] = getProjectedVertexRadius(index);
        }
        if (index < size() - 2 && this.vertexRadii[index + 1] == null)
        {
            this.vertexRadii[index + 1] = getProjectedVertexRadius(index + 1);
        }
        if (index == 0)
        {
            if (this.vertexRadii.length < 2)
            {
                return Length.instantiateSI(Double.NaN);
            }
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
     * edge. This function ignores Z components.
     * @param index int; index of the vertex in range [1 ... size() - 2]
     * @return Length; radius at the vertex
     * @throws OtsGeometryException if the index is out of bounds
     */
    public synchronized Length getProjectedVertexRadius(final int index) throws OtsGeometryException // XXX: DJ
    {
        Throw.when(index < 1 || index > size() - 2, OtsGeometryException.class, "Index %d is out of bounds [1 ... size() - 2].",
                index);
        makeLengthIndexedLine();
        determineFractionalHelpers(null, null);
        double length1 = this.lengthIndexedLine[index] - this.lengthIndexedLine[index - 1];
        double length2 = this.lengthIndexedLine[index + 1] - this.lengthIndexedLine[index];
        int shortIndex = length1 < length2 ? index : index + 1;
        // center of shortest edge
        OtsPoint3D p1 = new OtsPoint3D(.5 * (this.points[shortIndex - 1].x + this.points[shortIndex].x),
                .5 * (this.points[shortIndex - 1].y + this.points[shortIndex].y),
                .5 * (this.points[shortIndex - 1].z + this.points[shortIndex].z));
        // perpendicular to shortest edge, line crossing p1
        OtsPoint3D p2 = new OtsPoint3D(p1.x + (this.points[shortIndex].y - this.points[shortIndex - 1].y),
                p1.y - (this.points[shortIndex].x - this.points[shortIndex - 1].x), p1.z);
        // vertex
        OtsPoint3D p3 = this.points[index];
        // point on line that splits angle between edges at vertex 50-50
        OtsPoint3D p4 = this.fractionalHelperCenters[index];
        if (p4 == null)
        {
            // parallel helper lines
            p4 = new OtsPoint3D(p3.x + this.fractionalHelperDirections[index].x,
                    p3.y + this.fractionalHelperDirections[index].y);
        }
        OtsPoint3D intersection = OtsPoint3D.intersectionOfLines(p1, p2, p3, p4);
        if (null == intersection)
        {
            return Length.instantiateSI(Double.NaN);
        }
        // determine left or right
        double refLength = length1 < length2 ? length1 : length2;
        Length radius = intersection.horizontalDistance(p1);
        Length i2p2 = intersection.horizontalDistance(p2);
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
     * @throws OtsGeometryException if the index is out of bounds
     */
    public synchronized double getVertexFraction(final int index) throws OtsGeometryException // XXX: DJ
    {
        Throw.when(index < 0 || index > size() - 1, OtsGeometryException.class, "Index %d is out of bounds [0 %d].", index,
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
        for (OtsPoint3D p : this.points)
        {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            minZ = Math.min(minZ, p.z);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
            maxZ = Math.max(maxZ, p.z);
        }
        this.centroid = new OtsPoint3D((maxX + minX) / 2, (maxY + minY) / 2, (maxZ + minZ) / 2);
        double deltaX = maxX - minX;
        double deltaY = maxY - minY;
        double deltaZ = maxZ - minZ;
        this.bounds = new Bounds(new Point3d(-deltaX / 2.0, -deltaY / 2.0, -deltaZ / 2.0),
                new Point3d(deltaX / 2, deltaY / 2, deltaZ / 2));
        this.envelope = new Envelope(minX, maxX, minY, maxY);
    }

    /**
     * Retrieve the centroid of this OTSLine3D.
     * @return OTSPoint3D; the centroid of this OTSLine3D
     */
    public final synchronized OtsPoint3D getCentroid()
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
    public final synchronized Envelope getEnvelope() // XXX: DJ uses BoundingRectangle
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
    public synchronized DirectedPoint getLocation() // XXX: DJ
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
    public synchronized Bounds getBounds() // XXX: DJ
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
        OtsLine3D other = (OtsLine3D) obj;
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
        for (OtsPoint3D p : this.points)
        {
            s.append(p.x + "\t" + p.y + "\n");
        }
        return s.toString();
    }

    /**
     * Convert the 2D projection of this OTSLine3D to Peter's plot format.
     * @return Peter's format plot output
     */
    public final String toPlot() // XXX: DJ
    {
        StringBuffer result = new StringBuffer();
        for (OtsPoint3D p : this.points)
        {
            result.append(String.format(Locale.US, "%s%.3f,%.3f", 0 == result.length() ? "M" : " L", p.x, p.y));
        }
        result.append("\n");
        return result.toString();
    }

}

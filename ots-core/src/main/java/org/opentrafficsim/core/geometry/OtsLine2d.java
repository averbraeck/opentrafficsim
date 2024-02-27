package org.opentrafficsim.core.geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.djunits.unit.DirectionUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Line with underlying PolyLine2d, a cached length indexed line, a cached length, and a cached centroid (all calculated on
 * first use). This class supports fractional projection.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OtsLine2d implements Locatable, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** The 2d line. */
    private PolyLine2d line2d;

    /** The cumulative length of the line at point 'i'. */
    private double[] lengthIndexedLine = null;

    /** The cached length; will be calculated at time of construction. */
    private Length length;

    /** The cached centroid; will be calculated when needed for the first time. */
    private Point2d centroid = null;

    /** The cached bounds; will be calculated when needed for the first time. */
    private Bounds2d bounds = null;

    /** The cached helper points for fractional projection; will be calculated when needed for the first time. */
    private Point2d[] fractionalHelperCenters = null;

    /** The cached helper directions for fractional projection; will be calculated when needed for the first time. */
    private Point2D.Double[] fractionalHelperDirections = null;

    /** Intersection of unit offset lines of first two segments. */
    private Point2d firstOffsetIntersection;

    /** Intersection of unit offset lines of last two segments. */
    private Point2d lastOffsetIntersection;

    /** Precision for fractional projection algorithm. */
    private static final double FRAC_PROJ_PRECISION = 2e-5 /* PK too fine 1e-6 */;

    /** Radius at each vertex. */
    private Length[] vertexRadii;

    /**
     * Construct a new OtsLine2d.
     * @param points Point2d...; the array of points to construct this OtsLine2d from.
     */
    public OtsLine2d(final Point2d... points)
    {
        this(new PolyLine2d(points));
    }

    /**
     * Creates a new OtsLine2d based on 2d information. Elevation will be 0.
     * @param line2d PolyLine2d; 2d information.
     */
    public OtsLine2d(final PolyLine2d line2d)
    {
        init(line2d);
    }

    /**
     * Construct a new OtsLine2d, and immediately make the length-indexed line.
     * @param line2d PolyLine2d; the 2d line.
     */
    private void init(final PolyLine2d line2d)
    {
        this.lengthIndexedLine = new double[line2d.size()];
        this.lengthIndexedLine[0] = 0.0;
        for (int i = 1; i < line2d.size(); i++)
        {
            this.lengthIndexedLine[i] = this.lengthIndexedLine[i - 1] + line2d.get(i - 1).distance(line2d.get(i));
        }
        this.line2d = line2d;
        this.length = Length.instantiateSI(this.lengthIndexedLine[this.lengthIndexedLine.length - 1]);
    }

    /**
     * Construct parallel line.<br>
     * @param offset double; offset distance from the reference line; positive is LEFT, negative is RIGHT
     * @return OtsLine2d; the line that has the specified offset from the reference line
     */
    public final OtsLine2d offsetLine(final double offset)
    {
        return new OtsLine2d(this.line2d.offsetLine(offset));
    }

    /**
     * Clean up a list of points that describe a polyLine by removing points that lie within epsilon distance of a more
     * straightened version of the line. <br>
     * @param epsilon double; maximal deviation
     * @param useHorizontalDistance boolean; if true; the horizontal distance is used; if false; the 3D distance is used
     * @return OtsLine2d; a new OtsLine2d containing all the remaining points
     */
    @Deprecated
    public final OtsLine2d noiseFilterRamerDouglasPeucker(final double epsilon, final boolean useHorizontalDistance)
    {
        // Apply the Ramer-Douglas-Peucker algorithm to the buffered points.
        // Adapted from https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
        double maxDeviation = 0;
        int splitIndex = -1;
        int pointCount = size();
        // Find the point with largest deviation from the straight line from start point to end point
        for (int i = 1; i < pointCount - 1; i++)
        {
            Point2d point = this.line2d.get(i);
            Point2d closest = point.closestPointOnLine(this.line2d.get(0), this.line2d.get(pointCount - 1));
            double deviation = useHorizontalDistance ? closest.distance(point) : closest.distance(point);
            if (deviation > maxDeviation)
            {
                splitIndex = i;
                maxDeviation = deviation;
            }
        }
        if (maxDeviation <= epsilon)
        {
            // All intermediate points can be dropped. Return a new list containing only the first and last point.
            return new OtsLine2d(this.line2d.get(0), this.line2d.get(pointCount - 1));
        }
        // The largest deviation is larger than epsilon.
        // Split the polyLine at the point with the maximum deviation. Process each sub list recursively and concatenate the
        // results
        List<Point2d> points = this.line2d.getPointList();
        OtsLine2d first = new OtsLine2d(points.subList(0, splitIndex + 1).toArray(new Point2d[splitIndex + 1]))
                .noiseFilterRamerDouglasPeucker(epsilon, useHorizontalDistance);
        OtsLine2d second = new OtsLine2d(
                points.subList(splitIndex, this.line2d.size()).toArray(new Point2d[this.line2d.size() - splitIndex]))
                        .noiseFilterRamerDouglasPeucker(epsilon, useHorizontalDistance);
        return concatenate(epsilon, first, second);
    }

    /**
     * Returns a 2d representation of this line.
     * @return PolyLine2d; Returns a 2d representation of this line.
     */
    public PolyLine2d getLine2d()
    {
        return this.line2d;
    }

    /**
     * Create a line at linearly varying offset from this line. The offset may change linearly from its initial value at the
     * start of the reference line to its final offset value at the end of the reference line.
     * @param offsetAtStart double; offset at the start of the reference line (positive value is Left, negative value is Right)
     * @param offsetAtEnd double; offset at the end of the reference line (positive value is Left, negative value is Right)
     * @return OtsLine2d; the OtsLine2d of the line at linearly changing offset of the reference line
     */
    public final OtsLine2d offsetLine(final double offsetAtStart, final double offsetAtEnd)
    {
        return new OtsLine2d(this.line2d.offsetLine(offsetAtStart, offsetAtEnd));
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
    public final OtsLine2d offsetLine(final double[] relativeFractions, final double[] offsets) throws OtsGeometryException
    {
        return new OtsLine2d(OtsGeometryUtil.offsetLine(this.line2d, relativeFractions, offsets));
    }

    /**
     * Concatenate several OtsLine2d instances.
     * @param lines OtsLine2d...; OtsLine2d... one or more OtsLine2d. The last point of the first
     *            &lt;strong&gt;must&lt;/strong&gt; match the first of the second, etc.
     * @return OtsLine2d
     */
    public static OtsLine2d concatenate(final OtsLine2d... lines)
    {
        return concatenate(0.0, lines);
    }

    /**
     * Concatenate two OtsLine2d instances. This method is separate for efficiency reasons.
     * @param toleranceSI double; the tolerance between the end point of a line and the first point of the next line
     * @param line1 OtsLine2d; first line
     * @param line2 OtsLine2d; second line
     * @return OtsLine2d
     */
    public static OtsLine2d concatenate(final double toleranceSI, final OtsLine2d line1, final OtsLine2d line2)
    {
        return new OtsLine2d(PolyLine2d.concatenate(toleranceSI, line1.line2d, line2.line2d));
    }

    /**
     * Concatenate several OtsLine2d instances.
     * @param toleranceSI double; the tolerance between the end point of a line and the first point of the next line
     * @param lines OtsLine2d...; OtsLine2d... one or more OtsLine2d. The last point of the first
     *            &lt;strong&gt;must&lt;/strong&gt; match the first of the second, etc.
     * @return OtsLine2d
     */
    public static OtsLine2d concatenate(final double toleranceSI, final OtsLine2d... lines)
    {
        List<PolyLine2d> lines2d = new ArrayList<>();
        for (OtsLine2d line : lines)
        {
            lines2d.add(line.line2d);
        }
        return new OtsLine2d(PolyLine2d.concatenate(toleranceSI, lines2d.toArray(new PolyLine2d[lines.length])));
    }

    /**
     * Construct a new OtsLine2d with all points of this OtsLine2d in reverse order.
     * @return OtsLine2d; the new OtsLine2d
     */
    public final OtsLine2d reverse()
    {
        return new OtsLine2d(this.line2d.reverse());
    }

    /**
     * Construct a new OtsLine2d covering the indicated fraction of this OtsLine2d.
     * @param start double; starting point, valid range [0..<cite>end</cite>)
     * @param end double; ending point, valid range (<cite>start</cite>..1]
     * @return OtsLine2d; the new OtsLine2d
     */
    public final OtsLine2d extractFractional(final double start, final double end)
    {
        return extract(start * this.length.si, end * this.length.si);
    }

    /**
     * Create a new OtsLine2d that covers a sub-section of this OtsLine2d.
     * @param start Length; the length along this OtsLine2d where the sub-section starts, valid range [0..<cite>end</cite>)
     * @param end Length; length along this OtsLine2d where the sub-section ends, valid range
     *            (<cite>start</cite>..<cite>length</cite> (length is the length of this OtsLine2d)
     * @return OtsLine2d; the selected sub-section
     */
    public final OtsLine2d extract(final Length start, final Length end)
    {
        return extract(start.si, end.si);
    }

    /**
     * Create a new OtsLine2d that covers a sub-section of this OtsLine2d.
     * @param start double; length along this OtsLine2d where the sub-section starts, valid range [0..<cite>end</cite>)
     * @param end double; length along this OtsLine2d where the sub-section ends, valid range
     *            (<cite>start</cite>..<cite>length</cite> (length is the length of this OtsLine2d)
     * @return OtsLine2d; the selected sub-section
     */
    public final OtsLine2d extract(final double start, final double end)
    {
        return new OtsLine2d(this.line2d.extract(start, end));
    }

    /**
     * Create an OtsLine2d, while cleaning repeating successive points.
     * @param points Point2d...; the coordinates of the line as OtsPoint3d
     * @return the line
     * @throws OtsGeometryException when number of points &lt; 2
     */
    public static OtsLine2d createAndCleanOtsLine2d(final Point2d... points) throws OtsGeometryException
    {
        if (points.length < 2)
        {
            throw new OtsGeometryException(
                    "Degenerate OtsLine2d; has " + points.length + " point" + (points.length != 1 ? "s" : ""));
        }
        return createAndCleanOtsLine2d(new ArrayList<>(Arrays.asList(points)));
    }

    /**
     * Create an OtsLine2d, while cleaning repeating successive points.
     * @param pointList List&lt;Point2d&gt;; list of the coordinates of the line as OtsPoint3d; any duplicate points in this
     *            list are removed (this method may modify the provided list)
     * @return OtsLine2d; the line
     * @throws OtsGeometryException when number of non-equal points &lt; 2
     */
    public static OtsLine2d createAndCleanOtsLine2d(final List<Point2d> pointList) throws OtsGeometryException
    {
        return new OtsLine2d(new PolyLine2d(true, pointList));
    }

    /**
     * Construct a new OtsLine2d from a List&lt;OtsPoint3d&gt;.
     * @param pointList List&lt;OtsPoint3d&gt;; the list of points to construct this OtsLine2d from.
     * @throws OtsGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OtsLine2d(final List<Point2d> pointList) throws OtsGeometryException
    {
        this(pointList.toArray(new Point2d[pointList.size()]));
    }

    /**
     * Construct a new OtsShape (closed shape) from a Path2D. Elevation will be 0.
     * @param path Path2D; the Path2D to construct this OtsLine2d from.
     * @throws OtsGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OtsLine2d(final Path2D path) throws OtsGeometryException
    {
        List<Point2d> pl = new ArrayList<>();
        for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next())
        {
            double[] p = new double[6];
            int segType = pi.currentSegment(p);
            if (segType == PathIterator.SEG_MOVETO || segType == PathIterator.SEG_LINETO)
            {
                pl.add(new Point2d(p[0], p[1]));
            }
            else if (segType == PathIterator.SEG_CLOSE)
            {
                if (!pl.get(0).equals(pl.get(pl.size() - 1)))
                {
                    pl.add(new Point2d(pl.get(0).x, pl.get(0).y));
                }
                break;
            }
        }
        init(new PolyLine2d(pl.toArray(new Point2d[pl.size() - 1])));
    }

    /**
     * Return the number of points in this OtsLine2d. This is the number of points in horizontal plane.
     * @return the number of points on the line
     */
    public final int size()
    {
        return this.line2d.size();
    }

    /**
     * Return the first point of this OtsLine2d.
     * @return the first point on the line
     */
    public final Point2d getFirst()
    {
        return this.line2d.getFirst();
    }

    /**
     * Return the last point of this OtsLine2d.
     * @return the last point on the line
     */
    public final Point2d getLast()
    {
        return this.line2d.getLast();
    }

    /**
     * Return one point of this OtsLine2d.
     * @param i int; the index of the point to retrieve
     * @return Point2d; the i-th point of the line
     * @throws OtsGeometryException when i &lt; 0 or i &gt; the number of points
     */
    public final Point2d get(final int i) throws OtsGeometryException
    {
        if (i < 0 || i > size() - 1)
        {
            throw new OtsGeometryException("OtsLine2d.get(i=" + i + "); i<0 or i>=size(), which is " + size());
        }
        return this.line2d.get(i);
    }

    /**
     * Return the length of this OtsLine2d in meters. (Assuming that the coordinates of the points constituting this line are
     * expressed in meters.)
     * @return the length of the line
     */
    public final Length getLength()
    {
        return this.length;
    }

    /**
     * Return an array of OtsPoint3d that represents this OtsLine2d.
     * @return the points of this line
     */
    public final Point2d[] getPoints()
    {
        return this.line2d.getPointList().toArray(new Point2d[this.line2d.size()]);
    }

    /**
     * Get the location at a position on the line, with its direction. Position can be below 0 or more than the line length. In
     * that case, the position will be extrapolated in the direction of the line at its start or end.
     * @param position Length; the position on the line for which to calculate the point on, before, of after the line
     * @return a directed point
     */
    public final OrientedPoint2d getLocationExtended(final Length position)
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
    public final synchronized OrientedPoint2d getLocationExtendedSI(final double positionSI)
    {
        Ray2d ray = this.line2d.getLocationExtended(positionSI);
        return new OrientedPoint2d(ray.x, ray.y, ray.phi);
    }

    /**
     * Get the location at a fraction of the line, with its direction. Fraction should be between 0.0 and 1.0.
     * @param fraction double; the fraction for which to calculate the point on the line
     * @return a directed point
     * @throws OtsGeometryException when fraction less than 0.0 or more than 1.0.
     */
    public final OrientedPoint2d getLocationFraction(final double fraction) throws OtsGeometryException
    {
        if (fraction < 0.0 || fraction > 1.0)
        {
            throw new OtsGeometryException("getLocationFraction for line: fraction < 0.0 or > 1.0. fraction = " + fraction);
        }
        return getLocationSI(fraction * this.length.si);
    }

    /**
     * Get the location at a fraction of the line, with its direction. Fraction should be between 0.0 and 1.0.
     * @param fraction double; the fraction for which to calculate the point on the line
     * @param tolerance double; the delta from 0.0 and 1.0 that will be forgiven
     * @return a directed point
     * @throws OtsGeometryException when fraction less than 0.0 or more than 1.0.
     */
    public final OrientedPoint2d getLocationFraction(final double fraction, final double tolerance) throws OtsGeometryException
    {
        if (fraction < -tolerance || fraction > 1.0 + tolerance)
        {
            throw new OtsGeometryException(
                    "getLocationFraction for line: fraction < 0.0 - tolerance or > 1.0 + tolerance; fraction = " + fraction);
        }
        double f = fraction < 0 ? 0.0 : fraction > 1.0 ? 1.0 : fraction;
        return getLocationSI(f * this.length.si);
    }

    /**
     * Get the location at a fraction of the line (or outside the line), with its direction.
     * @param fraction double; the fraction for which to calculate the point on the line
     * @return a directed point
     */
    public final OrientedPoint2d getLocationFractionExtended(final double fraction)
    {
        return getLocationExtendedSI(fraction * this.length.si);
    }

    /**
     * Get the location at a position on the line, with its direction. Position should be between 0.0 and line length.
     * @param position Length; the position on the line for which to calculate the point on the line
     * @return a directed point
     * @throws OtsGeometryException when position less than 0.0 or more than line length.
     */
    public final OrientedPoint2d getLocation(final Length position) throws OtsGeometryException
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
    public final OrientedPoint2d getLocationSI(final double positionSI) throws OtsGeometryException
    {
        Ray2d ray = Try.assign(() -> this.line2d.getLocation(positionSI), OtsGeometryException.class, "Position not on line.");
        return new OrientedPoint2d(ray.x, ray.y, ray.phi);
    }

    /**
     * Truncate a line at the given length (less than the length of the line, and larger than zero) and return a new line.
     * @param lengthSI double; the location where to truncate the line
     * @return a new OtsLine2d truncated at the exact position where line.getLength() == lengthSI
     * @throws OtsGeometryException when position less than 0.0 or more than line length.
     */
    public final OtsLine2d truncate(final double lengthSI) throws OtsGeometryException
    {
        return new OtsLine2d(this.line2d.truncate(lengthSI));
    }

    /**
     * Returns the fractional position along this line of the orthogonal projection of point (x, y) on this line. If the point
     * is not orthogonal to the closest line segment, the nearest point is selected.
     * @param x double; x-coordinate of point to project
     * @param y double; y-coordinate of point to project
     * @return fractional position along this line of the orthogonal projection on this line of a point
     */
    public final double projectOrthogonal(final double x, final double y)
    {
        Point2d closest = this.line2d.closestPointOnPolyLine(new Point2d(x, y));
        return this.line2d.projectOrthogonalFractionalExtended(closest);
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
        double minDistance = Double.POSITIVE_INFINITY;
        double minSegmentFraction = 0;
        int minSegment = -1;
        Point2d point = new Point2d(x, y);

        // determine helpers (centers and directions)
        determineFractionalHelpers(start, end);

        // get distance of point to each segment
        double[] d = new double[size() - 1];
        double minD = Double.POSITIVE_INFINITY;
        for (int i = 0; i < size() - 1; i++)
        {
            d[i] = Line2D.ptSegDist(this.line2d.get(i).x, this.line2d.get(i).y, this.line2d.get(i + 1).x,
                    this.line2d.get(i + 1).y, x, y);
            minD = d[i] < minD ? d[i] : minD;
        }

        // loop over segments for projection
        double distance;
        for (int i = 0; i < size() - 1; i++)
        {
            // skip if not the closest segment, note that often two segments are equally close in their shared end point
            if (d[i] > minD + FRAC_PROJ_PRECISION)
            {
                continue;
            }
            Point2d center = this.fractionalHelperCenters[i];
            Point2d p;
            if (center != null)
            {
                // get intersection of line "center - (x, y)" and the segment
                p = intersectionOfLines(center, point, this.line2d.get(i), this.line2d.get(i + 1));
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
                Point2d offsetPoint =
                        new Point2d(x + this.fractionalHelperDirections[i].x, y + this.fractionalHelperDirections[i].y);
                p = intersectionOfLines(point, offsetPoint, this.line2d.get(i), this.line2d.get(i + 1));
            }
            double segLength = this.line2d.get(i).distance(this.line2d.get(i + 1)) + FRAC_PROJ_PRECISION;
            if (p == null || this.line2d.get(i).distance(p) > segLength || this.line2d.get(i + 1).distance(p) > segLength)
            {
                // intersection must be on the segment
                // in case of p == null, the length of the fractional helper direction falls away due to precision
                continue;
            }
            // distance from (x, y) to intersection on segment
            double dx = x - p.x;
            double dy = y - p.y;
            distance = Math.hypot(dx, dy);
            // distance from start of segment to point on segment
            if (distance < minDistance)
            {
                dx = p.x - this.line2d.get(i).x;
                dy = p.y - this.line2d.get(i).y;
                double dFrac = Math.hypot(dx, dy);
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
        return (this.lengthIndexedLine[minSegment] + segLen * minSegmentFraction) / this.length.si;

    }

    /**
     * Fallback method for when fractional projection fails as the point is beyond the line or from numerical limitations.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public enum FractionalFallback
    {
        /** Orthogonal projection. */
        ORTHOGONAL
        {
            @Override
            double getFraction(final OtsLine2d line, final double x, final double y)
            {
                return line.projectOrthogonal(x, y);
            }
        },

        /** Distance to nearest end point. */
        ENDPOINT
        {
            @Override
            double getFraction(final OtsLine2d line, final double x, final double y)
            {
                Point2d point = new Point2d(x, y);
                double dStart = point.distance(line.getFirst());
                double dEnd = point.distance(line.getLast());
                if (dStart < dEnd)
                {
                    return -dStart / line.length.si;
                }
                else
                {
                    return (dEnd + line.length.si) / line.length.si;
                }
            }
        },

        /** NaN value. */
        NaN
        {
            @Override
            double getFraction(final OtsLine2d line, final double x, final double y)
            {
                return Double.NaN;
            }
        };

        /**
         * Returns fraction for when fractional projection fails as the point is beyond the line or from numerical limitations.
         * @param line OtsLine2d; line
         * @param x double; x coordinate of point
         * @param y double; y coordinate of point
         * @return double; fraction for when fractional projection fails
         */
        abstract double getFraction(OtsLine2d line, double x, double y);

    }

    /**
     * Determines all helpers (points and/or directions) for fractional projection and stores fixed information in properties
     * while returning the first and last center points (.
     * @param start Direction; direction in first point
     * @param end Direction; direction in last point
     */
    private synchronized void determineFractionalHelpers(final Direction start, final Direction end)
    {

        final int n = size() - 1;

        // calculate fixed helpers if not done yet
        if (this.fractionalHelperCenters == null)
        {
            this.fractionalHelperCenters = new Point2d[n];
            this.fractionalHelperDirections = new Point2D.Double[n];
            if (size() > 2)
            {
                // intersection of parallel lines of first and second segment
                PolyLine2d prevOfsSeg = unitOffsetSegment(0);
                PolyLine2d nextOfsSeg = unitOffsetSegment(1);
                Point2d parStartPoint;
                parStartPoint = intersectionOfLines(prevOfsSeg.get(0), prevOfsSeg.get(1), nextOfsSeg.get(0), nextOfsSeg.get(1));
                if (parStartPoint == null || prevOfsSeg.get(1).distance(nextOfsSeg.get(0)) < Math
                        .min(prevOfsSeg.get(1).distance(parStartPoint), nextOfsSeg.get(0).distance(parStartPoint)))
                {
                    parStartPoint = new Point2d((prevOfsSeg.get(1).x + nextOfsSeg.get(0).x) / 2,
                            (prevOfsSeg.get(1).y + nextOfsSeg.get(0).y) / 2);
                }
                // remember the intersection of the first two unit offset segments
                this.firstOffsetIntersection = parStartPoint;
                // loop segments
                for (int i = 1; i < size() - 2; i++)
                {
                    prevOfsSeg = nextOfsSeg;
                    nextOfsSeg = unitOffsetSegment(i + 1);
                    Point2d parEndPoint;
                    parEndPoint =
                            intersectionOfLines(prevOfsSeg.get(0), prevOfsSeg.get(1), nextOfsSeg.get(0), nextOfsSeg.get(1));
                    if (parEndPoint == null || prevOfsSeg.get(1).distance(nextOfsSeg.get(0)) < Math
                            .min(prevOfsSeg.get(1).distance(parEndPoint), nextOfsSeg.get(0).distance(parEndPoint)))
                    {
                        parEndPoint = new Point2d((prevOfsSeg.get(1).x + nextOfsSeg.get(0).x) / 2,
                                (prevOfsSeg.get(1).y + nextOfsSeg.get(0).y) / 2);
                    }
                    // center = intersections of helper lines
                    this.fractionalHelperCenters[i] =
                            intersectionOfLines(this.line2d.get(i), parStartPoint, this.line2d.get(i + 1), parEndPoint);
                    if (this.fractionalHelperCenters[i] == null)
                    {
                        // parallel helper lines, parallel segments or /\/ cause parallel helper lines, use direction
                        this.fractionalHelperDirections[i] = new Point2D.Double(parStartPoint.x - this.line2d.get(i).x,
                                parStartPoint.y - this.line2d.get(i).y);
                    }
                    parStartPoint = parEndPoint;
                }
                // remember the intersection of the last two unit offset segments
                this.lastOffsetIntersection = parStartPoint;
            }
        }

        // use directions at start and end to get unit offset points to the left at a distance of 1
        double ang = (start == null
                ? Math.atan2(this.line2d.get(1).y - this.line2d.get(0).y, this.line2d.get(1).x - this.line2d.get(0).x)
                : start.getInUnit(DirectionUnit.DEFAULT)) + Math.PI / 2; // start.si + Math.PI / 2;
        Point2d p1 = new Point2d(this.line2d.get(0).x + Math.cos(ang), this.line2d.get(0).y + Math.sin(ang));
        ang = (end == null
                ? Math.atan2(this.line2d.get(n).y - this.line2d.get(n - 1).y, this.line2d.get(n).x - this.line2d.get(n - 1).x)
                : end.getInUnit(DirectionUnit.DEFAULT)) + Math.PI / 2; // end.si + Math.PI / 2;
        Point2d p2 = new Point2d(this.line2d.get(n).x + Math.cos(ang), this.line2d.get(n).y + Math.sin(ang));

        // calculate first and last center (i.e. intersection of unit offset segments), which depend on inputs 'start' and 'end'
        if (size() > 2)
        {
            this.fractionalHelperCenters[0] =
                    intersectionOfLines(this.line2d.get(0), p1, this.line2d.get(1), this.firstOffsetIntersection);
            this.fractionalHelperCenters[n - 1] =
                    intersectionOfLines(this.line2d.get(n - 1), this.lastOffsetIntersection, this.line2d.get(n), p2);
            if (this.fractionalHelperCenters[n - 1] == null)
            {
                // parallel helper lines, use direction for projection
                this.fractionalHelperDirections[n - 1] =
                        new Point2D.Double(p2.x - this.line2d.get(n).x, p2.y - this.line2d.get(n).y);
            }
        }
        else
        {
            // only a single segment
            this.fractionalHelperCenters[0] = intersectionOfLines(this.line2d.get(0), p1, this.line2d.get(1), p2);
        }
        if (this.fractionalHelperCenters[0] == null)
        {
            // parallel helper lines, use direction for projection
            this.fractionalHelperDirections[0] = new Point2D.Double(p1.x - this.line2d.get(0).x, p1.y - this.line2d.get(0).y);
        }

    }

    /**
     * This method is used, rather than {@code Point2d.intersectionOfLines()} because this method will return {@code null} if
     * the determinant &lt; 0.0000001, rather than determinant &eq; 0.0. The benefit of this is that intersections are not so
     * far away, that any calculations with them cause underflow or overflow issues.
     * @param line1P1 Point2d; point 1 of line 1.
     * @param line1P2 Point2d; point 2 of line 1.
     * @param line2P1 Point2d; point 1 of line 2.
     * @param line2P2 Point2d; point 2 of line 2.
     * @return Point2d; intersection of lines, or {@code null} for (nearly) parallel lines.
     */
    private Point2d intersectionOfLines(final Point2d line1P1, final Point2d line1P2, final Point2d line2P1,
            final Point2d line2P2)
    {
        double l1p1x = line1P1.x;
        double l1p1y = line1P1.y;
        double l1p2x = line1P2.x - l1p1x;
        double l1p2y = line1P2.y - l1p1y;
        double l2p1x = line2P1.x - l1p1x;
        double l2p1y = line2P1.y - l1p1y;
        double l2p2x = line2P2.x - l1p1x;
        double l2p2y = line2P2.y - l1p1y;
        double determinant = (0 - l1p2x) * (l2p1y - l2p2y) - (0 - l1p2y) * (l2p1x - l2p2x);
        if (Math.abs(determinant) < 0.0000001)
        {
            return null;
        }
        return new Point2d(l1p1x + (l1p2x * (l2p1x * l2p2y - l2p1y * l2p2x)) / determinant,
                l1p1y + (l1p2y * (l2p1x * l2p2y - l2p1y * l2p2x)) / determinant);
    }

    /**
     * Helper method for fractional projection which returns an offset line to the left of a segment at a distance of 1.
     * @param segment int; segment number
     * @return parallel line to the left of a segment at a distance of 1
     */
    private synchronized PolyLine2d unitOffsetSegment(final int segment)
    {
        return new PolyLine2d(this.line2d.get(segment), this.line2d.get(segment + 1)).offsetLine(1.0);
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
    // TODO: move to djutils?
    public synchronized Length getProjectedRadius(final double fraction) throws OtsGeometryException
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
    // TODO: move to djutils? Note, uses fractionalHelperCenters
    public synchronized Length getProjectedVertexRadius(final int index) throws OtsGeometryException
    {
        Throw.when(index < 1 || index > size() - 2, OtsGeometryException.class, "Index %d is out of bounds [1 ... size() - 2].",
                index);
        determineFractionalHelpers(null, null);
        double length1 = this.lengthIndexedLine[index] - this.lengthIndexedLine[index - 1];
        double length2 = this.lengthIndexedLine[index + 1] - this.lengthIndexedLine[index];
        int shortIndex = length1 < length2 ? index : index + 1;
        // center of shortest edge
        Point2d p1 = new Point2d(.5 * (this.line2d.get(shortIndex - 1).x + this.line2d.get(shortIndex).x),
                .5 * (this.line2d.get(shortIndex - 1).y + this.line2d.get(shortIndex).y));
        // perpendicular to shortest edge, line crossing p1
        Point2d p2 = new Point2d(p1.x + (this.line2d.get(shortIndex).y - this.line2d.get(shortIndex - 1).y),
                p1.y - (this.line2d.get(shortIndex).x - this.line2d.get(shortIndex - 1).x));
        // vertex
        Point2d p3 = this.line2d.get(index);
        // point on line that splits angle between edges at vertex 50-50
        Point2d p4 = this.fractionalHelperCenters[index];
        if (p4 == null)
        {
            // parallel helper lines
            p4 = new Point2d(p3.x + this.fractionalHelperDirections[index].x, p3.y + this.fractionalHelperDirections[index].y);
        }
        Point2d intersection = intersectionOfLines(p1, p2, p3, p4);
        if (null == intersection)
        {
            return Length.instantiateSI(Double.NaN);
        }
        // determine left or right
        double refLength = length1 < length2 ? length1 : length2;
        double radius = intersection.distance(p1);
        double i2p2 = intersection.distance(p2);
        if (radius < i2p2 && i2p2 > refLength)
        {
            // left as p1 is closer than p2 (which was placed to the right) and not on the perpendicular line
            return Length.instantiateSI(radius);
        }
        // right as not left
        return Length.instantiateSI(-radius);
    }

    /**
     * Returns the length fraction at the vertex.
     * @param index int; index of vertex [0 ... size() - 1]
     * @return double; length fraction at the vertex
     * @throws OtsGeometryException if the index is out of bounds
     */
    public double getVertexFraction(final int index) throws OtsGeometryException
    {
        Throw.when(index < 0 || index > size() - 1, OtsGeometryException.class, "Index %d is out of bounds [0 %d].", index,
                size() - 1);
        return this.lengthIndexedLine[index] / this.length.si;
    }

    /**
     * Retrieve the centroid of this OtsLine2d.
     * @return OtsPoint3d; the centroid of this OtsLine2d
     */
    public final Point2d getCentroid()
    {
        if (this.centroid == null)
        {
            this.centroid = this.line2d.getBounds().midPoint();
        }
        return this.centroid;
    }

    /**
     * Get the bounding rectangle of this OtsLine2d.
     * @return Rectangle2D; the bounding rectangle of this OtsLine2d
     */
    public final Bounds2d getEnvelope()
    {
        return this.line2d.getBounds();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Point2d getLocation()
    {
        return getCentroid();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds2d getBounds()
    {
        if (this.bounds == null)
        {
            Bounds2d envelope = getEnvelope();
            this.bounds = new Bounds2d(envelope.getDeltaX(), envelope.getDeltaY());
        }
        return this.bounds;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return this.line2d.toString();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        return this.line2d.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof OtsLine2d))
        {
            return false;
        }
        return this.line2d.equals(((OtsLine2d) obj).line2d);
    }

    /**
     * Convert the 2D projection of this OtsLine2d to something that MS-Excel can plot.
     * @return excel XY plottable output
     */
    public final String toExcel()
    {
        return this.line2d.toExcel();
    }

    /**
     * Convert the 2D projection of this OtsLine2d to Peter's plot format.
     * @return Peter's format plot output
     */
    public final String toPlot()
    {
        return this.line2d.toPlot();
    }

}

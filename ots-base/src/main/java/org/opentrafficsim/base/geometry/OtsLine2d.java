package org.opentrafficsim.base.geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.DrawRuntimeException;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * This class supports fractional projection, radius, and has location methods .
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OtsLine2d extends PolyLine2d implements Locatable, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** The cached typed length; will be calculated at time of construction. */
    private final Length length;

    // Fractional projection

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

    // Radii for curvature

    /** Radius at each vertex. */
    private Length[] vertexRadii;

    /**
     * Construct a new OtsLine2d.
     * @param points the array of points to construct this OtsLine2d from.
     */
    public OtsLine2d(final Point2d... points)
    {
        super(points);
        this.length = Length.instantiateSI(lengthAtIndex(size() - 1));
    }

    /**
     * Creates a new OtsLine2d based on 2d information.
     * @param line2d 2d information.
     */
    public OtsLine2d(final PolyLine2d line2d)
    {
        super(line2d.getPoints());
        this.length = Length.instantiateSI(lengthAtIndex(size() - 1));
    }

    /**
     * Creates a new OtsLine2d based on point iterator.
     * @param line2d point iterator.
     */
    public OtsLine2d(final Iterator<Point2d> line2d)
    {
        super(line2d);
        this.length = Length.instantiateSI(lengthAtIndex(size() - 1));
    }

    /**
     * Construct a new OtsLine2d from a List&lt;OtsPoint3d&gt;.
     * @param pointList the list of points to construct this OtsLine2d from.
     */
    public OtsLine2d(final List<Point2d> pointList)
    {
        super(pointList);
        this.length = Length.instantiateSI(lengthAtIndex(size() - 1));
    }

    /**
     * Construct parallel line.
     * @param offset offset distance from the reference line; positive is LEFT, negative is RIGHT
     * @return the line that has the specified offset from the reference line
     */
    @Override
    public final OtsLine2d offsetLine(final double offset)
    {
        return new OtsLine2d(super.offsetLine(offset));
    }

    /**
     * Create a line at linearly varying offset from this line. The offset may change linearly from its initial value at the
     * start of the reference line to its final offset value at the end of the reference line.
     * @param offsetAtStart offset at the start of the reference line (positive value is Left, negative value is Right)
     * @param offsetAtEnd offset at the end of the reference line (positive value is Left, negative value is Right)
     * @return the OtsLine2d of the line at linearly changing offset of the reference line
     */
    @Override
    public final OtsLine2d offsetLine(final double offsetAtStart, final double offsetAtEnd)
    {
        return new OtsLine2d(super.offsetLine(offsetAtStart, offsetAtEnd).getPointList());
    }

    /**
     * Create a line at linearly varying offset from this line. The offset may change linearly from its initial value at the
     * start of the reference line via a number of intermediate offsets at intermediate positions to its final offset value at
     * the end of the reference line.
     * @param relativeFractions positional fractions for which the offsets have to be generated
     * @param offsets offsets at the relative positions (positive value is Left, negative value is Right)
     * @return the Geometry of the line at linearly changing offset of the reference line
     */
    public final OtsLine2d offsetLine(final double[] relativeFractions, final double[] offsets)
    {
        return new OtsLine2d(OtsGeometryUtil.offsetLine(this, relativeFractions, offsets));
    }

    /**
     * Concatenate several OtsLine2d instances.
     * @param lines OtsLine2d... one or more OtsLine2d. The last point of the first &lt;strong&gt;must&lt;/strong&gt; match the
     *            first of the second, etc.
     * @return OtsLine2d
     */
    public static OtsLine2d concatenate(final OtsLine2d... lines)
    {
        return concatenate(0.0, lines);
    }

    /**
     * Concatenate two OtsLine2d instances. This method is separate for efficiency reasons.
     * @param toleranceSI the tolerance between the end point of a line and the first point of the next line
     * @param line1 first line
     * @param line2 second line
     * @return OtsLine2d
     */
    public static OtsLine2d concatenate(final double toleranceSI, final OtsLine2d line1, final OtsLine2d line2)
    {
        return new OtsLine2d(PolyLine2d.concatenate(toleranceSI, line1, line2));
    }

    /**
     * Concatenate several OtsLine2d instances.
     * @param toleranceSI the tolerance between the end point of a line and the first point of the next line
     * @param lines OtsLine2d... one or more OtsLine2d. The last point of the first &lt;strong&gt;must&lt;/strong&gt; match the
     *            first of the second, etc.
     * @return OtsLine2d
     */
    public static OtsLine2d concatenate(final double toleranceSI, final OtsLine2d... lines)
    {
        List<PolyLine2d> lines2d = new ArrayList<>();
        for (OtsLine2d line : lines)
        {
            lines2d.add(line);
        }
        return new OtsLine2d(PolyLine2d.concatenate(toleranceSI, lines2d.toArray(new PolyLine2d[lines.length])));
    }

    /**
     * Construct a new OtsLine2d with all points of this OtsLine2d in reverse order.
     * @return the new OtsLine2d
     */
    @Override
    public final OtsLine2d reverse()
    {
        return new OtsLine2d(super.reverse());
    }

    /**
     * Construct a new OtsLine2d covering the indicated fraction of this OtsLine2d.
     * @param start starting point, valid range [0..<cite>end</cite>)
     * @param end ending point, valid range (<cite>start</cite>..1]
     * @return the new OtsLine2d
     */
    @Override
    public OtsLine2d extractFractional(final double start, final double end)
    {
        return extract(start * this.length.si, end * this.length.si);
    }

    /**
     * Create a new OtsLine2d that covers a sub-section of this OtsLine2d.
     * @param start the length along this OtsLine2d where the sub-section starts, valid range [0..<cite>end</cite>)
     * @param end length along this OtsLine2d where the sub-section ends, valid range (<cite>start</cite>..<cite>length</cite>
     *            (length is the length of this OtsLine2d)
     * @return the selected sub-section
     */
    public final OtsLine2d extract(final Length start, final Length end)
    {
        return extract(start.si, end.si);
    }

    /**
     * Create a new OtsLine2d that covers a sub-section of this OtsLine2d.
     * @param start length along this OtsLine2d where the sub-section starts, valid range [0..<cite>end</cite>)
     * @param end length along this OtsLine2d where the sub-section ends, valid range (<cite>start</cite>..<cite>length</cite>
     *            (length is the length of this OtsLine2d)
     * @return the selected sub-section
     */
    @Override
    public final OtsLine2d extract(final double start, final double end)
    {
        return new OtsLine2d(super.extract(start, end));
    }

    /**
     * Return the length of this OtsLine2d in meters. (Assuming that the coordinates of the points constituting this line are
     * expressed in meters.)
     * @return the length of the line
     */
    public final Length getTypedLength()
    {
        return this.length;
    }

    /**
     * Get the location at a position on the line, with its direction. Position can be below 0 or more than the line length. In
     * that case, the position will be extrapolated in the direction of the line at its start or end.
     * @param position the position on the line for which to calculate the point on, before, of after the line
     * @return a directed point
     */
    public final DirectedPoint2d getLocationExtended(final Length position)
    {
        return rayToPoint(getLocationExtended(position.si));
    }

    /**
     * Get the location at a position on the line, with its direction. Position can be below 0 or more than the line length. In
     * that case, the position will be extrapolated in the direction of the line at its start or end.
     * @param positionSI the position on the line for which to calculate the point on, before, of after the line, in SI units
     * @return a directed point
     */
    public final DirectedPoint2d getLocationExtendedSI(final double positionSI)
    {
        return rayToPoint(getLocationExtended(positionSI));
    }

    /**
     * Get the location at a fraction of the line, with its direction. Fraction should be between 0.0 and 1.0.
     * @param fraction the fraction for which to calculate the point on the line
     * @return a directed point
     * @throws DrawRuntimeException when fraction less than 0.0 or more than 1.0.
     */
    public final DirectedPoint2d getLocationPointFraction(final double fraction) throws DrawRuntimeException
    {
        return rayToPoint(getLocationFraction(fraction));
    }

    /**
     * Get the location at a fraction of the line, with its direction. Fraction should be between 0.0 and 1.0.
     * @param fraction the fraction for which to calculate the point on the line
     * @param tolerance the delta from 0.0 and 1.0 that will be forgiven
     * @return a directed point
     * @throws DrawRuntimeException when fraction less than 0.0 or more than 1.0.
     */
    public final DirectedPoint2d getLocationPointFraction(final double fraction, final double tolerance)
            throws DrawRuntimeException
    {
        return rayToPoint(getLocationFraction(fraction, tolerance));
    }

    /**
     * Get the location at a fraction of the line (or outside the line), with its direction.
     * @param fraction the fraction for which to calculate the point on the line
     * @return a directed point
     */
    public final DirectedPoint2d getLocationPointFractionExtended(final double fraction)
    {
        return rayToPoint(getLocationFractionExtended(fraction));
    }

    /**
     * Get the location at a position on the line, with its direction. Position should be between 0.0 and line length.
     * @param position the position on the line for which to calculate the point on the line
     * @return a directed point
     * @throws DrawRuntimeException when position less than 0.0 or more than line length.
     */
    public final DirectedPoint2d getLocation(final Length position) throws DrawRuntimeException
    {
        return rayToPoint(getLocation(position.si));
    }

    /**
     * Get the location at a position on the line, with its direction. Position should be between 0.0 and line length.
     * @param positionSI the position on the line for which to calculate the point on the line
     * @return a directed point
     * @throws DrawRuntimeException when position less than 0.0 or more than line length.
     */
    public final DirectedPoint2d getLocationSI(final double positionSI) throws DrawRuntimeException
    {
        return rayToPoint(getLocation(positionSI));
    }

    /**
     * Returns an oriented point based on the information of a ray.
     * @param ray ray
     * @return oriented point based on the information of a ray
     */
    private DirectedPoint2d rayToPoint(final Ray2d ray)
    {
        return new DirectedPoint2d(ray.x, ray.y, ray.dirZ);
    }

    /**
     * Truncate a line at the given length (less than the length of the line, and larger than zero) and return a new line.
     * @param lengthSI the location where to truncate the line
     * @return a new OtsLine2d truncated at the exact position where line.getLength() == lengthSI
     */
    @Override
    public final OtsLine2d truncate(final double lengthSI)
    {
        return new OtsLine2d(super.truncate(lengthSI));
    }

    /**
     * Returns the fractional position along this line of the orthogonal projection of point (x, y) on this line. If the point
     * is not orthogonal to the closest line segment, the nearest point is selected.
     * @param x x-coordinate of point to project
     * @param y y-coordinate of point to project
     * @return fractional position along this line of the orthogonal projection on this line of a point
     */
    public final double projectOrthogonalSnap(final double x, final double y)
    {
        Point2d closest = closestPointOnPolyLine(new Point2d(x, y));
        return projectOrthogonalFractionalExtended(closest);
    }

    /**
     * Returns the fractional projection of a point to a line. The projection works by taking slices in space per line segment
     * as shown below. A point is always projected to the nearest segment, but not necessarily to the closest point on that
     * segment. The slices in space are analogous to a Voronoi diagram, but for the line segments instead of points. If
     * fractional projection fails, a fallback projection is returned.<br>
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
     * In the latter two cases the projection is undefined and the provided fallback is used to provide a point.
     * @param start direction in first point
     * @param end direction in last point
     * @param x x-coordinate of point to project
     * @param y y-coordinate of point to project
     * @param fallback fallback method for when fractional projection fails
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
            d[i] = Line2D.ptSegDist(get(i).x, get(i).y, get(i + 1).x, get(i + 1).y, x, y);
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
                p = intersectionOfLines(center, point, get(i), get(i + 1));
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
                p = intersectionOfLines(point, offsetPoint, get(i), get(i + 1));
            }
            double segLength = get(i).distance(get(i + 1)) + FRAC_PROJ_PRECISION;
            if (p == null || get(i).distance(p) > segLength || get(i + 1).distance(p) > segLength)
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
                dx = p.x - get(i).x;
                dy = p.y - get(i).y;
                double dFrac = Math.hypot(dx, dy);
                // fraction to point on segment
                minDistance = distance;
                minSegmentFraction = dFrac / (lengthAtIndex(i + 1) - lengthAtIndex(i));
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
            return fallback.getFraction(this, x, y);
        }

        double segLen = lengthAtIndex(minSegment + 1) - lengthAtIndex(minSegment);
        return (lengthAtIndex(minSegment) + segLen * minSegmentFraction) / this.length.si;

    }

    /**
     * Fallback method for when fractional projection fails as the point is beyond the line or from numerical limitations.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
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
                return line.projectOrthogonalSnap(x, y);
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
         * @param line line
         * @param x x coordinate of point
         * @param y y coordinate of point
         * @return fraction for when fractional projection fails
         */
        abstract double getFraction(OtsLine2d line, double x, double y);

    }

    /**
     * Determines all helpers (points and/or directions) for fractional projection and stores fixed information in properties
     * while returning the first and last center points (.
     * @param start direction in first point
     * @param end direction in last point
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
                    this.fractionalHelperCenters[i] = intersectionOfLines(get(i), parStartPoint, get(i + 1), parEndPoint);
                    if (this.fractionalHelperCenters[i] == null)
                    {
                        // parallel helper lines, parallel segments or /\/ cause parallel helper lines, use direction
                        this.fractionalHelperDirections[i] =
                                new Point2D.Double(parStartPoint.x - get(i).x, parStartPoint.y - get(i).y);
                    }
                    parStartPoint = parEndPoint;
                }
                // remember the intersection of the last two unit offset segments
                this.lastOffsetIntersection = parStartPoint;
            }
        }

        // use directions at start and end to get unit offset points to the left at a distance of 1
        double ang = (start == null ? Math.atan2(get(1).y - get(0).y, get(1).x - get(0).x) : start.si) + Math.PI / 2;
        Point2d p1 = new Point2d(get(0).x + Math.cos(ang), get(0).y + Math.sin(ang));
        ang = (end == null ? Math.atan2(get(n).y - get(n - 1).y, get(n).x - get(n - 1).x) : end.si) + Math.PI / 2;
        Point2d p2 = new Point2d(get(n).x + Math.cos(ang), get(n).y + Math.sin(ang));

        // calculate first and last center (i.e. intersection of unit offset segments), which depend on inputs 'start' and 'end'
        if (size() > 2)
        {
            this.fractionalHelperCenters[0] = intersectionOfLines(get(0), p1, get(1), this.firstOffsetIntersection);
            this.fractionalHelperCenters[n - 1] = intersectionOfLines(get(n - 1), this.lastOffsetIntersection, get(n), p2);
            if (this.fractionalHelperCenters[n - 1] == null)
            {
                // parallel helper lines, use direction for projection
                this.fractionalHelperDirections[n - 1] = new Point2D.Double(p2.x - get(n).x, p2.y - get(n).y);
            }
        }
        else
        {
            // only a single segment
            this.fractionalHelperCenters[0] = intersectionOfLines(get(0), p1, get(1), p2);
        }
        if (this.fractionalHelperCenters[0] == null)
        {
            // parallel helper lines, use direction for projection
            this.fractionalHelperDirections[0] = new Point2D.Double(p1.x - get(0).x, p1.y - get(0).y);
        }

    }

    /**
     * This method is used, rather than {@code Point2d.intersectionOfLines()} because this method will return {@code null} if
     * the determinant &lt; 0.0000001, rather than determinant &eq; 0.0. The benefit of this is that intersections are not so
     * far away, that any calculations with them cause underflow or overflow issues.
     * @param line1P1 point 1 of line 1.
     * @param line1P2 point 2 of line 1.
     * @param line2P1 point 1 of line 2.
     * @param line2P2 point 2 of line 2.
     * @return intersection of lines, or {@code null} for (nearly) parallel lines.
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
     * @param segment segment number
     * @return parallel line to the left of a segment at a distance of 1
     */
    private synchronized PolyLine2d unitOffsetSegment(final int segment)
    {
        return new PolyLine2d(get(segment), get(segment + 1)).offsetLine(1.0);
    }

    /**
     * Returns the projected directional radius of the line at a given fraction. Negative values reflect right-hand curvature in
     * the design-line direction. The radius is taken as the minimum of the radii at the vertices before and after the given
     * fraction. The radius at a vertex is calculated as the radius of a circle that is equidistant from both edges connected to
     * the vertex. The circle center is on a line perpendicular to the shortest edge, crossing through the middle of the
     * shortest edge. This method ignores Z components.
     * @param fraction fraction along the line, between 0.0 and 1.0 (both inclusive)
     * @return radius; the local radius; or si field set to Double.NaN if line is totally straight
     * @throws IllegalArgumentException fraction out of bounds
     */
    public synchronized Length getProjectedRadius(final double fraction) throws IllegalArgumentException
    {
        Throw.when(fraction < 0.0 || fraction > 1.0, IllegalArgumentException.class,
                "Fraction %f is out of bounds [0.0 ... 1.0]", fraction);
        if (this.vertexRadii == null)
        {
            this.vertexRadii = new Length[size() - 1];
        }
        int index = find(fraction * getLength());
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
     * @param index index of the vertex in range [1 ... size() - 2]
     * @return radius at the vertex
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    public synchronized Length getProjectedVertexRadius(final int index) throws IndexOutOfBoundsException
    {
        Throw.when(index < 1 || index > size() - 2, IndexOutOfBoundsException.class,
                "Index %d is out of bounds [1 ... size() - 2].", index);
        determineFractionalHelpers(null, null);
        double length1 = lengthAtIndex(index) - lengthAtIndex(index - 1);
        double length2 = lengthAtIndex(index + 1) - lengthAtIndex(index);
        int shortIndex = length1 < length2 ? index : index + 1;
        // center of shortest edge
        Point2d p1 =
                new Point2d(.5 * (get(shortIndex - 1).x + get(shortIndex).x), .5 * (get(shortIndex - 1).y + get(shortIndex).y));
        // perpendicular to shortest edge, line crossing p1
        Point2d p2 = new Point2d(p1.x + (get(shortIndex).y - get(shortIndex - 1).y),
                p1.y - (get(shortIndex).x - get(shortIndex - 1).x));
        // vertex
        Point2d p3 = get(index);
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
     * @param index index of vertex [0 ... size() - 1]
     * @return length fraction at the vertex
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    public double getVertexFraction(final int index) throws IndexOutOfBoundsException
    {
        Throw.when(index < 0 || index > size() - 1, IndexOutOfBoundsException.class, "Index %d is out of bounds [0 %d].", index,
                size() - 1);
        return lengthAtIndex(index) / this.length.si;
    }

    /**
     * Retrieve the centroid of this OtsLine2d.
     * @return the centroid of this OtsLine2d
     */
    public final Point2d getCentroid()
    {
        return getBounds().midPoint();
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Point2d getLocation()
    {
        return getCentroid();
    }

}

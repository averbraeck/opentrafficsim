package org.opentrafficsim.base.geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.geometry.FractionalProjectionHelper.FractionalFallback;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * This class supports fractional projection, radius, and has locatable methods.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OtsLine2d extends PolyLine2d implements Locatable
{

    /** Fractional projection helper. */
    private final FractionalProjectionHelper fracHelper;

    /** Radius calculator. */
    private final RadiusCalculator2d radiusCalc;

    /**
     * Constructor from points.
     * @param points array of points
     */
    public OtsLine2d(final Point2d... points)
    {
        super(0.0, points);
        this.fracHelper = new FractionalProjectionHelper(this);
        this.radiusCalc = new RadiusCalculator2d(this, this.fracHelper);
    }

    /**
     * Constructor based on 2d line.
     * @param line2d 2d line
     */
    public OtsLine2d(final PolyLine2d line2d)
    {
        super(0.0, line2d.iterator());
        this.fracHelper = new FractionalProjectionHelper(this);
        this.radiusCalc = new RadiusCalculator2d(this, this.fracHelper);
    }

    /**
     * Constructor based on point iterator.
     * @param line2d point iterator
     */
    public OtsLine2d(final Iterator<Point2d> line2d)
    {
        super(0.0, line2d);
        this.fracHelper = new FractionalProjectionHelper(this);
        this.radiusCalc = new RadiusCalculator2d(this, this.fracHelper);
    }

    /**
     * Constructor based on a {@link List}{@code <}{@link Point2d}{@code >}.
     * @param pointList list of points
     */
    public OtsLine2d(final List<Point2d> pointList)
    {
        super(0.0, pointList);
        this.fracHelper = new FractionalProjectionHelper(this);
        this.radiusCalc = new RadiusCalculator2d(this, this.fracHelper);
    }

    /**
     * Construct parallel line.
     * @param offset offset distance from the reference line; positive is LEFT, negative is RIGHT
     * @return the line that has the specified offset from this reference line
     */
    @Override
    public OtsLine2d offsetLine(final double offset)
    {
        return new OtsLine2d(super.offsetLine(offset));
    }

    /**
     * Create a line at linearly varying offset from this line. The offset may change linearly from its initial value at the
     * start of the reference line to its final offset value at the end of the reference line.
     * @param offsetAtStart offset at the start of the reference line (positive value is Left, negative value is Right)
     * @param offsetAtEnd offset at the end of the reference line (positive value is Left, negative value is Right)
     * @return line with linear offset
     */
    @Override
    public OtsLine2d offsetLine(final double offsetAtStart, final double offsetAtEnd)
    {
        return new OtsLine2d(super.offsetLine(offsetAtStart, offsetAtEnd).getPointList());
    }

    /**
     * Create a line at linearly varying offset from this line. The offset may change linearly from its initial value at the
     * start of the reference line via a number of intermediate offsets at intermediate positions to its final offset value at
     * the end of the reference line.
     * @param relativeFractions positional fractions for which the offsets have to be generated
     * @param offsets offsets at the relative positions (positive value is Left, negative value is Right)
     * @return line with profiled offset
     */
    public OtsLine2d offsetLine(final double[] relativeFractions, final double[] offsets)
    {
        return new OtsLine2d(OtsGeometryUtil.offsetLine(this, relativeFractions, offsets));
    }

    /**
     * Concatenate several OtsLine2d instances.
     * @param lines OtsLine2d... one or more OtsLine2d. The last point of the first &lt;strong&gt;must&lt;/strong&gt; match the
     *            first of the second, etc.
     * @return concatenated line
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
     * @return concatenated line
     */
    public static OtsLine2d concatenate(final double toleranceSI, final OtsLine2d line1, final OtsLine2d line2)
    {
        return new OtsLine2d(PolyLine2d.concatenate(toleranceSI, line1, line2));
    }

    /**
     * Concatenate several OtsLine2d instances.
     * @param toleranceSI the tolerance between the end point of a line and the first point of the next line
     * @param lines OtsLine2d... one or more OtsLine2d; the last point of the first <b>must</b> match the first of the second,
     *            etc.
     * @return concatenated line
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
     * Returns a reversed instance of this line.
     * @return reversed line
     */
    @Override
    public OtsLine2d reverse()
    {
        return new OtsLine2d(super.reverse());
    }

    /**
     * Create a new line covering the indicated fraction of this line.
     * @param start starting point, valid range [0..<i>end</i>)
     * @param end ending point, valid range (<i>start</i>..1]
     * @return extracted line
     */
    @Override
    public OtsLine2d extractFractional(final double start, final double end)
    {
        return extract(start * getLength(), end * getLength());
    }

    /**
     * Create a new line that covers a sub-section of this line.
     * @param start the length along this OtsLine2d where the sub-section starts, valid range [0..<i>end</i>)
     * @param end length along this OtsLine2d where the sub-section ends, valid range (<i>start</i>..<i>length</i> (length is
     *            the length of this line)
     * @return extracted line
     */
    public OtsLine2d extract(final Length start, final Length end)
    {
        return extract(start.si, end.si);
    }

    /**
     * Create a new line that covers a sub-section of this line.
     * @param start length along this line where the sub-section starts, valid range [0..<i>end</i>)
     * @param end length along this line where the sub-section ends, valid range (<i>start</i>..<i>length</i> (length is the
     *            length of this line)
     * @return extracted line
     */
    @Override
    public OtsLine2d extract(final double start, final double end)
    {
        return new OtsLine2d(super.extract(start, end));
    }

    /**
     * Return the length of this line.
     * @return length of the line
     */
    public Length getTypedLength()
    {
        return Length.ofSI(super.getLength());
    }

    /**
     * Get the location at a position on the line, with its direction. Position can be below 0 or more than the line length. In
     * that case, the position will be extrapolated in the direction of the line at its start or end.
     * @param position the position on the line for which to calculate the point on, before, of after the line
     * @return a directed point
     */
    public DirectedPoint2d getLocationExtended(final Length position)
    {
        return getLocationExtended(position.si);
    }

    /**
     * Get the location at a position on the line, with its direction. Position should be in [0..<i>length</i>].
     * @param position the position on the line for which to calculate the point on the line
     * @return a directed point
     */
    public DirectedPoint2d getLocation(final Length position)
    {
        return getLocation(position.si);
    }

    /**
     * Truncate line at the given length in range (0..<i>length</i>).
     * @param lengthSI the location where to truncate the line
     * @return truncated line
     */
    @Override
    public OtsLine2d truncate(final double lengthSI)
    {
        return new OtsLine2d(super.truncate(lengthSI));
    }

    /**
     * Orthogonally project a point onto this polyline. If the perpendicular foot on the closest segment falls outside that
     * segment, this method snaps to the nearest vertex of that segment. The result is returned as a fraction along the entire
     * polyline. The returned fraction is clamped to [0..1].
     * @param x x-coordinate of the point to project
     * @param y y-coordinate of the point to project
     * @return fraction along the line in [0..1]
     */
    public double projectOrthogonalSnapAt(final double x, final double y)
    {
        return projectOrthogonalSnapAt(x, y, true);
    }

    /**
     * Orthogonally project a point onto this polyline. If the perpendicular foot on the closest segment falls outside that
     * segment, this method snaps to the nearest vertex of that segment. The result is returned as a fraction along the entire
     * polyline.
     * <p>
     * If {@code clampToDomain} is {@code true}, the returned fraction is clamped to [0..1]. If {@code false}, the fraction may
     * be negative (before the start of the line) or larger than 1 (beyond the end of the line) when the closest point is the
     * first or last vertex, respectively.
     * @param x x-coordinate of the point to project
     * @param y y-coordinate of the point to project
     * @param clampToDomain whether to clamp the resulting fraction to [0..1]
     * @return fraction along the line; in [0..1] when {@code clampToDomain} is true; otherwise possibly &lt;0 or &gt;1
     */
    public double projectOrthogonalSnapAt(final double x, final double y, final boolean clampToDomain)
    {
        final int nPoints = size();
        if (nPoints < 2)
        {
            return Double.NaN; // no segments to project on
        }

        // Track the best (closest) candidate across all segments.
        double bestD2 = Double.POSITIVE_INFINITY;
        double bestFraction = Double.NaN;

        // Useful constants
        final double totalLength = lengthAtIndex(nPoints - 1);
        final Point2d first = get(0);
        final Point2d last = get(nPoints - 1);

        // Loop over segments and compute the closest point (orthogonal if interior, otherwise snap to an end-point)
        for (int i = 0; i < nPoints - 1; i++)
        {
            final Point2d a = get(i);
            final Point2d b = get(i + 1);

            final double abx = b.x - a.x;
            final double aby = b.y - a.y;
            final double ab2 = abx * abx + aby * aby;
            if (ab2 == 0.0)
            {
                // Degenerate segment; skip
                continue;
            }

            final double apx = x - a.x;
            final double apy = y - a.y;

            // Un-clamped parameter along the infinite line through segment [a,b]
            final double t = (apx * abx + apy * aby) / ab2;

            // Compute the "nearest point on the segment" by clamping t to [0, 1]
            final double tClamped = Math.max(0.0, Math.min(1.0, t));
            final double px = a.x + tClamped * abx;
            final double py = a.y + tClamped * aby;

            // Distance squared to the candidate on this segment
            final double dx = x - px;
            final double dy = y - py;
            final double d2 = dx * dx + dy * dy;

            if (d2 < bestD2)
            {
                bestD2 = d2;

                // Convert this segment-local candidate to a fraction along the entire polyline.
                if (t >= 0.0 && t <= 1.0)
                {
                    // Orthogonal foot lies within the segment.
                    final double segStart = lengthAtIndex(i);
                    final double segEnd = lengthAtIndex(i + 1);
                    final double absS = segStart + t * (segEnd - segStart);
                    bestFraction = absS / totalLength;
                }
                else
                {
                    // Orthogonal foot outside the segment: snap to the nearest endpoint of this segment.
                    // For t < 0 -> endpoint a; for t > 1 -> endpoint b.
                    if (t < 0.0)
                    {
                        if (clampToDomain)
                        {
                            bestFraction = lengthAtIndex(i) / totalLength; // fraction at vertex a
                        }
                        else
                        {
                            // Extended: if this is the first vertex, return a negative fraction
                            if (i == 0)
                            {
                                final double dStart = Math.hypot(x - first.x, y - first.y);
                                bestFraction = (0.0 - dStart) / totalLength;
                            }
                            else
                            {
                                // For internal vertices, still snap to the vertex fraction in-domain
                                bestFraction = lengthAtIndex(i) / totalLength;
                            }
                        }
                    }
                    else // t > 1.0
                    {
                        if (clampToDomain)
                        {
                            bestFraction = lengthAtIndex(i + 1) / totalLength; // fraction at vertex b
                        }
                        else
                        {
                            // Extended: if this is the last vertex, return a fraction beyond 1
                            if (i + 1 == nPoints - 1)
                            {
                                final double dEnd = Math.hypot(x - last.x, y - last.y);
                                bestFraction = (totalLength + dEnd) / totalLength;
                            }
                            else
                            {
                                // For internal vertices, still snap to the vertex fraction in-domain
                                bestFraction = lengthAtIndex(i + 1) / totalLength;
                            }
                        }
                    }
                }
            }
        }

        return bestFraction;
    }

    /**
     * Returns the fractional projection of a point to a line. The projection works by taking slices in space per line segment
     * as shown below. A point is always projected to the nearest segment, but not necessarily to the closest point on that
     * segment. The slices in space are analogous to a Voronoi diagram, but for the line segments instead of points. If
     * fractional projection fails, a fallback projection is returned.
     * <p>
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
    public double projectFractionalAt(final Direction start, final Direction end, final double x, final double y,
            final FractionalFallback fallback)
    {
        return this.fracHelper.projectFractionalAt(start, end, x, y, fallback);
    }

    /**
     * Returns the projected directional radius of the line at a given fraction. Negative values reflect right-hand curvature in
     * the design-line direction. The radius is taken as the minimum of the radii at the vertices before and after the given
     * fraction. The radius at a vertex is calculated as the radius of a circle that is equidistant from both edges connected to
     * the vertex. The circle center is on a line perpendicular to the shortest edge, crossing through the middle of the
     * shortest edge. This method ignores Z components.
     * @param fraction fraction along the line, between 0.0 and 1.0 (both inclusive)
     * @return radius; the local radius; empty if there is no radius as two segments have the same direction
     * @throws IllegalArgumentException fraction out of bounds
     */
    public Optional<Length> radiusAtFraction(final double fraction) throws IllegalArgumentException
    {
        return this.radiusCalc.radiusAtFraction(fraction);
    }

    /**
     * Calculates the directional radius at a vertex. Negative values reflect right-hand curvature in the design-line direction.
     * The radius at a vertex is calculated as the radius of a circle that is equidistant from both edges connected to the
     * vertex. The circle center is on a line perpendicular to the shortest edge, crossing through the middle of the shortest
     * edge. This function ignores Z components.
     * @param index index of the vertex in range [1 ... size() - 2]
     * @return radius at the vertex, empty if there is no radius as two segments have the same direction
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    public Optional<Length> radiusAtVertex(final int index) throws IndexOutOfBoundsException
    {
        return this.radiusCalc.radiusAtVertex(index);
    }

    /**
     * Returns the length fraction at the vertex.
     * @param index index of vertex [0 ... size() - 1]
     * @return length fraction at the vertex
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    public double vertexFraction(final int index) throws IndexOutOfBoundsException
    {
        Throw.when(index < 0 || index > size() - 1, IndexOutOfBoundsException.class, "Index %d is out of bounds [0 %d].", index,
                size() - 1);
        return lengthAtIndex(index) / getLength();
    }

    @Override
    public Bounds<?, ?> getRelativeBounds()
    {
        return OtsShape.toRelativeTransform(getLocation()).transform(getAbsoluteBounds());
    }

    @Override
    public Point2d getLocation()
    {
        return getAbsoluteBounds().midPoint();
    }

}

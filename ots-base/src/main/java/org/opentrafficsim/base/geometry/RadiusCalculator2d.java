package org.opentrafficsim.base.geometry;

import java.util.Optional;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

/**
 * Computes signed curvature radii for an {@link OtsLine2d} using the fractional projection helpers. Positive radius means
 * left-hand curvature in the design-line direction.
 * <p>
 * Rules:
 * </p>
 * <ul>
 * <li>Radius at a vertex is the distance from the midpoint of the <em>shorter</em> adjacent edge along the perpendicular line
 * to the intersection with the local angle-splitting ray (from helper).</li>
 * <li>Projected radius at a fraction equals the minimum (by absolute value) of the radii at the adjacent vertices.</li>
 * <li>If the polyline is straight throughout, returns NaN.</li>
 * </ul>
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class RadiusCalculator2d
{

    /** Small epsilon used in intersection guard only. */
    private static final double INTERSECTION_EPS = 1e-7;

    /** The line for which to compute radii. */
    private final OtsLine2d line;

    /** Fractional projection helper; used with (null, null) directions for curvature. */
    private final FractionalProjectionHelper fracHelper;

    /**
     * Lazily computed per-vertex radii; valid for indices [1 .. size() - 2]. Other indices are not used.
     */
    private Length[] vertexRadii;

    /**
     * Construct a radius calculator for a line.
     * @param line the line
     * @param fracHelper the fractional projection helper; if null, a new one is created
     */
    public RadiusCalculator2d(final OtsLine2d line, final FractionalProjectionHelper fracHelper)
    {
        this.line = line;
        this.fracHelper = fracHelper != null ? fracHelper : new FractionalProjectionHelper(line);
        this.vertexRadii = new Length[Math.max(0, line.size())];
    }

    /**
     * Returns the projected directional radius at a fraction in [0, 1]. Uses the minimum-by-absolute-value of the two adjacent
     * vertex radii. If no curvature exists or degenerate, returns NaN.
     * @param fraction fraction along the line, in [0, 1]
     * @return signed radius at the fraction, empty if not defined
     * @throws IllegalArgumentException if fraction out of bounds
     */
    public synchronized Optional<Length> radiusAtFraction(final double fraction) throws IllegalArgumentException
    {
        Throw.when(fraction < 0.0 || fraction > 1.0, IllegalArgumentException.class,
                "Fraction %s is out of bounds [0.0 .. 1.0]", fraction);

        final int n = this.line.size() - 1; // number of segments
        if (n < 2)
        {
            // fewer than two segments -> no vertex with curvature
            return Optional.empty();
        }

        final double totalLen = this.line.lengthAtIndex(this.line.size() - 1);
        final double absS = fraction * totalLen;

        final int segIndex = segmentIndexAt(absS);
        // Ensure adjacent vertex radii are computed where applicable
        if (segIndex > 0 && this.vertexRadii[segIndex] == null)
        {
            this.vertexRadii[segIndex] = computeProjectedVertexRadius(segIndex);
        }
        if (segIndex < n - 1 && this.vertexRadii[segIndex + 1] == null)
        {
            this.vertexRadii[segIndex + 1] = computeProjectedVertexRadius(segIndex + 1);
        }

        if (segIndex == 0)
        {
            // at start, only vertex 1 exists as internal
            return Optional.ofNullable(n >= 2 ? this.vertexRadii[1] : null);
        }
        if (segIndex == n - 1)
        {
            // at end, only vertex n-1 exists as internal
            return Optional.of(this.vertexRadii[n - 1]);
        }

        final Length left = this.vertexRadii[segIndex];
        final Length right = this.vertexRadii[segIndex + 1];
        if (left == null && right == null)
        {
            return Optional.empty();
        }
        else if (left == null)
        {
            return Optional.of(right);
        }
        else if (right == null)
        {
            return Optional.of(left);
        }
        return Optional.of(Math.abs(left.si) <= Math.abs(right.si) ? left : right);
    }

    /**
     * Returns the directional radius at an internal vertex (index in [1 .. size() - 2]). If the geometry is degenerate or
     * helper cannot construct a valid intersection, returns NaN.
     * @param index vertex index
     * @return signed radius at the vertex (NaN if undefined)
     * @throws IndexOutOfBoundsException if index not in [1 .. size() - 2]
     */
    public synchronized Optional<Length> radiusAtVertex(final int index) throws IndexOutOfBoundsException
    {
        Throw.when(index < 1 || index > this.line.size() - 2, IndexOutOfBoundsException.class,
                "Index %s is out of bounds [1 .. %s]", index, this.line.size() - 2);
        if (this.vertexRadii[index] == null)
        {
            this.vertexRadii[index] = computeProjectedVertexRadius(index);
        }
        return Optional.ofNullable(this.vertexRadii[index]);
    }

    /**
     * Computes the vertex radius.
     * @param index index
     * @return computed radius, {@code null} if there is no radius
     */
    private Length computeProjectedVertexRadius(final int index)
    {

        // Determine which adjacent edge is shorter
        final double lenPrev = this.line.lengthAtIndex(index) - this.line.lengthAtIndex(index - 1);
        final double lenNext = this.line.lengthAtIndex(index + 1) - this.line.lengthAtIndex(index);
        final int shortIndex = lenPrev <= lenNext ? index : index + 1;

        // Midpoint of the shorter edge
        final Point2d aS = this.line.get(shortIndex - 1);
        final Point2d bS = this.line.get(shortIndex);
        final Point2d mid = new Point2d(0.5 * (aS.x + bS.x), 0.5 * (aS.y + bS.y));

        // Perpendicular line through the midpoint: rotate edge vector (ex, ey) by -90 deg -> (ey, -ex). i.e. right
        final double ex = bS.x - aS.x;
        final double ey = bS.y - aS.y;
        final Point2d midPerpEnd = new Point2d(mid.x + ey, mid.y - ex);

        // Angle-splitting line from the helper at the vertex (null, null directions)
        final Point2d vertex = this.line.get(index);
        final FractionalProjectionHelper.Helper h = this.fracHelper.helperAtVertex(index, null, null);

        final Point2d rayEnd;
        if (h.hasCenter())
        {
            rayEnd = new Point2d(h.cx(), h.cy());
        }
        else
        {
            // Use direction as provided by the helper
            rayEnd = new Point2d(vertex.x + h.dx(), vertex.y + h.dy());
        }

        // Intersection of the two infinite lines
        Point2d inter = intersectionOrNull(mid, midPerpEnd, vertex, rayEnd);
        if (inter == null)
        {
            return null;
        }

        final double radius = inter.distance(mid);
        final double i2p2 = inter.distance(midPerpEnd);
        final double refLen = Math.min(lenPrev, lenNext);
        final boolean isLeft = (radius < i2p2 && i2p2 > refLen);
        return Length.ofSI(isLeft ? radius : -radius);
    }

    /**
     * Intersection of infinite lines (p1->p2) and (p3->p4). Returns null if near-parallel.
     * @param p1 first point of first line
     * @param p2 second point of first line
     * @param p3 first point of second line
     * @param p4 second point of second line
     * @return intersection
     */
    // TODO: can be replaced with djutils version with eps once it is published in djutils
    private static Point2d intersectionOrNull(final Point2d p1, final Point2d p2, final Point2d p3, final Point2d p4)
    {
        final double x1 = p1.x, y1 = p1.y;
        final double x2 = p2.x, y2 = p2.y;
        final double x3 = p3.x, y3 = p3.y;
        final double x4 = p4.x, y4 = p4.y;

        final double dx1 = x2 - x1, dy1 = y2 - y1;
        final double dx2 = x4 - x3, dy2 = y4 - y3;

        final double denom = dx1 * dy2 - dy1 * dx2;
        if (Math.abs(denom) < INTERSECTION_EPS)
        {
            return null; // near-parallel
        }
        final double t = ((x3 - x1) * dy2 - (y3 - y1) * dx2) / denom;
        return new Point2d(x1 + t * dx1, y1 + t * dy1);
    }

    /**
     * Find containing segment index for absolute s in [0 .. totalLen].
     * @param absS segment at index
     * @return segment at s
     */
    private int segmentIndexAt(final double absS)
    {
        final int n = this.line.size();
        for (int i = 0; i < n - 1; i++)
        {
            final double s1 = this.line.lengthAtIndex(i + 1);
            if (absS <= s1)
            {
                return i;
            }
        }
        return n - 2; // guard at the end
    }
}

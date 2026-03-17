package org.opentrafficsim.base.geometry;

import java.awt.geom.Line2D;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.math.AngleUtil;

/**
 * Fractional projection helper for {@link OtsLine2d}.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @see OtsLine2d#projectFractionalAt
 */
public final class FractionalProjectionHelper
{

    /** Numerical precision for fractional projection comparisons. */
    private static final double FRAC_PROJ_PRECISION = 2e-5;

    /** Epsilon to consider two lines near-parallel in intersection tests. */
    private static final double INTERSECTION_EPS = 1e-7;

    /** Owning line (used to avoid duplicating points and cumulative lengths). */
    private final OtsLine2d line;

    /** Number of segments. */
    private final int n;

    /** Whether fixed helper 1..n-2 is a center, other wise a direction. */
    private final boolean[] isCenter; // length n; interior entries set, edges ignored

    /** Helper center x coordinate. */
    private final double[] centerX; // length n; only valid when isCenter[i] == true

    /** Helper center y coordinate. */
    private final double[] centerY; // length n

    /** Helper direction x component. */
    private final double[] dirX; // length n; only valid when isCenter[i] == false

    /** Helper direction y component. */
    private final double[] dirY; // length n

    /** Intersection of first two unit-offset segments (start direction-independent). */
    private Point2d firstOffsetIntersection;

    /** Intersection of last two unit-offset segments (end direction-independent). */
    private Point2d lastOffsetIntersection;

    /**
     * Cached helpers for {@code null} start and end direction. Often used by {@link OtsLine2d#radiusAtFraction} and
     * {@link OtsLine2d#radiusAtVertex(int)}.
     */
    private EdgeHelpers edgeNullNull; // cached helpers for (null,null)

    /** Last none-both-{@code null} directions. */
    private DirectionKey lastKey; // last used (may include null on one side)

    /** Helpers of last none-both-{@code null} directions. */
    private EdgeHelpers edgeLast; // cached helpers for lastKey

    /**
     * Constructor.
     * @param line line
     */
    FractionalProjectionHelper(final OtsLine2d line)
    {
        this.line = line;
        this.n = this.line.size() - 1;

        this.isCenter = new boolean[this.n];
        this.centerX = new double[this.n];
        this.centerY = new double[this.n];
        this.dirX = new double[this.n];
        this.dirY = new double[this.n];

        // Pre-compute interior (none-edge) helpers and the two direction-independent offset intersections
        precomputeFixedHelpers();
    }

    /**
     * Pre-computes all fixed helpers.
     */
    private void precomputeFixedHelpers()
    {
        if (this.n < 2)
        {
            // No interior segments; nothing to pre-compute
            return;
        }

        // First two unit-offset segments (direction independent)
        PolyLine2d prevOfs = unitOffsetSegment(0);
        PolyLine2d nextOfs = unitOffsetSegment(1);

        Point2d parStart = intersectionOrFallbackMid(prevOfs.get(0), prevOfs.get(1), nextOfs.get(0), nextOfs.get(1));
        this.firstOffsetIntersection = parStart;

        // Special case: exactly two segments -> lastOffsetIntersection equals parStart
        if (this.n == 2)
        {
            this.lastOffsetIntersection = parStart;
            return; // no interior helpers to compute
        }

        // Build for interior segments i = 1 .. n-2
        for (int i = 1; i <= this.n - 2; i++)
        {
            prevOfs = nextOfs;
            if (i + 1 <= this.n - 1)
            {
                nextOfs = unitOffsetSegment(i + 1);
            }
            final Point2d parEnd = intersectionOrFallbackMid(prevOfs.get(0), prevOfs.get(1), nextOfs.get(0), nextOfs.get(1));

            // Intersection of helper lines: (vertex i -> parStart) and (vertex i+1 -> parEnd)
            final Point2d c = intersectionOrNull(this.line.get(i), parStart, this.line.get(i + 1), parEnd);
            if (c != null)
            {
                this.isCenter[i] = true;
                this.centerX[i] = c.x;
                this.centerY[i] = c.y;
            }
            else
            {
                this.isCenter[i] = false;
                this.dirX[i] = parStart.x - this.line.get(i).x;
                this.dirY[i] = parStart.y - this.line.get(i).y;
            }

            parStart = parEnd;
            if (i == this.n - 2)
            {
                this.lastOffsetIntersection = parStart;
            }
        }
    }

    /**
     * Unit-offset line to the LEFT of segment i (distance 1.0).
     * @param i segment
     * @return offset line
     */
    private PolyLine2d unitOffsetSegment(final int i)
    {
        return new PolyLine2d(this.line.get(i), this.line.get(i + 1)).offsetLine(1.0);
    }

    /**
     * Fractionally project a point on the polyline using the fractional helper logic. Falls back via the given strategy when
     * fractional projection is not applicable.
     * @param start direction in first point
     * @param end direction in last point
     * @param x x-coordinate of point to project
     * @param y y-coordinate of point to project
     * @param fallback fallback method for when fractional projection fails
     * @return fractional position along this line of the fractional projection on that line of a point
     * @see OtsLine2d#projectFractionalAt
     */
    public synchronized double projectFractionalAt(final Direction start, final Direction end, final double x, final double y,
            final FractionalFallback fallback)
    {
        Throw.whenNull(fallback, "fallback");

        // Determine edge helpers for these directions (2-slot cache: (null,null) + lastKey).
        final EdgeHelpers edges = getEdgeHelpers(start, end);

        // Compute distances to segments; consider only those near the minimum distance.
        double minD = Double.POSITIVE_INFINITY;
        final double[] segDist = new double[this.n];
        for (int i = 0; i < this.n; i++)
        {
            Point2d a = this.line.get(i);
            Point2d b = this.line.get(i + 1);
            segDist[i] = Line2D.ptSegDist(a.x, a.y, b.x, b.y, x, y);
            if (segDist[i] < minD)
            {
                minD = segDist[i];
            }
        }

        double bestDistance = Double.POSITIVE_INFINITY;
        int bestSeg = -1;
        double bestSegFrac = 0.0;

        final Point2d ext = new Point2d(x, y);

        for (int i = 0; i < this.n; i++)
        {
            if (segDist[i] > minD + FRAC_PROJ_PRECISION)
            {
                continue;
            }
            final Helper h = helperForSegment(i, edges);
            final Point2d p = intersectProjection(i, h, ext);
            if (p == null)
            {
                continue;
            }

            // Ensure intersection lies on segment (within tolerance)
            Point2d a = this.line.get(i);
            Point2d b = this.line.get(i + 1);
            final double segLen = a.distance(b) + FRAC_PROJ_PRECISION;
            if (p.distance(a) > segLen || p.distance(b) > segLen)
            {
                continue;
            }

            // Prefer the nearest intersection to the external point
            final double dist = p.distance(ext);
            if (dist < bestDistance)
            {
                bestDistance = dist;
                // Compute fraction within segment
                final double segActual = a.distance(b);
                final double along = a.distance(p);
                bestSegFrac = Math.min(1.0, Math.max(0.0, segActual > 0.0 ? (along / segActual) : 0.0));
                bestSeg = i;
            }
        }

        if (bestSeg < 0)
        {
            // Fractional projection not applicable; fallback
            return fallback.getFraction(this, x, y);
        }

        // Convert segment-local fraction to global fraction using line.lengthAtIndex
        final double segStartLen = this.line.lengthAtIndex(bestSeg);
        final double segEndLen = this.line.lengthAtIndex(bestSeg + 1);
        final double abs = segStartLen + bestSegFrac * (segEndLen - segStartLen);
        final double total = this.line.lengthAtIndex(this.line.size() - 1);
        return abs / total;
    }

    /**
     * Get the helper (center or direction) at a vertex index using (start,end) dependent edges. Useful for curvature / radius
     * logic. Supply ({@code null}, {@code null}) if that is the desired configuration.
     * @param vertexIndex 0 .. size-1
     * @param start direction at start (can be {@code null})
     * @param end direction at end (can be {@code null})
     * @return helper
     */
    public synchronized Helper helperAtVertex(final int vertexIndex, final Direction start, final Direction end)
    {
        Throw.when(vertexIndex < 0 || vertexIndex > this.line.size() - 1, IndexOutOfBoundsException.class,
                "vertexIndex %s out of bounds [0..%s]", vertexIndex, this.line.size() - 1);
        if (vertexIndex < 0 || vertexIndex > this.line.size() - 1)
        {
            throw new IndexOutOfBoundsException("vertexIndex out of bounds");
        }
        final EdgeHelpers edges = getEdgeHelpers(start, end);
        if (vertexIndex == 0)
        {
            return edges.first;
        }
        if (vertexIndex >= this.n - 1)
        {
            return edges.last;
        }
        // Interior vertex i corresponds to segment i (between i and i+1)
        return helperForInteriorSegment(vertexIndex);
    }

    /**
     * Fallback strategies for when fractional projection is not applicable.
     */
    public enum FractionalFallback
    {

        /** Orthogonal projection clamped to [0,1]. */
        ORTHOGONAL
        {
            @Override
            double getFraction(final FractionalProjectionHelper helper, final double x, final double y)
            {
                return helper.line.projectOrthogonalSnapAt(x, y);
            }
        },

        /** Orthogonal projection allowing extension beyond end-points. */
        ORTHOGONAL_EXTENDED
        {
            @Override
            double getFraction(final FractionalProjectionHelper helper, final double x, final double y)
            {
                return helper.line.projectOrthogonalSnapAt(x, y, false);
            }
        },

        /** Nearest end-point as fraction &lt;0 before start, &gt;1 after end. */
        ENDPOINT
        {
            @Override
            double getFraction(final FractionalProjectionHelper helper, final double x, final double y)
            {
                final Point2d p = new Point2d(x, y);
                final Point2d a = helper.line.get(0);
                final Point2d b = helper.line.get(helper.line.size() - 1);
                final double dStart = p.distance(a);
                final double dEnd = p.distance(b);
                final double total = helper.line.lengthAtIndex(helper.line.size() - 1);
                return (dStart < dEnd) ? (-dStart / total) : ((total + dEnd) / total);
            }
        },

        /** Return NaN. */
        NaN
        {
            @Override
            double getFraction(final FractionalProjectionHelper h, final double x, final double y)
            {
                return Double.NaN;
            }
        };

        /**
         * Returns fraction for when fractional projection fails as the point is beyond the line or from numerical limitations.
         * @param helper helper
         * @param x x coordinate of point
         * @param y y coordinate of point
         * @return fraction for when fractional projection fails
         */
        abstract double getFraction(FractionalProjectionHelper helper, double x, double y);
    }

    /**
     * Value object: per-segment helper, either center or direction.
     * @param hasCenter whether this is a center helper
     * @param cx center x coordinate
     * @param cy center y coordinate
     * @param dx direction x component
     * @param dy direction y component
     */
    public record Helper(boolean hasCenter, double cx, double cy, double dx, double dy)
    {
        /**
         * Factory for a center-based helper.
         * @param cx center x coordinate
         * @param cy center y coordinate
         * @return center-based helper
         */
        public static Helper center(final double cx, final double cy)
        {
            return new Helper(true, cx, cy, Double.NaN, Double.NaN);
        }

        /**
         * Factory for a direction-based helper.
         * @param dx direction x component
         * @param dy direction y component
         * @return direction-based helper
         */
        public static Helper direction(final double dx, final double dy)
        {
            return new Helper(false, Double.NaN, Double.NaN, dx, dy);
        }
    }

    /**
     * Value object for the two edge helpers.
     * @param first helper
     * @param last helper
     */
    private record EdgeHelpers(Helper first, Helper last)
    {
    }

    /**
     * Cache key for directions (normalized angles; nulls allowed).
     * @param start start direction
     * @param end end direction
     */
    public record DirectionKey(Double start, Double end)
    {
        /**
         * Create key with normalized directions, which may be {@code null}.
         * @param start start direction
         * @param end end direction
         * @return key
         */
        public static DirectionKey of(final Direction start, final Direction end)
        {
            Double sa = (start == null) ? null : AngleUtil.normalizeAroundZero(start.si);
            Double ea = (end == null) ? null : AngleUtil.normalizeAroundZero(end.si);
            return new DirectionKey(sa, ea);
        }
    }

    /**
     * Returns helper for interior (non-edge) segment.
     * @param segIndex index
     * @return helper for interior (non-edge) segment
     */
    private Helper helperForInteriorSegment(final int segIndex)
    {
        if (this.isCenter[segIndex])
        {
            return Helper.center(this.centerX[segIndex], this.centerY[segIndex]);
        }
        return Helper.direction(this.dirX[segIndex], this.dirY[segIndex]);
    }

    /**
     * Returns helper for segment.
     * @param segIndex index
     * @param edges edge helpers
     * @return helper for segment
     */
    private Helper helperForSegment(final int segIndex, final EdgeHelpers edges)
    {
        if (segIndex == 0)
        {
            return edges.first;
        }
        if (segIndex == this.n - 1)
        {
            return edges.last;
        }
        return helperForInteriorSegment(segIndex);
    }

    /**
     * Returns edge helpers based on directions.
     * @param start start direction
     * @param end end direction
     * @return edge helpers based on directions
     */
    private EdgeHelpers getEdgeHelpers(final Direction start, final Direction end)
    {
        if (start == null && end == null)
        {
            if (this.edgeNullNull == null)
            {
                this.edgeNullNull = computeEdgeHelpers(null, null);
            }
            return this.edgeNullNull;
        }
        final DirectionKey key = DirectionKey.of(start, end);
        if (key.equals(this.lastKey) && this.edgeLast != null)
        {
            return this.edgeLast;
        }
        this.edgeLast = computeEdgeHelpers(start, end);
        this.lastKey = key;
        return this.edgeLast;
    }

    /**
     * Compute edge helpers.
     * @param start start direction
     * @param end end direction
     * @return edge helpers
     */
    private EdgeHelpers computeEdgeHelpers(final Direction start, final Direction end)
    {
        // Angles (default to segment direction if null)
        final double startAng = (start == null)
                ? Math.atan2(this.line.get(1).y - this.line.get(0).y, this.line.get(1).x - this.line.get(0).x) : start.si;
        final double endAng = (end == null) ? Math.atan2(this.line.get(this.n).y - this.line.get(this.n - 1).y,
                this.line.get(this.n).x - this.line.get(this.n - 1).x) : end.si;

        // Unit offset points at start and end of line
        final Point2d p1 = new Point2d(this.line.get(0).x + Math.cos(startAng + Math.PI / 2.0),
                this.line.get(0).y + Math.sin(startAng + Math.PI / 2.0));
        final Point2d p2 = new Point2d(this.line.get(this.n).x + Math.cos(endAng + Math.PI / 2.0),
                this.line.get(this.n).y + Math.sin(endAng + Math.PI / 2.0));

        Helper hFirst, hLast;
        if (this.n == 1)
        {
            // Single segment: edges collapse; center is intersection of offset rays
            final Point2d c = intersectionOrNull(this.line.get(0), p1, this.line.get(1), p2);
            if (c != null)
            {
                hFirst = Helper.center(c.x, c.y);
            }
            else
            {
                hFirst = Helper.direction(p1.x - this.line.get(0).x, p1.y - this.line.get(0).y);
            }
            // Same for last (same segment)
            hLast = hFirst;
        }
        else
        {
            // Use direction-independent offset intersections
            Point2d cFirst = intersectionOrNull(this.line.get(0), p1, this.line.get(1), this.firstOffsetIntersection);
            if (cFirst != null)
            {
                hFirst = Helper.center(cFirst.x, cFirst.y);
            }
            else
            {
                hFirst = Helper.direction(p1.x - this.line.get(0).x, p1.y - this.line.get(0).y);
            }

            Point2d cLast =
                    intersectionOrNull(this.line.get(this.n - 1), this.lastOffsetIntersection, this.line.get(this.n), p2);
            if (cLast != null)
            {
                hLast = Helper.center(cLast.x, cLast.y);
            }
            else
            {
                hLast = Helper.direction(p2.x - this.line.get(this.n).x, p2.y - this.line.get(this.n).y);
            }
        }
        return new EdgeHelpers(hFirst, hLast);
    }

    /**
     * Projects external point to segment.
     * @param segIndex index
     * @param helper helper of the segment
     * @param ext external point
     * @return projection to segment, or {@code null} if no valid projection
     */
    private Point2d intersectProjection(final int segIndex, final Helper helper, final Point2d ext)
    {
        final Point2d a = this.line.get(segIndex);
        final Point2d b = this.line.get(segIndex + 1);

        if (helper.hasCenter)
        {
            // Intersection of (center -> ext) ray with segment line
            final Point2d c = new Point2d(helper.cx, helper.cy);
            final Point2d p = intersectionOrNull(c, ext, a, b);
            if (p == null)
            {
                return null;
            }
            // Ensure center is not between ext and intersection:
            final double v1x = p.x - c.x, v1y = p.y - c.y;
            final double v2x = ext.x - c.x, v2y = ext.y - c.y;
            final double dot = v1x * v2x + v1y * v2y;
            if (dot <= FRAC_PROJ_PRECISION)
            {
                return null;
            }
            return p;
        }
        // Parallel helper lines: project along stored direction from ext
        final Point2d off = new Point2d(ext.x + helper.dx, ext.y + helper.dy);
        return intersectionOrNull(ext, off, a, b);
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
     * Returns intersection for parallel segments, or midpoint fallback when intersection is null or unstable.
     * @param a1 first point of first parallel segment
     * @param a2 second point of first parallel segment
     * @param b1 first point of second parallel segment
     * @param b2 second point of second parallel segment
     * @return intersection
     */
    private static Point2d intersectionOrFallbackMid(final Point2d a1, final Point2d a2, final Point2d b1, final Point2d b2)
    {
        final Point2d inter = intersectionOrNull(a1, a2, b1, b2);
        if (inter == null)
        {
            return midpoint(a2, b1);
        }
        final double dStraight = a2.distance(b1);
        if (dStraight < Math.min(a2.distance(inter), b1.distance(inter)))
        {
            return midpoint(a2, b1);
        }
        return inter;
    }

    /**
     * Computes the mid-point.
     * @param p first point
     * @param q second point
     * @return mid-point
     */
    private static Point2d midpoint(final Point2d p, final Point2d q)
    {
        return new Point2d(0.5 * (p.x + q.x), 0.5 * (p.y + q.y));
    }

}

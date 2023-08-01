package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Angle;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;

/**
 * Continuous definition of a cubic Bezier. This extends from the more general {@code ContinuousBezier} as certain methods are
 * applied to calculate e.g. the roots, that are specific to cubic Beziers. With such information this class can also specify
 * information to be a {@code ContinuousLine}.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @see <a href="https://pomax.github.io/bezierinfo/">Bezier info</a>
 */
public class ContinuousBezierCubic extends ContinuousBezier implements ContinuousLine
{

    /** Start point with direction. */
    private final OrientedPoint2d startPoint;

    /** End point with direction. */
    private final OrientedPoint2d endPoint;

    /** Length. */
    private final double length;

    /** Cached splits. */
    private NavigableMap<Double, Integer> splits = null;

    /**
     * Create a cubic Bezier.
     * @param point1 Point2d; start point.
     * @param point2 Point2d; first intermediate shape point.
     * @param point3 Point2d; second intermediate shape point.
     * @param point4 Point2d; end point.
     */
    public ContinuousBezierCubic(final Point2d point1, final Point2d point2, final Point2d point3, final Point2d point4)
    {
        super(point1, point2, point3, point4);
        this.startPoint = new OrientedPoint2d(point1.x, point1.y, Math.atan2(point2.y - point1.y, point2.x - point1.x));
        this.endPoint = new OrientedPoint2d(point4.x, point4.y, Math.atan2(point4.y - point3.y, point4.x - point3.x));
        this.length = length();
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getStartPoint()
    {
        return this.startPoint;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getEndPoint()
    {
        return this.endPoint;
    }

    /** {@inheritDoc} */
    @Override
    public double getStartCurvature()
    {
        return curvature(0.0);
    }

    /** {@inheritDoc} */
    @Override
    public double getEndCurvature()
    {
        return curvature(1.0);
    }

    /** {@inheritDoc} */
    @Override
    public OtsLine3d flatten(final int numSegments)
    {
        Throw.when(numSegments < 1, IllegalArgumentException.class, "Number of segments should be at least 1.");
        return Try.assign(() -> Bezier.cubic(numSegments + 1, this.points[0], this.points[1], this.points[2], this.points[3]),
                "Cannot happen.");
    }

    /** {@inheritDoc} */
    @Override
    public OtsLine3d flatten(final Angle maxAngleError, final double maxSpatialError)
    {
        Throw.whenNull(maxAngleError, "Maximum angle error may not be null");
        Throw.when(maxAngleError.si <= 0.0, IllegalArgumentException.class, "Max angle error should be above 0.");
        Throw.when(maxSpatialError <= 0.0, IllegalArgumentException.class, "Max spatial error should be above 0.");
        // TODO
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public OtsLine3d offset(final NavigableMap<Double, Double> offsets, final int numSegments)
    {
        Throw.when(numSegments < 1, IllegalArgumentException.class, "Number of segments should be at least 1.");
        Throw.whenNull(offsets, "Offsets may not be null.");
        Throw.when(!offsets.containsKey(0.0), IllegalArgumentException.class, "Offsets need to contain key 0.0.");
        Throw.when(!offsets.containsKey(1.0), IllegalArgumentException.class, "Offsets need to contain key 1.0.");
        /*
         * A Bezier does not have a trivial offset. Hence, we split the Bezier along points of 3 types. 1) roots, where the
         * derivative of either the x-component or y-component is 0, such that we obtain C-shaped scalable segments, 2)
         * inflections, where the curvature changes sign and the offset and offset angle need to flip sign, and 3) cross-section
         * fractions so that the intended offset segments can be adhered to. Note that C-shaped segments can be scaled similar
         * to a circle arc, whereas S-shaped segments have no trivial scaling and are thus split.
         */

        // Gather all points to split segments, and their types (1=root, 2=inflection, or 3=cross-section)
        if (this.splits == null)
        {
            this.splits = new TreeMap<>(); // cache
            getRoots().forEach((t) -> this.splits.put(t, 1));
            getInflections().forEach((t) -> this.splits.put(t, 2));
            getCrossSections(offsets.navigableKeySet()).forEach((t) -> this.splits.put(t, 3));
            this.splits = this.splits.subMap(1e-6, false, 1.0 - 1e-6, false);
        }

        // Pre-calculate number of segments
        double tPrev = 0.0;
        List<Integer> numSegmentsPerSegment = new ArrayList<>();
        for (int i = 0; i <= this.splits.size(); i++)
        {
            Double t = this.splits.higherKey(tPrev);
            t = t == null ? 1.0 : t;
            numSegmentsPerSegment.add((int) Math.ceil((t - tPrev) * numSegments));
            tPrev = t;
        }

        // Initialize loop variables
        // copy of cross-section fractions, so we can remove each we use; exclude 0.0 value to find split points -on- Bezier
        NavigableSet<Double> fCrossSectionRemain = new TreeSet<>(offsets.navigableKeySet()).tailSet(0.0, false);
        double lengthTotal = length();
        ContinuousBezierCubic currentBezier = this;
        double lengthSoFar = 0.0;
        boolean first = true;
        // curvature and angle sign, flips at each inflection, start based on initial curve
        double sig = Math.signum((this.points[1].y - this.points[0].y) * (this.points[2].x - this.points[0].x)
                - (this.points[1].x - this.points[0].x) * (this.points[2].y - this.points[0].y));

        List<Point2d> points = new ArrayList<>();
        Iterator<Integer> numSegmentsIterator = numSegmentsPerSegment.iterator();
        Iterator<Double> typeIterator = this.splits.navigableKeySet().iterator();
        if (this.splits.isEmpty())
        {
            appendOffset(points, currentBezier, offsets, lengthSoFar, lengthTotal, lengthTotal, sig, first, true, numSegments);
        }
        while (typeIterator.hasNext())
        {

            int type = this.splits.get(typeIterator.next());
            boolean isRoot = type == 1;
            boolean isInflection = type == 2;
            // boolean isCrossSection = type == 3;
            double t;
            // Note: as we split the Bezier and work with the remainder in each loop, the resulting t value is not the same as
            // on the full Bezier. Therefore we need to refind the roots, or inflections, or at least one cross-section.
            if (isRoot)
            {
                t = currentBezier.getRoots().first();
            }
            else if (isInflection)
            {
                t = currentBezier.getInflections().first();
            }
            else
            {
                NavigableSet<Double> fCrossSection = new TreeSet<>();
                double fSoFar = lengthSoFar / lengthTotal;
                double fFirst = fCrossSectionRemain.pollFirst(); // fraction in total Bezier
                fCrossSection.add((fFirst - fSoFar) / (1.0 - fSoFar)); // add fraction in remaining Bezier
                t = currentBezier.getCrossSections(fCrossSection).first();
            }

            // Split Bezier, and add offset of first part
            ContinuousBezierCubic[] parts = currentBezier.split(t);
            double lengthSegment = parts[0].length();
            int n = numSegmentsIterator.next();
            appendOffset(points, parts[0], offsets, lengthSoFar, lengthSegment, lengthTotal, sig, first, false, n);

            // Update loop variables
            first = false;
            lengthSoFar += lengthSegment;
            if (isInflection)
            {
                sig = -sig;
            }

            // Append last segment, or loop again with remainder
            if (!typeIterator.hasNext())
            {
                lengthSegment = parts[1].length();
                n = numSegmentsIterator.next();
                appendOffset(points, parts[1], offsets, lengthSoFar, lengthSegment, lengthTotal, sig, first, true, n);
            }
            else
            {
                currentBezier = parts[1];
            }
        }

        return Try.assign(() -> new OtsLine3d(points), "Bezier offset has too few points.");
    }

    /**
     * Creates the line segment points of an offset line of a Bezier segment.
     * @param points List&lt;Point2d&gt;; list of points to add points to.
     * @param bezier ContinuousBezierCubic; Bezier segment to offset.
     * @param offsets NavigableMap&lt;Double, Double&gt;; offsets as defined for entire Bezier.
     * @param lengthSoFar double; total length of previous segments.
     * @param lengthSegment double; length of the current Bezier segment.
     * @param lengthTotal double; total length of full Bezier.
     * @param sig double; sign of offset and offset slope
     * @param first boolean; {@code true} for the first Bezier segment.
     * @param last boolean; {@code true} for the last Bezier segment.
     * @param numSegments int; number of segments to apply for this Bezier segment.
     */
    private static void appendOffset(final List<Point2d> points, final ContinuousBezierCubic bezier,
            final NavigableMap<Double, Double> offsets, final double lengthSoFar, final double lengthSegment,
            final double lengthTotal, final double sig, final boolean first, final boolean last, final int numSegments)
    {
        double offsetStart = sig * OtsGeometryUtil.offsetInterpolation(lengthSoFar / lengthTotal, offsets);
        double offsetEnd = sig * OtsGeometryUtil.offsetInterpolation((lengthSoFar + lengthSegment) / lengthTotal, offsets);

        Point2d p1 = new Point2d(bezier.points[0].x - (bezier.points[1].y - bezier.points[0].y),
                bezier.points[0].y + (bezier.points[1].x - bezier.points[0].x));
        Point2d p2 = new Point2d(bezier.points[3].x - (bezier.points[2].y - bezier.points[3].y),
                bezier.points[3].y + (bezier.points[2].x - bezier.points[3].x));
        Point2d center = Point2d.intersectionOfLines(bezier.points[0], p1, p2, bezier.points[3]);

        // move 1st and 4th point their respective offsets away from the center
        Point2d[] newBezierPoints = new Point2d[4];
        double off = offsetStart;
        for (int i = 0; i < 4; i = i + 3)
        {
            double dy = bezier.points[i].y - center.y;
            double dx = bezier.points[i].x - center.x;
            double ang = Math.atan2(dy, dx);
            double len = Math.hypot(dx, dy) + off;
            newBezierPoints[i] = new Point2d(center.x + len * Math.cos(ang), center.y + len * Math.sin(ang));
            off = offsetEnd;
        }

        // find tangent unit vectors that account for slope in offset
        double ang = sig * Math.atan((offsetEnd - offsetStart) / lengthSegment);
        double cosAng = Math.cos(ang);
        double sinAng = Math.sin(ang);
        double dx = bezier.points[1].x - bezier.points[0].x;
        double dy = bezier.points[1].y - bezier.points[0].y;
        double dx1;
        double dy1;
        if (first)
        {
            // force same start angle
            dx1 = dx;
            dy1 = dy;
        }
        else
        {
            // shift angle by 'ang'
            dx1 = cosAng * dx - sinAng * dy;
            dy1 = sinAng * dx + cosAng * dy;
        }
        dx = bezier.points[2].x - bezier.points[3].x;
        dy = bezier.points[2].y - bezier.points[3].y;
        double dx2;
        double dy2;
        if (last)
        {
            // force same end angle
            dx2 = dx;
            dy2 = dy;
        }
        else
        {
            // shift angle by 'ang'
            dx2 = cosAng * dx - sinAng * dy;
            dy2 = sinAng * dx + cosAng * dy;
        }

        // control points 2 and 3 as intersections between tangent unit vectors and line through center and original point 2 and
        // 3 in original Bezier
        Point2d cp2 = new Point2d(newBezierPoints[0].x + dx1, newBezierPoints[0].y + dy1);
        newBezierPoints[1] = Point2d.intersectionOfLines(newBezierPoints[0], cp2, center, bezier.points[1]);
        Point2d cp3 = new Point2d(newBezierPoints[3].x + dx2, newBezierPoints[3].y + dy2);
        newBezierPoints[2] = Point2d.intersectionOfLines(newBezierPoints[3], cp3, center, bezier.points[2]);

        // create and add points
        int lastI = last ? numSegments : numSegments - 1; // prevent duplicate points where segments meet
        Point2d[] offsetPoints = Try.assign(() -> Bezier.bezier(numSegments + 1, newBezierPoints).getPoints(),
                "Unable to create Bezier segment offset line.");
        for (int i = 0; i <= lastI; i++)
        {
            points.add(offsetPoints[i]);
        }
    }

    /** {@inheritDoc} */
    @Override
    public OtsLine3d offset(final NavigableMap<Double, Double> offsets, final Angle maxAngleError, final double maxSpatialError)
    {
        Throw.whenNull(maxAngleError, "Maximum angle error may not be null");
        Throw.when(maxAngleError.si <= 0.0, IllegalArgumentException.class, "Max angle error should be above 0.");
        Throw.when(maxSpatialError <= 0.0, IllegalArgumentException.class, "Max spatial error should be above 0.");
        Throw.whenNull(offsets, "Offsets may not be null.");
        Throw.when(!offsets.containsKey(0.0), IllegalArgumentException.class, "Offsets need to contain key 0.0.");
        Throw.when(!offsets.containsKey(1.0), IllegalArgumentException.class, "Offsets need to contain key 1.0.");
        // TODO
        return null;
    }

    /**
     * Returns the root t values, where each of the sub-components derivative for x and y are 0.0.
     * @return SortedSet&lt;Double&gt;; set of root t values, sorted and in the range (0, 1), exclusive.
     */
    private SortedSet<Double> getRoots()
    {
        // Uses quadratic Bezier formulation
        double ax = 3.0 * (-this.points[0].x + 3.0 * this.points[1].x - 3.0 * this.points[2].x + this.points[3].x);
        double ay = 3.0 * (-this.points[0].y + 3.0 * this.points[1].y - 3.0 * this.points[2].y + this.points[3].y);
        double bx = 6.0 * (this.points[0].x - 2.0 * this.points[1].x + this.points[2].x);
        double by = 6.0 * (this.points[0].y - 2.0 * this.points[1].y + this.points[2].y);
        double cx = 3.0 * (this.points[1].x - this.points[0].x);
        double cy = 3.0 * (this.points[1].y - this.points[0].y);

        // ABC formula
        TreeSet<Double> roots = new TreeSet<>();
        double g = bx * bx - 4.0 * ax * cx;
        if (g > 0)
        {
            double sqrtg = Math.sqrt(g);
            double ax2 = 2.0 * ax;
            roots.add((-bx + sqrtg) / ax2);
            roots.add((-bx - sqrtg) / ax2);
        }
        g = by * by - 4.0 * ay * cy;
        if (g > 0)
        {
            double sqrtg = Math.sqrt(g);
            double ay2 = 2.0 * ay;
            roots.add((-by + sqrtg) / ay2);
            roots.add((-by - sqrtg) / ay2);
        }

        // Only roots in range (0.0 ... 1.0) are valid and useful
        return roots.subSet(0.0, false, 1.0, false);
    }

    /**
     * Returns the inflection t values, where curvature changes sign.
     * @return SortedSet&lt;Double&gt;; set of inflection t values, sorted and in the range (0, 1), exclusive.
     */
    private SortedSet<Double> getInflections()
    {
        // Align: translate so first point is (0, 0), rotate so last point is on x=axis (y = 0)
        Point2d[] aligned = new Point2d[4];
        double ang = -Math.atan2(this.points[3].y - this.points[0].y, this.points[3].x - this.points[0].x);
        double cosAng = Math.cos(ang);
        double sinAng = Math.sin(ang);
        for (int i = 0; i < 4; i++)
        {
            aligned[i] =
                    new Point2d(cosAng * (this.points[i].x - this.points[0].x) - sinAng * (this.points[i].y - this.points[0].y),
                            sinAng * (this.points[i].x - this.points[0].x) + cosAng * (this.points[i].y - this.points[0].y));
        }

        // Inflection as curvature = 0, using:
        // curvature = x'(t)*y''(t) + y'(t)*x''(t) = 0
        // (this is highly simplified due to the alignment, removing many terms)
        double a = aligned[2].x * aligned[1].y;
        double b = aligned[3].x * aligned[1].y;
        double c = aligned[1].x * aligned[2].y;
        double d = aligned[3].x * aligned[2].y;

        double x = -3.0 * a + 2.0 * b + 3.0 * c - d;
        double y = 3.0 * a - b - 3.0 * c;
        double z = c - a;

        // ABC formula (on x, y, z)
        TreeSet<Double> inflections = new TreeSet<>();
        if (Math.abs(x) < 1.0e-6)
        {
            if (Math.abs(y) >= 1.0e-12)
            {
                inflections.add(-z / y);
            }
        }
        else
        {
            double det = y * y - 4.0 * x * z;
            double sq = Math.sqrt(det);
            double d2 = 2 * x;
            if (det >= 0.0 && Math.abs(d2) >= 1e-12)
            {
                inflections.add(-(y + sq) / d2);
                inflections.add((sq - y) / d2);
            }
        }

        // Only inflections in range (0.0 ... 1.0) are valid and useful
        return inflections.subSet(0.0, false, 1.0, false);
    }

    /**
     * Returns the cross-section t values.
     * @param fractions SortedSet&lt;Double&gt;; length fractions at which cross-sections are defined.
     * @return SortedSet&lt;Double&gt;; set of cross-section t values, sorted and in the range (0, 1), exclusive.
     */
    private SortedSet<Double> getCrossSections(final SortedSet<Double> fractions)
    {
        TreeSet<Double> crossSections = new TreeSet<>();
        double lenTot = length();
        for (Double f : fractions)
        {
            if (f > 0.0 && f < 1.0)
            {
                crossSections.add(getT(f * lenTot));
            }
        }
        return crossSections;
    }

    /**
     * Returns the t value at the provided length along the Bezier. This method uses an iterative approach with a precision of
     * 1e-6.
     * @param len double; length along the Bezier.
     * @return double; t value at the provided length along the Bezier.
     */
    public double getT(final double len)
    {
        // start at 0.0 and 1.0, cut in half, see which half to use next
        double t0 = 0.0;
        double t2 = 1.0;
        double t1 = 0.5;
        while (t2 > t0 + 1.0e-6)
        {
            t1 = (t2 + t0) / 2.0;
            ContinuousBezierCubic[] parts = split(t1);
            double len1 = parts[0].length();
            if (len1 < len)
            {
                t0 = t1;
            }
            else
            {
                t2 = t1;
            }
        }
        return t1;
    }

    /**
     * Splits the Bezier in two Beziers of the same order.
     * @param t double; t value along the Bezier to apply the split.
     * @return ContinuousBezierCubic[]; the Bezier before t, and the Bezier after t.
     */
    public ContinuousBezierCubic[] split(final double t)
    {
        Throw.when(t < 0.0 || t > 1.0, IllegalArgumentException.class, "t value should be in the range [0.0 ... 1.0].");
        List<Point2d> p1 = new ArrayList<>();
        List<Point2d> p2 = new ArrayList<>();
        split0(t, List.of(this.points), p1, p2);
        return new ContinuousBezierCubic[] {new ContinuousBezierCubic(p1.get(0), p1.get(1), p1.get(2), p1.get(3)),
                new ContinuousBezierCubic(p2.get(3), p2.get(2), p2.get(1), p2.get(0))};
    }

    /**
     * Performs the iterative algorithm of Casteljau to derive the split Beziers.
     * @param t double; t value along the Bezier to apply the split.
     * @param p List&lt;Point2d&gt;; shape points of Bezier still to split.
     * @param p1 List&lt;Point2d&gt;; shape points of first part, accumulated in the recursion.
     * @param p2 List&lt;Point2d&gt;; shape points of first part, accumulated in the recursion.
     */
    private void split0(final double t, final List<Point2d> p, final List<Point2d> p1, final List<Point2d> p2)
    {
        if (p.size() == 1)
        {
            p1.add(p.get(0));
            p2.add(p.get(0));
        }
        else
        {
            List<Point2d> pNew = new ArrayList<>();
            for (int i = 0; i < p.size() - 1; i++)
            {
                if (i == 0)
                {
                    p1.add(p.get(i));
                }
                if (i == p.size() - 2)
                {
                    p2.add(p.get(i + 1));
                }
                double t1 = 1.0 - t;
                pNew.add(new Point2d(t1 * p.get(i).x + t * p.get(i + 1).x, t1 * p.get(i).y + t * p.get(i + 1).y));
            }
            split0(t, pNew, p1, p2);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ContinuousBezierCubic [points=" + Arrays.toString(this.points) + "]";
    }

    /** {@inheritDoc} */
    @Override
    public double getLength()
    {
        return this.length;
    }

}

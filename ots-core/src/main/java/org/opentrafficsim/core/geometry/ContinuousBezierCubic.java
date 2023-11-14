package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

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
     * Returns the offset t values.
     * @param fractions Set&lt;Double&gt;; length fractions at which offsets are defined.
     * @return SortedSet&lt;Double&gt;; set of offset t values, sorted and in the range (0, 1), exclusive.
     */
    private SortedSet<Double> getOffsetT(final Set<Double> fractions)
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
    public PolyLine2d flatten(final Flattener flattener)
    {
        Throw.whenNull(flattener, "Flattener may not be null.");
        return flattener.flatten(new FlattableLine()
        {
            /** {@inheritDoc} */
            @Override
            public Point2d get(final double fraction)
            {
                return at(fraction);
            }

            /** {@inheritDoc} */
            @Override
            public double getDirection(final double fraction)
            {
                Point2d derivative = derivative().at(fraction);
                return Math.atan2(derivative.y, derivative.x);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d flattenOffset(final FractionalLengthData offsets, final Flattener flattener)
    {
        Throw.whenNull(offsets, "Offsets may not be null.");
        Throw.whenNull(flattener, "Flattener may not be null.");

        /*
         * A Bezier does not have a trivial offset. Hence, we split the Bezier along points of 3 types. 1) roots, where the
         * derivative of either the x-component or y-component is 0, such that we obtain C-shaped scalable segments, 2)
         * inflections, where the curvature changes sign and the offset and offset angle need to flip sign, and 3) offset
         * fractions so that the intended offset segments can be adhered to. Note that C-shaped segments can be scaled similar
         * to a circle arc, whereas S-shaped segments have no trivial scaling and are thus split.
         */
        NavigableMap<Double, ContinuousBezierCubic> segments = new TreeMap<>();

        // Gather all points to split segments, and their types (1=root, 2=inflection, or 3=offset fraction)
        NavigableMap<Double, Integer> splits0 = new TreeMap<>(); // splits0 & splits because splits0 must be effectively final
        getRoots().forEach((t) -> splits0.put(t, 1));
        getInflections().forEach((t) -> splits0.put(t, 2));
        getOffsetT(offsets.getFractionalLengths().toSet()).forEach((t) -> splits0.put(t, 3));
        NavigableMap<Double, Integer> splits = splits0.subMap(1e-6, false, 1.0 - 1e-6, false);

        // Initialize loop variables
        // copy of offset fractions, so we can remove each we use; exclude 0.0 value to find split points -on- Bezier
        NavigableSet<Double> fCrossSectionRemain = offsets.getFractionalLengths().toSet().tailSet(0.0, false);
        double lengthTotal = length();
        ContinuousBezierCubic currentBezier = this;
        double lengthSoFar = 0.0;
        boolean first = true;
        // curvature and angle sign, flips at each inflection, start based on initial curve
        double sig = Math.signum((this.points[1].y - this.points[0].y) * (this.points[2].x - this.points[0].x)
                - (this.points[1].x - this.points[0].x) * (this.points[2].y - this.points[0].y));

        Iterator<Double> typeIterator = splits.navigableKeySet().iterator();
        double tStart = 0.0;
        if (splits.isEmpty())
        {
            segments.put(tStart, currentBezier.offset(offsets, lengthSoFar, lengthTotal, sig, first, true));
        }
        while (typeIterator.hasNext())
        {

            double tInFull = typeIterator.next();
            int type = splits.get(tInFull);
            boolean isRoot = type == 1;
            boolean isInflection = type == 2;
            // boolean isOffsetFraction = type == 3;
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
                t = currentBezier.getOffsetT(fCrossSection).first();
            }

            // Split Bezier, and add offset of first part
            ContinuousBezierCubic[] parts = currentBezier.split(t);
            segments.put(tStart, parts[0].offset(offsets, lengthSoFar, lengthTotal, sig, first, false));

            // Update loop variables
            first = false;
            lengthSoFar += parts[0].getLength();
            if (isInflection)
            {
                sig = -sig;
            }
            tStart = tInFull;

            // Append last segment, or loop again with remainder
            if (!typeIterator.hasNext())
            {
                segments.put(tStart, parts[1].offset(offsets, lengthSoFar, lengthTotal, sig, first, true));
            }
            else
            {
                currentBezier = parts[1];
            }
        }
        segments.put(1.0, null); // so we can interpolate t values along segments

        // Flatten with FlattableLine based on the offset segments created above
        return flattener.flatten(new FlattableLine()
        {
            /** {@inheritDoc} */
            @Override
            public Point2d get(final double fraction)
            {
                Entry<Double, ContinuousBezierCubic> entry;
                double nextT;
                if (fraction == 1.0)
                {
                    entry = segments.lowerEntry(fraction);
                    nextT = fraction;
                }
                else
                {
                    entry= segments.floorEntry(fraction);
                    nextT = segments.higherKey(fraction);
                }
                double t = (fraction - entry.getKey()) / (nextT - entry.getKey());
                return entry.getValue().at(t);
            }

            /** {@inheritDoc} */
            @Override
            public double getDirection(final double fraction)
            {
                Entry<Double, ContinuousBezierCubic> entry = segments.floorEntry(fraction);
                if (entry.getValue() == null)
                {
                    // end of line
                    entry = segments.lowerEntry(fraction);
                    Point2d derivative = entry.getValue().derivative().at(1.0);
                    return Math.atan2(derivative.y, derivative.x);
                }
                Double nextT = segments.higherKey(fraction);
                if (nextT == null)
                {
                    nextT = 1.0;
                }
                double t = (fraction - entry.getKey()) / (nextT - entry.getKey());
                Point2d derivative = entry.getValue().derivative().at(t);
                return Math.atan2(derivative.y, derivative.x);
            }
        });
    }

    /**
     * Creates the offset Bezier of a Bezier segment. These segments are part of the offset procedure.
     * @param offsets FractionalLengthData; offsets as defined for entire Bezier.
     * @param lengthSoFar double; total length of previous segments.
     * @param lengthTotal double; total length of full Bezier.
     * @param sig double; sign of offset and offset slope
     * @param first boolean; {@code true} for the first Bezier segment.
     * @param last boolean; {@code true} for the last Bezier segment.
     * @return ContinuousBezierCubic; offset Bezier.
     */
    private ContinuousBezierCubic offset(final FractionalLengthData offsets, final double lengthSoFar, final double lengthTotal,
            final double sig, final boolean first, final boolean last)
    {
        double offsetStart = sig * offsets.get(lengthSoFar / lengthTotal);
        double offsetEnd = sig * offsets.get((lengthSoFar + getLength()) / lengthTotal);

        Point2d p1 = new Point2d(this.points[0].x - (this.points[1].y - this.points[0].y),
                this.points[0].y + (this.points[1].x - this.points[0].x));
        Point2d p2 = new Point2d(this.points[3].x - (this.points[2].y - this.points[3].y),
                this.points[3].y + (this.points[2].x - this.points[3].x));
        Point2d center = Point2d.intersectionOfLines(this.points[0], p1, p2, this.points[3]);

        // move 1st and 4th point their respective offsets away from the center
        Point2d[] newBezierPoints = new Point2d[4];
        double off = offsetStart;
        for (int i = 0; i < 4; i = i + 3)
        {
            double dy = this.points[i].y - center.y;
            double dx = this.points[i].x - center.x;
            double ang = Math.atan2(dy, dx);
            double len = Math.hypot(dx, dy) + off;
            newBezierPoints[i] = new Point2d(center.x + len * Math.cos(ang), center.y + len * Math.sin(ang));
            off = offsetEnd;
        }

        // find tangent unit vectors that account for slope in offset
        double ang = sig * Math.atan((offsetEnd - offsetStart) / getLength());
        double cosAng = Math.cos(ang);
        double sinAng = Math.sin(ang);
        double dx = this.points[1].x - this.points[0].x;
        double dy = this.points[1].y - this.points[0].y;
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
        dx = this.points[2].x - this.points[3].x;
        dy = this.points[2].y - this.points[3].y;
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
        newBezierPoints[1] = Point2d.intersectionOfLines(newBezierPoints[0], cp2, center, this.points[1]);
        Point2d cp3 = new Point2d(newBezierPoints[3].x + dx2, newBezierPoints[3].y + dy2);
        newBezierPoints[2] = Point2d.intersectionOfLines(newBezierPoints[3], cp3, center, this.points[2]);

        // create offset Bezier
        return new ContinuousBezierCubic(newBezierPoints[0], newBezierPoints[1], newBezierPoints[2], newBezierPoints[3]);
    }

    /** {@inheritDoc} */
    @Override
    public double getLength()
    {
        return this.length;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ContinuousBezierCubic [points=" + Arrays.toString(this.points) + "]";
    }

}

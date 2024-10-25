package org.opentrafficsim.base.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

/**
 * Shape defined by a rounded rectangle.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RoundedRectangleShape implements OtsShape
{

    /** Half length along y dimension. */
    private final double dx;

    /** Half length along y dimension. */
    private final double dy;

    /** Rounding radius. */
    private final double r;

    /** Max x coordinate, can be lower than dx due to large r. */
    private final double maxX;

    /** Max y coordinate, can be lower than dy due to large r. */
    private final double maxY;

    /** Number of line segments in polygon representation. */
    private final int polygonSegments;

    /** Polygon representation. */
    private Polygon2d polygon;

    /**
     * Constructor.
     * @param dx complete length along x dimension.
     * @param dy complete length along y dimension.
     * @param r radius of rounding, must be positive.
     * @throws IllegalArgumentException when r is negative, or so large no net shape remains
     */
    public RoundedRectangleShape(final double dx, final double dy, final double r)
    {
        this(dx, dy, r, DEFAULT_POLYGON_SEGMENTS);
    }

    /**
     * Constructor.
     * @param dx complete length along x dimension.
     * @param dy complete length along y dimension.
     * @param r radius of rounding, must be positive.
     * @param polygonSegments number of segments in polygon representation.
     * @throws IllegalArgumentException when r is negative, or so large no net shape remains
     */
    public RoundedRectangleShape(final double dx, final double dy, final double r, final int polygonSegments)
    {
        /*-
         * Equation derived from r^2 = (r-dx)^2 + (r-dy^2)  [note: dx and dy here as half of input values, i.e. this.dx/this.dy]
         * 
         *                dx
         *    ___       ______
         * ^ |   ''--_ |      | dy
         * | |---------o------
         * | | r-dx  / |'.
         * r |      /  |  '.
         * | |   r/    |    \
         * | |   /     |     \
         * | | /   r-dy|      |
         * v |/________|______|
         *    <-------r------>
         */
        this.dx = Math.abs(dx) / 2.0;
        this.dy = Math.abs(dy) / 2.0;
        Throw.when(r >= this.dx + this.dy + Math.sqrt(2.0 * this.dx * this.dy), IllegalArgumentException.class,
                "Radius makes rounded rectangle non-existent.");
        Throw.when(r < 0.0, IllegalArgumentException.class, "Radius must be positive.");
        this.r = r;
        this.maxX = this.dx - signedDistance(new Point2d(this.dx, 0.0));
        this.maxY = this.dy - signedDistance(new Point2d(0.0, this.dy));
        this.polygonSegments = polygonSegments;
    }

    @Override
    public double getMinX()
    {
        return -this.maxX;
    }

    @Override
    public double getMaxX()
    {
        return this.maxX;
    }

    @Override
    public double getMinY()
    {
        return -this.maxY;
    }

    @Override
    public double getMaxY()
    {
        return this.maxY;
    }

    @Override
    public boolean contains(final Point2d point) throws NullPointerException
    {
        return signedDistance(point) < 0.0;
    }

    @Override
    public boolean contains(final double x, final double y) throws NullPointerException
    {
        return contains(new Point2d(x, y));
    }

    /**
     * {@inheritDoc}
     * @see <a href="https://iquilezles.org/articles/distfunctions/">Signed distance functions by Inigo Quilez</a>
     */
    @Override
    public double signedDistance(final Point2d point)
    {
        double qx = Math.abs(point.x) - this.dx + this.r;
        double qy = Math.abs(point.y) - this.dy + this.r;
        return Math.hypot(Math.max(qx, 0.0), Math.max(qy, 0.0)) + Math.min(Math.max(qx, qy), 0.0) - this.r;
    }

    @Override
    public Polygon2d asPolygon()
    {
        if (this.polygon == null)
        {
            // calculate for top right quadrant only, others are negative or reversed copies
            int n = this.polygonSegments / 4;
            List<Point2d> pq = new ArrayList<>();
            for (int i = 0; i <= n; i++)
            {
                double ang = (0.5 * Math.PI * i) / n;
                double x = this.dx + Math.cos(ang) * this.r - this.r;
                double y = this.dy + Math.sin(ang) * this.r - this.r;
                if (x >= 0.0 && y >= 0.0) // else, radius larger than at least one of this.dx and this.dy, i.e. no full quarters
                {
                    pq.add(new Point2d(x, y));
                }
            }

            List<Point2d> pqReversed = new ArrayList<>(pq);
            Collections.reverse(pqReversed);
            List<Point2d> points = new ArrayList<>(pq); // top right quadrant (y = up)
            pqReversed.forEach((p) -> points.add(new Point2d(-p.x, p.y))); // top left quadrant
            pq.forEach((p) -> points.add(p.neg())); // bottom left quadrant
            pqReversed.forEach((p) -> points.add(new Point2d(p.x, -p.y))); // bottom right quadrant
            this.polygon = new Polygon2d(true, points);
        }
        return this.polygon;
    }

    @Override
    public String toString()
    {
        return "RoundedRectangleShape [dx=" + this.dx + ", dy=" + this.dy + ", r=" + this.r + "]";
    }

}

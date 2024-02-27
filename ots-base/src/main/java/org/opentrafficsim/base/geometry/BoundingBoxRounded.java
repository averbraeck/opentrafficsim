package org.opentrafficsim.base.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

/**
 * Bounds defined by a rounded rectangle.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class BoundingBoxRounded implements OtsBounds2d
{

    /** Number of line segments in polygon representation for the curves if they are 4 full quarter circles. */
    private final static int POLYGON_STEPS = 128;

    /** Half length along y dimension. */
    private final double dx;

    /** Half length along y dimension. */
    private final double dy;

    /** Polygon representation. */
    private Polygon2d polygon;

    /** Rounding radius. */
    private final double r;

    /** Max x coordinate, can be lower than dx due to large r. */
    private final double maxX;

    /** Max y coordinate, can be lower than dy due to large r. */
    private final double maxY;

    /**
     * Constructor.
     * @param dx double; complete length along x dimension.
     * @param dy double; complete length along y dimension.
     * @param r double; radius of rounding, must be positive.
     */
    public BoundingBoxRounded(final double dx, final double dy, final double r)
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
    }

    /** {@inheritDoc} */
    @Override
    public double getMinX()
    {
        return -this.maxX;
    }

    /** {@inheritDoc} */
    @Override
    public double getMaxX()
    {
        return this.maxX;
    }

    /** {@inheritDoc} */
    @Override
    public double getMinY()
    {
        return -this.maxY;
    }

    /** {@inheritDoc} */
    @Override
    public double getMaxY()
    {
        return this.maxY;
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Point2d point) throws NullPointerException
    {
        return signedDistance(point) < 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean covers(final Point2d point) throws NullPointerException
    {
        return signedDistance(point) <= 0.0;
    }

    /**
     * {@inheritDoc}
     * @see <a href="https://iquilezles.org/articles/distfunctions/">Signed distance functions by Inigo Quilez</a>
     */
    @Override
    public double signedDistance(final Point2d point)
    {
        double qx = Math.abs(point.x) - this.dx + this.r; // note: at time of writing site by Inigo Quilez omits the +r
        double qy = Math.abs(point.y) - this.dy + this.r;
        return Math.hypot(Math.max(qx, 0.0), Math.max(qy, 0.0)) + Math.min(Math.max(qx, qy), 0.0) - this.r;
    }

    /** {@inheritDoc} */
    @Override
    public Polygon2d asPolygon()
    {
        if (this.polygon == null)
        {
            // calculate for top right quadrant only, others are negative or reversed copies
            int n = POLYGON_STEPS / 4;
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

}

package org.opentrafficsim.base.geometry;

import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;

/**
 * Shape defined by a rectangle.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RectangleShape implements OtsShape
{

    /** Half length along y dimension. */
    private final double dx;

    /** Half length along y dimension. */
    private final double dy;

    /** Polygon representation. */
    private Polygon2d polygon;

    /**
     * Constructor.
     * @param dx complete length along x dimension.
     * @param dy complete length along y dimension.
     */
    public RectangleShape(final double dx, final double dy)
    {
        this.dx = Math.abs(dx) / 2.0;
        this.dy = Math.abs(dy) / 2.0;
    }

    @Override
    public double getMinX()
    {
        return -this.dx;
    }

    @Override
    public double getMaxX()
    {
        return this.dx;
    }

    @Override
    public double getMinY()
    {
        return -this.dy;
    }

    @Override
    public double getMaxY()
    {
        return this.dy;
    }

    @Override
    public boolean contains(final double x, final double y) throws NullPointerException
    {
        return Math.abs(x) < this.dx && Math.abs(y) < this.dy;
    }

    /**
     * {@inheritDoc}
     * @see <a href="https://iquilezles.org/articles/distfunctions/">Signed distance functions by Inigo Quilez</a>
     */
    @Override
    public double signedDistance(final Point2d point)
    {
        double qx = Math.abs(point.x) - this.dx;
        double qy = Math.abs(point.y) - this.dy;
        return Math.hypot(Math.max(qx, 0.0), Math.max(qy, 0.0)) + Math.min(Math.max(qx, qy), 0.0);
    }

    @Override
    public Polygon2d asPolygon()
    {
        if (this.polygon == null)
        {
            this.polygon = new Polygon2d(new Point2d(this.dx, this.dy), new Point2d(-this.dx, this.dy),
                    new Point2d(-this.dx, -this.dy), new Point2d(this.dx, -this.dy), new Point2d(this.dx, this.dy));
        }
        return this.polygon;
    }

    @Override
    public String toString()
    {
        return "RectangleShape [dx=" + this.dx + ", dy=" + this.dy + "]";
    }

}

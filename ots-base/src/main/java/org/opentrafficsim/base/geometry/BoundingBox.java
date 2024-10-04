package org.opentrafficsim.base.geometry;

import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;

/**
 * Bounds defined by a rectangle.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class BoundingBox implements OtsBounds2d
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
    public BoundingBox(final double dx, final double dy)
    {
        this.dx = Math.abs(dx) / 2.0;
        this.dy = Math.abs(dy) / 2.0;
    }

    /** {@inheritDoc} */
    @Override
    public double getMinX()
    {
        return -this.dx;
    }

    /** {@inheritDoc} */
    @Override
    public double getMaxX()
    {
        return this.dx;
    }

    /** {@inheritDoc} */
    @Override
    public double getMinY()
    {
        return -this.dy;
    }

    /** {@inheritDoc} */
    @Override
    public double getMaxY()
    {
        return this.dy;
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Point2d point) throws NullPointerException
    {
        return Math.abs(point.x) < this.dx && Math.abs(point.y) < this.dy;
    }

    /** {@inheritDoc} */
    @Override
    public boolean covers(final Point2d point) throws NullPointerException
    {
        return Math.abs(point.x) <= this.dx && Math.abs(point.y) <= this.dy;
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

    /** {@inheritDoc} */
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

}

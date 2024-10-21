package org.opentrafficsim.base.geometry;

import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;

/**
 * Shape defined by an offset rectangle.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OffsetRectangleShape implements OtsShape
{

    /** Minimum x coordinate. */
    private final double minX;

    /** Maximum x coordinate. */
    private final double maxX;

    /** Minimum y coordinate. */
    private final double minY;

    /** Maximum y coordinate. */
    private final double maxY;

    /** Half width. */
    private final double dx;

    /** Half height. */
    private final double dy;

    /** Middle point. */
    private final Point2d midPoint;

    /** Resulting polygon. */
    private Polygon2d polygon;

    /**
     * Constructor.
     * @param minX minimum x coordinate.
     * @param maxX maximum x coordinate.
     * @param minY minimum y coordinate.
     * @param maxY maximum y coordinate.
     */
    public OffsetRectangleShape(final double minX, final double maxX, final double minY, final double maxY)
    {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.dx = (maxX - minX) / 2.0;
        this.dy = (maxY - minY) / 2.0;
        this.midPoint = new Point2d(minX + this.dx, minY + this.dy);
    }

    /** {@inheritDoc} */
    @Override
    public double getMinX()
    {
        return this.minX;
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
        return this.minY;
    }

    /** {@inheritDoc} */
    @Override
    public double getMaxY()
    {
        return this.maxY;
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final double x, final double y)
    {
        return this.minX < x & x < this.maxX & this.minY < y & y < this.maxY;
    }

    /** {@inheritDoc} */
    @Override
    public Point2d midPoint()
    {
        return this.midPoint;
    }

    /** {@inheritDoc} */
    @Override
    public double signedDistance(final Point2d point)
    {
        double qx = Math.abs(point.x - this.midPoint.x) - this.dx;
        double qy = Math.abs(point.y - this.midPoint.y) - this.dy;
        return Math.hypot(Math.max(qx, 0.0), Math.max(qy, 0.0)) + Math.min(Math.max(qx, qy), 0.0);
    }

    /** {@inheritDoc} */
    @Override
    public Polygon2d asPolygon()
    {
        if (this.polygon == null)
        {
            this.polygon = new Polygon2d(new double[] {this.maxX, this.minX, this.minX, this.maxX},
                    new double[] {this.maxY, this.maxY, this.minY, this.minY});
        }
        return this.polygon;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "OffsetRectangleShape [minX=" + this.minX + ", maxX=" + this.maxX + ", minY=" + this.minY + ", maxY=" + this.maxY
                + "]";
    }

}

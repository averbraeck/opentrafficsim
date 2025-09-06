package org.opentrafficsim.base.geometry;

import org.djutils.draw.bounds.Bounds2d;
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
public abstract class OffsetRectangleShape implements OtsShape
{

    /** Bounds. */
    private final Bounds2d bounds;

    /** Middle point relative to location. */
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
        this.bounds = new Bounds2d(minX, maxX, minY, maxY);
        this.midPoint = new Point2d(minX + this.bounds.getDeltaX() / 2.0, minY + this.bounds.getDeltaY() / 2.0);
    }

    @Override
    public Polygon2d getRelativeContour()
    {
        if (this.polygon == null)
        {
            this.polygon = new Polygon2d(
                    new double[] {getRelativeBounds().getMaxX(), getRelativeBounds().getMinX(), getRelativeBounds().getMinX(),
                            getRelativeBounds().getMaxX()},
                    new double[] {getRelativeBounds().getMaxY(), getRelativeBounds().getMaxY(), getRelativeBounds().getMinY(),
                            getRelativeBounds().getMinY()});
        }
        return this.polygon;
    }

    @Override
    public Bounds2d getRelativeBounds()
    {
        return this.bounds;
    }

    @Override
    public double signedDistance(final Point2d point)
    {
        double qx = Math.abs(point.x - this.midPoint.x) - this.bounds.getDeltaX() / 2.0;
        double qy = Math.abs(point.y - this.midPoint.y) - this.bounds.getDeltaY() / 2.0;
        return Math.hypot(Math.max(qx, 0.0), Math.max(qy, 0.0)) + Math.min(Math.max(qx, qy), 0.0);
    }

    @Override
    public String toString()
    {
        return "OffsetRectangleShape [bounds=" + this.bounds + "]";
    }

}

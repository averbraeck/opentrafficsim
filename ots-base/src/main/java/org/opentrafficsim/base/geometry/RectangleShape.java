package org.opentrafficsim.base.geometry;

import org.djutils.draw.point.Point2d;

/**
 * Shape defined by a rectangle.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class RectangleShape extends OffsetRectangleShape
{

    /**
     * Constructor.
     * @param dx complete length along x dimension.
     * @param dy complete length along y dimension.
     */
    public RectangleShape(final double dx, final double dy)
    {
        super(-dx / 2.0, dx / 2.0, -dy / 2.0, dy / 2.0);
    }

    /**
     * {@inheritDoc}
     * @see <a href="https://iquilezles.org/articles/distfunctions/">Signed distance functions by Inigo Quilez</a>
     */
    @Override
    public double signedDistance(final Point2d point)
    {
        double qx = Math.abs(point.x) - getRelativeBounds().getDeltaX() / 2.0;
        double qy = Math.abs(point.y) - getRelativeBounds().getDeltaY() / 2.0;
        return Math.hypot(Math.max(qx, 0.0), Math.max(qy, 0.0)) + Math.min(Math.max(qx, qy), 0.0);
    }

    @Override
    public String toString()
    {
        return "RectangleShape [dx=" + getRelativeBounds().getDeltaX() + ", dy=" + getRelativeBounds().getDeltaY() + "]";
    }

}

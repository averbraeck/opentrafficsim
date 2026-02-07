package org.opentrafficsim.base.geometry;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;

/**
 * Shape defined by a polygon.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class PolygonShape implements OtsShape
{

    /** Polygon. */
    private final Polygon2d polygon;

    /**
     * Constructor.
     * @param polygon polygon.
     */
    public PolygonShape(final Polygon2d polygon)
    {
        this.polygon = polygon;
    }

    @Override
    public Polygon2d getRelativeContour()
    {
        return this.polygon;
    }

    @Override
    public Bounds2d getRelativeBounds()
    {
        return this.polygon.getAbsoluteBounds();
    }

    @Override
    public String toString()
    {
        return "PolygonShape [polygon=" + this.polygon + "]";
    }

}

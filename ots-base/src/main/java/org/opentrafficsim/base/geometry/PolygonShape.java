package org.opentrafficsim.base.geometry;

import org.djutils.draw.line.Polygon2d;

/**
 * Bounds defined by a polygon.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PolygonShape implements OtsShape
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

    /** {@inheritDoc} */
    @Override
    public Polygon2d asPolygon()
    {
        return this.polygon;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "PolygonShape [polygon=" + this.polygon + "]";
    }
    
}

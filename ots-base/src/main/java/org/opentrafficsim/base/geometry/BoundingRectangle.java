package org.opentrafficsim.base.geometry;

import org.djutils.draw.line.Polygon2d;

/**
 * Bounding rectangle; this is a non-centered box.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class BoundingRectangle implements OtsBounds2d
{

    /** Minimum x coordinate. */
    private final double minX;

    /** Maximum x coordinate. */
    private final double maxX;

    /** Minimum y coordinate. */
    private final double minY;

    /** Maximum y coordinate. */
    private final double maxY;

    /** Resulting polygon. */
    private Polygon2d polygon;

    /**
     * Constructor.
     * @param minX double; minimum x coordinate.
     * @param maxX double; maximum x coordinate.
     * @param minY double; minimum y coordinate.
     * @param maxY double; maximum y coordinate.
     */
    public BoundingRectangle(final double minX, final double maxX, final double minY, final double maxY)
    {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
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

}

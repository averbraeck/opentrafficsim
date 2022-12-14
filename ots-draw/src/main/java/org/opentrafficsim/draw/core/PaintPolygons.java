package org.opentrafficsim.draw.core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.core.geometry.OtsPoint3D;

/**
 * Paint a (series of) filled polygon(s) defined as a Path2D.Double
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public final class PaintPolygons
{
    /** Do not instantiate this class. */
    private PaintPolygons()
    {
        // Cannot be instantiated.
    }

    /** Dummy coordinate that forces the drawing operation to start a new path. */
    public static final OtsPoint3D NEWPATH = new OtsPoint3D(Double.NaN, Double.NaN, Double.NaN);

    /**
     * Paint (fill) a polygon or a series of polygons.
     * @param graphics Graphics2D; the graphics environment
     * @param color Color; the color to use
     * @param referencePoint DirectedPoint; the reference point
     * @param line OTSLine3D; array of points
     * @param fill boolean; fill or just contour
     */
    public static void paintMultiPolygon(final Graphics2D graphics, final Color color, final DirectedPoint referencePoint,
            final OtsLine3D line, final boolean fill)
    {
        graphics.setColor(color);
        Path2D.Double path = new Path2D.Double();
        boolean withinPath = false;
        for (OtsPoint3D point : line.getPoints())
        {
            if (NEWPATH.equals(point))
            {
                if (withinPath)
                {
                    path.closePath();
                    if (fill)
                    {
                        graphics.fill(path);
                    }
                }
                path = new Path2D.Double();
                withinPath = false;
            }
            else if (!withinPath)
            {
                withinPath = true;
                path.moveTo(point.x - referencePoint.x, -point.y + referencePoint.y);
            }
            else
            {
                path.lineTo(point.x - referencePoint.x, -point.y + referencePoint.y);
            }
        }
        if (withinPath)
        {
            path.closePath();
            if (fill)
            {
                graphics.fill(path);
            }
            else
            {
                graphics.draw(path);
            }
        }
    }

}

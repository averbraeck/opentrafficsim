package org.opentrafficsim.core.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;

/**
 * Paint a (series of) filled polygon(s) defined as a Path2D.Double
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 10 apr. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class PaintPolygons
{
    /** Do not instantiate this class. */
    private PaintPolygons()
    {
        // Cannot be instantiated.
    }

    /** Dummy coordinate that forces the drawing operation to start a new path. */
    public static final OTSPoint3D NEWPATH = new OTSPoint3D(Double.NaN, Double.NaN, Double.NaN);

    /**
     * Paint (fill) a polygon or a series of polygons.
     * @param graphics Graphics2D; the graphics environment
     * @param color Color; the color to use
     * @param referencePoint DirectedPoint; the reference point
     * @param line array of points
     */
    public static void paintMultiPolygon(final Graphics2D graphics, final Color color, final DirectedPoint referencePoint,
        final OTSLine3D line)
    {
        graphics.setColor(color);
        Path2D.Double path = new Path2D.Double();
        boolean withinPath = false;
        for (OTSPoint3D point : line.getPoints())
        {
            if (NEWPATH.equals(point))
            {
                if (withinPath)
                {
                    path.closePath();
                    graphics.fill(path);
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
        }
        graphics.fill(path);
    }

}

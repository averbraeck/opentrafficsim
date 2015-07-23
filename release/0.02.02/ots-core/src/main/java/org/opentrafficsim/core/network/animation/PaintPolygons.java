package org.opentrafficsim.core.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Paint a (series of) filled polygon(s) defined as a Path2D.Double
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 10 apr. 2015 <br>
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
    public static final Coordinate NEWPATH = new Coordinate(Double.NaN, Double.NaN);

    /**
     * Paint (fill) a polygon or a series of polygons.
     * @param graphics Graphics2D; the graphics environment
     * @param color Color; the color to use
     * @param referencePoint DirectedPoint; the reference point
     * @param coordinates Coordinate[]; array of Coordinate
     */
    public static void paintMultiPolygon(final Graphics2D graphics, final Color color,
            final DirectedPoint referencePoint, final Coordinate[] coordinates)
    {
        graphics.setColor(color);
        Path2D.Double path = new Path2D.Double();
        boolean withinPath = false;
        for (Coordinate c : coordinates)
        {
            if (c == NEWPATH)
            {
                path.closePath();
                graphics.fill(path);
                path = new Path2D.Double();
                withinPath = false;
            }
            else if (!withinPath)
            {
                withinPath = true;
                path.moveTo(c.x - referencePoint.x, -c.y + referencePoint.y);
            }
            else
            {
                path.lineTo(c.x - referencePoint.x, -c.y + referencePoint.y);
            }
        }
        if (withinPath)
        {
            path.closePath();
        }
        graphics.fill(path);
    }

}

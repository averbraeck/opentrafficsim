package org.opentrafficsim.draw.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;

import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Paint a line as a Path2D.Double
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-01-15 09:03:55 +0100 (Fri, 15 Jan 2016) $, @version $Revision: 1698 $, by $Author: averbraeck $,
 * initial version 10 apr. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class PaintLine
{
    /** Do not instantiate this class. */
    private PaintLine()
    {
        // Cannot be instantiated.
    }

    /** Dummy coordinate that forces the drawing operation to start a new path. */
    public static final OTSPoint3D NEWPATH = new OTSPoint3D(Double.NaN, Double.NaN, Double.NaN);

    /**
     * Paint line.
     * @param graphics Graphics2D; the graphics environment
     * @param color Color; the color to use
     * @param width double; the width to use
     * @param referencePoint DirectedPoint; the reference point
     * @param line OTSLine3D; array of points
     */
    public static void paintLine(final Graphics2D graphics, final Color color, final double width,
            final DirectedPoint referencePoint, final OTSLine3D line)
    {
        graphics.setColor(color);
        Stroke oldStroke = graphics.getStroke();
        // Setting cap and join to make perfectly visible where a line begins and ends.
        graphics.setStroke(new BasicStroke((float) width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        Path2D.Double path = new Path2D.Double();
        OTSPoint3D point = line.getFirst();
        path.moveTo(point.x - referencePoint.x, -point.y + referencePoint.y);
        for (int i = 1; i < line.getPoints().length; i++)
        {
            point = line.getPoints()[i];
            path.lineTo(point.x - referencePoint.x, -point.y + referencePoint.y);
        }
        graphics.draw(path);
        graphics.setStroke(oldStroke);
    }

}

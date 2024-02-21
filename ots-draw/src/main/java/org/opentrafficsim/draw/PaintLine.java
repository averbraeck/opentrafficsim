package org.opentrafficsim.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.Point;

/**
 * Paint a line as a Path2D.Double
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class PaintLine
{
    /** Do not instantiate this class. */
    private PaintLine()
    {
        // Cannot be instantiated.
    }

    /**
     * Returns drawable path of the line.
     * @param referencePoint DirectedPoint; the reference point
     * @param line PolyLine2d; array of points
     * @return Path2D.Double drawable path.
     */
    public static Path2D.Double getPath(final Point<?> referencePoint, final PolyLine2d line)
    {
        Path2D.Double path = new Path2D.Double();
        Point<?> point = line.getFirst();
        path.moveTo(point.getX() - referencePoint.getX(), -point.getY() + referencePoint.getY());
        for (int index = 1; index < line.size(); index++)
        {
            path.lineTo(line.getX(index) - referencePoint.getX(), -line.getY(index) + referencePoint.getY());
        }
        return path;
    }
    
    /**
     * Paint line.
     * @param graphics Graphics2D; the graphics environment
     * @param color Color; the color to use
     * @param width double; the width to use
     * @param path Path2D.Double; drawable path
     */
    public static void paintLine(final Graphics2D graphics, final Color color, final double width,
            final Path2D.Double path)
    {
        graphics.setColor(color);
        Stroke oldStroke = graphics.getStroke();
        // Setting cap and join to make perfectly visible where a line begins and ends.
        graphics.setStroke(new BasicStroke((float) width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        graphics.draw(path);
        graphics.setStroke(oldStroke);
    }

}

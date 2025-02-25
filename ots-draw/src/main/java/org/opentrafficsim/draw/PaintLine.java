package org.opentrafficsim.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;

import org.djutils.draw.Transform2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsLocatable;

/**
 * Paint a line as a Path2D.Double
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param line array of points
     * @return Path2D.Float drawable path.
     */
    public static Path2D.Float getPath(final PolyLine2d line)
    {
        return getPath(new DirectedPoint2d(0.0, 0.0, 0.0), line);
    }

    /**
     * Returns drawable path of the line.
     * @param referencePoint the reference point
     * @param line array of points
     * @return Path2D.Float drawable path.
     */
    public static Path2D.Float getPath(final Point2d referencePoint, final PolyLine2d line)
    {
        return getPath(new DirectedPoint2d(referencePoint, 0.0), line);
    }

    /**
     * Returns drawable path of the line.
     * @param referencePoint the reference point
     * @param line array of points
     * @return Path2D.Float drawable path.
     */
    public static Path2D.Float getPath(final DirectedPoint2d referencePoint, final PolyLine2d line)
    {
        Transform2d transform = OtsLocatable.toBoundsTransform(referencePoint);
        Path2D.Float path = new Path2D.Float();
        Point2d p = transform.transform(line.getFirst());
        path.moveTo(p.x, -p.y);
        for (int index = 1; index < line.size(); index++)
        {
            p = transform.transform(line.get(index));
            path.lineTo(p.x, -p.y);
        }
        return path;
    }

    /**
     * Paint line.
     * @param graphics the graphics environment
     * @param color the color to use
     * @param width the width to use
     * @param path Path2D.Float; drawable path
     */
    public static void paintLine(final Graphics2D graphics, final Color color, final double width, final Path2D.Float path)
    {
        graphics.setColor(color);
        Stroke oldStroke = graphics.getStroke();
        // Setting cap and join to make perfectly visible where a line begins and ends.
        graphics.setStroke(new BasicStroke((float) width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        graphics.draw(path);
        graphics.setStroke(oldStroke);
    }

}

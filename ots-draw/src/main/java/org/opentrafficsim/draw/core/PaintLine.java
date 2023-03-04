package org.opentrafficsim.draw.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;

import org.djutils.draw.line.PolyLine3d;
import org.djutils.draw.point.Point;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsPoint3d;

/**
 * Paint a line as a Path2D.Double
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public final class PaintLine
{
    /** Do not instantiate this class. */
    private PaintLine()
    {
        // Cannot be instantiated.
    }

    /** Dummy coordinate that forces the drawing operation to start a new path. */
    public static final OtsPoint3d NEWPATH = new OtsPoint3d(Double.NaN, Double.NaN, Double.NaN);

    /**
     * Paint line.
     * @param graphics Graphics2D; the graphics environment
     * @param color Color; the color to use
     * @param width double; the width to use
     * @param referencePoint DirectedPoint; the reference point
     * @param line OtsLine3d; array of points
     */
    public static void paintLine(final Graphics2D graphics, final Color color, final double width,
            final Point<?> referencePoint, final PolyLine3d line)
    {
        graphics.setColor(color);
        Stroke oldStroke = graphics.getStroke();
        // Setting cap and join to make perfectly visible where a line begins and ends.
        graphics.setStroke(new BasicStroke((float) width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        Path2D.Double path = new Path2D.Double();
        Point<?> point = line.getFirst();
        path.moveTo(point.getX() - referencePoint.getX(), -point.getY() + referencePoint.getY());
        for (int index = 1; index < line.size(); index++)
        {
            path.lineTo(line.getX(index) - referencePoint.getX(), -line.getY(index) + referencePoint.getY());
        }
        graphics.draw(path);
        graphics.setStroke(oldStroke);
    }

    /**
     * Paint line.
     * @param graphics Graphics2D; the graphics environment
     * @param color Color; the color to use
     * @param width double; the width to use
     * @param referencePoint DirectedPoint; the reference point
     * @param line OtsLine3d; array of points
     */
    public static void paintLine(final Graphics2D graphics, final Color color, final double width,
            final Point<?> referencePoint, final OtsLine3d line)
    {
        try
        {
            graphics.setColor(color);
            Stroke oldStroke = graphics.getStroke();
            // Setting cap and join to make perfectly visible where a line begins and ends.
            graphics.setStroke(new BasicStroke((float) width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            Path2D.Double path = new Path2D.Double();
            OtsPoint3d point = line.get(0);
            path.moveTo(point.x - referencePoint.getX(), -point.y + referencePoint.getY());
            for (int index = 1; index < line.size(); index++)
            {
                OtsPoint3d p = line.get(index);
                path.lineTo(p.x - referencePoint.getX(), -p.y + referencePoint.getY());
            }
            graphics.draw(path);
            graphics.setStroke(oldStroke);
        }
        catch (OtsGeometryException e)
        {
            CategoryLogger.always().warn(e);
        }
    }

}

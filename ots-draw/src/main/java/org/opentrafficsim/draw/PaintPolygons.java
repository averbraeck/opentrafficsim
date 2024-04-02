package org.opentrafficsim.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djutils.draw.Transform2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsRenderable;

/**
 * Paint a (series of) filled polygon(s) defined as a Path2D.Double
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class PaintPolygons
{
    /** Do not instantiate this class. */
    private PaintPolygons()
    {
        // Cannot be instantiated.
    }

    /** Dummy coordinate that forces the drawing operation to start a new path. */
    public static final Point2d NEWPATH = null;

    /**
     * Returns drawable paths of a polygon.
     * @param referencePoint Point2d; the reference point
     * @param line List&lt;Point2d&gt;; array of points
     * @return Set&lt;Path2D.Float&gt;; drawable paths.
     */
    public static Set<Path2D.Float> getPaths(final Point2d referencePoint, final List<Point2d> line)
    {
        return getPaths(new OrientedPoint2d(referencePoint, 0.0), line);
    }

    /**
     * Returns drawable paths of a polygon.
     * @param referencePoint Point2d; the reference point
     * @param line List&lt;Point2d&gt;; array of points
     * @return Set&lt;Path2D.Float&gt;; drawable paths.
     */
    public static Set<Path2D.Float> getPaths(final OrientedPoint2d referencePoint, final List<Point2d> line)
    {
        Transform2d transform = OtsRenderable.toBoundsTransform(referencePoint);
        Set<Path2D.Float> paths = new LinkedHashSet<>();
        Path2D.Float path = new Path2D.Float();
        paths.add(path);
        boolean withinPath = false;
        for (Point2d point : line)
        {
            if (point == NEWPATH)
            {
                if (withinPath)
                {
                    path.closePath();
                }
                path = new Path2D.Float();
                paths.add(path);
                withinPath = false;
            }
            else if (!withinPath)
            {
                withinPath = true;
                Point2d p = transform.transform(point);
                path.moveTo(p.x, -p.y);
            }
            else
            {
                Point2d p = transform.transform(point);
                path.lineTo(p.x, -p.y);
            }
        }
        if (withinPath)
        {
            path.closePath();
        }
        return paths;
    }

    /**
     * Returns drawable paths of a polygon.
     * @param line List&lt;Point2d&gt;; array of points
     * @return Set&lt;Path2D.Float&gt;; drawable paths.
     */
    public static Set<Path2D.Float> getPaths(final List<Point2d> line)
    {
        return getPaths(new OrientedPoint2d(0.0, 0.0), line);
    }

    /**
     * Paint (fill) a polygon or a series of polygons.
     * @param graphics Graphics2D; the graphics environment
     * @param color Color; the color to use
     * @param paths Set&lt;Path2D.Float&gt;; drawable paths.
     * @param fill boolean; fill or just contour
     */
    public static void paintPaths(final Graphics2D graphics, final Color color, final Set<Path2D.Float> paths,
            final boolean fill)
    {
        graphics.setColor(color);
        for (Path2D.Float path : paths)
        {
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

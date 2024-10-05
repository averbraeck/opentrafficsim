package org.opentrafficsim.draw;

import java.util.List;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.PolygonShape;

/**
 * Creates bounds that are at least 2m of size in the x and y direction, useful for animation objects a user may click on.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ClickableBounds
{

    /** Minimum distance from center to edge. */
    private static final double R_MIN = 1.0;

    /**
     * Constructor.
     */
    private ClickableBounds()
    {
        // utility class
    }

    /**
     * Creates bounds that are at least 2m of size in the x and y direction.
     * @param bounds actual object bounds.
     * @return bounds that are at least 2m of size in the x and y direction.
     */
    public static Bounds2d get(final Bounds2d bounds)
    {
        if (bounds.getDeltaX() < 2 * R_MIN)
        {
            double x = (bounds.getMinX() + bounds.getMaxX()) / 2.0;
            if (bounds.getDeltaY() < 2 * R_MIN)
            {
                double y = (bounds.getMinY() + bounds.getMaxY()) / 2.0;
                return new Bounds2d(x - R_MIN, x + R_MIN, y - R_MIN, y + R_MIN);
            }
            return new Bounds2d(x - R_MIN, x + R_MIN, bounds.getMinY(), bounds.getMaxY());
        }
        if (bounds.getDeltaY() < 2 * R_MIN)
        {
            double y = (bounds.getMinY() + bounds.getMaxY()) / 2.0;
            return new Bounds2d(bounds.getMinX(), bounds.getMaxX(), y - R_MIN, y + R_MIN);
        }
        return bounds;
    }

    /**
     * Creates bounds that are clickable from a line, generating an area of 2m wide.
     * @param flattenedLine line.
     * @return bounding polygon.
     */
    public static PolygonShape get(final PolyLine2d flattenedLine)
    {
        PolyLine2d left = flattenedLine.offsetLine(R_MIN);
        PolyLine2d right = flattenedLine.offsetLine(-R_MIN);
        List<Point2d> points = left.getPointList();
        points.addAll(right.reverse().getPointList());
        return new PolygonShape(new Polygon2d(points));
    }

}

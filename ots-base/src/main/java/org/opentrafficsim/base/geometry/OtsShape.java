package org.opentrafficsim.base.geometry;

import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;

/**
 * Shape representation of objects that is suitable for continuous spatial 2D algorithms. Default methods use the polygon
 * representation of the shape. For many simple shapes there may be faster methods to determine information.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface OtsShape
{

    /** Standard mid point. */
    Point2d CENTER = new Point2d(0.0, 0.0);

    /** Default number of segments in polygon. */
    int DEFAULT_POLYGON_SEGMENTS = 128;

    /**
     * Return the absolute lower bound for x.
     * @return double; the absolute lower bound for x
     */
    default double getMinX()
    {
        return asPolygon().getBounds().getMinX();
    }

    /**
     * Return the absolute upper bound for x.
     * @return double; the absolute upper bound for x
     */
    default double getMaxX()
    {
        return asPolygon().getBounds().getMaxX();
    }

    /**
     * Return the absolute lower bound for y.
     * @return double; the absolute lower bound for y
     */
    default double getMinY()
    {
        return asPolygon().getBounds().getMinY();
    }

    /**
     * Return the absolute upper bound for y.
     * @return double; the absolute upper bound for y
     */
    default double getMaxY()
    {
        return asPolygon().getBounds().getMaxY();
    }

    /**
     * Return the extent of this Bounds2d in the x-direction.
     * @return double; the extent of this Bounds2d in the x-direction
     */
    default double getDeltaX()
    {
        return getMaxX() - getMinX();
    }

    /**
     * Return the extent of this Bounds2d in the y-direction.
     * @return double; the extent of this Bounds2d in the y-direction
     */
    default double getDeltaY()
    {
        return getMaxY() - getMinY();
    }

    /**
     * Return the mid point of this Bounds object.
     * @return P; the mid point of this Bounds object
     */
    default Point2d midPoint()
    {
        return CENTER;
    }

    /**
     * Check if a point is contained in this OtsShape.
     * @param point the point
     * @return true if the point is within this OtsShape; false if the point is not within this OtsShape. Results may be
     *         ill-defined for points on the edges of this Polygon.
     */
    default boolean contains(final Point2d point)
    {
        return contains(point.x, point.y);
    }

    /**
     * Check if a point is contained in this OtsShape.
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if the point is within this OtsShape; false if the point is not within this OtsShape. Results may be
     *         ill-defined for points on the edges of this Polygon.
     */
    default boolean contains(final double x, final double y)
    {
        // do not use signedDistance(), it uses this function
        return asPolygon().contains(x, y);
    }

    /**
     * Returns a polygon representation of the bounds, such that an intersection can be derived.
     * @return polygon representation of the bounds.
     */
    Polygon2d asPolygon();

    /**
     * Signed distance function. The coordinates must be transformed to this bound's space. Negative distances returned are
     * inside the bounds, with the absolute value of the distance towards the edge. The default implementation is based on the
     * polygon representation and is expensive.
     * @param point point for which distance is returned.
     * @return distance from point to these bounds.
     */
    default double signedDistance(final Point2d point)
    {
        double dist = asPolygon().closestPointOnPolyLine(point).distance(point);
        return contains(point) ? -dist : dist;
    }

}

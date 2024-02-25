package org.opentrafficsim.base.geometry;

import org.djutils.draw.Drawable2d;
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;

/**
 * Bounds for generic usage within the OTS context. All input is assumed to be transformed to this bound's space. Default
 * methods use the polygon representation of the bounds. For many shapes there may be faster methods to determine information.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface OtsBounds2d extends Bounds<OtsBounds2d, Point2d, Drawable2d>
{

    /** Standard mid point. */
    static Point2d CENTER = new Point2d(0.0, 0.0);
    
    /** {@inheritDoc} */
    @Override
    default double getMinX()
    {
        return asPolygon().getBounds().getMinX();
    }

    /** {@inheritDoc} */
    @Override
    default double getMaxX()
    {
        return asPolygon().getBounds().getMaxX();
    }

    /** {@inheritDoc} */
    @Override
    default double getMinY()
    {
        return asPolygon().getBounds().getMinY();
    }

    /** {@inheritDoc} */
    @Override
    default double getMaxY()
    {
        return asPolygon().getBounds().getMaxY();
    }

    /** {@inheritDoc} */
    @Override
    default Point2d midPoint()
    {
        return CENTER;
    }

    /** {@inheritDoc} */
    @Override
    default boolean contains(final Point2d point) throws NullPointerException
    {
        // do not use signedDistance(), it uses this function
        return asPolygon().contains(point);
    }

    /** {@inheritDoc} */
    @Override
    default boolean covers(final Point2d point) throws NullPointerException
    {
        boolean contians = contains(point);
        if (contians)
        {
            return true;
        }
        return asPolygon().closestPointOnPolyLine(point).distance(point) == 0.0;
    }

    /** {@inheritDoc} */
    @Override
    default boolean covers(final Drawable2d drawable) throws NullPointerException
    {
        return contains(drawable);
    }

    /** {@inheritDoc} */
    @Override
    default boolean contains(final Drawable2d drawable) throws NullPointerException
    {
        // TODO: This checks whether the bounding box of the drawable is contained
        return asPolygon().contains(drawable.getBounds());
    }

    /** {@inheritDoc} */
    @Override
    default boolean disjoint(final Drawable2d drawable) throws NullPointerException
    {
        return !asPolygon().intersects(new Polygon2d(drawable.getPointList()));
    }

    /** {@inheritDoc} */
    @Override
    default boolean intersects(final OtsBounds2d otherBounds)
    {
        throw new UnsupportedOperationException("Intersects between bounds is not supported.");
    }

    /** {@inheritDoc} */
    @Override
    default OtsBounds2d intersection(final OtsBounds2d otherBounds)
    {
        throw new UnsupportedOperationException("Intersection between bounds is not supported.");
    }

    /**
     * Returns a polygon representation of the bounds, such that an intersection can be derived.
     * @return Polygon2d; polygon representation of the bounds.
     */
    Polygon2d asPolygon();

    /**
     * Signed distance function. The coordinates must be transformed to this bound's space. Negative distances returned are
     * inside the bounds, with the absolute value of the distance towards the edge. The default implementation is based on the
     * polygon representation and is expensive.
     * @param point Point2d; point for which distance is returned.
     * @return double; distance from point to these bounds.
     */
    default double signedDistance(final Point2d point)
    {
        double dist = asPolygon().closestPointOnPolyLine(point).distance(point);
        return contains(point) ? -dist : dist;
    }

}

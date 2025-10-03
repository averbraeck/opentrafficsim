package org.opentrafficsim.base.geometry;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.draw.Directed;
import org.djutils.draw.Transform2d;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Locatable that provides absolute and relative contours and bounds, both statically and dynamically.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface OtsShape extends Locatable
{

    /** Minimum world margin to click on object line or point objects. */
    double WORLD_MARGIN_LINE = 1.0;

    /** The default intended number of polygon segments to represent continuous shapes. */
    int DEFAULT_POLYGON_SEGMENTS = 128;

    @Override
    DirectedPoint2d getLocation();

    /**
     * Returns the contour of the object in world coordinates.
     * @return the contour of the object in world coordinates
     */
    default Polygon2d getAbsoluteContour()
    {
        Transform2d transform = toAbsoluteTransform(getLocation());
        return new Polygon2d(transform.transform(getRelativeContour().iterator()));
    }

    /**
     * Return the contour of a dynamic object at time 'time' in world coordinates. The default implementation returns the static
     * contour.
     * @param time simulation time for which we want the shape
     * @return the shape of the object at time 'time'
     */
    default Polygon2d getAbsoluteContour(final Duration time)
    {
        return getAbsoluteContour();
    }

    /**
     * Returns the contour of the object in relative coordinates.
     * @return the contour of the object in relative coordinates
     */
    Polygon2d getRelativeContour();

    /**
     * Returns the bounds relative to the location. The default implementation returns the bounds of the contour.
     * @return bounds relative to the location.
     */
    @Override
    default Bounds2d getRelativeBounds()
    {
        return getRelativeContour().getAbsoluteBounds();
    }

    /**
     * Returns whether the point is contained within the shape. The default implementation calculates this based on the contour.
     * @param point point
     * @return whether the point is contained within the shape
     */
    default boolean contains(final Point2d point)
    {
        return signedDistance(point) < 0.0;
    }

    /**
     * Returns whether the point is contained within the shape. The default implementation calculates this based on the contour.
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @return whether the point is contained within the shape
     */
    default boolean contains(final double x, final double y)
    {
        return contains(new Point2d(x, y));
    }

    /**
     * Returns the bounds in world coordinates.
     * @return bounds in world coordinates.
     */
    default Bounds2d getAbsoluteBounds()
    {
        return getAbsoluteContour().getAbsoluteBounds();
    }

    @Override
    default double getDirZ()
    {
        return getLocation().dirZ;
    }

    /**
     * Signed distance function. The point must be relative. Negative distances returned are inside the bounds, with the
     * absolute value of the distance towards the edge. The default implementation is based on the polygon representation, which
     * is expensive.
     * @param point point for which distance is returned.
     * @return distance from point to these bounds.
     */
    default double signedDistance(final Point2d point)
    {
        double dist = getRelativeContour().closestPointOnPolyLine(point).distance(point);
        return getRelativeContour().contains(point) ? -dist : dist;
    }

    /**
     * Signed distance function. The point must be relative. Negative distances returned are inside the bounds, with the
     * absolute value of the distance towards the edge. The default implementation is based on the polygon representation, which
     * is expensive.
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @return distance from point to these bounds.
     */
    default double signedDistance(final double x, final double y)
    {
        return signedDistance(new Point2d(x, y));
    }

    /*
     * The following methods that take an OtsLocatable as input are defined not as default but as static, as these methods
     * should not be exposed as functions of an OtsLocatable.
     */

    /**
     * Generates a polygon as contour based on bounds and location of the locatable.
     * @param locatable locatable
     * @return contour
     */
    static Polygon2d boundsAsAbsoluteContour(final OtsShape locatable)
    {
        return new Polygon2d(toAbsoluteTransform(locatable.getLocation()).transform(locatable.getRelativeBounds().iterator()));
    }

    /**
     * Transform the line by location, which may also be an {@code OrientedPoint} for rotation.
     * @param line line
     * @param location location, which may also be an {@code OrientedPoint} for rotation
     * @return transformed line
     */
    static PolyLine2d transformLine(final PolyLine2d line, final Point2d location)
    {
        return new PolyLine2d(toRelativeTransform(location).transform(line.iterator()));
    }

    /**
     * Returns a transformation by which absolute coordinates can be translated and rotated to the frame of the possibly
     * oriented location around which bounds are defined.
     * @param location location (can be an {@code Oriented}).
     * @return transformation.
     */
    static Transform2d toRelativeTransform(final Point2d location)
    {
        Transform2d transformation = new Transform2d();
        if (location instanceof Directed dir)
        {
            transformation.rotation(-dir.getDirZ());
        }
        transformation.translate(-location.getX(), -location.getY());
        return transformation;
    }

    /**
     * Returns a transformation by which relative coordinates can be translated and rotated to the frame of the possibly
     * oriented location around which bounds are defined.
     * @param location location (can be an {@code Oriented}).
     * @return transformation.
     */
    static Transform2d toAbsoluteTransform(final Point2d location)
    {
        Transform2d transformation = new Transform2d();
        transformation.translate(location.getX(), location.getY());
        if (location instanceof Directed dir)
        {
            transformation.rotation(dir.getDirZ());
        }
        return transformation;
    }

}

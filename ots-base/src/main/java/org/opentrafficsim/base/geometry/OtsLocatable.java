package org.opentrafficsim.base.geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.djutils.draw.Transform2d;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Locatable that specifies return types.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface OtsLocatable extends Locatable, SpatialObject
{

    /** {@inheritDoc} */
    @Override
    Point2d getLocation();

    /**
     * Returns the bounds relative to the location.
     * @return bounds relative to the location.
     */
    @Override
    Bounds2d getBounds();

    /**
     * Returns the shape relative to the location.
     * @return the shape relative to the location.
     */
    default OtsShape getShape()
    {
        return new PolygonShape(relativeContour(this));
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
    static Polygon2d boundsAsContour(final OtsLocatable locatable)
    {
        return new Polygon2d(transform(locatable.getBounds().getPoints(), locatable.getLocation()));
    }

    /**
     * Generates bounds based on polygon and location of the locatable.
     * @param locatable locatable
     * @return bounds
     */
    static Bounds2d contourAsBounds(final OtsLocatable locatable)
    {
        return relativeContour(locatable).getBounds();
    }

    /**
     * Returns the contour of the locatable translated and rotated such that it is defined relative to the location.
     * @param locatable locatable
     * @return contour relative to location
     */
    static Polygon2d relativeContour(final OtsLocatable locatable)
    {
        return transformContour(locatable.getContour(), locatable.getLocation());
    }

    /**
     * Transform the contour by location, which may also be an {@code OrientedPoint} for rotation.
     * @param contour contour
     * @param location location, which may also be an {@code OrientedPoint} for rotation
     * @return transformed contour
     */
    static Polygon2d transformContour(final Polygon2d contour, final Point2d location)
    {
        return new Polygon2d(transform(contour.getPoints(), location));
    }

    /**
     * Transform the line by location, which may also be an {@code OrientedPoint} for rotation.
     * @param line line
     * @param location location, which may also be an {@code OrientedPoint} for rotation
     * @return transformed line
     */
    static PolyLine2d transformLine(final PolyLine2d line, final Point2d location)
    {
        return new PolyLine2d(transform(line.getPoints(), location));
    }

    /**
     * Translates and possibly rotates points by location.
     * @param itSource points
     * @param location location
     * @return translated and possibly rotated contour
     */
    private static List<Point2d> transform(final Iterator<Point2d> itSource, final Point2d location)
    {
        Transform2d transformation = OtsRenderable.toBoundsTransform(location);
        Point2d prev = null;
        List<Point2d> points = new ArrayList<>();
        while (itSource.hasNext())
        {
            Point2d next = transformation.transform(itSource.next());
            if (!next.equals(prev))
            {
                points.add(next);
            }
            prev = next;
        }
        return points;
    }

}

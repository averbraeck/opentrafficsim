package org.opentrafficsim.base.geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.djutils.draw.Transform2d;
import org.djutils.draw.bounds.Bounds2d;
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
     * Generates a polygon as contour based on bounds and location of the locatable.
     * @param locatable locatable
     * @return contour
     */
    static Polygon2d asPolygon(final OtsLocatable locatable)
    {
        Transform2d transformation = OtsRenderable.toBoundsTransform(locatable.getLocation());
        Iterator<Point2d> itSource = locatable.getBounds().getPoints();
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
        return new Polygon2d(points);
    }

    /**
     * Generates bounds based on polygon and location of the locatable.
     * @param locatable locatable
     * @return bounds
     */
    static Bounds2d asBounds(final OtsLocatable locatable)
    {
        Transform2d transformation = OtsRenderable.toBoundsTransform(locatable.getLocation());
        Iterator<Point2d> itSource = locatable.getContour().getPoints();
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
        return new Polygon2d(points).getBounds();
    }
    
    /**
     * Returns the contour of the locatable translated and rotated such that it is defined relative to the location.
     * @param locatable locatable
     * @return contour relative to location
     */
    static Polygon2d relativeContour(final OtsLocatable locatable)
    {
        Transform2d transformation = OtsRenderable.toBoundsTransform(locatable.getLocation());
        Iterator<Point2d> itSource = locatable.getContour().getPoints();
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
        return new Polygon2d(points);
    }

}

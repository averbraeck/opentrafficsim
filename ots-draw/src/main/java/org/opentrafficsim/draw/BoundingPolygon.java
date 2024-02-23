package org.opentrafficsim.draw;

import org.djutils.draw.Drawable2d;
import org.djutils.draw.Transform2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;

/**
 * Bounds defined by a polygon.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class BoundingPolygon implements TransformableBounds<BoundingPolygon>
{

    /** Polygon. */
    private final Polygon2d polygon;

    /**
     * Constructor.
     * @param polygon Polygon2d; polygon.
     */
    public BoundingPolygon(final Polygon2d polygon)
    {
        this.polygon = polygon;
    }

    /** {@inheritDoc} */
    @Override
    public double getMinX()
    {
        return this.polygon.getBounds().getMinX();
    }

    /** {@inheritDoc} */
    @Override
    public double getMaxX()
    {
        return this.polygon.getBounds().getMaxX();
    }

    /** {@inheritDoc} */
    @Override
    public double getMinY()
    {
        return this.polygon.getBounds().getMinY();
    }

    /** {@inheritDoc} */
    @Override
    public double getMaxY()
    {
        return this.polygon.getBounds().getMaxY();
    }

    /** {@inheritDoc} */
    @Override
    public Point2d midPoint()
    {
        return this.polygon.getBounds().midPoint();
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Point2d point) throws NullPointerException
    {
        return this.polygon.contains(point);
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Drawable2d drawable) throws NullPointerException
    {
        // This checks whether the boundingbox of the drawable is contained
        return this.polygon.contains(drawable.getBounds());
    }

    /** {@inheritDoc} */
    @Override
    public boolean covers(final Point2d point) throws NullPointerException
    {
        return contains(point);
    }

    /** {@inheritDoc} */
    @Override
    public boolean covers(final Drawable2d drawable) throws NullPointerException
    {
        return contains(drawable);
    }

    /** {@inheritDoc} */
    @Override
    public boolean disjoint(final Drawable2d drawable) throws NullPointerException
    {
        return !this.polygon.intersects(new Polygon2d(drawable.getPointList()));
    }

    /** {@inheritDoc} */
    @Override
    public boolean intersects(final BoundingPolygon otherBounds)
    {
        return this.polygon.intersects(otherBounds.polygon);
    }

    /** {@inheritDoc} */
    @Override
    public BoundingPolygon intersection(final BoundingPolygon otherBounds)
    {
        throw new UnsupportedOperationException("Intersection of two bounding polygons is not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public BoundingPolygon transform(final Transform2d transformation)
    {
        return new BoundingPolygon(new Polygon2d(transformation.transform(this.polygon.getPoints())));
    }

}

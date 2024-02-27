package org.opentrafficsim.base.geometry;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

/**
 * Bounds defined by a circle.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class BoundingCircle implements OtsBounds2d
{

    /** Number of line segments in polygon representation. */
    private final static int POLYGON_STEPS = 128;

    /** Radius. */
    private final double radius;

    /** Polygon representation. */
    private Polygon2d polygon;

    /**
     * Constructor.
     * @param radius double; radius.
     */
    public BoundingCircle(final double radius)
    {
        Throw.whenNull(radius, "Radius must not be null.");
        Throw.when(radius <= 0.0, IllegalArgumentException.class, "Radius must be above 0.0.");
        this.radius = radius;
    }

    /** {@inheritDoc} */
    @Override
    public double getMinX()
    {
        return -this.radius;
    }

    /** {@inheritDoc} */
    @Override
    public double getMaxX()
    {
        return this.radius;
    }

    /** {@inheritDoc} */
    @Override
    public double getMinY()
    {
        return -this.radius;
    }

    /** {@inheritDoc} */
    @Override
    public double getMaxY()
    {
        return this.radius;
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Point2d point) throws NullPointerException
    {
        return CENTER.distance(point) < this.radius;
    }

    /** {@inheritDoc} */
    @Override
    public boolean covers(final Point2d point) throws NullPointerException
    {
        return CENTER.distance(point) <= this.radius;
    }

    /** {@inheritDoc} */
    @Override
    public double signedDistance(final Point2d point)
    {
        return CENTER.distance(point) - this.radius;
    }

    /** {@inheritDoc} */
    @Override
    public Polygon2d asPolygon()
    {
        if (this.polygon == null)
        {
            if (this.radius == 0.0)
            {
                this.polygon = new Polygon2d(false, new Point2d(0.0, 0.0));
            }
            else
            {
                this.polygon = new Polygon2d(new Iterator<Point2d>()
                {
                    /** Step. */
                    private int step = 0;

                    /** {@inheritDoc} */
                    @Override
                    public boolean hasNext()
                    {
                        return this.step <= POLYGON_STEPS;
                    }

                    /** {@inheritDoc} */
                    @Override
                    public Point2d next()
                    {
                        Throw.when(!hasNext(), NoSuchElementException.class, "Iterator has no more elements.");
                        // at full circle (this.step == POLYGON_STEPS), make sure end point is exactly the same as the start
                        // point
                        double ang = this.step == POLYGON_STEPS ? 0.0 : (2.0 * Math.PI * this.step) / POLYGON_STEPS;
                        this.step++;
                        return new Point2d(Math.sin(ang) * BoundingCircle.this.radius,
                                Math.sin(ang) * BoundingCircle.this.radius);
                    }
                });
            }
        }
        return this.polygon;
    }

}

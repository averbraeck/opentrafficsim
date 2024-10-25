package org.opentrafficsim.base.geometry;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

/**
 * Shape defined by a circle.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CircleShape implements OtsShape
{

    /** Radius. */
    private final double radius;

    /** Number of line segments in polygon representation. */
    private final int polygonSegments;

    /** Polygon representation. */
    private Polygon2d polygon;

    /**
     * Constructor.
     * @param radius radius.
     */
    public CircleShape(final double radius)
    {
        this(radius, DEFAULT_POLYGON_SEGMENTS);
    }

    /**
     * Constructor.
     * @param radius radius.
     * @param polygonSegments number of segments in polygon representation.
     */
    public CircleShape(final double radius, final int polygonSegments)
    {
        Throw.whenNull(radius, "Radius must not be null.");
        Throw.when(radius <= 0.0, IllegalArgumentException.class, "Radius must be above 0.0.");
        this.radius = radius;
        this.polygonSegments = polygonSegments;
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
    public boolean contains(final double x, final double y)
    {
        return contains(new Point2d(x, y));
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

                    @Override
                    public boolean hasNext()
                    {
                        return this.step <= CircleShape.this.polygonSegments;
                    }

                    @Override
                    public Point2d next()
                    {
                        Throw.when(!hasNext(), NoSuchElementException.class, "Iterator has no more elements.");
                        // at full circle (this.step == polygonSegments), make sure end point is exactly the same as the start
                        // point
                        double ang = this.step == CircleShape.this.polygonSegments ? 0.0
                                : (2.0 * Math.PI * this.step) / CircleShape.this.polygonSegments;
                        this.step++;
                        return new Point2d(Math.cos(ang) * CircleShape.this.radius, Math.sin(ang) * CircleShape.this.radius);
                    }
                });
            }
        }
        return this.polygon;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "CircleShape [radius=" + this.radius + "]";
    }

}

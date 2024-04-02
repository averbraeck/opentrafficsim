package org.opentrafficsim.base.geometry;

import java.util.Iterator;

import org.djutils.draw.Transform2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;

/**
 * Bounds defined by a polygon.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class BoundingPolygon implements OtsBounds2d
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
    public Polygon2d asPolygon()
    {
        return this.polygon;
    }

    /**
     * Translates absolute geometry to bounds relative to location, including rotation.
     * @param location OrientedPoint2d; location.
     * @param geometry PolyLine2d; geometry..
     * @return BoundingPolygon; bounded polygon.
     */
    public static BoundingPolygon geometryToBounds(final OrientedPoint2d location, final PolyLine2d geometry)
    {
        Transform2d transformation = OtsRenderable.toBoundsTransform(location);
        Iterator<Point2d> itSource = geometry.getPoints();
        Iterator<Point2d> itTarget = new Iterator<>()
        {
            /** {@inheritDoc} */
            @Override
            public boolean hasNext()
            {
                return itSource.hasNext();
            }

            /** {@inheritDoc} */
            @Override
            public Point2d next()
            {
                return transformation.transform(itSource.next());
            }
        };
        BoundingPolygon b = new BoundingPolygon(new Polygon2d(itTarget));
        return b;
    }

}

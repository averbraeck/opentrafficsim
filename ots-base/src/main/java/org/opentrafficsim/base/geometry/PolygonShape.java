package org.opentrafficsim.base.geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
public class PolygonShape implements OtsShape
{

    /** Polygon. */
    private final Polygon2d polygon;

    /**
     * Constructor.
     * @param polygon polygon.
     */
    public PolygonShape(final Polygon2d polygon)
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
     * @param location location.
     * @param geometry geometry..
     * @return bounded polygon.
     */
    public static PolygonShape geometryToBounds(final OrientedPoint2d location, final PolyLine2d geometry)
    {
        Transform2d transformation = OtsRenderable.toBoundsTransform(location);
        Iterator<Point2d> itSource = geometry.getPoints();
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
        PolygonShape b = new PolygonShape(new Polygon2d(points));
        return b;
    }

}

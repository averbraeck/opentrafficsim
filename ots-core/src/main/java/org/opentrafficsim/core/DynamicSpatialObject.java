package org.opentrafficsim.core;

import java.util.Iterator;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.Transform2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.core.geometry.OtsGeometryException;

/**
 * DynamicSpatialObject has two shapes: the shape that is registered in the Map is the shape that indicates where the object
 * <b>can be</b> present for the time frame for which intersections can be calculated. This is, for instance, a contour that
 * indicates the Minkowski sum of the locations of a GTU for the next time step. This contour IS registered in the spatial tree.
 * The getShape(time), however, provides the exact shape of the GTU at a fixed time instant. This contour is NOT registered in
 * the spatial tree.
 * <p>
 * Copyright (c) 2022-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface DynamicSpatialObject extends SpatialObject
{
    /**
     * Return the shape of a dynamic object at time 'time'. Note that the getShape() method without a time returns the Minkowski
     * sum of all shapes of the spatial object for a validity time window, e.g., a contour that describes all locations of a GTU
     * for the next time step, i.e., the contour of the GTU belonging to the next operational plan.
     * @param time Time; the time for which we want the shape
     * @return OtsShape; the shape of the object at time 'time'
     */
    Polygon2d getShape(Time time);

    /**
     * Return the contour of the dynamic object at the right position and in the right direction.
     * @param shape OtsShape; the shape to translate and rotate for point p
     * @param p OrientedPoint2d; the location and direction of the reference point of the object
     * @return Polygon2d; the contour of the dynamic object at the right position and in the right direction
     * @throws OtsGeometryException on invalid geometry after transformation
     */
    default Polygon2d transformShape(final Polygon2d shape, final OrientedPoint2d p) throws OtsGeometryException
    {
        Transform2d transform = new Transform2d();
        transform.translate(p.x, p.y);
        transform.rotation(p.getDirZ());
        Point2d[] points = new Point2d[shape.size()];
        int i = 0;
        Iterator<Point2d> iterator = shape.getPoints();
        while (iterator.hasNext())
        {
            Point2d sp = iterator.next();
            double[] point = {sp.x, sp.y};
            double[] t = transform.transform(point);
            points[i++] = new Point2d(t[0], t[1]);
        }
        return new Polygon2d(points);
    }

}

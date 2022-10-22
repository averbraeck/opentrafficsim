package org.opentrafficsim.core;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.Transform2d;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.geometry.OtsShape;

/**
 * DynamicSpatialObject has two shapes: the shape that is registered in the Map is the shape that indicates where the object
 * <b>can be</b> present for the time frame for which intersections can be calculated. This is, for instance, a contour that
 * indicates the Minkowski sum of the locations of a GTU for the next time step. This contour IS registered in the spatial tree.
 * The getShape(time), however, provides the exact shape of the GTU at a fixed time instant. This contour is NOT registered in
 * the spatial tree.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> The HierarchicalType of the typing object
 * @param <I> The type of the typed object
 */
public interface DynamicSpatialObject<T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I>>
        extends SpatialObject<T, I>
{
    /**
     * Return the shape of a dynamic object at time 'time'. Note that the getShape() method without a time returns the Minkowski
     * sum of all shapes of the spatial object for a validity time window, e.g., a contour that describes all locations of a GTU
     * for the next time step, i.e., the contour of the GTU belonging to the next operational plan.
     * @param time Time; the time for which we want the shape
     * @return OtsShape; the shape of the object at time 'time'
     */
    OtsShape getShape(Time time);
    
    /**
     * Return the contour of the dynamic object at the right position and in the right direction.
     * @param shape OtsShape; the shape to translate and rotate for point p
     * @param p DirectedPoint; the location and direction of the reference point of the object
     * @return Polygon2d; the contour of the dynamic object at the right position and in the right direction
     * @throws OtsGeometryException on invalid geometry after transformation
     */
    default OtsShape transformShape(final OtsShape shape, final DirectedPoint p) throws OtsGeometryException
    {
        Transform2d transform = new Transform2d();
        transform.rotation(p.getRotZ());
        transform.translate(p.x, p.y);
        OtsPoint3D[] points = new OtsPoint3D[shape.size()];
        int i = 0;
        for (OtsPoint3D sp : shape.getPoints())
        {
            double[] point = {sp.x, sp.y};
            double[] t = transform.transform(point);
            points[i] = new OtsPoint3D(t[0], t[1], sp.z);
        }
        return new OtsShape(points);
    }

}

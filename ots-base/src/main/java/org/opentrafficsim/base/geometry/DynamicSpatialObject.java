package org.opentrafficsim.base.geometry;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.line.Polygon2d;

/**
 * DynamicSpatialObject has two shapes: the shape that is registered in the Map is the shape that indicates where the object
 * <b>can be</b> present for the time frame for which intersections can be calculated. This is, for instance, a contour that
 * indicates the Minkowski sum of the locations of a GTU for the next time step. This contour IS registered in the spatial tree.
 * The getShape(time), however, provides the exact shape of the GTU at a fixed time instant. This contour is NOT registered in
 * the spatial tree.
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface DynamicSpatialObject extends SpatialObject
{

    /**
     * Return the contour of a dynamic object at time 'time' in world coordinates. Note that the getContour() method without a
     * time returns the Minkowski sum of all shapes of the spatial object for a validity time window, e.g., a contour that
     * describes all locations of a GTU for the next time step, i.e., the contour of the GTU belonging to the next operational
     * plan.
     * @param time the time for which we want the shape
     * @return the shape of the object at time 'time'
     */
    Polygon2d getContour(Time time);

}

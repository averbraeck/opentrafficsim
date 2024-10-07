package org.opentrafficsim.base.geometry;

import org.djutils.draw.line.Polygon2d;

/**
 * SpatialObject indicates that an object has a contour that can be requested. A spatial object can therefore be stored in a
 * spatial tree such as an R-Tree.
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface SpatialObject
{

    /**
     * Returns the contour of the object in world coordinates.
     * @return the contour of the object in world coordinates
     */
    Polygon2d getContour();

}

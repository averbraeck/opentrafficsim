package org.opentrafficsim.core;

import org.opentrafficsim.core.geometry.OtsShape;

/**
 * SpatialObject indicates that an object has a shape that can be requested. A spatial object can therefore be stored in a
 * spatial tree such as an R-Tree.
 * <p>
 * Copyright (c) 2022-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface SpatialObject
{
    /**
     * Return the shape of the object.
     * @return OtsShape; the shape of the object
     */
    OtsShape getShape();

}

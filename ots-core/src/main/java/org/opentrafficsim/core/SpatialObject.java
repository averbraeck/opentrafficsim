package org.opentrafficsim.core;

import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.core.geometry.OtsShape;

/**
 * SpatialObject.java.
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
public interface SpatialObject<T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I>> extends HierarchicallyTyped<T, I>
{
    /**
     * Return the shape of the object.
     * @return OtsShape; the shape of the object
     */
    OtsShape getShape();

}

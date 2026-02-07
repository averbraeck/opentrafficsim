package org.opentrafficsim.core.object;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.base.Identifiable;
import org.djutils.event.EventProducer;
import org.opentrafficsim.base.geometry.OtsShape;

/**
 * Generic object that can be placed in the model. This could be implemented for a traffic light, a road sign, or an obstacle.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public interface LocatedObject extends OtsShape, Identifiable, EventProducer
{

    /**
     * Returns the object height.
     * @return the height of the object (can be Length.ZERO).
     */
    Length getHeight();

    /**
     * Returns the full id that makes the id unique in the network.
     * @return the full id that makes the id unique in the network.
     */
    String getFullId();

}

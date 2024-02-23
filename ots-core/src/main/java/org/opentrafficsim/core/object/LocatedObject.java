package org.opentrafficsim.core.object;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.base.Identifiable;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.event.EventProducer;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Generic object that can be placed in the model. This could be implemented for a traffic light, a road sign, or an obstacle.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public interface LocatedObject extends Locatable, Identifiable, EventProducer, Serializable
{
    
    /** @return the outline geometry of the object. */
    PolyLine2d getGeometry();

    /** @return the height of the object (can be Length.ZERO). */
    Length getHeight();

    /** @return the full id that makes the id unique in the network. */
    String getFullId();

    @Override
    Bounds2d getBounds();

}

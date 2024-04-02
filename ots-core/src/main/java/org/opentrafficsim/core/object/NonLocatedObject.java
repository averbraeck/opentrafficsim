package org.opentrafficsim.core.object;

import java.io.Serializable;

import org.djutils.base.Identifiable;
import org.opentrafficsim.core.animation.Drawable;

/**
 * Interface for objects that live in a Network, but cannot be drawn and which do not have a specific location. These objects do
 * have a name and need to be cloned when the Network is cloned. <br>
 * Example: TrafficLightController.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface NonLocatedObject extends Identifiable, Drawable, Serializable
{

    /** @return the full id that makes the id unique in the network. */
    String getFullId();

}

package org.opentrafficsim.core.object;

import java.io.Serializable;

import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.animation.Drawable;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Interface for objects that live in a Network, but cannot be drawn and which do not have a specific location. These objects do
 * have a name and need to be cloned when the Network is cloned. <br>
 * Example: TrafficLightController.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface InvisibleObjectInterface extends Identifiable, Drawable, Serializable
{

    /**
     * Duplicate the invisibleObject in a new simulator and network.
     * @param newSimulator OTSSimulatorInterface; the new simulator
     * @param newNetwork Network; the new network
     * @return InvisibleObjectInterface; clone of this, but living in the new network and simulator
     * @throws NetworkException when the new Network is not compatible
     */
    InvisibleObjectInterface clone(OTSSimulatorInterface newSimulator, Network newNetwork) throws NetworkException;

    /** @return the full id that makes the id unique in the network. */
    String getFullId();

}

package org.opentrafficsim.draw.core;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;

/**
 * This interface extends the animation objects with an option to clone them for a new source on a new Simulator.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> the Locatable class of the source that indicates the location of the Renderable on the screen
 */
public interface ClonableRenderable2DInterface<T extends Locatable> extends Renderable2DInterface<T>
{
    /**
     * Clone the animation object and register it for the new source on the new simulation.
     * @param newSource T; the source
     * @param newSimulator OTSSimulatorInterface; the simulator
     * @return the generated clone
     * @throws NamingException when animation context cannot be created or retrieved
     * @throws RemoteException - when remote context cannot be found
     */
    ClonableRenderable2DInterface<T> clone(T newSource, OTSSimulatorInterface newSimulator)
            throws NamingException, RemoteException;

}

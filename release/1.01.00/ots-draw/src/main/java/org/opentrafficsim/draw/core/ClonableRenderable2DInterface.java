package org.opentrafficsim.draw.core;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * This interface extends the animation objects with an option to clone them for a new source on a new Simulator.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jan 15, 2017 <br>
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
     * @param newSimulator SimulatorInterface.TimeDoubleUnit; the simulator
     * @return the generated clone
     * @throws NamingException when animation context cannot be created or retrieved
     * @throws RemoteException - when remote context cannot be found
     */
    ClonableRenderable2DInterface<T> clone(T newSource, SimulatorInterface.TimeDoubleUnit newSimulator)
            throws NamingException, RemoteException;

}

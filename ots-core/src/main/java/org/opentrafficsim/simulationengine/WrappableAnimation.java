package org.opentrafficsim.simulationengine;

import java.awt.Rectangle;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;

/**
 * Requirements for demonstration that can be shown in the SuperDemo.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-23 12:51:29 +0200 (Sun, 23 Aug 2015) $, @version $Revision: 1293 $, by $Author: averbraeck $,
 * initial version 17 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface WrappableAnimation extends WrappableSimulation
{
    /**
     * Build the simulation.
     * @param properties ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the (possibly user-modified) properties. This list must
     *            contain all the properties returned by getProperties(); any additional properties may be ignored
     * @param rect the x, y, width and height for the window to rebuild. Use null for maximized screen.
     * @param exitOnClose Use EXIT_ON_CLOSE when true, DISPOSE_ON_CLOSE when false on closing of the window.
     * @return SimpleSimulation; the new simulation
     * @throws RemoteException on communications failure
     * @throws SimRuntimeException on ???
     * @throws NetworkException on Network inconsistency
     * @throws NamingException when context for the animation cannot be created
     */
    SimpleSimulatorInterface buildAnimator(ArrayList<AbstractProperty<?>> properties, Rectangle rect, boolean exitOnClose)
        throws SimRuntimeException, RemoteException, NetworkException, NamingException;

    /**
     * Restart (rebuild) the simulation.
     * @param rect the x, y, width and height for the window to rebuild. Use null for maximized screen.
     * @return SimpleSimulation; the new simulation
     * @throws RemoteException on communications failure
     * @throws SimRuntimeException on ???
     * @throws NetworkException on Network inconsistency
     * @throws NamingException when context for the animation cannot be created
     */
    SimpleSimulatorInterface rebuildSimulator(Rectangle rect) throws SimRuntimeException, RemoteException, NetworkException,
        NamingException;

    /**
     * Retrieve a list of properties as the user has modified them.
     * @return ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the list of visible properties
     */
    ArrayList<AbstractProperty<?>> getUserModifiedProperties();

    /**
     * Stop the timers and threads that are connected when disposing of this wrappable simulation.
     */
    void stopTimersThreads();
}

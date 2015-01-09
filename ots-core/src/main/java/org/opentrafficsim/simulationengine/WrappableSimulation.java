package org.opentrafficsim.simulationengine;

import java.rmi.RemoteException;
import java.util.ArrayList;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Requirements for demonstration that can be shown in the SuperDemo.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 17 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface WrappableSimulation
{
    /**
     * Build the simulation.
     * @param properties ArrayList&lt;AbstractProperty&lt;?&gt;*gt;; the (possibly user-modified) properties. This list
     *            must contain all the properties returned by getProperties(); any additional properties may be ignored
     * @return SimpleSimulator; the new simulation.
     * @throws RemoteException on communications failure
     * @throws SimRuntimeException on ???
     */
    SimpleSimulator buildSimulator(ArrayList<AbstractProperty<?>> properties) throws SimRuntimeException,
            RemoteException;

    /**
     * Return a very short description of the simulation.
     * @return String; short description of the simulation
     */
    String shortName();

    /**
     * Return a description of the simulation (HTML formatted).
     * @return String; HTML text describing the simulation
     */
    String description();

    /**
     * Retrieve a list of visible properties of the simulation. <br>
     * The caller can modify the returned result. If the internal format is also an ArrayList it is highly recommended to make a
     * protective copy and return that.
     * @return ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the list of visible properties
     */
    ArrayList<AbstractProperty<?>> getProperties();
}

package org.opentrafficsim.core.gtu;

import java.io.Serializable;
import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;

import org.opentrafficsim.core.location.Location;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID> The type of ID, e.g., String or Integer
 * @param <L> The type of Location, e.g., Location1D
 * @param <V> The type of Velocity, could be in 1D (relative), 2D or 3D
 */
public interface GTU<ID, L extends Location, V /* extends Velocity */> extends LocatableInterface, Serializable
{
    /** @return the id of the GTU, could be String or Integer */
    ID getID();

    /* @return the location of the GTU, could e.g. be (x,y) or (lat,lon), or relative */
    // L getLocation();

    /** @return the velocity of the GTU, in a space-per-time unit, could e.g. be 1D, 2D or 3D 
     * @throws RemoteException */
    V getVelocity() throws RemoteException;
}

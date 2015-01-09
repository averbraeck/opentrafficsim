package org.opentrafficsim.graphs;

import java.rmi.RemoteException;

import org.opentrafficsim.core.gtu.AbstractLaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Interface implemented by graphs that sample movements of a lane-based GTU.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 5 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface LaneBasedGTUSampler
{
    /**
     * Add the movement of an AbstractLaneBasedGTU to a graph.
     * @param gtu AbstractLaneBasedGTU&lt;?&gt;; the AbstractLaneBasedGTU
     * @throws RemoteException on communications failure
     */
    public void addData(AbstractLaneBasedGTU<?> gtu) throws RemoteException, NetworkException;
    
    /**
     * Force redraw of the graph.
     */
    public void reGraph();
}

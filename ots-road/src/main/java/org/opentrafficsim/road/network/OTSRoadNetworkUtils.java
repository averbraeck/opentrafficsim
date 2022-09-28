package org.opentrafficsim.road.network;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

/**
 * OTSRoadNetworkCloner makes a deep clone of a network.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class OTSRoadNetworkUtils
{
    /** */
    private OTSRoadNetworkUtils()
    {
        // utility class
    }

    /**
     * Remove all objects and animation in the road network.
     * @param network OTSRoadNetwork; the network to destroy
     * @param simulator OTSSimulatorInterface; the simulator of the old network
     */
    public static void destroy(final OTSRoadNetwork network, final OTSSimulatorInterface simulator)
    {
        OTSRoadNetworkUtils.destroy(network, simulator);
    }

}

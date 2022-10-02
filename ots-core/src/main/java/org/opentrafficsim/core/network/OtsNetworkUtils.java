package org.opentrafficsim.core.network;

import org.opentrafficsim.core.gtu.Gtu;

/**
 * OTSNetworkCloner makes a deep clone of a network.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class OTSNetworkUtils
{
    /** */
    private OTSNetworkUtils()
    {
        // utility class
    }

    /**
     * Remove all objects and animation in the network.
     * @param network OTSNetwork; the network to destroy
     */
    public static void destroy(final OTSNetwork network)
    {
        for (Gtu gtu : network.getGTUs())
        {
            gtu.destroy();
        }

        network.getRawNodeMap().clear();
        network.getRawLinkMap().clear();
        network.getRawLinkGraphs().clear();
        network.getRawRouteMap().clear();
    }

}

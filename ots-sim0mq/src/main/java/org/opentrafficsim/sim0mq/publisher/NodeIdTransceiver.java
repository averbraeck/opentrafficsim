package org.opentrafficsim.sim0mq.publisher;

import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.Network;

/**
 * Transceiver for Node ids.
 * <p>
 * Copyright (c) 2020-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class NodeIdTransceiver extends AbstractIdTransceiver
{
    /**
     * Construct a new LinkIdTransceiver.
     * @param network the OTS network
     */
    public NodeIdTransceiver(final Network network)
    {
        super(network, "Node id transceiver");
    }

    @Override
    public String toString()
    {
        return "NodeIdTransceiver [network=" + ", super=" + super.toString() + "]";
    }

    @Override
    ImmutableSet<Node> getSet()
    {
        return (ImmutableSet<Node>) getNetwork().getNodeMap().values();
    }

}

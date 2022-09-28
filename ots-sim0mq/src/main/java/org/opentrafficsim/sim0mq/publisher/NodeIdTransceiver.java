package org.opentrafficsim.sim0mq.publisher;

import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * Transceiver for Node ids.
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class NodeIdTransceiver extends AbstractIdTransceiver
{
    /**
     * Construct a new LinkIdTransceiver.
     * @param network OTSNetwork; the OTS network
     */
    public NodeIdTransceiver(final OTSNetwork network)
    {
        super(network, "Node id transceiver");
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "NodeIdTransceiver [network=" + ", super=" + super.toString() + "]";
    }

    /** {@inheritDoc} */
    @Override
    ImmutableSet<Node> getSet()
    {
        return (ImmutableSet<Node>) getNetwork().getNodeMap().values();
    }

}

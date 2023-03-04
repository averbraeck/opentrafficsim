package org.opentrafficsim.sim0mq.publisher;

import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.OtsNetwork;

/**
 * Transceiver for Link ids.
 * <p>
 * Copyright (c) 2020-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LinkIdTransceiver extends AbstractIdTransceiver
{
    /**
     * Construct a new LinkIdTransceiver.
     * @param network OtsNetwork; the OTS network
     */
    public LinkIdTransceiver(final OtsNetwork network)
    {
        super(network, "Link id transceiver");
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LinkIdTransceiver [super=" + super.toString() + "]";
    }

    /** {@inheritDoc} */
    @Override
    ImmutableSet<Link> getSet()
    {
        return (ImmutableSet<Link>) getNetwork().getLinkMap().values();
    }

}

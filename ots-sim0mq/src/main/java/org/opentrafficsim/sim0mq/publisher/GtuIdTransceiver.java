package org.opentrafficsim.sim0mq.publisher;

import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableLinkedHashSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.network.Network;

/**
 * Transceiver for GTU ids.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class GtuIdTransceiver extends AbstractIdTransceiver
{
    /**
     * Construct a GtuIdTransceiver.
     * @param network Network; the OTS network
     */
    public GtuIdTransceiver(final Network network)
    {
        super(network, "GTU id transceiver");
    }

    /** {@inheritDoc} */
    @Override
    ImmutableSet<Gtu> getSet()
    {
        return new ImmutableLinkedHashSet<Gtu>(getNetwork().getGTUs(), Immutable.WRAP);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GtuIdTransceiver [network=" + getNetwork().getId() + ", super=" + super.toString() + "]";
    }

}

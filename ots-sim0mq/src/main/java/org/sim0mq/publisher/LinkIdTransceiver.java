package org.sim0mq.publisher;

import org.djutils.immutablecollections.ImmutableSet;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * Transceiver for Link ids.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LinkIdTransceiver extends AbstractTransceiver
{
    /** The network. */
    private final OTSNetwork network;

    /**
     * Construct a new LinkIdTransceiver.
     * @param network OTSNetwork; the OTS network
     */
    public LinkIdTransceiver(final OTSNetwork network)
    {
        super("Link id transceiver", new MetaData("No address", "empty address", new ObjectDescriptor[0]),
                new MetaData("", "", new ObjectDescriptor[] { new ObjectDescriptor("String array",
                        "String array filled with all currently valid Link ids", String[].class) }));
        this.network = network;
    }

    /** {@inheritDoc} */
    @Override
    public final Object[] get(final Object[] address)
    {
        getAddressFields().verifyComposition(address);
        ImmutableSet<String> links = this.network.getLinkMap().keySet();
        Object[] result = new Object[links.size()];
        int nextIndex = 0;
        for (String linkId : links)
        {
            result[nextIndex++] = linkId;
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LinkIdTransceiver [network=" + network + ", super=" + super.toString() + "]";
    }

}

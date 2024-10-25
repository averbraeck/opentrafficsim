package org.opentrafficsim.sim0mq.publisher;

import java.util.Set;

import org.djutils.exceptions.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.sim0mq.Sim0MQException;

/**
 * Transceiver for the ids of the GTUs on a link.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LinkGtuIdTransceiver extends AbstractTransceiver
{
    /** The network. */
    private final Network network;

    /**
     * Construct a GtuIdTransceiver.
     * @param network the OTS network
     */
    public LinkGtuIdTransceiver(final Network network)
    {
        super("Link GTU id transceiver",
                new MetaData("Link id", "Link id",
                        new ObjectDescriptor[] {new ObjectDescriptor("Link id", "Link id", String.class)}),
                new MetaData("String array with all Link ids", "String array with all Link ids",
                        new ObjectDescriptor[] {new ObjectDescriptor("String array",
                                "String array filled with all currently valid Link ids", String[].class)}));
        Throw.whenNull(network, "Network may not be null");
        this.network = network;
    }

    @Override
    public final Object[] get(final Object[] address, final ReturnWrapper returnWrapper)
            throws Sim0MQException, SerializationException
    {
        String bad = verifyMetaData(getAddressFields(), address);
        if (bad != null)
        {
            returnWrapper.nack("Bad address; need id of a link");
            return null;
        }
        Link link = this.network.getLink((String) address[0]);
        if (null == link)
        {
            returnWrapper.nack("Network does not contain a link with id " + address[0]);
            return null;
        }
        Set<Gtu> gtus = link.getGTUs();
        Object[] result = new Object[gtus.size()];
        int nextIndex = 0;
        for (Gtu gtu : gtus)
        {
            result[nextIndex++] = gtu.getId();
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "LinkGtuIdTransceiver [network=" + this.network + ", super=" + super.toString() + "]";
    }

}

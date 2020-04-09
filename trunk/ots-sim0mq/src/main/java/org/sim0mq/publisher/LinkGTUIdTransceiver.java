package org.sim0mq.publisher;

import java.util.Set;

import org.djunits.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * Transceiver for the ids of the GTUs on a link.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LinkGTUIdTransceiver extends AbstractTransceiver
{
    /** The network. */
    private final OTSNetwork network;

    /**
     * Construct a GTUIdTransceiver.
     * @param network OTSNetwork; the OTS network
     */
    public LinkGTUIdTransceiver(final OTSNetwork network)
    {
        super("Link GTU id transceiver",
                new MetaData("Link id", "Link id",
                        new ObjectDescriptor[] { new ObjectDescriptor("Link id", "Link id", String.class) }),
                new MetaData("", "", new ObjectDescriptor[] { new ObjectDescriptor("String array",
                        "String array filled with all currently valid GTU ids", String[].class) }));
        Throw.whenNull(network, "Network may not be null");
        this.network = network;
    }

    /** {@inheritDoc} */
    @Override
    public final Object[] get(final Object[] address)
    {
        getAddressFields().verifyComposition(address);
        Link link = this.network.getLink((String) address[0]);
        Set<GTU> gtus = link.getGTUs();
        Object[] result = new Object[gtus.size()];
        int nextIndex = 0;
        for (GTU gtu : gtus)
        {
            result[nextIndex++] = gtu.getId();
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LinkGTUIdTransceiver [network=" + network + ", super=" + super.toString() + "]";
    }

}

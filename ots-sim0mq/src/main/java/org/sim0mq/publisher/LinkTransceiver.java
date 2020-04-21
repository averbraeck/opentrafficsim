package org.sim0mq.publisher;

import java.rmi.RemoteException;

import org.djunits.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;

/**
 * Transceiver for Link data.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LinkTransceiver extends AbstractTransceiver
{
    /** The OTS network. */
    private final OTSNetwork network;

    /** Transceiver for the GTU ids. */
    private final TransceiverInterface linkIdSource;

    /**
     * Construct a new LinkTransceiver.
     * @param network OTSNetwork; the network
     * @param linkIdSource LinkIdTransceiver; the transceiver that can produce all Link ids in the Network
     */
    public LinkTransceiver(final OTSNetwork network, final LinkIdTransceiver linkIdSource)
    {
        super("Link transceiver",
                new MetaData("Link id", "Link id",
                        new ObjectDescriptor[] { new ObjectDescriptor("Link id", "Link id", String.class) }),
                new MetaData("Link data",
                        "Link id, type, start node id, end node id, design line size, gtu count, cross section "
                                + "element count",
                        new ObjectDescriptor[] { new ObjectDescriptor("Link id", "link id", String.class),
                                new ObjectDescriptor("LinkType id", "Link type", String.class),
                                new ObjectDescriptor("Start node id", "Start node id", String.class),
                                new ObjectDescriptor("End node id", "End node id", String.class),
                                new ObjectDescriptor("Design line size", "Number of points in design line of link",
                                        Integer.class),
                                new ObjectDescriptor("GTU count", "Total number of GTUs on the link", Integer.class),
                                new ObjectDescriptor("CrossSectionElement count",
                                        "Number of cross section elements on the link", Integer.class) }));
        this.network = network;
        this.linkIdSource = linkIdSource;
    }

    /** {@inheritDoc} */
    @Override
    public Object[] get(final Object[] address) throws RemoteException
    {
        getAddressFields().verifyComposition(address);
        Link link = this.network.getLink((String) address[0]);
        if (null == link)
        {
            return null;
        }
        return new Object[] { link.getId(), link.getLinkType().getId(), link.getStartNode().getId(), link.getEndNode().getId(),
                link instanceof OTSLink ? ((OTSLink) link).getDesignLine().size() : 0, link.getGTUCount(),
                link instanceof CrossSectionLink ? ((CrossSectionLink) link).getCrossSectionElementList().size() : 0 };
    }

    /** {@inheritDoc} */
    @Override
    public TransceiverInterface getIdSource(final int addressLevel)
    {
        Throw.when(addressLevel != 0, IndexOutOfBoundsException.class, "Only addressLevel 0 is valid");
        return this.linkIdSource;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LinkTransceiver [network=" + network + ", super=" + super.toString() + "]";
    }

}

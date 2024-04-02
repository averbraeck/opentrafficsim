package org.opentrafficsim.sim0mq.publisher;

import java.rmi.RemoteException;

import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.sim0mq.Sim0MQException;

/**
 * Transceiver for Link data.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LinkTransceiver extends AbstractTransceiver
{
    /** The OTS network. */
    private final Network network;

    /** Transceiver for the GTU ids. */
    private final TransceiverInterface linkIdSource;

    /**
     * Construct a new LinkTransceiver.
     * @param network Network; the network
     * @param linkIdSource LinkIdTransceiver; the transceiver that can produce all Link ids in the Network
     */
    public LinkTransceiver(final Network network, final LinkIdTransceiver linkIdSource)
    {
        super("Link transceiver",
                new MetaData("Link id", "Link id",
                        new ObjectDescriptor[] {new ObjectDescriptor("Link id", "Link id", String.class)}),
                new MetaData("Link data",
                        "Link id, type, start node id, end node id, design line size, gtu count, cross section "
                                + "element count",
                        new ObjectDescriptor[] {new ObjectDescriptor("Link id", "link id", String.class),
                                new ObjectDescriptor("LinkType id", "Link type", String.class),
                                new ObjectDescriptor("Start node id", "Start node id", String.class),
                                new ObjectDescriptor("End node id", "End node id", String.class),
                                new ObjectDescriptor("Design line size", "Number of points in design line of link",
                                        Integer.class),
                                new ObjectDescriptor("GTU count", "Total number of GTUs on the link", Integer.class),
                                new ObjectDescriptor("CrossSectionElement count",
                                        "Number of cross section elements on the link", Integer.class)}));
        this.network = network;
        this.linkIdSource = linkIdSource;
    }

    /** {@inheritDoc} */
    @Override
    public Object[] get(final Object[] address, final ReturnWrapper returnWrapper)
            throws RemoteException, Sim0MQException, SerializationException
    {
        String bad = verifyMetaData(getAddressFields(), address);
        if (bad != null)
        {
            returnWrapper.nack(bad);
            return null;
        }
        Link link = this.network.getLink((String) address[0]);
        if (null == link)
        {
            returnWrapper.nack("Network does not contain a link with id " + address[0]);
            return null;
        }
        return new Object[] {link.getId(), link.getType().getId(), link.getStartNode().getId(), link.getEndNode().getId(),
                link instanceof Link ? ((Link) link).getDesignLine().size() : 0, link.getGTUCount(),
                link instanceof CrossSectionLink ? ((CrossSectionLink) link).getCrossSectionElementList().size() : 0};
    }

    /** {@inheritDoc} */
    @Override
    public TransceiverInterface getIdSource(final int addressLevel, final ReturnWrapper returnWrapper)
            throws Sim0MQException, SerializationException
    {
        if (addressLevel != 0)
        {
            returnWrapper.nack("Only empty address is valid");
            return null;
        }
        return this.linkIdSource;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasIdSource()
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LinkTransceiver [network=" + this.network + ", super=" + super.toString() + "]";
    }

}

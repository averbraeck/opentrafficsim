package org.sim0mq.publisher;

import java.rmi.RemoteException;

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

    /**
     * Construct a new LinkTransceiver.
     * @param network OTSNetwork; the network
     */
    public LinkTransceiver(final OTSNetwork network)
    {
        super("Link transceiver", new FieldDescriptor[] { new FieldDescriptor("Link id", String.class) },
                new FieldDescriptor[] { new FieldDescriptor("Link id", String.class),
                        new FieldDescriptor("LinkType id", String.class), new FieldDescriptor("Start node id", String.class),
                        new FieldDescriptor("End node id", String.class),
                        new FieldDescriptor("Design line size", Integer.class), new FieldDescriptor("GTU count", Integer.class),
                        new FieldDescriptor("CrossSectionElement count", Integer.class) });
        this.network = network;
    }

    /** {@inheritDoc} */
    @Override
    public Object[] get(final Object[] address) throws RemoteException
    {
        verifyAddressComponents(address);
        Link link = this.network.getLink((String) address[0]);
        if (link != null)
        {
            return new Object[] { link.getId(), link.getLinkType().getId(), link.getStartNode().getId(),
                    link.getEndNode().getId(), link instanceof OTSLink ? ((OTSLink) link).getDesignLine().size() : 0,
                    link.getGTUCount(),
                    link instanceof CrossSectionLink ? ((CrossSectionLink) link).getCrossSectionElementList().size() : 0 };
        }
        return null;
    }

}

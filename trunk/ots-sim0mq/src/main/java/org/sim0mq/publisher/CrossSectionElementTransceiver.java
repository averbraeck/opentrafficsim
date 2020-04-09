package org.sim0mq.publisher;

import java.rmi.RemoteException;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;

/**
 * Transceiver for CrossSectionElement data.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CrossSectionElementTransceiver extends AbstractTransceiver
{
    /** The OTS network. */
    private final OTSNetwork network;

    /**
     * Construct a new CrossSectionElementTransceiver for an OTS network.
     * @param network OTSNetwork; the OTS network
     */
    public CrossSectionElementTransceiver(final OTSNetwork network)
    {
        super("CrossSectionElement transceiver", new MetaData("", "",
                new ObjectDescriptor[] { new ObjectDescriptor("Link id", "Link id", String.class),
                        new ObjectDescriptor("CrossSectionElement rank", "Rank of cross section element", Integer.class) }),
                new MetaData("", "", new ObjectDescriptor[] {
                        new ObjectDescriptor("CrossSectionElement id", "CrossSectionElement id", String.class),
                        new ObjectDescriptor("Sub type", "cross section element sub type", String.class),
                        new ObjectDescriptor("Length along center line", "Length of cross section element along center line",
                                Length.class),
                        new ObjectDescriptor("Begin offset", "Lateral offset at begin", Length.class),
                        new ObjectDescriptor("Begin width", "Width at begin", Length.class),
                        new ObjectDescriptor("End offset", "Lateral offset at end", Length.class),
                        new ObjectDescriptor("End width", "Width at end", Length.class) }));
        this.network = network;
    }

    /** {@inheritDoc} */
    @Override
    public Object[] get(final Object[] address) throws RemoteException
    {
        getAddressFields().verifyComposition(address);
        Link link = network.getLink((String) (address[0]));
        if (link == null)
        {
            return null;
        }
        if (!(link instanceof CrossSectionLink))
        {
            return null;
        }
        CrossSectionLink csl = (CrossSectionLink) link;
        List<CrossSectionElement> cseList = csl.getCrossSectionElementList();
        int rank = (Integer) (address[1]);
        if (rank < 0 || rank >= cseList.size())
        {
            return null;
        }
        CrossSectionElement cse = cseList.get(rank);
        return new Object[] { cse.getId(), cse.getClass().getName(), cse.getLength(), cse.getWidth(0),
                cse.getDesignLineOffsetAtBegin(), cse.getWidth(1.0), cse.getDesignLineOffsetAtEnd() };
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "CrossSectionElementTransceiver [network=" + network + ", super=" + super.toString() + "]";
    }

}

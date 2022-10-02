package org.opentrafficsim.sim0mq.publisher;

import java.rmi.RemoteException;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.OtsNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.sim0mq.Sim0MQException;

/**
 * Transceiver for CrossSectionElement data.
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class CrossSectionElementTransceiver extends AbstractTransceiver
{
    /** The OTS network. */
    private final OtsNetwork network;

    /**
     * Construct a new CrossSectionElementTransceiver for an OTS network.
     * @param network OTSNetwork; the OTS network
     */
    public CrossSectionElementTransceiver(final OtsNetwork network)
    {
        super("CrossSectionElement transceiver", new MetaData("Cross section element", "Cross section element",
                new ObjectDescriptor[] {new ObjectDescriptor("Link id", "Link id", String.class),
                        new ObjectDescriptor("CrossSectionElement rank", "Rank of cross section element", Integer.class)}),
                new MetaData("Cross section element data", "Cross section element data", new ObjectDescriptor[] {
                        new ObjectDescriptor("CrossSectionElement id", "CrossSectionElement id", String.class),
                        new ObjectDescriptor("Sub type", "cross section element sub type", String.class),
                        new ObjectDescriptor("Length along center line", "Length of cross section element along center line",
                                Length.class),
                        new ObjectDescriptor("Begin offset", "Lateral offset at begin", Length.class),
                        new ObjectDescriptor("Begin width", "Width at begin", Length.class),
                        new ObjectDescriptor("End offset", "Lateral offset at end", Length.class),
                        new ObjectDescriptor("End width", "Width at end", Length.class)}));
        this.network = network;
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
        Link link = this.network.getLink((String) (address[0]));
        if (link == null)
        {
            returnWrapper.nack("Network does not contain a link with id \"" + address[0] + "\"");
            return null;
        }
        if (!(link instanceof CrossSectionLink))
        {
            returnWrapper.nack("Link with id \"" + address[0] + "\" is not a CrossSectionLink");
            return null;
        }
        CrossSectionLink csl = (CrossSectionLink) link;
        List<CrossSectionElement> cseList = csl.getCrossSectionElementList();
        int rank = (Integer) (address[1]);
        if (rank < 0 || rank >= cseList.size())
        {
            returnWrapper.nack("Link with id \"" + address[0] + "\" does not have a CrossSectionElement with rank " + address[1]
                    + " valid range is 0.." + (cseList.size() - 1));
            return null;
        }
        CrossSectionElement cse = cseList.get(rank);
        return new Object[] {cse.getId(), cse.getClass().getName(), cse.getLength(), cse.getWidth(0),
                cse.getDesignLineOffsetAtBegin(), cse.getWidth(1.0), cse.getDesignLineOffsetAtEnd()};
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "CrossSectionElementTransceiver [network=" + this.network + ", super=" + super.toString() + "]";
    }

}

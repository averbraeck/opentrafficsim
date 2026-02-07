package org.opentrafficsim.sim0mq.publisher;

import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableList;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.sim0mq.Sim0MQException;

/**
 * Transceiver for the ids of the GTUs on a link.
 * <p>
 * Copyright (c) 2020-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneGtuIdTransceiver extends AbstractTransceiver
{
    /** The network. */
    private final Network network;

    /**
     * Construct a GtuIdTransceiver.
     * @param network the OTS network
     */
    public LaneGtuIdTransceiver(final Network network)
    {
        super("Lane GTU id transceiver",
                new MetaData("Link id, lane id", "Link id, lane id",
                        new ObjectDescriptor[] {new ObjectDescriptor("Link id", "Link id", String.class),
                                new ObjectDescriptor("Lane id", "Lane id", String.class)}),
                new MetaData("String array", "String array filled with all currently valid GTU ids on the lane",
                        new ObjectDescriptor[] {new ObjectDescriptor("String array",
                                "String array filled with all currently valid GTU ids on the lane", String[].class)}));
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
            returnWrapper.nack("Bad address; need id of a link and id of a CrossSectionElement");
            return null;
        }
        Link link = this.network.getLink((String) address[0]).orElse(null);
        if (null == link || (!(link instanceof CrossSectionLink)))
        {
            returnWrapper.nack("Network does not contain a link with id " + address[0]);
            return null;
        }
        CrossSectionLink csl = (CrossSectionLink) link;
        CrossSectionElement cse = csl.getCrossSectionElement((String) address[1]).orElse(null);
        if (null == cse)
        {
            returnWrapper.nack("Link " + address[0] + " does not contain a cross section element with id " + address[1]);
            return null;
        }
        if (!(cse instanceof Lane))
        {
            returnWrapper.nack("CrossSectionElement " + address[1] + " of link with id " + address[0] + ", is not a lane");
            return null;
        }
        Lane lane = (Lane) cse;
        ImmutableList<LaneBasedGtu> gtus = lane.getGtuList();
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
        return "LaneGtuIdTransceiver [network=" + this.network + ", super=" + super.toString() + "]";
    }

}

package org.sim0mq.publisher;

import java.rmi.RemoteException;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
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
        super("CrossSectionElement transceiver",
                new FieldDescriptor[] { new FieldDescriptor("Link id", String.class),
                        new FieldDescriptor("CrossSectionElement rank", Integer.class) },
                new FieldDescriptor[] { new FieldDescriptor("Sub type", String.class),
                        new FieldDescriptor("Length along center line", Length.class) });
        this.network = network;
    }

    /** {@inheritDoc} */
    @Override
    public Object[] get(final Object[] address) throws RemoteException
    {
        verifyAddressComponents(address);
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
        return new Object[] { cse.getClass().getName(), cse.getLength() };
    }

}

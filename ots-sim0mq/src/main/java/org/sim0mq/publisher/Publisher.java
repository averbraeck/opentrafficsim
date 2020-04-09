package org.sim0mq.publisher;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * Publish all available transceivers for an OTS network.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2020-02-13 11:08:16 +0100 (Thu, 13 Feb 2020) $, @version $Revision: 6383 $, by $Author: pknoppers $,
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Publisher extends AbstractTransceiver
{
    /** Map transceiver names to the corresponding TransceiverInterface object. */
    private final Map<String, TransceiverInterface> transceiverMap = new LinkedHashMap<>();

    /** Embedded transceiver that can produce the names of all the transceivers for the objects in the OTS network. */
    private final TransceiverInterface idTransceiver = new AbstractTransceiver("Ids of available transceivers",
            new MetaData("Transceiver", "id of transceiver", new ObjectDescriptor[0]),
            new MetaData("Id of transceiver", "Id of transceiver", new ObjectDescriptor[] {
                    new ObjectDescriptor("Name of available transceiver", "Name of available transceiver", String.class) }))
    {
        /** {@inheritDoc} */
        @Override
        public Object[] get(final Object[] address)
        {
            getAddressFields().verifyComposition(address);
            Object[] result = new Object[transceiverMap.size()];
            int index = 0;
            for (String key : transceiverMap.keySet())
            {
                result[index++] = key;
            }
            return result;
        };
    };

    /**
     * Construct a Publisher for an OTS network.
     * @param network OTSNetwork; the OTS network
     */
    public Publisher(final OTSNetwork network)
    {
        super("Publisher for " + Throw.whenNull(network, "Network may not be null").getId(),
                new MetaData("", "",
                        new ObjectDescriptor[] { new ObjectDescriptor("Transceiver name", "Transceiver name", String.class) }),
                new MetaData("", "", new ObjectDescriptor[] {
                        new ObjectDescriptor("TransceiverInterface", "Transceiver", ObjectDescriptor.class) }));
        GTUIdTransceiver gtuIdTransceiver = new GTUIdTransceiver(network);
        addTransceiver(gtuIdTransceiver);
        addTransceiver(new GTUTransceiver(network, gtuIdTransceiver));
        LinkIdTransceiver linkIdTransceiver = new LinkIdTransceiver(network);
        addTransceiver(linkIdTransceiver);
        addTransceiver(new LinkTransceiver(network, linkIdTransceiver));
        addTransceiver(new CrossSectionElementTransceiver(network));
        addTransceiver(new LinkGTUIdTransceiver(network));
        addTransceiver(new LaneGTUIdTransceiver(network));
    }

    /**
     * Add a TransceiverInterface to the map.
     * @param transceiver TransceiverInterface; the transceiver interface to add to the map
     */
    private void addTransceiver(final TransceiverInterface transceiver)
    {
        this.transceiverMap.put(transceiver.getId(), transceiver);
    }

    /** {@inheritDoc} */
    @Override
    public Object[] get(final Object[] address)
    {
        getAddressFields().verifyComposition(address);
        TransceiverInterface result = this.transceiverMap.get(address[0]);
        if (null != result)
        {
            return new Object[] { result };
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final TransceiverInterface getIdSource(final int addressLevel)
    {
        Throw.when(addressLevel != 0, IndexOutOfBoundsException.class, "addressLevel must be 0");
        return this.idTransceiver;
    }

}

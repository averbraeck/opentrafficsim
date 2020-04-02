package org.sim0mq.publisher;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.Throw;
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
    private final TransceiverInterface idTransceiver =
            new AbstractTransceiver("Names of available transceivers", new FieldDescriptor[0],
                    new FieldDescriptor[] { new FieldDescriptor("Name of available transceiver", String.class) })
            {
                /** {@inheritDoc} */
                @Override
                public Object[] get(final Object[] address)
                {
                    verifyAddressComponents(address);
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
                new FieldDescriptor[] { new FieldDescriptor("Transceiver name", String.class) },
                new FieldDescriptor[] { new FieldDescriptor("TransceiverInterface", TransceiverInterface.class) });
        GTUIdTransceiver gtuIdTransceiver = new GTUIdTransceiver(network);
        addTransceiver(gtuIdTransceiver);
        addTransceiver(new GTUTransceiver(network, gtuIdTransceiver));
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
        verifyAddressComponents(address);
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

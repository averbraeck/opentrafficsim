package org.opentrafficsim.sim0mq.publisher;

import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableSet;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.core.network.Network;
import org.sim0mq.Sim0MQException;

/**
 * Common code for id transceivers that use an empty address.
 * <p>
 * Copyright (c) 2020-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractIdTransceiver extends AbstractTransceiver
{
    /** The network. */
    private final Network network;

    /**
     * Construct a GtuIdTransceiver.
     * @param network Network; the OTS network
     * @param id String; name of the IdTransceiver
     */
    public AbstractIdTransceiver(final Network network, final String id)
    {
        super(id, new MetaData("No address", "empty address", new ObjectDescriptor[0]),
                new MetaData("No address", "empty address", new ObjectDescriptor[] {new ObjectDescriptor("String array",
                        "String array filled with all currently valid GTU ids", String[].class)}));
        Throw.whenNull(network, "Network may not be null");
        this.network = network;
    }

    /**
     * Retrieve the set of names of objects that can be individually subscribed to.
     * @return Set&lt;?&gt;; the set of names of objects whose that can be subscribed to. Each object in this set should
     *         implement <code>Identifiable</code>
     */
    abstract ImmutableSet<?> getSet();

    /** {@inheritDoc} */
    @Override
    public final Object[] get(final Object[] address, final ReturnWrapper returnWrapper)
            throws Sim0MQException, SerializationException
    {
        String bad = verifyMetaData(getAddressFields(), address);
        if (bad != null)
        {
            returnWrapper.nack(bad);
            return null;
        }
        ImmutableSet<?> set = getSet();
        Object[] result = new Object[set.size()];
        int nextIndex = 0;
        for (Object object : set)
        {
            result[nextIndex++] = ((Identifiable) object).getId();
        }
        return result;
    }

    /**
     * Retrieve the network.
     * @return Network; the network
     */
    final Network getNetwork()
    {
        return this.network;
    }

}

package org.sim0mq.publisher;

import org.djunits.Throw;
import org.djutils.immutablecollections.ImmutableSet;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.network.OTSNetwork;
import org.sim0mq.Sim0MQException;

/**
 * Common code for id transceivers that use an empty address.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractIdTransceiver extends AbstractTransceiver
{
    /** The network. */
    private final OTSNetwork network;

    /**
     * Construct a GTUIdTransceiver.
     * @param network OTSNetwork; the OTS network
     * @param id String; name of the IdTransceiver
     */
    public AbstractIdTransceiver(final OTSNetwork network, final String id)
    {
        super(id, new MetaData("No address", "empty address", new ObjectDescriptor[0]),
                new MetaData("No address", "empty address", new ObjectDescriptor[] { new ObjectDescriptor("String array",
                        "String array filled with all currently valid GTU ids", String[].class) }));
        Throw.whenNull(network, "Network may not be null");
        this.network = network;
    }

    /**
     * Retrieve the set of objects whose names will be returned by the get method.
     * @return Set&lt;?&gt;; the set of objects whose names will be returned by the get method. Each object in this set should
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
            returnWrapper.encodeReplyAndTransmit("Bad address");
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
     * @return OTSNetwork; the network
     */
    final OTSNetwork getNetwork()
    {
        return this.network;
    }

}

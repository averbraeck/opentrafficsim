package org.sim0mq.publisher;

import java.util.Set;

import org.djunits.Throw;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * Transceiver for GTU ids.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GTUIdTransceiver extends AbstractTransceiver
{
    /** The network. */
    private final OTSNetwork network;

    /**
     * Construct a GTUIdTransceiver.
     * @param network OTSNetwork; the OTS network
     */
    public GTUIdTransceiver(final OTSNetwork network)
    {
        super("GTU id transceiver", new FieldDescriptor[] {}, new FieldDescriptor[] {
                new FieldDescriptor("String array filled with all currently valid GTU ids", String[].class) });
        Throw.whenNull(network, "Network may not be null");
        this.network = network;
    }

    /** {@inheritDoc} */
    @Override
    public final Object[] get(final Object[] address)
    {
        verifyAddressComponents(address);
        Set<GTU> gtus = this.network.getGTUs();
        Object[] result = new Object[gtus.size()];
        int nextIndex = 0;
        for (GTU gtu : gtus)
        {
            result[nextIndex++] = gtu.getId();
        }
        return result;
    }

}

package org.sim0mq.publisher;

import org.djunits.Throw;
import org.djutils.serialization.SerializationException;
import org.sim0mq.Sim0MQException;

/**
 * The ReturnWrapper interface enforces implementation of the encodeReplyAndTransmit method
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2020-02-13 11:08:16 +0100 (Thu, 13 Feb 2020) $, @version $Revision: 6383 $, by $Author: pknoppers $,
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface ReturnWrapper
{
    /**
     * Encode a reply and transmit it.
     * @param payload Object[]; payload of the reply message
     * @throws Sim0MQException not sure if that can happen
     * @throws SerializationException when an object in payload cannot be serialized
     */
    void encodeReplyAndTransmit(Object[] payload) throws Sim0MQException, SerializationException;

    /**
     * Encode a String reply and transmit it.
     * @param payload String; payload of the reply message
     * @throws Sim0MQException not sure if that can happen
     * @throws SerializationException when an object in payload cannot be serialized
     */
    default void encodeReplyAndTransmit(final String payload) throws Sim0MQException, SerializationException
    {
        Throw.whenNull(payload, "payload may not be null");
        encodeReplyAndTransmit(new Object[] { payload });
    }

}

package org.opentrafficsim.sim0mq.publisher;

import org.djutils.exceptions.Throw;
import org.djutils.serialization.SerializationException;
import org.sim0mq.Sim0MQException;

/**
 * The ReturnWrapper interface enforces implementation of the encodeReplyAndTransmit method
 * <p>
 * Copyright (c) 2020-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public interface ReturnWrapper
{
    /**
     * Encode a String reply and transmit it while patching the message type id field by adding a vertical bar and a suffix.
     * @param status Boolean; if null, the payload is transmitted as is; if true, a boolean true is prepended to the payload; if
     *            false, a boolean false is prepended to the payload
     * @param payload Object []; payload of the reply message
     * @throws Sim0MQException not sure if that can happen
     * @throws SerializationException when an object in payload cannot be serialized
     */
    void encodeReplyAndTransmit(Boolean status, Object[] payload) throws Sim0MQException, SerializationException;

    /**
     * Encode a String reply and transmit it.
     * @param payload String; payload of the reply message
     * @throws Sim0MQException not sure if that can happen
     * @throws SerializationException when an object in payload cannot be serialized
     */
    default void encodeReplyAndTransmit(final String payload) throws Sim0MQException, SerializationException
    {
        Throw.whenNull(payload, "payload may not be null");
        encodeReplyAndTransmit(null, new Object[] {payload});
    }

    /**
     * Encode a String reply and transmit it.
     * @param payload Object[]; payload of the reply message
     * @throws Sim0MQException not sure if that can happen
     * @throws SerializationException when an object in payload cannot be serialized
     */
    default void encodeReplyAndTransmit(final Object[] payload) throws Sim0MQException, SerializationException
    {
        Throw.whenNull(payload, "payload may not be null");
        encodeReplyAndTransmit(null, payload);
    }

    /**
     * Encode a String reply and transmit it.
     * @param status Boolean; if null, the payload is transmitted as is; if true, a boolean true is prepended to the payload; if
     *            false, a boolean false is prepended to the payload
     * @param payload String; payload of the reply message
     * @throws Sim0MQException not sure if that can happen
     * @throws SerializationException when an object in payload cannot be serialized
     */
    default void encodeReplyAndTransmit(final Boolean status, final String payload)
            throws Sim0MQException, SerializationException
    {
        Throw.whenNull(payload, "payload may not be null");
        encodeReplyAndTransmit(status, new Object[] {payload});
    }

    /**
     * Signal successful execution of a request.
     * @param payload String; additional description of the result
     * @throws Sim0MQException not sure if that can happen
     * @throws SerializationException when an object in payload cannot be serialized
     */
    default void ack(final String payload) throws Sim0MQException, SerializationException
    {
        encodeReplyAndTransmit(true, payload);
    }

    /**
     * Signal failure of execution of a request.
     * @param payload String; additional description of the failure
     * @throws Sim0MQException not sure if that can happen
     * @throws SerializationException when an object in payload cannot be serialized
     */
    default void nack(final String payload) throws Sim0MQException, SerializationException
    {
        encodeReplyAndTransmit(false, payload);
    }

}

package org.sim0mq.message.federatestarter;

import org.sim0mq.Sim0MQException;
import org.sim0mq.message.MessageStatus;
import org.sim0mq.message.Sim0MQMessage;
import org.sim0mq.message.util.SimulationMessage;

/**
 * RequestStatus, FS.1. This message is sent by the Federate Starter to the Model until a “started” response is received from
 * the Model. Since the message type id clarifies the function of this message and no information exchange is necessary, the
 * payload field can be empty (number of fields = 0).
 * <p>
 * Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://sim0mq.org/docs/current/license.html">Sim0MQ License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Apr 22, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class RequestStatusMessage extends Sim0MQMessage
{

    /** the unique message id. */
    private static final String MESSAGETYPE = "FS.1";

    /** */
    private static final long serialVersionUID = 20170422L;

    /**
     * @param simulationRunId the Simulation run ids can be provided in different types. Examples are two 64-bit longs
     *            indicating a UUID, or a String with a UUID number, a String with meaningful identification, or a short or an
     *            int with a simulation run number.
     * @param senderId The sender id can be used to send back a message to the sender at some later time.
     * @param receiverId The receiver id can be used to check whether the message is meant for us, or should be discarded (or an
     *            error can be sent if we receive a message not meant for us).
     * @param messageId The unique message number is meant to confirm with a callback that the message has been received
     *            correctly. The number is unique for the sender, so not globally within the federation.
     * @throws Sim0MQException on unknown data type
     * @throws NullPointerException when one of the parameters is null
     */
    public RequestStatusMessage(final Object simulationRunId, final Object senderId, final Object receiverId,
            final long messageId) throws Sim0MQException, NullPointerException
    {
        super(simulationRunId, senderId, receiverId, MESSAGETYPE, messageId, MessageStatus.NEW);
    }

    /**
     * @return messagetype
     */
    public static final String getMessageType()
    {
        return MESSAGETYPE;
    }

    /** {@inheritDoc} */
    @Override
    public Object[] createObjectArray()
    {
        return new Object[] { getSimulationRunId(), getSenderId(), getReceiverId(), getMessageTypeId(), getMessageId(),
                getMessageStatus() };
    }

    /** {@inheritDoc} */
    @Override
    public byte[] createByteArray() throws Sim0MQException
    {
        return SimulationMessage.encode(getSimulationRunId(), getSenderId(), getReceiverId(), getMessageTypeId(),
                getMessageId(), getMessageStatus());
    }

    /**
     * Builder for the StartFederate Message. Can string setters together, and call build() at the end to build the actual
     * message.
     * <p>
     * Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://sim0mq.org/docs/current/license.html">Sim0MQ License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Apr 22, 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public static class Builder extends Sim0MQMessage.Builder
    {
        /**
         * Empty constructor.
         */
        public Builder()
        {
            // noting to do.
        }

        /** {@inheritDoc} */
        @Override
        public Sim0MQMessage build() throws Sim0MQException, NullPointerException
        {
            return new RequestStatusMessage(this.simulationRunId, this.senderId, this.receiverId, this.messageId);
        }

    }
}

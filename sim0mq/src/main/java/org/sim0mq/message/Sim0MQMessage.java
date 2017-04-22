package org.sim0mq.message;

import java.io.Serializable;

import org.sim0mq.Sim0MQException;

import nl.tudelft.simulation.language.Throw;

/**
 * The abstract body of the message with the first fields of every Sim0MQ message.
 * <p>
 * Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://sim0mq.org/docs/current/license.html">Sim0MQ License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Apr 22, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class Sim0MQMessage implements Serializable
{
    /** */
    private static final long serialVersionUID = 20170422L;

    /**
     * the Simulation run ids can be provided in different types. Examples are two 64-bit longs indicating a UUID, or a String
     * with a UUID number, a String with meaningful identification, or a short or an int with a simulation run number.
     */
    private final Object simulationRunId;

    /** The sender id can be used to send back a message to the sender at some later time. */
    private final Object senderId;

    /**
     * The receiver id can be used to check whether the message is meant for us, or should be discarded (or an error can be sent
     * if we receive a message not meant for us).
     */
    private final Object receiverId;

    /**
     * Message type ids can be defined per type of simulation, and can be provided in different types. Examples are a String
     * with a meaningful identification, or a short or an int with a message type number.
     */
    private final Object messageTypeId;

    /**
     * The unique message number is meant to confirm with a callback that the message has been received correctly. The number is
     * unique for the sender, so not globally within the federation.
     */
    private final long messageId;

    /**
     * Three different status messages are defined: 1 for new, 2 for change, and 3 for delete. This field is coded as a byte.
     */
    private final MessageStatus messageStatus;

    /**
     * Encode the object array into a message.
     * @param simulationRunId the Simulation run ids can be provided in different types. Examples are two 64-bit longs
     *            indicating a UUID, or a String with a UUID number, a String with meaningful identification, or a short or an
     *            int with a simulation run number.
     * @param senderId The sender id can be used to send back a message to the sender at some later time.
     * @param receiverId The receiver id can be used to check whether the message is meant for us, or should be discarded (or an
     *            error can be sent if we receive a message not meant for us).
     * @param messageTypeId Message type ids can be defined per type of simulation, and can be provided in different types.
     *            Examples are a String with a meaningful identification, or a short or an int with a message type number.
     * @param messageId The unique message number is meant to confirm with a callback that the message has been received
     *            correctly. The number is unique for the sender, so not globally within the federation.
     * @param messageStatus Three different status messages are defined: 1 for new, 2 for change, and 3 for delete. This field
     *            is coded as a byte.
     * @throws Sim0MQException on unknown data type
     * @throws NullPointerException when one of the parameters is null
     */
    public Sim0MQMessage(final Object simulationRunId, final Object senderId, final Object receiverId,
            final Object messageTypeId, final long messageId, final MessageStatus messageStatus)
            throws Sim0MQException, NullPointerException
    {
        Throw.whenNull(simulationRunId, "simulationRunId cannot be null");
        Throw.whenNull(senderId, "senderId cannot be null");
        Throw.whenNull(receiverId, "receiverId cannot be null");
        Throw.whenNull(messageTypeId, "messageTypeId cannot be null");
        Throw.whenNull(messageId, "messageId cannot be null");
        Throw.whenNull(messageStatus, "messageStatus cannot be null");

        this.simulationRunId = simulationRunId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageTypeId = messageTypeId;
        this.messageId = messageId;
        this.messageStatus = messageStatus;
    }

    /**
     * @return simulationRunId
     */
    public final Object getSimulationRunId()
    {
        return this.simulationRunId;
    }

    /**
     * @return senderId
     */
    public final Object getSenderId()
    {
        return this.senderId;
    }

    /**
     * @return receiverId
     */
    public final Object getReceiverId()
    {
        return this.receiverId;
    }

    /**
     * @return messageTypeId
     */
    public final Object getMessageTypeId()
    {
        return this.messageTypeId;
    }

    /**
     * @return messageId
     */
    public final long getMessageId()
    {
        return this.messageId;
    }

    /**
     * @return messageStatus
     */
    public final MessageStatus getMessageStatus()
    {
        return this.messageStatus;
    }
    
    /**
     * Create a Sim0MQ object array of the fields.
     * @return Object[] a Sim0MQ object array of the fields
     */
    public abstract Object[] createObjectArray();

    /**
     * Create a byte array of the fields.
     * @return byte[] a Sim0MQ byte array of the content
     * @throws Sim0MQException on unknown data type as part of the content
     */
    public abstract byte[] createByteArray() throws Sim0MQException;

    /**
     * Builder for the Sim0MQMessage. Can string setters together, and call build() at the end to build the actual message.
     * <p>
     * Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://sim0mq.org/docs/current/license.html">Sim0MQ License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Apr 22, 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public static abstract class Builder
    {
        /**
         * the Simulation run ids can be provided in different types. Examples are two 64-bit longs indicating a UUID, or a
         * String with a UUID number, a String with meaningful identification, or a short or an int with a simulation run
         * number.
         */
        protected Object simulationRunId;

        /** The sender id can be used to send back a message to the sender at some later time. */
        protected Object senderId;

        /**
         * The receiver id can be used to check whether the message is meant for us, or should be discarded (or an error can be
         * sent if we receive a message not meant for us).
         */
        protected Object receiverId;

        /**
         * Message type ids can be defined per type of simulation, and can be provided in different types. Examples are a String
         * with a meaningful identification, or a short or an int with a message type number.
         */
        protected Object messageTypeId;

        /**
         * The unique message number is meant to confirm with a callback that the message has been received correctly. The
         * number is unique for the sender, so not globally within the federation.
         */
        protected long messageId;

        /**
         * Three different status messages are defined: 1 for new, 2 for change, and 3 for delete. This field is coded as a
         * byte.
         */
        protected MessageStatus messageStatus;

        /**
         * Empty constructor.
         */
        public Builder()
        {
            // noting to do.
        }

        /**
         * @param simulationRunId set simulationRunId
         * @return the original object for chaining
         */
        public final Builder setSimulationRunId(final Object simulationRunId)
        {
            this.simulationRunId = simulationRunId;
            return this;
        }

        /**
         * @param senderId set senderId
         * @return the original object for chaining
         */
        public final Builder setSenderId(final Object senderId)
        {
            this.senderId = senderId;
            return this;
        }

        /**
         * @param receiverId set receiverId
         * @return the original object for chaining
         */
        public final Builder setReceiverId(final Object receiverId)
        {
            this.receiverId = receiverId;
            return this;
        }

        /**
         * @param messageTypeId set messageTypeId
         * @return the original object for chaining
         */
        public final Builder setMessageTypeId(final Object messageTypeId)
        {
            this.messageTypeId = messageTypeId;
            return this;
        }

        /**
         * @param messageId set messageId
         * @return the original object for chaining
         */
        public final Builder setMessageId(final long messageId)
        {
            this.messageId = messageId;
            return this;
        }

        /**
         * @param messageStatus set messageStatus
         * @return the original object for chaining
         */
        public final Builder setMessageStatus(final MessageStatus messageStatus)
        {
            this.messageStatus = messageStatus;
            return this;
        }

        /**
         * Build the object.
         * @return the message object from the builder.
         * @throws Sim0MQException on unknown data type
         * @throws NullPointerException when one of the parameters is null
         */
        public abstract Sim0MQMessage build() throws Sim0MQException, NullPointerException;

    }
}

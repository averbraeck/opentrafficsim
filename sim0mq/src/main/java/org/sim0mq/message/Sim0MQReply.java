package org.sim0mq.message;

import org.sim0mq.Sim0MQException;

/**
 * The abstract body of a reply message with the first fields of every Sim0MQ reply message.
 * <p>
 * Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://sim0mq.org/docs/current/license.html">Sim0MQ License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Apr 22, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class Sim0MQReply extends Sim0MQMessage
{
    /** */
    private static final long serialVersionUID = 20170422L;

    /** The unique message id (Frame 5) of the sender for which this is the reply. */
    private final long replyToId;

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
     * @param replyToId The unique message id (Frame 5) of the sender for which this is the reply.
     * @throws Sim0MQException on unknown data type
     * @throws NullPointerException when one of the parameters is null
     */
    public Sim0MQReply(final Object simulationRunId, final Object senderId, final Object receiverId,
            final Object messageTypeId, final long messageId, final MessageStatus messageStatus, final long replyToId)
            throws Sim0MQException, NullPointerException
    {
        super(simulationRunId, senderId, receiverId, messageTypeId, messageId, messageStatus);
        this.replyToId = replyToId;
    }

    /**
     * @return replyToId
     */
    public final long getReplyToId()
    {
        return this.replyToId;
    }

    /**
     * Builder for the Sim0MQReply. Can string setters together, and call build() at the end to build the actual message.
     * <p>
     * Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://sim0mq.org/docs/current/license.html">Sim0MQ License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Apr 22, 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public static abstract class Builder extends Sim0MQMessage.Builder
    {
        /** The unique message id (Frame 5) of the sender for which this is the reply. */
        protected long replyToId;

        /**
         * Empty constructor.
         */
        public Builder()
        {
            // noting to do.
        }

        /**
         * @param replyToId set replyToId
         * @return the original object for chaining
         */
        public final Builder setReplyToId(final long replyToId)
        {
            this.replyToId = replyToId;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public abstract Sim0MQReply build() throws Sim0MQException, NullPointerException;

    }
}

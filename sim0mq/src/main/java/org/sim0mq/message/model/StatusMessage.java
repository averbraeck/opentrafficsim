package org.sim0mq.message.model;

import org.sim0mq.Sim0MQException;
import org.sim0mq.message.MessageStatus;
import org.sim0mq.message.Sim0MQMessage;
import org.sim0mq.message.SimulationMessage;

import nl.tudelft.simulation.language.Throw;

/**
 * StatusMessage, MC.1. The Model sends this message as a response to RequestStatus messages sent by the Federate Starter or the
 * Federation Manager.
 * <p>
 * Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://sim0mq.org/docs/current/license.html">Sim0MQ License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Apr 22, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class StatusMessage extends Sim0MQMessage
{
    /** The unique message id (Frame 5) of the sender for which this is the reply. */
    private final long uniqueId;

    /** A string that refers to the model status. Four options: “started”, “running”, “ended”, “error”. */
    private final String status;

    /** Optional. If there is an error, the error message is sent as well. Otherwise this field is an empty string. */
    private final String error;

    /** the unique message id. */
    private static final String MESSAGETYPE = "MC.1";

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
     * @param uniqueId Id to identify the callback to know which model instance has been started, e.g. "IDVV.14". The model
     *            instance will use this as its sender id.
     * @param status Code for the software to run, will be looked up in a table on the local computer to determine the path to
     *            start the software on that computer. Example: "java". If the softwarePath is defined, softwareCode can be an
     *            empty String (0 characters).
     * @param error Arguments that the software needs, before the model file path and name; e.g. "–Xmx2G -jar" in case of a Java
     *            model. This String can be empty (0 characters).
     * @throws Sim0MQException on unknown data type
     * @throws NullPointerException when one of the parameters is null
     */
    public StatusMessage(final Object simulationRunId, final Object senderId, final Object receiverId, final long messageId,
            final long uniqueId, final String status, final String error) throws Sim0MQException, NullPointerException
    {
        super(simulationRunId, senderId, receiverId, MESSAGETYPE, messageId, MessageStatus.NEW);
        Throw.whenNull(status, "status cannot be null");
        Throw.whenNull(error, "error cannot be null");

        Throw.when(status.isEmpty(), Sim0MQException.class, "status cannot be empty");
        Throw.when(!status.equals("started") && !status.equals("running") && !status.equals("ended") && !status.equals("error"),
                Sim0MQException.class, "status should be one of 'started', 'running', 'ended', 'error'");

        this.uniqueId = uniqueId;
        this.status = status;
        this.error = error;
    }

    /**
     * @return uniqueId
     */
    public final long getUniqueId()
    {
        return this.uniqueId;
    }

    /**
     * @return status
     */
    public final String getStatus()
    {
        return this.status;
    }

    /**
     * @return error
     */
    public final String getError()
    {
        return this.error;
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
                getMessageStatus(), this.uniqueId, this.status, this.error };
    }

    /** {@inheritDoc} */
    @Override
    public byte[] createByteArray() throws Sim0MQException
    {
        return SimulationMessage.encode(getSimulationRunId(), getSenderId(), getReceiverId(), getMessageTypeId(),
                getMessageId(), getMessageStatus(), this.uniqueId, this.status, this.error);
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
        /** The unique message id (Frame 5) of the sender for which this is the reply. */
        private long uniqueId;

        /** A string that refers to the model status. Four options: “started”, “running”, “ended”, “error”. */
        private String status;

        /** Optional. If there is an error, the error message is sent as well. Otherwise this field is an empty string. */
        private String error;

        /**
         * Empty constructor.
         */
        public Builder()
        {
            // noting to do.
        }

        /**
         * @param uniqueId set uniqueId
         * @return the original object for chaining
         */
        public final Builder setUniqueId(final long uniqueId)
        {
            this.uniqueId = uniqueId;
            return this;
        }

        /**
         * @param status set status
         * @return the original object for chaining
         */
        public final Builder setStatus(String status)
        {
            this.status = status;
            return this;
        }

        /**
         * @param error set error
         * @return the original object for chaining
         */
        public final Builder setError(String error)
        {
            this.error = error;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Sim0MQMessage build() throws Sim0MQException, NullPointerException
        {
            return new StatusMessage(this.simulationRunId, this.senderId, this.receiverId, this.messageId, this.uniqueId,
                    this.status, this.error);
        }

    }
}

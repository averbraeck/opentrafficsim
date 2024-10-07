package org.opentrafficsim.sim0mq.publisher;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.djutils.exceptions.Throw;
import org.djutils.serialization.SerializationException;
import org.djutils.serialization.SerializationRuntimeException;
import org.sim0mq.Sim0MQException;
import org.sim0mq.message.Sim0MQMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

/**
 * Container for all data needed to reply (once, or multiple times) to a Sim0MQ request.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ReturnWrapperImpl implements ReturnWrapper
{
    /** The ZContext needed to create the return socket(s). */
    private final ZContext zContext;

    /** Federation id. */
    private final Object federationId;

    /** Sender id (to be used as return address). */
    private final Object returnAddress;

    /** Our id (to be used as sender address in replies). */
    private final Object ourAddress;

    /** Message type id used by the sender; re-used in the reply. */
    private final Object messageTypeId;

    /** Message id used by the sender; re-used in the reply; post-incremented by one if it is an Integer. */
    private final Object messageId;

    /** Number of replies sent. SHOULD NOT BE USED IN equals or hashCode! */
    private int replyCount = 0;

    /**
     * Construct a new ReturnWrapper.
     * @param zContext the ZContext needed to create sockets for returned messages
     * @param receivedMessage the received message from which the reply envelope will be derived
     * @param socketMap Map&lt;Long, ZMQ.Socket&gt;; cache of created sockets for returned messages
     * @param packetsSent counter for returned messages
     * @throws SerializationException when the received message has an incorrect envelope
     * @throws Sim0MQException when the received message cannot be decoded
     */
    ReturnWrapperImpl(final ZContext zContext, final byte[] receivedMessage, final Map<Long, Socket> socketMap,
            final AtomicInteger packetsSent) throws Sim0MQException, SerializationException
    {
        this(zContext, Sim0MQMessage.decode(receivedMessage).createObjectArray(), socketMap);
    }

    /**
     * Construct a new ReturnWrapper.
     * @param zContext the ZContext needed to create sockets for returned messages
     * @param decodedReceivedMessage decoded Sim0MQ message
     * @param socketMap Map&lt;Long, ZMQ.Socket&gt;; cache of created sockets for returned messages
     */
    public ReturnWrapperImpl(final ZContext zContext, final Object[] decodedReceivedMessage, final Map<Long, Socket> socketMap)
    {
        Throw.whenNull(zContext, "zContext may not be null");
        Throw.whenNull(socketMap, "socket map may not be null");
        this.zContext = zContext;
        this.socketMap = socketMap;
        Throw.when(decodedReceivedMessage.length < 8, SerializationRuntimeException.class,
                "Received message is too short (minumum number of elements is 8; got %d", decodedReceivedMessage.length);
        this.federationId = decodedReceivedMessage[2];
        this.returnAddress = decodedReceivedMessage[3];
        this.ourAddress = decodedReceivedMessage[4];
        this.messageTypeId = decodedReceivedMessage[5];
        this.messageId = decodedReceivedMessage[6];
    }

    /** In memory sockets to talk to the multiplexer. */
    private final Map<Long, ZMQ.Socket> socketMap;

    /**
     * Central portal to send a message to the master.
     * @param data the data to send
     */
    public synchronized void sendToMaster(final byte[] data)
    {
        Long threadId = Thread.currentThread().getId();
        ZMQ.Socket socket = this.socketMap.get(threadId);
        while (null == socket)
        {
            // System.out.println("socket map is " + this.socketMap);
            System.out.println("Creating new internal socket for thread " + threadId + " (map currently contains "
                    + this.socketMap.size() + " entries)");
            socket = this.zContext.createSocket(SocketType.PUSH);
            socket.setHWM(100000);
            socket.connect("inproc://simulationEvents");
            this.socketMap.put(threadId, socket);
            // System.out.println("Socket created; map now contains " + this.socketMap.size() + " entries");
        }
        // System.out.println("pre send");
        socket.send(data, 0);
        // System.out.println("post send");
    }

    /** {@inheritDoc} */
    @Override
    public void encodeReplyAndTransmit(final Boolean ackNack, final Object[] payload)
            throws Sim0MQException, SerializationException
    {
        Throw.whenNull(payload, "payload may not be null (but it can be an emty Object array)");
        Object fixedMessageTypeId = this.messageTypeId;
        Object[] fixedPayload = payload;
        if (null != ackNack)
        {
            fixedPayload = new Object[payload.length + 1];
            fixedPayload[0] = ackNack;
            for (int index = 0; index < payload.length; index++)
            {
                fixedPayload[index + 1] = payload[index];
            }
        }
        byte[] result = Sim0MQMessage.encodeUTF8(true, this.federationId, this.ourAddress, this.returnAddress,
                fixedMessageTypeId, this.messageId, fixedPayload);
        sendToMaster(result);
        // System.out.println(SerialDataDumper.serialDataDumper(EndianUtil.BIG_ENDIAN, result));
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ReturnWrapper [federationId=" + this.federationId + ", returnAddress=" + this.returnAddress + ", ourAddress="
                + this.ourAddress + ", messageTypeId=" + this.messageTypeId + ", messageId=" + this.messageId + ", replyCount="
                + this.replyCount + "]";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.federationId == null) ? 0 : this.federationId.hashCode());
        result = prime * result + ((this.messageId == null) ? 0 : this.messageId.hashCode());
        result = prime * result + ((this.messageTypeId == null) ? 0 : this.messageTypeId.hashCode());
        result = prime * result + ((this.ourAddress == null) ? 0 : this.ourAddress.hashCode());
        result = prime * result + ((this.returnAddress == null) ? 0 : this.returnAddress.hashCode());
        return result; // replyCount is NOT used!
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:needbraces")
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReturnWrapperImpl other = (ReturnWrapperImpl) obj;
        if (this.federationId == null)
        {
            if (other.federationId != null)
                return false;
        }
        else if (!this.federationId.equals(other.federationId))
            return false;
        if (this.messageId == null)
        {
            if (other.messageId != null)
                return false;
        }
        else if (!this.messageId.equals(other.messageId))
            return false;
        if (this.messageTypeId == null)
        {
            if (other.messageTypeId != null)
                return false;
        }
        else if (!this.messageTypeId.equals(other.messageTypeId))
            return false;
        if (this.ourAddress == null)
        {
            if (other.ourAddress != null)
                return false;
        }
        else if (!this.ourAddress.equals(other.ourAddress))
            return false;
        if (this.returnAddress == null)
        {
            if (other.returnAddress != null)
                return false;
        }
        else if (!this.returnAddress.equals(other.returnAddress))
            return false;
        return true; // replyCount is NOT used
    }
}

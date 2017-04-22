package org.sim0mq.message.util;

import org.sim0mq.Sim0MQException;
import org.sim0mq.message.MessageStatus;

/**
 * The message structure of a typical typed Sim0MQ simulation message looks as follows:<br>
 * Frame 0. Magic number = |9|0|0|0|5|S|I|M|#|#| where ## stands for the version number, e.g., 01.<br>
 * Frame 1. Simulation run id. Simulation run ids can be provided in different types. Examples are two 64-bit longs indicating a
 * UUID, or a String with a UUID number, a String with meaningful identification, or a short or an int with a simulation run
 * number. In order to check whether the right information has been received, the id can be translated to a String and compared
 * with an internal string representation of the required id.<br>
 * Frame 2. Message type id. Message type ids can be defined per type of simulation, and can be provided in different types.
 * Examples are a String with a meaningful identification, or a short or an int with a message type number. For interoperability
 * between different types of simulation, a String id with dot-notation (e.g., DSOL.1 for a simulator start message from DSOL or
 * OTS.14 for a statistics message from OpenTrafficSim) would be preferred.<br>
 * Frame 3. Message status id. Messages can be about something new (containing a definition that can be quite long), an update
 * (which is often just an id followed by a single number), and a deletion (which is often just an id).<br>
 * Frame 4. Number of fields. The number of fields in the payload is indicated to be able to check the payload and to avoid
 * reading past the end. The number of fields can be encoded using any length type (byte, short, int, long).<br>
 * Frame 5-n. Payload, where each field has a 1-byte prefix denoting the type of field.
 * <p>
 * Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://sim0mq.org/docs/current/license.html">Sim0MQ License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 3, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SimulationMessage
{
    /**
     * Do not instantiate this utility class.
     */
    private SimulationMessage()
    {
        // Utility class; do not instantiate.
    }

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
     * @param content the objects to encode
     * @return the zeroMQ message to send as a byte array
     * @throws Sim0MQException on unknown data type
     */
    public static byte[] encode(final Object simulationRunId, final Object senderId, final Object receiverId,
            final Object messageTypeId, final long messageId, final MessageStatus messageStatus, final Object... content)
            throws Sim0MQException
    {
        Object[] simulationContent = new Object[content.length + 8];
        simulationContent[0] = TypedMessage.version;
        simulationContent[1] = simulationRunId;
        simulationContent[2] = senderId;
        simulationContent[3] = receiverId;
        simulationContent[4] = messageTypeId;
        simulationContent[5] = messageId;
        simulationContent[6] = new Byte(messageStatus.getStatus());
        simulationContent[7] = new Short((short) content.length);
        for (int i = 0; i < content.length; i++)
        {
            simulationContent[i + 8] = content[i];
        }
        return TypedMessage.encode(simulationContent);
    }

    /**
     * Encode the object array into a message.
     * @param identity the identity of the federate to which this is the reply
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
     * @param content the objects to encode
     * @return the zeroMQ message to send as a byte array
     * @throws Sim0MQException on unknown data type
     */
    public static byte[] encodeReply(final String identity, final Object simulationRunId, final Object senderId, final Object receiverId,
            final Object messageTypeId, final long messageId, final MessageStatus messageStatus, final Object... content)
            throws Sim0MQException
    {
        Object[] simulationContent = new Object[content.length + 10];
        simulationContent[0] = identity;
        simulationContent[1] = new byte[] {0}; 
        simulationContent[2] = TypedMessage.version;
        simulationContent[3] = simulationRunId;
        simulationContent[4] = senderId;
        simulationContent[5] = receiverId;
        simulationContent[6] = messageTypeId;
        simulationContent[7] = messageId;
        simulationContent[8] = new Byte(messageStatus.getStatus());
        simulationContent[9] = new Short((short) content.length);
        for (int i = 0; i < content.length; i++)
        {
            simulationContent[i + 10] = content[i];
        }
        return TypedMessage.encode(simulationContent);
    }

    /**
     * Decode the message into an object array. Note that the message fields are coded as follows:<br>
     * 0 = magic number, equal to the String "SIM##" where ## stands for the version number of the protocol.<br>
     * 1 = simulation run id, could be String, int, Object, ...<br>
     * 2 = sender id, could be String, int, Object, ...<br>
     * 3 = receiver id, could be String, int, Object, ...<br>
     * 4 = message type id, could be String, int, Object, ...<br>
     * 5 = message id, as a long.<br>
     * 6 = message status, 1=NEW, 2=CHANGE, 3=DELETE.<br>
     * 7 = number of fields that follow.<br>
     * 8-n = payload, where the number of fields was defined by message[7].
     * @param message the ZeroMQ byte array to decode
     * @return an array of objects of the right type
     * @throws Sim0MQException on unknown data type
     */
    public static Object[] decode(final byte[] message) throws Sim0MQException
    {
        return TypedMessage.decode(message);
    }

    /**
     * Return a printable version of the message, e.g. for debugging purposes.
     * @param message the message to parse
     * @return a string representation of the message
     */
    public static String print(final Object[] message)
    {
        StringBuffer s = new StringBuffer();
        s.append("0. magic number     : " + message[0] + "\n");
        s.append("1. simulation run id: " + message[1] + "\n");
        s.append("2. sender id        : " + message[2] + "\n");
        s.append("3. receiver id      : " + message[3] + "\n");
        s.append("4. message type id  : " + message[4] + "\n");
        s.append("5. message id       : " + message[5] + "\n");
        s.append("6. message status   : " + MessageStatus.getTypes().get((int)(byte)message[6]) + "\n");
        s.append("7. number of fields : " + message[7] + "\n");
        int nr = ((Number) message[7]).intValue();
        if (message.length != nr + 8)
        {
            s.append("Error - number of fields not matched by message structure");
        }
        else
        {
            for (int i = 0; i < nr; i++)
            {
                s.append((8 + i) + ". message field    : " + message[8 + i] + "  (" + message[8 + i].getClass().getSimpleName()
                        + ")\n");
            }
        }
        return s.toString();
    }
    
    /**
     * Return a printable line with the payload of the message, e.g. for debugging purposes.
     * @param message the message to parse
     * @return a string representation of the message
     */
    public static String listPayload(final Object[] message)
    {
        StringBuffer s = new StringBuffer();
        s.append('|');
        int nr = ((Number) message[7]).intValue();
        if (message.length != nr + 8)
        {
            s.append("Error - number of fields not matched by message structure");
        }
        else
        {
            for (int i = 0; i < nr; i++)
            {
                s.append(message[8 + i] + " (" + message[8 + i].getClass().getSimpleName()
                        + ") | ");
            }
        }
        return s.toString();
    }

}

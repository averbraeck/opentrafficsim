package org.opentrafficsim.sim0mq.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.draw.OtsDrawingException;
import org.opentrafficsim.sim0mq.publisher.SubscriptionHandler;
import org.sim0mq.Sim0MQException;
import org.sim0mq.message.Sim0MQMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DsolException;

/**
 * Unit tests. This requires half of OTS in the imports because it sets up a simulation and runs that for a couple of seconds.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class Sim0MQPublisherTest
{

    /**
     * Verify an ACK or a NACK message.
     * @param got byte[]; the not-yet-decoded message that is expected to decode into an ACK or a NACK
     * @param field5 String; expected content for the message type id field
     * @param field6 int; expected content for the message id field
     * @param expectedValue Boolean; expected Boolean value for the first payload field (field 8)
     * @param expectedDescription String; expected String value for the second and last payload field (field 9)
     * @throws Sim0MQException when that happens, this test has failed
     * @throws SerializationException when that happens this test has failed
     */
    public void verifyAckNack(final byte[] got, final String field5, final int field6, final Boolean expectedValue,
            final String expectedDescription) throws Sim0MQException, SerializationException
    {
        Object[] objects = Sim0MQMessage.decodeToArray(got);
        assertEquals(field5, objects[5], "Field 5 of message echos the command");
        assertEquals(field6, objects[6], "conversation id (field 6) matches");
        assertEquals(10, objects.length, "Response has 2 field payload");
        assertTrue(objects[8] instanceof Boolean, "First payload field is a boolean");
        assertEquals(expectedValue, objects[8], "First payload field has the expected value");
        assertTrue(objects[9] instanceof String, "Second (and last) payload field is a String");
        if (!((String) objects[9]).startsWith(expectedDescription))
        {
            fail("Description of ACK/NACK does not start with \"" + expectedDescription + "\" instead it contains \""
                    + objects[9] + "\"");
        }
    }

    /**
     * Wait for an incoming message and verify that it is an ACK or a NACK.
     * @param receivedMessages List&lt;byte[]&gt;; the list where incoming messages should appear
     * @param maximumSeconds double; maximum time to wait
     * @param field5 String; expected content for the message type id field
     * @param field6 int; expected content for the message id field
     * @param expectedValue Boolean; expected Boolean value for the first payload field (field 8)
     * @param expectedDescription String; expected String value for the second and last payload field (field 9)
     * @throws Sim0MQException when that happens, this test has failed
     * @throws SerializationException when that happens this test has failed
     * @throws InterruptedException when that happens this test has failed
     */
    public void waitAndVerifyAckNack(final List<byte[]> receivedMessages, final double maximumSeconds, final String field5,
            final int field6, final Boolean expectedValue, final String expectedDescription)
            throws Sim0MQException, SerializationException, InterruptedException
    {
        waitForReceivedMessages(receivedMessages, maximumSeconds);
        assertEquals(1, receivedMessages.size(), "Should have received one message");
        verifyAckNack(receivedMessages.get(0), field5, field6, expectedValue, expectedDescription);
        receivedMessages.clear();
    }

    /**
     * Test code.
     * @throws IOException if that happens uncaught; this test has failed
     * @throws NamingException if that happens uncaught; this test has failed
     * @throws SimRuntimeException if that happens uncaught; this test has failed
     * @throws DsolException if that happens uncaught; this test has failed
     * @throws OtsDrawingException if that happens uncaught; this test has failed
     * @throws SerializationException if that happens uncaught; this test has failed
     * @throws Sim0MQException if that happens uncaught; this test has failed
     * @throws InterruptedException if that happens uncaught; this test has failed
     * @throws URISyntaxException if network.xml file not found
     */
    // FIXME: The test has null pointer exceptions... @Test
    public void testSim0MQPublisher() throws IOException, SimRuntimeException, NamingException, DsolException,
            OtsDrawingException, Sim0MQException, SerializationException, InterruptedException, URISyntaxException
    {
        ZContext zContext = new ZContext(5); // 5 IO threads - how many is reasonable? It actually works with 1 IO thread.
        networkXML = new String(Files.readAllBytes(Paths.get(URLResource.getResource("/resources/network.xml").toURI())));

        List<byte[]> receivedMessages = Collections.synchronizedList(new ArrayList<>());
        List<byte[]> synchronizedReceivedMessages = Collections.synchronizedList(receivedMessages);
        ReadMessageThread readMessageThread = new ReadMessageThread(zContext, synchronizedReceivedMessages);
        readMessageThread.start();

        PublisherThread publisherThread = new PublisherThread(zContext);
        publisherThread.start();

        ZMQ.Socket publisherControlSocket = zContext.createSocket(SocketType.PUSH);
        publisherControlSocket.connect("inproc://publisherControl");

        int conversationId = 100; // Number the commands starting with something that is very different from 0
        String badCommand = "THIS_IS_NOT_A_SUPPORTED_COMMAND";
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", badCommand, ++conversationId));
        waitAndVerifyAckNack(receivedMessages, 1.0, badCommand, conversationId, false, "Don't know how to handle message:");

        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "SIMULATEUNTIL",
                ++conversationId, new Object[] {new Time(10, TimeUnit.BASE_SECOND)}));
        waitAndVerifyAckNack(receivedMessages, 1.0, "SIMULATEUNTIL", conversationId, false, "No network loaded");

        badCommand = "GTUs in network|SUBSCRIBE_TO_ADD";
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", badCommand, ++conversationId));
        waitAndVerifyAckNack(receivedMessages, 1.0, "GTUs in network", conversationId, false,
                "No simulation loaded; cannot execute command GTUs in network|SUBSCRIBE_TO_ADD");

        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "NEWSIMULATION",
                ++conversationId, networkXML, new Duration(60, DurationUnit.SECOND), Duration.ZERO, 123456L));
        waitAndVerifyAckNack(receivedMessages, 10.0, "NEWSIMULATION", conversationId, true, "OK");

        // Discover what services and commands are available
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "|GET_LIST", ++conversationId));
        waitForReceivedMessages(receivedMessages, 1);
        assertEquals(1, receivedMessages.size(), "Should have received one message");
        Object[] commands = Sim0MQMessage.decodeToArray(receivedMessages.get(0));
        assertTrue(commands.length > 8, "message decodes into more than 8 fields");
        for (int index = 8; index < commands.length; index++)
        {
            receivedMessages.clear();
            assertTrue(commands[index] instanceof String, "A service is identified by a String");
            String service = (String) commands[index];
            System.out.println("Service " + service);
            sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave",
                    service + "|" + SubscriptionHandler.Command.GET_COMMANDS, ++conversationId));
            waitForReceivedMessages(receivedMessages, 1.0);
            if (receivedMessages.size() > 0)
            {
                Object[] result = Sim0MQMessage.decodeToArray(receivedMessages.get(0));
                assertTrue(result.length >= 8, "result of GET_COMMANDS should be at least 8 long");
                for (int i = 8; i < result.length; i++)
                {
                    String command = (String) result[i];
                    receivedMessages.clear();
                    // System.out.println("trying command " + service + "|" + command);
                    sendCommand(publisherControlSocket,
                            Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", service + "|" + command, ++conversationId));
                    waitForReceivedMessages(receivedMessages, 1.0);
                    if (receivedMessages.size() > 0)
                    {
                        for (int ii = 8; ii < receivedMessages.size(); ii++)
                        {
                            System.out.println(Sim0MQMessage.print(Sim0MQMessage.decodeToArray(receivedMessages.get(ii))));
                        }
                    }
                    else
                    {
                        System.out.println("Received no reply");
                    }
                    System.out.print(""); // Good for a breakpoint
                }
            }
            else
            {
                System.out.println("Received no reply to GET_COMMANDS request");
            }
        }
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTU move|SUBSCRIBE_TO_CHANGE",
                ++conversationId, "2", "BAD")); // Too many fields
        waitAndEatMessagesUntilConversationId(receivedMessages, 1.0, conversationId);
        waitAndVerifyAckNack(receivedMessages, 1.0, "GTU move", conversationId, false, "Bad address");
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTU move|SUBSCRIBE_TO_CHANGE", ++conversationId));
        // Too few fields
        waitAndVerifyAckNack(receivedMessages, 1.0, "GTU move", conversationId, false,
                "Bad address: Address for GTU Id has wrong length");
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTU move|SUBSCRIBE_TO_CHANGE",
                ++conversationId, "NON EXISTING GTU ID")); // GTU id is not (currently) in use
        waitAndVerifyAckNack(receivedMessages, 1.0, "GTU move", conversationId, false, "No GTU with id");
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_CURRENT", ++conversationId));
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|SUBSCRIBE_TO_ADD", ++conversationId));
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "SIMULATEUNTIL",
                ++conversationId, new Object[] {new Time(10, TimeUnit.BASE_SECOND)}));
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_CURRENT", ++conversationId));
        int conversationIdForSubscribeToAdd = ++conversationId; // We need that to unsubscribe later
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave",
                "GTUs in network|SUBSCRIBE_TO_ADD", conversationIdForSubscribeToAdd));
        waitAndEatMessagesUntilConversationId(receivedMessages, 1.0, conversationIdForSubscribeToAdd);
        waitAndVerifyAckNack(receivedMessages, 1.0, "GTUs in network", conversationIdForSubscribeToAdd, true,
                "Subscription created");
        int conversationIdForGTU2Move = ++conversationId;
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTU move|SUBSCRIBE_TO_CHANGE",
                conversationIdForGTU2Move, "2")); // Subscribe to move events of GTU 2
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "SIMULATEUNTIL",
                ++conversationId, new Object[] {new Time(20, TimeUnit.BASE_SECOND)}));
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_CURRENT", ++conversationId));
        waitAndEatMessagesUntilConversationId(receivedMessages, 1.0, conversationId);
        // unsubscribe from GTU ADD events using the previously saved conversationId
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave",
                "GTUs in network|UNSUBSCRIBE_FROM_ADD", conversationIdForSubscribeToAdd));
        waitAndEatMessagesUntilConversationId(receivedMessages, 1.0, conversationIdForSubscribeToAdd);
        waitAndVerifyAckNack(receivedMessages, 1.0, "GTUs in network", conversationIdForSubscribeToAdd, true,
                "Subscription removed");
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave",
                "GTU move|UNSUBSCRIBE_FROM_CHANGE", conversationIdForGTU2Move, "2")); // Subscribe to move events of GTU 2
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "SIMULATEUNTIL",
                ++conversationId, new Object[] {new Time(30, TimeUnit.BASE_SECOND)}));
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_CURRENT", ++conversationId));
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave",
                "GTUs in network|GET_ADDRESS_META_DATA", ++conversationId));
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "SIMULATEUNTIL",
                ++conversationId, new Object[] {new Time(60, TimeUnit.BASE_SECOND)}));
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "SIMULATEUNTIL",
                ++conversationId, new Object[] {new Time(70, TimeUnit.BASE_SECOND)}));
        waitAndEatMessagesUntilConversationId(receivedMessages, 1.0, conversationId);
        waitAndVerifyAckNack(receivedMessages, 1.0, "SIMULATEUNTIL", conversationId, false,
                "Simulation is already at end of simulation time");

        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "DIE", ++conversationId));
        System.out.println("Master has sent last command; Publisher should be busy for a while and then die");
        System.out.println("Master joining publisher thread (this should block until publisher has died)");
        publisherThread.join();
        System.out.println("Master has joined publisher thread");
        System.out.println("Master interrupts read message thread");
        readMessageThread.interrupt();
        System.out.println("Master has interrupted read message thread; joining ...");
        readMessageThread.join();
        System.out.println("Master has joined read message thread");
        System.out.println("Master exits");
    }

    /**
     * Wait for incoming messages up to one that has a specified conversation id, or until 1000 times time out.
     * @param receivedMessages List&lt;byte[]&gt;; the list to monitor
     * @param maximumSeconds double; how long to wait (in seconds)
     * @param conversationId int; the conversation id to wait for
     * @throws Sim0MQException when that happens, this test has failed
     * @throws SerializationException when that happens, this test has failed
     * @throws InterruptedException when that happens, this test has failed
     */
    public void waitAndEatMessagesUntilConversationId(final List<byte[]> receivedMessages, final double maximumSeconds,
            final int conversationId) throws Sim0MQException, SerializationException, InterruptedException
    {
        for (int attempt = 0; attempt < 1000; attempt++)
        {
            waitForReceivedMessages(receivedMessages, 1.0);
            // System.out.println("attempt = " + attempt + " received " + receivedMessages.size() + " message(s)");
            while (receivedMessages.size() > 1)
            {
                receivedMessages.remove(0);
            }
            if (receivedMessages.size() == 1)
            {
                Object[] objects = Sim0MQMessage.decodeToArray(receivedMessages.get(0));
                if (objects[6].equals(conversationId))
                {
                    break;
                }
                receivedMessages.remove(0);
            }
        }

    }

    /**
     * Sleep up to 1 second waiting for at least one message to be received.
     * @param receivedMessages List&lt;byte[]&gt;; the list to monitor
     * @param maximumSeconds double; how long to wait (in seconds)
     * @throws InterruptedException when that happens uncaught; this test has failed
     */
    static void waitForReceivedMessages(final List<byte[]> receivedMessages, final double maximumSeconds)
            throws InterruptedException
    {
        double timeWaited = 0;
        while (receivedMessages.size() == 0 && timeWaited < maximumSeconds)
        {
            Thread.sleep(10);
            timeWaited += 0.01;
        }
    }

    /**
     * Wrapper for ZMQ.Socket.send that may output some debugging information.
     * @param socket ZMQ.Socket; the socket to send onto
     * @param message byte[]; the message to transmit
     */
    static void sendCommand(final ZMQ.Socket socket, final byte[] message)
    {
        // try
        // {
        // Object[] unpackedMessage = Sim0MQMessage.decodeToArray(message);
        // System.out.println("Master sending command " + unpackedMessage[5] + " conversation id " + unpackedMessage[6]);
        // }
        // catch (Sim0MQException | SerializationException e)
        // {
        // e.printStackTrace();
        // }
        socket.send(message);
    }

    /**
     * Repeatedly try to read all available messages.
     */
    static class ReadMessageThread extends Thread
    {
        /** The ZContext needed to create the socket. */
        private final ZContext zContext;

        /** Storage for the received messages. */
        private final List<byte[]> storage;

        /**
         * Repeatedly read all available messages.
         * @param zContext ZContext; the ZContext needed to create the read socket
         * @param storage List&lt;String&gt;; storage for the received messages
         */
        ReadMessageThread(final ZContext zContext, final List<byte[]> storage)
        {
            this.zContext = zContext;
            this.storage = storage;
        }

        @Override
        public void run()
        {
            System.out.println("Read message thread starting up");
            ZMQ.Socket socket = this.zContext.createSocket(SocketType.PULL);
            socket.setReceiveTimeOut(100);
            socket.bind("inproc://publisherOutput");
            while (!Thread.interrupted())
            {
                byte[][] all = readMessages(socket);
                for (byte[] one : all)
                {
                    this.storage.add(one);
                }
            }
            System.out.println("Read message thread exits due to interrupt");
        }

    }

    /**
     * Read as many messages from a ZMQ socket as are available. Do NOT block when there are no (more) messages to read.
     * @param socket ZMQ.Socket; the socket
     * @return byte[][]; the read messages
     */
    public static byte[][] readMessages(final ZMQ.Socket socket)
    {
        List<byte[]> resultList = new ArrayList<>();
        while (true)
        {
            byte[] message = socket.recv();
            StringBuilder output = new StringBuilder();
            if (null != message)
            {
                output.append("Master received " + message.length + " byte message: ");
                // System.out.println(SerialDataDumper.serialDataDumper(EndianUtil.BIG_ENDIAN, message));
                try
                {
                    Object[] fields = Sim0MQMessage.decodeToArray(message);
                    for (Object field : fields)
                    {
                        output.append("/" + field);
                    }
                    output.append("/");
                }
                catch (Sim0MQException | SerializationException e)
                {
                    e.printStackTrace();
                }
                System.out.println(output);
                resultList.add(message);
            }
            else
            {
                if (resultList.size() > 0)
                {
                    System.out.println(
                            "Master picked up " + resultList.size() + " message" + (resultList.size() == 1 ? "" : "s"));
                }
                break;
            }
        }
        return resultList.toArray(new byte[resultList.size()][]);
    }

    /**
     * Thread that runs a PublisherExperiment.
     */
    static class PublisherThread extends Thread
    {
        /** Passed onto the constructor of PublisherExperimentUsingSockets. */
        private final ZContext zContext;

        /**
         * Construct a new PublisherThread.
         * @param zContext ZContext; needed to construct the PublisherExperimentUsingSockets
         */
        PublisherThread(final ZContext zContext)
        {
            this.zContext = zContext;
        }

        /**
         * Construct a new PublisherThread.
         */
        PublisherThread()
        {
            this.zContext = new ZContext(5);
        }

        @Override
        public void run()
        {
            new Sim0mqPublisher(this.zContext, "publisherControl", "publisherOutput");
            System.out.println("Publisher thread exits");
        }

    }

    /** The test network. */
    private static String networkXML;

}

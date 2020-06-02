package org.sim0mq.publisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.sim0mq.Sim0MQException;
import org.sim0mq.message.Sim0MQMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DSOLException;

/**
 * Experiment with the Sim0MQPublisher.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class PublisherDemo
{
    /** Do not instantiate. */
    private PublisherDemo()
    {
        // Do not instantiate
    }

    /**
     * Test code.
     * @param args String[]; the command line arguments (not used)
     * @throws IOException ...
     * @throws NamingException ...
     * @throws SimRuntimeException ...
     * @throws DSOLException ...
     * @throws OTSDrawingException ...
     * @throws SerializationException ...
     * @throws Sim0MQException ...
     * @throws InterruptedException ...
     */
    public static void main(final String[] args) throws IOException, SimRuntimeException, NamingException, DSOLException,
            OTSDrawingException, Sim0MQException, SerializationException, InterruptedException
    {
        ZContext zContext = new ZContext(5); // 5 IO threads - how many is reasonable? It actually works with 1 IO thread.

        ReadMessageThread readMessageThread = new ReadMessageThread(zContext);
        readMessageThread.start();

        PublisherThread publisherThread = new PublisherThread(zContext);
        publisherThread.start();

        ZMQ.Socket publisherControlSocket = zContext.createSocket(SocketType.PUSH);
        publisherControlSocket.connect("inproc://publisherControl");

        int conversationId = 100; // Number the commands starting with something that is very different from 0
        String xml = new String(Files
                .readAllBytes(Paths.get("C:/Users/pknoppers/Java/ots-demo/src/main/resources/TrafCODDemo2/TrafCODDemo2.xml")));
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "NEWSIMULATION",
                conversationId++, xml, new Duration(3600, DurationUnit.SECOND), Duration.ZERO, 123456L));
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "|GET_CURRENT", conversationId++));
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_CURRENT", conversationId++));
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "SIMULATEUNTIL",
                conversationId++, new Object[] { new Time(10, TimeUnit.BASE_SECOND) }));
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_CURRENT", conversationId++));
        int conversationIdForSubscribeToAdd = conversationId++; // We need that to unsubscribe later
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave",
                "GTUs in network|SUBSCRIBE_TO_ADD", conversationIdForSubscribeToAdd));
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTU move|GET_RESULT_META_DATA", conversationId++));
        int conversationIdForGTU2Move = conversationId++;
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTU move|SUBSCRIBE_TO_CHANGE",
                conversationIdForGTU2Move, "2")); // Subscribe to move events of GTU 2
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "SIMULATEUNTIL",
                conversationId++, new Object[] { new Time(20, TimeUnit.BASE_SECOND) }));
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_CURRENT", conversationId++));
        // unsubscribe from GTU ADD events using saved conversationId
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave",
                "GTUs in network|UNSUBSCRIBE_FROM_ADD", conversationIdForSubscribeToAdd));
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave",
                "GTU move|UNSUBSCRIBE_FROM_CHANGE", conversationIdForGTU2Move, "2")); // Subscribe to move events of GTU 2
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "SIMULATEUNTIL",
                conversationId++, new Object[] { new Time(30, TimeUnit.BASE_SECOND) }));
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_CURRENT", conversationId++));
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave",
                "GTUs in network|GET_ADDRESS_META_DATA", conversationId++));
        sendCommand(publisherControlSocket,
                Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_RESULT_META_DATA", conversationId++));
        sendCommand(publisherControlSocket, Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "DIE", conversationId++));
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
     * Wrapper for ZMQ.Socket.send that may output some debugging information.
     * @param socket ZMQ.Socket; the socket to send onto
     * @param message byte[]; the message to transmit
     */
    static void sendCommand(final ZMQ.Socket socket, final byte[] message)
    {
        try
        {
            Object[] unpackedMessage = Sim0MQMessage.decodeToArray(message);
            System.out.println("Master sending command " + unpackedMessage[5] + " conversation id " + unpackedMessage[6]);
        }
        catch (Sim0MQException | SerializationException e)
        {
            e.printStackTrace();
        }
        socket.send(message);
    }

    /**
     * Repeatedly try to read all available messages.
     */
    static class ReadMessageThread extends Thread
    {
        /** The ZContext needed to create the socket. */
        private final ZContext zContext;

        /**
         * Repeatedly read all available messages.
         * @param zContext ZContext; the ZContext needed to create the read socket.
         */
        ReadMessageThread(final ZContext zContext)
        {
            this.zContext = zContext;
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
                readMessages(socket);
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
                        output.append("|" + field);
                    }
                    output.append("|");
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
            try
            {
                new Sim0MQPublisher(zContext);
            }
            catch (SimRuntimeException | IOException | NamingException | OTSDrawingException | DSOLException e)
            {
                e.printStackTrace();
            }
            System.out.println("Publisher thread exits");
        }

    }
}

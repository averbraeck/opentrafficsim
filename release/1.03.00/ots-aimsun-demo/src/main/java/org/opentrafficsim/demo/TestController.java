package org.opentrafficsim.demo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.djutils.io.URLResource;
import org.opentrafficsim.aimsun.proto.AimsunControlProtoBuf;

import com.google.protobuf.CodedOutputStream;

/**
 * Test client for AimsunController.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 18, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class TestController
{
    /**
     * Cannot be instantiated.
     */
    private TestController()
    {
        // Do not instantiate.
    }

    /**
     * Test client for AimsunControl.
     * <p>
     * (c) copyright 2002-2018 <a href="http://www.simulation.tudelft.nl">Delft University of Technology</a>. <br>
     * BSD-style license. See <a href="http://www.simulation.tudelft.nl/dsol/3.0/license.html">DSOL License</a>. <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @version Oct 21, 2016
     */
    /**
     * @param args String[]; command line arguments
     * @throws IOException when communication fails
     */
    public static void main(final String[] args) throws IOException
    {
        String ip = null;
        Integer port = null;

        for (String arg : args)
        {
            int equalsPos = arg.indexOf("=");
            if (equalsPos < 0)
            {
                System.err.println("Unhandled argument \"" + arg + "\"");
            }
            String key = arg.substring(0, equalsPos);
            String value = arg.substring(equalsPos + 1);
            switch (key.toUpperCase())
            {
                case "IP":
                    ip = value;
                    break;
                case "PORT":
                    try
                    {
                        port = Integer.parseInt(value);
                    }
                    catch (NumberFormatException exception)
                    {
                        System.err.println("Bad port number \"" + value + "\"");
                        System.exit(1);
                    }
                    break;
                default:
                    System.err.println("Unhandled argument \"" + arg + "\"");
            }
        }
        if (null == ip || null == port)
        {
            System.err.println("Missing required argument(s) ip=<ip-number_or_hostname> port=<port-number>");
            System.exit(1);
        }
        // Construct the create simulation command (including the network description in XML
        AimsunControlProtoBuf.CreateSimulation.Builder createSimulationBuilder =
                AimsunControlProtoBuf.CreateSimulation.newBuilder();
        // createSimulationBuilder.setRunTime(3600d);
        // createSimulationBuilder.setWarmUpTime(0d);
        // String network = URLResource.getResource("/aimsun/singleRoad.xml").toString(); // wrong; fix later
        // String networkResource = "/aimsun/singleRoad.xml";
        String networkResource = "C:/Temp/AimsunOtsNetwork.xml";
        String network = null; // IOUtils.toString(URLResource.getResource(networkResource));
        URL networkURL = URLResource.getResource(networkResource);
        if (null == networkURL)
        {
            throw new Error("Could not load network from resource " + networkResource);
        }
        URLConnection conn = networkURL.openConnection();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)))
        {
            network = reader.lines().collect(Collectors.joining("\n"));
        }

        // Socket to talk to server
        System.out.println("Connecting to server...");
        Socket socket = new Socket(ip, port);
        System.out.println("Connected");
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        // Send a create simulation command
        createSimulationBuilder.setNetworkXML(network);
        System.out.println("Sending CREATESIMULATION message");
        sendProtoMessage(outputStream,
                AimsunControlProtoBuf.OTSMessage.newBuilder().setCreateSimulation(createSimulationBuilder.build()).build());
        // Simulate 3600 seconds in 1 second steps
        for (int step = 1; step <= 3600; step++)
        {
            AimsunControlProtoBuf.SimulateUntil simulateUntil =
                    AimsunControlProtoBuf.SimulateUntil.newBuilder().setTime(1d * step).build();
            System.out.println("Sending simulate up to step " + step + " command");
            sendProtoMessage(outputStream,
                    AimsunControlProtoBuf.OTSMessage.newBuilder().setSimulateUntil(simulateUntil).build());
            System.out.println("Receive reply");
            AimsunControlProtoBuf.OTSMessage reply = receiveProtoMessage(inputStream);
            // System.out.println("Received " + reply);
            if (!reply.getGtuPositions().getStatus().contains("OK"))
            {
                System.out.println("status is " + reply.getGtuPositions().getStatus());
                break;
            }
        }
        System.out.println("Simulation stopped. Press return to exit");
        System.in.read();
        socket.close();
    }

    /**
     * Transit one message to the OTS server.
     * @param outputStream OutputStream; output stream to the OTS server
     * @param message AimsunControlProtoBuf.OTSMessage; the message
     * @throws IOException when communication fails in any way
     */
    public static void sendProtoMessage(final OutputStream outputStream, final AimsunControlProtoBuf.OTSMessage message)
            throws IOException
    {
        int size = message.getSerializedSize();
        // System.out.println("About to transmit message of " + size + " bytes");
        byte[] sizeBytes = new byte[4];
        sizeBytes[0] = (byte) ((size >> 24) & 0xff);
        sizeBytes[1] = (byte) ((size >> 16) & 0xff);
        sizeBytes[2] = (byte) ((size >> 8) & 0xff);
        sizeBytes[3] = (byte) (size & 0xff);
        outputStream.write(sizeBytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        message.writeTo(CodedOutputStream.newInstance(baos));
        byte[] buffer = new byte[size];
        buffer = message.toByteArray();
        outputStream.write(buffer);
        // System.out.println("Done");
    }

    /**
     * Read one OTSMessage.
     * @param inputStream InputStream; input stream to read from
     * @return OTSMessage; the OTSMessage that was constructed from the read bytes
     * @throws IOException when communication fails
     */
    public static AimsunControlProtoBuf.OTSMessage receiveProtoMessage(final InputStream inputStream) throws IOException
    {
        byte[] sizeBytes = receiveBytes(inputStream, 4);
        // for (int i = 0; i < 4; i++)
        // {
        // System.out.print(String.format("%d ", sizeBytes[i]));
        // }
        int size = ((sizeBytes[0] & 0xff) << 24) + ((sizeBytes[1] & 0xff) << 16) + ((sizeBytes[2] & 0xff) << 8)
                + (sizeBytes[3] & 0xff);
        // System.out.println(String.format("-> %d", size));
        byte[] messageBytes = receiveBytes(inputStream, size);
        return AimsunControlProtoBuf.OTSMessage.parseFrom(messageBytes);
    }

    /**
     * Read a specified number of bytes.
     * @param inputStream InputStream; input stream to read from
     * @param size int; number of bytes to read
     * @return byte[]; byte array filled with the read bytes
     * @throws IOException when communication fails
     */
    public static byte[] receiveBytes(final InputStream inputStream, final int size) throws IOException
    {
        System.out.print("Need to read " + size + " bytes ... ");
        int offset = 0;
        byte[] buffer = new byte[size];
        while (true)
        {
            int bytesRead = inputStream.read(buffer, offset, buffer.length - offset);
            if (-1 == bytesRead)
            {
                break;
            }
            offset += bytesRead;
            if (buffer.length == offset)
            {
                System.out.println("Got all " + buffer.length + " requested bytes");
                break;
            }
            if (buffer.length < offset)
            {
                System.out.println("Oops: Got more than " + buffer.length + " requested bytes");
                break;
            }
            System.out.print("Now got " + offset + " bytes; need to read " + (buffer.length - offset) + " more bytes ");
        }
        if (offset != buffer.length)
        {
            throw new IOException("Got only " + offset + " of expected " + buffer.length + " bytes");
        }
        return buffer;
    }

}

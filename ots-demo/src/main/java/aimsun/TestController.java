package aimsun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import nl.tudelft.simulation.language.io.URLResource;

/**
 * Test client for AimsunController.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * (c) copyright 2002-2017 <a href="http://www.simulation.tudelft.nl">Delft University of Technology</a>. <br>
     * BSD-style license. See <a href="http://www.simulation.tudelft.nl/dsol/3.0/license.html">DSOL License</a>. <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @version Oct 21, 2016
     */
    /**
     * @param args command line arguments
     * @throws IOException when communication fails
     */
    public static void main(final String[] args) throws IOException
    {
        // Socket to talk to server
        System.out.println("Connecting to server...");
        Socket socket = new Socket("localhost", 3333);
        System.out.println("Connected");
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        // Send a build network command
        System.out.println("Sending 1st test message");
        AimsunControlProtoBuf.CreateSimulation.Builder createSimulationBuilder =
                AimsunControlProtoBuf.CreateSimulation.newBuilder();
        createSimulationBuilder.setRunTime(3600d);
        createSimulationBuilder.setWarmUpTime(0d);
        // String network = URLResource.getResource("/aimsun/singleRoad.xml").toString(); // wrong; fix later
        String network = null; // IOUtils.toString(URLResource.getResource("/aimsun/singleRoad.xml"));
        URLConnection conn = URLResource.getResource("/aimsun/singleRoad.xml").openConnection();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)))
        {
            network = reader.lines().collect(Collectors.joining("\n"));
        }

        createSimulationBuilder.setNetworkXML(network);
        AimsunControlProtoBuf.OTSMessage command =
                AimsunControlProtoBuf.OTSMessage.newBuilder().setCreateSimulation(createSimulationBuilder.build()).build();
        // for (byte b : command.toByteArray())
        // {
        // System.out.print(String.format("%02x ", b));
        // }
        // System.out.println("");
        command.writeDelimitedTo(outputStream);
        // Send a simulate until command with time 0 to retrieve the initial gtu positions
        System.out.println("Sending simulate until 0 command to retrieve the initial GTU positions");
        AimsunControlProtoBuf.SimulateUntil simulateUntil =
                AimsunControlProtoBuf.SimulateUntil.newBuilder().setTime(0d).build();
        command = AimsunControlProtoBuf.OTSMessage.newBuilder().setSimulateUntil(simulateUntil).build();
        command.writeDelimitedTo(outputStream);
        // Receive a reply
        System.out.println("Waiting for / reading reply");
        AimsunControlProtoBuf.OTSMessage reply = AimsunControlProtoBuf.OTSMessage.parseDelimitedFrom(inputStream);
        System.out.println("Received " + reply);
        for (int step = 1; step <= 20; step++)
        {
            simulateUntil = AimsunControlProtoBuf.SimulateUntil.newBuilder().setTime(0.5d * step).build();
            System.out.println("Simulate step " + step);
            command = AimsunControlProtoBuf.OTSMessage.newBuilder().setSimulateUntil(simulateUntil).build();
            command.writeDelimitedTo(outputStream);
            System.out.println("Waiting for / reading reply");
            reply = AimsunControlProtoBuf.OTSMessage.parseDelimitedFrom(inputStream);
            System.out.println("Received " + reply);
        }
        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException exception)
        {
            exception.printStackTrace();
        }
        socket.close();
    }
}

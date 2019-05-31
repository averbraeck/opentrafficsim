package org.opentrafficsim.aimsun;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.aimsun.proto.AimsunControlProtoBuf;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.SimpleAnimator;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 18, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class AimsunControl extends AbstractWrappableAnimation
{

    /** */
    private static final long serialVersionUID = 20160418L;

    /** XML description of the network. */
    private String networkXML = null;

    /** Currently active Aimsun model. */
    private AimsunModel model = null;

    /**
     * Program entry point.
     * @param args String[]; the command line arguments
     */
    public static void main(final String[] args)
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
        try
        {
            System.out.println("Creating server socket for port " + port);
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true); // Ensure we can be restarted without the normal delay
            System.out.println("Waiting for client to connect");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected; closing server socket");
            serverSocket.close(); // don't accept any other connections
            System.out.println("Socket time out is " + clientSocket.getSoTimeout());
            clientSocket.setSoTimeout(0);
            System.out.println("Entering command loop");
            AimsunControl aimsunControl = new AimsunControl();
            aimsunControl.commandLoop(clientSocket);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        System.exit(0);
    }
    
    /**
     * Create a nice hex dump of a bunch of bytes.
     * @param bytes byte[]; the bytes
     * @return String; the hex dump
     */
    public static String hexDump(final byte[] bytes)
    {
        StringBuilder result = new StringBuilder();
        int pos = 0;
        for (byte b : bytes)
        {
            result.append(String.format("%02x", b));
            if (pos % 16 == 0)
            {
                result.append("\r\n");
            }
            else if (pos % 8 == 0)
            {
                result.append("  ");
            }
            else
            {
                result.append(" ");
            }
        }
        return result.toString();
    }

    /**
     * Process incoming commands.
     * @param socket Socket; the communications channel to Aimsun
     * @throws IOException when communication with Aimsun fails
     */
    private void commandLoop(final Socket socket) throws IOException
    {
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        while (true)
        {
            // byte[] in = new byte[1];
            // inputStream.read(in);
            // System.out.println(String.format("Got byte %02x", in[0]));
            try
            {
                byte[] sizeBytes = new byte[4];
                // inputStream.read(sizeBytes);
                fillBuffer(inputStream, sizeBytes);
                int size =
                        ((sizeBytes[0] & 0xff) << 24) + ((sizeBytes[1] & 0xff) << 16) + ((sizeBytes[2] & 0xff) << 8)
                                + (sizeBytes[3] & 0xff);
                System.out.println("expecting " + size + " bytes");
                byte[] buffer = new byte[size];
                // inputStream.read(buffer);
                fillBuffer(inputStream, buffer);
                AimsunControlProtoBuf.OTSMessage message = AimsunControlProtoBuf.OTSMessage.parseFrom(buffer);

                // AimsunControlProtoBuf.OTSMessage message = AimsunControlProtoBuf.OTSMessage.parseDelimitedFrom(inputStream);
                if (null == message)
                {
                    System.out.println("Connection terminated; exiting");
                    break;
                }
                switch (message.getMsgCase())
                {
                    case CREATESIMULATION:
                        System.out.println("Received CREATESIMULATION message");
                        AimsunControlProtoBuf.CreateSimulation createSimulation = message.getCreateSimulation();
                        this.networkXML = createSimulation.getNetworkXML();
                        // String network = null; // IOUtils.toString(URLResource.getResource("/aimsun/singleRoad.xml"));
                        // URLConnection conn = URLResource.getResource("/aimsun/singleRoad.xml").openConnection();
                        // try (BufferedReader reader =
                        // new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)))
                        // {
                        // network = reader.lines().collect(Collectors.joining("\n"));
                        // }
                        // this.networkXML = network;
                        Duration runDuration = new Duration(createSimulation.getRunTime(), DurationUnit.SECOND);
                        Duration warmupDuration = new Duration(createSimulation.getWarmUpTime(), DurationUnit.SECOND);
                        try
                        {
                            SimpleAnimator animator =
                                    buildAnimator(Time.ZERO, warmupDuration, runDuration, new ArrayList<Property<?>>(), null,
                                            true);
                            animator.setSpeedFactor(Double.MAX_VALUE, true);
                        }
                        catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException exception1)
                        {
                            exception1.printStackTrace();
                        }
                        break;

                    case SIMULATEUNTIL:
                        System.out.println("Received SIMULATEUNTIL message");
                        if (null == this.model)
                        {
                            System.err.println("No model active");
                            socket.close();
                            break;
                        }
                        AimsunControlProtoBuf.SimulateUntil simulateUntil = message.getSimulateUntil();
                        Time stopTime = new Time(simulateUntil.getTime(), TimeUnit.BASE_SECOND);
                        System.out.println("Simulate until " + stopTime);
                        DEVSSimulator<Time, ?, ?> simulator = (DEVSSimulator<Time, ?, ?>) this.model.getSimulator();
                        try
                        {
                            simulator.runUpTo(stopTime);
                            while (simulator.isRunning())
                            {
                                try
                                {
                                    Thread.sleep(10);
                                }
                                catch (InterruptedException ie)
                                {
                                    ie = null; // ignore
                                }
                            }
                            AimsunControlProtoBuf.GTUPositions.Builder builder =
                                    AimsunControlProtoBuf.GTUPositions.newBuilder();
                            for (GTU gtu : this.model.getNetwork().getGTUs())
                            {
                                AimsunControlProtoBuf.GTUPositions.GTUPosition.Builder gpb =
                                        AimsunControlProtoBuf.GTUPositions.GTUPosition.newBuilder();
                                gpb.setGtuId(gtu.getId());
                                DirectedPoint dp = gtu.getOperationalPlan().getLocation(stopTime);
                                gpb.setX(dp.x);
                                gpb.setY(dp.y);
                                gpb.setZ(dp.z);
                                gpb.setAngle(dp.getRotZ());
                                builder.addGtuPos(gpb.build());
                            }
                            AimsunControlProtoBuf.GTUPositions gtuPositions = builder.build();
                            AimsunControlProtoBuf.OTSMessage.Builder resultBuilder =
                                    AimsunControlProtoBuf.OTSMessage.newBuilder();
                            resultBuilder.setGtuPositions(gtuPositions);
                            AimsunControlProtoBuf.OTSMessage result = resultBuilder.build();
                            System.out.println("About to transmit " + result.toString());
                            size = result.getSerializedSize();
                            sizeBytes[0] = (byte) ((size >> 24) & 0xff);
                            sizeBytes[1] = (byte) ((size >> 16) & 0xff);
                            sizeBytes[2] = (byte) ((size >> 8) & 0xff);
                            sizeBytes[3] = (byte) (size & 0xff);
                            outputStream.write(sizeBytes);
                            buffer = new byte[size];
                            buffer = result.toByteArray();
                            outputStream.write(buffer);
                            // result.writeDelimitedTo(outputStream);
                        }
                        catch (SimRuntimeException | OperationalPlanException exception)
                        {
                            System.out.println("Error in runUpTo");
                            exception.printStackTrace();
                        }
                        break;

                    case GTUPOSITIONS:
                        System.out.println("Received GTUPOSITIONS message SHOULD NOT HAPPEN");
                        socket.close();
                        return;

                    case MSG_NOT_SET:
                        System.out.println("Received MSG_NOT_SET message SHOULD NOT HAPPEN");
                        socket.close();
                        return;

                    default:
                        System.out.println("Received unknown message SHOULD NOT HAPPEN");
                        socket.close();
                        break;
                }
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
                break;
            }
        }
    }

    /**
     * Fill a buffer from a stream; retry until the buffer is entirely filled.
     * @param in InputStream; the input stream for the data
     * @param buffer byte[]; the buffer
     */
    static void fillBuffer(final InputStream in, final byte[] buffer)
    {
        System.out.println("Need to read " + buffer.length + " bytes");
        int offset = 0;
        while (true)
        {
            try
            {
                int bytesRead = in.read(buffer, offset, buffer.length - offset);
                if (-1 == bytesRead)
                {
                    break;
                }
                offset += bytesRead;
                if (offset >= buffer.length)
                {
                    System.out.println("Got all " + buffer.length + " requested bytes");
                    break;
                }
                System.out.println("Now got " + offset + " bytes; need to read " + (buffer.length - offset) + " more bytes");
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "AimsunControlledOTS";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "Aimsun controlled OTS engine";
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer) throws OTSSimulationException
    {
        this.model = new AimsunModel();
        return this.model;
    }

    /**
     * The network.
     */
    class AimsunModel extends EventProducer implements OTSModelInterface, EventListenerInterface
    {

        /** */
        private static final long serialVersionUID = 20170419L;

        /** The network. */
        private OTSNetwork network;

        /** The simulator. */
        private SimulatorInterface<Time, Duration, OTSSimTimeDouble> simulator;

        /** {@inheritDoc} */
        @Override
        public void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> theSimulator)
                throws SimRuntimeException, RemoteException
        {
            try
            {
                this.simulator = theSimulator;
                // URL url = URLResource.getResource("/aimsun/singleRoad.xml");
                XmlNetworkLaneParser nlp = new XmlNetworkLaneParser((OTSDEVSSimulatorInterface) theSimulator);
                String xml = AimsunControl.this.networkXML;
                this.network = nlp.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
        {
            return this.simulator;
        }

        /** {@inheritDoc} */
        @Override
        public void notify(final EventInterface event) throws RemoteException
        {
            // WIP
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

    }

}
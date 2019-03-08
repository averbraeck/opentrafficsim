package org.opentrafficsim.aimsun;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeClock;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.logger.LogCategory;
import org.opentrafficsim.aimsun.proto.AimsunControlProtoBuf;
import org.opentrafficsim.aimsun.proto.AimsunControlProtoBuf.GTUPositions;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.road.network.factory.xml.old.XmlNetworkLaneParserOld;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.opentrafficsim.swing.gui.OTSSwingApplication;
import org.pmw.tinylog.Level;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 18, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class AimsunControl
{
    /** XML description of the network. */
    private String networkXML = null;

    /** Currently active Aimsun model. */
    private AimsunModel model = null;

    /**
     * Program entry point.
     * @param args String[]; the command line arguments
     * @throws OTSGeometryException on error
     * @throws NetworkException on error
     * @throws NamingException on error
     * @throws ValueException on error
     * @throws SimRuntimeException on error
     * @throws ParameterException on error
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException, NamingException,
            ValueException, ParameterException, SimRuntimeException
    {
        SimLogger.setAllLogLevel(Level.WARNING);
        SimLogger.setLogCategories(LogCategory.ALL);

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
            serverSocket.setReuseAddress(true); // Ensure we can be restarted without the normal 2 minute delay
            System.out.println("Waiting for client to connect");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected; closing server socket");
            serverSocket.close(); // don't accept any other connections
            System.out.println("Socket time out is " + clientSocket.getSoTimeout());
            clientSocket.setSoTimeout(0);
            System.out.println("Constructing animation/simulation");
            AimsunControl aimsunControl = new AimsunControl();
            aimsunControl.commandLoop(clientSocket);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        System.exit(0);
    }

    /** Shared between sendGTUPositionsToAimsun and the commandLoop methods. */
    private Time simulateUntil = null;

    /**
     * Construct a GTU positions message, clear the simulateUntil value, transmit all GTU positions to Aimsun and wait for the
     * simulateUntil value to be set again.
     * @param outputStream
     */
    protected void sendGTUPositionsToAimsun(final OutputStream outputStream)
    {
        OTSSimulatorInterface simulator = this.model.getSimulator();
        System.out.println("Simulator has stopped at time " + simulator.getSimulatorTime());
        Time stopTime = simulator.getSimulatorTime();
        AimsunControlProtoBuf.GTUPositions.Builder builder = AimsunControlProtoBuf.GTUPositions.newBuilder();
        for (GTU gtu : this.model.getNetwork().getGTUs())
        {
            AimsunControlProtoBuf.GTUPositions.GTUPosition.Builder gpb =
                    AimsunControlProtoBuf.GTUPositions.GTUPosition.newBuilder();
            gpb.setGtuId(gtu.getId());
            DirectedPoint dp;
            try
            {
                dp = gtu.getOperationalPlan().getLocation(stopTime);
                gpb.setX(dp.x);
                gpb.setY(dp.y);
                gpb.setZ(dp.z);
                gpb.setAngle(dp.getRotZ());
                gpb.setLength(gtu.getLength().si);
                gpb.setWidth(gtu.getWidth().si);
                gpb.setGtuTypeId(Integer.parseInt(gtu.getGTUType().getId().split("\\.")[1]));
                gpb.setSpeed(gtu.getSpeed().si);
                builder.addGtuPos(gpb.build());
            }
            catch (OperationalPlanException exception)
            {
                exception.printStackTrace();
            }
        }
        builder.setStatus("OK");
        GTUPositions gtuPositions = builder.build();
        AimsunControlProtoBuf.OTSMessage.Builder resultBuilder = AimsunControlProtoBuf.OTSMessage.newBuilder();
        resultBuilder.setGtuPositions(gtuPositions);
        AimsunControlProtoBuf.OTSMessage result = resultBuilder.build();
        this.simulateUntil = null;
        try
        {
            transmitMessage(result, outputStream);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        System.out.println("Simulator waiting for new simulateUntil value");
        while (this.simulateUntil == null)
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException exception)
            {
                // exception.printStackTrace();
            }
        }
        try
        {
            simulator.scheduleEventAbs(this.simulateUntil, this, this, "sendGTUPositionsToAimsun",
                    new Object[] { outputStream });
        }
        catch (SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
        System.out.println("Simulator resuming");
    }

    /**
     * Process incoming commands.
     * @param socket Socket; the communications channel to Aimsun
     * @throws IOException when communication with Aimsun fails
     * @throws OTSGeometryException on error
     * @throws NetworkException on error
     * @throws NamingException on error
     * @throws ValueException on error
     * @throws SimRuntimeException on error
     * @throws ParameterException on error
     */
    private void commandLoop(final Socket socket) throws IOException, NetworkException, OTSGeometryException,
            NamingException, ValueException, ParameterException, SimRuntimeException
    {
        System.out.println("Entering command loop");
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        String error = null;
        boolean simulatorStarted = false;
        while (null == error)
        {
            try
            {
                byte[] sizeBytes = new byte[4];
                fillBuffer(inputStream, sizeBytes);
                int size =
                        ((sizeBytes[0] & 0xff) << 24) + ((sizeBytes[1] & 0xff) << 16) + ((sizeBytes[2] & 0xff) << 8)
                                + (sizeBytes[3] & 0xff);
                System.out.println("expecting message of " + size + " bytes");
                byte[] buffer = new byte[size];
                fillBuffer(inputStream, buffer);
                AimsunControlProtoBuf.OTSMessage message = AimsunControlProtoBuf.OTSMessage.parseFrom(buffer);

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
                        try (PrintWriter pw = new PrintWriter("d:/AimsunOtsNetwork.xml"))
                        {
                            pw.print(this.networkXML);
                        }
                        Duration runDuration = new Duration(createSimulation.getRunTime(), DurationUnit.SECOND);
                        System.out.println("runDuration " + runDuration);
                        Duration warmupDuration = new Duration(createSimulation.getWarmUpTime(), DurationUnit.SECOND);
                        try
                        {
                            OTSAnimator animator = new OTSAnimator();
                            this.model = new AimsunModel(animator, "", "");
                            animator.initialize(Time.ZERO, warmupDuration, runDuration, this.model);
                            OTSAnimationPanel animationPanel =
                                    new OTSAnimationPanel(this.model.getNetwork().getExtent(), new Dimension(800, 600),
                                            animator, this.model, OTSSwingApplication.DEFAULT_COLORER,
                                            this.model.getNetwork());
                            DefaultAnimationFactory.animateXmlNetwork(this.model.getNetwork(), animator,
                                    new DefaultSwitchableGTUColorer());
                            new AimsunSwingApplication(this.model, animationPanel);
                            animator.setSpeedFactor(Double.MAX_VALUE, true);
                            animator.setSpeedFactor(1000.0, true);
                        }
                        catch (SimRuntimeException | NamingException | OTSDrawingException exception1)
                        {
                            exception1.printStackTrace();
                            // Stop the simulation
                            error = "XML ERROR";
                        }
                        break;

                    case SIMULATEUNTIL:
                    {
                        System.out.println("Received SIMULATEUNTIL message");
                        AimsunControlProtoBuf.SimulateUntil simulateUntilThing = message.getSimulateUntil();
                        Time stopTime = new Time(simulateUntilThing.getTime(), TimeUnit.BASE_SECOND);
                        System.out.println("Simulate until " + stopTime + " ");
                        OTSSimulatorInterface simulator = this.model.getSimulator();
                        if (!simulatorStarted)
                        {
                            simulatorStarted = true;
                            simulator.scheduleEventAbs(stopTime, this, this, "sendGTUPositionsToAimsun",
                                    new Object[] { outputStream });
                            System.out.println("Starting simulator");
                            this.simulateUntil = stopTime;
                            simulator.start();
                        }
                        else if (!simulator.isRunning())
                        {
                            // Whoops: simulator has stopped
                            error = "HMM Simulator stopped";
                        }
                        else
                        {
                            System.out.println("Resuming simulator");
                            this.simulateUntil = stopTime;
                        }
                        break;
                    }

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
     * Transmit a message
     * @param message AimsunControlProtoBuf.OTSMessage; the message
     * @param outputStream OutputStream; the output stream
     * @throws IOException when transmission fails
     */
    private void transmitMessage(final AimsunControlProtoBuf.OTSMessage message, final OutputStream outputStream)
            throws IOException
    {
        int size = message.getSerializedSize();
        System.out.print("Transmitting " + message.getGtuPositions().getGtuPosCount() + " GTU positions and status \""
                + message.getGtuPositions().getStatus() + "\" encoded in " + size + " bytes ... ");
        byte[] sizeBytes = new byte[4];
        sizeBytes[0] = (byte) ((size >> 24) & 0xff);
        sizeBytes[1] = (byte) ((size >> 16) & 0xff);
        sizeBytes[2] = (byte) ((size >> 8) & 0xff);
        sizeBytes[3] = (byte) (size & 0xff);
        outputStream.write(sizeBytes);
        byte[] buffer = new byte[size];
        buffer = message.toByteArray();
        outputStream.write(buffer);
        System.out.println("Message sent");
    }

    /**
     * Fill a buffer from a stream; retry until the buffer is entirely filled.
     * @param in InputStream; the input stream for the data
     * @param buffer byte[]; the buffer
     * @throws IOException when it is not possible to fill the entire buffer
     */
    static void fillBuffer(final InputStream in, final byte[] buffer) throws IOException
    {
        System.out.print("Need to read " + buffer.length + " bytes ... ");
        int offset = 0;
        while (true)
        {
            int bytesRead = in.read(buffer, offset, buffer.length - offset);
            if (-1 == bytesRead)
            {
                break;
            }
            offset += bytesRead;
            if (buffer.length == offset)
            {
                System.out.println("got all " + buffer.length + " requested bytes");
                break;
            }
            if (buffer.length < offset)
            {
                System.out.println("Oops: Got more than " + buffer.length + " requested bytes");
                break;
            }
            System.out.print("now got " + offset + " bytes; need to read " + (buffer.length - offset) + " more bytes ... ");
        }
        if (offset != buffer.length)
        {
            throw new IOException("Got only " + offset + " of expected " + buffer.length + " bytes");
        }
    }

    /**
     * The application.
     */
    class AimsunSwingApplication extends OTSSimulationApplication<OTSModelInterface>
    {
        /** */
        private static final long serialVersionUID = 1L;

        /**
         * @param model OTSModelInterface; the model
         * @param panel OTSAnimationPanel; the panel of the main screen
         * @throws OTSDrawingException on animation error
         */
        public AimsunSwingApplication(final OTSModelInterface model, final OTSAnimationPanel panel)
                throws OTSDrawingException
        {
            super(model, panel);
        }
    }

    /**
     * The network.
     */
    class AimsunModel extends AbstractOTSModel implements EventListenerInterface
    {
        /**
         * @param simulator OTSSimulatorInterface; the simulator
         * @param shortName String; the model name
         * @param description String; the model description
         */
        public AimsunModel(OTSSimulatorInterface simulator, String shortName, String description)
        {
            super(simulator, shortName, description);
        }

        /** */
        private static final long serialVersionUID = 20170419L;

        /** The network. */
        private OTSNetwork network;

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                this.simulator.addListener(this, DEVSRealTimeClock.CHANGE_SPEED_FACTOR_EVENT);
                this.simulator.addListener(this, SimulatorInterface.TIME_CHANGED_EVENT);
            }
            catch (RemoteException exception1)
            {
                exception1.printStackTrace();
            }
            // URL url = URLResource.getResource("/aimsun/singleRoad.xml");
            XmlNetworkLaneParserOld nlp = new XmlNetworkLaneParserOld(this.simulator);
            @SuppressWarnings("synthetic-access")
            String xml = AimsunControl.this.networkXML;
            try
            {
                this.network = nlp.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), false);
                ConflictBuilder.buildConflicts(this.network, GTUType.VEHICLE, this.simulator,
                        new ConflictBuilder.FixedWidthGenerator(Length.createSI(2.0)));
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException
                    | GTUException | OTSGeometryException | ValueException | ParameterException | SimRuntimeException exception)
            {
                exception.printStackTrace();
                throw new SimRuntimeException(exception);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void notify(EventInterface event) throws RemoteException
        {
            System.out.println("Received event " + event);
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

    }

}

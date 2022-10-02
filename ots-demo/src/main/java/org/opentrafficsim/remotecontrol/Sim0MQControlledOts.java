package org.opentrafficsim.remotecontrol;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.NamingException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.cli.Checkable;
import org.djutils.cli.CliUtil;
import org.djutils.decoderdumper.HexDumper;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventTypeInterface;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.logger.CategoryLogger;
import org.djutils.logger.LogCategory;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGtuColorer;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OtsNetwork;
import org.opentrafficsim.core.object.InvisibleObjectInterface;
import org.opentrafficsim.draw.core.OtsDrawingException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.conflict.LaneCombinationList;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.opentrafficsim.swing.gui.OTSSwingApplication;
import org.opentrafficsim.trafficcontrol.TrafficControlException;
import org.opentrafficsim.trafficcontrol.TrafficController;
import org.opentrafficsim.trafficcontrol.trafcod.TrafCOD;
import org.pmw.tinylog.Level;
import org.sim0mq.Sim0MQException;
import org.sim0mq.message.Sim0MQMessage;
import org.xml.sax.SAXException;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeAnimator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.dsol.swing.gui.TabbedContentPane;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Sim0MQ controlled OTS
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class Sim0MQControlledOts implements EventListenerInterface
{
    /** ... */
    private static final long serialVersionUID = 20200317L;

    /** Currently active model. */
    private Sim0MQOTSModel model = null;

    /** The ZContext of all the sockets. */
    private final ZContext zContext;

    /** The port number of the listening socket. */
    private final int port;

    /** Communication channel to the master. */
    private final MasterCommunication masterCommunication = new MasterCommunication();

    /**
     * Construct a new Sim0MQ controlled OTS.
     * @param zContext ZContext; the context of ZMQ
     * @param port int; the port number of the listening socket
     */
    public Sim0MQControlledOts(final ZContext zContext, final int port)
    {
        this.zContext = zContext;
        this.port = port;
        this.masterCommunication.start();
    }

    /**
     * Thread that handles ALL reads and writes on the socket to the master.
     */
    class MasterCommunication extends Thread
    {
        @Override
        public void run()
        {
            System.err.println("MasterCommunication thread id is " + Thread.currentThread().getId());
            ZMQ.Socket remoteControllerSocket = Sim0MQControlledOts.this.zContext.createSocket(SocketType.PAIR);
            remoteControllerSocket.setHWM(100000);
            remoteControllerSocket.bind("tcp://*:" + Sim0MQControlledOts.this.port);
            ZMQ.Socket resultQueue = Sim0MQControlledOts.this.zContext.createSocket(SocketType.PULL);
            resultQueue.bind("inproc://results");
            ZMQ.Socket toCommandLoop = Sim0MQControlledOts.this.zContext.createSocket(SocketType.PUSH);
            toCommandLoop.setHWM(1000);
            toCommandLoop.connect("inproc://commands");
            /*-
            while (!Thread.interrupted())
            {
                byte[] data;
                data = remoteControllerSocket.recv(ZMQ.DONTWAIT);
                if (null != data)
                {
                    System.err.println("Got incoming command");
                    toCommandLoop.send(data, 0);
                    System.err.println("Incoming command handed over to toCommandLoop socket");
                    continue;
                }
                data = resultQueue.recv(ZMQ.DONTWAIT);
                if (null != data)
                {
                    System.err.println("Got outgoing result");
                    remoteControllerSocket.send(data, 0);
                    System.err.println("Outgoing result handed over to remoteControllerSocket");
                }
                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException e)
                {
                    //e.printStackTrace();
                }
            }
            */
            /// *-
            ZMQ.Poller poller = Sim0MQControlledOts.this.zContext.createPoller(2);
            poller.register(remoteControllerSocket, ZMQ.Poller.POLLIN);
            poller.register(resultQueue, ZMQ.Poller.POLLIN);
            while (!Thread.currentThread().isInterrupted())
            {
                poller.poll();
                if (poller.pollin(0))
                {
                    System.err.println("Got incoming command");
                    byte[] data = remoteControllerSocket.recv();
                    toCommandLoop.send(data, 0);
                    System.err.println("Incoming command handed over to toCommandLoop socket");
                }
                else if (poller.pollin(1))
                {
                    System.err.println("Got outgoing result");
                    byte[] data = resultQueue.recv();
                    remoteControllerSocket.send(data, 0);
                    System.err.println("Outgoing result handed over to remoteControllerSocket");
                }
            }
            // */
        }
    }

    /**
     * The command line options.
     */
    @Command(description = "Sim0MQ Remotely Controlled OTS", name = "Sim0MQOTS", mixinStandardHelpOptions = true,
            version = "1.0")
    public static class Options implements Checkable
    {
        /** The IP port. */
        @Option(names = {"-p", "--port"}, description = "Internet port to use", defaultValue = "8888")
        private int port;

        /**
         * Retrieve the port.
         * @return int; the port
         */
        public final int getPort()
        {
            return this.port;
        }

        @Override
        public final void check() throws Exception
        {
            if (this.port <= 0 || this.port > 65535)
            {
                throw new Exception("Port should be between 1 and 65535");
            }
        }
    }

    /**
     * Program entry point.
     * @param args String[]; the command line arguments
     * @throws OtsGeometryException on error
     * @throws NetworkException on error
     * @throws NamingException on error
     * @throws ValueRuntimeException on error
     * @throws SimRuntimeException on error
     * @throws ParameterException on error
     * @throws SerializationException on error
     * @throws Sim0MQException on error
     * @throws IOException on error
     */
    public static void main(final String[] args) throws NetworkException, OtsGeometryException, NamingException,
            ValueRuntimeException, ParameterException, SimRuntimeException, Sim0MQException, SerializationException, IOException
    {
        CategoryLogger.setAllLogLevel(Level.WARNING);
        CategoryLogger.setLogCategories(LogCategory.ALL);
        Options options = new Options();
        CliUtil.execute(options, args); // register Unit converters, parse the command line, etc..
        int port = options.getPort();
        System.out.println("Creating OTS server listening on port " + port);
        ZContext context = new ZContext(10);
        Sim0MQControlledOts slave = new Sim0MQControlledOts(context, port);

        slave.commandLoop();
        // Currently, there is no shutdown command; so the following code is never executed
        context.destroy();
        context.close();
    }

    /**
     * Construct an OTS simulation experiment from an XML description.
     * @param xml String; the XML encoded network
     * @param simulationDuration Duration; total duration of the simulation
     * @param warmupTime Duration; warm up time of the simulation
     * @param seed Long; seed for the experiment
     * @return String; null on success, description of the problem on error
     */
    private String loadNetwork(final String xml, final Duration simulationDuration, final Duration warmupTime, final Long seed)
    {
        if (null != this.model)
        {
            return "Cannot create another network (yet)";
        }
        else
        {
            try
            {
                OtsAnimator animator = new OtsAnimator("OTS Animator");
                this.model = new Sim0MQOTSModel(animator, "OTS model", "Remotely controlled OTS model", xml);
                Map<String, StreamInterface> map = new LinkedHashMap<>();
                map.put("generation", new MersenneTwister(seed));
                animator.initialize(Time.ZERO, simulationDuration, warmupTime, this.model, map);
                this.model.getNetwork().addListener(this, Network.GTU_ADD_EVENT);
                this.model.getNetwork().addListener(this, Network.GTU_REMOVE_EVENT);
                OTSAnimationPanel animationPanel =
                        new OTSAnimationPanel(this.model.getNetwork().getExtent(), new Dimension(1100, 1000), animator,
                                this.model, OTSSwingApplication.DEFAULT_COLORER, this.model.getNetwork());
                DefaultAnimationFactory.animateXmlNetwork(this.model.getNetwork(), new DefaultSwitchableGtuColorer());
                new Sim0MQRemoteControlSwingApplication(this.model, animationPanel);
                JFrame frame = (JFrame) animationPanel.getParent().getParent().getParent();
                frame.setExtendedState(Frame.NORMAL);
                frame.setSize(new Dimension(1100, 1000));
                frame.setBounds(0, 25, 1100, 1000);
                animator.setSpeedFactor(Double.MAX_VALUE, true);
                animator.setSpeedFactor(1000.0, true);

                ImmutableMap<String, InvisibleObjectInterface> invisibleObjectMap =
                        this.model.getNetwork().getInvisibleObjectMap();
                animator.addListener(this, DEVSRealTimeAnimator.CHANGE_SPEED_FACTOR_EVENT);
                animator.addListener(this, SimulatorInterface.TIME_CHANGED_EVENT);
                for (InvisibleObjectInterface ioi : invisibleObjectMap.values())
                {
                    if (ioi instanceof TrafCOD)
                    {
                        TrafCOD trafCOD = (TrafCOD) ioi;
                        Container controllerDisplayPanel = trafCOD.getDisplayContainer();
                        if (null != controllerDisplayPanel)
                        {
                            JPanel wrapper = new JPanel(new BorderLayout());
                            wrapper.add(new JScrollPane(controllerDisplayPanel));
                            TabbedContentPane tabbedPane = animationPanel.getTabbedPane();
                            tabbedPane.addTab(tabbedPane.getTabCount() - 1, trafCOD.getId(), wrapper);
                        }
                        // trafCOD.addListener(this,
                        // TrafficController.TRAFFICCONTROL_CONTROLLER_EVALUATING);
                        trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONTROLLER_WARNING);
                        trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONFLICT_GROUP_CHANGED);
                        trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_STATE_CHANGED);
                        trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_VARIABLE_CREATED);
                        trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_TRACED_VARIABLE_UPDATED);
                    }
                }
                try
                {
                    Thread.sleep(300);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                animationPanel.actionPerformed(new ActionEvent(this, 0, "ZoomAll"));
            }
            catch (Exception e)
            {
                return e.getMessage();
            }
        }
        return null;
    }

    /** Count transmitted messages. */
    private AtomicInteger packetsSent = new AtomicInteger(0);

    /**
     * Read commands from the master, execute them and report the results.
     */
    @SuppressWarnings("checkstyle:methodlength")
    public void commandLoop()
    {
        System.err.println("CommandLoop thread id is " + Thread.currentThread().getId());
        ZMQ.Socket incomingCommands = this.zContext.createSocket(SocketType.PULL);
        incomingCommands.bind("inproc://commands");
        while (!Thread.currentThread().isInterrupted())
        {
            // Read the request from the client
            System.err.println("CommandLoop ready to read a command");
            byte[] request = incomingCommands.recv(0);
            System.err.println("CommandLoop processing a command of " + request.length + " bytes");
            Object[] message;
            String result = "At your command";
            try
            {
                message = Sim0MQMessage.decode(request).createObjectArray();
                System.out.println("Received Sim0MQ message:");

                if (message.length >= 8 && message[5] instanceof String)
                {
                    String command = (String) message[5];
                    System.out.println("Command is " + command);
                    switch (command)
                    {
                        case "LOADNETWORK":
                            if (message.length == 12 && message[8] instanceof String && message[9] instanceof Duration
                                    && message[10] instanceof Duration && message[11] instanceof Long)
                            {
                                System.out.println("xml length = " + ((String) message[8]).length());
                                String loadResult = loadNetwork((String) message[8], (Duration) message[9],
                                        (Duration) message[10], (Long) message[11]);
                                if (null != loadResult)
                                {
                                    result = loadResult;
                                }
                            }
                            else
                            {
                                result = "no network, warmupTime and/or runTime provided with LOADNETWORK command";
                            }
                            break;

                        case "SIMULATEUNTIL": // XXX: the SimulateUntil is blocking this loop. Do we want that?
                            if (null == this.model)
                            {
                                result = "No model loaded";
                            }
                            else if (message.length == 9 && message[8] instanceof Time)
                            {
                                OtsSimulatorInterface simulator = this.model.getSimulator();
                                System.out.println("Simulating up to " + message[8]);
                                simulator.runUpTo((Time) message[8]);
                                int count = 0;
                                while (simulator.isStartingOrRunning())
                                {
                                    System.out.print(".");
                                    count++;
                                    if (count > 1000) // 10 seconds
                                    {
                                        System.out.println("SIMULATOR DOES NOT STOP. TIME = " + simulator.getSimulatorTime());
                                        Iterator<SimEventInterface<Duration>> elIt = simulator.getEventList().iterator();
                                        while (elIt.hasNext())
                                        {
                                            System.out.println("EVENTLIST: " + elIt.next());
                                        }
                                        simulator.stop();
                                    }
                                    try
                                    {
                                        Thread.sleep(10);
                                    }
                                    catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                                System.out.println("Simulator has stopped at time " + simulator.getSimulatorTime());
                                try
                                {
                                    Thread.sleep(100); // EXTRA STOP FOR SYNC REASONS - BUG IN DSOL!
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            else
                            {
                                result = "Bad or missing stop time";
                            }
                            break;

                        case "SENDALLGTUPOSITIONS":
                            if (null == this.model)
                            {
                                result = "No model loaded";
                            }
                            else if (message.length == 8)
                            {
                                for (Gtu gtu : this.model.network.getGTUs())
                                {
                                    // Send information about one GTU to master
                                    try
                                    {
                                        DirectedPoint gtuPosition = gtu.getLocation();
                                        Object[] gtuData = new Object[] {gtu.getId(), gtu.getGtuType().getId(), gtuPosition.x,
                                                gtuPosition.y, gtuPosition.z, gtuPosition.getRotZ(), gtu.getSpeed(),
                                                gtu.getAcceleration()};
                                        sendToMaster(Sim0MQMessage.encodeUTF8(true, 0, "slave_XXXXX", "master", "GTUPOSITION",
                                                0, gtuData));
                                    }
                                    catch (Sim0MQException | SerializationException e)
                                    {
                                        e.printStackTrace();
                                        break; // this is fatal
                                    }

                                }
                            }
                            break;

                        default:

                            System.out.println("Don't know how to handle message:");
                            System.out.println(Sim0MQMessage.print(message));
                            result = "Unimplemented command " + command;
                            break;
                    }
                }
                else
                {
                    System.out.println("Don't know how to handle message:");
                    System.out.println(HexDumper.hexDumper(request));
                    result = "Ignored message";
                }
            }
            catch (Sim0MQException | SerializationException e)
            {
                e.printStackTrace();
                result = "Could not decode command: " + e.getMessage();
            }
            // Send reply to master
            try
            {
                sendToMaster(Sim0MQMessage.encodeUTF8(true, 0, "slave_XXXXX", "master", "READY", 0, result));
            }
            catch (Sim0MQException | SerializationException e)
            {
                e.printStackTrace();
                break; // this is fatal
            }
        }
    }

    /** In memory sockets to talk to the multiplexer. */
    private Map<Long, ZMQ.Socket> socketMap = new LinkedHashMap<>();

    /**
     * Safe - synchronized - portal to send a message to the remote controller.
     * @param data byte[]; the data to send
     */
    public synchronized void sendToMaster(final byte[] data)
    {
        byte[] fixedData = data;
        int number = -1;
        try
        {
            // Patch the sender field to include the packet counter value.
            Object[] messageFields = Sim0MQMessage.decode(data).createObjectArray();
            Object[] newMessageFields = Arrays.copyOfRange(messageFields, 8, messageFields.length);
            number = this.packetsSent.addAndGet(1);
            fixedData = Sim0MQMessage.encodeUTF8(true, messageFields[2], String.format("slave_%05d", number), messageFields[4],
                    messageFields[5], messageFields[6], newMessageFields);
            System.err.println("Prepared message " + number + ", type is " + messageFields[5]);
        }
        catch (Sim0MQException | SerializationException e)
        {
            e.printStackTrace();
        }
        Long threadId = Thread.currentThread().getId();
        ZMQ.Socket socket = this.socketMap.get(threadId);
        while (null == socket)
        {
            System.out.println("Creating new internal socket for thread " + threadId);
            try
            {
                socket = this.zContext.createSocket(SocketType.PUSH);
                socket.setHWM(100000);
                socket.connect("inproc://results");
                this.socketMap.put(threadId, socket);
                // System.out.println("Socket created");
            }
            catch (Exception cbie)
            {
                System.err.println("Caught funny exception - probably related to DSOL animator start/stop code ... retrying");
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    System.err.println("Sleep interrupted!");
                }
            }
        }
        System.out.println("pre send");

        // ZMQ.Socket socket = this.zContext.createSocket(SocketType.PUSH);
        // socket.setHWM(100000);
        // socket.connect("inproc://results");
        socket.send(fixedData, 0);
        // socket.close();
        // System.out.println("post send");
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        try
        {
            EventTypeInterface type = event.getType();
            String eventTypeName = type.getName();
            System.out.println("notify: start processing event " + eventTypeName);
            switch (eventTypeName)
            {
                case "TRAFFICCONTROL.CONTROLLER_EVALUATING":
                {
                    Object[] payload = (Object[]) event.getContent();
                    CategoryLogger.always().info("{}: Evaluating at time {}", payload[0], payload[1]);
                    sendToMaster(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0,
                            String.format("%s: Evaluating at time %s", payload[0], payload[1])));
                    break;
                }

                case "TRAFFICCONTROL.CONFLICT_GROUP_CHANGED":
                {
                    Object[] payload = (Object[]) event.getContent();
                    CategoryLogger.always().info("{}: Conflict group changed from {} to {}", payload[0], payload[1],
                            payload[2]);
                    sendToMaster(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0, payload));
                    break;
                }

                case "TRAFFICCONTROL.VARIABLE_UPDATED":
                {
                    Object[] payload = (Object[]) event.getContent();
                    CategoryLogger.always().info("{}: Variable changed {} <- {}   {}", payload[0], payload[1], payload[4],
                            payload[5]);
                    sendToMaster(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0, payload));
                    break;
                }

                case "TRAFFICCONTROL.CONTROLLER_WARNING":
                {
                    Object[] payload = (Object[]) event.getContent();
                    CategoryLogger.always().info("{}: Warning {}", payload[0], payload[1]);
                    sendToMaster(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0, payload));
                    break;
                }

                case "TIME_CHANGED_EVENT":
                {
                    CategoryLogger.always().info("Time changed to {}", event.getContent());
                    sendToMaster(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0,
                            String.format("Time changed to %s", event.getContent())));
                    break;
                }

                case "NETWORK.GTU.ADD":
                {
                    sendToMaster(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0, event.getContent()));
                    break;
                }

                case "NETWORK.GTU.REMOVE":
                {
                    sendToMaster(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0, event.getContent()));
                    break;
                }

                default:
                {
                    CategoryLogger.always().info("Event of unhandled type {} with payload {}", event.getType(),
                            event.getContent());
                    sendToMaster(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", "Event of unhandled type", 0, String
                            .format("%s: Event of unhandled type %s with payload {}", event.getType(), event.getContent())));
                    break;
                }
            }
            System.out.println("notify: finished processing event " + eventTypeName);
        }
        catch (Sim0MQException | SerializationException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * The application.
     */
    class Sim0MQRemoteControlSwingApplication extends OTSSimulationApplication<OtsModelInterface>
    {
        /** */
        private static final long serialVersionUID = 1L;

        /**
         * @param model OTSModelInterface; the model
         * @param panel OTSAnimationPanel; the panel of the main screen
         * @throws OtsDrawingException on animation error
         */
        Sim0MQRemoteControlSwingApplication(final OtsModelInterface model, final OTSAnimationPanel panel)
                throws OtsDrawingException
        {
            super(model, panel);
        }
    }

    /**
     * The Model.
     */
    class Sim0MQOTSModel extends AbstractOtsModel implements EventListenerInterface
    {
        /** */
        private static final long serialVersionUID = 20170419L;

        /** The network. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        OtsRoadNetwork network;

        /** The XML. */
        private final String xml;

        /**
         * @param simulator OTSSimulatorInterface; the simulator
         * @param shortName String; the model name
         * @param description String; the model description
         * @param xml String; the XML description of the simulation model
         */
        Sim0MQOTSModel(final OtsSimulatorInterface simulator, final String shortName, final String description,
                final String xml)
        {
            super(simulator, shortName, description);
            this.xml = xml;
        }

        /** {@inheritDoc} */
        @Override
        public void notify(final EventInterface event) throws RemoteException
        {
            System.err.println("Received event " + event);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            this.network = new OtsRoadNetwork(getShortName(), true, getSimulator());
            try
            {
                XmlNetworkLaneParser.build(new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8)), this.network,
                        false);
                LaneCombinationList ignoreList = new LaneCombinationList();
                LaneCombinationList permittedList = new LaneCombinationList();
                ConflictBuilder.buildConflictsParallel(this.network, this.network.getGtuType(GtuType.DEFAULTS.VEHICLE),
                        getSimulator(), new ConflictBuilder.FixedWidthGenerator(Length.instantiateSI(2.0)), ignoreList,
                        permittedList);
            }
            catch (NetworkException | OtsGeometryException | JAXBException | URISyntaxException | XmlParserException
                    | SAXException | ParserConfigurationException | GtuException | IOException
                    | TrafficControlException exception)
            {
                exception.printStackTrace();
                // Abusing the SimRuntimeException to propagate the message to the main method (the problem could actually be a
                // parsing problem)
                throw new SimRuntimeException(exception);
            }
        }

        /** {@inheritDoc} */
        @Override
        public OtsNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "Sim0MQOTSModel";
        }

    }

}

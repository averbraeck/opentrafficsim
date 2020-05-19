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
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.object.InvisibleObjectInterface;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
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
import org.zeromq.ZMonitor;
import org.zeromq.ZMonitor.ZEvent;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeClock;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.dsol.swing.gui.TabbedContentPane;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Sim0MQ controlled OTS
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 18, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Sim0MQControlledOTSNew implements EventListenerInterface
{
    /** ... */
    private static final long serialVersionUID = 20200317L;

    /** Currently active model. */
    private Sim0MQOTSModel model = null;

    /** The ZContext of all the sockets. */
    private final ZContext zContext;

    /** The port number of the listening socket. */
    private final int port;

    /**
     * Construct a new Sim0MQ controlled OTS.
     * @param zContext ZContext; the ZMQ context of all the sockets.
     * @param port int; the port number of the listening socket
     */
    public Sim0MQControlledOTSNew(final ZContext zContext, final int port)
    {
        this.zContext = zContext;
        this.port = port;
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
     * @throws OTSGeometryException on error
     * @throws NetworkException on error
     * @throws NamingException on error
     * @throws ValueRuntimeException on error
     * @throws SimRuntimeException on error
     * @throws ParameterException on error
     * @throws SerializationException on error
     * @throws Sim0MQException on error
     * @throws IOException on error
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException, NamingException,
            ValueRuntimeException, ParameterException, SimRuntimeException, Sim0MQException, SerializationException, IOException
    {
        CategoryLogger.setAllLogLevel(Level.WARNING);
        CategoryLogger.setLogCategories(LogCategory.ALL);
        Options options = new Options();
        CliUtil.execute(options, args); // register Unit converters, parse the command line, etc..
        int port = options.getPort();
        System.out.println("Creating OTS server listening on port " + port);

        ZContext context = new ZContext(1);
        Sim0MQControlledOTSNew slave = new Sim0MQControlledOTSNew(context, port);

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
                OTSAnimator animator = new OTSAnimator("OTS Animator");
                this.model = new Sim0MQOTSModel(animator, "OTS model", "Remotely controlled OTS model", xml);
                Map<String, StreamInterface> map = new LinkedHashMap<>();
                map.put("generation", new MersenneTwister(seed));
                animator.initialize(Time.ZERO, simulationDuration, warmupTime, this.model, map);
                this.model.getNetwork().addListener(this, Network.GTU_ADD_EVENT);
                this.model.getNetwork().addListener(this, Network.GTU_REMOVE_EVENT);
                OTSAnimationPanel animationPanel =
                        new OTSAnimationPanel(this.model.getNetwork().getExtent(), new Dimension(1100, 1000), animator,
                                this.model, OTSSwingApplication.DEFAULT_COLORER, this.model.getNetwork());
                DefaultAnimationFactory.animateXmlNetwork(this.model.getNetwork(), new DefaultSwitchableGTUColorer());
                new Sim0MQRemoteControlSwingApplication(this.model, animationPanel);
                JFrame frame = (JFrame) animationPanel.getParent().getParent().getParent();
                frame.setExtendedState(Frame.NORMAL);
                frame.setSize(new Dimension(1100, 1000));
                frame.setBounds(0, 25, 1100, 1000);
                animator.setSpeedFactor(Double.MAX_VALUE, true);
                animator.setSpeedFactor(1000.0, true);

                ImmutableMap<String, InvisibleObjectInterface> invisibleObjectMap =
                        this.model.getNetwork().getInvisibleObjectMap();
                animator.addListener(this, DEVSRealTimeClock.CHANGE_SPEED_FACTOR_EVENT);
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
        // Socket to talk to clients. XXX: PAIR socket
        ZMQ.Socket remoteControllerSocket = this.zContext.createSocket(SocketType.PAIR);
        remoteControllerSocket.setHWM(100000);
        // XXX: temporary without Monitor
        // ZMonitor zm = new ZMonitor(this.zContext, remoteControllerSocket);
        // zm.add(ZMonitor.Event.ALL);
        // zm.verbose(true);
        // zm.start();
        // new Monitor(zm).start();
        remoteControllerSocket.bind("tcp://*:" + this.port);
        ZMQ.Socket logMessages = this.zContext.createSocket(SocketType.PULL);
        logMessages.setHWM(100000);
        logMessages.bind("inproc://toMaster");

        // XXX: No POLLER
        // ZMQ.Poller items = this.zContext.createPoller(2);
        // items.register(logMessages, ZMQ.Poller.POLLIN);
        // items.register(remoteControllerSocket, ZMQ.Poller.POLLIN);
        while (!Thread.currentThread().isInterrupted())
        {
            // items.poll();
            // if (items.pollin(0))
            byte[] logMessage = logMessages.recv(ZMQ.DONTWAIT);
            if (logMessage != null)
            {
                try
                {
                    // Patch the sender field to include the packet counter value.
                    Object[] messageFields = Sim0MQMessage.decode(logMessage).createObjectArray();
                    Object[] newMessageFields = Arrays.copyOfRange(messageFields, 8, messageFields.length);
                    logMessage = Sim0MQMessage.encodeUTF8(true, messageFields[2],
                            String.format("slave_%05d", this.packetsSent.addAndGet(1)), messageFields[4], messageFields[5],
                            messageFields[6], newMessageFields);
                }
                catch (Sim0MQException | SerializationException e)
                {
                    e.printStackTrace();
                }
                remoteControllerSocket.send(logMessage);
            }
            else // The "else" ensures that all log messages are handled before a new command is handled
            {
                // Read the request from the client
                byte[] request = remoteControllerSocket.recv(ZMQ.DONTWAIT);
                if (request == null)
                {
                    try
                    {
                        Thread.sleep(1); // 1000 Hz
                        continue;
                    }
                    catch (InterruptedException e)
                    {
                        // ignore;
                    }
                }
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
                                    OTSSimulatorInterface simulator = this.model.getSimulator();
                                    System.out.println("Simulating up to " + message[8]);
                                    simulator.runUpTo((Time) message[8]);
                                    int count = 0;
                                    while (simulator.isStartingOrRunning())
                                    {
                                        System.out.print(".");
                                        count++;
                                        if (count > 1000) // 10 seconds
                                        {
                                            System.out
                                                    .println("SIMULATOR DOES NOT STOP. TIME = " + simulator.getSimulatorTime());
                                            Iterator<SimEventInterface<SimTimeDoubleUnit>> elIt =
                                                    simulator.getEventList().iterator();
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
                                    // try
                                    // {
                                    // Thread.sleep(100); // EXTRA STOP FOR SYNC REASONS - BUG IN DSOL - SOLVED IN DSOL 3.04.06
                                    // }
                                    // catch (InterruptedException e)
                                    // {
                                    // e.printStackTrace();
                                    // }
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
                                    for (GTU gtu : this.model.network.getGTUs())
                                    {
                                        // Send information about one GTU to master
                                        try
                                        {
                                            DirectedPoint gtuPosition = gtu.getLocation();
                                            Object[] gtuData = new Object[] {gtu.getId(), gtu.getGTUType().getId(),
                                                    gtuPosition.x, gtuPosition.y, gtuPosition.z, gtuPosition.getRotZ(),
                                                    gtu.getSpeed(), gtu.getAcceleration()};
                                            remoteControllerSocket.send(Sim0MQMessage.encodeUTF8(true, 0,
                                                    String.format("slave_%05d", this.packetsSent.addAndGet(1)), "master",
                                                    "GTUPOSITION", 0, gtuData), 0);
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
                catch (RemoteException e)
                {
                    e.printStackTrace();
                    result = "Caught RemoteException: " + e.getMessage();
                }
                // Send reply to master
                try
                {
                    remoteControllerSocket.send(Sim0MQMessage.encodeUTF8(true, 0,
                            String.format("slave_%05d", this.packetsSent.addAndGet(1)), "master", "READY", 0, result), 0);
                }
                catch (Sim0MQException | SerializationException e)
                {
                    e.printStackTrace();
                    break; // this is fatal
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        /**
         * Not sure if this method is always called from the same thread.
         */
        ZMQ.Socket toMaster = this.zContext.createSocket(SocketType.PUSH);
        toMaster.setHWM(100000);
        // XXX toMaster.setSendTimeOut(-1); // Blocking send mode
        toMaster.connect("inproc://toMaster");
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
                    toMaster.send(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0,
                            String.format("%s: Evaluating at time %s", payload[0], payload[1])), 0);
                    break;
                }

                case "TRAFFICCONTROL.CONFLICT_GROUP_CHANGED":
                {
                    Object[] payload = (Object[]) event.getContent();
                    CategoryLogger.always().info("{}: Conflict group changed from {} to {}", payload[0], payload[1],
                            payload[2]);
                    toMaster.send(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0, payload), 0);
                    break;
                }

                case "TRAFFICCONTROL.VARIABLE_UPDATED":
                {
                    Object[] payload = (Object[]) event.getContent();
                    CategoryLogger.always().info("{}: Variable changed {} <- {}   {}", payload[0], payload[1], payload[4],
                            payload[5]);
                    toMaster.send(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0, payload), 0);
                    break;
                }

                case "TRAFFICCONTROL.CONTROLLER_WARNING":
                {
                    Object[] payload = (Object[]) event.getContent();
                    CategoryLogger.always().info("{}: Warning {}", payload[0], payload[1]);
                    toMaster.send(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0, payload), 0);
                    break;
                }

                case "TIME_CHANGED_EVENT":
                {
                    CategoryLogger.always().info("Time changed to {}", event.getContent());
                    toMaster.send(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0,
                            String.format("Time changed to %s", event.getContent())), 0);
                    break;
                }

                case "NETWORK.GTU.ADD":
                {
                    toMaster.send(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0, event.getContent()),
                            0);
                    break;
                }

                case "NETWORK.GTU.REMOVE":
                {
                    toMaster.send(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", eventTypeName, 0, event.getContent()),
                            0);
                    break;
                }

                default:
                {
                    CategoryLogger.always().info("Event of unhandled type {} with payload {}", event.getType(),
                            event.getContent());
                    toMaster.send(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", "Event of unhandled type", 0, String
                            .format("%s: Event of unhandled type %s with payload {}", event.getType(), event.getContent())), 0);
                    break;
                }
            }
            System.out.println("notify: finished processing event " + eventTypeName);
        }
        catch (Sim0MQException | SerializationException e)
        {
            e.printStackTrace();
        }
        toMaster.close();
    }

    /** Monitor thread. */
    static class Monitor extends Thread
    {
        /** The ZMonitor that does the work. */
        private final ZMonitor monitor;

        /**
         * Construct a new Monitor.
         * @param monitor ZMonitor; the ZMonitor that collects events from the ZMQ socket that must be monitored
         */
        Monitor(final ZMonitor monitor)
        {
            this.monitor = monitor;
        }

        @Override
        public void run()
        {
            while (true)
            {
                ZEvent event = this.monitor.nextEvent();
                System.out.println("event is " + event);
                switch (event.type)
                {
                    case ACCEPTED:
                        System.out.println("Accepted");
                        break;
                    case ACCEPT_FAILED:
                        break;
                    case ALL:
                        break;
                    case BIND_FAILED:
                        break;
                    case CLOSED:
                        break;
                    case CLOSE_FAILED:
                        break;
                    case CONNECTED:
                        break;
                    case CONNECT_DELAYED:
                        break;
                    case CONNECT_RETRIED:
                        break;
                    case DISCONNECTED:
                        System.out.println("Disconnected");
                        break;
                    case HANDSHAKE_PROTOCOL:
                        break;
                    case LISTENING:
                        break;
                    case MONITOR_STOPPED:
                        break;
                    default:
                        System.out.println("Unknown event");
                        break;

                }
            }
        }

    }

    /**
     * The application.
     */
    class Sim0MQRemoteControlSwingApplication extends OTSSimulationApplication<OTSModelInterface>
    {
        /** */
        private static final long serialVersionUID = 1L;

        /**
         * @param model OTSModelInterface; the model
         * @param panel OTSAnimationPanel; the panel of the main screen
         * @throws OTSDrawingException on animation error
         */
        Sim0MQRemoteControlSwingApplication(final OTSModelInterface model, final OTSAnimationPanel panel)
                throws OTSDrawingException
        {
            super(model, panel);
        }
    }

    /**
     * The Model.
     */
    class Sim0MQOTSModel extends AbstractOTSModel implements EventListenerInterface
    {
        /** */
        private static final long serialVersionUID = 20170419L;

        /** The network. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        OTSRoadNetwork network;

        /** The XML. */
        private final String xml;

        /**
         * @param simulator OTSSimulatorInterface; the simulator
         * @param shortName String; the model name
         * @param description String; the model description
         * @param xml String; the XML description of the simulation model
         */
        Sim0MQOTSModel(final OTSSimulatorInterface simulator, final String shortName, final String description,
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
            this.network = new OTSRoadNetwork(getShortName(), true, getSimulator());
            try
            {
                XmlNetworkLaneParser.build(new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8)), this.network,
                        false);
                LaneCombinationList ignoreList = new LaneCombinationList();
                LaneCombinationList permittedList = new LaneCombinationList();
                ConflictBuilder.buildConflictsParallel(this.network, this.network.getGtuType(GTUType.DEFAULTS.VEHICLE),
                        getSimulator(), new ConflictBuilder.FixedWidthGenerator(Length.instantiateSI(2.0)), ignoreList,
                        permittedList);
            }
            catch (NetworkException | OTSGeometryException | JAXBException | URISyntaxException | XmlParserException
                    | SAXException | ParserConfigurationException | GTUException | IOException
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
        public OTSNetwork getNetwork()
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

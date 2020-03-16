package org.opentrafficsim.remotecontrol;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.swing.JFrame;
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
import org.djutils.logger.CategoryLogger;
import org.djutils.logger.LogCategory;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSLoggingAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
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
import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeClock;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.DSOLException;
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
public class Sim0MQControlledOTS
{
    /** Currently active model. */
    private Sim0MQOTSModel model = null;

    /**
     * The command line options.
     */
    @Command(description = "Sim0MQ Remotely Controlled OTS", name = "Sim0MQOTS", mixinStandardHelpOptions = true,
            version = "1.0")
    public static class Options implements Checkable
    {
        /** The IP port. */
        @Option(names = { "-p", "--port" }, description = "Internet port to use", defaultValue = "8888")
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
     * @throws SerializationException
     * @throws Sim0MQException
     * @throws IOException
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException, NamingException,
            ValueRuntimeException, ParameterException, SimRuntimeException, Sim0MQException, SerializationException, IOException
    {
        CategoryLogger.setAllLogLevel(Level.WARNING);
        CategoryLogger.setLogCategories(LogCategory.ALL);
        Options options = new Options();
        CliUtil.execute(options, args); // register Unit converters, parse the command line, etc..
        int port = options.getPort();
        System.out.println("Creating server socket for port " + port);
        ZContext context = new ZContext(1);

        // Socket to talk to clients
        ZMQ.Socket responder = context.createSocket(SocketType.DEALER);
        ZMonitor zm = new ZMonitor(context, responder);
        zm.add(ZMonitor.Event.ALL);
        zm.verbose(true);
        zm.start();
        new Monitor(zm).start();

        responder.bind("tcp://*:" + port);
        Sim0MQControlledOTS slave = new Sim0MQControlledOTS();
        slave.commandLoop(responder);
        responder.close();
        zm.close();
        context.destroy();
        context.close();
    }

    /**
     * Read commands from the master, execute them and report the results.
     * @param master ZMQ.Socket; the communication path to the master
     */
    public void commandLoop(final ZMQ.Socket master)
    {
        while (!Thread.currentThread().isInterrupted())
        {
            // Wait for next request from the client
            byte[] request = master.recv(0);
            System.out.println("Received message:");
            System.out.println(HexDumper.hexDumper(request));
            Object[] message;
            String result = "At your command";
            try
            {
                message = Sim0MQMessage.decode(request).createObjectArray();
                System.out.println("Received Sim0MQ message:");
                System.out.println(Sim0MQMessage.print(message));

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
                                if (null != this.model)
                                {
                                    result = "Cannot create another network (yet)";
                                }
                                else
                                {
                                    OTSAnimator animator =
                                            new OTSLoggingAnimator("C:/Temp/AimsunEventlog.txt", "AimsunControl");
                                    this.model = new Sim0MQOTSModel(animator, "OTS model", "OTS model", (String) message[8]);
                                    Map<String, StreamInterface> map = new LinkedHashMap<>();
                                    map.put("generation", new MersenneTwister((Long) message[11]));
                                    animator.initialize(Time.ZERO, (Duration) message[9], (Duration) message[10], this.model,
                                            map);
                                    OTSAnimationPanel animationPanel = new OTSAnimationPanel(
                                            this.model.getNetwork().getExtent(), new Dimension(1100, 1000), animator,
                                            this.model, OTSSwingApplication.DEFAULT_COLORER, this.model.getNetwork());
                                    DefaultAnimationFactory.animateXmlNetwork(this.model.getNetwork(), animator,
                                            new DefaultSwitchableGTUColorer());
                                    new Sim0MQRemoteControlSwingApplication(this.model, animationPanel);
                                    JFrame frame = (JFrame) animationPanel.getParent().getParent().getParent();
                                    frame.setExtendedState(Frame.NORMAL);
                                    frame.setSize(new Dimension(1100, 1000));
                                    frame.setBounds(0, 25, 1100, 1000);
                                    animator.setSpeedFactor(Double.MAX_VALUE, true);
                                    animator.setSpeedFactor(1000.0, true);
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
                            }
                            else
                            {
                                result = "no network, warmupTime and/or runTime provided with LOADNETWORK command";
                            }
                            break;

                        case "SIMULATEUNTIL":
                            if (null == this.model)
                            {
                                result = "No model loaded";
                            }
                            else if (message.length == 9 && message[8] instanceof Time)
                            {
                                OTSSimulatorInterface simulator = this.model.getSimulator();
                                simulator.runUpTo((Time) message[8]);
                                while (simulator.isRunning())
                                {
                                    try
                                    {
                                        Thread.sleep(10);
                                    }
                                    catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }
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
                                for (GTU gtu : this.model.network.getGTUs())
                                {
                                    // Send information about one GTU to master
                                    try
                                    {
                                        DirectedPoint gtuPosition = gtu.getLocation();
                                        Object[] gtuData = new Object[] { gtu.getId(), gtu.getGTUType().toString(),
                                                gtuPosition.x, gtuPosition.y, gtuPosition.z, gtuPosition.getRotZ(),
                                                gtu.getAcceleration() };
                                        master.send(
                                                Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", "GTUPOSITION", 0, gtuData),
                                                0);
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
                            result = "Unimplemented command " + command;
                            break;
                    }
                }
            }
            catch (Sim0MQException | SerializationException e)
            {
                e.printStackTrace();
                result = "Could not decode command: " + e.getMessage();
            }
            catch (SimRuntimeException | NamingException | RemoteException | DSOLException | OTSDrawingException e)
            {
                e.printStackTrace();
                result = "Could not initialize simulation: " + e.getMessage();
            }
            // Send reply to master
            try
            {
                master.send(Sim0MQMessage.encodeUTF8(true, 0, "slave", "master", "READY", 0, result), 0);
            }
            catch (Sim0MQException | SerializationException e)
            {
                e.printStackTrace();
                break; // this is fatal
            }
        }
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
                ZEvent event = monitor.nextEvent();
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
        private OTSRoadNetwork network;

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
            try
            {
                this.simulator.addListener(this, DEVSRealTimeClock.CHANGE_SPEED_FACTOR_EVENT);
                this.simulator.addListener(this, SimulatorInterface.TIME_CHANGED_EVENT);
            }
            catch (RemoteException exception1)
            {
                exception1.printStackTrace();
            }
            this.network = new OTSRoadNetwork(getShortName(), true);
            try
            {
                XmlNetworkLaneParser.build(new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8)), this.network,
                        getSimulator(), false);
                LaneCombinationList ignoreList = new LaneCombinationList();
                LaneCombinationList permittedList = new LaneCombinationList();
                ConflictBuilder.buildConflictsParallel(this.network, this.network.getGtuType(GTUType.DEFAULTS.VEHICLE),
                        getSimulator(), new ConflictBuilder.FixedWidthGenerator(Length.instantiateSI(2.0)), ignoreList,
                        permittedList);
                // new GTUDumper(simulator, Time.ZERO, Duration.instantiateSI(60), network, "C:/Temp/aimsun");
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

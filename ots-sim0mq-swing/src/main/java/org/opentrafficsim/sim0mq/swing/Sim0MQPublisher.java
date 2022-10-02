package org.opentrafficsim.sim0mq.swing;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.decoderdumper.HexDumper;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGtuColorer;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OtsNetwork;
import org.opentrafficsim.core.object.InvisibleObjectInterface;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.conflict.LaneCombinationList;
import org.opentrafficsim.sim0mq.publisher.IncomingDataHandler;
import org.opentrafficsim.sim0mq.publisher.Publisher;
import org.opentrafficsim.sim0mq.publisher.ReturnWrapper;
import org.opentrafficsim.sim0mq.publisher.ReturnWrapperImpl;
import org.opentrafficsim.sim0mq.publisher.SubscriptionHandler;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.opentrafficsim.swing.gui.OTSSwingApplication;
import org.opentrafficsim.swing.script.AbstractSimulationScript;
import org.opentrafficsim.trafficcontrol.TrafficControlException;
import org.opentrafficsim.trafficcontrol.trafcod.TrafCOD;
import org.sim0mq.Sim0MQException;
import org.sim0mq.message.Sim0MQMessage;
import org.xml.sax.SAXException;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.swing.gui.TabbedContentPane;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Sim0MQPublisher - make many OTS simulation controls and observations available over Sim0MQ.
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public final class Sim0MQPublisher
{
    /** The publisher. */
    private Publisher publisher = null;

    /** The ZContect. */
    private final ZContext zContext;

    /** The simulation model. */
    private Sim0MQOTSModel model = null;

    /** The OTS road network. */
    private OTSRoadNetwork network = null;

    /** The OTS animation panel. */
    private OTSAnimationPanel animationPanel = null;

    /**
     * Create a new Sim0MQPublisher that is operated through //inproc sockets.
     * @param zContext ZContext; needed to create the sockets
     * @param controlInput String; PULL socket for control input
     * @param resultOutput String; PUSH socket to output results
     */
    public Sim0MQPublisher(final ZContext zContext, final String controlInput, final String resultOutput)
    {
        this.zContext = zContext;
        ZMQ.Socket controlSocket = zContext.createSocket(SocketType.PULL);
        controlSocket.bind("inproc://" + controlInput);
        ZMQ.Socket resultOutputQueue = zContext.createSocket(SocketType.PUSH);
        resultOutputQueue.connect("inproc://" + resultOutput);
        pollingLoop(controlSocket, resultOutputQueue);
    }

    /**
     * Create a new Sim0MQPublisher that uses TCP transport.
     * @param port int; port number to bind to
     */
    public Sim0MQPublisher(final int port)
    {
        this.zContext = new ZContext(5);
        ZMQ.Socket socket = this.zContext.createSocket(SocketType.PAIR);
        socket.bind("tcp://*:" + port);
        pollingLoop(socket, socket);
    }

    /**
     * Create a new Sim0MQPublisher that uses TCP transport.
     * @param port int; port number to bind to
     * @param preloadedSimulation AbstractSimulationScript; a fully loaded (but not started) simulation
     * @param additionalSubscriptionHandlers List&lt;SubscriptionHandler&gt;; list of additional subscription handlers (may be
     *            null)
     * @param incomingDataHandlers List&lt;IncomfingDataHandler&gt;; list of additional handlers for incoming data that is not
     *            handled by the standard Sim0MQPublisher (may be null)
     * @throws RemoteException when construction of the Publisher failed
     */
    public Sim0MQPublisher(final int port, final AbstractSimulationScript preloadedSimulation,
            final List<SubscriptionHandler> additionalSubscriptionHandlers,
            final List<IncomingDataHandler> incomingDataHandlers) throws RemoteException
    {
        this.zContext = new ZContext(5);
        ZMQ.Socket socket = this.zContext.createSocket(SocketType.PAIR);
        socket.bind("tcp://*:" + port);
        this.network = preloadedSimulation.getNetwork();
        this.model = new Sim0MQOTSModel("Remotely controlled OTS model", this.network, null);
        this.publisher = new Publisher(this.network, additionalSubscriptionHandlers, incomingDataHandlers);
        ((OtsAnimator) preloadedSimulation.getSimulator()).setSpeedFactor(Double.MAX_VALUE, true);
        ((OtsAnimator) preloadedSimulation.getSimulator()).setSpeedFactor(1000.0, true);
        pollingLoop(socket, socket);
        System.exit(0);
    }

    /**
     * Create a new Sim0MQPublisher that uses TCP transport.
     * @param port int; port number to bind to
     * @param preloadedSimulation AbstractSimulationScript; a fully loaded (but not started) simulation
     * @param additionalSubscriptionHandlers List&lt;SubscriptionHandler&gt;; list of additional subscription handlers (may be
     *            null)
     * @throws RemoteException when construction of the Publisher failed
     */
    public Sim0MQPublisher(final int port, final AbstractSimulationScript preloadedSimulation,
            final List<SubscriptionHandler> additionalSubscriptionHandlers) throws RemoteException
    {
        this(port, preloadedSimulation, additionalSubscriptionHandlers, null);
    }

    /**
     * Poller that receives the commands and ensures that various output sources can talk to the master.
     * @param controlSocket ZMQ.Socket; PULL socket for commands from the master
     * @param resultOutputQueue ZMQ.Socket; PULL socket for output that must be relayed to the master
     */
    private void pollingLoop(final ZMQ.Socket controlSocket, final ZMQ.Socket resultOutputQueue)
    {
        System.out
                .println("Publisher communication relay and simulation control thread id is " + Thread.currentThread().getId());
        resultOutputQueue.setHWM(100000);
        AtomicInteger packetsSent = new AtomicInteger(0);
        Map<Long, ZMQ.Socket> socketMap = new HashMap<>();
        ZMQ.Socket resultInputQueue = this.zContext.createSocket(SocketType.PULL);
        resultInputQueue.bind("inproc://simulationEvents");
        // Poll the two input sockets using ZMQ poller
        ZMQ.Poller poller = this.zContext.createPoller(2);
        // TODO ensure that this also handles a closed control socket gracefully
        poller.register(resultInputQueue, ZMQ.Poller.POLLIN);
        poller.register(controlSocket, ZMQ.Poller.POLLIN);
        while (!Thread.currentThread().isInterrupted())
        {
            // System.out.println("Publisher calls Poller.poll()");
            poller.poll();
            if (poller.pollin(0))
            {
                byte[] data = resultInputQueue.recv();
                // System.out.println("Publisher got outgoing result of " + data.length + " bytes");
                byte[] fixedData = data;
                int number = -1;
                try
                {
                    // Patch the sender field to include the packet counter value - this is bloody expensive...
                    Object[] messageFields = Sim0MQMessage.decode(data).createObjectArray();
                    Object[] newMessageFields = Arrays.copyOfRange(messageFields, 8, messageFields.length);
                    number = packetsSent.addAndGet(1);
                    fixedData = Sim0MQMessage.encodeUTF8(true, messageFields[2], String.format("slave_%05d", number),
                            messageFields[4], messageFields[5], messageFields[6], newMessageFields);
                    // System.out
                    // .println("Prepared message " + number + ", type is \"" + messageFields[5] + "\", " + messageFields[6]);
                }
                catch (Sim0MQException | SerializationException e)
                {
                    e.printStackTrace();
                }
                resultOutputQueue.send(fixedData, 0);
                // System.out.println("Outgoing result handed over to controlSocket");
                continue; // Check for more results before checking the control input
            }
            if (poller.pollin(1))
            {
                byte[] data = controlSocket.recv();
                // System.out.println("Publisher received a command of " + data.length + " bytes");
                if (!handleCommand(data, socketMap))
                {
                    break;
                }
            }
        }
        System.out.println("Exiting publisher polling loop");
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
        try
        {
            OtsAnimator animator = new OtsAnimator("OTS Animator");
            this.network = new OTSRoadNetwork("OTS model for Sim0MQPublisher", true, animator);
            this.model = new Sim0MQOTSModel("Remotely controlled OTS model", this.network, xml);
            Map<String, StreamInterface> map = new LinkedHashMap<>();
            map.put("generation", new MersenneTwister(seed));
            animator.initialize(Time.ZERO, warmupTime, simulationDuration, this.model, map);
            this.publisher = new Publisher(this.network);
            this.animationPanel = new OTSAnimationPanel(this.model.getNetwork().getExtent(), new Dimension(1100, 1000),
                    animator, this.model, OTSSwingApplication.DEFAULT_COLORER, this.model.getNetwork());
            new OTSSimulationApplication<Sim0MQOTSModel>(this.model, this.animationPanel);
            DefaultAnimationFactory.animateXmlNetwork(this.model.getNetwork(), new DefaultSwitchableGtuColorer());
            JFrame frame = (JFrame) this.animationPanel.getParent().getParent().getParent();
            frame.setExtendedState(Frame.NORMAL);
            frame.setSize(new Dimension(1100, 1000));
            frame.setBounds(0, 25, 1100, 1000);
            animator.setSpeedFactor(Double.MAX_VALUE, true);
            animator.setSpeedFactor(1000.0, true);

            ImmutableMap<String, InvisibleObjectInterface> invisibleObjectMap = this.model.getNetwork().getInvisibleObjectMap();
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
                        TabbedContentPane tabbedPane = this.animationPanel.getTabbedPane();
                        tabbedPane.addTab(tabbedPane.getTabCount() - 1, trafCOD.getId(), wrapper);
                    }
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
            this.animationPanel.actionPerformed(new ActionEvent(this, 0, "ZoomAll"));
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
        return null;
    }

    /**
     * Execute one remote control command.
     * @param data byte[]; the SIM0MQ encoded command
     * @param socketMap Map&lt;Long, ZMQ.Socket&gt;; cache of created sockets for returned messages
     * @return boolean; true if another command can be processed after this one; false when no further commands can be processed
     */
    @SuppressWarnings("checkstyle:methodlength")
    private boolean handleCommand(final byte[] data, final Map<Long, ZMQ.Socket> socketMap)
    {
        boolean result = true;
        try
        {
            Object[] message = Sim0MQMessage.decode(data).createObjectArray();
            String resultMessage = "OK";
            Boolean ackNack = null;

            if (message.length >= 8 && message[5] instanceof String)
            {
                String command = (String) message[5];
                System.out.println("Publisher thread decoded Sim0MQ command: " + command);

                String[] parts = command.split("\\|");
                if (parts.length == 2)
                {
                    // This is a command for the embedded Publisher
                    ReturnWrapperImpl returnWrapper = new ReturnWrapperImpl(this.zContext,
                            new Object[] {"SIM01", true, message[2], message[3], message[4], parts[0], message[6], 0},
                            socketMap);
                    if (null == this.publisher)
                    {
                        returnWrapper.nack("No simulation loaded; cannot execute command " + command);
                        System.err.println("No publisher for command " + command);
                        return true;
                    }
                    Object[] payload = Arrays.copyOfRange(message, 8, message.length);
                    this.publisher.executeCommand(parts[0], parts[1], payload, returnWrapper);
                    return true;
                }
                else
                {
                    switch (command)
                    {
                        case "NEWSIMULATION":
                            if (message.length == 12 && message[8] instanceof String && message[9] instanceof Duration
                                    && message[10] instanceof Duration && message[11] instanceof Long)
                            {
                                if (null != this.animationPanel)
                                {
                                    for (Container container = this.animationPanel; container != null; container =
                                            container.getParent())
                                    {
                                        if (container instanceof JFrame)
                                        {
                                            JFrame jFrame = (JFrame) container;
                                            jFrame.dispose();
                                        }
                                    }
                                }
                                // System.out.println("xml length = " + ((String) message[8]).length());
                                resultMessage = loadNetwork((String) message[8], (Duration) message[9], (Duration) message[10],
                                        (Long) message[11]);
                                ackNack = null == resultMessage;
                                if (ackNack)
                                {
                                    resultMessage = "OK";
                                }
                            }
                            else
                            {
                                resultMessage =
                                        "No network, warmupTime and/or runTime, or seed provided with NEWSIMULATION command";
                                ackNack = false;
                            }
                            break;

                        case "DIE":
                            for (Container container = this.animationPanel; container != null; container =
                                    container.getParent())
                            {
                                // System.out.println("container is " + container);
                                if (container instanceof JFrame)
                                {
                                    JFrame jFrame = (JFrame) container;
                                    jFrame.dispose();
                                }
                            }
                            return false;

                        case "SIMULATEUNTIL":
                            if (message.length == 9 && message[8] instanceof Time)
                            {
                                System.out.println("Simulating up to " + message[8]);
                                if (null == this.network)
                                {
                                    resultMessage = "No network loaded";
                                    ackNack = false;
                                    break;
                                }
                                OtsSimulatorInterface simulator = this.network.getSimulator();
                                if (simulator.getSimulatorTime().ge(simulator.getReplication().getEndTime()))
                                {
                                    resultMessage = "Simulation is already at end of simulation time";
                                    ackNack = false;
                                    break;
                                }
                                if (simulator.isStartingOrRunning())
                                {
                                    resultMessage = "Simulator is already running"; // cannot happen for now
                                    ackNack = false;
                                    break;
                                }
                                ReturnWrapper returnWrapper = new ReturnWrapperImpl(this.zContext, new Object[] {"SIM01", true,
                                        message[2], message[3], message[4], message[5], message[6], 0}, socketMap);
                                returnWrapper.ack(resultMessage);
                                simulator.runUpTo((Time) message[8]);
                                int count = 0;
                                while (this.network.getSimulator().isStartingOrRunning())
                                {
                                    System.out.print(".");
                                    count++;
                                    if (count > 1000) // Quit after 1000 attempts of 10 ms; 10 s
                                    {
                                        System.out.println("TIMEOUT - STOPPING SIMULATOR. TIME = "
                                                + this.network.getSimulator().getSimulatorTime());
                                        this.network.getSimulator().stop();
                                        Iterator<SimEventInterface<Duration>> elIt =
                                                this.network.getSimulator().getEventList().iterator();
                                        while (elIt.hasNext())
                                        {
                                            System.out.println("EVENTLIST: " + elIt.next());
                                        }
                                    }
                                    try
                                    {
                                        Thread.sleep(10); // 10 ms
                                    }
                                    catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                                // TODO Fix this (it is still needed - 2020-06-16)
                                try
                                {
                                    Thread.sleep(100); // EXTRA STOP FOR SYNC REASONS - BUG IN DSOL!
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                                return true; // ack has been sent when simulation started
                            }
                            else
                            {
                                resultMessage = "Bad or missing stop time";
                                ackNack = false;
                            }
                            break;

                        default:
                            IncomingDataHandler incomingDataHandler = this.publisher.lookupIncomingDataHandler(command);
                            if (incomingDataHandler != null)
                            {
                                resultMessage = incomingDataHandler.handleIncomingData(message);
                                ackNack = resultMessage == null;
                            }
                            else
                            {
                                resultMessage = "Don't know how to handle message:\n" + Sim0MQMessage.print(message);
                                ackNack = false;
                            }
                            break;
                    }
                }
                if (resultMessage != null)
                {
                    ReturnWrapper returnWrapper = new ReturnWrapperImpl(this.zContext,
                            new Object[] {"SIM01", true, message[2], message[3], message[4], message[5], message[6], 0},
                            socketMap);
                    if (ackNack)
                    {
                        returnWrapper.ack(resultMessage);
                    }
                    else
                    {
                        returnWrapper.nack(resultMessage);
                    }
                }
            }
            else
            {
                // We cannot construct a ReturnWrapper because the request has too few fields.
                System.err.println("Publisher expected Sim0MQ command but is has too few fields:");
                System.err.println(HexDumper.hexDumper(data));
                return true; // Do we really want to try again?
            }
        }
        catch (Sim0MQException | SerializationException | RemoteException e)
        {
            System.err.println("Publisher thread could not decode command");
            e.printStackTrace();
        }
        return result;
    }

}

/**
 * The Model.
 */
class Sim0MQOTSModel extends AbstractOtsModel
{
    /** */
    private static final long serialVersionUID = 20170419L;

    /** The network. */
    private final OTSRoadNetwork network;

    /** The XML. */
    private final String xml;

    /**
     * @param description String; the model description
     * @param network OTSRoadNetwork; the network
     * @param xml String; the XML description of the simulation model
     */
    Sim0MQOTSModel(final String description, final OTSRoadNetwork network, final String xml)
    {
        super(network.getSimulator(), network.getId(), description);
        this.network = network;
        this.xml = xml;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel() throws SimRuntimeException
    {
        try
        {
            XmlNetworkLaneParser.build(new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8)), this.network,
                    false);
            ConflictBuilder.buildConflictsParallel(this.network, this.network.getGtuType(GtuType.DEFAULTS.VEHICLE),
                    getSimulator(), new ConflictBuilder.FixedWidthGenerator(Length.instantiateSI(2.0)),
                    new LaneCombinationList(), new LaneCombinationList());
        }
        catch (NetworkException | OtsGeometryException | JAXBException | URISyntaxException | XmlParserException | SAXException
                | ParserConfigurationException | GtuException | IOException | TrafficControlException exception)
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
        return "Sim0MQPublisherModel";
    }

}

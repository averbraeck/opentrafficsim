package org.sim0mq.publisher;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.NamingException;
import javax.swing.JFrame;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.Throw;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.decoderdumper.HexDumper;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.serialization.EndianUtil;
import org.djutils.serialization.SerializationException;
import org.djutils.serialization.util.SerialDataDumper;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
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
import org.sim0mq.Sim0MQException;
import org.sim0mq.message.Sim0MQMessage;
import org.xml.sax.SAXException;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.DSOLException;

/**
 * Test code to see if the Publisher works.
 */
public final class PublisherExperiment
{
    /** The publisher. */
    private final Publisher publisher;

    /** The ZContect. */
    private final ZContext zContext;

    /** The OTS road network. */
    private final OTSRoadNetwork network;

    /** The OTS animation panel. */
    private final OTSAnimationPanel animationPanel;

    /**
     * Create the publisher part.
     * @param zContext ZContext; needed to create the sockets
     * @throws IOException ...
     * @throws NamingException ...
     * @throws SimRuntimeException ...
     * @throws OTSDrawingException ...
     * @throws DSOLException ...
     */
    private PublisherExperiment(final ZContext zContext)
            throws IOException, SimRuntimeException, NamingException, OTSDrawingException, DSOLException
    {
        this.zContext = zContext;
        AtomicInteger packetsSent = new AtomicInteger(0);
        Map<Long, ZMQ.Socket> socketMap = new HashMap<>();
        OTSAnimator animator = new OTSAnimator("OTS Animator");
        this.network = new OTSRoadNetwork("OTS model for Publisher test", true, animator);
        String xml = new String(Files
                .readAllBytes(Paths.get("C:/Users/pknoppers/Java/ots-demo/src/main/resources/TrafCODDemo2/TrafCODDemo2.xml")));
        Sim0MQOTSModel model = new Sim0MQOTSModel("Remotely controlled OTS model", network, xml);
        Map<String, StreamInterface> map = new LinkedHashMap<>();
        Long seed = 123456L;
        map.put("generation", new MersenneTwister(seed));
        Duration warmupDuration = Duration.ZERO;
        Duration runDuration = new Duration(3600, DurationUnit.SECOND);
        animator.initialize(Time.ZERO, warmupDuration, runDuration, model, map);
        this.animationPanel = new OTSAnimationPanel(model.getNetwork().getExtent(), new Dimension(1200, 1000), animator, model,
                OTSSwingApplication.DEFAULT_COLORER, model.getNetwork());
        DefaultAnimationFactory.animateXmlNetwork(model.getNetwork(), new DefaultSwitchableGTUColorer());
        new OTSSimulationApplication<OTSModelInterface>(model, animationPanel);
        JFrame frame = (JFrame) animationPanel.getParent().getParent().getParent();
        frame.setExtendedState(Frame.NORMAL);
        frame.setSize(new Dimension(1100, 1000));
        frame.setBounds(0, 25, 1100, 1000);
        animator.setSpeedFactor(Double.MAX_VALUE, true);
        animator.setSpeedFactor(1000.0, true);
        this.publisher = new Publisher(network);
        System.out.println("MasterCommunication thread id is " + Thread.currentThread().getId());
        ZMQ.Socket controlSocket = zContext.createSocket(SocketType.PAIR);
        controlSocket.setHWM(100000);
        controlSocket.bind("inproc://publisher");
        ZMQ.Socket resultQueue = zContext.createSocket(SocketType.PULL);
        resultQueue.bind("inproc://simulationEvents");
        // Poll the two input sockets using ZMQ poller
        ZMQ.Poller poller = zContext.createPoller(2);
        poller.register(resultQueue, ZMQ.Poller.POLLIN);
        poller.register(controlSocket, ZMQ.Poller.POLLIN);
        while (!Thread.currentThread().isInterrupted())
        {
            poller.poll();
            if (poller.pollin(0))
            {
                byte[] data = resultQueue.recv(ZMQ.DONTWAIT);
                Throw.whenNull(data, "cannot happen");
                // System.out.println("Got outgoing result");
                controlSocket.send(data, 0);
                // System.out.println("Outgoing result handed over to controlSocket");
                continue;
            }
            else if (poller.pollin(1))
            {
                byte[] data = controlSocket.recv(ZMQ.DONTWAIT);
                Throw.whenNull(data, "cannot happen");
                System.out.println("Publisher thread received a command of " + data.length + " bytes");
                if (!handleCommand(data, socketMap, packetsSent))
                {
                    return;
                }
            }
        }
        /*-
        // Use busy waiting (with 1 ms sleep)
        while (!Thread.interrupted())
        {
            byte[] data;
            // Handle results from the simulator event system
            data = resultQueue.recv(ZMQ.DONTWAIT);
            if (null != data)
            {
                // System.out.println("Got outgoing result");
                controlSocket.send(data, 0);
                // System.out.println("Outgoing result handed over to controlSocket");
                continue;
            }
            data = controlSocket.recv(ZMQ.DONTWAIT);
            if (null != data)
            {
                System.out.println("Publisher thread received a command of " + data.length + " bytes");
                if (!handleCommand(data))
                {
                    return;
                }
                continue;
            }
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException e)
            {
                System.out.println("Publisher polling loop thread was interrupted in sleep ... returning");
                return;
            }
        }
        */
        System.out.println("Exiting publisher polling loop");
    }

    /**
     * Execute one remote control command.
     * @param data byte[]; the SIM0MQ encoded command
     * @param socketMap Map&lt;Long, ZMQ.Socket&gt;; cache of created sockets for returned messages
     * @param packetsSent AtomicInteger; counter for returned messages
     * @return boolean; true if another command can be processed after this one; false when no further commands can be processed
     */
    private boolean handleCommand(final byte[] data, final Map<Long, ZMQ.Socket> socketMap, final AtomicInteger packetsSent)
    {
        Object[] message;
        try
        {
            message = Sim0MQMessage.decode(data).createObjectArray();
            System.out.println("Publisher thread decoded Sim0MQ message:");

            if (message.length >= 8 && message[5] instanceof String)
            {
                String command = (String) message[5];
                System.out.println("Command is " + command);

                String[] parts = command.split("\\|");
                if (parts.length == 2)
                {
                    Object[] payload = new Object[message.length - 8];
                    for (int index = 0; index < payload.length; index++)
                    {
                        payload[index] = message[index + 8];
                    }
                    ReturnWrapper returnWrapper = new ReturnWrapper(zContext,
                            new Object[] { "SIM01", true, message[2], message[3], message[4], parts[0], 0, 0 }, socketMap,
                            packetsSent);
                    publisher.executeCommand(parts[0], parts[1], payload, returnWrapper);
                }
                else
                {
                    switch (command)
                    {
                        case "DIE":
                            for (Container container = animationPanel; container != null; container = container.getParent())
                            {
                                // System.out.println("container is " + container);
                                if (container instanceof JFrame)
                                {
                                    JFrame jFrame = (JFrame) container;
                                    jFrame.dispose();
                                }
                            }
                            return false; // There may not be a return ...

                        case "SIMULATEUNTIL":
                            if (message.length == 9 && message[8] instanceof Time)
                            {
                                System.out.println("Simulating up to " + message[8]);
                                this.network.getSimulator().runUpTo((Time) message[8]);
                                int count = 0;
                                while (this.network.getSimulator().isStartingOrRunning())
                                {
                                    System.out.print(".");
                                    count++;
                                    if (count > 1000) // 10 seconds
                                    {
                                        System.out.println("SIMULATOR DOES NOT STOP. TIME = "
                                                + this.network.getSimulator().getSimulatorTime());
                                        Iterator<SimEventInterface<SimTimeDoubleUnit>> elIt =
                                                this.network.getSimulator().getEventList().iterator();
                                        while (elIt.hasNext())
                                        {
                                            System.out.println("EVENTLIST: " + elIt.next());
                                        }
                                        this.network.getSimulator().stop();
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
                                System.out.println(
                                        "Simulator has stopped at time " + this.network.getSimulator().getSimulatorTime());
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
                                System.out.println("Bad or missing stop time");
                            }
                            break;

                        default:
                            System.out.println("Don't know how to handle message:");
                            System.out.println(Sim0MQMessage.print(message));
                            break;
                    }
                }
            }
            else
            {
                System.out.println("Don't know how to handle message:");
                System.out.println(HexDumper.hexDumper(data));
            }
        }
        catch (Sim0MQException | SerializationException | RemoteException e)
        {
            e.printStackTrace();
        }
        return true;
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

        @Override
        public void run()
        {
            try
            {
                new PublisherExperiment(zContext);
            }
            catch (SimRuntimeException | IOException | NamingException | OTSDrawingException | DSOLException e)
            {
                e.printStackTrace();
            }
        }

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
        ZContext zContext = new ZContext(10); // 10 IO threads
        PublisherThread publisherThread = new PublisherThread(zContext);
        publisherThread.start();
        Thread.sleep(5000); // Starts up a DSOL animator, loads a network generates conflicts, etc.

        ZMQ.Socket publisherSocket = zContext.createSocket(SocketType.PAIR);
        publisherSocket.connect("inproc://publisher");

        byte[] message;
        message = Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_CURRENT", 0);
        publisherSocket.send(message);
        Thread.sleep(2000);
        readMessages(publisherSocket);
        message = Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "SIMULATEUNTIL", 0,
                new Object[] { new Time(10, TimeUnit.BASE_SECOND) });
        publisherSocket.send(message);
        message = Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "|GET_CURRENT", 0);
        publisherSocket.send(message);
        Thread.sleep(2000);
        readMessages(publisherSocket);
        message = Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_CURRENT", 0);
        publisherSocket.send(message);
        Thread.sleep(2000);
        readMessages(publisherSocket);
        message = Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|SUBSCRIBE_TO_ADD", 0);
        publisherSocket.send(message);
        message = Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "SIMULATEUNTIL", 0,
                new Object[] { new Time(20, TimeUnit.BASE_SECOND) });
        publisherSocket.send(message);
        Thread.sleep(2000);
        readMessages(publisherSocket);
        message = Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_CURRENT", 0);
        publisherSocket.send(message);
        Thread.sleep(2000);
        readMessages(publisherSocket);
        // unsubscribe
        message = Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|UNSUBSCRIBE_FROM_ADD", 0);
        publisherSocket.send(message);
        message = Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "SIMULATEUNTIL", 0,
                new Object[] { new Time(30, TimeUnit.BASE_SECOND) });
        publisherSocket.send(message);
        Thread.sleep(2000);
        readMessages(publisherSocket);
        message = Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_CURRENT", 0);
        publisherSocket.send(message);
        Thread.sleep(2000);
        readMessages(publisherSocket);
        message = Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_RESULT_META_DATA", 0);
        publisherSocket.send(message);
        message = Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "GTUs in network|GET_ADDRESS_META_DATA", 0);
        publisherSocket.send(message);
        Thread.sleep(2000);
        readMessages(publisherSocket);
        message = Sim0MQMessage.encodeUTF8(true, 0, "Master", "Slave", "DIE", 0);
        publisherSocket.send(message);
        System.out.println("Publisher should be dead or dying");
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
            byte[] message = socket.recv(ZMQ.DONTWAIT);
            if (null != message)
            {
                System.out.println("Received " + message.length + " byte message:");
                System.out.println(SerialDataDumper.serialDataDumper(EndianUtil.BIG_ENDIAN, message));
                resultList.add(message);
            }
            else
            {
                System.out.println("Received " + resultList.size() + " messages");
                break;
            }
        }
        return resultList.toArray(new byte[resultList.size()][]);
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
            XmlNetworkLaneParser.build(new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8)), this.network,
                    false);
            ConflictBuilder.buildConflictsParallel(this.network, this.network.getGtuType(GTUType.DEFAULTS.VEHICLE),
                    getSimulator(), new ConflictBuilder.FixedWidthGenerator(Length.instantiateSI(2.0)),
                    new LaneCombinationList(), new LaneCombinationList());
        }
        catch (NetworkException | OTSGeometryException | JAXBException | URISyntaxException | XmlParserException | SAXException
                | ParserConfigurationException | GTUException | IOException | TrafficControlException exception)
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
        return "PublisherTestModel";
    }

}

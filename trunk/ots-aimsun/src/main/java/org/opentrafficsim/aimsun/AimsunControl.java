package org.opentrafficsim.aimsun;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.immutablecollections.ImmutableList;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.opentrafficsim.aimsun.proto.AimsunControlProtoBuf;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.animation.LinkAnimation;
import org.opentrafficsim.core.network.animation.NodeAnimation;
import org.opentrafficsim.road.animation.AnimationToggles;
import org.opentrafficsim.road.gtu.generator.GTUGeneratorAnimation;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODApplier.GeneratorObjects;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.gtu.generator.od.ODOptions.ShortestRouteRandomGTUCharacteristicsGeneratorOD;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.network.factory.xml.GTUTag;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.SpeedSign;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.SimpleAnimator;

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
     * @throws OTSGeometryException
     * @throws NetworkException
     * @throws NamingException
     * @throws ValueException
     * @throws SimRuntimeException
     * @throws ParameterException
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException, NamingException,
            ValueException, ParameterException, SimRuntimeException
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
            System.out.println("Calling command loop");
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
     * Retrieve the OD info from the XML comments and apply it to the network.
     * @throws NetworkException should never happen (of course)
     * @throws OTSGeometryException might happen if a centroid is positioned on top of the entry exit point of a link
     * @throws NamingException
     * @throws RemoteException
     * @throws ValueException
     * @throws SimRuntimeException
     * @throws ParameterException
     */
    @SuppressWarnings("synthetic-access")
    private void fixOD() throws NetworkException, OTSGeometryException, RemoteException, NamingException, ValueException,
            ParameterException, SimRuntimeException
    {
        // Reduce the list to only OD comments and strip the OD header and parse each into a key-value map.
        Map<String, Map<String, String>> odInfo = new HashMap<String, Map<String, String>>();
        for (String comment : this.model.getXMLComments())
        {
            if (comment.startsWith("OD "))
            {
                String odText = comment.substring(3);
                odInfo.put(odText, parseODLine(odText));
            }
        }
        System.out.println("There are " + odInfo.size() + " OD comments");
        OTSNetwork network = this.model.getNetwork();
        OTSSimulatorInterface simulator = (OTSSimulatorInterface) this.model.simulator;
        // Find the GTUTypes
        List<GTUType> gtuTypes = new ArrayList<>(this.model.gtuTypes);
        for (GTUType gtuType: gtuTypes)
        {
            if (GTUType.VEHICLE.equals(gtuType))
            {
                gtuTypes.remove(gtuType);
                break;
            }
        }
        double startTime = Double.NaN;
        // Extract the simulation start time
        for (Map<String, String> map : odInfo.values())
        {
            String startTimeString = map.get("simulationStartTime");
            if (null != startTimeString)
            {
                startTime = Double.parseDouble(startTimeString);
            }
        }
        if (Double.isNaN(startTime))
        {
            throw new NetworkException("Cannot find start time XML comment");
        }
        // Construct the centroid nodes and the links between the centroid nodes and the generation and extraction nodes
        Set<Node> origins = new HashSet<>();
        Set<Node> destinations = new HashSet<>();
        Set<String> startTimeStrings = new HashSet<>();
        Map<String, String> durations = new HashMap<>();
        for (Map<String, String> map : odInfo.values())
        {
            String od = map.get("od");
            if (null == od)
            {
                continue;
            }
            String startTimeString = map.get("startTime");
            if (null != startTimeString)
            {
                startTimeStrings.add(startTimeString);
                String durationString = map.get("duration");
                if (null == durationString)
                {
                    throw new NetworkException("No duration specified");
                }
                String old = durations.get(startTimeString);
                if (null != old && (!durationString.equals(old)))
                {
                    throw new NetworkException("Duration for period starting at " + startTimeString + " changed from " + old
                            + " to " + durationString);
                }
                else
                {
                    durations.put(startTimeString, durationString);
                }
            }
            String centroidName = map.get("centroid");
            String[] coordinates = map.get("centroidLocation").split(",");
            OTSPoint3D centroidPoint =
                    new OTSPoint3D(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
            Node centroidNode = network.getNode(centroidName);
            if (null == centroidNode)
            {
                centroidNode = new OTSNode(network, centroidName, centroidPoint);
                new NodeAnimation(centroidNode, simulator);
            }
            String linkId = map.get("link");
            Link link = network.getLink(linkId);
            if (null == link)
            {
                throw new NetworkException("Cannot find link with id \"" + linkId + "\"");
            }
            Node from = null;
            Node to = null;
            if ("attracts".equals(od))
            {
                destinations.add(centroidNode);
                from = link.getEndNode();
                to = centroidNode;

            }
            else if ("generates".equals(od))
            {
                origins.add(centroidNode);
                from = centroidNode;
                to = link.getStartNode();
            }
            OTSLine3D designLine = new OTSLine3D(from.getPoint(), to.getPoint());
            String linkName = String.format("connector_from_%s_to_%s", from.getId(), to.getId());
            Link connectorLink = network.getLink(linkName);
            if (null == connectorLink)
            {
                System.out.println("Constructing connector link " + linkName);
                connectorLink =
                        new CrossSectionLink(network, linkName, from, to, LinkType.CONNECTOR, designLine, simulator,
                                LaneKeepingPolicy.KEEP_RIGHT);
                new LinkAnimation(connectorLink, simulator, 0.5f);
            }
        }
        if (startTimeStrings.size() != 1)
        {
            throw new NetworkException("Cannot handle multiple start times - yet");
        }
        String startTimeString = startTimeStrings.iterator().next();
        double start = Double.parseDouble(startTimeString);
        start = 0;
        double duration = Double.parseDouble(durations.get(startTimeString));
        TimeVector tv = new TimeVector(new double[] { start, start + duration }, TimeUnit.BASE, StorageType.DENSE);
        // Categorization categorization = new Categorization("AimsunOTSExport", firstGTUType, otherGTUTypes);              
        Categorization categorization = new Categorization("AimsunOTSExport", GTUType.class);
        ODMatrix od =
                new ODMatrix("ODExample", new ArrayList<>(origins), new ArrayList<>(destinations), categorization, tv,
                        Interpolation.STEPWISE);
        for (Map<String, String> map : odInfo.values())
        {
            String flow = map.get("flow");
            if (null == flow)
            {
                continue;
            }
            String vehicleClassName = map.get("vehicleClass");
            GTUType gtuType = null;
            for (GTUType gt : gtuTypes)
            {
                if (gt.getId().equals(vehicleClassName))
                {
                    gtuType = gt;
                }
            }
            if (null == gtuType)
            {
                throw new NetworkException("Can not find GTUType with name " + vehicleClassName);
            }
            Category category = new Category(categorization, gtuType);
            Node from = network.getNode(map.get("origin"));
            Node to = network.getNode(map.get("destination"));
            FrequencyVector demand =
                    new FrequencyVector(new double[] { Double.parseDouble(map.get("flow")), 0 }, FrequencyUnit.PER_HOUR,
                            StorageType.DENSE);
            od.putDemandVector(from, to, category, demand);
            System.out.println("Adding demand from " + from.getId() + " to " + to.getId() + " category " + category + ": "
                    + demand);
        }
        Set<TemplateGTUType> templates = new HashSet<>();
        for (GTUType gtuType : gtuTypes)
        {
            GTUTag gtuTag = this.model.gtuTags.get(gtuType.getId());
            templates.add(new TemplateGTUType(gtuType, gtuTag.lengthDist, gtuTag.widthDist, gtuTag.maxSpeedDist));
        }
        ODOptions odOptions = new ODOptions().set(ODOptions.GTU_TYPE, 
                new ShortestRouteRandomGTUCharacteristicsGeneratorOD(templates));
        // ODOptions odOptions = new ODOptions().set(ODOptions.GTU_TYPE, ODOptions.SHORTEST_ROUTE);
        // ODApplier.applyOD(network, od, (OTSDEVSSimulatorInterface) simulator, new ODOptions());
        Map<String, GeneratorObjects> generatedObjects =
                ODApplier.applyOD(network, od, (OTSDEVSSimulatorInterface) simulator, odOptions);
        for (String str : generatedObjects.keySet())
        {
            new GTUGeneratorAnimation(generatedObjects.get(str).getGenerator(), simulator);
        }
        od.print();
    }

    /**
     * Parse a line that should look like a list of key=value pairs.
     * @param line String; the line to parse
     * @return Map&lt;String,String&gt;; the parsed line
     */
    private Map<String, String> parseODLine(final String line)
    {
        Map<String, String> result = new HashMap<>();
        // For now we'll assume that names of centroids, links and nodes do not contain spaces.
        for (String pair : line.split(" "))
        {
            String[] fields = pair.split("=");
            // Concatenate elements 1..n
            for (int index = fields.length; --index >= 2;)
            {
                fields[index - 1] += "=" + fields[index];
            }
            if (fields.length < 2)
            {
                throw new IndexOutOfBoundsException("can not find equals sign in \"" + pair + "\"");
            }
            if (fields[1].startsWith("\"") && fields[1].endsWith("\"") && fields[1].length() >= 2)
            {
                fields[1] = fields[1].substring(1, fields[1].length() - 1);
            }
            result.put(fields[0], fields[1]);
        }
        return result;
    }

    /**
     * Process incoming commands.
     * @param socket Socket; the communications channel to Aimsun
     * @throws IOException when communication with Aimsun fails
     * @throws OTSGeometryException
     * @throws NetworkException
     * @throws NamingException
     * @throws ValueException
     * @throws SimRuntimeException
     * @throws ParameterException
     */
    private void commandLoop(final Socket socket) throws IOException, NetworkException, OTSGeometryException,
            NamingException, ValueException, ParameterException, SimRuntimeException
    {
        System.out.println("Entering command loop");
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
                System.out.println("expecting message of " + size + " bytes");
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
                        try (PrintWriter pw = new PrintWriter("d:/AimsunOtsNetwork.xml"))
                        {
                            pw.print(this.networkXML);
                        }
                        Duration runDuration = new Duration(createSimulation.getRunTime(), DurationUnit.SECOND);
                        System.out.println("runDuration " + runDuration);
                        Duration warmupDuration = new Duration(createSimulation.getWarmUpTime(), DurationUnit.SECOND);
                        try
                        {
                            SimpleAnimator animator =
                                    buildAnimator(Time.ZERO, warmupDuration, runDuration, new ArrayList<Property<?>>(),
                                            null, true);
                            animator.setSpeedFactor(Double.MAX_VALUE, true);
                        }
                        catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException exception1)
                        {
                            exception1.printStackTrace();
                        }
                        fixOD();
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
                            size = result.getSerializedSize();
                            System.out.print("Transmitting " + this.model.getNetwork().getGTUs().size()
                                    + " GTU positions encoded in " + size + " bytes ... ");
                            sizeBytes[0] = (byte) ((size >> 24) & 0xff);
                            sizeBytes[1] = (byte) ((size >> 16) & 0xff);
                            sizeBytes[2] = (byte) ((size >> 8) & 0xff);
                            sizeBytes[3] = (byte) (size & 0xff);
                            outputStream.write(sizeBytes);
                            buffer = new byte[size];
                            buffer = result.toByteArray();
                            outputStream.write(buffer);
                            System.out.println("Done");
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
    protected OTSModelInterface makeModel() throws OTSSimulationException
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

        /** The XML comments (used to patch up the OD information). */
        private ImmutableList<String> xmlComments;
        
        /** The GTU types found by the XML parser. */
        private Collection<GTUType> gtuTypes;

        /** The simulator. */
        private SimulatorInterface<Time, Duration, OTSSimTimeDouble> simulator;

        /** The GTU tags. */
        private Map<String,GTUTag> gtuTags;

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
                @SuppressWarnings("synthetic-access")
                String xml = AimsunControl.this.networkXML;
                this.network = nlp.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
                this.xmlComments = nlp.getXMLComments();
                this.gtuTypes = nlp.gtuTypes.values();
                this.gtuTags = nlp.gtuTags;
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /**
         * Retrieve the XML comments.
         * @return ImmutableList&lt;String&gt;; the XML comments
         */
        public ImmutableList<String> getXMLComments()
        {
            return this.xmlComments;
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
            // TODO: WIP
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

    }

    /** {@inheritDoc} */
    @Override
    protected final void addAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesFull(this);
        this.toggleAnimationClass(OTSLink.class);
        this.toggleAnimationClass(OTSNode.class);
        showAnimationClass(SpeedSign.class);
    }

}

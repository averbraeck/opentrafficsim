package org.opentrafficsim.road.network.factory.xml.old;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.djutils.immutablecollections.ImmutableList;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.animation.network.NetworkAnimation;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.gtu.generator.od.DefaultGTUCharacteristicsGeneratorOD;
import org.opentrafficsim.road.gtu.generator.od.GTUCharacteristicsGeneratorOD;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.demand.XmlOdParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class XmlNetworkLaneParserOld implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Global values from the GLOBAL tag. */
    @SuppressWarnings("visibilitymodifier")
    protected GlobalTag globalTag;

    /** The UNprocessed nodes for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, NodeTag> nodeTags = new LinkedHashMap<>();

    /** The UNprocessed connectors for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, ConnectorTag> connectorTags = new LinkedHashMap<>();

    /** The UNprocessed links for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, LinkTag> linkTags = new LinkedHashMap<>();

    /** The GTU tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    public Map<String, GTUTag> gtuTags = new LinkedHashMap<>();

    /** The GTUmix tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, GTUMixTag> gtuMixTags = new LinkedHashMap<>();

    /** The route tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RouteTag> routeTags = new LinkedHashMap<>();

    /** The route mix tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RouteMixTag> routeMixTags = new LinkedHashMap<>();

    /** The shortest route tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, ShortestRouteTag> shortestRouteTags = new LinkedHashMap<>();

    /** The shortest route mix tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, ShortestRouteMixTag> shortestRouteMixTags = new LinkedHashMap<>();

    /** The road type tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RoadTypeTag> roadTypeTags = new LinkedHashMap<>();

    /** The road layout tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RoadLayoutTag> roadLayoutTags = new LinkedHashMap<>();

    /** The GTUTypes that have been created. public to make it accessible from LaneAttributes. */
    @SuppressWarnings("visibilitymodifier")
    public Map<String, GTUType> gtuTypes = new LinkedHashMap<>();

    /** The LaneType tags that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, LaneTypeTag> laneTypeTags = new LinkedHashMap<>();

    /** The LaneTypes that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, LaneType> laneTypes = new LinkedHashMap<>();

    /** The simulator for creating the animation. Null if no animation needed. */
    @SuppressWarnings("visibilitymodifier")
    protected OTSSimulatorInterface simulator;

    /** The network to register the nodes, links, roads, lanes, and GTUs in. */
    @SuppressWarnings("visibilitymodifier")
    protected OTSRoadNetwork network;

    /** The network to register the drawing information for the network in. */
    @SuppressWarnings("visibilitymodifier")
    protected NetworkAnimation networkAnimation;

    /** All comments encountered in the XML file. */
    List<String> xmlComments = new ArrayList<>();

    /**
     * @param simulator OTSSimulatorInterface; the simulator for creating the animation. Null if no animation needed.
     */
    public XmlNetworkLaneParserOld(final OTSSimulatorInterface simulator)
    {
        this.simulator = simulator;
        LaneTypeTag laneTypeTagNoTraffic = new LaneTypeTag();
        laneTypeTagNoTraffic.name = "NOTRAFFIC";
        this.laneTypeTags.put(laneTypeTagNoTraffic.name, laneTypeTagNoTraffic);
    }

    /**
     * @param simulator OTSSimulatorInterface; the simulator for creating the animation. Null if no animation needed.
     * @param colorer GTUColorer; GTU colorer
     */
    public XmlNetworkLaneParserOld(final OTSSimulatorInterface simulator, final GTUColorer colorer)
    {
        this.simulator = simulator;
        GTUColorerTag.defaultColorer = colorer;
        LaneTypeTag laneTypeTagNoTraffic = new LaneTypeTag();
        laneTypeTagNoTraffic.name = "NOTRAFFIC";
        this.laneTypeTags.put(laneTypeTagNoTraffic.name, laneTypeTagNoTraffic);
    }

    /**
     * @param url URL; the file with the network in the agreed xml-grammar.
     * @param interpretXMLComments boolean; if true; interpret specifically formatted XML comments and modify the network
     *            accordingly
     * @return the network with Nodes, Links, and Lanes.
     * @throws NetworkException in case of parsing problems.
     * @throws SAXException in case of parsing problems.
     * @throws ParserConfigurationException in case of parsing problems.
     * @throws IOException in case of file reading problems.
     * @throws NamingException in case the animation context cannot be found
     * @throws GTUException in case of a problem with creating the LaneBlock (which is a GTU right now)
     * @throws OTSGeometryException when construction of a lane contour or offset design line fails
     * @throws SimRuntimeException when simulator cannot be used to schedule GTU generation
     * @throws ParameterException ...
     * @throws ValueException ...
     */
    @SuppressWarnings("checkstyle:needbraces")
    public final OTSRoadNetwork build(final URL url, final boolean interpretXMLComments)
            throws NetworkException, ParserConfigurationException, SAXException, IOException, NamingException, GTUException,
            OTSGeometryException, SimRuntimeException, ValueException, ParameterException
    {
        return build(url, new OTSRoadNetwork(url.toString(), true), interpretXMLComments);
    }

    /**
     * @param stream InputStream; the input stream with the network in the agreed xml-grammar.
     * @param interpretXMLComments boolean; if true; interpret specifically formatted XML comments and modify the network
     *            accordingly
     * @return the network with Nodes, Links, and Lanes.
     * @throws NetworkException in case of parsing problems.
     * @throws SAXException in case of parsing problems.
     * @throws ParserConfigurationException in case of parsing problems.
     * @throws IOException in case of file reading problems.
     * @throws NamingException in case the animation context cannot be found
     * @throws GTUException in case of a problem with creating the LaneBlock (which is a GTU right now)
     * @throws OTSGeometryException when construction of a lane contour or offset design line fails
     * @throws SimRuntimeException when simulator cannot be used to schedule GTU generation
     * @throws ParameterException ...
     * @throws ValueException ...
     */
    @SuppressWarnings("checkstyle:needbraces")
    public final OTSRoadNetwork build(final InputStream stream, final boolean interpretXMLComments)
            throws NetworkException, ParserConfigurationException, SAXException, IOException, NamingException, GTUException,
            OTSGeometryException, SimRuntimeException, ValueException, ParameterException
    {
        return build(stream, new OTSRoadNetwork(stream.toString(), true), interpretXMLComments);
    }

    /**
     * @param url URL; the file with the network in the agreed xml-grammar.
     * @param otsNetwork OTSRoadNetwork; the network
     * @param interpretXMLComments boolean; if true; interpret specifically formatted XML comments and modify the network
     *            accordingly
     * @return the network with Nodes, Links, and Lanes.
     * @throws NetworkException in case of parsing problems.
     * @throws SAXException in case of parsing problems.
     * @throws ParserConfigurationException in case of parsing problems.
     * @throws IOException in case of file reading problems.
     * @throws NamingException in case the animation context cannot be found
     * @throws GTUException in case of a problem with creating the LaneBlock (which is a GTU right now)
     * @throws OTSGeometryException when construction of a lane contour or offset design line fails
     * @throws SimRuntimeException when simulator cannot be used to schedule GTU generation
     * @throws ParameterException ...
     * @throws ValueException ...
     */
    @SuppressWarnings("checkstyle:needbraces")
    public final OTSRoadNetwork build(final URL url, final OTSRoadNetwork otsNetwork, final boolean interpretXMLComments)
            throws NetworkException, ParserConfigurationException, SAXException, IOException, NamingException, GTUException,
            OTSGeometryException, SimRuntimeException, ValueException, ParameterException
    {
        return build(url.openStream(), otsNetwork, interpretXMLComments);
    }

    /**
     * @param stream InputStream; the input stream with the network in the agreed xml-grammar.
     * @param otsNetwork OTSRoadNetwork; the network
     * @param interpretXMLComments boolean; if true; interpret specifically formatted XML comments and modify the network
     *            accordingly
     * @return the network with Nodes, Links, and Lanes.
     * @throws NetworkException in case of parsing problems.
     * @throws SAXException in case of parsing problems.
     * @throws ParserConfigurationException in case of parsing problems.
     * @throws IOException in case of file reading problems.
     * @throws NamingException in case the animation context cannot be found
     * @throws GTUException in case of a problem with creating the LaneBlock (which is a GTU right now)
     * @throws OTSGeometryException when construction of a lane contour or offset design line fails
     * @throws SimRuntimeException when simulator cannot be used to schedule GTU generation
     * @throws ParameterException ...
     * @throws ValueException ...
     */
    @SuppressWarnings("checkstyle:needbraces")
    public final OTSRoadNetwork build(final InputStream stream, final OTSRoadNetwork otsNetwork, final boolean interpretXMLComments)
            throws NetworkException, ParserConfigurationException, SAXException, IOException, NamingException, GTUException,
            OTSGeometryException, SimRuntimeException, ValueException, ParameterException
    {
        // try
        // {
        // if (url.getFile().length() > 0 && !(new File(url.toURI()).exists()))
        // throw new SAXException("XmlNetworkLaneParser.build: File " + url.getFile() + " does not exist");
        // }
        // catch (URISyntaxException exception)
        // {
        // throw new SAXException("XmlNetworkLaneParser.build: File " + url.getFile() + " is not properly formatted",
        // exception);
        // }
        this.network = otsNetwork;
        this.networkAnimation = new NetworkAnimation(this.network);
        this.xmlComments.clear();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(stream);
        NodeList networkNodeList = document.getDocumentElement().getChildNodes();

        for (int i = 0; i < networkNodeList.getLength(); i++)
        {
            Node node = networkNodeList.item(i);
            if (node instanceof Comment)
            {
                this.xmlComments.add(node.getTextContent());
            }
        }

        if (!document.getDocumentElement().getNodeName().equals("NETWORK"))
            throw new SAXException("XmlNetworkLaneParser.build: XML document does not start with an NETWORK tag, found "
                    + document.getDocumentElement().getNodeName() + " instead");

        // there should be some definitions using DEFINITIONS tags (could be more than one due to include files)
        List<Node> definitionNodes = XMLParser.getNodes(networkNodeList, "DEFINITIONS");

        if (definitionNodes.size() == 0)
            throw new SAXException("XmlNetworkLaneParser.build: XML document does not have a DEFINITIONS tag");

        // make the GTUTypes ALL and NONE to get started
        this.gtuTypes.put("ALL", otsNetwork.getGtuType(GTUType.DEFAULTS.VEHICLE));
        // this.gtuTypes.put("NONE", GTUType.NONE);

        // parse the DEFINITIONS tags
        for (Node definitionNode : definitionNodes)
            GlobalTag.parseGlobal(definitionNode.getChildNodes(), this);
        for (Node definitionNode : definitionNodes)
            GTUTypeTag.parseGTUTypes(definitionNode.getChildNodes(), this);
        for (Node definitionNode : definitionNodes)
            GTUTag.parseGTUs(definitionNode.getChildNodes(), this);
        for (Node definitionNode : definitionNodes)
            GTUMixTag.parseGTUMix(definitionNode.getChildNodes(), this);
        for (Node definitionNode : definitionNodes)
            RoadTypeTag.parseRoadTypes(definitionNode.getChildNodes(), this);
        for (Node definitionNode : definitionNodes)
            LaneTypeTag.parseLaneTypes(definitionNode.getChildNodes(), this);
        for (Node definitionNode : definitionNodes)
            RoadLayoutTag.parseRoadTypes(definitionNode.getChildNodes(), this);

        // parse the NETWORK tag
        NodeTag.parseNodes(networkNodeList, this);
        RouteTag.parseRoutes(networkNodeList, this);
        ShortestRouteTag.parseShortestRoutes(networkNodeList, this);
        RouteMixTag.parseRouteMix(networkNodeList, this);
        ShortestRouteMixTag.parseShortestRouteMix(networkNodeList, this);
        ConnectorTag.parseConnectors(networkNodeList, this);
        LinkTag.parseLinks(networkNodeList, this);

        // process nodes and links to calculate coordinates and positions
        for (LinkTag linkTag : this.linkTags.values())
            Links.calculateNodeAngles(linkTag);
        for (ConnectorTag connectorTag : this.connectorTags.values())
            Links.buildConnector(connectorTag, this, this.simulator);
        for (LinkTag linkTag : this.linkTags.values())
            Links.buildLink(linkTag, this, this.simulator);
        for (LinkTag linkTag : this.linkTags.values())
            Links.applyRoadTypeToLink(linkTag, this, this.simulator);

        // process the routes
        for (RouteTag routeTag : this.routeTags.values())
            routeTag.makeRoute();
        // TODO shortestRoute, routeMix, ShortestRouteMix

        // store the structure information in the network
        makeNetwork();
        if (interpretXMLComments)
        {
            // fixOD(result);
        }

        List<Node> od = XMLParser.getNodes(networkNodeList, "OD");
        if (od.size() == 1)
        {
            Set<TemplateGTUType> templates = new LinkedHashSet<>();
            for (String gtuType : this.gtuTags.keySet())
            {
                GTUTag gtuTag = this.gtuTags.get(gtuType);
                templates.add(new TemplateGTUType(this.gtuTypes.get(gtuType), gtuTag.lengthDist, gtuTag.widthDist,
                        gtuTag.maxSpeedDist));
            }
            GTUCharacteristicsGeneratorOD gtuTypeGenerator = new DefaultGTUCharacteristicsGeneratorOD(templates);
            ODOptions odOptions = new ODOptions().set(ODOptions.GTU_TYPE, gtuTypeGenerator);
            // TODO add chosen model in gtuTypeGenerator
            XmlOdParser xmlOdParser =
                    new XmlOdParser(this.simulator, this.network, new LinkedHashSet<>(this.gtuTypes.values()));
            try
            {
                xmlOdParser.apply(od.get(0), odOptions);
            }
            catch (XmlParserException exception)
            {
                throw new SAXException("Exception while applying OD.", exception);
            }
        }
        else if (od.size() > 1)
        {
            throw new SAXException("XmlNetworkLaneParser.build: XML document contains multiple OD tags");
        }
        return this.network;
    }

    /**
     * Retrieve the OD info from the XML comments and apply it to the network.
     * @param otsNetwork OTSRoadNetwork; the network
     * @throws NetworkException should never happen (of course)
     * @throws OTSGeometryException might happen if a centroid is positioned on top of the entry exit point of a link
     * @throws NamingException on error
     * @throws RemoteException on error
     * @throws ValueException on error
     * @throws SimRuntimeException on error
     * @throws ParameterException on error
     */
    private void fixOD(final OTSRoadNetwork otsNetwork) throws NetworkException, OTSGeometryException, RemoteException,
            NamingException, ValueException, ParameterException, SimRuntimeException
    {
        // Reduce the list to only OD comments and strip the OD header and parse each into a key-value map.
        Map<String, Map<String, String>> odInfo = new LinkedHashMap<>();
        for (String comment : getXMLComments())
        {
            if (comment.startsWith("OD "))
            {
                String odText = comment.substring(3);
                odInfo.put(odText, parseODLine(odText));
            }
        }
        System.out.println("There are " + odInfo.size() + " OD comments");
        // Find the GTUTypes
        List<GTUType> odGTUTypes = new ArrayList<>(this.gtuTypes.values());
        for (GTUType gtuType : odGTUTypes)
        {
            if (otsNetwork.getGtuType(GTUType.DEFAULTS.VEHICLE).equals(gtuType))
            {
                odGTUTypes.remove(gtuType);
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
        Set<org.opentrafficsim.core.network.Node> origins = new LinkedHashSet<>();
        Set<org.opentrafficsim.core.network.Node> destinations = new LinkedHashSet<>();
        Set<String> startTimeStrings = new LinkedHashSet<>();
        Map<String, String> durations = new LinkedHashMap<>();
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
            OTSPoint3D centroidPoint = new OTSPoint3D(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
            org.opentrafficsim.core.network.Node centroidNode = otsNetwork.getNode(centroidName);
            if (null == centroidNode)
            {
                centroidNode = new OTSNode(otsNetwork, centroidName, centroidPoint);
            }
            String linkId = map.get("link");
            Link link = otsNetwork.getLink(linkId);
            if (null == link)
            {
                throw new NetworkException("Cannot find link with id \"" + linkId + "\"");
            }
            OTSRoadNode from = null;
            OTSRoadNode to = null;
            if ("attracts".equals(od))
            {
                destinations.add(centroidNode);
                from = (OTSRoadNode) link.getEndNode();
                to = (OTSRoadNode) centroidNode;

            }
            else if ("generates".equals(od))
            {
                origins.add(centroidNode);
                from = (OTSRoadNode) centroidNode;
                to = (OTSRoadNode) link.getStartNode();
            }
            OTSLine3D designLine = new OTSLine3D(from.getPoint(), to.getPoint());
            String linkName = String.format("connector_from_%s_to_%s", from.getId(), to.getId());
            Link connectorLink = otsNetwork.getLink(linkName);
            if (null == connectorLink)
            {
                System.out.println("Constructing connector link " + linkName);
                connectorLink = new CrossSectionLink(otsNetwork, linkName, from, to,
                        otsNetwork.getLinkType(LinkType.DEFAULTS.CONNECTOR), designLine, this.simulator,
                        LaneKeepingPolicy.KEEPRIGHT);
            }
        }
        if (startTimeStrings.size() > 1)
        {
            throw new NetworkException("Cannot handle multiple start times - yet");
        }
        if (startTimeStrings.size() == 0)
        {
            throw new NetworkException("Missing start time XML comment");
        }
        String startTimeString = startTimeStrings.iterator().next();
        double start = Double.parseDouble(startTimeString);
        start = 0;
        double duration = Double.parseDouble(durations.get(startTimeString));
        TimeVector tv = new TimeVector(new double[] {start, start + duration}, TimeUnit.BASE, StorageType.DENSE);
        // Categorization categorization = new Categorization("AimsunOTSExport", firstGTUType, otherGTUTypes);
        Categorization categorization = new Categorization("AimsunOTSExport", GTUType.class);
        ODMatrix od = new ODMatrix("ODExample", new ArrayList<>(origins), new ArrayList<>(destinations), categorization, tv,
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
            for (GTUType gt : odGTUTypes)
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
            org.opentrafficsim.core.network.Node from = otsNetwork.getNode(map.get("origin"));
            org.opentrafficsim.core.network.Node to = otsNetwork.getNode(map.get("destination"));
            FrequencyVector demand = new FrequencyVector(new double[] {Double.parseDouble(map.get("flow")), 0},
                    FrequencyUnit.PER_HOUR, StorageType.DENSE);
            od.putDemandVector(from, to, category, demand);
            System.out.println(
                    "Adding demand from " + from.getId() + " to " + to.getId() + " category " + category + ": " + demand);
        }
        Set<TemplateGTUType> templates = new LinkedHashSet<>();
        for (GTUType gtuType : odGTUTypes)
        {
            GTUTag gtuTag = this.gtuTags.get(gtuType.getId());
            templates.add(new TemplateGTUType(gtuType, gtuTag.lengthDist, gtuTag.widthDist, gtuTag.maxSpeedDist));
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
        Map<String, String> result = new LinkedHashMap<>();
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
     * Adds routes.
     * @throws NetworkException if items cannot be added to the Network
     */
    private void makeNetwork() throws NetworkException
    {
        for (RouteTag routeTag : this.routeTags.values())
        {
            // TODO Make routes GTU specific. See what to do with GTUType.ALL for routes
            // TODO Automate addition of Routes to network
            this.network.addRoute(this.network.getGtuType(GTUType.DEFAULTS.VEHICLE), routeTag.route);
        }
    }

    /**
     * Obtain an immutable copy of the collected XML comments.
     * @return List&lt;String&gt;; a list of the XML comments
     */
    public ImmutableList<String> getXMLComments()
    {
        return new ImmutableArrayList<>(this.xmlComments, Immutable.COPY);
    }

    /**
     * @return the network animation data belonging to the network that was parsed.
     */
    public final NetworkAnimation getNetworkAnimation()
    {
        return this.networkAnimation;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "XmlNetworkLaneParser [globalTag=" + this.globalTag + ", nodeTags.size=" + this.nodeTags.size()
                + ", linkTags.size=" + this.linkTags.size() + ", gtuTags.size=" + this.gtuTags.size() + ", gtuMixTags.size="
                + this.gtuMixTags.size() + ", routeTags.size=" + this.routeTags.size() + ", routeMixTags.size="
                + this.routeMixTags.size() + ", shortestRouteTagssize.=" + this.shortestRouteTags.size()
                + ", shortestRouteMixTags.size=" + this.shortestRouteMixTags.size() + ", roadTypeTags.size="
                + this.roadTypeTags.size() + ", gtuTypes.size=" + this.gtuTypes.size() + ", laneTypes.size="
                + this.laneTypeTags.size() + "]";
    }

}

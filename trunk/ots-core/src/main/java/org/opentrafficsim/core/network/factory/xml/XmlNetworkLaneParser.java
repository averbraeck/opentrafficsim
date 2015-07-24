package org.opentrafficsim.core.network.factory.xml;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.lane.LaneType;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class XmlNetworkLaneParser
{
    /** the ID class of the Network. */
    @SuppressWarnings("visibilitymodifier")
    protected final Class<?> networkIdClass;

    /** the ID class of the Node. */
    @SuppressWarnings("visibilitymodifier")
    protected final Class<?> nodeIdClass;

    /** the ID class of the Link. */
    @SuppressWarnings("visibilitymodifier")
    protected final Class<?> linkIdClass;

    /** global values from the GLOBAL tag. */
    @SuppressWarnings("visibilitymodifier")
    protected GlobalTag globalTag;

    /** the processed nodes for further reference. */
    @SuppressWarnings({"rawtypes", "visibilitymodifier"})
    protected Map<String, Node> nodes = new HashMap<>();

    /** the UNprocessed nodes for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, NodeTag> nodeTags = new HashMap<>();

    /** the links for further reference. */
    @SuppressWarnings({"rawtypes", "visibilitymodifier"})
    protected Map<String, Link> links = new HashMap<>();

    /** the UNprocessed links for further reference. */
    // @SuppressWarnings("visibilitymodifier")
    // protected Map<String, LinkTag> linkTags = new HashMap<>();
    
     /** the gtu tags for further reference. */
     @SuppressWarnings("visibilitymodifier")
     protected Map<String, GTUTag> gtuTags = new HashMap<>();
    
     /** the gtumix tags for further reference. */
     @SuppressWarnings("visibilitymodifier")
     protected Map<String, GTUMixTag> gtuMixTags = new HashMap<>();
    
    // /** the route tags for further reference. */
    // @SuppressWarnings("visibilitymodifier")
    // protected Map<String, RouteTag> routeTags = new HashMap<>();
    //
    // /** the route mix tags for further reference. */
    // @SuppressWarnings("visibilitymodifier")
    // protected Map<String, RouteMixTag> routeMixTags = new HashMap<>();
    //
    // /** the shortest route tags for further reference. */
    // @SuppressWarnings("visibilitymodifier")
    // protected Map<String, ShortestRouteTag> shortestRouteTags = new HashMap<>();
    //
    // /** the shortest route mix tags for further reference. */
    // @SuppressWarnings("visibilitymodifier")
    // protected Map<String, ShortestRouteMixTag> shortestRouteMixTags = new HashMap<>();
    
     /** the road type tags for further reference. */
     @SuppressWarnings("visibilitymodifier")
     protected Map<String, RoadTypeTag> roadTypeTags = new HashMap<>();

    /** the GTUTypes that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, GTUType<String>> gtuTypes = new HashMap<>();

    /** the LaneTypes that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, LaneType<String>> laneTypes = new HashMap<>();

    /** the no traffic LaneType. */
    @SuppressWarnings("visibilitymodifier")
    protected static LaneType<String> noTrafficLaneType = new LaneType<>("NOTRAFFIC");

    /** the simulator for creating the animation. Null if no animation needed. */
    @SuppressWarnings("visibilitymodifier")
    protected OTSDEVSSimulatorInterface simulator;

    /**
     * @param networkIdClass the ID class of the Network.
     * @param nodeIdClass the ID class of the Node.
     * @param linkIdClass the ID class of the Link.
     * @param simulator the simulator for creating the animation. Null if no animation needed.
     */
    public XmlNetworkLaneParser(final Class<?> networkIdClass, final Class<?> nodeIdClass, final Class<?> linkIdClass,
        final OTSDEVSSimulatorInterface simulator)
    {
        this.networkIdClass = networkIdClass;
        this.nodeIdClass = nodeIdClass;
        this.linkIdClass = linkIdClass;
        this.simulator = simulator;
        this.laneTypes.put(noTrafficLaneType.getId(), noTrafficLaneType);
    }

    /**
     * @param url the file with the network in the agreed xml-grammar.
     * @return the network with Nodes, Links, and Lanes.
     * @throws NetworkException in case of parsing problems.
     * @throws SAXException in case of parsing problems.
     * @throws ParserConfigurationException in case of parsing problems.
     * @throws IOException in case of file reading problems.
     */
    @SuppressWarnings("rawtypes")
    public final OTSNetwork build(final URL url) throws NetworkException, ParserConfigurationException, SAXException,
        IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(url.openStream());
        NodeList nodeList = document.getDocumentElement().getChildNodes();

        // handle the INCLUDE tags first in a recursive manner
        parseIncludes(nodeList);

        // parse the other tags
        GlobalTag.parseGlobal(nodeList, this);
        GTUTag.parseGTUs(nodeList, this);
        GTUMixTag.parseGTUMix(nodeList, this);
        LaneTypeTag.parseCompatibilities(nodeList, this);
        RoadTypeTag.parseRoadTypes(nodeList, this);
        NodeTag.parseNodes(nodeList, this);
        // parseLinks();
        // parseRoutes();

        // process the information for which multiple tags have to be combined
        // processLanes();
        // processFill();
        // processGenerators();
        // processSensors();

        // store the structure information in the network
        return makeNetwork(url.toString());
    }

    /**
     * Parse the INCLUDE node and transfer the tags from the include to this XmlParser.
     * @param nodeList the top-level nodes of the XML-file
     * @throws SAXException when parsing of INCLUDE tag fails
     */
    private void parseIncludes(final NodeList nodeList) throws SAXException
    {
        try
        {
            for (org.w3c.dom.Node node : XMLParser.getNodes(nodeList, "INCLUDE"))
            {
                NamedNodeMap attributes = node.getAttributes();
                String name = attributes.getNamedItem("FILE").getTextContent();
                URI includeURI = new URI(name);
                XmlNetworkLaneParser includeParser =
                    new XmlNetworkLaneParser(this.networkIdClass, this.nodeIdClass, this.linkIdClass, this.simulator);
                includeParser.build(includeURI.toURL());

                this.gtuTypes.putAll(includeParser.gtuTypes);
                this.gtuTags.putAll(includeParser.gtuTags);
                this.gtuMixTags.putAll(includeParser.gtuMixTags);
                this.links.putAll(includeParser.links);
                this.laneTypes.putAll(includeParser.laneTypes);
                this.nodes.putAll(includeParser.nodes);
                this.roadTypeTags.putAll(includeParser.roadTypeTags);
                this.nodeTags.putAll(includeParser.nodeTags);
                // this.routeMixTags.putAll(includeParser.routeMixTags);
                // this.routeTags.putAll(includeParser.routeTags);
                // this.shortestRouteMixTags.putAll(includeParser.shortestRouteMixTags);
                // this.shortestRouteTags.putAll(includeParser.shortestRouteTags);
            }
        }
        catch (NetworkException | SAXException | IOException | ParserConfigurationException | URISyntaxException exception)
        {
            throw new SAXException(exception);
        }
    }

    /**
     * @param name the name of the network
     * @return the OTSNetwork with the static information about the network
     * @throws NetworkException if items cannot be added to the Network
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private OTSNetwork makeNetwork(final String name) throws NetworkException
    {
        OTSNetwork network = new OTSNetwork(name);
        for (Node node : this.nodes.values())
        {
            network.addNode(node);
        }
        for (Link link : this.links.values())
        {
            network.addLink(link);
        }
        // TODO Routes
        return network;
    }

}

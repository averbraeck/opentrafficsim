package org.opentrafficsim.road.network.factory.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.factory.XMLParser;
import org.opentrafficsim.road.network.lane.LaneType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class XmlNetworkLaneParser
{
    /** global values from the GLOBAL tag. */
    @SuppressWarnings("visibilitymodifier")
    protected GlobalTag globalTag;

    /** the UNprocessed nodes for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, NodeTag> nodeTags = new HashMap<>();

    /** the UNprocessed links for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, LinkTag> linkTags = new HashMap<>();

    /** the gtu tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, GTUTag> gtuTags = new HashMap<>();

    /** the gtumix tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, GTUMixTag> gtuMixTags = new HashMap<>();

    /** the route tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RouteTag> routeTags = new HashMap<>();

    /** the route mix tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RouteMixTag> routeMixTags = new HashMap<>();

    /** the shortest route tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, ShortestRouteTag> shortestRouteTags = new HashMap<>();

    /** the shortest route mix tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, ShortestRouteMixTag> shortestRouteMixTags = new HashMap<>();

    /** the road type tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RoadTypeTag> roadTypeTags = new HashMap<>();

    /** the GTUTypes that have been created. public to make it accessible from LaneAttributes. */
    @SuppressWarnings("visibilitymodifier")
    public Map<String, GTUType> gtuTypes = new HashMap<>();

    /** the LaneTypes that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, LaneType> laneTypes = new HashMap<>();

    /** the no traffic LaneType. */
    @SuppressWarnings("visibilitymodifier")
    protected static LaneType noTrafficLaneType = new LaneType("NOTRAFFIC");

    /** the simulator for creating the animation. Null if no animation needed. */
    @SuppressWarnings("visibilitymodifier")
    protected OTSDEVSSimulatorInterface simulator;

    /** the network to register the GTUs in. */
    @SuppressWarnings("visibilitymodifier")
    protected OTSNetwork network;

    /**
     * @param simulator the simulator for creating the animation. Null if no animation needed.
     */
    public XmlNetworkLaneParser(final OTSDEVSSimulatorInterface simulator)
    {
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
     * @throws NamingException in case the animation context cannot be found
     * @throws GTUException in case of a problem with creating the LaneBlock (which is a GTU right now)
     * @throws OTSGeometryException when construction of a lane contour or offset design line fails
     * @throws SimRuntimeException when simulator cannot be used to schedule GTU generation
     */
    @SuppressWarnings("checkstyle:needbraces")
    public final OTSNetwork build(final URL url) throws NetworkException, ParserConfigurationException, SAXException,
        IOException, NamingException, GTUException, OTSGeometryException, SimRuntimeException
    {
        if (url.getFile().length() > 0 && !(new File(url.getFile()).exists()))
            throw new SAXException("XmlNetworkLaneParser.build: File url.getFile() does not exist");
        this.network = new OTSNetwork(url.toString());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(url.openStream());
        NodeList networkNodeList = document.getDocumentElement().getChildNodes();

        if (!document.getDocumentElement().getNodeName().equals("NETWORK"))
            throw new SAXException(
                "XmlNetworkLaneParser.build: XML document does not start with an NETWORK tag, found "
                    + document.getDocumentElement().getNodeName() + " instead");

        // there should be some definitions using DEFINITIONS tags (could be more than one due to include files)
        List<Node> definitionNodes = XMLParser.getNodes(networkNodeList, "DEFINITIONS");

        if (definitionNodes.size() == 0)
            throw new SAXException("XmlNetworkLaneParser.build: XML document does not have a DEFINITIONS tag");

        // make the GTUTypes ALL and NONE to get started
        this.gtuTypes.put("ALL", GTUType.ALL);
        this.gtuTypes.put("NONE", GTUType.NONE);

        // parse the DEFINITIONS tags
        for (Node definitionNode : definitionNodes)
            GlobalTag.parseGlobal(definitionNode.getChildNodes(), this);
        for (Node definitionNode : definitionNodes)
            GTUTag.parseGTUs(definitionNode.getChildNodes(), this);
        for (Node definitionNode : definitionNodes)
            GTUMixTag.parseGTUMix(definitionNode.getChildNodes(), this);
        for (Node definitionNode : definitionNodes)
            LaneTypeTag.parseLaneTypes(definitionNode.getChildNodes(), this);
        for (Node definitionNode : definitionNodes)
            CompatibilityTag.parseCompatibilities(definitionNode.getChildNodes(), this);
        for (Node definitionNode : definitionNodes)
            RoadTypeTag.parseRoadTypes(definitionNode.getChildNodes(), this);

        // parse the NETWORK tag
        NodeTag.parseNodes(networkNodeList, this);
        RouteTag.parseRoutes(networkNodeList, this);
        ShortestRouteTag.parseShortestRoutes(networkNodeList, this);
        RouteMixTag.parseRouteMix(networkNodeList, this);
        ShortestRouteMixTag.parseShortestRouteMix(networkNodeList, this);
        LinkTag.parseLinks(networkNodeList, this);

        // process nodes and links to calculate coordinates and positions
        Links.calculateNodeCoordinates(this);
        for (LinkTag linkTag : this.linkTags.values())
            Links.buildLink(linkTag, this, this.simulator);
        for (LinkTag linkTag : this.linkTags.values())
            Links.applyRoadTypeToLink(linkTag, this, this.simulator);

        // process the routes
        for (RouteTag routeTag : this.routeTags.values())
            routeTag.makeRoute();
        // TODO shortestRoute, routeMix, ShortestRouteMix

        // store the structure information in the network
        return makeNetwork();
    }

    /**
     * @return the OTSNetwork with the static information about the network
     * @throws NetworkException if items cannot be added to the Network
     */
    private OTSNetwork makeNetwork() throws NetworkException
    {
        for (NodeTag nodeTag : this.nodeTags.values())
        {
            this.network.addNode(nodeTag.node);
        }
        for (LinkTag linkTag : this.linkTags.values())
        {
            this.network.addLink(linkTag.link);
        }
        for (RouteTag routeTag : this.routeTags.values())
        {
            // TODO Make routes GTU specific. See what to do with GTUType.ALL for routes
            this.network.addRoute(GTUType.ALL, routeTag.route);
        }
        return this.network;
    }

}

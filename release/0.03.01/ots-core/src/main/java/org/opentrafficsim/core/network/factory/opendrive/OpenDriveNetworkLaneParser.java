package org.opentrafficsim.core.network.factory.opendrive;

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
import org.opentrafficsim.core.network.factory.XMLParser;
import org.opentrafficsim.core.network.lane.LaneType;
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
public class OpenDriveNetworkLaneParser
{
    /** Junction tags. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, JunctionTag> junctionTags = new HashMap<>();

    /** Road tags. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RoadTag> roadTags = new HashMap<>();

    /** the GTUTypes that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, GTUType> gtuTypes = new HashMap<>();

    /** the LaneTypes that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, LaneType> laneTypes = new HashMap<>();

    /** the no traffic LaneType. */
    @SuppressWarnings("visibilitymodifier")
    protected static LaneType noTrafficLaneType = new LaneType("NOTRAFFIC");

    /** the simulator for creating the animation. Null if no animation needed. */
    @SuppressWarnings("visibilitymodifier")
    protected OTSDEVSSimulatorInterface simulator;

    /**
     * @param simulator the simulator for creating the animation. Null if no animation needed.
     */
    public OpenDriveNetworkLaneParser(final OTSDEVSSimulatorInterface simulator)
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
    @SuppressWarnings({"rawtypes", "checkstyle:needbraces"})
    public final OTSNetwork build(final URL url) throws NetworkException, ParserConfigurationException, SAXException,
        IOException, NamingException, GTUException, OTSGeometryException, SimRuntimeException
    {
        if (url.getFile().length() > 0 && !(new File(url.getFile()).exists()))
            throw new SAXException("OpenDriveNetworkLaneParser.build: File url.getFile() does not exist");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(url.openStream());
        NodeList networkNodeList = document.getDocumentElement().getChildNodes();

        if (!document.getDocumentElement().getNodeName().equals("OpenDRIVE"))
            throw new SAXException(
                "OpenDriveNetworkLaneParser.build: XML document does not start with an OpenDRIVE tag, found "
                    + document.getDocumentElement().getNodeName() + " instead");

        // there should be a header tag
        List<Node> headerNodes = XMLParser.getNodes(networkNodeList, "header");
        if (headerNodes.size() != 1)
            throw new SAXException("OpenDriveNetworkLaneParser.build: XML document does not have a header tag");
        // TODO parse header, geoReference and check version number of OpenDrive

        // parse the junction tags
        List<Node> junctionNodes = XMLParser.getNodes(networkNodeList, "junction");
        for (Node junctionNode : junctionNodes)
            JunctionTag.parseJunction(junctionNode, this);

        // parse the road tags
        List<Node> roadNodes = XMLParser.getNodes(networkNodeList, "road");
        if (roadNodes.size() == 0)
            throw new SAXException("OpenDriveNetworkLaneParser.build: XML document does not have a road tag");
        for (Node roadNode : roadNodes)
        {
            RoadTag roadTag = RoadTag.parseRoad(roadNode, this);
            LinkTag.parseLink(roadNode.getChildNodes(), this, roadTag);
            TypeTag.parseType(roadNode.getChildNodes(), this, roadTag);
            /*-
            PlanViewTag.parsePlanView(roadNode.getChildNodes(), this, roadTag);
            ElevationProfileTag.parseElevationProfile(roadNode.getChildNodes(), this, roadTag);
            LateralProfileTag.parseLateralProfile(roadNode.getChildNodes(), this, roadTag);
            LanesTag.parseLanes(roadNode.getChildNodes(), this, roadTag);
            ObjectsTag.parseObjects(roadNode.getChildNodes(), this, roadTag);
            SignalsTag.parseSignals(roadNode.getChildNodes(), this, roadTag);
            SurfaceTag.parseSurface(roadNode.getChildNodes(), this, roadTag);
            RailroadTag.parseRailroad(roadNode.getChildNodes(), this, roadTag);
             */
        }

        // store the structure information in the network
        return makeNetwork(url.toString());
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
        /*-
        for (NodeTag nodeTag : this.nodeTags.values())
        {
            network.addNode(nodeTag.node);
        }
        for (LinkTag linkTag : this.linkTags.values())
        {
            network.addLink(linkTag.link);
        }
        for (RouteTag routeTag : this.routeTags.values())
        {
            network.addRoute(routeTag.route);
        }
         */
        return network;
    }

}
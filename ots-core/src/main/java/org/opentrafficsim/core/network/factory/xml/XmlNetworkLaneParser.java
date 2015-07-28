package org.opentrafficsim.core.network.factory.xml;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
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
import org.opentrafficsim.core.network.lane.LaneType;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @throws SimRuntimeException when simulator canot be used to schedule GTU generation
     */
    @SuppressWarnings("rawtypes")
    public final OTSNetwork build(final URL url) throws NetworkException, ParserConfigurationException, SAXException,
        IOException, NamingException, GTUException, OTSGeometryException, SimRuntimeException
    {
        if (url.getFile().length() > 0 && !(new File(url.getFile()).exists()))
        {
            throw new SAXException("XmlNetworkLaneParser.build: File url.getFile() does not exist");
        }
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
        RouteTag.parseRoutes(nodeList, this);
        ShortestRouteTag.parseShortestRoutes(nodeList, this);
        RouteMixTag.parseRouteMix(nodeList, this);
        ShortestRouteMixTag.parseShortestRouteMix(nodeList, this);
        LinkTag.parseLinks(nodeList, this);

        // process nodes and links to calculate coordinates and positions
        Links.calculateNodeCoordinates(this);
        for (LinkTag linkTag : this.linkTags.values())
        {
            Links.buildLink(linkTag, this, this.simulator);
            Links.applyRoadTypeToLink(linkTag, this, this.simulator);
        }
        
        // process the information for which multiple tags have to be combined
        for (LinkTag linkTag : this.linkTags.values())
        {
            GeneratorTag.makeGenerators(linkTag, this, this.simulator);
            // processFill();
            // processListGenerators();
            // processSensors();
        }

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
                XmlNetworkLaneParser includeParser = new XmlNetworkLaneParser(this.simulator);
                includeParser.build(includeURI.toURL());

                this.gtuTypes.putAll(includeParser.gtuTypes);
                this.gtuTags.putAll(includeParser.gtuTags);
                this.gtuMixTags.putAll(includeParser.gtuMixTags);
                this.laneTypes.putAll(includeParser.laneTypes);
                this.roadTypeTags.putAll(includeParser.roadTypeTags);
                this.nodeTags.putAll(includeParser.nodeTags);
                this.linkTags.putAll(includeParser.linkTags);
                this.routeMixTags.putAll(includeParser.routeMixTags);
                this.routeTags.putAll(includeParser.routeTags);
                this.shortestRouteMixTags.putAll(includeParser.shortestRouteMixTags);
                this.shortestRouteTags.putAll(includeParser.shortestRouteTags);
            }
        }
        catch (NetworkException | SAXException | IOException | ParserConfigurationException | URISyntaxException
            | NamingException | GTUException | OTSGeometryException | SimRuntimeException exception)
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
        for (NodeTag nodeTag : this.nodeTags.values())
        {
            network.addNode(nodeTag.node);
        }
        for (LinkTag linkTag : this.linkTags.values())
        {
            network.addLink(linkTag.link);
        }
        // TODO Routes
        return network;
    }

}

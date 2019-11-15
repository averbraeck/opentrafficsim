package org.opentrafficsim.road.network.factory.vissim;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.locationtech.jts.geom.LineString;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.LaneType;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class VissimNetworkLaneParser implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Global values from the GLOBAL tag. */
    private GlobalTag globalTag;

    /** The UNprocessed links for further reference. */
    private Map<String, NodeTag> nodeTags = new LinkedHashMap<>();

    /** The UNprocessed links for further reference. */
    private Map<String, LinkTag> linkTags = new LinkedHashMap<>();

    /** The UNprocessed links for further reference. */
    private Map<String, LinkTag> connectorTags = new LinkedHashMap<>();

    /** The UNprocessed links for further reference. */
    private Map<String, LinkTag> realLinkTags = new LinkedHashMap<>();

    /** The UNprocessed links for further reference. */
    private Map<String, SignalHeadTag> signalHeadTags = new LinkedHashMap<>();

    /** The UNprocessed links for further reference. */
    private Map<String, SensorTag> sensorTags = new LinkedHashMap<>();

    /** The gtu tags for further reference. */
    private Map<String, GTUTag> gtuTags = new LinkedHashMap<>();

    /** The gtumix tags for further reference. */
    private Map<String, GTUMixTag> gtuMixTags = new LinkedHashMap<>();

    /** The road type tags for further reference. */
    private Map<String, RoadTypeTag> roadTypeTags = new LinkedHashMap<>();

    /** The GTUTypes that have been created. public to make it accessible from LaneAttributes. */
    private Map<String, GTUType> gtuTypes = new LinkedHashMap<>();

    /** The LaneType tags that have been created. */
    private Map<String, LaneTypeTag> laneTypeTags = new LinkedHashMap<>();

    /** The LaneType tags that have been created. */
    private Map<String, RoadLayoutTag> roadLayoutTags = new LinkedHashMap<>();

    /** The RouteMix tags that have been created. */
    private Map<String, RouteMixTag> routeMixTags = new LinkedHashMap<>();

    /** The RouteMix tags that have been created. */
    private Map<String, ShortestRouteMixTag> shortestRouteMixTags = new LinkedHashMap<>();

    /** The RouteMix tags that have been created. */
    private Map<String, ShortestRouteTag> shortestRouteTags = new LinkedHashMap<>();

    /** The RouteMix tags that have been created. */
    private Map<String, RouteTag> routeTags = new LinkedHashMap<>();

    /** The LaneTypes that have been created. */
    private Map<String, LaneType> laneTypes = new LinkedHashMap<>();

    /** The simulator for creating the animation. Null if no animation needed. */
    private OTSSimulatorInterface simulator;

    /** The network to register the GTUs in. */
    @SuppressWarnings("visibilitymodifier")
    protected OTSRoadNetwork network;

    /*****
     * Variables, typically for Vissim network import
     */

    /** the node number is automatically generated and increases with every additional node. */
    private int upperNodeNr = 1;

    /** the node number is automatically generated and increases with every additional node. */
    private int upperLinkNr = 1;

    /**
     * @param simulator OTSSimulatorInterface; the simulator for creating the animation. Null if no animation needed.
     */
    public VissimNetworkLaneParser(final OTSSimulatorInterface simulator)
    {
        this.simulator = simulator;
    }

    /**
     * @param inputUrl URL; input
     * @param outputFile File; output file
     * @param network OTSRoadNetwork; network
     * @param sinkKillClassName String; name of the sink-sensor class
     * @param sensorClassName String; name of the sensor class
     * @param trafficLightName String; name of the trafficLight class
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
    public OTSRoadNetwork build(final URL inputUrl, final File outputFile, final OTSRoadNetwork network,
            final String sinkKillClassName, final String sensorClassName, final String trafficLightName)
            throws NetworkException, ParserConfigurationException, SAXException, IOException, NamingException, GTUException,
            OTSGeometryException, SimRuntimeException
    {
        if (inputUrl.getFile().length() > 0 && !(new File(inputUrl.getFile()).exists()))
        {
            throw new SAXException("XmlNetworkLaneParser.build: File url.getFile() does not exist");
        }

        // starts with the creation of an empty network
        this.network = network;

        // prepare for reading the XML document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputUrl.openStream());
        NodeList networkXMLNodeList = document.getDocumentElement().getChildNodes();

        // Is it a Vissim network?
        if (!document.getDocumentElement().getNodeName().equals("network"))
        {
            throw new SAXException("XmlNetworkLaneParser.build: XML document does not start with an NETWORK tag, found "
                    + document.getDocumentElement().getNodeName() + " instead");
        }

        // make the GTUTypes ALL and NONE to get started
        this.gtuTypes.put("ALL", network.getGtuType(GTUType.DEFAULTS.VEHICLE));
        // this.gtuTypes.put("NONE", GTUType.NONE);

        // Read the link (and connector) tags and ...
        // Construct the nodes as Vissim does not use Nodes!!
        LinkTag.parseLinks(networkXMLNodeList, this);

        // the signal heads are stored separately. Read them first, connect to links later on
        SignalHeadTag.parseSignalHead(networkXMLNodeList, this);

        // the sensor are defined separate also. Read them first, connect to links later on
        SensorTag.parseSensor(networkXMLNodeList, this);

        // add the signalHeads to the links
        LinkTag.addSignalHeads(this);

        // add the detectors to the links
        LinkTag.addDetectors(this);

        // create a subset of connector links
        for (LinkTag linkTag : this.linkTags.values())
        {
            if (linkTag.connector)
            {
                this.connectorTags.put(linkTag.name, linkTag);
            }
            else
            {
                this.realLinkTags.put(linkTag.name, linkTag);
            }
        }

        // removes all duplicate nodes, both in Link and Node lists
        NodeTag.removeDuplicateNodes(this);
        // Split links where appropriate
        // Step 1: split links, where a connector intersects a link
        // loops through all connectors
        // a link gets split if the connector is more than a "margin" number of meters from the start or end of a link
        Double margin = 5.0;
        splitLinksIntersectedByConnector(margin);

        // create space between connectors and links, in order to make bezier curved links between connectors and links
        // action: the connectors become shorter than they were
        // LinkTag.shortenConnectors(this);

        // Create links to connect the shortened connectors and links by means of bezier curved links
        createLinkBetweenConnectorAndLink(this);

        // Step 2: split links directly behind a signal head, assuming such a link enters a signalized junction
        //
        Double splitMetersAfterSignalHead = 1.5;
        margin = 4.0;
        splitLinkAtSignalAndDetector(this.linkTags, margin, splitMetersAfterSignalHead);

        // remove double links with the same start and end node
        LinkedHashMap<String, LinkTag> removeConnectorTags = new LinkedHashMap<>();
        removeDoubleConnectors(removeConnectorTags);

        // build links
        for (LinkTag linkTag : this.linkTags.values())
        {
            Links.buildLink(linkTag, this, this.simulator);
        }

        // Generate the real links
        for (LinkTag realLinkTag : this.linkTags.values())
        {
            Links.applyRoadTypeToLink(realLinkTag, this, this.simulator);
        }

        // Generate sink/kill sensors at the links that have no further connections (dead-walking)
        for (LinkTag realLinkTag : this.linkTags.values())
        {
            Links.createSinkSensor(realLinkTag, this, this.simulator);
        }

        // generate the connector links
        for (LinkTag connectorTag : this.linkTags.values())
        {
            Links.applyRoadTypeToConnector(connectorTag, this, this.simulator);
        }

        NodeTag.removeRedundantNodeTags(this);

        // process the routes
        // for (RouteTag routeTag : this.routeTags.values())
        // routeTag.makeRoute();
        // TODO shortestRoute, routeMix, ShortestRouteMix

        // store the structure information in the network
        XMLNetworkWriter.writeToXML(outputFile, linkTags, nodeTags, sinkKillClassName, sensorClassName, trafficLightName);

        return makeNetwork();
    }

    private void removeDoubleConnectors(HashMap<String, LinkTag> removeConnectorTags)
    {
        for (LinkTag linkTag : this.connectorTags.values())
        {
            for (LinkTag linkTag2 : this.connectorTags.values())
            {
                if (linkTag.nodeStartTag.name.equals(linkTag2.nodeStartTag.name)
                        && linkTag.nodeEndTag.name.equals(linkTag2.nodeEndTag.name) && !linkTag.equals(linkTag2))
                {
                    removeConnectorTags.put(linkTag.nodeStartTag.name + linkTag.nodeEndTag.name, linkTag);
                }
            }
        }
        for (Entry<String, LinkTag> entry : removeConnectorTags.entrySet())
        {
            connectorTags.remove(entry.getValue().name);
            linkTags.remove(entry.getValue().name);
        }
    }

    /**
     * @param parser VissimNetworkLaneParser;
     * @throws OTSGeometryException
     */
    private void createLinkBetweenConnectorAndLink(final VissimNetworkLaneParser parser) throws OTSGeometryException
    {
        // loop through all connector links
        for (LinkTag connectorLinkTag : this.connectorTags.values())
        {

            OTSLine3D designLineOTS = LinkTag.createLineString(connectorLinkTag);
            LineString designLine = designLineOTS.getLineString();
            Double length = designLine.getLength();
            // default: we replace the connector link by a bezier curved link
            if (length < 999995)
            {
                connectorLinkTag.nodeStartTag = parser.nodeTags.get(connectorLinkTag.connectorTag.fromNodeName);
                connectorLinkTag.nodeEndTag = parser.nodeTags.get(connectorLinkTag.connectorTag.toNodeName);
                connectorLinkTag.bezierTag = new BezierTag();
                if (connectorLinkTag.nodeStartTag.name.equals(connectorLinkTag.nodeEndTag.name))
                {
                    this.linkTags.remove(connectorLinkTag.name);
                    // this.connectorTags.remove(connectorLinkTag.name);
                }
                if (connectorLinkTag.polyLineTag != null)
                {
                    connectorLinkTag.polyLineTag = null;
                }
                if (connectorLinkTag.straightTag != null)
                {
                    connectorLinkTag.straightTag = null;
                }
            }
            // if the link is quite long....we could split it but we don't do this now
            // so the next block is inactive!!!
            else
            {
                // get the from link
                LinkTag pasteLinkFromTag = new LinkTag(connectorLinkTag);
                // add a unique name
                String linkName = "" + parser.upperLinkNr;
                parser.upperLinkNr++;
                pasteLinkFromTag.name = linkName;
                pasteLinkFromTag.nodeStartTag = parser.nodeTags.get(connectorLinkTag.connectorTag.fromNodeName);
                pasteLinkFromTag.nodeEndTag = connectorLinkTag.nodeStartTag;
                pasteLinkFromTag.bezierTag = new BezierTag();
                if (pasteLinkFromTag.polyLineTag != null)
                {
                    pasteLinkFromTag.polyLineTag = null;
                }
                if (pasteLinkFromTag.straightTag != null)
                {
                    pasteLinkFromTag.straightTag = null;
                }
                // this.linkTags.put(pasteLinkFromTag.name, pasteLinkFromTag);

                // get the To link
                LinkTag pasteLinkToTag = new LinkTag(connectorLinkTag);
                linkName = "" + parser.upperLinkNr;
                parser.upperLinkNr++;
                pasteLinkToTag.name = linkName;
                pasteLinkToTag.nodeStartTag = connectorLinkTag.nodeEndTag;
                pasteLinkToTag.nodeEndTag = parser.nodeTags.get(connectorLinkTag.connectorTag.toNodeName);

                pasteLinkToTag.bezierTag = new BezierTag();
                if (pasteLinkToTag.polyLineTag != null)
                {
                    pasteLinkToTag.polyLineTag = null;
                }
                if (pasteLinkToTag.straightTag != null)
                {
                    pasteLinkToTag.straightTag = null;
                }
                // this.linkTags.put(pasteLinkToTag.name, pasteLinkToTag);

            }
            // }
        }
    }

    private void splitLinkAtSignalAndDetector(Map<String, LinkTag> inputLinkTags, Double margin,
            Double splitMetersAfterSignalHead) throws OTSGeometryException, NetworkException
    {

        Map<String, LinkTag> newLinkTags = new LinkedHashMap<>();
        // Loops through all links
        splitLinksAtSignal(margin, splitMetersAfterSignalHead, inputLinkTags, newLinkTags);

    }

    private void splitLinksAtSignal(Double margin, Double splitMetersAfterSignalHead, Map<String, LinkTag> inputLinkTags,
            Map<String, LinkTag> newLinkTags) throws OTSGeometryException, NetworkException
    {
        for (LinkTag linkTag : inputLinkTags.values())
        {
            for (SignalHeadTag signalHeadTag : linkTag.signalHeads)
            {
                Double position = Double.parseDouble(signalHeadTag.positionStr);
                Double splitPosition = position + splitMetersAfterSignalHead;
                // A new node has to be created
                NodeTag newNodeTag = NodeTag.createNewNodeAtLinkPosition(linkTag, this, splitPosition);
                // split the link and connect the new node after the position of the signalhead\
                Map<String, LinkTag> newLinks = LinkTag.splitLink(newNodeTag, linkTag, this, splitPosition, margin, false);
                if (newLinks != null)
                {
                    newLinkTags.putAll(newLinks);
                }
            }
            linkTag.signalHeads.removeAll(linkTag.signalHeadsToRemove);
            linkTag.sensors.removeAll(linkTag.sensorTagsToRemove);
            // relocate the signalHeads and detectors afterwards!
        }
        if (!newLinkTags.isEmpty())
        {
            this.linkTags.putAll(newLinkTags);
        }

        // run again for the newly created linkTags
        Map<String, LinkTag> new2LinkTags = new LinkedHashMap<>();
        if (newLinkTags.size() > 0)
        {
            splitLinksAtSignal(margin, splitMetersAfterSignalHead, newLinkTags, new2LinkTags);
        }
    }

    private void splitLinksIntersectedByConnector(Double margin) throws OTSGeometryException, NetworkException
    {
        // loop through all connector links
        for (LinkTag connectorLinkTag : this.connectorTags.values())
        {
            // ***********************
            // 1: connector meets link:
            // get the position where this connector intersects the Link. Here the link will be split
            Double position = Double.parseDouble(connectorLinkTag.connectorTag.toPositionStr);

            // get the meeting link
            LinkTag linkToTag = this.realLinkTags.get(connectorLinkTag.connectorTag.toLinkNo);
            boolean isConnectorToLink = true;

            // create a new node at the split position of the "meeting" Link
            NodeTag newSplitNodeTag = NodeTag.createNewNodeAtLinkPosition(linkToTag, this, position);

            // split the link
            Map<String, LinkTag> newLinkToTags =
                    LinkTag.splitLink(newSplitNodeTag, linkToTag, this, position, margin, isConnectorToLink);
            linkToTag.signalHeads.removeAll(linkToTag.signalHeadsToRemove);
            linkToTag.sensors.removeAll(linkToTag.sensorTagsToRemove);

            if (newLinkToTags != null)
            {
                // only add if a link is split
                connectorLinkTag.connectorTag.toNodeName = newSplitNodeTag.name;
                this.nodeTags.put(newSplitNodeTag.name, newSplitNodeTag);
                // adjust the connection of other Connector links
                for (LinkTag connectorLinkTag2 : this.connectorTags.values())
                {

                    if (connectorLinkTag2 != connectorLinkTag
                            && connectorLinkTag2.connectorTag.toLinkNo.equals(connectorLinkTag.connectorTag.toLinkNo))
                    {

                        Double position2 = Double.parseDouble(connectorLinkTag2.connectorTag.toPositionStr);
                        if (position2 > position)
                        {
                            connectorLinkTag2.connectorTag.toLinkNo = newLinkToTags.values().iterator().next().name;
                            Double newPosition = position2 - position;
                            connectorLinkTag2.connectorTag.toPositionStr = newPosition.toString();
                        }
                    }
                    if (connectorLinkTag2 != connectorLinkTag
                            && connectorLinkTag2.connectorTag.fromLinkNo.equals(connectorLinkTag.connectorTag.toLinkNo))
                    {

                        Double position2 = Double.parseDouble(connectorLinkTag2.connectorTag.fromPositionStr);
                        if (position2 > position)
                        {
                            connectorLinkTag2.connectorTag.fromLinkNo = newLinkToTags.values().iterator().next().name;
                            Double newPosition = position2 - position;
                            connectorLinkTag2.connectorTag.fromPositionStr = newPosition.toString();
                        }
                    }
                }
                // connectorTag.connectorTag.toLinkNo = newLinkToTags.values().iterator().next().name;
                this.linkTags.putAll(newLinkToTags);
                this.realLinkTags.putAll(newLinkToTags);
            }
            else
            {

                // from connector to next link
                if (position < margin)
                {
                    this.nodeTags.remove(connectorLinkTag.connectorTag.toNodeName);
                    connectorLinkTag.connectorTag.toNodeName = linkToTag.nodeStartTag.name;
                }
                // from link to connector
                else
                {
                    this.nodeTags.remove(connectorLinkTag.connectorTag.toNodeName);
                    connectorLinkTag.connectorTag.toNodeName = linkToTag.nodeEndTag.name;
                }
            }

            // ******************************
            // 2: connector exits from a link:
            position = Double.parseDouble(connectorLinkTag.connectorTag.fromPositionStr);
            LinkTag linkFromTag = this.realLinkTags.get(connectorLinkTag.connectorTag.fromLinkNo);
            isConnectorToLink = false;
            NodeTag newSplitNodeTag2 = NodeTag.createNewNodeAtLinkPosition(linkFromTag, this, position);

            Map<String, LinkTag> newLinkFromTags =
                    LinkTag.splitLink(newSplitNodeTag2, linkFromTag, this, position, margin, isConnectorToLink);
            linkFromTag.signalHeads.removeAll(linkFromTag.signalHeadsToRemove);
            linkFromTag.sensors.removeAll(linkFromTag.sensorTagsToRemove);

            if (newLinkFromTags != null)
            {
                // only add if a link is split
                connectorLinkTag.connectorTag.fromNodeName = newSplitNodeTag2.name;
                this.nodeTags.put(newSplitNodeTag2.name, newSplitNodeTag2);
                // adjust the connection of other Connector links
                for (LinkTag connectorLinkTag2 : this.connectorTags.values())
                {
                    if (connectorLinkTag2 != connectorLinkTag
                            && connectorLinkTag2.connectorTag.fromLinkNo.equals(connectorLinkTag.connectorTag.fromLinkNo))
                    {

                        Double position2 = Double.parseDouble(connectorLinkTag2.connectorTag.fromPositionStr);
                        if (position2 > position)
                        {
                            connectorLinkTag2.connectorTag.fromLinkNo = newLinkFromTags.values().iterator().next().name;
                            Double newPosition = position2 - position;
                            connectorLinkTag2.connectorTag.fromPositionStr = newPosition.toString();
                        }
                    }
                    if (connectorLinkTag2 != connectorLinkTag
                            && connectorLinkTag2.connectorTag.toLinkNo.equals(connectorLinkTag.connectorTag.fromLinkNo))
                    {

                        Double position2 = Double.parseDouble(connectorLinkTag2.connectorTag.toPositionStr);
                        if (position2 > position)
                        {
                            connectorLinkTag2.connectorTag.toLinkNo = newLinkFromTags.values().iterator().next().name;
                            Double newPosition = position2 - position;
                            connectorLinkTag2.connectorTag.toPositionStr = newPosition.toString();
                        }
                    }
                }
                // connectorTag.connectorTag.fromLinkNo = newLinkFromTags.values().iterator().next().name;
                this.linkTags.putAll(newLinkFromTags);
                this.realLinkTags.putAll(newLinkFromTags);
            }
            else
            {
                // from connector to next link
                if (position < margin)
                {
                    this.nodeTags.remove(connectorLinkTag.connectorTag.fromNodeName);
                    connectorLinkTag.connectorTag.fromNodeName = linkFromTag.nodeStartTag.name;
                }
                // from link to connector
                else
                {
                    this.nodeTags.remove(connectorLinkTag.connectorTag.fromNodeName);
                    connectorLinkTag.connectorTag.fromNodeName = linkFromTag.nodeEndTag.name;
                }
            }
        }
    }

    /**
     * @return the OTSRoadNetwork with the static information about the network
     * @throws NetworkException if items cannot be added to the Network
     */
    private OTSRoadNetwork makeNetwork() throws NetworkException
    {
        // for (RouteTag routeTag : this.routeTags.values()) {
        // // TODO Make routes GTU specific. See what to do with GTUType.ALL for routes
        // // TODO Automate addition of Routes to network
        // this.network.addRoute(GTUType.ALL, routeTag.route);
        // }
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "VissimANMNetworkLaneParser [gtuTypes=" + this.gtuTypes + ", laneTypes=" + this.laneTypes + "]";
    }

    public GlobalTag getGlobalTag()
    {
        return globalTag;
    }

    public void setGlobalTag(GlobalTag globalTag)
    {
        this.globalTag = globalTag;
    }

    public Map<String, NodeTag> getNodeTags()
    {
        return nodeTags;
    }

    public void setNodeTags(Map<String, NodeTag> nodeTags)
    {
        this.nodeTags = nodeTags;
    }

    public Map<String, LinkTag> getLinkTags()
    {
        return linkTags;
    }

    public void setLinkTags(Map<String, LinkTag> linkTags)
    {
        this.linkTags = linkTags;
    }

    public Map<String, LinkTag> getConnectorTags()
    {
        return connectorTags;
    }

    public void setConnectorTags(Map<String, LinkTag> connectorTags)
    {
        this.connectorTags = connectorTags;
    }

    public Map<String, LinkTag> getRealLinkTags()
    {
        return realLinkTags;
    }

    public void setRealLinkTags(Map<String, LinkTag> realLinkTags)
    {
        this.realLinkTags = realLinkTags;
    }

    public Map<String, SignalHeadTag> getSignalHeadTags()
    {
        return signalHeadTags;
    }

    public void setSignalHeadTags(Map<String, SignalHeadTag> signalHeadTags)
    {
        this.signalHeadTags = signalHeadTags;
    }

    public Map<String, SensorTag> getSensorTags()
    {
        return sensorTags;
    }

    public void setSensorTags(Map<String, SensorTag> sensorTags)
    {
        this.sensorTags = sensorTags;
    }

    public Map<String, GTUTag> getGtuTags()
    {
        return gtuTags;
    }

    public void setGtuTags(Map<String, GTUTag> gtuTags)
    {
        this.gtuTags = gtuTags;
    }

    public Map<String, GTUMixTag> getGtuMixTags()
    {
        return gtuMixTags;
    }

    public void setGtuMixTags(Map<String, GTUMixTag> gtuMixTags)
    {
        this.gtuMixTags = gtuMixTags;
    }

    public Map<String, RoadTypeTag> getRoadTypeTags()
    {
        return roadTypeTags;
    }

    public void setRoadTypeTags(Map<String, RoadTypeTag> roadTypeTags)
    {
        this.roadTypeTags = roadTypeTags;
    }

    public Map<String, GTUType> getGtuTypes()
    {
        return gtuTypes;
    }

    public void setGtuTypes(Map<String, GTUType> gtuTypes)
    {
        this.gtuTypes = gtuTypes;
    }

    public Map<String, LaneTypeTag> getLaneTypeTags()
    {
        return laneTypeTags;
    }

    public void setLaneTypeTags(Map<String, LaneTypeTag> laneTypeTags)
    {
        this.laneTypeTags = laneTypeTags;
    }

    public Map<String, RoadLayoutTag> getRoadLayoutTags()
    {
        return roadLayoutTags;
    }

    public void setRoadLayoutTags(Map<String, RoadLayoutTag> roadLayoutTags)
    {
        this.roadLayoutTags = roadLayoutTags;
    }

    public Map<String, RouteMixTag> getRouteMixTags()
    {
        return routeMixTags;
    }

    public void setRouteMixTags(Map<String, RouteMixTag> routeMixTags)
    {
        this.routeMixTags = routeMixTags;
    }

    public Map<String, ShortestRouteMixTag> getShortestRouteMixTags()
    {
        return shortestRouteMixTags;
    }

    public void setShortestRouteMixTags(Map<String, ShortestRouteMixTag> shortestRouteMixTags)
    {
        this.shortestRouteMixTags = shortestRouteMixTags;
    }

    public Map<String, ShortestRouteTag> getShortestRouteTags()
    {
        return shortestRouteTags;
    }

    public void setShortestRouteTags(Map<String, ShortestRouteTag> shortestRouteTags)
    {
        this.shortestRouteTags = shortestRouteTags;
    }

    public Map<String, RouteTag> getRouteTags()
    {
        return routeTags;
    }

    public void setRouteTags(Map<String, RouteTag> routeTags)
    {
        this.routeTags = routeTags;
    }

    public Map<String, LaneType> getLaneTypes()
    {
        return laneTypes;
    }

    public void setLaneTypes(Map<String, LaneType> laneTypes)
    {
        this.laneTypes = laneTypes;
    }

    public DEVSSimulatorInterface.TimeDoubleUnit getSimulator()
    {
        return simulator;
    }

    public void setSimulator(OTSSimulatorInterface simulator)
    {
        this.simulator = simulator;
    }

    public OTSRoadNetwork getNetwork()
    {
        return network;
    }

    public void setNetwork(OTSRoadNetwork network)
    {
        this.network = network;
    }

    public int getUpperNodeNr()
    {
        return upperNodeNr;
    }

    public void setUpperNodeNr(int upperNodeNr)
    {
        this.upperNodeNr = upperNodeNr;
    }

    public int getUpperLinkNr()
    {
        return upperLinkNr;
    }

    public void setUpperLinkNr(int upperLinkNr)
    {
        this.upperLinkNr = upperLinkNr;
    }
}

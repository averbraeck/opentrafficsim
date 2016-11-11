package org.opentrafficsim.road.network.factory.vissim;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.AngleUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.factory.vissim.xsd.DEFINITIONS;
import org.opentrafficsim.road.network.factory.vissim.xsd.GLOBAL;
import org.opentrafficsim.road.network.factory.vissim.xsd.GTU;
import org.opentrafficsim.road.network.factory.vissim.xsd.LINK;
import org.opentrafficsim.road.network.factory.vissim.xsd.LINK.BEZIER;
import org.opentrafficsim.road.network.factory.vissim.xsd.NETWORK;
import org.opentrafficsim.road.network.factory.vissim.xsd.NODE;
import org.opentrafficsim.road.network.factory.vissim.xsd.ObjectFactory;
import org.opentrafficsim.road.network.factory.vissim.xsd.ROADLAYOUT;
import org.opentrafficsim.road.network.factory.vissim.xsd.ROADTYPE;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;
import org.opentrafficsim.road.network.lane.object.sensor.Sensor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.LineString;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class VissimNetworkLaneParser implements Serializable {
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Global values from the GLOBAL tag. */
    @SuppressWarnings("visibilitymodifier")
    protected GlobalTag globalTag;

    /** The UNprocessed links for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, NodeTag> nodeTags = new HashMap<>();

    /** The UNprocessed links for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, LinkTag> linkTags = new HashMap<>();

    /** The UNprocessed links for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, LinkTag> connectorTags = new HashMap<>();

    /** The UNprocessed links for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, LinkTag> realLinkTags = new HashMap<>();

    /** The UNprocessed links for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, SignalHeadTag> signalHeadTags = new HashMap<>();

    /** The UNprocessed links for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, SensorTag> sensorTags = new HashMap<>();

    /** The gtu tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, GTUTag> gtuTags = new HashMap<>();

    /** The gtumix tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, GTUMixTag> gtuMixTags = new HashMap<>();

    /** The road type tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RoadTypeTag> roadTypeTags = new HashMap<>();

    /** The GTUTypes that have been created. public to make it accessible from LaneAttributes. */
    @SuppressWarnings("visibilitymodifier")
    public Map<String, GTUType> gtuTypes = new HashMap<>();

    /** The LaneType tags that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, LaneTypeTag> laneTypeTags = new HashMap<>();

    /** The LaneType tags that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RoadLayoutTag> roadLayoutTags = new HashMap<>();

    /** The RouteMix tags that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RouteMixTag> routeMixTags = new HashMap<>();

    /** The RouteMix tags that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, ShortestRouteMixTag> shortestRouteMixTags = new HashMap<>();

    /** The RouteMix tags that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, ShortestRouteTag> shortestRouteTags = new HashMap<>();

    /** The RouteMix tags that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RouteTag> routeTags = new HashMap<>();

    /** The LaneTypes that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, LaneType> laneTypes = new HashMap<>();

    /** The simulator for creating the animation. Null if no animation needed. */
    @SuppressWarnings("visibilitymodifier")
    protected OTSDEVSSimulatorInterface simulator;

    /** The network to register the GTUs in. */
    @SuppressWarnings("visibilitymodifier")
    protected OTSNetwork network;

    /*****
     * Variables typically for Vissim network import
     */
    // the node number is automatically generated and increases with every additional node
    public int upperNodeNr = 1;

    // the node number is automatically generated and increases with every additional node
    public int upperLinkNr = 1;

    /**
     * @param simulator the simulator for creating the animation. Null if no animation needed.
     */
    public VissimNetworkLaneParser(final OTSDEVSSimulatorInterface simulator) {
        this.simulator = simulator;
    }

    /**
     * @param inputUrl the file with the network in the agreed xml-grammar.
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
    public OTSNetwork build(final URL inputUrl, File outputFile, final OTSNetwork network) throws NetworkException,
        ParserConfigurationException, SAXException, IOException, NamingException, GTUException, OTSGeometryException,
        SimRuntimeException {
        if (inputUrl.getFile().length() > 0 && !(new File(inputUrl.getFile()).exists())) {
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
        if (!document.getDocumentElement().getNodeName().equals("network")) {
            throw new SAXException("XmlNetworkLaneParser.build: XML document does not start with an NETWORK tag, found "
                + document.getDocumentElement().getNodeName() + " instead");
        }

        // make the GTUTypes ALL and NONE to get started
        this.gtuTypes.put("ALL", GTUType.ALL);
        this.gtuTypes.put("NONE", GTUType.NONE);

        // Read the link (and connector) tags and ...
        // Construct the nodes as Vissim does not use Nodes!!
        LinkTag.parseLinks(networkXMLNodeList, this);

        // the signal heads are stored separately. Read them first, connect to links later on
        SignalHeadTag.parseSignalHead(networkXMLNodeList, this);

        // the sensor are defined separate also. Read them first, connect to links later on
        SensorTag.parseSensor(networkXMLNodeList, this);

        // process nodes and links to calculate coordinates and positions
        // org.opentrafficsim.road.network.factory.xml.
        Links.calculateNodeCoordinates(this);

        // add the signalHeads to the links
        LinkTag.addSignalHeads(this);

        // add the detectors to the links
        LinkTag.addDetectors(this);

        // create a subset of connector links
        for (LinkTag linkTag : this.linkTags.values()) {
            if (linkTag.connector) {
                this.connectorTags.put(linkTag.name, linkTag);
            } else {
                this.realLinkTags.put(linkTag.name, linkTag);
            }
        }

        // removes all duplicate nodes, both in Link and Node lists
        NodeTag.removeDuplicateNodes(this);
        // Split links where appropriate
        // Step 1: split links, where a connector intersects a link
        // loops through all connectors
        // a link gets split if the connector is more than "margin" number of meters from the start or end of a link
        Double margin = 3.0;
        splitLinksIntersectedByConnector(margin);

        // create space between connectors and links, in order to make bezier curved links between connectors and links
        // action: the connectors become shorter than they were
        // LinkTag.shortenConnectors(this);

        // again process nodes and links to calculate coordinates and positions for the new link/nodes
        Links.calculateNodeCoordinates(this);

        // Create links to connect the shortened connectors and links by means of bezier curved links
        createLinkBetweenConnectorAndLink(this);

        // Step 2: split links directly behind a signal head, assuming such a link enters a signalized junction
        //
        Double splitMetersAfterSignalHead = 1.5;
        margin = 4.0;
        splitLinkAtSignalAndDetector(this.linkTags, margin, splitMetersAfterSignalHead);

        // again process nodes and links to calculate coordinates and positions for the new link/nodes
        Links.calculateNodeCoordinates(this);

        // build links
        for (LinkTag linkTag : this.linkTags.values()) {
            Links.buildLink(linkTag, this, this.simulator);
        }

        // Generate the real links
        for (LinkTag linkTag : this.linkTags.values()) {
            Links.applyRoadTypeToLink(linkTag, this, this.simulator);
        }

        // generate the connector links
        for (LinkTag linkTag : this.linkTags.values()) {
            Links.applyRoadTypeToConnector(linkTag, this, this.simulator);
        }
        // process the routes
        // for (RouteTag routeTag : this.routeTags.values())
        // routeTag.makeRoute();
        // TODO shortestRoute, routeMix, ShortestRouteMix

        // store the structure information in the network
        writeToXML(outputFile);

        return makeNetwork();
    }

    private void writeToXML(File file) throws NetworkException {
        try {

            DEFINITIONS definitions = generateDefinitions();

            generateGtusAndRoadtypes(definitions);

            List<NODE> nodes = new ArrayList<>();
            generateNodes(nodes);

            List<LINK> links = new ArrayList<>();
            List<ROADLAYOUT> roadLayouts = new ArrayList<>();
            generateLinks(links, roadLayouts);
            definitions.getContent().addAll(roadLayouts);

            marshall(file, definitions, nodes, links);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private DEFINITIONS generateDefinitions() {
        DEFINITIONS definitions = new DEFINITIONS();
        generateGTUTypes(definitions);
        GLOBAL global = new GLOBAL();
        definitions.getContent().add(global);
        return definitions;
    }

    private void generateGTUTypes(DEFINITIONS definitions) {
        List<GTUTYPE> gtuTypes = new ArrayList<>();
        GTUTYPE gtuType = new GTUTYPE();
        gtuType.setNAME("CAR");
        gtuTypes.add(gtuType);
        definitions.getContent().addAll(gtuTypes);
    }

    private void generateGtusAndRoadtypes(DEFINITIONS definitions) {
        // definitions.getContent().add(gtuType);
        List<GTU> gtus = new ArrayList<>();
        GTU gtu = new GTU();
        gtu.setNAME("CAR");
        gtu.setGTUTYPE("CAR");
        gtu.setMAXSPEED("CONST(" + new Speed(140, SpeedUnit.KM_PER_HOUR).getInUnit(SpeedUnit.KM_PER_HOUR) + ") km/h");
        gtu.setLENGTH("CONST(" + new Length(4.5, LengthUnit.METER).getInUnit(LengthUnit.METER) + ") m");
        gtu.setWIDTH("CONST(" + new Length(2.0, LengthUnit.METER).getInUnit(LengthUnit.METER) + ") m");
        gtus.add(gtu);
        definitions.getContent().addAll(gtus);

        // road types
        List<ROADTYPE> roadTypes = new ArrayList<>();
        ROADTYPE roadType = new ROADTYPE();
        roadType.setDEFAULTLANEKEEPING("KEEPLANE");
        roadType.setDEFAULTOVERTAKING("NONE");
        roadType.setDEFAULTLANEWIDTH("3.5m");
        roadType.setNAME("RINGROAD");
        ROADTYPE.SPEEDLIMIT speedLimit = new ROADTYPE.SPEEDLIMIT();
        speedLimit.setGTUTYPE(gtu.getGTUTYPE());
        speedLimit.setLEGALSPEEDLIMIT(new Speed(140, SpeedUnit.KM_PER_HOUR).getInUnit(SpeedUnit.KM_PER_HOUR) + " km/h");
        roadType.getSPEEDLIMIT().add(speedLimit);
        roadTypes.add(roadType);
        definitions.getContent().addAll(roadTypes);

    }

    private void marshall(File file, DEFINITIONS definitions, List<NODE> nodes, List<LINK> links) throws JAXBException,
        PropertyException {
        JAXBContext jaxbContext = JAXBContext.newInstance("org.opentrafficsim.road.network.factory.vissim.xsd");
        Marshaller marshaller = jaxbContext.createMarshaller();
        ObjectFactory outputFactory = new ObjectFactory();
        NETWORK networkElement = outputFactory.createNETWORK();
        networkElement.getDEFINITIONSOrIncludeOrNODE().add(definitions);
        networkElement.getDEFINITIONSOrIncludeOrNODE().addAll(nodes);
        networkElement.getDEFINITIONSOrIncludeOrNODE().addAll(links);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(networkElement, System.out);
        marshaller.marshal(networkElement, file);
    }

    private void generateLinks(List<LINK> links, List<ROADLAYOUT> roadLayouts) throws NetworkException {

        Iterator<LinkTag> iter = this.linkTags.values().iterator();
        while (iter.hasNext()) {
            LinkTag inputLink = iter.next();
            LINK link = new LINK();
            // set link the attributes and items
            link.setNAME(inputLink.name);
            String layoutName = "rl" + link.getNAME();
            link.setROADLAYOUTAttribute(layoutName);

            if (inputLink.arcTag != null) {
                LINK.ARC arc = new LINK.ARC();
                arc.setANGLE(inputLink.arcTag.angle.getInUnit(AngleUnit.DEGREE) + " deg");
                arc.setRADIUS("" + inputLink.arcTag.radius.getInUnit(LengthUnit.METER));
                arc.setDIRECTION("" + inputLink.arcTag.direction);
                link.setARC(arc);
            }

            if (inputLink.bezierTag != null) {
                LINK.BEZIER bezier = new BEZIER();
                link.setBEZIER(bezier);
            }

            ROADLAYOUT rla = new ROADLAYOUT();
            rla.setROADTYPE("RINGROAD");
            rla.setNAME(layoutName);

            Iterator<Lane> lanes = inputLink.lanes.values().iterator();
            while (lanes.hasNext()) {
                ROADLAYOUT.LANE lane = new ROADLAYOUT.LANE();
                Lane inputLane = lanes.next();
                lane.setNAME(inputLane.getId());
                lane.setWIDTH(inputLane.getBeginWidth().getInUnit(LengthUnit.METER) + "m");
                lane.setOFFSET(inputLane.getDesignLineOffsetAtBegin().getInUnit(LengthUnit.METER) + "m");
                if (inputLane.getDesignLineOffsetAtBegin().ne(inputLane.getDesignLineOffsetAtEnd())) {
                    double differenceOffset = inputLane.getDesignLineOffsetAtEnd().minus(inputLane
                        .getDesignLineOffsetAtBegin()).getInUnit(LengthUnit.METER);
                    link.setOFFSETEND("" + differenceOffset + "m");
                }
                if (inputLink.connector) {
                    lane.setCOLOR("BLACK");
                } else {
                    lane.setCOLOR("GRAY");
                }
                lane.setDIRECTION("FORWARD");
                ROADLAYOUT.LANE.SPEEDLIMIT speedLimit = new ROADLAYOUT.LANE.SPEEDLIMIT();
                speedLimit.setLEGALSPEEDLIMIT(inputLane.getSpeedLimit(GTUType.ALL).getInUnit(SpeedUnit.KM_PER_HOUR)
                    + " km/h");
                speedLimit.setGTUTYPE("CAR");
                lane.getSPEEDLIMIT().add(speedLimit);
                rla.getLANEOrNOTRAFFICLANEOrSHOULDER().add(lane);
                for (Sensor inputSensor : inputLane.getSensors()) {
                    LINK.SENSOR sensor = new LINK.SENSOR();
                    sensor.setNAME(inputSensor.getId());
                    sensor.setLANE(lane.getNAME());
                    sensor.setPOSITION(Double.toString(inputSensor.getLongitudinalPosition().getInUnit(LengthUnit.METER))
                        + " m");
                    sensor.setTRIGGER(" " + inputSensor.getPositionType());
                    sensor.setCLASS("nl.grontmij.smarttraffic.model.CheckSensor");
                    link.getLANEOVERRIDEOrGENERATOROrLISTGENERATOR().add(sensor);
                }
                for (LaneBasedObject inputSimpleTrafficLight : inputLane.getLaneBasedObjects()) {
                    LINK.TRAFFICLIGHT simpleTrafficLight = new LINK.TRAFFICLIGHT();
                    simpleTrafficLight.setNAME(inputSimpleTrafficLight.getId());
                    simpleTrafficLight.setLANE(lane.getNAME());
                    simpleTrafficLight.setPOSITION(Double.toString(inputSimpleTrafficLight.getLongitudinalPosition()
                        .getInUnit(LengthUnit.METER)) + " m");
                    simpleTrafficLight.setCLASS(
                        "org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight");
                    link.getLANEOVERRIDEOrGENERATOROrLISTGENERATOR().add(simpleTrafficLight);
                }
                ROADLAYOUT.STRIPE stripe = new ROADLAYOUT.STRIPE();
                stripe.setTYPE("DASHED");
                stripe.setOFFSET(inputLane.getDesignLineOffsetAtBegin().minus(inputLane.getBeginWidth().divideBy(2.0))
                    .getInUnit(LengthUnit.METER) + "m");
                rla.getLANEOrNOTRAFFICLANEOrSHOULDER().add(stripe);

            }
            // link.setROADLAYOUT(rla);
            roadLayouts.add(rla);

            if (inputLink.straightTag != null) {
                LINK.STRAIGHT straight = new LINK.STRAIGHT();
                if (inputLink.straightTag.length != null) {
                    straight.setLENGTH(inputLink.straightTag.length.getInUnit(LengthUnit.METER) + " m");
                }
                link.setSTRAIGHT(straight);
            }

            if (inputLink.polyLineTag != null) {
                LINK.POLYLINE polyLine = new LINK.POLYLINE();
                String coordString = null;
                int length = inputLink.polyLineTag.vertices.length;
                for (int i = 0; i < length; i++) {
                    OTSPoint3D coord = inputLink.polyLineTag.vertices[i];
                    coordString = "(" + coord.x + "," + coord.y + "," + coord.z + ")";
                    polyLine.getINTERMEDIATEPOINTS().add(coordString);
                }
                link.setPOLYLINE(polyLine);
            }

            link.setNODESTART(inputLink.nodeStartTag.name);
            link.setNODEEND(inputLink.nodeEndTag.name);
            links.add(link);
        }
    }

    private void generateNodes(List<NODE> nodes) {
        Iterator<NodeTag> iterNode = this.nodeTags.values().iterator();
        while (iterNode.hasNext()) {
            NodeTag inputNode = iterNode.next();
            NODE node = new NODE();
            node.setNAME(inputNode.name);
            node.setCOORDINATE(inputNode.coordinate.toString());
            node.setANGLE(inputNode.angle.getInUnit(AngleUnit.DEGREE) + " deg");
            nodes.add(node);
        }
    }

    /**
     * @param vissimNetworkLaneParser
     * @throws OTSGeometryException
     */
    private void createLinkBetweenConnectorAndLink(VissimNetworkLaneParser parser) throws OTSGeometryException {
        // loop through all connector links
        for (LinkTag connectorLinkTag : this.connectorTags.values()) {

            OTSLine3D designLineOTS = LinkTag.createLineString(connectorLinkTag);
            LineString designLine = designLineOTS.getLineString();
            Double length = designLine.getLength();
            // default: we replace the connector link by a bezier curved link
            if (length < 999995) {
                connectorLinkTag.nodeStartTag = parser.nodeTags.get(connectorLinkTag.connectorTag.fromNodeName);
                connectorLinkTag.nodeEndTag = parser.nodeTags.get(connectorLinkTag.connectorTag.toNodeName);
                connectorLinkTag.bezierTag = new BezierTag();
                if (connectorLinkTag.nodeStartTag.name.equals(connectorLinkTag.nodeEndTag.name)) {
                    this.linkTags.remove(connectorLinkTag.name);
                    // this.connectorTags.remove(connectorLinkTag.name);
                }
                if (connectorLinkTag.polyLineTag != null) {
                    connectorLinkTag.polyLineTag = null;
                }
                if (connectorLinkTag.straightTag != null) {
                    connectorLinkTag.straightTag = null;
                }
            }
            // if the link is quite long....we could split it but we don't do this now
            else {
                // get the from link
                LinkTag pasteLinkFromTag = new LinkTag(connectorLinkTag);
                // add a unique name
                String linkName = "" + parser.upperLinkNr;
                parser.upperLinkNr++;
                pasteLinkFromTag.name = linkName;
                pasteLinkFromTag.nodeStartTag = parser.nodeTags.get(connectorLinkTag.connectorTag.fromNodeName);
                pasteLinkFromTag.nodeEndTag = connectorLinkTag.nodeStartTag;
                pasteLinkFromTag.bezierTag = new BezierTag();
                if (pasteLinkFromTag.polyLineTag != null) {
                    pasteLinkFromTag.polyLineTag = null;
                }
                if (pasteLinkFromTag.straightTag != null) {
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
                if (pasteLinkToTag.polyLineTag != null) {
                    pasteLinkToTag.polyLineTag = null;
                }
                if (pasteLinkToTag.straightTag != null) {
                    pasteLinkToTag.straightTag = null;
                }
                // this.linkTags.put(pasteLinkToTag.name, pasteLinkToTag);

            }
            // }
        }
    }

    private void splitLinkAtSignalAndDetector(Map<String, LinkTag> inputLinkTags, Double margin,
        Double splitMetersAfterSignalHead) throws OTSGeometryException, NetworkException {

        Map<String, LinkTag> newLinkTags = new HashMap<>();
        // Loops through all links
        splitLinksAtSignal(margin, splitMetersAfterSignalHead, inputLinkTags, newLinkTags);

    }

    private void splitLinksAtSignal(Double margin, Double splitMetersAfterSignalHead, Map<String, LinkTag> inputLinkTags,
        Map<String, LinkTag> newLinkTags) throws OTSGeometryException, NetworkException {
        for (LinkTag linkTag : inputLinkTags.values()) {
            for (SignalHeadTag signalHeadTag : linkTag.signalHeads) {
                Double position = Double.parseDouble(signalHeadTag.positionStr);
                Double splitPosition = position + splitMetersAfterSignalHead;
                // A new node has to be created
                NodeTag newNodeTag = NodeTag.createNewNodeAtLinkPosition(linkTag, this, splitPosition);
                // split the link and connect the new node after the position of the signalhead\
                Map<String, LinkTag> newLinks = LinkTag.splitLink(newNodeTag, linkTag, this, splitPosition, margin, false);
                if (newLinks != null) {
                    newLinkTags.putAll(newLinks);
                }
            }
            linkTag.signalHeads.removeAll(linkTag.signalHeadsToRemove);
            linkTag.sensors.removeAll(linkTag.sensorTagsToRemove);
            // relocate the signalHeads and detectors afterwards!
        }
        if (!newLinkTags.isEmpty()) {
            this.linkTags.putAll(newLinkTags);
        }

        // run again for the newly created linkTags
        Map<String, LinkTag> new2LinkTags = new HashMap<>();
        if (newLinkTags.size() > 0) {
            splitLinksAtSignal(margin, splitMetersAfterSignalHead, newLinkTags, new2LinkTags);
        }
    }

    private void splitLinksIntersectedByConnector(Double margin) throws OTSGeometryException, NetworkException {
        // loop through all connector links
        for (LinkTag connectorLinkTag : this.connectorTags.values()) {
            if (connectorLinkTag.name.equals("10125")) {
                System.out.println("10125");
            }
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
            Map<String, LinkTag> newLinkToTags = LinkTag.splitLink(newSplitNodeTag, linkToTag, this, position, margin,
                isConnectorToLink);
            linkToTag.signalHeads.removeAll(linkToTag.signalHeadsToRemove);
            linkToTag.sensors.removeAll(linkToTag.sensorTagsToRemove);

            if (newLinkToTags != null) {
                // only add if a link is split
                connectorLinkTag.connectorTag.toNodeName = newSplitNodeTag.name;
                this.nodeTags.put(newSplitNodeTag.name, newSplitNodeTag);
                // adjust the connection of other Connector links
                for (LinkTag connectorLinkTag2 : this.connectorTags.values()) {

                    if (connectorLinkTag2 != connectorLinkTag && connectorLinkTag2.connectorTag.toLinkNo.equals(
                        connectorLinkTag.connectorTag.toLinkNo)) {

                        Double position2 = Double.parseDouble(connectorLinkTag2.connectorTag.toPositionStr);
                        if (position2 > position) {
                            connectorLinkTag2.connectorTag.toLinkNo = newLinkToTags.values().iterator().next().name;
                            Double newPosition = position2 - position;
                            connectorLinkTag2.connectorTag.toPositionStr = newPosition.toString();
                        }
                    }
                    if (connectorLinkTag2 != connectorLinkTag && connectorLinkTag2.connectorTag.fromLinkNo.equals(
                        connectorLinkTag.connectorTag.toLinkNo)) {

                        Double position2 = Double.parseDouble(connectorLinkTag2.connectorTag.fromPositionStr);
                        if (position2 > position) {
                            connectorLinkTag2.connectorTag.fromLinkNo = newLinkToTags.values().iterator().next().name;
                            Double newPosition = position2 - position;
                            connectorLinkTag2.connectorTag.fromPositionStr = newPosition.toString();
                        }
                    }
                }
                // connectorTag.connectorTag.toLinkNo = newLinkToTags.values().iterator().next().name;
                this.linkTags.putAll(newLinkToTags);
                this.realLinkTags.putAll(newLinkToTags);
            } else {

                // from connector to next link
                if (position < margin) {
                    this.nodeTags.remove(connectorLinkTag.connectorTag.toNodeName);
                    connectorLinkTag.connectorTag.toNodeName = linkToTag.nodeStartTag.name;
                }
                // from link to connector
                else {
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

            Map<String, LinkTag> newLinkFromTags = LinkTag.splitLink(newSplitNodeTag2, linkFromTag, this, position, margin,
                isConnectorToLink);
            linkFromTag.signalHeads.removeAll(linkFromTag.signalHeadsToRemove);
            linkFromTag.sensors.removeAll(linkFromTag.sensorTagsToRemove);

            if (newLinkFromTags != null) {
                // only add if a link is split
                connectorLinkTag.connectorTag.fromNodeName = newSplitNodeTag2.name;
                this.nodeTags.put(newSplitNodeTag2.name, newSplitNodeTag2);
                // adjust the connection of other Connector links
                for (LinkTag connectorLinkTag2 : this.connectorTags.values()) {
                    if (connectorLinkTag2 != connectorLinkTag && connectorLinkTag2.connectorTag.fromLinkNo.equals(
                        connectorLinkTag.connectorTag.fromLinkNo)) {

                        Double position2 = Double.parseDouble(connectorLinkTag2.connectorTag.fromPositionStr);
                        if (position2 > position) {
                            connectorLinkTag2.connectorTag.fromLinkNo = newLinkFromTags.values().iterator().next().name;
                            Double newPosition = position2 - position;
                            connectorLinkTag2.connectorTag.fromPositionStr = newPosition.toString();
                        }
                    }
                    if (connectorLinkTag2 != connectorLinkTag && connectorLinkTag2.connectorTag.toLinkNo.equals(
                        connectorLinkTag.connectorTag.fromLinkNo)) {

                        Double position2 = Double.parseDouble(connectorLinkTag2.connectorTag.toPositionStr);
                        if (position2 > position) {
                            connectorLinkTag2.connectorTag.toLinkNo = newLinkFromTags.values().iterator().next().name;
                            Double newPosition = position2 - position;
                            connectorLinkTag2.connectorTag.toPositionStr = newPosition.toString();
                        }
                    }
                }
                // connectorTag.connectorTag.fromLinkNo = newLinkFromTags.values().iterator().next().name;
                this.linkTags.putAll(newLinkFromTags);
                this.realLinkTags.putAll(newLinkFromTags);
            } else {
                // from connector to next link
                if (position < margin) {
                    this.nodeTags.remove(connectorLinkTag.connectorTag.fromNodeName);
                    connectorLinkTag.connectorTag.fromNodeName = linkFromTag.nodeStartTag.name;
                }
                // from link to connector
                else {
                    this.nodeTags.remove(connectorLinkTag.connectorTag.fromNodeName);
                    connectorLinkTag.connectorTag.fromNodeName = linkFromTag.nodeEndTag.name;
                }
            }
        }
    }

    /**
     * @return the OTSNetwork with the static information about the network
     * @throws NetworkException if items cannot be added to the Network
     */
    private OTSNetwork makeNetwork() throws NetworkException {
        // for (RouteTag routeTag : this.routeTags.values()) {
        // // TODO Make routes GTU specific. See what to do with GTUType.ALL for routes
        // // TODO Automate addition of Routes to network
        // this.network.addRoute(GTUType.ALL, routeTag.route);
        // }
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return "VissimANMNetworkLaneParser [gtuTypes=" + this.gtuTypes + ", laneTypes=" + this.laneTypes + "]";
    }

}

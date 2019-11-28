package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Length;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.Coordinates;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
final class LinkTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String legalSpeed = null;

    /** From node tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    NodeTag nodeStartTag = null;

    /** To node tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    NodeTag nodeEndTag = null;

    /** Offset for the link at the start node. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length offsetStart = null;

    /** Offset for the link at the end node. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length offsetEnd = null;

    /** Extra rotation for the link at the start node. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Angle rotationStart = null;

    /** Extra rotation for the link at the end node. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Angle rotationEnd = null;

    /** Straight. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    StraightTag straightTag = null;

    //
    /** PolyLine. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    PolyLineTag polyLineTag = null;

    /** Map of lane name to lane override. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, LaneOverrideTag> laneOverrideTags = new LinkedHashMap<>();

    /** Map of lane name to generators. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, GeneratorTag> generatorTags = new LinkedHashMap<>();

    /** Map of lane name to list generators. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, ListGeneratorTag> listGeneratorTags = new LinkedHashMap<>();

    /** Map of lane name to list of sensors. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, List<SensorTag>> sensorTags = new LinkedHashMap<>();

    /** Map of lane name to fill at t=0. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, FillTag> fillTags = new LinkedHashMap<>();

    /** Map of lane name to sink tags. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, SinkTag> sinkTags = new LinkedHashMap<>();

    /** Map of lane name to generated lanes. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, Lane> lanes = new LinkedHashMap<>();

    /** Map of lane name to generated lanes. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, LaneTag> laneTags = new LinkedHashMap<>();

    ConnectorTag connectorTag = null;

    /** The calculated Link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    CrossSectionLink link = null;

    /** The lane keeping policy, i.e., keep left, keep right or keep lane. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LaneKeepingPolicy laneKeepingPolicy = null;

    List<SignalHeadTag> signalHeads = new ArrayList<>();

    List<SensorTag> sensors = new ArrayList<>();

    List<SignalHeadTag> signalHeadsToRemove = new ArrayList<>();

    List<SensorTag> sensorTagsToRemove = new ArrayList<>();

    // a link (false) or a connector (true)
    boolean connector = false;

    ArcTag arcTag;

    BezierTag bezierTag;

    public RoadLayoutTag roadLayoutTag;

    /**
     * @param linkTag LinkTag; link Info from XML
     */
    public LinkTag(LinkTag linkTag)
    {
        this.connectorTag = linkTag.connectorTag;
        this.connector = linkTag.connector;
        this.laneKeepingPolicy = linkTag.laneKeepingPolicy;
        this.lanes.putAll(linkTag.lanes);
        this.laneTags.putAll(linkTag.laneTags);
        this.legalSpeed = linkTag.legalSpeed;
        if (linkTag.straightTag != null)
        {
            this.straightTag = new StraightTag(linkTag.straightTag);
        }
        if (linkTag.polyLineTag != null)
        {
            this.polyLineTag = new PolyLineTag(linkTag.polyLineTag);
        }
        this.nodeEndTag = linkTag.nodeEndTag;
        this.nodeStartTag = linkTag.nodeStartTag;
    }

    /**
     *
     */
    public LinkTag()
    {
    }

    /**
     * Parse the LINK tags from Vissim fiels (.inpx) .
     * @param nodeList NodeList; nodeList the top-level nodes of the XML-file
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseLinks(final NodeList nodeList, final VissimNetworkLaneParser parser) throws SAXException, NetworkException
    {

        for (Node linksNode : XMLParser.getNodes(nodeList, "links"))
        {

            for (Node node : XMLParser.getNodes(linksNode.getChildNodes(), "link"))
            {
                NamedNodeMap attributes = node.getAttributes();

                // make a linkTag with the link attributes
                LinkTag linkTag = new LinkTag();

                // Lane keeping± currently with this default behaviour
                // TODO: differentiate by road type
                linkTag.laneKeepingPolicy = LaneKeepingPolicy.KEEPLANE;

                if (attributes.getNamedItem("no") == null)
                {
                    throw new SAXException("LINK: missing attribute: no");
                }
                linkTag.name = attributes.getNamedItem("no").getNodeValue().trim();
                Integer linkNr = Integer.parseInt(linkTag.name);
                if (linkNr > parser.getUpperLinkNr())
                {
                    parser.setUpperLinkNr(linkNr);
                }

                if (attributes.getNamedItem("assumSpeedOncom") == null)
                {
                    throw new SAXException("LINK: missing attribute assumSpeedOncom");
                }
                linkTag.legalSpeed = attributes.getNamedItem("assumSpeedOncom").getNodeValue().trim();

                // parse the geometry (coordinates of the link (nodes/vertices)) and add them to the LinkTag
                OTSPoint3D[] nodeCoords = parseLinkGeometry(parser, node, linkTag);

                // create a pair of nodes for every link. They will later be corrected (remove duplicate nodes)
                createNodesForLink(parser, linkTag, nodeCoords);

                // Parse the lanes. They are part of the link description in Vissim files
                List<Node> laneNodes = XMLParser.getNodes(node.getChildNodes(), "lanes");

                // the lanes are added as an attribute of the LinkTag
                createLanes(linkTag, laneNodes);

                // additional info from connectors
                List<Node> connectorFromNode = XMLParser.getNodes(node.getChildNodes(), "fromLinkEndPt");
                if (connectorFromNode.size() > 0)
                {
                    linkTag.connector = true;
                    createConnectorInfoFrom(linkTag, connectorFromNode);
                }

                List<Node> connectorToNode = XMLParser.getNodes(node.getChildNodes(), "toLinkEndPt");
                if (connectorFromNode.size() > 0)
                {
                    linkTag.connector = true;
                    createConnectorInfoTo(linkTag, connectorToNode);
                }

                // put this link in the map with LinkTags
                parser.getLinkTags().put(linkTag.name, linkTag);
            }

        }

    }

    private static void createConnectorInfoTo(LinkTag linkTag, List<Node> connectorToNode) throws SAXException
    {
        NamedNodeMap attributes;
        if (linkTag.connectorTag == null)
        {
            linkTag.connectorTag = new ConnectorTag();
        }
        attributes = connectorToNode.get(0).getAttributes();
        if (attributes.getNamedItem("lane") == null)
        {
            throw new SAXException("Connector: missing attribute: link/lane info");
        }
        String connect = attributes.getNamedItem("lane").getNodeValue().trim();
        String[] connectInfo = connect.split("\\s+");
        linkTag.connectorTag.toLinkNo = connectInfo[0];
        linkTag.connectorTag.toLaneNo = connectInfo[1];
        if (attributes.getNamedItem("pos") == null)
        {
            throw new SAXException("Connector: missing attribute: pos (position info)");
        }
        linkTag.connectorTag.toPositionStr = attributes.getNamedItem("pos").getNodeValue().trim();
    }

    private static void createConnectorInfoFrom(LinkTag linkTag, List<Node> connectorFromNode) throws SAXException
    {
        NamedNodeMap attributes;
        if (linkTag.connectorTag == null)
        {
            linkTag.connectorTag = new ConnectorTag();
        }
        attributes = connectorFromNode.get(0).getAttributes();
        if (attributes.getNamedItem("lane") == null)
        {
            throw new SAXException("Connector: missing attribute: link/lane info");
        }
        String connect = attributes.getNamedItem("lane").getNodeValue().trim();
        String[] connectInfo = connect.split("\\s+");
        linkTag.connectorTag.fromLinkNo = connectInfo[0];
        linkTag.connectorTag.fromLaneNo = connectInfo[1];

        if (attributes.getNamedItem("pos") == null)
        {
            throw new SAXException("Connector: missing attribute: pos (position info)");
        }
        linkTag.connectorTag.fromPositionStr = attributes.getNamedItem("pos").getNodeValue().trim();
    }

    private static void createLanes(LinkTag linkTag, List<Node> laneNodes)
    {
        NamedNodeMap attributes;
        int laneNo = 1;
        for (Node laneNode : XMLParser.getNodes(laneNodes.get(0).getChildNodes(), "lane"))
        {

            attributes = laneNode.getAttributes();
            LaneTag laneTag = new LaneTag();
            if (attributes.getLength() > 0)
            {
                laneTag.width = attributes.getNamedItem("width").getNodeValue().trim();
            }
            else
            {
                laneTag.width = "3.5";
                // must be a connector without lane attributes
                // the lane width is determined by its predecessor and successor
            }
            laneTag.linkNo = linkTag.name;
            laneTag.laneNo = "" + laneNo;
            laneNo++;
            linkTag.laneTags.put(laneTag.laneNo, laneTag);
        }
    }

    private static void createNodesForLink(final VissimNetworkLaneParser parser, LinkTag linkTag, OTSPoint3D[] nodeCoords)
            throws SAXException, NetworkException
    {
        // generate nodes from every Vissim link/connector
        String fromNodeStr = "" + parser.getUpperNodeNr();
        parser.setUpperNodeNr(parser.getUpperNodeNr() + 1);
        String toNodeStr = "" + parser.getUpperNodeNr();
        parser.setUpperNodeNr(parser.getUpperNodeNr() + 1);

        // parse the NODES, and add them to a nodelist directly
        NodeTag.parseNodes(parser, fromNodeStr, toNodeStr, nodeCoords);

        linkTag.nodeStartTag = parser.getNodeTags().get(fromNodeStr);
        linkTag.nodeEndTag = parser.getNodeTags().get(toNodeStr);
    }

    private static OTSPoint3D[] parseLinkGeometry(final VissimNetworkLaneParser parser, Node node, LinkTag linkTag)
            throws SAXException, NetworkException
    {
        List<Node> geometry = XMLParser.getNodes(node.getChildNodes(), "geometry");
        List<Node> pointsNodes = XMLParser.getNodes(geometry.get(0).getChildNodes(), "points3D");
        String coords = "";
        int numberOfPoints = 0;
        for (Node pointNode : XMLParser.getNodes(pointsNodes.get(0).getChildNodes(), "point3D"))
        {
            NamedNodeMap polyLineAttributes = pointNode.getAttributes();
            coords += "(" + polyLineAttributes.getNamedItem("x").getNodeValue() + ", "
                    + polyLineAttributes.getNamedItem("y").getNodeValue() + ")";
            numberOfPoints++;
        }

        OTSPoint3D[] nodeCoords = null;
        if (numberOfPoints > 2)
        {
            // process the intermediate vertices only
            PolyLineTag.parsePolyLine(coords, parser, linkTag);
        }
        else
        {
            // parse the STRAIGHT tag
            StraightTag.parseStraight(coords, parser, linkTag);
            // add coordinates to the nodes and vertices
        }
        // coords of begin and end Node
        nodeCoords = Coordinates.parseCoordinates(coords);
        return nodeCoords;
    }

    /**
     * Split the links at a certain point along the link.
     * @param splitNodeTag NodeTag; Node located at the intersection point of the connector and the link
     * @param linkTag LinkTag; Tag of the link that meets the connector
     * @param parser VissimNetworkLaneParser; the VissimParser with info to create a network
     * @param splitPosition Double; position at the link where the split is expected
     * @param margin Double; if the splitPosition is at the start or end of the LinkTag, the connector is supposed to be a chain
     *            and not a split
     * @param isConnectorToLink boolean; is this is a connector towards a link (true) or starting from a link (false)
     * @return Mat&lt;String, LinkTab&gt;
     * @throws OTSGeometryException ...
     */
    public static Map<String, LinkTag> splitLink(final NodeTag splitNodeTag, final LinkTag linkTag,
            final VissimNetworkLaneParser parser, final Double splitPosition, final Double margin,
            final boolean isConnectorToLink) throws OTSGeometryException
    {

        // generate a LineString of the "real" Link
        OTSLine3D designLineOTS = createLineString(linkTag);
        LineString designLine = designLineOTS.getLineString();

        // only split if the splitPosition is not:
        // (1) at or very near the start of a link
        // (2) at or very near the end of a link
        if (splitPosition > margin && splitPosition < designLine.getLength() - margin)
        {
            // split the geometry in two parts (cut by the connector)
            LineString designLineStart = SubstringLine.getSubstring(designLine, 0.0, splitPosition);
            LineString designLineEnd = SubstringLine.getSubstring(designLine, splitPosition, designLine.getLength());

            // the linkTag is split in two parts....
            // first create a copy of the current toLink
            LinkTag endLinkTag = new LinkTag(linkTag);
            // add a unique name
            String linkName = "" + parser.getUpperLinkNr();
            parser.setUpperLinkNr(parser.getUpperLinkNr() + 1);
            endLinkTag.name = linkName;

            // the first link has the same characteristics as the old link, but implements a new endNode (the endNode of the
            // Connector) and an updated geometry.
            // the Second link (endLinkTag) copies the characteristics from the old link, but creates a new startNode, endNode
            // and geometry.
            createGeometryStartLink(linkTag, designLineStart, splitNodeTag);
            createGeometryEndLink(endLinkTag, designLineEnd, splitNodeTag);

            // Furthermore, the signalHeads and sensors are moved to one of the new links
            // First, relocate the signalheads over the links that are split
            Iterator<SignalHeadTag> signalHeads = linkTag.signalHeads.iterator();
            while (signalHeads.hasNext())
            {
                SignalHeadTag signalHeadTag = signalHeads.next();
                Double position = Double.parseDouble(signalHeadTag.positionStr);
                if (position > splitPosition)
                {
                    // update the position of the signalHead!
                    Double newPosition = position - splitPosition;
                    signalHeadTag.positionStr = newPosition.toString();
                    // add to the newly constructed link
                    endLinkTag.signalHeads.add(new SignalHeadTag(signalHeadTag));
                    // remove from the other part of the link
                    signalHeadTag.activeOnThisLink = false;
                    linkTag.signalHeadsToRemove.add(signalHeadTag);
                }
            }

            // relocate the signalheads over the links that are split
            Iterator<SensorTag> sensors = linkTag.sensors.iterator();
            while (sensors.hasNext())
            {
                SensorTag sensorTag = sensors.next();
                Double position = Double.parseDouble(sensorTag.positionStr);
                if (position > splitPosition)
                {
                    // update the position of the Sensor!
                    Double newPosition = position - splitPosition;
                    sensorTag.positionStr = newPosition.toString();
                    // add to the newly constructed link
                    endLinkTag.sensors.add(new SensorTag(sensorTag));
                    // remove from the other part of the link
                    sensorTag.activeOnThisLink = false;
                    linkTag.sensorTagsToRemove.add(sensorTag);
                    // sensors.remove();
                }
            }

            // put this link in the map with LinkTags
            Map<String, LinkTag> newLinkTags = new LinkedHashMap<>();
            newLinkTags.put(endLinkTag.name, endLinkTag);
            return newLinkTags;

        }
        return null;

    }

    /**
     * Creates the Node info for start and end node and intermediate vertices of the StartLink
     * @param linkTag LinkTag;
     * @param designLine LineString;
     * @param nodeTag NodeTag;
     */
    private static void createGeometryStartLink(LinkTag linkTag, LineString designLine, NodeTag nodeTag)
    {
        Coordinate[] coords = designLine.getCoordinates();
        OTSPoint3D[] vertices = new OTSPoint3D[coords.length - 2];
        int i = 0;

        for (Coordinate coord : coords)
        {
            // startNode point
            if (i == 0)
            {
                linkTag.nodeStartTag.coordinate = new OTSPoint3D(coord);
            }
            // endNode point
            if (i == coords.length - 1)
            {
                nodeTag.coordinate = new OTSPoint3D(coord);
                linkTag.nodeEndTag = nodeTag;
            }
            if (coords.length > 2 && (i > 0 && i < coords.length - 1))
            {
                vertices[i - 1] = new OTSPoint3D(coord);
            }
            i++;
        }
        if (linkTag.polyLineTag != null)
        {
            if (coords.length <= 2)
            {
                linkTag.polyLineTag = null;
                linkTag.straightTag = new StraightTag();
            }
            else
            {
                linkTag.polyLineTag.vertices = vertices;
            }
        }
    }

    /**
     * Creates the Node info for start and end node and intermediate vertices of the EndLink
     * @param linkTag LinkTag;
     * @param designLine LineString;
     * @param nodeTag NodeTag;
     */
    private static void createGeometryEndLink(LinkTag linkTag, LineString designLine, NodeTag nodeTag)
    {
        Coordinate[] coords = designLine.getCoordinates();
        OTSPoint3D[] vertices = new OTSPoint3D[coords.length - 2];
        int i = 0;
        for (Coordinate coord : coords)
        {
            if (i == 0)
            {
                nodeTag.coordinate = new OTSPoint3D(coord);
                linkTag.nodeStartTag = nodeTag;
            }
            if (i == coords.length - 1)
            {
                linkTag.nodeEndTag.coordinate = new OTSPoint3D(coord);
            }
            if (coords.length > 2 && (i > 0 && i < coords.length - 1))
            {
                vertices[i - 1] = new OTSPoint3D(coord);
            }
            i++;
        }
        if (linkTag.polyLineTag != null)
        {
            if (coords.length <= 2)
            {
                linkTag.polyLineTag = null;
                linkTag.straightTag = new StraightTag();
            }
            else
            {
                linkTag.polyLineTag.vertices = vertices;
            }
        }
    }

    private static void createGeometryShortenedLink(LinkTag linkTag, LineString designLine)
    {
        Coordinate[] coords = designLine.getCoordinates();
        OTSPoint3D[] vertices = new OTSPoint3D[coords.length - 2];
        int i = 0;
        for (Coordinate coord : coords)
        {
            if (i == 0)
            {
                linkTag.nodeStartTag.coordinate = new OTSPoint3D(coord);
            }
            if (i == coords.length - 1)
            {
                linkTag.nodeEndTag.coordinate = new OTSPoint3D(coord);
            }
            if (coords.length > 2 && (i > 0 && i < coords.length - 1))
            {
                vertices[i - 1] = new OTSPoint3D(coord);
            }
            i++;
        }
        if (linkTag.polyLineTag != null)
        {
            if (coords.length <= 2)
            {
                linkTag.polyLineTag = null;
                linkTag.straightTag = new StraightTag();
            }
            else
            {
                linkTag.polyLineTag.vertices = vertices;
            }
        }
    }

    public static OTSLine3D createLineString(LinkTag linkTag) throws OTSGeometryException
    {
        OTSPoint3D[] coordinates = null;
        if (linkTag.straightTag != null)
        {
            coordinates = new OTSPoint3D[2];
            coordinates[0] = linkTag.nodeStartTag.coordinate;
            coordinates[1] = linkTag.nodeEndTag.coordinate;
        }
        else if (linkTag.polyLineTag != null)
        {
            int intermediatePoints = linkTag.polyLineTag.vertices.length;
            coordinates = new OTSPoint3D[intermediatePoints + 2];
            coordinates[0] = linkTag.nodeStartTag.coordinate;
            coordinates[intermediatePoints + 1] = linkTag.nodeEndTag.coordinate;
            for (int p = 0; p < intermediatePoints; p++)
            {
                coordinates[p + 1] = linkTag.polyLineTag.vertices[p];
            }
        }
        return OTSLine3D.createAndCleanOTSLine3D(coordinates);

    }

    /**
     * This method parses a length string that can have values such as: BEGIN, END, 10m, END-10m, 98%. Only use the method after
     * the length of the cross section elements is known!
     * @param posStr String; the position string to parse. Lengths are relative to the center line of the cross section element.
     * @param cse CrossSectionElement; the cross section element to retrieve the center line
     * @return the corresponding position as a length on the center line
     * @throws NetworkException when parsing fails
     */
    static Length parseBeginEndPosition(final String posStr, final CrossSectionElement cse) throws NetworkException
    {
        if (posStr.trim().equals("BEGIN"))
        {
            return new Length(0.0, LengthUnit.METER);
        }

        double length = cse.getCenterLine().getLengthSI();

        if (posStr.trim().equals("END"))
        {
            return new Length(length, LengthUnit.METER);
        }

        if (posStr.endsWith("%"))
        {
            String s = posStr.substring(0, posStr.length() - 1).trim();
            try
            {
                double fraction = Double.parseDouble(s) / 100.0;
                if (fraction < 0.0 || fraction > 1.0)
                {
                    throw new NetworkException("parseBeginEndPosition: attribute POSITION with value " + posStr
                            + " invalid for lane " + cse.toString() + ", should be a percentage between 0 and 100%");
                }
                return new Length(length * fraction, LengthUnit.METER);
            }
            catch (NumberFormatException nfe)
            {
                throw new NetworkException("parseBeginEndPosition: attribute POSITION with value " + posStr
                        + " invalid for lane " + cse.toString() + ", should be a percentage between 0 and 100%", nfe);
            }
        }

        if (posStr.trim().startsWith("END-"))
        {
            String s = posStr.substring(4).trim();
            double offset = Length.valueOf(s).getSI();
            if (offset > length)
            {
                throw new NetworkException("parseBeginEndPosition - attribute POSITION with value " + posStr
                        + " invalid for lane " + cse.toString() + ": provided negative offset greater than than link length");
            }
            return new Length(length - offset, LengthUnit.METER);
        }

        Length offset = Length.valueOf(posStr);
        if (offset.getSI() > length)
        {
            throw new NetworkException("parseBeginEndPosition - attribute POSITION with value " + posStr + " invalid for lane "
                    + cse.toString() + ": provided offset greater than than link length");
        }
        return offset;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LinkTag [name=" + this.name + "]";
    }

    /**
     * @param parser VissimNetworkLaneParser; the VissimParser with info to create a network
     */
    public static void addSignalHeads(VissimNetworkLaneParser parser)
    {
        for (SignalHeadTag signalHeadTag : parser.getSignalHeadTags().values())
        {
            parser.getLinkTags().get(signalHeadTag.linkName).signalHeads.add(signalHeadTag);
        }
    }

    /**
     * @param parser VissimNetworkLaneParser; the VissimParser with info to create a network
     */
    public static void addDetectors(VissimNetworkLaneParser parser)
    {
        for (SensorTag sensorTag : parser.getSensorTags().values())
        {
            parser.getLinkTags().get(sensorTag.linkName).sensors.add(sensorTag);
        }
    }

    /**
     * @param parser VissimNetworkLaneParser; the VissimParser with info to create a network
     * @throws OTSGeometryException:
     * @throws NamingException:
     * @throws NetworkException:
     */
    public static void shortenConnectors(VissimNetworkLaneParser parser)
            throws OTSGeometryException, NetworkException, NamingException
    {
        for (LinkTag connectorTag : parser.getConnectorTags().values())
        {
            OTSLine3D designLineOTS = LinkTag.createLineString(connectorTag);
            LineString designLine = designLineOTS.getLineString();
            Double length = designLine.getLength();
            Double decreaseLength = length / 3.0;
            LineString shortenedDesignLine = SubstringLine.getSubstring(designLine, decreaseLength, length - decreaseLength);
            createGeometryShortenedLink(connectorTag, shortenedDesignLine);
            // make a new OTS node, because the coordinates have changed
            connectorTag.nodeStartTag.node = NodeTag.makeOTSNode(connectorTag.nodeStartTag, parser);
            connectorTag.nodeEndTag.node = NodeTag.makeOTSNode(connectorTag.nodeEndTag, parser);
        }
    }

}

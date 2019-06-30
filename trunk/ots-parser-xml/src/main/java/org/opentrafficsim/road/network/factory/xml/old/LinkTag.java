package org.opentrafficsim.road.network.factory.xml.old;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.AngleUnits;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;
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
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
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

    /** From node tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    NodeTag nodeStartTag = null;

    /** To node tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    NodeTag nodeEndTag = null;

    /** Road type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    RoadLayoutTag roadLayoutTag = null;

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

    /** PolyLine. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    PolyLineTag polyLineTag = null;

    /** Arc. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ArcTag arcTag = null;

    /** Bezier. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    BezierTag bezierTag = null;

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

    /** Map of lane name to traffic lights. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, List<TrafficLightTag>> trafficLightTags = new LinkedHashMap<>();

    /** Map of lane name to fill at t=0. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, FillTag> fillTags = new LinkedHashMap<>();

    /** Map of lane name to sink tags. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, SinkTag> sinkTags = new LinkedHashMap<>();

    /** Map of lane name to generated lanes. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, Lane> lanes = new LinkedHashMap<>();

    /** The calculated Link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    CrossSectionLink link = null;

    /** The lane keeping policy, i.e., keep left, keep right or keep lane. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LaneKeepingPolicy laneKeepingPolicy = null;

    /** The priority for conflicts. */
    Priority priority = null;

    /**
     * Parse the LINK tags.
     * @param nodeList NodeList; nodeList the top-level nodes of the XML-file
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseLinks(final NodeList nodeList, final XmlNetworkLaneParserOld parser) throws SAXException, NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "LINK"))
        {
            NamedNodeMap attributes = node.getAttributes();
            LinkTag linkTag = new LinkTag();

            if (attributes.getNamedItem("NAME") == null)
                throw new SAXException("LINK: missing attribute NAME");
            linkTag.name = attributes.getNamedItem("NAME").getNodeValue().trim();
            if (parser.connectorTags.keySet().contains(linkTag.name) || parser.linkTags.keySet().contains(linkTag.name))
                throw new SAXException("LINK: NAME " + linkTag.name + " defined twice");

            if (attributes.getNamedItem("ROADLAYOUT") == null)
                throw new SAXException("LINK: missing attribute ROADLAYOUT for link " + linkTag.name);
            String roadTypeName = attributes.getNamedItem("ROADLAYOUT").getNodeValue().trim();
            if (!parser.roadLayoutTags.containsKey(roadTypeName))
                throw new SAXException("LINK: ROADLAYOUT " + roadTypeName + " not found for link " + linkTag.name);
            linkTag.roadLayoutTag = parser.roadLayoutTags.get(roadTypeName);

            if (attributes.getNamedItem("NODESTART") == null)
                throw new SAXException("LINK: missing attribute NODESTART for link " + linkTag.name);
            String fromNodeStr = attributes.getNamedItem("NODESTART").getNodeValue().trim();
            linkTag.nodeStartTag = parser.nodeTags.get(fromNodeStr);
            if (linkTag.nodeStartTag == null)
                throw new SAXException("LINK: NODESTART node " + fromNodeStr + " for link " + linkTag.name + " not defined");

            if (attributes.getNamedItem("NODEEND") == null)
                throw new SAXException("LINK: missing attribute NODEEND for link " + linkTag.name);
            String toNodeStr = attributes.getNamedItem("NODEEND").getNodeValue().trim();
            linkTag.nodeEndTag = parser.nodeTags.get(toNodeStr);
            if (linkTag.nodeEndTag == null)
                throw new SAXException("LINK: NODEEND node " + toNodeStr + " for link " + linkTag.name + " not defined");

            if (attributes.getNamedItem("OFFSETSTART") != null)
                linkTag.offsetStart = LengthUnits.parseLength(attributes.getNamedItem("OFFSETSTART").getNodeValue());

            if (attributes.getNamedItem("OFFSETEND") != null)
                linkTag.offsetEnd = LengthUnits.parseLength(attributes.getNamedItem("OFFSETEND").getNodeValue());

            if (attributes.getNamedItem("ROTATIONSTART") != null)
                linkTag.rotationStart = AngleUnits.parseAngle(attributes.getNamedItem("ROTATIONSTART").getNodeValue());

            if (attributes.getNamedItem("ROTATIONEND") != null)
                linkTag.rotationEnd = AngleUnits.parseAngle(attributes.getNamedItem("ROTATIONEND").getNodeValue());

            if (attributes.getNamedItem("PRIORITY") != null)
                linkTag.priority = LaneAttributes.parsePriority(attributes.getNamedItem("PRIORITY").getNodeValue());

            Node lkp = attributes.getNamedItem("LANEKEEPING");
            if (lkp != null)
                linkTag.laneKeepingPolicy = LaneAttributes.parseLaneKeepingPolicy(lkp.getNodeValue().trim());
            else if (linkTag.roadLayoutTag.laneKeepingPolicy != null)
                linkTag.laneKeepingPolicy = linkTag.roadLayoutTag.laneKeepingPolicy;
            else if (linkTag.roadLayoutTag.roadTypeTag.defaultLaneKeepingPolicy != null)
                linkTag.laneKeepingPolicy = linkTag.roadLayoutTag.roadTypeTag.defaultLaneKeepingPolicy;
            else
                throw new SAXException("LINK: cannot determine LANEKEEPING for lane: " + linkTag.name);

            List<Node> straightNodes = XMLParser.getNodes(node.getChildNodes(), "STRAIGHT");
            List<Node> polyLineNodes = XMLParser.getNodes(node.getChildNodes(), "POLYLINE");
            List<Node> arcNodes = XMLParser.getNodes(node.getChildNodes(), "ARC");
            List<Node> bezierNodes = XMLParser.getNodes(node.getChildNodes(), "BEZIER");
            if (straightNodes.size() > 1)
                throw new SAXException("LINK: more than one STRAIGHT tag for link " + linkTag.name);
            if (polyLineNodes.size() > 1)
                throw new SAXException("LINK: more than one POLYLINE tag for link " + linkTag.name);
            if (bezierNodes.size() > 1)
                throw new SAXException("LINK: more than one BEZIER tag for link " + linkTag.name);
            if (arcNodes.size() > 1)
                throw new SAXException("LINK: more than one ARC tag for link " + linkTag.name);
            if (straightNodes.size() + polyLineNodes.size() + arcNodes.size() + bezierNodes.size() > 1)
                throw new SAXException("LINK: multiple tags (STRAIGHT, POLYLINE, ARC, BEZIER) for link " + linkTag.name);
            if (straightNodes.size() + polyLineNodes.size() + arcNodes.size() + bezierNodes.size() == 0)
                throw new SAXException("LINK: no tags (STRAIGHT, POLYLINE, ARC, BEZIER) for link " + linkTag.name);

            // parse the STRAIGHT tag
            if (straightNodes.size() == 1)
                StraightTag.parseStraight(straightNodes.get(0), parser, linkTag);

            // parse the POLYLINE tag
            if (polyLineNodes.size() == 1)
                PolyLineTag.parsePolyLine(polyLineNodes.get(0), parser, linkTag);

            // parse the ARC tags
            if (arcNodes.size() == 1)
                ArcTag.parseArc(arcNodes.get(0), parser, linkTag);

            // parse the BEZIER tag
            if (bezierNodes.size() == 1)
                BezierTag.parseBezier(bezierNodes.get(0), parser, linkTag);

            parser.linkTags.put(linkTag.name, linkTag);

            // parse the LANEOVERRIDE tags
            for (Node loNode : XMLParser.getNodes(node.getChildNodes(), "LANEOVERRIDE"))
            {
                LaneOverrideTag.parseLaneOverride(loNode, parser, linkTag);
            }

            // parse the GENERATOR tags
            for (Node genNode : XMLParser.getNodes(node.getChildNodes(), "GENERATOR"))
            {
                GeneratorTag.parseGenerator(genNode, parser, linkTag);
            }

            // parse the LISTGENERATOR tags
            for (Node listGenNode : XMLParser.getNodes(node.getChildNodes(), "LISTGENERATOR"))
            {
                ListGeneratorTag.parseListGenerator(listGenNode, parser, linkTag);
            }

            // parse the SENSOR tags
            for (Node sensorNode : XMLParser.getNodes(node.getChildNodes(), "SENSOR"))
            {
                SensorTag.parseSensor(sensorNode, parser, linkTag);
            }

            // parse the TRAFFICLIGHT tags
            for (Node trafficLightNode : XMLParser.getNodes(node.getChildNodes(), "TRAFFICLIGHT"))
            {
                TrafficLightTag.parseTrafficLight(trafficLightNode, parser, linkTag);
            }

            // parse the SINK tags
            for (Node sinkNode : XMLParser.getNodes(node.getChildNodes(), "SINK"))
            {
                SinkTag.parseSink(sinkNode, parser, linkTag);
            }

            // parse the FILL tags
            for (Node fillNode : XMLParser.getNodes(node.getChildNodes(), "FILL"))
            {
                FillTag.parseFill(fillNode, parser, linkTag);
            }

        }
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
            double offset = LengthUnits.parseLength(s).getSI();
            if (offset > length)
            {
                throw new NetworkException("parseBeginEndPosition - attribute POSITION with value " + posStr
                        + " invalid for lane " + cse.toString() + ": provided negative offset greater than than link length");
            }
            return new Length(length - offset, LengthUnit.METER);
        }

        Length offset = LengthUnits.parseLength(posStr);
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
        return "LinkTag [name=" + this.name + ", nodeStartTag=" + this.nodeStartTag + ", nodeEndTag=" + this.nodeEndTag + "]";
    }

}

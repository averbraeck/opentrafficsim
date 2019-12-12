package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
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
class LaneTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150723L;

    /** Id of the lane. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Integer id = null;

    /** Type */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String type = null;

    /** Level */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String level = null;

    /** Successor lane Id. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String successorId = null;

    /** Predecessor lane Id. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String predecessorId = null;

    /** Width Tag */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<WidthTag> widthTags = new ArrayList<>();

    /** RoadMark Tag */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<RoadMarkTag> roadMarkTags = new ArrayList<>();

    /** Speed Tag */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<SpeedTag> speedTags = new ArrayList<>();

    /** Height Tag */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    HeightTag heightTag = null;

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param node Node; the top-level road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @return the generated RoadTag for further reference
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static LaneTag parseLane(final Node node, final OpenDriveNetworkLaneParserOld parser) throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        LaneTag laneTag = new LaneTag();

        Node id = attributes.getNamedItem("id");
        if (id == null)
            throw new SAXException("LANE: missing attribute id");
        laneTag.id = Integer.parseInt(id.getNodeValue().trim());

        Node type = attributes.getNamedItem("type");
        if (type == null)
            throw new SAXException("LANE: missing attribute type");
        laneTag.type = type.getNodeValue().trim();

        Node level = attributes.getNamedItem("level");
        if (level == null)
            throw new SAXException("LANE: missing attribute level");
        laneTag.level = level.getNodeValue().trim();

        for (Node link : XMLParser.getNodes(node.getChildNodes(), "link"))
        {
            NamedNodeMap attributes1 = link.getAttributes();

            for (Node successor : XMLParser.getNodes(link.getChildNodes(), "successor"))
            {
                NamedNodeMap attributes2 = successor.getAttributes();
                Node successorId = attributes2.getNamedItem("id");
                laneTag.successorId = successorId.getNodeValue().trim();
            }

            for (Node predecessor : XMLParser.getNodes(link.getChildNodes(), "predecessor"))
            {
                NamedNodeMap attributes2 = predecessor.getAttributes();
                Node predecessorId = attributes2.getNamedItem("id");
                laneTag.predecessorId = predecessorId.getNodeValue().trim();
            }
        }

        WidthTag.parseWidth(node.getChildNodes(), parser, laneTag);
        RoadMarkTag.parseRoadMark(node.getChildNodes(), parser, laneTag);
        SpeedTag.parseSpeed(node.getChildNodes(), parser, laneTag);
        HeightTag.parseHeight(node.getChildNodes(), parser, laneTag);

        return laneTag;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneTag [id=" + this.id + ", type=" + this.type + ", level=" + this.level + ", successorId=" + this.successorId
                + ", predecessorId=" + this.predecessorId + ", widthTags=" + this.widthTags + ", roadMarkTags="
                + this.roadMarkTags + ", speedTags=" + this.speedTags + ", heightTag=" + this.heightTag + "]";
    }
}

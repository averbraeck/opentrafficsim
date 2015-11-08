package org.opentrafficsim.road.network.factory.opendrive;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.XMLParser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
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
class LaneTag 
{

    /** id of the lane. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Integer id = null;
    
    /** type */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String type = null;
    
    /** level */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String level = null;
    
    /** successor lane Id. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String successorId = null;
    
    /** predecessor lane Id. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String predecessorId = null;

    /** width Tag */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    WidthTag widthTag = null;

    /** RoadMark Tag */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<RoadMarkTag> roadMarkTags = new ArrayList<>();
    
    /** Speed Tag */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<SpeedTag> speedTags = new ArrayList<>();
    
    /** height Tag */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    HeightTag heightTag = null;

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param node the top-level road node
     * @param parser the parser with the lists of information
     * @return the generated RoadTag for further reference
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static LaneTag parseLane(final Node node, final OpenDriveNetworkLaneParser parser) throws SAXException, NetworkException
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
}

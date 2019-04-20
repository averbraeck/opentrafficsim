package org.opentrafficsim.road.network.factory.opendrive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.XMLParser;
import org.opentrafficsim.road.network.lane.Lane;
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
class LaneSectionTag 
{

    /** sequence of the laneSection. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    int id = 0;
    
    /** start position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel s = null;       

    /** left lanes */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<Integer, LaneTag> leftLaneTags = new HashMap<Integer, LaneTag>();

    /** center lanes */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<Integer, LaneTag> centerLaneTags = new HashMap<Integer, LaneTag>();

    /** right lanes */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<Integer, LaneTag> rightLaneTags = new HashMap<Integer, LaneTag>();
    
    /** all lanes */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<Integer, Lane> lanes = new HashMap<Integer, Lane>();

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param node the top-level road node
     * @param parser the parser with the lists of information
     * @return the generated RoadTag for further reference
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static LaneSectionTag parseLaneSection(final Node node, final OpenDriveNetworkLaneParser parser) throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        LaneSectionTag laneSectionTag = new LaneSectionTag();

        Node s = attributes.getNamedItem("s");
        if (s == null)
            throw new SAXException("LaneSection: missing attribute s");
        laneSectionTag.s = new Length.Rel(Double.parseDouble(s.getNodeValue().trim()), LengthUnit.METER);

        for (Node leftNode : XMLParser.getNodes(node.getChildNodes(), "left"))
            for (Node laneNode : XMLParser.getNodes(leftNode.getChildNodes(), "lane"))
            {
                LaneTag laneTag = LaneTag.parseLane(laneNode, parser);
                laneSectionTag.leftLaneTags.put(laneTag.id, laneTag);
            }

        for (Node centerNode : XMLParser.getNodes(node.getChildNodes(), "center"))
            for (Node laneNode : XMLParser.getNodes(centerNode.getChildNodes(), "lane"))
            {
                LaneTag laneTag = LaneTag.parseLane(laneNode, parser);
                laneSectionTag.centerLaneTags.put(laneTag.id, laneTag);
            }
        
        for (Node rightNode : XMLParser.getNodes(node.getChildNodes(), "right"))
            for (Node laneNode : XMLParser.getNodes(rightNode.getChildNodes(), "lane"))
            {
                LaneTag laneTag = LaneTag.parseLane(laneNode, parser);
                laneSectionTag.rightLaneTags.put(laneTag.id, laneTag);
            }

        return laneSectionTag;
    }

    /**
     * @param orientation
     * @return lanes
     */
    public List<Lane> findLanes(String orientation)
    {
        List<Lane> lanes1 = new ArrayList<Lane>();
        for(int key: this.lanes.keySet())
        {
            Lane lane = this.lanes.get(key);
            if(key < 0)
            {
                if(orientation.equals("+") && this.rightLaneTags.get(key).type.equals("driving"))
                    lanes1.add(lane);
            }
            else if(key > 0)
            {
                if(orientation.equals("-") && this.leftLaneTags.get(key).type.equals("driving"))
                    lanes1.add(lane);
            }
                    
        }
        if(lanes1.size() != 1)
            System.err.println("Exception in finding lanes");
        return lanes1;
    }
}
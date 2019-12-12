package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.Lane;
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
class LaneSectionTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150723L;

    /** Sequence of the laneSection. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    int id = 0;

    /** Start position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length s = null;

    /** Left lanes */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<Integer, LaneTag> leftLaneTags = new LinkedHashMap<Integer, LaneTag>();

    /** Center lanes */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<Integer, LaneTag> centerLaneTags = new LinkedHashMap<Integer, LaneTag>();

    /** Right lanes */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<Integer, LaneTag> rightLaneTags = new LinkedHashMap<Integer, LaneTag>();

    /** All lanes */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<Integer, Lane> lanes = new LinkedHashMap<Integer, Lane>();

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param node Node; the top-level road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @return the generated RoadTag for further reference
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static LaneSectionTag parseLaneSection(final Node node, final OpenDriveNetworkLaneParserOld parser)
            throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        LaneSectionTag laneSectionTag = new LaneSectionTag();

        Node s = attributes.getNamedItem("s");
        if (s == null)
            throw new SAXException("LaneSection: missing attribute s");
        laneSectionTag.s = new Length(Double.parseDouble(s.getNodeValue().trim()), LengthUnit.METER);

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
     * @param orientation String; Plus or minus orientation, indicated by '+' or '-'
     * @return lanes a list of lanes in the given orientation
     */
    public List<Lane> findLanes(String orientation)
    {
        List<Lane> lanes1 = new ArrayList<Lane>();
        for (int key : this.lanes.keySet())
        {
            Lane lane = this.lanes.get(key);
            if (key < 0)
            {
                if (orientation.equals("+") && this.rightLaneTags.get(key).type.equals("driving"))
                    lanes1.add(lane);
            }
            else if (key > 0)
            {
                if (orientation.equals("-") && this.leftLaneTags.get(key).type.equals("driving"))
                    lanes1.add(lane);
            }

        }
        if (lanes1.size() != 1)
            System.err.println("Exception in finding lanes");
        return lanes1;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneSectionTag [id=" + this.id + ", s=" + this.s + ", leftLaneTags=" + this.leftLaneTags + ", centerLaneTags="
                + this.centerLaneTags + ", rightLaneTags=" + this.rightLaneTags + ", lanes=" + this.lanes + "]";
    }
}

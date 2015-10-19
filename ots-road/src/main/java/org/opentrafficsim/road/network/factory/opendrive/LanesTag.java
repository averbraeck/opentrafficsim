package org.opentrafficsim.road.network.factory.opendrive;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.XMLParser;
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
class LanesTag
{

    /** geometryTags */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<LaneSectionTag> laneSectionTags = new ArrayList<LaneSectionTag>();

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param nodeList the list of subnodes of the road node
     * @param parser the parser with the lists of information
     * @param roadTag the RoadTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseLanes(final NodeList nodeList, final OpenDriveNetworkLaneParser parser, final RoadTag roadTag)
        throws SAXException, NetworkException
    {        
        int laneSectionCount = 0;
        for (Node node0 : XMLParser.getNodes(nodeList, "lanes"))
            for (Node node : XMLParser.getNodes(node0.getChildNodes(), "laneSection"))
        {
            LanesTag lanesTag = new LanesTag();
            roadTag.lanesTag = lanesTag;
            
            LaneSectionTag laneSectionTag = LaneSectionTag.parseLaneSection(node, parser);
            laneSectionTag.id = laneSectionCount;
            laneSectionCount++;

            lanesTag.laneSectionTags.add(laneSectionTag);
        }
    }
}

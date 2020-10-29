package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parser for lanes.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class LanesTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150723L;

    /** GeometryTags. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<LaneSectionTag> laneSectionTags = new ArrayList<LaneSectionTag>();

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param nodeList NodeList; the list of subnodes of the road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @param roadTag RoadTag; the RoadTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseLanes(final NodeList nodeList, final OpenDriveNetworkLaneParserOld parser, final RoadTag roadTag)
            throws SAXException, NetworkException
    {
        int laneSectionCount = 0;
        LanesTag lanesTag = new LanesTag();

        for (Node node0 : XMLParser.getNodes(nodeList, "lanes"))
            for (Node node : XMLParser.getNodes(node0.getChildNodes(), "laneSection"))
            {
                LaneSectionTag laneSectionTag = LaneSectionTag.parseLaneSection(node, parser);
                laneSectionTag.id = laneSectionCount;
                laneSectionCount++;

                lanesTag.laneSectionTags.add(laneSectionTag);
            }
        roadTag.lanesTag = lanesTag;

    }

    /**
     * @param s Length; progression on the lane in the design direction
     * @return laneSection the section belonging to 's' progression
     */
    public LaneSectionTag findDrivingLaneSec(Length s)
    {
        for (int i = 0; i < this.laneSectionTags.size(); i++)
        {
            if (i < this.laneSectionTags.size() - 1)
            {
                LaneSectionTag currentSec = this.laneSectionTags.get(i);
                LaneSectionTag nextSec = this.laneSectionTags.get(i + 1);

                if (s.si <= nextSec.s.si && s.si >= currentSec.s.si)
                {
                    return currentSec;
                }
            }
            else
            {
                LaneSectionTag currentSec = this.laneSectionTags.get(i);
                if (s.si >= currentSec.s.si)
                {
                    return currentSec;
                }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LanesTag [laneSectionTags=" + this.laneSectionTags + "]";
    }
}

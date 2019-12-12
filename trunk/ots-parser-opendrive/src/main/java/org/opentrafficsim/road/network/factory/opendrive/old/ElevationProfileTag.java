package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parser for elevation profile.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class ElevationProfileTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150723L;

    /** ElevationTags */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    NavigableMap<Double, ElevationTag> elevationTags = new TreeMap<Double, ElevationTag>();

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param nodeList NodeList; the list of subnodes of the road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @param roadTag RoadTag; the RoadTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseElevationProfile(final NodeList nodeList, final OpenDriveNetworkLaneParserOld parser, final RoadTag roadTag)
            throws SAXException, NetworkException
    {
        ElevationProfileTag elevationProfileTag = new ElevationProfileTag();

        for (Node node0 : XMLParser.getNodes(nodeList, "elevationProfile"))
            for (Node node : XMLParser.getNodes(node0.getChildNodes(), "elevation"))
            {
                ElevationTag elevationTag = ElevationTag.parseElevation(node, parser);
                elevationProfileTag.elevationTags.put(elevationTag.s.si, elevationTag);
            }
        roadTag.elevationProfileTag = elevationProfileTag;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ElevationProfileTag [elevationTags=" + this.elevationTags + "]";
    }
}

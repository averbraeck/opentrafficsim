package org.opentrafficsim.core.network.factory.xml;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.XMLParser;
import org.opentrafficsim.core.network.lane.LaneType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class LaneTypeTag
{
    /** name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /**
     * Parse the LANETYPE tag.
     * @param nodeList nodeList the top-level nodes of the XML-file
     * @param parser the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseLaneTypes(final NodeList nodeList, final XmlNetworkLaneParser parser) throws SAXException,
        NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "LANETYPE"))
        {
            NamedNodeMap attributes = node.getAttributes();
            LaneTypeTag laneTypeTag = new LaneTypeTag();

            if (attributes.getNamedItem("NAME") == null)
                throw new SAXException("LANETYPE: missing attribute NAME");
            laneTypeTag.name = attributes.getNamedItem("NAME").getNodeValue().trim();
            if (parser.laneTypes.keySet().contains(laneTypeTag.name))
                throw new SAXException("LANETYPE: NAME " + laneTypeTag.name + " defined twice");

            LaneType laneType = new LaneType(laneTypeTag.name);
            parser.laneTypes.put(laneTypeTag.name, laneType);
        }
    }
}

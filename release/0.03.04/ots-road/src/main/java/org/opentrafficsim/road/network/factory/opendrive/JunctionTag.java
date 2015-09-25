package org.opentrafficsim.road.network.factory.opendrive;

import org.opentrafficsim.core.network.NetworkException;
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
class JunctionTag
{
    /** name of the junction. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** unique ID within database. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String id = null;

    /**
     * Parse the attributes of the junction tag. The sub-elements are parsed in separate classes.
     * @param node the junction node to parse
     * @param parser the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseJunction(final Node node, final OpenDriveNetworkLaneParser parser) throws SAXException,
        NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        JunctionTag junctionTag = new JunctionTag();

        Node id = attributes.getNamedItem("id");
        if (id == null)
            throw new SAXException("JUNCTION: missing attribute ID");
        junctionTag.id = id.getNodeValue().trim();
        if (parser.roadTags.keySet().contains(junctionTag.id))
            throw new SAXException("JUNCTION: ID " + junctionTag.id + " defined twice");

        Node name = attributes.getNamedItem("name");
        if (name == null)
            throw new SAXException("JUNCTION: missing attribute NAME for ID=" + junctionTag.id);
        junctionTag.name = name.getNodeValue().trim();

        // TODO parse junction.connection
        // TODO parse junction.laneLink
        // TODO parse junction.priority
        // TODO parse junction.controller
    }
}

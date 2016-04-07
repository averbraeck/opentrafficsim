package org.opentrafficsim.road.network.factory.xml;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.XMLParser;
import org.opentrafficsim.road.network.lane.LaneType;
import org.w3c.dom.NamedNodeMap;
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
class CompatibilityTag
{
    /** Lane type name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String laneTypeName = null;

    /** Compatible gtu types. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<GTUTag> gtuList = new ArrayList<GTUTag>();

    /**
     * Parse the COMPATIBILITY and COMPATIBILITY.LANETYPE tag.
     * @param nodeList nodeList the top-level nodes of the XML-file
     * @param parser the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseCompatibilities(final NodeList nodeList, final XmlNetworkLaneParser parser) throws SAXException,
        NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "COMPATIBILITY"))
        {
            NamedNodeMap attributes = node.getAttributes();
            CompatibilityTag compatibilityTag = new CompatibilityTag();

            if (attributes.getNamedItem("LANETYPE") == null)
                throw new SAXException("COMPATIBILITY: missing attribute LANETYPE");
            compatibilityTag.laneTypeName = attributes.getNamedItem("LANETYPE").getNodeValue().trim();
            if (!parser.laneTypes.keySet().contains(compatibilityTag.laneTypeName))
                throw new SAXException("COMPATIBILITY: LANETYPE " + compatibilityTag.laneTypeName
                    + " not defined in LANETYPE element");

            if (attributes.getNamedItem("GTULIST") == null)
                throw new SAXException("COMPATIBILITY.LANETYPE: missing attribute GTULIST for LANETYPE="
                    + compatibilityTag.laneTypeName);
            compatibilityTag.gtuList = parseGTUList(attributes.getNamedItem("GTULIST").getNodeValue(), parser);

            addLaneType(compatibilityTag, parser);
        }
    }

    /**
     * @param gtuNames the GTU names as a space-separated String
     * @param parser the parser with the lists of information
     * @return a list of GTUTags
     * @throws SAXException when node could not be found
     */
    private static List<GTUTag> parseGTUList(final String gtuNames, final XmlNetworkLaneParser parser)
        throws SAXException
    {
        List<GTUTag> gtuList = new ArrayList<>();
        String[] ns = gtuNames.split("\\s");
        for (String s : ns)
        {
            if (!parser.gtuTags.containsKey(s))
            {
                throw new SAXException("GTU " + s + " from GTU list [" + gtuNames + "] was not defined");
            }
            gtuList.add(parser.gtuTags.get(s));
        }
        return gtuList;
    }

    /**
     * Add a compatibility type for this lane type.
     * @param laneTypeTag the parsed LaneTypeTag that contains a relation between a LaneType and one or more GTUTypes.
     * @param parser the parser with the lists of information
     */
    private static void addLaneType(final CompatibilityTag laneTypeTag, final XmlNetworkLaneParser parser)
    {
        if (!parser.laneTypes.containsKey(laneTypeTag.laneTypeName))
        {
            LaneType laneType = new LaneType(laneTypeTag.laneTypeName);
            parser.laneTypes.put(laneTypeTag.laneTypeName, laneType);
        }
        for (GTUTag gtuTag : laneTypeTag.gtuList)
        {
            parser.laneTypes.get(laneTypeTag.laneTypeName).addCompatibility(gtuTag.gtuType);
        }
    }

}

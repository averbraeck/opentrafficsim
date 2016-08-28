package org.opentrafficsim.road.network.factory.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.XMLParser;
import org.opentrafficsim.road.network.lane.LaneType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class CompatibilityTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

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
    static void parseCompatibilities(final NodeList nodeList, final XmlNetworkLaneParser parser)
            throws SAXException, NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "COMPATIBILITY"))
        {
            NamedNodeMap attributes = node.getAttributes();
            CompatibilityTag compatibilityTag = new CompatibilityTag();

            if (attributes.getNamedItem("LANETYPE") == null)
                throw new SAXException("COMPATIBILITY: missing attribute LANETYPE");
            compatibilityTag.laneTypeName = attributes.getNamedItem("LANETYPE").getNodeValue().trim();
            if (!parser.laneTypeTags.keySet().contains(compatibilityTag.laneTypeName))
                throw new SAXException(
                        "COMPATIBILITY: LANETYPE " + compatibilityTag.laneTypeName + " not defined in LANETYPE element");

            List<Node> gtuList = XMLParser.getNodes(node.getChildNodes(), "GTU");
            if (gtuList.size() == 0)
                throw new SAXException("GTUMIX: missing tag GTU");
            for (Node gtuNode : gtuList)
            {
                parseCompatibilityGTUTag(gtuNode, parser, compatibilityTag);
            }

            addLaneType(compatibilityTag, parser);
        }
    }

    /**
     * @param gtuNode the GTU node to parse
     * @param parser the parser with the lists of information
     * @param compatibilityTag the parent tag for adding the gtus to the list
     * @throws NetworkException when the parsing of the GTUs fails
     * @throws SAXException when node could not be found
     */
    @SuppressWarnings("checkstyle:needbraces")
    private static void parseCompatibilityGTUTag(final Node gtuNode, final XmlNetworkLaneParser parser,
            final CompatibilityTag compatibilityTag) throws NetworkException, SAXException
    {
        NamedNodeMap attributes = gtuNode.getAttributes();

        Node gtuName = attributes.getNamedItem("NAME");
        if (gtuName == null)
            throw new NetworkException("COMPATIBILITY: No GTU NAME defined for lane type " + compatibilityTag.laneTypeName);
        if (!parser.gtuTags.containsKey(gtuName.getNodeValue().trim()))
            throw new NetworkException("COMPATIBILITY: For lane type " + compatibilityTag.laneTypeName + ", GTU "
                    + gtuName.getNodeValue().trim() + " is not defined");
        compatibilityTag.gtuList.add(parser.gtuTags.get(gtuName.getNodeValue().trim()));
    }

    /**
     * Add a compatibility type for this lane type.
     * @param laneTypeTag the parsed LaneTypeTag that contains a relation between a LaneType and one or more GTUTypes.
     * @param parser the parser with the lists of information
     */
    private static void addLaneType(final CompatibilityTag laneTypeTag, final XmlNetworkLaneParser parser)
    {
        Set<GTUType> compatibility = new HashSet<>();
        for (GTUTag gtuTag : laneTypeTag.gtuList)
        {
            compatibility.add(gtuTag.gtuType);
        }
        LaneType laneType = new LaneType(laneTypeTag.laneTypeName, compatibility);
        parser.laneTypes.put(laneType.getId(), laneType);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CompatibilityTag [laneTypeName=" + this.laneTypeName + ", gtuList=" + this.gtuList + "]";
    }

}

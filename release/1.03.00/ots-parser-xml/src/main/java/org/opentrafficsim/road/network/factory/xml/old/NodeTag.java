package org.opentrafficsim.road.network.factory.xml.old;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Direction;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.factory.xml.units.AngleUnits;
import org.opentrafficsim.core.network.factory.xml.units.Coordinates;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
class NodeTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Coordinate (null at first, can be calculated later when connected to a link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSPoint3D coordinate = null;

    /** Default direction of the node. 0 is "East", pi/2 = "North". */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Direction direction = null;

    /** The calculated Node, either through a coordinate or after calculation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSNode node = null;

    /**
     * @param nodeList NodeList; nodeList the top-level nodes of the XML-file
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @throws SAXException when parsing of GTU tag fails
     * @throws NetworkException when parsing of GTU tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseNodes(final NodeList nodeList, final XmlNetworkLaneParserOld parser) throws SAXException, NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "NODE"))
        {
            NamedNodeMap attributes = node.getAttributes();
            NodeTag nodeTag = new NodeTag();

            Node name = attributes.getNamedItem("NAME");
            if (name == null)
                throw new SAXException("NODE: missing attribute NAME");
            nodeTag.name = name.getNodeValue().trim();
            if (parser.nodeTags.keySet().contains(nodeTag.name))
                throw new SAXException("NODE: NAME " + nodeTag.name + " defined twice");

            if (attributes.getNamedItem("COORDINATE") != null)
                nodeTag.coordinate = Coordinates.parseCoordinate(attributes.getNamedItem("COORDINATE").getNodeValue());
            else
                throw new SAXException("NODE: missing attribute COORDINATE");

            if (attributes.getNamedItem("DIRECTION") != null)
                nodeTag.direction = AngleUnits.parseDirection(attributes.getNamedItem("DIRECTION").getNodeValue());

            parser.nodeTags.put(nodeTag.name, nodeTag);

            try
            {
                makeOTSNode(nodeTag, parser);
            }
            catch (NamingException exception)
            {
                throw new NetworkException(exception);
            }
        }
    }

    /**
     * Parse a list of Nodes, e.g. for a ROUTE.
     * @param nodeNames String; the space separated String with the node names
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @return a list of NodeTags
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    static List<NodeTag> parseNodeList(final String nodeNames, final XmlNetworkLaneParserOld parser)
            throws SAXException, NetworkException
    {
        List<NodeTag> nodeList = new ArrayList<>();
        String[] ns = nodeNames.split("\\s");
        for (String s : ns)
        {
            if (!parser.nodeTags.containsKey(s))
            {
                throw new SAXException("Node " + s + " from node list [" + nodeNames + "] was not defined");
            }
            nodeList.add(parser.nodeTags.get(s));
        }
        return nodeList;

    }

    /**
     * @param nodeTag NodeTag; the tag with the info for the node.
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @return a constructed node
     * @throws NetworkException when point cannot be instantiated
     * @throws NamingException when animation context cannot be found.
     */
    static OTSNode makeOTSNode(final NodeTag nodeTag, final XmlNetworkLaneParserOld parser)
            throws NetworkException, NamingException
    {
        String id = nodeTag.name;
        OTSNode node = new OTSNode(parser.network, id, nodeTag.coordinate);
        nodeTag.node = node;
        return node;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "NodeTag [name=" + this.name + ", coordinate=" + this.coordinate + ", node=" + this.node + "]";
    }

}

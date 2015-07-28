package org.opentrafficsim.core.network.factory.xml;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.factory.xml.units.AngleUnits;
import org.opentrafficsim.core.network.factory.xml.units.Coordinates;
import org.opentrafficsim.core.unit.AnglePlaneUnit;
import org.opentrafficsim.core.unit.AngleSlopeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
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
class NodeTag
{
    /** name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** coordinate (null at first, can be calculated later when connected to a link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSPoint3D coordinate = null;

    /** absolute angle of the node. 0 is "East", pi/2 = "North". */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DoubleScalar.Abs<AnglePlaneUnit> angle = null;

    /** TODO slope as an angle. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DoubleScalar.Abs<AngleSlopeUnit> slope = null;

    /** the calculated Node, either through a coordinate or after calculation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    org.opentrafficsim.core.network.Node<String> node = null;

    /**
     * @param nodeList nodeList the top-level nodes of the XML-file
     * @param parser the parser with the lists of information
     * @throws SAXException when parsing of GTU tag fails
     * @throws NetworkException when parsing of GTU tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseNodes(final NodeList nodeList, final XmlNetworkLaneParser parser) throws SAXException, NetworkException
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

            if (attributes.getNamedItem("ANGLE") != null)
                nodeTag.angle = AngleUnits.parseAngleAbs(attributes.getNamedItem("ANGLE").getNodeValue());

            // TODO slope for the Node.

            parser.nodeTags.put(nodeTag.name, nodeTag);

            if (nodeTag.coordinate != null)
            {
                // only make a node if we know the coordinate. Otherwise, wait till we can calculate it.
                try
                {
                    makeOTSNode(nodeTag, parser);
                }
                catch (RemoteException | NamingException exception)
                {
                    throw new NetworkException(exception);
                }
            }
        }
    }

    /**
     * Parse a list of Nodes, e.g. for a ROUTE.
     * @param nodeNames the space separated String with the node names
     * @param parser the parser with the lists of information
     * @return a list of NodeTags
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    static List<NodeTag> parseNodeList(final String nodeNames, final XmlNetworkLaneParser parser) throws SAXException,
        NetworkException
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
     * @param nodeTag the tag with the info for the node.
     * @param parser the parser with the lists of information
     * @return a constructed node
     * @throws NetworkException when point cannot be instantiated
     * @throws NamingException when animation context cannot be found.
     * @throws RemoteException when communication error occurs when trying to find animation context.
     */
    static org.opentrafficsim.core.network.Node<String>
        makeOTSNode(final NodeTag nodeTag, final XmlNetworkLaneParser parser) throws NetworkException, RemoteException,
            NamingException
    {
        String id = nodeTag.name;
        DoubleScalar.Abs<AnglePlaneUnit> angle =
            nodeTag.angle == null ? new DoubleScalar.Abs<AnglePlaneUnit>(0.0, AnglePlaneUnit.SI) : nodeTag.angle;
        DoubleScalar.Abs<AngleSlopeUnit> slope =
            nodeTag.slope == null ? new DoubleScalar.Abs<AngleSlopeUnit>(0.0, AngleSlopeUnit.SI) : nodeTag.slope;
        org.opentrafficsim.core.network.Node<String> node = new OTSNode<String>(id, nodeTag.coordinate, angle, slope);
        nodeTag.node = node;
        return node;
    }
}

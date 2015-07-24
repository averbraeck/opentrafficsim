package org.opentrafficsim.core.network.factory.xml;

import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
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
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 23, 2015 <br>
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
            if (parser.nodes.keySet().contains(nodeTag.name))
                throw new SAXException("NODE: NAME " + nodeTag.name + " defined twice");

            if (attributes.getNamedItem("COORDINATE") != null)
                nodeTag.coordinate = Coordinates.parseCoordinate(attributes.getNamedItem("COORDINATE").getNodeValue());

            if (attributes.getNamedItem("ANGLE") != null)
                nodeTag.angle = AngleUnits.parseAngleAbs(attributes.getNamedItem("ANGLE").getNodeValue());

            // TODO slope for the Node.
            
            parser.nodeTags.put(nodeTag.name, nodeTag);
        }
    }
}

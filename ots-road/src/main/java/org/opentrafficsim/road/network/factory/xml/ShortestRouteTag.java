package org.opentrafficsim.road.network.factory.xml;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.opentrafficsim.core.network.factory.xml.units.TimeUnits;
import org.opentrafficsim.road.network.factory.XMLParser;
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
class ShortestRouteTag
{
    /** name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** From Node. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    NodeTag from = null;

    /** Via Nodes. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<NodeTag> via = new ArrayList<NodeTag>();

    /** To Node. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    NodeTag to = null;

    /** time unit for the "cost" per time. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Frequency costPerTime = null;

    /** distance unit for the "cost" per time. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DoubleScalar.Abs<LinearDensityUnit> costPerDistance = null;

    /**
     * Parse the SHORTESTROUTE tag.
     * @param nodeList nodeList the top-level nodes of the XML-file
     * @param parser the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseShortestRoutes(final NodeList nodeList, final XmlNetworkLaneParser parser) throws SAXException,
        NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "SHORTESTROUTE"))
        {
            NamedNodeMap attributes = node.getAttributes();
            ShortestRouteTag shortestRouteTag = new ShortestRouteTag();

            if (attributes.getNamedItem("NAME") == null)
                throw new SAXException("SHORTESTROUTE: missing attribute NAME");
            shortestRouteTag.name = attributes.getNamedItem("NAME").getNodeValue().trim();
            if (parser.routeTags.keySet().contains(shortestRouteTag.name))
                throw new SAXException("SHORTESTROUTE: NAME " + shortestRouteTag.name + " defined twice");

            if (attributes.getNamedItem("FROM") == null)
                throw new SAXException("SHORTESTROUTE: missing attribute FROM");
            String fromNode = attributes.getNamedItem("FROM").getNodeValue().trim();
            if (!parser.nodeTags.containsKey(fromNode))
                throw new SAXException("SHORTESTROUTE " + shortestRouteTag.name + ": FROM node " + fromNode
                    + " not found");
            shortestRouteTag.from = parser.nodeTags.get(fromNode);

            if (attributes.getNamedItem("NODELIST") != null)
            {
                String viaNodes = attributes.getNamedItem("NODELIST").getNodeValue().trim();
                shortestRouteTag.via = NodeTag.parseNodeList(viaNodes, parser);
            }

            if (attributes.getNamedItem("TO") == null)
                throw new SAXException("SHORTESTROUTE: missing attribute TO");
            String toNode = attributes.getNamedItem("TO").getNodeValue().trim();
            if (!parser.nodeTags.containsKey(toNode.trim()))
                throw new SAXException("SHORTESTROUTE " + shortestRouteTag.name + ": TO node " + toNode + " not found");
            shortestRouteTag.to = parser.nodeTags.get(toNode);

            Node distanceCost = attributes.getNamedItem("DISTANCECOST");
            if (distanceCost == null)
                throw new SAXException("SHORTESTROUTE: missing attribute DISTANCECOST");
            shortestRouteTag.costPerDistance = LengthUnits.parsePerLengthAbs(distanceCost.getNodeValue().trim());

            Node timeCost = attributes.getNamedItem("TIMECOST");
            if (timeCost == null)
                throw new SAXException("SHORTESTROUTE: missing attribute TIMECOST");
            shortestRouteTag.costPerTime = TimeUnits.parsePerTimeAbs(timeCost.getNodeValue().trim());

            parser.shortestRouteTags.put(shortestRouteTag.name.trim(), shortestRouteTag);
        }
    }
}

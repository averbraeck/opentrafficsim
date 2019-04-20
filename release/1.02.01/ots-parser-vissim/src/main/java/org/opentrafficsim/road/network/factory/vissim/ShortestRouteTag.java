package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.DurationUnits;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class ShortestRouteTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Name. */
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

    /** Time unit for the "cost" per time. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Frequency costPerTime = null;

    /** Distance unit for the "cost" per time. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LinearDensity costPerDistance = null;

    /**
     * Parse the SHORTESTROUTE tag.
     * @param nodeList NodeList; nodeList the top-level nodes of the XML-file
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseShortestRoutes(final NodeList nodeList, final VissimNetworkLaneParser parser)
            throws SAXException, NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "SHORTESTROUTE"))
        {
            NamedNodeMap attributes = node.getAttributes();
            ShortestRouteTag shortestRouteTag = new ShortestRouteTag();

            if (attributes.getNamedItem("NAME") == null)
            {
                throw new SAXException("SHORTESTROUTE: missing attribute NAME");
            }
            shortestRouteTag.name = attributes.getNamedItem("NAME").getNodeValue().trim();
            if (parser.getRouteTags().keySet().contains(shortestRouteTag.name))
            {
                throw new SAXException("SHORTESTROUTE: NAME " + shortestRouteTag.name + " defined twice");
            }

            if (attributes.getNamedItem("FROM") == null)
            {
                throw new SAXException("SHORTESTROUTE: missing attribute FROM");
            }
            String fromNode = attributes.getNamedItem("FROM").getNodeValue().trim();
            if (!parser.getNodeTags().containsKey(fromNode))
            {
                throw new SAXException("SHORTESTROUTE " + shortestRouteTag.name + ": FROM node " + fromNode + " not found");
            }
            shortestRouteTag.from = parser.getNodeTags().get(fromNode);

            if (attributes.getNamedItem("NODELIST") != null)
            {
                String viaNodes = attributes.getNamedItem("NODELIST").getNodeValue().trim();
                shortestRouteTag.via = NodeTag.parseNodeList(viaNodes, parser);
            }

            if (attributes.getNamedItem("TO") == null)
            {
                throw new SAXException("SHORTESTROUTE: missing attribute TO");
            }
            String toNode = attributes.getNamedItem("TO").getNodeValue().trim();
            if (!parser.getNodeTags().containsKey(toNode.trim()))
            {
                throw new SAXException("SHORTESTROUTE " + shortestRouteTag.name + ": TO node " + toNode + " not found");
            }
            shortestRouteTag.to = parser.getNodeTags().get(toNode);

            Node distanceCost = attributes.getNamedItem("DISTANCECOST");
            if (distanceCost == null)
            {
                throw new SAXException("SHORTESTROUTE: missing attribute DISTANCECOST");
            }
            shortestRouteTag.costPerDistance = LengthUnits.parseLinearDensity(distanceCost.getNodeValue().trim());

            Node timeCost = attributes.getNamedItem("TIMECOST");
            if (timeCost == null)
            {
                throw new SAXException("SHORTESTROUTE: missing attribute TIMECOST");
            }
            shortestRouteTag.costPerTime = DurationUnits.parseFrequency(timeCost.getNodeValue().trim());

            parser.getShortestRouteTags().put(shortestRouteTag.name.trim(), shortestRouteTag);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ShortestRouteTag [name=" + this.name + ", from=" + this.from + ", via=" + this.via + ", to=" + this.to
                + ", costPerTime=" + this.costPerTime + ", costPerDistance=" + this.costPerDistance + "]";
    }
}

package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class ShortestRouteMixTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Routes. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<ShortestRouteTag> routes = new ArrayList<ShortestRouteTag>();

    /** Weights. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<Double> weights = new ArrayList<Double>();

    /**
     * Parse the SHORTESTROUTE tag.
     * @param nodeList NodeList; nodeList the top-level nodes of the XML-file
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseShortestRouteMix(final NodeList nodeList, final VissimNetworkLaneParser parser)
            throws SAXException, NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "SHORTESTROUTEMIX"))
        {
            NamedNodeMap attributes = node.getAttributes();
            ShortestRouteMixTag shortestRouteMixTag = new ShortestRouteMixTag();

            if (attributes.getNamedItem("NAME") == null)
            {
                throw new SAXException("SHORTESTROUTEMIX: missing attribute NAME");
            }
            shortestRouteMixTag.name = attributes.getNamedItem("NAME").getNodeValue().trim();
            if (parser.getRouteTags().keySet().contains(shortestRouteMixTag.name))
            {
                throw new SAXException("SHORTESTROUTEMIX: NAME " + shortestRouteMixTag.name + " defined twice");
            }

            List<Node> shortestRouteList = XMLParser.getNodes(node.getChildNodes(), "SHORTESTROUTE");
            if (shortestRouteList.size() == 0)
            {
                throw new SAXException("SHORTESTROUTEMIX: missing tag SHORTESTROUTE");
            }
            for (Node shortestRouteNode : shortestRouteList)
            {
                parseRouteMixRouteTag(shortestRouteNode, parser, shortestRouteMixTag);
            }

            parser.getShortestRouteMixTags().put(shortestRouteMixTag.name, shortestRouteMixTag);
        }
    }

    /**
     * Parse the SHORTESTROUTEMIX's SHORTESTROUTE tag.
     * @param shortestRouteNode Node; the SHORTESTROUTE node to parse
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @param shortestRouteMixTag ShortestRouteMixTag; the parent tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    private static void parseRouteMixRouteTag(final Node shortestRouteNode, final VissimNetworkLaneParser parser,
            final ShortestRouteMixTag shortestRouteMixTag) throws NetworkException, SAXException
    {
        NamedNodeMap attributes = shortestRouteNode.getAttributes();

        Node shortestRouteName = attributes.getNamedItem("NAME");
        if (shortestRouteName == null)
        {
            throw new NetworkException("SHORTESTROUTEMIX: No SHORTESTROUTE NAME defined");
        }
        if (!parser.getRouteTags().containsKey(shortestRouteName.getNodeValue().trim()))
        {
            throw new NetworkException("SHORTESTROUTEMIX: " + shortestRouteMixTag.name + " SHORTESTROUTE "
                    + shortestRouteName.getNodeValue().trim() + " not defined");
        }
        shortestRouteMixTag.routes.add(parser.getShortestRouteTags().get(shortestRouteName.getNodeValue().trim()));

        Node weight = attributes.getNamedItem("WEIGHT");
        if (weight == null)
        {
            throw new NetworkException("SHORTESTROUTEMIX: " + shortestRouteMixTag.name + " SHORTESTROUTE "
                    + shortestRouteName.getNodeValue().trim() + ": weight not defined");
        }
        shortestRouteMixTag.weights.add(Double.parseDouble(weight.getNodeValue()));
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ShortestRouteMixTag [name=" + this.name + ", routes=" + this.routes + ", weights=" + this.weights + "]";
    }

}

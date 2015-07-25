package org.opentrafficsim.core.network.factory.xml;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;
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
class RouteMixTag
{
    /** name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** routes. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<RouteTag> routes = new ArrayList<RouteTag>();

    /** weights. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<Double> weights = new ArrayList<Double>();

    /**
     * Parse the ROUTE tag.
     * @param nodeList nodeList the top-level nodes of the XML-file
     * @param parser the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseRouteMix(final NodeList nodeList, final XmlNetworkLaneParser parser) throws SAXException,
        NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "ROUTEMIX"))
        {
            NamedNodeMap attributes = node.getAttributes();
            RouteMixTag routeMixTag = new RouteMixTag();

            if (attributes.getNamedItem("NAME") == null)
                throw new SAXException("ROUTEMIX: missing attribute NAME");
            routeMixTag.name = attributes.getNamedItem("NAME").getNodeValue().trim();
            if (parser.routeTags.keySet().contains(routeMixTag.name))
                throw new SAXException("ROUTEMIX: NAME " + routeMixTag.name + " defined twice");

            List<Node> routeList = XMLParser.getNodes(node.getChildNodes(), "ROUTE");
            if (routeList.size() == 0)
                throw new SAXException("ROUTEMIX: missing tag ROUTE");
            for (Node routeNode : routeList)
            {
                parseRouteMixRouteTag(routeNode, parser, routeMixTag);
            }

            parser.routeMixTags.put(routeMixTag.name, routeMixTag);
        }
    }

    /**
     * Parse the ROUTEMIX's ROUTE tag.
     * @param routeNode the ROUTE node to parse
     * @param parser the parser with the lists of information
     * @param routeMixTag the parent tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    private static void parseRouteMixRouteTag(final Node routeNode, final XmlNetworkLaneParser parser,
        final RouteMixTag routeMixTag) throws NetworkException, SAXException
    {
        NamedNodeMap attributes = routeNode.getAttributes();

        Node routeName = attributes.getNamedItem("NAME");
        if (routeName == null)
            throw new NetworkException("ROUTEMIX: No ROUTE NAME defined");
        if (!parser.routeTags.containsKey(routeName.getNodeValue().trim()))
            throw new NetworkException("ROUTEMIX: " + routeMixTag.name + " ROUTE " + routeName.getNodeValue().trim()
                + " not defined");
        routeMixTag.routes.add(parser.routeTags.get(routeName.getNodeValue().trim()));

        Node weight = attributes.getNamedItem("WEIGHT");
        if (weight == null)
            throw new NetworkException("ROUTEMIX: " + routeMixTag.name + " ROUTE " + routeName.getNodeValue().trim()
                + ": weight not defined");
        routeMixTag.weights.add(Double.parseDouble(weight.getNodeValue()));
    }

}

package org.opentrafficsim.road.network.factory.xml.old;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.Distributions;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.network.factory.xml.old.CrossSectionElementTag.ElementType;
import org.opentrafficsim.road.network.lane.Lane;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class FillTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Lane name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String laneName = null;

    /** GTU tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    GTUTag gtuTag = null;

    /** GTU mix tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    GTUMixTag gtuMixTag = null;

    /** Inter-vehicle distance. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ContinuousDistDoubleScalar.Rel<Length, LengthUnit> distanceDist = null;

    /** Initial speed. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist = null;

    /** Maximmum number of generated GTUs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    int maxGTUs = Integer.MAX_VALUE;

    /** Route tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    RouteTag routeTag = null;

    /** Route mix tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    RouteMixTag routeMixTag = null;

    /** Shortest route tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ShortestRouteTag shortestRouteTag = null;

    /** Shortest route mix tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ShortestRouteMixTag shortestRouteMixTag = null;

    /**
     * Parse the FILL tag.
     * @param node Node; the FILL node to parse
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param linkTag LinkTag; the parent LINK tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseFill(final Node node, final XmlNetworkLaneParserOld parser, final LinkTag linkTag)
            throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        FillTag fillTag = new FillTag();

        if (attributes.getNamedItem("LANE") == null)
            throw new SAXException("FILL: missing attribute LANE" + " for link " + linkTag.name);
        String laneName = attributes.getNamedItem("LANE").getNodeValue().trim();
        if (linkTag.roadLayoutTag == null)
            throw new NetworkException("FILL: LANE " + laneName + " no ROADTYPE for link " + linkTag.name);
        CrossSectionElementTag cseTag = linkTag.roadLayoutTag.cseTags.get(laneName);
        if (cseTag == null)
            throw new NetworkException("FILL: LANE " + laneName + " not found in elements of link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);
        if (cseTag.elementType != ElementType.LANE)
            throw new NetworkException("FILL: LANE " + laneName + " not a real GTU lane for link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);
        if (linkTag.generatorTags.containsKey(laneName))
            throw new SAXException("FILL for LANE with NAME " + laneName + " defined twice");
        fillTag.laneName = laneName;

        if (attributes.getNamedItem("GTU") != null)
        {
            String gtuName = attributes.getNamedItem("GTU").getNodeValue().trim();
            if (!parser.gtuTags.containsKey(gtuName))
                throw new NetworkException(
                        "FILL: LANE " + laneName + " GTU " + gtuName + " in link " + linkTag.name + " not defined");
            fillTag.gtuTag = parser.gtuTags.get(gtuName);
        }

        if (attributes.getNamedItem("GTUMIX") != null)
        {
            String gtuMixName = attributes.getNamedItem("GTUMIX").getNodeValue().trim();
            if (!parser.gtuMixTags.containsKey(gtuMixName))
                throw new NetworkException(
                        "FILL: LANE " + laneName + " GTUMIX " + gtuMixName + " in link " + linkTag.name + " not defined");
            fillTag.gtuMixTag = parser.gtuMixTags.get(gtuMixName);
        }

        if (fillTag.gtuTag == null && fillTag.gtuMixTag == null)
            throw new SAXException(
                    "FILL: missing attribute GTU or GTUMIX for Lane with NAME " + laneName + " of link " + linkTag.name);

        if (fillTag.gtuTag != null && fillTag.gtuMixTag != null)
            throw new SAXException(
                    "FILL: both attribute GTU and GTUMIX defined for Lane with NAME " + laneName + " of link " + linkTag.name);

        Node distance = attributes.getNamedItem("DISTANCE");
        if (distance == null)
            throw new SAXException("FILL: missing attribute DISTANCE");
        fillTag.distanceDist = Distributions.parseLengthDist(distance.getNodeValue());

        Node initialSpeed = attributes.getNamedItem("INITIALSPEED");
        if (initialSpeed == null)
            throw new SAXException("FILL: missing attribute INITIALSPEED");
        fillTag.initialSpeedDist = Distributions.parseSpeedDist(initialSpeed.getNodeValue());

        Node maxGTU = attributes.getNamedItem("MAXGTU");
        fillTag.maxGTUs = maxGTU == null ? Integer.MAX_VALUE : Integer.parseInt(maxGTU.getNodeValue().trim());

        int numberRouteTags = 0;

        if (attributes.getNamedItem("ROUTE") != null)
        {
            String routeName = attributes.getNamedItem("ROUTE").getNodeValue().trim();
            if (!parser.routeTags.containsKey(routeName))
                throw new NetworkException(
                        "FILL: LANE " + laneName + " ROUTE " + routeName + " in link " + linkTag.name + " not defined");
            fillTag.routeTag = parser.routeTags.get(routeName);
            numberRouteTags++;
        }

        if (attributes.getNamedItem("ROUTEMIX") != null)
        {
            String routeMixName = attributes.getNamedItem("ROUTEMIX").getNodeValue().trim();
            if (!parser.routeMixTags.containsKey(routeMixName))
                throw new NetworkException(
                        "FILL: LANE " + laneName + " ROUTEMIX " + routeMixName + " in link " + linkTag.name + " not defined");
            fillTag.routeMixTag = parser.routeMixTags.get(routeMixName);
            numberRouteTags++;
        }

        if (attributes.getNamedItem("SHORTESTROUTE") != null)
        {
            String shortestRouteName = attributes.getNamedItem("SHORTESTROUTE").getNodeValue().trim();
            if (!parser.shortestRouteTags.containsKey(shortestRouteName))
                throw new NetworkException("FILL: LANE " + laneName + " SHORTESTROUTE " + shortestRouteName + " in link "
                        + linkTag.name + " not defined");
            fillTag.shortestRouteTag = parser.shortestRouteTags.get(shortestRouteName);
            numberRouteTags++;
        }

        if (attributes.getNamedItem("SHORTESTROUTEMIX") != null)
        {
            String shortestRouteMixName = attributes.getNamedItem("SHORTESTROUTEMIX").getNodeValue().trim();
            if (!parser.shortestRouteMixTags.containsKey(shortestRouteMixName))
                throw new NetworkException("FILL: LANE " + laneName + " SHORTESTROUTEMIX " + shortestRouteMixName + " in link "
                        + linkTag.name + " not defined");
            fillTag.shortestRouteMixTag = parser.shortestRouteMixTags.get(shortestRouteMixName);
            numberRouteTags++;
        }

        if (numberRouteTags > 1)
            throw new SAXException(
                    "FILL: multiple ROUTE tags defined for Lane with NAME " + laneName + " of link " + linkTag.name);

        // TODO GTUColorer

        linkTag.fillTags.put(laneName, fillTag);
    }

    /**
     * Make a fill of a Lane.
     * @param fillTag FillTag; XML tag for the generator to build
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param linkTag LinkTag; the parent LINK tag
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to schedule GTU generation
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws NetworkException when route generator cannot be instantiated
     */
    static void makeFill(final FillTag fillTag, final XmlNetworkLaneParserOld parser, final LinkTag linkTag,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator) throws SimRuntimeException, NetworkException
    {
        Lane lane = linkTag.lanes.get(fillTag.laneName);
        Class<?> gtuClass = LaneBasedIndividualGTU.class;
        List<org.opentrafficsim.core.network.Node> nodeList = new ArrayList<>();
        for (NodeTag nodeTag : fillTag.routeTag.routeNodeTags)
        {
            nodeList.add(parser.nodeTags.get(nodeTag.name).node);
        }
        Generator<Route> rg = new FixedRouteGenerator(
                new CompleteRoute("fixed route", lane.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE), nodeList));

        // TODO create a FILL

        // TODO GTUMix
        // TODO RouteMix
        // TODO ShortestRoute
        // TODO ShortestRouteMix
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "FillTag [laneName=" + this.laneName + ", gtuTag=" + this.gtuTag + ", gtuMixTag=" + this.gtuMixTag
                + ", distanceDist=" + this.distanceDist + ", initialSpeedDist=" + this.initialSpeedDist + ", maxGTUs="
                + this.maxGTUs + ", routeTag=" + this.routeTag + ", routeMixTag=" + this.routeMixTag + ", shortestRouteTag="
                + this.shortestRouteTag + ", shortestRouteMixTag=" + this.shortestRouteMixTag + "]";
    }

}

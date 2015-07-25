package org.opentrafficsim.core.network.factory.xml;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.CrossSectionElementTag.ElementType;
import org.opentrafficsim.core.network.factory.xml.units.Distributions;
import org.opentrafficsim.core.network.factory.xml.units.TimeUnits;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DistContinuousDoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
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
class GeneratorTag
{
    /** position of the generator on the link, relative to the design line. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DoubleScalar.Rel<LengthUnit> position = null;

    /** GTU tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    GTUTag gtuTag = null;

    /** GTU mix tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    GTUMixTag gtuMixTag = null;

    /** interarrival time. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DistContinuousDoubleScalar.Rel<TimeUnit> iatDist = null;

    /** initial speed. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DistContinuousDoubleScalar.Abs<SpeedUnit> initialSpeedDist = null;

    /** max number of generated GTUs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    int maxGTUs = Integer.MAX_VALUE;

    /** start time of generation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DoubleScalar.Abs<TimeUnit> startTime = null;

    /** end time of generation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DoubleScalar.Abs<TimeUnit> endTime = null;

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

    /** GTU colorer. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String gtuColorer;

    /**
     * Parse the GENERATOR tag.
     * @param node the GENERATOR node to parse
     * @param parser the parser with the lists of information
     * @param linkTag the parent LINK tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseGenerator(final Node node, final XmlNetworkLaneParser parser, final LinkTag linkTag)
        throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        GeneratorTag generatorTag = new GeneratorTag();

        if (attributes.getNamedItem("LANE") == null)
            throw new SAXException("GENERATOR: missing attribute LANE" + " for link " + linkTag.name);
        String laneName = attributes.getNamedItem("LANE").getNodeValue().trim();
        if (linkTag.roadTypeTag == null)
            throw new NetworkException("GENERATOR: LANE " + laneName + " no ROADTYPE for link " + linkTag.name);
        CrossSectionElementTag cseTag = linkTag.roadTypeTag.cseTags.get(laneName);
        if (cseTag == null)
            throw new NetworkException("GENERATOR: LANE " + laneName + " not found in elements of link " + linkTag.name
                + " - roadtype " + linkTag.roadTypeTag.name);
        if (cseTag.elementType != ElementType.LANE)
            throw new NetworkException("GENERATOR: LANE " + laneName + " not a real GTU lane for link " + linkTag.name
                + " - roadtype " + linkTag.roadTypeTag.name);
        if (linkTag.generatorTags.containsKey(laneName))
            throw new SAXException("GENERATOR for LANE with NAME " + laneName + " defined twice");

        Node position = attributes.getNamedItem("POSITION");
        generatorTag.position =
            LinkTag.parseBeginEndPosition(position == null ? "END" : position.getNodeValue().trim(), linkTag);

        if (attributes.getNamedItem("GTU") != null)
        {
            String gtuName = attributes.getNamedItem("GTU").getNodeValue().trim();
            if (!parser.gtuTags.containsKey(gtuName))
                throw new NetworkException("GENERATOR: LANE " + laneName + " GTU " + gtuName + " in link " + linkTag.name
                    + " not defined");
            generatorTag.gtuTag = parser.gtuTags.get(gtuName);
        }

        if (attributes.getNamedItem("GTUMIX") != null)
        {
            String gtuMixName = attributes.getNamedItem("GTUMIX").getNodeValue().trim();
            if (!parser.gtuMixTags.containsKey(gtuMixName))
                throw new NetworkException("GENERATOR: LANE " + laneName + " GTUMIX " + gtuMixName + " in link "
                    + linkTag.name + " not defined");
            generatorTag.gtuMixTag = parser.gtuMixTags.get(gtuMixName);
        }

        if (generatorTag.gtuTag == null && generatorTag.gtuMixTag == null)
            throw new SAXException("GENERATOR: missing attribute GTU or GTUMIX for Lane with NAME " + laneName + " of link "
                + linkTag.name);

        if (generatorTag.gtuTag != null && generatorTag.gtuMixTag != null)
            throw new SAXException("GENERATOR: both attribute GTU and GTUMIX defined for Lane with NAME " + laneName
                + " of link " + linkTag.name);

        Node iat = attributes.getNamedItem("IAT");
        if (iat == null)
            throw new SAXException("GENERATOR: missing attribute IAT");
        generatorTag.iatDist = Distributions.parseTimeDistRel(iat.getNodeValue());

        Node initialSpeed = attributes.getNamedItem("INITIALSPEED");
        if (initialSpeed == null)
            throw new SAXException("GENERATOR: missing attribute INITIALSPEED");
        generatorTag.initialSpeedDist = Distributions.parseSpeedDistAbs(initialSpeed.getNodeValue());

        Node maxGTU = attributes.getNamedItem("MAXGTU");
        generatorTag.maxGTUs = maxGTU == null ? Integer.MAX_VALUE : Integer.parseInt(maxGTU.getNodeValue().trim());

        if (attributes.getNamedItem("STARTTIME") != null)
            generatorTag.startTime = TimeUnits.parseTimeAbs(attributes.getNamedItem("STARTTIME").getNodeValue());

        if (attributes.getNamedItem("ENDTIME") != null)
            generatorTag.endTime = TimeUnits.parseTimeAbs(attributes.getNamedItem("ENDTIME").getNodeValue());

        int numberRouteTags = 0;

        if (attributes.getNamedItem("ROUTE") != null)
        {
            String routeName = attributes.getNamedItem("ROUTE").getNodeValue().trim();
            if (!parser.routeTags.containsKey(routeName))
                throw new NetworkException("GENERATOR: LANE " + laneName + " ROUTE " + routeName + " in link "
                    + linkTag.name + " not defined");
            generatorTag.routeTag = parser.routeTags.get(routeName);
            numberRouteTags++;
        }

        if (attributes.getNamedItem("ROUTEMIX") != null)
        {
            String routeMixName = attributes.getNamedItem("ROUTEMIX").getNodeValue().trim();
            if (!parser.routeMixTags.containsKey(routeMixName))
                throw new NetworkException("GENERATOR: LANE " + laneName + " ROUTEMIX " + routeMixName + " in link "
                    + linkTag.name + " not defined");
            generatorTag.routeMixTag = parser.routeMixTags.get(routeMixName);
            numberRouteTags++;
        }

        if (attributes.getNamedItem("SHORTESTROUTE") != null)
        {
            String shortestRouteName = attributes.getNamedItem("SHORTESTROUTE").getNodeValue().trim();
            if (!parser.shortestRouteTags.containsKey(shortestRouteName))
                throw new NetworkException("GENERATOR: LANE " + laneName + " SHORTESTROUTE " + shortestRouteName
                    + " in link " + linkTag.name + " not defined");
            generatorTag.shortestRouteTag = parser.shortestRouteTags.get(shortestRouteName);
            numberRouteTags++;
        }

        if (attributes.getNamedItem("SHORTESTROUTEMIX") != null)
        {
            String shortestRouteMixName = attributes.getNamedItem("SHORTESTROUTEMIX").getNodeValue().trim();
            if (!parser.shortestRouteMixTags.containsKey(shortestRouteMixName))
                throw new NetworkException("GENERATOR: LANE " + laneName + " SHORTESTROUTEMIX " + shortestRouteMixName
                    + " in link " + linkTag.name + " not defined");
            generatorTag.shortestRouteMixTag = parser.shortestRouteMixTags.get(shortestRouteMixName);
            numberRouteTags++;
        }

        if (numberRouteTags > 1)
            throw new SAXException("GENERATOR: multiple ROUTE tags defined for Lane with NAME " + laneName + " of link "
                + linkTag.name);

        // TODO GTUColorer

        linkTag.generatorTags.put(laneName, generatorTag);
    }
}

package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.Distributions;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.generator.GTUGeneratorIndividualOld;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.lane.tactical.toledo.ToledoFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.factory.vissim.CrossSectionElementTag.ElementType;
import org.opentrafficsim.road.network.lane.Lane;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class GeneratorTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Lane name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String laneName = null;

    /** Position of the sink on the link, relative to the design line, stored as a string to parse when the length is known. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String positionStr = null;

    /** Direction in which to generate the GTU, relative to the design line of the Link. */
    // TOO parse direction, and add to XML formal
    GTUDirectionality gtuDirection = GTUDirectionality.DIR_PLUS;

    /** GTU tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    GTUTag gtuTag = null;

    /** GTU mix tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    GTUMixTag gtuMixTag = null;

    /** Interarrival time. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> iatDist = null;

    /** Initial speed. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist = null;

    /** Maximimum number of generated GTUs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    int maxGTUs = Integer.MAX_VALUE;

    /** Start time of generation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Time startTime = null;

    /** End time of generation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Time endTime = null;

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

    /** Tactical planner name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String tacticalPlannerName = null;

    /** GTU colorer. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    GTUColorer gtuColorer;

    /**
     * Parse the GENERATOR tag.
     * @param node Node; the GENERATOR node to parse
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @param linkTag LinkTag; the parent LINK tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseGenerator(final Node node, final VissimNetworkLaneParser parser, final LinkTag linkTag)
            throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        GeneratorTag generatorTag = new GeneratorTag();

        if (attributes.getNamedItem("LANE") == null)
        {
            throw new SAXException("GENERATOR: missing attribute LANE" + " for link " + linkTag.name);
        }
        String laneName = attributes.getNamedItem("LANE").getNodeValue().trim();
        if (linkTag.roadLayoutTag == null)
        {
            throw new NetworkException("GENERATOR: LANE " + laneName + " no ROADTYPE for link " + linkTag.name);
        }
        CrossSectionElementTag cseTag = linkTag.roadLayoutTag.cseTags.get(laneName);
        if (cseTag == null)
        {
            throw new NetworkException("GENERATOR: LANE " + laneName + " not found in elements of link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);
        }
        if (cseTag.elementType != ElementType.LANE)
        {
            throw new NetworkException("GENERATOR: LANE " + laneName + " not a real GTU lane for link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);
        }
        if (linkTag.generatorTags.containsKey(laneName))
        {
            throw new SAXException("GENERATOR for LANE with NAME " + laneName + " defined twice");
        }
        generatorTag.laneName = laneName;

        Node position = attributes.getNamedItem("POSITION");
        if (position == null)
        {
            throw new NetworkException("GENERATOR: POSITION element not found in elements of link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);
        }
        generatorTag.positionStr = position.getNodeValue().trim();

        /*-
        // TODO parse direction
        Node directionStr = attributes.getNamedItem("DIRECTION");
        if (directionStr == null)
            throw new NetworkException("GENERATOR: DIRECTION element not found in elements of link " + linkTag.name
                + " - roadtype " + linkTag.roadLayoutTag.name);
        generatorTag.gtuDirection = parseDirection(directionStr.getNodeValue().trim());
         */

        if (attributes.getNamedItem("GTU") != null)
        {
            String gtuName = attributes.getNamedItem("GTU").getNodeValue().trim();
            if (!parser.getGtuTags().containsKey(gtuName))
            {
                throw new NetworkException(
                        "GENERATOR: LANE " + laneName + " GTU " + gtuName + " in link " + linkTag.name + " not defined");
            }
            generatorTag.gtuTag = parser.getGtuTags().get(gtuName);
        }

        if (attributes.getNamedItem("GTUMIX") != null)
        {
            String gtuMixName = attributes.getNamedItem("GTUMIX").getNodeValue().trim();
            if (!parser.getGtuMixTags().containsKey(gtuMixName))
            {
                throw new NetworkException(
                        "GENERATOR: LANE " + laneName + " GTUMIX " + gtuMixName + " in link " + linkTag.name + " not defined");
            }
            generatorTag.gtuMixTag = parser.getGtuMixTags().get(gtuMixName);
        }

        if (generatorTag.gtuTag == null && generatorTag.gtuMixTag == null)
        {
            throw new SAXException(
                    "GENERATOR: missing attribute GTU or GTUMIX for Lane with NAME " + laneName + " of link " + linkTag.name);
        }

        if (generatorTag.gtuTag != null && generatorTag.gtuMixTag != null)
        {
            throw new SAXException("GENERATOR: both attribute GTU and GTUMIX defined for Lane with NAME " + laneName
                    + " of link " + linkTag.name);
        }

        if (attributes.getNamedItem("TACTICALPLANNER") != null)
        {
            generatorTag.tacticalPlannerName = attributes.getNamedItem("TACTICALPLANNER").getNodeValue().trim();
        }

        Node iat = attributes.getNamedItem("IAT");
        if (iat == null)
        {
            throw new SAXException("GENERATOR: missing attribute IAT");
        }
        generatorTag.iatDist = Distributions.parseDurationDist(iat.getNodeValue());

        Node initialSpeed = attributes.getNamedItem("INITIALSPEED");
        if (initialSpeed == null)
        {
            throw new SAXException("GENERATOR: missing attribute INITIALSPEED");
        }
        generatorTag.initialSpeedDist = Distributions.parseSpeedDist(initialSpeed.getNodeValue());

        Node maxGTU = attributes.getNamedItem("MAXGTU");
        generatorTag.maxGTUs = maxGTU == null ? Integer.MAX_VALUE : Integer.parseInt(maxGTU.getNodeValue().trim());

        if (attributes.getNamedItem("STARTTIME") != null)
        {
            generatorTag.startTime = Time.valueOf(attributes.getNamedItem("STARTTIME").getNodeValue());
        }

        if (attributes.getNamedItem("ENDTIME") != null)
        {
            generatorTag.endTime = Time.valueOf(attributes.getNamedItem("ENDTIME").getNodeValue());
        }

        int numberRouteTags = 0;

        if (attributes.getNamedItem("ROUTE") != null)
        {
            String routeName = attributes.getNamedItem("ROUTE").getNodeValue().trim();
            if (!parser.getRouteTags().containsKey(routeName))
            {
                throw new NetworkException(
                        "GENERATOR: LANE " + laneName + " ROUTE " + routeName + " in link " + linkTag.name + " not defined");
            }
            generatorTag.routeTag = parser.getRouteTags().get(routeName);
            numberRouteTags++;
        }

        if (attributes.getNamedItem("ROUTEMIX") != null)
        {
            String routeMixName = attributes.getNamedItem("ROUTEMIX").getNodeValue().trim();
            if (!parser.getRouteMixTags().containsKey(routeMixName))
            {
                throw new NetworkException("GENERATOR: LANE " + laneName + " ROUTEMIX " + routeMixName + " in link "
                        + linkTag.name + " not defined");
            }
            generatorTag.routeMixTag = parser.getRouteMixTags().get(routeMixName);
            numberRouteTags++;
        }

        if (attributes.getNamedItem("SHORTESTROUTE") != null)
        {
            String shortestRouteName = attributes.getNamedItem("SHORTESTROUTE").getNodeValue().trim();
            if (!parser.getShortestRouteTags().containsKey(shortestRouteName))
            {
                throw new NetworkException("GENERATOR: LANE " + laneName + " SHORTESTROUTE " + shortestRouteName + " in link "
                        + linkTag.name + " not defined");
            }
            generatorTag.shortestRouteTag = parser.getShortestRouteTags().get(shortestRouteName);
            numberRouteTags++;
        }

        if (attributes.getNamedItem("SHORTESTROUTEMIX") != null)
        {
            String shortestRouteMixName = attributes.getNamedItem("SHORTESTROUTEMIX").getNodeValue().trim();
            if (!parser.getShortestRouteMixTags().containsKey(shortestRouteMixName))
            {
                throw new NetworkException("GENERATOR: LANE " + laneName + " SHORTESTROUTEMIX " + shortestRouteMixName
                        + " in link " + linkTag.name + " not defined");
            }
            generatorTag.shortestRouteMixTag = parser.getShortestRouteMixTags().get(shortestRouteMixName);
            numberRouteTags++;
        }

        if (numberRouteTags > 1)
        {
            throw new SAXException(
                    "GENERATOR: multiple ROUTE tags defined for Lane with NAME " + laneName + " of link " + linkTag.name);
        }

        if (numberRouteTags == 0)
        {
            throw new SAXException(
                    "GENERATOR: no ROUTE tags defined for Lane with NAME " + laneName + " of link " + linkTag.name);
        }

        Node gtuColorerNode = attributes.getNamedItem("GTUCOLORER");
        if (gtuColorerNode == null)
        {
            throw new SAXException("GENERATOR: missing attribute GTUCOLORER");
        }
        generatorTag.gtuColorer = GTUColorerTag.parseGTUColorer(gtuColorerNode.getNodeValue().trim(), parser.getGlobalTag());

        linkTag.generatorTags.put(generatorTag.laneName, generatorTag);
    }

    /**
     * Make the generators for this link, if available.
     * @param linkTag LinkTag; the parent LINK tag
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @param simulator OTSSimulatorInterface; the simulator to schedule GTU generation
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws NetworkException when route generator cannot be instantiated
     * @throws GTUException when construction of the Strategical Planner failed
     */
    static void makeGenerators(final LinkTag linkTag, final VissimNetworkLaneParser parser,
            final OTSSimulatorInterface simulator) throws SimRuntimeException, NetworkException, GTUException
    {
        for (GeneratorTag generatorTag : linkTag.generatorTags.values())
        {
            makeGenerator(generatorTag, parser, linkTag, simulator);
        }
    }

    /**
     * Make a generator.
     * @param generatorTag GeneratorTag; XML tag for the generator to build
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @param linkTag LinkTag; the parent LINK tag
     * @param simulator OTSSimulatorInterface; the simulator to schedule GTU generation
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws NetworkException when route generator cannot be instantiated
     * @throws GTUException when construction of the Strategical Planner failed
     */
    static void makeGenerator(final GeneratorTag generatorTag, final VissimNetworkLaneParser parser, final LinkTag linkTag,
            final OTSSimulatorInterface simulator) throws SimRuntimeException, NetworkException, GTUException
    {
        Lane lane = linkTag.lanes.get(generatorTag.laneName);
        Class<?> gtuClass = LaneBasedIndividualGTU.class;
        List<org.opentrafficsim.core.network.Node> nodeList = new ArrayList<>();
        for (NodeTag nodeTag : generatorTag.routeTag.routeNodeTags)
        {
            nodeList.add(parser.getNodeTags().get(nodeTag.name).node);
        }
        Generator<Route> routeGenerator = new FixedRouteGenerator(new Route(generatorTag.laneName, nodeList));
        Time startTime = generatorTag.startTime != null ? generatorTag.startTime : Time.ZERO;
        Time endTime = generatorTag.endTime != null ? generatorTag.endTime : new Time(Double.MAX_VALUE, TimeUnit.BASE_SECOND);
        Length position = LinkTag.parseBeginEndPosition(generatorTag.positionStr, lane);
        // TODO is the default stream present?
        LaneBasedTacticalPlannerFactory<?> tacticalPlannerFactory =
                makeTacticalPlannerFactory(generatorTag, simulator.getReplication().getStream("default"));
        LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactory =
                new LaneBasedStrategicalRoutePlannerFactory(tacticalPlannerFactory);
        new GTUGeneratorIndividualOld(linkTag.name + "." + generatorTag.laneName, simulator, generatorTag.gtuTag.gtuType,
                gtuClass, generatorTag.initialSpeedDist, generatorTag.iatDist, generatorTag.gtuTag.lengthDist,
                generatorTag.gtuTag.widthDist, generatorTag.gtuTag.maxSpeedDist, generatorTag.maxGTUs, startTime, endTime, lane,
                position, generatorTag.gtuDirection, strategicalPlannerFactory, routeGenerator, parser.network);

        // TODO Use LaneBasedGTUGenerator
        // TODO GTUMix
        // TODO RouteMix
        // TODO ShortestRoute
        // TODO ShortestRouteMix
        // TODO Different strategical planner factories
    }

    /**
     * Factories are: IDM|MOBIL/IDM|DIRECTION/IDM|LMRS|TOLEDO.
     * @param generatorTag GeneratorTag; the tag to parse
     * @param stream StreamInterface; random number stream
     * @return a LaneBasedTacticalPlannerFactory according to the tag
     */
    static LaneBasedTacticalPlannerFactory<?> makeTacticalPlannerFactory(final GeneratorTag generatorTag,
            final StreamInterface stream)
    {
        if (generatorTag.tacticalPlannerName == null || generatorTag.tacticalPlannerName.equals("IDM"))
        {
            return new LaneBasedGTUFollowingTacticalPlannerFactory(new IDMPlusOld());
        }
        if (generatorTag.tacticalPlannerName.equals("MOBIL/IDM"))
        {
            return new LaneBasedCFLCTacticalPlannerFactory(new IDMPlusOld(), new Egoistic());
        }
        if (generatorTag.tacticalPlannerName.equals("DIRECTION/IDM"))
        {
            return new LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory(new IDMPlusOld());
        }
        if (generatorTag.tacticalPlannerName.equals("LMRS"))
        {
            try
            {
                return new LMRSFactory(new IDMPlusFactory(stream), new DefaultLMRSPerceptionFactory());
            }
            catch (GTUException exception)
            {
                exception.printStackTrace();
            }
        }
        if (generatorTag.tacticalPlannerName.equals("TOLEDO"))
        {
            return new ToledoFactory();
        }
        System.err.println("Unknown generatorTag.tacticalPlannerName: " + generatorTag.tacticalPlannerName
                + ", not one of: IDM|MOBIL/IDM|DIRECTION/IDM|LMRS|TOLEDO");
        return new LaneBasedGTUFollowingTacticalPlannerFactory(new IDMPlusOld());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GeneratorTag [laneName=" + this.laneName + ", positionStr=" + this.positionStr + ", gtuDirection="
                + this.gtuDirection + ", gtuTag=" + this.gtuTag + ", gtuMixTag=" + this.gtuMixTag + ", iatDist=" + this.iatDist
                + ", initialSpeedDist=" + this.initialSpeedDist + ", maxGTUs=" + this.maxGTUs + ", startTime=" + this.startTime
                + ", endTime=" + this.endTime + ", routeTag=" + this.routeTag + ", routeMixTag=" + this.routeMixTag
                + ", shortestRouteTag=" + this.shortestRouteTag + ", shortestRouteMixTag=" + this.shortestRouteMixTag
                + ", gtuColorer=" + this.gtuColorer + "]";
    }

}

package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.ProbabilisticRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuTemplate;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuTemplateDistribution;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLmrsPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.Lmrs;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.Generators;
import org.opentrafficsim.road.network.factory.xml.utils.ParseUtil;
import org.opentrafficsim.road.network.factory.xml.utils.Transformer;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.detector.DetectorType;
import org.opentrafficsim.road.network.lane.object.detector.SinkDetector;
import org.opentrafficsim.xml.generated.Demand;
import org.opentrafficsim.xml.generated.GtuTemplate;
import org.opentrafficsim.xml.generated.GtuTemplateMix;
import org.opentrafficsim.xml.generated.RouteMix;
import org.opentrafficsim.xml.generated.ShortestRoute;
import org.opentrafficsim.xml.generated.ShortestRoute.Via;
import org.opentrafficsim.xml.generated.ShortestRouteMix;
import org.opentrafficsim.xml.generated.Sink;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * GeneratorSinkParser.java.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class GeneratorSinkParser
{
    /** */
    private GeneratorSinkParser()
    {
        // utility class
    }

    /**
     * Parse the ROUTE tags.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param definitions Definitions; parsed definitions
     * @param demand Demand; the Demand tag
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseRoutes(final RoadNetwork otsNetwork, final Definitions definitions, final Demand demand)
            throws NetworkException
    {
        for (org.opentrafficsim.xml.generated.Route routeTag : demand.getRoute())
        {
            GtuType gtuType = definitions.get(GtuType.class, routeTag.getGtuType());
            Route route = new Route(routeTag.getId(), gtuType);
            if (gtuType == null)
                throw new NetworkException("GTUTYPE " + routeTag.getGtuType() + " not found in ROUTE " + routeTag.getId());
            for (String nodeTag : routeTag.getNode())
            {
                Node node = otsNetwork.getNode(nodeTag);
                if (node == null)
                    throw new NetworkException("NODE " + nodeTag + " not found in ROUTE " + routeTag.getId());
                route.addNode(node);
            }
            otsNetwork.addRoute(gtuType, route);
        }
    }

    /**
     * Parse the ShortestRoute tags.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param definitions Definitions; parsed definitions
     * @param demand Demand; the Demand tag
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseShortestRoutes(final RoadNetwork otsNetwork, final Definitions definitions, final Demand demand)
            throws NetworkException
    {
        for (ShortestRoute shortestRouteTag : demand.getShortestRoute())
        {
            GtuType gtuType = definitions.get(GtuType.class, shortestRouteTag.getGtuType());
            Route route = new Route(shortestRouteTag.getId(), gtuType);
            if (gtuType == null)
                throw new NetworkException(
                        "GTUTYPE " + shortestRouteTag.getGtuType() + " not found in ShortestRoute " + shortestRouteTag.getId());
            Node nodeFrom = otsNetwork.getNode(shortestRouteTag.getFrom().getNode());
            if (nodeFrom == null)
                throw new NetworkException("FROM NODE " + shortestRouteTag.getFrom().getNode() + " not found in ShortestRoute "
                        + shortestRouteTag.getId());
            Node nodeTo = otsNetwork.getNode(shortestRouteTag.getTo().getNode());
            if (nodeTo == null)
                throw new NetworkException("TO NODE " + shortestRouteTag.getTo().getNode() + " not found in ShortestRoute "
                        + shortestRouteTag.getId());
            List<Node> nodesVia = new ArrayList<>();
            for (Via nodeViaTag : shortestRouteTag.getVia())
            {
                Node nodeVia = otsNetwork.getNode(nodeViaTag.getNode());
                if (nodeTo == null)
                    throw new NetworkException(
                            "VIA NODE " + nodeViaTag.getNode() + " not found in ShortestRoute " + shortestRouteTag.getId());
                nodesVia.add(nodeVia);
            }
            // TODO: distance weight and time weight
            Route shortestRoute = otsNetwork.getShortestRouteBetween(gtuType, nodeFrom, nodeTo, nodesVia);
            if (shortestRoute == null)
            {
                throw new NetworkException("Cannot find shortest route from " + nodeFrom.getId() + " to " + nodeTo.getId());
            }
            for (Node node : shortestRoute.getNodes())
            {
                route.addNode(node);
            }
            otsNetwork.addRoute(gtuType, route);
        }
    }

    /**
     * Parse the ROUTEMIX tags.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param demand Demand; the Demand tag
     * @return id-based Map of routemix objects as FrequencyAndObject lists
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    @SuppressWarnings("checkstyle:needbraces")
    static Map<String, List<FrequencyAndObject<Route>>> parseRouteMix(final RoadNetwork otsNetwork, final Demand demand)
            throws NetworkException
    {
        Map<String, List<FrequencyAndObject<Route>>> routeMixMap = new LinkedHashMap<>();
        for (RouteMix routeMixTag : demand.getRouteMix())
        {
            List<FrequencyAndObject<Route>> probRoutes = new ArrayList<>();
            for (RouteMix.Route mixRoute : routeMixTag.getRoute())
            {
                String routeName = mixRoute.getId();
                double weight = mixRoute.getWeight();
                Route route = otsNetwork.getRoute(routeName);
                if (route == null)
                    throw new NetworkException(
                            "Parsing ROUTEMIX " + routeMixTag.getId() + " -- ROUTE " + routeName + " not found");
                probRoutes.add(new FrequencyAndObject<>(weight, route));
            }
            routeMixMap.put(routeMixTag.getId(), probRoutes);
        }
        return routeMixMap;
    }

    /**
     * Parse the ShortestRouteMix tags.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param demand Demand; the Demand tag
     * @return id-based Map of routemix objects as FrequencyAndObject lists
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    @SuppressWarnings("checkstyle:needbraces")
    static Map<String, List<FrequencyAndObject<Route>>> parseShortestRouteMix(final RoadNetwork otsNetwork, final Demand demand)
            throws NetworkException
    {
        Map<String, List<FrequencyAndObject<Route>>> shortestRouteMixMap = new LinkedHashMap<>();
        for (ShortestRouteMix routeMixTag : demand.getShortestRouteMix())
        {
            List<FrequencyAndObject<Route>> probRoutes = new ArrayList<>();
            for (ShortestRouteMix.ShortestRoute mixRoute : routeMixTag.getShortestRoute())
            {
                String routeName = mixRoute.getId();
                double weight = mixRoute.getWeight();
                Route route = otsNetwork.getRoute(routeName);
                if (route == null)
                    throw new NetworkException(
                            "Parsing ShortestRouteMix " + routeMixTag.getId() + " -- SHORESTROUTE " + routeName + " not found");
                probRoutes.add(new FrequencyAndObject<>(weight, route));
            }
            shortestRouteMixMap.put(routeMixTag.getId(), probRoutes);
        }
        return shortestRouteMixMap;
    }

    /**
     * Parse the Generators.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param definitions Definitions; parsed definitions
     * @param demand Network; the Network tag
     * @param gtuTemplates GtuTemplate tags
     * @param routeMixMap map with route mix entries
     * @param shortestRouteMixMap map with shortest route mix entries
     * @param streamInformation map with stream information
     * @return list of created GTU generators
     * @throws XmlParserException when the objects cannot be inserted into the network due to inconsistencies
     */
    @SuppressWarnings("checkstyle:needbraces")
    public static List<LaneBasedGtuGenerator> parseGenerators(final RoadNetwork otsNetwork, final Definitions definitions,
            final Demand demand, final Map<String, GtuTemplate> gtuTemplates,
            final Map<String, List<FrequencyAndObject<Route>>> routeMixMap,
            final Map<String, List<FrequencyAndObject<Route>>> shortestRouteMixMap, final StreamInformation streamInformation)
            throws XmlParserException
    {
        OtsSimulatorInterface simulator = otsNetwork.getSimulator();
        List<LaneBasedGtuGenerator> generators = new ArrayList<>();
        try
        {
            for (org.opentrafficsim.xml.generated.Generator generatorTag : demand.getGenerator())
            {
                StreamInterface stream = ParseUtil.findStream(streamInformation, generatorTag.getRandomStream());

                Generator<Route> routeGenerator;
                if (generatorTag.getRoute() != null)
                {
                    Route route = otsNetwork.getRoute(generatorTag.getRoute());
                    if (route == null)
                        throw new XmlParserException("GENERATOR for LANE " + generatorTag.getLink() + "."
                                + generatorTag.getLane() + ": Route " + generatorTag.getRoute() + " not found");
                    routeGenerator = new FixedRouteGenerator(route);
                }

                else if (generatorTag.getRouteMix() != null)
                {
                    List<FrequencyAndObject<Route>> routeMix = routeMixMap.get(generatorTag.getRouteMix());
                    if (routeMix == null)
                        throw new XmlParserException("GENERATOR for LANE " + generatorTag.getLink() + "."
                                + generatorTag.getLane() + ": RouteMix " + generatorTag.getRouteMix() + " not found");
                    RouteMix routeMixXml = null;
                    for (RouteMix mix : demand.getRouteMix())
                    {
                        if (mix.getId().equals(generatorTag.getRouteMix()))
                        {
                            routeMixXml = mix;
                        }
                    }
                    Throw.when(routeMixXml == null, XmlParserException.class, "Route mix '%s' not defined.",
                            generatorTag.getRouteMix());
                    StreamInterface routeMixStream = ParseUtil.findStream(streamInformation, routeMixXml.getRandomStream());
                    try
                    {
                        routeGenerator = new ProbabilisticRouteGenerator(routeMix, routeMixStream);
                    }
                    catch (ProbabilityException exception)
                    {
                        throw new RuntimeException("GENERATOR for LANE " + generatorTag.getLink() + "." + generatorTag.getLane()
                                + "Could not generate RouteMix " + generatorTag.getRouteMix());
                    }
                }

                else if (generatorTag.getShortestRoute() != null)
                {
                    Route shortestRoute = otsNetwork.getRoute(generatorTag.getShortestRoute());
                    if (shortestRoute == null)
                        throw new XmlParserException("GENERATOR for LANE " + generatorTag.getLink() + "."
                                + generatorTag.getLane() + ": ShortestRoute " + generatorTag.getShortestRoute() + " not found");
                    routeGenerator = new FixedRouteGenerator(shortestRoute);
                }

                else if (generatorTag.getShortestRouteMix() != null)
                {
                    List<FrequencyAndObject<Route>> shortestRouteMix =
                            shortestRouteMixMap.get(generatorTag.getShortestRouteMix());
                    if (shortestRouteMix == null)
                        throw new XmlParserException(
                                "GENERATOR for LANE " + generatorTag.getLink() + "." + generatorTag.getLane()
                                        + ": ShortestRouteMix " + generatorTag.getShortestRouteMix() + " not found");
                    ShortestRouteMix shortestRouteMixXml = null;
                    for (ShortestRouteMix mix : demand.getShortestRouteMix())
                    {
                        if (mix.getId().equals(generatorTag.getShortestRouteMix()))
                        {
                            shortestRouteMixXml = mix;
                        }
                    }
                    Throw.when(shortestRouteMixXml == null, XmlParserException.class, "Shortest route mix '%s' not defined.",
                            generatorTag.getShortestRouteMix());
                    StreamInterface shortestRouteMixStream =
                            ParseUtil.findStream(streamInformation, shortestRouteMixXml.getRandomStream());
                    try
                    {
                        routeGenerator = new ProbabilisticRouteGenerator(shortestRouteMix, shortestRouteMixStream);
                    }
                    catch (ProbabilityException exception)
                    {
                        throw new RuntimeException("GENERATOR for LANE " + generatorTag.getLink() + "." + generatorTag.getLane()
                                + "Could not generate ShortestRouteMix " + generatorTag.getShortestRouteMix());
                    }
                }

                else
                {
                    throw new XmlParserException("GENERATOR for LANE " + generatorTag.getLink() + "." + generatorTag.getLane()
                            + ": No route information");
                }

                CarFollowingModelFactory<IdmPlus> idmPlusFactory = new IdmPlusFactory(stream);
                LaneBasedTacticalPlannerFactory<Lmrs> tacticalFactory =
                        new LmrsFactory(idmPlusFactory, new DefaultLmrsPerceptionFactory());
                LaneBasedStrategicalRoutePlannerFactory strategicalFactory =
                        new LaneBasedStrategicalRoutePlannerFactory(tacticalFactory);

                // the distribution of GTUs
                Distribution<LaneBasedGtuTemplate> gtuTypeDistribution;
                if (generatorTag.getGtuTemplate() != null)
                {
                    gtuTypeDistribution = new Distribution<>(stream);
                    GtuTemplate templateTag = gtuTemplates.get(generatorTag.getGtuTemplate());
                    if (templateTag == null)
                        throw new XmlParserException(
                                "GtuTemplate " + generatorTag.getGtuTemplate() + " in generator not defined");
                    LaneBasedGtuTemplate templateGtuType = parseGtuTemplate(templateTag, definitions, streamInformation,
                            generatorTag, routeGenerator, strategicalFactory);
                    gtuTypeDistribution.add(new FrequencyAndObject<>(1.0, templateGtuType));
                }
                else if (generatorTag.getGtuTemplateMix() != null)
                {
                    Throw.when(demand.getGtuTemplateMix() == null, XmlParserException.class,
                            "GtuTemplateMix %s cannot be found, there are no mixes defined.",
                            generatorTag.getGtuTemplateMix());
                    GtuTemplateMix gtuTemplateMix = null;
                    for (GtuTemplateMix mix : demand.getGtuTemplateMix())
                    {
                        if (generatorTag.getGtuTemplateMix().equals(mix.getId()))
                        {
                            gtuTemplateMix = mix;
                            break;
                        }
                    }
                    Throw.when(gtuTemplateMix == null, XmlParserException.class, "GtuTemplateMix %s is not defined.",
                            generatorTag.getGtuTemplateMix());
                    StreamInterface mixStream = gtuTemplateMix.getRandomStream() == null ? stream
                            : ParseUtil.findStream(streamInformation, gtuTemplateMix.getRandomStream());
                    gtuTypeDistribution = new Distribution<>(mixStream);
                    for (org.opentrafficsim.xml.generated.GtuTemplateMix.GtuTemplate template : gtuTemplateMix.getGtuTemplate())
                    {
                        Throw.when(!gtuTemplates.containsKey(template.getId()), XmlParserException.class,
                                "GtuTemplate %s is not defined.", template.getId());
                        LaneBasedGtuTemplate templateGtuType = parseGtuTemplate(gtuTemplates.get(template.getId()), definitions,
                                streamInformation, generatorTag, routeGenerator, strategicalFactory);
                        gtuTypeDistribution
                                .add(new FrequencyAndObject<LaneBasedGtuTemplate>(template.getWeight(), templateGtuType));
                    }
                }
                else
                {
                    throw new XmlParserException("No GTU information in Generator");
                }

                RoomChecker roomChecker = Transformer.parseRoomChecker(generatorTag.getRoomChecker());

                Generator<Duration> headwayGenerator = new HeadwayGenerator(generatorTag.getFrequency(), stream);

                CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(generatorTag.getLink());
                Lane lane = (Lane) link.getCrossSectionElement(generatorTag.getLane());
                Length position = Transformer.parseLengthBeginEnd(generatorTag.getPosition(), lane.getLength());
                Set<LanePosition> initialLongitudinalPositions = new LinkedHashSet<>();
                initialLongitudinalPositions.add(new LanePosition(lane, position));

                IdGenerator idGenerator = new IdGenerator(lane.getFullId());

                LaneBasedGtuTemplateDistribution characteristicsGenerator =
                        new LaneBasedGtuTemplateDistribution(gtuTypeDistribution);
                generators.add(new LaneBasedGtuGenerator(lane.getFullId(), headwayGenerator, characteristicsGenerator,
                        GeneratorPositions.create(initialLongitudinalPositions, stream), otsNetwork, simulator, roomChecker,
                        idGenerator));
            }
        }
        catch (Exception exception)
        {
            throw new XmlParserException(exception);
        }
        return generators;
    }

    /**
     * Parse a GtuTemplate.
     * @param templateTag GtuTemplate; tag of the GTU template.
     * @param definitions Definitions; definitions.
     * @param streamInformation StreamInformation; stream information.
     * @param generatorTag Generator; generator tag of generator for which GtuTemplate will be used.
     * @param routeGenerator Generator&lt;Route&gt;; route generator.
     * @param strategicalFactory LaneBasedStrategicalRoutePlannerFactory; strategical factory.
     * @return LaneBasedGtuTemplate; parsed GTU template.
     * @throws XmlParserException if the GtuType is not defined.
     */
    private static LaneBasedGtuTemplate parseGtuTemplate(final GtuTemplate templateTag, final Definitions definitions,
            final StreamInformation streamInformation, final org.opentrafficsim.xml.generated.Generator generatorTag,
            final Generator<Route> routeGenerator, final LaneBasedStrategicalRoutePlannerFactory strategicalFactory)
            throws XmlParserException
    {
        GtuType gtuType = definitions.get(GtuType.class, templateTag.getGtuType());
        if (gtuType == null)
            throw new XmlParserException("GTUTYPE " + templateTag.getGtuType() + " in GtuTemplate "
                    + generatorTag.getGtuTemplate() + " not defined");
        Generator<Length> lengthGenerator = Generators.makeLengthGenerator(streamInformation, templateTag.getLengthDist());
        Generator<Length> widthGenerator = Generators.makeLengthGenerator(streamInformation, templateTag.getWidthDist());
        Generator<Speed> maximumSpeedGenerator =
                Generators.makeSpeedGenerator(streamInformation, templateTag.getMaxSpeedDist());
        LaneBasedGtuTemplate templateGtuType = new LaneBasedGtuTemplate(gtuType, lengthGenerator, widthGenerator,
                maximumSpeedGenerator, strategicalFactory, routeGenerator);
        return templateGtuType;
    }

    /**
     * Parse the Sinks.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param demand Network; the Network tag
     * @param simulator OtsSimulatorInterface; the simulator
     * @param definitions Definitions; type definitions.
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    public static void parseSinks(final RoadNetwork otsNetwork, final Demand demand, final OtsSimulatorInterface simulator,
            final Definitions definitions) throws NetworkException
    {
        for (Sink sinkTag : demand.getSink())
        {
            CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(sinkTag.getLink());
            Lane lane = (Lane) link.getCrossSectionElement(sinkTag.getLane());
            Length position = Transformer.parseLengthBeginEnd(sinkTag.getPosition(), lane.getLength());
            DetectorType detectorType = definitions.get(DetectorType.class, sinkTag.getType());
            new SinkDetector(lane, position, simulator, detectorType);
        }
    }

    /**
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private static class HeadwayGenerator implements Generator<Duration>
    {
        /** Demand level. */
        private final Frequency demand;

        /** the stream information. */
        private final StreamInterface stream;

        /**
         * @param demand Frequency; demand
         * @param stream the stream to use for generation
         */
        HeadwayGenerator(final Frequency demand, final StreamInterface stream)
        {
            this.demand = demand;
            this.stream = stream;
        }

        /** {@inheritDoc} */
        @Override
        public Duration draw() throws ProbabilityException, ParameterException
        {
            return new Duration(-Math.log(this.stream.nextDouble()) / this.demand.si, DurationUnit.SI);
        }

    }
}

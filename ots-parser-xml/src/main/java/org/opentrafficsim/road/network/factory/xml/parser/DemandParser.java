package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.base.AbstractDoubleScalarRel;
import org.djutils.exceptions.Throw;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkWeight;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.ProbabilisticRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.InputParameters;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
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
import org.opentrafficsim.road.network.factory.xml.utils.ParseDistribution;
import org.opentrafficsim.road.network.factory.xml.utils.ParseUtil;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.detector.DetectorType;
import org.opentrafficsim.road.network.lane.object.detector.SinkDetector;
import org.opentrafficsim.xml.bindings.types.StringType;
import org.opentrafficsim.xml.generated.ConstantDistType;
import org.opentrafficsim.xml.generated.Demand;
import org.opentrafficsim.xml.generated.GtuTemplate;
import org.opentrafficsim.xml.generated.GtuTemplateMix;
import org.opentrafficsim.xml.generated.RouteMix;
import org.opentrafficsim.xml.generated.ShortestRoute;
import org.opentrafficsim.xml.generated.ShortestRoute.Cost;
import org.opentrafficsim.xml.generated.ShortestRouteMix;
import org.opentrafficsim.xml.generated.Sink;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * This utility class parses all demand related elements that are <i>not</i> from an OD.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class DemandParser
{
    /** */
    private DemandParser()
    {
        // utility class
    }

    /**
     * Parse the ROUTE tags.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param definitions Definitions; parsed definitions
     * @param demand Demand; the Demand tag
     * @param inputParameters InputParameters; input parameters.
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    static void parseRoutes(final RoadNetwork otsNetwork, final Definitions definitions, final Demand demand,
            final InputParameters inputParameters) throws NetworkException
    {
        for (org.opentrafficsim.xml.generated.Route routeTag : demand.getRoute())
        {
            String gtuTypeString = routeTag.getGtuType().get(inputParameters);
            GtuType gtuType = definitions.get(GtuType.class, gtuTypeString);
            Route route = new Route(routeTag.getId(), gtuType);
            Throw.when(gtuType == null, NetworkException.class, "GtuType %s not found in Route %s", gtuTypeString,
                    routeTag.getId());
            for (StringType nodeTag : routeTag.getNode())
            {
                Node node = otsNetwork.getNode(nodeTag.get(inputParameters));
                Throw.when(node == null, NetworkException.class, "Node %s not found in Route %s", nodeTag, routeTag.getId());
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
     * @param inputParameters InputParameters; input parameters.
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    static void parseShortestRoutes(final RoadNetwork otsNetwork, final Definitions definitions, final Demand demand,
            final InputParameters inputParameters) throws NetworkException
    {
        for (ShortestRoute shortestRouteTag : demand.getShortestRoute())
        {
            String gtuTypeId = shortestRouteTag.getGtuType().get(inputParameters);
            GtuType gtuType = definitions.get(GtuType.class, gtuTypeId);
            Throw.when(gtuType == null, NetworkException.class, "GtuType %s not found in ShortestRoute %s",
                    shortestRouteTag.getGtuType(), shortestRouteTag.getId());
            Route route = new Route(shortestRouteTag.getId(), gtuType);
            String nodeFromId = shortestRouteTag.getFrom().get(inputParameters);
            Node nodeFrom = otsNetwork.getNode(nodeFromId);
            Throw.when(nodeFrom == null, NetworkException.class, "From Node %s not found in ShortestRoute", nodeFromId,
                    shortestRouteTag.getId());
            String nodeToId = shortestRouteTag.getTo().get(inputParameters);
            Node nodeTo = otsNetwork.getNode(nodeToId);
            Throw.when(nodeTo == null, NetworkException.class, "To Node %s not found in ShortestRoute", nodeToId,
                    shortestRouteTag.getId());
            List<Node> nodesVia = new ArrayList<>();
            for (StringType nodeViaValue : shortestRouteTag.getVia())
            {
                String nodeViaId = nodeViaValue.get(inputParameters);
                Node nodeVia = otsNetwork.getNode(nodeViaId);
                Throw.when(nodeTo == null, NetworkException.class, "Via Node %s not found in ShortestRoute", nodeViaId,
                        shortestRouteTag.getId());
                nodesVia.add(nodeVia);
            }

            LinkWeight linkWeight;
            Cost cost = shortestRouteTag.getCost();
            if (cost == null || cost.getDistance() != null)
            {
                // Default link weight / standard distance weight
                linkWeight = LinkWeight.ASTAR_LENGTH_NO_CONNECTORS;
            }
            else if (cost.getFreeFlowTime() != null)
            {
                // Free flow time
                Speed maxSpeed = new Speed(250.0, SpeedUnit.KM_PER_HOUR);
                AStarAdmissibleHeuristic<Node> aStarHeuristicTime = getTimeAStarHeuristic(maxSpeed);
                linkWeight = new LinkWeight()
                {
                    /** {@inheritDoc} */
                    @Override
                    public double getWeight(final Link link)
                    {
                        if (link.isConnector())
                        {
                            return 1000000;
                        }
                        Speed speedLimit = link instanceof CrossSectionLink
                                ? getLinkSpeedLimit((CrossSectionLink) link, gtuType) : maxSpeed;
                        return link.getLength().si / speedLimit.si;
                    }

                    /** {@inheritDoc} */
                    @Override
                    public AStarAdmissibleHeuristic<Node> getAStarHeuristic()
                    {
                        return aStarHeuristicTime;
                    }
                };
            }
            else if (cost.getDistanceAndFreeFlowTime() != null)
            {
                // Balance time and distance
                LinearDensity perDistance = cost.getDistanceAndFreeFlowTime().getDistanceCost().get(inputParameters);
                Frequency perTime = cost.getDistanceAndFreeFlowTime().getTimeCost().get(inputParameters);
                Speed maxSpeed = new Speed(250.0, SpeedUnit.KM_PER_HOUR);
                AStarAdmissibleHeuristic<Node> aStarHeuristicTime = getTimeAStarHeuristic(maxSpeed);
                linkWeight = new LinkWeight()
                {
                    /** {@inheritDoc} */
                    @Override
                    public double getWeight(final Link link)
                    {
                        if (link.isConnector())
                        {
                            return 1000000;
                        }
                        Speed speedLimit = link instanceof CrossSectionLink
                                ? getLinkSpeedLimit((CrossSectionLink) link, gtuType) : maxSpeed;
                        return link.getLength().si * perDistance.si + (link.getLength().si / speedLimit.si) * perTime.si;
                    }

                    /** {@inheritDoc} */
                    @Override
                    public AStarAdmissibleHeuristic<Node> getAStarHeuristic()
                    {
                        return new AStarAdmissibleHeuristic<Node>()
                        {
                            /** {@inheritDoc} */
                            @Override
                            public double getCostEstimate(final Node sourceVertex, final Node targetVertex)
                            {
                                return EUCLIDEAN_DISTANCE.getCostEstimate(sourceVertex, targetVertex) * perDistance.si
                                        + aStarHeuristicTime.getCostEstimate(sourceVertex, targetVertex) * perTime.si;
                            }
                        };
                    }
                };
            }
            else
            {
                throw new NetworkException("Shortest route " + shortestRouteTag.getId() + " has invalid cost defined.");
            }

            Route shortestRoute = otsNetwork.getShortestRouteBetween(gtuType, nodeFrom, nodeTo, nodesVia, linkWeight);
            Throw.when(shortestRoute == null, NetworkException.class, "Cannot find shortest route from %s to %s",
                    nodeFrom.getId(), nodeTo.getId());
            for (Node node : shortestRoute.getNodes())
            {
                route.addNode(node);
            }
            otsNetwork.addRoute(gtuType, route);
        }
    }

    /**
     * Returns the speed limit representative for the link. This is the highest speed limit defined on any lane for the GTU
     * type, or the maximum speed limit for any GTU type if no speed limit is defined for the given GTU type.
     * @param link CrossSectionLink; link.
     * @param gtuType GtuType; GTU type.
     * @return Speed; speed limit representative for the link
     */
    private static Speed getLinkSpeedLimit(final CrossSectionLink link, final GtuType gtuType)
    {
        Speed speed = null;
        for (Lane lane : link.getLanes())
        {
            if (lane.getType().isCompatible(gtuType))
            {
                try
                {
                    Speed laneSpeed = lane.getSpeedLimit(gtuType);
                    speed = speed == null || laneSpeed.gt(speed) ? laneSpeed : speed;
                }
                catch (NetworkException e)
                {
                    // just skip
                }
            }
        }
        if (speed == null)
        {
            for (Lane lane : link.getLanes())
            {
                if (lane.getType().isCompatible(gtuType))
                {
                    try
                    {
                        Speed laneSpeed = lane.getHighestSpeedLimit();
                        speed = speed == null || laneSpeed.gt(speed) ? laneSpeed : speed;
                    }
                    catch (NetworkException e)
                    {
                        // just skip
                    }
                }
            }
        }
        return speed;
    }

    /**
     * Returns an A* heuristic that divides the euclidean distance by a maximum speed.
     * @param maxSpeed Speed; maximum speed any GTU could reasonable have.
     * @return A* heuristic that divides the euclidean distance by a maximum speed
     */
    private static AStarAdmissibleHeuristic<Node> getTimeAStarHeuristic(final Speed maxSpeed)
    {
        return new AStarAdmissibleHeuristic<Node>()
        {
            /** {@inheritDoc} */
            @Override
            public double getCostEstimate(final Node sourceVertex, final Node targetVertex)
            {
                return LinkWeight.EUCLIDEAN_DISTANCE.getCostEstimate(sourceVertex, targetVertex) / maxSpeed.si;
            }
        };
    }

    /**
     * Parse the RouteMix tags.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param demand Demand; the Demand tag
     * @param inputParameters InputParameters; input parameters.
     * @return id-based Map of routemix objects as FrequencyAndObject lists
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    static Map<String, List<FrequencyAndObject<Route>>> parseRouteMix(final RoadNetwork otsNetwork, final Demand demand,
            final InputParameters inputParameters) throws NetworkException
    {
        Map<String, List<FrequencyAndObject<Route>>> routeMixMap = new LinkedHashMap<>();
        for (RouteMix routeMixTag : demand.getRouteMix())
        {
            List<FrequencyAndObject<Route>> probRoutes = new ArrayList<>();
            for (RouteMix.Route mixRoute : routeMixTag.getRoute())
            {
                String routeName = mixRoute.getId().get(inputParameters);
                double weight = mixRoute.getWeight().get(inputParameters);
                Route route = otsNetwork.getRoute(routeName);
                Throw.when(route == null, NetworkException.class, "Parsing RouteMix %s -- Route %s not found",
                        routeMixTag.getId(), routeName);
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
     * @param inputParameters InputParameters; input parameters.
     * @return id-based Map of routemix objects as FrequencyAndObject lists
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    static Map<String, List<FrequencyAndObject<Route>>> parseShortestRouteMix(final RoadNetwork otsNetwork, final Demand demand,
            final InputParameters inputParameters) throws NetworkException
    {
        Map<String, List<FrequencyAndObject<Route>>> shortestRouteMixMap = new LinkedHashMap<>();
        for (ShortestRouteMix routeMixTag : demand.getShortestRouteMix())
        {
            List<FrequencyAndObject<Route>> probRoutes = new ArrayList<>();
            for (ShortestRouteMix.ShortestRoute mixRoute : routeMixTag.getShortestRoute())
            {
                String routeName = mixRoute.getId().get(inputParameters);
                double weight = mixRoute.getWeight().get(inputParameters);
                Route route = otsNetwork.getRoute(routeName);
                Throw.when(route == null, NetworkException.class, "Parsing ShortestRouteMix %s -- ShortestRoute %s not found",
                        routeMixTag.getId(), routeName);
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
     * @param inputParameters InputParameters; input parameters.
     * @return list of created GTU generators
     * @throws XmlParserException when the objects cannot be inserted into the network due to inconsistencies
     */
    public static List<LaneBasedGtuGenerator> parseGenerators(final RoadNetwork otsNetwork, final Definitions definitions,
            final Demand demand, final Map<String, GtuTemplate> gtuTemplates,
            final Map<String, List<FrequencyAndObject<Route>>> routeMixMap,
            final Map<String, List<FrequencyAndObject<Route>>> shortestRouteMixMap, final StreamInformation streamInformation,
            final InputParameters inputParameters) throws XmlParserException
    {
        OtsSimulatorInterface simulator = otsNetwork.getSimulator();
        List<LaneBasedGtuGenerator> generators = new ArrayList<>();
        try
        {
            for (org.opentrafficsim.xml.generated.Generator generatorTag : demand.getGenerator())
            {
                StreamInterface stream =
                        ParseUtil.findStream(streamInformation, generatorTag.getRandomStream(), inputParameters);

                String linkId = generatorTag.getLink().get(inputParameters);
                String laneId = generatorTag.getLane().get(inputParameters);

                Generator<Route> routeGenerator;
                if (generatorTag.getRoute() != null)
                {
                    String routeId = generatorTag.getRoute().get(inputParameters);
                    Route route = otsNetwork.getRoute(routeId);
                    Throw.when(route == null, XmlParserException.class, "Generator for Lane %s.%s: Route %s not found", linkId,
                            laneId, routeId);
                    routeGenerator = new FixedRouteGenerator(route);
                }

                else if (generatorTag.getRouteMix() != null)
                {
                    String routeMixId = generatorTag.getRouteMix().get(inputParameters);
                    List<FrequencyAndObject<Route>> routeMix = routeMixMap.get(routeMixId);
                    Throw.when(routeMix == null, XmlParserException.class, "Generator for Lane %s.%s: RouteMix %s not found",
                            linkId, laneId, routeMixId);
                    RouteMix routeMixXml = null;
                    for (RouteMix mix : demand.getRouteMix())
                    {
                        if (mix.getId().equals(routeMixId))
                        {
                            routeMixXml = mix;
                        }
                    }
                    Throw.when(routeMixXml == null, XmlParserException.class, "Route mix '%s' not defined.", routeMixId);
                    StreamInterface routeMixStream =
                            ParseUtil.findStream(streamInformation, routeMixXml.getRandomStream(), inputParameters);
                    try
                    {
                        routeGenerator = new ProbabilisticRouteGenerator(routeMix, routeMixStream);
                    }
                    catch (ProbabilityException exception)
                    {
                        throw new RuntimeException(
                                "Generator for Lane " + linkId + "." + laneId + "Could not generate RouteMix " + routeMixId);
                    }
                }

                else if (generatorTag.getShortestRoute() != null)
                {
                    String shortestRouteId = generatorTag.getShortestRoute().get(inputParameters);
                    Route shortestRoute = otsNetwork.getRoute(shortestRouteId);
                    Throw.when(shortestRoute == null, XmlParserException.class,
                            "Generator for Lane %s.%s: ShortestRoute %s not found", linkId, laneId, shortestRouteId);
                    routeGenerator = new FixedRouteGenerator(shortestRoute);
                }

                else if (generatorTag.getShortestRouteMix() != null)
                {
                    String shortestRouteMixId = generatorTag.getShortestRouteMix().get(inputParameters);
                    List<FrequencyAndObject<Route>> shortestRouteMix = shortestRouteMixMap.get(shortestRouteMixId);
                    Throw.when(shortestRouteMix == null, XmlParserException.class,
                            "Generator for Lane %s.%s: ShortestRouteMix %s not found", linkId, laneId, shortestRouteMixId);
                    ShortestRouteMix shortestRouteMixXml = null;
                    for (ShortestRouteMix mix : demand.getShortestRouteMix())
                    {
                        if (mix.getId().equals(shortestRouteMixId))
                        {
                            shortestRouteMixXml = mix;
                        }
                    }
                    Throw.when(shortestRouteMixXml == null, XmlParserException.class, "Shortest route mix '%s' not defined.",
                            shortestRouteMixId);
                    StreamInterface shortestRouteMixStream =
                            ParseUtil.findStream(streamInformation, shortestRouteMixXml.getRandomStream(), inputParameters);
                    try
                    {
                        routeGenerator = new ProbabilisticRouteGenerator(shortestRouteMix, shortestRouteMixStream);
                    }
                    catch (ProbabilityException exception)
                    {
                        throw new RuntimeException("Generator for Lane " + linkId + "." + laneId
                                + "Could not generate ShortestRouteMix " + shortestRouteMixId);
                    }
                }

                else
                {
                    throw new XmlParserException("Generator for Lane " + linkId + "." + laneId + ": No route information");
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
                    String gtuTemplateId = generatorTag.getGtuTemplate().get(inputParameters);
                    GtuTemplate templateTag = gtuTemplates.get(gtuTemplateId);
                    Throw.when(templateTag == null, XmlParserException.class, "GtuTemplate %s in generator not defined",
                            gtuTemplateId);
                    LaneBasedGtuTemplate templateGtuType = parseGtuTemplate(templateTag, definitions, streamInformation,
                            gtuTemplateId, routeGenerator, strategicalFactory, inputParameters);
                    gtuTypeDistribution.add(new FrequencyAndObject<>(1.0, templateGtuType));
                }
                else if (generatorTag.getGtuTemplateMix() != null)
                {
                    String gtuTemplateMixId = generatorTag.getGtuTemplateMix().get(inputParameters);
                    Throw.when(demand.getGtuTemplateMix() == null, XmlParserException.class,
                            "GtuTemplateMix %s cannot be found, there are no mixes defined.", gtuTemplateMixId);
                    GtuTemplateMix gtuTemplateMix = null;
                    for (GtuTemplateMix mix : demand.getGtuTemplateMix())
                    {
                        if (gtuTemplateMixId.equals(mix.getId()))
                        {
                            gtuTemplateMix = mix;
                            break;
                        }
                    }
                    Throw.when(gtuTemplateMix == null, XmlParserException.class, "GtuTemplateMix %s is not defined.",
                            gtuTemplateMixId);
                    StreamInterface mixStream = gtuTemplateMix.getRandomStream() == null ? stream
                            : ParseUtil.findStream(streamInformation, gtuTemplateMix.getRandomStream(), inputParameters);
                    gtuTypeDistribution = new Distribution<>(mixStream);
                    for (org.opentrafficsim.xml.generated.GtuTemplateMix.GtuTemplate template : gtuTemplateMix.getGtuTemplate())
                    {
                        Throw.when(!gtuTemplates.containsKey(template.getId()), XmlParserException.class,
                                "GtuTemplate %s is not defined.", template.getId());
                        LaneBasedGtuTemplate templateGtuType = parseGtuTemplate(gtuTemplates.get(template.getId()), definitions,
                                streamInformation, gtuTemplateMixId, routeGenerator, strategicalFactory, inputParameters);
                        gtuTypeDistribution.add(new FrequencyAndObject<LaneBasedGtuTemplate>(
                                template.getWeight().get(inputParameters), templateGtuType));
                    }
                }
                else
                {
                    throw new XmlParserException("No GTU information in Generator");
                }

                RoomChecker roomChecker = ParseUtil.parseRoomChecker(generatorTag.getRoomChecker(), inputParameters);

                Generator<Duration> headwayGenerator =
                        new HeadwayGenerator(generatorTag.getFrequency().get(inputParameters), stream);

                CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(linkId);
                Lane lane = (Lane) link.getCrossSectionElement(laneId);
                Length position =
                        ParseUtil.parseLengthBeginEnd(generatorTag.getPosition().get(inputParameters), lane.getLength());
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
     * @param gtuTemplateId String; id of GTU template.
     * @param routeGenerator Generator&lt;Route&gt;; route generator.
     * @param strategicalFactory LaneBasedStrategicalRoutePlannerFactory; strategical factory.
     * @param inputParameters InputParameters; input parameters.
     * @return LaneBasedGtuTemplate; parsed GTU template.
     * @throws XmlParserException if the GtuType is not defined.
     */
    private static LaneBasedGtuTemplate parseGtuTemplate(final GtuTemplate templateTag, final Definitions definitions,
            final StreamInformation streamInformation, final String gtuTemplateId, final Generator<Route> routeGenerator,
            final LaneBasedStrategicalRoutePlannerFactory strategicalFactory, final InputParameters inputParameters)
            throws XmlParserException
    {
        String gtuTypeId = templateTag.getGtuType().get(inputParameters);
        GtuType gtuType = definitions.get(GtuType.class, gtuTypeId);
        Throw.when(gtuType == null, XmlParserException.class, "GtuType %s in GtuTemplate %s not defined", gtuTypeId,
                gtuTemplateId);
        Generator<Length> lengthGenerator = makeGenerator(streamInformation, templateTag.getLengthDist(),
                templateTag.getLengthDist().getLengthUnit().get(inputParameters), inputParameters);
        Generator<Length> widthGenerator = makeGenerator(streamInformation, templateTag.getWidthDist(),
                templateTag.getWidthDist().getLengthUnit().get(inputParameters), inputParameters);
        Generator<Speed> maximumSpeedGenerator = makeGenerator(streamInformation, templateTag.getMaxSpeedDist(),
                templateTag.getMaxSpeedDist().getSpeedUnit().get(inputParameters), inputParameters);
        LaneBasedGtuTemplate templateGtuType = new LaneBasedGtuTemplate(gtuType, lengthGenerator, widthGenerator,
                maximumSpeedGenerator, strategicalFactory, routeGenerator);
        return templateGtuType;
    }

    /**
     * Parse a unit-based distribution into a Generator.
     * @param streamMap StreamInformation; the map with predefined streams
     * @param distribution ConstantDistType; the tag to parse (sub class of ConstantDistType)
     * @param unit U; unit as taken from the tag
     * @param inputParameters InputParameters; input parameters.
     * @return the generator
     * @throws XmlParserException on parse error
     */
    private static <T extends AbstractDoubleScalarRel<U, T>, U extends Unit<U>> Generator<T> makeGenerator(
            final StreamInformation streamMap, final ConstantDistType distribution, final U unit,
            final InputParameters inputParameters) throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Rel<T, U> dist =
                    ParseDistribution.parseContinuousDist(streamMap, distribution, unit, inputParameters);
            Generator<T> generator = new Generator<T>()
            {
                /** {@inheritDoc} */
                @Override
                public T draw() throws ProbabilityException, ParameterException
                {
                    return dist.draw();
                }

                /** {@inheritDoc} */
                @Override
                public String toString()
                {
                    return "Generator<>(" + dist.getDistribution().toString() + " " + dist.getDisplayUnit() + ")";
                }
            };
            return generator;
        }
        catch (Exception exception)
        {
            throw new XmlParserException(exception);
        }
    }

    /**
     * Parse the Sinks.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param demand Network; the Network tag
     * @param simulator OtsSimulatorInterface; the simulator
     * @param definitions Definitions; type definitions.
     * @param inputParameters InputParameters; input parameters.
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    public static void parseSinks(final RoadNetwork otsNetwork, final Demand demand, final OtsSimulatorInterface simulator,
            final Definitions definitions, final InputParameters inputParameters) throws NetworkException
    {
        for (Sink sinkTag : demand.getSink())
        {
            CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(sinkTag.getLink().get(inputParameters));
            Lane lane = (Lane) link.getCrossSectionElement(sinkTag.getLane().get(inputParameters));
            Length position = ParseUtil.parseLengthBeginEnd(sinkTag.getPosition().get(inputParameters), lane.getLength());
            DetectorType detectorType = definitions.get(DetectorType.class, sinkTag.getType().get(inputParameters));
            if (sinkTag.getDestination().get(inputParameters))
            {
                new SinkDetector(lane, position, simulator, detectorType, SinkDetector.DESTINATION);
            }
            else
            {
                new SinkDetector(lane, position, simulator, detectorType);
            }
        }
    }

    /**
     * Standard Poisson arrivals with fixed (mean) arrival rate.
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

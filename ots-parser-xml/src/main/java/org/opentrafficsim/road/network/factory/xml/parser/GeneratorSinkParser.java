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
import org.opentrafficsim.base.parameters.ParameterException;
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
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGtuType;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGtuTypeDistribution;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLmrsPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.Lmrs;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.Generators;
import org.opentrafficsim.road.network.factory.xml.utils.Transformer;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.xml.generated.GENERATOR;
import org.opentrafficsim.xml.generated.GTUTEMPLATE;
import org.opentrafficsim.xml.generated.NETWORKDEMAND;
import org.opentrafficsim.xml.generated.ROUTE;
import org.opentrafficsim.xml.generated.ROUTEMIX;
import org.opentrafficsim.xml.generated.SHORTESTROUTE;
import org.opentrafficsim.xml.generated.SHORTESTROUTEMIX;
import org.opentrafficsim.xml.generated.SINK;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * GeneratorSinkParser.java.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param demand NETWORKDEMAND; the NETWORKDEMAND tag
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseRoutes(final OtsRoadNetwork otsNetwork, final NETWORKDEMAND demand) throws NetworkException
    {
        for (ROUTE routeTag : demand.getROUTE())
        {
            Route route = new Route(routeTag.getID());
            GtuType gtuType = otsNetwork.getGtuType(routeTag.getGTUTYPE());
            if (gtuType == null)
                throw new NetworkException("GTUTYPE " + routeTag.getGTUTYPE() + " not found in ROUTE " + routeTag.getID());
            for (ROUTE.NODE nodeTag : routeTag.getNODE())
            {
                Node node = otsNetwork.getNode(nodeTag.getID());
                if (node == null)
                    throw new NetworkException("NODE " + nodeTag.getID() + " not found in ROUTE " + routeTag.getID());
                route.addNode(node);
            }
            otsNetwork.addRoute(gtuType, route);
        }
    }

    /**
     * Parse the SHORTESTROUTE tags.
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param demand NETWORKDEMAND; the NETWORKDEMAND tag
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseShortestRoutes(final OtsRoadNetwork otsNetwork, final NETWORKDEMAND demand) throws NetworkException
    {
        for (SHORTESTROUTE shortestRouteTag : demand.getSHORTESTROUTE())
        {
            Route route = new Route(shortestRouteTag.getID());
            GtuType gtuType = otsNetwork.getGtuType(shortestRouteTag.getGTUTYPE());
            if (gtuType == null)
                throw new NetworkException(
                        "GTUTYPE " + shortestRouteTag.getGTUTYPE() + " not found in SHORTESTROUTE " + shortestRouteTag.getID());
            Node nodeFrom = otsNetwork.getNode(shortestRouteTag.getFROM().getNODE());
            if (nodeFrom == null)
                throw new NetworkException("FROM NODE " + shortestRouteTag.getFROM().getNODE() + " not found in SHORTESTROUTE "
                        + shortestRouteTag.getID());
            Node nodeTo = otsNetwork.getNode(shortestRouteTag.getTO().getNODE());
            if (nodeTo == null)
                throw new NetworkException("TO NODE " + shortestRouteTag.getTO().getNODE() + " not found in SHORTESTROUTE "
                        + shortestRouteTag.getID());
            List<Node> nodesVia = new ArrayList<>();
            for (SHORTESTROUTE.VIA nodeViaTag : shortestRouteTag.getVIA())
            {
                Node nodeVia = otsNetwork.getNode(nodeViaTag.getNODE());
                if (nodeTo == null)
                    throw new NetworkException(
                            "VIA NODE " + nodeViaTag.getNODE() + " not found in SHORTESTROUTE " + shortestRouteTag.getID());
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
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param demand NETWORKDEMAND; the NETWORKDEMAND tag
     * @return id-based Map of routemix objects as FrequencyAndObject lists
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    @SuppressWarnings("checkstyle:needbraces")
    static Map<String, List<FrequencyAndObject<Route>>> parseRouteMix(final OtsRoadNetwork otsNetwork,
            final NETWORKDEMAND demand) throws NetworkException
    {
        Map<String, List<FrequencyAndObject<Route>>> routeMixMap = new LinkedHashMap<>();
        for (ROUTEMIX routeMixTag : demand.getROUTEMIX())
        {
            List<FrequencyAndObject<Route>> probRoutes = new ArrayList<>();
            for (ROUTEMIX.ROUTE mixRoute : routeMixTag.getROUTE())
            {
                String routeName = mixRoute.getID();
                double weight = mixRoute.getWEIGHT();
                Route route = otsNetwork.getRoute(routeName);
                if (route == null)
                    throw new NetworkException(
                            "Parsing ROUTEMIX " + routeMixTag.getID() + " -- ROUTE " + routeName + " not found");
                probRoutes.add(new FrequencyAndObject<>(weight, route));
            }
            routeMixMap.put(routeMixTag.getID(), probRoutes);
        }
        return routeMixMap;
    }

    /**
     * Parse the SHORTESTROUTEMIX tags.
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param demand NETWORKDEMAND; the NETWORKDEMAND tag
     * @return id-based Map of routemix objects as FrequencyAndObject lists
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    @SuppressWarnings("checkstyle:needbraces")
    static Map<String, List<FrequencyAndObject<Route>>> parseShortestRouteMix(final OtsRoadNetwork otsNetwork,
            final NETWORKDEMAND demand) throws NetworkException
    {
        Map<String, List<FrequencyAndObject<Route>>> shortestRouteMixMap = new LinkedHashMap<>();
        for (SHORTESTROUTEMIX routeMixTag : demand.getSHORTESTROUTEMIX())
        {
            List<FrequencyAndObject<Route>> probRoutes = new ArrayList<>();
            for (SHORTESTROUTEMIX.SHORTESTROUTE mixRoute : routeMixTag.getSHORTESTROUTE())
            {
                String routeName = mixRoute.getID();
                double weight = mixRoute.getWEIGHT();
                Route route = otsNetwork.getRoute(routeName);
                if (route == null)
                    throw new NetworkException(
                            "Parsing SHORTESTROUTEMIX " + routeMixTag.getID() + " -- SHORESTROUTE " + routeName + " not found");
                probRoutes.add(new FrequencyAndObject<>(weight, route));
            }
            shortestRouteMixMap.put(routeMixTag.getID(), probRoutes);
        }
        return shortestRouteMixMap;
    }

    /**
     * Parse the Generators.
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param demand NETWORK; the NETWORK tag
     * @param gtuTemplates GGTUTEMPLATE tags
     * @param routeMixMap map with route mix entries
     * @param shortestRouteMixMap map with shortest route mix entries
     * @param streamInformation map with stream information
     * @return list of created GTU generators
     * @throws XmlParserException when the objects cannot be inserted into the network due to inconsistencies
     */
    @SuppressWarnings("checkstyle:needbraces")
    public static List<LaneBasedGtuGenerator> parseGenerators(final OtsRoadNetwork otsNetwork, final NETWORKDEMAND demand,
            final Map<String, GTUTEMPLATE> gtuTemplates, final Map<String, List<FrequencyAndObject<Route>>> routeMixMap,
            final Map<String, List<FrequencyAndObject<Route>>> shortestRouteMixMap, final StreamInformation streamInformation)
            throws XmlParserException
    {
        OtsSimulatorInterface simulator = otsNetwork.getSimulator();
        List<LaneBasedGtuGenerator> generators = new ArrayList<>();
        try
        {
            for (GENERATOR generatorTag : demand.getGENERATOR())
            {

                if (simulator.getModel().getStream("generation") == null)
                {
                    simulator.getModel().getStreams().put("generation", new MersenneTwister(1L));
                }
                StreamInterface stream = simulator.getModel().getStream("generation");

                Generator<Route> routeGenerator;
                if (generatorTag.getROUTE() != null)
                {
                    Route route = otsNetwork.getRoute(generatorTag.getROUTE());
                    if (route == null)
                        throw new XmlParserException("GENERATOR for LANE " + generatorTag.getLINK() + "."
                                + generatorTag.getLANE() + ": Route " + generatorTag.getROUTE() + " not found");
                    routeGenerator = new FixedRouteGenerator(route);
                }

                else if (generatorTag.getROUTEMIX() != null)
                {
                    List<FrequencyAndObject<Route>> routeMix = routeMixMap.get(generatorTag.getROUTEMIX());
                    if (routeMix == null)
                        throw new XmlParserException("GENERATOR for LANE " + generatorTag.getLINK() + "."
                                + generatorTag.getLANE() + ": RouteMix " + generatorTag.getROUTEMIX() + " not found");
                    try
                    {
                        routeGenerator = new ProbabilisticRouteGenerator(routeMix, stream);
                    }
                    catch (ProbabilityException exception)
                    {
                        throw new RuntimeException("GENERATOR for LANE " + generatorTag.getLINK() + "." + generatorTag.getLANE()
                                + "Could not generate RouteMix " + generatorTag.getROUTEMIX());
                    }
                }

                else if (generatorTag.getSHORTESTROUTE() != null)
                {
                    Route shortestRoute = otsNetwork.getRoute(generatorTag.getSHORTESTROUTE());
                    if (shortestRoute == null)
                        throw new XmlParserException("GENERATOR for LANE " + generatorTag.getLINK() + "."
                                + generatorTag.getLANE() + ": ShortestRoute " + generatorTag.getSHORTESTROUTE() + " not found");
                    routeGenerator = new FixedRouteGenerator(shortestRoute);
                }

                else if (generatorTag.getSHORTESTROUTEMIX() != null)
                {
                    List<FrequencyAndObject<Route>> shortestRouteMix =
                            shortestRouteMixMap.get(generatorTag.getSHORTESTROUTEMIX());
                    if (shortestRouteMix == null)
                        throw new XmlParserException(
                                "GENERATOR for LANE " + generatorTag.getLINK() + "." + generatorTag.getLANE()
                                        + ": ShortestRouteMix " + generatorTag.getSHORTESTROUTEMIX() + " not found");
                    try
                    {
                        routeGenerator = new ProbabilisticRouteGenerator(shortestRouteMix, stream);
                    }
                    catch (ProbabilityException exception)
                    {
                        throw new RuntimeException("GENERATOR for LANE " + generatorTag.getLINK() + "." + generatorTag.getLANE()
                                + "Could not generate ShortestRouteMix " + generatorTag.getSHORTESTROUTEMIX());
                    }
                }

                else
                {
                    throw new XmlParserException("GENERATOR for LANE " + generatorTag.getLINK() + "." + generatorTag.getLANE()
                            + ": No route information");
                }

                CarFollowingModelFactory<IdmPlus> idmPlusFactory =
                        new IdmPlusFactory(streamInformation.getStream("generation"));
                LaneBasedTacticalPlannerFactory<Lmrs> tacticalFactory =
                        new LmrsFactory(idmPlusFactory, new DefaultLmrsPerceptionFactory());
                LaneBasedStrategicalRoutePlannerFactory strategicalFactory =
                        new LaneBasedStrategicalRoutePlannerFactory(tacticalFactory);

                // the distribution of GTUs
                Distribution<LaneBasedTemplateGtuType> gtuTypeDistribution =
                        new Distribution<>(streamInformation.getStream("generation"));
                if (generatorTag.getGTUTEMPLATE() != null)
                {
                    GTUTEMPLATE templateTag = gtuTemplates.get(generatorTag.getGTUTEMPLATE());
                    if (templateTag == null)
                        throw new XmlParserException(
                                "GTUTEMPLATE " + generatorTag.getGTUTEMPLATE() + " in generator not defined");
                    GtuType gtuType = otsNetwork.getGtuType(templateTag.getGTUTYPE());
                    if (gtuType == null)
                        throw new XmlParserException("GTUTYPE " + templateTag.getGTUTYPE() + " in GTUTEMPLATE "
                                + generatorTag.getGTUTEMPLATE() + " not defined");
                    Generator<Length> lengthGenerator =
                            Generators.makeLengthGenerator(streamInformation, templateTag.getLENGTHDIST());
                    Generator<Length> widthGenerator =
                            Generators.makeLengthGenerator(streamInformation, templateTag.getWIDTHDIST());
                    Generator<Speed> maximumSpeedGenerator =
                            Generators.makeSpeedGenerator(streamInformation, templateTag.getMAXSPEEDDIST());
                    LaneBasedTemplateGtuType templateGtuType = new LaneBasedTemplateGtuType(gtuType, lengthGenerator,
                            widthGenerator, maximumSpeedGenerator, strategicalFactory, routeGenerator);
                    gtuTypeDistribution.add(new FrequencyAndObject<>(1.0, templateGtuType));
                }
                else if (generatorTag.getGTUTEMPLATEMIX() != null)
                {
                    // TODO: GTUTEMPLATEMIX
                    throw new XmlParserException("GtuTemplateMix not implemented yet in GENERATOR");
                }
                else
                {
                    throw new XmlParserException("No GTU information in GENERATOR");
                }

                RoomChecker roomChecker = Transformer.parseRoomChecker(generatorTag.getROOMCHECKER());

                Generator<Duration> headwayGenerator =
                        new HeadwayGenerator(generatorTag.getFREQUENCY(), streamInformation.getStream("generation"));

                CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(generatorTag.getLINK());
                Lane lane = (Lane) link.getCrossSectionElement(generatorTag.getLANE());
                // TODO: remove this hack for testing
                Length position = Length.instantiateSI(5.0); // Transformer.parseLengthBeginEnd(generatorTag.getPOSITION(),
                // lane.getLength());
                Set<LanePosition> initialLongitudinalPositions = new LinkedHashSet<>();
                initialLongitudinalPositions.add(new LanePosition(lane, position));

                IdGenerator idGenerator = new IdGenerator(lane.getFullId());

                LaneBasedTemplateGtuTypeDistribution characteristicsGenerator =
                        new LaneBasedTemplateGtuTypeDistribution(gtuTypeDistribution);
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
     * Parse the Sinks.
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param demand NETWORK; the NETWORK tag
     * @param simulator OTSSimulatorInterface; the simulator
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    public static void parseSinks(final OtsRoadNetwork otsNetwork, final NETWORKDEMAND demand,
            final OtsSimulatorInterface simulator) throws NetworkException
    {
        for (SINK sinkTag : demand.getSINK())
        {
            CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(sinkTag.getLINK());
            Lane lane = (Lane) link.getCrossSectionElement(sinkTag.getLANE());
            Length position = Transformer.parseLengthBeginEnd(sinkTag.getPOSITION(), lane.getLength());
            new SinkSensor(lane, position, simulator);
        }
    }

    /**
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
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

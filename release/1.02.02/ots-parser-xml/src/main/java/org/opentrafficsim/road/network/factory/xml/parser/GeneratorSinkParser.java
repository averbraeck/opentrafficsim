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
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.ProbabilisticRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUTypeDistribution;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRS;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.Generators;
import org.opentrafficsim.road.network.factory.xml.utils.StreamInformation;
import org.opentrafficsim.road.network.factory.xml.utils.Transformer;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.xml.generated.GENERATOR;
import org.opentrafficsim.xml.generated.GTUTEMPLATE;
import org.opentrafficsim.xml.generated.NETWORKDEMAND;
import org.opentrafficsim.xml.generated.ROUTE;
import org.opentrafficsim.xml.generated.ROUTEMIX;
import org.opentrafficsim.xml.generated.SHORTESTROUTE;
import org.opentrafficsim.xml.generated.SHORTESTROUTEMIX;
import org.opentrafficsim.xml.generated.SINK;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * GeneratorSinkParser.java. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
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
    static void parseRoutes(final OTSRoadNetwork otsNetwork, final NETWORKDEMAND demand) throws NetworkException
    {
        for (ROUTE routeTag : demand.getROUTE())
        {
            Route route = new Route(routeTag.getID());
            GTUType gtuType = otsNetwork.getGtuType(routeTag.getGTUTYPE());
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
    static void parseShortestRoutes(final OTSRoadNetwork otsNetwork, final NETWORKDEMAND demand) throws NetworkException
    {
        for (SHORTESTROUTE shortestRouteTag : demand.getSHORTESTROUTE())
        {
            Route route = new Route(shortestRouteTag.getID());
            GTUType gtuType = otsNetwork.getGtuType(shortestRouteTag.getGTUTYPE());
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
    static Map<String, List<FrequencyAndObject<Route>>> parseRouteMix(final OTSRoadNetwork otsNetwork,
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
    static Map<String, List<FrequencyAndObject<Route>>> parseShortestRouteMix(final OTSRoadNetwork otsNetwork,
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
     * @param simulator OTSSimulatorInterface; the simulator
     * @param streamMap map with stream information
     * @return list of created GTU generators
     * @throws XmlParserException when the objects cannot be inserted into the network due to inconsistencies
     */
    @SuppressWarnings("checkstyle:needbraces")
    public static List<LaneBasedGTUGenerator> parseGenerators(final OTSRoadNetwork otsNetwork, final NETWORKDEMAND demand,
            final Map<String, GTUTEMPLATE> gtuTemplates, final Map<String, List<FrequencyAndObject<Route>>> routeMixMap,
            final Map<String, List<FrequencyAndObject<Route>>> shortestRouteMixMap, final OTSSimulatorInterface simulator,
            final Map<String, StreamInformation> streamMap) throws XmlParserException
    {
        List<LaneBasedGTUGenerator> generators = new ArrayList<>();
        try
        {
            for (GENERATOR generatorTag : demand.getGENERATOR())
            {

                if (simulator.getReplication().getStream("generation") == null)
                {
                    simulator.getReplication().getStreams().put("generation", new MersenneTwister(1L));
                }
                StreamInterface stream = simulator.getReplication().getStream("generation");

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

                CarFollowingModelFactory<IDMPlus> idmPlusFactory = new IDMPlusFactory(streamMap.get("generation").getStream());
                LaneBasedTacticalPlannerFactory<LMRS> tacticalFactory =
                        new LMRSFactory(idmPlusFactory, new DefaultLMRSPerceptionFactory());
                LaneBasedStrategicalRoutePlannerFactory strategicalFactory =
                        new LaneBasedStrategicalRoutePlannerFactory(tacticalFactory);

                // the distribution of GTUs
                Distribution<LaneBasedTemplateGTUType> gtuTypeDistribution =
                        new Distribution<>(streamMap.get("generation").getStream());
                if (generatorTag.getGTUTEMPLATE() != null)
                {
                    GTUTEMPLATE templateTag = gtuTemplates.get(generatorTag.getGTUTEMPLATE());
                    if (templateTag == null)
                        throw new XmlParserException(
                                "GTUTEMPLATE " + generatorTag.getGTUTEMPLATE() + " in generator not defined");
                    GTUType gtuType = otsNetwork.getGtuType(templateTag.getGTUTYPE());
                    if (gtuType == null)
                        throw new XmlParserException("GTUTYPE " + templateTag.getGTUTYPE() + " in GTUTEMPLATE "
                                + generatorTag.getGTUTEMPLATE() + " not defined");
                    Generator<Length> lengthGenerator = Generators.makeLengthGenerator(streamMap, templateTag.getLENGTHDIST());
                    Generator<Length> widthGenerator = Generators.makeLengthGenerator(streamMap, templateTag.getWIDTHDIST());
                    Generator<Speed> maximumSpeedGenerator =
                            Generators.makeSpeedGenerator(streamMap, templateTag.getMAXSPEEDDIST());
                    LaneBasedTemplateGTUType templateGTUType = new LaneBasedTemplateGTUType(gtuType, lengthGenerator,
                            widthGenerator, maximumSpeedGenerator, strategicalFactory, routeGenerator);
                    gtuTypeDistribution.add(new FrequencyAndObject<>(1.0, templateGTUType));
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
                        new HeadwayGenerator(generatorTag.getFREQUENCY(), streamMap.get("generation").getStream());

                CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(generatorTag.getLINK());
                Lane lane = (Lane) link.getCrossSectionElement(generatorTag.getLANE());
                // TODO: remove this hack for testing
                Length position = Length.createSI(5.0); // Transformer.parseLengthBeginEnd(generatorTag.getPOSITION(),
                                                        // lane.getLength());
                GTUDirectionality direction = GTUDirectionality.valueOf(generatorTag.getDIRECTION());
                Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>();
                initialLongitudinalPositions.add(new DirectedLanePosition(lane, position, direction));

                IdGenerator idGenerator = new IdGenerator(lane.getFullId());

                LaneBasedTemplateGTUTypeDistribution characteristicsGenerator =
                        new LaneBasedTemplateGTUTypeDistribution(gtuTypeDistribution);
                generators.add(new LaneBasedGTUGenerator(lane.getFullId(), headwayGenerator, characteristicsGenerator,
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
    public static void parseSinks(final OTSRoadNetwork otsNetwork, final NETWORKDEMAND demand,
            final OTSSimulatorInterface simulator) throws NetworkException
    {
        for (SINK sinkTag : demand.getSINK())
        {
            CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(sinkTag.getLINK());
            Lane lane = (Lane) link.getCrossSectionElement(sinkTag.getLANE());
            Length position = Transformer.parseLengthBeginEnd(sinkTag.getPOSITION(), lane.getLength());
            new SinkSensor(lane, position, GTUDirectionality.valueOf(sinkTag.getDIRECTION()), simulator);
        }
    }

    /**
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 jan. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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

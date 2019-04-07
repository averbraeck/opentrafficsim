package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
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
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.generator.CFBARoomChecker;
import org.opentrafficsim.road.gtu.generator.CFRoomChecker;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUTypeDistribution;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
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
public class GeneratorSinkParser
{

    /** */
    private GeneratorSinkParser()
    {
        // utility class
    }

    /**
     * Parse the Generators.
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param demand NETWORK; the NETWORK tag
     * @param gtuTemplates GGTUTEMPLATE tags
     * @param simulator OTSSimulatorInterface; the simulator
     * @param streamMap map with stream information
     * @throws XmlParserException when the objects cannot be inserted into the network due to inconsistencies
     */
    public static void parseGenerators(final OTSRoadNetwork otsNetwork, final NETWORKDEMAND demand,
            Map<String, GTUTEMPLATE> gtuTemplates, final OTSSimulatorInterface simulator,
            Map<String, StreamInformation> streamMap) throws XmlParserException
    {
        try
        {
            for (GENERATOR generatorTag : demand.getGENERATOR())
            {
                CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(generatorTag.getLINK());
                Lane lane = (Lane) link.getCrossSectionElement(generatorTag.getLANE());
                Length position = Transformer.parseLengthBeginEnd(generatorTag.getPOSITION(), lane.getLength());

                Class<?> gtuClass = LaneBasedIndividualGTU.class;

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
                        throw new XmlParserException("Route " + generatorTag.getROUTE() + " not found");
                    routeGenerator = new FixedRouteGenerator(route);
                }
                else if (generatorTag.getROUTEMIX() != null)
                {
                    // TODO: ROUTEMIX
                    throw new XmlParserException("RouteMix not implemented yet in GENERATOR");
                    /*-
                    List<FrequencyAndObject<Route>> probRoutes = new ArrayList<>();
                    for (int i = 0; i < generatorTag.routeMixTag.weights.size(); i++)
                    {
                    List<Node> nodeList = new ArrayList<>();
                    for (NodeTag nodeTag : generatorTag.routeMixTag.routes.get(i).routeNodeTags)
                    {
                        nodeList.add(parser.nodeTags.get(nodeTag.name).node);
                    }
                    probRoutes.add(new FrequencyAndObject<>(generatorTag.routeMixTag.weights.get(i),
                            new Route(generatorTag.routeMixTag.routes.get(i).name, nodeList)));
                    }
                    try
                    {
                    routeGenerator = new ProbabilisticRouteGenerator(probRoutes, stream);
                    }
                    catch (ProbabilityException exception)
                    {
                    throw new RuntimeException("Could not generate route mix.");
                    }
                    */
                }
                else if (generatorTag.getSHORTESTROUTE() != null)
                {
                    Route shortestRoute = otsNetwork.getRoute(generatorTag.getSHORTESTROUTE());
                    if (shortestRoute == null)
                        throw new XmlParserException("ShortestRoute " + generatorTag.getSHORTESTROUTE() + " not found");
                    routeGenerator = new FixedRouteGenerator(shortestRoute);
                }
                else if (generatorTag.getSHORTESTROUTE() != null)
                {
                    // TODO: SHORTESTROUTEMIX
                    throw new XmlParserException("ShortestRouteMix not implemented yet in GENERATOR");
                }
                else
                {
                    throw new XmlParserException("No route information in GENERATOR");
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
                    gtuTypeDistribution.add(new FrequencyAndObject<LaneBasedTemplateGTUType>(1.0, templateGTUType));
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

                Time startTime = generatorTag.getSTARTTIME() != null ? generatorTag.getSTARTTIME() : Time.ZERO;
                Time endTime = generatorTag.getENDTIME() != null ? generatorTag.getENDTIME()
                        : new Time(Double.MAX_VALUE, TimeUnit.BASE_SECOND);

                // room checker: CF|CFBA|TTC(\d*(\.\d\d*)s)
                RoomChecker roomChecker = null;

                switch (generatorTag.getROOMCHECKER())
                {
                    case "CF":
                        roomChecker = new CFRoomChecker();
                        break;

                    case "CFBA":
                        roomChecker = new CFBARoomChecker();
                        break;

                    case "TTC":
                        // TODO: TTC
                        // break;

                    default:
                        throw new XmlParserException(
                                "RoomChecker " + generatorTag.getROOMCHECKER() + " not one of CF|CFBA|TTC");
                }

                IdGenerator idGenerator = new IdGenerator(lane.getFullId());

                Generator<Duration> headwayGenerator =
                        new HeadwayGenerator(generatorTag.getFREQUENCY(), streamMap.get("generation").getStream());

                Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>();
                // TODO: DIR_MINUS
                initialLongitudinalPositions
                        .add(new DirectedLanePosition(lane, new Length(5.0, LengthUnit.SI), GTUDirectionality.DIR_PLUS));

                LaneBasedTemplateGTUTypeDistribution characteristicsGenerator =
                        new LaneBasedTemplateGTUTypeDistribution(gtuTypeDistribution);
                new LaneBasedGTUGenerator(lane.getFullId(), headwayGenerator, characteristicsGenerator,
                        GeneratorPositions.create(initialLongitudinalPositions, stream), otsNetwork, simulator, roomChecker,
                        idGenerator);
            }
        }
        catch (Exception exception)
        {
            throw new XmlParserException(exception);
        }
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
            new SinkSensor(lane, position, simulator);
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

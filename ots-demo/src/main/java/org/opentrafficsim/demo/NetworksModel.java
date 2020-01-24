package org.opentrafficsim.demo;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.base.DoubleScalar;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventType;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.ProbabilisticRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.generator.CFRoomChecker;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUTypeDistribution;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterSelectionMap;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistErlang;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Simulate a single lane road of 5 km length. Vehicles are generated at a constant rate of 1500 veh/hour. At time 300s a
 * blockade is inserted at position 4 km; this blockade is removed at time 500s. The used car following algorithm is IDM+
 * <a href="http://opentrafficsim.org/downloads/MOTUS%20reference.pdf"><i>Integrated Lane Change Model with Relaxation and
 * Synchronization</i>, by Wouter J. Schakel, Victor L. Knoop and Bart van Arem, 2012</a>. <br>
 * Output is a set of block charts:
 * <ul>
 * <li>Traffic density</li>
 * <li>Speed</li>
 * <li>Flow</li>
 * <li>Acceleration</li>
 * </ul>
 * All these graphs display simulation time along the horizontal axis and distance along the road along the vertical axis.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2019-01-06 01:35:05 +0100 (Sun, 06 Jan 2019) $, @version $Revision: 4831 $, by $Author: averbraeck $,
 * initial version ug 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class NetworksModel extends AbstractOTSModel implements EventListenerInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** The network. */
    private final OTSRoadNetwork network = new OTSRoadNetwork("network", true);

    /** Strategical planner generator for cars. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactoryCars = null;

    /** Strategical planner generator for trucks. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactoryTrucks = null;

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** Minimum distance. */
    private Length minimumDistance = new Length(0, METER);

    /** Maximum distance. */
    private Length maximumDistance = new Length(5000, METER);

    /** The random number generator used to decide what kind of GTU to generate. */
    private StreamInterface stream = new MersenneTwister(12345);

    /** The route generator for the main line. */
    private Generator<Route> routeGeneratorMain;

    /** The route generator for the onramp. */
    private Generator<Route> routeGeneratorRamp;

    /** The speed limit. */
    private Speed speedLimit = new Speed(60, KM_PER_HOUR);

    /** The sequence of Lanes that all vehicles will follow. */
    private List<List<Lane>> paths = new ArrayList<>();

    /** Id generator (used by all generators). */
    private IdGenerator idGenerator = new IdGenerator("");

    /** The probability distribution for the variable part of the headway. */
    private DistContinuous headwayGenerator;

    /**
     * @param simulator OTSSimulatorInterface; the simulator for this model
     */
    public NetworksModel(final OTSSimulatorInterface simulator)
    {
        super(simulator);
        createInputParameters();
    }

    /**
     * Create input parameters for the networks demo.
     */
    private void createInputParameters()
    {
        InputParameterHelper.makeInputParameterMapCarTruck(this.inputParameterMap, 1.0);
        try
        {
            InputParameterMap genericMap = (InputParameterMap) this.inputParameterMap.get("generic");

            genericMap.add(new InputParameterDouble("flow", "Flow per input lane", "Traffic flow per input lane", 500d, 0d,
                    3000d, true, true, "%.0f veh/h", 1.5));

            SortedMap<String, String> networks = new TreeMap<>();
            networks.put("Merge 1 plus 1 into 1", "M111");
            networks.put("Merge 2 plus 1 into 2", "M212");
            networks.put("Merge 2 plus 2 into 4", "M224");
            networks.put("Split 1 into 1 plus 1", "S111");
            networks.put("Split 2 into 1 plus 2", "S212");
            networks.put("Split 4 into 2 plus 2", "S422");
            InputParameterSelectionMap<String, String> paramSelect = new InputParameterSelectionMap<String, String>("network",
                    "Network to run simulation for", "Network to run simulaton for", networks, "M111", 2.0);
            genericMap.add(paramSelect);
        }
        catch (InputParameterException exception)
        {
            exception.printStackTrace();
        }

    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:methodlength")
    public final void constructModel() throws SimRuntimeException
    {
        this.network.addListener(this, Network.GTU_ADD_EVENT);
        this.network.addListener(this, Network.GTU_REMOVE_EVENT);
        try
        {
            GTUType car = this.network.getGtuType(GTUType.DEFAULTS.CAR);
            this.carProbability = (double) getInputParameter("generic.carProbability");

            ParameterFactory params = new InputParameterHelper(getInputParameterMap());
            this.strategicalPlannerFactoryCars = new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()), params);
            this.strategicalPlannerFactoryTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()), params);

            OTSPoint3D pFrom2a = new OTSPoint3D(0, -50, 0);
            OTSPoint3D pFrom2b = new OTSPoint3D(490, -0.5, 0);
            Direction onrampDirection = pFrom2a.horizontalDirection(pFrom2b);
            OTSRoadNode from = new OTSRoadNode(this.network, "From", new OTSPoint3D(0, 0, 0), Direction.ZERO);
            OTSRoadNode end = new OTSRoadNode(this.network, "End", new OTSPoint3D(2000, 0, 0), Direction.ZERO);
            OTSRoadNode from2a = new OTSRoadNode(this.network, "From2a", pFrom2a, onrampDirection);
            OTSRoadNode from2b = new OTSRoadNode(this.network, "From2b", pFrom2b, onrampDirection);
            OTSRoadNode firstVia = new OTSRoadNode(this.network, "Via1", new OTSPoint3D(500, 0, 0), Direction.ZERO);
            OTSPoint3D pEnd2a = new OTSPoint3D(1020, -0.5, 0);
            OTSPoint3D pEnd2b = new OTSPoint3D(2000, -50, 0);
            Direction offrampDirection = pEnd2a.horizontalDirection(pEnd2b);
            OTSRoadNode end2a = new OTSRoadNode(this.network, "End2a", pEnd2a, offrampDirection);
            OTSRoadNode end2b = new OTSRoadNode(this.network, "End2b", pEnd2b, offrampDirection);
            OTSRoadNode secondVia = new OTSRoadNode(this.network, "Via2", new OTSPoint3D(1000, 0, 0), Direction.ZERO);

            String networkType = getInputParameter("generic.network").toString();
            boolean merge = networkType.startsWith("M");
            int lanesOnMain = Integer.parseInt("" + networkType.charAt(merge ? 1 : 3));
            int lanesOnBranch = Integer.parseInt("" + networkType.charAt(2));
            int lanesOnCommon = lanesOnMain + lanesOnBranch;
            int lanesOnCommonCompressed = Integer.parseInt("" + networkType.charAt(merge ? 3 : 1));

            double contP = (double) getInputParameter("generic.flow");
            Duration averageHeadway = new Duration(3600.0 / contP, SECOND);
            Duration minimumHeadway = new Duration(3, SECOND);
            this.headwayGenerator =
                    new DistErlang(new MersenneTwister(1234), 4, DoubleScalar.minus(averageHeadway, minimumHeadway).getSI());

            LaneType laneType = this.network.getLaneType(LaneType.DEFAULTS.TWO_WAY_LANE);
            if (merge)
            {
                // provide a route -- at the merge point, the GTU can otherwise decide to "go back"
                ArrayList<Node> mainRouteNodes = new ArrayList<>();
                mainRouteNodes.add(from);
                mainRouteNodes.add(firstVia);
                mainRouteNodes.add(secondVia);
                mainRouteNodes.add(end);
                Route mainRoute = new Route("main", mainRouteNodes);
                this.routeGeneratorMain = new FixedRouteGenerator(mainRoute);

                ArrayList<Node> rampRouteNodes = new ArrayList<>();
                rampRouteNodes.add(from2a);
                rampRouteNodes.add(from2b);
                rampRouteNodes.add(firstVia);
                rampRouteNodes.add(secondVia);
                rampRouteNodes.add(end);
                Route rampRoute = new Route("ramp", rampRouteNodes);
                this.routeGeneratorRamp = new FixedRouteGenerator(rampRoute);
            }
            else
            {
                // determine the routes
                List<FrequencyAndObject<Route>> routeProbabilities = new ArrayList<>();

                ArrayList<Node> mainRouteNodes = new ArrayList<>();
                mainRouteNodes.add(from);
                mainRouteNodes.add(firstVia);
                mainRouteNodes.add(secondVia);
                mainRouteNodes.add(end);
                Route mainRoute = new Route("main", mainRouteNodes);
                routeProbabilities.add(new FrequencyAndObject<>(lanesOnMain, mainRoute));

                ArrayList<Node> sideRouteNodes = new ArrayList<>();
                sideRouteNodes.add(from);
                sideRouteNodes.add(firstVia);
                sideRouteNodes.add(secondVia);
                sideRouteNodes.add(end2a);
                sideRouteNodes.add(end2b);
                Route sideRoute = new Route("side", sideRouteNodes);
                routeProbabilities.add(new FrequencyAndObject<>(lanesOnBranch, sideRoute));
                try
                {
                    this.routeGeneratorMain = new ProbabilisticRouteGenerator(routeProbabilities, new MersenneTwister(1234));
                }
                catch (ProbabilityException exception)
                {
                    exception.printStackTrace();
                }
            }

            if (merge)
            {
                setupGenerator(LaneFactory.makeMultiLane(this.network, "From2a to From2b", from2a, from2b, null, lanesOnBranch,
                        0, lanesOnCommon - lanesOnBranch, laneType, this.speedLimit, this.simulator));
                LaneFactory.makeMultiLaneBezier(this.network, "From2b to FirstVia", from2a, from2b, firstVia, secondVia,
                        lanesOnBranch, lanesOnCommon - lanesOnBranch, lanesOnCommon - lanesOnBranch, laneType, this.speedLimit,
                        this.simulator);
            }
            else
            {
                LaneFactory.makeMultiLaneBezier(this.network, "SecondVia to end2a", firstVia, secondVia, end2a, end2b,
                        lanesOnBranch, lanesOnCommon - lanesOnBranch, lanesOnCommon - lanesOnBranch, laneType, this.speedLimit,
                        this.simulator);
                setupSink(LaneFactory.makeMultiLane(this.network, "end2a to end2b", end2a, end2b, null, lanesOnBranch,
                        lanesOnCommon - lanesOnBranch, 0, laneType, this.speedLimit, this.simulator), laneType);
            }

            Lane[] startLanes = LaneFactory.makeMultiLane(this.network, "From to FirstVia", from, firstVia, null,
                    merge ? lanesOnMain : lanesOnCommonCompressed, laneType, this.speedLimit, this.simulator);
            setupGenerator(startLanes);
            Lane[] common = LaneFactory.makeMultiLane(this.network, "FirstVia to SecondVia", firstVia, secondVia, null,
                    lanesOnCommon, laneType, this.speedLimit, this.simulator);
            setupSink(
                    LaneFactory.makeMultiLane(this.network, "SecondVia to end", secondVia, end, null,
                            merge ? lanesOnCommonCompressed : lanesOnMain, laneType, this.speedLimit, this.simulator),
                    laneType);

            for (int index = 0; index < lanesOnCommon; index++)
            {
                this.paths.add(new ArrayList<Lane>());
                Lane lane = common[index];
                // Follow back
                while (lane.prevLanes(car).size() > 0)
                {
                    if (lane.prevLanes(car).size() > 1)
                    {
                        throw new NetworkException("This network should not have lane merge points");
                    }
                    lane = lane.prevLanes(car).keySet().iterator().next();
                }
                // Follow forward
                while (true)
                {
                    this.paths.get(index).add(lane);
                    int branching = lane.nextLanes(car).size();
                    if (branching == 0)
                    {
                        break;
                    }
                    if (branching > 1)
                    {
                        throw new NetworkException("This network should not have lane split points");
                    }
                    lane = lane.nextLanes(car).keySet().iterator().next();
                }
            }
        }
        catch (SimRuntimeException | NetworkException | OTSGeometryException | InputParameterException | GTUException
                | ParameterException | NamingException | ProbabilityException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add a generator to an array of Lane.
     * @param lanes Lane[]; the lanes that must get a generator at the start
     * @return Lane[]; the lanes
     * @throws GTUException when lane position out of bounds
     * @throws SimRuntimeException when generation scheduling fails
     * @throws ProbabilityException when probability distribution is wrong
     * @throws ParameterException when a parameter is missing for the perception of the GTU
     */
    private Lane[] setupGenerator(final Lane[] lanes)
            throws SimRuntimeException, GTUException, ProbabilityException, ParameterException
    {
        for (Lane lane : lanes)
        {
            makeGenerator(lane);
        }
        return lanes;
    }

    /**
     * Build a generator.
     * @param lane Lane; the lane on which the generated GTUs are placed
     * @return LaneBasedGTUGenerator
     * @throws GTUException when lane position out of bounds
     * @throws SimRuntimeException when generation scheduling fails
     * @throws ProbabilityException when probability distribution is wrong
     * @throws ParameterException when a parameter is missing for the perception of the GTU
     */
    private LaneBasedGTUGenerator makeGenerator(final Lane lane)
            throws GTUException, SimRuntimeException, ProbabilityException, ParameterException
    {
        Distribution<LaneBasedTemplateGTUType> distribution = new Distribution<>(this.stream);
        Length initialPosition = new Length(16, METER);
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));

        LaneBasedTemplateGTUType template = makeTemplate(this.stream, lane,
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistUniform(this.stream, 3, 6), METER),
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistUniform(this.stream, 1.6, 2.0), METER),
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistUniform(this.stream, 140, 180), KM_PER_HOUR),
                initialPositions, this.strategicalPlannerFactoryCars);
        // System.out.println("Constructed template " + template);
        distribution.add(new FrequencyAndObject<>(this.carProbability, template));
        template = makeTemplate(this.stream, lane,
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistUniform(this.stream, 8, 14), METER),
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistUniform(this.stream, 2.0, 2.5), METER),
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistUniform(this.stream, 100, 140), KM_PER_HOUR),
                initialPositions, this.strategicalPlannerFactoryTrucks);
        // System.out.println("Constructed template " + template);
        distribution.add(new FrequencyAndObject<>(1.0 - this.carProbability, template));
        LaneBasedTemplateGTUTypeDistribution templateDistribution = new LaneBasedTemplateGTUTypeDistribution(distribution);
        LaneBasedGTUGenerator.RoomChecker roomChecker = new CFRoomChecker();
        return new LaneBasedGTUGenerator(lane.getId(), new Generator<Duration>()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public Duration draw()
            {
                return new Duration(NetworksModel.this.headwayGenerator.draw(), DurationUnit.SI);
            }
        }, templateDistribution, GeneratorPositions.create(initialPositions, this.stream), this.network, this.simulator,
                roomChecker, this.idGenerator);
    }

    /**
     * @param randStream StreamInterface; the random stream to use
     * @param lane Lane; reference lane to generate GTUs on
     * @param lengthDistribution ContinuousDistDoubleScalar.Rel&lt;Length,LengthUnit&gt;; distribution of the GTU length
     * @param widthDistribution ContinuousDistDoubleScalar.Rel&lt;Length,LengthUnit&gt;; distribution of the GTU width
     * @param maximumSpeedDistribution ContinuousDistDoubleScalar.Rel&lt;Speed,SpeedUnit&gt;; distribution of the GTU's maximum
     *            speed
     * @param initialPositions Set&lt;DirectedLanePosition&gt;; initial position(s) of the GTU on the Lane(s)
     * @param strategicalPlannerFactory LaneBasedStrategicalPlannerFactory&lt;LaneBasedStrategicalPlanner&gt;; factory to
     *            generate the strategical planner for the GTU
     * @return template for a GTU
     * @throws GTUException when characteristics cannot be initialized
     */
    LaneBasedTemplateGTUType makeTemplate(final StreamInterface randStream, final Lane lane,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDistribution,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> widthDistribution,
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeedDistribution,
            final Set<DirectedLanePosition> initialPositions,
            final LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactory) throws GTUException
    {
        return new LaneBasedTemplateGTUType(this.network.getGtuType(GTUType.DEFAULTS.CAR), new Generator<Length>()
        {
            @Override
            public Length draw()
            {
                return lengthDistribution.draw();
            }
        }, new Generator<Length>()
        {
            @Override
            public Length draw()
            {
                return widthDistribution.draw();
            }
        }, new Generator<Speed>()
        {
            @Override
            public Speed draw()
            {
                return maximumSpeedDistribution.draw();
            }
        }, strategicalPlannerFactory,
                lane.getParentLink().getStartNode().getId().equals("From") ? this.routeGeneratorMain : this.routeGeneratorRamp);

    }

    /**
     * Append a sink to each lane of an array of Lanes.
     * @param lanes Lane[]; the array of lanes
     * @param laneType LaneType; the LaneType for cars
     * @return Lane[]; the lanes
     * @throws NetworkException on network inconsistency
     * @throws OTSGeometryException on problem making the path for a link
     */
    private Lane[] setupSink(final Lane[] lanes, final LaneType laneType) throws NetworkException, OTSGeometryException
    {
        CrossSectionLink link = lanes[0].getParentLink();
        OTSRoadNode to = (OTSRoadNode) link.getEndNode();
        OTSRoadNode from = (OTSRoadNode) link.getStartNode();
        double endLinkLength = 50; // [m]
        double endX = to.getPoint().x + (endLinkLength / link.getLength().getSI()) * (to.getPoint().x - from.getPoint().x);
        double endY = to.getPoint().y + (endLinkLength / link.getLength().getSI()) * (to.getPoint().y - from.getPoint().y);
        OTSRoadNode end = new OTSRoadNode(this.network, link.getId() + "END", new OTSPoint3D(endX, endY, to.getPoint().z), 
                Direction.instantiateSI(Math.atan2(to.getPoint().y - from.getPoint().y, to.getPoint().x - from.getPoint().x)));
        CrossSectionLink endLink = LaneFactory.makeLink(this.network, link.getId() + "endLink", to, end, null, this.simulator);
        for (Lane lane : lanes)
        {
            // Overtaking left and right allowed on the sinkLane
            Lane sinkLane = new Lane(endLink, lane.getId() + "." + "sinkLane", lane.getLateralCenterPosition(1.0),
                    lane.getLateralCenterPosition(1.0), lane.getWidth(1.0), lane.getWidth(1.0), laneType, this.speedLimit);
            new SinkSensor(sinkLane, new Length(10.0, METER), Compatible.EVERYTHING, this.simulator);
        }
        return lanes;
    }

    /** The set of GTUs that we want to sample regularly. */
    private Set<GTU> knownGTUs = new LinkedHashSet<>();

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        EventType eventType = event.getType();
        if (Network.GTU_ADD_EVENT.equals(eventType))
        {
            System.out.println("A GTU was created (id " + (String) event.getContent() + ")");
            this.knownGTUs.add(this.network.getGTU((String) event.getContent()));
        }
        else if (Network.GTU_REMOVE_EVENT.equals(eventType))
        {
            System.out.println("A GTU was removed (id " + ((String) event.getContent()) + ")");
            this.knownGTUs.remove(this.network.getGTU((String) event.getContent()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public OTSRoadNetwork getNetwork()
    {
        return this.network;
    }

    /**
     * @param index int; the rank number of the path
     * @return List&lt;Lane&gt;; the set of lanes for the specified index
     */
    public final List<Lane> getPath(final int index)
    {
        return this.paths.get(index);
    }

    /**
     * Return the number of paths that can be used to show graphs.
     * @return int; the number of paths that can be used to show graphs
     */
    public final int pathCount()
    {
        return this.paths.size();
    }

    /**
     * @return minimumDistance
     */
    public final Length getMinimumDistance()
    {
        return this.minimumDistance;
    }

    /**
     * @return maximumDistance
     */
    public final Length getMaximumDistance()
    {
        return this.maximumDistance;
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "NetworksModel";
    }

}

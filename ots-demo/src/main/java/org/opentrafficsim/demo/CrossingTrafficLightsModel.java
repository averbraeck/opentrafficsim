package org.opentrafficsim.demo;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.base.DoubleScalar;
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
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
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
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistErlang;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Simulate four double lane roads with a crossing in the middle.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CrossingTrafficLightsModel extends AbstractOTSModel implements UNITS
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** The network. */
    private final OTSRoadNetwork network;

    /** the random stream for this demo. */
    private StreamInterface stream = new MersenneTwister(555);

    /** Id generator (used by all generators). */
    private IdGenerator idGenerator = new IdGenerator("");

    /** The probability distribution for the variable part of the headway. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected DistContinuous headwayGenerator;

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** the strategical planner factory for cars. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactoryCar;

    /** the strategical planner factory for trucks. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactoryTruck;

    /** The speed limit on all Lanes. */
    private Speed speedLimit = new Speed(80, KM_PER_HOUR);

    /** Fixed green time. */
    private static final Duration TGREEN = new Duration(39.0, DurationUnit.SI);

    /** Fixed yellow time. */
    private static final Duration TYELLOW = new Duration(6.0, DurationUnit.SI);

    /** Fixed red time. */
    private static final Duration TRED = new Duration(45.0, DurationUnit.SI);

    /**
     * @param simulator OTSSimulatorInterface; the simulator for this model
     */
    public CrossingTrafficLightsModel(final OTSSimulatorInterface simulator)
    {
        super(simulator);
        this.network = new OTSRoadNetwork("network", true, simulator);
        createInputParameters();
    }

    /**
     * Create input parameters for the networks demo.
     */
    private void createInputParameters()
    {
        try
        {
            InputParameterHelper.makeInputParameterMapCarTruck(this.inputParameterMap, 1.0);
            InputParameterMap genericMap = (InputParameterMap) this.inputParameterMap.get("generic");
            genericMap.add(new InputParameterDouble("flow", "Flow per input lane", "Traffic flow per input lane", 250d, 0d,
                    400d, true, true, "%.0f veh/h", 1.5));
        }
        catch (InputParameterException e)
        {
            e.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel() throws SimRuntimeException
    {
        try
        {
            OTSRoadNode[][] nodes = new OTSRoadNode[4][4];
            nodes[0][0] = new OTSRoadNode(this.network, "sn1", new OTSPoint3D(10, -500),
                    new Direction(90, DirectionUnit.EAST_DEGREE));
            nodes[0][1] =
                    new OTSRoadNode(this.network, "sn2", new OTSPoint3D(10, -20), new Direction(90, DirectionUnit.EAST_DEGREE));
            nodes[0][2] =
                    new OTSRoadNode(this.network, "sn3", new OTSPoint3D(10, +20), new Direction(90, DirectionUnit.EAST_DEGREE));
            nodes[0][3] = new OTSRoadNode(this.network, "sn4", new OTSPoint3D(10, +600),
                    new Direction(90, DirectionUnit.EAST_DEGREE));

            nodes[1][0] = new OTSRoadNode(this.network, "we1", new OTSPoint3D(-500, -10), Direction.ZERO);
            nodes[1][1] = new OTSRoadNode(this.network, "we2", new OTSPoint3D(-20, -10), Direction.ZERO);
            nodes[1][2] = new OTSRoadNode(this.network, "we3", new OTSPoint3D(+20, -10), Direction.ZERO);
            nodes[1][3] = new OTSRoadNode(this.network, "we4", new OTSPoint3D(+600, -10), Direction.ZERO);

            nodes[2][0] = new OTSRoadNode(this.network, "ns1", new OTSPoint3D(-10, +500),
                    new Direction(270, DirectionUnit.EAST_DEGREE));
            nodes[2][1] = new OTSRoadNode(this.network, "ns2", new OTSPoint3D(-10, +20),
                    new Direction(270, DirectionUnit.EAST_DEGREE));
            nodes[2][2] = new OTSRoadNode(this.network, "ns3", new OTSPoint3D(-10, -20),
                    new Direction(270, DirectionUnit.EAST_DEGREE));
            nodes[2][3] = new OTSRoadNode(this.network, "ns4", new OTSPoint3D(-10, -600),
                    new Direction(270, DirectionUnit.EAST_DEGREE));

            nodes[3][0] = new OTSRoadNode(this.network, "ew1", new OTSPoint3D(+500, 10),
                    new Direction(180, DirectionUnit.EAST_DEGREE));
            nodes[3][1] = new OTSRoadNode(this.network, "ew2", new OTSPoint3D(+20, 10),
                    new Direction(180, DirectionUnit.EAST_DEGREE));
            nodes[3][2] = new OTSRoadNode(this.network, "ew3", new OTSPoint3D(-20, 10),
                    new Direction(180, DirectionUnit.EAST_DEGREE));
            nodes[3][3] = new OTSRoadNode(this.network, "ew4", new OTSPoint3D(-600, 10),
                    new Direction(180, DirectionUnit.EAST_DEGREE));

            LaneType laneType = this.network.getLaneType(LaneType.DEFAULTS.TWO_WAY_LANE);

            Map<Lane, SimpleTrafficLight> trafficLights = new LinkedHashMap<>();

            this.carProbability = (double) getInputParameter("generic.carProbability");
            ParameterFactory params = new InputParameterHelper(getInputParameterMap());
            this.strategicalPlannerFactoryCar = new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()), params);
            this.strategicalPlannerFactoryTruck = new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()), params);
            this.idGenerator = new IdGenerator("");
            double contP = (double) getInputParameter("generic.flow");
            Duration averageHeadway = new Duration(3600.0 / contP, SECOND);
            Duration minimumHeadway = new Duration(3, SECOND);
            this.headwayGenerator =
                    new DistErlang(new MersenneTwister(1234), DoubleScalar.minus(averageHeadway, minimumHeadway).getSI(), 4);

            for (int i = 0; i < 4; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    Lane[] lanes = LaneFactory.makeMultiLane(this.network,
                            "Lane_" + nodes[i][j].getId() + "-" + nodes[i][j + 1].getId(), nodes[i][j], nodes[i][j + 1], null,
                            2, laneType, this.speedLimit, this.simulator);
                    if (j == 0)
                    {
                        for (Lane lane : lanes)
                        {
                            // make a generator for the lane
                            Generator<Route> routeGenerator = new FixedRouteGenerator(new Route("main",
                                    Arrays.asList(new Node[] {nodes[i][0], nodes[i][1], nodes[i][2], nodes[i][3]})));
                            makeGenerator(lane, routeGenerator);

                            // add the traffic light
                            SimpleTrafficLight tl = new SimpleTrafficLight(lane.getId() + "_TL", lane,
                                    new Length(lane.getLength().minus(new Length(10.0, LengthUnit.METER))), this.simulator);
                            trafficLights.put(lane, tl);

                            try
                            {
                                new TrafficLightAnimation(tl, this.simulator);
                            }
                            catch (RemoteException | NamingException exception)
                            {
                                throw new NetworkException(exception);
                            }

                            if (i == 0 || i == 2)
                            {
                                this.simulator.scheduleEventRel(Duration.ZERO, this, this, "changeTL", new Object[] {tl});
                            }
                            else
                            {
                                this.simulator.scheduleEventRel(TRED, this, this, "changeTL", new Object[] {tl});
                            }
                        }
                    }
                    if (j == 2)
                    {
                        for (Lane lane : lanes)
                        {
                            new SinkSensor(lane, new Length(500.0, METER), Compatible.EVERYTHING, this.simulator);
                        }
                    }
                }
            }
        }
        catch (SimRuntimeException | NamingException | NetworkException | OTSGeometryException | GTUException
                | InputParameterException | ProbabilityException | ParameterException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Build a generator.
     * @param lane Lane; the lane on which the generated GTUs are placed
     * @param routeGenerator the fixed route for this lane
     * @return LaneBasedGTUGenerator
     * @throws GTUException when lane position out of bounds
     * @throws SimRuntimeException when generation scheduling fails
     * @throws ProbabilityException when probability distribution is wrong
     * @throws ParameterException when a parameter is missing for the perception of the GTU
     * @throws InputParameterException when a parameter is missing for the perception of the GTU
     */
    private LaneBasedGTUGenerator makeGenerator(final Lane lane, final Generator<Route> routeGenerator)
            throws GTUException, SimRuntimeException, ProbabilityException, ParameterException, InputParameterException
    {
        Distribution<LaneBasedTemplateGTUType> distribution = new Distribution<>(this.stream);
        Length initialPosition = new Length(16, METER);
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));

        LaneBasedTemplateGTUType template = makeTemplate(this.stream, lane,
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistUniform(this.stream, 3, 6), METER),
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistUniform(this.stream, 1.6, 2.0), METER),
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistUniform(this.stream, 140, 180), KM_PER_HOUR),
                initialPositions, this.strategicalPlannerFactoryCar, routeGenerator);
        // System.out.println("Constructed template " + template);
        distribution.add(new FrequencyAndObject<>(this.carProbability, template));
        template = makeTemplate(this.stream, lane,
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistUniform(this.stream, 8, 14), METER),
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistUniform(this.stream, 2.0, 2.5), METER),
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistUniform(this.stream, 100, 140), KM_PER_HOUR),
                initialPositions, this.strategicalPlannerFactoryTruck, routeGenerator);
        // System.out.println("Constructed template " + template);
        distribution.add(new FrequencyAndObject<>(1.0 - this.carProbability, template));
        LaneBasedTemplateGTUTypeDistribution templateDistribution = new LaneBasedTemplateGTUTypeDistribution(distribution);
        LaneBasedGTUGenerator.RoomChecker roomChecker = new CFRoomChecker();
        return new LaneBasedGTUGenerator(lane.getId(), new Generator<Duration>()
        {
            @Override
            public Duration draw()
            {
                return new Duration(CrossingTrafficLightsModel.this.headwayGenerator.draw(), DurationUnit.SI);
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
     * @param routeGenerator the route generator
     * @return template for a GTU
     * @throws GTUException when characteristics cannot be initialized
     */
    @SuppressWarnings("checkstyle:parameternumber")
    LaneBasedTemplateGTUType makeTemplate(final StreamInterface randStream, final Lane lane,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDistribution,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> widthDistribution,
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeedDistribution,
            final Set<DirectedLanePosition> initialPositions,
            final LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactory,
            final Generator<Route> routeGenerator) throws GTUException
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
        }, strategicalPlannerFactory, routeGenerator);

    }

    /**
     * Change the traffic light to a new color.
     * @param tl TrafficLight; the traffic light
     * @throws SimRuntimeException when scheduling fails
     */
    protected final void changeTL(final TrafficLight tl) throws SimRuntimeException
    {
        if (tl.getTrafficLightColor().isRed())
        {
            tl.setTrafficLightColor(TrafficLightColor.GREEN);
            this.simulator.scheduleEventRel(TGREEN, this, this, "changeTL", new Object[] {tl});
        }
        else if (tl.getTrafficLightColor().isGreen())
        {
            tl.setTrafficLightColor(TrafficLightColor.YELLOW);
            this.simulator.scheduleEventRel(TYELLOW, this, this, "changeTL", new Object[] {tl});
        }
        else if (tl.getTrafficLightColor().isYellow())
        {
            tl.setTrafficLightColor(TrafficLightColor.RED);
            this.simulator.scheduleEventRel(TRED, this, this, "changeTL", new Object[] {tl});
        }
    }

    /** {@inheritDoc} */
    @Override
    public OTSRoadNetwork getNetwork()
    {
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "CrossingTrafficLightsModel";
    }
}

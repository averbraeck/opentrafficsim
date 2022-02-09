package org.opentrafficsim.demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.distributions.ConstantGenerator;
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
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.TTCRoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUTypeDistribution;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIDM;
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
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
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
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2019-01-06 01:35:05 +0100 (Sun, 06 Jan 2019) $, @version $Revision: 4831 $, by $Author: averbraeck $,
 * initial version ug 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class StraightModel extends AbstractOTSModel implements UNITS
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** The network. */
    private final OTSRoadNetwork network = new OTSRoadNetwork("network", true, getSimulator());

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** The blocking, implemented as a traffic light. */
    private SimpleTrafficLight block = null;

    /** Maximum distance. */
    private Length maximumDistance = new Length(5000, METER);

    /** The Lane that contains the simulated Cars. */
    private Lane lane;

    /** The random number generator used to decide what kind of GTU to generate. */
    private StreamInterface stream = new MersenneTwister(12345);

    /** The sequence of Lanes that all vehicles will follow. */
    private List<Lane> path = new ArrayList<>();

    /** The speed limit on all Lanes. */
    private Speed speedLimit = new Speed(120, KM_PER_HOUR);

    /**
     * @param simulator OTSSimulatorInterface; the simulator for this model
     */
    public StraightModel(final OTSSimulatorInterface simulator)
    {
        super(simulator);
        InputParameterHelper.makeInputParameterMapCarTruck(this.inputParameterMap, 1.0);
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel() throws SimRuntimeException
    {
        try
        {
            OTSRoadNode from =
                    new OTSRoadNode(this.network, "From", new OTSPoint3D(0.0, 0, 0), Direction.ZERO);
            OTSRoadNode to =
                    new OTSRoadNode(this.network, "To", new OTSPoint3D(this.maximumDistance.getSI(), 0, 0), Direction.ZERO);
            OTSRoadNode end = new OTSRoadNode(this.network, "End", new OTSPoint3D(this.maximumDistance.getSI() + 50.0, 0, 0),
                    Direction.ZERO);
            LaneType laneType = this.network.getLaneType(LaneType.DEFAULTS.TWO_WAY_LANE);
            this.lane = LaneFactory.makeLane(this.network, "Lane", from, to, null, laneType, this.speedLimit, this.simulator);
            this.path.add(this.lane);
            CrossSectionLink endLink = LaneFactory.makeLink(this.network, "endLink", to, end, null, this.simulator);
            // No overtaking, single lane
            Lane sinkLane = new Lane(endLink, "sinkLane", this.lane.getLateralCenterPosition(1.0),
                    this.lane.getLateralCenterPosition(1.0), this.lane.getWidth(1.0), this.lane.getWidth(1.0), laneType,
                    this.speedLimit);
            new SinkSensor(sinkLane, new Length(10.0, METER), Compatible.EVERYTHING, this.simulator);
            this.path.add(sinkLane);

            this.carProbability = (double) getInputParameter("generic.carProbability");

            // Generation of a new car / truck
            TTCRoomChecker roomChecker = new TTCRoomChecker(new Duration(10.0, DurationUnit.SI));
            IdGenerator idGenerator = new IdGenerator("");
            ParameterSet params = new ParameterSet();
            params.setDefaultParameter(AbstractIDM.DELTA);
            GTUType car = new GTUType("car", this.network.getGtuType(GTUType.DEFAULTS.CAR));
            GTUType truck = new GTUType("truck", this.network.getGtuType(GTUType.DEFAULTS.TRUCK));
            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedCar =
                    new ContinuousDistDoubleScalar.Rel<>(new DistUniform(this.stream, 90.0, 110.0), SpeedUnit.KM_PER_HOUR);
            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedTruck =
                    new ContinuousDistDoubleScalar.Rel<>(new DistUniform(this.stream, 80, 95), SpeedUnit.KM_PER_HOUR);
            Generator<Route> routeGenerator = new FixedRouteGenerator(null);
            LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactoryCars =
                    new LaneBasedStrategicalRoutePlannerFactory(
                            new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));
            LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFctoryTrucks =
                    new LaneBasedStrategicalRoutePlannerFactory(
                            new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));
            LaneBasedTemplateGTUType carTemplate = new LaneBasedTemplateGTUType(car,
                    new ConstantGenerator<>(Length.instantiateSI(4.0)), new ConstantGenerator<>(Length.instantiateSI(2.0)),
                    speedCar, strategicalPlannerFactoryCars, routeGenerator);
            LaneBasedTemplateGTUType truckTemplate = new LaneBasedTemplateGTUType(truck,
                    new ConstantGenerator<>(Length.instantiateSI(15.0)), new ConstantGenerator<>(Length.instantiateSI(2.5)),
                    speedTruck, strategicalPlannerFctoryTrucks, routeGenerator);
            Distribution<LaneBasedTemplateGTUType> gtuTypeDistribution = new Distribution<>(this.stream);
            gtuTypeDistribution.add(new FrequencyAndObject<>(this.carProbability, carTemplate));
            gtuTypeDistribution.add(new FrequencyAndObject<>(1.0 - this.carProbability, truckTemplate));
            Generator<Duration> headwayGenerator = new HeadwayGenerator(new Frequency(1500.0, PER_HOUR));
            Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>();
            initialLongitudinalPositions
                    .add(new DirectedLanePosition(this.lane, new Length(5.0, LengthUnit.SI), GTUDirectionality.DIR_PLUS));
            LaneBasedTemplateGTUTypeDistribution characteristicsGenerator =
                    new LaneBasedTemplateGTUTypeDistribution(gtuTypeDistribution);
            new LaneBasedGTUGenerator("Generator", headwayGenerator, characteristicsGenerator,
                    GeneratorPositions.create(initialLongitudinalPositions, this.stream), this.network, getSimulator(),
                    roomChecker, idGenerator);
            // End generation

            this.block = new SimpleTrafficLight(this.lane.getId() + "_TL", this.lane,
                    new Length(new Length(4000.0, LengthUnit.METER)), this.simulator);
            this.block.setTrafficLightColor(TrafficLightColor.GREEN);
            // Create a block at t = 5 minutes
            this.simulator.scheduleEventAbs(new Time(300, TimeUnit.BASE_SECOND), this, this, "createBlock", null);
            // Remove the block at t = 7 minutes
            this.simulator.scheduleEventAbs(new Time(420, TimeUnit.BASE_SECOND), this, this, "removeBlock", null);
        }
        catch (SimRuntimeException | NetworkException | OTSGeometryException | InputParameterException | GTUException
                | ParameterException | ProbabilityException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Set up the block.
     */
    protected final void createBlock()
    {
        this.block.setTrafficLightColor(TrafficLightColor.RED);
    }

    /**
     * Remove the block.
     */
    protected final void removeBlock()
    {
        this.block.setTrafficLightColor(TrafficLightColor.GREEN);
    }

    /** {@inheritDoc} */
    @Override
    public OTSRoadNetwork getNetwork()
    {
        return this.network;
    }

    /**
     * @return the path for sampling the graphs
     */
    public final List<Lane> getPath()
    {
        return this.path;
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "StraightModel";
    }

    /**
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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

        /** a random stream. */
        private StreamInterface stream = new MersenneTwister(4L);

        /**
         * @param demand Frequency; demand
         */
        HeadwayGenerator(final Frequency demand)
        {
            this.demand = demand;
        }

        /** {@inheritDoc} */
        @Override
        public Duration draw() throws ProbabilityException, ParameterException
        {
            return new Duration(-Math.log(this.stream.nextDouble()) / this.demand.si, DurationUnit.SI);
        }

    }

}

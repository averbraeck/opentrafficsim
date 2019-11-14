package org.opentrafficsim.demo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
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
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private final OTSRoadNetwork network = new OTSRoadNetwork("network", true);

    /** The headway (inter-vehicle time). */
    private Duration headway;

    /** Number of cars created. */
    private int carsCreated = 0;

    /** Strategical planner generator for cars. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorCars = null;

    /** Strategical planner generator for trucks. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorTrucks = null;

    /** Car parameters. */
    private Parameters parametersCar;

    /** Truck parameters. */
    private Parameters parametersTruck;

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** The blocking, implemented as a traffic light. */
    private SimpleTrafficLight block = null;

    /** Minimum distance. */
    private Length minimumDistance = new Length(0, METER);

    /** Maximum distance. */
    private Length maximumDistance = new Length(5000, METER);

    /** The Lane that contains the simulated Cars. */
    private Lane lane;

    /** The random number generator used to decide what kind of GTU to generate. */
    private StreamInterface stream = new MersenneTwister(12345);

    /** The sequence of Lanes that all vehicles will follow. */
    private List<Lane> path = new ArrayList<>();

    /** The speed limit on all Lanes. */
    private Speed speedLimit = new Speed(100, KM_PER_HOUR);

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
                    new OTSRoadNode(this.network, "From", new OTSPoint3D(getMinimumDistance().getSI(), 0, 0), Direction.ZERO);
            OTSRoadNode to =
                    new OTSRoadNode(this.network, "To", new OTSPoint3D(getMaximumDistance().getSI(), 0, 0), Direction.ZERO);
            OTSRoadNode end = new OTSRoadNode(this.network, "End", new OTSPoint3D(getMaximumDistance().getSI() + 50.0, 0, 0),
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
            this.parametersCar = InputParameterHelper.getParametersCar(getInputParameterMap());
            this.parametersTruck = InputParameterHelper.getParametersTruck(getInputParameterMap());

            this.strategicalPlannerGeneratorCars = new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));
            this.strategicalPlannerGeneratorTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));

            // 1500 [veh / hour] == 2.4s headway
            this.headway = new Duration(3600.0 / 1500.0, SECOND);

            // Schedule creation of the first car (it will re-schedule itself one headway later, etc.).
            this.simulator.scheduleEventAbs(Time.ZERO, this, this, "generateCar", null);

            this.block = new SimpleTrafficLight(this.lane.getId() + "_TL", this.lane,
                    new Length(new Length(4000.0, LengthUnit.METER)), this.simulator);
            this.block.setTrafficLightColor(TrafficLightColor.GREEN);
            // Create a block at t = 5 minutes
            this.simulator.scheduleEventAbs(new Time(300, TimeUnit.BASE_SECOND), this, this, "createBlock", null);
            // Remove the block at t = 7 minutes
            this.simulator.scheduleEventAbs(new Time(420, TimeUnit.BASE_SECOND), this, this, "removeBlock", null);
        }
        catch (SimRuntimeException | NetworkException | OTSGeometryException | InputParameterException | GTUException
                | ParameterException exception)
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

    /**
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     */
    protected final void generateCar()
    {
        try
        {
            boolean generateTruck = this.stream.nextDouble() > this.carProbability;
            Length vehicleLength = new Length(generateTruck ? 15 : 4, METER);
            LaneBasedIndividualGTU gtu = new LaneBasedIndividualGTU("" + (++this.carsCreated),
                    this.network.getGtuType(GTUType.DEFAULTS.CAR), vehicleLength, new Length(1.8, METER),
                    new Speed(200, KM_PER_HOUR), vehicleLength.times(0.5), this.simulator, this.network);
            gtu.setParameters(generateTruck ? this.parametersTruck : this.parametersCar);
            gtu.setNoLaneChangeDistance(Length.ZERO);
            gtu.setMaximumAcceleration(Acceleration.instantiateSI(3.0));
            gtu.setMaximumDeceleration(Acceleration.instantiateSI(-8.0));

            // strategical planner
            LaneBasedStrategicalPlanner strategicalPlanner =
                    generateTruck ? this.strategicalPlannerGeneratorTrucks.create(gtu, null, null, null)
                            : this.strategicalPlannerGeneratorCars.create(gtu, null, null, null);

            Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
            Length initialPosition = new Length(20, METER);
            initialPositions.add(new DirectedLanePosition(this.lane, initialPosition, GTUDirectionality.DIR_PLUS));
            Speed initialSpeed = new Speed(100.0, KM_PER_HOUR);
            gtu.init(strategicalPlanner, initialPositions, initialSpeed);
            this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", null);
        }
        catch (SimRuntimeException | NetworkException | GTUException | OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @return List&lt;Lane&gt;; the set of lanes for the specified index
     */
    public List<Lane> getPath()
    {
        return new ArrayList<>(this.path);
    }

    /** {@inheritDoc} */
    @Override
    public OTSRoadNetwork getNetwork()
    {
        return this.network;
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

    /**
     * @return lane.
     */
    public Lane getLane()
    {
        return this.lane;
    }

}

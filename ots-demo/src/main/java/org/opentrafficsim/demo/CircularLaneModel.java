package org.opentrafficsim.demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDoubleScalar;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Simulate traffic on a circular, one-lane road.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class CircularLaneModel extends AbstractOtsModel implements UNITS
{
    /** */
    private static final long serialVersionUID = 20141121L;

    /** Number of cars created. */
    private int carsCreated = 0;

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** Minimum distance. */
    private Length minimumDistance = new Length(0, METER);

    /** The speed limit. */
    private Speed speedLimit = new Speed(100, KM_PER_HOUR);

    /** The sequence of Lanes that all vehicles will follow. */
    private List<Lane> path = new ArrayList<>();

    /** The left Lane that contains simulated Cars. */
    private Lane lane1;

    /** The right Lane that contains simulated Cars. */
    private Lane lane2;

    /** The random number generator used to decide what kind of GTU to generate etc. */
    private StreamInterface stream = new MersenneTwister(12345);

    /** Strategical planner generator for cars. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorCars = null;

    /** Strategical planner generator for trucks. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorTrucks = null;

    /** Car parameters. */
    private Parameters parametersCar;

    /** Truck parameters. */
    private Parameters parametersTruck;

    /** The OTSRoadNetwork. */
    private final OTSRoadNetwork network;

    /**
     * @param simulator OTSSimulatorInterface; the simulator for this model
     */
    public CircularLaneModel(final OtsSimulatorInterface simulator)
    {
        super(simulator);
        this.network = new OTSRoadNetwork("network", true, simulator);
        makeInputParameterMap();
    }

    /**
     * Make a map of input parameters for this demo.
     */
    public void makeInputParameterMap()
    {
        try
        {
            InputParameterHelper.makeInputParameterMapCarTruck(this.inputParameterMap, 4.0);
            InputParameterMap genericMap = (InputParameterMap) this.inputParameterMap.get("generic");

            genericMap.add(new InputParameterDoubleScalar<LengthUnit, Length>("trackLength", "Track length",
                    "Track length (circumfence of the track)", Length.instantiateSI(1000.0), Length.instantiateSI(500.0),
                    Length.instantiateSI(2000.0), true, true, "%.0f", 1.0));
            genericMap.add(new InputParameterDouble("densityMean", "Mean density (veh / km)",
                    "mean density of the vehicles (vehicles per kilometer)", 30.0, 5.0, 45.0, true, true, "%.0f", 2.0));
            genericMap.add(new InputParameterDouble("densityVariability", "Density variability",
                    "Variability of the denisty: variability * (headway - 20) meters", 0.0, 0.0, 1.0, true, true, "%.00f",
                    3.0));
        }
        catch (InputParameterException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel() throws SimRuntimeException
    {
        try
        {
            this.carProbability = (double) getInputParameter("generic.carProbability");
            double radius = ((Length) getInputParameter("generic.trackLength")).si / 2 / Math.PI;
            double headway = 1000.0 / (double) getInputParameter("generic.densityMean");
            double headwayVariability = (double) getInputParameter("generic.densityVariability");

            this.parametersCar = InputParameterHelper.getParametersCar(getInputParameterMap());
            this.parametersTruck = InputParameterHelper.getParametersTruck(getInputParameterMap());

            this.strategicalPlannerGeneratorCars = new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));
            this.strategicalPlannerGeneratorTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));

            LaneType laneType = this.network.getLaneType(LaneType.DEFAULTS.TWO_WAY_LANE);
            OTSRoadNode start = new OTSRoadNode(this.network, "Start", new OTSPoint3D(radius, 0, 0),
                    new Direction(90, DirectionUnit.EAST_DEGREE));
            OTSRoadNode halfway = new OTSRoadNode(this.network, "Halfway", new OTSPoint3D(-radius, 0, 0),
                    new Direction(270, DirectionUnit.EAST_DEGREE));

            OTSPoint3D[] coordsHalf1 = new OTSPoint3D[127];
            for (int i = 0; i < coordsHalf1.length; i++)
            {
                double angle = Math.PI * (1 + i) / (1 + coordsHalf1.length);
                coordsHalf1[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle), 0);
            }
            this.lane1 = LaneFactory.makeMultiLane(this.network, "Lane1", start, halfway, coordsHalf1, 1, laneType,
                    this.speedLimit, this.simulator)[0];
            this.path.add(this.lane1);

            OTSPoint3D[] coordsHalf2 = new OTSPoint3D[127];
            for (int i = 0; i < coordsHalf2.length; i++)
            {
                double angle = Math.PI + Math.PI * (1 + i) / (1 + coordsHalf2.length);
                coordsHalf2[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle), 0);
            }
            this.lane2 = LaneFactory.makeMultiLane(this.network, "Lane2", halfway, start, coordsHalf2, 1, laneType,
                    this.speedLimit, this.simulator)[0];
            this.path.add(this.lane2);

            // Put the (not very evenly spaced) cars on track1
            double trackLength = this.lane1.getLength().getSI();
            double variability = (headway - 20) * headwayVariability;
            System.out.println("headway is " + headway + " variability limit is " + variability);
            Random random = new Random(12345);
            for (double pos = 0; pos <= trackLength - headway - variability;)
            {
                // Actual headway is uniformly distributed around headway
                double actualHeadway = headway + (random.nextDouble() * 2 - 1) * variability;
                generateCar(this.lane1, new Length(pos, METER));
                pos += actualHeadway;
            }
            // Put the (not very evenly spaced) cars on track2
            trackLength = this.lane2.getLength().getSI();
            variability = (headway - 20) * headwayVariability;
            System.out.println("headway is " + headway + " variability limit is " + variability);
            random = new Random(54321);
            for (double pos = 0; pos <= trackLength - headway - variability;)
            {
                // Actual headway is uniformly distributed around headway
                double actualHeadway = headway + (random.nextDouble() * 2 - 1) * variability;
                generateCar(this.lane2, new Length(pos, METER));
                pos += actualHeadway;
            }

        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Generate one gtu.
     * @param initialPosition Length; the initial position of the new cars
     * @param lane Lane; the lane on which the new cars are placed
     * @throws GtuException when something goes wrong during construction of the car
     */
    protected final void generateCar(final Lane lane, final Length initialPosition) throws GtuException
    {
        // GTU itself
        boolean generateTruck = this.stream.nextDouble() > this.carProbability;
        Length vehicleLength = new Length(generateTruck ? 15 : 4, METER);
        LaneBasedIndividualGtu gtu = new LaneBasedIndividualGtu("" + (++this.carsCreated),
                this.network.getGtuType(GtuType.DEFAULTS.CAR), vehicleLength, new Length(1.8, METER),
                new Speed(200, KM_PER_HOUR), vehicleLength.times(0.5), this.simulator, this.network);
        gtu.setParameters(generateTruck ? this.parametersTruck : this.parametersCar);
        gtu.setNoLaneChangeDistance(Length.ZERO);
        gtu.setMaximumAcceleration(Acceleration.instantiateSI(3.0));
        gtu.setMaximumDeceleration(Acceleration.instantiateSI(-8.0));

        // strategical planner
        LaneBasedStrategicalPlanner strategicalPlanner;
        Route route = null;
        if (!generateTruck)
        {
            strategicalPlanner = this.strategicalPlannerGeneratorCars.create(gtu, route, null, null);
        }
        else
        {
            strategicalPlanner = this.strategicalPlannerGeneratorTrucks.create(gtu, route, null, null);
        }

        // init
        Set<LanePosition> initialPositions = new LinkedHashSet<>(1);
        initialPositions.add(new LanePosition(lane, initialPosition));
        Speed initialSpeed = new Speed(0, KM_PER_HOUR);
        try
        {
            gtu.init(strategicalPlanner, initialPositions, initialSpeed);
        }
        catch (NetworkException | SimRuntimeException | OTSGeometryException exception)
        {
            throw new GtuException(exception);
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
     * Stop simulation and throw an Error.
     * @param theSimulator OTSSimulatorInterface; the simulator
     * @param errorMessage String; the error message
     */
    public void stopSimulator(final OtsSimulatorInterface theSimulator, final String errorMessage)
    {
        System.out.println("Error: " + errorMessage);
        try
        {
            if (theSimulator.isStartingOrRunning())
            {
                theSimulator.stop();
            }
        }
        catch (SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
        throw new Error(errorMessage);
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "CircularLaneModel";
    }

}

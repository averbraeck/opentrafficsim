package org.opentrafficsim.web.test;

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
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLmrsPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.LaneType;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDoubleScalar;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Simulate traffic on a circular, two-lane road.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class CircularRoadModel extends AbstractOtsModel implements UNITS
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
    private List<List<Lane>> paths = new ArrayList<>();

    /** The random number generator used to decide what kind of GTU to generate etc. */
    private StreamInterface stream = new MersenneTwister(12345);

    /** Strategical planner generator for cars. */
    private LaneBasedStrategicalPlannerFactory<?> strategicalPlannerGeneratorCars = null;

    /** Strategical planner generator for trucks. */
    private LaneBasedStrategicalPlannerFactory<?> strategicalPlannerGeneratorTrucks = null;

    /** Car parameters. */
    private Parameters parametersCar;

    /** Truck parameters. */
    private Parameters parametersTruck;

    /** The RoadNetwork. */
    private final RoadNetwork network;

    /**
     * @param simulator the simulator for this model
     */
    public CircularRoadModel(final OtsSimulatorInterface simulator)
    {
        super(simulator);
        this.network = new RoadNetwork("network", simulator);
        makeInputParameterMap();
    }

    /**
     * Make a map of input parameters for this demo.
     */
    public void makeInputParameterMap()
    {
        try
        {
            InputParameterHelper.makeInputParameterMapCarTruck(this.inputParameterMap, 1.0);

            InputParameterMap genericMap = null;
            if (this.inputParameterMap.getValue().containsKey("generic"))
            {
                genericMap = (InputParameterMap) this.inputParameterMap.get("generic");
            }
            else
            {
                genericMap = new InputParameterMap("generic", "Generic", "Generic parameters", 1.0);
                this.inputParameterMap.add(genericMap);
            }

            genericMap.add(new InputParameterDoubleScalar<LengthUnit, Length>("trackLength", "Track length",
                    "Track length (circumfence of the track)", Length.instantiateSI(1000.0), Length.instantiateSI(500.0),
                    Length.instantiateSI(2000.0), true, true, "%.0f", 1.5));
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

    /**
     * @param index the rank number of the path
     * @return the set of lanes for the specified index
     */
    public List<Lane> getPath(final int index)
    {
        return this.paths.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel() throws SimRuntimeException
    {
        System.out.println("MODEL CONSTRUCTED");
        try
        {
            final int laneCount = 2;
            for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
            {
                this.paths.add(new ArrayList<Lane>());
            }

            this.carProbability = (double) getInputParameter("generic.carProbability");
            double radius = ((Length) getInputParameter("generic.trackLength")).si / 2 / Math.PI;
            double headway = 1000.0 / (double) getInputParameter("generic.densityMean");
            double headwayVariability = (double) getInputParameter("generic.densityVariability");

            this.parametersCar = InputParameterHelper.getParametersCar(getInputParameterMap());
            this.parametersTruck = InputParameterHelper.getParametersTruck(getInputParameterMap());

            this.strategicalPlannerGeneratorCars = new LaneBasedStrategicalRoutePlannerFactory(
                    new LmrsFactory(new IdmPlusFactory(this.stream), new DefaultLmrsPerceptionFactory()));
            this.strategicalPlannerGeneratorTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                    new LmrsFactory(new IdmPlusFactory(this.stream), new DefaultLmrsPerceptionFactory()));

            GtuType gtuType = DefaultsNl.CAR;
            LaneType laneType = DefaultsRoadNl.TWO_WAY_LANE;
            Node start = new Node(this.network, "Start", new Point2d(radius, 0), new Direction(90, DirectionUnit.EAST_DEGREE));
            Node halfway =
                    new Node(this.network, "Halfway", new Point2d(-radius, 0), new Direction(-90, DirectionUnit.EAST_DEGREE));

            Point2d[] coordsHalf1 = new Point2d[127];
            for (int i = 0; i < coordsHalf1.length; i++)
            {
                double angle = Math.PI * (1 + i) / (1 + coordsHalf1.length);
                coordsHalf1[i] = new Point2d(radius * Math.cos(angle), radius * Math.sin(angle));
            }
            Lane[] lanes1 = LaneFactory.makeMultiLane(this.network, "FirstHalf", start, halfway, coordsHalf1, laneCount,
                    laneType, this.speedLimit, this.simulator, DefaultsNl.VEHICLE);
            Point2d[] coordsHalf2 = new Point2d[127];
            for (int i = 0; i < coordsHalf2.length; i++)
            {
                double angle = Math.PI + Math.PI * (1 + i) / (1 + coordsHalf2.length);
                coordsHalf2[i] = new Point2d(radius * Math.cos(angle), radius * Math.sin(angle));
            }
            Lane[] lanes2 = LaneFactory.makeMultiLane(this.network, "SecondHalf", halfway, start, coordsHalf2, laneCount,
                    laneType, this.speedLimit, this.simulator, DefaultsNl.VEHICLE);
            for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
            {
                this.paths.get(laneIndex).add(lanes1[laneIndex]);
                this.paths.get(laneIndex).add(lanes2[laneIndex]);
            }
            // Put the (not very evenly spaced) cars on the track
            double variability = (headway - 20) * headwayVariability;
            System.out.println("headway is " + headway + " variability limit is " + variability);
            Random random = new Random(12345);
            for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
            {
                double lane1Length = lanes1[laneIndex].getLength().getSI();
                double trackLength = lane1Length + lanes2[laneIndex].getLength().getSI();
                for (double pos = 0; pos <= trackLength - headway - variability;)
                {
                    Lane lane = pos >= lane1Length ? lanes2[laneIndex] : lanes1[laneIndex];
                    // Actual headway is uniformly distributed around headway
                    double laneRelativePos = pos > lane1Length ? pos - lane1Length : pos;
                    double actualHeadway = headway + (random.nextDouble() * 2 - 1) * variability;
                    // System.out.println(lane + ", len=" + lane.getLength() + ", pos=" + laneRelativePos);
                    generateGTU(new Length(laneRelativePos, METER), lane, gtuType);
                    pos += actualHeadway;
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Generate one gtu.
     * @param initialPosition the initial position of the new cars
     * @param lane the lane on which the new cars are placed
     * @param gtuType the type of the new cars
     * @throws SimRuntimeException cannot happen
     * @throws NetworkException on network inconsistency
     * @throws GtuException when something goes wrong during construction of the car
     * @throws OtsGeometryException when the initial position is outside the center line of the lane
     */
    protected final void generateGTU(final Length initialPosition, final Lane lane, final GtuType gtuType)
            throws GtuException, NetworkException, SimRuntimeException, OtsGeometryException
    {
        // GTU itself
        boolean generateTruck = this.stream.nextDouble() > this.carProbability;
        Length vehicleLength = new Length(generateTruck ? 15 : 4, METER);
        LaneBasedGtu gtu = new LaneBasedGtu("" + (++this.carsCreated), gtuType, vehicleLength, new Length(1.8, METER),
                new Speed(200, KM_PER_HOUR), vehicleLength.times(0.5), this.network);
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
        Speed initialSpeed = new Speed(0, KM_PER_HOUR);
        gtu.init(strategicalPlanner, new LanePosition(lane, initialPosition), initialSpeed);
    }

    /** {@inheritDoc} */
    @Override
    public RoadNetwork getNetwork()
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
     * @param theSimulator the simulator
     * @param errorMessage the error message
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

}

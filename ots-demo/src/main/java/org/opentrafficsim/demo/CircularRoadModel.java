package org.opentrafficsim.demo;

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
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
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

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterBoolean;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDoubleScalar;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Simulate traffic on a circular, two-lane road.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-11-18 20:49:04 +0100 (Sun, 18 Nov 2018) $, @version $Revision: 4743 $, by $Author: averbraeck $,
 * initial version 1 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CircularRoadModel extends AbstractOTSModel implements UNITS
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
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorCars = null;

    /** Strategical planner generator for trucks. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorTrucks = null;

    /** Car parameters. */
    private Parameters parametersCar;

    /** Truck parameters. */
    private Parameters parametersTruck;

    /** The OTSRoadNetwork. */
    private final OTSRoadNetwork network = new OTSRoadNetwork("network", true);

    /**
     * @param simulator OTSSimulatorInterface; the simulator for this model
     */
    public CircularRoadModel(final OTSSimulatorInterface simulator)
    {
        super(simulator);
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
            genericMap.add(new InputParameterBoolean("gradualLaneChange", "Gradual lane change",
                    "Gradual lane change when true; instantaneous lane change when false", true, 4.0));
        }
        catch (InputParameterException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @param index int; the rank number of the path
     * @return List&lt;Lane&gt;; the set of lanes for the specified index
     */
    public List<Lane> getPath(final int index)
    {
        return this.paths.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel() throws SimRuntimeException
    {
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
                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));
            this.strategicalPlannerGeneratorTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));

            GTUType gtuType = this.network.getGtuType(GTUType.DEFAULTS.CAR);
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
            Lane[] lanes1 = LaneFactory.makeMultiLane(this.network, "FirstHalf", start, halfway, coordsHalf1, laneCount,
                    laneType, this.speedLimit, this.simulator);
            OTSPoint3D[] coordsHalf2 = new OTSPoint3D[127];
            for (int i = 0; i < coordsHalf2.length; i++)
            {
                double angle = Math.PI + Math.PI * (1 + i) / (1 + coordsHalf2.length);
                coordsHalf2[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle), 0);
            }
            Lane[] lanes2 = LaneFactory.makeMultiLane(this.network, "SecondHalf", halfway, start, coordsHalf2, laneCount,
                    laneType, this.speedLimit, this.simulator);
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
     * @param initialPosition Length; the initial position of the new cars
     * @param lane Lane; the lane on which the new cars are placed
     * @param gtuType GTUType; the type of the new cars
     * @throws SimRuntimeException cannot happen
     * @throws NetworkException on network inconsistency
     * @throws GTUException when something goes wrong during construction of the car
     * @throws OTSGeometryException when the initial position is outside the center line of the lane
     * @throws InputParameterException when generic.gradualLaneChange is not set
     */
    protected final void generateGTU(final Length initialPosition, final Lane lane, final GTUType gtuType)
            throws GTUException, NetworkException, SimRuntimeException, OTSGeometryException, InputParameterException
    {
        // GTU itself
        boolean generateTruck = this.stream.nextDouble() > this.carProbability;
        Length vehicleLength = new Length(generateTruck ? 15 : 4, METER);
        LaneBasedIndividualGTU gtu =
                new LaneBasedIndividualGTU("" + (++this.carsCreated), gtuType, vehicleLength, new Length(1.8, METER),
                        new Speed(200, KM_PER_HOUR), vehicleLength.times(0.5), this.simulator, this.network);
        gtu.setParameters(generateTruck ? this.parametersTruck : this.parametersCar);
        gtu.setNoLaneChangeDistance(Length.ZERO);
        gtu.setInstantaneousLaneChange(!((boolean) getInputParameter("generic.gradualLaneChange")));
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
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
        Speed initialSpeed = new Speed(0, KM_PER_HOUR);
        gtu.init(strategicalPlanner, initialPositions, initialSpeed);
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
     * @param theSimulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     * @param errorMessage String; the error message
     */
    public void stopSimulator(final DEVSSimulatorInterface.TimeDoubleUnit theSimulator, final String errorMessage)
    {
        System.out.println("Error: " + errorMessage);
        try
        {
            if (theSimulator.isRunning())
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

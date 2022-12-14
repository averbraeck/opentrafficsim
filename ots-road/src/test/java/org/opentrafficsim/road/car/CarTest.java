package org.opentrafficsim.road.car;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGtu;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCfLcTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.GtuFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OtsRoadNode;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class CarTest implements UNITS
{
    /**
     * Test some basics of the Car class.
     * @throws NetworkException on ???
     * @throws SimRuntimeException on ???
     * @throws NamingException on ???
     * @throws GtuException on ???
     * @throws OtsGeometryException when center line or contour of a link or lane cannot be generated
     */
    @SuppressWarnings("static-method")
    @Test
    public final void carTest()
            throws NetworkException, SimRuntimeException, NamingException, GtuException, OtsGeometryException
    {
        Time initialTime = new Time(0, TimeUnit.BASE_SECOND);
        OtsSimulatorInterface simulator = makeSimulator();
        OtsRoadNetwork network = new OtsRoadNetwork("network", true, simulator);
        GtuType gtuType = network.getGtuType(GtuType.DEFAULTS.CAR);
        LaneType laneType = network.getLaneType(LaneType.DEFAULTS.TWO_WAY_LANE);
        Lane lane = makeLane(network, laneType, simulator);
        Length initialPosition = new Length(12, METER);
        Speed initialSpeed = new Speed(34, KM_PER_HOUR);
        GtuFollowingModelOld gtuFollowingModel =
                new FixedAccelerationModel(new Acceleration(0, METER_PER_SECOND_2), new Duration(10, SECOND));
        LaneChangeModel laneChangeModel = new Egoistic();
        LaneBasedIndividualGtu referenceCar = makeReferenceCar("12345", gtuType, lane, initialPosition, initialSpeed, simulator,
                gtuFollowingModel, laneChangeModel, network);
        assertEquals("The car should store it's ID", "12345", referenceCar.getId());
        assertEquals("At t=initialTime the car should be at it's initial position", initialPosition.getSI(),
                referenceCar.position(lane, referenceCar.getReference(), initialTime).getSI(), 0.0001);
        assertEquals("The car should store it's initial speed", initialSpeed.getSI(), referenceCar.getSpeed().getSI(), 0.00001);
        assertEquals("The car should have an initial acceleration equal to 0", 0, referenceCar.getAcceleration().getSI(),
                0.0001);
        // TODO check with following model as part of tactical planner
        // assertEquals("The gtu following model should be " + gtuFollowingModel, gtuFollowingModel, referenceCar
        // .getBehavioralCharacteristics().getGtuFollowingModel());
        // There is (currently) no way to retrieve the lane change model of a GTU.
    }

    /**
     * Create the simplest possible simulator.
     * @return DEVSSimulator.TimeDoubleUnit
     * @throws SimRuntimeException on ???
     * @throws NamingException on ???
     */
    public static OtsSimulatorInterface makeSimulator() throws SimRuntimeException, NamingException
    {
        OtsSimulatorInterface simulator = new OtsSimulator("CarTest");
        Model model = new Model(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model);
        return simulator;
    }

    /**
     * Create a new Car.
     * @param id String; the name (number) of the Car
     * @param gtuType GtuType; the type of the new car
     * @param lane Lane; the lane on which the new Car is positioned
     * @param initialPosition Length; the initial longitudinal position of the new Car
     * @param initialSpeed Speed; the initial speed
     * @param simulator OTSDEVVSimulator; the simulator that controls the new Car (and supplies the initial value for
     *            getLastEvalutionTime())
     * @param gtuFollowingModel GtuFollowingModel; the GTU following model
     * @param laneChangeModel LaneChangeModel; the lane change model
     * @param network the network
     * @return Car; the new Car
     * @throws NamingException on network error when making the animation
     * @throws NetworkException when the GTU cannot be placed on the given lane.
     * @throws SimRuntimeException when the move method cannot be scheduled.
     * @throws GtuException when construction of the GTU fails (probably due to an invalid parameter)
     * @throws OtsGeometryException when the initial path is wrong
     */
    public static LaneBasedIndividualGtu makeReferenceCar(final String id, final GtuType gtuType, final Lane lane,
            final Length initialPosition, final Speed initialSpeed, final OtsSimulatorInterface simulator,
            final GtuFollowingModelOld gtuFollowingModel, final LaneChangeModel laneChangeModel, final OtsRoadNetwork network)
            throws NamingException, NetworkException, SimRuntimeException, GtuException, OtsGeometryException
    {
        Length length = new Length(5.0, METER);
        Length width = new Length(2.0, METER);
        Set<LanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
        initialLongitudinalPositions.add(new LanePosition(lane, initialPosition));
        Speed maxSpeed = new Speed(120, KM_PER_HOUR);
        Parameters parameters = DefaultTestParameters.create();
        LaneBasedIndividualGtu gtu =
                new LaneBasedIndividualGtu(id, gtuType, length, width, maxSpeed, length.times(0.5), simulator, network);
        LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                new LaneBasedCfLcTacticalPlanner(gtuFollowingModel, laneChangeModel, gtu), gtu);
        gtu.setParameters(parameters);
        gtu.init(strategicalPlanner, initialLongitudinalPositions, initialSpeed);

        return gtu;
    }

    /**
     * @param network RoadNetwork; the network
     * @param laneType LaneType&lt;String&gt;; the type of the lane
     * @param simulator OTSSimulatorInterface; simulator
     * @return a lane of 1000 m long.
     * @throws NetworkException on network error
     * @throws OtsGeometryException when center line or contour of a link or lane cannot be generated
     */
    public static Lane makeLane(final OtsRoadNetwork network, final LaneType laneType, final OtsSimulatorInterface simulator)
            throws NetworkException, OtsGeometryException
    {
        OtsRoadNode n1 = new OtsRoadNode(network, "n1", new OtsPoint3D(0, 0), Direction.ZERO);
        OtsRoadNode n2 = new OtsRoadNode(network, "n2", new OtsPoint3D(100000.0, 0.0), Direction.ZERO);
        OtsPoint3D[] coordinates = new OtsPoint3D[] {new OtsPoint3D(0.0, 0.0), new OtsPoint3D(100000.0, 0.0)};
        CrossSectionLink link12 = new CrossSectionLink(network, "link12", n1, n2, network.getLinkType(LinkType.DEFAULTS.ROAD),
                new OtsLine3D(coordinates), LaneKeepingPolicy.KEEPRIGHT);
        Length latPos = new Length(0.0, METER);
        Length width = new Length(4.0, METER);
        return new Lane(link12, "lane.1", latPos, latPos, width, width, laneType, new Speed(100, KM_PER_HOUR));
    }

    /** The helper model. */
    protected static class Model extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20141027L;

        /**
         * @param simulator the simulator to use
         */
        public Model(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public final OtsRoadNetwork getNetwork()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "CarTestModel";
        }
    }
}

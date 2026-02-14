package org.opentrafficsim.road.car;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.FixedCarFollowing;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.gtu.tactical.lmrs.Lmrs;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory.Setting;
import org.opentrafficsim.road.network.CrossSectionLink;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.LaneGeometryUtil;
import org.opentrafficsim.road.network.LaneKeepingPolicy;
import org.opentrafficsim.road.network.LanePosition;
import org.opentrafficsim.road.network.LaneType;
import org.opentrafficsim.road.network.RoadNetwork;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class CarTest implements UNITS
{

    /** */
    private CarTest()
    {
        // do not instantiate test class
    }

    /**
     * Test some basics of the Car class.
     * @throws NetworkException on ???
     * @throws SimRuntimeException on ???
     * @throws NamingException on ???
     * @throws GtuException on ???
     */
    @SuppressWarnings("static-method")
    @Test
    public void carTest() throws NetworkException, SimRuntimeException, NamingException, GtuException
    {
        Duration initialTime = new Duration(0, DurationUnit.SI);
        OtsSimulatorInterface simulator = makeSimulator();
        RoadNetwork network = new RoadNetwork("network", simulator);
        GtuType gtuType = DefaultsNl.CAR;
        LaneType laneType = DefaultsRoadNl.TWO_WAY_LANE;
        Lane lane = makeLane(network, laneType, simulator);
        Length initialPosition = new Length(12, METER);
        Speed initialSpeed = new Speed(34, KM_PER_HOUR);
        LaneBasedGtu referenceCar = makeReferenceCar("12345", gtuType, lane, initialPosition, initialSpeed, network);
        assertEquals("12345", referenceCar.getId(), "The car should store it's ID");
        assertEquals(initialPosition.getSI(), referenceCar.getPosition(lane, referenceCar.getReference(), initialTime).getSI(),
                0.0001, "At t=initialTime the car should be at it's initial position");
        assertEquals(initialSpeed.getSI(), referenceCar.getSpeed().getSI(), 0.00001, "The car should store it's initial speed");
        assertEquals(0, referenceCar.getAcceleration().getSI(), 0.0001,
                "The car should have an initial acceleration equal to 0");
        // TODO check with following model as part of tactical planner
        // assertEquals("The gtu following model should be " + gtuFollowingModel, gtuFollowingModel, referenceCar
        // .getBehavioralCharacteristics().getGtuFollowingModel());
        // There is (currently) no way to retrieve the lane change model of a GTU.
    }

    /**
     * Create the simplest possible simulator.
     * @return DevsSimulator.TimeDoubleUnit
     * @throws SimRuntimeException on ???
     * @throws NamingException on ???
     */
    public static OtsSimulatorInterface makeSimulator() throws SimRuntimeException, NamingException
    {
        OtsSimulatorInterface simulator = new OtsSimulator("CarTest");
        Model model = new Model(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model,
                HistoryManagerDevs.noHistory(simulator));
        return simulator;
    }

    /**
     * Create a new Car.
     * @param id the name (number) of the Car
     * @param gtuType the type of the new car
     * @param lane the lane on which the new Car is positioned
     * @param initialPosition the initial longitudinal position of the new Car
     * @param initialSpeed the initial speed
     * @param network the network
     * @return the new Car
     * @throws NamingException on network error when making the animation
     * @throws NetworkException when the GTU cannot be placed on the given lane.
     * @throws SimRuntimeException when the move method cannot be scheduled.
     * @throws GtuException when construction of the GTU fails (probably due to an invalid parameter)
     */
    public static LaneBasedGtu makeReferenceCar(final String id, final GtuType gtuType, final Lane lane,
            final Length initialPosition, final Speed initialSpeed, final RoadNetwork network)
            throws NamingException, NetworkException, SimRuntimeException, GtuException
    {
        Length length = new Length(5.0, METER);
        Length width = new Length(2.0, METER);
        Speed maxSpeed = new Speed(120, KM_PER_HOUR);
        Parameters parameters = DefaultTestParameters.create();
        LaneBasedGtu gtu = new LaneBasedGtu(id, gtuType, length, width, maxSpeed, length.times(0.5), network);
        gtu.setParameters(parameters);
        LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(new LmrsFactory<>(Lmrs::new)
                .set(Setting.CAR_FOLLOWING_MODEL, (h, v) -> new FixedCarFollowing().get()).create(gtu), gtu);
        gtu.init(strategicalPlanner, new LanePosition(lane, initialPosition).getLocation(), initialSpeed);

        return gtu;
    }

    /**
     * Makes lane.
     * @param network the network
     * @param laneType the type of the lane
     * @param simulator simulator
     * @return a lane of 1000 m long.
     * @throws NetworkException on network error
     */
    public static Lane makeLane(final RoadNetwork network, final LaneType laneType, final OtsSimulatorInterface simulator)
            throws NetworkException
    {
        Node n1 = new Node(network, "n1", new Point2d(0, 0), Direction.ZERO);
        Node n2 = new Node(network, "n2", new Point2d(100000.0, 0.0), Direction.ZERO);
        Point2d[] coordinates = new Point2d[] {new Point2d(0.0, 0.0), new Point2d(100000.0, 0.0)};
        CrossSectionLink link12 = new CrossSectionLink(network, "link12", n1, n2, DefaultsNl.ROAD, new OtsLine2d(coordinates),
                null, LaneKeepingPolicy.KEEPRIGHT);
        Length latPos = new Length(0.0, METER);
        Length width = new Length(4.0, METER);
        return LaneGeometryUtil.createStraightLane(link12, "lane.1", latPos, latPos, width, width, laneType,
                Map.of(DefaultsNl.VEHICLE, new Speed(100, KM_PER_HOUR)));
    }

    /** The helper model. */
    protected static class Model extends AbstractOtsModel
    {
        /**
         * Constructor.
         * @param simulator the simulator to use
         */
        public Model(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        @Override
        public final void constructModel() throws SimRuntimeException
        {
            //
        }

        @Override
        public final RoadNetwork getNetwork()
        {
            return null;
        }
    }
}

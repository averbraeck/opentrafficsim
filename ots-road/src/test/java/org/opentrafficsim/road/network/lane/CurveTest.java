package org.opentrafficsim.road.network.lane;

import javax.naming.NamingException;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DirectionUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.car.CarTest;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.FixedLaneChangeModel;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * Verify that GTUs register and unregister at the correct times and locations when following a curve.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class CurveTest
{

    /**
     * Let GTUs drive through a curve and check (de-)registration times at each node.
     * @throws NamingException on error
     * @throws SimRuntimeException on error
     * @throws NetworkException on error
     * @throws GtuException on error
     */
    @Test
    public final void curveTest() throws SimRuntimeException, NamingException, NetworkException, GtuException
    {
        final int laneCount = 1;
        OtsSimulatorInterface simulator = CarTest.makeSimulator();
        RoadNetwork network = new RoadNetwork("curve test network", simulator);
        GtuType gtuType = DefaultsNl.CAR;
        LaneType laneType = DefaultsRoadNl.TWO_WAY_LANE;
        Speed speedLimit = new Speed(50, SpeedUnit.KM_PER_HOUR);
        Node origin = new Node(network, "origin", new Point2d(10, 10), Direction.ZERO);
        Node curveStart = new Node(network, "curveStart", new Point2d(100, 10), Direction.ZERO);
        Node curveEnd = new Node(network, "curveEnd", new Point2d(150, 60), new Direction(90, DirectionUnit.EAST_DEGREE));
        Node destination =
                new Node(network, "destination", new Point2d(150, 150), new Direction(90, DirectionUnit.EAST_DEGREE));
        Lane[] straight1 = LaneFactory.makeMultiLane(network, "straight1", origin, curveStart, null, laneCount, laneType,
                speedLimit, simulator, DefaultsNl.VEHICLE);
        Lane[] straight2 = LaneFactory.makeMultiLane(network, "straight2", curveEnd, destination, null, laneCount, laneType,
                speedLimit, simulator, DefaultsNl.VEHICLE);
        OtsLine2d curveLine = LaneFactory.makeBezier(origin, curveStart, curveEnd, destination);
        Lane[] curve = LaneFactory.makeMultiLane(network, "bezier", curveStart, curveEnd,
                curveLine.getPointList().toArray(new Point2d[curveLine.size()]), laneCount, laneType, speedLimit, simulator,
                DefaultsNl.VEHICLE);
        Lane[][] laneSets = new Lane[][] {straight1, curve, straight2};
        Length initialPosition = new Length(5, LengthUnit.METER);
        Speed speed = new Speed(10, SpeedUnit.SI);
        for (int lane = 0; lane < laneCount; lane++)
        {
            // System.out.println("Lane is " + lane);
            double cumulativeLength = 0;
            for (Lane[] set : laneSets)
            {
                cumulativeLength += set[lane].getLength().si;
                double timeAtEnd = simulator.getSimulatorTime().si + (cumulativeLength - initialPosition.si) / speed.si;
                System.out.println("lane " + set[lane] + " length is " + set[lane].getLength()
                        + " time for reference to get to end " + timeAtEnd);
            }
            LaneBasedGtu car = CarTest.makeReferenceCar("car", gtuType, straight1[lane], initialPosition, speed,
                    new FixedAccelerationModel(new Acceleration(0, AccelerationUnit.SI), new Duration(25, DurationUnit.SI)),
                    new FixedLaneChangeModel(null), (RoadNetwork) network);
            printEventList(simulator);
            System.out.println("STEP");
            simulator.step();
            printEventList(simulator);
            System.out.println("STEP");
            simulator.step();
            printEventList(simulator);
            // TODO finish writing this test
        }
    }

    /**
     * Print all scheduled events of an OtsSimulatorInterface.
     * @param simulator the OtsSimulatorInterface
     */
    public final void printEventList(final OtsSimulatorInterface simulator)
    {
        for (SimEventInterface<Duration> se : simulator.getEventList())
        {
            System.out.println("se: " + se);
        }

    }

}

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
import org.junit.Test;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.car.CarTest;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.FixedLaneChangeModel;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * Verify that GTUs register and unregister at the correct times and locations when following a curve.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class CurveTest
{

    /**
     * Let GTUs drive through a curve and check (de-)registration times at each node.
     * @throws OTSGeometryException on error
     * @throws NamingException on error
     * @throws SimRuntimeException on error
     * @throws NetworkException on error
     * @throws GtuException on error
     */
    @Test
    public final void curveTest()
            throws OTSGeometryException, SimRuntimeException, NamingException, NetworkException, GtuException
    {
        final int laneCount = 1;
        OtsSimulatorInterface simulator = CarTest.makeSimulator();
        OTSRoadNetwork network = new OTSRoadNetwork("curve test network", true, simulator);
        GtuType gtuType = network.getGtuType(GtuType.DEFAULTS.CAR);
        LaneType laneType = network.getLaneType(LaneType.DEFAULTS.TWO_WAY_LANE);
        Speed speedLimit = new Speed(50, SpeedUnit.KM_PER_HOUR);
        OTSRoadNode origin = new OTSRoadNode(network, "origin", new OTSPoint3D(10, 10, 0), Direction.ZERO);
        OTSRoadNode curveStart = new OTSRoadNode(network, "curveStart", new OTSPoint3D(100, 10, 0), Direction.ZERO);
        OTSRoadNode curveEnd =
                new OTSRoadNode(network, "curveEnd", new OTSPoint3D(150, 60, 0), new Direction(90, DirectionUnit.EAST_DEGREE));
        OTSRoadNode destination = new OTSRoadNode(network, "destination", new OTSPoint3D(150, 150, 0),
                new Direction(90, DirectionUnit.EAST_DEGREE));
        Lane[] straight1 = LaneFactory.makeMultiLane(network, "straight1", origin, curveStart, null, laneCount, laneType,
                speedLimit, simulator);
        Lane[] straight2 = LaneFactory.makeMultiLane(network, "straight2", curveEnd, destination, null, laneCount, laneType,
                speedLimit, simulator);
        OTSLine3D curveLine = LaneFactory.makeBezier(origin, curveStart, curveEnd, destination);
        Lane[] curve = LaneFactory.makeMultiLane(network, "bezier", curveStart, curveEnd, curveLine.getPoints(), laneCount,
                laneType, speedLimit, simulator);
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
            LaneBasedIndividualGtu car =
                    CarTest.makeReferenceCar("car", gtuType, straight1[lane], initialPosition, speed, simulator,
                            new FixedAccelerationModel(new Acceleration(0, AccelerationUnit.SI),
                                    new Duration(25, DurationUnit.SI)),
                            new FixedLaneChangeModel(null), (OTSRoadNetwork) network);
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
     * Print all scheduled events of an OTSSimulatorInterface.
     * @param simulator OTSSimulatorInterface; the OTSSimulatorInterface
     */
    public final void printEventList(final OtsSimulatorInterface simulator)
    {
        for (SimEventInterface<Duration> se : simulator.getEventList())
        {
            System.out.println("se: " + se);
        }

    }

}

package org.opentrafficsim.road.network.lane;

import static org.opentrafficsim.core.gtu.GTUType.CAR;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.car.CarTest;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.FixedLaneChangeModel;
import org.opentrafficsim.road.network.factory.LaneFactory;

/**
 * Verify that GTUs register and unregister at the correct times and locations when following a curve.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 15, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class CurveTest
{

    /**
     * Let GTUs drive through a curve and check (de-)registration times at each node.
     * @throws OTSGeometryException on error
     * @throws NamingException on error
     * @throws SimRuntimeException on error
     * @throws NetworkException on error
     * @throws GTUException on error
     */
    @Test
    public final void curveTest() throws OTSGeometryException, SimRuntimeException, NamingException, NetworkException, GTUException
    {
        final int laneCount = 1;
        GTUType gtuType = CAR;
        LaneType laneType = LaneType.TWO_WAY_LANE;
        Speed speedLimit = new Speed(50, SpeedUnit.KM_PER_HOUR);
        OTSDEVSSimulatorInterface simulator = CarTest.makeSimulator();
        Network network = new OTSNetwork("curve test network");
        OTSNode origin = new OTSNode(network, "origin", new OTSPoint3D(10, 10, 0));
        OTSNode curveStart = new OTSNode(network, "curveStart", new OTSPoint3D(100, 10, 0));
        OTSNode curveEnd = new OTSNode(network, "curveEnd", new OTSPoint3D(150, 60, 0));
        OTSNode destination = new OTSNode(network, "destination", new OTSPoint3D(150, 150, 0));
        Lane[] straight1 = LaneFactory.makeMultiLane(network, "straight1", origin, curveStart, null, laneCount, laneType,
                speedLimit, simulator, LongitudinalDirectionality.DIR_PLUS);
        Lane[] straight2 = LaneFactory.makeMultiLane(network, "straight2", curveEnd, destination, null, laneCount, laneType,
                speedLimit, simulator, LongitudinalDirectionality.DIR_PLUS);
        OTSLine3D curveLine = LaneFactory.makeBezier(origin, curveStart, curveEnd, destination);
        Lane[] curve = LaneFactory.makeMultiLane(network, "bezier", curveStart, curveEnd, curveLine.getPoints(), laneCount,
                laneType, speedLimit, simulator, LongitudinalDirectionality.DIR_PLUS);
        Lane[][] laneSets = new Lane[][] { straight1, curve, straight2 };
        Length initialPosition = new Length(5, LengthUnit.METER);
        Speed speed = new Speed(10, SpeedUnit.SI);
        for (int lane = 0; lane < laneCount; lane++)
        {
            // System.out.println("Lane is " + lane);
            double cumulativeLength = 0;
            for (Lane[] set : laneSets)
            {
                cumulativeLength += set[lane].getLength().si;
                double timeAtEnd = simulator.getSimulatorTime().get().si + (cumulativeLength - initialPosition.si) / speed.si;
                System.out.println("lane " + set[lane] + " length is " + set[lane].getLength()
                        + " time for reference to get to end " + timeAtEnd);
            }
            LaneBasedIndividualGTU car = CarTest.makeReferenceCar("car", gtuType, straight1[lane], initialPosition, speed,
                    (OTSDEVSSimulator) simulator,
                    new FixedAccelerationModel(new Acceleration(0, AccelerationUnit.SI), new Duration(25, DurationUnit.SI)),
                    new FixedLaneChangeModel(null), (OTSNetwork) network);
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
     * Print all scheduled events of an OTSDEVSSimulatorInterface.
     * @param simulator OTSDEVSSimulatorInterface; the OTSDEVSSimulatorInterface
     */
    public final void printEventList(final OTSDEVSSimulatorInterface simulator)
    {
        for (SimEventInterface<OTSSimTimeDouble> se : simulator.getEventList())
        {
            System.out.println("se: " + se);
        }

    }

}

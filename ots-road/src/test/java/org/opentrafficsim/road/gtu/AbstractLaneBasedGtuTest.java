package org.opentrafficsim.road.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
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
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGtu;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCfLcTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.GtuFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.FixedLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OtsRoadNode;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the various methods of an AbstractLaneBasedGtu.<br>
 * As abstract classes cannot be directly
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class AbstractLaneBasedGtuTest implements UNITS
{
    /**
     * Test that the constructor puts the supplied values in the correct fields, then check the motion of the GTU.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public final void abstractLaneBasedGtuTest() throws Exception
    {
        // This initialization code should probably be moved to a helper method that will be used in several tests.
        // First we need a set of Lanes
        // To create Lanes we need Nodes and a LaneType
        // And a simulator, but for that we first need something that implements OTSModelInterface
        OtsSimulatorInterface simulator = new OtsSimulator("abstractLaneBasedGtuTest");
        OtsRoadNetwork network = new OtsRoadNetwork("lane base gtu test network", true, simulator);
        OtsModelInterface model = new DummyModel(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(1, DurationUnit.HOUR), model);
        OtsRoadNode nodeAFrom = new OtsRoadNode(network, "AFrom", new OtsPoint3D(0, 0, 0), Direction.ZERO);
        OtsRoadNode nodeATo = new OtsRoadNode(network, "ATo", new OtsPoint3D(1000, 0, 0), Direction.ZERO);
        GtuType gtuType = network.getGtuType(GtuType.DEFAULTS.CAR);
        LaneType laneType = network.getLaneType(LaneType.DEFAULTS.TWO_WAY_LANE);

        Lane[] lanesGroupA = LaneFactory.makeMultiLane(network, "A", nodeAFrom, nodeATo, null, 3, laneType,
                new Speed(100, KM_PER_HOUR), simulator);
        // A GTU can exist on several lanes at once; create another lane group to test that
        OtsRoadNode nodeBFrom = new OtsRoadNode(network, "BFrom", new OtsPoint3D(10, 0, 0), Direction.ZERO);
        OtsRoadNode nodeBTo = new OtsRoadNode(network, "BTo", new OtsPoint3D(1000, 0, 0), Direction.ZERO);
        Lane[] lanesGroupB = LaneFactory.makeMultiLane(network, "B", nodeBFrom, nodeBTo, null, 3, laneType,
                new Speed(100, KM_PER_HOUR), simulator);
        Set<LanePosition> initialLongitudinalPositions = new LinkedHashSet<>(2);

        Length positionA = new Length(100, METER);
        initialLongitudinalPositions.add(new LanePosition(lanesGroupA[1], positionA));
        Length positionB = new Length(90, METER);
        initialLongitudinalPositions.add(new LanePosition(lanesGroupB[1], positionB));
        // A Car needs a CarFollowingModel
        Acceleration acceleration = new Acceleration(2, METER_PER_SECOND_2);
        Duration validFor = new Duration(10, SECOND);
        GtuFollowingModelOld gfm = new FixedAccelerationModel(acceleration, validFor);
        // A Car needs a lane change model
        // AbstractLaneChangeModel laneChangeModel = new Egoistic();
        LaneChangeModel laneChangeModel = new FixedLaneChangeModel(null);
        // A Car needs an initial speed
        Speed initialSpeed = new Speed(50, KM_PER_HOUR);
        // Length of the Car
        Length carLength = new Length(4, METER);
        // Width of the Car
        Length carWidth = new Length(1.8, METER);
        // Maximum speed of the Car
        Speed maximumSpeed = new Speed(200, KM_PER_HOUR);
        // ID of the Car
        String carID = "theCar";
        // List of Nodes visited by the Car
        List<Node> nodeList = new ArrayList<Node>();
        nodeList.add(nodeAFrom);
        nodeList.add(nodeATo);
        // Route of the Car
        Route route = new Route("Route", gtuType, nodeList);
        // Now we can make a GTU
        Parameters parameters = DefaultTestParameters.create(); // new
                                                                // BehavioralCharacteristics();
        // LaneBasedBehavioralCharacteristics drivingCharacteristics =
        // new LaneBasedBehavioralCharacteristics(gfm, laneChangeModel);
        LaneBasedIndividualGtu car = new LaneBasedIndividualGtu(carID, gtuType, carLength, carWidth, maximumSpeed,
                carLength.times(0.5), simulator, network);
        LaneBasedStrategicalPlanner strategicalPlanner =
                new LaneBasedStrategicalRoutePlanner(new LaneBasedCfLcTacticalPlanner(gfm, laneChangeModel, car), car);
        car.setParameters(parameters);
        car.init(strategicalPlanner, initialLongitudinalPositions, initialSpeed);
        // Now we can verify the various fields in the newly created Car
        assertEquals("ID of the car should be identical to the provided one", carID, car.getId());
        // TODO: Test with gfm as part of tactical planner
        // assertEquals("GTU following model should be identical to the provided one", gfm, car
        // .getBehavioralCharacteristics().getGtuFollowingModel());
        assertEquals("Width should be identical to the provided width", carWidth, car.getWidth());
        assertEquals("Length should be identical to the provided length", carLength, car.getLength());
        assertEquals("GTU type should be identical to the provided one", gtuType, car.getType());
        assertEquals("front in lanesGroupA[1] is positionA", positionA.getSI(),
                car.position(lanesGroupA[1], car.getReference()).getSI(), 0.0001);
        assertEquals("front in lanesGroupB[1] is positionB", positionB.getSI(),
                car.position(lanesGroupB[1], car.getReference()).getSI(), 0.0001);
        // assertEquals("acceleration is 0", 0, car.getAcceleration().getSI(), 0.00001);
        // edit wouter schakel: fixed acceleration model has a=2.0m/s^2, first plan is made during initialization
        assertEquals("acceleration is 2", 2.0, car.getAcceleration().getSI(), 0.00001);
        assertEquals("longitudinal speed is " + initialSpeed, initialSpeed.getSI(), car.getSpeed().getSI(), 0.00001);
        assertEquals("lastEvaluation time is 0", 0, car.getOperationalPlan().getStartTime().getSI(), 0.00001);
        // Test the position(Lane, RelativePosition) method
        // WS: Removed as null check has been removed from position(...)
        // try
        // {
        // car.position(null, car.getFront());
        // fail("position on null lane should have thrown a NetworkException");
        // }
        // catch (GTUException ne)
        // {
        // // Ignore
        // }
        for (Lane[] laneGroup : new Lane[][] {lanesGroupA, lanesGroupB})
        {
            for (int laneIndex = 0; laneIndex < laneGroup.length; laneIndex++)
            {
                Lane lane = laneGroup[laneIndex];
                boolean expectException = 1 != laneIndex;
                for (RelativePosition relativePosition : new RelativePosition[] {car.getFront(), car.getReference(),
                        car.getRear()})
                {
                    // System.out.println("lane:" + lane + ", expectedException: " + expectException
                    // + ", relativePostion: " + relativePosition);
                    try
                    {
                        Length position = car.position(lane, relativePosition);
                        if (expectException)
                        {
                            // System.out.println("position: " + position);
                            fail("Calling position on lane that the car is NOT on should have thrown a NetworkException");
                        }
                        else
                        {
                            Length expectedPosition = laneGroup == lanesGroupA ? positionA : positionB;
                            expectedPosition = expectedPosition.plus(relativePosition.getDx());
                            // System.out.println("reported position: " + position);
                            // System.out.println("expected position: " + expectedPosition);
                            assertEquals("Position should match initial position", expectedPosition.getSI(), position.getSI(),
                                    0.0001);
                        }
                    }
                    catch (GtuException ne)
                    {
                        if (!expectException)
                        {
                            System.out.println(ne);
                            fail("Calling position on lane that the car is on should NOT have thrown a NetworkException");
                        }
                    }
                }
            }
        }
        // Assign a movement to the car (10 seconds of acceleration of 2 m/s/s)
        // scheduled event that moves the car at t=0
        assertEquals("lastEvaluation time is 0", 0, car.getOperationalPlan().getStartTime().getSI(), 0.00001);
        // assertEquals("nextEvaluation time is 0", 0, car.getOperationalPlan().getEndTime().getSI(), 0.00001);
        // edit wouter schakel: fixed acceleration model has t=10s, first plan is made during initialization
        assertEquals("nextEvaluation time is 10", 10.0, car.getOperationalPlan().getEndTime().getSI(), 0.00001);
        // Increase the simulator clock in small steps and verify the both positions on all lanes at each step
        double step = 0.01d;
        for (int i = 0;; i++)
        {
            Time stepTime = new Time(i * step, TimeUnit.BASE_SECOND);
            if (stepTime.getSI() > validFor.getSI())
            {
                break;
            }
            if (stepTime.getSI() > 0.5)
            {
                step = 0.1; // Reduce testing time by increasing the step size
            }
            // System.out.println("Simulating until " + stepTime.getSI());
            simulator.runUpTo(stepTime);
            while (simulator.isStartingOrRunning())
            {
                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException ie)
                {
                    ie = null; // ignore
                }
            }
            // Debugging code that helped locate a problem in the DSOL runUpTo code.
            // System.out.println("stepTime is " + stepTime);
            // System.out.println("Car simulator time " + car.getSimulator().getSimulatorTime());
            // System.out.println("Simulator time is now " + simulator.getSimulatorTime());
            // if (simulator != car.getSimulator())
            // {
            // System.err.println("Car runs on a different simulator!");
            // }
            // System.out.println("operational plan is " + car.getOperationalPlan());
            // System.out.println("operational plan end time is " + car.getOperationalPlan().getEndTime());
            // car.getOperationalPlan().getEndTime();
            // if (stepTime.getSI() > 0)
            // {
            // assertEquals("nextEvaluation time is " + validFor, validFor.getSI(),
            // car.getOperationalPlan().getEndTime().getSI(), 0.0001);
            // assertEquals("acceleration is " + acceleration, acceleration.getSI(), car.getAcceleration().getSI(), 0.00001);
            // }
            Speed longitudinalSpeed = car.getSpeed();
            double expectedLongitudinalSpeed = initialSpeed.getSI() + stepTime.getSI() * acceleration.getSI();
            assertEquals("longitudinal speed is " + expectedLongitudinalSpeed, expectedLongitudinalSpeed,
                    longitudinalSpeed.getSI(), 0.00001);
            for (RelativePosition relativePosition : new RelativePosition[] {car.getFront(), car.getRear()})
            {
                Map<Lane, Double> positions = car.fractionalPositions(relativePosition);
                assertEquals("Car should be in two lanes", 2, positions.size());
                Double pos = positions.get(lanesGroupA[1]);
                // System.out.println("Fractional positions: " + positions);
                assertTrue("Car should be in lane 1 of lane group A", null != pos);
                assertEquals("fractional position should be equal to result of fractionalPosition(lane, ...)", pos,
                        car.fractionalPosition(lanesGroupA[1], relativePosition), 0.0000001);
                pos = positions.get(lanesGroupB[1]);
                assertTrue("Car should be in lane 1 of lane group B", null != pos);
                assertEquals("fractional position should be equal to result of fractionalPosition(lane, ...)", pos,
                        car.fractionalPosition(lanesGroupB[1], relativePosition), 0.0000001);
            }
            for (Lane[] laneGroup : new Lane[][] {lanesGroupA, lanesGroupB})
            {
                for (int laneIndex = 0; laneIndex < laneGroup.length; laneIndex++)
                {
                    Lane lane = laneGroup[laneIndex];
                    boolean expectException = 1 != laneIndex;
                    for (RelativePosition relativePosition : new RelativePosition[] {car.getFront(), car.getReference(),
                            car.getRear()})
                    {
                        // System.out.println("lane:" + lane + ", expectedException: " + expectException
                        // + ", relativePostion: " + relativePosition);
                        try
                        {
                            Length position = car.position(lane, relativePosition);
                            if (expectException)
                            {
                                // System.out.println("position: " + position);
                                fail("Calling position on lane that the car is NOT on should have thrown a "
                                        + "NetworkException");
                            }
                            else
                            {
                                Length expectedPosition = laneGroup == lanesGroupA ? positionA : positionB;
                                expectedPosition = expectedPosition
                                        .plus(new Length(stepTime.getSI() * initialSpeed.getSI(), LengthUnit.SI));
                                expectedPosition = expectedPosition.plus(new Length(
                                        0.5 * acceleration.getSI() * stepTime.getSI() * stepTime.getSI(), LengthUnit.SI));
                                expectedPosition = expectedPosition.plus(relativePosition.getDx());
                                // System.out.println("reported position: " + position);
                                // System.out.println("expected position: " + expectedPosition);
                                assertEquals("Position should match initial position", expectedPosition.getSI(),
                                        position.getSI(), 0.0001);
                            }
                        }
                        catch (GtuException ne)
                        {
                            if (!expectException)
                            {
                                System.out.println(ne);
                                fail("Calling position on lane that the car is on should NOT have thrown a NetworkException");
                            }
                        }
                        try
                        {
                            double fractionalPosition = car.fractionalPosition(lane, relativePosition);
                            if (expectException)
                            {
                                // System.out.println("position: " + position);
                                fail("Calling position on lane that the car is NOT on should have thrown a NetworkException");
                            }
                            else
                            {
                                Length expectedPosition = laneGroup == lanesGroupA ? positionA : positionB;
                                expectedPosition = expectedPosition
                                        .plus(new Length(stepTime.getSI() * initialSpeed.getSI(), LengthUnit.SI));
                                expectedPosition = expectedPosition.plus(new Length(
                                        0.5 * acceleration.getSI() * stepTime.getSI() * stepTime.getSI(), LengthUnit.SI));
                                expectedPosition = expectedPosition.plus(relativePosition.getDx());
                                // System.out.println("reported position: " + position);
                                // System.out.println("expected position: " + expectedPosition);
                                double expectedFractionalPosition = expectedPosition.getSI() / lane.getLength().getSI();
                                assertEquals("Position should match initial position", expectedFractionalPosition,
                                        fractionalPosition, 0.000001);
                            }
                        }
                        catch (GtuException ne)
                        {
                            if (!expectException)
                            {
                                System.out.println(ne);
                                fail("Calling fractionalPosition on lane that the car is on should NOT have thrown a "
                                        + "NetworkException");
                            }
                        }
                    }
                }
            }
        }
        // A GTU can exist on several lanes at once; create another lane group to test that
        OtsRoadNode nodeCFrom = new OtsRoadNode(network, "CFrom", new OtsPoint3D(10, 100, 0), Direction.ZERO);
        OtsRoadNode nodeCTo = new OtsRoadNode(network, "CTo", new OtsPoint3D(1000, 0, 0), Direction.ZERO);
        Lane[] lanesGroupC = LaneFactory.makeMultiLane(network, "C", nodeCFrom, nodeCTo, null, 3, laneType,
                new Speed(100, KM_PER_HOUR), simulator);
        // wouter schakel car.enterLane(lanesGroupC[0], new Length(0.0, LengthUnit.SI), GTUDirectionality.DIR_PLUS);
        for (RelativePosition relativePosition : new RelativePosition[] {car.getFront(), car.getRear()})
        {
            Map<Lane, Double> positions = car.fractionalPositions(relativePosition);
            // wouter schakel assertEquals("Car should be in three lanes", 3, positions.size());
            Double pos = positions.get(lanesGroupA[1]);
            assertTrue("Car should be in lane 1 of lane group A", null != pos);
            assertEquals("fractional position should be equal to result of fractionalPosition(lane, ...)", pos,
                    car.fractionalPosition(lanesGroupA[1], relativePosition), 0.0000001);
            pos = positions.get(lanesGroupB[1]);
            assertTrue("Car should be in lane 1 of lane group B", null != pos);
            assertEquals("fractional position should be equal to result of fractionalPosition(lane, ...)", pos,
                    car.fractionalPosition(lanesGroupB[1], relativePosition), 0.0000001);
            pos = positions.get(lanesGroupC[0]);
            // wouter schakel assertTrue("Car should be in lane 0 of lane group C", null != pos);
            // The next one fails - maybe I don't understand something - PK
            // assertEquals("fractional position should be 0", 0,
            // car.fractionalPosition(lanesGroupC[0], relativePosition), 0.0000001);
            // wouter schakel assertEquals("fractional position should be equal to result of fractionalPosition(lane, ...)",
            // pos,
            // car.fractionalPosition(lanesGroupC[0], relativePosition), 0.0000001);
        }
        // wouter schakel car.leaveLane(lanesGroupA[1]);
        for (RelativePosition relativePosition : new RelativePosition[] {car.getFront(), car.getRear()})
        {
            Map<Lane, Double> positions = car.fractionalPositions(relativePosition);
            assertEquals("Car should be in two lanes", 2, positions.size());
            Double pos = positions.get(lanesGroupB[1]);
            assertTrue("Car should be in lane 1 of lane group B", null != pos);
            assertEquals("fractional position should be equal to result of fractionalPosition(lane, ...)", pos,
                    car.fractionalPosition(lanesGroupB[1], relativePosition), 0.0000001);
            pos = positions.get(lanesGroupC[0]);
            // wouter schakel assertTrue("Car should be in lane 0 of lane group C", null != pos);
            // The next one fails - maybe I don't understand something - PK
            // assertEquals("fractional position should be 0", 0,
            // car.fractionalPosition(lanesGroupC[0], relativePosition), 0.0000001);
            // wouter schakel assertEquals("fractional position should be equal to result of fractionalPosition(lane, ...)",
            // pos,
            // car.fractionalPosition(lanesGroupC[0], relativePosition), 0.0000001);
        }
        // TODO removeLane should throw an Error when the car is not on that lane (currently this is silently ignored)
        // TODO figure out why the added lane has a non-zero position
    }
}

/**
 * Dummy OTSModelInterface.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
class DummyModel extends AbstractOtsModel
{
    /** */
    private static final long serialVersionUID = 20150114L;

    /**
     * @param simulator the simulator to use
     */
    DummyModel(final OtsSimulatorInterface simulator)
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
        return "AbstractLaneBasedGtuTest.DummyModel";
    }

}

package org.opentrafficsim.road.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedBehavioralCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.FixedLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * Test the LaneBasedGTU class.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version 27 jan. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedGTUTest implements UNITS
{
    /** The network. */
    private OTSNetwork network = new OTSNetwork("network");

    /** Id generator. */
    private IdGenerator idGenerator = new IdGenerator("id");

    /**
     * Test if a Truck covering a specified range of lanes can <i>see</i> a Car covering a specified range of lanes. <br>
     * The network is a linear array of Nodes connected by 5-Lane Links. In the middle, the Nodes are very closely spaced. A
     * truck is positioned over those center Nodes ensuring it covers several of the short Lanes in succession.
     * @param truckFromLane int; lowest rank of lane range of the truck
     * @param truckUpToLane int; highest rank of lane range of the truck
     * @param carLanesCovered int; number of lanes that the car covers
     * @throws Exception when something goes wrong (should not happen)
     */
    private void leaderFollowerParallel(int truckFromLane, int truckUpToLane, int carLanesCovered) throws Exception
    {
        // Perform a few sanity checks
        if (carLanesCovered < 1)
        {
            fail("carLanesCovered must be >= 1 (got " + carLanesCovered + ")");
        }
        if (truckUpToLane < truckFromLane)
        {
            fail("truckUpToLane must be >= truckFromLane");
        }
        OTSModelInterface model = new Model();
        SimpleSimulator simulator =
                new SimpleSimulator(new Time.Abs(0.0, SECOND), new Time.Rel(0.0, SECOND), new Time.Rel(3600.0, SECOND), model);
        GTUType carType = GTUType.makeGTUType("car");
        GTUType truckType = GTUType.makeGTUType("truck");
        LaneType laneType = new LaneType("CarLane");
        laneType.addCompatibility(carType);
        laneType.addCompatibility(truckType);
        // Create a series of Nodes (some closely bunched together)
        ArrayList<OTSNode> nodes = new ArrayList<OTSNode>();
        int[] linkBoundaries = { 0, 25, 50, 100, 101, 102, 103, 104, 105, 150, 175, 200 };
        for (int xPos : linkBoundaries)
        {
            nodes.add(new OTSNode("Node at " + xPos, new OTSPoint3D(xPos, 20, 0)));
        }
        // Now we can build a series of Links with Lanes on them
        ArrayList<CrossSectionLink> links = new ArrayList<CrossSectionLink>();
        final int laneCount = 5;
        for (int i = 1; i < nodes.size(); i++)
        {
            OTSNode fromNode = nodes.get(i - 1);
            OTSNode toNode = nodes.get(i);
            String linkName = fromNode.getId() + "-" + toNode.getId();
            Lane[] lanes =
                    LaneFactory.makeMultiLane(linkName, fromNode, toNode, null, laneCount, laneType,
                            new Speed(100, KM_PER_HOUR), simulator, LongitudinalDirectionality.DIR_PLUS);
            links.add(lanes[0].getParentLink());
        }
        // Create a long truck with its front (reference) one meter in the last link on the 3rd lane
        Length.Rel truckPosition = new Length.Rel(99.5, METER);
        Length.Rel truckLength = new Length.Rel(15, METER);

        Set<DirectedLanePosition> truckPositions =
                buildPositionsSet(truckPosition, truckLength, links, truckFromLane, truckUpToLane);
        Speed truckSpeed = new Speed(0, KM_PER_HOUR);
        Length.Rel truckWidth = new Length.Rel(2.5, METER);
        LaneChangeModel laneChangeModel = new FixedLaneChangeModel(null);
        Speed maximumVelocity = new Speed(120, KM_PER_HOUR);
        GTUFollowingModelOld gtuFollowingModel = new IDMPlusOld();
        LaneBasedBehavioralCharacteristics drivingCharacteristics =
                new LaneBasedBehavioralCharacteristics(gtuFollowingModel, laneChangeModel);
        LaneBasedStrategicalPlanner strategicalPlanner =
                new LaneBasedStrategicalRoutePlanner(drivingCharacteristics, new LaneBasedCFLCTacticalPlanner());
        LaneBasedIndividualGTU truck =
                new LaneBasedIndividualGTU("Truck", truckType, truckPositions, truckSpeed, truckLength, truckWidth,
                        maximumVelocity, simulator, strategicalPlanner, new LanePerceptionFull(), this.network);
        // Verify that the truck is registered on the correct Lanes
        int lanesChecked = 0;
        int found = 0;
        for (CrossSectionLink link : links)
        {
            for (CrossSectionElement cse : link.getCrossSectionElementList())
            {
                if (cse instanceof Lane)
                {
                    Lane lane = (Lane) cse;
                    boolean truckPositionsOnLane = false;
                    for (DirectedLanePosition pos : truckPositions)
                    {
                        if (pos.getLane().equals(lane))
                        {
                            truckPositionsOnLane = true;
                        }
                    }
                    if (truckPositionsOnLane)
                    {
                        assertTrue("Truck should be registered on Lane " + lane, lane.getGtuList().contains(truck));
                        found++;
                    }
                    else
                    {
                        assertFalse("Truck should NOT be registered on Lane " + lane, lane.getGtuList().contains(truck));
                    }
                    lanesChecked++;
                }
            }
        }
        // Make sure we tested them all
        assertEquals("lanesChecked should equals the number of Links times the number of lanes on each Link",
                laneCount * links.size(), lanesChecked);
        assertEquals("Truck should be registered in " + truckPositions.size() + " lanes", truckPositions.size(), found);
        Length.Rel forwardMaxDistance = truck.getBehavioralCharacteristics().getForwardHeadwayDistance();
        // TODO see how we can ask the vehicle to look this far ahead
        truck.getPerception().perceive();
        HeadwayGTU leader = truck.getPerception().getForwardHeadwayGTU();
        assertTrue(
                "With one vehicle in the network forward headway should return a value larger than zero, and smaller than maxDistance",
                forwardMaxDistance.getSI() >= leader.getDistance().si && leader.getDistance().si > 0);
        assertEquals("With one vehicle in the network forward headwayGTU should return null", null, leader.getGtuId());
        // TODO see how we can ask the vehicle to look this far behind
        Length.Rel reverseMaxDistance = truck.getBehavioralCharacteristics().getBackwardHeadwayDistance();
        HeadwayGTU follower = truck.getPerception().getBackwardHeadwayGTU();
        assertTrue(
                "With one vehicle in the network reverse headway should return a value less than zero, and smaller than |maxDistance|",
                Math.abs(reverseMaxDistance.getSI()) >= Math.abs(follower.getDistance().si) && follower.getDistance().si < 0);
        assertEquals("With one vehicle in the network reverse headwayGTU should return null", null, follower.getGtuId());
        Length.Rel carLength = new Length.Rel(4, METER);
        Length.Rel carWidth = new Length.Rel(1.8, METER);
        Speed carSpeed = new Speed(0, KM_PER_HOUR);
        int maxStep = linkBoundaries[linkBoundaries.length - 1];
        for (int laneRank = 0; laneRank < laneCount + 1 - carLanesCovered; laneRank++)
        {
            for (int step = 0; step < maxStep; step += 5)
            {
                if (laneRank >= truckFromLane && laneRank <= truckUpToLane
                        && step >= truckPosition.getSI() - truckLength.getSI()
                        && step - carLength.getSI() <= truckPosition.getSI())
                {
                    continue; // Truck and car would overlap; the result of that placement is not defined :-)
                }
                Length.Rel carPosition = new Length.Rel(step, METER);
                Set<DirectedLanePosition> carPositions =
                        buildPositionsSet(carPosition, carLength, links, laneRank, laneRank + carLanesCovered - 1);
                drivingCharacteristics = new LaneBasedBehavioralCharacteristics(gtuFollowingModel, laneChangeModel);
                strategicalPlanner =
                        new LaneBasedStrategicalRoutePlanner(drivingCharacteristics, new LaneBasedCFLCTacticalPlanner());
                LaneBasedIndividualGTU car =
                        new LaneBasedIndividualGTU("Car", carType, carPositions, carSpeed, carLength, carWidth,
                                maximumVelocity, simulator, strategicalPlanner, new LanePerceptionFull(), this.network);
                // leader = truck.headway(forwardMaxDistance);
                // TODO see how we can ask the vehicle to look 'forwardMaxDistance' ahead
                leader = truck.getPerception().getForwardHeadwayGTU();
                double actualHeadway = leader.getDistance().si;
                double expectedHeadway =
                        laneRank + carLanesCovered - 1 < truckFromLane || laneRank > truckUpToLane
                                || step - truckPosition.getSI() - truckLength.getSI() <= 0 ? Double.MAX_VALUE : step
                                - truckLength.getSI() - truckPosition.getSI();
                // System.out.println("carLanesCovered " + laneRank + ".." + (laneRank + carLanesCovered - 1)
                // + " truckLanesCovered " + truckFromLane + ".." + truckUpToLane + " car pos " + step
                // + " laneRank " + laneRank + " expected headway " + expectedHeadway);
                // The next assert found a subtle bug (">" instead of ">=")
                assertEquals("Forward headway should return " + expectedHeadway, expectedHeadway, actualHeadway, 0.1);
                String leaderGtuId = leader.getGtuId();
                if (expectedHeadway == Double.MAX_VALUE)
                {
                    assertEquals("Leader id should be null", null, leaderGtuId);
                }
                else
                {
                    assertEquals("Leader id should be the car id", car, leaderGtuId);
                }
                // TODO follower = truck.headway(reverseMaxDistance);
                follower = truck.getPerception().getBackwardHeadwayGTU();
                double actualReverseHeadway = follower.getDistance().si;
                double expectedReverseHeadway =
                        laneRank + carLanesCovered - 1 < truckFromLane || laneRank > truckUpToLane
                                || step + carLength.getSI() >= truckPosition.getSI() ? Double.MAX_VALUE : truckPosition.getSI()
                                - carLength.getSI() - step;
                assertEquals("Reverse headway should return " + expectedReverseHeadway, expectedReverseHeadway,
                        actualReverseHeadway, 0.1);
                String followerGtuId = follower.getGtuId();
                if (expectedReverseHeadway == Double.MAX_VALUE)
                {
                    assertEquals("Follower id should be null", null, followerGtuId);
                }
                else
                {
                    assertEquals("Follower id should be the car id", car.getId(), followerGtuId);
                }
                for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
                {
                    Lane l = null;
                    double cumulativeDistance = 0;
                    for (CrossSectionLink csl : links)
                    {
                        cumulativeDistance += csl.getLength().getSI();
                        if (cumulativeDistance >= truckPosition.getSI())
                        {
                            l = getNthLane(csl, laneIndex);
                            break;
                        }
                    }
                    leader = truck.getPerception().getForwardHeadwayGTU();
                    actualHeadway = leader.getDistance().si;
                    expectedHeadway =
                            laneIndex < laneRank || laneIndex > laneRank + carLanesCovered - 1
                                    || step - truckLength.getSI() - truckPosition.getSI() <= 0 ? Double.MAX_VALUE : step
                                    - truckLength.getSI() - truckPosition.getSI();
                    assertEquals("Headway on lane " + laneIndex + " should be " + expectedHeadway, expectedHeadway,
                            actualHeadway, 0.001);
                    leaderGtuId = leader.getGtuId();
                    if (laneIndex >= laneRank && laneIndex <= laneRank + carLanesCovered - 1
                            && step - truckLength.getSI() - truckPosition.getSI() > 0)
                    {
                        assertEquals("Leader id should be the car id", car.getId(), leaderGtuId);
                    }
                    else
                    {
                        assertEquals("Leader id should be null", null, leaderGtuId);
                    }
                    follower = truck.getPerception().getBackwardHeadwayGTU();
                    actualReverseHeadway = follower.getDistance().si;
                    expectedReverseHeadway =
                            laneIndex < laneRank || laneIndex > laneRank + carLanesCovered - 1
                                    || step + carLength.getSI() >= truckPosition.getSI() ? Double.MAX_VALUE : truckPosition
                                    .getSI() - carLength.getSI() - step;
                    assertEquals("Headway on lane " + laneIndex + " should be " + expectedReverseHeadway,
                            expectedReverseHeadway, actualReverseHeadway, 0.001);
                    followerGtuId = follower.getGtuId();
                    if (laneIndex >= laneRank && laneIndex <= laneRank + carLanesCovered - 1
                            && step + carLength.getSI() < truckPosition.getSI())
                    {
                        assertEquals("Follower id should be the car id", car, followerGtuId);
                    }
                    else
                    {
                        assertEquals("Follower id should be null", null, followerGtuId);
                    }
                }
                Set<LaneBasedGTU> leftParallel = truck.getPerception().getParallelGTUsLeft();
                int expectedLeftSize =
                        laneRank + carLanesCovered - 1 < truckFromLane - 1 || laneRank >= truckUpToLane
                                || step + carLength.getSI() <= truckPosition.getSI()
                                || step > truckPosition.getSI() + truckLength.getSI() ? 0 : 1;
                // This one caught a complex bug
                assertEquals("Left parallel set size should be " + expectedLeftSize, expectedLeftSize, leftParallel.size());
                if (leftParallel.size() > 0)
                {
                    assertTrue("Parallel GTU should be the car", leftParallel.contains(car));
                }
                Set<LaneBasedGTU> rightParallel = truck.getPerception().getParallelGTUsRight();
                int expectedRightSize =
                        laneRank + carLanesCovered - 1 <= truckFromLane || laneRank > truckUpToLane + 1
                                || step + carLength.getSI() < truckPosition.getSI()
                                || step > truckPosition.getSI() + truckLength.getSI() ? 0 : 1;
                assertEquals("Right parallel set size should be " + expectedRightSize, expectedRightSize, rightParallel.size());
                if (rightParallel.size() > 0)
                {
                    assertTrue("Parallel GTU should be the car", rightParallel.contains(car));
                }
                for (DirectedLanePosition pos : carPositions)
                {
                    pos.getLane().removeGTU(car);
                }
            }
        }
    }

    /**
     * Test the leader, follower and parallel methods.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void leaderFollowerAndParallelTest() throws Exception
    {
        // TODO leaderFollowerParallel(2, 2, 1);
        // TODO leaderFollowerParallel(2, 3, 1);
        // TODO leaderFollowerParallel(2, 2, 2);
        // TODO leaderFollowerParallel(2, 3, 2);
    }

    /**
     * Test the deltaTimeForDistance and timeAtDistance methods.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void timeAtDistanceTest() throws Exception
    {
        for (int a = 1; a >= -1; a--)
        {
            // Create a car with constant acceleration
            OTSModelInterface model = new Model();
            SimpleSimulator simulator =
                    new SimpleSimulator(new Time.Abs(0.0, SECOND), new Time.Rel(0.0, SECOND), new Time.Rel(3600.0, SECOND),
                            model);
            // Run the simulator clock to some non-zero value
            simulator.runUpTo(new Time.Abs(60, SECOND));
            while (simulator.isRunning())
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
            GTUType carType = GTUType.makeGTUType("car");
            LaneType laneType = new LaneType("CarLane");
            laneType.addCompatibility(carType);
            OTSNode fromNode = new OTSNode("Node A", new OTSPoint3D(0, 0, 0));
            OTSNode toNode = new OTSNode("Node B", new OTSPoint3D(1000, 0, 0));
            String linkName = "AB";
            Lane lane =
                    LaneFactory.makeMultiLane(linkName, fromNode, toNode, null, 1, laneType, new Speed(200, KM_PER_HOUR),
                            simulator, LongitudinalDirectionality.DIR_PLUS)[0];
            Length.Rel carPosition = new Length.Rel(100, METER);
            Set<DirectedLanePosition> carPositions = new LinkedHashSet<>(1);
            carPositions.add(new DirectedLanePosition(lane, carPosition, GTUDirectionality.DIR_PLUS));
            Speed carSpeed = new Speed(10, METER_PER_SECOND);
            Acceleration acceleration = new Acceleration(a, METER_PER_SECOND_2);
            FixedAccelerationModel fam = new FixedAccelerationModel(acceleration, new Time.Rel(10, SECOND));
            LaneChangeModel laneChangeModel = new FixedLaneChangeModel(null);
            Speed maximumVelocity = new Speed(200, KM_PER_HOUR);
            LaneBasedBehavioralCharacteristics drivingCharacteristics = new LaneBasedBehavioralCharacteristics(fam, laneChangeModel);
            LaneBasedStrategicalPlanner strategicalPlanner =
                    new LaneBasedStrategicalRoutePlanner(drivingCharacteristics, new LaneBasedCFLCTacticalPlanner());
            LaneBasedIndividualGTU car =
                    new LaneBasedIndividualGTU("Car" + this.idGenerator.nextId(), carType, carPositions, carSpeed,
                            new Length.Rel(4, METER), new Length.Rel(1.8, METER), maximumVelocity, simulator,
                            strategicalPlanner, new LanePerceptionFull(), this.network);
            // Let the simulator execute the move method of the car
            simulator.runUpTo(new Time.Abs(61, SECOND));
            while (simulator.isRunning())
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

            // System.out.println("acceleration is " + acceleration);
            // Check the results
            for (int timeStep = 1; timeStep < 100; timeStep++)
            {
                double deltaTime = 0.1 * timeStep;
                double distanceAtTime = carSpeed.getSI() * deltaTime + 0.5 * acceleration.getSI() * deltaTime * deltaTime;
                // System.out.println(String.format("time %.1fs, distance %.3fm", 60 + deltaTime, carPosition.getSI()
                // + distanceAtTime));
                // System.out.println("Expected differential distance " + distanceAtTime);
                /*- 
                 * TODO assertEquals("It should take " + deltaTime + " seconds to cover distance " + distanceAtTime, deltaTime,
                 * car.deltaTimeForDistance(new Length.Rel(distanceAtTime, METER)).getSI(), 0.0001);
                 * TODO assertEquals("Car should reach distance " + distanceAtTime + " at " + (deltaTime + 60), deltaTime + 60,
                 * car.timeAtDistance(new Length.Rel(distanceAtTime, METER)).getSI(), 0.0001);
                 */
            }
        }
    }

    /**
     * Executed as scheduled event.
     */
    public final void autoPauseSimulator()
    {
        // do nothing
    }

    /**
     * Create the Map that records in which lane a GTU is registered.
     * @param totalLongitudinalPosition Length.Rel; the front position of the GTU from the start of the chain of Links
     * @param gtuLength Length.Rel; the length of the GTU
     * @param links ArrayList&lt;CrossSectionLink&lt;?,?&gt;&gt;; the list of Links
     * @param fromLaneRank int; lowest rank of lanes that the GTU must be registered on (0-based)
     * @param uptoLaneRank int; highest rank of lanes that the GTU must be registered on (0-based)
     * @return the Set of the LanePositions that the GTU is registered on
     */
    private Set<DirectedLanePosition> buildPositionsSet(Length.Rel totalLongitudinalPosition, Length.Rel gtuLength,
            ArrayList<CrossSectionLink> links, int fromLaneRank, int uptoLaneRank)
    {
        Set<DirectedLanePosition> result = new LinkedHashSet<>(1);
        double cumulativeLength = 0;
        for (CrossSectionLink link : links)
        {
            double linkLength = link.getLength().getSI();
            double frontPositionInLink = totalLongitudinalPosition.getSI() - cumulativeLength + gtuLength.getSI();
            double rearPositionInLink = frontPositionInLink - gtuLength.getSI();
            // double linkEnd = cumulativeLength + linkLength;
            // System.out.println("cumulativeLength: " + cumulativeLength + ", linkEnd: " + linkEnd + ", frontpos: "
            // + frontPositionInLink + ", rearpos: " + rearPositionInLink);
            if (rearPositionInLink < linkLength && frontPositionInLink >= 0)
            {
                // Some part of the GTU is in this Link
                for (int laneRank = fromLaneRank; laneRank <= uptoLaneRank; laneRank++)
                {
                    Lane lane = getNthLane(link, laneRank);
                    if (null == lane)
                    {
                        fail("Error in test; canot find lane with rank " + laneRank);
                    }
                    try
                    {
                        result.add(new DirectedLanePosition(lane, new Length.Rel(rearPositionInLink, METER),
                                GTUDirectionality.DIR_PLUS));
                    }
                    catch (GTUException exception)
                    {
                        fail("Error in test; DirectedLanePosition for lane " + lane);
                    }
                }
            }
            cumulativeLength += linkLength;
        }
        return result;
    }

    /**
     * Find the Nth Lane on a Link.
     * @param link Link; the Link
     * @param rank int; the zero-based rank of the Lane to return
     * @return Lane
     */
    private Lane getNthLane(final CrossSectionLink link, int rank)
    {
        for (CrossSectionElement cse : link.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                if (0 == rank--)
                {
                    return (Lane) cse;
                }
            }
        }
        return null;
    }
}

/** */
class Model implements OTSModelInterface
{

    /** */
    private static final long serialVersionUID = 20150127L;

    /** {@inheritDoc} */
    @Override
    public void constructModel(
            SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator)
            throws SimRuntimeException
    {
        // Dummy
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()

    {
        return null;
    }

}

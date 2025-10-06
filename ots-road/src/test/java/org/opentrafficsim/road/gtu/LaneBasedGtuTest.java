package org.opentrafficsim.road.gtu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdSupplier;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.FixedCarFollowing;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.LaneType;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the LaneBasedGtu class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class LaneBasedGtuTest implements UNITS
{
    /** Id generator. */
    private IdSupplier idGenerator = new IdSupplier("id");

    /** */
    private LaneBasedGtuTest()
    {
        // do not instantiate test class
    }

    /**
     * Test if a Truck covering a specified range of lanes can <i>see</i> a Car covering a specified range of lanes. <br>
     * The network is a linear array of Nodes connected by 5-Lane Links. In the middle, the Nodes are very closely spaced. A
     * truck is positioned over those center Nodes ensuring it covers several of the short Lanes in succession.
     * @param truckFromLane lowest rank of lane range of the truck
     * @param truckUpToLane highest rank of lane range of the truck
     * @param carLanesCovered number of lanes that the car covers
     * @throws Exception when something goes wrong (should not happen)
     */
    private void leaderFollowerParallel(final int truckFromLane, final int truckUpToLane, final int carLanesCovered)
            throws Exception
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
        OtsSimulatorInterface simulator = new OtsSimulator("leaderFollowerParallel");
        RoadNetwork network = new RoadNetwork("leader follower parallel gtu test network", simulator);

        Model model = new Model(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model,
                HistoryManagerDevs.noHistory(simulator));
        GtuType carType = DefaultsNl.CAR;
        GtuType truckType = DefaultsNl.TRUCK;
        LaneType laneType = DefaultsRoadNl.TWO_WAY_LANE;
        // Create a series of Nodes (some closely bunched together)
        List<Node> nodes = new ArrayList<>();
        int[] linkBoundaries = {0, 25, 50, 100, 101, 102, 103, 104, 105, 150, 175, 200};
        for (int xPos : linkBoundaries)
        {
            nodes.add(new Node(network, "Node at " + xPos, new Point2d(xPos, 20), Direction.ZERO));
        }
        // Now we can build a series of Links with Lanes on them
        ArrayList<CrossSectionLink> links = new ArrayList<CrossSectionLink>();
        final int laneCount = 5;
        for (int i = 1; i < nodes.size(); i++)
        {
            Node fromNode = nodes.get(i - 1);
            Node toNode = nodes.get(i);
            String linkName = fromNode.getId() + "-" + toNode.getId();
            Lane[] lanes = LaneFactory.makeMultiLane(network, linkName, fromNode, toNode, null, laneCount, laneType,
                    new Speed(100, KM_PER_HOUR), simulator, DefaultsNl.VEHICLE);
            links.add(lanes[0].getLink());
        }
        // Create a long truck with its front (reference) one meter in the last link on the 3rd lane
        Length truckPosition = new Length(99.5, METER);
        Length truckLength = new Length(15, METER);

        Set<LanePosition> truckPositions = buildPositionsSet(truckPosition, truckLength, links, truckFromLane, truckUpToLane);
        Speed truckSpeed = new Speed(0, KM_PER_HOUR);
        Length truckWidth = new Length(2.5, METER);
        Speed maximumSpeed = new Speed(120, KM_PER_HOUR);
        Parameters parameters = DefaultTestParameters.create();

        LaneBasedGtu truck =
                new LaneBasedGtu("Truck", truckType, truckLength, truckWidth, maximumSpeed, truckLength.times(0.5), network);
        LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                new LmrsFactory.Factory().setCarFollowingModelFactory(new FixedCarFollowing()).build(null).create(truck),
                truck);
        truck.setParameters(parameters);
        truck.init(strategicalPlanner, getReferencePosition(truckPositions).getLocation(), truckSpeed);
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
                    for (LanePosition pos : truckPositions)
                    {
                        if (pos.lane().equals(lane))
                        {
                            truckPositionsOnLane = true;
                        }
                    }
                    if (truckPositionsOnLane)
                    {
                        assertTrue(lane.getGtuList().contains(truck), "Truck should be registered on Lane " + lane);
                        found++;
                    }
                    else
                    {
                        assertFalse(lane.getGtuList().contains(truck), "Truck should NOT be registered on Lane " + lane);
                    }
                    lanesChecked++;
                }
            }
        }
        // Make sure we tested them all
        assertEquals(laneCount * links.size(), lanesChecked,
                "lanesChecked should equals the number of Links times the number of lanes on each Link");
        assertEquals(truckPositions.size(), found, "Truck should be registered in " + truckPositions.size() + " lanes");
        Length forwardMaxDistance = truck.getParameters().getParameter(ParameterTypes.LOOKAHEAD);
        // TODO see how we can ask the vehicle to look this far ahead
        truck.getTacticalPlanner().getPerception().perceive();
        PerceivedObject leader = truck.getTacticalPlanner().getPerception().getPerceptionCategory(NeighborsPerception.class)
                .getLeaders(RelativeLane.CURRENT).first();
        assertTrue(forwardMaxDistance.getSI() >= leader.getDistance().si && leader.getDistance().si > 0,
                "With one vehicle in the network forward headway should return a value larger than zero, and smaller than maxDistance");
        assertEquals(null, leader.getId(), "With one vehicle in the network forward headwayGTU should return null");
        // TODO see how we can ask the vehicle to look this far behind
        Length reverseMaxDistance = truck.getParameters().getParameter(ParameterTypes.LOOKBACKOLD);
        PerceivedObject follower = truck.getTacticalPlanner().getPerception().getPerceptionCategory(NeighborsPerception.class)
                .getFollowers(RelativeLane.CURRENT).first();
        assertTrue(Math.abs(reverseMaxDistance.getSI()) >= Math.abs(follower.getDistance().si) && follower.getDistance().si < 0,
                "With one vehicle in the network reverse headway should return a value less than zero, and smaller than |maxDistance|");
        assertEquals(null, follower.getId(), "With one vehicle in the network reverse headwayGTU should return null");
        Length carLength = new Length(4, METER);
        Length carWidth = new Length(1.8, METER);
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
                Length carPosition = new Length(step, METER);
                Set<LanePosition> carPositions =
                        buildPositionsSet(carPosition, carLength, links, laneRank, laneRank + carLanesCovered - 1);
                parameters = DefaultTestParameters.create();

                LaneBasedGtu car =
                        new LaneBasedGtu("Car", carType, carLength, carWidth, maximumSpeed, carLength.times(0.5), network);
                strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                        new LmrsFactory.Factory().setCarFollowingModelFactory(new FixedCarFollowing()).build(null).create(car),
                        car);
                car.setParameters(parameters);
                car.init(strategicalPlanner, getReferencePosition(carPositions).getLocation(), carSpeed);
                // leader = truck.headway(forwardMaxDistance);
                // TODO see how we can ask the vehicle to look 'forwardMaxDistance' ahead
                leader = truck.getTacticalPlanner().getPerception().getPerceptionCategory(NeighborsPerception.class)
                        .getLeaders(RelativeLane.CURRENT).first();
                double actualHeadway = leader.getDistance().si;
                double expectedHeadway = laneRank + carLanesCovered - 1 < truckFromLane || laneRank > truckUpToLane
                        || step - truckPosition.getSI() - truckLength.getSI() <= 0 ? Double.MAX_VALUE
                                : step - truckLength.getSI() - truckPosition.getSI();
                // System.out.println("carLanesCovered " + laneRank + ".." + (laneRank + carLanesCovered - 1)
                // + " truckLanesCovered " + truckFromLane + ".." + truckUpToLane + " car pos " + step
                // + " laneRank " + laneRank + " expected headway " + expectedHeadway);
                // The next assert found a subtle bug (">" instead of ">=")
                assertEquals(expectedHeadway, actualHeadway, 0.1, "Forward headway should return " + expectedHeadway);
                String leaderGtuId = leader.getId();
                if (expectedHeadway == Double.MAX_VALUE)
                {
                    assertEquals(null, leaderGtuId, "Leader id should be null");
                }
                else
                {
                    assertEquals(car, leaderGtuId, "Leader id should be the car id");
                }
                // TODO follower = truck.headway(reverseMaxDistance);
                follower = truck.getTacticalPlanner().getPerception().getPerceptionCategory(NeighborsPerception.class)
                        .getFollowers(RelativeLane.CURRENT).first();
                double actualReverseHeadway = follower.getDistance().si;
                double expectedReverseHeadway = laneRank + carLanesCovered - 1 < truckFromLane || laneRank > truckUpToLane
                        || step + carLength.getSI() >= truckPosition.getSI() ? Double.MAX_VALUE
                                : truckPosition.getSI() - carLength.getSI() - step;
                assertEquals(expectedReverseHeadway, actualReverseHeadway, 0.1,
                        "Reverse headway should return " + expectedReverseHeadway);
                String followerGtuId = follower.getId();
                if (expectedReverseHeadway == Double.MAX_VALUE)
                {
                    assertEquals(null, followerGtuId, "Follower id should be null");
                }
                else
                {
                    assertEquals(car.getId(), followerGtuId, "Follower id should be the car id");
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
                    leader = truck.getTacticalPlanner().getPerception().getPerceptionCategory(NeighborsPerception.class)
                            .getLeaders(RelativeLane.CURRENT).first();
                    actualHeadway = leader.getDistance().si;
                    expectedHeadway = laneIndex < laneRank || laneIndex > laneRank + carLanesCovered - 1
                            || step - truckLength.getSI() - truckPosition.getSI() <= 0 ? Double.MAX_VALUE
                                    : step - truckLength.getSI() - truckPosition.getSI();
                    assertEquals(expectedHeadway, actualHeadway, 0.001,
                            "Headway on lane " + laneIndex + " should be " + expectedHeadway);
                    leaderGtuId = leader.getId();
                    if (laneIndex >= laneRank && laneIndex <= laneRank + carLanesCovered - 1
                            && step - truckLength.getSI() - truckPosition.getSI() > 0)
                    {
                        assertEquals(car.getId(), leaderGtuId, "Leader id should be the car id");
                    }
                    else
                    {
                        assertEquals(null, leaderGtuId, "Leader id should be null");
                    }
                    follower = truck.getTacticalPlanner().getPerception().getPerceptionCategory(NeighborsPerception.class)
                            .getFollowers(RelativeLane.CURRENT).first();
                    actualReverseHeadway = follower.getDistance().si;
                    expectedReverseHeadway = laneIndex < laneRank || laneIndex > laneRank + carLanesCovered - 1
                            || step + carLength.getSI() >= truckPosition.getSI() ? Double.MAX_VALUE
                                    : truckPosition.getSI() - carLength.getSI() - step;
                    assertEquals(expectedReverseHeadway, actualReverseHeadway, 0.001,
                            "Headway on lane " + laneIndex + " should be " + expectedReverseHeadway);
                    followerGtuId = follower.getId();
                    if (laneIndex >= laneRank && laneIndex <= laneRank + carLanesCovered - 1
                            && step + carLength.getSI() < truckPosition.getSI())
                    {
                        assertEquals(car, followerGtuId, "Follower id should be the car id");
                    }
                    else
                    {
                        assertEquals(null, followerGtuId, "Follower id should be null");
                    }
                }
                PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leftParallel = truck.getTacticalPlanner().getPerception()
                        .getPerceptionCategory(NeighborsPerception.class).getFollowers(RelativeLane.LEFT);
                int expectedLeftSize = laneRank + carLanesCovered - 1 < truckFromLane - 1 || laneRank >= truckUpToLane
                        || step + carLength.getSI() <= truckPosition.getSI()
                        || step > truckPosition.getSI() + truckLength.getSI() ? 0 : 1;
                // This one caught a complex bug
                assertEquals(expectedLeftSize, leftParallel.collect(() -> Integer.valueOf(0), (inter, gtu, dist) ->
                {
                    if (dist.lt0())
                    {
                        inter.setObject(inter.getObject() + 1);
                    }
                    else
                    {
                        inter.stop();
                    }
                    return inter;
                }, (inter) -> inter), "Left parallel set size should be " + expectedLeftSize);
                boolean foundCar = false;
                for (PerceivedObject hw : leftParallel)
                {
                    if (car.getId().equals(hw.getId()))
                    {
                        foundCar = true;
                        break;
                    }
                }
                assertTrue(foundCar, "car was not found in rightParallel");
                PerceptionCollectable<PerceivedGtu, LaneBasedGtu> rightParallel = truck.getTacticalPlanner().getPerception()
                        .getPerceptionCategory(NeighborsPerception.class).getFollowers(RelativeLane.RIGHT);
                int expectedRightSize = laneRank + carLanesCovered - 1 <= truckFromLane || laneRank > truckUpToLane + 1
                        || step + carLength.getSI() < truckPosition.getSI()
                        || step > truckPosition.getSI() + truckLength.getSI() ? 0 : 1;
                assertEquals(expectedRightSize, rightParallel.collect(() -> Integer.valueOf(0), (inter, gtu, dist) ->
                {
                    if (dist.lt0())
                    {
                        inter.setObject(inter.getObject() + 1);
                    }
                    else
                    {
                        inter.stop();
                    }
                    return inter;
                }, (inter) -> inter), "Right parallel set size should be " + expectedRightSize);
                foundCar = false;
                for (PerceivedObject hw : rightParallel)
                {
                    if (car.getId().equals(hw.getId()))
                    {
                        foundCar = true;
                        break;
                    }
                }
                assertTrue(foundCar, "car was not found in rightParallel");
                for (LanePosition pos : carPositions)
                {
                    pos.lane().removeGtu(car, true, pos.position());
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
        // leaderFollowerParallel(2, 2, 1);
        // leaderFollowerParallel(2, 3, 1);
        // leaderFollowerParallel(2, 2, 2);
        // leaderFollowerParallel(2, 3, 2);
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
            OtsSimulatorInterface simulator = new OtsSimulator("timeAtDistanceTest");
            RoadNetwork network = new RoadNetwork("test", simulator);
            // Create a car with constant acceleration
            Model model = new Model(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model,
                    HistoryManagerDevs.noHistory(simulator));
            // Run the simulator clock to some non-zero value
            simulator.runUpTo(Duration.instantiateSI(60.0));
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
            GtuType carType = DefaultsNl.CAR;
            LaneType laneType = DefaultsRoadNl.TWO_WAY_LANE;
            Node fromNode = new Node(network, "Node A", new Point2d(0, 0), Direction.ZERO);
            Node toNode = new Node(network, "Node B", new Point2d(1000, 0), Direction.ZERO);
            String linkName = "AB";
            Lane lane = LaneFactory.makeMultiLane(network, linkName, fromNode, toNode, null, 1, laneType,
                    new Speed(200, KM_PER_HOUR), simulator, DefaultsNl.VEHICLE)[0];
            Length carPosition = new Length(100, METER);
            Set<LanePosition> carPositions = new LinkedHashSet<>(1);
            carPositions.add(new LanePosition(lane, carPosition));
            Speed carSpeed = new Speed(10, METER_PER_SECOND);
            Acceleration acceleration = new Acceleration(a, METER_PER_SECOND_2);
            Speed maximumSpeed = new Speed(200, KM_PER_HOUR);
            Parameters parameters = DefaultTestParameters.create();

            LaneBasedGtu car = new LaneBasedGtu("Car" + this.idGenerator.get(), carType, new Length(4, METER),
                    new Length(1.8, METER), maximumSpeed, Length.instantiateSI(2.0), network);
            LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(new LmrsFactory.Factory()
                    .setCarFollowingModelFactory(new FixedCarFollowing(acceleration)).build(null).create(car), car);
            car.setParameters(parameters);
            car.init(strategicalPlanner, getReferencePosition(carPositions).getLocation(), carSpeed);
            // Let the simulator execute the move method of the car
            simulator.runUpTo(Duration.instantiateSI(61.0));
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
                assertEquals("It should take " + deltaTime + " seconds to cover distance " + distanceAtTime, deltaTime, car
                        .deltaTimeForDistance(new Length(distanceAtTime, METER)).getSI(), 0.0001);
                assertEquals("Car should reach distance " + distanceAtTime + " at " + (deltaTime + 60), deltaTime + 60, car
                        .timeAtDistance(new Length(distanceAtTime, METER)).getSI(), 0.0001);
                 */
            }
        }
    }

    /**
     * Executed as scheduled event.
     */
    public void autoPauseSimulator()
    {
        // do nothing
    }

    /**
     * Create the Map that records in which lane a GTU is registered.
     * @param totalLongitudinalPosition the front position of the GTU from the start of the chain of Links
     * @param gtuLength the length of the GTU
     * @param links the list of Links
     * @param fromLaneRank lowest rank of lanes that the GTU must be registered on (0-based)
     * @param uptoLaneRank highest rank of lanes that the GTU must be registered on (0-based)
     * @return the Set of the LanePositions that the GTU is registered on
     */
    private Set<LanePosition> buildPositionsSet(final Length totalLongitudinalPosition, final Length gtuLength,
            final ArrayList<CrossSectionLink> links, final int fromLaneRank, final int uptoLaneRank)
    {
        Set<LanePosition> result = new LinkedHashSet<>(1);
        double cumulativeLength = 0;
        for (CrossSectionLink link : links)
        {
            double linkLength = link.getLength().getSI();
            double frontPositionInLink = totalLongitudinalPosition.getSI() - cumulativeLength + gtuLength.getSI();
            double rearPositionInLink = frontPositionInLink - gtuLength.getSI();
            double midPositionInLink = frontPositionInLink - gtuLength.getSI() / 2.0;
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
                    result.add(new LanePosition(lane, new Length(midPositionInLink, METER)));
                }
            }
            cumulativeLength += linkLength;
        }
        return result;
    }

    /**
     * Returns the reference position from the set of positions.
     * @param positions positions.
     * @return reference position.
     */
    private LanePosition getReferencePosition(final Set<LanePosition> positions)
    {
        for (LanePosition lanePosition : positions)
        {
            if (lanePosition.position().gt0() && lanePosition.position().le(lanePosition.lane().getLength()))
            {
                return lanePosition;
            }
        }
        throw new NoSuchElementException("Reference point is not on any of the given lanes.");
    }

    /**
     * Find the Nth Lane on a Link.
     * @param link the Link
     * @param rank the zero-based rank of the Lane to return
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

    /** The helper model. */
    public static class Model extends AbstractOtsModel
    {
        /**
         * Constructor.
         * @param simulator the simulator to use
         */
        public Model(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** */
        private static final long serialVersionUID = 20141027L;

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

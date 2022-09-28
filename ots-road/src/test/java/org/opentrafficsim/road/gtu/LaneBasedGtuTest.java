package org.opentrafficsim.road.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGtu;
import org.opentrafficsim.road.gtu.lane.perception.categories.DefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.GtuFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.FixedLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the LaneBasedGtu class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LaneBasedGtuTest implements UNITS
{
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
        OTSSimulatorInterface simulator = new OTSSimulator("leaderFollowerParallel");
        OTSRoadNetwork network = new OTSRoadNetwork("leader follower parallel gtu test network", true, simulator);

        Model model = new Model(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model);
        GtuType carType = network.getGtuType(GtuType.DEFAULTS.CAR);
        GtuType truckType = network.getGtuType(GtuType.DEFAULTS.TRUCK);
        LaneType laneType = network.getLaneType(LaneType.DEFAULTS.TWO_WAY_LANE);
        // Create a series of Nodes (some closely bunched together)
        List<OTSRoadNode> nodes = new ArrayList<>();
        int[] linkBoundaries = {0, 25, 50, 100, 101, 102, 103, 104, 105, 150, 175, 200};
        for (int xPos : linkBoundaries)
        {
            nodes.add(new OTSRoadNode(network, "Node at " + xPos, new OTSPoint3D(xPos, 20, 0), Direction.ZERO));
        }
        // Now we can build a series of Links with Lanes on them
        ArrayList<CrossSectionLink> links = new ArrayList<CrossSectionLink>();
        final int laneCount = 5;
        for (int i = 1; i < nodes.size(); i++)
        {
            OTSRoadNode fromNode = nodes.get(i - 1);
            OTSRoadNode toNode = nodes.get(i);
            String linkName = fromNode.getId() + "-" + toNode.getId();
            Lane[] lanes = LaneFactory.makeMultiLane(network, linkName, fromNode, toNode, null, laneCount, laneType,
                    new Speed(100, KM_PER_HOUR), simulator);
            links.add(lanes[0].getParentLink());
        }
        // Create a long truck with its front (reference) one meter in the last link on the 3rd lane
        Length truckPosition = new Length(99.5, METER);
        Length truckLength = new Length(15, METER);

        Set<DirectedLanePosition> truckPositions =
                buildPositionsSet(truckPosition, truckLength, links, truckFromLane, truckUpToLane);
        Speed truckSpeed = new Speed(0, KM_PER_HOUR);
        Length truckWidth = new Length(2.5, METER);
        LaneChangeModel laneChangeModel = new FixedLaneChangeModel(null);
        Speed maximumSpeed = new Speed(120, KM_PER_HOUR);
        GtuFollowingModelOld gtuFollowingModel = new IDMPlusOld();
        Parameters parameters = DefaultTestParameters.create();

        LaneBasedIndividualGtu truck = new LaneBasedIndividualGtu("Truck", truckType, truckLength, truckWidth, maximumSpeed,
                truckLength.times(0.5), simulator, network);
        LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                new LaneBasedCFLCTacticalPlanner(gtuFollowingModel, laneChangeModel, truck), truck);
        truck.setParameters(parameters);
        truck.init(strategicalPlanner, truckPositions, truckSpeed);
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
        Length forwardMaxDistance = truck.getParameters().getParameter(ParameterTypes.LOOKAHEAD);
        // TODO see how we can ask the vehicle to look this far ahead
        truck.getTacticalPlanner().getPerception().perceive();
        Headway leader = truck.getTacticalPlanner().getPerception().getPerceptionCategory(DefaultSimplePerception.class)
                .getForwardHeadwayGtu();
        assertTrue(
                "With one vehicle in the network forward headway should return a value larger than zero, and smaller than maxDistance",
                forwardMaxDistance.getSI() >= leader.getDistance().si && leader.getDistance().si > 0);
        assertEquals("With one vehicle in the network forward headwayGTU should return null", null, leader.getId());
        // TODO see how we can ask the vehicle to look this far behind
        Length reverseMaxDistance = truck.getParameters().getParameter(ParameterTypes.LOOKBACKOLD);
        Headway follower = truck.getTacticalPlanner().getPerception().getPerceptionCategory(DefaultSimplePerception.class)
                .getBackwardHeadway();
        assertTrue(
                "With one vehicle in the network reverse headway should return a value less than zero, and smaller than |maxDistance|",
                Math.abs(reverseMaxDistance.getSI()) >= Math.abs(follower.getDistance().si) && follower.getDistance().si < 0);
        assertEquals("With one vehicle in the network reverse headwayGTU should return null", null, follower.getId());
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
                Set<DirectedLanePosition> carPositions =
                        buildPositionsSet(carPosition, carLength, links, laneRank, laneRank + carLanesCovered - 1);
                parameters = DefaultTestParameters.create();

                LaneBasedIndividualGtu car = new LaneBasedIndividualGtu("Car", carType, carLength, carWidth, maximumSpeed,
                        carLength.times(0.5), simulator, network);
                strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                        new LaneBasedCFLCTacticalPlanner(gtuFollowingModel, laneChangeModel, car), car);
                car.setParameters(parameters);
                car.init(strategicalPlanner, carPositions, carSpeed);
                // leader = truck.headway(forwardMaxDistance);
                // TODO see how we can ask the vehicle to look 'forwardMaxDistance' ahead
                leader = truck.getTacticalPlanner().getPerception().getPerceptionCategory(DefaultSimplePerception.class)
                        .getForwardHeadwayGtu();
                double actualHeadway = leader.getDistance().si;
                double expectedHeadway = laneRank + carLanesCovered - 1 < truckFromLane || laneRank > truckUpToLane
                        || step - truckPosition.getSI() - truckLength.getSI() <= 0 ? Double.MAX_VALUE
                                : step - truckLength.getSI() - truckPosition.getSI();
                // System.out.println("carLanesCovered " + laneRank + ".." + (laneRank + carLanesCovered - 1)
                // + " truckLanesCovered " + truckFromLane + ".." + truckUpToLane + " car pos " + step
                // + " laneRank " + laneRank + " expected headway " + expectedHeadway);
                // The next assert found a subtle bug (">" instead of ">=")
                assertEquals("Forward headway should return " + expectedHeadway, expectedHeadway, actualHeadway, 0.1);
                String leaderGtuId = leader.getId();
                if (expectedHeadway == Double.MAX_VALUE)
                {
                    assertEquals("Leader id should be null", null, leaderGtuId);
                }
                else
                {
                    assertEquals("Leader id should be the car id", car, leaderGtuId);
                }
                // TODO follower = truck.headway(reverseMaxDistance);
                follower = truck.getTacticalPlanner().getPerception().getPerceptionCategory(DefaultSimplePerception.class)
                        .getBackwardHeadway();
                double actualReverseHeadway = follower.getDistance().si;
                double expectedReverseHeadway = laneRank + carLanesCovered - 1 < truckFromLane || laneRank > truckUpToLane
                        || step + carLength.getSI() >= truckPosition.getSI() ? Double.MAX_VALUE
                                : truckPosition.getSI() - carLength.getSI() - step;
                assertEquals("Reverse headway should return " + expectedReverseHeadway, expectedReverseHeadway,
                        actualReverseHeadway, 0.1);
                String followerGtuId = follower.getId();
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
                    leader = truck.getTacticalPlanner().getPerception().getPerceptionCategory(DefaultSimplePerception.class)
                            .getForwardHeadwayGtu();
                    actualHeadway = leader.getDistance().si;
                    expectedHeadway = laneIndex < laneRank || laneIndex > laneRank + carLanesCovered - 1
                            || step - truckLength.getSI() - truckPosition.getSI() <= 0 ? Double.MAX_VALUE
                                    : step - truckLength.getSI() - truckPosition.getSI();
                    assertEquals("Headway on lane " + laneIndex + " should be " + expectedHeadway, expectedHeadway,
                            actualHeadway, 0.001);
                    leaderGtuId = leader.getId();
                    if (laneIndex >= laneRank && laneIndex <= laneRank + carLanesCovered - 1
                            && step - truckLength.getSI() - truckPosition.getSI() > 0)
                    {
                        assertEquals("Leader id should be the car id", car.getId(), leaderGtuId);
                    }
                    else
                    {
                        assertEquals("Leader id should be null", null, leaderGtuId);
                    }
                    follower = truck.getTacticalPlanner().getPerception().getPerceptionCategory(DefaultSimplePerception.class)
                            .getBackwardHeadway();
                    actualReverseHeadway = follower.getDistance().si;
                    expectedReverseHeadway = laneIndex < laneRank || laneIndex > laneRank + carLanesCovered - 1
                            || step + carLength.getSI() >= truckPosition.getSI() ? Double.MAX_VALUE
                                    : truckPosition.getSI() - carLength.getSI() - step;
                    assertEquals("Headway on lane " + laneIndex + " should be " + expectedReverseHeadway,
                            expectedReverseHeadway, actualReverseHeadway, 0.001);
                    followerGtuId = follower.getId();
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
                Collection<Headway> leftParallel = truck.getTacticalPlanner().getPerception()
                        .getPerceptionCategory(DefaultSimplePerception.class).getParallelHeadwaysLeft();
                int expectedLeftSize = laneRank + carLanesCovered - 1 < truckFromLane - 1 || laneRank >= truckUpToLane
                        || step + carLength.getSI() <= truckPosition.getSI()
                        || step > truckPosition.getSI() + truckLength.getSI() ? 0 : 1;
                // This one caught a complex bug
                assertEquals("Left parallel set size should be " + expectedLeftSize, expectedLeftSize, leftParallel.size());
                boolean foundCar = false;
                for (Headway hw : leftParallel)
                {
                    if (car.getId().equals(hw.getId()))
                    {
                        foundCar = true;
                        break;
                    }
                }
                assertTrue("car was not found in rightParallel", foundCar);
                Collection<Headway> rightParallel = truck.getTacticalPlanner().getPerception()
                        .getPerceptionCategory(DefaultSimplePerception.class).getParallelHeadwaysRight();
                int expectedRightSize = laneRank + carLanesCovered - 1 <= truckFromLane || laneRank > truckUpToLane + 1
                        || step + carLength.getSI() < truckPosition.getSI()
                        || step > truckPosition.getSI() + truckLength.getSI() ? 0 : 1;
                assertEquals("Right parallel set size should be " + expectedRightSize, expectedRightSize, rightParallel.size());
                foundCar = false;
                for (Headway hw : rightParallel)
                {
                    if (car.getId().equals(hw.getId()))
                    {
                        foundCar = true;
                        break;
                    }
                }
                assertTrue("car was not found in rightParallel", foundCar);
                for (DirectedLanePosition pos : carPositions)
                {
                    pos.getLane().removeGTU(car, true, pos.getPosition());
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
    public final void timeAtDistanceTest() throws Exception
    {
        for (int a = 1; a >= -1; a--)
        {
            OTSSimulatorInterface simulator = new OTSSimulator("timeAtDistanceTest");
            OTSRoadNetwork network = new OTSRoadNetwork("test", true, simulator);
            // Create a car with constant acceleration
            Model model = new Model(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model);
            // Run the simulator clock to some non-zero value
            simulator.runUpTo(new Time(60, TimeUnit.BASE_SECOND));
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
            GtuType carType = network.getGtuType(GtuType.DEFAULTS.CAR);
            LaneType laneType = network.getLaneType(LaneType.DEFAULTS.TWO_WAY_LANE);
            OTSRoadNode fromNode = new OTSRoadNode(network, "Node A", new OTSPoint3D(0, 0, 0), Direction.ZERO);
            OTSRoadNode toNode = new OTSRoadNode(network, "Node B", new OTSPoint3D(1000, 0, 0), Direction.ZERO);
            String linkName = "AB";
            Lane lane = LaneFactory.makeMultiLane(network, linkName, fromNode, toNode, null, 1, laneType,
                    new Speed(200, KM_PER_HOUR), simulator)[0];
            Length carPosition = new Length(100, METER);
            Set<DirectedLanePosition> carPositions = new LinkedHashSet<>(1);
            carPositions.add(new DirectedLanePosition(lane, carPosition, GTUDirectionality.DIR_PLUS));
            Speed carSpeed = new Speed(10, METER_PER_SECOND);
            Acceleration acceleration = new Acceleration(a, METER_PER_SECOND_2);
            FixedAccelerationModel fam = new FixedAccelerationModel(acceleration, new Duration(10, SECOND));
            LaneChangeModel laneChangeModel = new FixedLaneChangeModel(null);
            Speed maximumSpeed = new Speed(200, KM_PER_HOUR);
            Parameters parameters = DefaultTestParameters.create();

            LaneBasedIndividualGtu car = new LaneBasedIndividualGtu("Car" + this.idGenerator.nextId(), carType,
                    new Length(4, METER), new Length(1.8, METER), maximumSpeed, Length.instantiateSI(2.0), simulator, network);
            LaneBasedStrategicalPlanner strategicalPlanner =
                    new LaneBasedStrategicalRoutePlanner(new LaneBasedCFLCTacticalPlanner(fam, laneChangeModel, car), car);
            car.setParameters(parameters);
            car.init(strategicalPlanner, carPositions, carSpeed);
            // Let the simulator execute the move method of the car
            simulator.runUpTo(new Time(61, TimeUnit.BASE_SECOND));
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
    public final void autoPauseSimulator()
    {
        // do nothing
    }

    /**
     * Create the Map that records in which lane a GTU is registered.
     * @param totalLongitudinalPosition Length; the front position of the GTU from the start of the chain of Links
     * @param gtuLength Length; the length of the GTU
     * @param links ArrayList&lt;CrossSectionLink&lt;?,?&gt;&gt;; the list of Links
     * @param fromLaneRank int; lowest rank of lanes that the GTU must be registered on (0-based)
     * @param uptoLaneRank int; highest rank of lanes that the GTU must be registered on (0-based)
     * @return the Set of the LanePositions that the GTU is registered on
     */
    private Set<DirectedLanePosition> buildPositionsSet(final Length totalLongitudinalPosition, final Length gtuLength,
            final ArrayList<CrossSectionLink> links, final int fromLaneRank, final int uptoLaneRank)
    {
        Set<DirectedLanePosition> result = new LinkedHashSet<>(1);
        double cumulativeLength = 0;
        for (CrossSectionLink link : links)
        {
            double linkLength = link.getLength().getSI();
            double frontPositionInLink = totalLongitudinalPosition.getSI() - cumulativeLength + gtuLength.getSI();
            double rearPositionInLink = frontPositionInLink - gtuLength.getSI();
            double linkEnd = cumulativeLength + linkLength;
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
                        result.add(new DirectedLanePosition(lane, new Length(rearPositionInLink, METER),
                                GTUDirectionality.DIR_PLUS));
                    }
                    catch (GtuException exception)
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

    /** The helper model. */
    public static class Model extends AbstractOTSModel
    {
        /**
         * @param simulator the simulator to use
         */
        public Model(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** */
        private static final long serialVersionUID = 20141027L;

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public final OTSRoadNetwork getNetwork()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "LaneBasedGtuTest.Model";
        }
    }
}

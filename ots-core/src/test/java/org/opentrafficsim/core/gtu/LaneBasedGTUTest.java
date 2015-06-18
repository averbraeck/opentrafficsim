package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.junit.Test;
import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.following.FixedAccelerationModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.gtu.lane.changing.FixedLaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.SimpleSimulator;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Test the LaneBasedGTU class.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 27 jan. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedGTUTest
{

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
            new SimpleSimulator(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(0.0,
                TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0, TimeUnit.SECOND), model);
        GTUType<String> carType = GTUType.makeGTUType("car");
        GTUType<String> truckType = GTUType.makeGTUType("truck");
        LaneType<String> laneType = new LaneType<String>("CarLane");
        laneType.addPermeability(carType);
        laneType.addPermeability(truckType);
        // Create a series of Nodes (some closely bunched together)
        ArrayList<NodeGeotools.STR> nodes = new ArrayList<NodeGeotools.STR>();
        int[] linkBoundaries = {0, 25, 50, 100, 101, 102, 103, 104, 105, 150, 175, 200};
        for (int xPos : linkBoundaries)
        {
            nodes.add(new NodeGeotools.STR("Node at " + xPos, new Coordinate(xPos, 20, 0)));
        }
        // Now we can build a series of Links with Lanes on them
        ArrayList<CrossSectionLink<?, ?>> links = new ArrayList<CrossSectionLink<?, ?>>();
        final int laneCount = 5;
        for (int i = 1; i < nodes.size(); i++)
        {
            NodeGeotools.STR fromNode = nodes.get(i - 1);
            NodeGeotools.STR toNode = nodes.get(i);
            String linkName = fromNode.getId() + "-" + toNode.getId();
            Lane[] lanes =
                LaneFactory.makeMultiLane(linkName, fromNode, toNode, null, laneCount, laneType,
                    new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR), simulator);
            links.add(lanes[0].getParentLink());
        }
        // Create a long truck with its front (reference) one meter in the last link on the 3rd lane
        DoubleScalar.Rel<LengthUnit> truckPosition = new DoubleScalar.Rel<LengthUnit>(99.5, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> truckLength = new DoubleScalar.Rel<LengthUnit>(15, LengthUnit.METER);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> truckPositions =
            buildPositionsMap(truckPosition, truckLength, links, truckFromLane, truckUpToLane);
        DoubleScalar.Abs<SpeedUnit> truckSpeed = new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR);
        DoubleScalar.Rel<LengthUnit> truckWidth = new DoubleScalar.Rel<LengthUnit>(2.5, LengthUnit.METER);
        LaneChangeModel laneChangeModel = new FixedLaneChangeModel(null);
        DoubleScalar.Abs<SpeedUnit> maximumVelocity = new DoubleScalar.Abs<SpeedUnit>(120, SpeedUnit.KM_PER_HOUR);
        try
        {
            new LaneBasedIndividualCar<String>("Truck", truckType, null /* GTU following model */, laneChangeModel,
                truckPositions, truckSpeed, truckLength, truckWidth, maximumVelocity,
                new Route(new ArrayList<Node<?, ?>>()), simulator);
            fail("null GTUFollowingModel should have thrown a GTUException");
        }
        catch (GTUException e)
        {
            // Ignore expected exception
        }
        GTUFollowingModel gtuFollowingModel = new IDMPlus();
        try
        {
            new LaneBasedIndividualCar<String>("Truck", truckType, gtuFollowingModel, laneChangeModel, truckPositions,
                truckSpeed, truckLength, truckWidth, maximumVelocity, null, simulator);
            fail("null Route should have thrown a GTUException");
        }
        catch (GTUException e)
        {
            // Ignore expected exception
        }
        LaneBasedIndividualCar<String> truck =
            new LaneBasedIndividualCar<String>("Truck", truckType, gtuFollowingModel, laneChangeModel, truckPositions,
                truckSpeed, truckLength, truckWidth, maximumVelocity, new Route(new ArrayList<Node<?, ?>>()), simulator);
        // Verify that the truck is registered on the correct Lanes
        int lanesChecked = 0;
        int found = 0;
        for (CrossSectionLink<?, ?> link : links)
        {
            for (CrossSectionElement cse : link.getCrossSectionElementList())
            {
                if (cse instanceof Lane)
                {
                    Lane lane = (Lane) cse;
                    if (truckPositions.containsKey(lane))
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
        assertEquals("lanesChecked should equals the number of Links times the number of lanes on each Link", laneCount
            * links.size(), lanesChecked);
        assertEquals("Truck should be registered in " + truckPositions.keySet().size() + " lanes", truckPositions.keySet()
            .size(), found);
        DoubleScalar.Rel<LengthUnit> forwardMaxDistance = new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER);
        HeadwayGTU leader = truck.headway(forwardMaxDistance);
        assertTrue("With one vehicle in the network forward headway should return a value larger than maxDistance",
            forwardMaxDistance.getSI() < leader.getDistanceSI());
        assertEquals("With one vehicle in the network forward headwayGTU should return null", null, leader.getOtherGTU());
        DoubleScalar.Rel<LengthUnit> reverseMaxDistance = new DoubleScalar.Rel<LengthUnit>(-9999, LengthUnit.METER);
        HeadwayGTU follower = truck.headway(reverseMaxDistance);
        assertTrue("With one vehicle in the network reverse headway should return a value larger than maxDistance", Math
            .abs(reverseMaxDistance.getSI()) < follower.getDistanceSI());
        assertEquals("With one vehicle in the network reverse headwayGTU should return null", null, follower.getOtherGTU());
        DoubleScalar.Rel<LengthUnit> carLength = new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> carWidth = new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> carSpeed = new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR);
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
                DoubleScalar.Rel<LengthUnit> carPosition = new DoubleScalar.Rel<LengthUnit>(step, LengthUnit.METER);
                Map<Lane, DoubleScalar.Rel<LengthUnit>> carPositions =
                    buildPositionsMap(carPosition, carLength, links, laneRank, laneRank + carLanesCovered - 1);
                LaneBasedIndividualCar<String> car =
                    new LaneBasedIndividualCar<String>("Car", carType, gtuFollowingModel, laneChangeModel, carPositions,
                        carSpeed, carLength, carWidth, maximumVelocity, new Route(new ArrayList<Node<?, ?>>()), simulator);
                leader = truck.headway(forwardMaxDistance);
                double actualHeadway = leader.getDistanceSI();
                double expectedHeadway =
                    laneRank + carLanesCovered - 1 < truckFromLane || laneRank > truckUpToLane
                        || step - truckPosition.getSI() - truckLength.getSI() <= 0 ? Double.MAX_VALUE : step
                        - truckLength.getSI() - truckPosition.getSI();
                // System.out.println("carLanesCovered " + laneRank + ".." + (laneRank + carLanesCovered - 1)
                // + " truckLanesCovered " + truckFromLane + ".." + truckUpToLane + " car pos " + step
                // + " laneRank " + laneRank + " expected headway " + expectedHeadway);
                // The next assert found a subtle bug (">" in stead of ">=")
                assertEquals("Forward headway should return " + expectedHeadway, expectedHeadway, actualHeadway, 0.1);
                LaneBasedGTU<?> leaderGTU = leader.getOtherGTU();
                if (expectedHeadway == Double.MAX_VALUE)
                {
                    assertEquals("Leader should be null", null, leaderGTU);
                }
                else
                {
                    assertEquals("Leader should be the car", car, leaderGTU);
                }
                follower = truck.headway(reverseMaxDistance);
                double actualReverseHeadway = follower.getDistanceSI();
                double expectedReverseHeadway =
                    laneRank + carLanesCovered - 1 < truckFromLane || laneRank > truckUpToLane
                        || step + carLength.getSI() >= truckPosition.getSI() ? Double.MAX_VALUE : truckPosition.getSI()
                        - carLength.getSI() - step;
                assertEquals("Reverse headway should return " + expectedReverseHeadway, expectedReverseHeadway,
                    actualReverseHeadway, 0.1);
                LaneBasedGTU<?> followerGTU = follower.getOtherGTU();
                if (expectedReverseHeadway == Double.MAX_VALUE)
                {
                    assertEquals("Follower should be null", null, followerGTU);
                }
                else
                {
                    assertEquals("Follower should be the car", car, followerGTU);
                }
                for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
                {
                    Lane l = null;
                    double cumulativeDistance = 0;
                    for (CrossSectionLink<?, ?> csl : links)
                    {
                        cumulativeDistance += csl.getLength().getSI();
                        if (cumulativeDistance >= truckPosition.getSI())
                        {
                            l = getNthLane(csl, laneIndex);
                            break;
                        }
                    }
                    leader = truck.headway(l, forwardMaxDistance);
                    actualHeadway = leader.getDistanceSI();
                    expectedHeadway =
                        laneIndex < laneRank || laneIndex > laneRank + carLanesCovered - 1
                            || step - truckLength.getSI() - truckPosition.getSI() <= 0 ? Double.MAX_VALUE : step
                            - truckLength.getSI() - truckPosition.getSI();
                    assertEquals("Headway on lane " + laneIndex + " should be " + expectedHeadway, expectedHeadway,
                        actualHeadway, 0.001);
                    leaderGTU = leader.getOtherGTU();
                    if (laneIndex >= laneRank && laneIndex <= laneRank + carLanesCovered - 1
                        && step - truckLength.getSI() - truckPosition.getSI() > 0)
                    {
                        assertEquals("Leader should be the car", car, leaderGTU);
                    }
                    else
                    {
                        assertEquals("Leader should be null", null, leaderGTU);
                    }
                    follower = truck.headway(l, reverseMaxDistance);
                    actualReverseHeadway = follower.getDistanceSI();
                    expectedReverseHeadway =
                        laneIndex < laneRank || laneIndex > laneRank + carLanesCovered - 1
                            || step + carLength.getSI() >= truckPosition.getSI() ? Double.MAX_VALUE : truckPosition.getSI()
                            - carLength.getSI() - step;
                    assertEquals("Headway on lane " + laneIndex + " should be " + expectedReverseHeadway,
                        expectedReverseHeadway, actualReverseHeadway, 0.001);
                    followerGTU = follower.getOtherGTU();
                    if (laneIndex >= laneRank && laneIndex <= laneRank + carLanesCovered - 1
                        && step + carLength.getSI() < truckPosition.getSI())
                    {
                        assertEquals("Follower should be the car", car, followerGTU);
                    }
                    else
                    {
                        assertEquals("Follower should be null", null, followerGTU);
                    }
                }
                Set<LaneBasedGTU<?>> leftParallel =
                    truck.parallel(LateralDirectionality.LEFT, simulator.getSimulatorTime().get());
                int expectedLeftSize =
                    laneRank + carLanesCovered - 1 < truckFromLane - 1 || laneRank >= truckUpToLane
                        || step + carLength.getSI() <= truckPosition.getSI()
                        || step > truckPosition.getSI() + truckLength.getSI() ? 0 : 1;
                assertEquals("Left parallel set size should be " + expectedLeftSize, expectedLeftSize, leftParallel.size()); // This
                                                                                                                             // one
                                                                                                                             // caught
                                                                                                                             // a
                                                                                                                             // complex
                                                                                                                             // bug
                if (leftParallel.size() > 0)
                {
                    assertTrue("Parallel GTU should be the car", leftParallel.contains(car));
                }
                Set<LaneBasedGTU<?>> rightParallel =
                    truck.parallel(LateralDirectionality.RIGHT, simulator.getSimulatorTime().get());
                int expectedRightSize =
                    laneRank + carLanesCovered - 1 <= truckFromLane || laneRank > truckUpToLane + 1
                        || step + carLength.getSI() < truckPosition.getSI()
                        || step > truckPosition.getSI() + truckLength.getSI() ? 0 : 1;
                assertEquals("Right parallel set size should be " + expectedRightSize, expectedRightSize, rightParallel
                    .size());
                if (rightParallel.size() > 0)
                {
                    assertTrue("Parallel GTU should be the car", rightParallel.contains(car));
                }
                for (Lane lane : carPositions.keySet())
                {
                    lane.removeGTU(car);
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
        leaderFollowerParallel(2, 2, 1);
        leaderFollowerParallel(2, 3, 1);
        leaderFollowerParallel(2, 2, 2);
        leaderFollowerParallel(2, 3, 2);
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
                new SimpleSimulator(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(
                    0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0, TimeUnit.SECOND), model);
            // Run the simulator clock to some non-zero value
            simulator.runUpTo(new DoubleScalar.Abs<TimeUnit>(60, TimeUnit.SECOND));
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
            GTUType<String> carType = GTUType.makeGTUType("car");
            LaneType<String> laneType = new LaneType<String>("CarLane");
            laneType.addPermeability(carType);
            NodeGeotools.STR fromNode = new NodeGeotools.STR("Node A", new Coordinate(0, 0, 0));
            NodeGeotools.STR toNode = new NodeGeotools.STR("Node B", new Coordinate(1000, 0, 0));
            String linkName = "AB";
            Lane lane =
                LaneFactory.makeMultiLane(linkName, fromNode, toNode, null, 1, laneType, new DoubleScalar.Abs<SpeedUnit>(
                    100, SpeedUnit.KM_PER_HOUR), simulator)[0];
            DoubleScalar.Rel<LengthUnit> carPosition = new DoubleScalar.Rel<LengthUnit>(100, LengthUnit.METER);
            Map<Lane, DoubleScalar.Rel<LengthUnit>> carPositions = new LinkedHashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
            carPositions.put(lane, carPosition);
            DoubleScalar.Abs<SpeedUnit> carSpeed = new DoubleScalar.Abs<SpeedUnit>(10, SpeedUnit.METER_PER_SECOND);
            DoubleScalar.Abs<AccelerationUnit> acceleration =
                new DoubleScalar.Abs<AccelerationUnit>(a, AccelerationUnit.METER_PER_SECOND_2);
            FixedAccelerationModel fam =
                new FixedAccelerationModel(acceleration, new DoubleScalar.Rel<TimeUnit>(10, TimeUnit.SECOND));
            LaneChangeModel laneChangeModel = new FixedLaneChangeModel(null);
            DoubleScalar.Abs<SpeedUnit> maximumVelocity = new DoubleScalar.Abs<SpeedUnit>(200, SpeedUnit.KM_PER_HOUR);
            LaneBasedIndividualCar<String> car =
                new LaneBasedIndividualCar<String>("Car", carType, fam, laneChangeModel, carPositions, carSpeed,
                    new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER), new DoubleScalar.Rel<LengthUnit>(1.8,
                        LengthUnit.METER), maximumVelocity, new Route(new ArrayList<Node<?, ?>>()), simulator);
            // Let the simulator execute the move method of the car
            simulator.runUpTo(new DoubleScalar.Abs<TimeUnit>(61, TimeUnit.SECOND));
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
                assertEquals("It should take " + deltaTime + " seconds to cover distance " + distanceAtTime, deltaTime, car
                    .deltaTimeForDistance(new DoubleScalar.Rel<LengthUnit>(distanceAtTime, LengthUnit.METER)).getSI(),
                    0.0001);
                assertEquals("Car should reach distance " + distanceAtTime + " at " + (deltaTime + 60), deltaTime + 60, car
                    .timeAtDistance(new DoubleScalar.Rel<LengthUnit>(distanceAtTime, LengthUnit.METER)).getSI(), 0.0001);
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
     * @param totalLongitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the front position of the GTU from the start of the
     *            chain of Links
     * @param gtuLength DoubleScalar.Rel&lt;LengthUnit&gt;; the length of the GTU
     * @param links ArrayList&lt;CrossSectionLink&lt;?,?&gt;&gt;; the list of Links
     * @param fromLaneRank int; lowest rank of lanes that the GTU must be registered on (0-based)
     * @param uptoLaneRank int; highest rank of lanes that the GTU must be registered on (0-based)
     * @return Map&lt;Lane, DoubleScalar.Rel&lt;LengthUnit&gt;&gt;; the Map of the Lanes that the GTU is registered on
     */
    private Map<Lane, DoubleScalar.Rel<LengthUnit>> buildPositionsMap(
        DoubleScalar.Rel<LengthUnit> totalLongitudinalPosition, DoubleScalar.Rel<LengthUnit> gtuLength,
        ArrayList<CrossSectionLink<?, ?>> links, int fromLaneRank, int uptoLaneRank)
    {
        Map<Lane, DoubleScalar.Rel<LengthUnit>> result = new LinkedHashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        double cumulativeLength = 0;
        for (CrossSectionLink<?, ?> link : links)
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
                    result.put(lane, new DoubleScalar.Rel<LengthUnit>(rearPositionInLink, LengthUnit.METER));
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
    private Lane getNthLane(CrossSectionLink<?, ?> link, int rank)
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
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> simulator)
        throws SimRuntimeException, RemoteException
    {
        // Dummy
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return null;
    }

}

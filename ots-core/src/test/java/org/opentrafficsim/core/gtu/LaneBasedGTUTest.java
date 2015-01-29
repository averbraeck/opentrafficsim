package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.junit.Test;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.lanechanging.LaneChangeModel;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.factory.Node;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
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
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
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
     * The network is a linear array of Nodes connected by 5-Lane Links. In the middle, the Nodes are very closely
     * spaced. A truck is positioned over those center Nodes ensuring it covers several of the short Lanes in
     * succession.
     * @param truckFromLane int; lowest rank of lane range of the truck
     * @param truckUpToLane int; highest rank of lane range of the truck
     * @param carLanesCovered int; number of lanes that the car covers
     * @throws RemoteException on communications failure
     * @throws SimRuntimeException on ??? (should never happen; the simulator is not really used)
     * @throws NetworkException on network topology problem (should never happen)
     * @throws NamingException on errors registering the animation of objects (should never happen)
     */
    private void leaderFollowerParallel(int truckFromLane, int truckUpToLane, int carLanesCovered)
            throws RemoteException, SimRuntimeException, NetworkException, NamingException
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
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 2000, 2000));
        GTUType<String> carType = new GTUType<String>("car");
        GTUType<String> truckType = new GTUType<String>("truck");
        LaneType<String> laneType = new LaneType<String>("CarLane");
        laneType.addPermeability(carType);
        laneType.addPermeability(truckType);
        // Create a series of Nodes (some closely bunched together)
        ArrayList<Node> nodes = new ArrayList<Node>();
        int[] linkBoundaries = {0, 25, 50, 100, 101, 102, 103, 104, 105, 150, 175, 200};
        for (int xPos : linkBoundaries)
        {
            nodes.add(new Node("Node at " + xPos, new Coordinate(xPos, 20, 0)));
        }
        // Now we can build a series of Links with Lanes on them
        ArrayList<CrossSectionLink<?, ?>> links = new ArrayList<CrossSectionLink<?, ?>>();
        final int laneCount = 5;
        for (int i = 1; i < nodes.size(); i++)
        {
            Node fromNode = nodes.get(i - 1);
            Node toNode = nodes.get(i);
            String linkName = fromNode.getId() + "-" + toNode.getId();
            Lane[] lanes =
                    LaneFactory.makeMultiLane(linkName, fromNode, toNode, null, laneCount, laneType,
                            (OTSDEVSSimulatorInterface) simulator.getSimulator());
            links.add(lanes[0].getParentLink());
        }
        // Create a long truck with its front (reference) one meter in the last link on the 3rd lane
        DoubleScalar.Rel<LengthUnit> truckPosition = new DoubleScalar.Rel<LengthUnit>(106, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> truckLength = new DoubleScalar.Rel<LengthUnit>(15, LengthUnit.METER);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> truckPositions =
                buildPositionsMap(truckPosition, truckLength, links, truckFromLane, truckUpToLane);
        DoubleScalar.Abs<SpeedUnit> truckSpeed = new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR);
        DoubleScalar.Rel<LengthUnit> truckWidth = new DoubleScalar.Rel<LengthUnit>(2.5, LengthUnit.METER);
        Car<String> truck =
                new Car<String>("Truck", truckType, null, truckPositions, truckSpeed, truckLength, truckWidth, null,
                        (OTSDEVSSimulatorInterface) simulator.getSimulator());
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
        assertEquals("Truck should be registered in " + truckPositions.keySet().size() + " lanes", truckPositions
                .keySet().size(), found);
        DoubleScalar.Rel<LengthUnit> forwardMaxDistance = new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER);
        assertTrue("With one vehicle in the network forward headway should return a value larger than maxDistance",
                forwardMaxDistance.getSI() < truck.headway(forwardMaxDistance).getSI());
        assertEquals("With one vehicle in the network forward headwayGTU should return null", null,
                truck.headwayGTU(forwardMaxDistance));
        DoubleScalar.Rel<LengthUnit> reverseMaxDistance = new DoubleScalar.Rel<LengthUnit>(-9999, LengthUnit.METER);
        assertTrue("With one vehicle in the network reverse headway should return a value larger than maxDistance",
                Math.abs(reverseMaxDistance.getSI()) < truck.headway(reverseMaxDistance).getSI());
        assertEquals("With one vehicle in the network reverse headwayGTU should return null", null,
                truck.headwayGTU(reverseMaxDistance));
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
                Car<String> car =
                        new Car<String>("Car", carType, null, carPositions, carSpeed, carLength, carWidth, null,
                                (OTSDEVSSimulatorInterface) simulator.getSimulator());
                double actualHeadway = truck.headway(forwardMaxDistance).getSI();
                double expectedHeadway =
                        laneRank + carLanesCovered - 1 < truckFromLane || laneRank > truckUpToLane
                                || step - carLength.getSI() - truckPosition.getSI() <= 0 ? Double.MAX_VALUE : step
                                - carLength.getSI() - truckPosition.getSI();
                // System.out.println("carLanesCovered " + laneRank + ".." + (laneRank + carLanesCovered - 1)
                // + " truckLanesCovered " + truckFromLane + ".." + truckUpToLane + " car pos " + step
                // + " laneRank " + laneRank + " expected headway " + expectedHeadway);
                assertEquals("Forward headway should return " + expectedHeadway, expectedHeadway, actualHeadway, 0.1);
                LaneBasedGTU<?> leader = truck.headwayGTU(forwardMaxDistance);
                if (expectedHeadway == Double.MAX_VALUE)
                {
                    assertEquals("Leader should be null", null, leader);
                }
                else
                {
                    assertEquals("Leader should be the car", car, leader);
                }
                double actualReverseHeadway = truck.headway(reverseMaxDistance).getSI();
                double expectedReverseHeadway =
                        laneRank + carLanesCovered - 1 < truckFromLane || laneRank > truckUpToLane
                                || step >= truckPosition.getSI() - truckLength.getSI() ? Double.MAX_VALUE
                                : truckPosition.getSI() - truckLength.getSI() - step;
                assertEquals("Reverse headway should return " + expectedReverseHeadway, expectedReverseHeadway,
                        actualReverseHeadway, 0.1);
                LaneBasedGTU<?> follower = truck.headwayGTU(reverseMaxDistance);
                if (expectedReverseHeadway == Double.MAX_VALUE)
                {
                    assertEquals("Follower should be null", null, follower);
                }
                else
                {
                    assertEquals("Follower should be the car", car, follower);
                }
                Set<LaneBasedGTU<?>> leftParallel =
                        truck.parallel(LateralDirectionality.LEFT, simulator.getSimulator().getSimulatorTime().get());
                int expectedLeftSize =
                        laneRank + carLanesCovered - 1 < truckFromLane - 1 || laneRank >= truckUpToLane
                                || step <= truckPosition.getSI() - truckLength.getSI()
                                || step - carLength.getSI() > truckPosition.getSI() ? 0 : 1;
                assertEquals("Left parallel set size should be " + expectedLeftSize, expectedLeftSize,
                        leftParallel.size()); // This one caught a complex bug
                if (leftParallel.size() > 0)
                {
                    assertTrue("Parallel GTU should be the car", leftParallel.contains(car));
                }
                Set<LaneBasedGTU<?>> rightParallel =
                        truck.parallel(LateralDirectionality.RIGHT, simulator.getSimulator().getSimulatorTime().get());
                int expectedRightSize =
                        laneRank <= truckFromLane || laneRank > truckUpToLane + 1
                                || step < truckPosition.getSI() - truckLength.getSI()
                                || step - carLength.getSI() > truckPosition.getSI() ? 0 : 1;
                assertEquals("Right parallel set size should be " + expectedRightSize, expectedRightSize,
                        rightParallel.size());
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
     * @throws NetworkException
     * @throws NamingException
     * @throws RemoteException
     * @throws SimRuntimeException
     */
    @Test
    public void leaderFollowerAndParallelTest() throws RemoteException, NamingException, NetworkException,
            SimRuntimeException
    {
        leaderFollowerParallel(2, 2, 1);
        leaderFollowerParallel(2, 3, 1);
        leaderFollowerParallel(2, 2, 2);
        leaderFollowerParallel(2, 3, 2);
    }

    /**
     * Test the deltaTimeForDistance and timeAtDistance methods.
     * @throws RemoteException
     * @throws SimRuntimeException
     * @throws NamingException
     * @throws NetworkException
     */
    @Test
    public void timeAtDistanceTest() throws RemoteException, SimRuntimeException, NamingException, NetworkException
    {
        // Create a car with constant acceleration
        OTSModelInterface model = new Model();
        SimpleSimulator simulator =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 2000, 2000));
        // Run the simulator clock to some non-zero value
        simulator.getSimulator().scheduleEvent(
                new SimEvent<OTSSimTimeDouble>(
                        new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(60, TimeUnit.SECOND)),
                        SimEventInterface.MAX_PRIORITY, this, this, "autoPauseSimulator", null));
        while (simulator.getSimulator().getSimulatorTime().get().getSI() < 60)
        {
            simulator.getSimulator().step();
        }
        GTUType<String> carType = new GTUType<String>("car");
        LaneType<String> laneType = new LaneType<String>("CarLane");
        laneType.addPermeability(carType);
        Node fromNode = new Node("Node A", new Coordinate(0, 0, 0));
        Node toNode = new Node("Node B", new Coordinate(1000, 0, 0));
        String linkName = "AB";
        Lane lane =
                LaneFactory.makeMultiLane(linkName, fromNode, toNode, null, 1, laneType,
                        (OTSDEVSSimulatorInterface) simulator.getSimulator())[0];
        DoubleScalar.Rel<LengthUnit> carPosition = new DoubleScalar.Rel<LengthUnit>(100, LengthUnit.METER);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> carPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        carPositions.put(lane, carPosition);
        DoubleScalar.Abs<SpeedUnit> carSpeed = new DoubleScalar.Abs<SpeedUnit>(10, SpeedUnit.METER_PER_SECOND);
        for (int a = 1; a >= -1; a--)
        {
            DoubleScalar.Abs<AccelerationUnit> acceleration =
                    new DoubleScalar.Abs<AccelerationUnit>(a, AccelerationUnit.METER_PER_SECOND_2);
            Car<String> car =
                    new Car<String>("Car", carType, null, carPositions, carSpeed, new DoubleScalar.Rel<LengthUnit>(4,
                            LengthUnit.METER), new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER), null,
                            (OTSDEVSSimulatorInterface) simulator.getSimulator());
            // System.out.println("acceleration is " + acceleration);
            GTUFollowingModel.GTUFollowingModelResult gfmr =
                    new GTUFollowingModel.GTUFollowingModelResult(acceleration, new DoubleScalar.Abs<TimeUnit>(70,
                            TimeUnit.SECOND));
            car.setState(gfmr);
            // Check the results
            for (int timeStep = 1; timeStep < 100; timeStep++)
            {
                double time = 0.1 * timeStep;
                double distanceAtTime = carSpeed.getSI() * time + 0.5 * acceleration.getSI() * time * time;
                // System.out.println(String.format("time %.1fs, distance %.3fm", time, distanceAtTime));
                assertEquals("It should take " + time + " seconds to cover distance " + distanceAtTime, time, car
                        .deltaTimeForDistance(new DoubleScalar.Rel<LengthUnit>(distanceAtTime, LengthUnit.METER))
                        .getSI(), 0.0001);
                assertEquals("Car should reach distance " + distanceAtTime + " at " + (time + 60), time + 60, car
                        .timeAtDistance(new DoubleScalar.Rel<LengthUnit>(distanceAtTime, LengthUnit.METER)).getSI(),
                        0.0001);
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
     * @param totalLongitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the front position of the GTU from the start
     *            of the chain of Links
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
        Map<Lane, DoubleScalar.Rel<LengthUnit>> result = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        double cumulativeLength = 0;
        for (CrossSectionLink<?, ?> link : links)
        {
            double linkLength = link.getLength().getSI();
            double frontPositionInLink = totalLongitudinalPosition.getSI() - cumulativeLength;
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
                    result.put(lane, new DoubleScalar.Rel<LengthUnit>(frontPositionInLink, LengthUnit.METER));
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

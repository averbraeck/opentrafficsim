package org.opentrafficsim.road.gtu.following;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

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
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.road.car.CarTest;
import org.opentrafficsim.road.car.LaneBasedIndividualCar;
import org.opentrafficsim.road.gtu.lane.changing.AbstractLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.changing.Egoistic;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.route.CompleteLaneBasedRouteNavigator;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Jul 11, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IDMPlusTest implements UNITS
{

    /**
     * Test IDMPlus.
     * @throws Exception when something goes wrong (should not happen)
     */
    @SuppressWarnings({"static-method", "checkstyle:methodlength"})
    @Test
    public final void computeAcceleration() throws Exception
    {
        // Check a car standing still with no leaders accelerates with maximum acceleration
        SimpleSimulator simulator =
            new SimpleSimulator(new Time.Abs(0, SECOND), new Time.Rel(0, SECOND), new Time.Rel(3600, SECOND),
                new IDMPlusTestModel());
        Length.Rel s0 = new Length.Rel(2, METER);
        GTUFollowingModel carFollowingModel =
            new IDMPlus(new Acceleration(1.25, METER_PER_SECOND_2), new Acceleration(1.5, METER_PER_SECOND_2), s0,
                new Time.Rel(1, SECOND), 1d);
        GTUType gtuType = GTUType.makeGTUType("Car");
        LaneType laneType = new LaneType("CarLane");
        laneType.addCompatibility(gtuType);
        Lane lane = CarTest.makeLane(laneType);
        Time.Abs initialTime = new Time.Abs(0, SECOND);
        Length.Rel initialPosition = new Length.Rel(123.456, METER);
        Speed initialSpeed = new Speed(0, KM_PER_HOUR);
        Length.Rel length = new Length.Rel(5.0, METER);
        Length.Rel width = new Length.Rel(2.0, METER);
        Map<Lane, Length.Rel> initialLongitudinalPositions = new LinkedHashMap<>();
        initialLongitudinalPositions.put(lane, initialPosition);
        Speed maxSpeed = new Speed(120, KM_PER_HOUR);
        AbstractLaneChangeModel laneChangeModel = new Egoistic();
        LaneBasedIndividualCar referenceCar =
            new LaneBasedIndividualCar("12345", gtuType, carFollowingModel, laneChangeModel, initialLongitudinalPositions,
                initialSpeed, length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(new CompleteRoute("", GTUType.ALL)), simulator);
        Speed speedLimit = new Speed(100, KM_PER_HOUR);
        AccelerationStep cfmr = carFollowingModel.computeAccelerationWithNoLeader(referenceCar, speedLimit);
        assertEquals("Standard time slice in IDM+ is 0.5s", 0.5, cfmr.getValidUntil().getSI(), 0.0001);
        assertEquals("Acceleration should be maximum", 1.25, cfmr.getAcceleration().getSI(), 0.0001);
        // Create another car at exactly the stationary following distance
        // Check that the follower remains stationary
        Length.Rel leaderPosition =
            new Length.Rel(2 + referenceCar.getLength().getSI()
                + referenceCar.position(lane, referenceCar.getReference(), initialTime).getSI(), METER);
        Map<Lane, Length.Rel> leaderPositions = new LinkedHashMap<>();
        leaderPositions.put(lane, leaderPosition);
        // The leader gets a car following model that makes it stay in place for a loooong time
        FixedAccelerationModel fam =
            new FixedAccelerationModel(new Acceleration(0, METER_PER_SECOND_2), new Time.Rel(9999, SECOND));
        LaneBasedIndividualCar leaderCar =
            new LaneBasedIndividualCar("23456", gtuType, fam, laneChangeModel, leaderPositions, initialSpeed, length, width,
                maxSpeed, new CompleteLaneBasedRouteNavigator(new CompleteRoute("", GTUType.ALL)), simulator);
        HeadwayGTU leader =
            new HeadwayGTU(leaderCar, leaderPosition.getSI() - referenceCar.getLength().getSI() - initialPosition.getSI());
        cfmr =
            carFollowingModel.computeAcceleration(referenceCar, leaderCar.getLongitudinalVelocity(), leader.getDistance(),
                speedLimit);
        assertEquals("Acceleration should be 0", 0, cfmr.getAcceleration().getSI(), 0.0001);
        leaderPosition =
            new Length.Rel(1000 + (3 + referenceCar.getLength().getSI() + referenceCar.position(lane,
                referenceCar.getFront(), initialTime).getSI()), METER);
        leaderPositions = new LinkedHashMap<>();
        leaderPositions.put(lane, leaderPosition);
        // Exercise the if statement that ignores leaders that are further ahead
        LaneBasedIndividualCar leaderCar2 =
            new LaneBasedIndividualCar("34567", gtuType, fam, laneChangeModel, leaderPositions, initialSpeed, length, width,
                maxSpeed, new CompleteLaneBasedRouteNavigator(new CompleteRoute("", GTUType.ALL)), simulator);
        // Verify that the result is independent of the order of adding in the Collection
        Collection<HeadwayGTU> leaders = new ArrayList<HeadwayGTU>();
        HeadwayGTU leader2 =
            new HeadwayGTU(leaderCar2, leaderPosition.getSI() - referenceCar.getLength().getSI() - initialPosition.getSI());
        leaders.add(leader2); // Put the 2nd leader in first place
        leaders.add(leader);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit).getLeaderAccelerationStep();
        assertEquals("Acceleration should be 0", 0, cfmr.getAcceleration().getSI(), 0.0001);
        leaders.clear();
        leaders.add(leader); // Put the 1st leader in first place
        leaders.add(leader2);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit).getLeaderAccelerationStep();
        assertEquals("Acceleration should be 0", 0, cfmr.getAcceleration().getSI(), 0.0001);
        leaders.clear();
        leaderPosition =
            new Length.Rel(-(3 + referenceCar.getLength().getSI() + referenceCar.position(lane, referenceCar.getFront(),
                initialTime).getSI()), METER);
        leaderPositions = new LinkedHashMap<>();
        leaderPositions.put(lane, leaderPosition);
        leaderCar.destroy();
        leaderCar2.destroy();
        leaderCar =
            new LaneBasedIndividualCar("223344", gtuType, fam, laneChangeModel, leaderPositions, initialSpeed, length,
                width, maxSpeed, new CompleteLaneBasedRouteNavigator(new CompleteRoute("", GTUType.ALL)), simulator);
        leader =
            new HeadwayGTU(leaderCar, leaderPosition.getSI() - referenceCar.getLength().getSI() - initialPosition.getSI());
        leaders.add(leader);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit).getFollowerAccelerationStep();
        assertEquals("Acceleration should be 1.25", 1.25, cfmr.getAcceleration().getSI(), 0.0001);
        // Check that the returned acceleration increases with the distance to the leader
        double referenceAcceleration = -1;
        for (int spareDistance = 0; spareDistance <= 500; spareDistance++)
        {
            leaders.clear();
            leaderPosition =
                new Length.Rel(spareDistance
                    + (3 + referenceCar.getLength().getSI() + referenceCar.position(lane, referenceCar.getFront(),
                        initialTime).getSI()), METER);
            leaderPositions = new LinkedHashMap<>();
            leaderPositions.put(lane, leaderPosition);
            leaderCar =
                new LaneBasedIndividualCar("0", gtuType, fam, laneChangeModel, leaderPositions, initialSpeed, length, width,
                    maxSpeed, new CompleteLaneBasedRouteNavigator(new CompleteRoute("", GTUType.ALL)), simulator);
            leader =
                new HeadwayGTU(leaderCar, leaderPosition.getSI() - referenceCar.getLength().getSI()
                    - initialPosition.getSI());
            leaders.add(leader);
            cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit).getFollowerAccelerationStep();
            double acceleration = cfmr.getAcceleration().getSI();
            // System.out.println("Acceleration with stationary leader at " + spareDistance + " is " + acceleration);
            assertTrue("acceleration should not decrease when distance to leader is increased",
                acceleration >= referenceAcceleration);
            referenceAcceleration = acceleration;
            leaderCar.destroy();
        }
        assertTrue("Highest acceleration should be less than max", referenceAcceleration <= 1.25);
        // Check that the returned acceleration increases with the speed of the leader
        // System.out.println("");
        referenceAcceleration = Double.NEGATIVE_INFINITY;
        leaderPosition =
            new Length.Rel(2 + 3 + referenceCar.getLength().getSI()
                + referenceCar.position(lane, referenceCar.getFront(), initialTime).getSI(), METER);
        leaderPositions = new LinkedHashMap<>();
        leaderPositions.put(lane, leaderPosition);
        // In IDM+ the reference car must have non-zero speed for the leader speed to have any effect
        initialSpeed = new Speed(2, METER_PER_SECOND);
        for (int integerLeaderSpeed = 0; integerLeaderSpeed <= 40; integerLeaderSpeed++)
        {
            Map<Lane, Length.Rel> initialPositions = new LinkedHashMap<>();
            initialPositions.put(lane, initialPosition);
            referenceCar.destroy();
            referenceCar =
                new LaneBasedIndividualCar("12345", gtuType, carFollowingModel, laneChangeModel, initialPositions,
                    initialSpeed, length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(new CompleteRoute("", GTUType.ALL)),
                    simulator);
            leaders.clear();
            Speed leaderSpeed = new Speed(integerLeaderSpeed, METER_PER_SECOND);
            leaderCar =
                new LaneBasedIndividualCar("0", gtuType, fam, laneChangeModel, leaderPositions, leaderSpeed, length, width,
                    maxSpeed, new CompleteLaneBasedRouteNavigator(new CompleteRoute("", GTUType.ALL)), simulator);
            leader =
                new HeadwayGTU(leaderCar, leaderPosition.getSI() - referenceCar.getLength().getSI()
                    - initialPosition.getSI());
            leaders.add(leader);
            // System.out.println("referenceCar: " + referenceCar);
            // System.out.println("leaderCar   : " + leaderCar);
            cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit).getFollowerAccelerationStep();
            double acceleration = cfmr.getAcceleration().getSI();
            // System.out.println("Acceleration with leader driving " + integerLeaderSpeed + " m/s is " + acceleration);
            assertTrue("acceleration should not decrease when leader speed is increased",
                acceleration >= referenceAcceleration);
            referenceAcceleration = acceleration;
            leaderCar.destroy();
        }
        assertTrue("Highest acceleration should be less than max", referenceAcceleration <= 1.25);
        // Check that a car that is 100m behind a stationary car accelerates, then decelerates and stops at the right
        // point. (In IDM+ the car oscillates a while around the final position with pretty good damping.)
        initialPosition = new Length.Rel(100, METER);
        Map<Lane, Length.Rel> initialPositions = new LinkedHashMap<>();
        initialPositions.put(lane, initialPosition);
        initialSpeed = new Speed(0, METER_PER_SECOND);
        referenceCar.destroy();
        referenceCar =
            new LaneBasedIndividualCar("12345", gtuType, carFollowingModel, laneChangeModel, initialPositions, initialSpeed,
                length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(new CompleteRoute("", GTUType.ALL)), simulator);
        leaderPosition =
            new Length.Rel(100 + 3 + referenceCar.getLength().getSI()
                + referenceCar.position(lane, referenceCar.getFront(), initialTime).getSI(), METER);
        leaderCar =
            new LaneBasedIndividualCar("0", gtuType, fam, laneChangeModel, leaderPositions, initialSpeed, length, width,
                maxSpeed, new CompleteLaneBasedRouteNavigator(new CompleteRoute("", GTUType.ALL)), simulator);
        for (int timeStep = 0; timeStep < 200; timeStep++)
        {
            Time.Abs simulateUntil = new Time.Abs(0.1 * timeStep, TimeUnit.SI);
            simulator.runUpTo(simulateUntil);
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
            // System.out.println(String.format("step %3d, t=%s, referenceCar: %s, speed %s, leaderCar: %s", timeStep,
            // simulateUntil, referenceCar, referenceCar.getLongitudinalVelocity(), leaderCar));
            if (timeStep > 120)
            {
                double position = referenceCar.position(lane, referenceCar.getFront()).getSI();
                assertEquals("After 20 seconds the referenceCar should now be very close to " + s0
                    + " before the rear of the leader", leaderCar.position(lane, referenceCar.getRear()).getSI()
                    - s0.getSI(), position, 0.2);
                assertEquals("After 20 seconds the speed of the referenceCar should be almost 0", 0, referenceCar
                    .getLongitudinalVelocity().getSI(), 0.2);
            }
        }
    }

}

/**
 * Simulation model for IDMPlusTest.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version 0 feb. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class IDMPlusTestModel implements OTSModelInterface
{

    /** */
    private static final long serialVersionUID = 20150210L;

    /** {@inheritDoc} */
    @Override
    public void constructModel(
        final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator)
        throws SimRuntimeException
    {
        // do nothing.
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
        
    {
        return null;
    }

}

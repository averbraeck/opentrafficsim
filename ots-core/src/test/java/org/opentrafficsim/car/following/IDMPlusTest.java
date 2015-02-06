package org.opentrafficsim.car.following;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.junit.Test;
import org.opentrafficsim.car.CarTest;
import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.AccelerationStep;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 11, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IDMPlusTest
{

    /**
     * Test IDMPlus.
     * @throws RemoteException on communications failure
     * @throws NetworkException on network error
     * @throws NamingException on ???
     * @throws SimRuntimeException on ???
     */
    @SuppressWarnings({"static-method", "checkstyle:methodlength"})
    @Test
    public final void computeAcceleration() throws RemoteException, NetworkException, SimRuntimeException,
            NamingException
    {
        // Check a car standing still with no leaders accelerates with maximum acceleration
        OTSDEVSSimulator simulator = CarTest.makeSimulator();
        GTUFollowingModel carFollowingModel =
                new IDMPlus(new DoubleScalar.Abs<AccelerationUnit>(1.25, AccelerationUnit.METER_PER_SECOND_2),
                        new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                        new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(1,
                                TimeUnit.SECOND), 1d);
        Lane lane = CarTest.makeLane();
        GTUType<String> carType = new GTUType<String>("Car");
        DoubleScalar.Abs<TimeUnit> initialTime = new DoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND);
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(123.456, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR);
        DoubleScalar.Rel<LengthUnit> length = new DoubleScalar.Rel<LengthUnit>(5.0, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions = new HashMap<>();
        initialLongitudinalPositions.put(lane, initialPosition);
        DoubleScalar.Abs<SpeedUnit> maxSpeed = new DoubleScalar.Abs<SpeedUnit>(120, SpeedUnit.KM_PER_HOUR);
        LaneBasedIndividualCar<Integer> referenceCar =
                new LaneBasedIndividualCar<Integer>(12345, carType, carFollowingModel, initialLongitudinalPositions,
                        initialSpeed, length, width, maxSpeed, simulator);
        DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        Collection<LaneBasedIndividualCar<Integer>> leaders = new ArrayList<LaneBasedIndividualCar<Integer>>();
        AccelerationStep cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Standard time slice in IDM+ is 0.5s", 0.5, cfmr.getValidUntil().getSI(), 0.0001);
        assertEquals("Acceleration should be maximum", 1.25, cfmr.getAcceleration().getSI(), 0.0001);
        // Create another car at exactly the stationary following distance
        // Check that the follower remains stationary
        DoubleScalar.Rel<LengthUnit> leaderPosition =
                new DoubleScalar.Rel<LengthUnit>(2 + referenceCar.getLength().getSI()
                        + referenceCar.position(lane, referenceCar.getReference(), initialTime).getSI(),
                        LengthUnit.METER);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> leaderPositions = new HashMap<>();
        leaderPositions.put(lane, leaderPosition);
        LaneBasedIndividualCar<Integer> leaderCar =
                new LaneBasedIndividualCar<Integer>(23456, carType, null, leaderPositions, initialSpeed, length, width,
                        maxSpeed, simulator);
        leaders.add(leaderCar);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Acceleration should be 0", 0, cfmr.getAcceleration().getSI(), 0.0001);
        leaders.clear();
        leaderPosition =
                new DoubleScalar.Rel<LengthUnit>(1000 + (3 + referenceCar.getLength().getSI() + referenceCar.position(
                        lane, referenceCar.getFront(), initialTime).getSI()), LengthUnit.METER);
        leaderPositions = new HashMap<>();
        leaderPositions.put(lane, leaderPosition);
        // Exercise the if statement that ignores leaders that are further ahead
        LaneBasedIndividualCar<Integer> leaderCar2 =
                new LaneBasedIndividualCar<Integer>(34567, carType, null, leaderPositions, initialSpeed, length, width,
                        maxSpeed, simulator);
        leaders.add(leaderCar2); // Put the 2nd leader in first place
        leaders.add(leaderCar);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Acceleration should be 0", 0, cfmr.getAcceleration().getSI(), 0.0001);
        leaders.clear();
        leaders.add(leaderCar); // Put the 1st leader in first place
        leaders.add(leaderCar2);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Acceleration should be 0", 0, cfmr.getAcceleration().getSI(), 0.0001);
        leaders.clear();
        leaderPosition =
                new DoubleScalar.Rel<LengthUnit>(-(3 + referenceCar.getLength().getSI() + referenceCar.position(lane,
                        referenceCar.getFront(), initialTime).getSI()), LengthUnit.METER);
        leaderPositions = new HashMap<>();
        leaderPositions.put(lane, leaderPosition);
        leaderCar =
                new LaneBasedIndividualCar<Integer>(23456, carType, null, leaderPositions, initialSpeed, length, width,
                        maxSpeed, simulator);
        leaders.add(leaderCar);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Acceleration should be 1.25", 1.25, cfmr.getAcceleration().getSI(), 0.0001);
        // Check that the returned acceleration increases with the distance to the leader
        double referenceAcceleration = -1;
        for (int spareDistance = 0; spareDistance <= 500; spareDistance++)
        {
            leaders.clear();
            leaderPosition =
                    new DoubleScalar.Rel<LengthUnit>(spareDistance
                            + (3 + referenceCar.getLength().getSI() + referenceCar.position(lane,
                                    referenceCar.getFront(), initialTime).getSI()), LengthUnit.METER);
            leaderPositions = new HashMap<>();
            leaderPositions.put(lane, leaderPosition);
            leaderCar =
                    new LaneBasedIndividualCar<Integer>(0, carType, null, leaderPositions, initialSpeed, length, width,
                            maxSpeed, simulator);
            leaders.add(leaderCar);
            cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
            double acceleration = cfmr.getAcceleration().getSI();
            // System.out.println("Acceleration with stationary leader at " + spareDistance + " is " + acceleration);
            assertTrue("acceleration should not decrease when distance to leader is increased",
                    acceleration >= referenceAcceleration);
            referenceAcceleration = acceleration;
        }
        assertTrue("Highest acceleration should be less than max", referenceAcceleration <= 1.25);
        // Check that the returned acceleration increases with the speed of the leader
        // System.out.println("");
        referenceAcceleration = Double.NEGATIVE_INFINITY;
        leaderPosition =
                new DoubleScalar.Rel<LengthUnit>(2 + 3 + referenceCar.getLength().getSI()
                        + referenceCar.position(lane, referenceCar.getFront(), initialTime).getSI(), LengthUnit.METER);
        leaderPositions = new HashMap<>();
        leaderPositions.put(lane, leaderPosition);
        // In IDM+ the reference car must have non-zero speed for the leader speed to have any effect
        initialSpeed = new DoubleScalar.Abs<SpeedUnit>(2, SpeedUnit.METER_PER_SECOND);
        for (int integerLeaderSpeed = 0; integerLeaderSpeed <= 40; integerLeaderSpeed++)
        {
            Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<>();
            initialPositions.put(lane, initialPosition);
            referenceCar =
                    new LaneBasedIndividualCar<Integer>(12345, carType, carFollowingModel, initialPositions,
                            initialSpeed, length, width, maxSpeed, simulator);
            leaders.clear();
            DoubleScalar.Abs<SpeedUnit> leaderSpeed =
                    new DoubleScalar.Abs<SpeedUnit>(integerLeaderSpeed, SpeedUnit.METER_PER_SECOND);
            leaderCar =
                    new LaneBasedIndividualCar<Integer>(0, carType, null, leaderPositions, leaderSpeed, length, width,
                            maxSpeed, simulator);
            leaders.add(leaderCar);
            // System.out.println("referenceCar: " + referenceCar);
            // System.out.println("leaderCar   : " + leaderCar);
            cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
            double acceleration = cfmr.getAcceleration().getSI();
            // System.out.println("Acceleration with leader driving " + integerLeaderSpeed + " m/s is " + acceleration);
            assertTrue("acceleration should not decrease when leader speed is increased",
                    acceleration >= referenceAcceleration);
            referenceAcceleration = acceleration;
        }
        assertTrue("Highest acceleration should be less than max", referenceAcceleration <= 1.25);
        // Check that a car that is 100m behind a stationary car accelerates, then decelerates and stops at the right
        // point. (In IDM+ the car oscillates a while around the final position with pretty good damping.)
        initialPosition = new DoubleScalar.Rel<LengthUnit>(100, LengthUnit.METER);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<>();
        initialPositions.put(lane, initialPosition);
        initialSpeed = new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.METER_PER_SECOND);
        referenceCar =
                new LaneBasedIndividualCar<Integer>(12345, carType, carFollowingModel, initialPositions, initialSpeed,
                        length, width, maxSpeed, simulator);
        leaders.clear();
        leaderPosition =
                new DoubleScalar.Rel<LengthUnit>(100 + 3 + referenceCar.getLength().getSI()
                        + referenceCar.position(lane, referenceCar.getFront(), initialTime).getSI(), LengthUnit.METER);
        leaderPositions.clear();
        leaderPositions.put(lane, leaderPosition);
        leaderCar =
                new LaneBasedIndividualCar<Integer>(0, carType, null, leaderPositions, initialSpeed, length, width,
                        maxSpeed, simulator);
        leaders.add(leaderCar);
        // System.out.println("Setup    referenceCar: " + referenceCar);
        for (int timeStep = 0; timeStep < 200; timeStep++)
        {
            // System.out.println("timeStep: " + timeStep);
            // This crashes at timeStep 62.
            if (timeStep > 0)
            {
                break; // TODO fix the problem as described below...
            }
            // TODO: re-write and USE that simulator to do run the car.
            // The way things work today, the referenceCar is driving backwards!
            cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
            // System.out.println("follower: " + referenceCar);
            // System.out.println("      leader: " + leaderCar + "   cfmr: " + cfmr);
            referenceCar.setState(cfmr);
            // DoubleScalar.Abs<TimeUnit> thisEvaluationTime = referenceCar.getNextEvaluationTime();
            // DoubleScalar.Abs<SpeedUnit> currentSpeed = referenceCar.getLongitudinalVelocity(thisEvaluationTime);
            // System.out.println(String.format("step %3d, t=%s, v=%s, a=%s, referenceCar: %s, leaderCar: %s", timeStep,
            // cfmr.getValidUntil().getSI(), currentSpeed, referenceCar.getAcceleration(),
            // referenceCar, leaderCar));
            if (timeStep > 100)
            {
                double position = referenceCar.position(lane, referenceCar.getFront(), cfmr.getValidUntil()).getSI();
                assertEquals(
                        "After 20 seconds the referenceCar should now be very close to 2m before the rear of the leader",
                        leaderCar.position(lane, referenceCar.getRear()).getSI() - 2.0, position, 0.2);
                assertEquals("After 20 seconds the speed of the referenceCar should be almost 0", 0, referenceCar
                        .getLongitudinalVelocity(cfmr.getValidUntil()).getSI(), 0.2);
            }
        }
    }

}

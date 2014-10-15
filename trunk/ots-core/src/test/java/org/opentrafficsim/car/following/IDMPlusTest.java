package org.opentrafficsim.car.following;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.location.Line;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 11, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IDMPlusTest
{

    /**
     * Test IDMPlus.
     */
    @SuppressWarnings("static-method")
    @Test
    public final void computeAcceleration()
    {
        // Check a car standing still with no leaders accelerates with maximum acceleration
        OTSDEVSSimulator simulator = new OTSDEVSSimulator();
        CarFollowingModel<Car> carFollowingModel = new IDMPlus<Line<String>, Car>();
        DoubleScalar.Abs<TimeUnit> initialTime = new DoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND);
        DoubleScalar.Abs<LengthUnit> initialPosition = new DoubleScalar.Abs<LengthUnit>(123.456, LengthUnit.METER);
        DoubleScalar.Rel<SpeedUnit> initialSpeed = new DoubleScalar.Rel<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR);
        Car referenceCar = new Car(12345, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
        DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        Collection<Car> leaders = new ArrayList<Car>();
        CarFollowingModelResult cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Standard time slice in IDM+ is 0.5s", 0.5, cfmr.validUntil.getSI(), 0.0001);
        assertEquals("Acceleration should be maximum", 1.25, cfmr.acceleration.getSI(), 0.0001);
        // Create another car at exactly the stationary following distance
        // Check that the follower remains stationary
        DoubleScalar.Abs<LengthUnit> leaderPosition =
                new DoubleScalar.Abs<LengthUnit>(3 + referenceCar.length().getSI()
                        + referenceCar.getPosition(initialTime).getSI(), LengthUnit.METER);
        Car leaderCar = new Car(23456, simulator, null, initialTime, leaderPosition, initialSpeed);
        leaders.add(leaderCar);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Acceleration should be 0", 0, cfmr.acceleration.getSI(), 0.0001);
        leaders.clear();
        leaderPosition =
                new DoubleScalar.Abs<LengthUnit>(1000 + (3 + referenceCar.length().getSI() + referenceCar.getPosition(
                        initialTime).getSI()), LengthUnit.METER);
        // Exercise the if statement that ignores leaders that are further ahead
        Car leaderCar2 = new Car(34567, simulator, null, initialTime, leaderPosition, initialSpeed);
        leaders.add(leaderCar2); // Put the 2nd leader in first place
        leaders.add(leaderCar);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Acceleration should be 0", 0, cfmr.acceleration.getSI(), 0.0001);
        leaders.clear();
        leaders.add(leaderCar); // Put the 1st leader in first place
        leaders.add(leaderCar2);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Acceleration should be 0", 0, cfmr.acceleration.getSI(), 0.0001);
        leaders.clear();
        leaderPosition =
                new DoubleScalar.Abs<LengthUnit>(-(3 + referenceCar.length().getSI() + referenceCar.getPosition(
                        initialTime).getSI()), LengthUnit.METER);
        leaderCar = new Car(23456, simulator, null, initialTime, leaderPosition, initialSpeed);
        leaders.add(leaderCar);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Acceleration should be 1.25", 1.25, cfmr.acceleration.getSI(), 0.0001);
        // Check that the returned acceleration increases with the distance to the leader
        double referenceAcceleration = -1;
        for (int spareDistance = 0; spareDistance <= 500; spareDistance++)
        {
            leaders.clear();
            leaderPosition =
                    new DoubleScalar.Abs<LengthUnit>(spareDistance
                            + (3 + referenceCar.length().getSI() + referenceCar.getPosition(initialTime).getSI()),
                            LengthUnit.METER);
            leaderCar = new Car(0, simulator, null, initialTime, leaderPosition, initialSpeed);
            leaders.add(leaderCar);
            cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
            double acceleration = cfmr.acceleration.getSI();
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
                new DoubleScalar.Abs<LengthUnit>(2 + 3 + referenceCar.length().getSI()
                        + referenceCar.getPosition(initialTime).getSI(), LengthUnit.METER);
        // In IDM+ the reference car must have non-zero speed for the leader speed to have any effect
        initialSpeed = new DoubleScalar.Rel<SpeedUnit>(2, SpeedUnit.METER_PER_SECOND);
        for (int integerLeaderSpeed = 0; integerLeaderSpeed <= 40; integerLeaderSpeed++)
        {
            referenceCar = new Car(12345, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
            leaders.clear();
            DoubleScalar.Rel<SpeedUnit> leaderSpeed =
                    new DoubleScalar.Rel<SpeedUnit>(integerLeaderSpeed, SpeedUnit.METER_PER_SECOND);
            leaderCar = new Car(0, simulator, null, initialTime, leaderPosition, leaderSpeed);
            leaders.add(leaderCar);
            // System.out.println("referenceCar: " + referenceCar);
            // System.out.println("leaderCar   : " + leaderCar);
            cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
            double acceleration = cfmr.acceleration.getSI();
            // System.out.println("Acceleration with leader driving " + integerLeaderSpeed + " m/s is " + acceleration);
            assertTrue("acceleration should not decrease when leader speed is increased", acceleration >= referenceAcceleration);
            referenceAcceleration = acceleration;
        }
        assertTrue("Highest acceleration should be less than max", referenceAcceleration <= 1.25);
        // Check that a car that is 100m behind a stationary car accelerates, then decelerates and stops at the right
        // point. (In IDM+ the car oscillates a while around the final position with pretty good damping.)
        initialPosition = new DoubleScalar.Abs<LengthUnit>(100, LengthUnit.METER);
        initialSpeed = new DoubleScalar.Rel<SpeedUnit>(0, SpeedUnit.METER_PER_SECOND);
        referenceCar = new Car(12345, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
        leaders.clear();
        leaderPosition =
                new DoubleScalar.Abs<LengthUnit>(100 + 3 + referenceCar.length().getSI()
                        + referenceCar.getPosition(initialTime).getSI(), LengthUnit.METER);
        leaderCar = new Car(0, simulator, null, initialTime, leaderPosition, initialSpeed);
        leaders.add(leaderCar);
        // System.out.println("Setup    referenceCar: " + referenceCar);
        for (int timeStep = 0; timeStep < 200; timeStep++)
        {
            cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
            referenceCar.setState(cfmr);
            // System.out.println(String.format("step %3d referenceCar: %s", timeStep, referenceCar));
            if (timeStep > 100)
            {
                double position = referenceCar.getPosition(cfmr.validUntil).getSI();
                assertEquals("After 20 seconds the referenceCar should now be very close to 3m before the rear of the leader",
                        200, position, 0.1);
                assertEquals("After 20 seconds the speed of the referenceCar should be almost 0", 0,
                        referenceCar.getVelocity(cfmr.validUntil).getSI(), 0.2);
            }
        }
    }

}

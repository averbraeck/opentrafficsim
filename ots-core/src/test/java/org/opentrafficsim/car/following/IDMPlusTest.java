package org.opentrafficsim.car.following;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;

import org.junit.Test;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.core.location.Line;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Jul 11, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IDMPlusTest
{

    /**
     * Test IDMPlus
     */
    @SuppressWarnings("static-method")
    @Test
    public void computeAcceleration()
    {
        // Check a car standing still with no leaders accelerates with maximum acceleration
        DEVSSimulator simulator = new DEVSSimulator();
        CarFollowingModel carFollowingModel = new IDMPlus<Line<String>>();
        DoubleScalarAbs<TimeUnit> initialTime = new DoubleScalarAbs<TimeUnit>(0, TimeUnit.SECOND);
        DoubleScalarAbs<LengthUnit> initialPosition = new DoubleScalarAbs<LengthUnit>(123.456, LengthUnit.METER);
        DoubleScalarRel<SpeedUnit> initialSpeed = new DoubleScalarRel<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR);
        Car referenceCar = new Car(12345, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
        DoubleScalarAbs<SpeedUnit> speedLimit = new DoubleScalarAbs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        Collection<Car> leaders = new ArrayList<Car>();
        CarFollowingModelResult cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Standard time slice in IDM+ is 0.5s", 0.5, cfmr.validUntil.getValueSI(), 0.0001);
        assertEquals("Acceleration should be maximum", 1.25, cfmr.acceleration.getValueSI(), 0.0001);
        // Create another car at exactly the stationary following distance
        // Check that the follower remains stationary
        DoubleScalarAbs<LengthUnit> leaderPosition =
                new DoubleScalarAbs<LengthUnit>(3 + referenceCar.length().getValueSI()
                        + referenceCar.position(initialTime).getValueSI(), LengthUnit.METER);
        Car leaderCar = new Car(23456, simulator, null, initialTime, leaderPosition, initialSpeed);
        leaders.add(leaderCar);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Acceleration should be 0", 0, cfmr.acceleration.getValueSI(), 0.0001);
        leaders.clear();
        leaderPosition =
                new DoubleScalarAbs<LengthUnit>(1000 + (3 + referenceCar.length().getValueSI() + referenceCar.position(
                        initialTime).getValueSI()), LengthUnit.METER);
        // Exercise the if statement that ignores leaders that are further ahead
        Car leaderCar2 = new Car(34567, simulator, null, initialTime, leaderPosition, initialSpeed);
        leaders.add(leaderCar2); // Put the 2nd leader in first place
        leaders.add(leaderCar);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Acceleration should be 0", 0, cfmr.acceleration.getValueSI(), 0.0001);
        leaders.clear();
        leaders.add(leaderCar); // Put the 1st leader in first place
        leaders.add(leaderCar2);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Acceleration should be 0", 0, cfmr.acceleration.getValueSI(), 0.0001);
        leaders.clear();
        leaderPosition =
                new DoubleScalarAbs<LengthUnit>(-(3 + referenceCar.length().getValueSI() + referenceCar.position(
                        initialTime).getValueSI()), LengthUnit.METER);
        leaderCar = new Car(23456, simulator, null, initialTime, leaderPosition, initialSpeed);
        leaders.add(leaderCar);
        cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
        assertEquals("Acceleration should be 1.25", 1.25, cfmr.acceleration.getValueSI(), 0.0001);
        // Check that the returned acceleration increases with the distance to the leader
        double referenceAcceleration = -1;
        for (int spareDistance = 0; spareDistance <= 500; spareDistance++)
        {
            leaders.clear();
            leaderPosition =
                    new DoubleScalarAbs<LengthUnit>(
                            spareDistance
                                    + (3 + referenceCar.length().getValueSI() + referenceCar.position(initialTime)
                                            .getValueSI()), LengthUnit.METER);
            leaderCar = new Car(0, simulator, null, initialTime, leaderPosition, initialSpeed);
            leaders.add(leaderCar);
            cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
            double acceleration = cfmr.acceleration.getValueSI();
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
                new DoubleScalarAbs<LengthUnit>(2 + 3 + referenceCar.length().getValueSI()
                        + referenceCar.position(initialTime).getValueSI(), LengthUnit.METER);
        // In IDM+ the reference car must have non-zero speed for the leader speed to have any effect
        initialSpeed = new DoubleScalarRel<SpeedUnit>(2, SpeedUnit.METER_PER_SECOND);
        for (int integerLeaderSpeed = 0; integerLeaderSpeed <= 40; integerLeaderSpeed++)
        {
            referenceCar = new Car(12345, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
            leaders.clear();
            DoubleScalarRel<SpeedUnit> leaderSpeed =
                    new DoubleScalarRel<SpeedUnit>(integerLeaderSpeed, SpeedUnit.METER_PER_SECOND);
            leaderCar = new Car(0, simulator, null, initialTime, leaderPosition, leaderSpeed);
            leaders.add(leaderCar);
            // System.out.println("referenceCar: " + referenceCar);
            // System.out.println("leaderCar   : " + leaderCar);
            cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
            double acceleration = cfmr.acceleration.getValueSI();
            // System.out.println("Acceleration with leader driving " + integerLeaderSpeed + " m/s is " + acceleration);
            assertTrue("acceleration should not decrease when leader speed is increased",
                    acceleration >= referenceAcceleration);
            referenceAcceleration = acceleration;
        }
        assertTrue("Highest acceleration should be less than max", referenceAcceleration <= 1.25);
        // Check that a car that is 100m behind a stationary car accelerates, then decelerates and stops at the right
        // point. (In IDM+ the car oscillates a while around the final position with pretty good damping.)
        initialPosition = new DoubleScalarAbs<LengthUnit>(100, LengthUnit.METER);
        initialSpeed = new DoubleScalarRel<SpeedUnit>(0, SpeedUnit.METER_PER_SECOND);
        referenceCar = new Car(12345, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
        leaders.clear();
        leaderPosition =
                new DoubleScalarAbs<LengthUnit>(100 + 3 + referenceCar.length().getValueSI()
                        + referenceCar.position(initialTime).getValueSI(), LengthUnit.METER);
        leaderCar = new Car(0, simulator, null, initialTime, leaderPosition, initialSpeed);
        leaders.add(leaderCar);
        //System.out.println("Setup    referenceCar: " + referenceCar);
        for (int timeStep = 0; timeStep < 200; timeStep++)
        {
            cfmr = carFollowingModel.computeAcceleration(referenceCar, leaders, speedLimit);
            referenceCar.setState(cfmr);
            //System.out.println(String.format("step %3d referenceCar: %s", timeStep, referenceCar));
            if (timeStep > 100)
            {
                double position = referenceCar.position(cfmr.validUntil).getValueSI();
                assertEquals("After 20 seconds the referenceCar should now be very close to 3m before the rear of the leader", 200, position, 0.1);
                assertEquals("After 20 seconds the speed of the referenceCar should be almost 0", 0, referenceCar.speed(cfmr.validUntil).getValueSI(), 0.2);
            }
        }
    }

}

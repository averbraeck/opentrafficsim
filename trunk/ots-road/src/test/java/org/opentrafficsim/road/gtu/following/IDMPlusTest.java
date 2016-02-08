package org.opentrafficsim.road.gtu.following;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
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
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.car.CarTest;
import org.opentrafficsim.road.car.LaneBasedIndividualCar;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedDrivingCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.lane.tactical.lanechange.AbstractLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechange.Egoistic;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
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
    /** network. */
    private OTSNetwork network = new OTSNetwork("network");

    /**
     * Test IDMPlus.
     * @throws Exception when something goes wrong (should not happen)
     */
    @SuppressWarnings({"static-method", "checkstyle:methodlength"})
    @Test
    public final void computeAcceleration() throws Exception
    {
        // Test 1. Check a car standing still with no leaders accelerates with maximum acceleration
        // cars have #10 and up
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
        Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
        initialLongitudinalPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
        Speed maxSpeed = new Speed(120, KM_PER_HOUR);
        AbstractLaneChangeModel laneChangeModel = new Egoistic();
        LaneBasedDrivingCharacteristics drivingCharacteristicsIDM =
            new LaneBasedDrivingCharacteristics(carFollowingModel, laneChangeModel);
        LaneBasedStrategicalPlanner strategicalPlannerIDM =
            new LaneBasedStrategicalRoutePlanner(drivingCharacteristicsIDM, new LaneBasedGTUFollowingTacticalPlanner());
        LaneBasedIndividualCar referenceCar10 =
            new LaneBasedIndividualCar("10", gtuType, initialLongitudinalPositions, initialSpeed, length, width,
                maxSpeed, simulator, strategicalPlannerIDM, new LanePerceptionFull(), this.network);
        referenceCar10.getPerception().perceive();
        Speed speedLimit = new Speed(100, KM_PER_HOUR);
        AccelerationStep cfmr =
            carFollowingModel.computeAccelerationStepWithNoLeader(referenceCar10, referenceCar10
                .getDrivingCharacteristics().getForwardHeadwayDistance(), speedLimit);
        assertEquals("Standard time slice in IDM+ is 0.5s", 0.5, cfmr.getValidUntil().getSI(), 0.0001);
        assertEquals("Acceleration should be maximum", 1.25, cfmr.getAcceleration().getSI(), 0.0001);
        // Create another car at exactly the stationary following distance
        // Check that the follower remains stationary
        Length.Rel leaderPosition =
            new Length.Rel(2 + referenceCar10.getLength().getSI()
                + referenceCar10.position(lane, referenceCar10.getReference(), initialTime).getSI(), METER);
        Set<DirectedLanePosition> leaderPositions = new LinkedHashSet<>(1);
        leaderPositions.add(new DirectedLanePosition(lane, leaderPosition, GTUDirectionality.DIR_PLUS));

        // The leader gets a car following model that makes it stay in place for a loooong time
        FixedAccelerationModel fam =
            new FixedAccelerationModel(new Acceleration(0, METER_PER_SECOND_2), new Time.Rel(9999, SECOND));
        LaneBasedDrivingCharacteristics drivingCharacteristicsFAM =
            new LaneBasedDrivingCharacteristics(fam, laneChangeModel);
        LaneBasedStrategicalPlanner strategicalPlannerFAM =
            new LaneBasedStrategicalRoutePlanner(drivingCharacteristicsFAM, new LaneBasedGTUFollowingTacticalPlanner());
        LaneBasedIndividualCar leaderCar11 =
            new LaneBasedIndividualCar("11", gtuType, leaderPositions, initialSpeed, length, width, maxSpeed,
                simulator, strategicalPlannerFAM, new LanePerceptionFull(), this.network);
        leaderCar11.getPerception().perceive();
        HeadwayGTU leader =
            new HeadwayGTU(leaderCar11.getId(), leaderCar11.getVelocity(), leaderPosition.getSI()
                - referenceCar10.getLength().getSI() - initialPosition.getSI(), leaderCar11.getGTUType());
        cfmr =
            carFollowingModel.computeAccelerationStep(referenceCar10, leaderCar11.getVelocity(), leader.getDistance(),
                leaderCar11.getDrivingCharacteristics().getForwardHeadwayDistance(), speedLimit);
        assertEquals("Acceleration should be 0", 0, cfmr.getAcceleration().getSI(), 0.0001);
        leaderPosition =
            new Length.Rel(1000 + (3 + referenceCar10.getLength().getSI() + referenceCar10.position(lane,
                referenceCar10.getFront(), initialTime).getSI()), METER);
        leaderPositions = new LinkedHashSet<>(1);
        leaderPositions.add(new DirectedLanePosition(lane, leaderPosition, GTUDirectionality.DIR_PLUS));
        // Exercise the if statement that ignores leaders that are further ahead
        drivingCharacteristicsFAM = new LaneBasedDrivingCharacteristics(fam, laneChangeModel);
        strategicalPlannerFAM =
            new LaneBasedStrategicalRoutePlanner(drivingCharacteristicsFAM, new LaneBasedGTUFollowingTacticalPlanner());
        LaneBasedIndividualCar leaderCar12 =
            new LaneBasedIndividualCar("12", gtuType, leaderPositions, initialSpeed, length, width, maxSpeed,
                simulator, strategicalPlannerFAM, new LanePerceptionFull(), this.network);
        leaderCar12.getPerception().perceive();
        // Verify that the result is independent of the order of adding in the Collection
        Collection<HeadwayGTU> leaders = new ArrayList<HeadwayGTU>();
        HeadwayGTU leader2 =
            new HeadwayGTU(leaderCar12.getId(), leaderCar12.getVelocity(), leaderPosition.getSI()
                - referenceCar10.getLength().getSI() - initialPosition.getSI(), leaderCar12.getGTUType());
        leaders.add(leader2); // Put the 2nd leader in first place
        leaders.add(leader);
        cfmr =
            carFollowingModel.computeDualAccelerationStep(referenceCar10, leaders,
                referenceCar10.getDrivingCharacteristics().getForwardHeadwayDistance(), speedLimit)
                .getLeaderAccelerationStep();
        assertEquals("Acceleration should be 0", 0, cfmr.getAcceleration().getSI(), 0.0001);
        leaders.clear();
        leaders.add(leader); // Put the 1st leader in first place
        leaders.add(leader2);
        cfmr =
            carFollowingModel.computeDualAccelerationStep(referenceCar10, leaders,
                referenceCar10.getDrivingCharacteristics().getForwardHeadwayDistance(), speedLimit)
                .getLeaderAccelerationStep();
        assertEquals("Acceleration should be 0", 0, cfmr.getAcceleration().getSI(), 0.0001);
        referenceCar10.destroy();
        leaderCar11.destroy();
        leaderCar12.destroy();

        // Test 2, cars have #20 and up
        LaneBasedIndividualCar referenceCar20 =
            new LaneBasedIndividualCar("20", gtuType, initialLongitudinalPositions, initialSpeed, length, width,
                maxSpeed, simulator, strategicalPlannerIDM, new LanePerceptionFull(), this.network);
        leaders.clear();
        leaderPosition =
            new Length.Rel(-(3 + referenceCar20.getLength().getSI())
                + referenceCar20.position(lane, referenceCar20.getFront(), initialTime).getSI(), METER);
        leaderPositions = new LinkedHashSet<>(1);
        leaderPositions.add(new DirectedLanePosition(lane, leaderPosition, GTUDirectionality.DIR_PLUS));
        LaneBasedIndividualCar leaderCar21 =
            new LaneBasedIndividualCar("21", gtuType, leaderPositions, initialSpeed, length, width, maxSpeed,
                simulator, strategicalPlannerFAM, new LanePerceptionFull(), this.network);
        referenceCar20.getPerception().perceive();
        leaderCar21.getPerception().perceive();
        leader =
            new HeadwayGTU(leaderCar21.getId(), leaderCar21.getVelocity(), leaderPosition.getSI()
                - referenceCar20.getLength().getSI() - initialPosition.getSI(), leaderCar21.getGTUType());
        leaders.add(leader);
        cfmr =
            carFollowingModel.computeDualAccelerationStep(referenceCar20, leaders,
                referenceCar20.getDrivingCharacteristics().getForwardHeadwayDistance(), speedLimit)
                .getLeaderAccelerationStep();
        assertEquals("Leader acceleration should be 1.25", 1.25, cfmr.getAcceleration().getSI(), 0.0001);
        leaderCar21.destroy();

        // Check that the returned acceleration increases with the distance to the leader
        double referenceAcceleration = -1;
        for (int spareDistance = 0; spareDistance <= 500; spareDistance++)
        {
            leaders.clear();
            leaderPosition =
                new Length.Rel(spareDistance
                    + (3 + referenceCar20.getLength().getSI() + referenceCar20.position(lane,
                        referenceCar20.getFront(), initialTime).getSI()), METER);
            leaderPositions = new LinkedHashSet<>(1);
            leaderPositions.add(new DirectedLanePosition(lane, leaderPosition, GTUDirectionality.DIR_PLUS));
            LaneBasedIndividualCar leaderCar22 =
                new LaneBasedIndividualCar("0", gtuType, leaderPositions, initialSpeed, length, width, maxSpeed,
                    simulator, strategicalPlannerFAM, new LanePerceptionFull(), this.network);
            leaderCar22.getPerception().perceive();
            leader =
                new HeadwayGTU(leaderCar22.getId(), leaderCar22.getVelocity(), leaderPosition.getSI()
                    - referenceCar20.getLength().getSI() - initialPosition.getSI(), leaderCar22.getGTUType());
            leaders.add(leader);
            cfmr =
                carFollowingModel.computeDualAccelerationStep(referenceCar20, leaders,
                    referenceCar20.getDrivingCharacteristics().getForwardHeadwayDistance(), speedLimit)
                    .getFollowerAccelerationStep();
            double acceleration = cfmr.getAcceleration().getSI();
            // System.out.println("Acceleration with stationary leader at " + spareDistance + " is " + acceleration);
            assertTrue("acceleration should not decrease when distance to leader is increased",
                acceleration >= referenceAcceleration);
            referenceAcceleration = acceleration;
            leaderCar22.destroy();
        }
        assertTrue("Highest acceleration should be less than max", referenceAcceleration <= 1.25);
        referenceCar20.destroy();

        // Test 3. Check that the returned acceleration increases with the speed of the leader
        // cars have #30 and up
        LaneBasedIndividualCar referenceCar30 =
            new LaneBasedIndividualCar("30", gtuType, initialLongitudinalPositions, initialSpeed, length, width,
                maxSpeed, simulator, strategicalPlannerIDM, new LanePerceptionFull(), this.network);
        referenceCar30.getPerception().perceive();
        referenceAcceleration = Double.NEGATIVE_INFINITY;
        leaderPosition =
            new Length.Rel(2 + 3 + referenceCar30.getLength().getSI()
                + referenceCar30.position(lane, referenceCar30.getFront(), initialTime).getSI(), METER);
        leaderPositions = new LinkedHashSet<>(1);
        leaderPositions.add(new DirectedLanePosition(lane, leaderPosition, GTUDirectionality.DIR_PLUS));
        // In IDM+ the reference car must have non-zero speed for the leader speed to have any effect
        initialSpeed = new Speed(2, METER_PER_SECOND);
        for (int integerLeaderSpeed = 0; integerLeaderSpeed <= 40; integerLeaderSpeed++)
        {
            Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
            initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
            referenceCar30.destroy();
            drivingCharacteristicsIDM = new LaneBasedDrivingCharacteristics(carFollowingModel, laneChangeModel);
            strategicalPlannerIDM =
                new LaneBasedStrategicalRoutePlanner(drivingCharacteristicsIDM,
                    new LaneBasedGTUFollowingTacticalPlanner());
            referenceCar30 =
                new LaneBasedIndividualCar("30", gtuType, initialPositions, initialSpeed, length, width, maxSpeed,
                    simulator, strategicalPlannerIDM, new LanePerceptionFull(), this.network);
            leaders.clear();
            Speed leaderSpeed = new Speed(integerLeaderSpeed, METER_PER_SECOND);
            drivingCharacteristicsFAM = new LaneBasedDrivingCharacteristics(fam, laneChangeModel);
            strategicalPlannerFAM =
                new LaneBasedStrategicalRoutePlanner(drivingCharacteristicsFAM,
                    new LaneBasedGTUFollowingTacticalPlanner());
            LaneBasedIndividualCar leaderCar31 =
                new LaneBasedIndividualCar("31", gtuType, leaderPositions, leaderSpeed, length, width, maxSpeed,
                    simulator, strategicalPlannerFAM, new LanePerceptionFull(), this.network);
            leaderCar31.getPerception().perceive();
            leader =
                new HeadwayGTU(leaderCar31.getId(), leaderCar31.getVelocity(), leaderPosition.getSI()
                    - referenceCar30.getLength().getSI() - initialPosition.getSI(), leaderCar31.getGTUType());
            leaders.add(leader);
            // System.out.println("referenceCar: " + referenceCar);
            // System.out.println("leaderCar   : " + leaderCar);
            cfmr =
                carFollowingModel.computeDualAccelerationStep(referenceCar30, leaders,
                    referenceCar30.getDrivingCharacteristics().getForwardHeadwayDistance(), speedLimit)
                    .getFollowerAccelerationStep();
            double acceleration = cfmr.getAcceleration().getSI();
            // System.out.println("Acceleration with leader driving " + integerLeaderSpeed + " m/s is " + acceleration);
            assertTrue("acceleration should not decrease when leader speed is increased",
                acceleration >= referenceAcceleration);
            referenceAcceleration = acceleration;
            leaderCar31.destroy();
        }
        assertTrue("Highest acceleration should be less than max", referenceAcceleration <= 1.25);
        referenceCar30.destroy();

        // Test 4. Check that a car that is 100m behind a stationary car accelerates, then decelerates and stops at
        // the right point. (In IDM+ the car oscillates a while around the final position with pretty good damping.)
        // Cars have #40 and up
        initialPosition = new Length.Rel(100, METER);
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
        initialSpeed = new Speed(0, METER_PER_SECOND);
        drivingCharacteristicsIDM = new LaneBasedDrivingCharacteristics(carFollowingModel, laneChangeModel);
        strategicalPlannerIDM =
            new LaneBasedStrategicalRoutePlanner(drivingCharacteristicsIDM, new LaneBasedGTUFollowingTacticalPlanner());
        LaneBasedIndividualCar referenceCar40 =
            new LaneBasedIndividualCar("40", gtuType, initialPositions, initialSpeed, length, width, maxSpeed,
                simulator, strategicalPlannerIDM, new LanePerceptionFull(), this.network);
        referenceCar40.getPerception().perceive();
        leaderPosition =
            new Length.Rel(100 + 3 + referenceCar40.getLength().getSI()
                + referenceCar40.position(lane, referenceCar40.getFront(), initialTime).getSI(), METER);
        drivingCharacteristicsFAM = new LaneBasedDrivingCharacteristics(fam, laneChangeModel);
        strategicalPlannerFAM =
            new LaneBasedStrategicalRoutePlanner(drivingCharacteristicsFAM, new LaneBasedGTUFollowingTacticalPlanner());
        LaneBasedIndividualCar leaderCar41 =
            new LaneBasedIndividualCar("41", gtuType, leaderPositions, initialSpeed, length, width, maxSpeed,
                simulator, strategicalPlannerFAM, new LanePerceptionFull(), this.network);
        leaderCar41.getPerception().perceive();
        for (int timeStep = 0; timeStep < 200; timeStep++)
        {
            Time.Abs simulateUntil = new Time.Abs(0.1 * timeStep, TimeUnit.SI);
            simulator.runUpTo(simulateUntil);
            while (simulator.isRunning())
            {
                try
                {
                    Thread.sleep(1);
                    System.out.println(referenceCar40 + ", t= " + simulator.getSimulatorTime().get() + ", pos="
                        + referenceCar40.getLocation());
                }
                catch (InterruptedException ie)
                {
                    ie = null; // ignore
                }
            }
            // System.out.println(String.format("step %3d, t=%s, referenceCar: %s, speed %s, leaderCar: %s", timeStep,
            // simulateUntil, referenceCar, referenceCar.getVelocity(), leaderCar));
            if (timeStep > 120)
            {
                double position = referenceCar40.position(lane, referenceCar40.getFront()).getSI();
                assertEquals("After 20 seconds the referenceCar should now be very close to " + s0
                    + " before the rear of the leader", leaderCar41.position(lane, referenceCar40.getRear()).getSI()
                    - s0.getSI(), position, 0.2);
                assertEquals("After 20 seconds the speed of the referenceCar should be almost 0", 0, referenceCar40
                    .getVelocity().getSI(), 0.2);
            }
        }
        referenceCar40.destroy();
        leaderCar41.destroy();
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

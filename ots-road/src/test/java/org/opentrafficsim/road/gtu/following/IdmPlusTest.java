package org.opentrafficsim.road.gtu.following;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.car.CarTest;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtuSimple;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGtuFollowingTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.GtuFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.LaneType;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class IdmPlusTest implements UNITS
{
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
        OtsSimulatorInterface simulator = new OtsSimulator("IDMPlusTest");
        OtsRoadNetwork network = new OtsRoadNetwork("IDMPlus test network", simulator);
        IDMPlusTestModel model = new IDMPlusTestModel(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model);
        Length s0 = new Length(2, METER);
        GtuFollowingModelOld carFollowingModel = new IdmPlusOld(new Acceleration(1.25, METER_PER_SECOND_2),
                new Acceleration(1.5, METER_PER_SECOND_2), s0, new Duration(1, SECOND), 1d);
        GtuType gtuType = DefaultsNl.CAR;
        LaneType laneType = DefaultsRoadNl.TWO_WAY_LANE;
        Lane lane = CarTest.makeLane(network, laneType, simulator);
        Time initialTime = new Time(0, TimeUnit.BASE_SECOND);
        Length initialPosition = new Length(123.456, METER);
        Speed initialSpeed = new Speed(0, KM_PER_HOUR);
        Length length = new Length(5.0, METER);
        Length width = new Length(2.0, METER);
        Set<LanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
        initialLongitudinalPositions.add(new LanePosition(lane, initialPosition));
        Speed maxSpeed = new Speed(120, KM_PER_HOUR);
        // AbstractLaneChangeModel laneChangeModel = new Egoistic();
        Parameters parametersIDM = DefaultTestParameters.create();

        // fixed value for consistent testing even if the default value is changed
        Length lookAhead = new Length(250, LengthUnit.SI);
        // LaneBasedBehavioralCharacteristics drivingCharacteristicsIDM =
        // new LaneBasedBehavioralCharacteristics(carFollowingModel, laneChangeModel);
        LaneBasedGtu referenceCar10 =
                new LaneBasedGtu("10", gtuType, length, width, maxSpeed, length.times(0.5), network);
        LaneBasedStrategicalPlanner strategicalPlannerIDM = new LaneBasedStrategicalRoutePlanner(
                new LaneBasedGtuFollowingTacticalPlanner(carFollowingModel, referenceCar10), referenceCar10);
        referenceCar10.setParameters(parametersIDM);
        referenceCar10.init(strategicalPlannerIDM, initialLongitudinalPositions, initialSpeed);
        referenceCar10.getTacticalPlanner().getPerception().perceive();
        Speed speedLimit = new Speed(100, KM_PER_HOUR);
        AccelerationStep cfmr = carFollowingModel.computeAccelerationStepWithNoLeader(referenceCar10, lookAhead, speedLimit);
        assertEquals("Standard time slice in IDM+ is 0.5s", 0.5, cfmr.getValidUntil().getSI(), 0.0001);
        assertEquals("Acceleration should be maximum", 1.25, cfmr.getAcceleration().getSI(), 0.0001);
        // Create another car at exactly the stationary following distance
        // Check that the follower remains stationary
        Length leaderPosition = new Length(2 + referenceCar10.getLength().getSI()
                + referenceCar10.position(lane, referenceCar10.getReference(), initialTime).getSI(), METER);
        Set<LanePosition> leaderPositions = new LinkedHashSet<>(1);
        leaderPositions.add(new LanePosition(lane, leaderPosition));

        // The leader gets a car following model that makes it stay in place for a loooong time
        // edit wouter schakel: not too long, cars should not exceed the lane length, i.e. 9999 to 999
        FixedAccelerationModel fam =
                new FixedAccelerationModel(new Acceleration(0, METER_PER_SECOND_2), new Duration(999, SECOND));
        Parameters parametersFAM = DefaultTestParameters.create();

        // LaneBasedBehavioralCharacteristics drivingCharacteristicsFAM =
        // new LaneBasedBehavioralCharacteristics(fam, laneChangeModel);

        LaneBasedGtu leaderCar11 =
                new LaneBasedGtu("11", gtuType, length, width, maxSpeed, length.times(0.5), network);
        LaneBasedStrategicalPlanner strategicalPlannerFAM =
                new LaneBasedStrategicalRoutePlanner(new LaneBasedGtuFollowingTacticalPlanner(fam, leaderCar11), leaderCar11);
        leaderCar11.setParameters(parametersFAM);
        leaderCar11.init(strategicalPlannerFAM, leaderPositions, initialSpeed);
        leaderCar11.getTacticalPlanner().getPerception().perceive();
        HeadwayGtuSimple leader = new HeadwayGtuSimple(leaderCar11.getId(), leaderCar11.getType(),
                new Length(leaderPosition.getSI() - referenceCar10.getLength().getSI() - initialPosition.getSI(),
                        LengthUnit.SI),
                leaderCar11.getLength(), leaderCar11.getWidth(), leaderCar11.getSpeed(), leaderCar11.getAcceleration(), null);
        cfmr = carFollowingModel.computeAccelerationStep(referenceCar10, leaderCar11.getSpeed(), leader.getDistance(),
                lookAhead, speedLimit);
        assertEquals("Acceleration should be 0", 0, cfmr.getAcceleration().getSI(), 0.0001);
        leaderPosition = new Length(1000 + (3 + referenceCar10.getLength().getSI()
                + referenceCar10.position(lane, referenceCar10.getFront(), initialTime).getSI()), METER);
        leaderPositions = new LinkedHashSet<>(1);
        leaderPositions.add(new LanePosition(lane, leaderPosition));
        // Exercise the if statement that ignores leaders that are further ahead
        parametersFAM = DefaultTestParameters.create();
        // drivingCharacteristicsFAM = new LaneBasedBehavioralCharacteristics(fam, laneChangeModel);
        LaneBasedGtu leaderCar12 =
                new LaneBasedGtu("12", gtuType, length, width, maxSpeed, length.times(0.5), network);
        strategicalPlannerFAM =
                new LaneBasedStrategicalRoutePlanner(new LaneBasedGtuFollowingTacticalPlanner(fam, leaderCar12), leaderCar12);
        leaderCar12.setParameters(parametersFAM);
        leaderCar12.init(strategicalPlannerFAM, leaderPositions, initialSpeed);
        leaderCar12.getTacticalPlanner().getPerception().perceive();
        // Verify that the result is independent of the order of adding in the Collection
        Collection<Headway> leaders = new ArrayList<>();
        HeadwayGtuSimple leader2 = new HeadwayGtuSimple(leaderCar12.getId(), leaderCar12.getType(),
                new Length(leaderPosition.getSI() - referenceCar10.getLength().getSI() - initialPosition.getSI(),
                        LengthUnit.SI),
                leaderCar12.getLength(), leaderCar12.getWidth(), leaderCar12.getSpeed(), leaderCar12.getAcceleration(), null);
        leaders.add(leader2); // Put the 2nd leader in first place
        leaders.add(leader);
        cfmr = carFollowingModel.computeDualAccelerationStep(referenceCar10, leaders, lookAhead, speedLimit)
                .getLeaderAccelerationStep();
        assertEquals("Acceleration should be 0", 0, cfmr.getAcceleration().getSI(), 0.0001);
        leaders.clear();
        leaders.add(leader); // Put the 1st leader in first place
        leaders.add(leader2);
        cfmr = carFollowingModel.computeDualAccelerationStep(referenceCar10, leaders, lookAhead, speedLimit)
                .getLeaderAccelerationStep();
        assertEquals("Acceleration should be 0", 0, cfmr.getAcceleration().getSI(), 0.0001);
        referenceCar10.destroy();
        leaderCar11.destroy();
        leaderCar12.destroy();

        // Test 2, cars have #20 and up
        LaneBasedGtu referenceCar20 =
                new LaneBasedGtu("20", gtuType, length, width, maxSpeed, length.times(0.5), network);
        strategicalPlannerIDM = new LaneBasedStrategicalRoutePlanner(
                new LaneBasedGtuFollowingTacticalPlanner(carFollowingModel, referenceCar20), referenceCar20);
        referenceCar20.setParameters(parametersIDM);
        referenceCar20.init(strategicalPlannerIDM, initialLongitudinalPositions, initialSpeed);
        leaders.clear();
        leaderPosition = new Length(-(3 + referenceCar20.getLength().getSI())
                + referenceCar20.position(lane, referenceCar20.getFront(), initialTime).getSI(), METER);
        leaderPositions = new LinkedHashSet<>(1);
        leaderPositions.add(new LanePosition(lane, leaderPosition));
        LaneBasedGtu leaderCar21 =
                new LaneBasedGtu("21", gtuType, length, width, maxSpeed, length.times(0.5), network);
        strategicalPlannerFAM =
                new LaneBasedStrategicalRoutePlanner(new LaneBasedGtuFollowingTacticalPlanner(fam, leaderCar21), leaderCar21);
        leaderCar21.setParameters(parametersFAM);
        leaderCar21.init(strategicalPlannerFAM, leaderPositions, initialSpeed);
        referenceCar20.getTacticalPlanner().getPerception().perceive();
        leaderCar21.getTacticalPlanner().getPerception().perceive();
        leader = new HeadwayGtuSimple(leaderCar21.getId(), leaderCar21.getType(),
                new Length(leaderPosition.getSI() - referenceCar20.getLength().getSI() - initialPosition.getSI(),
                        LengthUnit.SI),
                leaderCar21.getLength(), leaderCar21.getWidth(), leaderCar21.getSpeed(), leaderCar21.getAcceleration(), null);
        leaders.add(leader);
        cfmr = carFollowingModel.computeDualAccelerationStep(referenceCar20, leaders, lookAhead, speedLimit)
                .getLeaderAccelerationStep();
        assertEquals("Leader acceleration should be 1.25", 1.25, cfmr.getAcceleration().getSI(), 0.0001);
        leaderCar21.destroy();

        // Check that the returned acceleration increases with the distance to the leader
        double referenceAcceleration = -1;
        for (int spareDistance = 0; spareDistance <= 500; spareDistance++)
        {
            leaders.clear();
            leaderPosition = new Length(spareDistance + (3 + referenceCar20.getLength().getSI()
                    + referenceCar20.position(lane, referenceCar20.getFront(), initialTime).getSI()), METER);
            leaderPositions = new LinkedHashSet<>(1);
            leaderPositions.add(new LanePosition(lane, leaderPosition));
            LaneBasedGtu leaderCar22 =
                    new LaneBasedGtu("0", gtuType, length, width, maxSpeed, length.times(0.5), network);
            strategicalPlannerFAM = new LaneBasedStrategicalRoutePlanner(
                    new LaneBasedGtuFollowingTacticalPlanner(fam, leaderCar22), leaderCar22);
            leaderCar22.setParameters(parametersFAM);
            leaderCar22.init(strategicalPlannerFAM, leaderPositions, initialSpeed);
            leaderCar22.getTacticalPlanner().getPerception().perceive();
            leader = new HeadwayGtuSimple(leaderCar22.getId(), leaderCar22.getType(),
                    new Length(leaderPosition.getSI() - referenceCar20.getLength().getSI() - initialPosition.getSI(),
                            LengthUnit.SI),
                    leaderCar22.getLength(), leaderCar22.getWidth(), leaderCar22.getSpeed(), leaderCar22.getAcceleration(),
                    null);
            leaders.add(leader);
            cfmr = carFollowingModel.computeDualAccelerationStep(referenceCar20, leaders, lookAhead, speedLimit)
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
        LaneBasedGtu referenceCar30 =
                new LaneBasedGtu("30", gtuType, length, width, maxSpeed, length.times(0.5), network);
        strategicalPlannerIDM = new LaneBasedStrategicalRoutePlanner(
                new LaneBasedGtuFollowingTacticalPlanner(carFollowingModel, referenceCar30), referenceCar30);
        referenceCar30.setParameters(parametersIDM);
        referenceCar30.init(strategicalPlannerIDM, initialLongitudinalPositions, initialSpeed);
        referenceCar30.getTacticalPlanner().getPerception().perceive();
        referenceAcceleration = Double.NEGATIVE_INFINITY;
        leaderPosition = new Length(2 + 3 + referenceCar30.getLength().getSI()
                + referenceCar30.position(lane, referenceCar30.getFront(), initialTime).getSI(), METER);
        leaderPositions = new LinkedHashSet<>(1);
        leaderPositions.add(new LanePosition(lane, leaderPosition));
        // In IDM+ the reference car must have non-zero speed for the leader speed to have any effect
        initialSpeed = new Speed(2, METER_PER_SECOND);
        for (int integerLeaderSpeed = 0; integerLeaderSpeed <= 40; integerLeaderSpeed++)
        {
            Set<LanePosition> initialPositions = new LinkedHashSet<>(1);
            initialPositions.add(new LanePosition(lane, initialPosition));
            referenceCar30.destroy();
            // parametersIDM = new BehavioralCharacteristics();
            // drivingCharacteristicsIDM = new LaneBasedBehavioralCharacteristics(carFollowingModel, laneChangeModel);
            referenceCar30 =
                    new LaneBasedGtu("30", gtuType, length, width, maxSpeed, length.times(0.5), network);
            strategicalPlannerIDM = new LaneBasedStrategicalRoutePlanner(
                    new LaneBasedGtuFollowingTacticalPlanner(carFollowingModel, referenceCar30), referenceCar30);
            referenceCar30.setParameters(parametersIDM);
            referenceCar30.init(strategicalPlannerIDM, initialPositions, initialSpeed);
            leaders.clear();
            Speed leaderSpeed = new Speed(integerLeaderSpeed, METER_PER_SECOND);
            parametersFAM = DefaultTestParameters.create(); // new BehavioralCharacteristics();
            // drivingCharacteristicsFAM = new LaneBasedBehavioralCharacteristics(fam, laneChangeModel);
            LaneBasedGtu leaderCar31 =
                    new LaneBasedGtu("31", gtuType, length, width, maxSpeed, length.times(0.5), network);
            strategicalPlannerFAM = new LaneBasedStrategicalRoutePlanner(
                    new LaneBasedGtuFollowingTacticalPlanner(fam, leaderCar31), leaderCar31);
            leaderCar31.setParameters(parametersFAM);
            leaderCar31.init(strategicalPlannerFAM, leaderPositions, leaderSpeed);
            leaderCar31.getTacticalPlanner().getPerception().perceive();
            leader = new HeadwayGtuSimple(leaderCar31.getId(), leaderCar31.getType(),
                    new Length(leaderPosition.getSI() - referenceCar30.getLength().getSI() - initialPosition.getSI(),
                            LengthUnit.SI),
                    leaderCar31.getLength(), leaderCar31.getWidth(), leaderCar31.getSpeed(), leaderCar31.getAcceleration(),
                    null);
            leaders.add(leader);
            // System.out.println("referenceCar: " + referenceCar);
            // System.out.println("leaderCar : " + leaderCar);
            cfmr = carFollowingModel.computeDualAccelerationStep(referenceCar30, leaders, lookAhead, speedLimit)
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
        initialPosition = new Length(100, METER);
        Set<LanePosition> initialPositions = new LinkedHashSet<>(1);
        initialPositions.add(new LanePosition(lane, initialPosition));
        initialSpeed = new Speed(0, METER_PER_SECOND);
        parametersIDM = DefaultTestParameters.create(); // new BehavioralCharacteristics();
        // drivingCharacteristicsIDM = new LaneBasedBehavioralCharacteristics(carFollowingModel, laneChangeModel);
        LaneBasedGtu referenceCar40 =
                new LaneBasedGtu("40", gtuType, length, width, maxSpeed, length.times(0.5), network);
        strategicalPlannerIDM = new LaneBasedStrategicalRoutePlanner(
                new LaneBasedGtuFollowingTacticalPlanner(carFollowingModel, referenceCar40), referenceCar40);
        referenceCar40.setParameters(parametersIDM);
        referenceCar40.init(strategicalPlannerIDM, initialPositions, initialSpeed);
        referenceCar40.getTacticalPlanner().getPerception().perceive();
        leaderPosition = new Length(100 + 3 + referenceCar40.getLength().getSI()
                + referenceCar40.position(lane, referenceCar40.getFront(), initialTime).getSI(), METER);
        parametersFAM = DefaultTestParameters.create(); // new BehavioralCharacteristics();
        // drivingCharacteristicsFAM = new LaneBasedBehavioralCharacteristics(fam, laneChangeModel);
        LaneBasedGtu leaderCar41 =
                new LaneBasedGtu("41", gtuType, length, width, maxSpeed, length.times(0.5), network);
        strategicalPlannerFAM =
                new LaneBasedStrategicalRoutePlanner(new LaneBasedGtuFollowingTacticalPlanner(fam, leaderCar41), leaderCar41);
        leaderCar41.setParameters(parametersFAM);
        leaderCar41.init(strategicalPlannerFAM, leaderPositions, initialSpeed);
        leaderCar41.getTacticalPlanner().getPerception().perceive();
        for (int timeStep = 0; timeStep < 200; timeStep++)
        {
            Time simulateUntil = new Time(0.1 * timeStep, TimeUnit.BASE_SECOND);
            simulator.runUpTo(simulateUntil);
            while (simulator.isStartingOrRunning())
            {
                try
                {
                    Thread.sleep(1);
                    // System.out.println(referenceCar40 + ", t= " + simulator.getSimulatorTime() + ", pos="
                    // + referenceCar40.getLocation());
                }
                catch (InterruptedException ie)
                {
                    ie = null; // ignore
                }
            }
            // System.out.println(String.format("step %3d, t=%s, referenceCar: %s, speed %s, leaderCar: %s", timeStep,
            // simulateUntil, referenceCar, referenceCar.getSpeed(), leaderCar));
            if (timeStep > 120)
            {
                double position = referenceCar40.position(lane, referenceCar40.getFront()).getSI();
                assertEquals(
                        "After 20 seconds the referenceCar should now be very close to " + s0
                                + " before the rear of the leader",
                        leaderCar41.position(lane, referenceCar40.getRear()).getSI() - s0.getSI(), position, 0.2);
                assertEquals("After 20 seconds the speed of the referenceCar should be almost 0", 0,
                        referenceCar40.getSpeed().getSI(), 0.2);
            }
        }
        referenceCar40.destroy();
        leaderCar41.destroy();
    }

}

/**
 * Simulation model for IDMPlusTest.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
class IDMPlusTestModel extends AbstractOtsModel
{

    /** */
    private static final long serialVersionUID = 20150210L;

    /**
     * @param simulator the simulator to use
     */
    IDMPlusTestModel(final OtsSimulatorInterface simulator)
    {
        super(simulator);
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel() throws SimRuntimeException
    {
        // do nothing.
    }

    /** {@inheritDoc} */
    @Override
    public OtsRoadNetwork getNetwork()
    {
        return null;
    }

}

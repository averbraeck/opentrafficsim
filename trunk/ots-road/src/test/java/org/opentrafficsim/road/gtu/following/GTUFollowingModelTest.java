package org.opentrafficsim.road.gtu.following;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
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
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.road.car.CarTest;
import org.opentrafficsim.road.car.LaneBasedIndividualCar;
import org.opentrafficsim.road.gtu.lane.changing.AbstractLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.changing.Egoistic;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.route.CompleteLaneBasedRouteNavigator;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * Test the methods that the classes that implement GTUFollowingModel have in common.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version 27 feb. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUFollowingModelTest implements OTSModelInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 20150227L;

    /**
     * Test that the methods required by the GTUFollowingModel interface.
     * @param gtuFollowingModel GTUFollowingModel
     * @throws Exception when something goes wrong (should not happen)
     */
    private void gtuFollowingModelTests(GTUFollowingModel gtuFollowingModel) throws Exception
    {
        Acceleration maxSafeDeceleration = gtuFollowingModel.maximumSafeDeceleration();
        assertNotNull("maximumSafeDeceleration must return non-null value", maxSafeDeceleration);
        assertTrue("value of maximuSafeDeceleration must be positive", 0 < maxSafeDeceleration.getSI());
        assertTrue("value of maximumSafeDeceleration must be less than g", maxSafeDeceleration.getSI() < 10);
        Time.Rel stepSize = gtuFollowingModel.getStepSize();
        assertNotNull("stepSize must return non-null value", stepSize);
        assertTrue("stepSize must be > 0", 0 < stepSize.getSI());
        String name = gtuFollowingModel.getName();
        // System.out.println("GTUFollowingModel " + name);
        assertNotNull("getName must return non-null value", name);
        assertTrue("getName result must not be the empty string", name.length() > 0);
        String longName = gtuFollowingModel.getLongName();
        assertNotNull("getLongName must return non-null value", longName);
        assertTrue("getLongName result must not be the empty string", longName.length() > 0);
        Speed speed = new Speed(0, SpeedUnit.SI);
        Length.Rel precision = new Length.Rel(0.5, METER);
        Speed maxSpeed = new Speed(200, KM_PER_HOUR);
        Speed speedLimit = new Speed(100, KM_PER_HOUR);
        Length.Rel minimumHeadway = gtuFollowingModel.minimumHeadway(speed, speed, precision, speedLimit, maxSpeed);
        assertNotNull("minimum headway at speed 0 should be non null", minimumHeadway);
        assertTrue("minimum headway at speed 0 hould have value >= 0", 0 <= minimumHeadway.getSI());
        // System.out.println("minimum headway at speed " + speed + " is " + minimumHeadway);
        speed = new Speed(50, KM_PER_HOUR);
        minimumHeadway = gtuFollowingModel.minimumHeadway(speed, speed, precision, speedLimit, maxSpeed);
        assertNotNull("minimum headway at speed 0 should be non null", minimumHeadway);
        assertTrue("minimum headway at speed 0 hould have value >= 0", 0 <= minimumHeadway.getSI());
        // System.out.println("minimum headway at speed " + speed + " is " + minimumHeadway);
        SimpleSimulator simulator =
            new SimpleSimulator(new Time.Abs(0, SECOND), new Time.Rel(0, SECOND), new Time.Rel(1800, SECOND), this);
        GTUType carType = GTUType.makeGTUType("Car");
        LaneType laneType = new LaneType("CarLane");
        laneType.addCompatibility(carType);
        Lane lane = CarTest.makeLane(laneType);
        Length.Rel initialPosition = new Length.Rel(1234.567, METER);
        Length.Rel length = new Length.Rel(5.0, METER);
        Length.Rel width = new Length.Rel(2.0, METER);
        Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
        initialLongitudinalPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
        AbstractLaneChangeModel laneChangeModel = new Egoistic();
        LaneBasedIndividualCar gtu =
            new LaneBasedIndividualCar("12345", carType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositions, speed, length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(
                    new CompleteRoute("", GTUType.ALL)), simulator);
        Length.Rel longerHeadway = minimumHeadway.plus(precision);
        Acceleration longerHeadwayAcceleration =
            gtuFollowingModel.computeAcceleration(speed, maxSpeed, speed, longerHeadway, speedLimit);
        // System.out.println("acceleration at headway " + longerHeadway + " is " + longerHeadwayAcceleration);
        assertTrue("deceleration with longer headway than minimum should be >= -maximumSafeDeceleration",
            -maxSafeDeceleration.getSI() <= longerHeadwayAcceleration.getSI());
        Length.Rel shorterHeadway = minimumHeadway.minus(precision);
        Acceleration shorterHeadwayAcceleration =
            gtuFollowingModel.computeAcceleration(speed, maxSpeed, speed, shorterHeadway, speedLimit);
        // System.out.println("acceleration at headway " + shorterHeadway + " is " + shorterHeadwayAcceleration);
        gtuFollowingModel.computeAcceleration(speed, maxSpeed, speed, shorterHeadway, speedLimit);
        assertTrue("deceleration with longer headway than minimum should be <= -maximumSafeDeceleration",
            -maxSafeDeceleration.getSI() >= shorterHeadwayAcceleration.getSI());
        AccelerationStep noLeader = gtuFollowingModel.computeAccelerationWithNoLeader(gtu, speedLimit);
        // System.out.println("noLeader is " + noLeader);
        assertNotNull("result of computeAccelerationWithNoLeader is not null", noLeader);
        assertEquals("result of computeAccelerationWithNoLeader is valid for " + stepSize, stepSize.getSI(), noLeader
            .getValidUntil().getSI(), 0.001);
        assertTrue("acceleration of stationary gtu with no leader should be > 0", 0 < noLeader.getAcceleration()
            .getSI());
        precision = new Length.Rel(0, LengthUnit.SI);
        try
        {
            gtuFollowingModel.minimumHeadway(speed, speed, precision, speedLimit, maxSpeed);
            fail("precision 0 should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        precision = new Length.Rel(-1, LengthUnit.SI);
        try
        {
            gtuFollowingModel.minimumHeadway(speed, speed, precision, speedLimit, maxSpeed);
            fail("precision -1 should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        Length.Rel headway50m = new Length.Rel(50, METER);
        Set<DirectedLanePosition> initialLongitudinalPositions50 = new LinkedHashSet<>(1);
        initialLongitudinalPositions.add(new DirectedLanePosition(lane, initialPosition.plus(headway50m),
            GTUDirectionality.DIR_PLUS));

        LaneBasedIndividualCar gtu50m =
            new LaneBasedIndividualCar("100050", carType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositions50, speed, length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(
                    new CompleteRoute("", GTUType.ALL)), simulator);
        HeadwayGTU hwgtu50m = new HeadwayGTU(gtu50m, headway50m.getSI());
        Collection<HeadwayGTU> otherGTUs = new ArrayList<HeadwayGTU>();
        DualAccelerationStep asEmpty = gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        // System.out.println("asEmpty: [" + asEmpty[0] + ", " + asEmpty[1] + "]");
        Time.Abs expectedValidUntil = new Time.Abs(stepSize.getSI(), TimeUnit.SI);
        checkAccelerationStep("Empty collection", asEmpty, noLeader.getAcceleration(), noLeader.getAcceleration(),
            expectedValidUntil);
        otherGTUs.add(new HeadwayGTU(gtu, java.lang.Double.NaN));
        // If otherGTUs only contains the reference GTU, the result should be exactly the same
        asEmpty = gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        checkAccelerationStep("Empty collection", asEmpty, noLeader.getAcceleration(), noLeader.getAcceleration(),
            expectedValidUntil);
        otherGTUs.clear();
        otherGTUs.add(hwgtu50m);
        DualAccelerationStep as50m = gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        // System.out.println("as50m: [" + as50m[0] + ", " + as50m[1] + "]");
        AccelerationStep a50 =
            gtuFollowingModel.computeAcceleration(gtu50m, gtu50m.getLongitudinalVelocity(), headway50m, speedLimit);
        checkAccelerationStep("leader at " + headway50m, as50m, a50.getAcceleration(), noLeader.getAcceleration(),
            expectedValidUntil);
        Map<Lane, Length.Rel> initialLongitudinalPositions100 = new HashMap<>();
        Length.Rel headway100m = new Length.Rel(100, METER);
        initialLongitudinalPositions100.put(lane, initialPosition.plus(headway100m));
        LaneBasedIndividualCar gtu100m =
            new LaneBasedIndividualCar("100100", carType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositions50, speed, length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(
                    new CompleteRoute("", GTUType.ALL)), simulator);
        HeadwayGTU hwgtu100m = new HeadwayGTU(gtu100m, headway100m.getSI());
        otherGTUs.add(hwgtu100m);
        DualAccelerationStep as50and100m = gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        checkAccelerationStep("leader at " + headway50m + " and at " + headway100m, as50and100m, a50.getAcceleration(),
            noLeader.getAcceleration(), expectedValidUntil);
        otherGTUs.add(new HeadwayGTU(gtu, 0));
        as50and100m = gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        checkAccelerationStep("follower at 0, leader at " + headway50m + " and at " + headway100m, as50and100m, a50
            .getAcceleration(), noLeader.getAcceleration(), expectedValidUntil);
        otherGTUs.add(new HeadwayGTU(gtu, java.lang.Double.NaN));
        as50and100m = gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        checkAccelerationStep("follower at NaN, leader at " + headway50m + " and at " + headway100m, as50and100m, a50
            .getAcceleration(), noLeader.getAcceleration(), expectedValidUntil);
        otherGTUs.clear();
        otherGTUs.add(hwgtu100m);
        DualAccelerationStep as100m = gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        AccelerationStep a100 =
            gtuFollowingModel.computeAcceleration(gtu, gtu100m.getLongitudinalVelocity(), headway100m, speedLimit);
        // System.out.println("a100: " + a100);
        // System.out.println("as100m: [" + as100m[0] + ", " + as100m[1] + "]");
        checkAccelerationStep("leader at " + headway100m, as100m, a100.getAcceleration(), noLeader.getAcceleration(),
            expectedValidUntil);
        // Add an overlapping GTU. Immediate collision situation should return TOODANGEROUS
        Map<Lane, Length.Rel> initialLongitudinalPositionsOverlapping = new HashMap<>();
        initialLongitudinalPositionsOverlapping.put(lane, initialPosition.plus(new Length.Rel(1, METER)));
        LaneBasedIndividualCar gtu1m =
            new LaneBasedIndividualCar("100100", carType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositions50, speed, length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(
                    new CompleteRoute("", GTUType.ALL)), simulator);
        HeadwayGTU hwgtu1m = new HeadwayGTU(gtu1m, java.lang.Double.NaN);
        otherGTUs.add(hwgtu1m);
        DualAccelerationStep as1m = gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        AccelerationStep a1 = AbstractGTUFollowingModel.TOODANGEROUS.getLeaderAccelerationStep();
        // System.out.println("a1: " + a1);
        // System.out.println("as1m: [" + as1m[0] + ", " + as1m[1] + "]");
        checkAccelerationStep("leader overlapping ", as1m, a1.getAcceleration(), a1.getAcceleration(), a1
            .getValidUntil());
        otherGTUs.clear();
        otherGTUs.add(hwgtu100m);
        // Follower at 75m
        Length.Rel headwayMinus75m = new Length.Rel(-75, METER);
        Set<DirectedLanePosition> initialLongitudinalPositionsMinus75 = new LinkedHashSet<>(1);
        initialLongitudinalPositions.add(new DirectedLanePosition(lane, initialPosition.plus(headwayMinus75m),
            GTUDirectionality.DIR_PLUS));

        LaneBasedIndividualCar gtuMinus75m =
            new LaneBasedIndividualCar("100075", carType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositionsMinus75, speed, length, width, maxSpeed,
                new CompleteLaneBasedRouteNavigator(new CompleteRoute("", GTUType.ALL)), simulator);
        HeadwayGTU hwgtuMinus75m = new HeadwayGTU(gtuMinus75m, headwayMinus75m.getSI());
        otherGTUs.add(hwgtuMinus75m);
        DualAccelerationStep asMinus75And100m = gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        AccelerationStep a75 =
            gtuFollowingModel.computeAcceleration(gtuMinus75m, gtu.getLongitudinalVelocity(), new Length.Rel(Math
                .abs(headwayMinus75m.getSI()), LengthUnit.SI), speedLimit);
        checkAccelerationStep("leader at " + headway100m + " and follower at " + headwayMinus75m, asMinus75And100m,
            a100.getAcceleration(), a75.getAcceleration(), expectedValidUntil);
        // Another follower at 200m
        Length.Rel headwayMinus200m = new Length.Rel(-200, METER);
        Set<DirectedLanePosition> initialLongitudinalPositionsMinus200 = new LinkedHashSet<>(1);
        initialLongitudinalPositions.add(new DirectedLanePosition(lane, initialPosition.plus(headwayMinus200m),
            GTUDirectionality.DIR_PLUS));

        LaneBasedIndividualCar gtuMinus200m =
            new LaneBasedIndividualCar("100200", carType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositionsMinus200, speed, length, width, maxSpeed,
                new CompleteLaneBasedRouteNavigator(new CompleteRoute("", GTUType.ALL)), simulator);
        HeadwayGTU hwgtuMinus200m = new HeadwayGTU(gtuMinus200m, headwayMinus200m.getSI());
        otherGTUs.add(hwgtuMinus200m);
        DualAccelerationStep asMinus200Minus75And100m =
            gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        // The extra follower at -200 should not make a difference
        checkAccelerationStep("leader at " + headway100m + " and follower at " + headwayMinus75m,
            asMinus200Minus75And100m, a100.getAcceleration(), a75.getAcceleration(), expectedValidUntil);
    }

    /**
     * Verify a result of computeAcceleration.
     * @param description String; description of the result to check
     * @param as AccelerationStep[2]; the result to verify
     * @param a0 DoubleScalar.Abs&lt;AccelerationUnit&gt;; the expected acceleration in as[0]
     * @param a1 DoubleScalar.Abs&lt;AccelerationUnit&gt;; the expected acceleration in as[1]
     * @param validUntil DoubleScalar.Abs&lt;TimeUnit&gt;; the expected validUntil value in both entries of as
     */
    private void checkAccelerationStep(final String description, final DualAccelerationStep as, final Acceleration a0,
        final Acceleration a1, final Time.Abs validUntil)
    {
        assertEquals(description + ": a leader should be " + a0, a0.getSI(), as.getLeaderAcceleration().getSI(), 0.001);
        assertEquals(description + ": a leader should be " + a0, a0.getSI(), as.getLeaderAccelerationStep()
            .getAcceleration().getSI(), 0.001);
        assertEquals(description + ": a leader should be valid until " + validUntil, validUntil.getSI(), as
            .getLeaderValidUntil().getSI(), 0.001);
        assertEquals(description + ": a leader should be valid until " + validUntil, validUntil.getSI(), as
            .getLeaderAccelerationStep().getValidUntil().getSI(), 0.001);
        assertEquals(description + ": a follower should be " + a1, a1.getSI(), as.getFollowerAcceleration().getSI(),
            0.001);
        assertEquals(description + ": a follower should be " + a1, a1.getSI(), as.getFollowerAccelerationStep()
            .getAcceleration().getSI(), 0.001);
        assertEquals(description + ": a follower should be valid until " + validUntil, validUntil.getSI(), as
            .getFollowerValidUntil().getSI(), 0.001);
        assertEquals(description + ": a follower should be valid until " + validUntil, validUntil.getSI(), as
            .getFollowerValidUntil().getSI(), 0.001);
    }

    /**
     * Test IDM
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void testIDM() throws Exception
    {
        gtuFollowingModelTests(new IDM());
    }

    /**
     * Test IDMPlus
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void testIDMPlus() throws Exception
    {
        gtuFollowingModelTests(new IDMPlus());
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(
        SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator)
        throws SimRuntimeException
    {
        // Do nothing.
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()

    {
        return null;
    }
}

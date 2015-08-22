package org.opentrafficsim.core.gtu.following;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.junit.Test;
import org.opentrafficsim.core.car.CarTest;
import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.lane.changing.AbstractLaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.route.CompleteLaneBasedRouteNavigator;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * Test the methods that the classes that implement GTUFollowingModel have in common.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 27 feb. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUFollowingModelTest implements OTSModelInterface
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
        DoubleScalar.Abs<AccelerationUnit> maxSafeDeceleration = gtuFollowingModel.maximumSafeDeceleration();
        assertNotNull("maximumSafeDeceleration must return non-null value", maxSafeDeceleration);
        assertTrue("value of maximuSafeDeceleration must be positive", 0 < maxSafeDeceleration.getSI());
        assertTrue("value of maximumSafeDeceleration must be less than g", maxSafeDeceleration.getSI() < 10);
        DoubleScalar.Rel<TimeUnit> stepSize = gtuFollowingModel.getStepSize();
        assertNotNull("stepSize must return non-null value", stepSize);
        assertTrue("stepSize must be > 0", 0 < stepSize.getSI());
        String name = gtuFollowingModel.getName();
        // System.out.println("GTUFollowingModel " + name);
        assertNotNull("getName must return non-null value", name);
        assertTrue("getName result must not be the empty string", name.length() > 0);
        String longName = gtuFollowingModel.getLongName();
        assertNotNull("getLongName must return non-null value", longName);
        assertTrue("getLongName result must not be the empty string", longName.length() > 0);
        DoubleScalar.Abs<SpeedUnit> speed = new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.SI);
        DoubleScalar.Rel<LengthUnit> precision = new DoubleScalar.Rel<LengthUnit>(0.5, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> maxSpeed = new DoubleScalar.Abs<SpeedUnit>(200, SpeedUnit.KM_PER_HOUR);
        DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        DoubleScalar.Rel<LengthUnit> minimumHeadway =
            gtuFollowingModel.minimumHeadway(speed, speed, precision, speedLimit, maxSpeed);
        assertNotNull("minimum headway at speed 0 should be non null", minimumHeadway);
        assertTrue("minimum headway at speed 0 hould have value >= 0", 0 <= minimumHeadway.getSI());
        // System.out.println("minimum headway at speed " + speed + " is " + minimumHeadway);
        speed = new DoubleScalar.Abs<SpeedUnit>(50, SpeedUnit.KM_PER_HOUR);
        minimumHeadway = gtuFollowingModel.minimumHeadway(speed, speed, precision, speedLimit, maxSpeed);
        assertNotNull("minimum headway at speed 0 should be non null", minimumHeadway);
        assertTrue("minimum headway at speed 0 hould have value >= 0", 0 <= minimumHeadway.getSI());
        // System.out.println("minimum headway at speed " + speed + " is " + minimumHeadway);
        SimpleSimulator simulator =
            new SimpleSimulator(new DoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(0,
                TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800, TimeUnit.SECOND), this);
        GTUType<String> carType = GTUType.makeGTUType("Car");
        LaneType<String> laneType = new LaneType<String>("CarLane");
        laneType.addCompatibility(carType);
        Lane<?, ?> lane = CarTest.makeLane(laneType);
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(1234.567, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> length = new DoubleScalar.Rel<LengthUnit>(5.0, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER);
        Map<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions = new HashMap<>();
        initialLongitudinalPositions.put(lane, initialPosition);
        AbstractLaneChangeModel laneChangeModel = new Egoistic();
        LaneBasedIndividualCar<Integer> gtu =
            new LaneBasedIndividualCar<Integer>(12345, carType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositions, speed, length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(
                    new CompleteRoute<>("")), simulator);
        DoubleScalar.Rel<LengthUnit> longerHeadway = DoubleScalar.plus(minimumHeadway, precision).immutable();
        DoubleScalar.Abs<AccelerationUnit> longerHeadwayAcceleration =
            gtuFollowingModel.computeAcceleration(speed, maxSpeed, speed, longerHeadway, speedLimit);
        // System.out.println("acceleration at headway " + longerHeadway + " is " + longerHeadwayAcceleration);
        assertTrue("deceleration with longer headway than minimum should be >= -maximumSafeDeceleration",
            -maxSafeDeceleration.getSI() <= longerHeadwayAcceleration.getSI());
        DoubleScalar.Rel<LengthUnit> shorterHeadway = DoubleScalar.minus(minimumHeadway, precision).immutable();
        DoubleScalar.Abs<AccelerationUnit> shorterHeadwayAcceleration =
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
        assertTrue("acceleration of stationary gtu with no leader should be > 0", 0 < noLeader.getAcceleration().getSI());
        precision = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.SI);
        try
        {
            gtuFollowingModel.minimumHeadway(speed, speed, precision, speedLimit, maxSpeed);
            fail("precision 0 should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        precision = new DoubleScalar.Rel<LengthUnit>(-1, LengthUnit.SI);
        try
        {
            gtuFollowingModel.minimumHeadway(speed, speed, precision, speedLimit, maxSpeed);
            fail("precision -1 should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        Map<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions50 = new HashMap<>();
        DoubleScalar.Rel<LengthUnit> headway50m = new DoubleScalar.Rel<LengthUnit>(50, LengthUnit.METER);
        initialLongitudinalPositions50.put(lane, DoubleScalar.plus(initialPosition, headway50m).immutable());
        LaneBasedIndividualCar<Integer> gtu50m =
            new LaneBasedIndividualCar<Integer>(100050, carType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositions50, speed, length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(
                    new CompleteRoute<>("")), simulator);
        HeadwayGTU hwgtu50m = new HeadwayGTU(gtu50m, headway50m.getSI());
        Collection<HeadwayGTU> otherGTUs = new ArrayList<HeadwayGTU>();
        DualAccelerationStep asEmpty = gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        // System.out.println("asEmpty: [" + asEmpty[0] + ", " + asEmpty[1] + "]");
        DoubleScalar.Abs<TimeUnit> expectedValidUntil = new DoubleScalar.Abs<TimeUnit>(stepSize.getSI(), TimeUnit.SI);
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
        Map<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions100 = new HashMap<>();
        DoubleScalar.Rel<LengthUnit> headway100m = new DoubleScalar.Rel<LengthUnit>(100, LengthUnit.METER);
        initialLongitudinalPositions100.put(lane, DoubleScalar.plus(initialPosition, headway100m).immutable());
        LaneBasedIndividualCar<Integer> gtu100m =
            new LaneBasedIndividualCar<Integer>(100100, carType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositions50, speed, length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(
                    new CompleteRoute<>("")), simulator);
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
        Map<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositionsOverlapping = new HashMap<>();
        initialLongitudinalPositionsOverlapping.put(lane, DoubleScalar.plus(initialPosition,
            new DoubleScalar.Rel<LengthUnit>(1, LengthUnit.METER)).immutable());
        LaneBasedIndividualCar<Integer> gtu1m =
            new LaneBasedIndividualCar<Integer>(100100, carType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositions50, speed, length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(
                    new CompleteRoute<>("")), simulator);
        HeadwayGTU hwgtu1m = new HeadwayGTU(gtu1m, java.lang.Double.NaN);
        otherGTUs.add(hwgtu1m);
        DualAccelerationStep as1m = gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        AccelerationStep a1 = AbstractGTUFollowingModel.TOODANGEROUS.getLeaderAccelerationStep();
        // System.out.println("a1: " + a1);
        // System.out.println("as1m: [" + as1m[0] + ", " + as1m[1] + "]");
        checkAccelerationStep("leader overlapping ", as1m, a1.getAcceleration(), a1.getAcceleration(), a1.getValidUntil());
        otherGTUs.clear();
        otherGTUs.add(hwgtu100m);
        // Follower at 75m
        Map<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositionsMinus75 = new HashMap<>();
        DoubleScalar.Rel<LengthUnit> headwayMinus75m = new DoubleScalar.Rel<LengthUnit>(-75, LengthUnit.METER);
        initialLongitudinalPositionsMinus75.put(lane, DoubleScalar.plus(initialPosition, headwayMinus75m).immutable());
        LaneBasedIndividualCar<Integer> gtuMinus75m =
            new LaneBasedIndividualCar<Integer>(100075, carType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositionsMinus75, speed, length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(
                    new CompleteRoute<>("")), simulator);
        HeadwayGTU hwgtuMinus75m = new HeadwayGTU(gtuMinus75m, headwayMinus75m.getSI());
        otherGTUs.add(hwgtuMinus75m);
        DualAccelerationStep asMinus75And100m = gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        AccelerationStep a75 =
            gtuFollowingModel.computeAcceleration(gtuMinus75m, gtu.getLongitudinalVelocity(),
                new DoubleScalar.Rel<LengthUnit>(Math.abs(headwayMinus75m.getSI()), LengthUnit.SI), speedLimit);
        checkAccelerationStep("leader at " + headway100m + " and follower at " + headwayMinus75m, asMinus75And100m, a100
            .getAcceleration(), a75.getAcceleration(), expectedValidUntil);
        // Another follower at 200m
        Map<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositionsMinus200 = new HashMap<>();
        DoubleScalar.Rel<LengthUnit> headwayMinus200m = new DoubleScalar.Rel<LengthUnit>(-200, LengthUnit.METER);
        initialLongitudinalPositionsMinus200.put(lane, DoubleScalar.plus(initialPosition, headwayMinus200m).immutable());
        LaneBasedIndividualCar<Integer> gtuMinus200m =
            new LaneBasedIndividualCar<Integer>(100200, carType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositionsMinus200, speed, length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(
                    new CompleteRoute<>("")), simulator);
        HeadwayGTU hwgtuMinus200m = new HeadwayGTU(gtuMinus200m, headwayMinus200m.getSI());
        otherGTUs.add(hwgtuMinus200m);
        DualAccelerationStep asMinus200Minus75And100m = gtuFollowingModel.computeAcceleration(gtu, otherGTUs, speedLimit);
        // The extra follower at -200 should not make a difference
        checkAccelerationStep("leader at " + headway100m + " and follower at " + headwayMinus75m, asMinus200Minus75And100m,
            a100.getAcceleration(), a75.getAcceleration(), expectedValidUntil);
    }

    /**
     * Verify a result of computeAcceleration.
     * @param description String; description of the result to check
     * @param as AccelerationStep[2]; the result to verify
     * @param a0 DoubleScalar.Abs&lt;AccelerationUnit&gt;; the expected acceleration in as[0]
     * @param a1 DoubleScalar.Abs&lt;AccelerationUnit&gt;; the expected acceleration in as[1]
     * @param validUntil DoubleScalar.Abs&lt;TimeUnit&gt;; the expected validUntil value in both entries of as
     */
    private void checkAccelerationStep(final String description, final DualAccelerationStep as,
        final DoubleScalar.Abs<AccelerationUnit> a0, final DoubleScalar.Abs<AccelerationUnit> a1,
        final DoubleScalar.Abs<TimeUnit> validUntil)
    {
        assertEquals(description + ": a leader should be " + a0, a0.getSI(), as.getLeaderAcceleration().getSI(), 0.001);
        assertEquals(description + ": a leader should be " + a0, a0.getSI(), as.getLeaderAccelerationStep()
            .getAcceleration().getSI(), 0.001);
        assertEquals(description + ": a leader should be valid until " + validUntil, validUntil.getSI(), as
            .getLeaderValidUntil().getSI(), 0.001);
        assertEquals(description + ": a leader should be valid until " + validUntil, validUntil.getSI(), as
            .getLeaderAccelerationStep().getValidUntil().getSI(), 0.001);
        assertEquals(description + ": a follower should be " + a1, a1.getSI(), as.getFollowerAcceleration().getSI(), 0.001);
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
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> simulator)
        throws SimRuntimeException, RemoteException
    {
        // Do nothing.
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return null;
    }
}

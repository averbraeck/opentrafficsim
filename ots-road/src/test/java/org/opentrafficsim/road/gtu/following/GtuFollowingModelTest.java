package org.opentrafficsim.road.gtu.following;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.car.CarTest;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtuSimple;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGtuFollowingTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractGtuFollowingModelMobil;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.DualAccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GtuFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.LaneType;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the methods that the classes that implement GtuFollowingModel have in common.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class GtuFollowingModelTest implements UNITS
{
    /** Generate unique names for the GTUs. */
    private IdGenerator gtuIdGenerator = new IdGenerator("GTU");

    /**
     * Test that the methods required by the GtuFollowingModel interface.
     * @param gtuFollowingModel GtuFollowingModel
     * @throws Exception when something goes wrong (should not happen)
     */
    private void gtuFollowingModelTests(final GtuFollowingModelOld gtuFollowingModel) throws Exception
    {
        OtsSimulatorInterface simulator = new OtsSimulator("GtuFollowingModelTest");
        OtsRoadNetwork network = new OtsRoadNetwork("gtu following test network", simulator);
        Model model = new Model(simulator, network);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model);

        Acceleration maxSafeDeceleration = gtuFollowingModel.getMaximumSafeDeceleration();
        assertNotNull("maximumSafeDeceleration must return non-null value", maxSafeDeceleration);
        assertTrue("value of maximuSafeDeceleration must be positive", 0 < maxSafeDeceleration.getSI());
        assertTrue("value of maximumSafeDeceleration must be less than g", maxSafeDeceleration.getSI() < 10);
        Duration stepSize = gtuFollowingModel.getStepSize();
        assertNotNull("stepSize must return non-null value", stepSize);
        assertTrue("stepSize must be > 0", 0 < stepSize.getSI());
        String name = gtuFollowingModel.getName();
        // System.out.println("GtuFollowingModel " + name);
        assertNotNull("getName must return non-null value", name);
        assertTrue("getName result must not be the empty string", name.length() > 0);
        String longName = gtuFollowingModel.getLongName();
        assertNotNull("getLongName must return non-null value", longName);
        assertTrue("getLongName result must not be the empty string", longName.length() > 0);
        Speed speed = Speed.ZERO;
        Length precision = new Length(0.5, METER);
        Speed maxSpeed = new Speed(200, KM_PER_HOUR);
        Speed speedLimit = new Speed(100, KM_PER_HOUR);
        Length maxHeadway = new Length(250.0, LengthUnit.METER);
        Length minimumHeadway = gtuFollowingModel.minimumHeadway(speed, speed, precision, maxHeadway, speedLimit, maxSpeed);
        assertNotNull("minimum headway at speed 0 should be non null", minimumHeadway);
        assertTrue("minimum headway at speed 0 hould have value >= 0", 0 <= minimumHeadway.getSI());
        // System.out.println("minimum headway at speed " + speed + " is " + minimumHeadway);
        speed = new Speed(50, KM_PER_HOUR);
        minimumHeadway = gtuFollowingModel.minimumHeadway(speed, speed, precision, maxHeadway, speedLimit, maxSpeed);
        assertNotNull("minimum headway at speed 0 should be non null", minimumHeadway);
        assertTrue("minimum headway at speed 0 hould have value >= 0", 0 <= minimumHeadway.getSI());
        // System.out.println("minimum headway at speed " + speed + " is " + minimumHeadway);
        GtuType carType = DefaultsNl.CAR;
        LaneType laneType = DefaultsRoadNl.TWO_WAY_LANE;
        Lane lane = CarTest.makeLane(network, laneType, simulator);
        Length initialPosition = new Length(1234.567, METER);
        Length length = new Length(5.0, METER);
        Length width = new Length(2.0, METER);
        Set<LanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
        initialLongitudinalPositions.add(new LanePosition(lane, initialPosition));
        // AbstractLaneChangeModel laneChangeModel = new Egoistic();
        Parameters parameters = DefaultTestParameters.create();
        maxHeadway = parameters.getParameter(ParameterTypes.LOOKAHEAD);
        LaneBasedGtu gtu = new LaneBasedGtu("12345", carType, length, width, maxSpeed, length.times(0.5), network);
        LaneBasedStrategicalPlanner strategicalPlanner =
                new LaneBasedStrategicalRoutePlanner(new LaneBasedGtuFollowingTacticalPlanner(gtuFollowingModel, gtu), gtu);
        gtu.setParameters(parameters);
        gtu.init(strategicalPlanner, initialLongitudinalPositions, speed);
        Length longerHeadway = minimumHeadway.plus(precision);
        Acceleration longerHeadwayAcceleration =
                gtuFollowingModel.computeAcceleration(speed, maxSpeed, speed, longerHeadway, speedLimit);
        // System.out.println("acceleration at headway " + longerHeadway + " is " + longerHeadwayAcceleration);
        assertTrue("deceleration with longer headway than minimum should be >= -maximumSafeDeceleration",
                -maxSafeDeceleration.getSI() <= longerHeadwayAcceleration.getSI());
        Length shorterHeadway = minimumHeadway.minus(precision);
        Acceleration shorterHeadwayAcceleration =
                gtuFollowingModel.computeAcceleration(speed, maxSpeed, speed, shorterHeadway, speedLimit);
        // System.out.println("acceleration at headway " + shorterHeadway + " is " + shorterHeadwayAcceleration);
        gtuFollowingModel.computeAcceleration(speed, maxSpeed, speed, shorterHeadway, speedLimit);
        assertTrue("deceleration with longer headway than minimum should be <= -maximumSafeDeceleration",
                -maxSafeDeceleration.getSI() >= shorterHeadwayAcceleration.getSI());
        AccelerationStep noLeader = gtuFollowingModel.computeAccelerationStepWithNoLeader(gtu, maxHeadway, speedLimit);
        // System.out.println("noLeader is " + noLeader);
        assertNotNull("result of computeAccelerationWithNoLeader is not null", noLeader);
        assertEquals("result of computeAccelerationWithNoLeader is valid for " + stepSize, stepSize.getSI(),
                noLeader.getValidUntil().getSI(), 0.001);
        assertTrue("acceleration of stationary gtu with no leader should be > 0", 0 < noLeader.getAcceleration().getSI());
        precision = Length.ZERO;
        try
        {
            gtuFollowingModel.minimumHeadway(speed, speed, precision, maxHeadway, speedLimit, maxSpeed);
            fail("precision 0 should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        precision = new Length(-1, LengthUnit.SI);
        try
        {
            gtuFollowingModel.minimumHeadway(speed, speed, precision, maxHeadway, speedLimit, maxSpeed);
            fail("precision -1 should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        Length headway50m = new Length(50, METER);
        Set<LanePosition> initialLongitudinalPositions50 = new LinkedHashSet<>(1);
        initialLongitudinalPositions50.add(new LanePosition(lane, initialPosition.plus(headway50m)));
        LaneBasedGtu gtu50m = new LaneBasedGtu("100050", carType, length, width, maxSpeed, length.times(0.5), network);
        strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                new LaneBasedGtuFollowingTacticalPlanner(gtuFollowingModel, gtu50m), gtu50m);
        gtu50m.setParameters(parameters);
        gtu50m.init(strategicalPlanner, initialLongitudinalPositions50, speed);
        HeadwayGtuSimple hwgtu50m = new HeadwayGtuSimple(gtu50m.getId(), gtu50m.getType(), headway50m, gtu50m.getLength(),
                gtu50m.getWidth(), gtu50m.getSpeed(), gtu50m.getAcceleration(), null);
        Collection<Headway> otherGTUs = new ArrayList<>();
        DualAccelerationStep asEmpty = gtuFollowingModel.computeDualAccelerationStep(gtu, otherGTUs, maxHeadway, speedLimit);
        // System.out.println("asEmpty: [" + asEmpty[0] + ", " + asEmpty[1] + "]");
        Time expectedValidUntil = new Time(stepSize.getSI(), TimeUnit.BASE_SECOND);
        checkAccelerationStep("Empty collection", asEmpty, noLeader.getAcceleration(), noLeader.getAcceleration(),
                expectedValidUntil);
        otherGTUs.add(new HeadwayGtuSimple(gtu.getId(), gtu.getType(), new Length(Double.NaN, LengthUnit.SI), gtu.getLength(),
                gtu.getWidth(), gtu.getSpeed()));
        // If otherGTUs only contains the reference GTU, the result should be exactly the same
        asEmpty = gtuFollowingModel.computeDualAccelerationStep(gtu, otherGTUs, maxHeadway, speedLimit);
        checkAccelerationStep("Empty collection", asEmpty, noLeader.getAcceleration(), noLeader.getAcceleration(),
                expectedValidUntil);
        otherGTUs.clear();
        otherGTUs.add(hwgtu50m);
        DualAccelerationStep as50m = gtuFollowingModel.computeDualAccelerationStep(gtu, otherGTUs, maxHeadway, speedLimit);
        // System.out.println("as50m: [" + as50m[0] + ", " + as50m[1] + "]");
        AccelerationStep a50 =
                gtuFollowingModel.computeAccelerationStep(gtu50m, gtu50m.getSpeed(), headway50m, maxHeadway, speedLimit);
        checkAccelerationStep("leader at " + headway50m, as50m, a50.getAcceleration(), noLeader.getAcceleration(),
                expectedValidUntil);
        Map<Lane, Length> initialLongitudinalPositions100 = new LinkedHashMap<>();
        Length headway100m = new Length(100, METER);
        initialLongitudinalPositions100.put(lane, initialPosition.plus(headway100m));
        LaneBasedGtu gtu100m = new LaneBasedGtu("100100", carType, length, width, maxSpeed, length.times(0.5), network);
        strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                new LaneBasedGtuFollowingTacticalPlanner(gtuFollowingModel, gtu100m), gtu100m);
        gtu100m.setParameters(parameters);
        gtu100m.init(strategicalPlanner, initialLongitudinalPositions50, speed);
        HeadwayGtuSimple hwgtu100m = new HeadwayGtuSimple(gtu100m.getId(), gtu100m.getType(), headway100m, gtu100m.getLength(),
                gtu100m.getWidth(), gtu100m.getSpeed(), gtu100m.getAcceleration(), maxSpeed);
        // gtu100m.getDesiredSpeed());
        otherGTUs.add(hwgtu100m);
        DualAccelerationStep as50and100m =
                gtuFollowingModel.computeDualAccelerationStep(gtu, otherGTUs, maxHeadway, speedLimit);
        checkAccelerationStep("leader at " + headway50m + " and at " + headway100m, as50and100m, a50.getAcceleration(),
                noLeader.getAcceleration(), expectedValidUntil);
        otherGTUs.add(new HeadwayGtuSimple(gtu.getId(), gtu.getType(), Length.ZERO, gtu.getLength(), gtu.getWidth(),
                gtu.getSpeed(), gtu.getAcceleration(), maxSpeed)); // gtu.getDesiredSpeed()));
        as50and100m = gtuFollowingModel.computeDualAccelerationStep(gtu, otherGTUs, maxHeadway, speedLimit);
        checkAccelerationStep("follower at 0, leader at " + headway50m + " and at " + headway100m, as50and100m,
                a50.getAcceleration(), noLeader.getAcceleration(), expectedValidUntil);
        otherGTUs.add(new HeadwayGtuSimple(gtu.getId(), gtu.getType(), new Length(Double.NaN, LengthUnit.SI), gtu.getLength(),
                gtu.getWidth(), gtu.getSpeed(), gtu.getAcceleration(), maxSpeed)); // gtu.getDesiredSpeed()));
        as50and100m = gtuFollowingModel.computeDualAccelerationStep(gtu, otherGTUs, maxHeadway, speedLimit);
        checkAccelerationStep("follower at NaN, leader at " + headway50m + " and at " + headway100m, as50and100m,
                a50.getAcceleration(), noLeader.getAcceleration(), expectedValidUntil);
        otherGTUs.clear();
        otherGTUs.add(hwgtu100m);
        DualAccelerationStep as100m = gtuFollowingModel.computeDualAccelerationStep(gtu, otherGTUs, maxHeadway, speedLimit);
        AccelerationStep a100 =
                gtuFollowingModel.computeAccelerationStep(gtu, gtu100m.getSpeed(), headway100m, maxHeadway, speedLimit);
        // System.out.println("a100: " + a100);
        // System.out.println("as100m: [" + as100m[0] + ", " + as100m[1] + "]");
        checkAccelerationStep("leader at " + headway100m, as100m, a100.getAcceleration(), noLeader.getAcceleration(),
                expectedValidUntil);
        // Add an overlapping GTU. Immediate collision situation should return TOODANGEROUS
        Map<Lane, Length> initialLongitudinalPositionsOverlapping = new LinkedHashMap<>();
        Length ahead = new Length(1, METER);
        initialLongitudinalPositionsOverlapping.put(lane, initialPosition.plus(ahead));
        LaneBasedGtu gtu1m = new LaneBasedGtu("100100" + this.gtuIdGenerator.get(), carType, length, width, maxSpeed,
                length.times(0.5), network);
        strategicalPlanner =
                new LaneBasedStrategicalRoutePlanner(new LaneBasedGtuFollowingTacticalPlanner(gtuFollowingModel, gtu1m), gtu1m);
        gtu1m.setParameters(parameters);
        gtu1m.init(strategicalPlanner, initialLongitudinalPositions50, speed);
        Length overlap = new Length(length.minus(ahead));
        HeadwayGtuSimple hwgtu1m =
                new HeadwayGtuSimple(gtu1m.getId(), gtu1m.getType(), ahead, overlap, Length.ZERO.minus(overlap),
                        gtu1m.getLength(), gtu1m.getWidth(), gtu1m.getSpeed(), gtu1m.getAcceleration(), maxSpeed);
        // gtu1m.getDesiredSpeed());
        otherGTUs.add(hwgtu1m);
        DualAccelerationStep as1m = gtuFollowingModel.computeDualAccelerationStep(gtu, otherGTUs, maxHeadway, speedLimit);
        AccelerationStep a1 = AbstractGtuFollowingModelMobil.TOODANGEROUS.getLeaderAccelerationStep();
        // System.out.println("a1: " + a1);
        // System.out.println("as1m: [" + as1m[0] + ", " + as1m[1] + "]");
        checkAccelerationStep("leader overlapping ", as1m, a1.getAcceleration(), a1.getAcceleration(), a1.getValidUntil());
        otherGTUs.clear();
        otherGTUs.add(hwgtu100m);
        // Follower at 75m
        Length headwayMinus75m = new Length(-75, METER);
        Set<LanePosition> initialLongitudinalPositionsMinus75 = new LinkedHashSet<>(1);
        initialLongitudinalPositionsMinus75.add(new LanePosition(lane, initialPosition.plus(headwayMinus75m)));
        LaneBasedGtu gtuMinus75m = new LaneBasedGtu("100075", carType, length, width, maxSpeed, length.times(0.5), network);
        strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                new LaneBasedGtuFollowingTacticalPlanner(gtuFollowingModel, gtuMinus75m), gtuMinus75m);
        gtuMinus75m.setParameters(parameters);
        gtuMinus75m.init(strategicalPlanner, initialLongitudinalPositionsMinus75, speed);
        HeadwayGtuSimple hwgtuMinus75m =
                new HeadwayGtuSimple(gtuMinus75m.getId(), gtuMinus75m.getType(), headwayMinus75m, gtuMinus75m.getLength(),
                        gtuMinus75m.getWidth(), gtuMinus75m.getSpeed(), gtuMinus75m.getAcceleration(), maxSpeed);
        // gtuMinus75m.getDesiredSpeed());
        otherGTUs.add(hwgtuMinus75m);
        DualAccelerationStep asMinus75And100m =
                gtuFollowingModel.computeDualAccelerationStep(gtu, otherGTUs, maxHeadway, speedLimit);
        AccelerationStep a75 = gtuFollowingModel.computeAccelerationStep(gtuMinus75m, gtu.getSpeed(),
                new Length(Math.abs(headwayMinus75m.getSI()), LengthUnit.SI), maxHeadway, speedLimit);
        checkAccelerationStep("leader at " + headway100m + " and follower at " + headwayMinus75m, asMinus75And100m,
                a100.getAcceleration(), a75.getAcceleration(), expectedValidUntil);
        // Another follower at 200m
        Length headwayMinus200m = new Length(-200, METER);
        Set<LanePosition> initialLongitudinalPositionsMinus200 = new LinkedHashSet<>(1);
        initialLongitudinalPositionsMinus200.add(new LanePosition(lane, initialPosition.plus(headwayMinus200m)));
        LaneBasedGtu gtuMinus200m = new LaneBasedGtu("100200", carType, length, width, maxSpeed, length.times(0.5), network);
        strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                new LaneBasedGtuFollowingTacticalPlanner(gtuFollowingModel, gtuMinus200m), gtuMinus200m);
        gtuMinus200m.setParameters(parameters);
        gtuMinus200m.init(strategicalPlanner, initialLongitudinalPositionsMinus200, speed);
        HeadwayGtuSimple hwgtuMinus200m =
                new HeadwayGtuSimple(gtuMinus200m.getId(), gtuMinus200m.getType(), headwayMinus200m, gtuMinus200m.getLength(),
                        gtuMinus200m.getWidth(), gtuMinus200m.getSpeed(), gtuMinus200m.getAcceleration(), maxSpeed); // gtuMinus200m.getDesiredSpeed());
        otherGTUs.add(hwgtuMinus200m);
        DualAccelerationStep asMinus200Minus75And100m =
                gtuFollowingModel.computeDualAccelerationStep(gtu, otherGTUs, maxHeadway, speedLimit);
        // The extra follower at -200 should not make a difference
        checkAccelerationStep("leader at " + headway100m + " and follower at " + headwayMinus75m, asMinus200Minus75And100m,
                a100.getAcceleration(), a75.getAcceleration(), expectedValidUntil);
    }

    /**
     * Verify a result of computeAcceleration.
     * @param description String; description of the result to check
     * @param as AccelerationStep[2]; the result to verify
     * @param a0 Acceleration; the expected acceleration in as[0]
     * @param a1 Acceleration; the expected acceleration in as[1]
     * @param validUntil Time; the expected validUntil value in both entries of as
     */
    private void checkAccelerationStep(final String description, final DualAccelerationStep as, final Acceleration a0,
            final Acceleration a1, final Time validUntil)
    {
        assertEquals(description + ": a leader should be " + a0, a0.getSI(), as.getLeaderAcceleration().getSI(), 0.001);
        assertEquals(description + ": a leader should be " + a0, a0.getSI(),
                as.getLeaderAccelerationStep().getAcceleration().getSI(), 0.001);
        assertEquals(description + ": a leader should be valid until " + validUntil, validUntil.getSI(),
                as.getLeaderValidUntil().getSI(), 0.001);
        assertEquals(description + ": a leader should be valid until " + validUntil, validUntil.getSI(),
                as.getLeaderAccelerationStep().getValidUntil().getSI(), 0.001);
        assertEquals(description + ": a follower should be " + a1, a1.getSI(), as.getFollowerAcceleration().getSI(), 0.001);
        assertEquals(description + ": a follower should be " + a1, a1.getSI(),
                as.getFollowerAccelerationStep().getAcceleration().getSI(), 0.001);
        assertEquals(description + ": a follower should be valid until " + validUntil, validUntil.getSI(),
                as.getFollowerValidUntil().getSI(), 0.001);
        assertEquals(description + ": a follower should be valid until " + validUntil, validUntil.getSI(),
                as.getFollowerValidUntil().getSI(), 0.001);
    }

    /**
     * Test IDM.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public final void testIDM() throws Exception
    {
        gtuFollowingModelTests(new IdmOld());
    }

    /**
     * Test IDMPlus.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public final void testIDMPlus() throws Exception
    {
        gtuFollowingModelTests(new IdmPlusOld());
    }

    /** The helper model. */
    protected static class Model extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20141027L;

        /** */
        private final OtsRoadNetwork network;

        /**
         * @param simulator the simulator to use
         * @param network the network
         */
        public Model(final OtsSimulatorInterface simulator, final OtsRoadNetwork network)
        {
            super(simulator);
            this.network = network;
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public final OtsRoadNetwork getNetwork()
        {
            return this.network;
        }
    }
}

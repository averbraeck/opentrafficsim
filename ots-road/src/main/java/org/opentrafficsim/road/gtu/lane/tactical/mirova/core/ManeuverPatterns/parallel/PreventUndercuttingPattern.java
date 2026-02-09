package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.parallel;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.exclusive.SimpleLaneChangePattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.MacroTrafficContext;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * PreventUndercuttingPattern.
 * <p>
 * Ensures compliance with the "no overtaking on the right" regulation (Rechtsüberholverbot, §5 StVO).
 * This pattern activates when the perception detects a slower vehicle on the immediate left lane
 * while traffic is free flowing (speed > VCONG).
 * </p>
 * <p>
 * Instead of performing a hard brake, it initiates a "Shadowing" state, matching the speed of the
 * left neighbor until a lane change is possible or the situation clears.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class PreventUndercuttingPattern extends ManeuverPattern
{

    /**
     * Constructor.
     * @param kc The knowledge chunk associated with this pattern (likely Safety or Regulatory chunk).
     * @throws ParameterException if parameters are missing
     */
    public PreventUndercuttingPattern(final MirovaTacticalPlanner vehicle) throws ParameterException
    {

        super(PatternType.PARALLEL, vehicle);
        this.initialActionState =  () -> new ShadowingState(this);
    }

    /**
     * Determines if this pattern is applicable based on the current context.
     * <p>
     * Logic extracted from {@code AutobahnFreeDrivingPattern}:
     * 1. Right side overtaking is detected ahead.
     * 2. Traffic is flowing (Speed > VCONG).
     * </p>
     *
     * @param context The context manager containing perception data.
     * @return true if we are at risk of undercutting, false otherwise.
     */
    @Override
    public boolean checkAbility()
    {
        try
        {
            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
            EgoContext ego = this.vehicle.getContext(EgoContext.class);

            // 1. Check Traffic State (Undercutting is allowed/tolerated in congestion)
            Speed congestionThreshold = this.vehicle.getParameters().getParameter(ParameterTypes.VCONG);
            boolean isFreeFlow = ego.getEgoSpeed().gt(congestionThreshold);

            if (isFreeFlow == true)
            {
                // 2. Check Perception for Undercutting situation
                boolean potentialUndercut = neighbors.getRightSideOvertakingAhead();

                if (potentialUndercut)
                {
                    return true;
                }

            }
            setRunning(false);
            return false;

        }
        catch (ParameterException e)
        {
            throw new RuntimeException("Missing VCONG parameter for PreventUndercutting logic.", e);
        }
    }

    /*
     *
     */
    @Override
    public boolean checkContext()
    {
        return true; // Placeholder
    }

    /**
     * The active state of this pattern.
     * It calculates an acceleration that matches the left neighbor (Shadowing),
     * while respecting the safety distance to the own leader.
     */
    public static class ShadowingState extends ActionState
    {

        public ShadowingState(final ManeuverPattern pattern)
        {
            super(pattern);
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {

            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);

            // 1. Base Acceleration: Follow own leader (Safety first)
            //Acceleration acc = ego.getCurrentCarFollowingAcceleration();

            // 2. Calculate Regulatory Acceleration (Shadowing left leader)
            LateralDirectionality leftDir = LateralDirectionality.LEFT;

            // We re-verify existence to be safe, though pattern logic checked it
            if (neighbors.getLeader(leftDir) != null)
            {
                HeadwayGtu leftLeader = neighbors.getLeader(leftDir);
                Length leftDistHeadway = neighbors.getFrontGapDistance(leftDir);
                Speed leftLeaderSpeed = leftLeader.getSpeed();
                SpeedLimitInfo speedLimit = infra.getCurrentSpeedLimit();

                // Calculate acceleration required to stay behind the left vehicle
                Double safetyDistanceReductionFactorLaneChange = this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange) * 1.1;
                Duration timeHeadwayReduced = this.vehicle.getParameters().getParameter(ParameterTypes.T).times(safetyDistanceReductionFactorLaneChange);
                this.vehicle.getParameters().setParameterResettable(ParameterTypes.T, timeHeadwayReduced);
                Acceleration aShadow = CarFollowingUtil.followSingleLeader(
                        this.vehicle.getCarFollowingModel(),
                        this.vehicle.getParameters(),
                        ego.getEgoSpeed(),
                        speedLimit,
                        leftDistHeadway,
                        leftLeaderSpeed);
                this.vehicle.getParameters().resetParameter(ParameterTypes.T);
                System.out.println("GTU " + this.vehicle.getGtu().getId() + " is shadowing left leader with acc " + aShadow);

                if (aShadow.lt(Acceleration.instantiateSI(-6.0)))
                {
                    MacroTrafficContext macroCtx = this.vehicle.getContext(MacroTrafficContext.class);
                    Speed leftLaneSpeed = macroCtx.getAverageSpeed(RelativeLane.LEFT);
                    aShadow = CarFollowingUtil.approachTargetSpeed(
                            this.vehicle.getCarFollowingModel(),
                            this.vehicle.getParameters(),
                            ego.getEgoSpeed(),
                            infra.getCurrentSpeedLimit(),
                            Length.instantiateSI(10.0),
                            leftLaneSpeed
                            );
                    System.out.println("GTU " + this.vehicle.getGtu().getId() + " is applying emergency braking with acc " + aShadow);
                }

                return new SimpleOperationalPlan(
                        aShadow,
                        this.vehicle.getGtu().getParameters().getParameter(ParameterTypes.DT));

            }
            return null; // No left leader, should not happen as pattern should not be active, but safety first

        }

        @Override
        public SimpleOperationalPlan next() throws ParameterException, GtuException, NetworkException
        {
            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);

            // ESCAPE LOGIC:
            // If we are actively braking/shadowing to prevent undercutting,
            // the logical resolution is to move behind the vehicle we are shadowing.
            if (neighbors.getIfLaneChangePossible(LateralDirectionality.LEFT))
            {
                // Transition to performing the lane change
                return transitionTo(new SimpleLaneChangePattern.PerformLaneChangeState(this.maneuverPattern, LateralDirectionality.LEFT));
            }

            return null; // Stay in shadowing
        }

        @Override
        public SimpleOperationalPlan abort()
        {
            return null;
        }

        @Override
        public String toString()
        {
            return "PreventUndercutting:Shadowing";
        }
    }

//
// // ========================================================================================
//    // STATE: PERFORM REGULATORY LANE CHANGE
//    // ========================================================================================
//
//    /**
//     * Executes a lane change to resolve the undercutting situation (moving behind the slower vehicle).
//     */
//    public static class PerformRegulatoryLaneChange extends ActionState
//    {
//        private final LateralDirectionality direction;
//        private final Lane originLane;
//
//        public PerformRegulatoryLaneChange(final ManeuverPattern pattern, final LateralDirectionality direction)
//        {
//            super(pattern);
//            this.direction = direction;
//            this.originLane = this.vehicle.getGtu().getLane();
//        }
//
//        @Override
//        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
//        {
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
//            Parameters params = this.vehicle.getParameters();
//
//            Double safetyDistanceReductionFactorLaneChange = this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange) * 1.1;
//            Duration timeHeadwayReduced = this.vehicle.getParameters().getParameter(ParameterTypes.T).times(safetyDistanceReductionFactorLaneChange);
//            this.vehicle.getParameters().setParameterResettable(ParameterTypes.T, timeHeadwayReduced);
//
//            // 1. Basic longitudinal control
//            Acceleration minAcc = ego.getCurrentCarFollowingAcceleration();
//
//            // 2. Dual-Target Consideration:
//            // While changing lanes, we must respect leaders on BOTH lanes.
//            // Especially strictly here, as we are merging behind someone slower.
//            if (this.vehicle.getGtu().getLane().equals(this.originLane))
//            {
//                // We are still on the old lane, looking at the new leader
//                HeadwayGtu targetLeader = neighbors.getLeader(this.direction);
//                if (targetLeader != null)
//                {
//                    Acceleration aTarget = CarFollowingUtil.followSingleLeader(
//                            this.vehicle.getCarFollowingModel(),
//                            params,
//                            ego.getEgoSpeed(),
//                            infra.getCurrentSpeedLimit(),
//                            targetLeader.getDistance(),
//                            targetLeader.getSpeed());
//
//                    minAcc = Acceleration.min(minAcc, aTarget);
//                }
//            }
//            this.vehicle.getParameters().resetParameter(ParameterTypes.T);
//            // 3. Create Plan with Lateral Component
//            SimpleOperationalPlan plan = new SimpleOperationalPlan(
//                    minAcc,
//                    params.getParameter(ParameterTypes.DT),
//                    this.direction); // Initiate/Continue lateral move
//
//            // Set Indicators
//            if (this.direction == LateralDirectionality.LEFT) {
//                plan.setIndicatorIntentLeft();
//            } else {
//                plan.setIndicatorIntentRight();
//            }
//
//            // Mark maneuver as running so PatternSelector sticks to us
//            this.vehicle.setRunningManeuver(true);
//
//            return plan;
//        }
//
//        @Override
//        public SimpleOperationalPlan next() throws ParameterException, GtuException, NetworkException
//        {
//            // Check completion
//            boolean currentLaneIsNotOrigin = !this.vehicle.getGtu().getLane().equals(this.originLane);
//            boolean isChanging = this.vehicle.getLaneChange().isChangingLane();
//
//            if (currentLaneIsNotOrigin && !isChanging)
//            {
//                // Lane change done. The pattern has fulfilled its purpose (we are now behind the slower car).
//                // We return null to let the PatternSelector decide what to do next
//                // (likely switch to FreeDriving or DiscretionaryLaneChange to overtake later).
//                this.vehicle.setRunningManeuver(false);
//                EgoContext ego = this.vehicle.getContext(EgoContext.class);
//                Acceleration zeroAcc = ego.getCurrentCarFollowingAcceleration();
//                return new SimpleOperationalPlan(
//                        zeroAcc,
//                        this.vehicle.getGtu().getParameters().getParameter(ParameterTypes.DT));
//            }
//
//            return null; // Continue this state
//        }
//
//        @Override
//        public SimpleOperationalPlan abort()
//        {
//            // Fail-safe logic could go here (e.g. aborting lane change if gap closes suddenly)
//            // For now, we rely on the ActionState base behavior or standard OTS collision avoidance.
//            return null;
//        }
//
//        @Override
//        public String toString()
//        {
//            return "PreventUndercutting:ChangeTo" + this.direction;
//        }
//    }
}
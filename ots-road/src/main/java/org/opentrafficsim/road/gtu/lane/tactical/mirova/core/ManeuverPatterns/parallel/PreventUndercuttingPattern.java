package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.parallel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
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
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.exclusive.SimpleLaneChangePattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.MacroTrafficContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.following.MirovaCarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Parallel maneuver pattern that prevents undercutting on the right.
 * <p>
 * Forms part of <b>Layer 4 (Procedure & Action)</b> in the MiRoVA architecture. Ensures compliance with the German "no
 * overtaking on the right" regulation (Rechtsüberholverbot, §5 StVO). This pattern activates when the perception detects a
 * slower vehicle on the immediate left lane while traffic is free flowing (speed > VCONG).
 * </p>
 * <p>
 * Instead of performing a hard brake, it initiates a "Shadowing" state, matching the speed of the left neighbor until a lane
 * change is possible or the situation clears.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class PreventUndercuttingPattern extends ManeuverPattern
{

    /** ID of the vehicle on the left lane that this ego vehicle is currently shadowing. */
    protected String shadowingLeftNeighborId = null;

    /**
     * Constructs a new PreventUndercuttingPattern.
     * @param vehicle the tactical planner associated with the ego vehicle
     * @throws ParameterException if parameter initialization fails
     */
    public PreventUndercuttingPattern(final MirovaTacticalPlanner vehicle) throws ParameterException
    {
        super(PatternType.PARALLEL, vehicle);
        this.initialActionState = () -> new ShadowingState(this);
    }

    /**
     * Determines if this pattern is applicable based on the current context.
     * <p>
     * Logic: 1. Check if traffic is flowing (Speed > VCONG). Undercutting is allowed in congestion. 2. Check if a right-side
     * overtaking situation is detected ahead.
     * </p>
     * @return {@code true} if we are at risk of undercutting and must prevent it, {@code false} otherwise
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

            if (isFreeFlow)
            {
                // 2. Check Perception for Undercutting situation
                boolean potentialUndercut = neighbors.getRightSideOvertakingAhead();

                if (potentialUndercut)
                {
                    this.shadowingLeftNeighborId = neighbors.getLeader(LateralDirectionality.LEFT).getId();
                    return true;
                }
            }

            this.shadowingLeftNeighborId = null;
            setRunning(false);
            return false;

        }
        catch (ParameterException e)
        {
            throw new RuntimeException("Missing VCONG parameter for PreventUndercutting logic.", e);
        }
    }

    /**
     * Context check placeholder for parallel execution.
     * @return always {@code true}, as contextual relevance is handled via checkAbility
     */
    @Override
    public boolean checkContext()
    {
        return true;
    }

    /**
     * Returns the ID of the left neighbor currently being shadowed.
     * @return the ID of the left neighbor, or null if no vehicle is being shadowed
     */
    public String getShadowingLeftNeighborId()
    {
        return this.shadowingLeftNeighborId;
    }

    /*
     * ========================================================================================= STATE: SHADOWING
     * =========================================================================================
     */

    /**
     * The active state of this pattern.
     * <p>
     * It calculates an acceleration that matches the left neighbor (Shadowing), while respecting the safety distance to the own
     * leader.
     * </p>
     */
    public static class ShadowingState extends ActionState
    {
        /** The parent maneuver pattern. */
        private final PreventUndercuttingPattern maneuverPattern;

        /**
         * Constructor.
         * @param pattern the parent maneuver pattern
         */
        public ShadowingState(final PreventUndercuttingPattern pattern)
        {
            super(pattern);
            this.maneuverPattern = pattern;
        }

        /**
         * Executes the longitudinal control to shadow the left neighbor.
         * @return the operational plan for the current tick
         * @throws ParameterException if a parameter lookup fails
         * @throws GtuException if GTU state prevents plan generation
         * @throws NetworkException if network topology limits calculation
         */
        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            this.maneuverPattern.setRunning(true);
            this.maneuverPattern.setCurrentActionState(this);

            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);

            LateralDirectionality leftDir = LateralDirectionality.LEFT;

            // We re-verify existence to be safe, though pattern logic checked it
            if (neighbors.getLeader(leftDir) != null)
            {
                HeadwayGtu leftLeader = neighbors.getLeader(leftDir);
                Length leftDistHeadway = neighbors.getFrontGapDistance(leftDir);
                Speed leftLeaderSpeed = leftLeader.getSpeed();
                Length leftLeaderLength = leftLeader.getLength();

                // Calculate acceleration required to stay behind the left vehicle
                Double safetyDistanceReductionFactorLaneChange =
                        this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)
                                * 1.1;
                Duration timeHeadwayReduced = this.vehicle.getParameters().getParameter(ParameterTypes.T)
                        .times(safetyDistanceReductionFactorLaneChange);
                this.vehicle.getParameters().setParameterResettable(ParameterTypes.T, timeHeadwayReduced);

                // 1. Berechnung für den linken Zielfahrstreifen
                Acceleration aShadowLeft = MirovaCarFollowingUtil.followDistanceAndSpeed(this.vehicle,
                        leftDistHeadway.minus(leftLeaderLength), leftLeaderSpeed);

                this.vehicle.getParameters().resetParameter(ParameterTypes.T);

                // Emergency break logic für die Ziellücke
                if (aShadowLeft.lt(Acceleration.instantiateSI(-6.0)))
                {
                    MacroTrafficContext macroCtx = this.vehicle.getContext(MacroTrafficContext.class);
                    Speed leftLaneSpeed = macroCtx.getAverageSpeed(RelativeLane.LEFT);
                    aShadowLeft =
                            MirovaCarFollowingUtil.approachTargetSpeed(this.vehicle, Length.instantiateSI(50.0), leftLaneSpeed);
                }

                // Limit deceleration to a comfortable level for the lane maneuver
                Acceleration comfortableEgoDecel =
                        this.vehicle.getParameters().getParameter(MirovaParameters.egoDecelerationThreshold);
                aShadowLeft = Acceleration.max(aShadowLeft, comfortableEgoDecel);

                EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);
                Acceleration aTarget = Acceleration.min(aShadowLeft, egoCtx.getCurrentCarFollowingAcceleration());

                return new SimpleOperationalPlan(aTarget,
                        this.vehicle.getGtu().getParameters().getParameter(ParameterTypes.DT));
            }

            return null; // No left leader, should not happen as pattern should not be active, but safety first
        }

        /**
         * Checks if the vehicle can transition out of the shadowing state.
         * @return transition to lane change preparation, or {@code null} to continue shadowing
         * @throws ParameterException if a parameter lookup fails
         * @throws GtuException if GTU limits fail
         * @throws NetworkException if network topology fails
         */
        @Override
        public SimpleOperationalPlan next() throws ParameterException, GtuException, NetworkException
        {
            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);

            if (neighbors.getIfLaneChangePossible(LateralDirectionality.LEFT))
            {
                // Transition to performing the lane change
                return transitionTo(
                        new SimpleLaneChangePattern.PerformLaneChangeState(this.maneuverPattern, LateralDirectionality.LEFT));
            }

            Duration leftTimeHeadway = neighbors.getFrontGapTimeHeadway(LateralDirectionality.LEFT);

            if (leftTimeHeadway.si < 1.5)
            {
                Duration gapLeftLane = getGapBehindLeftLeader(this.vehicle);

                if (gapLeftLane.ge(this.vehicle.getParameters().getParameter(ParameterTypes.T)))
                {
                    // Transition to preparing the lane change
                    return transitionTo(new PrepareLaneChangeState(this.maneuverPattern));
                }
            }

            return null; // Stay in shadowing
        }

        /**
         * Verifies if the state should be aborted.
         * <p>
         * Abort conditions: 1. The left leader disappears or changes ID. 2. Traffic state transitions to congestion
         * (undercutting allowed). 3. The undercutting situation is physically resolved (leader is far away or pulling away).
         * </p>
         * @return finish maneuver if no longer required, {@code null} otherwise
         * @throws ParameterException if parameters are missing
         * @throws GtuException if GTU context fails
         * @throws NetworkException if network context fails
         */
        @Override
        public SimpleOperationalPlan abort() throws ParameterException, GtuException, NetworkException
        {
            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
            HeadwayGtu leftLeader = neighbors.getLeader(LateralDirectionality.LEFT);

            // 1. Leader disappeared
            if (leftLeader == null)
            {
                return finishManeuver();
            }

            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            Speed congestionThreshold = this.vehicle.getParameters().getParameter(ParameterTypes.VCONG);
            boolean isFreeFlow = ego.getEgoSpeed().gt(congestionThreshold);

            // 2. Leader ID changed or traffic state changed
            if (!leftLeader.getId().equals(this.maneuverPattern.getShadowingLeftNeighborId()) || !isFreeFlow)
            {
                return finishManeuver();
            }

            // 3. NEW: The situation has naturally resolved (Left Leader drove away)
            Length leftGap = neighbors.getFrontGapDistance(LateralDirectionality.LEFT);
            Speed leftSpeed = leftLeader.getSpeed();
            Speed egoSpeed = ego.getEgoSpeed();

            // Abort if the left neighbor is far away (> 80m)
            // OR if they are accelerating away (speed delta > 1 m/s and gap > 40m)
            boolean isFarAway = leftGap.si > 80.0;
            boolean isPullingAway = (leftSpeed.si > egoSpeed.si + 1.0) && (leftGap.si > 40.0);

            if (isFarAway || isPullingAway)
            {
                return finishManeuver();
            }

            return null; // Continue maneuver
        }

        /**
         * * Calculates the time headway to the left leader, considering both front and rear gaps and the length of the left
         * leader. This is used to determine if we have enough gap to safely move behind the left leader. * @param vehicle the
         * tactical planner instance
         * @return Duration representing the time headway to the left leader
         * @throws ParameterException if parameters are missing
         * @throws GtuException if GTU-related errors occur
         * @throws NetworkException if network-related errors occur
         */
        public static Duration getGapBehindLeftLeader(final MirovaTacticalPlanner vehicle)
                throws ParameterException, GtuException, NetworkException
        {
            Duration gapLeftLane;
            NeighborsContext neighbors = vehicle.getContext(NeighborsContext.class);
            HeadwayGtu leftLeader = neighbors.getLeader(LateralDirectionality.LEFT);

            if (leftLeader.isParallel())
            {
                gapLeftLane = neighbors.getRearGapTimeHeadway(LateralDirectionality.LEFT);
            }
            else
            {
                Length gapLength = neighbors.getRearGapDistance(LateralDirectionality.LEFT)
                        .plus(neighbors.getFrontGapDistance(LateralDirectionality.LEFT)).plus(vehicle.getGtu().getLength());
                EgoContext ego = vehicle.getContext(EgoContext.class);
                gapLeftLane = gapLength.divide(ego.getEgoSpeed());
            }
            return gapLeftLane;
        }

        @Override
        public double getUtility()
        {
            return 0.2;
        }

        @Override
        public String toString()
        {
            return "PreventUndercutting:Shadowing";
        }
    }

    /*
     * ========================================================================================= STATE: PREPARE_LANE_CHANGE
     * =========================================================================================
     */

    /**
     * * Prepares for the lane change by ensuring we have a safe gap to the left leader and adjusting speed if necessary. This
     * state is a safety buffer before initiating the lane change, ensuring we do not cut in too closely behind the left leader.
     */
    public static class PrepareLaneChangeState extends ActionState
    {
        private final PreventUndercuttingPattern maneuverPattern;

        /**
         * Constructor.
         * @param pattern the parent maneuver pattern
         */
        public PrepareLaneChangeState(final PreventUndercuttingPattern pattern)
        {
            super(pattern);
            this.maneuverPattern = pattern;
        }

        /**
         * Executes deceleration to ensure a comfortable gap before changing lanes.
         * @return the operational plan
         * @throws ParameterException if a parameter lookup fails
         * @throws GtuException if GTU state prevents plan generation
         * @throws NetworkException if network topology limits calculation
         */
        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            this.maneuverPattern.setRunning(true);
            this.maneuverPattern.setCurrentActionState(this);

            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);

            LateralDirectionality leftDir = LateralDirectionality.LEFT;
            HeadwayGtu leftLeader = neighbors.getLeader(leftDir);

            if (leftLeader == null)
            {
                // Should not happen as we checked in the previous state, but we add a safety check.
                return null;
            }

            // Calculate acceleration required to stay behind the left vehicle
            Double safetyDistanceReductionFactorLaneChange =
                    this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange) * 1.1;
            Duration timeHeadwayReduced =
                    this.vehicle.getParameters().getParameter(ParameterTypes.T).times(safetyDistanceReductionFactorLaneChange);
            this.vehicle.getParameters().setParameterResettable(ParameterTypes.T, timeHeadwayReduced);

            Acceleration aDecel;

            // If we have a comfortable gap, we can match the left leader's speed.
            // If not, we apply a more assertive deceleration to create space for the lane change.
            aDecel = MirovaCarFollowingUtil.followSingleLeader(this.vehicle, leftLeader);

            this.vehicle.getParameters().resetParameter(ParameterTypes.T);
            aDecel = Acceleration.max(aDecel, Acceleration.instantiateSI(-2.0)); // Limit deceleration to a comfortable level

            aDecel = Acceleration.min(aDecel, ego.getCurrentCarFollowingAcceleration()); // Do not decelerate more than current
                                                                                         // following accel

            SimpleOperationalPlan plan =
                    new SimpleOperationalPlan(aDecel, this.vehicle.getGtu().getParameters().getParameter(ParameterTypes.DT));
            plan.setIndicatorIntentLeft();

            return plan;
        }

        /**
         * Checks if the preparation is complete and the lane change can begin.
         * @return transition to perform lane change, transition back to shadowing if gap lost, or null
         * @throws ParameterException if a parameter lookup fails
         * @throws GtuException if GTU limits fail
         * @throws NetworkException if network topology fails
         */
        @Override
        public SimpleOperationalPlan next() throws ParameterException, GtuException, NetworkException
        {
            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);

            if (neighbors.getIfLaneChangePossible(LateralDirectionality.LEFT))
            {
                finishManeuver();
                return transitionTo(
                        new SimpleLaneChangePattern.PerformLaneChangeState(this.maneuverPattern, LateralDirectionality.LEFT));
            }
            else if (ShadowingState.getGapBehindLeftLeader(this.vehicle).si < this.vehicle.getParameters()
                    .getParameter(ParameterTypes.T).si)
            {
                // If we lose the gap while preparing, we go back to shadowing to avoid cutting in too closely
                return transitionTo(new ShadowingState(this.maneuverPattern));
            }

            return null; // Stay in preparation state until we can move
        }

        /**
         * Verifies if the state should be aborted.
         * <p>
         * Abort conditions: 1. The left leader disappears or changes ID. 2. Traffic state transitions to congestion
         * (undercutting allowed). 3. The undercutting situation is physically resolved (leader is far away or pulling away).
         * </p>
         * @return finish maneuver if no longer required, {@code null} otherwise
         * @throws ParameterException if parameters are missing
         * @throws GtuException if GTU context fails
         * @throws NetworkException if network context fails
         */
        @Override
        public SimpleOperationalPlan abort() throws ParameterException, GtuException, NetworkException
        {
            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
            HeadwayGtu leftLeader = neighbors.getLeader(LateralDirectionality.LEFT);

            // 1. Leader disappeared
            if (leftLeader == null)
            {
                return finishManeuver();
            }

            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            Speed congestionThreshold = this.vehicle.getParameters().getParameter(ParameterTypes.VCONG);
            boolean isFreeFlow = ego.getEgoSpeed().gt(congestionThreshold);

            // 2. Leader ID changed or traffic state changed
            if (!leftLeader.getId().equals(this.maneuverPattern.getShadowingLeftNeighborId()) || !isFreeFlow)
            {
                return finishManeuver();
            }

            // 3. NEW: The situation has naturally resolved (Left Leader drove away)
            Length leftGap = neighbors.getFrontGapDistance(LateralDirectionality.LEFT);
            Speed leftSpeed = leftLeader.getSpeed();
            Speed egoSpeed = ego.getEgoSpeed();

            // Abort if the left neighbor is far away (> 80m)
            // OR if they are accelerating away (speed delta > 1 m/s and gap > 40m)
            boolean isFarAway = leftGap.si > 80.0;
            boolean isPullingAway = (leftSpeed.si > egoSpeed.si + 1.0) && (leftGap.si > 40.0);

            if (isFarAway || isPullingAway)
            {
                return finishManeuver();
            }

            return null; // Continue maneuver
        }

        @Override
        public double getUtility()
        {
            return 0.2;
        }

        @Override
        public String toString()
        {
            return "PreventUndercutting:PrepareLaneChange";
        }
    }
}

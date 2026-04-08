package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.parallel;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;

/**
 * Parallel maneuver pattern that anticipates the speed of vehicles on the merging target lane.
 * <p>
 * This class forms part of <b>Layer 4 (Procedure & Action)</b> in the MiRoVA architecture.
 * As a <b>Parallel Maneuver</b>, it continuously runs alongside standard car-following behavior
 * to actively decelerate the ego vehicle on an on-ramp if the target lane is congested.
 * This ensures a smooth merging process and avoids abrupt braking at the lane drop.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class AnticipatingUpstreamMergingSpeedPattern extends ManeuverPattern {

    /**
     * Constructor for the anticipating merging speed pattern.
     *
     * @param vehicle the tactical planner associated with the ego vehicle
     */
    public AnticipatingUpstreamMergingSpeedPattern(final MirovaTacticalPlanner vehicle) {
        super(PatternType.PARALLEL, vehicle);
        this.initialActionState = () -> new AnticipatingSpeedState(this);

        // Register required context categories
        this.requiredContextKeys.add("Ego");
        this.requiredContextKeys.add("Infrastructure");
        this.requiredContextKeys.add("Neighbors");
    }

    /**
     * Evaluates if the context allows for upstream speed anticipation.
     * <p>
     * Context check ensures that the pattern is only active when the ego lane ends
     * within the extended look-ahead distance.
     * </p>
     *
     * @return {@code true} if the lane ends within the lookahead distance, {@code false} otherwise
     * @throws ParameterException if parameter evaluation fails
     */
    @Override
    public boolean checkContext() throws ParameterException {
        InfrastructureContext infraCtx = this.vehicle.getContext(InfrastructureContext.class);
        Length distToLaneChange = infraCtx.getDistanceToLaneChangeExtendedLookahead();
        Length extendedLookahead = this.vehicle.getParameters().getParameter(MirovaParameters.extendedLookAheadDistance);

        // Pattern is only contextually valid if the ego lane actually ends
        return distToLaneChange.le(extendedLookahead) && distToLaneChange.si > 0.0;
    }

    /**
     * Evaluates if speed anticipation is physically beneficial right now.
     * <p>
     * Ability check ensures that the pattern is only active if the prevailing speed on
     * the target lane is below the congestion threshold, indicating that proactive
     * speed adaptation is beneficial for a smooth merge.
     * </p>
     *
     * @return {@code true} if the target lane is congested but not at a standstill, {@code false} otherwise
     * @throws ParameterException if parameter evaluation fails
     */
    @Override
    public boolean checkAbility() throws ParameterException {
        NeighborsContext neighborsCtx = this.vehicle.getContext(NeighborsContext.class);
        Speed egoSpeed = this.vehicle.getContext(EgoContext.class).getEgoSpeed();
        Speed frontSpeedGap = neighborsCtx.getFrontGapDeltaSpeed(LateralDirectionality.NONE);
        Speed speedLeader = egoSpeed.minus(frontSpeedGap);
        Speed vCong = this.vehicle.getParameters().getParameter(ParameterTypes.VCONG);
        Length frontGap = neighborsCtx.getFrontGapDistance(LateralDirectionality.NONE);

        // Only apply if the target lane is congested but not at a complete standstill,
        // allowing for proactive speed adaptation without causing unnecessary braking
        return speedLeader.lt(vCong) && frontGap.gt(Length.instantiateSI(100.0));
    }

    /**
     * Action state responsible for analyzing the target lane and adapting the ego speed.
     */
    public static class AnticipatingSpeedState extends ActionState {

        /**
         * Constructor.
         *
         * @param maneuverPattern the parent pattern orchestrating this state
         */
        public AnticipatingSpeedState(final ManeuverPattern maneuverPattern) {
            super(maneuverPattern);
        }

        /**
         * Executes control logic to adjust acceleration towards the anticipated merging speed.
         *
         * @return the operational plan for the current time step
         * @throws ParameterException       if a parameter required for calculation is missing
         * @throws OperationalPlanException if generation of the operational plan fails
         * @throws GtuException             if an error occurs within the GTU state
         * @throws NetworkException         if network topology limits calculation
         */
        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, OperationalPlanException, GtuException, NetworkException {
            // This pattern continuously monitors the context and does not autonomously transition
            // to other states, so we set it to non-running to allow for continuous execution
            // as long as context conditions are met.
            this.maneuverPattern.setRunning(false);

            MirovaTacticalPlanner vehicle = this.maneuverPattern.getMirovaTacticalPlanner();
            InfrastructureContext infraCtx = vehicle.getContext(InfrastructureContext.class);
            NeighborsContext neighborsCtx = vehicle.getContext(NeighborsContext.class);
            EgoContext egoCtx = vehicle.getContext(EgoContext.class);

            Speed egoSpeed = egoCtx.getEgoSpeed();
            Speed leaderSpeed = egoSpeed.minus(neighborsCtx.getFrontGapDeltaSpeed(LateralDirectionality.NONE));
            Speed lowSpeedThreshold = new Speed(10.0, SpeedUnit.KM_PER_HOUR);
            Speed targetSpeed = Speed.max(leaderSpeed, lowSpeedThreshold);

            Acceleration targetAcceleration = CarFollowingUtil.approachTargetSpeed(
                    vehicle.getCarFollowingModel(),
                    vehicle.getParameters(),
                    egoSpeed,
                    infraCtx.getCurrentSpeedLimit(),
                    Length.instantiateSI(20.0),
                    targetSpeed);

            Acceleration maxDecel = vehicle.getParameters().getParameter(MirovaParameters.egoDecelerationThreshold);
            targetAcceleration = Acceleration.max(targetAcceleration, maxDecel);

            return new SimpleOperationalPlan(targetAcceleration, this.maneuverPattern.getPatternSpecificTimestep());
        }

        /**
         * Determines the next action state.
         * <p>
         * Parallel patterns act as continuous overlays and do not autonomously transition to other states here.
         * </p>
         *
         * @return {@code null}, keeping the state machine in the current state
         */
        @Override
        public SimpleOperationalPlan next() {
            return null;
        }

        /**
         * Checks whether the maneuver should be aborted.
         * <p>
         * No specific abort behavior needed for this pattern, as it will simply be deactivated
         * by the tactical planner when context conditions are no longer met.
         * </p>
         *
         * @return {@code null}, as no internal abort logic is necessary
         */
        @Override
        public SimpleOperationalPlan abort() {
            return null;
        }
    }
}
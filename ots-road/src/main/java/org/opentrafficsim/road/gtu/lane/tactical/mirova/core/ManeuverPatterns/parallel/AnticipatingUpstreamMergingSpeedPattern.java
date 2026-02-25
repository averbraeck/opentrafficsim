package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.parallel;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
/**
 * Parallel maneuver pattern that anticipates the speed of vehicles on the merging target lane.
 * It actively decelerates the ego vehicle on an on-ramp if the target lane is congested,
 * ensuring a smooth merging process and avoiding abrupt braking at the lane drop.
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class AnticipatingUpstreamMergingSpeedPattern extends ManeuverPattern {

    /**
     * Constructor for the anticipating merging speed pattern.
     * * @param vehicle the tactical planner / ego vehicle
     */
    public AnticipatingUpstreamMergingSpeedPattern(final MirovaTacticalPlanner vehicle) {
        super(PatternType.PARALLEL, vehicle);
        this.initialActionState = () -> new AnticipatingSpeedState(this);

        // Register required context categories
        this.requiredContextKeys.add("Ego");
        this.requiredContextKeys.add("Infrastructure");
        this.requiredContextKeys.add("Neighbors");
    }

    /** {@inheritDoc}
     * Context check ensures that the pattern is only active
     * when the ego lane ends within the extended look-ahead distance,
     *  */
    @Override
    public boolean checkContext() throws ParameterException {
        InfrastructureContext infraCtx = this.vehicle.getContext(InfrastructureContext.class);
        Length distToLaneChange = infraCtx.getDistanceToLaneChangeExtendedLookahead();
        Length extendedLookahead = this.vehicle.getParameters().getParameter(MirovaParameters.extendedLookAheadDistance);
        // Pattern is only contextually valid if the ego lane actually ends
        return distToLaneChange.le(extendedLookahead) && distToLaneChange.si > 0.0;
    }

    /** {@inheritDoc}
     * Ability check ensures that the pattern is only active if the prevailing speed on the target lane is below the congestion threshold,
     * indicating that proactive speed adaptation is beneficial for a smooth merge.
     */
    @Override
    public boolean checkAbility() throws ParameterException {
        NeighborsContext neighborsCtx = this.vehicle.getContext(NeighborsContext.class);
        Speed egoSpeed = this.vehicle.getContext(EgoContext.class).getEgoSpeed();
        Speed frontSpeedGap = neighborsCtx.getFrontGapDeltaSpeed(LateralDirectionality.NONE);
        Speed speedLeader = egoSpeed.minus(frontSpeedGap);
        Speed vCong = this.vehicle.getParameters().getParameter(ParameterTypes.VCONG);
        Length frontGap = neighborsCtx.getFrontGapDistance(LateralDirectionality.NONE);
        return speedLeader.lt(vCong) && frontGap.gt(Length.instantiateSI(100.0)); // Only apply if the target lane is congested but not at a complete standstill, allowing for proactive speed adaptation without causing unnecessary braking
    }

    /**
     * Action state responsible for analyzing the target lane and adapting the ego speed.
     */
    private class AnticipatingSpeedState extends ActionState {

        /**
         * Constructor.
         * * @param maneuverPattern the parent pattern
         */
        public AnticipatingSpeedState(final ManeuverPattern maneuverPattern) {
            super(maneuverPattern);
        }

        /** {@inheritDoc} */
        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, OperationalPlanException, GtuException, NetworkException {
            this.maneuverPattern.setRunning(false); // This pattern continuously monitors the context and does not autonomously transition to other states, so we set it to non-running to allow for continuous execution as long as context conditions are met
            //Speed vCong = this.vehicle.getParameters().getParameter(ParameterTypes.VCONG).minus(new Speed(15.0, SpeedUnit.KM_PER_HOUR)); // Adding a small buffer to the congestion threshold to allow for proactive adaptation
            InfrastructureContext infraCtx = this.vehicle.getContext(InfrastructureContext.class);
            NeighborsContext neighborsCtx = this.vehicle.getContext(NeighborsContext.class);
            EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);
            Speed egoSpeed = egoCtx.getEgoSpeed();
            Speed leaderSpeed = egoSpeed.minus(neighborsCtx.getFrontGapDeltaSpeed(LateralDirectionality.NONE));
            Speed lowSpeedThreshold = new Speed(10.0, SpeedUnit.KM_PER_HOUR);
            Speed targetSpeed = Speed.max(leaderSpeed, lowSpeedThreshold);

            Acceleration targetAcceleration = CarFollowingUtil.approachTargetSpeed(
                    this.vehicle.getCarFollowingModel(),
                    this.vehicle.getParameters(),
                    egoSpeed,
                    infraCtx.getCurrentSpeedLimit(),
                    Length.instantiateSI(20.0),
                    targetSpeed);

            Acceleration maxDecel = this.vehicle.getParameters().getParameter(MirovaParameters.egoDecelerationThreshold);
            targetAcceleration = Acceleration.max(targetAcceleration, maxDecel);
//            System.out.println("GTU " + this.vehicle.getGtu().getId() + " applying anticipating"
//                    + " merging speed " + targetSpeed.toDisplayString(SpeedUnit.KM_PER_HOUR)
//                    + " pattern with target acceleration: " + targetAcceleration);
            return new SimpleOperationalPlan(targetAcceleration, this.maneuverPattern.getPatternSpecificTimestep());
        }

        /** {@inheritDoc} */
        @Override
        public SimpleOperationalPlan next() throws OperationalPlanException, ParameterException, GtuException, NetworkException {
            // Parallel patterns act as continuous overlays and do not autonomously transition to other states here
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public SimpleOperationalPlan abort() throws ParameterException, OperationalPlanException, GtuException, NetworkException {
            // No specific abort behavior needed for this pattern, as it will simply be deactivated when context conditions are no longer met
            return null;
        }
    }
}
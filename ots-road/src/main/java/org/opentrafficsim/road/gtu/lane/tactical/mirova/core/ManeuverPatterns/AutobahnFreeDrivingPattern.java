//package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns;
//
//import org.opentrafficsim.base.parameters.ParameterException;
//import org.opentrafficsim.core.gtu.GtuException;
//import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
//import org.opentrafficsim.core.network.LateralDirectionality;
//import org.opentrafficsim.core.network.NetworkException;
//import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
//import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.DiscretionaryLaneChangePatternOld.ActionStateCompleteLaneChange;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.*;
//import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
//import org.opentrafficsim.road.network.lane.Lane;
//import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
//import org.djunits.value.vdouble.scalar.Acceleration;
//import org.djunits.value.vdouble.scalar.Length;
//import org.djunits.value.vdouble.scalar.Speed;
//import org.opentrafficsim.base.parameters.ParameterException;
//import org.opentrafficsim.base.parameters.ParameterTypes;
//import org.opentrafficsim.base.parameters.Parameters;
//import org.opentrafficsim.core.network.LateralDirectionality;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.*;
//
///**
// * AutobahnFreeDrivingPattern
// * --------------------------
// *
// * A unified default tactical behavior model for German motorways.
// * It covers:
// *  - Free driving / following
// *  - Free left lane change (overtaking)
// *  - Free right lane change (Rechtsfahrgebot)
// *  - Right-overtake prevention (§5 StVO)
// *  - Falling behind a slower left-lane vehicle when side-by-side
// *
// * All logic is embedded in a coherent finite state machine.
// */
//public class AutobahnFreeDrivingPattern extends ManeuverPattern {
//
//
//    public AutobahnFreeDrivingPattern(final KnowledgeChunk kc) throws ParameterException {
//        super(PatternType.COOPERATIVE, kc);
//        this.initialActionState = new FreeDrivingState(this);
//
//    }
//
//    @Override
//    public boolean checkContext() {
//        return true; // always relevant
//    }
//
//    @Override
//    public boolean checkAbility() {
//        return true; // handled in states
//    }
//
//
//    public static class FreeDrivingState extends ActionState {
//
//        /** Threshold for discretionary lane change (from Mirova concept). */
//        private final double D_FREE;
//        /** Last lane change direction to prevent immediate reversals. */
//        public LateralDirectionality lastLaneChangeDirection;
//
//        public FreeDrivingState(final ManeuverPattern pattern) throws ParameterException {
//            super(pattern);
//            this.D_FREE = pattern.getKnowledgeChunk().getMirovaTacticalPlanner().getDFree();
//            this.lastLaneChangeDirection = LateralDirectionality.NONE;
//        }
//
//        public FreeDrivingState(final ManeuverPattern pattern, final LateralDirectionality lastLaneChangeDirection) throws ParameterException {
//            super(pattern);
//            this.D_FREE = pattern.getKnowledgeChunk().getMirovaTacticalPlanner().getDFree();
//            this.lastLaneChangeDirection = lastLaneChangeDirection;
//        }
//
//        @Override
//        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException {
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//            Speed congestionThreshold = this.vehicle.getParameters().getParameter(ParameterTypes.VCONG);
//            // Basic car-following
//            Acceleration acc = ego.getCurrentCarFollowingAcceleration();
//
//
//            // Falling behind left-lane vehicle when side-by-side
//            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
//
//            if (neigh.checkRightSideOvertakingAhead() && ego.getEgoSpeed().gt(congestionThreshold)) {
//                LateralDirectionality leftDir = LateralDirectionality.LEFT;
//                Length leftDistHeadway = neigh.getFrontGapDistance(leftDir); //.plus(Length.instantiateSI(4.0));
//                HeadwayGtu leftLeader = neigh.getLeader(leftDir);
//                Speed leftLeaderSpeed = leftLeader.getSpeed();
//                SpeedLimitInfo speedLimit = infra.getCurrentSpeedLimit();
//
//                Acceleration aLeftCf = CarFollowingUtil.followSingleLeader(
//                        this.vehicle.getCarFollowingModel(),
//                        this.vehicle.getParameters(),
//                        ego.getEgoSpeed(),
//                        speedLimit,
//                        leftDistHeadway,
//                        leftLeaderSpeed);
//                if (aLeftCf.gt(Acceleration.instantiateSI(-8.0))) {
//                   // System.out.println(this.vehicle.getGtu().getId() + ": Preventing right-side overtaking by decelerating to fall behind left-lane vehicle.");
//                    acc = Acceleration.min(acc, aLeftCf);
//                }
//                else {
//                    //System.out.println(this.vehicle.getGtu().getId() + ": Too fast to fall behind safely.");
//                }
//
//            }
//            // sets running maneuver flag to false because this is not a real maneuver state
//            this.vehicle.setRunningManeuver(false);
//            return new SimpleOperationalPlan(
//                    acc,
//                    this.vehicle.getGtu().getParameters().getParameter(ParameterTypes.DT)
//                    );
//
//        }
//
//        /**
//         * Checks for lane change desire and feasibility to initiate a lane change.
//         * @throws NetworkException
//         * @throws GtuException
//         * @throws IllegalArgumentException
//         * @throws NullPointerException
//         */
//        @Override
//        public SimpleOperationalPlan next()
//                throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
//
//            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//
//            Desire desire = this.vehicle.getLaneChangeDesire();
//            LateralDirectionality laneChangeDirection = desire.dominantDirection();
//
//            Double lcThreshold = this.D_FREE;
//            /*
//            if (laneChangeDirection.isRight()) {
//                lcThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.DRIGHT);
//            }
//            */
//            // Check for lane change desire exceeding threshold: this could be due to overtaking or right-lane return
//            if (desire.magnitude() >= lcThreshold &&
//                    // prevent immediate reversal of lane change
//                    (laneChangeDirection == this.lastLaneChangeDirection
//                    || this.vehicle.getTimeSinceLastLaneChange().ge(this.vehicle.getParameters().getParameter(MirovaParameters.socialInteractionCooldown)))) {
//                if (neigh.getIfLaneChangePossible(laneChangeDirection)) {
//                    return transitionTo(new ExecuteLaneChange(
//                            this.maneuverPattern, laneChangeDirection));
//                }
//            }
//
//            // check for potential upcoming right-side overtaking
//            if (neigh.getRightSideOvertakingAhead()
//                    && this.vehicle.getMandatoryLaneChangeDesire().getLeft() >= 0.0
//                    && ego.getEgoSpeed().gt(this.vehicle.getParameters().getParameter(ParameterTypes.VCONG))) {
//                // check lane change to left to prevent right-side overtaking
//
//                if (neigh.getIfLaneChangePossible(LateralDirectionality.LEFT)) {
//                    return transitionTo(new ExecuteLaneChange(
//                            this.maneuverPattern, LateralDirectionality.LEFT));
//                }
//            }
//
//            return null; // remain in free driving
//        }
//
//        @Override
//        public SimpleOperationalPlan abort() {
//            return null; // free driving cannot abort
//        }
//
//        @Override
//        public String toString() {
//            return "FreeDrivingState";
//        }
//
//    }
//
//    public static class ExecuteLaneChange extends ActionState {
//
//        /** Target direction of the lane change (LEFT or RIGHT). */
//        private final LateralDirectionality direction;
//
//
//        /** Desire hysteresis for abort stability. */
//        private static final double DESIRE_HYSTERESIS = 0.1;
//
//        /** Cached origin lane to detect completion. */
//        private final Lane originLane;
//
//
//        // ----------------------------------------------------------------------
//        // Construction
//        // ----------------------------------------------------------------------
//
//        /** ActionStatePerformLaneChange constructor.
//         * @param pattern
//         * @param direction
//         */
//        public ExecuteLaneChange(final ManeuverPattern pattern, final LateralDirectionality direction) {
//            super(pattern);
//            this.direction = direction;
//
//            this.originLane = this.vehicle.getGtu().getLane();
//        }
//
//        // ----------------------------------------------------------------------
//        // Core control logic
//        // ----------------------------------------------------------------------
//
//        /**
//         * Executes longitudinal control using a simplified Two-Leader Car-Following logic.
//         * <p>
//         * The ego vehicle simultaneously considers the leader on its current lane and
//         * the leader on the target lane. The resulting acceleration is the most restrictive
//         * (minimum) across these influences.
//         * </p>
//         * @throws NetworkException
//         * @throws GtuException
//         */
//        @Override
//        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException {
//            InfrastructureContext infraCtx = this.vehicle.getContext(InfrastructureContext.class);
//            NeighborsContext neighborsCtx = this.vehicle.getContext(NeighborsContext.class);
//            EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);
//
//            Speed egoSpeed = egoCtx.getEgoSpeed();
//            Parameters params = this.vehicle.getGtu().getParameters();
//
//
//
//            // Start with relaxed car-following acceleration (already includes Desire effects)
//            Acceleration minAcc = egoCtx.getCurrentCarFollowingAcceleration();
//
//
//            // Add target-lane leader constraint
//            if (this.vehicle.getGtu().getLane() == this.originLane) {
//                // Only consider target-lane leader if still on origin lane;
//                HeadwayGtu targetLeader = neighborsCtx.getLeader(this.direction);
//                if (targetLeader != null) {
//                    Acceleration aTarget = CarFollowingUtil.followSingleLeader(
//                            this.vehicle.getCarFollowingModel(),
//                            params,
//                            egoSpeed,
//                            infraCtx.getCurrentSpeedLimit(),
//                            targetLeader.getDistance(),
//                            targetLeader.getSpeed());
//                    minAcc = Acceleration.min(minAcc, aTarget);
//                }
//            }
//
//            SimpleOperationalPlan plan =new SimpleOperationalPlan(
//                    minAcc,
//                    params.getParameter(ParameterTypes.DT),
//                    this.direction);
//
//            if (this.direction == LateralDirectionality.LEFT) {
//                plan.setIndicatorIntentLeft();
//            } else if (this.direction == LateralDirectionality.RIGHT) {
//                plan.setIndicatorIntentRight();
//            }
//
//            return plan;
////            if (this.vehicle.getLaneChange().isChangingLane()) {
////                // Create operational plan
////                return new SimpleOperationalPlan(
////                        minAcc,
////                        params.getParameter(ParameterTypes.DT),
////                        LateralDirectionality.NONE);
////            } else {
////                // Create operational plan
////                return new SimpleOperationalPlan(
////                        minAcc,
////                        params.getParameter(ParameterTypes.DT),
////                        this.direction);
////            }
//            }
//
//        // ----------------------------------------------------------------------
//        // Transitions
//        // ----------------------------------------------------------------------
//
//        /**
//         * Proceeds to {@link ActionStateCompleteLaneChange} when the lane change is completed.
//         * @return
//         * @throws NetworkException
//         * @throws GtuException
//         * @throws IllegalArgumentException
//         * @throws NullPointerException
//         */
//        @Override
//        public SimpleOperationalPlan next() throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
//            boolean finished = !this.vehicle.getLaneChange().isChangingLane()
//                    && !this.originLane.equals(this.vehicle.getGtu().getLane());
//
//            if (finished) {
//                ActionState nextState = new FreeDrivingState(this.maneuverPattern, this.direction);
//                return transitionTo(nextState);
//            }
//            return null;
//        }
//
//        /**
//         * Checks whether the lane-change should be aborted (safety or desire violation).
//         * @return
//         */
//        @Override
//        public SimpleOperationalPlan abort() throws ParameterException, OperationalPlanException {
//            // abort Lane Change is currently not supported
//        //        NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
//        //        Acceleration bDes = this.vehicle.getGtu().getParameters().getParameter(ParameterTypes.B);
//        //        Acceleration egoDecel = neighbors.getEgoDeceleration(this.direction);
//        //        Acceleration follDecel = neighbors.getFollowerDeceleration(this.direction);
//        //
//        //        boolean unsafe = egoDecel.lt(bDes) || follDecel.lt(bDes);
//        //        boolean lowDesire = this.vehicle.getDesire() < this.vehicle.getDFree() - DESIRE_HYSTERESIS;
//        //
//        //        if (unsafe || lowDesire) {
//        //            ActionState nextState = new ActionStateAbortLaneChange(this.maneuverPattern, this.direction);
//        //            transitionTo(nextState);
//        //        }
//            return null;
//        }
//
//        @Override
//        public String toString() {
//            return "ExecuteLaneChange[" + this.direction + "]";
//        }
//}
//}
//

package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.parallel;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterableSet;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.exclusive.SimpleLaneChangePattern.PerformLaneChangeState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.MacroTrafficContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;

/**
 * Handles cooperative behavior towards merging vehicles on adjacent lanes.
 * <p>
 * This class forms part of <b>Layer 4 (Procedure & Action)</b> in the MiRoVA architecture.
 * As a <b>Parallel Maneuver</b>, it continuously runs alongside standard car-following behavior
 * and implements a strict multi-step verification process to determine if and how the ego
 * vehicle should decelerate to open a gap for a merging neighbor.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class MergeCooperationPattern extends ManeuverPattern {

    /** The ID of the vehicle we are actively cooperating with. */
    private String activeMergeCandidateId = null;

    /** The lateral direction from which the candidate is merging. */
    private LateralDirectionality directionOfMergeCandidate = null;

    /** Distance threshold to consider cooperation near a lane drop. */
    private static final Length DISTANCE_THRESHOLD_MERGE_COOPERATION = Length.instantiateSI(250.0);

    /** Time-to-lane-end threshold to consider cooperation. */
    private static final Duration TIME_THRESHOLD_MERGE_COOPERATION = Duration.instantiateSI(15.0);

    /** List of lateral directions that currently require cooperation monitoring. */
    private ArrayList<LateralDirectionality> listLanesWithCooperationNeeds = new ArrayList<>();

    /** Enum describing the relative position of the candidate to the ego vehicle. */
    public enum RelativeCandidatePosition {
        /** Candidate is ahead of ego. */
        AHEAD,
        /** Candidate is behind ego. */
        BEHIND
    }

    /** The last recorded relative position of the candidate. */
    private RelativeCandidatePosition lastCandidatePosition = null;

    /**
     * Constructs a new MergeCooperationPattern.
     *
     * @param vehicle the tactical planner executing this pattern
     */
    public MergeCooperationPattern(final MirovaTacticalPlanner vehicle) {
        super(PatternType.PARALLEL, vehicle);
        this.initialActionState = () -> new PreemptiveDecelerationState(this);
        this.requiredContextKeys.add("Ego");
        this.requiredContextKeys.add("Neighbors");
        this.requiredContextKeys.add("Infrastructure");
    }

    /**
     * Determines if cooperation is necessary and feasible based on indicator state.
     *
     * @return {@code true} if a neighbor indicates a desire to merge into the ego lane, {@code false} otherwise
     */
    @Override
    public boolean checkAbility() {
        NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);
        for (LateralDirectionality dir : this.listLanesWithCooperationNeeds) {
            Iterable<HeadwayGtu> potentialCandidates = neighbors.getLeaders(dir);
            if (potentialCandidates != null) {
                for (HeadwayGtu candidate : potentialCandidates) {
                    boolean indicatesTowardsUs = (dir.isRight() && candidate.isLeftTurnIndicatorOn())
                                              || (dir.isLeft() && candidate.isRightTurnIndicatorOn());
                    if (indicatesTowardsUs) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if the infrastructure context suggests a potential need for cooperation.
     * Scans for merges (incoming lanes) or lane ends within the lookahead distance.
     *
     * @return {@code true} if a relevant infrastructure event is detected, {@code false} otherwise
     * @throws ParameterException if parameter retrieval fails
     */
    @Override
    public boolean checkContext() throws ParameterException {
        this.listLanesWithCooperationNeeds.clear();

        InfrastructureContext infra = this.vehicle.getContextManager().getCategory("Infrastructure", InfrastructureContext.class);
        MacroTrafficContext macro = this.vehicle.getContextManager().getCategory("MacroTraffic", MacroTrafficContext.class);

        Speed leftLaneSpeed = Speed.POSITIVE_INFINITY;
        Speed rightLaneSpeed = Speed.POSITIVE_INFINITY;
        try {
            leftLaneSpeed = macro.getAverageSpeed(RelativeLane.LEFT);
            rightLaneSpeed = macro.getAverageSpeed(RelativeLane.RIGHT);
        } catch (OperationalPlanException | ParameterException exception) {
            exception.printStackTrace();
        }

        EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);
        Speed egoSpeed = ego.getEgoSpeed();
        Speed vCong = this.vehicle.getParameters().getParameter(ParameterTypes.VCONG);

        Length distanceToEndLeft = infra.getDistanceToLaneEnd(RelativeLane.LEFT);
        Length distanceToEndRight = infra.getDistanceToLaneEnd(RelativeLane.RIGHT);

        if (egoSpeed.si > 15) {
            if (distanceToEndLeft != null) {
                Duration timeToEndLeft = Duration.instantiateSI(distanceToEndLeft.si / egoSpeed.si);
                if (timeToEndLeft.lt(TIME_THRESHOLD_MERGE_COOPERATION)) {
                    this.listLanesWithCooperationNeeds.add(LateralDirectionality.LEFT);
                } else if (leftLaneSpeed != null && leftLaneSpeed.lt(vCong) && leftLaneSpeed.si < egoSpeed.si + 3.0) {
                    // If no lane end, but adjacent lane is significantly slower, cooperate to merge into it.
                    this.listLanesWithCooperationNeeds.add(LateralDirectionality.LEFT);
                }
            }
            if (distanceToEndRight != null) {
                Duration timeToEndRight = Duration.instantiateSI(distanceToEndRight.si / egoSpeed.si);
                if (timeToEndRight.lt(TIME_THRESHOLD_MERGE_COOPERATION)) {
                    this.listLanesWithCooperationNeeds.add(LateralDirectionality.RIGHT);
                } else if (rightLaneSpeed != null && rightLaneSpeed.lt(vCong) && rightLaneSpeed.si < egoSpeed.si + 3.0) {
                    this.listLanesWithCooperationNeeds.add(LateralDirectionality.RIGHT);
                }
            }
        } else {
            // Stationary vehicle - check only distance
            if (distanceToEndLeft != null && distanceToEndLeft.lt(DISTANCE_THRESHOLD_MERGE_COOPERATION)) {
                this.listLanesWithCooperationNeeds.add(LateralDirectionality.LEFT);
            }
            if (distanceToEndRight != null && distanceToEndRight.lt(DISTANCE_THRESHOLD_MERGE_COOPERATION)) {
                this.listLanesWithCooperationNeeds.add(LateralDirectionality.RIGHT);
            }
        }

        return !this.listLanesWithCooperationNeeds.isEmpty();
    }

    /**
     * Retrieves the direction (LEFT/RIGHT) of the active merge candidate.
     *
     * @return the {@link LateralDirectionality} of the candidate, or {@code null} if none
     */
    public LateralDirectionality getDirectionOfMergeCandidate() {
        return this.directionOfMergeCandidate;
    }

    /**
     * Retrieves the current {@link HeadwayGtu} object for the stored candidate ID.
     * Scans the NeighborsContext to find the fresh snapshot.
     *
     * @return the updated HeadwayGtu, or {@code null} if lost/merged
     */
    public HeadwayGtu getActiveMergeCandidate() {
        if (this.activeMergeCandidateId == null) return null;

        NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);

        // Prioritize the last known position to optimize search
        for (LateralDirectionality dir : this.listLanesWithCooperationNeeds) {
            if (this.lastCandidatePosition != null && this.lastCandidatePosition == RelativeCandidatePosition.AHEAD) {
                Iterable<HeadwayGtu> leaders = neighbors.getLeaders(dir);
                if (leaders != null) {
                    for (HeadwayGtu gtu : leaders) {
                        if (gtu.getId().equals(this.activeMergeCandidateId)) {
                            this.lastCandidatePosition = RelativeCandidatePosition.AHEAD;
                            return gtu;
                        }
                    }
                }
            }
        }
        return null; // Lost contact
    }

    /**
     * Retrieves the last known relative position (ahead/behind) of the merge candidate.
     *
     * @return the relative candidate position, or {@code null} if unknown
     */
    public RelativeCandidatePosition getLastCandidatePosition() {
        return this.lastCandidatePosition;
    }

    /**
     * Updates the last known relative position of the merge candidate.
     *
     * @param position the new relative position
     */
    public void setLastCandidatePosition(final RelativeCandidatePosition position) {
        this.lastCandidatePosition = position;
    }

    /**
     * Scans for parallel merge candidates in close proximity within the relevant lanes.
     * Used as a fallback when ego speed is very low to allow opportunistic merges.
     *
     * @return a suitable parallel merge candidate, or {@code null} if none found
     * @throws ParameterException       if parameters cannot be read
     * @throws NullPointerException     if required context is null
     * @throws IllegalArgumentException if arguments are invalid
     */
    public HeadwayGtu findParallelMergeCandidate() throws ParameterException, NullPointerException, IllegalArgumentException {
        NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);
        for (LateralDirectionality dir : this.listLanesWithCooperationNeeds) {
            if (neighbors.isGtuAlongside(dir)) {
                HeadwayGtu parallelMergeCandidate = neighbors.getLeader(dir);
                if (parallelMergeCandidate != null) {
                    Length frontGap = neighbors.getFrontGapDistance(dir);
                    if (frontGap != null && frontGap.si < 0.0) {
                        this.activeMergeCandidateId = parallelMergeCandidate.getId();
                        this.directionOfMergeCandidate = dir;
                        this.lastCandidatePosition = RelativeCandidatePosition.AHEAD;
                        return parallelMergeCandidate;
                    }
                }
            }
        }
        return null;
    }

    /* =========================================================================================
     * STATE: PREEMPTIVE_DECELERATION
     * ========================================================================================= */

    /**
     * State to gently decelerate and evaluate if a merging vehicle needs assistance.
     */
    public static class PreemptiveDecelerationState extends ActionState {

        private final MergeCooperationPattern maneuverPattern;

        /**
         * Constructor.
         *
         * @param pattern the parent maneuver pattern
         */
        public PreemptiveDecelerationState(final MergeCooperationPattern pattern) {
            super(pattern);
            this.maneuverPattern = pattern;
            this.maneuverPattern.activeMergeCandidateId = null;
            this.maneuverPattern.directionOfMergeCandidate = null;
            this.maneuverPattern.lastCandidatePosition = null;
        }

        /**
         * Scans for valid candidates that need cooperation to merge.
         *
         * @return {@code true} if a valid candidate is locked in, {@code false} otherwise
         */
        private boolean searchCooperationCandidate() {
            NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);
            EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);
            InfrastructureContext infrastructure = this.vehicle.getContextManager().getCategory("Infrastructure", InfrastructureContext.class);
            HeadwayGtu egoLeader = neighbors.getCurrentLeader();

            this.maneuverPattern.activeMergeCandidateId = null;

            for (LateralDirectionality dir : this.maneuverPattern.listLanesWithCooperationNeeds) {
                Iterable<HeadwayGtu> adjacentLeaders = neighbors.getLeaders(dir);
                if (adjacentLeaders == null) continue;

                for (HeadwayGtu candidate : adjacentLeaders) {
                    // CHECK 1: Lane Change Wish (Indicators)
                    boolean indicatesTowardsUs = (dir.isRight() && candidate.isLeftTurnIndicatorOn())
                                              || (dir.isLeft() && candidate.isRightTurnIndicatorOn());
                    if (!indicatesTowardsUs) continue;

                    // CHECK 2: Can Ego-Leader handle it?
                    boolean leaderSuitable = false;
                    if (egoLeader != null) {
                        SortedSet<HeadwayGtu> tempSet = new TreeSet<>();
                        tempSet.add(candidate);
                        PerceptionIterable<HeadwayGtu> iterable = new PerceptionIterableSet<>(tempSet);
                        Acceleration potentialLeaderDeceleration = null;

                        try {
                            Duration temporaryHeadway = egoLeader.getParameters().getParameter(ParameterTypes.T)
                                    .times(this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange));
                            egoLeader.getParameters().setParameterResettable(ParameterTypes.T, temporaryHeadway);

                            potentialLeaderDeceleration = CarFollowingUtil.followSingleLeader(
                                    egoLeader.getCarFollowingModel(),
                                    egoLeader.getParameters(),
                                    egoLeader.getSpeed(),
                                    infrastructure.getCurrentSpeedLimit(),
                                    candidate.getDistance().minus(egoLeader.getDistance()),
                                    candidate.getSpeed());

                            egoLeader.getParameters().resetParameter(ParameterTypes.T);
                        } catch (ParameterException exception) {
                            exception.printStackTrace();
                        }

                        try {
                            if (potentialLeaderDeceleration != null &&
                                potentialLeaderDeceleration.si > this.vehicle.getParameters().getParameter(MirovaParameters.cooperativeDecelerationThreshold).si) {
                                leaderSuitable = true;
                            }
                        } catch (ParameterException exception) {
                            exception.printStackTrace();
                        }
                    }

                    if (leaderSuitable) {
                        continue;
                    }

                    // CHECK 3: Can Ego brake reasonably?
                    Acceleration requiredDeceleration = null;
                    try {
                        Duration temporaryHeadway = this.vehicle.getParameters().getParameter(ParameterTypes.T)
                                .times(this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange));
                        this.vehicle.getParameters().setParameterResettable(ParameterTypes.T, temporaryHeadway);

                        requiredDeceleration = CarFollowingUtil.followSingleLeader(
                                this.vehicle.getCarFollowingModel(),
                                this.vehicle.getParameters(),
                                ego.getEgoSpeed(),
                                infrastructure.getCurrentSpeedLimit(),
                                candidate);

                        this.vehicle.getParameters().resetParameter(ParameterTypes.T);
                    } catch (ParameterException exception) {
                        exception.printStackTrace();
                    }

                    try {
                        if (requiredDeceleration != null &&
                            requiredDeceleration.si > this.vehicle.getParameters().getParameter(MirovaParameters.cooperativeDecelerationThreshold).si) {
                            // All checks passed. We are the chosen one.
                            this.maneuverPattern.activeMergeCandidateId = candidate.getId();
                            this.maneuverPattern.directionOfMergeCandidate = dir;
                            this.maneuverPattern.lastCandidatePosition = RelativeCandidatePosition.AHEAD;
                            return true;
                        }
                    } catch (ParameterException exception) {
                        exception.printStackTrace();
                    }
                }
            }
            return false;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, OperationalPlanException, GtuException, NetworkException {
            this.maneuverPattern.setRunning(false);
            this.maneuverPattern.setCurrentActionState(this);

            if (this.maneuverPattern.getDirectionOfMergeCandidate() != null) {
                MacroTrafficContext macro = this.vehicle.getContextManager().getCategory("MacroTraffic", MacroTrafficContext.class);
                EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);
                Speed vCong = this.vehicle.getParameters().getParameter(ParameterTypes.VCONG);
                Speed targetLaneSpeed = macro.getAverageSpeed(this.maneuverPattern.directionOfMergeCandidate.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT);
                InfrastructureContext infrastructure = this.vehicle.getContextManager().getCategory("Infrastructure", InfrastructureContext.class);

                Acceleration aCoopMax = this.vehicle.getParameters().getParameter(MirovaParameters.preemptiveCooperativeDeceleration);
                Speed targetSpeed = targetLaneSpeed.gt(vCong) ? targetLaneSpeed : vCong;

                Acceleration aCoop = CarFollowingUtil.approachTargetSpeed(
                        this.vehicle.getCarFollowingModel(),
                        this.vehicle.getParameters(),
                        ego.getEgoSpeed(),
                        infrastructure.getCurrentSpeedLimit(),
                        Length.instantiateSI(10.0),
                        targetSpeed
                        );

                aCoop = aCoop.gt(aCoopMax) ? aCoop : aCoopMax;
                return new SimpleOperationalPlan(aCoop, this.vehicle.getParameters().getParameter(ParameterTypes.DT));
            }
            return null;
        }

        @Override
        public SimpleOperationalPlan next() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
            EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);

            if (this.maneuverPattern.getDirectionOfMergeCandidate() != null) {
                if (ego.getEgoSpeed().gt(this.vehicle.getParameters().getParameter(ParameterTypes.VCONG))
                    && this.vehicle.getParameters().getParameter(MirovaParameters.cooperativeLaneChangesEnabled)) {

                    NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);
                    LateralDirectionality dir = this.maneuverPattern.getDirectionOfMergeCandidate();
                    LateralDirectionality oppositeDir = dir.isLeft() ? LateralDirectionality.RIGHT : LateralDirectionality.LEFT;
                    MacroTrafficContext macro = this.vehicle.getContextManager().getCategory("MacroTraffic", MacroTrafficContext.class);
                    RelativeLane targetLane = dir.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;
                    Speed vTargetLane = macro.getAverageSpeed(targetLane);

                    // Courtesy Lane Change to make room
                    if (neighbors.checkIfLaneChangeIsPossible(oppositeDir) && ego.getEgoSpeed().si > (vTargetLane.si - 5.0)) {
                        return transitionTo(new PerformLaneChangeState(this.maneuverPattern, oppositeDir));
                    }
                }
            }

            if (ego.getEgoSpeed().lt(new Speed(50.0, SpeedUnit.KM_PER_HOUR))) {
                this.maneuverPattern.findParallelMergeCandidate();
                if (this.maneuverPattern.activeMergeCandidateId != null) {
                    return transitionTo(new OpenGapCongestedState(this.maneuverPattern));
                }
            }

            if (searchCooperationCandidate()) {
                return transitionTo(new OpenGapState(this.maneuverPattern));
            }
            return null;
        }

        @Override
        public SimpleOperationalPlan abort() throws ParameterException, OperationalPlanException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
            return null;
        }

        @Override
        public String toString() {
            return "PreemptiveDecelerationState";
        }
    }

    /* =========================================================================================
     * STATE: OPEN_GAP
     * ========================================================================================= */

    /**
     * State where ego actively brakes to allow the target vehicle to merge.
     */
    public static class OpenGapState extends ActionState {

        private final MergeCooperationPattern maneuverPattern;
        private Acceleration aCoop = Acceleration.NaN;
        private HeadwayGtu mergeCandidate = null;

        /**
         * Constructor.
         *
         * @param pattern the parent pattern
         */
        public OpenGapState(final MergeCooperationPattern pattern) {
            super(pattern);
            this.maneuverPattern = pattern;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, OperationalPlanException, GtuException, NetworkException {
            this.maneuverPattern.setRunning(true);
            this.maneuverPattern.setCurrentActionState(this);

            EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);
            Acceleration aCF = ego.getCurrentCarFollowingAcceleration();

            Acceleration chosenAcceleration = aCF.lt(this.aCoop) ? aCF : this.aCoop;
            return new SimpleOperationalPlan(chosenAcceleration, this.vehicle.getParameters().getParameter(ParameterTypes.DT));
        }

        @Override
        public SimpleOperationalPlan next() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
            if (this.mergeCandidate.isLeftTurnIndicatorOn() == false && this.mergeCandidate.isRightTurnIndicatorOn() == false) {
                return finishManeuver();
            }

            EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);
            if (ego.getEgoSpeed().gt(this.vehicle.getParameters().getParameter(ParameterTypes.VCONG))
                && this.vehicle.getParameters().getParameter(MirovaParameters.cooperativeLaneChangesEnabled)) {

                NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);
                LateralDirectionality dir = this.maneuverPattern.getDirectionOfMergeCandidate();
                LateralDirectionality oppositeDir = dir.isLeft() ? LateralDirectionality.RIGHT : LateralDirectionality.LEFT;
                MacroTrafficContext macro = this.vehicle.getContextManager().getCategory("MacroTraffic", MacroTrafficContext.class);
                RelativeLane targetLane = dir.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;
                Speed vTargetLane = macro.getAverageSpeed(targetLane);

                if (neighbors.checkIfLaneChangeIsPossible(oppositeDir) && ego.getEgoSpeed().si > (vTargetLane.si - 5.0)) {
                    return transitionTo(new PerformLaneChangeState(this.maneuverPattern, oppositeDir));
                }
            }

            if (ego.getEgoSpeed().lt(new Speed(30.0, SpeedUnit.KM_PER_HOUR))) {
                if (this.maneuverPattern.findParallelMergeCandidate() != null) {
                    return transitionTo(new OpenGapCongestedState(this.maneuverPattern));
                }
            }

            return null;
        }

        @Override
        public SimpleOperationalPlan abort() throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
            EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);
            this.mergeCandidate = this.maneuverPattern.getActiveMergeCandidate();

            if (this.mergeCandidate == null) {
                return finishManeuver();
            }

            InfrastructureContext infrastructure = this.vehicle.getContextManager().getCategory("Infrastructure", InfrastructureContext.class);

            this.aCoop = CarFollowingUtil.followSingleLeader(
                    this.vehicle.getCarFollowingModel(),
                    this.vehicle.getParameters(),
                    ego.getEgoSpeed(),
                    infrastructure.getCurrentSpeedLimit(),
                    this.mergeCandidate);

            if (this.aCoop.si < this.vehicle.getParameters().getParameter(MirovaParameters.cooperativeDecelerationThreshold).si) {
                this.maneuverPattern.setRunning(false);
                this.aCoop = Acceleration.POSITIVE_INFINITY;
            }
            return null;
        }

        @Override
        public String toString() {
            return "OpenGapState";
        }
    }

    /* =========================================================================================
     * STATE: OPEN_GAP_CONGESTED
     * ========================================================================================= */

    /**
     * State for opening a gap in congested, slow-moving traffic.
     */
    public static class OpenGapCongestedState extends ActionState {

        private final MergeCooperationPattern maneuverPattern;
        private HeadwayGtu mergeCandidate = null;
        private Acceleration aCoop = null;

        /**
         * Constructor.
         *
         * @param pattern the parent pattern
         */
        public OpenGapCongestedState(final MergeCooperationPattern pattern) {
            super(pattern);
            this.maneuverPattern = pattern;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, OperationalPlanException, GtuException, NetworkException {
            this.maneuverPattern.setRunning(true);
            this.maneuverPattern.setCurrentActionState(this);

            InfrastructureContext infrastructure = this.vehicle.getContextManager().getCategory("Infrastructure", InfrastructureContext.class);
            NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);

            if (this.mergeCandidate.equals(neighbors.getLeader(LateralDirectionality.NONE))) {
                Duration laneChangeDesiredHeadDuration = this.vehicle.getParameters().getParameter(ParameterTypes.T).times(this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange));
                this.vehicle.setTargetDesiredHeadway(laneChangeDesiredHeadDuration);
                this.vehicle.getParameters().setParameterResettable(ParameterTypes.S0, Length.instantiateSI(0.5));
            }

            this.aCoop = CarFollowingUtil.followSingleLeader(
                    this.vehicle.getCarFollowingModel(),
                    this.vehicle.getParameters(),
                    this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed(),
                    infrastructure.getCurrentSpeedLimit(),
                    this.mergeCandidate);

            if (this.mergeCandidate.equals(neighbors.getLeader(LateralDirectionality.NONE))) {
                this.vehicle.getParameters().resetParameter(ParameterTypes.S0);
            }

            Acceleration decelThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.preemptiveCooperativeDeceleration);
            this.aCoop = this.aCoop.gt(decelThreshold) ? this.aCoop : decelThreshold;

            return new SimpleOperationalPlan(this.aCoop, this.vehicle.getParameters().getParameter(ParameterTypes.DT));
        }

        @Override
        public SimpleOperationalPlan next() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
            EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);

            if (this.maneuverPattern.getDirectionOfMergeCandidate() != null) {
                if (ego.getEgoSpeed().gt(this.vehicle.getParameters().getParameter(ParameterTypes.VCONG))
                    && this.vehicle.getParameters().getParameter(MirovaParameters.cooperativeLaneChangesEnabled)) {

                    NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);
                    LateralDirectionality dir = this.maneuverPattern.getDirectionOfMergeCandidate();
                    LateralDirectionality oppositeDir = dir.isLeft() ? LateralDirectionality.RIGHT : LateralDirectionality.LEFT;
                    MacroTrafficContext macro = this.vehicle.getContextManager().getCategory("MacroTraffic", MacroTrafficContext.class);
                    RelativeLane targetLane = dir.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;
                    Speed vTargetLane = macro.getAverageSpeed(targetLane);

                    if (neighbors.checkIfLaneChangeIsPossible(oppositeDir) && ego.getEgoSpeed().si > (vTargetLane.si - 5.0)) {
                        return transitionTo(new PerformLaneChangeState(this.maneuverPattern, oppositeDir));
                    }
                }
            }
            return null;
        }

        @Override
        public SimpleOperationalPlan abort() throws ParameterException, OperationalPlanException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
            NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);
            LateralDirectionality dir = this.maneuverPattern.getDirectionOfMergeCandidate();

            this.mergeCandidate = this.maneuverPattern.getActiveMergeCandidate();

            if (this.mergeCandidate == null) {
                return finishManeuver();
            }

            InfrastructureContext infrastructure = this.vehicle.getContextManager().getCategory("Infrastructure", InfrastructureContext.class);

            if (this.maneuverPattern.lastCandidatePosition == RelativeCandidatePosition.BEHIND
                    && (neighbors.getRearGapDistance(dir).si > 0.0
                            || infrastructure.getDistanceToLaneEnd(dir.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT).si > Length.instantiateSI(300.0).si)) {
                return finishManeuver();
            }

            Length emergencyStopBuffer = this.vehicle.getParameters().getParameter(MirovaParameters.emergencyStoppingDistance);
            if (this.maneuverPattern.lastCandidatePosition == RelativeCandidatePosition.AHEAD) {
                Length frontGap = neighbors.getFrontGapDistance(dir);
                if (frontGap != null && frontGap.si < 0.0) {
                    Length candidateLength = this.mergeCandidate.getLength();
                    Length standstillBuffer = this.vehicle.getParameters().getParameter(ParameterTypes.S0);
                    Length distanceToEnd = infrastructure.getDistanceToLaneEnd(dir.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT);
                    if (distanceToEnd.si < candidateLength.si + standstillBuffer.si + emergencyStopBuffer.si + 4.0) {
                        return finishManeuver();
                    }
                }
            }

            Length standstillBuffer = this.vehicle.getParameters().getParameter(ParameterTypes.S0);
            if (infrastructure.getDistanceToLaneEnd(dir.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT).si < standstillBuffer.si + emergencyStopBuffer.si + 0.5) {
                return finishManeuver();
            }

            return null;
        }

        @Override
        public String toString() {
            return "OpenGapCongestedState";
        }
    }
}
package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns;

import java.util.Iterator;

import org.djunits.value.vdouble.scalar.*;
import org.opentrafficsim.base.parameters.*;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;

import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.AutobahnFreeDrivingPattern.FreeDrivingState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.DiscretionaryLaneChangePatternOld.ActionStateCompleteLaneChange;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.helpers.GapCandidate;

import static org.opentrafficsim.base.parameters.ParameterTypes.*;
import static org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters.*;

/**
 * Mandatory lane change pattern implementing the full Berghaus & Oeser (2025) gap selection
 * and targeted acceleration (aM) planning using dimensional quantities.
 */
public class MandatoryLaneChangePattern extends ManeuverPattern {

    private final LateralDirectionality targetDirection;
    private GapCandidate activeGap;
    /** Buffer distance before the end of the lane where emergency braking is enforced. */
    public static Length RAMP_END_BUFFER;
    private final Duration patternSpecificTimestep = Duration.instantiateSI(0.2);


    public MandatoryLaneChangePattern(final KnowledgeChunk kc) {
        super(PatternType.TACTICAL_LC, kc);
        this.initialActionState = new MatchTargetLaneSpeedState(this);
        this.targetDirection = this.vehicle.getLaneChangeDesire().dominantDirection();
        this.requiredContextKeys.add("Ego");
        this.requiredContextKeys.add("Neighbors");
        this.requiredContextKeys.add("Infrastructure");
        this.requiredContextKeys.add("MacroTraffic");

        try
        {
            this.RAMP_END_BUFFER = this.vehicle.getParameters().getParameter(MirovaParameters.emergencyStoppingDistance).plus(Length.instantiateSI(5.0));
        }
        catch (ParameterException exception)
        {
            exception.printStackTrace();
        }
    }

    public LateralDirectionality getTargetDirection() {return this.targetDirection; }
    public GapCandidate getActiveGap() { return this.activeGap; }
    public void setActiveGap(final GapCandidate gap) { this.activeGap = gap; }

    @Override
    public boolean checkContext() { return true; }

    @Override
    public boolean checkAbility() {
        InfrastructurePerception infraPerception = this.knowledgeChunk.getInfrastructurePerception();
        if (infraPerception.getLegalLaneChangePossibility(RelativeLane.CURRENT, this.targetDirection).si > 0.0) {
            // only apply if lane change is legally and physically possible
            return true;
        }
        else {
            return false; }
        }


    /* =========================================================================================
     * 1) MATCH TARGET-LANE SPEED
     * ========================================================================================= */
    public static class MatchTargetLaneSpeedState extends ActionState {

        private final MandatoryLaneChangePattern pattern;

        public MatchTargetLaneSpeedState(final ManeuverPattern p) {
            super(p);
            this.pattern = (MandatoryLaneChangePattern) p;
            this.active = true;
        }

        @Override
        public SimpleOperationalPlan executeControl()
                throws ParameterException, GtuException, NetworkException {

            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
            Parameters params = this.vehicle.getParameters();

            Acceleration acc = ego.getCurrentCarFollowingAcceleration();

            if (infra.getIfLaneAvailable(this.pattern.targetDirection)) {
                Speed targetLaneSpeed = ego.getEgoSpeed();
                if (this.pattern.getTargetDirection().isLeft())
                    targetLaneSpeed = macro.getAverageSpeedLeft();
                else if (this.pattern.getTargetDirection().isRight())
                    targetLaneSpeed = macro.getAverageSpeedRight();

                Acceleration aToMatch = CarFollowingUtil.approachTargetSpeed(
                        this.vehicle.getCarFollowingModel(),
                        params,
                        ego.getEgoSpeed(),
                        infra.getCurrentSpeedLimit(),
                        infra.getDistanceToLaneEnd().times(0.9),
                        targetLaneSpeed);

                acc = Acceleration.min(acc, aToMatch);
            }

            SimpleOperationalPlan plan = new SimpleOperationalPlan(acc, this.pattern.patternSpecificTimestep);
            LateralDirectionality direction = ((MandatoryLaneChangePattern)this.maneuverPattern).getTargetDirection();
            if (direction == LateralDirectionality.LEFT) {
                plan.setIndicatorIntentLeft();
            } else if (direction == LateralDirectionality.RIGHT) {
                plan.setIndicatorIntentRight();
            }

            return plan;
        }

        @Override
        public SimpleOperationalPlan next()
                throws ParameterException, OperationalPlanException, NetworkException, GtuException {

            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
            Desire mand = this.vehicle.getMandatoryLaneChangeDesire();

            if (neigh.getIfLaneChangePossible(this.pattern.getTargetDirection()))
                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));

            if (mand.magnitude() >= this.vehicle.getParameters().getParameter(DMAND))
                return transitionTo(new SearchForGapState(this.maneuverPattern));

            final InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            final EgoContext ego = this.vehicle.getContext(EgoContext.class);
            final Parameters params = this.vehicle.getParameters();
            Acceleration stopAccel = CarFollowingUtil.stop(
                    this.vehicle.getCarFollowingModel(),
                    params,
                    ego.getEgoSpeed(),
                    infra.getCurrentSpeedLimit(),
                    infra.getDistanceToLaneEnd().minus(this.vehicle.getParameters().getParameter(MirovaParameters.emergencyStoppingDistance)));

            if (stopAccel.si < -6.5) // emergency braking threshold
            {
                transitionTo(new BreakingEndOfRampState(this.maneuverPattern));
            }

            return null;
        }

        @Override
        public SimpleOperationalPlan abort() { return null; }

        @Override
        public String toString() {
            return "MatchTargetLaneSpeedState";
        }
    }

    /* =========================================================================================
     * 2) STATE: SEARCH_FOR_GAP
     * ========================================================================================= */

    /**
     * State where the agent actively scans the target lane for a feasible gap according to
     * the Berghaus & Oeser (2025) model.
     * <p>
     * <b>Behavior:</b>
     * <ul>
     * <li><b>Longitudinal Control:</b> The agent drives defensively. It follows its current leader
     * (Car-Following) but strictly enforces a stop before the virtual ramp end (Safety Shield).</li>
     * <li><b>Gap Search:</b> In every simulation step, it searches for a valid gap.
     * Depending on the speed difference relative to the target lane, it searches either
     * upstream (if slower) or downstream (if faster).</li>
     * </ul>
     * </p>
     */
    public static class SearchForGapState extends ActionState {

        protected final MandatoryLaneChangePattern pattern;

        /**
         * Constructor.
         * @param p The owning maneuver pattern.
         */
        public SearchForGapState(final ManeuverPattern p) {
            super(p);
            this.pattern = (MandatoryLaneChangePattern) p;
            this.active = true;
        }

        /**
         * Calculates the acceleration for this time step.
         * <p>
         * <b>Strategy:</b>
         * <ul>
         * <li><b>Car Following:</b> Follow the leader on the current lane safely.</li>
         * <li><b>Speed Matching:</b> actively accelerate (or decelerate) to match the average speed
         * of the target lane by the time the vehicle reaches the merge area (defined by {@code RAMP_END_BUFFER}).</li>
         * </ul>
         * The stop at the end of the lane is NOT enforced here, as the {@link #next()} method handles
         * the transition to {@link BreakingEndOfRampState} if the remaining distance becomes critical.
         * </p>
         *
         * @return The operational plan containing acceleration and turn indicators.
         */
        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException {
            final EgoContext ego = this.vehicle.getContext(EgoContext.class);
            final InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            final MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
            final Parameters params = this.vehicle.getParameters();

            // 1. Base: Standard Car-Following (don't hit the leader on the ramp)
            Acceleration acc = ego.getCurrentCarFollowingAcceleration();

            // 2. Speed Matching: Approach the average speed of the target lane
            if (infra.getIfLaneAvailable(this.pattern.targetDirection)) {

                // Determine target speed
                Speed vTarget = this.pattern.getTargetDirection().isLeft()
                        ? macro.getAverageSpeedLeft()
                        : macro.getAverageSpeedRight();

                // Determine distance available to adjust speed
                // We aim to have the speed matched when we reach the buffer area
                Length distToMerge = infra.getDistanceToLaneEnd().minus(MandatoryLaneChangePattern.RAMP_END_BUFFER);

                // Sanity check: if we are already inside the buffer, use a tiny distance to force immediate adaptation
                if (distToMerge.si < 1.0) {
                    distToMerge = Length.instantiateSI(1.0);
                }

                // Calculate kinematic acceleration required to reach vTarget over distToMerge
                Acceleration aMatch = CarFollowingUtil.approachTargetSpeed(
                        this.vehicle.getCarFollowingModel(),
                        params,
                        ego.getEgoSpeed(),
                        infra.getCurrentSpeedLimit(),
                        distToMerge,
                        vTarget
                );

                // Combine: We want to match speed (aMatch), but Safety (aCF) is the upper bound.
                // If aMatch wants to accelerate hard (e.g. +3m/s^2) but leader is slow (aCF = +1m/s^2), we must stick to +1.
                // If aMatch wants to brake (target is slow), min() will select the braking.
                acc = Acceleration.min(acc, aMatch);
            }

            // 3. Construct Plan
            SimpleOperationalPlan plan = new SimpleOperationalPlan(acc, this.pattern.patternSpecificTimestep);
            if (this.pattern.getTargetDirection() == LateralDirectionality.LEFT) {
                plan.setIndicatorIntentLeft();
            } else if (this.pattern.getTargetDirection() == LateralDirectionality.RIGHT) {
                plan.setIndicatorIntentRight();
            }

            return plan;
        }

        /**
         * Determines the next state based on perception and gap feasibility.
         *
         * @return The next {@link ActionState} or {@code null} to continue searching.
         */
        @Override
        public SimpleOperationalPlan next() throws ParameterException, OperationalPlanException, NetworkException, GtuException {
            final NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);

            // A. Check for immediate opportunities (e.g., adjacent lane is empty or cooperative neighbor)
            if (neigh.getIfLaneChangePossible(this.pattern.getTargetDirection())) {
                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));
            }

            // B. Active Search: Look for a specific gap using the Berghaus & Oeser logic
            //    This now correctly handles upstream vs. downstream search based on relative speed.
            GapCandidate bestGap = findFeasibleGap();
            if (bestGap != null) {
                // Gap found! Lock it in and transition to "Attack Mode"
                this.pattern.setActiveGap(bestGap);
                return transitionTo(new AccelerateToTargetGapState(this.maneuverPattern));
            }

            // C. Emergency Check: Are we running out of road?
            final InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            final EgoContext ego = this.vehicle.getContext(EgoContext.class);

            // Calculate braking requirement for the buffer point
            Acceleration requiredStopAccel = CarFollowingUtil.stop(
                    this.vehicle.getCarFollowingModel(),
                    this.vehicle.getParameters(),
                    ego.getEgoSpeed(),
                    infra.getCurrentSpeedLimit(),
                    infra.getDistanceToLaneEnd().minus(MandatoryLaneChangePattern.RAMP_END_BUFFER));

            // Panic Threshold: If we need to brake harder than -5.0 m/s^2 to respect the buffer,
            // we stop searching and switch to dedicated emergency braking.
            if (requiredStopAccel.si < -5.0) {
                return transitionTo(new BreakingEndOfRampState(this.maneuverPattern));
            }

            // Continue searching in current state
            return null;
        }

        @Override
        public SimpleOperationalPlan abort() { return null; }

        /**
         * Iterates through the target lane to find a gap that satisfies the Berghaus & Oeser kinematic constraints.
         * <p>
         * The search direction depends on the relative speed:
         * <ul>
         * <li><b>V_ego > V_target:</b> Downstream search (we are overtaking gaps).</li>
         * <li><b>V_ego <= V_target:</b> Upstream search (we let gaps approach us).</li>
         * </ul>
         * </p>
         *
         * @return A valid {@link GapCandidate}, or {@code null} if no feasible gap exists.
         */
        private GapCandidate findFeasibleGap() throws ParameterException, GtuException, NetworkException {
            NeighborsContext neighCtx = this.vehicle.getContext(NeighborsContext.class);
            MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
            EgoContext ego = this.vehicle.getContext(EgoContext.class);

            LateralDirectionality direction = this.pattern.getTargetDirection();
            RelativeLane targetLane = direction.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;

            Speed vEgo = ego.getEgoSpeed();
            Speed vTarget = macro.getAverageSpeed(targetLane); // or macro.getAverageSpeedLeft()/Right()

            // --- CASE 1: Downstream Search (Ego is faster) ---
            if (vEgo.si > vTarget.si) {
                Iterator<HeadwayGtu> leaderIt = neighCtx.getLeaders(direction).iterator();

                // Start with the immediate follower (behind us) as the "back boundary" of the first gap
                HeadwayGtu potentialFollower = this.vehicle.getContext(NeighborsContext.class).getFollower(direction);

                if (potentialFollower == null) return null; // No rear boundary defined

                while (leaderIt.hasNext()) {
                    HeadwayGtu potentialLeader = leaderIt.next(); // Looking further ahead

                    GapCandidate candidate = new GapCandidate(
                            potentialLeader,
                            potentialFollower,
                            direction,
                            this.vehicle
                    );

                    if (candidate.computeCurrentAcceleration() != null) {
                        return candidate;
                    }

                    // Shift window downstream: Current leader becomes follower for the next gap
                    potentialFollower = potentialLeader;
                }
            }
            // --- CASE 2: Upstream Search (Ego is slower or equal) ---
            else {
                Iterator<HeadwayGtu> followerIt = neighCtx.getFollowers(direction).iterator();

                // Start with the immediate leader (ahead of us) as the "front boundary" of the first gap
                HeadwayGtu potentialLeader = neighCtx.getLeader(direction);

                if (potentialLeader == null) return null; // No front boundary defined

                while (followerIt.hasNext()) {
                    HeadwayGtu potentialFollower = followerIt.next(); // Looking further back

                    GapCandidate candidate = new GapCandidate(
                            potentialLeader,
                            potentialFollower,
                            direction,
                            this.vehicle
                    );

                    if (candidate.computeCurrentAcceleration() != null) {
                        return candidate;
                    }

                    // Shift window upstream: Current follower becomes leader for the next gap
                    potentialLeader = potentialFollower;
                }
            }

            return null;
        }

        @Override
        public String toString() { return "SearchForGapState"; }
    }


    /* =========================================================================================
     * 3) STATE: ACCELERATE_TO_TARGET_GAP
     * ========================================================================================= */

    /**
     * State where the agent actively executes the longitudinal adjustments required to merge into
     * the selected gap.
     * <p>
     * <b>Behavior:</b>
     * <ul>
     * <li><b>Commitment:</b> The agent "commits" to the selected {@link GapCandidate}. It assumes that
     * the calculated acceleration {@code aM} will safely guide it to the merge point before the lane ends.
     * Therefore, the explicit "Safety Shield" (braking at ramp end) is <b>disabled</b> to prevent interference
     * with the planned trajectory.</li>
     * <li><b>Control:</b> In every step, it recalculates the required acceleration {@code aM} using
     * {@link GapCandidate#computeCurrentAcceleration()}. This ensures reaction to dynamic changes of the neighbors.</li>
     * <li><b>Optimization:</b> The calculation result is cached between the {@code abort()} check and the
     * {@code executeControl()} call within the same simulation tick to avoid redundant computations.</li>
     * <li><b>Safety:</b> The acceleration is capped by the standard car-following model ({@code aCF}) to ensure
     * no collision occurs with the leading vehicle on the current ramp.</li>
     * </ul>
     * </p>
     */
    public static class AccelerateToTargetGapState extends SearchForGapState {

        /**
         * Caches the acceleration calculated during the {@link #abort()} phase to be used
         * in the subsequent {@link #executeControl()} phase of the same tick.
         */
        private Acceleration cachedAcceleration;

        /**
         * Constructor.
         * @param p The owning maneuver pattern.
         */
        public AccelerateToTargetGapState(final ManeuverPattern p) {
            super(p);
            this.active = true;
        }

        /**
         * Checks for failure conditions (Gap Loss) and pre-calculates the acceleration.
         * <p>
         * This method performs the heavy lifting of the Berghaus & Oeser math. If the gap is feasible,
         * the result is stored in {@link #cachedAcceleration} for efficiency.
         * </p>
         *
         * @return {@link SearchForGapState} if the gap is lost, otherwise {@code null}.
         */
        @Override
        public SimpleOperationalPlan abort() {
            GapCandidate gap = this.pattern.getActiveGap();
            try {
                // Pre-calculate and cache the result for this tick
                this.cachedAcceleration = gap.computeCurrentAcceleration();

                // If result is null, the gap has collapsed or is not feasible anymore
                if (this.cachedAcceleration == null) {
                    // Log or Debug could go here
                    return transitionTo(new SearchForGapState(this.maneuverPattern));
                }
            } catch (Exception e) {
                e.printStackTrace();
                // In case of perception errors, safe fallback to search
                try
                {
                    return transitionTo(new SearchForGapState(this.maneuverPattern));
                }
                catch (ParameterException | NullPointerException | IllegalArgumentException | GtuException
                        | NetworkException exception)
                {
                    exception.printStackTrace();
                }
            }
            // Gap is valid, stay in this state
            return null;
        }

        /**
         * Calculates the acceleration required to reach the gap.
         * <p>
         * Uses the cached acceleration from {@link #abort()} if available.
         * </p>
         *
         * @return The operational plan using the target acceleration.
         */
        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException {
            final EgoContext ego = this.vehicle.getContext(EgoContext.class);
            final Parameters params = this.vehicle.getParameters();

            // 1. Retrieve the calculated acceleration (Try Cache first)
            Acceleration aM = this.cachedAcceleration;

            // Clear cache to ensure fresh calculation in the next tick
            this.cachedAcceleration = null;

            // Fallback (Defensive Coding): If abort() wasn't called or cache is empty for some reason,
            // we must recalculate to be safe.
            if (aM == null) {
                GapCandidate gap = this.pattern.getActiveGap();
                if (gap != null) {
                    aM = gap.computeCurrentAcceleration();
                }
            }

            // Fallback Level 2: If calculation fails (Gap became invalid in micro-step),
            // default to standard car-following.
            if (aM == null) {
                aM = ego.getCurrentCarFollowingAcceleration();
            }

            // 2. Car-Following Integrity check
            // Even if the gap requires strong acceleration, we must not crash into our own leader on the ramp.
            Acceleration aCF = ego.getCurrentCarFollowingAcceleration();
            Acceleration finalAcc = Acceleration.min(aM, aCF);

            // 3. Construct Plan
            // We consciously DO NOT apply the getStopAcceleration() constraint here ("Safety Shield"),
            // because we trust the model's aM to land us in the gap before the road ends.
            SimpleOperationalPlan plan = new SimpleOperationalPlan(finalAcc, this.pattern.patternSpecificTimestep);
            if (this.pattern.getTargetDirection() == LateralDirectionality.LEFT) {
                plan.setIndicatorIntentLeft();
            } else if (this.pattern.getTargetDirection() == LateralDirectionality.RIGHT) {
                plan.setIndicatorIntentRight();
            }

            return plan;
        }

        /**
         * Checks for successful completion of the approach phase.
         *
         * @return {@link ExecuteLaneChangeState} if the lane change can be started, otherwise {@code null}.
         */
        @Override
        public SimpleOperationalPlan next() throws ParameterException, OperationalPlanException, NetworkException, GtuException {
            final NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);

            // Check if we have reached the position/speed conditions to actually steer into the target lane.
            if (neigh.getIfLaneChangePossible(this.pattern.getTargetDirection())) {
                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));
            }

            return null;
        }

        @Override
        public String toString() { return "AccelerateToTargetGapState"; }
    }




    /* =========================================================================================
     * 4) STATE: BREAKING_END_OF_RAMP
     * ========================================================================================= */

    public static class BreakingEndOfRampState extends ActionState {


        /** Owning pattern. */
        private final MandatoryLaneChangePattern pattern;

        public BreakingEndOfRampState(final ManeuverPattern p) {
            super(p);
            this.pattern = (MandatoryLaneChangePattern) p;
            this.active = true;
        }

        @Override
        public SimpleOperationalPlan executeControl()
                throws ParameterException {

            Parameters params = this.vehicle.getParameters();
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);

            Acceleration a = CarFollowingUtil.stop(
                    this.vehicle.getCarFollowingModel(),
                    params,
                    ego.getEgoSpeed(),
                    infra.getCurrentSpeedLimit(),
                    infra.getDistanceToLaneEnd().minus(this.vehicle.getParameters().getParameter(MirovaParameters.emergencyStoppingDistance)));

            SimpleOperationalPlan plan = new SimpleOperationalPlan(a, this.pattern.patternSpecificTimestep);
            LateralDirectionality direction = ((MandatoryLaneChangePattern)this.maneuverPattern).getTargetDirection();
            if (direction == LateralDirectionality.LEFT) {
                plan.setIndicatorIntentLeft();
            } else if (direction == LateralDirectionality.RIGHT) {
                plan.setIndicatorIntentRight();
            }

            return plan;
        }

        @Override
        public SimpleOperationalPlan next()
                throws ParameterException, OperationalPlanException, NetworkException, GtuException {

            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
            EgoContext ego = this.vehicle.getContext(EgoContext.class);

            if(neigh.getIfLaneChangePossible(
                    ((MandatoryLaneChangePattern)this.maneuverPattern).getTargetDirection()))
                return transitionTo(new ExecuteLaneChangeState(
                        this.maneuverPattern,
                        ((MandatoryLaneChangePattern)this.maneuverPattern).getTargetDirection()));

            return null;
        }

        @Override
        public SimpleOperationalPlan abort() { return null; }

        @Override
        public String toString() {
            return "BreakingEndOfRampState";
        }
    }

    /* =========================================================================================
     * 5) STATE: EXECUTE_LANE_CHANGE
     * ========================================================================================= */

    public static class ExecuteLaneChangeState extends ActionState {

        /** Target direction of the lane change (LEFT or RIGHT). */
        private final LateralDirectionality direction;


        /** Cached origin lane to detect completion. */
        private final Lane originLane;

        private MandatoryLaneChangePattern pattern;


        // ----------------------------------------------------------------------
        // Construction
        // ----------------------------------------------------------------------

        /** ActionStatePerformLaneChange constructor.
         * @param pattern
         * @param direction
         */
        public ExecuteLaneChangeState(final ManeuverPattern p, final LateralDirectionality direction) {
            super(p);
            this.direction = direction;
            this.pattern = (MandatoryLaneChangePattern) p;

            this.originLane = this.vehicle.getGtu().getLane();
        }

        // ----------------------------------------------------------------------
        // Core control logic
        // ----------------------------------------------------------------------

        /**
         * Executes longitudinal control using a simplified Two-Leader Car-Following logic.
         * <p>
         * The ego vehicle simultaneously considers the leader on its current lane and
         * the leader on the target lane. The resulting acceleration is the most restrictive
         * (minimum) across these influences.
         * </p>
         * @throws NetworkException
         * @throws GtuException
         */
        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException {
            InfrastructureContext infraCtx = this.vehicle.getContext(InfrastructureContext.class);
            NeighborsContext neighborsCtx = this.vehicle.getContext(NeighborsContext.class);
            EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);

            Speed egoSpeed = egoCtx.getEgoSpeed();
            Parameters params = this.vehicle.getGtu().getParameters();

            this.vehicle.setTargetDesiredHeadway(this.vehicle.getParameters().getParameter(ParameterTypes.T)
                    .times(this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)));

            // Start with relaxed car-following acceleration (already includes Desire effects)
            Acceleration minAcc = egoCtx.getCurrentCarFollowingAcceleration();


            // Add target-lane leader constraint
            if (this.vehicle.getGtu().getLane() == this.originLane) {
                // Only consider target-lane leader if still on origin lane;
                HeadwayGtu targetLeader = neighborsCtx.getLeader(this.direction);
                if (targetLeader != null) {
                    Acceleration aTarget = CarFollowingUtil.followSingleLeader(
                            this.vehicle.getCarFollowingModel(),
                            params,
                            egoSpeed,
                            infraCtx.getCurrentSpeedLimit(),
                            targetLeader.getDistance(),
                            targetLeader.getSpeed());
                    minAcc = Acceleration.min(minAcc, aTarget);
                }
            }

            SimpleOperationalPlan plan = new SimpleOperationalPlan(
                    minAcc,
                    this.pattern.patternSpecificTimestep,
                    this.direction);

            if (this.direction == LateralDirectionality.LEFT) {
                plan.setIndicatorIntentLeft();
            } else if (this.direction == LateralDirectionality.RIGHT) {
                plan.setIndicatorIntentRight();
            }

            return plan;
            }

        // ----------------------------------------------------------------------
        // Transitions
        // ----------------------------------------------------------------------

        /**
         * Proceeds to {@link ActionStateCompleteLaneChange} when the lane change is completed.
         * @return
         * @throws NetworkException
         * @throws GtuException
         * @throws IllegalArgumentException
         * @throws NullPointerException
         */
        @Override
        public SimpleOperationalPlan next() throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
            boolean finished = !this.vehicle.getLaneChange().isChangingLane()
                    && !this.originLane.equals(this.vehicle.getGtu().getLane());

            if (finished) {
                ActionState nextState = new FreeDrivingState(this.maneuverPattern, this.direction);
                return transitionTo(nextState);
            }
            return null;
        }

        /**
         * Checks whether the lane-change should be aborted (safety or desire violation).
         * @return
         */
        @Override
        public SimpleOperationalPlan abort() throws ParameterException, OperationalPlanException {
              return null;
        }

        @Override
        public String toString() {
            return "ExecuteLaneChange[" + this.direction + "]";
        }
}
}





///* =========================================================================================
//* 2) STATE: SEARCH_FOR_GAP — CLEAN BERGHAUS & OESER (2025) FORMULATION (NO COOPERATION)
//* ========================================================================================= */
//public static class SearchForGapState extends ActionState
//{
//  /** Owning pattern. */
//  private final MandatoryLaneChangePattern pattern;
//
//  public SearchForGapState(final ManeuverPattern p)
//  {
//      super(p);
//      this.pattern = (MandatoryLaneChangePattern) p;
//      this.active = true;
//  }
//
//  @Override
//  public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
//  {
//      final EgoContext ego = this.vehicle.getContext(EgoContext.class);
//      final InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//      final MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
//      final Parameters params = this.vehicle.getParameters();
//
//      // Longitudinal baseline: regular car-following
//      Acceleration acc = ego.getCurrentCarFollowingAcceleration();
//
//      if (infra.getIfLaneAvailable(this.pattern.targetDirection)) {
//
//          // Optional: gently align with target-lane average speed to ease merging
//          Speed targetLaneSpeed = ego.getEgoSpeed();
//          if (this.pattern.getTargetDirection().isLeft())
//          {
//              targetLaneSpeed = macro.getAverageSpeedLeft();
//          }
//          else if (this.pattern.getTargetDirection().isRight())
//          {
//              targetLaneSpeed = macro.getAverageSpeedRight();
//          }
//
//          final Acceleration aToMatch = CarFollowingUtil.approachTargetSpeed(
//                  this.vehicle.getCarFollowingModel(),
//                  params,
//                  ego.getEgoSpeed(),
//                  infra.getCurrentSpeedLimit(),
//                  infra.getDistanceToLaneEnd().times(0.9),
//                  targetLaneSpeed);
//
//          acc = Acceleration.min(acc, aToMatch);
//          }
//
//      SimpleOperationalPlan plan = new SimpleOperationalPlan(acc, this.pattern.patternSpecificTimestep);
//      LateralDirectionality direction = ((MandatoryLaneChangePattern)this.maneuverPattern).getTargetDirection();
//      if (direction == LateralDirectionality.LEFT) {
//          plan.setIndicatorIntentLeft();
//      } else if (direction == LateralDirectionality.RIGHT) {
//          plan.setIndicatorIntentRight();
//      }
//
//      return plan;
//  }
//
//  @Override
//  public SimpleOperationalPlan next() throws ParameterException, OperationalPlanException, NetworkException, GtuException
//  {
//      final NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
//      EgoContext ego = this.vehicle.getContext(EgoContext.class);
//      final Parameters params = this.vehicle.getParameters();
//
//      // (a) immediate feasible lane change?
//      if (neigh.getIfLaneChangePossible(this.pattern.getTargetDirection()))
//      {
//          return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));
//      }
//
//      // (b) Berghaus & Oeser gap search (no cooperation here)
//      final GapCandidate gap = computeGapCandidate();
//      this.pattern.setActiveGap(gap);
//
//      // (c) if we found a valid candidate and urgency is high enough -> accelerate-to-gap phase
//      if (gap != null)
//      {
//          return transitionTo(new AccelerateToTargetGapState(this.maneuverPattern));
//      }
//
//      // (d) emergency break if we are close to the end of the lane and no gap found
//      final InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//      Acceleration stopAccel = CarFollowingUtil.stop(
//              this.vehicle.getCarFollowingModel(),
//              params,
//              ego.getEgoSpeed(),
//              infra.getCurrentSpeedLimit(),
//              infra.getDistanceToLaneEnd().minus(this.vehicle.getParameters().getParameter(MirovaParameters.emergencyStoppingDistance)));
//
//      if (stopAccel.si < -6.5) // emergency braking threshold
//      {
//          transitionTo(new BreakingEndOfRampState(this.maneuverPattern));
//      }
//
//      return null;
//  }
//
//  @Override
//  public SimpleOperationalPlan abort()
//  {
//      return null;
//  }
//
//  /**
//   * Performs upstream gap search on the target lane, using the Berghaus & Oeser (2025) formulation.
//   *
//   * Coordinate convention in OTS (ego-centric):
//   * - xM := 0 (ego reference position; "Merger")
//   * - xE := distance from ego to end of ramp / end of current lane (merge point)
//   * - xL := leader distance in target lane (positive; ahead of ego)
//   * - xF := follower distance in target lane (negative; behind ego)
//   *
//   * Note: the paper uses vehicle centers, thus ±L/2 terms appear; we map this explicitly.
//   *
//   * @return first feasible gap candidate upstream, or null if none found
//   */
//  private GapCandidate computeGapCandidate() throws ParameterException, GtuException, NetworkException
//  {
//      NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
//      MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
//      EgoContext ego = this.vehicle.getContext(EgoContext.class);
//      // Perception iteration (upstream on target lane)
//      final NeighborsPerception neighPerception =
//              this.vehicle.getPerception().getPerceptionCategory(NeighborsPerception.class);
//      final RelativeLane targetLane =
//              this.pattern.getTargetDirection().isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;
//
//
//      if (macro.getAverageSpeed(targetLane).ge(ego.getEgoSpeed()))
//      {
//          final Iterator<HeadwayGtu> followerIt = neighPerception.getFollowers(targetLane).iterator();
//
//          // Start gap with current immediate leader on the target lane (ahead)
//          HeadwayGtu leader = neighbors.getLeader(this.pattern.getTargetDirection());
//          if (leader == null)
//          {
//              // No leader => no well-defined bounded gap to merge into; keep searching later.
//              return null;
//          }
//
//          while (followerIt.hasNext())
//          {
//              final HeadwayGtu follower = followerIt.next();
//              if (follower == null)
//              {
//                  continue;
//              }
//              GapCandidate gapCandidate = searchGapCandidate(follower, leader);
//              if (gapCandidate != null)
//              {
//                  return gapCandidate;
//              }
//
//
//              // Shift upstream: next candidate gap is between previous follower and its upstream follower
//              leader = follower;
//          }
//      }
//      else
//      {
//          final Iterator<HeadwayGtu> leaderIt = neighPerception.getLeaders(targetLane).iterator();
//
//          // Start gap with current immediate follower on the target lane (behind)
//          HeadwayGtu follower = neighbors.getFollower(this.pattern.getTargetDirection());
//          if (follower == null)
//          {
//              // No follower => no well-defined bounded gap to merge into; keep searching later.
//              return null;
//          }
//
//          while (leaderIt.hasNext())
//          {
//              final HeadwayGtu leader = leaderIt.next();
//              if (leader == null)
//              {
//                  continue;
//              }
//              GapCandidate gapCandidate = searchGapCandidate(follower, leader);
//              if (gapCandidate != null)
//              {
//                  return gapCandidate;
//              }
//              // Shift downstream: next candidate gap is between previous leader and its downstream leader
//              follower = leader;
//              }
//          }
//
//      return null;
//  }
//
//  /**
//   * Searches a single gap candidate between given follower and leader GTUs.
//   *
//   * @param follower target-lane follower GTU
//   * @param leader target-lane leader GTU
//   * @return gap candidate if feasible, null otherwise
//   * @throws ParameterException on parameter error
//   * @throws GtuException on GTU error
//   * @throws NetworkException on network error
//   */
//  private GapCandidate searchGapCandidate(final HeadwayGtu follower, HeadwayGtu leader) throws ParameterException, GtuException, NetworkException {
//      final EgoContext ego = this.vehicle.getContext(EgoContext.class);
//      final InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//      final MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
//
//      final Parameters params = this.vehicle.getParameters();
//
//      // Paper parameters (mapped)
//      final Duration tauLC = params.getParameter(ParameterTypes.LCDUR);
//      final Length dxMin = params.getParameter(ParameterTypes.S0);
//      final Duration Tdes = params.getParameter(ParameterTypes.T);
//      final Length emergencyStoppingDistance = params.getParameter(MirovaParameters.emergencyStoppingDistance);
//
//      // Coordinate anchor: merge point at end of lane
//      final Length xE = infra.getDistanceToLaneEnd().minus(emergencyStoppingDistance);     // merge point (end of ramp / lane)
//      final Length xM = Length.instantiateSI(0.0);        // ego reference
//      final Speed vM = ego.getEgoSpeed();                 // ego speed
//      final Length lM = this.vehicle.getGtu().getLength(); // ego length
//
//      // Target-lane speed for feasibility branch (26)/(27)
//      final Speed vTargetLane =
//              this.pattern.getTargetDirection().isLeft()
//                      ? macro.getAverageSpeedLeft()
//                      : macro.getAverageSpeedRight();
//
//      // Placeholder for gap search logic
//
//      // --- Map OTS headways to paper positions (center-based) ---
//      final Length xL = leader.getDistance();             // ahead of ego (+)
//      final Length xF = follower.getDistance().neg();    // behind ego (-)
//      final Speed vL = leader.getSpeed();
//      final Speed vF = follower.getSpeed();
//      final Length lL = leader.getLength();
//      final Length lF = follower.getLength();
//
//      // --- Time horizons from paper ---
//      final Duration tauELeader = computeTauELeader(xL, xE, xM, vL, vM, dxMin, Tdes);
//      if (tauELeader.si <= 0.0)
//      {
//          leader = follower;
//          return null;
//      }
//
//      final Duration tauEFollower = computeTauEFollower(xE, xM, xF, vF, dxMin, Tdes);
//      if (tauEFollower.si <= 0.0)
//      {
//          leader = follower;
//          return null;
//      }
//
//      // --- Planned merge acceleration aM = min(aLeader, aFollower, aCF) ---
//      final Acceleration aM = computeMergeAcceleration(
//              xE, xM, vM,
//              leader, follower,
//              tauELeader, tauEFollower,
//              dxMin, Tdes, tauLC);
//
//      // --- Feasibility check (26)/(27) ---
//      if (isGapFeasible(
//              xE, xM, vM, lM,
//              xL, vL, lL,
//              xF, vF, lF,
//              aM, tauELeader, tauEFollower, tauLC, dxMin,
//              vTargetLane))
//      {
//          // Merge point in our OTS representation is simply xE (distance to lane end).
//          // Store it as such (planning uses xE as merge target).
//          return new GapCandidate(leader, follower, xE, aM);
//      }
//      else
//      {
//          // Gap not feasible; continue searching upstream
//          return null;
//      }
//  }
//
//  /* =========================================================================================
//   *  Eq. (12): tau_E for leader constraint
//   * ========================================================================================= */
//
//  /**
//   * Eq. (12) from Berghaus & Oeser (2025), adapted to ego-centric coordinates.
//   * Uses the smallest positive real solution.
//   * Eliminate vehicle lengths because we measure distances from vehicle front end to back end and not centers.
//   */
//  protected Duration computeTauELeader(
//          final Length xL,
//          final Length xE,
//          final Length xM,
//          final Speed vL,
//          final Speed vM,
//          final Length dxMin,
//          final Duration Tdes)
//  {
//      final double vLsi = Math.max(vL.si, 0.1); // avoid division by ~0
//
//      // alpha = xL - xE - dxMin + Tdes*vM -> eliminate vehicle lengths because we measure
//      // distances from vehicle front end to back end and not centers
//      final double alpha =
//              xL.si - xE.si - dxMin.si + (Tdes.si * vM.si);
//
//      // sqrt(alpha^2 + 8*vL*Tdes*(xE-xM))
//      final double inside =
//              alpha * alpha + 8.0 * vLsi * Tdes.si * (xE.si - xM.si);
//
//      final double sqrtTerm = Math.sqrt(Math.max(0.0, inside));
//
//      // numerator = -xL + xE + dxMin - Tdes*vM ± sqrtTerm
//      final double base =
//              -xL.si + xE.si  + dxMin.si - (Tdes.si * vM.si);
//
//      final double numPlus = base + sqrtTerm;
//      final double numMinus = base - sqrtTerm;
//
//      // choose smallest positive real root
//      final double numerator =
//              (numMinus > 0.0) ? Math.min(numPlus, numMinus) : numPlus;
//
//      final double tauE = numerator / (2.0 * vLsi);
//
//      return Duration.instantiateSI(tauE);
//  }
//
//  /* =========================================================================================
//   *  Eq. (24): tau_E,Fol for follower constraint
//   * ========================================================================================= */
//
//  /**
//   * Eq. (24) from Berghaus & Oeser (2025), adapted to ego-centric coordinates.
//   *
//   * tau_E,Fol = (xE - xF - LM/2 - LF/2 - dxMin)/vF - Tdes
//   * eliminate vehicle lengths because we measure distances from vehicle front end to back end and not centers
//   *
//   * Note: xF is negative (behind ego).
//   */
//  protected Duration computeTauEFollower(
//          final Length xE,
//          final Length xM,
//          final Length xF,
//          final Speed vF,
//          final Length dxMin,
//          final Duration Tdes)
//  {
//      final double vFsi = Math.max(vF.si, 0.1);
//
//      final double tau =
//              (xE.si - xF.si  - dxMin.si) / vFsi
//              - Tdes.si;
//
//      return Duration.instantiateSI(tau);
//  }
//
//  /* =========================================================================================
//   *  a_M computation: min(leader-constraint, follower-constraint, baseline car-following)
//   * ========================================================================================= */
//
//  protected Acceleration computeMergeAcceleration(
//          final Length xE,
//          final Length xM,
//          final Speed vM,
//          final HeadwayGtu leader,
//          final HeadwayGtu follower,
//          final Duration tauELeader,
//          final Duration tauEFollower,
//          final Length dxMin,
//          final Duration Tdes,
//          final Duration tauLC) throws ParameterException, GtuException, NetworkException
//  {
//      final Parameters params = this.vehicle.getParameters();
//      final EgoContext ego = this.vehicle.getContext(EgoContext.class);
//
//      // --- Leader constraint: a_M,Leader = min(aDesiredHeadway, aZeroHeadway) ---
//      final Acceleration aLeader = computeAMLeader(
//              xE, xM, vM,
//              leader.getSpeed(),
//              tauELeader,
//              dxMin, Tdes, tauLC);
//
//      // --- Follower constraint: a_M,Fol (Eq. 25) ---
//      final Acceleration aFollower = computeAMFollower(xE, xM, vM, tauEFollower);
//
//      // --- Baseline CF (do not exceed own car-following plan) ---
//      final Acceleration aCF = ego.getCurrentCarFollowingAcceleration();
//
//      final double aMin = Math.min(aLeader.si, Math.min(aFollower.si, aCF.si));
//      return Acceleration.instantiateSI(aMin);
//  }
//
//  /**
//   * Leader-based merge acceleration:
//   * - Eq. (10): desired-headway acceleration
//   * - Eq. (14): zero-headway acceleration
//   * - take min(.) of both.
//   */
//  private Acceleration computeAMLeader(
//          final Length xE,
//          final Length xM,
//          final Speed vM,
//          final Speed vL,
//          final Duration tauELeader,
//          final Length dxMin,
//          final Duration Tdes,
//          final Duration tauLC)
//  {
//      // Eq. (10): aDesired = 2 * ( (xE-xM) - vM*tauE ) / tauE^2
//      final double tauE = Math.max(tauELeader.si, 0.1);
//      final double aDesired =
//              2.0 * ((xE.si - xM.si) - vM.si * tauE) / (tauE * tauE);
//
//      // Eq. (14): aZeroHeadway, with tauZero = tauE - tauLC
//      final double tauZero = Math.max(tauELeader.si - tauLC.si, 0.1);
//      final double aZero =
//              ((vL.si - vM.si) * tauZero + dxMin.si) / (0.5 * tauZero * tauZero);
//
//      return Acceleration.instantiateSI(Math.min(aDesired, aZero));
//  }
//
//  /**
//   * Follower-based merge acceleration (Eq. 25):
//   * a_M,Fol = 2 * ( (xE-xM) - vM*tau_E,Fol ) / tau_E,Fol^2
//   */
//  private Acceleration computeAMFollower(
//          final Length xE,
//          final Length xM,
//          final Speed vM,
//          final Duration tauEFollower)
//  {
//      final double tau = Math.max(tauEFollower.si, 0.1);
//      final double a =
//              2.0 * ((xE.si - xM.si) - vM.si * tau) / (tau * tau);
//      return Acceleration.instantiateSI(a);
//  }
//
//  /* =========================================================================================
//   *  Feasibility check: Eq. (26)-(27)
//   * ========================================================================================= */
//
//  protected boolean isGapFeasible(
//          final Length xE,
//          final Length xM,
//          final Speed vM,
//          final Length lM,
//          final Length xL,
//          final Speed vL,
//          final Length lL,
//          final Length xF,
//          final Speed vF,
//          final Length lF,
//          final Acceleration aM,
//          final Duration tauELeader,
//          final Duration tauEFollower,
//          final Duration tauLC,
//          final Length dxMin,
//          final Speed vTargetLane)
//  {
//      // tauPass,i per Eq. (28):
//      // if x_i(t) > x_M(t) (i is ahead): tauPass = tauE_{i+1} - tauLC (leader-side)
//      // else: tauPass = tauE_i - tauLC (follower-side)
//      final Duration tauPass =
//              (vTargetLane.si < vM.si)
//                      ? Duration.instantiateSI(Math.max(0.0, tauELeader.si - tauLC.si))
//                      : Duration.instantiateSI(Math.max(0.0, tauEFollower.si - tauLC.si));
//
//      // Predict merger position at tauPass with constant acceleration aM
//      final Length xM_end = Length.instantiateSI(
//              xM.si + vM.si * tauPass.si + 0.5 * aM.si * tauPass.si * tauPass.si
//      );
//
//      // Predict follower position at tauPass with constant speed vF
//      final Length xF_end = Length.instantiateSI(
//              xF.si + vF.si * tauPass.si
//      );
//
//      // Eq. (26): target lane slower than merger -> ensure merger not behind follower (rear constraint)
//      if (vTargetLane.si < vM.si)
//      {
//          // xM_end - xF_end - LM/2 - dxMin >= 0
//          final double lhs = xM_end.si - xF_end.si - (lM.si) - dxMin.si;
//          return lhs >= 0.0;
//      }
//
//      // Eq. (27): target lane faster than merger -> ensure follower not behind merger (alternative ordering)
//      // xF_end - xM_end - LM/2 - dxMin >= 0
//      final double lhs = xF_end.si - xM_end.si - (lM.si ) - dxMin.si;
//      return lhs >= 0.0;
//  }
//
//  @Override
//  public String toString() {
//      return "SearchForGapState";
//  }
//}

//
///* =========================================================================================
//* 3) STATE: ACCELERATE_TO_TARGET_GAP — BERGHAUS & OESER CONSISTENT
//* ========================================================================================= */
//public static class AccelerateToTargetGapState extends SearchForGapState {
//
//  private final MandatoryLaneChangePattern pattern;
//
//  public AccelerateToTargetGapState(final ManeuverPattern p) {
//      super(p);
//      this.pattern = (MandatoryLaneChangePattern) p;
//      this.active = true;
//  }
//
//  @Override
//  public SimpleOperationalPlan executeControl()
//          throws ParameterException, GtuException, NetworkException {
//
//      final EgoContext ego = this.vehicle.getContext(EgoContext.class);
//      Parameters params = this.vehicle.getParameters();
//
//      final GapCandidate gap = this.pattern.getActiveGap();
//      // aM was already updated in abort method
//      Acceleration acc = gap.getAM();
//
//      // Defensive clipping against car-following baseline
//      Acceleration aCF = ego.getCurrentCarFollowingAcceleration();
//      acc = Acceleration.min(acc, aCF);
//
//      SimpleOperationalPlan plan = new SimpleOperationalPlan(acc, this.pattern.patternSpecificTimestep);
//      LateralDirectionality direction = ((MandatoryLaneChangePattern)this.maneuverPattern).getTargetDirection();
//      if (direction == LateralDirectionality.LEFT) {
//          plan.setIndicatorIntentLeft();
//      } else if (direction == LateralDirectionality.RIGHT) {
//          plan.setIndicatorIntentRight();
//      }
//
//      return plan;
//  }
//
//  @Override
//  public SimpleOperationalPlan next()
//          throws ParameterException, OperationalPlanException, NetworkException, GtuException {
//
//      final NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
//
//      if (neigh.getIfLaneChangePossible(this.pattern.getTargetDirection()))
//          return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));
//
//      return null;
//  }
//
//  @Override
//  public SimpleOperationalPlan abort() {
//      final GapCandidate gap = this.pattern.getActiveGap();
//      final NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
//      if (gap.checkGapStillValid(neigh,  this.pattern.getTargetDirection()) == false)
//      {
//          try
//          {
//              return transitionTo(new SearchForGapState(this.maneuverPattern));
//          }
//          catch (ParameterException | NullPointerException | IllegalArgumentException | GtuException
//                  | NetworkException exception)
//          {
//              exception.printStackTrace();
//          }
//      }
//
//      final EgoContext ego = this.vehicle.getContext(EgoContext.class);
//      final InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//      final Parameters params = this.vehicle.getParameters();
//      Length emergencyStoppingDistance = null;
//      try
//      {
//          emergencyStoppingDistance = params.getParameter(MirovaParameters.emergencyStoppingDistance);
//      }
//      catch (ParameterException exception)
//      {
//          exception.printStackTrace();
//      }
//
//      final Speed vM = ego.getEgoSpeed();
//      final Length xE = infra.getDistanceToLaneEnd().minus(emergencyStoppingDistance);       // distance to lane end
//      System.out.println("Id GTU: " + this.vehicle.getGtu().getId() + " xE: " + xE);
//      final Length xM = Length.instantiateSI(0.0); // ego reference
//      final HeadwayGtu leader = gap.getLeader();
//      final HeadwayGtu follower = gap.getFollower();
//      // Paper parameters (mapped)
//      Duration tauLC = null;
//      try
//      {
//          tauLC = params.getParameter(ParameterTypes.LCDUR);
//      }
//      catch (ParameterException exception)
//      {
//          exception.printStackTrace();
//      }
//      Length dxMin = null;
//      try
//      {
//          dxMin = params.getParameter(ParameterTypes.S0);
//      }
//      catch (ParameterException exception)
//      {
//          exception.printStackTrace();
//      }
//      Duration Tdes = null;
//      try
//      {
//          Tdes = params.getParameter(ParameterTypes.T);
//      }
//      catch (ParameterException exception)
//      {
//          exception.printStackTrace();
//      }
//
//      // Target-lane speed for feasibility branch (26)/(27)
//      final MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
//      Speed vTargetLane = null;
//      try
//      {
//          vTargetLane = this.pattern.getTargetDirection().isLeft()
//                  ? macro.getAverageSpeedLeft()
//                  : macro.getAverageSpeedRight();
//      }
//      catch (OperationalPlanException | ParameterException exception)
//      {
//          exception.printStackTrace();
//      }
//
//      final Length lM = this.vehicle.getGtu().getLength(); // ego length
//      final Length xL = leader.getDistance();             // ahead of ego (+)
//      final Length xF = follower.getDistance().neg();    // behind ego (-)
//      final Speed vL = leader.getSpeed();
//      final Speed vF = follower.getSpeed();
//      final Length lL = leader.getLength();
//      final Length lF = follower.getLength();
//
//
//      // Re-compute time horizons for consistency
//      final Duration tauELeader =computeTauELeader(
//              leader.getDistance(),
//              xE, xM,
//              leader.getSpeed(),
//              vM,
//              dxMin, Tdes);
//      final Duration tauEFollower = computeTauEFollower(
//              xE, xM,
//              follower.getDistance().neg(),
//              follower.getSpeed(),
//              dxMin, Tdes);
//      // Re-compute planned merge acceleration aM for consistency
//      Acceleration aM = null;
//      try
//      {
//          aM = computeMergeAcceleration(
//                  xE, xM, vM,
//                  leader, follower,
//                  tauELeader, tauEFollower,
//                  dxMin, Tdes, tauLC);
//      }
//      catch (ParameterException | GtuException | NetworkException exception)
//      {
//          exception.printStackTrace();
//      }
//
//      gap.setAM(aM);
//
//      if (isGapFeasible(
//              xE, xM, vM, lM,
//              xL, vL, lL,
//              xF, vF, lF,
//              aM, tauELeader, tauEFollower, tauLC, dxMin,
//              vTargetLane) == false) {
//          try
//          {
//              return transitionTo(new SearchForGapState(this.maneuverPattern));
//          }
//          catch (ParameterException | NullPointerException | IllegalArgumentException | GtuException
//                  | NetworkException exception)
//          {
//              exception.printStackTrace();
//          }
//      }
//
//      return null;
//  }
//
//  @Override
//  public String toString() {
//      return "AccelerateToTargetGapState";
//  }
//}
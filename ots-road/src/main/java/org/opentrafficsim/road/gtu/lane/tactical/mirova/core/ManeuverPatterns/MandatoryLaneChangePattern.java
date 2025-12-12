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
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.AutobahnFreeDrivingPattern.FreeDrivingState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.DiscretionaryLaneChangePattern.ActionStateCompleteLaneChange;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.*;

import static org.opentrafficsim.base.parameters.ParameterTypes.*;
import static org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters.*;

/**
 * Mandatory lane change pattern implementing the full Berghaus & Oeser (2025) gap selection
 * and targeted acceleration (aM) planning using dimensional quantities.
 */
public class MandatoryLaneChangePattern extends ManeuverPattern {

    private final LateralDirectionality targetDirection;
    private GapCandidate activeGap;



    public MandatoryLaneChangePattern(final KnowledgeChunk kc, final LateralDirectionality targetDirection) {
        super(PatternType.COOPERATIVE, kc);
        this.targetDirection = targetDirection;
        this.initialActionState = new MatchTargetLaneSpeedState(this);
        this.requiredContextKeys.add("Ego");
        this.requiredContextKeys.add("Neighbors");
        this.requiredContextKeys.add("Infrastructure");
        this.requiredContextKeys.add("MacroTraffic");
    }

    public LateralDirectionality getTargetDirection() { return this.targetDirection; }
    public GapCandidate getActiveGap() { return this.activeGap; }
    public void setActiveGap(final GapCandidate gap) { this.activeGap = gap; }

    @Override
    public boolean checkContext() { return true; }

    @Override
    public boolean checkAbility() { return true; }

    /** Represents a feasible gap between target-lane Leader and Follower. */
    public static class GapCandidate {
        private final HeadwayGtu leader;
        private final HeadwayGtu follower;
        private final Length mergePoint;
        private Acceleration aM;     // planned merge acceleration

        public GapCandidate(
                final HeadwayGtu leader,
                final HeadwayGtu follower,
                final Length mergePoint,
                final Acceleration aM) {
            this.leader = leader;
            this.follower = follower;
            this.mergePoint = mergePoint;
            this.aM = aM;
        }

        public HeadwayGtu getLeader() { return this.leader; }
        public HeadwayGtu getFollower() { return this.follower; }
        public Length getMergePoint() { return this.mergePoint; }
        public Acceleration getAM() { return this.aM; }
        public void setAM(final Acceleration aM) {
            this.aM = aM;
        }

        public boolean checkGapStillValid(final MirovaTacticalPlanner egoVehicle, final MandatoryLaneChangePattern pattern)
                throws ParameterException, GtuException, NetworkException {
            NeighborsContext neigh = egoVehicle.getContext(NeighborsContext.class);
            NeighborsPerception neighborsPerception =
                    egoVehicle.getPerception().getPerceptionCategory(NeighborsPerception.class);

            RelativeLane targetLane = pattern.targetDirection.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;

            if (this.follower.isAhead()) {
                final Iterator<HeadwayGtu> followerIt = neighborsPerception.getFollowers(targetLane).iterator();

                // Start gap with current immediate leader on the target lane (ahead)
                HeadwayGtu leaderI = neigh.getLeader(pattern.getTargetDirection());
                if (this.leader == null)
                {
                    // No leader => no well-defined bounded gap to merge into; keep searching later.
                    return false;
                }

                while (followerIt.hasNext())
                {
                    final HeadwayGtu followerI = followerIt.next();

                    if (followerI == this.follower) {
                        // found current follower; check if leader matches
                        return leaderI == this.leader;
                    }

                    // Shift upstream: next candidate gap is between previous follower and its upstream follower
                    leaderI = followerI;

                }

          }
            else {
                // follower is behind ego
                final Iterator<HeadwayGtu> leaderIt = neighborsPerception.getLeaders(targetLane).iterator();

                // Start gap with current immediate follower on the target lane (behind)
                HeadwayGtu followerI = neigh.getFollower(pattern.getTargetDirection());
                if (this.follower == null)
                {
                    // No follower => no well-defined bounded gap to merge into; keep searching later.
                    return false;
                }

                while (leaderIt.hasNext())
                {
                    final HeadwayGtu leaderI = leaderIt.next();

                    if (leaderI == this.leader) {
                        // found current leader; check if follower matches
                        return followerI == this.follower;
                    }

                    // Shift downstream: next candidate gap is between previous leader and its downstream leader
                    followerI = leaderI;

                }
            }
            return false;

        }
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
                    Length.instantiateSI(150.0),
                    targetLaneSpeed);

            acc = Acceleration.min(acc, aToMatch);

            SimpleOperationalPlan plan = new SimpleOperationalPlan(acc, params.getParameter(DT));
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

            if (mand.magnitude() >= this.vehicle.getParameters().getParameter(DSEARCH))
                return transitionTo(new SearchForGapState(this.maneuverPattern));

            return null;
        }

        @Override
        public SimpleOperationalPlan abort() { return null; }
    }


    /* =========================================================================================
     * 2) STATE: SEARCH_FOR_GAP — CLEAN BERGHAUS & OESER (2025) FORMULATION (NO COOPERATION)
     * ========================================================================================= */
    public static class SearchForGapState extends ActionState
    {
        /** Owning pattern. */
        private final MandatoryLaneChangePattern pattern;

        public SearchForGapState(final ManeuverPattern p)
        {
            super(p);
            this.pattern = (MandatoryLaneChangePattern) p;
            this.active = true;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            final EgoContext ego = this.vehicle.getContext(EgoContext.class);
            final InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            final MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
            final Parameters params = this.vehicle.getParameters();

            // Longitudinal baseline: regular car-following
            Acceleration acc = ego.getCurrentCarFollowingAcceleration();

            // Optional: gently align with target-lane average speed to ease merging
            Speed targetLaneSpeed = ego.getEgoSpeed();
            if (this.pattern.getTargetDirection().isLeft())
            {
                targetLaneSpeed = macro.getAverageSpeedLeft();
            }
            else if (this.pattern.getTargetDirection().isRight())
            {
                targetLaneSpeed = macro.getAverageSpeedRight();
            }

            final Acceleration aToMatch = CarFollowingUtil.approachTargetSpeed(
                    this.vehicle.getCarFollowingModel(),
                    params,
                    ego.getEgoSpeed(),
                    infra.getCurrentSpeedLimit(),
                    Length.instantiateSI(150.0),
                    targetLaneSpeed);

            acc = Acceleration.min(acc, aToMatch);

            SimpleOperationalPlan plan = new SimpleOperationalPlan(acc, params.getParameter(DT));
            LateralDirectionality direction = ((MandatoryLaneChangePattern)this.maneuverPattern).getTargetDirection();
            if (direction == LateralDirectionality.LEFT) {
                plan.setIndicatorIntentLeft();
            } else if (direction == LateralDirectionality.RIGHT) {
                plan.setIndicatorIntentRight();
            }

            return plan;
        }

        @Override
        public SimpleOperationalPlan next() throws ParameterException, OperationalPlanException, NetworkException, GtuException
        {
            final NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            final Parameters params = this.vehicle.getParameters();

            // (a) immediate feasible lane change?
            if (neigh.getIfLaneChangePossible(this.pattern.getTargetDirection()))
            {
                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));
            }

            // (b) Berghaus & Oeser gap search (no cooperation here)
            final GapCandidate gap = computeGapCandidate();
            this.pattern.setActiveGap(gap);

            // (c) if we found a valid candidate and urgency is high enough -> accelerate-to-gap phase
            if (gap != null)
            {
                return transitionTo(new AccelerateToTargetGapState(this.maneuverPattern));
            }

            // (d) emergency break if we are close to the end of the lane and no gap found
            final InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
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
        public SimpleOperationalPlan abort()
        {
            return null;
        }

        /**
         * Performs upstream gap search on the target lane, using the Berghaus & Oeser (2025) formulation.
         *
         * Coordinate convention in OTS (ego-centric):
         * - xM := 0 (ego reference position; "Merger")
         * - xE := distance from ego to end of ramp / end of current lane (merge point)
         * - xL := leader distance in target lane (positive; ahead of ego)
         * - xF := follower distance in target lane (negative; behind ego)
         *
         * Note: the paper uses vehicle centers, thus ±L/2 terms appear; we map this explicitly.
         *
         * @return first feasible gap candidate upstream, or null if none found
         */
        private GapCandidate computeGapCandidate() throws ParameterException, GtuException, NetworkException
        {
            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
            MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            // Perception iteration (upstream on target lane)
            final NeighborsPerception neighPerception =
                    this.vehicle.getPerception().getPerceptionCategory(NeighborsPerception.class);
            final RelativeLane targetLane =
                    this.pattern.getTargetDirection().isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;


            if (macro.getAverageSpeed(targetLane).ge(ego.getEgoSpeed()))
            {
                final Iterator<HeadwayGtu> followerIt = neighPerception.getFollowers(targetLane).iterator();

                // Start gap with current immediate leader on the target lane (ahead)
                HeadwayGtu leader = neighbors.getLeader(this.pattern.getTargetDirection());
                if (leader == null)
                {
                    // No leader => no well-defined bounded gap to merge into; keep searching later.
                    return null;
                }

                while (followerIt.hasNext())
                {
                    final HeadwayGtu follower = followerIt.next();
                    if (follower == null)
                    {
                        continue;
                    }
                    GapCandidate gapCandidate = searchGapCandidate(follower, leader);
                    if (gapCandidate != null)
                    {
                        return gapCandidate;
                    }


                    // Shift upstream: next candidate gap is between previous follower and its upstream follower
                    leader = follower;
                }
            }
            else
            {
                final Iterator<HeadwayGtu> leaderIt = neighPerception.getLeaders(targetLane).iterator();

                // Start gap with current immediate follower on the target lane (behind)
                HeadwayGtu follower = neighbors.getFollower(this.pattern.getTargetDirection());
                if (follower == null)
                {
                    // No follower => no well-defined bounded gap to merge into; keep searching later.
                    return null;
                }

                while (leaderIt.hasNext())
                {
                    final HeadwayGtu leader = leaderIt.next();
                    if (leader == null)
                    {
                        continue;
                    }
                    GapCandidate gapCandidate = searchGapCandidate(follower, leader);
                    if (gapCandidate != null)
                    {
                        return gapCandidate;
                    }
                    // Shift downstream: next candidate gap is between previous leader and its downstream leader
                    follower = leader;
                    }
                }

            return null;
        }

        /**
         * Searches a single gap candidate between given follower and leader GTUs.
         *
         * @param follower target-lane follower GTU
         * @param leader target-lane leader GTU
         * @return gap candidate if feasible, null otherwise
         * @throws ParameterException on parameter error
         * @throws GtuException on GTU error
         * @throws NetworkException on network error
         */
        private GapCandidate searchGapCandidate(final HeadwayGtu follower, HeadwayGtu leader) throws ParameterException, GtuException, NetworkException {
            final EgoContext ego = this.vehicle.getContext(EgoContext.class);
            final InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            final MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);

            final Parameters params = this.vehicle.getParameters();

            // Paper parameters (mapped)
            final Duration tauLC = params.getParameter(ParameterTypes.LCDUR);
            final Length dxMin = params.getParameter(ParameterTypes.S0);
            final Duration Tdes = params.getParameter(ParameterTypes.T);

            // Coordinate anchor: merge point at end of lane
            final Length xE = infra.getDistanceToLaneEnd();     // merge point (end of ramp / lane)
            final Length xM = Length.instantiateSI(0.0);        // ego reference
            final Speed vM = ego.getEgoSpeed();                 // ego speed
            final Length lM = this.vehicle.getGtu().getLength(); // ego length

            // Target-lane speed for feasibility branch (26)/(27)
            final Speed vTargetLane =
                    this.pattern.getTargetDirection().isLeft()
                            ? macro.getAverageSpeedLeft()
                            : macro.getAverageSpeedRight();

            // Placeholder for gap search logic

            // --- Map OTS headways to paper positions (center-based) ---
            final Length xL = leader.getDistance();             // ahead of ego (+)
            final Length xF = follower.getDistance().neg();    // behind ego (-)
            final Speed vL = leader.getSpeed();
            final Speed vF = follower.getSpeed();
            final Length lL = leader.getLength();
            final Length lF = follower.getLength();

            // --- Time horizons from paper ---
            final Duration tauELeader = computeTauELeader(xL, xE, xM, vL, vM, dxMin, Tdes);
            if (tauELeader.si <= 0.0)
            {
                leader = follower;
                return null;
            }

            final Duration tauEFollower = computeTauEFollower(xE, xM, xF, vF, dxMin, Tdes);
            if (tauEFollower.si <= 0.0)
            {
                leader = follower;
                return null;
            }

            // --- Planned merge acceleration aM = min(aLeader, aFollower, aCF) ---
            final Acceleration aM = computeMergeAcceleration(
                    xE, xM, vM,
                    leader, follower,
                    tauELeader, tauEFollower,
                    dxMin, Tdes, tauLC);

            // --- Feasibility check (26)/(27) ---
            if (isGapFeasible(
                    xE, xM, vM, lM,
                    xL, vL, lL,
                    xF, vF, lF,
                    aM, tauELeader, tauEFollower, tauLC, dxMin,
                    vTargetLane))
            {
                // Merge point in our OTS representation is simply xE (distance to lane end).
                // Store it as such (planning uses xE as merge target).
                return new GapCandidate(leader, follower, xE, aM);
            }
            else
            {
                // Gap not feasible; continue searching upstream
                return null;
            }
        }

        /* =========================================================================================
         *  Eq. (12): tau_E for leader constraint
         * ========================================================================================= */

        /**
         * Eq. (12) from Berghaus & Oeser (2025), adapted to ego-centric coordinates.
         * Uses the smallest positive real solution.
         * Eliminate vehicle lengths because we measure distances from vehicle front end to back end and not centers.
         */
        protected Duration computeTauELeader(
                final Length xL,
                final Length xE,
                final Length xM,
                final Speed vL,
                final Speed vM,
                final Length dxMin,
                final Duration Tdes)
        {
            final double vLsi = Math.max(vL.si, 0.1); // avoid division by ~0

            // alpha = xL - xE - dxMin + Tdes*vM -> eliminate vehicle lengths because we measure
            // distances from vehicle front end to back end and not centers
            final double alpha =
                    xL.si - xE.si - dxMin.si + (Tdes.si * vM.si);

            // sqrt(alpha^2 + 8*vL*Tdes*(xE-xM))
            final double inside =
                    alpha * alpha + 8.0 * vLsi * Tdes.si * (xE.si - xM.si);

            final double sqrtTerm = Math.sqrt(Math.max(0.0, inside));

            // numerator = -xL + xE + dxMin - Tdes*vM ± sqrtTerm
            final double base =
                    -xL.si + xE.si  + dxMin.si - (Tdes.si * vM.si);

            final double numPlus = base + sqrtTerm;
            final double numMinus = base - sqrtTerm;

            // choose smallest positive real root
            final double numerator =
                    (numMinus > 0.0) ? Math.min(numPlus, numMinus) : numPlus;

            final double tauE = numerator / (2.0 * vLsi);

            return Duration.instantiateSI(tauE);
        }

        /* =========================================================================================
         *  Eq. (24): tau_E,Fol for follower constraint
         * ========================================================================================= */

        /**
         * Eq. (24) from Berghaus & Oeser (2025), adapted to ego-centric coordinates.
         *
         * tau_E,Fol = (xE - xF - LM/2 - LF/2 - dxMin)/vF - Tdes
         * eliminate vehicle lengths because we measure distances from vehicle front end to back end and not centers
         *
         * Note: xF is negative (behind ego).
         */
        protected Duration computeTauEFollower(
                final Length xE,
                final Length xM,
                final Length xF,
                final Speed vF,
                final Length dxMin,
                final Duration Tdes)
        {
            final double vFsi = Math.max(vF.si, 0.1);

            final double tau =
                    (xE.si - xF.si  - dxMin.si) / vFsi
                    - Tdes.si;

            return Duration.instantiateSI(tau);
        }

        /* =========================================================================================
         *  a_M computation: min(leader-constraint, follower-constraint, baseline car-following)
         * ========================================================================================= */

        protected Acceleration computeMergeAcceleration(
                final Length xE,
                final Length xM,
                final Speed vM,
                final HeadwayGtu leader,
                final HeadwayGtu follower,
                final Duration tauELeader,
                final Duration tauEFollower,
                final Length dxMin,
                final Duration Tdes,
                final Duration tauLC) throws ParameterException, GtuException, NetworkException
        {
            final Parameters params = this.vehicle.getParameters();
            final EgoContext ego = this.vehicle.getContext(EgoContext.class);

            // --- Leader constraint: a_M,Leader = min(aDesiredHeadway, aZeroHeadway) ---
            final Acceleration aLeader = computeAMLeader(
                    xE, xM, vM,
                    leader.getSpeed(),
                    tauELeader,
                    dxMin, Tdes, tauLC);

            // --- Follower constraint: a_M,Fol (Eq. 25) ---
            final Acceleration aFollower = computeAMFollower(xE, xM, vM, tauEFollower);

            // --- Baseline CF (do not exceed own car-following plan) ---
            final Acceleration aCF = ego.getCurrentCarFollowingAcceleration();

            final double aMin = Math.min(aLeader.si, Math.min(aFollower.si, aCF.si));
            return Acceleration.instantiateSI(aMin);
        }

        /**
         * Leader-based merge acceleration:
         * - Eq. (10): desired-headway acceleration
         * - Eq. (14): zero-headway acceleration
         * - take min(.) of both.
         */
        private Acceleration computeAMLeader(
                final Length xE,
                final Length xM,
                final Speed vM,
                final Speed vL,
                final Duration tauELeader,
                final Length dxMin,
                final Duration Tdes,
                final Duration tauLC)
        {
            // Eq. (10): aDesired = 2 * ( (xE-xM) - vM*tauE ) / tauE^2
            final double tauE = Math.max(tauELeader.si, 0.1);
            final double aDesired =
                    2.0 * ((xE.si - xM.si) - vM.si * tauE) / (tauE * tauE);

            // Eq. (14): aZeroHeadway, with tauZero = tauE - tauLC
            final double tauZero = Math.max(tauELeader.si - tauLC.si, 0.1);
            final double aZero =
                    ((vL.si - vM.si) * tauZero + dxMin.si) / (0.5 * tauZero * tauZero);

            return Acceleration.instantiateSI(Math.min(aDesired, aZero));
        }

        /**
         * Follower-based merge acceleration (Eq. 25):
         * a_M,Fol = 2 * ( (xE-xM) - vM*tau_E,Fol ) / tau_E,Fol^2
         */
        private Acceleration computeAMFollower(
                final Length xE,
                final Length xM,
                final Speed vM,
                final Duration tauEFollower)
        {
            final double tau = Math.max(tauEFollower.si, 0.1);
            final double a =
                    2.0 * ((xE.si - xM.si) - vM.si * tau) / (tau * tau);
            return Acceleration.instantiateSI(a);
        }

        /* =========================================================================================
         *  Feasibility check: Eq. (26)-(27)
         * ========================================================================================= */

        protected boolean isGapFeasible(
                final Length xE,
                final Length xM,
                final Speed vM,
                final Length lM,
                final Length xL,
                final Speed vL,
                final Length lL,
                final Length xF,
                final Speed vF,
                final Length lF,
                final Acceleration aM,
                final Duration tauELeader,
                final Duration tauEFollower,
                final Duration tauLC,
                final Length dxMin,
                final Speed vTargetLane)
        {
            // tauPass,i per Eq. (28):
            // if x_i(t) > x_M(t) (i is ahead): tauPass = tauE_{i+1} - tauLC (leader-side)
            // else: tauPass = tauE_i - tauLC (follower-side)
            final Duration tauPass =
                    (vTargetLane.si < vM.si)
                            ? Duration.instantiateSI(Math.max(0.0, tauELeader.si - tauLC.si))
                            : Duration.instantiateSI(Math.max(0.0, tauEFollower.si - tauLC.si));

            // Predict merger position at tauPass with constant acceleration aM
            final Length xM_end = Length.instantiateSI(
                    xM.si + vM.si * tauPass.si + 0.5 * aM.si * tauPass.si * tauPass.si
            );

            // Predict follower position at tauPass with constant speed vF
            final Length xF_end = Length.instantiateSI(
                    xF.si + vF.si * tauPass.si
            );

            // Eq. (26): target lane slower than merger -> ensure merger not behind follower (rear constraint)
            if (vTargetLane.si < vM.si)
            {
                // xM_end - xF_end - LM/2 - dxMin >= 0
                final double lhs = xM_end.si - xF_end.si - (lM.si) - dxMin.si;
                return lhs >= 0.0;
            }

            // Eq. (27): target lane faster than merger -> ensure follower not behind merger (alternative ordering)
            // xF_end - xM_end - LM/2 - dxMin >= 0
            final double lhs = xF_end.si - xM_end.si - (lM.si ) - dxMin.si;
            return lhs >= 0.0;
        }
    }


    /* =========================================================================================
     * 3) STATE: ACCELERATE_TO_TARGET_GAP — BERGHAUS & OESER CONSISTENT
     * ========================================================================================= */
    public static class AccelerateToTargetGapState extends SearchForGapState {

        private final MandatoryLaneChangePattern pattern;

        public AccelerateToTargetGapState(final ManeuverPattern p) {
            super(p);
            this.pattern = (MandatoryLaneChangePattern) p;
            this.active = true;
        }

        @Override
        public SimpleOperationalPlan executeControl()
                throws ParameterException, GtuException, NetworkException {

            final EgoContext ego = this.vehicle.getContext(EgoContext.class);
            Parameters params = this.vehicle.getParameters();

            final GapCandidate gap = this.pattern.getActiveGap();
            // aM was already updated in abort method
            Acceleration acc = gap.getAM();

            // Defensive clipping against car-following baseline
            Acceleration aCF = ego.getCurrentCarFollowingAcceleration();
            acc = Acceleration.min(acc, aCF);

            SimpleOperationalPlan plan = new SimpleOperationalPlan(acc, params.getParameter(DT));
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

            final NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);

            if (neigh.getIfLaneChangePossible(this.pattern.getTargetDirection()))
                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));
            return null;
        }

        @Override
        public SimpleOperationalPlan abort() {
            final GapCandidate gap = this.pattern.getActiveGap();
            try
            {
                if (gap.checkGapStillValid(this.vehicle, this.pattern) == false)
                {
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
            }
            catch (ParameterException | GtuException | NetworkException exception)
            {
                exception.printStackTrace();
            }

            final EgoContext ego = this.vehicle.getContext(EgoContext.class);
            final InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            final Parameters params = this.vehicle.getParameters();

            final Speed vM = ego.getEgoSpeed();
            final Length xE = gap.getMergePoint();      // distance to lane end
            final Length xM = Length.instantiateSI(0.0); // ego reference
            final HeadwayGtu leader = gap.getLeader();
            final HeadwayGtu follower = gap.getFollower();
            // Paper parameters (mapped)
            Duration tauLC = null;
            try
            {
                tauLC = params.getParameter(ParameterTypes.LCDUR);
            }
            catch (ParameterException exception)
            {
                exception.printStackTrace();
            }
            Length dxMin = null;
            try
            {
                dxMin = params.getParameter(ParameterTypes.S0);
            }
            catch (ParameterException exception)
            {
                exception.printStackTrace();
            }
            Duration Tdes = null;
            try
            {
                Tdes = params.getParameter(ParameterTypes.T);
            }
            catch (ParameterException exception)
            {
                exception.printStackTrace();
            }

            // Target-lane speed for feasibility branch (26)/(27)
            final MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
            Speed vTargetLane = null;
            try
            {
                vTargetLane = this.pattern.getTargetDirection().isLeft()
                        ? macro.getAverageSpeedLeft()
                        : macro.getAverageSpeedRight();
            }
            catch (OperationalPlanException | ParameterException exception)
            {
                exception.printStackTrace();
            }

            final Length lM = this.vehicle.getGtu().getLength(); // ego length
            final Length xL = leader.getDistance();             // ahead of ego (+)
            final Length xF = follower.getDistance().neg();    // behind ego (-)
            final Speed vL = leader.getSpeed();
            final Speed vF = follower.getSpeed();
            final Length lL = leader.getLength();
            final Length lF = follower.getLength();


            // Re-compute time horizons for consistency
            final Duration tauELeader =computeTauELeader(
                    leader.getDistance(),
                    xE, xM,
                    leader.getSpeed(),
                    vM,
                    dxMin, Tdes);
            final Duration tauEFollower = computeTauEFollower(
                    xE, xM,
                    follower.getDistance().neg(),
                    follower.getSpeed(),
                    dxMin, Tdes);
            // Re-compute planned merge acceleration aM for consistency
            Acceleration aM = null;
            try
            {
                aM = computeMergeAcceleration(
                        xE, xM, vM,
                        leader, follower,
                        tauELeader, tauEFollower,
                        dxMin, Tdes, tauLC);
            }
            catch (ParameterException | GtuException | NetworkException exception)
            {
                exception.printStackTrace();
            }

            gap.setAM(aM);

            if (isGapFeasible(
                    xE, xM, vM, lM,
                    xL, vL, lL,
                    xF, vF, lF,
                    aM, tauELeader, tauEFollower, tauLC, dxMin,
                    vTargetLane) == false) {
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

            return null;
        }

        @Override
        public String toString() {
            return "AccelerateToTargetGapState";
        }
    }


    /* =========================================================================================
     * 4) STATE: BREAKING_END_OF_RAMP
     * ========================================================================================= */

    public static class BreakingEndOfRampState extends ActionState {

        public BreakingEndOfRampState(final ManeuverPattern p) {
            super(p);
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

            SimpleOperationalPlan plan = new SimpleOperationalPlan(a, params.getParameter(DT));
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
    }

    /* =========================================================================================
     * 5) STATE: EXECUTE_LANE_CHANGE
     * ========================================================================================= */

    public static class ExecuteLaneChangeState extends ActionState {

        /** Target direction of the lane change (LEFT or RIGHT). */
        private final LateralDirectionality direction;


        /** Cached origin lane to detect completion. */
        private final Lane originLane;


        // ----------------------------------------------------------------------
        // Construction
        // ----------------------------------------------------------------------

        /** ActionStatePerformLaneChange constructor.
         * @param pattern
         * @param direction
         */
        public ExecuteLaneChangeState(final ManeuverPattern pattern, final LateralDirectionality direction) {
            super(pattern);
            this.direction = direction;

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
                    params.getParameter(ParameterTypes.DT),
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

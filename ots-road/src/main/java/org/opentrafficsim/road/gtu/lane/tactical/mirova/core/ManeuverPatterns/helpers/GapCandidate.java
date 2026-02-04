package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.helpers;

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
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.MacroTrafficContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;


import java.util.Iterator;

/**
 * Represents a potential gap on a target lane, defined by a Leader (vehicle ahead)
 * and a Follower (vehicle behind).
 * <p>
 * This class encapsulates gap geometry and solves the <b>Object Identity issue</b> in OTS
 * by re-matching vehicles via persistent IDs.
 * </p>
 * <p>
 * <b>Optimization Update:</b> This version uses an <b>Adaptive Search Heuristic</b>.
 * It tracks the last known location of the gap (Upstream, Downstream, or Straddling) to
 * determine which perception list (Leaders vs. Followers) to scan first. This allows for
 * an "Early Exit" in O(N) time for the majority of simulation steps, skipping the second
 * list traversal entirely if the gap is found intact in the expected location.
 * </p>
 *
 * @author MiRoVA Architect
 */
public class GapCandidate
{
    private MirovaTacticalPlanner vehicle;
    private HeadwayGtu leader;
    private HeadwayGtu follower;
    private final String leaderId;
    private final String followerId;

    private Acceleration aM;
    private Acceleration lastComputedAM;

    private static final Duration MAX_CONVERGENCE_TIME = Duration.instantiateSI(10.0);

    private LateralDirectionality gapDirection;

    /**
     * Tracks the relative position of the gap to optimize search order.
     */
    private enum GapLocation {
        /** Gap is fully ahead of Ego (Follower distance > 0). Check Leaders list first. */
        DOWNSTREAM,
        /** Gap is fully behind Ego (Leader distance < 0). Check Followers list first. */
        UPSTREAM,
        /** Ego is inside the gap (Follower < 0 < Leader). Must check both lists. */
        STRADDLE
    }

    /** The last confirmed location of this gap. Used as a heuristic for the next tick. */
    private GapLocation lastKnownLocation;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    public GapCandidate(final HeadwayGtu leader, final HeadwayGtu follower, final LateralDirectionality gapDirection, final MirovaTacticalPlanner vehicle)
    {
        if (leader == null || follower == null)
        {
            throw new IllegalArgumentException("GapCandidate requires non-null leader and follower GTUs.");
        }
        this.leader = leader;
        this.follower = follower;
        this.leaderId = leader.getId();
        this.followerId = follower.getId();
        this.vehicle = vehicle;
        this.gapDirection = gapDirection;

        // Initialize heuristic based on initial geometry
        this.lastKnownLocation = determineLocation(leader, follower);
    }

    // ----------------------------------------------------------------------
    // Accessors
    // ----------------------------------------------------------------------

    public HeadwayGtu getLeader() { return this.leader; }
    public HeadwayGtu getFollower() { return this.follower; }
    public LateralDirectionality getGapDirection() { return this.gapDirection; }
    //public Length getMergePoint() { return this.mergePoint; }
    public Acceleration getAM() { return this.aM; }
    public void setAM(final Acceleration aM) { this.aM = aM; }

    // ----------------------------------------------------------------------
    // Validity & Consistency Logic (Adaptive & Optimized)
    // ----------------------------------------------------------------------

    /**
     * Checks if the gap still exists and is valid in the current time step.
     * <p>
     * Uses the {@link #lastKnownLocation} to prioritize the search. If the gap is found
     * adjacent in the prioritized list, the method returns immediately (Early Exit),
     * avoiding unnecessary iterations.
     * </p>
     *
     * @param neighborsPerception the perception of neighboring vehicles
     * @param targetLane          the relative lane to scan
     * @return {@code true} if the gap is valid and updated; {@code false} otherwise.
     */
    public boolean checkGapStillValid()
    {

        NeighborsContext neighborsContext = this.vehicle.getContext(NeighborsContext.class);
        // Mutable state container for the search process
        SearchState state = new SearchState();

        // 1. Heuristic Decision: Where to look first?
        if (this.lastKnownLocation == GapLocation.DOWNSTREAM)
        {
            // Expectation: Gap is ahead. Scan Leaders first.
            scanLeaders(neighborsContext.getLeaders(this.gapDirection), state);
            if (state.isCompleteAndAdjacent()) return updateSuccess(state);

            // Fallback: Scan Followers (Gap might have drifted back or we overtook it)
            scanFollowers(neighborsContext.getFollowers(this.gapDirection), state);
        }
        else if (this.lastKnownLocation == GapLocation.UPSTREAM)
        {
            // Expectation: Gap is behind. Scan Followers first.
            scanFollowers(neighborsContext.getFollowers(this.gapDirection), state);
            if (state.isCompleteAndAdjacent()) return updateSuccess(state);

            // Fallback: Scan Leaders (Gap might have accelerated ahead)
            scanLeaders(neighborsContext.getLeaders(this.gapDirection), state);
        }
        else // STRADDLE
        {
            // Expectation: We are in the middle. We likely need information from BOTH lists.
            // Order is less critical here, but checking both is usually required to confirm identity.
            scanLeaders(neighborsContext.getLeaders(this.gapDirection), state);
            scanFollowers(neighborsContext.getFollowers(this.gapDirection), state);
        }

        // 2. Final Logic (handles Straddle case and Cross-List verification)
        if (state.isCompleteAndAdjacent())
        {
            return updateSuccess(state);
        }

        // Special Case: Straddle (Ego is between Follower and Leader)
        // This is valid if:
        // - Found Leader is the FIRST in Leaders list (closest ahead)
        // - Found Follower is the FIRST in Followers list (closest behind)
        if (state.foundLeader != null && state.foundFollower != null && !state.adjacent)
        {
            if (state.leaderIsFirstInLeaders && state.followerIsFirstInFollowers)
            {
                // It's a valid straddle
                state.adjacent = true; // logically adjacent via Ego
                return updateSuccess(state);
            }
        }

        return false;
    }

    /**
     * Updates internal state after a successful validation and updates the heuristic location.
     */
    private boolean updateSuccess(final SearchState state)
    {
        this.leader = state.foundLeader;
        this.follower = state.foundFollower;

        // Update heuristic for next tick (Adaptive behavior)
        this.lastKnownLocation = determineLocation(this.leader, this.follower);

        return true;
    }

    private GapLocation determineLocation(final HeadwayGtu l, final HeadwayGtu f)
    {
        // Distances are relative to Ego front/ref.
        // Follower > 0 means Follower is ahead of Ego -> Gap is Downstream
        if (f.getDistance().si >= 0.0) return GapLocation.DOWNSTREAM;

        // Leader < 0 means Leader is behind Ego -> Gap is Upstream
        if (l.getDistance().si <= 0.0) return GapLocation.UPSTREAM;

        return GapLocation.STRADDLE;
    }

    // ----------------------------------------------------------------------
    // Search Helpers (avoid code duplication)
    // ----------------------------------------------------------------------

    private void scanLeaders(final Iterable<HeadwayGtu> leaders, final SearchState state)
    {
        Iterator<HeadwayGtu> it = leaders.iterator();
        HeadwayGtu prev = null;
        boolean first = true;

        while (it.hasNext())
        {
            HeadwayGtu gtu = it.next();
            String id = gtu.getId();

            if (id.equals(this.leaderId))
            {
                state.foundLeader = gtu;
                if (first) state.leaderIsFirstInLeaders = true;

                // Adjacency Check (Downstream logic): Follower comes BEFORE Leader in distance-sorted list
                if (prev != null && prev.getId().equals(this.followerId))
                {
                    state.adjacent = true;
                }
            }
            else if (id.equals(this.followerId))
            {
                state.foundFollower = gtu;
            }
            prev = gtu;
            first = false;
        }
    }

    private void scanFollowers(final Iterable<HeadwayGtu> followers, final SearchState state)
    {
        Iterator<HeadwayGtu> it = followers.iterator();
        HeadwayGtu prev = null;
        boolean first = true;

        while (it.hasNext())
        {
            HeadwayGtu gtu = it.next();
            String id = gtu.getId();

            if (id.equals(this.followerId))
            {
                state.foundFollower = gtu;
                if (first) state.followerIsFirstInFollowers = true;

                // Adjacency Check (Upstream logic): Leader comes BEFORE Follower in magnitude-sorted list
                if (prev != null && prev.getId().equals(this.leaderId))
                {
                    state.adjacent = true;
                }
            }
            else if (id.equals(this.leaderId))
            {
                state.foundLeader = gtu;
            }
            prev = gtu;
            first = false;
        }
    }


 // ----------------------------------------------------------------------
    // Math & Physics Logic
    // ----------------------------------------------------------------------

    /**
     * Recalculates the required acceleration {@code aM} for this gap based on the Berghaus & Oeser (2025) model.
     * <p>
     * This method performs three critical steps:
     * <ol>
     * <li><b>State Validation:</b> Uses heuristics to verify if the specific Leader/Follower pair still constitutes a valid gap.</li>
     * <li><b>Coordinate Transformation:</b> Maps the OTS relative net distances to the absolute coordinate system used in the paper.
     * The Ego front is defined as the reference point (x = 0).
     * <ul>
     * <li><i>x_M (Merger/Ego):</i> 0.0</li>
     * <li><i>x_L (Leader):</i> Positive net distance to leader's rear bumper.</li>
     * <li><i>x_F (Follower):</i> Negative distance to follower's front bumper. Calculated as {@code -(EgoLength + NetDistance)}.</li>
     * <li><i>x_E (MergePoint):</i> Distance to the effective end of the acceleration lane (physical end minus safety buffer).</li>
     * </ul>
     * </li>
     * <li><b>Model Execution:</b> Solves the equations for time horizons (Tau) and target acceleration (aM),
     * and validates the kinematic feasibility of the maneuver.</li>
     * </ol>
     * </p>
     *
     * @return The calculated acceleration {@code aM}, or {@code null} if the gap has collapsed,
     * the merge point has been passed, or no physically feasible solution exists.
     * @throws ParameterException if parameters are missing.
     * @throws GtuException if GTU state cannot be accessed.
     * @throws NetworkException if network topology is inconsistent.
     */
    public Acceleration computeCurrentAcceleration()
            throws ParameterException, GtuException, NetworkException {

        // 1. Update Gap State
        if (!checkGapStillValid()) {
            return null;
        }

        // 2. Prepare Calculation Context
        InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
        EgoContext ego = this.vehicle.getContext(EgoContext.class);
        MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
        Parameters params = this.vehicle.getParameters();
        Length emergencyBuffer = params.getParameter(MirovaParameters.emergencyStoppingDistance);

        // --- COORDINATE MAPPING (Reference: Ego Front = 0) ---
        Length lM = this.vehicle.getGtu().getLength();

        // xE: Distance to effective end of lane (Physical end minus buffer)
        Length xE = infra.getDistanceToLaneEnd().minus(emergencyBuffer);

        // xM: Ego Front position
        Length xM = Length.ZERO;
        Speed vM = ego.getEgoSpeed();

        // Abort if we have already passed the effective merge point (Model invalid for negative distances)
        if (xE.si <= 0.0) return null;

        // xL: Leader Rear Position (Positive, ahead of Ego)
        // OTS getDistance() returns the net gap from Ego Front to Leader Rear.
        Length xL = this.leader.getDistance();

        // xF: Follower Front Position (Negative, behind Ego)
        // OTS getDistance() returns the net gap from Ego Rear to Follower Front.
        // In the paper's coordinate system (origin at Ego Front), xF is at: -(EgoLength + NetGap).
        Length xF = this.follower.getDistance().plus(lM);//.neg();

        // Paper Parameters
        Duration tauLC = params.getParameter(ParameterTypes.LCDUR);
        Length dxMin = params.getParameter(ParameterTypes.S0);
        Duration Tdes = params.getParameter(ParameterTypes.T);

        // 3. Compute Time Horizons (Tau)
        Duration tauELeader = computeTauELeader(xL, xE, xM, this.leader.getSpeed(), vM, dxMin, Tdes);
        if (tauELeader.si <= 0.0) return null; // Collision inevitable

        Duration tauEFollower = computeTauEFollower(xE, xM, xF, this.follower.getSpeed(), dxMin, Tdes);
        if (tauEFollower.si <= 0.0) return null; // Collision inevitable

        if (tauELeader.gt(MAX_CONVERGENCE_TIME) || tauEFollower.gt(MAX_CONVERGENCE_TIME))
        {
            return null;
        }
//        System.out.println("GTU " + this.vehicle.getGtu().getId()
//                + " xF=" + xF + ", xL=" + xL + ", xE=" + xE + ", egoSpeed=" + vM + ", leaderSpeed=" + this.leader.getSpeed() + ", followerSpeed=" + this.follower.getSpeed()
//                + " Computed Taus for Gap (" + this.leaderId + ", " + this.followerId + "): "
//                + "TauELeader=" + tauELeader + ", TauEFollower=" + tauEFollower);
        // 4. Compute Acceleration aM
        // Minimum of Leader constraints, Follower constraints, and current Car-Following limit.
        Acceleration calculatedAM = computeMergeAcceleration(xE, xM, vM, this.leader, this.follower,
                tauELeader, tauEFollower, dxMin, Tdes, tauLC, ego.getCurrentCarFollowingAcceleration());

//        System.out.println("Calculated aM for Gap (" + this.leaderId + ", " + this.followerId + "): " + calculatedAM);

        // 5. Check Realism of Acceleration
        if (!isAccelerationRealizable(calculatedAM, params)) {
            return null;
        }

        // 6. Check Feasibility (Equations 26 & 27)
        Speed vTargetLane = this.gapDirection.isLeft() ? macro.getAverageSpeedLeft() : macro.getAverageSpeedRight();

        boolean feasible = isGapFeasible(
                xE, xM, vM, lM,
                xL, this.leader.getSpeed(),
                xF, this.follower.getSpeed(),
                calculatedAM, tauELeader, tauEFollower, tauLC, dxMin, vTargetLane
        );

        if (feasible) {
            this.aM = calculatedAM;
            this.lastComputedAM = calculatedAM;
            return calculatedAM;
        } else {
            return null;
        }
    }

    // ----------------------------------------------------------------------
    // Math Helpers (Berghaus & Oeser Implementation)
    // ----------------------------------------------------------------------

    /**
     * Computes {@code tau_E,Leader} according to Eq. (12).
     * <p>
     * Represents the time horizon until the leader constraint becomes active.
     * Calculated as the smallest positive root of the quadratic equation derived from the kinematic constraints.
     * </p>
     *
     * @param xL    Leader rear position
     * @param xE    Merge point position
     * @param xM    Merger (Ego) front position
     * @param vL    Leader speed
     * @param vM    Merger (Ego) speed
     * @param dxMin Minimum distance (s0)
     * @param Tdes  Desired time headway (T)
     * @return The time horizon Tau, or a specialized value if no solution exists.
     */
    private Duration computeTauELeader(final Length xL, final Length xE, final Length xM, final Speed vL, final Speed vM, final Length dxMin, final Duration Tdes) {
        double vLsi = Math.max(vL.si, 0.1); // Avoid division by zero

        // Term alpha from Eq. (12)
        double alpha = xL.si - xE.si - dxMin.si + (Tdes.si * vM.si);

        // Discriminant
        double inside = alpha * alpha + 8.0 * vLsi * Tdes.si * (xE.si - xM.si);
        double sqrtTerm = Math.sqrt(Math.max(0.0, inside));

        double base = -xL.si + xE.si + dxMin.si - (Tdes.si * vM.si);

        // Select the smallest positive solution
        double numerator = (base - sqrtTerm > 0.0) ? Math.min(base + sqrtTerm, base - sqrtTerm) : base + sqrtTerm;

        return Duration.instantiateSI(numerator / (2.0 * vLsi));
    }

    /**
     * Computes {@code tau_E,Follower} according to Eq. (24).
     * <p>
     * Represents the time horizon based on the follower approaching from behind.
     * Solved from the linear kinematic equation.
     * </p>
     *
     * @param xE    Merge point position
     * @param xM    Merger (Ego) front position
     * @param xF    Follower front position (negative)
     * @param vF    Follower speed
     * @param dxMin Minimum distance (s0)
     * @param Tdes  Desired time headway (T)
     * @return The time horizon Tau.
     */
    private Duration computeTauEFollower(final Length xE, final Length xM, final Length xF, final Speed vF, final Length dxMin, final Duration Tdes) {
        double vFsi = Math.max(vF.si, 0.1);
        // Eq (24): (xE - xF - s0) / vF - T
        return Duration.instantiateSI((xE.si - xF.si - dxMin.si - this.vehicle.getGtu().getLength().si) / vFsi - Tdes.si);
    }

    /**
     * Determines the optimal merge acceleration {@code aM}.
     * <p>
     * This calculates the minimum acceleration required to satisfy constraints from:
     * <ul>
     * <li>The Leader (Eq. 10 & 14): Aiming for desired headway or zero headway.</li>
     * <li>The Follower (Eq. 25): Ensuring the follower can maintain their headway.</li>
     * <li>The current Car-Following model: Ensuring safety on the current lane.</li>
     * </ul>
     * The result is the minimum of these values (safest common denominator).
     * </p>
     *
     * @return The instantiated acceleration.
     */
    private Acceleration computeMergeAcceleration(final Length xE, final Length xM, final Speed vM, final HeadwayGtu leader, final HeadwayGtu follower,
                                                  final Duration tauELeader, final Duration tauEFollower, final Length dxMin, final Duration Tdes, final Duration tauLC, final Acceleration aCF) {
        // --- Leader Constraint ---
        double tauEL = Math.max(tauELeader.si, 0.1);
        // Eq (10): aDesired (Acceleration to reach Tdes at merge)
        double aDesired = 2.0 * ((xE.si - xM.si) - vM.si * tauEL) / (tauEL * tauEL);

        // Eq (14): aZeroHeadway (Acceleration to just barely respect s0 -> more aggressive)
        double tauZero = Math.max(tauELeader.si - tauLC.si, 0.1);
        double aZero = ((leader.getSpeed().si - vM.si) * tauZero + (leader.getDistance().si - dxMin.si)) / (0.5 * tauZero * tauZero);

        // Conservative approach: take the minimum
        double aLeader = Math.min(aDesired, aZero);

        // --- Follower Constraint ---
        // Eq (25): Acceleration to ensure the follower isn't forced to breach safety distance
        double tauEF = Math.max(tauEFollower.si, 0.1);
        double aFollower = 2.0 * ((xE.si - xM.si) - vM.si * tauEF) / (tauEF * tauEF);

        // Global Minimum: Apply constraints from Leader, Follower and the current CF model (own leader)
        return Acceleration.instantiateSI(Math.min(aLeader, Math.min(aFollower, aCF.si)));
    }

    /**
     * Verifies the kinematic feasibility of the trajectory according to Eq. (26) and (27).
     * <p>
     * This projects the positions of Ego, Leader, and Follower to the time {@code tauPass} (completion of lane change).
     * It checks if the minimum distance {@code s0} is respected relative to the relevant neighbor
     * (Follower if target lane is slower, Leader if target lane is faster).
     * </p>
     *
     * @param vTargetLane Average speed on the target lane (determines relative positioning logic).
     * @return {@code true} if the gap remains physically viable at the end of the maneuver.
     */
    private boolean isGapFeasible(final Length xE, final Length xM, final Speed vM, final Length lM,
                                  final Length xL, final Speed vL,
                                  final Length xF, final Speed vF,
                                  final Acceleration aM, final Duration tauELeader, final Duration tauEFollower, final Duration tauLC, final Length dxMin, final Speed vTargetLane) {

        Double vGap = (vL.si + vF.si) / 2.0;
        // Determine relevant time horizon tauPass
        Duration tauPass = (vGap < vM.si)
                ? Duration.instantiateSI(Math.max(0.0, tauELeader.si - tauLC.si))
                : Duration.instantiateSI(Math.max(0.0, tauEFollower.si - tauLC.si));

        // 1. Projected Ego Position (Front)
        double xM_end_si = xM.si + vM.si * tauPass.si + 0.5 * aM.si * tauPass.si * tauPass.si;

        // 2. Projected Follower Position (Front)
        double xF_end_si = xF.si + vF.si * tauPass.si;

        // 3. Projected Leader Position (Rear, derived from current net gap xL)
        // xL is net gap. xL_rear(0) = xM(0) + xL.
        // xL_rear(tau) = xM(0) + xL + vL * tau
        double xL_end_si = xM.si + xL.si + vL.si * tauPass.si;

        // Check Rear Gap (Follower Constraint)
        boolean rearClear = (xM_end_si - lM.si - xF_end_si - dxMin.si) >= 0.0;

        // Check Front Gap (Leader Constraint)
        // xL_end (Rear of leader) - xM_end (Front of Ego) >= dxMin
        boolean frontClear = (xL_end_si - xM_end_si - dxMin.si) >= 0.0;

        return rearClear && frontClear;

//        if (vTargetLane.si < vM.si) {
//            // CASE A: Target lane is slower.
//            // We merge IN FRONT of the Follower. Critical constraint is rear gap.
//            // Check: (Ego Rear - Follower Front) >= s0
//            // Ego Rear = xM_end - lM
//            return (xM_end_si - lM.si - xF_end_si - dxMin.si) >= 0.0;
//        } else {
//            // CASE B: Target lane is faster.
//            // We merge BEHIND the Leader.
//            // Although the paper mentions checking xF_end in Eq (27), mathematically we are constrained
//            // by the vehicle behind us if we are slower, or the vehicle in front if we are faster.
//            // Since aM already satisfies the Leader constraint (TauELeader), checking the rear clearance
//            // ensures that our selected deceleration/acceleration didn't cause the follower to crash into us.
//            return (xM_end_si - lM.si - xF_end_si - dxMin.si) >= 0.0;
//        }
    }

    /**
     * Checks if the calculated acceleration is within the physical limits of the vehicle.
     * Use tolerance to filter out mathematical singularities.
     *
     * @param a The calculated acceleration
     * @param params The vehicle parameters
     * @return true if realistic, false otherwise
     */
    private boolean isAccelerationRealizable(final Acceleration a, final Parameters params) throws ParameterException {

        double maxAcc = 1.5;

        double maxDec = -6.0;

        // Toleranz von 0.5 m/s^2 für numerische Stabilität
        if (a.si > maxAcc + 0.5) return false;
        if (a.si < maxDec - 0.5) return false;

        return true;
    }


    /** Internal helper to hold search results during a tick. */
    private static class SearchState
    {
        HeadwayGtu foundLeader = null;
        HeadwayGtu foundFollower = null;
        boolean adjacent = false;
        boolean leaderIsFirstInLeaders = false;
        boolean followerIsFirstInFollowers = false;

        boolean isCompleteAndAdjacent()
        {
            return this.foundLeader != null && this.foundFollower != null && this.adjacent;
        }
    }

    @Override
    public String toString()
    {
        return "GapCandidate[L=" + this.leaderId + ", F=" + this.followerId +
               ", Loc=" + this.lastKnownLocation + "]";
    }
}
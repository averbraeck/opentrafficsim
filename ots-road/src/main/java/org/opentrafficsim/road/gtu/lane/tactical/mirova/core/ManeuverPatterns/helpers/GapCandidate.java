package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.helpers;

import java.util.Iterator;

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
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.MacroTrafficContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;

/**
 * Represents a potential gap on a target lane, defined by a Leader (vehicle ahead)
 * and a Follower (vehicle behind).
 * <p>
 * This class encapsulates gap geometry and solves the <b>Object Identity issue</b> in OTS
 * by re-matching vehicles via persistent IDs. It is used as a helper in <b>Layer 4 (Procedure & Action)</b>
 * by the {@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.exclusive.GapSearchPattern}.
 * </p>
 * <p>
 * <b>Optimization Update:</b> This version uses an <b>Adaptive Search Heuristic</b>.
 * It tracks the last known location of the gap (Upstream, Downstream, or Straddling) to
 * determine which perception list (Leaders vs. Followers) to scan first. This allows for
 * an "Early Exit" in O(N) time for the majority of simulation steps, skipping the second
 * list traversal entirely if the gap is found intact in the expected location.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class GapCandidate
{
    /** The ego vehicle tactical planner. */
    private final MirovaTacticalPlanner vehicle;

    /** The leader vehicle forming the front boundary of the gap. */
    private HeadwayGtu leader;

    /** The follower vehicle forming the rear boundary of the gap. */
    private HeadwayGtu follower;

    /** The persistent ID of the leader vehicle. */
    private final String leaderId;

    /** The persistent ID of the follower vehicle. */
    private final String followerId;

    /** The calculated target acceleration to reach the gap safely. */
    private Acceleration aM;

    /** The last calculated target acceleration (cached). */
    private Acceleration lastComputedAM;

    /** Maximum allowed time horizon for convergence. */
    private static final Duration MAX_CONVERGENCE_TIME = Duration.instantiateSI(10.0);

    /** The intended lateral direction to reach the gap. */
    private final LateralDirectionality gapDirection;

    /**
     * Tracks the relative position of the gap to optimize search order.
     */
    private enum GapLocation
    {
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

    /**
     * Constructs a new GapCandidate.
     *
     * @param leader       the vehicle forming the front boundary
     * @param follower     the vehicle forming the rear boundary
     * @param gapDirection the direction relative to the ego vehicle
     * @param vehicle      the ego vehicle tactical planner
     * @throws IllegalArgumentException if leader or follower are null
     */
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

    /** * Gets the gap leader. * @return the leader */
    public HeadwayGtu getLeader() { return this.leader; }

    /** * Gets the gap follower. * @return the follower */
    public HeadwayGtu getFollower() { return this.follower; }

    /** * Gets the lateral direction of the gap. * @return the direction */
    public LateralDirectionality getGapDirection() { return this.gapDirection; }

    /** * Gets the calculated target acceleration. * @return the acceleration */
    public Acceleration getAM() { return this.aM; }

    /** * Sets the calculated target acceleration. * @param aM the acceleration to set */
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
     * @return {@code true} if the gap is valid and updated; {@code false} otherwise.
     */
    public boolean checkGapStillValid()
    {
        NeighborsContext neighborsContext = this.vehicle.getContext(NeighborsContext.class);
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
     *
     * @param state the result of the search process
     * @return true representing a successful update
     */
    private boolean updateSuccess(final SearchState state)
    {
        this.leader = state.foundLeader;
        this.follower = state.foundFollower;
        this.lastKnownLocation = determineLocation(this.leader, this.follower);
        return true;
    }

    /**
     * Determines the relative location of the gap compared to the ego vehicle.
     *
     * @param l the leader vehicle
     * @param f the follower vehicle
     * @return the determined GapLocation
     */
    private GapLocation determineLocation(final HeadwayGtu l, final HeadwayGtu f)
    {
        // Follower > 0 means Follower is ahead of Ego -> Gap is Downstream
        if (f.getDistance().si >= 0.0) return GapLocation.DOWNSTREAM;
        // Leader < 0 means Leader is behind Ego -> Gap is Upstream
        if (l.getDistance().si <= 0.0) return GapLocation.UPSTREAM;

        return GapLocation.STRADDLE;
    }

    // ----------------------------------------------------------------------
    // Search Helpers
    // ----------------------------------------------------------------------

    /**
     * Scans the provided leaders list for the gap boundaries.
     *
     * @param leaders the iterable of leader vehicles
     * @param state   the mutable search state to update
     */
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

    /**
     * Scans the provided followers list for the gap boundaries.
     *
     * @param followers the iterable of follower vehicles
     * @param state     the mutable search state to update
     */
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
        EgoContext ego = this.vehicle.getContext(EgoContext.class);
        MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
        Parameters params = this.vehicle.getParameters();

        // --- COORDINATE MAPPING (Reference: Ego Front = 0) ---
        Length lM = this.vehicle.getGtu().getLength();

        Length[] positions = getPositions();
        Length xE = positions[0];
        Length xM = positions[1];
        Length xL = positions[2];
        Length xF = positions[3];

        Speed vM = ego.getEgoSpeed();

        // Abort if we have already passed the effective merge point
        if (xE.si <= 0.0) return null;

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

        // 4. Compute Acceleration aM
        Acceleration calculatedAM = computeMergeAcceleration(xE, xM, vM, this.leader, this.follower,
                tauELeader, tauEFollower, dxMin, Tdes, tauLC, ego.getCurrentCarFollowingAcceleration());

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

    /**
     * Computes the key positions in the absolute coordinate system.
     *
     * @return Array of positions: [xE, xM, xL, xF]
     * @throws ParameterException if parameters are missing.
     * @throws GtuException if GTU state cannot be accessed.
     * @throws NetworkException if network topology is inconsistent.
     */
    private Length[] getPositions() throws ParameterException, GtuException, NetworkException {
        InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
        Length emergencyBuffer = this.vehicle.getParameters().getParameter(MirovaParameters.emergencyStoppingDistance);
        Length lM = this.vehicle.getGtu().getLength();
        Length lF = this.follower.getLength();
        Length lL = this.leader.getLength();
        Length xE = infra.getDistanceToLaneEnd().minus(emergencyBuffer);
        Length xM = Length.ZERO;
        Length deltaxL = this.leader.getDistance();
        Length deltaxF = this.follower.getDistance().plus(lM);

        Length xL;
        Length xF;

        switch (this.lastKnownLocation) {
            case DOWNSTREAM:
                xL = Length.instantiateSI(xM.si + deltaxL.si + lL.si/2.0 + lM.si/2.0);
                xF = Length.instantiateSI(xM.si + deltaxF.si + lF.si/2.0 + lM.si/2.0);
                break;
            case UPSTREAM:
                xL = Length.instantiateSI(xM.si - deltaxL.si - lL.si/2.0 - lM.si/2.0);
                xF = Length.instantiateSI(xM.si - deltaxF.si - lF.si/2.0 - lM.si/2.0);
                break;
            case STRADDLE:
                xL = Length.instantiateSI(xM.si + deltaxL.si + lL.si/2.0 + lM.si/2.0);
                xF = Length.instantiateSI(xM.si - deltaxF.si - lF.si/2.0 - lM.si/2.0);
                break;
            default:
                throw new IllegalArgumentException("Unsupported gap location: " + this.lastKnownLocation);
        }

        return new Length[] {xE, xM, xL, xF};
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
        double alpha = xL.si - xE.si - this.leader.getLength().si/2.0 - this.vehicle.getGtu().getLength().si/2.0 - dxMin.si + (Tdes.si * vM.si);

        // Discriminant
        double inside = alpha * alpha + 8.0 * vLsi * Tdes.si * (xE.si - xM.si);
        double sqrtTerm = Math.sqrt(Math.max(0.0, inside));

        double base = -alpha;

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
        Double tauEFollowerSI = (xE.si - xF.si - dxMin.si - this.vehicle.getGtu().getLength().si/2.0 - this.follower.getLength().si/2.0) / vF.si - Tdes.si;
        return Duration.instantiateSI(tauEFollowerSI);
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
     * @param xE           Merge point position
     * @param xM           Merger front position
     * @param vM           Merger speed
     * @param leader       Leader vehicle
     * @param follower     Follower vehicle
     * @param tauELeader   Tau Leader
     * @param tauEFollower Tau Follower
     * @param dxMin        Minimum distance
     * @param Tdes         Desired headway
     * @param tauLC        Lane change duration
     * @param aCF          Current car-following acceleration
     * @return The instantiated acceleration.
     * @throws ParameterException if a parameter lookup fails
     */
    private Acceleration computeMergeAcceleration(final Length xE, final Length xM, final Speed vM, final HeadwayGtu leader, final HeadwayGtu follower,
                                                  final Duration tauELeader, final Duration tauEFollower, final Length dxMin, final Duration Tdes, final Duration tauLC, final Acceleration aCF) throws ParameterException {

        double vF = follower.getSpeed().si;

        // --- Leader Constraint ---
        double tauEL = tauELeader.si;
        double aMDesiredHeadway = 2.0 * (xE.si - vM.si * tauEL - xM.si) / (tauEL * tauEL);

        double deltaXLeader = leader.getDistance().si;
        if (this.leader.isAhead()) {
            deltaXLeader += this.vehicle.getGtu().getLength().si / 2.0 + leader.getLength().si / 2.0;
        } else {
            deltaXLeader = -deltaXLeader - this.vehicle.getGtu().getLength().si / 2.0 - leader.getLength().si / 2.0;
        }

        double tauZero = tauELeader.si - tauLC.si;
        double aMZeroHeadway = ((leader.getSpeed().si - vM.si) * tauZero + (deltaXLeader)) / (0.5 * tauZero * tauZero);

        InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
        double speedLimit = infra.getLegalSpeedLimit().si;
        double aFollowerbound = (speedLimit - vF) / tauZero;

        double aMerger = Math.max(Math.min(Math.min(aMDesiredHeadway, aMZeroHeadway), aFollowerbound), vF/tauZero);

        return Acceleration.instantiateSI(aMerger);
    }

    /**
     * Verifies the kinematic feasibility of the trajectory according to Eq. (26) and (27).
     * <p>
     * This projects the positions of Ego, Leader, and Follower to the time {@code tauPass} (completion of lane change).
     * It checks if the minimum distance {@code s0} is respected relative to both neighbors.
     * </p>
     *
     * @param xE           Merge point position
     * @param xM           Merger front position
     * @param vM           Merger speed
     * @param lM           Merger length
     * @param xL           Leader position
     * @param vL           Leader speed
     * @param xF           Follower position
     * @param vF           Follower speed
     * @param aM           Calculated merge acceleration
     * @param tauELeader   Tau leader
     * @param tauEFollower Tau follower
     * @param tauLC        Lane change duration
     * @param dxMin        Minimum gap
     * @param vTargetLane  Average speed on the target lane
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

        // 3. Projected Leader Position (Rear)
        double xL_end_si = xM.si + xL.si + vL.si * tauPass.si;

        // Check Rear Gap (Follower Constraint)
        boolean rearClear = (xM_end_si - lM.si - xF_end_si - dxMin.si) >= 0.0;

        // Check Front Gap (Leader Constraint)
        boolean frontClear = (xL_end_si - xM_end_si - dxMin.si) >= 0.0;

        return rearClear && frontClear;
    }

    /**
     * Checks if the calculated acceleration is within the physical limits of the vehicle.
     * Uses tolerance to filter out mathematical singularities.
     *
     * @param a      The calculated acceleration
     * @param params The vehicle parameters
     * @return true if realistic, false otherwise
     * @throws ParameterException if parameter evaluation fails
     */
    private boolean isAccelerationRealizable(final Acceleration a, final Parameters params) throws ParameterException {
        double maxAcc = 1.5;
        double maxDec = -6.0;

        // Tolerance of 0.5 m/s^2 for numerical stability
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

        /**
         * Verifies if the search yielded a complete and adjacent gap.
         *
         * @return true if valid, false otherwise
         */
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
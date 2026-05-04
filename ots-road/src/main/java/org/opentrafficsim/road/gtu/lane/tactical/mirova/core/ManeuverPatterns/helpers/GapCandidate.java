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
 * Represents a potential gap on a target lane, defined by a Leader (vehicle ahead) and a Follower (vehicle behind).
 * <p>
 * This class encapsulates gap geometry and solves the <b>Object Identity issue</b> in OTS by re-matching vehicles via
 * persistent IDs. It implements the car-following and gap-selection kinematics proposed by Berghaus and Oeser (2025). It is
 * used as a helper in <b>Layer 4 (Procedure &amp; Action)</b>.
 * </p>
 * <p>
 * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
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
        /** Gap is fully ahead of Ego (Follower distance &gt; 0). Check Leaders list first. */
        DOWNSTREAM,
        /** Gap is fully behind Ego (Leader distance &lt; 0). Check Followers list first. */
        UPSTREAM,
        /** Ego is inside the gap (Follower &lt; 0 &lt; Leader). Must check both lists. */
        STRADDLE
    }

    /** The last confirmed location of this gap. Used as a heuristic for the next tick. */
    private GapLocation lastKnownLocation;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Constructs a new GapCandidate.
     * @param leader the vehicle forming the front boundary
     * @param follower the vehicle forming the rear boundary
     * @param gapDirection the direction relative to the ego vehicle
     * @param vehicle the ego vehicle tactical planner
     * @throws IllegalArgumentException if leader or follower are null
     */
    public GapCandidate(final HeadwayGtu leader, final HeadwayGtu follower, final LateralDirectionality gapDirection,
            final MirovaTacticalPlanner vehicle)
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

        this.lastKnownLocation = determineLocation(leader, follower);
    }

    // ----------------------------------------------------------------------
    // Accessors
    // ----------------------------------------------------------------------

    /**
     * * Gets the gap leader.
     * @return the leader
     */
    public HeadwayGtu getLeader()
    {
        return this.leader;
    }

    /**
     * * Gets the gap follower.
     * @return the follower
     */
    public HeadwayGtu getFollower()
    {
        return this.follower;
    }

    /**
     * * Gets the lateral direction of the gap.
     * @return the direction
     */
    public LateralDirectionality getGapDirection()
    {
        return this.gapDirection;
    }

    /**
     * * Gets the calculated target acceleration.
     * @return the acceleration
     */
    public Acceleration getAM()
    {
        return this.aM;
    }

    /**
     * * Sets the calculated target acceleration.
     * @param aM the acceleration to set
     */
    public void setAM(final Acceleration aM)
    {
        this.aM = aM;
    }

    // ----------------------------------------------------------------------
    // Validity & Consistency Logic
    // ----------------------------------------------------------------------

    /**
     * Checks if the gap still exists and is valid in the current time step.
     * <p>
     * Uses the {@link #lastKnownLocation} to prioritize the search, allowing early exits.
     * </p>
     * @return {@code true} if the gap is valid and updated; {@code false} otherwise
     */
    public boolean checkGapStillValid()
    {
        NeighborsContext neighborsContext = this.vehicle.getContext(NeighborsContext.class);
        SearchState state = new SearchState();

        if (this.lastKnownLocation == GapLocation.DOWNSTREAM)
        {
            scanLeaders(neighborsContext.getLeaders(this.gapDirection), state);
            if (state.isCompleteAndAdjacent())
                return updateSuccess(state);
            scanFollowers(neighborsContext.getFollowers(this.gapDirection), state);
        }
        else if (this.lastKnownLocation == GapLocation.UPSTREAM)
        {
            scanFollowers(neighborsContext.getFollowers(this.gapDirection), state);
            if (state.isCompleteAndAdjacent())
                return updateSuccess(state);
            scanLeaders(neighborsContext.getLeaders(this.gapDirection), state);
        }
        else
        {
            scanLeaders(neighborsContext.getLeaders(this.gapDirection), state);
            scanFollowers(neighborsContext.getFollowers(this.gapDirection), state);
        }

        if (state.isCompleteAndAdjacent())
        {
            return updateSuccess(state);
        }

        if (state.foundLeader != null && state.foundFollower != null && !state.adjacent)
        {
            if (state.leaderIsFirstInLeaders && state.followerIsFirstInFollowers)
            {
                state.adjacent = true;
                return updateSuccess(state);
            }
        }

        return false;
    }

    /**
     * Updates internal state after a successful validation and updates the heuristic location.
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
     * @param l the leader vehicle
     * @param f the follower vehicle
     * @return the determined GapLocation
     */
    private GapLocation determineLocation(final HeadwayGtu l, final HeadwayGtu f)
    {
        if (f.getDistance().si >= 0.0)
            return GapLocation.DOWNSTREAM;
        if (l.getDistance().si <= 0.0)
            return GapLocation.UPSTREAM;
        return GapLocation.STRADDLE;
    }

    // ----------------------------------------------------------------------
    // Search Helpers
    // ----------------------------------------------------------------------

    /**
     * Scans the provided leaders list for the gap boundaries.
     * @param leaders the iterable of leader vehicles
     * @param state the mutable search state to update
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
                if (first)
                    state.leaderIsFirstInLeaders = true;

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
     * @param followers the iterable of follower vehicles
     * @param state the mutable search state to update
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
                if (first)
                    state.followerIsFirstInFollowers = true;

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
     * Recalculates the required acceleration {@code aM} for this gap based on the Berghaus &amp; Oeser (2025) model.
     * @return The calculated acceleration {@code aM}, or {@code null} if the gap has collapsed or is unfeasible.
     * @throws ParameterException if parameters are missing
     * @throws GtuException if GTU state cannot be accessed
     * @throws NetworkException if network topology is inconsistent
     */
    public Acceleration computeCurrentAcceleration() throws ParameterException, GtuException, NetworkException
    {
        if (!checkGapStillValid())
        {
            return null;
        }

        EgoContext ego = this.vehicle.getContext(EgoContext.class);
        MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
        Parameters params = this.vehicle.getParameters();

        Length lM = this.vehicle.getGtu().getLength();
        Length[] positions = getPositions();
        Length xE = positions[0];
        Length xM = positions[1];
        Length xL = positions[2];
        Length xF = positions[3];

        Speed vM = ego.getEgoSpeed();

        // Abort if we have already passed the effective merge point
        if (xE.si <= 0.0)
            return null;

        Duration tauLC = params.getParameter(ParameterTypes.LCDUR);
        Length dxMin = params.getParameter(ParameterTypes.S0);
        Duration Tdes = params.getParameter(ParameterTypes.T);

        Duration tauELeader = computeTauELeader(xL, xE, xM, this.leader.getSpeed(), vM, dxMin, Tdes);
        if (tauELeader.si <= 0.0)
            return null;

        Duration tauEFollower = computeTauEFollower(xE, xF, this.follower.getSpeed(), dxMin, Tdes);
        if (tauEFollower.si <= 0.0)
            return null;

        if (tauELeader.gt(MAX_CONVERGENCE_TIME) || tauEFollower.gt(MAX_CONVERGENCE_TIME))
        {
            return null;
        }

        Acceleration calculatedAM = computeMergeAcceleration(xE, xM, vM, this.leader, xL, tauELeader, dxMin, Tdes, tauLC);

        if (!isAccelerationRealizable(calculatedAM))
        {
            return null;
        }

        Speed vTargetLane = this.gapDirection.isLeft() ? macro.getAverageSpeedLeft() : macro.getAverageSpeedRight();

        boolean feasible = isGapFeasible(xM, vM, lM, xL, this.leader.getSpeed(), xF, this.follower.getSpeed(), calculatedAM,
                tauELeader, tauEFollower, tauLC, dxMin);

        if (feasible)
        {
            this.aM = calculatedAM;
            this.lastComputedAM = calculatedAM;
            return calculatedAM;
        }

        return null;
    }

    /**
     * Computes the key positions in the absolute coordinate system where Ego Center is x = 0.
     * @return Array of positions: [xE, xM, xL, xF]
     * @throws ParameterException if parameters are missing
     * @throws GtuException if GTU state cannot be accessed
     * @throws NetworkException if network topology is inconsistent
     */
    private Length[] getPositions() throws ParameterException, GtuException, NetworkException
    {
        InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
        Length emergencyBuffer = this.vehicle.getParameters().getParameter(MirovaParameters.emergencyStoppingDistance);
        Length lM = this.vehicle.getGtu().getLength();

        // Define Ego Center strictly at x = 0.0
        Length xM = Length.ZERO;

        // Lane end distance in OTS is relative to Ego Front. We shift it to Ego Center.
        Length xE = infra.getDistanceToLaneEnd().minus(emergencyBuffer).plus(lM.divide(2.0));

        Length xL = getCenterPosition(this.leader, lM);
        Length xF = getCenterPosition(this.follower, lM);

        return new Length[] {xE, xM, xL, xF};
    }

    /**
     * Calculates the absolute center position of a given GTU based on its net distance. * @param gtu the GTU to calculate the
     * position for
     * @param egoLength the length of the ego vehicle
     * @return the absolute center position
     */
    private Length getCenterPosition(final HeadwayGtu gtu, final Length egoLength)
    {
        double netDist = gtu.getDistance().si;
        if (netDist >= 0.0)
        {
            return Length.instantiateSI(netDist + egoLength.si / 2.0 + gtu.getLength().si / 2.0);
        }
        else
        {
            return Length.instantiateSI(netDist - egoLength.si / 2.0 - gtu.getLength().si / 2.0);
        }
    }

    /**
     * Computes the time horizon until the leader constraint becomes active.
     * @param xL Leader center position
     * @param xE Merge point position
     * @param xM Merger (Ego) center position
     * @param vL Leader speed
     * @param vM Merger (Ego) speed
     * @param dxMin Minimum distance (s0)
     * @param Tdes Desired time headway (T)
     * @return The time horizon Tau
     */
    private Duration computeTauELeader(final Length xL, final Length xE, final Length xM, final Speed vL, final Speed vM,
            final Length dxMin, final Duration Tdes)
    {
        double vLsi = Math.max(vL.si, 0.1);
        double alpha = xL.si - xE.si - this.leader.getLength().si / 2.0 - this.vehicle.getGtu().getLength().si / 2.0 - dxMin.si
                + (Tdes.si * vM.si);
        double inside = alpha * alpha + 8.0 * vLsi * Tdes.si * (xE.si - xM.si);
        double sqrtTerm = Math.sqrt(Math.max(0.0, inside));
        double base = -alpha;
        double numerator = (base - sqrtTerm > 0.0) ? Math.min(base + sqrtTerm, base - sqrtTerm) : base + sqrtTerm;

        return Duration.instantiateSI(numerator / (2.0 * vLsi));
    }

    /**
     * Computes the time horizon based on the follower approaching from behind.
     * @param xE Merge point position
     * @param xF Follower center position
     * @param vF Follower speed
     * @param dxMin Minimum distance (s0)
     * @param Tdes Desired time headway (T)
     * @return The time horizon Tau
     */
    private Duration computeTauEFollower(final Length xE, final Length xF, final Speed vF, final Length dxMin,
            final Duration Tdes)
    {
        Double tauEFollowerSI =
                (xE.si - xF.si - dxMin.si - this.vehicle.getGtu().getLength().si / 2.0 - this.follower.getLength().si / 2.0)
                        / vF.si - Tdes.si;
        return Duration.instantiateSI(tauEFollowerSI);
    }

    /**
     * Determines the optimal merge acceleration {@code aM}.
     * @param xE Merge point position
     * @param xM Merger center position
     * @param vM Merger speed
     * @param leader Leader vehicle
     * @param xL Leader center position
     * @param tauELeader Tau Leader
     * @param dxMin Minimum distance
     * @param Tdes Desired headway
     * @param tauLC Lane change duration
     * @return The instantiated acceleration
     * @throws ParameterException if a parameter lookup fails
     */
    private Acceleration computeMergeAcceleration(final Length xE, final Length xM, final Speed vM, final HeadwayGtu leader,
            final Length xL, final Duration tauELeader, final Length dxMin, final Duration Tdes, final Duration tauLC)
            throws ParameterException
    {
        double tauEL = tauELeader.si;
        double aMDesiredHeadway = 2.0 * (xE.si - vM.si * tauEL - xM.si) / (tauEL * tauEL);

        double tauZero = Math.max(tauELeader.si - tauLC.si, 0.01); // Protect against <= 0

        // Correct Net Distance Calculation (minus dxMin) preventing collision courses
        double deltaXLeader =
                (xL.si - xM.si) - leader.getLength().si / 2.0 - this.vehicle.getGtu().getLength().si / 2.0 - dxMin.si;

        double aMZeroHeadway = ((leader.getSpeed().si - vM.si) * tauZero + deltaXLeader) / (0.5 * tauZero * tauZero);

        InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
        double speedLimit = infra.getLegalSpeedLimit().si;

        // Corrected Bounds applying to Merger (vM), not Follower (vF)
        double aUpperBound = (speedLimit - vM.si) / tauZero;
        double aLowerBound = -vM.si / tauZero;

        double aMerger = Math.max(Math.min(Math.min(aMDesiredHeadway, aMZeroHeadway), aUpperBound), aLowerBound);

        return Acceleration.instantiateSI(aMerger);
    }

    /**
     * Verifies the kinematic feasibility of the trajectory (Eq. 26 and 27).
     * <p>
     * Modification from Berghaus &amp; Oeser: We do not only check the static minimum distance (s0 / dxMin), but strictly
     * enforce a dynamic safety time headway. If the follower is too fast or too close, the gap is rejected, forcing the
     * tactical planner to target an upstream gap instead.
     * </p>
     * @param xM Merger center position
     * @param vM Merger speed
     * @param lM Merger length
     * @param xL Leader center position
     * @param vL Leader speed
     * @param xF Follower center position
     * @param vF Follower speed
     * @param aM Calculated merge acceleration
     * @param tauELeader Tau leader
     * @param tauEFollower Tau follower
     * @param tauLC Lane change duration
     * @param dxMin Minimum gap (s0)
     * @return {@code true} if the gap remains physically and dynamically viable
     */
    private boolean isGapFeasible(final Length xM, final Speed vM, final Length lM, final Length xL, final Speed vL,
            final Length xF, final Speed vF, final Acceleration aM, final Duration tauELeader, final Duration tauEFollower,
            final Duration tauLC, final Length dxMin)
    {
        Double vGap = (vL.si + vF.si) / 2.0;

        Duration tauPass = (vGap < vM.si) ? Duration.instantiateSI(Math.max(0.0, tauELeader.si - tauLC.si))
                : Duration.instantiateSI(Math.max(0.0, tauEFollower.si - tauLC.si));

        // 1. Projected Positions (Centers) at the end of the maneuver
        double xM_center_end = xM.si + vM.si * tauPass.si + 0.5 * aM.si * tauPass.si * tauPass.si;
        double xF_center_end = xF.si + vF.si * tauPass.si;
        double xL_center_end = xL.si + vL.si * tauPass.si;

        // 2. Define dynamic safety thresholds (e.g., 50% of desired headway as absolute minimum safety buffer)
        // If the gap yields a time headway smaller than this, it is rejected.
        double minSafeTimeHeadway = 0.8; // seconds (adjustable parameter)

        // 3. Check Rear Gap: Ego Rear - Follower Front >= (dxMin + dynamic buffer)
        double egoRear = xM_center_end - lM.si / 2.0;
        double followerFront = xF_center_end + this.follower.getLength().si / 2.0;

        // Add dynamic speed-dependent buffer to the follower constraint
        double requiredRearDistance = dxMin.si + (vF.si * minSafeTimeHeadway);
        boolean rearClear = (egoRear - followerFront - requiredRearDistance) >= 0.0;

        // 4. Check Front Gap: Leader Rear - Ego Front >= (dxMin + dynamic buffer)
        double leaderRear = xL_center_end - this.leader.getLength().si / 2.0;
        double egoFront = xM_center_end + lM.si / 2.0;

        // Add dynamic speed-dependent buffer to the leader constraint
        double requiredFrontDistance = dxMin.si + (vM.si * minSafeTimeHeadway);
        boolean frontClear = (leaderRear - egoFront - requiredFrontDistance) >= 0.0;

        System.out.println("Feasibility Check: GTU " + this.vehicle.getGtu().getId() + " targeting gap [L=" + this.leaderId
                + ", F=" + this.followerId + "] at tauPass=" + String.format("%.2f", tauPass.si) + "s: RearClear=" + rearClear
                + " (Required: " + requiredRearDistance + "m), FrontClear=" + frontClear + " (Required: "
                + requiredFrontDistance + "m)");

        return rearClear && frontClear;
    }

    /**
     * Checks if the calculated acceleration is within the physical limits.
     * @param a The calculated acceleration
     * @return true if realistic, false otherwise
     */
    private boolean isAccelerationRealizable(final Acceleration a)
    {
        double maxAcc = 1.5;
        double maxDec = -6.0;

        if (a.si > maxAcc + 0.5)
            return false;
        if (a.si < maxDec - 0.5)
            return false;

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
        return "GapCandidate[L=" + this.leaderId + ", F=" + this.followerId + ", Loc=" + this.lastKnownLocation + "]";
    }
}

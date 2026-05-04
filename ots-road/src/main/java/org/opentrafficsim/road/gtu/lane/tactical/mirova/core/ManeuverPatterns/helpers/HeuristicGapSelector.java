package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.helpers;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;

/**
 * Heuristics-based gap selector for merging maneuvers.
 * <p>
 * This class breaks away from pure continuous kinematic equations and instead uses a human-like, discrete decision tree. It
 * tests predefined deceleration levels to determine if an upstream merge is feasible before the acceleration lane ends.
 * </p>
 * <p>
 * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class HeuristicGapSelector
{
    /** Discrete deceleration level 1 (gentle braking). */
    private static final Acceleration DECEL_LEVEL_1 = Acceleration.instantiateSI(-1.0);

    /** Discrete deceleration level 2 (moderate braking). */
    private static final Acceleration DECEL_LEVEL_2 = Acceleration.instantiateSI(-2.0);

    /**
     * Heuristically decides whether the ego vehicle should aim to merge AHEAD (downstream) or BEHIND (upstream) the parallel
     * vehicle.
     * @param parallelVehicle the vehicle next to the ego vehicle (putative leader for an upstream merge)
     * @param vEgo the current speed of the ego vehicle
     * @param distToLaneEnd the remaining distance on the acceleration lane
     * @param speedThreshold the significance threshold defining how much faster the ego vehicle must be (e.g., 2.0 m/s)
     * @param distThreshold the minimum required buffer distance to the lane end (e.g., 50.0 m)
     * @return true if a downstream merge is targeted, false for an upstream merge
     */
    public static boolean shouldMergeDownstream(final HeadwayGtu parallelVehicle, final Speed vEgo, final Length distToLaneEnd,
            final Speed speedThreshold, final Length distThreshold)
    {
        if (parallelVehicle == null)
        {
            // No vehicle next to us, aim forward
            return true;
        }

        boolean isSignificantlyFaster = vEgo.si > (parallelVehicle.getSpeed().si + speedThreshold.si);
        boolean hasEnoughDistance = distToLaneEnd.si > distThreshold.si;

        return isSignificantlyFaster && hasEnoughDistance;
    }

    /**
     * Sequentially tests different deceleration levels to find the most comfortable acceleration that allows reaching the
     * upstream gap BEFORE the end of the acceleration lane.
     * @param vEgo current speed of the ego vehicle
     * @param xEgoToLaneEnd remaining distance to the end of the acceleration lane
     * @param parallelVehicle the vehicle to fall behind (putative leader)
     * @param initialNetDistance initial net distance (rear of parallel vehicle to front of ego). Often negative during overlap.
     * @param minSafeGap the target gap size (including safety reduction factors)
     * @param maxEgoDecel the physical maximum deceleration of the ego vehicle (e.g., -4.0 m/s^2)
     * @return the determined acceleration (deceleration) to apply, or null if the maneuver is impossible
     */
    public static Acceleration evaluateUpstreamMerge(final Speed vEgo, final Length xEgoToLaneEnd,
            final HeadwayGtu parallelVehicle, final Length initialNetDistance, final Length minSafeGap,
            final Acceleration maxEgoDecel)
    {
        Speed vLeader = parallelVehicle.getSpeed();

        // Buffer for moderate decelerations (we do not want to use the lane until the very last meter)
        Length bufferModerate = Length.instantiateSI(30.0);

        // Test 1: Very gentle deceleration (-1.0 m/s^2) with a large buffer
        if (canReachUpstreamGap(vEgo, vLeader, xEgoToLaneEnd.minus(bufferModerate), DECEL_LEVEL_1, initialNetDistance,
                minSafeGap))
        {
            return DECEL_LEVEL_1;
        }

        // Test 2: Moderate braking (-2.0 m/s^2) with a slight buffer
        Length bufferMedium = Length.instantiateSI(15.0);
        if (canReachUpstreamGap(vEgo, vLeader, xEgoToLaneEnd.minus(bufferMedium), DECEL_LEVEL_2, initialNetDistance,
                minSafeGap))
        {
            return DECEL_LEVEL_2;
        }

        // Test 3: Hard braking (maxEgoDecel) WITHOUT buffer (last resort)
        if (canReachUpstreamGap(vEgo, vLeader, xEgoToLaneEnd, maxEgoDecel, initialNetDistance, minSafeGap))
        {
            return maxEgoDecel;
        }

        // No scenario is sufficient to fall behind the vehicle before the lane ends
        return null;
    }

    /**
     * Performs the physical calculation: Can the ego vehicle reach the required target gap with the given deceleration BEFORE
     * running out of track?
     * @param vEgo ego vehicle speed
     * @param vLeader leader speed (assumed constant)
     * @param xAvailable available distance for the ego vehicle (lane end minus buffer)
     * @param aEgo the deceleration to test (MUST be negative)
     * @param dx0 initial distance from leader's rear to ego's front
     * @param sMin required minimum safety distance
     * @return true if the gap is reached temporally BEFORE the ego vehicle reaches xAvailable
     * @throws IllegalArgumentException if the tested acceleration is not negative
     */
    private static boolean canReachUpstreamGap(final Speed vEgo, final Speed vLeader, final Length xAvailable,
            final Acceleration aEgo, final Length dx0, final Length sMin)
    {
        if (aEgo.si >= 0.0)
        {
            throw new IllegalArgumentException("Tested acceleration for upstream merge must be negative.");
        }

        // 1. Time until Ego reaches the end of the available track (t_End)
        // Equation of motion: x = v*t + 0.5*a*t^2 => 0.5*a*t^2 + v*t - x = 0
        double discriminantEnd = vEgo.si * vEgo.si + 2.0 * aEgo.si * xAvailable.si;
        Duration tEnd;

        if (discriminantEnd < 0)
        {
            // The vehicle comes to a full stop before reaching the end of the lane.
            tEnd = Duration.POSITIVE_INFINITY;
        }
        else
        {
            // We take the negative root, since 'a' is negative, to find the FIRST (smaller) positive time
            double tEndSi = (-vEgo.si - Math.sqrt(discriminantEnd)) / aEgo.si;
            if (tEndSi < 0)
            {
                return false; // Should not happen physically with positive vEgo and positive xAvailable
            }
            tEnd = Duration.instantiateSI(tEndSi);
        }

        // 2. Time until Ego establishes the required gap to the leader (t_Gap)
        // Leader position (relative): xL(t) = dx0 + vL*t
        // Ego position (relative): xM(t) = vM*t + 0.5*aM*t^2
        // Condition: xL(t) - xM(t) >= sMin
        // 0 = -0.5*aM*t^2 + (vL - vM)*t + (dx0 - sMin)

        double a = -0.5 * aEgo.si; // Positive, since aEgo is negative
        double b = vLeader.si - vEgo.si;
        double c = dx0.si - sMin.si; // Usually negative, as the gap needs to be built up

        double discriminantGap = b * b - 4.0 * a * c;

        if (discriminantGap < 0)
        {
            // The gap will mathematically never be reached (Ego brakes, but Leader is extremely slow or reversing)
            return false;
        }

        // We are looking for the positive point in time when the condition is met
        double tGapSi1 = (-b + Math.sqrt(discriminantGap)) / (2.0 * a);
        double tGapSi2 = (-b - Math.sqrt(discriminantGap)) / (2.0 * a);

        // Take the smallest positive value (when the gap is reached for the very first time)
        double tGapSi = -1.0;
        if (tGapSi1 > 0 && tGapSi2 > 0)
        {
            tGapSi = Math.min(tGapSi1, tGapSi2);
        }
        else if (tGapSi1 > 0)
        {
            tGapSi = tGapSi1;
        }
        else if (tGapSi2 > 0)
        {
            tGapSi = tGapSi2;
        }

        if (tGapSi < 0)
        {
            // We already have the gap right now! (Or reached it in the past)
            return true;
        }

        Duration tGap = Duration.instantiateSI(tGapSi);

        // 3. Final check: Have we established the gap before running out of road?
        return tGap.le(tEnd);
    }

    /**
     * Evaluates whether the ego vehicle can safely merge ahead of a target vehicle (downstream merge) BEFORE the lane ends,
     * simply by maintaining its CURRENT acceleration.
     * @param vEgo current speed of the ego vehicle
     * @param aEgoCurrent current acceleration of the ego vehicle
     * @param xEgoToLaneEnd remaining distance to the end of the acceleration lane
     * @param parallelVehicle the vehicle to overtake (putative follower)
     * @param initialNetDistance initial net distance (ego front to follower front, usually negative)
     * @param egoLength length of the ego vehicle
     * @param minSafeGap the required gap behind the ego vehicle (safety distance)
     * @return the current acceleration if the maneuver is feasible, or null if it is impossible
     */
    public static Acceleration evaluateDownstreamMerge(final Speed vEgo, final Acceleration aEgoCurrent,
            final Length xEgoToLaneEnd, final HeadwayGtu parallelVehicle, final Length initialNetDistance,
            final Length egoLength, final Length minSafeGap)
    {
        Speed vFollower = parallelVehicle.getSpeed();

        // We test exactly one scenario: maintaining the current acceleration
        if (canReachDownstreamGap(vEgo, vFollower, xEgoToLaneEnd, aEgoCurrent, initialNetDistance, egoLength, minSafeGap))
        {
            return aEgoCurrent;
        }

        return null;
    }

    /**
     * Core kinematic check for downstream merging. Robustly handles constant speeds (zero acceleration) and deceleration to
     * prevent mathematical errors.
     * @param vEgo ego speed
     * @param vFollower follower speed
     * @param xAvailable distance to lane end
     * @param aEgo tested current acceleration
     * @param dx0 initial distance (ego front to follower front)
     * @param lEgo length of ego
     * @param sMin required gap behind ego
     * @return true if safe gap is reached before lane end
     */
    private static boolean canReachDownstreamGap(final Speed vEgo, final Speed vFollower, final Length xAvailable,
            final Acceleration aEgo, final Length dx0, final Length lEgo, final Length sMin)
    {
        double vM = vEgo.si;
        double aM = aEgo.si;
        double xAvail = xAvailable.si;
        double vF = vFollower.si;

        // 1. Time to reach lane end (tEnd)
        double tEnd = Double.POSITIVE_INFINITY;
        if (Math.abs(aM) < 1E-6)
        {
            // Linear motion (a = 0)
            if (vM > 0.0)
            {
                tEnd = xAvail / vM;
            }
        }
        else
        {
            // Quadratic motion: 0.5*a*t^2 + v*t - x = 0
            double discriminantEnd = vM * vM + 2.0 * aM * xAvail;
            if (discriminantEnd >= 0.0)
            {
                double t1 = (-vM + Math.sqrt(discriminantEnd)) / aM;
                double t2 = (-vM - Math.sqrt(discriminantEnd)) / aM;
                tEnd = getSmallestPositive(t1, t2);
            }
        }

        // If the vehicle never reaches the lane end (or stops before it), the maneuver fails
        if (Double.isInfinite(tEnd))
        {
            return false;
        }

        // 2. Time to reach safe gap (tGap)
        // Goal: Ego Rear - Follower Front >= sMin
        // 0.5*aM*t^2 + (vM - vF)*t - (dx0 + lEgo + sMin) = 0
        double a = 0.5 * aM;
        double b = vM - vF;
        double c = -(dx0.si + lEgo.si + sMin.si);

        // Quick check: If the gap is ALREADY large enough right now (t=0)
        if (c >= 0.0)
        {
            return true;
        }

        double tGap = -1.0;

        if (Math.abs(a) < 1E-6)
        {
            // Linear relative motion
            if (b > 0.0)
            {
                tGap = -c / b;
            }
        }
        else
        {
            // Quadratic relative motion
            double discGap = b * b - 4.0 * a * c;
            if (discGap >= 0.0)
            {
                double t1 = (-b + Math.sqrt(discGap)) / (2.0 * a);
                double t2 = (-b - Math.sqrt(discGap)) / (2.0 * a);
                tGap = getSmallestPositive(t1, t2);
            }
        }

        // 3. Final Evaluation
        // We must reach the gap in the future (tGap > 0) AND before we run out of road (tGap <= tEnd)
        return tGap > 0.0 && tGap <= tEnd;
    }

    /**
     * Helper method to find the smallest positive root of a quadratic equation.
     * @param t1 first root
     * @param t2 second root
     * @return the smallest positive value, or POSITIVE_INFINITY if neither is positive
     */
    private static double getSmallestPositive(final double t1, final double t2)
    {
        if (t1 > 0.0 && t2 > 0.0)
        {
            return Math.min(t1, t2);
        }
        if (t1 > 0.0)
        {
            return t1;
        }
        if (t2 > 0.0)
        {
            return t2;
        }
        return Double.POSITIVE_INFINITY;
    }
}

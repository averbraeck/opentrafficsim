package org.opentrafficsim.road.gtu.lane.tactical.mirova.following;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.RelaxationState;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Utility class for MiRoVA specific car-following calculations.
 * <p>
 * This class acts as a transparent wrapper around the standard OTS {@code CarFollowingUtil}. It automatically injects the Keane
 * and Gao (2021) 2-parameter relaxation buffers for specific leader GTUs. Additionally, it simplifies the method signatures for
 * MiRoVA by extracting repetitive parameters (model, speed, limits) directly from the tactical planner.
 * </p>
 * <p>
 * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public final class MirovaCarFollowingUtil
{
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MirovaCarFollowingUtil()
    {
        // Utility class
    }

    /*
     * ========================================================================================= 1) RELAXED METHODS (Applicable
     * for physical GTUs with IDs) =========================================================================================
     */

    /**
     * Calculates the acceleration towards a single leader, transparently applying ID-based relaxation.
     * <p>
     * This method utilizes a single-tick cache stored within the {@code EgoContext} to prevent redundant car-following
     * evaluations of the same leader GTU within the same simulation step.
     * </p>
     * @param vehicle the tactical planner of the ego vehicle containing all contexts
     * @param leader the actual, unmanipulated perception of the leader GTU
     * @return the acceleration calculated by the car-following model
     * @throws ParameterException if a required parameter is missing
     * @throws GtuException if GTU state cannot be accessed
     */
    public static Acceleration followSingleLeader(final MirovaTacticalPlanner vehicle, final HeadwayGtu leader)
            throws ParameterException, GtuException
    {
        if (leader == null)
        {
            return freeAcceleration(vehicle);
        }

        EgoContext ego = vehicle.getContext(EgoContext.class);
        String leaderId = leader.getId();

        // 1. Check Cache (Early Exit for Performance)
        if (leaderId != null)
        {
            Acceleration cachedAcc = ego.getCachedAcceleration(leaderId);
            if (cachedAcc != null)
            {
                return cachedAcc; // Spart den kompletten Berechnungsbaum!
            }
        }

        Duration now = vehicle.getGtu().getSimulator().getSimulatorTime();
        Length perceivedDistance = leader.getDistance();
        Speed perceivedLeaderSpeed = leader.getSpeed();

        Acceleration aSafe =
                getKinematicEmergencyBrake(vehicle, leader.getSpeed(), leader.getDistance(), leader.getAcceleration());

        // 2. Check for and apply ID-based relaxation buffers
        RelaxationState activeRelaxation = ego.getActiveRelaxationForLeader(leaderId);
        if (activeRelaxation != null)
        {
            perceivedDistance = perceivedDistance.plus(activeRelaxation.getVirtualSpaceBuffer(now));
            perceivedLeaderSpeed = perceivedLeaderSpeed.plus(activeRelaxation.getVirtualSpeedBuffer(now));

        }

        // 3. Perform the heavy physical calculation
        Acceleration result = CarFollowingUtil.followSingleLeader(vehicle.getCarFollowingModel(), vehicle.getParameters(),
                ego.getEgoSpeed(), vehicle.getContext(InfrastructureContext.class).getCurrentSpeedLimit(), perceivedDistance,
                perceivedLeaderSpeed);

        // System.out.println("DEBUG: GTU " + vehicle.getGtu().getId() + " is Following leader " + leaderId
        // + " with perceived distance " + perceivedDistance + " and speed " + perceivedLeaderSpeed
        // + " while actual distance is " + leader.getDistance() + " and speed is " + leader.getSpeed()
        // + ". Calculated acceleration: " + result + ". Active relaxation: " + (activeRelaxation != null));

        // 4. Store the result in the cache for subsequent calls in this tick
        if (leaderId != null)
        {
            ego.cacheAcceleration(leaderId, result);
        }

        // if (result.lt(Acceleration.instantiateSI(-8.0)))
        // {
        // System.out.println("WARNING: Unusually strong deceleration calculated for GTU " + vehicle.getGtu().getId()
        // + " following leader " + leaderId + " with perceived distance " + perceivedDistance + " and speed "
        // + perceivedLeaderSpeed + ". Actual distance is " + leader.getDistance() + " and speed is "
        // + leader.getSpeed() + ". Calculated acceleration: " + result + ". Active relaxation: "
        // + (activeRelaxation != null));
        // }

        return result;

    }

    /**
     * Calculates the most restrictive acceleration towards a set of multiple leaders, evaluating ID-based relaxation for each
     * leader individually.
     * @param vehicle the tactical planner of the ego vehicle containing all contexts
     * @param leaders the iterable collection of leaders (e.g., from multiple lanes)
     * @return the most restrictive (minimum) acceleration among all evaluated leaders
     * @throws ParameterException if a required parameter is missing
     * @throws GtuException if GTU state cannot be accessed
     */
    public static Acceleration followMultipleLeaders(final MirovaTacticalPlanner vehicle, final Iterable<HeadwayGtu> leaders)
            throws ParameterException, GtuException
    {
        Acceleration minAcceleration = null;

        for (HeadwayGtu leader : leaders)
        {
            Acceleration acc = followSingleLeader(vehicle, leader);

            if (minAcceleration == null || acc.lt(minAcceleration))
            {
                minAcceleration = acc;
            }
        }

        if (minAcceleration == null)
        {
            return freeAcceleration(vehicle);
        }

        return minAcceleration;
    }

    /**
     * Calculates a strictly kinematic emergency deceleration if the gap is closing too fast.
     * <p>
     * Forms a central safety layer in the MiRoVA Car-Following logic. This bypasses any relaxed parameters of the psychological
     * Car-Following models to prevent crashes, especially during the relaxation phase or in case of unexpected hard braking of
     * a leading vehicle.
     * </p>
     * <p>
     * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
     * @param vehicle the tactical planner of the ego vehicle
     * @param leaderSpeed Speed; the speed of the leading vehicle
     * @param gap Length; the net distance to the leading vehicle
     * @param leaderAcceleration Acceleration; the current acceleration of the leader (can be null if unknown)
     * @return Acceleration; the required emergency acceleration (or POSITIVE_INFINITY if safe)
     * @throws ParameterException
     */
    public static Acceleration getKinematicEmergencyBrake(final MirovaTacticalPlanner vehicle, final Speed leaderSpeed,
            final Length gap, final Acceleration leaderAcceleration) throws ParameterException
    {
        Speed egoSpeed = vehicle.getContext(EgoContext.class).getEgoSpeed();
        if (gap == null || gap.si <= 0.0)
        {
            return Acceleration.instantiateSI(-10.0); // Crash imminent or already happened
        }

        double aSafe = Double.POSITIVE_INFINITY;

        // 1. Kinematic protection: Time-To-Collision (TTC) Check
        if (egoSpeed.si > leaderSpeed.si)
        {
            double closingSpeed = egoSpeed.si - leaderSpeed.si;
            double ttc = gap.si / closingSpeed;

            // If TTC drops below 2.0 seconds, enforce physical braking limit
            if (ttc < vehicle.getParameters().getParameter(MirovaParameters.ttc_emergency_braking).si)
            {
                double safeGap = vehicle.getParameters().getParameter(ParameterTypes.S0).si; // Minimum desired gap at
                                                                                             // standstill
                double distanceToDecel = Math.max(0.1, gap.si - safeGap);

                // Physics: a = -(dv^2) / (2 * ds)
                aSafe = -(closingSpeed * closingSpeed) / (2.0 * distanceToDecel);
            }
        }

        // 2. Reactive protection: Take over hard braking maneuvers from the leader
        if (leaderAcceleration != null && leaderAcceleration.si < -3.0)
        {
            aSafe = Math.min(aSafe, leaderAcceleration.si);
        }

        return Acceleration.instantiateSI(aSafe);
    }

    /*
     * ========================================================================================= 2) STANDARD METHODS (No
     * Relaxation - Target is virtual or lacks an ID)
     * =========================================================================================
     */

    /**
     * Follows an arbitrary distance and speed without a specific GTU ID.
     * <p>
     * Note: No relaxation is applied here, as relaxation requires tracking a specific vehicle ID over time.
     * </p>
     * @param vehicle the tactical planner of the ego vehicle
     * @param distance the distance to follow
     * @param leaderSpeed the speed to follow
     * @return acceleration for following the virtual leader
     * @throws ParameterException if a parameter is missing
     * @throws GtuException if GTU state cannot be accessed
     */
    public static Acceleration followDistanceAndSpeed(final MirovaTacticalPlanner vehicle, final Length distance,
            final Speed leaderSpeed) throws ParameterException, GtuException
    {
        return CarFollowingUtil.followSingleLeader(vehicle.getCarFollowingModel(), vehicle.getParameters(),
                vehicle.getContext(EgoContext.class).getEgoSpeed(),
                vehicle.getContext(InfrastructureContext.class).getCurrentSpeedLimit(), distance, leaderSpeed);
    }

    /**
     * Stop within given distance.
     * <p>
     * Note: Stopping is usually done for static infrastructure (e.g., traffic lights). Therefore, the Keane and Gao relaxation
     * phenomenon is physically not applicable here.
     * </p>
     * @param vehicle the tactical planner of the ego vehicle
     * @param distance distance to stop over
     * @return acceleration to stop over distance
     * @throws ParameterException if a parameter is missing
     * @throws GtuException if GTU state cannot be accessed
     */
    public static Acceleration stop(final MirovaTacticalPlanner vehicle, final Length distance)
            throws ParameterException, GtuException
    {
        return CarFollowingUtil.stop(vehicle.getCarFollowingModel(), vehicle.getParameters(),
                vehicle.getContext(EgoContext.class).getEgoSpeed(),
                vehicle.getContext(InfrastructureContext.class).getCurrentSpeedLimit(), distance);
    }

    /**
     * Return constant acceleration in order to stop in specified distance.
     * @param vehicle the tactical planner of the ego vehicle
     * @param distance distance to stop over
     * @return constant acceleration in order to stop in specified distance
     * @throws ParameterException on missing parameter
     * @throws GtuException if GTU state cannot be accessed
     */
    public static Acceleration constantAccelerationStop(final MirovaTacticalPlanner vehicle, final Length distance)
            throws ParameterException, GtuException
    {
        return CarFollowingUtil.constantAccelerationStop(vehicle.getCarFollowingModel(), vehicle.getParameters(),
                vehicle.getContext(EgoContext.class).getEgoSpeed(), distance);
    }

    /**
     * Calculate free acceleration.
     * @param vehicle the tactical planner of the ego vehicle
     * @return acceleration free acceleration
     * @throws ParameterException if a parameter is missing
     * @throws GtuException if GTU state cannot be accessed
     */
    public static Acceleration freeAcceleration(final MirovaTacticalPlanner vehicle) throws ParameterException, GtuException
    {
        return CarFollowingUtil.freeAcceleration(vehicle.getCarFollowingModel(), vehicle.getParameters(),
                vehicle.getContext(EgoContext.class).getEgoSpeed(),
                vehicle.getContext(InfrastructureContext.class).getCurrentSpeedLimit());
    }

    /**
     * Returns an acceleration based on the car-following model in order to adjust the speed to a given value at some location
     * ahead (e.g., for early anticipation).
     * <p>
     * Note: This method operates on a virtual stationary vehicle internally. Relaxation cannot and should not be applied here.
     * </p>
     * @param vehicle the tactical planner of the ego vehicle
     * @param distance distance to the location of the target speed
     * @param targetSpeed target speed
     * @return acceleration based on the car-following model in order to adjust the speed
     * @throws ParameterException if parameter exception occurs
     * @throws GtuException if GTU state cannot be accessed
     */
    public static Acceleration approachTargetSpeed(final MirovaTacticalPlanner vehicle, final Length distance,
            final Speed targetSpeed) throws ParameterException, GtuException
    {
        return CarFollowingUtil.approachTargetSpeed(vehicle.getCarFollowingModel(), vehicle.getParameters(),
                vehicle.getContext(EgoContext.class).getEgoSpeed(),
                vehicle.getContext(InfrastructureContext.class).getCurrentSpeedLimit(), distance, targetSpeed);
    }
}

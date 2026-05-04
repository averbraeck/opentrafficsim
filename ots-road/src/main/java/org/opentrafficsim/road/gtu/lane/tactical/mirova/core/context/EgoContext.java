package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.following.DynamicHeadwayProvider;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;

/**
 * Context category representing ego-vehicle-related state variables.
 * <p>
 * Forms a central part of <b>Layer 1 (Perception & Context)</b> in the MiRoVA architecture. Provides direct access to low-level
 * vehicle states such as speed, accelerations, and deceleration thresholds, which are frequently required by tactical and
 * longitudinal control logic.
 * </p>
 * <p>
 * The values are lazily updated once per simulation tick and cached within the {@link VehicleContextManager} to optimize
 * performance and ensure intra-tick consistency. Furthermore, it tracks ID-based relaxation states for specific leader vehicles
 * and provides a highly efficient single-tick cache for longitudinal acceleration evaluations.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class EgoContext extends ContextCategory implements UpdatableContext
{

    /** Cache key for ego speed. */
    public static final String EGO_SPEED = "egoSpeed";

    /** Cache key for current car-following acceleration. */
    public static final String CURRENT_CF_ACCELERATION = "currentCarFollowingAcceleration";

    /** Cache key for current desired speed. */
    public static final String CURRENT_DESIRED_SPEED = "currentDesiredSpeed";

    /** Cache key for desired front headway in current lane. */
    public static final String DESIRED_FRONT_HEADWAY_CURRENT = "desiredFrontHeadwayCurrent";

    /** Cache key for desired front headway in left lane. */
    public static final String DESIRED_FRONT_HEADWAY_LEFT = "desiredFrontHeadwayLeft";

    /** Cache key for desired front headway in right lane. */
    public static final String DESIRED_FRONT_HEADWAY_RIGHT = "desiredFrontHeadwayRight";

    /** Cache key for desired rear headway in left lane. */
    public static final String DESIRED_REAR_HEADWAY_LEFT = "desiredRearHeadwayLeft";

    /** Cache key for desired rear headway in right lane. */
    public static final String DESIRED_REAR_HEADWAY_RIGHT = "desiredRearHeadwayRight";

    /** Cache key for desired rear headway in current lane. */
    public static final String DESIRED_REAR_HEADWAY_CURRENT = "desiredRearHeadwayCurrent";

    /** Cache key for ego deceleration threshold (left). */
    public static final String EGO_DECELERATION_THRESHOLD_LEFT = "egoDecelerationThresholdLeft";

    /** Cache key for ego deceleration threshold (right). */
    public static final String EGO_DECELERATION_THRESHOLD_RIGHT = "egoDecelerationThresholdRight";

    /** Cache key for follower deceleration threshold (left). */
    public static final String FOLLOWER_DECELERATION_THRESHOLD_LEFT = "followerDecelerationThresholdLeft";

    /** Cache key for follower deceleration threshold (right). */
    public static final String FOLLOWER_DECELERATION_THRESHOLD_RIGHT = "followerDecelerationThresholdRight";

    /** Cache key for maximum physical acceleration. */
    public static final String MAX_PHYSICAL_ACCELERATION = "maxPhysicalAcceleration";

    // =========================================================================================
    // FIELDS: RELAXATION & CACHING
    // =========================================================================================

    /**
     * * Map of active relaxation states, tracked by the GTU ID of the respective leader. Handles the Keane and Gao (2021)
     * 2-parameter relaxation phenomenon.
     */
    private final Map<String, RelaxationState> activeRelaxations = new HashMap<>();

    /**
     * * Temporary cache for longitudinal accelerations evaluated during the current time step. Key is the GTU ID of the leader.
     * This cache is cleared at the start of every perception update.
     */
    private final Map<String, Acceleration> tickAccelerationCache = new HashMap<>();

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Constructs a new {@code EgoContext}.
     * @param vehicle the ego vehicle associated with this context
     */
    public EgoContext(final MirovaTacticalPlanner vehicle)
    {
        super("Ego", vehicle);
    }

    // =========================================================================================
    // METHODS: SINGLE-TICK ACCELERATION CACHE
    // =========================================================================================

    /**
     * Retrieves a cached acceleration for a specific leader ID evaluated in the current tick.
     * @param leaderId the GTU ID of the leader
     * @return the cached acceleration, or null if not yet evaluated in this tick
     */
    public Acceleration getCachedAcceleration(final String leaderId)
    {
        return this.tickAccelerationCache.get(leaderId);
    }

    /**
     * Caches a computed acceleration for a specific leader ID for the duration of the current tick.
     * @param leaderId the GTU ID of the leader
     * @param acceleration the computed acceleration
     */
    public void cacheAcceleration(final String leaderId, final Acceleration acceleration)
    {
        if (leaderId != null && acceleration != null)
        {
            this.tickAccelerationCache.put(leaderId, acceleration);
        }
    }

    /**
     * Provides access to the entire tick acceleration cache, which can be useful for debugging or advanced maneuver logic that
     * needs to
     * @return the current tick acceleration cache map
     */
    public Map<String, Acceleration> getCurrentTickAccelerationCache()
    {
        return this.tickAccelerationCache;
    }

    /**
     * Returns the map of active relaxation states for all leaders. This can be used by maneuver patterns or tactical logic to
     * @return the map of active relaxations, keyed by leader GTU ID
     */
    public Map<String, RelaxationState> getActiveRelaxations()
    {
        return this.activeRelaxations;
    }

    // =========================================================================================
    // METHODS: RELAXATION MANAGEMENT
    // =========================================================================================

    /**
     * Evaluates a new cut-in situation and triggers the 2-parameter relaxation if the new leader violates the dynamic desired
     * headway or has a significant speed difference.
     * <p>
     * This method is typically called by the {@code NeighborsContext} when a change in the leader ID is detected (edge
     * trigger). It leverages the {@link DynamicHeadwayProvider} to accurately assess the required spatial gap.
     * </p>
     * @param newLeader HeadwayGtu; the new headway object that just cut in
     * @param oldLeaderSpeed Speed; the speed of the previous leader at the time of the cut-in (can be null)
     * @throws ParameterException if a required parameter is missing
     * @throws GtuException if GTU state cannot be accessed
     */
    public void evaluateAndTriggerRelaxation(final HeadwayGtu newLeader, final Speed oldLeaderSpeed)
            throws ParameterException, GtuException
    {
        if (newLeader == null)
        {
            return;
        }

        Parameters params = this.vehicle.getParameters();
        CarFollowingModel cfModel = this.vehicle.getCarFollowingModel();
        Speed egoSpeed = this.getEgoSpeed();

        // 1. Compute static equilibrium headway
        Length targetHeadway = cfModel.desiredHeadway(params, egoSpeed);

        // 3. Calculate spatial deficit (gamma_s)
        Length gammaS = Length.ZERO;
        if (newLeader.getDistance().lt(targetHeadway))
        {
            gammaS = targetHeadway.minus(newLeader.getDistance());
        }

        // 4. Calculate speed deficit (gamma_v)
        Speed gammaV = oldLeaderSpeed != null ? oldLeaderSpeed.minus(newLeader.getSpeed()) : Speed.ZERO;

        // 5. Trigger relaxation if there is ANY deficit (space OR speed)
        if (gammaS.si > 0.0 || gammaV.si > 0.0)
        {
            Duration tauSpace = params.getParameter(MirovaParameters.RELAXATION_TAU_SPACE);
            Duration tauSpeed = params.getParameter(MirovaParameters.RELAXATION_TAU_SPEED);

            triggerRelaxation(newLeader.getId(), gammaS, gammaV, tauSpace, tauSpeed, false);
        }
    }

    /**
     * Explicitly registers a relaxation state for a specific target vehicle without overwriting an active state.
     * <p>
     * This is a legacy/convenience wrapper that defaults to {@code forceOverwrite = false}.
     * </p>
     * @param leaderId String; the ID of the target leader GTU
     * @param initialSpaceDeficit Length; the initial space headway deficit [m]
     * @param initialSpeedDeficit Speed; the speed difference (oldLeaderSpeed - newLeaderSpeed) [m/s]
     * @param tauSpace Duration; the spatial relaxation time constant [s]
     * @param tauSpeed Duration; the speed relaxation time constant [s]
     */
    public void triggerRelaxation(final String leaderId, final Length initialSpaceDeficit, final Speed initialSpeedDeficit,
            final Duration tauSpace, final Duration tauSpeed)
    {
        triggerRelaxation(leaderId, initialSpaceDeficit, initialSpeedDeficit, tauSpace, tauSpeed, false);
    }

    /**
     * Explicitly registers or updates a relaxation state for a specific target vehicle.
     * <p>
     * If {@code forceOverwrite} is true, an ongoing relaxation is reset. This freezes the buffer at 100% while the maneuver is
     * being prepared but not yet physically executed.
     * </p>
     * @param leaderId String; the ID of the target leader GTU
     * @param initialSpaceDeficit Length; the initial space headway deficit [m]
     * @param initialSpeedDeficit Speed; the speed difference (oldLeaderSpeed - newLeaderSpeed) [m/s]
     * @param tauSpace Duration; the spatial relaxation time constant [s]
     * @param tauSpeed Duration; the speed relaxation time constant [s]
     * @param forceOverwrite boolean; if true, any active relaxation state for this leader is overwritten
     */
    public void triggerRelaxation(final String leaderId, final Length initialSpaceDeficit, final Speed initialSpeedDeficit,
            final Duration tauSpace, final Duration tauSpeed, final boolean forceOverwrite)
    {
        if (forceOverwrite || !this.activeRelaxations.containsKey(leaderId))
        {
            // Verify there is actually a deficit to relax
            if ((initialSpaceDeficit != null && initialSpaceDeficit.si > 0.0)
                    || (initialSpeedDeficit != null && initialSpeedDeficit.si > 0.0))
            {
                Duration now = this.vehicle.getGtu().getSimulator().getSimulatorTime();
                this.activeRelaxations.put(leaderId,
                        new RelaxationState(now, initialSpaceDeficit, initialSpeedDeficit, tauSpace, tauSpeed));

                // ARCHITECTURE-UPDATE: Targeted cache invalidation ensures the IDM immediately recalculates
                this.tickAccelerationCache.remove(leaderId);
            }
        }
    }

    /**
     * Proactively calculates deficits and triggers relaxation for a specific target leader.
     * <p>
     * This method is designed for maneuver patterns to safely accept gaps on adjacent lanes. It leverages the
     * {@link DynamicHeadwayProvider} and deliberately <b>overwrites</b> existing states to keep the buffer fresh while waiting
     * for the physical lane change to start.
     * </p>
     * @param targetLeader HeadwayGtu; the target leader GTU to relax towards
     * @throws ParameterException if required relaxation parameters are missing
     */
    public void triggerRelaxation(final HeadwayGtu targetLeader) throws ParameterException
    {
        // NOTE: We do NOT check !this.activeRelaxations.containsKey anymore, because we want to overwrite!
        if (targetLeader == null)
        {
            return;
        }

        Parameters params = this.vehicle.getParameters();
        CarFollowingModel cfModel = this.vehicle.getCarFollowingModel();
        Speed egoSpeed = this.getEgoSpeed();

        Length targetHeadway = cfModel.desiredHeadway(params, egoSpeed);

        Length spaceDeficit = Length.ZERO;
        if (targetLeader.getDistance().lt(targetHeadway))
        {
            spaceDeficit = targetHeadway.minus(targetLeader.getDistance());
        }

        // For proactive lane changes, speed deficit is Ego Speed minus Target Leader Speed
        Speed speedDeficit = egoSpeed.minus(targetLeader.getSpeed());

        if (spaceDeficit.si > 0.0 || speedDeficit.si > 0.0)
        {
            Duration tauSpace = params.getParameter(MirovaParameters.RELAXATION_TAU_SPACE);
            Duration tauSpeed = params.getParameter(MirovaParameters.RELAXATION_TAU_SPEED);

            // Force overwrite = true! Buffer will not decay until the trigger stops (i.e. physical LC starts).
            triggerRelaxation(targetLeader.getId(), spaceDeficit, speedDeficit, tauSpace, tauSpeed, false);
        }
    }

    /**
     * Retrieves the active relaxation state for a specific leader.
     * @param leaderId String; the ID of the leader GTU
     * @return RelaxationState; the active relaxation state, or null if no relaxation is active for this leader
     */
    public RelaxationState getActiveRelaxationForLeader(final String leaderId)
    {
        return this.activeRelaxations.get(leaderId);
    }
    // ----------------------------------------------------------------------
    // Lazy Accessors
    // ----------------------------------------------------------------------

    /**
     * Returns the current ego-vehicle speed as perceived in the last simulation tick.
     * <p>
     * This method uses lazy evaluation: the speed is only retrieved once per tick from {@link EgoPerception} and then cached.
     * </p>
     * @return current ego speed
     */
    public Speed getEgoSpeed()
    {
        Speed cached = getCachedValue(EGO_SPEED, Speed.class);
        if (cached != null)
        {
            return cached;
        }

        Speed result = computeEgoSpeed();
        cacheValue(EGO_SPEED, result, true);
        return result;
    }

    /**
     * Returns the current baseline car-following acceleration of the ego vehicle. Uses lazy evaluation to cache the result per
     * tick.
     * @return current car-following acceleration
     * @throws ParameterException if a required parameter is missing
     * @throws GtuException if GTU state cannot be accessed
     * @throws NetworkException if network state cannot be accessed
     */
    public Acceleration getCurrentCarFollowingAcceleration() throws ParameterException, GtuException, NetworkException
    {
        Acceleration cached = getCachedValue(CURRENT_CF_ACCELERATION, Acceleration.class);
        if (cached != null)
        {
            return cached;
        }

        Acceleration result = this.vehicle.computeLongitudinalAcceleration();
        cacheValue(CURRENT_CF_ACCELERATION, result, true);
        return result;
    }

    /**
     * Returns the currently desired speed of the ego vehicle. Uses lazy evaluation to cache the result per tick.
     * @return the current desired speed
     * @throws ParameterException if parameter resolution fails
     * @throws GtuException if GTU state is invalid
     * @throws NetworkException if network state is invalid
     */
    public Speed getCurrentDesiredSpeed() throws ParameterException, GtuException, NetworkException
    {
        Speed cached = getCachedValue(CURRENT_DESIRED_SPEED, Speed.class);
        if (cached != null)
        {
            return cached;
        }
        Speed result = this.vehicle.getGtu().getDesiredSpeed();
        cacheValue(CURRENT_DESIRED_SPEED, result, true);
        return result;
    }

    /**
     * Computes and returns the deceleration threshold for the ego vehicle for a specific lane change direction.
     * @param dir the lateral direction to consider (LEFT or RIGHT)
     * @return the acceptable deceleration threshold for the ego vehicle
     * @throws ParameterException if threshold parameters are missing
     */
    public Acceleration getEgoDecelerationThreshold(final LateralDirectionality dir) throws ParameterException
    {
        String key = (dir == LateralDirectionality.LEFT) ? EGO_DECELERATION_THRESHOLD_LEFT : EGO_DECELERATION_THRESHOLD_RIGHT;

        Acceleration cached = getCachedValue(key, Acceleration.class);
        if (cached != null)
        {
            return cached;
        }

        Acceleration result = computeEgoDecelerationThreshold(dir);
        cacheValue(key, result, true);
        return result;
    }

    /**
     * Computes and returns the expected deceleration threshold for the follower in a target lane.
     * @param dir the lateral direction to consider (LEFT or RIGHT)
     * @return the expected deceleration threshold for the follower
     * @throws ParameterException if threshold parameters are missing
     */
    public Acceleration getFollowerDecelerationThreshold(final LateralDirectionality dir) throws ParameterException
    {
        String key = (dir == LateralDirectionality.LEFT) ? FOLLOWER_DECELERATION_THRESHOLD_LEFT
                : FOLLOWER_DECELERATION_THRESHOLD_RIGHT;

        Acceleration cached = getCachedValue(key, Acceleration.class);
        if (cached != null)
        {
            return cached;
        }

        Acceleration result = computeFollowerDecelerationThreshold(dir);
        cacheValue(key, result, true);
        return result;
    }

    /**
     * Calculates the desired front headway distance for a given direction.
     * @param dir the lateral direction (NONE for current lane)
     * @return the desired front headway distance
     */
    public Length getDesiredFrontHeadway(final LateralDirectionality dir)
    {
        String key;
        if (dir == LateralDirectionality.LEFT)
        {
            key = DESIRED_FRONT_HEADWAY_LEFT;
        }
        else if (dir == LateralDirectionality.RIGHT)
        {
            key = DESIRED_FRONT_HEADWAY_RIGHT;
        }
        else
        {
            key = DESIRED_FRONT_HEADWAY_CURRENT;
        }

        Length cached = getCachedValue(key, Length.class);
        if (cached != null)
        {
            return cached;
        }

        Length result = computeDesiredFrontHeadway();
        cacheValue(key, result, true);
        return result;
    }

    /**
     * Calculates the desired rear headway distance for a given direction based on the follower.
     * @param dir the lateral direction (NONE for current lane)
     * @return the desired rear headway distance
     */
    public Length getDesiredRearHeadway(final LateralDirectionality dir)
    {
        String key;
        if (dir == LateralDirectionality.LEFT)
        {
            key = DESIRED_REAR_HEADWAY_LEFT;
        }
        else if (dir == LateralDirectionality.RIGHT)
        {
            key = DESIRED_REAR_HEADWAY_RIGHT;
        }
        else
        {
            key = DESIRED_REAR_HEADWAY_CURRENT;
        }

        Length cached = getCachedValue(key, Length.class);
        if (cached != null)
        {
            return cached;
        }

        Length result = computeDesiredRearHeadway(dir);
        cacheValue(key, result, true);
        return result;
    }

    /**
     * Calculates the maximum physical acceleration currently possible based on the vehicle's speed.
     * <p>
     * Uses lazy evaluation to cache the result per tick. The calculation is based on an empirical piece-wise linear function
     * representing a typical combustion engine vehicle's performance.
     * </p>
     * @return Acceleration; the dynamically calculated maximum physical acceleration
     */
    public Acceleration getMaxPhysicalAcceleration()
    {
        Acceleration cached = getCachedValue(MAX_PHYSICAL_ACCELERATION, Acceleration.class);
        if (cached != null)
        {
            return cached;
        }

        Acceleration result = computeMaxPhysicalAcceleration();
        cacheValue(MAX_PHYSICAL_ACCELERATION, result, true);
        return result;
    }
    // ----------------------------------------------------------------------
    // Safe computation wrappers
    // ----------------------------------------------------------------------

    /**
     * Safely computes the ego-vehicle speed from {@link EgoPerception}. Returns zero speed in case of missing perception data
     * or errors.
     * @return ego speed or {@link Speed#ZERO} on error
     */
    private Speed computeEgoSpeed()
    {
        try
        {
            return this.vehicle.getPerception().getPerceptionCategory(EgoPerception.class).getSpeed();
        }
        catch (Exception e)
        {
            return Speed.ZERO;
        }
    }

    /**
     * Computes the dynamically desired front headway based on current speed and relaxed headway.
     * @return the computed desired front headway distance
     */
    private Length computeDesiredFrontHeadway()
    {
        Length desiredFrontHeadway = Length.NaN;
        try
        {
            desiredFrontHeadway = getEgoSpeed().times(this.vehicle.getCurrentRelaxedHeadway())
                    .plus(this.vehicle.getParameters().getParameter(ParameterTypes.S0));
        }
        catch (ParameterException exception)
        {
            exception.printStackTrace();
        }
        return desiredFrontHeadway;
    }

    /**
     * Computes the dynamically desired rear headway based on the target lane follower's speed. * @param dir the lateral
     * direction to inspect
     * @return the computed desired rear headway distance
     */
    private Length computeDesiredRearHeadway(final LateralDirectionality dir)
    {
        Length desiredRearHeadway = Length.NaN;
        try
        {
            HeadwayGtu follower =
                    this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class).getFollower(dir);
            if (follower == null)
            {
                // No follower, so no rear headway constraint
                desiredRearHeadway = Length.NEGATIVE_INFINITY;
            }
            else
            {
                Speed followerSpeed = follower.getSpeed();
                if (followerSpeed.lt(new Speed(15.0, SpeedUnit.KM_PER_HOUR)))
                {
                    // If follower is very slow, assume it can be very close without safety issues
                    desiredRearHeadway = Length.instantiateSI(1.5);
                }
                else
                {
                    desiredRearHeadway = followerSpeed.times(this.vehicle.getCurrentRelaxedHeadway())
                            .plus(this.vehicle.getParameters().getParameter(ParameterTypes.S0));
                }
            }
        }
        catch (ParameterException exception)
        {
            exception.printStackTrace();
        }
        return desiredRearHeadway;
    }

    /**
     * Interpolates the acceptable follower deceleration threshold based on current lane change desire.
     * <p>
     * The calculation returns the minimum threshold if the current desire is below the mandatory lane change threshold
     * ({@code DMAND}), the maximum threshold if the desire exceeds {@code 1.0}, and linearly interpolates between the two for
     * intermediate desire values. Clamping is applied strictly to the interpolation fraction to ensure mathematical robustness
     * with negative acceleration values.
     * </p>
     * * @param dir LateralDirectionality; the lateral direction for which the desire is evaluated
     * @return Acceleration; the computed acceleration threshold for the following vehicle (typically a negative value)
     * @throws ParameterException if a required parameter is missing in the vehicle's parameter set
     */
    private Acceleration computeFollowerDecelerationThreshold(final LateralDirectionality dir) throws ParameterException
    {
        Acceleration minThreshold =
                this.vehicle.getParameters().getParameter(MirovaParameters.minFollowerDecelerationThreshold);
        Acceleration maxThreshold =
                this.vehicle.getParameters().getParameter(MirovaParameters.maxFollowerDecelerationThreshold);

        // Use primitive double to avoid unnecessary autoboxing/unboxing overhead in the simulation loop
        double currentDirectionDesire = this.vehicle.getLaneChangeDesire().getDirectionalDesire(dir);
        double mandatoryDesireThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.DMAND);

        // Calculate the interpolation fraction based on current desire
        double fraction = (currentDirectionDesire - mandatoryDesireThreshold) / (1.0 - mandatoryDesireThreshold);

        // Clamp the fraction strictly between 0.0 (min limit) and 1.0 (max limit)
        fraction = Math.max(0.0, Math.min(1.0, fraction));

        // Interpolate using the clamped fraction
        double currentThresholdSi = minThreshold.si + fraction * (maxThreshold.si - minThreshold.si);

        return Acceleration.instantiateSI(currentThresholdSi);
    }

    /**
     * Interpolates the acceptable ego deceleration threshold based on current lane change desire.
     * <p>
     * The calculation returns the minimum threshold if the current desire is below the mandatory lane change threshold
     * ({@code DMAND}), the maximum threshold if the desire exceeds {@code 1.0}, and linearly interpolates between the two for
     * intermediate desire values.
     * </p>
     * * @param dir LateralDirectionality; the lateral direction for which the desire is evaluated
     * @return Acceleration; the computed acceleration threshold (typically a negative value for deceleration)
     * @throws ParameterException if a required parameter is missing in the vehicle's parameter set
     */
    private Acceleration computeEgoDecelerationThreshold(final LateralDirectionality dir) throws ParameterException
    {
        Acceleration minThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.minEgoDecelerationThreshold);
        Acceleration maxThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.maxEgoDecelerationThreshold);
        double currentDirectionDesire = this.vehicle.getLaneChangeDesire().getDirectionalDesire(dir);
        double mandatoryDesireThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.DMAND);

        // Calculate the interpolation fraction based on current desire
        double fraction = (currentDirectionDesire - mandatoryDesireThreshold) / (1.0 - mandatoryDesireThreshold);

        // Clamp the fraction to strictly bind it between 0.0 (min limit) and 1.0 (max limit)
        // This makes the logic mathematically robust, even if maxThreshold is numerically smaller than minThreshold (negative
        // accelerations)
        fraction = Math.max(0.0, Math.min(1.0, fraction));

        // Interpolate using the clamped fraction
        double currentThresholdSi = minThreshold.si + fraction * (maxThreshold.si - minThreshold.si);

        return Acceleration.instantiateSI(currentThresholdSi);
    }

    // ----------------------------------------------------------------------
    // Update handling
    // ----------------------------------------------------------------------

    /**
     * Marks the cached values as valid for the current simulation tick, clears the single-tick acceleration cache, and performs
     * housekeeping on active relaxation states.
     * @param vehicle the ego vehicle executing the update
     */
    @Override
    public void updateFromPerception(final MirovaTacticalPlanner vehicle)
    {
        // 1. CRITICAL: Clear the tick cache. Acceleration values from the previous tick are invalid!
        this.tickAccelerationCache.clear();

        // 2. Housekeeping for active relaxations
        try
        {
            Duration now = vehicle.getGtu().getSimulator().getSimulatorTime();
            Iterator<Map.Entry<String, RelaxationState>> iterator = this.activeRelaxations.entrySet().iterator();

            while (iterator.hasNext())
            {
                RelaxationState state = iterator.next().getValue();

                // If both the space buffer (< 10cm) and speed buffer (< 0.1 m/s) have decayed,
                // the relaxation process is finished. We remove it to free memory.
                if (state.getVirtualSpaceBuffer(now).si < 0.1 && Math.abs(state.getVirtualSpeedBuffer(now).si) < 0.1)
                {
                    iterator.remove();
                }
            }
        }
        catch (Exception e)
        {
            // Failsafe if simulator time is temporarily unavailable
        }

        // 3. Mark the context properties cache as valid (Lazy evaluation trigger)
        markCacheValid();
    }

    /**
     * Computes the maximum physical acceleration based on an empirical piece-wise linear model.
     * <p>
     * The model evaluates the current speed in km/h to apply the following constraints:
     * <ul>
     * <li>0 to 100 km/h: linear decrease from 3.5 m/s&sup2; to 1.0 m/s&sup2;</li>
     * <li>100 to 250 km/h: linear decrease from 1.0 m/s&sup2; to 0.0 m/s&sup2;</li>
     * <li>Above 250 km/h: 0.0 m/s&sup2;</li>
     * </ul>
     * </p>
     * @return Acceleration; the computed maximum physical acceleration
     */
    private Acceleration computeMaxPhysicalAcceleration()
    {
        double speedKmh = getEgoSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
        double maxAccSi;

        if (speedKmh < 100.0)
        {
            maxAccSi = 3.5 - (2.5 / 100.0) * speedKmh;
        }
        else if (speedKmh < 250.0)
        {
            maxAccSi = 1.0 - (1.0 / 150.0) * (speedKmh - 100.0);
        }
        else
        {
            maxAccSi = 0.0;
        }

        // Apply stochastic scaling factor
        try
        {
            double scalingFactor = this.vehicle.getParameters().getParameter(MirovaParameters.ACCELERATION_SCALING_FACTOR);
            maxAccSi *= scalingFactor;
        }
        catch (ParameterException e)
        {
            // Fallback: If parameter is somehow missing, we silently default to a scale of 1.0
            // No action required, maxAccSi remains unchanged
        }

        return Acceleration.instantiateSI(maxAccSi);
    }

    /**
     * Returns a compact textual summary of the currently cached ego parameters.
     * @return summary string
     */
    @Override
    public String toString()
    {
        return "EgoContext[" + "egoSpeed=" + getCachedValue(EGO_SPEED, Speed.class) + "]";
    }
}

package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
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

    // =========================================================================================
    // METHODS: RELAXATION MANAGEMENT
    // =========================================================================================

    /**
     * Evaluates a new cut-in situation and triggers the 2-parameter relaxation if the new leader violates the equilibrium
     * desired headway.
     * <p>
     * This method is typically called by the {@code NeighborsContext} when a change in the leader ID is detected (edge
     * trigger).
     * </p>
     * @param newLeader the new headway object that just cut in
     * @param oldLeaderSpeed the speed of the previous leader at the time of the cut-in
     * @throws ParameterException if a required parameter is missing
     * @throws GtuException if GTU state cannot be accessed
     */
    public void evaluateAndTriggerRelaxation(final HeadwayGtu newLeader, final Speed oldLeaderSpeed)
            throws ParameterException, GtuException
    {

        // Compute equilibrium headway for current speed
        Length desiredHeadway =
                this.vehicle.getCarFollowingModel().desiredHeadway(this.vehicle.getParameters(), this.getEgoSpeed());

        // If the cut-in is too close, trigger the relaxation
        if (newLeader.getDistance().lt(desiredHeadway))
        {
            Length gammaS = desiredHeadway.minus(newLeader.getDistance());

            // Calculate gamma_v (Speed of old leader minus speed of new leader)
            Speed gammaV = oldLeaderSpeed != null ? oldLeaderSpeed.minus(newLeader.getSpeed()) : Speed.ZERO;

            Duration tauSpace = this.vehicle.getParameters().getParameter(MirovaParameters.RELAXATION_TAU_SPACE);
            Duration tauSpeed = this.vehicle.getParameters().getParameter(MirovaParameters.RELAXATION_TAU_SPEED);

            triggerRelaxation(newLeader.getId(), gammaS, gammaV, tauSpace, tauSpeed);
        }
    }

    /**
     * Explicitly registers a relaxation state for a specific target vehicle.
     * <p>
     * If an active relaxation state already exists for the given leader ID, this method call is ignored to ensure the
     * exponential decay process is not interrupted by continuous maneuver triggers.
     * </p>
     * @param leaderId the ID of the target leader GTU
     * @param initialSpaceDeficit the initial space headway deficit [m]
     * @param initialSpeedDeficit the speed difference (oldLeaderSpeed - newLeaderSpeed) [m/s]
     * @param tauSpace the spatial relaxation time constant [s]
     * @param tauSpeed the speed relaxation time constant [s]
     */
    public void triggerRelaxation(final String leaderId, final Length initialSpaceDeficit, final Speed initialSpeedDeficit,
            final Duration tauSpace, final Duration tauSpeed)
    {

        // PREVENT OVERWRITING: Nur triggern, wenn für diesen Leader nicht schon eine Relaxation läuft!
        if (initialSpaceDeficit != null && initialSpaceDeficit.si > 0.0 && !this.activeRelaxations.containsKey(leaderId))
        {
            Duration now = this.vehicle.getGtu().getSimulator().getSimulatorTime();
            this.activeRelaxations.put(leaderId,
                    new RelaxationState(now, initialSpaceDeficit, initialSpeedDeficit, tauSpace, tauSpeed));
        }
    }

    /**
     * Proactively calculates deficits and triggers relaxation for a specific target leader.
     * <p>
     * This convenience method is designed for maneuver patterns to easily accept smaller gaps on adjacent lanes. It
     * automatically calculates the spatial deficit based on the current desired headway, and uses the speed difference between
     * the ego vehicle and the target leader as the initial speed deficit.
     * </p>
     * @param targetLeader the target leader GTU to relax towards
     * @throws ParameterException if required relaxation parameters are missing
     */
    public void triggerRelaxation(final HeadwayGtu targetLeader) throws ParameterException
    {
        if (targetLeader == null || this.activeRelaxations.containsKey(targetLeader.getId()))
        {
            return;
        }

        Parameters params = this.vehicle.getParameters();
        Length desiredHeadway = this.vehicle.getCarFollowingModel().desiredHeadway(params, this.getEgoSpeed());

        // Only trigger if the gap is actually tighter than our equilibrium desired headway
        if (targetLeader.getDistance().lt(desiredHeadway))
        {
            Length spaceDeficit = desiredHeadway.minus(targetLeader.getDistance());

            // For proactive lane changes, the speed deficit (gamma_v) is approximated
            // as the difference between Ego Speed and the Target Leader's Speed.
            Speed speedDeficit = this.getEgoSpeed().minus(targetLeader.getSpeed());

            Duration tauSpace = params.getParameter(MirovaParameters.RELAXATION_TAU_SPACE);
            Duration tauSpeed = params.getParameter(MirovaParameters.RELAXATION_TAU_SPEED);

            // Delegate to the main method
            triggerRelaxation(targetLeader.getId(), spaceDeficit, speedDeficit, tauSpace, tauSpeed);
        }
    }

    /**
     * Retrieves the active relaxation state for a specific leader.
     * @param leaderId the ID of the leader GTU
     * @return the active relaxation state, or null if no relaxation is active for this leader
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
     * @param dir the lateral direction
     * @return the computed acceleration threshold
     * @throws ParameterException if a parameter is missing
     */
    private Acceleration computeFollowerDecelerationThreshold(final LateralDirectionality dir) throws ParameterException
    {
        Acceleration minThreshold =
                this.vehicle.getParameters().getParameter(MirovaParameters.minFollowerDecelerationThreshold);
        Acceleration maxThreshold =
                this.vehicle.getParameters().getParameter(MirovaParameters.maxFollowerDecelerationThreshold);
        Double currentDirectionDesire = this.vehicle.getLaneChangeDesire().getDirectionalDesire(dir);
        Double mandatoryDesireThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.DMAND);

        Double currentThreshold;
        if (currentDirectionDesire >= mandatoryDesireThreshold)
        {
            currentThreshold = minThreshold.si + (maxThreshold.si - minThreshold.si)
                    * (currentDirectionDesire - mandatoryDesireThreshold) / (1.0 - mandatoryDesireThreshold);
        }
        else
        {
            currentThreshold = minThreshold.si;
        }
        currentThreshold = Math.max(maxThreshold.si, Math.min(minThreshold.si, currentThreshold));
        return Acceleration.instantiateSI(currentThreshold);
    }

    /**
     * Interpolates the acceptable ego deceleration threshold based on current lane change desire.
     * @param dir the lateral direction
     * @return the computed acceleration threshold
     * @throws ParameterException if a parameter is missing
     */
    private Acceleration computeEgoDecelerationThreshold(final LateralDirectionality dir) throws ParameterException
    {
        Acceleration minThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.minEgoDecelerationThreshold);
        Acceleration maxThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.maxEgoDecelerationThreshold);
        Double currentDirectionDesire = this.vehicle.getLaneChangeDesire().getDirectionalDesire(dir);
        Double mandatoryDesireThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.DMAND);

        Double currentThreshold;
        if (currentDirectionDesire >= mandatoryDesireThreshold)
        {
            currentThreshold = minThreshold.si + (maxThreshold.si - minThreshold.si)
                    * (currentDirectionDesire - mandatoryDesireThreshold) / (1.0 - mandatoryDesireThreshold);
        }
        else
        {
            currentThreshold = minThreshold.si;
        }
        currentThreshold = Math.max(maxThreshold.si, Math.min(minThreshold.si, currentThreshold));
        return Acceleration.instantiateSI(currentThreshold);
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
     * Returns a compact textual summary of the currently cached ego parameters.
     * @return summary string
     */
    @Override
    public String toString()
    {
        return "EgoContext[" + "egoSpeed=" + getCachedValue(EGO_SPEED, Speed.class) + "]";
    }
}

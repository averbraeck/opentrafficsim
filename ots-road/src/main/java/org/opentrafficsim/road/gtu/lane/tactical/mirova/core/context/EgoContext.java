package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
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
 * Forms a central part of <b>Layer 1 (Perception & Context)</b> in the MiRoVA architecture.
 * Provides direct access to low-level vehicle states such as speed, accelerations, and
 * deceleration thresholds, which are frequently required by tactical and longitudinal control logic.
 * </p>
 * <p>
 * The values are lazily updated once per simulation tick and cached within the
 * {@link VehicleContextManager} to optimize performance and ensure intra-tick consistency.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class EgoContext extends ContextCategory implements UpdatableContext {

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

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Constructs a new {@code EgoContext}.
     *
     * @param vehicle the ego vehicle associated with this context
     */
    public EgoContext(final MirovaTacticalPlanner vehicle) {
        super("Ego", vehicle);
    }

    // ----------------------------------------------------------------------
    // Lazy Accessors
    // ----------------------------------------------------------------------

    /**
     * Returns the current ego-vehicle speed as perceived in the last simulation tick.
     * <p>
     * This method uses lazy evaluation: the speed is only retrieved once per tick
     * from {@link EgoPerception} and then cached.
     * </p>
     *
     * @return current ego speed
     */
    public Speed getEgoSpeed() {
        Speed cached = getCachedValue(EGO_SPEED, Speed.class);
        if (cached != null) {
            return cached;
        }

        Speed result = computeEgoSpeed();
        cacheValue(EGO_SPEED, result, true);
        return result;
    }

    /**
     * Returns the current baseline car-following acceleration of the ego vehicle.
     * Uses lazy evaluation to cache the result per tick.
     *
     * @return current car-following acceleration
     * @throws ParameterException if a required parameter is missing
     * @throws GtuException if GTU state cannot be accessed
     * @throws NetworkException if network state cannot be accessed
     */
    public Acceleration getCurrentCarFollowingAcceleration() throws ParameterException, GtuException, NetworkException {
        Acceleration cached = getCachedValue(CURRENT_CF_ACCELERATION, Acceleration.class);
        if (cached != null) {
            return cached;
        }

        Acceleration result = this.vehicle.getGtu().getCarFollowingAcceleration();
        cacheValue(CURRENT_CF_ACCELERATION, result, true);
        return result;
    }

    /**
     * Returns the currently desired speed of the ego vehicle.
     * Uses lazy evaluation to cache the result per tick.
     *
     * @return the current desired speed
     * @throws ParameterException if parameter resolution fails
     * @throws GtuException if GTU state is invalid
     * @throws NetworkException if network state is invalid
     */
    public Speed getCurrentDesiredSpeed() throws ParameterException, GtuException, NetworkException {
        Speed cached = getCachedValue(CURRENT_DESIRED_SPEED, Speed.class);
        if (cached != null) {
            return cached;
        }
        Speed result = this.vehicle.getGtu().getDesiredSpeed();
        cacheValue(CURRENT_DESIRED_SPEED, result, true);
        return result;
    }

    /**
     * Computes and returns the deceleration threshold for the ego vehicle for a specific lane change direction.
     *
     * @param dir the lateral direction to consider (LEFT or RIGHT)
     * @return the acceptable deceleration threshold for the ego vehicle
     * @throws ParameterException if threshold parameters are missing
     */
    public Acceleration getEgoDecelerationThreshold(final LateralDirectionality dir) throws ParameterException {
        String key = (dir == LateralDirectionality.LEFT) ? EGO_DECELERATION_THRESHOLD_LEFT : EGO_DECELERATION_THRESHOLD_RIGHT;

        Acceleration cached = getCachedValue(key, Acceleration.class);
        if (cached != null) {
            return cached;
        }

        Acceleration result = computeEgoDecelerationThreshold(dir);
        cacheValue(key, result, true);
        return result;
    }

    /**
     * Computes and returns the expected deceleration threshold for the follower in a target lane.
     *
     * @param dir the lateral direction to consider (LEFT or RIGHT)
     * @return the expected deceleration threshold for the follower
     * @throws ParameterException if threshold parameters are missing
     */
    public Acceleration getFollowerDecelerationThreshold(final LateralDirectionality dir) throws ParameterException {
        String key = (dir == LateralDirectionality.LEFT) ? FOLLOWER_DECELERATION_THRESHOLD_LEFT : FOLLOWER_DECELERATION_THRESHOLD_RIGHT;

        Acceleration cached = getCachedValue(key, Acceleration.class);
        if (cached != null) {
            return cached;
        }

        Acceleration result = computeFollowerDecelerationThreshold(dir);
        cacheValue(key, result, true);
        return result;
    }

    /**
     * Calculates the desired front headway distance for a given direction.
     *
     * @param dir the lateral direction (NONE for current lane)
     * @return the desired front headway distance
     */
    public Length getDesiredFrontHeadway(final LateralDirectionality dir) {
        String key;
        if (dir == LateralDirectionality.LEFT) {
            key = DESIRED_FRONT_HEADWAY_LEFT;
        } else if (dir == LateralDirectionality.RIGHT) {
            key = DESIRED_FRONT_HEADWAY_RIGHT;
        } else {
            key = DESIRED_FRONT_HEADWAY_CURRENT;
        }

        Length cached = getCachedValue(key, Length.class);
        if (cached != null) {
            return cached;
        }

        Length result = computeDesiredFrontHeadway();
        cacheValue(key, result, true);
        return result;
    }

    /**
     * Calculates the desired rear headway distance for a given direction based on the follower.
     *
     * @param dir the lateral direction (NONE for current lane)
     * @return the desired rear headway distance
     */
    public Length getDesiredRearHeadway(final LateralDirectionality dir) {
        String key;
        if (dir == LateralDirectionality.LEFT) {
            key = DESIRED_REAR_HEADWAY_LEFT;
        } else if (dir == LateralDirectionality.RIGHT) {
            key = DESIRED_REAR_HEADWAY_RIGHT;
        } else {
            key = DESIRED_REAR_HEADWAY_CURRENT;
        }

        Length cached = getCachedValue(key, Length.class);
        if (cached != null) {
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
     * Safely computes the ego-vehicle speed from {@link EgoPerception}.
     * Returns zero speed in case of missing perception data or errors.
     *
     * @return ego speed or {@link Speed#ZERO} on error
     */
    private Speed computeEgoSpeed() {
        try {
            return this.vehicle.getPerception()
                    .getPerceptionCategory(EgoPerception.class)
                    .getSpeed();
        } catch (Exception e) {
            return Speed.ZERO;
        }
    }

    /**
     * Computes the dynamically desired front headway based on current speed and relaxed headway.
     *
     * @return the computed desired front headway distance
     */
    private Length computeDesiredFrontHeadway() {
        Length desiredFrontHeadway = Length.NaN;
        try {
            desiredFrontHeadway = getEgoSpeed().times(this.vehicle.getCurrentRelaxedHeadway())
                    .plus(this.vehicle.getParameters().getParameter(ParameterTypes.S0));
        } catch (ParameterException exception) {
            exception.printStackTrace();
        }
        return desiredFrontHeadway;
    }

    /**
     * Computes the dynamically desired rear headway based on the target lane follower's speed.
     * * @param dir the lateral direction to inspect
     * @return the computed desired rear headway distance
     */
    private Length computeDesiredRearHeadway(final LateralDirectionality dir) {
        Length desiredRearHeadway = Length.NaN;
        try {
            HeadwayGtu follower = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class).getFollower(dir);
            if (follower == null) {
                // No follower, so no rear headway constraint
                desiredRearHeadway = Length.NEGATIVE_INFINITY;
            } else {
                Speed followerSpeed = follower.getSpeed();
                if (followerSpeed.lt(new Speed(15.0, SpeedUnit.KM_PER_HOUR))) {
                    // If follower is very slow, assume it can be very close without safety issues
                    desiredRearHeadway = Length.instantiateSI(1.5);
                } else {
                    desiredRearHeadway = followerSpeed.times(this.vehicle.getCurrentRelaxedHeadway())
                            .plus(this.vehicle.getParameters().getParameter(ParameterTypes.S0));
                }
            }
        } catch (ParameterException exception) {
            exception.printStackTrace();
        }
        return desiredRearHeadway;
    }

    /**
     * Interpolates the acceptable follower deceleration threshold based on current lane change desire.
     *
     * @param dir the lateral direction
     * @return the computed acceleration threshold
     * @throws ParameterException if a parameter is missing
     */
    private Acceleration computeFollowerDecelerationThreshold(final LateralDirectionality dir) throws ParameterException {
        Acceleration minThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.minFollowerDecelerationThreshold);
        Acceleration maxThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.maxFollowerDecelerationThreshold);
        Double currentDirectionDesire = this.vehicle.getLaneChangeDesire().getDirectionalDesire(dir);
        Double mandatoryDesireThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.DMAND);

        Double currentThreshold;
        if (currentDirectionDesire >= mandatoryDesireThreshold) {
            currentThreshold = minThreshold.si
                    + (maxThreshold.si - minThreshold.si)
                    * (currentDirectionDesire - mandatoryDesireThreshold)
                    / (1.0 - mandatoryDesireThreshold);
        } else {
            currentThreshold = minThreshold.si;
        }
        currentThreshold = Math.max(maxThreshold.si, Math.min(minThreshold.si, currentThreshold));
        return Acceleration.instantiateSI(currentThreshold);
    }

    /**
     * Interpolates the acceptable ego deceleration threshold based on current lane change desire.
     *
     * @param dir the lateral direction
     * @return the computed acceleration threshold
     * @throws ParameterException if a parameter is missing
     */
    private Acceleration computeEgoDecelerationThreshold(final LateralDirectionality dir) throws ParameterException {
        Acceleration minThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.minEgoDecelerationThreshold);
        Acceleration maxThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.maxEgoDecelerationThreshold);
        Double currentDirectionDesire = this.vehicle.getLaneChangeDesire().getDirectionalDesire(dir);
        Double mandatoryDesireThreshold = this.vehicle.getParameters().getParameter(MirovaParameters.DMAND);

        Double currentThreshold;
        if (currentDirectionDesire >= mandatoryDesireThreshold) {
            currentThreshold = minThreshold.si
                    + (maxThreshold.si - minThreshold.si)
                    * (currentDirectionDesire - mandatoryDesireThreshold)
                    / (1.0 - mandatoryDesireThreshold);
        } else {
            currentThreshold = minThreshold.si;
        }
        currentThreshold = Math.max(maxThreshold.si, Math.min(minThreshold.si, currentThreshold));
        return Acceleration.instantiateSI(currentThreshold);
    }

    // ----------------------------------------------------------------------
    // Update handling
    // ----------------------------------------------------------------------

    /**
     * Marks the cached values as valid for the current simulation tick.
     * <p>
     * No immediate update is required, as values are computed lazily.
     * </p>
     *
     * @param vehicle the ego vehicle executing the update
     */
    @Override
    public void updateFromPerception(final MirovaTacticalPlanner vehicle) {
        markCacheValid();
    }

    /**
     * Returns a compact textual summary of the currently cached ego parameters.
     *
     * @return summary string
     */
    @Override
    public String toString() {
        return "EgoContext[" +
                "egoSpeed=" + getCachedValue(EGO_SPEED, Speed.class) +
                "]";
    }
}
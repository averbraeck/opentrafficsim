package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import java.util.Collections;
import java.util.Iterator;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Context category describing the dynamic interaction between the ego vehicle
 * and neighboring vehicles on adjacent lanes.
 * <p>
 * Forms a central part of <b>Layer 1 (Perception & Context)</b> in the MiRoVA architecture.
 * Computes and stores indicators for lane-change feasibility, such as:
 * <ul>
 * <li>Deceleration required by the ego vehicle on the target lane.</li>
 * <li>Deceleration required by the follower in the target lane.</li>
 * </ul>
 * These indicators are computed separately for both left and right directions,
 * but only if a lane change in that direction is currently legal and possible.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class NeighborsContext extends ContextCategory implements UpdatableContext {

    /** Cache key for ego deceleration left. */
    public static final String EGO_DECEL_LEFT = "egoDecel_LEFT";
    /** Cache key for ego deceleration right. */
    public static final String EGO_DECEL_RIGHT = "egoDecel_RIGHT";
    /** Cache key for follower deceleration left. */
    public static final String FOLLOWER_DECEL_LEFT = "followerDecel_LEFT";
    /** Cache key for follower deceleration right. */
    public static final String FOLLOWER_DECEL_RIGHT = "followerDecel_RIGHT";
    /** Cache key for front gap distance current lane. */
    public static final String FRONT_GAP_DISTANCE_CURRENT = "frontGapDistanceCurrent";
    /** Cache key for front gap distance left lane. */
    public static final String FRONT_GAP_DISTANCE_LEFT = "frontGapDistanceLeft";
    /** Cache key for front gap distance right lane. */
    public static final String FRONT_GAP_DISTANCE_RIGHT = "frontGapDistanceRight";
    /** Cache key for front gap delta speed current lane. */
    public static final String FRONT_GAP_DELTA_SPEED_CURRENT = "frontGapDeltaSpeedCurrent";
    /** Cache key for front gap delta speed left lane. */
    public static final String FRONT_GAP_DELTA_SPEED_LEFT = "frontGapDeltaSpeedLeft";
    /** Cache key for front gap delta speed right lane. */
    public static final String FRONT_GAP_DELTA_SPEED_RIGHT = "frontGapDeltaSpeedRight";
    /** Cache key for front gap time headway current lane. */
    public static final String FRONT_GAP_TIME_HEADWAY_CURRENT = "frontGapTimeHeadwayCurrent";
    /** Cache key for front gap time headway left lane. */
    public static final String FRONT_GAP_TIME_HEADWAY_LEFT = "frontGapTimeHeadwayLeft";
    /** Cache key for front gap time headway right lane. */
    public static final String FRONT_GAP_TIME_HEADWAY_RIGHT = "frontGapTimeHeadwayRight";
    /** Cache key for current leader. */
    public static final String CURRENT_LEADER = "currentLeader";
    /** Cache key for left leader. */
    public static final String LEFT_LEADER = "leftLeader";
    /** Cache key for right leader. */
    public static final String RIGHT_LEADER = "rightLeader";
    /** Cache key for current follower. */
    public static final String CURRENT_FOLLOWER = "currentFollower";
    /** Cache key for left follower. */
    public static final String LEFT_FOLLOWER = "leftFollower";
    /** Cache key for right follower. */
    public static final String RIGHT_FOLLOWER = "rightFollower";
    /** Cache key for leaders in current lane. */
    public static final String LEADERS_CURRENT = "leaders_CURRENT";
    /** Cache key for leaders in left lane. */
    public static final String LEADERS_LEFT = "leaders_LEFT";
    /** Cache key for leaders in right lane. */
    public static final String LEADERS_RIGHT = "leaders_RIGHT";
    /** Cache key for followers in current lane. */
    public static final String FOLLOWERS_CURRENT = "followers_CURRENT";
    /** Cache key for followers in left lane. */
    public static final String FOLLOWERS_LEFT = "followers_LEFT";
    /** Cache key for followers in right lane. */
    public static final String FOLLOWERS_RIGHT = "followers_RIGHT";
    /** Cache key for rear gap distance current lane. */
    public static final String REAR_GAP_DISTANCE_CURRENT = "rearGapDistanceCurrent";
    /** Cache key for rear gap distance left lane. */
    public static final String REAR_GAP_DISTANCE_LEFT = "rearGapDistanceLeft";
    /** Cache key for rear gap distance right lane. */
    public static final String REAR_GAP_DISTANCE_RIGHT = "rearGapDistanceRight";
    /** Cache key for rear gap delta speed current lane. */
    public static final String REAR_GAP_DELTA_SPEED_CURRENT = "rearGapDeltaSpeedCurrent";
    /** Cache key for rear gap delta speed left lane. */
    public static final String REAR_GAP_DELTA_SPEED_LEFT = "rearGapDeltaSpeedLeft";
    /** Cache key for rear gap delta speed right lane. */
    public static final String REAR_GAP_DELTA_SPEED_RIGHT = "rearGapDeltaSpeedRight";
    /** Cache key for rear gap time headway current lane. */
    public static final String REAR_GAP_TIME_HEADWAY_CURRENT = "rearGapTimeHeadwayCurrent";
    /** Cache key for rear gap time headway left lane. */
    public static final String REAR_GAP_TIME_HEADWAY_LEFT = "rearGapTimeHeadwayLeft";
    /** Cache key for rear gap time headway right lane. */
    public static final String REAR_GAP_TIME_HEADWAY_RIGHT = "rearGapTimeHeadwayRight";
    /** Cache key for right side overtaking ahead flag. */
    public static final String RIGHT_SIDE_OVERTAKING_AHEAD = "rightSideOvertakingAhead";
    /** Cache key for lane change possible left flag. */
    public static final String LANE_CHANGE_POSSIBLE_LEFT = "laneChangePossibleLeft";
    /** Cache key for lane change possible right flag. */
    public static final String LANE_CHANGE_POSSIBLE_RIGHT = "laneChangePossibleRight";
    /** Cache key for GTU alongside left flag. */
    public static final String GTU_ALONGSIDE_LEFT = "alongside_LEFT";
    /** Cache key for GTU alongside right flag. */
    public static final String GTU_ALONGSIDE_RIGHT = "alongside_RIGHT";

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Constructs a new {@code NeighborsContext}.
     *
     * @param vehicle the ego vehicle associated with this context
     */
    public NeighborsContext(final MirovaTacticalPlanner vehicle) {
        super("Neighbors", vehicle);
    }

    // ----------------------------------------------------------------------
    // Computation methods
    // ----------------------------------------------------------------------

    /**
     * Computes the deceleration required by the ego vehicle when attempting
     * a lane change in the specified direction.
     * <p>
     * For each leader vehicle on the target lane, the car-following model is
     * evaluated to determine the necessary braking effort to safely merge.
     * The minimum (most restrictive) value across all leaders is returned.
     * </p>
     *
     * @param laneChangeDirection the intended lane change direction (LEFT or RIGHT)
     * @return minimum required ego deceleration [m/s²]
     * @throws ParameterException       if a parameter lookup fails
     * @throws OperationalPlanException if car-following computation fails
     */
    private Acceleration computeLaneChangeEgoDeceleration(final LateralDirectionality laneChangeDirection)
            throws ParameterException, OperationalPlanException {
        Acceleration egoDeceleration = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        NeighborsPerception neighbors = this.vehicle.getPerception().getPerceptionCategory(NeighborsPerception.class);
        InfrastructureContext infra = this.vehicle.getContextManager()
                .getCategory("Infrastructure", InfrastructureContext.class);

        // Retrieve current legal speed limit info
        SpeedLimitInfo currentLimitInfo = infra.getCurrentSpeedLimit();
        CarFollowingModel cfModel = this.vehicle.getCarFollowingModel();
        Parameters params = this.vehicle.getGtu().getParameters();
        Speed egoSpeed = this.vehicle.getContextManager()
                .getCategory("Ego", EgoContext.class)
                .getEgoSpeed();

        for (HeadwayGtu leader : neighbors.getFirstLeaders(laneChangeDirection)) {
            Acceleration iteraryDecel = CarFollowingUtil.followSingleLeader(
                    cfModel,
                    params,
                    egoSpeed,
                    currentLimitInfo,
                    leader.getDistance(),
                    leader.getSpeed());
            egoDeceleration = Acceleration.min(egoDeceleration, iteraryDecel);
        }
        return egoDeceleration;
    }

    /**
     * Computes the deceleration required by the follower on the target lane
     * to maintain safety if the ego vehicle were to change lanes.
     * <p>
     * This method evaluates, for each follower vehicle, the car-following model
     * of that follower using its own parameters and perception. The result
     * represents the minimum (most restrictive) deceleration that would be
     * induced by the ego vehicle’s lane change.
     * </p>
     *
     * @param laneChangeDirection the intended lane change direction (LEFT or RIGHT)
     * @return minimum required follower deceleration [m/s²]
     * @throws ParameterException       if a parameter lookup fails
     * @throws OperationalPlanException if car-following computation fails
     */
    private Acceleration computeLaneChangeFollowerDeceleration(final LateralDirectionality laneChangeDirection)
            throws ParameterException, OperationalPlanException {
        Acceleration followerDecelValue = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        NeighborsPerception neighbors = this.vehicle.getPerception().getPerceptionCategory(NeighborsPerception.class);

        Speed egoSpeed = this.vehicle.getContextManager()
                .getCategory("Ego", EgoContext.class)
                .getEgoSpeed();

        for (HeadwayGtu follower : neighbors.getFirstFollowers(laneChangeDirection)) {
            Acceleration iteraryDecel = CarFollowingUtil.followSingleLeader(
                    follower.getCarFollowingModel(),
                    follower.getParameters(),
                    follower.getSpeed(),
                    follower.getSpeedLimitInfo(),
                    follower.getDistance(),
                    egoSpeed);
            followerDecelValue = Acceleration.min(followerDecelValue, iteraryDecel);
        }
        return followerDecelValue;
    }

    // ---- Lazy accessors for deceleration values -------------------------------------

    /**
     * Lazily computes and returns the ego deceleration required for a lane change.
     *
     * @param dir the intended lane change direction
     * @return the computed ego deceleration
     */
    public Acceleration getEgoDeceleration(final LateralDirectionality dir) {
        String name = dir.isLeft() ? EGO_DECEL_LEFT : EGO_DECEL_RIGHT;
        Acceleration cached = getCachedValue(name, Acceleration.class);
        if (cached != null) {
            return cached;
        }

        Acceleration result = computeSafeEgoDecel(dir);
        cacheValue(name, result, true);
        return result;
    }

    /**
     * Lazily computes and returns the follower deceleration induced by a lane change.
     *
     * @param dir the intended lane change direction
     * @return the computed follower deceleration
     */
    public Acceleration getFollowerDeceleration(final LateralDirectionality dir) {
        String name = dir.isLeft() ? FOLLOWER_DECEL_LEFT : FOLLOWER_DECEL_RIGHT;
        Acceleration cached = getCachedValue(name, Acceleration.class);
        if (cached != null) {
            return cached;
        }

        Acceleration result = computeSafeFollowerDecel(dir);
        cacheValue(name, result, true);
        return result;
    }

    /**
     * Lazily computes and returns the front gap distance in the specified direction.
     *
     * @param dir the intended direction
     * @return the front gap distance
     */
    public Length getFrontGapDistance(final LateralDirectionality dir) {
        String name;
        if (dir.isLeft()) {
            name = FRONT_GAP_DISTANCE_LEFT;
        } else if (dir.isRight()) {
            name = FRONT_GAP_DISTANCE_RIGHT;
        } else {
            name = FRONT_GAP_DISTANCE_CURRENT;
        }
        Length cached = getCachedValue(name, Length.class);
        if (cached != null) {
            return cached;
        }

        Length result = computeFrontGapDistance(dir);
        cacheValue(name, result, true);
        return result;
    }

    /**
     * Lazily computes and returns the speed difference to the front gap leader in the specified direction.
     *
     * @param dir the intended direction
     * @return the speed difference (ego speed minus leader speed)
     */
    public Speed getFrontGapDeltaSpeed(final LateralDirectionality dir) {
        String name;
        if (dir.isLeft()) {
            name = FRONT_GAP_DELTA_SPEED_LEFT;
        } else if (dir.isRight()) {
            name = FRONT_GAP_DELTA_SPEED_RIGHT;
        } else {
            name = FRONT_GAP_DELTA_SPEED_CURRENT;
        }
        Speed cached = getCachedValue(name, Speed.class);
        if (cached != null) {
            return cached;
        }

        Speed result = computeFrontGapDeltaSpeed(dir);
        cacheValue(name, result, true);
        return result;
    }

    /**
     * Lazily computes and returns the front gap time headway in the specified direction.
     *
     * @param dir the intended direction
     * @return the front gap time headway
     */
    public Duration getFrontGapTimeHeadway(final LateralDirectionality dir) {
        String name;
        if (dir.isLeft()) {
            name = FRONT_GAP_TIME_HEADWAY_LEFT;
        } else if (dir.isRight()) {
            name = FRONT_GAP_TIME_HEADWAY_RIGHT;
        } else {
            name = FRONT_GAP_TIME_HEADWAY_CURRENT;
        }
        Duration cached = getCachedValue(name, Duration.class);
        if (cached != null) {
            return cached;
        }

        Duration result = computeFrontGapTimeHeadway(dir);
        cacheValue(name, result, true);
        return result;
    }

    /**
     * Lazily computes and returns the rear gap distance in the specified direction.
     *
     * @param dir the intended direction
     * @return the rear gap distance
     */
    public Length getRearGapDistance(final LateralDirectionality dir) {
        String name;
        if (dir.isLeft()) {
            name = REAR_GAP_DISTANCE_LEFT;
        } else if (dir.isRight()) {
            name = REAR_GAP_DISTANCE_RIGHT;
        } else {
            name = REAR_GAP_DISTANCE_CURRENT;
        }
        Length cached = getCachedValue(name, Length.class);
        if (cached != null) {
            return cached;
        }

        Length result = computeRearGapDistance(dir);
        cacheValue(name, result, true);
        return result;
    }

    /**
     * Lazily computes and returns the speed difference to the rear gap follower in the specified direction.
     *
     * @param dir the intended direction
     * @return the speed difference (ego speed minus follower speed)
     */
    public Speed getRearGapDeltaSpeed(final LateralDirectionality dir) {
        String name;
        if (dir.isLeft()) {
            name = REAR_GAP_DELTA_SPEED_LEFT;
        } else if (dir.isRight()) {
            name = REAR_GAP_DELTA_SPEED_RIGHT;
        } else {
            name = REAR_GAP_DELTA_SPEED_CURRENT;
        }
        Speed cached = getCachedValue(name, Speed.class);
        if (cached != null) {
            return cached;
        }

        Speed result = computeRearGapDeltaSpeed(dir);
        cacheValue(name, result, true);
        return result;
    }

    /**
     * Lazily computes and returns the rear gap time headway in the specified direction.
     *
     * @param dir the intended direction
     * @return the rear gap time headway
     */
    public Duration getRearGapTimeHeadway(final LateralDirectionality dir) {
        String name;
        if (dir.isLeft()) {
            name = REAR_GAP_TIME_HEADWAY_LEFT;
        } else if (dir.isRight()) {
            name = REAR_GAP_TIME_HEADWAY_RIGHT;
        } else {
            name = REAR_GAP_TIME_HEADWAY_CURRENT;
        }
        Duration cached = getCachedValue(name, Duration.class);
        if (cached != null) {
            return cached;
        }

        Duration result = computeRearGapTimeHeadway(dir);
        cacheValue(name, result, true);
        return result;
    }

    // ----------------------------------------------------------------------
    // Cached List Accessors (Iterable)
    // ----------------------------------------------------------------------

    /**
     * Returns a cached iterable of all leaders in the given direction.
     * Computes (via Perception) and caches the result if not yet available.
     *
     * @param dir Direction to look for leaders
     * @return Iterable of HeadwayGtu (empty if no perception available)
     */
    @SuppressWarnings("unchecked")
    public Iterable<HeadwayGtu> getLeaders(final LateralDirectionality dir) {
        String key;
        RelativeLane lane;
        if (dir.isLeft()) {
            key = LEADERS_LEFT;
            lane = RelativeLane.LEFT;
        } else if (dir.isRight()) {
            key = LEADERS_RIGHT;
            lane = RelativeLane.RIGHT;
        } else {
            key = LEADERS_CURRENT;
            lane = RelativeLane.CURRENT;
        }

        Iterable<HeadwayGtu> cached = getCachedValue(key, Iterable.class);
        if (cached != null) {
            return cached;
        }

        try {
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> result =
                    this.vehicle.getPerception().getPerceptionCategory(NeighborsPerception.class).getLeaders(lane);
            cacheValue(key, result, true);
            return result;
        } catch (OperationalPlanException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Returns a cached iterable of all followers in the given direction.
     * Computes (via Perception) and caches the result if not yet available.
     *
     * @param dir Direction to look for followers
     * @return Iterable of HeadwayGtu (empty if no perception available)
     */
    @SuppressWarnings("unchecked")
    public Iterable<HeadwayGtu> getFollowers(final LateralDirectionality dir) {
        String key;
        RelativeLane lane;
        if (dir.isLeft()) {
            key = FOLLOWERS_LEFT;
            lane = RelativeLane.LEFT;
        } else if (dir.isRight()) {
            key = FOLLOWERS_RIGHT;
            lane = RelativeLane.RIGHT;
        } else {
            key = FOLLOWERS_CURRENT;
            lane = RelativeLane.CURRENT;
        }

        Iterable<HeadwayGtu> cached = getCachedValue(key, Iterable.class);
        if (cached != null) {
            return cached;
        }

        try {
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> result =
                    this.vehicle.getPerception().getPerceptionCategory(NeighborsPerception.class).getFollowers(lane);
            cacheValue(key, result, true);
            return result;
        } catch (OperationalPlanException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Returns whether there is a GTU alongside in the given direction.
     * Computes (via Perception) and caches the result if not yet available.
     *
     * @param dir Direction to check for alongside GTU
     * @return true if there is a GTU alongside, false if not or if perception unavailable
     * @throws ParameterException       if a parameter lookup fails
     * @throws NullPointerException     if perception context is null
     * @throws IllegalArgumentException if an illegal argument is provided
     */
    public Boolean isGtuAlongside(final LateralDirectionality dir) throws ParameterException, NullPointerException, IllegalArgumentException {
        String key;
        if (dir.isLeft()) {
            key = GTU_ALONGSIDE_LEFT;
        } else if (dir.isRight()) {
            key = GTU_ALONGSIDE_RIGHT;
        } else {
            return false; // No alongside for current lane
        }
        Boolean cached = getCachedValue(key, Boolean.class);
        if (cached != null) {
            return cached;
        }
        try {
            boolean result = this.vehicle.getPerception().getPerceptionCategory(NeighborsPerception.class).isGtuAlongside(dir);
            cacheValue(key, result, true);
            return result;
        } catch (OperationalPlanException e) {
            return false;
        }
    }

    // ----------------------------------------------------------------------
    // Single Leader Accessors
    // ----------------------------------------------------------------------

    /**
     * Returns the nearest leader in the specified direction.
     * Delegates to the specific lane accessor methods.
     *
     * @param dir the lateral direction
     * @return the nearest leader or null
     */
    public HeadwayGtu getLeader(final LateralDirectionality dir) {
        if (dir.isLeft()) {
            return getLeftLeader();
        } else if (dir.isRight()) {
            return getRightLeader();
        } else {
            return getCurrentLeader();
        }
    }

    /**
     * Returns the nearest leader on the current lane.
     * Uses the cached leader list to extract the first element.
     *
     * @return the nearest leader or null
     */
    public HeadwayGtu getCurrentLeader() {
        HeadwayGtu cached = getCachedValue(CURRENT_LEADER, HeadwayGtu.class);
        if (cached != null) {
            return cached;
        }

        Iterable<HeadwayGtu> leaders = getLeaders(LateralDirectionality.NONE);
        Iterator<HeadwayGtu> it = leaders.iterator();
        HeadwayGtu result = it.hasNext() ? it.next() : null;

        cacheValue(CURRENT_LEADER, result, true);
        return result;
    }

    /**
     * Returns the nearest leader on the left lane.
     * Uses the cached leader list to extract the first element.
     *
     * @return the nearest leader or null
     */
    public HeadwayGtu getLeftLeader() {
        HeadwayGtu cached = getCachedValue(LEFT_LEADER, HeadwayGtu.class);
        if (cached != null) {
            return cached;
        }

        Iterable<HeadwayGtu> leaders = getLeaders(LateralDirectionality.LEFT);
        Iterator<HeadwayGtu> it = leaders.iterator();
        HeadwayGtu result = it.hasNext() ? it.next() : null;

        cacheValue(LEFT_LEADER, result, true);
        return result;
    }

    /**
     * Returns the nearest leader on the right lane.
     * Uses the cached leader list to extract the first element.
     *
     * @return the nearest leader or null
     */
    public HeadwayGtu getRightLeader() {
        HeadwayGtu cached = getCachedValue(RIGHT_LEADER, HeadwayGtu.class);
        if (cached != null) {
            return cached;
        }

        Iterable<HeadwayGtu> leaders = getLeaders(LateralDirectionality.RIGHT);
        Iterator<HeadwayGtu> it = leaders.iterator();
        HeadwayGtu result = it.hasNext() ? it.next() : null;

        cacheValue(RIGHT_LEADER, result, true);
        return result;
    }

    // ----------------------------------------------------------------------
    // Single Follower Accessors
    // ----------------------------------------------------------------------

    /**
     * Returns the nearest follower in the specified direction.
     * Delegates to the specific lane accessor methods.
     *
     * @param dir the lateral direction
     * @return the nearest follower or null
     */
    public HeadwayGtu getFollower(final LateralDirectionality dir) {
        if (dir.isLeft()) {
            return getLeftFollower();
        } else if (dir.isRight()) {
            return getRightFollower();
        } else {
            return null;
        }
    }

    /**
     * Returns the nearest follower on the current lane.
     * Uses the cached follower list to extract the first element.
     *
     * @return the nearest follower or null
     */
    public HeadwayGtu getCurrentFollower() {
        HeadwayGtu cached = getCachedValue(CURRENT_FOLLOWER, HeadwayGtu.class);
        if (cached != null) {
            return cached;
        }

        Iterable<HeadwayGtu> followers = getFollowers(LateralDirectionality.NONE);
        Iterator<HeadwayGtu> it = followers.iterator();
        HeadwayGtu result = it.hasNext() ? it.next() : null;

        cacheValue(CURRENT_FOLLOWER, result, true);
        return result;
    }

    /**
     * Returns the nearest follower on the left lane.
     * Uses the cached follower list to extract the first element.
     *
     * @return the nearest follower or null
     */
    public HeadwayGtu getLeftFollower() {
        HeadwayGtu cached = getCachedValue(LEFT_FOLLOWER, HeadwayGtu.class);
        if (cached != null) {
            return cached;
        }

        Iterable<HeadwayGtu> followers = getFollowers(LateralDirectionality.LEFT);
        Iterator<HeadwayGtu> it = followers.iterator();
        HeadwayGtu result = it.hasNext() ? it.next() : null;

        cacheValue(LEFT_FOLLOWER, result, true);
        return result;
    }

    /**
     * Returns the nearest follower on the right lane.
     * Uses the cached follower list to extract the first element.
     *
     * @return the nearest follower or null
     */
    public HeadwayGtu getRightFollower() {
        HeadwayGtu cached = getCachedValue(RIGHT_FOLLOWER, HeadwayGtu.class);
        if (cached != null) {
            return cached;
        }

        Iterable<HeadwayGtu> followers = getFollowers(LateralDirectionality.RIGHT);
        Iterator<HeadwayGtu> it = followers.iterator();
        HeadwayGtu result = it.hasNext() ? it.next() : null;

        cacheValue(RIGHT_FOLLOWER, result, true);
        return result;
    }

    /**
     * Safely computes ego deceleration handling exceptions internally.
     *
     * @param dir the lateral direction
     * @return the computed deceleration or NaN on failure
     */
    private Acceleration computeSafeEgoDecel(final LateralDirectionality dir) {
        try {
            return computeLaneChangeEgoDeceleration(dir);
        } catch (Exception e) {
            return new Acceleration(Double.NaN, AccelerationUnit.SI);
        }
    }

    /**
     * Safely computes follower deceleration handling exceptions internally.
     *
     * @param dir the lateral direction
     * @return the computed deceleration or NaN on failure
     */
    private Acceleration computeSafeFollowerDecel(final LateralDirectionality dir) {
        try {
            return computeLaneChangeFollowerDeceleration(dir);
        } catch (Exception e) {
            return new Acceleration(Double.NaN, AccelerationUnit.SI);
        }
    }

    /**
     * Internal logic for computing front gap distance.
     *
     * @param dir the lateral direction
     * @return the gap distance
     */
    private Length computeFrontGapDistance(final LateralDirectionality dir) {
        try {
            if (dir.isLeft()) {
                HeadwayGtu leftLeader = getLeftLeader();
                return leftLeader != null ? leftLeader.getDistance() : Length.POSITIVE_INFINITY;
            } else if (dir.isRight()) {
                HeadwayGtu rightLeader = getRightLeader();
                return rightLeader != null ? rightLeader.getDistance() : Length.POSITIVE_INFINITY;
            } else {
                HeadwayGtu leaderHeadway = getCurrentLeader();
                return leaderHeadway != null ? leaderHeadway.getDistance() : Length.POSITIVE_INFINITY;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Computes the speed difference between the ego vehicle and the leader in the specified direction.
     * A positive value indicates that the ego is faster than the leader, while a negative value indicates it is slower.
     *
     * @param dir the lateral direction to check (LEFT, RIGHT, or NONE for current lane)
     * @return the speed difference (ego speed minus leader speed) or null if computation fails
     */
    private Speed computeFrontGapDeltaSpeed(final LateralDirectionality dir) {
        try {
            Speed egoSpeed = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed();
            if (dir.isLeft()) {
                HeadwayGtu leftLeader = getLeftLeader();
                return leftLeader != null ? egoSpeed.minus(leftLeader.getSpeed()) : Speed.NEGATIVE_INFINITY;
            } else if (dir.isRight()) {
                HeadwayGtu rightLeader = getRightLeader();
                return rightLeader != null ? egoSpeed.minus(rightLeader.getSpeed()) : Speed.NEGATIVE_INFINITY;
            } else {
                HeadwayGtu leaderHeadway = getCurrentLeader();
                return leaderHeadway != null ? egoSpeed.minus(leaderHeadway.getSpeed()) : Speed.NEGATIVE_INFINITY;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Internal logic for computing front gap time headway.
     *
     * @param dir the lateral direction
     * @return the time headway
     */
    private Duration computeFrontGapTimeHeadway(final LateralDirectionality dir) {
        try {
            Speed egoSpeed = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed();
            if (egoSpeed.le(Speed.ZERO)) {
                return Duration.POSITIVE_INFINITY;
            }

            if (dir.isLeft()) {
                HeadwayGtu leftLeader = getLeftLeader();
                return leftLeader != null ? Duration.instantiateSI(leftLeader.getDistance().si / egoSpeed.si) : Duration.POSITIVE_INFINITY;
            } else if (dir.isRight()) {
                HeadwayGtu rightLeader = getRightLeader();
                return rightLeader != null ? Duration.instantiateSI(rightLeader.getDistance().si / egoSpeed.si) : Duration.POSITIVE_INFINITY;
            } else {
                HeadwayGtu leaderHeadway = getCurrentLeader();
                return leaderHeadway != null ? Duration.instantiateSI(leaderHeadway.getDistance().si / egoSpeed.si) : Duration.POSITIVE_INFINITY;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Internal logic for computing rear gap distance.
     *
     * @param dir the lateral direction
     * @return the gap distance
     */
    private Length computeRearGapDistance(final LateralDirectionality dir) {
        try {
            if (dir.isLeft()) {
                HeadwayGtu leftFollower = getLeftFollower();
                return leftFollower != null ? leftFollower.getDistance() : Length.POSITIVE_INFINITY;
            } else if (dir.isRight()) {
                HeadwayGtu rightFollower = getRightFollower();
                return rightFollower != null ? rightFollower.getDistance() : Length.POSITIVE_INFINITY;
            } else {
                HeadwayGtu followerHeadway = getCurrentFollower();
                return followerHeadway != null ? followerHeadway.getDistance() : Length.POSITIVE_INFINITY;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Internal logic for computing rear gap delta speed.
     *
     * @param dir the lateral direction
     * @return the delta speed
     */
    private Speed computeRearGapDeltaSpeed(final LateralDirectionality dir) {
        try {
            Speed egoSpeed = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed();
            if (dir.isLeft()) {
                HeadwayGtu leftFollower = getLeftFollower();
                return leftFollower != null ? egoSpeed.minus(leftFollower.getSpeed()) : Speed.ZERO;
            } else if (dir.isRight()) {
                HeadwayGtu rightFollower = getRightFollower();
                return rightFollower != null ? egoSpeed.minus(rightFollower.getSpeed()) : Speed.ZERO;
            } else {
                HeadwayGtu followerHeadway = getCurrentFollower();
                return followerHeadway != null ? egoSpeed.minus(followerHeadway.getSpeed()) : Speed.ZERO;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Internal logic for computing rear gap time headway.
     *
     * @param dir the lateral direction
     * @return the time headway
     */
    private Duration computeRearGapTimeHeadway(final LateralDirectionality dir) {
        try {
            HeadwayGtu follower;
            if (dir.isLeft()) {
                follower = getLeftFollower();
            } else if (dir.isRight()) {
                follower = getRightFollower();
            } else {
                follower = getCurrentFollower();
            }

            if (follower != null) {
                Speed followerSpeed = getFollower(dir).getSpeed();
                if (followerSpeed.gt(Speed.ZERO)) {
                    return Duration.instantiateSI(follower.getDistance().si / followerSpeed.si);
                }
            }
            return Duration.POSITIVE_INFINITY;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks and lazily returns if there is a right-side overtaking situation ahead of the ego vehicle.
     *
     * @return true if overtaking on the right is imminent, false otherwise
     */
    public Boolean getRightSideOvertakingAhead() {
        Boolean cached = getCachedValue(RIGHT_SIDE_OVERTAKING_AHEAD, Boolean.class);
        if (cached != null) {
            return cached;
        }

        Boolean result = checkRightSideOvertakingAhead();
        cacheValue(RIGHT_SIDE_OVERTAKING_AHEAD, result, true);
        return result;
    }

    /**
     * Checks if there is a right-side overtaking situation ahead of the ego vehicle.
     * <p>
     * This method evaluates the speed and distance of vehicles in the left and current lanes
     * to determine if the ego vehicle is set to overtake a slower vehicle in the left lane.
     * </p>
     *
     * @return true if a right-side overtaking situation is detected, false otherwise
     */
    public Boolean checkRightSideOvertakingAhead() {
        try {
            Speed leftSpeedDelta = getFrontGapDeltaSpeed(LateralDirectionality.LEFT);
            Speed egoSpeed = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed();

            // german law allows right-side overtaking only if the left vehicle is at least 20 km/h slower and ego is not exceeding 60 km/h (StVO §5(4))
            // for now, we assume strict adherence to this rule (because we are german)
            if (leftSpeedDelta.le(new Speed(20.0, SpeedUnit.KM_PER_HOUR)) && egoSpeed.le(new Speed(60.0, SpeedUnit.KM_PER_HOUR))) {
                return false;
            }

            Length leftDistance = getFrontGapDistance(LateralDirectionality.LEFT);
            Duration leftTTC = leftDistance.divide(leftSpeedDelta.abs());
            Duration thresholdTTC = this.vehicle.getParameters().getParameter(MirovaParameters.undercuttingTTCThreshold);

            return leftSpeedDelta.gt(Speed.ZERO) && leftTTC.lt(thresholdTTC);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Determines if a lane change is feasible in the specified direction.
     * <p>
     * The feasibility check considers the required decelerations for both
     * the ego vehicle and the follower on the target lane, as well as
     * the available headways (gaps) in front of and behind the ego vehicle.
     * </p>
     *
     * @param laneChangeDirection the intended lane change direction (LEFT or RIGHT)
     * @return true if the lane change is feasible, false otherwise
     * @throws ParameterException if parameter retrieval fails
     * @throws NetworkException   if network parsing fails
     * @throws GtuException       if GTU access fails
     */
    public Boolean checkIfLaneChangeIsPossible(final LateralDirectionality laneChangeDirection) throws ParameterException, GtuException, NetworkException {

        InfrastructureContext infraCtx = this.vehicle.getContext(InfrastructureContext.class);
        Boolean laneChangeLegal = infraCtx.getIfLaneAvailable(laneChangeDirection);
        if (getFollower(laneChangeDirection) == null && getLeader(laneChangeDirection) == null && laneChangeLegal) {
            // if there is no follower, we can be a bit more lenient on the gaps and decelerations, so we consider lane change possible
            return true;
        }

        EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);

        Acceleration followerDecelThreshold = egoCtx.getFollowerDecelerationThreshold(laneChangeDirection);
        Acceleration egoDecelThreshold = egoCtx.getEgoDecelerationThreshold(laneChangeDirection);
        Double reductionFactor = this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange);

        Acceleration egoDecel = getEgoDeceleration(laneChangeDirection);
        Acceleration followerDecel = getFollowerDeceleration(laneChangeDirection);

        Length vehicleLength = this.vehicle.getGtu().getLength();

        Length desiredRearHeadway = egoCtx.getDesiredRearHeadway(laneChangeDirection).times(reductionFactor);
        Length rearHeadway = getRearGapDistance(laneChangeDirection);

        Length desiredFrontHeadway = Length.max(egoCtx.getDesiredFrontHeadway(laneChangeDirection).times(reductionFactor), vehicleLength);
        Length frontHeadway = getFrontGapDistance(laneChangeDirection);

        return laneChangeLegal
                && egoDecel.gt(egoDecelThreshold)
                && followerDecel.gt(followerDecelThreshold)
                && rearHeadway.gt(desiredRearHeadway)
                && frontHeadway.gt(desiredFrontHeadway);
    }

    /**
     * Lazily evaluates and caches if a lane change is physically and safely possible in a given direction.
     *
     * @param dir the intended lane change direction
     * @return true if possible, false otherwise
     * @throws GtuException     if GTU limits fail
     * @throws NetworkException if network queries fail
     */
    public Boolean getIfLaneChangePossible(final LateralDirectionality dir) throws GtuException, NetworkException {
        String name = dir.isLeft() ? LANE_CHANGE_POSSIBLE_LEFT : LANE_CHANGE_POSSIBLE_RIGHT;
        Boolean cached = getCachedValue(name, Boolean.class);
        if (cached != null) {
            return cached;
        }

        Boolean result;
        try {
            result = checkIfLaneChangeIsPossible(dir);
        } catch (ParameterException e) {
            result = false;
        }
        cacheValue(name, result, true);
        return result;
    }

    @Override
    public String toString() {
        return "NeighborsContext[" +
                "egoDecelLeft=" + getCachedValue(EGO_DECEL_LEFT, Acceleration.class) +
                ", egoDecelRight=" + getCachedValue(EGO_DECEL_RIGHT, Acceleration.class) +
                ", followerDecelLeft=" + getCachedValue(FOLLOWER_DECEL_LEFT, Acceleration.class) +
                ", followerDecelRight=" + getCachedValue(FOLLOWER_DECEL_RIGHT, Acceleration.class) +
                ", frontGapDistanceCurrent=" + getCachedValue(FRONT_GAP_DISTANCE_CURRENT, Length.class) +
                ", frontGapDistanceLeft=" + getCachedValue(FRONT_GAP_DISTANCE_LEFT, Length.class) +
                ", frontGapDistanceRight=" + getCachedValue(FRONT_GAP_DISTANCE_RIGHT, Length.class) +
                ", frontGapDeltaSpeedCurrent=" + getCachedValue(FRONT_GAP_DELTA_SPEED_CURRENT, Speed.class) +
                ", frontGapDeltaSpeedLeft=" + getCachedValue(FRONT_GAP_DELTA_SPEED_LEFT, Speed.class) +
                ", frontGapDeltaSpeedRight=" + getCachedValue(FRONT_GAP_DELTA_SPEED_RIGHT, Speed.class) +
                ", frontGapTimeHeadwayCurrent=" + getCachedValue(FRONT_GAP_TIME_HEADWAY_CURRENT, Duration.class) +
                ", frontGapTimeHeadwayLeft=" + getCachedValue(FRONT_GAP_TIME_HEADWAY_LEFT, Duration.class) +
                ", frontGapTimeHeadwayRight=" + getCachedValue(FRONT_GAP_TIME_HEADWAY_RIGHT, Duration.class) +
                ", rearGapDistanceCurrent=" + getCachedValue(REAR_GAP_DISTANCE_CURRENT, Length.class) +
                ", rearGapDistanceLeft=" + getCachedValue(REAR_GAP_DISTANCE_LEFT, Length.class) +
                ", rearGapDistanceRight=" + getCachedValue(REAR_GAP_DISTANCE_RIGHT, Length.class) +
                ", rearGapDeltaSpeedCurrent=" + getCachedValue(REAR_GAP_DELTA_SPEED_CURRENT, Speed.class) +
                ", rearGapDeltaSpeedLeft=" + getCachedValue(REAR_GAP_DELTA_SPEED_LEFT, Speed.class) +
                ", rearGapDeltaSpeedRight=" + getCachedValue(REAR_GAP_DELTA_SPEED_RIGHT, Speed.class) +
                ", rearGapTimeHeadwayCurrent=" + getCachedValue(REAR_GAP_TIME_HEADWAY_CURRENT, Duration.class) +
                ", rearGapTimeHeadwayLeft=" + getCachedValue(REAR_GAP_TIME_HEADWAY_LEFT, Duration.class) +
                ", rearGapTimeHeadwayRight=" + getCachedValue(REAR_GAP_TIME_HEADWAY_RIGHT, Duration.class) +
                ", rightSideOvertakingAhead=" + getCachedValue(RIGHT_SIDE_OVERTAKING_AHEAD, Boolean.class) +
                ", laneChangePossibleLeft=" + getCachedValue(LANE_CHANGE_POSSIBLE_LEFT, Boolean.class) +
                ", laneChangePossibleRight=" + getCachedValue(LANE_CHANGE_POSSIBLE_RIGHT, Boolean.class) +
                "]";
    }

    @Override
    public void updateFromPerception(final MirovaTacticalPlanner vehicle) {
        // Lazy: no global update required
        markCacheValid();
    }
}
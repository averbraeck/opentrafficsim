package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectDefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
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
 * Computes and stores indicators for lane-change feasibility, such as:
 * <ul>
 *   <li>Deceleration required by the ego vehicle on the target lane.</li>
 *   <li>Deceleration required by the follower in the target lane.</li>
 * </ul>
 * These indicators are computed separately for both left and right directions,
 * but only if a lane change in that direction is currently legal and possible.
 * </p>
 */
public class NeighborsContext extends ContextCategory implements UpdatableContext {

    public static final String EGO_DECEL_LEFT = "egoDecel_LEFT";
    public static final String EGO_DECEL_RIGHT = "egoDecel_RIGHT";
    public static final String FOLLOWER_DECEL_LEFT = "followerDecel_LEFT";
    public static final String FOLLOWER_DECEL_RIGHT = "followerDecel_RIGHT";
    public static final String FRONT_GAP_DISTANCE_CURRENT = "frontGapDistanceCurrent";
    public static final String FRONT_GAP_DISTANCE_LEFT = "frontGapDistanceLeft";
    public static final String FRONT_GAP_DISTANCE_RIGHT = "frontGapDistanceRight";
    public static final String FRONT_GAP_DELTA_SPEED_CURRENT = "frontGapDeltaSpeedCurrent";
    public static final String FRONT_GAP_DELTA_SPEED_LEFT = "frontGapDeltaSpeedLeft";
    public static final String FRONT_GAP_DELTA_SPEED_RIGHT = "frontGapDeltaSpeedRight";
    public static final String FRONT_GAP_TIME_HEADWAY_CURRENT = "frontGapTimeHeadwayCurrent";
    public static final String FRONT_GAP_TIME_HEADWAY_LEFT = "frontGapTimeHeadwayLeft";
    public static final String FRONT_GAP_TIME_HEADWAY_RIGHT = "frontGapTimeHeadwayRight";
    public static final String CURRENT_LEADER = "currentLeader";
    public static final String LEFT_LEADER = "leftLeader";
    public static final String RIGHT_LEADER = "rightLeader";
    public static final String CURRENT_FOLLOWER = "currentFollower";
    public static final String LEFT_FOLLOWER = "leftFollower";
    public static final String RIGHT_FOLLOWER = "rightFollower";
    public static final String LEADERS_CURRENT = "leaders_CURRENT";
    public static final String LEADERS_LEFT = "leaders_LEFT";
    public static final String LEADERS_RIGHT = "leaders_RIGHT";
    public static final String FOLLOWERS_CURRENT = "followers_CURRENT";
    public static final String FOLLOWERS_LEFT = "followers_LEFT";
    public static final String FOLLOWERS_RIGHT = "followers_RIGHT";
    public static final String REAR_GAP_DISTANCE_CURRENT = "rearGapDistanceCurrent";
    public static final String REAR_GAP_DISTANCE_LEFT = "rearGapDistanceLeft";
    public static final String REAR_GAP_DISTANCE_RIGHT = "rearGapDistanceRight";
    public static final String REAR_GAP_DELTA_SPEED_CURRENT = "rearGapDeltaSpeedCurrent";
    public static final String REAR_GAP_DELTA_SPEED_LEFT = "rearGapDeltaSpeedLeft";
    public static final String REAR_GAP_DELTA_SPEED_RIGHT = "rearGapDeltaSpeedRight";
    public static final String REAR_GAP_TIME_HEADWAY_CURRENT = "rearGapTimeHeadwayCurrent";
    public static final String REAR_GAP_TIME_HEADWAY_LEFT = "rearGapTimeHeadwayLeft";
    public static final String REAR_GAP_TIME_HEADWAY_RIGHT = "rearGapTimeHeadwayRight";
    public static final String RIGHT_SIDE_OVERTAKING_AHEAD = "rightSideOvertakingAhead";
    public static final String LANE_CHANGE_POSSIBLE_LEFT = "laneChangePossibleLeft";
    public static final String LANE_CHANGE_POSSIBLE_RIGHT = "laneChangePossibleRight";



    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

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
     * <p>
     * The current legal speed limit is retrieved from the cached
     * {@link InfrastructureContext}, ensuring consistency across all
     * context-dependent computations.
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
        var neighbors = this.vehicle.getPerception().getPerceptionCategory(NeighborsPerception.class);
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
     * <p>
     * Uses cached infrastructure information from {@link InfrastructureContext}
     * to ensure consistent speed limit evaluation across both vehicles.
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
        var neighbors = this.vehicle.getPerception().getPerceptionCategory(NeighborsPerception.class);

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

    public Acceleration getEgoDeceleration(final LateralDirectionality dir) {
        String name;
        if (dir.isLeft()) {
            name = EGO_DECEL_LEFT;
        } else {
            name = EGO_DECEL_RIGHT;
        }

        Acceleration cached = getCachedValue(name, Acceleration.class);
        if (cached != null) return cached;

        Acceleration result = computeSafeEgoDecel(dir);
        cacheValue(name, result, true);
        return result;
    }

    public Acceleration getFollowerDeceleration(final LateralDirectionality dir) {
        String name;
        if (dir.isLeft()) {
            name = FOLLOWER_DECEL_LEFT;
        } else {
            name = FOLLOWER_DECEL_RIGHT;
        }
        Acceleration cached = getCachedValue(name, Acceleration.class);
        if (cached != null) return cached;

        Acceleration result = computeSafeFollowerDecel(dir);
        cacheValue(name, result, true);
        return result;
    }

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
        if (cached != null) return cached;

        Length result = computeFrontGapDistance(dir);
        cacheValue(name, result, true);
        return result;
    }

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
        if (cached != null) return cached;

        Speed result = computeFrontGapDeltaSpeed(dir);
        cacheValue(name, result, true);
        return result;
    }

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
        if (cached != null) return cached;

        Duration result = computeFrontGapTimeHeadway(dir);
        cacheValue(name, result, true);
        return result;
    }

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
        if (cached != null) return cached;

        Length result = computeRearGapDistance(dir);
        cacheValue(name, result, true);
        return result;
        }

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
        if (cached != null) return cached;

        Speed result = computeRearGapDeltaSpeed(dir);
        cacheValue(name, result, true);
        return result;
    }

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
        if (cached != null) return cached;

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
        if (cached != null) return cached;

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
        if (cached != null) return cached;

        try {
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> result =
                    this.vehicle.getPerception().getPerceptionCategory(NeighborsPerception.class).getFollowers(lane);
            cacheValue(key, result, true);
            return result;
        } catch (OperationalPlanException e) {
            return Collections.emptyList();
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
        if (dir.isLeft()) return getLeftLeader();
        else if (dir.isRight()) return getRightLeader();
        else return getCurrentLeader();
    }

    /**
     * Returns the nearest leader on the current lane.
     * Uses the cached leader list to extract the first element.
     *
     * @return the nearest leader or null
     */
    public HeadwayGtu getCurrentLeader() {
        HeadwayGtu cached = getCachedValue(CURRENT_LEADER, HeadwayGtu.class);
        if (cached != null) return cached;

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
        if (cached != null) return cached;

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
        if (cached != null) return cached;

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
        if (dir.isLeft()) return getLeftFollower();
        else if (dir.isRight()) return getRightFollower();
        else return null;
    }

    /**
     * Returns the nearest follower on the current lane.
     * Uses the cached follower list to extract the first element.
     *
     * @return the nearest follower or null
     */
    public HeadwayGtu getCurrentFollower() {
        HeadwayGtu cached = getCachedValue(CURRENT_FOLLOWER, HeadwayGtu.class);
        if (cached != null) return cached;

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
        if (cached != null) return cached;

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
        if (cached != null) return cached;

        Iterable<HeadwayGtu> followers = getFollowers(LateralDirectionality.RIGHT);
        Iterator<HeadwayGtu> it = followers.iterator();
        HeadwayGtu result = it.hasNext() ? it.next() : null;

        cacheValue(RIGHT_FOLLOWER, result, true);
        return result;
    }

    private Acceleration computeSafeEgoDecel(final LateralDirectionality dir) {
        try {
            return computeLaneChangeEgoDeceleration(dir);
        } catch (Exception e) {
            return new Acceleration(Double.NaN, AccelerationUnit.SI);
        }
    }

    private Acceleration computeSafeFollowerDecel(final LateralDirectionality dir) {
        try {
            return computeLaneChangeFollowerDeceleration(dir);
        } catch (Exception e) {
            return new Acceleration(Double.NaN, AccelerationUnit.SI);
        }
    }

    private Length computeFrontGapDistance(final LateralDirectionality dir) {
        try {
            if (dir.isLeft()) {
                HeadwayGtu leftLeader = getLeftLeader();
                if (leftLeader != null) {
                    return leftLeader.getDistance();
                } else {
                    return Length.POSITIVE_INFINITY;
                }
            } else if (dir.isRight()) {
                HeadwayGtu rightLeader = getRightLeader();
                if (rightLeader != null) {
                    return rightLeader.getDistance();
                } else {
                    return Length.POSITIVE_INFINITY;
                }
            }
            else {
                HeadwayGtu leaderHeadway = getCurrentLeader();
                if (leaderHeadway == null) {
                    return Length.POSITIVE_INFINITY;
                }
                else {
                    return leaderHeadway.getDistance();
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Speed computeFrontGapDeltaSpeed(final LateralDirectionality dir) {
        try {
            if (dir.isLeft()) {
                HeadwayGtu leftLeader = getLeftLeader();
                if (leftLeader != null) {
                    return this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed().minus(leftLeader.getSpeed());
                } else {
                    return Speed.ZERO;
                }
            } else if (dir.isRight()) {
                HeadwayGtu rightLeader = getRightLeader();
                if (rightLeader != null) {
                    return this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed().minus(rightLeader.getSpeed());
                } else {
                    return Speed.ZERO;
                }
            } else {
                HeadwayGtu leaderHeadway = getCurrentLeader();
                if (leaderHeadway != null) {
                    return this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed().minus(leaderHeadway.getSpeed());
                } else {
                    return Speed.ZERO;
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Duration computeFrontGapTimeHeadway(final LateralDirectionality dir) {
        try {
            if (dir.isLeft()) {
                HeadwayGtu leftLeader = getLeftLeader();
                if (leftLeader != null) {
                    Speed egoSpeed = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed();
                    if (egoSpeed.gt(Speed.ZERO)) {
                        return Duration.instantiateSI(leftLeader.getDistance().si / egoSpeed.si);
                    } else {
                        return Duration.POSITIVE_INFINITY;
                    }
                } else {
                    return Duration.POSITIVE_INFINITY;
                }
            } else if (dir.isRight()) {
                HeadwayGtu rightLeader = getRightLeader();
                if (rightLeader != null) {
                    Speed egoSpeed = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed();
                    if (egoSpeed.gt(Speed.ZERO)) {
                        return Duration.instantiateSI(rightLeader.getDistance().si / egoSpeed.si);
                    } else {
                        return Duration.POSITIVE_INFINITY;
                    }
                } else {
                    return Duration.POSITIVE_INFINITY;
                }
            } else {
                HeadwayGtu leaderHeadway = getCurrentLeader();
                if (leaderHeadway != null) {
                    Speed egoSpeed = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed();
                    if (egoSpeed.gt(Speed.ZERO)) {
                        return Duration.instantiateSI(leaderHeadway.getDistance().si / egoSpeed.si);
                    } else {
                        return Duration.POSITIVE_INFINITY;
                    }
                } else {
                    return Duration.POSITIVE_INFINITY;
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Length computeRearGapDistance(final LateralDirectionality dir) {
        try {
            if (dir.isLeft()) {
                HeadwayGtu leftFollower = getLeftFollower();
                if (leftFollower != null) {
                    return leftFollower.getDistance();
                } else {
                    return Length.POSITIVE_INFINITY;
                }
            } else if (dir.isRight()) {
                HeadwayGtu rightFollower = getRightFollower();
                if (rightFollower != null) {
                    return rightFollower.getDistance();
                } else {
                    return Length.POSITIVE_INFINITY;
                }
            }
            else {
                HeadwayGtu followerHeadway = getCurrentFollower();
                if (followerHeadway == null) {
                    return Length.POSITIVE_INFINITY;
                }
                else {
                    return followerHeadway.getDistance();
                }
                }
        } catch (Exception e) {
            return null;
        }
    }

    private Speed computeRearGapDeltaSpeed(final LateralDirectionality dir) {
        try {
            if (dir.isLeft()) {
                HeadwayGtu leftFollower = getLeftFollower();
                if (leftFollower != null) {
                    return this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed().minus(leftFollower.getSpeed());
                } else {
                    return Speed.ZERO;
                }
            } else if (dir.isRight()) {
                HeadwayGtu rightFollower = getRightFollower();
                if (rightFollower != null) {
                    return this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed().minus(rightFollower.getSpeed());
                } else {
                    return Speed.ZERO;
                }
            } else {
                HeadwayGtu followerHeadway = getCurrentFollower();
                if (followerHeadway != null) {
                    return this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed().minus(followerHeadway.getSpeed());
                } else {
                    return Speed.ZERO;
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Duration computeRearGapTimeHeadway(final LateralDirectionality dir) {
        try {
            if (dir.isLeft()) {
                HeadwayGtu leftFollower = getLeftFollower();
                if (leftFollower != null) {
                    Speed followerSpeed = getFollower(dir).getSpeed();
                    if (followerSpeed.gt(Speed.ZERO)) {
                        return Duration.instantiateSI(leftFollower.getDistance().si / followerSpeed.si);
                    } else {
                        return Duration.POSITIVE_INFINITY;
                    }
                } else {
                    return Duration.POSITIVE_INFINITY;
                }
            } else if (dir.isRight()) {
                HeadwayGtu rightFollower = getRightFollower();
                if (rightFollower != null) {
                    Speed followerSpeed = getFollower(dir).getSpeed();
                    if (followerSpeed.gt(Speed.ZERO)) {
                        return Duration.instantiateSI(rightFollower.getDistance().si / followerSpeed.si);
                    } else {
                        return Duration.POSITIVE_INFINITY;
                    }
                } else {
                    return Duration.POSITIVE_INFINITY;
                }
            } else {
                HeadwayGtu followerHeadway = getCurrentFollower();
                if (followerHeadway != null) {
                    Speed followerSpeed = getFollower(dir).getSpeed();
                    if (followerSpeed.gt(Speed.ZERO)) {
                        return Duration.instantiateSI(followerHeadway.getDistance().si / followerSpeed.si);
                    } else {
                        return Duration.POSITIVE_INFINITY;
                    }
                } else {
                    return Duration.POSITIVE_INFINITY;
                }
            }
        } catch (Exception e) {
            return null;
        }
    }



    public Boolean getRightSideOvertakingAhead() {
        Boolean cached = getCachedValue(RIGHT_SIDE_OVERTAKING_AHEAD, Boolean.class);
        if (cached != null) return cached;

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
            Length currentDistance = getFrontGapDistance(LateralDirectionality.NONE);

            if (leftSpeedDelta.gt(Speed.ZERO) && leftDistance.lt(currentDistance)) {
                return true;
            }
            else {
                return false;
            }

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
     */
    public Boolean checkIfLaneChangeIsPossible(final LateralDirectionality laneChangeDirection) throws ParameterException {

        EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);
        InfrastructureContext infraCtx = this.vehicle.getContext(InfrastructureContext.class);
        Boolean laneChangeLegal = infraCtx.getIfLaneAvailable(laneChangeDirection);

        Acceleration followerDecelThreshold = egoCtx.getFollowerDecelerationThreshold(laneChangeDirection);
        Acceleration egoDecelThreshold = egoCtx.getEgoDecelerationThreshold(laneChangeDirection);
        Double reductionFactor = this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange);

        Acceleration egoDecel = getEgoDeceleration(laneChangeDirection);
        Acceleration followerDecel = getFollowerDeceleration(laneChangeDirection);

        Length desiredRearHeadway = egoCtx.getDesiredRearHeadway(laneChangeDirection).times(reductionFactor);
        Length rearHeadway = getRearGapDistance(laneChangeDirection);

        Length desiredFrontHeadway = egoCtx.getDesiredFrontHeadway(laneChangeDirection).times(reductionFactor);
        Length frontHeadway = getFrontGapDistance(laneChangeDirection);

        return egoDecel.gt(egoDecelThreshold)
                && followerDecel.gt(followerDecelThreshold)
                && rearHeadway.gt(desiredRearHeadway)
                && frontHeadway.gt(desiredFrontHeadway)
                && infraCtx.getIfLaneAvailable(laneChangeDirection);
    }

    public Boolean getIfLaneChangePossible(final LateralDirectionality dir) {
        String name;
        if (dir.isLeft()) {
            name = LANE_CHANGE_POSSIBLE_LEFT;
        } else {
            name = LANE_CHANGE_POSSIBLE_RIGHT;
        }
        Boolean cached = getCachedValue(name, Boolean.class);
        if (cached != null) return cached;

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
        // lazy: kein globales Update nötig
        markCacheValid();
    }
}

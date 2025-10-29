package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import java.util.EnumMap;
import java.util.Map;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes.AbstractMirovaVehicle;
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

    private static final String EGO_DECEL_LEFT = "egoDecel_LEFT";
    private static final String EGO_DECEL_RIGHT = "egoDecel_RIGHT";
    private static final String FOLLOWER_DECEL_LEFT = "followerDecel_LEFT";
    private static final String FOLLOWER_DECEL_RIGHT = "followerDecel_RIGHT";

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    public NeighborsContext(final AbstractMirovaVehicle vehicle) {
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
        var neighbors = this.vehicle.getLanePerception().getPerceptionCategory(NeighborsPerception.class);
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
        var neighbors = this.vehicle.getLanePerception().getPerceptionCategory(NeighborsPerception.class);

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

    @Override
    public String toString() {
        return "NeighborsContext[" +
                "egoDecelLeft=" + getCachedValue(EGO_DECEL_LEFT, Acceleration.class) +
                ", egoDecelRight=" + getCachedValue(EGO_DECEL_RIGHT, Acceleration.class) +
                ", followerDecelLeft=" + getCachedValue(FOLLOWER_DECEL_LEFT, Acceleration.class) +
                ", followerDecelRight=" + getCachedValue(FOLLOWER_DECEL_RIGHT, Acceleration.class) +
 "]";
    }

    @Override
    public void updateFromPerception(final AbstractMirovaVehicle vehicle) {
        // lazy: kein globales Update nötig
        markCacheValid();
    }
}

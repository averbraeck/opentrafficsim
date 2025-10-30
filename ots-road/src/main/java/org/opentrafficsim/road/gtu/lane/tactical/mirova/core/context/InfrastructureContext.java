package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import java.util.SortedSet;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.*;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.perception.*;
import org.opentrafficsim.road.gtu.lane.perception.categories.*;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.util.*;
import org.opentrafficsim.road.network.*;
import org.opentrafficsim.road.network.speed.*;

/**
 * Context category providing infrastructure-related information relevant for
 * longitudinal control and tactical reasoning.
 * <p>
 * Computes and lazily caches the following values:
 * <ul>
 *   <li>Remaining distance to the end of the current lane</li>
 *   <li>Legal and upcoming speed limits</li>
 *   <li>Recommended deceleration for transitions such as curves and speed bumps</li>
 * </ul>
 * <p>
 * All values are computed on demand and cached per simulation tick to minimize
 * redundant perception processing.
 * </p>
 */
public class InfrastructureContext extends ContextCategory implements UpdatableContext {

    /** Cache key for distance to lane end. */
    private static final String DIST_TO_LANE_END = "distToLaneEnd";
    /** Cache key for lane-end urgency flag. */
    private static final String LANE_END_URGENT = "laneEndUrgent";
    /** Cache key for current speed limit information. */
    private static final String CURRENT_SPEED_LIMIT = "currentSpeedLimit";
    /** Cache key for next speed limit information (look-ahead). */
    private static final String NEXT_SPEED_LIMIT = "nextSpeedLimit";
    /** Cache key for effective legal speed limit (minimum across limit types). */
    private static final String LEGAL_SPEED_LIMIT = "legalSpeedLimit";


    /** Distance threshold [m] below which a lane-end is considered critical. */
    private static final double LANE_END_THRESHOLD = 200.0;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Constructs a new {@code InfrastructureContext}.
     *
     * @param vehicle the ego vehicle associated with this context
     */
    public InfrastructureContext(final MirovaTacticalPlanner vehicle) {
        super("Infrastructure", vehicle);
    }

    // ----------------------------------------------------------------------
    // Lazy Accessors
    // ----------------------------------------------------------------------

    /**
     * Returns the remaining distance to the end of the current lane.
     * <p>
     * The value is lazily computed from {@link InfrastructurePerception}
     * and cached per simulation tick.
     * </p>
     *
     * @return remaining distance until the lane end [m]
     */
    public Length getDistanceToLaneEnd() {
        Length cached = getCachedValue(DIST_TO_LANE_END, Length.class);
        if (cached != null) return cached;
        Length result = computeSafeLaneEndDistance();
        cacheValue(DIST_TO_LANE_END, result, true);
        return result;
    }

    /**
     * Returns whether the current lane end is within the urgency threshold.
     * <p>
     * This flag can be used to trigger tactical actions such as mandatory
     * lane changes when the remaining lane distance falls below 200 m.
     * </p>
     *
     * @return {@code true} if the lane end is closer than {@value #LANE_END_THRESHOLD} m, else {@code false}
     */
    public Boolean isLaneEndUrgent() {
        Boolean cached = getCachedValue(LANE_END_URGENT, Boolean.class);
        if (cached != null) return cached;
        boolean urgent = getDistanceToLaneEnd().si < LANE_END_THRESHOLD;
        cacheValue(LANE_END_URGENT, urgent, true);
        return urgent;
    }

    /**
     * Returns the current applicable {@link SpeedLimitInfo} for the ego lane.
     *
     * @return current speed limit information object
     */
    public SpeedLimitInfo getCurrentSpeedLimit() {
        SpeedLimitInfo cached = getCachedValue(CURRENT_SPEED_LIMIT, SpeedLimitInfo.class);
        if (cached != null) return cached;
        SpeedLimitInfo info = computeSafeCurrentSpeedLimit();
        cacheValue(CURRENT_SPEED_LIMIT, info, true);
        return info;
    }

    /**
     * Returns the upcoming {@link SpeedLimitInfo}, approximately 200 meters ahead.
     * <p>
     * Useful for anticipating upcoming speed transitions before they are reached.
     * </p>
     *
     * @return speed limit information 200 meters ahead
     */
    public SpeedLimitInfo getNextSpeedLimit() {
        SpeedLimitInfo cached = getCachedValue(NEXT_SPEED_LIMIT, SpeedLimitInfo.class);
        if (cached != null) return cached;
        SpeedLimitInfo info = computeSafeNextSpeedLimit();
        cacheValue(NEXT_SPEED_LIMIT, info, true);
        return info;
    }

    /**
     * Returns the effective legal speed limit.
     * <p>
     * This is the minimum among all currently active limit types, such as
     * vehicle class restrictions, static signs, or dynamic signals.
     * </p>
     *
     * @return effective legal speed limit [m/s]
     */
    public Speed getLegalSpeedLimit() {
        Speed cached = getCachedValue(LEGAL_SPEED_LIMIT, Speed.class);
        if (cached != null) return cached;
        Speed limit = SpeedLimitUtil.getLegalSpeedLimit(getCurrentSpeedLimit());
        cacheValue(LEGAL_SPEED_LIMIT, limit, true);
        return limit;
    }


    // ----------------------------------------------------------------------
    // Safe computation wrappers
    // ----------------------------------------------------------------------

    /**
     * Wrapper for lane-end distance computation with exception safety.
     *
     * @return computed distance to lane end or {@link Length#POSITIVE_INFINITY} on error
     */
    private Length computeSafeLaneEndDistance() {
        try {
            return computeDistanceToLaneEnd();
        } catch (Exception e) {
            return Length.POSITIVE_INFINITY;
        }
    }

    /**
     * Wrapper for current speed limit lookup with exception safety.
     *
     * @return current {@link SpeedLimitInfo} or {@code null} if unavailable
     */
    private SpeedLimitInfo computeSafeCurrentSpeedLimit() {
        try {
            return computeSpeedLimitInfo(Length.ZERO);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Wrapper for next (look-ahead) speed limit lookup with exception safety.
     *
     * @return next {@link SpeedLimitInfo} or {@code null} if unavailable
     */
    private SpeedLimitInfo computeSafeNextSpeedLimit() {
        try {
            return computeSpeedLimitInfo(new Length(200.0, LengthUnit.SI));
        } catch (Exception e) {
            return null;
        }
    }


    // ----------------------------------------------------------------------
    // Core computation methods
    // ----------------------------------------------------------------------

    /**
     * Computes the remaining distance to the end of the current lane.
     * <p>
     * Uses {@link InfrastructurePerception#getLegalLaneChangeInfo(RelativeLane)} to
     * find the first relevant {@link LaneChangeInfo} instance and its remaining distance.
     * </p>
     *
     * @return distance to lane end [m]
     * @throws ParameterException if perception parameters are missing
     * @throws OperationalPlanException if lane information retrieval fails
     */
    private Length computeDistanceToLaneEnd() throws ParameterException, OperationalPlanException {
        InfrastructurePerception infra =
                this.vehicle.getPerception().getPerceptionCategory(InfrastructurePerception.class);

        SortedSet<LaneChangeInfo> laneInfo = infra.getLegalLaneChangeInfo(RelativeLane.CURRENT);
        if (!laneInfo.isEmpty()) {
            LaneChangeInfo first = laneInfo.first();
            if (first.numberOfLaneChanges() == 0) {
                return first.remainingDistance();
            }
        }
        return Length.POSITIVE_INFINITY;
    }

    /**
     * Computes the speed limit information at a given look-ahead distance.
     *
     * @param lookAhead distance ahead along the lane centerline
     * @return {@link SpeedLimitInfo} valid at the specified look-ahead position
     * @throws OperationalPlanException
     */
    private SpeedLimitInfo computeSpeedLimitInfo(final Length lookAhead) throws OperationalPlanException {
        InfrastructurePerception infra =
                this.vehicle.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        return infra.getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(lookAhead);
    }


    /**
     * Updates the cache validity for this context category.
     * <p>
     * No direct computations are performed here since all values are
     * evaluated lazily on demand.
     * </p>
     *
     * @param vehicle the ego vehicle (unused)
     */
    @Override
    public void updateFromPerception(final MirovaTacticalPlanner vehicle) {
        // Lazy: recomputed only when requested
        markCacheValid();
    }

    /**
     * Returns a compact textual summary of the currently cached values.
     *
     * @return a short summary string
     */
    @Override
    public String toString() {
        return "InfrastructureContext[" +
                "distToLaneEnd=" + getCachedValue(DIST_TO_LANE_END, Length.class) +
                ", legalSpeedLimit=" + getCachedValue(LEGAL_SPEED_LIMIT, Speed.class) +
                "]";
    }
}

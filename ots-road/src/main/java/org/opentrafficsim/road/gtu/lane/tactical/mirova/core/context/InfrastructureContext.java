package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import java.util.SortedSet;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.SpeedLimitUtil;
import org.opentrafficsim.road.network.LaneChangeInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Context category providing infrastructure-related information relevant for
 * longitudinal control and tactical reasoning.
 * <p>
 * Computes and lazily caches the following values:
 * <ul>
 * <li>Remaining distance to the end of the current and adjacent lanes</li>
 * <li>Legal and upcoming speed limits</li>
 * <li>Recommended deceleration for transitions such as curves and speed bumps</li>
 * </ul>
 * <p>
 * All values are computed on demand and cached per simulation tick to minimize
 * redundant perception processing.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class InfrastructureContext extends ContextCategory implements UpdatableContext
{

    /** Cache key prefix for distance to lane end. */
    private static final String DIST_TO_LANE_END_PREFIX = "distToLaneEnd_";
    /** Cache key for lane-end urgency flag. */
    private static final String LANE_END_URGENT = "laneEndUrgent";
    /** Cache key for current speed limit information. */
    private static final String CURRENT_SPEED_LIMIT = "currentSpeedLimit";
    /** Cache key for next speed limit information (look-ahead). */
    private static final String NEXT_SPEED_LIMIT = "nextSpeedLimit";
    /** Cache key for effective legal speed limit (minimum across limit types). */
    private static final String LEGAL_SPEED_LIMIT = "legalSpeedLimit";
    /** Cache key for left lane availability. */
    private static final String LEFT_LANE_AVAILABLE = "leftLaneAvailable";
    /** Cache key for right lane availability. */
    private static final String RIGHT_LANE_AVAILABLE = "rightLaneAvailable";
    /** Cache key for distance to next legal lane change opportunity with extended look-ahead. */
    private static final String DIST_TO_NEXT_LANE_CHANGE_OPPORTUNITY = "distanceToNextLaneChangeOpportunity";

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
    public InfrastructureContext(final MirovaTacticalPlanner vehicle)
    {
        super("Infrastructure", vehicle);
    }

    // ----------------------------------------------------------------------
    // Lazy Accessors
    // ----------------------------------------------------------------------

    /**
     * Returns the remaining distance to the end of the current lane.
     *
     * @return remaining distance until the current lane ends [m]
     */
    public Length getDistanceToLaneEnd()
    {
        return getDistanceToLaneEnd(RelativeLane.CURRENT);
    }

    /**
     * Returns the remaining distance to the end of the specified lane.
     * <p>
     * The value is lazily computed from {@link InfrastructurePerception}
     * and cached per simulation tick.
     * </p>
     *
     * @param lane the relative lane to check (e.g., CURRENT, LEFT, RIGHT)
     * @return remaining distance until the lane ends [m]
     */
    public Length getDistanceToLaneEnd(final RelativeLane lane)
    {
        String key = DIST_TO_LANE_END_PREFIX + lane.toString();
        Length cached = getCachedValue(key, Length.class);
        if (cached != null)
        {
            return cached;
        }
        Length result = computeSafeLaneEndDistance(lane);
        cacheValue(key, result, true);
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
    public Boolean isLaneEndUrgent()
    {
        Boolean cached = getCachedValue(LANE_END_URGENT, Boolean.class);
        if (cached != null)
        {
            return cached;
        }
        boolean urgent = getDistanceToLaneEnd(RelativeLane.CURRENT).si < LANE_END_THRESHOLD;
        cacheValue(LANE_END_URGENT, urgent, true);
        return urgent;
    }

    /**
     * Returns the current applicable {@link SpeedLimitInfo} for the ego lane.
     *
     * @return current speed limit information object
     */
    public SpeedLimitInfo getCurrentSpeedLimit()
    {
        SpeedLimitInfo cached = getCachedValue(CURRENT_SPEED_LIMIT, SpeedLimitInfo.class);
        if (cached != null)
        {
            return cached;
        }
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
    public SpeedLimitInfo getNextSpeedLimit()
    {
        SpeedLimitInfo cached = getCachedValue(NEXT_SPEED_LIMIT, SpeedLimitInfo.class);
        if (cached != null)
        {
            return cached;
        }
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
    public Speed getLegalSpeedLimit()
    {
        Speed cached = getCachedValue(LEGAL_SPEED_LIMIT, Speed.class);
        if (cached != null)
        {
            return cached;
        }
        Speed limit = SpeedLimitUtil.getLegalSpeedLimit(getCurrentSpeedLimit());
        cacheValue(LEGAL_SPEED_LIMIT, limit, true);
        return limit;
    }

    /**
     * Returns whether a lane change is currently legally permitted in the specified direction.
     * <p>
     * The result is cached per simulation tick to avoid redundant perception queries.
     * </p>
     *
     * @param laneChangeDirection direction of the lane change
     * @return {@code true} if a lane change is allowed, else {@code false}
     */
    public boolean getIfLaneAvailable(final LateralDirectionality laneChangeDirection)
    {
        String cacheKey = laneChangeDirection == LateralDirectionality.LEFT ? LEFT_LANE_AVAILABLE : RIGHT_LANE_AVAILABLE;
        Boolean cached = getCachedValue(cacheKey, Boolean.class);
        if (cached != null)
        {
            return cached;
        }
        boolean available = checkLaneAvailable(laneChangeDirection);
        cacheValue(cacheKey, available, true);
        return available;
    }

    /**
     * Returns the distance to the next legal lane change opportunity in the current lane.
     * <p>
     * This method uses an extended look-ahead distance defined in {@link MirovaParameters}
     * to ensure that lane change information is retrieved for a sufficiently long horizon.
     * The original look-ahead value is restored after retrieval.
     * </p>
     *
     * @return distance to next legal lane change opportunity [m], or {@link Length#POSITIVE_INFINITY} if none found
     */
    public Length getDistanceToLaneChangeExtendedLookahead()
    {
        Length cached = getCachedValue(DIST_TO_NEXT_LANE_CHANGE_OPPORTUNITY, Length.class);
        if (cached != null)
        {
            return cached;
        }
        Length distance = null;
        try
        {
            distance = distanceToLaneChangeExtendedLookahead();
        }
        catch (Exception e)
        {
            distance = Length.POSITIVE_INFINITY;
        }
        cacheValue(DIST_TO_NEXT_LANE_CHANGE_OPPORTUNITY, distance, true);
        return distance;
    }

    // ----------------------------------------------------------------------
    // Safe computation wrappers
    // ----------------------------------------------------------------------

    /**
     * Wrapper for lane-end distance computation with exception safety.
     *
     * @param lane the relative lane
     * @return computed distance to lane end or {@link Length#POSITIVE_INFINITY} on error
     */
    private Length computeSafeLaneEndDistance(final RelativeLane lane)
    {
        try
        {
            return computeDistanceToLaneEnd(lane);
        }
        catch (Exception e)
        {
            return Length.POSITIVE_INFINITY;
        }
    }

    /**
     * Wrapper for current speed limit lookup with exception safety.
     *
     * @return current {@link SpeedLimitInfo} or {@code null} if unavailable
     */
    private SpeedLimitInfo computeSafeCurrentSpeedLimit()
    {
        try
        {
            return computeSpeedLimitInfo(Length.ZERO);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Wrapper for next (look-ahead) speed limit lookup with exception safety.
     *
     * @return next {@link SpeedLimitInfo} or {@code null} if unavailable
     */
    private SpeedLimitInfo computeSafeNextSpeedLimit()
    {
        try
        {
            return computeSpeedLimitInfo(new Length(200.0, LengthUnit.SI));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    // ----------------------------------------------------------------------
    // Core computation methods
    // ----------------------------------------------------------------------

    /**
     * Computes the remaining distance to the end of the specified lane.
     * <p>
     * Uses {@link InfrastructurePerception#getLegalLaneChangeInfo(RelativeLane)} to
     * find the first relevant {@link LaneChangeInfo} instance and its remaining distance.
     * </p>
     *
     * @param lane the relative lane
     * @return distance to lane end [m]
     * @throws ParameterException if perception parameters are missing
     * @throws OperationalPlanException if lane information retrieval fails
     */
    private Length computeDistanceToLaneEnd(final RelativeLane lane) throws ParameterException, OperationalPlanException
    {
        InfrastructurePerception infra =
                this.vehicle.getPerception().getPerceptionCategory(InfrastructurePerception.class);

        SortedSet<LaneChangeInfo> laneInfo = infra.getLegalLaneChangeInfo(lane);
        if (laneInfo != null && !laneInfo.isEmpty())
        {
            LaneChangeInfo first = laneInfo.first();
            return first.remainingDistance();
        }
        return Length.POSITIVE_INFINITY;
    }

    /**
     * Computes the speed limit information at a given look-ahead distance.
     *
     * @param lookAhead distance ahead along the lane centerline
     * @return {@link SpeedLimitInfo} valid at the specified look-ahead position
     * @throws OperationalPlanException if perception fails
     */
    private SpeedLimitInfo computeSpeedLimitInfo(final Length lookAhead) throws OperationalPlanException
    {
        InfrastructurePerception infra =
                this.vehicle.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        return infra.getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(lookAhead);
    }

    /**
     * Checks whether a lane change is currently legally permitted in the specified direction.
     *
     * @param laneChangeDirection direction of the lane change
     * @return {@code true} if a lane change is allowed, else {@code false}
     */
    private boolean checkLaneAvailable(final LateralDirectionality laneChangeDirection)
    {
        InfrastructurePerception infra = null;
        try
        {
            infra = this.vehicle.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        }
        catch (OperationalPlanException exception)
        {
            exception.printStackTrace();
            return false;
        }
        // Check if the possibility distance is non-negative (meaning we are not past the allowed point)
        // and if the lane actually exists in the cross section.
        return infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, laneChangeDirection).si > 0.0;
    }

    /**
     * Computes the distance to the next legal lane change opportunity in the current lane.
     * <p>
     * This method temporarily overrides the look-ahead parameter to the extended value
     * defined in {@link MirovaParameters} to ensure that lane change information is retrieved
     * for a sufficiently long horizon. The original look-ahead value is restored after retrieval.
     * </p>
     *
     * @return distance to next legal lane change opportunity [m], or {@link Length#POSITIVE_INFINITY} if none found
     * @throws OperationalPlanException if perception fails
     * @throws ParameterException if parameter access fails
     */
    private Length distanceToLaneChangeExtendedLookahead() throws OperationalPlanException, ParameterException
    {
        InfrastructurePerception infra = this.vehicle.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        Length extendedLookaheadDistance = this.vehicle.getParameters().getParameter(MirovaParameters.extendedLookAheadDistance);
        this.vehicle.getParameters().setParameterResettable(ParameterTypes.LOOKAHEAD, extendedLookaheadDistance);
        SortedSet<LaneChangeInfo> laneInfo = infra.getLegalLaneChangeInfo(RelativeLane.CURRENT);
        this.vehicle.getParameters().resetParameter(ParameterTypes.LOOKAHEAD);
        if (laneInfo != null && !laneInfo.isEmpty())
        {
            LaneChangeInfo first = laneInfo.first();
            return first.remainingDistance();
        }
        return Length.POSITIVE_INFINITY;
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
    public void updateFromPerception(final MirovaTacticalPlanner vehicle)
    {
        // Lazy: recomputed only when requested
        markCacheValid();
    }


    /**
     * Returns a compact textual summary of the currently cached values.
     *
     * @return a short summary string
     */
    @Override
    public String toString()
    {
        return "InfrastructureContext[" +
                "distToLaneEnd=" + getCachedValue(DIST_TO_LANE_END_PREFIX + RelativeLane.CURRENT, Length.class) +
                ", legalSpeedLimit=" + getCachedValue(LEGAL_SPEED_LIMIT, Speed.class) +
                "]";
    }
}
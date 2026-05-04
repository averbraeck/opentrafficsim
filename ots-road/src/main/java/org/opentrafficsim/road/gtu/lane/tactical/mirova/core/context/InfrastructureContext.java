package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.immutablecollections.ImmutableList;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LanePathInfo;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.SpeedLimitUtil;
import org.opentrafficsim.road.network.LaneChangeInfo;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Context category providing infrastructure-related information relevant for longitudinal control and tactical reasoning.
 * <p>
 * Forms a central part of <b>Layer 1 (Perception & Context)</b> in the MiRoVA architecture. Computes and lazily caches the
 * following values:
 * <ul>
 * <li>Remaining distance to the end of the current and adjacent lanes</li>
 * <li>Legal and upcoming speed limits</li>
 * <li>Recommended deceleration for transitions such as curves and speed bumps</li>
 * </ul>
 * <p>
 * All values are computed on demand and cached per simulation tick to minimize redundant perception processing.
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

    /** Cache key prefix for anticipated adjacent lane drops. */
    private static final String ANTICIPATED_LANE_DROP_PREFIX = "anticipatedLaneDrop_";

    /** Cache key prefix for downstream adjacent lanes. */
    private static final String DOWNSTREAM_ADJACENT_LANE_PREFIX = "downstreamAdjacentLane_";

    /** Cache key for parallel merge detection on the left. */
    private static final String MERGE_CACHE_KEY_LEFT = "PARALLEL_MERGE_LEFT";

    /** Cache key for parallel merge detection on the right. */
    private static final String MERGE_CACHE_KEY_RIGHT = "PARALLEL_MERGE_RIGHT";

    /** Distance threshold [m] below which a lane-end is considered critical. */
    private static final double LANE_END_THRESHOLD = 200.0;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Constructs a new {@code InfrastructureContext}.
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
     * @return remaining distance until the current lane ends [m]
     */
    public Length getDistanceToLaneEnd()
    {
        return getDistanceToLaneEnd(RelativeLane.CURRENT);
    }

    /**
     * Returns the remaining distance to the end of the specified lane.
     * <p>
     * The value is lazily computed from {@link InfrastructurePerception} and cached per simulation tick.
     * </p>
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
     * This flag can be used to trigger tactical actions such as mandatory lane changes when the remaining lane distance falls
     * below 200 m.
     * </p>
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
     * This is the minimum among all currently active limit types, such as vehicle class restrictions, static signs, or dynamic
     * signals.
     * </p>
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
     * This method uses an extended look-ahead distance defined in {@link MirovaParameters} to ensure that lane change
     * information is retrieved for a sufficiently long horizon. The original look-ahead value is restored after retrieval.
     * </p>
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

    /**
     * Projects the tactical path forward to find the first downstream lane that provides an adjacent lane in the specified
     * direction.
     * <p>
     * This is essential for vehicles on a single-lane approach ramp to identify the main road lane they will eventually merge
     * into, long before the ramp physically runs parallel to the main road. Evaluated lazily and safely cached per simulation
     * tick.
     * </p>
     * @param direction the lateral direction of the target merge lane
     * @return the downstream adjacent lane on the main road, or {@code null} if none is found
     */
    public Lane getDownstreamAdjacentLane(final LateralDirectionality direction)
    {
        String key = DOWNSTREAM_ADJACENT_LANE_PREFIX + direction.name();
        DownstreamLaneInfo cached = getCachedValue(key, DownstreamLaneInfo.class);

        if (cached != null)
        {
            // Return the cached lane (which might correctly be null if none exists)
            return cached.getLane();
        }

        // Not in cache, compute strictly for the requested direction
        Lane computedLane = computeDownstreamAdjacentLane(direction);

        // Wrap the result in DownstreamLaneInfo so we can cache "null" findings safely
        cacheValue(key, new DownstreamLaneInfo(computedLane), true);

        return computedLane;
    }

    /**
     * Retrieves the anticipated {@link LaneDropInfo} for an upcoming lane drop on an adjacent lane.
     * <p>
     * This overcomes the myopic limit of standard perception by looking ahead along the tactical path to spot terminating lanes
     * (e.g. an on-ramp further downstream). Evaluated lazily and cached per direction.
     * </p>
     * @param direction the lateral direction to check (LEFT or RIGHT)
     * @return the lane drop info, or {@code null} if no drop exists within the horizon
     */
    public LaneDropInfo getAnticipatedLaneDropInfo(final LateralDirectionality direction)
    {
        String key = ANTICIPATED_LANE_DROP_PREFIX + direction.name();
        LaneDropInfo cached = getCachedValue(key, LaneDropInfo.class);

        if (cached != null)
        {
            // Translate our cached "Infinity" dummy back to null for the caller
            if (cached.getDistance().eq(Length.POSITIVE_INFINITY))
            {
                return null;
            }
            return cached;
        }

        // Not in cache, compute strictly for the requested direction
        LaneDropInfo computedDrop = computeAnticipatedLaneDrop(direction);

        // Cache the result. If no drop exists, cache a dummy to prevent redundant calculations in the same tick.
        if (computedDrop == null)
        {
            cacheValue(key, new LaneDropInfo(Length.POSITIVE_INFINITY, null), true);
        }
        else
        {
            cacheValue(key, computedDrop, true);
        }

        return computedDrop;
    }

    /**
     * Determines if there is a parallel merging lane ending within the look-ahead distance.
     * <p>
     * This method evaluates the infrastructure topology. The result is stored in the general context cache to prevent redundant
     * heavy topological calculations during a single time step.
     * </p>
     * * @param dir LateralDirectionality; the lateral direction to check for a parallel merge
     * @return boolean; {@code true} if a parallel merge situation exists in the given direction, {@code false} otherwise
     * @throws ParameterException if required parameters (like LOOKAHEAD) are missing
     */
    public boolean getParallelMerge(final LateralDirectionality dir) throws ParameterException
    {
        // Prevent dynamic String concatenation by routing to static constants
        String cacheKey = (dir == LateralDirectionality.LEFT) ? MERGE_CACHE_KEY_LEFT : MERGE_CACHE_KEY_RIGHT;

        // Check the general updatable context cache (assuming a method like getContextValue or direct map access exists)
        // Adjust 'this.cache' to match the exact cache map/method naming of your base UpdatableContext class.
        Boolean cachedValue = (Boolean) this.cache.get(cacheKey);
        if (cachedValue != null)
        {
            return cachedValue; // Autounboxing to primitive boolean
        }

        // Compute if not cached
        boolean hasMerge = computeParallelMerge(dir);

        // Store in general cache
        this.cache.put(cacheKey, hasMerge); // Autoboxing to Boolean occurs here

        return hasMerge;
    }

    // ----------------------------------------------------------------------
    // Safe computation wrappers
    // ----------------------------------------------------------------------

    /**
     * Wrapper for lane-end distance computation with exception safety.
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
     * Uses {@link InfrastructurePerception#getLegalLaneChangeInfo(RelativeLane)} to find the first relevant
     * {@link LaneChangeInfo} instance and its remaining distance.
     * </p>
     * @param lane the relative lane
     * @return distance to lane end [m]
     * @throws ParameterException if perception parameters are missing
     * @throws OperationalPlanException if lane information retrieval fails
     */
    private Length computeDistanceToLaneEnd(final RelativeLane lane) throws ParameterException, OperationalPlanException
    {
        InfrastructurePerception infra = this.vehicle.getPerception().getPerceptionCategory(InfrastructurePerception.class);

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
     * @param lookAhead distance ahead along the lane centerline
     * @return {@link SpeedLimitInfo} valid at the specified look-ahead position
     * @throws OperationalPlanException if perception fails
     */
    private SpeedLimitInfo computeSpeedLimitInfo(final Length lookAhead) throws OperationalPlanException
    {
        InfrastructurePerception infra = this.vehicle.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        return infra.getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(lookAhead);
    }

    /**
     * Checks whether a lane change is currently legally permitted in the specified direction.
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
     * This method temporarily overrides the look-ahead parameter to the extended value defined in {@link MirovaParameters} to
     * ensure that lane change information is retrieved for a sufficiently long horizon. The original look-ahead value is
     * restored after retrieval.
     * </p>
     * @return distance to next legal lane change opportunity [m], or {@link Length#POSITIVE_INFINITY} if none found
     * @throws OperationalPlanException if perception fails
     * @throws ParameterException if parameter access fails
     */
    private Length distanceToLaneChangeExtendedLookahead() throws OperationalPlanException, ParameterException
    {
        InfrastructurePerception infra = this.vehicle.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        Length extendedLookaheadDistance =
                this.vehicle.getParameters().getParameter(MirovaParameters.extendedLookAheadDistance);
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
     * Calculates the spatial average speed of all vehicles currently present on the entire given lane.
     * <p>
     * Evaluated strictly on demand. This method delegates to the bounded speed calculation using the full length of the lane.
     * </p>
     * @param lane the lane to analyze
     * @return the average speed, or {@link Speed#POSITIVE_INFINITY} if the lane is currently empty
     */
    public Speed getLaneAverageSpeed(final Lane lane)
    {
        return getLaneAverageSpeed(lane, Length.ZERO, lane.getLength(), 9999, ScanDirection.FRONT_TO_BACK);
    }

    /**
     * Calculates the spatial average speed of a limited number of vehicles within a specific segment of a lane, scanning from a
     * specific direction (front/downstream or back/upstream).
     * <p>
     * This is the ultimate anticipation sensor. It allows the ego vehicle to selectively measure shockwaves at specific
     * bottlenecks (e.g., the last 300 meters of a lane) by limiting the sample size and defining the spatial boundaries.
     * Evaluated strictly on demand to prevent memory leaks.
     * </p>
     * @param lane the lane to analyze
     * @param startPosition the longitudinal start position of the segment to scan
     * @param endPosition the longitudinal end position of the segment to scan
     * @param maxVehicles the maximum number of vehicles to include in the average
     * @param scanDirection the direction to iterate over the vehicles
     * @return the average speed, or {@link Speed#POSITIVE_INFINITY} if no valid vehicles are found
     */
    public Speed getLaneAverageSpeed(final Lane lane, final Length startPosition, final Length endPosition,
            final int maxVehicles, final ScanDirection scanDirection)
    {
        ImmutableList<LaneBasedGtu> gtuList = lane.getGtuList();

        // Fast fail for empty lanes or invalid requests
        if (gtuList == null || gtuList.isEmpty() || maxVehicles <= 0)
        {
            return Speed.POSITIVE_INFINITY;
        }

        int count = 0;
        double sumSpeedSI = 0.0;

        try
        {
            // In OpenTrafficSim, getGtuList() is ordered by longitudinal position.
            // Index 0 is the furthest upstream (back), index size()-1 is the furthest downstream (front).
            if (scanDirection == ScanDirection.FRONT_TO_BACK)
            {
                // Start at the downstream end (front) and move upstream
                for (int i = gtuList.size() - 1; i >= 0 && count < maxVehicles; i--)
                {
                    LaneBasedGtu gtu = gtuList.get(i);
                    Length pos = gtu.position(lane, gtu.getReference());

                    // Only count the vehicle if it is within the specified spatial boundaries
                    if (pos.ge(startPosition) && pos.le(endPosition))
                    {
                        sumSpeedSI += gtu.getSpeed().si;
                        count++;
                    }
                }
            }
            else
            {
                // Start at the upstream end (back) and move downstream
                for (int i = 0; i < gtuList.size() && count < maxVehicles; i++)
                {
                    LaneBasedGtu gtu = gtuList.get(i);
                    Length pos = gtu.position(lane, gtu.getReference());

                    if (pos.ge(startPosition) && pos.le(endPosition))
                    {
                        sumSpeedSI += gtu.getSpeed().si;
                        count++;
                    }
                }
            }
        }
        catch (GtuException exception)
        {
            // Failsafe: If GTU positioning cannot be resolved, assume free flow
            exception.printStackTrace();
            return Speed.POSITIVE_INFINITY;
        }

        // Failsafe if the segment was empty
        if (count == 0)
        {
            return Speed.POSITIVE_INFINITY;
        }

        // Return the arithmetic mean of instantaneous speeds (Space-Mean Speed)
        return new Speed(sumSpeedSI / count, org.djunits.unit.SpeedUnit.SI);
    }

    /**
     * Computes the anticipated lane drop on a specific adjacent lane direction, including downstream lanes.
     * <p>
     * By looping over {@code pathInfo.laneList()} and targeting only the requested direction, this efficiently handles on-ramps
     * that start further downstream. The loop terminates immediately upon finding the target.
     * </p>
     * @param direction the specific lateral direction to scan
     * @return the lane drop info, or {@code null} if no drop exists in this direction
     */
    private LaneDropInfo computeAnticipatedLaneDrop(final LateralDirectionality direction)
    {
        try
        {
            LaneBasedGtu egoGtu = this.vehicle.getGtu();
            Length lookahead = this.vehicle.getParameters().getParameter(MirovaParameters.extendedLookAheadDistance);

            // Project the path ahead
            LanePathInfo pathInfo = AbstractLaneBasedTacticalPlanner.buildLanePathInfo(egoGtu, lookahead);

            Length accumulatedPathLength = Length.ZERO;

            // Retrieve the current longitudinal position on the reference lane to offset the calculation
            Lane currentReferenceLane = egoGtu.getLane();
            Length currentPositionOnRef = egoGtu.position(currentReferenceLane, egoGtu.getReference());
            GtuType gtuType = egoGtu.getType();

            // Iterate over the projected future path
            for (Lane lanesAhead : pathInfo.laneList())
            {
                Set<Lane> adjacentLanes = lanesAhead.accessibleAdjacentLanesLegal(direction, gtuType);

                if (!adjacentLanes.isEmpty())
                {
                    Lane adjacentLane = adjacentLanes.iterator().next(); // Grab the direct adjacent lane

                    // If the adjacent lane has no subsequent lanes, it's a physical lane drop
                    if (adjacentLane.nextLanes(gtuType).isEmpty())
                    {
                        Length dropDist = accumulatedPathLength.plus(adjacentLane.getLength()).minus(currentPositionOnRef);

                        // Only return if the drop is actually ahead of us
                        if (dropDist.ge0())
                        {
                            return new LaneDropInfo(dropDist, adjacentLane);
                        }
                    }
                }

                accumulatedPathLength = accumulatedPathLength.plus(lanesAhead.getLength());
            }
        }
        catch (GtuException | NetworkException | ParameterException e)
        {
            e.printStackTrace();
        }

        // Return null if the path was fully scanned without finding a drop in the specified direction
        return null;
    }

    /**
     * Executes the heavy path projection to find the first adjacent lane downstream.
     * <p>
     * This iterates over the projected future path of the vehicle from closest to furthest. If a specific direction is
     * provided, it searches only in that direction. If no direction is provided ({@code null} or {@code NONE}), it tests both
     * lateral directions and returns the very first valid adjacent lane it encounters along the downstream path.
     * </p>
     * <p>
     * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @param direction the lateral direction of the target merge lane, or {@code null}/{@code NONE} to search both
     * @return the downstream adjacent lane on the main road, or {@code null} if none is found
     */
    private Lane computeDownstreamAdjacentLane(final LateralDirectionality direction)
    {
        try
        {
            LaneBasedGtu egoGtu = this.vehicle.getGtu();
            Length lookahead = this.vehicle.getParameters().getParameter(MirovaParameters.extendedLookAheadDistance);

            LanePathInfo pathInfo = AbstractLaneBasedTacticalPlanner.buildLanePathInfo(egoGtu, lookahead);
            if (pathInfo == null || pathInfo.laneList().isEmpty())
            {
                return null; // No path ahead, so no downstream lanes
            }
            GtuType gtuType = egoGtu.getType();

            // Bestimme, welche Richtungen überhaupt geprüft werden sollen
            boolean searchLeft = (direction == null || direction == LateralDirectionality.NONE || direction.isLeft());
            boolean searchRight = (direction == null || direction == LateralDirectionality.NONE || direction.isRight());

            List<Lane> lanes = pathInfo.laneList();
            if (lanes.isEmpty() || lanes.size() <= 1)
            {
                return null; // No path ahead, so no downstream lanes
            }
            Lane lastLane = lanes.get(lanes.size() - 1);
            if (lastLane == null || lastLane.nextLanes(gtuType).isEmpty())
            {
                return null; // Last lane has no downstream lanes, so no adjacent lanes either
            }
            Lane lcLane = lastLane.nextLanes(gtuType).iterator().next();
            if (lcLane == null)
            {
                return null; // No lane ahead, so no downstream lanes
            }

            // Prüfe linke Seite, falls relevant
            if (searchLeft)
            {
                Set<Lane> adjacentLeft = lcLane.accessibleAdjacentLanesLegal(LateralDirectionality.LEFT, gtuType);

                if (adjacentLeft != null && !adjacentLeft.isEmpty())
                {
                    // System.out.println("Found downstream adjacent lane in direction: LEFT");
                    return adjacentLeft.iterator().next();
                }
            }

            // Prüfe rechte Seite, falls relevant
            if (searchRight)
            {
                Set<Lane> adjacentRight = lcLane.accessibleAdjacentLanesLegal(LateralDirectionality.RIGHT, gtuType);

                if (adjacentRight != null && !adjacentRight.isEmpty())
                {
                    // System.out.println("Found downstream adjacent lane in direction: RIGHT");
                    return adjacentRight.iterator().next();
                }
            }

        }
        catch (GtuException | NetworkException | ParameterException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    // ----------------------------------------------------------------------
    // Helper Classes
    // ----------------------------------------------------------------------

    /**
     * Container holding information about a downstream lane drop.
     */
    public static class LaneDropInfo implements Serializable
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 20260414L;

        /** The distance to the point where the lane physically drops. */
        private final Length distance;

        /** The specific Lane object that is ending. */
        private final Lane lane;

        /**
         * Constructor.
         * @param distance the distance until the drop
         * @param lane the lane that drops
         */
        public LaneDropInfo(final Length distance, final Lane lane)
        {
            this.distance = distance;
            this.lane = lane;
        }

        /**
         * @return the distance to the lane drop
         */
        public Length getDistance()
        {
            return this.distance;
        }

        /**
         * @return the lane object that is ending
         */
        public Lane getLane()
        {
            return this.lane;
        }
    }

    /**
     * Computes the presence of a parallel merging lane in the specified direction.
     * <p>
     * The logic identifies a parallel merge scenario if a second adjacent lane exists, ends within the look-ahead distance,
     * while the first adjacent lane continues.
     * </p>
     * * @param dir LateralDirectionality; the lateral direction to evaluate
     * @return boolean; {@code true} if a merge point is detected, {@code false} otherwise
     * @throws ParameterException if required parameters are missing
     */
    private boolean computeParallelMerge(final LateralDirectionality dir) throws ParameterException
    {
        GtuType gtuType = this.vehicle.getGtu().getType();
        Lane currentLane = this.vehicle.getGtu().getLane();

        // Defensive programming: Ensure current lane is known before accessing adjacency
        if (currentLane == null)
        {
            return false;
        }

        // Safely resolve the first adjacent lane
        Lane firstAdjacentLane = currentLane.getAdjacentLane(dir, gtuType);
        if (firstAdjacentLane == null)
        {
            return false;
        }

        // Safely resolve the second adjacent lane
        Lane secondAdjacentLane = firstAdjacentLane.getAdjacentLane(dir, gtuType);
        if (secondAdjacentLane == null)
        {
            return false; // No second adjacent lane exists
        }

        // Ensure the second adjacent lane ends, but the first one continues
        if (secondAdjacentLane.nextLanes(gtuType).isEmpty() && !firstAdjacentLane.nextLanes(gtuType).isEmpty())
        {
            Length distanceToMergePoint = secondAdjacentLane.getLength().minus(this.vehicle.getGtu().getLongitudinalPosition());
            Length lookahead = this.vehicle.getParameters().getParameter(ParameterTypes.LOOKAHEAD);

            // Check if the topological merge point is within the tactical look-ahead horizon
            if (distanceToMergePoint.lt(lookahead))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Container holding information about a downstream adjacent lane.
     * <p>
     * This wrapper is essential to safely cache the result of the downstream lane search, even if no lane is found (preventing
     * repetitive calculations for null results).
     * </p>
     * <p>
     * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
     */
    public static class DownstreamLaneInfo implements Serializable
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 20260415L;

        /** The downstream adjacent lane, or null if none exists. */
        private final Lane lane;

        /**
         * Constructor.
         * @param lane the downstream lane, or {@code null} if none was found within the horizon
         */
        public DownstreamLaneInfo(final Lane lane)
        {
            this.lane = lane;
        }

        /**
         * Retrieves the cached lane object.
         * @return the lane object, or {@code null}
         */
        public Lane getLane()
        {
            return this.lane;
        }
    }

    /**
     * Defines the direction for iterating over vehicles on a lane.
     */
    public enum ScanDirection
    {
        /** Start at the downstream end of the lane (front) and scan backwards. */
        FRONT_TO_BACK,

        /** Start at the upstream end of the lane (back) and scan forwards. */
        BACK_TO_FRONT
    }

    /**
     * Updates the cache validity for this context category.
     * <p>
     * No direct computations are performed here since all values are evaluated lazily on demand.
     * </p>
     * @param vehicle the ego vehicle executing the update
     */
    @Override
    public void updateFromPerception(final MirovaTacticalPlanner vehicle)
    {
        // Lazy: recomputed only when requested
        markCacheValid();
    }

    /**
     * Returns a compact textual summary of the currently cached values.
     * @return a short summary string
     */
    @Override
    public String toString()
    {
        return "InfrastructureContext[" + "distToLaneEnd="
                + getCachedValue(DIST_TO_LANE_END_PREFIX + RelativeLane.CURRENT, Length.class) + ", legalSpeedLimit="
                + getCachedValue(LEGAL_SPEED_LIMIT, Speed.class) + "]";
    }
}

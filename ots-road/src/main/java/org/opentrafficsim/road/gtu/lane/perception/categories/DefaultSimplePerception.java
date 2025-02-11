package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.LanePathInfo;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Default perception category.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface DefaultSimplePerception extends LaneBasedPerceptionCategory
{

    /**
     * Update lane path info.
     * @throws GtuException when the GTU was not initialized yet.
     * @throws NetworkException when the speed limit for a GTU type cannot be retrieved from the network.
     * @throws ParameterException in case of not being able to retrieve parameter ParameterTypes.LOOKAHEAD
     */
    void updateLanePathInfo() throws GtuException, NetworkException, ParameterException;

    /**
     * Update the forward headway and first object (a GTU) in front.
     * @throws GtuException when the GTU was not yet initialized
     * @throws ParameterException if parameter is not defined or out of bounds
     * @throws NetworkException in case of network exception
     */
    void updateForwardHeadwayGtu() throws GtuException, NetworkException, ParameterException;

    /**
     * Update the forward headway and first object (but not a GTU) in front.
     * @throws GtuException when the GTU was not yet initialized
     * @throws ParameterException if parameter is not defined or out of bounds
     * @throws NetworkException in case of network exception
     */
    void updateForwardHeadwayObject() throws GtuException, NetworkException, ParameterException;

    /**
     * Update the backward headway and first object (e.g., a GTU) behind.
     * @throws GtuException when the GTU was not yet initialized
     * @throws ParameterException if parameter is not defined or out of bounds
     * @throws NetworkException in case of network exception
     */
    void updateBackwardHeadway() throws GtuException, ParameterException, NetworkException;

    /**
     * Update the accessible adjacent lanes.
     * @throws GtuException when the GTU was not yet initialized
     */
    default void updateAccessibleAdjacentLanes() throws GtuException
    {
        updateAccessibleAdjacentLanesLeft();
        updateAccessibleAdjacentLanesRight();
    }

    /**
     * Update the accessible adjacent lanes on the left.
     * @throws GtuException when the GTU was not yet initialized
     */
    void updateAccessibleAdjacentLanesLeft() throws GtuException;

    /**
     * Update the accessible adjacent lanes on the right.
     * @throws GtuException when the GTU was not yet initialized
     */
    void updateAccessibleAdjacentLanesRight() throws GtuException;

    /**
     * Update the objects (e.g., GTUs) in parallel, in front and behind on the left neighboring lane, with their headway
     * relative to our GTU, and information about the status of the adjacent objects.
     * @throws GtuException when the GTU was not yet initialized
     * @throws ParameterException if parameter is not defined or out of bounds
     * @throws NetworkException in case of network exception
     */
    void updateNeighboringHeadwaysLeft() throws GtuException, ParameterException, NetworkException;

    /**
     * Update the objects (e.g., GTUs) in parallel, in front and behind on the right neighboring lane, with their headway
     * relative to our GTU, and information about the status of the adjacent objects.
     * @throws GtuException when the GTU was not yet initialized
     * @throws ParameterException if parameter is not defined or out of bounds
     * @throws NetworkException in case of network exception
     */
    void updateNeighboringHeadwaysRight() throws GtuException, ParameterException, NetworkException;

    /**
     * Update the objects (e.g., GTUs) in parallel, in front and behind for the lane in the given direction, with their headway
     * relative to our GTU, and information about the status of the adjacent objects.
     * @param lateralDirection the direction to update the parallel headway collection for
     * @throws GtuException when the GTU was not yet initialized
     * @throws ParameterException if parameter is not defined or out of bounds
     * @throws NetworkException in case of network exception
     */
    void updateNeighboringHeadways(LateralDirectionality lateralDirection)
            throws GtuException, ParameterException, NetworkException;

    /**
     * Update the parallel objects (e.g., GTUs) on the left, with information about their status and parallel overlap with our
     * GTU.
     * @throws GtuException when the GTU was not yet initialized
     */
    void updateParallelHeadwaysLeft() throws GtuException;

    /**
     * Update the parallel objects (e.g., GTUs) on the right, with information about their status and parallel overlap with our
     * GTU.
     * @throws GtuException when the GTU was not yet initialized
     */
    void updateParallelHeadwaysRight() throws GtuException;

    /**
     * Update the parallel objects (e.g., GTUs) for the given direction, with information about their status and parallel
     * overlap with our GTU.
     * @param lateralDirection the direction to return the neighboring headway collection for
     * @throws GtuException when the GTU was not yet initialized
     */
    void updateParallelHeadways(LateralDirectionality lateralDirection) throws GtuException;

    /**
     * Update speedLimit.
     * @throws GtuException when the GTU was not yet initialized
     * @throws NetworkException in case of network exception
     */
    void updateSpeedLimit() throws GtuException, NetworkException;

    /**
     * Retrieve the last perceived lane path info.
     * @return LanePathInfo
     */
    LanePathInfo getLanePathInfo();

    /**
     * Returns forward headways.
     * @return forwardHeadway, the forward headway and first object (GTU) in front
     */
    Headway getForwardHeadwayGtu();

    /**
     * Returns forward headway objects.
     * @return forwardHeadway, the forward headway and first object (not a GTU) in front
     */
    Headway getForwardHeadwayObject();

    /**
     * Returns backward headways.
     * @return backwardHeadwayGtu, the backward headway and first object (e.g., a GTU) behind
     */
    Headway getBackwardHeadway();

    /**
     * Returns right accessible adjacent lanes.
     * @return accessibleAdjacentLanesLeft, the accessible adjacent lanes on the left
     */
    Map<Lane, Set<Lane>> getAccessibleAdjacentLanesLeft();

    /**
     * Returns right accessible adjacent lanes.
     * @return accessibleAdjacentLanesRight, the accessible adjacent lanes on the right
     */
    Map<Lane, Set<Lane>> getAccessibleAdjacentLanesRight();

    /**
     * Returns accessible adjacent lanes.
     * @param lateralDirection the direction to return the accessible adjacent lane map for
     * @return the accessible adjacent lane map for the given direction
     */
    Map<Lane, Set<Lane>> getAccessibleAdjacentLanes(LateralDirectionality lateralDirection);

    /**
     * Returns left neighbors.
     * @return neighboringHeadwaysLeft, the objects (e.g., GTUs) in parallel, in front and behind on the left neighboring lane,
     *         with their headway relative to our GTU, and information about the status of the adjacent objects
     */
    Collection<Headway> getNeighboringHeadwaysLeft();

    /**
     * Returns right neighbors.
     * @return neighboringHeadwaysRight, the objects (e.g., GTUs) in parallel, in front and behind on the right neighboring
     *         lane, with their headway relative to our GTU, and information about the status of the adjacent objects
     */
    Collection<Headway> getNeighboringHeadwaysRight();

    /**
     * Returns neighbors.
     * @param lateralDirection the direction to return the parallel headway collection for
     * @return the the objects (e.g., GTUs) in parallel, in front and behind for the lane in the given direction, with their
     *         headway relative to our GTU, and information about the status of the adjacent objects
     */
    Collection<Headway> getNeighboringHeadways(LateralDirectionality lateralDirection);

    /**
     * Returns left parallel headways.
     * @return parallelHeadwaysLeft, the parallel objects (e.g., GTUs) on the left, with information about their status and
     *         parallel overlap with our GTU.
     */
    Collection<Headway> getParallelHeadwaysLeft();

    /**
     * Return right parallel headways.
     * @return parallelHeadwaysRight, the parallel objects (e.g., GTUs) on the right, with information about their status and
     *         parallel overlap with our GTU.
     */
    Collection<Headway> getParallelHeadwaysRight();

    /**
     * Returns parallel headways.
     * @param lateralDirection the direction to return the neighboring headway collection for
     * @return the the parallel objects (e.g., GTUs) for the given direction, with information about their status and parallel
     *         overlap with our GTU.
     */
    Collection<Headway> getParallelHeadways(LateralDirectionality lateralDirection);

    /**
     * Returns the speed limit.
     * @return speedLimit
     */
    Speed getSpeedLimit();

    /**
     * Determine whether there is a lane to the left or to the right of this lane, which is accessible from this lane, or null
     * if no lane could be found. The method takes the LongitidinalDirectionality of the lane into account. In other words, if
     * we drive FORWARD and look for a lane on the LEFT, and there is a lane but the Directionality of that lane is not FORWARD
     * or BOTH, null will be returned.<br>
     * A lane is called adjacent to another lane if the lateral edges are not more than a delta distance apart. This means that
     * a lane that <i>overlaps</i> with another lane is <b>not</b> returned as an adjacent lane. <br>
     * The algorithm also looks for RoadMarkerAcross elements between the lanes to determine the lateral permeability for a GTU.
     * A RoadMarkerAcross is seen as being between two lanes if its center line is not more than delta distance from the
     * relevant lateral edges of the two adjacent lanes. <br>
     * When there are multiple lanes that are adjacent, which could e.g. be the case if an overlapping tram lane and a car lane
     * are adjacent to the current lane, the widest lane that best matches the GTU accessibility of the provided GtuType is
     * returned. <br>
     * <b>Note:</b> LEFT is seen as a negative lateral direction, RIGHT as a positive lateral direction. <br>
     * FIXME In other places in OTS LEFT is positive (and RIGHT is negative). This should be made more consistent.
     * @param currentLane the lane to look for the best accessible adjacent lane
     * @param lateralDirection the direction (LEFT, RIGHT) to look at
     * @param longitudinalPosition the position of the GTU along <cite>currentLane</cite>
     * @return the lane if it is accessible, or null if there is no lane, it is not accessible, or the driving direction does
     *         not match.
     */
    Lane bestAccessibleAdjacentLane(Lane currentLane, LateralDirectionality lateralDirection, Length longitudinalPosition);

}

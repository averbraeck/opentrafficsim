package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.perception.PerceptionCategory;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.LanePathInfo;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Default perception category.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 feb. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface DefaultSimplePerception extends PerceptionCategory
{

    /**
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when the speed limit for a GTU type cannot be retrieved from the network.
     * @throws ParameterException in case of not being able to retrieve parameter ParameterTypes.LOOKAHEAD
     */
    void updateLanePathInfo() throws GTUException, NetworkException, ParameterException;

    /**
     * Update the forward headway and first object (a GTU) in front.
     * @throws GTUException when the GTU was not yet initialized
     * @throws ParameterException if parameter is not defined or out of bounds
     * @throws NetworkException in case of network exception
     */
    void updateForwardHeadwayGTU() throws GTUException, NetworkException, ParameterException;

    /**
     * Update the forward headway and first object (but not a GTU) in front.
     * @throws GTUException when the GTU was not yet initialized
     * @throws ParameterException if parameter is not defined or out of bounds
     * @throws NetworkException in case of network exception
     */
    void updateForwardHeadwayObject() throws GTUException, NetworkException, ParameterException;

    /**
     * Update the backward headway and first object (e.g., a GTU) behind.
     * @throws GTUException when the GTU was not yet initialized
     * @throws ParameterException if parameter is not defined or out of bounds
     * @throws NetworkException in case of network exception
     */
    void updateBackwardHeadway() throws GTUException, ParameterException, NetworkException;

    /**
     * Update the accessible adjacent lanes.
     * @throws GTUException when the GTU was not yet initialized
     */
    default void updateAccessibleAdjacentLanes() throws GTUException
    {
        updateAccessibleAdjacentLanesLeft();
        updateAccessibleAdjacentLanesRight();
    }
    
    /**
     * Update the accessible adjacent lanes on the left.
     * @throws GTUException when the GTU was not yet initialized
     */
    void updateAccessibleAdjacentLanesLeft() throws GTUException;

    /**
     * Update the accessible adjacent lanes on the right.
     * @throws GTUException when the GTU was not yet initialized
     */
    void updateAccessibleAdjacentLanesRight() throws GTUException;

    /**
     * Update the objects (e.g., GTUs) in parallel, in front and behind on the left neighboring lane, with their headway
     * relative to our GTU, and information about the status of the adjacent objects.
     * @throws GTUException when the GTU was not yet initialized
     * @throws ParameterException if parameter is not defined or out of bounds
     * @throws NetworkException in case of network exception
     */
    void updateNeighboringHeadwaysLeft() throws GTUException, ParameterException, NetworkException;

    /**
     * Update the objects (e.g., GTUs) in parallel, in front and behind on the right neighboring lane, with their headway
     * relative to our GTU, and information about the status of the adjacent objects.
     * @throws GTUException when the GTU was not yet initialized
     * @throws ParameterException if parameter is not defined or out of bounds
     * @throws NetworkException in case of network exception
     */
    void updateNeighboringHeadwaysRight() throws GTUException, ParameterException, NetworkException;

    /**
     * Update the objects (e.g., GTUs) in parallel, in front and behind for the lane in the given direction, with their headway
     * relative to our GTU, and information about the status of the adjacent objects.
     * @param lateralDirection the direction to update the parallel headway collection for
     * @throws GTUException when the GTU was not yet initialized
     * @throws ParameterException if parameter is not defined or out of bounds
     * @throws NetworkException in case of network exception
     */
    void updateNeighboringHeadways(LateralDirectionality lateralDirection)
            throws GTUException, ParameterException, NetworkException;

    /**
     * Update the parallel objects (e.g., GTUs) on the left, with information about their status and parallel overlap with our
     * GTU.
     * @throws GTUException when the GTU was not yet initialized
     */
    void updateParallelHeadwaysLeft() throws GTUException;

    /**
     * Update the parallel objects (e.g., GTUs) on the right, with information about their status and parallel overlap with our
     * GTU.
     * @throws GTUException when the GTU was not yet initialized
     */
    void updateParallelHeadwaysRight() throws GTUException;

    /**
     * Update the parallel objects (e.g., GTUs) for the given direction, with information about their status and parallel
     * overlap with our GTU.
     * @param lateralDirection the direction to return the neighboring headway collection for
     * @throws GTUException when the GTU was not yet initialized
     */
    void updateParallelHeadways(LateralDirectionality lateralDirection) throws GTUException;

    /**
     * Update speedLimit.
     * @throws GTUException when the GTU was not yet initialized
     * @throws NetworkException in case of network exception
     */
    void updateSpeedLimit() throws GTUException, NetworkException;

    /**
     * Retrieve the last perceived lane path info.
     * @return LanePathInfo
     */
    LanePathInfo getLanePathInfo();
    
    /**
     * @return forwardHeadway, the forward headway and first object (GTU) in front
     */
    Headway getForwardHeadwayGTU();

    /**
     * @return forwardHeadway, the forward headway and first object (not a GTU) in front
     */
    Headway getForwardHeadwayObject();

    /**
     * @return backwardHeadwayGTU, the backward headway and first object (e.g., a GTU) behind
     */
    Headway getBackwardHeadway();

    /**
     * @return accessibleAdjacentLanesLeft, the accessible adjacent lanes on the left
     */
    Map<Lane, Set<Lane>> getAccessibleAdjacentLanesLeft();

    /**
     * @return accessibleAdjacentLanesRight, the accessible adjacent lanes on the right
     */
    Map<Lane, Set<Lane>> getAccessibleAdjacentLanesRight();

    /**
     * @param lateralDirection the direction to return the accessible adjacent lane map for
     * @return the accessible adjacent lane map for the given direction
     */
    Map<Lane, Set<Lane>> getAccessibleAdjacentLanes(LateralDirectionality lateralDirection);

    /**
     * @return neighboringHeadwaysLeft, the objects (e.g., GTUs) in parallel, in front and behind on the left neighboring lane,
     *         with their headway relative to our GTU, and information about the status of the adjacent objects
     */
    Collection<Headway> getNeighboringHeadwaysLeft();

    /**
     * @return neighboringHeadwaysRight, the objects (e.g., GTUs) in parallel, in front and behind on the right neighboring
     *         lane, with their headway relative to our GTU, and information about the status of the adjacent objects
     */
    Collection<Headway> getNeighboringHeadwaysRight();

    /**
     * @param lateralDirection the direction to return the parallel headway collection for
     * @return the the objects (e.g., GTUs) in parallel, in front and behind for the lane in the given direction, with their
     *         headway relative to our GTU, and information about the status of the adjacent objects
     */
    Collection<Headway> getNeighboringHeadways(LateralDirectionality lateralDirection);

    /**
     * @return parallelHeadwaysLeft, the parallel objects (e.g., GTUs) on the left, with information about their status and
     *         parallel overlap with our GTU.
     */
    Collection<Headway> getParallelHeadwaysLeft();

    /**
     * @return parallelHeadwaysRight, the parallel objects (e.g., GTUs) on the right, with information about their status and
     *         parallel overlap with our GTU.
     */
    Collection<Headway> getParallelHeadwaysRight();

    /**
     * @param lateralDirection the direction to return the neighboring headway collection for
     * @return the the parallel objects (e.g., GTUs) for the given direction, with information about their status and parallel
     *         overlap with our GTU.
     */
    Collection<Headway> getParallelHeadways(LateralDirectionality lateralDirection);

    /**
     * @return speedLimit
     */
    Speed getSpeedLimit();

    /** {@inheritDoc} */
    @Override
    default void updateAll() throws GTUException, ParameterException, NetworkException
    {
        updateLanePathInfo();
        updateForwardHeadwayGTU();
        updateForwardHeadwayObject();
        updateBackwardHeadway();
        updateAccessibleAdjacentLanesLeft();
        updateAccessibleAdjacentLanesRight();
        updateNeighboringHeadwaysLeft();
        updateNeighboringHeadwaysRight();
        updateParallelHeadwaysLeft();
        updateParallelHeadwaysRight();
        updateSpeedLimit();
    }

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
     * are adjacent to the current lane, the widest lane that best matches the GTU accessibility of the provided GTUType is
     * returned. <br>
     * <b>Note:</b> LEFT is seen as a negative lateral direction, RIGHT as a positive lateral direction. <br>
     * FIXME In other places in OTS LEFT is positive (and RIGHT is negative). This should be made more consistent.
     * @param currentLane the lane to look for the best accessible adjacent lane
     * @param lateralDirection the direction (LEFT, RIGHT) to look at
     * @param longitudinalPosition Length; the position of the GTU along <cite>currentLane</cite>
     * @return the lane if it is accessible, or null if there is no lane, it is not accessible, or the driving direction does
     *         not match.
     */
    Lane bestAccessibleAdjacentLane(Lane currentLane, LateralDirectionality lateralDirection, Length longitudinalPosition);
    
}

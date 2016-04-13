package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.core.gtu.perception.TimeStampedObject;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Interface for perception in a lane-based model. The following information can be perceived:
 * <ul>
 * <li>maximum speed we can use at the current location; either time stamped or just the information</li>
 * <li>forward headway and first object (e.g., GTU) in front; either time stamped or just the information</li>
 * <li>backward headway and first object (e.g., GTU) behind; either time stamped or just the information</li>
 * <li>accessible adjacent lanes on the left or right; either time stamped or just the information</li>
 * <li>parallel objects (e.g., GTUa) on the left or right; either time stamped or just the information</li>
 * <li>Objects (e.g., GTUs) in parallel, in front and behind on the left or right neighboring lane, with their headway relative
 * to our GTU; either time stamped or just the information</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jan 30, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface LanePerception extends Perception
{
    /**
     * sets the GTU -- call this method before any call to the perceive() method!
     * @param gtu the GTU for which this is the perception module
     */
    void setGTU(LaneBasedGTU gtu);

    /**
     * @return the gtu for which this is the perception
     */
    LaneBasedGTU getGTU();

    /************************************************************************************************************/
    /********************************** RETRIEVING OF THE INFORMATION *****************************************/
    /************************************************************************************************************/

    /**
     * @return forwardHeadway, the forward headway and first object (e.g., a GTU) in front
     */
    Headway getForwardHeadway();

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
    Map<Lane, Set<Lane>> accessibleAdjacentLaneMap(LateralDirectionality lateralDirection);

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

    /************************************************************************************************************/
    /********************************** RETRIEVING TIMESTAMPED INFORMATION ************************************/
    /************************************************************************************************************/

    /**
     * @return TimeStamped forwardHeadway, the forward headway and first object (e.g., a GTU) in front
     */
    TimeStampedObject<Headway> getTimeStampedForwardHeadway();

    /**
     * @return TimeStamped backwardHeadwayGTU, the backward headway and first object (e.g., a GTU) behind
     */
    TimeStampedObject<Headway> getTimeStampedBackwardHeadway();

    /**
     * @return TimeStamped accessibleAdjacentLanesLeft, the accessible adjacent lanes on the left
     */
    TimeStampedObject<Map<Lane, Set<Lane>>> getTimeStampedAccessibleAdjacentLanesLeft();

    /**
     * @return TimeStamped accessibleAdjacentLanesRight, the accessible adjacent lanes on the right
     */
    TimeStampedObject<Map<Lane, Set<Lane>>> getTimeStampedAccessibleAdjacentLanesRight();

    /**
     * @return TimeStamped neighboringHeadwaysLeft, the objects (e.g., GTUs) in parallel, in front and behind on the left
     *         neighboring lane, with their headway relative to our GTU, and information about the status of the adjacent
     *         objects
     */
    TimeStampedObject<Collection<Headway>> getTimeStampedNeighboringHeadwaysLeft();

    /**
     * @return TimeStamped neighboringHeadwaysRight, the objects (e.g., GTUs) in parallel, in front and behind on the right
     *         neighboring lane, with their headway relative to our GTU, and information about the status of the adjacent
     *         objects
     */
    TimeStampedObject<Collection<Headway>> getTimeStampedNeighboringHeadwaysRight();

    /**
     * @return TimeStamped parallelHeadwaysLeft, the parallel objects (e.g., GTUs) on the left, with information about their
     *         status and parallel overlap with our GTU.
     */
    TimeStampedObject<Collection<Headway>> getTimeStampedParallelHeadwaysLeft();

    /**
     * @return TimeStamped parallelHeadwaysRight, the parallel objects (e.g., GTUs) on the right, with information about their
     *         status and parallel overlap with our GTU.
     */
    TimeStampedObject<Collection<Headway>> getTimeStampedParallelHeadwaysRight();

    /**
     * @return TimeStamped speedLimit
     */
    TimeStampedObject<Speed> getTimeStampedSpeedLimit();

    /************************************************************************************************************/
    /********************************** UPDATING OF THE INFORMATION *********************************************/
    /************************************************************************************************************/

    /**
     * Update who's in front of us and how far away the nearest object (e.g., a GTU) is.
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when the headway cannot be determined for this GTU, usually due to routing problems.
     * @throws ParameterException when there is a parameter problem, e.g., retrieving the forwardHeadwayDistance.
     */
    void updateForwardHeadway() throws GTUException, NetworkException, ParameterException;

    /**
     * Update who's behind us and how far away the nearest object (e.g., a GTU) is.
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when the headway cannot be determined for this GTU, usually due to routing problems.
     * @throws ParameterException when there is a parameter problem, e.g., retrieving the backwardHeadwayDistance.
     */
    void updateBackwardHeadway() throws GTUException, NetworkException, ParameterException;

    /**
     * Build a set of Lanes that is adjacent to the given lane that this GTU can enter, for the left lateral direction.
     * @throws GTUException when the GTU was not initialized yet.
     */
    void updateAccessibleAdjacentLanesLeft() throws GTUException;

    /**
     * Build a set of Lanes that is adjacent to the given lane that this GTU can enter, for the left lateral direction.
     * @throws GTUException when the GTU was not initialized yet.
     */
    void updateAccessibleAdjacentLanesRight() throws GTUException;

    /**
     * Update the information about the objects (e.g., GTUs) parallel to our GTU on the left side.
     * @throws GTUException when the GTU was not initialized yet.
     */
    void updateParallelHeadwaysLeft() throws GTUException;

    /**
     * Update the information about the objects (e.g., GTUs) parallel to our GTU on the right side.
     * @throws GTUException when the GTU was not initialized yet.
     */
    void updateParallelHeadwaysRight() throws GTUException;

    /**
     * Update the information about the objects (e.g., GTUs) left of our GTU, and behind us or ahead on the left hand side.
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when there is an inconsistency in the lanes on this network
     * @throws ParameterException when there is a parameter problem.
     */
    void updateLaneTrafficLeft() throws GTUException, NetworkException, ParameterException;

    /**
     * Update the information about the objects (e.g., GTUs) right of our GTU, and behind us or ahead on the left hand side.
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when there is an inconsistency in the lanes on this network
     * @throws ParameterException when there is a parameter problem.
     */
    void updateLaneTrafficRight() throws GTUException, NetworkException, ParameterException;

    /**
     * Update the perceived speed limit.
     * @throws NetworkException when the speed limit for a GTU type cannot be retreived from the network.
     * @throws GTUException when the GTU was not initialized yet.
     */
    void updateSpeedLimit() throws GTUException, NetworkException;

}

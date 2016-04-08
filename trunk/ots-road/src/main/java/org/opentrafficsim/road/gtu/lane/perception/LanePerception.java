package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.drivercharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.PerceivedObject;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.HeadwayGTU;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Interface for perception in a lane-based model. The following information can be perceived:
 * <ul>
 * <li>maximum speed we can use at the current location; either time stamped or just the information</li>
 * <li>forward headway and first GTU in front; either time stamped or just the information</li>
 * <li>backward headway and first GTU behind; either time stamped or just the information</li>
 * <li>accessible adjacent lanes on the left or right; either time stamped or just the information</li>
 * <li>parallel GTUs on the left or right; either time stamped or just the information</li>
 * <li>GTUs in parallel, in front and behind on the left or right neighboring lane, with their headway relative to our GTU;
 * either time stamped or just the information</li>
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
     * @return forwardHeadwayGTU, the forward headway and first GTU in front
     */
    HeadwayGTU getForwardHeadwayGTU();

    /**
     * @return backwardHeadwayGTU, the backward headway and first GTU behind
     */
    HeadwayGTU getBackwardHeadwayGTU();

    /**
     * @return accessibleAdjacentLanesLeft, the accessible adjacent lanes on the left
     */
    Map<Lane, Set<Lane>> getAccessibleAdjacentLanesLeft();

    /**
     * @return accessibleAdjacentLanesRight, the accessible adjacent lanes on the right
     */
    Map<Lane, Set<Lane>> getAccessibleAdjacentLanesRight();

    /**
     * @return neighboringGTUsLeft, the GTUs in parallel, in front and behind on the left neighboring lane, with their headway
     *         relative to our GTU
     */
    Collection<HeadwayGTU> getNeighboringGTUsLeft();

    /**
     * @return neighboringGTUsRight, the GTUs in parallel, in front and behind on the right neighboring lane, with their headway
     *         relative to our GTU
     */
    Collection<HeadwayGTU> getNeighboringGTUsRight();

    /**
     * @return parallelGTUsLeft, the parallel GTUs on the left
     */
    Set<LaneBasedGTU> getParallelGTUsLeft();

    /**
     * @return parallelGTUsRight, the parallel GTUs on the right
     */
    Set<LaneBasedGTU> getParallelGTUsRight();

    /**
     * @return speedLimit
     */
    Speed getSpeedLimit();

    /**
     * @param lateralDirection the direction to return the accessible adjacent lane map for
     * @return the accessible adjacent lane map for the given direction
     */
    Map<Lane, Set<Lane>> accessibleAdjacentLaneMap(LateralDirectionality lateralDirection);

    /**
     * @param lateralDirection the direction to return the parallel GTU map for
     * @return the parallel GTU map for the given direction
     */
    Set<LaneBasedGTU> parallelGTUs(LateralDirectionality lateralDirection);

    /**
     * @param lateralDirection the direction to return the neighboring GTU collection for
     * @return the neighboring GTU collection for the given direction
     */
    Collection<HeadwayGTU> neighboringGTUCollection(LateralDirectionality lateralDirection);

    /************************************************************************************************************/
    /********************************** RETRIEVING TIMESTAMPED INFORMATION ************************************/
    /************************************************************************************************************/

    /**
     * @return TimeStamped forwardHeadwayGTU, the forward headway and first GTU in front
     */
    TimeStampedObject<HeadwayGTU> getTimeStampedForwardHeadwayGTU();

    /**
     * @return TimeStamped backwardHeadwayGTU, the backward headway and first GTU behind
     */
    TimeStampedObject<HeadwayGTU> getTimeStampedBackwardHeadwayGTU();

    /**
     * @return TimeStamped accessibleAdjacentLanesLeft, the accessible adjacent lanes on the left
     */
    TimeStampedObject<Map<Lane, Set<Lane>>> getTimeStampedAccessibleAdjacentLanesLeft();

    /**
     * @return TimeStamped accessibleAdjacentLanesRight, the accessible adjacent lanes on the right
     */
    TimeStampedObject<Map<Lane, Set<Lane>>> getTimeStampedAccessibleAdjacentLanesRight();

    /**
     * @return TimeStamped neighboringGTUsLeft, the GTUs in parallel, in front and behind on the left neighboring lane, with
     *         their headway relative to our GTU
     */
    TimeStampedObject<Collection<HeadwayGTU>> getTimeStampedNeighboringGTUsLeft();

    /**
     * @return TimeStamped neighboringGTUsRight, the GTUs in parallel, in front and behind on the right neighboring lane, with
     *         their headway relative to our GTU
     */
    TimeStampedObject<Collection<HeadwayGTU>> getTimeStampedNeighboringGTUsRight();

    /**
     * @return TimeStamped parallelGTUsLeft, the parallel GTUs on the left
     */
    TimeStampedObject<Set<LaneBasedGTU>> getTimeStampedParallelGTUsLeft();

    /**
     * @return TimeStamped parallelGTUsRight, the parallel GTUs on the right
     */
    TimeStampedObject<Set<LaneBasedGTU>> getTimeStampedParallelGTUsRight();

    /**
     * @return TimeStamped speedLimit
     */
    TimeStampedObject<Speed> getTimeStampedSpeedLimit();

    /**
     * @return TimeStamped perceived objects
     * @throws GTUException when GTU was not initialized
     */
    TimeStampedObject<Set<PerceivedObject>> getTimeStampedPerceivedObjects() throws GTUException;

    /************************************************************************************************************/
    /********************************** UPDATING OF THE INFORMATION *******************************************/
    /************************************************************************************************************/

    /**
     * Update the perceived speed limit.
     * @throws NetworkException when the speed limit for a GTU type cannot be retreived from the network.
     * @throws GTUException when the GTU was not initialized yet.
     */
    void updateSpeedLimit() throws GTUException, NetworkException;

    /**
     * Update who's in front of us and how far away the nearest GTU is.
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when the headway cannot be determined for this GTU, usually due to routing problems.
     * @throws ParameterException when there is a parameter problem.
     */
    void updateForwardHeadwayGTU() throws GTUException, NetworkException, ParameterException;

    /**
     * Update who's behind us and how far away the nearest GTU is.
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when the headway cannot be determined for this GTU, usually due to routing problems.
     * @throws ParameterException when there is a parameter problem
     */
    void updateBackwardHeadwayGTU() throws GTUException, NetworkException, ParameterException;

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
     * Update the information about the GTUs parallel to our GTU on the left side.
     * @throws GTUException when the GTU was not initialized yet.
     */
    void updateParallelGTUsLeft() throws GTUException;

    /**
     * Update the information about the GTUs parallel to our GTU on the right side.
     * @throws GTUException when the GTU was not initialized yet.
     */
    void updateParallelGTUsRight() throws GTUException;

    /**
     * Update the information about the GTUs left of our GTU, and behind us or ahead on the left hand side.
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when there is an inconsistency in the lanes on this network
     * @throws ParameterException when there is a parameter problem.
     */
    void updateLaneTrafficLeft() throws GTUException, NetworkException, ParameterException;

    /**
     * Update the information about the GTUs right of our GTU, and behind us or ahead on the left hand side.
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when there is an inconsistency in the lanes on this network
     * @throws ParameterException when there is a parameter problem.
     */
    void updateLaneTrafficRight() throws GTUException, NetworkException, ParameterException;

}

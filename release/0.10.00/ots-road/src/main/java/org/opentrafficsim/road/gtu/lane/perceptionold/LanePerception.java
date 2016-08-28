package org.opentrafficsim.road.gtu.lane.perceptionold;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.core.gtu.perception.TimeStampedObject;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.AbstractHeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayTrafficLight;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

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
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @return the gtu for which this is the perception
     */
    LaneBasedGTU getGtu();

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
    
    /**********************/
    /** Surrounding GTUs **/
    /**********************/

    /**
     * Set of leaders on a lane, which is usually 0 or 1, but possibly more in case of a downstream split with no intermediate
     * GTU. This is shown below. Suppose A needs to go straight. If A considers a lane change to the left, both GTUs B (who's
     * tail ~ is still on the straight lane) and C need to be considered for whether it's safe to do so. In case of multiple
     * splits close to one another, the returned set may contain even more than 2 leaders. Leaders are sorted by headway value.
     * 
     * <pre>
     *          | |
     * _________/B/_____
     * _ _?_ _ _~_ _C_ _
     * _ _A_ _ _ _ _ _ _
     * _________________
     * </pre>
     * @param lat LEFT, null (current) or RIGHT
     * @return list of followers on a lane
     */
    SortedSet<AbstractHeadwayGTU> getFirstLeaders(LateralDirectionality lat);

    /**
     * Set of followers on a lane, which is usually 0 or 1, but possibly more in case of an upstream merge with no intermediate
     * GTU. This is shown below. If A considers a lane change to the left, both GTUs B and C need to be considered for whether
     * it's safe to do so. In case of multiple merges close to one another, the returned set may contain even more than 2
     * followers. Followers are sorted by tailway value.
     * 
     * <pre>
     *        | |
     *        |C| 
     * ________\ \______
     * _ _B_|_ _ _ _ _?_
     * _ _ _|_ _ _ _ _A_ 
     * _____|___________
     * </pre>
     * @param lat LEFT, null (current) or RIGHT
     * @return list of followers on a lane
     */
    SortedSet<AbstractHeadwayGTU> getFirstFollowers(LateralDirectionality lat);

    /**
     * Whether there is a GTU alongside, i.e. with overlap, in an adjacent lane.
     * @param lat LEFT or RIGHT, null not allowed
     * @return whether there is a GTU alongside, i.e. with overlap, in an adjacent lane
     * @throws NullPointerException if {@code lat == null}
     */
    boolean existsGtuAlongside(LateralDirectionality lat);

    /**
     * Set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT. Leaders are sorted by
     * headway value.
     * @param lane relative lateral lane
     * @return set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT
     */
    SortedSet<AbstractHeadwayGTU> getLeaders(RelativeLane lane);

    /**
     * Set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR. Follower are are sorted
     * by tailway value.
     * @param lane relative lateral lane
     * @return set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR
     */
    SortedSet<AbstractHeadwayGTU> getFollowers(RelativeLane lane);

    /********************/
    /** Infrastructure **/
    /********************/

    /**
     * Returns infrastructure lane change info of a lane. A set is returned as multiple points may force lane changes. Which
     * point is considered most critical is a matter of driver interpretation and may change over time. This is shown below.
     * Suppose vehicle A needs to take the off-ramp, and that behavior is that the minimum distance per required lane change
     * determines how critical it is. First, 400m before the lane-drop, the off-ramp is critical. 300m downstream, the lane-drop
     * is critical. Info is sorted by distance, closest first.
     * 
     * <pre>
     * _______
     * _ _A_ _\_________
     * _ _ _ _ _ _ _ _ _
     * _________ _ _ ___
     *          \_______
     *     (-)        Lane-drop: 1 lane change  in 400m (400m per lane change)
     *     (--------) Off-ramp:  3 lane changes in 900m (300m per lane change, critical)
     *     
     *     (-)        Lane-drop: 1 lane change  in 100m (100m per lane change, critical)
     *     (--------) Off-ramp:  3 lane changes in 600m (200m per lane change)
     * </pre>
     * @param lane relative lateral lane
     * @return infrastructure lane change info of a lane
     */
    SortedSet<InfrastructureLaneChangeInfo> getInfrastructureLaneChangeInfo(RelativeLane lane);

    /**
     * Split number of given infrastructure lane change info, 0 if it does not regard a split.
     * <pre>
     *  ________________
     *  _ _ _ _ _ _ _ _____________
     * A___.....____________.....__
     *     \______          \______ (destination of A)
     *                          ^
     *                          |
     *             This split provides the 1st 
     *      infrastructure info on the current lane. 
     *      It regards the 3rd split along the road.
     *                 split number = 3
     * </pre>
     * @param info infrastructure lane change info
     * @return split number of given infrastructure lane change info, 0 if it does not regard a split
     */
    int getSplitNumber(InfrastructureLaneChangeInfo info);
    
    /**
     * Returns the prospect for speed limits on a lane (dynamic speed limits may vary between lanes).
     * @param lane relative lateral lane
     * @return prospect for speed limits on a lane
     */
    SpeedLimitProspect getSpeedLimitProspect(RelativeLane lane);

    /**
     * Returns the distance over which a lane change remains legally possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @return distance over which a lane change remains possible
     * @throws NullPointerException if {@code lat == null}
     */
    Length getLegalLaneChangePossibility(RelativeLane fromLane, LateralDirectionality lat);

    /**
     * Returns the distance over which a lane change remains physically possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @return distance over which a lane change remains possible
     * @throws NullPointerException if {@code lat == null}
     */
    Length getPhysicalLaneChangePossibility(RelativeLane fromLane, LateralDirectionality lat);

    /**
     * Returns a set of relative lanes representing the current cross section. Lanes are sorted left to right.
     * @return set of relative lanes representing the current cross section
     */
    SortedSet<RelativeLane> getCurrentCrossSection();

    /*************************/
    /** Perceivable objects **/
    /*************************/

    /**
     * Returns a set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @return set of traffic lights along the route
     */
    SortedSet<HeadwayTrafficLight> getTrafficLights();

    /**
     * Returns a set of intersection conflicts along the route. Conflicts are sorted by headway value.
     * @param lane relative lateral lane
     * @return set of intersection conflicts along the route
     */
    SortedSet<HeadwayConflict> getIntersectionConflicts(RelativeLane lane);

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

    /**********************/
    /** Surrounding GTUs **/
    /**********************/

    /**
     * Set of leaders on a lane, which is usually 0 or 1, but possibly more in case of a downstream split with no intermediate
     * GTU. This is shown below. Suppose A needs to go straight. If A considers a lane change to the left, both GTUs B (who's
     * tail ~ is still on the straight lane) and C need to be considered for whether it's safe to do so. In case of multiple
     * splits close to one another, the returned set may contain even more than 2 leaders. Leaders are sorted by headway value.
     * 
     * <pre>
     *          | |
     * _________/B/_____
     * _ _?_ _ _~_ _C_ _
     * _ _A_ _ _ _ _ _ _
     * _________________
     * </pre>
     * @param lat LEFT, null (current) or RIGHT
     * @return list of followers on a lane
     */
    TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedFirstLeaders(LateralDirectionality lat);

    /**
     * Set of followers on a lane, which is usually 0 or 1, but possibly more in case of an upstream merge with no intermediate
     * GTU. This is shown below. If A considers a lane change to the left, both GTUs B and C need to be considered for whether
     * it's safe to do so. In case of multiple merges close to one another, the returned set may contain even more than 2
     * followers. Followers are sorted by tailway value.
     * 
     * <pre>
     *        | |
     *        |C| 
     * ________\ \______
     * _ _B_|_ _ _ _ _?_
     * _ _ _|_ _ _ _ _A_ 
     * _____|___________
     * </pre>
     * @param lat LEFT, null (current) or RIGHT
     * @return list of followers on a lane
     */
    TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedFirstFollowers(LateralDirectionality lat);

    /**
     * Whether there is a GTU alongside, i.e. with overlap, in an adjacent lane.
     * @param lat LEFT or RIGHT, null not allowed
     * @return whether there is a GTU alongside, i.e. with overlap, in an adjacent lane
     * @throws NullPointerException if {@code lat == null}
     */
    TimeStampedObject<Boolean> existsGtuAlongsideTimeStamped(LateralDirectionality lat);

    /**
     * Set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT. Leaders are sorted by
     * headway value.
     * @param lane relative lateral lane
     * @return set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT
     */
    TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedLeaders(RelativeLane lane);

    /**
     * Set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR. Follower are are sorted
     * by tailway value.
     * @param lane relative lateral lane
     * @return set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR
     */
    TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedFollowers(RelativeLane lane);

    /********************/
    /** Infrastructure **/
    /********************/

    /**
     * Returns infrastructure lane change info of a lane. A set is returned as multiple points may force lane changes. Which
     * point is considered most critical is a matter of driver interpretation and may change over time. This is shown below.
     * Suppose vehicle A needs to take the off-ramp, and that behavior is that the minimum distance per required lane change
     * determines how critical it is. First, 400m before the lane-drop, the off-ramp is critical. 300m downstream, the lane-drop
     * is critical. Info is sorted by distance, closest first.
     * 
     * <pre>
     * _______
     * _ _A_ _\_________
     * _ _ _ _ _ _ _ _ _
     * _________ _ _ ___
     *          \_______
     *     (-)        Lane-drop: 1 lane change  in 400m (400m per lane change)
     *     (--------) Off-ramp:  3 lane changes in 900m (300m per lane change, critical)
     *     
     *     (-)        Lane-drop: 1 lane change  in 100m (100m per lane change, critical)
     *     (--------) Off-ramp:  3 lane changes in 600m (200m per lane change)
     * </pre>
     * @param lane relative lateral lane
     * @return infrastructure lane change info of a lane
     */
    TimeStampedObject<SortedSet<InfrastructureLaneChangeInfo>> getTimeStampedInfrastructureLaneChangeInfo(RelativeLane lane);

    /**
     * Split number of given infrastructure lane change info, 0 if it does not regard a split.
     * <pre>
     *  ________________
     *  _ _ _ _ _ _ _ _____________
     * A___.....____________.....__
     *     \______          \______ (destination of A)
     *                          ^
     *                          |
     *             This split provides the 1st 
     *      infrastructure info on the current lane. 
     *      It regards the 3rd split along the road.
     *                 split number = 3
     * </pre>
     * @param info infrastructure lane change info
     * @return split number of given infrastructure lane change info, 0 if it does not regard a split
     */
    TimeStampedObject<Integer> getTimeStampedSplitNumber(InfrastructureLaneChangeInfo info);
    
    /**
     * Returns the prospect for speed limits on a lane (dynamic speed limits may vary between lanes).
     * @param lane relative lateral lane
     * @return prospect for speed limits on a lane
     */
    TimeStampedObject<SpeedLimitProspect> getTimeStampedSpeedLimitProspect(RelativeLane lane);

    /**
     * Returns the distance over which a lane change remains legally possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @return distance over which a lane change remains possible
     * @throws NullPointerException if {@code lat == null}
     */
    TimeStampedObject<Length> getTimeStampedLegalLaneChangePossibility(RelativeLane fromLane, LateralDirectionality lat);

    /**
     * Returns the distance over which a lane change remains physically possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @return distance over which a lane change remains possible
     * @throws NullPointerException if {@code lat == null}
     */
    TimeStampedObject<Length> getTimeStampedPhysicalLaneChangePossibility(RelativeLane fromLane, LateralDirectionality lat);

    /**
     * Returns a set of relative lanes representing the current cross section. Lanes are sorted left to right.
     * @return set of relative lanes representing the current cross section
     */
    TimeStampedObject<SortedSet<RelativeLane>> getTimeStampedCurrentCrossSection();

    /*************************/
    /** Perceivable objects **/
    /*************************/

    /**
     * Returns a set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @return set of traffic lights along the route
     */
    TimeStampedObject<SortedSet<HeadwayTrafficLight>> getTimeStampedTrafficLights();

    /**
     * Returns a set of intersection conflicts along the route. Conflicts are sorted by headway value.
     * @param lane relative lateral lane
     * @return set of intersection conflicts along the route
     */
    TimeStampedObject<SortedSet<HeadwayConflict>> getTimeStampedIntersectionConflicts(RelativeLane lane);
    
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
    
    /**********************/
    /** Surrounding GTUs **/
    /**********************/

    /**
     * Updates of leaders on a lane, which is usually 0 or 1, but possibly more in case of a downstream split with no intermediate
     * GTU. This is shown below. Suppose A needs to go straight. If A considers a lane change to the left, both GTUs B (who's
     * tail ~ is still on the straight lane) and C need to be considered for whether it's safe to do so. In case of multiple
     * splits close to one another, the returned set may contain even more than 2 leaders. Leaders are sorted by headway value.
     * 
     * <pre>
     *          | |
     * _________/B/_____
     * _ _?_ _ _~_ _C_ _
     * _ _A_ _ _ _ _ _ _
     * _________________
     * </pre>
     */
    void updateFirstLeaders();

    /**
     * Updates of followers on a lane, which is usually 0 or 1, but possibly more in case of an upstream merge with no intermediate
     * GTU. This is shown below. If A considers a lane change to the left, both GTUs B and C need to be considered for whether
     * it's safe to do so. In case of multiple merges close to one another, the returned set may contain even more than 2
     * followers. Followers are sorted by tailway value.
     * 
     * <pre>
     *        | |
     *        |C| 
     * ________\ \______
     * _ _B_|_ _ _ _ _?_
     * _ _ _|_ _ _ _ _A_ 
     * _____|___________
     * </pre>
     */
    void updateFirstFollowers();

    /**
     * Updates whether there is a GTU alongside, i.e. with overlap, in an adjacent lane.
     * @throws NullPointerException if {@code lat == null}
     */
    void updateGtuAlongside();

    /**
     * Updates set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT. Leaders are sorted by
     * headway value.
     */
    void updateLeaders();

    /**
     * Updates set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR. Follower are are sorted
     * by tailway value.
     */
    void updateFollowers();

    /********************/
    /** Infrastructure **/
    /********************/

    /**
     * Updates infrastructure lane change info of a lane. A set is returned as multiple points may force lane changes. Which
     * point is considered most critical is a matter of driver interpretation and may change over time. This is shown below.
     * Suppose vehicle A needs to take the off-ramp, and that behavior is that the minimum distance per required lane change
     * determines how critical it is. First, 400m before the lane-drop, the off-ramp is critical. 300m downstream, the lane-drop
     * is critical. Info is sorted by distance, closest first.
     * 
     * <pre>
     * _______
     * _ _A_ _\_________
     * _ _ _ _ _ _ _ _ _
     * _________ _ _ ___
     *          \_______
     *     (-)        Lane-drop: 1 lane change  in 400m (400m per lane change)
     *     (--------) Off-ramp:  3 lane changes in 900m (300m per lane change, critical)
     *     
     *     (-)        Lane-drop: 1 lane change  in 100m (100m per lane change, critical)
     *     (--------) Off-ramp:  3 lane changes in 600m (200m per lane change)
     * </pre>
     */
    void updateInfrastructureLaneChangeInfo();

    /**
     * Updates split numbers of infrastructure lane change info, 0 if it does not regard a split.
     * <pre>
     *  ________________
     *  _ _ _ _ _ _ _ _____________
     * A___.....____________.....__
     *     \______          \______ (destination of A)
     *                          ^
     *                          |
     *             This split provides the 1st 
     *      infrastructure info on the current lane. 
     *      It regards the 3rd split along the road.
     *                 split number = 3
     * </pre>
     */
    void updateSplitNumber();
    
    /**
     * Updates the prospect for speed limits on a lane (dynamic speed limits may vary between lanes).
     */
    void updateSpeedLimitProspect();

    /**
     * Updates the distance over which a lane change remains legally possible.
     * @throws NullPointerException if {@code lat == null}
     */
    void updateLegalLaneChangePossibility();

    /**
     * Updates the distance over which a lane change remains physically possible.
     * @throws NullPointerException if {@code lat == null}
     */
    void updatePhysicalLaneChangePossibility();

    /**
     * Updates a set of relative lanes representing the current cross section. Lanes are sorted left to right.
     */
    void updateCurrentCrossSection();

    /*************************/
    /** Perceivable objects **/
    /*************************/

    /**
     * Updates a set of traffic lights along the route. Traffic lights are sorted by headway value.
     */
    void updateTrafficLights();

    /**
     * Updates a set of intersection conflicts along the route. Conflicts are sorted by headway value.
     */
    void updateIntersectionConflicts();
    
}

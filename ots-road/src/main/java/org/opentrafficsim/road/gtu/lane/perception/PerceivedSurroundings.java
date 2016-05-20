package org.opentrafficsim.road.gtu.lane.perception;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * Perception interface which allows most tactical planners for human driver behavior on freeways.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// TODO HeadwayGTU -> TailwayGTU for followers
public interface PerceivedSurroundings
{

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
    SortedSet<HeadwayGTU> getFirstLeaders(LateralDirectionality lat);

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
    SortedSet<HeadwayGTU> getFirstFollowers(LateralDirectionality lat);

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
    SortedSet<HeadwayGTU> getLeaders(RelativeLane lane);

    /**
     * Set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR. Follower are are sorted
     * by tailway value.
     * @param lane relative lateral lane
     * @return set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR
     */
    SortedSet<HeadwayGTU> getFollowers(RelativeLane lane);

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
     *     <->        Lane-drop: 1 lane change  in 400m (400m per lane change)
     *     <--------> Off-ramp:  3 lane changes in 900m (300m per lane change, critical)
     *     
     *     <->        Lane-drop: 1 lane change  in 100m (100m per lane change, critical)
     *     <--------> Off-ramp:  3 lane changes in 600m (200m per lane change)
     * </pre>
     * @param lane relative lateral lane
     * @return infrastructure lane change info of a lane
     */
    SortedSet<InfrastructureLaneChangeInfo> getInfrastructureLaneChangeInfo(RelativeLane lane);

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
    SortedSet<Object> getIntersectionConflicts(RelativeLane lane); // TODO Change output type to ConflictHeadway

}

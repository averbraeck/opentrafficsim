package org.opentrafficsim.road.gtu.lane.perception;

import java.util.List;

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

public interface PerceivedSurroundings
{

    /**************************/
    /** Surrounding vehicles **/
    /**************************/

    /**
     * List of leaders on a lane, which is usually 0 or 1, but possibly more in case of a downstream split with no intermediate
     * vehicle. This is shown below. Suppose A needs to go straight. If A considers a lane change to the left, both vehicle B
     * (who's tail ~ is still on the straight lane) and C need to be considered for whether it's safe to do so. In case of
     * multiple splits close to one another, the returned set may contain even more than 2 leaders.
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
    List<HeadwayGTU> getFirstLeaders(LateralDirectionality lat);

    /**
     * List of followers on a lane, which is usually 0 or 1, but possibly more in case of an upstream merge with no intermediate
     * vehicle. This is shown below. If A considers a lane change to the left, both vehicle B and C need to be considered for
     * whether it's safe to do so. In case of multiple merges close to one another, the returned set may contain even more than
     * 2 followers.
     * 
     * <pre>
     *        | |
     *        |C| 
     * ________\ \______
     * _ _B_|_ _\_ _ _?_
     * _ _ _|_ _ _ _ _A_ 
     * _____|___________
     * </pre>
     * @param lat LEFT, null (current) or RIGHT
     * @return list of followers on a lane
     */
    List<HeadwayGTU> getFirstFollowers(LateralDirectionality lat);

    /**
     * Whether there is an adjacent GTU, i.e. with overlap, in an adjacent lane.
     * @param lat LEFT or RIGHT, null not allowed
     * @return whether there is an adjacent GTU, i.e. with overlap, in an adjacent lane
     * @throws NullPointerException if {@code lat == null}
     */
    boolean gtuIsAdjacent(LateralDirectionality lat);

    /**
     * List of leaders on a lane, including adjacent GTU's who's nose is ahead of the own vehicle nose.
     * @param lane relative lateral lane
     * @return list of leaders on a lane, including adjacent GTU's who's nose is ahead of the own vehicle nose
     */
    List<HeadwayGTU> getLeaders(RelativeLane lane);

    /**
     * List of followers on a lane, including adjacent GTU's who's tail is back of the own vehicle tail.
     * @param lane relative lateral lane
     * @return list of followers on a lane, including adjacent GTU's who's tail is back of the own vehicle tail
     */
    List<HeadwayGTU> getFollowers(RelativeLane lane);

    /********************/
    /** Infrastructure **/
    /********************/

    /**
     * Returns infrastructure lane change info of a lane. A list is returned as multiple points may force lane changes. Which
     * points is considered most critical is a matter of driver interpretation and may change over time. This is shown below.
     * Suppose vehicle A needs to take the off-ramp, and that behavior is that the minimum distance per required lane change
     * determines how critical it is. First, 400m before the lane-drop, the off-ramp is critical. 300m downstream, the lane-drop
     * is critical.
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
    List<InfrastructureLaneChangeInfo> getInfrastructureLaneChangeInfo(RelativeLane lane);

    /**
     * Returns the prospect for speed limits on a lane (dynamic speed limits may vary between lanes).
     * @param lane relative lateral lane
     * @return prospect for speed limits on a lane
     */
    SpeedLimitProspect getSpeedLimitProspect(RelativeLane lane);

    /**
     * Returns the distance over which a lane change remains possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @return distance over which a lane change remains possible
     * @throws NullPointerException if {@code lat == null}
     */
    Length getLaneChangePossibility(RelativeLane fromLane, LateralDirectionality lat);

    /**
     * Returns the number of lanes to the left or right in the current cross-section.
     * @param lat LEFT or RIGHT, null not allowed
     * @return number of lanes to the left or right in the current cross-section
     * @throws NullPointerException if {@code lat == null}
     */
    int getNumberOfSideLanes(LateralDirectionality lat);

    /*************/
    /** Objects **/
    /*************/

    /**
     * Returns a set of traffic lights along the route.
     * @return set of traffic lights along the route
     */
    List<Object> getTrafficLights();

    /**
     * Returns a set of intersection conflicts along the route.
     * @return set of intersection conflicts along the route
     */
    List<Object> getIntersectionConflicts();

}

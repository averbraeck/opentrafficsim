package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.util.SortedSet;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.LaneBasedPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface NeighborsPerception extends LaneBasedPerceptionCategory
{

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
     * 
     * <b>Only vehicles who's rear is beyond the own front are considered, no alongside vehicles.</b><br>
     * <br>
     * @param lat LateralDirectionality; LEFT or RIGHT
     * @return list of followers on a lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    SortedSet<HeadwayGtu> getFirstLeaders(LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException;

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
     * 
     * <b>Only vehicles who's front is before the own rear are considered, no alongside vehicles.</b><br>
     * <br>
     * @param lat LateralDirectionality; LEFT or RIGHT
     * @return list of followers on a lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    SortedSet<HeadwayGtu> getFirstFollowers(LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException;

    /**
     * Whether there is a GTU alongside, i.e. with overlap, in an adjacent lane.
     * @param lat LateralDirectionality; LEFT or RIGHT
     * @return whether there is a GTU alongside, i.e. with overlap, in an adjacent lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    boolean isGtuAlongside(LateralDirectionality lat) throws ParameterException, NullPointerException, IllegalArgumentException;

    /**
     * Set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT. Leaders are sorted by
     * headway value.
     * @param lane RelativeLane; relative lateral lane
     * @return set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT
     */
    PerceptionCollectable<HeadwayGtu, LaneBasedGtu> getLeaders(RelativeLane lane);

    /**
     * Set of followers on a lane, including adjacent GTU's who's FRONT is back of the own vehicle FRONT. Follower are are
     * sorted by distance.
     * @param lane RelativeLane; relative lateral lane
     * @return set of followers on a lane, including adjacent GTU's who's FRONT is back of the own vehicle FRONT
     */
    PerceptionCollectable<HeadwayGtu, LaneBasedGtu> getFollowers(RelativeLane lane);

}

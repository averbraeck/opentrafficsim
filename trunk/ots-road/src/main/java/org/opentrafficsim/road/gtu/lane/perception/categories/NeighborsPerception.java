package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.SortedSet;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 feb. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface NeighborsPerception extends LaneBasedPerceptionCategory
{

    /**
     * Update set of leaders on a lane, which is usually 0 or 1, but possibly more in case of a downstream split with no
     * intermediate GTU.
     * @param lat LEFT or RIGHT
     * @throws NetworkException in case of a network exception
     * @throws GTUException if the GTU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    void updateFirstLeaders(LateralDirectionality lat) throws ParameterException, GTUException, NetworkException;

    /**
     * Update set of followers on a lane, which is usually 0 or 1, but possibly more in case of an upstream merge with no
     * intermediate GTU.
     * @param lat LEFT or RIGHT
     * @throws NetworkException in case of a network exception
     * @throws GTUException if the GTU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    void updateFirstFollowers(LateralDirectionality lat) throws GTUException, ParameterException, NetworkException;

    /**
     * Update whether there is a GTU alongside, i.e. with overlap, in an adjacent lane.
     * @param lat LEFT or RIGHT
     * @throws NetworkException in case of a network exception
     * @throws GTUException if the GTU was not initialized
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    void updateGtuAlongside(LateralDirectionality lat) throws GTUException, ParameterException, NetworkException;

    /**
     * Update set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT. Distance will be
     * from the own vehicle FRONT to their REAR. This is negative for (partially) adjacent vehicles.
     * @param lane relative lateral lane
     * @throws NetworkException in case of a network exception
     * @throws GTUException if the GRU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     */
    void updateLeaders(RelativeLane lane) throws ParameterException, GTUException, NetworkException;

    /**
     * Update set of followers on a lane, including adjacent GTU's who's FRONT is back of the own vehicle FRONT. Distance will
     * be from their FRONT to the own vehicle REAR. This is negative for (partially) adjacent vehicles.
     * @param lane relative lateral lane
     * @throws NetworkException in case of a network exception
     * @throws GTUException if the GRU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     */
    void updateFollowers(RelativeLane lane) throws GTUException, NetworkException, ParameterException;

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
     * @param lat LEFT or RIGHT
     * @return list of followers on a lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    SortedSet<HeadwayGTU> getFirstLeaders(LateralDirectionality lat)
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
     * @param lat LEFT or RIGHT
     * @return list of followers on a lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    SortedSet<HeadwayGTU> getFirstFollowers(LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException;

    /**
     * Whether there is a GTU alongside, i.e. with overlap, in an adjacent lane.
     * @param lat LEFT or RIGHT
     * @return whether there is a GTU alongside, i.e. with overlap, in an adjacent lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    boolean isGtuAlongside(LateralDirectionality lat) throws ParameterException, NullPointerException, IllegalArgumentException;

    /**
     * Set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT. Leaders are sorted by
     * headway value.
     * @param lane relative lateral lane
     * @return set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT
     */
    PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getLeaders(RelativeLane lane);

    /**
     * Set of followers on a lane, including adjacent GTU's who's FRONT is back of the own vehicle FRONT. Follower are are
     * sorted by distance.
     * @param lane relative lateral lane
     * @return set of followers on a lane, including adjacent GTU's who's FRONT is back of the own vehicle FRONT
     */
    PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getFollowers(RelativeLane lane);

}

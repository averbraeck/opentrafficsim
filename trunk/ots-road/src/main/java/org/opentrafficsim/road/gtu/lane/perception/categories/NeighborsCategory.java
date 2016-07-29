package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.Map;
import java.util.SortedSet;

import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.core.gtu.perception.TimeStampedObject;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.AbstractHeadwayGTU;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class NeighborsCategory extends AbstractPerceptionCategory
{
    
    /** Set of followers per relative lane. */
    private Map<RelativeLane, TimeStampedObject<SortedSet<AbstractHeadwayGTU>>> followers;
    
    /** Set of leaders per relative lane. */
    private Map<RelativeLane, TimeStampedObject<SortedSet<AbstractHeadwayGTU>>> leaders;
    
    /** Set of first followers per lane upstream of merge per lateral direction, i.e. in the left or right lane. */
    private Map<LateralDirectionality, TimeStampedObject<SortedSet<AbstractHeadwayGTU>>> firstFollowers;
    
    /** Set of first leaders per lane downstream of split per lateral direction, i.e. in the left or right lane. */
    private Map<LateralDirectionality, TimeStampedObject<SortedSet<AbstractHeadwayGTU>>> firstLeaders;
    
    /** Whether a GTU is alongside per lateral direction, i.e. in the left or right lane. */
    private Map<LateralDirectionality, TimeStampedObject<Boolean>> gtuAlongside;

    /**
     * @param perception perception
     */
    public NeighborsCategory(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAll()
    {
        //
    }

    /**
     * Update set of leaders on a lane, which is usually 0 or 1, but possibly more in case of a downstream split with no
     * intermediate GTU.
     * @param lat LEFT, null (current) or RIGHT
     */
    public final void updateFirstLeaders(final LateralDirectionality lat)
    {
        //
    }

    /**
     * Update set of followers on a lane, which is usually 0 or 1, but possibly more in case of an upstream merge with no
     * intermediate GTU.
     * @param lat LEFT, null (current) or RIGHT
     */
    public final void updateFirstFollowers(final LateralDirectionality lat)
    {
        //
    }

    /**
     * Update whether there is a GTU alongside, i.e. with overlap, in an adjacent lane.
     * @param lat LEFT, null (current) or RIGHT
     */
    public final void updateGtuAlongside(final LateralDirectionality lat)
    {
        //
    }

    /**
     * Update set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT.
     * @param lane relative lateral lane
     */
    public final void updateLeaders(final RelativeLane lane)
    {
        //
    }

    /**
     * Update set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR.
     * @param lane relative lateral lane
     */
    public final void updateFollowers(final RelativeLane lane)
    {
        //
    }

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
    public final SortedSet<AbstractHeadwayGTU> getFirstLeaders(final LateralDirectionality lat)
    {
        return null;
    }

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
    public final SortedSet<AbstractHeadwayGTU> getFirstFollowers(final LateralDirectionality lat)
    {
        return null;
    }

    /**
     * Whether there is a GTU alongside, i.e. with overlap, in an adjacent lane.
     * @param lat LEFT or RIGHT, null not allowed
     * @return whether there is a GTU alongside, i.e. with overlap, in an adjacent lane
     * @throws NullPointerException if {@code lat == null}
     */
    public final boolean isGtuAlongside(final LateralDirectionality lat)
    {
        return false;
    }

    /**
     * Set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT. Leaders are sorted by
     * headway value.
     * @param lane relative lateral lane
     * @return set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT
     */
    public final SortedSet<AbstractHeadwayGTU> getLeaders(final RelativeLane lane)
    {
        return null;
    }

    /**
     * Set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR. Follower are are sorted
     * by tailway value.
     * @param lane relative lateral lane
     * @return set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR
     */
    public final SortedSet<AbstractHeadwayGTU> getFollowers(final RelativeLane lane)
    {
        return null;
    }

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
    public final TimeStampedObject<SortedSet<AbstractHeadwayGTU>>
        getTimeStampedFirstLeaders(final LateralDirectionality lat)
    {
        return null;
    }

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
    public final TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedFirstFollowers(
        final LateralDirectionality lat)
    {
        return null;
    }

    /**
     * Whether there is a GTU alongside, i.e. with overlap, in an adjacent lane.
     * @param lat LEFT or RIGHT, null not allowed
     * @return whether there is a GTU alongside, i.e. with overlap, in an adjacent lane
     * @throws NullPointerException if {@code lat == null}
     */
    public final TimeStampedObject<Boolean> isGtuAlongsideTimeStamped(final LateralDirectionality lat)
    {
        return null;
    }

    /**
     * Set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT. Leaders are sorted by
     * headway value.
     * @param lane relative lateral lane
     * @return set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT
     */
    public final TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedLeaders(final RelativeLane lane)
    {
        return null;
    }

    /**
     * Set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR. Follower are are sorted
     * by tailway value.
     * @param lane relative lateral lane
     * @return set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR
     */
    public final TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedFollowers(final RelativeLane lane)
    {
        return null;
    }

}

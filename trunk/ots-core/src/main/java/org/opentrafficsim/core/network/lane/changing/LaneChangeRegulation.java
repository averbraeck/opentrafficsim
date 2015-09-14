package org.opentrafficsim.core.network.lane.changing;

import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 13, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LaneChangeRegulation
{
    /** the lane for which these regulations hold. */
    private final Lane lane;

    /** the general lane keeping policy to use, e.g., keep left, keep right, keep lane. */
    private final LaneKeepingPolicy laneKeepingPolicy;

    /** the overtaking regulations, e.g. allow overtaking left and right such as on American highways. */
    private final OvertakingConditions overtakingConditions;

    /**
     * @param lane the lane for which these overtaking conditions hold
     * @param laneKeepingPolicy the general lane keeping policy to use, e.g., keep left, keep right, keep lane
     * @param overtakingConditions the lane change regulations, e.g. allow overtaking left and right such as on American
     *            highways
     */
    public LaneChangeRegulation(final Lane lane, final LaneKeepingPolicy laneKeepingPolicy,
        final OvertakingConditions overtakingConditions)
    {
        super();
        this.lane = lane;
        this.laneKeepingPolicy = laneKeepingPolicy;
        this.overtakingConditions = overtakingConditions;
    }

    /**
     * @return laneKeepingPolicy the general lane keeping policy to use, e.g., keep left, keep right, keep lane
     */
    public final LaneKeepingPolicy getLaneKeepingPolicy()
    {
        return this.laneKeepingPolicy;
    }

    /**
     * Check the overtaking conditions. E.g., is a car allowed on this road to overtake a tractor that is sriving in front of
     * it? If so, on which side(s)?
     * @param gtu the GTU that might overtake another GTU
     * @param predecessorGTU the GTU in front of the GTU that might want to overtake
     * @return an overtaking direction: LEFT, RIGHT, BOTH or NONE
     */
    public final OvertakingDirection checkOvertaking(final LaneBasedGTU gtu, final LaneBasedGTU predecessorGTU)
    {
        return this.overtakingConditions.checkOvertaking(this.lane, gtu, predecessorGTU);
    }

    /**
     * @return lane the lane for which these overtaking conditions hold
     */
    public final Lane getLane()
    {
        return this.lane;
    }

}

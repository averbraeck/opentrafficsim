package org.opentrafficsim.road.network.lane.changing;

/**
 * Enum to indicate the general lane keeping rules: keep left, keep right, or keep lane. When KEEP_RIGHT or KEEP_LEFT is used,
 * the gap acceptance policy of the GTU indicates when the vehicle will move back to the left or right after overtaking. The GTU
 * could decide to stay in a lane and not go back as well; it is a general policy that asks the collaboration of the GTU driver.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 13, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public enum LaneKeepingPolicy
{
    /** Constant to indicate that the general policy is to keep right. */
    KEEPRIGHT,

    /** Constant to indicate that the general policy is to keep left. */
    KEEPLEFT,

    /** Constant to indicate that the general policy is to keep lane. */
    KEEPLANE;
}

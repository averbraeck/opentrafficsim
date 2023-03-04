package org.opentrafficsim.road.network.lane.changing;

/**
 * Enum to indicate the general lane keeping rules: keep left, keep right, or keep lane. When KEEP_RIGHT or KEEP_LEFT is used,
 * the gap acceptance policy of the GTU indicates when the vehicle will move back to the left or right after overtaking. The GTU
 * could decide to stay in a lane and not go back as well; it is a general policy that asks the collaboration of the GTU driver.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
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

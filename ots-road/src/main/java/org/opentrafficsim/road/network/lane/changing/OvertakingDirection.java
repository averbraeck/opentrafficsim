package org.opentrafficsim.road.network.lane.changing;

/**
 * The direction in which a GTU is allowed to overtake another GTU, used as a return type for evaluating overtaking conditions.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public enum OvertakingDirection
{
    /** Left only. */
    LEFT,

    /** Right only. */
    RIGHT,

    /** Left and right are both allowed. */
    BOTH,

    /** Neither left nor right are allowed. */
    NONE;

}

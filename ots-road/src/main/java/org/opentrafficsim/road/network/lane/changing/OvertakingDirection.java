package org.opentrafficsim.road.network.lane.changing;

/**
 * The direction in which a GTU is allowed to overtake another GTU, used as a return type for evaluating overtaking conditions.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 13, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
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

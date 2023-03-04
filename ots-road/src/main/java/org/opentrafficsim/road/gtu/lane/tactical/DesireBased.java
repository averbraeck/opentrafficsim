package org.opentrafficsim.road.gtu.lane.tactical;

import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Incentive;

/**
 * Interface for tactical planners that can return desire information for visualization.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface DesireBased
{

    /**
     * Returns the latest desire from the specified incentive.
     * @param incentiveClass Class&lt;? extends Incentive&gt;; incentive class
     * @return latest desire from the specified incentive
     */
    Desire getLatestDesire(Class<? extends Incentive> incentiveClass);

}

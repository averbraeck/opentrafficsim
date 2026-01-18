package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.Optional;

import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Incentive;

/**
 * Interface for tactical planners that can return desire information for visualization.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface DesireBased
{

    /**
     * Returns the latest desire from the specified incentive.
     * @param incentiveClass incentive class
     * @return latest desire from the specified incentive, empty if this incentive is not active
     */
    Optional<Desire> getLatestDesire(Class<? extends Incentive> incentiveClass);

}

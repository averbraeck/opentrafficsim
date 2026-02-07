package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.Optional;

import org.djunits.value.vdouble.scalar.Acceleration;

/**
 * Interface for tactical planners that can return acceleration information.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface AccelerationBased
{

    /**
     * Return latest acceleration of incentive class.
     * @param incentiveClass incentive class
     * @return latest acceleration, or empty if incentive does not apply
     */
    Optional<Acceleration> getLatestAcceleration(Class<? extends AccelerationIncentive> incentiveClass);

}

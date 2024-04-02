package org.opentrafficsim.road.gtu.lane.control;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * Longitudinal controller, such as ACC or CACC. The controller is part of a tactical planner and does not function
 * automatically.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface LongitudinalControl
{

    /**
     * Calculate acceleration.
     * @param gtu LaneBasedGtu; controlled GTU
     * @param settings Parameters; system settings
     * @return Acceleration; level of acceleration, may be {@code null} if the controller is unable to deal with a situation
     */
    Acceleration getAcceleration(LaneBasedGtu gtu, Parameters settings);

}

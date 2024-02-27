package org.opentrafficsim.road.network.control.rampmetering;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Interface for controllers of traffic lights for ramp metering.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface RampMeteringLightController
{

    /**
     * Disables the traffic lights.
     */
    void disable();

    /**
     * Enables, or keep enabled, the controller.
     * @param cycleTime Duration; cycle time
     */
    void enable(Duration cycleTime);

}

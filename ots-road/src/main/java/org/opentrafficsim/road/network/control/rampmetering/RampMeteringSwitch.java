package org.opentrafficsim.road.network.control.rampmetering;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Determines whether the controller should be on or off.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface RampMeteringSwitch
{

    /**
     * Returns the control interval.
     * @return Duration; the control interval
     */
    Duration getInterval();

    /**
     * Evaluates whether the ramp metering should be enabled.
     * @return boolean; whether the ramp metering should be enabled
     */
    boolean isEnabled();

    /**
     * Returns the cycle time.
     * @return Duration; the cycle time
     */
    Duration getCycleTime();

}

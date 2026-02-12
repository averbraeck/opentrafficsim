package org.opentrafficsim.road.gtu.lane.tactical;

import org.djunits.value.vdouble.scalar.Speed;

/**
 * Interface for tactical planners that can return a desired speed.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@FunctionalInterface
public interface WithDesiredSpeed
{

    /**
     * Returns the last computed desired speed.
     * @return last computed desired speed
     */
    Speed getDesiredSpeed();

}

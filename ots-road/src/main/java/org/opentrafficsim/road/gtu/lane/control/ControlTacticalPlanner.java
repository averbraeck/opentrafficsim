package org.opentrafficsim.road.gtu.lane.control;

import org.opentrafficsim.base.parameters.Parameters;

/**
 * Interface for tactical planners that use control.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */

public interface ControlTacticalPlanner
{

    /**
     * Returns the system settings. This is used for sub-components that have no direct access to the settings. For example,
     * when initiating sensor perception.
     * @return Parameters; system settings
     */
    Parameters getSettings();

}

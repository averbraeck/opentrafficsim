package org.opentrafficsim.road.gtu.lane.control;

import org.opentrafficsim.base.parameters.Parameters;

/**
 * Interface for tactical planners that use control.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 12, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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

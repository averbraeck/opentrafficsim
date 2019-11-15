package org.opentrafficsim.road.gtu.lane.control;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Longitudinal controller, such as ACC or CACC. The controller is part of a tactical planner and does not function
 * automatically.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 12, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface LongitudinalControl
{

    /**
     * Calculate acceleration.
     * @param gtu LaneBasedGTU; controlled GTU
     * @param settings Parameters; system settings
     * @return Acceleration; level of acceleration, may be {@code null} if the controller is unable to deal with a situation
     */
    Acceleration getAcceleration(LaneBasedGTU gtu, Parameters settings);
    
}

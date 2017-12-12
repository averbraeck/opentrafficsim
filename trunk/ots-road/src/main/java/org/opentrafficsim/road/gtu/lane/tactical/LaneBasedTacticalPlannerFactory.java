package org.opentrafficsim.road.gtu.lane.tactical;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * A factory class is used to generate tactical planners as the tactical planner is state-full.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> class of the tactical planner generated
 */

public interface LaneBasedTacticalPlannerFactory<T extends LaneBasedTacticalPlanner> extends ModelComponentFactory
{

    /**
     * Creates a new tactical planner for the given GTU.
     * @param gtu GTU
     * @return tactical planner for the given GTU
     * @throws GTUException if the gtu is not suitable in any way for the creation of the tactical planner
     */
    T create(LaneBasedGTU gtu) throws GTUException;

}

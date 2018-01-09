package org.opentrafficsim.road.gtu.strategical;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * A factory class is used to generate strategical planners as the strategical planner is state-full.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> class of the strategical planner generated
 */

public interface LaneBasedStrategicalPlannerFactory<T extends LaneBasedStrategicalPlanner>
{

    /**
     * Creates a new strategical planner for the given GTU. The default parameters should be used.
     * @param gtu GTU
     * @param route route, may be null
     * @param origin origin, may be null
     * @param destination destination, may be null
     * @return strategical planner for the given GTU
     * @throws GTUException if the gtu is not suitable in any way for the creation of the strategical planner
     */
    T create(LaneBasedGTU gtu, Route route, Node origin, Node destination) throws GTUException;

}

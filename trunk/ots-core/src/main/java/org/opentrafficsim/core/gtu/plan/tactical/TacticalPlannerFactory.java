package org.opentrafficsim.core.gtu.plan.tactical;

import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;

/**
 * A factory class is used to generate tactical planners as the tactical planner is state-full.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> class of the tactical planner generated
 */

public interface TacticalPlannerFactory<T extends TacticalPlanner>
{

    /**
     * Returns a set of parameters with default values for the next tactical planner that will be generated.
     * @return set of parameters with default values for the next tactical planner that will be generated
     */
    Parameters getDefaultParameters();

    /**
     * Creates a new tactical planner for the given GTU.
     * @param gtu GTU; GTU
     * @return tactical planner for the given GTU
     * @throws GTUException if the gtu is not suitable in any way for the creation of the tactical planner
     */
    T create(GTU gtu) throws GTUException;

}

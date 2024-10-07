package org.opentrafficsim.core.gtu.plan.tactical;

import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;

/**
 * A factory class is used to generate tactical planners as the tactical planner is state-full.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param gtu GTU
     * @return tactical planner for the given GTU
     * @throws GtuException if the gtu is not suitable in any way for the creation of the tactical planner
     */
    T create(Gtu gtu) throws GtuException;

}

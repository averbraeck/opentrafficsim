package org.opentrafficsim.core.gtu.plan.strategical;

import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;

/**
 * A factory class is used to generate strategical planners as the strategical planner is state-full.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> class of the strategical planner generated
 */

public interface StrategicalPlannerFactory<T extends StrategicalPlanner>
{

    /**
     * Returns a set of parameters with default values for the next strategical planner that will be generated.
     * @return set of parameters with default values for the next strategical planner that will be generated
     */
    Parameters getDefaultParameters();

    /**
     * Set parameters to use with the next creation of a strategical planner. Only the next planner will use this.
     * @param parameters Parameters; parameters to use with the next creation of a strategical planner
     */
    void setParameters(Parameters parameters);

    /**
     * Creates a new strategical planner for the given GTU. If no default parameters are set, the default values will be used.
     * @param gtu Gtu; GTU
     * @return strategical planner for the given GTU
     * @throws GtuException if the gtu is not suitable in any way for the creation of the strategical planner
     */
    T create(Gtu gtu) throws GtuException;

}

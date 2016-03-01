package org.opentrafficsim.core.network.route;

/**
 * Interface for objects that generate a route (to be assigned to a newly constructed GTU).
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 20 mrt. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface RouteGenerator
{
    /**
     * Generate a Route. Generation can be based on GTU type, randomness, fixed, or otherwise.
     * @return a Route, which can be complete or partial.
     */
    Route generateRoute();
}

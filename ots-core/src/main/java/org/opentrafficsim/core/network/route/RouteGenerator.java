package org.opentrafficsim.core.network.route;

import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;

/**
 * Interface for objects that generate a route (to be assigned to a newly constructed GTU).
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 20 mrt. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface RouteGenerator extends Generator<Route>
{
    /** 
     * Return the (next) Route.
     * @return Route; the next Route 
     * @throws ProbabilityException when the an error was detected in the probabilities of the routes to choose from
     * */
    Route draw() throws ProbabilityException;
    
}

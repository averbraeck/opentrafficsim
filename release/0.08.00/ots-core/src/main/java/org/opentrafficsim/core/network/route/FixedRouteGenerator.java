package org.opentrafficsim.core.network.route;

import org.opentrafficsim.core.network.Node;

/**
 * Generate a fixed route (always the same).
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 20 mrt. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FixedRouteGenerator implements RouteGenerator
{
    /** The route that is returned on every call to generateRoute. */
    private final Route route;

    /**
     * Construct a new FixedRouteGenerator.
     * @param route the CompleteRoute to generate
     */
    public FixedRouteGenerator(final Route route)
    {
        this.route = route;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public Route draw()
    {
        return this.route;
    }

    /**
     * @return the fixed route.
     */
    public final Route getRoute()
    {
        return this.route;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("FixedRouteGenerator");
        String separator = " [";
        for (Node node : this.route.getNodes())
        {
            result.append(separator);
            result.append(node);
            separator = ", ";
        }
        result.append("]");
        return result.toString();
    }

}

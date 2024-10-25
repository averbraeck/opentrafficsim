package org.opentrafficsim.core.network.route;

import java.io.Serializable;

import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.network.Node;

/**
 * Generate a fixed route (always the same).
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version 20 mrt. 2015 <br>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class FixedRouteGenerator extends ConstantGenerator<Route> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /**
     * Construct a new FixedRouteGenerator.
     * @param route the CompleteRoute to generate
     */
    public FixedRouteGenerator(final Route route)
    {
        super(route);
    }

    @Override
    public final String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("FixedRouteGenerator");
        String separator = " [";
        for (Node node : getValue().getNodes())
        {
            result.append(separator);
            result.append(node);
            separator = ", ";
        }
        result.append("]");
        return result.toString();
    }

}

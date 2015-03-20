package org.opentrafficsim.core.network;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.network.route.RouteGenerator;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 20 mrt. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FixedRouteGenerator implements RouteGenerator
{
    /** The route that is returned on every call to generateRoute. */
    final private List<Node<?, ?>> nodeList;

    /**
     * Construct a new FixedRouteGenerator.
     * @param nodeList List&lt;Node&lt;?, ?&kgt;&gt;; List of Nodes that define the Route (this constructor makes a deep
     *            copy of the provided List)
     */
    public FixedRouteGenerator(List<Node<?, ?>> nodeList)
    {
        this.nodeList = new ArrayList<Node<?, ?>>(nodeList);
    }

    /** {@inheritDoc} */
    @Override
    public Route generateRoute()
    {
        return new Route(this.nodeList);
    }
}

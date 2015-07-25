package org.opentrafficsim.core.network.route;

import org.opentrafficsim.core.network.Node;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 20 mrt. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FixedRouteGenerator implements RouteGenerator, Comparable<FixedRouteGenerator>
{
    /** The route that is returned on every call to generateRoute. */
    private final CompleteRoute<?, ?> route;

    /**
     * Construct a new FixedRouteGenerator.
     * @param route the CompleteRoute to generate
     */
    public FixedRouteGenerator(final CompleteRoute<?, ?> route)
    {
        this.route = route;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public RouteNavigator generateRouteNavigator()
    {
        return new RouteNavigator(this.route);
    }

    /**
     * @return route.
     */
    public final CompleteRoute<?, ?> getRoute()
    {
        return this.route;
    }

    /** {@inheritDoc} */
    @Override
    public final int compareTo(final FixedRouteGenerator o)
    {
        /*-

        TODO repair compareTo(...)
        
        List<Node<?>> otherNodes = o.nodeList;
        String myEndNodeId = this.nodeList.get(this.nodeList.size() - 1).getId().toString();
        String otherEndNodeId = otherNodes.get(otherNodes.size() - 1).getId().toString();
        int result = myEndNodeId.compareTo(otherEndNodeId);
        if (0 != result)
        {
            return result;
        }
        int sizeDifference = this.nodeList.size() - otherNodes.size();
        if (sizeDifference > 0)
        {
            return 1;
        }
        else if (sizeDifference < 0)
        {
            return -1;
        }
        // This is a tough one
        for (int index = this.nodeList.size() - 1; --index >= 0;)
        {
            String myNodeId = this.nodeList.get(index).getId().toString();
            String otherNodeId = otherNodes.get(index).getId().toString();
            result = myNodeId.compareTo(otherNodeId);
            if (0 != result)
            {
                return result;
            }
        }
        // FIXME: this goes VERY wrong if different Nodes can have the same id

         */
        return 0;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("FixedRouteGenerator");
        String separator = " [";
        for (Node<?> node : this.route.getNodes())
        {
            result.append(separator);
            result.append(node);
            separator = ", ";
        }
        result.append("]");
        return result.toString();
    }

}

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
public class FixedRouteGenerator implements RouteGenerator, Comparable<FixedRouteGenerator>
{
    /** The route that is returned on every call to generateRoute. */
    final private List<Node<?, ?>> nodeList;

    /**
     * Construct a new FixedRouteGenerator.
     * @param nodeList List&lt;Node&lt;?, ?&gt;&gt;; List of Nodes that define the Route (this constructor makes a deep
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

    /** {@inheritDoc} */
    @Override
    public int compareTo(FixedRouteGenerator o)
    {
        List<Node<?, ?>> otherNodes = o.nodeList;
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
        for (int index = this.nodeList.size() - 1; --index >= 0; )
        {
            String myNodeId = this.nodeList.get(index).getId().toString();
            String otherNodeId = otherNodes.get(index).getId().toString();
            result = myNodeId.compareTo(otherNodeId);
            if (0 != result)
            {
                return result;
            }
        }
        // FIXME: this goes VERY wrong in different Nodes can have the same id
        return 0;
    }
}

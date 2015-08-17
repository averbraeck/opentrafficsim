package org.opentrafficsim.core.network.route;

import java.io.Serializable;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;

/**
 * A CompleteRouteNavigator contains a complete route and maintains a position on the route.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CompleteRouteNavigator implements RouteNavigator, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** The complete route. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final CompleteRoute<?, ?> completeRoute;

    /** last visited node on the route. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected int lastVisitedNodeIndex = -1;

    /**
     * Create a navigator.
     * @param completeRoute the route to follow
     */
    public CompleteRouteNavigator(final CompleteRoute<?, ?> completeRoute)
    {
        this.completeRoute = completeRoute;
    }

    /** {@inheritDoc} */
    @Override
    public final Node<?> lastVisitedNode() throws NetworkException
    {
        if (this.lastVisitedNodeIndex == -1)
        {
            return null;
        }
        return this.completeRoute.getNodes().get(this.lastVisitedNodeIndex);
    }

    /** {@inheritDoc} */
    @Override
    public final Node<?> nextNodeToVisit() throws NetworkException
    {
        if (this.lastVisitedNodeIndex >= this.completeRoute.size() - 1)
        {
            return null;
        }
        return this.completeRoute.getNodes().get(this.lastVisitedNodeIndex + 1);
    }

    /** {@inheritDoc} */
    @Override
    public final Node<?> visitNextNode() throws NetworkException
    {
        if (this.lastVisitedNodeIndex >= this.completeRoute.size() - 1)
        {
            return null;
        }
        this.lastVisitedNodeIndex++;
        return this.completeRoute.getNodes().get(this.lastVisitedNodeIndex);
    }

    /**
     * @return the (complete) route.
     */
    public final CompleteRoute<?, ?> getRoute()
    {
        return this.completeRoute;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        final String currentLocationMark = "<>";
        result.append("Route: [");
        String separator = "";
        if (this.lastVisitedNodeIndex < 0)
        {
            result.append(currentLocationMark);
        }
        for (int index = 0; index < this.completeRoute.size(); index++)
        {
            Node<?> node = this.completeRoute.getNodes().get(index);
            result.append(separator + node);
            if (index == this.lastVisitedNodeIndex)
            {
                result.append(" " + currentLocationMark); // Indicate current position in the route
            }
            separator = ", ";
        }
        result.append("]");
        return result.toString();
    }

}

package org.opentrafficsim.sim0mq.kpi;

import org.opentrafficsim.kpi.interfaces.RouteDataInterface;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class RouteData implements RouteDataInterface
{
    /** Route name. */
    private final String routeName;

    /** Route start. */
    private final NodeData startNode;

    /** Route end. */
    private final NodeData endNode;

    /**
     * @param routeName String; name of the route
     * @param startNode NodeData; data of the start node of the route
     * @param endNode NodeData; data of the end node of the route
     */
    public RouteData(final String routeName, final NodeData startNode, final NodeData endNode)
    {
        this.routeName = routeName;
        this.startNode = startNode;
        this.endNode = endNode;
    }

    /**
     * @return routeName
     */
    public final String getRouteName()
    {
        return this.routeName;
    }

    /**
     * @return startNode
     */
    public final NodeData getStartNode()
    {
        return this.startNode;
    }

    /**
     * @return endNode
     */
    public final NodeData getEndNode()
    {
        return this.endNode;
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.routeName;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.routeName == null) ? 0 : this.routeName.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RouteData other = (RouteData) obj;
        if (this.routeName == null)
        {
            if (other.routeName != null)
                return false;
        }
        else if (!this.routeName.equals(other.routeName))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RouteData [routeName=" + this.routeName + ", startNode=" + this.startNode + ", endNode=" + this.endNode + "]";
    }

}

package org.opentrafficsim.sim0mq.kpi;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RouteData
{
    /** Route name. */
    private final String routeName;

    /** Route start. */
    private final String startNode;

    /** Route end. */
    private final String endNode;

    /**
     * @param routeName name of the route
     * @param startNode data of the start node of the route
     * @param endNode data of the end node of the route
     */
    public RouteData(final String routeName, final String startNode, final String endNode)
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
    public final String getStartNode()
    {
        return this.startNode;
    }

    /**
     * @return endNode
     */
    public final String getEndNode()
    {
        return this.endNode;
    }

    /**
     * @return id
     */
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
    public boolean equals(final Object obj)
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

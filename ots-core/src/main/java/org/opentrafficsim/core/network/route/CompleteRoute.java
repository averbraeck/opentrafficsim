package org.opentrafficsim.core.network.route;

import java.util.List;

import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;

/**
 * A CompleteRoute is a Route with directly connected Nodes.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class CompleteRoute extends Route
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** The GtuType for which this is a route. */
    private final GtuType gtuType;

    /**
     * Create an empty route for the given GtuType.
     * @param id String; the name of the route
     * @param gtuType GtuType; the GtuType for which this is a route
     */
    public CompleteRoute(final String id, final GtuType gtuType)
    {
        super(id);
        this.gtuType = gtuType;
    }

    /**
     * Create a route based on an initial list of nodes. <br>
     * This constructor makes a defensive copy of the provided List.
     * @param id String; the name of the route.
     * @param gtuType GtuType; the GtuType for which this is a route
     * @param nodes List&lt;Node&gt;; the initial list of nodes.
     * @throws NetworkException if intermediate nodes are missing in the route.
     */
    public CompleteRoute(final String id, final GtuType gtuType, final List<Node> nodes) throws NetworkException
    {
        super(id, nodes);
        this.gtuType = gtuType;
        Node fromNode = null;
        for (Node toNode : getNodes())
        {
            if (null != fromNode)
            {
                if (!fromNode.isDirectionallyConnectedTo(this.gtuType, toNode))
                {
                    throw new NetworkException("CompleteRoute: node " + fromNode
                            + " not directly or not directionally connected to node " + toNode);
                }
            }
            fromNode = toNode;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final CompleteRoute addNode(final Node node) throws NetworkException
    {
        if (getNodes().size() > 0)
        {
            Node lastNode = getNodes().get(getNodes().size() - 1);
            if (!lastNode.isDirectionallyConnectedTo(this.gtuType, node))
            {
                throw new NetworkException("CompleteRoute: last node " + lastNode
                        + " not directly or not directionally connected to node " + node);
            }
        }
        super.addNode(node);
        return this;
    }

    /**
     * Determine if this Route contains the specified Link.
     * @param link Link; the link to check in the route.
     * @return whether the link is part of the route or not.
     */
    public final boolean containsLink(final Link link)
    {
        int index1 = getNodes().indexOf(link.getStartNode());
        int index2 = getNodes().indexOf(link.getEndNode());
        return index1 >= 0 && index2 >= 0 && Math.abs(index2 - index1) == 1;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CompleteRoute [gtuType=" + this.gtuType + ", nodes=" + super.getNodes() + "]";
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public CompleteRoute clone(final Network newNetwork) throws NetworkException
    {
        CompleteRoute newRoute = new CompleteRoute(getId(), this.gtuType);
        for (Node node : getNodes())
        {
            newRoute.addNode(newNetwork.getNode(node.getId()));
        }
        return newRoute;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode(); // BE WARE
        result = prime * result + ((gtuType == null) ? 0 : gtuType.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompleteRoute other = (CompleteRoute) obj;
        if (gtuType == null)
        {
            if (other.gtuType != null)
                return false;
        }
        else if (!gtuType.equals(other.gtuType))
            return false;
        return super.equals(other); // BE WARE
    }

}

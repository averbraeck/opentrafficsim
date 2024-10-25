package org.opentrafficsim.core.network.route;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.djutils.base.Identifiable;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;

/**
 * A route is defined as a series of nodes. Each pair of consecutive nodes should have a link valid for the given direction and
 * GTU type.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Route implements Serializable, Identifiable
{
    /** */
    private static final long serialVersionUID = 20221910L;

    /** The nodes of the route. */
    private final List<Node> nodes;

    /** The nodes of the route as a Set for quick containsNode() method. */
    private final Set<Node> nodeSet = new LinkedHashSet<>();

    /** Name of the route. */
    private final String id;

    /** The GtuType for which this is a route. */
    private final GtuType gtuType;

    /**
     * Create an empty route for the given GtuType.
     * @param id the name of the route
     * @param gtuType the GtuType for which this is a route
     */
    public Route(final String id, final GtuType gtuType)
    {
        this.nodes = new ArrayList<>();
        this.id = id;
        this.gtuType = gtuType;
    }

    /**
     * Create a route based on an initial list of nodes. <br>
     * This constructor makes a defensive copy of the provided List.
     * @param id the name of the route.
     * @param gtuType the GtuType for which this is a route
     * @param nodes the initial list of nodes.
     * @throws NetworkException if intermediate nodes are missing in the route.
     */
    public Route(final String id, final GtuType gtuType, final List<Node> nodes) throws NetworkException
    {
        this.id = id;
        this.nodes = new ArrayList<>(nodes); // defensive copy
        this.nodeSet.addAll(nodes);
        verify();
        this.gtuType = gtuType;
        Node fromNode = null;
        for (Node toNode : getNodes())
        {
            if (null != fromNode)
            {
                if (!fromNode.isConnectedTo(this.gtuType, toNode))
                {
                    throw new NetworkException(
                            "Route: node " + fromNode + " not directly or not directionally connected to node " + toNode);
                }
            }
            fromNode = toNode;
        }
    }

    /**
     * Verify that there are normal (non Connectors) between adjacent nodes, except at start and end (where Connectors are OK.
     */
    public void verify()
    {
        // XXX Sanity check - there should be no Connectors (except at start and end)
        for (int index = 0; index < this.nodes.size() - 1; index++)
        {
            Node from = this.nodes.get(index);
            Node to = this.nodes.get(index + 1);
            boolean normalLinkFound = false;
            boolean connectorFound = false;
            for (Link link : from.getLinks())
            {
                if (link.getStartNode().equals(to) || link.getEndNode().equals(to))
                {
                    if (link.isConnector())
                    {
                        connectorFound = true;
                    }
                    else
                    {
                        normalLinkFound = true;
                    }
                }
            }
            if ((!normalLinkFound) && (!connectorFound))
            {
                CategoryLogger.always()
                        .error(String.format("Unlike this route, the network has no link from %s (index %d of %d) to %s", from,
                                index, this.nodes.size(), to));
            }
            else if ((!normalLinkFound) && index > 0 && index < this.nodes.size() - 2)
            {
                CategoryLogger.always()
                        .error(String.format(
                                "Route (from node %s to node %s) includes connector along the way (index %d; node %s and %d; "
                                        + "node %s of %d)",
                                this.nodes.get(0).getId(), this.nodes.get(this.nodes.size() - 1).getId(), index, from,
                                index + 1, to, this.nodes.size()));
            }
        }
    }

    /**
     * Add a node to the end of the node list.
     * @param node the node to add.
     * @return this route for method chaining
     * @throws NetworkException in case node could not be added to the route.
     */
    public final Route addNode(final Node node) throws NetworkException
    {
        if (getNodes().size() > 0)
        {
            Node lastNode = getNodes().get(getNodes().size() - 1);
            if (!lastNode.isConnectedTo(this.gtuType, node))
            {
                throw new NetworkException(
                        "Route: last node " + lastNode + " not directly or not directionally connected to node " + node);
            }
        }
        this.nodes.add(node);
        this.nodeSet.add(node);
        verify();
        return this;
    }

    /**
     * @return nodes.
     */
    public final List<Node> getNodes()
    {
        return this.nodes;
    }

    /**
     * @param i the index of the node to obtain
     * @return node i.
     * @throws NetworkException if i &lt; 0 or i &gt; size
     */
    public final Node getNode(final int i) throws NetworkException
    {
        if (i < 0 || i >= this.nodes.size())
        {
            throw new NetworkException("Route.getNode(i=" + i + "); i<0 or i>size=" + size());
        }
        return this.nodes.get(i);
    }

    /**
     * @return the first node of the route.
     * @throws NetworkException when the list has no nodes.
     */
    public final Node originNode() throws NetworkException
    {
        if (this.nodes.size() == 0)
        {
            throw new NetworkException("Route.getOrigin() called, but node list has no nodes");
        }
        return this.nodes.get(0);
    }

    /**
     * @return the number of nodes in the list. If the list contains more than Integer.MAX_VALUE elements, returns
     *         Integer.MAX_VALUE.
     */
    public final int size()
    {
        return this.nodes.size();
    }

    /**
     * @return the last node of the route.
     * @throws NetworkException when the list has no nodes.
     */
    public final Node destinationNode() throws NetworkException
    {
        if (this.nodes.size() == 0)
        {
            throw new NetworkException("Route.getDestination() called, but node list has no nodes");
        }
        return this.nodes.get(size() - 1);
    }

    /**
     * Return the index of a Node in this Route, or -1 if this Route does not contain the specified Node. <br>
     * If this route contains the Node more than once, the index of the first is returned.
     * @param node the Node to find
     * @return
     */
    public final int indexOf(final Node node)
    {
        return this.nodes.indexOf(node);
    }

    /**
     * @param node the Node to find
     * @return whether the route contains this node (quick using LinkedHashSet);
     */
    public final boolean contains(final Node node)
    {
        return this.nodeSet.contains(node);
    }

    /**
     * @return name.
     */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * Determine if this Route contains the specified Link.
     * @param link the link to check in the route.
     * @return whether the link is part of the route or not.
     */
    public final boolean containsLink(final Link link)
    {
        int index1 = getNodes().indexOf(link.getStartNode());
        int index2 = getNodes().indexOf(link.getEndNode());
        return index1 >= 0 && index2 >= 0 && (index2 - index1) == 1;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.gtuType, this.id, this.nodes);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Route other = (Route) obj;
        return Objects.equals(this.gtuType, other.gtuType) && Objects.equals(this.id, other.id)
                && Objects.equals(this.nodes, other.nodes);
    }

    @Override
    public String toString()
    {
        return "Route [id=" + this.id + ", gtuType=" + this.gtuType + ", nodes=" + this.nodes + "]";
    }

}

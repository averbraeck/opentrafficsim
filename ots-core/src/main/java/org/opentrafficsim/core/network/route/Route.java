package org.opentrafficsim.core.network.route;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;

/**
 * A Route consists of a list of Nodes. A route does not have to be complete. As long as all 'splitting' nodes are part of the
 * route and have a valid successor node (connected by a Link), the strategical planner is able to make a plan. An extension of
 * the Route class exists that contains a complete route, where all nodes on the route have to be present and connected.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Jan 1, 2015 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class Route implements Serializable, Identifiable
{
    /** */
    private static final long serialVersionUID = 20150101L;

    /** The nodes of the route. */
    private final List<Node> nodes;

    /** The nodes of the route as a Set for quick containsNode() method. */
    private final Set<Node> nodeSet = new LinkedHashSet<>();

    /** Name of the route. */
    private final String id;

    /**
     * Create an empty route.
     * @param id String; the name of the route.
     */
    public Route(final String id)
    {
        this.nodes = new ArrayList<>();
        this.id = id;
    }

    /**
     * Create a route based on an initial list of nodes. <br>
     * @param nodes List&lt;Node&gt;; the initial list of nodes.
     * @param id String; the name of the route.
     */
    public Route(final String id, final List<Node> nodes)
    {
        this.id = id;
        this.nodes = new ArrayList<>(nodes); // defensive copy
        this.nodeSet.addAll(nodes);
        verify();
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
                    if (link.getType().isConnector())
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
     * @param node Node; the node to add.
     * @return Route; this route for method chaining
     * @throws NetworkException in case node could not be added to the route.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public Route addNode(final Node node) throws NetworkException
    {
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
     * @param i int; the index of the node to obtain
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
     * @param node Node; the Node to find
     * @return int;
     */
    public final int indexOf(final Node node)
    {
        return this.nodes.indexOf(node);
    }

    /**
     * @param node Node; the Node to find
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

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.nodeSet == null) ? 0 : this.nodeSet.hashCode());
        result = prime * result + ((this.nodes == null) ? 0 : this.nodes.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Route other = (Route) obj;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.nodeSet == null)
        {
            if (other.nodeSet != null)
                return false;
        }
        else if (!this.nodeSet.equals(other.nodeSet))
            return false;
        if (this.nodes == null)
        {
            if (other.nodes != null)
                return false;
        }
        else if (!this.nodes.equals(other.nodes))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "Route [id=" + this.id + ", nodes=" + this.nodes + "]";
    }

}

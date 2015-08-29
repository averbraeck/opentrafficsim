package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opentrafficsim.core.network.route.Route;

/**
 * A Network consists of a set of links. Each link has, in its turn, a start node and an end node.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class OTSNetwork implements Network, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722;

    /** network id. */
    private final String id;

    /** Map of Nodes. */
    private Map<String, Node> nodeMap = new HashMap<>();

    /** Map of Links. */
    private Map<String, Link> linkMap = new HashMap<>();

    /** Map of Routes. */
    private Map<String, Route> routeMap = new HashMap<>();

    /**
     * Construction of an empty network.
     * @param id the network id.
     */
    public OTSNetwork(final String id)
    {
        this.id = id;
    }

    /** {@inheritDoc} */
    @Override
    public final void addNode(final Node node) throws NetworkException
    {
        if (containsNode(node))
        {
            throw new NetworkException("Node " + node + " already registered in network " + this.id);
        }
        if (this.nodeMap.keySet().contains(node.getId()))
        {
            throw new NetworkException("Node with name " + node.getId() + " already registered in network " + this.id);
        }
        this.nodeMap.put(node.getId(), node);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeNode(final Node node) throws NetworkException
    {
        if (!containsNode(node))
        {
            throw new NetworkException("Node " + node + " not registered in network " + this.id);
        }
        this.nodeMap.remove(node.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsNode(final Node node)
    {
        return this.nodeMap.keySet().contains(node.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsNode(final String nodeId)
    {
        return this.nodeMap.keySet().contains(nodeId);
    }

    /** {@inheritDoc} */
    @Override
    public final Node getNode(final String nodeId)
    {
        return this.nodeMap.get(nodeId);
    }

    /** {@inheritDoc} */
    @Override
    public final void addLink(final Link link) throws NetworkException
    {
        if (containsLink(link))
        {
            throw new NetworkException("Link " + link + " already registered in network " + this.id);
        }
        if (this.linkMap.keySet().contains(link.getId()))
        {
            throw new NetworkException("Link with name " + link.getId() + " already registered in network " + this.id);
        }
        if (!containsNode(link.getStartNode()) || !containsNode(link.getEndNode()))
        {
            throw new NetworkException("Start node or end node of Link " + link.getId() + " not registered in network "
                + this.id);
        }
        this.linkMap.put(link.getId(), link);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeLink(final Link link) throws NetworkException
    {
        if (!containsLink(link))
        {
            throw new NetworkException("Link " + link + " not registered in network " + this.id);
        }
        this.linkMap.remove(link.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final Link getLink(final Node node1, final Node node2)
    {
        for (Link link : this.linkMap.values())
        {
            if (link.getStartNode().equals(node1) && link.getEndNode().equals(node2))
            {
                return link;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Link getLink(final String nodeId1, final String nodeId2) throws NetworkException
    {
        if (!containsNode(nodeId1))
        {
            throw new NetworkException("Node " + nodeId1 + " not in network " + this.id);
        }
        if (!containsNode(nodeId2))
        {
            throw new NetworkException("Node " + nodeId2 + " not in network " + this.id);
        }
        return getLink(getNode(nodeId1), getNode(nodeId2));
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsLink(final Link link)
    {
        return this.linkMap.keySet().contains(link.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsLink(final String linkId)
    {
        return this.linkMap.keySet().contains(linkId);
    }

    /** {@inheritDoc} */
    @Override
    public final Link getLink(final String linkId)
    {
        return this.linkMap.get(linkId);
    }

    /** {@inheritDoc} */
    @Override
    public final void addRoute(final Route route) throws NetworkException
    {
        if (containsRoute(route))
        {
            throw new NetworkException("Route " + route + " already registered in network " + this.id);
        }
        if (this.routeMap.keySet().contains(route.getId()))
        {
            throw new NetworkException("Route with name " + route.getId() + " already registered in network " + this.id);
        }
        for (Node node : route.getNodes())
        {
            if (!containsNode(node))
            {
                throw new NetworkException("Node " + node.getId() + " of route " + route.getId()
                    + " not registered in network " + this.id);
            }
        }
        this.routeMap.put(route.getId(), route);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeRoute(final Route route) throws NetworkException
    {
        if (!containsRoute(route))
        {
            throw new NetworkException("Route " + route + " not registered in network " + this.id);
        }
        this.routeMap.remove(route.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsRoute(final Route route)
    {
        return this.routeMap.keySet().contains(route.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsRoute(final String routeId)
    {
        return this.routeMap.keySet().contains(routeId);
    }

    /** {@inheritDoc} */
    @Override
    public final Route getRoute(final String routeId)
    {
        return this.routeMap.get(routeId);
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Route> getRoutesBetween(final Node nodeFrom, final Node nodeTo)
    {
        return null;
        // FIXME getRoutesBetween(Node nodeFrom, Node nodeTo)
    }

    /** {@inheritDoc} */
    @Override
    public final Route getShortestRouteBetween(final Node nodeFrom, final Node nodeTo)
    {
        return null;
        // FIXME getShortestRouteBetween(Node nodeFrom, Node nodeTo)
    }

    /**
     * @return id
     */
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<String, Node> getNodeMap()
    {
        return this.nodeMap;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<String, Link> getLinkMap()
    {
        return this.linkMap;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<String, Route> getRouteMap()
    {
        return this.routeMap;
    }

}

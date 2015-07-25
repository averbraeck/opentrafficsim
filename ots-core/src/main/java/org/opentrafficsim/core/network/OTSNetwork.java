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
 * @param <NETWORKID> the ID type of the Network, e.g., String.
 * @param <NODEID> the ID type of the Node, e.g., String.
 * @param <LINKID> the ID type of the Link, e.g., String.
 */
public class OTSNetwork<NETWORKID, LINKID, NODEID> implements Network<LINKID, NODEID>, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722;

    /** network id. */
    private final NETWORKID id;

    /** Map of Nodes. */
    private Map<NODEID, Node<NODEID>> nodeMap = new HashMap<>();

    /** Map of Links. */
    private Map<LINKID, Link<LINKID, NODEID>> linkMap = new HashMap<>();

    /** Map of Routes. */
    private Map<String, Route<LINKID, NODEID>> routeMap = new HashMap<>();

    /**
     * Construction of an empty network.
     * @param id the network id.
     */
    public OTSNetwork(final NETWORKID id)
    {
        this.id = id;
    }

    /** {@inheritDoc} */
    @Override
    public final void addNode(final Node<NODEID> node) throws NetworkException
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
    public final void removeNode(final Node<NODEID> node) throws NetworkException
    {
        if (!containsNode(node))
        {
            throw new NetworkException("Node " + node + " not registered in network " + this.id);
        }
        this.nodeMap.remove(node.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsNode(final Node<NODEID> node)
    {
        return this.nodeMap.keySet().contains(node.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsNode(final NODEID nodeId)
    {
        return this.nodeMap.keySet().contains(nodeId);
    }

    /** {@inheritDoc} */
    @Override
    public final Node<NODEID> getNode(final NODEID nodeId)
    {
        return this.nodeMap.get(nodeId);
    }

    /** {@inheritDoc} */
    @Override
    public final void addLink(final Link<LINKID, NODEID> link) throws NetworkException
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
    public final void removeLink(final Link<LINKID, NODEID> link) throws NetworkException
    {
        if (!containsLink(link))
        {
            throw new NetworkException("Link " + link + " not registered in network " + this.id);
        }
        this.linkMap.remove(link.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final Link<LINKID, NODEID> getLink(final Node<NODEID> node1, final Node<NODEID> node2)
    {
        for (Link<LINKID, NODEID> link : this.linkMap.values())
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
    public final Link<LINKID, NODEID> getLink(final NODEID nodeId1, final NODEID nodeId2) throws NetworkException
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
    public final boolean containsLink(final Link<LINKID, NODEID> link)
    {
        return this.linkMap.keySet().contains(link.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsLink(final LINKID linkId)
    {
        return this.linkMap.keySet().contains(linkId);
    }

    /** {@inheritDoc} */
    @Override
    public final Link<LINKID, NODEID> getLink(final LINKID linkId)
    {
        return this.linkMap.get(linkId);
    }

    /** {@inheritDoc} */
    @Override
    public final void addRoute(final Route<LINKID, NODEID> route) throws NetworkException
    {
        if (containsRoute(route))
        {
            throw new NetworkException("Route " + route + " already registered in network " + this.id);
        }
        if (this.routeMap.keySet().contains(route.getId()))
        {
            throw new NetworkException("Route with name " + route.getId() + " already registered in network " + this.id);
        }
        for (Node<NODEID> node : route.getNodes())
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
    public final void removeRoute(final Route<LINKID, NODEID> route) throws NetworkException
    {
        if (!containsRoute(route))
        {
            throw new NetworkException("Route " + route + " not registered in network " + this.id);
        }
        this.routeMap.remove(route.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsRoute(final Route<LINKID, NODEID> route)
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
    public final Route<LINKID, NODEID> getRoute(final String routeId)
    {
        return this.routeMap.get(routeId);
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Route<LINKID, NODEID>> getRoutesBetween(final Node<NODEID> nodeFrom, final Node<NODEID> nodeTo)
    {
        return null;
        // FIXME getRoutesBetween(Node<NODEID> nodeFrom, Node<NODEID> nodeTo)
    }

    /** {@inheritDoc} */
    @Override
    public final Route<LINKID, NODEID> getShortestRouteBetween(final Node<NODEID> nodeFrom, final Node<NODEID> nodeTo)
    {
        return null;
        // FIXME getShortestRouteBetween(Node<NODEID> nodeFrom, Node<NODEID> nodeTo)
    }

    /**
     * @return id
     */
    public final NETWORKID getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<NODEID, Node<NODEID>> getNodeMap()
    {
        return this.nodeMap;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<LINKID, Link<LINKID, NODEID>> getLinkMap()
    {
        return this.linkMap;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<String, Route<LINKID, NODEID>> getRouteMap()
    {
        return this.routeMap;
    }

}

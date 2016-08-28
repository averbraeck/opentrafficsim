package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.perception.PerceivableContext;

import nl.tudelft.simulation.event.EventProducer;

/**
 * A Network consists of a set of links. Each link has, in its turn, a start node and an end node.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class OTSNetwork extends EventProducer implements Network, PerceivableContext, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722;

    /** Id of this network. */
    private final String id;

    /** Map of Nodes. */
    private Map<String, Node> nodeMap = new HashMap<>();

    /** Map of Links. */
    private Map<String, Link> linkMap = new HashMap<>();

    /** Map of Routes. */
    private Map<GTUType, Map<String, Route>> routeMap = new HashMap<>();

    /** Graphs to calculate shortest paths per GTUType. */
    private Map<GTUType, SimpleWeightedGraph<Node, LinkEdge<Link>>> linkGraphs = new HashMap<>();

    /** GTUs registered in this network. */
    private Map<String, GTU> gtuMap = new HashMap<>();

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
    public final String getId()
    {
        return this.id;
    }

    /***************************************************************************************/
    /**************************************** NODES ****************************************/
    /***************************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final Map<String, Node> getNodeMap()
    {
        return new HashMap<String, Node>(this.nodeMap);
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
        System.out.println(node);
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

    /***************************************************************************************/
    /**************************************** LINKS ****************************************/
    /***************************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final Map<String, Link> getLinkMap()
    {
        return new HashMap<String, Link>(this.linkMap);
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

    /***************************************************************************************/
    /*************************************** ROUTES ****************************************/
    /***************************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final Map<String, Route> getDefinedRouteMap(final GTUType gtuType)
    {
        Map<String, Route> routes = new HashMap<>();
        if (this.routeMap.containsKey(gtuType))
        {
            routes.putAll(this.routeMap.get(gtuType));
        }
        return routes;
    }

    /** {@inheritDoc} */
    @Override
    public final void addRoute(final GTUType gtuType, final Route route) throws NetworkException
    {
        if (containsRoute(gtuType, route))
        {
            throw new NetworkException("Route " + route + " for GTUType " + gtuType + " already registered in network "
                    + this.id);
        }
        if (this.routeMap.containsKey(gtuType) && this.routeMap.get(gtuType).keySet().contains(route.getId()))
        {
            throw new NetworkException("Route with name " + route.getId() + " for GTUType " + gtuType
                    + " already registered in network " + this.id);
        }
        for (Node node : route.getNodes())
        {
            if (!containsNode(node))
            {
                throw new NetworkException("Node " + node.getId() + " of route " + route.getId() + " for GTUType " + gtuType
                        + " not registered in network " + this.id);
            }
        }
        if (!this.routeMap.containsKey(gtuType))
        {
            this.routeMap.put(gtuType, new HashMap<String, Route>());
        }
        this.routeMap.get(gtuType).put(route.getId(), route);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeRoute(final GTUType gtuType, final Route route) throws NetworkException
    {
        if (!containsRoute(gtuType, route))
        {
            throw new NetworkException("Route " + route + " for GTUType " + gtuType + " not registered in network " + this.id);
        }
        this.routeMap.get(gtuType).remove(route.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsRoute(final GTUType gtuType, final Route route)
    {
        if (this.routeMap.containsKey(gtuType))
        {
            return this.routeMap.get(gtuType).values().contains(route);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsRoute(final GTUType gtuType, final String routeId)
    {
        if (this.routeMap.containsKey(gtuType))
        {
            return this.routeMap.get(gtuType).keySet().contains(routeId);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final Route getRoute(final GTUType gtuType, final String routeId)
    {
        if (this.routeMap.containsKey(gtuType))
        {
            return this.routeMap.get(gtuType).get(routeId);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Route> getRoutesBetween(final GTUType gtuType, final Node nodeFrom, final Node nodeTo)
    {
        Set<Route> routes = new HashSet<>();
        if (this.routeMap.containsKey(gtuType))
        {
            for (Route route : this.routeMap.get(gtuType).values())
            {
                try
                {
                    if (route.originNode().equals(nodeFrom) && route.destinationNode().equals(nodeTo))
                    {
                        routes.add(route);
                    }
                }
                catch (NetworkException ne)
                {
                    // thrown if no nodes exist in the route. Do not add the route in that case.
                }
            }
        }
        return routes;
    }

    /** {@inheritDoc} */
    @Override
    public final void buildGraph(final GTUType gtuType)
    {
        // TODO take connections into accound, and possibly do node expansion to build the graph
        @SuppressWarnings("rawtypes")
        Class linkEdgeClass = LinkEdge.class;
        @SuppressWarnings("unchecked")
        SimpleWeightedGraph<Node, LinkEdge<Link>> graph = new SimpleWeightedGraph<Node, LinkEdge<Link>>(linkEdgeClass);
        for (Node node : this.nodeMap.values())
        {
            graph.addVertex(node);
        }
        for (Link link : this.linkMap.values())
        {
            LinkEdge<Link> linkEdge = new LinkEdge<>(link);
            // determine if the link is accessible for the GTUType , and in which direction(s)
            LongitudinalDirectionality directionality = link.getDirectionality(gtuType);
            if (directionality.equals(LongitudinalDirectionality.DIR_PLUS)
                    || directionality.equals(LongitudinalDirectionality.DIR_BOTH))
            {
                graph.addEdge(link.getStartNode(), link.getEndNode(), linkEdge);
                graph.setEdgeWeight(linkEdge, link.getLength().doubleValue());
            }
            if (directionality.equals(LongitudinalDirectionality.DIR_MINUS)
                    || directionality.equals(LongitudinalDirectionality.DIR_BOTH))
            {
                graph.addEdge(link.getEndNode(), link.getStartNode(), linkEdge);
                graph.setEdgeWeight(linkEdge, link.getLength().doubleValue());
            }
        }
        this.linkGraphs.put(gtuType, graph);
    }

    /** {@inheritDoc} */
    @Override
    public final CompleteRoute getShortestRouteBetween(final GTUType gtuType, final Node nodeFrom, final Node nodeTo)
            throws NetworkException
    {
        CompleteRoute route = new CompleteRoute("Route for " + gtuType + " from " + nodeFrom + "to " + nodeTo, gtuType);
        SimpleWeightedGraph<Node, LinkEdge<Link>> graph = this.linkGraphs.get(gtuType);
        if (graph == null)
        {
            buildGraph(gtuType);
            graph = this.linkGraphs.get(gtuType);
        }
        DijkstraShortestPath<Node, LinkEdge<Link>> path = new DijkstraShortestPath<>(graph, nodeFrom, nodeTo);
        if (path.getPath() == null)
        {
            return null;
        }
        route.addNode(nodeFrom);
        for (LinkEdge<Link> link : path.getPathEdgeList())
        {
            if (!link.getLink().getEndNode().equals(route.destinationNode())
                    && route.destinationNode().isDirectionallyConnectedTo(gtuType, link.getLink().getEndNode()))
            {
                route.addNode(link.getLink().getEndNode());
            }
            else if (!link.getLink().getStartNode().equals(route.destinationNode())
                    && route.destinationNode().isDirectionallyConnectedTo(gtuType, link.getLink().getStartNode()))
            {
                route.addNode(link.getLink().getStartNode());
            }
            else
            {
                throw new NetworkException("Cannot connect two links when calculating shortest route");
            }
        }
        return route;
    }

    /** {@inheritDoc} */
    @Override
    public final CompleteRoute getShortestRouteBetween(final GTUType gtuType, final Node nodeFrom, final Node nodeTo,
            final List<Node> nodesVia) throws NetworkException
    {
        CompleteRoute route =
                new CompleteRoute(
                        "Route for " + gtuType + " from " + nodeFrom + "to " + nodeTo + " via " + nodesVia.toString(), gtuType);
        SimpleWeightedGraph<Node, LinkEdge<Link>> graph = this.linkGraphs.get(gtuType);
        if (graph == null)
        {
            buildGraph(gtuType);
            graph = this.linkGraphs.get(gtuType);
        }
        List<Node> nodes = new ArrayList<>();
        nodes.add(nodeFrom);
        nodes.addAll(nodesVia);
        nodes.add(nodeTo);
        Node from = nodeFrom;
        route.addNode(nodeFrom);
        for (int i = 1; i < nodes.size(); i++)
        {
            Node to = nodes.get(i);
            DijkstraShortestPath<Node, LinkEdge<Link>> path = new DijkstraShortestPath<>(graph, from, to);
            if (path.getPath() == null)
            {
                return null;
            }
            for (LinkEdge<Link> link : path.getPathEdgeList())
            {
                if (!link.getLink().getEndNode().equals(route.destinationNode())
                        && route.destinationNode().isDirectionallyConnectedTo(gtuType, link.getLink().getEndNode()))
                {
                    route.addNode(link.getLink().getEndNode());
                }
                else if (!link.getLink().getStartNode().equals(route.destinationNode())
                        && route.destinationNode().isDirectionallyConnectedTo(gtuType, link.getLink().getStartNode()))
                {
                    route.addNode(link.getLink().getStartNode());
                }
                else
                {
                    throw new NetworkException(
                            "Cannot connect two links when calculating shortest route with intermediate nodes");
                }
            }
            from = to;
        }
        return route;
    }

    /***************************************************************************************/
    /**************************************** GTUs *****************************************/
    /***************************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final void addGTU(final GTU gtu)
    {
        this.gtuMap.put(gtu.getId(), gtu);
        fireTimedEvent(Network.GTU_ADD_EVENT, new Object[] {gtu.getId()}, gtu.getSimulator().getSimulatorTime());
    }

    /** {@inheritDoc} */
    @Override
    public final void removeGTU(final GTU gtu)
    {
        fireTimedEvent(Network.GTU_REMOVE_EVENT, new Object[] {gtu.getId()}, gtu.getSimulator().getSimulatorTime());
        this.gtuMap.remove(gtu.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsGTU(final GTU gtu)
    {
        return this.gtuMap.containsValue(gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final GTU getGTU(final String gtuId)
    {
        return this.gtuMap.get(gtuId);
    }

    /** {@inheritDoc} */
    @Override
    public final Set<GTU> getGTUs()
    {
        // defensive copy
        return new HashSet<GTU>(this.gtuMap.values());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsGtuId(final String gtuId)
    {
        return this.gtuMap.containsKey(gtuId);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OTSNetwork [id=" + this.id + ", nodeMapSize=" + this.nodeMap.size() + ", linkMapSize=" + this.linkMap.size()
                + ", routeMapSize=" + this.routeMap.size() + ", gtuMapSize=" + this.gtuMap.size() + "]";
    }

}

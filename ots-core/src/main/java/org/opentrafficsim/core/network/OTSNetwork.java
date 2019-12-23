package org.opentrafficsim.core.network;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.vecmath.Point3d;

import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableHashMap;
import org.djutils.immutablecollections.ImmutableMap;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.opentrafficsim.core.compatibility.GTUCompatibility;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.object.InvisibleObjectInterface;
import org.opentrafficsim.core.object.ObjectInterface;
import org.opentrafficsim.core.perception.PerceivableContext;

import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.language.d3.BoundingBox;

/**
 * A Network consists of a set of links. Each link has, in its turn, a start node and an end node.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private Map<String, Node> nodeMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Map of Links. */
    private Map<String, Link> linkMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Map of ObjectInterface. */
    private Map<String, ObjectInterface> objectMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Map of InvisibleObjects. */
    private Map<String, InvisibleObjectInterface> invisibleObjectMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Map of Routes. */
    private Map<GTUType, Map<String, Route>> routeMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Graphs to calculate shortest paths per GTUType. */
    private Map<GTUType, SimpleDirectedWeightedGraph<Node, LinkEdge<Link>>> linkGraphs = new LinkedHashMap<>();

    /** GTUTypes registered for this network. */
    private Map<String, GTUType> gtuTypeMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /** LinkTypes registered for this network. */
    private Map<String, LinkType> linkTypeMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /** GTUs registered in this network. */
    private Map<String, GTU> gtuMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Construction of an empty network.
     * @param id String; the network id.
     * @param addDefaultTypes add the default GTUTypes and LinkTypes, or not
     */
    public OTSNetwork(final String id, final boolean addDefaultTypes)
    {
        this.id = id;
        if (addDefaultTypes)
        {
            addDefaultGtuTypes();
            addDefaultLinkTypes();
        }
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
    public final ImmutableMap<String, Node> getNodeMap()
    {
        return new ImmutableHashMap<>(this.nodeMap, Immutable.WRAP);
    }

    /**
     * @return the original NodeMap; only to be used in the 'network' package for cloning.
     */
    final Map<String, Node> getRawNodeMap()
    {
        return this.nodeMap;
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
        fireEvent(Network.NODE_ADD_EVENT, node.getId());
        fireEvent(Network.ANIMATION_NODE_ADD_EVENT, node);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeNode(final Node node) throws NetworkException
    {
        if (!containsNode(node))
        {
            throw new NetworkException("Node " + node + " not registered in network " + this.id);
        }
        fireEvent(Network.NODE_REMOVE_EVENT, node.getId());
        fireEvent(Network.ANIMATION_NODE_REMOVE_EVENT, node);
        this.nodeMap.remove(node.getId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsNode(final Node node)
    {
        // System.out.println(node);
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
    public final ImmutableMap<String, Link> getLinkMap()
    {
        return new ImmutableHashMap<>(this.linkMap, Immutable.WRAP);
    }

    /**
     * @return the original LinkMap; only to be used in the 'network' package for cloning.
     */
    final Map<String, Link> getRawLinkMap()
    {
        return this.linkMap;
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
            throw new NetworkException(
                    "Start node or end node of Link " + link.getId() + " not registered in network " + this.id);
        }
        this.linkMap.put(link.getId(), link);
        fireEvent(Network.LINK_ADD_EVENT, link.getId());
        fireEvent(Network.ANIMATION_LINK_ADD_EVENT, link);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeLink(final Link link) throws NetworkException
    {
        if (!containsLink(link))
        {
            throw new NetworkException("Link " + link + " not registered in network " + this.id);
        }
        fireEvent(Network.LINK_REMOVE_EVENT, link.getId());
        fireEvent(Network.ANIMATION_LINK_REMOVE_EVENT, link);
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
    /************************ OBJECT INTERFACE IMPLEMENTING OBJECTS ************************/
    /***************************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final ImmutableMap<String, ObjectInterface> getObjectMap()
    {
        return new ImmutableHashMap<>(this.objectMap, Immutable.WRAP);
    }

    /**
     * @return the original ObjectMap; only to be used in the 'network' package for cloning.
     */
    final Map<String, ObjectInterface> getRawObjectMap()
    {
        return this.objectMap;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public final <T extends ObjectInterface> ImmutableMap<String, T> getObjectMap(final Class<T> objectType)
    {
        Map<String, T> result = new LinkedHashMap<>();
        for (String key : this.objectMap.keySet())
        {
            ObjectInterface o = this.objectMap.get(key);
            if (objectType.isInstance(o))
            {
                result.put(key, (T) o);
            }
        }
        return new ImmutableHashMap<>(result, Immutable.WRAP);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public final <T extends ObjectInterface> T getObject(final Class<T> objectType, final String objectId)
    {
        for (Entry<String, ObjectInterface> entry : this.objectMap.entrySet())
        {
            if (entry.getKey().equals(objectId) && objectType.isInstance(entry.getValue()))
            {
                return (T) entry.getValue();
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final void addObject(final ObjectInterface object) throws NetworkException
    {
        if (containsObject(object))
        {
            throw new NetworkException("Object " + object + " already registered in network " + this.id);
        }
        if (containsObject(object.getFullId()))
        {
            throw new NetworkException("Object with name " + object.getFullId() + " already registered in network " + this.id);
        }
        this.objectMap.put(object.getFullId(), object);
        fireEvent(Network.OBJECT_ADD_EVENT, object.getFullId());
        fireEvent(Network.ANIMATION_OBJECT_ADD_EVENT, object);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeObject(final ObjectInterface object) throws NetworkException
    {
        if (!containsObject(object))
        {
            throw new NetworkException("Object " + object + " not registered in network " + this.id);
        }
        fireEvent(Network.OBJECT_REMOVE_EVENT, object.getFullId());
        fireEvent(Network.ANIMATION_OBJECT_REMOVE_EVENT, object);
        this.objectMap.remove(object.getFullId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsObject(final ObjectInterface object)
    {
        return this.objectMap.containsKey(object.getFullId());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that the objectId should be the <b>fullId</b> of the object, including any additions such as lane ids, link ids,
     * etc.
     */
    @Override
    public final boolean containsObject(final String objectId)
    {
        return this.objectMap.containsKey(objectId);
    }

    /***************************************************************************************/
    /********************************* INVISIBLE OBJECTS ***********************************/
    /***************************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final ImmutableMap<String, InvisibleObjectInterface> getInvisibleObjectMap()
    {
        return new ImmutableHashMap<>(this.invisibleObjectMap, Immutable.WRAP);
    }

    /**
     * @return the original InvisibleObjectMap; only to be used in the 'network' package for cloning.
     */
    final Map<String, InvisibleObjectInterface> getRawInvisibleObjectMap()
    {
        return this.invisibleObjectMap;
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableMap<String, InvisibleObjectInterface> getInvisibleObjectMap(
            final Class<InvisibleObjectInterface> objectType)
    {
        Map<String, InvisibleObjectInterface> result = new LinkedHashMap<>();
        for (String key : this.objectMap.keySet())
        {
            InvisibleObjectInterface o = this.invisibleObjectMap.get(key);
            if (objectType.isInstance(o))
            {
                result.put(key, o);
            }
        }
        return new ImmutableHashMap<>(result, Immutable.WRAP);
    }

    /** {@inheritDoc} */
    @Override
    public final void addInvisibleObject(final InvisibleObjectInterface object) throws NetworkException
    {
        if (containsInvisibleObject(object))
        {
            throw new NetworkException("InvisibleObject " + object + " already registered in network " + this.id);
        }
        if (containsInvisibleObject(object.getFullId()))
        {
            throw new NetworkException(
                    "InvisibleObject with name " + object.getFullId() + " already registered in network " + this.id);
        }
        this.invisibleObjectMap.put(object.getFullId(), object);
        fireEvent(Network.INVISIBLE_OBJECT_ADD_EVENT, object.getFullId());
        fireEvent(Network.ANIMATION_INVISIBLE_OBJECT_ADD_EVENT, object);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeInvisibleObject(final InvisibleObjectInterface object) throws NetworkException
    {
        if (!containsInvisibleObject(object))
        {
            throw new NetworkException("InvisibleObject " + object + " not registered in network " + this.id);
        }
        fireEvent(Network.INVISIBLE_OBJECT_REMOVE_EVENT, object.getFullId());
        fireEvent(Network.ANIMATION_INVISIBLE_OBJECT_REMOVE_EVENT, object);
        this.objectMap.remove(object.getFullId());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsInvisibleObject(final InvisibleObjectInterface object)
    {
        return this.invisibleObjectMap.containsKey(object.getFullId());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that the objectId should be the <b>fullId</b> of the object, including any additions such as lane ids, link ids,
     * etc.
     */
    @Override
    public final boolean containsInvisibleObject(final String objectId)
    {
        return this.invisibleObjectMap.containsKey(objectId);
    }

    /***************************************************************************************/
    /*************************************** ROUTES ****************************************/
    /***************************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final ImmutableMap<String, Route> getDefinedRouteMap(final GTUType gtuType)
    {
        Map<String, Route> routes = new LinkedHashMap<>();
        if (this.routeMap.containsKey(gtuType))
        {
            routes.putAll(this.routeMap.get(gtuType));
        }
        return new ImmutableHashMap<>(routes, Immutable.WRAP);
    }

    /** {@inheritDoc} */
    @Override
    public final void addRoute(final GTUType gtuType, final Route route) throws NetworkException
    {
        if (containsRoute(gtuType, route))
        {
            throw new NetworkException(
                    "Route " + route + " for GTUType " + gtuType + " already registered in network " + this.id);
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
            this.routeMap.put(gtuType, new LinkedHashMap<String, Route>());
        }
        this.routeMap.get(gtuType).put(route.getId(), route);
        fireEvent(Network.ROUTE_ADD_EVENT, new Object[] {gtuType.getId(), route.getId()});
        fireEvent(Network.ANIMATION_ROUTE_ADD_EVENT, new Object[] {gtuType, route});
    }

    /** {@inheritDoc} */
    @Override
    public final void removeRoute(final GTUType gtuType, final Route route) throws NetworkException
    {
        if (!containsRoute(gtuType, route))
        {
            throw new NetworkException("Route " + route + " for GTUType " + gtuType + " not registered in network " + this.id);
        }
        fireEvent(Network.ROUTE_REMOVE_EVENT, new Object[] {gtuType.getId(), route.getId()});
        fireEvent(Network.ANIMATION_ROUTE_REMOVE_EVENT, new Object[] {gtuType, route});
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

    /**
     * Returns the route with given id or {@code null} if no such route is available.
     * @param routeId String; route id
     * @return route with given id or {@code null} if no such route is available
     */
    public final Route getRoute(final String routeId)
    {
        for (GTUType gtuType : this.routeMap.keySet())
        {
            Route route = this.routeMap.get(gtuType).get(routeId);
            if (route != null)
            {
                return route;
            }
        }
        return null;
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
        Set<Route> routes = new LinkedHashSet<>();
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
        SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> graph = buildGraph(gtuType, LinkWeight.LENGTH_NO_CONNECTORS);
        this.linkGraphs.put(gtuType, graph);
    }

    /**
     * Builds a graph using the specified link weight.
     * @param gtuType GTUType; GTU type
     * @param linkWeight LinkWeight; link weight
     * @return SimpleDirectedWeightedGraph graph
     */
    private SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> buildGraph(final GTUType gtuType, final LinkWeight linkWeight)
    {
        // TODO: take connections into account, and possibly do node expansion to build the graph
        @SuppressWarnings({"unchecked"})
        // TODO: the next line with .class has problems compiling... So used a dirty hack instead for now...
        Class<LinkEdge<Link>> linkEdgeClass = (Class<LinkEdge<Link>>) new LinkEdge<OTSLink>(null).getClass();
        SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> graph = new SimpleDirectedWeightedGraph<>(linkEdgeClass);
        for (Node node : this.nodeMap.values())
        {
            graph.addVertex(node);
        }
        for (Link link : this.linkMap.values())
        {
            // determine if the link is accessible for the GTUType , and in which direction(s)
            LongitudinalDirectionality directionality = link.getDirectionality(gtuType);
            if (directionality.isForwardOrBoth())
            {
                LinkEdge<Link> linkEdge = new LinkEdge<>(link);
                graph.addEdge(link.getStartNode(), link.getEndNode(), linkEdge);
                graph.setEdgeWeight(linkEdge, linkWeight.getWeight(link));
            }
            if (directionality.isBackwardOrBoth())
            {
                LinkEdge<Link> linkEdge = new LinkEdge<>(link);
                graph.addEdge(link.getEndNode(), link.getStartNode(), linkEdge);
                graph.setEdgeWeight(linkEdge, linkWeight.getWeight(link));
            }
        }
        return graph;
    }

    /** {@inheritDoc} */
    @Override
    public final CompleteRoute getShortestRouteBetween(final GTUType gtuType, final Node nodeFrom, final Node nodeTo,
            final LinkWeight linkWeight) throws NetworkException
    {
        CompleteRoute route = new CompleteRoute("Route for " + gtuType + " from " + nodeFrom + "to " + nodeTo, gtuType);
        SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> graph = getGraph(gtuType, linkWeight);
        // DijkstraShortestPath<Node, LinkEdge<Link>> dijkstra = new DijkstraShortestPath<>(graph);
        // GraphPath<Node, LinkEdge<Link>> path = dijkstra.getPath(nodeFrom, nodeTo);
        GraphPath<Node, LinkEdge<Link>> path = DijkstraShortestPath.findPathBetween(graph, nodeFrom, nodeTo);
        if (path == null)
        {
            SimLogger.always().debug("No path from " + nodeFrom + " to " + nodeTo + " for gtuType " + gtuType);
            return null;
        }
        route.addNode(nodeFrom);
        for (LinkEdge<Link> link : path.getEdgeList())
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
        return getShortestRouteBetween(gtuType, nodeFrom, nodeTo, nodesVia, LinkWeight.LENGTH);
    }

    /** {@inheritDoc} */
    @Override
    public final CompleteRoute getShortestRouteBetween(final GTUType gtuType, final Node nodeFrom, final Node nodeTo,
            final List<Node> nodesVia, final LinkWeight linkWeight) throws NetworkException
    {
        CompleteRoute route = new CompleteRoute(
                "Route for " + gtuType + " from " + nodeFrom + "to " + nodeTo + " via " + nodesVia.toString(), gtuType);
        SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> graph = getGraph(gtuType, linkWeight);
        List<Node> nodes = new ArrayList<>();
        nodes.add(nodeFrom);
        nodes.addAll(nodesVia);
        nodes.add(nodeTo);
        Node from = nodeFrom;
        route.addNode(nodeFrom);
        for (int i = 1; i < nodes.size(); i++)
        {
            Node to = nodes.get(i);
            DijkstraShortestPath<Node, LinkEdge<Link>> dijkstra = new DijkstraShortestPath<>(graph);
            GraphPath<Node, LinkEdge<Link>> path = dijkstra.getPath(from, to);
            if (path == null)
            {
                SimLogger.always().debug("Cannot find a path from " + nodeFrom + " via " + nodesVia + " to " + nodeTo
                        + " (failing between " + from + " and " + to + ")");
                //dijkstra.getPath(from, to);
                return null;
            }
            // System.out.println("Path:");
            // for (LinkEdge<Link> link : path.getEdgeList())
            // {
            // System.out.println(" " + link);
            // }
            for (LinkEdge<Link> linkEdge : path.getEdgeList())
            {
                if (linkEdge.getLink().getEndNode().equals(route.destinationNode()))
                {
                    System.out.println("Let op");
                }
                if (!linkEdge.getLink().getEndNode().equals(route.destinationNode())
                        && route.destinationNode().isDirectionallyConnectedTo(gtuType, linkEdge.getLink().getEndNode()))
                {
                    route.addNode(linkEdge.getLink().getEndNode());
                }
                else if (!linkEdge.getLink().getStartNode().equals(route.destinationNode())
                        && route.destinationNode().isDirectionallyConnectedTo(gtuType, linkEdge.getLink().getStartNode()))
                {
                    route.addNode(linkEdge.getLink().getStartNode());
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

    /**
     * Returns the graph, possibly a stored one.
     * @param gtuType GTUType; GTU type
     * @param linkWeight LinkWeight; link weight
     * @return SimpleDirectedWeightedGraph
     */
    private SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> getGraph(final GTUType gtuType, final LinkWeight linkWeight)
    {
        SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> graph;
        if (linkWeight.equals(LinkWeight.LENGTH))
        {
            // stored default
            if (!this.linkGraphs.containsKey(gtuType))
            {
                buildGraph(gtuType);
            }
            graph = this.linkGraphs.get(gtuType);
        }
        else
        {
            graph = buildGraph(gtuType, linkWeight);
        }
        return graph;
    }

    /**
     * @return a defensive copy of the routeMap.
     */
    public final ImmutableMap<GTUType, Map<String, Route>> getRouteMap()
    {
        return new ImmutableHashMap<>(this.routeMap, Immutable.WRAP);
    }

    /**
     * @return routeMap; only to be used in the 'network' package for cloning.
     */
    final Map<GTUType, Map<String, Route>> getRawRouteMap()
    {
        return this.routeMap;
    }

    /**
     * @param newRouteMap Map&lt;GTUType,Map&lt;String,Route&gt;&gt;; the routeMap to set, only to be used in the 'network'
     *            package for cloning.
     */
    public final void setRawRouteMap(final Map<GTUType, Map<String, Route>> newRouteMap)
    {
        this.routeMap = newRouteMap;
    }

    /**
     * @return linkGraphs; only to be used in the 'network' package for cloning.
     */
    public final ImmutableMap<GTUType, SimpleDirectedWeightedGraph<Node, LinkEdge<Link>>> getLinkGraphs()
    {
        return new ImmutableHashMap<>(this.linkGraphs, Immutable.WRAP);
    }

    /**
     * @return linkGraphs; only to be used in the 'network' package for cloning.
     */
    final Map<GTUType, SimpleDirectedWeightedGraph<Node, LinkEdge<Link>>> getRawLinkGraphs()
    {
        return this.linkGraphs;
    }

    /***************************************************************************************/
    /************************************** LinkTypes **************************************/
    /***************************************************************************************/

    /** {@inheritDoc} */
    @Override
    public void addDefaultLinkTypes()
    {
        GTUCompatibility<LinkType> compatibility = new GTUCompatibility<>((LinkType) null);
        new LinkType("NONE", null, compatibility, this);
        //
        compatibility = new GTUCompatibility<>((LinkType) null);
        compatibility.addAllowedGTUType(getGtuType(GTUType.DEFAULTS.ROAD_USER), LongitudinalDirectionality.DIR_BOTH);
        LinkType road = new LinkType("ROAD", null, compatibility, this);
        //
        compatibility = new GTUCompatibility<>((LinkType) null);
        compatibility.addAllowedGTUType(getGtuType(GTUType.DEFAULTS.ROAD_USER), LongitudinalDirectionality.DIR_PLUS);
        compatibility.addAllowedGTUType(getGtuType(GTUType.DEFAULTS.PEDESTRIAN), LongitudinalDirectionality.DIR_NONE);
        compatibility.addAllowedGTUType(getGtuType(GTUType.DEFAULTS.BICYCLE), LongitudinalDirectionality.DIR_NONE);
        new LinkType("FREEWAY", road, compatibility, this);
        //
        compatibility = new GTUCompatibility<>((LinkType) null);
        compatibility.addAllowedGTUType(getGtuType(GTUType.DEFAULTS.WATERWAY_USER), LongitudinalDirectionality.DIR_BOTH);
        new LinkType("WATERWAY", null, compatibility, this);
        //
        compatibility = new GTUCompatibility<>((LinkType) null);
        compatibility.addAllowedGTUType(getGtuType(GTUType.DEFAULTS.RAILWAY_USER), LongitudinalDirectionality.DIR_BOTH);
        new LinkType("RAILWAY", null, compatibility, this);
        //
        compatibility = new GTUCompatibility<>((LinkType) null);
        compatibility.addAllowedGTUType(getGtuType(GTUType.DEFAULTS.ROAD_USER), LongitudinalDirectionality.DIR_PLUS);
        compatibility.addAllowedGTUType(getGtuType(GTUType.DEFAULTS.WATERWAY_USER), LongitudinalDirectionality.DIR_PLUS);
        compatibility.addAllowedGTUType(getGtuType(GTUType.DEFAULTS.RAILWAY_USER), LongitudinalDirectionality.DIR_PLUS);
        new LinkType("CONNECTOR", null, compatibility, this);
    }

    /** {@inheritDoc} */
    @Override
    public void addLinkType(final LinkType linkType)
    {
        this.linkTypeMap.put(linkType.getId(), linkType);
    }

    /** {@inheritDoc} */
    @Override
    public LinkType getLinkType(final String linkId)
    {
        return this.linkTypeMap.get(linkId);
    }

    /** {@inheritDoc} */
    @Override
    public LinkType getLinkType(final LinkType.DEFAULTS linkEnum)
    {
        return this.linkTypeMap.get(linkEnum.getId());
    }

    /** {@inheritDoc} */
    @Override
    public ImmutableMap<String, LinkType> getLinkTypes()
    {
        return new ImmutableHashMap<>(this.linkTypeMap, Immutable.WRAP);
    }

    /***************************************************************************************/
    /************************************** GTUTypes ***************************************/
    /***************************************************************************************/

    /** {@inheritDoc} */
    @Override
    public void addDefaultGtuTypes()
    {
        GTUType roadUser = new GTUType("ROAD_USER", this);
        GTUType waterwayUser = new GTUType("WATERWAY_USER", this);
        GTUType railwayUser = new GTUType("RAILWAY_USER", this);

        new GTUType("SHIP", waterwayUser);
        new GTUType("TRAIN", railwayUser);
        new GTUType("PEDESTRIAN", roadUser);
        GTUType bicycle = new GTUType("BICYCLE", roadUser);

        new GTUType("MOPED", bicycle);

        GTUType vehicle = new GTUType("VEHICLE", roadUser);
        new GTUType("EMERGENCY_VEHICLE", vehicle);
        new GTUType("CAR", vehicle);
        new GTUType("VAN", vehicle);
        GTUType bus = new GTUType("BUS", vehicle);
        new GTUType("TRUCK", vehicle);
        new GTUType("SCHEDULED_BUS", bus);
    }

    /** {@inheritDoc} */
    @Override
    public void addGtuType(final GTUType gtuType)
    {
        this.gtuTypeMap.put(gtuType.getId(), gtuType);
    }

    /** {@inheritDoc} */
    @Override
    public GTUType getGtuType(final String gtuId)
    {
        return this.gtuTypeMap.get(gtuId);
    }

    /** {@inheritDoc} */
    @Override
    public GTUType getGtuType(final GTUType.DEFAULTS gtuEnum)
    {
        return this.gtuTypeMap.get(gtuEnum.getId());
    }

    /** {@inheritDoc} */
    @Override
    public ImmutableMap<String, GTUType> getGtuTypes()
    {
        return new ImmutableHashMap<>(this.gtuTypeMap, Immutable.WRAP);
    }

    /***************************************************************************************/
    /**************************************** GTUs *****************************************/
    /***************************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final void addGTU(final GTU gtu)
    {
        this.gtuMap.put(gtu.getId(), gtu);
        fireTimedEvent(Network.GTU_ADD_EVENT, gtu.getId(), gtu.getSimulator().getSimulatorTime());
        fireTimedEvent(Network.ANIMATION_GTU_ADD_EVENT, gtu, gtu.getSimulator().getSimulatorTime());
    }

    /** {@inheritDoc} */
    @Override
    public final void removeGTU(final GTU gtu)
    {
        fireTimedEvent(Network.GTU_REMOVE_EVENT, gtu.getId(), gtu.getSimulator().getSimulatorTime());
        fireTimedEvent(Network.ANIMATION_GTU_REMOVE_EVENT, gtu, gtu.getSimulator().getSimulatorTime());
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
        return new LinkedHashSet<>(this.gtuMap.values());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsGtuId(final String gtuId)
    {
        return this.gtuMap.containsKey(gtuId);
    }

    /**
     * @return gtuMap
     */
    final Map<String, GTU> getRawGtuMap()
    {
        return this.gtuMap;
    }

    /***************************************************************************************/

    /** Extra clearance around boundaries of network as fraction of width and height. */
    public static final double EXTENT_MARGIN = 0.05;

    /**
     * Calculate the extent of the network based on the network objects' locations and return the dimensions.
     * @return Rectangle2D.Double; the extent of the network
     */
    public Rectangle2D.Double getExtent()
    {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        boolean content = false;
        Point3d p3dL = new Point3d();
        Point3d p3dU = new Point3d();
        try
        {
            for (Node node : this.nodeMap.values())
            {
                BoundingBox b = new BoundingBox(node.getBounds());
                b.getLower(p3dL);
                b.getUpper(p3dU);
                minX = Math.min(minX, node.getLocation().x + Math.min(p3dL.x, p3dU.x));
                minY = Math.min(minY, node.getLocation().y + Math.min(p3dL.y, p3dU.y));
                maxX = Math.max(maxX, node.getLocation().x + Math.max(p3dL.x, p3dU.x));
                maxY = Math.max(maxY, node.getLocation().y + Math.max(p3dL.y, p3dU.y));
                content = true;
            }
            for (Link link : this.linkMap.values())
            {
                BoundingBox b = new BoundingBox(link.getBounds());
                b.getLower(p3dL);
                b.getUpper(p3dU);
                minX = Math.min(minX, link.getLocation().x + Math.min(p3dL.x, p3dU.x));
                minY = Math.min(minY, link.getLocation().y + Math.min(p3dL.y, p3dU.y));
                maxX = Math.max(maxX, link.getLocation().x + Math.max(p3dL.x, p3dU.x));
                maxY = Math.max(maxY, link.getLocation().y + Math.max(p3dL.y, p3dU.y));
                content = true;
            }
            for (ObjectInterface object : this.objectMap.values())
            {
                BoundingBox b = new BoundingBox(object.getBounds());
                b.getLower(p3dL);
                b.getUpper(p3dU);
                minX = Math.min(minX, object.getLocation().x + Math.min(p3dL.x, p3dU.x));
                minY = Math.min(minY, object.getLocation().y + Math.min(p3dL.y, p3dU.y));
                maxX = Math.max(maxX, object.getLocation().x + Math.max(p3dL.x, p3dU.x));
                maxY = Math.max(maxY, object.getLocation().y + Math.max(p3dL.y, p3dU.y));
                content = true;
            }
        }
        catch (RemoteException exception)
        {
            SimLogger.always().error(exception);
        }
        if (content)
        {
            double relativeMargin = EXTENT_MARGIN;
            double xMargin = relativeMargin * (maxX - minX);
            double yMargin = relativeMargin * (maxY - minY);
            return new Rectangle2D.Double(minX - xMargin / 2, minY - yMargin / 2, maxX - minX + xMargin, maxY - minY + yMargin);
        }
        else
        {
            return new Rectangle2D.Double(-500, -500, 1000, 1000);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OTSNetwork [id=" + this.id + ", nodeMapSize=" + this.nodeMap.size() + ", linkMapSize=" + this.linkMap.size()
                + ", objectMapSize=" + this.objectMap.size() + ", routeMapSize=" + this.routeMap.size() + ", gtuMapSize="
                + this.gtuMap.size() + "]";
    }

}

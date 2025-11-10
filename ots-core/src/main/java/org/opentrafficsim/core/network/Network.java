package org.opentrafficsim.core.network;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.event.EventProducer;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableHashMap;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.logger.CategoryLogger;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.multikeymap.MultiKeyMap;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.object.LocatedObject;
import org.opentrafficsim.core.object.NonLocatedObject;
import org.opentrafficsim.core.perception.PerceivableContext;

/**
 * A Network consists of a set of links. Each link has, in its turn, a start node and an end node.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Jul 22, 2015 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class Network extends LocalEventProducer implements PerceivableContext, EventProducer
{
    /** Id of this network. */
    private final String id;

    /** Map of Nodes. */
    private Map<String, Node> nodeMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Map of Links. */
    private Map<String, Link> linkMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Map of LocatedObject. */
    private Map<String, LocatedObject> objectMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Map of NonLocatedObjects. */
    private Map<String, NonLocatedObject> nonLocatedObjectMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Map of Routes. */
    private Map<GtuType, Map<String, Route>> routeMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Graphs to calculate shortest paths per GtuType and LinkWeight. */
    private MultiKeyMap<SimpleDirectedWeightedGraph<Node, Link>> linkGraphs =
            new MultiKeyMap<>(GtuType.class, LinkWeight.class);

    /** GTUs registered in this network. */
    private Map<String, Gtu> gtuMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /** The DSOL simulator engine. */
    private final OtsSimulatorInterface simulator;

    /**
     * Construction of an empty network.
     * @param id the network id.
     * @param simulator the DSOL simulator engine
     */
    public Network(final String id, final OtsSimulatorInterface simulator)
    {
        this.id = id;
        this.simulator = simulator;
    }

    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * Return the simulator.
     * @return the simulator
     */
    public OtsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /***************************************************************************************/
    /**************************************** NODES ****************************************/
    /***************************************************************************************/

    /**
     * Provide an immutable map of node ids to nodes in the network.
     * @return an immutable map of nodes.
     */
    public final ImmutableMap<String, Node> getNodeMap()
    {
        return new ImmutableHashMap<>(this.nodeMap, Immutable.WRAP);
    }

    /**
     * Return node map.
     * @return only to be used in the 'network' package for cloning.
     */
    final Map<String, Node> getRawNodeMap()
    {
        return this.nodeMap;
    }

    /**
     * Register a node in the network.
     * @param node the node to add to the network.
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    public final void addNode(final Node node) throws NetworkException
    {
        if (containsNode(node))
        {
            throw new NetworkException("Node " + node + " already registered in network " + this.id);
        }
        this.nodeMap.put(node.getId(), node);
        fireTimedEvent(Network.NODE_ADD_EVENT, node.getId(), getSimulator().getSimulatorTime());
    }

    /**
     * Unregister a node from the network.
     * @param node the node to remove from the network.
     * @throws NetworkException if node does not exist in the network.
     */
    public final void removeNode(final Node node) throws NetworkException
    {
        if (!containsNode(node))
        {
            throw new NetworkException("Node " + node + " not registered in network " + this.id);
        }
        fireTimedEvent(Network.NODE_REMOVE_EVENT, node.getId(), getSimulator().getSimulatorTime());
        this.nodeMap.remove(node.getId());
    }

    /**
     * Test whether a node is present in the network.
     * @param node the node to search for in the network.
     * @return whether the node is in this network
     */
    public final boolean containsNode(final Node node)
    {
        // System.out.println(node);
        return this.nodeMap.keySet().contains(node.getId());
    }

    /**
     * Test whether a node with a given id is present in the network.
     * @param nodeId the id of the node to search for in the network.
     * @return whether the node is in this network
     */
    public final boolean containsNode(final String nodeId)
    {
        return this.nodeMap.keySet().contains(nodeId);
    }

    /**
     * Retrieve a node with a given id from the network, or null if the id cannot be found.
     * @param nodeId the id of the node to search for in the network.
     * @return the node or null if not present
     */
    public final Node getNode(final String nodeId)
    {
        return this.nodeMap.get(nodeId);
    }

    /**
     * Return a list of Centroid nodes that have incoming connectors without corresponding outgoing connectors to the same node
     * or vice versa (which can be fully okay, especially when the lanes are a dead end, or when lanes / links only go in a
     * single direction).
     * @param gtuType the GTU type for which to check the connectors
     * @return a list of Centroid nodes that have incoming connectors without corresponding outgoing connectors to the same node
     *         or vice versa.
     */
    public List<Node> getUnbalancedCentroids(final GtuType gtuType)
    {
        List<Node> centroidList = new ArrayList<>();
        for (Node node : getRawNodeMap().values())
        {
            if (node.isCentroid())
            {
                boolean in = false;
                boolean out = false;
                for (Link link : node.getLinks())
                {
                    if (link.getEndNode().equals(node))
                    {
                        in = true;
                    }
                    else if (link.getStartNode().equals(node))
                    {
                        out = true;
                    }
                }
                if ((in && !out) || (out && !in))
                {
                    centroidList.add(node);
                }
            }
        }
        return centroidList;
    }

    /***************************************************************************************/
    /**************************************** LINKS ****************************************/
    /***************************************************************************************/

    /**
     * Provide an immutable map of link ids to links in the network.
     * @return the an immutable map of links.
     */
    public final ImmutableMap<String, Link> getLinkMap()
    {
        return new ImmutableHashMap<>(this.linkMap, Immutable.WRAP);
    }

    /**
     * Return link map.
     * @return only to be used in the 'network' package for cloning.
     */
    final Map<String, Link> getRawLinkMap()
    {
        return this.linkMap;
    }

    /**
     * Register a link in the network.
     * @param link the link to add to the network.
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
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
        fireTimedEvent(Network.LINK_ADD_EVENT, link.getId(), getSimulator().getSimulatorTime());
    }

    /**
     * Unregister a link from the network.
     * @param link the link to remove from the network.
     * @throws NetworkException if link does not exist in the network.
     */
    public final void removeLink(final Link link) throws NetworkException
    {
        if (!containsLink(link))
        {
            throw new NetworkException("Link " + link + " not registered in network " + this.id);
        }
        fireTimedEvent(Network.LINK_REMOVE_EVENT, link.getId(), getSimulator().getSimulatorTime());
        this.linkMap.remove(link.getId());
    }

    /**
     * Find a link between node1 and node2 and return it if it exists in the network. If not, return null.
     * @param node1 first node
     * @param node2 second node
     * @return the link between node1 and node2 in the network or null if it does not exist.
     */
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

    /**
     * Test whether a link is present in the network.
     * @param link the link to search for in the network.
     * @return whether the link is in this network
     */
    public final boolean containsLink(final Link link)
    {
        return this.linkMap.keySet().contains(link.getId());
    }

    /**
     * Test whether a link with a given id is present in the network.
     * @param linkId the id of the link to search for in the network.
     * @return whether the link is in this network
     */
    public final boolean containsLink(final String linkId)
    {
        return this.linkMap.keySet().contains(linkId);
    }

    /**
     * Find a link between node1 and node2 and return it if it exists in the network. If not, return null.
     * @param nodeId1 id of the first node
     * @param nodeId2 id of the second node
     * @return the link between node1 and node2 in the network or null if it does not exist.
     * @throws NetworkException if the node(s) cannot be found by their id
     */
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

    /**
     * Retrieve a node with a given id from the network, or null if the id cannot be found.
     * @param linkId the id of the link to search for in the network.
     * @return the link or null if not present
     */
    public final Link getLink(final String linkId)
    {
        return this.linkMap.get(linkId);
    }

    /***************************************************************************************/
    /************************ OBJECT INTERFACE IMPLEMENTING OBJECTS ************************/
    /***************************************************************************************/

    /**
     * Return an immutable map of all ObjectInterface implementing objects in the Network.
     * @return the immutable map of all ObjectInterface implementing objects in the Network
     */
    public final ImmutableMap<String, LocatedObject> getObjectMap()
    {
        return new ImmutableHashMap<>(this.objectMap, Immutable.WRAP);
    }

    /**
     * Return object map.
     * @return only to be used in the 'network' package for cloning.
     */
    final Map<String, LocatedObject> getRawObjectMap()
    {
        return this.objectMap;
    }

    /**
     * Return an immutable map of all ObjectInterface implementing objects in the network that are of type objectType, or any
     * sub type thereof.
     * @param objectType the (sub-)type of ObjectInterface that the returned map is reduced to
     * @param <T> type of object
     * @return the immutable map of all ObjectInterface implementing objects in the Network that are of the type objectType, or
     *         any sub type thereof
     */
    @SuppressWarnings("unchecked")
    public final <T extends LocatedObject> ImmutableMap<String, T> getObjectMap(final Class<T> objectType)
    {
        Map<String, T> result = new LinkedHashMap<>();
        for (String key : this.objectMap.keySet())
        {
            LocatedObject o = this.objectMap.get(key);
            if (objectType.isInstance(o))
            {
                result.put(key, (T) o);
            }
        }
        return new ImmutableHashMap<>(result, Immutable.WRAP);
    }

    /**
     * Return object of given type with given id.
     * @param objectType object type class
     * @param objectId id of object
     * @param <T> object type
     * @return object of given type with given id, {@code null} if no such object
     */
    @SuppressWarnings("unchecked")
    public final <T extends LocatedObject> T getObject(final Class<T> objectType, final String objectId)
    {
        for (Entry<String, LocatedObject> entry : this.objectMap.entrySet())
        {
            if (entry.getKey().equals(objectId) && objectType.isInstance(entry.getValue()))
            {
                return (T) entry.getValue();
            }
        }
        return null;
    }

    /**
     * Add an ObjectInterface implementing object to the Network.
     * @param object the object that implements ObjectInterface
     * @throws NetworkException if link already exists in the network, if name of the object is not unique.
     */
    public final void addObject(final LocatedObject object) throws NetworkException
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
        fireTimedEvent(Network.OBJECT_ADD_EVENT, object.getFullId(), getSimulator().getSimulatorTime());
    }

    /**
     * Remove an ObjectInterface implementing object form the Network.
     * @param object the object that implements ObjectInterface
     * @throws NetworkException if the object does not exist in the network.
     */
    public final void removeObject(final LocatedObject object) throws NetworkException
    {
        if (!containsObject(object))
        {
            throw new NetworkException("Object " + object + " not registered in network " + this.id);
        }
        fireTimedEvent(Network.OBJECT_REMOVE_EVENT, object.getFullId(), getSimulator().getSimulatorTime());
        this.objectMap.remove(object.getFullId());
    }

    /**
     * Test whether the object is present in the Network.
     * @param object the object that is tested for presence
     * @return whether the object is present in the Network
     */
    public final boolean containsObject(final LocatedObject object)
    {
        return this.objectMap.containsKey(object.getFullId());
    }

    /**
     * Test whether an object with the given id is present in the Network.
     * <p>
     * Note that the objectId should be the <b>fullId</b> of the object, including any additions such as lane ids, link ids,
     * etc.
     * @param objectId the id that is tested for presence
     * @return whether an object with the given id is present in the Network
     */
    public final boolean containsObject(final String objectId)
    {
        return this.objectMap.containsKey(objectId);
    }

    /***************************************************************************************/
    /******************************** NON LOCATED OBJECTS **********************************/
    /***************************************************************************************/

    /**
     * Return an immutable map of all NonLocatedObject implementing objects in the Network.
     * @return the immutable map of all NonLocatedObject implementing objects in the Network
     */
    public final ImmutableMap<String, NonLocatedObject> getNonLocatedObjectMap()
    {
        return new ImmutableHashMap<>(this.nonLocatedObjectMap, Immutable.WRAP);
    }

    /**
     * Get non-located object map.
     * @return only to be used in the 'network' package for cloning.
     */
    final Map<String, NonLocatedObject> getRawNonLocatedObjectMap()
    {
        return this.nonLocatedObjectMap;
    }

    /**
     * Return an immutable map of all NonLocatedObject implementing objects in the network that are of type objectType, or any
     * sub type thereof.
     * @param objectType the (sub-)type of NonLocatedObject that the returned map is reduced to
     * @return the immutable map of all NonLocatedObject implementing objects in the Network that are of the type objectType, or
     *         any sub type thereof
     */
    public final ImmutableMap<String, NonLocatedObject> getNonLocatedObjectMap(final Class<NonLocatedObject> objectType)
    {
        Map<String, NonLocatedObject> result = new LinkedHashMap<>();
        for (String key : this.objectMap.keySet())
        {
            NonLocatedObject o = this.nonLocatedObjectMap.get(key);
            if (objectType.isInstance(o))
            {
                result.put(key, o);
            }
        }
        return new ImmutableHashMap<>(result, Immutable.WRAP);
    }

    /**
     * Add a NonLocatedObject implementing object to the Network.
     * @param object the object that implements ObjectInterface
     * @throws NetworkException if link already exists in the network, if name of the object is not unique.
     */
    public final void addNonLocatedObject(final NonLocatedObject object) throws NetworkException
    {
        if (containsNonLocatedObject(object))
        {
            throw new NetworkException("NonLocatedObject " + object + " already registered in network " + this.id);
        }
        if (containsNonLocatedObject(object.getFullId()))
        {
            throw new NetworkException(
                    "NonLocatedObject with name " + object.getFullId() + " already registered in network " + this.id);
        }
        this.nonLocatedObjectMap.put(object.getFullId(), object);
        fireTimedEvent(Network.NONLOCATED_OBJECT_ADD_EVENT, object.getFullId(), getSimulator().getSimulatorTime());
    }

    /**
     * Remove a NonLocatedObject implementing object form the Network.
     * @param object the object that implements ObjectInterface
     * @throws NetworkException if the object does not exist in the network.
     */
    public final void removeNonLocatedObject(final NonLocatedObject object) throws NetworkException
    {
        if (!containsNonLocatedObject(object))
        {
            throw new NetworkException("NonLocatedObject " + object + " not registered in network " + this.id);
        }
        fireTimedEvent(Network.NONLOCATED_OBJECT_REMOVE_EVENT, object.getFullId(), getSimulator().getSimulatorTime());
        this.objectMap.remove(object.getFullId());
    }

    /**
     * Test whether the NonLocatedObject is present in the Network.
     * @param object the object that is tested for presence
     * @return whether the invisible object is present in the Network
     */
    public final boolean containsNonLocatedObject(final NonLocatedObject object)
    {
        return this.nonLocatedObjectMap.containsKey(object.getFullId());
    }

    /**
     * Test whether an NonLocatedObject object with the given id is present in the Network.
     * <p>
     * Note that the objectId should be the <b>fullId</b> of the object, including any additions such as lane ids, link ids,
     * etc.
     * @param objectId the id that is tested for presence
     * @return whether an invisible object with the given id is present in the Network
     */
    public final boolean containsNonLocatedObject(final String objectId)
    {
        return this.nonLocatedObjectMap.containsKey(objectId);
    }

    /***************************************************************************************/
    /*************************************** ROUTES ****************************************/
    /***************************************************************************************/

    /**
     * Return an immutable map of routes that exist in the network for the GtuType.
     * @param gtuType the GtuType for which to retrieve the defined routes
     * @return an immutable map of routes in the network for the given GtuType, or an empty Map if no routes are defined for the
     *         given GtuType.
     */
    public final ImmutableMap<String, Route> getDefinedRouteMap(final GtuType gtuType)
    {
        Map<String, Route> routes = new LinkedHashMap<>();
        if (this.routeMap.containsKey(gtuType))
        {
            routes.putAll(this.routeMap.get(gtuType));
        }
        return new ImmutableHashMap<>(routes, Immutable.WRAP);
    }

    /**
     * Add a route to the network.
     * @param gtuType the GtuType for which to add a route
     * @param route the route to add to the network.
     * @throws NetworkException if route already exists in the network, if name of the route is not unique, if one of the nodes
     *             of the route are not registered in the network.
     */
    public final void addRoute(final GtuType gtuType, final Route route) throws NetworkException
    {
        if (containsRoute(gtuType, route))
        {
            throw new NetworkException(
                    "Route " + route + " for GtuType " + gtuType + " already registered in network " + this.id);
        }
        if (this.routeMap.containsKey(gtuType) && this.routeMap.get(gtuType).keySet().contains(route.getId()))
        {
            throw new NetworkException("Route with name " + route.getId() + " for GtuType " + gtuType
                    + " already registered in network " + this.id);
        }
        for (Node node : route.getNodes())
        {
            if (!containsNode(node))
            {
                throw new NetworkException("Node " + node.getId() + " of route " + route.getId() + " for GtuType " + gtuType
                        + " not registered in network " + this.id);
            }
        }
        if (!this.routeMap.containsKey(gtuType))
        {
            this.routeMap.put(gtuType, new LinkedHashMap<String, Route>());
        }
        this.routeMap.get(gtuType).put(route.getId(), route);
        fireTimedEvent(Network.ROUTE_ADD_EVENT, new Object[] {gtuType.getId(), route.getId()},
                getSimulator().getSimulatorTime());
    }

    /**
     * Remove the route from the network, e.g. because of road maintenance.
     * @param gtuType the GtuType for which to remove a route
     * @param route the route to remove from the network.
     * @throws NetworkException if route does not exist in the network.
     */
    public final void removeRoute(final GtuType gtuType, final Route route) throws NetworkException
    {
        if (!containsRoute(gtuType, route))
        {
            throw new NetworkException("Route " + route + " for GtuType " + gtuType + " not registered in network " + this.id);
        }
        fireTimedEvent(Network.ROUTE_REMOVE_EVENT, new Object[] {gtuType.getId(), route.getId()},
                getSimulator().getSimulatorTime());
        this.routeMap.get(gtuType).remove(route.getId());
    }

    /**
     * Determine whether the provided route exists in the network for the given GtuType.
     * @param gtuType the GtuType for which to check whether the route exists
     * @param route the route to check for
     * @return whether the route exists in the network for the given GtuType
     */
    public final boolean containsRoute(final GtuType gtuType, final Route route)
    {
        if (this.routeMap.containsKey(gtuType))
        {
            return this.routeMap.get(gtuType).values().contains(route);
        }
        return false;
    }

    /**
     * Determine whether a route with the given id exists in the network for the given GtuType.
     * @param gtuType the GtuType for which to check whether the route exists
     * @param routeId the id of the route to check for
     * @return whether a route with the given id exists in the network for the given GtuType
     */
    public final boolean containsRoute(final GtuType gtuType, final String routeId)
    {
        if (this.routeMap.containsKey(gtuType))
        {
            return this.routeMap.get(gtuType).keySet().contains(routeId);
        }
        return false;
    }

    /**
     * Returns the route with given id or {@code null} if no such route is available.
     * @param routeId route id
     * @return route with given id or {@code null} if no such route is available
     */
    public final Route getRoute(final String routeId)
    {
        for (GtuType gtuType : this.routeMap.keySet())
        {
            Route route = this.routeMap.get(gtuType).get(routeId);
            if (route != null)
            {
                return route;
            }
        }
        return null;
    }

    /**
     * Return the route with the given id in the network for the given GtuType, or null if it the route with the id does not
     * exist.
     * @param gtuType the GtuType for which to retrieve a route based on its id.
     * @param routeId the route to search for in the network.
     * @return the route or null if not present
     */
    public final Route getRoute(final GtuType gtuType, final String routeId)
    {
        if (this.routeMap.containsKey(gtuType))
        {
            return this.routeMap.get(gtuType).get(routeId);
        }
        return null;
    }

    /**
     * Return the the shortest route between two nodes in the network, via a list of intermediate nodes. If no path exists from
     * the start node to the end node via the intermediate nodes in the network, null is returned.
     * @param gtuType the GtuType for which to retrieve the defined routes
     * @param nodeFrom the start node.
     * @param nodeTo the end node.
     * @return if no route can be found, an empty set is returned.
     */
    public final Set<Route> getRoutesBetween(final GtuType gtuType, final Node nodeFrom, final Node nodeTo)
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

    /**
     * Calculate the shortest route between two nodes in the network. If no path exists from the start node to the end node in
     * the network, null is returned. This method returns a CompleteRoute, which includes all nodes to get from start to end. In
     * case the graph for the GtuType has not yet been built, this method will call the buildGraph method.
     * @param gtuType the GtuType for which to calculate the shortest route
     * @param nodeFrom the start node.
     * @param nodeTo the end node.
     * @return the shortest route from the start Node to the end Node in the network. If no path exists from the start node to
     *         the end node in the network, null is returned.
     * @throws NetworkException in case nodes cannot be added to the route, e.g. because they are not directly connected. This
     *             can be the case when the links in the network have changed, but the graph has not been rebuilt.
     */
    public Route getShortestRouteBetween(final GtuType gtuType, final Node nodeFrom, final Node nodeTo) throws NetworkException
    {
        return getShortestRouteBetween(gtuType, nodeFrom, nodeTo, LinkWeight.LENGTH);
    }

    /**
     * Builds a graph using the specified link weight.
     * @param gtuType GTU type
     * @param linkWeight link weight
     * @return SimpleDirectedWeightedGraph graph
     */
    private SimpleDirectedWeightedGraph<Node, Link> buildGraph(final GtuType gtuType, final LinkWeight linkWeight)
    {
        // TODO: take connections into account, and possibly do node expansion to build the graph
        SimpleDirectedWeightedGraph<Node, Link> graph = new SimpleDirectedWeightedGraph<>(Link.class);
        for (Node node : this.nodeMap.values())
        {
            graph.addVertex(node);
        }
        for (Link link : this.linkMap.values())
        {
            // determine if the link is accessible for the GtuType , and in which direction(s)
            graph.addEdge(link.getStartNode(), link.getEndNode(), link);
            graph.setEdgeWeight(link, linkWeight.getWeight(link));
        }
        return graph;
    }

    /**
     * Calculate the shortest route between two nodes in the network. If no path exists from the start node to the end node in
     * the network, null is returned. This method returns a CompleteRoute, which includes all nodes to get from start to end.
     * This method recalculates the graph.
     * @param gtuType the GtuType for which to calculate the shortest route
     * @param nodeFrom the start node.
     * @param nodeTo the end node.
     * @param linkWeight link weight.
     * @return the shortest route from the start Node to the end Node in the network. If no path exists from the start node to
     *         the end node in the network, null is returned.
     * @throws NetworkException in case nodes cannot be added to the route, e.g. because they are not directly connected. This
     *             can be the case when the links in the network have changed, but the graph has not been rebuilt.
     */
    public final Route getShortestRouteBetween(final GtuType gtuType, final Node nodeFrom, final Node nodeTo,
            final LinkWeight linkWeight) throws NetworkException
    {
        return getShortestRouteBetween(gtuType, nodeFrom, nodeTo, new ArrayList<>(), linkWeight);
    }

    /**
     * Calculate the shortest route between two nodes in the network, via a list of intermediate nodes. If no path exists from
     * the start node to the end node via the intermediate nodes in the network, null is returned. This method returns a
     * CompleteRoute, which includes all nodes to get from start to end. This method recalculates the graph.
     * @param gtuType the GtuType for which to calculate the shortest route
     * @param nodeFrom the start node.
     * @param nodeTo the end node.
     * @param nodesVia a number of nodes that the GTU has to pass between nodeFrom and nodeTo in the given order.
     * @return the shortest route between two nodes in the network, via the intermediate nodes. If no path exists from the start
     *         node to the end node via the intermediate nodes in the network, null is returned.
     * @throws NetworkException in case nodes cannot be added to the route, e.g. because they are not directly connected. This
     *             can be the case when the links in the network have changed, but the graph has not been rebuilt.
     */
    public final Route getShortestRouteBetween(final GtuType gtuType, final Node nodeFrom, final Node nodeTo,
            final List<Node> nodesVia) throws NetworkException
    {
        return getShortestRouteBetween(gtuType, nodeFrom, nodeTo, nodesVia, LinkWeight.LENGTH_NO_CONNECTORS);
    }

    /**
     * Calculate the shortest route between two nodes in the network, via a list of intermediate nodes. If no path exists from
     * the start node to the end node via the intermediate nodes in the network, null is returned. This method returns a
     * CompleteRoute, which includes all nodes to get from start to end. This method recalculates the graph.
     * @param gtuType the GtuType for which to calculate the shortest route
     * @param nodeFrom the start node.
     * @param nodeTo the end node.
     * @param nodesVia a number of nodes that the GTU has to pass between nodeFrom and nodeTo in the given order.
     * @param linkWeight link weight.
     * @return the shortest route between two nodes in the network, via the intermediate nodes. If no path exists from the start
     *         node to the end node via the intermediate nodes in the network, null is returned.
     * @throws NetworkException in case nodes cannot be added to the route, e.g. because they are not directly connected. This
     *             can be the case when the links in the network have changed, but the graph has not been rebuilt.
     */
    public final Route getShortestRouteBetween(final GtuType gtuType, final Node nodeFrom, final Node nodeTo,
            final List<Node> nodesVia, final LinkWeight linkWeight) throws NetworkException
    {
        Route route = new Route("Route for " + gtuType + " from " + nodeFrom + "to " + nodeTo + " via " + nodesVia.toString(),
                gtuType);
        SimpleDirectedWeightedGraph<Node, Link> graph = getGraph(gtuType, linkWeight);
        List<Node> nodes = new ArrayList<>();
        nodes.add(nodeFrom);
        nodes.addAll(nodesVia);
        nodes.add(nodeTo);
        Node from = nodeFrom;
        route.addNode(nodeFrom);
        for (int i = 1; i < nodes.size(); i++)
        {
            Node to = nodes.get(i);
            GraphPath<Node, Link> path =
                    linkWeight.getAStarHeuristic() == null ? DijkstraShortestPath.findPathBetween(graph, from, to)
                            : new AStarShortestPath<>(graph, linkWeight.getAStarHeuristic()).getPath(from, to);
            if (path == null)
            {
                CategoryLogger.always().debug("Cannot find a path from " + nodeFrom + " via " + nodesVia + " to " + nodeTo
                        + " (failing between " + from + " and " + to + ")");
                return null;
            }
            for (Link link : path.getEdgeList())
            {
                if (!link.getEndNode().equals(route.destinationNode())
                        && route.destinationNode().isConnectedTo(gtuType, link.getEndNode()))
                {
                    route.addNode(link.getEndNode());
                }
                else if (!link.getStartNode().equals(route.destinationNode())
                        && route.destinationNode().isConnectedTo(gtuType, link.getStartNode()))
                {
                    route.addNode(link.getStartNode());
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
     * @param gtuType GTU type
     * @param linkWeight link weight
     * @return SimpleDirectedWeightedGraph
     */
    private SimpleDirectedWeightedGraph<Node, Link> getGraph(final GtuType gtuType, final LinkWeight linkWeight)
    {
        if (linkWeight.isStatic())
        {
            return this.linkGraphs.get(() -> buildGraph(gtuType, linkWeight), gtuType, linkWeight);
        }
        return buildGraph(gtuType, linkWeight);
    }

    /**
     * Get route map.
     * @return a defensive copy of the routeMap.
     */
    public final ImmutableMap<GtuType, Map<String, Route>> getRouteMap()
    {
        return new ImmutableHashMap<>(this.routeMap, Immutable.WRAP);
    }

    /**
     * Get route map.
     * @return only to be used in the 'network' package for cloning.
     */
    final Map<GtuType, Map<String, Route>> getRawRouteMap()
    {
        return this.routeMap;
    }

    /**
     * Set route map.
     * @param newRouteMap the routeMap to set, only to be used in the 'network' package for cloning.
     */
    public final void setRawRouteMap(final Map<GtuType, Map<String, Route>> newRouteMap)
    {
        this.routeMap = newRouteMap;
    }

    /***************************************************************************************/
    /**************************************** GTUs *****************************************/
    /***************************************************************************************/

    @Override
    public final void addGTU(final Gtu gtu)
    {
        this.gtuMap.put(gtu.getId(), gtu);
        // TODO verify that gtu.getSimulator() equals getSimulator() ?
        fireTimedEvent(Network.GTU_ADD_EVENT, gtu.getId(), getSimulator().getSimulatorTime());
    }

    @Override
    public final void removeGTU(final Gtu gtu)
    {
        fireTimedEvent(Network.GTU_REMOVE_EVENT, gtu.getId(), getSimulator().getSimulatorTime());
        this.gtuMap.remove(gtu.getId());
    }

    @Override
    public final boolean containsGTU(final Gtu gtu)
    {
        return this.gtuMap.containsValue(gtu);
    }

    @Override
    public final Gtu getGTU(final String gtuId)
    {
        return this.gtuMap.get(gtuId);
    }

    @Override
    public final Set<Gtu> getGTUs()
    {
        // defensive copy
        return new LinkedHashSet<>(this.gtuMap.values());
    }

    @Override
    public final boolean containsGtuId(final String gtuId)
    {
        return this.gtuMap.containsKey(gtuId);
    }

    /**
     * Return GTU map.
     * @return gtuMap
     */
    final Map<String, Gtu> getRawGtuMap()
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
        for (Node node : this.nodeMap.values())
        {
            Bounds2d b = node.getAbsoluteBounds();
            minX = Math.min(minX, b.getMinX());
            minY = Math.min(minY, b.getMinY());
            maxX = Math.max(maxX, b.getMaxX());
            maxY = Math.max(maxY, b.getMaxY());
            content = true;
        }
        for (Link link : this.linkMap.values())
        {
            Bounds2d b = link.getAbsoluteBounds();
            minX = Math.min(minX, b.getMinX());
            minY = Math.min(minY, b.getMinY());
            maxX = Math.max(maxX, b.getMaxX());
            maxY = Math.max(maxY, b.getMaxY());
            content = true;
        }
        for (LocatedObject object : this.objectMap.values())
        {
            Bounds2d b = object.getAbsoluteBounds();
            minX = Math.min(minX, b.getMinX());
            minY = Math.min(minY, b.getMinY());
            maxX = Math.max(maxX, b.getMaxX());
            maxY = Math.max(maxY, b.getMaxY());
            content = true;
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

    @Override
    public final String toString()
    {
        return "Network [id=" + this.id + ", nodeMapSize=" + this.nodeMap.size() + ", linkMapSize=" + this.linkMap.size()
                + ", objectMapSize=" + this.objectMap.size() + ", routeMapSize=" + this.routeMap.size() + ", gtuMapSize="
                + this.gtuMap.size() + "]";
    }

    /***************************************************************************************/
    /*************************************** EVENTS ****************************************/
    /***************************************************************************************/

    /**
     * The timed event type for pub/sub indicating the removal of a GTU from the network. <br>
     * Payload: String gtuId (not an array, just a String)
     */
    public static final EventType GTU_REMOVE_EVENT = new EventType("Network.GTU.REMOVE",
            new MetaData("GTU removed", "GTU removed", new ObjectDescriptor("GTU id", "GTU id", String.class)));

    /**
     * The timed event type for pub/sub indicating the addition of a GTU to the network. <br>
     * Payload: String gtuId (not an array, just a String)
     */
    public static final EventType GTU_ADD_EVENT = new EventType("Network.GTU.ADD",
            new MetaData("GTU added", "GTU added", new ObjectDescriptor("GTU id", "GTU id", String.class)));

    /**
     * The timed event type for pub/sub indicating the removal of a Route for a gtuType. <br>
     * Payload: [String gtuTypeId, String routeId]
     */
    public static final EventType ROUTE_REMOVE_EVENT = new EventType("Network.ROUTE.REMOVE",
            new MetaData("Route removed", "Route removed",
                    new ObjectDescriptor[] {new ObjectDescriptor("GTU Type id", "GTU Type id", String.class),
                            new ObjectDescriptor("Route id", "Route id", String.class)}));

    /**
     * The timed event type for pub/sub indicating the addition of a Route for a gtuType. <br>
     * Payload: [String gtuTypeId, String routeId]
     */
    public static final EventType ROUTE_ADD_EVENT = new EventType("Network.ROUTE.ADD",
            new MetaData("Route added", "Route added",
                    new ObjectDescriptor[] {new ObjectDescriptor("GTU Type id", "GTU Type id", String.class),
                            new ObjectDescriptor("Route id", "Route id", String.class)}));

    /**
     * The timed event type for pub/sub indicating the removal of a NonLocatedObject implementing object. <br>
     * Payload: String objectId (not an array, just a String)
     */
    public static final EventType NONLOCATED_OBJECT_REMOVE_EVENT = new EventType("Network.NONLOCATED_OBJECT.REMOVE",
            new MetaData("Non-located object removed", "Non-located, stationary object removed",
                    new ObjectDescriptor("NonLocatedObject", "Id of non-located, stationary object", String.class)));

    /**
     * The timed event type for pub/sub indicating the addition of a NonLocatedObject implementing object. <br>
     * Payload: String ObjectId (not an array, just a String)
     */
    public static final EventType NONLOCATED_OBJECT_ADD_EVENT = new EventType("Network.NONLOCATED_OBJECT.ADD",
            new MetaData("Non-located object added", "Non-located, stationary object added",
                    new ObjectDescriptor("NonLocatedObject", "Id of non-located, stationary object", String.class)));

    /**
     * The timed event type for pub/sub indicating the removal of an ObjectInterface implementing object. <br>
     * Payload: String objectId (not an array, just a String)
     */
    public static final EventType OBJECT_REMOVE_EVENT =
            new EventType("Network.OBJECT.REMOVE", new MetaData("Object removed", "Visible, stationary object removed",
                    new ObjectDescriptor("id of Static object", "id of Visible, stationary object", String.class)));

    /**
     * The timed event type for pub/sub indicating the addition of an ObjectInterface implementing object. <br>
     * Payload: String ObjectId (not an array, just a String)
     */
    public static final EventType OBJECT_ADD_EVENT =
            new EventType("Network.OBJECT.ADD", new MetaData("Object added", "Visible, stationary object added",
                    new ObjectDescriptor("id of Static object", "id of Visible, stationary object", String.class)));

    /**
     * The timed event type for pub/sub indicating the removal of a Link. <br>
     * Payload: String linkId (not an array, just a String)
     */
    public static final EventType LINK_REMOVE_EVENT = new EventType("Network.LINK.REMOVE",
            new MetaData("Link removed", "Link removed", new ObjectDescriptor("Link", "Name of link", String.class)));

    /**
     * The timed event type for pub/sub indicating the addition of a Link. <br>
     * Payload: String linkId (not an array, just a String)
     */
    public static final EventType LINK_ADD_EVENT = new EventType("Network.LINK.ADD",
            new MetaData("Link added", "Link added", new ObjectDescriptor("Link", "Name of link", String.class)));

    /**
     * The timed event type for pub/sub indicating the removal of a Node. <br>
     * Payload: String nodeId (not an array, just a String)
     */
    public static final EventType NODE_REMOVE_EVENT = new EventType("Network.NODE.REMOVE",
            new MetaData("Node removed", "Node removed", new ObjectDescriptor("Node", "Name of node", String.class)));

    /**
     * The timed event type for pub/sub indicating the addition of a Node. <br>
     * Payload: String nodeId (not an array, just a String)
     */
    public static final EventType NODE_ADD_EVENT = new EventType("Network.NODE.ADD",
            new MetaData("Node added", "Node added", new ObjectDescriptor("Node", "Name of node", String.class)));

}

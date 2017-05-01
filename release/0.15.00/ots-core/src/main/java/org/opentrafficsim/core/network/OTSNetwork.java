package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.event.EventContext;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.opentrafficsim.core.animation.ClonableRenderable2DInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.object.InvisibleObjectInterface;
import org.opentrafficsim.core.object.ObjectInterface;
import org.opentrafficsim.core.perception.PerceivableContext;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.immutablecollections.Immutable;
import nl.tudelft.simulation.immutablecollections.ImmutableHashMap;
import nl.tudelft.simulation.immutablecollections.ImmutableMap;
import nl.tudelft.simulation.naming.context.ContextUtil;

/**
 * A Network consists of a set of links. Each link has, in its turn, a start node and an end node.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** Map of ObjectInterface. */
    private Map<String, ObjectInterface> objectMap = new HashMap<>();

    /** Map of InvisibleObjects. */
    private Map<String, InvisibleObjectInterface> invisibleObjectMap = new HashMap<>();

    /** Map of Routes. */
    private Map<GTUType, Map<String, Route>> routeMap = new HashMap<>();

    /** Graphs to calculate shortest paths per GTUType. */
    private Map<GTUType, SimpleDirectedWeightedGraph<Node, LinkEdge<Link>>> linkGraphs = new HashMap<>();

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
    public final ImmutableMap<String, Node> getNodeMap()
    {
        return new ImmutableHashMap<String, Node>(this.nodeMap, Immutable.WRAP);
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
        return new ImmutableHashMap<String, Link>(this.linkMap, Immutable.WRAP);
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
        return new ImmutableHashMap<String, ObjectInterface>(this.objectMap, Immutable.WRAP);
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableMap<String, ObjectInterface> getObjectMap(final Class<ObjectInterface> objectType)
    {
        Map<String, ObjectInterface> result = new HashMap<>();
        for (String key : this.objectMap.keySet())
        {
            ObjectInterface o = this.objectMap.get(key);
            if (objectType.isInstance(o))
            {
                result.put(key, o);
            }
        }
        return new ImmutableHashMap<String, ObjectInterface>(result, Immutable.WRAP);
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
        return new ImmutableHashMap<String, InvisibleObjectInterface>(this.invisibleObjectMap, Immutable.WRAP);
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableMap<String, InvisibleObjectInterface> getInvisibleObjectMap(
            final Class<InvisibleObjectInterface> objectType)
    {
        Map<String, InvisibleObjectInterface> result = new HashMap<>();
        for (String key : this.objectMap.keySet())
        {
            InvisibleObjectInterface o = this.invisibleObjectMap.get(key);
            if (objectType.isInstance(o))
            {
                result.put(key, o);
            }
        }
        return new ImmutableHashMap<String, InvisibleObjectInterface>(result, Immutable.WRAP);
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
        Map<String, Route> routes = new HashMap<>();
        if (this.routeMap.containsKey(gtuType))
        {
            routes.putAll(this.routeMap.get(gtuType));
        }
        return new ImmutableHashMap<String, Route>(routes, Immutable.WRAP);
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
            this.routeMap.put(gtuType, new HashMap<String, Route>());
        }
        this.routeMap.get(gtuType).put(route.getId(), route);
        fireEvent(Network.ROUTE_ADD_EVENT, new Object[] { gtuType.getId(), route.getId() });
    }

    /** {@inheritDoc} */
    @Override
    public final void removeRoute(final GTUType gtuType, final Route route) throws NetworkException
    {
        if (!containsRoute(gtuType, route))
        {
            throw new NetworkException("Route " + route + " for GTUType " + gtuType + " not registered in network " + this.id);
        }
        fireEvent(Network.ROUTE_REMOVE_EVENT, new Object[] { gtuType.getId(), route.getId() });
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
        // TODO take connections into account, and possibly do node expansion to build the graph
        @SuppressWarnings("rawtypes")
        Class linkEdgeClass = LinkEdge.class;
        @SuppressWarnings("unchecked")
        SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> graph =
                new SimpleDirectedWeightedGraph<Node, LinkEdge<Link>>(linkEdgeClass);
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
                graph.setEdgeWeight(linkEdge, link.getLength().doubleValue());
            }
            if (directionality.isBackwardOrBoth())
            {
                LinkEdge<Link> linkEdge = new LinkEdge<>(link);
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
        SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> graph = this.linkGraphs.get(gtuType);
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
        CompleteRoute route = new CompleteRoute(
                "Route for " + gtuType + " from " + nodeFrom + "to " + nodeTo + " via " + nodesVia.toString(), gtuType);
        SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> graph = this.linkGraphs.get(gtuType);
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
        fireTimedEvent(Network.GTU_ADD_EVENT, gtu.getId(), gtu.getSimulator().getSimulatorTime());
    }

    /** {@inheritDoc} */
    @Override
    public final void removeGTU(final GTU gtu)
    {
        fireTimedEvent(Network.GTU_REMOVE_EVENT, gtu.getId(), gtu.getSimulator().getSimulatorTime());
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
                + ", objectMapSize=" + this.objectMap.size() + ", routeMapSize=" + this.routeMap.size() + ", gtuMapSize="
                + this.gtuMap.size() + "]";
    }

    /***************************************************************************************/
    /*************************************** CLONE *****************************************/
    /***************************************************************************************/

    /**
     * Clone the OTSNetwork.
     * @param newId the new id of the network
     * @param oldSimulator the old simulator for this network
     * @param newSimulator the new simulator for this network
     * @param animation whether to (re)create animation or not
     * @return a clone of this network
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public OTSNetwork clone(final String newId, final OTSSimulatorInterface oldSimulator,
            final OTSSimulatorInterface newSimulator, final boolean animation) throws NetworkException
    {
        OTSNetwork newNetwork = new OTSNetwork(newId);

        // clone the nodes
        for (Node node : this.nodeMap.values())
        {
            ((OTSNode) node).clone1(newNetwork, newSimulator);
        }

        // clone the links
        for (Link oldLink : this.linkMap.values())
        {
            OTSLink newLink = ((OTSLink) oldLink).clone(newNetwork, newSimulator, animation);
            if (animation)
            {
                cloneAnimation(oldLink, newLink, oldSimulator, newSimulator);
            }
        }

        // make the link-connections for the cloned nodes
        for (Node oldNode : this.nodeMap.values())
        {
            OTSNode newNode = ((OTSNode) oldNode).clone2(newNetwork, newSimulator, animation);
            if (animation)
            {
                cloneAnimation(oldNode, newNode, oldSimulator, newSimulator);
            }
        }

        // clone the graphs that had been created for the old network
        for (GTUType gtuType : this.linkGraphs.keySet())
        {
            newNetwork.buildGraph(gtuType);
        }

        // clone the routes
        Map<GTUType, Map<String, Route>> newRouteMap = new HashMap<>();
        for (GTUType gtuType : this.routeMap.keySet())
        {
            Map<String, Route> newRoutes = new HashMap<>();
            for (Route route : this.routeMap.get(gtuType).values())
            {
                newRoutes.put(route.getId(), route.clone(newNetwork, newSimulator, animation));
            }
            newRouteMap.put(gtuType, newRoutes);
        }
        newNetwork.routeMap = newRouteMap;
        // clone the traffic lights
        for (InvisibleObjectInterface io : getInvisibleObjectMap().values())
        {
            InvisibleObjectInterface clonedIO = io.clone(newSimulator, newNetwork);
            newNetwork.addInvisibleObject(clonedIO);
        }
        return newNetwork;
    }

    /**
     * Clone all animation objects for the given class. The given class is the <b>source</b> of the animation objects, as it is
     * not known on beforehand which objects need to be cloned. It is important for cloning that the animation objects implement
     * the CloneableRenderable2DInterface, so they can be cloned with their properties. If not, they will not be taken into
     * account for cloning by this method.
     * @param oldSource the old source object that might have one or more animation objects attached to it
     * @param newSource the new source object to attach the cloned animation objects to
     * @param oldSimulator the old simulator when the old objects can be found
     * @param newSimulator the new simulator where the new simulation objects need to be registered
     */
    @SuppressWarnings("checkstyle:designforextension")
    public static void cloneAnimation(final Locatable oldSource, final Locatable newSource,
            final OTSSimulatorInterface oldSimulator, final OTSSimulatorInterface newSimulator)
    {
        if (!(oldSimulator instanceof AnimatorInterface) || !(newSimulator instanceof AnimatorInterface))
        {
            return;
        }

        try
        {
            EventContext context =
                    (EventContext) ContextUtil.lookup(oldSimulator.getReplication().getContext(), "/animation/2D");
            NamingEnumeration<Binding> list = context.listBindings("");
            while (list.hasMore())
            {
                Binding binding = list.next();
                Renderable2DInterface animationObject = (Renderable2DInterface) binding.getObject();
                Locatable locatable = animationObject.getSource();
                if (oldSource.equals(locatable) && animationObject instanceof ClonableRenderable2DInterface)
                {
                    ((ClonableRenderable2DInterface) animationObject).clone(newSource, newSimulator);
                }
            }
        }
        catch (NamingException | RemoteException exception)
        {
            System.err.println("Error when cloning animation objects for object " + oldSource);
        }
    }

    /**
     * Remove all objects and animation in the network.
     * @param simulator the simulator of the old network
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void destroy(final OTSSimulatorInterface simulator)
    {
        for (GTU gtu : this.getGTUs())
        {
            gtu.destroy();
        }

        Set<Renderable2DInterface> animationObjects = new HashSet<>();
        try
        {
            EventContext context = (EventContext) ContextUtil.lookup(simulator.getReplication().getContext(), "/animation/2D");
            NamingEnumeration<Binding> list = context.listBindings("");
            while (list.hasMore())
            {
                Binding binding = list.next();
                Renderable2DInterface animationObject = (Renderable2DInterface) binding.getObject();
                animationObjects.add(animationObject);
            }

            for (Renderable2DInterface ao : animationObjects)
            {
                try
                {
                    ao.destroy();
                }
                catch (Exception e)
                {
                    //
                }
            }
        }
        catch (NamingException | RemoteException exception)
        {
            System.err.println("Error when destroying animation objects");
        }

        this.nodeMap.clear();
        this.linkMap.clear();
        this.linkGraphs.clear();
        this.routeMap.clear();
    }

    /**
     * Remove all animation objects of the given class.
     * @param clazz the class to remove the animation objects for
     * @param oldSimulator the old simulator
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void removeAnimation(final Class<?> clazz, final OTSSimulatorInterface oldSimulator)
    {
        if (!(oldSimulator instanceof AnimatorInterface))
        {
            return;
        }

        try
        {
            EventContext context =
                    (EventContext) ContextUtil.lookup(oldSimulator.getReplication().getContext(), "/animation/2D");
            NamingEnumeration<Binding> list = context.listBindings("");
            while (list.hasMore())
            {
                Binding binding = list.next();
                Renderable2DInterface animationObject = (Renderable2DInterface) binding.getObject();
                Locatable locatable = animationObject.getSource();
                if (clazz.isAssignableFrom(locatable.getClass()))
                {
                    animationObject.destroy();
                }
            }
        }
        catch (NamingException | RemoteException exception)
        {
            System.err.println("Error when destroying animation objects for class " + clazz.getSimpleName());
        }
    }

}

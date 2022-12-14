package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.djutils.event.EventProducerInterface;
import org.djutils.event.TimedEventType;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.object.InvisibleObjectInterface;
import org.opentrafficsim.core.object.ObjectInterface;

/**
 * Interface that defines what information a network should be able to provide about Nodes, Links and Routes.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Jul 22, 2015 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public interface Network extends Definitions, EventProducerInterface, Identifiable
{
    /** @return String; the id */
    @Override
    String getId();

    /**
     * Return the simulator.
     * @return OTSSimulatorInterface; the simulator
     */
    OtsSimulatorInterface getSimulator();

    /***************************************************************************************/
    /**************************************** NODES ****************************************/
    /***************************************************************************************/

    /**
     * Provide an immutable map of node ids to nodes in the network.
     * @return an immutable map of nodes.
     */
    ImmutableMap<String, Node> getNodeMap();

    /**
     * Register a node in the network.
     * @param node Node; the node to add to the network.
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    void addNode(Node node) throws NetworkException;

    /**
     * Unregister a node from the network.
     * @param node Node; the node to remove from the network.
     * @throws NetworkException if node does not exist in the network.
     */
    void removeNode(Node node) throws NetworkException;

    /**
     * Test whether a node is present in the network.
     * @param node Node; the node to search for in the network.
     * @return whether the node is in this network
     */
    boolean containsNode(Node node);

    /**
     * Test whether a node with a given id is present in the network.
     * @param nodeId String; the id of the node to search for in the network.
     * @return whether the node is in this network
     */
    boolean containsNode(String nodeId);

    /**
     * Retrieve a node with a given id from the network, or null if the id cannot be found.
     * @param nodeId String; the id of the node to search for in the network.
     * @return the node or null if not present
     */
    Node getNode(String nodeId);

    /***************************************************************************************/
    /**************************************** LINKS ****************************************/
    /***************************************************************************************/

    /**
     * Provide an immutable map of link ids to links in the network.
     * @return the an immutable map of links.
     */
    ImmutableMap<String, Link> getLinkMap();

    /**
     * Register a link in the network.
     * @param link Link; the link to add to the network.
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    void addLink(Link link) throws NetworkException;

    /**
     * Unregister a link from the network.
     * @param link Link; the link to remove from the network.
     * @throws NetworkException if link does not exist in the network.
     */
    void removeLink(Link link) throws NetworkException;

    /**
     * Test whether a link is present in the network.
     * @param link Link; the link to search for in the network.
     * @return whether the link is in this network
     */
    boolean containsLink(Link link);

    /**
     * Test whether a link with a given id is present in the network.
     * @param link String; the id of the link to search for in the network.
     * @return whether the link is in this network
     */
    boolean containsLink(String link);

    /**
     * Retrieve a node with a given id from the network, or null if the id cannot be found.
     * @param linkId String; the id of the link to search for in the network.
     * @return the link or null if not present
     */
    Link getLink(String linkId);

    /**
     * Find a link between node1 and node2 and return it if it exists in the network. If not, return null.
     * @param node1 Node; first node
     * @param node2 Node; second node
     * @return the link between node1 and node2 in the network or null if it does not exist.
     */
    Link getLink(Node node1, Node node2);

    /**
     * Find a link between node1 and node2 and return it if it exists in the network. If not, return null.
     * @param nodeId1 String; id of the first node
     * @param nodeId2 String; id of the second node
     * @return the link between node1 and node2 in the network or null if it does not exist.
     * @throws NetworkException if the node(s) cannot be found by their id
     */
    Link getLink(String nodeId1, String nodeId2) throws NetworkException;

    /***************************************************************************************/
    /************************ OBJECT INTERFACE IMPLEMENTING OBJECTS ************************/
    /***************************************************************************************/

    /**
     * Return an immutable map of all ObjectInterface implementing objects in the Network.
     * @return ImmutableMap&lt;String, ObjectInterface&gt;; the immutable map of all ObjectInterface implementing objects in the
     *         Network
     */
    ImmutableMap<String, ObjectInterface> getObjectMap();

    /**
     * Return an immutable map of all ObjectInterface implementing objects in the network that are of type objectType, or any
     * sub type thereof.
     * @param objectType Class&lt;T&gt;; the (sub-)type of ObjectInterface that the returned map is reduced to
     * @param <T> type of object
     * @return ImmutableMap&lt;String, ObjectInterface&gt;; the immutable map of all ObjectInterface implementing objects in the
     *         Network that are of the type objectType, or any sub type thereof
     */
    <T extends ObjectInterface> ImmutableMap<String, T> getObjectMap(Class<T> objectType);

    /**
     * Return object of given type with given id.
     * @param objectType T; object type class
     * @param objectId String; id of object
     * @param <T> object type
     * @return T; object of given type with given id, {@code null} if no such object
     */
    <T extends ObjectInterface> T getObject(Class<T> objectType, String objectId);

    /**
     * Add an ObjectInterface implementing object to the Network.
     * @param object ObjectInterface; the object that implements ObjectInterface
     * @throws NetworkException if link already exists in the network, if name of the object is not unique.
     */
    void addObject(ObjectInterface object) throws NetworkException;

    /**
     * Remove an ObjectInterface implementing object form the Network.
     * @param object ObjectInterface; the object that implements ObjectInterface
     * @throws NetworkException if the object does not exist in the network.
     */
    void removeObject(ObjectInterface object) throws NetworkException;

    /**
     * Test whether the object is present in the Network.
     * @param object ObjectInterface; the object that is tested for presence
     * @return boolean; whether the object is present in the Network
     */
    boolean containsObject(ObjectInterface object);

    /**
     * Test whether an object with the given id is present in the Network.
     * @param objectId String; the id that is tested for presence
     * @return boolean; whether an object with the given id is present in the Network
     */
    boolean containsObject(String objectId);

    /***************************************************************************************/
    /********************************* INVISIBLE OBJECTS ***********************************/
    /***************************************************************************************/

    /**
     * Return an immutable map of all InvisibleObject implementing objects in the Network.
     * @return ImmutableMap&lt;String, ObjectInterface&gt;; the immutable map of all ObjectInterface implementing objects in the
     *         Network
     */
    ImmutableMap<String, InvisibleObjectInterface> getInvisibleObjectMap();

    /**
     * Return an immutable map of all InvisibleObject implementing objects in the network that are of type objectType, or any
     * sub type thereof.
     * @param objectType Class&lt;InvisibleObjectInterface&gt;; the (sub-)type of InvisibleObject that the returned map is
     *            reduced to
     * @return ImmutableMap&lt;String, InvisibleObject&gt;; the immutable map of all InvisibleObject implementing objects in the
     *         Network that are of the type objectType, or any sub type thereof
     */
    ImmutableMap<String, InvisibleObjectInterface> getInvisibleObjectMap(Class<InvisibleObjectInterface> objectType);

    /**
     * Add an ObjectInterface implementing object to the Network.
     * @param object InvisibleObjectInterface; the object that implements ObjectInterface
     * @throws NetworkException if link already exists in the network, if name of the object is not unique.
     */
    void addInvisibleObject(InvisibleObjectInterface object) throws NetworkException;

    /**
     * Remove an ObjectInterface implementing object form the Network.
     * @param object InvisibleObjectInterface; the object that implements ObjectInterface
     * @throws NetworkException if the object does not exist in the network.
     */
    void removeInvisibleObject(InvisibleObjectInterface object) throws NetworkException;

    /**
     * Test whether the invisible object is present in the Network.
     * @param object InvisibleObjectInterface; the object that is tested for presence
     * @return boolean; whether the invisible object is present in the Network
     */
    boolean containsInvisibleObject(InvisibleObjectInterface object);

    /**
     * Test whether an invisible object with the given id is present in the Network.
     * @param objectId String; the id that is tested for presence
     * @return boolean; whether an invisible object with the given id is present in the Network
     */
    boolean containsInvisibleObject(String objectId);

    /***************************************************************************************/
    /*************************************** ROUTES ****************************************/
    /***************************************************************************************/

    /**
     * Return an immutable map of routes that exist in the network for the GtuType.
     * @param gtuType GtuType; the GtuType for which to retrieve the defined routes
     * @return an immutable map of routes in the network for the given GtuType, or an empty Map if no routes are defined for the
     *         given GtuType.
     */
    ImmutableMap<String, Route> getDefinedRouteMap(GtuType gtuType);

    /**
     * Add a route to the network.
     * @param gtuType GtuType; the GtuType for which to add a route
     * @param route Route; the route to add to the network.
     * @throws NetworkException if route already exists in the network, if name of the route is not unique, if one of the nodes
     *             of the route are not registered in the network.
     */
    void addRoute(GtuType gtuType, Route route) throws NetworkException;

    /**
     * Remove the route from the network, e.g. because of road maintenance.
     * @param gtuType GtuType; the GtuType for which to remove a route
     * @param route Route; the route to remove from the network.
     * @throws NetworkException if route does not exist in the network.
     */
    void removeRoute(GtuType gtuType, Route route) throws NetworkException;

    /**
     * Return the route with the given id in the network for the given GtuType, or null if it the route with the id does not
     * exist.
     * @param gtuType GtuType; the GtuType for which to retrieve a route based on its id.
     * @param routeId String; the route to search for in the network.
     * @return the route or null if not present
     */
    Route getRoute(GtuType gtuType, String routeId);

    /**
     * Determine whether the provided route exists in the network for the given GtuType.
     * @param gtuType GtuType; the GtuType for which to check whether the route exists
     * @param route Route; the route to check for
     * @return whether the route exists in the network for the given GtuType
     */
    boolean containsRoute(GtuType gtuType, Route route);

    /**
     * Determine whether a route with the given id exists in the network for the given GtuType.
     * @param gtuType GtuType; the GtuType for which to check whether the route exists
     * @param routeId String; the id of the route to check for
     * @return whether a route with the given id exists in the network for the given GtuType
     */
    boolean containsRoute(GtuType gtuType, String routeId);

    /**
     * Return the the shortest route between two nodes in the network, via a list of intermediate nodes. If no path exists from
     * the start node to the end node via the intermediate nodes in the network, null is returned.
     * @param gtuType GtuType; the GtuType for which to retrieve the defined routes
     * @param nodeFrom Node; the start node.
     * @param nodeTo Node; the end node.
     * @return a set with routes from the start Node to the end Node in the network; if no route can be found, an empty set is
     *         returned.
     */
    Set<Route> getRoutesBetween(GtuType gtuType, Node nodeFrom, Node nodeTo);

    /**
     * (Re)build the underlying graph for the given GtuType.
     * @param gtuType GtuType; the GtuType for which to (re)build the graph
     */
    void buildGraph(GtuType gtuType);

    /**
     * Calculate the shortest route between two nodes in the network. If no path exists from the start node to the end node in
     * the network, null is returned. This method returns a CompleteRoute, which includes all nodes to get from start to end. In
     * case the graph for the GtuType has not yet been built, this method will call the buildGraph method.
     * @param gtuType GtuType; the GtuType for which to calculate the shortest route
     * @param nodeFrom Node; the start node.
     * @param nodeTo Node; the end node.
     * @return the shortest route from the start Node to the end Node in the network. If no path exists from the start node to
     *         the end node in the network, null is returned.
     * @throws NetworkException in case nodes cannot be added to the route, e.g. because they are not directly connected. This
     *             can be the case when the links in the network have changed, but the graph has not been rebuilt.
     */
    default Route getShortestRouteBetween(GtuType gtuType, Node nodeFrom, Node nodeTo) throws NetworkException
    {
        return getShortestRouteBetween(gtuType, nodeFrom, nodeTo, LinkWeight.LENGTH);
    }

    /**
     * Calculate the shortest route between two nodes in the network. If no path exists from the start node to the end node in
     * the network, null is returned. This method returns a CompleteRoute, which includes all nodes to get from start to end.
     * This method recalculates the graph.
     * @param gtuType GtuType; the GtuType for which to calculate the shortest route
     * @param nodeFrom Node; the start node.
     * @param nodeTo Node; the end node.
     * @param linkWeight LinkWeight; link weight.
     * @return the shortest route from the start Node to the end Node in the network. If no path exists from the start node to
     *         the end node in the network, null is returned.
     * @throws NetworkException in case nodes cannot be added to the route, e.g. because they are not directly connected. This
     *             can be the case when the links in the network have changed, but the graph has not been rebuilt.
     */
    Route getShortestRouteBetween(GtuType gtuType, Node nodeFrom, Node nodeTo, LinkWeight linkWeight)
            throws NetworkException;

    /**
     * Calculate the shortest route between two nodes in the network, via a list of intermediate nodes. If no path exists from
     * the start node to the end node via the intermediate nodes in the network, null is returned. This method returns a
     * CompleteRoute, which includes all nodes to get from start to end. In case the graph for the GtuType has not yet been
     * built, this method will call the buildGraph method.
     * @param gtuType GtuType; the GtuType for which to calculate the shortest route
     * @param nodeFrom Node; the start node.
     * @param nodeTo Node; the end node.
     * @param nodesVia List&lt;Node&gt;; a number of nodes that the GTU has to pass between nodeFrom and nodeTo in the given
     *            order.
     * @return the shortest route between two nodes in the network, via the intermediate nodes. If no path exists from the start
     *         node to the end node via the intermediate nodes in the network, null is returned.
     * @throws NetworkException in case nodes cannot be added to the route, e.g. because they are not directly connected. This
     *             can be the case when the links in the network have changed, but the graph has not been rebuilt.
     */
    default Route getShortestRouteBetween(GtuType gtuType, Node nodeFrom, Node nodeTo, List<Node> nodesVia)
            throws NetworkException
    {
        return getShortestRouteBetween(gtuType, nodeFrom, nodeTo, nodesVia, LinkWeight.LENGTH);
    }

    /**
     * Calculate the shortest route between two nodes in the network, via a list of intermediate nodes. If no path exists from
     * the start node to the end node via the intermediate nodes in the network, null is returned. This method returns a
     * CompleteRoute, which includes all nodes to get from start to end. This method recalculates the graph.
     * @param gtuType GtuType; the GtuType for which to calculate the shortest route
     * @param nodeFrom Node; the start node.
     * @param nodeTo Node; the end node.
     * @param nodesVia List&lt;Node&gt;; a number of nodes that the GTU has to pass between nodeFrom and nodeTo in the given
     *            order.
     * @param linkWeight LinkWeight; link weight.
     * @return the shortest route between two nodes in the network, via the intermediate nodes. If no path exists from the start
     *         node to the end node via the intermediate nodes in the network, null is returned.
     * @throws NetworkException in case nodes cannot be added to the route, e.g. because they are not directly connected. This
     *             can be the case when the links in the network have changed, but the graph has not been rebuilt.
     */
    Route getShortestRouteBetween(GtuType gtuType, Node nodeFrom, Node nodeTo, List<Node> nodesVia,
            LinkWeight linkWeight) throws NetworkException;

    /***************************************************************************************/
    /********************************** ANIMATION EVENTS ***********************************/
    /***************************************************************************************/

    /**
     * The timed event type for pub/sub indicating the addition of a Node. <br>
     * Payload: Node node (not an array, just an Object)
     */
    TimedEventType ANIMATION_NODE_ADD_EVENT = new TimedEventType("ANIMATION.NETWORK.NODE.ADD",
            new MetaData("Node animation added", "Node animation", new ObjectDescriptor("Node", "Node", Node.class)));

    /**
     * The timed event type for pub/sub indicating the removal of a Node. <br>
     * Payload: Node node (not an array, just an Object)
     */
    TimedEventType ANIMATION_NODE_REMOVE_EVENT = new TimedEventType("ANIMATION.NETWORK.NODE.REMOVE",
            new MetaData("Node animation removed", "Node animation", new ObjectDescriptor("Node", "Node", Node.class)));

    /**
     * The timed event type for pub/sub indicating the addition of a Link. <br>
     * Payload: Link link (not an array, just an Object)
     */
    TimedEventType ANIMATION_LINK_ADD_EVENT = new TimedEventType("ANIMATION.NETWORK.LINK.ADD",
            new MetaData("Link animation added", "Link animation", new ObjectDescriptor("Link", "Link", Link.class)));

    /**
     * The timed event type for pub/sub indicating the removal of a Link. <br>
     * Payload: Link link (not an array, just an Object)
     */
    TimedEventType ANIMATION_LINK_REMOVE_EVENT = new TimedEventType("ANIMATION.NETWORK.LINK.REMOVE",
            new MetaData("Link animation removed", "Link animation", new ObjectDescriptor("Link", "Link", Link.class)));

    /**
     * The timed event type for pub/sub indicating the addition of an ObjectInterface implementing object. <br>
     * Payload: StaticObject object (not an array, just an Object)
     */
    TimedEventType ANIMATION_OBJECT_ADD_EVENT =
            new TimedEventType("ANIMATION.NETWORK.OBJECT.ADD", new MetaData("Object animation added", "Object animation",
                    new ObjectDescriptor("StaticObject", "Visible, stationary object", ObjectInterface.class)));

    /**
     * The timed event type for pub/sub indicating the removal of an ObjectInterface implementing object. <br>
     * Payload: StaticObject object (not an array, just an Object)
     */
    TimedEventType ANIMATION_OBJECT_REMOVE_EVENT =
            new TimedEventType("ANIMATION.NETWORK.OBJECT.REMOVE", new MetaData("Object animation removed", "Object animation",
                    new ObjectDescriptor("ObjectInterface", "Visible, stationary object", ObjectInterface.class)));

    /**
     * The timed event type for pub/sub indicating the addition of an InvisibleObjectInterface implementing object. <br>
     * TODO: explain why invisible objects have associated animation events (or remove them from the Network interface) <br>
     * Payload: InvisibleObject object (not an array, just an Object)
     */
    TimedEventType ANIMATION_INVISIBLE_OBJECT_ADD_EVENT = new TimedEventType("ANIMATION.NETWORK.INVISIBLE_OBJECT.ADD",
            new MetaData("Invisible object animation added", "Invisible object animation",
                    new ObjectDescriptor("InvisibleObjectInterface",
                            "Object that lives in a Network, but cannot be drawn and which does not have a specific location",
                            InvisibleObjectInterface.class)));

    /**
     * The timed event type for pub/sub indicating the removal of an InvisibleObjectInterface implementing object. <br>
     * Payload: InvisibleObject object (not an array, just an Object)
     */
    TimedEventType ANIMATION_INVISIBLE_OBJECT_REMOVE_EVENT = new TimedEventType("ANIMATION.NETWORK.INVISIBLE_OBJECT.REMOVE",
            new MetaData("Invisible object animation removed", "Invisible object animation",
                    new ObjectDescriptor("InvisibleObjectInterface",
                            "Object that lives in a Network, but cannot be drawn and which does not have a specific location",
                            InvisibleObjectInterface.class)));

    /**
     * The timed event type for pub/sub indicating the addition of a Route for a gtuType. <br>
     * Payload: [GtuType gtuType, Route route]
     */
    TimedEventType ANIMATION_ROUTE_ADD_EVENT = new TimedEventType("ANIMATION.NETWORK.ROUTE.ADD",
            new MetaData("Route animation added", "Route animation",
                    new ObjectDescriptor[] {new ObjectDescriptor("GTU type", "GTU type", GtuType.class),
                            new ObjectDescriptor("Route animation", "Route animation", Route.class)}));

    /**
     * The timed event type for pub/sub indicating the removal of a Route for a gtuType. <br>
     * Payload: [GtuType gtuType, Route route]
     */
    TimedEventType ANIMATION_ROUTE_REMOVE_EVENT = new TimedEventType("ANIMATION.NETWORK.ROUTE.REMOVE",
            new MetaData("Route animation removed", "Route animation",
                    new ObjectDescriptor[] {new ObjectDescriptor("GTU type", "GTU type", GtuType.class),
                            new ObjectDescriptor("Route animation", "Route animation", Route.class)}));

    /**
     * The timed event type for pub/sub indicating the addition of a GTU to the network. <br>
     * Payload: GTU gtu (not an array, just an Object)
     */
    TimedEventType ANIMATION_GTU_ADD_EVENT = new TimedEventType("ANIMATION.NETWORK.GTU.ADD",
            new MetaData("GTU animation added", "GTU animation", new ObjectDescriptor("GTU", "GTU", Gtu.class)));

    /**
     * The timed event type for pub/sub indicating the removal of a GTU from the network. <br>
     * Payload: GTU gtu (not an array, just an Object)
     */
    TimedEventType ANIMATION_GTU_REMOVE_EVENT = new TimedEventType("ANIMATION.NETWORK.GTU.REMOVE",
            new MetaData("GTU animation removed", "GTU animation", new ObjectDescriptor("GTU", "GTU", Gtu.class)));

    /**
     * The timed event type for pub/sub indicating the addition of an GtuGenerator implementing object. <br>
     * Payload: AbstractGtuGenerator object (not an array, just an Object) <br>
     * TODO: there seems to be no specific class or interface for these that can be specified in the ObjectDescriptor
     */
    TimedEventType ANIMATION_GENERATOR_ADD_EVENT = new TimedEventType("ANIMATION.NETWORK.GENERATOR.ADD",
            new MetaData("GTU generator animation added", "GTU generator animation",
                    new ObjectDescriptor("GTU generator animation", "Abstract GTU generator", Serializable.class)));

    /**
     * The timed event type for pub/sub indicating the removal of an GtuGenerator implementing object. <br>
     * Payload: AbstractGtuGenerator object (not an array, just an Object) <br>
     * TODO: there seems to be no specific class or interface for these that can be specified in the ObjectDescriptor
     */
    TimedEventType ANIMATION_GENERATOR_REMOVE_EVENT = new TimedEventType("ANIMATION.NETWORK.GENERATOR.REMOVE",
            new MetaData("GTU generator animation removed", "GTU generator animation",
                    new ObjectDescriptor("GTU generator animation", "Abstract GTU generator", Serializable.class)));

    /***************************************************************************************/
    /*************************************** EVENTS ****************************************/
    /***************************************************************************************/

    /**
     * The timed event type for pub/sub indicating the addition of a Node. <br>
     * Payload: String nodeId (not an array, just a String)
     */
    TimedEventType NODE_ADD_EVENT = new TimedEventType("NETWORK.NODE.ADD",
            new MetaData("Node added", "Node added", new ObjectDescriptor("Node", "Name of node", String.class)));

    /**
     * The timed event type for pub/sub indicating the removal of a Node. <br>
     * Payload: String nodeId (not an array, just a String)
     */
    TimedEventType NODE_REMOVE_EVENT = new TimedEventType("NETWORK.NODE.REMOVE",
            new MetaData("Node removed", "Node removed", new ObjectDescriptor("Node", "Name of node", String.class)));

    /**
     * The timed event type for pub/sub indicating the addition of a Link. <br>
     * Payload: String linkId (not an array, just a String)
     */
    TimedEventType LINK_ADD_EVENT = new TimedEventType("NETWORK.LINK.ADD",
            new MetaData("Link added", "Link added", new ObjectDescriptor("Link", "Name of link", String.class)));

    /**
     * The timed event type for pub/sub indicating the removal of a Link. <br>
     * Payload: String linkId (not an array, just a String)
     */
    TimedEventType LINK_REMOVE_EVENT = new TimedEventType("NETWORK.LINK.REMOVE",
            new MetaData("Link removed", "Link removed", new ObjectDescriptor("Link", "Name of link", String.class)));

    /**
     * The timed event type for pub/sub indicating the addition of an ObjectInterface implementing object. <br>
     * Payload: String ObjectId (not an array, just a String)
     */
    TimedEventType OBJECT_ADD_EVENT =
            new TimedEventType("NETWORK.OBJECT.ADD", new MetaData("Object added", "Visible, stationary object added",
                    new ObjectDescriptor("id of Static object", "id of Visible, stationary object", String.class)));

    /**
     * The timed event type for pub/sub indicating the removal of an ObjectInterface implementing object. <br>
     * Payload: String objectId (not an array, just a String)
     */
    TimedEventType OBJECT_REMOVE_EVENT =
            new TimedEventType("NETWORK.OBJECT.REMOVE", new MetaData("Object removed", "Visible, stationary object removed",
                    new ObjectDescriptor("id of Static object", "id of Visible, stationary object", String.class)));

    /**
     * The timed event type for pub/sub indicating the addition of an InvisibleObjectInterface implementing object. <br>
     * Payload: String ObjectId (not an array, just a String)
     */
    TimedEventType INVISIBLE_OBJECT_ADD_EVENT = new TimedEventType("NETWORK.INVISIBLE_OBJECT.ADD",
            new MetaData("Invisible object added", "Invisible, stationary object added",
                    new ObjectDescriptor("InvisibleObjectInterface", "Id of invisible, stationary object", String.class)));

    /**
     * The timed event type for pub/sub indicating the removal of an InvisibleObjectInterface implementing object. <br>
     * Payload: String objectId (not an array, just a String)
     */
    TimedEventType INVISIBLE_OBJECT_REMOVE_EVENT = new TimedEventType("NETWORK.INVISIBLE_OBJECT.REMOVE",
            new MetaData("Invisible object removed", "Invisible, stationary object removed",
                    new ObjectDescriptor("InvisibleObjectInterface", "Id of invisible, stationary object", String.class)));

    /**
     * The timed event type for pub/sub indicating the addition of a Route for a gtuType. <br>
     * Payload: [String gtuTypeId, String routeId]
     */
    TimedEventType ROUTE_ADD_EVENT = new TimedEventType("NETWORK.ROUTE.ADD",
            new MetaData("Route added", "Route added",
                    new ObjectDescriptor[] {new ObjectDescriptor("GTU Type id", "GTU Type id", String.class),
                            new ObjectDescriptor("Route id", "Route id", String.class)}));

    /**
     * The timed event type for pub/sub indicating the removal of a Route for a gtuType. <br>
     * Payload: [String gtuTypeId, String routeId]
     */
    TimedEventType ROUTE_REMOVE_EVENT = new TimedEventType("NETWORK.ROUTE.REMOVE",
            new MetaData("Route removed", "Route removed",
                    new ObjectDescriptor[] {new ObjectDescriptor("GTU Type id", "GTU Type id", String.class),
                            new ObjectDescriptor("Route id", "Route id", String.class)}));

    /**
     * The timed event type for pub/sub indicating the addition of a GTU to the network. <br>
     * Payload: String gtuId (not an array, just a String)
     */
    TimedEventType GTU_ADD_EVENT = new TimedEventType("NETWORK.GTU.ADD",
            new MetaData("GTU added", "GTU added", new ObjectDescriptor("GTU id", "GTU id", String.class)));

    /**
     * The timed event type for pub/sub indicating the removal of a GTU from the network. <br>
     * Payload: String gtuId (not an array, just a String)
     */
    TimedEventType GTU_REMOVE_EVENT = new TimedEventType("NETWORK.GTU.REMOVE",
            new MetaData("GTU removed", "GTU removed", new ObjectDescriptor("GTU id", "GTU id", String.class)));

    /**
     * The timed event type for pub/sub indicating the addition of a GtuGenerator to the network. <br>
     * Payload: String generatorName (not an array, just a String)
     */
    TimedEventType GENERATOR_ADD_EVENT = new TimedEventType("NETWORK.GENERATOR.ADD", new MetaData("GTU generator added",
            "GTU generator added", new ObjectDescriptor("GTU generator name", "GTU generator name", String.class)));

    /**
     * The timed event type for pub/sub indicating the removal of a GtuGenerator from the network. <br>
     * Payload: String generatorName (not an array, just a String)
     */
    TimedEventType GENERATOR_REMOVE_EVENT = new TimedEventType("NETWORK.GENERATOR.REMOVE", new MetaData("GTU generator removed",
            "GTU generator removed", new ObjectDescriptor("GTU generator name", "GTU generator name", String.class)));

}

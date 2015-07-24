package org.opentrafficsim.core.network;

import java.util.Map;
import java.util.Set;

import org.opentrafficsim.core.network.route.Route;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <NODEID> the ID type of the Node, e.g., String.
 * @param <LINKID> the ID type of the Link, e.g., String.
 */
public interface Network<LINKID, NODEID>
{
    /** @return the nodes. */
    Map<NODEID, Node<NODEID>> getNodeMap();

    /** @return the links. */
    Map<LINKID, Link<LINKID, NODEID>> getLinkMap();

    /** @return the defined routes in the network. */
    Map<String, Route<LINKID, NODEID>> getRouteMap();

    /**
     * @param nodeFrom the start node.
     * @param nodeTo the end node.
     * @return the routes between two nodes in the network.
     */
    Set<Route<LINKID, NODEID>> getRoutesBetween(Node<NODEID> nodeFrom, Node<NODEID> nodeTo);

    /**
     * @param nodeFrom the start node.
     * @param nodeTo the end node.
     * @return the shortest route between two nodes in the network.
     */
    Route<LINKID, NODEID> getShortestRouteBetween(Node<NODEID> nodeFrom, Node<NODEID> nodeTo);

    /**
     * @param node the node to add to the network.
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    void addNode(Node<NODEID> node) throws NetworkException;

    /**
     * @param node the node to remove from the network.
     * @throws NetworkException if node does not exist in the network.
     */
    void removeNode(Node<NODEID> node) throws NetworkException;

    /**
     * @param node the node to search for in the network.
     * @return whether the node is in this network
     */
    boolean containsNode(Node<NODEID> node);

    /**
     * @param nodeId the id of the node to search for in the network.
     * @return whether the node is in this network
     */
    boolean containsNode(NODEID nodeId);

    /**
     * @param nodeId the id of the node to search for in the network.
     * @return the node or null if not present
     */
    Node<NODEID> getNode(NODEID nodeId);

    /**
     * @param link the link to add to the network.
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    void addLink(Link<LINKID, NODEID> link) throws NetworkException;

    /**
     * @param link the link to remove from the network.
     * @throws NetworkException if link does not exist in the network.
     */
    void removeLink(Link<LINKID, NODEID> link) throws NetworkException;

    /**
     * @param link the link to search for in the network.
     * @return whether the link is in this network
     */
    boolean containsLink(Link<LINKID, NODEID> link);

    /**
     * @param link the id of the link to search for in the network.
     * @return whether the link is in this network
     */
    boolean containsLink(LINKID link);

    /**
     * @param linkId the id of the link to search for in the network.
     * @return the link or null if not present
     */
    Link<LINKID, NODEID> getLink(LINKID linkId);

    /**
     * Find a link between node1 and node2 and return it if it exists in the network. If not, return null.
     * @param node1 first node
     * @param node2 second node
     * @return the link between node1 and node2 in the network or null if it does not exist.
     */
    Link<LINKID, NODEID> getLink(Node<NODEID> node1, Node<NODEID> node2);
    
    /**
     * Find a link between node1 and node2 and return it if it exists in the network. If not, return null.
     * @param node1 id of the first node
     * @param node2 id of the second node
     * @return the link between node1 and node2 in the network or null if it does not exist.
     * @throws NetworkException if the node(s) cannot be found by their id
     */
    Link<LINKID, NODEID> getLink(NODEID node1, NODEID node2) throws NetworkException;
    
    /**
     * @param route the route to add to the network.
     * @throws NetworkException if route already exists in the network, if name of the route is not unique, if one of the nodes
     *             of the route are not registered in the network.
     */
    void addRoute(Route<LINKID, NODEID> route) throws NetworkException;

    /**
     * @param route the route to remove from the network.
     * @throws NetworkException if route does not exist in the network.
     */
    void removeRoute(Route<LINKID, NODEID> route) throws NetworkException;

    /**
     * @param route the route to search for in the network.
     * @return whether the route is in this network
     */
    boolean containsRoute(Route<LINKID, NODEID> route);
    
    /**
     * @param routeId the route to search for in the network.
     * @return whether the route is in this network
     */
    boolean containsRoute(String routeId);
    
    /**
     * @param routeId the route to search for in the network.
     * @return the route or null if not present
     */
    Route<LINKID, NODEID> getRoute(String routeId);
}

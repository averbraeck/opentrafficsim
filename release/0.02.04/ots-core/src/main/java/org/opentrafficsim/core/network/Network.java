package org.opentrafficsim.core.network;

import java.util.Map;
import java.util.Set;

import org.opentrafficsim.core.network.route.Route;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public interface Network
{
    /** @return the nodes. */
    Map<String, Node> getNodeMap();

    /** @return the links. */
    Map<String, Link> getLinkMap();

    /** @return the defined routes in the network. */
    Map<String, Route> getRouteMap();

    /**
     * @param nodeFrom the start node.
     * @param nodeTo the end node.
     * @return the routes between two nodes in the network.
     */
    Set<Route> getRoutesBetween(Node nodeFrom, Node nodeTo);

    /**
     * @param nodeFrom the start node.
     * @param nodeTo the end node.
     * @return the shortest route between two nodes in the network.
     */
    Route getShortestRouteBetween(Node nodeFrom, Node nodeTo);

    /**
     * @param node the node to add to the network.
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    void addNode(Node node) throws NetworkException;

    /**
     * @param node the node to remove from the network.
     * @throws NetworkException if node does not exist in the network.
     */
    void removeNode(Node node) throws NetworkException;

    /**
     * @param node the node to search for in the network.
     * @return whether the node is in this network
     */
    boolean containsNode(Node node);

    /**
     * @param nodeId the id of the node to search for in the network.
     * @return whether the node is in this network
     */
    boolean containsNode(String nodeId);

    /**
     * @param nodeId the id of the node to search for in the network.
     * @return the node or null if not present
     */
    Node getNode(String nodeId);

    /**
     * @param link the link to add to the network.
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    void addLink(Link link) throws NetworkException;

    /**
     * @param link the link to remove from the network.
     * @throws NetworkException if link does not exist in the network.
     */
    void removeLink(Link link) throws NetworkException;

    /**
     * @param link the link to search for in the network.
     * @return whether the link is in this network
     */
    boolean containsLink(Link link);

    /**
     * @param link the id of the link to search for in the network.
     * @return whether the link is in this network
     */
    boolean containsLink(String link);

    /**
     * @param linkId the id of the link to search for in the network.
     * @return the link or null if not present
     */
    Link getLink(String linkId);

    /**
     * Find a link between node1 and node2 and return it if it exists in the network. If not, return null.
     * @param node1 first node
     * @param node2 second node
     * @return the link between node1 and node2 in the network or null if it does not exist.
     */
    Link getLink(Node node1, Node node2);

    /**
     * Find a link between node1 and node2 and return it if it exists in the network. If not, return null.
     * @param node1 id of the first node
     * @param node2 id of the second node
     * @return the link between node1 and node2 in the network or null if it does not exist.
     * @throws NetworkException if the node(s) cannot be found by their id
     */
    Link getLink(String node1, String node2) throws NetworkException;

    /**
     * @param route the route to add to the network.
     * @throws NetworkException if route already exists in the network, if name of the route is not unique, if one of the nodes
     *             of the route are not registered in the network.
     */
    void addRoute(Route route) throws NetworkException;

    /**
     * @param route the route to remove from the network.
     * @throws NetworkException if route does not exist in the network.
     */
    void removeRoute(Route route) throws NetworkException;

    /**
     * @param route the route to search for in the network.
     * @return whether the route is in this network
     */
    boolean containsRoute(Route route);

    /**
     * @param routeId the route to search for in the network.
     * @return whether the route is in this network
     */
    boolean containsRoute(String routeId);

    /**
     * @param routeId the route to search for in the network.
     * @return the route or null if not present
     */
    Route getRoute(String routeId);
}

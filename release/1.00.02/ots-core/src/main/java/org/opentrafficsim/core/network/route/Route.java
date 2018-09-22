package org.opentrafficsim.core.network.route;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * A Route consists of a list of Nodes. A route does not have to be complete. As long as all 'splitting' nodes are part of the
 * route and have a valid successor node (connected by a Link), the strategical planner is able to make a plan. An extension of
 * the Route class exists that contains a complete route, where all nodes on the route have to be present and connected.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jan 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Route implements Serializable, Identifiable
{
    /** */
    private static final long serialVersionUID = 20150101L;

    /** The nodes of the route. */
    private final List<Node> nodes;

    /** The nodes of the route as a Set for quick containsNode() method. */
    private final Set<Node> nodeSet = new HashSet<>();

    /** Name of the route. */
    private final String id;

    /**
     * Create an empty route.
     * @param id the name of the route.
     */
    public Route(final String id)
    {
        this.nodes = new ArrayList<>();
        this.id = id;
    }

    /**
     * Create a route based on an initial list of nodes. <br>
     * @param nodes the initial list of nodes.
     * @param id the name of the route.
     */
    public Route(final String id, final List<Node> nodes)
    {
        this.id = id;
        this.nodes = new ArrayList<>(nodes); // defensive copy
        this.nodeSet.addAll(nodes);
    }

    /**
     * Add a node to the end of the node list.
     * @param node the node to add.
     * @return Route; this route for method chaining
     * @throws NetworkException in case node could not be added to the route.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public Route addNode(final Node node) throws NetworkException
    {
        this.nodes.add(node);
        this.nodeSet.add(node);
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
     * @param i the index of the node to obtain
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
     * @return whether the route contains this node (quick using HashSet);
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
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "Route [id=" + this.id + ", nodes=" + this.nodes + "]";
    }

    /**
     * Clone the Route.
     * @param newNetwork the new network
     * @param newSimulator the new simulator for this network
     * @param animation whether to (re)create animation or not
     * @return a clone of this route
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public Route clone(final Network newNetwork, final SimulatorInterface.TimeDoubleUnit newSimulator, final boolean animation)
            throws NetworkException
    {
        Route newRoute = new Route(this.id);
        for (Node node : this.nodes)
        {
            newRoute.addNode(newNetwork.getNode(node.getId()));
        }
        return newRoute;
    }
}

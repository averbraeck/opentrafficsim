package org.opentrafficsim.core.network.route;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.lane.CrossSectionLink;

/**
 * A Route consists of a list of Nodes. The last visited node is kept. Code can ask what the next node is, and can
 * indicate the next node to visit. Routes can be expanded (e.g. for node expansion), collapsed (e.g. to use a macro
 * model for a part of the route) or changed (e.g. to avoid congestion). Changing is done by adding and/or removing
 * nodes of the node list. When the last visited node of the route is deleted, however, it is impossible to follow the
 * route any further, which will result in a <code>NetworkException</code>.<br>
 * The Node type in the list has generic parameters. This requires callers of getNode(), visitNextNode(), etc. to cast
 * the result as needed. and permits inclusion mixed types of Nodes in one Route.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jan 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Route implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150101L;

    /** The nodes of the route. */
    private final List<Node<?, ?>> nodes;

    /** last visited node on the route. */
    private int lastNode = -1;

    /**
     * Create an empty route.
     */
    public Route()
    {
        this.nodes = new ArrayList<Node<?, ?>>();
    }

    /**
     * Create a route based on an initial list of nodes. <br>
     * This constructor makes a defensive copy of the provided List.
     * @param nodes the initial list of nodes.
     */
    public Route(final List<Node<?, ?>> nodes)
    {
        // Make a defensive copy so constructors of GTUs can alter and re-use their List to construct other GTUs
        this.nodes = new ArrayList<Node<?, ?>>(nodes);
    }

    /**
     * Add a node to the end of the node list.
     * @param node the node to add.
     * @return whether the add was successful.
     * @throws NetworkException when node could not be added.
     */
    public final boolean addNode(final Node<?, ?> node) throws NetworkException
    {
        try
        {
            return this.nodes.add(node);
        }
        catch (RuntimeException e)
        {
            throw new NetworkException("Route.addNode(Node) could not be executed", e);
        }
    }

    /**
     * Add a node at a specific location.
     * @param i the location to put the node (0-based).
     * @param node the node to add.
     * @throws NetworkException when i&lt;0 or i&gt;=nodes.size(). Also thrown when another error occurs.
     */
    public final void addNode(final int i, final Node<?, ?> node) throws NetworkException
    {
        try
        {
            this.nodes.add(i, node);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new NetworkException("Route.addNode(i, Node) was called where i<0 or i>=nodes.size()");
        }
        catch (RuntimeException e)
        {
            throw new NetworkException("Route.addNode(i, Node) could not be executed", e);
        }
        // Do this AFTER executing the insert operation (otherwise we could/would increment lastNode and then fail to
        // insert the node).
        if (i <= this.lastNode)
        {
            // quite useless, as we have already done that part of the route, but we have to keep consistency!
            this.lastNode++;
        }
    }

    /**
     * Remove a node from a specific location.
     * @param i the location to remove the node from (0-based).
     * @return the removed node.
     * @throws NetworkException when i is equal to the last visited node because the next link on the route cannot be
     *             computed anymore. Also thrown when another error occurs.
     */
    public final Node<?, ?> removeNode(final int i) throws NetworkException
    {
        Node<?, ?> result = null;
        if (i == this.lastNode)
        {
            throw new NetworkException("Route.removeNode(i) was called where i was equal to the last visited node");
        }
        try
        {
            result = this.nodes.remove(i);
        }
        catch (RuntimeException e)
        {
            throw new NetworkException("Route.removeNode(i, Node) could not be executed", e);
        }
        // Do this AFTER the removal operation has succeeded.
        if (i < this.lastNode)
        {
            // quite useless, as we have already done that part of the route, but we have to keep consistency!
            this.lastNode--;
        }
        return result;
    }

    /**
     * Return a node at a specific location.
     * @param i the location to get the node from (0-based).
     * @return the retrieved node.
     * @throws NetworkException when i&lt;0 or i&gt;=nodes.size().
     */
    public final Node<?, ?> getNode(final int i) throws NetworkException
    {
        try
        {
            return this.nodes.get(i);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new NetworkException("Route.getNode(i) was called where i<0 or i>=nodes.size()");
        }
    }

    /**
     * @return the first node of the route.
     * @throws NetworkException when the list has no nodes.
     */
    public final Node<?, ?> originNode() throws NetworkException
    {
        if (this.nodes.size() == 0)
        {
            throw new NetworkException("Route.getOrigin() called, but node list has no nodes");
        }
        return getNode(0);
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
    public final Node<?, ?> destinationNode() throws NetworkException
    {
        if (this.nodes.size() == 0)
        {
            throw new NetworkException("Route.getDestination() called, but node list has no nodes");
        }
        return getNode(size() - 1);
    }

    /**
     * @return the last visited node of the route, and null when no nodes have been visited yet.
     * @throws NetworkException when the index is out of bounds (should never happen).
     */
    public final Node<?, ?> lastVisitedNode() throws NetworkException
    {
        if (this.lastNode == -1)
        {
            return null;
        }
        return getNode(this.lastNode);
    }

    /**
     * This method does <b>not</b> advance the route pointer.
     * @return the next node of the route to visit, and null when we already reached the destination.
     * @throws NetworkException when the index is out of bounds (should never happen).
     */
    public final Node<?, ?> nextNodeToVisit() throws NetworkException
    {
        if (this.lastNode >= size() - 1)
        {
            return null;
        }
        return getNode(this.lastNode + 1);
    }

    /**
     * This method <b>does</b> advance the route pointer (if possible).
     * @return the next node of the route to visit, and null when we already reached the destination.
     * @throws NetworkException when the index is out of bounds (should never happen).
     */
    public final Node<?, ?> visitNextNode() throws NetworkException
    {
        if (this.lastNode >= size() - 1)
        {
            return null;
        }
        this.lastNode++;
        return getNode(this.lastNode);
    }

    /**
     * Determine if this Route contains the specified Link.
     * @param link the link to check in the route.
     * @return whether the link is part of the route or not.
     */
    public final boolean containsLink(final CrossSectionLink<?, ?> link)
    {
        Node<?, ?> sn = link.getStartNode();
        Node<?, ?> en = link.getEndNode();
        for (int index = 1; index < this.nodes.size(); index++)
        {
            if (this.nodes.get(index) == en)
            {
                if (this.nodes.get(index - 1) == sn)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return the index of a Node in this Route, or -1 if this Route does not contain the specified Node. <br>
     * If this route contains the Node more than once, the index of the first is returned.
     * @param node Node&lt;?, ?&gt;; the Node to find
     * @return int;
     */
    public final int indexOf(final Node<?, ?> node)
    {
        return this.nodes.indexOf(node);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        final String currentLocationMark = "<>";
        result.append("Route: [");
        String separator = "";
        if (this.lastNode < 0)
        {
            result.append(currentLocationMark);
        }
        for (int index = 0; index < this.nodes.size(); index++)
        {
            Node<?, ?> node = this.nodes.get(index);
            result.append(separator + node);
            if (index == this.lastNode)
            {
                result.append(" " + currentLocationMark); // Indicate current position in the route
            }
            separator = ", ";
        }
        result.append("]");
        return result.toString();
    }

}

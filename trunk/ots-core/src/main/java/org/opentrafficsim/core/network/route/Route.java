package org.opentrafficsim.core.network.route;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;

/**
 * A Route consists of a list of Nodes.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jan 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <NODEID> the ID type of the Node, e.g., String.
 * @param <LINKID> the ID type of the Link, e.g., String.
 */
public class Route<LINKID, NODEID> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150101L;

    /** The nodes of the route. */
    private final List<Node<NODEID>> nodes;

    /** name of the route. */
    private final String id;

    /**
     * Create an empty route.
     * @param id the name of the route.
     */
    public Route(final String id)
    {
        this.nodes = new ArrayList<Node<NODEID>>();
        this.id = id;
    }

    /**
     * Create a route based on an initial list of nodes. <br>
     * @param nodes the initial list of nodes.
     * @param id the name of the route.
     */
    public Route(final String id, final List<Node<NODEID>> nodes)
    {
        this.id = id;
        this.nodes = nodes;
    }

    /**
     * Add a node to the end of the node list.
     * @param node the node to add.
     * @throws NetworkException in case node could not be added to the route.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void addNode(final Node<NODEID> node) throws NetworkException
    {
        this.nodes.add(node);
    }

    /**
     * @return nodes.
     */
    public final List<Node<NODEID>> getNodes()
    {
        return this.nodes;
    }

    /**
     * @param i the index of the node to obtain
     * @return node i.
     * @throws NetworkException if i &lt; 0 or i &gt; size
     */
    public final Node<NODEID> getNode(final int i) throws NetworkException
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
    public final Node<NODEID> originNode() throws NetworkException
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
    public final Node<NODEID> destinationNode() throws NetworkException
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
     * @param node Node&lt;?, ?&gt;; the Node to find
     * @return int;
     */
    public final int indexOf(final Node<NODEID> node)
    {
        return this.nodes.indexOf(node);
    }

    /**
     * @return name.
     */
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

}

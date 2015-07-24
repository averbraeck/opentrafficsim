package org.opentrafficsim.core.network.route;

import java.util.List;

import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;

/**
 * A CompleteRoute is a Route with directly connected Nodes.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <NODEID> the ID type of the Node, e.g., String.
 * @param <LINKID> the ID type of the Link, e.g., String.
 */
public class CompleteRoute<LINKID, NODEID> extends Route<LINKID, NODEID>
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /**
     * Create an empty route.
     * @param id the name of the route.
     */
    public CompleteRoute(final String id)
    {
        super(id);
    }

    /**
     * Create a route based on an initial list of nodes. <br>
     * This constructor makes a defensive copy of the provided List.
     * @param id the name of the route.
     * @param nodes the initial list of nodes.
     * @throws NetworkException if intermediate nodes are missing in the route.
     */
    public CompleteRoute(final String id, final List<Node<NODEID>> nodes) throws NetworkException
    {
        super(id, nodes);
        Node<NODEID> fromNode = null;
        for (Node<NODEID> toNode : getNodes())
        {
            if (null != fromNode)
            {
                if (!isDirectlyConnected(fromNode, toNode))
                {
                    throw new NetworkException("CompleteRoute: node " + fromNode + " not directly connected to node "
                            + toNode);
                }
            }
            fromNode = toNode;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void addNode(final Node<NODEID> node) throws NetworkException
    {
        if (getNodes().size() > 0)
        {
            Node<NODEID> lastNode = getNodes().get(getNodes().size() - 1);
            if (!isDirectlyConnected(lastNode, node))
            {
                throw new NetworkException("CompleteRoute: last node " + lastNode + " not directly connected to node "
                    + node);
            }
        }
        super.addNode(node);
    }

    /**
     * Check if two nodes are directly linked in the specified direction.
     * @param fromNode the from node
     * @param toNode the to node
     * @return whether two nodes are directly linked in the specified direction.
     */
    private boolean isDirectlyConnected(final Node<NODEID> fromNode, final Node<NODEID> toNode)
    {
        for (Link<?, NODEID> link : fromNode.getLinksOut())
        {
            if (toNode.equals(link.getEndNode()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if this Route contains the specified Link.
     * @param link the link to check in the route.
     * @return whether the link is part of the route or not.
     */
    public final boolean containsLink(final Link<LINKID, NODEID> link)
    {
        Node<NODEID> sn = link.getStartNode();
        Node<NODEID> en = link.getEndNode();
        for (int index = 1; index < size(); index++)
        {
            if (getNodes().get(index) == en && getNodes().get(index - 1) == sn)
            {
                return true;
            }
        }
        return false;
    }

}

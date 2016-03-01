package org.opentrafficsim.core.network.route;

import java.util.List;

import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;

/**
 * A CompleteRoute is a Route with directly connected Nodes.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CompleteRoute extends Route
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** the GTUType for which this is a route. */
    private final GTUType gtuType;

    /**
     * Create an empty route for the given GTUType.
     * @param id the name of the route
     * @param gtuType the GTUType for which this is a route
     */
    public CompleteRoute(final String id, final GTUType gtuType)
    {
        super(id);
        this.gtuType = gtuType;
    }

    /**
     * Create a route based on an initial list of nodes. <br>
     * This constructor makes a defensive copy of the provided List.
     * @param id the name of the route.
     * @param gtuType the GTUType for which this is a route
     * @param nodes the initial list of nodes.
     * @throws NetworkException if intermediate nodes are missing in the route.
     */
    public CompleteRoute(final String id, final GTUType gtuType, final List<Node> nodes) throws NetworkException
    {
        super(id, nodes);
        this.gtuType = gtuType;
        Node fromNode = null;
        for (Node toNode : getNodes())
        {
            if (null != fromNode)
            {
                if (!fromNode.isDirectionallyConnectedTo(this.gtuType, toNode))
                {
                    throw new NetworkException("CompleteRoute: node " + fromNode
                        + " not directly or not directionally connected to node " + toNode);
                }
            }
            fromNode = toNode;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void addNode(final Node node) throws NetworkException
    {
        if (getNodes().size() > 0)
        {
            Node lastNode = getNodes().get(getNodes().size() - 1);
            if (!lastNode.isDirectionallyConnectedTo(this.gtuType, node))
            {
                throw new NetworkException("CompleteRoute: last node " + lastNode
                    + " not directly or not directionally connected to node " + node);
            }
        }
        super.addNode(node);
    }

    /**
     * Determine if this Route contains the specified Link.
     * @param link the link to check in the route.
     * @return whether the link is part of the route or not.
     */
    public final boolean containsLink(final Link link)
    {
        Node sn = link.getStartNode();
        Node en = link.getEndNode();
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

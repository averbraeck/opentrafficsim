package org.opentrafficsim.core.network.route;

import java.util.List;

import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * A CompleteRoute is a Route with directly connected Nodes.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** The GTUType for which this is a route. */
    private final GTUType gtuType;

    /**
     * Create an empty route for the given GTUType.
     * @param id String; the name of the route
     * @param gtuType GTUType; the GTUType for which this is a route
     */
    public CompleteRoute(final String id, final GTUType gtuType)
    {
        super(id);
        this.gtuType = gtuType;
    }

    /**
     * Create a route based on an initial list of nodes. <br>
     * This constructor makes a defensive copy of the provided List.
     * @param id String; the name of the route.
     * @param gtuType GTUType; the GTUType for which this is a route
     * @param nodes List&lt;Node&gt;; the initial list of nodes.
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
    public final CompleteRoute addNode(final Node node) throws NetworkException
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
        return this;
    }

    /**
     * Determine if this Route contains the specified Link.
     * @param link Link; the link to check in the route.
     * @return whether the link is part of the route or not.
     */
    public final boolean containsLink(final Link link)
    {
        int index1 = getNodes().indexOf(link.getStartNode());
        int index2 = getNodes().indexOf(link.getEndNode());
        return index1 >= 0 && index2 >= 0 && Math.abs(index2 - index1) == 1;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CompleteRoute [gtuType=" + this.gtuType + ", nodes=" + super.getNodes() + "]";
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public CompleteRoute clone(final Network newNetwork, final SimulatorInterface.TimeDoubleUnit newSimulator)
            throws NetworkException
    {
        CompleteRoute newRoute = new CompleteRoute(getId(), this.gtuType);
        for (Node node : getNodes())
        {
            newRoute.addNode(newNetwork.getNode(node.getId()));
        }
        return newRoute;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gtuType == null) ? 0 : gtuType.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompleteRoute other = (CompleteRoute) obj;
        if (gtuType == null)
        {
            if (other.gtuType != null)
                return false;
        }
        else if (!gtuType.equals(other.gtuType))
            return false;
        return true;
    }

}

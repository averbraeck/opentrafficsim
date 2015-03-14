package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A Network consists of a set of links. Each link has, in its turn, a start node and an end node.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID> the ID type of the network.
 * @param <L>
 */
public class Network<ID, L extends Link<?, ?>> extends HashSet<L> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150102L;

    /** network id. */
    private final ID id;

    /** HashSet of Nodes. */
    private Set<Node<?, ?>> nodeSet = new HashSet<>();

    /**
     * Construction of an empty network.
     * @param id the network id.
     */
    public Network(final ID id)
    {
        super();
        this.id = id;
    }

    /**
     * Construction of a network with an initial set of links.
     * @param id the network id.
     * @param collection the initial collection of links.
     */
    public Network(final ID id, final Collection<? extends L> collection)
    {
        super(collection);
        this.id = id;
    }

    /**
     * @return id
     */
    public final ID getId()
    {
        return this.id;
    }

    /**
     * @return nodeSet
     */
    public final Set<Node<?, ?>> getNodeSet()
    {
        return this.nodeSet;
    }

    /**
     * @param nodeSet set nodeSet
     */
    public final void setNodeSet(final Set<Node<?, ?>> nodeSet)
    {
        this.nodeSet = nodeSet;
    }

    /**
     * Determine if a node is part of this Network.
     * @param node AbstractNode&lt;?, ?&gt;; the node
     * @return true or false
     */
    public final boolean isInNetwork(final Node<?, ?> node)
    {
        return this.nodeSet.contains(node);
    }

    /**
     * Add a node to this Network.
     * @param node Node; the node that must be added
     * @throws NetworkException if the node is already part of this network
     */
    public final void addNode(final Node<?, ?> node) throws NetworkException
    {
        if (isInNetwork(node))
        {
            throw new NetworkException("Adding Node " + node.getId().toString() + ". This Node is  already in the Set");
        }
        else
        {
            this.nodeSet.add(node);
        }
    }

    /**
     * Delete a node from this network.
     * @param deleteThis AbstractNode&lt;?, ?&gt;; the node that must be deleted
     * @return boolean
     * @throws NetworkException on network inconsistency Note: method can be overridden, e.g. by the ExpansionNetwork.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public boolean deleteNode(final Node<?, ?> deleteThis) throws NetworkException
    {
        // TODO ensure that no links are orphaned due to removal of the node
        if (isInNetwork(deleteThis))
        {
            this.nodeSet.remove(deleteThis);
            return true;
        }
        throw new NetworkException("Deleting" + deleteThis.getId().toString() + " failed. Possible cause:"
            + " node is not a member of the given Network");
    }
}

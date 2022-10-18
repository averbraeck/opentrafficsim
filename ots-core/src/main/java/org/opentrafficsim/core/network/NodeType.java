package org.opentrafficsim.core.network;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.HierarchicalType;

/**
 * Node type to indicate different types of Nodes. At the moment there are two types of Nodes, the Centroid and the regular
 * Node. Note that hashCode() and equals() for this class should NOT incorporate the network. The superclass hashCode() and
 * equals() methods that just involve the id and parent ensure that NodeTypes registered IN a network with the same name as the
 * default NodeTypes below, will be equal to the default NodeTypes, although they are a different instance.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class NodeType extends HierarchicalType<NodeType, Node>
{
    /** */
    private static final long serialVersionUID = 20140821L;

    /** the network to which the NodeType belongs. */
    private final Network network;

    /** Name of default type NODE. */
    public static final String NODE_NAME = "NODE";

    /** Default type NODE. */
    public static final NodeType NODE = new NodeType(NODE_NAME, null);

    /** Name of default type CENTROID. */
    public static final String CENTROID_NAME = "CENTROID";

    /** Default type CENTROID. */
    public static final NodeType CENTROID = new NodeType(CENTROID_NAME, null);

    /**
     * Create a new Node type.
     * @param id String; the id of the node type (may not be null)
     * @param parent NodeType; the parent type (may be null)
     * @param network Network; The network to which the NodeType belongs (may not be null)
     */
    public NodeType(final String id, final NodeType parent, final Network network)
    {
        super(id, parent);
        Throw.whenNull(network, "network should not be null");
        this.network = network;
        // TODO: this.network.addNodeType(this);
    }

    /**
     * Create a new default Node type.
     * @param id String; the id of the node type (may not be null)
     * @param parent NodeType; the parent type (may be null)
     */
    protected NodeType(final String id, final NodeType parent)
    {
        super(id, parent);
        this.network = null;
    }

    /**
     * Return whether this is a centroid.
     * @return boolean; whether this is a {@code CENTROID}
     */
    public final boolean isCentroid()
    {
        return this.equals(CENTROID);
    }

    /**
     * Return whether this is a regular node.
     * @return boolean; whether this is a regular node
     */
    public final boolean isNode()
    {
        return this.equals(NODE);
    }

    /**
     * Return the network (may be null for default types).
     * @return the network to which the NodeType belongs
     */
    public Network getNetwork()
    {
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "NodeType [id=" + getId() + "]";
    }

}

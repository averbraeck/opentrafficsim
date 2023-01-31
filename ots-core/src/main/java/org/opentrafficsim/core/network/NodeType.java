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
     */
    public NodeType(final String id)
    {
        super(id, null);
    }

    /**
     * Create a new default Node type.
     * @param id String; the id of the node type (may not be null)
     * @param parent NodeType; the parent type (may be null)
     */
    public NodeType(final String id, final NodeType parent)
    {
        super(id, parent);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "NodeType [id=" + getId() + "]";
    }

}

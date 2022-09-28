package org.opentrafficsim.road.network.sampling;

import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.kpi.interfaces.NodeDataInterface;

/**
 * Node representation in road sampler.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class NodeData implements NodeDataInterface
{

    /** Node. */
    private final Node node;

    /**
     * @param node Node; node
     */
    public NodeData(final Node node)
    {
        this.node = node;
    }

    /**
     * @return node.
     */
    public final Node getNode()
    {
        return this.node;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.node.getId();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.node == null) ? 0 : this.node.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        NodeData other = (NodeData) obj;
        if (this.node == null)
        {
            if (other.node != null)
            {
                return false;
            }
        }
        else if (!this.node.equals(other.node))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "NodeData [node=" + this.node + "]";
    }

}

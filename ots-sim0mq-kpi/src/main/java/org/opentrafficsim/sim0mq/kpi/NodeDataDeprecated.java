package org.opentrafficsim.sim0mq.kpi;

import nl.tudelft.simulation.language.d3.CartesianPoint;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class NodeDataDeprecated // implements NodeDataInterface
{

    /** Node name. */
    private final String nodeName;

    /** position. */
    private final CartesianPoint position;

    /**
     * @param nodeName String; name of the node
     * @param position CartesianPoint; position of the node
     */
    public NodeDataDeprecated(final String nodeName, final CartesianPoint position)
    {
        this.nodeName = nodeName;
        this.position = position;
    }

    /**
     * @return nodeName
     */
    public final String getNodeName()
    {
        return this.nodeName;
    }

    /**
     * @return position
     */
    public final CartesianPoint getPosition()
    {
        return this.position;
    }

    /** {@inheritDoc} */
    // @Override
    public String getId()
    {
        return this.nodeName;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.nodeName == null) ? 0 : this.nodeName.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NodeDataDeprecated other = (NodeDataDeprecated) obj;
        if (this.nodeName == null)
        {
            if (other.nodeName != null)
                return false;
        }
        else if (!this.nodeName.equals(other.nodeName))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "NodeData [nodeName=" + this.nodeName + ", position=" + this.position + "]";
    }

}

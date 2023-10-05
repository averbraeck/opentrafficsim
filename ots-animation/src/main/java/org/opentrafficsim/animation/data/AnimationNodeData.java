package org.opentrafficsim.animation.data;

import java.rmi.RemoteException;

import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.draw.ClickableBounds;
import org.opentrafficsim.draw.network.NodeAnimation.NodeData;

/**
 * Animation data of a Node.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AnimationNodeData implements NodeData
{

    /** Node */
    private final Node node;

    /**
     * Constructor.
     * @param node Node; node.
     */
    public AnimationNodeData(final Node node)
    {
        this.node = node;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds<?, ?, ?> getBounds() throws RemoteException
    {
        return ClickableBounds.get(this.node.getBounds());
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.node.getId();
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        return this.node.getLocation();
    }
    
    /**
     * Returns the node.
     * @return Node; node.
     */
    public Node getNode()
    {
        return this.node;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Node " + this.node.getId();
    }

}

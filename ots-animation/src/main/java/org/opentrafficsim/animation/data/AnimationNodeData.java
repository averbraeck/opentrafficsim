package org.opentrafficsim.animation.data;

import org.djutils.draw.line.Polygon2d;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.draw.network.NodeAnimation.NodeData;

/**
 * Animation data of a Node.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationNodeData extends AnimationIdentifiableShape<Node> implements NodeData
{

    /**
     * Constructor.
     * @param node node.
     */
    public AnimationNodeData(final Node node)
    {
        super(node);
    }

    @Override
    public Polygon2d getAbsoluteContour()
    {
        throw new UnsupportedOperationException("Nodes do not have a drawable contour.");
    }

    @Override
    public Polygon2d getRelativeContour()
    {
        throw new UnsupportedOperationException("Nodes do not have a drawable contour.");
    }

    @Override
    public String toString()
    {
        return "Node " + getId();
    }

}

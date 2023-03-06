package org.opentrafficsim.core.network;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.geometry.OtsPoint3d;

/**
 * Centroid Node that is only connected by Connector links.
 * <p>
 * Copyright (c) 2022-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class Centroid extends Node
{
    /** */
    private static final long serialVersionUID = 20221117L;

    /**
     * Construction of a Centroid Node.
     * @param network Network; the network.
     * @param id String; the id of the Node.
     * @param point OtsPoint3d; the point with usually an x and y setting.
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    public Centroid(final Network network, final String id, final OtsPoint3d point) throws NetworkException
    {
        super(network, id, point);
    }

    /** {@inheritDoc} */
    @Override
    public void addLink(final Link link)
    {
        Throw.when(!link.isConnector(), OtsRuntimeException.class, "Centroids can only be connected to Connectors.");
        super.addLink(link);
    }
    
    /** {@inheritDoc} */
    @Override
    public NodeType getType()
    {
        return NodeType.CENTROID;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCentroid()
    {
        return true;
    }

}

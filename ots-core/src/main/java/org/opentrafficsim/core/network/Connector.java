package org.opentrafficsim.core.network;

import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.geometry.OtsLine2d;

/**
 * Special link type that represents a connector.
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Connector extends Link
{

    /** Weight value to divide origin or destination flow over connectors. */
    private double demandWeight = 0.0;

    /**
     * Construct a new link.
     * @param network the network to which the link belongs
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param linkType Link type to indicate compatibility with GTU types
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, if the start node or
     *             the end node of the link are not registered in the network, or if neither node is a centroid.
     */
    public Connector(final Network network, final String id, final Node startNode, final Node endNode, final LinkType linkType)
            throws NetworkException
    {
        super(network, id, startNode, endNode, linkType,
                Try.assign(() -> new OtsLine2d(startNode.getPoint(), endNode.getPoint()), "Could not create connector line."),
                null);
        Throw.when(!startNode.isCentroid() && !endNode.isCentroid(), NetworkException.class,
                "At least on node connected to a Connector should be a centroid.");
    }

    /**
     * Sets the demand weight. This is only applicable to links of type CONNECTOR.
     * @param demandWeight demand weight, which is any positive value
     */
    public final void setDemandWeight(final double demandWeight)
    {
        Throw.when(demandWeight < 0.0, IllegalArgumentException.class, "Demand weight should be positive.");
        this.demandWeight = demandWeight;
    }

    /**
     * Clears the demand weight, which is equal to setting it to 0.0.
     */
    public final void clearDemandWeight()
    {
        this.demandWeight = 0.0;
    }

    /**
     * Returns the demand weight.
     * @return demand weight, any positive value.
     */
    public final double getDemandWeight()
    {
        return this.demandWeight;
    }

    @Override
    public boolean isConnector()
    {
        return true;
    }

}

package org.opentrafficsim.core.network;

import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.core.geometry.FractionalLengthData;
import org.opentrafficsim.core.geometry.OtsLine2d;

/**
 * A link with a maximum capacity, expressed as the maximum number of GTUs per time unit that the link can handle.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class CapacityLink extends Link implements Capacity
{
    /** */
    private static final long serialVersionUID = 20151108L;

    /** Link capacity in vehicles per time unit. This is a mutable property (e.g., blockage). */
    private Frequency capacity;

    /**
     * Construct a new link.
     * @param network Network; the network.
     * @param id String; the link id
     * @param startNode Node; start node (directional)
     * @param endNode Node; end node (directional)
     * @param linkType LinkType; Link type to indicate compatibility with GTU types
     * @param designLine OtsLine2d; the OtsLine2d design line of the Link
     * @param elevation FractionalLengthData; elevation given over fractional length, may be {@code null}.
     * @param capacity Frequency; link capacity in GTUs per hour // XXX per direction? which GtuType?
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public CapacityLink(final Network network, final String id, final Node startNode, final Node endNode,
            final LinkType linkType, final OtsLine2d designLine, final FractionalLengthData elevation, final Frequency capacity)
            throws NetworkException
    {
        super(network, id, startNode, endNode, linkType, designLine, elevation);
        this.capacity = capacity;
    }

    /** {@inheritDoc} */
    @Override
    public final Frequency getCapacity()
    {
        return this.capacity;
    }

    /** {@inheritDoc} */
    @Override
    public final void setCapacity(final Frequency capacity)
    {
        this.capacity = capacity;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "CapacityOTSLink [capacity=" + this.capacity + "]";
    }

}

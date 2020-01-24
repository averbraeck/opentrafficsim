package org.opentrafficsim.core.network;

import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;

/**
 * A link with a maximum capacity, expressed as the maximum number of GTUs per time unit that the link can handle.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 8, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CapacityOTSLink extends OTSLink implements Capacity
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
     * @param designLine OTSLine3D; the OTSLine3D design line of the Link
     * @param simulator OTSSimulatorInterface; the simulator on which events can be scheduled
     * @param capacity Frequency; link capacity in GTUs per hour // XXX per direction? which GTUType?
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public CapacityOTSLink(final Network network, final String id, final Node startNode, final Node endNode,
            final LinkType linkType, final OTSLine3D designLine, final OTSSimulatorInterface simulator,
            final Frequency capacity) throws NetworkException
    {
        super(network, id, startNode, endNode, linkType, designLine, simulator);
        this.capacity = capacity;
    }

    /**
     * Clone a link for a new network.
     * @param newNetwork Network; the new network to which the clone belongs
     * @param newSimulator OTSSimulatorInterface; the new simulator for this network
     * @param link CapacityOTSLink; the link to clone from
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    protected CapacityOTSLink(final Network newNetwork, final OTSSimulatorInterface newSimulator, final CapacityOTSLink link)
            throws NetworkException
    {
        super(newNetwork, newSimulator, link);
        this.capacity = link.capacity;
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

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public CapacityOTSLink clone(final Network newNetwork, final OTSSimulatorInterface newSimulator) throws NetworkException
    {
        return new CapacityOTSLink(newNetwork, newSimulator, this);
    }

}

package org.opentrafficsim.core.network;

import java.util.Map;

import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * A link with a maximum capacity, expressed as the maximum number of GTUs per time unit that the link can handle.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param linkType Link type to indicate compatibility with GTU types
     * @param designLine the OTSLine3D design line of the Link
     * @param capacity link capacity in GTUs per hour // XXX per direction? which GTUType?
     * @param directionalityMap the directions (FORWARD, BACKWARD, BOTH, NONE) that GTUtypes can traverse this link
     */
    public CapacityOTSLink(final String id, final OTSNode startNode, final OTSNode endNode, final LinkType linkType,
        final OTSLine3D designLine, final Frequency capacity,
        final Map<GTUType, LongitudinalDirectionality> directionalityMap)
    {
        super(id, startNode, endNode, linkType, designLine, directionalityMap);
        this.capacity = capacity;
    }

    /**
     * Construct a new link, with a directionality for all GTUs as provided.
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param linkType Link type to indicate compatibility with GTU types
     * @param designLine the OTSLine3D design line of the Link
     * @param capacity link capacity in GTUs per hour
     * @param directionality the directionality for all GTUs
     */
    public CapacityOTSLink(final String id, final OTSNode startNode, final OTSNode endNode, final LinkType linkType,
        final OTSLine3D designLine, final Frequency capacity, final LongitudinalDirectionality directionality)
    {
        super(id, startNode, endNode, linkType, designLine, directionality);
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
    public final String toString()
    {
        return "CapacityOTSLink [capacity=" + this.capacity + "]";
    }
}
